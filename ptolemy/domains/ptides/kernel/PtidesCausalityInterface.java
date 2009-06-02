/* Interface representing a dependency in the PTIDES domain.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.domains.ptides.kernel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.DefaultCausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// PTIDESCausalityInterface

/**
 * This class gives a specialized causality interface for PTIDES.
 * @author Jia Zou
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
*/
public class PtidesCausalityInterface extends CausalityInterfaceForComposites {

    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *   This is required to be an instance of CompositeEntity.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     *  @exception IllegalArgumentException If the actor parameter is not
     *  an instance of CompositeEntity.
     */
    public PtidesCausalityInterface(Actor actor,
            Dependency defaultDependency) throws IllegalArgumentException {
        super(actor, defaultDependency);
        if (!(actor instanceof CompositeEntity)) {
            throw new IllegalArgumentException("Cannot create an instance of "
                    + "CausalityInterfaceForComposites for "
                    + actor.getFullName() + ", which is not a CompositeEntity.");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Get the dependency between the input and output ports. If the 
     *  ports does not belong to the same actor, an exception is thrown.
     *  Depending on the actor, the corresponding getDependency() method in
     *  its super class or super's super's method is called. Also, if the
     *  output is null, super's getDependency() is called because super's
     *  method uses this method to update the dependency in the system.
     *  @param input The input port.
     *  @param output The output port.
     *  @return The dependency between the specified input port
     *   and the specified output port.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    
    public Dependency getDependency(IOPort input, IOPort output)
            throws IllegalActionException {
        Actor actor = (Actor)input.getContainer();
        if (output != null) {
            Actor outputActor = (Actor)output.getContainer();
            if (actor != outputActor) {
                throw new IllegalActionException(input, output, "Cannot get dependency" +
                        "from these two ports, becasue they do not belong" +
                "to the same actor.");
            }
        } else {
            // if the output is null, then the method is simply called to update the
            // dependency of the graph, which is done in the super method of
            // getDependency().
            return super.getDependency(input, output);
        }
        CausalityInterface causalityInterface = actor.getCausalityInterface();
        if (causalityInterface instanceof PtidesCausalityInterface) {
            return super.getDependency(input, output);
        } else {
            return causalityInterface.getDependency(input, output);
        }
    }
    
    /** Return a collection of ports the given port is dependent on, within
     *  the same actor.
     *  This method first look at all declared dependencies, and add all
     *  dependencies that are not equal to the oPlusIdentity. Then, for
     *  ports that do not have declared dependencies, we rely on
     *  dependentPorts() to ensure dependent ports are added to the returned
     *  Collection.
     *  Note we use a HashSet to ensure only one copy of each port is
     *  returned in the Collection.
     *  @see #dependentPorts(IOPort)
     * 
     *  @param port The given port to find finite dependent ports.
     *  @return Collection of finite dependent ports.
     *  @throws IllegalActionException
     */
    public Collection<IOPort> finiteDependentPorts(IOPort port)
            throws IllegalActionException {
        // FIXME: This does not support ports that are both input and output.
        // Should it?
        Collection<IOPort> result = new HashSet<IOPort>();
        Actor actor = (Actor)port.getContainer();
        CausalityInterface actorCausalityInterface;
        if (actor instanceof AtomicActor) {
            actorCausalityInterface = (DefaultCausalityInterface)actor.getCausalityInterface();
        } else if (actor instanceof CompositeActor) {
            actorCausalityInterface = (CausalityInterfaceForComposites)actor.getCausalityInterface();
        } else {
            throw new IllegalActionException(actor, "Actor is not a typed atomic or typed composite" +
            		"actor, do not know how to deal with it.");
        }
        if (port.isInput()) {
            List<IOPort> outputs = ((Actor)port.getContainer()).outputPortList();
            for (IOPort output : outputs) {
                Dependency dependency = actorCausalityInterface.getDependency(port, output);
                if (dependency != null && dependency.compareTo(dependency.oPlusIdentity())!= 0) {
                    result.add(output);
                }
            }
        } else { // port is output port.
            List<IOPort> inputs = ((Actor)port.getContainer()).inputPortList();
            for (IOPort input : inputs) {
                Dependency dependency = actorCausalityInterface.getDependency(input, port);
                if (dependency != null && dependency.compareTo(dependency.oPlusIdentity())!= 0) {
                    result.add(input);
                }
            }
        }
        return result;
    }
    
    /** Return a collection of ports that are finite equivalent ports
     *  of the input port. But finite, we mean the delay between the input
     *  and output is a finite value that's other than double.POSITIVE_INFINITY
     *  or 0.0 (taking RealDependency for example). 
     *  <p>
     *  An equivalence class is defined as follows.
     *  If input ports X and Y each have a dependency not equal to the
     *  default depenency's oPlusIdentity() on any common port
     *  or on two equivalent ports
     *  or on the state of the associated actor, then they
     *  are in a finite equivalence class.
     *  Note we use a HashSet to ensure only one copy of each port is
     *  returned in the Collection.

     *  If the port is not an input port, an exception
     *  is thrown.
     *  
     *  @param input
     *  @return Collection of finite equivalent ports.
     *  @throws IllegalActionException
     */
    public Collection<IOPort> finiteEquivalentPorts(IOPort input)
            throws IllegalActionException {
        if (input.getContainer() != _actor || !input.isInput()) {
            throw new IllegalActionException(input, _actor,
                    "equivalentPort() called with argument "
                    + input.getFullName()
                    + " which is not an input port of "
                    + _actor.getFullName());
        }
        Collection<IOPort> result = new HashSet<IOPort>();
        // first get all outputs that are dependent on this input.
        Collection<IOPort> outputs = finiteDependentPorts(input);
        // now for every input that is also dependent on the output, add
        // it to the list of ports that are returned.
        for (IOPort output : outputs) {
            result.addAll(finiteDependentPorts(output));
        }
        return result;
    }
}
