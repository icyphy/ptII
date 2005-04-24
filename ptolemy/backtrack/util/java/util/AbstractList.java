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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

public abstract class AbstractList extends AbstractCollection implements List, Rollbackable {

    private int modCount;

    protected int getModCount() {
        return modCount;
    }

    protected void setModCount(int modCount) {
        this.$ASSIGN$modCount(modCount);
    }

    protected AbstractList() {
    }

    public abstract Object get(int index);

    public void add(int index, Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean add(Object o) {
        add(size(), o);
        return true;
    }

    public boolean addAll(int index, Collection c) {
        Iterator itr = c.iterator();
        int size = c.size();
        for (int pos = size; pos > 0; pos--) 
            add(index++, itr.next());
        return size > 0;
    }

    public void clear() {
        removeRange(0, size());
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof List))
            return false;
        int size = size();
        if (size != ((List)o).size())
            return false;
        Iterator itr1 = iterator();
        Iterator itr2 = ((List)o).iterator();
        while (--size >= 0) 
            if (!equals(itr1.next(), itr2.next()))
                return false;
        return true;
    }

    public int hashCode() {
        int hashCode = 1;
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0) 
            hashCode = 31 * hashCode + hashCode(itr.next());
        return hashCode;
    }

    public int indexOf(Object o) {
        ListIterator itr = listIterator();
        int size = size();
        for (int pos = 0; pos < size; pos++) 
            if (equals(o, itr.next()))
                return pos;
        return -1;
    }

    public Iterator iterator() {
        return new Iterator() {

            private int pos = 0;

            private int size = size();

            private int last = -1;

            private int knownMod = getModCount();

            private void checkMod() {
                if (knownMod != getModCount())
                    throw new ConcurrentModificationException();
            }

            public boolean hasNext() {
                checkMod();
                return pos < size;
            }

            public Object next() {
                checkMod();
                if (pos == size)
                    throw new NoSuchElementException();
                $ASSIGN$last(pos);
                return get($ASSIGN$SPECIAL$pos(11, pos));
            }

            public void remove() {
                checkMod();
                if (last < 0)
                    throw new IllegalStateException();
                AbstractList.this.remove(last);
                $ASSIGN$SPECIAL$pos(12, pos);
                $ASSIGN$SPECIAL$size(12, size);
                $ASSIGN$last(-1);
                $ASSIGN$knownMod(getModCount());
            }

            final class _PROXY_ implements Rollbackable {

                public final void $COMMIT(long timestamp) {
                    $COMMIT_ANONYMOUS(timestamp);
                }

                public final void $RESTORE(long timestamp, boolean trim) {
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }

                public final Checkpoint $GET$CHECKPOINT() {
                    return $GET$CHECKPOINT_ANONYMOUS();
                }

                public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
                    $SET$CHECKPOINT_ANONYMOUS(checkpoint);
                    return this;
                }
            }

            private final int $ASSIGN$SPECIAL$pos(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$pos.add(null, pos, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return pos += newValue;
                    case 1:
                        return pos -= newValue;
                    case 2:
                        return pos *= newValue;
                    case 3:
                        return pos /= newValue;
                    case 4:
                        return pos &= newValue;
                    case 5:
                        return pos |= newValue;
                    case 6:
                        return pos ^= newValue;
                    case 7:
                        return pos %= newValue;
                    case 8:
                        return pos <<= newValue;
                    case 9:
                        return pos >>= newValue;
                    case 10:
                        return pos >>>= newValue;
                    case 11:
                        return pos++;
                    case 12:
                        return pos--;
                    case 13:
                        return ++pos;
                    case 14:
                        return --pos;
                    default:
                        return pos;
                }
            }

            private final int $ASSIGN$SPECIAL$size(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return size += newValue;
                    case 1:
                        return size -= newValue;
                    case 2:
                        return size *= newValue;
                    case 3:
                        return size /= newValue;
                    case 4:
                        return size &= newValue;
                    case 5:
                        return size |= newValue;
                    case 6:
                        return size ^= newValue;
                    case 7:
                        return size %= newValue;
                    case 8:
                        return size <<= newValue;
                    case 9:
                        return size >>= newValue;
                    case 10:
                        return size >>>= newValue;
                    case 11:
                        return size++;
                    case 12:
                        return size--;
                    case 13:
                        return ++size;
                    case 14:
                        return --size;
                    default:
                        return size;
                }
            }

            private final int $ASSIGN$last(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$last.add(null, last, $CHECKPOINT.getTimestamp());
                }
                return last = newValue;
            }

            private final int $ASSIGN$knownMod(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$knownMod.add(null, knownMod, $CHECKPOINT.getTimestamp());
                }
                return knownMod = newValue;
            }

            public void $COMMIT_ANONYMOUS(long timestamp) {
                FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                $RECORD$$CHECKPOINT.commit(timestamp);
            }

            public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                pos = $RECORD$pos.restore(pos, timestamp, trim);
                size = $RECORD$size.restore(size, timestamp, trim);
                last = $RECORD$last.restore(last, timestamp, trim);
                knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
                if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                    $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, new _PROXY_(), timestamp, trim);
                    FieldRecord.popState($RECORDS);
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }
            }

            public final Checkpoint $GET$CHECKPOINT_ANONYMOUS() {
                return $CHECKPOINT;
            }

            public final Object $SET$CHECKPOINT_ANONYMOUS(Checkpoint checkpoint) {
                if ($CHECKPOINT != checkpoint) {
                    Checkpoint oldCheckpoint = $CHECKPOINT;
                    if (checkpoint != null) {
                        $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                        FieldRecord.pushState($RECORDS);
                    }
                    $CHECKPOINT = checkpoint;
                    oldCheckpoint.setCheckpoint(checkpoint);
                    checkpoint.addObject(new _PROXY_());
                }
                return this;
            }

            private FieldRecord $RECORD$pos = new FieldRecord(0);

            private FieldRecord $RECORD$size = new FieldRecord(0);

            private FieldRecord $RECORD$last = new FieldRecord(0);

            private FieldRecord $RECORD$knownMod = new FieldRecord(0);

            private FieldRecord[] $RECORDS = new FieldRecord[] {
                    $RECORD$pos,
                    $RECORD$size,
                    $RECORD$last,
                    $RECORD$knownMod
                };

            {
                $CHECKPOINT.addObject(new _PROXY_());
            }
        };
    }

    public int lastIndexOf(Object o) {
        int pos = size();
        ListIterator itr = listIterator(pos);
        while (--pos >= 0) 
            if (equals(o, itr.previous()))
                return pos;
        return -1;
    }

    public ListIterator listIterator() {
        return listIterator(0);
    }

    public ListIterator listIterator(final int index) {
        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size());
        return new ListIterator() {

            private int knownMod = getModCount();

            private int position = index;

            private int lastReturned = -1;

            private int size = size();

            private void checkMod() {
                if (knownMod != getModCount())
                    throw new ConcurrentModificationException();
            }

            public boolean hasNext() {
                checkMod();
                return position < size;
            }

            public boolean hasPrevious() {
                checkMod();
                return position > 0;
            }

            public Object next() {
                checkMod();
                if (position == size)
                    throw new NoSuchElementException();
                $ASSIGN$lastReturned(position);
                return get($ASSIGN$SPECIAL$position(11, position));
            }

            public Object previous() {
                checkMod();
                if (position == 0)
                    throw new NoSuchElementException();
                $ASSIGN$lastReturned($ASSIGN$SPECIAL$position(14, position));
                return get(lastReturned);
            }

            public int nextIndex() {
                checkMod();
                return position;
            }

            public int previousIndex() {
                checkMod();
                return position - 1;
            }

            public void remove() {
                checkMod();
                if (lastReturned < 0)
                    throw new IllegalStateException();
                AbstractList.this.remove(lastReturned);
                $ASSIGN$SPECIAL$size(12, size);
                $ASSIGN$position(lastReturned);
                $ASSIGN$lastReturned(-1);
                $ASSIGN$knownMod(getModCount());
            }

            public void set(Object o) {
                checkMod();
                if (lastReturned < 0)
                    throw new IllegalStateException();
                AbstractList.this.set(lastReturned, o);
            }

            public void add(Object o) {
                checkMod();
                AbstractList.this.add($ASSIGN$SPECIAL$position(11, position), o);
                $ASSIGN$SPECIAL$size(11, size);
                $ASSIGN$lastReturned(-1);
                $ASSIGN$knownMod(getModCount());
            }

            final class _PROXY_ implements Rollbackable {

                public final void $COMMIT(long timestamp) {
                    $COMMIT_ANONYMOUS(timestamp);
                }

                public final void $RESTORE(long timestamp, boolean trim) {
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }

                public final Checkpoint $GET$CHECKPOINT() {
                    return $GET$CHECKPOINT_ANONYMOUS();
                }

                public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
                    $SET$CHECKPOINT_ANONYMOUS(checkpoint);
                    return this;
                }
            }

            private final int $ASSIGN$knownMod(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$knownMod.add(null, knownMod, $CHECKPOINT.getTimestamp());
                }
                return knownMod = newValue;
            }

            private final int $ASSIGN$position(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$position.add(null, position, $CHECKPOINT.getTimestamp());
                }
                return position = newValue;
            }

            private final int $ASSIGN$SPECIAL$position(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$position.add(null, position, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return position += newValue;
                    case 1:
                        return position -= newValue;
                    case 2:
                        return position *= newValue;
                    case 3:
                        return position /= newValue;
                    case 4:
                        return position &= newValue;
                    case 5:
                        return position |= newValue;
                    case 6:
                        return position ^= newValue;
                    case 7:
                        return position %= newValue;
                    case 8:
                        return position <<= newValue;
                    case 9:
                        return position >>= newValue;
                    case 10:
                        return position >>>= newValue;
                    case 11:
                        return position++;
                    case 12:
                        return position--;
                    case 13:
                        return ++position;
                    case 14:
                        return --position;
                    default:
                        return position;
                }
            }

            private final int $ASSIGN$lastReturned(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$lastReturned.add(null, lastReturned, $CHECKPOINT.getTimestamp());
                }
                return lastReturned = newValue;
            }

            private final int $ASSIGN$SPECIAL$size(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return size += newValue;
                    case 1:
                        return size -= newValue;
                    case 2:
                        return size *= newValue;
                    case 3:
                        return size /= newValue;
                    case 4:
                        return size &= newValue;
                    case 5:
                        return size |= newValue;
                    case 6:
                        return size ^= newValue;
                    case 7:
                        return size %= newValue;
                    case 8:
                        return size <<= newValue;
                    case 9:
                        return size >>= newValue;
                    case 10:
                        return size >>>= newValue;
                    case 11:
                        return size++;
                    case 12:
                        return size--;
                    case 13:
                        return ++size;
                    case 14:
                        return --size;
                    default:
                        return size;
                }
            }

            public void $COMMIT_ANONYMOUS(long timestamp) {
                FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                $RECORD$$CHECKPOINT.commit(timestamp);
            }

            public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
                position = $RECORD$position.restore(position, timestamp, trim);
                lastReturned = $RECORD$lastReturned.restore(lastReturned, timestamp, trim);
                size = $RECORD$size.restore(size, timestamp, trim);
                if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                    $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, new _PROXY_(), timestamp, trim);
                    FieldRecord.popState($RECORDS);
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }
            }

            public final Checkpoint $GET$CHECKPOINT_ANONYMOUS() {
                return $CHECKPOINT;
            }

            public final Object $SET$CHECKPOINT_ANONYMOUS(Checkpoint checkpoint) {
                if ($CHECKPOINT != checkpoint) {
                    Checkpoint oldCheckpoint = $CHECKPOINT;
                    if (checkpoint != null) {
                        $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                        FieldRecord.pushState($RECORDS);
                    }
                    $CHECKPOINT = checkpoint;
                    oldCheckpoint.setCheckpoint(checkpoint);
                    checkpoint.addObject(new _PROXY_());
                }
                return this;
            }

            private FieldRecord $RECORD$knownMod = new FieldRecord(0);

            private FieldRecord $RECORD$position = new FieldRecord(0);

            private FieldRecord $RECORD$lastReturned = new FieldRecord(0);

            private FieldRecord $RECORD$size = new FieldRecord(0);

            private FieldRecord[] $RECORDS = new FieldRecord[] {
                    $RECORD$knownMod,
                    $RECORD$position,
                    $RECORD$lastReturned,
                    $RECORD$size
                };

            {
                $CHECKPOINT.addObject(new _PROXY_());
            }
        };
    }

    public Object remove(int index) {
        throw new UnsupportedOperationException();
    }

    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator itr = listIterator(fromIndex);
        for (int index = fromIndex; index < toIndex; index++) {
            itr.next();
            itr.remove();
        }
    }

    public Object set(int index, Object o) {
        throw new UnsupportedOperationException();
    }

    public List subList(int fromIndex, int toIndex) {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException(fromIndex + " > "+toIndex);
        if (fromIndex < 0 || toIndex > size())
            throw new IndexOutOfBoundsException();
        if (this instanceof RandomAccess)
            return new RandomAccessSubList(this, fromIndex, toIndex);
        return new SubList(this, fromIndex, toIndex);
    }

    private final int $ASSIGN$modCount(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$modCount.add(null, modCount, $CHECKPOINT.getTimestamp());
        }
        return modCount = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        modCount = $RECORD$modCount.restore(modCount, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$modCount = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$modCount
        };
}

