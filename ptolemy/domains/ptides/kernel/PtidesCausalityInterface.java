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

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.CausalityInterfaceForComposites;
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
    
    /** Return a collection of ports the given pot is dependent on.
     * 
     *  @param port The given port to find finite dependent ports.
     *  @return Collection of finite dependent ports.
     *  @throws IllegalActionException
     */
    public Collection<IOPort> finiteDependentPorts(IOPort port)
            throws IllegalActionException {
        // FIXME: implement me.
        HashSet<IOPort> result = new HashSet<IOPort>();
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
     *  are in an equivalence class.
     *  
     *  If the port is not an input port, an exception
     *  is thrown.
     *  
     *  @param input
     *  @return Collection of finite equivalent ports.
     *  @throws IllegalActionException
     */
    public Collection<IOPort> FiniteEquivalentPorts(IOPort input)
            throws IllegalActionException {
        // FIXME: implement me.
        if (input.getContainer() != _actor || !input.isInput()) {
            throw new IllegalActionException(input, _actor,
                    "equivalentPort() called with argument "
                    + input.getFullName()
                    + " which is not an input port of "
                    + _actor.getFullName());
        }
        HashSet<IOPort> result = new HashSet<IOPort>();
        return result;
    }
}
