/* An Optimizing Scheduler for the SDF domain

 Copyright (c) 1998-2014 The Regents of the University of California.
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

package ptolemy.domains.sdf.optimize;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
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
import ptolemy.domains.sdf.optimize.OptimizingSDFDirector.OptimizationCriteria;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.Fraction;

///////////////////////////////////////////////////////////////////
////OptimizingSDFScheduler

/**
OptimizingSDFScheduler is the scheduler companion to the OptimizingSDFDirector
It works with the synchronous dataflow (SDF) model of computation to find
an optimized schedule according to a defined criterion.

<h1>Class comments</h1>
An OptimizingSDFScheduler is the class that determines an optimized static schedule.
<p>
See {@link ptolemy.domains.sdf.kernel.SDFScheduler} and
{@link ptolemy.domains.sdf.optimize.OptimizingSDFDirector} for more information.
</p>
@see ptolemy.domains.sdf.kernel.SDFScheduler
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector

@author Marc Geilen
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
 */

public class OptimizingSDFScheduler extends SDFScheduler {

    /**
     * The optimization criterion to use.
     */
    public OptimizationCriteria optimizationCriterion;

    /**
     * Construct an instance of an OptimizingSDFScheduler. Provide container and name as
     * usual and an optimization criterion <i>crit</i>.
     * @param container container
     * @param name name
     * @param crit optimization criterion
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public OptimizingSDFScheduler(OptimizingSDFDirector container, String name,
            OptimizationCriteria crit) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        optimizationCriterion = crit;
    }

    /** Return the scheduling sequence.  An exception will be thrown if the
     *  graph is not schedulable.  This occurs in the following circumstances:
     *  <ul>
     *  <li>The graph is not a connected graph.
     *  <li>No integer solution exists for the balance equations.
     *  <li>The graph contains cycles without delays (deadlock).
     *  <li>Multiple output ports are connected to the same broadcast
     *  relation. (equivalent to a non-deterministic merge)
     *  <li>The vectorizationFactor parameter of the director does
     *  not contain a positive integer.
     *  </ul>
     *
     *  @return A schedule of the deeply contained opaque entities
     *  in the firing order.
     *  @exception NotSchedulableException If the rates specified for
     *  the model imply that the model is not statically schedulable.
     *  @exception IllegalActionException If the rate parameters
     *  of the model are not correct, or the computed rates for
     *  external ports are not correct.
     */
    @Override
    protected Schedule _getSchedule() throws NotSchedulableException,
    IllegalActionException {
        SDFDirector director = (SDFDirector) getContainer();
        CompositeActor model = (CompositeActor) director.getContainer();

        _checkDynamicRateVariables(model, _rateVariables);

        int vectorizationFactor = 1;

        Token token = director.vectorizationFactor.getToken();
        vectorizationFactor = ((IntToken) token).intValue();

        if (vectorizationFactor < 1) {
            throw new NotSchedulableException(this,
                    "The supplied vectorizationFactor must be "
                            + "a positive integer. The given value was: "
                            + vectorizationFactor);
        }

        CompositeActor container = (CompositeActor) director.getContainer();

        // A linked list containing all the actors.
        List allActorList = container.deepEntityList();

        // externalRates maps from external
        // ports to the number of tokens that that port
        // will produce or consume in each firing.
        // It gets populated with the fractional production ratios
        // and is used in the end to set final rates on external ports.
        // This map is initialized to zero.
        // NOTE: This used to be a TreeMap using DFUtilities.NamedObjComparator().
        // However, that comparator is very slow.
        // FIXME: Why not get this via the container of the receivers?
        // or better yet, cache it in the receivers?
        Map externalRates = new HashMap();

        // Initialize externalRates to zero.
        for (Iterator ports = container.portList().iterator(); ports.hasNext();) {
            IOPort port = (IOPort) ports.next();
            externalRates.put(port, Fraction.ZERO);
        }

        // First solve the balance equations
        Map entityToFiringsPerIteration = _solveBalanceEquations(container,
                allActorList, externalRates);

        // Multiply the number of firings for each actor by the
        // vectorizationFactor.
        _vectorizeFirings(vectorizationFactor, entityToFiringsPerIteration,
                externalRates);

        // Set the firing vector.
        _firingVector = entityToFiringsPerIteration;

        if (_debugging) {
            _debug("Normalized Firing Counts:");
            _debug(entityToFiringsPerIteration.toString());
        }

        // Schedule all the actors using the calculated firings.
        Schedule result = _scheduleConnectedActors(externalRates, allActorList,
                container);

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

    /** Create an optimal schedule for a set of actors.
     *  FIXME: contains a lot of duplicated code from same method in SDFScheduler
     *  Would be good to factor out common code, but I do not want to touch SDFScheduler
     *
     *  @param externalRates Map from external port to an Integer
     *   representing the number of tokens produced or consumed from
     *   that port during the course of an iteration.
     *  @param actorList The actors that need to be scheduled.
     *  @param container The container.
     *  @return An instance of the Schedule class, indicating the order
     *   in which actors should fire.
     *  @exception NotSchedulableException If the algorithm encounters an SDF
     *   graph that is not consistent with the firing vector, or detects an
     *   inconsistent internal state, or detects a graph that cannot be
     *   scheduled.
     */
    private Schedule _scheduleConnectedActors(Map externalRates,
            List actorList, CompositeActor container)
                    throws NotSchedulableException {
        // A linked list containing all the actors that have no inputs.
        LinkedList readyToScheduleActorList = new LinkedList();

        // optimizedSchedule holds the new schedule
        Schedule optimizedSchedule;

        // An association between each actor and the number of firings
        // for that actor that remain to be simulated.
        // NOTE: This used to be a TreeMap using DFUtilities.NamedObjComparator().
        // However, that comparator is very slow.
        // Map firingsRemainingVector = new TreeMap(
        //        new DFUtilities.NamedObjComparator());

        // FindBugs: Useless object stored in variable firingsRemainingVector
        //Map firingsRemainingVector = new HashMap();

        // Initialized the firingsRemainingVector to the current
        // firing vector.
        //firingsRemainingVector.putAll(_firingVector);

        // A list of all that actors that we have not yet completely scheduled.
        // FIXME: Is this list needed?
        LinkedList unscheduledActorList = new LinkedList();
        unscheduledActorList.addAll(actorList);

        try {
            // clear the receivers of input ports of all actors in actorList
            // as well as the external ports. This should cover all receivers
            // we are dealing with

            // clear all the receivers of input ports of actors in actorList
            Iterator actorsIterator = actorList.iterator();
            while (actorsIterator.hasNext()) {
                Actor actor = (Actor) actorsIterator.next();
                Iterator inputPorts = actor.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();
                    Receiver[][] receivers = inputPort.getReceivers();
                    if (receivers != null) {
                        for (int m = 0; m < receivers.length; m++) {
                            for (int n = 0; n < receivers[m].length; n++) {
                                ((SDFReceiver) receivers[m][n])._waitingTokens = 0;
                            }
                        }
                    }
                }
            }
            // clear all the receivers of the external output ports
            Iterator externalOutputPorts = container.outputPortList()
                    .iterator();
            while (externalOutputPorts.hasNext()) {
                IOPort outputPort = (IOPort) externalOutputPorts.next();
                Receiver[][] receivers = outputPort.getInsideReceivers();
                if (receivers != null) {
                    for (int m = 0; m < receivers.length; m++) {
                        for (int n = 0; n < receivers[m].length; n++) {
                            ((SDFReceiver) receivers[m][n])._waitingTokens = 0;
                        }
                    }
                }
            }

            // Simulate production of initial tokens.
            Iterator actors = actorList.iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                Iterator outputPorts = actor.outputPortList().iterator();

                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort) outputPorts.next();
                    int count = DFUtilities.getTokenInitProduction(outputPort);

                    if (count > 0) {
                        _simulateTokensCreated(outputPort, count);
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

            // Now the initial state is known and we can compute the optimal schedule
            OptimalScheduleFinder finder = new OptimalScheduleFinder(this,
                    optimizationCriterion);

            // Collect the repetition vector from _firingVector
            // exclude the composite actor from the vector
            HashMap repVec = new HashMap();
            Iterator rvi = _firingVector.keySet().iterator();
            while (rvi.hasNext()) {
                Actor rva = (Actor) rvi.next();
                // do not include the composite
                if (rva != container) {
                    repVec.put(rva, _firingVector.get(rva));
                }
            }
            // delegate the construction of the schedule to the OptimizedScheduleFinder
            //optimizedSchedule = finder.makeSchedule(repVec);
            optimizedSchedule = finder.makeScheduleGreedy(repVec);

            // Iterate over the schedule once to fix the buffer sizes.
            Iterator si = optimizedSchedule.iterator();
            while (si.hasNext()) {
                Firing firing = (Firing) si.next();
                Actor firingActor = firing.getActor();

                // simulate firing according to firing
                // simulate input consumption
                _simulateInputConsumption(firingActor, 1);
                // Get all its outputPorts
                // and simulate the proper production of tokens.
                for (Iterator outputPorts = firingActor.outputPortList()
                        .iterator(); outputPorts.hasNext();) {
                    IOPort outputPort = (IOPort) outputPorts.next();
                    int count = DFUtilities.getTokenProductionRate(outputPort);
                    _simulateTokensCreated(outputPort, count);
                }
            }

        } catch (IllegalActionException ex) {
            // This could happen if we call getTokenConsumptionRate on a
            // port that isn't a part of the actor.   This probably means
            // the graph is screwed up, or somebody else is mucking
            // with it.
            throw new InternalErrorException(this, ex,
                    "SDF Scheduler Failed internal consistency check.");
        }

        if (_debugging) {
            _debug("Schedule is:");
            _debug(optimizedSchedule.toString());
        }

        return optimizedSchedule;
    }

