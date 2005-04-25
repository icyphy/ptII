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
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.Collection;
import ptolemy.backtrack.util.java.util.Set;

public class Hashtable extends Dictionary implements Map, Cloneable, Serializable, Rollbackable {

    protected Checkpoint $CHECKPOINT = new Checkpoint(this);

    private static final int DEFAULT_CAPACITY = 11;

    static final int KEYS = 0, VALUES = 1, ENTRIES = 2;

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static final long serialVersionUID = 1421746759512286392L;

    private int threshold;

    private final float loadFactor;

    private transient HashEntry[] buckets;

    private transient int modCount;

    private transient int size;

    private transient Set keys;

    private transient Collection values;

    private transient Set entries;

    private static final class HashEntry extends AbstractMap.BasicMapEntry implements Rollbackable {

        private HashEntry next;

        HashEntry(Object key, Object value) {
            super(key, value);
        }

        public Object setValue(Object newVal) {
            if (newVal == null)
                throw new NullPointerException();
            return super.setValue(newVal);
        }

        void setNext(HashEntry next) {
            this.$ASSIGN$next(next);
        }

        HashEntry getNext() {
            return next;
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
            super.$COMMIT(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            next = (HashEntry)$RECORD$next.restore(next, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$next
            };
    }

    private final class HashIterator implements Iterator, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        final int type;

        private int knownMod = modCount;

        private int count = getSize();

        private int idx = buckets.length;

        private HashEntry last;

        private HashEntry next;

        HashIterator(int type) {
            this.type = type;
        }

        public boolean hasNext() {
            if (getKnownMod() != modCount)
                throw new ConcurrentModificationException();
            return getCount() > 0;
        }

        public Object next() {
            if (getKnownMod() != modCount)
                throw new ConcurrentModificationException();
            if (getCount() == 0)
                throw new NoSuchElementException();
            setCount(getCount() - 1);
            HashEntry e = getNext();
            while (e == null)
                e = buckets[setIdx(getIdx() - 1)];
            setNext(e.getNext());
            setLast(e);
            if (type == VALUES)
                return e.getValue();
            if (type == KEYS)
                return e.getKey();
            return e;
        }

        public void remove() {
            if (getKnownMod() != modCount)
                throw new ConcurrentModificationException();
            if (getLast() == null)
                throw new IllegalStateException();
            Hashtable.this.remove(getLast().getKey());
            setLast(null);
            setKnownMod(getKnownMod() + 1);
        }

        void setKnownMod(int knownMod) {
            this.$ASSIGN$knownMod(knownMod);
        }

        int getKnownMod() {
            return knownMod;
        }

        void setCount(int count) {
            this.$ASSIGN$count(count);
        }

        int getCount() {
            return count;
        }

        int setIdx(int idx) {
            return this.$ASSIGN$idx(idx);
        }

        int getIdx() {
            return idx;
        }

        void setLast(HashEntry last) {
            this.$ASSIGN$last(last);
        }

        HashEntry getLast() {
            return last;
        }

        void setNext(HashEntry next) {
            this.$ASSIGN$next(next);
        }

        HashEntry getNext() {
            return next;
        }

        private final int $ASSIGN$knownMod(int newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$knownMod.add(null, knownMod, $CHECKPOINT.getTimestamp());
            }
            return knownMod = newValue;
        }

        private final int $ASSIGN$count(int newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$count.add(null, count, $CHECKPOINT.getTimestamp());
            }
            return count = newValue;
        }

        private final int $ASSIGN$idx(int newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$idx.add(null, idx, $CHECKPOINT.getTimestamp());
            }
            return idx = newValue;
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

        private FieldRecord $RECORD$knownMod = new FieldRecord(0);

        private FieldRecord $RECORD$count = new FieldRecord(0);

        private FieldRecord $RECORD$idx = new FieldRecord(0);

