/* An instance of FunctionDependencyOfAtomicActor describes the function
   dependency information of an atomic actor.

   Copyright (c) 2003-2004 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
   FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
   ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
   THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.

   THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
   PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
   CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
   ENHANCEMENTS, OR MODIFICATIONS.

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

*/

package ptolemy.actor;

import java.util.Iterator;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependencyOfAtomicActor
/**
   An instance of FunctionDependencyOfAtomicActor describes the function
   dependence relation of an atomic actor. 
   <p>
   Because an atomic actor does not contain other actors, its abstract
   graph is the same with the detailed graph.
   <p> 
   For most atomic actors, usually, all the input ports and
   output ports are dependent. E.g, the Scale actor. (For
   definition of dependent, see {@link FunctionDependency}.) Thus, the
   input and output ports in the port graph are fully connected. for
   some atomic actors, such as the TimedDelay actor, its input and output
   ports are not dependent. We use the removeDependence() method to 
   declare that there is no dependency between its input and output ports. 
   See the {@link ptolemy.domains.de.lib.TimedDelay} actor for usage 
   pattern.
   <p> 
   Note, for the Multiplexer, Demultiplexer actors, their output port 
   depends on the the boolean control input.

   @see FunctionDependency
   @see ptolemy.domains.de.lib.TimedDelay
   @author Haiyang Zheng
   @version $Id: FunctionDependencyOfAtomicActor.java,v 1.2 2004/02/21
   07:57:24 hyzheng Exp $
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class FunctionDependencyOfAtomicActor extends FunctionDependency {

    /** Construct a FunctionDependencyOfAtomicActor in the given container.
     *  @param container The container.
     */
    public FunctionDependencyOfAtomicActor(Actor container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove the dependence between the input and output ports.
     *
     *  @param inputPort An input port.
     *  @param outputPort An output Port.
     */
    public void removeDependency(IOPort inputPort, IOPort outputPort) {
        // local variables setup
        //DirectedGraph detailedPortGraph = getDetailedPortGraph();
        DirectedGraph detailedPortGraph = _detailedPortGraph;
        // FIXME: do we need to check the validity of the
        // FunctionDependence here?
        // Since this method is always called from the 
        // _constructDirectedGraph() method (defined below), 
        // where the validity is checked, we may not need the check.
        // _validate();
        Iterator inputPorts = detailedPortGraph.nodes(inputPort).iterator();
        while (inputPorts.hasNext()) {
            Node input = (Node) inputPorts.next();
            Iterator outputPorts =
                detailedPortGraph.nodes(outputPort).iterator();
            while (outputPorts.hasNext()) {
                Node output = (Node) outputPorts.next();
                Object[] incidentEdgeArray =
                    detailedPortGraph.incidentEdges(input).toArray();
                for (int i = 0; i < incidentEdgeArray.length; i++) {
                    Edge edge = (Edge)(incidentEdgeArray[i]);
                    if (edge.sink().equals(output)) {
                        detailedPortGraph.removeEdge(edge);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    /** Construct an abstract port graph from a detailed port graph by 
        excluding the internal ports. For atomic actor, the abstract
        graph is the same with the detailed graph.
     */
    protected void _constructAbstractPortGraph() {
        //DirectedGraph abstractPortGraph = getAbstractPortGraph();
        //abstractPortGraph = getDetailedPortGraph();
        _abstractPortGraph = _detailedPortGraph;
    }
    
    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.
     */
    protected void _constructDetailedPortGraph() {
        // local variables setup
        AtomicActor container = (AtomicActor)getContainer();
        //DirectedGraph directedGraph = getDetailedPortGraph();
        DirectedGraph directedGraph = _detailedPortGraph;

        // Construct a fully connected ports graph
        // that the inputs and outputs are all dependent.
        Iterator inputs = container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            Iterator outputs = 
                container.outputPortList().listIterator();
            while (outputs.hasNext()) {
                // connected the inputs and outputs
                directedGraph.addEdge(inputPort, outputs.next());
            }
        }

        // If the atomic actor declares some dependencies do not
        // exist, remove the according edges from the graph.
        // Note: the following method calls the
        // removeDependence(input, output) method defined above.
        ((AtomicActor)container).removeDependencies();
    }
}
