/** A directed acyclic graph (DAG).

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (yuhong@eecs.berkeley.edu)
@AcceptedRating Green (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.graph;

import ptolemy.kernel.util.InvalidStateException;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DirectedAcyclicGraph.java
/**
A directed acyclic graph (DAG).

The graphs constructed by this class cannot have cycles. For performance
reasons, this requirement is not checked (except for self-loops) during
the construction of the graph (calls to <code>add</code> and
<code>addEdge</code>), but is checked when any of the other methods is
called for the first time after the addition of nodes or edges. If the
graph is cyclic, an InvalidStateException is thrown. The check for cycles
is done by computing the transitive closure, so the first operation after
graph changes is slower.

This class implements the CPO interface since the Hasse diagram of a CPO
can be viewed as a DAG.  Therefore, this class can be viewed as both a DAG
and a finite CPO. In the case of CPO, the Objects representing
nodes of the graph are CPO elements. The CPO does not require the bottom
element to exist. The call to <code>bottom</code> returns
<code>null</code> if the bottom element does not exist.
<p>
NOTE: This class is a starting point for implementing graph algorithms,
more methods will be added.

@author Yuhong Xiong
@version $Id$
*/

// The methods greatestLowerBound, downSet, greatestElement share the
// same code with their duals, leastUpperBound, upSet, leastElement.
// The only thing different is the use of the transposition of the
// transitive closure instead of the original transitive closure.
// In another word, the computation of greatestLowerBound, downSet,
// greatestElement is converted to their dual operation by reversing
// the order relation in this CPO.

public class DirectedAcyclicGraph extends DirectedGraph implements CPO
{
    /** Construct an empty DAG.
     */
    public DirectedAcyclicGraph() {
        super();
    }

