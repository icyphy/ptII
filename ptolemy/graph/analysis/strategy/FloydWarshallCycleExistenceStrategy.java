/* Computation of cycle existence in directed graphs using an all pair shortest
 path algorithm based on the Floyd-Warshall algorithm.

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
package ptolemy.graph.analysis.strategy;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Graph;
import ptolemy.graph.analysis.analyzer.CycleExistenceAnalyzer;

///////////////////////////////////////////////////////////////////
//// FloydWarshallCycleExistenceStrategy

/**
 Computation of cycle existence in directed graphs using an all pair shortest
 path algorithm based on the Floyd-Warshall algorithm.
 The complexity of this algorithm is O(N^3), where N is the number of nodes.
 <p>
 @see ptolemy.graph.analysis.CycleExistenceAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public class FloydWarshallCycleExistenceStrategy extends CachedStrategy
        implements CycleExistenceAnalyzer {
    /** Construct an instance of this analyzer for a given graph.
     *
     *  @param graph The given graph.
     */
    public FloydWarshallCycleExistenceStrategy(Graph graph) {
        super(graph);
        _strategy = new FloydWarshallTransitiveClosureStrategy(graph());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check acyclic property of the graph.
     *
     *  @return True if cyclic.
     */
    @Override
    public boolean hasCycle() {
        return ((Boolean) _result()).booleanValue();
    }

    /** Return a description of the analyzer.
     *
     *  @return Return a description of the analyzer.
     */
    @Override
    public String toString() {
        return "Cycle existence analyzer"
                + " based on the Floyd-Warshall algorithm.";
    }

    /** Check for compatibility between the analysis and the given
     *  graph. A graph needs to be an instance of a {@link DirectedGraph}
     *  in order to use this algorithm.
     *
     *  @return True if the graph is a directed graph.
     */
    @Override
    public boolean valid() {
        return graph() instanceof DirectedGraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** The computation associated with the Floyd-Warshall algorithm.
     *
     *  @return Return a true {@link Boolean} {@link Object} if the graph is
     *  cyclic.
     */
    @Override
    protected Object _compute() {
        boolean cyclic = false;
        boolean[][] transitiveClosure = _strategy.transitiveClosureMatrix();

        for (int i = 0; i < transitiveClosure.length; i++) {
            if (transitiveClosure[i][i] == true) {
                cyclic = true;
                break;
            }
        }

        return Boolean.valueOf(cyclic);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The transitive closure analyzer used to check the existence of a cycle
    // in the associated graph.
    private FloydWarshallTransitiveClosureStrategy _strategy;
}
