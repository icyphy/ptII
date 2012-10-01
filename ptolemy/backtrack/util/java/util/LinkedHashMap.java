/* LinkedHashMap.java -- a class providing hashtable data structure,
   mapping Object --> Object, with linked list traversal
   Copyright (C) 2001, 2002, 2005 Free Software Foundation, Inc.

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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

/**
 * This class provides a hashtable-backed implementation of the
 * Map interface, with predictable traversal order.
 * <p>
 * It uses a hash-bucket approach; that is, hash collisions are handled
 * by linking the new node off of the pre-existing node (or list of
 * nodes).  In this manner, techniques such as linear probing (which
 * can cause primary clustering) and rehashing (which does not fit very
 * well with Java's method of precomputing hash codes) are avoided.  In
 * addition, this maintains a doubly-linked list which tracks either
 * insertion or access order.
 * <p>
 * In insertion order, calling <code>put</code> adds the key to the end of
 * traversal, unless the key was already in the map; changing traversal order
 * requires removing and reinserting a key.  On the other hand, in access
 * order, all calls to <code>put</code> and <code>get</code> cause the
 * accessed key to move to the end of the traversal list.  Note that any
 * accesses to the map's contents via its collection views and iterators do
 * not affect the map's traversal order, since the collection views do not
 * call <code>put</code> or <code>get</code>.
 * <p>
 * One of the nice features of tracking insertion order is that you can
 * copy a hashtable, and regardless of the implementation of the original,
 * produce the same results when iterating over the copy.  This is possible
 * without needing the overhead of <code>TreeMap</code>.
 * <p>
 * When using this {
@link #LinkedHashMap(int, float, boolean) constructor}
,
 * you can build an access-order mapping.  This can be used to implement LRU
 * caches, for example.  By overriding {
@link #removeEldestEntry(Map.Entry)}
,
 * you can also control the removal of the oldest entry, and thereby do
 * things like keep the map at a fixed size.
 * <p>
 * Under ideal circumstances (no collisions), LinkedHashMap offers O(1)
 * performance on most operations (<code>containsValue()</code> is,
 * of course, O(n)).  In the worst case (all keys map to the same
 * hash code -- very unlikely), most operations are O(n).  Traversal is
 * faster than in HashMap (proportional to the map size, and not the space
 * allocated for the map), but other operations may be slower because of the
 * overhead of the maintaining the traversal order list.
 * <p>
 * LinkedHashMap accepts the null key and null values.  It is not
 * synchronized, so if you need multi-threaded access, consider using:<br>
 * <code>Map m = Collections.synchronizedMap(new LinkedHashMap(...));</code>
 * <p>
 * The iterators are <i>fail-fast</i>, meaning that any structural
 * modification, except for <code>remove()</code> called on the iterator
 * itself, cause the iterator to throw a{
@link ConcurrentModificationException}
 rather than exhibit
 * non-deterministic behavior.
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see Object#hashCode()
 * @see Collection
 * @see Map
 * @see HashMap
 * @see TreeMap
 * @see Hashtable
 * @since 1.4
 * @status updated to 1.4
 */
public class LinkedHashMap extends HashMap implements Rollbackable {

    /**
     * Compatible with JDK 1.4.
     */
    private static final long serialVersionUID = 3801124242820219131L;

    /**
     * The oldest Entry to begin iteration at.
     */
    private transient LinkedHashEntry root;

    /**
     * The iteration order of this linked hash map: <code>true</code> for
     * access-order, <code>false</code> for insertion-order.
     * @serial true for access order traversal
     */
    final boolean accessOrder;

    /**
     * Class to represent an entry in the hash table. Holds a single key-value
     * pair and the doubly-linked insertion order list.
     */
    class LinkedHashEntry extends HashEntry implements Rollbackable {

        /**
         * The predecessor in the iteration list. If this entry is the root
         * (eldest), pred points to the newest entry.
         */
        private LinkedHashEntry pred;

        /**
         * The successor in the iteration list, null if this is the newest.
         */
        private LinkedHashEntry succ;

        /**
         * Simple constructor.
         * @param key the key
         * @param value the value
         */
        LinkedHashEntry(Object key, Object value) {
            super(key, value);
            if (getRoot() == null) {
                setRoot(this);
                setPred(this);
            } else {
                setPred(getRoot().getPred());
                getPred().setSucc(this);
                getRoot().setPred(this);
            }
        }

