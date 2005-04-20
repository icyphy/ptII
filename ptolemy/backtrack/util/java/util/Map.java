package ptolemy.backtrack.util.java.util;

import java.lang.Object;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;

public interface Map extends Rollbackable {

    static interface Entry extends Rollbackable {

        Object getKey();

        Object getValue();

        Object setValue(Object value);

        int hashCode();

        boolean equals(Object o);

        public void $RESTORE(long timestamp, boolean trim);

        public Checkpoint $GET$CHECKPOINT();

        public Object $SET$CHECKPOINT(Checkpoint checkpoint);
    }

    void clear();

    boolean containsKey(Object key);

    boolean containsValue(Object value);

    Set entrySet();

    boolean equals(Object o);

    Object get(Object key);

    Object put(Object key, Object value);

    int hashCode();

    boolean isEmpty();

    Set keySet();

    void putAll(Map m);

    Object remove(Object o);

    int size();

    Collection values();

    public void $RESTORE(long timestamp, boolean trim);

    public Checkpoint $GET$CHECKPOINT();

    public Object $SET$CHECKPOINT(Checkpoint checkpoint);
}
