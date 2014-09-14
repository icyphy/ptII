/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009-2013 The Regents of the University of California.
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
 */
package ptolemy.cg.kernel.generic.program.procedural.java.modular;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.util.DFUtilities;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.math.Fraction;

/** An SDF scheduler for modular code generation.

 *  @author Dai Bui
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 */
public class SDFModularScheduler extends SDFScheduler {

    // FIXME: This class has lots of duplicate code from SDFScheduler.

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get the firing vector for a given container.
     * @param container The container that is being scheduled.
     * @param vectorizationFactor The vectorization factor
     * @return A map from each actor to its fractional
     *  firing.
     * @exception IllegalActionException If thrown while solving the balance equations.
     */
    @SuppressWarnings("unused")
    public Map getFiringVector(CompositeActor container, int vectorizationFactor)
            throws IllegalActionException {
        if (vectorizationFactor < 1) {
            throw new NotSchedulableException(this,
                    "The supplied vectorizationFactor must be "
                            + "a positive integer. The given value was: "
                            + vectorizationFactor);
        }

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

        if (_debugging && VERBOSE) {
            _debug("Firing Ratios: " + entityToFiringsPerIteration.toString());
        }

        // Multiply the number of firings for each actor by the
        // vectorizationFactor.
        _vectorizeFirings(vectorizationFactor, entityToFiringsPerIteration,
                externalRates);

        // Set the firing vector.
        return entityToFiringsPerIteration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Solve the balance equations for the list of connected Actors.
     *  For each actor, determine the ratio that determines the rate at
     *  which it should fire relative to the other actors for the graph to
     *  be live and operate within bounded memory. Normalize this ratio
     *  into integer, which is the minimum number of firings of the actor
     *  to satisfy the balance equations.
     *
     *  @param container The container that is being scheduled.
     *  @param actorList The actors that we are interested in.
     *  @param externalRates A map from external ports of container to
     *  the fractional rates of that port.  This starts out initialized with
     *  Fraction.ZERO and will be populated during this method.
     *  @return A map from each actor to its fractional
     *  firing.
     *  @exception NotSchedulableException If the graph is not consistent
     *  under the synchronous dataflow model, or if the graph is not connected.
     *  @exception IllegalActionException If any called method throws it.
     */
    @Override
    protected Map _solveBalanceEquations(CompositeActor container,
            List actorList, Map externalRates) throws NotSchedulableException,
            IllegalActionException {
        // The map that we will return.
        // This will be populated with the fraction firing ratios for
        // each actor.
        // NOTE: This used to be a TreeMap using DFUtilities.NamedObjComparator().
        // However, that comparator is very slow.
        // Map entityToFiringsPerIteration = new TreeMap(
        //        new DFUtilities.NamedObjComparator());
        Map entityToFiringsPerIteration = new HashMap();

        if (actorList.size() == 0) {

            _checkDirectInputOutputConnection(container, externalRates);
            // If we've been given
            // no actors to do anything with, return an empty Map.
            return entityToFiringsPerIteration;
        }

        // The pool of actors that have their firingsPerIteration set,
        // but have not had their ports explored yet.
        LinkedList pendingActors = new LinkedList();

        // Set of actors that belong to the same cluster.
        Set clusteredActors = new HashSet();

        // Set of external ports that are conneted to
        // actors of the same cluster.
        Set clusteredExternalPorts = new HashSet();

        // The pool of Actors that have not been touched
        // yet. (i.e. all their firingsPerIteration are still set to
        // Fraction equal to -1/1)
        LinkedList remainingActors = new LinkedList();

        // Initialize remainingActors to contain all the actors we were given.
        remainingActors.addAll(actorList);

        // Initialize entityToFiringsPerIteration for each actor to -1.
        for (Iterator actors = remainingActors.iterator(); actors.hasNext();) {
            ComponentEntity entity = (ComponentEntity) actors.next();
            entityToFiringsPerIteration.put(entity, _minusOne);
        }

        // Ned Stoffel's change to support disconnected graphs:
        // Finally, the schedule can jump from one island to
        // another among the disconnected graphs. There is nothing
        // to force the scheduler to finish executing all actors
        // on one island before firing actors on another
        // island. However, the order of execution within an
        // island should be correct.
        while (!remainingActors.isEmpty()) {
            clusteredActors.clear();
            clusteredExternalPorts.clear();

            ComponentEntity actor = _pickZeroRatePortActor(remainingActors);

            if (actor == null) {
                actor = (ComponentEntity) remainingActors.removeFirst();
            } else {
                remainingActors.remove(actor);
            }

            clusteredActors.add(actor);

            entityToFiringsPerIteration.put(actor, new Fraction(1));
            pendingActors.addLast(actor);

            while (!pendingActors.isEmpty()) {
                Actor currentActor = (Actor) pendingActors.removeFirst();
                Iterator actorPorts = ((ComponentEntity) currentActor)
                        .portList().iterator();

                while (actorPorts.hasNext()) {
                    IOPort currentPort = (IOPort) actorPorts.next();
                    _propagatePort(container, currentPort,
                            entityToFiringsPerIteration, externalRates,
                            remainingActors, pendingActors, clusteredActors,
                            clusteredExternalPorts);
                }
            }

            // Now we have _clusteredActors, which contains actors in
            // one cluster (they are connected). Find the LCM of their
            // denominator and normalize their firings. This means firings
            // of actors are only normalized within their cluster.
            int lcm = 1;

            for (Iterator actors = clusteredActors.iterator(); actors.hasNext();) {
                Actor currentActor = (Actor) actors.next();
                Fraction fraction = (Fraction) entityToFiringsPerIteration
                        .get(currentActor);
                int denominator = fraction.getDenominator();
                lcm = Fraction.lcm(lcm, denominator);
            }

            // Got the normalizing factor.
            Fraction lcmFraction = new Fraction(lcm);

            for (Iterator actors = clusteredActors.iterator(); actors.hasNext();) {
                Actor currentActor = (Actor) actors.next();
                Fraction repetitions = ((Fraction) entityToFiringsPerIteration
                        .get(currentActor)).multiply(lcmFraction);

                if (repetitions.getDenominator() != 1) {
                    throw new InternalErrorException(
                            "Failed to properly perform"
                                    + " fraction normalization.");
                }

                entityToFiringsPerIteration.put(currentActor, repetitions);
            }

            for (Iterator externalPorts = clusteredExternalPorts.iterator(); externalPorts
                    .hasNext();) {
                IOPort port = (IOPort) externalPorts.next();
                Fraction rate = ((Fraction) externalRates.get(port))
                        .multiply(lcmFraction);

                if (rate.getDenominator() != 1) {
                    throw new InternalErrorException(
                            "Failed to properly perform"
                                    + " fraction normalization.");
                }

                externalRates.put(port, rate);
            }

            clusteredActors.clear();
            clusteredExternalPorts.clear();

        }

        _checkDirectInputOutputConnection(container, externalRates);

        if (!remainingActors.isEmpty()) {
            // If there are any actors left that we didn't get to, then
            // this is not a connected graph, and we throw an exception.
            StringBuffer messageBuffer = new StringBuffer(
                    "SDF scheduler found disconnected actors! "
                            + "Usually, disconnected actors in an SDF model "
                            + "indicates an error.  If this is not an error, try "
                            + "setting the SDFDirector parameter "
                            + "allowDisconnectedGraphs to true.");

            // Look through all the unreached actors.  If any of them are
            // in transparent composite actors that contain PortParameters,
            // print a message.
            // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4086

            // We only print messages about the first 99 PortParameters.
            int count = 0;
            StringBuffer portParameterMessageBuffer = new StringBuffer();
            Set portParametersFound = new HashSet();
            Set containersSeen = new HashSet();
            for (Iterator actors = actorList.iterator(); actors.hasNext()
                    && count < 100; count++) {
                NamedObj actor = (NamedObj) actors.next();
                NamedObj actorContainer = actor.getContainer();
                if (actorContainer instanceof CompositeActor
                        && !((CompositeActor) actorContainer).isOpaque()
                        && !containersSeen.contains(actorContainer)) {
                    containersSeen.add(actorContainer);
                    List portParameters = actorContainer
                            .attributeList(PortParameter.class);
                    for (Object portParameter : portParameters) {
                        if (!portParametersFound.contains(portParameter)) {
                            portParametersFound.add(portParameter);
                            portParameterMessageBuffer
                                    .append(((PortParameter) portParameter)
                                            .getFullName() + " ");
                            if (count > 100) {
                                break;
                            }
                        }
                    }
                }
            }
            if (portParameterMessageBuffer.length() > 0) {
                messageBuffer
                        .append("Note that some of the unreached actors are in "
                                + "transparent composite actors that have PortParameters.  "
                                + "A transparent composite actor is composite actor that has "
                                + "no local director.  Transparent composite actors and "
                                + "PortParameters are not compatible, the workaround is to "
                                + "insert a director or remove the PortParameter.  "
                                + "\nThe PortParameters:\n"
                                + portParameterMessageBuffer.toString());
                if (count >= 99) {
                    messageBuffer.append("...");
                }
            }

            messageBuffer.append("\nUnreached Actors:\n");
            count = 0;
            for (Iterator unreachedActors = remainingActors.iterator(); unreachedActors
                    .hasNext() && count < 100; count++) {
                NamedObj unreachedActor = (NamedObj) unreachedActors.next();
                messageBuffer.append(unreachedActor.getFullName() + " ");
            }

            if (count >= 99) {
                messageBuffer.append("...");
            }
            messageBuffer.append("\nReached Actors:\n");

            List reachedActorList = new LinkedList();
            reachedActorList.addAll(actorList);
            reachedActorList.removeAll(remainingActors);

            count = 0;
            for (Iterator actors = reachedActorList.iterator(); actors
                    .hasNext() && count < 100; count++) {
                Entity entity = (Entity) actors.next();
                messageBuffer.append(entity.getFullName() + " ");
            }

            if (count >= 99) {
                messageBuffer.append("...");
            }
            throw new NotSchedulableException(this, messageBuffer.toString());
        }

        return entityToFiringsPerIteration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Update the The external rates of those directly connected input
     *  and output ports to be 1. So a direct connection will transfer
     *  one token in each execution of the schedule.
     *
     *  @param container The container that is being scheduled.
     *  @param externalRates A map from external ports of container to
     *  the fractional rates of that port.  The external rates of
     *  those directly connected input and output ports will be updated
     *  to be 1 during this method.
     */
    private void _checkDirectInputOutputConnection(CompositeActor container,
            Map externalRates) {

        Iterator inputPorts = container.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            Fraction rate = (Fraction) externalRates.get(inputPort);
            if (rate.equals(Fraction.ZERO)) {

                // Check to make sure that if this port is an external
                // input port, then it does not drive the same relation as some
                // other output port or some other external input port.
                // This results in a non-deterministic merge and is illegal.

                Iterator connectedPorts = inputPort.deepInsidePortList()
                        .iterator();

                // Make sure any connected output ports are connected on
                // the inside.
                while (connectedPorts.hasNext()) {
                    IOPort connectedPort = (IOPort) connectedPorts.next();

                    // connectPort might be connected on the inside to the
                    // currentPort, which is legal.  The container argument
                    // is always the container of the director, so any port
                    // that has that container must be connected on the inside.
                    if (connectedPort.isOutput()
                            && connectedPort.getContainer() != container) {
                        throw new NotSchedulableException(inputPort,
                                connectedPort,
                                "External input port drive the same relation "
                                        + "as an output port. "
                                        + "This is not legal in SDF.");
                    } else if (connectedPort.isInput()
                            && connectedPort.getContainer() == container) {
                        throw new NotSchedulableException(inputPort,
                                connectedPort,
                                "External input port drives the same relation "
                                        + "as another external input port. "
                                        + "This is not legal in SDF.");
                    }
                }

                boolean isDirectionConnection = true;
                List insideSinkPorts = inputPort.insideSinkPortList();

                // A dangling port has zero rate.
                if (insideSinkPorts.isEmpty()) {
                    isDirectionConnection = false;
                } else {
                    // If the zero external port rate is due to the rate
                    // propagation from a contained actor (i.e., connected to the
                    // zero rate port of the actor), then the zero external rate
                    // must be preserved.
                    Iterator sinkPorts = insideSinkPorts.iterator();
                    while (sinkPorts.hasNext()) {
                        IOPort sinkPort = (IOPort) sinkPorts.next();
                        if (sinkPort.getContainer() != container) {
                            isDirectionConnection = false;
                            break;
                        }
                    }
                }

                if (isDirectionConnection) {
                    externalRates.put(inputPort, new Fraction(1));
                    Iterator sinkPorts = insideSinkPorts.iterator();
                    while (sinkPorts.hasNext()) {
                        IOPort sinkPort = (IOPort) sinkPorts.next();
                        externalRates.put(sinkPort, new Fraction(1));
                    }
                }
            }
        }
    }

    /** Search the given list of actors for one that contains at least
     *  one port that has zero rate.
     *
     *  @param actorList The list of all of the actors to search.
     *  @return An actor that contains at least one zero rate port, or null
     *  if no actor has a zero rate port.
     */
    private ComponentEntity _pickZeroRatePortActor(List actorList)
            throws IllegalActionException {
        for (Iterator actors = actorList.iterator(); actors.hasNext();) {
            ComponentEntity actor = (ComponentEntity) actors.next();

            // Check if this actor has any ports with rate of zero.
            for (Iterator ports = actor.portList().iterator(); ports.hasNext();) {
                IOPort port = (IOPort) ports.next();

                if (DFUtilities.getRate(port) == 0) {
                    return actor;
                }
            }
        }

        return null;
    }

    /** Propagate the number of fractional firings decided for this actor
     *  through the specified port.  Compute the fractional
     *  firing ratio for each actor that is connected to the given port.
     *  If we have not previously computed the ratio for an
     *  actor, then store the value in the given map of firing ratios and move
     *  the actor from the remainingActors list to the pendingActors list.
     *  If the value has been previously computed and is not the same,
     *  then the model is not schedulable and an exception will be thrown.
     *  Note that ports directly contained by the given container are
     *  handled slightly differently from other ports.  Most importantly,
     *  their rates are propagated to ports they are connected to on the
     *  inside, as opposed to ports they are connected to on the outside.
     *
     *  @param container The actor that is being scheduled.
     *  @param currentPort The port that we are propagating from.
     *  @param entityToFiringsPerIteration The current Map of
     *  fractional firing ratios for each actor.  This map will be
     *  updated if the ratio for any actor has not been previously
     *  computed.
     *  @param externalRates A map from external ports of container to
     *  the fractional rates of that port.  This will be updated
     *  during this method.
     *  @param remainingActors The set of actors that have not had their
     *  fractional firing set.  This will be updated during this method.
     *  @param pendingActors The set of actors that have had their rate
     *  set, but have not been propagated onwards.  This will be updated
     *  during this method.
     *  @param clusteredActors The set of actors that are within one
     *  cluster, i.e., they are connected.
     *  @param clusteredExternalPorts The set of external ports that
     *  are connected with the same cluster of actors.
     *
     *  @exception NotSchedulableException If the model is not
     *  schedulable.
     *  @exception IllegalActionException If the expression for a
     *  rate parameter is not valid.
     */
    @SuppressWarnings("unused")
    private void _propagatePort(CompositeActor container, IOPort currentPort,
            Map entityToFiringsPerIteration, Map externalRates,
            LinkedList remainingActors, LinkedList pendingActors,
            Set clusteredActors, Set clusteredExternalPorts)
            throws NotSchedulableException, IllegalActionException {
        ComponentEntity currentActor = (ComponentEntity) currentPort
                .getContainer();

        // First check to make sure that this port is not connected to
        // any other output ports on the outside.
        // This results in a non-deterministic merge and is illegal.
        // Do not do this test for output ports where we are propagating
        // inwards instead of outwards.
        if (currentPort.isOutput() && currentPort.getContainer() != container) {
            Iterator connectedPorts = currentPort.deepConnectedPortList()
                    .iterator();

            // Make sure any connected output ports are connected on
            // the inside.
            while (connectedPorts.hasNext()) {
                IOPort connectedPort = (IOPort) connectedPorts.next();

                // connectedPort might be connected on the inside to the
                // currentPort, which is legal.  The container argument
                // is always the container of the director, so any port
                // that has that container must be connected on the inside.
                if (connectedPort.isOutput()
                        && connectedPort.getContainer() != container) {
                    throw new NotSchedulableException(currentPort,
                            connectedPort,
                            "Output ports drive the same relation. "
                                    + "This is not legal in SDF.");
                } else if (connectedPort.isInput()
                        && connectedPort.getContainer() == container) {
                    throw new NotSchedulableException(currentPort,
                            connectedPort,
                            "Output port drives the same relation "
                                    + "as the external input port. "
                                    + "This is not legal in SDF.");
                }
            }
        }

        // Next check to make sure that if this port is an external
        // input port, then it does not drive the same relation as some
        // other output port or some other external input port.
        // This results in a non-deterministic merge and is illegal.
        if (currentPort.isInput() && currentPort.getContainer() == container) {
            Iterator connectedPorts = currentPort.deepInsidePortList()
                    .iterator();

            // Make sure any connected output ports are connected on
            // the inside.
            while (connectedPorts.hasNext()) {
                IOPort connectedPort = (IOPort) connectedPorts.next();

                // connectPort might be connected on the inside to the
                // currentPort, which is legal.  The container argument
                // is always the container of the director, so any port
                // that has that container must be connected on the inside.
                if (connectedPort.isOutput()
                        && connectedPort.getContainer() != container) {
                    throw new NotSchedulableException(currentPort,
                            connectedPort,
                            "External input port drive the same relation "
                                    + "as an output port. "
                                    + "This is not legal in SDF.");
                } else if (connectedPort.isInput()
                        && connectedPort.getContainer() == container) {
                    throw new NotSchedulableException(currentPort,
                            connectedPort,
                            "External input port drives the same relation "
                                    + "as another external input port. "
                                    + "This is not legal in SDF.");
                }
            }
        }

        //        Director director = (Director) getContainer();
        //        CompositeActor model = (CompositeActor) director.getContainer();

        CompositeActor model = container; //FIXME

        // Get the rate of this port.
        int currentRate;

        if (currentActor == model) {
            currentRate = 1;
        } else {
            currentRate = DFUtilities.getRate(currentPort);
        }

        // Port rates of less than zero are not valid.
        if (currentRate < 0) {
            throw new NotSchedulableException(currentPort,
                    "Rate cannot be less than zero.  It was: " + currentRate);
        }

        // Propagate to anything that this port is connected to.  For
        // external ports, this is anything that is connected on the
        // inside.  For ports of actors that are being scheduled, this is
        // anything that is connected on the outside.
        Iterator connectedPorts;

        if (currentPort.getContainer() == container) {
            // Find all the ports that are deeply connected to
            // current port on the inside.

            if (_debugging && VERBOSE) {
                // Move this inside and avoid FindBugs Dead Local Store
                connectedPorts = currentPort.deepInsidePortList().iterator();
                _debug("deepInsidePortList of " + currentPort);

                while (connectedPorts.hasNext()) {
                    _debug(connectedPorts.next().toString());
                }
            }

            connectedPorts = currentPort.deepInsidePortList().iterator();
        } else {
            connectedPorts = currentPort.deepConnectedPortList().iterator();
        }

        // For every port we are connected to.
        while (connectedPorts.hasNext()) {
            IOPort connectedPort = (IOPort) connectedPorts.next();

            ComponentEntity connectedActor = (ComponentEntity) connectedPort
                    .getContainer();

            if (_debugging && VERBOSE) {
                _debug("Propagating " + currentPort + " to "
                        + connectedActor.getName());
            }

            int connectedRate;

            if (connectedActor == model) {
                connectedRate = 1;
            } else {
                connectedRate = DFUtilities.getRate(connectedPort);
            }

            // currentFiring is the firing ratio that we've already
            // calculated for currentActor
            Fraction currentFiring = (Fraction) entityToFiringsPerIteration
                    .get(currentActor);

            // Compute the firing ratio that we think connected actor
            // should have, based on its connection to currentActor
            Fraction desiredFiring;

            // HDF actors might have zero rates...
            if (currentRate == 0 && connectedRate > 0) {
                // The current port of the current actor has a rate
                // of 0, and the current connected port of the
                // connected actor has a positive integer rate.
                // therefore, we must set the firing count of
                // the connected actor to 0 so that it will
                // not appear in the final static schedule.
                desiredFiring = Fraction.ZERO;
            } else if (currentRate > 0 && connectedRate == 0) {
                // The current port of the current actor has a
                // positive integer rate, and the current
                // connected port of the connected actor has
                // rate of 0. therefore, we set the firing
                // count of the current actor to 0 so that
                // it will not appear in the final static schedule.
                currentFiring = Fraction.ZERO;

                // Update the entry in the firing table.
                entityToFiringsPerIteration.put(currentActor, currentFiring);

                // Set the firing count of the connected actor to
                // be 1.
                desiredFiring = new Fraction(1);
            } else if (currentRate == 0 && connectedRate == 0) {
                // Give the connected actor the same rate as the
                // current actor.
                desiredFiring = currentFiring;
            } else {
                // Both the rates are non zero, so we can just do the
                // regular actor propagation.
                desiredFiring = currentFiring.multiply(new Fraction(
                        currentRate, connectedRate));
            }

            // Now, compare the firing ratio that was computed before
            // with what we just determined.
            // This should be either
            // the firing that we computed previously, or null
            // if the port is an external port, or _minusOne if
            // we have not computed the firing ratio for this actor yet.
            Fraction presentFiring = (Fraction) entityToFiringsPerIteration
                    .get(connectedActor);

            if (_debugging && VERBOSE) {
                _debug("presentFiring of connectedActor " + connectedActor
                        + " = " + presentFiring);
            }

            // if (presentFiring == null) {
            // Make sure to check for presentFiring == null here so that
            // we avoid a NullPointerException if the model is ill formed.
            // I had problems here with a bug in Publisher.clone() and
            // Subscriber.clone() where presentFiring was null.
            // Getting a NullPointerException is bad, we should check
            // for null and try to give a better message.
            if (connectedActor == model || presentFiring == null) {
                // We've gotten out to an external port.
                // Temporarily create the entry in the firing table.
                // This is possibly rather fragile.
                entityToFiringsPerIteration.put(connectedActor, desiredFiring);

                // Compute the external rate for this port.
                Fraction rate = currentFiring.multiply(new Fraction(
                        currentRate, 1));
                Fraction previousRate = (Fraction) externalRates
                        .get(connectedPort);

                if (previousRate == null) {
                    // This can happen if we somehow have a link to a port
                    // within a class definition.
                    // Give better error message than null pointer exception.
                    throw new InternalErrorException(
                            "Invalid connection found between ports: "
                                    + currentPort.getFullName() + " and "
                                    + connectedPort.getFullName());
                }

                //if (previousRate.equals(Fraction.ZERO)) {
                if (!clusteredExternalPorts.contains(connectedPort)) {
                    clusteredExternalPorts.add(connectedPort);
                    externalRates.put(connectedPort, rate);

                    _propagatePort(container, connectedPort,
                            entityToFiringsPerIteration, externalRates,
                            remainingActors, pendingActors, clusteredActors,
                            clusteredExternalPorts);
                } else if (!rate.equals(previousRate)) {
                    // The rates don't match.
                    throw new NotSchedulableException(this, "No solution "
                            + "exists for the balance equations.\n"
                            + "Graph is not "
                            + "consistent under the SDF domain "
                            + "detected on external port "
                            + connectedPort.getFullName());
                }

                // _propagatePort(container, connectedPort,
                //         entityToFiringsPerIteration, externalRates,
                //         remainingActors, pendingActors, clusteredActors,
                //        clusteredExternalPorts);
                // entityToFiringsPerIteration.remove(connectedActor);
            } else if (presentFiring.equals(_minusOne)) {
                // So we are propagating here for the first time.
                // Create the entry in the firing table.
                entityToFiringsPerIteration.put(connectedActor, desiredFiring);

                // Remove them from remainingActors.
                remainingActors.remove(connectedActor);
                clusteredActors.add(connectedActor);

                // and add them to the pendingActors.
                pendingActors.addLast(connectedActor);
            } else if (!presentFiring.equals(desiredFiring)) {
                // So we've already propagated here, but the
                // firingsPerIteration don't match.
                throw new NotSchedulableException(this, "No solution "
                        + "exists for the balance equations.\n"
                        + "Graph is not " + "consistent under the SDF domain "
                        + "detected on external port "
                        + connectedPort.getFullName());
            }

            if (_debugging && VERBOSE) {
                _debug("New Firing: ");
                _debug(entityToFiringsPerIteration.toString());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A fraction equal to -1.  Used in several places to indicate an
     * actor for which we have not determined the number of times it will
     * fire.
     */
    private Fraction _minusOne = new Fraction(-1);
}
