/* Base class for directors that have fixed point semantics at each iteration.

 Copyright (c) 2006-2007 The Regents of the University of California.
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
package ptolemy.actor.sched;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FixedPointDirector

/**
 A base class for directors that have fixed point semantics at each
 iteration. An iteration consists of repeated firings of the
 actors controlled by this director until a fixed point is reached.
 An iteration has converged if firing actors will not change signal
 status any more.
 <p>
 At the beginning of each iteration, the status of
 all inputs and outputs is unknown. Upon firing an actor,
 the status of its output signals may become known.  Once the status of
 a signal becomes known, it cannot be changed back to unknown in the
 iteration. This monotonicity constraint ensures the existence and
 uniqueness of the fixed point.
 During an iteration, the prefire() and fire() methods of the controlled
 actors may be repeatedly invoked, but the postfire() method will be
 invoked exactly once after the fixed point has been found.
 The postfire() methods of the contained actors are invoked only
 in the postfire() method of this director, and they are invoked
 in arbitrary order.
 </p><p>
 If the prefire() method of an actor returns false, then this director
 assumes that all the outputs of the actor are absent. The actor has
 declined to fire.
 </p><p>
 Although this director does not require any specific ordering of actor
 firings, a scheduler is used to choose an efficient ordering.
 <p>
 By default, actors are <i>strict</i>, which means that all their
 input signals must be known before the actor can be fired. Here,
 what we mean by "fired" is that prefire() is invoked, and if it
 returns true, then fire() is invoked. Such actors
 will be fired only once in an iteration. A non-strict actor can
 be fired regardless of the status of its inputs, and may be fired
 repeatedly in an iteration if some of the inputs are unknown.
 Once an actor is fired with all its inputs known, it will not
 be fired again in the same iteration.
 A composite actor containing this director is a non-strict actor.
 <p>
 For an actor to be used under the control of this director, it must
 either be strict, or if it is non-strict, it must be monotonic.
 Montonicity implies two constraints on the actor. First, if prefire()
 ever returns true during an iteration, then it will return true
 on all subsequent invocations in the same iteration().
 Second, if either prefire() or fire() call clear() on an output port,
 then no subsequent invocation in the same iteration can call
 put() on the port. If prefire() or fire() call put() on an
 output port with some token, then no subsequent invocation in
 the same iteration can call clear() or put() with a token with
 a different value.
 These constraints ensure determinacy.
 </p><p>
 If <i>synchronizeToRealTime</i> is set to <code>true</code>,
 then the postfire() method stalls until the real time elapsed
 since the model started matches the current time.
 This ensures that the director does not get ahead of real time. However,
 of course, this does not ensure that the director keeps up with real time.
 Note that this synchronization occurs <i>after</i> actors have been fired,
 but before they have been postfired.
 <p>
 This class is based on the original SRDirector, written by Paul Whitaker.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Yellow (eal)
 */
