package ptolemy.backtrack.util.java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.Object;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.Set;

public class TreeMap extends AbstractMap implements SortedMap, Cloneable, Serializable, Rollbackable {

    private static final long serialVersionUID = 919286545866124006L;

    static final int RED = -1, BLACK = 1;

    static final Node nil = new Node(null, null, BLACK);

    private transient Node root = nil;

    private transient int size;

    private transient Set entries;

    private transient int modCount;

    final Comparator comparator;

    private static final class Node extends AbstractMap.BasicMapEntry implements Rollbackable {

        private int color;

        private Node left = nil;

        private Node right = nil;

        private Node parent = nil;

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

        public void $RESTORE(long timestamp, boolean trim) {
            color = $RECORD$color.restore(color, timestamp, trim);
            left = (Node)$RECORD$left.restore(left, timestamp, trim);
            right = (Node)$RECORD$right.restore(right, timestamp, trim);
            parent = (Node)$RECORD$parent.restore(parent, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$color = new FieldRecord(0);

        private FieldRecord $RECORD$left = new FieldRecord(0);

        private FieldRecord $RECORD$right = new FieldRecord(0);

        private FieldRecord $RECORD$parent = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$color,
                $RECORD$left,
                $RECORD$right,
                $RECORD$parent
            };
    }

    private final class TreeIterator implements Iterator, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private final int type;

        private int knownMod = modCount;

        private Node last;

        private Node next;

        private final Node max;

        TreeIterator(int type) {
            this.type = type;
            this.$ASSIGN$next(firstNode());
            this.max = nil;
        }

        TreeIterator(int type, Node first, Node max) {
            this.type = type;
            this.$ASSIGN$next(first);
            this.max = max;
        }

        public boolean hasNext() {
            if (knownMod != modCount)
                throw new ConcurrentModificationException();
            return next != max;
        }

        public Object next() {
            if (knownMod != modCount)
                throw new ConcurrentModificationException();
            if (next == max)
                throw new NoSuchElementException();
            $ASSIGN$last(next);
            $ASSIGN$next(successor(last));
            if (type == VALUES)
                return last.getValue();
            else if (type == KEYS)
                return last.getKey();
            return last;
        }

        public void remove() {
            if (last == null)
                throw new IllegalStateException();
            if (knownMod != modCount)
                throw new ConcurrentModificationException();
            removeNode(last);
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

        public void $RESTORE(long timestamp, boolean trim) {
            knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
            last = (Node)$RECORD$last.restore(last, timestamp, trim);
            next = (Node)$RECORD$next.restore(next, timestamp, trim);
            $RECORD$max.restore(max, timestamp, trim);
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

        private FieldRecord $RECORD$type = new FieldRecord(0);

        private FieldRecord $RECORD$knownMod = new FieldRecord(0);

        private FieldRecord $RECORD$last = new FieldRecord(0);

        private FieldRecord $RECORD$next = new FieldRecord(0);

        private FieldRecord $RECORD$max = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$type,
                $RECORD$knownMod,
                $RECORD$last,
                $RECORD$next,
                $RECORD$max
            };
    }

    private final class SubMap extends AbstractMap implements SortedMap, Rollbackable {

        final Object minKey;

        final Object maxKey;

        private Set entries;

        SubMap(Object minKey, Object maxKey) {
            if (minKey != nil && maxKey != nil && compare(minKey, maxKey) > 0)
                throw new IllegalArgumentException("fromKey > toKey");
            this.minKey = minKey;
            this.maxKey = maxKey;
        }

        final boolean keyInRange(Object key) {
            return ((minKey == nil || compare(key, minKey) >= 0) && (maxKey == nil || compare(key, maxKey) < 0));
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
                if (equals(value, node.getValue()))
                    return true;
                node = successor(node);
            }
            return false;
        }

        public Set entrySet() {
            if (entries == null)
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
                        if (!(o instanceof Map.Entry))
                            return false;
                        Map.Entry me = (Map.Entry)o;
                        Object key = me.getKey();
                        if (!keyInRange(key))
                            return false;
                        Node n = getNode(key);
                        return n != nil && AbstractSet.equals(me.getValue(), n.getValue());
                    }

                    public boolean remove(Object o) {
                        if (!(o instanceof Map.Entry))
                            return false;
                        Map.Entry me = (Map.Entry)o;
                        Object key = me.getKey();
                        if (!keyInRange(key))
                            return false;
                        Node n = getNode(key);
                        if (n != nil && AbstractSet.equals(me.getValue(), n.getValue())) {
                            removeNode(n);
                            return true;
                        }
                        return false;
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
            return entries;
        }

