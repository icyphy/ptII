/* An analysis to find the longest path from a single source to all the other
 nodes in a directed graph.

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
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.SingleSourceLongestPathAnalyzer;
import ptolemy.graph.analysis.strategy.AllEdgeSingleSourceLongestPathStrategy;
import ptolemy.graph.mapping.ToDoubleMapping;

///////////////////////////////////////////////////////////////////
//// SingleSourceLongestPathAnalysis

/**
 An analysis to find the longest path from a single source to all the other
 nodes in a directed graph. In a graph with multiple edges between two nodes the
 one with the largest associated value is being considered for the longest path.
 <p>

 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class SingleSourceLongestPathAnalysis extends Analysis {
    /** Construct an instance of this class with a default analyzer.
     *  The default analyzer runs in O(E), in which E is the number of edges.
     *
     *  @param graph The given graph.
     *  @param startNode The node from which the longest path is going to be
     *  calculated.
     *  @param edgeLengths The lengths of the edges of the given graph, which
     *  are going to be used to calculated the longest path.
     */
    public SingleSourceLongestPathAnalysis(Graph graph, Node startNode,
            ToDoubleMapping edgeLengths) {
        super(new AllEdgeSingleSourceLongestPathStrategy(graph, startNode,
                edgeLengths));
    }

    /** Construct an instance of this class with a given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public SingleSourceLongestPathAnalysis(
            SingleSourceLongestPathAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the distance from the node "startNode" to all the other nodes in
     *  the graph.
     *  The result is a double[] indexed by the destination node-label.
     *  <p>
     *  @see ptolemy.graph.Graph#nodeLabel
     *
     *  @return Return the distance from the start node to all the other nodes
     *  in the graph.
     */
    public double[] distance() {
        return ((SingleSourceLongestPathAnalyzer) analyzer()).distance();
    }

    /** Return the single source-node (start node) of this analyzer.
     *
     *  @return Return the starting node of this analyzer.
     *  @see #setStartNode(Node)
     */
    public Node getStartNode() {
        return ((SingleSourceLongestPathAnalyzer) analyzer()).getStartNode();
    }

    /** Return the longest path from node "startNode" to node "endNode" in the
     *  form of an ordered list.
     *
     *  @param endNode The ending node of the path.
     *  @return The longest path from "startNode" to "endNode".
     */
    public List path(Node endNode) {
        return ((SingleSourceLongestPathAnalyzer) analyzer()).path(endNode);
    }

    /** Return the length of the longest path from node "startNode"
     *  to node "endNode". The source node can be set
     *  using {@link #setStartNode}.
     *
     *  @param endNode The ending node of the path.
     *  @return The length of the longest path.
     */
    public double pathLength(Node endNode) {
        return ((SingleSourceLongestPathAnalyzer) analyzer())
                .pathLength(endNode);
    }

    /** Set the start-node of this analysis to the given node.
     *
     *  @param startNode The given node.
     *  @see #getStartNode()
     */
    public void setStartNode(Node startNode) {
        ((SingleSourceLongestPathAnalyzer) analyzer()).setStartNode(startNode);
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    @Override
    public String toString() {
        return "Single source longest path analysis using "
                + "the following analyzer:\n" + analyzer().toString();
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
        return analyzer instanceof SingleSourceLongestPathAnalyzer;
    }
}
