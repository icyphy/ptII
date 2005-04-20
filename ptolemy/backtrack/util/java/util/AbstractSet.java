package ptolemy.backtrack.util.java.util;

import java.util.Iterator;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

public abstract class AbstractSet extends AbstractCollection implements Set, Rollbackable {

    protected AbstractSet() {
    }

    public boolean equals(Object o) {
        return (o == this || (o instanceof Set && ((Set)o).size() == size() && containsAll((Collection)o)));
    }

    public int hashCode() {
        Iterator itr = iterator();
        int hash = 0;
        int pos = size();
        while (--pos >= 0) 
            hash += hashCode(itr.next());
        return hash;
    }

    public boolean removeAll(Collection c) {
        int oldsize = size();
        int count = c.size();
        Iterator i;
        if (oldsize < count) {
            for (i = iterator(), count = oldsize; count > 0; count--) 
                if (c.contains(i.next()))
                    i.remove();
        } else
            for (i = c.iterator(); count > 0; count--) 
                remove(i.next());
        return oldsize != size();
    }

    public void $RESTORE(long timestamp, boolean trim) {
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord[] $RECORDS = new FieldRecord[] {
        };
}
