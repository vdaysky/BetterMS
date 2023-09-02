package obfuscate.event.dispatch.storage;

import obfuscate.event.LocalEvent;
import obfuscate.event.custom.CustomEvent;
import obfuscate.util.java.DefaultMap;

import java.lang.reflect.Method;

/** this class links Event handler collection to listener class
 * @see EventSpecificHandlerRegistry EventSpecificHandlerRegistry
 * */
public class HandlerClassRegistry {

    private DefaultMap< Class<?>, EventSpecificHandlerRegistry> registeredHandlers = new DefaultMap<>();

    public void registerHandlerClassMethod(Class<?> handlerClass, Method method)
    {
        LocalEvent annotation = method.getAnnotation(LocalEvent.class);

        if (!method.getParameterTypes()[0].isAssignableFrom(CustomEvent.class))
            return;

        Class<? extends CustomEvent> handledEvent = (Class<? extends CustomEvent>) method.getParameterTypes()[0];

        registeredHandlers.getOrDefault(handlerClass, new EventSpecificHandlerRegistry()).registerEventSpecific(
            handledEvent,
            new Handler(
                method,
                annotation.priority(),
                annotation.cascade(),
                annotation._native()
            )
        );
    }

    public EventSpecificHandlerRegistry getHandlersFor(Class<?> eventClass) {
        EventSpecificHandlerRegistry existingHandlers = registeredHandlers.get(eventClass);
        if (existingHandlers == null) {
            registeredHandlers.put(eventClass, new EventSpecificHandlerRegistry());
        }
        return registeredHandlers.get(eventClass);
    }

}
