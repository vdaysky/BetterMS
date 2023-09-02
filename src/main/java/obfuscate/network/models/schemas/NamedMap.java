package obfuscate.network.models.schemas;

import obfuscate.event.custom.network.ModelEvent;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Model;

@Model(name = "map")
public class NamedMap extends SyncableObject {

    @Loadable(field = "name")
    private String name;

    @Override
    public Class<? extends ModelEvent<? extends SyncableObject>> getFulfilledEvent() {
        return null;
    }

    public String getName() {
        return name;
    }
}