    /** Simulate the creation of tokens by the given output port when
     *  its actor fires.  If any actors that receive tokens are then ready to
     *  fire, given that only actors in the actor list are being scheduled, then
     *  add those actors to the list of actors that are ready to schedule.
     *  @param outputPort The port that is creating the tokens.
     *  @param createdTokens The number of tokens to create.
     */
    private void _simulateTokensCreated(IOPort outputPort, int createdTokens)
            throws IllegalActionException {
        // FIXME: Why are the actor lists lists rather than sets?
        Receiver[][] receivers = outputPort.getRemoteReceivers();

        for (int channel = 0; channel < receivers.length; channel++) {
            if (receivers[channel] == null) {
                continue;
            }

            for (int copy = 0; copy < receivers[channel].length; copy++) {
                if (!(receivers[channel][copy] instanceof SDFReceiver)) {
                    // NOTE: This should only occur if it is null.
                    assert receivers[channel][copy] == null;
                    continue;
                }

                SDFReceiver receiver = (SDFReceiver) receivers[channel][copy];
                // Increment the number of waiting tokens.
                receiver._waitingTokens += createdTokens;

                // Update the buffer size, if necessary.
                boolean enforce = ((BooleanToken) constrainBufferSizes
                        .getToken()).booleanValue();

                if (enforce) {
                    int capacity = receiver.getCapacity();

                    if (capacity == SDFReceiver.INFINITE_CAPACITY
                            || receiver._waitingTokens > capacity) {
                        receiver.setCapacity(receiver._waitingTokens);
                    }
                }
            }
        }
    }

    /**
     * Print a debug message.
     * @param message message to display
     */
    public void showDebug(String message) {
        if (_debugging) {
            _debug(message);
        }

    }

}
