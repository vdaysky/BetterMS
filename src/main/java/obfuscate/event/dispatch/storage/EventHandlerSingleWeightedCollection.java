package obfuscate.event.dispatch.storage;

import java.util.ArrayList;

public class EventHandlerSingleWeightedCollection {
    private ArrayList<Handler> handlers = new ArrayList<>();

    public void registerHandler(Handler handler) {
        handlers.add(handler);
    }

    public ArrayList<Handler> asList() {
        return handlers;
    }
}