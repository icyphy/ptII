/* A mutable directed graph, and related graph algorithms.

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

package pt.graph;

import pt.kernel.util.InvalidStateException;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// DirectedGraph
/** 
A mutable directed graph, and associated graph algorithms.
This class is evolved from Jie's static graph classes.

@author Yuhong Xiong, Jie Liu
@version $Id$
*/

public class DirectedGraph extends Graph {
    /** Contruct an empty directed graph.
     */
    public DirectedGraph() {
        super();
        _inDegree = new Vector();
    }

    /** Construct an empty directed graph with enough storage allocated
     *  for the specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param numNodes the integer specifying the number of nodes.
     */ 
 
    public DirectedGraph(int numNodes) {
        super(numNodes);
        _inDegree = new Vector(numNodes);
    }
        
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Adds a node to this graph.  The node is associated with the
     *  spedified object. The object can't be null.  In addition, two
     *  objects equal to each other, as determined by the
     *  <code>equals</code> method, can't both be added.
     *
     *  @param o the Object associated with the node to be added.
     *  @exception IllegalArgumentException an object equals to the
     *  specified one is already associated with a node in this graph,
     *  where equality is determined by the <code>equals</code> method.
     *  @exception NullPointerException the specified object is null.
     */
    public void add(Object o) {
        super.add(o);

        _inDegree.addElement(new Integer(0));
    }
        
    /** Adds a directed edge to connect twe nodes.  Multiple connections
     *  between two nodes are allowed, but they are counted separately.
     *  Self loop is also allowed.
     *
     *  @param o1 the Object associated with the source node in directed
     *  graph.
     *  @param o2 the Object associated with the sink node in directed graph.
     *  @exception IllegalArgumentException at least one object is not
     *  equal to an object associated with the nodes in this graph, as
     *  determined by the <code>equals</code> method.
     */ 
     public void addEdge(Object o1, Object o2) {
        super.addEdge(o1, o2);

        int id2 = _getNodeId(o2);
        int indeg = ((Integer)(_inDegree.elementAt(id2))).intValue();
        _inDegree.setElementAt(new Integer(indeg+1), id2);
    }

    /** Test if this graph is directed.
     *  @return always returns <code>true</code>.
     */
    public boolean isDirected() {
        return true;
    } 

    /** Tests if this graph is acyclic(is a DAG).
     *  The implementation computes the transitive closure of the
     *  graph, if it is not already computed after the last graph
     *  mutation.  So the first call to this method after graph
     *  mutation may be slow, but all the subsequent calls returns
     *  in constant time.
     *  @return <code>true</code> if the the graph is a DAG;
     *  <code>false</code> otherwise.
     */
    public boolean isAcyclic() {
        _compTranClosure();
        
        return _isAcyclic;
    }

    /** Finds all the nodes reacheable from the specified one.
     *  @param o an object associated with a node in this graph.
     *  @return an array of objects associated with the reacheable
     *   nodes.
     *  @exception IllegalArgumentException the specified object is
     *   not associated with a node in this graph.
     */
    public Object[] reacheableNodes(Object o) {
	_compTranClosure();

	int id = _getNodeId(o);
	Vector nodes = new Vector(_tranClosure.length);
	for (int i = 0; i < _tranClosure.length; i++) {
	    if (_tranClosure[id][i]) {
		nodes.addElement(_backRef.elementAt(i));
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
 
    /** Topological sort of this directed graph. 
     * The implementation uses the method of A.B. Kahn: ``Topological
     * Sorting of Large Networks", Communications of the ACM, 
     * Vol. 5, 558-562, 1962. 
     * It has complexity O(|N|+|E|), where N for nodes and E for edges.
     *
     * @return an array of objects represents an enumeration of the nodes
     * sorted according to the topology.
     * @exception InvalidStateException if the graph is cyclic.
     */
    public Object[] topSort() {
        int size = numNodes();
        int[] indeg = new int[size];
        for (int i = 0; i < size; i++) {
            indeg[i] = ((Integer)_inDegree.elementAt(i)).intValue();
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
                    result[nextResultIndex++] = _backRef.elementAt(id);
                    indeg[id]--;
                    Vector arcs = (Vector)(_graph.elementAt(id));
                    for(int i = 0; i < arcs.size(); i++) {
                        int item = ((Integer)(arcs.elementAt(i))).intValue();
                        indeg[item]--;
                    }
                }
            }
            if(finished && active) {
                throw new InvalidStateException("Graph is cyclic.");
            }
        }
        return result;
    } 

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /* Computes the transitive closure.
     * @return a boolean matrix representing the adjacency matrix of the
     *  transitive closure.
     */ 
    protected boolean[][] _compTranClosure() {
        if ( !_modified) {
            return _tranClosure;
        }

        int size = numNodes();
        if (size == 0) {          // graph empty
            throw new InvalidStateException("graph empty.");
        }

        _tranClosure = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                _tranClosure[i][j] = false;
            }

            Vector edge = (Vector)(_graph.elementAt(i));
                for (int j = 0; j < edge.size(); j++) {
                int k = ((Integer)edge.elementAt(j)).intValue();
                _tranClosure[i][k] = true;
            }
        }

        // Warshall's algorithm
        // FIXME: check the algorithm LEDA uses and see if it's
        // more efficient.
        for (int k=0; k<size; k++) {
            for (int i=0; i<size; i++) {
                for (int j=0; j<size; j++) {
                    _tranClosure[i][j] |= _tranClosure[i][k] &
                    _tranClosure[k][j];
                }
            }
        }
       
        // check for cycles.
        _isAcyclic = true;
        for (int i = 0; i < size; i++) {
            if (_tranClosure[i][i]) {
                _isAcyclic = false;
            }
        }
        
        _modified = false;
	return _tranClosure;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                        protected variables                     ////

    /** The in-degree of each node.
     *  This vector is indexed by node id with each entry an
     *  <code>Integer</code> containing the in-degree of the corresponding
     *  node.
     */
    protected Vector _inDegree;
        
    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////    

    private boolean[][] _tranClosure;
    private boolean _isAcyclic;
    private boolean _modified = true;
}

