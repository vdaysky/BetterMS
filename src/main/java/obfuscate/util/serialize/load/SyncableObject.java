package obfuscate.util.serialize.load;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.network.ModelEvent;
import obfuscate.logging.Logger;
import obfuscate.logging.Tag;
import obfuscate.network.BackendManager;
import obfuscate.util.Promise;
import obfuscate.util.serialize.FieldMeta;
import obfuscate.util.serialize.ListMeta;
import obfuscate.util.serialize.ObjectId;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;

/** Syncs model with database as-is, without any additional relations */
public abstract class SyncableObject extends Struct {

    protected ObjectId objectId;

    @Nullable
    public ObjectId getId() {
        return objectId;
    }

    public ObjectId getIdWithDeclaredDependencies() {
        var deps = new ArrayList<>(objectId.getDependencies());
        deps.addAll(declaredDependencies());

        return new ObjectId(
                objectId.getObjId(),
                objectId.getEntity(),
                deps
        );
    }

    protected final Promise<SyncableObject> onInitialLoad = new Promise<>();

    public Promise<SyncableObject> getInitializationPromise() {
        return onInitialLoad;
    }

    public String getModelName() {
        return getClass().getAnnotation(Model.class).name();
    }

    public ArrayList<ObjectId> declaredDependencies() {
        return new ArrayList<>();
    }

    /** Turn model fields into GraphQL representation,
     * like {name uuid team { members { uuid } } }
     *
     * @param type model class
     * @return representation of model fields in GraphQL
     * */
    private static String fieldsToGraphQl(Class<? extends Struct> type, boolean isExplicit) {
        LoadableModelMeta meta = getClsMeta(type);

        StringBuilder graphQL = new StringBuilder("{");

        for (FieldMeta field : meta.getPrimitiveFields()) {
            graphQL.append(field.getApiField()).append(" ");
        }

        for (FieldMeta field : meta.getObjectFields()) {
            if (field.isExplicit() || isExplicit) {
                graphQL.append(
                        field.getApiField() + fieldsToGraphQl((Class<? extends SyncableObject>) field.getField().getType(), true)
                ).append(" ");

            } else {
                graphQL.append(
                        field.getApiField() + "{obj_id entity dependencies { obj_id entity } }"
                ).append(" ");
            }

//            // if field is explicit it means it contains more fields,
//            // unlike usual models that are getting replaced with ObjectId
//            if (field.isExplicit()) {
//                graphQL.append(fieldsToGraphQl((Class<? extends SyncableObject>) field.getField().getType(), true));
//            }
        }

        for (ListMeta field : meta.getListFields()) {
            Class<?> listEntry = field.getModel();

            graphQL.append(field.getApiField()).append(" ");

            var isModel = SyncableObject.class.isAssignableFrom(listEntry);

            if (isModel) {
                if (field.isExplicit()) {
                    // if field is explicit it means it contains more fields
                    graphQL.append(fieldsToGraphQl((Class<? extends SyncableObject>) listEntry, true));
                } else {
                    // if field is not explicit it means it contains only ObjectId
                    graphQL.append("{obj_id entity dependencies { obj_id entity } }");
                }
            }
        }
        return graphQL.append("}").toString();
    }

    /** Create a complete GraphQL query for this object */
    public String getGraphQlQuery() {
        return "query {" + getModelName() + "(identifier:" + getIdWithDeclaredDependencies().toGraphQlArg() + ")" +
                fieldsToGraphQl(this.getClass(), false) +
                "}";
    }

    public Promise<?> setId(ObjectId key) {

        // model was already initialized,
        // avoid crazy recursion
        if (Objects.equals(getId(), key)) {
            Promise<?> done = new Promise<>();
            done.fulfill(null);
            return done;
        }

        // Register dependencies of this model
        for (ObjectId dep : declaredDependencies()) {
            MsdmPlugin.getBackend().registerDependency(dep, this);
        }

        MsdmPlugin.getBackend().registerDependency(key, this);

        var keyDeps = key.getDependencies();

        if (keyDeps != null) {
            for (ObjectId dep : keyDeps) {
                MsdmPlugin.getBackend().registerDependency(dep, this);
            }
        }
        // ------------------------------------

        // theoretically it should be done once in static context,
        // but static methods are not inherited
        // so there is no way to analyze descendant class
        this.getClsMeta();

        this.objectId = key;

        BackendManager Backend = MsdmPlugin.getBackend();

        // associate this id with this instance
        Backend.addTrackedModel(this, key);

        return Backend.loadModel(this);
    }

    /** Triggered when model is fully loaded */
    public void onModelUpdate() {

        // trigger event after promise is fulfilled so everyone knows it was updated
        if (this.getFulfilledEvent() == null) {
            return;
        }

        for (Constructor<?> c : this.getFulfilledEvent().getDeclaredConstructors()) {
            Constructor<? extends ModelEvent<?>> constructor = (Constructor<? extends ModelEvent<?>>) c;

            if (constructor.getParameterCount() == 1 && SyncableObject.class.isAssignableFrom(constructor.getParameters()[0].getType())) {
                try {
                    constructor.newInstance(this).trigger();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    Logger.warning("Could not trigger Fulfilled Event on Model", e, Tag.BLAZELINK);
                }
                return;
            }
        }

        Logger.severe("Could not find fitting constructor for event " + this.getFulfilledEvent().getSimpleName(), Tag.BLAZELINK);
    }

    @Override
    public String toString() {
        return this.getModelName() + "(" + this.getClass().getSimpleName() + ")" + "@" + Integer.toHexString(hashCode()) + "<" + getIdField() + "=" + getId() + ">";
    }

    public abstract Class<? extends ModelEvent<? extends SyncableObject>> getFulfilledEvent();

}
