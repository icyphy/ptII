/* Maximum profit to cost ratio analyzer which uses Parhi's algorithm for
 iteration bound.

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.SingleSourceLongestPathAnalysis;
import ptolemy.graph.analysis.analyzer.CycleExistenceAnalyzer;
import ptolemy.graph.analysis.analyzer.CycleMeanAnalyzer;
import ptolemy.graph.analysis.analyzer.MaximumProfitToCostRatioAnalyzer;
import ptolemy.graph.mapping.ToDoubleMapMapping;
import ptolemy.graph.mapping.ToDoubleMapping;
import ptolemy.graph.mapping.ToIntMapping;

///////////////////////////////////////////////////////////////////
//// ParhiMaximumProfitToCostRatioStrategy

/**
 Maximum profit to cost ratio analyzer which uses Parhi's algorithm for
 iteration bound.
 <p>
 For details about the algorithm, please refer to:
 <p>
 K. Ito and K. K. Parhi. Determining the minimum iteration period of an
 algorithm. Journal of VLSI Signal Processing, 11(3):229-244, December 1995
 <p>
 @see ptolemy.graph.analysis.MaximumProfitToCostRatioAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class ParhiMaximumProfitToCostRatioStrategy extends CachedStrategy
        implements MaximumProfitToCostRatioAnalyzer {
    /** Construct an instance of this class.
     *
     * @param graph The given graph.
     * @param edgeProfits The profits associated with the edges of the graph.
     * @param edgeCosts The costs associated with the edges of the graph.
     */
    public ParhiMaximumProfitToCostRatioStrategy(Graph graph,
            ToDoubleMapping edgeProfits, ToIntMapping edgeCosts) {
        super(graph);
        _edgeProfits = edgeProfits;
        _edgeCosts = edgeCosts;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the nodes on the cycle that corresponds to the maximum profit to
     *  cost ratio.
     *
     *  @return The nodes on the cycle as an ordered list.
     */
    @Override
    public List cycle() {
        _result();
        return _maximumProfitToCostRatioCycle;
    }

    /** Return the maximum profit to cost ratio of the given graph.
     *
     *  @return Return the maximum profit to cost ratio of the given graph.
     */
    @Override
    public double maximumRatio() {
        return ((Double) _result()).doubleValue();
    }

    /** Return a description of the analyzer.
     *
     *  @return Return a description of the analyzer..
     */
    @Override
    public String toString() {
        return "All pair shortest path analyzer"
                + " based on Parhi's algorithm.";
    }

    /** Check for compatibility between the analysis and the given
     *  graph. A graph needs to be an instance of a DirectedGraph and cyclic
     *  in order to have a maximum profit to cost ratio.  In addition the given
     *  object should be the same graph associated with this analyzer.
     *
     *  @return True if the graph is a directed and cyclic graph.
     */
    @Override
    public boolean valid() {
        boolean result = false;

        if (graph() instanceof DirectedGraph) {
            CycleExistenceAnalyzer analyzer = new FloydWarshallCycleExistenceStrategy(
                    graph());
            result = analyzer.hasCycle();
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    // build a delay graph an use it as the application graph.
    // what result() returns is the iteration bound, and what cycle
    // returns is a cycle of delays.
    // This cycle of delays is converted to a cycle in the original graph
    // through the _maximumProfitToCostCycle method, which return that
    // cycle.

    /* Return the maximum profit to cost ratio of the given graph.
     *
     *  @return Return the maximum profit to cost ratio of the given graph.
     */
    @Override
    protected Object _compute() {
        _delayNodeList = new ArrayList();
        _maximumProfitToCostRatioCycle = new ArrayList();

        DirectedGraph originalGraph = (DirectedGraph) graph();

        // Build a new graph with the delays as nodes added to the previous
        // graph.
        DirectedGraph graphPlusDelaysAsNodes = (DirectedGraph) originalGraph
                .cloneAs(new DirectedGraph());
        Object[] edges = graphPlusDelaysAsNodes.edges().toArray();
        HashMap edgeProfitsMap = new HashMap();

        for (int j = 0; j < edges.length; j++) {
            Edge edge = (Edge) edges[j];
            Node source = edge.source();
            Node sink = edge.sink();

            //_edgeCostsMap.put(edge, _edgeCosts.toInt());
            // For all the edges that have at least one delay
            if (_edgeCosts.toInt(edge) != 0) {
                graphPlusDelaysAsNodes.removeEdge(edge);

                int delays = _edgeCosts.toInt(edge);

                for (int i = 0; i < delays; i++) {
                    Node addedNode = graphPlusDelaysAsNodes.addNodeWeight("D"
                            + j + i);
                    _delayNodeList.add(addedNode);

                    Edge addedEdge = graphPlusDelaysAsNodes.addEdge(source,
                            addedNode);
                    edgeProfitsMap.put(addedEdge, Double.valueOf(0.0));
                    source = addedNode;
                }

                Edge lastAddedEdge = graphPlusDelaysAsNodes.addEdge(source,
                        sink);
                edgeProfitsMap.put(lastAddedEdge,
                        Double.valueOf(_edgeProfits.toDouble(edge)));
            } else {
                edgeProfitsMap.put(edge,
                        Double.valueOf(_edgeProfits.toDouble(edge)));
            }
        }

        HashMap D = new HashMap(_delayNodeList.size());
        edges = graphPlusDelaysAsNodes.edges().toArray();

        // compute the first order longest path matrix
        HashMap predecessorMap = new HashMap();

        for (Iterator delayNodes = _delayNodeList.iterator(); delayNodes
                .hasNext();) {
            Node delayNode = (Node) delayNodes.next();
            DirectedGraph thisRoundGraph = (DirectedGraph) graphPlusDelaysAsNodes
                    .clone();
            HashMap delayGraphProfitMap = new HashMap();

            for (Object edge2 : edges) {
                Edge edge = (Edge) edge2;
                Node source = edge.source();
                Node sink = edge.sink();

                if (sink == delayNode) {
                    predecessorMap.put(delayNode, source);
                }

                if (_delayNodeList.contains(source)
                        || _delayNodeList.contains(sink)) {
                    if (source == delayNode) {
                        delayGraphProfitMap.put(edge, edgeProfitsMap.get(edge));
                    }

                    if (sink == delayNode) {
                        thisRoundGraph.removeEdge(edge);
                    }

                    if (source != delayNode && sink != delayNode) {
                        if (_delayNodeList.contains(source)) {
                            thisRoundGraph.removeEdge(edge);
                        } else {
                            delayGraphProfitMap.put(edge,
                                    edgeProfitsMap.get(edge));
                        }
                    }
                } else {
                    delayGraphProfitMap.put(edge, edgeProfitsMap.get(edge));
                }
            }

            SingleSourceLongestPathAnalysis longestPath = null;
            longestPath = new SingleSourceLongestPathAnalysis(thisRoundGraph,
                    delayNode, new ToDoubleMapMapping(delayGraphProfitMap));
            D.put(delayNode, longestPath);
        }

        _makeFirstOrderLongestPathMatrix(D, graphPlusDelaysAsNodes,
                predecessorMap);

        // create the delay graph on which the maximum cycle mean is going to
        // be executed.
        DirectedGraph delayGraph = new DirectedGraph();
        HashMap delayGraphEdgeProfits = new HashMap();

        for (int i = 0; i < _delayNodeList.size(); i++) {
            delayGraph.addNode((Node) _delayNodeList.get(i));
        }

        for (int i = 0; i < _delayNodeList.size(); i++) {
            for (int j = 0; j < _delayNodeList.size(); j++) {
                Node source = (Node) _delayNodeList.get(i);
                Node sink = (Node) _delayNodeList.get(j);

                if (_firstOrderLongestPathMatrix[i][j] >= 0) {
                    if (!(source == sink && _firstOrderLongestPathMatrix[i][j] == 0)) {
                        Edge addedEdge = delayGraph.addEdge(source, sink);
                        delayGraphEdgeProfits.put(addedEdge, Double
                                .valueOf(_firstOrderLongestPathMatrix[i][j]));
                    }
                }
            }
        }

        double result = _computeMCM(delayGraph, new ToDoubleMapMapping(
                delayGraphEdgeProfits));

        // creating the cycle that leads to the result
        //edges = graphPlusDelaysAsNodes.edges().toArray();

        Object[] delayNodes = _delayCycle.toArray();

        for (int i = 0; i < delayNodes.length; i++) {
            Node delayNode = (Node) delayNodes[i];

            for (int j = 0; j < delayNodes.length; j++) {
                if (i != j || delayNodes.length != 1) {
                    Node endDelayNode = (Node) delayNodes[j];
                    List path = ((SingleSourceLongestPathAnalysis) D
                            .get(delayNode)).path(endDelayNode);

                    for (int k = 0; k < path.size(); k++) {
                        if (!_delayNodeList.contains(path.get(k))) {
                            _maximumProfitToCostRatioCycle.add(path.get(k));
                        }
                    }
                } else if (delayNodes.length == 1) {
                    Node predecessor = (Node) predecessorMap.get(delayNode);

                    if (!_delayNodeList.contains(predecessor)) {
                        List path = ((SingleSourceLongestPathAnalysis) D
                                .get(delayNode)).path(predecessor);

                        for (int k = 0; k < path.size(); k++) {
                            if (!_delayNodeList.contains(path.get(k))) {
                                _maximumProfitToCostRatioCycle.add(path.get(k));
                            }
                        }
                    }
                }
            }
        }

        return Double.valueOf(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  To compute the maximum cycle mean of the delay graph.
     *  Derived class may override this method in case that they need to do
     *  further transformation on the delay graph or the edgeLength, such as
     *  making the edge costs negative in order to compute the minimum cycle
     *  mean.
     */
    private double _computeMCM(DirectedGraph graph, ToDoubleMapping edgeLength) {
        CycleMeanAnalyzer cycleMean = new KarpCycleMeanStrategy(graph,
                edgeLength);
        double result = cycleMean.maximumCycleMean();
        _delayCycle = cycleMean.cycle();
        return result;
    }

    // computes the First Order Longest Path Matrix which is a matrix
    // of longest distances from every node to every other node indexed
    // by the node label.
    private double[][] _makeFirstOrderLongestPathMatrix(HashMap D,
            DirectedGraph graph, HashMap predecessorMap) {
        _firstOrderLongestPathMatrix = new double[_delayNodeList.size()][_delayNodeList
                .size()];

        for (int i = 0; i < _delayNodeList.size(); i++) {
            for (int j = 0; j < _delayNodeList.size(); j++) {
                Node column = (Node) _delayNodeList.get(i);
                Node row = (Node) _delayNodeList.get(j);
                double value = 0;
                double[] distances = ((SingleSourceLongestPathAnalysis) D
                        .get(column)).distance();
                Node predecessor = (Node) predecessorMap.get(row);

                if (i != j || _delayNodeList.contains(predecessor)) {
                    value = distances[graph.nodeLabel(row)];
                } else {
                    value = distances[graph.nodeLabel(predecessor)];
                }

                _firstOrderLongestPathMatrix[i][j] = value;
            }
        }

        return _firstOrderLongestPathMatrix;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double[][] _firstOrderLongestPathMatrix;

    private List _delayCycle;

    private ArrayList _delayNodeList;

    private ArrayList _maximumProfitToCostRatioCycle;

    private ToDoubleMapping _edgeProfits;

    private ToIntMapping _edgeCosts;
}