    /** Construct an empty DAG with enough storage allocated
     *  for the specified number of elements.  Memory management is more
     *  efficient with this constructor if the number of elements is
     *  known.
     *  @param elementCount the number of elements.
     */
    public DirectedAcyclicGraph(int nodeCount) {
        super(nodeCount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a node to this DAG.  The node is represented by the
     *  specified <code>Object</code>. The <code>Object</code> cannot
     *  be <code>null</code>.  In addition, two <code>Object</code>s
     *  equal to each other, as determined by the <code>equals</code>
     *  method, cannot both be added.<p>
     *
     *  After nodes are added to a graph, The node Objects should not
     *  be changed in such a way that the Objects representing
     *  distinct nodes are equal, as determined by the
     *  <code>equals</code> method. Doing so may generate unexpected
     *  results.
     *
     *  @param o the Object representing a graph node.
     *  @exception IllegalArgumentException If an Object equals to the
     *   specified one is already in this DAG.
     *  @exception NullPointerException If the specified Object is
     *   <code>null</code>.
     */
    public void add(Object o) {
        super.add(o);

        _invalidate();
    }

    /** Add a directed edge to connect two nodes. That is, make the first
     *  argument lower than the second.  Multiple connections
     *  between two nodes are allowed, and are considered different
     *  edges. When this class is used to model a finite CPO, multiple
     *  connections between the same nodes are redundant and have no
     *  effects on CPO operations. Self loop is not allowed.
     *
     *  @param o1 the Object representing the lower node.
     *  @param o2 the Object representing the higher node.
     *  @exception IllegalArgumentException If the two nodes are equal,
     *   as determined by the <code>equals</code> method.
     *  @exception NullPointerException If at least one of the specified
     *   Objects is <code>null</code>.
     */
    public void addEdge(Object o1, Object o2) {
        if (o1.equals(o2)) {
            throw new IllegalArgumentException("DirectedAcyclicGraph.addEdge: "
                    + "Cannot add a self loop in acyclic graph.");
        }

        super.addEdge(o1, o2);
        _invalidate();
    }

    /** Return the bottom element of this CPO.
     *  @return an Object representing the bottom element, or
     *   <code>null</code> if the bottom does not exist.
     */
    public Object bottom() {
        _validate();
        return _bottom;
    }

    /** Compare two elements in this CPO.
     *  @param e1 an Object representing a CPO element.
     *  @param e2 an Object representing a CPO element.
     *  @return one of <code>CPO.LOWER, CPO.SAME,
     *   CPO.HIGHER, CPO.INCOMPARABLE</code>.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public int compare(Object e1, Object e2) {
        _validate();

        int i1 = _getNodeId(e1);
        int i2 = _getNodeId(e2);

        return _compareNodeId(i1, i2);
    }

    /** Compute the down-set of an element in this CPO.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of of Objects representing the elements in the
     *   down-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element in this CPO.
     */
    public Object[] downSet(Object e) {
        _validateDual();
        return _upSetShared(e);
    }

    /** Compute the greatest lower bound (GLB) of two elements.
     *  @param e1 an Object representing an element in this CPO.
     *  @param e2 an Object representing an element in this CPO.
     *  @return an Object representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public Object greatestLowerBound(Object e1, Object e2) {
        _validateDual();
        return _lubShared(e1, e2);
    }

    /** Compute the greatest lower bound (GLB) of a subset.
     *  If the specified array representing the subset has size 0,
     *  the subset is considered empty, in which case the top element
     *  of this CPO is returned, if it exists. If the subset is empty
     *  and the top does not exist, <code>null</code> is returned.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the GLB of the subset, or
     *   <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one Object
     *   in the specified array is not an element of this CPO.
     */
    public Object greatestLowerBound(Object[] subset) {
        _validateDual();
        return _lubShared(subset);
    }

    /** Compute the greatest element of a subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the greatest element of the subset,
     *   or <code>null</code> if the greatest element does not exist.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this CPO.
     */
    public Object greatestElement(Object[] subset) {
        _validateDual();
        return _leastElementShared(subset);
    }

    /** Test if this CPO is a lattice.
     *  By a theorem in Davey and Priestley, only the LUB or the GLB
     *  need to be checked, but not both. The implementation tests the
     *  existence of the LUB of any pair of elements, as well as the
     *  existence of the bottom and top elements. The complexity is
     *  O(|N|*|N|) where N for elements, and an individual computation
     *  is the LUB of two elements.
     *  @return <code>true</code> if this CPO is a lattice;
     *   <code>false</code> otherwise.
     */
    public boolean isLattice() {
	_validate();

	if (bottom() == null || top() == null) {
	    return false;
	}

	Object[] nodes = getNodes();
	for (int i = 0; i < nodes.length-1; i++) {
	    for (int j = i+1; j < nodes.length; j++) {
		if (leastUpperBound(nodes[i], nodes[j]) == null) {
		    return false;
		}
	    }
	}
	return true;
    }

    /** Compute the least element of a subset.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the least element of the subset,
     *   or <code>null</code> if the least element does not exist.
     *  @exception IllegalArgumentException If at least one Object in the
     *   specified array is not an element of this CPO.
     */
    public Object leastElement(Object[] subset) {
        _validate();
        return _leastElementShared(subset);
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  @param e1 an Object representing an element in this CPO.
     *  @param e2 an Object representing element in this CPO.
     *  @return an Object representing the LUB of the two specified
     *   elements, or <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this CPO.
     */
    public Object leastUpperBound(Object e1, Object e2) {
        _validate();
        return _lubShared(e1, e2);
    }

    /** Compute the least upper bound (LUB) of a subset.
     *  If the specified array representing the subset has size 0,
     *  the subset is considered empty, in which case the bottom element
     *  of this CPO is returned, if it exists. If the subset is empty
     *  and the bottom does not exist, <code>null</code> is returned.
     *  @param subset an array of Objects representing the subset.
     *  @return an Object representing the LUB of the subset, or
     *   <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one Object
     *   in the specified array is not an element of this CPO.
     */
    public Object leastUpperBound(Object[] subset) {
        _validate();
        return _lubShared(subset);
    }

    /** Return the top element of this CPO.
     *  @return an Object representing the top element, or
     *   <code>null</code> if the top does not exist.
     */
    public Object top() {
        _validate();
        return _top;
    }

    /** Topological sort the whole graph.
     *  The implementation uses the method of A.B. Kahn: "Topological
     *  Sorting of Large Networks", Communications of the ACM,
     *  Vol. 5, 558-562, 1962.
     *  It has complexity O(|N|+|E|), where N for nodes and E for edges,
     *
     *  @return an array of Objects representing the nodes sorted
     *   according to the topology.
     *  @exception InvalidStateException If the graph is cyclic.
     */
    public Object[] topologicalSort() {
        int size = getNodeCount();
        int[] indeg = new int[size];
        for (int i = 0; i < size; i++) {
            indeg[i] = ((Integer)_inDegree.get(i)).intValue();
        }
        Object[] result = new Object[size];
        boolean finished = false;
        boolean active = true;
        int nextResultIndex = 0;
        while (!finished) {
            active = false;
            finished = true;
            for (int id = 0; id < size; id++) {
                if(indeg[id] > 0) {
                    active = true;
                }
                if(indeg[id] == 0) {
                    finished = false;
                    result[nextResultIndex++] = _getNodeObject(id);
                    indeg[id]--;
                    ArrayList arcs = (ArrayList)(_graph.get(id));
                    for(int i = 0; i < arcs.size(); i++) {
                        int item = ((Integer)(arcs.get(i))).intValue();
                        indeg[item]--;
                    }
                }
            }
            if(finished && active) {
                throw new InvalidStateException(
                    "DirectedAcyclicGraph.topologicalSort: Graph is cyclic.");
            }
        }
        return result;
    }

    /** Sort the given graph objects in their topological order.
     *  This method use the transitive closure matrix. Since generally
     *  the graph is checked for cyclicity before this method is
     *  called, the use of the transitive closure matrix should
     *  not add any overhead. A bubble sort is used for the internal
     *  implementation, so the complexity is n^2.
     *  @return The objects in there sorted order.
     */
    public Object[] topologicalSort(Object[] objs) {
        _validate();
        int N = objs.length;
        int[] ids = new int[N];
        for (int i = 0; i < N; i++) {
            ids[i] = _getNodeId(objs[i]);
        }
        for (int i = 0; i < N-1; i++) {
            for (int j = i+1; j < N; j++) {
                if(_compareNodeId(ids[i], ids[j]) == HIGHER) {
                    //swap
                    int tmp = ids[i];
                    ids[i] = ids[j];
                    ids[j] = tmp;
                }
            }
        }
        Object[] result = new Object[N];
        for (int i = 0; i < N; i++) {
            result[i] = _getNodeObject(ids[i]);
        }
        return result;
    }

    /** Compute the up-set of an element in this CPO.
     *  @param e an Object representing an element in this CPO.
     *  @return an array of Objects representing the elements in the
     *   up-set of the specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element of this CPO.
     */
    public Object[] upSet(Object e) {
        _validate();
        return _upSetShared(e);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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

    // set the flag indicating the graph is modified so some data structures
    // are invalid.
    private void _invalidate() {
	_invalid = true;
    }

    // compute transitive closure.  Throws InvalidStateException if detects
    // cycles.  Find bottom and top elements.
    private void _validate() {
        if ( !_invalid) {
            _closure = _transitiveClosure;
            return;
        }

        _computeTransitiveClosure();
        if ( !isAcyclic()) {
            throw new InvalidStateException("DirectedAcyclicGraph._validate: "
			+ "Graph is cyclic.");
        }

        // find bottom
        _bottom = null;
        for (int i = 0; i < getNodeCount(); i++) {
            if (((Integer)(_inDegree.get(i))).intValue() == 0) {
                if (_bottom == null) {
                    _bottom = _getNodeObject(i);
                } else {
		    _bottom = null;
		    break;
                }
            }
        }

	// find top
	_top = null;
	for (int i = 0; i < getNodeCount(); i++) {
	    int outdegree = ((ArrayList)(_graph.get(i))).size();
	    if (outdegree == 0) {
		if (_top == null) {
		    _top = _getNodeObject(i);
		} else {
		    _top = null;
		    break;
		}
	    }
	}

        _closure = _transitiveClosure;
        _tranClosureTranspose = null;

        _invalid = false;
    }

    // compute the transposition of transitive closure and point _closure
    // to the transposition
    private void _validateDual() {
        _validate();

        if (_tranClosureTranspose == null) {
            int size = _transitiveClosure.length;
            _tranClosureTranspose = new boolean[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    _tranClosureTranspose[i][j] = _transitiveClosure[j][i];
                }
            }
        }

        _closure = _tranClosureTranspose;
    }

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

    // compute the least element of a subset nodeIds using _closure.
    // if ids.length = 0, return null.
    private Object _leastElementNodeId(int[] ids) {

        // Algorithm: use 2 data structures: (1)a linked list storing all
        // the upper bounds incomparable with one another.  The least
        // element (if exists) must be less than all the elements in this
        // list. (2)an int storing the nodeId of a least element
        // candidate( == -1 if no candidate).  Scan all the elements in the
        // ids array, for each current element(CE) in ids, there are 4 cases:
        // (1) (candidate == -1 && list empty)
        //         candidate = CE;
        // (2) (candidate != -1 && list empty) {
        //         if (CE >= candidate) {
        //             discard CE
        //         } else if (CE < candidate) {
        //             replace candidate with CE
        //         } else {
        //             put both candidate and CE to list;
        //             candidate = -1;
        //         }
        // (3) (candidate == -1 && list not empty)
        //         if (CE >= any element in list) {
        //             discard CE;
        //         } else if (CE < all elements in list) {
        //             empty list and candidate = CE;
        //         } else {
	//	       // CE is less than some elements in list, but
	//	       // incomparable with others
        //             remove all elements in list that > CE;
        //             insert CE in list;
        //         }
        //  (4) (candidate != -1 && list not empty)
        //         ERROR!

        // list of incomparable elements.
        LinkedList incompList = new LinkedList();
        int candidate = -1;

        for (int i = 0; i < ids.length; i++) {
            boolean listEmpty = incompList.size() == 0;
            if (candidate == -1 && listEmpty) {
		// case (1)
                candidate = ids[i];
            } else if (candidate != -1 && listEmpty) {
		// case (2)
                int result = _compareNodeId(ids[i], candidate);
                if (result == LOWER) {
                    candidate = ids[i];
                } else if (result == INCOMPARABLE) {

                    incompList.addLast(new Integer(candidate));
                    incompList.addLast(new Integer(ids[i]));

                    candidate = -1;
                }
            } else if (candidate == -1 && !listEmpty) {
		// case (3)
		// flag indicating if the current element should be discarded
                boolean discard = false;

                for (ListIterator iter = incompList.listIterator(0);
                     iter.hasNext() ;) {
                    int listValue = ((Integer)iter.next()).intValue();
                    int result = _compareNodeId(ids[i], listValue);
                    if (result == LOWER) {
                        iter.remove();
                    } else if (result == HIGHER || result == SAME) {
                        discard = true;
                        break;
                    }
                }


                if (incompList.size() == 0) {
                    candidate = ids[i];
                } else if ( !discard) {

                    incompList.addLast(new Integer(ids[i]));

                }
            } else {
		// case (4)
		// candidate != -1 && !listEmpty
                throw new InvalidStateException("bug in code! " +
                        "Inconsistent data structure!");
            }
        }

        if (candidate == -1) {
            return null;
        } else {
            return _getNodeObject(candidate);
        }
    }

    // compute the least element in a subset.
    private Object _leastElementShared(Object[] subset) {
        if (subset.length == 1) {
            if (contains(subset[0])) {
                return subset[0];
            } else {
                throw new IllegalArgumentException("Object not in CPO.");
            }
        } else if (subset.length == 2) {
            int i1 = _getNodeId(subset[0]);
            int i2 = _getNodeId(subset[1]);

            int result = _compareNodeId(i1, i2);
            if (result == LOWER || result == SAME) {
                return subset[0];
            } else if (result == HIGHER) {
                return subset[1];
            } else {       // INCOMPARABLE
                return null;
            }
        } else {
            int[] ids = new int[subset.length];
            for (int i = 0; i < subset.length; i++) {
                ids[i] = _getNodeId(subset[i]);
            }
            return _leastElementNodeId(ids);
        }
    }

    // compute the lub using _closure.  This method is shared by
    // leastUpperBound() and greatestLowerBound()
    private Object _lubShared(Object e1, Object e2) {
        int i1 = _getNodeId(e1);
        int i2 = _getNodeId(e2);

        int result = _compareNodeId(i1, i2);
        if (result == LOWER || result == SAME) {
            return e2;
        } else if (result == HIGHER) {
            return e1;
        } else {    // incomparable
            // an array of flags indicating if the ith element is an
            // upper bound.
            int size = getNodeCount();
            boolean[] isUpperBD = new boolean[size];
            int numUpperBD = 0;
            for (int i = 0; i < size; i++) {
                isUpperBD[i] = false;
		if (_closure[i1][i] && _closure[i2][i]) {
                    isUpperBD[i] = true;
                    numUpperBD++;
                }
            }

            // if the number of upper bounds is 0, there is no upper bound.
            // else, put all upper bounds in an array.  if there is only
            // one element in array, that is the LUB; if there is more than
            // one element, find the least one, which may not exist.
            if (numUpperBD == 0) {     // BTW, this CPO has no top.
                return null;
            } else {
                int[] upperBD = new int[numUpperBD];
                int count = 0;
                for (int i = 0; i < size; i++) {
                    if (isUpperBD[i]) {
                        upperBD[count++] = i;
                    }
                }

                if (numUpperBD == 1) {
                    return _getNodeObject(upperBD[0]);
                } else {
                    return _leastElementNodeId(upperBD);
                }
            }
        }
    }

    // compute the lub of a subset using _closure.  This method is
    // shared by leastUpperBound() and greatestLowerBound(). This method
    // should work when subset.length = 0, in which case the top or bottom
    // of this CPO is returned, depending on whether the lub or the glb
    // is computed.
    private Object _lubShared(Object[] subset) {
	// convert all elements to their IDs
	int[] subsetId = new int[subset.length];
	for (int i = 0; i < subset.length; i++) {
	    subsetId[i] = _getNodeId(subset[i]);
	}

	// find all the upper bounds
	int size = getNodeCount();
	int numUB = 0;
	int[] ubId = new int[size];
	for (int i = 0; i < size; i++) {
	    boolean isUB = true;
	    for (int j = 0; j < subsetId.length; j++) {
		int compare = _compareNodeId(i, subsetId[j]);
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
        int id = _getNodeId(e);
        ArrayList upset = new ArrayList(_closure.length);
        upset.add(e);    // up-set includes the element itself.
        for (int i = 0; i < _closure.length; i++) {
            if (_closure[id][i]) {
                upset.add(_getNodeObject(i));
            }
        }

        return upset.toArray();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // flag indicating that the graph is modified so some data structures
    // are invalid.
    private boolean _invalid = true;

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