        private FieldRecord $RECORD$last = new FieldRecord(0);

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$knownMod,
                $RECORD$count,
                $RECORD$idx,
                $RECORD$last,
                $RECORD$next
            };
    }

    private final class Enumerator implements Enumeration, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        final int type;

        private int count = getSize();

        private int idx = buckets.length;

        private HashEntry next;

        Enumerator(int type) {
            this.type = type;
        }

        public boolean hasMoreElements() {
            return getCount() > 0;
        }

        public Object nextElement() {
            if (getCount() == 0)
                throw new NoSuchElementException("Hashtable Enumerator");
            setCount(getCount() - 1);
            HashEntry e = getNext();
            while (e == null)
                e = buckets[setIdx(getIdx() - 1)];
            setNext(e.getNext());
            return type == VALUES?e.getValue():e.getKey();
        }

        void setCount(int count) {
            this.$ASSIGN$count(count);
        }

        int getCount() {
            return count;
        }

        int setIdx(int idx) {
            return this.$ASSIGN$idx(idx);
        }

        int getIdx() {
            return idx;
        }

        void setNext(HashEntry next) {
            this.$ASSIGN$next(next);
        }

        HashEntry getNext() {
            return next;
        }

        private final int $ASSIGN$count(int newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$count.add(null, count, $CHECKPOINT.getTimestamp());
            }
            return count = newValue;
        }

        private final int $ASSIGN$idx(int newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$idx.add(null, idx, $CHECKPOINT.getTimestamp());
            }
            return idx = newValue;
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
            count = $RECORD$count.restore(count, timestamp, trim);
            idx = $RECORD$idx.restore(idx, timestamp, trim);
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

        private FieldRecord $RECORD$count = new FieldRecord(0);

        private FieldRecord $RECORD$idx = new FieldRecord(0);

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$count,
                $RECORD$idx,
                $RECORD$next
            };
    }

    public Hashtable() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public Hashtable(Map m) {
        this(Math.max(m.size() * 2, DEFAULT_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    public Hashtable(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public Hashtable(int initialCapacity, float loadFactor) {
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

    public synchronized int size() {
        return getSize();
    }

    public synchronized boolean isEmpty() {
        return getSize() == 0;
    }

    public Enumeration keys() {
        return new Enumerator(KEYS);
    }

    public Enumeration elements() {
        return new Enumerator(VALUES);
    }

    public synchronized boolean contains(Object value) {
        for (int i = buckets.length - 1; i >= 0; i--) {
            HashEntry e = buckets[i];
            while (e != null) {
                if (value.equals(e.getValue()))
                    return true;
                e = e.getNext();
            }
        }
        if (value == null)
            throw new NullPointerException();
        return false;
    }

    public boolean containsValue(Object value) {
        return contains(value);
    }

    public synchronized boolean containsKey(Object key) {
        int idx = hash(key);
        HashEntry e = buckets[idx];
        while (e != null) {
            if (key.equals(e.getKey()))
                return true;
            e = e.getNext();
        }
        return false;
    }

    public synchronized Object get(Object key) {
        int idx = hash(key);
        HashEntry e = buckets[idx];
        while (e != null) {
            if (key.equals(e.getKey()))
                return e.getValue();
            e = e.getNext();
        }
        return null;
    }

    public synchronized Object put(Object key, Object value) {
        int idx = hash(key);
        HashEntry e = buckets[idx];
        if (value == null)
            throw new NullPointerException();
        while (e != null) {
            if (key.equals(e.getKey())) {
                Object r = e.getValue();
                e.setValue(value);
                return r;
            } else {
                e = e.getNext();
            }
        }
        $ASSIGN$SPECIAL$modCount(11, modCount);
        if (setSize(getSize() + 1) > threshold) {
            rehash();
            idx = hash(key);
        }
        e = new HashEntry(key, value);
        e.setNext(buckets[idx]);
        $ASSIGN$buckets(idx, e);
        return null;
    }

    public synchronized Object remove(Object key) {
        int idx = hash(key);
        HashEntry e = buckets[idx];
        HashEntry last = null;
        while (e != null) {
            if (key.equals(e.getKey())) {
                $ASSIGN$SPECIAL$modCount(11, modCount);
                if (last == null)
                    $ASSIGN$buckets(idx, e.getNext());
                else
                    last.setNext(e.getNext());
                setSize(getSize() - 1);
                return e.getValue();
            }
            last = e;
            e = e.getNext();
        }
        return null;
    }

    public synchronized void putAll(Map m) {
        Iterator itr = m.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry e = (Map.Entry)itr.next();
            if (e instanceof AbstractMap.BasicMapEntry) {
                AbstractMap.BasicMapEntry entry = (AbstractMap.BasicMapEntry)e;
                put(entry.getKey(), entry.getValue());
            } else {
                put(e.getKey(), e.getValue());
            }
        }
    }

    public synchronized void clear() {
        if (getSize() > 0) {
            $ASSIGN$SPECIAL$modCount(11, modCount);
            Arrays.fill($BACKUP$buckets(), null);
            setSize(0);
        }
    }

    public synchronized Object clone() {
        Hashtable copy = null;
        try {
            copy = (Hashtable)super.clone();
        } catch (CloneNotSupportedException x) {
        }
        copy.$ASSIGN$buckets(new HashEntry[buckets.length]);
        copy.putAllInternal(this);
        copy.$ASSIGN$keys(null);
        copy.$ASSIGN$values(null);
        copy.$ASSIGN$entries(null);
        return copy;
    }

    public synchronized String toString() {
        Iterator entries = new HashIterator(ENTRIES);
        StringBuffer r = new StringBuffer("{");
        for (int pos = getSize(); pos > 0; pos--) {
            r.append(entries.next());
            if (pos > 1)
                r.append(", ");
        }
        r.append("}");
        return r.toString();
    }

    public Set keySet() {
        if (keys == null) {
            Set r = new AbstractSet() {

                public int size() {
                    return size;
                }

                public Iterator iterator() {
                    return new HashIterator(KEYS);
                }

                public void clear() {
                    Hashtable.this.clear();
                }

                public boolean contains(Object o) {
                    if (o == null)
                        return false;
                    return containsKey(o);
                }

                public boolean remove(Object o) {
                    return Hashtable.this.remove(o) != null;
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
            };
            $ASSIGN$keys(new Collections.SynchronizedSet(this, r));
        }
        return keys;
    }

    public Collection values() {
        if (values == null) {
            Collection r = new AbstractCollection() {

                public int size() {
                    return size;
                }

                public Iterator iterator() {
                    return new HashIterator(VALUES);
                }

                public void clear() {
                    Hashtable.this.clear();
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
            };
            $ASSIGN$values(new Collections.SynchronizedCollection(this, r));
        }
        return values;
    }

    public Set entrySet() {
        if (entries == null) {
            Set r = new AbstractSet() {

                public int size() {
                    return size;
                }

                public Iterator iterator() {
                    return new HashIterator(ENTRIES);
                }

                public void clear() {
                    Hashtable.this.clear();
                }

                public boolean contains(Object o) {
                    return getEntry(o) != null;
                }

                public boolean remove(Object o) {
                    HashEntry e = getEntry(o);
                    if (e != null) {
                        Hashtable.this.remove(e.getKey());
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
            };
            $ASSIGN$entries(new Collections.SynchronizedSet(this, r));
        }
        return entries;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map))
            return false;
        return entrySet().equals(((Map)o).entrySet());
    }

    public synchronized int hashCode() {
        Iterator itr = new HashIterator(ENTRIES);
        int hashcode = 0;
        for (int pos = getSize(); pos > 0; pos--)
            hashcode += itr.next().hashCode();
        return hashcode;
    }

    private int hash(Object key) {
        int hash = key.hashCode() % buckets.length;
        return hash < 0?-hash:hash;
    }

    HashEntry getEntry(Object o) {
        if (!(o instanceof Map.Entry))
            return null;
        Object key = ((Map.Entry)o).getKey();
        if (key == null)
            return null;
        int idx = hash(key);
        HashEntry e = buckets[idx];
        while (e != null) {
            if (o.equals(e))
                return e;
            e = e.getNext();
        }
        return null;
    }

    void putAllInternal(Map m) {
        Iterator itr = m.entrySet().iterator();
        setSize(0);
        while (itr.hasNext()) {
            setSize(getSize() + 1);
            Map.Entry e = (Map.Entry)itr.next();
            Object key = e.getKey();
            int idx = hash(key);
            HashEntry he = new HashEntry(key, e.getValue());
            he.setNext(buckets[idx]);
            $ASSIGN$buckets(idx, he);
        }
    }

    protected void rehash() {
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
                    while (dest.getNext() != null)
                        dest = dest.getNext();
                    dest.setNext(e);
                } else {
                    $ASSIGN$buckets(idx, e);
                }
                HashEntry next = e.getNext();
                e.setNext(null);
                e = next;
            }
        }
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException  {
        s.defaultWriteObject();
        s.writeInt(buckets.length);
        s.writeInt(getSize());
        Iterator it = new HashIterator(ENTRIES);
        while (it.hasNext()) {
            HashEntry entry = (HashEntry)it.next();
            s.writeObject(entry.getKey());
            s.writeObject(entry.getKey());
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
        s.defaultReadObject();
        $ASSIGN$buckets(new HashEntry[s.readInt()]);
        int len = s.readInt();
        while (--len >= 0)
            put(s.readObject(), s.readObject());
    }

    void setBuckets(HashEntry[] buckets) {
        this.$ASSIGN$buckets(buckets);
    }

    HashEntry[] getBuckets() {
        return $BACKUP$buckets();
    }

    void setModCount(int modCount) {
        this.$ASSIGN$modCount(modCount);
    }

    int getModCount() {
        return modCount;
    }

    int setSize(int size) {
        return this.$ASSIGN$size(size);
    }

    int getSize() {
        return size;
    }

    private final int $ASSIGN$threshold(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$threshold.add(null, threshold, $CHECKPOINT.getTimestamp());
        }
        return threshold = newValue;
    }

    private final HashEntry[] $ASSIGN$buckets(HashEntry[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$buckets.add(null, buckets, $CHECKPOINT.getTimestamp());
        }
        return buckets = newValue;
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

    private final Set $ASSIGN$keys(Set newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$keys.add(null, keys, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return keys = newValue;
    }

    private final Collection $ASSIGN$values(Collection newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$values.add(null, values, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return values = newValue;
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
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        threshold = $RECORD$threshold.restore(threshold, timestamp, trim);
        buckets = (HashEntry[])$RECORD$buckets.restore(buckets, timestamp, trim);
        modCount = $RECORD$modCount.restore(modCount, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        keys = (Set)$RECORD$keys.restore(keys, timestamp, trim);
        values = (Collection)$RECORD$values.restore(values, timestamp, trim);
        entries = (Set)$RECORD$entries.restore(entries, timestamp, trim);
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

    private FieldRecord $RECORD$threshold = new FieldRecord(0);

    private FieldRecord $RECORD$loadFactor = new FieldRecord(0);

    private FieldRecord $RECORD$buckets = new FieldRecord(1);

    private FieldRecord $RECORD$modCount = new FieldRecord(0);

    private FieldRecord $RECORD$size = new FieldRecord(0);

    private FieldRecord $RECORD$keys = new FieldRecord(0);

    private FieldRecord $RECORD$values = new FieldRecord(0);

    private FieldRecord $RECORD$entries = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$threshold,
            $RECORD$loadFactor,
            $RECORD$buckets,
            $RECORD$modCount,
            $RECORD$size,
            $RECORD$keys,
            $RECORD$values,
            $RECORD$entries
        };
}
