/* Computation of self-loops in a graph.

 Copyright (c) 2002 The University of Maryland. All rights reserved.
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

@ProposedRating Red (shahrooz@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)

*/

package ptolemy.graph.analysis;

import java.util.List;

import ptolemy.graph.Graph;
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.SelfLoopAnalyzer;
import ptolemy.graph.analysis.strategy.SelfLoopStrategy;

//////////////////////////////////////////////////////////////////////////
//// SelfLoopAnalysis
/**
Computation of self-loops in a graph.
A self-loop (also called a self-loop edge) in a graph is an
edge whose source and sink nodes are identical.
<p>
The returned collection cannot be modified when the client uses the default
analyzer.
<p>

@since Ptolemy II 2.0
@author Shahrooz Shahparnia
@version $Id$
*/

public class SelfLoopAnalysis extends Analysis {

    /** Construct an instance of this class for a given graph.
     *
     *  @param graph The given graph.
     */
    public SelfLoopAnalysis(Graph graph) {
        super(new SelfLoopStrategy(graph));;
    }

    /** Construct an instance of this class with a given analyzer.
     *
     *  @param analyzer The analyzer to use.
     */
    public SelfLoopAnalysis(SelfLoopAnalyzer analyzer) {
        super(analyzer);;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the self-loop edges in the graph under analysis.
     *  Each element of the list is an {@link ptolemy.graph.Edge}.
     *
     *  @return Return the self-loop edges.
     */
    public List edges() {
        return ((SelfLoopAnalyzer)analyzer()).edges();
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    public String toString() {
        return "Self loop analysis using the following analyzer:\n"
                + analyzer().toString();
    }

    /** Check if a given analyzer is compatible with this analysis.
     *  In other words if it is possible to use it to compute the computation
     *  associated with this analysis.
     *
     *  @param analyzer The given analyzer.
     *  @return True if the given analyzer is valid for this analysis.
     */
    public boolean validAnalyzerInterface(Analyzer analyzer) {
        return analyzer instanceof SelfLoopAnalyzer;
    }
}
