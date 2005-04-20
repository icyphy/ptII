package ptolemy.backtrack.util.java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.SortedMap;

public class TreeSet extends AbstractSet implements SortedSet, Cloneable, Serializable, Rollbackable {

    private static final long serialVersionUID = -2479143000061671589L;

    private transient SortedMap map;

    public TreeSet() {
        $ASSIGN$map(new TreeMap());
    }

    public TreeSet(Comparator comparator) {
        $ASSIGN$map(new TreeMap(comparator));
    }

    public TreeSet(Collection collection) {
        $ASSIGN$map(new TreeMap());
        addAll(collection);
    }

    public TreeSet(SortedSet sortedSet) {
        $ASSIGN$map(new TreeMap(sortedSet.comparator()));
        Iterator itr = sortedSet.iterator();
        ((TreeMap)map).putKeysLinear(itr, sortedSet.size());
    }

    private TreeSet(SortedMap backingMap) {
        $ASSIGN$map(backingMap);
    }

    public boolean add(Object obj) {
        return map.put(obj, "") == null;
    }

    public boolean addAll(Collection c) {
        boolean result = false;
        int pos = c.size();
        Iterator itr = c.iterator();
        while (--pos >= 0) 
            result |= (map.put(itr.next(), "") == null);
        return result;
    }

    public void clear() {
        map.clear();
    }

    public Object clone() {
        TreeSet copy = null;
        try {
            copy = (TreeSet)super.clone();
            copy.$ASSIGN$map((SortedMap)((AbstractMap)map).clone());
        } catch (CloneNotSupportedException x) {
        }
        return copy;
    }

    public Comparator comparator() {
        return map.comparator();
    }

    public boolean contains(Object obj) {
        return map.containsKey(obj);
    }

    public Object first() {
        return map.firstKey();
    }

    public SortedSet headSet(Object to) {
        return new TreeSet(map.headMap(to));
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Iterator iterator() {
        return map.keySet().iterator();
    }

    public Object last() {
        return map.lastKey();
    }

    public boolean remove(Object obj) {
        return map.remove(obj) != null;
    }

    public int size() {
        return map.size();
    }

    public SortedSet subSet(Object from, Object to) {
        return new TreeSet(map.subMap(from, to));
    }

    public SortedSet tailSet(Object from) {
        return new TreeSet(map.tailMap(from));
    }

    private void writeObject(ObjectOutputStream s) throws IOException  {
        s.defaultWriteObject();
        Iterator itr = map.keySet().iterator();
        int pos = map.size();
        s.writeObject(map.comparator());
        s.writeInt(pos);
        while (--pos >= 0) 
            s.writeObject(itr.next());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
        s.defaultReadObject();
        Comparator comparator = (Comparator)s.readObject();
        int size = s.readInt();
        $ASSIGN$map(new TreeMap(comparator));
        ((TreeMap)map).putFromObjStream(s, size, false);
    }

    private final SortedMap $ASSIGN$map(SortedMap newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$map.add(null, map, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return map = newValue;
    }

    public void $RESTORE(long timestamp, boolean trim) {
        map = (SortedMap)$RECORD$map.restore(map, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$map = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$map
        };
}
