/* Maximum profit to cost ratio analysis.

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
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.MaximumProfitToCostRatioAnalyzer;
import ptolemy.graph.analysis.strategy.ParhiMaximumProfitToCostRatioStrategy;
import ptolemy.graph.mapping.ToDoubleMapping;
import ptolemy.graph.mapping.ToIntMapping;

///////////////////////////////////////////////////////////////////
//// MaximumProfitToCostRatioAnalysis

/**
 Maximum profit to cost ratio analysis.
 <p>
 Please refer to:
 <p>
 Ali Dasdan , Sandy S. Irani , Rajesh K. Gupta, Efficient algorithms for optimum
 cycle mean and optimum cost to time ratio problems,
 Proceedings of the 36th ACM/IEEE conference on Design automation conference,
 p.37-42, June 21-25, 1999, New Orleans, Louisiana, United States
 <p>
 for a detailed mathematical description of the problem.
 <p>
 @see ptolemy.graph.analysis.CycleMeanAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class MaximumProfitToCostRatioAnalysis extends Analysis {
    /** Construct an instance of this class using a default analyzer.
     *  Please note the limitation on edge costs which is being imposed by the
     *  default analyzer.
     *
     *  @param graph The given graph.
     *  @param edgeProfits The profits associated with the edges of the graph.
     *  @param edgeCosts The costs associated with the edges of the graph.
     */
    public MaximumProfitToCostRatioAnalysis(Graph graph,
            ToDoubleMapping edgeProfits, ToIntMapping edgeCosts) {
        super(new ParhiMaximumProfitToCostRatioStrategy(graph, edgeProfits,
                edgeCosts));
    }

    /** Construct an instance of this class using a given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public MaximumProfitToCostRatioAnalysis(
            MaximumProfitToCostRatioAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the nodes on the cycle that corresponds to the maximum profit to
     *  cost ratio.
     *
     *  @return The nodes on the cycle as an ordered list.
     */
    public List cycle() {
        return ((MaximumProfitToCostRatioAnalyzer) analyzer()).cycle();
    }

    /** Return the maximum profit to cost ratio of the given graph.
     *
     *  @return Return the maximum profit to cost ratio of the associated graph.
     */
    public double maximumRatio() {
        return ((MaximumProfitToCostRatioAnalyzer) analyzer()).maximumRatio();
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    @Override
    public String toString() {
        return "Maximum profit to cost ratio analysis using "
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
        return analyzer instanceof MaximumProfitToCostRatioAnalyzer;
    }
}
