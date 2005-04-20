package ptolemy.backtrack.util.java.util;

import java.io.Serializable;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

public class LinkedHashSet extends HashSet implements Set, Cloneable, Serializable, Rollbackable {

    private static final long serialVersionUID = -2851667679971038690L;

    public LinkedHashSet() {
        super();
    }

    public LinkedHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public LinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public LinkedHashSet(Collection c) {
        super(c);
    }

    HashMap init(int capacity, float load) {
        return new LinkedHashMap(capacity, load);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord[] $RECORDS = new FieldRecord[] {
        };
}
