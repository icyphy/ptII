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
import java.util.Comparator;
import java.util.Iterator;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.SortedMap;

public class TreeSet extends AbstractSet implements SortedSet, Cloneable, Serializable, Rollbackable {

    private static final long serialVersionUID = -2479143000061671589L;

    private transient SortedMap map;

    public TreeSet() {
        $ASSIGN$map(new TreeMap());
    }

    public TreeSet(Comparator comparator) {
        $ASSIGN$map(new TreeMap(comparator));
    }

    public TreeSet(Collection collection) {
        $ASSIGN$map(new TreeMap());
        addAll(collection);
    }

    public TreeSet(SortedSet sortedSet) {
        $ASSIGN$map(new TreeMap(sortedSet.comparator()));
        Iterator itr = sortedSet.iterator();
        ((TreeMap)map).putKeysLinear(itr, sortedSet.size());
    }

    private TreeSet(SortedMap backingMap) {
        $ASSIGN$map(backingMap);
    }

    public boolean add(Object obj) {
        return map.put(obj, "") == null;
    }

    public boolean addAll(Collection c) {
        boolean result = false;
        int pos = c.size();
        Iterator itr = c.iterator();
        while (--pos >= 0) 
            result |= (map.put(itr.next(), "") == null);
        return result;
    }

    public void clear() {
        map.clear();
    }

    public Object clone() {
        TreeSet copy = null;
        try {
            copy = (TreeSet)super.clone();
            copy.$ASSIGN$map((SortedMap)((AbstractMap)map).clone());
        } catch (CloneNotSupportedException x) {
        }
        return copy;
    }

    public Comparator comparator() {
        return map.comparator();
    }

    public boolean contains(Object obj) {
        return map.containsKey(obj);
    }

    public Object first() {
        return map.firstKey();
    }

    public SortedSet headSet(Object to) {
        return new TreeSet(map.headMap(to));
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Iterator iterator() {
        return map.keySet().iterator();
    }

    public Object last() {
        return map.lastKey();
    }

    public boolean remove(Object obj) {
        return map.remove(obj) != null;
    }

    public int size() {
        return map.size();
    }

    public SortedSet subSet(Object from, Object to) {
        return new TreeSet(map.subMap(from, to));
    }

    public SortedSet tailSet(Object from) {
        return new TreeSet(map.tailMap(from));
    }

    private void writeObject(ObjectOutputStream s) throws IOException  {
        s.defaultWriteObject();
        Iterator itr = map.keySet().iterator();
        int pos = map.size();
        s.writeObject(map.comparator());
        s.writeInt(pos);
        while (--pos >= 0) 
            s.writeObject(itr.next());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException  {
        s.defaultReadObject();
        Comparator comparator = (Comparator)s.readObject();
        int size = s.readInt();
        $ASSIGN$map(new TreeMap(comparator));
        ((TreeMap)map).putFromObjStream(s, size, false);
    }

    private final SortedMap $ASSIGN$map(SortedMap newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$map.add(null, map, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return map = newValue;
    }

    public void $RESTORE(long timestamp, boolean trim) {
        map = (SortedMap)$RECORD$map.restore(map, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private FieldRecord $RECORD$map = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$map
        };
}
