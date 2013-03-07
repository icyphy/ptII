/* Causality interface where no output depends on any input.

 Copyright (c) 2008-2013 The Regents of the University of California.
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
package ptolemy.actor.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;

///////////////////////////////////////////////////////////////////
//// BreakCausalityInterface

/**
 This class provides a causality interface
 where no output port depends on any input port.
 That is, the dependency of any output port on any
 input port is the oPlusIdentity() of the specified
 default dependency.
 <p>
 The {@link #equivalentPorts(IOPort)} normally returns list
 containing only the specified port. If, however, the actor
 has any instance of PortParameter in its input port list, then
 it returns a list of all input ports. The reason for this is
 that any output, present or future, may depend on the values at
 such port parameters. In particular, it is necessary for inputs
 on these port parameters to be present when any other input is
 processed because it affects the parameters of the actor.
 It is more efficient to use this
 class than to use the base class and call removeDependency()
 to remove all the dependencies.

 @see Dependency

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class BreakCausalityInterface extends DefaultCausalityInterface {

    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     */
    public BreakCausalityInterface(Actor actor, Dependency defaultDependency) {
        super(actor, defaultDependency);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a collection of the ports in this actor that depend on
     *  or are depended on by the specified port. This method
     *  returns an empty collection.
     *  <p>
     *  Derived classes may override this, but they may need to
     *  also override {@link #getDependency(IOPort, IOPort)}
     *  and {@link #equivalentPorts(IOPort)} to be consistent.
     *  @param port The port to find the dependents of.
     *  @return a collection of ports, this method returns
     *  an empty collection of ports
     */
    public Collection<IOPort> dependentPorts(IOPort port) {
        return _EMPTY_COLLECTION;
    }

    /** Return a collection of the ports in this actor that are
     *  in the same equivalence class. This method
     *  returns a collection containing only the specified port,
     *  unless there is a PortParameter, in which case it returns
     *  all the input ports.
     *  <p>
     *  If derived classes override this, they may also
     *  need to override {@link #getDependency(IOPort,IOPort)}
     *  and {@link #dependentPorts(IOPort)} to be consistent.
     *  The returned result should always include the specified input port.
     *  @param input The port to find the equivalence class of.
     *  @return a collection of ports that are in the same equivalence
     *  class.  This method returns a collection containing only
     *  the specified port.
     */
    public Collection<IOPort> equivalentPorts(IOPort input) {
        // FIXME: Should the result be cached?
        // Presumably, this can only change
        // if ports are added or removed from the actor.
        List<IOPort> inputs = _actor.inputPortList();
        // If there is an instance of PortParameter, then return the
        // whole collection of input ports.
        for (IOPort actorInput : inputs) {
            if (actorInput instanceof ParameterPort) {
                return _actor.inputPortList();
            }
        }
        LinkedList<IOPort> result = new LinkedList<IOPort>();
        result.add(input);
        return result;
    }

    /** Return the dependency between the specified input port
     *  and the specified output port.  This method returns
     *  the oPlusIdentity() of the default dependency given
     *  in the constructor.
     *  <p>
     *  Derived classes should override this method to provide
     *  actor-specific dependency information. If they do so,
     *  then they may also need to override {@link #equivalentPorts(IOPort)}
     *  and {@link #dependentPorts(IOPort)} to be consistent.
     *  @param input The specified input port.
     *  @param output The specified output port.
     *  @return The dependency between the specified input port
     *   and the specified output port.
     */
    public Dependency getDependency(IOPort input, IOPort output) {
        return _defaultDependency.oPlusIdentity();
    }

    /** Remove the dependency that the specified output port has
     *  on the specified input port. This method does nothing since
     *  in this class, all dependencies have already been removed.
     *  @see #getDependency(IOPort, IOPort)
     *  @param inputPort The input port.
     *  @param outputPort The output port that does not depend on the
     *   input port.
     */
    public void removeDependency(IOPort inputPort, IOPort outputPort) {
    }
}
