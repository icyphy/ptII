/* Director for SysML in the style of IBM Rational Rhapsody.

 Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.domains.sysml.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.FiringsRecordable;
import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.continuous.Advanceable;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SysMLSequentialDirector

/**
 <p>
 Sequential version of a SysML director. This version is inspired by a
 subset of the semantics of IBM Rational's Rhapsody SysML tool.
 In this MoC, all actors execute in the same thread.
 Inputs provided to an input port (by another actor) are
 put into a single queue managed by this director.
 This director executes by first initializing all actors
 (which may cause them to place events on the queue and/or
 to request firings at future times, "timeouts").
 Then, in each iteration, this director retrieves one event
 from the queue, offers it to the destination actor as
 an input, and fires that destination actor. That actor
 may put events on the event queue and/or request firings
 at future times. The director continues
 doing this until there are no more events to be
 processed at the current time.
 At that point, it allows time to advance, and first all
 actors that whose timeouts match the time advance, in the
 same order in which they asserted those timeouts (in previous
 firings). Those firings may again place events in the event queue,
 so this director will again fire actors until the event queue
 is empty.
 <p>
 Ports that are marked as flow ports (which contain an attribute
 named "flow" with value "true") are treated specially.
 First, the value on such ports is persistent. It does not
 disappear after being read, unlike the events that arrive
 on standard ports. Second, when a new value is sent to a flow
 port, the new value is made immediately available to the
 destination actor, and a "change event" is placed on the event
 queue. The change event ensures that the actor is notified of
 the changed value, but the actor may actually notice the change
 value before it processes the change event, if there are already
 other events in the queue destined for that actor.
 <p>
 This director is related to the DE director, with some key
 differences. First, it handles only one event from the
 event queue at a time. Second, the dependencies between
 actors are ignored. Third, it supports "flow ports,"
 which have persistent value, and "change events," which
 indicate to an actor that the value on a flow port has
 changed.
 <p>
 Unlike the DE director, the order in which actors are
 initialized affects the execution because it affects
 the initial order in which events are placed on the event
 queue. After that, if the actors are deterministic,
 then the execution is deterministic.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class SysMLSequentialDirector extends Director implements
SuperdenseTimeDirector {

    /** Construct a director in the given container with the given name.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  Thrown in derived classes.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public SysMLSequentialDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new PNDirector.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SysMLSequentialDirector newObject = (SysMLSequentialDirector) super
                .clone(workspace);
        newObject._inputQueue = null;
        newObject._fireAtRequests = null;
        return newObject;
    }

    /** Start a new iteration (at a new time, presumably) and either
     *  run the actors to completion in order of creation or
     *  wait until a deadlock is detected, depending on activeObjects.
     *  Then deal with the deadlock
     *  by calling the protected method _resolveDeadlock() and return.
     *  This method is synchronized on the director.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public synchronized void fire() throws IllegalActionException {

        Time currentTime = getModelTime();
        if (_debugging) {
            _debug("====== Firing SysMLSequentialDirector at time "
                    + currentTime + " and microstep " + _microstep);
        }
        if (_stopRequested) {
            _debug("******* A stop has been requested at time " + currentTime);
            return;
        }

        // Iterate all the advanceables.
        // FIXME: Is this really the right thing to do?
        // Should probably filter outputs. Those marked with "continuous"
        // variability should be sent to the output unconditionally, and
        // the corresponding ports should be marked as flow ports.
        // Those marked "discrete" should be sent to the output conditionally.
        // But what condition?
        for (Advanceable advanceable : _getAdvanceables()) {
            _iterateActorOnce((Actor) advanceable);
        }

        // If time and microstep match the next firing time, then
        // fire all the actors that have been waiting for this time.
        // The firing order here is the order in which the firing requests were made.
        if (_fireAtRequests.size() > 0) {
            RefireRequest request = _fireAtRequests.peek();
            if (currentTime.equals(request.time)
                    && _microstep == request.microstep) {
                // Time matches.
                while (true) {
                    if (_stopRequested) {
                        return;
                    }

                    request = _fireAtRequests.poll();
                    if (_debugging) {
                        _debug(request.actor.getFullName()
                                + " has requested a firing at the current time and microstep.");
                    }
                    _iterateActorOnce(request.actor);

                    if (_fireAtRequests.size() > 0) {
                        request = _fireAtRequests.peek();
                        if (!_schedule((NamedObj) request.actor, getModelTime())) {
                            break;
                        }
                        if (!currentTime.equals(request.time)
                                || _microstep != request.microstep) {
                            break;
                        }
                    } else {
                        // No more requests.
                        break;
                    }
                }
            }
        }
        // Next, iterate actors until the input queue is empty.
        while (_inputQueue.size() > 0 && !_stopRequested) {
            Input input = _inputQueue.get(0);
            IOPort port = input.receiver.getContainer();
            int channel = port.getChannelForReceiver(input.receiver);
            Actor actor = (Actor) port.getContainer();

            // Input queue is not empty. Extract
            // an input from the queue and deposit in the receiver, unless
            // it is an event from a flow port, in which case the value
            // has already been updated.
            input = _inputQueue.remove(0);

            // Make sure the actor will have exactly one input event.
            // Normally, the receivers will already be cleared, but a misbehaving
            // actor that doesn't read all its inputs, or an actor that has had
            // input ports added to it that it is not aware of will not have
            // cleared its receivers.
            _clearReceivers(actor);

            if (!input.isChangeEvent) {
                // Now make the one input event available to the actor.
                input.receiver.reallyPut(input.token);
                if (_debugging) {
                    _debug(actor.getFullName() + ": Providing input to port "
                            + port.getName() + " on channel " + channel
                            + " with value: " + input.token);
                }
            } else if (_debugging) {
                _debug(actor.getFullName()
                        + ": Providing change event to port " + port.getName()
                        + " on channel " + channel);
            }
            if (actor != getContainer()) {
                if (!_schedule((NamedObj) actor, getModelTime())) {
                    break;
                }
                _iterateActorOnce(actor);
            } else {
                // The actor is the container of this director,
                // so this is an output going to the outside.
                // Send it now.
                _transferOutputs(port);
            }
        }
    }

    /** Override the base class to make a local record of the requested
     *  firing.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param microstep The requested microstep.
     *  @return An instance of Time with the current time value, or
     *   if there is an executive director, the time at which the
     *   container of this director will next be fired
     *   in response to this request.
     *  @exception IllegalActionException If there is an executive director
     *   and it throws it. Derived classes may choose to throw this
     *   exception for other reasons.
     */
    @Override
    public synchronized Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        RefireRequest request = new RefireRequest();
        request.actor = actor;
        request.time = time;
        request.microstep = microstep;

        Time currentTime = getModelTime();
        if (time.compareTo(currentTime) < 0) {
            throw new IllegalActionException(actor,
                    "Requesting firing at time " + time
                    + ", which is in the past. Current time is "
                    + currentTime);
        }

        if (time.compareTo(currentTime) == 0 && microstep <= _microstep
                && !_isInitializing) {
            // NOTE: Incrementing the microstep here is wrong if we are in initialize().
            microstep = _microstep + 1;

            if (microstep == Integer.MAX_VALUE) {
                throw new IllegalActionException(
                        this,
                        actor,
                        "Microstep has hit the maximum while scheduling a firing of "
                                + actor.getFullName()
                                + ". Perhaps the model has a stuttering Zeno Condition?");
            }
        }

        _fireAtRequests.add(request);
        if (_debugging) {
            _debug(actor.getFullName() + " requests firing at time " + time
                    + " and microstep " + microstep);
        }
        return time;
    }

    /** Invoke the initialize() method of ProcessDirector. Also set all the
     *  state variables to the their initial values. The list of process
     *  listeners is not reset as the developer might want to reuse the
     *  list of listeners.
     *  @exception IllegalActionException If the initialize() method of one
     *  of the deeply contained actors throws it.
     */
    @Override
    public synchronized void initialize() throws IllegalActionException {
        _isInitializing = true;
        _advanceablesVersion = -1;
        // FIXME: Do we want to start at microstep 0 or 1?
        // I'm assuming 0 in case some internal model is Continuous.
        _microstep = 0;

        // Initialize queues.
        if (_inputQueue == null) {
            _inputQueue = new LinkedList<Input>();
        } else {
            _inputQueue.clear();
        }
        if (_fireAtRequests == null) {
            _fireAtRequests = new PriorityQueue<RefireRequest>();
        } else {
            _fireAtRequests.clear();
        }

        // Reset the receivers on the inside of output ports.
        Actor container = (Actor) getContainer();
        List<IOPort> ports = container.outputPortList();
        for (IOPort port : ports) {
            Receiver[][] receivers = port.getInsideReceivers();
            for (Receiver[] receiver : receivers) {
                for (int j = 0; j < receiver.length; j++) {
                    receiver[j].reset();
                }
            }
        }

        // The following calls initialize(Actor), which may
        // create the threads and start them, if using active objects.
        // It also initializes the _queueDirectory structure.
        super.initialize();

        // The above has initialized the actors, which may have
        // called fireAt(). If so, we need to delegate the fireAt()
        // up the hierarchy.
        if (isEmbedded()) {
            // If any actor called fireAt() during initialization, then
            // find the earliest requested firing time and convey a fireAt()
            // call to the container.
            RefireRequest nextFiring = _earliestNextFiringRequest();
            if (nextFiring != null) {
                fireContainerAt(nextFiring.time, nextFiring.microstep);
            }
        }
        _isInitializing = false;
    }

    /** Initialize the given actor.  This method is generally called
     *  by the initialize() method of the director, and by the manager
     *  whenever an actor is added to an executing model as a
     *  mutation.  This method will generally perform domain-specific
     *  initialization on the specified actor and call its
     *  initialize() method.  In this base class, only the actor's
     *  initialize() method of the actor is called and no
     *  domain-specific initialization is performed.  Typical actions
     *  a director might perform include starting threads to execute
     *  the actor or checking to see whether the actor can be managed
     *  by this director.  For example, a time-based domain (such as
     *  CT) might reject sequence based actors.
     *  @param actor The actor that is to be initialized.
     *  @exception IllegalActionException If the actor is not
     *  acceptable to the domain.  Not thrown in this base class.
     */
    @Override
    public void initialize(Actor actor) throws IllegalActionException {
        // Reset the receivers.
        Iterator ports = actor.inputPortList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            Receiver[][] receivers = port.getReceivers();

            for (Receiver[] receiver : receivers) {
                for (int j = 0; j < receiver.length; j++) {
                    receiver[j].reset();
                }
            }
        }
        super.initialize(actor);
    }

    /** Return a superdense time index for the current time,
     *  where the index is equal to the microstep.
     *  @return A superdense time index.
     *  @see #setIndex(int)
     *  @see ptolemy.actor.SuperdenseTimeDirector
     */
    @Override
    public int getIndex() {
        return _microstep;
    }

    /** Return a new receiver SysMLAReceiver.
     *  @return A new SysMLAReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new SysMLSequentialReceiver();
    }

    /** Return true if next actor in list of fire requests was scheduled
     *  and can execute.
     *  @return true If next actor can execute.
     *  @exception IllegalActionException If request to resource scheduler fails.
     */
    @Override
    public boolean scheduleContainedActors() throws IllegalActionException {
        RefireRequest request = _fireAtRequests.peek();
        if (request == null) {
            return true;
        }
        return _schedule((NamedObj) request.actor, getModelTime());
    }

    /** Return false if a stop has been requested or if
     *  the model has reached deadlock. Otherwise, if there is a
     *  pending fireAt request, either advance time to that
     *  requested time (if at the top level) or request a
     *  firing at that time. If there is no pending fireAt
     *  request, then return false. Otherwise, return true.
     *  @return False if the director has detected a deadlock or
     *   a stop has been requested.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        super.postfire();

        // Determine the earliest time at which an actor wants to be fired next.
        RefireRequest earliestFireAtRequest = _earliestNextFiringRequest();
        if (earliestFireAtRequest == null) {
            // There is no pending fireAt request.
            // Advance directly to the stop time, if it is finite, unless we
            // are embedded.
            if (!isEmbedded()) {
                Time stopTime = getModelStopTime();
                if (stopTime == null || stopTime == Time.POSITIVE_INFINITY) {
                    // If the stop time is also infinity, then stop execution.
                    // FIXME: If there are actors with unpredictable events,
                    // such as FMUs, then this might not be what we want to do.
                    // For now, we require a finite stop time so that the model
                    // will attempt a finite step size.
                    if (_debugging) {
                        _debug("No pending events and stop time is not given. Stopping execution.");
                    }
                    stop();
                    return false;
                } else {
                    // Attempt to advance to the stop time.
                    SuperdenseTime advance = _advanceAdvanceables(stopTime, 0);
                    setModelTime(advance.time);
                    setIndex(advance.microstep);
                    return true;
                }
            } else {
                return true;
            }
        }
        if (earliestFireAtRequest.time.compareTo(getModelStopTime()) > 0) {
            // The next available time is past the stop time.
            if (_debugging) {
                _debug("Next firing request is beyond the model stop time of "
                        + getModelStopTime());
            }
            stop();
            return false;
        }
        if (_debugging) {
            _debug("Next earliest fire at request is at time "
                    + earliestFireAtRequest.time + " and microstep "
                    + earliestFireAtRequest.microstep);
        }

        if (earliestFireAtRequest.time.compareTo(Time.POSITIVE_INFINITY) < 0) {
            if (isEmbedded()) {
                fireContainerAt(earliestFireAtRequest.time,
                        earliestFireAtRequest.microstep);
                if (_debugging) {
                    _debug("+++ Requesting refiring at "
                            + earliestFireAtRequest.time);
                }
            } else {
                SuperdenseTime advance = _advanceAdvanceables(
                        earliestFireAtRequest.time,
                        earliestFireAtRequest.microstep);
                setModelTime(advance.time);
                setIndex(advance.microstep);
                if (_debugging) {
                    _debug("+++ Advancing time to " + advance.time
                            + " and microstep " + advance.microstep);
                }
            }
        } else if (!isEmbedded()) {
            // Next firing request is at infinity. Again, we are done.
            if (_debugging) {
                _debug("No pending events and stop time is not given. Stopping execution.");
            }
            stop();
            return false;
        }
        return true;
    }

    /** Override the base class to set time to match environment
     *  time if this director is embedded.
     *  @return Whatever the superclass returns.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Director: Called prefire() at time " + getModelTime());
        }
        if (isEmbedded()) {
            setModelTime(localClock.getLocalTimeForCurrentEnvironmentTime());
            Director containingDirector = ((CompositeActor) ((CompositeActor) getContainer())
                    .getContainer()).getDirector();
            if (containingDirector instanceof DEDirector) {
                setIndex(((DEDirector) containingDirector).getIndex());
            } else if (containingDirector instanceof FixedPointDirector) {
                setIndex(((FixedPointDirector) containingDirector).getIndex());
            }
            if (_debugging) {
                _debug("-- Setting current time to " + getModelTime());
            }
        }
        return super.prefire();
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
        _microstep = index;
    }

    /** Set a new value to the current time of the model.
     *  @param newTime The new current simulation time.
     *  @exception IllegalActionException If the new time is less than
     *  the current time returned by getCurrentTime().
     *  @see #getModelTime()
     */
    @Override
    public void setModelTime(Time newTime) throws IllegalActionException {
        // FIXME: Needed only for debugging.
        super.setModelTime(newTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Advance time of all actors that implement Advanceable to the
     *  specified time and microstep. If they all succeed, then return
     *  the specified time and microstep. Otherwise, return the
     *  time and microstep that is the minimum of the superdense times
     *  to which they succeeded.
     *  @param time The time to advance to.
     *  @param microstep The microstep to advance to.
     *  @return The time and microstep that is the minimum of the
     *  superdense times to which they succeeded.
     *  @exception IllegalActionException If an actor refuses to advance time,
     *   or if the proposed time is in the past.
     */
    protected SuperdenseTime _advanceAdvanceables(Time time, int microstep)
            throws IllegalActionException {
        Time currentTime = getModelTime();
        for (Advanceable advanceable : _getAdvanceables()) {
            int timeCompareToCurrentTime = time.compareTo(currentTime);
            if (timeCompareToCurrentTime < 0 || timeCompareToCurrentTime == 0
                    && microstep < _microstep) {
                throw new IllegalActionException(this,
                        "Proposed time advance is not an advance. Current time is "
                                + currentTime + ", and proposed time is "
                                + time);
            }
            if (!advanceable.advance(time, microstep)) {
                // Step size has been rejected.
                double suggestedStepSize = advanceable.refinedStepSize();
                if (suggestedStepSize > 0) {
                    // This will presumably only decrease time.
                    // That will be checked at the start of the next iteration.
                    time = currentTime.add(suggestedStepSize);
                    microstep = 1;
                } else if (suggestedStepSize == 0) {
                    // Let the microstep advance.
                    microstep = microstep + 1;
                    time = currentTime;
                }
            }
        }
        // Perform one last check for a valid result.
        int timeCompareToCurrentTime = time.compareTo(currentTime);
        if (timeCompareToCurrentTime < 0 || timeCompareToCurrentTime == 0
                && microstep < _microstep) {
            throw new IllegalActionException(this,
                    "Proposed time advance is not an advance. Current time is "
                            + currentTime + ", and proposed time is " + time);
        }
        SuperdenseTime result = new SuperdenseTime();
        result.time = time;
        result.microstep = microstep;
        return result;
    }

    /** Clear all the input receivers for the specified actor.
     *  @param actor The actor.
     *  @exception IllegalActionException If the receivers can't be cleared.
     */
    protected void _clearReceivers(Actor actor) throws IllegalActionException {
        List<IOPort> inputPorts = actor.inputPortList();
        for (IOPort inputPort : inputPorts) {
            if (_isFlowPort(inputPort)) {
                continue;
            }
            Receiver[][] receivers = inputPort.getReceivers();
            for (Receiver[] receiver : receivers) {
                for (int j = 0; j < receiver.length; j++) {
                    if (receiver[j] != null) {
                        receiver[j].clear();
                    }
                }
            }
        }
    }

    // FIXME: need a clone method with at least _advanceablesVersion = -1;

    /** Return the earliest pending fire request.
     *  @return The earliest pending fire request, or null if there
     *   are no pending fireAt requests.
     */
    protected RefireRequest _earliestNextFiringRequest() {
        if (_fireAtRequests.size() > 0) {
            RefireRequest request = _fireAtRequests.peek();
            return request;
        }
        return null;
    }

    /** Return a list of actors under the control of this director that implement
     *  the {@link Advanceable} interface.
     *  @return the list of actors.
     */
    protected List<Advanceable> _getAdvanceables() {
        if (workspace().getVersion() != _advanceablesVersion) {
            _advanceablesVersion = workspace().getVersion();
            CompositeEntity container = (CompositeEntity) getContainer();
            _advanceables = new LinkedList<Advanceable>();
            List<Actor> actors = container.deepEntityList();
            for (Actor actor : actors) {
                if (actor instanceof Advanceable) {
                    _advanceables.add((Advanceable) actor);
                }
            }
        }
        return _advanceables;
    }

    /** Return true if the specified port is a flow port.
     *  @param port The port.
     *  @return True if the port contains a boolean-valued parameter named "flow"
     *   with value true.
     */
    protected boolean _isFlowPort(IOPort port) {
        boolean isFlowPort = false;
        Attribute flowPortMarker = port.getAttribute("flow");
        if (flowPortMarker instanceof Parameter) {
            try {
                Token flowPortMarkerValue = ((Parameter) flowPortMarker)
                        .getToken();
                if (flowPortMarkerValue instanceof BooleanToken
                        && ((BooleanToken) flowPortMarkerValue).booleanValue()) {
                    isFlowPort = true;
                }
            } catch (IllegalActionException e) {
                // If we get an exception, ignore and assume it's not
                // a flow port.
            }
        }
        return isFlowPort;
    }

    /** Iterate the specified actor once.
     *  @param actor The actor to be iterated.
     *  @return True if either prefire() returns false
     *   or postfire() returns true.
     *  @exception IllegalActionException If the actor throws it.
     */
    protected boolean _iterateActorOnce(Actor actor)
            throws IllegalActionException {
        // FIXME: See ProcessThread.iterateActor() for very similar code.
        FiringsRecordable firingsRecordable = null;
        if (actor instanceof FiringsRecordable) {
            firingsRecordable = (FiringsRecordable) actor;
        }

        if (firingsRecordable != null) {
            firingsRecordable.recordFiring(FiringEvent.BEFORE_PREFIRE);
        }
        boolean result = true;
        if (actor.prefire()) {

            if (firingsRecordable != null) {
                firingsRecordable.recordFiring(FiringEvent.AFTER_PREFIRE);
                firingsRecordable.recordFiring(FiringEvent.BEFORE_FIRE);
            }

            actor.fire();

            if (firingsRecordable != null) {
                firingsRecordable.recordFiring(FiringEvent.AFTER_FIRE);
                firingsRecordable.recordFiring(FiringEvent.BEFORE_POSTFIRE);
            }

            result = actor.postfire();

            if (firingsRecordable != null) {
                firingsRecordable.recordFiring(FiringEvent.AFTER_POSTFIRE);
            }
        } else {
            if (firingsRecordable != null) {
                firingsRecordable.recordFiring(FiringEvent.AFTER_PREFIRE);
            }
        }
        if (!result) {
            // Postfire returned false.
            if (_debugging) {
                _debug(actor.getFullName()
                        + " postfire() returns false. Terminating actor.");
            }
            _actorsFinishedExecution.add(actor);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A local boolean variable indicating whether this director is in
     *  initialization phase execution.
     */
    protected boolean _isInitializing = false;

    /** The current microstep. */
    protected int _microstep = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of actors implementing {@link Advanceable}. */
    private List<Advanceable> _advanceables;

    /** Workspace version of the list of Advanceables. */
    private long _advanceablesVersion = -1;

    /** Fire at times. */
    private PriorityQueue<RefireRequest> _fireAtRequests = null;

    /** Input queue. */
    private List<Input> _inputQueue = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Data structure for storing inputs in the director's queue. */
    private static class Input {
        public boolean isChangeEvent;
        public SysMLSequentialReceiver receiver;
        public Token token;

        @Override
        public String toString() {
            IOPort port = receiver.getContainer();
            int channel;
            try {
                channel = port.getChannelForReceiver(receiver);
            } catch (IllegalActionException e) {
                // This should not happen.
                return "[invalid receiver]";
            }
            if (isChangeEvent) {
                return "[changeEvent for port " + port.getName() + " channel "
                        + channel + "]";
            }
            return "[" + token + " for port " + port.getName() + " channel "
            + channel + "]";
        }
    }

    /** Data structure for a refire request. */
    private static class RefireRequest implements Comparable {
        public Actor actor;
        public Time time;
        public int microstep;

        @Override
        public int compareTo(Object request) {
            int result = time.compareTo(((RefireRequest) request).time);
            if (result == 0) {
                if (microstep < ((RefireRequest) request).microstep) {
                    return -1;
                } else if (microstep == ((RefireRequest) request).microstep) {
                    return 0;
                } else {
                    return 1;
                }
            }
            return result;
        }

        /** Return true if the argument is an instance of RefireRequest
         *  and compares as the same value.
         *  @param object An instance of Object.
         *  @return True if the argument is a RefireRequest.
         */
        @Override
        public boolean equals(Object object) {
            // See http://www.technofundo.com/tech/java/equalhash.html
            if (object == this) {
                return true;
            }
            if (object == null) {
                return false;
            }
            // This test rules out subclasses.
            if (object.getClass() != getClass()) {
                return false;
            }

            if (compareTo(object) == 0) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            // FIXME: Shouldn't the hashCode take into account the microstep?
            return time.hashCode();
        }
    }

    /** Data structure for storing a superdense time. */
    public static class SuperdenseTime {
        /** The time. */
        public Time time;
        /** The microstep.. */
        public int microstep;
    }

    /** Variant of a Mailbox that overrides the put() method to
     *  divert the input to the director's queue
     *  and then provides a method to really put a token into
     *  a receiver. If the containing port has a boolean-valued
     *  attribute named "flow", then the behavior is quite different.
     *  First, a put() updates the value of the receiver immediately
     *  in addition to putting a "change event" on the event queue.
     *  Second, the receiver value is persistent. A get() does not
     *  clear the receiver.
     */
    public class SysMLSequentialReceiver extends Mailbox {

        /** Get the contained Token.  If there is none, throw an exception.
         *  The token is removed.
         *  @return The token contained by this mailbox.
         *  @exception NoTokenException If this mailbox is empty.
         */
        @Override
        public Token get() throws NoTokenException {
            if (_token == null) {
                throw new NoTokenException(getContainer(),
                        "Attempt to get data from an empty mailbox.");
            }
            IOPort port = getContainer();
            boolean isFlowPort = _isFlowPort(port);
            if (isFlowPort) {
                // Value is persistent. Do not clear.
                return _token;
            } else {
                Token token = _token;
                _token = null;
                return token;
            }
        }

        /** Put a token into the queue for containing actor.
         *  @param token The token to be put into the queue.
         */
        @Override
        public void put(Token token) {
            IOPort port = getContainer();
            boolean isFlowPort = _isFlowPort(port);
            Input input = new Input();
            input.receiver = this;
            input.token = token;
            input.isChangeEvent = isFlowPort;
            _inputQueue.add(input);
            if (SysMLSequentialDirector.this._debugging) {
                Actor actor = (Actor) port.getContainer();
                SysMLSequentialDirector.this
                ._debug("Adding to queue event destined for "
                        + actor.getName() + " at time "
                        + getModelTime() + ": " + input);
                SysMLSequentialDirector.this._debug("input queue: "
                        + _inputQueue);
            }
            if (isFlowPort) {
                // Do not use super.put() here because it
                // will throw an exception if there already
                // is a token.
                _token = token;
            }
        }

        /** Put a token into the mailbox.
         *  @param token The token to be put into the mailbox.
         *  @exception NoRoomException If this mailbox is not empty.
         */
        public void reallyPut(Token token) throws NoRoomException {
            super.put(token);
        }
    }
}
