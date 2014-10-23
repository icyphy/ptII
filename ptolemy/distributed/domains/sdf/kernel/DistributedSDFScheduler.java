/* A Scheduler for the Distributed-SDF domain

 @Copyright (c) 2005-2014 The Regents of Aalborg University.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
 HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.distributed.domains.sdf.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Fraction;

///////////////////////////////////////////////////////////////////
////DistributedSDFScheduler

/**

 A scheduler that extends the SDFScheduler. This class relies on the extended
 SDFScheduler to calculate sequential schedules. In case a parallelSchedule
 is requested, it performs a topological sort of the graph that can be
 constructed from the data dependencies among the actors.
 The existing SDFScheduler produces schedules in a depth-first fashion.
 From the topological sort a schedule is created containing subschedules that
 contain the actors in the different levels, indicating they can be executed
 in parallel.
 I.e.:
 <table border = "0" cellpadding = "0" cellspacing = "0" width = "300"
 align = "center">
 <tr>
 <td width = "20%" align = "center">&nbsp;</td>
 <td width = "20%" align = "center">&nbsp;</td>
 <td width = "20%" align = "center">A</td>
 <td width = "20%" align = "center">&nbsp;</td>
 <td width = "20%" align = "center">&nbsp;</td>
 </tr>
 <tr>
 <td width = "20%" align = "center">&nbsp;</td>
 <td width = "20%" align = "center">B</td>
 <td width = "20%" align = "center">&nbsp;</td>
 <td width = "20%" align = "center">C</td>
 <td width = "20%" align = "center">&nbsp;</td>
 </tr>
 <tr>
 <td width = "20%" align = "center">D</td>
 <td width = "20%" align = "center">&nbsp;</td>
 <td width = "20%" align = "center">E&nbsp;&nbsp;&nbsp;&nbsp;
 F</td>
 <td width = "20%" align = "center">&nbsp;</td>
 <td width = "20%" align = "center">G</td>
 </tr>
 </table>

 <ul>
 <li>Sequential: (ABDECFG)
 <li>Parallel: ((A)(BC)(DEFG))
 </ul>

 To allow extending the actor, the SDFScheduler class has been modified.
 The members:
 <ul>
 <li>_rateVariables
 <li>_externalRates
 <li>_firingVector
 </ul>
 and methods:
 <ul>
 <li>_serFiringVector
 <li>_simulateExternalInputs
 <li>_countUnfulfilledInputs
 <li>_computeMaximumFirings
 <li>_simulateInputConsumption
 <li>_getFiringCount
 </ul>
 have been modified their visibility from private to protected.

 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.domains.sdf.kernel.SDFScheduler
 */
public class DistributedSDFScheduler extends SDFScheduler {
    /** Construct a scheduler with no container(director)
     *  in the default workspace.
     */
    public DistributedSDFScheduler() {
        super();
    }

