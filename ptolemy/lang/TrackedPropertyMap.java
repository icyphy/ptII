package ptolemy.lang;

import java.util.Iterator;
import java.util.HashSet;

public class TrackedPropertyMap extends PropertyMap {
    public TrackedPropertyMap() {
        super();

    }

    public boolean addVisitor(Object o) {
        return _visitedBySet.add(o);
    }

    public void clearVisitors() {
        _visitedBySet.clear();
    }

    public boolean removeVisitor(Object o) {
        return _visitedBySet.remove(o);
    }

    public boolean wasVisitedBy(Object o) {
        return _visitedBySet.contains(o);
    }

    public Iterator visitorIterator() {
        return _visitedBySet.iterator();
    }

    protected HashSet _visitedBySet = new HashSet();
}