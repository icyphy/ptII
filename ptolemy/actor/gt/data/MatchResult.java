package ptolemy.actor.gt.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class MatchResult extends HashMap<Object, Object> {

    public void clear() {
        super.clear();
        _keys.clear();
        _values.clear();
    }

    public boolean containsValue(Object value) {
        return _values.contains(value);
    }

    public Object put(Object key, Object value) {
        if (!super.containsKey(key)) {
            _keys.add(key);
        }
        Object oldValue = super.put(key, value);
        _values.add(value);
        return oldValue;
    }

    public Object remove(Object key) {
        Object oldValue = super.remove(key);
        if (oldValue != null) {
            _keys.remove(key);
            _values.remove(oldValue);
        }
        return oldValue;
    }

    public void retain(int count) {
        int size = _keys.size();
        if (size > count) {
            ListIterator<Object> iterator = _keys.listIterator(size);
            for (; size > count; size--) {
                Object key = iterator.previous();
                iterator.remove();
                _values.remove(super.remove(key));
            }
        }
    }

    public String toString() {
        Comparator<Object> keyComparator = new Comparator<Object>() {
            public int compare(Object key1, Object key2) {
                return key1.toString().compareTo(key2.toString());
            }
        };

        // Return a deterministic string for the map.
        StringBuffer buffer = new StringBuffer("{");
        List<Object> keys = new LinkedList<Object>(keySet());
        Collections.sort(keys, keyComparator);
        int i = 0;
        for (Object key : keys) {
            if (i++ != 0) {
                buffer.append(", ");
            }
            buffer.append(key);
            buffer.append(" = ");
            buffer.append(get(key));
        }
        buffer.append("}");
        return buffer.toString();
    }

    public Set<Object> values() {
        return Collections.unmodifiableSet(_values);
    }

    private List<Object> _keys = new LinkedList<Object>();

    private Set<Object> _values = new HashSet<Object>();

    private static final long serialVersionUID = -539612130819642425L;

}
