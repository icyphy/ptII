/* A graph with optionally-weighted nodes and edges.

Copyright (c) 1997-2005 The Regents of the University of California.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import ptolemy.graph.analysis.Analysis;
import ptolemy.graph.analysis.SelfLoopAnalysis;
import ptolemy.graph.analysis.strategy.CachedStrategy;


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

   <p>The same node can exist in multiple graphs, but any given graph can
   contain only one instance of the node. Node labels, however, are local to
   individual graphs. Thus, the same node may have different labels in different
   graphs.
   Furthermore, the label assigned in a given graph to a node may change over
   time (if the set of nodes in the graph changes). If a node is contained in
   multiple graphs, it has the same weight in all of the graphs.
   All of this holds for edges
   as well. The same weight may be shared among multiple nodes and edges.

   <p> Multiple edges in a graph can connect the same pair of nodes.
   Thus, multigraphs are supported.

   <p>Once assigned, node and edge weights should not be changed in ways that
   affect comparison under the <code>equals</code> method.
   Otherwise, unpredictable behavior may result.

   <p>In discussions of complexity, <em>n</em> and <em>e</em> refers to the
   number of graph nodes and edges, respectively.

   <p>In derived classes, the following methods need special
   attention regarding whether or not they should be overridden:
   <br>{@link #validEdgeWeight(Object)} {@link #validNodeWeight(Object)}

   @author Shuvra S. Bhattacharyya, Ming-Yung Ko, Fuat Keceli,
   Shahrooz Shahparnia, Yuhong Xiong, Jie Liu.
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
   @see ptolemy.graph.Edge
   @see ptolemy.graph.Node
*/
public class Graph implements Cloneable {
    /** Construct an empty graph.
     */
    public Graph() {
        _nodes = new ElementList("node", this);
        _edges = new ElementList("edge", this);
        _initializeAnalyses();
        _hiddenEdgeSet = new HashSet();
        _incidentEdgeMap = new HashMap();
    }

    /** Construct an empty graph with enough storage allocated for the
     *  specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param nodeCount The number of nodes.
     */
    public Graph(int nodeCount) {
        _nodes = new ElementList("node", this, nodeCount);
        _edges = new ElementList("edge", this);
        _initializeAnalyses();
        _hiddenEdgeSet = new HashSet();
        _incidentEdgeMap = new HashMap(nodeCount);
    }

