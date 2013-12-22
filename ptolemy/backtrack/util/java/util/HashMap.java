/* HashMap.java -- a class providing a basic hashtable data structure,
   mapping Object --> Object
   Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005  Free Software Foundation, Inc.

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
// NOTE: This implementation is very similar to that of Hashtable. If you fix
// a bug in here, chances are you should make a similar change to the Hashtable
// code.
// NOTE: This implementation has some nasty coding style in order to
// support LinkedHashMap, which extends this.
package ptolemy.backtrack.util.java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;

/**
 * This class provides a hashtable-backed implementation of the
 * Map interface.
 * <p>
 * It uses a hash-bucket approach; that is, hash collisions are handled
 * by linking the new node off of the pre-existing node (or list of
 * nodes).  In this manner, techniques such as linear probing (which
 * can cause primary clustering) and rehashing (which does not fit very
 * well with Java's method of precomputing hash codes) are avoided.
 * <p>
 * Under ideal circumstances (no collisions), HashMap offers O(1)
 * performance on most operations (<code>containsValue()</code> is,
 * of course, O(n)).  In the worst case (all keys map to the same
 * hash code -- very unlikely), most operations are O(n).
 * <p>
 * HashMap is part of the JDK1.2 Collections API.  It differs from
 * Hashtable in that it accepts the null key and null values, and it
 * does not support "Enumeration views." Also, it is not synchronized;
 * if you plan to use it in multiple threads, consider using:<br>
 * <code>Map m = Collections.synchronizedMap(new HashMap(...));</code>
 * <p>
 * The iterators are <i>fail-fast</i>, meaning that any structural
 * modification, except for <code>remove()</code> called on the iterator
 * itself, cause the iterator to throw a
 * <code>ConcurrentModificationException</code> rather than exhibit
 * non-deterministic behavior.
 * @author Jon Zeppieri
 * @author Jochen Hoenicke
 * @author Bryce McKinlay
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see Object#hashCode()
 * @see Collection
 * @see Map
 * @see TreeMap
 * @see LinkedHashMap
 * @see IdentityHashMap
 * @see Hashtable
 * @since 1.2
 * @status updated to 1.4
 */
