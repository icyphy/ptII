/* Exception for unweighted graphs or graphs with improper weights.

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
//// GraphWeightException

/** Exception for unweighted graphs or graphs with improper weights.
    This exception can also be thrown due to accessing elements with
    incorrect weights.

    @author Mingyung Ko
    @version $Id$
    @since Ptolemy II 2.1
    @Pt.ProposedRating Red (myko)
    @Pt.AcceptedRating Red (ssb)
*/
public class GraphWeightException extends GraphException {
    /** Constructor for a given message.
     *  @param message The message.
     */
    public GraphWeightException(String message) {
        super(message);
    }

    /** Constructor with arguments of weight, element, graph,
     *  and a message.
     *  This exception is generally thrown because of invalid weight
     *  association to a specific element. It can also be thrown for
     *  unspecific elements by setting the <code>element</code> argument
     *  to <code>null</code>.
     *
     *  @param weight The invalid weight.
     *  @param element The element to associate the invalid weight. Set
     *         <code>null</code> for unspecific elements.
     *  @param graph The graph accessed.
     *  @param message The exception message.
     */
    public GraphWeightException(Object weight, Element element, Graph graph,
        String message) {
        super(_argumentsToString(weight, element, graph, message));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  A method for generating proper string dumps.
     *  @param weight The invalid weight.
     *  @param element The element the invalid weight tries to associate.
     *  @param graph The graph to access.
     *  @param message The exception message given by users.
     *  @return The desired exception message.
     */
    static private String _argumentsToString(Object weight, Element element,
        Graph graph, String message) {
        StringBuffer outputMessage = new StringBuffer(message
                + weightDump(weight));

        if (element != null) {
            outputMessage.append(elementDump(element, graph));
        } else {
            outputMessage.append(graphDump(graph));
        }

        return outputMessage.toString();
    }
}
