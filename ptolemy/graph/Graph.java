/* A basic weighted graph.

 Copyright (c) 1997-2001 The Regents of the University of California.
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
A basic weighted graph.
Each node has a unique Object, called the <em>node weight</em>, associated
with it.  To construct a graph,
use <code>add</code> to add nodes and <code>addEdge</code> to add
edges between nodes.
<p>
Distinct nodes must have distinct weights, as determined
by the <code>equals</code> method. After a node has been inserted into a
weighted graph, its weight may be modified as long as the
modification does not affect comparison of the weight using the
<code>equals</code> method of the weight's class. Violation of this convention
may lead to unpredictable results. 
<p>
Edges also have arbitrary Objects associated
with them, and these are referred to as <em>edge weights</em>. 
At present, edge weights need not be unique.
<p>
NOTE: This class is a starting point for implementing graph algorithms,
more methods will be added.
<p> 
Both directed and undirected graphs can be implemented using this class.
In directed graphs, the order of nodes specified to <code>addEdge</code> is relevant,
whereas in undirected graphs, the order is unimportant. Support for both
undirected and directed graphs follows from the combined support for
these in the underlying {@link Node} and {@link Edge} classes.
<p>
If a class <code>XXXGraph</code> extends {@link Graph}, and another class
<code>YYYGraph</code> extends <code>XXXGraph</code>, then the node (edge)
weight type used in <code>YYYGraph</code> must be the same as or must extend
the node (edge) weight type used in <code>XXXGraph</code>. The node weight
type used in {@link Graph} is Object, and the edge weight type used in
{@link Graph} is also Object. These are the types of the objects that maintain
the weights of the {@link Node}s and {@link Edge}s, respectively.  

@author Shuvra S. Bhattacharyya, Yuhong Xiong, Jie Liu
@version $Id$
@see ptolemy.synthesis.Edge
@see ptolemy.synthesis.Node
*/

public class Graph {

    /** Construct an empty graph.
     */
    public Graph() {
        _nodes = new ArrayList();
        _edges = new ArrayList();
        _nodeIdTable= new HashMap();
        _nodeTable= new HashMap();
        _recomputeIdentifiers = true;
    }

    /** Construct an empty graph with enough storage allocated for the
     *  specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param nodeCount the number of nodes.
     */
    public Graph(int nodeCount) {
        _nodes = new ArrayList(nodeCount);
        _edges = new ArrayList();
        _nodeIdTable= new HashMap(nodeCount);
        _nodeTable= new HashMap(nodeCount);
        _recomputeIdentifiers = true;
    }

