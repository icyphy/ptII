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
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
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
IODependence attributes of the embedded actors. 
<p>
For atomic actors, by default, all the input ports and output ports are
directly dependent. To check the direct dependence of a pair, input port
and output port, use the <i>hasDependence(input, output)</i> method. To remove
the direct dependence of a pair, input port and output port, use 
<i>removeDependence(input, output)</i> method.
<p>
//FIXME:
This attribute is synchronized with the workspace. 
<p>
This attribute is not persistent by default, so it will not be exported
into a MoML representation of the model.

@see ptolemy.domains.de.lib.TimedDelay
@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public abstract class IODependence extends Attribute {

    /** Construct an IODependence attribute in the given container 
     *  with the given name. The container argument must not be null, 
     *  or a NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  Resolve the IODependence of input and output ports. Set this
     *  attribute nonpersistent.
     *
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public IODependence(Entity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the nodes in a directed cycle loop. If there are multiple
     *  cycles, all the nodes will be returned. If there is no cycle, return
     *  an empty array. The nodes are IOPorts. 
     *  @return An array contains the IOPorts in the cycles.
     */
    public Object[] getCycleNodes() throws IllegalActionException {
        validate();
        return _dg.cycleNodes();
    } 
    
    /** Return the set of all the output ports that are not directly dependent 
     *  on the given input. The elements inside the set are IOPorts.
     * 
     *  @param inputPort The given input port of the container.
     *  @return The set of all the output ports not directly dependent on the given
     *  input port.
     *  @exception IllegalActionException If the given input port does not
     *  belong to the same container of the IODependence attribute. 
     */
    public Set getNotDirectlyDependentPorts(IOPort inputPort) 
        throws IllegalActionException {
        validate();
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
    public boolean hasDependence(IOPort inputPort, IOPort outputPort)
        throws IllegalActionException {
        validate();
        Collection dependentOutputs = _dg.reachableNodes(_dg.node(inputPort));
        return dependentOutputs.contains(_dg.node(outputPort));
    }

    /** Invalidate this attribute.
     */        
    public void inValidate() {
        _dgValid = -1;
    }
    
    /** Check the validity of the IODependence.
     */        
    public void validate() throws IllegalActionException {
        if (_dgValid == -1) {
            _inferDependence();
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    // Construct a directed graph with the nodes representing input and
    // output ports, and directed edges representing dependencies.  
    // The directed graph is returned.

    // The following code has recursive calls. 
    // FIXME: Steve suggests the performance analysis.

    protected abstract void _constructDirectedGraph() 
        throws IllegalActionException, NameDuplicationException; 

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                    ////

    // The directed graph of the input and output ports
    protected DirectedGraph _dg;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Infer the dependence relationship between the inputs and outputs 
     *  of the container of this attribute. If the cached version
     *  is valid, use the cached directed graph. Otherwise, recaculate a new 
     *  one. Cache and synchronize the new version with the workspace. 
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    private void _inferDependence() 
        throws IllegalActionException {
        try {
            // FIXME: Update the _dgValid before it is actually constructed
            // may be dangerous. However, if update is put after construction,
            // it will never be called because the construction may force 
            // infinite _inferDependence calls. (See the removeDependencies()
            // and removeDependence() of the IODependenceOfAtomicActor.)
            _dgValid = workspace().getVersion();
            _constructDirectedGraph();

            System.out.println(_dg.toString());
            // FIXME: how to show the following debugging information
            // when listening to director?
            if (_debugging){ 
                _debug(getContainer().getName() +
                    " has a directed graph based on input and" +
                    " output ports: ");
                _debug(_dg.toString());
            }
        } catch(NameDuplicationException e) {
            throw new InternalErrorException( 
                " has NameDuplicationException thrown.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // The validity flag of this attribute.
    private long _dgValid = -1;
    
}
