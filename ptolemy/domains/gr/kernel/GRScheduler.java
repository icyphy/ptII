/* A Scheduler for the GR domain

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating red (chf@eecs.berkeley.edu)
@AcceptedRating red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.kernel;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import ptolemy.math.Fraction;

import java.util.*;

///////////////////////////////////////////////////////////
//// GRScheduler
/**

A scheduler that implements scheduling of the active parts of a GR
scene graph DAG.

@see ptolemy.actor.sched.Scheduler

@author C. Fong
@version $Id$
*/
public class GRScheduler extends Scheduler {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public GRScheduler() {
        super();
	    _localMemberInitialize();
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public GRScheduler(Workspace ws) {
        super(ws);
	    _localMemberInitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor, as supplied by
     *  by the port's "tokenConsumptionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     *  @exception IllegalActionException If the tokenConsumptionRate
     *   parameter has an invalid expression.
     */
    public static int getTokenConsumptionRate(IOPort port)
            throws IllegalActionException {
        if(port.isInput()) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Get the number of tokens that are produced on this output port
     * during initialization, as supplied by
     * by the port's "tokenInitProduction" parameter.   If the parameter
     * does not exist, then assume the actor is zero-delay and return
     * a value of zero.
     * @exception IllegalActionException If the tokenInitProduction
     *  parameter has an invalid expression.
     */
    //public static int getTokenInitProduction(IOPort p)
    //        throws IllegalActionException {
    //    Parameter param = (Parameter)p.getAttribute("tokenInitProduction");
    //    if(param == null)
    //        return 0;
    //    return ((IntToken)param.getToken()).intValue();
    //}

    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor during each firing,
     *  as supplied by
     *  by the port's "tokenProductionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     *  @exception IllegalActionException If the tokenProductionRate
     *   parameter has an invalid expression.
     */
    public static int getTokenProductionRate(IOPort port)
            throws IllegalActionException {
            if (port.isOutput()) {
                return 1;
            } else {
                return 0;
            }
    }

    /** Initialize the local data members of this object.  */
    protected void _localMemberInitialize() {
        _firingvector = new TreeMap(new _NamedObjComparator());
        _firingvectorvalid = true;
    }

    /** Return the scheduling sequence.  An exception will be thrown if the
     *  graph is not schedulable.  This occurs in the following circumstances:
     *  <ul>
     *  <li>The graph is not a connected graph.
     *  <li>No integer solution exists for the balance equations.
     *  <li>The graph contains cycles without delays (deadlock).
     *  <li>Multiple output ports are connected to the same broadcast
     *  relation. (equivalent to a non-deterministic merge)
     *  </ul>
     *
     * @return An Enumeration of the deeply contained opaque entities
     *  in the firing order.
     * @exception NotScheduleableException If the CompositeActor is not
     *  schedulable.
     */
    protected Enumeration _schedule() throws NotSchedulableException {
        StaticSchedulingDirector dir =
            (StaticSchedulingDirector)getContainer();
        CompositeActor ca = (CompositeActor)(dir.getContainer());

        // A linked list containing all the actors
        LinkedList AllActors = new LinkedList();
        Iterator entities = ca.deepEntityList().iterator();


        while(entities.hasNext()) {
            ComponentEntity a = (ComponentEntity)entities.next();

            if(a instanceof CompositeActor) {
        		if (_debugging) _debug("Scheduling contained system");
                Director containedDirector =
                    ((CompositeActor) a).getDirector();
                if(containedDirector instanceof StaticSchedulingDirector) {
                    Scheduler containedScheduler =
                        ((StaticSchedulingDirector) containedDirector)
                        .getScheduler();
                    try {
                        containedScheduler.schedule();
                    } catch (IllegalActionException e) {
                        // This should never happen.
                        throw new InternalErrorException(e.getMessage());
                    }
                }
            }

            // Fill AllActors with the list of things that we can schedule
            // FIXME: What if other things can be scheduled than actors?
            if(a instanceof Actor) AllActors.addLast(a);
        }

        // First solve the balance equations
        Map firings = null;
        try {
           firings = _solveBalanceEquations(AllActors);
        } catch (IllegalActionException ex) {
            throw new NotSchedulableException(this, "Check expression of "
                    + "rate and initial production parameters.");
        }
        
        Iterator Actors = firings.keySet().iterator();

        while(Actors.hasNext()) {

            Object actor = Actors.next();
            firings.put(actor, new Integer(1));
        }
        //_normalizeFirings(firings);

        _setFiringVector(firings);

        if (_debugging) {
            _debug("Firing Vector:");
            _debug(firings.toString());
        }

        // Schedule all the actors using the calculated firings.
        LinkedList result = _scheduleConnectedActors(AllActors);

        _setFiringVector(firings);

        if (_debugging) {
            _debug("Firing Vector:");
            _debug(firings.toString());
        }

        try {
            _setContainerRates();
        } catch (IllegalActionException ex) {
            throw new NotSchedulableException(this, "Check expression of "
                    + "rate and initial production parameters.");
        }

        setValid(true);

        return Collections.enumeration(result);
    }

   
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Count the number of inputports in the Actor that must be
     *  fulfilled before the actor can fire.  Ports that are connected
     *  to actors that we are not scheduling right now are assumed to
     *  be fulfilled.  Ports that have more tokens waiting on each of
     *  their channels than their input consumption rate are also
     *  already fulfilled.  All other ports are considered to be
     *  unfulfilled.
     *  @param a The actor
     *  @param unscheduledactors The set of actors that we are scheduling.
     *  @param waitingTokens The Map of tokens currently waiting on all the
     *  input ports.
     *  @return The number of unfulfilled inputs of a.
     *  @exception IllegalActionException If any called method throws it.
     */
    private int _countUnfulfilledInputs(Actor a,
            LinkedList actorList, Map waitingTokens)
            throws IllegalActionException {
	if (_debugging)
            _debug("counting unfulfilled inputs for " +
                    ((Entity) a).getFullName());
        Iterator ainputPorts = a.inputPortList().iterator();

	int inputCount = 0;
	while(ainputPorts.hasNext()) {
	    IOPort ainputPort = (IOPort) ainputPorts.next();
	    if (_debugging) _debug("checking input " +
                    ainputPort.getFullName());

	    Iterator cports = ainputPort.deepConnectedOutPortList().iterator();

	    boolean isOnlyExternalPort = true;
	    while(cports.hasNext()) {
		IOPort cport = (IOPort) cports.next();
		if(actorList.contains(cport.getContainer()))
		    isOnlyExternalPort = false;
	    }

	    int threshold =
		getTokenConsumptionRate(ainputPort);
	    if (_debugging) _debug("Threshold = " + threshold);
	    int[] tokens =
		(int []) waitingTokens.get(ainputPort);

	    boolean isAlreadyFulfilled = true;
	    int channel;
	    for(channel = 0;
		channel < ainputPort.getWidth();
		channel++) {
		if (_debugging) {
                    _debug("Channel = " + channel);
                    _debug("Waiting Tokens = " + tokens[channel]);
                }
		if(tokens[channel] < threshold)
		    isAlreadyFulfilled = false;
	    }
	    if(!isOnlyExternalPort && !isAlreadyFulfilled)
		inputCount++;
	}
	return inputCount;
    }

    /** Return the number of firings associated with the Actor.   The
     *  number of firings is stored in the _firingvector Map, indexed
     *  by the entity.
     */
    private int _getFiringCount(Entity entity) {
        return 1;
        //return ((Integer) _firingvector.get(entity)).intValue();
    }

  
    /** Propagate the number of fractional firings decided for this actor
     *  through the specified input port.   Set and verify the fractional
     *  firing for each Actor that is connected through this input port.
     *  Any actors that we calculate their firing vector for the first time
     *  are moved from RemainingActors to pendingActors.
     *
     *  @param currentPort The port that we are propagating from.
     *  @param firings The current Map of fractional firings for each
     *  Actor.
     *  @param remainingActors The set of actors that have not had their
     *  fractional firing set.
     *  @param pendingActors The set of actors that have had their rate
     *  set, but have not been propagated onwards.
     *  @exception NotSchedulableException If the CompositeActor is not
     *  schedulable.
     *  @exception IllegalActionException If any called method throws it.
     */
    private void _propagateInputPort(IOPort currentPort,
            Map firings,
            LinkedList remainingActors,
            LinkedList pendingActors)
            throws NotSchedulableException, IllegalActionException {

        ComponentEntity currentActor =
            (ComponentEntity) currentPort.getContainer();

        // Calculate over all the output ports of this actor.
        int currentRate = getTokenConsumptionRate(currentPort);

        if(currentRate > 0) {
            // Compute the rate for the Actor currentPort is connected to
            Iterator connectedPorts =
                currentPort.deepConnectedOutPortList().iterator();

            while(connectedPorts.hasNext()) {
                IOPort connectedPort =
                    (IOPort) connectedPorts.next();

                ComponentEntity connectedActor =
                    (ComponentEntity) connectedPort.getContainer();

                if (_debugging) _debug("Propagating input to " +
                        connectedActor.getName());

                int connectedRate =
                    getTokenProductionRate(connectedPort);

                // currentFiring is the firing that we've already
                // calculated for currentactor
                Fraction currentFiring =
                    (Fraction) firings.get(currentActor);

                // the firing that we think the connected actor should be,
                // based on currentActor
                Fraction desiredFiring =
                    currentFiring.multiply(
                            new Fraction(currentRate, connectedRate));

                // What the firing for connectedActor already is set to.
                // This should be either 0, or equal to desiredFiring.
                try {
                    Fraction presentFiring =
                        (Fraction) firings.get(connectedActor);
		    if(presentFiring == null) {			
		    } else if(presentFiring.equals(Fraction.ZERO)) {
                        // create the entry in the firing table
                        firings.put(connectedActor, desiredFiring);
                        // Remove them from remainingActors
                        remainingActors.remove(connectedActor);
                        // and add them to the pendingActors.
                        pendingActors.addLast(connectedActor);
                    }
                    else if(!presentFiring.equals(desiredFiring))
                        throw new NotSchedulableException("No solution " +
                                "exists for the balance equations.\n" +
                                "Graph is not " +
                                "consistent under the SDF domain");
                }
                catch (NoSuchElementException e) {
                    throw new InternalErrorException("GRScheduler: " +
                            "connectedActor " +
                            ((ComponentEntity) connectedActor).getName() +
                            "does not appear in the firings Map");
                }

                /*
                  else if(!presentFiring.equals(desiredFiring))
                  throw new NotSchedulableException("No solution " +
                  "exists for the balance equations.\n" +
                  "Graph is not" +
                  "consistent under the SDF domain");
                  }
                  catch (NoSuchElementException e) {
                  throw new InternalErrorException("GRScheduler: " +
                  "connectedActor " +
                  ((ComponentEntity) connectedActor).getName() +
                  "does not appear in the firings Map");
                  }
                */

                if (_debugging) {
                    _debug("New Firing: ");
                    _debug(firings.toString());
                }
            }
        }
    }

    /** Propagate the number of fractional firing decided for this actor
     *  through the specified output port.   Set or verify the fractional
     *  firing for each Actor that is connected through this output port.
     *  Any actors that we calculate their firing vector for the first time
     *  are moved from remainingActors to pendingActors.
     *
     *  @param currentPort The port that we are propagating from.
     *  @param firings The current Map of fractional firings for each
     *  Actor.
     *  @param remainingActors The set of actors that have not had their
     *  fractional firing set.
     *  @param pendingActors The set of actors that have had their rate
     *  set, but have not been propagated onwards.
     *  @exception NotSchedulableException If the CompositeActor is not
     *  schedulable.
     *  @exception IllegalActionException If any called method throws it.
     */
    private void _propagateOutputPort(IOPort currentPort,
            Map firings,
            LinkedList remainingActors,
            LinkedList pendingActors)
            throws NotSchedulableException, IllegalActionException {

        ComponentEntity currentActor =
            (ComponentEntity) currentPort.getContainer();

        // First check to make sure that this Port is not connected to
        // Any other output ports.  This results in a non-deterministic
        // merge and is illegal.
        Iterator connectedOutPorts =
            currentPort.deepConnectedOutPortList().iterator();

        while(connectedOutPorts.hasNext()) {
            IOPort connectedPort =
                (IOPort) connectedOutPorts.next();
            // connectPort might be connected on the inside to the
            // currentPort, which is legal.
            if(!connectedPort.getContainer().equals(
                    currentPort.getContainer().getContainer())) {
                throw new NotSchedulableException(
                        currentPort, connectedPort,
                        "Two output ports are connected " +
                        "on the same relation. " +
                        "This is not legal in SDF.");
            }
        }

        //Calculate over all the output ports of this actor.
        int currentRate = getTokenProductionRate(currentPort);

        if(currentRate > 0) {
            // Compute the rate for the Actor currentPort is connected to
            Iterator connectedPorts =
                currentPort.deepConnectedInPortList().iterator();

            while(connectedPorts.hasNext()) {
                IOPort connectedPort =
                    (IOPort) connectedPorts.next();

                ComponentEntity connectedActor =
                    (ComponentEntity) connectedPort.getContainer();

                if (_debugging) _debug("Propagating output to " +
                        connectedActor.getName());

                int connectedRate =
                    getTokenConsumptionRate(connectedPort);

                // currentFiring is the firing that we've already
                // calculated for currentactor
                Fraction currentFiring =
                    (Fraction) firings.get(currentActor);

                // the firing that we think the connected actor should be,
                // based on currentActor
                Fraction desiredFiring =
                    currentFiring.multiply(
                            new Fraction(currentRate, connectedRate));

                // What the firing for connectedActor already is set to.
                // This should be either 0, or equal to desiredFiring.
                try {
                    Fraction presentFiring =
                        (Fraction) firings.get(connectedActor);

                    if(presentFiring.equals(Fraction.ZERO)) {
                        firings.put(connectedActor, desiredFiring);
                        // Remove them from remainingActors
                        remainingActors.remove(connectedActor);
                        // and add them to the pendingActors.
                        pendingActors.addLast(connectedActor);
                    }
                    else if(!presentFiring.equals(desiredFiring))
                        throw new NotSchedulableException("No solution " +
                                "exists for the balance equations.\n" +
                                "Graph is not" +
                                "consistent under the SDF domain");
                }
                catch (NoSuchElementException e) {
                    throw new InternalErrorException("GRScheduler: " +
                            "connectedActor " +
                            ((ComponentEntity) connectedActor).getName() +
                            "does not appear in the firings Map");
                }

                if (_debugging) {
                    _debug("New Firing: ");
                    _debug(firings.toString());
                }
            }
        }
    }

    /** Create a schedule for a set of UnscheduledActors.  Given a valid
     *  firing vector, simulate the scheduling of the actors until the
     *  end of one synchronous dataflow iteration.
     *  Each actor will appear in the schedule exactly the number of times that
     *  minimally solves the balance equations and in an order where each
     *  actor has sufficient tokens on its inputs to fire.   Note that no
     *  claim is made that this is an optimal solution in any other sense.
     *
     *  @param UnscheduledActors The Actors that need to be scheduled.
     *  @return A LinkedList of the Actors in the order they should fire.
     *  @exception NotSchedulableException If the algorithm encounters an SDF
     *  graph that is not consistent with the firing vector, or detects an
     *  inconsistent internal state, or detects a graph that cannot be
     *  scheduled.
     */
    private LinkedList _scheduleConnectedActors(LinkedList actorList)
            throws NotSchedulableException {

        // A linked list containing all the actors that have no inputs
        LinkedList readyToScheduleActorList = new LinkedList();
        // A linked list that will contain our new schedule.
        LinkedList newSchedule = new LinkedList();

        // An association between All the input ports in a simulation and an
	// array of the number of tokens waiting on each relation of that port
    	Map waitingTokens = new TreeMap(new _NamedObjComparator());
        Map firingsRemainingVector = new TreeMap(new _NamedObjComparator());
        firingsRemainingVector.putAll(_firingvector);

	    LinkedList unscheduledActorList = new LinkedList();
	    unscheduledActorList.addAll(actorList);

        try {
	    // Initialize the waitingTokens at all the
	    // input ports to zero
	        Iterator schedulableEntities = actorList.iterator();
	        while(schedulableEntities.hasNext()) {
		        Actor a = (Actor)schedulableEntities.next();

		        Iterator ainputports = a.inputPortList().iterator();
		        while(ainputports.hasNext()) {
		            IOPort ainputport = (IOPort) ainputports.next();
		            int[] tokencount = new int[ainputport.getWidth()];
		            for(int channel = 0; channel < tokencount.length;
			        channel++)
			            tokencount[channel] = 0;
		            waitingTokens.put(ainputport, tokencount);
		        }
	        }


	    // Fill readyToScheduleActorList with all the actors that have
	    // no unfulfilled input ports, and are thus ready to fire.
	    schedulableEntities = actorList.iterator();
	    while(schedulableEntities.hasNext()) {
		Actor a = (Actor)schedulableEntities.next();

		int inputCount = _countUnfulfilledInputs(a, actorList,
                        waitingTokens);
		if(inputCount == 0)
		    readyToScheduleActorList.addFirst((ComponentEntity) a);

		if (_debugging) _debug("Actor "
                        + ((ComponentEntity) a).getName() +
                        " has " + inputCount + " unfulfilledInputs.");
	    }

	    while(readyToScheduleActorList.size() > 0) {
		if (_debugging) _debug("\nwaitingTokens: ");
		Iterator ports = waitingTokens.keySet().iterator();
		while(ports.hasNext()) {
		    IOPort port = (IOPort)ports.next();
		    int tokencount[] = (int[])waitingTokens.get(port);
		    if (_debugging) {
                        _debug("Port " + port.getFullName());
                        _debug("Number of channels = " + tokencount.length);
                    }
		    for(int channel = 0;
			channel < tokencount.length;
			channel++)
			if (_debugging) _debug("Channel " + channel + " has " +
                                tokencount[channel] + " tokens.");
		}

		if (_debugging) {
                    _debug("Actors that can be scheduled:");
                    Iterator actorsLeft = readyToScheduleActorList.iterator();
                    while(actorsLeft.hasNext()) {
                        Entity e = (Entity)actorsLeft.next();
                        _debug(e.getFullName());
                    }
                    _debug("Actors with firings left:");
                    actorsLeft = unscheduledActorList.iterator();
                    while(actorsLeft.hasNext()) {
                        Entity e = (Entity)actorsLeft.next();
                        _debug(e.getFullName());
                    }
		}

		// pick an actor that is ready to fire.
		ComponentEntity currentActor
		    = (ComponentEntity) readyToScheduleActorList.getFirst();
		// remove it from the list of actors we are waiting to fire
                while(readyToScheduleActorList.remove(currentActor));

		if (_debugging) {
                    _debug("Scheduling Actor " + currentActor.getName());
                }
		_simulateInputConsumption(currentActor, waitingTokens);

		// add it to the schedule
		newSchedule.addLast(currentActor);

		// Get all it's outputPorts.
		Iterator aOutputPorts =
		    ((Actor) currentActor).outputPortList().iterator();

		// And simulate the proper production of tokens.
		while(aOutputPorts.hasNext()) {
		    IOPort aOutputPort = (IOPort) aOutputPorts.next();

		    _simulateTokensCreated(aOutputPort,
                            1,
                            unscheduledActorList,
                            readyToScheduleActorList,
                            waitingTokens);
		}

		// Update the firingRemainingVector for this actor.
		int firingsRemaining =
                    ((Integer) firingsRemainingVector.get(currentActor)).
                    intValue();
		firingsRemaining -= 1;
                firingsRemainingVector.put(currentActor,
                        new Integer(firingsRemaining));

		if (_debugging) {
                    _debug(currentActor.getName() + " should fire " +
                            firingsRemaining + " more times.");
                }

		// Figure out what to do with the actor, now that it has been
		// scheduled.
		if(firingsRemaining < 0)
		    // If we screwed up somewhere, and fired this more
		    // times than we thought we should have
		    // then throw an exception.
		    // This should never happen.
		    throw new IllegalStateException("Balance Equation " +
                            "solution does not agree with " +
                            "scheduling algorithm");

		// If we've fired this actor all the times that it should, then
		// we get rid of it entirely.
		else {
		    if(firingsRemaining == 0) {
			_debug("Current Actor = " + currentActor);
			// remove the actor from the readyToScheduleActorList
			// so that it does not gt scheduled
			while(readyToScheduleActorList.remove(currentActor));
			// remove the actor from the unscheduledActorList
			// so that it does not get added back to the
			// readyToScheduleActorList
			while(unscheduledActorList.remove(currentActor));
			_debug("Remaining actors");
			Iterator actorsLeft =
                            readyToScheduleActorList.iterator();
			while(actorsLeft.hasNext()) {
			    Entity e = (Entity)actorsLeft.next();
			    if (_debugging) _debug(e.getFullName());
			}
		    }
		    // Otherwise the actor still has firings left
		    else {
			// Count the number of unfulfilled inputs.
			int inputCount =
			    _countUnfulfilledInputs((Actor)currentActor,
                                    unscheduledActorList,
                                    waitingTokens);
			// We've already removed currentActor from
			// ReadytoSchedule actors
			// so if it can be fired again right away,
			// put it back on the list.
			// if the actor can still be scheduled
			// i.e. all its inputs are satisfied, and it
			// appears in the unscheduled actors list
			// then put it
			// at the END of readyToScheduleActorList.
			if(inputCount < 1 &&
                                unscheduledActorList.contains(currentActor))
			    readyToScheduleActorList.addLast(currentActor);
		    }
		}
	    }
	}
        catch (IllegalActionException iae) {
            // This could happen if we call getTokenConsumptionRate on a
            // port that isn't a part of the actor.   This probably means
            // the graph is screwed up, or somebody else is mucking
            // with it.
            throw new InternalErrorException("SDF Scheduler Failed " +
                    "internal consistency check: " + iae.getMessage());
        } finally {
            if (_debugging) _debug("finishing loop");
	}

	if(unscheduledActorList.size() > 0) {
	    String s = new String("Actors remain that cannot be scheduled:\n");
	    Iterator actors = unscheduledActorList.iterator();
	    while(actors.hasNext()) {
		Entity actor = (Entity)actors.next();
		s += actor.getFullName() + "\n";
	    }
	    throw new NotSchedulableException(s);
	}

        Iterator eschedule = newSchedule.iterator();
        if (_debugging) {
            _debug("Schedule is:");
            while(eschedule.hasNext()) {
                _debug(((ComponentEntity) eschedule.next()).toString());
            }
        }
        return newSchedule;
    }

    /** Push the rates calculated for this system up to the contained Actor.
     *  This allows the container to be properly scheduled if it is
     *  in a hierarchical system
     *  @exception IllegalActionException If any called method throws it.
     */
    private void _setContainerRates()
            throws NotSchedulableException, IllegalActionException {
        Director director = (Director) getContainer();
        if(director == null)
            throw new NotSchedulableException("Scheduler must " +
                    "have a director in order to schedule.");

        CompositeActor container = (CompositeActor) director.getContainer();
        if(container == null) throw new NotSchedulableException(
                "The model must be contained within a CompositeActor in " +
                "order to be scheduled.");

        Iterator ports = container.portList().iterator();
        while(ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            // Extrapolate the Rates
            Iterator connectedports = port.insidePortList().iterator();
            int consumptionRate = 0;
            int productionRate = 0;
            //int initProduction = 0;
            if(connectedports.hasNext()) {
                IOPort cport = (IOPort) connectedports.next();
                Entity cactor = (Entity) cport.getContainer();
                consumptionRate = _getFiringCount(cactor) *
                    getTokenConsumptionRate(cport);
                productionRate = _getFiringCount(cactor) *
                    getTokenProductionRate(cport);
                //initProduction = _getFiringCount(cactor) *
                //    getTokenInitProduction(cport);
                if (_debugging) {
                    _debug("CPort " + cport.getName());
                    _debug("consumptionRate = " + consumptionRate);
                    _debug("productionRate = " + productionRate);
                    //_debug("initProduction = " + initProduction);
                }
            }
            // All the ports connected to this port must have the same rate
            while(connectedports.hasNext()) {
                IOPort cport = (IOPort) connectedports.next();
                Entity cactor = (Entity) cport.getContainer();
                int crate = _getFiringCount(cactor) *
                    getTokenConsumptionRate(cport);
                if(crate != consumptionRate) throw new NotSchedulableException(
                        port, cport, "Port " + cport.getName() +
                        " has an aggregate consumption rate of " + crate +
                        " which does not match the computed aggregate rate " +
                        "of " + port.getName() + " of " + consumptionRate +
                        "!");
                int prate = _getFiringCount(cactor) *
                    getTokenProductionRate(cport);
                if(prate != productionRate) throw new NotSchedulableException(
                        port, cport, "Port " + cport.getName() +
                        " has an aggregate production rate of " + prate +
                        " which does not match the computed aggregate rate " +
                        "of " + port.getName() + " of " + productionRate +
                        "!");
                //int initp = _getFiringCount(cactor) *
                //    getTokenInitProduction(cport);
                //if(initp != initProduction) throw new NotSchedulableException(
                //        port, cport, "Port " + cport.getName() +
                //        " has an aggregate init production of " + initp +
                //        " which does not match the computed aggregate " +
                //        "of " + port.getName() + " of " + initProduction +
                //        "!");

            }
            if (_debugging) {
                _debug("Port " + port.getName());
                _debug("consumptionRate = " + consumptionRate);
                _debug("productionRate = " + productionRate);
                //_debug("initProduction = " + initProduction);
            }
            // SDFAtomicActor blindly creates parameters with bad values.

            try {
                Parameter param;
                param = (Parameter)port.getAttribute("tokenConsumptionRate");
                if(param == null)
                    param = new Parameter(port,"tokenConsumptionRate",
                            new IntToken(1));
                param.setToken(new IntToken(consumptionRate));
                param = (Parameter)port.getAttribute("tokenProductionRate");
                if(param == null)
                    param = new Parameter(port,"tokenProductionRate",
                            new IntToken(1));
                param.setToken(new IntToken(productionRate));
                //param = (Parameter)port.getAttribute("tokenInitProduction");
                //if(param == null)
                //    param = new Parameter(port,"tokenInitProduction",
                //            new IntToken(1));
                //param.setToken(new IntToken(initProduction));
            } catch (Exception ex) {
            }
        }
    }

    /** Set the firing vector, which is a Map associating an Actor
     *  with the number of times that it will fire during an SDF iteration.
     *  Every object that this Scheduler is responsible for should have an
     *  entry, even if it is zero indicating that the Actor has not yet had
     *  its firings determined.
     *
     *  @param newfiringvector A Map from ComponentEntity to Integer.
     */
    private void _setFiringVector(Map newfiringvector) {
        _firingvector = newfiringvector;
        _firingvectorvalid = true;
    }

    /** Simulate the consumption of tokens by the actor during an execution.
     *  The entries in Map will be modified to reflect the number of
     *  tokens still waiting after the actor has consumed tokens for a firing.
     *  Also determine if enough tokens still remain at the inputs of the actor
     *  for it to fire again immediately.
     *
     *  @param currentActor The actor that is being simulated.
     *  @param waitingTokens A Map between each input IOPort and the number of
     *  tokens in the queue for that port.
     *  @return boolean Whether or not the actor can fire again right away
     *  after it has consumed tokens.
     *  @exception IllegalActionException If any called method throws it.
     */
    private boolean _simulateInputConsumption(ComponentEntity currentActor,
            Map waitingTokens)
            throws IllegalActionException {

        boolean stillReadyToSchedule = true;
        // update tokensWaiting on the actor's input ports.

        Iterator inputPorts = 
	    ((Actor) currentActor).inputPortList().iterator();
        while(inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int[] tokens =
		(int []) waitingTokens.get(inputPort);
	    int tokenrate =
		getTokenConsumptionRate(inputPort);
	    for(int channel = 0; channel < inputPort.getWidth(); channel++) {
		tokens[channel] -= tokenrate;

		// keep track of whether or not this actor can fire again
		// immediately
		if(tokens[channel] < tokenrate) stillReadyToSchedule = false;
	    }
	}
        return stillReadyToSchedule;
    }

    /**
     * Simulate the creation of tokens by the given output port when
     * its actor fires.  If any actors that receive tokens are then ready to
     * fire, given that only actors in the actor list are being scheduled, then
     * add those actors to the list of actors that are ready to schedule.
     * update the waiting tokens map with the tokens available on each
     * channel of each port.
     */
    private void _simulateTokensCreated(IOPort outputPort,
	    int createdTokens,
            LinkedList actorList,
            LinkedList readyToScheduleActorList,
            Map waitingTokens)
            throws IllegalActionException {

	Receiver[][] creceivers = outputPort.getRemoteReceivers();

	if (_debugging) {
            _debug("Creating " + createdTokens + " tokens on "
                    + outputPort.getFullName());
            _debug("source channels = " + creceivers.length);
        }
	int sourcechannel;
	for(sourcechannel = 0;
            sourcechannel < creceivers.length;
            sourcechannel++) {
	    if (_debugging) {
                _debug("destination receivers for channel "
                + sourcechannel + ": " + creceivers[sourcechannel].length);
            }
	    int destinationreceiver;
	    for(destinationreceiver = 0;
		destinationreceiver < creceivers[sourcechannel].length;
		destinationreceiver++) {
		IOPort connectedPort =
		    (IOPort) creceivers[sourcechannel][destinationreceiver].
		    getContainer();
		ComponentEntity connectedActor =
		    (ComponentEntity) connectedPort.getContainer();
		// Only proceed if the connected actor is something we are
		// scheduling.  The most notable time when this will not be
		// true is when a connections is made to the
		// inside of an opaque port.
		if(actorList.contains(connectedActor)) {
		    int destinationchannel =
			_getChannel(connectedPort,
                                creceivers[sourcechannel]
                                [destinationreceiver]
				    );
		    int[] tokens = (int[]) waitingTokens.get(connectedPort);
		    tokens[destinationchannel] += 1;
		    if (_debugging) {
                        _debug("Channel " + destinationchannel + " of " +
                                connectedPort.getName());
                    }
		    // Check and see if the connectedActor can be scheduled
		    int ival =
			_countUnfulfilledInputs((Actor)connectedActor,
                                actorList,
                                waitingTokens);
		    int firingsRemaining = 1;
		    // If so, then add it to the proper list.  Note that the
		    // actor may appear more than once.  This is OK, since we
		    // remove all of the appearances from the list when the
		    // actor is actually scheduled.
		    if((ival <= 0) && (firingsRemaining > 0)) {
			    readyToScheduleActorList.addLast(connectedActor);
		    }
		}
	    }
	}
    }

    /** Solve the Balance Equations for the list of connected Actors.
     *  For each actor, determine the ratio that determines the rate at
     *  which it should fire relative to the other actors for the graph to
     *  be live and operate within bounded memory.   This ratio is known as the
     *  fractional firing of the actor.
     *
     *  @param Actors The actors that we are interested in.
     *  @return A Map that associates each actor with its fractional
     *  firing.
     *  @exception NotSchedulableException If the graph is not consistent
     *  under the synchronous dataflow model.
     *  @exception NotSchedulableException If the graph is not connected.
     *  @exception IllegalActionException If any called method throws it.
     */
    private Map _solveBalanceEquations(List Actors)
            throws NotSchedulableException, IllegalActionException {

        // firings contains the Map that we will return.
        // It gets populated with the fraction firing ratios for
        // each actor
        Map firings = new TreeMap(new _NamedObjComparator());

        // remainingActors contains the pool of Actors that have not been
        // touched yet. (i.e. all their firings are still set to Fraction.ZERO)
        LinkedList remainingActors = new LinkedList();

        // pendingActors have their firings set, but have not had their
        // ports explored yet.
        LinkedList pendingActors = new LinkedList();

        // Are we done?  (Is pendingActors Empty?)
        boolean done = false;

        // Initialize remainingActors to contain all the actors we were given
        remainingActors.addAll(Actors);

        // Initialize firings for everybody to Zero
        Iterator enumActors = remainingActors.iterator();
        while(enumActors.hasNext()) {
            ComponentEntity e = (ComponentEntity) enumActors.next();
            firings.put(e, Fraction.ZERO);
        }

        try {
            // Pick an actor as a reference
            Actor a = (Actor) remainingActors.removeFirst();
            // And set it's rate to one per iteration
            firings.put(a, new Fraction(1));
            // And start the list to recurse over.
            pendingActors.addLast(a);
        }
        catch (NoSuchElementException e) {
            // if remainingActors.removeFirst() fails, then we've been given
            // no actors to do anything with, so return an empty Map
            return firings;
        }

        while(!done) try {
            // NOTE: Do not move this debug clause after the removeFirst()
            // call... that causes an infinite loop!
            if (_debugging) {
                _debug("pendingActors: ");
                _debug(pendingActors.toString());
            }
            // Get the next actor to recurse over
            Actor currentActor = (Actor) pendingActors.removeFirst();
            if (_debugging) {
                _debug("Balancing from " +
                        ((ComponentEntity) currentActor).getName());
            }

            // traverse all the input and output ports, setting the firings
            // for the actor(s)???? that each port is connected to relative
            // to currentActor.
            Iterator AllPorts =
                ((ComponentEntity) currentActor).portList().iterator();
            while(AllPorts.hasNext()) {
                IOPort currentPort = (IOPort) AllPorts.next();

                if(currentPort.isInput())
                    _propagateInputPort(currentPort, firings,
                            remainingActors, pendingActors);

                if(currentPort.isOutput())
                    _propagateOutputPort(currentPort, firings,
                            remainingActors, pendingActors);
            }
        }
        catch (NoSuchElementException e) {
            // Once we've exhausted pendingActors, this exception will be
            // thrown, causing us to terminate the loop.
	    // FIXME this is a bad way to do this.
            done = true;

            Iterator actors = remainingActors.iterator();

            // If there are any Actors left that we didn't get to, then
            // this is not a connected graph, and we throw an exception.
            if(actors.hasNext()) {
                String msg = "GR scheduler found disconnected actors: ";
                while (actors.hasNext()) {
                    NamedObj actor = (NamedObj)(actors.next());
                    msg += actor.getFullName() + " ";
                }
                throw new NotSchedulableException(msg);
            }
        }
        return firings;
    }

    /** Find the channel number of the given port that corresponds to the
     *  given receiver.  If the receiver is not contained within the port,
     *  throw an InternalErrorException.
     */
// FIXME: Move this functionality to the kernel.
    private int _getChannel(IOPort port, Receiver receiver)
            throws IllegalActionException {
	int width = port.getWidth();
	Receiver[][] receivers = port.getReceivers();
	int channel;
	if (_debugging) {
            _debug("-- getting channels on port " + port.getFullName());
            _debug("port width = " + width);
            _debug("number of channels = " + receivers.length);
        }
	for(channel = 0; channel < receivers.length; channel++) {
	    int receivernumber;
	    if (_debugging) {
                _debug("number of receivers in channel " + channel
                + " = " + receivers[channel].length);
            }
	    for(receivernumber = 0;
		receivernumber < receivers[channel].length;
		receivernumber++)
		if(receivers[channel][receivernumber] == receiver) {
                    if (_debugging) {
                        _debug("-- returning channel number:" + channel);
                    }
                    return channel;
                }
	}
	// Hmm...  didn't find it yet.  Port might be connected on the inside,
	// so try the inside relations.
	receivers = port.getInsideReceivers();
	for(channel = 0; channel < receivers.length; channel++) {
	    int receivernumber;
	    if (_debugging) {
                _debug("number of insidereceivers = "
                        + receivers[channel].length);
            }
	    for(receivernumber = 0;
		receivernumber < receivers[channel].length;
		receivernumber++) {
		if(receivers[channel][receivernumber] == receiver) {
                    return channel;
                }
            }
	}

	throw new InternalErrorException("Receiver not found in the port " +
                port.getFullName());
    }

    /** A comparator for Named Objects.  Note: getFullName() is not cached,
     *  so this implementation is quite slow.
     */
    private class _NamedObjComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if((o1 instanceof NamedObj)&&(o2 instanceof NamedObj)) {
    
                // Compare names.
                NamedObj n1 = (NamedObj) o1;
                NamedObj n2 = (NamedObj) o2;
                int compare = n1.getFullName().compareTo(n2.getFullName());
                if(compare != 0) return compare;
                // Compare class names.
                Class c1 = n1.getClass();
                Class c2 = n2.getClass();
                compare = c1.getName().compareTo(c2.getName());
                if(compare != 0) return compare;
                if(o1.equals(o2))
                    return 0;
                else {
                    // FIXME This should never happen, hopefully.  Otherwise
                    // the comparator needs to be made more specific.
                    throw new InternalErrorException("Comparator not " +
                            "capable of comparing not equal objects.");
                }
            } else
                throw new ClassCastException("Object must be NamedObjs");
        }
    }

    private Map _firingvector;
    private boolean _firingvectorvalid;
}
