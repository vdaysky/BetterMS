package obfuscate.util.serialize;

import obfuscate.util.java.Reflect;

import java.lang.reflect.Field;

public class ListMeta extends FieldMeta {

    public ListMeta(Field field, String apiField) {
        super(field, apiField);
    }

    private String key = null;

    private Class<?> model = null;


    public ListMeta(Field field, String apiField, String key, Class<?> model) {
        super(field, apiField);
        this.key = key;
        this.model = model;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getModel() {
        if (model == null) {
            model = Reflect.getGenericArg(getField());
        }
        return model;
    }
}
