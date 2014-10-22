/* An analysis for the computation of transitive closure of a directed graph.

 Copyright (c) 2002-2014 The University of Maryland. All rights reserved.
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

import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.TransitiveClosureAnalyzer;
import ptolemy.graph.analysis.strategy.FloydWarshallTransitiveClosureStrategy;

///////////////////////////////////////////////////////////////////
//// TransitiveClosureAnalysis

/**
 An analysis for the computation of transitive closure of a directed graph.
 While there is a path directed from node X to Y in the given graph,
 there is an edge from X to Y in the transformed graph. Generally, transitive
 closure is expressed in terms of square matrix with graph node labels as
 indices.
 <p>
 The {@link #transitiveClosureMatrix()} method returns the transitive closure of
 the graph in the form of a two dimensional array. The first dimension represents
 source node label while the second one represents sink node label.
 Assume i and j are labels of two nodes. Matrix[i][j] is true if there is a path
 on the graph from "i" to "j".
 <p>
 @see ptolemy.graph.Graph#nodeLabel
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class TransitiveClosureAnalysis extends Analysis {
    /** Construct an instance of this class for a given graph with
     *  a default analyzer.
     *  The complexity of the default algorithm is O(N^3), where N is the
     *  number of nodes.
     *
     *  @param graph The given graph.
     */
    public TransitiveClosureAnalysis(Graph graph) {
        super(new FloydWarshallTransitiveClosureStrategy(graph));
    }

    /** Construct an instance of this class with a given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public TransitiveClosureAnalysis(TransitiveClosureAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check if there exist a path between a starting node "startNode" and an
     *  ending node "endNode" on the graph under analysis.
     *
     *  @param startNode The starting node.
     *  @param endNode The ending node.
     *  @return True if such a path exists.
     */
    public boolean pathExistence(Node startNode, Node endNode) {
        return ((TransitiveClosureAnalyzer) analyzer()).pathExistence(
                startNode, endNode);
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return Return a description of the analysis and the associated
     *  analyzer.
     */
    @Override
    public String toString() {
        return "Transitive closure analysis using the following analyzer:\n"
                + analyzer().toString();
    }

    /** Compute the transitive closure of the graph under analysis in the
     *  form of two dimensional array. The first dimension represents source
     *  node label while the second one represents sink node label.
     *
     *  @see ptolemy.graph.Graph#nodeLabel
     *  @return The transitive closure in the form of 2D array.
     */
    public boolean[][] transitiveClosureMatrix() {
        return ((TransitiveClosureAnalyzer) analyzer())
                .transitiveClosureMatrix();
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
        return analyzer instanceof TransitiveClosureAnalyzer;
    }
}
