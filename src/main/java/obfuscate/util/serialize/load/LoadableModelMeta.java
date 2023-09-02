package obfuscate.util.serialize.load;

import obfuscate.util.serialize.FieldMeta;
import obfuscate.util.serialize.ListMeta;

import java.util.ArrayList;

/** Metadata of model, contains information about model fields */
public class LoadableModelMeta {


    private final ArrayList<FieldMeta> primitiveFields = new ArrayList<>();
    private final ArrayList<FieldMeta> objectFields = new ArrayList<>();

    private final ArrayList<ListMeta> listFields = new ArrayList<>();


    public void addPrimitiveField(FieldMeta field) {
        primitiveFields.add(field);
    }
    public void addObjectField(FieldMeta field) {
        objectFields.add(field);
    }

    public void addListField(ListMeta field) {
        listFields.add(field);
    }

    public ArrayList<FieldMeta> getPrimitiveFields() {
        return primitiveFields;
    }

    public ArrayList<FieldMeta> getObjectFields() {
        return objectFields;
    }

    public ArrayList<ListMeta> getListFields() {
        return listFields;
    }
}
