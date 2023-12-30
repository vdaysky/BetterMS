package obfuscate.network;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.logging.Logger;
import obfuscate.network.annotations.Backend;
import obfuscate.util.time.Task;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class BackendEventManager {

    private static HashMap<String, Class<? extends FromBackendEvent>> eventClasses = new HashMap<>();
    private static boolean eventClassesLoaded = false;

    public static void collectEventClasses() {
        if (!eventClassesLoaded) {
            Logger.info("Parse all backend event classes");
            for (Class<?> cls : new Reflections("obfuscate").getTypesAnnotatedWith(Backend.class)) {
                eventClasses.put(cls.getSimpleName(), (Class<? extends FromBackendEvent>) cls);
                Logger.info("add class: " + cls);
            }
            Logger.info("All event classes parsed");
            eventClassesLoaded = true;
        }
    }

    public static void parseAndTrigger(HashMap<String, Object> json) {
    /*
        TODO: use loadable object like a normal person,
         return promise to make it possible to have foreign keys in backend events
    */
        collectEventClasses();

        try {
            String eventType = (String) json.get("type");
            Class<? extends FromBackendEvent> cls = eventClasses.get(eventType);

            if (cls == null) {
                throw new RuntimeException("BackendEvent Class not found for type '" + json.get("type") + "'");
            }

            FromBackendEvent event = cls.getDeclaredConstructor(Object.class).newInstance(json);

            event.waitLoaded().thenSync(
                (e) -> {
                    //MsdmPlugin.logger().info("[WS] Parsed event: " + event);
                    // trigger from a task to enter main thread safely
                    new Task(event::trigger, 0).run();
                    return e;
                }
            );

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
