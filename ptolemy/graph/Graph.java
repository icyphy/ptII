/* A mutable graph.

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
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// Graph
/** 
A mutable graph.
This class is evolved from Jie's static graph classes.
Each node in the graph is associated with an Object specified
by the user.

@author Yuhong Xiong, Jie Liu
@version $Id$
*/

public class Graph {

    /** Constructs an empty graph.
     */
    public Graph() {
        _graph = new Vector();
        _backRef = new Vector();
        _nodeIdTable = new Hashtable();
    }
    
    /** Constructs an empty graph with enough storage allocated for the
     *  specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param numNodes the integer specifying the number of nodes
     */ 
    public Graph(int numNodes) {
        _graph = new Vector(numNodes);
        _backRef = new Vector(numNodes);
        _nodeIdTable = new Hashtable(numNodes);
    }
        
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Adds a node to this graph.  The node is associated with the
     *  spedified object. The object can't be null.  In addition, two
     *  objects equal to each other, as determined by the
     *  <code>equals</code> method, can't both be added.
     *
     *  @param o the object associated with the node to be added.
     *  @exception IllegalArgumentException an object equals to the
     *  specified one is already associated with a node in this graph,
     *  where equality is determined by the <code>equals</code> method.
     *  @exception NullPointerException object is null.
     */
    public void add(Object o) {
        if (contains(o)) {
            throw new IllegalArgumentException("Object already added.");
        }
        _backRef.addElement(o);
        _graph.addElement(new Vector());
        _nodeIdTable.put(o, new Integer(_graph.size() - 1));
    }
 
    /** Adds an edge to connect two nodes.  Multiple connections between
     *  two nodes are allowed, but they are counted separately.  Self loop
     *  is also allowed.
     *
     *  @param o1 the object associated with one of the nodes connected
     *  by the edge.
     *  @param o2 the object associated with one of the nodes connected
     *  by the edge.
     *  @exception IllegalArgumentException at least one object is not
     *  equal to an object associated with the nodes in this graph, as
     *  determined by the <code>equals</code> method.
     */ 
     public void addEdge(Object o1, Object o2) {        
        int id1 = _getNodeId(o1);
        int id2 = _getNodeId(o2);
        
        ((Vector)(_graph.elementAt(id1))).addElement(new Integer(id2));
	_numEdges++;
    }

    /** Returns all the edges in this graph in the form of a 2-D object
     *  array. Multiple connections between two nodes have multiple
     *  entries in the array.
     *  Each entry in the first array dimension represents an edge.
     *  The second dimension always has two entries, holding the two
     *  objects connected by the edge.  For example, Object[i][0] and
     *  Object[i][1] are the two objects connected by the i'th edge.
     *  In the derived class <code>DirectedGraph</code>, Object[i][0]
     *  is the source node, Object[i][1] is the sink node.
     *  @ return an array of objects representing all the edges.
     */
    public Object[][] allEdges() {
	Object[][] result = new Object[_numEdges][2];

	int count = 0;
	for (int i = 0; i < numNodes(); i++) {
	    Object source = _backRef.elementAt(i);
	    Vector sinkIds = (Vector)_graph.elementAt(i);
	    for (int j = 0; j < sinkIds.size(); j++) {
		int id = ((Integer)(sinkIds.elementAt(j))).intValue();
		Object sink = _backRef.elementAt(id);
		result[count][0] = source;
		result[count++][1] = sink;
	    }
	}

	return result;
    }

    /** Returns all the nodes in this graph in the form of an
     *  array of objects.  The objects are the ones passed to the
     *  <code>add()</code> method.
     *  @return an array of objects representing all the nodes in this graph.
     */
    public Object[] allNodes() {

	// FIXME: restore this line for jdk1.2
//	return _backRef.toArray();

	// FIXME: delete the following code for jdk1.2
	Object[] arr = new Object[numNodes()];
	for (int i = 0; i < numNodes(); i++) {
	    arr[i] = _backRef.elementAt(i);
	}
	return arr;
    }

    /** Returns the total number of edges in this graph.  Multiple
     *  connections between two nodes are counted multiple times.
     *  @return the total number of edges in this graph.
     */
    public int numEdges() {
	return _numEdges;
    }

    /** Returns the total number of nodes in this graph.
     *  @return the total number of nodes in this graph.
     */
    public int numNodes() {
        return _backRef.size();
    }

    /** Tests if the specified object is equal to an object associated
     *  with a node in this graph.  The equality is determined by the
     *  <code>equals</code> method.
     *  @param o the Object to be tested.
     *  @return <code>true</code> if the an object equals to the specified
     *  one is associated with a node in this graph; <code>false</code>
     *  otherwise.
     */
    public boolean contains(Object o) {
        return _nodeIdTable.containsKey(o);
    }

    /** Test if this graph is directed.
     *  @return alway returns <code>false</code> in this base class.
     */
    public boolean isDirected() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Returns the node id of the node associated with the specified Object.
     *  @param o an object associated with a graph node.
     *  @return the node id.
     *  @exception IllegalArgumentException if the specified object is not
     *   associated with a node in this graph.
     */
    protected int _getNodeId(Object o) {
        Integer v = (Integer)(_nodeIdTable.get(o));
        if (v == null) {
            throw new IllegalArgumentException("Argument: " + o.toString()
			+ " not associated with a node in graph.");
        }
        
        return v.intValue();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                          private methods                       ////

    ////////////////////////////////////////////////////////////////////////
    ////                        protected variables                     ////

    /** The adjacency list representation of a graph.
     *  This vector is indexed by the node id, each entry is a vector of
     *  <code>Integers</code> containing the node ids of nodes that can
     *  be reached in one step.
     */ 
    protected Vector _graph;
    
    /** Tranlation from nodeId to associated Object.
     *  This vector is indexed by node id and stores the object associated
     *  with each node id.
     */
    protected Vector _backRef;
    
    ////////////////////////////////////////////////////////////////////////
    ////                        protected variables                     ////

    // Translation from Object to nodeId.  This can be down with
    // _backRef.indexOf(), but Hashtable is faster.
    private Hashtable _nodeIdTable;

    // number of edges in this graph
    private int _numEdges = 0;
}

