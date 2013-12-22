/* LinkedList.java -- Linked list implementation of the List interface
   Copyright (C) 1998, 1999, 2000, 2001, 2002, 2004, 2005  Free Software Foundation, Inc.

This file is part of GNU Classpath.

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
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

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
import java.lang.reflect.Array;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;

/**
 * Linked list implementation of the List interface. In addition to the
 * methods of the List interface, this class provides access to the first
 * and last list elements in O(1) time for easy stack, queue, or double-ended
 * queue (deque) creation. The list is doubly-linked, with traversal to a
 * given index starting from the end closest to the element.<p>
 * LinkedList is not synchronized, so if you need multi-threaded access,
 * consider using:<br>
 * <code>List l = Collections.synchronizedList(new LinkedList(...));</code>
 * <p>
 * The iterators are <i>fail-fast</i>, meaning that any structural
 * modification, except for <code>remove()</code> called on the iterator
 * itself, cause the iterator to throw a{
@link ConcurrentModificationException}
 rather than exhibit
 * non-deterministic behavior.
 * @author Original author unknown
 * @author Bryce McKinlay
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see List
 * @see ArrayList
 * @see Vector
 * @see Collections#synchronizedList(List)
 * @since 1.2
 * @status missing javadoc, but complete to 1.4
 */
