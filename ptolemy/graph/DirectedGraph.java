/* A directed graph and some graph algorithms.

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

@ProposedRating Yellow (pwhitake@eecs.berkeley.edu)
@AcceptedRating Yellow (pwhitake@eecs.berkeley.edu)

*/

package ptolemy.graph;

import ptolemy.graph.analysis.Analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
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

    /** Sort a collection of graph nodes in their topological order as long as
     *  no two of the given nodes are mutually reachable by each other.
     *  This method uses the transitive closure matrix. Since generally
     *  the graph is checked for cyclicity before this method is
     *  called, the use of the transitive closure matrix should
     *  not add any overhead. A bubble sort is used for the internal
     *  implementation, so the complexity is <i>O(V^2)</i>.
     *  @param nodes The collection of nodes to be sorted; each element is
     *  a {@link Node}.
     *  @return The nodes in their sorted order in the form of a collection;
     *  each element is a {@link Node}.
     *  @exception IllegalActionException If any two nodes are strongly
     *  connected.
     *  @see #attemptTopologicalSort(Object[])
     */
    public Collection attemptTopologicalSort(Collection nodeCollection)
            throws IllegalActionException {
        _computeTransitiveClosure();

        int N = nodeCollection.size();
        Node[] nodeArray = new Node[N];
        Iterator nodes = nodeCollection.iterator();
        int i = 0;
        while (nodes.hasNext()) {
            nodeArray[i++] = (Node)(nodes.next());
        }
        for (i = 0; i < N-1; i++) {
            for (int j = i+1; j < N; j++) {
                int label1 = nodeLabel(nodeArray[i]);
                int label2 = nodeLabel(nodeArray[j]);
                if (_transitiveClosure[label2][label1]) {
                    if (_transitiveClosure[label1][label2]) {
                        throw new IllegalActionException("Attempted to"
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
     *  @exception IllegalActionException If any two nodes are strongly
     *   connected.
     *  @see #attemptTopologicalSort(Collection)
     */
    public Object[] attemptTopologicalSort(Object[] weights) throws
            IllegalActionException {
        return weightArray(attemptTopologicalSort(
                nodes(Arrays.asList(weights))));
    }

    /** Find all the nodes that can be reached backward from the
     *  specified node.
     *  The reachable nodes do not include the argument unless
     *  there is a loop from the specified node back to itself.
     *  @param node A node in this graph.
     *  @return The collection of nodes that is backward-reachable from the
     *  specified node; each element is a {@link Node}.
     *  @exception IllegalArgumentException If the specified node is
     *  not a node in this graph.
     */
    public Collection backwardReachableNodes(Node node) {
        _computeTransitiveClosure();

        int nodeLabel = nodeLabel(node);
        ArrayList nodes = new ArrayList(_transitiveClosure.length);
        // Look at the corresponding column.
        Iterator graphNodes = nodes().iterator();
        while (graphNodes.hasNext()) {
            Node next = (Node)(graphNodes.next());
            if (_transitiveClosure[nodeLabel(next)][nodeLabel]) {
                nodes.add(next);
            }
        }
        return nodes;
    }

    /** Find all the nodes that can be reached backward from the
     *  specified node (weights version).
     *  The reachable nodes do not include the argument unless
     *  there is a loop from the specified node back to itself.
     *  @param node A node weight in this graph.
     *  @return An array of node weights that are backward-reachable from the
     *  nodes that have the specified weight; each element is an {@link Object}.
     *  @exception IllegalArgumentException If the specified weight is
     *  not a node weight in this graph.
     */
    public Object[] backwardReachableNodes(Object weight) {
        return weightArray(backwardReachableNodes(nodes(weight)));
    }

    /** Find all the nodes that can be reached backward from the
     *  specified collection of nodes.
     *  The reachable nodes do not include the specific ones unless
     *  there is a loop from the specified node back to itself.
     *  @param nodes A collection of nodes in this graph; each element is
     *  a {@link Node}.
     *  @return The collection of nodes that are backward-reachable from
     *  the specified nodes; each element is a {@link Node}.
     */
    public Collection backwardReachableNodes(Collection nodeCollection) {
        _computeTransitiveClosure();
        ArrayList reachableNodes = new ArrayList(_transitiveClosure.length);
        // Compute the OR of the corresponding rows.
        Iterator graphNodes = nodes().iterator();
        while (graphNodes.hasNext()) {
            Node nextGraphNode = (Node)graphNodes.next();
            int nextLabel = nodeLabel(nextGraphNode);
            boolean reachable = false;
            Iterator nodes = nodeCollection.iterator();
            while (nodes.hasNext()) {
                Node nextNode = (Node)nodes.next();
                if (_transitiveClosure[nextLabel][nodeLabel(nextNode)]) {
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
     *  @exception IllegalArgumentException If the one or more of the specified
     *  weights is not a node weight in this graph.
     */
    public Object[] backwardReachableNodes(Object[] weights) {
        return weightArray(backwardReachableNodes(
                nodes(Arrays.asList(weights))));
    }

    /** Return the nodes that are in cycles. If there are multiple cycles,
     *  the nodes in all the cycles will be returned.
     *  @return The collection of nodes that are in cycles; each element
     *  is a {@link Node}.
     */
    public Collection cycleNodeCollection() {
        _computeTransitiveClosure();

        ArrayList result = new ArrayList(_transitiveClosure.length);
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            Node next = (Node)nodes.next();
            int label = nodeLabel(next);
            if (_transitiveClosure[label][label]) {
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
            if (((Edge)(outputEdges.next())).sink() == node2) {
                return true;
            }
        }
        return false;
    }

    /** Test whether an edge exists from one node weight to another.
     *  More specifically, test whether there exists an edge (n1, n2)
     *  such that
     *
     *  <p> <code> (n1.weight() == weight1) && (n2.weight() == weight2) </code>.
     *  @param weight1 The first (source) node weight.
     *  @param weight2 The second (sink) node weight.
     *  @return True if the graph includes an edge from the first node weight to
     *  the second node weight.
     */
    public boolean edgeExists(Object weight1, Object weight2) {
        Iterator sources = nodes(weight1).iterator();
        while (sources.hasNext()) {
            Node candidateSource = (Node)sources.next();
            Iterator sinks = nodes(weight2).iterator();
            while (sinks.hasNext()) {
                Node candidateSink = (Node)sinks.next();
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
     *  The implementation computes the transitive closure of the
     *  graph, if it is not already computed after the last change to
     *  this graph.  So the first call to this method after a graph change
     *  may be slow, but all the subsequent calls return in constant
     *  time.
     *  @return True if the the graph is acyclic, or
     *  empty; false otherwise.
     */
    public boolean isAcyclic() {
        _computeTransitiveClosure();
        return _isAcyclic;
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
            Edge edge = (Edge)edges.next();
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
            Node source = ((Edge)(inputEdges.next())).source();
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
     *  @exception IllegalArgumentException If the specified node is
     *  not a node in this graph.
     */
    public Collection reachableNodes(Node node) {
        _computeTransitiveClosure();
        int label = nodeLabel(node);
        ArrayList result = new ArrayList(_transitiveClosure.length);
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            Node next = (Node)nodes.next();
            if (_transitiveClosure[label][nodeLabel(next)]) {
                result.add(next);
            }
        }
        return result;
    }

    /** Find all the nodes that can be reached from any node that has the
     *  specified node weight (weights version).
     *  @param node The specified node weight.
     *  @return An array of node weights reachable from the specified weight;
     *  each element is an {@link Object}.
     *  @exception IllegalArgumentException If the specified node weight is
     *  not a node weight in this graph.
     *  @see #reachableNodes(Node)
     */
    public Object[] reachableNodes(Object weight) {
        return weightArray(reachableNodes(nodes(weight)));
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
     *  @param nodes The specified collection of nodes; each element is
     *  a {@link Node}.
     *  @return The collection of nodes that are reachable from
     *  the specified one; each element is a {@link Node}.
     */
    public Collection reachableNodes(Collection nodeCollection) {
        _computeTransitiveClosure();

        int N = nodeCollection.size();
        int labels[] = new int[N];
        Iterator nodes = nodeCollection.iterator();
        for (int i = 0; i < N; i++) {
            labels[i] = nodeLabel((Node)nodes.next());
        }
        ArrayList reachableNodes = new ArrayList(_transitiveClosure.length);
        // Compute the OR of the corresponding rows.
        Iterator graphNodes = nodes().iterator();
        while (graphNodes.hasNext()) {
            Node nextGraphNode = (Node)graphNodes.next();
            int nextGraphLabel = nodeLabel(nextGraphNode);
            boolean reachable = false;
            for (int i = 0; i < N; i++) {
                if (_transitiveClosure[labels[i]][nextGraphLabel]) {
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
        _computeTransitiveClosure();

        int N = nodeCount();
        if (_transitiveClosure.length != N) {
            throw new InternalErrorException("Graph inconsistency. A dump"
                    + " of the graph follows.\n" + this);
        }

        // initially, no nodes have been added to an SCC
        boolean addedToAnSCC[] = new boolean[N];
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
                        if (_transitiveClosure[i][j] &&
                                _transitiveClosure[j][i]) {
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
            sortedSCCRepresentatives =
                attemptTopologicalSort(sccRepresentatives);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("nodes in different SCCs were"
                    + " found to be strongly connected.");
        }

        ArrayList sortedSCCNodeLists = new ArrayList();

        Iterator representatives = sortedSCCRepresentatives.iterator();
        for (int i = 0; i < numberOfSCCs; i++) {
            Node sccRepresentative = (Node)(representatives.next());
            for (int j = 0; j < numberOfSCCs; j++) {
                ArrayList nodeList = (ArrayList) (sccNodeLists.get(j));
                if (nodeList.get(0) == sccRepresentative) {
                    sortedSCCNodeLists.add(nodeList);
                }
            }
        }

        DirectedGraph sccs[] = new DirectedGraph[numberOfSCCs];
        for (int i = 0; i < numberOfSCCs; i++) {
            ArrayList nodeList = (ArrayList) (sortedSCCNodeLists.get(i));
            sccs[i] = (DirectedGraph)subgraph(nodeList);
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
    public int selfLoopEdgeCount(Node node) {
        // This can be determined more efficiently for directed
        // graphs, so we override the method from the base class.
        // A self loop edge appears in both the input and output edge lists.
        // Thus, the number of self loop edges is simply the total number
        // of input and output edges minus the number of edges that
        // are connected to this node.
        return inputEdgeCount(node) + outputEdgeCount(node) -
            incidentEdgeCount(node);
    }

    /** Return the number of sink nodes in this graph.
     *  A <i>sink node</i> is a node that has no output edges.
     *  @param node The node.
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
        if (_sinkNodeListener.obsolete()) {
            _sinkNodes = new ArrayList();
            Iterator nodes = nodes().iterator();
            while (nodes.hasNext()) {
                Node node = (Node)nodes.next();
                if (outputEdgeCount(node) == 0) {
                    _sinkNodes.add(node);
                }
            }
            _sinkNodeListener.registerComputation();
        }
        return Collections.unmodifiableList(_sinkNodes);
    }

    /** Return the number of source nodes in this graph.
     *  A <i>source node</i> is a node that has no input edges.
     *  @param node The node.
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
        if (_sourceNodeListener.obsolete()) {
            _sourceNodes = new ArrayList();
            Iterator nodes = nodes().iterator();
            while (nodes.hasNext()) {
                Node node = (Node)nodes.next();
                if (inputEdgeCount(node) == 0) {
                    _sourceNodes.add(node);
                }
            }
            _sourceNodeListener.registerComputation();
        }
        return Collections.unmodifiableList(_sourceNodes);
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
            Node sink = ((Edge)(outputEdges.next())).sink();
            if (!result.contains(sink)) {
                result.add(sink);
            }
        }
        return result;
    }

	/** Return an acyclic graph if this graph is acyclic.
	 *
	 *	@return An acyclic graph in the form of
	 *			{@link DirectedAcyclicGraph}.
	 *	@exception IllegalArgumentException This graph is not acyclic.
     *  FIXME: we need a better exception for this.
	 */
	public DirectedAcyclicGraph toDirectedAcyclicGraph() {
		DirectedAcyclicGraph acyclicGraph;
		if (isAcyclic()) {
			acyclicGraph = (DirectedAcyclicGraph)
					cloneAs(new DirectedAcyclicGraph());
		} else {
			throw new IllegalArgumentException("This graph is not acyclic");
		}
		return acyclicGraph;
	}

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compute the transitive closure. Puts the result in the
     *  boolean array _transitiveClosure. If this graph is empty,
     *  set the dimension of _transitiveClosure to be 0 by 0.
     *  The implementation uses Warshall's algorithm, which can be
     *  found in chapter 6 of "Discrete Mathematics and Its
     *  Applications," 3rd Ed., by K. H. Rosen.  The complexity
     *  of this algorithm is O(N^3), where N is the number of nodes.
     *  This method also checks if the graph is cyclic and stores
     *  the result in an internal flag.
     */
    protected void _computeTransitiveClosure() {
        if (_transitiveClosure != null) {
            return;
        }

        int size = nodeCount();

        // Initialize _transitiveClosure to the adjacency matrix
        _transitiveClosure = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                _transitiveClosure[i][j] = false;
            }
            Iterator outputEdges = outputEdges(node(i)).iterator();
            while (outputEdges.hasNext()) {
                _transitiveClosure[i][nodeLabel(((Edge)outputEdges.next())
                        .sink())] = true;
            }
        }

        // Warshall's algorithm
        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
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
		break;
            }
        }
    }

    /** Connect an edge to a node by appropriately modifying
     * the adjacency information associated with the node.
     * @param edge The edge.
     * @param node The node.
     * @exception IllegalArgumentException If the edge has already
     * been connected to the node.
     */
    protected void _connect(Edge edge, Node node) {
        super._connect(edge, node);
        if (edge.source() == node) {
            _outputEdgeList(node).add(edge);
        }
        if (edge.sink() == node) {
            _inputEdgeList(node).add(edge);
        }
    }

    /* Disconnect an edge from a node that it is incident to.
     * Do nothing if the edge is not incident to the node.
     *  @param edge The edge.
     *  @param node The node.
     */
    protected void _disconnect(Edge edge, Node node) {
        super._disconnect(edge, node);
        _removeIfPresent(_inputEdgeList(node), edge);
        _removeIfPresent(_outputEdgeList(node), edge);
    }

    /** Create and register all of the change listeners for this graph, and
     *  initialize the change counter of the graph.
     */
    protected void _initializeListeners() {
        super._initializeListeners();
        _sinkNodeListener = new Analysis(this);
        _sourceNodeListener = new Analysis(this);
    }

    /** Register a new edge in the graph.
     *  @param edge The new edge.
     */
    protected void _registerEdge(Edge edge) {
        super._registerEdge(edge);
        _transitiveClosure = null;
    }

    /** Register a new node in the graph.
     *  @param node The new node.
     */
    protected void _registerNode(Node node) {
        super._registerNode(node);
        _inputEdgeMap.put(node, new ArrayList());
        _outputEdgeMap.put(node, new ArrayList());
        _transitiveClosure = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The adjacency matrix representation of the transitive closure.
     *  The entry (i, j) is <code>true</code> if and only if there
     *  exists a path from the node with label <i>i</i> to the node with label
     *  <i>j</i>.
     *  This array is computed by {@link #_computeTransitiveClosure()}.
     *  After each graph change, that method should be called before
     *  this array is used. Otherwise, this array is not valid.
     */
    protected boolean[][] _transitiveClosure = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return the list of input edges for a specified node.
    private ArrayList _inputEdgeList(Node node) {
        return (ArrayList)_inputEdgeMap.get(node);
    }

    // Return the list of output edges for a specified node.
    private ArrayList _outputEdgeList(Node node) {
        return (ArrayList)_outputEdgeMap.get(node);
    }

    // Remove an object from an ArrayList if it exists in the list.
    private void _removeIfPresent(ArrayList list, Object element) {
        int index;
        if ((index = list.indexOf(element)) != -1) {
            list.remove(index);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A mapping from nodes into their lists of input edges.
    // Each key in this map is an instance of Node. Each value
    // is an instance of ArrayList whose elements are instances of Edge.
    // This redundant information is maintained for improved
    // run-time efficiency when handing undirected graphs, or when operating
    // on directed graphs in ways for which edge orientation is not relevant.
    private HashMap _inputEdgeMap;

    // A mapping from nodes into their lists of output edges.
    // Each key in this map is an instance of Node. Each value
    // is an instance of ArrayList whose elements are instances of Edge.
    private HashMap _outputEdgeMap;

    // A flag that indicates whether or not this graph is acyclic.
    private boolean _isAcyclic;

    // The graph listener for computation of sink nodes.
    private Analysis _sinkNodeListener;

    // The set of sink nodes in this graph. Recomputation requirements
    // of this data structure are tracked by _sinkNodeListener.
    private ArrayList _sinkNodes;

    // The graph listener for computation of source nodes.
    private Analysis _sourceNodeListener;

    // The set of source nodes in this graph. Recomputation requirements
    // of this data structure are tracked by _sourceNodeListener.
    private ArrayList _sourceNodes;
}
