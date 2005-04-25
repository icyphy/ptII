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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.Object;
import java.lang.reflect.Array;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;

public class LinkedList extends AbstractSequentialList implements List, Cloneable, Serializable, Rollbackable {

    private static final long serialVersionUID = 876323262645176354L;

    private transient Entry first;

    private transient Entry last;

    private transient int size = 0;

    private static final class Entry implements Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private Object data;

        private Entry next;

        private Entry previous;

        Entry(Object data) {
            this.setData(data);
        }

        void setData(Object data) {
            this.$ASSIGN$data(data);
        }

        Object getData() {
            return data;
        }

        void setNext(Entry next) {
            this.$ASSIGN$next(next);
        }

        Entry getNext() {
            return next;
        }

        void setPrevious(Entry previous) {
            this.$ASSIGN$previous(previous);
        }

        Entry getPrevious() {
            return previous;
        }

        private final Object $ASSIGN$data(Object newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$data.add(null, data, $CHECKPOINT.getTimestamp());
            }
            return data = newValue;
        }

        private final Entry $ASSIGN$next(Entry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$next.add(null, next, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return next = newValue;
        }

        private final Entry $ASSIGN$previous(Entry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$previous.add(null, previous, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return previous = newValue;
        }

        public void $COMMIT(long timestamp) {
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
            $RECORD$$CHECKPOINT.commit(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            data = (Object)$RECORD$data.restore(data, timestamp, trim);
            next = (Entry)$RECORD$next.restore(next, timestamp, trim);
            previous = (Entry)$RECORD$previous.restore(previous, timestamp, trim);
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

        private FieldRecord $RECORD$data = new FieldRecord(0);

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord $RECORD$previous = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$data,
            $RECORD$next,
            $RECORD$previous
        };
    }

    private final class LinkedListItr implements ListIterator, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private int knownMod = getModCount();

        private Entry next;

        private Entry previous;

        private Entry lastReturned;

        private int position;

        LinkedListItr(int index) {
            if (index == size) {
                $ASSIGN$next(null);
                $ASSIGN$previous(last);
            } else {
                $ASSIGN$next(getEntry(index));
                $ASSIGN$previous(next.getPrevious());
            }
            $ASSIGN$position(index);
        }

        private void checkMod() {
            if (knownMod != getModCount())
                throw new ConcurrentModificationException();
        }

        public int nextIndex() {
            checkMod();
            return position;
        }

        public int previousIndex() {
            checkMod();
            return position - 1;
        }

        public boolean hasNext() {
            checkMod();
            return (next != null);
        }

        public boolean hasPrevious() {
            checkMod();
            return (previous != null);
        }

        public Object next() {
            checkMod();
            if (next == null)
                throw new NoSuchElementException();
            $ASSIGN$SPECIAL$position(11, position);
            $ASSIGN$lastReturned($ASSIGN$previous(next));
            $ASSIGN$next(lastReturned.getNext());
            return lastReturned.getData();
        }

        public Object previous() {
            checkMod();
            if (previous == null)
                throw new NoSuchElementException();
            $ASSIGN$SPECIAL$position(12, position);
            $ASSIGN$lastReturned($ASSIGN$next(previous));
            $ASSIGN$previous(lastReturned.getPrevious());
            return lastReturned.getData();
        }

        public void remove() {
            checkMod();
            if (lastReturned == null)
                throw new IllegalStateException();
            if (lastReturned == previous)
                $ASSIGN$SPECIAL$position(12, position);
            $ASSIGN$next(lastReturned.getNext());
            $ASSIGN$previous(lastReturned.getPrevious());
            removeEntry(lastReturned);
            $ASSIGN$SPECIAL$knownMod(11, knownMod);
            $ASSIGN$lastReturned(null);
        }

        public void add(Object o) {
            checkMod();
            setModCount(getModCount() + 1);
            $ASSIGN$SPECIAL$knownMod(11, knownMod);
            $ASSIGN$SPECIAL$size(11, size);
            $ASSIGN$SPECIAL$position(11, position);
            Entry e = new Entry(o);
            e.setPrevious(previous);
            e.setNext(next);
            if (previous != null)
                previous.setNext(e);
            else
                $ASSIGN$first(e);
            if (next != null)
                next.setPrevious(e);
            else
                $ASSIGN$last(e);
            $ASSIGN$previous(e);
            $ASSIGN$lastReturned(null);
        }

        public void set(Object o) {
            checkMod();
            if (lastReturned == null)
                throw new IllegalStateException();
            lastReturned.setData(o);
        }

        private final int $ASSIGN$SPECIAL$knownMod(int operator, long newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$knownMod.add(null, knownMod, $CHECKPOINT.getTimestamp());
            }
            switch (operator) {
            case 0:
                return knownMod += newValue;
            case 1:
                return knownMod -= newValue;
            case 2:
                return knownMod *= newValue;
            case 3:
                return knownMod /= newValue;
            case 4:
                return knownMod &= newValue;
            case 5:
                return knownMod |= newValue;
            case 6:
                return knownMod ^= newValue;
            case 7:
                return knownMod %= newValue;
            case 8:
                return knownMod <<= newValue;
            case 9:
                return knownMod >>= newValue;
            case 10:
                return knownMod >>>= newValue;
            case 11:
                return knownMod++;
            case 12:
                return knownMod--;
            case 13:
                return ++knownMod;
            case 14:
                return --knownMod;
            default:
                return knownMod;
            }
        }

        private final Entry $ASSIGN$next(Entry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$next.add(null, next, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return next = newValue;
        }

        private final Entry $ASSIGN$previous(Entry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$previous.add(null, previous, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return previous = newValue;
        }

        private final Entry $ASSIGN$lastReturned(Entry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$lastReturned.add(null, lastReturned, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return lastReturned = newValue;
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

        public void $COMMIT(long timestamp) {
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
            $RECORD$$CHECKPOINT.commit(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
            next = (Entry)$RECORD$next.restore(next, timestamp, trim);
            previous = (Entry)$RECORD$previous.restore(previous, timestamp, trim);
            lastReturned = (Entry)$RECORD$lastReturned.restore(lastReturned, timestamp, trim);
            position = $RECORD$position.restore(position, timestamp, trim);
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

        private FieldRecord $RECORD$knownMod = new FieldRecord(0);

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord $RECORD$previous = new FieldRecord(0);

        private FieldRecord $RECORD$lastReturned = new FieldRecord(0);

        private FieldRecord $RECORD$position = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$knownMod,
            $RECORD$next,
            $RECORD$previous,
            $RECORD$lastReturned,
            $RECORD$position
        };
    }

    Entry getEntry(int n) {
        Entry e;
        if (n < size / 2) {
            e = first;
            while (n-- > 0)
                e = e.getNext();
        } else {
            e = last;
            while (++n < size)
                e = e.getPrevious();
        }
        return e;
    }

    void removeEntry(Entry e) {
        setModCount(getModCount() + 1);
        $ASSIGN$SPECIAL$size(12, size);
        if (size == 0)
            $ASSIGN$first($ASSIGN$last(null));
        else {
            if (e == first) {
                $ASSIGN$first(e.getNext());
                e.getNext().setPrevious(null);
            } else if (e == last) {
                $ASSIGN$last(e.getPrevious());
                e.getPrevious().setNext(null);
            } else {
                e.getNext().setPrevious(e.getPrevious());
                e.getPrevious().setNext(e.getNext());
            }
        }
    }

    private void checkBoundsInclusive(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size);
    }

    private void checkBoundsExclusive(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index+", Size:"+size);
    }

    public LinkedList() {
    }

    public LinkedList(Collection c) {
        addAll(c);
    }

    public Object getFirst() {
        if (size == 0)
            throw new NoSuchElementException();
        return first.getData();
    }

    public Object getLast() {
        if (size == 0)
            throw new NoSuchElementException();
        return last.getData();
    }

    public Object removeFirst() {
        if (size == 0)
            throw new NoSuchElementException();
        setModCount(getModCount() + 1);
        $ASSIGN$SPECIAL$size(12, size);
        Object r = first.getData();
        if (first.getNext() != null)
            first.getNext().setPrevious(null);
        else
            $ASSIGN$last(null);
        $ASSIGN$first(first.getNext());
        return r;
    }

    public Object removeLast() {
        if (size == 0)
            throw new NoSuchElementException();
        setModCount(getModCount() + 1);
        $ASSIGN$SPECIAL$size(12, size);
        Object r = last.getData();
        if (last.getPrevious() != null)
            last.getPrevious().setNext(null);
        else
            $ASSIGN$first(null);
        $ASSIGN$last(last.getPrevious());
        return r;
    }

    public void addFirst(Object o) {
        Entry e = new Entry(o);
        setModCount(getModCount() + 1);
        if (size == 0)
            $ASSIGN$first($ASSIGN$last(e));
        else {
            e.setNext(first);
            first.setPrevious(e);
            $ASSIGN$first(e);
        }
        $ASSIGN$SPECIAL$size(11, size);
    }

    public void addLast(Object o) {
        addLastEntry(new Entry(o));
    }

    private void addLastEntry(Entry e) {
        setModCount(getModCount() + 1);
        if (size == 0)
            $ASSIGN$first($ASSIGN$last(e));
        else {
            e.setPrevious(last);
            last.setNext(e);
            $ASSIGN$last(e);
        }
        $ASSIGN$SPECIAL$size(11, size);
    }

    public boolean contains(Object o) {
        Entry e = first;
        while (e != null) {
            if (equals(o, e.getData()))
                return true;
            e = e.getNext();
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean add(Object o) {
        addLastEntry(new Entry(o));
        return true;
    }

    public boolean remove(Object o) {
        Entry e = first;
        while (e != null) {
            if (equals(o, e.getData())) {
                removeEntry(e);
                return true;
            }
            e = e.getNext();
        }
        return false;
    }

    public boolean addAll(Collection c) {
        return addAll(size, c);
    }

    public boolean addAll(int index, Collection c) {
        checkBoundsInclusive(index);
        int csize = c.size();
        if (csize == 0)
            return false;
        Iterator itr = c.iterator();
        Entry after = null;
        Entry before = null;
        if (index != size) {
            after = getEntry(index);
            before = after.getPrevious();
        } else
            before = last;
        Entry e = new Entry(itr.next());
        e.setPrevious(before);
        Entry prev = e;
        Entry firstNew = e;
        for (int pos = 1; pos < csize; pos++) {
            e = new Entry(itr.next());
            e.setPrevious(prev);
            prev.setNext(e);
            prev = e;
        }
        setModCount(getModCount() + 1);
        $ASSIGN$SPECIAL$size(0, csize);
        prev.setNext(after);
        if (after != null)
            after.setPrevious(e);
        else
            $ASSIGN$last(e);
        if (before != null)
            before.setNext(firstNew);
        else
            $ASSIGN$first(firstNew);
        return true;
    }

    public void clear() {
        if (size > 0) {
            setModCount(getModCount() + 1);
            $ASSIGN$first(null);
            $ASSIGN$last(null);
            $ASSIGN$size(0);
        }
    }

    public Object get(int index) {
        checkBoundsExclusive(index);
        return getEntry(index).getData();
    }

    public Object set(int index, Object o) {
        checkBoundsExclusive(index);
        Entry e = getEntry(index);
        Object old = e.getData();
        e.setData(o);
        return old;
    }

    public void add(int index, Object o) {
        checkBoundsInclusive(index);
        Entry e = new Entry(o);
        if (index < size) {
            setModCount(getModCount() + 1);
            Entry after = getEntry(index);
            e.setNext(after);
            e.setPrevious(after.getPrevious());
            if (after.getPrevious() == null)
                $ASSIGN$first(e);
            else
                after.getPrevious().setNext(e);
            after.setPrevious(e);
            $ASSIGN$SPECIAL$size(11, size);
        } else
            addLastEntry(e);
    }

    public Object remove(int index) {
        checkBoundsExclusive(index);
        Entry e = getEntry(index);
        removeEntry(e);
        return e.getData();
    }

    public int indexOf(Object o) {
        int index = 0;
        Entry e = first;
        while (e != null) {
            if (equals(o, e.getData()))
                return index;
            index++;
            e = e.getNext();
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        int index = size - 1;
        Entry e = last;
        while (e != null) {
            if (equals(o, e.getData()))
                return index;
            index--;
            e = e.getPrevious();
        }
        return -1;
    }

    public ListIterator listIterator(int index) {
        checkBoundsInclusive(index);
        return new LinkedListItr(index);
    }

    public Object clone() {
        LinkedList copy = null;
        try {
            copy = (LinkedList)super.clone();
        } catch (CloneNotSupportedException ex) {
        }
        copy.clear();
        copy.addAll(this);
        return copy;
    }

    public Object[] toArray() {
        Object[] array = new Object[size];
        Entry e = first;
        for (int i = 0; i < size; i++) {
            array[i] = e.getData();
            e = e.getNext();
        }
        return array;
    }

    public Object[] toArray(Object[] a) {
        if (a.length < size)
            a = (Object[])Array.newInstance(a.getClass().getComponentType(), size);
        else if (a.length > size)
            a[size] = null;
        Entry e = first;
        for (int i = 0; i < size; i++) {
            a[i] = e.getData();
            e = e.getNext();
        }
        return a;
    }

    private void writeObject(ObjectOutputStream s) throws IOException  {
        s.defaultWriteObject();
        s.writeInt(size);
        Entry e = first;
        while (e != null) {
            s.writeObject(e.getData());
            e = e.getNext();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
        s.defaultReadObject();
        int i = s.readInt();
        while (--i >= 0)
            addLastEntry(new Entry(s.readObject()));
    }

    void setSize(int size) {
        this.$ASSIGN$size(size);
    }

    int getSize() {
        return size;
    }

    private final Entry $ASSIGN$first(Entry newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$first.add(null, first, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return first = newValue;
    }

    private final Entry $ASSIGN$last(Entry newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$last.add(null, last, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return last = newValue;
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
        first = (Entry)$RECORD$first.restore(first, timestamp, trim);
        last = (Entry)$RECORD$last.restore(last, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$first = new FieldRecord(0);

    private FieldRecord $RECORD$last = new FieldRecord(0);

    private FieldRecord $RECORD$size = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
        $RECORD$first,
        $RECORD$last,
        $RECORD$size
    };
}
