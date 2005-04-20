package ptolemy.backtrack.util.java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.HashMap;

public class HashSet extends AbstractSet implements Set, Cloneable, Serializable, Rollbackable {

    private static final long serialVersionUID = -5024744406713321676L;

    private transient HashMap map;

    public HashSet() {
        this(HashMap.DEFAULT_CAPACITY, HashMap.DEFAULT_LOAD_FACTOR);
    }

    public HashSet(int initialCapacity) {
        this(initialCapacity, HashMap.DEFAULT_LOAD_FACTOR);
    }

    public HashSet(int initialCapacity, float loadFactor) {
        $ASSIGN$map(init(initialCapacity, loadFactor));
    }

    public HashSet(Collection c) {
        this(Math.max(2 * c.size(), HashMap.DEFAULT_CAPACITY));
        addAll(c);
    }

    public boolean add(Object o) {
        return map.put(o, "") == null;
    }

    public void clear() {
        map.clear();
    }

    public Object clone() {
        HashSet copy = null;
        try {
            copy = (HashSet)super.clone();
        } catch (CloneNotSupportedException x) {
        }
        copy.$ASSIGN$map((HashMap)map.clone());
        return copy;
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public boolean isEmpty() {
        return map.getSize() == 0;
    }

    public Iterator iterator() {
        return map.iterator(HashMap.KEYS);
    }

    public boolean remove(Object o) {
        return (map.remove(o) != null);
    }

    public int size() {
        return map.getSize();
    }

    HashMap init(int capacity, float load) {
        return new HashMap(capacity, load);
    }

    private void writeObject(ObjectOutputStream s) throws IOException  {
        s.defaultWriteObject();
        Iterator it = map.iterator(HashMap.KEYS);
        s.writeInt(map.getBuckets().length);
        s.writeFloat(map.loadFactor);
        s.writeInt(map.getSize());
        while (it.hasNext()) 
            s.writeObject(it.next());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
        s.defaultReadObject();
        $ASSIGN$map(init(s.readInt(), s.readFloat()));
        for (int size = s.readInt(); size > 0; size--) 
            map.put(s.readObject(), "");
    }

    private final HashMap $ASSIGN$map(HashMap newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$map.add(null, map, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return map = newValue;
    }

    public void $RESTORE(long timestamp, boolean trim) {
        map = (HashMap)$RECORD$map.restore(map, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$map = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$map
        };
}
