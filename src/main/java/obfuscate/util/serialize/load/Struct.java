package obfuscate.util.serialize.load;

import obfuscate.MsdmPlugin;
import obfuscate.network.BackendManager;
import obfuscate.util.Promise;
import obfuscate.util.serialize.FieldMeta;
import obfuscate.util.serialize.ListMeta;
import obfuscate.util.serialize.ModelSchemaHolder;
import obfuscate.util.serialize.ObjectId;
import obfuscate.util.time.Task;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/** Object that can be constructed from JSON. */
public abstract class Struct extends ModelSchemaHolder {

    protected final Promise<Struct> onFullLoad = new Promise<>();

    /** Recursively wait for all models to be resolved */
    public Promise<Struct> waitFullyInitialized() {
        return onFullLoad;
    }

    @Deprecated
    public String getIdField() {
        return "id";
    }


    public <T extends Struct> Promise<T> load(Object payload) {
        return this.load(payload, false);
    }

    public <T extends Struct> Promise<T> load(Object payload, boolean explicit) {
        Promise<T> loadDone = (Promise<T>) this.loadFromJson(payload, explicit);

        // Fulfill onFullLoad member
        loadDone.thenSync(
            y -> {
                // execute callback in main thread
                new Task(() -> {
                    // it is possible that there would be two sources for updating a single model
                    // like field was updated on model shortly after we requested that model
                    if (!this.onFullLoad.isFulfilled()) {
                        this.onFullLoad.fulfill(this);
                    }
                }, 0).run();
                return y;
            }
        );

        return loadDone;
    }