        /**
         * Called when this entry is accessed via put or get. This version does
         * the necessary bookkeeping to keep the doubly-linked list in order,
         * after moving this element to the newest position in access order.
         */
        void access() {
            if (accessOrder && getSucc() != null) {
                setModCount(getModCount() + 1);
                if (this == getRoot()) {
                    setRoot(getSucc());
                    getPred().setSucc(this);
                    setSucc(null);
                } else {
                    getPred().setSucc(succ);
                    getSucc().setPred(pred);
                    setSucc(null);
                    setPred(getRoot().getPred());
                    getPred().setSucc(this);
                    getRoot().setPred(this);
                }
            }
        }

        /**
         * Called when this entry is removed from the map. This version does
         * the necessary bookkeeping to keep the doubly-linked list in order.
         * @return the value of this key as it is removed
         */
        Object cleanup() {
            if (this == getRoot()) {
                setRoot(getSucc());
                if (getSucc() != null) {
                    getSucc().setPred(pred);
                }
            } else if (getSucc() == null) {
                getPred().setSucc(null);
                getRoot().setPred(pred);
            } else {
                getPred().setSucc(succ);
                getSucc().setPred(pred);
            }
            return getValueField();
        }

        void setPred(LinkedHashEntry pred) {
            this.$ASSIGN$pred(pred);
        }

        LinkedHashEntry getPred() {
            return pred;
        }

        void setSucc(LinkedHashEntry succ) {
            this.$ASSIGN$succ(succ);
        }

        LinkedHashEntry getSucc() {
            return succ;
        }

        private final LinkedHashEntry $ASSIGN$pred(LinkedHashEntry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$pred.add(null, pred, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return pred = newValue;
        }

        private final LinkedHashEntry $ASSIGN$succ(LinkedHashEntry newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$succ.add(null, succ, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return succ = newValue;
        }

        public void $COMMIT(long timestamp) {
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                    .getTopTimestamp());
            super.$COMMIT(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            pred = (LinkedHashEntry) $RECORD$pred
                    .restore(pred, timestamp, trim);
            succ = (LinkedHashEntry) $RECORD$succ
                    .restore(succ, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private transient FieldRecord $RECORD$pred = new FieldRecord(0);

        private transient FieldRecord $RECORD$succ = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$pred, $RECORD$succ };

    }

    // class LinkedHashEntry
    /**
     * Construct a new insertion-ordered LinkedHashMap with the default
     * capacity (11) and the default load factor (0.75).
     */
    public LinkedHashMap() {
        super();
        accessOrder = false;
    }

    /**
     * Construct a new insertion-ordered LinkedHashMap from the given Map,
     * with initial capacity the greater of the size of <code>m</code> or
     * the default of 11.
     * <p>
     * Every element in Map m will be put into this new HashMap, in the
     * order of m's iterator.
     * @param m a Map whose key / value pairs will be put into
     * the new HashMap.  <b>NOTE: key / value pairs
     * are not cloned in this constructor.</b>
     * @exception NullPointerException if m is null
     */
    public LinkedHashMap(Map m) {
        super(m);
        accessOrder = false;
    }

    /**
     * Construct a new insertion-ordered LinkedHashMap with a specific
     * inital capacity and default load factor of 0.75.
     * @param initialCapacity the initial capacity of this HashMap (&gt;= 0)
     * @exception IllegalArgumentException if (initialCapacity &lt; 0)
     */
    public LinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        accessOrder = false;
    }

    /**
     * Construct a new insertion-orderd LinkedHashMap with a specific
     * inital capacity and load factor.
     * @param initialCapacity the initial capacity (&gt;= 0)
     * @param loadFactor the load factor (&gt; 0, not NaN)
     * @exception IllegalArgumentException if (initialCapacity &lt; 0) ||
     * ! (loadFactor &gt; 0.0)
     */
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        accessOrder = false;
    }

    /**
     * Construct a new LinkedHashMap with a specific inital capacity, load
     * factor, and ordering mode.
     * @param initialCapacity the initial capacity (&gt;=0)
     * @param loadFactor the load factor (&gt;0, not NaN)
     * @param accessOrder true for access-order, false for insertion-order
     * @exception IllegalArgumentException if (initialCapacity &lt; 0) ||
     * ! (loadFactor &gt; 0.0)
     */
    public LinkedHashMap(int initialCapacity, float loadFactor,
            boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }

    /**
     * Clears the Map so it has no keys. This is O(1).
     */
    public void clear() {
        super.clear();
        setRoot(null);
    }

