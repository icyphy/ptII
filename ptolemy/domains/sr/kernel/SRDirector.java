/* Director for the Synchronous-Reactive model of computation.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sr.lib.NonStrictActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SRDirector
/**
A director for the Synchronous-Reactive (SR) model of computation.  In SR,
both computation and communication are considered to happen instantaneously.
In models with cycles, this introduces interesting issues involving 
instantaneous feedback.
<p>
Time in SR is defined as a series of instants.  An instant is one iteration 
of the director.  The <i>period</i> method of the director determines the 
amount of time between instants, in which no activity occurs.
<p>
In SR, each iteration begins with the values on all channels being unknown.  
To ensure that an iteration converges to final values in finite time, it is 
required that values change only from unknown to known, and never the other 
way around.  Once a value is known (or known to be absent), it must not 
change.
<p>
An actor is considered <i>ready to fire</i> if sufficient known inputs are 
available.  Unless an actor implements the NonStrictActor interface, it is
assumed to be a strict actor, meaning that it requires all of its inputs
to be known before it is fired.  This is very important since once an actor
defines a particular output, it is not allowed to change that value in a 
subsequent firing in the course of the iteration.  An actor <i>has completed 
firing</i> if it has defined all of its outputs.
<p>
An actor is considered <i>allowed to fire</i> if its prefire()
method has returned true.  An actor is considered <i>allowed to iterate</i>
if its postfire() method has not returned false.
<p>
The SRScheduler returns an ordering of the actors.  SR semantics do not
require any particular ordering of actor firings, so the ordering exists only
to attempt to reduce the computation time required for a given iteration to
converge.  In the course of an iteration, the director cycles through the 
schedule repeatedly, firing those actors that are allowed to fire and ready 
to fire.
<p>
An iteration <i>has converged</i> if both the total number of receivers and
the number of actors that are allowed to fire have converged.

@author Paul Whitaker
@version $Id$
*/
public class SRDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public SRDirector() {
        super();
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     */
    public SRDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public SRDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of times that postfire may be called before it
     *  returns false. The type must be IntToken, and the value 
     *  defaults to zero. If the value is less than or equal to zero,
     *  then the execution will never return false in postfire, and 
     *  thus the execution can continue forever.
     */
    public Parameter iterations;

