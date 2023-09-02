package obfuscate.event.custom;

import obfuscate.MsdmPlugin;
import obfuscate.event.Tracked;
import obfuscate.event.dispatch.EventDispatcher;
import obfuscate.game.Server;
import obfuscate.util.serialize.dump.ClassSerializer;
import obfuscate.util.serialize.load.Struct;
import obfuscate.network.models.responses.EventResponse;
import obfuscate.util.Promise;

import java.util.HashMap;

public class CustomEvent extends Struct implements ToBackendEvent
{

    public Promise<? extends EventResponse> trigger()
    {
        boolean isTracked = (this.getClass().getAnnotation(Tracked.class) != null) || this instanceof TrackedEvent;

        Promise<?> handlersDone = EventDispatcher.dispatch(this);

        // some events are tracked,
        // meaning we want to forward them to the backend.
        // tracked events will return meaningful response from backend
        if (isTracked) {
            MsdmPlugin.highlight("Forward tracked event " + this);
            return handlersDone.thenAsync((x) -> {
                MsdmPlugin.important("Tracked event " + this + " handled locally, forwarding to backend");
                return MsdmPlugin.getBackend().sendEvent(this);
            });
        }

        // destroy meaningless promise result from all handlers
        return handlersDone.thenSync(response -> null);
    }

    /** Add this to make sure events are propagated to server */
    public Server getServer() {
        return MsdmPlugin.getGameServer();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public HashMap<String, Object> getPayload() {
        return ClassSerializer.serializeClass(this);
    }
}
