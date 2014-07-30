/* Base class for directors that have fixed point semantics at each iteration.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
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
public class FixedPointDirector extends StaticSchedulingDirector implements
SuperdenseTimeDirector {

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
     *  has no effect. Note that in this base class, there is
     *  no <i>period</i> parameter and time is never advanced,
     *  so this will have no effect. It has effect in derived
     *  classes.
     */
    public Parameter synchronizeToRealTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new director.
     *  @param workspace The workspace for the new director.
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FixedPointDirector newObject = (FixedPointDirector) super
                .clone(workspace);
        newObject._receivers = new LinkedList();

        newObject._actorsAllowedToFire = new HashSet();
        newObject._actorsFinishedFiring = new HashSet();
        newObject._actorsFired = new HashSet();
        newObject._cachedAllInputsKnown = new HashSet();
        return newObject;
    }

    /** Prefire and fire actors in the order given by the scheduler
     *  until the iteration converges.
     *  An iteration converges when a pass through the schedule does
     *  not change the status of any receiver.
     *  @exception IllegalActionException If an actor violates the
     *   monotonicity constraints, or the prefire() or fire() method
     *   of the actor throws it.
     */
    @Override
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
                    } else {
                        if (_debugging) {
                            if (!_actorsFinishedFiring.contains(actor)
                                    && actor.isStrict()) {
                                _debug("Strict actor has uknown inputs: "
                                        + actor.getFullName());
                            }
                        }
                    }
                } else {
                    // The postfire() method of this actor returned false in
                    // some previous iteration, so here, for the benefit of
                    // connected actors, we need to explicitly call the
                    // send(index, null) method of all of its output ports,
                    // which indicates that a signal is known to be absent.
                    if (_debugging) {
                        _debug("FixedPointDirector: no longer enabled (return false in postfire): "
                                + actor.getFullName());
                    }
                    _sendAbsentToAllUnknownOutputsOf(actor);
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
     *  @return the superdense time index
     *  @see #setIndex(int)
     *  @see ptolemy.actor.SuperdenseTimeDirector
     */
    @Override
    public int getIndex() {
        return _index;
    }

    /** Return the next time of interest in the model being executed by
     *  this director or the director of any enclosing model up the
     *  hierarchy. If this director is at the top level, then this
     *  default implementation simply returns infinity, indicating
     *  that this director has no interest in any future time.
     *  If this director is not at the top level, then return
     *  whatever the enclosing director returns.
     *  <p>
     *  This method is useful for domains that perform
     *  speculative execution (such as Continuous itself).
     *  Such a domain in a hierarchical
     *  model (i.e. CT inside DE) uses this method to determine how far
     *  into the future to execute. This is simply an optimization that
     *  reduces the likelihood of having to roll back.
     *  <p>
     *  The base class implementation in Director is almost right,
     *  but not quite, because at the top level it returns current
     *  time. However, this director should not constrain any director
     *  below it from speculatively executing into the future.
     *  Instead, it assumes that any director below it implements
     *  a strict actor semantics.  Note in particular that the
     *  implementation below would block time advancement in
     *  a Continuous in DE in Continuous model because the
     *  top-level model will usually only invoke the DE model
     *  during a zero-step execution, which means that the returned
     *  next iteration time will always be current time, which will
     *  force the inside Continuous director to have a zero step
     *  size always.
     *  @return The next time of interest.
     *  @exception IllegalActionException If creating a Time object fails.
     *  @see #getModelTime()
     */
    @Override
    public Time getModelNextIterationTime() throws IllegalActionException {
        if (isEmbedded()) {
            return super.getModelNextIterationTime();
        }
        return Time.POSITIVE_INFINITY;
    }

    /** Return true, indicating that this director assumes and exports
     *  the strict actor semantics, as described in this paper:
     *  <p>
     *  A. Goderis, C. Brooks, I. Altintas, E. A. Lee, and C. Goble,
     *  "Heterogeneous Composition of Models of Computation,"
     *  EECS Department, University of California, Berkeley,
     *  Tech. Rep. UCB/EECS-2007-139, Nov. 2007.
     *  http://www.eecs.berkeley.edu/Pubs/TechRpts/2007/EECS-2007-139.html
     *  <p>
     *  In particular, a director that implements this interface guarantees
     *  that it will not invoke the postfire() method of an actor until all
     *  its inputs are known at the current tag.  Moreover, it it will only
     *  do so in its own postfire() method, and in its prefire() and fire()
     *  methods, it does not change its own state.  Thus, such a director
     *  can be used within a model of computation that has a fixed-point
     *  semantics, such as SRDirector and ContinuousDirector.
     *  @return True.
     */
    @Override
    public boolean implementsStrictActorSemantics() {
        return true;
    }

    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method. Reset all private variables.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _currentIteration = 0;
        // This variable has to be reset at the very beginning, because
        // some actors may call fireAt method to register breakpoints in DE
        // and Continuous domains, which depend on the value of _index.
        _index = 0;
        // This could be getting re-initialized during execution
        // (e.g., if we are inside a modal model), in which case,
        // if the enclosing director is a superdense time director,
        // we should initialize to its microstep, not to our own.
        // NOTE: Some (weird) directors pretend they are not embedded even
        // if they are (e.g. in Ptides), so we call _isEmbedded() to give
        // the subclass the option of pretending it is not embedded.
        /* NOTE: No, this doesn't make sense. Initialization should
         * reset the microstep to zero, otherwise actors like clocks
         * and PeriodicSampler won't work properly.
        if (isEmbedded()) {
            Nameable container = getContainer();
            if (container instanceof CompositeActor) {
                Director executiveDirector = ((CompositeActor) container)
                        .getExecutiveDirector();
                if (executiveDirector instanceof SuperdenseTimeDirector) {
                    _index = ((SuperdenseTimeDirector) executiveDirector)
                            .getIndex();
                }
            }
        }
         */

        _cachedFunctionalProperty = true;
        _functionalPropertyVersion = -1L;

        super.initialize();

        _realStartTime = System.currentTimeMillis();

        // NOTE: The following used to be done in prefire(), which is wrong,
        // because prefire() can be invoked multiple times in an iteration
        // (particularly if this is inside another FixedPointDirector).
        _resetAllReceivers();
    }

    /** Return true if all the controlled actors' isFireFunctional()
     *  methods return true. Otherwise, return false.
     *
     *  @return True if all controlled actors are functional.  Return
     *  false if there is no container or no actors in the container.
     */
    @Override
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
     *  Thus this director tolerates unknown inputs.
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Return a new FixedPointReceiver. If a subclass overrides this
     *  method, the receiver it creates must be a subclass of FixedPointReceiver,
     *  and it must add the receiver to the _receivers list (a protected
     *  member of this class).
     *  @return A new FixedPointReceiver.
     */
    @Override
    public Receiver newReceiver() {
        Receiver receiver = new FixedPointReceiver(this);
        _receivers.add(receiver);
        return receiver;
    }

    /** Call postfire() on all contained actors that were fired in the current
     *  iteration.  Return false if the model
     *  has finished executing, either by reaching the iteration limit, or if
     *  no actors in the model return true in postfire(), or if stop has
     *  been requested, or if no actors fired at all in the last iteration.
     *  This method is called only once for each iteration.
     *  Note that actors are postfired in arbitrary order.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token, or if there still some unknown inputs (which
     *   indicates a causality loop).
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("FixedPointDirector: Called postfire().");
        }

        boolean needMoreIterations = true;
        // If no actors were fired, this director used to return
        // false in postfire. However, this is not correct because there
        // may be actors that are using time to decide whether to fire
        // and, in addition, this may be embedded, in which case future
        // events will trigger firings.
        /*
        int numberOfActors = getScheduler().getSchedule().size();
        if ((numberOfActors > 0) && (_actorsFired.size() == 0)) {
            needMoreIterations = false;
        }
         */

        // The following used to inexplicably iterate only over
        // _actorsFired. Now we iterate over all actors in the order
        // of the schedule.
        Schedule schedule = getScheduler().getSchedule();
        Iterator firingIterator = schedule.firingIterator();
        while (firingIterator.hasNext() && !_stopRequested) {
            Actor actor = ((Firing) firingIterator.next()).getActor();
            // Check for remaining unknown inputs.
            // Don't care about actors that have previously returned false from postfire().
            if (!_areAllInputsKnown(actor)
                    && !_actorsFinishedExecution.contains(actor)) {
                // Construct a list of the unknown inputs.
                StringBuffer unknownInputs = new StringBuffer();
                Iterator inputPorts = actor.inputPortList().iterator();
                IOPort firstPort = null;
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();
                    if (!inputPort.isKnown()) {
                        unknownInputs.append(inputPort.getName());
                        unknownInputs.append("\n");
                        if (firstPort == null) {
                            firstPort = inputPort;
                        }
                    }
                }
                throw new IllegalActionException(actor, firstPort,
                        "Unknown inputs remain. Possible causality loop:\n"
                                + unknownInputs);
            }
            if (_actorsFired.contains(actor)) {
                if (!_postfireActor(actor)) {
                    // postfire() returned false, so prevent the actor
                    // from iterating again.
                    _actorsFinishedExecution.add(actor);
                }
            }
        }
        if (_debugging) {
            _debug(this.getFullName() + ": Iteration " + _currentIteration
                    + " is complete.");
        }

        // NOTE: The following used to be done in prefire(), which is wrong,
        // because prefire() can be invoked multiple times in an iteration
        // (particularly if this is inside another FixedPointDirector).
        _resetAllReceivers();

        // In this base class, the superdense time index is the only advancement
        // of time, and it advances on every iteration. Derived classes must set
        // it to zero in their postfire method if they advance time.
        _index++;

        // Check whether the current execution has reached its iteration limit.
        _currentIteration++;
        int numberOfIterations = ((IntToken) iterations.getToken()).intValue();
        if (numberOfIterations > 0 && _currentIteration >= numberOfIterations) {
            super.postfire();
            return false;
        }

        return super.postfire() && needMoreIterations;
    }

    /** Return true if the director is ready to fire.
     *  If <i>synchronizeToRealTime</i> is true, then
     *  wait for real time elapse to match or exceed model time.
     *  The return whatever the base class returns.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        _synchronizeToRealTime();
        _postfireReturns = true;

        boolean result = true;
        List<IOPort> ports = ((CompositeEntity) getContainer()).portList();
        for (IOPort port : ports) {
            if (port instanceof ParameterPort) {
                if (!port.isKnown()) {
                    result = false;
                    break;
                }
            }
        }
        // The following synchronizes to environment time, making
        // any necessary adjustments for drift or offset of the local clock.
        return super.prefire() && result;
    }

    /** Set the superdense time index. This should only be
     *  called by an enclosing director.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @see #getIndex()
     *  @see ptolemy.actor.SuperdenseTimeDirector
     */
    @Override
    public void setIndex(int index) throws IllegalActionException {
        if (_debugging) {
            _debug("Setting superdense time index to " + index);
        }
        _index = index;
    }

    /** Return an array of suggested directors to be used with
     *  ModalModel. Each director is specified by its full class
     *  name.  The first director in the array will be the default
     *  director used by a modal model.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    @Override
    public String[] suggestedModalModelDirectors() {
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[1] = "ptolemy.domains.modal.kernel.NonStrictFSMDirector";
        defaultSuggestions[0] = "ptolemy.domains.modal.kernel.FSMDirector";
        return defaultSuggestions;
    }

    /** Transfer data from the specified input port of the
     *  container to the ports it is connected to on the inside.
     *  If there is no data on the specified input port, then
     *  set the ports on the inside to absent by calling sendInside(index, null).
     *  This method delegates the data transfer
     *  operation to the transferInputs method of the super class.
     *
     *  @param port The port to transfer tokens from.
     *  @return True if at least one token is transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port.
     */
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        boolean result = false;
        int insideWidth = port.getWidthInside();
        for (int i = 0; i < port.getWidth(); i++) {
            if (port.isKnown(i)) {
                if (port.hasToken(i)) {
                    result = super.transferInputs(port) || result;
                } else {
                    if (i < insideWidth) {
                        port.sendInside(i, null);
                    }
                }
            }
            // we do not explicit reset the receivers receiving inputs
            // from this port because the fixedpoint director resets the
            // receivers in its prefire() method.
        }
        // If the inside is wider than the outside, send clear on the inside.
        for (int i = port.getWidth(); i < insideWidth; i++) {
            port.sendInside(i, null);
        }
        return result;
    }

    /** Transfer data from the specified output port of the
     *  container to the ports it is connected to on the outside.
     *  If there is no data on the specified output port, then
     *  set the ports on the outside to absent by calling send(index, null).
     *  This method delegates the data transfer
     *  operation to the transferOutputs method of the super class.
     *
     *  @param port The port to transfer tokens from.
     *  @return True if at least one token is transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port, or if there are not enough input tokens available.
     */
    @Override
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        boolean result = false;
        int outsideWidth = port.getWidth();
        for (int i = 0; i < port.getWidthInside(); i++) {
            if (port.isKnownInside(i)) {
                if (port.hasTokenInside(i)) {
                    result = super.transferOutputs(port) || result;
                } else {
                    // Mark the destination receivers absent, if the destination
                    // receiver has such a notion, and otherwise do nothing.
                    if (i < outsideWidth) {
                        port.send(i, null);
                    }
                }
            } else if (i < outsideWidth) {
                // Output is not known. To ensure that this fact propagate
                // outside, find the remote receivers and reset them.
                // This was causing a monotonicity failure in certain
                // modal models.
                Receiver[][] remoteReceivers = port.getRemoteReceivers();
                for (Receiver remoteReceiver : remoteReceivers[i]) {
                    remoteReceiver.reset();
                }
            }
        }
        // If the outside is wider than the inside, send clear on the outside.
        /* NOTE: The following isn't right!  Need to leave the output unknown in case
         * we are in a modal model. A transition may be wanting to set it.
         * it has to become known only if the environment sets it known
         * by presuming that any unproduced outputs are absent.
         *
        for (int i = port.getWidthInside(); i < outsideWidth; i++) {
            port.send(i, null);
        }
         */
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if all the inputs of the specified actor are known.
     *  @param actor The specified actor.
     *  @return True if the all the inputs of the specified actor are known.
     *  @exception IllegalActionException If thrown while checking if an input
     *  port is known.
     */
    protected boolean _areAllInputsKnown(Actor actor)
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
     *  @param actor The actor to be fired.
     *  @exception IllegalActionException If the prefire() method
     *   returns false having previously returned true in the same
     *   iteration, or if the prefire() or fire() method of the actor
     *   throws it.
     */
    protected void _fireActor(Actor actor) throws IllegalActionException {
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
                _sendAbsentToAllUnknownOutputsOf(actor);
            }
        } else {
            // prefire() returned false. The actor declines
            // to fire. This could be because some inputs are
            // not known.  If all inputs are known, then we
            // interpret this to mean that all outputs should be absent.
            // Note that prefire() is executed only after all the inputs are
            // known if the actor is strict.
            if (actor.isStrict() || _areAllInputsKnown(actor)) {
                _actorsFinishedFiring.add(actor);
                _sendAbsentToAllUnknownOutputsOf(actor);
            }
        }
    }

    /** Return true if this iteration has converged.  The iteration has
     *  converged if both the number of known receivers
     *  has not changed since the previous invocation of this method.
     *  @return true if this iteration has converged.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected boolean _hasIterationConverged() throws IllegalActionException {
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

    /** Return true if the specified actor is ready to fire.  An actor is
     *  ready to fire if it has not previously finished firing in this iteration
     *  and either it is strict and all inputs are known or it is nonstrict.
     *  Note that this ignores whether the actor has previously returned
     *  false in postfire().
     *  @param actor The actor that is checked for being ready to fire.
     *  @return true if the actor is ready to fire.
     *  @exception IllegalActionException If thrown while determining
     *  if actors are finished firing, or while determining if the actor is
     *  strict, or while determining if all the inputs are known.
     */
    protected boolean _isReadyToFire(Actor actor) throws IllegalActionException {
        return !_actorsFinishedFiring.contains(actor)
                && (!actor.isStrict() || _areAllInputsKnown(actor));
    }

    /** React to the change in receiver status by incrementing the count of
     *  known receivers.
     */
    protected void _receiverChanged() {
        _currentNumberOfKnownReceivers++;
    }

    /** Reset all receivers to unknown status and clear out variables used
     *  to track which actors fired in the last iteration.
     */
    protected void _resetAllReceivers() {
        _actorsAllowedToFire.clear();
        _actorsFinishedFiring.clear();
        _actorsFired.clear();
        _cachedAllInputsKnown.clear();
        _lastNumberOfKnownReceivers = -1;

        if (_debugging) {
            _debug("    FixedPointDirector is resetting all receivers");
        }
        _currentNumberOfKnownReceivers = 0;

        Iterator receiverIterator = _receivers.iterator();
        while (receiverIterator.hasNext()) {
            FixedPointReceiver receiver = (FixedPointReceiver) receiverIterator
                    .next();
            receiver.reset();
        }
    }

    /** Call the send(index, null) method of each output port with
     *  unknown status of the specified actor.
     *  @param actor The actor.
     *  @exception IllegalActionException If thrown while getting
     *  the width of a port, determining if a port is known
     *  or while sending data.
     */
    protected void _sendAbsentToAllUnknownOutputsOf(Actor actor)
            throws IllegalActionException {
        // An actor, if its firing has finished but some of its
        // outputs are still unknown, clear these outputs.
        // However, there is nothing need to do if this actor has
        // resolved all of its outputs.
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            // NOTE: The following assumes that if ANY destination
            // receiver is known, then all are known. isKnown(j)
            // will return false if ANY destination receiver on channel
            // j is unknown, but send(j, null) will assert that ALL
            // destination receivers are absent.
            for (int j = 0; j < outputPort.getWidth(); j++) {
                if (!outputPort.isKnown(j)) {
                    if (_debugging) {
                        _debug("  FixedPointDirector: Set output "
                                + outputPort.getFullName() + " to absent.");
                    }
                    outputPort.send(j, null);
                }
            }
        }
    }

    /** Synchronize to real time, if appropriate.
     *  NOTE: This method is provided for backward compatibility. The preferred
     *  method for synchronizing to real time is now to use a
     *  {@link ptolemy.actor.lib.SynchronizeToRealTime} attribute, which implements the
     *  {@link ptolemy.actor.TimeRegulator} interface.
     *  @exception IllegalActionException If the <i>synchronizeToRealTime</i>
     *   parameter is ill formed.
     */
    protected void _synchronizeToRealTime() throws IllegalActionException {
        boolean synchronizeValue = ((BooleanToken) synchronizeToRealTime
                .getToken()).booleanValue();

        if (synchronizeValue) {
            int depth = 0;
            try {
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
                            _debug("Waiting for real time to pass: "
                                    + timeToWait);
                        }

                        try {
                            // NOTE: The built-in Java wait() method
                            // does not release the
                            // locks on the workspace, which would block
                            // UI interactions and may cause deadlocks.
                            // SOLUTION: explicitly release read permissions.
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
                                depth = _workspace.releaseReadPermission();
                                wait(timeToWait);
                            }
                        } catch (InterruptedException ex) {
                            // Continue executing.
                        }
                    }
                }
            } finally {
                if (depth > 0) {
                    _workspace.reacquireReadPermission(depth);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The set of actors that have returned true in their prefire() methods
     *  in the current iteration. This is used only to check monotonicity
     *  constraints and to determine which actors should be postfired.
     */
    protected Set _actorsAllowedToFire = new HashSet();

    /** Actors that were fired in the most recent invocation of the fire() method. */
    protected Set _actorsFired = new HashSet();

    /** The current index of the model. */
    protected int _index;

    /** List of all receivers this director has created. */
    protected List _receivers = new LinkedList();

    /** The set of actors that have been fired in this iteration with
     *  all inputs known.
     */
    protected Set _actorsFinishedFiring = new HashSet();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the director by creating the parameters and setting their
     *  values and types.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        _zeroTime = new Time(this, 0.0);

        iterations = new Parameter(this, "iterations", new IntToken(0));
        iterations.setTypeEquals(BaseType.INT);

        synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime");
        synchronizeToRealTime.setExpression("false");
        synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

        FixedPointScheduler scheduler = new FixedPointScheduler(this,
                uniqueName("Scheduler"));
        setScheduler(scheduler);
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The set of actors that have all inputs known in the given iteration. */
    private Set _cachedAllInputsKnown = new HashSet();

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
