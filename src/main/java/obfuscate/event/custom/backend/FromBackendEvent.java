package obfuscate.event.custom.backend;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.CustomEvent;
import obfuscate.network.BackendManager;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.network.models.responses.EventResponse;
import obfuscate.util.Promise;
import obfuscate.util.java.Reflect;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Events that are initiated by the backend */
public abstract class FromBackendEvent extends CustomEvent {

    private final Object payload;

    private final Integer msgId;

    private final HashMap<String, Object> response = new HashMap<>();

    private final BackendManager backend;

    protected final HashMap<String, Object> json;

    public FromBackendEvent(Object jsonEvt) {
        json = (HashMap<String, Object>) jsonEvt;

        this.payload = json.get("data");
        this.msgId = ((Long)json.get("msg_id")).intValue();

        // load payload into class fields of inherited class
        if (this.payload != null) {
            this.load(this.payload);
        }

        // reference backend so it will receive events
        backend = MsdmPlugin.getBackend();
    }

    public BackendManager getBackend() {
        return backend;
    }

    public HashMap<String, Object> getPayload() {
        return (HashMap<String, Object>) payload;
    }

    public Integer getMsgId() {
        return msgId;
    }

    public Promise<Void> waitLoaded() {

        List<Promise<?>> waitFor = new ArrayList<>();

        for (Field field : Reflect.getAllFields(this.getClass())) {
            field.setAccessible(true);

            if (SyncableObject.class.isAssignableFrom(field.getType())) {
                try {
                    var fieldObject = (SyncableObject) field.get(this);

                    if (fieldObject != null) {
                        Promise<? extends SyncableObject> fieldPromise = (fieldObject).getInitializationPromise();
                        waitFor.add(fieldPromise);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return Promise.gather(waitFor);
    }

    /** Trigger event received from backend to be handled in minecraft.
     * Returned value will be always null, because this event is not tracked */
    @Override
    public Promise<? extends @Nullable EventResponse> trigger() {

//        MsdmPlugin.highlight("Trigger Backend Event: " + this.getMsgId() + " " + this);
        // backend events will be triggered only once all ids are resolved into models
        return this.waitLoaded().thenSync((x) -> {

            // now we wait for all async handlers to finish to send confirmation
            super.trigger().thenSync(y -> {
//                MsdmPlugin.logger().info("Backend event was processed. Send confirmation to the backend: " + this.getMsgId());
                MsdmPlugin.getBackend().confirmBackendEvent(this, this.response);
                return y;
            });

            // websocket events require confirmation
            // so backend knows when event was handled
            return x;
        }).thenSync(x -> null);
    }
}
