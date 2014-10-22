/* An analyzer for computing the maximum/minimum cycle mean of a graph which
 uses Karp's algorithm.

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.analyzer.CycleExistenceAnalyzer;
import ptolemy.graph.analysis.analyzer.CycleMeanAnalyzer;
import ptolemy.graph.mapping.ToDoubleMapping;

///////////////////////////////////////////////////////////////////
//// KarpCycleMeanAnalyzer

/**
 An analyzer for computing the maximum/minimum cycle mean of a graph.
 This implementation uses the Karp's algorithm described in:
 <p>
 A.Dasdan, R.K. Gupta, "Faster Maximum and Minimum Mean Cycle Algorithms
 for System Performance".
 <p>
 Note that the mathematical definition of maximum cycle mean and maximum profit
 to cost are different, though some time the name "maximum cycle mean" is used to
 refer to the maximum profit to cost ratio.
 <p>
 @see ptolemy.graph.analysis.CycleMeanAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class KarpCycleMeanStrategy extends CachedStrategy implements
        CycleMeanAnalyzer {
    /** Construct a maximum cycle mean analyzer for a given graph, using the
     *  Karp's algorithm.
     *
     *  @param graph The given graph.
     *  @param edgeLengths The lengths associated with the edges of the given
     *  graph.
     */
    public KarpCycleMeanStrategy(Graph graph, ToDoubleMapping edgeLengths) {
        super(graph);
        _edgeLengths = edgeLengths;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the nodes on the cycle that corresponds to the maximum/minimum
     *  cycle mean as an ordered list. If there is more than one cycle with the
     *  same maximal/minimal MCM, one of them is returned randomly, but the same
     *  cycle is returned by different invocations of the method, unless the
     *  graph changes. A call to maximumCycleMean() or minimumCycleMean() should
     *  precede a call to this method, in order to return a valid cycle.
     *
     *  @return The nodes on the cycle that corresponds to one of the
     *  maximum/minimum cycle means as an ordered list.
     */
    @Override
    public List cycle() {
        return _cycle;
    }

    /** Finds the cycle mean for a given directed graph.
     *  Strongly connected components are being considered separately.
     *  And the CycleMean is the maximum/minimum among them.
     *  When there are multiple edges between two nodes, the edge with the
     *  maximum/minimum weight is considered for the cycle that gives the
     *  maximum/minimum cycle mean.
     *
     *  @param maximum True if the maximum cycle mean is requested.
     *  @return The maximum/minimum cycle mean.
     */
    public double cycleMean(boolean maximum) {
        if (_maximumAnalysis != maximum) {
            _maximumAnalysis = maximum;
            reset();
        }

        return ((Double) _result()).doubleValue();
    }

    /** Return the maximum cycle mean.
     *
     *  @return The maximum cycle mean value.
     */
    @Override
    public double maximumCycleMean() {
        return cycleMean(true);
    }

    /** Return minimum cycle mean.
     *
     *  @return The minimum cycle mean value.
     */
    @Override
    public double minimumCycleMean() {
        return cycleMean(false);
    }

    /** Return a description of the analyzer.
     *
     *  @return Return a description of the analyzer..
     */
    @Override
    public String toString() {
        return "All pair shortest path analyzer"
                + " based on Karp's algorithm.";
    }

    /** Check for compatibility between the analysis and the given
     *  graph. A graph needs to be an instance of a DirectedGraph and cyclic
     *  in order to have a cycle mean.  In addition the given object should be
     *  the same graph associated with this analyzer.
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
    @Override
    protected Object _compute() {
        DirectedGraph[] graph = ((DirectedGraph) graph()).sccDecomposition();
        double maximumResult = -Double.MAX_VALUE;
        double result = 0;

        for (int i = 0; i < graph.length; i++) {
            if (!graph[i].isAcyclic()) {
                result = _computeMCMOfSCC(graph[i]);

                if (result > maximumResult) {
                    maximumResult = result;
                    _cycle = new ArrayList();

                    for (int j = 0; j < _nodesOnCycle.size(); j++) {
                        _cycle.add(_nodesOnCycle.get(j));
                    }
                }
            }
        }

        if (_maximumAnalysis) {
            result = maximumResult;
        } else {
            result = -maximumResult;
        }

        return Double.valueOf(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Computes the MCM for one strongly connected component of the graph.
    // It uses the Karp's algorithm described in:
    // A.Dasdan, R.K. Gupta, "Faster Maximum and Minimum Mean Cycle Algorithms
    // for System Performance".
    private double _computeMCMOfSCC(DirectedGraph directedCyclicGraph) {
        _nodesOnCycle.clear();

        // Head
        int n = directedCyclicGraph.nodeCount();
        Node resultNode = null;
        HashMap[] maximumPathLength = new HashMap[n + 1];
        HashMap[] predecessor = new HashMap[n + 1];
        HashMap cycleMean = new HashMap(n);
        HashMap cycleMeanLevel = new HashMap(n);
        double result = -Double.MAX_VALUE;
        Node startingNode = directedCyclicGraph.node(0);
        Collection nodeCollection = directedCyclicGraph.nodes();

        for (int k = 0; k <= n; k++) {
            maximumPathLength[k] = new HashMap(n);
            predecessor[k] = new HashMap(n);

            Iterator nodes = nodeCollection.iterator();

            while (nodes.hasNext()) {
                Node node = (Node) nodes.next();
                maximumPathLength[k].put(node,
                        Double.valueOf(-Double.MAX_VALUE));
            }
        }

        maximumPathLength[0].put(startingNode, Double.valueOf(0));
        predecessor[0].put(startingNode, null);

        // Body
        for (int k = 1; k <= n; k++) {
            Iterator nodes = nodeCollection.iterator();

            while (nodes.hasNext()) {
                Node node = (Node) nodes.next();
                Collection predecessorCollection = directedCyclicGraph
                        .predecessors(node);
                Iterator predecessors = predecessorCollection.iterator();

                while (predecessors.hasNext()) {
                    Node nodePredecessor = (Node) predecessors.next();
                    double dKOfV = ((Double) maximumPathLength[k].get(node))
                            .doubleValue();
                    double dKMinusOneU = ((Double) maximumPathLength[k - 1]
                            .get(nodePredecessor)).doubleValue();
                    double distance = _getCost(nodePredecessor, node);
                    double cost = dKMinusOneU + distance;

                    if (dKOfV < cost) {
                        predecessor[k].put(node, nodePredecessor);
                        maximumPathLength[k].put(node, Double.valueOf(cost));
                    }
                }
            }
        }

        // Tail
        Iterator nodes = nodeCollection.iterator();

        while (nodes.hasNext()) {
            Node node = (Node) nodes.next();
            cycleMean.put(node, Double.valueOf(Double.MAX_VALUE));

            for (int k = 0; k < n; k++) {
                double maximumPathLengthToLevelK = ((Double) maximumPathLength[k]
                        .get(node)).doubleValue();
                double maximumPathLengthToLevelN = ((Double) maximumPathLength[n]
                        .get(node)).doubleValue();
                double cycleMeanValue = ((Double) cycleMean.get(node))
                        .doubleValue();
                double testValue = (maximumPathLengthToLevelN - maximumPathLengthToLevelK)
                        / (n - k);

                if (cycleMeanValue > testValue) {
                    cycleMean.put(node, Double.valueOf(testValue));
                    cycleMeanLevel.put(node, Integer.valueOf(k));
                }
            }

            double cycleMeanValue = ((Double) cycleMean.get(node))
                    .doubleValue();

            if (result < cycleMeanValue) {
                result = cycleMeanValue;
                resultNode = node;
            }
        }

        // _dumpVariable(maximumPathLength, directedCyclicGraph);
        //int lambdaCycleMeanLevel = ((Integer) cycleMeanLevel.get(resultNode))
        //        .intValue();
        Node firstNode = resultNode;
        Node secondNode = firstNode;
        int firstNodeLevel = 0;
        int secondNodeLevel = 0;

        for (int i = n; i > 0; i--) {
            for (int j = i; j > 0; j--) {
                secondNode = (Node) predecessor[j].get(secondNode);

                if (secondNode == firstNode) {
                    firstNodeLevel = i;
                    secondNodeLevel = j;
                    break;
                }
            }

            if (secondNode == firstNode) {
                break;
            }

            firstNode = (Node) predecessor[i].get(firstNode);
            secondNode = firstNode;
        }

        for (int k = firstNodeLevel; k >= secondNodeLevel; k--) {
            firstNode = (Node) predecessor[k].get(firstNode);
            _nodesOnCycle.add(firstNode);
        }

        return result;
    }

    // Used for debugging purposes.
    //    private void _dumpVariable(HashMap[] maximumPathLength,
    //            DirectedGraph directedCyclicGraph) {
    //        Collection nodeCollection = directedCyclicGraph.nodes();
    //        int n = directedCyclicGraph.nodeCount();
    //
    //        for (int k = 0; k <= n; k++) {
    //            Iterator nodes = nodeCollection.iterator();
    //
    //            while (nodes.hasNext()) {
    //                Node node = (Node) nodes.next();
    //                System.out.println(node + ":" + maximumPathLength[k].get(node)
    //                        + "   ");
    //            }
    //
    //            System.out.println();
    //        }
    //    }
    // Return the length of edge with maximum/minimum length between
    // the two nodes.
    private double _getCost(Node u, Node v) {
        DirectedGraph directedCyclicGraph = (DirectedGraph) graph();
        Collection edgeCollection = directedCyclicGraph.predecessorEdges(v, u);
        Iterator edges = edgeCollection.iterator();
        double weight = -Double.MAX_VALUE;

        if (!_maximumAnalysis) {
            weight = Double.MAX_VALUE;
        }

        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();

            if (_maximumAnalysis) {
                double nextWeight = _edgeLengths.toDouble(edge);

                if (nextWeight > weight) {
                    weight = nextWeight;
                }
            } else {
                double nextWeight = -_edgeLengths.toDouble(edge);

                if (nextWeight < weight) {
                    weight = nextWeight;
                }
            }
        }

        return weight;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _maximumAnalysis = true;

    private ArrayList _nodesOnCycle = new ArrayList();

    private ArrayList _cycle;

    private ToDoubleMapping _edgeLengths;
}
