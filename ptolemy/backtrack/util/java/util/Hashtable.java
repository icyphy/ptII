/* Hashtable.java -- a class providing a basic hashtable data structure,
 mapping Object --> Object
 Copyright (C) 1998, 1999, 2000, 2001, 2002 Free Software Foundation, Inc.

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

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.Collection;
import ptolemy.backtrack.util.java.util.Set;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// NOTE: This implementation is very similar to that of HashMap. If you fix
// a bug in here, chances are you should make a similar change to the HashMap
// code.
import java.io.Serializable;
import java.lang.Object;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A class which implements a hashtable data structure.
 * <p>
 * This implementation of Hashtable uses a hash-bucket approach. That is:
 * linear probing and rehashing is avoided; instead, each hashed value maps
 * to a simple linked-list which, in the best case, only has one node.
 * Assuming a large enough table, low enough load factor, and / or well
 * implemented hashCode() methods, Hashtable should provide O(1)
 * insertion, deletion, and searching of keys.  Hashtable is O(n) in
 * the worst case for all of these (if all keys hash to the same bucket).
 * <p>
 * This is a JDK-1.2 compliant implementation of Hashtable.  As such, it
 * belongs, partially, to the Collections framework (in that it implements
 * Map).  For backwards compatibility, it inherits from the obsolete and
 * utterly useless Dictionary class.
 * <p>
 * Being a hybrid of old and new, Hashtable has methods which provide redundant
 * capability, but with subtle and even crucial differences.
 * For example, one can iterate over various aspects of a Hashtable with
 * either an Iterator (which is the JDK-1.2 way of doing things) or with an
 * Enumeration.  The latter can end up in an undefined state if the Hashtable
 * changes while the Enumeration is open.
 * <p>
 * Unlike HashMap, Hashtable does not accept `null' as a key value. Also,
 * all accesses are synchronized: in a single thread environment, this is
 * expensive, but in a multi-thread environment, this saves you the effort
 * of extra synchronization. However, the old-style enumerators are not
 * synchronized, because they can lead to unspecified behavior even if
 * they were synchronized. You have been warned.
 * <p>
 * The iterators are <i>fail-fast</i>, meaning that any structural
 * modification, except for <code>remove()</code> called on the iterator
 * itself, cause the iterator to throw a
 * <code>ConcurrentModificationException</code> rather than exhibit
 * non-deterministic behavior.
 * @author Jon Zeppieri
 * @author Warren Levy
 * @author Bryce McKinlay
 * @author Eric Blake <ebb9@email.byu.edu>
 * @see HashMap
 * @see TreeMap
 * @see IdentityHashMap
 * @see LinkedHashMap
 * @since 1.0
 * @status updated to 1.4
 */
