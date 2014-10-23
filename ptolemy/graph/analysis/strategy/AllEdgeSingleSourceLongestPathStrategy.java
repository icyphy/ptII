/* An analyzer used for finding the longest path from a single source.

 Copyright (c) 2003-2014 The University of Maryland. All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.


 */
package ptolemy.graph.analysis.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.analyzer.CycleExistenceAnalyzer;
import ptolemy.graph.analysis.analyzer.SingleSourceLongestPathAnalyzer;
import ptolemy.graph.mapping.ToDoubleMapping;

///////////////////////////////////////////////////////////////////
//// AllEdgeSingleSourceLongestPathStrategy

/**
 An analyzer used to find the longest path from a single source.
 <p>
 This algorithm runs in O(E), in which E is the number of edges.
 <p>
 @see ptolemy.graph.analysis.SingleSourceLongestPathAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class AllEdgeSingleSourceLongestPathStrategy extends CachedStrategy
implements SingleSourceLongestPathAnalyzer {
    /** Construct an instance of this analyzer.
     *
     *  @param graph The given graph.
     *  @param startNode The node from which the longest path is going to be
     *  calculated.
     *  @param edgeLengths The lengths of the edges of the given graph, which
     *  are going to be used to calculated the longest path.
     */
    public AllEdgeSingleSourceLongestPathStrategy(Graph graph, Node startNode,
            ToDoubleMapping edgeLengths) {
        super(graph);
        _startNode = startNode;
        _edgeLengths = edgeLengths;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the distance from the start node to all the other nodes in the
     *  graph. The result is a double[] indexed by the destination node label.
     *  @see ptolemy.graph.Graph#nodeLabel
     *
     *  @return Return the distance from the start node to all the other nodes
     *  in the graph.
     */
    @Override
    public double[] distance() {
        return (double[]) _result();
    }

    /** Return the single source-node (start node) of this analyzer.
     *
     *  @return Return the starting node of this analyzer.
     *  @see #setStartNode(Node)
     */
    @Override
    public Node getStartNode() {
        return _startNode;
    }

    /** Return the longest path from node startNode to node endNode in the form
     *  of an ordered list. The source node is defined in the constructor,
     *  and can be changed using {@link #setStartNode}.
     *  The result includes the starting and the ending nodes.
     *
     *  @param endNode The ending node of the path.
     *  @return The longest path.
     */
    @Override
    public List path(Node endNode) {
        int[] predecessors = predecessors();
        ArrayList pathNodes = new ArrayList();
        int predecessorsIndex = predecessors[graph().nodeLabel(endNode)];
        Node predecessor = null;

        if (predecessorsIndex != -1) {
            predecessor = graph().node(predecessorsIndex);

            do {
                pathNodes.add(predecessor);
                predecessorsIndex = predecessors[graph().nodeLabel(predecessor)];

                if (predecessorsIndex != -1) {
                    predecessor = graph().node(predecessorsIndex);
                } else {
                    break;
                }
            } while (predecessor != _startNode);

            if (predecessor == _startNode) {
                pathNodes.add(endNode);
            }
        }

        return pathNodes;
    }

    /** Return the length of the longest path from node startNode
     *  to node endNode. The source node is defined in the constructor
     *  and can be changed using {@link #setStartNode}.
     *
     *  @param endNode The ending node of the path.
     *  @return The length of the longest path.
     */
    @Override
    public double pathLength(Node endNode) {
        double[] distance = distance();
        return distance[graph().nodeLabel(endNode)];
    }

    /** Set the single source node (starting node) of this analyzer to the
     *  given node.
     *
     *  @param startNode The given node.
     *  @see #getStartNode()
     */
    @Override
    public void setStartNode(Node startNode) {
        _startNode = startNode;
        reset();
    }

    /** Return a description of the analyzer.
     *
     *  @return Return a description of the analyzer..
     */
    @Override
    public String toString() {
        return "Single source longest path analyzer"
                + " which runs in O(E) in which E is the number of edges.";
    }

    /** Check for compatibility between the analysis and the given
     *  graph. A graph needs to be an instance of a DirectedGraph and acyclic
     *  in order to use this algorithm. This compatibility check
     *  runs in O(N^3) in which N is the number of nodes.
     *
     *  @return True if the graph is a directed graph and acyclic.
     */
    @Override
    public boolean valid() {
        boolean result = false;

        if (graph() instanceof DirectedGraph) {
            CycleExistenceAnalyzer analyzer = new FloydWarshallCycleExistenceStrategy(
                    graph());
            result = !analyzer.hasCycle();
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** The computation associated with this analyzer.
     *
     *  @return The result of the computation.
     */
    @Override
    protected Object _compute() {
        DirectedGraph graph = (DirectedGraph) graph();
        ArrayList queue = new ArrayList();

        //HashMap color = new HashMap();
        double[] distance = new double[graph.nodeCount()];
        _predecessor = new int[graph.nodeCount()];

        for (Iterator nodes = graph.nodes().iterator(); nodes.hasNext();) {
            Node node = (Node) nodes.next();

            if (node != _startNode) {
                //color.put(node, Color.white);
                distance[graph.nodeLabel(node)] = -Double.MIN_VALUE;
                _predecessor[graph.nodeLabel(node)] = -1;
            } else {
                distance[graph.nodeLabel(node)] = 0.0;
            }
        }

        queue.add(_startNode);
        _predecessor[graph.nodeLabel(_startNode)] = -1;

        while (!queue.isEmpty()) {
            Node u = (Node) queue.get(0);
            Collection successors = graph.successors(u);

            if (successors != null) {
                for (Iterator successorNodes = successors.iterator(); successorNodes
                        .hasNext();) {
                    Node v = (Node) successorNodes.next();
                    double predecessorDistance = distance[graph().nodeLabel(u)];
                    double actualDistance = distance[graph().nodeLabel(v)];
                    Collection edgeCollection = graph.predecessorEdges(v, u);
                    Iterator edges = edgeCollection.iterator();
                    double connectingEdgeCost = -Double.MAX_VALUE;

                    while (edges.hasNext()) {
                        Edge edge = (Edge) edges.next();

                        if (_edgeLengths.toDouble(edge) > connectingEdgeCost) {
                            connectingEdgeCost = _edgeLengths.toDouble(edge);
                        }
                    }

                    if (actualDistance < predecessorDistance
                            + connectingEdgeCost) {
                        distance[graph.nodeLabel(v)] = predecessorDistance
                                + connectingEdgeCost;
                        _predecessor[graph.nodeLabel(v)] = graph.nodeLabel(u);
                    }

                    if (v != _startNode) {
                        queue.add(v);
                    }
                }
            }

            queue.remove(0);

            //color.put(u, Color.black);
        }

        return distance;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the predecessor array of this analyzer.
     *  The array is indexed by a node label and contains the predecessor
     *  node label on the longest path.
     *
     *  @return Return the predecessor array of this analyzer.
     */
    private int[] predecessors() {
        return _predecessor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The values associated to the edges, in this analyzer.
    private ToDoubleMapping _edgeLengths;

    // The starting node used in this analyzer.
    private Node _startNode;

    // The predecessors of the nodes on the longest path.
    private int[] _predecessor;
}
