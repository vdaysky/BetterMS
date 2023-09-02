package obfuscate.util.serialize.dump;

import obfuscate.util.java.Reflect;
import obfuscate.util.serialize.ObjectId;
import obfuscate.util.serialize.load.SyncableObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassSerializer {

    private static final HashMap<Class<?>, List<Field>> serializableFields = new HashMap<>();

    private static List<Field> getObjectSerializableFields(Class<?> e) {

        if (!serializableFields.containsKey(e)) {
            List<Field> fields = new ArrayList<>();

            for (Field f : Reflect.getAllFields(e)) {

                // only serialize marked fields
                if (!f.isAnnotationPresent(Serializable.class)) {
                    continue;
                }

                // todo: maybe return type validation
                fields.add(f);
            }
            serializableFields.put(e, fields);
        }
        return serializableFields.get(e);
    }

    public static HashMap<String, Object> serializeClass(Object object) {
        HashMap<String, Object> serialized = new HashMap<>();

        for (Field f : getObjectSerializableFields(object.getClass())) {
            f.setAccessible(true);
            try {
                serialized.put(f.getName(), serializeObject(f.get(object), f.getAnnotation(Serializable.class).serializer()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return serialized;
    }

    public static Object serializeObject(Object obj, String serializerPreference) {

        if (obj == null) {
            return null;
        }

        if (obj instanceof String || obj instanceof Integer || obj instanceof Double || obj instanceof Float || obj instanceof Boolean || obj instanceof List || obj instanceof HashMap) {
            return obj;
        }

        // todo: handle lists / hashmaps of objects
        if (obj instanceof SyncableObject) {
            ObjectId id = ((SyncableObject)obj).getId();
            if (id == null) {
                return null;
            }
            return id.toMap();
        }
        if (obj.getClass().isEnum()) {
            Enum<?> enumValue = (Enum<?>) obj;
            return enumValue.name();
        }

        // primitives and lists/hashmaps are serializable instantly
        if (serializerPreference.length() > 0) {
            // method call
            if (serializerPreference.startsWith(":")) {
                String methodName = serializerPreference.substring(1);
                try {
                    return serializeObject(obj.getClass().getMethod(methodName).invoke(obj), "");
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            } else if (serializerPreference.equals("arbitrary")) { // serialize arbitrary class. We were hoping it would never come to this...
                HashMap<String, Object> serialized = new HashMap<>();
                for (Field field : Reflect.getAllFields(obj.getClass())) {
                    field.setAccessible(true);
                    Object fieldValue;
                    try {
                        fieldValue = field.get(obj);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    serialized.put(field.getName(), serializeObject(fieldValue, ""));
                }
                return serialized;
            }
        }
        throw new RuntimeException("Unknown serializer preference: " + serializerPreference);
    }
}
