/* An undirected graph.

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
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// Graph
/** 
An undirected graph.
This class is evolved from the staticGraph class written by Jie Liu.
Each node in the graph is represented by an Object. To construct a graph,
use <code>add()</code> to add nodes and <code>addEdge()</code> to add
edges between nodes.

@author Yuhong Xiong, Jie Liu 
$Id$
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
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Adds a node to this graph.  The node is represented by the
     *  specified Object. The Object cannot be <code>null</code>.
     *  In addition, two Objects equal to each other, as determined
     *  by the <code>equals</code> method, cannot both be added.<p>
     *
     *  After nodes are added to a graph, The node Objects should not
     *  be changed in such a way that the Objects representing
     *  distinct nodes are equal, as determined by the
     *  <code>equals</code> method. Doing so may generate unexpected
     *  results.
     *
     *  @param o the Object representing a graph node
     *  @exception IllegalArgumentException an Object equals to the
     *   specified one is already in this graph.
     *  @exception NullPointerException the specified Object is
     *   <code>null</code>.
     */
    public void add(Object o) {
        if (contains(o)) {
            throw new IllegalArgumentException("Graph.add: Object is " +
			"already in the graph.");
        }
        _backRef.addElement(o);
        _graph.addElement(new Vector());
        _nodeIdTable.put(o, new Integer(_graph.size() - 1));
    }
 
    /** Adds an edge between two nodes.  Multiple edges
     *  between the same nodes are allowed, and are considered different
     *  edges.  Self loop is also allowed.
     *
     *  @param o1 the Object representing one graph node
     *  @param o2 the Object representing another graph node
     *  @exception IllegalArgumentException at least one of the arguments
     *   is not a graph node, i.e., the argument is not equal to an
     *   Object specified in a successful <code>add</code> call. Equality
     *   is determined by the <code>equals</code> method.
     */ 
    public void addEdge(Object o1, Object o2) {        
        int id1 = _getNodeId(o1);
        int id2 = _getNodeId(o2);
        
        ((Vector)(_graph.elementAt(id1))).addElement(new Integer(id2));
	_numEdges++;
    }

    /** Returns all the edges in this graph in the form of a 2-D Object
     *  array. Each row of the array represents an edge, corresponding
     *  to a successful <code>addEdge</code> call, but the order of the
     *  rows is not necessarily the same as the calls. The array always
     *  has two columns, corresponding to the two arguments of the
     *  <code>addEdge</code> calls, in the order the arguments are
     *  listed.
     *  @return a 2-D Object array.
     */
    public Object[][] allEdges() {
	Object[][] result = new Object[_numEdges][2];

	int count = 0;
	for (int i = 0; i < numNodes(); i++) {
	    Object source = _getBackRef(i);
	    Vector sinkIds = (Vector)_graph.elementAt(i);
	    for (int j = 0; j < sinkIds.size(); j++) {
		int id = ((Integer)(sinkIds.elementAt(j))).intValue();
		Object sink = _getBackRef(id);
		result[count][0] = source;
		result[count++][1] = sink;
	    }
	}

	return result;
    }

    /** Returns all the nodes in this graph in the form of an Objects array.
     *  The Objects are the ones passed in successful <code>add()</code>
     *  calls.
     *  @return an Object array
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

    /** Returns a description of this graph.
     *  The form of the description is:<p>
     *  <pre>
     *  {class_name
     *    {node list_of_nodes_connected_to_that_node_by_addEdge()}
     *    {node list_of_nodes_connected_to_that_node_by_addEdge()}
     *    ...
     *  }
     *  </pre>
     *  where each node is described by the <code>toString()</code>
     *  method of the Object representing the node.
     *  @return a String description of this graph.
     */
    public String description() {
	String result = new String("{" + this.getClass().getName() + "\n");

        for (int i = 0; i < numNodes(); i++) {
            Object elem = _getBackRef(i);
            result = result.concat("  {" + elem.toString());

            Vector re = (Vector)(_graph.elementAt(i));
            for (Enumeration e = re.elements(); e.hasMoreElements(); ) {
                int i2 = ((Integer)e.nextElement()).intValue();
                result = result.concat(" " +
                        _getBackRef(i2).toString());
            }
            result = result.concat("}\n");
        }
	result = result.concat("}");
	return result;
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

    /** Tests if the specified Object is a node in this graph. The
     *  Object is a node if it is equal to an Object specified in
     *  a successful <code>add</code> call. The equality is determined
     *  by the <code>equals</code> method.
     *  @param o the Object to be tested.
     *  @return <code>true</code> if the specified Object is a node
     *   in this graph; <code>false</code> otherwise.
     */
    public boolean contains(Object o) {
        return _nodeIdTable.containsKey(o);
    }

    /** Tests if this graph is directed.
     *  @return alway returns <code>false</code> in this class.
     */
    public boolean isDirected() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Returns the node in this graph with the specified node ID.
     *  @param nodeId a node ID
     *  @return an Object representing a node.
     *  @exception IllegalArgumentException the node ID is negative
     *   or is not less than the total number of nodes in
     *   this graph.
     */
    protected Object _getBackRef(int nodeId) {
	try {
	    return _backRef.elementAt(nodeId);
	} catch (ArrayIndexOutOfBoundsException ex) {
	    throw new IllegalArgumentException("Graph._getBackRef: " +
		"node ID is negative or is not less than the total " +
		"number of nodes in this graph.");
	}
    }

    /** Returns the node ID of the specified node.
     *  @param o an Object representing a graph node.
     *  @return the node id.
     *  @exception IllegalArgumentException the specified Object is not
     *   a node in this graph.
     */
    protected int _getNodeId(Object o) {
        Integer v = (Integer)(_nodeIdTable.get(o));
        if (v == null) {
            throw new IllegalArgumentException("Graph._getNodeId: " +
		"the specified Object is not a node in this graph.");
        }
        
        return v.intValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The data structure storing the topology of this graph.
     *  This vector is indexed by the node IDs, each entry is a vector
     *  of <code>Integers</code> containing node IDs. Every successful
     *  call of <code>addEdge</code> adds the node ID of the second
     *  argument to the Vector at the entry of _graph indexed by
     *  the node ID of the first argument.
     */ 
    protected Vector _graph;
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // Tranlation from nodeId to node.
    // This vector is indexed by node ID. The entries are the Objects
    // representing the graph nodes with the corresponding node IDs.
    private Vector _backRef;
    
    // Translation from node to node ID. The keys of this Hashtable
    // are the Objects representing graph nodes, and the values are
    // the corresponding node IDs.  This translation can also be
    // done with _backRef.indexOf(), but Hashtable is faster.
    private Hashtable _nodeIdTable;

    // number of edges in this graph
    private int _numEdges = 0;
}

