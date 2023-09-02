package obfuscate.network.event;

import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.network.annotations.Backend;
import obfuscate.util.serialize.ObjectId;
import obfuscate.util.serialize.load.Loadable;

@Backend
public class ModelUpdateEvent extends FromBackendEvent {

    @Loadable(field = "update_type")
    private String type;
    @Loadable(field = "identifier", explicit = true)
    private ObjectId objectId;

    public ModelUpdateEvent(Object jsonEvt) {
        super(jsonEvt);
    }

    public ObjectId getUpdatedObjectId() {
        return objectId;
    }

    public String getType() {
        return type;
    }

    public ObjectId getObjectId() {
        return objectId;
    }
}
