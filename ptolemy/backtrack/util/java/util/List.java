package ptolemy.backtrack.util.java.util;

import java.lang.Object;
import java.util.Iterator;
import java.util.ListIterator;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;

public interface List extends Collection, Rollbackable {

    void add(int index, Object o);

    boolean add(Object o);

    boolean addAll(int index, Collection c);

    boolean addAll(Collection c);

    void clear();

    boolean contains(Object o);

    boolean containsAll(Collection c);

    boolean equals(Object o);

    Object get(int index);

    int hashCode();

    int indexOf(Object o);

    boolean isEmpty();

    Iterator iterator();

    int lastIndexOf(Object o);

    ListIterator listIterator();

    ListIterator listIterator(int index);

    Object remove(int index);

    boolean remove(Object o);

    boolean removeAll(Collection c);

    boolean retainAll(Collection c);

    Object set(int index, Object o);

    int size();

    List subList(int fromIndex, int toIndex);

    Object[] toArray();

    Object[] toArray(Object[] a);

    public void $RESTORE(long timestamp, boolean trim);

    public Checkpoint $GET$CHECKPOINT();

    public Object $SET$CHECKPOINT(Checkpoint checkpoint);
}
