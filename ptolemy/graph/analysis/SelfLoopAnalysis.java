/* Computation of self-loops in a graph.

   Copyright (c) 2002-2003 The University of Maryland. All rights reserved.
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

   @ProposedRating Red (cxh@eecs.berkeley.edu)
   @AcceptedRating Red (cxh@eecs.berkeley.edu)

 */

package ptolemy.graph.analysis;

import ptolemy.graph.Edge;
import ptolemy.graph.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SelfLoopAnalysis
/** Computation of self-loops in a graph.

    A <em>self-loop</em> (also called a <em>self-loop edge</em>) in a graph is an
    edge whose source and sink nodes are identical.  The <code>result</code> method
    (see {@link Analysis#result()})
    of this analysis returns the self-loop edges in the associated graph.
    The self-loop edges are returned in the form of a
    {@link java.util.Collection}, where each element in the collection is an
    {@link Edge}. The collection returned cannot be modified.
    <p>
    This analysis requires <em>O</em>(<em>E</em>) time, where <em>E</em> is the
    number of edges in the graph.

    @author Shuvra S. Bhattacharyya
    @version $Id$
 */

public class SelfLoopAnalysis extends Analysis {

    /** Construct a self-loop analysis for a given graph.
     *  @param graph The given graph.
     */
    public SelfLoopAnalysis(Graph graph) {
        super(graph);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a description of the analysis. This method
     *  simply returns a description of the associated graph.
     *  It should be overridden in derived classes to
     *  include details associated with the associated analyses.
     *
     *  @return A description of the analysis.
     */
    public String toString() {
        return "Self-loop analysis for the following graph.\n"
            + graph().toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Compute the self-loop edges in the graph in the form of
     *  a collection. Each element of the collection is an {@link Edge}.
     *  @return The self-loop edges.
     */
    protected Object _compute() {
        ArrayList selfLoopEdges = new ArrayList();
        Iterator edges = graph().edges().iterator();
        while (edges.hasNext()) {
            Edge edge = (Edge)edges.next();
            if (edge.isSelfLoop()) {
                selfLoopEdges.add(edge);
            }
        }
        return selfLoopEdges;
    }

    /** Return the result of this analysis (collection of self-loop edges)
     *  in a form that cannot be modified.
     *  @return The analysis result in unmodifiable form.
     */
    protected Object _convertResult() {
        return Collections.unmodifiableList((List)_cachedResult());
    }

}
