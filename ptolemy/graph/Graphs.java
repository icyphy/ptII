/* Utilities for working with graphs.

   Copyright (c) 2001-2002 The University of Maryland.
   All rights reserved.
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

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

   @ProposedRating Red (ssb@eng.umd.edu)
   @AcceptedRating Red (ssb@eng.umd.edu)

 */

package ptolemy.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Graphs
/**
   Utilities for working with graphs.

   @author Shuvra S. Bhattacharyya, Ming-Yung Ko
   @version $Id$
   @since Ptolemy II 2.0
   @see ptolemy.graph.Graph
 */
public class Graphs {

    // Private constructor to prevent instantiation of the class
    private Graphs() {
    }

    /** Given a collection of nodes in a graph, replace the subgraph induced by
     *  the nodes with a single node N. Each edge that connects a node Z
     *  outside the subgraph a node inside the subgraph is replaced by
     *  an edge (with the same edge weight, if there is one) that connects
     *  Z to N. Return the subgraph that is replaced;
     *  that is, return the subgraph induced by the given collection of nodes.
     *  @param graph The graph.
     *  @param nodeCollection The collection of nodes.
     *  @param superNode The node that replaces the subgraph.
     *  @return The subgraph that is replaced.
     */
    public static Graph clusterNodes(Graph graph, Collection nodeCollection,
            Node superNode) {

        // When removing nodes and edges, we have to be careful about
        // concurrent modification problems with the associated iterators.

        Graph subgraph = graph.subgraph(nodeCollection);
        graph.addNode(superNode);
        HashSet nodesToRemove = new HashSet(nodeCollection);

        // Remove all edges that are inside the induced subgraph.
        Iterator edges = graph.edges().iterator();
        ArrayList removeList = new ArrayList();
        while (edges.hasNext()) {
            Edge edge = (Edge)(edges.next());
            if (nodesToRemove.contains(edge.source()) &&
                    nodesToRemove.contains(edge.sink())) {
                removeList.add(edge);
            }
        }
        Iterator edgesToRemove = removeList.iterator();
        while (edgesToRemove.hasNext()) {
            graph.removeEdge((Edge)edgesToRemove.next());
        }

        // For each edge that connects a node Z outside the induced subgraph
        // to a node inside the subgraph, replace the edge that connects
        // Z to the super node.
        removeList.clear();
        ArrayList addList = new ArrayList();
        edges = graph.edges().iterator();
        while (edges.hasNext()) {
            Edge edge = (Edge)edges.next();
            Edge newEdge = null;
            if (nodesToRemove.contains(edge.source())) {
                if (edge.hasWeight()) {
                    newEdge = new Edge(superNode, edge.sink(),
                            edge.getWeight());
                } else {
                    newEdge = new Edge(superNode, edge.sink());
                }
            } else if (nodesToRemove.contains(edge.sink())) {
                if (edge.hasWeight()) {
                    newEdge = new Edge(edge.source(), superNode,
                            edge.getWeight());
                } else {
                    newEdge = new Edge(edge.source(), superNode);
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
            graph.addEdge((Edge)edgesToAdd.next());
        }

        // Remove old edges connecting members outside the subgraph to members
        // of N.
        edgesToRemove = removeList.iterator();
        while (edgesToRemove.hasNext()) {
            graph.removeEdge((Edge)edgesToRemove.next());
        }

        // Remove the nodes in the specified collection.
        Iterator nodes = nodeCollection.iterator();
        while (nodes.hasNext()) {
            graph.removeNode((Node)nodes.next());
        }

        // Return the subgraph that was induced by the collection of nodes.
        return subgraph;
    }



}
