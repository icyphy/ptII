/* A Scheduler for the SDF domain

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import ptolemy.math.Fraction;

import java.util.*;
import collections.CircularList;
import collections.LLMap;
import collections.HashedSet;

///////////////////////////////////////////////////////////
//// SDFScheduler
/**

A scheduler that implements basic scheduling of SDF graphs.  This
class calculates the SDF schedule in two phases.  First, the balance
equations for the rates between actors are solved to tdetermine the
<i>firing vector</i> (also known as the repetitions vector).  The
firing vector is the least integer solution such that the number of
tokens created on each channel of each relation is equal to the number
of tokens consumed.  In some cases, no solution exists.  Such graphs
are not executable under SDF
<p>

Then the actors are ordered such that each actor only fires when the
scheduler has determined that enough tokens will be present on its
input ports to allow it to fire.  In cases where the Dataflow graph is
cyclic, a valid firing vector exists, but no actor can fire, since
they all depend on the output of another actor.  This situation is
known as "Deadlock".  Deadlock must be prevented in SDF by manually
inserting delay actors, which represent initial tokens on each
relation.  Such delay actors are responsible for creating tokens
during initialization that will prevent deadlock.  Delay actors must
set their "TokenInitProduction" parameters to represent the number of
tokens they will create during initialization.  The SDFScheduler uses
the TokenInitProduction parameter to break the dependency in a cyclic
graph.

<p>

Note that this scheduler only ensures that the number of firings is
minimal.  Most notably, it does not attempt to minimize the size of
the buffers that are associated with each relation.  The resulting
schedule is a linear schedule (as opposed to a looped schedule) and is
not suitable for Multiprocessing environments.  <p> Any actors may be
scheduled by this scheduler, which will, by default, assume
homogeneous behavior for each actor.  (i.e. each output port produces
one token for each firing, and each input port consumes one token on
each firing, and no tokens are created during initialization.)  If
this is not the case then the parameters "TokenConsumptionRate",
"TokenProductionRate", and "TokenInitProduction" must be set.  The
SDFAtomicActor class provides easier access to these parameters.  <p>
Note that reconstructing the schedule is expensive, so the schedule is
locally cached for as long as possible, and mutations under SDF should
be avoided.

@see ptolemy.actor.sched.Scheduler
@see ptolemy.domains.sdf.kernel.SDFAtomicActor

@author Stephen Neuendorffer
@version $Id$
*/
public class SDFScheduler extends Scheduler {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public SDFScheduler() {
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
    public SDFScheduler(Workspace ws) {
        super(ws);
	_localMemberInitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the schedule.  Return the number of times that the given
     *  entity will fire in a single iteration of the system.
     */
    public int getFiringCount(Entity entity)
            throws IllegalActionException {
        schedule();
        return _getFiringCount(entity);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor, as supplied by
     *  by the port's "TokenConsumptionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     */
    protected int _getTokenConsumptionRate(IOPort p) {
        Parameter param = (Parameter)p.getAttribute("TokenConsumptionRate");
        if(param == null) {
            if(p.isInput())
                return 1;
            else
                return 0;
        } else
            return ((IntToken)param.getToken()).intValue();
    }

    /**
     * Get the number of tokens that are produced on this output port
     * during initialization, as supplied by
     * by the port's "TokenInitProduction" parameter.   If the parameter
     * does not exist, then assume the actor is zero-delay and return
     * a value of zero.
     */
    protected int _getTokenInitProduction(IOPort p) {
        Parameter param = (Parameter)p.getAttribute("TokenInitProduction");
        if(param == null)
            return 0;
        return ((IntToken)param.getToken()).intValue();
    }

    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor during each firing,
     *  as supplied by
     *  by the port's "TokenConsumptionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     */
    protected int _getTokenProductionRate(IOPort p) {
        Parameter param = (Parameter)p.getAttribute("TokenProductionRate");
        if(param == null) {
            if(p.isOutput())
                return 1;
            else
                return 0;
        }
        return ((IntToken)param.getToken()).intValue();
    }

    /** Initialize the local data members of this object.  */
    protected void _localMemberInitialize() {
        LLMap _firingvector = new LLMap();
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
     * @exception NotSchedulableException If the CompositeActor is not
     *  schedulable.
     */
    protected Enumeration _schedule() throws NotSchedulableException {
        StaticSchedulingDirector dir =
            (StaticSchedulingDirector)getContainer();
        CompositeActor ca = (CompositeActor)(dir.getContainer());

        // A linked list containing all the actors
        CircularList AllActors = new CircularList();
        Enumeration Entities = ca.deepGetEntities();


        while(Entities.hasMoreElements()) {
            ComponentEntity a = (ComponentEntity)Entities.nextElement();

            if(a instanceof CompositeActor) {
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
            if(a instanceof Actor) AllActors.insertLast(a);
        }

        // First solve the balance equations
        LLMap firings = _solveBalanceEquations(AllActors.elements());
        firings = _normalizeFirings(firings);

        _setFiringVector(firings);

        _debug("Firing Vector:");
        _debug(firings.toString());

        // Schedule all the actors using the calculated firings.
        CircularList result = _scheduleConnectedActors(AllActors);

        _setFiringVector(firings);

        _debug("Firing Vector:");
        _debug(firings.toString());

        _setContainerRates();

        setValid(true);

        return result.elements();
    }

    protected void _setTokenConsumptionRate(Entity e, IOPort port, int rate)
            throws NotSchedulableException {
        if(rate <= 0) throw new NotSchedulableException(
                "Rate must be > 0");
        if(!port.isInput()) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not an Input Port.");
        Port pp = e.getPort(port.getName());
        if(!port.equals(pp)) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not contained in Entity " +
                e.getName());
        Parameter param = (Parameter)
            port.getAttribute("TokenConsumptionRate");
        try {
            if(param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port,"TokenConsumptionRate",
                        new IntToken(rate));
            }
        } catch (Exception exception) {
            // This should never happen.
            // e might be NameDuplicationException, but we already
            // know it doesn't exist.
            // e might be IllegalActionException, but we've already
            // checked the error conditions
            throw new InternalErrorException(exception.getMessage());
        }
    }

    protected void _setTokenProductionRate(Entity e, IOPort port, int rate)
            throws NotSchedulableException {
        if(rate <= 0) throw new NotSchedulableException(
                "Rate must be > 0");
        if(!port.isOutput()) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not an Output Port.");
        Port pp = e.getPort(port.getName());
        if(!port.equals(pp)) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not contained in Entity " +
                e.getName());
        Parameter param = (Parameter)
            port.getAttribute("TokenProductionRate");
        try {
            if(param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port,"TokenProductionRate",
                        new IntToken(rate));
            }
        } catch (Exception exception) {
            // This should never happen.
            // e might be NameDuplicationException, but we already
            // know it doesn't exist.
            // e might be IllegalActionException, but we've already
            // checked the error conditions
            throw new InternalErrorException(exception.getMessage());
        }
    }

