/* Computation of self loops in a graph.

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

*/

package ptolemy.graph.analysis;

import ptolemy.graph.Edge;
import ptolemy.graph.Graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

//////////////////////////////////////////////////////////////////////////
//// SelfLoopAnalysis
/** Computation of self loops in a graph.

The <code>result</code> method
(see {@link #ptolemy.graph.analysis.Analysis.result()})
of this analysis returns the self loop edges in the associated graph.
The self loop edges are returned in the form of a 
{@link java.util.Collection}, where each element in the collection is an
{@link Edge}. The collection returned is safe in that modifications
to the returned collection do not affect the value cached
in this analysis.

@author Shuvra S. Bhattacharyya
@version $Id$
*/

// FIXME: this should be an abstract class.
public class SelfLoopAnalysis extends Analysis {

    /** Construct a self loop analysis for a given graph. 
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
        return "Self loop anlaysis for the following graph.\n"
                + graph().toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Compute the set of self-loop edges in the graph.
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

    protected Object _convertResult() {
        return Collections.unmodifiableList((List)_cachedResult);
    }

}
