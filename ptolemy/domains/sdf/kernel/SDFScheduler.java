/* A Scheduler for the SDF domain

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
//import ptolemy.actor.NotSchedulableException;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import ptolemy.math.Fraction;

import java.util.*;
import collections.CircularList;
import collections.HashedMap;
import collections.HashedSet;

//////////////////////////////////////////////////////////////////////////
//// SDFScheduler
/**
A scheduler than implements scheduling of SDF networks by solving the
balance equations for SDFActors.

FIXME: This class uses CircularList in the collections package. Change it
to Java collection when update to JDK1.2
@see ptolemy.actor.Scheduler
@author Stephen Neuendorffer
@version $Id$
*/
public class SDFScheduler extends Scheduler{
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Basic Scheduler".
     * @see ptolemy.kernel.util.NamedObj
     * @return The scheduler
     */
    public SDFScheduler() {
        super();
        _localMemberInitialize();
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Basic Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this scheduler.
     */
    public SDFScheduler(Workspace ws) {
        super(ws);
        _localMemberInitialize();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the firing vector, which is a HashedMap associating an Actor
     *  with the number of times that it will fire during an SDF iteration.
     *  Some entries may be zero, if the Actor has not yet been scheduled.
     *
     *  @return A HashedMap from ComponentEntity to Integer.
     */
    public HashedMap getFiringVector() {
        return _firingvector;
    }

    /** Set the firing vector, which is a HashedMap associating an Actor
     *  with the number of times that it will fire during an SDF iteration.
     *  Every object that this Scheduler is responsible for should have an
     *  entry, even if it is zero indicating that the Actor has not yet had
     *  its firings determined.
     *
     *  @param A HashedMap from ComponentEntity to Integer.
     */
    public void setFiringVector(HashedMap newfiringvector) {
        _firingvector = newfiringvector;
        _firingvectorvalid = true;
    }

    /** Return the number of firings associated with the Actor.   This is
     *  equivalent to indexing into the Map returned by getFiringVector and
     *  casting the result to an integer.
     */
    public int getFiringCount(ComponentEntity entity) {
            Debug.println(_firingvector.toString());
            return ((Integer) _firingvector.at(entity)).intValue();
    }

    /** Set the number of firings associated with the Actor.   This is
     *  equivalent to changing the entry in the FiringVector associated with
     *  with the entity to have a value count.
     */
    public void setFiringCount(ComponentEntity entity, int count) {
        _firingvector = (HashedMap)
            _firingvector.puttingAt(entity, new Integer(count));
    }


    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /** Count the number of inputports in the Actor that must be
     *  fulfilled before the actor can fire.   Ports
     *  that are connected to actors that we are not scheduling right now are
     *  assumed to be fulfilled.   Ports that have more tokens waiting than
     *  their input consumption rate are also already fulfilled.
     *
     *  @param a The actor
     *  @param unscheduledactors The set of actors that we are scheduling.
     *  @param waitingTokens The Map of tokens currently waiting on all the
     *  input ports.
     *  @return The number of unfulfilled inputs of a.
     *  @exception IllegalActionException if any called method throws it.
     */
    protected int _countUnfulfilledInputs(Actor a,
            HashedSet unscheduledactors, HashedMap waitingTokens)
            throws IllegalActionException {
               Enumeration ainputPorts = a.inputPorts();

            int InputCount = 0;
            while(ainputPorts.hasMoreElements()) {
                IOPort ainputPort = (IOPort) ainputPorts.nextElement();
                Enumeration cports = ainputPort.deepConnectedOutPorts();

                boolean isonlyexternalport = true;
                while(cports.hasMoreElements()) {
                    IOPort cport = (IOPort) cports.nextElement();
                    if(unscheduledactors.includes(cport.getContainer()))
                        isonlyexternalport = false;
                }

                boolean isalreadyfulfilled = false;
                int threshold
                    = _getTokenConsumptionRate(ainputPort);
                int tokens
                    = ((Integer) waitingTokens.at(ainputPort)).intValue();
                if(tokens >= threshold)
                    isalreadyfulfilled = true;

                if(!isonlyexternalport && !isalreadyfulfilled)
                    InputCount++;
            }
            return InputCount;
    }

    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor, as supplied by
     *  by the port's "Token Consumption Rate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogenous and return a
     *  rate of 1.
     */
    protected int _getTokenConsumptionRate(IOPort p) {
        Parameter param = (Parameter)p.getAttribute("Token Consumption Rate");
        if(param == null) return 1;
        return ((IntToken)param.getToken()).intValue();
    }

    /**
     * Get the number of tokens that are produced on this output port
     * during initialization, as supplied by
     * by the port's "Token Init Production" parameter.   If the parameter
     * does not exist, then assume the actor is zero-delay and return
     * a value of zero.
     */
    protected int _getTokenInitProduction(IOPort p) {
        Parameter param = (Parameter)p.getAttribute("Token Init Production");
        if(param == null) return 0;
        return ((IntToken)param.getToken()).intValue();
    }

    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor during each firing,
     *  as supplied by
     *  by the port's "Token Consumption Rate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogenous and return a
     *  rate of 1..
     */
    protected int _getTokenProductionRate(IOPort p) {
        Parameter param = (Parameter)p.getAttribute("Token Production Rate");
        if(param == null) return 1;
        return ((IntToken)param.getToken()).intValue();
    }

    /** Normalize fractional firing ratios into a firing vector that
     *  corresponds to a single SDF iteration.   Multiplies all of the
     *  fractions by the GCD of their denominators.
     *
     *  @param Firings HashedMap of firing ratios to be normalized
     *  @return The normalized firing vector.
     *  @exception ArithmeticException If the calculated GCD does not
     *  normalize all of the fractions.
     */
    protected HashedMap _normalizeFirings(HashedMap Firings) {
        Enumeration unnormalizedFirings = Firings.elements();
        int lcm = 1;

        Debug.println("Normalizing Firings");
        // First find the lcm of all the denominators
        while(unnormalizedFirings.hasMoreElements()) {
            Fraction f = (Fraction) unnormalizedFirings.nextElement();
            int den = f.getDenominator();
            lcm = Fraction.lcm(lcm, den);
        }

        Debug.println("lcm = " + (new Integer(lcm)).toString());
        Enumeration Actors = Firings.keys();

        Fraction lcmFraction = new Fraction(lcm);
        // now go back through and multiply by the lcm we just found, which
        // should normalize all the fractions to integers.
        while(Actors.hasMoreElements()) {

            Object actor = Actors.nextElement();
            Debug.println("normalizing Actor " + 
                    ((ComponentEntity) actor).getName());
            Fraction reps = (Fraction) Firings.at(actor);
            reps = Fraction.multiply(reps,lcmFraction);
            reps.simplify();
            if(reps.getDenominator() != 1)
                throw new ArithmeticException("Failed to properly perform " +
                        "fraction normalization");
            Firings = (HashedMap) 
                Firings.puttingAt(actor,new Integer(reps.getNumerator()));
        }
        return Firings;
    }

    /** Initialize the local data members of this object.
     */
    private void _localMemberInitialize() {
        HashedMap _firingvector = new HashedMap();
        _firingvectorvalid = true;
    }

    /** Propagate the number of fractional firing decided for this actor
     *  through the specified input port.   Set and verify the fractional
     *  firing for each Actor that is connected through this input port.
     *  Any actors that we calculate their firing vector for the first time
     *  are moved from RemainingActors to PendingActors.
     *
     *  @param currentPort The port that we are propagating from.
     *  @param Firings The current HashedMap of fractional firings for each
     *  Actor
     *  @param RemainingActors The set of actors that have not had their
     *  fractional firing set.
     *  @param PendingActors The set of actors that have had their rate
     *  set, but have not been propagated onwards.
     */
    protected void _propagateInputPort(IOPort currentPort,
            HashedMap Firings,
            HashedSet RemainingActors,
            HashedSet PendingActors)
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

                    Debug.println("Propagating input to " +
                            connectedActor.getName());

                    int connectedRate =
                        _getTokenProductionRate(connectedPort);

                    // currentFiring is the firing that we've already
                    // calculated for currentactor
                    Fraction currentFiring =
                        (Fraction) Firings.at(currentActor);

                    // the firing that we think the connected actor should be,
                    // based on currentActor
                    Fraction desiredFiring =
                        Fraction.multiply(currentFiring,
                                new Fraction(currentRate,connectedRate));

                    // What the firing for connectedActor already is set to.
                    // This should be either 0, or equal to desiredFiring.
                    try {
                        Fraction presentFiring =
                            (Fraction) Firings.at(connectedActor);

                        desiredFiring.simplify();

                        if(Fraction.equals(presentFiring,Fraction.ZERO)) {
                            // create the entry in the firing table
                            Firings.putAt(connectedActor,desiredFiring);

                            // Remove them from RemainingActors
                            RemainingActors.exclude(connectedActor);

                            // and add them to the PendingActors.
                            PendingActors.include(connectedActor);
                        }

                        else if(!Fraction.equals(presentFiring,desiredFiring))
                            throw new NotSchedulableException("Graph is not" +
                                    "consistent under the SDF domain");
                    }
                    catch (NoSuchElementException e) {
                        throw new InvalidStateException("SDFScheduler: " +
                                "connectedActor " +
                                ((ComponentEntity) connectedActor).getName() +
                                "does not appear in the Firings hashedmap");
                    }

                    Debug.print("New Firing: ");
                    Debug.println(Firings.toString());
                }
            }
    }

    /** Propagate the number of fractional firing decided for this actor
     *  through the specified output port.   Set or verify the fractional
     *  firing for each Actor that is connected through this output port.
     *  Any actors that we calculate their firing vector for the first time
     *  are moved from RemainingActors to PendingActors.
     *
     *  @param currentPort The port that we are propagating from.
     *  @param Firings The current HashedMap of fractional firings for each
     *  Actor
     *  @param RemainingActors The set of actors that have not had their
     *  fractional firing set.
     *  @param PendingActors The set of actors that have had their rate
     *  set, but have not been propagated onwards.
     */
    protected void _propagateOutputPort(IOPort currentPort,
            HashedMap Firings,
            HashedSet RemainingActors,
            HashedSet PendingActors)
        throws NotSchedulableException {

            ComponentEntity currentActor =
                (ComponentEntity) currentPort.getContainer();

            //Calculate over all the output ports of this actor.
            int currentRate = _getTokenProductionRate(currentPort);

            if(currentRate>0) {
                // Compute the rate for the Actor currentPort is connected to
                Enumeration connectedPorts =
                    currentPort.deepConnectedInPorts();

                while(connectedPorts.hasMoreElements()) {
                    IOPort connectedPort =
                        (IOPort) connectedPorts.nextElement();

                    ComponentEntity connectedActor =
                        (ComponentEntity) connectedPort.getContainer();

                    Debug.println("Propagating output to " +
                            connectedActor.getName());
                    if(connectedActor instanceof DataflowActor)
                        Debug.println("Succeed!");
                            else Debug.println("fail");

                    Debug.assert(connectedActor instanceof DataflowActor);
                    int connectedRate =
                        _getTokenConsumptionRate(connectedPort);

                    // currentFiring is the firing that we've already
                    // calculated for currentactor
                    Fraction currentFiring =
                        (Fraction) Firings.at(currentActor);
                    // the firing that we think the connected actor should be,
                    // based on currentActor
                    Fraction desiredFiring =
                        Fraction.multiply(currentFiring,
                                new Fraction(currentRate,connectedRate));

                    // What the firing for connectedActor already is set to.
                    // This should be either 0, or equal to desiredFiring.
                    try {
                        Fraction presentFiring =
                            (Fraction) Firings.at(connectedActor);

                        desiredFiring.simplify();

                        if(Fraction.equals(presentFiring,Fraction.ZERO)) {
                            Firings.putAt(connectedActor,desiredFiring);

                            // Remove them from RemainingActors
                            RemainingActors.exclude(connectedActor);

                            // and add them to the PendingActors.
                            PendingActors.include(connectedActor);
                        }
                        else if(!Fraction.equals(presentFiring,desiredFiring))
                            throw new NotSchedulableException("Graph is not" +
                                    "consistent under the SDF domain");
                    }
                    catch (NoSuchElementException e) {
                        throw new InvalidStateException("SDFScheduler: " +
                                "connectedActor " +
                                ((ComponentEntity) connectedActor).getName() +
                                "does not appear in the Firings hashedmap");
                    }

                   // Remove them from RemainingActors
                    RemainingActors.exclude(connectedActor);
                }
            }
    }

    /** Return the scheduling sequence. In this base class, it returns
     *  the containees of the CompositeActor in the order of construction.
     *  (Same as calling deepCetEntities()). The derived classes will
     *  override this method and add their scheduling algorithms here.
     *  This method should not be called directly, rather the schedule()
     *  will call it when the schedule is not valid. So it is not
     *  synchronized on the workspace.
     *
     * @see ptolemy.kernel.CompositeEntity#deepGetEntities()
     * @return An Enumeration of the deeply contained opaque entities
     *  in the firing order.
     * @exception NotSchedulableException If the CompositeActor is not
     *  schedulable. Not thrown in this base class, but may be needed
     *  by the derived scheduler.
     */

     protected Enumeration _schedule() throws NotSchedulableException {
        StaticSchedulingDirector dir =
            (StaticSchedulingDirector)getContainer();
        CompositeActor ca = (CompositeActor)(dir.getContainer());

        // A linked list containing all the actors
        HashedSet AllActors = new HashedSet();
        Enumeration Entities = ca.deepGetEntities();


        while(Entities.hasMoreElements()) {
            ComponentEntity a = (ComponentEntity)Entities.nextElement();

            // Fill AllActors with the list of things that we can schedule
            // CHECKME: What if other things can be scheduled than actors?
            if(a instanceof Actor) AllActors.include(a);
        }

        // First solve the balance equations
        HashedMap Firings = _solveBalanceEquations(AllActors.elements());
        Firings = _normalizeFirings(Firings);

        setFiringVector(Firings);

        Debug.print("Firing Vector:");
        Debug.println(Firings.toString());

        // Schedule all the actors using the calculated firings.
        CircularList result = _scheduleConnectedActors(AllActors);

        setFiringVector(Firings);

        Debug.print("Firing Vector:");
        Debug.println(Firings.toString());

        setValid(true);

        return result.elements();
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
     *  graph that is not consistant with the firing vector, or detects an
     *  inconsistant internal state.
     */

    protected CircularList _scheduleConnectedActors(
            HashedSet UnscheduledActors) {

        // A linked list containing all the actors that have no inputs
        CircularList ReadyToScheduleActors = new CircularList();
        // A linked list that will contain our new schedule.
        CircularList newSchedule = new CircularList();

        boolean Done = false;

        // an association between AllActors and the number of unfulfilledInputs
        // that that actor contains.   when this number goes to zero, then the
        // actor is ready to fire.
        HashedMap unfulfilledInputs = new HashedMap();

        // an association between All the input ports in a simulation and the
        // number of tokens waiting on that port
        // Fixme: What if there are delays and we start with some tokens?
        HashedMap waitingTokens = new HashedMap();

        Enumeration SchedulableEntities = UnscheduledActors.elements();

        try {
        // Fill ReadyToScheduleActors with all the actors that have
        // no unfulfilled input ports, and are thus ready to fire.
        while(SchedulableEntities.hasMoreElements()) {
            Actor a = (Actor)SchedulableEntities.nextElement();

            Enumeration ainputports = a.inputPorts();
            while(ainputports.hasMoreElements()) {
                IOPort ainputport = (IOPort) ainputports.nextElement();
                // Initialize the waitingTokens at all the inputports to zero
                waitingTokens.putAt(ainputport,new Integer(0));

                Enumeration cports = ainputport.deepConnectedOutPorts();

                while(cports.hasMoreElements()) {
                    IOPort cport = (IOPort) cports.nextElement();
                    ComponentEntity cactor
                        = (ComponentEntity) cport.getContainer();
                    int rate = _getTokenInitProduction(cport);
                    if(rate > 0) {
                        int tokens = ((Integer) waitingTokens.at(ainputport))
                            .intValue();
                        waitingTokens.putAt(ainputport,
                            new Integer(tokens +rate));
                    }
                }
            }


            int inputcount = _countUnfulfilledInputs(a, UnscheduledActors,
                waitingTokens);
            if(inputcount == 0)
                ReadyToScheduleActors.insertFirst((ComponentEntity) a);
            // map a->InputCount
            unfulfilledInputs.putAt(a,new Integer(inputcount));

            Debug.print("Actor ");
            Debug.print(((ComponentEntity) a).getName());
            Debug.print(" has ");
            Debug.print((new Integer(inputcount)).toString());
            Debug.println(" unfulfilledInputs.");
        }

        while(!Done) {
                Debug.print("waitingTokens: ");
                Debug.println(waitingTokens.toString());

                // pick an actor that is ready to fire.
                ComponentEntity currentActor
                    = (ComponentEntity) ReadyToScheduleActors.at(0);

                // remove it from the list of actors we are waiting to fire
                ReadyToScheduleActors.removeAt(0);

                Debug.println("Scheduling Actor "+currentActor.getName());


                boolean stillReadyToSchedule =
                    _simulateInputConsumption(currentActor,waitingTokens);

                // Reset the number of unfulfilled inputs.
                int inputcount = _countUnfulfilledInputs((Actor)currentActor,
                    UnscheduledActors, waitingTokens);
                unfulfilledInputs.putAt(currentActor, new Integer(inputcount));

                // Update the firingCount for this actor.
                int firingsRemaining = getFiringCount(currentActor);
                firingsRemaining -= 1;
                setFiringCount(currentActor,firingsRemaining);

                Debug.print(currentActor.getName() + " should fire ");
                Debug.print((new Integer(firingsRemaining)).toString());
                Debug.println(" more times");

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
                else if(firingsRemaining == 0)
                    UnscheduledActors.removeOneOf(currentActor);
                // Otherwise the actor still has firings left
                else
                    // We've already removed currentActor from ReadytoSchedule
                    // so if it can be fired again right away, put it back on
                    // the list.
                    if(stillReadyToSchedule)
                        // if the actor can still be scheduled, then put it
                        //at the END of ReadytoScheduleActors.
                        ReadyToScheduleActors.insertLast(currentActor);

                // add it to the schedule
                newSchedule.insertLast(currentActor);

                // Get all it's outputPorts.
                Enumeration aOutputPorts =
                    ((Actor) currentActor).outputPorts();
                while(aOutputPorts.hasMoreElements()) {
                    IOPort aOutputPort = (IOPort) aOutputPorts.nextElement();

                    Integer createdTokens = new Integer(
                        _getTokenProductionRate(aOutputPort));

                    // find all the ports that this one is connected to.
                    Enumeration connectedPorts =
                        aOutputPort.deepConnectedInPorts();

                    // loop through each of the connected Actors, and
                    // increment their waitingTokens.
                    // FIXME: This doesn't work if an actors input
                    // comes from two different ports, but I can't think
                    // of how this really makes sense in SDF so I'm not
                    // going to worry about it now.
                    // Maybe this should be an IllegalStateException?
                    while(connectedPorts.hasMoreElements()) {
                        IOPort connectedPort =
                            (IOPort) connectedPorts.nextElement();
                        ComponentEntity connectedActor =
                            (ComponentEntity) connectedPort.getContainer();

                        // if this is something that we are supposed to
                        // schedule  (i.e. not the composite actor)
                        if(UnscheduledActors.includes(connectedActor)) {
                            // FIXME: So, if a single output port is connected
                            // to multiple actors, we make multiple copies of
                            // the token.   Is this right, or should this also
                            // be an exception?

                            // Update the number of tokens waiting at the port.
                            Integer curTokenAmt =
                                (Integer) waitingTokens.at(connectedPort);
                            Integer newTokenAmt =
                                new Integer(curTokenAmt.intValue() +
                                        createdTokens.intValue());
                            waitingTokens.putAt(connectedPort,newTokenAmt);

                            // update unfulfilledInputs
                            int inputThreshold =
                                _getTokenConsumptionRate(connectedPort);
                            if((curTokenAmt.intValue()<inputThreshold) &&
                                    ((newTokenAmt.intValue()>=inputThreshold)))
                                {
                                Integer i =
                                    (Integer) unfulfilledInputs.at(
                                            connectedActor);
                                int ival = i.intValue() - 1;
                                unfulfilledInputs.putAt(connectedActor,
                                        new Integer(ival));
                                Debug.print("Actor ");
                                Debug.print(connectedActor.getName());
                                Debug.print(" now has ");
                                Debug.print((new Integer(ival)).toString());
                                Debug.println(" unfulfilledInputs.");
                                // If this Actor has no more
                                // UnfulfilledInputs, then
                                // add it to the list of actors
                                // ready to be scheduled.
                                if(ival <= 0) {
                                    ReadyToScheduleActors.
                                        insertLast(connectedActor);
                                }
                            }
                        }
                    }
                }
            }
        }

        // This will get thrown by the removeFirst
        // call if we've run out of things to schedule.
        // FIXME: This should probably throw another exception if
        // unscheduledActors still contains elements.
        catch (NoSuchElementException e) {
            Debug.print("Caught NSEE:");
            Debug.println(e.getMessage());
            Done = true;
        }
        catch (IllegalActionException iae) {
            // This could happen if we call _getTokenConsumptionRate on a

            // port that isn't a part of the actor.   This probably means
            // the graph is screwed up, or somebody else is mucking
            // with it.
            throw new InvalidStateException("SDF Scheduler Failed " +
                    "Internal consistancy check: " + iae.getMessage());
        }
        finally {
            Debug.println("finishing loop");
        }
        
        Enumeration eschedule = newSchedule.elements();
        Debug.println("Schedule is:");
        while(eschedule.hasMoreElements())
            Debug.println(
                    ((ComponentEntity) eschedule.nextElement()).toString());
        return newSchedule;
    }
    
    /** Simulate the consumption of tokens by the actor during an execution.
     *  The entries in HashedMap will be modified to reflect the number of
     *  tokens still waiting after the actor has consumed tokens for a firing.
     *  Also determine if enough tokens still remain at the inputs of the actor
     *  for it to fire again immediately.
     *
     *  @param currentActor The actor that is being simulated
     *  @param waitingTokens A Map between each input IOPort and the number of
     *  tokens in the queue for that port.
     *  @return boolean Whether or not the actor can fire again right away
     *  after it has consumed tokens.
     *  @exception IllegalActionException if any called method throws it.
     */
    protected boolean _simulateInputConsumption(ComponentEntity currentActor,
            HashedMap waitingTokens)
        throws IllegalActionException {

        boolean stillReadyToSchedule = true;
        // update tokensWaiting on the actor's input ports.

        Enumeration inputPorts = ((Actor) currentActor).inputPorts();
        while(inputPorts.hasMoreElements()) {
            IOPort inputPort = (IOPort) inputPorts.nextElement();
            int tokens =
                        ((Integer) waitingTokens.at(inputPort)).intValue();
                    int tokenrate =
                        _getTokenConsumptionRate(inputPort);
                    tokens -= tokenrate;

                    // keep track of whether or not this actor can fire again
                    // immediately
                    if(tokens < tokenrate) stillReadyToSchedule = false;

                    // update the number of pseudo-tokens waiting on each input
                    waitingTokens.putAt(inputPort, new Integer(tokens));
                }
        return stillReadyToSchedule;
     }

    /** Solve the Balance Equations for the list of connected Actors.
     *  For each actor, determine the ratio that determines the rate at
     *  which it should fire relative to the other actors for the graph to
     *  be live and operate within bounded memory.   This ratio is known as the
     *  fractional firing of the actor.
     *
     *  @param Actors The actors that we are interested in
     *  @return A HashedMap that associates each actor with its fractional
     *  firing.
     *  @exception NotScheduleableExecption If the graph is not consistant
     *  under the synchronous dataflow model.
     *  @exception NotScheduleableException If the graph is not connected.
     */
    protected HashedMap _solveBalanceEquations(Enumeration Actors)
            throws NotSchedulableException {

        // Firings contains the HashedMap that we will return.
        // It gets populated with the fraction firing ratios for
        // each actor
        HashedMap Firings = new HashedMap();

        // RemainingActors contains the pool of Actors that have not been
        // touched yet. (i.e. all their firings are still set to Fraction.ZERO)
        HashedSet RemainingActors = new HashedSet();

        // PendingActors have their Firings set, but have not had their
        // ports explored yet.
        HashedSet PendingActors = new HashedSet();

        // Are we done?  (Is PendingActors Empty?)
        boolean Done = false;

        // Initialize RemainingActors to contain all the actors we were given
        RemainingActors.includeElements(Actors);

        // Initialize Firings for everybody to Zero
        Enumeration enumActors = RemainingActors.elements();
        while(enumActors.hasMoreElements()) {
            ComponentEntity e = (ComponentEntity) enumActors.nextElement();
            Firings.putAt(e, Fraction.ZERO);
        }

        try {
            // Pick an actor as a reference
            Actor a = (Actor) RemainingActors.take();
            // And set it's rate to one per iteration
            Firings.putAt(a,new Fraction(1));
            // And start the list to recurse over.
            PendingActors.include(a);
        }
        catch (NoSuchElementException e) {
            // if RemainingActors.take() fails, then we've been given
            // no actors to do anything with, so return an empty HashedMap
            return Firings;
        }

        while(! Done) try {
            Debug.print("PendingActors: ");
            Debug.println(PendingActors.toString());
            // Get the next actor to recurse over
            Actor currentActor = (Actor) PendingActors.take();
            Debug.println("Balancing from " +
                    ((ComponentEntity) currentActor).getName());

            // traverse all the input and output ports, setting the firings
            // for the actor(s)???? that each port is connected to relative
            // to currentActor.
            Enumeration AllPorts =
                ((ComponentEntity) currentActor).getPorts();
            while(AllPorts.hasMoreElements()) {
                IOPort currentPort = (IOPort) AllPorts.nextElement();

                if(currentPort.isInput())
                    _propagateInputPort(currentPort, Firings,
                            RemainingActors, PendingActors);

                if(currentPort.isOutput())
                    _propagateOutputPort(currentPort, Firings,
                            RemainingActors, PendingActors);
            }
        }
        catch (NoSuchElementException e) {
            // Once we've exhausted PendingActors, this exception will be
            // thrown, causing us to terminate the loop.
            Done = true;

            // If there are any Actors left that we didn't get to, then
            // this is not a connected graph, and we throw an exception.
            if(RemainingActors.elements().hasMoreElements())
                throw new NotSchedulableException("The graph cannot " +
                        "be scheduled using the SDF scheduler because " +
                        "it is not connected");
        }
        return Firings;
    }

    protected HashedMap _firingvector;
    protected boolean _firingvectorvalid;

}







