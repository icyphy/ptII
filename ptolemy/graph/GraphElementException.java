/* Exception of accessing graph elements in wrong ways.

Copyright (c) 2002-2005 The University of Maryland.
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
package ptolemy.graph;


//////////////////////////////////////////////////////////////////////////
//// GraphElementException

/**
   The exception of accessing graph elements in wrong ways. This exception
   could be caused by accessing nonexistent elements or elements with incorrect
   association values.

   @author Mingyung Ko, Shuvra S. Bhattacharyya
   @version $Id$
   @since Ptolemy II 2.1
   @Pt.ProposedRating Red (myko)
   @Pt.AcceptedRating Red (ssb)
*/
public class GraphElementException extends GraphException {
    /** Constructor for a given message.
     *  @param message The message.
     */
    public GraphElementException(String message) {
        super(message);
    }

    /** Constructor with arguments of element, graph, and a message.
     *  @param element The invalid element.
     *  @param graph The graph accessed.
     *  @param message The exception message.
     */
    public GraphElementException(Element element, Graph graph, String message) {
        super(_argumentsToString(element, graph, message));
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
                    + "not in the graph.\n" + elementDump(node, graph));
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
                    + "not in the graph.\n" + elementDump(edge, graph));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  A method for generating proper string dumps.
     *  @param element The element the invalid weight tries to associate.
     *  @param graph The graph to access.
     *  @param message The exception message given by users.
     *  @return The desired exception message.
     */
    static private String _argumentsToString(Element element, Graph graph,
            String message) {
        return message + elementDump(element, graph);
    }
}
