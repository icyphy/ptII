/* A graph with optionally-weighted nodes and edges.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.graph;

import ptolemy.graph.analysis.Analysis;
import ptolemy.graph.analysis.SelfLoopAnalysis;

import java.lang.reflect.Method;
import java.text.CharacterIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Graph
/**
A graph with optionally-weighted nodes and edges.

<p>Each node or edge may have a weight associated with it
(see {@link Edge} and {@link Node}).
The nodes (edges) in a graph are always distinct, but their weights
need not be.

<p>Each node (edge) has a unique, integer label associated with it.
These labels can be used, for example, to index arrays and matrixes
whose rows/columns correspond to nodes (edges). See {@link #nodeLabel(Node)}
({@link #edgeLabel(Edge)}) for details.

<p>Both directed and undirected graphs can be implemented using this
class. In directed graphs, the order of nodes specified to the
<code>addEdge</code> method is relevant, whereas in undirected graphs, the
order is unimportant. Support for both undirected and directed graphs
follows from the combined support for these in the underlying {@link
Node} and {@link Edge} classes. For more thorough support for directed
graphs, see {@link DirectedGraph}.

<p>The same node can exist in multiple graphs, but any given graph can contain
only one instance of the node. Node labels, however, are local to individual
graphs. Thus, the same node may have different labels in different graphs.
Furthermore, the label assigned in a given graph to a node may change over time
(if the set of nodes in the graph changes). If a node is contained in
multiple graphs, it has the same weight in all of the graphs.
All of this holds for edges
as well. The same weight may be shared among multiple nodes and edges.

<p> Multiple edges in a graph can connect the same pair of nodes.
Thus, multigraphs are supported.

<p>Once assigned, node and edge weights should not be changed in ways that
affect comparison under the <code>equals</code> method.
Otherwise, unpredictable behavior may result.

<p>In discussions of complexity, <em>n</em> and <em>e</em> refers to the number
of graph nodes and edges, respectively.

@author Shuvra S. Bhattacharyya, Ming-Yung Ko,
Fuat Keceli, Shahrooz Shahparnia, Yuhong Xiong, Jie Liu.
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.graph.Edge
@see ptolemy.graph.Node
*/
public class Graph implements Cloneable {

    /** Construct an empty graph.
     */
    public Graph() {
        _nodes = new LabeledList();
        _edges = new LabeledList();
        _initializeAnalyses();
        _edgeWeightMap = new HashMap();
        _hiddenEdgeSet = new HashSet();
        _incidentEdgeMap = new HashMap();
        _nodeWeightMap = new HashMap();
    }

    /** Construct an empty graph with enough storage allocated for the
     *  specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param nodeCount The number of nodes.
     */
    public Graph(int nodeCount) {
        _nodes = new LabeledList(nodeCount);
        _edges = new LabeledList();
        _initializeAnalyses();
        _edgeWeightMap = new HashMap();
        _hiddenEdgeSet = new HashSet();
        _incidentEdgeMap = new HashMap(nodeCount);
        _nodeWeightMap = new HashMap(nodeCount);
    }

