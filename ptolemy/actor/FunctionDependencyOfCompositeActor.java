/* An instance of FunctionDependencyOfCompositeActor describes the 
function dependency information of a composite actor. 

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

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependence
/** An instance of FunctionDependencyOfCompositeActor describes the function 
dependency information of a composite actor. The construction of the ports
graph is in a bottom-up way by composing the FunctionDependencies of the 
contained actors, which may be either atomic or composite. 

@see FunctionDependency
@author Haiyang Zheng
@version $Id $
@since Ptolemy II 3.1
*/
public class FunctionDependencyOfCompositeActor extends FunctionDependency {

    /** Construct a FunctionDependency in the given container. 
     *  @param container The container has this FunctionDependency object.
     */
    public FunctionDependencyOfCompositeActor(Actor container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.  
     */
    // The following code has recursive calls. 
    // FIXME: Steve suggests the performance analysis.
    protected void _constructDirectedGraph()  {

        // get a new directed graph
        _directedGraph = new DirectedGraph();
    
        // First, include all the ports as nodes in the graph.
        // The ports may belong to the container, or the
        // actors it contains.
    
        // get all the inputs and outputs of the container
        Iterator inputs = _container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            _directedGraph.addNodeWeight(inputs.next());
        }
        List outputPorts = _container.outputPortList();
        Iterator outputs = outputPorts.listIterator();
        while (outputs.hasNext()) {
            _directedGraph.addNodeWeight(outputs.next());
        }

        // Here we may add constraints on which actors to be used to
        // construct graph. For example, in a modal model, we only include
        // the controller, but not the refinements. The controller
        // will provide a function dependency of the refinement of 
        // current state.
        List embeddedActors = _getEntities();
        
        // merge the contained actors' graph into current one
        Iterator embeddedActorsIterator = embeddedActors.iterator();
        while (embeddedActorsIterator.hasNext()) {
            Actor embeddedActor = (Actor)embeddedActorsIterator.next();
            FunctionDependency functionDependency = 
                embeddedActor.getFunctionDependencies();
            // merge the ports graph of the embedded actor into current one
            if (functionDependency != null) {
                _directedGraph.addGraph(functionDependency.getAbstractPortsGraph());
            }
        }
           
        // Next, create the directed edges according to the connections at 
        // the container level
        embeddedActorsIterator = embeddedActors.iterator();
        // iterate all embedded actors (include opaque composite actors)
        while (embeddedActorsIterator.hasNext()) {
            Actor embeddedActor = (Actor)embeddedActorsIterator.next();
            // Find the successor of the output ports of current actor.
            Iterator successors = 
                embeddedActor.outputPortList().iterator();
            while (successors.hasNext()) {
                IOPort outPort = (IOPort) successors.next();
                // Find the inside ports connected to outPort.
                // NOTE: sinkPortList() is an expensive operation,
                // and it may return ports that are not physically
                // connected (as in wireless ports).  Hence, we
                // use getRemoteReceivers() here. EAL
                /*
                Iterator inPortIterator =
                    outPort.sinkPortList().iterator();
                while (inPortIterator.hasNext()) {
                    // connected them
                    _directedGraph.addEdge(outPort, inPortIterator.next());
                }
                */
                Receiver[][] receivers = outPort.getRemoteReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    // FIXME: For PrameterPort, it is possible that 
                    // the downstream receivers are null. It is a
                    // unresolved issue about the semantics of Parameter
                    // Port considering the lazy evaluation of variables.
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            IOPort ioPort = 
                                receivers[i][j].getContainer();
                            if (embeddedActors.contains(ioPort.getContainer()) ||
                                outputPorts.contains(ioPort)) {
                                _directedGraph.addEdge(
                                    outPort, ioPort);
    //                                _directedGraph.addEdge(
    //                                    outPort, receivers[i][j].getContainer(), 
    //                                    new Integer(1));
                            }
                        }
                    }
                }
            }
        }

        // Last, connect the _container inputs to the inside
        // ports receiving tokens from these inputs.
        inputs = _container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            // Find the inside ports connected to this input port.
            // NOTE: insideSinkPortList() is an expensive operation,
            // and it may return ports that are not physically
            // connected (as in wireless ports).  Hence, we
            // use deepGetReceivers() here. EAL
            /*
            Iterator inPortIterator =
                inputPort.insideSinkPortList().iterator();
            while (inPortIterator.hasNext()) {
                // connected them
                _directedGraph.addEdge(inputPort, inPortIterator.next());
            }
            */
            Receiver[][] receivers = inputPort.deepGetReceivers();
            for (int i = 0; i < receivers.length; i++) {
                for (int j = 0; j < receivers[i].length; j++) {
                    IOPort ioPort = 
                        receivers[i][j].getContainer();
                    // The receivers may belong to either the inputs of contained
                    // actors, or the outputs of the containter.
                    if (embeddedActors.contains(ioPort.getContainer()) ||
                        _container.equals(ioPort.getContainer())) {
                        _directedGraph.addEdge(inputPort, 
                            receivers[i][j].getContainer());
    //                    _directedGraph.addEdge(inputPort, 
    //                        receivers[i][j].getContainer(), new Integer(1));
                    }
                }
            }
        }
    }

    protected List _getEntities() {
        return ((CompositeActor)_container).deepEntityList();
    }
}
