package obfuscate.event.dispatch.storage;

import obfuscate.MsdmPlugin;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.CustomEvent;

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
            MsdmPlugin.logger().log(Level.SEVERE, "trying to trigger method " + method.getName() + " on instance of " + instance.getClass() + " : Method not found on instance");
            throw new RuntimeException("Method not found on instance");
        }

        try {
            this.method.setAccessible(true);
            return this.method.invoke(instance, e);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            MsdmPlugin.info("============[EVENT ERROR]============");
            MsdmPlugin.logger().severe("Error calling method: " + this.method.getName() + " on " + instance + " with event " + e);
            MsdmPlugin.info("Below is the list of models Loaded:");
            for (var model : MsdmPlugin.getBackend().getCachedModels()) {
                MsdmPlugin.info("- " + model);
            }
            MsdmPlugin.info("================= This message is probably useless, only mentioning reflection error ====================");
            ex.printStackTrace();
            MsdmPlugin.info("============[EXCEPTION CAUSE, COULD BE USEFUL]============");
            if (ex.getCause() != null) {
                ex.getCause().printStackTrace();
            }
            MsdmPlugin.info("============[       EXCEPTION CAUSE END       ]============");
        }
        return null;
    }
}
