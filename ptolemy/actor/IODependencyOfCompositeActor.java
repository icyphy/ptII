/* An instance of IODependencyOfCompositeActor describes the input-output 
dependency information of a composite actor. 

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

//////////////////////////////////////////////////////////////////////////
//// IODependence
/** An instance of IODependencyOfCompositeActor describes the input-output 
dependency information of a composite actor. The construction of the ports
graph is in a bottom-up way by composition from the IODependencies of the 
contained actors, which may be either atomic or composite. 

@see IODependency
@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public class IODependencyOfCompositeActor extends IODependency {

    /** Construct an IODependency in the given container. 
     *  @param container The container has this IODependency object.
     */
    public IODependencyOfCompositeActor(Actor container) {
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
        Iterator outputs = _container.outputPortList().listIterator();
        while (outputs.hasNext()) {
            _directedGraph.addNodeWeight(outputs.next());
        }

        // merge the contained actors' graph into current one
        Iterator embeddedActors = 
            ((CompositeActor)_container).deepEntityList().iterator();
        while (embeddedActors.hasNext()) {
            Actor embeddedActor = (Actor)embeddedActors.next();
            IODependency ioDependency = embeddedActor.getIODependencies();
            // merge the ports graph of the embedded actor into current one
            _directedGraph.addGraph(ioDependency.getAbstractPortsGraph());
            // FIXME: we need a way to differ the internal connections of an actor
            // from the external connections between actors.
            // Maybe we need a different weight...
        }
           
        // Next, create the directed edges according to the connections at 
        // the container level
        embeddedActors = 
            ((CompositeActor)_container).deepEntityList().iterator();
        // iterate all embedded actors (include opaque composite actors)
        while (embeddedActors.hasNext()) {
            Actor embeddedActor = (Actor)embeddedActors.next();
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
                                _directedGraph.addEdge(
                                    outPort, receivers[i][j].getContainer());
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
                    _directedGraph.addEdge(inputPort, receivers[i][j].getContainer());
                }
            }
        }
    }
}
