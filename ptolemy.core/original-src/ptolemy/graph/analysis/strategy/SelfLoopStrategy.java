/* Computation of self-loops in a graph.

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.analysis.analyzer.SelfLoopAnalyzer;

///////////////////////////////////////////////////////////////////
//// SelfLoopAnalyzer

/**
 Computation of self-loops in a graph.
 The returned collection cannot be modified.
 <p>
 This analysis requires <em>O</em>(<em>E</em>) time, where <em>E</em> is the
 number of edges in the graph.
 <p>
 @see ptolemy.graph.analysis.SelfLoopAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (ssb)
 @Pt.AcceptedRating Red (ssb)
 @author Shuvra S. Bhattacharyya, Shahrooz Shahparnia
 @version $Id$
 */
public class SelfLoopStrategy extends CachedStrategy implements
SelfLoopAnalyzer {
    /** Construct an instance of this strategy for a given graph.
     *
     *  @param graph The given graph.
     */
    public SelfLoopStrategy(Graph graph) {
        super(graph);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute the self-loop edges in the graph in the form of
     *  a collection. Each element of the collection is an {@link Edge}.
     *  @return The self-loop edges.
     */
    @Override
    public List edges() {
        return (List) _result();
    }

    /** Return a description of the analyzer.
     *
     *  @return Return a description of the analyzer..
     */
    @Override
    public String toString() {
        return "Ordinary Self-loop analyzer.\n";
    }

    /** Check for validity of this strategy.
     *
     *  @return True since this strategy is always valid.
     */
    @Override
    public boolean valid() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compute the self-loop edges in the graph in the form of
     *  a collection. Each element of the collection is an {@link Edge}.
     *
     *  @return The self-loop edges.
     */
    @Override
    protected Object _compute() {
        ArrayList selfLoopEdges = new ArrayList();
        Iterator edges = graph().edges().iterator();

        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();

            if (edge.isSelfLoop()) {
                selfLoopEdges.add(edge);
            }
        }

        return selfLoopEdges;
    }

    /** Return the result of this analysis (collection of self-loop edges)
     *  in a form that cannot be modified.
     *
     *  @return The analysis result in unmodifiable form.
     */
    protected Object _convertResult() {
        return Collections.unmodifiableList((List) _result());
    }
}
