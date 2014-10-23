/* Computation of transitive closure of a directed graph using the
 Floyd-Warshall algorithm.

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

import java.util.Arrays;
import java.util.Iterator;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.analyzer.TransitiveClosureAnalyzer;

///////////////////////////////////////////////////////////////////
//// FloydWarshallTransitiveClosureStrategy

/**
 Computation of transitive closure of a directed graph using the
 Floyd-Warshall algorithm described in:
 Thomas H. Cormen, Charles E. Leiserson and Ronald L. Rivest:
 Introduction to Algorithms. Cambridge: MIT Press, 1990.
 <p>
 The complexity of this algorithm is O(N^3), where N is the number of nodes.
 <p>
 @see ptolemy.graph.Graph#nodeLabel
 @see ptolemy.graph.analysis.TransitiveClosureAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia based on an initial implementation by Ming Yung Ko.
 @version $Id$
 */
public class FloydWarshallTransitiveClosureStrategy extends
FloydWarshallStrategy implements TransitiveClosureAnalyzer {
    /** Construct a transitive closure analysis for a given directed graph.
     *  @param graph The given directed graph.
     */
    public FloydWarshallTransitiveClosureStrategy(Graph graph) {
        super(graph);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check if there exist a path between a starting node and an ending node
     *  on the analyzer's graph.
     *
     *  @param startNode The starting node.
     *  @param endNode The ending node.
     *  @return True if such a path exists.
     */
    @Override
    public boolean pathExistence(Node startNode, Node endNode) {
        return _transitiveClosure[graph().nodeLabel(startNode)][graph()
                                                                .nodeLabel(endNode)];
    }

    /** Return a description of the analyzer.
     *
     *  @return Return a description of the analyzer..
     */
    @Override
    public String toString() {
        return "Transitive closure analyzer"
                + " based on the Floyd-Warshall algorithm.";
    }

    /** Compute the transitive closure of the graph under analysis in the
     *  form of two dimensional array. The first dimension represents
     *  source node label while the second one represents sink node label.
     *  Assume i and j are labels of two nodes.
     *  transitiveClosureMatrix()[i][j] is true if there is a path on the graph
     *  from "i" to "j".
     *
     *  @return The transitive closure in the form of 2D array.
     */
    @Override
    public boolean[][] transitiveClosureMatrix() {
        return (boolean[][]) _result();
    }

    /** Check for validity of this strategy.
     *  A graph needs to be an instance of a {@link DirectedGraph} in order
     *  to use this algorithm.
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
     *  @return Return the transitive closure matrix as an {@link Object}
     *  in order to be stored in the result-cache.
     */
    @Override
    protected Object _compute() {
        int size = graph().nodeCount();

        // Initialize transitiveClosure to the adjacency matrix
        _transitiveClosure = new boolean[size][size];

        for (int i = 0; i < size; i++) {

            // For graphs of 300 nodes, Arrays.fill() is about 3%
            // faster than iterating through the row.
            Arrays.fill(_transitiveClosure[i], false);

            Node node = graph().node(i);
            Iterator outputEdges = ((DirectedGraph) graph()).outputEdges(node)
                    .iterator();

            while (outputEdges.hasNext()) {
                int sinkLabel = ((DirectedGraph) graph())
                        .nodeLabel(((Edge) outputEdges.next()).sink());
                _transitiveClosure[i][sinkLabel] = true;
            }
        }

        super._compute();
        return _transitiveClosure;
    }

    /** Overrides the computation associated with the implementation of the
     *  Floyd-Warshall algorithm, for the purpose of computing the transitive
     *  closure.
     */
    @Override
    protected void _floydWarshallComputation(int k, int i, int j) {
        _transitiveClosure[i][j] |= _transitiveClosure[i][k]
                & _transitiveClosure[k][j];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A reference to the result of the computation to be shared between the
    // two protected methods.
    private boolean[][] _transitiveClosure;
}