    /** Construct a scheduler in the given workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public DistributedSDFScheduler(Workspace workspace) {
        super(workspace);
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DistributedSDFScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a parallelSchedule result of performing a topological
     *  sort of the graph that can be constructed from the model's data
     *  dependencies. This method duplicates and modifies the code in
     *  _getSchedule from the extended SDFScheduler class. It differs on
     *  the call to the _scheduleConnectedActors, that in this case is
     *  substituted by a call to the _scheduleInParallelConnectedActors
     *  method.
     *  Modifications are marked with NEW!.
     *
     *  @return A parallelSchedule.
     *  @exception NotSchedulableException If the rates specified for
     *  the model imply that the model is not statically schedulable.
     *  @exception IllegalActionException If the rate parameters
     *  of the model are not correct, or the computed rates for
     *  external ports are not correct.
     */
    @SuppressWarnings("unused")
    protected Schedule _getParallelSchedule() throws NotSchedulableException,
    IllegalActionException {
        SDFDirector director = (SDFDirector) getContainer();
        CompositeActor model = (CompositeActor) director.getContainer();

        _checkDynamicRateVariables(model, _rateVariables);

        int vectorizationFactor = 1;

        //if (director instanceof SDFDirector) {
        Token token = director.vectorizationFactor.getToken();
        vectorizationFactor = ((IntToken) token).intValue();
        //}

        if (vectorizationFactor < 1) {
            throw new NotSchedulableException(this,
                    "The supplied vectorizationFactor must be "
                            + "a positive integer. " + "The given value was: "
                            + vectorizationFactor);
        }

        CompositeActor container = (CompositeActor) director.getContainer();

        // A linked list containing all the actors.
        LinkedList allActorList = new LinkedList();

        // Populate it.
        for (Iterator entities = container.deepEntityList().iterator(); entities
                .hasNext();) {
            ComponentEntity entity = (ComponentEntity) entities.next();

            // Fill allActorList with the list of things that we can schedule
            // FIXME: What if other things can be scheduled than actors?
            if (entity instanceof Actor) {
                allActorList.addLast(entity);
            }
        }

        // externalRates maps from external
        // ports to the number of tokens that that port
        // will produce or consume in each firing.
        // It gets populated with the fractional production ratios
        // and is used in the end to set final rates on external ports.
        // This map is initialized to zero.
        Map externalRates = new TreeMap(new DFUtilities.NamedObjComparator());

        // Initialize externalRates to zero.
        for (Iterator ports = container.portList().iterator(); ports.hasNext();) {
            IOPort port = (IOPort) ports.next();
            externalRates.put(port, Fraction.ZERO);
        }

        // An association between all the relations in a simulation and
        // and array of the maximum number of tokens that are ever
        // waiting on that relation.
        Map minimumBufferSize = new TreeMap(
                new DFUtilities.NamedObjComparator());

        // Initialize the buffer size of each relation to zero.
        for (Iterator relations = container.relationList().iterator(); relations
                .hasNext();) {
            Relation relation = (Relation) relations.next();
            minimumBufferSize.put(relation, Integer.valueOf(0));
        }

        // First solve the balance equations
        Map entityToFiringsPerIteration = _solveBalanceEquations(container,
                allActorList, externalRates);

        if (_debugging && VERBOSE) {
            _debug("Firing Ratios: " + entityToFiringsPerIteration.toString());
        }

        // A list that contains actors that do not fire.
        LinkedList deadActorList = new LinkedList();
        LinkedList liveActorList = new LinkedList();

        // Populate deadActorList.
        for (Iterator actors = allActorList.iterator(); actors.hasNext();) {
            ComponentEntity actor = (ComponentEntity) actors.next();

            // Remove this actor from the firing sequence if it will
            // not be fired.
            Fraction firing = (Fraction) entityToFiringsPerIteration.get(actor);

            if (_debugging && VERBOSE) {
                _debug("Actor " + actor.getName() + "fires "
                        + firing.getNumerator() + " times.");
            }

            if (firing.getNumerator() == 0) {
                if (_debugging && VERBOSE) {
                    _debug("and will be removed because "
                            + "it is not being fired.");
                }

                deadActorList.add(actor);
            } else {
                liveActorList.add(actor);
            }
        }

        // Normalize the number of for each actor using the
        // vectorizationFactor.
        _vectorizeFirings(vectorizationFactor, entityToFiringsPerIteration,
                externalRates);

        // Set the firing vector.
        _firingVector = entityToFiringsPerIteration;

        if (_debugging) {
            _debug("Normalized Firing Counts:");
            _debug(entityToFiringsPerIteration.toString());
        }

        /**************************************************
         * > NEW!                                          *
         **************************************************/

        // Schedule all the actors using the calculated firings.
        Schedule result = _scheduleInParallelConnectedActors(minimumBufferSize,
                externalRates, liveActorList, container, allActorList);

        /**************************************************
         * < NEW!                                         *
         **************************************************/
        if (_debugging && VERBOSE) {
            _debug("Firing Vector:");
            _debug(entityToFiringsPerIteration.toString());
        }

        // Set parameters on each actor that contain the number
        // of firings in an iteration.
        _saveFiringCounts(entityToFiringsPerIteration);

        // Set the rate parameters of any external ports.
        _saveContainerRates(externalRates);

        // Set the schedule to be valid.
        setValid(true);
        _externalRates = externalRates;
        return result;
    }

