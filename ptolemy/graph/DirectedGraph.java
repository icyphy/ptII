/* A directed graph and some graph algorithms.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

import ptolemy.kernel.util.InvalidStateException;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// DirectedGraph
/** 
A directed graph and some graph algorithms.
This class is evolved from the StaticGraph class written by Jie Liu.
<p>
NOTE: This class is a starting point for implementing graph algorithms,
more methods will be added.

@author Yuhong Xiong, Jie Liu
$Id$
*/

public class DirectedGraph extends Graph {

    /** Construct an empty directed graph.
     */
    public DirectedGraph() {
        super();
    }

    /** Construct an empty directed graph with enough storage allocated
     *  for the specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param nodeCount the integer specifying the number of nodes
     */ 
    public DirectedGraph(int nodeCount) {
        super(nodeCount);
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a node to this graph.  The node is represented by the
     *  specified Object. The Object cannot be <code>null</code>.
     *  In addition, two Objects equal to each other, as determined
     *  by the <code>equals</code> method, cannot both be added.
     *
     *  @param o the Object representing a graph node
     *  @exception IllegalArgumentException an Object equals to the
     *   specified one is already in this graph.
     *  @exception NullPointerException the specified Object is
     *   <code>null</code>.
     *  @see ptolemy.graph.Graph#add
     */
    public void add(Object o) {
        super.add(o);

        _inDegree.addElement(new Integer(0));
	_transitiveClosure = null;
    }
 
    /** Add a directed edge to connect two nodes. The first argument
     *  is the lower node and the second the higher.  Multiple connections
     *  between two nodes are allowed, and are considered different
     *  edges. Self loop is also allowed.
     *
     *  @param o1 the Object representing the lower node
     *  @param o2 the Object representing the higher node
     *  @exception IllegalArgumentException at least one of the arguments
     *   is not a graph node, i.e., the argument is not equal to an Object
     *   specified in a successful <code>add</code> call. Equality
     *   is determined by the <code>equals</code> method.
     */ 
    public void addEdge(Object o1, Object o2) {
        super.addEdge(o1, o2);

        int id2 = _getNodeId(o2);
        int indeg = ((Integer)(_inDegree.elementAt(id2))).intValue();
        _inDegree.setElementAt(new Integer(indeg+1), id2);
	_transitiveClosure = null;
    }

    /** Test if this graph is acyclic (is a DAG).
     *  The implementation computes the transitive closure of the
     *  graph, if it is not already computed after the last change to
     *  this graph.  So the first call to this method after graph change
     *  may be slow, but all the subsequent calls returns in constant
     *  time.
     *  @return <code>true</code> if the the graph is acyclic, or
     *   empty; <code>false</code> otherwise.
     */
    public boolean isAcyclic() {
        _computeTransitiveClosure();
 
        return _isAcyclic;
    }

    /** Find all the nodes that can be reached from the specified node.
     *  The reachable nodes do not include the specific one unless 
     *  there is a loop from the specified node back to itself.
     *  The implementation computes the transitive closure of the
     *  graph, if it is not already computed after the last graph
     *  change.  So the first call to this method after graph
     *  change may be slow, but all the subsequent calls return
     *  in constant time.
     *  @param o an Object representing a node in this graph.
     *  @return an array of Objects representing nodes reachable from
     *   the specified one.
     *  @exception IllegalArgumentException the specified Object is
     *   not a node in this graph.
     */
    public Object[] reachableNodes(Object o) {
	_computeTransitiveClosure();

	int id = _getNodeId(o);
	Vector nodes = new Vector(_transitiveClosure.length);
	for (int i = 0; i < _transitiveClosure.length; i++) {
	    if (_transitiveClosure[id][i]) {
		nodes.addElement(_getNodeObject(i));
	    }
	}

        // FIXME: restore the following line when moving to jdk1.2
        //	return nodes.toArray();
 
        // FIXME: remove the following lines when moving to jdk1.2
        Object[] arr = new Object[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            arr[i] = nodes.elementAt(i);
        }
        return arr;
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compute the transitive closure. Puts the result in the
     *  boolean array _transitiveClosure. If this graph is empty,
     *  set the dimension of _transitiveClosure to be 0 by 0.
     *  The implementation uses Warshall's algorithm, which can be
     *  found in chapter 6 of "Discrete Mathematics and Its
     *  Applications", 3rd Ed., by Kenneth H. Rosen.  The complexity
     *  of this algorithm is O(|N|^3), where N for nodes.
     */ 
    // This method also checks if the graph is acyclic and set
    // _isAcyclic.
    protected void _computeTransitiveClosure() {
        if (_transitiveClosure != null) {
            return;
        }

        int size = getNodeCount();

	// Initialize _transitiveClosure to the adjacency matrix
        _transitiveClosure = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                _transitiveClosure[i][j] = false;
            }

            Vector edge = (Vector)(_graph.elementAt(i));
            for (int j = 0; j < edge.size(); j++) {
                int k = ((Integer)edge.elementAt(j)).intValue();
                _transitiveClosure[i][k] = true;
            }
        }

        // Warshall's algorithm
        for (int k=0; k<size; k++) {
            for (int i=0; i<size; i++) {
                for (int j=0; j<size; j++) {
                    _transitiveClosure[i][j] |= _transitiveClosure[i][k] &
                        _transitiveClosure[k][j];
                }
            }
        }
 
        // check for cycles.
        _isAcyclic = true;
        for (int i = 0; i < size; i++) {
            if (_transitiveClosure[i][i]) {
                _isAcyclic = false;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The in-degree of each node.
     *  This vector is indexed by node ID with each entry an
     *  <code>Integer</code> containing the in-degree of the
     *  corresponding node.
     */
    protected Vector _inDegree = new Vector();

    /** The adjacency matrix representation of the transitive closure.
     *  The entry (i, j) is <code>true</code> if and only if there
     *  exists a path from the node with ID i to the node with ID j.
     *  This array is computed by <code>_computeTransitiveClosure</code>.
     *  After each graph change, that method should be called before
     *  this array is used. Otherwise, this array is not valid.
     */
    protected boolean[][] _transitiveClosure = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _isAcyclic;
}

