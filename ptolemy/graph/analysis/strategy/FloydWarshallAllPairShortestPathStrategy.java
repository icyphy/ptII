/* Computation of the all pair shortest path of a DirectedGraph using the
 Floyd-Warshall algorithm.

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
import java.util.Iterator;
import java.util.List;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.analyzer.AllPairShortestPathAnalyzer;
import ptolemy.graph.mapping.ToDoubleMapping;

///////////////////////////////////////////////////////////////////
//// FloydWarshallAllPairShortestPathStrategy

/**
 Computation of the all pair shortest path of a directed graph using the
 Floyd-Warshall algorithm. The result is in the form of two dimensional array
 (matrix).
 The first dimension is indexed by the source node label while the second one is
 indexed by the sink node label. In graphs that have multiple edges between two
 nodes obviously the edge with the minimum weight is being considered for
 the shortest path.
 The distance between a node and itself is being considered Double.MAX_VALUE,
 unless otherwise specified by a self edge for a cyclic graph.
 ((double[][])result())[i][i] would be the length of the shortest cycle that
 includes the node with label "i".
 <p>
 @see ptolemy.graph.analysis.AllPairShortestPathAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class FloydWarshallAllPairShortestPathStrategy extends
        FloydWarshallStrategy implements AllPairShortestPathAnalyzer {
    /** Construct an AllPairShortestPathAnalyzer which works using the
     *  Floyd-Warshall strategy.
     *
     *  @param graph The given graph.
     *  @param edgeLengths  The edge lengths.
     */
    public FloydWarshallAllPairShortestPathStrategy(Graph graph,
            ToDoubleMapping edgeLengths) {
        super(graph);
        _edgeLengths = edgeLengths;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the nodes on the shortest path from the node
     *  startNode to the node endNode in the form of an ordered list.
     *
     *  @param startNode The starting node of the path.
     *  @param endNode The ending node of the path.
     *  @return Return the nodes on the shortest path from the
     *  node startNode to the node endNode in the form of an ordered list.
     */
    @Override
    public List shortestPath(Node startNode, Node endNode) {
        ArrayList shortestPath = null;
        int startNodeLabel = graph().nodeLabel(startNode);
        int endNodeLabel = graph().nodeLabel(endNode);
        //int n = graph().nodeCount();
        int[][] nodeLabels = predecessors();

        if (nodeLabels[startNodeLabel][endNodeLabel] != -1) {
            shortestPath = new ArrayList();
            shortestPath.add(endNode);

            Node nodeOnPath = endNode;

            while (nodeOnPath != startNode) {
                int nodeOnPathLabel = graph().nodeLabel(nodeOnPath);
                nodeOnPath = graph().node(
                        nodeLabels[startNodeLabel][nodeOnPathLabel]);
                shortestPath.add(nodeOnPath);
            }
        }

        return shortestPath;
    }

    /** Return the length of the shortest path from the node
     *  startNode to the node endNode.
     *
     *  @param startNode The starting node of the path.
     *  @param endNode The end node of the path.
     *  @return Return the length of the shortest path from the node
     *  startNode to the node endNode.
     */
    @Override
    public double shortestPathLength(Node startNode, Node endNode) {
        double result = 0.0;
        //int n = graph().nodeCount();
        double[][] shortestPathResults = (double[][]) _result();
        result = shortestPathResults[graph().nodeLabel(startNode)][graph()
                .nodeLabel(endNode)];
        return result;
    }

    /** Return the all pair shortest path of the graph in the form of
     *  two dimensional array (matrix). The first dimension is indexed by the
     *  source node label while the second one is indexed by the
     *  sink node label. In graphs that have multiple edges between two nodes
     *  obviously the edge with the minimum weight is being considered for
     *  the shortest path.
     *  The distance between a node and itself is being considered
     *  Double.MAX_VALUE unless otherwise specified by a self edge.
     *  for a cyclic graph ((double[][])result())[i][i] would be the length
     *  of the shortest cycle that includes the node with label "i".
     *
     *  @see ptolemy.graph.Graph#nodeLabel
     *  @return The all pair shortest path matrix as a double[][].
     */
    @Override
    public double[][] shortestPathMatrix() {
        return (double[][]) _result();
    }

    /** Return a description of the analyzer.
     *
     *  @return Return a description of the analyzer..
     */
    @Override
    public String toString() {
        return "All pair shortest path analyzer"
                + " based on the Floyd-Warshall algorithm.";
    }

    /** Check for compatibility between the analysis and the given
     *  graph. A graph needs to be an instance of a DirectedGraph in order
     *  to use this algorithm. In addition the given object should be the same
     *  graph associated with this analyzer.
     *
     *  @return True if the graph is a directed graph.
     */
    @Override
    public boolean valid() {
        return graph() instanceof DirectedGraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compute the all pair shortest path of the graph in the form of
     *  two dimensional array (matrix).
     *
     *  @return The all pair shortest path matrix as a double[][] Object.
     */
    @Override
    protected Object _compute() {
        int n = graph().nodeCount();

        // Initialize shortest path matrix
        _allPairShortestPath = new double[n + 1][n][n];
        _predecessors = new int[n + 1][n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                _predecessors[0][i][j] = -1;

                _allPairShortestPath[0][i][j] = Double.MAX_VALUE;
            }

            Node node = graph().node(i);
            Iterator outputEdges = ((DirectedGraph) graph()).outputEdges(node)
                    .iterator();

            while (outputEdges.hasNext()) {
                Edge edge = (Edge) outputEdges.next();
                int sinkLabel = ((DirectedGraph) graph())
                        .nodeLabel(edge.sink());

                if (_allPairShortestPath[0][i][sinkLabel] > _edgeLengths
                        .toDouble(edge)) {
                    _allPairShortestPath[0][i][sinkLabel] = _edgeLengths
                            .toDouble(edge);
                }

                _predecessors[0][i][sinkLabel] = i;
            }
        }

        super._compute();
        _predecessorResult = _predecessors[n];
        return _allPairShortestPath[n];
    }

    /** The FloydWarshall computation associated with this,
     *  analysis.
     *
     *  @param k The counting parameter of the first loop of the Floyd-Warshall
     *  computation.
     *  @param i The counting parameter of the second loop of the Floyd-Warshall
     *  computation.
     *  @param j The counting parameter of the third and last loop of the
     *  Floyd-Warshall computation.
     */
    @Override
    protected final void _floydWarshallComputation(int k, int i, int j) {
        double b = Double.MAX_VALUE;
        double a = _allPairShortestPath[k][i][j];

        if (i != k && k != j) {
            b = _allPairShortestPath[k][i][k] + _allPairShortestPath[k][k][j];
        } else if (i == k && k != j) {
            b = _allPairShortestPath[k][k][j];
        } else if (i != k && k == j) {
            b = _allPairShortestPath[k][i][k];
        }

        if (b >= a) {
            _allPairShortestPath[k + 1][i][j] = a;
            _predecessors[k + 1][i][j] = _predecessors[k][i][j];
        } else {
            _allPairShortestPath[k + 1][i][j] = b;
            _predecessors[k + 1][i][j] = _predecessors[k][k][j];
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the node before the end node on the shortest path from a starting
     *  node to an ending node. The first dimension is indexed by the
     *  start node label while the second one is indexed by the
     *  end node label. The {@link #shortestPathMatrix} method should be called
     *  before this method in order to make the result consistent with the
     *  current state (graph, edge values, ...).
     *
     *  @return Return the node before the end node on the shortest path from a
     *  starting node to an ending node.
     */
    private int[][] predecessors() {
        return _predecessorResult;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ToDoubleMapping _edgeLengths;

    private int[][][] _predecessors;

    private int[][] _predecessorResult;

    private double[][][] _allPairShortestPath;
}