public class HashMap extends AbstractMap implements Map, Cloneable,
        Serializable, Rollbackable {

    /**
     * Default number of buckets. This is the value the JDK 1.3 uses. Some
     * early documentation specified this value as 101. That is incorrect.
     * Package visible for use by HashSet.
     */
    static final int DEFAULT_CAPACITY = 11;

    /**
     * The default load factor; this is explicitly specified by the spec.
     * Package visible for use by HashSet.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Compatible with JDK 1.2.
     */
    private static final long serialVersionUID = 362498820763181265L;

    /**
     * The rounded product of the capacity and the load factor; when the number
     * of elements exceeds the threshold, the HashMap calls
     * <code>rehash()</code>.
     * @serial the threshold for rehashing
     */
    private int threshold;

    /**
     * Load factor of this HashMap:  used in computing the threshold.
     * Package visible for use by HashSet.
     * @serial the load factor
     */
    final float loadFactor;

    /**
     * Array containing the actual key-value mappings.
     * Package visible for use by nested and subclasses.
     */
    private transient HashEntry[] buckets;

    /**
     * Counts the number of modifications this HashMap has undergone, used
     * by Iterators to know when to throw ConcurrentModificationExceptions.
     * Package visible for use by nested and subclasses.
     */
    private transient int modCount;

    /**
     * The size of this HashMap:  denotes the number of key-value pairs.
     * Package visible for use by nested and subclasses.
     */
    private transient int size;

    /**
     * The cache for {
    @link #entrySet()    }
    .
     */
    private transient Set entries;

    /**
     * Class to represent an entry in the hash table. Holds a single key-value
     * pair. Package visible for use by subclass.
     * @author Eric Blake (ebb9@email.byu.edu)
     */
    static class HashEntry extends AbstractMap.BasicMapEntry implements
            Rollbackable {

        /**
         * The next entry in the linked list. Package visible for use by subclass.
         */
        private HashEntry next;

        /**
         * Simple constructor.
         * @param key the key
         * @param value the value
         */
        HashEntry(Object key, Object value) {
            super(key, value);
        }

        /**
         * Called when this entry is accessed via {
        @link #put(Object, Object)        }
        .
         * This version does nothing, but in LinkedHashMap, it must do some
         * bookkeeping for access-traversal mode.
         */
        void access() {
        }

        /**
         * Called when this entry is removed from the map. This version simply
         * returns the value, but in LinkedHashMap, it must also do bookkeeping.
         * @return the value of this key as it is removed
         */
        Object cleanup() {
            return getValueField();
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
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                    .getTopTimestamp());
            super.$COMMIT(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            next = (HashEntry) $RECORD$next.restore(next, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private transient FieldRecord $RECORD$next = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] { $RECORD$next };

    }

    // check for NaN too
    // Must call this for bookkeeping in LinkedHashMap.
    // At this point, we know we need to add a new entry.
    // Need a new hash value to suit the bigger table.
    // LinkedHashMap cannot override put(), hence this call.
    // Optimize in case the Entry is one of our own.
    // Method call necessary for LinkedHashMap to work correctly.
    // This is impossible.
    // Clear the entry cache. AbstractMap.clone() does the others.
    // Create an AbstractSet with custom implementations of those methods
    // that can be overridden easily and efficiently.
    // Cannot create the iterator directly, because of LinkedHashMap.
    // Test against the size of the HashMap to determine if anything
    // really got removed. This is necessary because the return value
    // of HashMap.remove() is ambiguous in the null case.
    // We don't bother overriding many of the optional methods, as doing so
    // wouldn't provide any significant performance advantage.
    // Cannot create the iterator directly, because of LinkedHashMap.
    // Create an AbstractSet with custom implementations of those methods
    // that can be overridden easily and efficiently.
    // Cannot create the iterator directly, because of LinkedHashMap.
    // Package visible, for use in nested classes.
    // Write the threshold and loadFactor fields.
    // Avoid creating a wasted Set by creating the iterator directly.
    // Read the threshold and loadFactor fields.
    // Read and use capacity, followed by key/value pairs.
    /**
     * Iterate over HashMap's entries.
     * This implementation is parameterized to give a sequential view of
     * keys, values, or entries.
     * @author Jon Zeppieri
     */
    private final class HashIterator implements Iterator, Rollbackable {

        private transient Checkpoint $CHECKPOINT = new Checkpoint(this);

        /**
         * The type of this Iterator: {
        @link #KEYS        }
        , {
        @link #VALUES        }
        ,
         * or {
        @link #ENTRIES        }
        .
         */
        private final int type;

        /**
         * The number of modifications to the backing HashMap that we know about.
         */
        private int knownMod = getModCount();

        /**
         * The number of elements remaining to be returned by next().
         */
        private int count = getSize();

        /**
         * Current index in the physical hash table.
         */
        private int idx = getBuckets().length;

        /**
         * The last Entry returned by a next() call.
         */
        private HashEntry last;

        /**
         * The next entry that should be returned by next(). It is set to something
         * if we're iterating through a bucket that contains multiple linked
         * entries. It is null if next() needs to find a new bucket.
         */
        private HashEntry next;

        /**
         * Construct a new HashIterator with the supplied type.
         * @param type {
        @link #KEYS        }
        , {
        @link #VALUES        }
        , or {
        @link #ENTRIES        }

         */
        HashIterator(int type) {
            this.type = type;
        }

        /**
         * Returns true if the Iterator has more elements.
         * @return true if there are more elements
         */
        public boolean hasNext() {
            return count > 0;
        }

        /**
         * Returns the next element in the Iterator's sequential view.
         * @return the next element
         * @exception ConcurrentModificationException if the HashMap was modified
         * @exception NoSuchElementException if there is none
         */
        public Object next() {
            if (knownMod != getModCount()) {
                throw new ConcurrentModificationException();
            }
            if (count == 0) {
                throw new NoSuchElementException();
            }
            $ASSIGN$SPECIAL$count(12, count);
            HashEntry e = next;
            while (e == null) {
                e = getBuckets()[$ASSIGN$SPECIAL$idx(14, idx)];
            }
            $ASSIGN$next(e.getNext());
            $ASSIGN$last(e);
            if (type == VALUES) {
                return e.getValueField();
            }
            if (type == KEYS) {
                return e.getKeyField();
            }
            return e;
        }

        /**
         * Removes from the backing HashMap the last element which was fetched
         * with the <code>next()</code> method.
         * @exception ConcurrentModificationException if the HashMap was modified
         * @exception IllegalStateException if called when there is no last element
         */
        public void remove() {
            if (knownMod != getModCount()) {
                throw new ConcurrentModificationException();
            }
            if (last == null) {
                throw new IllegalStateException();
            }
            HashMap.this.remove(last.getKeyField());
            $ASSIGN$last(null);
            $ASSIGN$SPECIAL$knownMod(11, knownMod);
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
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                    .getTopTimestamp());
            $RECORD$$CHECKPOINT.commit(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
            count = $RECORD$count.restore(count, timestamp, trim);
            idx = $RECORD$idx.restore(idx, timestamp, trim);
            last = (HashEntry) $RECORD$last.restore(last, timestamp, trim);
            next = (HashEntry) $RECORD$next.restore(next, timestamp, trim);
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

        private transient FieldRecord $RECORD$type = new FieldRecord(0);

        private transient FieldRecord $RECORD$knownMod = new FieldRecord(0);

        private transient FieldRecord $RECORD$count = new FieldRecord(0);

        private transient FieldRecord $RECORD$idx = new FieldRecord(0);

        private transient FieldRecord $RECORD$last = new FieldRecord(0);

        private transient FieldRecord $RECORD$next = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$type, $RECORD$knownMod, $RECORD$count, $RECORD$idx,
                $RECORD$last, $RECORD$next };

    }

    /**
     * Construct a new HashMap with the default capacity (11) and the default
     * load factor (0.75).
     */
    public HashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct a new HashMap from the given Map, with initial capacity
     * the greater of the size of <code>m</code> or the default of 11.
     * <p>
     * Every element in Map m will be put into this new HashMap.
     * @param m a Map whose key / value pairs will be put into the new HashMap.
     * <b>NOTE: key / value pairs are not cloned in this constructor.</b>
     * @exception NullPointerException if m is null
     */
    public HashMap(Map m) {
        this(Math.max(m.size() * 2, DEFAULT_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    /**
     * Construct a new HashMap with a specific inital capacity and
     * default load factor of 0.75.
     * @param initialCapacity the initial capacity of this HashMap (&gt;=0)
     * @exception IllegalArgumentException if (initialCapacity &lt; 0)
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct a new HashMap with a specific inital capacity and load factor.
     * @param initialCapacity the initial capacity (&gt;=0)
     * @param loadFactor the load factor (&gt; 0, not NaN)
     * @exception IllegalArgumentException if (initialCapacity &lt; 0) ||
     * ! (loadFactor &gt; 0.0)
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: "
                    + initialCapacity);
        }
        if (!(loadFactor > 0)) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        }
        if (initialCapacity == 0) {
            initialCapacity = 1;
        }
        setBuckets(new HashEntry[initialCapacity]);
        this.loadFactor = loadFactor;
        $ASSIGN$threshold((int) (initialCapacity * loadFactor));
    }

    /**
     * Returns the number of kay-value mappings currently in this Map.
     * @return the size
     */
    public int size() {
        return getSize();
    }

    /**
     * Returns true if there are no key-value mappings currently in this Map.
     * @return <code>size() == 0</code>
     */
    public boolean isEmpty() {
        return getSize() == 0;
    }

    /**
     * Return the value in this HashMap associated with the supplied key,
     * or <code>null</code> if the key maps to nothing.  NOTE: Since the value
     * could also be null, you must use containsKey to see if this key
     * actually maps to something.
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
                return e.getValueField();
            }
            e = e.getNext();
        }
        return null;
    }

    /**
     * Returns true if the supplied object <code>equals()</code> a key
     * in this HashMap.
     * @param key the key to search for in this HashMap
     * @return true if the key is in the table
     * @see #containsValue(Object)
     */
    public boolean containsKey(Object key) {
        int idx = hash(key);
        HashEntry e = getBuckets()[idx];
        while (e != null) {
            if (equals(key, e.getKeyField())) {
                return true;
            }
            e = e.getNext();
        }
        return false;
    }

    /**
     * Puts the supplied value into the Map, mapped by the supplied key.
     * The value may be retrieved by any object which <code>equals()</code>
     * this key. NOTE: Since the prior value could also be null, you must
     * first use containsKey if you want to see if you are replacing the
     * key's mapping.
     * @param key the key used to locate the value
     * @param value the value to be stored in the HashMap
     * @return the prior mapping of the key, or null if there was none
     * @see #get(Object)
     * @see Object#equals(Object)
     */
    public Object put(Object key, Object value) {
        int idx = hash(key);
        HashEntry e = getBuckets()[idx];
        while (e != null) {
            if (equals(key, e.getKeyField())) {
                e.access();
                Object r = e.getValueField();
                e.setValueField(value);
                return r;
            } else {
                e = e.getNext();
            }
        }
        setModCount(getModCount() + 1);
        if (setSize(getSize() + 1) > threshold) {
            rehash();
            idx = hash(key);
        }
        addEntry(key, value, idx, true);
        return null;
    }

    /**
     * Copies all elements of the given map into this hashtable.  If this table
     * already has a mapping for a key, the new mapping replaces the current
     * one.
     * @param m the map to be hashed into this
     */
    public void putAll(Map m) {
        Iterator itr = m.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry e = (Map.Entry) itr.next();
            if (e instanceof AbstractMap.BasicMapEntry) {
                AbstractMap.BasicMapEntry entry = (AbstractMap.BasicMapEntry) e;
                put(entry.getKeyField(), entry.getValueField());
            } else {
                put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Removes from the HashMap and returns the value which is mapped by the
     * supplied key. If the key maps to nothing, then the HashMap remains
     * unchanged, and <code>null</code> is returned. NOTE: Since the value
     * could also be null, you must use containsKey to see if you are
     * actually removing a mapping.
     * @param key the key used to locate the value to remove
     * @return whatever the key mapped to, if present
     */
    public Object remove(Object key) {
        int idx = hash(key);
        HashEntry e = getBuckets()[idx];
        HashEntry last = null;
        while (e != null) {
            if (equals(key, e.getKeyField())) {
                setModCount(getModCount() + 1);
                if (last == null) {
                    getBuckets()[idx] = e.getNext();
                } else {
                    last.setNext(e.getNext());
                }
                setSize(getSize() - 1);
                return e.cleanup();
            }
            last = e;
            e = e.getNext();
        }
        return null;
    }

    /**
     * Clears the Map so it has no keys. This is O(1).
     */
    public void clear() {
        if (getSize() != 0) {
            setModCount(getModCount() + 1);
            Arrays.fill(getBuckets(), null);
            setSize(0);
        }
    }

    /**
     * Returns true if this HashMap contains a value <code>o</code>, such that
     * <code>o.equals(value)</code>.
     * @param value the value to search for in this HashMap
     * @return true if at least one key maps to the value
     * @see #containsKey(Object)
     */
    public boolean containsValue(Object value) {
        for (int i = getBuckets().length - 1; i >= 0; i--) {
            HashEntry e = getBuckets()[i];
            while (e != null) {
                if (equals(value, e.getValueField())) {
                    return true;
                }
                e = e.getNext();
            }
        }
        return false;
    }

    /**
     * Returns a shallow clone of this HashMap. The Map itself is cloned,
     * but its contents are not.  This is O(n).
     * @return the clone
     */
    public Object clone() {
        HashMap copy = null;
        try {
            copy = (HashMap) super.clone();
        } catch (CloneNotSupportedException x) {
        }
        copy.setBuckets(new HashEntry[getBuckets().length]);
        copy.putAllInternal(this);
        copy.$ASSIGN$entries(null);
        return copy;
    }

    /**
     * Returns a "set view" of this HashMap's keys. The set is backed by the
     * HashMap, so changes in one show up in the other.  The set supports
     * element removal, but not element addition.
     * @return a set view of the keys
     * @see #values()
     * @see #entrySet()
     */
    public Set keySet() {
        if (getKeys() == null) {
            setKeys(new AbstractSet() {
                public int size() {
                    return getSize();
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
                    int oldsize = getSize();
                    HashMap.this.remove(o);
                    return oldsize != getSize();
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
                    FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                            .getTopTimestamp());
                    super.$COMMIT(timestamp);
                }

                public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                    super.$RESTORE(timestamp, trim);
                }

                public final Checkpoint $GET$CHECKPOINT_ANONYMOUS() {
                    return $CHECKPOINT;
                }

                public final Object $SET$CHECKPOINT_ANONYMOUS(
                        Checkpoint checkpoint) {
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

                private transient FieldRecord[] $RECORDS = new FieldRecord[] {};

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }

            });
        }
        return getKeys();
    }

    /**
     * Returns a "collection view" (or "bag view") of this HashMap's values.
     * The collection is backed by the HashMap, so changes in one show up
     * in the other.  The collection supports element removal, but not element
     * addition.
     * @return a bag view of the values
     * @see #keySet()
     * @see #entrySet()
     */
    public Collection values() {
        if (getValues() == null) {
            setValues(new AbstractCollection() {
                public int size() {
                    return getSize();
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
                    FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                            .getTopTimestamp());
                    super.$COMMIT(timestamp);
                }

                public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                    super.$RESTORE(timestamp, trim);
                }

                public final Checkpoint $GET$CHECKPOINT_ANONYMOUS() {
                    return $CHECKPOINT;
                }

                public final Object $SET$CHECKPOINT_ANONYMOUS(
                        Checkpoint checkpoint) {
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

                private transient FieldRecord[] $RECORDS = new FieldRecord[] {};

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }

            });
        }
        return getValues();
    }

    /**
     * Returns a "set view" of this HashMap's entries. The set is backed by
     * the HashMap, so changes in one show up in the other.  The set supports
     * element removal, but not element addition.<p>
     * Note that the iterators for all three views, from keySet(), entrySet(),
     * and values(), traverse the HashMap in the same sequence.
     * @return a set view of the entries
     * @see #keySet()
     * @see #values()
     * @see Map.Entry
     */
    public Set entrySet() {
        if (entries == null) {
            $ASSIGN$entries(new AbstractSet() {
                public int size() {
                    return getSize();
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
                        HashMap.this.remove(e.getKeyField());
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
                    FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                            .getTopTimestamp());
                    super.$COMMIT(timestamp);
                }

                public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                    super.$RESTORE(timestamp, trim);
                }

                public final Checkpoint $GET$CHECKPOINT_ANONYMOUS() {
                    return $CHECKPOINT;
                }

                public final Object $SET$CHECKPOINT_ANONYMOUS(
                        Checkpoint checkpoint) {
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

                private transient FieldRecord[] $RECORDS = new FieldRecord[] {};

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }

            });
        }
        return entries;
    }

    /**
     * Helper method for put, that creates and adds a new Entry.  This is
     * overridden in LinkedHashMap for bookkeeping purposes.
     * @param key the key of the new Entry
     * @param value the value
     * @param idx the index in buckets where the new Entry belongs
     * @param callRemove whether to call the removeEldestEntry method
     * @see #put(Object, Object)
     */
    void addEntry(Object key, Object value, int idx, boolean callRemove) {
        HashEntry e = new HashEntry(key, value);
        e.setNext(getBuckets()[idx]);
        getBuckets()[idx] = e;
    }

    /**
     * Helper method for entrySet(), which matches both key and value
     * simultaneously.
     * @param o the entry to match
     * @return the matching entry, if found, or null
     * @see #entrySet()
     */
    final HashEntry getEntry(Object o) {
        if (!(o instanceof Map.Entry)) {
            return null;
        }
        Map.Entry me = (Map.Entry) o;
        Object key = me.getKey();
        int idx = hash(key);
        HashEntry e = getBuckets()[idx];
        while (e != null) {
            if (equals(e.getKeyField(), key)) {
                return equals(e.getValueField(), me.getValue()) ? e : null;
            }
            e = e.getNext();
        }
        return null;
    }

    /**
     * Helper method that returns an index in the buckets array for `key'
     * based on its hashCode().  Package visible for use by subclasses.
     * @param key the key
     * @return the bucket number
     */
    final int hash(Object key) {
        return key == null ? 0 : Math.abs(key.hashCode() % getBuckets().length);
    }

    /**
     * Generates a parameterized iterator.  Must be overrideable, since
     * LinkedHashMap iterates in a different order.
     * @param type {
    @link #KEYS    }
    , {
    @link #VALUES    }
    , or {
    @link #ENTRIES    }

     * @return the appropriate iterator
     */
    Iterator iterator(int type) {
        return new HashIterator(type);
    }

    /**
     * A simplified, more efficient internal implementation of putAll(). clone()
     * should not call putAll or put, in order to be compatible with the JDK
     * implementation with respect to subclasses.
     * @param m the map to initialize this from
     */
    void putAllInternal(Map m) {
        Iterator itr = m.entrySet().iterator();
        setSize(0);
        while (itr.hasNext()) {
            setSize(getSize() + 1);
            Map.Entry e = (Map.Entry) itr.next();
            Object key = e.getKey();
            int idx = hash(key);
            addEntry(key, e.getValue(), idx, false);
        }
    }

    /**
     * Increases the size of the HashMap and rehashes all keys to new
     * array indices; this is called when the addition of a new value
     * would cause size() &gt; threshold. Note that the existing Entry
     * objects are reused in the new hash table.
     * <p>This is not specified, but the new size is twice the current size
     * plus one; this number is not always prime, unfortunately.
     */
    private void rehash() {
        HashEntry[] oldBuckets = getBuckets();
        int newcapacity = (getBuckets().length * 2) + 1;
        $ASSIGN$threshold((int) (newcapacity * loadFactor));
        setBuckets(new HashEntry[newcapacity]);
        for (int i = oldBuckets.length - 1; i >= 0; i--) {
            HashEntry e = oldBuckets[i];
            while (e != null) {
                int idx = hash(e.getKeyField());
                HashEntry next = e.getNext();
                e.setNext(getBuckets()[idx]);
                getBuckets()[idx] = e;
                e = next;
            }
        }
    }

    /**
     * Serializes this object to the given stream.
     * @param s the stream to write to
     * @exception IOException if the underlying stream fails
     * @serialData the <i>capacity</i>(int) that is the length of the
     * bucket array, the <i>size</i>(int) of the hash map
     * are emitted first.  They are followed by size entries,
     * each consisting of a key (Object) and a value (Object).
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(getBuckets().length);
        s.writeInt(getSize());
        Iterator it = iterator(ENTRIES);
        while (it.hasNext()) {
            HashEntry entry = (HashEntry) it.next();
            s.writeObject(entry.getKeyField());
            s.writeObject(entry.getValueField());
        }
    }

    /**
     * Deserializes this object from the given stream.
     * @param s the stream to read from
     * @exception ClassNotFoundException if the underlying stream fails
     * @exception IOException if the underlying stream fails
     * @serialData the <i>capacity</i>(int) that is the length of the
     * bucket array, the <i>size</i>(int) of the hash map
     * are emitted first.  They are followed by size entries,
     * each consisting of a key (Object) and a value (Object).
     */
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        setBuckets(new HashEntry[s.readInt()]);
        int len = s.readInt();
        setSize(len);
        while (len-- > 0) {
            Object key = s.readObject();
            addEntry(key, s.readObject(), hash(key), false);
        }
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

    void setBuckets(HashEntry[] buckets) {
        this.$ASSIGN$buckets(buckets);
    }

    HashEntry[] getBuckets() {
        return $BACKUP$buckets();
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

    private final int $ASSIGN$size(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
        }
        return size = newValue;
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
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                .getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        threshold = $RECORD$threshold.restore(threshold, timestamp, trim);
        buckets = (HashEntry[]) $RECORD$buckets.restore(buckets, timestamp,
                trim);
        modCount = $RECORD$modCount.restore(modCount, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        entries = (Set) $RECORD$entries.restore(entries, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private transient FieldRecord $RECORD$threshold = new FieldRecord(0);

    private transient FieldRecord $RECORD$buckets = new FieldRecord(1);

    private transient FieldRecord $RECORD$modCount = new FieldRecord(0);

    private transient FieldRecord $RECORD$size = new FieldRecord(0);

    private transient FieldRecord $RECORD$entries = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$threshold, $RECORD$buckets, $RECORD$modCount, $RECORD$size,
            $RECORD$entries };

}