public class LinkedList extends AbstractSequentialList implements List,
        Cloneable, Serializable, Rollbackable {

    /**
     * Compatible with JDK 1.2.
     */
    private static final long serialVersionUID = 876323262645176354L;

    /**
     * The first element in the list.
     */
    private transient Entry first;

    /**
     * The last element in the list.
     */
    private transient Entry last;

    /**
     * The current length of the list.
     */
    private transient int size = 0;

    /**
     * Class to represent an entry in the list. Holds a single element.
     */
    private static final class Entry implements Rollbackable {

        private transient Checkpoint $CHECKPOINT = new Checkpoint(this);

        /**
         * The element in the list.
         */
        private Object data;

        /**
         * The next list entry, null if this is last.
         */
        private Entry next;

        /**
         * The previous list entry, null if this is first.
         */
        private Entry previous;

        /**
         * Construct an entry.
         * @param data the list element
         */
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
                $RECORD$previous
                        .add(null, previous, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return previous = newValue;
        }

        public void $COMMIT(long timestamp) {
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                    .getTopTimestamp());
            $RECORD$$CHECKPOINT.commit(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            data = $RECORD$data.restore(data, timestamp, trim);
            next = (Entry) $RECORD$next.restore(next, timestamp, trim);
            previous = (Entry) $RECORD$previous.restore(previous, timestamp,
                    trim);
            if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this,
                        timestamp, trim);
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
                    $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint
                            .getTimestamp());
                    FieldRecord.pushState($RECORDS);
                }
                $CHECKPOINT = checkpoint;
                oldCheckpoint.setCheckpoint(checkpoint);
                checkpoint.addObject(this);
            }
            return this;
        }

        private transient CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

        private transient FieldRecord $RECORD$data = new FieldRecord(0);

        private transient FieldRecord $RECORD$next = new FieldRecord(0);

        private transient FieldRecord $RECORD$previous = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$data, $RECORD$next, $RECORD$previous };

    }

    // class Entry
    // Package visible for use in nested classes.
    // n less than size/2, iterate from start
    // n greater than size/2, iterate from end
    // Package visible for use in nested classes.
    // Get the entries just before and after index. If index is at the start
    // of the list, BEFORE is null. If index is at the end of the list, AFTER
    // is null. If the list is empty, both are null.
    // Create the first new entry. We do not yet set the link from `before'
    // to the first entry, in order to deal with the case where (c == this).
    // [Actually, we don't have to handle this case to fufill the
    // contract for addAll(), but Sun's implementation appears to.]
    // Create and link all the remaining entries.
    // Link the new chain of entries into the list.
    /**
     * A ListIterator over the list. This class keeps track of its
     * position in the list and the two list entries it is between.
     * @author Original author unknown
     * @author Eric Blake (ebb9@email.byu.edu)
     */
    private final class LinkedListItr implements ListIterator, Rollbackable {

        private transient Checkpoint $CHECKPOINT = new Checkpoint(this);

        /**
         * Number of modifications we know about.
         */
        private int knownMod = getModCount();

        /**
         * Entry that will be returned by next().
         */
        private Entry next;

        /**
         * Entry that will be returned by previous().
         */
        private Entry previous;

        /**
         * Entry that will be affected by remove() or set().
         */
        private Entry lastReturned;

        /**
         * Index of `next'.
         */
        private int position;

        /**
         * Initialize the iterator.
         * @param index the initial index
         */
        LinkedListItr(int index) {
            if (index == getSize()) {
                $ASSIGN$next(null);
                $ASSIGN$previous(getLastField());
            } else {
                $ASSIGN$next(getEntry(index));
                $ASSIGN$previous(next.getPrevious());
            }
            $ASSIGN$position(index);
        }

        /**
         * Checks for iterator consistency.
         * @exception ConcurrentModificationException if the list was modified
         */
        private void checkMod() {
            if (knownMod != getModCount()) {
                throw new ConcurrentModificationException();
            }
        }

        /**
         * Returns the index of the next element.
         * @return the next index
         */
        public int nextIndex() {
            return position;
        }

        /**
         * Returns the index of the previous element.
         * @return the previous index
         */
        public int previousIndex() {
            return position - 1;
        }

        /**
         * Returns true if more elements exist via next.
         * @return true if next will succeed
         */
        public boolean hasNext() {
            return (next != null);
        }

        /**
         * Returns true if more elements exist via previous.
         * @return true if previous will succeed
         */
        public boolean hasPrevious() {
            return (previous != null);
        }

        /**
         * Returns the next element.
         * @return the next element
         * @exception ConcurrentModificationException if the list was modified
         * @exception NoSuchElementException if there is no next
         */
        public Object next() {
            checkMod();
            if (next == null) {
                throw new NoSuchElementException();
            }
            $ASSIGN$SPECIAL$position(11, position);
            $ASSIGN$lastReturned($ASSIGN$previous(next));
            $ASSIGN$next(lastReturned.getNext());
            return lastReturned.getData();
        }

        /**
         * Returns the previous element.
         * @return the previous element
         * @exception ConcurrentModificationException if the list was modified
         * @exception NoSuchElementException if there is no previous
         */
        public Object previous() {
            checkMod();
            if (previous == null) {
                throw new NoSuchElementException();
            }
            $ASSIGN$SPECIAL$position(12, position);
            $ASSIGN$lastReturned($ASSIGN$next(previous));
            $ASSIGN$previous(lastReturned.getPrevious());
            return lastReturned.getData();
        }

        /**
         * Remove the most recently returned element from the list.
         * @exception ConcurrentModificationException if the list was modified
         * @exception IllegalStateException if there was no last element
         */
        public void remove() {
            checkMod();
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            // Adjust the position to before the removed element, if the element
            // being removed is behind the cursor.
            if (lastReturned == previous) {
                $ASSIGN$SPECIAL$position(12, position);
            }
            $ASSIGN$next(lastReturned.getNext());
            $ASSIGN$previous(lastReturned.getPrevious());
            removeEntry(lastReturned);
            $ASSIGN$SPECIAL$knownMod(11, knownMod);
            $ASSIGN$lastReturned(null);
        }

        /**
         * Adds an element between the previous and next, and advance to the next.
         * @param o the element to add
         * @exception ConcurrentModificationException if the list was modified
         */
        public void add(Object o) {
            checkMod();
            setModCount(getModCount() + 1);
            $ASSIGN$SPECIAL$knownMod(11, knownMod);
            setSize(getSize() + 1);
            $ASSIGN$SPECIAL$position(11, position);
            Entry e = new Entry(o);
            e.setPrevious(previous);
            e.setNext(next);
            if (previous != null) {
                previous.setNext(e);
            } else {
                setFirstField(e);
            }
            if (next != null) {
                next.setPrevious(e);
            } else {
                setLastField(e);
            }
            $ASSIGN$previous(e);
            $ASSIGN$lastReturned(null);
        }

        /**
         * Changes the contents of the element most recently returned.
         * @param o the new element
         * @exception ConcurrentModificationException if the list was modified
         * @exception IllegalStateException if there was no last element
         */
        public void set(Object o) {
            checkMod();
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            lastReturned.setData(o);
        }

        private final int $ASSIGN$SPECIAL$knownMod(int operator, long newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$knownMod
                        .add(null, knownMod, $CHECKPOINT.getTimestamp());
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
                $RECORD$previous
                        .add(null, previous, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return previous = newValue;
        }

        private final Entry $ASSIGN$lastReturned(Entry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$lastReturned.add(null, lastReturned, $CHECKPOINT
                        .getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return lastReturned = newValue;
        }

        private final int $ASSIGN$position(int newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$position
                        .add(null, position, $CHECKPOINT.getTimestamp());
            }
            return position = newValue;
        }

        private final int $ASSIGN$SPECIAL$position(int operator, long newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$position
                        .add(null, position, $CHECKPOINT.getTimestamp());
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
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                    .getTopTimestamp());
            $RECORD$$CHECKPOINT.commit(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
            next = (Entry) $RECORD$next.restore(next, timestamp, trim);
            previous = (Entry) $RECORD$previous.restore(previous, timestamp,
                    trim);
            lastReturned = (Entry) $RECORD$lastReturned.restore(lastReturned,
                    timestamp, trim);
            position = $RECORD$position.restore(position, timestamp, trim);
            if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this,
                        timestamp, trim);
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
                    $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint
                            .getTimestamp());
                    FieldRecord.pushState($RECORDS);
                }
                $CHECKPOINT = checkpoint;
                oldCheckpoint.setCheckpoint(checkpoint);
                checkpoint.addObject(this);
            }
            return this;
        }

        private transient CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

        private transient FieldRecord $RECORD$knownMod = new FieldRecord(0);

        private transient FieldRecord $RECORD$next = new FieldRecord(0);

        private transient FieldRecord $RECORD$previous = new FieldRecord(0);

        private transient FieldRecord $RECORD$lastReturned = new FieldRecord(0);

        private transient FieldRecord $RECORD$position = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$knownMod, $RECORD$next, $RECORD$previous,
                $RECORD$lastReturned, $RECORD$position };

    }

    // class LinkedListItr
    /**
     * Obtain the Entry at a given position in a list. This method of course
     * takes linear time, but it is intelligent enough to take the shorter of the
     * paths to get to the Entry required. This implies that the first or last
     * entry in the list is obtained in constant time, which is a very desirable
     * property.
     * For speed and flexibility, range checking is not done in this method:
     * Incorrect values will be returned if (n &lt; 0) or (n &gt;= size).
     * @param n the number of the entry to get
     * @return the entry at position n
     */
    Entry getEntry(int n) {
        Entry e;
        if (n < getSize() / 2) {
            e = getFirstField();
            while (n-- > 0) {
                e = e.getNext();
            }
        } else {
            e = getLastField();
            while (++n < getSize()) {
                e = e.getPrevious();
            }
        }
        return e;
    }

    /**
     * Remove an entry from the list. This will adjust size and deal with
     * `first' and  `last' appropriatly.
     * @param e the entry to remove
     */
    void removeEntry(Entry e) {
        setModCount(getModCount() + 1);
        setSize(getSize() - 1);
        if (getSize() == 0) {
            setFirstField(setLastField(null));
        } else {
            if (e == getFirstField()) {
                setFirstField(e.getNext());
                e.getNext().setPrevious(null);
            } else if (e == getLastField()) {
                setLastField(e.getPrevious());
                e.getPrevious().setNext(null);
            } else {
                e.getNext().setPrevious(e.getPrevious());
                e.getPrevious().setNext(e.getNext());
            }
        }
    }

    /**
     * Checks that the index is in the range of possible elements (inclusive).
     * @param index the index to check
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt; size
     */
    private void checkBoundsInclusive(int index) {
        if (index < 0 || index > getSize()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size:"
                    + getSize());
        }
    }

    /**
     * Checks that the index is in the range of existing elements (exclusive).
     * @param index the index to check
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt;= size
     */
    private void checkBoundsExclusive(int index) {
        if (index < 0 || index >= getSize()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size:"
                    + getSize());
        }
    }

    /**
     * Create an empty linked list.
     */
    public LinkedList() {
    }

    /**
     * Create a linked list containing the elements, in order, of a given
     * collection.
     * @param c the collection to populate this list from
     * @exception NullPointerException if c is null
     */
    public LinkedList(Collection c) {
        addAll(c);
    }

    /**
     * Returns the first element in the list.
     * @return the first list element
     * @exception NoSuchElementException if the list is empty
     */
    public Object getFirst() {
        if (getSize() == 0) {
            throw new NoSuchElementException();
        }
        return getFirstField().getData();
    }

    /**
     * Returns the last element in the list.
     * @return the last list element
     * @exception NoSuchElementException if the list is empty
     */
    public Object getLast() {
        if (getSize() == 0) {
            throw new NoSuchElementException();
        }
        return getLastField().getData();
    }

    /**
     * Remove and return the first element in the list.
     * @return the former first element in the list
     * @exception NoSuchElementException if the list is empty
     */
    public Object removeFirst() {
        if (getSize() == 0) {
            throw new NoSuchElementException();
        }
        setModCount(getModCount() + 1);
        setSize(getSize() - 1);
        Object r = getFirstField().getData();
        if (getFirstField().getNext() != null) {
            getFirstField().getNext().setPrevious(null);
        } else {
            setLastField(null);
        }
        setFirstField(getFirstField().getNext());
        return r;
    }

    /**
     * Remove and return the last element in the list.
     * @return the former last element in the list
     * @exception NoSuchElementException if the list is empty
     */
    public Object removeLast() {
        if (getSize() == 0) {
            throw new NoSuchElementException();
        }
        setModCount(getModCount() + 1);
        setSize(getSize() - 1);
        Object r = getLastField().getData();
        if (getLastField().getPrevious() != null) {
            getLastField().getPrevious().setNext(null);
        } else {
            setFirstField(null);
        }
        setLastField(getLastField().getPrevious());
        return r;
    }

    /**
     * Insert an element at the first of the list.
     * @param o the element to insert
     */
    public void addFirst(Object o) {
        Entry e = new Entry(o);
        setModCount(getModCount() + 1);
        if (getSize() == 0) {
            setFirstField(setLastField(e));
        } else {
            e.setNext(getFirstField());
            getFirstField().setPrevious(e);
            setFirstField(e);
        }
        setSize(getSize() + 1);
    }

    /**
     * Insert an element at the last of the list.
     * @param o the element to insert
     */
    public void addLast(Object o) {
        addLastEntry(new Entry(o));
    }

    /**
     * Inserts an element at the end of the list.
     * @param e the entry to add
     */
    private void addLastEntry(Entry e) {
        setModCount(getModCount() + 1);
        if (getSize() == 0) {
            setFirstField(setLastField(e));
        } else {
            e.setPrevious(getLastField());
            getLastField().setNext(e);
            setLastField(e);
        }
        setSize(getSize() + 1);
    }

    /**
     * Returns true if the list contains the given object. Comparison is done by
     * <code>o == null ? e = null : o.equals(e)</code>.
     * @param o the element to look for
     * @return true if it is found
     */
    public boolean contains(Object o) {
        Entry e = getFirstField();
        while (e != null) {
            if (equals(o, e.getData())) {
                return true;
            }
            e = e.getNext();
        }
        return false;
    }

    /**
     * Returns the size of the list.
     * @return the list size
     */
    public int size() {
        return getSize();
    }

    /**
     * Adds an element to the end of the list.
     * @param o the entry to add
     * @return true, as it always succeeds
     */
    public boolean add(Object o) {
        addLastEntry(new Entry(o));
        return true;
    }

    /**
     * Removes the entry at the lowest index in the list that matches the given
     * object, comparing by <code>o == null ? e = null : o.equals(e)</code>.
     * @param o the object to remove
     * @return true if an instance of the object was removed
     */
    public boolean remove(Object o) {
        Entry e = getFirstField();
        while (e != null) {
            if (equals(o, e.getData())) {
                removeEntry(e);
                return true;
            }
            e = e.getNext();
        }
        return false;
    }

    /**
     * Append the elements of the collection in iteration order to the end of
     * this list. If this list is modified externally (for example, if this
     * list is the collection), behavior is unspecified.
     * @param c the collection to append
     * @return true if the list was modified
     * @exception NullPointerException if c is null
     */
    public boolean addAll(Collection c) {
        return addAll(getSize(), c);
    }

    /**
     * Insert the elements of the collection in iteration order at the given
     * index of this list. If this list is modified externally (for example,
     * if this list is the collection), behavior is unspecified.
     * @param c the collection to append
     * @return true if the list was modified
     * @exception NullPointerException if c is null
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     */
    public boolean addAll(int index, Collection c) {
        checkBoundsInclusive(index);
        int csize = c.size();
        if (csize == 0) {
            return false;
        }
        Iterator itr = c.iterator();
        Entry after = null;
        Entry before = null;
        if (index != getSize()) {
            after = getEntry(index);
            before = after.getPrevious();
        } else {
            before = getLastField();
        }
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
        setSize(getSize() + csize);
        prev.setNext(after);
        if (after != null) {
            after.setPrevious(e);
        } else {
            setLastField(e);
        }
        if (before != null) {
            before.setNext(firstNew);
        } else {
            setFirstField(firstNew);
        }
        return true;
    }

    /**
     * Remove all elements from this list.
     */
    public void clear() {
        if (getSize() > 0) {
            setModCount(getModCount() + 1);
            setFirstField(null);
            setLastField(null);
            setSize(0);
        }
    }

    /**
     * Return the element at index.
     * @param index the place to look
     * @return the element at index
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
     */
    public Object get(int index) {
        checkBoundsExclusive(index);
        return getEntry(index).getData();
    }

    /**
     * Replace the element at the given location in the list.
     * @param index which index to change
     * @param o the new element
     * @return the prior element
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
     */
    public Object set(int index, Object o) {
        checkBoundsExclusive(index);
        Entry e = getEntry(index);
        Object old = e.getData();
        e.setData(o);
        return old;
    }

    /**
     * Inserts an element in the given position in the list.
     * @param index where to insert the element
     * @param o the element to insert
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     */
    public void add(int index, Object o) {
        checkBoundsInclusive(index);
        Entry e = new Entry(o);
        if (index < getSize()) {
            setModCount(getModCount() + 1);
            Entry after = getEntry(index);
            e.setNext(after);
            e.setPrevious(after.getPrevious());
            if (after.getPrevious() == null) {
                setFirstField(e);
            } else {
                after.getPrevious().setNext(e);
            }
            after.setPrevious(e);
            setSize(getSize() + 1);
        } else {
            addLastEntry(e);
        }
    }

    /**
     * Removes the element at the given position from the list.
     * @param index the location of the element to remove
     * @return the removed element
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     */
    public Object remove(int index) {
        checkBoundsExclusive(index);
        Entry e = getEntry(index);
        removeEntry(e);
        return e.getData();
    }

    /**
     * Returns the first index where the element is located in the list, or -1.
     * @param o the element to look for
     * @return its position, or -1 if not found
     */
    public int indexOf(Object o) {
        int index = 0;
        Entry e = getFirstField();
        while (e != null) {
            if (equals(o, e.getData())) {
                return index;
            }
            index++;
            e = e.getNext();
        }
        return -1;
    }

    /**
     * Returns the last index where the element is located in the list, or -1.
     * @param o the element to look for
     * @return its position, or -1 if not found
     */
    public int lastIndexOf(Object o) {
        int index = getSize() - 1;
        Entry e = getLastField();
        while (e != null) {
            if (equals(o, e.getData())) {
                return index;
            }
            index--;
            e = e.getPrevious();
        }
        return -1;
    }

    /**
     * Obtain a ListIterator over this list, starting at a given index. The
     * ListIterator returned by this method supports the add, remove and set
     * methods.
     * @param index the index of the element to be returned by the first call to
     * next(), or size() to be initially positioned at the end of the list
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     */
    public ListIterator listIterator(int index) {
        checkBoundsInclusive(index);
        return new LinkedListItr(index);
    }

    /**
     * Create a shallow copy of this LinkedList (the elements are not cloned).
     * @return an object of the same class as this object, containing the
     * same elements in the same order
     */
    public Object clone() {
        LinkedList copy = null;
        try {
            copy = (LinkedList) super.clone();
        } catch (CloneNotSupportedException ex) {
        }
        copy.clear();
        copy.addAll(this);
        return copy;
    }

    /**
     * Returns an array which contains the elements of the list in order.
     * @return an array containing the list elements
     */
    public Object[] toArray() {
        Object[] array = new Object[getSize()];
        Entry e = getFirstField();
        for (int i = 0; i < getSize(); i++) {
            array[i] = e.getData();
            e = e.getNext();
        }
        return array;
    }

    /**
     * Returns an Array whose component type is the runtime component type of
     * the passed-in Array.  The returned Array is populated with all of the
     * elements in this LinkedList.  If the passed-in Array is not large enough
     * to store all of the elements in this List, a new Array will be created
     * and returned; if the passed-in Array is <i>larger</i> than the size
     * of this List, then size() index will be set to null.
     * @param a the passed-in Array
     * @return an array representation of this list
     * @exception ArrayStoreException if the runtime type of a does not allow
     * an element in this list
     * @exception NullPointerException if a is null
     */
    public Object[] toArray(Object[] a) {
        if (a.length < getSize()) {
            a = (Object[]) Array.newInstance(a.getClass().getComponentType(),
                    getSize());
        } else if (a.length > getSize()) {
            a[getSize()] = null;
        }
        Entry e = getFirstField();
        for (int i = 0; i < getSize(); i++) {
            a[i] = e.getData();
            e = e.getNext();
        }
        return a;
    }

    /**
     * Serializes this object to the given stream.
     * @param s the stream to write to
     * @exception IOException if the underlying stream fails
     * @serialData the size of the list (int), followed by all the elements
     * (Object) in proper order
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(getSize());
        Entry e = getFirstField();
        while (e != null) {
            s.writeObject(e.getData());
            e = e.getNext();
        }
    }

    /**
     * Deserializes this object from the given stream.
     * @param s the stream to read from
     * @exception ClassNotFoundException if the underlying stream fails
     * @exception IOException if the underlying stream fails
     * @serialData the size of the list (int), followed by all the elements
     * (Object) in proper order
     */
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        int i = s.readInt();
        while (--i >= 0) {
            addLastEntry(new Entry(s.readObject()));
        }
    }

    void setFirstField(Entry first) {
        this.$ASSIGN$first(first);
    }

    Entry getFirstField() {
        return first;
    }

    Entry setLastField(Entry last) {
        return this.$ASSIGN$last(last);
    }

    Entry getLastField() {
        return last;
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

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                .getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        first = (Entry) $RECORD$first.restore(first, timestamp, trim);
        last = (Entry) $RECORD$last.restore(last, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private transient FieldRecord $RECORD$first = new FieldRecord(0);

    private transient FieldRecord $RECORD$last = new FieldRecord(0);

    private transient FieldRecord $RECORD$size = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$first, $RECORD$last, $RECORD$size };

}
