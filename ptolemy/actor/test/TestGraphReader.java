/* An application for testing the conversion of Ptolemy models into
   weighted graphs.

   Copyright (c) 2003-2005 The University of Maryland
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
package ptolemy.actor.test;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.GraphReader;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

import java.util.Collection;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// TestGraphReader

/** An application for testing the conversion of Ptolemy models into
    weighted graphs.
    <p>
    Usage: <code>java ptolemy.actor.test <em>xmlFileName</em></code>,
    <p>
    where <code><em>xmlFileName</code></em> is the name of a MoML file that
    contains a Ptolemy II specification. This application converts the
    specification into a weighted graph representation, and prints out information
    about this weighted graph.

    @author Shuvra S. Bhattacharyya
    @version $Id$
    @since Ptolemy II 4.0
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class TestGraphReader {
    // Make the constructor protected to prevent instantiation of this class
    // outside of subclasses.
    protected TestGraphReader() {
    }

    /** Convert a MoML file that contains a Ptolemy II specification into a
     *  weighted graph representation, and display information about
     *  the weighted graph.
     *  @param args The name of the MoML file.
     */
    public static void main(String[] args) {
        TestGraphReader tester = new TestGraphReader();
        CompositeActor toplevel = tester._readGraph(args);
        GraphReader graphReader = new GraphReader();
        DirectedGraph graph = (DirectedGraph) (graphReader.convert(toplevel));
        tester._printGraph(graph);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Print information about a graph to standard output. This method
     *  is called by {@link #main(String[])} to display information about the
     *  model that is read. It should be overridden to change the way this
     *  information is displayed.
     *
     * @param graph The graph for which information is to be printed.
     */
    protected void _printGraph(DirectedGraph graph) {
        System.out.println(graph.toString());

        // Determine the source nodes
        Collection sourceCollection = graph.sourceNodes();
        System.out.println("Number of source nodes = "
                + sourceCollection.size());

        Iterator sources = sourceCollection.iterator();
        int sourceNumber = 1;

        while (sources.hasNext()) {
            System.out.println("source #" + sourceNumber++ + ": "
                    + ((Node) (sources.next())).getWeight());
            System.out.println();
        }

        // Determine the sink nodes
        Collection sinkCollection = graph.sinkNodes();
        System.out.println("Number of sink nodes = " + sinkCollection.size());

        Iterator sinks = sinkCollection.iterator();
        int sinkNumber = 1;

        while (sinks.hasNext()) {
            System.out.println("sink #" + sinkNumber++ + ": "
                    + ((Node) (sinks.next())).getWeight());
            System.out.println();
        }
    }

    /** Convert a MoML file that contains a Ptolemy II specification into a
     *  composite actor representation.
     *  @param args The name of the MoML file.
     *  @return The composite actor representation.
     */
    protected CompositeActor _readGraph(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("TestGraphReader expects exactly one "
                    + "argument.");
        }

        // The Ptolemy II model returned by the Java parser.
        NamedObj toplevel;

        try {
            MoMLParser parser = new MoMLParser();
            toplevel = parser.parseFile(args[0]);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage()
                    + "Exception raised from the MoML parser\n");
        }

        if (!(toplevel instanceof CompositeActor)) {
            throw new RuntimeException("Top level must be a CompositeActor "
                    + "(in this case, it is '"
                    + ((toplevel == null) ? "null" : toplevel.getClass().getName())
                    + "')\n");
        }

        return (CompositeActor) toplevel;
    }
}
