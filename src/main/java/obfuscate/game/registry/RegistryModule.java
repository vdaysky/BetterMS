package obfuscate.game.registry;

import java.util.HashMap;
import java.util.UUID;

public class RegistryModule<T> {
    private final HashMap<UUID, T> registry = new HashMap<>();
    private String name;

    public RegistryModule(String name) {
        this.name = name;
    }

    public void addEntry(UUID uuid, T entry) {
        registry.put(uuid, entry);
    }

    public boolean hasEntry(UUID uuid) {
        return registry.containsKey(uuid);
    }

    public T getEntry(UUID uuid) {
        return registry.get(uuid);
    }

    public T removeEntry(UUID uuid) {
        return registry.remove(uuid);
    }

    public void clearRegistry() {
        registry.clear();
    }

    public void resetPlayerData(UUID uuid) {
        registry.remove(uuid);
    }

    public String getName() {
        return name;
    }
}
