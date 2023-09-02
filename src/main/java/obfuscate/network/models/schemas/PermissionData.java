package obfuscate.network.models.schemas;

import obfuscate.event.custom.network.ModelEvent;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Model;
import obfuscate.permission.Permission;

@Model(name = "playerPermission")
public class PermissionData extends SyncableObject {

    @Loadable(field = "name")
    private String name;

    public String getName() {
        return name;
    }

    public Permission toPermission() {
        return Permission.getPermissionByName(name);
    }

    @Override
    public Class<? extends ModelEvent<PermissionData>> getFulfilledEvent() {
        return null;
    }
}