    private void loadPrimitives(LoadableModelMeta meta, HashMap<String, Object> json) {

        for (FieldMeta primitiveFieldMeta : meta.getPrimitiveFields()) {

            Field primitiveField = primitiveFieldMeta.getField();
            String apiField = primitiveFieldMeta.getApiField();

            primitiveField.setAccessible(true);
            Class<?> model = primitiveField.getType();
            Object value = json.get(apiField);

            if (value == null) {
                try {
                    primitiveField.set(this, null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if (model == Integer.class) {
                try {
                    primitiveField.set(this, ((Long) value).intValue());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (model == UUID.class) {
                try {
                    primitiveField.set(this, UUID.fromString((String) value));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (model == HashMap.class) {

                try {
                    HashMap<String, Object> fieldValue = (HashMap<String, Object>) primitiveField.get(this);
                    HashMap<String, Object> fieldJson;

                    if (value instanceof String) {
                        // dictionaries are transported as strings to avoid graphql fuck me in the ass moments.
                        fieldJson = (HashMap<String, Object>) new JSONParser().parse((String) value);
                    } else {
                        // in other cases, having explicit hashmap is fine
                        fieldJson = (JSONObject) value;
                    }

                    // initialize field if it is empty
                    if (fieldValue == null) {
                        fieldValue = new HashMap<>();
                    }

                    // field is not marked as merging, clean map
                    if (!primitiveFieldMeta.merge()) {
                        fieldValue.clear();
                    }

                    fieldValue.putAll(fieldJson);

                    primitiveField.set(this, fieldValue);
                } catch (IllegalAccessException | ParseException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    // if enum get by ordinal
                    if (model.isEnum()) {
                        primitiveField.set(this, model.getEnumConstants()[((Long) value).intValue()]);
                        continue;
                    }
                    primitiveField.set(this, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Promise<?> loadObjects(LoadableModelMeta meta, HashMap<String, Object> json, boolean isExplicit){
        List<Promise<?>> promises = new ArrayList<>();

        BackendManager Backend = MsdmPlugin.getBackend();

        for (FieldMeta fieldMeta : meta.getObjectFields()) {
            boolean explicit = isExplicit || fieldMeta.isExplicit();
            String apiField = fieldMeta.getApiField();
            Field typeField = fieldMeta.getField();

            typeField.setAccessible(true);

            Class<? extends Struct> model = (Class<? extends Struct>) typeField.getType();

            try {
                // Object constructor will work just fine for any id - id field name can be
                // overridden for uuid case, but it is 'id' by default
                Struct instance;

                // for the objects we have two options:
                // 1) load from json object
                // 2) set id and run sync
                if (!explicit) {

                    var jsonObjectId = json.get(apiField);

                    // foreign key is null, no need to load anything
                    if (jsonObjectId == null) {
                        typeField.set(this, null);
                        continue;
                    }

                    ObjectId foreignKeyId = new ObjectId();
                    var foreignKeyIdPromise = foreignKeyId.load(jsonObjectId, true);

                    // ObjectId struct does not have any foreign keys, so it should always be resolved instantly
                    if (!foreignKeyIdPromise.isFulfilled()) {
                        throw new RuntimeException("ObjectID promise was not resolved instantly");
                    }

                    // we only perform loading if this model wasn't loaded yet
                    if (Backend.getById(foreignKeyId) == null) {

                        ObjectLoadFuture<?> future = Backend.getOrCreateModel(
                                (Class<SyncableObject>) model, // this only works for syncable models actually so cast is theoretically unsafe.
                                foreignKeyId
                        );

                        promises.add(future.getPromise().thenSync(x -> {
                            MsdmPlugin.info("Loaded foreign key " + foreignKeyId + " for " + this);
                            return x;
                        }));

                        instance = future.getObject();
                    } else {
                        // we are using existing model and do not perform any loading
                        instance = Backend.getById(foreignKeyId);
                    }

                } else {
                    HashMap<String, Object> data = (HashMap<String, Object>) json.get(apiField);

                    if (data == null) {
                        MsdmPlugin.logger().warning("Explicit field " + typeField.getName() + " is null on model " + getClass().getSimpleName());
                    }

                    // for explicitly defined objects we want to reuse them
                    // if we don't it won't be possible to preserve/merge state
                    instance = (Struct) typeField.get(this);

                    if (instance == null) {
                        instance = model.getDeclaredConstructor().newInstance();
                    }

                    // explicit fields are loaded from json
                    promises.add(instance.load(data, true));
                }

                typeField.set(this, instance);


            } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                     NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return Promise.gather(promises);
    }

    private Promise<?> loadLists(LoadableModelMeta meta, HashMap<String, Object> json, boolean explicit) {
        List<Promise<?>> promises = new ArrayList<>();

        BackendManager Backend = MsdmPlugin.getBackend();

        for (ListMeta fieldMeta : meta.getListFields()) {
            Field field = fieldMeta.getField();
            boolean isExplicit = explicit || fieldMeta.isExplicit();
            String apiField = fieldMeta.getApiField();
            field.setAccessible(true);
            Class<?> model = fieldMeta.getModel();
            String mapKey = fieldMeta.getKey();

            @Nullable List<Object> value = (List<Object>) json.get(apiField);

            try {
                if (value == null) {
                    field.set(this, null);
                    continue;
                }

                boolean isStruct = Struct.class.isAssignableFrom(model);

                if (isStruct) {
                    if (isExplicit) {
                        ArrayList<Struct> loadedData = new ArrayList<>();
                        ArrayList<Promise<?>> _promises = new ArrayList<>();

                        // iterate over each item of this list and load structs for each
                        for (Object instanceJson : value) {

                            // create new struct
                            Struct instance = Backend.createLoadableModel((Class<? extends Struct>) model);

                            // load explicitly defined object
                            _promises.add(instance.load(instanceJson, true));

                            // save to result list
                            loadedData.add(instance);
                        }

                        // set list value only once loaded to prevent partially loaded data being accessible
                        var promise = Promise.gather(_promises).thenSync(
                            (x) -> {
                                try {
                                    field.set(this, loadedData);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        );

                        promises.add(promise);

                    } else if (mapKey != null) {

                        LoadableMap<?,?> wrappedMap = (LoadableMap<?,?>) field.get(this);

                        // once again I made some stupid convention with myself:
                        // usually loadable/syncable fields have default constructor
                        // but map field requires string constructor where string is the name of key
                        if (wrappedMap == null) {
                            wrappedMap = (LoadableMap<?,?>) field.getType().getDeclaredConstructor(String.class, Class.class).newInstance(mapKey, model);
                        }

                        LoadableMap<?,?> finalWrappedMap = wrappedMap;

                        var promise = wrappedMap.load(value).thenSync(
                            (x) -> {
                                try {
                                    field.set(this, finalWrappedMap);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        );
                        promises.add(promise);

                    } else {
                        ArrayList<Struct> loadedData = new ArrayList<>();
                        ArrayList<Promise<?>> _promises = new ArrayList<>();

                        for (Object key : value) {

                            ObjectId keyValue = new ObjectId();
                            keyValue.load(key, true);

//                            MsdmPlugin.info("Loading list item which is not explicit " + keyValue);
                            // will never load if model was loaded already
                            ObjectLoadFuture<?> future = Backend.getOrCreateModel(
                                    (Class<? extends SyncableObject>) model,
                                    keyValue
                            );
                            _promises.add(future.getPromise());
                            loadedData.add(future.getObject());
                        }

                        // set list value only once loaded to prevent partially loaded data being accessible
                        var promise = Promise.gather(_promises).thenSync(
                                (x) -> {
                                    try {
                                        field.set(this, loadedData);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                        );

                        promises.add(promise);
                    }
                } else {

                    // list of primitives
                    ArrayList<Object> loadedData = new ArrayList<>();
                    for (Object item : value) {
                        if (item instanceof Long) {
                            loadedData.add(((Long) item).intValue());
                        } else {
                            loadedData.add(item);
                        }
                    }
                    field.set(this, loadedData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Promise.gather(promises);
    }
    /**
     * Constructs self from a json
     */
    private Promise<? extends Struct> loadFromJson(Object payload, boolean explicit) {

        List<Promise<?>> promises = new ArrayList<>();
        LoadableModelMeta meta = getClsMeta(this.getClass());
        HashMap<String, Object> json = (HashMap<String, Object>) payload;

        loadPrimitives(meta, json);

        promises.add(loadObjects(meta, json, explicit));

        promises.add(loadLists(meta, json, explicit));

        return Promise.gather(promises).thenSync(x -> this);
    }

    public HashMap<String, Object> serialize() {
        var data = new HashMap<String, Object>();

        for (FieldMeta field : getClsMeta().getPrimitiveFields()) {
            try {
                data.put(field.getApiField(), field.getField().get(this));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        for (FieldMeta field : getClsMeta().getObjectFields()) {

            try {
                var fieldVal = field.getField().get(this);
                if (fieldVal == null) {
                    data.put(field.getApiField(), null);
                    continue;
                }

                // syncable object vs struct
                if (SyncableObject.class.isAssignableFrom(fieldVal.getClass())) {
                    data.put(field.getApiField(), ((SyncableObject) fieldVal).getId());
                } else {
                    data.put(field.getApiField(), ((Struct) fieldVal).serialize());
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        for (ListMeta field : getClsMeta().getListFields()) {
            try {
                List<Object> items;

                Object fieldValue = field.getField().get(this);
                // LoadableMap is constructed from list, so it is considered list field
                if (fieldValue instanceof LoadableMap<?, ?> map) {
                    items = new ArrayList<>(map.values());
                } else {
                    items = (List<Object>) field.getField().get(this);
                }

                if (items == null) {
                    data.put(field.getApiField(), null);
                    continue;
                }

                var serialized = new ArrayList<>();
                for (Object item : items) {

                    if (item instanceof SyncableObject) {
                        serialized.add(((SyncableObject) item).getId());
                    } else {
                        serialized.add(item);
                    }
                }
                data.put(field.getApiField(), serialized);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return data;
    }

    @Override
    public String toString() {
        return "<" + getClass().getSimpleName() + " " + this.serialize() + ">";
    }
}