public class Hashtable extends Dictionary implements Map, Cloneable,
        Serializable, Rollbackable {
    protected Checkpoint $CHECKPOINT = new Checkpoint(this);

    // WARNING: Hashtable is a CORE class in the bootstrap cycle. See the
    // comments in vm/reference/java/lang/Runtime for implications of this fact.

    /**
     * Default number of buckets. This is the value the JDK 1.3 uses. Some
     * early documentation specified this value as 101. That is incorrect.
     */
    private static final int DEFAULT_CAPACITY = 11;

    /**
     * An "enum" of iterator types.
     */

    // Package visible for use by nested classes.
    static final int KEYS = 0;

    /**
     * An "enum" of iterator types.
     */

    // Package visible for use by nested classes.
    static final int VALUES = 1;

    /**
     * An "enum" of iterator types.
     */

    // Package visible for use by nested classes.
    static final int ENTRIES = 2;

    /**
     * The default load factor; this is explicitly specified by the spec.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Compatible with JDK 1.0+.
     */
    private static final long serialVersionUID = 1421746759512286392L;

    /**
     * The rounded product of the capacity and the load factor; when the number
     * of elements exceeds the threshold, the Hashtable calls
     * <code>rehash()</code>.
     * @serial
     */
    private int threshold;

    /**
     * Load factor of this Hashtable:  used in computing the threshold.
     * @serial
     */
    private final float loadFactor;

    /**
     * Array containing the actual key-value mappings.
     */

    // Package visible for use by nested classes.
    private transient HashEntry[] buckets;

    /**
     * Counts the number of modifications this Hashtable has undergone, used
     * by Iterators to know when to throw ConcurrentModificationExceptions.
     */

    // Package visible for use by nested classes.
    private transient int modCount;

    /**
     * The size of this Hashtable:  denotes the number of key-value pairs.
     */

    // Package visible for use by nested classes.
    private transient int size;

    /**
     * The cache for {
     @link #keySet()    }
     .
     */
    private transient Set keys;

    /**
     * The cache for {
     @link #values()    }
     .
     */
    private transient Collection values;

    /**
     * The cache for {
     @link #entrySet()    }
     .
     */
    private transient Set entries;

    /**
     * Class to represent an entry in the hash table. Holds a single key-value
     * pair. A Hashtable Entry is identical to a HashMap Entry, except that
     * `null' is not allowed for keys and values.
     */
    private static final class HashEntry extends AbstractMap.BasicMapEntry
            implements Rollbackable {
        /**
         * The next entry in the linked list.
         */
        private HashEntry next;

        /**
         * Simple constructor.
         * @param key the key, already guaranteed non-null
         * @param value the value, already guaranteed non-null
         */
        HashEntry(Object key, Object value) {
            super(key, value);
        }

        /**
         * Resets the value.
         * @param newValue the new value
         * @return the prior value
         * @throws NullPointerException if <code>newVal</code> is null
         */
        public Object setValue(Object newVal) {
            if (newVal == null) {
                throw new NullPointerException();
            }

            return super.setValue(newVal);
        }

        /**
         * @param next The next to set.
         */
        void setNext(HashEntry next) {
            this.$ASSIGN$next(next);
        }

        /**
         * @return Returns the next.
         */
        HashEntry getNext() {
            return next;
        }

        private final HashEntry $ASSIGN$next(HashEntry newValue) {
            if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
                $RECORD$next.add(null, next, $CHECKPOINT.getTimestamp());
            }

            if ((newValue != null)
                    && ($CHECKPOINT != newValue.$GET$CHECKPOINT())) {
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

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] { $RECORD$next };
    }

    // check for NaN too
    // Must throw on null argument even if the table is empty

    /**         // Delegate to older method to make sure code overriding it continues
     // to work.

     *     // Check if value is null since it is not permitted.
     A class which implements the Iterator interface and is used for    // Bypass e.setValue, since we already know value is non-null.

     *     // At this point, we know we need to add a new entry.
     iterating over Hashtables.    // Need a new hash value to suit the bigger table.

     *     // Optimize in case the Entry is one of our own.
     This implementation is parameterized to give a sequential view of    // This is impossible.

     *     // Clear the caches.
     keys, values, or entries; it also allows the removal of elements,    // Since we are already synchronized, and entrySet().iterator()
     // would repeatedly re-lock/release the monitor, we directly use the
     // unsynchronized HashIterator instead.

     *     // Create a synchronized AbstractSet with custom implementations of
     // those methods that can be overridden easily and efficiently.
     as per the Javasoft spec.  Note that it is not synchronized; this is    // We must specify the correct object to synchronize upon, hence the
     // use of a non-public API

     *     // We don't bother overriding many of the optional methods, as doing so
     // wouldn't provide any significant performance advantage.
     a performance enhancer since it is never exposed externally and is    // We must specify the correct object to synchronize upon, hence the
     // use of a non-public API

     *     // Create an AbstractSet with custom implementations of those methods
     // that can be overridden easily and efficiently.
     only used within synchronized blocks above.    // We must specify the correct object to synchronize upon, hence the
     // use of a non-public API
     // no need to synchronize, entrySet().equals() does that
     // Since we are already synchronized, and entrySet().iterator()
     // would repeatedly re-lock/release the monitor, we directly use the
     // unsynchronized HashIterator instead.

     * @author    // Note: Inline Math.abs here, for less method overhead, and to avoid
     // a bootstrap dependency, since Math relies on native methods.
     Jon Zeppieri    // Package visible, for use in nested classes.
     // Write the threshold and loadFactor fields.

     */

    // Since we are already synchronized, and entrySet().iterator()
    // would repeatedly re-lock/release the monitor, we directly use the
    // unsynchronized HashIterator instead.
    private final class // Read the threshold and loadFactor fields.
    HashIterator // Read and use capacity.
            implements Iterator, Rollbackable {
        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

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
        final int type;

        /**
         * The number of modifications to the backing Hashtable that we know about.
         */
        private int knownMod = modCount;

        /**
         * The number of elements remaining to be returned by next().
         */
        private int count = getSize();

        /**
         * Current index in the physical hash table.
         */
        private int idx = buckets.length;

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
         * @throws ConcurrentModificationException if the hashtable was modified
         */
        public boolean hasNext() {
            if (getKnownMod() != modCount) {
                throw new ConcurrentModificationException();
            }

            return getCount() > 0;
        }

        /**
         * Returns the next element in the Iterator's sequential view.
         * @return the next element
         * @throws ConcurrentModificationException if the hashtable was modified
         * @throws NoSuchElementException if there is none
         */
        public Object next() {
            if (getKnownMod() != modCount) {
                throw new ConcurrentModificationException();
            }

            if (getCount() == 0) {
                throw new NoSuchElementException();
            }

            setCount(getCount() - 1);

            HashEntry e = getNext();

            while (e == null) {
                e = buckets[setIdx(getIdx() - 1)];
            }

            setNext(e.getNext());
            setLast(e);

            if (type == VALUES) {
                return e.getValue();
            }

            if (type == KEYS) {
                return e.getKey();
            }

            return e;
        }

        /**
         * Removes from the backing Hashtable the last element which was fetched
         * with the <code>next()</code> method.
         * @throws ConcurrentModificationException if the hashtable was modified
         * @throws IllegalStateException if called when there is no last element
         */
        public void remove() {
            if (getKnownMod() != modCount) {
                throw new ConcurrentModificationException();
            }

            if (getLast() == null) {
                throw new IllegalStateException();
            }

            Hashtable.this.remove(getLast().getKey());
            setLast(null);
            setKnownMod(getKnownMod() + 1);
        }

        /**
         * @param knownMod The knownMod to set.
         */
        void setKnownMod(int knownMod) {
            this.$ASSIGN$knownMod(knownMod);
        }

        /**
         * @return Returns the knownMod.
         */
        int getKnownMod() {
            return knownMod;
        }

        /**
         * @param count The count to set.
         */
        void setCount(int count) {
            this.$ASSIGN$count(count);
        }

        /**
         * @return Returns the count.
         */
        int getCount() {
            return count;
        }

        /**
         * @param idx The idx to set.
         */
        int setIdx(int idx) {
            return this.$ASSIGN$idx(idx);
        }

        /**
         * @return Returns the idx.
         */
        int getIdx() {
            return idx;
        }

        /**
         * @param last The last to set.
         */
        void setLast(HashEntry last) {
            this.$ASSIGN$last(last);
        }

        /**
         * @return Returns the last.
         */
        HashEntry getLast() {
            return last;
        }

        /**
         * @param next The next to set.
         */
        void setNext(HashEntry next) {
            this.$ASSIGN$next(next);
        }

        /**
         * @return Returns the next.
         */
        HashEntry getNext() {
            return next;
        }

        private final int $ASSIGN$knownMod(int newValue) {
            if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
                $RECORD$knownMod
                        .add(null, knownMod, $CHECKPOINT.getTimestamp());
            }

            return knownMod = newValue;
        }

        private final int $ASSIGN$count(int newValue) {
            if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
                $RECORD$count.add(null, count, $CHECKPOINT.getTimestamp());
            }

            return count = newValue;
        }

        private final int $ASSIGN$idx(int newValue) {
            if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
                $RECORD$idx.add(null, idx, $CHECKPOINT.getTimestamp());
            }

            return idx = newValue;
        }

        private final HashEntry $ASSIGN$last(HashEntry newValue) {
            if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
                $RECORD$last.add(null, last, $CHECKPOINT.getTimestamp());
            }

            if ((newValue != null)
                    && ($CHECKPOINT != newValue.$GET$CHECKPOINT())) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }

            return last = newValue;
        }

        private final HashEntry $ASSIGN$next(HashEntry newValue) {
            if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
                $RECORD$next.add(null, next, $CHECKPOINT.getTimestamp());
            }

            if ((newValue != null)
                    && ($CHECKPOINT != newValue.$GET$CHECKPOINT())) {
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

        protected CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

        private FieldRecord $RECORD$knownMod = new FieldRecord(0);

        private FieldRecord $RECORD$count = new FieldRecord(0);

        private FieldRecord $RECORD$idx = new FieldRecord(0);

        private FieldRecord $RECORD$last = new FieldRecord(0);

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] { $RECORD$knownMod,
                $RECORD$count, $RECORD$idx, $RECORD$last, $RECORD$next };
    }

    // class HashIterator

    /**
     * Enumeration view of this Hashtable, providing sequential access to its
     * elements; this implementation is parameterized to provide access either
     * to the keys or to the values in the Hashtable.
     * <b>NOTE</b>: Enumeration is not safe if new elements are put in the table
     * as this could cause a rehash and we'd completely lose our place.  Even
     * without a rehash, it is undetermined if a new element added would
     * appear in the enumeration.  The spec says nothing about this, but
     * the "Java Class Libraries" book infers that modifications to the
     * hashtable during enumeration causes indeterminate results.  Don't do it!
     * @author Jon Zeppieri
     */
    private final class Enumerator implements Enumeration, Rollbackable {
        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        /**
         * The type of this Iterator: {
         @link #KEYS        }
         or {
         @link #VALUES        }
         .
         */
        final int type;

        /**
         * The number of elements remaining to be returned by next().
         */
        private int count = getSize();

        /**
         * Current index in the physical hash table.
         */
        private int idx = buckets.length;

        /**
         * Entry which will be returned by the next nextElement() call. It is
         * set if we are iterating through a bucket with multiple entries, or null
         * if we must look in the next bucket.
         */
        private HashEntry next;

        /**
         * Construct the enumeration.
         * @param type either {
         @link #KEYS        }
         or {
         @link #VALUES        }
         .
         */
        Enumerator(int type) {
            this.type = type;
        }

        /**
         * Checks whether more elements remain in the enumeration.
         * @return true if nextElement() will not fail.
         */
        public boolean hasMoreElements() {
            return getCount() > 0;
        }

        /**
         * Returns the next element.
         * @return the next element
         * @throws NoSuchElementException if there is none.
         */
        public Object nextElement() {
            if (getCount() == 0) {
                throw new NoSuchElementException("Hashtable Enumerator");
            }

            setCount(getCount() - 1);

            HashEntry e = getNext();

            while (e == null) {
                e = buckets[setIdx(getIdx() - 1)];
            }

            setNext(e.getNext());
            return (type == VALUES) ? e.getValue() : e.getKey();
        }

        /**
         * @param count The count to set.
         */
        void setCount(int count) {
            this.$ASSIGN$count(count);
        }

        /**
         * @return Returns the count.
         */
        int getCount() {
            return count;
        }

        /**
         * @param idx The idx to set.
         */
        int setIdx(int idx) {
            return this.$ASSIGN$idx(idx);
        }

        /**
         * @return Returns the idx.
         */
        int getIdx() {
            return idx;
        }

        /**
         * @param next The next to set.
         */
        void setNext(HashEntry next) {
            this.$ASSIGN$next(next);
        }

        /**
         * @return Returns the next.
         */
        HashEntry getNext() {
            return next;
        }

        private final int $ASSIGN$count(int newValue) {
            if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
                $RECORD$count.add(null, count, $CHECKPOINT.getTimestamp());
            }

            return count = newValue;
        }

        private final int $ASSIGN$idx(int newValue) {
            if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
                $RECORD$idx.add(null, idx, $CHECKPOINT.getTimestamp());
            }

            return idx = newValue;
        }

        private final HashEntry $ASSIGN$next(HashEntry newValue) {
            if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
                $RECORD$next.add(null, next, $CHECKPOINT.getTimestamp());
            }

            if ((newValue != null)
                    && ($CHECKPOINT != newValue.$GET$CHECKPOINT())) {
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
            count = $RECORD$count.restore(count, timestamp, trim);
            idx = $RECORD$idx.restore(idx, timestamp, trim);
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

        protected CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

        private FieldRecord $RECORD$count = new FieldRecord(0);

        private FieldRecord $RECORD$idx = new FieldRecord(0);

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] { $RECORD$count,
                $RECORD$idx, $RECORD$next };
    }

    // class Enumerator

    /**
     * Construct a new Hashtable with the default capacity (11) and the default
     * load factor (0.75).
     */
    public Hashtable() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct a new Hashtable from the given Map, with initial capacity
     * the greater of the size of <code>m</code> or the default of 11.
     * <p>
     * Every element in Map m will be put into this new Hashtable.
     * @param m a Map whose key / value pairs will be put into
     * the new Hashtable.  <b>NOTE: key / value pairs
     * are not cloned in this constructor.</b>
     * @throws NullPointerException if m is null, or if m contains a mapping
     * to or from `null'.
     * @since 1.2
     */
    public Hashtable(Map m) {
        this(Math.max(m.size() * 2, DEFAULT_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    /**
     * Construct a new Hashtable with a specific inital capacity and
     * default load factor of 0.75.
     * @param initialCapacity the initial capacity of this Hashtable (&gt;= 0)
     * @throws IllegalArgumentException if (initialCapacity &lt; 0)
     */
    public Hashtable(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct a new Hashtable with a specific initial capacity and
     * load factor.
     * @param initialCapacity the initial capacity (&gt;= 0)
     * @param loadFactor the load factor (&gt; 0, not NaN)
     * @throws IllegalArgumentException if (initialCapacity &lt; 0) ||
     * ! (loadFactor &gt; 0.0)
     */
    public Hashtable(int initialCapacity, float loadFactor) {
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

        $ASSIGN$buckets(new HashEntry[initialCapacity]);
        this.loadFactor = loadFactor;
        $ASSIGN$threshold((int) (initialCapacity * loadFactor));
    }

    /**
     * Returns the number of key-value mappings currently in this hashtable.
     * @return the size
     */
    public synchronized int size() {
        return getSize();
    }

    /**
     * Returns true if there are no key-value mappings currently in this table.
     * @return <code>size() == 0</code>
     */
    public synchronized boolean isEmpty() {
        return getSize() == 0;
    }

    /**
     * Return an enumeration of the keys of this table. There's no point
     * in synchronizing this, as you have already been warned that the
     * enumeration is not specified to be thread-safe.
     * @return the keys
     * @see #elements()
     * @see #keySet()
     */
    public Enumeration keys() {
        return new Enumerator(KEYS);
    }

    /**
     * Return an enumeration of the values of this table. There's no point
     * in synchronizing this, as you have already been warned that the
     * enumeration is not specified to be thread-safe.
     * @return the values
     * @see #keys()
     * @see #values()
     */
    public Enumeration elements() {
        return new Enumerator(VALUES);
    }

    /**
     * Returns true if this Hashtable contains a value <code>o</code>,
     * such that <code>o.equals(value)</code>.  This is the same as
     * <code>containsValue()</code>, and is O(n).
     * <p>
     * @param value the value to search for in this Hashtable
     * @return true if at least one key maps to the value
     * @throws NullPointerException if <code>value</code> is null
     * @see #containsValue(Object)
     * @see #containsKey(Object)
     */
    public synchronized boolean contains(Object value) {
        for (int i = buckets.length - 1; i >= 0; i--) {
            HashEntry e = buckets[i];

            while (e != null) {
                if (value.equals(e.getValue())) {
                    return true;
                }

                e = e.getNext();
            }
        }

        if (value == null) {
            throw new NullPointerException();
        }

        return false;
    }

    /**
     * Returns true if this Hashtable contains a value <code>o</code>, such that
     * <code>o.equals(value)</code>. This is the new API for the old
     * <code>contains()</code>.
     * @param value the value to search for in this Hashtable
     * @return true if at least one key maps to the value
     * @see #contains(Object)
     * @see #containsKey(Object)
     * @throws NullPointerException if <code>value</code> is null
     * @since 1.2
     */
    public boolean containsValue(Object value) {
        return contains(value);
    }

    /**
     * Returns true if the supplied object <code>equals()</code> a key
     * in this Hashtable.
     * @param key the key to search for in this Hashtable
     * @return true if the key is in the table
     * @throws NullPointerException if key is null
     * @see #containsValue(Object)
     */
    public synchronized boolean containsKey(Object key) {
        int idx = hash(key);
        HashEntry e = buckets[idx];

        while (e != null) {
            if (key.equals(e.getKey())) {
                return true;
            }

            e = e.getNext();
        }

        return false;
    }

    /**
     * Return the value in this Hashtable associated with the supplied key,
     * or <code>null</code> if the key maps to nothing.
     * @param key the key for which to fetch an associated value
     * @return what the key maps to, if present
     * @throws NullPointerException if key is null
     * @see #put(Object, Object)
     * @see #containsKey(Object)
     */
    public synchronized Object get(Object key) {
        int idx = hash(key);
        HashEntry e = buckets[idx];

        while (e != null) {
            if (key.equals(e.getKey())) {
                return e.getValue();
            }

            e = e.getNext();
        }

        return null;
    }

    /**
     * Puts the supplied value into the Map, mapped by the supplied key.
     * Neither parameter may be null.  The value may be retrieved by any
     * object which <code>equals()</code> this key.
     * @param key the key used to locate the value
     * @param value the value to be stored in the table
     * @return the prior mapping of the key, or null if there was none
     * @throws NullPointerException if key or value is null
     * @see #get(Object)
     * @see Object#equals(Object)
     */
    public synchronized Object put(Object key, Object value) {
        int idx = hash(key);
        HashEntry e = buckets[idx];

        if (value == null) {
            throw new NullPointerException();
        }

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

    /**
     * Removes from the table and returns the value which is mapped by the
     * supplied key. If the key maps to nothing, then the table remains
     * unchanged, and <code>null</code> is returned.
     * @param key the key used to locate the value to remove
     * @return whatever the key mapped to, if present
     */
    public synchronized Object remove(Object key) {
        int idx = hash(key);
        HashEntry e = buckets[idx];
        HashEntry last = null;

        while (e != null) {
            if (key.equals(e.getKey())) {
                $ASSIGN$SPECIAL$modCount(11, modCount);

                if (last == null) {
                    $ASSIGN$buckets(idx, e.getNext());
                } else {
                    last.setNext(e.getNext());
                }

                setSize(getSize() - 1);
                return e.getValue();
            }

            last = e;
            e = e.getNext();
        }

        return null;
    }

    /**
     * Copies all elements of the given map into this hashtable.  However, no
     * mapping can contain null as key or value.  If this table already has
     * a mapping for a key, the new mapping replaces the current one.
     * @param m the map to be hashed into this
     * @throws NullPointerException if m is null, or contains null keys or values
     */
    public synchronized void putAll(Map m) {
        Iterator itr = m.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry e = (Map.Entry) itr.next();

            if (e instanceof AbstractMap.BasicMapEntry) {
                AbstractMap.BasicMapEntry entry = (AbstractMap.BasicMapEntry) e;
                put(entry.getKey(), entry.getValue());
            } else {
                put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Clears the hashtable so it has no keys.  This is O(1).
     */
    public synchronized void clear() {
        if (getSize() > 0) {
            $ASSIGN$SPECIAL$modCount(11, modCount);
            Arrays.fill($BACKUP$buckets(), null);
            setSize(0);
        }
    }

    /**
     * Returns a shallow clone of this Hashtable. The Map itself is cloned,
     * but its contents are not.  This is O(n).
     * @return the clone
     */
    public synchronized Object clone() {
        Hashtable copy = null;

        try {
            copy = (Hashtable) super.clone();
        } catch (CloneNotSupportedException x) {
        }

        copy.$ASSIGN$buckets(new HashEntry[buckets.length]);
        copy.putAllInternal(this);
        copy.$ASSIGN$keys(null);
        copy.$ASSIGN$values(null);
        copy.$ASSIGN$entries(null);
        return copy;
    }

    /**
     * Converts this Hashtable to a String, surrounded by braces, and with
     * key/value pairs listed with an equals sign between, separated by a
     * comma and space. For example, <code>"{a=1, b=2}"</code>.<p>
     * NOTE: if the <code>toString()</code> method of any key or value
     * throws an exception, this will fail for the same reason.
     * @return the string representation
     */
    public synchronized String toString() {
        Iterator entries = new HashIterator(ENTRIES);
        StringBuffer r = new StringBuffer("{");

        for (int pos = getSize(); pos > 0; pos--) {
            r.append(entries.next());

            if (pos > 1) {
                r.append(", ");
            }
        }

        r.append("}");
        return r.toString();
    }

    /**
     * Returns a "set view" of this Hashtable's keys. The set is backed by
     * the hashtable, so changes in one show up in the other.  The set supports
     * element removal, but not element addition.  The set is properly
     * synchronized on the original hashtable.  Sun has not documented the
     * proper interaction of null with this set, but has inconsistent behavior
     * in the JDK. Therefore, in this implementation, contains, remove,
     * containsAll, retainAll, removeAll, and equals just ignore a null key
     * rather than throwing a {
     @link NullPointerException    }
     .
     * @return a set view of the keys
     * @see #values()
     * @see #entrySet()
     * @since 1.2
     */
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
                    if (o == null) {
                        return false;
                    }

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

                private FieldRecord[] $RECORDS = new FieldRecord[] {};

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }
            };

            $ASSIGN$keys(new Collections.SynchronizedSet(this, r));
        }

        return keys;
    }

    /**
     * Returns a "collection view" (or "bag view") of this Hashtable's values.
     * The collection is backed by the hashtable, so changes in one show up
     * in the other.  The collection supports element removal, but not element
     * addition.  The collection is properly synchronized on the original
     * hashtable.  Sun has not documented the proper interaction of null with
     * this set, but has inconsistent behavior in the JDK. Therefore, in this
     * implementation, contains, remove, containsAll, retainAll, removeAll, and
     * equals just ignore a null value rather than throwing a{
     @link NullPointerException    }
     .
     * @return a bag view of the values
     * @see #keySet()
     * @see #entrySet()
     * @since 1.2
     */
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

                private FieldRecord[] $RECORDS = new FieldRecord[] {};

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }
            };

            $ASSIGN$values(new Collections.SynchronizedCollection(this, r));
        }

        return values;
    }

    /**
     * Returns a "set view" of this Hashtable's entries. The set is backed by
     * the hashtable, so changes in one show up in the other.  The set supports
     * element removal, but not element addition.  The set is properly
     * synchronized on the original hashtable.  Sun has not documented the
     * proper interaction of null with this set, but has inconsistent behavior
     * in the JDK. Therefore, in this implementation, contains, remove,
     * containsAll, retainAll, removeAll, and equals just ignore a null entry,
     * or an entry with a null key or value, rather than throwing a{
     @link NullPointerException    }
     . However, calling entry.setValue(null)
     * will fail.
     * <p>
     * Note that the iterators for all three views, from keySet(), entrySet(),
     * and values(), traverse the hashtable in the same sequence.
     * @return a set view of the entries
     * @see #keySet()
     * @see #values()
     * @see Map.Entry
     * @since 1.2
     */
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

                private FieldRecord[] $RECORDS = new FieldRecord[] {};

                {
                    $CHECKPOINT.addObject(new _PROXY_());
                }
            };

            $ASSIGN$entries(new Collections.SynchronizedSet(this, r));
        }

        return entries;
    }

    /**
     * Returns true if this Hashtable equals the supplied Object <code>o</code>.
     * As specified by Map, this is:
     * <code>
     * (o instanceof Map) && entrySet().equals(((Map) o).entrySet());
     * </code>
     * @param o the object to compare to
     * @return true if o is an equal map
     * @since 1.2
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Map)) {
            return false;
        }

        return entrySet().equals(((Map) o).entrySet());
    }

    /**
     * Returns the hashCode for this Hashtable.  As specified by Map, this is
     * the sum of the hashCodes of all of its Map.Entry objects
     * @return the sum of the hashcodes of the entries
     * @since 1.2
     */
    public synchronized int hashCode() {
        Iterator itr = new HashIterator(ENTRIES);
        int hashcode = 0;

        for (int pos = getSize(); pos > 0; pos--) {
            hashcode += itr.next().hashCode();
        }

        return hashcode;
    }

    /**
     * Helper method that returns an index in the buckets array for `key'
     * based on its hashCode().
     * @param key the key
     * @return the bucket number
     * @throws NullPointerException if key is null
     */
    private int hash(Object key) {
        int hash = key.hashCode() % buckets.length;
        return (hash < 0) ? (-hash) : hash;
    }

    /**
     * Helper method for entrySet(), which matches both key and value
     * simultaneously. Ignores null, as mentioned in entrySet().
     * @param o the entry to match
     * @return the matching entry, if found, or null
     * @see #entrySet()
     */
    HashEntry getEntry(Object o) {
        if (!(o instanceof Map.Entry)) {
            return null;
        }

        Object key = ((Map.Entry) o).getKey();

        if (key == null) {
            return null;
        }

        int idx = hash(key);
        HashEntry e = buckets[idx];

        while (e != null) {
            if (o.equals(e)) {
                return e;
            }

            e = e.getNext();
        }

        return null;
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
            HashEntry he = new HashEntry(key, e.getValue());
            he.setNext(buckets[idx]);
            $ASSIGN$buckets(idx, he);
        }
    }

    /**
     * Increases the size of the Hashtable and rehashes all keys to new array
     * indices; this is called when the addition of a new value would cause
     * size() &gt; threshold. Note that the existing Entry objects are reused in
     * the new hash table.
     * <p>
     * This is not specified, but the new size is twice the current size plus
     * one; this number is not always prime, unfortunately. This implementation
     * is not synchronized, as it is only invoked from synchronized methods.
     */
    protected void rehash() {
        HashEntry[] oldBuckets = $BACKUP$buckets();
        int newcapacity = (buckets.length * 2) + 1;
        $ASSIGN$threshold((int) (newcapacity * loadFactor));
        $ASSIGN$buckets(new HashEntry[newcapacity]);

        for (int i = oldBuckets.length - 1; i >= 0; i--) {
            HashEntry e = oldBuckets[i];

            while (e != null) {
                int idx = hash(e.getKey());
                HashEntry dest = buckets[idx];

                if (dest != null) {
                    while (dest.getNext() != null) {
                        dest = dest.getNext();
                    }

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

    /**
     * Serializes this object to the given stream.
     * @param s the stream to write to
     * @throws IOException if the underlying stream fails
     * @serialData the <i>capacity</i> (int) that is the length of the
     * bucket array, the <i>size</i> (int) of the hash map
     * are emitted first.  They are followed by size entries,
     * each consisting of a key (Object) and a value (Object).
     */
    private synchronized void writeObject(ObjectOutputStream s)
            throws IOException {
        s.defaultWriteObject();
        s.writeInt(buckets.length);
        s.writeInt(getSize());

        Iterator it = new HashIterator(ENTRIES);

        while (it.hasNext()) {
            HashEntry entry = (HashEntry) it.next();
            s.writeObject(entry.getKey());
            s.writeObject(entry.getKey());
        }
    }

    /**
     * Deserializes this object from the given stream.
     * @param s the stream to read from
     * @throws ClassNotFoundException if the underlying stream fails
     * @throws IOException if the underlying stream fails
     * @serialData the <i>capacity</i> (int) that is the length of the
     * bucket array, the <i>size</i> (int) of the hash map
     * are emitted first.  They are followed by size entries,
     * each consisting of a key (Object) and a value (Object).
     */
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        $ASSIGN$buckets(new HashEntry[s.readInt()]);

        int len = s.readInt();

        while (--len >= 0) {
            put(s.readObject(), s.readObject());
        }
    }

    /**
     * @param buckets The buckets to set.
     */
    void setBuckets(HashEntry[] buckets) {
        this.$ASSIGN$buckets(buckets);
    }

    /**
     * @return Returns the buckets.
     */
    HashEntry[] getBuckets() {
        return $BACKUP$buckets();
    }

    /**
     * @param modCount The modCount to set.
     */
    void setModCount(int modCount) {
        this.$ASSIGN$modCount(modCount);
    }

    /**
     * @return Returns the modCount.
     */
    int getModCount() {
        return modCount;
    }

    /**
     * @param size The size to set.
     */
    int setSize(int size) {
        return this.$ASSIGN$size(size);
    }

    /**
     * @return Returns the size.
     */
    int getSize() {
        return size;
    }

    private final int $ASSIGN$threshold(int newValue) {
        if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
            $RECORD$threshold.add(null, threshold, $CHECKPOINT.getTimestamp());
        }

        return threshold = newValue;
    }

    private final HashEntry[] $ASSIGN$buckets(HashEntry[] newValue) {
        if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
            $RECORD$buckets.add(null, buckets, $CHECKPOINT.getTimestamp());
        }

        return buckets = newValue;
    }

    private final HashEntry $ASSIGN$buckets(int index0, HashEntry newValue) {
        if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
            $RECORD$buckets.add(new int[] { index0 }, buckets[index0],
                    $CHECKPOINT.getTimestamp());
        }

        if ((newValue != null) && ($CHECKPOINT != newValue.$GET$CHECKPOINT())) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }

        return buckets[index0] = newValue;
    }

    private final HashEntry[] $BACKUP$buckets() {
        $RECORD$buckets.backup(null, buckets, $CHECKPOINT.getTimestamp());
        return buckets;
    }

    private final int $ASSIGN$modCount(int newValue) {
        if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
            $RECORD$modCount.add(null, modCount, $CHECKPOINT.getTimestamp());
        }

        return modCount = newValue;
    }

    private final int $ASSIGN$SPECIAL$modCount(int operator, long newValue) {
        if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
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
        if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
            $RECORD$size.add(null, size, $CHECKPOINT.getTimestamp());
        }

        return size = newValue;
    }

    private final Set $ASSIGN$keys(Set newValue) {
        if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
            $RECORD$keys.add(null, keys, $CHECKPOINT.getTimestamp());
        }

        if ((newValue != null) && ($CHECKPOINT != newValue.$GET$CHECKPOINT())) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }

        return keys = newValue;
    }

    private final Collection $ASSIGN$values(Collection newValue) {
        if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
            $RECORD$values.add(null, values, $CHECKPOINT.getTimestamp());
        }

        if ((newValue != null) && ($CHECKPOINT != newValue.$GET$CHECKPOINT())) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }

        return values = newValue;
    }

    private final Set $ASSIGN$entries(Set newValue) {
        if (($CHECKPOINT != null) && ($CHECKPOINT.getTimestamp() > 0)) {
            $RECORD$entries.add(null, entries, $CHECKPOINT.getTimestamp());
        }

        if ((newValue != null) && ($CHECKPOINT != newValue.$GET$CHECKPOINT())) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }

        return entries = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                .getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        threshold = $RECORD$threshold.restore(threshold, timestamp, trim);
        buckets = (HashEntry[]) $RECORD$buckets.restore(buckets, timestamp,
                trim);
        modCount = $RECORD$modCount.restore(modCount, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        keys = (Set) $RECORD$keys.restore(keys, timestamp, trim);
        values = (Collection) $RECORD$values.restore(values, timestamp, trim);
        entries = (Set) $RECORD$entries.restore(entries, timestamp, trim);

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

    private FieldRecord[] $RECORDS = new FieldRecord[] { $RECORD$threshold,
            $RECORD$loadFactor, $RECORD$buckets, $RECORD$modCount,
            $RECORD$size, $RECORD$keys, $RECORD$values, $RECORD$entries };
}

// class Hashtable
