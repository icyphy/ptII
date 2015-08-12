/* An analysis to compute the all pair shortest path of a directed graph.

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
package ptolemy.graph.analysis;

import java.util.List;

import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.analyzer.AllPairShortestPathAnalyzer;
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.strategy.FloydWarshallAllPairShortestPathStrategy;
import ptolemy.graph.mapping.ToDoubleMapping;

///////////////////////////////////////////////////////////////////
//// AllPairShortestPathAnalysis

/**
 An analysis to compute of the all pair shortest path of a directed graph.
 The result is in the form of two dimensional array (matrix).
 The first dimension is indexed by the source node label while the second one is
 indexed by the sink node label. In graphs that have multiple edges between two
 nodes obviously the edge with the minimum weight is being considered for
 the shortest path.
 <p>
 The distance between a node and itself is being considered Double.MAX_VALUE,
 unless there is a self-edge.
 <p>
 The result of {@link #shortestPathMatrix()}[i][i] would be the
 length of the shortest cycle that includes the node with label "i".
 <p>

 The default analyzer runs in O(N^3) in which N is the number of nodes.

 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @version $Id$
 @author Shahrooz Shahparnia
 @see ptolemy.graph.Graph#nodeLabel
 */
public class AllPairShortestPathAnalysis extends Analysis {
    /** Construct an instance of this class with a default analyzer.
     *  The default analyzer runs in O(N^3) where N is the number of nodes.
     *
     *  @param graph The given graph.
     *  @param edgeLength A mapping between the graph edges and double values,
     *  which play the role of edge costs.
     *
     */
    public AllPairShortestPathAnalysis(Graph graph, ToDoubleMapping edgeLength) {
        super(new FloydWarshallAllPairShortestPathStrategy(graph, edgeLength));
    }

    /** Construct an instance of this class with a given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public AllPairShortestPathAnalysis(AllPairShortestPathAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the nodes on the shortest path from the node
     *  "startNode" to the node "endNode" in the form of an ordered list.
     *
     *  @param startNode The starting node of the path.
     *  @param endNode The ending node of the path.
     *  @return Return the nodes on the shortest path from the
     *  node "startNode" to the node "endNode" in the form of an ordered list.
     */
    public List shortestPath(Node startNode, Node endNode) {
        return ((AllPairShortestPathAnalyzer) analyzer()).shortestPath(
                startNode, endNode);
    }

    /** Return the length of the shortest path from the node
     *  startNode to the node endNode.
     *
     *  @param startNode The starting node of the path.
     *  @param endNode The end node of the path.
     *  @return Return the length of the shortest path from the node
     *  "startNode" to the node "endNode".
     */
    public double shortestPathLength(Node startNode, Node endNode) {
        return ((AllPairShortestPathAnalyzer) analyzer()).shortestPathLength(
                startNode, endNode);
    }

    /** Return a matrix representing the result of the all pair shortest path
     *  algorithm.
     *  The first dimension is indexed by the source node label while the
     *  second one is indexed by the sink node label.
     *  The result of {@link #shortestPathMatrix()}[i][i] would be
     *  the length of the shortest cycle that includes the node with label "i"
     *  and the result of {@link #shortestPathMatrix()}[i][j] would be
     *  the length of the shortest from the node with label "i" to the node
     *  with label "j".
     *
     *  @return Return a matrix representing the result of the all pair shortest
     *  path algorithm.
     */
    public double[][] shortestPathMatrix() {
        return ((AllPairShortestPathAnalyzer) analyzer()).shortestPathMatrix();
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    @Override
    public String toString() {
        return "All pair shortest path analysis using the following analyzer:\n"
                + analyzer().toString();
    }

    /** Check if a given analyzer is compatible with this analysis.
     *  In other words if it is possible to use it to compute the computation
     *  associated with this analysis.
     *
     *  @param analyzer The given analyzer.
     *  @return True if the given analyzer is valid for this analysis.
     */
    @Override
    public boolean validAnalyzerInterface(Analyzer analyzer) {
        return analyzer instanceof AllPairShortestPathAnalyzer;
    }
}