    /**
     * Returns <code>true</code> if this HashMap contains a value
     * <code>o</code>, such that <code>o.equals(value)</code>.
     * @param value the value to search for in this HashMap
     * @return <code>true</code> if at least one key maps to the value
     */
    public boolean containsValue(Object value) {
        LinkedHashEntry e = getRoot();
        while (e != null) {
            if (equals(value, e.getValueField())) {
                return true;
            }
            e = e.getSucc();
        }
        return false;
    }

    /**
     * Return the value in this Map associated with the supplied key,
     * or <code>null</code> if the key maps to nothing.  If this is an
     * access-ordered Map and the key is found, this performs structural
     * modification, moving the key to the newest end of the list. NOTE:
     * Since the value could also be null, you must use containsKey to
     * see if this key actually maps to something.
     * @param key the key for which to fetch an associated value
     * @return what the key maps to, if present
     * @see #put(Object, Object)
     * @see #containsKey(Object)
     */
    public Object get(Object key) {
        int idx = hash(key);
        HashEntry e = getBuckets()[idx];
        while (e != null) {
            if (equals(key, e.getKeyField())) {
                e.access();
                return e.getValueField();
            }
            e = e.getNext();
        }
        return null;
    }

    /**
     * Returns <code>true</code> if this map should remove the eldest entry.
     * This method is invoked by all calls to <code>put</code> and
     * <code>putAll</code> which place a new entry in the map, providing
     * the implementer an opportunity to remove the eldest entry any time
     * a new one is added.  This can be used to save memory usage of the
     * hashtable, as well as emulating a cache, by deleting stale entries.
     * <p>
     * For example, to keep the Map limited to 100 entries, override as follows:
     * <pre>
     * private static final int MAX_ENTRIES = 100;
     * protected boolean removeEldestEntry(Map.Entry eldest)
     * {
     * return size() &gt; MAX_ENTRIES;
     * }
     * </pre><p>
     * Typically, this method does not modify the map, but just uses the
     * return value as an indication to <code>put</code> whether to proceed.
     * However, if you override it to modify the map, you must return false
     * (indicating that <code>put</code> should leave the modified map alone),
     * or you face unspecified behavior.  Remember that in access-order mode,
     * even calling <code>get</code> is a structural modification, but using
     * the collections views (such as <code>keySet</code>) is not.
     * <p>
     * This method is called after the eldest entry has been inserted, so
     * if <code>put</code> was called on a previously empty map, the eldest
     * entry is the one you just put in! The default implementation just
     * returns <code>false</code>, so that this map always behaves like
     * a normal one with unbounded growth.
     * @param eldest the eldest element which would be removed if this
     * returns true. For an access-order map, this is the least
     * recently accessed; for an insertion-order map, this is the
     * earliest element inserted.
     * @return true if <code>eldest</code> should be removed
     */
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return false;
    }

    /**
     * Helper method called by <code>put</code>, which creates and adds a
     * new Entry, followed by performing bookkeeping (like removeEldestEntry).
     * @param key the key of the new Entry
     * @param value the value
     * @param idx the index in buckets where the new Entry belongs
     * @param callRemove whether to call the removeEldestEntry method
     * @see #put(Object, Object)
     * @see #removeEldestEntry(Map.Entry)
     * @see LinkedHashEntry#LinkedHashEntry(Object, Object)
     */
    void addEntry(Object key, Object value, int idx, boolean callRemove) {
        LinkedHashEntry e = new LinkedHashEntry(key, value);
        e.setNext(getBuckets()[idx]);
        getBuckets()[idx] = e;
        if (callRemove && removeEldestEntry(getRoot())) {
            remove(getRoot().getKeyField());
        }
    }

    /**
     * Helper method, called by clone() to reset the doubly-linked list.
     * @param m the map to add entries from
     * @see #clone()
     */
    void putAllInternal(Map m) {
        setRoot(null);
        super.putAllInternal(m);
    }

