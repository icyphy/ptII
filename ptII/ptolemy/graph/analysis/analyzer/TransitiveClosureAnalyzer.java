/* An interface to the analyzers used for the computation of transitive closure
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

//////////////////////////////////////////////////////////////////////////
//// TransitiveClosureAnalyzer

/**
 An interface to the analyzers for the computation of transitive closure
 of a directed graph.
 <p>
 @see ptolemy.graph.analysis.TransitiveClosureAnalysis
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @author Shahrooz Shahparnia
 @version $Id$
 */
public interface TransitiveClosureAnalyzer extends GraphAnalyzer {
    /** Check if there exist a path between a starting node "startNode" and an
     *  ending node "endNode" on the graph under analysis.
     *
     *  @param startNode The starting node.
     *  @param endNode The ending node.
     *  @return True if such a path exists.
     */
    public boolean pathExistence(Node startNode, Node endNode);

    /** Return the transitive closure of the graph under analysis in the
     *  form of two dimensional array. The first dimension represents
     *  source node label while the second one represents sink node label.
     *  Assume i and j are labels of two nodes.
     *  The value of {@link #transitiveClosureMatrix()}[i][j] is true if there
     *  is a path on the graph from "i" to "j".
     *
     *  @see ptolemy.graph.Graph#nodeLabel
     *  @return The transitive closure in the form of 2D array.
     */
    public boolean[][] transitiveClosureMatrix();
}
