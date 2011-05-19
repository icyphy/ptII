package ptolemy.apps.ptalon.model;

import java.util.Hashtable;
import java.util.Map;

public class ReversableHashtable<K, V> extends Hashtable<K, V> {
    private Hashtable<V, K> reverse = new Hashtable<V, K>();

    public synchronized void clear() {
        reverse.clear();
        super.clear();
    }

    public synchronized K getKey(V value) {
        return reverse.get(value);
    }

    public synchronized V put(K key, V value) {
        if (reverse.containsKey(value)) {
            throw new IllegalArgumentException(
                    "Cannot put key, value pair, since the value is a duplicate.");
        }
        reverse.put(value, key);
        return super.put(key, value);
    }

    public synchronized void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException(
                "putAll() is not supported in this inherited class.");
    }

    public synchronized V remove(Object key) {
        V value = super.remove(key);
        reverse.remove(value);
        return value;
    }

    public boolean containsValue(Object value) {
        return reverse.containsKey(value);
    }
}
