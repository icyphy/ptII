/* Analyzer to check if a given directed graph has a zero length cycle using the
 Floyd-Warshall all pair shortest path algorithm.

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
import ptolemy.graph.analysis.analyzer.ZeroLengthCycleAnalyzer;
import ptolemy.graph.mapping.ToDoubleMapping;

///////////////////////////////////////////////////////////////////
//// FloydWarshallZeroLengthCycleStrategy

/**
 Analyzer to check if a given directed graph has a zero cycle using the
 Floyd-Warshall all pair shortest path algorithm.
 <p>
 @see ptolemy.graph.analysis.ZeroLengthCycleAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia, Nimish Sane
 @version $Id$
 */
public class FloydWarshallZeroLengthCycleStrategy extends CachedStrategy
        implements ZeroLengthCycleAnalyzer {
    /** Constructs negative cycle detection analyzer for a given graph and
     *  given edge values.
     *
     *  @param graph The given graph.
     *  @param edgeLengths The lengths associated with the given graph.
     */
    public FloydWarshallZeroLengthCycleStrategy(Graph graph,
            ToDoubleMapping edgeLengths) {
        super(graph);
        _edgeLengths = edgeLengths;
        _strategy = new FloydWarshallAllPairShortestPathStrategy(graph,
                _edgeLengths);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if a zero length cycle exists in the graph under analysis.
     *
     *  @return True if the graph has a zero length cycle.
     */
    @Override
    public boolean hasZeroLengthCycle() {
        return ((Boolean) _result()).booleanValue();
    }

    /** Return a description of the analyzer.
     *
     *  @return Return a description of the analyzer..
     */
    @Override
    public String toString() {
        return "Zero Length analyzer"
                + " based on the Floyd-Warshall algorithm.";
    }

    /** Check for compatibility between the analysis and the given
     *  graph. A graph needs to be an instance of a DirectedGraph in order
     *  to use this algorithm.
     *
     *  @return True if the graph is a directed and cyclic graph.
     */
    @Override
    public boolean valid() {
        return graph() instanceof DirectedGraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** The computation associated with the Floyd-Warshall algorithm.
     *
     *  @return Return a true {@link Boolean} {@link Object} if the graph has
     *  a negative cycle.
     */
    @Override
    protected Object _compute() {
        double[][] allPairShortestPath = _strategy.shortestPathMatrix();
        boolean zeroCycle = false;
        int n = graph().nodeCount();

        for (int i = 0; i < n; i++) {
            if (allPairShortestPath[i][i] == 0) {
                zeroCycle = true;
                break;
            }
        }

        return Boolean.valueOf(zeroCycle);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The transitive closure analyzer used to check the existence of a zero
    // length cycle in the associated graph.
    private FloydWarshallAllPairShortestPathStrategy _strategy;

    private ToDoubleMapping _edgeLengths;
}
