package ptolemy.backtrack.util.java.util;

import java.lang.Object;
import java.util.Iterator;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.Collection;
import ptolemy.backtrack.util.java.util.Set;

public abstract class AbstractMap implements Map, Rollbackable {

    protected Checkpoint $CHECKPOINT = new Checkpoint(this);

    static final int KEYS = 0, VALUES = 1, ENTRIES = 2;

    private Set keys;

    private Collection values;

    static class BasicMapEntry implements Map.Entry, Rollbackable {

        protected Checkpoint $CHECKPOINT = new Checkpoint(this);

        private Object key;

        private Object value;

        BasicMapEntry(Object newKey, Object newValue) {
            $ASSIGN$key(newKey);
            $ASSIGN$value(newValue);
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            if (o instanceof BasicMapEntry) {
                BasicMapEntry e = (BasicMapEntry)o;
                return (AbstractMap.equals(key, e.key) && AbstractMap.equals(value, e.value));
            }
            Map.Entry e = (Map.Entry)o;
            return (AbstractMap.equals(key, e.getKey()) && AbstractMap.equals(value, e.getValue()));
        }

        public final Object getKey() {
            return key;
        }

        public Object setKey(Object newKey) {
            Object r = key;
            $ASSIGN$key(newKey);
            return r;
        }

        public final Object getValue() {
            return value;
        }

        public final int hashCode() {
            return (AbstractMap.hashCode(key) ^ AbstractMap.hashCode(value));
        }

        public Object setValue(Object newVal) {
            Object r = value;
            $ASSIGN$value(newVal);
            return r;
        }

        public final String toString() {
            return key + "="+value;
        }

        private final Object $ASSIGN$key(Object newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$key.add(null, key, $CHECKPOINT.getTimestamp());
            }
            return key = newValue;
        }

        private final Object $ASSIGN$value(Object newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$value.add(null, value, $CHECKPOINT.getTimestamp());
            }
            return value = newValue;
        }

        public void $RESTORE(long timestamp, boolean trim) {
            key = (Object)$RECORD$key.restore(key, timestamp, trim);
            value = (Object)$RECORD$value.restore(value, timestamp, trim);
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

        private FieldRecord $RECORD$key = new FieldRecord(0);

        private FieldRecord $RECORD$value = new FieldRecord(0);

        private FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$key,
                $RECORD$value
            };
    }

    protected Set getKeys() {
        return keys;
    }

    protected void setKeys(Set keys) {
        this.$ASSIGN$keys(keys);
    }

    protected Collection getValues() {
        return values;
    }

    protected void setValues(Collection values) {
        this.$ASSIGN$values(values);
    }

    protected AbstractMap() {
    }

    public void clear() {
        entrySet().clear();
    }

    protected Object clone() throws CloneNotSupportedException  {
        AbstractMap copy = (AbstractMap)super.clone();
        copy.$ASSIGN$keys(null);
        copy.$ASSIGN$values(null);
        return copy;
    }

    public boolean containsKey(Object key) {
        Iterator entries = entrySet().iterator();
        int pos = size();
        while (--pos >= 0) 
            if (equals(key, ((Map.Entry)entries.next()).getKey()))
                return true;
        return false;
    }

    public boolean containsValue(Object value) {
        Iterator entries = entrySet().iterator();
        int pos = size();
        while (--pos >= 0) 
            if (equals(value, ((Map.Entry)entries.next()).getValue()))
                return true;
        return false;
    }

    public abstract Set entrySet();

    public boolean equals(Object o) {
        return (o == this || (o instanceof Map && entrySet().equals(((Map)o).entrySet())));
    }

    public Object get(Object key) {
        Iterator entries = entrySet().iterator();
        int pos = size();
        while (--pos >= 0) {
            Map.Entry entry = (Map.Entry)entries.next();
            if (equals(key, entry.getKey()))
                return entry.getValue();
        }
        return null;
    }

    public int hashCode() {
        return entrySet().hashCode();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Set keySet() {
        if (keys == null)
            $ASSIGN$keys(new AbstractSet() {

                public int size() {
                    return AbstractMap.this.size();
                }

                public boolean contains(Object key) {
                    return containsKey(key);
                }

                public Iterator iterator() {
                    return new Iterator() {

                        private final Iterator map_iterator = entrySet().iterator();

                        public boolean hasNext() {
                            return map_iterator.hasNext();
                        }

                        public Object next() {
                            return ((Map.Entry)map_iterator.next()).getKey();
                        }

                        public void remove() {
                            map_iterator.remove();
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
                            $RECORD$map_iterator.restore(map_iterator, timestamp, trim);
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

                        private FieldRecord $RECORD$map_iterator = new FieldRecord(0);

                        private FieldRecord[] $RECORDS = new FieldRecord[] {
                                $RECORD$map_iterator
                            };

                        {
                            $CHECKPOINT.addObject(new _PROXY_());
                        }
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
            });
        return keys;
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map m) {
        Iterator entries = m.entrySet().iterator();
        int pos = m.size();
        while (--pos >= 0) {
            Map.Entry entry = (Map.Entry)entries.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    public Object remove(Object key) {
        Iterator entries = entrySet().iterator();
        int pos = size();
        while (--pos >= 0) {
            Map.Entry entry = (Map.Entry)entries.next();
            if (equals(key, entry.getKey())) {
                Object r = entry.getValue();
                entries.remove();
                return r;
            }
        }
        return null;
    }

    public int size() {
        return entrySet().size();
    }

    public String toString() {
        Iterator entries = entrySet().iterator();
        StringBuffer r = new StringBuffer("{");
        for (int pos = size(); pos > 0; pos--) {
            Map.Entry entry = (Map.Entry)entries.next();
            r.append(entry.getKey());
            r.append('=');
            r.append(entry.getValue());
            if (pos > 1)
                r.append(", ");
        }
        r.append("}");
        return r.toString();
    }

    public Collection values() {
        if (values == null)
            $ASSIGN$values(new AbstractCollection() {

                public int size() {
                    return AbstractMap.this.size();
                }

                public boolean contains(Object value) {
                    return containsValue(value);
                }

                public Iterator iterator() {
                    return new Iterator() {

                        private final Iterator map_iterator = entrySet().iterator();

                        public boolean hasNext() {
                            return map_iterator.hasNext();
                        }

                        public Object next() {
                            return ((Map.Entry)map_iterator.next()).getValue();
                        }

                        public void remove() {
                            map_iterator.remove();
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
                            $RECORD$map_iterator.restore(map_iterator, timestamp, trim);
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

                        private FieldRecord $RECORD$map_iterator = new FieldRecord(0);

                        private FieldRecord[] $RECORDS = new FieldRecord[] {
                                $RECORD$map_iterator
                            };

                        {
                            $CHECKPOINT.addObject(new _PROXY_());
                        }
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
            });
        return values;
    }

    static final boolean equals(Object o1, Object o2) {
        return o1 == null?o2 == null:o1.equals(o2);
    }

    static final int hashCode(Object o) {
        return o == null?0:o.hashCode();
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

    private FieldRecord $RECORD$keys = new FieldRecord(0);

    private FieldRecord $RECORD$values = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$keys,
            $RECORD$values
        };
}
