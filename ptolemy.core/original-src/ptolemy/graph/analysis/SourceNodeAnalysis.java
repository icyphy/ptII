/* Computation of source nodes in a graph.

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

import java.util.List;

import ptolemy.graph.Graph;
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.SourceNodeAnalyzer;
import ptolemy.graph.analysis.strategy.SourceNodeStrategy;

///////////////////////////////////////////////////////////////////
//// SourceNodeAnalysis

/**
 Computation of source nodes in a graph.
 A source node in a graph is a node without input edges.
 <p>
 The returned collection cannot be modified when the client uses the default
 analyzer.
 <p>
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class SourceNodeAnalysis extends Analysis {
    /** Construct an instance of this class for a given graph.
     *
     *  @param graph The given graph.
     */
    public SourceNodeAnalysis(Graph graph) {
        super(new SourceNodeStrategy(graph));
    }

    /** Construct an instance of this class using a given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public SourceNodeAnalysis(SourceNodeAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the source nodes in the graph under analysis.
     *  Each element of the collection is an {@link ptolemy.graph.Node}.
     *
     *  @return Return the source nodes.
     */
    public List nodes() {
        return ((SourceNodeAnalyzer) analyzer()).nodes();
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    @Override
    public String toString() {
        return "Source node analysis using the following analyzer:\n"
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
        return analyzer instanceof SourceNodeAnalyzer;
    }
}
