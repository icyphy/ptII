/* A base class for analyses on graphs.

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
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.GraphAnalyzer;
import ptolemy.graph.analysis.strategy.CachedStrategy;

///////////////////////////////////////////////////////////////////
//// Analysis

/**
 A base class for analyses on graphs.
 <p>
 The organization of the package follows:
 <p>
 Classes in ptolemy.graph.analysis consists of different wrappers in
 which a client can plug a requested strategy/algorithm for an analysis.
 Strategies for a given analysis implement the same interface defined
 in ptolemy.graph.analysis.analyzer.
 Therefore from now on we will use the name analyzer for all the strategies
 that implement the same interface and therefore  solve the same problem.
 Analysis classes access the plugged-in strategy class through these interfaces.
 <p>
 In the base class methods are provided in order to dynamically change the
 analyzer of the current analysis and also to check if a given analyzer is
 applicable to the given analysis.
 <p>
 Analyzers that can be used in these analyses are a specialized version of
 analyzers called {@link ptolemy.graph.analysis.analyzer.GraphAnalyzer}.
 <p>
 Classes in ptolemy.graph.analysis.analyzer are the interfaces for
 different strategies(algorithms) used for the analysis. The strategies classes
 are defined in ptolemy.graph.analysis.strategy
 <p>
 In addition, the analysis classes provide default constructors which use
 predefined strategies for those clients who do not want to deal with different
 strategies.
 Although this introduces some limitations imposed by the used strategy. The
 documentation of such constructor will reflect the limitations, if any.
 <p>
 Finally, strategies can be instantiated and used independently. In this case
 the client will lose the possibility of dynamically changing the analyzer for
 the associated analysis, which would not exist at all, and there will be no
 default constructor therefore the client need to be familiar with the strategy
 that she/he is using.

 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia, Shuvra S. Bhattacharyya
 @version $Id$
 */
public class Analysis {
    /** Construct an analysis using a given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public Analysis(GraphAnalyzer analyzer) {
        _analyzer = analyzer;

        // Maybe we may want to implement the Observer pattern instead of this.
        graph().addAnalysis(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the analyzer associated with this analysis class.
     *
     *  @return Return the analyzer associated with this analysis class.
     */
    public GraphAnalyzer analyzer() {
        return _analyzer;
    }

    /** Change the analyzer associated with this analysis class to the given
     *  analyzer.
     *
     *  @param analyzer The given analyzer.
     *  @exception InvalidAnalyzerException If the analyzer is not a valid
     *  analyzer for this analysis.
     */
    public void changeAnalyzer(GraphAnalyzer analyzer) {
        if (validAnalyzerInterface(analyzer)) {
            if (analyzer instanceof CachedStrategy) {
                if (graph() == analyzer().graph()) {
                    ((CachedStrategy) analyzer)
                            .setCachedResult((CachedStrategy) _analyzer);
                }
            }

            _analyzer = analyzer;
        } else {
            throw new InvalidAnalyzerException(
                    "Invalid analyzer for the analysis:\n" + toString());
        }
    }

    /** The graph associated with the analysis. This association is made
     *  through the associated analyzer interface.
     *
     *  @return Return the graph under analysis.
     */
    public Graph graph() {
        return _analyzer.graph();
    }

    /** Return a description of the analysis and the associated analyzer.
     *  It should be overridden in derived classes to
     *  include details associated with the associated analysis/analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    @Override
    public String toString() {
        return "Analysis using the following analyzer:\n"
                + _analyzer.toString();
    }

    /** Return the validity of the associated analyzer. An analyzer is valid
     *  if the graph and the associated data is in a format suitable for the
     *  analyzer.
     *
     *  @return Return the validity of the associated analyzer.
     */
    public boolean valid() {
        return _analyzer.valid();
    }

    /** Check if a given analyzer is compatible with this analysis.
     *  In other words if it is possible to use it to compute the computation
     *  associated with this analysis.
     *  Derived classes should override this method to provide the valid type
     *  of analyzer that they need.
     *
     *  @param analyzer The given analyzer.
     *  @return True if the given analyzer is valid for this analysis.
     */
    public boolean validAnalyzerInterface(Analyzer analyzer) {
        return analyzer instanceof GraphAnalyzer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The analyzer that is used in the computation of this analysis.
    private GraphAnalyzer _analyzer;
}
