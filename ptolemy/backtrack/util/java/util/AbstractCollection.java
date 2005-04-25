/* This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package ptolemy.backtrack.util.java.util;

import java.lang.Object;
import java.lang.reflect.Array;
import java.util.Iterator;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;

public abstract class AbstractCollection implements Collection, Rollbackable {

    protected Checkpoint $CHECKPOINT = new Checkpoint(this);

    protected AbstractCollection() {
    }

    public abstract Iterator iterator();

    public abstract int size();

    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection c) {
        Iterator itr = c.iterator();
        boolean modified = false;
        int pos = c.size();
        while (--pos >= 0)
            modified |= add(itr.next());
        return modified;
    }

    public void clear() {
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0) {
            itr.next();
            itr.remove();
        }
    }

    public boolean contains(Object o) {
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0)
            if (equals(o, itr.next()))
                return true;
        return false;
    }

    public boolean containsAll(Collection c) {
        Iterator itr = c.iterator();
        int pos = c.size();
        while (--pos >= 0)
            if (!contains(itr.next()))
                return false;
        return true;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean remove(Object o) {
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0)
            if (equals(o, itr.next())) {
                itr.remove();
                return true;
            }
        return false;
    }

    public boolean removeAll(Collection c) {
        return removeAllInternal(c);
    }

    boolean removeAllInternal(Collection c) {
        Iterator itr = iterator();
        boolean modified = false;
        int pos = size();
        while (--pos >= 0)
            if (c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        return modified;
    }

    public boolean retainAll(Collection c) {
        return retainAllInternal(c);
    }

    boolean retainAllInternal(Collection c) {
        Iterator itr = iterator();
        boolean modified = false;
        int pos = size();
        while (--pos >= 0)
            if (!c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        return modified;
    }

    public Object[] toArray() {
        Iterator itr = iterator();
        int size = size();
        Object[] a = new Object[size];
        for (int pos = 0; pos < size; pos++)
            a[pos] = itr.next();
        return a;
    }

    public Object[] toArray(Object[] a) {
        int size = size();
        if (a.length < size)
            a = (Object[])Array.newInstance(a.getClass().getComponentType(), size);
        else if (a.length > size)
            a[size] = null;
        Iterator itr = iterator();
        for (int pos = 0; pos < size; pos++)
            a[pos] = itr.next();
        return a;
    }

    public String toString() {
        Iterator itr = iterator();
        StringBuffer r = new StringBuffer("[");
        for (int pos = size(); pos > 0; pos--) {
            r.append(itr.next());
            if (pos > 1)
                r.append(", ");
        }
        r.append("]");
        return r.toString();
    }

    static final boolean equals(Object o1, Object o2) {
        return o1 == null?o2 == null:o1.equals(o2);
    }

    static final int hashCode(Object o) {
        return o == null?0:o.hashCode();
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
            $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
            FieldRecord.popState($RECORDS);
            $RESTORE(timestamp, trim);
        }
    }

    public final Checkpoint $GET$CHECKPOINT() {
        return $CHECKPOINT;
    }

    public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
        if ($CHECKPOINT != checkpoint) {
            Checkpoint oldCheckpoint = $CHECKPOINT;
            if (checkpoint != null) {
                $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                FieldRecord.pushState($RECORDS);
            }
            $CHECKPOINT = checkpoint;
            oldCheckpoint.setCheckpoint(checkpoint);
            checkpoint.addObject(this);
        }
        return this;
    }

    protected CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

    private FieldRecord[] $RECORDS = new FieldRecord[] {
    };
}
