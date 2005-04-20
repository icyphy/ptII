package ptolemy.backtrack.util.java.util;

import java.lang.Object;
import java.util.Iterator;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;

public interface Set extends Collection, Rollbackable {

    boolean add(Object o);

    boolean addAll(Collection c);

    void clear();

    boolean contains(Object o);

    boolean containsAll(Collection c);

    boolean equals(Object o);

    int hashCode();

    boolean isEmpty();

    Iterator iterator();

    boolean remove(Object o);

    boolean removeAll(Collection c);

    boolean retainAll(Collection c);

    int size();

    Object[] toArray();

    Object[] toArray(Object[] a);

    public void $RESTORE(long timestamp, boolean trim);

    public Checkpoint $GET$CHECKPOINT();

    public Object $SET$CHECKPOINT(Checkpoint checkpoint);
}
