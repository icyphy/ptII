/* Utilities for data dependencies between actors.

   Copyright (c) 2012 The Regents of the University of California.
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
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ActorDependencies

/**
 *  Utilities for data dependencies between actors.
 *
 *  @author Christopher Brooks
 *  @version $Id: DependencyResultsDialog.java 63152 2012-03-08 21:28:43Z derler $
 *  @since Ptolemy II 8.1
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

    /** Return a Set of dependent (upstream) atomic actors that are connected to the
     *  target.  Opaque composite actors are searched.   
     *  @param actor the Actor to be searched.
     *  @return A Set of dependent atomic actors.
     *  @exception KernelException If there is a problem with the receivers.
     *  the top level or if preinitialize() fails.
     */
    public static Set<AtomicActor> dependents(Actor actor)
            throws KernelException {
        return ActorDependencies.dependents(actor, AtomicActor.class);
    }

    /** Return a Set of dependent (upstream) actors of a particular
     *  class that are connected to the target.  Opaque composite
     *  actors are searched.
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

        // FIXME: this seems really wrong - overly complex?
        Iterator outputs = actor.outputPortList().iterator();
        while (outputs.hasNext()) {
            IOPort output = (IOPort)outputs.next();
            results.addAll(_dependents(output, filter));

//             Iterator ports = output.sinkPortList().iterator();
//             while (ports.hasNext()) {
//                 IOPort port = (IOPort) ports.next();
//                 NamedObj container = port.getContainer();
//                 if (filter.isInstance(container)) {
//                     results.add((AtomicActor) container);
//                 } else {
//                     results.addAll(_dependents(port, filter));
//                     // Handle ports in TypedComposites?
//                     Receiver[][] receivers = port.getRemoteReceivers();
//                     if (receivers != null) {
//                         for (int i = 0; i < receivers.length; i++) {
//                             if (receivers[i] != null) {
//                                 for (int j = 0; j < receivers[i].length; j++) {
//                                     if (receivers[i][j] != null) {
//                                         IOPort remotePort = receivers[i][j]
//                                             .getContainer();
//                                         if (remotePort != null) {
//                                             results.addAll(_dependents(remotePort, filter));
//                                             //System.out.println("AtomicActor.subscribers0: " + results);
//                                         }
//                                     }
//                                 }
//                             }
//                         }
//                     }
//                }
        }
        //System.out.println("ActorDependencies.dependents: END " + actor.getFullName() + " " + results);
        return results;
    }

    /** Return a Set of AtomicActors that are connected to this AtomicActor.
     *  @param actor the Actor to be searched.
     *  @return A Set of AtomicActors that are connected to this AtomicActor.
     *  @exception Kernelxception If thrown when a Manager is added to
     *  the top level or if preinitialize() fails.
     */
    public static Set<AtomicActor> prerequisites(Actor actor)
            throws KernelException {
        return ActorDependencies.prerequisites(actor, AtomicActor.class);
    }

    /** Return a Set of AtomicActors that are connected to this AtomicActor.
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
            IOPort input = (IOPort)inputs.next();
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
                        for (int i = 0; i < receivers.length; i++) {
                            if (receivers[i] != null) {
                                for (int j = 0; j < receivers[i].length; j++) {
                                    if (receivers[i][j] != null) {
                                        IOPort remotePort = receivers[i][j]
                                            .getContainer();
                                        if (remotePort != null) {
                                            results.addAll(_prerequisites(remotePort, filter));
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


    /** Return the set of all actors of a filter type connected upstream
     *  to a port.   
     *  This method traverses opaque composites.   
     *  @param remotePort The port to be checked   
     *  @param filter The class of dependent actors to be returned.
     *  @return The Set of all AtomicActors connected to the port.
     */
    private static Set<AtomicActor> _dependents(IOPort remotePort, Class filter) 
    throws IllegalActionException {
        //System.out.println("ActorDependencies._dependents: START" + remotePort.getFullName());
        Set<AtomicActor> results = new HashSet<AtomicActor>();
        NamedObj container = remotePort.getContainer();
        if (filter.isInstance(container)) {
            results.add((AtomicActor) container);
        } else {
            Receiver[][] receivers = null;
            if (remotePort.isOutput() && remotePort.isInput()) {
                throw new InternalError("Can't handle port that is both input nor output: " + remotePort);
            }
            if (remotePort.isOutput()) {
                receivers = remotePort.getRemoteReceivers();
            } else if (remotePort.isInput()) {
                receivers = remotePort.deepGetReceivers();
            } else {
                throw new InternalError("Can't handle port that is neither input nor output: " + remotePort);
            }
            if (receivers != null) {
                for (int i = 0; i < receivers.length; i++) {
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            if (receivers[i][j] != null) {
                                IOPort remotePort2 = receivers[i][j]
                                    .getContainer();
                                if (remotePort2 != null) {
                                    //System.out.println("ActorDependencies._dependents: rempotePort2: " + remotePort2.getFullName());
                                    results.addAll(_dependents(remotePort2, filter));
                                }
                            }
                        }
                    }
                }
            }

