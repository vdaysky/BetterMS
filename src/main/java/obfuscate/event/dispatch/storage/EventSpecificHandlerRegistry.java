package obfuscate.event.dispatch.storage;

import obfuscate.event.custom.CustomEvent;
import obfuscate.util.java.DefaultMap;

/** this class links together event and its handlers list.
 * @see HandlerTypedRegistry HandlerTypedRegistry
 * */
public class EventSpecificHandlerRegistry {
    DefaultMap<Class<?extends CustomEvent>, HandlerTypedRegistry> eventSpecificHandlerRegistry = new DefaultMap<>();

    public void registerEventSpecific(Class<?extends CustomEvent> eventClass, Handler handler){
        eventSpecificHandlerRegistry.getOrDefault(eventClass, new HandlerTypedRegistry()).registerTypedHandler(handler);
    }

    public void registerEventList(Class<?extends CustomEvent> eventClass, HandlerTypedRegistry handlers) {
        HandlerTypedRegistry registry = eventSpecificHandlerRegistry.getOrDefault(eventClass, new HandlerTypedRegistry() );
        registry.extend(handlers);
    }

    public HandlerTypedRegistry getForEvent(Class<? extends CustomEvent> event) {
        return eventSpecificHandlerRegistry.get(event);
    }

    public void setForEvent(Class<? extends CustomEvent> event, HandlerTypedRegistry reg) {
        eventSpecificHandlerRegistry.put(event, reg);
    }
}