    /** Return the scheduling sequence. In case the parameter
     *  parallelSchedule is true, it returns a parallel Schedule,
     *  otherwise it calls the parent's _getSchedule() method returning
     *  the given result.
     *
     *  @return A schedule of the deeply contained opaque entities
     *  in the firing order, sequential or parallel depending of the
     *  value of the parameter parallelSchedule.
     *  @exception NotSchedulableException If the rates specified for
     *  the model imply that the model is not statically schedulable.
     *  @exception IllegalActionException If the rate parameters
     *  of the model are not correct, or the computed rates for
     *  external ports are not correct.
     */
    @Override
    protected Schedule _getSchedule() throws NotSchedulableException,
    IllegalActionException {
        boolean parallelSchedule = false;
        Schedule schedule = null;

        DistributedSDFDirector director = (DistributedSDFDirector) getContainer();

        Token token = director.parallelSchedule.getToken();
        parallelSchedule = ((BooleanToken) token).booleanValue();

        if (parallelSchedule) {
            schedule = _getParallelSchedule();
        } else {
            schedule = super._getSchedule();
        }

        return schedule;
    }

    /** Duplicate with modifications of the _scheduleConnectedActors
     *  method.
     *  Modifications are marked with NEW!.
     *
     *  @param minimumBufferSize A map from relation to an Integer
     *  representing the minimum size buffer necessary for the computed
     *  schedule.  The map will be populated during the execution of this
     *  method.
     *  @param externalRates Map from external port to an Integer
     *  representing the number of tokens produced or consumed from
     *  that port during the course of an iteration.
     *  @param actorList The actors that need to be scheduled.
     *  @param container The container.
     *  @param allActorList All the actors, including those that do
     *  not need to be scheduled.  These actors will still be
     *  initialized, which means we must take their initial tokens
     *  into account when calculating buffer sizes.
     *  @return An instance of the Schedule class, indicating the order
     *  in which actors should fire.
     *  @exception NotSchedulableException If the algorithm encounters an SDF
     *  graph that is not consistent with the firing vector, or detects an
     *  inconsistent internal state, or detects a graph that cannot be
     *  scheduled.
     *
     */
    @SuppressWarnings("unused")
    protected Schedule _scheduleInParallelConnectedActors(
            Map minimumBufferSize, Map externalRates, LinkedList actorList,
            CompositeActor container, LinkedList allActorList)
                    throws NotSchedulableException {
        // A linked list containing all the actors that have no inputs.
        LinkedList readyToScheduleActorList = new LinkedList();

        Schedule newSchedule = new Schedule();

        /**************************************************
         * > NEW!                                         *
         **************************************************/

        // Auxiliar Schedule to build the sublevels.
        Schedule auxSchedule = new Schedule();

        /**************************************************
         * < NEW!                                         *
         **************************************************/

        // An association between each actor and the number of firings
        // for that actor that remain to be simulated.
        Map firingsRemainingVector = new TreeMap(
                new DFUtilities.NamedObjComparator());

        // Initialized the firingsRemainingVector to the current
        // firing vector.
        firingsRemainingVector.putAll(_firingVector);

        // A list of all that actors that we have not yet completely scheduled.
        LinkedList unscheduledActorList = new LinkedList();
        unscheduledActorList.addAll(actorList);

        /**************************************************
         * > NEW!                                          *
         **************************************************/

        // Keeps a list of the actors on the same level.
        LinkedList parallelLevel = new LinkedList();

        /**************************************************
         * < NEW!                                          *
         **************************************************/
        try {
            // Initializing waitingTokens at all the input ports of actors and
            // output ports of the model to zero is not necessary because
            // SDFReceiver.clear() does it.
            // Simulate the creation of initialization tokens (delays).
            // Fill readyToScheduleActorList with all the actors that have
            // no unfulfilled input ports, and are thus ready to fire.
            // This includes actors with no input ports and those
            // whose input ports have consumption rates of zero.
            Iterator actors = actorList.iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                int firingsRemaining = ((Integer) firingsRemainingVector
                        .get(actor)).intValue();

                if (firingsRemaining == 0) {
                    unscheduledActorList.remove(actor);
                    continue;
                }

                int inputCount = _countUnfulfilledInputs(actor, actorList, true);

                if (inputCount == 0) {
                    /**************************************************
                     * > NEW!                                         *
                     **************************************************/

                    // Changed from addFirst to addLast, this does not really
                    // matter since this is are all the sources that have
                    // no input requirements to be fired.
                    readyToScheduleActorList.addLast(actor);

                    /**************************************************
                     * > NEW!                                         *
                     **************************************************/
                }

                if (_debugging && VERBOSE) {
                    _debug("Actor " + ((ComponentEntity) actor).getName()
                            + " has " + inputCount + " unfulfilledInputs.");
                }
            }

            // Simulate production of initial tokens.
            actors = actorList.iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                Iterator outputPorts = actor.outputPortList().iterator();

                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort) outputPorts.next();
                    int count = DFUtilities.getTokenInitProduction(outputPort);

