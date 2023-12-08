package obfuscate.event.custom;

import obfuscate.game.core.plugins.Plugin;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PluginManager {
    private static final HashMap<String, Class<?>> pluginClasses = new HashMap<>();

    public static void collectPluginClasses() {
        for (Class<?> cls : new Reflections("obfuscate.game.core.plugins").getTypesAnnotatedWith(Plugin.class)) {
            pluginClasses.put(cls.getSimpleName(), cls);
        }
    }

    public static Class<?> getPluginClass(String name) {
        return pluginClasses.get(name);
    }

    public static List<String> getPluginNames() {
        return new ArrayList<>(pluginClasses.keySet());
    }
}
