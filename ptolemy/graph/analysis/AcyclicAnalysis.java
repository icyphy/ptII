/* Computation of acyclic property of a directed graph.

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

//////////////////////////////////////////////////////////////////////////
//// AcyclicAnalysis
/** Computation of acyclic property of a directed graph.

    A {@link TransitiveClosureAnalysis} is used internally to check acyclic
    property.

    @author Mingyung Ko
    @version $Id$
 */

public class AcyclicAnalysis extends Analysis {

    /** Construct an acyclic analysis for a given directed graph.
     *  @param graph The given directed graph.
     */
    public AcyclicAnalysis(Graph graph) {
        super(graph);
        _transitiveClosureAnalysis = new TransitiveClosureAnalysis(graph);
    }

    /** Construct an acyclic analysis for a given directed graph and an
     *  existent {@link TransitiveClosureAnalysis}.
     *  @param graph The given directed graph.
     *  @param closure The given transitive closure analysis.
     */
    public AcyclicAnalysis(Graph graph, TransitiveClosureAnalysis closure) {
        super(graph);
        _transitiveClosureAnalysis = closure;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Check compatibility for between the analysis and the given
     *  graph. The graph class of <code>DirectedGraph</code> is checked
     *  here for compatibility.
     *
     *  @param graph The given graph.
     *  @return True if the graph is a <code>DirectedGraph</code>.
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

    /** Return a description of the analysis. This method
     *  simply returns a description of the associated graph.
     *  It should be overridden in derived classes to
     *  include details associated with the associated analyses.
     *
     *  @return A description of the analysis.
     */
    public String toString() {
        return "Acyclic checking for the following graph.\n"
            + graph().toString();
    }

    /** Return the associated transitive closure in the form of two
     *  dimensional array.
     *
     *  @return The associated transitive closure.
     */
    public boolean[][] transitiveClosure() {
        return (boolean[][])_transitiveClosureAnalysis.result();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Check acyclic property of the graph.
     *
     *  @return <code>True</code> if acyclic; <code>false</code> otherwise.
     */
    protected Object _compute() {
        boolean[][] transitiveClosure =
            (boolean[][])_transitiveClosureAnalysis.result();
        boolean acyclic = true;
        for (int i = 0; i < transitiveClosure.length; i++) {
            if (transitiveClosure[i][i] == true) {
                acyclic = false;
                break;
            }
        }
        return new Boolean(acyclic);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // an internal transitive closure analysis
    private TransitiveClosureAnalysis _transitiveClosureAnalysis;

}
