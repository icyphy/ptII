/* Computation of source nodes in a graph.

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

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SourceNodeAnalysis
/** Computation of source nodes in a graph.

    A <em>source node</em> in a graph is a node without input edges. The
    <code>result</code> method (see {@link Analysis#result()})
    of this analysis returns the source nodes in the associated graph.
    The source nodes are returned in the form of a {@link java.util.Collection},
    where each element in the collection is a {@link Node}. The collection
    returned cannot be modified.
    <p>
    This analysis requires <em>O</em>(<em>N</em>) time, where <em>N</em> is the
    number of nodes in the graph.

    @author Mingyung Ko
    @version $Id$
 */

public class SourceNodeAnalysis extends Analysis {

    /** Construct a source node analysis for a given graph.
     *  @param graph The given graph.
     */
    public SourceNodeAnalysis(Graph graph) {
        super(graph);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check compatibility of the class of graph. The given graph
     *  must be an instance of <code>DirectedGraph</code>
     *
     *  @param graph The given graph.
     *  @return True if the given graph is of class DirectedGraph.
     */
    public boolean compatible(Graph graph) {
        if (graph instanceof DirectedGraph)
            return true;
        else
            return false;
    }

    /** Return a description of incompatibility. The class of graph
     *  is checked for compatibility.
     *
     *  @param graph The given graph.
     *  @return A description of invalid graph class.
     */
    public String incompatibilityDescription(Graph graph) {
        String result = "The given graph (of class " +
            graph.getClass().getName() +
            ") is not an instance of DirectedGraph.";
        return result;
    }

    /** Return a description of source nodes.
     *
     *  @return A description of the source nodes.
     */
    public String toString() {
        return "Source node analysis for the following graph.\n"
            + graph().toString() + "The source nodes are:\n" + _cachedResult();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Compute the source nodes in the graph in the form of
     *  a collection. Each element of the collection is a {@link Node}.
     *  @return The source nodes.
     */
    protected Object _compute() {
        ArrayList sourceNodes = new ArrayList();
        Iterator nodes = graph().nodes().iterator();
        while (nodes.hasNext()) {
            Node node = (Node)nodes.next();
            if (((DirectedGraph)graph()).inputEdgeCount(node) == 0) {
                sourceNodes.add(node);
            }
        }
        return sourceNodes;
    }

    /** Return the result of this analysis (collection of source nodes)
     *  in a form that cannot be modified.
     *  @return The analysis result in unmodifiable form.
     */
    protected Object _convertResult() {
        return Collections.unmodifiableList((List)_cachedResult());
    }

}
