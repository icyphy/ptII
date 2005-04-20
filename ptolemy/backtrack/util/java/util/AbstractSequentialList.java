package ptolemy.backtrack.util.java.util;

import java.util.Iterator;
import java.util.ListIterator;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

public abstract class AbstractSequentialList extends AbstractList implements Rollbackable {

    protected AbstractSequentialList() {
    }

    public abstract ListIterator listIterator(int index);

    public void add(int index, Object o) {
        listIterator(index).add(o);
    }

    public boolean addAll(int index, Collection c) {
        Iterator ci = c.iterator();
        int size = c.size();
        ListIterator i = listIterator(index);
        for (int pos = size; pos > 0; pos--) 
            i.add(ci.next());
        return size > 0;
    }

    public Object get(int index) {
        if (index == size())
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size());
        return listIterator(index).next();
    }

    public Iterator iterator() {
        return listIterator();
    }

    public Object remove(int index) {
        if (index == size())
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size());
        ListIterator i = listIterator(index);
        Object removed = i.next();
        i.remove();
        return removed;
    }

    public Object set(int index, Object o) {
        if (index == size())
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size());
        ListIterator i = listIterator(index);
        Object old = i.next();
        i.set(o);
        return old;
    }

    public void $RESTORE(long timestamp, boolean trim) {
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord[] $RECORDS = new FieldRecord[] {
        };
}
