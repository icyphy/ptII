/* A scheduler for multidimensional dataflow.

 Copyright (c) 1998-2009 The Regents of the University of California.
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

package ptolemy.domains.pthales.kernel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.domains.pthales.lib.PThalesIOPort;
import ptolemy.domains.pthales.lib.PthalesActorInterface;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** 
 *  FIXME: To do:
 *  - The tokenInitProduction parameter is not being used. ArrayOL equivalent?
 *  
 * @author eal
 *
 */
public class PthalesScheduler extends SDFScheduler {

    public PthalesScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    protected Schedule _getSchedule() throws IllegalActionException,
            NotSchedulableException {
        // Context of this scheduler.
        PthalesDirector director = (PthalesDirector) getContainer();
        CompositeActor compositeActor = (CompositeActor) (director
                .getContainer());
        List<Actor> actors = compositeActor.deepEntityList();

        // Before overwriting them, collect the production and
        // consumption rates of the actors, if they are declared.
        // The will be declared if the actor is an SDF actor (either atomic
        // or an opaque composite actor). These will need to be restored later.
        Map<IOPort, Integer> originalDeclaredPortRates = new HashMap<IOPort, Integer>();
        for (Actor actor : actors) {
            List<IOPort> ports = actor.inputPortList();
            for (IOPort port : ports) {
                PThalesIOPort thalesPort = (PThalesIOPort)port;

                Integer rate = thalesPort.getDeclaredPortRate(port,
                        "tokenConsumptionRate");
                if (rate != null) {
                    originalDeclaredPortRates.put(port, rate);
                }
            }
            ports = actor.outputPortList();
            for (IOPort port : ports) {
                PThalesIOPort thalesPort = (PThalesIOPort)port;

                Integer rate = thalesPort.getDeclaredPortRate(port, "tokenProductionRate");
                if (rate != null) {
                    originalDeclaredPortRates.put(port, rate);
                }
            }
        }

        // Iterate over the actors.
        for (Actor actor : actors) {
            PthalesActorInterface myActor = (PthalesActorInterface)actor;
  
            // Next do the output ports.
            List<IOPort> ports = actor.outputPortList();
            for (IOPort port : ports) {
                PThalesIOPort thalesPort = (PThalesIOPort)port;

                // FIXME: The following method looks for a stride as well,
                // which does not make sense for a tiling spec.

                // Now we need to set capacities of each of the receivers.
                // Notify the destination receivers of the write pattern.

                Receiver[][] receivers = port.getRemoteReceivers();
                if (receivers != null && receivers.length > 0) {
                    for (Receiver[] receiverss : receivers) {
                        if (receiverss != null && receiverss.length > 0) {
                            for (Receiver receiver : receiverss) {
                                ((PthalesReceiver) receiver).setOutputArray(thalesPort,myActor);
                            }
                        }
                    }
                }
            }
            // FIXME: Need to do the input ports of the container of this director,
            // treating them as output ports, in order to support hierarchical nestings
            // of this director inside other models.

            // Next do the input ports.
            ports = actor.inputPortList();
            for (IOPort port : ports) {
                PThalesIOPort thalesPort = (PThalesIOPort)port;
 
                // Notify the receivers of the read pattern.
                // This will have the side effect of setting the capacity of the receivers.
                Receiver[][] receivers = port.getReceivers();
                if (receivers != null && receivers.length > 0) {
                    for (Receiver[] receiverss : receivers) {
                        if (receiverss != null && receiverss.length > 0) {
                            for (Receiver receiver : receiverss) {
                                // FIXME: Is the cast to LinkedHashSet safe?
                                // Depends on the Java implementation of LinkedHashMap.
                                ((PthalesReceiver) receiver).setInputArray(thalesPort,myActor);
                            }
                        }
                    }
                }
            }
        }

        // Populate the schedule with a subclass of Firing
        // that keeps track of the dimensions for the firing.
        // FIXME: Brute force technique here assumes an acyclic graph.
        // It executes all firings of upstream actors before any firing
        // of a downstream actor.
        Schedule schedule = new Schedule();
        CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) compositeActor
                .getCausalityInterface();
        List<Actor> sortedActors = causality.topologicalSort();
        for (Actor actor : sortedActors) {
            PthalesActorInterface myActor = (PthalesActorInterface)actor;
            Firing firing = new Firing(actor);
 
            // Iteration is only done on external loops
            firing.setIterationCount(myActor.getIterations());
            schedule.add(firing);
        }
        return schedule;
    }
 }
