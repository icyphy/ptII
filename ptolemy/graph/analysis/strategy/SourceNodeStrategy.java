/* Computation of source nodes in a graph.

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

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.analyzer.SourceNodeAnalyzer;

///////////////////////////////////////////////////////////////////
//// SourceNodeAnalysis

/**
 Computation of source nodes in a graph.
 The collection returned cannot be modified.
 <p>
 This analysis requires <em>O</em>(<em>N</em>) time, where <em>N</em> is the
 number of nodes in the graph.
 <p>
 @see ptolemy.graph.analysis.SourceNodeAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Ming Yung Ko, Shahrooz Shahparnia
 @version $Id$
 */
public class SourceNodeStrategy extends CachedStrategy implements
        SourceNodeAnalyzer {
    /** Construct an instance of this strategy for a given graph.
     *
     *  @param graph The given graph.
     */
    public SourceNodeStrategy(Graph graph) {
        super(graph);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute the source nodes in the graph in the form of
     *  a collection. Each element of the collection is a {@link Node}.
     *
     *  @return The source nodes.
     */
    @Override
    public List nodes() {
        return (List) _result();
    }

    /** Return a description of the analyzer.
     *
     *  @return Return a description of the analyzer..
     */
    @Override
    public String toString() {
        return "Ordinary Source-loop analyzer.\n";
    }

    /** Check compatibility of the class of graph. The given graph
     *  must be an instance of DirectedGraph.
     *
     *  @return True if the given graph is of class DirectedGraph.
     */
    @Override
    public boolean valid() {
        return graph() instanceof DirectedGraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compute the source nodes in the graph in the form of
     *  a collection. Each element of the collection is a {@link Node}.
     *
     *  @return The source nodes.
     */
    @Override
    protected Object _compute() {
        ArrayList sourceNodes = new ArrayList();
        Iterator nodes = graph().nodes().iterator();

        while (nodes.hasNext()) {
            Node node = (Node) nodes.next();

            if (((DirectedGraph) graph()).inputEdgeCount(node) == 0) {
                sourceNodes.add(node);
            }
        }

        return sourceNodes;
    }

    /** Return the result of this analysis (collection of source nodes)
     *  in a form that cannot be modified.
     *
     *  @return The analysis result in unmodifiable form.
     */
    protected Object _convertResult() {
        return Collections.unmodifiableList((List) _result());
    }
}