    /**
     * Generates a parameterized iterator. This allows traversal to follow
     * the doubly-linked list instead of the random bin order of HashMap.
     * @param type {
    @link #KEYS    }
    , {
    @link #VALUES    }
    , or {
    @link #ENTRIES    }

     * @return the appropriate iterator
     */
    Iterator iterator(final int type) {
        return new Iterator() {
            /**
             * The current Entry.
             */
            private LinkedHashEntry current = getRoot();

            /**
             * The previous Entry returned by next().
             */
            private LinkedHashEntry last;

            /**
             * The number of known modifications to the backing Map.
             */
            private int knownMod = getModCount();

            /**
             * Returns true if the Iterator has more elements.
             * @return true if there are more elements
             */
            public boolean hasNext() {
                return getCurrent() != null;
            }

            /**
             * Returns the next element in the Iterator's sequential view.
             * @return the next element
             * @exception ConcurrentModificationException if the HashMap was modified
             * @exception NoSuchElementException if there is none
             */
            public Object next() {
                if (getKnownMod() != getModCount()) {
                    throw new ConcurrentModificationException();
                }
                if (getCurrent() == null) {
                    throw new NoSuchElementException();
                }
                setLast(getCurrent());
                setCurrent(getCurrent().getSucc());
                return type == VALUES ? getLast().getValueField()
                        : type == KEYS ? getLast().getKeyField() : getLast();
            }

            /**
             * Removes from the backing HashMap the last element which was fetched
             * with the <code>next()</code> method.
             * @exception ConcurrentModificationException if the HashMap was modified
             * @exception IllegalStateException if called when there is no last element
             */
            public void remove() {
                if (getKnownMod() != getModCount()) {
                    throw new ConcurrentModificationException();
                }
                if (getLast() == null) {
                    throw new IllegalStateException();
                }
                LinkedHashMap.this.remove(getLast().getKeyField());
                setLast(null);
                setKnownMod(getKnownMod() + 1);
            }

            void setCurrent(LinkedHashEntry current) {
                this.$ASSIGN$current(current);
            }

            LinkedHashEntry getCurrent() {
                return current;
            }

            void setLast(LinkedHashEntry last) {
                this.$ASSIGN$last(last);
            }

            LinkedHashEntry getLast() {
                return last;
            }

            void setKnownMod(int knownMod) {
                this.$ASSIGN$knownMod(knownMod);
            }

            int getKnownMod() {
                return knownMod;
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

            private final LinkedHashEntry $ASSIGN$current(
                    LinkedHashEntry newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$current.add(null, current, $CHECKPOINT
                            .getTimestamp());
                }
                if (newValue != null
                        && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                    newValue.$SET$CHECKPOINT($CHECKPOINT);
                }
                return current = newValue;
            }

            private final LinkedHashEntry $ASSIGN$last(LinkedHashEntry newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$last.add(null, last, $CHECKPOINT.getTimestamp());
                }
                if (newValue != null
                        && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                    newValue.$SET$CHECKPOINT($CHECKPOINT);
                }
                return last = newValue;
            }

            private final int $ASSIGN$knownMod(int newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$knownMod.add(null, knownMod, $CHECKPOINT
                            .getTimestamp());
                }
                return knownMod = newValue;
            }

            public void $COMMIT_ANONYMOUS(long timestamp) {
                FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                        .getTopTimestamp());
                $RECORD$$CHECKPOINT.commit(timestamp);
            }

            public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                current = (LinkedHashEntry) $RECORD$current.restore(current,
                        timestamp, trim);
                last = (LinkedHashEntry) $RECORD$last.restore(last, timestamp,
                        trim);
                knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
                if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                    $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT,
                            new _PROXY_(), timestamp, trim);
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
                        $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint
                                .getTimestamp());
                        FieldRecord.pushState($RECORDS);
                    }
                    $CHECKPOINT = checkpoint;
                    oldCheckpoint.setCheckpoint(checkpoint);
                    checkpoint.addObject(new _PROXY_());
                }
                return this;
            }

            private transient FieldRecord $RECORD$current = new FieldRecord(0);

            private transient FieldRecord $RECORD$last = new FieldRecord(0);

            private transient FieldRecord $RECORD$knownMod = new FieldRecord(0);

            private transient FieldRecord[] $RECORDS = new FieldRecord[] {
                    $RECORD$current, $RECORD$last, $RECORD$knownMod };

            {
                $CHECKPOINT.addObject(new _PROXY_());
            }

        };
    }

    void setRoot(LinkedHashEntry root) {
        this.$ASSIGN$root(root);
    }

    LinkedHashEntry getRoot() {
        return root;
    }

    private final LinkedHashEntry $ASSIGN$root(LinkedHashEntry newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$root.add(null, root, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return root = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                .getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        root = (LinkedHashEntry) $RECORD$root.restore(root, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private transient FieldRecord $RECORD$root = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] { $RECORD$root };

}

// class LinkedHashMap
