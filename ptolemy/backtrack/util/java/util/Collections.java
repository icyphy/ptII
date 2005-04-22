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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.Collection;
import ptolemy.backtrack.util.java.util.Set;
import ptolemy.backtrack.util.java.util.SortedSet;

public class Collections implements Rollbackable {

    protected Checkpoint $CHECKPOINT = new Checkpoint(this);

    private static final int LARGE_LIST_SIZE = 16;

    public static final Set EMPTY_SET = new EmptySet();

    public static final List EMPTY_LIST = new EmptyList();

    public static final Map EMPTY_MAP = new EmptyMap();

    private static final ReverseComparator rcInstance = new ReverseComparator();

    private static Random defaultRandom = null;

    private static final class EmptySet extends AbstractSet implements Serializable, Rollbackable {

        private static final long serialVersionUID = 1582296315990362920L;

        EmptySet() {
        }

        public int size() {
            return 0;
        }

        public Iterator iterator() {
            return EMPTY_LIST.iterator();
        }

        public boolean contains(Object o) {
            return false;
        }

        public boolean containsAll(Collection c) {
            return c.isEmpty();
        }

        public boolean equals(Object o) {
            return o instanceof Set && ((Set)o).isEmpty();
        }

        public int hashCode() {
            return 0;
        }

        public boolean remove(Object o) {
            return false;
        }

        public boolean removeAll(Collection c) {
            return false;
        }

        public boolean retainAll(Collection c) {
            return false;
        }

        public Object[] toArray() {
            return new Object[0];
        }

        public Object[] toArray(Object[] a) {
            if (a.length > 0)
                a[0] = null;
            return a;
        }

