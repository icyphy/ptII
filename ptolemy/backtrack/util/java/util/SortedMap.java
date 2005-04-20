package ptolemy.backtrack.util.java.util;

import java.lang.Object;
import java.util.Comparator;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;

public interface SortedMap extends Map, Rollbackable {

    Comparator comparator();

    Object firstKey();

    SortedMap headMap(Object toKey);

    Object lastKey();

    SortedMap subMap(Object fromKey, Object toKey);

    SortedMap tailMap(Object fromKey);

    public void $RESTORE(long timestamp, boolean trim);

    public Checkpoint $GET$CHECKPOINT();

    public Object $SET$CHECKPOINT(Checkpoint checkpoint);
}
