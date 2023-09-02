package obfuscate.util.java;


import java.util.HashMap;
import java.util.function.Function;

public class DefaultMap<K, V> extends HashMap<K, V> {

    private Function<Void, V> factory = null;

    public DefaultMap(){}

    public DefaultMap(Function<Void, V> runnable) {
        factory = runnable;
    }

    @Override
    public V get(Object key) {
        if (factory != null && !containsKey(key)) {
            put((K) key, factory.apply(null));
        }
        return super.get(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V value = get(key);
        if (value == null) {
            put((K) key, defaultValue);
        }
        return get(key);
    }
}
