/* A basic graph.

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
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// Graph
/**
A basic graph.
Each node in the graph is represented by an Object. To construct a graph,
use <code>add</code> to add nodes and <code>addEdge</code> to add
edges between nodes.
<p>
NOTE: This class is a starting point for implementing graph algorithms,
more methods will be added.

@author Yuhong Xiong, Jie Liu
@version $Id$
*/

public class Graph {

    /** Construct an empty graph.
     */
    public Graph() {
        _graph = new ArrayList();
        _nodeObjects = new ArrayList();
        _nodeIdTable = new HashMap();
    }

    /** Construct an empty graph with enough storage allocated for the
     *  specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param nodeCount the integer specifying the number of nodes
     */
    public Graph(int nodeCount) {
        _graph = new ArrayList(nodeCount);
        _nodeObjects = new ArrayList(nodeCount);
        _nodeIdTable = new HashMap(nodeCount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a node to this graph.  The node is represented by the
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
     *  @exception IllegalArgumentException If an Object equals to the
     *   specified one is already in this graph.
     *  @exception NullPointerException If the specified Object is
     *   <code>null</code>.
     */
    public void add(Object o) {
        if (contains(o)) {
            throw new IllegalArgumentException("Graph.add: Object is " +
                    "already in the graph.");
        }
        _nodeObjects.add(o);
        _graph.add(new ArrayList());
        _nodeIdTable.put(o, new Integer(_graph.size() - 1));
    }

    /** Add an edge between two nodes.  Multiple edges
     *  between the same nodes are allowed, and are considered different
     *  edges.  Self loop is also allowed.
     *
     *  @param o1 the Object representing a graph node
     *  @param o2 the Object representing a graph node
     *  @exception IllegalArgumentException If at least one of the
     *   arguments is not a graph node, i.e., the argument is not equal
     *   to an Object specified in a successful <code>add</code> call.
     *   Equality is determined by the <code>equals</code> method.
     */
    public void addEdge(Object o1, Object o2) {
        int id1 = _getNodeId(o1);
        int id2 = _getNodeId(o2);

        ArrayList sinkIds = (ArrayList)(_graph.get(id1));
	sinkIds.add(new Integer(id2));
	_edgeCount++;
    }

    /** Test if the specified Object is a node in this graph. The
     *  Object is a node if it is equal to an Object specified in
     *  a successful <code>add</code> call. Equality is determined
     *  by the <code>equals</code> method.
     *  @param o the Object to be tested.
     *  @return <code>true</code> if the specified Object is a node
     *   in this graph; <code>false</code> otherwise.
     */
    public boolean contains(Object o) {
        return _nodeIdTable.containsKey(o);
    }

    /** Return a description of this graph.
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

        for (int i = 0; i < getNodeCount(); i++) {
            Object elem = _getNodeObject(i);
            result = result.concat("  {" + elem.toString());

            ArrayList sinkIds = (ArrayList)(_graph.get(i));
	    for (int j = 0; j < sinkIds.size(); j++) {
                int i2 = ((Integer)sinkIds.get(j)).intValue();
                result = result.concat(" " +
                        _getNodeObject(i2).toString());
            }
            result = result.concat("}\n");
        }
	result = result.concat("}");
	return result;
    }

    /** Return the total number of edges in this graph.  Multiple
     *  connections between two nodes are counted multiple times.
     *  @return the total number of edges in this graph.
     */
    public int getEdgeCount() {
	return _edgeCount;
    }

    /** Return all the edges in this graph in the form of a 2-D Object
     *  array. Each row of the array represents an edge, corresponding
     *  to a successful <code>addEdge</code> call, but the order of the
     *  rows is not necessarily the same as the calls. The array always
     *  has two columns, corresponding to the two arguments of
     *  <code>addEdge</code>, in the order the arguments are
     *  listed.
     *  @return a 2-D Object array.
     */
    public Object[][] getEdges() {
	Object[][] result = new Object[_edgeCount][2];

	int count = 0;
	for (int i = 0; i < getNodeCount(); i++) {
	    Object source = _getNodeObject(i);
	    ArrayList sinkIds = (ArrayList)_graph.get(i);
	    for (int j = 0; j < sinkIds.size(); j++) {
		int id = ((Integer)(sinkIds.get(j))).intValue();
		Object sink = _getNodeObject(id);
		result[count][0] = source;
		result[count++][1] = sink;
	    }
	}

	return result;
    }

    /** Return the total number of nodes in this graph.
     *  @return the total number of nodes in this graph.
     */
    public int getNodeCount() {
        return _nodeObjects.size();
    }

    /** Return all the nodes in this graph in the form of an Objects array.
     *  The Objects are the ones passed in successful <code>add</code>
     *  calls.
     *  @return an Object array
     */
    public Object[] getNodes() {
        return _nodeObjects.toArray();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the node ID of the specified node.
     *  @param o an Object representing a graph node.
     *  @return the node id.
     *  @exception IllegalArgumentException If the specified Object is
     *   not a node in this graph.
     */
    protected final int _getNodeId(Object o) {
        Integer v = (Integer)(_nodeIdTable.get(o));
        if (v == null) {
            throw new IllegalArgumentException("Graph._getNodeId: " +
                    "the object \"" + o.toString() +
                    "\" is not a node in this graph.");
        }

        return v.intValue();
    }

    /** Return the node in this graph with the specified node ID.
     *  @param nodeId a node ID
     *  @return an Object representing a node.
     *  @exception IllegalArgumentException If the node ID is
     *   negative or is not less than the total number of nodes in
     *   this graph.
     */
    protected final Object _getNodeObject(int nodeId) {
	try {
	    return _nodeObjects.get(nodeId);
	} catch (ArrayIndexOutOfBoundsException ex) {
	    throw new IllegalArgumentException("Graph._getNodeObject: " +
                    "node ID is negative or is not less than the total " +
                    "number of nodes in this graph.");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The data structure storing the topology of this graph.
     *  This ArrayList is indexed by the node IDs, each entry is as
     *  ArrayList of <code>Integers</code> containing node IDs. Every
     *  successful call of <code>addEdge</code> adds the node ID of
     *  the second argument to the ArrayList at the entry of _graph
     *  indexed by the node ID of the first argument.
     */
    protected ArrayList _graph;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // number of edges in this graph
    private int _edgeCount = 0;

    // Translation from node to node ID. The keys of this HashMap
    // are the Objects representing graph nodes, and the values are
    // the corresponding node IDs.  This translation can also be
    // done with _nodeObjects.indexOf(), but HashMap is faster.
    private HashMap _nodeIdTable;

    // Translation from node Id to node.
    // This ArrayList is indexed by node ID. The entries are the Objects
    // representing the graph nodes with the corresponding node IDs.
    private ArrayList _nodeObjects;
}