    /** The period of an iteration. The type must be less than or equal to
     *  DoubleToken, and it defaults to 1.
     */
    public Parameter period;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the iterations member.  The new
     *  actor will have the same parameter values as the old.
     *  FIXME: Are the parameters cloned automatically?
     *  FIXME: Are the type constraints cloned automatically?
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SRDirector newObject = (SRDirector)(super.clone(workspace));
        newObject.iterations = (Parameter)newObject.getAttribute("iterations");
        //newObject.iterations.setTypeEquals(BaseType.INT);
        newObject.period = (Parameter)newObject.getAttribute("period");
        //newObject.period.setTypeEquals(BaseType.DOUBLE);
        return newObject;
    }

    /** Iterate every contained actor with calls to prefire(), fire(), and 
     *  postfire().
     *  @exception IllegalActionException If an actor executed by this
     *  director returns false in its prefire().
     */
    public void fire() throws IllegalActionException {

        Schedule schedule = _getSchedule();
        Iterator firingIterator;

        _initFiring();

        // Here we need to check for each actor that iteration is allowed
        // (in other words, that the actor has not returned false in 
        //  postfire()).
        
        // _fireActor and _postfireActor are responsible for checking that
        // firing is allowed (in other words, the actor will not call fire()
        // or postfire() on an actor until it returns true in prefire()).

        // _fireActor is also responsible for checking that the actor is
        // ready to fire (sufficient known inputs are available).

        do {
            firingIterator = schedule.firingIterator();

            while (firingIterator.hasNext()) {
                Firing firing = (Firing) firingIterator.next();
                Actor actor = firing.getActor();
                if (_isIterationAllowed(actor)) {
                    _fireActor(actor);
                } else {
                    // The postfire() method of this actor returned false in 
                    // a previous iteration, so here, for the benefit of 
                    // connected actors, we need to explicitly call the 
                    // sendAbsent() method of all of its output ports.
                    _sendAbsentToAllUnknownOutputsOf(actor);
                }

            }
        } while (!_hasIterationConverged());

        firingIterator = schedule.firingIterator();
        while (firingIterator.hasNext()) {
            Firing firing = (Firing) firingIterator.next();
            Actor actor = firing.getActor();
            if (_isIterationAllowed(actor)) {
                if (_postfireActor(actor)) {
                    _postfireReturns = true;
                } else {
                    _doNotAllowIterationOf(actor);
                }
            }
        }

    }

    /** Return the number of iterations.
     *  @return The number of iterations.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public int getIterations() throws IllegalActionException {
        return ((IntToken) iterations.getToken()).intValue();
    }

    /** Return the period.
     *  @return The period.
     *  @exception IllegalActionException If the period parameter does
     *   not have a valid token.
     */
    public double getPeriod() throws IllegalActionException {
        return ((DoubleToken) period.getToken()).doubleValue();
    }

    /** Initialize the director by initializing state variables and invoke
     *  the initialize() methods of all actors deeply contained by the
     *  container.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iteration = 0;
        _actorsNotAllowedToIterate = null;
        // Force the schedule to be computed.
        _getSchedule();
    }

    /** Return a new receiver consistent with the SR domain.
     *  @return A new SRReceiver.
     */
    public Receiver newReceiver() {
        if (_receivers == null) _receivers = new LinkedList();
        Receiver receiver = new SRReceiver();
        _receivers.add(receiver);
        return receiver;
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the model
     *  return false in postfire.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {

        _advanceCurrentTime(getPeriod());

        int numberOfIterations = getIterations();
        _iteration++;

        _debug("SRDirector: Time instant",
                String.valueOf(_iteration-1), 
                "is complete.");

        if((numberOfIterations > 0) && (_iteration >= numberOfIterations)) {
            _iteration = 0;
            return false;
        }

        return _postfireReturns;
    }

    /** Enable all actors and invoke the preinitialize() methods of
     *  all actors deeply contained by the container.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void preinitialize() throws IllegalActionException {

        _receivers = new LinkedList();

        // Call the parent preinitialize method to create the receivers.
        super.preinitialize();
    }

    //FIXMESOON: transferInputs
    //FIXMESOON: transferOutputs

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The static default SR period is 1 second.
     */
    protected static int _DEFAULT_SR_PERIOD = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Advance the current time by the specified amount.
     */
    private void _advanceCurrentTime(double amount) {
        _currentTime = getCurrentTime() + amount;
    }

    /** Return true if all the inputs of the specified actor are known.
     */
    private boolean _areAllInputsKnown(Actor actor)
            throws IllegalActionException {

        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort)inputPorts.next();
            if (!inputPort.isKnown()) return false;
        }

        return true;
    }

    /** Do allow the specified actor to fire.  Typically called when
     *  the prefire method of the actor returns true.
     *  will be ignored. If the argument is null, then do nothing.
     */
    private void _doAllowFiringOf(Actor actor) {
        if (actor != null) {
            if (_actorsAllowedToFire == null) {
                _actorsAllowedToFire = new HashSet();
            }
            _actorsAllowedToFire.add(actor);
        }
    }

    /** Disable the specified actor.  All events destined to this actor
     *  will be ignored. If the argument is null, then do nothing.
     */
    private void _doNotAllowIterationOf(Actor actor) {
        if (actor != null) {
            if (_actorsNotAllowedToIterate == null) {
                _actorsNotAllowedToIterate = new HashSet();
            }
            _debug("  Added to _actorsNotAllowedToIterate:",
                    _getNameOf(actor));
            _actorsNotAllowedToIterate.add(actor);
        }
    }

    /** Fire the specified actor if the prefire() method of this actor has
     *  returned true in the current iteration.  If the prefire() method 
     *  of this actor has not returned true in the current iteration, 
     *  the prefire() method will be called first.
     */
    private void _fireActor(Actor actor) throws IllegalActionException {

        if (_isReadyToFire(actor)) {
            if (_isFiringAllowed(actor)) {
                if (!_hasCompletedFiring(actor)) {
                    _debug("    SRDirector is firing", _getNameOf(actor));
                    // Whether all inputs are known must be checked before
                    // firing to handle cases with self-loops.
                    boolean allInputsKnownBeforeFiring =
                        _areAllInputsKnown(actor);
                    actor.fire();

                    // If all of the inputs of this actor are known, firing
                    // the actor again in this iteration is not necessary.
                    // The actor will not produce any more outputs because
                    // it will have no new inputs to react to.  Thus, we
                    // can assume that any unknown outputs of this actor
                    // are actually absent.
                    if (allInputsKnownBeforeFiring) 
                        _sendAbsentToAllUnknownOutputsOf(actor);
                }
            } else {
                _debug("    SRDirector is prefiring", _getNameOf(actor));
                if (actor.prefire()) {
                    _doAllowFiringOf(actor);
                    _fireActor(actor);
                }
            }
        }
    }

    /** Return the name of the specified actor.
     */
    private String _getNameOf(Actor actor) {
        return ((Nameable)actor).getName();
    }

    /** Return the schedule associated with this director.
     *  @exception IllegalActionException If this director has no
     *   associated scheduler.
     */
    private Schedule _getSchedule() throws IllegalActionException {
        Scheduler scheduler = getScheduler();
        if (scheduler == null)
            throw new IllegalActionException(this,
                    "SRDirector has no associated scheduler.");
        return scheduler.getSchedule();
    }

    /** Return true if the specified actor has completed firing.  An actor 
     *  has completed firing if it has defined all of its outputs.
     */
    private boolean _hasCompletedFiring(Actor actor)
            throws IllegalActionException {

        // Non strict actors should fire every phase in case more inputs
        // become available (the inputs might be, for example, cached and
        // used in a subsequent iteration.
        if (actor instanceof NonStrictActor) return false;

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort)outputPorts.next();
            if (!outputPort.isKnown()) return false;
        }

        // FIXMESOON:* it might be possible to improve efficiency here
        // by caching the fact that this actor is done firing. 
        return true;
    }

    /** Return true if this iteration has converged.  The iteration has
     *  converged if both the number of known receivers has converged and
     *  the number of actors to fire has converged.
     */
    private boolean _hasIterationConverged() {

        // Get the previous values for local use.
        int previousNumOfActorsAllowedToFire = _lastNumOfActorsAllowedToFire;
        int previousNumOfKnownReceivers = _lastNumOfKnownReceivers;

        // Get the current values for local use.
        int currentNumOfActorsAllowedToFire = _numOfActorsAllowedToFire();
        int currentNumOfKnownReceivers = _numOfKnownReceivers();

        // Update the previous values for use the next time this method
        // is called.
        _lastNumOfActorsAllowedToFire = currentNumOfActorsAllowedToFire;
        _lastNumOfKnownReceivers = currentNumOfKnownReceivers;

        _debug("  previousNumOfActorsAllowedToFire is",
                String.valueOf(previousNumOfActorsAllowedToFire));
        _debug("  currentNumOfActorsAllowedToFire is",
                String.valueOf(currentNumOfActorsAllowedToFire));

        _debug("  previousNumOfKnownReceivers is",
                String.valueOf(previousNumOfKnownReceivers));
        _debug("  currentNumOfKnownReceivers is",
                String.valueOf(currentNumOfKnownReceivers));

        _debug("  total number of receivers is",
                String.valueOf(_receivers.size()));

        // Note that having zero actors to fire is not sufficient for 
        // convergence.  Some actors may fire in the next phase if
        // the director calls sendAbsent() on any output ports.

        // Note that all receivers having known state is not sufficient
        // for convergence.  After the values are present, each actor must
        // have the opportunity to fire before the end of the iteration.

        // This is the first phase of actor firings.
        if (previousNumOfKnownReceivers == -1) return false;

        // The number of actors to fire has not converged, so the
        // iteration has not converged.
        if (previousNumOfActorsAllowedToFire != 
                currentNumOfActorsAllowedToFire)
            return false;

        // The number of known receivers has not converged, so the
        // iteration has not converged.
        if (previousNumOfKnownReceivers != currentNumOfKnownReceivers)
            return false;

        // The number of actors to fire and the number of known receivers
        // have both converged, so the iteration has converged.
        return true;
    }

    /** Initialize the director by creating the scheduler and the parameters.
     */
    private void _init() {
        try {
            SRScheduler scheduler = 
                new SRScheduler(this, uniqueName("Scheduler"));
            setScheduler(scheduler);
        }
        catch (Exception ex) {
            // if setScheduler fails, then we should just set it to null.
            // This should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Cannot create SRScheduler:\n" + ex.getMessage());
        }

	try {
	    period = new Parameter(this, "period",
                    new IntToken(_DEFAULT_SR_PERIOD));
            period.setTypeEquals(BaseType.DOUBLE);
	    iterations = new Parameter(this, "iterations", new IntToken(0));
            iterations.setTypeEquals(BaseType.INT);
	    setCurrentTime(0.0);
        } catch (KernelException ex) {
	    throw new InternalErrorException(
                    "Cannot initialize SRDirector: " + ex.getMessage());
        }
    }

    /** Initialize the firing of the director by resetting state variables
     *  and resetting all receivers to have unknown status.
     */
    private void _initFiring() {
        _postfireReturns = false;
        _actorsAllowedToFire = null;

        _lastNumOfActorsAllowedToFire = -1;
        _lastNumOfKnownReceivers = -1;

        _resetAllReceivers();
    }

    /** Return true if the specified actor is allowed to fire, that is,
     *  the prefire method of the actor has returned true.
     */
    private boolean _isFiringAllowed(Actor actor) {
        return (!(_actorsAllowedToFire == null) && 
                _actorsAllowedToFire.contains(actor));
    }

    /** Return true if the specified actor is allowed to iterate.
     */
    private boolean _isIterationAllowed(Actor actor) {
        return (_actorsNotAllowedToIterate == null || 
                !_actorsNotAllowedToIterate.contains(actor));
    }

    /** Return true if the specified actor is ready to fire.  An actor is
     *  ready to fire if sufficient known inputs are available.
     */
    private boolean _isReadyToFire(Actor actor) throws IllegalActionException {

        // Non strict actors are allowed to fire even if no inputs are known.
        if (actor instanceof NonStrictActor) {
            return true;
        }

        return _areAllInputsKnown(actor);
    }

    /** Return the number of actors that are allowed to fire.
     */
    private int _numOfActorsAllowedToFire() {
        if (_actorsAllowedToFire == null) {
           return 0;
        }

        return _actorsAllowedToFire.size();
    }

    /** Return the number of receivers with known state.
     */
    private int _numOfKnownReceivers() {
        if (_receivers == null) {
           return 0;
        }

        int count = 0;
        Iterator i = _receivers.iterator();

        while (i.hasNext()) {
            if (((SRReceiver) i.next()).isKnown()) count++;
        }

        return count;
    }

    /** Return the result of the postfire() method of the specified actor 
     *  if it has been fired in the current iteration.  If this actor has 
     *  not been fired in the current iteration, return true without
     *  calling the postfire() method of the actor.
     */
    private boolean _postfireActor(Actor actor)
            throws IllegalActionException {

        if (_isFiringAllowed(actor)) {
            _debug("    SRDirector is postfiring", _getNameOf(actor));
            return actor.postfire();
        }

        return true;
    }

    /** Reset all receivers to allow a new firing of the director.
     */
    private void _resetAllReceivers() {
        Iterator i = _receivers.iterator();
        while (i.hasNext()) {
            ((SRReceiver) i.next()).reset();
        }
    }

    /** Call the sendAbsent() method of each of the output ports of the
     *  specified actor.
     */
    private void _sendAbsentToAllUnknownOutputsOf(Actor actor)
            throws IllegalActionException {

        // Nonstrict actors may intend to output undefined values.
        if (!(actor instanceof NonStrictActor)) {
            // No need to do anything if this actor has defined all of its 
            // outputs.
            if (!_hasCompletedFiring(actor)) {
                _debug("  SRDirector is calling sendAbsent()",
                        "on the output ports of", _getNameOf(actor));

                Iterator outputPorts = actor.outputPortList().iterator();

                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort)outputPorts.next();
                    for (int j = 0; j < outputPort.getWidth(); j++) {
                        if (!outputPort.isKnown(j)) outputPort.sendAbsent(j);
                    }
                }
            }

            // FIXMESOON:* it might be possible to improve efficiency here
            // by caching the fact that this actor is done firing. 
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The set of actors that have returned false in their postfire() methods.
    // Events destined for these actors are discarded and the actors are
    // never fired.
    private HashSet _actorsNotAllowedToIterate;

    // The set of actors that have returned true in their prefire() methods
    // on the given iteration.
    private HashSet _actorsAllowedToFire;

    // The count of iterations executed.
    private int _iteration;

    // The logical or of the values returned by actors' postfire().
    private boolean _postfireReturns;

    // The number of actors that were fired on the last phase of
    // actor firings.
    private int _lastNumOfActorsAllowedToFire;

    // The number of receivers with known state on the last phase of
    // actor firings.
    private int _lastNumOfKnownReceivers;

    // List of all receivers this director has created.
    private List _receivers;

}



