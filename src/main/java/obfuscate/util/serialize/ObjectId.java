package obfuscate.util.serialize;

import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Struct;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/** Object id is a unique identifier used to globally identify an object and manage its dependencies.
 * Cam be constructed with constructor or with load method. */
public class ObjectId extends Struct {

    @Loadable(field = "obj_id")
    private Integer objId;

    @Loadable(field = "entity")
    private String entity;

    @Loadable(field = "modifiers")
    private String modifiers;

    @Loadable(field = "dependencies", explicit = true)
    private List<ObjectId> dependencies = new ArrayList<>();

    public ObjectId() {}

    public ObjectId(Integer objId, String entity, List<ObjectId> dependencies) {
        this.objId = objId;
        this.entity = entity;

        this.modifiers = "[]";

        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }

        this.dependencies = dependencies;
    }

    public String toString() {
        String deps;
        if (dependencies != null) {
            deps = dependencies.stream().map(ObjectId::toString).collect(Collectors.joining(", "));
        } else {
            deps = "null";
        }

        return "<ObjectId {" + hashCode() + "} id=" + objId +
                " entity=" + entity +
                " deps=[" + deps + "]>";
    }

    public Integer getObjId() {
        return objId;
    }

    public String getEntity() {
        return entity;
    }

    public List<ObjectId> getDependencies() {
        return dependencies;
    }

    public String toGraphQlArg() {
        var modifiersString = new JSONArray().toJSONString();

        StringBuilder dependenciesString = new StringBuilder("[");
        for (var dependency : dependencies) {
            dependenciesString.append(dependency.toGraphQlArg()).append(", ");
        }
        if (dependenciesString.length() > 1) {
            dependenciesString.delete(dependenciesString.length() - 2, dependenciesString.length());
        }
        dependenciesString.append("]");

        return "{obj_id: " + objId +
                ", entity: \"" + entity +
                "\", modifiers: \"" + modifiersString +
                "\", dependencies: " + dependenciesString + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ObjectId otherId) {
            return entity.equalsIgnoreCase(otherId.entity) && objId.equals(otherId.objId);
        }
        return false;
    }

    public int hashCode() {
        // TODO: this hash function is a fucking disgrace
        int dependenciesHash = 0;

//        if (dependencies != null) {
//            for (var dependency : dependencies) {
//                dependenciesHash += dependency.hashCode();
//            }
//        }

        return (objId == null ? Integer.MAX_VALUE : objId.hashCode()) + (entity.toLowerCase().hashCode() * 1000) + dependenciesHash;
    }

    public Object toMap() {
        var map = new HashMap<>();
        map.put("obj_id", objId);
        map.put("entity", entity);
        map.put("modifiers", modifiers);
        map.put("dependecies", dependencies.stream().map(ObjectId::toMap).collect(Collectors.toList()));
        return map;
    }
}
