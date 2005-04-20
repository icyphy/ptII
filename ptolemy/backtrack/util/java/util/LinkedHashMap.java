package ptolemy.backtrack.util.java.util;

import java.lang.Object;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

public class LinkedHashMap extends HashMap implements Rollbackable {

    private static final long serialVersionUID = 3801124242820219131L;

    transient LinkedHashEntry root;

    final boolean accessOrder;

    class LinkedHashEntry extends HashEntry implements Rollbackable {

        LinkedHashEntry pred;

        LinkedHashEntry succ;

        LinkedHashEntry(Object key, Object value) {
            super(key, value);
            if (root == null) {
                root = this;
                pred = this;
            } else {
                pred = root.pred;
                pred.succ = this;
                root.pred = this;
            }
        }

        void access() {
            if (accessOrder && succ != null) {
                setModCount(getModCount() + 1);
                if (this == root) {
                    root = succ;
                    pred.succ = this;
                    succ = null;
                } else {
                    pred.succ = succ;
                    succ.pred = pred;
                    succ = null;
                    pred = root.pred;
                    pred.succ = this;
                }
            }
        }

        Object cleanup() {
            if (this == root) {
                root = succ;
                if (succ != null)
                    succ.pred = pred;
            } else if (succ == null) {
                pred.succ = null;
                root.pred = pred;
            } else {
                pred.succ = succ;
                succ.pred = pred;
            }
            return getValue();
        }

        public void $RESTORE(long timestamp, boolean trim) {
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord[] $RECORDS = new FieldRecord[] {
            };
    }

    public LinkedHashMap() {
        super();
        accessOrder = false;
    }

    public LinkedHashMap(Map m) {
        super(m);
        accessOrder = false;
    }

    public LinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        accessOrder = false;
    }

    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        accessOrder = false;
    }

    public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }

    public void clear() {
        super.clear();
        root = null;
    }

    public boolean containsValue(Object value) {
        LinkedHashEntry e = root;
        while (e != null) {
            if (equals(value, e.getValue()))
                return true;
            e = e.succ;
        }
        return false;
    }

    public Object get(Object key) {
        int idx = hash(key);
        HashEntry e = getBucket(idx);
        while (e != null) {
            if (equals(key, e.getKey())) {
                e.access();
                return e.getValue();
            }
            e = e.next;
        }
        return null;
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return false;
    }

    void addEntry(Object key, Object value, int idx, boolean callRemove) {
        LinkedHashEntry e = new LinkedHashEntry(key, value);
        e.next = getBucket(idx);
        setBucket(idx, e);
        if (callRemove && removeEldestEntry(root))
            remove(root);
    }

    void putAllInternal(Map m) {
        root = null;
        super.putAllInternal(m);
    }

    Iterator iterator(final int type) {
        return new Iterator() {

            LinkedHashEntry current = root;

            LinkedHashEntry last;

            int knownMod = getModCount();

            public boolean hasNext() {
                if (knownMod != getModCount())
                    throw new ConcurrentModificationException();
                return current != null;
            }

            public Object next() {
                if (knownMod != getModCount())
                    throw new ConcurrentModificationException();
                if (current == null)
                    throw new NoSuchElementException();
                last = current;
                current = current.succ;
                return type == VALUES?last.getValue():type == KEYS?last.getKey():last;
            }

            public void remove() {
                if (knownMod != getModCount())
                    throw new ConcurrentModificationException();
                if (last == null)
                    throw new IllegalStateException();
                LinkedHashMap.this.remove(last.getKey());
                last = null;
                knownMod++;
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