//             // Handle cases where the AtomicActor is deep inside opaque actors.
//             Iterator insidePorts = remotePort.sinkPortList().iterator();
//             while (insidePorts.hasNext()) {
//                 IOPort insidePort = (IOPort)insidePorts.next();
//                 container = insidePort.getContainer();
//                 if (filter.isInstance(container)) {
//                     results.add((AtomicActor) container);
//                 }
//                 Iterator sourcePorts = insidePort.sinkPortList().iterator();
//                 while (sourcePorts.hasNext()) {
//                     IOPort sourcePort = (IOPort)sourcePorts.next();
//                     container = sourcePort.getContainer();
//                     if (container instanceof AtomicActor) {
//                         results.add((AtomicActor) container);
//                     } else {
//                         results.addAll(_dependents(sourcePort, filter));
//                     }
//                 }
//                     // Handle ports in TypedComposites?
//                     Receiver[][] receivers = insidePort.getRemoteReceivers();
//                     if (receivers != null) {
//                         for (int i = 0; i < receivers.length; i++) {
//                             if (receivers[i] != null) {
//                                 for (int j = 0; j < receivers[i].length; j++) {
//                                     if (receivers[i][j] != null) {
//                                         IOPort remotePort2 = receivers[i][j]
//                                             .getContainer();
//                                         if (remotePort2 != null) {
//                                             results.addAll(_dependents(remotePort2, filter));
//                                             //System.out.println("AtomicActor.subscribers0: " + results);
//                                         }
//                                     }
//                                 }
//                             }
//                         }
//                     }
//             }

//             // Handle cases where the AtomicActor is deep inside opaque actors.
//             for( IOPort insidePort : remotePort.insideSourcePortList()) {
//                 container = insidePort.getContainer();
//                 if (filter.isInstance(container)) {
//                     results.add((AtomicActor) container);
//                 } 
// //                 Iterator sourcePorts = insidePort.deepConnectedInPortList().iterator();
// //                 while (sourcePorts.hasNext()) {
// //                     IOPort sourcePort = (IOPort)sourcePorts.next();
// //                     container = sourcePort.getContainer();
// //                     if (filter.isInstance(container)) {
// //                         results.add((AtomicActor) container);
// //                     } else {
// //                         results.addAll(_dependents(sourcePort, filter));
// //                     }
// //                 }

