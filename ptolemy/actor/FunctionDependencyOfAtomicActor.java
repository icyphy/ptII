/* An instance of FunctionDependencyOfAtomicActor describes the function
dependency information of an atomic actor.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependencyOfAtomicActor
/** An instance of FunctionDependencyOfAtomicActor describes the function
dependence relation of an atomic actor. It contains a ports graph
including the container ports only.
<p>
For most atomic actors, usually, all the input ports and output ports are
dependent. E.g, the AddSubtract actor. (For definition of <i>dependent</i>,
see FunctionDependency.) Thus, the input and out ports in the ports graph are
fully connected. for some atomic actors, such as TimedDelay actor, its input
and output ports are not dependent, we use the
<i>removeDependence(input, output)</i> method to declare that there is no
dependency between the input and output port, thus they are not connected.
See the {@link ptolemy.domains.de.lib.TimedDelay} for usage pattern.
<p>
Note, for Multiplexer, Demultiplexer actors, the boolean control input and
output are dependent.

@see FunctionDependency
@see ptolemy.domains.de.lib.TimedDelay
@author Haiyang Zheng
@version $Id: FunctionDependencyOfAtomicActor.java,v 1.2 2004/02/21
07:57:24 hyzheng Exp $
@since Ptolemy II 3.1
*/
public class FunctionDependencyOfAtomicActor extends FunctionDependency {

    /** Construct a FunctionDependencyOfAtomicActor in the given container.
     *  @param container The container has this FunctionDependency object.
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
        // FIXME: do we need to check the validity of the
        // FunctionDependence here?
        // Since this method is called from the _constructDirectedGraph
        // method, where the validity is checked, we may not need this check.
        // _validate();
        Iterator inputPorts = _directedGraph.nodes(inputPort).iterator();
        while (inputPorts.hasNext()) {
            Node input = (Node) inputPorts.next();
            Iterator outputPorts =
                _directedGraph.nodes(outputPort).iterator();
            while (outputPorts.hasNext()) {
                Node output = (Node) outputPorts.next();
                Object[] incidentEdgeArray =
                    _directedGraph.incidentEdges(input).toArray();
                for (int i = 0; i < incidentEdgeArray.length; i++) {
                    Edge edge = (Edge)(incidentEdgeArray[i]);
                    if (edge.sink().equals(output)) {
                        _directedGraph.removeEdge(edge);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.
     */
    protected void _constructDirectedGraph() {

        // get a new directed graph
        _directedGraph = new DirectedGraph();

        // First, include all the ports as nodes in the graph.

        // get all the inputs and outputs of the container
        Iterator inputs = _container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            _directedGraph.addNodeWeight(inputs.next());
        }
        Iterator outputs = _container.outputPortList().listIterator();
        while (outputs.hasNext()) {
            _directedGraph.addNodeWeight(outputs.next());
        }

        // Construct a fully connected ports graph
        // that the inputs and outputs are all directly dependent.

        inputs = _container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            outputs = _container.outputPortList().listIterator();
            while (outputs.hasNext()) {
                // connected the inputs and outputs
                _directedGraph.addEdge(inputPort, outputs.next());
            }
        }

        // if the atomic actor declares some dependencies do not
        // exist, remove them from the graph.
        // Note: the following method calls the
        // removeDependence(input, output) method defined above.
        ((AtomicActor)_container).removeDependencies();
    }
}
