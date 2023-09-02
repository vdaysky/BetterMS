package obfuscate.game.registry;

import obfuscate.MsdmPlugin;

import java.util.HashMap;
import java.util.UUID;

public class OnlinePlayerDataRegistry {

    private HashMap<String, RegistryModule> modules = new HashMap<>();

    public <T> RegistryModule<T> getModule(String name) {
        if (!modules.containsKey(name)) {
            registerModule(name);
        }

        return modules.get(name);
    }

    public <T> void registerModule(String name) {
        if (modules.containsKey(name)) {
            throw new IllegalArgumentException("Module with name " + name + " already exists!");
        }
        modules.put(name, new RegistryModule<T>(name));
    }

    public void resetPlayerData(UUID uuid) {
        for (RegistryModule module : modules.values()) {
            MsdmPlugin.logger().info("Resetting player data for " + uuid.toString() + " in module " + module.getName());
            module.resetPlayerData(uuid);
        }
    }

}