    /** Construct an empty graph with enough storage allocated for the
     *  specified number of edges, and number of nodes.  Memory
     *  management is more efficient with this constructor if the
     *  number of nodes and edges is known.
     *  @param nodeCount The number of nodes.
     *  @param edgeCount The number of edges.
     */
    public Graph(int nodeCount, int edgeCount) {
        _nodes = new LabeledList(nodeCount);
        _edges = new LabeledList(edgeCount);
        _initializeAnalyses();
        _edgeWeightMap = new HashMap(edgeCount);
        _hiddenEdgeSet = new HashSet();
        _incidentEdgeMap = new HashMap(nodeCount);
        _nodeWeightMap = new HashMap(nodeCount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an analysis to the list of analyses that this graph is associated
     *  with.
     *  @param analysis The analysis.
     *  @exception IllegalArgumentException If the graph associated with the
     *  analysis is not equal to this graph, or if the graph already contains
     *  the analysis in its list of analyses.
     */
    public void addAnalysis(Analysis analysis) {
        if (analysis.graph() != this) {
            throw new IllegalArgumentException("Invalid associated graph.\n" +
                    "The analysis:\n" + analysis + "\n");
        }
        if (_analysisList.contains(analysis)) {
            throw new IllegalArgumentException("Attempt to add " +
                    "duplicate analysis.\nThe analysis:\n" + analysis);
        }
        _analysisList.add(analysis);
    }

    /** Add a weighted edge between two nodes.  If the edge is subsequently
     *  operated on as a directed edge, its orientation will be taken
     *  to be directed <i>from</i> the first (<code>node1</code>) node
     *  <i>to</i> the second (<code>node2</code>) node. Multiple edges
     *  between the same nodes are allowed, and are considered
     *  different edges.  Self-loops are also allowed.
     *
     *  @param node1 The first node.
     *  @param node2 The second node.
     *  @param weight The weight.
     *  @return The edge.
     *  @exception IllegalArgumentException If the first node or second
     *  node is not already in the graph, or if the weight is
     *  <code>null</code>.
     */
    public Edge addEdge(Node node1, Node node2, Object weight) {
        return _addEdge(node1, node2, true, weight);
    }

    /** Add an unweighted edge between two nodes. Operation is the same as in
     *  {@link #addEdge(Node, Node, Object)}, except that no
     *  weight is assigned to the edge.
     *
     *  @param node1 The first node.
     *  @param node2 The second node.
     *  @return The edge.
     *  @exception IllegalArgumentException If the first node or second
     *  node is not already in the graph.
     */
    public Edge addEdge(Node node1, Node node2) {
        return _addEdge(node1, node2, false, null);
    }

    /** Given two node weights <i>w1</i> and <i>w2</i>, add weighted
     *  edges of the form (<i>x1</i>, <i>x2</i>), where
     *  <code>(x1.getWeight() == w1) && (x2.getWeight() == w2)</code>.
     *
     *  @param weight1 The first node weight.
     *  @param weight2 The second node weight.
     *  @param newEdgeWeight The weight to assign to each new edge.
     *  @return The set of edges that were added; each element
     *  of this set is an instance of {@link Edge}.
     *  @exception IllegalArgumentException If no edge is
     *  added (i.e., if no nodes x1, x2 satisfy the above condition).
     */
    public Collection addEdge(Object weight1, Object weight2,
            Object newEdgeWeight) {
        return _addEdges(weight1, weight2, true, newEdgeWeight);
    }

    /** Given two node weights <i>w1</i> and <i>w2</i>, add all unweighted
     *  edges of the form (<i>x1</i>, <i>x2</i>), where
     *  <code>(x1.getWeight() == w1) && (x2.getWeight() == w2)</code>.
     *
     *  @param weight1 The first node weight.
     *  @param weight2 The second node weight.
     *  @return The set of edges that were added; each element
     *  of this set is an instance of {@link Edge}.
     *  @exception IllegalArgumentException If no edge is
     *  added (i.e., if no nodes x1, x2 satisfy the above condition).
     */
    public Collection addEdge(Object weight1, Object weight2) {
        return _addEdges(weight1, weight2, false, null);
    }

    /** Add a pre-constructed edge (unweighted or weighted).
     *
     *  @param edge The edge.
     *  @exception IllegalArgumentException If the source or sink node
     *  of the edge is not already in the graph, if the edge is
     *  already in the graph, or if the edge is hidden in the graph.
     *  @see #hideEdge(Edge).
     */
    public Edge addEdge(Edge edge) {
        if (!containsNode(edge.source())) {
            throw new IllegalArgumentException("The source node "
                    + "is not in the graph." + _edgeDump(edge));
        } else if (!containsNode(edge.sink())) {
            throw new IllegalArgumentException("The sink node "
                    + "is not in the graph." + _edgeDump(edge));
        } else if (containsEdge(edge)) {
            throw new IllegalArgumentException("Attempt to add an edge that "
                    + "is already in the graph." + _edgeDump(edge));
        } else if (hidden(edge)) {
            throw new IllegalArgumentException("Attempt to add an edge that "
                    + "is already hidden in the graph." + _edgeDump(edge));
        } else {
            _registerEdge(edge);
            return edge;
        }
    }

    /** Add a collection of edges to the graph. Each element in the
     *  argument collection must be a unique {@link Edge}.
     *  @param edgeCollection The collection of edges to add.
     */
    public void addEdges(Collection edgeCollection) {
        Iterator edges = edgeCollection.iterator();
        while (edges.hasNext()) {
            addEdge((Edge)(edges.next()));
        }
    }

    /** Add an unweighted node to this graph.
     *  @return The node.
     */
    public Node addNode() {
        Node node = new Node();
        _registerNode(node);
        return node;
    }

    /** Add a pre-constructed node (unweighted or weighted).
     *
     *  @param node The node.
     *  @exception IllegalArgumentException If the node is already in the graph.
     */
    public Node addNode(Node node) {
        if (containsNode(node)) {
            throw new IllegalArgumentException("Attempt to add a node "
                    + "that is already contained in the graph."
                    + _nodeDump(node));
        } else {
            _registerNode(node);
            return node;
        }
    }

    /** Add a new weighted node to this graph given the node weight.
     *
     *  @param weight The node weight.
     *  @return The node.
     *  @exception IllegalArgumentException If the specified weight is null.
     */
    public Node addNodeWeight(Object weight) {
        Node node = new Node(weight);
        _registerNode(node);
        return node;
    }

    /** Add a collection of nodes to the graph.
     *  Each element of the collection is interpreted
     *  as a weight of a new node to add in the graph.
     *  @param weightCollection The collection of node weights; each element
     *  is an instance of {@link Object}.
     *  @return The set of nodes that that were added; each element
     *  is an instance of {@link Node}.
     */
    public Collection addNodeWeights(Collection weightCollection) {
        ArrayList nodes = new ArrayList();
        Iterator weights = weightCollection.iterator();
        while (weights.hasNext()) {
            nodes.add(addNodeWeight(weights.next()));
        }
        return nodes;
    }

    /** Add a collection of nodes to the graph. Each element in the
     *  argument collection must be a unique {@link Node}.
     *  @param nodeCollection The collection of nodes to add.
     */
    public void addNodes(Collection nodeCollection) {
        Iterator nodes = nodeCollection.iterator();
        while (nodes.hasNext()) {
            addNode((Node)(nodes.next()));
        }
    }

    /** Return the present value of a counter that keeps track
     *  of changes to the graph.
     *  This counter is monitored by {@link Analysis}s to determine
     *  if associated computations are obsolete. Upon overflow, the counter
     *  resets to zero, broadcasts a change to all graph analyses, and
     *  begins counting again.
     *  @return The present value of the counter.
     */
    public long changeCount() {
        return _changeCount;
    }

    /** Return a clone of this graph. The clone has the same set of
     *  nodes and edges. Changes to the node or edge weights
     *  affect the clone simultaneously. However,
     *  modifications to the graph topology make the clone different from
     *  this graph (e.g., they are no longer equal (see
     *  {@link #equals(Object)})).
     *
     *  @return The clone graph.
     */
    public Object clone() {
        return cloneAs(this);
    }

    /** Return a clone of this graph in the form of the argument graph type
     *  (i.e., the run-time type of the returned graph is that of the
     *  argument graph). The clone has the
     *  same set of nodes and edges. Changes to the node or edge weights
     *  affect the
     *  clone simultaneously. If the run-time type of the argument graph is
     *  equal to that of this graph, then the clone is equal to this
     *  graph, as determined by {@link #equals(Object)}. However,
     *  modifications to the graph topology
     *  make the clone not equal to this graph.
     *
     *  @param graph The graph that gives the run-time type of the clone.
     *  @return A clone of this graph.
     */
    public Graph cloneAs(Graph graph) {
        Graph cloneGraph = graph._emptyGraph();
        // copy nodes and edges
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            cloneGraph.addNode((Node)nodes.next());
        }
        Iterator edges = edges().iterator();
        while (edges.hasNext()) {
            cloneGraph.addEdge((Edge)edges.next());
        }
        return cloneGraph;
    }

    /** Return the connected components of the graph. The connected
     *  components are returned as a Collection, where each element
     *  of the Collection is a Collection of Nodes.
     *  @return The connected components.
     */
    public Collection connectedComponents() {
        // We divide the set of nodes into disjoint subsets called 'components'.
        // These components are repeatedly modified until they coincide with
        // the connected components. The following HashMap is a map from
        // nodes into the components that contain them. Each element in the map
        // is a Node whose weight is an ArrayList of Nodes. We encapsulate
        // each ArrayList as the weight of a Node (called the 'container' of
        // the ArrayList) so that we can modify the ArrayList without
        // interfering with the hashing semantics of the HashMap.
        HashMap componentMap = new HashMap(nodeCount());
        HashSet components = new HashSet(nodeCount());
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            Node node = (Node)nodes.next();
            ArrayList component = new ArrayList();
            component.add(node);
            Node container = new Node(component);
            componentMap.put(node, container);
            components.add(container);
        }
        Iterator edges = edges().iterator();
        while (edges.hasNext()) {
            Edge edge = (Edge)edges.next();
            Node sourceContainer = (Node)(componentMap.get(edge.source()));
            Node sinkContainer = (Node)(componentMap.get(edge.sink()));
            ArrayList sourceSet = (ArrayList)(sourceContainer.getWeight());
            ArrayList sinkSet = (ArrayList)(sinkContainer.getWeight());
            if (sourceSet != sinkSet) {
                // Construct the union of the two components in the source set.
                components.remove(sinkContainer);
                Iterator moveNodes = sinkSet.iterator();
                while (moveNodes.hasNext()) {
                    Node moveNode = (Node)moveNodes.next();
                    componentMap.put(moveNode, sourceContainer);
                    sourceSet.add(moveNode);
                }
            }
        }

        // Before returning the result, do away with the container that
        // encapsulates each connected component.
        ArrayList result = new ArrayList(components.size());
        Iterator connectedComponents = components.iterator();
        while (connectedComponents.hasNext()) {
            result.add(((Node)connectedComponents.next()).getWeight());
        }
        return result;
    }

    /** Return true if the specified edge exists in the graph, and the
     *  edge is not hidden in the graph.
     *  @param edge The specified edge.
     *  @return True if the specified edge exists in the graph and is not
     *  hidden.
     *  @see #hidden(Edge).
     *  @see #hideEdge(Edge).
     */
    public boolean containsEdge(Edge edge) {
        return _edges.contains(edge) && (!hidden(edge));
    }

    /** Test if the specified object is an edge weight in this
     *  graph. Equality is
     *  determined by the <code>equals</code> method. If the specified
     *  edge weight is null, return false.
     *
     *  @param weight The edge weight to be tested.
     *  @return True if the specified object is an edge weight in this graph.
     */
    public boolean containsEdgeWeight(Object weight) {
        return _edgeWeightMap.containsKey(weight);
    }

    /** Return True if the specified node exists in the
     *  graph.
     *  @param node The specified node.
     *  @return True if the specified node exists in the
     *  graph.
     */
    public boolean containsNode(Node node) {
        return _nodes.contains(node);
    }

    /** Test if the specified object is a node weight in this
     *  graph. Equality is
     *  determined by the <code>equals</code> method. If the specified
     *  weight is null, return false.
     *
     *  @param weight The node weight to be tested.
     *  @return True if the specified object is a node weight in this graph.
     */
    public boolean containsNodeWeight(Object weight) {
        return _nodeWeightMap.containsKey(weight);
    }

