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
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.Set;

public class HashMap extends AbstractMap implements Map, Cloneable, Serializable, Rollbackable {

    static final int DEFAULT_CAPACITY = 11;

    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static final long serialVersionUID = 362498820763181265L;

    private int threshold;

    final float loadFactor;

    private transient HashEntry[] buckets;

    private transient int modCount;

    private transient int size;

    private transient Set entries;

    static class HashEntry extends AbstractMap.BasicMapEntry implements Rollbackable {

        HashEntry next;

        HashEntry(Object key, Object value) {
            super(key, value);
        }

        void access() {
        }

        Object cleanup() {
            return getValue();
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

    private final class HashIterator implements Iterator, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private final int type;

        private int knownMod = modCount;

        private int count = size;

        private int idx = buckets.length;

        private HashEntry last;

        private HashEntry next;

        HashIterator(int type) {
            this.type = type;
        }

        public boolean hasNext() {
            if (knownMod != modCount)
                throw new ConcurrentModificationException();
            return count > 0;
        }

        public Object next() {
            if (knownMod != modCount)
                throw new ConcurrentModificationException();
            if (count == 0)
                throw new NoSuchElementException();
            $ASSIGN$SPECIAL$count(12, count);
            HashEntry e = next;
            while (e == null)
                e = buckets[$ASSIGN$SPECIAL$idx(14, idx)];
            $ASSIGN$next(e.next);
            $ASSIGN$last(e);
            if (type == VALUES)
                return e.getValue();
            if (type == KEYS)
                return e.getKey();
            return e;
        }

        public void remove() {
            if (knownMod != modCount)
                throw new ConcurrentModificationException();
            if (last == null)
                throw new IllegalStateException();
            HashMap.this.remove(last.getKey());
            $ASSIGN$last(null);
            $ASSIGN$SPECIAL$knownMod(11, knownMod);
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

        private final int $ASSIGN$SPECIAL$count(int operator, long newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$count.add(null, count, $CHECKPOINT.getTimestamp());
            }
            switch (operator) {
                case 0:
                    return count += newValue;
                case 1:
                    return count -= newValue;
                case 2:
                    return count *= newValue;
                case 3:
                    return count /= newValue;
                case 4:
                    return count &= newValue;
                case 5:
                    return count |= newValue;
                case 6:
                    return count ^= newValue;
                case 7:
                    return count %= newValue;
                case 8:
                    return count <<= newValue;
                case 9:
                    return count >>= newValue;
                case 10:
                    return count >>>= newValue;
                case 11:
                    return count++;
                case 12:
                    return count--;
                case 13:
                    return ++count;
                case 14:
                    return --count;
                default:
                    return count;
            }
        }

        private final int $ASSIGN$SPECIAL$idx(int operator, long newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$idx.add(null, idx, $CHECKPOINT.getTimestamp());
            }
            switch (operator) {
                case 0:
                    return idx += newValue;
                case 1:
                    return idx -= newValue;
                case 2:
                    return idx *= newValue;
                case 3:
                    return idx /= newValue;
                case 4:
                    return idx &= newValue;
                case 5:
                    return idx |= newValue;
                case 6:
                    return idx ^= newValue;
                case 7:
                    return idx %= newValue;
                case 8:
                    return idx <<= newValue;
                case 9:
                    return idx >>= newValue;
                case 10:
                    return idx >>>= newValue;
                case 11:
                    return idx++;
                case 12:
                    return idx--;
                case 13:
                    return ++idx;
                case 14:
                    return --idx;
                default:
                    return idx;
            }
        }

