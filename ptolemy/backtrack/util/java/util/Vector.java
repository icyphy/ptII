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

import java.io.Serializable;
import java.lang.Object;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

public class Vector extends AbstractList implements List, RandomAccess, Cloneable, Serializable, Rollbackable {

    private static final long serialVersionUID = -2767605614048989439L;

    private Object[] elementData;

    private int elementCount;

    private int capacityIncrement;

    public Vector() {
        this(10, 0);
    }

    public Vector(Collection c) {
        setElementCount(c.size());
        setElementData(c.toArray(new Object[getElementCount()]));
    }

    public Vector(int initialCapacity, int capacityIncrement) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException();
        setElementData(new Object[initialCapacity]);
        this.$ASSIGN$capacityIncrement(capacityIncrement);
    }

    public Vector(int initialCapacity) {
        this(initialCapacity, 0);
    }

    public synchronized void copyInto(Object[] a) {
        System.arraycopy(getElementData(), 0, a, 0, getElementCount());
    }

    public synchronized void trimToSize() {
        Object[] newArray = new Object[getElementCount()];
        System.arraycopy(getElementData(), 0, newArray, 0, getElementCount());
        setElementData(newArray);
    }

    public synchronized void ensureCapacity(int minCapacity) {
        if (getElementData().length >= minCapacity)
            return;
        int newCapacity;
        if (capacityIncrement <= 0)
            newCapacity = getElementData().length * 2;
        else
            newCapacity = getElementData().length + capacityIncrement;
        Object[] newArray = new Object[Math.max(newCapacity, minCapacity)];
        System.arraycopy(getElementData(), 0, newArray, 0, getElementCount());
        setElementData(newArray);
    }

    public synchronized void setSize(int newSize) {
        setModCount(getModCount() + 1);
        ensureCapacity(newSize);
        if (newSize < getElementCount())
            Arrays.fill(getElementData(), newSize, getElementCount(), null);
        setElementCount(newSize);
    }

    public synchronized int capacity() {
        return getElementData().length;
    }

    public synchronized int size() {
        return getElementCount();
    }

    public synchronized boolean isEmpty() {
        return getElementCount() == 0;
    }

    public Enumeration elements() {
        return new Enumeration() {

                private int i = 0;

                public boolean hasMoreElements() {
                    return i < elementCount;
                }

                public Object nextElement() {
                    if (i >= elementCount)
                        throw new NoSuchElementException();
                    return elementData[$ASSIGN$SPECIAL$i(11, i)];
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

                private final int $ASSIGN$SPECIAL$i(int operator, long newValue) {
                    if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                        $RECORD$i.add(null, i, $CHECKPOINT.getTimestamp());
                    }
                    switch (operator) {
                    case 0:
                        return i += newValue;
                    case 1:
                        return i -= newValue;
                    case 2:
                        return i *= newValue;
                    case 3:
                        return i /= newValue;
                    case 4:
                        return i &= newValue;
                    case 5:
                        return i |= newValue;
                    case 6:
                        return i ^= newValue;
                    case 7:
                        return i %= newValue;
                    case 8:
                        return i <<= newValue;
                    case 9:
                        return i >>= newValue;
                    case 10:
                        return i >>>= newValue;
                    case 11:
                        return i++;
                    case 12:
                        return i--;
                    case 13:
                        return ++i;
                    case 14:
                        return --i;
                    default:
                        return i;
                    }
                }

                public void $COMMIT_ANONYMOUS(long timestamp) {
                    FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                    $RECORD$$CHECKPOINT.commit(timestamp);
                }

                public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                    i = $RECORD$i.restore(i, timestamp, trim);
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

                private FieldRecord[] $RECORDS = new FieldRecord[] {
                    $RECORD$i
                };

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }
            };
    }

    public boolean contains(Object elem) {
        return indexOf(elem, 0) >= 0;
    }

    public int indexOf(Object elem) {
        return indexOf(elem, 0);
    }

    public synchronized int indexOf(Object e, int index) {
        for (int i = index; i < getElementCount(); i++)
            if (equals(e, getElementData()[i]))
                return i;
        return -1;
    }

    public int lastIndexOf(Object elem) {
        return lastIndexOf(elem, getElementCount() - 1);
    }

    public synchronized int lastIndexOf(Object e, int index) {
        checkBoundExclusive(index);
        for (int i = index; i >= 0; i--)
            if (equals(e, getElementData()[i]))
                return i;
        return -1;
    }

    public synchronized Object elementAt(int index) {
        checkBoundExclusive(index);
        return getElementData()[index];
    }

    public synchronized Object firstElement() {
        if (getElementCount() == 0)
            throw new NoSuchElementException();
        return getElementData()[0];
    }

    public synchronized Object lastElement() {
        if (getElementCount() == 0)
            throw new NoSuchElementException();
        return getElementData()[getElementCount() - 1];
    }

    public void setElementAt(Object obj, int index) {
        set(index, obj);
    }

    public void removeElementAt(int index) {
        remove(index);
    }

    public synchronized void insertElementAt(Object obj, int index) {
        checkBoundInclusive(index);
        if (getElementCount() == getElementData().length)
            ensureCapacity(getElementCount() + 1);
        setModCount(getModCount() + 1);
        System.arraycopy(getElementData(), index, getElementData(), index + 1, getElementCount() - index);
        setElementCount(getElementCount() + 1);
        getElementData()[index] = obj;
    }

    public synchronized void addElement(Object obj) {
        if (getElementCount() == getElementData().length)
            ensureCapacity(getElementCount() + 1);
        setModCount(getModCount() + 1);
        getElementData()[$ASSIGN$SPECIAL$elementCount(11, elementCount)] = obj;
    }

    public synchronized boolean removeElement(Object obj) {
        int idx = indexOf(obj, 0);
        if (idx >= 0) {
            remove(idx);
            return true;
        }
        return false;
    }

    public synchronized void removeAllElements() {
        if (getElementCount() == 0)
            return;
        setModCount(getModCount() + 1);
        Arrays.fill(getElementData(), 0, getElementCount(), null);
        setElementCount(0);
    }

    public synchronized Object clone() {
        try {
            Vector clone = (Vector)super.clone();
            clone.setElementData((Object[])getElementData().clone());
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex.toString());
        }
    }

    public synchronized Object[] toArray() {
        Object[] newArray = new Object[getElementCount()];
        copyInto(newArray);
        return newArray;
    }

    public synchronized Object[] toArray(Object[] a) {
        if (a.length < getElementCount())
            a = (Object[])Array.newInstance(a.getClass().getComponentType(), getElementCount());
        else if (a.length > getElementCount())
            a[getElementCount()] = null;
        System.arraycopy(getElementData(), 0, a, 0, getElementCount());
        return a;
    }

    public Object get(int index) {
        return elementAt(index);
    }

    public synchronized Object set(int index, Object element) {
        checkBoundExclusive(index);
        Object temp = getElementData()[index];
        getElementData()[index] = element;
        return temp;
    }

    public boolean add(Object o) {
        addElement(o);
        return true;
    }

    public boolean remove(Object o) {
        return removeElement(o);
    }

    public void add(int index, Object element) {
        insertElementAt(element, index);
    }

    public synchronized Object remove(int index) {
        checkBoundExclusive(index);
        Object temp = getElementData()[index];
        setModCount(getModCount() + 1);
        setElementCount(getElementCount() - 1);
        if (index < getElementCount())
            System.arraycopy(getElementData(), index + 1, getElementData(), index, getElementCount() - index);
        getElementData()[getElementCount()] = null;
        return temp;
    }

    public void clear() {
        removeAllElements();
    }

    public synchronized boolean containsAll(Collection c) {
        return super.containsAll(c);
    }

    public synchronized boolean addAll(Collection c) {
        return addAll(getElementCount(), c);
    }

    public synchronized boolean removeAll(Collection c) {
        if (c == null)
            throw new NullPointerException();
        int i;
        int j;
        for (i = 0; i < getElementCount(); i++)
            if (c.contains(getElementData()[i]))
                break;
        if (i == getElementCount())
            return false;
        setModCount(getModCount() + 1);
        for (j = i++; i < getElementCount(); i++)
            if (!c.contains(getElementData()[i]))
                getElementData()[j++] = getElementData()[i];
        setElementCount(getElementCount() - (i - j));
        return true;
    }

    public synchronized boolean retainAll(Collection c) {
        if (c == null)
            throw new NullPointerException();
        int i;
        int j;
        for (i = 0; i < getElementCount(); i++)
            if (!c.contains(getElementData()[i]))
                break;
        if (i == getElementCount())
            return false;
        setModCount(getModCount() + 1);
        for (j = i++; i < getElementCount(); i++)
            if (c.contains(getElementData()[i]))
                getElementData()[j++] = getElementData()[i];
        setElementCount(getElementCount() - (i - j));
        return true;
    }

    public synchronized boolean addAll(int index, Collection c) {
        checkBoundInclusive(index);
        Iterator itr = c.iterator();
        int csize = c.size();
        setModCount(getModCount() + 1);
        ensureCapacity(getElementCount() + csize);
        int end = index + csize;
        if (getElementCount() > 0 && index != getElementCount())
            System.arraycopy(getElementData(), index, getElementData(), end, getElementCount() - index);
        setElementCount(getElementCount() + csize);
        for (; index < end; index++)
            getElementData()[index] = itr.next();
        return (csize > 0);
    }

    public synchronized boolean equals(Object o) {
        return super.equals(o);
    }

    public synchronized int hashCode() {
        return super.hashCode();
    }

    public synchronized String toString() {
        return super.toString();
    }

    public synchronized List subList(int fromIndex, int toIndex) {
        List sub = super.subList(fromIndex, toIndex);
        return new Collections.SynchronizedList(this, sub);
    }

    protected void removeRange(int fromIndex, int toIndex) {
        int change = toIndex - fromIndex;
        if (change > 0) {
            setModCount(getModCount() + 1);
            System.arraycopy(getElementData(), toIndex, getElementData(), fromIndex, getElementCount() - toIndex);
            int save = getElementCount();
            setElementCount(getElementCount() - change);
            Arrays.fill(getElementData(), getElementCount(), save, null);
        } else if (change < 0)
            throw new IndexOutOfBoundsException();
    }

    private void checkBoundInclusive(int index) {
        if (index > getElementCount())
            throw new ArrayIndexOutOfBoundsException(index + " > "+getElementCount());
    }

    private void checkBoundExclusive(int index) {
        if (index >= getElementCount())
            throw new ArrayIndexOutOfBoundsException(index + " >= "+getElementCount());
    }

    protected void setCapacityIncrement(int capacityIncrement) {
        this.$ASSIGN$capacityIncrement(capacityIncrement);
    }

    protected int getCapacityIncrement() {
        return capacityIncrement;
    }

    protected void setElementCount(int elementCount) {
        this.$ASSIGN$elementCount(elementCount);
    }

    protected int getElementCount() {
        return elementCount;
    }

    protected void setElementData(Object[] elementData) {
        this.$ASSIGN$elementData(elementData);
    }

    protected Object[] getElementData() {
        return $BACKUP$elementData();
    }

    private final Object[] $ASSIGN$elementData(Object[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$elementData.add(null, elementData, $CHECKPOINT.getTimestamp());
        }
        return elementData = newValue;
    }

    private final Object[] $BACKUP$elementData() {
        $RECORD$elementData.backup(null, elementData, $CHECKPOINT.getTimestamp());
        return elementData;
    }

    private final int $ASSIGN$elementCount(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$elementCount.add(null, elementCount, $CHECKPOINT.getTimestamp());
        }
        return elementCount = newValue;
    }

    private final int $ASSIGN$SPECIAL$elementCount(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$elementCount.add(null, elementCount, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
        case 0:
            return elementCount += newValue;
        case 1:
            return elementCount -= newValue;
        case 2:
            return elementCount *= newValue;
        case 3:
            return elementCount /= newValue;
        case 4:
            return elementCount &= newValue;
        case 5:
            return elementCount |= newValue;
        case 6:
            return elementCount ^= newValue;
        case 7:
            return elementCount %= newValue;
        case 8:
            return elementCount <<= newValue;
        case 9:
            return elementCount >>= newValue;
        case 10:
            return elementCount >>>= newValue;
        case 11:
            return elementCount++;
        case 12:
            return elementCount--;
        case 13:
            return ++elementCount;
        case 14:
            return --elementCount;
        default:
            return elementCount;
        }
    }

    private final int $ASSIGN$capacityIncrement(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$capacityIncrement.add(null, capacityIncrement, $CHECKPOINT.getTimestamp());
        }
        return capacityIncrement = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        elementData = (Object[])$RECORD$elementData.restore(elementData, timestamp, trim);
        elementCount = $RECORD$elementCount.restore(elementCount, timestamp, trim);
        capacityIncrement = $RECORD$capacityIncrement.restore(capacityIncrement, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$elementData = new FieldRecord(1);

    private FieldRecord $RECORD$elementCount = new FieldRecord(0);

    private FieldRecord $RECORD$capacityIncrement = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
        $RECORD$elementData,
        $RECORD$elementCount,
        $RECORD$capacityIncrement
    };
}
