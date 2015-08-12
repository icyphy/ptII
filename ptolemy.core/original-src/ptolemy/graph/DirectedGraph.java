/* A directed graph and some graph algorithms.

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.graph.analysis.CycleExistenceAnalysis;
import ptolemy.graph.analysis.SinkNodeAnalysis;
import ptolemy.graph.analysis.SourceNodeAnalysis;
import ptolemy.graph.analysis.TransitiveClosureAnalysis;

///////////////////////////////////////////////////////////////////
//// DirectedGraph

/**
 A directed graph.
 Some methods in this class have two versions, one that operates
 on graph nodes, and another that operates on
 node weights. The latter form is called the <i>weights version</i>.
 More specifically, the weights version of an operation takes individual
 node weights or arrays of weights as arguments, and, when applicable, returns
 individual weights or arrays of weights.

 <p> Multiple edges in a graph can be directed between the same pair of nodes.
 Thus, directed multigraphs are supported.

 @author Yuhong Xiong, Jie Liu, Paul Whitaker, Shuvra S. Bhattacharyya,
 Shahrooz Shahparnia
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (pwhitake)
 @Pt.AcceptedRating Yellow (pwhitake)
 */
public class DirectedGraph extends Graph {
    /** Construct an empty directed graph.
     */
    public DirectedGraph() {
        super();
        _inputEdgeMap = new HashMap();
        _outputEdgeMap = new HashMap();
    }

    /** Construct an empty directed graph with enough storage allocated
     *  for the specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param nodeCount The integer specifying the number of nodes
     */
    public DirectedGraph(int nodeCount) {
        super(nodeCount);
        _inputEdgeMap = new HashMap(nodeCount);
        _outputEdgeMap = new HashMap(nodeCount);
    }

