/* TreeMap.java -- a class providing a basic Red-Black Tree data structure,
   mapping Object --> Object
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
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;

/**
 * This class provides a red-black tree implementation of the SortedMap
 * interface.  Elements in the Map will be sorted by either a user-provided
 * Comparator object, or by the natural ordering of the keys.
 * The algorithms are adopted from Corman, Leiserson, and Rivest's
 * <i>Introduction to Algorithms.</i>  TreeMap guarantees O(log n)
 * insertion and deletion of elements.  That being said, there is a large
 * enough constant coefficient in front of that "log n" (overhead involved
 * in keeping the tree balanced), that TreeMap may not be the best choice
 * for small collections. If something is already sorted, you may want to
 * just use a LinkedHashMap to maintain the order while providing O(1) access.
 * TreeMap is a part of the JDK1.2 Collections API.  Null keys are allowed
 * only if a Comparator is used which can deal with them; natural ordering
 * cannot cope with null.  Null values are always allowed. Note that the
 * ordering must be <i>consistent with equals</i> to correctly implement
 * the Map interface. If this condition is violated, the map is still
 * well-behaved, but you may have suprising results when comparing it to
 * other maps.<p>
 * This implementation is not synchronized. If you need to share this between
 * multiple threads, do something like:<br>
 * <code>SortedMap m
 * = Collections.synchronizedSortedMap(new TreeMap(...));</code><p>
 * The iterators are <i>fail-fast</i>, meaning that any structural
 * modification, except for <code>remove()</code> called on the iterator
 * itself, cause the iterator to throw a
 * <code>ConcurrentModificationException</code> rather than exhibit
 * non-deterministic behavior.
 * @author Jon Zeppieri
 * @author Bryce McKinlay
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see Map
 * @see HashMap
 * @see Hashtable
 * @see LinkedHashMap
 * @see Comparable
 * @see Comparator
 * @see Collection
 * @see Collections#synchronizedSortedMap(SortedMap)
 * @since 1.2
 * @status updated to 1.4
 */
