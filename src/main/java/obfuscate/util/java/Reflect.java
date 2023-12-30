package obfuscate.util.java;

import obfuscate.MsdmPlugin;
import obfuscate.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reflect
{
    public static Object callStatic(Class<?> cls, String method_name)
    {
        try
        {
            Method method = cls.getDeclaredMethod(method_name);
            method.setAccessible(true);
            Object res = method.invoke(null);
            return res;
        }
        catch (Exception ex)
        {
            System.out.println("static method reflection failed (" + method_name + ")");
            ex.printStackTrace();
        }
        return null;
    }
    /* get generic argument of given field */
    public static Class getGenericArg(Field field, Integer idx) {
        try {
            return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[idx];
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Class getGenericArg(Field field) {
        return getGenericArg(field, 0);
    }

    public static boolean isPrimitive(Class<?> x) {
        return x == Integer.class || x == String.class || x == Float.class || x == Double.class;
    }

    public static List<Field> getAllFields(Class<?> t) {
    List<Field> fields = new ArrayList<>();
    Class<?> clazz = t;

    while (clazz != Object.class) {
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        clazz = clazz.getSuperclass();
    }

    return fields;
}

    public static Field getField(Class<?> cls, String key) {
        try {
            return cls.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            if (cls.getSuperclass() != null) {
                return getField(cls.getSuperclass(), key);
            }
        }
        return null;
    }

    public static Object getFieldValue(Object instance, String key) {
        try {
            Field f = getField(instance.getClass(), key);
            if (f == null) {
                Logger.warning("getFieldValue called with invalid arguments: Field does not exist");
                return null;
            }
            f.setAccessible(true);
            return f.get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