//                     // Handle ports in TypedComposites?
//                     Receiver[][] receivers = insidePort.getRemoteReceivers();
//                     if (receivers != null) {
//                         for (int i = 0; i < receivers.length; i++) {
//                             if (receivers[i] != null) {
//                                 for (int j = 0; j < receivers[i].length; j++) {
//                                     if (receivers[i][j] != null) {
//                                         IOPort remotePort2 = receivers[i][j]
//                                             .getContainer();
//                                         if (remotePort2 != null) {
//                                             results.addAll(_dependents(remotePort2, filter));
//                                             //System.out.println("AtomicActor.subscribers0: " + results);
//                                         }
//                                     }
//                                 }
//                             }
//                         }
//                     }
//             }


        }
        //System.out.println("ActorDependencies._dependents: END" + remotePort.getFullName() + " " + results);
        return results;
    }

    /** Return the set of all actors of a filter type connected downstream
     *  to a port.   
     *  This method traverses opaque composites.   
     *  @param remotePort The port to be checked   
     *  @param filter The class of prerequisite actors to be returned.
     *  @return The Set of all AtomicActors connected to the port.
     */
    private static Set<AtomicActor> _prerequisites(IOPort remotePort, Class filter) 
            throws IllegalActionException {
        //System.out.println("ActorDependencies._prerequisites: START " + remotePort.getFullName());
        Set<AtomicActor> results = new HashSet<AtomicActor>();
        NamedObj container = remotePort.getContainer();
        if (filter.isInstance(container)) {
            results.add((AtomicActor) container);
        } else {
            Receiver[][] receivers = null;
            //System.out.println("ActorDependencies._prerequisites: 10 " + remotePort.isInput() + " " + remotePort.isOutput() + " " + remotePort.isOpaque());
            if (remotePort.isOutput() && remotePort.isInput()) {
                throw new InternalError("Can't handle port that is both input nor output: " + remotePort);
            }
            if (remotePort.isOutput()) {
                // isOutput: getRemoteReceivers()
                // isOutput && isOpaque: getInsideReceivers()
                if (remotePort.isOpaque()) {
                    receivers = remotePort.getInsideReceivers();
                } else {
                    receivers = remotePort.getRemoteReceivers();
                }
            } else if (remotePort.isInput()) {
                // isInput: getReceivers()
                // isInput: deepGetReceivers()
                receivers = remotePort.deepGetReceivers();
            } else {
                throw new InternalError("Can't handle port that is neither input nor output: " + remotePort);
            }
//             System.out.println("ActorDependencies._prerequisites: 20 receivers: " + receivers + " " + (receivers != null ? receivers.length : "null") );
//             if (receivers != null) {
//                 for (int i = 0; i < receivers.length; i++) {
//                     if (receivers[i] != null) {
//                         for (int j = 0; j < receivers[i].length; j++) {
//                             if (receivers[i][j] != null) {
//                                 IOPort remotePort2 = receivers[i][j]
//                                     .getContainer();
//                                 if (remotePort2 != null /*&& remotePort2 != remotePort*/) {
//                                     System.out.println("ActorDependencies._prerequisites: remotePort2: " + remotePort2.getFullName());
//                                     results.addAll(_prerequisites(remotePort2, filter));
//                                 }
//                             }
//                         }
//                     }
//                 }
//             }

            // Handle cases where the AtomicActor is deep inside opaque actors.
            for( IOPort insidePort : remotePort.insideSourcePortList()) {
                Iterator sourcePorts = insidePort.insideSourcePortList().iterator();
                while (sourcePorts.hasNext()) {
                    IOPort sourcePort = (IOPort)sourcePorts.next();
                    container = sourcePort.getContainer();
                    if (filter.isInstance(container)) {
                        results.add((AtomicActor) container);
                    } else {
                        results.addAll(_prerequisites(sourcePort, filter));
                    }
                }
            }

            // Handle cases where the AtomicActor is deep inside opaque actors.
            Iterator remoteSourcePorts = remotePort.sourcePortList().iterator();
            while (remoteSourcePorts.hasNext()) {
                IOPort remoteSourcePort = (IOPort)remoteSourcePorts.next();
                container = remoteSourcePort.getContainer();
                if (filter.isInstance(container)) {
                    results.add((AtomicActor) container);
                }
                Iterator sourcePorts = remoteSourcePort.sourcePortList().iterator();
                while (sourcePorts.hasNext()) {
                    IOPort sourcePort = (IOPort)sourcePorts.next();
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