    /** Return a description of this graph.
     *  The form of the description is:<p>
     *  <pre>
     *  {class_name
     *    {node0 sinks(node0)}
     *    {node1 sinks(node1)}
     *    ...
     *    {nodeN sinks(nodeN)}
     *  }
     *  </pre>
     *  where N is the number of nodes in the graph, nodeI denotes the node
     *  whose label is I, each node is described by its <code>toString()</code>
     *  method,
     *  and sinks(nodeI) denotes the set of nodes nodeK such that (nodeI, nodeK)
     *  is an edge in the graph (with each node again represented by its
     *  <code>toString()</code> method).
     *  @return A description of this graph.
     *  @deprecated Use toString().
     */
    public String description() {
        StringBuffer result = new StringBuffer("{"
                + this.getClass().getName() + "\n");
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            Node node = (Node)(nodes.next());
            result.append("  {" + node.toString());
            Iterator incidentEdges = incidentEdges(node).iterator();
            while (incidentEdges.hasNext()) {
                Edge edge = (Edge)(incidentEdges.next());
                if (edge.source() == node) {
                    result.append(" " + edge.sink().toString());
                }
            }
            result.append("}\n");
        }
        result.append("}");
        return result.toString();
    }

    /** Return an edge in this graph that has a specified weight. If multiple
     *  edges have the specified weight, then return one of them
     *  arbitrarily.
     *  @param weight The specified edge weight.
     *  @return An edge that has this weight.
     *  @exception NullPointerException If the specified weight
     *  is null.
     *  @exception IllegalArgumentException If the specified weight
     *  is not an edge weight in this graph.
     */
    public Edge edge(Object weight) {
        return (Edge)(((ArrayList)_sameWeightEdges(weight)).get(0));
    }

    /** Return an edge in this graph given the edge label.
     *  @param edge The edge label.
     *  @return The edge.
     *  @exception IllegalArgumentException If the label is not associated
     *  with an edge in this graph.
     *  @see #edgeLabel(Edge).
     */
    public Edge edge(int label) {
        return (Edge)(_edges.get(label));
    }

    /** Return the total number of edges in this graph.  Multiple
     *  connections between two nodes are counted multiple times.
     *  @return The total number of edges in this graph.
     */
    public int edgeCount() {
        return _edges.size();
    }

    /** Return the edge label of the specified edge.
     *  The edge label is a unique integer from 0 through
     *  <i>E</i>-1, where <i>E</i> is the number of edges
     *  currently in the graph. Edge labels maintain their
     *  consistency (remain constant) during periods when
     *  no edges are removed from the graph. When edges are removed,
     *  the labels assigned to the remaining edges may change.
     *
     *  @param edge A graph edge.
     *  @return The edge label.
     *  @exception IllegalArgumentException If the specified edge is not
     *  not an edge in this graph.
     */
    public int edgeLabel(Edge edge) throws IllegalArgumentException {
        return _edges.label(edge);
    }

    /** Return the edge label of the specified edge given the edge weight.
     *  If multiple edges have the specified weight, then return one of their
     *  labels arbitrarily.
     *
     *  @param weight The edge weight.
     *  @return The edge label.
     *  @exception IllegalArgumentException If the specified weight is not
     *  an edge weight in this graph.
     *  @see #edgeLabel(Edge).
     */
    public int edgeLabel(Object weight) throws IllegalArgumentException {
        return _edges.label(edge(weight));
    }

    /** Return the weight of a given edge in the graph given the edge label.
     *
     *  @param edge The edge label.
     *  @return The weight of the edge.
     *  @exception IndexOutOfBoundsException If the label is
     *  not valid.
     *  @exception IllegalArgumentException If the edge corresponding
     *  to the label is unweighted.
     *  @see #edgeLabel(Edge).
     */
    public Object edgeWeight(int label) {
        return ((Edge)(_edges.get(label))).getWeight();
    }

    /** Return all the edges in this graph in the form of a collection.
     *  Each element in the returned collection is an instance of {@link Edge}.
     *  Hidden edges are not included in the returned collection.
     *  The returned collection cannot be modified.
     *  This is an <em>O(1)</em> operation if there are no hidden edges;
     *  otherwise, it is an <em>O(e)</em> operation.
     *  @return All the edges in this graph.
     */
    public Collection edges() {
        int hiddenEdgeCount = _hiddenEdgeSet.size();
        if (hiddenEdgeCount == 0) {
            return Collections.unmodifiableList(_edges);
        }

        // There is at least one hidden edge.
        int visibleEdgeCount = _edges.size() - hiddenEdgeCount;
        ArrayList result = new ArrayList(visibleEdgeCount);
        if (visibleEdgeCount == 0) {
            return Collections.unmodifiableList(result);
        }

        // There is at least one edge to return.
        Iterator edges = _edges.iterator();
        while (edges.hasNext()) {
            Edge edge = (Edge)(edges.next());
            if (!hidden(edge)) {
                result.add(edge);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** Return all the edges in this graph that have a specified weight.
     *  The edges are returned in the form of a collection.
     *  Each element in the returned collection is an instance of {@link Edge}.
     *  Hidden edges are not included in the returned collection.
     *  The returned collection cannot be modified.
     *  This is an <em>O(1)</em> operation.
     *  @param weight The specified weight.
     *  @return The edges in this graph that have the specified weight.
     *  @exception NullPointerException If the specified weight
     *  is null.
     *  @exception IllegalArgumentException If the specified weight
     *  is not a node weight in this graph.
     */
    public Collection edges(Object weight) {
        return Collections.unmodifiableList(_sameWeightEdges(weight));
    }

    /** Return all the edges in this graph whose weights are contained
     *  in a specified collection.
     *  The edges are returned in the form of a collection.
     *  Duplicate weights in the specified collection result
     *  in duplicate edges in the returned collection.
     *  Each element in the returned collection is an instance of
     *  {@link Edge}.
     *  @param collection The specified collection of weights.
     *  @return The edges in this graph whose weights are contained
     *  in the specified collection.
     */
    public Collection edges(Collection collection) {
        ArrayList edges = new ArrayList();
        Iterator weights = collection.iterator();
        while (weights.hasNext()) {
            edges.addAll(edges(weights.next()));
        }
        return edges;
    }

    /** Test if a graph is equal to this one. It is equal
     *  if it is of the same class, and has the same sets of nodes
     *  and edges.
     *
     *  @param graph The graph with which to compare this graph.
     *  @return True if the graph is equal to this one.
     */
    public boolean equals(Object graph) {
        if (graph == null) {
            return false;
        }
        if (graph.getClass() != getClass()) {
            return false;
        } 
        Graph argumentGraph = (Graph)graph;
        if ((argumentGraph.nodeCount() != nodeCount()) || 
                   (argumentGraph.edgeCount() != edgeCount())) {
            return false;
        }
        Iterator argumentNodes = argumentGraph.nodes().iterator();
        while (argumentNodes.hasNext()) {
            if (!containsNode((Node)argumentNodes.next())) {
                return false;
            }
        }
        Iterator argumentEdges = argumentGraph.edges().iterator();
        while (argumentEdges.hasNext()) {
            if (!containsEdge((Edge)argumentEdges.next())) {
                return false;
            }
        }
        return true;
    }

    /** Returns the hash code for this graph.
     *
     *  @return The hash code for this graph.
     */
    public int hashCode() {
        int code = getClass().getName().hashCode();
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            code += nodes.next().hashCode();
        }
        Iterator edges = edges().iterator();
        while (edges.hasNext()) {
            code += edges.next().hashCode();
        }
        return code;
    }

    /** Return true if a given edge is hidden in this graph.
     *  @param edge The given edge.
     *  @return True if the edge is hidden in this graph.
     */
    public boolean hidden(Edge edge) {
        return _hiddenEdgeSet.contains(edge);
    }

    /** Return the number of hidden edges in the graph.
     *  @return The number of hidden edges.
     */
    public int hiddenEdgeCount() {
        return _hiddenEdgeSet.size();
    }

    /** Return all the hidden edges in this graph in the form of a collection.
     *  Each element in the returned collection is an instance of {@link Edge}.
     *  This is an <em>O(1)</em> operation.
     *  @return All the hidden edges in this graph.
     */
    public Collection hiddenEdges() {
        return Collections.unmodifiableCollection(_hiddenEdgeSet);
    }

    /** Hide an edge if the edge exists in the graph and is not already hidden.
     *  This method removes an edge from the graph, including
     *  removal from the incidence lists of the source and sink nodes, but
     *  preserves the allocation of the edge label to the edge. This
     *  makes the operation more efficient than standard edge removal
     *  {@link #removeEdge(Edge)}, and
     *  allows the same label to be used if the edge is restored later.
     *  This is an <em>O(1)</em> operation.
     *  @param edge The edge to hide.
     *  @return true If the edge is in the graph and not already hidden.
     *  @see #restoreEdge(Edge).
     */
    public boolean hideEdge(Edge edge) {
        if (!containsEdge(edge)) {
            return false;
        }
        if (_hiddenEdgeSet.add(edge)) {
            _disconnectEdge(edge);
            return true;
        } else {
            // The edge is already hidden.
            return false;
        }
    }


    /** Return the number of edges that are incident to a specified node.
     *  @param node The node.
     *  @return The number of incident edges.
     *  @exception IllegalArgumentException If the specified node is not in
     *  the graph.
     */
    public int incidentEdgeCount(Node node) {
        _checkNode(node);
        return _incidentEdgeList(node).size();
    }

    /** Return the set of incident edges for a specified node. Each element in
     *  the returned set is an {@link Edge}.
     *
     *  @param node The specified node.
     *  @return The set of incident edges.
     *  @exception IllegalArgumentException If the specified node is not in
     *  the graph.
     */
    public Collection incidentEdges(Node node) {
        _checkNode(node);
        return Collections.unmodifiableList(_incidentEdgeList(node));
    }

    /** A {@link #mirror(boolean)} method without weights cloning.
     *  The argument <code>cloneWeights</code> is set to default
     *  value of <code>false</code>.
     *
     *  @return The mirror graph.
     */
    public Graph mirror() {
        return mirror(false);
    }

    /** Return a mirror of this graph.
     *  This is a {@link #mirrorAs(Graph, boolean)} with this graph
     *  as the argument graph.
     *  The mirror and original graphs
     *  are isomorphic (of same topology). However, nodes and edges
     *  of the mirror are newly created and therefore not equal to
     *  those of the original graph (See {@link #equals(Object)}).
     *  Users can specify whether to clone weights.
     *
     *  @param cloneWeights True if weight cloning is desired.
     *  @return The mirror graph.
     */
    public Graph mirror(boolean cloneWeights) {
        return mirrorAs(this, cloneWeights);
    }

    /** A {@link #mirrorAs(Graph, boolean)} method without weights cloning.
     *  The argument <code>cloneWeights</code> is set to default value of
     *  <code>false</code>.
     *
     *  @param graph The desired target graph type.
     *  @return The mirror graph.
     */
    public Graph mirrorAs(Graph graph) {
        return mirrorAs(graph, false);
    }

    /** Return a mirror of this graph in the form of the argument graph type
     *  (i.e., the run-time type of the returned graph is that of the
     *  argument graph).  The mirror and original graphs
     *  are isomorphic (of same topology). However, nodes and edges
     *  of the mirror are newly created and therefore not equal to
     *  those of the original graph.
     *  <p>
     *  The returned mirror graph has the same ordering(integer labeling)
     *  of nodes(edges) as the original graph. Therefore, correspondent
     *  nodes(edges) pairs in both graphs can be gotten through same labels.
     *  In other words, labels are used to relate mirror and original
     *  nodes(edges).
     *  <p>
     *  In this method, users can also specify whether to clone node and
     *  edge weights. If the weights are no cloneable, a
     *  <code>RuntimeException</code> will be thrown.
     *
     *  @param graph The desired target graph type.
     *  @param cloneWeights True if weight cloning is desired.
     *  @return The mirror graph.
     */
    public Graph mirrorAs(Graph graph, boolean cloneWeights) {
        String nameClone = new String("clone");
        Graph mirrorGraph = graph._emptyGraph();
        // A map from original nodes to mirror nodes
        HashMap nodeMap = new HashMap();

        // create new nodes for the mirror
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            Node node = (Node)nodes.next();
            Node mirrorNode = null;
            if (!node.hasWeight()) {
                mirrorNode = new Node();
            } else {
                Object mirrorWeight = null;
                try {
                    // Clone weights of any type of object.
                    if (cloneWeights) {
                        Object oldWeight = node.getWeight();
                        if (oldWeight instanceof Cloneable) {
                            /* Since clone() of Object is protected, it can't
                               be called publicly. The class Method is used
                               here to call public clone(). */
                            Class[] argumentTypes = {};
                            Method method = oldWeight.getClass().
                                    getMethod(nameClone, argumentTypes);
                            mirrorWeight = method.invoke(oldWeight, null);
                        } else
                            throw new RuntimeException();
                    } else
                        mirrorWeight = node.getWeight();
                } catch (Exception e) {
                    /* Exception due to non-Cloneable weights or
                       weights without public clone(). */
                    throw new RuntimeException(
                            "Can not clone the node weight.\n");
                }
                mirrorNode = new Node(mirrorWeight);
            }
            nodeMap.put(node, mirrorNode);
            mirrorGraph.addNode(mirrorNode);
        }

        // create new edges for the mirror
        Iterator edges = edges().iterator();
        while (edges.hasNext()) {
            Edge edge = (Edge)edges.next();
            Edge mirrorEdge = null;
            Node mirrorSource = (Node)nodeMap.get(edge.source());
            Node mirrorSink   = (Node)nodeMap.get(edge.sink());
            if (!edge.hasWeight()) {
                mirrorEdge = new Edge(mirrorSource, mirrorSink);
            } else {
                Object mirrorWeight = null;
                try {
                    // Clone weights of any type of object.
                    if (cloneWeights) {
                        Object oldWeight = edge.getWeight();
                        if (oldWeight instanceof Cloneable) {
                            /* Since clone() of Object is protected, it can't
                               be called publicly. The class Method is used
                               here to call public clone(). */
                            Class[] argumentTypes = {};
                            Method method = oldWeight.getClass().
                                    getMethod(nameClone, argumentTypes);
                            mirrorWeight = method.invoke(oldWeight, null);
                        } else
                            throw new RuntimeException();
                    } else
                        mirrorWeight = edge.getWeight();
                } catch (Exception e) {
                    /* Exception due to non-Cloneable weights or
                       weights without public clone(). */
                    throw new RuntimeException(
                            "Can not clone the edge weight.\n");
                }
                mirrorEdge =
                        new Edge(mirrorSource, mirrorSink, mirrorWeight);
            }
            mirrorGraph.addEdge(mirrorEdge);
        }

        return mirrorGraph;
    }

    /** Return the collection of edges that make a node node2 a neighbor of a
     *  node node1. In other words, return the set of edges that are incident to
     *  both node1 and node2. Each element of the returned collection is an
     *  instance of {@link Edge}.
     *  @param node1 The node node1.
     *  @param node2 The node node2.
     *  @return The collection of edges that make node2 a neighbor of node1.
     *  @see DirectedGraph#predecessorEdges(Node, Node)
     *  @see DirectedGraph#successorEdges(Node, Node)
     *  @exception IllegalArgumentException If node1 or node2 is not in this
     *  graph.
     */
    public Collection neighborEdges(Node node1, Node node2) {
        // Method incidentEdges will validate existence of node1 in the graph.
        Collection edgeCollection = incidentEdges(node1);
        _checkNode(node2);
        Iterator edges = edgeCollection.iterator();
        ArrayList commonEdges = new ArrayList();
        while (edges.hasNext()) {
            Edge edge = (Edge)edges.next();
            if (edge.source() == node2) {
                commonEdges.add(edge);
            } else if (edge.sink() == node2) {
                commonEdges.add(edge);
            }
        }
        return commonEdges;
    }

    /** Return all of the neighbors of a given node in the form of a
     *  a collection. Each element of the collection is a Node.
     *  A neighbor of a node X is a node that is the sink
     *  of an edge whose source is X, or the source of a node whose sink
     *  is node X. In other words, a neighbor of X is a node that is adjacent
     *  to X. All elements in the returned collection are unique nodes.
     *  @param node The node whose neighbors are to be returned.
     *  @return The neighbors of the node as a collection.
     */
    public Collection neighbors(Node node) {
        // Method incidentEdges will validate existence of node in the graph.
        Collection incidentEdgeCollection = incidentEdges(node);
        Iterator incidentEdges = incidentEdgeCollection.iterator();
        ArrayList result = new ArrayList(incidentEdgeCollection.size());
        while (incidentEdges.hasNext()) {
            Edge edge = (Edge)(incidentEdges.next());
            Node sink = edge.sink();
            Node source = edge.source();
            if (source == node) {
                if (!result.contains(sink)) {
                    result.add(sink);
                }
            } else if (sink == node) {
                if (!result.contains(source)) {
                    result.add(source);
                }
            }
        }
        return result;
    }

    /** Return a node in this graph that has a specified weight. If multiple
     *  nodes have the specified weight, then return one of them
     *  arbitrarily.
     *  @param weight The specified node weight.
     *  @return A node that has this weight.
     *  @exception NullPointerException If the specified weight
     *  is null.
     *  @exception IllegalArgumentException If the specified weight
     *  is not a node weight in this graph.
     */
    public Node node(Object weight) {
        return (Node)(((ArrayList)_sameWeightNodes(weight)).get(0));
    }

    /** Return a node in this graph given the node label.
     *  @param node The node label.
     *  @return The node.
     *  @exception IndexOutOfBoundsException If the label is not associated with
     *  a node in this graph.
     *  @see #nodeLabel(Node).
     */
    public Node node(int label) {
        return (Node)(_nodes.get(label));
    }

    /** Return the total number of nodes in this graph.
     *  @return The total number of nodes in this graph.
     */
    public int nodeCount() {
        return _nodes.size();
    }

    /** Return the node label of the specified node.
     *  The node label is a unique integer from 0 through
     *  <i>N</i>-1, where <i>N</i> is the number of nodes
     *  currently in the graph. Node labels maintain their
     *  consistency (remain constant) during periods when
     *  no nodes are removed from the graph. When nodes are removed,
     *  the labels assigned to the remaining nodes may change.
     *
     *  @param node A graph node.
     *  @return The node label.
     *  @exception IllegalArgumentException If the specified node is not
     *  a node in this graph.
     */
    public int nodeLabel(Node node) {
        return _nodes.label(node);
    }

    /** Return the node label of the specified node given the node weight.
     *  If multiple nodes have the specified weight, then return one of their
     *  labels arbitrarily.
     *
     *  @param weight The node weight.
     *  @return The node label.
     *  @exception IllegalArgumentException If the specified weight is not
     *  a node weight in this graph.
     *  @see #nodeLabel(Node).
     */
    public int nodeLabel(Object weight) {
        return _nodes.label(node(weight));
    }

    /** Return the weight of a given node in the graph given the node label.
     *
     *  @param node The node label.
     *  @return The weight of the node.
     *  @exception IndexOutOfBoundsException If the label is
     *  not valid.
     *  @exception IllegalArgumentException If the node corresponding
     *  to the label is unweighted.
     *  @see #nodeLabel(Node).
     */
    public Object nodeWeight(int label) {
        return ((Node)(_nodes.get(label))).getWeight();
    }

    /** Return all the nodes in this graph in the form of a collection.
     *  Each element in the returned collection is an instance of {@link Node}.
     *  @return All the nodes in this graph.
     */
    public Collection nodes() {
        return Collections.unmodifiableList(_nodes);
    }

    /** Return all the nodes in this graph that have a specified weight.
     *  The nodes are returned in the form of a collection.
     *  Each element in the returned collection is an instance of {@link Node}.
     *  @param weight The specified weight.
     *  @return The nodes in this graph that have the specified weight.
     *  @exception IllegalArgumentException If the specified weight
     *  is not a node weight in this graph.
     */
    public Collection nodes(Object weight) {
        return Collections.unmodifiableList(_sameWeightNodes(weight));
    }

    /** Return the collection of nodes in this graph whose weights are contained
     *  in a specified collection.
     *  Each element in the returned collection is an instance of
     *  {@link Node}. Duplicate weights in the specified collection result
     *  in duplicate nodes in the returned collection.
     *  @param collection The specified collection of weights.
     *  @return The nodes in this graph whose weights are contained
     *  in a specified collection.
     *  @exception IllegalArgumentException If any specified weight
     *  is not a node weight in this graph.
     */
    public Collection nodes(Collection collection) {
        ArrayList nodes = new ArrayList();
        Iterator weights = collection.iterator();
        while (weights.hasNext()) {
            nodes.addAll(nodes(weights.next()));
        }
        return nodes;
    }

    /** Remove an edge from this graph if it exists in the graph.
     *  The edge may be hidden.
     * An edge that is removed from a graph can be re-inserted
     * into the graph at a later time (using {@link #addEdge(Edge)}),
     * provided that the incident nodes are still in the graph.
     *
     * <p>This is an <em>O(e)</em> operation. A similar operation can be
     * performed in <em>O(1)</em> time using {@link #hideEdge(Edge)}.
     * @param edge The edge to be removed.
     * @return True if the edge was removed.
     * @see #hideEdge(Edge).
     */
    public boolean removeEdge(Edge edge) {
        if (!_edges.contains(edge)) {
            return false;
        }
        _edges.remove(edge);
        if (!hidden(edge)) {
            _disconnectEdge(edge);
        }
        return true;
    }

    /** Remove a node from this graph if it exists in the graph.
     * All edges incident to the node (excluding hidden edges) are also removed.
     * This is an
     * <em>O(n + ke)</em> operation, where <em>k</em> is the number of
     * incident edges.
     * @param node The node to be removed.
     * @return True if the node was removed.
     */
    public boolean removeNode(Node node) {
        if (!_nodes.contains(node)) {
            return false;
        }
        // Avoid concurrent modification of the incident edges list.
        Object[] incidentEdgeArray = incidentEdges(node).toArray();
        _nodes.remove(node);
        for (int i = 0; i < incidentEdgeArray.length; i++) {
            removeEdge((Edge)(incidentEdgeArray[i]));
        }
        _incidentEdgeMap.remove(node);
        if (node.hasWeight()) {
            ArrayList sameWeightList = _sameWeightNodes(node.getWeight());
            sameWeightList.remove(node);
            if (sameWeightList.size() == 0) {
                _nodeWeightMap.remove(node.getWeight());
            }
        }
        _registerChange();
        return true;
    }

    /** Restore an edge if the edge exists in the graph and is presently
     *  hidden. This is an <em>O(1)</em> operation.
     *  @param edge The edge to restore.
     *  @return true If the edge is in the graph and was hidden.
     *  @exception IllegalArgumentException If the source node and sink node
     *  of the given edge are not both in the graph.
     *  @see #hideEdge(Edge).
     */
    public boolean restoreEdge(Edge edge) {
        if (!containsEdge(edge)) {
            return false;
        }
        if (_hiddenEdgeSet.remove(edge)) {
            // Make sure the source and sink are still in the graph.
            if (!containsNode(edge.source())) {
                throw new IllegalArgumentException("Source node is not in "
                        + "the graph.\n" + _edgeDump(edge));
            }
            if (!containsNode(edge.sink())) {
                throw new IllegalArgumentException("Sink node is not in "
                        + "the graph.\n" + _edgeDump(edge));
            }

            // Re-connect the edge.
            _connectEdge(edge);
            return true;
        } else {
            // The edge was not hidden.
            return false;
        }
    }

    /** Return the number of self loop edges in this graph.
     *  @param node The node.
     *  @return The number of self loop edges.
     */
    public int selfLoopEdgeCount() {
        return selfLoopEdges().size();
    }

    /** Return the number of self loop edges of a specified node.
     *  @param node The node.
     *  @return The number of self loop edges.
     *  @exception IllegalArgumentException If the node is not in the graph.
     */
    public int selfLoopEdgeCount(Node node) {
        return selfLoopEdges(node).size();
    }

    /** Return the collection of all self-loop edges in this graph.
     *  Each element in the returned collection is an {@link Edge}.
     *  This operation takes <i>O(E)</i> time, where E is the number
     *  of edges in the graph.
     *  @return The self-loop edges in this graph.
     */
    public Collection selfLoopEdges() {
        return (Collection)_selfLoopAnalysis.result();
    }

    /** Return the collection of all self-loop edges that are incident to
     *  a specified node. Each element in the collection is an {@link Edge}.
     *
     *  @param node The node.
     *  @return The self-loop edges that are incident to the node.
     *  @exception IllegalArgumentException If the node is not in the graph.
     */
    public Collection selfLoopEdges(Node node) {
        ArrayList result = new ArrayList();
        // The call to incidentEdges validates existence of the node.
        Iterator edges = incidentEdges(node).iterator();
        while (edges.hasNext()) {
            Edge edge = (Edge)edges.next();
            if (edge.isSelfLoop()) {
                result.add(edge);
            }
        }
        return result;
    }

    /** Return the subgraph induced by a collection of nodes.
     *  In other words, return the subgraph formed by the given collection N of
     *  nodes together with the set of edges of the form (x, y), where
     *  x and y are both in N.
     *  Node and edge weights are preserved. In derived classes, this
     *  method returns the same type of graph as is returned by
     *  {@link ptolemy.graph.Graph#_emptyGraph()}.
     *  @param nodes The collection of nodes; each element is a {@link Node}.
     *  @return The induced subgraph.
     *  @exception IllegalArgumentException If the collection contains a node
     *  that is not in this graph.
     */
    public Graph subgraph(Collection collection) {
        Graph subgraph = _emptyGraph();
        Iterator nodes = collection.iterator();
        while (nodes.hasNext()) {
            subgraph.addNode((Node)nodes.next());
        }
        nodes = collection.iterator();
        while (nodes.hasNext()) {
            Node node = (Node)nodes.next();
            if (!containsNode(node)) {
                throw new IllegalArgumentException("Attempt to form an "
                        + "induced subgraph \ncontaining a node that is not in "
                        + "the 'parent' graph.\n" + _nodeDump(node));
            }
            Iterator incidentEdges = incidentEdges(node).iterator();
            while (incidentEdges.hasNext()) {
                Edge edge = (Edge)(incidentEdges.next());
                if (subgraph.containsNode(edge.source()) &&
                        subgraph.containsNode(edge.sink()) &&
                        !subgraph.containsEdge(edge)) {
                    subgraph.addEdge(edge);
                }
            }
        }
        return subgraph;
    }

    /** Return the subgraph formed by a subset of nodes and a subset of
     *  edges. Node and edge weights are preserved.
     *  In derived classes, this
     *  method returns the same type of graph as is returned by
     *  {@link ptolemy.graph.Graph#_emptyGraph()}.
     *  @param nodes The subset of nodes; each element is an instance
     *  of {@link Node}.
     *  @param edges The subset of edges. Each element is an instance
     *  of {@link Edge}.
     *  @exception IllegalArgumentException If the argument collections contain
     *  a node or edge that is not in this graph.
     *  @return The subgraph.
     *  @see #addEdges(Collection).
     *  @see #addNodes(Collection).
     */
    public Graph subgraph(Collection nodeCollection,
            Collection edgeCollection) {
        Graph subgraph = _emptyGraph();

        Iterator nodes = nodeCollection.iterator();
        while (nodes.hasNext()) {
            Node node = (Node)(nodes.next());
            if (!containsNode(node)) {
                throw new IllegalArgumentException("Attempt to form a "
                        + "subgraph \ncontaining a node that is not in "
                        + "the 'parent' graph.\n" + _nodeDump(node));
            }
        }

        Iterator edges = edgeCollection.iterator();
        while (edges.hasNext()) {
            Edge edge = (Edge)(edges.next());
            if (!containsEdge(edge)) {
                throw new IllegalArgumentException("Attempt to form a "
                        + "subgraph \ncontaining a edge that is not in "
                        + "the 'parent' graph.\n" + _edgeDump(edge));
            }
        }
        subgraph.addNodes(nodeCollection);
        subgraph.addEdges(edgeCollection);
        return subgraph;
    }

    /** Return a string representation of this graph. The string
     *  representation lists the nodes, including their labels
     *  and their weights, followed by the edges, including their
     *  labels, source nodes, sink nodes, and weights.
     *  @return A string representation of this graph.
     */
    public String toString() {
        StringBuffer result = new StringBuffer("{"
                + this.getClass().getName() + "\n");
        result.append("Node Set:\n" + _nodes.toString("\n", true) + "\n");
        result.append("Edge Set:\n" + _edges.toString("\n", true) + "\n}\n");
        return result.toString();
    }

    /** Return true if the given object is a valid edge weight for this graph.
     *  An object is a valid edge weight if it is meaningful to assign it as
     *  an edge weight in this type of graph.
     *  If the given object is null this method returns true if it is valid
     *  to have an unweighted edge in this type of graph.
     *  This base class method returns true unconditionally.
     *  In derived classes, the method should be
     *  overridden to take into account any restrictions on edge weights.
     *  @param object The given object.
     *  @return True if if the given object is a valid edge weight for this
     *  graph.
     */
    public boolean validEdgeWeight(Object object) {
        return true;
    }

    /** Return true if the given object is a valid node weight for this graph.
     *  An object is a valid node weight if it is meaningful to assign it as
     *  a node weight in this type of graph.
     *  If the given object is null this method returns true if it is valid
     *  to have an unweighted node in this type of graph.
     *  This base class method returns true unconditionally.
     *  In derived classes, the method should be
     *  overridden to take into account any restrictions on node weights.
     *  @param object The given object.
     *  @return True if if the given object is a valid node weight for this
     *  graph.
     */
    public boolean validNodeWeight(Object object) {
        return true;
    }

    /** Validate the weight of a node. This method checks the validity of
     *  of the given node weight, using {@link #validNodeWeight(Object)}, and
     *  updates, if necessary, the internal mapping of weights into
     *  their associated nodes.
     *  This updating operation is necessary for correct operation of
     *  {@link #containsNodeWeight(Object)}, 
     *  {@link #node(Object)}, 
     *  {@link #nodes(Collection)}, and
     *  {@link #nodes(Object)}, 
     *  if the node weight has changed in a way
     *  that affects comparison under the equals method.
     *  This method returns true if the node weight has changed (as determined
     *  by the equals() method) since the last time the graph was notified
     *  of the node's weight. Furthermore, if the node weight has changed in
     *  this way,  a graph change is registered.
     *  This is an <em>O(n)</em> operation. 
     *  @param node The node whose weight is to be validated.
     *  @return True if the node weight has changed.
     *  @exception IllegalStateException if the weight of the given node
     *  is not valid, as determined by {@link #validNodeWeight(Object)}.
     */
    public boolean validateWeight(Node node) {
        // FIXME:  @see #validateWeight(Node, Object).
        boolean weightValueHasChanged = false;
        if (!validNodeWeight(node.getWeight())) {
            throw new IllegalStateException("Invalid weight associated with a "
                    + "node in the graph." +  _nodeDump(node));
        }
        Iterator weights = _nodeWeightMap.keySet().iterator();
        boolean removed = false;
        Object weight = null;
        List nodes = null;
        while (weights.hasNext() && !removed) {
            weight = weights.next();
            nodes = (List)_nodeWeightMap.get(weight);
            removed = nodes.remove(node);
        }  
        if (removed) {
            // Note that the weight can change without the weight value,
            // as referenced here, changing if the change does not affect
            // comparison under the equals method. 
            weightValueHasChanged = weight.equals(node.getWeight());
            if (nodes.size() == 0) {
                _nodeWeightMap.remove(weight);
            }
        }
        _registerWeight(node);
        if (weightValueHasChanged) {
            _registerChange();
        }
        return weightValueHasChanged;
    }

    public void validateWeight(Node node, Object oldWeight) {
    // FIXME: need to implement this
    }
     

    /** Given a collection of graph elements (nodes and edges), return an array
     * of weights associated with these elements.
     * If a weight is common across multiple elements in
     * the collection, it will appear multiple times in the array.
     * If the element collection is null or empty, an empty (zero-element)
     * array is returned.
     * @param elementCollection The collection of graph elements;
     * each element is a {@link Node} or an {@link Edge}.
     * @return The weights of the graph elements, in the order that that
     * elements are returned by collection's iterator; each element in the
     * returned array is an {@link Object}.
     * @exception NullPointerException If the specified collection contains
     * a null value.
     * @exception IllegalArgumentException If the specified collection
     * contains a non-null value that is neither a node nor an edge.
     */
    public static Object[] weightArray(Collection elementCollection) {
        if (elementCollection == null) {
            return new Object[0];
        } else {
            Object[] result = new Object[elementCollection.size()];
            Iterator elements = elementCollection.iterator();
            for (int i = 0; i < elementCollection.size(); i++) {
                Object element = elements.next();
                if (element == null) {
                    throw new NullPointerException("Null graph element "
                            + "specified.\n");
                } else if (element instanceof Node) {
                    result[i] = ((Node)element).getWeight();
                } else if (element instanceof Edge) {
                    result[i] = ((Edge)element).getWeight();
                } else {
                    throw new IllegalArgumentException("Illegal graph element "
                            + "(neither a Node nor an Edge) specified.\n"
                            + "The element's type is: "
                            + element.getClass().getName() + ".\n");
                }
            }
            return result;
        }
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
     *  @exception IllegalArgumentException If either of the specified nodes
     *  is not in the graph.
     *  @exception NullPointerException If the edge is to be weighted, but
     *  the specified weight is null.
     */
    protected Edge _addEdge(Node node1, Node node2, boolean weighted,
            Object weight) {
        if (!containsNode(node1)) {
            throw new IllegalArgumentException("The specified first node "
                    + "is not in the graph.\nThe node: " + _nodeDump(node1));
        }
        else if (!containsNode(node2)) {
            throw new IllegalArgumentException("The specified second node "
                    + "is not in the graph.\nThe node: " + _nodeDump(node2));

        }
        else if (weighted && (weight == null)) {
            throw new IllegalArgumentException("Attempt to assign a null "
                    + "weight to an edge. The first node:\n" + node1
                    + "\nThe second node:\n" + node2 + "The graph: \n" + this);
        } else {
            Edge edge = null;
            if (weighted) {
                edge = new Edge(node1, node2, weight);
            } else {
                edge = new Edge(node1, node2);
            }
            _registerEdge(edge);
            return edge;
        }
    }

    /** Connect an edge to a node by appropriately modifying
     * the adjacency information associated with the node.
     * The node is assumed to be in the graph.
     * @param edge The edge.
     * @param node The node.
     * @exception IllegalArgumentException If the edge has already
     * been connected to the node.
     */
    protected void _connect(Edge edge, Node node) {
        if (_incidentEdgeList(node).contains(edge)) {
            throw new IllegalArgumentException("Attempt to connect the "
                    + "same edge multiple times." + _edgeDump(edge));
        } else {
            _incidentEdgeList(node).add(edge);
        }
    }

    /** Connect a given edge in this graph. The edge and its source and sink
     *  nodes are assumed to be in
     *  the graph. This method performs operations that are common to
     *  the addition of a new edge and the restoration of a hidden edge.
     *  Specifically, this method connects, using {@link #_connect(Edge, Node)},
     *  the given edge to its source and sink nodes;
     *  updates the mapping of weights into corresponding graph
     *  edges; and registers a change in the graph. This method should be
     *  overridden to perform additional operations that are necessary
     *  to connect edges in derived graph classes.
     *  @param edge The edge to connect.
     *  @see #hideEdge(Edge).
     *  @see #removeEdge(Edge).
     *  @see #_disconnectEdge(Edge).
     *  @see #_registerChange().
     */
    protected void _connectEdge(Edge edge) {
        _connect(edge, edge.source());
        if (!edge.isSelfLoop()) {
            _connect(edge, edge.sink());
        }
        if (edge.hasWeight()) {
            ArrayList sameWeightList;
            try {
                sameWeightList = _sameWeightEdges(edge.getWeight());
            } catch (Exception exception) {
                sameWeightList = new ArrayList();
                _edgeWeightMap.put(edge.getWeight(), sameWeightList);
            }
            sameWeightList.add(edge);
        }
        _registerChange();
    }

    /** Disconnect an edge from a node that it is incident to.
     *  Specifically, this method removes the edge from the set of
     *  edges that are considered incident to the node in this graph.
     *  This method does nothing if the given edge is not incident to the
     *  given node.
     *  This method should be overridden to incorporate additional operations
     *  that are required to disconnect an edge from a node (see, for
     *  example, DirectedGraph.#_disconnect(Edge, Node)).
     *  @param edge The edge.
     *  @param node The node.
     */
    protected void _disconnect(Edge edge, Node node) {
        _removeIfPresent(_incidentEdgeList(node), edge);
    }

    /** Disconnect a given edge in this graph. The edge is assumed to be in
     *  the graph and
     *  not already hidden. This method performs operations that are common to
     *  the removal of and hiding of an edge. Specifically, this method
     *  disconnects, using {@link #_disconnect(Edge, Node)}, the given edge
     *  from its source and sink nodes;
     *  updates the mapping of weights into corresponding graph
     *  edges; and registers a change in the graph. This method should be
     *  overridden to perform additional operations that are necessary
     *  to disconnect edges in derived graph classes.
     *  @param edge The edge to disconnect.
     *  @see #hideEdge(Edge).
     *  @see #removeEdge(Edge).
     *  @see #_connectEdge(Edge)
     *  @see #_registerChange().
     */
    protected void _disconnectEdge(Edge edge) {
        _disconnect(edge, edge.source());
        _disconnect(edge, edge.sink());
        if (edge.hasWeight()) {
            ArrayList sameWeightList = _sameWeightEdges(edge.getWeight());
            sameWeightList.remove(edge);
            if (sameWeightList.size() == 0) {
                _edgeWeightMap.remove(edge.getWeight());
            }
        }
        _registerChange();
    }

    /** Return an empty graph that has the same run-time type as this graph.
     *  @return An empty graph.
     */
    protected Graph _emptyGraph() {
        Graph graph = null;
        try {
            graph = (Graph)(getClass().newInstance());
        } catch (Exception exception) {
            throw new RuntimeException("Could not create an empty graph from "
                    + "this one.\n" + exception + "\n" + _graphDump());
        }
        return graph;
    }

    /** Initialize the list of analyses that are associated with this graph,
     *  and initialize the change counter of the graph.
     *  @see ptolemy.graph.analysis.Analysis
     */
    protected void _initializeAnalyses() {
        _analysisList = new ArrayList();
        _selfLoopAnalysis = new SelfLoopAnalysis(this);
        _changeCount = 0;
    }

    /** Register a change to the graph by updating the change counter.
     *  This method must be called after any change to the graph
     *  that may affect (invalidate) any of the computations associated with
     *  analyses that this graph is associated with.
     *  @see Analysis
     */
    protected void _registerChange() {
        if (_changeCount == Long.MAX_VALUE) {
            // Invalidate all of the associated analyses.
            Iterator analyses = _analysisList.iterator();
            while (analyses.hasNext()) {
                ((Analysis)(analyses.next())).reset();
            }
            _changeCount = 0;
        } else {
            _changeCount++;
        }
    }

    /** Register a new edge in the graph. The edge is assumed to
     *  be non-null, unique, and consistent with the node set.
     *  This method performs updates of internal
     *  data structures that are required for every edge that is added
     *  to the graph.
     *  Derived classes can override this method to perform additional updates
     *  of internal data structures.
     *  @param edge The new edge.
     *  @exception IllegalArgumentException If the weight of the given edge is
     *  not valid, as determined by {@link #validEdgeWeight(Object)}.
     *  @see #_registerNode(Node).
     */
    protected void _registerEdge(Edge edge) {
        Object weight = edge.hasWeight() ? edge.getWeight() : null;
        if (!validEdgeWeight(weight)) {
            throw new IllegalArgumentException("Invalid edge weight.\n"
                    + _edgeDump(edge));
        }
        _edges.add(edge);
        _connectEdge(edge);
    }

    /** Register a new node in the graph. The node is assumed to
     *  be non-null and unique. This method performs updates of internal
     *  data structures that are required for every node that is added
     *  to the graph.
     *  Derived classes can override this method to perform additional updates
     *  of internal data structures.
     *  @param node The new node.
     *  @exception IllegalArgumentException If the weight of the given node is
     *  not valid, as determined by {@link #validNodeWeight(Object)}.
     *  @see #_registerEdge(Edge).
     */
    protected void _registerNode(Node node) {
        Object weight = node.hasWeight() ? node.getWeight() : null;
        if (!validNodeWeight(weight)) {
            throw new IllegalArgumentException("Invalid node weight.\n"
                    + _nodeDump(node));
        }
        _nodes.add(node);
        _incidentEdgeMap.put(node, new ArrayList());
        _registerWeight(node);
        _registerChange();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Given two node weights w1 and w2, add all edges of the form
    // edges of the form (x1, x2), where
    //     (x1.getWeight() == w1) && (x2.getWeight() == w2).
    // The third parameter specifies whether the edges are to be
    // weighted, and the fourth parameter is the weight that is
    // to be applied if the edges are weighted.
    // The method returns one of the edges that is added.
    // The method returns an iterator over the edges that were added;
    // each element of this iterator is an instance of Edge.
    // The method throws an IllegalArgumentException if no edge is
    // added (i.e., if no nodes x1, x2 satisfy the above condition.
    // The method throws a NullPointerException if w1 or w2 is null.
    private Collection _addEdges(Object weight1, Object weight2,
            boolean weighted, Object weight) {
        if (weight1 == null) {
            throw new NullPointerException("Null source node weight");
        }
        else if (weight2 == null) {
            throw new NullPointerException("Null sink node weight");
        }
        Iterator nodes1 = nodes(weight1).iterator();
        Edge newEdge = null;
        ArrayList newEdges = new ArrayList();
        while (nodes1.hasNext()) {
            Node node1 = (Node)(nodes1.next());
            Iterator nodes2 = nodes(weight2).iterator();
            while (nodes2.hasNext()) {
                newEdge = _addEdge(node1, (Node)(nodes2.next()), weighted,
                        weight);
                newEdges.add(newEdge);
            }
        }
        if (newEdges.isEmpty()) {
            throw new IllegalArgumentException("No edge can be added based "
                    + "on the specified source and sink node weights.\n"
                    + "Weight1:\n" + weight1 + "\nWeight2:\n" + weight2 + "\n"
                    + _graphDump());
        } else {
            return newEdges;
        }
    }

    // Verify that a node is in the graph.
    // @param The node to verify.
    // @exception IllegalArgumentException If the node is not in the graph.
    private void _checkNode(Node node) {
        if (!containsNode(node)) {
            throw new IllegalArgumentException("Reference to a node that is "
                    + "not in the graph.\n" + _nodeDump(node));
        }
    }

    // Return a dump of an edge and this graph suitable to be appended
    // to an error message.
    private String _edgeDump(Edge edge) {
        String edgeString = (edge == null) ? "<null>" : edge.toString();
        return "\nDumps of the offending edge and graph follow.\n"
            + "The offending edge:\n" + edgeString
            + "\nThe offending graph:\n" + this.description() + "\n";
    }

    // Return a dump of this graph suitable to be appended to an error message.
    private String _graphDump() {
        return "\nA Dump of the offending graph follows.\n" + toString()
            + "\n";
    }

    // Return the list of incident edges for a specified node.
    // Return null if the specified node is not in the graph.
    private ArrayList _incidentEdgeList(Node node) {
        return (ArrayList)_incidentEdgeMap.get(node);
    }

    // Return a dump of a node and this graph suitable to be appended
    // to an error message.
    private String _nodeDump(Node node) {
        String nodeString = (node == null) ? "<null>" : node.toString();
        return "\nDumps of the offending node and graph follow.\n"
            + "The offending node:\n" + nodeString
            + "\nThe offending graph:\n" + this.description() + "\n";
    }

    // Associate a node to its weight in the internal mapping of node
    // weights to nodes.
    // @param node The node.
    private void _registerWeight(Node node) {
        if (node.hasWeight()) {
            Object weight = node.getWeight();
            ArrayList sameWeightList = (ArrayList)(_nodeWeightMap.get(weight));
            if (sameWeightList == null) {
                sameWeightList = new ArrayList();
                _nodeWeightMap.put(weight, sameWeightList);
            }
            sameWeightList.add(node);
        }
    }

    // Remove an object from an ArrayList if it exists in the list.
    private void _removeIfPresent(ArrayList list, Object element) {
        int index;
        if ((index = list.indexOf(element)) != -1) {
            list.remove(index);
        }
    }

    // Return the list of edges that have a given edge weight. Return
    // null if no edges have the given weight.
    // @exception NullPointerException If the specified weight is null.
    // @exception IllegalArgumentException If the specified weight
    // is not an edge weight in this graph.
    private ArrayList _sameWeightEdges(Object weight) {
        if (weight == null) {
            throw new NullPointerException("Null edge weight specified.");
        } else {
            ArrayList edgeList = (ArrayList)_edgeWeightMap.get(weight);
            if (edgeList == null) {
                throw new IllegalArgumentException("The specified weight "
                        + "is not an edge weight in this graph."
                        + _weightDump(weight));
            } else {
                return edgeList;
            }
        }
    }

    // Return the list of nodes that have a given node weight. Return
    // null if no nodes have the given weight.
    // @exception NullPointerException If the specified weight is null.
    // @exception IllegalArgumentException If the specified weight
    // is not a node weight in this graph.
    private ArrayList _sameWeightNodes(Object weight) {
        if (weight == null) {
            throw new NullPointerException("Null node weight specified.");
        } else {
            ArrayList nodeList = (ArrayList)_nodeWeightMap.get(weight);
            if (nodeList == null) {
                throw new IllegalArgumentException("The specified weight "
                        + "is not a node weight in this graph."
                        + _weightDump(weight));
            } else {
                return nodeList;
            }
        }
    }

    // Return a dump of a node or edge weight and this graph suitable to be
    // appended to an error message.
    private String _weightDump(Object weight) {
        String weightString = (weight == null) ? "<null>" : weight.toString();
        return "\nDumps of the offending weight and graph follow.\n"
            + "The offending weight:\n" + weightString
            + "\nThe offending graph:\n" + this.description() + "\n";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A list of analyses that are associated with this graph. Each
    // element of the list is an instance of ptolemy.graph.analysis.Analysis.
    private ArrayList _analysisList;

    // A counter that keeps track of changes to the graph.
    private long _changeCount;

    // A mapping from edge weights to associated edges. Unweighted edges are not
    // represented in this map. Keys in this this map are instances of
    // of Object, and values are instances of ArrayList whose elements
    // are instances of Edge.
    private HashMap _edgeWeightMap;

    // The list of edges in this graph.
    // Each element of this list is an Edge.
    private LabeledList _edges;

    // The set of hidden edges. Each element is an Edge.
    // Hidden edges remain contained in the _edges list, but are removed from
    // the incidence and weight maps.
    private HashSet _hiddenEdgeSet;

    // A mapping from nodes into their lists of incident edges.
    // This redundant information is maintained for improved
    // run-time efficiency when handing undirected graphs, or when operating
    // on directed graphs in ways for which edge orientation is not relevant.
    // Each key in this map is an instance of Node. Each value
    // is an instance of ArrayList whose elements are instances of Edge.
    private HashMap _incidentEdgeMap;

    // A mapping from node weights to associated nodes. Unweighted
    // nodes are not represented in this map. Keys in this this map
    // are instances of of Object, and values instances of ArrayList
    // whose elements are instances of Node.
    private HashMap _nodeWeightMap;

    // The list of nodes in this graph.
    // Each element of this list is a Node.
    private LabeledList _nodes;

    // The analysis for computation of self loop edges.
    private SelfLoopAnalysis _selfLoopAnalysis;

}
