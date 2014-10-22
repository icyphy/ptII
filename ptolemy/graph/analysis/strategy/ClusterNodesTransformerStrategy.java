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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.analyzer.ClusterNodesTransformer;

///////////////////////////////////////////////////////////////////
////  ClusterNodesTransformerStrategy

/**
 Strategy for cluster transformers for graphs. The nodes of a graph given
 in a collection are being removed (clustered) and all of them are replaced
 by a single node called super node.
 <p>

 @see ptolemy.graph.analysis.ClusterNodesAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia based on a method by Ming Yung Ko.
 @version $Id$
 */
public class ClusterNodesTransformerStrategy extends CachedStrategy implements
        ClusterNodesTransformer {
    /** Construct a clusterer for a given graph.
     *  @param graph The given graph.
     *  @param nodeCollection The collection of nodes to be clustered.
     *  @param superNode The super node that replaces the clustered nodes.
     */
    public ClusterNodesTransformerStrategy(Graph graph,
            Collection nodeCollection, Node superNode) {
        super(graph);
        _nodeCollection = nodeCollection;
        _superNode = superNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the clustered Graph.
     *
     *  @return Return the clustered Graph.
     */
    @Override
    public Graph clusterNodes() {
        return (Graph) _result();
    }

    /** Specify if this transformation has a mapping from the transformed
     *  version to the original version or not. This implementation does not.
     *
     *  @return True If the implementation of the transformer supports backward
     *  mapping.
     */
    @Override
    public boolean hasBackwardMapping() {
        return false;
    }

    /** Specify if this transformation has a mapping from the original
     *  version to the transformed version or not. This implementation does not.
     *
     *  @return True If the implementation of the transformer supports forward
     *  mapping.
     */
    @Override
    public boolean hasForwardMapping() {
        return false;
    }

    /** Unsupported operation.
     *
     *  @exception UnsupportedOperationException If this method is called
     *  in any case.
     */
    @Override
    public Object originalVersionOf(Object dummy) {
        throw new UnsupportedOperationException();
    }

    /** Unsupported operation.
     *
     *  @exception UnsupportedOperationException If this method is called
     *  in any case.
     */
    @Override
    public Object transformedVersionOf(Object dummy) {
        throw new UnsupportedOperationException();
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
        // When removing nodes and edges, we have to be careful about
        // concurrent modification problems with the associated iterators.
        Graph graph = graph();
        Graph subgraph = graph.subgraph(_nodeCollection);
        graph.addNode(_superNode);

        HashSet nodesToRemove = new HashSet(_nodeCollection);

        // Remove all edges that are inside the induced subgraph.
        Iterator edges = graph.edges().iterator();
        ArrayList removeList = new ArrayList();

        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();

            if (nodesToRemove.contains(edge.source())
                    && nodesToRemove.contains(edge.sink())) {
                removeList.add(edge);
            }
        }

        Iterator edgesToRemove = removeList.iterator();

        while (edgesToRemove.hasNext()) {
            graph.removeEdge((Edge) edgesToRemove.next());
        }

        // For each edge that connects a node Z outside the induced subgraph
        // to a node inside the subgraph, replace the edge that connects
        // Z to the super node.
        removeList.clear();

        ArrayList addList = new ArrayList();
        edges = graph.edges().iterator();

        while (edges.hasNext()) {
            Edge edge = (Edge) edges.next();
            Edge newEdge = null;

            if (nodesToRemove.contains(edge.source())) {
                if (edge.hasWeight()) {
                    newEdge = new Edge(_superNode, edge.sink(),
                            edge.getWeight());
                } else {
                    newEdge = new Edge(_superNode, edge.sink());
                }
            } else if (nodesToRemove.contains(edge.sink())) {
                if (edge.hasWeight()) {
                    newEdge = new Edge(edge.source(), _superNode,
                            edge.getWeight());
                } else {
                    newEdge = new Edge(edge.source(), _superNode);
                }
            }

            if (newEdge != null) {
                removeList.add(edge);
                addList.add(newEdge);
            }
        }

        // Add edges for super nodes.
        Iterator edgesToAdd = addList.iterator();

        while (edgesToAdd.hasNext()) {
            graph.addEdge((Edge) edgesToAdd.next());
        }

        // Remove old edges connecting members outside the subgraph to members
        // of N.
        edgesToRemove = removeList.iterator();

        while (edgesToRemove.hasNext()) {
            graph.removeEdge((Edge) edgesToRemove.next());
        }

        // Remove the nodes in the specified collection.
        Iterator nodes = _nodeCollection.iterator();

        while (nodes.hasNext()) {
            graph.removeNode((Node) nodes.next());
        }

        // Return the subgraph that was induced by the collection of nodes.
        return subgraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Node _superNode;

    private Collection _nodeCollection;
}
