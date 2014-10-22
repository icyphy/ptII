/* Utilities for data dependencies between actors.

   Copyright (c) 2012-2014 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ActorDependencies

/**
 *  Utilities for data dependencies between actors.
 *
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class ActorDependencies {

    /** Construct an ActorDependencies object.
     */
    private ActorDependencies() {
        // This method is private because this class has only static
        // public methods.
    }

    /** Return a Set of dependent (downstream) atomic actors that are connected to the
     *  target.  Opaque composite actors are searched. For output ports of the
     *  specified actor, these are downstream actors that are connected on the outside.
     *  For input ports of the specified actor, these are downstream actors connected
     *  on the inside.
     *  @param actor the Actor to be searched.
     *  @return A Set of dependent atomic actors.
     *  @exception KernelException If there is a problem with the receivers.
     *  the top level or if preinitialize() fails.
     */
    public static Set<AtomicActor> dependents(Actor actor)
            throws KernelException {
        return ActorDependencies.dependents(actor, AtomicActor.class);
    }

    /** Return a Set of dependent (downstream) actors of a particular
     *  class that are connected to the target.  Opaque composite
     *  actors are searched. For output ports of the
     *  specified actor, these are downstream actors that are connected on the outside.
     *  For input ports of the specified actor, these are downstream actors connected
     *  on the inside.
     *  @param actor the Actor to be searched.
     *  @param filter The class of prerequisite actors to be returned.
     *  @return A Set of dependent atomic actors
     *  @exception KernelException If there is a problem with the receivers.
     *  the top level or if preinitialize() fails.
     */
    public static Set<AtomicActor> dependents(Actor actor, Class filter)
            throws KernelException {
        //System.out.println("ActorDependencies.dependents: START" + actor.getFullName());
        Set<AtomicActor> results = new HashSet<AtomicActor>();

        // preinitialize() creates connections between Publishers and
        // Subscribers.
        Manager.preinitializeThenWrapup(actor);

        // Iterate through the output ports and add actors that are
        // of the filter type to the result.  Opaque Composites are
        // handled specially.

        Iterator outputs = actor.outputPortList().iterator();
        while (outputs.hasNext()) {
            IOPort output = (IOPort) outputs.next();
            results.addAll(_dependents(output, filter));
        }
        //System.out.println("ActorDependencies.dependents: END " + actor.getFullName() + " " + results);
        return results;
    }

    /** Return a Set of dependent (downstream) actors of a particular
     *  class that are connected to a port.  Opaque composite
     *  actors are searched. For output ports of the specified actor,
     *  these are downstream actors that are connected on the outside.
     *  For input ports of the specified actor, these are downstream
     *  actors connected on the inside.
     *  @param port The target port.
     *  @param filter The class of prerequisite actors to be returned.
     *  @return A Set of dependent atomic actors
     *  @exception KernelException If there is a problem with the receivers.
     *  the top level or if preinitialize() fails.
     */
    public static Set<AtomicActor> dependents(IOPort port, Class filter)
            throws KernelException {
        //System.out.println("ActorDependencies.dependents: START" + actor.getFullName());
        Set<AtomicActor> results = new HashSet<AtomicActor>();

        // preinitialize() creates connections between Publishers and
        // Subscribers.
        Manager.preinitializeThenWrapup((Actor) port.getContainer());

        results.addAll(_dependents(port, filter));

        //System.out.println("ActorDependencies.dependents: END " + actor.getFullName() + " " + results);
        return results;
    }

    /** Return a Set of AtomicActors that are connected upstream to this AtomicActor.
     *  @param actor the Actor to be searched.
     *  @return A Set of AtomicActors that are connected to this AtomicActor.
     *  @exception KernelException If thrown when a Manager is added to
     *  the top level or if preinitialize() fails.
     */
    public static Set<AtomicActor> prerequisites(Actor actor)
            throws KernelException {
        return ActorDependencies.prerequisites(actor, AtomicActor.class);
    }

    /** Return a Set of actors that match the specified filter
     *  that are connected upstream to the specified actor.
     *  If an upstream actor does not match the filter, then look
     *  through it to other actors connected further upstream.
     *  Upstream actors include any that can send data to the
     *  specified actor.
     *  @param actor the Actor to be searched.
     *  @param filter The class of prerequisite actors to be returned.
     *  @return A Set of AtomicActors that are connected to this AtomicActor.
     *  @exception KernelException If thrown when a Manager is added to
     *  the top level or if preinitialize() fails.
     */
    public static Set<AtomicActor> prerequisites(Actor actor, Class filter)
            throws KernelException {
        //System.out.println("ActorDependencies.prerequisites: START: " + actor.getFullName());
        Set<AtomicActor> results = new HashSet<AtomicActor>();

        // preinitialize() creates connections between Publishers and
        // Subscribers.
        Manager.preinitializeThenWrapup(actor);

        // Iterate through the input ports and add actors that are
        // of the filter type to the result.  Opaque Composites are
        // handled specially.

        // FIXME: this seems really wrong - overly complex?

        Iterator inputs = actor.inputPortList().iterator();
        while (inputs.hasNext()) {
            IOPort input = (IOPort) inputs.next();
            //            results.addAll(_prerequisites(input, filter));

            Iterator ports = input.sourcePortList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                NamedObj container = port.getContainer();
                if (filter.isInstance(container)) {
                    results.add((AtomicActor) container);
                } else {
                    results.addAll(_prerequisites(port, filter));
                    // Handle ports in TypedComposites?
                    // FIXME: This seems wrong.  Why getRemoteReceivers()?
                    // Can't we simplify this?
                    Receiver[][] receivers = port.getRemoteReceivers();
                    if (receivers != null) {
                        for (Receiver[] receiver : receivers) {
                            if (receiver != null) {
                                for (int j = 0; j < receiver.length; j++) {
                                    if (receiver[j] != null) {
                                        IOPort remotePort = receiver[j]
                                                .getContainer();
                                        if (remotePort != null) {
                                            results.addAll(_prerequisites(
                                                    remotePort, filter));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //System.out.println("ActorDependencies.prerequisites: DONE: " + actor.getFullName());
        return results;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the container of the specified port matches the specified filter, then return it;
     *  otherwise, return the set of all actors that match the
     *  filter type connected downstream to the specified port.
     *  If this is output port, these are downstream
     *  ports connected on the outside. If this is an input port, then these
     *  are downstream ports connected on the inside.
     *  This method traverses opaque composites.
     *  @param port The port to be checked
     *  @param filter The class of dependent actors to be returned.
     *  @return The Set of all AtomicActors connected to the port.
     */
    private static Set<AtomicActor> _dependents(IOPort port, Class filter)
            throws IllegalActionException {
        //System.out.println("ActorDependencies._dependents: START" + remotePort.getFullName());
        Set<AtomicActor> results = new HashSet<AtomicActor>();
        NamedObj container = port.getContainer();
        if (filter.isInstance(container)) {
            results.add((AtomicActor) container);
        } else {
            Receiver[][] receivers = null;
            if (port.isOutput() && port.isInput()) {
                throw new InternalError(
                        "Can't handle port that is both input nor output: "
                                + port);
            }
            if (port.isOutput()) {
                receivers = port.getRemoteReceivers();
            } else if (port.isInput()) {
                receivers = port.deepGetReceivers();
            } else {
                throw new InternalError(
                        "Can't handle port that is neither input nor output: "
                                + port);
            }
            if (receivers != null) {
                for (Receiver[] receiver : receivers) {
                    if (receiver != null) {
                        for (int j = 0; j < receiver.length; j++) {
                            if (receiver[j] != null) {
                                IOPort remotePort2 = receiver[j].getContainer();
                                if (remotePort2 != null) {
                                    //System.out.println("ActorDependencies._dependents: rempotePort2: " + remotePort2.getFullName());
                                    results.addAll(_dependents(remotePort2,
                                            filter));
                                }
                            }
                        }
                    }
                }
            }
        }
        //System.out.println("ActorDependencies._dependents: END" + remotePort.getFullName() + " " + results);
        return results;
    }

    /** If the container of the specified port matches the specified filter,
     *  then return that container; otherwise,
     *  return the set of all actors that match the filter type connected upstream
     *  to the port. If the port is an output, then these are actors
     *  connected on the inside. If it is an input, then these are actors
     *  connected on the outside. If an upstream port is not an instance
     *  of the specified filter, then traverse it, looking for ports
     *  that are further upstream of it.
     *  This method traverses opaque composites.
     *  @param port The port to be checked
     *  @param filter The class of prerequisite actors to be returned.
     *  @return The Set of all AtomicActors connected to the port.
     */
    private static Set<AtomicActor> _prerequisites(IOPort port, Class filter)
            throws IllegalActionException {
        //System.out.println("ActorDependencies._prerequisites: START " + remotePort.getFullName());
        Set<AtomicActor> results = new HashSet<AtomicActor>();
        NamedObj container = port.getContainer();
        if (filter.isInstance(container)) {
            results.add((AtomicActor) container);
        } else {
            // Handle inside connections of an output port.
            for (IOPort insidePort : port.insideSourcePortList()) {
                Iterator sourcePorts = insidePort.insideSourcePortList()
                        .iterator();
                while (sourcePorts.hasNext()) {
                    IOPort sourcePort = (IOPort) sourcePorts.next();
                    container = sourcePort.getContainer();
                    if (filter.isInstance(container)) {
                        results.add((AtomicActor) container);
                    } else {
                        results.addAll(_prerequisites(sourcePort, filter));
                    }
                }
            }

            // Handle outside connections.
            Iterator remoteSourcePorts = port.sourcePortList().iterator();
            while (remoteSourcePorts.hasNext()) {
                IOPort remoteSourcePort = (IOPort) remoteSourcePorts.next();
                container = remoteSourcePort.getContainer();
                if (filter.isInstance(container)) {
                    results.add((AtomicActor) container);
                }
                Iterator sourcePorts = remoteSourcePort.sourcePortList()
                        .iterator();
                while (sourcePorts.hasNext()) {
                    IOPort sourcePort = (IOPort) sourcePorts.next();
                    container = sourcePort.getContainer();
                    if (filter.isInstance(container)) {
                        results.add((AtomicActor) container);
                    } else {
                        results.addAll(_prerequisites(sourcePort, filter));
                    }
                }
            }
        }
        //System.out.println("ActorDependencies._prerequisites: END " + remotePort.getFullName() + " " + results);
        return results;
    }
}
