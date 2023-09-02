package obfuscate.util.serialize;

import obfuscate.MsdmPlugin;
import obfuscate.util.java.Reflect;
import obfuscate.util.serialize.load.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class ModelSchemaHolder {


    /** Cache storage for this class */
    private static HashMap<Class<? extends ModelSchemaHolder>, LoadableModelMeta> cachedNeta = new HashMap<>();

    public static LoadableModelMeta getClsMeta(Class<? extends ModelSchemaHolder> type) {

        if (cachedNeta.containsKey(type)) {
            return cachedNeta.get(type);
        }

        LoadableModelMeta classMeta = new LoadableModelMeta();

        for (Field field : Reflect.getAllFields(type)) {

            Loadable fieldLoadable = field.getAnnotation(Loadable.class);
            Model classIsModel = type.getAnnotation(Model.class);

            if (classIsModel == null && SyncableObject.class.isAssignableFrom(type)) {
                MsdmPlugin.getInstance().getLogger().info("SyncableObject instance missing @Model");
                continue;
            }

            if (fieldLoadable != null) {
                Class<?> model = field.getType();

                if (List.class.isAssignableFrom(model)) {
                    classMeta.addListField(new ListMeta(field, fieldLoadable.field()));
                    continue;
                }

                // hashmap that provides key arg is constructed from list
                if (LoadableMap.class.isAssignableFrom(model) && fieldLoadable.key().length() > 0) {
                    classMeta.addListField(new ListMeta(field, fieldLoadable.field(), fieldLoadable.key(), Reflect.getGenericArg(field, 1)));
                    continue;
                }

                boolean isPrimitive = !Struct.class.isAssignableFrom(model);

                if (isPrimitive) {
                    classMeta.addPrimitiveField(new FieldMeta(field, fieldLoadable.field(), fieldLoadable.explicit(), fieldLoadable.propagateUpdate(), fieldLoadable.merge()));
                } else {
                    classMeta.addObjectField(new FieldMeta(field, fieldLoadable.field(), fieldLoadable.explicit(), fieldLoadable.propagateUpdate(), fieldLoadable.merge()));
                }
            }
        }
        cachedNeta.put(type, classMeta);
        return classMeta;
    }

    /** Collect all fields with annotations from this class */
    protected LoadableModelMeta getClsMeta() {
        return getClsMeta(this.getClass());
    }

}
