package ptolemy.backtrack.util.java.util;

import java.lang.Object;
import java.util.Comparator;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;

public interface SortedSet extends Set, Rollbackable {

    Comparator comparator();

    Object first();

    SortedSet headSet(Object toElement);

    Object last();

    SortedSet subSet(Object fromElement, Object toElement);

    SortedSet tailSet(Object fromElement);

    public void $RESTORE(long timestamp, boolean trim);

    public Checkpoint $GET$CHECKPOINT();

    public Object $SET$CHECKPOINT(Checkpoint checkpoint);
}
