package obfuscate.util.serialize;

import java.lang.reflect.Field;

public class FieldMeta {

    private Field field;
    private String apiField;

    private boolean explicit;

    private boolean propagateUpdate;

    private boolean merge;

    public FieldMeta(Field field, String apiField) {
        this(field, apiField, false, true, false);
    }

    public FieldMeta(Field field, String apiField, boolean explicit, boolean propagateUpdate, boolean merge) {
        this.field = field;
        this.apiField = apiField;
        this.explicit = explicit;
        this.propagateUpdate = propagateUpdate;
        this.merge = merge;
    }

    public Field getField() {
        return field;
    }

    public String getApiField() {
        return apiField;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public boolean doesPropagateUpdate() {
        return propagateUpdate;
    }

    public boolean merge() {
        return merge;
    }
}
