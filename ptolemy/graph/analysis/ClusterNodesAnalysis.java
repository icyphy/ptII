/* Computation of clusters in a graph.

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

import java.util.Collection;

import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.analysis.analyzer.Analyzer;
import ptolemy.graph.analysis.analyzer.ClusterNodesTransformer;
import ptolemy.graph.analysis.strategy.ClusterNodesTransformerStrategy;

//////////////////////////////////////////////////////////////////////////
//// ClusterNodesAnalysis
/**
Given a collection of nodes in a graph, replace the subgraph induced by
the nodes with a single node N. Each edge that connects a node Z
outside the subgraph a node inside the subgraph is replaced by
an edge (with the same edge weight, if there is one) that connects
Z to N. Return the subgraph that is replaced;
that is, return the subgraph induced by the given collection of nodes.

@since Ptolemy II 2.0
@version $Id$
@author Shahrooz Shahparnia based on a file by Shuvra S. Bhattacharyya and
Ming-Yung Ko
@see ptolemy.graph.Graph
*/

public class ClusterNodesAnalysis extends Analysis {

    /** Construct an instance of this class for a given graph.
     *  Given a collection of nodes in a graph, replace the subgraph induced by
     *  the nodes with a single node N. Each edge that connects a node Z
     *  outside the subgraph a node inside the subgraph is replaced by
     *  an edge (with the same edge weight, if there is one) that connects
     *  Z to N. Return the subgraph that is replaced;
     *  that is, return the subgraph induced by the given collection of nodes.
     *  @param graph The graph.
     *  @param nodeCollection The collection of nodes.
     *  @param superNode The node that replaces the subgraph.
     */
    public ClusterNodesAnalysis(Graph graph, Collection nodeCollection,
            Node superNode) {
        super(new ClusterNodesTransformerStrategy(graph, nodeCollection,
                superNode));
    }

    /** Construct an instance of this class with a given analyzer.
     *
     *  @param analyzer The analyzer to use.
     */
    public ClusterNodesAnalysis(ClusterNodesTransformer analyzer) {
        super(analyzer);;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the clustered Graph.
     *
     *  @return Return the clustered Graph.
     */
    public Graph clusterNodes() {
        return ((ClusterNodesTransformer)analyzer()).clusterNodes();
    }

    /** Return a description of the analysis and the associated analyzer.
     *
     *  @return A description of the analysis and the associated analyzer.
     */
    public String toString() {
        return "Cluster node analysis using the following analyzer:\n"
                + analyzer().toString();
    }

    /** Check if a given analyzer is compatible with this analysis.
     *  In other words if it is possible to use it to compute the computation
     *  associated with this analysis.
     *
     *  @param analyzer The given analyzer.
     *  @return True if the given analyzer is valid for this analysis.
     */
    public boolean validAnalyzerInterface(Analyzer analyzer) {
        return analyzer instanceof ClusterNodesTransformer;
    }
}
