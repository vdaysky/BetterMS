package obfuscate.event.dispatch.storage;

import obfuscate.event.LocalPriority;

import java.util.ArrayList;

public class HandlerTypedRegistry {
    private EventHandlerMultiWeightCollection nativeHandlers = new EventHandlerMultiWeightCollection();
    private EventHandlerMultiWeightCollection simpleMethods = new EventHandlerMultiWeightCollection();

    public void registerTypedHandler(Handler handler)
    {
        if (handler._native) {
            nativeHandlers.registerHandler(handler);
            return;
        }
        simpleMethods.registerHandler(handler);
    }

    public ArrayList<Handler> getNativeHandlers(LocalPriority prior) {
        return nativeHandlers.getWithPrior(prior);
    }

    public ArrayList<Handler> getSimpleMethods(LocalPriority prior) {
        return simpleMethods.getWithPrior(prior);
    }

    public HandlerTypedRegistry extend(HandlerTypedRegistry handlers) {
        for (LocalPriority prior : LocalPriority.values()) {
            for (Handler handler : handlers.nativeHandlers.getWithPrior(prior)) {
                nativeHandlers.getWithPrior(prior).addAll(handlers.getNativeHandlers(prior));
            }
            for (Handler handler : handlers.simpleMethods.getWithPrior(prior)) {
                simpleMethods.getWithPrior(prior).addAll(handlers.getSimpleMethods(prior));
            }
        }
        return this;
    }
}
