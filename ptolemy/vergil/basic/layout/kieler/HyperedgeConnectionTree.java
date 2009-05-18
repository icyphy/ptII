package ptolemy.vergil.basic.layout.kieler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;

import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.layout.util.KimlLayoutUtil;

public class HyperedgeConnectionTree {

    List<KPoint> _commonBendpoints;
    Set<KEdge> _commonEdges;
    List<HyperedgeConnectionTree> _subTrees;
    int _startingIndex;

    public HyperedgeConnectionTree() {
        init();
    }

    private HyperedgeConnectionTree(int startingIndex) {
        init();
        _startingIndex = startingIndex;
    }

    void init() {
        _commonEdges = new HashSet<KEdge>();
        _commonBendpoints = new ArrayList<KPoint>();
        _subTrees = new ArrayList<HyperedgeConnectionTree>();
        _startingIndex = 0;
    }

    protected void setEdges(Set<KEdge> kEdges) {
        _commonEdges = kEdges;
    }

    protected void addEdge(KEdge edge) {
        // always allowed to add an first edge
        if (_commonBendpoints.isEmpty()) {
            _commonBendpoints.add(getBendPoint(edge, _startingIndex));
            _commonEdges.add(edge);
        }
        // if list is not empty check if having same bendpoint then add
        else {
            KPoint point = getBendPoint(edge, _startingIndex);
            KPoint commonPoint = this.getFirstCommonBendPoint();
            if (equals(point, commonPoint)) {
                _commonEdges.add(edge);
                calculateCommonBendpoints();
            }
            // if not, put it in subtree
            else {
                for (HyperedgeConnectionTree subTree : _subTrees) {
                    KPoint subTreePoint = subTree.getFirstCommonBendPoint();
                    // if there is already a fitting subtree, put it there
                    if (equals(point, subTreePoint)) {
                        subTree.addEdge(edge);
                        subTree.calculateCommonBendpoints();
                    } else {
                        HyperedgeConnectionTree newTree = new HyperedgeConnectionTree(
                                _startingIndex + 1);
                        _subTrees.add(newTree);
                        newTree.addEdge(edge);
                    }
                }
                // if not, make a new
                //	if (makeNewSubtree) {
                //		HyperedgeConnectionTree newTree = new HyperedgeConnectionTree(
                //				_startingIndex + 1);
                //		_subTrees.add(newTree);
                //		newTree.addEdge(edge);
                //	}
            }

        }
    }

    public void addAll(Collection<KEdge> c) {
        for (KEdge edge : c) {
            this.addEdge(edge);
        }
    }

    private void calculateCommonBendpoints() {
        final int initial = _startingIndex + 1;
        int index = initial;

        Set<Set<KEdge>> equalClasses = categorizeFromBendpoint(_commonEdges,
                index);
        while (true) {
            // all classes have same bendpoints
            if (equalClasses.size() == 1) {
                // we are fine, try next index if possible only
                Set<KEdge> equalEdges = equalClasses.iterator().next();
                // check if maybe all bendpointlists are empty now
                boolean allNull = true;
                KPoint newCommonBendPoint = null;
                for (KEdge edge : equalEdges) {
                    KPoint bendPoint = getBendPoint(edge, index);
                    if (bendPoint != null) {
                        allNull = false;
                        newCommonBendPoint = bendPoint;
                        break;
                    }
                }
                // no more bendpoints left, we are done
                if (allNull) {
                    break;
                } else {
                    if (!contains(_commonBendpoints, newCommonBendPoint)) {
                        _commonBendpoints.add(newCommonBendPoint);
                    }
                }
            } else {
                // Add equal classes to subtrees
                for (Set<KEdge> equalClass : equalClasses) {
                    for (KEdge edge : equalClass) {
                        // will create new subtree only if prefixes do not match
                        addEdgeToSubtree(edge);
                    }
                }
            }
            equalClasses = categorizeFromBendpoint(_commonEdges, ++index);
        }
    }

    private void addEdgeToSubtree(KEdge edge) {
        KPoint point = getBendPoint(edge, _startingIndex + 1);
        boolean foundSubTree = false;
        // search existing subTrees
        for (HyperedgeConnectionTree subTree : _subTrees) {
            KPoint subTreePoint = subTree.getFirstCommonBendPoint();
            // if there is already a fitting subtree, put it there
            if (equals(point, subTreePoint)) {
                subTree.addEdge(edge);
                subTree.calculateCommonBendpoints();
                foundSubTree = true;
                break;
            }
        }
        // if not, make a new
        if (!foundSubTree) {
            HyperedgeConnectionTree newTree = new HyperedgeConnectionTree(
                    _startingIndex + 1);
            _subTrees.add(newTree);
            newTree.addEdge(edge);
        }

    }

    private Set<Set<KEdge>> categorizeFromBendpoint(Set<KEdge> edges, int index) {
        Set<Set<KEdge>> result = new HashSet<Set<KEdge>>();
        for (Iterator iterator = edges.iterator(); iterator.hasNext();) {
            KEdge edge = (KEdge) iterator.next();
            boolean makeNewEqualClass = true;
            KPoint myPoint = getBendPoint(edge, index);
            // if( myPoint == null ){
            // makeNewEqualClass = false;
            // }else
            for (Set<KEdge> set : result) {
                KEdge otherEdge = set.iterator().next();
                KPoint otherPoint = getBendPoint(otherEdge, index);
                if (equals(myPoint, otherPoint)) {
                    makeNewEqualClass = false;
                    set.add(edge);
                }
            }
            if (makeNewEqualClass) {
                Set<KEdge> equalClass = new HashSet<KEdge>();
                equalClass.add(edge);
                result.add(equalClass);
            }
        }
        return result;
    }

    private void splitup() {
        // TODO Auto-generated method stub

    }

    public KPoint getFirstCommonBendPoint() {
        return _commonBendpoints.get(0);
    }

    public KPoint getBendPoint(KEdge edge, int index) {
        KEdgeLayout layout = KimlLayoutUtil.getEdgeLayout(edge);
        EList<KPoint> bendpoints = layout.getBendPoints();
        if (bendpoints.size() <= index || index < 0) {
            return null;
        }
        KPoint point = bendpoints.get(index);
        return point;
    }

    public boolean equals(KPoint point1, KPoint point2) {
        if (point1 == point2) {
            return true;
        }
        if (point1 == null || point2 == null) {
            return false;
        }
        return (point1.getX() == point2.getX() && point1.getY() == point2
                .getY());
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("i=" + _startingIndex + " ");
        for (KPoint point : _commonBendpoints) {
            if (point != null) {
                s.append("[" + point.getX() + "," + point.getY() + "]");
            } else {
                s.append(point + " ");
            }
        }
        for (KEdge edge : _commonEdges) {
            s.append(":E(" + edge.hashCode() + ")");
        }
        s.append("\n");
        for (HyperedgeConnectionTree subtree : _subTrees) {
            s.append(subtree.toString());
        }
        return s.toString();
    }

    public boolean contains(List<KPoint> points, KPoint p) {
        for (KPoint point : points) {
            if (point.getX() == p.getX() && point.getY() == p.getY()) {
                return true;
            }
        }
        return false;
    }

    public List<KPoint> bendPointList() {
        return _commonBendpoints;
    }

    public List<HyperedgeConnectionTree> subTreeList() {
        return _subTrees;
    }

    public Set<KEdge> commonEdgeSet() {
        return _commonEdges;
    }
}
