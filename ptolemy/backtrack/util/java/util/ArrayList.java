/* ArrayList.java -- JDK1.2's answer to Vector; this is an array-backed
   implementation of the List interface
   Copyright (C) 1998, 1999, 2000, 2001, 2004, 2005  Free Software Foundation, Inc.

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.RandomAccess;

import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

/**
 * An array-backed implementation of the List interface.  This implements
 * all optional list operations, and permits null elements, so that it is
 * better than Vector, which it replaces. Random access is roughly constant
 * time, and iteration is roughly linear time, so it is nice and fast, with
 * less overhead than a LinkedList.
 * <p>
 * Each list has a capacity, and as the array reaches that capacity it
 * is automatically transferred to a larger array. You also have access to
 * ensureCapacity and trimToSize to control the backing array's size, avoiding
 * reallocation or wasted memory.
 * <p>
 * ArrayList is not synchronized, so if you need multi-threaded access,
 * consider using:<br>
 * <code>List l = Collections.synchronizedList(new ArrayList(...));</code>
 * <p>
 * The iterators are <i>fail-fast</i>, meaning that any structural
 * modification, except for <code>remove()</code> called on the iterator
 * itself, cause the iterator to throw a{
@link ConcurrentModificationException}
 rather than exhibit
 * non-deterministic behavior.
 * @author Jon A. Zeppieri
 * @author Bryce McKinlay
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see Collection
 * @see List
 * @see LinkedList
 * @see Vector
 * @see Collections#synchronizedList(List)
 * @see AbstractList
 * @status updated to 1.4
 */
