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

import java.lang.Object;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;

public class LinkedHashMap extends HashMap implements Rollbackable {

    private static final long serialVersionUID = 3801124242820219131L;

    private transient LinkedHashEntry root;

    final boolean accessOrder;

    class LinkedHashEntry extends HashEntry implements Rollbackable {

        private LinkedHashEntry pred;

        private LinkedHashEntry succ;

        LinkedHashEntry(Object key, Object value) {
            super(key, value);
            if (root == null) {
                $ASSIGN$root(this);
                $ASSIGN$pred(this);
            } else {
                $ASSIGN$pred(root.pred);
                pred.$ASSIGN$succ(this);
                root.$ASSIGN$pred(this);
            }
        }

        void access() {
            if (accessOrder && succ != null) {
                setModCount(getModCount() + 1);
                if (this == root) {
                    $ASSIGN$root(succ);
                    pred.$ASSIGN$succ(this);
                    $ASSIGN$succ(null);
                } else {
                    pred.$ASSIGN$succ(succ);
                    succ.$ASSIGN$pred(pred);
                    $ASSIGN$succ(null);
                    $ASSIGN$pred(root.pred);
                    pred.$ASSIGN$succ(this);
                }
            }
        }

        Object cleanup() {
            if (this == root) {
                $ASSIGN$root(succ);
                if (succ != null)
                    succ.$ASSIGN$pred(pred);
            } else if (succ == null) {
                pred.$ASSIGN$succ(null);
                root.$ASSIGN$pred(pred);
            } else {
                pred.$ASSIGN$succ(succ);
                succ.$ASSIGN$pred(pred);
            }
            return getValue();
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
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
            super.$COMMIT(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            pred = (LinkedHashEntry)$RECORD$pred.restore(pred, timestamp, trim);
            succ = (LinkedHashEntry)$RECORD$succ.restore(succ, timestamp, trim);
            super.$RESTORE(timestamp, trim);
        }

        private FieldRecord $RECORD$pred = new FieldRecord(0);

        private FieldRecord $RECORD$succ = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$pred,
                $RECORD$succ
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
        $ASSIGN$root(null);
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
        $ASSIGN$root(null);
        super.putAllInternal(m);
    }

    Iterator iterator(final int type) {
        return new Iterator() {

            private LinkedHashEntry current = root;

            private LinkedHashEntry last;

            private int knownMod = getModCount();

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
                $ASSIGN$last(current);
                $ASSIGN$current(current.succ);
                return type == VALUES?last.getValue():type == KEYS?last.getKey():last;
            }

            public void remove() {
                if (knownMod != getModCount())
                    throw new ConcurrentModificationException();
                if (last == null)
                    throw new IllegalStateException();
                LinkedHashMap.this.remove(last.getKey());
                $ASSIGN$last(null);
                $ASSIGN$SPECIAL$knownMod(11, knownMod);
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

            private final LinkedHashEntry $ASSIGN$current(LinkedHashEntry newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$current.add(null, current, $CHECKPOINT.getTimestamp());
                }
                if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                    newValue.$SET$CHECKPOINT($CHECKPOINT);
                }
                return current = newValue;
            }

            private final LinkedHashEntry $ASSIGN$last(LinkedHashEntry newValue) {
                if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                    $RECORD$last.add(null, last, $CHECKPOINT.getTimestamp());
                }
                if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
                    newValue.$SET$CHECKPOINT($CHECKPOINT);
                }
                return last = newValue;
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

            public void $COMMIT_ANONYMOUS(long timestamp) {
                FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
                $RECORD$$CHECKPOINT.commit(timestamp);
            }

            public void $RESTORE_ANONYMOUS(long timestamp, boolean trim) {
                current = (LinkedHashEntry)$RECORD$current.restore(current, timestamp, trim);
                last = (LinkedHashEntry)$RECORD$last.restore(last, timestamp, trim);
                knownMod = $RECORD$knownMod.restore(knownMod, timestamp, trim);
                if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
                    $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, new _PROXY_(), timestamp, trim);
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
                        $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                        FieldRecord.pushState($RECORDS);
                    }
                    $CHECKPOINT = checkpoint;
                    oldCheckpoint.setCheckpoint(checkpoint);
                    checkpoint.addObject(new _PROXY_());
                }
                return this;
            }

            private FieldRecord $RECORD$current = new FieldRecord(0);

            private FieldRecord $RECORD$last = new FieldRecord(0);

            private FieldRecord $RECORD$knownMod = new FieldRecord(0);

            private FieldRecord[] $RECORDS = new FieldRecord[] {
                    $RECORD$current,
                    $RECORD$last,
                    $RECORD$knownMod
                };

            {
                $CHECKPOINT.addObject(new _PROXY_());
            }
        };
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
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        root = (LinkedHashEntry)$RECORD$root.restore(root, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$root = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$root
        };
}
