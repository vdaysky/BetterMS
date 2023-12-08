package obfuscate.util.serialize.load;

import obfuscate.MsdmPlugin;
import obfuscate.util.Promise;
import obfuscate.util.java.Reflect;
import obfuscate.util.serialize.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LoadableMap<K, V extends SyncableObject> extends Struct implements Map<K, V> {

    private final HashMap<K, V> wrapped = new HashMap<>();

    private List<V> objects = new ArrayList<>();

    private String key;

    private Class<?> cls;


    public LoadableMap(String key, Class<?> cls) {
        this.key = key;
        this.cls = cls;
    }

    @Override
    public Promise<LoadableMap<K, V>> load(Object payload) {
        this.objects.clear();

        List<Promise<?>> ps = new ArrayList<>();

        for (Object x : (List<?>) payload) {

            ObjectId objId = new ObjectId();
            objId.load(x, true);

            ObjectLoadFuture<?> future = MsdmPlugin.getBackend().getOrCreateModel(
                    (Class<? extends SyncableObject>) cls,
                    objId
            );
//            MsdmPlugin.logger().info("LoadableMap : Instance " + future.getObject()  + " is ffed: " + future.getObject().waitFullyInitialized().isFulfilled());
            future.getPromise().thenSync(
                y -> {
//                    MsdmPlugin.logger().info("LoadableMap : Instance " + future.getObject()  + " fully loaded");
                    return null;
                }
            );
            ps.add(future.getPromise());
            this.objects.add((V) future.getObject());
        }

        Promise<LoadableMap<K, V>> result = new Promise<>();

        Promise.gather(ps).thenSync(
            x -> {
                wrapped.clear();
//                MsdmPlugin.logger().info("LoadableMap put data in map");
                for (V obj : objects) {
//                    MsdmPlugin.logger().info("LoadableMap put " + obj + " in map");
                    wrapped.put((K) Reflect.getFieldValue(obj, key), obj);
                }

                if (!waitFullyInitialized().isFulfilled()) {
//                    MsdmPlugin.logger().info("Fulfill promise");
                    waitFullyInitialized().fulfill(this);
                }
                result.fulfill(null);

//                MsdmPlugin.logger().info("LoadableMap is loaded " + this.wrapped);
                return null;
            }
        );

        return result;
    }

    @Override
    public V get(Object key) {
        return wrapped.get(key);
    }

    public boolean containsKey(Object key) {
        return wrapped.containsKey(key);
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return wrapped.containsValue(value);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return wrapped.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return wrapped.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        wrapped.putAll(m);
    }

    @Override
    public void clear() {
        wrapped.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return wrapped.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return wrapped.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return wrapped.entrySet();
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }
}
