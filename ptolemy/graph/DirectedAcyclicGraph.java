/** A directed acyclic graph (DAG).

 Copyright (c) 1997-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package ptolemy.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import ptolemy.graph.analysis.TransitiveClosureAnalysis;
import ptolemy.graph.analysis.strategy.CachedStrategy;

///////////////////////////////////////////////////////////////////
//// DirectedAcyclicGraph.java

/**
 A directed acyclic graph (DAG).

 The graphs constructed by this class cannot have cycles. For performance
 reasons, this requirement is not checked (except for self-loops) during
 the construction of the graph (calls to <code>add</code> and
 <code>addEdge</code>), but is checked when any of the other methods is
 called for the first time after the addition of nodes or edges. If the
 graph is cyclic, a GraphTopologyException is thrown. The check for cycles
 is done by computing the transitive closure, so the first operation after
 a graph change is slower.

 This class implements the CPO interface since the Hasse diagram of a CPO
 can be viewed as a DAG.  Therefore, this class can be viewed as both a DAG
 and a finite CPO. In the case of CPO, the node weights
 are the CPO elements. The CPO does not require the bottom
 element to exist. The call to <code>bottom</code> returns
 <code>null</code> if the bottom element does not exist.
 <p>
 NOTE: This class is a starting point for implementing graph algorithms,
 more methods will be added.

 @author Yuhong Xiong, Shuvra S. Bhattacharyya
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (yuhong)
 @Pt.AcceptedRating Green (kienhuis)
 */

// The methods greatestLowerBound, downSet, greatestElement share the
// same code with their duals, leastUpperBound, upSet, leastElement.
// The only thing different is the use of the transposition of the
// transitive closure instead of the original transitive closure.
// In another word, the computation of greatestLowerBound, downSet,
// greatestElement is converted to their dual operation by reversing
// the order relation in this CPO.
public class DirectedAcyclicGraph extends DirectedGraph implements CPO<Object> {
    /** Construct an empty DAG.
     */
    public DirectedAcyclicGraph() {
        super();
    }

