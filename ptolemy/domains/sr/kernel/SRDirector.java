/* Director for the Synchronous Reactive model of computation.

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
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SRDirector
/**
A director for the Synchronous Reactive (SR) model of computation.  In SR,
both computation and communication are considered to happen instantaneously.
In models with cycles, this introduces interesting issues involving 
instantaneous feedback.
<p>
SR is an untimed domain, so it has no notion of the passage of time.  
Computation happens in a series of instants.  An instant is one iteration of 
the director.  If SR is embedded inside a timed domain, the SR director will 
inherit the current time of the outside director.
<p>
In SR, each iteration begins with the values on all channels being unknown.  
To ensure that an iteration converges to final values in finite time, it is 
required that values change only from unknown to known, and never the other 
way around.  Once a value is known (or known to be absent), it must not 
change again in the course of the iteration.
<p>
An actor is considered <i>ready to fire</i> if sufficient known inputs are 
available.  In a sense, an actor firing is triggered by these known inputs, 
because the director only fires an actor if it is ready to fire.  Unless an 
actor contains an attribute called "_nonStrictMarker", it is assumed to be a 
strict actor, meaning that it requires all of its inputs to be known before 
it is fired.  This is very important since once an actor defines a particular 
output, it is not allowed to change that value in a subsequent firing in the 
course of the iteration.  An actor <i>has completed firing</i> if it has 
defined all of its outputs.
<p>
An actor is considered <i>allowed to fire</i> if its prefire()
method has returned true.  An actor is considered <i>allowed to iterate</i>
if its postfire() method has not returned false.
<p>
The SRScheduler returns an ordering of the actors.  SR semantics do not
require any specific ordering of actor firings, so the ordering exists only
to attempt to reduce the computation time required for a given iteration to
converge.  In the course of an iteration, the director cycles through the 
schedule repeatedly, firing those actors that are allowed to fire and ready 
to fire.
<p>
For an actor to be valid in the SR domain, its prefire() method must be 
monotonic.  In other words, once the prefire() method of an actor returns 
true in a given iteration, this method must not return false if it were to be 
called again in the same iteration.  It is only possible for the number of 
known inputs of an actor to increase in a given iteration, so, for example, 
if the prefire() method of an actor returns true and then more inputs become 
known, the method must return true if it were to be called again.  If this
were not the case, the behavior of the model would be nondeterministic.
<p>
An iteration <i>has converged</i> if both the total number of known outputs 
and the number of actors that are allowed to fire have converged.

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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire contained actors until the iteration converges.  This method
     *  also calls the prefire() method of an actor before it is fired for
     *  the first time, and at the end, calls the postfire() methods of all 
     *  actors that were fired.
     *  @exception IllegalActionException If an actor attempts to modify
     *   a known value.
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

    /** Return the number of iterations to be executed by the director, which 
     *  is specified by a parameter of the director.
     *  @return The number of iterations.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public int getIterations() throws IllegalActionException {
        return ((IntToken) iterations.getToken()).intValue();
    }

    /** Increment the count of known receivers.  Called by a receiver when
     *  it changes from unknown to known status.
     */
    public void incrementKnownReceiverCount() {
        _currentNumOfKnownReceivers++;
    }

    /** Initialize the director and invoke the initialize() methods of all 
     *  actors deeply contained by the container.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iteration = 0;
        _actorsNotAllowedToIterate = null;

        _resetAllReceivers();

        // Force the schedule to be computed.
        _getSchedule();
    }

    /** Return a new receiver consistent with the SR domain.
     *  @return A new SRReceiver.
     */
    public Receiver newReceiver() {
        if (_receivers == null) _receivers = new LinkedList();
        Receiver receiver = new SRReceiver(this);
        _receivers.add(receiver);
        return receiver;
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or if no actors in the model
     *  return true in postfire.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {

        int numberOfIterations = getIterations();
        _iteration++;

        _debug("SRDirector: Instant",
                String.valueOf(_iteration-1), 
                "is complete.");

        // All receivers must be reset before any actors are executed in the 
        // next iteration.  By doing this at the end of each iteration, all 
        // receivers are guaranteed to be reset, even in a heterogeneous model.
        _resetAllReceivers();

        if((numberOfIterations > 0) && (_iteration >= numberOfIterations)) {
            _iteration = 0;
            return false;
        }

        return _postfireReturns;
    }

    /** Initialize the internal receiver list and invoke the preinitialize() 
     *  methods of all deeply contained actors.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void preinitialize() throws IllegalActionException {

        _receivers = new LinkedList();

        // Call the parent preinitialize method to create the receivers.
        super.preinitialize();
    }

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

    /** Return true if all the inputs of the specified actor are known.
     */
    private boolean _areAllInputsKnown(Actor actor)
            throws IllegalActionException {

        if (_cachedAllInputsKnown == null)
            _cachedAllInputsKnown = new HashSet();

        if (_cachedAllInputsKnown.contains(actor)) return true;

        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort)inputPorts.next();
            if (!inputPort.isKnown()) return false;
        }

        _cachedAllInputsKnown.add(actor);
        return true;
    }

    /** Return true if all the outputs of the specified actor are known.
     */
    private boolean _areAllOutputsKnown(Actor actor)
            throws IllegalActionException {

        if (_cachedAllOutputsKnown == null)
            _cachedAllOutputsKnown = new HashSet();

        if (_cachedAllOutputsKnown.contains(actor)) return true;

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort)outputPorts.next();
            if (!outputPort.isKnown()) return false;
        }

        _cachedAllOutputsKnown.add(actor);
        return true;
    }

    /** Do allow the specified actor to fire.  Typically called when
     *  the prefire method of the actor returns true.
     */
    private void _doAllowFiringOf(Actor actor) {
        if (actor != null) {
            if (_actorsAllowedToFire == null)
                _actorsAllowedToFire = new HashSet();
            _actorsAllowedToFire.add(actor);
        }
    }

    /** Do not allow the specified actor to iterate.  Typically called when
     *  the postfire method of the actor returns false.
     */
    private void _doNotAllowIterationOf(Actor actor) {
        if (actor != null) {
            if (_actorsNotAllowedToIterate == null)
                _actorsNotAllowedToIterate = new HashSet();
            _debug("  Added to _actorsNotAllowedToIterate:",
                    _getNameOf(actor));
            _actorsNotAllowedToIterate.add(actor);
        }
    }

    /** Fire the specified actor if the actor is ready to fire.  If the 
     *  prefire() method of this actor has not returned true in the current 
     *  iteration, the prefire() method will be called first.
     */
    private void _fireActor(Actor actor) throws IllegalActionException {

        if (_isReadyToFire(actor)) {
            if (_isFiringAllowed(actor)) {
                if (!_isFinishedFiring(actor)) {
                    _debug("    SRDirector is firing", _getNameOf(actor));

                    // Whether all inputs are known must be checked before
                    // firing to handle cases with self-loops.
                    boolean allInputsKnownBeforeFiring =
                        _areAllInputsKnown(actor);
                    actor.fire();

                    if (_actorsFired == null) _actorsFired = new HashSet();
                    _actorsFired.add(actor);

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
        int currentNumOfKnownReceivers = _currentNumOfKnownReceivers;

        if (_debugging) {
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
        }

        // Update the previous values for use the next time this method
        // is called.
        _lastNumOfActorsAllowedToFire = currentNumOfActorsAllowedToFire;
        _lastNumOfKnownReceivers = _currentNumOfKnownReceivers;

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
        _actorsFired = null;

        _cachedAllInputsKnown = null;
        _cachedAllOutputsKnown = null;

        _lastNumOfActorsAllowedToFire = -1;
        _lastNumOfKnownReceivers = -1;

        //_resetAllReceivers();
        //_currentNumOfKnownReceivers = 0;
    }

    /** Return true if the specified actor is finished firing.  An actor 
     *  has finished firing if it is strict and has defined all of its outputs.
     */
    private boolean _isFinishedFiring(Actor actor)
            throws IllegalActionException {

        // Non strict actors should fire every phase in case more inputs
        // become available (the inputs might be, for example, cached and
        // used in a subsequent iteration.
        if (_isNonStrict(actor)) return false;

        // Actors that have not fired in this iteration are not finished.
        if (_actorsFired == null) return false;
        if (!_actorsFired.contains(actor)) return false;

        return _areAllOutputsKnown(actor);
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

    /** Return true if the specified actor is a nonstrict actor.
     */
    private boolean _isNonStrict(Actor actor) {

        // This information is not cached, since there is no semantic reason
        // that the strictness of an actor could not change during execution.
        Attribute nonStrictAttribute = 
            ((NamedObj) actor).getAttribute(NON_STRICT_ATTRIBUTE_NAME);

        return (nonStrictAttribute != null);
    }

    /** Return true if the specified actor is ready to fire.  An actor is
     *  ready to fire if sufficient known inputs are available.
     */
    private boolean _isReadyToFire(Actor actor) throws IllegalActionException {

        // Non strict actors are allowed to fire even if no inputs are known.
        if (_isNonStrict(actor)) return true;

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
        _currentNumOfKnownReceivers = 0;
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
        if (!_isNonStrict(actor)) {
            // No need to do anything if this actor has defined all of its 
            // outputs.
            if (!_isFinishedFiring(actor)) {
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

    // The set of actors that have been fired once or more in the given 
    // iteration.
    private HashSet _actorsFired;

    // The set of actors that have all inputs known in the given iteration.
    private HashSet _cachedAllInputsKnown;

    // The set of actors that have all outputs known in the given iteration.
    private HashSet _cachedAllOutputsKnown;

    // The current number of receivers with known state.
    private int _currentNumOfKnownReceivers;

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

    // The name of an attribute that marks an actor as non strict.
    private static final String NON_STRICT_ATTRIBUTE_NAME = "_nonStrictMarker";
}