        public Object firstKey() {
            Node node = lowestGreaterThan(minKey, true);
            if (node == nil || !keyInRange(node.getKey()))
                throw new NoSuchElementException();
            return node.getKey();
        }

        public Object get(Object key) {
            if (keyInRange(key))
                return TreeMap.this.get(key);
            return null;
        }

        public SortedMap headMap(Object toKey) {
            if (!keyInRange(toKey))
                throw new IllegalArgumentException("key outside range");
            return new SubMap(minKey, toKey);
        }

        public Set keySet() {
            if (getKeys() == null)
                setKeys(new AbstractSet() {

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
                        if (!keyInRange(o))
                            return false;
                        return getNode(o) != nil;
                    }

                    public boolean remove(Object o) {
                        if (!keyInRange(o))
                            return false;
                        Node n = getNode(o);
                        if (n != nil) {
                            removeNode(n);
                            return true;
                        }
                        return false;
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
            return getKeys();
        }

        public Object lastKey() {
            Node node = highestLessThan(maxKey);
            if (node == nil || !keyInRange(node.getKey()))
                throw new NoSuchElementException();
            return node.getKey();
        }

        public Object put(Object key, Object value) {
            if (!keyInRange(key))
                throw new IllegalArgumentException("Key outside range");
            return TreeMap.this.put(key, value);
        }

        public Object remove(Object key) {
            if (keyInRange(key))
                return TreeMap.this.remove(key);
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
            if (!keyInRange(fromKey) || !keyInRange(toKey))
                throw new IllegalArgumentException("key outside range");
            return new SubMap(fromKey, toKey);
        }

        public SortedMap tailMap(Object fromKey) {
            if (!keyInRange(fromKey))
                throw new IllegalArgumentException("key outside range");
            return new SubMap(fromKey, maxKey);
        }

        public Collection values() {
            if (getValues() == null)
                setValues(new AbstractCollection() {

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
            return getValues();
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
            entries = (Set)$RECORD$entries.restore(entries, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$entries = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$entries
            };
    }

    public TreeMap() {
        this((Comparator)null);
    }

    public TreeMap(Comparator c) {
        comparator = c;
    }

    public TreeMap(Map map) {
        this((Comparator)null);
        putAll(map);
    }

    public TreeMap(SortedMap sm) {
        this(sm.comparator());
        int pos = sm.size();
        Iterator itr = sm.entrySet().iterator();
        fabricateTree(pos);
        Node node = firstNode();
        while (--pos >= 0) {
            Map.Entry me = (Map.Entry)itr.next();
            node.setKey(me.getKey());
            node.setValue(me.getValue());
            node = successor(node);
        }
    }

    public void clear() {
        if (size > 0) {
            $ASSIGN$SPECIAL$modCount(11, modCount);
            $ASSIGN$root(nil);
            $ASSIGN$size(0);
        }
    }

    public Object clone() {
        TreeMap copy = null;
        try {
            copy = (TreeMap)super.clone();
        } catch (CloneNotSupportedException x) {
        }
        copy.$ASSIGN$entries(null);
        copy.fabricateTree(size);
        Node node = firstNode();
        Node cnode = copy.firstNode();
        while (node != nil) {
            cnode.setKey(node.getKey());
            cnode.setValue(node.getValue());
            node = successor(node);
            cnode = copy.successor(cnode);
        }
        return copy;
    }

    public Comparator comparator() {
        return comparator;
    }

    public boolean containsKey(Object key) {
        return getNode(key) != nil;
    }

    public boolean containsValue(Object value) {
        Node node = firstNode();
        while (node != nil) {
            if (equals(value, node.getValue()))
                return true;
            node = successor(node);
        }
        return false;
    }

    public Set entrySet() {
        if (entries == null)
            $ASSIGN$entries(new AbstractSet() {

                public int size() {
                    return size;
                }

                public Iterator iterator() {
                    return new TreeIterator(ENTRIES);
                }

                public void clear() {
                    TreeMap.this.clear();
                }

                public boolean contains(Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry me = (Map.Entry)o;
                    Node n = getNode(me.getKey());
                    return n != nil && AbstractSet.equals(me.getValue(), n.getValue());
                }

                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry me = (Map.Entry)o;
                    Node n = getNode(me.getKey());
                    if (n != nil && AbstractSet.equals(me.getValue(), n.getValue())) {
                        removeNode(n);
                        return true;
                    }
                    return false;
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
        return entries;
    }

    public Object firstKey() {
        if (root == nil)
            throw new NoSuchElementException();
        return firstNode().getKey();
    }

    public Object get(Object key) {
        return getNode(key).getValue();
    }

    public SortedMap headMap(Object toKey) {
        return new SubMap(nil, toKey);
    }

    public Set keySet() {
        if (getKeys() == null)
            setKeys(new AbstractSet() {

                public int size() {
                    return size;
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
                    if (n == nil)
                        return false;
                    removeNode(n);
                    return true;
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
        return getKeys();
    }

    public Object lastKey() {
        if (root == nil)
            throw new NoSuchElementException("empty");
        return lastNode().getKey();
    }

    public Object put(Object key, Object value) {
        Node current = root;
        Node parent = nil;
        int comparison = 0;
        while (current != nil) {
            parent = current;
            comparison = compare(key, current.getKey());
            if (comparison > 0)
                current = current.getRight();
            else if (comparison < 0)
                current = current.getLeft();
            else
                return current.setValue(value);
        }
        Node n = new Node(key, value, RED);
        n.setParent(parent);
        $ASSIGN$SPECIAL$modCount(11, modCount);
        $ASSIGN$SPECIAL$size(11, size);
        if (parent == nil) {
            $ASSIGN$root(n);
            return null;
        }
        if (comparison > 0)
            parent.setRight(n);
        else
            parent.setLeft(n);
        insertFixup(n);
        return null;
    }

    public void putAll(Map m) {
        Iterator itr = m.entrySet().iterator();
        int pos = m.size();
        while (--pos >= 0) {
            Map.Entry e = (Map.Entry)itr.next();
            put(e.getKey(), e.getValue());
        }
    }

    public Object remove(Object key) {
        Node n = getNode(key);
        if (n == nil)
            return null;
        Object result = n.getValue();
        removeNode(n);
        return result;
    }

    public int size() {
        return size;
    }

    public SortedMap subMap(Object fromKey, Object toKey) {
        return new SubMap(fromKey, toKey);
    }

    public SortedMap tailMap(Object fromKey) {
        return new SubMap(fromKey, nil);
    }

    public Collection values() {
        if (getValues() == null)
            setValues(new AbstractCollection() {

                public int size() {
                    return size;
                }

                public Iterator iterator() {
                    return new TreeIterator(VALUES);
                }

                public void clear() {
                    TreeMap.this.clear();
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
        return getValues();
    }

    final int compare(Object o1, Object o2) {
        return (comparator == null?((Comparable)o1).compareTo(o2):comparator.compare(o1, o2));
    }

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
                if (sibling.getLeft().getColor() == BLACK && sibling.getRight().getColor() == BLACK) {
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
                if (sibling.getRight().getColor() == BLACK && sibling.getLeft().getColor() == BLACK) {
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

    private void fabricateTree(final int count) {
        if (count == 0)
            return;
        $ASSIGN$root(new Node(null, null, BLACK));
        $ASSIGN$size(count);
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
                if (last != null)
                    last.setRight(left);
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

    final Node firstNode() {
        Node node = root;
        while (node.getLeft() != nil) 
            node = node.getLeft();
        return node;
    }

    final Node getNode(Object key) {
        Node current = root;
        while (current != nil) {
            int comparison = compare(key, current.getKey());
            if (comparison > 0)
                current = current.getRight();
            else if (comparison < 0)
                current = current.getLeft();
            else
                return current;
        }
        return current;
    }

    final Node highestLessThan(Object key) {
        if (key == nil)
            return lastNode();
        Node last = nil;
        Node current = root;
        int comparison = 0;
        while (current != nil) {
            last = current;
            comparison = compare(key, current.getKey());
            if (comparison > 0)
                current = current.getRight();
            else if (comparison < 0)
                current = current.getLeft();
            else
                return predecessor(last);
        }
        return comparison <= 0?predecessor(last):last;
    }

    private void insertFixup(Node n) {
        while (n.getParent().getColor() == RED && n.getParent().getParent() != nil) {
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

    private Node lastNode() {
        Node node = root;
        while (node.getRight() != nil) 
            node = node.getRight();
        return node;
    }

    final Node lowestGreaterThan(Object key, boolean first) {
        if (key == nil)
            return first?firstNode():nil;
        Node last = nil;
        Node current = root;
        int comparison = 0;
        while (current != nil) {
            last = current;
            comparison = compare(key, current.getKey());
            if (comparison > 0)
                current = current.getRight();
            else if (comparison < 0)
                current = current.getLeft();
            else
                return current;
        }
        return comparison > 0?successor(last):last;
    }

    private Node predecessor(Node node) {
        if (node.getLeft() != nil) {
            node = node.getLeft();
            while (node.getRight() != nil) 
                node = node.getRight();
            return node;
        }
        Node parent = node.getParent();
        while (node == parent.getLeft()) {
            node = parent;
            parent = node.getParent();
        }
        return parent;
    }

    final void putFromObjStream(ObjectInputStream s, int count, boolean readValues) throws IOException, ClassNotFoundException  {
        fabricateTree(count);
        Node node = firstNode();
        while (--count >= 0) {
            node.setKey(s.readObject());
            node.setValue(readValues?s.readObject():"");
            node = successor(node);
        }
    }

    final void putKeysLinear(Iterator keys, int count) {
        fabricateTree(count);
        Node node = firstNode();
        while (--count >= 0) {
            node.setKey(keys.next());
            node.setValue("");
            node = successor(node);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
        s.defaultReadObject();
        int size = s.readInt();
        putFromObjStream(s, size, true);
    }

    final void removeNode(Node node) {
        Node splice;
        Node child;
        $ASSIGN$SPECIAL$modCount(11, modCount);
        $ASSIGN$SPECIAL$size(12, size);
        if (node.getLeft() == nil) {
            splice = node;
            child = node.getRight();
        } else if (node.getRight() == nil) {
            splice = node;
            child = node.getLeft();
        } else {
            splice = node.getLeft();
            while (splice.getRight() != nil) 
                splice = splice.getRight();
            child = splice.getLeft();
            node.setKey(splice.getKey());
            node.setKey(splice.getValue());
        }
        Node parent = splice.getParent();
        if (child != nil)
            child.setParent(parent);
        if (parent == nil) {
            $ASSIGN$root(child);
            return;
        }
        if (splice == parent.getLeft())
            parent.setLeft(child);
        else
            parent.setRight(child);
        if (splice.getColor() == BLACK)
            deleteFixup(child, parent);
    }

    private void rotateLeft(Node node) {
        Node child = node.getRight();
        node.setRight(child.getLeft());
        if (child.getLeft() != nil)
            child.getLeft().setParent(node);
        child.setParent(node.getParent());
        if (node.getParent() != nil) {
            if (node == node.getParent().getLeft())
                node.getParent().setLeft(child);
            else
                node.getParent().setRight(child);
        } else
            $ASSIGN$root(child);
        child.setLeft(node);
        node.setParent(child);
    }

    private void rotateRight(Node node) {
        Node child = node.getLeft();
        node.setLeft(child.getRight());
        if (child.getRight() != nil)
            child.getRight().setParent(node);
        child.setParent(node.getParent());
        if (node.getParent() != nil) {
            if (node == node.getParent().getRight())
                node.getParent().setRight(child);
            else
                node.getParent().setLeft(child);
        } else
            $ASSIGN$root(child);
        child.setRight(node);
        node.setParent(child);
    }

    final Node successor(Node node) {
        if (node.getRight() != nil) {
            node = node.getRight();
            while (node.getLeft() != nil) 
                node = node.getLeft();
            return node;
        }
        Node parent = node.getParent();
        while (node == parent.getRight()) {
            node = parent;
            parent = parent.getParent();
        }
        return parent;
    }

    private void writeObject(ObjectOutputStream s) throws IOException  {
        s.defaultWriteObject();
        Node node = firstNode();
        s.writeInt(size);
        while (node != nil) {
            s.writeObject(node.getKey());
            s.writeObject(node.getValue());
            node = successor(node);
        }
    }

static     {
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

    public void $RESTORE(long timestamp, boolean trim) {
        root = (Node)$RECORD$root.restore(root, timestamp, trim);
        size = $RECORD$size.restore(size, timestamp, trim);
        entries = (Set)$RECORD$entries.restore(entries, timestamp, trim);
        modCount = $RECORD$modCount.restore(modCount, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$root = new FieldRecord(0);

    private FieldRecord $RECORD$size = new FieldRecord(0);

    private FieldRecord $RECORD$entries = new FieldRecord(0);

    private FieldRecord $RECORD$modCount = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$root,
            $RECORD$size,
            $RECORD$entries,
            $RECORD$modCount
        };
}
