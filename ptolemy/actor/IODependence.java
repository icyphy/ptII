/* An IODependence contains the input/output dependence information
of an actor.

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
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// IODependence
/** An instance of IODependence contains the dependence information between the inputs
and outputs. It is an attribute associated with an actor. For atomic actors,
this attribute is constructed inside the constructor. For composite actors,
this attribute is constructed in the preinitialize method of the composite
actors, after their directors finish preinitialization. The process is 
performed in a bottom-up way because the upper level IO dependence
information is built from those of the lower level.
<p>
This object contains a list of instances of <i>IOInformation</i>, each of
which corresponds to one input port. To access the IOInformation, use the
<i>getInputPort(IOPort)</i>, method which does a name mapping search.
To add one IOInformation of an input port, use <i>addInputPort(IOPort)</i>.
This method returns an IOInformation object.
<p>
Each input port has an IOInformation, which is an inner class providing
access to the relation between the input port and all the output ports.
Output ports are divided into three groups: (a) those immediately dependent
on the input, (b) those not immediately dependent on the input, and (c) those
not dependent on the input. The outputs in group (a) can be accessed with
<i>getDelayToPorts()</i> method, and the outputs in group (b) can be accessed
with <i>getDirectFeedthroughPort</i> method. The outputs in group (c) are
discarded.
<p>
This attribute is not persistent by default, so it will not be recorded
in a MoML representation of the model containing it.

@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public class IODependence extends SingletonAttribute {

    /** Construct an IODependence in the given container with the
     *  given name. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container The container.
     *  @param name The name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public IODependence(Entity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        inferDependence();
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an instance of IODependence describing the relation between
     *  the inputs and outputs of a composite actor.
     *  @param compositeActor 
     *  @return 
     */
    public void inferDependence() 
        throws IllegalActionException, NameDuplicationException {
        NamedObj container = (NamedObj)getContainer();
        // If the composite actor is at top level,
        // do nothing.
        // if (container.getContainer() == null) return;

        if (_dgValid != workspace().getVersion()){
            // construct a directed graph
            _dg = _constructDirectedGraph();
            _dgValid = workspace().getVersion();
        }
    }
        
    public boolean containsCyclicLoops() {
        return !_dg.isAcyclic();
    }

    public Object[] getCycleNodes() {
        return _dg.cycleNodes();
    } 
    
    public Set getNotDirectlyDependentPorts(IOPort inputPort) {
        Set notDirectlyDependentPortsSet = new HashSet();
        Actor actor = (Actor)getContainer();
        Iterator outputs = actor.outputPortList().listIterator();
        while (outputs.hasNext()) {
            IOPort outputPort = (IOPort)outputs.next();
            if (!hasDependence(inputPort, outputPort)){
                notDirectlyDependentPortsSet.add(outputPort);
            }
        }
        return notDirectlyDependentPortsSet;
    }
    
    public boolean hasDependence(IOPort inputPort, IOPort outputPort){
        Collection dependentOutputs = _dg.reachableNodes(_dg.node(inputPort));
        return dependentOutputs.contains(_dg.node(outputPort));
    }

    /** Clear the input ports list. This method is called in the
     *  preinitialize method only by composite actors when the topology
     *  of the embedded entities change.
     */
    public void invalidate() {
        _dgValid = -1;
    }

    public void removeDependence(IOPort inputPort, IOPort outputPort){
        Object[] incidentEdgeArray = 
            _dg.incidentEdges(_dg.node(inputPort)).toArray();
        for (int i = 0; i < incidentEdgeArray.length; i++) {
            Edge edge = (Edge)(incidentEdgeArray[i]);
            if (edge.sink().equals(_dg.node(outputPort))) {
                _dg.removeEdge(edge);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Construct a directed graph with the nodes representing actors and
    // directed edges representing dependencies.  The directed graph
    // is returned.
    private DirectedGraph _constructDirectedGraph() 
        throws IllegalActionException, NameDuplicationException {

        Actor actor = (Actor)getContainer();       

        DirectedGraph dg = new DirectedGraph();

        // First, include all the ports as nodes in the graph.
        // The ports may belong to the composite actor, or the
        // actors it contains.
        
        // get all the inputs and outputs of the composite actor
        Iterator inputs = actor.inputPortList().listIterator();
        while (inputs.hasNext()) {
            // 'add' replaced with 'addNodeWeight' since the former
            // has been deprecated.  The change here should have no
            // effect since .add had already been defined as a call
            // to .addNodeWeight -winthrop
            dg.addNodeWeight(inputs.next());
        }
        
        Iterator outputs = actor.outputPortList().listIterator();
        while (outputs.hasNext()) {
            dg.addNodeWeight(outputs.next());
        }
        
        if (actor instanceof CompositeActor) {
            // If the actor is a composite one,
            // get all the contained actors
            // and add their ports to directed graph.
            Iterator embeddedActors = 
                ((CompositeActor)actor).deepEntityList().iterator();
            while (embeddedActors.hasNext()) {
                Actor embeddedActor = (Actor) embeddedActors.next();
                Iterator inputsInside = 
                    embeddedActor.inputPortList().listIterator();
                while (inputsInside.hasNext()) {
                    dg.addNodeWeight(inputsInside.next());
                }
                Iterator outputsInside = 
                    embeddedActor.outputPortList().listIterator();
                while (outputsInside.hasNext()) {
                    dg.addNodeWeight(outputsInside.next());
                }
            }

            // Next, create the directed edges by iterating again.
            embeddedActors = 
                ((CompositeActor)actor).deepEntityList().iterator();
            // iterate all contained actors (include opaque composite actors)
            while (embeddedActors.hasNext()) {
                Actor embeddedActor = (Actor)embeddedActors.next();
                // get the IODependence attribute of the current actor
                // if no one is available, construct one.
                // The following code has recursive calls. 
                // FIXME: Steve suggests the performance analysis.
                IODependence ioDependence = embeddedActor.getIODependence();
                if (ioDependence == null) {
                    ioDependence = 
                        new IODependence((Entity) embeddedActor, "_IODependence");
                }
                // get all the input ports of current actor
                Iterator inputPorts = 
                    embeddedActor.inputPortList().iterator();
                // iterate the input ports
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort)inputPorts.next();
                    Set notDirectlyDependentOutputPorts = 
                        ioDependence.getNotDirectlyDependentPorts(inputPort);
                    // get the output ports on the current input port
                    Iterator outputPorts = 
                        embeddedActor.outputPortList().iterator();
                    while (outputPorts.hasNext()) {
                        IOPort outputPort = (IOPort)outputPorts.next();
                        if (!notDirectlyDependentOutputPorts.contains(outputPort)) {
                            dg.addEdge(inputPort, outputPort);
                        }
                    }
                }
    
                // Find the successor of the output ports of current actor.
                Iterator successors = 
                    embeddedActor.outputPortList().iterator();
                while (successors.hasNext()) {
                    IOPort outPort = (IOPort) successors.next();
                    // find the inside ports connected to outPort
                    Iterator inPortIterator =
                        outPort.sinkPortList().iterator();
                    while (inPortIterator.hasNext()) {
                        // connected them
                        dg.addEdge(outPort, inPortIterator.next());
                    }
                }
            }

            // Last, connect the actor inputs to the inside
            // ports receiving tokens from these inputs.
            inputs = actor.inputPortList().listIterator();
            while (inputs.hasNext()) {
                IOPort inputPort = (IOPort) inputs.next();
                // find the inside ports connected to this input port
                Iterator inPortIterator =
                    inputPort.insideSinkPortList().iterator();
                while (inPortIterator.hasNext()) {
                    // connected them
                    dg.addEdge(inputPort, inPortIterator.next());
                }
            }
            return dg;
        }
                
        // If the container is an atomic actor, 
        // the IODependence attribute has a default behavior
        // that the inputs and outputs are all directly dependent.
        // If any special properties are necessary, e.g. TimedDelay
        // for DE models, they will be given by the designers.
        // The reason for this is that we do not do code analysis,
        // and we can not tell the details.

        inputs = actor.inputPortList().listIterator();
        outputs = actor.outputPortList().listIterator();
        IODependence ioDependence = actor.getIODependence();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            while (outputs.hasNext()) {
                // connected the inputs and outputs
                dg.addEdge(inputPort, outputs.next());
            }
            /**
            if (ioDependence != null) {
                Set notDirectlyDependentPorts = 
                    ioDependence.getNotDirectlyDependentPorts(inputPort);
                while (outputs.hasNext()) {
                    IOPort output = (IOPort) outputs.next();
                    if (!notDirectlyDependentPorts.contains(output)){
                        dg.addEdge(inputPort, output);
                    }
                }
            } else {
                while (outputs.hasNext()) {
                    // connected the inputs and outputs
                    dg.addEdge(inputPort, outputs.next());
                }
            }
            */
        }
        return dg;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // The directed graph of the inputs and outputs
    private DirectedGraph _dg;
    private long _dgValid = -1;
    
}