    /** Construct an empty directed graph with enough storage allocated for the
     *  specified number of edges, and number of nodes.  Memory
     *  management is more efficient with this constructor if the
     *  number of nodes and edges is known.
     *  @param nodeCount The number of nodes.
     *  @param edgeCount The number of edges.
     */
    public DirectedGraph(int nodeCount, int edgeCount) {
        super(nodeCount, edgeCount);
        _inputEdgeMap = new HashMap(nodeCount);
        _outputEdgeMap = new HashMap(nodeCount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Find all the nodes that can be reached backward from the
     *  specified node.
     *  The reachable nodes do not include the argument unless
     *  there is a loop from the specified node back to itself.
     *  @param node A node in this graph.
     *  @return The collection of nodes that is backward-reachable from the
     *  specified node; each element is a {@link Node}.
     *  @exception GraphElementException If the specified node is
     *  not a node in this graph.
     */
    public Collection backwardReachableNodes(Node node) {
        boolean[][] transitiveClosure = transitiveClosure();

        int nodeLabel = nodeLabel(node);
        ArrayList nodes = new ArrayList(transitiveClosure.length);

        // Look at the corresponding column.
        Iterator graphNodes = nodes().iterator();

        while (graphNodes.hasNext()) {
            Node next = (Node) graphNodes.next();

            if (transitiveClosure[nodeLabel(next)][nodeLabel]) {
                nodes.add(next);
            }
        }

        return nodes;
    }

    /** Find all the nodes that can be reached backward from the
     *  specified node (weights version).
     *  If the specified weight
     *  is null, find all the nodes that can be reached backward from any node
     *  that is unweighted.
     *  The reachable nodes do not include the argument unless
     *  there is a loop from the specified node back to itself.
     *  @param weight A node weight in this graph.
     *  @return An array of node weights that are backward-reachable from the
     *  nodes that have the specified weight; each element is an
     *  {@link Object}.
     *  @exception GraphWeightException If the specified weight is
     *  not a node weight in this graph.
     */
    public Object[] backwardReachableNodes(Object weight) {
        Collection sameWeightNodes = nodes(weight);

        if (sameWeightNodes.size() == 0) {
            throw new GraphWeightException(weight, null, this,
                    "The specified weight is not a "
                            + "node weight in this graph.");
        }

        return weightArray(backwardReachableNodes(sameWeightNodes));
    }

    /** Find all the nodes that can be reached backward from the
     *  specified collection of nodes.
     *  The reachable nodes do not include the specific ones unless
     *  there is a loop from the specified node back to itself.
     *  @param nodeCollection A collection of nodes in this graph;
     *  each element is a {@link Node}.
     *  @return The collection of nodes that are backward-reachable from
     *  the specified nodes; each element is a {@link Node}.
     */
    public Collection backwardReachableNodes(Collection nodeCollection) {
        boolean[][] transitiveClosure = transitiveClosure();
        ArrayList reachableNodes = new ArrayList(transitiveClosure.length);

        // Compute the OR of the corresponding rows.
        Iterator graphNodes = nodes().iterator();

        while (graphNodes.hasNext()) {
            Node nextGraphNode = (Node) graphNodes.next();
            int nextLabel = nodeLabel(nextGraphNode);
            boolean reachable = false;
            Iterator nodes = nodeCollection.iterator();

            while (nodes.hasNext()) {
                Node nextNode = (Node) nodes.next();

                if (transitiveClosure[nextLabel][nodeLabel(nextNode)]) {
                    reachable = true;
                    break;
                }
            }

            if (reachable) {
                reachableNodes.add(nextGraphNode);
            }
        }

        return reachableNodes;
    }

    /** Find all the nodes that can be reached backward from the
     *  specified collection of nodes (weights version).
     *  The reachable nodes do not include the weights in the argument unless
     *  there is a loop from the specified node back to itself.
     *  @param weights An array of node weights in this graph; each
     *  element is an {@link Object}.
     *  @return An array of node weights that are backward-reachable from the
     *  nodes that have the specified weights; each element is an
     *  {@link Object}.
     *  @exception GraphElementException If the one or more of the specified
     *  weights is not a node weight in this graph.
     */
    public Object[] backwardReachableNodes(Object[] weights) {
        return weightArray(backwardReachableNodes(nodes(Arrays.asList(weights))));
    }

    /** Return the nodes that are in cycles. If there are multiple cycles,
     *  the nodes in all the cycles will be returned.
     *  @return The collection of nodes that are in cycles; each element
     *  is a {@link Node}.
     */
    public Collection cycleNodeCollection() {
        boolean[][] transitiveClosure = transitiveClosure();

        ArrayList result = new ArrayList(transitiveClosure.length);
        Iterator nodes = nodes().iterator();

        while (nodes.hasNext()) {
            Node next = (Node) nodes.next();
            int label = nodeLabel(next);

            if (transitiveClosure[label][label]) {
                result.add(next);
            }
        }

        return result;
    }

    /** Return the nodes that are in cycles (weights version).
     *  If there are multiple cycles,
     *  the nodes in all the cycles will be returned.
     *  @return An array of node weights that are in cycles; each element
     *  is an {@link Object}.
     */
    public Object[] cycleNodes() {
        return weightArray(cycleNodeCollection());
    }

    /** Test if an edge exists from one node to another.
     *  @param node1 The weight of the first node.
     *  @param node2 The weight of the second node.
     *  @return True if the graph includes an edge from the first node to
     *  the second node; false otherwise.
     */
    public boolean edgeExists(Node node1, Node node2) {
        Iterator outputEdges = outputEdges(node1).iterator();

        while (outputEdges.hasNext()) {
            if (((Edge) outputEdges.next()).sink() == node2) {
                return true;
            }
        }

        return false;
    }

    /** Test whether an edge exists from one node weight to another.
     *  More specifically, test whether there exists an edge (n1, n2)
     *  such that
     *
     *  <p><code>
     *      (n1.getWeight() == weight1) &amp;&amp; (n2.getWeight() == weight2)
     *  </code>.
     *
     *  @param weight1 The first (source) node weight.
     *  @param weight2 The second (sink) node weight.
     *  @return True if the graph includes an edge from the first node weight to
     *  the second node weight.
     */
    public boolean edgeExists(Object weight1, Object weight2) {
        Iterator sources = nodes(weight1).iterator();

        while (sources.hasNext()) {
            Node candidateSource = (Node) sources.next();
            Iterator sinks = nodes(weight2).iterator();

            while (sinks.hasNext()) {
                Node candidateSink = (Node) sinks.next();

                if (edgeExists(candidateSource, candidateSink)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Return the number of input edges of a specified node.
     *  @param node The node.
     *  @return The number of input edges.
     */
    public int inputEdgeCount(Node node) {
        return _inputEdgeList(node).size();
    }

    /** Return the collection of input edges for a specified node.
     *
     *  @param node The specified node.
     *  @return The collection of input edges; each element is an {@link Edge}.
     */
    public Collection inputEdges(Node node) {
        return Collections.unmodifiableList(_inputEdgeList(node));
    }

    /** Test if this graph is acyclic (is a DAG).
     *  @return True if the the graph is acyclic, or
     *  empty; false otherwise.
     */
    public boolean isAcyclic() {
        return !_acyclicAnalysis.hasCycle();
    }

    /** Return the number of output edges of a specified node.
     *  @param node The node.
     *  @return The number of output edges.
     */
    public int outputEdgeCount(Node node) {
        return _outputEdgeList(node).size();
    }

    /** Return the collection of output edges for a specified node.
     *
     *  @param node The specified node.
     *  @return The collection of output edges; each element is an {@link Edge}.
     */
    public Collection outputEdges(Node node) {
        return Collections.unmodifiableList(_outputEdgeList(node));
    }

    /** Return the collection of edges that make a node n2 a predecessor of a
     *  node n1. In other words, return the collection of edges directed from
     *  n2 to n1. Each element of the collection is an {@link Edge}.
     *  @param n1 The node n1.
     *  @param n2 The node n2.
     *  @return The collection of edges that make n2 a predecessor of n1.
     *  @see DirectedGraph#successorEdges(Node, Node)
     *  @see Graph#neighborEdges(Node, Node)
     */
    public Collection predecessorEdges(Node n1, Node n2) {
        Collection edgeCollection = this.outputEdges(n2);
        Iterator edges = edgeCollection.iterator();
        ArrayList commonEdges = new ArrayList();

        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();

            if (edge.sink() == n1) {
                commonEdges.add(edge);
            }
        }

        return commonEdges;
    }

    /** Return all of the predecessors of a given node in the form of a
     *  a collection. Each element of the collection is a Node.
     *  A <i>predecessor</i> of a node X is a node that is the source
     *  of an edge whose sink is X. All elements in the returned collection
     *  are unique nodes.
     *  @param node The node whose predecessors are to be returned.
     *  @return The predecessors of the node.
     */
    public Collection predecessors(Node node) {
        Collection inputEdgeCollection = inputEdges(node);
        Iterator inputEdges = inputEdgeCollection.iterator();
        ArrayList result = new ArrayList(inputEdgeCollection.size());

        while (inputEdges.hasNext()) {
            Node source = ((Edge) inputEdges.next()).source();

            if (!result.contains(source)) {
                result.add(source);
            }
        }

        return result;
    }

    /** Find all the nodes that can be reached from the specified node.
     *  The reachable nodes do not include the specific one unless
     *  there is a loop from the specified node back to itself.
     *  @param node The specified node.
     *  @return The collection of nodes reachable from the specified one;
     *  each element is a {@link Node}.
     *  @exception GraphElementException If the specified node is
     *  not a node in this graph.
     */
    public Collection reachableNodes(Node node) {
        boolean[][] transitiveClosure = transitiveClosure();
        int label = nodeLabel(node);
        ArrayList result = new ArrayList(transitiveClosure.length);
        Iterator nodes = nodes().iterator();

        while (nodes.hasNext()) {
            Node next = (Node) nodes.next();

            if (transitiveClosure[label][nodeLabel(next)]) {
                result.add(next);
            }
        }

        return result;
    }

    /** Find all the nodes that can be reached from any node that has the
     *  specified node weight (weights version). If the specified weight
     *  is null, find all the nodes that can be reached from any node
     *  that is unweighted.
     *  @param weight The specified node weight.
     *  @return An array of node weights reachable from the specified weight;
     *  each element is an {@link Object}.
     *  @exception GraphWeightException If the specified node weight is
     *  not a node weight in this graph.
     *  @see #reachableNodes(Node)
     */
    public Object[] reachableNodes(Object weight) {
        Collection sameWeightNodes = nodes(weight);

        if (sameWeightNodes.size() == 0) {
            throw new GraphWeightException(weight, null, this,
                    "The specified weight is not a node weight in this graph.");
        }

        return weightArray(reachableNodes(sameWeightNodes));
    }

    /** Find all the nodes that can be reached from the specified collection
     *  of nodes (weights version). The reachable nodes do not include a
     *  specified one unless there is a loop from the specified node back to
     *  itself.
     *  @param weights An array of node weights; each element is an
     *  {@link Object}.
     *  @return The array of nodes that are reachable from
     *  the specified one; each element is an {@link Object}.
     *  @see #reachableNodes(Node)
     */
    public Object[] reachableNodes(Object[] weights) {
        return weightArray(reachableNodes(nodes(Arrays.asList(weights))));
    }

    /** Find all the nodes that can be reached from the specified collection
     *  of nodes. The reachable nodes do not include a specified one unless
     *  there is a loop from the specified node back to itself.
     *  @param nodeCollection The specified collection of nodes;
     *  each element is a {@link Node}.
     *  @return The collection of nodes that are reachable from
     *  the specified one; each element is a {@link Node}.
     */
    public Collection reachableNodes(Collection nodeCollection) {
        boolean[][] transitiveClosure = transitiveClosure();

        int N = nodeCollection.size();
        int[] labels = new int[N];
        Iterator nodes = nodeCollection.iterator();

        for (int i = 0; i < N; i++) {
            labels[i] = nodeLabel((Node) nodes.next());
        }

        ArrayList reachableNodes = new ArrayList(transitiveClosure.length);

        // Compute the OR of the corresponding rows.
        Iterator graphNodes = nodes().iterator();

        while (graphNodes.hasNext()) {
            Node nextGraphNode = (Node) graphNodes.next();
            int nextGraphLabel = nodeLabel(nextGraphNode);
            boolean reachable = false;

            for (int i = 0; i < N; i++) {
                if (transitiveClosure[labels[i]][nextGraphLabel]) {
                    reachable = true;
                    break;
                }
            }

            if (reachable) {
                reachableNodes.add(nextGraphNode);
            }
        }

        return reachableNodes;
    }

    /** Compute the strongly connected component (SCC) decomposition of a graph.
     *  @return An array of instances of DirectedGraph that represent
     *  the SCCs of the graph in topological order.
     */
    public DirectedGraph[] sccDecomposition() {
        boolean[][] transitiveClosure = transitiveClosure();

        int N = nodeCount();

        if (transitiveClosure.length != N) {
            throw new GraphStateException("Graph inconsistency."
                    + " A dump of the graph follows.\n" + this);
        }

        // initially, no nodes have been added to an SCC
        boolean[] addedToAnSCC = new boolean[N];

        for (int i = 0; i < N; i++) {
            addedToAnSCC[i] = false;
        }

        // Each element in each of these array lists is a Node.
        ArrayList sccNodeLists = new ArrayList();
        ArrayList sccRepresentatives = new ArrayList();

        for (int i = 0; i < N; i++) {
            // given a node, if that node is not part of an SCC, assign
            // it to a new SCC
            if (!addedToAnSCC[i]) {
                ArrayList nodeList = new ArrayList();
                sccNodeLists.add(nodeList);

                Node node = node(i);
                nodeList.add(node);
                sccRepresentatives.add(node);
                addedToAnSCC[i] = true;

                for (int j = i + 1; j < N; j++) {
                    // given two nodes, the two are in the same SCC if they
                    // are mutually reachable
                    if (!addedToAnSCC[j]) {
                        if (transitiveClosure[i][j] && transitiveClosure[j][i]) {
                            nodeList.add(node(j));
                            addedToAnSCC[j] = true;
                        }
                    }
                }
            }
        }

        int numberOfSCCs = sccNodeLists.size();
        Collection sortedSCCRepresentatives;

        try {
            sortedSCCRepresentatives = topologicalSort(sccRepresentatives);
        } catch (GraphActionException ex) {
            throw new GraphStateException("nodes in different SCCs were"
                    + " found to be strongly connected.");
        }

        ArrayList sortedSCCNodeLists = new ArrayList();

        Iterator representatives = sortedSCCRepresentatives.iterator();

        for (int i = 0; i < numberOfSCCs; i++) {
            Node sccRepresentative = (Node) representatives.next();

            for (int j = 0; j < numberOfSCCs; j++) {
                ArrayList nodeList = (ArrayList) sccNodeLists.get(j);

                if (nodeList.get(0) == sccRepresentative) {
                    sortedSCCNodeLists.add(nodeList);
                }
            }
        }

        DirectedGraph[] sccs = new DirectedGraph[numberOfSCCs];

        for (int i = 0; i < numberOfSCCs; i++) {
            ArrayList nodeList = (ArrayList) sortedSCCNodeLists.get(i);
            sccs[i] = (DirectedGraph) subgraph(nodeList);
        }

        return sccs;
    }

    /** Return the number of self loop edges of a specified node.
     *  A directed self loop edge (an edge whose source and sink nodes are
     *  identical) is both an input edge and an output
     *  edge of the incident node, but it is not duplicated in the set of
     *  incident edges. Thus, the number of edges incident edges
     *  to a node is equal to
     *  <i>I + O - S</i>, where <i>I</i> is the number of input edges,
     *  <i>O</i> is the number of output edges, and <i>S</i> is the number
     *  of self loop edges.
     *  @param node The node.
     *  @return The number of self loop edges.
     */
    @Override
    public int selfLoopEdgeCount(Node node) {
        // This can be determined more efficiently for directed
        // graphs, so we override the method from the base class.
        // A self loop edge appears in both the input and output edge lists.
        // Thus, the number of self loop edges is simply the total number
        // of input and output edges minus the number of edges that
        // are connected to this node.
        return inputEdgeCount(node) + outputEdgeCount(node)
                - incidentEdgeCount(node);
    }

    /** Return the number of sink nodes in this graph.
     *  A <i>sink node</i> is a node that has no output edges.
     *  @return The number of sink nodes.
     */
    public int sinkNodeCount() {
        return sinkNodes().size();
    }

    /** Return all the sink nodes in this graph in the form of a collection.
     *  Each element in the collection is a {@link Node}.
     *  @return The sink nodes in this graph.
     *  @see #sinkNodeCount()
     */
    public Collection sinkNodes() {
        return _sinkNodeAnalysis.nodes();
    }

    /** Return the number of source nodes in this graph.
     *  A <i>source node</i> is a node that has no input edges.
     *  @return The number of source nodes.
     */
    public int sourceNodeCount() {
        return sourceNodes().size();
    }

    /** Return all the source nodes in this graph in the form of a collection.
     *  Each element in the collection is a {@link Node}.
     *  @return The source nodes in this graph.
     *  @see #sourceNodeCount()
     */
    public Collection sourceNodes() {
        return _sourceNodeAnalysis.nodes();
    }

    /** Return a list of disconnected subgraphs of this graph.
     *  @return A list of disconnected subgraphs.
     */
    public LinkedList subgraphs() {
        LinkedList subgraphList = new LinkedList();
        LinkedList remainingNodes = new LinkedList(nodes());

        while (!remainingNodes.isEmpty()) {
            DirectedGraph subgraph = new DirectedGraph();
            Node node = (Node) remainingNodes.remove(0);
            _connectedSubGraph(node, subgraph, remainingNodes);
            subgraphList.add(subgraph);
        }

        return subgraphList;
    }

    /** Return the collection of edges that make a node n2 a successor of a
     *  node n1. In other words, return the collection of edges directed
     *  from n1 to n2.
     *  Each element of the collection is an {@link Edge}.
     *  @param n1 The node n1.
     *  @param n2 The node n2.
     *  @return The collection of edges that make n2 a successor of n1.
     *  @see DirectedGraph#predecessorEdges(Node, Node)
     *  @see Graph#neighborEdges(Node, Node)
     */
    public Collection successorEdges(Node n1, Node n2) {
        return predecessorEdges(n2, n1);
    }

    /** Return all of the successors of a given node in the form of a
     *  a collection. Each element of the collection is a {@link Node}.
     *  A <i>successor</i> of a node X is a node that is the sink
     *  of an edge whose source is X. All elements in the returned collection
     *  are unique nodes.
     *  @param node The node whose successors are to be returned.
     *  @return The successors of the node.
     */
    public Collection successors(Node node) {
        Collection outputEdgeCollection = outputEdges(node);
        Iterator outputEdges = outputEdgeCollection.iterator();
        ArrayList result = new ArrayList(outputEdgeCollection.size());

        while (outputEdges.hasNext()) {
            Node sink = ((Edge) outputEdges.next()).sink();

            if (!result.contains(sink)) {
                result.add(sink);
            }
        }

        return result;
    }

    /** Return an acyclic graph if this graph is acyclic.
     *
     *  @return An acyclic graph in the form of
     *          {@link DirectedAcyclicGraph}.
     *  @exception GraphException If the graph is cyclic.
     */
    public DirectedAcyclicGraph toDirectedAcyclicGraph() {
        DirectedAcyclicGraph acyclicGraph;

        if (isAcyclic()) {
            acyclicGraph = (DirectedAcyclicGraph) cloneAs(new DirectedAcyclicGraph());
        } else {
            throw new GraphTopologyException("This graph is not acyclic."
                    + GraphException.graphDump(this));
        }

        return acyclicGraph;
    }

    /** Sort a collection of graph nodes in their topological order as long as
     *  no two of the given nodes are mutually reachable by each other.
     *  This method uses the transitive closure matrix. Since generally
     *  the graph is checked for cyclicity before this method is
     *  called, the use of the transitive closure matrix should
     *  not add any overhead. A bubble sort is used for the internal
     *  implementation, so the complexity is <i>O(V^2)</i>.
     *  @param nodeCollection The collection of nodes to be sorted;
     *  each element is a {@link Node}.
     *  @return The nodes in their sorted order in the form of a list;
     *  each element is a {@link Node}.
     *  @exception GraphActionException If any two nodes are strongly
     *  connected.
     *  @see #topologicalSort(Object[])
     */
    public List topologicalSort(Collection nodeCollection)
            throws GraphActionException {
        boolean[][] transitiveClosure = transitiveClosure();

        int N = nodeCollection.size();
        Node[] nodeArray = new Node[N];
        Iterator nodes = nodeCollection.iterator();
        int i = 0;

        while (nodes.hasNext()) {
            nodeArray[i++] = (Node) nodes.next();
        }

        for (i = 0; i < N - 1; i++) {
            for (int j = i + 1; j < N; j++) {
                int label1 = nodeLabel(nodeArray[i]);
                int label2 = nodeLabel(nodeArray[j]);

                if (transitiveClosure[label2][label1]) {
                    if (transitiveClosure[label1][label2]) {
                        throw new GraphActionException("Attempted to"
                                + " topologically sort cyclic nodes.");
                    } else {
                        // Swap nodes
                        Node node = nodeArray[i];
                        nodeArray[i] = nodeArray[j];
                        nodeArray[j] = node;
                    }
                }
            }
        }

        return new ArrayList(Arrays.asList(nodeArray));
    }

    /** Sort the given nodes in their topological order as long as
     *  no two of the given nodes are mutually reachable by each other
     *  (weights version).
     *  The set of nodes to sort is taken as the set of nodes whose
     *  weights are contained in the specified weight set.
     *  The weights of the sorted nodes are returned.
     *  @param weights The weight set.
     *  @return The weights of the sorted nodes.
     *  @exception GraphActionException If any two nodes are strongly
     *   connected.
     *  @see #topologicalSort(Collection)
     */
    public Object[] topologicalSort(Object[] weights)
            throws GraphActionException {
        return weightArray(topologicalSort(nodes(Arrays.asList(weights))));
    }

    /** Return transitive closure for the graph.
     *
     *  @return Transitive closure for the graph.
     */
    public boolean[][] transitiveClosure() {
        return _transitiveClosureAnalysis.transitiveClosureMatrix();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Connect an edge to a node by appropriately modifying
     * the adjacency information associated with the node.
     * @param edge The edge.
     * @param node The node.
     * @exception GraphConstructionException If the edge has already
     * been connected to the node.
     */
    @Override
    protected void _connect(Edge edge, Node node) {
        super._connect(edge, node);

        if (edge.source() == node) {
            _outputEdgeList(node).add(edge);
        }

        if (edge.sink() == node) {
            _inputEdgeList(node).add(edge);
        }
    }

    /** Given a node, get all the edges and nodes that are connected
     *  to it directly and/or indirectly. Add them in the given graph.
     *  Remove the nodes from the remaining nodes.
     *  FIXME: Hidden edges not considered.
     * @param node The given node.
     * @param graph The given graph.
     * @param remainingNodes Set of nodes that haven't been reached.
     */
    protected void _connectedSubGraph(Node node, DirectedGraph graph,
            Collection remainingNodes) {
        if (!graph.containsNode(node)) {
            graph.addNode(node);
            remainingNodes.remove(node);
        }

        // Handle source nodes.
        Iterator inputEdges = inputEdges(node).iterator();

        while (inputEdges.hasNext()) {
            Edge inputEdge = (Edge) inputEdges.next();

            if (!graph.containsEdge(inputEdge)) {
                Node sourceNode = inputEdge.source();

                if (!graph.containsNode(sourceNode)) {
                    graph.addNode(sourceNode);
                    _connectedSubGraph(sourceNode, graph, remainingNodes);
                    remainingNodes.remove(sourceNode);
                }

                if (!graph.containsEdge(inputEdge)) {
                    graph.addEdge(sourceNode, node);
                }
            }
        }

        // Handle sink nodes.
        Iterator outputEdges = outputEdges(node).iterator();

        while (outputEdges.hasNext()) {
            Edge outputEdge = (Edge) outputEdges.next();

            if (!graph.containsEdge(outputEdge)) {
                Node sinkNode = outputEdge.sink();

                if (!graph.containsNode(sinkNode)) {
                    graph.addNode(sinkNode);
                    _connectedSubGraph(sinkNode, graph, remainingNodes);
                    remainingNodes.remove(sinkNode);
                }

                if (!graph.containsEdge(outputEdge)) {
                    graph.addEdge(node, sinkNode);
                }
            }
        }
    }

    /* Disconnect an edge from a node that it is incident to by modifying
     * the adjacency information (incident, input, and output edge sets)
     * that is associated with the node in this graph.
     * Do nothing if the edge is not incident to the node.
     *  @param edge The edge.
     *  @param node The node.
     */
    @Override
    protected void _disconnect(Edge edge, Node node) {
        super._disconnect(edge, node);
        _removeIfPresent(_inputEdgeList(node), edge);
        _removeIfPresent(_outputEdgeList(node), edge);
    }

    /** Initialize the list of analyses that are associated with this graph,
     *  and initialize the change counter of the graph.
     *  @see ptolemy.graph.analysis.Analysis
     */
    @Override
    protected void _initializeAnalyses() {
        super._initializeAnalyses();
        _transitiveClosureAnalysis = new TransitiveClosureAnalysis(this);
        _acyclicAnalysis = new CycleExistenceAnalysis(this);
        _sinkNodeAnalysis = new SinkNodeAnalysis(this);
        _sourceNodeAnalysis = new SourceNodeAnalysis(this);
    }

    /** Register a new node in the graph.
     *  @param node The new node.
     */
    @Override
    protected void _registerNode(Node node) {
        super._registerNode(node);
        _inputEdgeMap.put(node, new ArrayList());
        _outputEdgeMap.put(node, new ArrayList());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The graph analysis for computation of transitive closure. */
    protected TransitiveClosureAnalysis _transitiveClosureAnalysis;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Return the list of input edges for a specified node. */
    private ArrayList _inputEdgeList(Node node) {
        return (ArrayList) _inputEdgeMap.get(node);
    }

    /** Return the list of output edges for a specified node. */
    private ArrayList _outputEdgeList(Node node) {
        return (ArrayList) _outputEdgeMap.get(node);
    }

    /** Remove an object from an ArrayList if it exists in the list. */
    private void _removeIfPresent(ArrayList list, Object element) {
        int index;

        if ((index = list.indexOf(element)) != -1) {
            list.remove(index);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A mapping from nodes into their lists of input edges.
     * Each key in this map is an instance of Node. Each value
     * is an instance of ArrayList whose elements are instances of Edge.
     * This redundant information is maintained for improved
     * run-time efficiency when handing undirected graphs, or when operating
     * on directed graphs in ways for which edge orientation is not relevant.
     */
    private HashMap _inputEdgeMap;

    /** A mapping from nodes into their lists of output edges.
     * Each key in this map is an instance of Node. Each value
     * is an instance of ArrayList whose elements are instances of Edge.
     */
    private HashMap _outputEdgeMap;

    /** The graph analysis for computation of acyclic property. */
    private CycleExistenceAnalysis _acyclicAnalysis;

    /** The graph analysis for computation of sink nodes. */
    private SinkNodeAnalysis _sinkNodeAnalysis;

    /** The graph analysis for computation of source nodes. */
    private SourceNodeAnalysis _sourceNodeAnalysis;
}