public class TreeMap extends AbstractMap implements SortedMap, Cloneable,
        Serializable, Rollbackable {

    // Implementation note:
    // A red-black tree is a binary search tree with the additional properties
    // that all paths to a leaf node visit the same number of black nodes,
    // and no red node has red children. To avoid some null-pointer checks,
    // we use the special node nil which is always black, has no relatives,
    // and has key and value of null (but is not equal to a mapping of null).
    /**
     * Compatible with JDK 1.2.
     */
    private static final long serialVersionUID = 919286545866124006L;

    /**
     * Color status of a node. Package visible for use by nested classes.
     */
    static final int RED = -1, BLACK = 1;

    /**
     * Sentinal node, used to avoid null checks for corner cases and make the
     * delete rebalance code simpler. The rebalance code must never assign
     * the parent, left, or right of nil, but may safely reassign the color
     * to be black. This object must never be used as a key in a TreeMap, or
     * it will break bounds checking of a SubMap.
     */
    static final Node nil = new Node(null, null, BLACK);

    // Nil is self-referential, so we must initialize it after creation.
    /**
     * The root node of this TreeMap.
     */
    private transient Node root;

    /**
     * The size of this TreeMap. Package visible for use by nested classes.
     */
    private transient int size;

    /**
     * The cache for {
    @link #entrySet()    }
    .
     */
    private transient Set entries;

    /**
     * Counts the number of modifications this TreeMap has undergone, used
     * by Iterators to know when to throw ConcurrentModificationExceptions.
     * Package visible for use by nested classes.
     */
    private transient int modCount;

    /**
     * This TreeMap's comparator, or null for natural ordering.
     * Package visible for use by nested classes.
     * @serial the comparator ordering this tree, or null
     */
    final Comparator comparator;

    /**
     * Class to represent an entry in the tree. Holds a single key-value pair,
     * plus pointers to parent and child nodes.
     * @author Eric Blake (ebb9@email.byu.edu)
     */
    private static final class Node extends AbstractMap.BasicMapEntry implements
            Rollbackable {

        // All fields package visible for use by nested classes.
        /**
         * The color of this node.
         */
        private int color;

        /**
         * The left child node.
         */
        private Node left = nil;

        /**
         * The right child node.
         */
        private Node right = nil;

        /**
         * The parent node.
         */
        private Node parent = nil;

        /**
         * Simple constructor.
         * @param key the key
         * @param value the value
         */
        Node(Object key, Object value, int color) {
            super(key, value);
            this.setColor(color);
        }

        void setColor(int color) {
            this.$ASSIGN$color(color);
        }

        int getColor() {
            return color;
        }

        void setLeft(Node left) {
            this.$ASSIGN$left(left);
        }

        Node getLeft() {
            return left;
        }

        void setRight(Node right) {
            this.$ASSIGN$right(right);
        }

        Node getRight() {
            return right;
        }

        void setParent(Node parent) {
            this.$ASSIGN$parent(parent);
        }

        Node getParent() {
            return parent;
        }

        private final int $ASSIGN$color(int newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$color.add(null, color, $CHECKPOINT.getTimestamp());
            }
            return color = newValue;
        }

        private final Node $ASSIGN$left(Node newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$left.add(null, left, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return left = newValue;
        }

        private final Node $ASSIGN$right(Node newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$right.add(null, right, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return right = newValue;
        }

        private final Node $ASSIGN$parent(Node newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$parent.add(null, parent, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return parent = newValue;
        }

        public void $COMMIT(long timestamp) {
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                    .getTopTimestamp());
            super.$COMMIT(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            color = $RECORD$color.restore(color, timestamp, trim);
            left = (Node) $RECORD$left.restore(left, timestamp, trim);
            right = (Node) $RECORD$right.restore(right, timestamp, trim);
            parent = (Node) $RECORD$parent.restore(parent, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private transient FieldRecord $RECORD$color = new FieldRecord(0);

        private transient FieldRecord $RECORD$left = new FieldRecord(0);

        private transient FieldRecord $RECORD$right = new FieldRecord(0);

        private transient FieldRecord $RECORD$parent = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$color, $RECORD$left, $RECORD$right, $RECORD$parent };

    }

    // Create an AbstractSet with custom implementations of those methods
    // that can be overridden easily and efficiently.
    // Exploit fact that nil.value == null.
    // Create an AbstractSet with custom implementations of those methods
    // that can be overridden easily and efficiently.
    // Find new node's parent.
    // Key already in tree.
    // Set up new node.
    // Insert node in tree.
    // Special case inserting into an empty tree.
    // Rebalance after insert.
    // Note: removeNode can alter the contents of n, so save value now.
    // We don't bother overriding many of the optional methods, as doing so
    // wouldn't provide any significant performance advantage.
    // if (parent == nil)
    //   throw new InternalError();
    // If a black node has been removed, we need to rebalance to avoid
    // violating the "same number of black nodes on any path" rule. If
    // node is red, we can simply recolor it black and all is well.
    // Rebalance left side.
    // if (sibling == nil)
    //   throw new InternalError();
    // Case 1: Sibling is red.
    // Recolor sibling and parent, and rotate parent left.
    // Case 2: Sibling has no red children.
    // Recolor sibling, and move to parent.
    // Case 3: Sibling has red left child.
    // Recolor sibling and left child, rotate sibling right.
    // Case 4: Sibling has red right child. Recolor sibling,
    // right child, and parent, and rotate parent left.
    // Finished.
    // Symmetric "mirror" of left-side case.
    // if (sibling == nil)
    //   throw new InternalError();
    // Case 1: Sibling is red.
    // Recolor sibling and parent, and rotate parent right.
    // Case 2: Sibling has no red children.
    // Recolor sibling, and move to parent.
    // Case 3: Sibling has red right child.
    // Recolor sibling and right child, rotate sibling left.
    // Case 4: Sibling has red left child. Recolor sibling,
    // left child, and parent, and rotate parent right.
    // Finished.
    // We color every row of nodes black, except for the overflow nodes.
    // I believe that this is the optimal arrangement. We construct the tree
    // in place by temporarily linking each node to the next node in the row,
    // then updating those links to the children when working on the next row.
    // Make the root node.
    // Fill each row that is completely full of nodes.
    // Now do the partial final row in red.
    // Add a lone left node if necessary.
    // Unlink the remaining nodes of the previous row.
    // Exploit fact that nil.left == nil.
    // Exact match.
    // Only need to rebalance when parent is a RED node, and while at least
    // 2 levels deep into the tree (ie: node has a grandparent). Remember
    // that nil.color == BLACK.
    // Uncle may be nil, in which case it is BLACK.
    // Case 1. Uncle is RED: Change colors of parent, uncle,
    // and grandparent, and move n to grandparent.
    // Case 2. Uncle is BLACK and x is right child.
    // Move n to parent, and rotate n left.
    // Case 3. Uncle is BLACK and x is left child.
    // Recolor parent, grandparent, and rotate grandparent right.
    // Mirror image of above code.
    // Uncle may be nil, in which case it is BLACK.
    // Case 1. Uncle is RED: Change colors of parent, uncle,
    // and grandparent, and move n to grandparent.
    // Case 2. Uncle is BLACK and x is left child.
    // Move n to parent, and rotate n right.
    // Case 3. Uncle is BLACK and x is right child.
    // Recolor parent, grandparent, and rotate grandparent left.
    // Exploit fact that nil.right == nil.
    // Exploit fact that nil.left == nil and node is non-nil.
    // Find splice, the node at the position to actually remove from the tree.
    // Node to be deleted has 0 or 1 children.
    // Node to be deleted has 1 child.
    // Node has 2 children. Splice is node's predecessor, and we swap
    // its contents into node.
    // Unlink splice from the tree.
    // Special case for 0 or 1 node remaining.
    // if (node == nil || child == nil)
    //   throw new InternalError();
    // Establish node.right link.
    // Establish child->parent link.
    // Link n and child.
    // if (node == nil || child == nil)
    //   throw new InternalError();
    // Establish node.left link.
    // Establish child->parent link.
    // Link n and child.
    // Exploit fact that nil.right == nil and node is non-nil.
    // FIXME gcj cannot handle this. Bug java/4695
    // this(type, firstNode(), nil);
    // class TreeIterator
    /**
     * Iterate over TreeMap's entries. This implementation is parameterized
     * to give a sequential view of keys, values, or entries.
     * @author Eric Blake (ebb9@email.byu.edu)
     */
    private final class TreeIterator implements Iterator, Rollbackable {

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
         * The number of modifications to the backing Map that we know about.
         */
        private int knownMod = getModCount();

        /**
         * The last Entry returned by a next() call.
         */
        private Node last;

        /**
         * The next entry that should be returned by next().
         */
        private Node next;

        /**
         * The last node visible to this iterator. This is used when iterating
         * on a SubMap.
         */
        private final Node max;

        /**
         * Construct a new TreeIterator with the supplied type.
         * @param type {
        @link #KEYS        }
        , {
        @link #VALUES        }
        , or {
        @link #ENTRIES        }

         */
        TreeIterator(int type) {
            this.type = type;
            this.$ASSIGN$next(firstNode());
            this.max = nil;
        }

        /**
         * Construct a new TreeIterator with the supplied type. Iteration will
         * be from "first" (inclusive) to "max" (exclusive).
         * @param type {
        @link #KEYS        }
        , {
        @link #VALUES        }
        , or {
        @link #ENTRIES        }

         * @param first where to start iteration, nil for empty iterator
         * @param max the cutoff for iteration, nil for all remaining nodes
         */
        TreeIterator(int type, Node first, Node max) {
            this.type = type;
            this.$ASSIGN$next(first);
            this.max = max;
        }

        /**
         * Returns true if the Iterator has more elements.
         * @return true if there are more elements
         */
        public boolean hasNext() {
            return next != max;
        }

        /**
         * Returns the next element in the Iterator's sequential view.
         * @return the next element
         * @exception ConcurrentModificationException if the TreeMap was modified
         * @exception NoSuchElementException if there is none
         */
        public Object next() {
            if (knownMod != getModCount()) {
                throw new ConcurrentModificationException();
            }
            if (next == max) {
                throw new NoSuchElementException();
            }
            $ASSIGN$last(next);
            $ASSIGN$next(successor(last));
            if (type == VALUES) {
                return last.getValueField();
            } else if (type == KEYS) {
                return last.getKeyField();
            }
            return last;
        }

        /**
         * Removes from the backing TreeMap the last element which was fetched
         * with the <code>next()</code> method.
         * @exception ConcurrentModificationException if the TreeMap was modified
         * @exception IllegalStateException if called when there is no last element
         */
        public void remove() {
            if (last == null) {
                throw new IllegalStateException();
            }
            if (knownMod != getModCount()) {
                throw new ConcurrentModificationException();
            }
            removeNode(last);
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

        private final Node $ASSIGN$last(Node newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$last.add(null, last, $CHECKPOINT.getTimestamp());
            }
            if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                newValue.$SET$CHECKPOINT($CHECKPOINT);
            }
            return last = newValue;
        }

        private final Node $ASSIGN$next(Node newValue) {
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
            last = (Node) $RECORD$last.restore(last, timestamp, trim);
            next = (Node) $RECORD$next.restore(next, timestamp, trim);
            $RECORD$max.restore(max, timestamp, trim);
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

        private transient FieldRecord $RECORD$last = new FieldRecord(0);

        private transient FieldRecord $RECORD$next = new FieldRecord(0);

        private transient FieldRecord $RECORD$max = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$type, $RECORD$knownMod, $RECORD$last, $RECORD$next,
                $RECORD$max };

    }

    /**
     * Implementation of {
    @link #subMap(Object, Object)    }
    and other map
     * ranges. This class provides a view of a portion of the original backing
     * map, and throws {
    @link IllegalArgumentException    }
    for attempts to
     * access beyond that range.
     * @author Eric Blake (ebb9@email.byu.edu)
     */
    private final class SubMap extends AbstractMap implements SortedMap,
            Rollbackable {

        /**
         * The lower range of this view, inclusive, or nil for unbounded.
         * Package visible for use by nested classes.
         */
        final Object minKey;

        /**
         * The upper range of this view, exclusive, or nil for unbounded.
         * Package visible for use by nested classes.
         */
        final Object maxKey;

        /**
         * The cache for {
        @link #entrySet()        }
        .
         */
        private Set entries;

        /**
         * Create a SubMap representing the elements between minKey (inclusive)
         * and maxKey (exclusive). If minKey is nil, SubMap has no lower bound
         * (headMap). If maxKey is nil, the SubMap has no upper bound (tailMap).
         * @param minKey the lower bound
         * @param maxKey the upper bound
         * @exception IllegalArgumentException if minKey &gt; maxKey
         */
        SubMap(Object minKey, Object maxKey) {
            if (minKey != nil && maxKey != nil && compare(minKey, maxKey) > 0) {
                throw new IllegalArgumentException("fromKey > toKey");
            }
            this.minKey = minKey;
            this.maxKey = maxKey;
        }

        /**
         * Check if "key" is in within the range bounds for this SubMap. The
         * lower ("from") SubMap range is inclusive, and the upper ("to") bound
         * is exclusive. Package visible for use by nested classes.
         * @param key the key to check
         * @return true if the key is in range
         */
        boolean keyInRange(Object key) {
            return ((minKey == nil || compare(key, minKey) >= 0) && (maxKey == nil || compare(
                    key, maxKey) < 0));
        }

        public void clear() {
            Node next = lowestGreaterThan(minKey, true);
            Node max = lowestGreaterThan(maxKey, false);
            while (next != max) {
                Node current = next;
                next = successor(current);
                removeNode(current);
            }
        }

        public Comparator comparator() {
            return comparator;
        }

        public boolean containsKey(Object key) {
            return keyInRange(key) && TreeMap.this.containsKey(key);
        }

        public boolean containsValue(Object value) {
            Node node = lowestGreaterThan(minKey, true);
            Node max = lowestGreaterThan(maxKey, false);
            while (node != max) {
                if (equals(value, node.getValue())) {
                    return true;
                }
                node = successor(node);
            }
            return false;
        }

        public Set entrySet() {
            if (entries == null) {
                // Create an AbstractSet with custom implementations of those methods
                // that can be overridden easily and efficiently.
                $ASSIGN$entries(new AbstractSet() {
                    public int size() {
                        return SubMap.this.size();
                    }

                    public Iterator iterator() {
                        Node first = lowestGreaterThan(minKey, true);
                        Node max = lowestGreaterThan(maxKey, false);
                        return new TreeIterator(ENTRIES, first, max);
                    }

                    public void clear() {
                        SubMap.this.clear();
                    }

                    public boolean contains(Object o) {
                        if (!(o instanceof Map.Entry)) {
                            return false;
                        }
                        Map.Entry me = (Map.Entry) o;
                        Object key = me.getKey();
                        if (!keyInRange(key)) {
                            return false;
                        }
                        Node n = getNode(key);
                        return n != nil
                                && AbstractCollection.equals(me.getValue(), n
                                        .getValueField());
                    }

                    public boolean remove(Object o) {
                        if (!(o instanceof Map.Entry)) {
                            return false;
                        }
                        Map.Entry me = (Map.Entry) o;
                        Object key = me.getKey();
                        if (!keyInRange(key)) {
                            return false;
                        }
                        Node n = getNode(key);
                        if (n != nil
                                && AbstractCollection.equals(me.getValue(), n
                                        .getValueField())) {
                            removeNode(n);
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

                        public final Object $SET$CHECKPOINT(
                                Checkpoint checkpoint) {
                            $SET$CHECKPOINT_ANONYMOUS(checkpoint);
                            return this;
                        }

                    }

                    public void $COMMIT_ANONYMOUS(long timestamp) {
                        FieldRecord.commit($RECORDS, timestamp,
                                $RECORD$$CHECKPOINT.getTopTimestamp());
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

        public Object firstKey() {
            Node node = lowestGreaterThan(minKey, true);
            if (node == nil || !keyInRange(node.getKeyField())) {
                throw new NoSuchElementException();
            }
            return node.getKeyField();
        }

        public Object get(Object key) {
            if (keyInRange(key)) {
                return TreeMap.this.get(key);
            }
            return null;
        }

        public SortedMap headMap(Object toKey) {
            if (!keyInRange(toKey)) {
                throw new IllegalArgumentException("key outside range");
            }
            return new SubMap(minKey, toKey);
        }

        public Set keySet() {
            if (this.getKeys() == null) {
                // Create an AbstractSet with custom implementations of those methods
                // that can be overridden easily and efficiently.
                this.setKeys(new AbstractSet() {
                    public int size() {
                        return SubMap.this.size();
                    }

                    public Iterator iterator() {
                        Node first = lowestGreaterThan(minKey, true);
                        Node max = lowestGreaterThan(maxKey, false);
                        return new TreeIterator(KEYS, first, max);
                    }

                    public void clear() {
                        SubMap.this.clear();
                    }

                    public boolean contains(Object o) {
                        if (!keyInRange(o)) {
                            return false;
                        }
                        return getNode(o) != nil;
                    }

                    public boolean remove(Object o) {
                        if (!keyInRange(o)) {
                            return false;
                        }
                        Node n = getNode(o);
                        if (n != nil) {
                            removeNode(n);
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

                        public final Object $SET$CHECKPOINT(
                                Checkpoint checkpoint) {
                            $SET$CHECKPOINT_ANONYMOUS(checkpoint);
                            return this;
                        }

                    }

                    public void $COMMIT_ANONYMOUS(long timestamp) {
                        FieldRecord.commit($RECORDS, timestamp,
                                $RECORD$$CHECKPOINT.getTopTimestamp());
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
            return this.getKeys();
        }

        public Object lastKey() {
            Node node = highestLessThan(maxKey);
            if (node == nil || !keyInRange(node.getKeyField())) {
                throw new NoSuchElementException();
            }
            return node.getKeyField();
        }

        public Object put(Object key, Object value) {
            if (!keyInRange(key)) {
                throw new IllegalArgumentException("Key outside range");
            }
            return TreeMap.this.put(key, value);
        }

        public Object remove(Object key) {
            if (keyInRange(key)) {
                return TreeMap.this.remove(key);
            }
            return null;
        }

        public int size() {
            Node node = lowestGreaterThan(minKey, true);
            Node max = lowestGreaterThan(maxKey, false);
            int count = 0;
            while (node != max) {
                count++;
                node = successor(node);
            }
            return count;
        }

        public SortedMap subMap(Object fromKey, Object toKey) {
            if (!keyInRange(fromKey) || !keyInRange(toKey)) {
                throw new IllegalArgumentException("key outside range");
            }
            return new SubMap(fromKey, toKey);
        }

        public SortedMap tailMap(Object fromKey) {
            if (!keyInRange(fromKey)) {
                throw new IllegalArgumentException("key outside range");
            }
            return new SubMap(fromKey, maxKey);
        }

        public Collection values() {
            if (this.getValues() == null) {
                // Create an AbstractCollection with custom implementations of those
                // methods that can be overridden easily and efficiently.
                this.setValues(new AbstractCollection() {
                    public int size() {
                        return SubMap.this.size();
                    }

                    public Iterator iterator() {
                        Node first = lowestGreaterThan(minKey, true);
                        Node max = lowestGreaterThan(maxKey, false);
                        return new TreeIterator(VALUES, first, max);
                    }

                    public void clear() {
                        SubMap.this.clear();
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

                        public final Object $SET$CHECKPOINT(
                                Checkpoint checkpoint) {
                            $SET$CHECKPOINT_ANONYMOUS(checkpoint);
                            return this;
                        }

                    }

                    public void $COMMIT_ANONYMOUS(long timestamp) {
                        FieldRecord.commit($RECORDS, timestamp,
                                $RECORD$$CHECKPOINT.getTopTimestamp());
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
            return this.getValues();
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
            entries = (Set) $RECORD$entries.restore(entries, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private transient FieldRecord $RECORD$entries = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] { $RECORD$entries };

    }

    // class SubMap
    /**
     * Instantiate a new TreeMap with no elements, using the keys' natural
     * ordering to sort. All entries in the map must have a key which implements
     * Comparable, and which are <i>mutually comparable</i>, otherwise map
     * operations may throw a {
    @link ClassCastException    }
    . Attempts to use
     * a null key will throw a {
    @link NullPointerException    }
    .
     * @see Comparable
     */
    public TreeMap() {
        this((Comparator) null);
    }

    /**
     * Instantiate a new TreeMap with no elements, using the provided comparator
     * to sort. All entries in the map must have keys which are mutually
     * comparable by the Comparator, otherwise map operations may throw a{
    @link ClassCastException    }
    .
     * @param c the sort order for the keys of this map, or null
     * for the natural order
     */
    public TreeMap(Comparator c) {
        comparator = c;
        fabricateTree(0);
    }

    /**
     * Instantiate a new TreeMap, initializing it with all of the elements in
     * the provided Map.  The elements will be sorted using the natural
     * ordering of the keys. This algorithm runs in n*log(n) time. All entries
     * in the map must have keys which implement Comparable and are mutually
     * comparable, otherwise map operations may throw a{
    @link ClassCastException    }
    .
     * @param map a Map, whose entries will be put into this TreeMap
     * @exception ClassCastException if the keys in the provided Map are not
     * comparable
     * @exception NullPointerException if map is null
     * @see Comparable
     */
    public TreeMap(Map map) {
        this((Comparator) null);
        putAll(map);
    }

    /**
     * Instantiate a new TreeMap, initializing it with all of the elements in
     * the provided SortedMap.  The elements will be sorted using the same
     * comparator as in the provided SortedMap. This runs in linear time.
     * @param sm a SortedMap, whose entries will be put into this TreeMap
     * @exception NullPointerException if sm is null
     */
    public TreeMap(SortedMap sm) {
        this(sm.comparator());
        int pos = sm.size();
        Iterator itr = sm.entrySet().iterator();
        fabricateTree(pos);
        Node node = firstNode();
        while (--pos >= 0) {
            Map.Entry me = (Map.Entry) itr.next();
            node.setKeyField(me.getKey());
            node.setValueField(me.getValue());
            node = successor(node);
        }
    }

    /**
     * Clears the Map so it has no keys. This is O(1).
     */
    public void clear() {
        if (getSize() > 0) {
            setModCount(getModCount() + 1);
            $ASSIGN$root(nil);
            setSize(0);
        }
    }

    /**
     * Returns a shallow clone of this TreeMap. The Map itself is cloned,
     * but its contents are not.
     * @return the clone
     */
    public Object clone() {
        TreeMap copy = null;
        try {
            copy = (TreeMap) super.clone();
        } catch (CloneNotSupportedException x) {
        }
        copy.$ASSIGN$entries(null);
        copy.fabricateTree(getSize());
        Node node = firstNode();
        Node cnode = copy.firstNode();
        while (node != nil) {
            cnode.setKeyField(node.getKeyField());
            cnode.setValueField(node.getValueField());
            node = successor(node);
            cnode = copy.successor(cnode);
        }
        return copy;
    }

    /**
     * Return the comparator used to sort this map, or null if it is by
     * natural order.
     * @return the map's comparator
     */
    public Comparator comparator() {
        return comparator;
    }

    /**
     * Returns true if the map contains a mapping for the given key.
     * @param key the key to look for
     * @return true if the key has a mapping
     * @exception ClassCastException if key is not comparable to map elements
     * @exception NullPointerException if key is null and the comparator is not
     * tolerant of nulls
     */
    public boolean containsKey(Object key) {
        return getNode(key) != nil;
    }

    /**
     * Returns true if the map contains at least one mapping to the given value.
     * This requires linear time.
     * @param value the value to look for
     * @return true if the value appears in a mapping
     */
    public boolean containsValue(Object value) {
        Node node = firstNode();
        while (node != nil) {
            if (equals(value, node.getValueField())) {
                return true;
            }
            node = successor(node);
        }
        return false;
    }

    /**
     * Returns a "set view" of this TreeMap's entries. The set is backed by
     * the TreeMap, so changes in one show up in the other.  The set supports
     * element removal, but not element addition.<p>
     * Note that the iterators for all three views, from keySet(), entrySet(),
     * and values(), traverse the TreeMap in sorted sequence.
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
                    return new TreeIterator(ENTRIES);
                }

                public void clear() {
                    TreeMap.this.clear();
                }

                public boolean contains(Object o) {
                    if (!(o instanceof Map.Entry)) {
                        return false;
                    }
                    Map.Entry me = (Map.Entry) o;
                    Node n = getNode(me.getKey());
                    return n != nil
                            && AbstractCollection.equals(me.getValue(), n
                                    .getValueField());
                }

                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry)) {
                        return false;
                    }
                    Map.Entry me = (Map.Entry) o;
                    Node n = getNode(me.getKey());
                    if (n != nil
                            && AbstractCollection.equals(me.getValue(), n
                                    .getValueField())) {
                        removeNode(n);
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
     * Returns the first (lowest) key in the map.
     * @return the first key
     * @exception NoSuchElementException if the map is empty
     */
    public Object firstKey() {
        if (root == nil) {
            throw new NoSuchElementException();
        }
        return firstNode().getKeyField();
    }

    /**
     * Return the value in this TreeMap associated with the supplied key,
     * or <code>null</code> if the key maps to nothing.  NOTE: Since the value
     * could also be null, you must use containsKey to see if this key
     * actually maps to something.
     * @param key the key for which to fetch an associated value
     * @return what the key maps to, if present
     * @exception ClassCastException if key is not comparable to elements in the map
     * @exception NullPointerException if key is null but the comparator does not
     * tolerate nulls
     * @see #put(Object, Object)
     * @see #containsKey(Object)
     */
    public Object get(Object key) {
        return getNode(key).getValueField();
    }

    /**
     * Returns a view of this Map including all entries with keys less than
     * <code>toKey</code>. The returned map is backed by the original, so changes
     * in one appear in the other. The submap will throw an{
    @link IllegalArgumentException    }
    for any attempt to access or add an
     * element beyond the specified cutoff. The returned map does not include
     * the endpoint; if you want inclusion, pass the successor element.
     * @param toKey the (exclusive) cutoff point
     * @return a view of the map less than the cutoff
     * @exception ClassCastException if <code>toKey</code> is not compatible with
     * the comparator (or is not Comparable, for natural ordering)
     * @exception NullPointerException if toKey is null, but the comparator does not
     * tolerate null elements
     */
    public SortedMap headMap(Object toKey) {
        return new SubMap(nil, toKey);
    }

    /**
     * Returns a "set view" of this TreeMap's keys. The set is backed by the
     * TreeMap, so changes in one show up in the other.  The set supports
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
                    return new TreeIterator(KEYS);
                }

                public void clear() {
                    TreeMap.this.clear();
                }

                public boolean contains(Object o) {
                    return containsKey(o);
                }

                public boolean remove(Object key) {
                    Node n = getNode(key);
                    if (n == nil) {
                        return false;
                    }
                    removeNode(n);
                    return true;
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
     * Returns the last (highest) key in the map.
     * @return the last key
     * @exception NoSuchElementException if the map is empty
     */
    public Object lastKey() {
        if (root == nil) {
            throw new NoSuchElementException("empty");
        }
        return lastNode().getKeyField();
    }

    /**
     * Puts the supplied value into the Map, mapped by the supplied key.
     * The value may be retrieved by any object which <code>equals()</code>
     * this key. NOTE: Since the prior value could also be null, you must
     * first use containsKey if you want to see if you are replacing the
     * key's mapping.
     * @param key the key used to locate the value
     * @param value the value to be stored in the Map
     * @return the prior mapping of the key, or null if there was none
     * @exception ClassCastException if key is not comparable to current map keys
     * @exception NullPointerException if key is null, but the comparator does
     * not tolerate nulls
     * @see #get(Object)
     * @see Object#equals(Object)
     */
    public Object put(Object key, Object value) {
        Node current = root;
        Node parent = nil;
        int comparison = 0;
        while (current != nil) {
            parent = current;
            comparison = compare(key, current.getKeyField());
            if (comparison > 0) {
                current = current.getRight();
            } else if (comparison < 0) {
                current = current.getLeft();
            } else {
                return current.setValue(value);
            }
        }
        Node n = new Node(key, value, RED);
        n.setParent(parent);
        setModCount(getModCount() + 1);
        setSize(getSize() + 1);
        if (parent == nil) {
            $ASSIGN$root(n);
            return null;
        }
        if (comparison > 0) {
            parent.setRight(n);
        } else {
            parent.setLeft(n);
        }
        insertFixup(n);
        return null;
    }

    /**
     * Copies all elements of the given map into this TreeMap.  If this map
     * already has a mapping for a key, the new mapping replaces the current
     * one.
     * @param m the map to be added
     * @exception ClassCastException if a key in m is not comparable with keys
     * in the map
     * @exception NullPointerException if a key in m is null, and the comparator
     * does not tolerate nulls
     */
    public void putAll(Map m) {
        Iterator itr = m.entrySet().iterator();
        int pos = m.size();
        while (--pos >= 0) {
            Map.Entry e = (Map.Entry) itr.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes from the TreeMap and returns the value which is mapped by the
     * supplied key. If the key maps to nothing, then the TreeMap remains
     * unchanged, and <code>null</code> is returned. NOTE: Since the value
     * could also be null, you must use containsKey to see if you are
     * actually removing a mapping.
     * @param key the key used to locate the value to remove
     * @return whatever the key mapped to, if present
     * @exception ClassCastException if key is not comparable to current map keys
     * @exception NullPointerException if key is null, but the comparator does
     * not tolerate nulls
     */
    public Object remove(Object key) {
        Node n = getNode(key);
        if (n == nil) {
            return null;
        }
        Object result = n.getValueField();
        removeNode(n);
        return result;
    }

    /**
     * Returns the number of key-value mappings currently in this Map.
     * @return the size
     */
    public int size() {
        return getSize();
    }

    /**
     * Returns a view of this Map including all entries with keys greater or
     * equal to <code>fromKey</code> and less than <code>toKey</code> (a
     * half-open interval). The returned map is backed by the original, so
     * changes in one appear in the other. The submap will throw an{
    @link IllegalArgumentException    }
    for any attempt to access or add an
     * element beyond the specified cutoffs. The returned map includes the low
     * endpoint but not the high; if you want to reverse this behavior on
     * either end, pass in the successor element.
     * @param fromKey the (inclusive) low cutoff point
     * @param toKey the (exclusive) high cutoff point
     * @return a view of the map between the cutoffs
     * @exception ClassCastException if either cutoff is not compatible with
     * the comparator (or is not Comparable, for natural ordering)
     * @exception NullPointerException if fromKey or toKey is null, but the
     * comparator does not tolerate null elements
     * @exception IllegalArgumentException if fromKey is greater than toKey
     */
    public SortedMap subMap(Object fromKey, Object toKey) {
        return new SubMap(fromKey, toKey);
    }

    /**
     * Returns a view of this Map including all entries with keys greater or
     * equal to <code>fromKey</code>. The returned map is backed by the
     * original, so changes in one appear in the other. The submap will throw an{
    @link IllegalArgumentException    }
    for any attempt to access or add an
     * element beyond the specified cutoff. The returned map includes the
     * endpoint; if you want to exclude it, pass in the successor element.
     * @param fromKey the (inclusive) low cutoff point
     * @return a view of the map above the cutoff
     * @exception ClassCastException if <code>fromKey</code> is not compatible with
     * the comparator (or is not Comparable, for natural ordering)
     * @exception NullPointerException if fromKey is null, but the comparator
     * does not tolerate null elements
     */
    public SortedMap tailMap(Object fromKey) {
        return new SubMap(fromKey, nil);
    }

    /**
     * Returns a "collection view" (or "bag view") of this TreeMap's values.
     * The collection is backed by the TreeMap, so changes in one show up
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
                    return new TreeIterator(VALUES);
                }

                public void clear() {
                    TreeMap.this.clear();
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
     * Compares two elements by the set comparator, or by natural ordering.
     * Package visible for use by nested classes.
     * @param o1 the first object
     * @param o2 the second object
     * @exception ClassCastException if o1 and o2 are not mutually comparable,
     * or are not Comparable with natural ordering
     * @exception NullPointerException if o1 or o2 is null with natural ordering
     */
    final int compare(Object o1, Object o2) {
        return (comparator == null ? ((Comparable) o1).compareTo(o2)
                : comparator.compare(o1, o2));
    }

    /**
     * Maintain red-black balance after deleting a node.
     * @param node the child of the node just deleted, possibly nil
     * @param parent the parent of the node just deleted, never nil
     */
    private void deleteFixup(Node node, Node parent) {
        while (node != root && node.getColor() == BLACK) {
            if (node == parent.getLeft()) {
                Node sibling = parent.getRight();
                if (sibling.getColor() == RED) {
                    sibling.setColor(BLACK);
                    parent.setColor(RED);
                    rotateLeft(parent);
                    sibling = parent.getRight();
                }
                if (sibling.getLeft().getColor() == BLACK
                        && sibling.getRight().getColor() == BLACK) {
                    sibling.setColor(RED);
                    node = parent;
                    parent = parent.getParent();
                } else {
                    if (sibling.getRight().getColor() == BLACK) {
                        sibling.getLeft().setColor(BLACK);
                        sibling.setColor(RED);
                        rotateRight(sibling);
                        sibling = parent.getRight();
                    }
                    sibling.setColor(parent.getColor());
                    parent.setColor(BLACK);
                    sibling.getRight().setColor(BLACK);
                    rotateLeft(parent);
                    node = root;
                }
            } else {
                Node sibling = parent.getLeft();
                if (sibling.getColor() == RED) {
                    sibling.setColor(BLACK);
                    parent.setColor(RED);
                    rotateRight(parent);
                    sibling = parent.getLeft();
                }
                if (sibling.getRight().getColor() == BLACK
                        && sibling.getLeft().getColor() == BLACK) {
                    sibling.setColor(RED);
                    node = parent;
                    parent = parent.getParent();
                } else {
                    if (sibling.getLeft().getColor() == BLACK) {
                        sibling.getRight().setColor(BLACK);
                        sibling.setColor(RED);
                        rotateLeft(sibling);
                        sibling = parent.getLeft();
                    }
                    sibling.setColor(parent.getColor());
                    parent.setColor(BLACK);
                    sibling.getLeft().setColor(BLACK);
                    rotateRight(parent);
                    node = root;
                }
            }
        }
        node.setColor(BLACK);
    }

    /**
     * Construct a perfectly balanced tree consisting of n "blank" nodes. This
     * permits a tree to be generated from pre-sorted input in linear time.
     * @param count the number of blank nodes, non-negative
     */
    private void fabricateTree(final int count) {
        if (count == 0) {
            $ASSIGN$root(nil);
            setSize(0);
            return;
        }
        $ASSIGN$root(new Node(null, null, BLACK));
        setSize(count);
        Node row = root;
        int rowsize;
        for (rowsize = 2; rowsize + rowsize <= count; rowsize <<= 1) {
            Node parent = row;
            Node last = null;
            for (int i = 0; i < rowsize; i += 2) {
                Node left = new Node(null, null, BLACK);
                Node right = new Node(null, null, BLACK);
                left.setParent(parent);
                left.setRight(right);
                right.setParent(parent);
                parent.setLeft(left);
                Node next = parent.getRight();
                parent.setRight(right);
                parent = next;
                if (last != null) {
                    last.setRight(left);
                }
                last = right;
            }
            row = row.getLeft();
        }
        int overflow = count - rowsize;
        Node parent = row;
        int i;
        for (i = 0; i < overflow; i += 2) {
            Node left = new Node(null, null, RED);
            Node right = new Node(null, null, RED);
            left.setParent(parent);
            right.setParent(parent);
            parent.setLeft(left);
            Node next = parent.getRight();
            parent.setRight(right);
            parent = next;
        }
        if (i - overflow == 0) {
            Node left = new Node(null, null, RED);
            left.setParent(parent);
            parent.setLeft(left);
            parent = parent.getRight();
            left.getParent().setRight(nil);
        }
        while (parent != nil) {
            Node next = parent.getRight();
            parent.setRight(nil);
            parent = next;
        }
    }

    /**
     * Returns the first sorted node in the map, or nil if empty. Package
     * visible for use by nested classes.
     * @return the first node
     */
    final Node firstNode() {
        Node node = root;
        while (node.getLeft() != nil) {
            node = node.getLeft();
        }
        return node;
    }

    /**
     * Return the TreeMap.Node associated with key, or the nil node if no such
     * node exists in the tree. Package visible for use by nested classes.
     * @param key the key to search for
     * @return the node where the key is found, or nil
     */
    final Node getNode(Object key) {
        Node current = root;
        while (current != nil) {
            int comparison = compare(key, current.getKeyField());
            if (comparison > 0) {
                current = current.getRight();
            } else if (comparison < 0) {
                current = current.getLeft();
            } else {
                return current;
            }
        }
        return current;
    }

    /**
     * Find the "highest" node which is &lt; key. If key is nil, return last
     * node. Package visible for use by nested classes.
     * @param key the upper bound, exclusive
     * @return the previous node
     */
    final Node highestLessThan(Object key) {
        if (key == nil) {
            return lastNode();
        }
        Node last = nil;
        Node current = root;
        int comparison = 0;
        while (current != nil) {
            last = current;
            comparison = compare(key, current.getKeyField());
            if (comparison > 0) {
                current = current.getRight();
            } else if (comparison < 0) {
                current = current.getLeft();
            } else {
                return predecessor(last);
            }
        }
        return comparison <= 0 ? predecessor(last) : last;
    }

    /**
     * Maintain red-black balance after inserting a new node.
     * @param n the newly inserted node
     */
    private void insertFixup(Node n) {
        while (n.getParent().getColor() == RED
                && n.getParent().getParent() != nil) {
            if (n.getParent() == n.getParent().getParent().getLeft()) {
                Node uncle = n.getParent().getParent().getRight();
                if (uncle.getColor() == RED) {
                    n.getParent().setColor(BLACK);
                    uncle.setColor(BLACK);
                    uncle.getParent().setColor(RED);
                    n = uncle.getParent();
                } else {
                    if (n == n.getParent().getRight()) {
                        n = n.getParent();
                        rotateLeft(n);
                    }
                    n.getParent().setColor(BLACK);
                    n.getParent().getParent().setColor(RED);
                    rotateRight(n.getParent().getParent());
                }
            } else {
                Node uncle = n.getParent().getParent().getLeft();
                if (uncle.getColor() == RED) {
                    n.getParent().setColor(BLACK);
                    uncle.setColor(BLACK);
                    uncle.getParent().setColor(RED);
                    n = uncle.getParent();
                } else {
                    if (n == n.getParent().getLeft()) {
                        n = n.getParent();
                        rotateRight(n);
                    }
                    n.getParent().setColor(BLACK);
                    n.getParent().getParent().setColor(RED);
                    rotateLeft(n.getParent().getParent());
                }
            }
        }
        root.setColor(BLACK);
    }

    /**
     * Returns the last sorted node in the map, or nil if empty.
     * @return the last node
     */
    private Node lastNode() {
        Node node = root;
        while (node.getRight() != nil) {
            node = node.getRight();
        }
        return node;
    }

    /**
     * Find the "lowest" node which is &gt;= key. If key is nil, return either
     * nil or the first node, depending on the parameter first.
     * Package visible for use by nested classes.
     * @param key the lower bound, inclusive
     * @param first true to return the first element instead of nil for nil key
     * @return the next node
     */
    final Node lowestGreaterThan(Object key, boolean first) {
        if (key == nil) {
            return first ? firstNode() : nil;
        }
        Node last = nil;
        Node current = root;
        int comparison = 0;
        while (current != nil) {
            last = current;
            comparison = compare(key, current.getKeyField());
            if (comparison > 0) {
                current = current.getRight();
            } else if (comparison < 0) {
                current = current.getLeft();
            } else {
                return current;
            }
        }
        return comparison > 0 ? successor(last) : last;
    }

    /**
     * Return the node preceding the given one, or nil if there isn't one.
     * @param node the current node, not nil
     * @return the prior node in sorted order
     */
    private Node predecessor(Node node) {
        if (node.getLeft() != nil) {
            node = node.getLeft();
            while (node.getRight() != nil) {
                node = node.getRight();
            }
            return node;
        }
        Node parent = node.getParent();
        while (node == parent.getLeft()) {
            node = parent;
            parent = node.getParent();
        }
        return parent;
    }

    /**
     * Construct a tree from sorted keys in linear time. Package visible for
     * use by TreeSet.
     * @param s the stream to read from
     * @param count the number of keys to read
     * @param readValues true to read values, false to insert "" as the value
     * @exception ClassNotFoundException if the underlying stream fails
     * @exception IOException if the underlying stream fails
     * @see #readObject(ObjectInputStream)
     * @see TreeSet#readObject(ObjectInputStream)
     */
    final void putFromObjStream(ObjectInputStream s, int count,
            boolean readValues) throws IOException, ClassNotFoundException {
        fabricateTree(count);
        Node node = firstNode();
        while (--count >= 0) {
            node.setKeyField(s.readObject());
            node.setValueField(readValues ? s.readObject() : "");
            node = successor(node);
        }
    }

    /**
     * Construct a tree from sorted keys in linear time, with values of "".
     * Package visible for use by TreeSet.
     * @param keys the iterator over the sorted keys
     * @param count the number of nodes to insert
     * @see TreeSet#TreeSet(SortedSet)
     */
    final void putKeysLinear(Iterator keys, int count) {
        fabricateTree(count);
        Node node = firstNode();
        while (--count >= 0) {
            node.setKeyField(keys.next());
            node.setValueField("");
            node = successor(node);
        }
    }

    /**
     * Deserializes this object from the given stream.
     * @param s the stream to read from
     * @exception ClassNotFoundException if the underlying stream fails
     * @exception IOException if the underlying stream fails
     * @serialData the <i>size</i> (int), followed by key (Object) and value
     * (Object) pairs in sorted order
     */
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        putFromObjStream(s, size, true);
    }

    /**
     * Remove node from tree. This will increment modCount and decrement size.
     * Node must exist in the tree. Package visible for use by nested classes.
     * @param node the node to remove
     */
    final void removeNode(Node node) {
        Node splice;
        Node child;
        setModCount(getModCount() + 1);
        setSize(getSize() - 1);
        if (node.getLeft() == nil) {
            splice = node;
            child = node.getRight();
        } else if (node.getRight() == nil) {
            splice = node;
            child = node.getLeft();
        } else {
            splice = node.getLeft();
            while (splice.getRight() != nil) {
                splice = splice.getRight();
            }
            child = splice.getLeft();
            node.setKeyField(splice.getKeyField());
            node.setValueField(splice.getValueField());
        }
        Node parent = splice.getParent();
        if (child != nil) {
            child.setParent(parent);
        }
        if (parent == nil) {
            $ASSIGN$root(child);
            return;
        }
        if (splice == parent.getLeft()) {
            parent.setLeft(child);
        } else {
            parent.setRight(child);
        }
        if (splice.getColor() == BLACK) {
            deleteFixup(child, parent);
        }
    }

    /**
     * Rotate node n to the left.
     * @param node the node to rotate
     */
    private void rotateLeft(Node node) {
        Node child = node.getRight();
        node.setRight(child.getLeft());
        if (child.getLeft() != nil) {
            child.getLeft().setParent(node);
        }
        child.setParent(node.getParent());
        if (node.getParent() != nil) {
            if (node == node.getParent().getLeft()) {
                node.getParent().setLeft(child);
            } else {
                node.getParent().setRight(child);
            }
        } else {
            $ASSIGN$root(child);
        }
        child.setLeft(node);
        node.setParent(child);
    }

    /**
     * Rotate node n to the right.
     * @param node the node to rotate
     */
    private void rotateRight(Node node) {
        Node child = node.getLeft();
        node.setLeft(child.getRight());
        if (child.getRight() != nil) {
            child.getRight().setParent(node);
        }
        child.setParent(node.getParent());
        if (node.getParent() != nil) {
            if (node == node.getParent().getRight()) {
                node.getParent().setRight(child);
            } else {
                node.getParent().setLeft(child);
            }
        } else {
            $ASSIGN$root(child);
        }
        child.setRight(node);
        node.setParent(child);
    }

    /**
     * Return the node following the given one, or nil if there isn't one.
     * Package visible for use by nested classes.
     * @param node the current node, not nil
     * @return the next node in sorted order
     */
    final Node successor(Node node) {
        if (node.getRight() != nil) {
            node = node.getRight();
            while (node.getLeft() != nil) {
                node = node.getLeft();
            }
            return node;
        }
        Node parent = node.getParent();
        while (node == parent.getRight()) {
            node = parent;
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * Serializes this object to the given stream.
     * @param s the stream to write to
     * @exception IOException if the underlying stream fails
     * @serialData the <i>size</i> (int), followed by key (Object) and value
     * (Object) pairs in sorted order
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Node node = firstNode();
        s.writeInt(getSize());
        while (node != nil) {
            s.writeObject(node.getKeyField());
            s.writeObject(node.getValueField());
            node = successor(node);
        }
    }

    void setModCount(int modCount) {
        this.$ASSIGN$modCount(modCount);
    }

    int getModCount() {
        return modCount;
    }

    void setSize(int size) {
        this.$ASSIGN$size(size);
    }

    int getSize() {
        return size;
    }

    static {
        nil.setParent(nil);
        nil.setLeft(nil);
        nil.setRight(nil);
    }

    private final Node $ASSIGN$root(Node newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$root.add(null, root, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return root = newValue;
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

    private final int $ASSIGN$modCount(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$modCount.add(null, modCount, $CHECKPOINT.getTimestamp());
        }
        return modCount = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT
                .getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        root = (Node) $RECORD$root.restore(root, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        entries = (Set) $RECORD$entries.restore(entries, timestamp, trim);
        modCount = $RECORD$modCount.restore(modCount, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private transient FieldRecord $RECORD$root = new FieldRecord(0);

    private transient FieldRecord $RECORD$size = new FieldRecord(0);

    private transient FieldRecord $RECORD$entries = new FieldRecord(0);

    private transient FieldRecord $RECORD$modCount = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$root, $RECORD$size, $RECORD$entries, $RECORD$modCount };

}

// class TreeMap
