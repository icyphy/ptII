/* Analyzes a directed graph and detects the existence of cycles.

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

import ptolemy.graph.Graph;
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.CycleExistenceAnalyzer;
import ptolemy.graph.analysis.strategy.FloydWarshallCycleExistenceStrategy;

//////////////////////////////////////////////////////////////////////////
//// CycleExistenceAnalysis
/**
Analyzes a directed graph and detects the existence of cycles.
In other words, this analysis checks if a given directed graph has at least
one cycle or not. The default analyzer runs in O(N^3) in which N is the number
of nodes.

@since Ptolemy II 2.0
@author Shahrooz Shahparnia
@version $Id$
*/

public class CycleExistenceAnalysis extends Analysis {

    /** Construct an instance of this class for a given graph, using a
     *  default analyzer that runs in O(N^3) in which N is the number of nodes.
     *
     *  @param graph The given directed graph.
     */
    public CycleExistenceAnalysis(Graph graph) {
        super(new FloydWarshallCycleExistenceStrategy(graph));
    }

    /** Construct an instance of this class with a given analyzer.
     *
     *  @param analyzer The default Analyzer.
     */
    public CycleExistenceAnalysis(CycleExistenceAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check if the graph under analysis has at least one cycle.
     *
     *  @return True if the graph under analysis has at least one cycle.
     */
    public boolean hasCycle() {
        return ((CycleExistenceAnalyzer)analyzer()).hasCycle();
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    public String toString() {
        return "Cyclic existence analysis using the following analyzer:\n"
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
        return analyzer instanceof CycleExistenceAnalyzer;
    }
}
