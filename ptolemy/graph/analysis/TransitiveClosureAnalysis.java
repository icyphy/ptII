/* Computation of transitive closure of a directed graph.

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
import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// TransitiveClosureAnalysis
/** Computation of transitive closure in a directed graph.

    <em>Transitive closure</em> is a reachability graph transformed from a given
    graph. While there is a path directed from node X to Y in the given graph,
    there is an edge from X to Y in the transformed graph. Generally, transitive
    closure is expressed in terms of square matrix with graph node labels as
    indices. The {@link #result()} method returns an <code>ArrayList</code> of
    rows (also in <code>ArrayList</code>) to represent the matrix.

    <p>
    The implementation uses Warshall's algorithm, which can be found in chapter
    6 of "Discrete Mathematics and Its Applications," 3rd Ed., by K. H. Rosen.
    The complexity of this algorithm is O(N^3), where N is the number of nodes.

    <p>
    Transitive closure is important in obtaining reachability information.
    One application is to check if a graph is cyclic.

    @author Mingyung Ko
    @version $Id$
 */

public class TransitiveClosureAnalysis extends Analysis {

    /** Construct a transitive closure analysis for a given directed graph.
     *  @param graph The given directed graph.
     */
    public TransitiveClosureAnalysis(Graph graph) {
        super(graph);
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
        return "Transitive closure analysis for the following graph.\n"
            + graph().toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Compute the transitive closure of the graph in the form of
     *  two dimensional array. The first dimension represents source
     *  node label while the second one represents sink node label.
     *
     *  @return The transitive closure in the form of 2D array.
     */
    protected Object _compute() {
        int size = graph().nodeCount();

        // Initialize transitiveClosure to the adjacency matrix
        boolean transitiveClosure[][] = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                transitiveClosure[i][j] = false;
            }
            Node node = graph().node(i);
            Iterator outputEdges =
                ((DirectedGraph)graph()).outputEdges(node).iterator();
            while (outputEdges.hasNext()) {
                int sinkLabel = ((DirectedGraph)graph()).
                    nodeLabel(((Edge)outputEdges.next()).sink());
                transitiveClosure[i][sinkLabel] = true;
            }
        }

        // Warshall's algorithm
        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    transitiveClosure[i][j] |= transitiveClosure[i][k] &
                        transitiveClosure[k][j];
                }
            }
        }

        return transitiveClosure;
    }

}
