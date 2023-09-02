package obfuscate.util.java;

public class Pair<K, V> {
    K key;
    V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K key(){
        return key;
    }

    public V value() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public K getFirst() {
        return key();
    }

    public V getSecond() {
        return value();
    }

    public void setFirst(K key) {
        setKey(key);
    }

    public void setSecond(V value) {
        setValue(value);
    }
}