                    if (_debugging && VERBOSE) {
                        _debug("Simulating " + count + " initial tokens "
                                + "created on " + outputPort);
                    }

                    if (count > 0) {
                        /**************************************************
                         * > NEW!                                         *
                         **************************************************/
                        _simulateTokensCreatedLast(outputPort, count,
                                actorList, readyToScheduleActorList);

                        /**************************************************
                         * < NEW!                                         *
                         **************************************************/
                    }
                }
            }

            // Simulate a number of tokens initially present on each
            // external input port.
            for (Iterator inputPorts = container.inputPortList().iterator(); inputPorts
                    .hasNext();) {
                IOPort port = (IOPort) inputPorts.next();
                int count = ((Integer) externalRates.get(port)).intValue();

                if (count > 0) {
                    _simulateExternalInputs(port, count, actorList,
                            readyToScheduleActorList);
                }
            }

            /**************************************************
             * > NEW!                                         *
             **************************************************/

            // We copy all the initial ready actors into the parallelLevel
            // to keep track of when we finalize processing every level.
            for (actors = readyToScheduleActorList.iterator(); actors.hasNext();) {
                // Changed from addFirst to addLast, this does not really
                // matter since this is are all the sources that have
                // no input requirements to be fired.
                parallelLevel.addLast(actors.next());
            }

            /**************************************************
             * > NEW!                                         *
             **************************************************/

            // While we have actors left, pick one that is ready and fire it.
            while (readyToScheduleActorList.size() > 0) {
                if (_debugging && VERBOSE) {
                    _debug("Actors that can be scheduled:");

                    for (Iterator readyActors = readyToScheduleActorList
                            .iterator(); readyActors.hasNext();) {
                        Entity readyActor = (Entity) readyActors.next();
                        _debug(readyActor.getFullName());
                    }

                    _debug("Actors with firings left:");

                    for (Iterator remainingActors = unscheduledActorList
                            .iterator(); remainingActors.hasNext();) {
                        Entity remainingActor = (Entity) remainingActors.next();
                        _debug(remainingActor.getFullName());
                    }
                }

                // Pick an actor that is ready to fire.
                Actor currentActor = (Actor) readyToScheduleActorList
                        .getFirst();

                // Remove it from the list of actors we are waiting to fire.
                while (readyToScheduleActorList.remove(currentActor)) {
                }

                /**************************************************
                 * > NEW!                                         *
                 **************************************************/

                // Remove it from the parallelLevel.
                while (parallelLevel.remove(currentActor)) {
                    ;
                }

                /**************************************************
                 * < NEW!                                         *
                 **************************************************/

                // Determine the number of times currentActor can fire.
                int numberOfFirings = _computeMaximumFirings(currentActor);

                // We should never schedule something more than the number
                // of times expected by the balance equations.  This might
                // happen because we assume an infinite number of tokens
                // are waiting on external ports.
                int firingsRemaining = ((Integer) firingsRemainingVector
                        .get(currentActor)).intValue();

                if (numberOfFirings > firingsRemaining) {
                    numberOfFirings = firingsRemaining;
                }

                if (_debugging && VERBOSE) {
                    _debug("Scheduling actor " + currentActor.getName() + " "
                            + numberOfFirings + " times.");
                }

                // Update the firingsRemainingVector for this actor.
                firingsRemaining -= numberOfFirings;
                firingsRemainingVector.put(currentActor,
                        Integer.valueOf(firingsRemaining));

                if (_debugging && VERBOSE) {
                    _debug(currentActor.getName() + " should fire "
                            + firingsRemaining + " more times.");
                }

                // Simulate the tokens that are consumed by the actors
                // input ports.
                _simulateInputConsumption(currentActor, numberOfFirings);

                // Add it to the schedule numberOfFirings times.
                Firing firing = new Firing();
                firing.setActor(currentActor);
                firing.setIterationCount(numberOfFirings);

                /**************************************************
                 * > NEW!                                         *
                 **************************************************/
                // newSchedule to auxSchedule
                auxSchedule.add(firing);

                /**************************************************
                 * < NEW!                                         *
                 **************************************************/

                // Get all its outputPorts
                // and simulate the proper production of tokens.
                for (Iterator outputPorts = currentActor.outputPortList()
                        .iterator(); outputPorts.hasNext();) {
                    IOPort outputPort = (IOPort) outputPorts.next();

                    int count = DFUtilities.getTokenProductionRate(outputPort);

                    /**************************************************
                     * > NEW!                                         *
                     **************************************************/
                    // First to Last
                    _simulateTokensCreatedLast(outputPort, count
                            * numberOfFirings, unscheduledActorList,
                            readyToScheduleActorList);

                    /**************************************************
                     * < NEW!                                         *
                     **************************************************/
                }

                // System.out.println(parallelLevel.get(parallelLevel.
                // size()-1));

                /**************************************************
                 * > NEW!                                         *
                 **************************************************/

                // if the parallel level is empty we start with a new level.
                if (parallelLevel.size() == 0) {
                    newSchedule.add(auxSchedule);
                    parallelLevel.addAll(readyToScheduleActorList);
                    auxSchedule = new Schedule();
                }

                /**************************************************
                 * > NEW!                                         *
                 **************************************************/

                // Figure out what to do with the actor, now that it has been
                // scheduled.
                if (firingsRemaining < 0) {
                    // If we screwed up somewhere, and fired this more
                    // times than we thought we should have
                    // then throw an exception.
                    // This should never happen.
                    throw new InternalErrorException("Balance Equation "
                            + "solution does not agree with "
                            + "scheduling algorithm!");
                } else {
                    if (firingsRemaining == 0) {
                        // If we've fired this actor all the
                        // times that it should, then
                        // we get rid of it entirely.
                        if (_debugging && VERBOSE) {
                            _debug("Actor = " + currentActor
                                    + " is done firing.");
                        }

                        // Remove the actor from the unscheduledActorList
                        // since we don't need to fire it any more.
                        while (unscheduledActorList.remove(currentActor)) {
                            ;
                        }

                        if (_debugging && VERBOSE) {
                            _debug("Remaining actors:");

                            for (Iterator readyActors = readyToScheduleActorList
                                    .iterator(); readyActors.hasNext();) {
                                Entity entity = (Entity) readyActors.next();
                                _debug(entity.getFullName());
                            }
                        }
                    } else {
                        // Otherwise the actor still has firings left.
                        // Count the number of unfulfilled inputs.
                        int inputCount = _countUnfulfilledInputs(currentActor,
                                unscheduledActorList, false);

                        // We've already removed currentActor from
                        // readyToSchedule actors, and presumably
                        // fired it until it can be fired no more.
                        // This check is here for robustness...
                        // if the actor can still be scheduled
                        // i.e. all its inputs are satisfied, and it
                        // appears in the unscheduled actors list
                        // then put it at the END of readyToScheduleActorList.
                        if (inputCount <= 0
                                && unscheduledActorList.contains(currentActor)) {
                            readyToScheduleActorList.addFirst(currentActor);
                        }
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // This could happen if we call getTokenConsumptionRate on a
            // port that isn't a part of the actor.   This probably means
            // the graph is screwed up, or somebody else is mucking
            // with it.
            throw new InternalErrorException(this, ex,
                    "SDF Scheduler Failed internal consistency check.");
        } finally {
            if (_debugging && VERBOSE) {
                _debug("finishing loop");
            }
        }

        // If there are any actors left when we're done, then report the
        // error.
        if (unscheduledActorList.size() > 0) {
            StringBuffer string = new StringBuffer(
                    "Actors remain that cannot be scheduled!\n"
                            + "Scheduled actors:\n");

            List scheduledActorList = new LinkedList();
            scheduledActorList.addAll(actorList);
            scheduledActorList.removeAll(unscheduledActorList);

            for (Iterator actors = scheduledActorList.iterator(); actors
                    .hasNext();) {
                Entity entity = (Entity) actors.next();
                string.append(entity.getFullName() + "\n");
            }

            string.append("Unscheduled actors:\n");

            for (Iterator actors = unscheduledActorList.iterator(); actors
                    .hasNext();) {
                Entity entity = (Entity) actors.next();
                string.append(entity.getFullName() + "\n");
            }

            throw new NotSchedulableException(this, string.toString());
        }

        if (_debugging) {
            _debug("Schedule is:");
            _debug(newSchedule.toString());
        }

        return newSchedule;
    }

    /** Duplicate and modification of the _simulateTokensCreated
     *  method.
     *  Modifications are marked with NEW!.
     *
     *  @param outputPort The port that is creating the tokens.
     *  @param createdTokens The number of tokens to create.
     *  @param actorList The list of actors that are being scheduled.
     *  @param readyToScheduleActorList The list of actors that are ready
     *  to be scheduled.  This will be updated if any actors that receive
     *  tokens from outputPort are now ready to fire.
     *  @exception IllegalActionException If the rate parameters are invalid.
     */
    @SuppressWarnings("unused")
    protected void _simulateTokensCreatedLast(IOPort outputPort,
            int createdTokens, LinkedList actorList,
            LinkedList readyToScheduleActorList) throws IllegalActionException {
        Receiver[][] receivers = outputPort.getRemoteReceivers();

        if (_debugging && VERBOSE) {
            _debug("Creating " + createdTokens + " tokens on "
                    + outputPort.getFullName());
            _debug("source channels = " + receivers.length);
            _debug("width = " + outputPort.getWidth());
        }

        for (int channel = 0; channel < receivers.length; channel++) {
            if (receivers[channel] == null) {
                continue;
            }

            for (int copy = 0; copy < receivers[channel].length; copy++) {
                if (!(receivers[channel][copy] instanceof SDFReceiver)) {
                    // NOTE: This should only occur if it is null.
                    continue;
                }

                SDFReceiver receiver = (SDFReceiver) receivers[channel][copy];
                IOPort connectedPort = receivers[channel][copy].getContainer();
                ComponentEntity connectedActor = (ComponentEntity) connectedPort
                        .getContainer();

                // Increment the number of waiting tokens.
                receiver._waitingTokens += createdTokens;

                // Update the buffer size, if necessary.
                int capacity = receiver.getCapacity();

                if (capacity == SDFReceiver.INFINITE_CAPACITY
                        || receiver._waitingTokens > capacity) {
                    receiver.setCapacity(receiver._waitingTokens);
                }

                // Only proceed if the connected actor is
                // something we are scheduling.
                // The most notable time when this will not be
                // true is when a connection is made to the
                // inside of an opaque port.
                if (actorList.contains(connectedActor)) {
                    // Check and see whether the connectedActor
                    // can be scheduled.
                    int inputCount = _countUnfulfilledInputs(
                            (Actor) connectedActor, actorList, false);
                    int firingsRemaining = _getFiringCount(connectedActor);

                    // If so, then add it to the proper list.
                    // Note that the actor may appear more than once.
                    // This is OK, since we remove all of the appearances from
                    // the list when the actor is actually scheduled.
                    if (inputCount < 1 && firingsRemaining > 0) {
                        // Ned Stoffel suggested changing this from
                        // addLast() to addFirst() so as to minimize
                        // the number of tokens in transit.  "This leads
                        // to a markedly more serial schedule, as can
                        // be demonstrated by animating the simulations"

                        /**************************************************
                         * > NEW!                                         *
                         **************************************************/

                        // NEW! Addlast in order to create a topological sort.
                        readyToScheduleActorList.addLast(connectedActor);

                        /**************************************************
                         * < NEW!                                         *
                         **************************************************/
                    }
                }
            }
        }
    }
}