    /** Construct an empty graph with enough storage allocated for the
     *  specified number of edges, and number of nodes.  Memory
     *  management is more efficient with this constructor if the
     *  number of nodes and edges is known.
     *  @param nodeCount The number of nodes.
     *  @param edgeCount The number of edges.
     */
    public Graph(int nodeCount, int edgeCount) {
        _nodes = new ElementList("node", this, nodeCount);
        _edges = new ElementList("edge", this, edgeCount);
        _initializeAnalyses();
        _hiddenEdgeSet = new HashSet();
        _incidentEdgeMap = new HashMap(nodeCount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an analysis to the list of analyses that this graph is associated
     *  with. This method is called by {@link ptolemy.graph.analysis.Analysis}
     *  when an analysis is created, and normally should not be called
     *  elsewhere.
     *
     *  @param analysis The analysis.
     *  @exception IllegalArgumentException If the graph associated with the
     *  analysis is not equal to this graph, or if the graph already contains
     *  the analysis in its list of analyses.
     */
    public void addAnalysis(Analysis analysis) {
        if (analysis.graph() != this) {
            throw new IllegalArgumentException("Invalid associated graph.\n"
                    + "The analysis:\n" + analysis + "\n");
        }

        if (_analysisList.contains(analysis)) {
            throw new IllegalArgumentException("Attempt to add "
                    + "duplicate analysis.\nThe analysis:\n" + analysis);
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
     *  @exception GraphElementException If the first node or second
     *  node is not already in the graph.
     *  @exception NullPointerException If the weight is <code>null</code>.
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
     *  @exception GraphElementException If the first node or second
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
     *  @exception GraphElementException If no edge is
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
     *  @exception GraphElementException If no edge is
     *  added (i.e., if no nodes x1, x2 satisfy the above condition).
     */
    public Collection addEdge(Object weight1, Object weight2) {
        return _addEdges(weight1, weight2, false, null);
    }

    /** Add a pre-constructed edge (unweighted or weighted).
     *
     *  @param edge The edge.
     *  @exception GraphElementException If the source or sink node
     *  of the edge is not already in the graph.
     *  @exception GraphConstructionException If the edge is already in
     *  the graph, or if the edge is hidden in the graph.
     *  @see #hideEdge(Edge).
     */
    public Edge addEdge(Edge edge) {
        if (!containsNode(edge.source())) {
            throw new GraphElementException(edge, this,
                    "The source node is not in the graph.");
        } else if (!containsNode(edge.sink())) {
            throw new GraphElementException(edge, this,
                    "The sink node is not in the graph.");
        } else if (containsEdge(edge)) {
            throw new GraphConstructionException("Attempt to add an edge that "
                    + "is already in the graph."
                    + GraphException.elementDump(edge, this));
        } else if (hidden(edge)) {
            throw new GraphConstructionException("Attempt to add an edge that "
                    + "is already hidden in the graph."
                    + GraphException.elementDump(edge, this));
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
            addEdge((Edge) (edges.next()));
        }
    }

    /** Add a given graph to this graph. This base class method simply
     *  adds all nodes and edges in the given graph to this graph. If a derived
     *  class contains extra fields associated with
     *  edges, nodes and the graph itself, it can override this method to
     *  handle those fields. This method does not add hidden edges of
     *  the argument graph to this graph.
     *  @param graph The graph to add.
     *  @return True if this graph changed as a result of the call.
     */
    public boolean addGraph(Graph graph) {
        addNodes(graph.nodes());
        addEdges(graph.edges());
        return (graph.nodeCount() + graph.edgeCount()) > 0;
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
     *  @exception GraphConstructionException If the node is already
     *  in the graph.
     *  @exception GraphWeightException If the weight is invalid.
     */
    public Node addNode(Node node) {
        if (containsNode(node)) {
            throw new GraphConstructionException("Attempt to add a node "
                    + "that is already contained in the graph."
                    + GraphException.elementDump(node, this));
        } else {
            _registerNode(node);
            return node;
        }
    }

    /** Add a new weighted node to this graph given the node weight.
     *
     *  @param weight The node weight.
     *  @return The node.
     *  @exception GraphWeightException If the specified weight is null.
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
            addNode((Node) (nodes.next()));
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
            cloneGraph.addNode((Node) nodes.next());
        }

        Iterator edges = edges().iterator();

        while (edges.hasNext()) {
            cloneGraph.addEdge((Edge) edges.next());
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
            Node node = (Node) nodes.next();
            ArrayList component = new ArrayList();
            component.add(node);

            Node container = new Node(component);
            componentMap.put(node, container);
            components.add(container);
        }

        Iterator edges = edges().iterator();

        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();
            Node sourceContainer = (Node) (componentMap.get(edge.source()));
            Node sinkContainer = (Node) (componentMap.get(edge.sink()));
            ArrayList sourceSet = (ArrayList) (sourceContainer.getWeight());
            ArrayList sinkSet = (ArrayList) (sinkContainer.getWeight());

            if (sourceSet != sinkSet) {
                // Construct the union of the two components in the source set.
                components.remove(sinkContainer);

                Iterator moveNodes = sinkSet.iterator();

                while (moveNodes.hasNext()) {
                    Node moveNode = (Node) moveNodes.next();
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
            result.add(((Node) connectedComponents.next()).getWeight());
        }

        return result;
    }

    /** Return true if the specified edge exists in the graph, and the
     *  edge is not hidden in the graph.
     *  @param edge The specified edge.
     *  @return True if the specified edge exists in the graph and is not
     *  hidden.
     *  @see #hidden(Edge)
     *  @see #hideEdge(Edge)
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
        // FIXME: on null, return true if there is an unweighted element.
        return _edges.containsWeight(weight);
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
        // FIXME: on null, return true if there is an unweighted element.
        return _nodes.containsWeight(weight);
    }

    /** Return an edge in this graph that has a specified weight. If multiple
     *  edges have the specified weight, then return one of them
     *  arbitrarily. If the specified weight is null, return an unweighted
     *  edge (again arbitrarily chosen if there are multiple unweighted
     *  edges).
     *  @param weight The specified edge weight.
     *  @return An edge that has this weight.
     *  @exception GraphWeightException If the specified weight
     *  is not an edge weight in this graph or if the specified weight
     *  is null but the graph does not contain any unweighted edges.
     */
    public Edge edge(Object weight) {
        return (Edge) _edges.element(weight);
    }

    /** Return an edge in this graph given the edge label;
     *  the returned edge may be hidden see {@link #hideEdge(Edge)}.
     *  @param label The edge label.
     *  @return The edge.
     *  @exception GraphElementException If the label is not associated
     *  with an edge in this graph.
     *  @see #edgeLabel(Edge)
     */
    public Edge edge(int label) {
        return (Edge) (_edges.get(label));
    }

    /** Return the total number of edges in this graph.  Multiple
     *  connections between two nodes are counted multiple times.
     *  Hidden edges are not included in this count.
     *  @return The total number of edges in this graph.
     */
    public int edgeCount() {
        return _edges.size() - _hiddenEdgeSet.size();
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
     *  @exception GraphElementException If the specified edge is not
     *  not an edge in this graph.
     */
    public int edgeLabel(Edge edge) {
        return _edges.label(edge);
    }

    /** Return the edge label of the specified edge given the edge weight.
     *  If multiple edges have the specified weight, then return one of their
     *  labels arbitrarily.
     *
     *  @param weight The edge weight.
     *  @return The edge label.
     *  @exception GraphWeightException If the specified weight is not
     *  an edge weight in this graph.
     *  @see #edgeLabel(Edge)
     */
    public int edgeLabel(Object weight) {
        return _edges.label(edge(weight));
    }

    /** Return the weight of a given edge in the graph given the edge label.
     *
     *  @param label The edge label.
     *  @return The weight of the edge.
     *  @exception IndexOutOfBoundsException If the label is
     *  not valid.
     *  @exception GraphWeightException If the edge corresponding
     *  to the label is unweighted.
     *  @see #edgeLabel(Edge)
     */
    public Object edgeWeight(int label) {
        return ((Edge) (_edges.get(label))).getWeight();
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
        if (hiddenEdgeCount() == 0) {
            return Collections.unmodifiableList(_edges);
        }

        // There is at least one hidden edge.
        int visibleEdgeCount = _edges.size() - hiddenEdgeCount();
        ArrayList result = new ArrayList(visibleEdgeCount);

        if (visibleEdgeCount == 0) {
            return Collections.unmodifiableList(result);
        }

        // There is at least one edge to return.
        Iterator edges = _edges.iterator();

        while (edges.hasNext()) {
            Edge edge = (Edge) (edges.next());

            if (!hidden(edge)) {
                result.add(edge);
            }
        }

        return Collections.unmodifiableList(result);
    }

    /** Return all the edges in this graph that have a specified weight.
     *  The edges are returned in the form of a collection.
     *  If the specified weight is null, return all the unweighted edges.
     *  If no edges have the specified weight (or if the argument is null and
     *  there are no unweighted edges), return an empty collection.
     *  Each element in the returned collection is an instance of {@link Node}.
     *  @param weight The specified weight.
     *  @return The edges in this graph that have the specified weight.
     */
    public Collection edges(Object weight) {
        // Hidden edges will not be included since their weights are
        // disassociated in the element list.
        return _edges.elements(weight);
    }

    /** Return all the edges in this graph whose weights are contained
     *  in a specified collection.
     *  The edges are returned in the form of a collection.
     *  Each element in the returned collection is an instance of
     *  {@link Edge}. A null element in the argument collection is interpreted
     *  to mean that all unweighted edges are to be included in the result.
     *  Duplicate weights or null elements in the specified collection result
     *  in duplicate edges in the returned collection.
     *  Non-null elements in the argument collection that are not edge weights
     *  are ignored.
     *  @param collection The specified collection of weights.
     *  @return The edges in this graph whose weights are contained
     *  in the specified collection.
     *  @see #edges(Object)
     */
    public Collection edges(Collection collection) {
        // Hidden edges will not be included since they are removed from
        // the weight map.
        ArrayList edges = new ArrayList();
        Iterator weights = collection.iterator();

        while (weights.hasNext()) {
            edges.addAll(edges(weights.next()));
        }

        return Collections.unmodifiableCollection(edges);
    }

    /** Test if a graph is equal to this one. It is equal
     *  if it is of the same class, and has the same sets of nodes
     *  and edges.
     *
     *  <p> Derived graph classes may override this method if
     *  there is additional information in the graphs (beyond nodes
     *  and edges) that is relevant to equality.
     *
     *  @param graph The graph with which to compare this graph.
     *  @return True if the graph is equal to this one.
     *  @see #hashCode()
     */
    public boolean equals(Object graph) {
        if (graph == null) {
            return false;
        }

        if (graph.getClass() != getClass()) {
            return false;
        }

        Graph argumentGraph = (Graph) graph;

        if ((argumentGraph.nodeCount() != nodeCount())
                || (argumentGraph.edgeCount() != edgeCount())) {
            return false;
        }

        Iterator argumentNodes = argumentGraph.nodes().iterator();

        while (argumentNodes.hasNext()) {
            if (!containsNode((Node) argumentNodes.next())) {
                return false;
            }
        }

        Iterator argumentEdges = argumentGraph.edges().iterator();

        while (argumentEdges.hasNext()) {
            if (!containsEdge((Edge) argumentEdges.next())) {
                return false;
            }
        }

        return true;
    }

    /** Returns the hash code for this graph. The hash code is the
     *  sum of the hash codes of the nodes and edges.
     *
     *  <p> Derived graph classes may override this method if
     *  there is additional information in the graphs (beyond nodes
     *  and edges) that is relevant to equality between graphs.
     *
     *  @return The hash code for this graph.
     *  @see #equals(Object)
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
     *  @return True if the edge was in the graph and not already hidden.
     *  @see #restoreEdge(Edge)
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
     *  @exception GraphElementException If the specified node is not in
     *  the graph.
     */
    public int incidentEdgeCount(Node node) {
        GraphElementException.checkNode(node, this);
        return _incidentEdgeList(node).size();
    }

    /** Return the set of incident edges for a specified node. Each element in
     *  the returned set is an {@link Edge}.
     *
     *  @param node The specified node.
     *  @return The set of incident edges.
     *  @exception GraphElementException If the specified node is not in
     *  the graph.
     */
    public Collection incidentEdges(Node node) {
        GraphElementException.checkNode(node, this);
        return Collections.unmodifiableList(_incidentEdgeList(node));
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
     *  @exception GraphElementException If node1 or node2 is not in this
     *  graph.
     */
    public Collection neighborEdges(Node node1, Node node2) {
        // Method incidentEdges will validate existence of node1 in the graph.
        Collection edgeCollection = incidentEdges(node1);
        GraphElementException.checkNode(node2, this);

        Iterator edges = edgeCollection.iterator();
        ArrayList commonEdges = new ArrayList();

        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();

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
            Edge edge = (Edge) (incidentEdges.next());
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
     *  arbitrarily. If the specified weight is null, return an unweighted
     *  node (again arbitrarily chosen if there are multiple unweighted
     *  nodes).
     *  @param weight The specified node weight.
     *  @return A node that has this weight.
     *  @exception GraphWeightException If the specified weight
     *  is not a node weight in this graph or if the specified weight
     *  is null but the graph does not contain any unweighted nodes.
     */
    public Node node(Object weight) {
        return (Node) _nodes.element(weight);
    }

    /** Return a node in this graph given the node label.
     *  @param label The node label.
     *  @return The node.
     *  @exception IndexOutOfBoundsException If the label is not
     *  associated with a node in this graph.
     *  @see #nodeLabel(Node)
     */
    public Node node(int label) {
        return (Node) (_nodes.get(label));
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
     *  @exception GraphElementException If the specified node is not
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
     *  @exception GraphWeightException If the specified weight is not
     *  a node weight in this graph.
     *  @see #nodeLabel(Node)
     */
    public int nodeLabel(Object weight) {
        return _nodes.label(node(weight));
    }

    /** Return the weight of a given node in the graph given the node label.
     *
     *  @param label The node label.
     *  @return The weight of the node.
     *  @exception IndexOutOfBoundsException If the label is
     *  not valid.
     *  @exception GraphWeightException If the node corresponding
     *  to the label is unweighted.
     *  @see #nodeLabel(Node)
     */
    public Object nodeWeight(int label) {
        return ((Node) (_nodes.get(label))).getWeight();
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
     *  If the specified weight is null, return all the unweighted nodes.
     *  If no nodes have the specified weight (or if the argument is null and
     *  there are no unweighted nodes), return an empty collection.
     *  Each element in the returned collection is an instance of {@link Node}.
     *  @param weight The specified weight.
     *  @return The nodes in this graph that have the specified weight.
     */
    public Collection nodes(Object weight) {
        return _nodes.elements(weight);
    }

    /** Return the collection of nodes in this graph whose weights are
     *  contained in a specified collection.
     *  Each element in the returned collection is an instance of
     *  {@link Node}.
     *  A null element in the argument collection is interpreted
     *  to mean that all unweighted nodes are to be included in the result.
     *  Duplicate weights or null elements in the specified collection result
     *  in duplicate nodes in the returned collection.
     *  Non-null elements in the argument collection that are not node weights
     *  are ignored.
     *  @param collection The specified collection of weights.
     *  @return The nodes in this graph whose weights are contained
     *  in a specified collection.
     *  @exception GraphWeightException If any specified weight
     *  is not a node weight in this graph.
     */
    public Collection nodes(Collection collection) {
        ArrayList nodes = new ArrayList();
        Iterator weights = collection.iterator();

        while (weights.hasNext()) {
            nodes.addAll(nodes(weights.next()));
        }

        return Collections.unmodifiableCollection(nodes);
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
     * @see #hideEdge(Edge)
     */
    public boolean removeEdge(Edge edge) {
        if (!_edges.contains(edge)) {
            return false;
        }

        _edges.remove(edge);

        if (hidden(edge)) {
            _hiddenEdgeSet.remove(edge);
        } else {
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
            removeEdge((Edge) (incidentEdgeArray[i]));
        }

        _incidentEdgeMap.remove(node);
        _registerChange();
        return true;
    }

    /** Restore an edge if the edge exists in the graph and is presently
     *  hidden. This is an <em>O(1)</em> operation.
     *  @param edge The edge to restore.
     *  @return True if the edge is in the graph and was hidden.
     *  @exception GraphElementException If the source node and
     *  sink node of the given edge are not both in the graph.
     *  @see #hideEdge(Edge).
     */
    public boolean restoreEdge(Edge edge) {
        if (_hiddenEdgeSet.remove(edge)) {
            // Make sure the source and sink are still in the graph.
            if (!containsNode(edge.source())) {
                throw new GraphElementException(edge, this,
                        "Source node is not in the graph.");
            }

            if (!containsNode(edge.sink())) {
                throw new GraphElementException(edge, this,
                        "Sink node is not in the graph.");
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
     *  @return The number of self loop edges.
     */
    public int selfLoopEdgeCount() {
        return selfLoopEdges().size();
    }

    /** Return the number of self loop edges of a specified node.
     *  @param node The node.
     *  @return The number of self loop edges.
     *  @exception GraphElementException If the node is not in the graph.
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
        return (Collection) _selfLoopAnalysis.edges();
    }

    /** Return the collection of all self-loop edges that are incident to
     *  a specified node. Each element in the collection is an {@link Edge}.
     *
     *  @param node The node.
     *  @return The self-loop edges that are incident to the node.
     *  @exception GraphElementException If the node is not in the graph.
     */
    public Collection selfLoopEdges(Node node) {
        ArrayList result = new ArrayList();

        // The call to incidentEdges validates existence of the node.
        Iterator edges = incidentEdges(node).iterator();

        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();

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
     *  Derived classes that do not have zero-argument constructors may
     *  need to override this method to properly initialize the subgraph.
     *  @param collection The collection of nodes; each element
     *  is a {@link Node}.
     *  @return The induced subgraph.
     *  @exception GraphElementException If the collection contains a node
     *  that is not in this graph.
     */
    public Graph subgraph(Collection collection) {
        Graph subgraph = _emptyGraph();
        Iterator nodes = collection.iterator();

        while (nodes.hasNext()) {
            subgraph.addNode((Node) nodes.next());
        }

        nodes = collection.iterator();

        while (nodes.hasNext()) {
            Node node = (Node) nodes.next();

            if (!containsNode(node)) {
                throw new GraphElementException(node, this,
                        "Attempt to form an induced subgraph containing a "
                        + "node that is not in the 'parent' graph.");
            }

            Iterator incidentEdges = incidentEdges(node).iterator();

            while (incidentEdges.hasNext()) {
                Edge edge = (Edge) (incidentEdges.next());

                if (subgraph.containsNode(edge.source())
                        && subgraph.containsNode(edge.sink())
                        && !subgraph.containsEdge(edge)) {
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
     *  @param nodeCollection The subset of nodes; each element is an instance
     *  of {@link Node}.
     *  @param edgeCollection The subset of edges. Each element is an instance
     *  of {@link Edge}.
     *  @exception GraphElementException If the argument collections contain
     *  a node or edge that is not in this graph.
     *  @return The subgraph.
     *  @see #addEdges(Collection)
     *  @see #addNodes(Collection)
     */
    public Graph subgraph(Collection nodeCollection, Collection edgeCollection) {
        Graph subgraph = _emptyGraph();

        Iterator nodes = nodeCollection.iterator();

        while (nodes.hasNext()) {
            Node node = (Node) (nodes.next());

            if (!containsNode(node)) {
                throw new GraphElementException(node, this,
                        "Attempt to form a subgraph containing a node "
                        + "that is not in the 'parent' graph.");
            }
        }

        Iterator edges = edgeCollection.iterator();

        while (edges.hasNext()) {
            Edge edge = (Edge) (edges.next());

            if (!containsEdge(edge)) {
                throw new GraphElementException(edge, this,
                        "Attempt to form a subgraph containing an edge "
                        + "that is not in the 'parent' graph.");
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
        StringBuffer result = new StringBuffer("{" + this.getClass().getName()
                + "\n");
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

    /** Validate the weight of an edge. Operation parallels that of
     *  #validateWeight(Node).
     *  @param edge The edge whose weight is to be validated.
     *  @return True if the edge weight has changed, as determined by
     *  the equals method.
     *  @exception GraphElementException If the specified edge is not in
     *  the graph.
     *  @exception GraphWeightException If the weight of the given edge
     *  is not valid, as determined by {@link #validEdgeWeight(Object)}.
     *  @see #validateWeight(Edge, Object)
     *  @see #validateWeight(Node)
     */
    public boolean validateWeight(Edge edge) {
        if (edge == null) {
            throw new NullPointerException("Attempt to validate the weight "
                    + "of a null graph edge.");
        }

        if (!containsEdge(edge)) {
            throw new GraphElementException(edge, this,
                    "The specified edge is not in the graph.");
        }

        Object weightArgument = edge.hasWeight() ? edge.getWeight() : null;

        if (!validEdgeWeight(weightArgument)) {
            throw new GraphWeightException(weightArgument, edge, this,
                    "Invalid weight associated with an edge in the graph.\n");
        }

        boolean changed = _edges.changeWeight(edge);

        if (changed) {
            _registerChange();
        }

        return changed;
    }

    /** Validate the weight of an edge given the edge and its previous weight.
     *  Operation parallels that of #validateWeight(Node, Object).
     *
     *  @param edge The edge whose weight is to be validated.
     *  @param oldWeight The previous weight of the edge (null if the edge
     *  was previously unweighted).
     *  @return True if the edge weight has changed, as determined by
     *  the equals method.
     *  @see #validateWeight(Edge)
     *  @see #validateWeight(Node, Object)
     */
    public boolean validateWeight(Edge edge, Object oldWeight) {
        if (!containsEdge(edge)) {
            throw new GraphElementException(edge, this,
                    "The specified edge is not in the graph.");
        }

        Object newWeight = edge.hasWeight() ? edge.getWeight() : null;

        if (!validEdgeWeight(newWeight)) {
            throw new GraphWeightException(newWeight, edge, this,
                    "Invalid weight associated with an edge in the graph.");
        }

        boolean changed = _edges.validateWeight(edge, oldWeight);

        if (changed) {
            _registerChange();
        }

        return changed;
    }

    /** Validate the weight of a node. This method checks the validity of
     *  the node weight (using {@link #validNodeWeight(Object)}, and
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
     *  @return True if the node weight has changed, as determined by
     *  the equals method.
     *  @exception GraphElementException If the specified node is not in
     *  the graph.
     *  @exception GraphWeightException If the weight of the given node
     *  is not valid, as determined by {@link #validNodeWeight(Object)}.
     *  @see #validateWeight(Node, Object)
     */
    public boolean validateWeight(Node node) {
        if (node == null) {
            throw new NullPointerException("Attempt to validate the weight "
                    + "of a null graph node.");
        }

        if (!containsNode(node)) {
            throw new GraphElementException(node, this,
                    "The specified node is not in the graph.");
        }

        Object weightArgument = node.hasWeight() ? node.getWeight() : null;

        if (!validNodeWeight(weightArgument)) {
            throw new GraphWeightException(weightArgument, node, this,
                    "Invalid weight associated with a node in the graph.");
        }

        boolean changed = _nodes.changeWeight(node);

        if (changed) {
            _registerChange();
        }

        return changed;
    }

    /** Validate the weight of a node given the node and its previous weight.
     *  The previous weight argument should be set to
     *  the weight of the node when the node was last added
     *  to the graph or last had its node validated, whichever was more recent.
     *  Operation is equivalent to {@link #validateWeight(Node)}
     *  except that the additional argument is used to improve efficiency.
     *  The previous node weight should be set to null to indicate that
     *  the node was previously unweighted.
     *
     *  <p>Consider an example in which a given Node <em>node</em> is
     *  contained in two graphs <em>graph1</em> and <em> graph2 </em>,
     *  and suppose that we wish to change the weight of the
     *  node. Below is a sample code fragment that achieves such a
     *  weight change with proper notification to the containing
     *  graphs.
     *
     *  <pre>
     *  Object oldWeight = node.getWeight();
     *  node.setWeight(newWeight);
     *  graph1.validateWeight(node, oldWeight);
     *  graph2.validateWeight(node, oldWeight);
     *  </pre>
     *
     *  <p>In this example, #validateWeight(Node) could be used
     *  (e.g., if the previous weight <em>oldWeight</em> was not available)
     *  in place of #validateWeight(Node, Object), but the efficiency would be
     *  lower.
     *
     *  @param node The node whose weight is to be validated.
     *  @param oldWeight The previous weight of the node.
     *  @return True if the node weight has changed, as determined by
     *  the equals method.
     *  @see #validateWeight(Node)
     */
    public boolean validateWeight(Node node, Object oldWeight) {
        if (!containsNode(node)) {
            throw new GraphElementException(node, this,
                    "The specified node is not in the graph.");
        }

        Object newWeight = node.hasWeight() ? node.getWeight() : null;

        if (!validNodeWeight(newWeight)) {
            throw new GraphWeightException(newWeight, node, this,
                    "Invalid weight associated with a node in the graph.");
        }

        boolean changed = _nodes.validateWeight(node, oldWeight);

        if (changed) {
            _registerChange();
        }

        return changed;
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
     * @exception GraphElementException If the specified collection
     * contains a non-null value that is neither a node nor an edge.
     */
    public static Object[] weightArray(Collection elementCollection) {
        if (elementCollection == null) {
            return new Object[0];
        } else {
            Element element = null;
            Object[] result = new Object[elementCollection.size()];
            Iterator elements = elementCollection.iterator();

            try {
                for (int i = 0; i < elementCollection.size(); i++) {
                    element = (Element) (elements.next());

                    if (element == null) {
                        throw new NullPointerException("Null graph element "
                                + "specified.\n");
                    } else {
                        result[i] = element.getWeight();
                    }
                }
            } catch (ClassCastException exception) {
                throw new GraphElementException("Illegal graph element "
                        + "(neither a Node nor an Edge) specified.\n"
                        + "The element's type is: " + element.getClass().getName()
                        + ".\n");
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
     *  @exception GraphElementException If either of the specified
     *  nodes is not in the graph.
     *  @exception NullPointerException If the edge is to be weighted, but
     *  the specified weight is null.
     */
    protected Edge _addEdge(Node node1, Node node2, boolean weighted,
            Object weight) {
        if (!containsNode(node1)) {
            throw new GraphElementException(node1, this,
                    "The specified first node is not in the graph.");
        } else if (!containsNode(node2)) {
            throw new GraphElementException(node2, this,
                    "The specified second node is not in the graph.");
        } else if (weighted && (weight == null)) {
            throw new NullPointerException("Attempt to assign a null "
                    + "weight to an edge. The first node:\n" + node1
                    + "\nThe second node:\n" + node2 + "\nThe graph: \n" + this);
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
     * @exception GraphConstructionException If the edge has already
     * been connected to the node.
     */
    protected void _connect(Edge edge, Node node) {
        if (_incidentEdgeList(node).contains(edge)) {
            throw new GraphConstructionException(
                    "Attempt to connect the same edge multiple times."
                    + GraphException.elementDump(edge, this));
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
     *  @see #hideEdge(Edge)
     *  @see #removeEdge(Edge)
     *  @see #_disconnectEdge(Edge)
     *  @see #_registerChange()
     */
    protected void _connectEdge(Edge edge) {
        _connect(edge, edge.source());

        if (!edge.isSelfLoop()) {
            _connect(edge, edge.sink());
        }

        _edges.registerWeight(edge);
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
        _incidentEdgeList(node).remove(edge);
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
     *  @see #hideEdge(Edge)
     *  @see #removeEdge(Edge)
     *  @see #_connectEdge(Edge)
     *  @see #_registerChange()
     */
    protected void _disconnectEdge(Edge edge) {
        _disconnect(edge, edge.source());
        _disconnect(edge, edge.sink());
        _edges.cancelWeight(edge);
        _registerChange();
    }

    /** Return an empty graph that has the same run-time type as this graph.
     *  This class should be overridden in derived classes that do not
     *  have zero-argument constructors.
     *  @return An empty graph.
     */
    protected Graph _emptyGraph() {
        Graph graph = null;

        try {
            graph = (Graph) (getClass().newInstance());
        } catch (Exception exception) {
            throw new GraphConstructionException("Could not create an "
                    + "empty graph from this one.\n" + exception + "\n"
                    + GraphException.graphDump(this));
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
                Analysis analysis = (Analysis) (analyses.next());

                if (analysis.analyzer() instanceof CachedStrategy) {
                    ((CachedStrategy) analysis.analyzer()).reset();
                }
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
     *  @exception GraphWeightException If the weight of the given edge is
     *  not valid, as determined by {@link #validEdgeWeight(Object)}.
     *  @see #_registerNode(Node)
     */
    protected void _registerEdge(Edge edge) {
        Object weight = edge.hasWeight() ? edge.getWeight() : null;

        if (!validEdgeWeight(weight)) {
            throw new GraphWeightException(weight, edge, this,
                    "Invalid edge weight.");
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
     *  @exception GraphWeightException If the weight of the given node is
     *  not valid, as determined by {@link #validNodeWeight(Object)}.
     *  @see #_registerEdge(Edge)
     */
    protected void _registerNode(Node node) {
        Object weight = node.hasWeight() ? node.getWeight() : null;

        if (!validNodeWeight(weight)) {
            throw new GraphWeightException(weight, node, this,
                    "Invalid node weight.");
        }

        _nodes.add(node);
        _incidentEdgeMap.put(node, new ArrayList());
        _nodes.registerWeight(node);
        _registerChange();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Given two node weights w1 and w2, add all edges of the form
    // (x1, x2), where
    //     (x1.getWeight() == w1) && (x2.getWeight() == w2).
    // The third parameter specifies whether the edges are to be
    // weighted, and the fourth parameter is the weight that is
    // to be applied if the edges are weighted.
    // The method returns one of the edges that is added.
    // The method returns an iterator over the edges that were added;
    // each element of this iterator is an instance of Edge.
    // The method throws an GraphConstructionException if no edge is
    // added (i.e., if no nodes x1, x2 satisfy the above condition.
    // The method throws a NullPointerException if w1 or w2 is null.
    private Collection _addEdges(Object weight1, Object weight2,
            boolean weighted, Object weight) {
        if (weight1 == null) {
            throw new NullPointerException("Null source node weight");
        } else if (weight2 == null) {
            throw new NullPointerException("Null sink node weight");
        }

        Iterator nodes1 = nodes(weight1).iterator();
        Edge newEdge = null;
        ArrayList newEdges = new ArrayList();

        while (nodes1.hasNext()) {
            Node node1 = (Node) (nodes1.next());
            Iterator nodes2 = nodes(weight2).iterator();

            while (nodes2.hasNext()) {
                newEdge = _addEdge(node1, (Node) (nodes2.next()), weighted,
                        weight);
                newEdges.add(newEdge);
            }
        }

        if (newEdges.isEmpty()) {
            throw new GraphConstructionException("No edge can be added based "
                    + "on the specified source and sink node weights.\n"
                    + "Weight1:\n" + weight1 + "\nWeight2:\n" + weight2 + "\n"
                    + GraphException.graphDump(this));
        } else {
            return newEdges;
        }
    }

    // Return the list of incident edges for a specified node.
    // Return null if the specified node is not in the graph.
    private ArrayList _incidentEdgeList(Node node) {
        return (ArrayList) _incidentEdgeMap.get(node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A list of analyses that are associated with this graph. Each
    // element of the list is an instance of ptolemy.graph.analysis.Analysis.
    private ArrayList _analysisList;

    // A counter that keeps track of changes to the graph.
    private long _changeCount;

    // The list of edges in this graph.
    // Each element of this list is an Edge.
    private ElementList _edges;

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

    // The list of nodes in this graph.
    // Each element of this list is a Node.
    private ElementList _nodes;

    // The analysis for computation of self loop edges.
    private SelfLoopAnalysis _selfLoopAnalysis;
}
