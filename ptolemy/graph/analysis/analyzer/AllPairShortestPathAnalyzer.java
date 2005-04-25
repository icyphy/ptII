/*  An interface for all the analyzers that compute the all pair shortest path
    of a directed graph.

    Copyright (c) 2003-2005 The University of Maryland.
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

*/
package ptolemy.graph.analysis.analyzer;

import ptolemy.graph.Node;

import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// AllPairShortestPathAnalyzer

/**
   An interface for all the analyzers that compute the all pair shortest path of
   a directed graph.
   <p>
   @see ptolemy.graph.analysis.AllPairShortestPathAnalysis
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (shahrooz)
   @Pt.AcceptedRating Red (ssb)
   @author Shahrooz Shahparnia
   @version $Id$
*/
public interface AllPairShortestPathAnalyzer extends GraphAnalyzer {
    /** Return the nodes on the shortest path from the node
     *  "startNode" to the node "endNode" in the form of an ordered list.
     *
     *  @param startNode The starting node of the path.
     *  @param endNode The ending node of the path.
     *  @return Return the nodes on the shortest path from the
     *  node "startNode" to the node "endNode" in the form of an ordered list.
     */
    public List shortestPath(Node startNode, Node endNode);

    /** Return the length of the shortest path from the node
     *  startNode to the node endNode.
     *
     *  @param startNode The starting node of the path.
     *  @param endNode The end node of the path.
     *  @return Return the length of the shortest path from the node
     *  startNode to the node endNode.
     */
    public double shortestPathLength(Node startNode, Node endNode);

    /** A matrix representing the result of the all pair shortest path
     *  algorithm.
     *  The first dimension is indexed by the source node label while the
     *  second one is indexed by the sink node label.
     *
     *  @see ptolemy.graph.Graph#nodeLabel
     *  @return Return a matrix representing the result of the all pair shortest
     *  path algorithm.
     */
    public double[][] shortestPathMatrix();
}
