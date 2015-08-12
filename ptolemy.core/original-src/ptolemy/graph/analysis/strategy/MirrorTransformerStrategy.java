/* A mirror transformer for graphs.

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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.AnalysisException;
import ptolemy.graph.analysis.analyzer.MirrorTransformer;

///////////////////////////////////////////////////////////////////
//// MirrorTransformerStrategy

/**
 A mirror transformer for graphs.
 <p>
 In the {@link #cloneWeight} method, users can also specify whether to clone node
 and edge weights. For non cloneable
 weights a {@link java.lang.CloneNotSupportedException} will be thrown by
 the virtual machine.

 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia based on a method by Ming Yung Ko.
 @version $Id$
 */
public class MirrorTransformerStrategy extends CachedStrategy implements
MirrorTransformer {
    /** Construct a transformer for a given graph.
     *  @param graph The given graph.
     */
    public MirrorTransformerStrategy(Graph graph) {
        super(graph);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Changes the status of the graph returned by the {@link #mirror} method.
     *  If true, the weights will also be cloned in the next calls to the
     *  {@link #mirror} method.
     *
     *  @param status If true, the weights will also be cloned.
     */
    @Override
    public void cloneWeight(boolean status) {
        _cloneWeights = status;
    }

    /** Specify if this transformation has a mapping from the transformed
     *  version to the original version or not.
     *
     *  @return True If the implementation of the transformer supports backward
     *  mapping.
     */
    @Override
    public boolean hasBackwardMapping() {
        return true;
    }

    /** Specify if this transformation has a mapping from the original
     *  version to the transformed version or not.
     *
     *  @return True If the implementation of the transformer supports forward
     *  mapping.
     */
    @Override
    public boolean hasForwardMapping() {
        return true;
    }

    /** Create a mirror of the graph associated with this analyzer with the
     *  same runtime class.
     *
     *  @return The mirror graph.
     */
    @Override
    public Graph mirror() {
        return mirror(graph(), _cloneWeights);
    }

    /** Return a mirror of this graph in the form of the argument graph type
     *  (i.e., the run-time type of the returned graph is that of the
     *  argument graph).  The mirror and original graphs
     *  are isomorphic (of same topology). However, nodes and edges
     *  of the mirror are newly created and therefore not equal to
     *  those of the original graph.
     *  <p>
     *  The returned mirror graph has the same ordering(integer labeling)
     *  of nodes(edges) as the original graph. Therefore, correspondent
     *  nodes(edges) pairs in both graphs can be gotten through same labels.
     *  In other words, labels can also be used to relate mirror and original
     *  nodes(edges).
     *  <p>
     *
     *  @param graph The type of the graph which the graph associated with
     *  this analyzer is being mirrored to.
     *  @param cloneWeights If true, the weights will also be cloned.
     *  @return The mirror graph.
     */
    @Override
    public Graph mirror(Graph graph, boolean cloneWeights) {
        if (graph.getClass() != graph().getClass()
                || cloneWeights != _cloneWeights) {
            reset();
        }

        _graph = graph;

        boolean tempCloneWeights = _cloneWeights;
        _cloneWeights = cloneWeights;

        Graph result = (Graph) _result();
        _cloneWeights = tempCloneWeights;
        return result;
    }

    /** Return the original version of given object in the transformed graph.
     *
     *  @param transformedObject The given object in the transformed graph.
     *  @return Return the original version the given object.
     */
    @Override
    public Object originalVersionOf(Object transformedObject) {
        return _originalVersion.get(transformedObject);
    }

    /** Return the transformed version of a given object in the original graph.
     *
     *  @param originalObject The given object in the original graph.
     *  @return Return the transformed version the given object.
     */
    @Override
    public Object transformedVersionOf(Object originalObject) {
        return _transformedVersion.get(originalObject);
    }

    /** Always valid.
     *
     *  @return True always.
     */
    @Override
    public boolean valid() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** The computation associated with this strategy.
     *
     *  @return The mirror graph as an {@link Object}.
     */
    @Override
    protected Object _compute() {
        String nameClone = "clone";
        Graph mirrorGraph = null;

        try {
            // Kepler (jdk1.4?) requires this cast
            mirrorGraph = _graph.getClass().newInstance();
        } catch (Exception exception) {
            throw new RuntimeException("Could not create an empty graph from "
                    + "this one.\n" + exception + "\n");
        }

        // create new nodes for the mirror
        Iterator nodes = graph().nodes().iterator();

        while (nodes.hasNext()) {
            Node node = (Node) nodes.next();
            Node mirrorNode = null;

            if (!node.hasWeight()) {
                mirrorNode = new Node();
            } else {
                Object mirrorWeight = null;

                try {
                    // Clone weights of any type of object.
                    if (_cloneWeights) {
                        Object oldWeight = node.getWeight();

                        if (oldWeight instanceof Cloneable) {
                            /* Since clone() of Object is protected, it can't
                             be called publicly. The class Method is used
                             here to call public clone(). */
                            Class[] argumentTypes = {};
                            Method method = oldWeight.getClass().getMethod(
                                    nameClone, argumentTypes);

                            // Cast to (Object []) so as to avoid varargs call.
                            mirrorWeight = method.invoke(oldWeight,
                                    (Object[]) null);
                        } else {
                            throw new RuntimeException();
                        }
                    } else {
                        mirrorWeight = node.getWeight();
                    }
                } catch (Throwable throwable) {
                    /* Exception due to non-Cloneable weights or
                     weights without public clone(). */
                    throw new AnalysisException(
                            "Can not clone the node weight.\n", throwable);
                }

                mirrorNode = new Node(mirrorWeight);
            }

            mirrorGraph.addNode(mirrorNode);
            _originalVersion.put(mirrorNode, node);
            _transformedVersion.put(node, mirrorNode);
        }

        // create new edges for the mirror
        Iterator edges = graph().edges().iterator();

        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();
            Edge mirrorEdge = null;
            Node mirrorSource = (Node) _transformedVersion.get(edge.source());
            Node mirrorSink = (Node) _transformedVersion.get(edge.sink());

            if (!edge.hasWeight()) {
                mirrorEdge = new Edge(mirrorSource, mirrorSink);
            } else {
                Object mirrorWeight = null;

                try {
                    // Clone weights of any type of object.
                    if (_cloneWeights) {
                        Object oldWeight = edge.getWeight();

                        if (oldWeight instanceof Cloneable) {
                            /* Since clone() of Object is protected, it can't
                             be called publicly. The class Method is used
                             here to call public clone(). */
                            Class[] argumentTypes = {};
                            Method method = oldWeight.getClass().getMethod(
                                    nameClone, argumentTypes);

                            // Cast to (Object []) so as to avoid varargs call.
                            mirrorWeight = method.invoke(oldWeight,
                                    (Object[]) null);
                        } else {
                            throw new RuntimeException();
                        }
                    } else {
                        mirrorWeight = edge.getWeight();
                    }
                } catch (Throwable throwable) {
                    /* Exception due to non-Cloneable weights or
                     weights without public clone(). */
                    throw new RuntimeException(
                            "Can not clone the edge weight.\n", throwable);
                }

                mirrorEdge = new Edge(mirrorSource, mirrorSink, mirrorWeight);
            }

            mirrorGraph.addEdge(mirrorEdge);
            _originalVersion.put(mirrorEdge, edge);
            _transformedVersion.put(edge, mirrorEdge);
        }

        return mirrorGraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Graph _graph;

    private boolean _cloneWeights = false;

    private HashMap _originalVersion = new HashMap();

    private HashMap _transformedVersion = new HashMap();
}
