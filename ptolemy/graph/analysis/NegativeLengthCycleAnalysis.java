/* Analysis to check if a cyclic directed graph has a negative-length cycle.

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

import ptolemy.graph.Graph;
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.NegativeLengthCycleAnalyzer;
import ptolemy.graph.analysis.strategy.FloydWarshallNegativeLengthCycleStrategy;
import ptolemy.graph.mapping.ToDoubleMapping;

///////////////////////////////////////////////////////////////////
//// NegativeLengthCycleAnalysis

/**
 Analysis to check if a cyclic directed graph has a negative-length cycle.
 A negative-length cycle is a cycle in which the sum of all the values associated
 with the edges of the cycle is negative. In a graph with multiple edges between
 two nodes the one with the smallest associated value is being used to check for
 the existence of negative cycles.
 <p>

 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class NegativeLengthCycleAnalysis extends Analysis {
    /** Construct an instance of this class using a default analyzer.
     *  The default analyzer runs in O(N^3) in which N is the number of nodes.
     *
     *  @param graph The given graph.
     *  @param edgeLengths The lengths associated with the edges of the graph.
     */
    public NegativeLengthCycleAnalysis(Graph graph, ToDoubleMapping edgeLengths) {
        super(new FloydWarshallNegativeLengthCycleStrategy(graph, edgeLengths));
    }

    /** Construct an instance of this class using a given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public NegativeLengthCycleAnalysis(NegativeLengthCycleAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if a negative cycle exists in the graph under analysis.
     *
     *  @return True if the graph has a negative cycle.
     */
    public boolean hasNegativeLengthCycle() {
        return ((NegativeLengthCycleAnalyzer) analyzer())
                .hasNegativeLengthCycle();
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    @Override
    public String toString() {
        return "Negative-length cycle analysis using the following analyzer:\n"
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
        return analyzer instanceof NegativeLengthCycleAnalyzer;
    }
}
