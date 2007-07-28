package ptolemy.actor.gt.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapSet<K, V> {

    public void clear() {
        values.clear();
        _map.clear();
    }

    public boolean containsKey(K key) {
        return _map.containsKey(key);
    }

    public boolean containsValue(V value) {
        return values.contains(value);
    }

    public Set<V> get(K key) {
        Set<V> values = _map.get(key);
        return values == null ? null : Collections.unmodifiableSet(values);
    }

    public Set<K> keySet() {
        Set<K> keys = _map.keySet();
        return keys == null ? null : Collections.unmodifiableSet(keys);
    }

    public void put(K key, V value) {
        Set<V> values = _map.get(key);
        if (values == null) {
            values = new HashSet<V>();
            _map.put(key, values);
        }
        values.add(value);
        this.values.add(value);
    }

    public void remove(K key) {
        values.remove(_map.remove(key));
    }

    public void remove(K key, V value) {
        Set<V> values = _map.get(key);
        if (values != null) {
            values.remove(value);
            this.values.remove(value);
            if (values.isEmpty()) {
                _map.remove(key);
            }
        }
    }

    public String toString() {
        Comparator<K> keyComparator = new Comparator<K>() {
            public int compare(K key1, K key2) {
                return key1.toString().compareTo(key2.toString());
            }
        };

        // Return a deterministic string for the map.
        StringBuffer buffer = new StringBuffer("{");
        List<K> keys = new LinkedList<K>(_map.keySet());
        Collections.sort(keys, keyComparator);
        int i = 0;
        for (K key : keys) {
            for (V value : _map.get(key)) {
                if (i++ != 0) {
                    buffer.append(", ");
                }
                buffer.append(key);
                buffer.append(" = ");
                buffer.append(value);
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    public Set<V> values() {
        return Collections.unmodifiableSet(values);
    }

    private Map<K, Set<V>> _map = new HashMap<K, Set<V>>();

    private static final long serialVersionUID = -539612130819642425L;

    private Set<V> values = new HashSet<V>();

}