        private final HashEntry $ASSIGN$last(HashEntry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$last.add(null, last, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return last = newValue;
        }

        private final HashEntry $ASSIGN$next(HashEntry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$next.add(null, next, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return next = newValue;
        }

        public void $COMMIT(long timestamp) {
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
            $RECORD$$CHECKPOINT.commit(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
            count = $RECORD$count.restore(count, timestamp, trim);
            idx = $RECORD$idx.restore(idx, timestamp, trim);
            last = (HashEntry)$RECORD$last.restore(last, timestamp, trim);
            next = (HashEntry)$RECORD$next.restore(next, timestamp, trim);
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

        private FieldRecord $RECORD$type = new FieldRecord(0);

        private FieldRecord $RECORD$knownMod = new FieldRecord(0);

        private FieldRecord $RECORD$count = new FieldRecord(0);

        private FieldRecord $RECORD$idx = new FieldRecord(0);

        private FieldRecord $RECORD$last = new FieldRecord(0);

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$type,
                $RECORD$knownMod,
                $RECORD$count,
                $RECORD$idx,
                $RECORD$last,
                $RECORD$next
            };
    }

    protected HashEntry[] getBuckets() {
        return $BACKUP$buckets();
    }

    protected HashEntry getBucket(int index) {
        return buckets[index];
    }

    protected void setBucket(int index, HashEntry entry) {
        $ASSIGN$buckets(index, entry);
    }

    protected int getModCount() {
        return modCount;
    }

    protected void setModCount(int modCount) {
        this.$ASSIGN$modCount(modCount);
    }

    protected int getSize() {
        return size;
    }

    public HashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public HashMap(Map m) {
        this(Math.max(m.size() * 2, DEFAULT_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        if (!(loadFactor > 0))
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        if (initialCapacity == 0)
            initialCapacity = 1;
        $ASSIGN$buckets(new HashEntry[initialCapacity]);
        this.loadFactor = loadFactor;
        $ASSIGN$threshold((int)(initialCapacity * loadFactor));
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Object get(Object key) {
        int idx = hash(key);
        HashEntry e = buckets[idx];
        while (e != null) {
            if (equals(key, e.getKey()))
                return e.getValue();
            e = e.next;
        }
        return null;
    }

    public boolean containsKey(Object key) {
        int idx = hash(key);
        HashEntry e = buckets[idx];
        while (e != null) {
            if (equals(key, e.getKey()))
                return true;
            e = e.next;
        }
        return false;
    }

    public Object put(Object key, Object value) {
        int idx = hash(key);
        HashEntry e = buckets[idx];
        while (e != null) {
            if (equals(key, e.getKey())) {
                e.access();
                Object r = e.getValue();
                e.setValue(value);
                return r;
            } else
                e = e.next;
        }
        $ASSIGN$SPECIAL$modCount(11, modCount);
        if ($ASSIGN$SPECIAL$size(13, size) > threshold) {
            rehash();
            idx = hash(key);
        }
        addEntry(key, value, idx, true);
        return null;
    }

    public void putAll(Map m) {
        Iterator itr = m.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry e = (Map.Entry)itr.next();
            if (e instanceof AbstractMap.BasicMapEntry) {
                AbstractMap.BasicMapEntry entry = (AbstractMap.BasicMapEntry)e;
                put(entry.getKey(), entry.getValue());
            } else
                put(e.getKey(), e.getValue());
        }
    }

    public Object remove(Object key) {
        int idx = hash(key);
        HashEntry e = buckets[idx];
        HashEntry last = null;
        while (e != null) {
            if (equals(key, e.getKey())) {
                $ASSIGN$SPECIAL$modCount(11, modCount);
                if (last == null)
                    $ASSIGN$buckets(idx, e.next);
                else
                    last.next = e.next;
                $ASSIGN$SPECIAL$size(12, size);
                return e.cleanup();
            }
            last = e;
            e = e.next;
        }
        return null;
    }

    public void clear() {
        if (size != 0) {
            $ASSIGN$SPECIAL$modCount(11, modCount);
            Arrays.fill($BACKUP$buckets(), null);
            $ASSIGN$size(0);
        }
    }

    public boolean containsValue(Object value) {
        for (int i = buckets.length - 1; i >= 0; i--) {
            HashEntry e = buckets[i];
            while (e != null) {
                if (equals(value, e.getValue()))
                    return true;
                e = e.next;
            }
        }
        return false;
    }

    public Object clone() {
        HashMap copy = null;
        try {
            copy = (HashMap)super.clone();
        } catch (CloneNotSupportedException x) {
        }
        copy.$ASSIGN$buckets(new HashEntry[buckets.length]);
        copy.putAllInternal(this);
        copy.$ASSIGN$entries(null);
        return copy;
    }

    public Set keySet() {
        if (getKeys() == null)
            setKeys(new AbstractSet() {

                public int size() {
                    return size;
                }

                public Iterator iterator() {
                    return HashMap.this.iterator(KEYS);
                }

                public void clear() {
                    HashMap.this.clear();
                }

                public boolean contains(Object o) {
                    return containsKey(o);
                }

                public boolean remove(Object o) {
                    int oldsize = size;
                    HashMap.this.remove(o);
                    return oldsize != size;
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

                public void $COMMIT_ANONYMOUS(long timestamp) {
                    FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                    super.$COMMIT(timestamp);
                }

                public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                    super.$RESTORE(timestamp, trim);
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

                private FieldRecord[] $RECORDS = new FieldRecord[] {
                    };

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }
            });
        return getKeys();
    }

    public Collection values() {
        if (getValues() == null)
            setValues(new AbstractCollection() {

                public int size() {
                    return size;
                }

                public Iterator iterator() {
                    return HashMap.this.iterator(VALUES);
                }

                public void clear() {
                    HashMap.this.clear();
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

                public void $COMMIT_ANONYMOUS(long timestamp) {
                    FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                    super.$COMMIT(timestamp);
                }

                public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                    super.$RESTORE(timestamp, trim);
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

                private FieldRecord[] $RECORDS = new FieldRecord[] {
                    };

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }
            });
        return getValues();
    }

    public Set entrySet() {
        if (entries == null)
            $ASSIGN$entries(new AbstractSet() {

                public int size() {
                    return size;
                }

                public Iterator iterator() {
                    return HashMap.this.iterator(ENTRIES);
                }

                public void clear() {
                    HashMap.this.clear();
                }

                public boolean contains(Object o) {
                    return getEntry(o) != null;
                }

                public boolean remove(Object o) {
                    HashEntry e = getEntry(o);
                    if (e != null) {
                        HashMap.this.remove(e.getKey());
                        return true;
                    }
                    return false;
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

                public void $COMMIT_ANONYMOUS(long timestamp) {
                    FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                    super.$COMMIT(timestamp);
                }

                public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                    super.$RESTORE(timestamp, trim);
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

                private FieldRecord[] $RECORDS = new FieldRecord[] {
                    };

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }
            });
        return entries;
    }

    void addEntry(Object key, Object value, int idx, boolean callRemove) {
        HashEntry e = new HashEntry(key, value);
        e.next = buckets[idx];
        $ASSIGN$buckets(idx, e);
    }

    final HashEntry getEntry(Object o) {
        if (!(o instanceof Map.Entry))
            return null;
        Map.Entry me = (Map.Entry)o;
        Object key = me.getKey();
        int idx = hash(key);
        HashEntry e = buckets[idx];
        while (e != null) {
            if (equals(e.getKey(), key))
                return equals(e.getValue(), me.getValue())?e:null;
            e = e.next;
        }
        return null;
    }

    final int hash(Object key) {
        return key == null?0:Math.abs(key.hashCode() % buckets.length);
    }

    Iterator iterator(int type) {
        return new HashIterator(type);
    }

    void putAllInternal(Map m) {
        Iterator itr = m.entrySet().iterator();
        $ASSIGN$size(0);
        while (itr.hasNext()) {
            $ASSIGN$SPECIAL$size(11, size);
            Map.Entry e = (Map.Entry)itr.next();
            Object key = e.getKey();
            int idx = hash(key);
            addEntry(key, e.getValue(), idx, false);
        }
    }

    private void rehash() {
        HashEntry[] oldBuckets = $BACKUP$buckets();
        int newcapacity = (buckets.length * 2) + 1;
        $ASSIGN$threshold((int)(newcapacity * loadFactor));
        $ASSIGN$buckets(new HashEntry[newcapacity]);
        for (int i = oldBuckets.length - 1; i >= 0; i--) {
            HashEntry e = oldBuckets[i];
            while (e != null) {
                int idx = hash(e.getKey());
                HashEntry dest = buckets[idx];
                if (dest != null) {
                    while (dest.next != null)
                        dest = dest.next;
                    dest.next = e;
                } else
                    $ASSIGN$buckets(idx, e);
                HashEntry next = e.next;
                e.next = null;
                e = next;
            }
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException  {
        s.defaultWriteObject();
        s.writeInt(buckets.length);
        s.writeInt(size);
        Iterator it = iterator(ENTRIES);
        while (it.hasNext()) {
            HashEntry entry = (HashEntry)it.next();
            s.writeObject(entry.getKey());
            s.writeObject(entry.getValue());
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
        s.defaultReadObject();
        $ASSIGN$buckets(new HashEntry[s.readInt()]);
        int len = s.readInt();
        $ASSIGN$size(len);
        while (len-- > 0) {
            Object key = s.readObject();
            addEntry(key, s.readObject(), hash(key), false);
        }
    }

    private final int $ASSIGN$threshold(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$threshold.add(null, threshold, $CHECKPOINT.getTimestamp());
        }
        return threshold = newValue;
    }

    private final HashEntry $ASSIGN$buckets(int index0, HashEntry newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$buckets.add(new int[] {
                    index0
                }, buckets[index0], $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return buckets[index0] = newValue;
    }

    private final HashEntry[] $ASSIGN$buckets(HashEntry[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$buckets.add(null, buckets, $CHECKPOINT.getTimestamp());
        }
        return buckets = newValue;
    }

    private final HashEntry[] $BACKUP$buckets() {
        $RECORD$buckets.backup(null, buckets, $CHECKPOINT.getTimestamp());
        return buckets;
    }

    private final int $ASSIGN$modCount(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$modCount.add(null, modCount, $CHECKPOINT.getTimestamp());
        }
        return modCount = newValue;
    }

    private final int $ASSIGN$SPECIAL$modCount(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$modCount.add(null, modCount, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return modCount += newValue;
            case 1:
                return modCount -= newValue;
            case 2:
                return modCount *= newValue;
            case 3:
                return modCount /= newValue;
            case 4:
                return modCount &= newValue;
            case 5:
                return modCount |= newValue;
            case 6:
                return modCount ^= newValue;
            case 7:
                return modCount %= newValue;
            case 8:
                return modCount <<= newValue;
            case 9:
                return modCount >>= newValue;
            case 10:
                return modCount >>>= newValue;
            case 11:
                return modCount++;
            case 12:
                return modCount--;
            case 13:
                return ++modCount;
            case 14:
                return --modCount;
            default:
                return modCount;
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

    private final Set $ASSIGN$entries(Set newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$entries.add(null, entries, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return entries = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        threshold = $RECORD$threshold.restore(threshold, timestamp, trim);
        buckets = (HashEntry[])$RECORD$buckets.restore(buckets, timestamp, trim);
        modCount = $RECORD$modCount.restore(modCount, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        entries = (Set)$RECORD$entries.restore(entries, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$threshold = new FieldRecord(0);

    private FieldRecord $RECORD$buckets = new FieldRecord(1);

    private FieldRecord $RECORD$modCount = new FieldRecord(0);

    private FieldRecord $RECORD$size = new FieldRecord(0);

    private FieldRecord $RECORD$entries = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$threshold,
            $RECORD$buckets,
            $RECORD$modCount,
            $RECORD$size,
            $RECORD$entries
        };
}
