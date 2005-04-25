/* Base exception for graph errors.

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
//// GraphException

/**
   Base exception for graph errors. This is also an instance of
   <code>RuntimeException</code>.

   @author Mingyung Ko, Shuvra S. Bhattacharyya
   @version $Id$
   @since Ptolemy II 2.1
   @Pt.ProposedRating Red (myko)
   @Pt.AcceptedRating Red (ssb)
*/
public class GraphException extends RuntimeException {
    /** The default constructor without arguments.
     */
    public GraphException() {
        // Note: this nullary exception is required.  If it is
        // not present, then the subclasses of this class will not
        // compile.
        this(null);
    }

    /** Constructor with an argument of text description.
     *  @param message The exception message.
     */
    public GraphException(String message) {
        super(message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a dump of this graph suitable to be appended to an
     *  error message.
     *
     *  @param graph The graph to dump.
     *  @return A text string dump of the graph.
     */
    static public String graphDump(Graph graph) {
        return "\nA Dump of the offending graph follows.\n" + graph.toString()
            + "\n";
    }

    /** Return a dump of a graph element and the container graph suitable to
     *  be appended to an error message.
     *
     *  @param element The element to dump.
     *  @param graph The graph where the element resides.
     *  @return A text string dump of the element and graph.
     */
    static public String elementDump(Element element, Graph graph) {
        String descriptor;

        if (element == null) {
            descriptor = "element";
        } else {
            descriptor = element.descriptor();
        }

        return _elementDump(element, graph, descriptor);
    }

    /** Return a dump of a weight and the container graph suitable to
     *  be appended to an error message. Generally, an
     *  {@link #elementDump(Element, Graph)} follows.
     *
     *  @param weight The weight to dump.
     *  @return A text string dump of the weight and graph.
     */
    static public String weightDump(Object weight) {
        String dump = "\nThe weight is of class " + weight.getClass().getName()
            + " and its description follows:\n" + weight.toString();
        return dump;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a dump of an element ({@link Node}, {@link Edge},
     *  or weight) and the container graph suitable to be appended
     *  to an error message.
     *
     *  @param element The element to dump.
     *  @param graph The container graph.
     *  @param elementDescriptor Descriptor of the element.
     *  @return A text string dump of the element and graph.
     */
    static protected String _elementDump(Object element, Graph graph,
            String elementDescriptor) {
        String elementString = (element == null) ? "<null>" : element.toString();
        return "\nDumps of the offending " + elementDescriptor
            + " and graph follow.\n" + "The offending " + elementDescriptor + ":\n"
            + elementString + "\nThe offending graph:\n" + graph.toString() + "\n";
    }
}