    /** Construct an empty graph with enough storage allocated for the
     *  specified number of edges, and number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes and edges is
     *  known.
     *  @param nodeCount the number of nodes.
     *  @param edgeCount the number of edges.
     */
    public Graph(int nodeCount, int edgeCount) {
        _nodes = new ArrayList(nodeCount);
        _edges = new ArrayList(edgeCount);
        _nodeIdTable= new HashMap(nodeCount);
        _nodeTable= new HashMap(nodeCount);
        _recomputeIdentifiers = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a node to this graph.  The node is represented by (has as its weight) 
     *  the specified Object. The Object cannot be <code>null</code>.
     *  In addition, two Objects equal to each other, as determined
     *  by the <code>equals</code> method, cannot both be added.<p>
     *
     *  After nodes are added to a graph, the node weights should not
     *  be changed in a manner that affects comparison against other
     *  possible node weights by the <code>equals</code>
     *  method. Doing so may generate unexpected results.
     *
     *  @param o the Object ("node weight") representing a graph node
     *  @exception IllegalArgumentException if a
     *  node weight equal to the specified one is already associated with a
     *  node in this graph.
     *  @exception NullPointerException if the specified node weight is
     *  <code>null</code>.
     */
    public void add(Object o) {
        if (contains(o)) {
            throw new IllegalArgumentException("Graph.add: Object is " +
                    "already in the graph.");
        }
        Node node = new Node(o); 
        _nodes.add(node);

        // Add the node to the node table with key equal to the node weight.
        _nodeTable.put(o, node);   

        _recomputeIdentifiers = true;
    }

    /** Add a collection of nodes to the graph. Each object in the collection
     *  is taken to be a weight of a new node that is added to the graph.
     *  None of the objects in the collection can be null (however, it
     *  is fine to pass an empty collection). Effectively,
     *  each object in the collection is operated on by the
     *  {@link #add(Object)} method.
     *  @param weights the collection of Objects representing nodes
     *  to be added to the graph.
     */
    public void addAll(Collection weightCollection) {
        Iterator weights = weightCollection.iterator();
        while (weights.hasNext()) add(weights.next());
    }

    /** Add an edge between two nodes.  If the edge is subsequently
     *  operated on as a directed edge, its orientation will be
     *  taken to be directed <em>from</em> the first (<em>o1</em>) node 
     *  <em>to</em> the second
     *  (<em>o2</em>) node. Multiple edges
     *  between the same nodes are allowed, and are considered different
     *  edges.  Self loops are also allowed.
     *
     *  @param o1 an Object (node weight) representing a graph node.
     *  @param o2 an Object (node weight) representing a graph node.
     *  @exception IllegalArgumentException if at least one of the
     *   arguments is not a node weight, i.e., the argument is not equal
     *   to an Object specified in a successful <code>add</code> call.
     *   Equality is determined by the <code>equals</code> method.
     */
    public void addEdge(Object o1, Object o2) {
        Edge edge = new Edge(getNode(o1), getNode(o2), new Object());
        _edges.add(edge);
    }
    
    /** Add a weighted edge between two nodes.  An arbitrary Object can be
     *  assigned as the edge weight. If the edge is subsequently
     *  operated on as a directed edge, its orientation will be
     *  taken to be directed <em>from</em> the first (<em>o1</em>) node 
     *  <em>to</em> the second
     *  (<em>o2</em>) node. Multiple edges
     *  between the same nodes are allowed, and are considered different
     *  edges.  Self loops are also allowed.
     *
     *  @param o1 an Object (node weight) representing a graph node
     *  @param o2 an Object (node weight) representing a graph node
     *  @param weight the Object (edge weight) representing the edge.
     *  @exception IllegalArgumentException if at least one of the
     *   arguments is not a node weight, i.e., the argument is not equal
     *   to an Object specified in a successful <code>add</code> call.
     *   Equality is determined by the <code>equals</code> method.
     */
    public void addEdge(Object o1, Object o2, Object weight) {
        Edge edge = new Edge(getNode(o1), getNode(o2), weight);
        _edges.add(edge);
    }

    /** Test if the specified Object is a node weight in this graph. The
     *  Object is a node weight if it is equal to an Object specified in
     *  a successful <code>add</code> call. Equality is determined
     *  by the <code>equals</code> method.
     *  @param o the Object (node weight) to be tested.
     *  @return <code>true</code> if the specified Object is a node weight
     *   in this graph; <code>false</code> otherwise.
     */
    public boolean contains(Object o) {
        return _nodeTable.containsKey(o);
    }

    /** Return a description of this graph. The form of the description is:<p>
     *  <pre>
     *  {class_name
     *    {node list_of_nodes_connected_to_that_node_by_addEdge()}
     *    {node list_of_nodes_connected_to_that_node_by_addEdge()}
     *    ...
     *  }
     *  </pre>
     *  where each node is described by the <code>toString()</code>
     *  method of the Object (node weight) representing the node.
     *  @return a String description of this graph.
     */
    public String description() {
	    StringBuffer result = new StringBuffer("{" + this.getClass().getName() + "\n");
        Iterator nodes = _nodes.iterator();
        while (nodes.hasNext()) {
            Node node = (Node)(nodes.next());
            result.append("  {" + node.weight().toString());
            int outputEdgeCount = 0;
            Iterator outputEdges = node.outputEdges();
            while (outputEdges.hasNext()) {
                Edge edge = (Edge)(outputEdges.next());
                result.append(" " + edge.sink().weight().toString());
            }
            result.append("}\n");
        }
	    result.append("}");
	    return result.toString();
    }

    /** Return the total number of edges in this graph.  Multiple
     *  connections between two nodes are counted multiple times.
     *  @return the total number of edges in this graph.
     */
    public int getEdgeCount() {
	    return _edges.size();
    }

    /** Return all the edges in this graph in the form of an Iterator.
     *  Each element in the returned Iterator is an {@link Edge}.
     *  @return an Iterator over the edges in the graph.
     */
    public Iterator getEdgeIterator() {
        return _edges.iterator();
    }
     
    /** Return all the edges in this graph in the form of a 2-D Object
     *  array. Each row of the array represents an edge, corresponding
     *  to a successful <code>addEdge</code> call, but the order of the
     *  rows is not necessarily the same as the calls. The array always
     *  has two columns, corresponding to the two arguments of
     *  <code>addEdge</code>, in the order the arguments are
     *  listed. These columns contain the node weights of the nodes that
     *  are connected by the edges. Specifically, each [<em>i</em>][0] entry
     *  of this array gives the source node weight of the <em>i</em>th 
     *  edge, and each [<em>i</em>][1] entry gives the 
     *  sink node weight of the <em>i</em>th edge.
     *  @return the edges in the graph.
     */
    public Object[][] getEdges() {
	    Object[][] result = new Object[_edges.size()][2];
        int count = 0;
        Iterator nodes = _nodes.iterator();
        while (nodes.hasNext()) {
            Node node = (Node)(nodes.next());
	        Object sourceWeight = node.weight();
            Iterator outputEdges = node.outputEdges();
            while (outputEdges.hasNext()) {
                Edge edge = (Edge)(outputEdges.next());
		        Object sinkWeight = edge.sink().weight();
		        result[count][0] = sourceWeight;
		        result[count++][1] = sinkWeight;
	        }
	    }
	    return result;
    }

    /** Return the node that has a specified weight.
     *  @param o an Object representing the weight of a graph node.
     *  @return the node.
     *  @exception IllegalArgumentException if the specified Object is
     *   not a node weight in this graph (that is, there is no node in the graph
     *   whose weight is equal to the specified Object, as determined by
     *   the <code>equals()</code> method).
     */
    public final Node getNode(Object o) {
        Object associatedObject = _nodeTable.get(o);
        if (associatedObject == null) 
            throw new IllegalArgumentException("Graph.getNode(): " +
                    "the weight \"" + o.toString() +
                    "\" is not associated with a node in this graph.");
        else if (!(associatedObject instanceof Node))
            throw new IllegalArgumentException("Graph.getNode: " +
                    "the weight \"" + o.toString() +
                    "\" maps to an object of type " + 
                    associatedObject.getClass().getName() + 
                    "\n(it should map to a Node)");
        else return (Node)associatedObject;
    }

    /** Return the total number of nodes in this graph.
     *  @return the total number of nodes in this graph.
     */
    public int getNodeCount() {
        return _nodes.size();
    }

    /** Return all the nodes in this graph in the form of an Iterator.
     *  Each element in the returned Iterator is a {@link Node}.
     *  @return an Iterator over the nodes in the graph.
     */
    public Iterator getNodeIterator() {
        return _nodes.iterator();
    }
     
    /** Return all the node weights in this graph in the form of an Objects array.
     *  The Objects are the ones passed in successful <code>add</code>
     *  calls.
     *  @return the node weights in the graph.
     */
    public Object[] getNodes() {
        int count = 0;
        Object[] nodeWeights = new Object[_nodes.size()];
        Iterator nodes = _nodes.iterator();
        while (nodes.hasNext()) {
            nodeWeights[count++] = ((Node)(nodes.next())).weight(); 
        }
        return nodeWeights;
    }

    /** Return the sink nodes of this graph. A node is a sink node if
     *  it contains no output edges. Thus, this method makes sense only
     *  for directed graphs. The sink nodes are returned as an array
     *  whose size (number of elements) is equal to the number of sink nodes.
     *  Each element of the array is a {@link Node}. A zero-element array is returned
     *  if there are no sink nodes.
     *  @return the sink nodes of the graph.
     */
    public Node[] getSinkNodes() {
       // FIXME: move this to DirectedGraph
       LinkedList sinkList = new LinkedList();
       Iterator nodes = getNodeIterator();
       while (nodes.hasNext()) {
           Node node = (Node)(nodes.next());
           if (node.outputEdgeCount() == 0) sinkList.add(node);
       }
       Node[] sinks = new Node[sinkList.size()];
       return (Node[]) (sinkList.toArray(sinks)); 
    }

    /** Return the source nodes of this graph. A node is a source node if
     *  it contains no input edges. Thus, this method makes sense only
     *  for directed graphs. The source nodes are returned as an array
     *  whose size (number of elements) is equal to the number of source nodes.
     *  Each element of the array is a {@link Node}. A zero-element array is returned
     *  if there are no source nodes.
     *  @return the source nodes of the graph.
     */
    public Node[] getSourceNodes() {
       // FIXME: move this to DirectedGraph
       LinkedList sourceList = new LinkedList();
       Iterator nodes = getNodeIterator();
       while (nodes.hasNext()) {
           Node node = (Node)(nodes.next());
           if (node.inputEdgeCount() == 0) sourceList.add(node);
       }
       Node[] sources = new Node[sourceList.size()];
       return (Node[]) (sourceList.toArray(sources)); 
    }

    /** Remove an edge from the graph.
     * @param removeMe the edge to be removed.
     */
    public void removeEdge(Edge removeMe) {
        if (_remove(_edges, removeMe) == null) {
            throw new RuntimeException("Attempt to remove and edge that is not "
                    + "contained in the graph.\n"
                    + "A dump of the edge and graph follow.\n"
                    + "The edge:\n" + removeMe + "\nThe graph:\n" + this.description()
                    + "\n");
        }
        removeMe.source().removeEdge(removeMe);
        removeMe.sink().removeEdge(removeMe);
    }

    /** Remove a node from the graph.
     * All edges incident to the node are also removed.
     * @param removeMe the node to be removed.
     */
    public void removeNode(Node removeMe) {
        if (_remove(_nodes, removeMe) == null) {
                throw new RuntimeException("Attempt to remove a node that is not "
                        + "contained in the graph.\n"
                        + "A dump of the node and graph follow.\n"
                        + "The node:\n" + removeMe + "\nThe graph:\n" + this.description()
                        + "\n");
        }
        _nodeTable.remove(removeMe.weight());
        Iterator incidentEdges = removeMe.incidentEdges();
        while (incidentEdges.hasNext()) {
            Edge edge = (Edge)(incidentEdges.next());
            if (edge.source() != removeMe) edge.source().removeEdge(edge);
            if (edge.sink() != removeMe) edge.sink().removeEdge(edge);
            if (_remove(_edges, edge) == null) {
                throw new RuntimeException("We have encountered an edge that is "
                        + "incident to a node, but is not contained in the graph's "
                        + "edge list.\nA dump of the node and edge follow.\n"
                        + "The node:\n" + removeMe + "\nThe edge:\n" + edge + "\n");
            }
        }
        _recomputeIdentifiers = true;
    } 
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a node in the graph given the node identifer.
     *  The node set of the graph should not have changed since the
     *  last invocation of {@link #_enumerateNodes()} (otherwise, the
     *  node identifier might not be valid).
     *  @param node the node identifier.
     *  @return the node.
     *  @exception IllegalArgumentException if the node is not contained
     *  in this graph.
     */
    protected final Node _getNode(int identifier) {
       _enumerateNodes();
        return (Node)(_nodes.get(identifier));
    }

    /** Return the node identifier of the specified node. See 
     *  {@link #_enumerateNodes()}.
     *  @param o an Object (node weight) representing a graph node.
     *  @return the node identifier.
     *  @exception IllegalArgumentException If the specified Object is
     *   not a node weight in this graph.
     */
    protected final int _getNodeId(Object o) {
       _enumerateNodes();
       Integer v = (Integer)(_nodeIdTable.get(getNode(o)));
       if (v == null) {
           throw new IllegalArgumentException("Graph._getNodeId: " +
                   "the object \"" + o.toString() +
                   "\" is not a node weight in this graph, or a node " +
                   "identifier has not yet been determined for it.\n");
       }
 
       return v.intValue();
    } 

    /** Return the weight of a given node in the graph.
     *  @param node the graph node.
     *  @return the weight of the node.
     *  @exception IllegalArgumentException if the node is not contained
     *  in this graph.
     */
    protected final Object _getNodeObject(Node node) {
        if (!_nodes.contains(node)) 
	        throw new IllegalArgumentException("Graph._getNodeObject: \"" +
                    node + "\" is not a node in this graph");
        else return node.weight();
    }

    /** Return the weight of a given node in the graph given the node identifer.
     *  The node set of the graph should not have changed since the
     *  last invocation of {@link #_enumerateNodes()} (otherwise, the
     *  node identifier might not be valid).
     *  @param node the node identifier.
     *  @return the weight of the node.
     */
    protected final Object _getNodeObject(int identifier) {
        _enumerateNodes();
        return ((Node)(_nodes.get(identifier))).weight();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    //  Assign to each node a unique integer identifier between 
    //  0 and <em>N</em>-1, where <em>N</em> is 
    //  the number of nodes currently in the graph. The identifier
    //  assigned to a node can be accessed by using the 
    //  {@link #getNodeId(Object)} method. In general, if the set of
    //  graph nodes is changed, then the {@link #enumerateNodes()}
    //  method must be re-invoked before node identifiers are accessed. 
    //  The identifiers are recomputed in this method only if the
    //  node set has changed since the last invocation of the method.
    //
    private void _enumerateNodes() {
        if (_recomputeIdentifiers) {
            _nodeIdTable.clear();
            int i;
            for (i=0; i<_nodes.size(); i++) {
                _nodeIdTable.put(_nodes.get(i), new Integer(i));
            }
           _recomputeIdentifiers = false;
        }
    }

    // Remove an object from an ArrayList if it exists in the list.
    // Return null if the object is not in the list to begin with, or
    // return the object that was removed.
    private Object _remove(ArrayList list, Object element) {
        int index;
        if ((index = list.indexOf(element)) == -1) return null;
        else return list.remove(index);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The data structure storing the edges of this graph.
     *  Each entry in this ArrayList is an {@link Edge}.
     */
    private ArrayList _edges;

    /** The data structure storing the nodes of this graph.
     *  Each entry in this ArrayList is a {@link Node}.
     */
    private ArrayList _nodes;

    // Translation from node to node ID. The keys of this HashMap
    // are the Objects representing graph nodes, and the values are
    // the corresponding node IDs.  This translation can also be
    // done with _nodes.indexOf(), but HashMap is faster.
    private HashMap _nodeIdTable;

    // Translation from node weight to node. The keys of this HashMap
    // are the unique weights (of type Object) associated with the graph nodes, 
    // and the values are
    // the corresponding node objects (of type {@link Node}).  
    private HashMap _nodeTable;

    // A flag that indicates whether or not node identifiers need to
    // be recomputed before they are next used.
    private boolean _recomputeIdentifiers;
}
