/* A mirror transformations on graphs.

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

@ProposedRating Red (shahrooz@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)

*/

package ptolemy.graph.analysis;

import ptolemy.graph.Graph;
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.MirrorTransformer;
import ptolemy.graph.analysis.analyzer.Transformer;
import ptolemy.graph.analysis.strategy.MirrorTransformerStrategy;

//////////////////////////////////////////////////////////////////////////
//// MirrorTransformation
/**
A mirror transformations on graphs. Creates a mirror of this graph in the form
of the type of the associated graph.  The mirror and original graphs are
isomorphic(of same topology). However, node and edge objects of the mirror are
newly created and therefore not "equal" to those of the original graph.
<p>
To relate nodes and edges from the original and the mirrored graph
the {@link #transformedVersionOf} and {@link #originalVersionOf} methods are
provided.
Labels can also be used to relate mirror and original
nodes(edges).
<p>
In the {@link #cloneWeight} method, users can also specify whether to clone node
and edge weights. For non cloneable
weights a {@link java.lang.CloneNotSupportedException} will be thrown by
the virtual machine.

@since Ptolemy II 2.0
@author Shahrooz Shahparnia
@version $Id$
*/
public class MirrorTransformation extends Analysis {

    /** Construct a transformation for a given graph with a default analyzer.
     *  The default constructor runs in O(N+E) in which N is the number of
     *  nodes in the graph and E is the number of edges in the graph.
     *
     *  @param graph The given graph.
     */
    public MirrorTransformation(Graph graph) {
        super(new MirrorTransformerStrategy(graph));
    }

    /** Construct a transformation for a given graph and a given analyzer.
     *
     *  @param analyzer The default Analyzer.
     */
    public MirrorTransformation(MirrorTransformer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Changes the status of the graph returned by the {@link #mirror} method.
     *  If true, the weights will also be cloned in the next calls to the
     *  {@link #mirror} method.
     *
     *  @param status If true, the weights will also be cloned.
     */
    public void cloneWeight(boolean status) {
        ((MirrorTransformer)analyzer())
                .cloneWeight(status);
    }

    /** Specify if this transformation has a mapping from the transformed
     *  version to the original version or not.
     *
     *  @return True if the implementation of the transformer supports backward
     *  mapping.
     */
    public boolean hasBackwardMapping() {
        return ((MirrorTransformer)analyzer())
                .hasBackwardMapping();
    }

    /** Specify if this transformation has a mapping from the original
     *  version to the transformed version or not.
     *
     *  @return True if the implementation of the transformer supports forward
     *  mapping.
     */
    public boolean hasForwardMapping() {
        return ((MirrorTransformer)analyzer())
                .hasForwardMapping();
    }

    /** Create a mirror of the graph associated with this analyzer with the
     *  same runtime class.
     *
     *  @return The mirror graph.
     */
    public Graph mirror() {
        return ((MirrorTransformer)analyzer())
                .mirror();
    }

    /** Return a mirror of this graph in the form of the argument graph type
     *  (i.e., the run-time type of the returned graph is that of the
     *  argument graph).
     *  <p>
     *  In this method, users can also specify whether to clone node and
     *  edge weights.
     *
     *  @return The mirror graph.
     */
    public Graph mirror(Graph graph, boolean cloneWeights) {
        return ((MirrorTransformer)analyzer())
                .mirror(graph, cloneWeights);
    }

    /** Return the original version of given object in the transformed graph.
     *
     *  @param transformedObject The given object in the transformed graph.
     *  @return Return the original version the given object.
     */
    public Object originalVersionOf(Object transformedObject) {
        return ((Transformer)analyzer()).originalVersionOf(transformedObject);
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    public String toString() {
        return "Mirror transformation using the following analyzer:\n"
                + analyzer().toString();
    }

    /** Return the transformed version of a given object in the original graph.
     *
     *  @param originalObject The given object in the original graph.
     *  @return Return the transformed version of the given object.
     */
    public Object transformedVersionOf(Object originalObject) {
        return ((Transformer)analyzer()).transformedVersionOf(originalObject);
    }

    /** Check if a given analyzer is compatible with this analysis.
     *  In other words if it is possible to use it to compute the computation
     *  associated with this analysis.
     *
     *  @param analyzer The given analyzer.
     *  @return True if the given analyzer is valid for this analysis.
     */
    public boolean validAnalyzerInterface(Analyzer analyzer) {
        return analyzer instanceof MirrorTransformer;
    }
}



