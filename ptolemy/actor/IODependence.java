/* An instance of IODependence is an attribute of an actor containing
the input-output dependence information.

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
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// IODependence
/** An instance of IODependence is an attribute containing
the input-output dependence information of an actor. For atomic actors,
this attribute is constructed in the preinitialize method. For
composite actors, this attribute is inferred in a bottom-up way from the
IODependence attributes of the emgedded actors. 
<p>
For atomic actors, by default, all the input ports and output ports are
directly dependent. To check the direct dependence of a pair of input port
and output port, use <i>hasDependence(input, output)</i> method. To remove
the direct dependence of a pair of input port and output port, use 
<i>removeDependence(input, output)</i> method.
<p>
This attribute is synchronized with the workspace. 
<p>
This attribute is not persistent by default, so it will not be exported
into a MoML representation of the model.

@see ptolemy.domains.de.lib.TimedDelay
@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public class IODependence extends Attribute {

    /** Construct an IODependence attribute in the given container 
     *  with the given name. The container argument must not be null, 
     *  or a NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  Resolve the IODependence of input and output ports. Set this
     *  attribute nonpersistent.
     *
     *  @param container The container.
     *  @param name The name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
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

    /** Return true if there are directed cycle loops. 
     *  @return True if there are directed loops.
     */
    public boolean containsCyclicLoops() {
        return !_dg.isAcyclic();
    }

    /** Return the nodes in a directed cycle loop. If there are multiple
     *  cycles, all the nodes will be returned.
     *  @return An array contains the nodes in the cycles.
     */
    public Object[] getCycleNodes() {
        return _dg.cycleNodes();
    } 
    
    /** Return a set of output ports which are not directly dependent on the
     *  given input. 
     * 
     *  @param inputPort The given input port of the container.
     *  @return A set of output ports not directly dependent on the given
     *  input port.
     *  @exception IllegalActionException If the given input port does not
     *  belong to the same container of the IODependence attribute. 
     */
    public Set getNotDirectlyDependentPorts(IOPort inputPort) 
        throws IllegalActionException {
        // get container of the IODependence attribute.
        Actor actor = (Actor)getContainer();
        if (inputPort.getContainer().getContainer().equals(getContainer())) {
            throw new IllegalActionException(this, "doesn't belong to " +
                    ((NamedObj)actor).getName());
        }
        Set notDirectlyDependentPortsSet = new HashSet();
        Iterator outputs = actor.outputPortList().listIterator();
        while (outputs.hasNext()) {
            IOPort outputPort = (IOPort)outputs.next();
            if (!hasDependence(inputPort, outputPort)){
                notDirectlyDependentPortsSet.add(outputPort);
            }
        }
        return notDirectlyDependentPortsSet;
    }
    
    /** Return true if the output port is directly dependent on the 
     *  given input port.
     * 
     *  @param inputPort The given input port.
     *  @param outputPort The given output port. 
     *  @return True if the output port is directly dependent on the
     *  input port.
     */
    public boolean hasDependence(IOPort inputPort, IOPort outputPort){
        Collection dependentOutputs = _dg.reachableNodes(_dg.node(inputPort));
        return dependentOutputs.contains(_dg.node(outputPort));
    }

    /** Resolve the IODependence describing the relation between
     *  the inputs and outputs of an actor. If the cached version
     *  is valid, use the cached one. Otherwise, recaculate a new 
     *  one. Cache and synchronize the new version with the workspace. 
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public void inferDependence() 
        throws IllegalActionException, NameDuplicationException {

        if (_dgValid != workspace().getVersion()){
            // construct a directed graph
            _dg = _constructDirectedGraph();
            // System.out.println(_dg.toString());
            // FIXME: how to show the following debugging information
            // when listening to director?
            if (_debugging){ 
                _debug(getContainer().getName() +
                    " has a directed graph based on input and" +
                    " output ports: ");
                _debug(_dg.toString());
            }
            _dgValid = workspace().getVersion();
        }
    }
        
    /** Invalidate the cached version of this attribute.
     */
    public void invalidate() {
        _dgValid = -1;
    }

    /** Remove the dependence between the input and output ports.
     * 
     *  @param inputPort An input port.
     *  @param outputPort An output Port.
     */
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

    /** Specify the container, adding this attribute to the
     *  list of attributes in the container. 
     *  <p>
     *  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the super class throws it.
     *  @exception NameDuplicationException If the super class throws it.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            super.setContainer(container);

            if (container instanceof CompositeActor) {
                // Set cached value in composite actor.
                ((CompositeActor)container)._setIODependence(this);
            } else if (container instanceof AtomicActor) {
                ((AtomicActor)container)._setIODependence(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Construct a directed graph with the nodes representing input and
    // output ports, and directed edges representing dependencies.  
    // The directed graph is returned.

    // The following code has recursive calls. 
    // FIXME: Steve suggests the performance analysis.

    private DirectedGraph _constructDirectedGraph() 
        throws IllegalActionException, NameDuplicationException {

        Actor container = (Actor)getContainer();       

        DirectedGraph dg = new DirectedGraph();

        // First, include all the ports as nodes in the graph.
        // The ports may belong to the container, or the
        // actors it contains.
        
        // get all the inputs and outputs of the container
        Iterator inputs = container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            // 'add' replaced with 'addNodeWeight' since the former
            // has been deprecated.  The change here should have no
            // effect since .add had already been defined as a call
            // to .addNodeWeight -winthrop
            dg.addNodeWeight(inputs.next());
        }
        
        Iterator outputs = container.outputPortList().listIterator();
        while (outputs.hasNext()) {
            dg.addNodeWeight(outputs.next());
        }
        
        if (container instanceof CompositeActor) {
            // If the container is a composite actor,
            // get all the embedded actors
            // and add their ports to directed graph.
            Iterator embeddedActors = 
                ((CompositeActor)container).deepEntityList().iterator();
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
                ((CompositeActor)container).deepEntityList().iterator();
            // iterate all embedded actors (include opaque composite actors)
            while (embeddedActors.hasNext()) {
                Actor embeddedActor = (Actor)embeddedActors.next();
                // get the IODependence attribute of the current actor
                // if no one is available, construct one.
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

            // Last, connect the container inputs to the inside
            // ports receiving tokens from these inputs.
            inputs = container.inputPortList().listIterator();
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

        inputs = container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            outputs = container.outputPortList().listIterator();
            while (outputs.hasNext()) {
                // connected the inputs and outputs
                dg.addEdge(inputPort, outputs.next());
            }
        }
        return dg;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // The directed graph of the input and output ports
    private DirectedGraph _dg;
    // The validity flag of this attribute.
    private long _dgValid = -1;
    
}
