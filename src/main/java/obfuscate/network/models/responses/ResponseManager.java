package obfuscate.network.models.responses;

import obfuscate.event.custom.ToBackendEvent;
import obfuscate.event.custom.IntentEvent;
import obfuscate.util.Promise;
import obfuscate.util.time.Scheduler;

import java.util.HashMap;

public class ResponseManager {

    public static Promise<? extends EventResponse> createResponse(ToBackendEvent e, HashMap<String, Object> responseObject) {
        Object payload = responseObject.get("payload");
//        MsdmPlugin.important("Create response for " + e.getClass().getSimpleName() + " with payload " + payload);
        if (payload == null) {
            return Promise.Instant(null);
        }

        boolean isIntent = e instanceof IntentEvent;

        // todo:
        //  make a map of all possible responses
        //  or instead of map attach response to request with annotation
        //  like @RespondsWith(CreateGameIntentResponse.class)
        //  if no response is bound to event, fallback to the logic below

        if (isIntent) {

            Promise<EventResponse> promise = new IntentResponse().load(payload).thenSync(x -> (EventResponse) x);
            Promise<EventResponse> syncPromise = new Promise<>();

            promise.thenSync(
                    response -> {
                        // run in scheduler to synchronize with main thread
                        Scheduler.runNextTick(
                                () -> syncPromise.fulfill(response)
                        );
                        return null;
                    }
            );
            return syncPromise;
        }
        return new EventResponse().load(payload).thenSync(x -> (EventResponse) x);
    }
}
