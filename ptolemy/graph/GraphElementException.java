/* Exception of accessing graph elements in wrong ways.

 Copyright (c) 2002 The University of Maryland.
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

@ProposedRating Red (myko@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.graph;

//////////////////////////////////////////////////////////////////////////
//// GraphElementException
/**
The exception of accessing graph elements in wrong ways. This exception
could be caused by accessing inexistent elements or elements with incorrect
association values.

@author Mingyung Ko, Shuvra S. Bhattacharyya
@version $Id$
*/
public class GraphElementException extends GraphException {

    /** The default constructor without arguments.
     */
    public GraphElementException() {}

    /** Constructor with an argument of text description.
     *  @param message The exception message.
     */
    public GraphElementException(String message) {
        super(message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Verify that a node is in the container graph.
     *
     *  @param node The node to verify.
     *  @param graph The container graph.
     *  @exception IllegalArgumentException If the node is not in the graph.
     */
    static public void checkNode(Node node, Graph graph) {
        if (!graph.containsNode(node)) {
            throw new GraphElementException("Reference to a node that is "
                    + "not in the graph.\n" + nodeDump(node, graph));
        }
    }

    /** Verify that an edge is in the container graph.
     *
     *  @param edge The edge to verify.
     *  @param graph The container graph.
     *  @exception IllegalArgumentException If the edge is not in the graph.
     */
    static public void checkEdge(Edge edge, Graph graph) {
        if (!graph.containsEdge(edge)) {
            throw new GraphElementException("Reference to an edge that is "
                    + "not in the graph.\n" + edgeDump(edge, graph));
        }
    }
}

