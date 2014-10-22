/* An analysis to calculate the maximum/minimum cycle mean of a directed
 cyclic graph.

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
import ptolemy.graph.analysis.analyzer.CycleMeanAnalyzer;
import ptolemy.graph.analysis.strategy.KarpCycleMeanStrategy;
import ptolemy.graph.mapping.ToDoubleMapping;

///////////////////////////////////////////////////////////////////
//// CycleMeanAnalysis

/**
 An analysis to calculate the maximum/minimum cycle mean of a directed
 cyclic graph.
 <p>
 The analyzer associated to this analysis, adds up the cost of the edges on
 different cycles and divides it by the number of edges in that cycle,
 then picks the maximum/minimum of them.
 <p>
 Strongly connected components are being processed separately, and the cycle
 mean has the maximum/minimum value among them.
 When there are multiple edges between two nodes, the edge with the
 maximum/minimum weight is considered for the cycle that gives the
 maximum/minimum cycle mean.

 <p>
 Note that the mathematical definition of maximum cycle mean and maximum profit
 to cost are different, though some time the name "maximum cycle mean" is used to
 refer to the maximum profit to cost ratio.
 <p>
 For a detailed mathematical description of the problem, refer to:
 <p>
 Ali Dasdan , Sandy S. Irani , Rajesh K. Gupta, Efficient algorithms for optimum
 cycle mean and optimum cost to time ratio problems,
 Proceedings of the 36th ACM/IEEE conference on Design automation conference,
 p.37-42, June 21-25, 1999, New Orleans, Louisiana, United States
 <p>

 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @version $Id$
 @author Shahrooz Shahparnia
 @see ptolemy.graph.analysis.MaximumProfitToCostRatioAnalysis
 */
public class CycleMeanAnalysis extends Analysis {
    /** Construct a maximum cycle mean analysis associated with a graph with a
     *  default analyzer.
     *
     *  @param graph The given graph.
     *  @param edgeLengths  The lengths associated with the edges of the graph.
     */
    public CycleMeanAnalysis(Graph graph, ToDoubleMapping edgeLengths) {
        super(new KarpCycleMeanStrategy(graph, edgeLengths));
    }

    /** Construct a maximum cycle mean analysis associated with a graph with a
     *  given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public CycleMeanAnalysis(CycleMeanAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the nodes on the cycle that corresponds to the maximum/minimum
     *  cycle mean as an ordered list. If there is more than one cycle with the
     *  same maximal/minimal cycle mean, one of them is returned randomly,
     *  but the same cycle is returned by different invocations of the method,
     *  unless the graph changes.
     *
     *  @return The nodes on the cycle that corresponds to one of the
     *  maximum/minimum cycle means as an ordered list.
     */
    public List cycle() {
        return ((CycleMeanAnalyzer) analyzer()).cycle();
    }

    /** Return the maximum cycle mean value.
     *
     *  @return The maximum cycle mean value.
     */
    public double maximumCycleMean() {
        return ((CycleMeanAnalyzer) analyzer()).maximumCycleMean();
    }

    /** Return minimum cycle mean value.
     *
     *  @return The minimum cycle mean value.
     */
    public double minimumCycleMean() {
        return ((CycleMeanAnalyzer) analyzer()).minimumCycleMean();
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    @Override
    public String toString() {
        return "Cycle mean analysis using the following analyzer:\n"
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
        return analyzer instanceof CycleMeanAnalyzer;
    }
}