public class FixedPointDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FixedPointDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FixedPointDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
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
    public FixedPointDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of times that postfire may be called before it
     *  returns false. The type must be int, and the value
     *  defaults to zero. If the value is less than or equal to zero,
     *  then the execution will never return false in postfire, and
     *  thus the execution can continue forever.
     */
    public Parameter iterations;

    /** Specify whether the execution should synchronize to the
     *  real time. This parameter has type boolean and defaults
     *  to false. If set to true, then this director stalls in the
     *  prefire() method until the elapsed real real time matches
     *  the current time. If the <i>period</i> parameter has value
     *  0.0 (the default), then changing this parameter to true
     *  has no effect.
     */
    public Parameter synchronizeToRealTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Prefire and fire actors in the order given by the scheduler
     *  until the iteration converges.
     *  An iteration converges when a pass through the schedule does
     *  not change the status of any receiver.
     *  @exception IllegalActionException If an actor violates the
     *   monotonicity constraints, or the prefire() or fire() method
     *   of the actor throws it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("FixedPointDirector: invoking fire().");
        }
        Schedule schedule = getScheduler().getSchedule();
        int iterationCount = 0;
        do {
            Iterator firingIterator = schedule.firingIterator();
            while (firingIterator.hasNext() && !_stopRequested) {
                Actor actor = ((Firing) firingIterator.next()).getActor();
                // If the actor has previously returned false in postfire(),
                // do not fire it.
                if (!_actorsFinishedExecution.contains(actor)) {
                    // check if the actor is ready to fire.
                    if (_isReadyToFire(actor)) {
                        _fireActor(actor);
                        _actorsFired.add(actor);
                    }
                } else {
                    // The postfire() method of this actor returned false in
                    // some previous iteration, so here, for the benefit of
                    // connected actors, we need to explicitly call the
                    // sendClear() method of all of its output ports,
                    // which indicates that a signal is known to be absent.
                    if (_debugging) {
                        _debug("FixedPointDirector: no longer enabled (return false in postfire): "
                                + actor.getFullName());
                    }
                    _sendClearToAllUnknownOutputsOf(actor);
                }
            }
            iterationCount++;
        } while (!_hasIterationConverged() && !_stopRequested);

        if (_debugging) {
            _debug(this.getFullName() + ": Fixed point found after "
                    + iterationCount + " iterations.");
        }
    }

    /** Return the current index of the director.
     *  The current index is a portion of the superdense time.
     *  Superdense time means that time is a real value and an index,
     *  allowing multiple sequential steps to occur at a fixed (real) time.
     *  @return Index of the director.
     */
    public int getIndex() {
        return _index;
    }

    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method. Reset all private variables.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        _currentIteration = 0;
        // This variable has to be reset at the very beginning, because
        // some actors may call fireAt method to register breakpoints in DE
        // and Continuous domains, which depend on the value of _index.
        _index = 0;

        _actorsAllowedToFire = new HashSet();
        _actorsFinishedFiring = new HashSet();
        _cachedAllInputsKnown = new HashSet();

        _cachedFunctionalProperty = true;
        _functionalPropertyVersion = -1;

        super.initialize();

        _realStartTime = System.currentTimeMillis();
    }

    /** Return true if all the controlled actors' isFireFunctional()
     *  methods return true. Otherwise, return false.
     *
     *  @return True if all controlled actors are functional.  Return
     *  false if there is no container or no actors in the container.
     */
    public boolean isFireFunctional() {
        if (workspace().getVersion() == _functionalPropertyVersion) {
            return _cachedFunctionalProperty;
        }

        boolean result = true;
        boolean containsActors = false;

        CompositeActor container = (CompositeActor) getContainer();
        if (container == null) {
            return false;
        }
        Iterator actors = container.deepEntityList().iterator();

        while (result && actors.hasNext() && !_stopRequested) {
            Actor actor = (Actor) actors.next();
            result = actor.isFireFunctional() && result;
            containsActors = true;
        }

        if (!containsActors) {
            result = false;
        } 
        _cachedFunctionalProperty = result;
        _functionalPropertyVersion = workspace().getVersion();

        return result;
    }

    /** Return false. The transferInputs() method checks whether
     *  the inputs are known before calling hasToken().
     *  Thus this derictor tolerate unknown inputs.
     *
     *  @return False.
     */
    public boolean isStrict() {
        return false;
    }

    /** Return a new FixedPointReceiver. If a subclass overrides this
     *  method, the receiver it creates must be a subclass of FixedPointReceiver,
     *  and it must add the receiver to the _receivers list (a protected
     *  member of this class).
     *  @return A new FixedPointReceiver.
     */
    public Receiver newReceiver() {
        Receiver receiver = new FixedPointReceiver(this);
        _receivers.add(receiver);
        return receiver;
    }

    /** Call postfire() on all contained actors that were fired on the last
     *  invocation of fire().  If <i>synchronizeToRealTime</i> is true, then
     *  wait for real time elapse to match or exceed model time. Return false if the model
     *  has finished executing, either by reaching the iteration limit, or if
     *  no actors in the model return true in postfire(), or if stop has
     *  been requested. This method is called only once for each iteration.
     *  Note that actors are postfired in arbitrary order.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token, or if there still some unknown inputs (which
     *   indicates a causality loop).
     */
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("FixedPointDirector: Called postfire().");
        }
        _synchronizeToRealTime();

        boolean needMoreIterations = true;
        int numberOfActors = getScheduler().getSchedule().size();
        if ((numberOfActors > 0) && (_actorsFired.size() == 0)) {
            needMoreIterations = false;
        }

        Iterator actors = _actorsFired.iterator();
        while (actors.hasNext() && !_stopRequested) {
            Actor actor = (Actor) actors.next();
            if (!_areAllInputsKnown(actor)) {
                throw new IllegalActionException(actor,
                        "Unknown inputs remain. Possible causality loop.");
            }
            if (!_actorsFinishedExecution.contains(actor)) {
                if (!_postfireActor(actor)) {
                    // postfire() returned false, so prevent the actor
                    // from iterating again.
                    _actorsFinishedExecution.add(actor);
                }
            }
        }
        if (_debugging) {
            _debug(this.getFullName() + "Iteration " + _currentIteration
                    + " is complete.");
        }

        // Check whether the current execution has reached its iteration limit.
        _currentIteration++;
        int numberOfIterations = ((IntToken) iterations.getToken()).intValue();
        if ((numberOfIterations > 0)
                && (_currentIteration >= numberOfIterations)) {
            super.postfire();
            return false;
        }

        return super.postfire() && needMoreIterations;
    }

    /** Initialize the firing of the director by resetting state variables
     *  and reset all receivers to unknown.
     *  @return True always.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("FixedPointDirector: Called prefire().");
        }
        _actorsAllowedToFire.clear();
        _actorsFinishedFiring.clear();
        _actorsFired.clear();
        _cachedAllInputsKnown.clear();
        _lastNumberOfKnownReceivers = -1;

        _resetAllReceivers();

        // Set current time based on the enclosing model.
        boolean result = super.prefire();

        return result;
    }

    /** Return an array of suggested directors to be used with
     *  ModalModel. Each director is specified by its full class
     *  name.  The first director in the array will be the default
     *  director used by a modal model.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    public String[] suggestedModalModelDirectors() {
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[1] = "ptolemy.domains.fsm.kernel.NonStrictFSMDirector";
        defaultSuggestions[0] = "ptolemy.domains.fsm.kernel.FSMDirector";
        return defaultSuggestions;
    }

    /** Transfer data from the specified input port of the
     *  container to the ports it is connected to on the inside.
     *  If there is no data on the specified input port, then
     *  set the ports on the inside to absent by calling sendClearInside().
     *  This method delegates the data transfer
     *  operation to the transferInputs method of the super class.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one token is transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        boolean result = false;
        for (int i = 0; i < port.getWidth(); i++) {
            if (port.isKnown(i)) {
                if (port.hasToken(i)) {
                    result = super.transferInputs(port) || result;
                } else {
                    port.sendClearInside(i);
                }
            }
            // we do not explicit reset the receivers receiving inputs
            // from this port because the fixedpoint director resets the
            // receivers in its prefire() method.
        }
        return result;
    }

    /** Transfer data from the specified output port of the
     *  container to the ports it is connected to on the outside.
     *  If there is no data on the specified output port, then
     *  set the ports on the outside to absent by calling sendClear().
     *  This method delegates the data transfer
     *  operation to the transferOutputs method of the super class.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one token is transferred.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        boolean result = false;
        for (int i = 0; i < port.getWidthInside(); i++) {
            if (port.isKnownInside(i)) {
                if (port.hasTokenInside(i)) {
                    result = super.transferOutputs(port) || result;
                } else {
                    port.sendClear(i);
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** React to the change in receiver status by incrementing the count of
     *  known receivers.
     */
    protected void _receiverChanged() {
        _currentNumberOfKnownReceivers++;
    }

    /** Reset all receivers to unknown status.
     */
    protected void _resetAllReceivers() {
        if (_debugging) {
            _debug("    FixedPointDirector is resetting all receivers");
        }
        _currentNumberOfKnownReceivers = 0;

        Iterator receiverIterator = _receivers.iterator();
        while (receiverIterator.hasNext()) {
            ((FixedPointReceiver) receiverIterator.next()).reset();
        }
    }

    /** Synchronize to real time, if appropriate.
     *  @exception IllegalActionException If the <i>synchronizeToRealTime</i>
     *   parameter is ill formed.
     */
    protected void _synchronizeToRealTime() throws IllegalActionException {
        boolean synchronizeValue = ((BooleanToken) synchronizeToRealTime
                .getToken()).booleanValue();

        if (synchronizeValue) {
            synchronized (this) {
                while (true) {
                    long elapsedTime = System.currentTimeMillis()
                            - _realStartTime;

                    // NOTE: We assume that the elapsed time can be
                    // safely cast to a double.  This means that
                    // the SR domain has an upper limit on running
                    // time of Double.MAX_VALUE milliseconds.
                    double elapsedTimeInSeconds = elapsedTime / 1000.0;
                    double currentTime = getModelTime().getDoubleValue();

                    if (currentTime <= elapsedTimeInSeconds) {
                        break;
                    }

                    long timeToWait = (long) ((currentTime - elapsedTimeInSeconds) * 1000.0);

                    if (_debugging) {
                        _debug("Waiting for real time to pass: " + timeToWait);
                    }

                    try {
                        // NOTE: The built-in Java wait() method
                        // does not release the
                        // locks on the workspace, which would block
                        // UI interactions and may cause deadlocks.
                        // SOLUTION: workspace.wait(object, long).
                        if (timeToWait > 0) {
                            // Bug fix from J. S. Senecal:
                            //
                            //  The problem was that sometimes, the
                            //  method Object.wait(timeout) was called
                            //  with timeout = 0. According to java
                            //  documentation:
                            //
                            // " If timeout is zero, however, then
                            // real time is not taken into
                            // consideration and the thread simply
                            // waits until notified."
                            _workspace.wait(this, timeToWait);
                        }
                    } catch (InterruptedException ex) {
                        // Continue executing.
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The current index of the model. */
    protected int _index;

    /** List of all receivers this director has created. */
    protected List _receivers = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if all the inputs of the specified actor are known.
     */
    private boolean _areAllInputsKnown(Actor actor)
            throws IllegalActionException {

        if (_cachedAllInputsKnown.contains(actor)) {
            return true;
        }

        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();

            if (!inputPort.isKnown()) {
                return false;
            }
        }

        _cachedAllInputsKnown.add(actor);
        return true;
    }

    /** Fire an actor. Call its prefire() method, and
     *  if that returns true, call its fire() method.
     *  @exception IllegalActionException If the prefire() method
     *   returns false having previously returned true in the same
     *   iteration, or if the prefire() or fire() method of the actor
     *   throws it.
     */
    private void _fireActor(Actor actor) throws IllegalActionException {
        // Prefire the actor.
        boolean prefireReturns = actor.prefire();
        if (_debugging) {
            _debug("FixedPointDirector: Prefiring: "
                    + ((Nameable) actor).getFullName() + ", which returns "
                    + prefireReturns);
        }
        // Check monotonicity constraint.
        if (!prefireReturns && _actorsAllowedToFire.contains(actor)) {
            throw new IllegalActionException(
                    actor,
                    "prefire() method returns false, but it"
                            + " has previously returned true in this iteration.");
        }
        if (prefireReturns) {
            _actorsAllowedToFire.add(actor);

            // Whether all inputs are known must be checked before
            // firing to handle cases with self-loops, because the
            // current firing may change the status of some input
            // receivers from unknown to known.
            boolean allInputsKnownBeforeFiring = _areAllInputsKnown(actor);

            if (_debugging) {
                if (allInputsKnownBeforeFiring) {
                    _debug("Firing: " + ((Nameable) actor).getName()
                            + ", which has all inputs known.");
                } else {
                    _debug("Firing: " + ((Nameable) actor).getName()
                            + ", which has some inputs unknown.");
                }
            }

            actor.fire();
            // If all of the inputs of this actor were known before firing, firing
            // the actor again in the current iteration is not necessary.
            if (allInputsKnownBeforeFiring) {
                _actorsFinishedFiring.add(actor);
                _sendClearToAllUnknownOutputsOf(actor);
            }
        } else {
            // If prefire() returned false, the actor declines
            // to fire, meaning all its outputs are absent.
            // Note that prefire() is executed only after all the inputs are
            // known or the actor is non-strict.
            _actorsFinishedFiring.add(actor);
            _sendClearToAllUnknownOutputsOf(actor);
        }
    }

    /** Return true if this iteration has converged.  The iteration has
     *  converged if both the number of known receivers
     *  has not changed since the previous invocation of this method.
     */
    private boolean _hasIterationConverged() {
        if (_debugging) {
            _debug(this.getFullName()
                    + ":\n Number of receivers known previously is "
                    + _lastNumberOfKnownReceivers
                    + ":\n Number of receivers known now is "
                    + _currentNumberOfKnownReceivers);
        }
        // Determine the number of known receivers has changed since the
        // last iteration. If not, the current iteration has converged.
        // Note that checking whether all receivers are known is not sufficient
        // to conclude the convergence of the iteration because if some
        // receivers just become known, their containers (actors) need to be
        // fired to react these new inputs.
        boolean converged = _lastNumberOfKnownReceivers == _currentNumberOfKnownReceivers;
        _lastNumberOfKnownReceivers = _currentNumberOfKnownReceivers;

        // One might try to optimize this method by also considering the
        // _actorsFinishedFiring set.
        // CompositeActor container = (CompositeActor) getContainer();
        // converged =
        // _actorsFinishedFiring.size() == container.deepEntityList().size());
        return converged;
    }

    /** Initialize the director by creating the parameters and setting their
     *  values and types.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        iterations = new Parameter(this, "iterations", new IntToken(0));
        iterations.setTypeEquals(BaseType.INT);

        synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime");
        synchronizeToRealTime.setExpression("false");
        synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

        timeResolution.setVisibility(Settable.FULL);
        timeResolution.moveToLast();

        FixedPointScheduler scheduler = new FixedPointScheduler(this,
                uniqueName("Scheduler"));
        setScheduler(scheduler);
    }

    /** Return true if the specified actor is ready to fire.  An actor is
     *  ready to fire if it has not previously finished firing in this iteration
     *  and either it is strict and all inputs are known or it is nonstrict.
     *  Note that this ignores whether the actor has previously returned
     *  false in postfire().
     */
    private boolean _isReadyToFire(Actor actor) throws IllegalActionException {
        return !_actorsFinishedFiring.contains(actor)
                && (!actor.isStrict() || _areAllInputsKnown(actor));
    }

    /** Return the result of the postfire() method of the specified actor
     *  if it is allowed to be fired in the current iteration.  If this actor
     *  is not to be fired in the current iteration, return true without
     *  calling the postfire() method of the actor.
     */
    private boolean _postfireActor(Actor actor) throws IllegalActionException {
        if (_actorsAllowedToFire.contains(actor)) {
            _debug(getFullName() + " is postfiring "
                    + ((Nameable) actor).getFullName());
            return actor.postfire();
        }
        return true;
    }

    /** Call the sendClear() method of each output port with
     *  unknown status of the specified actor
     *  @param actor The actor.
     */
    private void _sendClearToAllUnknownOutputsOf(Actor actor)
            throws IllegalActionException {
        // An actor, if its firing has finished but some of its
        // outputs are still unknown, clear these outputs.
        // However, there is nothing need to do if this actor has
        // resolved all of its outputs.
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            for (int j = 0; j < outputPort.getWidth(); j++) {
                if (!outputPort.isKnown(j)) {
                    if (_debugging) {
                        _debug("  FixedPointDirector: Set output "
                                + outputPort.getFullName() + " to absent.");
                    }
                    outputPort.sendClear(j);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The set of actors that have returned true in their prefire() methods
     *  in the current iteration. This is used only to check monotonicity
     *  constraints.
     */
    private Set _actorsAllowedToFire;

    /** The set of actors that have been fired in this iteration with
     *  all inputs known.
     */
    private Set _actorsFinishedFiring;

    /** Actors that were fired in the most recent invocation of the fire() method. */
    private Set _actorsFired = new HashSet();

    /** The set of actors that have all inputs known in the given iteration. */
    private Set _cachedAllInputsKnown;

    /** The cache of the functional property of the container of this director. */
    private boolean _cachedFunctionalProperty;

    /** The current number of receivers with known state. */
    private int _currentNumberOfKnownReceivers;

    /** The count of iterations executed. */
    private int _currentIteration;

    /** Version number for the cached functional property. */
    private transient long _functionalPropertyVersion = -1L;

    /** The number of receivers with known state on the last phase of
     *  actor firings.
     */
    private int _lastNumberOfKnownReceivers;

    /** The real time at which the model begins executing. */
    private long _realStartTime = 0L;
}
