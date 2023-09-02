package obfuscate.event.custom;

import obfuscate.network.models.responses.EventResponse;
import obfuscate.util.Promise;
import org.bukkit.event.Cancellable;

public class CancellableEvent extends CustomEvent implements Cancellable
{
    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public Promise<? extends EventResponse> trigger() {
        return super.trigger();
    }

    public boolean triggerSync() {
        super.trigger();
        return isCancelled();
    }
}
