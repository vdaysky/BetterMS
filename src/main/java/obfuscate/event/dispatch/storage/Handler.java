package obfuscate.event.dispatch.storage;

import obfuscate.MsdmPlugin;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.CustomEvent;
import obfuscate.logging.Logger;
import obfuscate.logging.Tag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class Handler {
    public Method method;
    public LocalPriority priority;
    public boolean _native;
    private boolean cascade;

    public Handler(Method method, LocalPriority priority, boolean cascade, boolean _native) {
        this.method = method;
        this.priority = priority;
        this._native = _native;
        this.cascade = cascade;

    }

    public Object call(CustomEvent e, Object instance) {

        if (!method.getDeclaringClass().isAssignableFrom(instance.getClass())) {
            Logger.severe("trying to trigger method " + method.getName() + " on instance of " + instance.getClass() + " : Method not found on instance", instance, Tag.EVENTS);
            throw new RuntimeException("Method not found on instance");
        }

        try {
            this.method.setAccessible(true);
            return this.method.invoke(instance, e);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            Logger.severe("Error calling method: " + this.method.getName() + " on " + instance + " with event " + e, e, ex, Tag.EVENTS);
            Logger.info("Below is the list of models Loaded:");

            for (var model : MsdmPlugin.getBackend().getCachedModels()) {
                Logger.info("- " + model, model);
            }

            if (ex.getCause() != null) {
                Logger.info("Exception Cause", ex.getCause());
            }
        }
        return null;
    }
}
