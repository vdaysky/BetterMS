package obfuscate.event.custom.network;

import obfuscate.event.custom.CustomEvent;
import obfuscate.util.serialize.load.SyncableObject;

public class ModelEvent<T extends SyncableObject> extends CustomEvent {

    private final T model;

    public ModelEvent(T model) {
        this.model = model;
    }

    public T getModel() {
        return model;
    }

}
