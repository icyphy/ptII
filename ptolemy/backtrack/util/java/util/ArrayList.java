package ptolemy.backtrack.util.java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.Object;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.RandomAccess;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

public class ArrayList extends AbstractList implements List, RandomAccess, Cloneable, Serializable, Rollbackable {

    private static final long serialVersionUID = 8683452581122892189L;

    private static final int DEFAULT_CAPACITY = 16;

    private int size;

    private transient Object[] data;

    public ArrayList(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        $ASSIGN$data(new Object[capacity]);
    }

    public ArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public ArrayList(Collection c) {
        this((int)(c.size() * 1.1f));
        addAll(c);
    }

    public void trimToSize() {
        if (size != data.length) {
            Object[] newData = new Object[size];
            System.arraycopy($BACKUP$data(), 0, newData, 0, size);
            $ASSIGN$data(newData);
        }
    }

    public void ensureCapacity(int minCapacity) {
        int current = data.length;
        if (minCapacity > current) {
            Object[] newData = new Object[Math.max(current * 2, minCapacity)];
            System.arraycopy($BACKUP$data(), 0, newData, 0, size);
            $ASSIGN$data(newData);
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object e) {
        return indexOf(e) != -1;
    }

    public int indexOf(Object e) {
        for (int i = 0; i < size; i++) 
            if (equals(e, data[i]))
                return i;
        return -1;
    }

    public int lastIndexOf(Object e) {
        for (int i = size - 1; i >= 0; i--) 
            if (equals(e, data[i]))
                return i;
        return -1;
    }

    public Object clone() {
        ArrayList clone = null;
        try {
            clone = (ArrayList)super.clone();
            clone.$ASSIGN$data((Object[])data.clone());
        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }

    public Object[] toArray() {
        Object[] array = new Object[size];
        System.arraycopy($BACKUP$data(), 0, array, 0, size);
        return array;
    }

    public Object[] toArray(Object[] a) {
        if (a.length < size)
            a = (Object[])Array.newInstance(a.getClass().getComponentType(), size);
        else if (a.length > size)
            a[size] = null;
        System.arraycopy($BACKUP$data(), 0, a, 0, size);
        return a;
    }

    public Object get(int index) {
        checkBoundExclusive(index);
        return data[index];
    }

    public Object set(int index, Object e) {
        checkBoundExclusive(index);
        Object result = data[index];
        $ASSIGN$data(index, e);
        return result;
    }

    public boolean add(Object e) {
        setModCount(getModCount() + 1);
        if (size == data.length)
            ensureCapacity(size + 1);
        $ASSIGN$data($ASSIGN$SPECIAL$size(11, size), e);
        return true;
    }

    public void add(int index, Object e) {
        checkBoundInclusive(index);
        setModCount(getModCount() + 1);
        if (size == data.length)
            ensureCapacity(size + 1);
        if (index != size)
            System.arraycopy($BACKUP$data(), index, $BACKUP$data(), index + 1, size - index);
        $ASSIGN$data(index, e);
        $ASSIGN$SPECIAL$size(11, size);
    }

    public Object remove(int index) {
        checkBoundExclusive(index);
        Object r = data[index];
        setModCount(getModCount() + 1);
        if (index != $ASSIGN$SPECIAL$size(14, size))
            System.arraycopy($BACKUP$data(), index + 1, $BACKUP$data(), index, size - index);
        $ASSIGN$data(size, null);
        return r;
    }

    public void clear() {
        if (size > 0) {
            setModCount(getModCount() + 1);
            Arrays.fill($BACKUP$data(), 0, size, null);
            $ASSIGN$size(0);
        }
    }

    public boolean addAll(Collection c) {
        return addAll(size, c);
    }

    public boolean addAll(int index, Collection c) {
        checkBoundInclusive(index);
        Iterator itr = c.iterator();
        int csize = c.size();
        setModCount(getModCount() + 1);
        if (csize + size > data.length)
            ensureCapacity(size + csize);
        int end = index + csize;
        if (size > 0 && index != size)
            System.arraycopy($BACKUP$data(), index, $BACKUP$data(), end, size - index);
        $ASSIGN$SPECIAL$size(0, csize);
        for (; index < end; index++) 
            $ASSIGN$data(index, itr.next());
        return csize > 0;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        int change = toIndex - fromIndex;
        if (change > 0) {
            setModCount(getModCount() + 1);
            System.arraycopy($BACKUP$data(), toIndex, $BACKUP$data(), fromIndex, size - toIndex);
            $ASSIGN$SPECIAL$size(1, change);
        } else if (change < 0)
            throw new IndexOutOfBoundsException();
    }

    private void checkBoundInclusive(int index) {
        if (index > size)
            throw new IndexOutOfBoundsException("Index: " + index+", Size: "+size);
    }

    private void checkBoundExclusive(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index: " + index+", Size: "+size);
    }

    boolean removeAllInternal(Collection c) {
        int i;
        int j;
        for (i = 0; i < size; i++) 
            if (c.contains(data[i]))
                break;
        if (i == size)
            return false;
        setModCount(getModCount() + 1);
        for (j = i++; i < size; i++) 
            if (!c.contains(data[i]))
                $ASSIGN$data(j++, data[i]);
        $ASSIGN$SPECIAL$size(1, i - j);
        return true;
    }

    boolean retainAllInternal(Collection c) {
        int i;
        int j;
        for (i = 0; i < size; i++) 
            if (!c.contains(data[i]))
                break;
        if (i == size)
            return false;
        setModCount(getModCount() + 1);
        for (j = i++; i < size; i++) 
            if (c.contains(data[i]))
                $ASSIGN$data(j++, data[i]);
        $ASSIGN$SPECIAL$size(1, i - j);
        return true;
    }

    private void writeObject(ObjectOutputStream s) throws IOException  {
        s.defaultWriteObject();
        int len = data.length;
        s.writeInt(len);
        for (int i = 0; i < size; i++) 
            s.writeObject(data[i]);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
        s.defaultReadObject();
        int capacity = s.readInt();
        $ASSIGN$data(new Object[capacity]);
        for (int i = 0; i < size; i++) 
            $ASSIGN$data(i, s.readObject());
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
            $RECORD$data.add(new int[] {
                    index0
                }, data[index0], $CHECKPOINT.getTimestamp());
        }
        return data[index0] = newValue;
    }

    private final Object[] $BACKUP$data() {
        $RECORD$data.backup(null, data, $CHECKPOINT.getTimestamp());
        return data;
    }

    public void $RESTORE(long timestamp, boolean trim) {
        size = $RECORD$size.restore(size, timestamp, trim);
        data = (Object[])$RECORD$data.restore(data, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$size = new FieldRecord(0);

    private FieldRecord $RECORD$data = new FieldRecord(1);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$size,
            $RECORD$data
        };
}
