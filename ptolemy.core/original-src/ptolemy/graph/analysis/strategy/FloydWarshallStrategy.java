/* Base class for all the analysis based on a Floyd-Warshall like computation.

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

import ptolemy.graph.Graph;

///////////////////////////////////////////////////////////////////
//// FloydWarshallAnalysis

/**
 Base class for all the analysis based on a floyd-warshall like computation.
 This is an abstract class and cannot be instantiated.
 <p>
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
abstract public class FloydWarshallStrategy extends CachedStrategy {
    /** Construct an FloydWarshallStrategy.
     *  @param graph The given graph.
     */
    public FloydWarshallStrategy(Graph graph) {
        super(graph);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Basic computation performed by all the analysis implementing a
     *  floyd-warshall like analysis on a given graph.
     *  Derived classed need to override the (@link #_floydWarshallComputation}
     *  method of this class to provide the correct functionality.
     *  @return The analysis results.
     */
    @Override
    protected Object _compute() {
        int n = graph().nodeCount();
        Object floydWarshallResult = null;

        // Warshall's algorithm
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    _floydWarshallComputation(k, i, j);
                }
            }
        }

        return floydWarshallResult;
    }

    /** Derived classed need to override the _floydWarshallComputation method
     *  of this class to provide the correct functionality.
     *  @param k The counting parameter of the first loop of the Floyd-Warshall
     *  computation.
     *  @param i The counting parameter of the second loop of the Floyd-Warshall
     *  computation.
     *  @param j The counting parameter of the third and last loop of the
     *  Floyd-Warshall computation.
     */
    protected void _floydWarshallComputation(int k, int i, int j) {
    }
}
