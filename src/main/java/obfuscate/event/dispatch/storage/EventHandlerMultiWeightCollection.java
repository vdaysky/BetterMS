package obfuscate.event.dispatch.storage;

import obfuscate.event.LocalPriority;
import obfuscate.util.java.DefaultMap;

import java.util.ArrayList;

/** Stores list of handlers that are sorted by priority.
 * @see EventHandlerSingleWeightedCollection EventHandlerSingleWeightedCollection
 *
 * */
public class EventHandlerMultiWeightCollection {
    private DefaultMap<LocalPriority, EventHandlerSingleWeightedCollection> weightedHandlers = new DefaultMap<>();

    /** @param handler handler to store in
     **/
    public void registerHandler(Handler handler) {
        weightedHandlers.getOrDefault(handler.priority, new EventHandlerSingleWeightedCollection()).registerHandler(handler);
    }

    public ArrayList<Handler> getWithPrior(LocalPriority prior) {

        EventHandlerSingleWeightedCollection s = weightedHandlers.getOrDefault(prior, new EventHandlerSingleWeightedCollection());
        return s.asList();
    }
}
