package obfuscate.event.dispatch;

import obfuscate.event.CustomListener;
import obfuscate.event.LocalEvent;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.CustomEvent;
import obfuscate.event.custom.PluginHolder;
import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.event.dispatch.storage.EventSpecificHandlerRegistry;
import obfuscate.event.dispatch.storage.Handler;
import obfuscate.event.dispatch.storage.HandlerClassRegistry;
import obfuscate.event.dispatch.storage.HandlerTypedRegistry;
import obfuscate.util.Promise;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/** This class dispatches events according to objects it affects.
 * it uses objects that are accessible through getters of event class. */
public class EventDispatcher
{

    private static HashMap<String, Class<? extends FromBackendEvent>> backendEventClasses = new HashMap<>();
    private static HandlerClassRegistry handlerRegistry = new HandlerClassRegistry();
    public static HandlerTypedRegistry getHandlersFromCache(Class<?> methodSource,
                                                            Class<?extends CustomEvent> event)
    {
        EventSpecificHandlerRegistry registry = handlerRegistry.getHandlersFor(methodSource);

        if (registry.getForEvent(event) == null) {
            HandlerTypedRegistry reg = getHandlersForEvent(methodSource, event);
            registry.setForEvent(event, reg);
        }
        return registry.getForEvent(event);
    }

    private static HandlerTypedRegistry getHandlersForEvent(Class<?> listenerClass,
                                                                    Class<?extends CustomEvent> eventClass)
    {
        HandlerTypedRegistry registry = new HandlerTypedRegistry();
        for (Method handler : getMethods(listenerClass, null))
        {
            if (handler.isAnnotationPresent(LocalEvent.class))
            {
                LocalEvent annotation = handler.getAnnotation(LocalEvent.class);
                LocalPriority priority = annotation.priority();
                boolean cascade = annotation.cascade();
                boolean _native = annotation._native();

                Class<? extends CustomEvent> handledEvent = (Class<? extends CustomEvent>) handler.getParameterTypes()[0];

                if ( handledEvent == eventClass || ( handledEvent.isAssignableFrom(eventClass) && cascade ) ) {
                    registry.registerTypedHandler(new Handler(handler, priority, cascade, _native));
                }
            }
        }
        return registry;
    }



    private static boolean isExtending(Class<?> classToGoThrough, Class<?> target)
    {
        return target.isAssignableFrom(classToGoThrough);
    }

    private static ArrayList<Method> getAffectedListenerGetters(Class<?extends CustomEvent> eventClass)
    {
        ArrayList<Method> affectedClassGetters = new ArrayList<>();

        for (Method eventFieldGetter : getMethods(eventClass, null))
        {
            boolean isListener = CustomListener.class.isAssignableFrom(eventFieldGetter.getReturnType());

            if (!isListener)
                continue;

            affectedClassGetters.add(eventFieldGetter);
        }
        return affectedClassGetters;
    }

    private static Promise<?> dispatchInOrderToAllConsumers(CustomEvent event, Object instance, HandlerTypedRegistry handlerRegistry) {
        // NOTE: permissions only work for fully sync handlers
        // for async methods, I am not waiting for one future to complete to start next handler.
        // handlers will be called in correct order, but after that I do not guarantee anything.

        List<Promise<?>> promises = new ArrayList<>();

        for (LocalPriority priority : LocalPriority.values())
        {
            // Execute native methods after custom handlers to make sure event cancelling is possible
            Promise<?> ps = executeEvent(handlerRegistry.getSimpleMethods(priority), event, instance)
                    .thenAsync(
                            (x) -> executeEvent(handlerRegistry.getNativeHandlers(priority), event, instance)
                    );
            promises.add(ps);
        }

        return Promise.gather(promises);
    }

    /**
     * Dispatch event internally to subscribed handlers
     * */
    public static Promise<?> dispatch(CustomEvent event)
    {
        List<Promise<?>> promises = new ArrayList<>();

        for (Method listenerGetter : getAffectedListenerGetters(event.getClass()))
        {
            // executes event in actual instance and all plugins at the same time.
            // order can get messed up
            try
            {
                listenerGetter.setAccessible(true);

                // object that was updated in this event
                CustomListener affectedListener = (CustomListener) listenerGetter.invoke(event);

                if (affectedListener == null)
                    continue;

                HandlerTypedRegistry typedHandlers = getHandlersFromCache(affectedListener.getClass(), event.getClass());

                if (PluginHolder.class.isAssignableFrom(affectedListener.getClass())) {
                    PluginHolder pluginHolder = (PluginHolder) affectedListener;

                    for (Object plugin : pluginHolder.getPlugins()) {
                        HandlerTypedRegistry plugins = getHandlersFromCache(plugin.getClass(), event.getClass());
                        promises.add(dispatchInOrderToAllConsumers(event, plugin, plugins));
                    }
                }

                promises.add(dispatchInOrderToAllConsumers(event, affectedListener, typedHandlers));
            }
            catch (IllegalAccessException | InvocationTargetException ex)
            {
                ex.printStackTrace();
            }
        }

        return Promise.gather(promises);
    }

    private static Promise<?> executeEvent(Iterable<Handler> handlers, CustomEvent event, Object instance) {

        List<Promise<?>> ps = new ArrayList<>();

        for (Handler handler : handlers) {
            Object res = handler.call(event, instance);

            if (res instanceof Promise<?>) {
                ps.add((Promise<?>) res);
            }
        }

        return Promise.gather(ps);
    }

    private static ArrayList<Method>getMethods(Class<?> cls, ArrayList<Method> found)
    {
        if (found == null)
        {
            found = new ArrayList<>();
        }
        found.addAll(Arrays.asList(cls.getDeclaredMethods()));
        if (cls.getSuperclass() != null){
            getMethods(cls.getSuperclass(), found);
        }
        return found;
    }
}