    /** Construct an empty DAG with enough storage allocated
     *  for the specified number of elements.  Memory management is more
     *  efficient with this constructor if the number of elements is
     *  known.
     *  @param nodeCount The number of elements.
     */
    public DirectedAcyclicGraph(int nodeCount) {
        super(nodeCount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the bottom element of this CPO.
     *  @return An Object representing the bottom element, or
     *   <code>null</code> if the bottom does not exist.
     */
    @Override
    public Object bottom() {
        _validate();
        return _bottom;
    }

    /** Compare two elements in this CPO.
     *  @param e1 An Object representing a CPO element.
     *  @param e2 An Object representing a CPO element.
     *  @return One of <code>CPO.LOWER, CPO.SAME,
     *   CPO.HIGHER, CPO.INCOMPARABLE</code>.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    @Override
    public int compare(Object e1, Object e2) {
        _validate();

        int i1 = nodeLabel(e1);
        int i2 = nodeLabel(e2);

        return _compareNodeId(i1, i2);
    }

    /** Compute the down-set of an element in this CPO.
     *  @param e An Object representing an element in this CPO.
     *  @return An array of of Objects representing the elements in the
     *   down-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element in this CPO.
     */
    @Override
    public Object[] downSet(Object e) {
        _validateDual();
        return _upSetShared(e);
    }

    /** Compute the greatest element of a subset.
     *  @param subset A set of Objects representing the subset.
     *  @return An Object representing the greatest element of the subset,
     *   or <code>null</code> if the greatest element does not exist.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this CPO.
     */
    @Override
    public Object greatestElement(Set<Object> subset) {
        _validateDual();
        return _leastElementShared(subset);
    }

    /** Compute the greatest lower bound (GLB) of two elements.
     *  @param e1 An Object representing an element in this CPO.
     *  @param e2 An Object representing an element in this CPO.
     *  @return An Object representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    @Override
    public Object greatestLowerBound(Object e1, Object e2) {
        _validateDual();
        return _lubShared(e1, e2);
    }

    /** Compute the greatest lower bound (GLB) of a subset.
     *  If the specified array representing the subset has size 0,
     *  the subset is considered empty, in which case the top element
     *  of this CPO is returned, if it exists. If the subset is empty
     *  and the top does not exist, <code>null</code> is returned.
     *  @param subset A set of Objects representing the subset.
     *  @return An Object representing the GLB of the subset, or
     *   <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one Object
     *   in the specified array is not an element of this CPO.
     */
    @Override
    public Object greatestLowerBound(Set<Object> subset) {
        _validateDual();
        return _lubShared(subset);
    }

    /** Test if this CPO is a lattice.
     *  @return True if this CPO is a lattice;
     *   <code>false</code> otherwise.
     */
    @Override
    public boolean isLattice() {
        return nonLatticeReason() == null;
    }

    /** Compute the least element of a subset.
     *  @param subset A set of Objects representing the subset.
     *  @return An Object representing the least element of the subset,
     *   or <code>null</code> if the least element does not exist.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this CPO.
     */
    @Override
    public Object leastElement(Set<Object> subset) {
        _validate();
        return _leastElementShared(subset);
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  @param e1 An Object representing an element in this CPO.
     *  @param e2 An Object representing element in this CPO.
     *  @return An Object representing the LUB of the two specified
     *   elements, or <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    @Override
    public Object leastUpperBound(Object e1, Object e2) {
        _validate();
        return _lubShared(e1, e2);
    }

    /** Compute the least upper bound (LUB) of a subset.
     *  If the specified array representing the subset has size 0,
     *  the subset is considered empty, in which case the bottom element
     *  of this CPO is returned, if it exists. If the subset is empty
     *  and the bottom does not exist, <code>null</code> is returned.
     *  @param subset A set of Objects representing the subset.
     *  @return An Object representing the LUB of the subset, or
     *   <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one Object
     *   in the specified array is not an element of this CPO.
     */
    @Override
    public Object leastUpperBound(Set<Object> subset) {
        _validate();
        return _lubShared(subset);
    }

    /** Return a counterexample reason as to why this graph is not a lattice.
     *  If it is a lattice, return null.
     *  First check to see if the graph has a cycle. If it does not, then
     *  check to see if all pair combinations of elements in the graph have
     *  both a least upper bound and a greatest lower bound. The first time
     *  a counterexample is found, return it.
     *
     *  @return A counterexample that demonstrates why this graph is not a
     *   lattice, or null if it is.
     */
    public NonLatticeCounterExample nonLatticeReason() {
        try {
            _validate();

            // If there is a cycle in the graph, a runtime GraphStateException
            // will be thrown by the _validate() method.
        } catch (GraphStateException graphStateEx) {
            Node cycleNode = _findNodeWithCycle();

            // If a node in a cycle cannot be found, rethrow the
            // GraphStateException.
            if (cycleNode == null) {
                throw graphStateEx;
            } else {
                return new NonLatticeCounterExample(cycleNode.getWeight());
            }
        }

        if (nodeCount() == 0) {
            return null;
        }

        Object[] nodes = weightArray(nodes());

        for (int i = 0; i < nodes.length - 1; i++) {
            for (int j = i + 1; j < nodes.length; j++) {
                if (leastUpperBound(nodes[i], nodes[j]) == null) {
                    return new NonLatticeCounterExample(BoundType.LEASTUPPER,
                            nodes[i], nodes[j]);
                }
            }
        }

        for (int i = 0; i < nodes.length - 1; i++) {
            for (int j = i + 1; j < nodes.length; j++) {
                if (greatestLowerBound(nodes[i], nodes[j]) == null) {
                    return new NonLatticeCounterExample(
                            BoundType.GREATESTLOWER, nodes[i], nodes[j]);
                }
            }
        }

        return null;
    }

    /** Return the opposite of the given compare return code, as if the
     *  arguments had been given to compare in the reverse order.
     *  @param compareCode One of <code>CPO.SAME, CPO.HIGHER,
     *      CPO.LOWER, CPO.INCOMPARABLE</code>.
     *  @return The compare code that represents the opposite result
     *      from the given compare code.
     */
    public static final int reverseCompareCode(int compareCode) {
        if (compareCode == CPO.HIGHER) {
            return CPO.LOWER;
        } else if (compareCode == CPO.LOWER) {
            return CPO.HIGHER;
        } else {
            return compareCode;
        }
    }

    /** Return the top element of this CPO.
     *  @return An Object representing the top element, or
     *   <code>null</code> if the top does not exist.
     */
    @Override
    public Object top() {
        _validate();
        return _top;
    }

    /** Topological sort the whole graph.
     *  The implementation uses the method of A. B. Kahn: "Topological
     *  Sorting of Large Networks," <i>Communications of the ACM</i>,
     *  Vol. 5, 558-562, 1962.
     *  It has complexity O(|N|+|E|), where N for nodes and E for edges,
     *
     *  @return An array of Objects representing the nodes sorted
     *   according to the topology.
     *  @exception GraphStateException If the graph is cyclic.
     */
    public Object[] topologicalSort() {
        _validate();

        int size = nodeCount();
        int[] indeg = new int[size];

        for (int i = 0; i < size; i++) {
            indeg[i] = inputEdgeCount(node(i));
        }

        Object[] result = new Object[size];
        boolean finished = false;
        boolean active = true;
        int nextResultIndex = 0;

        while (!finished) {
            active = false;
            finished = true;

            for (int id = 0; id < size; id++) {
                if (indeg[id] > 0) {
                    active = true;
                }

                if (indeg[id] == 0) {
                    finished = false;
                    result[nextResultIndex++] = nodeWeight(id);
                    indeg[id]--;

                    Iterator outputEdges = outputEdges(node(id)).iterator();

                    while (outputEdges.hasNext()) {
                        Node sink = ((Edge) outputEdges.next()).sink();
                        indeg[nodeLabel(sink)]--;
                    }
                }
            }

            // The following codes can be removed since they are not
            // reachable. Cycles are checked by _validate() so that
            // no cyclic graphs can reach to this point.
            if (finished && active) {
                throw new GraphStateException(
                        "DirectedAcyclicGraph.topologicalSort: Graph is "
                                + "cyclic.");
            }
        }

        return result;
    }

    /** Sort the given node weights in their topological order.
     *  In other words, this method returns the specified node weights
     *  according to a topological sort of the corresponding
     *  graph nodes.
     *  This method use the transitive closure matrix. Since generally
     *  the graph is checked for cyclicity before this method is
     *  called, the use of the transitive closure matrix should
     *  not add any overhead. A bubble sort is used for the internal
     *  implementation, so the complexity is <i>O(n^2)</i>.
     *  The result is unpredictable if the multiple nodes have the same
     *  weight (i.e., if the specified weights are not uniquely
     *  associated with nodes).
     *  @param weights The given node weights.
     *  @return The weights in their sorted order.
     */
    @Override
    public Object[] topologicalSort(Object[] weights) {
        _validate();

        int N = weights.length;
        int[] ids = new int[N];

        for (int i = 0; i < N; i++) {
            ids[i] = nodeLabel(weights[i]);
        }

        for (int i = 0; i < N - 1; i++) {
            for (int j = i + 1; j < N; j++) {
                if (_compareNodeId(ids[i], ids[j]) == HIGHER) {
                    //swap
                    int tmp = ids[i];
                    ids[i] = ids[j];
                    ids[j] = tmp;
                }
            }
        }

        Object[] result = new Object[N];

        for (int i = 0; i < N; i++) {
            result[i] = nodeWeight(ids[i]);
        }

        return result;
    }

    /** Compute the up-set of an element in this CPO.
     *  @param e An Object representing an element in this CPO.
     *  @return An array of Objects representing the elements in the
     *   up-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element of this CPO.
     */
    @Override
    public Object[] upSet(Object e) {
        _validate();
        return _upSetShared(e);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create and add an edge with a specified source node, sink node,
     *  and optional weight.
     *  The third parameter specifies whether the edge is to be
     *  weighted, and the fourth parameter is the weight that is
     *  to be applied if the edge is weighted.
     *  Returns the edge that is added.
     *  @param node1 The source node of the edge.
     *  @param node2 The sink node of the edge.
     *  @param weighted True if the edge is to be weighted.
     *  @param weight The weight that is to be applied if the edge is to
     *  be weighted.
     *  @return The edge.
     *  @exception GraphConstructionException If the specified nodes
     *  are identical.
     *  @exception GraphElementException If either of the specified nodes
     *  is not in the graph.
     *  @exception NullPointerException If the edge is to be weighted, but
     *  the specified weight is null.
     */
    @Override
    protected Edge _addEdge(Node node1, Node node2, boolean weighted,
            Object weight) {
        if (node1 == node2) {
            throw new GraphConstructionException("Cannot add a self loop in "
                    + "an acyclic graph.\nA self loop was attempted on the "
                    + "following node.\n" + node1.toString());
        } else {
            return super._addEdge(node1, node2, weighted, weight);
        }
    }

    /** Initialize the list of analyses that are associated with this graph,
     *  and initialize the change counter of the graph.
     *  @see ptolemy.graph.analysis.Analysis
     */
    @Override
    protected void _initializeAnalyses() {
        super._initializeAnalyses();
        _transitiveClosureAnalysis = new TransitiveClosureAnalysis(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // compare two elements using their nodeIds using _closure.
    private int _compareNodeId(int i1, int i2) {
        if (i1 == i2) {
            return SAME;
        }

        if (_closure[i1][i2]) {
            return LOWER;
        }

        if (_closure[i2][i1]) {
            return HIGHER;
        }

        return INCOMPARABLE;
    }

    /** If the graph has a cycle, find one node on the cycle path and return it,
     *  or null if the graph has no cycles.
     *  @return A node in the graph on the cycle path, or null if the graph has
     *   no cycles.
     */
    private Node _findNodeWithCycle() {
        int cycleNodeIndex = -1;
        boolean[][] transitiveClosureMatrix = transitiveClosure();
        for (int i = 0; i < transitiveClosureMatrix.length; i++) {
            if (transitiveClosureMatrix[i][i] == true) {
                cycleNodeIndex = i;
                break;
            }
        }

        if (cycleNodeIndex < 0) {
            return null;
        } else {
            return node(cycleNodeIndex);
        }
    }

    // compute the least element of a subset nodeIds using _closure.
    // if ids.length = 0, return null.
    private Object _leastElementNodeId(int[] ids) {
        // Algorithm: Use a linked list storing all the elements incomparable
        // with at least one other. The least element, if it exists, must be
        // less than all the elements in this list. Compare the elements in
        // the ids array in consecutive pairs. Elements found  higher in a
        // pair-comparison are removed from the ids array. Elements found
        // incomparable are removed from the ids array and put into the list.
        // If two elements are found equal, one of them is arbitrarily removed
        // from the ids array. Repeat the above process until the ids array
        // contains no more than one element. In the end, if the ids array
        // contains no elements, return null. If it contains an element,
        // compare it with all the elements in the list. If it is found lower
        // than all of them, then this is the least element, otherwise there
        // exists no least element.
        // This algorithm computes the least element of a poset in O(n) time.
        // (ematsi 09/2003)
        // list of incomparable elements.
        LinkedList incomparables = new LinkedList();
        int virtualLength = ids.length;

        while (virtualLength > 1) {
            int i;
            int virtualIndex = 0;
            int numberOfRemovedElements = 0;

            for (i = 0; i < virtualLength - 1;) {
                switch (_compareNodeId(ids[i++], ids[i++])) {
                case LOWER:
                case SAME:
                    ids[virtualIndex++] = ids[i - 2];
                    numberOfRemovedElements++;
                    break;

                case HIGHER:
                    ids[virtualIndex++] = ids[i - 1];
                    numberOfRemovedElements++;
                    break;

                case INCOMPARABLE:
                    incomparables.addLast(Integer.valueOf(ids[i - 2]));
                    incomparables.addLast(Integer.valueOf(ids[i - 1]));
                    numberOfRemovedElements += 2;
                    break;

                default:
                    throw new GraphStateException(
                            "Bugs in code! Inconsistent data structure!");
                }
            }

            if (i == virtualLength - 1) {
                ids[virtualIndex] = ids[i];
            }

            virtualLength -= numberOfRemovedElements;
        }

        if (virtualLength == 0) {
            return null;
        } else if (incomparables.size() != 0) {
            for (ListIterator iterator = incomparables.listIterator(0); iterator
                    .hasNext();) {
                int result = _compareNodeId(ids[0],
                        ((Integer) iterator.next()).intValue());

                if (result == HIGHER || result == INCOMPARABLE) {
                    return null;
                }
            }
        }

        return nodeWeight(ids[0]);
    }

    // compute the least element in a subset.
    private Object _leastElementShared(Set<Object> subset) {
        if (subset.size() == 1) {
            Object obj = subset.iterator().next();
            if (containsNodeWeight(obj)) {
                return obj;
            } else {
                throw new IllegalArgumentException("Object not in CPO.");
            }
        } else if (subset.size() == 2) {
            Iterator<Object> itr = subset.iterator();
            Object o1 = itr.next();
            Object o2 = itr.next();
            int i1 = nodeLabel(o1);
            int i2 = nodeLabel(o2);

            int result = _compareNodeId(i1, i2);

            if (result == LOWER || result == SAME) {
                return o1;
            } else if (result == HIGHER) {
                return o2;
            } else { // INCOMPARABLE
                return null;
            }
        } else {
            int[] ids = new int[subset.size()];
            int i = 0;
            for (Object obj : subset) {
                ids[i] = nodeLabel(obj);
                i++;
            }

            return _leastElementNodeId(ids);
        }
    }

    // compute the lub using _closure.  This method is shared by
    // leastUpperBound() and greatestLowerBound()
    private Object _lubShared(Object e1, Object e2) {
        int i1 = nodeLabel(e1);
        int i2 = nodeLabel(e2);

        int result = _compareNodeId(i1, i2);

        if (result == LOWER || result == SAME) {
            return e2;
        } else if (result == HIGHER) {
            return e1;
        } else { // incomparable

            // an array of flags indicating if the ith element is an
            // upper bound.
            int size = nodeCount();
            boolean[] isUpperBound = new boolean[size];
            int numUpperBound = 0;

            for (int i = 0; i < size; i++) {
                isUpperBound[i] = false;

                if (_closure[i1][i] && _closure[i2][i]) {
                    isUpperBound[i] = true;
                    numUpperBound++;
                }
            }

            // if the number of upper bounds is 0, there is no upper bound.
            // else, put all upper bounds in an array.  if there is only
            // one element in array, that is the LUB; if there is more than
            // one element, find the least one, which may not exist.
            if (numUpperBound == 0) { // This CPO has no top.
                return null;
            } else {
                int[] upperBound = new int[numUpperBound];
                int count = 0;

                for (int i = 0; i < size; i++) {
                    if (isUpperBound[i]) {
                        upperBound[count++] = i;
                    }
                }

                if (numUpperBound == 1) {
                    return nodeWeight(upperBound[0]);
                } else {
                    return _leastElementNodeId(upperBound);
                }
            }
        }
    }

    // compute the lub of a subset using _closure.  This method is
    // shared by leastUpperBound() and greatestLowerBound(). This method
    // should work when subset.length = 0, in which case the top or bottom
    // of this CPO is returned, depending on whether the lub or the glb
    // is computed.
    private Object _lubShared(Set<?> subset) {
        // convert all elements to their IDs
        int[] subsetId = new int[subset.size()];
        int k = 0;
        for (Object obj : subset) {
            subsetId[k] = nodeLabel(obj);
            k++;
        }

        // find all the upper bounds
        int size = nodeCount();
        int numUB = 0;
        int[] ubId = new int[size];

        for (int i = 0; i < size; i++) {
            boolean isUB = true;

            for (int element : subsetId) {
                int compare = _compareNodeId(i, element);

                if (compare == LOWER || compare == INCOMPARABLE) {
                    isUB = false;
                    break;
                }
            }

            if (isUB) {
                ubId[numUB++] = i;
            }
        }

        // pack all the IDs of all upper bounds into an array
        int[] ids = new int[numUB];

        for (int i = 0; i < numUB; i++) {
            ids[i] = ubId[i];
        }

        return _leastElementNodeId(ids);
    }

    // compute the up-set of an element.
    private Object[] _upSetShared(Object e) {
        int id = nodeLabel(e);
        ArrayList upset = new ArrayList(_closure.length);
        upset.add(e); // up-set includes the element itself.

        for (int i = 0; i < _closure.length; i++) {
            if (_closure[id][i]) {
                upset.add(nodeWeight(i));
            }
        }

        return upset.toArray();
    }

    // call sequence (the lower methods are called by the higher ones):
    //
    // leastUpperBound     leastUpperBound([])     leastElement
    // greatestLowerBound  greatestLowerBound([])  greatestElement
    //         |                    |                    |
    //         |                    |                    |
    // _lubShared(Object) _lubShared(Object[])   _leastElementShared
    //         |                    |                    |
    //         -------------------------------------------
    //                              |
    //                  _leastElementNodeId(int[])
    //
    // downSet
    // upSet
    //   |
    // _upSetShared
    // compute transitive closure.  Throws GraphStateException if detects
    // cycles.  Find bottom and top elements.
    private void _validate() {
        if (!((CachedStrategy) _transitiveClosureAnalysis.analyzer())
                .obsolete() && isAcyclic()) {
            _closure = transitiveClosure();
            return;
        }

        boolean[][] transitiveClosure = transitiveClosure();

        if (!isAcyclic()) {
            throw new GraphStateException(
                    "DirectedAcyclicGraph._validate: Graph is cyclic.");
        }

        // find bottom
        _bottom = null;

        for (int i = 0; i < nodeCount(); i++) {
            if (inputEdgeCount(node(i)) == 0) {
                if (_bottom == null) {
                    _bottom = nodeWeight(i);
                } else {
                    _bottom = null;
                    break;
                }
            }
        }

        // find top
        _top = null;

        for (int i = 0; i < nodeCount(); i++) {
            if (outputEdgeCount(node(i)) == 0) {
                if (_top == null) {
                    _top = nodeWeight(i);
                } else {
                    _top = null;
                    break;
                }
            }
        }

        _closure = transitiveClosure;
        _tranClosureTranspose = null;
    }

    // compute the transposition of transitive closure and point _closure
    // to the transposition
    private void _validateDual() {
        _validate();

        boolean[][] transitiveClosure = transitiveClosure();

        if (_tranClosureTranspose == null) {
            int size = transitiveClosure.length;
            _tranClosureTranspose = new boolean[size][size];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    _tranClosureTranspose[i][j] = transitiveClosure[j][i];
                }
            }
        }

        _closure = _tranClosureTranspose;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // _closure = _transitiveClosure for lub, upSet, leastElement;
    // _closure = _tranClosureTranspose for the dual operations: glb,
    //   downSet, greatestElement.
    // all the private methods, exception _validate() and _validateDual(),
    // use _closure instead of _transitiveClosure or _tranClosureTranspose.
    private boolean[][] _closure = null;

    private boolean[][] _tranClosureTranspose = null;

    private Object _bottom = null;

    private Object _top = null;
}