    protected void _setTokenInitProduction(Entity e, IOPort port, int rate)
            throws NotSchedulableException {
        if(rate <= 0) throw new NotSchedulableException(
                "Rate must be > 0");
        if(!port.isOutput()) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not an Input Port.");
        Port pp = e.getPort(port.getName());
        if(!port.equals(pp)) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not contained in Entity " +
                e.getName());
        Parameter param = (Parameter)
            port.getAttribute("TokenInitProduction");
        try {
            if(param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port,"TokenInitProduction",
                        new IntToken(rate));
            }
        } catch (Exception exception) {
            // This should never happen.
            // e might be NameDuplicationException, but we already
            // know it doesn't exist.
            // e might be IllegalActionException, but we've already
            // checked the error conditions
            throw new InternalErrorException(exception.getMessage());
        }
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
            CircularList actorList, LLMap waitingTokens)
            throws IllegalActionException {
        Enumeration ainputPorts = a.inputPorts();
	_debug("counting unfufilled inputs for " +
	       ((Entity) a).getFullName());

	int inputCount = 0;
	while(ainputPorts.hasMoreElements()) {
	    IOPort ainputPort = (IOPort) ainputPorts.nextElement();
	    _debug("checking input " +
		   ainputPort.getFullName());

	    Enumeration cports = ainputPort.deepConnectedOutPorts();

	    boolean isonlyexternalport = true;
	    while(cports.hasMoreElements()) {
		IOPort cport = (IOPort) cports.nextElement();
		if(actorList.includes(cport.getContainer()))
		    isonlyexternalport = false;
	    }

	    int threshold =
		_getTokenConsumptionRate(ainputPort);
	    _debug("Threshold = " + threshold);
	    int[] tokens =
		(int []) waitingTokens.at(ainputPort);

	    boolean isalreadyfulfilled = true;
	    int channel;
	    for(channel = 0;
		channel < ainputPort.getWidth();
		channel++) {
		_debug("Channel = " + channel);
		_debug("Waiting Tokens = " + tokens[channel]);
		if(tokens[channel] < threshold)
		    isalreadyfulfilled = false;
	    }
	    if(!isonlyexternalport && !isalreadyfulfilled)
		inputCount++;
	}
	return inputCount;
    }

    /** Return the number of firings associated with the Actor.   This is
     *  equivalent to indexing into the Map returned by _getFiringVector and
     *  casting the result to an integer.
     */
    private int _getFiringCount(Entity entity) {
        _debug(_firingvector.toString());
        return ((Integer) _firingvector.at(entity)).intValue();
    }

    /** Return the firing vector, which is a LLMap associating an Actor
     *  with the number of times that it will fire during an SDF iteration.
     *  The firing vector is only guaranteed to be valid if the schedule
     *  is valid.
     *
     *  @return A LLMap from ComponentEntity to Integer.
     */
    private LLMap _getFiringVector() {
        return _firingvector;
    }

    /** Normalize fractional firing ratios into a firing vector that
     *  corresponds to a single SDF iteration.   Multiply all of the
     *  fractions by the GCD of their denominators.
     *
     *  @param Firings LLMap of firing ratios to be normalized
     *  @return The normalized firing vector.
     *  @exception InternalErrorException If the calculated GCD does not
     *  normalize all of the fractions.
     */
    private LLMap _normalizeFirings(LLMap firings) {
        Enumeration unnormalizedFirings = firings.elements();
        int lcm = 1;

        _debug("Normalizing Firings");
        // First find the lcm of all the denominators
        while(unnormalizedFirings.hasMoreElements()) {
            Fraction f = (Fraction) unnormalizedFirings.nextElement();
            int den = f.getDenominator();
            lcm = Fraction.lcm(lcm, den);
        }

        _debug("lcm = " + (new Integer(lcm)).toString());
        Enumeration Actors = firings.keys();

        Fraction lcmFraction = new Fraction(lcm);
        // now go back through and multiply by the lcm we just found, which
        // should normalize all the fractions to integers.
        while(Actors.hasMoreElements()) {

            Object actor = Actors.nextElement();
            _debug("normalizing Actor " +
                    ((ComponentEntity) actor).getName());
            Fraction reps = (Fraction) firings.at(actor);
            reps = reps.multiply(lcmFraction);
            if(reps.getDenominator() != 1)
                throw new InternalErrorException(
                        "Failed to properly perform " +
                        "fraction normalization");
            firings = (LLMap)
                firings.puttingAt(actor, new Integer(reps.getNumerator()));
        }
        return firings;
    }

    /** Propagate the number of fractional firings decided for this actor
     *  through the specified input port.   Set and verify the fractional
     *  firing for each Actor that is connected through this input port.
     *  Any actors that we calculate their firing vector for the first time
     *  are moved from RemainingActors to pendingActors.
     *
     *  @param currentPort The port that we are propagating from.
     *  @param firings The current LLMap of fractional firings for each
     *  Actor.
     *  @param remainingActors The set of actors that have not had their
     *  fractional firing set.
     *  @param pendingActors The set of actors that have had their rate
     *  set, but have not been propagated onwards.
     *  @exception NotSchedulableException If the CompositeActor is not
     *  schedulable.
     */
    private void _propagateInputPort(IOPort currentPort,
            LLMap firings,
            CircularList remainingActors,
            CircularList pendingActors)

        throws NotSchedulableException {

            ComponentEntity currentActor =
                (ComponentEntity) currentPort.getContainer();

            //Calculate over all the output ports of this actor.
            int currentRate =
                _getTokenConsumptionRate(currentPort);

            if(currentRate>0) {
                // Compute the rate for the Actor currentPort is connected to
                Enumeration connectedPorts =
                    currentPort.deepConnectedOutPorts();

                while(connectedPorts.hasMoreElements()) {
                    IOPort connectedPort =
                        (IOPort) connectedPorts.nextElement();

                    ComponentEntity connectedActor =
                        (ComponentEntity) connectedPort.getContainer();

                    _debug("Propagating input to " +
                            connectedActor.getName());

                    int connectedRate =
                        _getTokenProductionRate(connectedPort);

                    // currentFiring is the firing that we've already
                    // calculated for currentactor
                    Fraction currentFiring =
                        (Fraction) firings.at(currentActor);

                    // the firing that we think the connected actor should be,
                    // based on currentActor
                    Fraction desiredFiring =
                        currentFiring.multiply(
                                new Fraction(currentRate, connectedRate));

                    // What the firing for connectedActor already is set to.
                    // This should be either 0, or equal to desiredFiring.
                    try {
                        Fraction presentFiring =
                            (Fraction) firings.at(connectedActor);
			if(presentFiring.equals(Fraction.ZERO)) {
                            // create the entry in the firing table
                            firings.putAt(connectedActor, desiredFiring);
                            // Remove them from remainingActors
                            remainingActors.removeOneOf(connectedActor);
                            // and add them to the pendingActors.
                            pendingActors.insertLast(connectedActor);
                        }
                        else if(!presentFiring.equals(desiredFiring))
                            throw new NotSchedulableException("No solution " +
                                    "exists for the balance equations.\n" +
                                    "Graph is not" +
                                    "consistent under the SDF domain");
                    }
                    catch (NoSuchElementException e) {
                        throw new InternalErrorException("SDFScheduler: " +
                                "connectedActor " +
                                ((ComponentEntity) connectedActor).getName() +
                                "does not appear in the firings LLMap");
                    }

		    /*                    else if(!presentFiring.equals(desiredFiring))
                        throw new NotSchedulableException("No solution " +
                                "exists for the balance equations.\n" +
                                "Graph is not" +
                                "consistent under the SDF domain");
                }
                catch (NoSuchElementException e) {
                    throw new InternalErrorException("SDFScheduler: " +
                            "connectedActor " +
                            ((ComponentEntity) connectedActor).getName() +
                            "does not appear in the firings LLMap");
                }
		    */

                _debug("New Firing: ");
                _debug(firings.toString());
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
     *  @param firings The current LLMap of fractional firings for each
     *  Actor.
     *  @param remainingActors The set of actors that have not had their
     *  fractional firing set.
     *  @param pendingActors The set of actors that have had their rate
     *  set, but have not been propagated onwards.
     *  @exception NotSchedulableException If the CompositeActor is not
     *  schedulable.
     */
    private void _propagateOutputPort(IOPort currentPort,
            LLMap firings,
            CircularList remainingActors,
            CircularList pendingActors)
            throws NotSchedulableException {

        ComponentEntity currentActor =
            (ComponentEntity) currentPort.getContainer();

        // First check to make sure that this Port is not connected to
        // Any other output ports.  This results in a non-deterministic
        // merge and is illegal.
        Enumeration connectedOutPorts =
            currentPort.deepConnectedOutPorts();

        while(connectedOutPorts.hasMoreElements()) {
            IOPort connectedPort =
                (IOPort) connectedOutPorts.nextElement();
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
        int currentRate = _getTokenProductionRate(currentPort);

        if(currentRate > 0) {
            // Compute the rate for the Actor currentPort is connected to
            Enumeration connectedPorts =
                currentPort.deepConnectedInPorts();

            while(connectedPorts.hasMoreElements()) {
                IOPort connectedPort =
                    (IOPort) connectedPorts.nextElement();

                ComponentEntity connectedActor =
                    (ComponentEntity) connectedPort.getContainer();

                _debug("Propagating output to " +
                        connectedActor.getName());

                int connectedRate =
                    _getTokenConsumptionRate(connectedPort);

                // currentFiring is the firing that we've already
                // calculated for currentactor
                Fraction currentFiring =
                    (Fraction) firings.at(currentActor);
                // the firing that we think the connected actor should be,
                // based on currentActor
                Fraction desiredFiring =
                    currentFiring.multiply(
                            new Fraction(currentRate, connectedRate));

                // What the firing for connectedActor already is set to.
                // This should be either 0, or equal to desiredFiring.
                try {
                    Fraction presentFiring =
                        (Fraction) firings.at(connectedActor);

                    if(presentFiring.equals(Fraction.ZERO)) {
                        firings.putAt(connectedActor, desiredFiring);

                        // Remove them from remainingActors
                        remainingActors.removeOneOf(connectedActor);

                        // and add them to the pendingActors.
                        pendingActors.insertLast(connectedActor);
                    }
                    else if(!presentFiring.equals(desiredFiring))
                        throw new NotSchedulableException("No solution " +
                                "exists for the balance equations.\n" +
                                "Graph is not" +
                                "consistent under the SDF domain");
                }
                catch (NoSuchElementException e) {
                    throw new InternalErrorException("SDFScheduler: " +
                            "connectedActor " +
                            ((ComponentEntity) connectedActor).getName() +
                            "does not appear in the firings LLMap");
                }

                // Remove them from remainingActors
                remainingActors.removeOneOf(connectedActor);
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
     *  FIXME: This method destroys the firing vector.  This is not nice.
     *
     *  @param UnscheduledActors The Actors that need to be scheduled.
     *  @return A CircularList of the Actors in the order they should fire.
     *  @exception InvalidStateException If the algorithm encounters an SDF
     *  graph that is not consistent with the firing vector, or detects an
     *  inconsistent internal state.
     */
    private CircularList _scheduleConnectedActors(
            CircularList actorList) {

        // A linked list containing all the actors that have no inputs
        CircularList readyToScheduleActorList = new CircularList();
        // A linked list that will contain our new schedule.
        CircularList newSchedule = new CircularList();

        boolean done = false;

        // An association between All the input ports in a simulation and an
	// array of the number of tokens waiting on each relation of that port
	LLMap waitingTokens = new LLMap();

	CircularList unscheduledActorList = new CircularList();
	unscheduledActorList.appendElements(actorList.elements());

        Enumeration schedulableEntities = actorList.elements();

        try {
	    // Fill readyToScheduleActorList with all the actors that have
	    // no unfulfilled input ports, and are thus ready to fire.
	    while(schedulableEntities.hasMoreElements()) {
		Actor a = (Actor)schedulableEntities.nextElement();

		Enumeration ainputports = a.inputPorts();
		while(ainputports.hasMoreElements()) {
		    IOPort ainputport = (IOPort) ainputports.nextElement();
		    // Initialize the waitingTokens at all the
		    // inputports to zero
		    int[] tokencount = new int[ainputport.getWidth()];
		    for(int channel = 0; channel < tokencount.length;
			channel++)
			tokencount[channel] = 0;
		    waitingTokens.putAt(ainputport, tokencount);

		    Enumeration crelations = ainputport.linkedRelations();

		    int channelNumber = 0;
		    // Add the tokens from init production.
		    while(crelations.hasMoreElements()) {
			IORelation crelation =
			    (IORelation) crelations.nextElement();

			Enumeration cports = crelation.linkedSourcePorts();
			IOPort cport = (IOPort) cports.nextElement();

			// Check for the non-deterministic
			// merge when a relation
			// is connected to multiple sources.
			if(cports.hasMoreElements())
			    throw new IllegalActionException(crelation, cport,
				"Relation is " +
				"connected to more than one source port!");

			ComponentEntity cactor
			    = (ComponentEntity) cport.getContainer();
			int rate = _getTokenInitProduction(cport);
			if(rate > 0) {
			    for(int j = 0; j < crelation.getWidth(); j++)
				tokencount[channelNumber++] +=
				    _getTokenProductionRate(cport);
			}
		    }
		}
		int inputCount = _countUnfulfilledInputs(a, actorList,
							 waitingTokens);
		if(inputCount == 0)
		    readyToScheduleActorList.insertFirst((ComponentEntity) a);

		_debug("Actor " + ((ComponentEntity) a).getName() +
		       " has " + (new Integer(inputCount)).toString() +
		       " unfulfilledInputs.");
	    }

	    while(!done) {
		_debug("waitingTokens: ");
		_debug(waitingTokens.toString());

		_debug("Actors that can be scheduled:");
		Enumeration actorsLeft = readyToScheduleActorList.elements();
		while(actorsLeft.hasMoreElements()) {
		    Entity e = (Entity)actorsLeft.nextElement();
		    _debug(e.getFullName());
		}

		// pick an actor that is ready to fire.
		ComponentEntity currentActor
		    = (ComponentEntity) readyToScheduleActorList.at(0);
		// remove it from the list of actors we are waiting to fire
		readyToScheduleActorList.removeAt(0);

		_debug("\nScheduling Actor " + currentActor.getName());
		_simulateInputConsumption(currentActor, waitingTokens);

		// add it to the schedule
		newSchedule.insertLast(currentActor);

		// Get all it's outputPorts.
		Enumeration aOutputPorts =
		    ((Actor) currentActor).outputPorts();

		// And simulate the proper production of tokens.
		while(aOutputPorts.hasMoreElements()) {
		    IOPort aOutputPort = (IOPort) aOutputPorts.nextElement();

		    Integer createdTokens =
			new Integer(_getTokenProductionRate(aOutputPort));

		    _simulateTokensCreated(aOutputPort, actorList,
			readyToScheduleActorList, waitingTokens);
		}

		// Update the firingCount for this actor.
		int firingsRemaining = _getFiringCount(currentActor);
		firingsRemaining -= 1;
		_setFiringCount(currentActor, firingsRemaining);

		_debug(currentActor.getName() + " should fire " +
		       firingsRemaining + " more times.");

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
			unscheduledActorList.removeOneOf(currentActor);
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
			if(inputCount < 1)
			    // if the actor can still be scheduled, then put it
			    // at the END of readyToScheduleActorList.
			    readyToScheduleActorList.insertLast(currentActor);
		    }
		}
	    }
        }

        // This will get thrown by the removeOneOf
        // call if we've run out of things to schedule.
        // FIXME: This should probably throw another exception if
        // unscheduledActors still contains elements.
        catch (NoSuchElementException e) {
            _debug("Caught NSEE:");
            _debug(e.getMessage());
            done = true;
        }
        catch (IllegalActionException iae) {
            // This could happen if we call _getTokenConsumptionRate on a
            // port that isn't a part of the actor.   This probably means
            // the graph is screwed up, or somebody else is mucking
            // with it.
            throw new InternalErrorException("SDF Scheduler Failed " +
                    "Internal consistency check: " + iae.getMessage());
        }
        finally {
            _debug("finishing loop");
        }

        Enumeration eschedule = newSchedule.elements();
        _debug("Schedule is:");
        while(eschedule.hasMoreElements())
            _debug(((ComponentEntity) eschedule.nextElement()).toString());
        return newSchedule;
    }

    /** Push the rates calculated for this system up to the contained Actor.
     *  This allows the container to be properly scheduled if it is
     *  in a hierarchical system
     */
    private void _setContainerRates()
            throws NotSchedulableException {
        Director director = (Director) getContainer();
        if(director == null)
            throw new NotSchedulableException("Scheduler must " +
                    "have a director in order to schedule.");

        CompositeActor container = (CompositeActor) director.getContainer();
        if(container == null) throw new NotSchedulableException(
                "The model must be contained within a CompositeActor in " +
                "order to be scheduled.");

        Enumeration ports = container.getPorts();
        while(ports.hasMoreElements()) {
            IOPort port = (IOPort) ports.nextElement();
            // Extrapolate the Rates
            Enumeration connectedports = port.insidePorts();
            int consumptionRate = 0;
            int productionRate = 0;
            int initProduction = 0;
            if(connectedports.hasMoreElements()) {
                IOPort cport = (IOPort) connectedports.nextElement();
                Entity cactor = (Entity) cport.getContainer();
                consumptionRate = _getFiringCount(cactor) *
                    _getTokenConsumptionRate(cport);
                productionRate = _getFiringCount(cactor) *
                    _getTokenProductionRate(cport);
                initProduction = _getFiringCount(cactor) *
                    _getTokenInitProduction(cport);
                _debug("CPort " + cport.getName());
                _debug("consumptionRate = " + consumptionRate);
                _debug("productionRate = " + productionRate);
                _debug("initProduction = " + initProduction);
            }
            // All the ports connected to this port must have the same rate
            while(connectedports.hasMoreElements()) {
                IOPort cport = (IOPort) connectedports.nextElement();
                Entity cactor = (Entity) cport.getContainer();
                int crate = _getFiringCount(cactor) *
                    _getTokenConsumptionRate(cport);
                if(crate != consumptionRate) throw new NotSchedulableException(
                        port, cport, "Port " + cport.getName() +
                        " has an aggregate consumption rate of " + crate +
                        " which does not match the computed aggregate rate " +
                        "of " + port.getName() + " of " + consumptionRate +
                        "!");
                int prate = _getFiringCount(cactor) *
                    _getTokenProductionRate(cport);
                if(prate != productionRate) throw new NotSchedulableException(
                        port, cport, "Port " + cport.getName() +
                        " has an aggregate production rate of " + prate +
                        " which does not match the computed aggregate rate " +
                        "of " + port.getName() + " of " + productionRate +
                        "!");
                int initp = _getFiringCount(cactor) *
                    _getTokenInitProduction(cport);
                if(initp != initProduction) throw new NotSchedulableException(
                        port, cport, "Port " + cport.getName() +
                        " has an aggregate init production of " + initp +
                        " which does not match the computed aggregate " +
                        "of " + port.getName() + " of " + initProduction +
                        "!");

            }
            _debug("Port " + port.getName());
            _debug("consumptionRate = " + consumptionRate);
            _debug("productionRate = " + productionRate);
            _debug("initProduction = " + initProduction);
            // SDFAtomicActor blindly creates parameters with bad values.

            /*
              if((consumptionRate == 0) && port.isInput()) {
              throw new NotSchedulableException(port, "Port " +
              port.getName() + " declares that it consumes tokens," +
              " but has a consumption rate of 0");
              }

              if((consumptionRate != 0) && !port.isInput()) {
              throw new NotSchedulableException(port, "Port " +
              port.getName() + " has a nonzero consumption rate, " +
              "but does not declare that it is an input port.");
              }

              if(_getTokenConsumptionRate(port) != consumptionRate) {
              throw new NotSchedulableException(port, "Port " +
              port.getName() + " has a declared consumption rate " +
              "of " + _getTokenConsumptionRate(port) + " that " +
              "does not match the rate extrapolated from the " +
              "contained model of " + consumptionRate + ".");
              }
              if((productionRate == 0) && port.isOutput()) {
              throw new NotSchedulableException(port, "Port " +
              port.getName() + " declares that it produces tokens," +
              " but has a production rate of 0");
              }
              if((productionRate != 0) && !port.isOutput()) {
              throw new NotSchedulableException(port, "Port " +
              port.getName() + " has a nonzero production rate, " +
              "but does not declare that it is an output port.");
              }
              if(_getTokenProductionRate(port) != productionRate) {
              throw new NotSchedulableException(port, "Port " +
              port.getName() + " has a declared production rate " +
              "of " + _getTokenProductionRate(port) + " that " +
              "does not match the rate extrapolated from the " +
              "contained model of " + productionRate + ".");
              }
              if((initProduction != 0) && !port.isOutput()) {
              throw new NotSchedulableException(port, "Port " +
              port.getName() + " has a nonzero init production, " +
              "but does not declare that it is an output port.");
              }
              if(_getTokenInitProduction(port) != initProduction) {
              throw new NotSchedulableException(port, "Port " +
              port.getName() + " has a declared init production " +
              "of " + _getTokenInitProduction(port) + " that " +
              "does not match the extrapolated value from the " +
              "contained model of " + initProduction + ".");
              }
              _setTokenConsumptionRate(container, port, consumptionRate);
              _setTokenProductionRate(container, port, productionRate);
              _setTokenInitProduction(container, port, initProduction);
            */
            try {
                Parameter param;
                param = (Parameter)port.getAttribute("TokenConsumptionRate");
                if(param == null)
                    param = new Parameter(port,"TokenConsumptionRate",
                            new IntToken(1));
                param.setToken(new IntToken(consumptionRate));
                param = (Parameter)port.getAttribute("TokenProductionRate");
                if(param == null)
                    param = new Parameter(port,"TokenProductionRate",
                            new IntToken(1));
                param.setToken(new IntToken(productionRate));
                param = (Parameter)port.getAttribute("TokenInitProduction");
                if(param == null)
                    param = new Parameter(port,"TokenInitProduction",
                            new IntToken(1));
                param.setToken(new IntToken(initProduction));
            }
            catch (Exception ex) {
            }
        }
    }

    /** Set the number of firings associated with the Actor.   This is
     *  equivalent to changing the entry in the FiringVector associated with
     *  with the entity to have a value count.
     */
    private void _setFiringCount(Entity entity, int count) {
        _firingvector = (LLMap)
            _firingvector.puttingAt(entity, new Integer(count));
    }

    /** Set the firing vector, which is a LLMap associating an Actor
     *  with the number of times that it will fire during an SDF iteration.
     *  Every object that this Scheduler is responsible for should have an
     *  entry, even if it is zero indicating that the Actor has not yet had
     *  its firings determined.
     *
     *  @param newfiringvector A LLMap from ComponentEntity to Integer.
     */
    private void _setFiringVector(LLMap newfiringvector) {
        _firingvector = newfiringvector;
        _firingvectorvalid = true;
    }

    /** Simulate the consumption of tokens by the actor during an execution.
     *  The entries in LLMap will be modified to reflect the number of
     *  tokens still waiting after the actor has consumed tokens for a firing.
     *  Also determine if enough tokens still remain at the inputs of the actor
     *  for it to fire again immediately.
     *
     *  @param currentActor The actor that is being simulated.
     *  @param waitingTokens A Map between each input IOPort and the number of
     *  tokens in the queue for that port.
     *  @return boolean Whether or not the actor can fire again right away
     *  after it has consumed tokens.
     *  @exception IllegalActionException if any called method throws it.
     */
    private boolean _simulateInputConsumption(ComponentEntity currentActor,
            LLMap waitingTokens)
            throws IllegalActionException {

        boolean stillReadyToSchedule = true;
        // update tokensWaiting on the actor's input ports.

        Enumeration inputPorts = ((Actor) currentActor).inputPorts();
        while(inputPorts.hasMoreElements()) {
            IOPort inputPort = (IOPort) inputPorts.nextElement();
            int[] tokens =
		(int []) waitingTokens.at(inputPort);
	    int tokenrate =
		_getTokenConsumptionRate(inputPort);
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
     * it's actor fires.  If any actors that receive tokens are then ready to
     * fire, given that only actors in the actor list are eing scheduled, then
     * add those actors to the list of actors that are ready to schedule.
     * update the waiting tokens map with the tokens available on each
     * channel of each port.
     */
    private void _simulateTokensCreated(IOPort outputPort,
					CircularList actorList,
					CircularList readyToScheduleActorList,
					LLMap waitingTokens)
	throws IllegalActionException {
	int createdTokens =
	    _getTokenProductionRate(outputPort);
	_debug("Creating " + createdTokens + " on " +
	       outputPort.getFullName());

	// find all the relations that this one is connected to.
	Enumeration crelations =
	    outputPort.linkedRelations();

	while(crelations.hasMoreElements()) {
	    IORelation connectedRelation =
		(IORelation) crelations.nextElement();

	    Enumeration cports = connectedRelation.linkedPorts();
	    while(cports.hasMoreElements()) {
		IOPort connectedPort =
		    (IOPort) cports.nextElement();
		ComponentEntity connectedActor =
		    (ComponentEntity) connectedPort.getContainer();
		// if this is not the source port
		if(!connectedPort.equals(outputPort)) {
		    // Update the number of tokens waiting at the port.
		    int[] tokens = (int[]) waitingTokens.at(connectedPort);
		    int startChannel = _startChannel(connectedPort,
							connectedRelation);
		    for(int i = 0; i < connectedRelation.getWidth(); i++) {
			tokens[startChannel++] += createdTokens;
		    }

		    int ival =
			_countUnfulfilledInputs((Actor)connectedActor,
						actorList,
						waitingTokens);
		    int firingsRemaining = _getFiringCount(connectedActor);
		    if((ival <= 0) && (firingsRemaining > 0)) {
			readyToScheduleActorList.insertLast(connectedActor);
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
     *  @return A LLMap that associates each actor with its fractional
     *  firing.
     *  @exception NotSchedulableException If the graph is not consistent
     *  under the synchronous dataflow model.
     *  @exception NotSchedulableException If the graph is not connected.
     */
    private LLMap _solveBalanceEquations(Enumeration Actors)
            throws NotSchedulableException {

        // firings contains the LLMap that we will return.
        // It gets populated with the fraction firing ratios for
        // each actor
        LLMap firings = new LLMap();

        // remainingActors contains the pool of Actors that have not been
        // touched yet. (i.e. all their firings are still set to Fraction.ZERO)
        CircularList remainingActors = new CircularList();

        // pendingActors have their firings set, but have not had their
        // ports explored yet.
        CircularList pendingActors = new CircularList();

        // Are we done?  (Is pendingActors Empty?)
        boolean done = false;

        // Initialize remainingActors to contain all the actors we were given
        remainingActors.appendElements(Actors);

        // Initialize firings for everybody to Zero
        Enumeration enumActors = remainingActors.elements();
        while(enumActors.hasMoreElements()) {
            ComponentEntity e = (ComponentEntity) enumActors.nextElement();
            firings.putAt(e, Fraction.ZERO);
        }

        try {
            // Pick an actor as a reference
            Actor a = (Actor) remainingActors.take();
            // And set it's rate to one per iteration
            firings.putAt(a, new Fraction(1));
            // And start the list to recurse over.
            pendingActors.insertLast(a);
        }
        catch (NoSuchElementException e) {
            // if remainingActors.take() fails, then we've been given
            // no actors to do anything with, so return an empty LLMap
            return firings;
        }

        while(!done) try {
            _debug("pendingActors: ");
            _debug(pendingActors.toString());
            // Get the next actor to recurse over
            Actor currentActor = (Actor) pendingActors.take();
            _debug("Balancing from " +
                    ((ComponentEntity) currentActor).getName());

            // traverse all the input and output ports, setting the firings
            // for the actor(s)???? that each port is connected to relative
            // to currentActor.
            Enumeration AllPorts =
                ((ComponentEntity) currentActor).getPorts();
            while(AllPorts.hasMoreElements()) {
                IOPort currentPort = (IOPort) AllPorts.nextElement();

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

            // If there are any Actors left that we didn't get to, then
            // this is not a connected graph, and we throw an exception.
            if(remainingActors.elements().hasMoreElements()) {
                String msg = "SDF scheduler found disconnected actors: ";
                Enumeration actors = remainingActors.elements();
                while (actors.hasMoreElements()) {
                    NamedObj actor = (NamedObj)(actors.nextElement());
                    msg += actor.getFullName() + " ";
                }
                throw new NotSchedulableException(msg);
            }
        }
        return firings;
    }

    /** Find the first channel number of the given port that is connected
     * by the given relation.
     */
    private int _startChannel(IOPort port, IORelation relation) {
	Enumeration connectedRelations = port.linkedRelations();
	int channel = 0;
	boolean done = false;
	while(connectedRelations.hasMoreElements() && (!done)) {
	    IORelation connectedRelation = (IORelation)
		connectedRelations.nextElement();
	    if(connectedRelation.equals(relation))
		done = true;
	    else
		channel += connectedRelation.getWidth();
	}
	return channel;
    }

    private LLMap _firingvector;
    private boolean _firingvectorvalid;

}
