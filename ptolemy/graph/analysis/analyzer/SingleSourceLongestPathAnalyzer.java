/* A common interface for all the single source longest path analyzers.

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

import java.util.List;

import ptolemy.graph.Node;

//////////////////////////////////////////////////////////////////////////
//// SingleSourceLongestPathAnalyzer

/**
 A common interface for all the single source longest path analyzers.
 <p>
 @see ptolemy.graph.analysis.SingleSourceLongestPathAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public interface SingleSourceLongestPathAnalyzer extends GraphAnalyzer {
    /** Return the distance from the start node to all the other nodes in the
     *  graph. The result is a double[] indexed by the destination node label.
     *
     *  @see ptolemy.graph.Graph#nodeLabel
     *  @return Return the distance from the start node to all the other nodes
     *  in the graph.
     */
    public double[] distance();

    /** Return the start node of this analyzer.
     *
     *  @return Return the start node of this analyzer.
     */
    public Node getStartNode();

    /** Return the longest path from node "startNode" to node "endNode" in the
     *  form of an ordered list.
     *
     *  @param endNode The ending node of the path.
     */
    public List path(Node endNode);

    /** Return the length of the longest path from node "startNode"
     *  to node "endNode". The source node can be
     *  set using {@link #setStartNode}.
     *
     *  @param endNode The ending node of the path.
     */
    public double pathLength(Node endNode);

    /** Set the single source node of this analyzer to the given node.
     *
     *  @param startNode The given node.
     */
    public void setStartNode(Node startNode);
}