public class ArrayList extends AbstractList implements List, RandomAccess,
        Cloneable, Serializable, Rollbackable {

    /**
     * Compatible with JDK 1.2
     */
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * The default capacity for new ArrayLists.
     */
    private static final int DEFAULT_CAPACITY = 16;

    /**
     * The number of elements in this list.
     * @serial the list size
     */
    private int size;

    /**
     * Where the data is stored.
     */
    private transient Object[] data;

    /**
     * Construct a new ArrayList with the supplied initial capacity.
     * @param capacity initial capacity of this ArrayList
     * @exception IllegalArgumentException if capacity is negative
     */
    public ArrayList(int capacity) {
        // Must explicitly check, to get correct exception.
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        $ASSIGN$data(new Object[capacity]);
    }

    /**
     * Construct a new ArrayList with the default capacity (16).
     */
    public ArrayList() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Construct a new ArrayList, and initialize it with the elements
     * in the supplied Collection. The initial capacity is 110% of the
     * Collection's size.
     * @param c the collection whose elements will initialize this list
     * @exception NullPointerException if c is null
     */
    public ArrayList(Collection c) {
        this((int) (c.size() * 1.1f));
        addAll(c);
    }

    /**
     * Trims the capacity of this List to be equal to its size;
     * a memory saver.
     */
    public void trimToSize() {
        // Not a structural change from the perspective of iterators on this list,
        // so don't update modCount.
        if (size != data.length) {
            Object[] newData = new Object[size];
            System.arraycopy($BACKUP$data(), 0, newData, 0, size);
            $ASSIGN$data(newData);
        }
    }

    /**
     * Guarantees that this list will have at least enough capacity to
     * hold minCapacity elements. This implementation will grow the list to
     * max(current * 2, minCapacity) if (minCapacity &gt; current). The JCL says
     * explictly that "this method increases its capacity to minCap", while
     * the JDK 1.3 online docs specify that the list will grow to at least the
     * size specified.
     * @param minCapacity the minimum guaranteed capacity
     */
    public void ensureCapacity(int minCapacity) {
        int current = data.length;
        if (minCapacity > current) {
            Object[] newData = new Object[Math.max(current * 2, minCapacity)];
            System.arraycopy($BACKUP$data(), 0, newData, 0, size);
            $ASSIGN$data(newData);
        }
    }

    /**
     * Returns the number of elements in this list.
     * @return the list size
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the list is empty.
     * @return true if there are no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns true iff element is in this ArrayList.
     * @param e the element whose inclusion in the List is being tested
     * @return true if the list contains e
     */
    public boolean contains(Object e) {
        return indexOf(e) != -1;
    }

    /**
     * Returns the lowest index at which element appears in this List, or
     * -1 if it does not appear.
     * @param e the element whose inclusion in the List is being tested
     * @return the index where e was found
     */
    public int indexOf(Object e) {
        for (int i = 0; i < size; i++) {
            if (equals(e, data[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the highest index at which element appears in this List, or
     * -1 if it does not appear.
     * @param e the element whose inclusion in the List is being tested
     * @return the index where e was found
     */
    public int lastIndexOf(Object e) {
        for (int i = size - 1; i >= 0; i--) {
            if (equals(e, data[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Creates a shallow copy of this ArrayList (elements are not cloned).
     * @return the cloned object
     */
    public Object clone() {
        ArrayList clone = null;
        try {
            clone = (ArrayList) super.clone();
            clone.$ASSIGN$data(data.clone());
        } catch (CloneNotSupportedException e) {
            // Impossible to get here.
        }
        return clone;
    }

    /**
     * Returns an Object array containing all of the elements in this ArrayList.
     * The array is independent of this list.
     * @return an array representation of this list
     */
    public Object[] toArray() {
        Object[] array = new Object[size];
        System.arraycopy($BACKUP$data(), 0, array, 0, size);
        return array;
    }

    /**
     * Returns an Array whose component type is the runtime component type of
     * the passed-in Array.  The returned Array is populated with all of the
     * elements in this ArrayList.  If the passed-in Array is not large enough
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
        if (a.length < size) {
            a = (Object[]) Array.newInstance(a.getClass().getComponentType(),
                    size);
        } else if (a.length > size) {
            a[size] = null;
        }
        System.arraycopy($BACKUP$data(), 0, a, 0, size);
        return a;
    }

    /**
     * Retrieves the element at the user-supplied index.
     * @param index the index of the element we are fetching
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
     */
    public Object get(int index) {
        checkBoundExclusive(index);
        return data[index];
    }

    /**
     * Sets the element at the specified index.  The new element, e,
     * can be an object of any type or null.
     * @param index the index at which the element is being set
     * @param e the element to be set
     * @return the element previously at the specified index
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt;= 0
     */
    public Object set(int index, Object e) {
        checkBoundExclusive(index);
        Object result = data[index];
        $ASSIGN$data(index, e);
        return result;
    }

    /**
     * Appends the supplied element to the end of this list.
     * The element, e, can be an object of any type or null.
     * @param e the element to be appended to this list
     * @return true, the add will always succeed
     */
    public boolean add(Object e) {
        setModCount(getModCount() + 1);
        if (size == data.length) {
            ensureCapacity(size + 1);
        }
        $ASSIGN$data($ASSIGN$SPECIAL$size(11, size), e);
        return true;
    }

    /**
     * Adds the supplied element at the specified index, shifting all
     * elements currently at that index or higher one to the right.
     * The element, e, can be an object of any type or null.
     * @param index the index at which the element is being added
     * @param e the item being added
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt; size()
     */
    public void add(int index, Object e) {
        checkBoundInclusive(index);
        setModCount(getModCount() + 1);
        if (size == data.length) {
            ensureCapacity(size + 1);
        }
        if (index != size) {
            System.arraycopy($BACKUP$data(), index, $BACKUP$data(), index + 1,
                    size - index);
        }
        $ASSIGN$data(index, e);
        $ASSIGN$SPECIAL$size(11, size);
    }

    /**
     * Removes the element at the user-supplied index.
     * @param index the index of the element to be removed
     * @return the removed Object
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt;= size()
     */
    public Object remove(int index) {
        checkBoundExclusive(index);
        Object r = data[index];
        setModCount(getModCount() + 1);
        if (index != $ASSIGN$SPECIAL$size(14, size)) {
            System.arraycopy($BACKUP$data(), index + 1, $BACKUP$data(), index,
                    size - index);
        }
        // Aid for garbage collection by releasing this pointer.
        $ASSIGN$data(size, null);
        return r;
    }

    /**
     * Removes all elements from this List
     */
    public void clear() {
        if (size > 0) {
            setModCount(getModCount() + 1);
            // Allow for garbage collection.
            Arrays.fill($BACKUP$data(), 0, size, null);
            $ASSIGN$size(0);
        }
    }

    /**
     * Add each element in the supplied Collection to this List. It is undefined
     * what happens if you modify the list while this is taking place; for
     * example, if the collection contains this list.  c can contain objects
     * of any type, as well as null values.
     * @param c a Collection containing elements to be added to this List
     * @return true if the list was modified, in other words c is not empty
     * @exception NullPointerException if c is null
     */
    public boolean addAll(Collection c) {
        return addAll(size, c);
    }

    /**
     * Add all elements in the supplied collection, inserting them beginning
     * at the specified index.  c can contain objects of any type, as well
     * as null values.
     * @param index the index at which the elements will be inserted
     * @param c the Collection containing the elements to be inserted
     * @exception IndexOutOfBoundsException if index &lt; 0 || index &gt; 0
     * @exception NullPointerException if c is null
     */
    public boolean addAll(int index, Collection c) {
        checkBoundInclusive(index);
        Iterator itr = c.iterator();
        int csize = c.size();
        setModCount(getModCount() + 1);
        if (csize + size > data.length) {
            ensureCapacity(size + csize);
        }
        int end = index + csize;
        if (size > 0 && index != size) {
            System.arraycopy($BACKUP$data(), index, $BACKUP$data(), end, size
                    - index);
        }
        $ASSIGN$SPECIAL$size(0, csize);
        for (; index < end; index++) {
            $ASSIGN$data(index, itr.next());
        }
        return csize > 0;
    }

    /**
     * Removes all elements in the half-open interval [fromIndex, toIndex).
     * Does nothing when toIndex is equal to fromIndex.
     * @param fromIndex the first index which will be removed
     * @param toIndex one greater than the last index which will be removed
     * @exception IndexOutOfBoundsException if fromIndex &gt; toIndex
     */
    protected void removeRange(int fromIndex, int toIndex) {
        int change = toIndex - fromIndex;
        if (change > 0) {
            setModCount(getModCount() + 1);
            System.arraycopy($BACKUP$data(), toIndex, $BACKUP$data(),
                    fromIndex, size - toIndex);
            $ASSIGN$SPECIAL$size(1, change);
        } else if (change < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Checks that the index is in the range of possible elements (inclusive).
     * @param index the index to check
     * @exception IndexOutOfBoundsException if index &gt; size
     */
    private void checkBoundInclusive(int index) {
        // Implementation note: we do not check for negative ranges here, since
        // use of a negative index will cause an ArrayIndexOutOfBoundsException,
        // a subclass of the required exception, with no effort on our part.
        if (index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);
        }
    }

    /**
     * Checks that the index is in the range of existing elements (exclusive).
     * @param index the index to check
     * @exception IndexOutOfBoundsException if index &gt;= size
     */
    private void checkBoundExclusive(int index) {
        // Implementation note: we do not check for negative ranges here, since
        // use of a negative index will cause an ArrayIndexOutOfBoundsException,
        // a subclass of the required exception, with no effort on our part.
        if (index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);
        }
    }

    /**
     * Remove from this list all elements contained in the given collection.
     * This is not public, due to Sun's API, but this performs in linear
     * time while the default behavior of AbstractList would be quadratic.
     * @param c the collection to filter out
     * @return true if this list changed
     * @exception NullPointerException if c is null
     */
    boolean removeAllInternal(Collection c) {
        int i;
        int j;
        for (i = 0; i < size; i++) {
            if (c.contains(data[i])) {
                break;
            }
        }
        if (i == size) {
            return false;
        }
        setModCount(getModCount() + 1);
        for (j = i++; i < size; i++) {
            if (!c.contains(data[i])) {
                $ASSIGN$data(j++, data[i]);
            }
        }
        $ASSIGN$SPECIAL$size(1, i - j);
        return true;
    }

    /**
     * Retain in this vector only the elements contained in the given collection.
     * This is not public, due to Sun's API, but this performs in linear
     * time while the default behavior of AbstractList would be quadratic.
     * @param c the collection to filter by
     * @return true if this vector changed
     * @exception NullPointerException if c is null
     * @since 1.2
     */
    boolean retainAllInternal(Collection c) {
        int i;
        int j;
        for (i = 0; i < size; i++) {
            if (!c.contains(data[i])) {
                break;
            }
        }
        if (i == size) {
            return false;
        }
        setModCount(getModCount() + 1);
        for (j = i++; i < size; i++) {
            if (c.contains(data[i])) {
                $ASSIGN$data(j++, data[i]);
            }
        }
        $ASSIGN$SPECIAL$size(1, i - j);
        return true;
    }

    /**
     * Serializes this object to the given stream.
     * @param s the stream to write to
     * @exception IOException if the underlying stream fails
     * @serialData the size field (int), the length of the backing array
     * (int), followed by its elements (Objects) in proper order.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        // The 'size' field.
        s.defaultWriteObject();
        // We serialize unused list entries to preserve capacity.
        int len = data.length;
        s.writeInt(len);
        // it would be more efficient to just write "size" items,
        // this need readObject read "size" items too.
        for (int i = 0; i < size; i++) {
            s.writeObject(data[i]);
        }
    }

    /**
     * Deserializes this object from the given stream.
     * @param s the stream to read from
     * @exception ClassNotFoundException if the underlying stream fails
     * @exception IOException if the underlying stream fails
     * @serialData the size field (int), the length of the backing array
     * (int), followed by its elements (Objects) in proper order.
     */
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        // the `size' field.
        s.defaultReadObject();
        int capacity = s.readInt();
        $ASSIGN$data(new Object[capacity]);
        for (int i = 0; i < size; i++) {
            $ASSIGN$data(i, s.readObject());
        }
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

    private final Object[] $ASSIGN$data(Object[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$data.add(null, data, $CHECKPOINT.getTimestamp());
        }
        return data = newValue;
    }

    private final Object $ASSIGN$data(int index0, Object newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$data.add(new int[] { index0 }, data[index0], $CHECKPOINT
                    .getTimestamp());
        }
        return data[index0] = newValue;
    }

    private final Object[] $BACKUP$data() {
        $RECORD$data.backup(null, data, $CHECKPOINT.getTimestamp());
        return data;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                .getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        size = $RECORD$size.restore(size, timestamp, trim);
        data = (Object[]) $RECORD$data.restore(data, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private transient FieldRecord $RECORD$size = new FieldRecord(0);

    private transient FieldRecord $RECORD$data = new FieldRecord(1);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$size, $RECORD$data };

}
