package obfuscate.event.custom;

import obfuscate.network.models.responses.IntentResponse;
import obfuscate.util.Promise;

/** Alternative way to mark event as tracked. It is a shortcut to get IntentResponse directly without casting it. */
public class TrackedEvent extends CancellableEvent {

    public Promise<? extends IntentResponse> trigger() {
        return (Promise<? extends IntentResponse>) super.trigger();
    }
}