class SubList extends AbstractList implements Rollbackable {

    private final AbstractList backingList;

    final int offset;

    private int size;

    AbstractList getBackingList() {
        return backingList;
    }

    int getSize() {
        return size;
    }

    void setSize(int size) {
        this.$ASSIGN$size(size);
    }

    SubList(AbstractList backing, int fromIndex, int toIndex) {
        backingList = backing;
        setModCount(backing.getModCount());
        offset = fromIndex;
        $ASSIGN$size(toIndex - fromIndex);
    }

    void checkMod() {
        if (getModCount() != backingList.getModCount())
            throw new ConcurrentModificationException();
    }

    private void checkBoundsInclusive(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size);
    }

    private void checkBoundsExclusive(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size);
    }

    public int size() {
        checkMod();
        return size;
    }

    public Object set(int index, Object o) {
        checkMod();
        checkBoundsExclusive(index);
        return backingList.set(index + offset, o);
    }

    public Object get(int index) {
        checkMod();
        checkBoundsExclusive(index);
        return backingList.get(index + offset);
    }

    public void add(int index, Object o) {
        checkMod();
        checkBoundsInclusive(index);
        backingList.add(index + offset, o);
        $ASSIGN$SPECIAL$size(11, size);
        setModCount(backingList.getModCount());
    }

    public Object remove(int index) {
        checkMod();
        checkBoundsExclusive(index);
        Object o = backingList.remove(index + offset);
        $ASSIGN$SPECIAL$size(12, size);
        setModCount(backingList.getModCount());
        return o;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        checkMod();
        backingList.removeRange(offset + fromIndex, offset + toIndex);
        $ASSIGN$SPECIAL$size(1, toIndex - fromIndex);
        setModCount(backingList.getModCount());
    }

    public boolean addAll(int index, Collection c) {
        checkMod();
        checkBoundsInclusive(index);
        int csize = c.size();
        boolean result = backingList.addAll(offset + index, c);
        $ASSIGN$SPECIAL$size(0, csize);
        setModCount(backingList.getModCount());
        return result;
    }

    public boolean addAll(Collection c) {
        return addAll(size, c);
    }

    public Iterator iterator() {
        return listIterator();
    }

    public ListIterator listIterator(final int index) {
        checkMod();
        checkBoundsInclusive(index);
        return new ListIterator() {

            private final ListIterator i = backingList.listIterator(index + offset);

            private int position = index;

            public boolean hasNext() {
                checkMod();
                return position < size;
            }

            public boolean hasPrevious() {
                checkMod();
                return position > 0;
            }

            public Object next() {
                if (position == size)
                    throw new NoSuchElementException();
                $ASSIGN$SPECIAL$position(11, position);
                return i.next();
            }

            public Object previous() {
                if (position == 0)
                    throw new NoSuchElementException();
                $ASSIGN$SPECIAL$position(12, position);
                return i.previous();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            public void remove() {
                i.remove();
                $ASSIGN$SPECIAL$size(12, size);
                $ASSIGN$position(nextIndex());
                setModCount(backingList.getModCount());
            }

            public void set(Object o) {
                i.set(o);
            }

            public void add(Object o) {
                i.add(o);
                $ASSIGN$SPECIAL$size(11, size);
                $ASSIGN$SPECIAL$position(11, position);
                setModCount(backingList.getModCount());
            }

            final class _PROXY_ implements Rollbackable {

                public final void $COMMIT(long timestamp) {
                    $COMMIT_ANONYMOUS(timestamp);
                }

                public final void $RESTORE(long timestamp, boolean trim) {
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }

                public final Checkpoint $GET$CHECKPOINT() {
                    return $GET$CHECKPOINT_ANONYMOUS();
                }

                public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
                    $SET$CHECKPOINT_ANONYMOUS(checkpoint);
                    return this;
                }
            }

            private final int $ASSIGN$position(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$position.add(null, position, $CHECKPOINT.getTimestamp());
                }
                return position = newValue;
            }

            private final int $ASSIGN$SPECIAL$position(int operator, long newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$position.add(null, position, $CHECKPOINT.getTimestamp());
                }
                switch (operator) {
                    case 0:
                        return position += newValue;
                    case 1:
                        return position -= newValue;
                    case 2:
                        return position *= newValue;
                    case 3:
                        return position /= newValue;
                    case 4:
                        return position &= newValue;
                    case 5:
                        return position |= newValue;
                    case 6:
                        return position ^= newValue;
                    case 7:
                        return position %= newValue;
                    case 8:
                        return position <<= newValue;
                    case 9:
                        return position >>= newValue;
                    case 10:
                        return position >>>= newValue;
                    case 11:
                        return position++;
                    case 12:
                        return position--;
                    case 13:
                        return ++position;
                    case 14:
                        return --position;
                    default:
                        return position;
                }
            }

            public void $COMMIT_ANONYMOUS(long timestamp) {
                FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                $RECORD$$CHECKPOINT.commit(timestamp);
            }

            public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                $RECORD$i.restore(i, timestamp, trim);
                position = $RECORD$position.restore(position, timestamp, trim);
                if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                    $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, new _PROXY_(), timestamp, trim);
                    FieldRecord.popState($RECORDS);
                    $RESTORE_ANONYMOUS(timestamp, trim);
                }
            }

            public final Checkpoint $GET$CHECKPOINT_ANONYMOUS() {
                return $CHECKPOINT;
            }

            public final Object $SET$CHECKPOINT_ANONYMOUS(Checkpoint checkpoint) {
                if ($CHECKPOINT != checkpoint) {
                    Checkpoint oldCheckpoint = $CHECKPOINT;
                    if (checkpoint != null) {
                        $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                        FieldRecord.pushState($RECORDS);
                    }
                    $CHECKPOINT = checkpoint;
                    oldCheckpoint.setCheckpoint(checkpoint);
                    checkpoint.addObject(new _PROXY_());
                }
                return this;
            }

            private FieldRecord $RECORD$i = new FieldRecord(0);

            private FieldRecord $RECORD$position = new FieldRecord(0);

            private FieldRecord[] $RECORDS = new FieldRecord[] {
                    $RECORD$i,
                    $RECORD$position
                };

            {
                $CHECKPOINT.addObject(new _PROXY_());
            }
        };
    }

    private final int $ASSIGN$size(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
        }
        return size = newValue;
    }

    private final int $ASSIGN$SPECIAL$size(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return size += newValue;
            case 1:
                return size -= newValue;
            case 2:
                return size *= newValue;
            case 3:
                return size /= newValue;
            case 4:
                return size &= newValue;
            case 5:
                return size |= newValue;
            case 6:
                return size ^= newValue;
            case 7:
                return size %= newValue;
            case 8:
                return size <<= newValue;
            case 9:
                return size >>= newValue;
            case 10:
                return size >>>= newValue;
            case 11:
                return size++;
            case 12:
                return size--;
            case 13:
                return ++size;
            case 14:
                return --size;
            default:
                return size;
        }
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        $RECORD$backingList.restore(backingList, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$backingList = new FieldRecord(0);

    private FieldRecord $RECORD$size = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$backingList,
            $RECORD$size
        };
}

final class RandomAccessSubList extends SubList implements RandomAccess, Rollbackable {

    RandomAccessSubList(AbstractList backing, int fromIndex, int toIndex) {
        super(backing, fromIndex, toIndex);
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord[] $RECORDS = new FieldRecord[] {
        };
}