        public String toString() {
            return "[]";
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static final class EmptyList extends AbstractList implements Serializable, RandomAccess, Rollbackable {

        private static final long serialVersionUID = 8842843931221139166L;

        EmptyList() {
        }

        public int size() {
            return 0;
        }

        public Object get(int index) {
            throw new IndexOutOfBoundsException();
        }

        public boolean contains(Object o) {
            return false;
        }

        public boolean containsAll(Collection c) {
            return c.isEmpty();
        }

        public boolean equals(Object o) {
            return o instanceof List && ((List)o).isEmpty();
        }

        public int hashCode() {
            return 1;
        }

        public int indexOf(Object o) {
            return -1;
        }

        public int lastIndexOf(Object o) {
            return -1;
        }

        public boolean remove(Object o) {
            return false;
        }

        public boolean removeAll(Collection c) {
            return false;
        }

        public boolean retainAll(Collection c) {
            return false;
        }

        public Object[] toArray() {
            return new Object[0];
        }

        public Object[] toArray(Object[] a) {
            if (a.length > 0)
                a[0] = null;
            return a;
        }

        public String toString() {
            return "[]";
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static final class EmptyMap extends AbstractMap implements Serializable, Rollbackable {

        private static final long serialVersionUID = 6428348081105594320L;

        EmptyMap() {
        }

        public Set entrySet() {
            return EMPTY_SET;
        }

        public boolean containsKey(Object key) {
            return false;
        }

        public boolean containsValue(Object value) {
            return false;
        }

        public boolean equals(Object o) {
            return o instanceof Map && ((Map)o).isEmpty();
        }

        public Object get(Object o) {
            return null;
        }

        public int hashCode() {
            return 0;
        }

        public Set keySet() {
            return EMPTY_SET;
        }

        public Object remove(Object o) {
            return null;
        }

        public int size() {
            return 0;
        }

        public Collection values() {
            return EMPTY_SET;
        }

        public String toString() {
            return "[]";
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static final class CopiesList extends AbstractList implements Serializable, RandomAccess, Rollbackable {

        private static final long serialVersionUID = 2739099268398711800L;

        private final int n;

        private final Object element;

        CopiesList(int n, Object o) {
            if (n < 0)
                throw new IllegalArgumentException();
            this.n = n;
            element = o;
        }

        public int size() {
            return n;
        }

        public Object get(int index) {
            if (index < 0 || index >= n)
                throw new IndexOutOfBoundsException();
            return element;
        }

        public boolean contains(Object o) {
            return n > 0 && equals(o, element);
        }

        public int indexOf(Object o) {
            return (n > 0 && equals(o, element))?0:-1;
        }

        public int lastIndexOf(Object o) {
            return equals(o, element)?n - 1:-1;
        }

        public List subList(int from, int to) {
            if (from < 0 || to > n)
                throw new IndexOutOfBoundsException();
            return new CopiesList(to - from, element);
        }

        public Object[] toArray() {
            Object[] a = new Object[n];
            Arrays.fill(a, element);
            return a;
        }

        public String toString() {
            StringBuffer r = new StringBuffer("{");
            for (int i = n - 1; --i > 0; ) 
                r.append(element).append(", ");
            r.append(element).append("}");
            return r.toString();
        }

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$element.restore(element, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$n = new FieldRecord(0);

        private FieldRecord $RECORD$element = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$n,
                $RECORD$element
            };
    }

    private static final class ReverseComparator implements Comparator, Serializable, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private static final long serialVersionUID = 7207038068494060240L;

        ReverseComparator() {
        }

        public int compare(Object a, Object b) {
            return ((Comparable)b).compareTo(a);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
                FieldRecord.popState($RECORDS);
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

    private static final class SingletonSet extends AbstractSet implements Serializable, Rollbackable {

        private static final long serialVersionUID = 3193687207550431679L;

        final Object element;

        SingletonSet(Object o) {
            element = o;
        }

        public int size() {
            return 1;
        }

        public Iterator iterator() {
            return new Iterator() {

                private boolean hasNext = true;

                public boolean hasNext() {
                    return hasNext;
                }

                public Object next() {
                    if (hasNext) {
                        $ASSIGN$hasNext(false);
                        return element;
                    } else
                        throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                final class _PROXY_ implements Rollbackable {

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

                private final boolean $ASSIGN$hasNext(boolean newValue) {
                    if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                        $RECORD$hasNext.add(null, hasNext, $CHECKPOINT.getTimestamp());
                    }
                    return hasNext = newValue;
                }

                public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                    hasNext = $RECORD$hasNext.restore(hasNext, timestamp, trim);
                    if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                        $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, new _PROXY_(), timestamp, trim);
                        FieldRecord.popState($RECORDS);
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

                private FieldRecord $RECORD$hasNext = new FieldRecord(0);

                private FieldRecord[] $RECORDS = new FieldRecord[] {
                        $RECORD$hasNext
                    };

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }
            };
        }

        public boolean contains(Object o) {
            return equals(o, element);
        }

        public boolean containsAll(Collection c) {
            Iterator i = c.iterator();
            int pos = c.size();
            while (--pos >= 0) 
                if (!equals(i.next(), element))
                    return false;
            return true;
        }

        public int hashCode() {
            return hashCode(element);
        }

        public Object[] toArray() {
            return new Object[] {
                    element
                };
        }

        public String toString() {
            return "[" + element+"]";
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static final class SingletonList extends AbstractList implements Serializable, RandomAccess, Rollbackable {

        private static final long serialVersionUID = 3093736618740652951L;

        private final Object element;

        SingletonList(Object o) {
            element = o;
        }

        public int size() {
            return 1;
        }

        public Object get(int index) {
            if (index == 0)
                return element;
            throw new IndexOutOfBoundsException();
        }

        public boolean contains(Object o) {
            return equals(o, element);
        }

        public boolean containsAll(Collection c) {
            Iterator i = c.iterator();
            int pos = c.size();
            while (--pos >= 0) 
                if (!equals(i.next(), element))
                    return false;
            return true;
        }

        public int hashCode() {
            return 31 + hashCode(element);
        }

        public int indexOf(Object o) {
            return equals(o, element)?0:-1;
        }

        public int lastIndexOf(Object o) {
            return equals(o, element)?0:-1;
        }

        public List subList(int from, int to) {
            if (from == to && (to == 0 || to == 1))
                return EMPTY_LIST;
            if (from == 0 && to == 1)
                return this;
            if (from > to)
                throw new IllegalArgumentException();
            throw new IndexOutOfBoundsException();
        }

        public Object[] toArray() {
            return new Object[] {
                    element
                };
        }

        public String toString() {
            return "[" + element+"]";
        }

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$element.restore(element, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$element = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$element
            };
    }

    private static final class SingletonMap extends AbstractMap implements Serializable, Rollbackable {

        private static final long serialVersionUID = -6979724477215052911L;

        private final Object k;

        private final Object v;

        private transient Set entries;

        SingletonMap(Object key, Object value) {
            k = key;
            v = value;
        }

        public Set entrySet() {
            if (entries == null)
                $ASSIGN$entries(singleton(new AbstractMap.BasicMapEntry(k, v) {

                    public Object setValue(Object o) {
                        throw new UnsupportedOperationException();
                    }

                    final class _PROXY_ implements Rollbackable {

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
                }));
            return entries;
        }

        public boolean containsKey(Object key) {
            return equals(key, k);
        }

        public boolean containsValue(Object value) {
            return equals(value, v);
        }

        public Object get(Object key) {
            return equals(key, k)?v:null;
        }

        public int hashCode() {
            return hashCode(k) ^ hashCode(v);
        }

        public Set keySet() {
            if (getKeys() == null)
                setKeys(singleton(k));
            return getKeys();
        }

        public int size() {
            return 1;
        }

        public Collection values() {
            if (getValues() == null)
                setValues(singleton(v));
            return getValues();
        }

        public String toString() {
            return "{" + k+"="+v+"}";
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

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$k.restore(k, timestamp, trim);
            $RECORD$v.restore(v, timestamp, trim);
            entries = (Set)$RECORD$entries.restore(entries, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$k = new FieldRecord(0);

        private FieldRecord $RECORD$v = new FieldRecord(0);

        private FieldRecord $RECORD$entries = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$k,
                $RECORD$v,
                $RECORD$entries
            };
    }

    static class SynchronizedCollection implements Collection, Serializable, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private static final long serialVersionUID = 3053995032091335093L;

        final Collection c;

        final Object mutex;

        SynchronizedCollection(Collection c) {
            this.c = c;
            mutex = this;
            if (c == null)
                throw new NullPointerException();
        }

        SynchronizedCollection(Object sync, Collection c) {
            this.c = c;
            mutex = sync;
        }

        public boolean add(Object o) {
            synchronized (mutex) {
                return c.add(o);
            }
        }

        public boolean addAll(Collection col) {
            synchronized (mutex) {
                return c.addAll(col);
            }
        }

        public void clear() {
            synchronized (mutex) {
                c.clear();
            }
        }

        public boolean contains(Object o) {
            synchronized (mutex) {
                return c.contains(o);
            }
        }

        public boolean containsAll(Collection c1) {
            synchronized (mutex) {
                return c.containsAll(c1);
            }
        }

        public boolean isEmpty() {
            synchronized (mutex) {
                return c.isEmpty();
            }
        }

        public Iterator iterator() {
            synchronized (mutex) {
                return new SynchronizedIterator(mutex, c.iterator());
            }
        }

        public boolean remove(Object o) {
            synchronized (mutex) {
                return c.remove(o);
            }
        }

        public boolean removeAll(Collection col) {
            synchronized (mutex) {
                return c.removeAll(col);
            }
        }

        public boolean retainAll(Collection col) {
            synchronized (mutex) {
                return c.retainAll(col);
            }
        }

        public int size() {
            synchronized (mutex) {
                return c.size();
            }
        }

        public Object[] toArray() {
            synchronized (mutex) {
                return c.toArray();
            }
        }

        public Object[] toArray(Object[] a) {
            synchronized (mutex) {
                return c.toArray(a);
            }
        }

        public String toString() {
            synchronized (mutex) {
                return c.toString();
            }
        }

        public void $RESTORE(long timestamp, boolean trim) {
            if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
                FieldRecord.popState($RECORDS);
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

    private static class SynchronizedIterator implements Iterator, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        final Object mutex;

        private final Iterator i;

        SynchronizedIterator(Object sync, Iterator i) {
            this.i = i;
            mutex = sync;
        }

        public Object next() {
            synchronized (mutex) {
                return i.next();
            }
        }

        public boolean hasNext() {
            synchronized (mutex) {
                return i.hasNext();
            }
        }

        public void remove() {
            synchronized (mutex) {
                i.remove();
            }
        }

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$i.restore(i, timestamp, trim);
            if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
                FieldRecord.popState($RECORDS);
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

        private FieldRecord $RECORD$i = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$i
            };
    }

    static class SynchronizedList extends SynchronizedCollection implements List, Rollbackable {

        private static final long serialVersionUID = -7754090372962971524L;

        final List list;

        SynchronizedList(List l) {
            super(l);
            list = l;
        }

        SynchronizedList(Object sync, List l) {
            super(sync, l);
            list = l;
        }

        public void add(int index, Object o) {
            synchronized (mutex) {
                list.add(index, o);
            }
        }

        public boolean addAll(int index, Collection c) {
            synchronized (mutex) {
                return list.addAll(index, c);
            }
        }

        public boolean equals(Object o) {
            synchronized (mutex) {
                return list.equals(o);
            }
        }

        public Object get(int index) {
            synchronized (mutex) {
                return list.get(index);
            }
        }

        public int hashCode() {
            synchronized (mutex) {
                return list.hashCode();
            }
        }

        public int indexOf(Object o) {
            synchronized (mutex) {
                return list.indexOf(o);
            }
        }

        public int lastIndexOf(Object o) {
            synchronized (mutex) {
                return list.lastIndexOf(o);
            }
        }

        public ListIterator listIterator() {
            synchronized (mutex) {
                return new SynchronizedListIterator(mutex, list.listIterator());
            }
        }

        public ListIterator listIterator(int index) {
            synchronized (mutex) {
                return new SynchronizedListIterator(mutex, list.listIterator(index));
            }
        }

        public Object remove(int index) {
            synchronized (mutex) {
                return list.remove(index);
            }
        }

        public Object set(int index, Object o) {
            synchronized (mutex) {
                return list.set(index, o);
            }
        }

        public List subList(int fromIndex, int toIndex) {
            synchronized (mutex) {
                return new SynchronizedList(mutex, list.subList(fromIndex, toIndex));
            }
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static final class SynchronizedRandomAccessList extends SynchronizedList implements RandomAccess, Rollbackable {

        private static final long serialVersionUID = 1530674583602358482L;

        SynchronizedRandomAccessList(List l) {
            super(l);
        }

        SynchronizedRandomAccessList(Object sync, List l) {
            super(sync, l);
        }

        public List subList(int fromIndex, int toIndex) {
            synchronized (mutex) {
                return new SynchronizedRandomAccessList(mutex, list.subList(fromIndex, toIndex));
            }
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static final class SynchronizedListIterator extends SynchronizedIterator implements ListIterator, Rollbackable {

        private final ListIterator li;

        SynchronizedListIterator(Object sync, ListIterator li) {
            super(sync, li);
            this.li = li;
        }

        public void add(Object o) {
            synchronized (mutex) {
                li.add(o);
            }
        }

        public boolean hasPrevious() {
            synchronized (mutex) {
                return li.hasPrevious();
            }
        }

        public int nextIndex() {
            synchronized (mutex) {
                return li.nextIndex();
            }
        }

        public Object previous() {
            synchronized (mutex) {
                return li.previous();
            }
        }

        public int previousIndex() {
            synchronized (mutex) {
                return li.previousIndex();
            }
        }

        public void set(Object o) {
            synchronized (mutex) {
                li.set(o);
            }
        }

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$li.restore(li, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$li = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$li
            };
    }

    private static class SynchronizedMap implements Map, Serializable, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private static final long serialVersionUID = 1978198479659022715L;

        private final Map m;

        final Object mutex;

        private transient Set entries;

        private transient Set keys;

        private transient Collection values;

        SynchronizedMap(Map m) {
            this.m = m;
            mutex = this;
            if (m == null)
                throw new NullPointerException();
        }

        SynchronizedMap(Object sync, Map m) {
            this.m = m;
            mutex = sync;
        }

        public void clear() {
            synchronized (mutex) {
                m.clear();
            }
        }

        public boolean containsKey(Object key) {
            synchronized (mutex) {
                return m.containsKey(key);
            }
        }

        public boolean containsValue(Object value) {
            synchronized (mutex) {
                return m.containsValue(value);
            }
        }

        public Set entrySet() {

            class SynchronizedMapEntry implements Map.Entry, Rollbackable {

                protected Checkpoint $CHECKPOINT = new Checkpoint(this);

                final Map.Entry e;

                SynchronizedMapEntry(Object o) {
                    e = (Map.Entry)o;
                }

                public boolean equals(Object o) {
                    synchronized (mutex) {
                        return e.equals(o);
                    }
                }

                public Object getKey() {
                    synchronized (mutex) {
                        return e.getKey();
                    }
                }

                public Object getValue() {
                    synchronized (mutex) {
                        return e.getValue();
                    }
                }

                public int hashCode() {
                    synchronized (mutex) {
                        return e.hashCode();
                    }
                }

                public Object setValue(Object value) {
                    synchronized (mutex) {
                        return e.setValue(value);
                    }
                }

                public String toString() {
                    synchronized (mutex) {
                        return e.toString();
                    }
                }

                public void $RESTORE(long timestamp, boolean trim) {
                    if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                        $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
                        FieldRecord.popState($RECORDS);
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
            if (entries == null)
                synchronized (mutex) {
                    $ASSIGN$entries(new SynchronizedSet(mutex, m.entrySet()) {

                        public Iterator iterator() {
                            synchronized (super.mutex) {
                                return new SynchronizedIterator(super.mutex, c.iterator()) {

                                    public Object next() {
                                        synchronized (super.mutex) {
                                            return new SynchronizedMapEntry(super.next());
                                        }
                                    }

                                    final class _PROXY_ implements Rollbackable {

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
                            }
                        }

                        final class _PROXY_ implements Rollbackable {

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
                }
            return entries;
        }

        public boolean equals(Object o) {
            synchronized (mutex) {
                return m.equals(o);
            }
        }

        public Object get(Object key) {
            synchronized (mutex) {
                return m.get(key);
            }
        }

        public int hashCode() {
            synchronized (mutex) {
                return m.hashCode();
            }
        }

        public boolean isEmpty() {
            synchronized (mutex) {
                return m.isEmpty();
            }
        }

        public Set keySet() {
            if (keys == null)
                synchronized (mutex) {
                    $ASSIGN$keys(new SynchronizedSet(mutex, m.keySet()));
                }
            return keys;
        }

        public Object put(Object key, Object value) {
            synchronized (mutex) {
                return m.put(key, value);
            }
        }

        public void putAll(Map map) {
            synchronized (mutex) {
                m.putAll(map);
            }
        }

        public Object remove(Object o) {
            synchronized (mutex) {
                return m.remove(o);
            }
        }

        public int size() {
            synchronized (mutex) {
                return m.size();
            }
        }

        public String toString() {
            synchronized (mutex) {
                return m.toString();
            }
        }

        public Collection values() {
            if (values == null)
                synchronized (mutex) {
                    $ASSIGN$values(new SynchronizedCollection(mutex, m.values()));
                }
            return values;
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

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$m.restore(m, timestamp, trim);
            entries = (Set)$RECORD$entries.restore(entries, timestamp, trim);
            keys = (Set)$RECORD$keys.restore(keys, timestamp, trim);
            values = (Collection)$RECORD$values.restore(values, timestamp, trim);
            if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
                FieldRecord.popState($RECORDS);
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

        private FieldRecord $RECORD$m = new FieldRecord(0);

        private FieldRecord $RECORD$entries = new FieldRecord(0);

        private FieldRecord $RECORD$keys = new FieldRecord(0);

        private FieldRecord $RECORD$values = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$m,
                $RECORD$entries,
                $RECORD$keys,
                $RECORD$values
            };
    }

    static class SynchronizedSet extends SynchronizedCollection implements Set, Rollbackable {

        private static final long serialVersionUID = 487447009682186044L;

        SynchronizedSet(Set s) {
            super(s);
        }

        SynchronizedSet(Object sync, Set s) {
            super(sync, s);
        }

        public boolean equals(Object o) {
            synchronized (mutex) {
                return c.equals(o);
            }
        }

        public int hashCode() {
            synchronized (mutex) {
                return c.hashCode();
            }
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static final class SynchronizedSortedMap extends SynchronizedMap implements SortedMap, Rollbackable {

        private static final long serialVersionUID = -8798146769416483793L;

        private final SortedMap sm;

        SynchronizedSortedMap(SortedMap sm) {
            super(sm);
            this.sm = sm;
        }

        SynchronizedSortedMap(Object sync, SortedMap sm) {
            super(sync, sm);
            this.sm = sm;
        }

        public Comparator comparator() {
            synchronized (mutex) {
                return sm.comparator();
            }
        }

        public Object firstKey() {
            synchronized (mutex) {
                return sm.firstKey();
            }
        }

        public SortedMap headMap(Object toKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap(mutex, sm.headMap(toKey));
            }
        }

        public Object lastKey() {
            synchronized (mutex) {
                return sm.lastKey();
            }
        }

        public SortedMap subMap(Object fromKey, Object toKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap(mutex, sm.subMap(fromKey, toKey));
            }
        }

        public SortedMap tailMap(Object fromKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap(mutex, sm.tailMap(fromKey));
            }
        }

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$sm.restore(sm, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$sm = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$sm
            };
    }

    private static final class SynchronizedSortedSet extends SynchronizedSet implements SortedSet, Rollbackable {

        private static final long serialVersionUID = 8695801310862127406L;

        private final SortedSet ss;

        SynchronizedSortedSet(SortedSet ss) {
            super(ss);
            this.ss = ss;
        }

        SynchronizedSortedSet(Object sync, SortedSet ss) {
            super(sync, ss);
            this.ss = ss;
        }

        public Comparator comparator() {
            synchronized (mutex) {
                return ss.comparator();
            }
        }

        public Object first() {
            synchronized (mutex) {
                return ss.first();
            }
        }

        public SortedSet headSet(Object toElement) {
            synchronized (mutex) {
                return new SynchronizedSortedSet(mutex, ss.headSet(toElement));
            }
        }

        public Object last() {
            synchronized (mutex) {
                return ss.last();
            }
        }

        public SortedSet subSet(Object fromElement, Object toElement) {
            synchronized (mutex) {
                return new SynchronizedSortedSet(mutex, ss.subSet(fromElement, toElement));
            }
        }

        public SortedSet tailSet(Object fromElement) {
            synchronized (mutex) {
                return new SynchronizedSortedSet(mutex, ss.tailSet(fromElement));
            }
        }

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$ss.restore(ss, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$ss = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$ss
            };
    }

    private static class UnmodifiableCollection implements Collection, Serializable, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private static final long serialVersionUID = 1820017752578914078L;

        final Collection c;

        UnmodifiableCollection(Collection c) {
            this.c = c;
            if (c == null)
                throw new NullPointerException();
        }

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o) {
            return c.contains(o);
        }

        public boolean containsAll(Collection c1) {
            return c.containsAll(c1);
        }

        public boolean isEmpty() {
            return c.isEmpty();
        }

        public Iterator iterator() {
            return new UnmodifiableIterator(c.iterator());
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return c.size();
        }

        public Object[] toArray() {
            return c.toArray();
        }

        public Object[] toArray(Object[] a) {
            return c.toArray(a);
        }

        public String toString() {
            return c.toString();
        }

        public void $RESTORE(long timestamp, boolean trim) {
            if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
                FieldRecord.popState($RECORDS);
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

    private static class UnmodifiableIterator implements Iterator, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private final Iterator i;

        UnmodifiableIterator(Iterator i) {
            this.i = i;
        }

        public Object next() {
            return i.next();
        }

        public boolean hasNext() {
            return i.hasNext();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$i.restore(i, timestamp, trim);
            if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
                FieldRecord.popState($RECORDS);
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

        private FieldRecord $RECORD$i = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$i
            };
    }

    private static class UnmodifiableList extends UnmodifiableCollection implements List, Rollbackable {

        private static final long serialVersionUID = -283967356065247728L;

        final List list;

        UnmodifiableList(List l) {
            super(l);
            list = l;
        }

        public void add(int index, Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(int index, Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object o) {
            return list.equals(o);
        }

        public Object get(int index) {
            return list.get(index);
        }

        public int hashCode() {
            return list.hashCode();
        }

        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        public ListIterator listIterator() {
            return new UnmodifiableListIterator(list.listIterator());
        }

        public ListIterator listIterator(int index) {
            return new UnmodifiableListIterator(list.listIterator(index));
        }

        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }

        public Object set(int index, Object o) {
            throw new UnsupportedOperationException();
        }

        public List subList(int fromIndex, int toIndex) {
            return unmodifiableList(list.subList(fromIndex, toIndex));
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static final class UnmodifiableRandomAccessList extends UnmodifiableList implements RandomAccess, Rollbackable {

        private static final long serialVersionUID = -2542308836966382001L;

        UnmodifiableRandomAccessList(List l) {
            super(l);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static final class UnmodifiableListIterator extends UnmodifiableIterator implements ListIterator, Rollbackable {

        private final ListIterator li;

        UnmodifiableListIterator(ListIterator li) {
            super(li);
            this.li = li;
        }

        public void add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean hasPrevious() {
            return li.hasPrevious();
        }

        public int nextIndex() {
            return li.nextIndex();
        }

        public Object previous() {
            return li.previous();
        }

        public int previousIndex() {
            return li.previousIndex();
        }

        public void set(Object o) {
            throw new UnsupportedOperationException();
        }

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$li.restore(li, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$li = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$li
            };
    }

    private static class UnmodifiableMap implements Map, Serializable, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private static final long serialVersionUID = -1034234728574286014L;

        private final Map m;

        private transient Set entries;

        private transient Set keys;

        private transient Collection values;

        private static final class UnmodifiableEntrySet extends UnmodifiableSet implements Serializable, Rollbackable {

            private static final long serialVersionUID = 7854390611657943733L;

            UnmodifiableEntrySet(Set s) {
                super(s);
            }

            public Iterator iterator() {
                return new UnmodifiableIterator(c.iterator()) {

                    public Object next() {
                        final Map.Entry e = (Map.Entry)super.next();
                        return new Map.Entry() {

                            protected Checkpoint $CHECKPOINT = new Checkpoint(this);

                            public boolean equals(Object o) {
                                return e.equals(o);
                            }

                            public Object getKey() {
                                return e.getKey();
                            }

                            public Object getValue() {
                                return e.getValue();
                            }

                            public int hashCode() {
                                return e.hashCode();
                            }

                            public Object setValue(Object value) {
                                throw new UnsupportedOperationException();
                            }

                            public String toString() {
                                return e.toString();
                            }

                            public void $RESTORE(long timestamp, boolean trim) {
                                if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                                    $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
                                    FieldRecord.popState($RECORDS);
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
                        };
                    }

                    final class _PROXY_ implements Rollbackable {

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
            }

            public void $RESTORE(long timestamp, boolean trim) {
                super.$RESTORE(timestamp, trim);
            }

            private FieldRecord[] $RECORDS = new FieldRecord[] {
                };
        }

        UnmodifiableMap(Map m) {
            this.m = m;
            if (m == null)
                throw new NullPointerException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Object key) {
            return m.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return m.containsValue(value);
        }

        public Set entrySet() {
            if (entries == null)
                $ASSIGN$entries(new UnmodifiableEntrySet(m.entrySet()));
            return entries;
        }

        public boolean equals(Object o) {
            return m.equals(o);
        }

        public Object get(Object key) {
            return m.get(key);
        }

        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        public int hashCode() {
            return m.hashCode();
        }

        public boolean isEmpty() {
            return m.isEmpty();
        }

        public Set keySet() {
            if (keys == null)
                $ASSIGN$keys(new UnmodifiableSet(m.keySet()));
            return keys;
        }

        public void putAll(Map m) {
            throw new UnsupportedOperationException();
        }

        public Object remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return m.size();
        }

        public String toString() {
            return m.toString();
        }

        public Collection values() {
            if (values == null)
                $ASSIGN$values(new UnmodifiableCollection(m.values()));
            return values;
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

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$m.restore(m, timestamp, trim);
            entries = (Set)$RECORD$entries.restore(entries, timestamp, trim);
            keys = (Set)$RECORD$keys.restore(keys, timestamp, trim);
            values = (Collection)$RECORD$values.restore(values, timestamp, trim);
            if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
                FieldRecord.popState($RECORDS);
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

        private FieldRecord $RECORD$m = new FieldRecord(0);

        private FieldRecord $RECORD$entries = new FieldRecord(0);

        private FieldRecord $RECORD$keys = new FieldRecord(0);

        private FieldRecord $RECORD$values = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$m,
                $RECORD$entries,
                $RECORD$keys,
                $RECORD$values
            };
    }

    private static class UnmodifiableSet extends UnmodifiableCollection implements Set, Rollbackable {

        private static final long serialVersionUID = -9215047833775013803L;

        UnmodifiableSet(Set s) {
            super(s);
        }

        public boolean equals(Object o) {
            return c.equals(o);
        }

        public int hashCode() {
            return c.hashCode();
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    private static class UnmodifiableSortedMap extends UnmodifiableMap implements SortedMap, Rollbackable {

        private static final long serialVersionUID = -8806743815996713206L;

        private final SortedMap sm;

        UnmodifiableSortedMap(SortedMap sm) {
            super(sm);
            this.sm = sm;
        }

        public Comparator comparator() {
            return sm.comparator();
        }

        public Object firstKey() {
            return sm.firstKey();
        }

        public SortedMap headMap(Object toKey) {
            return new UnmodifiableSortedMap(sm.headMap(toKey));
        }

        public Object lastKey() {
            return sm.lastKey();
        }

        public SortedMap subMap(Object fromKey, Object toKey) {
            return new UnmodifiableSortedMap(sm.subMap(fromKey, toKey));
        }

        public SortedMap tailMap(Object fromKey) {
            return new UnmodifiableSortedMap(sm.tailMap(fromKey));
        }

        public void $RESTORE(long timestamp, boolean trim) {
            $RECORD$sm.restore(sm, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$sm = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$sm
            };
    }

    private static class UnmodifiableSortedSet extends UnmodifiableSet implements SortedSet, Rollbackable {

        private static final long serialVersionUID = -4929149591599911165L;

        private SortedSet ss;

        UnmodifiableSortedSet(SortedSet ss) {
            super(ss);
            this.$ASSIGN$ss(ss);
        }

        public Comparator comparator() {
            return ss.comparator();
        }

        public Object first() {
            return ss.first();
        }

        public SortedSet headSet(Object toElement) {
            return new UnmodifiableSortedSet(ss.headSet(toElement));
        }

        public Object last() {
            return ss.last();
        }

        public SortedSet subSet(Object fromElement, Object toElement) {
            return new UnmodifiableSortedSet(ss.subSet(fromElement, toElement));
        }

        public SortedSet tailSet(Object fromElement) {
            return new UnmodifiableSortedSet(ss.tailSet(fromElement));
        }

        private final SortedSet $ASSIGN$ss(SortedSet newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$ss.add(null, ss, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return ss = newValue;
        }

        public void $RESTORE(long timestamp, boolean trim) {
            ss = (SortedSet)$RECORD$ss.restore(ss, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$ss = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$ss
            };
    }

    private static boolean isSequential(List l) {
        return !(l instanceof RandomAccess) && l.size() > LARGE_LIST_SIZE;
    }

    private Collections() {
    }

    static final int compare(Object o1, Object o2, Comparator c) {
        return c == null?((Comparable)o1).compareTo(o2):c.compare(o1, o2);
    }

    public static int binarySearch(List l, Object key) {
        return binarySearch(l, key, null);
    }

    public static int binarySearch(List l, Object key, Comparator c) {
        int pos = 0;
        int low = 0;
        int hi = l.size() - 1;
        if (isSequential(l)) {
            ListIterator itr = l.listIterator();
            int i = 0;
            while (low <= hi) {
                pos = (low + hi) >> 1;
                if (i < pos)
                    for (; i != pos; i++, itr.next()) 
                        ;
                else
                    for (; i != pos; i--, itr.previous()) 
                        ;
                final int d = compare(key, itr.next(), c);
                if (d == 0)
                    return pos;
                else if (d < 0)
                    hi = pos - 1;
                else
                    low = ++pos;
            }        } else {
            while (low <= hi) {
                pos = (low + hi) >> 1;
                final int d = compare(key, l.get(pos), c);
                if (d == 0)
                    return pos;
                else if (d < 0)
                    hi = pos - 1;
                else
                    low = ++pos;
            }
        }
        return -pos - 1;
    }

    public static void copy(List dest, List source) {
        int pos = source.size();
        if (dest.size() < pos)
            throw new IndexOutOfBoundsException("Source does not fit in dest");
        Iterator i1 = source.iterator();
        ListIterator i2 = dest.listIterator();
        while (--pos >= 0) {
            i2.next();
            i2.set(i1.next());
        }
    }

    public static Enumeration enumeration(Collection c) {
        final Iterator i = c.iterator();
        return new Enumeration() {

            public final boolean hasMoreElements() {
                return i.hasNext();
            }

            public final Object nextElement() {
                return i.next();
            }
        };
    }

    public static void fill(List l, Object val) {
        ListIterator itr = l.listIterator();
        for (int i = l.size() - 1; i >= 0; --i) {
            itr.next();
            itr.set(val);
        }
    }

    public static int indexOfSubList(List source, List target) {
        int ssize = source.size();
        for (int i = 0, j = target.size(); j <= ssize; i++, j++) 
            if (source.subList(i, j).equals(target))
                return i;
        return -1;
    }

    public static int lastIndexOfSubList(List source, List target) {
        int ssize = source.size();
        for (int i = ssize - target.size(), j = ssize; i >= 0; i--, j--) 
            if (source.subList(i, j).equals(target))
                return i;
        return -1;
    }

    public static ArrayList list(Enumeration e) {
        ArrayList l = new ArrayList();
        while (e.hasMoreElements()) 
            l.add(e.nextElement());
        return l;
    }

    public static Object max(Collection c) {
        return max(c, null);
    }

    public static Object max(Collection c, Comparator order) {
        Iterator itr = c.iterator();
        Object max = itr.next();
        int csize = c.size();
        for (int i = 1; i < csize; i++) {
            Object o = itr.next();
            if (compare(max, o, order) < 0)
                max = o;
        }
        return max;
    }

    public static Object min(Collection c) {
        return min(c, null);
    }

    public static Object min(Collection c, Comparator order) {
        Iterator itr = c.iterator();
        Object min = itr.next();
        int csize = c.size();
        for (int i = 1; i < csize; i++) {
            Object o = itr.next();
            if (compare(min, o, order) > 0)
                min = o;
        }
        return min;
    }

    public static List nCopies(final int n, final Object o) {
        return new CopiesList(n, o);
    }

    public static boolean replaceAll(List list, Object oldval, Object newval) {
        ListIterator itr = list.listIterator();
        boolean replace_occured = false;
        for (int i = list.size(); --i >= 0; ) 
            if (AbstractCollection.equals(oldval, itr.next())) {
                itr.set(newval);
                replace_occured = true;
            }
        return replace_occured;
    }

    public static void reverse(List l) {
        ListIterator i1 = l.listIterator();
        int pos1 = 1;
        int pos2 = l.size();
        ListIterator i2 = l.listIterator(pos2);
        while (pos1 < pos2) {
            Object o = i1.next();
            i1.set(i2.previous());
            i2.set(o);
            ++pos1;
            --pos2;
        }
    }

    public static Comparator reverseOrder() {
        return rcInstance;
    }

    public static void rotate(List list, int distance) {
        int size = list.size();
        distance %= size;
        if (distance == 0)
            return;
        if (distance < 0)
            distance += size;
        if (isSequential(list)) {
            reverse(list);
            reverse(list.subList(0, distance));
            reverse(list.subList(distance, size));
        } else {
            int a = size;
            int lcm = distance;
            int b = a % lcm;
            while (b != 0) {
                a = lcm;
                lcm = b;
                b = a % lcm;
            }
            while (--lcm >= 0) {
                Object o = list.get(lcm);
                for (int i = lcm + distance; i != lcm; i = (i + distance) % size) 
                    o = list.set(i, o);
                list.set(lcm, o);
            }
        }
    }

    public static void shuffle(List l) {
        if (defaultRandom == null) {
            synchronized (Collections.class) {
                if (defaultRandom == null)
                    defaultRandom = new Random();
            }
        }
        shuffle(l, defaultRandom);
    }

    public static void shuffle(List l, Random r) {
        int lsize = l.size();
        ListIterator i = l.listIterator(lsize);
        boolean sequential = isSequential(l);
        Object[] a = null;
        if (sequential)
            a = l.toArray();
        for (int pos = lsize - 1; pos > 0; --pos) {
            int swap = r.nextInt(pos + 1);
            Object o;
            if (sequential) {
                o = a[swap];
                a[swap] = i.previous();
            } else
                o = l.set(swap, i.previous());
            i.set(o);
        }
    }

    public static Set singleton(Object o) {
        return new SingletonSet(o);
    }

    public static List singletonList(Object o) {
        return new SingletonList(o);
    }

    public static Map singletonMap(Object key, Object value) {
        return new SingletonMap(key, value);
    }

    public static void sort(List l) {
        sort(l, null);
    }

    public static void sort(List l, Comparator c) {
        Object[] a = l.toArray();
        Arrays.sort(a, c);
        ListIterator i = l.listIterator(a.length);
        for (int pos = a.length; --pos >= 0; ) {
            i.previous();
            i.set(a[pos]);
        }
    }

    public static void swap(List l, int i, int j) {
        l.set(i, l.set(j, l.get(i)));
    }

    public static Collection synchronizedCollection(Collection c) {
        return new SynchronizedCollection(c);
    }

    public static List synchronizedList(List l) {
        if (l instanceof RandomAccess)
            return new SynchronizedRandomAccessList(l);
        return new SynchronizedList(l);
    }

    public static Map synchronizedMap(Map m) {
        return new SynchronizedMap(m);
    }

    public static Set synchronizedSet(Set s) {
        return new SynchronizedSet(s);
    }

    public static SortedMap synchronizedSortedMap(SortedMap m) {
        return new SynchronizedSortedMap(m);
    }

    public static SortedSet synchronizedSortedSet(SortedSet s) {
        return new SynchronizedSortedSet(s);
    }

    public static Collection unmodifiableCollection(Collection c) {
        return new UnmodifiableCollection(c);
    }

    public static List unmodifiableList(List l) {
        if (l instanceof RandomAccess)
            return new UnmodifiableRandomAccessList(l);
        return new UnmodifiableList(l);
    }

    public static Map unmodifiableMap(Map m) {
        return new UnmodifiableMap(m);
    }

    public static Set unmodifiableSet(Set s) {
        return new UnmodifiableSet(s);
    }

    public static SortedMap unmodifiableSortedMap(SortedMap m) {
        return new UnmodifiableSortedMap(m);
    }

    public static SortedSet unmodifiableSortedSet(SortedSet s) {
        return new UnmodifiableSortedSet(s);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
            $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
            FieldRecord.popState($RECORDS);
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
