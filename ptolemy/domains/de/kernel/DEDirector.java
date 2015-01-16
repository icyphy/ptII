/* The DE domain director.

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
package ptolemy.domains.de.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.ActorExecutionAspect;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.QuasiTransparentDirector;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DEDirector

/**
 <p>This director implements the discrete-event (DE) model of computation (MoC).
 It should be used as the local director of a CompositeActor that is
 to be executed according to the DE MoC. This director maintains a totally
 ordered set of events and processes these events in the order defined on
 their tags and depths.
 </p><p>
 An event is associated with a tag, which is a tuple of timestamp and
 microstep. A timestamp indicates the model time when this event occurs. It
 is an object of the {@link ptolemy.actor.util.Time} class. A microstep is an
 integer which represents the index of the sequence of execution phases when
 this director processes events with the same timestamp. Two tags are equal
 if they have the same timestamp and microstep. If two events have the same
 tag, they are called simultaneous events.
 </p><p>
 Microsteps can only be increased by calling the fireAt() method. For example,
 when an actor requests to be fired again at the current model time, a
 new event with the same timestamp but a bigger microstep (incremented by 1)
 will be generated.
 </p><p>
 An event is also associated with a depth reflecting its priority, based
 on which a DE director chooses the execution order for simultaneous events.
 A depth is an integer and a larger value of depth indicates a lower priority.
 The depth of an event is determined by topologically sorting all the ports
 of actors according to their data dependencies over which there is no time
 delay.
 </p><p>
 The order of events is defined as follows. An event A is said to be earlier
 than another event B if A's timestamp is smaller than B's; or if A's
 timestamp is the same as B's, and A's microstep is smaller than B's; or if
 A's tag is the same as B's, and A's depth is smaller than B's. By giving
 events this well-defined order, this director can handle simultaneous events
 in a deterministic way.
 </p><p>
 The bottleneck in a typical DE simulator is in the maintenance of the
 global event queue. This director uses the calendar queue as the global
 event queue. This is an efficient algorithm with O(1) time complexity in
 both enqueue and dequeue operations. Sorting in the
 {@link ptolemy.actor.util.CalendarQueue} class is done according to the
 order defined above.
 </p><p>
 The complexity of the calendar algorithm is sensitive to the length of the
 event queue. When the size of the event queue becomes too long or changes
 very often, the simulation performance suffers from the penalties of queuing
 and dequeuing events. A few mechanisms are implemented to reduce such
 penalties by keeping the event queue short. The first mechanism is to only
 store in the event queue <i>pure</i> events and the <i>trigger</i> events
 with the same timestamp and microstep as those of the director. See
 {@link DEEvent} for explanation of these two types of events. What is more,
 no duplicate trigger events are allowed in the event queue. Another mechanism
 is that in a hierarchical model, each level keeps a local event queue.
 A lower level only reports the earliest event to its upper level
 to schedule a future firing. The last mechanism is to maintain a list which
 records all actors that are disabled. Any triggers sent to the actors in
 this list are discarded.
 </p><p>
 In the initialize() method, depths of actors and IO ports are statically
 analyzed and calculated. They are not calculated in the preinitialize()
 method because hierarchical models may change their structures during their
 preinitialize() method. For example, a modal model does not specify its
 initial state (and its refinement) until the end of its preinitialize()
 method. See {@link ptolemy.domains.modal.kernel.FSMActor}. In order to support
 mutation, this director recalculates the depths at the beginning of its next
 iteration.
 </p><p>
 There are two types of depths: one is associated with IO ports, which
 reflects the order of trigger events; the other one is associated with
 actors, which is for pure events. The relationship between the depths of IO
 ports and actors is that the depth of an actor is the smallest of the depths
 of its IO ports. Pure events can only be produced by calling the fireAt()
 method, and trigger events can only be produced by actors that produce
 outputs. See {@link ptolemy.domains.de.kernel.DEReceiver#put(Token)}.
 </p><p>
 Directed loops of IO ports with no delay will trigger an exception.
 These are called <i>causality loops</i>. Such loops can be broken with
 actors whose output ports do not have an immediate dependence on their
 input ports, such as the <i>TimeDelay</i> actor.  Notice that the
 <i>TimeDelay</i> actor breaks a causality loop even if the time
 delay is set to 0.0. This is because DE uses a <i>superdense</i>
 notion of time.  The output is interpreted as being strictly later
 than the input even though its time value is the same.
 Whether a causality loop exists is determined by the
 {@link ptolemy.actor.util.CausalityInterface} returned by each actor's
 getCausalityInterface() method.
 </p><p>
 An input port in a DE model contains an instance of DEReceiver.
 When a token is put into a DEReceiver, that receiver posts a trigger
 event to the director. This director sorts trigger events in a global event
 queue.
 </p><p>
 An iteration, in the DE domain, is defined as processing all the events
 whose tags are equal to the current tag of the director (also called the
 model tag). At the beginning of the fire() method, this director dequeues
 a subset of the earliest events (the ones with smallest timestamp, microstep,
 and depth) that have the same destination actor
 from the global event queue. Then, this director fires that actor.
 This actor must consume tokens from its input port(s),
 and usually produces new events on its output port(s). These new events will
 trigger the destination actors to fire. It is important that the actor
 actually consumes tokens from its inputs, even if the tokens are solely
 used to trigger reactions, because the actor will be fired repeatedly
 until there are no more tokens in its input ports with the same tag,
 or until the actor returns false in its prefire() method. The
 director then keeps dequeuing and processing the earliest events from the
 event queue until no more events have the same tag.
 </p><p>
 Note that each time this director fires an actor, it
 also invokes postfire() on that actor.
 Note that under this policy, it is possible for an actor to be fired and postfired
 multiple times in an iteration.
 This does not really correctly implement superdense time semantics, but it is
 an approximation that will reject some models that should be able to be executed.
 An actor like the TimeDelay will be fired (and postfired) multiple times
 at a superdense time index if it is in a feedback loop.
 </p><p>
 A model starts from the time specified by <i>startTime</i>. This is blank
 by default, which indicates that the start time is the current time of
 the enclosing director, if there is one, and 0.0 otherwise.
 The stop time of the execution can be set
 using the <i>stopTime</i> parameter. The parameter has a default value
 <i>Infinity</i>, which means the execution runs forever.
 </p><p>
 Execution of a DE model ends when the timestamp of the earliest event
 exceeds the stop time. This stopping condition is checked inside
 the postfire() method of this director. By default, execution also ends
 when the global event queue becomes empty. Sometimes, the desired
 behaviour is for the director to wait on an empty queue until another
 thread makes new events available. For example, a DE actor may produce
 events when a user hits a button on the screen. To prevent ending the
 execution when there are no more events, set the
 <i>stopWhenQueueIsEmpty</i> parameter to <code>false</code>.
 </p><p>
 Parameters <i>isCQAdaptive</i>, <i>minBinCount</i>, and
 <i>binCountFactor</i>, are used to configure the calendar queue.
 Changes to these parameters are ignored when the model is running.
 </p><p>
 If the parameter <i>synchronizeToRealTime</i> is set to <code>true</code>,
 then the director will not process events until the real time elapsed
 since the model started matches the timestamp of the event.
 This ensures that the director does not get ahead of real time. However,
 of course, this does not ensure that the director keeps up with real time.
 </p><p>
 This director tolerates changes to the model during execution.
 The change should be queued with a component in the hierarchy using
 requestChange().  While invoking those changes, the method
 invalidateSchedule() is expected to be called, notifying the director
 that the topology it used to calculate the priorities of the actors
 is no longer valid.  This will result in the priorities (depths of actors)
 being recalculated the next time prefire() is invoked.
 </p><p>
 <b>Limitations</b>: According to [1], at each microstep, DE should
 perform a fixed point iteration. This implementation does not do that,
 and consequently, this director is not able to execute all correctly
 constructed DE models. For an example, see
 $PTII/ptolemy/domains/de/test/auto/DEFixedPointLimitation.xml.
 That example has a DE opaque composite actor in a feedback loop.
 In principle, there should be no causality loop. The actor output
 should be able to be produced without knowing the input. However,
 the inside director has to guarantee that when it fires any of
 its contained actors, all inputs of a given microstep are available
 to that actor. As a consequence, the opaque actor also needs to know
 all of its inputs at the current microstep. Hence, a causality loop
 is reported. We encourage the reader to make a variant of this director
 that can handle such models.
  </p><p>
 <b>References</b>:
 <br>
 [1] Lee, E. A. and H. Zheng (2007). Leveraging Synchronous Language
 Principles for Heterogeneous Modeling and Design of Embedded Systems.
 EMSOFT, Salzburg, Austria, October, ACM.

 @author Lukito Muliadi, Edward A. Lee, Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
public class DEDirector extends Director implements SuperdenseTimeDirector {

    /* NOTE: This implementation of DE has a very subtle bug documented in the
     * following test:
     *   $PTII/ptolemy/domains/de/test/auto/knownFailedTests/DirectFeedback.xml
     * This test exposes a subtle bug in DE that is probably not worth
     * fixing because the only fix I can find would incur considerable
     * overhead on every event transaction, and it is rather difficult to
     * write an actor that will trigger the bug. The SuperdensTimeTest
     * actor used in this test is such an actor, but as of this writing,
     * there are no such actor in the library.
     *
     * The bug occurs when an actor declares that an output port does
     * not depend on any input (something that is rather hard to do
     * correctly), and then feeds back a signal directly
     * from the output to an input. The bug is that an output token
     * produced by the actor may be visible to the actor in the very same
     * firing of the actor, or in postfire of the same iteration. This violates
     * a principle in DE that when an actor firing begins, all inputs at
     * the current superdense time are available.
     */

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public DEDirector() throws IllegalActionException, NameDuplicationException {
        super();
        _initParameters();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public DEDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _initParameters();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public DEDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The factor when adjusting the bin number.
     *  This parameter must contain an IntToken.
     *  Changes to this parameter are ignored when the model is running.
     *  The value defaults to 2.
     */
    public Parameter binCountFactor;

    /** A flag indicating whether this director should enforce
     *  microstep semantics, throwing an exception when actors
     *  deliver events at microstep 0. Such events can arise from
     *  attempting to deliver to the DE domain a continuous signal
     *  from the Continuous domain. This is a boolean that defaults
     *  to false.
     */
    public Parameter enforceMicrostepSemantics;

    /** Specify whether the calendar queue adjusts its bin number
     *  at run time. This parameter must contain a BooleanToken.
     *  If this parameter is true, the calendar queue will adapt
     *  its bin number with respect to the distribution of events.
     *  Changes to this parameter are ignored when the model is running.
     *  The value defaults to true.
     */
    public Parameter isCQAdaptive;

    /** The minimum (initial) number of bins in the calendar queue.
     *  This parameter must contain an IntToken.
     *  Changes to this parameter are ignored when the model is running.
     *  The value defaults to 2.
     */
    public Parameter minBinCount;

    /** Specify whether the execution stops when the queue is empty.
     *  This parameter must contain a
     *  BooleanToken. If this parameter is true, the
     *  execution of the model will be stopped when the queue is empty.
     *  The value defaults to true.
     */
    public Parameter stopWhenQueueIsEmpty;

    /** Specify whether the execution should synchronize to the
     *  real time. This parameter must contain a BooleanToken.
     *  If this parameter is true, then do not process events until the
     *  elapsed real time matches the time stamp of the events.
     *  The value defaults to false.
     */
    public Parameter synchronizeToRealTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the specified listener to the current set of debug listeners.
     *  If an event queue has been created, register the listener to that queue.
     *  @param listener The listener to be added to the list of listeners
     *  to which debug messages are sent.
     *  @see #removeDebugListener(DebugListener)
     */
    @Override
    public void addDebugListener(DebugListener listener) {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _eventQueue.addDebugListener(listener);
            }
        }

        super.addDebugListener(listener);
    }

    /** Update the director parameters when attributes are changed.
     *  Changes to <i>isCQAdaptive</i>, <i>minBinCount</i>, and
     *  <i>binCountFactor</i> parameters will only be effective on
     *  the next time when the model is executed.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *  Not thrown in this class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == stopWhenQueueIsEmpty) {
            _stopWhenQueueIsEmpty = ((BooleanToken) stopWhenQueueIsEmpty
                    .getToken()).booleanValue();
        } else if (attribute == synchronizeToRealTime) {
            _synchronizeToRealTime = ((BooleanToken) synchronizeToRealTime
                    .getToken()).booleanValue();
        } else if (attribute == enforceMicrostepSemantics) {
            _enforceMicrostepSemantics = ((BooleanToken) enforceMicrostepSemantics
                    .getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Cancel a requested firing of the given actor at the given model
     *  time microstep 1.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @exception IllegalActionException If cancelling a firing is not
     *   supported by the current event queue.
     */
    public void cancelFireAt(Actor actor, Time time)
            throws IllegalActionException {
        cancelFireAt(actor, time, 1);
    }

    /** Cancel a requested firing of the given actor at the given model
     *  time with the given microstep.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param index The microstep.
     *  @exception IllegalActionException If cancelling a firing is not
     *   supported by the current event queue.
     */
    public void cancelFireAt(Actor actor, Time time, int index)
            throws IllegalActionException {
        if (_eventQueue == null) {
            throw new IllegalActionException(this,
                    "Calling cancelFireAt() before preinitialize().");
        }
        if (_debugging) {
            _debug("DEDirector: Cancelling firing of actor "
                    + actor.getFullName() + " at " + time + " with microstep "
                    + index);
        }
        int depth = _getDepthOfActor(actor);
        _eventQueue.remove(new DEEvent(actor, time, index, depth));
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DEDirector newObject = (DEDirector) super.clone(workspace);
        newObject._disabledActors = null;
        newObject._eventQueue = null;
        newObject._exceedStopTime = false;
        newObject._isInitializing = false;
        newObject._microstep = 1;
        newObject._noMoreActorsToFire = false;
        newObject._realStartTime = 0;
        newObject._stopFireRequested = false;
        return newObject;
    }

    /** Return a string that describes the depths of actors and their ports.
     *  These depths are used to prioritize firings, where lower depths
     *  result in higher priorities.
     *  @return A string that describes the depths of actors and their ports.
     *  @exception IllegalActionException If there is a causality loop.
     */
    public String describePriorities() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) container
                .getCausalityInterface();
        return causality.describeDepths();
    }

    /** Fire actors according to events in the event queue. The actual
     *  selecting which events to process is done in _fire(). _fire()
     *  will return whether the previous firing was successful. According
     *  to this information, it is decided whether _fire() should be called
     *  again in order to keep processing events. After each actor firing,
     *  book keeping procedures are called, to keep track of the current
     *  state of the scheduler. The model time of the next events are also
     *  checked to see if we have produced an event of smaller timestamp.
     *  @see #_fire
     *  @exception IllegalActionException If we couldn't process an event
     *  or if an event of smaller timestamp is found within the event queue.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("========= " + this.getName() + " director fires at "
                    + getModelTime() + "  with microstep as " + _microstep);
        }

        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A BIG while loop that handles all events with the same tag.
        while (true) {
            int result = _fire();
            assert result <= 1 && result >= -1;
            if (result == 1) {
                continue;
            } else if (result == -1) {
                _noActorToFire();
                return;
            } // else if 0, keep executing

            // after actor firing, the subclass may wish to perform some book keeping
            // procedures. However in this class the following method does nothing.
            _actorFired();

            if (!_checkForNextEvent()) {
                break;
            } // else keep executing in the current iteration
        } // Close the BIG while loop.

        // Since we are now actually stopping the firing, we can set this false.
        _stopFireRequested = false;

        if (_debugging) {
            _debug("DE director fired!");
        }
    }

    /** Schedule an actor to be fired at the specified time by posting
     *  a pure event to the director, and return the time at which
     *  the specified actor will be fired. If the requested time is in the past
     *  relative to the current time, then it will be increased to match
     *  current time. The caller to this method is responsible for throwing
     *  an exception if it is not acceptable for that time to differ from
     *  the requested time.
     *  <p>
     *  If this director is embedded within another model (the container
     *  has an executive director), and this method is being called between
     *  iterations of this director (which can only occur from a thread
     *  different from the one firing this director), then
     *  this method also delegates a request to the executive director
     *  to fire the container of this director at the requested time.
     *  If the executive director returns a value different from the
     *  specified time, then this method will use that revised value
     *  to schedule the firing of the specified actor, and will return
     *  that value.
     *  <p>
     *  A subtle corner case can occur in a multithreaded execution that
     *  will trigger an exception. In particular, it is possible for this
     *  director to delegate a request to its executive director, and
     *  for that request to be honored before it has posted the event
     *  on its own local event queue. This situation is avoided, for
     *  example, by putting this director within a ThreadedComposite
     *  actor. If this situation occurs, an exception will be thrown.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @return The time at which the actor will be fired, which matches
     *   the argument.
     *  @exception IllegalActionException If the event queue is not ready,
     *   or if a threading error occurs that would result in returning
     *   a time value that is already in the past.
     */
    @Override
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        return fireAt(actor, time, 1);
    }

    /** Request a firing of the given actor at the given model
     *  time with the given microstep. Most actors will not want to use
     *  this method, but if you need for a firing to occur at microstep 0,
     *  then use this method to trigger that firing. Note that any actor
     *  that fires at microstep 0 is expected to not produce any output
     *  events at that firing.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param index The microstep.
     *  @return An instance of Time with value NEGATIVE_INFINITY, or
     *   if there is an executive director, the time at which the
     *   container of this director will next be fired
     *   in response to this request.
     *  @see #fireAtCurrentTime(Actor)
     *  @exception IllegalActionException If there is an executive director
     *   and it throws it. Derived classes may choose to throw this
     *   exception for other reasons.
     */
    @Override
    public Time fireAt(Actor actor, Time time, int index)
            throws IllegalActionException {
        if (_eventQueue == null) {
            throw new IllegalActionException(this,
                    "Calling fireAt() before preinitialize().");
        }
        if (_debugging) {
            _debug("DEDirector: Actor " + actor.getFullName()
                    + " requests refiring at " + time + " with microstep "
                    + index);
        }
        // Normally, we do not need to delegate the request up
        // the hierarchy. This will be done in postfire.
        // We want to keep event queues at all levels in hierarchy
        // as short as possible. So, this pure event is not reported
        // to the higher level in hierarchy immediately. The postfire
        // method of this director will report the next
        // earliest event in the event queue to the higher level.
        // However, if this request is occurring between iterations within
        // an opaque composite actor, then postfire() will not
        // be invoked again to pass this fireAt() request up
        // the hierarchy to the executive director.  We
        // have to pass it up here. We need to do this before
        // enqueueing the local event because the enclosing director
        // may return a different time from the one we specified.
        // We would like to prevent time from advancing while this request
        // is passed up the chain. Otherwise, the enclosing executive
        // director could respond to the fireAt() request before we
        // get our own request posted on the event queue. This has the
        // unfortunate cost of resulting in multiple nested synchronized
        // blocks being possible, which will almost certainly trigger a
        // deadlock. Hence, we cannot do the following within the synchronized
        // block. Instead, we have to check for the error after the synchronized
        // block and throw an exception.
        Time result = time;
        if (_delegateFireAt) {
            if (result.compareTo(getModelTime()) < 0) {
                // NOTE: There is no assurance in a multithreaded situation
                // that time will not advance prior to the posting of
                // this returned time on the local event queue, so
                // an exception can still occur reporting an attempt
                // to post an event in the past.
                result = getModelTime();
            }
            CompositeActor container = (CompositeActor) getContainer();
            if (_debugging) {
                _debug("DEDirector: Requests refiring of container: "
                        + container.getName() + " at time " + time
                        + " with microstep " + index);
            }
            // Enqueue a pure event to fire the container of this director.
            // Note that if the enclosing director is ignoring fireAt(),
            // or if it cannot refire at exactly the requested time,
            // then the following will throw an exception.
            result = fireContainerAt(result, index);
        }
        synchronized (_eventQueue) {
            if (!_delegateFireAt) {
                // If we have not made a request to the executive director,
                // then we can modify time here. If we have, then we can't,
                // but if the time of that request is now in the past,
                // we will throw an exception with an attempt to post
                // an event in the past.
                if (result.compareTo(getModelTime()) < 0) {
                    // NOTE: There is no assurance in a multithreaded situation
                    // that time will not advance prior to the posting of
                    // this returned time on the local event queue, so
                    // an exception can still occur reporting an attempt
                    // to post an event in the past.
                    result = getModelTime();
                }
            }
            if (result.compareTo(getModelTime()) == 0 && index <= _microstep
                    && !_isInitializing) {
                // NOTE: Incrementing the microstep here is wrong if we are in initialize().
                index = _microstep + 1;

                if (index == Integer.MAX_VALUE) {
                    throw new IllegalActionException(
                            this,
                            actor,
                            "Microstep has hit the maximum while scheduling a firing of "
                                    + actor.getFullName()
                                    + ". Perhaps the model has a stuttering Zeno Condition?");
                }
            }

            _enqueueEvent(actor, result, index);

            // Findbugs: Multithreaded correctness,
            // [M M MWN] Mismatched notify() [MWN_MISMATCHED_NOTIFY]
            //    This method calls Object.notify() or Object.notifyAll()
            //    without obviously holding a lock on the object.
            //    Calling notify() or notifyAll() without a lock
            //    held will result in an IllegalMonitorStateException
            //    being thrown.
            // Actually, this seems to be Find Bugs error since the
            // statement is within a synchronized (_eventQueue) block.
            _eventQueue.notifyAll();
        }
        return result;
    }
    
    /** Fire the actor actor at the current model time or, if synchronizeToRealTime
     *  is enabled, fire the actor at the model time that corresponds to the current
     *  real time. This model time is computed by subtracting the model start time
     *  recorded by this director at the beginning of the simulation from the 
     *  system time and multiplying this with the time resolution of the localClock.
     *  @param actor The actor to be fired.
     *  @return The model time the actor will be fired at.
     *  @exception IllegalActionException If thrown while creating a Time object
     *  or while calling fireAt.
     */
    @Override
    public Time fireAtCurrentTime(Actor actor) throws IllegalActionException {
        if (_synchronizeToRealTime) {
            Time modelTimeForCurrentRealTime = new Time(this,
                    (System.currentTimeMillis() - this
                            .getRealStartTimeMillis())
                            * this.localClock.getTimeResolution());
            return fireAt(actor, modelTimeForCurrentRealTime);
        } else {
            // NOTE: We do not need to override the functionality of 
            // fireAtCurrentTime(Actor) because in
            // the base class it does the right thing. In particular, it attempts
            // to fire at the time returned by getModelTime(), but if by the time
            // it enters the synchronized block that time is in the past, it
            // adjusts the time to match current time. This is exactly what
            // fireAtCurrentTime(Actor) should do.
            return super.fireAtCurrentTime(actor);
        }
    }

    /** Schedule an actor to be fired in the specified time relative to
     *  the current model time.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If the specified time contains
     *  a negative time value, or event queue is not ready.
     */
    public void fireAtRelativeTime(Actor actor, Time time)
            throws IllegalActionException {
        fireAt(actor, time.add(getModelTime()));
    }

    /** Return a causality interface for the composite actor that
     *  contains this director. This base class returns an
     *  instance of {@link CausalityInterfaceForComposites}, but
     *  subclasses may override this to return a domain-specific
     *  causality interface.
     *  @return A representation of the dependencies between input ports
     *   and output ports of the container.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        return new DECausalityInterface((Actor) getContainer(),
                defaultDependency());
    }

    /** Get the current microstep.
     *  @return microstep of the current time.
     *  @see #getIndex()
     *  @see #setIndex(int)
     *  @see ptolemy.actor.SuperdenseTimeDirector
     */
    public int getMicrostep() {
        return _microstep;
    }

    /** Return the event queue. Note that this method is not synchronized.
     *  Any further accesses to this event queue needs synchronization.
     *  @return The event queue.
     */
    /* Appears to not be used, and seems dangerous anyway
     * since accesses to the event queue need to be synchronized.
    public DEEventQueue getEventQueue() {
        return _eventQueue;
    }
     */

    /** Return the timestamp of the next event in the queue.
     *  The next iteration time, for example, is used to estimate the
     *  run-ahead time, when a continuous time composite actor is embedded
     *  in a DE model. If there is no event in the event queue, a positive
     *  infinity object is returned.
     *  @return The time stamp of the next event in the event queue.
     * @exception IllegalActionException If Time object cannot be created.
     */
    @Override
    public Time getModelNextIterationTime() throws IllegalActionException {
        Time aFutureTime = Time.POSITIVE_INFINITY;

        synchronized (_eventQueue) {
            // Record the model next iteration time as the tag of the the earliest
            // event in the queue.
            if (_eventQueue.size() > 0) {
                aFutureTime = _eventQueue.get().timeStamp();
            }

            // Iterate the event queue to find the earliest event with a bigger tag
            // ((either timestamp or microstop). If such an event exists,
            // use its time as the model next iteration time. If no such event
            // exists, it means that the model next iteration time still needs to
            // be resolved. In other words, the model next iteration time is
            // just the current time.
            Object[] events = _eventQueue.toArray();
            for (Object event2 : events) {
                DEEvent event = (DEEvent) event2;
                Time eventTime = event.timeStamp();
                int eventMicrostep = event.microstep();
                if (eventTime.compareTo(getModelTime()) > 0
                        || eventMicrostep > _microstep) {
                    aFutureTime = eventTime;
                    break;
                }
            }
        }
        // Go through hierarchy to find the minimum step.
        Director executiveDirector = ((CompositeActor) getContainer())
                .getExecutiveDirector();
        // Some composites, such as RunCompositeActor want to be treated
        // as if they are at the top level even though they have an executive
        // director, so be sure to check _isTopLevel().
        if (executiveDirector != null && !_isTopLevel()) {

            Time aFutureTimeOfUpperLevel = localClock
                    .getLocalTimeForEnvironmentTime(executiveDirector
                            .getModelNextIterationTime());

            if (aFutureTime.compareTo(aFutureTimeOfUpperLevel) > 0) {
                aFutureTime = aFutureTimeOfUpperLevel;
            }
        }

        return aFutureTime;
    }

    /** Return the timestamp of the next event in the queue. This is
     *  different from getModelNextIterationTime as it just considers
     *  the local event queue and not that of directors higher up in
     *  the model hierarchy.
     *  @return The timestamp of the next event in the local event
     *  queue.
     */
    public Time getNextEventTime() {
        if (_eventQueue.size() == 0) {
            return null;
        }
        return _eventQueue.get().timeStamp();
    }

    /** Return the system time at which the model begins executing.
     *  That is, the system time (in milliseconds) when the initialize()
     *  method of the director is called.
     *  The time is in the form of milliseconds counting
     *  from 1/1/1970 (UTC).
     *  @return The real start time of the model.
     */
    public long getRealStartTimeMillis() {
        return _realStartTime;
    }

    /** Return the start time parameter value.
     *  <p>
     *  When the start time is too big, the double representation loses
     *  the specified time resolution. To avoid this loss, use the
     *  {@link #getModelStartTime()} instead.</p>
     *  @return the start time.
     *  @exception IllegalActionException If the enclosing director throws it.
     *  @deprecated As Ptolemy II 4.1, use {@link #getModelStartTime()}
     *  instead.
     */
    @Deprecated
    @Override
    public final double getStartTime() throws IllegalActionException {
        // This method is final for performance reason.
        return getModelStartTime().getDoubleValue();
    }

    /** Return the stop time.
     *  <p>
     *  When the stop time is too big, the double representation loses
     *  the specified time resolution. To avoid this loss, use the
     *  {@link #getModelStopTime()} instead.</p>
     *  @return the stop time.
     *  @exception IllegalActionException If getModelStopTime() throws it.
     *  @deprecated As Ptolemy II 4.1, use {@link #getModelStopTime()}
     *  instead.
     */
    @Deprecated
    @Override
    public final double getStopTime() throws IllegalActionException {
        // This method is final for performance reason.
        return getModelStopTime().getDoubleValue();
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

    /** Initialize all the contained actors by invoke the initialize() method
     *  of the super class. If any events are generated during the
     *  initialization, and the container is not at the top level, request a
     *  refiring.
     *  <p>
     *  The real start time of the model is recorded when this method
     *  is called. This method is <i>not</i> synchronized on the workspace,
     *  so the caller should be.</p>
     *
     *  @exception IllegalActionException If the initialize() method of
     *   the super class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _isInitializing = true;

        synchronized (_eventQueue) {
            _eventQueue.clear();

            // Reset the following private variables.
            _disabledActors = null;
            _exceedStopTime = false;
            _noMoreActorsToFire = false;
            _realStartTime = System.currentTimeMillis();
            _stopFireRequested = false;

            // Initialize the microstep to zero, even though
            // DE normally wants to run with microstep 1 or higher.
            // During initialization, some contained actors will request
            // firings. One of those might be a Continuous subsystem,
            // which will explicitly request a firing at microstep 0.
            // Others will have their requests automatically set
            // to microstep 1. Thus, with normal DE-only models,
            // the only events in the event queue after initialization
            // will all have microstep 1, and hence that is where the
            // simulation will start.
            _microstep = 0;
            // This could be getting re-initialized during execution
            // (e.g., if we are inside a modal model), in which case,
            // if the enclosing director is a superdense time director,
            // we should initialize to its microstep, not to our own.
            // NOTE: Some (weird) directors pretend they are not embedded even
            // if they are (e.g. in Ptides), so we call _isEmbedded() to give
            // the subclass the option of pretending it is not embedded.
            if (isEmbedded()) {
                Nameable container = getContainer();
                if (container instanceof CompositeActor) {
                    Director executiveDirector = ((CompositeActor) container)
                            .getExecutiveDirector();
                    // Some composites, such as RunCompositeActor want to be treated
                    // as if they are at the top level even though they have an executive
                    // director, so be sure to check _isTopLevel().
                    if (executiveDirector instanceof SuperdenseTimeDirector
                            && !_isTopLevel()) {
                        _microstep = ((SuperdenseTimeDirector) executiveDirector)
                                .getIndex();
                    }
                }
            }
            super.initialize();

            // Register the stop time as an event such that the model is
            // guaranteed to stop at that time. This event also serves as
            // a guideline for an embedded Continuous model to know how much
            // further to integrate into future. But only do this if the
            // stop time is finite.
            Time stopTime = getModelStopTime();
            if (!stopTime.isPositiveInfinite()) {
                fireAt((Actor) getContainer(), stopTime, 1);
            }

            if (isEmbedded() && !_eventQueue.isEmpty()) {
                // If the event queue is not empty and the container is not at
                // the top level, ask the upper level director in the
                // hierarchy to refire the container at the timestamp of
                // the earliest event of the local event queue.
                // This design allows the upper level director to keep a
                // relatively short event queue.
                _requestFiring();
                // Indicate that fireAt() request should be passed
                // up the chain if they are made before the next iteration.
                _delegateFireAt = true;
            } else {
                _delegateFireAt = false;
            }

            _isInitializing = false;
        }
    }

    /** Indicate that a schedule for the model may no longer be valid.
     *  This forces the actor depths to be recalculated the next time
     *  they are accessed.
     */
    @Override
    public void invalidateSchedule() {
        CompositeActor container = (CompositeActor) getContainer();
        CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) container
                .getCausalityInterface();
        causality.invalidate();
    }

    /** Return the object to use to obtain a mutex lock on this director.
     *  This class overrides the base class to return the event queue.
     *  @return An object to use to obtain a lock on this director.
     */
    @Override
    public Object mutexLockObject() {
        return _eventQueue;
    }

    /** Return a new receiver of the type DEReceiver.
     *  @return A new DEReceiver.
     */
    @Override
    public Receiver newReceiver() {
        if (_debugging && _verbose) {
            _debug("Creating a new DE receiver.");
        }

        return new DEReceiver();
    }

    /** Return false if there are no more actors to be fired or the stop()
     *  method has been called. Otherwise, if the director is an embedded
     *  director and the local event queue is not empty, request the executive
     *  director to refire the container of this director at the timestamp of
     *  the first event in the event queue.
     *  @return True If this director will be fired again.
     *  @exception IllegalActionException If the postfire method of the super
     *  class throws it, or the stopWhenQueueIsEmpty parameter does not contain
     *  a valid token, or refiring can not be requested.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();

        // If any output ports still have tokens to transfer,
        // request a refiring at the current time.
        CompositeActor container = (CompositeActor) getContainer();
        Iterator<IOPort> outports = container.outputPortList().iterator();
        boolean moreOutputsToTransfer = false;
        while (outports.hasNext() && !moreOutputsToTransfer) {
            IOPort outport = outports.next();
            for (int i = 0; i < outport.getWidthInside(); i++) {
                if (outport.hasNewTokenInside(i)) {
                    moreOutputsToTransfer = true;
                    break;
                }
            }
        }

        // Reset the microstep to zero if the next event is
        // in the future.
        synchronized (_eventQueue) {
            if (!_eventQueue.isEmpty() && !moreOutputsToTransfer) {
                DEEvent next = _eventQueue.get();
                if (next.timeStamp().compareTo(getModelTime()) > 0) {
                    _microstep = 0;
                }
            }
            boolean stop = ((BooleanToken) stopWhenQueueIsEmpty.getToken())
                    .booleanValue();

            // Request refiring and/or stop the model.
            // There are two conditions to stop the model.
            // 1. There are no more actors to be fired (i.e. event queue is
            // empty), and either of the following conditions is satisfied:
            //     a. the stopWhenQueueIsEmpty parameter is set to true.
            //     b. the current model time equals the model stop time.
            // 2. The event queue is not empty, but the current time exceeds
            // the stop time.
            if (moreOutputsToTransfer) {
                fireContainerAt(getModelTime());
            } else if (_noMoreActorsToFire
                    && (stop || getModelTime().compareTo(getModelStopTime()) == 0)) {
                if (_debugging) {
                    _debug("No more actors to fire and time to stop.");
                }
                _exceedStopTime = true;
                result = false;
            } else if (_exceedStopTime) {
                // If the current time is bigger than the stop time,
                // stop the model execution.
                result = false;
            } else if (isEmbedded() && !_eventQueue.isEmpty()) {
                // If the event queue is not empty and the container is an
                // embedded model, ask the upper level director in the
                // hierarchy to refire the container at the timestamp of the
                // first event of the local event queue.
                // This design allows the upper level director (actually all
                // levels in hierarchy) to keep a relatively short event queue.
                _requestFiring();
            }
        }
        if (isEmbedded()) {
            // Indicate that fireAt() requests should be passed up the
            // hierarchy if they are made before the next iteration.
            _delegateFireAt = true;
        }
        // NOTE: The following commented block enforces that no events with
        // different tags can exist in the same receiver.
        // This is a quite different semantics from the previous designs,
        // and its effects are still under investigation and debate.
        //        // Clear all of the contained actor's input ports.
        //        for (Iterator actors = ((CompositeActor)getContainer())
        //                .entityList(Actor.class).iterator();
        //                actors.hasNext();) {
        //            Entity actor = (Entity)actors.next();
        //            Iterator ports = actor.portList().iterator();
        //            while (ports.hasNext()) {
        //                IOPort port = (IOPort)ports.next();
        //                if (port.isInput()) {
        //                    // Clear all receivers.
        //                    Receiver[][] receivers = port.getReceivers();
        //                    if (receivers == null) {
        //                        throw new InternalErrorException(this, null,
        //                                "port.getReceivers() returned null! "
        //                                + "This should never happen. "
        //                                + "port was '" + port + "'");
        //                    }
        //                    for (int i = 0; i < receivers.length; i++) {
        //                        Receiver[] receivers2 = receivers[i];
        //                        for (int j = 0; j < receivers2.length; j++) {
        //                            receivers2[j].clear();
        //                        }
        //                    }
        //                }
        //            }
        //        }
        return result;
    }

    /** Set the model time to the outside time if this director is
     *  not at the top level. Check the time of the next event to decide
     *  whether to fire. Return true if there are inputs to this composite
     *  actor, or the time of the next event is equal to the current model
     *  time. Otherwise, return false.
     *  <p>
     *  Note that microsteps are not synchronized.
     *  </p><p>
     *  Throw an exception if the current model time is greater than the next
     *  event time.
     *  @return True if the composite actor is ready to fire.
     *  @exception IllegalActionException If there is a missed event,
     *  or the prefire method of the super class throws it, or can not
     *  query the tokens of the input ports of the container of this
     *  director.</p>
     */
    @Override
    public boolean prefire() throws IllegalActionException {

        // NOTE: super.prefire() is not of much use, because we want
        // to set current time adjusted for accumulated suspend time.

        if (_debugging) {
            _debug("DEDirector: Called prefire().");
        }

        // The following call sets the local time to match
        // the environment time (with drift and offset taken into account),
        // but it does not set the microstep. We do that below.
        super.prefire();

        // Have to also do this for the microstep.
        if (isEmbedded()) {
            Nameable container = getContainer();
            if (container instanceof CompositeActor) {
                Director executiveDirector = ((CompositeActor) container)
                        .getExecutiveDirector();
                // Some composites, such as RunCompositeActor want to be treated
                // as if they are at the top level even though they have an executive
                // director, so be sure to check _isTopLevel().
                if (executiveDirector instanceof SuperdenseTimeDirector
                        && !_isTopLevel()) {
                    _microstep = ((SuperdenseTimeDirector) executiveDirector)
                            .getIndex();
                }
                if (_debugging) {
                    _debug("DEDirector: Set microstep to " + _microstep);
                }
            }
        }

        // A top-level DE director is always ready to fire.
        if (_isTopLevel()) {
            if (_debugging) {
                _debug("Prefire returns true.");
            }
            return true;
        }

        // If embedded, check the timestamp of the next event to decide
        // whether this director is ready to fire.
        synchronized (_eventQueue) {
            Time modelTime = getModelTime();
            Time nextEventTime = Time.POSITIVE_INFINITY;

            if (!_eventQueue.isEmpty()) {
                DEEvent nextEvent = _eventQueue.get();
                nextEventTime = nextEvent.timeStamp();
            }

            // If the model time is larger (later) than the first event
            // in the queue, then
            // catch up with the current model time by discarding
            // the old events. Do not, however, discard events whose
            // index but not time has passed.
            while (modelTime.compareTo(nextEventTime) > 0) {
                DEEvent skippedEvent = _eventQueue.take();
                if (_debugging) {
                    _debug("Skipping event at time (" + nextEventTime
                            + ") destined for actor "
                            + skippedEvent.actor().getFullName());
                }
                if (!_eventQueue.isEmpty()) {
                    DEEvent nextEvent = _eventQueue.get();
                    nextEventTime = nextEvent.timeStamp();
                } else {
                    nextEventTime = Time.POSITIVE_INFINITY;
                }
            }
        }
        // NOTE: An alternative would be to throw an exception. This means that the
        // enclosing director is breaking the fireAt() contract, since
        // it presumably returned a value indicating it would do the
        // firing and then failed to do it. However, we don't do that
        // because the old style of modal models (in the fsm domain)
        // will result in this exception being thrown. The
        // code to do that is below.
        /*
        if (modelTime.compareTo(nextEventTime) > 0) {
            throw new IllegalActionException(this,
                    "DEDirector expected to be fired at time "
                    + nextEventTime
                    + " but instead is being fired at time "
                    + modelTime);
        }
         */

        // If model time is strictly less than the next event time,
        // then there are no events on the event queue with this
        // model time, and hence, if there are also no input events,
        // then there is nothing to do, and we can return false.
        /* The following is no longer correct.
         * We need to ensure that postfire() is invoked so that fireAt()
         * gets called. Although fireAt() should have already been called
         * for pending events in the event queue, it may need to be done again
         * because we may have been suspending when the resulting fire occurred.
         * EAL 9/18/09
        if (!nextEventTime.equals(modelTime)) {
            // If the event timestamp is greater than the model timestamp,
            // we check if there's any external input.
            Iterator<?> inputPorts = container.inputPortList().iterator();
            boolean hasInput = false;

            while (inputPorts.hasNext() && !hasInput) {
                IOPort port = (IOPort) inputPorts.next();

                for (int i = 0; i < port.getWidth(); i++) {
                    if (port.hasToken(i)) {
                        hasInput = true;
                        break;
                    }
                }
            }

            if (!hasInput) {
                // If there is no internal event, it is not the correct
                // time to fire.
                // NOTE: This may happen because the container is statically
                // scheduled by its director to fire at this time.
                // For example, a DE model in a Giotto model.
                result = false;
            }
        }
         */

        if (_debugging) {
            _debug("Prefire returns true.");
        }
        // Indicate that fireAt requests can be handled locally because
        // we are within an iteration.
        _delegateFireAt = false;
        return true;
    }

    /** Set the current timestamp to the model start time, invoke the
     *  preinitialize() methods of all actors deeply contained by the
     *  container.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. Actors cannot produce output data in their preinitialize()
     *  methods. If initial events are needed, e.g. pure events for source
     *  actor, the actors should do so in their initialize() method.
     *  </p><p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.</p>
     *
     *  @exception IllegalActionException If the preinitialize() method of the
     *  container or one of the deeply contained actors throws it, or the
     *  parameters, minBinCount, binCountFactor, and isCQAdaptive, do not have
     *  valid tokens.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // Initialize an event queue.
        _eventQueue = new DECQEventQueue(
                ((IntToken) minBinCount.getToken()).intValue(),
                ((IntToken) binCountFactor.getToken()).intValue(),
                ((BooleanToken) isCQAdaptive.getToken()).booleanValue());

        // Add debug listeners.
        if (_debugListeners != null) {
            Iterator<?> listeners = _debugListeners.iterator();

            while (listeners.hasNext()) {
                DebugListener listener = (DebugListener) listeners.next();
                _eventQueue.addDebugListener(listener);
            }
        }

        // Call the preinitialize method of the super class.
        super.preinitialize();

        if (getContainer() instanceof CompositeActor) {
            // Tests in ptolemy/configs/test expand the configuration which
            // results in the ModelDirectory getting expanded.  In the
            // ModelDirectory, the container might be an EntityLibrary.
            // The Ptides director is calling preinitialize() in
            // attributeChanged(), which means that this code gets called.

            // Do this here so that performance measurements
            // clearly indicate that the cost is in static analysis
            // done in preinitialize.
            CompositeActor container = (CompositeActor) getContainer();
            CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) container
                    .getCausalityInterface();
            causality.checkForCycles();
        }

        _actorsFinished = new ArrayList();

        if (_debugging && _verbose) {
            _debug("## Depths assigned to actors and ports:");
            _debug(describePriorities());
        }
        _issueExecutionAspectWarning();
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    @Override
    public void removeDebugListener(DebugListener listener) {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _eventQueue.removeDebugListener(listener);
            }
        }

        super.removeDebugListener(listener);
    }

    /** Resume the execution of an actor that was previously blocked because
     *  it didn't have all the resources it needed for execution. This method
     *  puts an event into the queue for the current time.
     *
     *  @param actor The actor that resumes execution.
     *  @exception IllegalActionException Not thrown here but in derived classes.
     */
    @Override
    public void resumeActor(NamedObj actor) throws IllegalActionException {
        List<DEEvent> events = _actorsInExecution.get(actor);
        ActorExecutionAspect aspect = getExecutionAspect(actor);
        if (aspect == null) {
            throw new IllegalActionException(this, "Cannot resume actor "
                    + actor.getName() + " because aspect cannot be found.");
        }
        NamedObj container = aspect.getContainer();
        if (container == null) {
            throw new IllegalActionException(this,
                    "Cannot get container of actor " + actor.getName());
        }
        Director director = ((CompositeActor) container).getDirector();
        if (director == null) {
            throw new IllegalActionException(this,
                    "Cannot get director of container " + container.getName()
                            + " of actor " + actor.getName());
        }

        Time time = director.getModelTime();
        DEEvent event = events.get(0);
        events.remove(event);
        _actorsInExecution.put((Actor) actor, events);

        if (event.ioPort() != null) {
            _enqueueTriggerEvent(event.ioPort(), time);
        } else {
            _enqueueEvent((Actor) actor, time, 1);
        }
        fireContainerAt(time);
        if (_actorsFinished == null) {
            _actorsFinished = new ArrayList();
        }
        _actorsFinished.add((Actor) actor);
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

    /** Request the execution of the current iteration to stop.
     *  This is similar to stopFire(), except that the current iteration
     *  is not allowed to complete.  This is useful if there is actor
     *  in the model that has a bug where it fails to consume inputs.
     *  An iteration will never terminate if such an actor receives
     *  an event.
     *  If the director is paused waiting for events to appear in the
     *  event queue, then it stops waiting, and calls stopFire() for all actors
     *  that are deeply contained by the container of this director.
     */
    @Override
    public void stop() {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _stopRequested = true;
                _eventQueue.notifyAll();
            }
        }

        super.stop();
    }

    /** Request the execution of the current iteration to complete.
     *  If the director is paused waiting for events to appear in the
     *  event queue, then it stops waiting,
     *  and calls stopFire() for all actors
     *  that are deeply contained by the container of this director.
     */
    @Override
    public void stopFire() {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _stopFireRequested = true;
                _eventQueue.notifyAll();
            }
        }

        super.stopFire();
    }

    // FIXME: it is questionable whether the multirate FSMActor and FSMDirector
    // should be used in DE as the default? I will say NO.

    /** Return an array of suggested directors to use with
     *  ModalModel. Each director is specified by its full class
     *  name.  The first director in the array will be the default
     *  director used by a modal model.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    @Override
    public String[] suggestedModalModelDirectors() {
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[1] = "ptolemy.domains.modal.kernel.MultirateFSMDirector";
        defaultSuggestions[0] = "ptolemy.domains.modal.kernel.FSMDirector";
        return defaultSuggestions;
    }

    /** Transfer data from an input port of the container to the ports
     *  it is connected to on the inside.  This transfers at most one token
     *  on each channel, and overrides the base class to temporarily set
     *  the microstep to match or exceed that of the enclosing director if
     *  the enclosing director implements SuperdenseTimeDirector.
     *  Otherwise, it sets the microstep to match or exceed 1
     *  to ensure that inputs are interpreted as discrete values.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port.
     */
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        int defaultMicrostep = _defaultMicrostep;
        int previousMicrostep = _microstep;
        SuperdenseTimeDirector enclosingDirector = _enclosingSuperdenseTimeDirector();
        if (enclosingDirector != null) {
            defaultMicrostep = enclosingDirector.getIndex();
        }
        if (_microstep < defaultMicrostep) {
            try {
                _microstep = defaultMicrostep;
                return super.transferInputs(port);
            } finally {
                _microstep = previousMicrostep;
            }
        }
        return super.transferInputs(port);
    }

    // NOTE: We used to override transferOutputs
    // to transfer ALL output tokens at boundary of
    // hierarchy to outside. See de/test/auto/transferInputsandOutputs.xml.
    // However, the right thing to do is to request a refiring at the current
    // if outputs remain to be transferred. So that's what we do now.

    /** Invoke the wrapup method of the super class. Reset the private
     *  state variables.
     *  @exception IllegalActionException If the wrapup() method of
     *  one of the associated actors throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _disabledActors = null;
        synchronized (_eventQueue) {
            _eventQueue.clear();
        }
        _noMoreActorsToFire = false;
        _microstep = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Perform book keeping procedures after an actor firing.
     *  In this base class, do nothing.
     *  @exception IllegalActionException Not thrown in this base class.
     *  Derived classes may throw it if book keeping procedures are not
     *  successful.
     */
    protected void _actorFired() throws IllegalActionException {
    }

    /** Enforces a firing of a DE director only handles events with the
     *  same tag. Checks what is the model time of the earliest event
     *  in the event queue.
     *  @return true if the earliest event in the event queue is at the
     *  same model time as the event that was just processed. Else if
     *  that event's timestamp is in the future, return false.
     *  @exception IllegalActionException If model time is set backwards.
     */
    protected boolean _checkForNextEvent() throws IllegalActionException {
        // The following code enforces that a firing of a
        // DE director only handles events with the same tag.
        // If the earliest event in the event queue is in the future,
        // this code terminates the current iteration.
        // This code is applied on both embedded and top-level directors.
        synchronized (_eventQueue) {
            if (!_eventQueue.isEmpty()) {
                DEEvent next = _eventQueue.get();

                if (next.timeStamp().compareTo(getModelTime()) > 0) {
                    // If the next event is in the future time,
                    // jump out of the big while loop in fire() and
                    // proceed to postfire().
                    return false;
                } else if (next.microstep() > _microstep) {
                    // If the next event has a bigger microstep,
                    // jump out of the big while loop in fire() and
                    // proceed to postfire().
                    return false;
                } else if (next.timeStamp().compareTo(getModelTime()) < 0
                        || next.microstep() < _microstep) {
                    throw new IllegalActionException(
                            "The tag of the next event (" + next.timeStamp()
                            + "." + next.microstep()
                            + ") can not be less than"
                            + " the current tag (" + getModelTime()
                            + "." + _microstep + ") !");
                } else {
                    // The next event has the same tag as the current tag,
                    // indicating that at least one actor is going to be
                    // fired at the current iteration.
                    // Continue the current iteration.
                }
            }
        }
        return true;
    }

    /** Disable the specified actor.  All events destined to this actor
     *  will be ignored. If the argument is null, then do nothing.
     *  @param actor The actor to disable.
     */
    protected void _disableActor(Actor actor) {
        if (actor != null) {
            if (_debugging) {
                _debug("Actor ", ((Nameable) actor).getName(), " is disabled.");
            }

            if (_disabledActors == null) {
                _disabledActors = new HashSet<Actor>();
            }

            _disabledActors.add(actor);
        }
    }

    /** Put a pure event into the event queue to schedule the given actor to
     *  fire at the specified timestamp.
     *  <p>
     *  The default microstep for the queued event is equal to one,
     *  unless the time is equal to the current time, where the microstep
     *  will be the current microstep plus one.
     *  </p><p>
     *  The depth for the queued event is the minimum of the depths of
     *  all the ports of the destination actor.
     *  </p><p>
     *  If there is no event queue or the given actor is disabled, then
     *  this method does nothing.</p>
     *
     *  @param actor The actor to be fired.
     *  @param time The timestamp of the event.
     *  @param defaultMicrostep If the requested firing time is in the future,
     *   then use this defaultMicrostep for the microstep.
     *  @exception IllegalActionException If the time argument is less than
     *  the current model time, or the depth of the actor has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected void _enqueueEvent(Actor actor, Time time, int defaultMicrostep)
            throws IllegalActionException {
        if (_eventQueue == null || _disabledActors != null
                && _disabledActors.contains(actor)) {
            return;
        }

        // Adjust the microstep if it is less than or equal to the current
        // microstep.
        int microstep = defaultMicrostep;
        if (time.compareTo(getModelTime()) == 0 && microstep <= _microstep) {
            // If during initialization, do not increase the microstep.
            // This is based on the assumption that an actor only requests
            // one firing during initialization. In fact, if an actor requests
            // several firings at the same time,
            // only the first request will be granted.
            if (!_isInitializing) {
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
        } else if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(actor,
                    "Attempt to queue an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + time);
        }

        int depth = _getDepthOfActor(actor);

        if (_debugging) {
            _debug("DEDirector: enqueue a pure event: ",
                    ((NamedObj) actor).getName(), "time = " + time
                    + " microstep = " + microstep + " depth = " + depth);
        }

        DEEvent newEvent = new DEEvent(actor, time, microstep, depth);
        synchronized (_eventQueue) {
            _eventQueue.put(newEvent);
        }
    }

    /** Put a trigger event into the event queue. A trigger event is
     *  an event destined for the specified port that will convey
     *  data to that port at the current time and microstep.
     *  The depth for the queued event is the
     *  depth of the destination IO port. The microstep of
     *  the enqueued event will be the greater of the current
     *  microstep and 1. That is, an event destined for a port
     *  is never queued with microstep less than 1.
     *  <p>
     *  If the event queue is not ready or the actor containing the destination
     *  port is disabled, do nothing.
     *
     *  @param ioPort The destination IO port.
     *  @exception IllegalActionException If the time argument is not the
     *  current time, or the depth of the given IO port has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected void _enqueueTriggerEvent(IOPort ioPort)
            throws IllegalActionException {
        _enqueueTriggerEvent(ioPort, localClock.getLocalTime());
    }

    /** Put a trigger event into the event queue with a timestamp that can be
     *  different from the current model time.
     *  Only resource schedulers can enqueue trigger events with future timestamps.
     *  @param ioPort The destination IO port.
     *  @param time The timestamp of the new event.
     *  @exception IllegalActionException If the time argument is not the
     *  current time, or the depth of the given IO port has not be calculated,
     *  or the new event can not be enqueued.
     */
    private void _enqueueTriggerEvent(IOPort ioPort, Time time)
            throws IllegalActionException {
        Actor actor = (Actor) ioPort.getContainer();
        if (_eventQueue == null || _disabledActors != null
                && _disabledActors.contains(actor)) {
            return;
        }

        /* NOTE: We would like to throw an exception if the microstep is
         * zero, but this breaks models with CT inside DE.
         * The CTDirector does not have a notion of superdense time.
         * Ideally, we could detect that is coming from a submodel that
         * does not implement SuperdenseTimeDirector, and hence doesn't
         * know any better.
         * Unfortunately, it is rather difficult to determine where
         * the event originated, since it could have come from arbitrarily
         * deep in the hierarchy. At a minimum, this would create
         * a dependency on domains/modal.
         */
        if (_microstep < 1 && _enforceMicrostepSemantics) {
            throw new IllegalActionException(
                    this,
                    ioPort.getContainer(),
                    "Received a non-discrete event at port "
                            + ioPort.getName()
                            + " of actor "
                            + ioPort.getContainer().getName()
                            + ". Discrete events are required to have microstep greater than zero,"
                            + " but this one has microstep "
                            + _microstep
                            + ". Perhaps a Continuous submodel is sending a continuous rather than"
                            + " discrete signal?");
        }
        int depth = _getDepthOfIOPort(ioPort);

        int microstep = _microstep;
        if (microstep < 1) {
            microstep = 1;
        }

        if (_aspectsPresent) {
            if (_aspectForActor.get(actor) != null
                    && _aspectForActor.get(actor).isWaitingForResource(actor)) {
                Object[] eventArray = _eventQueue.toArray();
                for (Object object : eventArray) {
                    DEEvent event = (DEEvent) object;
                    if (event.actor().equals(actor)) {
                        if (event.timeStamp().compareTo(time) == 0
                                && event.microstep() == 1) {
                            microstep = microstep + 1;
                        } else if (event.timeStamp().compareTo(time) < 0) {
                            time = event.timeStamp();
                            microstep = microstep + 1;
                        }
                    }
                }
            }
        }

        if (_debugging) {
            _debug("enqueue a trigger event for ",
                    ((NamedObj) actor).getName(), " time = " + time
                    + " microstep = " + microstep + " depth = " + depth);
        }

        // Register this trigger event.
        DEEvent newEvent = new DEEvent(ioPort, time, microstep, depth);
        synchronized (_eventQueue) {
            _eventQueue.put(newEvent);
        }
    }

    /** Advance the current model tag to that of the earliest event in
     *  the event queue, and fire all actors that have requested or
     *  are triggered to be fired at the current tag. If
     *  <i>synchronizeToRealTime</i> is true, then before firing, wait
     *  until real time matches or exceeds the timestamp of the
     *  event. Note that the default unit for time is seconds.
     *  <p>
     *  Each actor is fired repeatedly (prefire(), fire()),
     *  until either it has no more input tokens, or its prefire() method
     *  returns false. Note that if the actor fails to consume its
     *  inputs, then this can result in an infinite loop.
     *  Each actor that is fired is then postfired once at the
     *  conclusion of the iteration.
     *  </p><p>
     *  If there are no events in the event queue, then the behavior
     *  depends on the <i>stopWhenQueueIsEmpty</i> parameter. If it is
     *  false, then this thread will stall until events become
     *  available in the event queue. Otherwise, time will advance to
     *  the stop time and the execution will halt.</p>
     *
     *  @return 0 if firing was successful, and the next event in event
     *   queue should be checked for processing;
     *   -1 if there's no actor to fire, and we should not keep firing;
     *   1 if there's no actor to fire, but the next event should be
     *   checked for processing.
     *  @exception IllegalActionException If the firing actor throws it, or
     *   event queue is not ready, or an event is missed, or time is set
     *   backwards.
     */
    protected int _fire() throws IllegalActionException {
        // Find the next actor to be fired.
        Actor actorToFire = _getNextActorToFire();

        // Check whether the actor to be fired is null.
        // -- If the actor to be fired is null,
        // There are two conditions that the actor to be fired
        // can be null.
        if (actorToFire == null) {
            if (_isTopLevel()) {
                // Case 1:
                // If this director is an executive director at
                // the top level, a null actor means that there are
                // no events in the event queue.
                if (_debugging) {
                    _debug("No more events in the event queue.");
                }

                // Setting the following variable to true makes the
                // postfire method return false.
                // Do not do this if _stopFireRequested is true,
                // since there may in fact be actors to fire, but
                // their firing has been deferred.
                if (!_stopFireRequested) {
                    _noMoreActorsToFire = true;
                }
            } else {
                // Case 2:
                // If this director belongs to an opaque composite model,
                // which is not at the top level, the director may be
                // invoked by an update of an external parameter port.
                // Therefore, no actors contained by the composite model
                // need to be fired.
                // NOTE: There may still be events in the event queue
                // of this director that are scheduled for future firings.
                if (_debugging) {
                    _debug("No actor requests to be fired "
                            + "at the current tag.");
                }
            }
            // Nothing more needs to be done in the current iteration.
            // Simply return.
            // Since we are now actually stopping the firing, we can set this false.
            _stopFireRequested = false;
            return -1;
        }

        // NOTE: Here we used to check to see whether
        // the actor to be fired is the container of this director,
        // and if so, return to give the outside domain a chance to react
        // to that event. This strategy assumed that the
        // topological sort would always assign the composite actor the
        // lowest priority, which would guarantee that all the inside actors
        // have fired (reacted to their triggers) before the composite
        // actor is what is returned. However, the priority no longer
        // seems to always be lower. A better strategy is to continue
        // firing until we have exhausted all events with the current
        // tag and microstep.
        if (actorToFire == getContainer()) {
            /* What we used to do (before 5/17/09):
            // Since we are now actually stopping the firing, we can set this false.
            _stopFireRequested = false;
            return;
             */
            return 1;
        }

        if (_debugging) {
            _debug("****** Actor to fire: " + actorToFire.getFullName());
        }

        // Keep firing the actor to be fired until there are no more input
        // tokens available in any of its input ports with the same tag, or its prefire()
        // method returns false.
        boolean refire;

        do {
            refire = false;

            // NOTE: There are enough tests here against the
            // _debugging variable that it makes sense to split
            // into two duplicate versions.
            if (_debugging) {
                // Debugging. Report everything.
                // If the actor to be fired is not contained by the container,
                // it may just be deleted. Put this actor to the
                // list of disabled actors.
                if (!((CompositeEntity) getContainer())
                        .deepContains((NamedObj) actorToFire)) {
                    _debug("Actor no longer under the control of this director. Disabling actor.");
                    _disableActor(actorToFire);
                    break;
                }

                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.BEFORE_PREFIRE));

                if (!actorToFire.prefire()) {
                    _debug("*** Prefire returned false.");
                    break;
                }

                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.AFTER_PREFIRE));

                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.BEFORE_FIRE));

                actorToFire.fire();
                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.AFTER_FIRE));

                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.BEFORE_POSTFIRE));

                if (!actorToFire.postfire()) {
                    _debug("*** Postfire returned false:",
                            ((Nameable) actorToFire).getName());

                    // This actor requests not to be fired again.
                    _disableActor(actorToFire);
                    break;
                }

                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.AFTER_POSTFIRE));
            } else {
                // No debugging.
                // If the actor to be fired is not contained by the container,
                // it may just be deleted. Put this actor to the
                // list of disabled actors.
                if (!((CompositeEntity) getContainer())
                        .deepContains((NamedObj) actorToFire)) {
                    _disableActor(actorToFire);
                    break;
                }
                if (!actorToFire.prefire()) {
                    break;
                }

                actorToFire.fire();
                // NOTE: It is the fact that we postfire actors now that makes
                // this director not comply with the actor abstract semantics.
                // However, it's quite a redesign to make it comply, and the
                // semantics would not be backward compatible. It really needs
                // to be a new director to comply.
                if (!actorToFire.postfire()) {
                    // This actor requests not to be fired again.
                    _disableActor(actorToFire);
                    break;
                }

            }

            // Check all the input ports of the actor to see whether there
            // are more input tokens to be processed.
            // FIXME: This particular situation can only occur if either the
            // actor failed to consume a token, or multiple
            // events with the same destination were queued with the same tag.
            // In theory, both are errors. One possible fix for the latter
            // case would be to requeue the token with a larger microstep.
            // A possible fix for the former (if we can detect it) would
            // be to throw an exception. This would be far better than
            // going into an infinite loop.
            Iterator<?> inputPorts = actorToFire.inputPortList().iterator();

            while (inputPorts.hasNext() && !refire) {
                IOPort port = (IOPort) inputPorts.next();

                // iterate all the channels of the current input port.
                for (int i = 0; i < port.getWidth(); i++) {
                    if (port.hasNewToken(i)) {
                        if (_debugging) {
                            _debug("Port named " + port.getName()
                                    + " still has input on channel " + i
                                    + ". Refire the actor.");
                        }
                        // refire only if can be scheduled.
                        if (!_aspectsPresent
                                || _schedule((NamedObj) actorToFire,
                                        getModelTime())) {
                            refire = true;

                            // Found a channel that has input data,
                            // jump out of the for loop.
                            break;
                        } else if (_aspectsPresent) {
                            if (_actorsInExecution == null) {
                                _actorsInExecution = new HashMap();
                            }
                            List<DEEvent> events = _actorsInExecution
                                    .get(actorToFire);
                            if (events == null) {
                                events = new ArrayList<DEEvent>();
                            }

                            events.add(new DEEvent(port, getModelTime(), 1,
                                    _getDepthOfActor(actorToFire)));
                            _actorsInExecution.put(actorToFire, events);
                        }
                    }
                }
            }
        } while (refire); // close the do {...} while () loop
        // NOTE: On the above, it would be nice to be able to
        // check _stopFireRequested, but this doesn't actually work.
        // In particular, firing an actor may trigger a call to stopFire(),
        // for example if the actor makes a change request, as for example
        // an FSM actor will do.  This will prevent subsequent firings,
        // incorrectly.
        return 0;
    }

    /** Return the depth of an actor.
     *  @param actor An actor whose depth is requested.
     *  @return An integer indicating the depth of the given actor.
     *  @exception IllegalActionException If the actor depth has
     *   not been computed (this should not occur if the ioPort is under the control
     *   of this director).
     */
    protected int _getDepthOfActor(Actor actor) throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) container
                .getCausalityInterface();
        return causality.getDepthOfActor(actor);
    }

    /** Return the depth of an ioPort.
     *  @param ioPort A port whose depth is requested.
     *  @return An integer representing the depth of the specified ioPort.
     *  @exception IllegalActionException If the ioPort does not have
     *   a depth (this should not occur if the ioPort is under the control
     *   of this director).
     */
    protected int _getDepthOfIOPort(IOPort ioPort)
            throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) container
                .getCausalityInterface();
        return causality.getDepthOfPort(ioPort);
    }

    /** Dequeue the events that have the smallest tag from the event queue.
     *  Return their destination actor. Advance the model tag to their tag.
     *  If the timestamp of the smallest tag is greater than the stop time
     *  then return null. If there are no events in the event queue, and
     *  the stopWhenQueueIsEmpty parameter is set to true, then return null.
     *  Both cases will have the effect of stopping the simulation.
     *  <p>
     *  If the stopWhenQueueIsEmpty parameter is false and the queue is empty,
     *  then stall the current thread by calling wait() on the _eventQueue
     *  until there are new events available.  If the synchronizeToRealTime
     *  parameter is true, then this method may suspend the calling thread
     *  by using Object.wait(long) to let elapsed real time catch up with the
     *  current model time.</p>
     *  @return The next actor to be fired, which can be null.
     *  @exception IllegalActionException If event queue is not ready, or
     *  an event is missed, or time is set backwards.
     */
    protected Actor _getNextActorToFire() throws IllegalActionException {
        if (_eventQueue == null) {
            throw new IllegalActionException(
                    "Fire method called before the preinitialize method.");
        }

        Actor actorToFire = null;
        DEEvent lastFoundEvent = null;
        DEEvent nextEvent = null;

        // Keep taking events out until there are no more events that have the
        // same tag and go to the same destination actor, or until the queue is
        // empty, or until a stop is requested.
        // LOOPLABEL::GetNextEvent
        while (!_stopRequested) {
            // Get the next event from the event queue.
            if (_stopWhenQueueIsEmpty) {
                if (_eventQueue.isEmpty()) {
                    // If the event queue is empty,
                    // jump out of the loop: LOOPLABEL::GetNextEvent
                    break;
                }
            }

            if (isEmbedded()) {
                // If the director is not at the top level.
                if (_eventQueue.isEmpty()) {
                    // This could happen if the container simply fires
                    // this composite at times it chooses. Most directors
                    // do this (SDF, SR, Continuous, etc.). It can also
                    // happen if an input is provided to a parameter port
                    // and the container is DE.
                    // In all these cases, no actors inside need to be
                    // fired.
                    break;
                }
                // For an embedded DE director, the following code prevents
                // the director from reacting to future events with bigger
                // time values in their tags.
                // For a top-level DE director, there is no such constraint
                // because the top-level director is responsible to advance
                // simulation by increasing the model tag.
                nextEvent = _eventQueue.get();

                // An embedded director should process events
                // that only happen at the current tag.
                // If the event is in the past, that is an error,
                // because the event should have been consumed in prefire().
                if (nextEvent.timeStamp().compareTo(getModelTime()) < 0) {
                    // missed an event
                    throw new IllegalActionException(
                            "Fire: Missed an event: the next event tag "
                                    + nextEvent.timeStamp() + " :: "
                                    + nextEvent.microstep()
                                    + " is earlier than the current model tag "
                                    + getModelTime() + " :: " + _microstep
                                    + " !");
                }

                // If the event is in the future time, it is ignored
                // and will be processed later. There is some complexity
                // here for backward compatibility with directors that do
                // not support superdense time. If the enclosing director
                // does not support superdense time, then we ignore the
                // microstep. Otherwise, we require the microstep of
                // the event to match the microstep that was set in
                // prefire(), which matches the microstep of the enclosing
                // director.
                boolean microstepMatches = true;
                Nameable container = getContainer();
                if (container instanceof CompositeActor) {
                    Director executiveDirector = ((CompositeActor) container)
                            .getExecutiveDirector();
                    // Some composites, such as RunCompositeActor want to be treated
                    // as if they are at the top level even though they have an executive
                    // director, so be sure to check _isTopLevel().
                    if (executiveDirector instanceof SuperdenseTimeDirector
                            && !_isTopLevel()) {
                        // If the next event microstep in the past (which it should
                        // not be normally), then we will consider it to match.
                        microstepMatches = nextEvent.microstep() <= _microstep;
                    }
                }

                int comparison = nextEvent.timeStamp()
                        .compareTo(getModelTime());
                if (comparison > 0 || comparison == 0 && !microstepMatches) {
                    // reset the next event
                    nextEvent = null;

                    // jump out of the loop: LOOPLABEL::GetNextEvent
                    break;
                }
            } else { // if (!topLevel)
                // If the director is at the top level
                // If the event queue is empty, normally
                // a blocking read is performed on the queue.
                // However, there are two conditions that the blocking
                // read is not performed, which are checked below.
                if (_eventQueue.isEmpty()) {
                    // The two conditions are:
                    // 1. An actor to be fired has been found; or
                    // 2. There are no more events in the event queue,
                    // and the current time is equal to the stop time.
                    if (actorToFire != null
                            || getModelTime().equals(getModelStopTime())) {
                        // jump out of the loop: LOOPLABEL::GetNextEvent
                        break;
                    }
                }

                // Otherwise, if the event queue is empty,
                // a blocking read is performed on the queue.
                // stopFire() needs to also cause this to fall out!
                synchronized (_eventQueue) {
                    while (_eventQueue.isEmpty() && !_stopRequested
                            && !_stopFireRequested) {
                        if (_debugging) {
                            _debug("Queue is empty. Waiting for input events.");
                        }

                        try {
                            // NOTE: Release the read access held
                            // by this thread to prevent deadlocks.
                            // NOTE: If a ChangeRequest has been requested,
                            // then _eventQueue.notifyAll() is called
                            // and stopFire() is called, so we will stop
                            // waiting for events. However,
                            // CompositeActor used to call stopFire() before
                            // queuing the change request, which created the risk
                            // that the below wait() would be terminated by
                            // a notifyAll() on _eventQueue with _stopFireRequested
                            // having been set, but before the change request has
                            // actually been filed.  See CompositeActor.requestChange().
                            // Does this matter? It means that on the next invocation
                            // of the fire() method, we could resume waiting on an empty queue
                            // without having filed the change request. That filing will
                            // no longer succeed in interrupting this wait, since
                            // stopFire() has already been called. Only on the next
                            // instance of change request would the first change
                            // request get a chance to execute.
                            workspace().wait(_eventQueue);
                        } catch (InterruptedException e) {
                            // If the wait is interrupted,
                            // then stop waiting.
                            break;
                        }
                    } // Close the blocking read while loop

                    // To reach this point, either the event queue is not empty,
                    // or _stopRequested or _stopFireRequested is true, or an interrupted exception
                    // happened.
                    if (_eventQueue.isEmpty()) {
                        // Stop is requested or this method is interrupted.
                        // This can occur, for example, if a change has been requested.
                        // jump out of the loop: LOOPLABEL::GetNextEvent
                        return null;
                    }
                    // At least one event is found in the event queue.
                    nextEvent = _eventQueue.get();
                } // Close synchronized block
            }

            // This is the end of the different behaviors of embedded and
            // top-level directors on getting the next event.
            // When this point is reached, the nextEvent can not be null.
            // In the rest of this method, this is not checked any more.

            // If the actorToFire is null, find the destination actor associated
            // with the event just found. Store this event as lastFoundEvent and
            // go back to continue the GetNextEvent loop.
            // Otherwise, check whether the event just found goes to the
            // same actor to be fired. If so, dequeue that event and continue
            // the GetNextEvent loop. Otherwise, jump out of the GetNextEvent
            // loop.
            // TESTIT
            if (actorToFire == null) {
                // If the actorToFire is not set yet,
                // find the actor associated with the event just found,
                // and update the current tag with the event tag.
                Time currentTime;
                int depth = 0;
                try {
                    synchronized (_eventQueue) {
                        lastFoundEvent = _eventQueue.get();
                        currentTime = _consultTimeRegulators(lastFoundEvent
                                .timeStamp());

                        // NOTE: Synchronize to real time here for backward compatibility,
                        // but the preferred way to do this is now to use a
                        // {@link SynchronizeToRealTime} attribute, which implements the
                        //  {@link TimeRegulator} interface.
                        if (_synchronizeToRealTime) {
                            // If synchronized to the real time.
                            while (!_stopRequested && !_stopFireRequested) {
                                lastFoundEvent = _eventQueue.get();
                                currentTime = lastFoundEvent.timeStamp();

                                long elapsedTime = System.currentTimeMillis()
                                        - _realStartTime;

                                // NOTE: We assume that the elapsed time can be
                                // safely cast to a double.  This means that
                                // the DE domain has an upper limit on running
                                // time of Double.MAX_VALUE milliseconds.
                                double elapsedTimeInSeconds = elapsedTime / 1000.0;
                                ptolemy.actor.util.Time elapsed = new ptolemy.actor.util.Time(
                                        this, elapsedTimeInSeconds);
                                if (currentTime.compareTo(elapsed) <= 0) {
                                    break;
                                }

                                // NOTE: We used to do the following, but it had a limitation.
                                // In particular, if any user code also calculated the elapsed
                                // time and then constructed a Time object to post an event
                                // on the event queue, there was no assurance that the quantization
                                // would be the same, and hence it was possible for that event
                                // to be in the past when posted, even if done in the same thread.
                                // To ensure that the comparison of current time against model time
                                // always yields the same result, we have to do the comparison using
                                // the Time class, which is what the event queue does.
                                /*
                                if (currentTime.getDoubleValue() <= elapsedTimeInSeconds) {
                                    break;
                                }*/

                                long timeToWait = (long) (currentTime.subtract(
                                        elapsed).getDoubleValue() * 1000.0);

                                if (timeToWait > 0) {
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
                                        depth = _workspace
                                                .releaseReadPermission();
                                        _eventQueue.wait(timeToWait);
                                    } catch (InterruptedException ex) {
                                        // Continue executing?
                                        // No, because this could be a problem if any
                                        // actor assumes that model time always exceeds
                                        // real time when synchronizeToRealTime is set.
                                        throw new IllegalActionException(
                                                this,
                                                ex,
                                                "Thread interrupted when waiting for"
                                                        + " real time to match model time.");
                                    }
                                }
                            } // while
                            // If stopFire() has been called, then the wait for real
                            // time above was interrupted by a change request. Hence,
                            // real time will not have reached the time of the first
                            // event in the event queue. If we allow this method to
                            // proceed, it will set model time to that event time,
                            // which is in the future. This violates the principle
                            // of synchronize to real time.  Hence, we must return
                            // without processing the event or incrementing time.

                            // NOTE: CompositeActor used to call stopFire() before
                            // queuing the change request, which created the risk
                            // that the above wait() would be terminated by
                            // a notifyAll() on _eventQueue with _stopFireRequested
                            // having been set, but before the change request has
                            // actually been filed.  See CompositeActor.requestChange().
                            // Does this matter? It means that on the next invocation
                            // of the fire() method, we could resume processing the
                            // same event, waiting for real time to elapse, without
                            // having filed the change request. That filing will
                            // no longer succeed in interrupting this wait, since
                            // stopFire() has already been called. Alternatively,
                            // before we get to the wait for real time in the next
                            // firing, the change request could complete and be
                            // executed.
                            if (_stopRequested || _stopFireRequested) {
                                return null;
                            }
                        } // if (_synchronizeToRealTime)
                    } // sync
                } finally {
                    if (depth > 0) {
                        _workspace.reacquireReadPermission(depth);
                    }
                }

                // Consume the earliest event from the queue. The event must be
                // obtained here, since a new event could have been enqueued
                // into the queue while the queue was waiting. Note however
                // that this would usually be an error. Any other thread that
                // posts events in the event queue should do so in a change request,
                // which will not be executed during the above wait.
                // Nonetheless, we are conservative here, and take the earliest
                // event in the event queue.
                synchronized (_eventQueue) {
                    lastFoundEvent = _eventQueue.take();
                    currentTime = lastFoundEvent.timeStamp();
                    actorToFire = lastFoundEvent.actor();

                    // NOTE: The _enqueueEvent method discards the events
                    // for disabled actors.
                    if (_disabledActors != null
                            && _disabledActors.contains(actorToFire)) {
                        // This actor has requested not to be fired again.
                        if (_debugging) {
                            _debug("Skipping disabled actor: ",
                                    ((Nameable) actorToFire).getFullName());
                        }

                        actorToFire = null;

                        // start a new iteration of the loop:
                        // LOOPLABEL::GetNextEvent
                        continue;
                    }

                    // Advance the current time to the event time.
                    // NOTE: This is the only place that the model time changes.
                    setModelTime(currentTime);

                    // Advance the current microstep to the event microstep.
                    _microstep = lastFoundEvent.microstep();
                    if (_debugging) {
                        _debug("Current time is: (" + currentTime + ", "
                                + _microstep + ")");
                    }
                }

                // Exceeding stop time means the current time is strictly
                // bigger than the model stop time.
                if (currentTime.compareTo(getModelStopTime()) > 0) {
                    if (_debugging) {
                        _debug("Current time has passed the stop time.");
                    }

                    _exceedStopTime = true;
                    return null;
                }
            } else { // i.e., actorToFire != null
                // In a previous iteration of this while loop,
                // we have already found an event and the actor to react to it.
                // Check whether the newly found event has the same tag
                // and destination actor. If so, they are
                // handled at the same time. For example, a pure
                // event and a trigger event that go to the same actor.
                if (nextEvent.hasTheSameTagAs(lastFoundEvent)
                        && nextEvent.actor() == actorToFire) {
                    // Consume the event from the queue and discard it.
                    // In theory, there should be no event with the same depth
                    // as well as tag because
                    // the DEEvent class equals() method returns true in this
                    // case, and the CalendarQueue class does not enqueue an
                    // event that is equal to one already on the queue.
                    // Note that the Repeat actor, for one, produces a sequence
                    // of outputs, each of which will have the same microstep.
                    // These reduce to a single event in the event queue.
                    // The DEReceiver in the downstream port, however,
                    // contains multiple tokens. When the one event on
                    // event queue is encountered, then the actor will
                    // be repeatedly fired until it has no more input tokens.
                    // However, there could be events with the same tag
                    // and different depths, e.g. a trigger event and a pure
                    // event going to the same actor.
                    synchronized (_eventQueue) {
                        _eventQueue.take();
                    }
                } else {
                    // Next event has a future tag or a different destination.
                    break;
                }
            }
            if (actorToFire != null && _aspectsPresent) {
                if (_actorsFinished.contains(actorToFire)) {
                    _actorsFinished.remove(actorToFire);
                } else if (!_schedule((NamedObj) actorToFire, getModelTime())) {
                    _nextScheduleTime.get(_aspectForActor.get(actorToFire))
                    .add(getModelTime());
                    if (_actorsInExecution == null) {
                        _actorsInExecution = new HashMap();
                    }
                    List<DEEvent> events = _actorsInExecution.get(actorToFire);
                    if (events == null) {
                        events = new ArrayList<DEEvent>();
                    }
                    events.add(lastFoundEvent);
                    _actorsInExecution.put(actorToFire, events);
                    actorToFire = null;
                }
            }
        } // close the loop: LOOPLABEL::GetNextEvent

        // Note that the actor to be fired can be null.
        return actorToFire;
    }

    /** In DE, a warning is issued when execution aspects are used because
     *  these might change the DE semantics of the execution. In Ptides,
     *  this is not the case.
     * @exception IllegalActionException
     */
    protected void _issueExecutionAspectWarning() throws IllegalActionException {
        if (_executionAspects.size() > 0) {
            boolean _aspectUsed = false;
            if (getContainer() instanceof CompositeActor) {
                for (Object entity : ((CompositeActor) getContainer())
                        .entityList()) {
                    Actor actor = (Actor) entity;
                    if (getExecutionAspect((NamedObj) actor) != null) {
                        _aspectUsed = true;
                        break;
                    }
                }
            }
            if (_aspectUsed) {
                //if (!MessageHandler.yesNoQuestion(
                System.out
                        .println("WARNING: The execution aspects in this model can "
                                + "influence the timing of actors by delaying the \n"
                                + "execution, which can potentially reverse causality. "
                                + "There is no guarantee that actors fire at the \n"
                                + "time they request to be fired. \n"
                                + "Use Ptides for deterministic DE behavior that is "
                                + "not influenced by execution aspects. \n");
                //+ "Continue?")) {
                //stop();
                //}
            }
        }
    }

    /** There are no actor to fire. In this base class, do nothing. Subclasses
     *  may override this method in case there is no actor to fire.
     *  @exception IllegalActionException Not thrown in this base class.
     *  Derived classes may throw it if unable to get the next actuation
     *  event.
     */
    protected void _noActorToFire() throws IllegalActionException {
    }

    /** Schedule an actor for execution on a ExecutionAspect. If the actor can
     *  execute this method returns true. If resources are not available this
     *  method returns false.
     *
     *  @param actor The actor.
     *  @param timestamp The time the actor requests to be scheduled.
     *  @return True if actor was scheduled and can be fired.
     *  @exception IllegalActionException Thrown if parameters cannot be read, actor cannot be
     *   scheduled or container cannot be fired at future time.
     */
    @Override
    protected boolean _schedule(NamedObj actor, Time timestamp)
            throws IllegalActionException {
        boolean schedule = super._schedule(actor, timestamp);
        if (!schedule) {
            ActorExecutionAspect scheduler = getExecutionAspect(actor);
            if (scheduler != null) {
                ((CompositeActor) scheduler.getContainer()).getDirector()
                        .fireAt((Actor) scheduler,
                                getModelTime().add(
                                        _nextScheduleTime.get(scheduler)));
            } else {
                throw new InternalErrorException(this, null,
                        "_getExecutionAspect(" + actor.getFullName()
                                + ") returned null?");
            }
        }
        return schedule;
    }

    /** Actors and their matching events currently in execution and waiting
     *  for resources.
     */
    protected HashMap<Actor, List<DEEvent>> _actorsInExecution;

    /** Actors that just got granted all the resources they needed for
     *  execution but have not actually been fired yet. After the actor
     *  is fired, it is removed from this list.
     */
    protected List<Actor> _actorsFinished;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The set of actors that have returned false in their postfire()
     *  methods. Events destined for these actors are discarded and
     *  the actors are  never fired.
     */
    protected Set<Actor> _disabledActors;

    /** The queue used for sorting events. */
    protected DEEventQueue _eventQueue;

    /** A local boolean variable indicating whether this director is in
     *  initialization phase execution.
     */
    protected boolean _isInitializing = false;

    /** The current microstep. */
    protected int _microstep = 1;

    /** Set to true when it is time to end the execution. */
    protected boolean _noMoreActorsToFire = false;

    /** Flag that stopFire() has been called. */
    protected boolean _stopFireRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the nearest enclosing director that implements
     *  SuperdenseTimeDirector, or null if there
     *  is none.  The enclosing SuperdenseTimeDirector director is a director
     *  above this in the hierarchy, possibly separated by composite
     *  actors with actors that implement the QuasiTransparentDirector
     *  interface, such as FSMDirector or CaseDirector.
     *  @return The enclosing ContinuousDirector, or null if there is none.
     */
    private SuperdenseTimeDirector _enclosingSuperdenseTimeDirector() {
        if (_enclosingSuperdenseTimeDirectorVersion != _workspace.getVersion()) {
            // Update the cache.
            _enclosingSuperdenseTimeDirector = null;
            NamedObj container = getContainer().getContainer();
            while (container != null) {
                if (container instanceof Actor) {
                    Director director = ((Actor) container).getDirector();
                    if (director instanceof SuperdenseTimeDirector) {
                        _enclosingSuperdenseTimeDirector = (SuperdenseTimeDirector) director;
                        break;
                    }
                    if (!(director instanceof QuasiTransparentDirector)) {
                        break;
                    }
                }
                container = container.getContainer();
            }
            _enclosingSuperdenseTimeDirectorVersion = _workspace.getVersion();
        }
        return _enclosingSuperdenseTimeDirector;
    }

    /** initialize parameters. Set all parameters to their default values.
     */
    private void _initParameters() {
        _verbose = true;
        _defaultMicrostep = 1;
        try {
            stopWhenQueueIsEmpty = new Parameter(this, "stopWhenQueueIsEmpty");
            stopWhenQueueIsEmpty.setExpression("true");
            stopWhenQueueIsEmpty.setTypeEquals(BaseType.BOOLEAN);

            synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime");
            synchronizeToRealTime.setExpression("false");
            synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

            isCQAdaptive = new Parameter(this, "isCQAdaptive");
            isCQAdaptive.setExpression("true");
            isCQAdaptive.setTypeEquals(BaseType.BOOLEAN);
            isCQAdaptive.setVisibility(Settable.EXPERT);

            minBinCount = new Parameter(this, "minBinCount");
            minBinCount.setExpression("2");
            minBinCount.setTypeEquals(BaseType.INT);
            minBinCount.setVisibility(Settable.EXPERT);

            binCountFactor = new Parameter(this, "binCountFactor");
            binCountFactor.setExpression("2");
            binCountFactor.setTypeEquals(BaseType.INT);
            binCountFactor.setVisibility(Settable.EXPERT);

            enforceMicrostepSemantics = new Parameter(this,
                    "enforceMicrostepSemantics");
            enforceMicrostepSemantics.setExpression("false");
            enforceMicrostepSemantics.setTypeEquals(BaseType.BOOLEAN);
        } catch (KernelException e) {
            throw new InternalErrorException("Cannot set parameter:\n"
                    + e.getMessage());
        }
    }

    /** Request that the container of this director be refired in some
     *  future time specified by the first event of the local event queue.
     *  This method is used when the director is embedded inside an opaque
     *  composite actor. If the queue is empty, then throw an
     *  IllegalActionException.
     *  @exception IllegalActionException If the queue is empty, or
     *   if the executive director does not respect the fireAt() call.
     */
    protected void _requestFiring() throws IllegalActionException {
        DEEvent nextEvent = null;
        synchronized (_eventQueue) {
            nextEvent = _eventQueue.get();
        }

        if (_debugging) {
            CompositeActor container = (CompositeActor) getContainer();
            _debug("DEDirector: Requests refiring of: " + container.getName()
                    + " at time " + nextEvent.timeStamp());
        }

        // Enqueue a pure event to fire the container of this director.
        fireContainerAt(nextEvent.timeStamp(), nextEvent.microstep());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that calls to fireAt() should be delegated
     *  to the executive director.
     */
    private boolean _delegateFireAt = false;

    /** Cache for the enclosing superdense time director. */
    private SuperdenseTimeDirector _enclosingSuperdenseTimeDirector;

    /** Cache version for the enclosing superdense time director. */
    private long _enclosingSuperdenseTimeDirectorVersion = -1;

    /** Cached value of enforceMicrostepSemantics parameter. */
    private boolean _enforceMicrostepSemantics = false;

    /** Set to true when the time stamp of the token to be dequeue
     *  has exceeded the stopTime.
     */
    private boolean _exceedStopTime = false;

    /** The real time at which the model begins executing. */
    private long _realStartTime = 0;

    /** Decide whether the simulation should be stopped when there's no more
     *  events in the global event queue. By default, its value is 'true',
     *  meaning that the simulation will stop under that circumstances.
     *  Setting it to 'false', instruct the director to wait on the queue
     *  while some other threads might enqueue events in it.
     */
    private boolean _stopWhenQueueIsEmpty = true;

    /** Specify whether the director should wait for elapsed real time to
     *  catch up with model time.
     */
    private boolean _synchronizeToRealTime;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Causality interface for the DEDirector that reflects the limitations
     *  from not having an implementation that computes a fixed point.
     *  Specifically, this causality interface extends CausalityInterfaceForComposites
     *  so that it can do the depth analysis internally, but for the ports
     *  of the container, it declares that all outputs depend on all inputs.
     *  This is necessary to ensure that if a DE opaque composite is fired
     *  only when all inputs at the current microstep are known. See
     *  $PTII/ptolemy/domains/de/test/auto/DEFixedPointLimitation.xml.
     */
    private static class DECausalityInterface extends
    CausalityInterfaceForComposites {
        // FindBugs indicates that this should be a static class.

        /** Construct a causality interface for the specified actor.
         *  @param actor The actor for which this is a causality interface.
         *   This is required to be an instance of CompositeEntity.
         *  @param defaultDependency The default dependency of an output
         *   port on an input port.
         *  @exception IllegalArgumentException If the actor parameter is not
         *  an instance of CompositeEntity.
         */
        public DECausalityInterface(Actor actor, Dependency defaultDependency)
                throws IllegalArgumentException {
            super(actor, defaultDependency);
        }

        /** Return a collection of the ports in this actor that depend on
         *  or are depended on by the specified port. A port X depends
         *  on a port Y if X is an output and Y is an input and
         *  getDependency(X,Y) returns oTimesIdentity()
         *  of the default dependency specified in the constructor.
         *  <p>
         *  This class presumes (but does not check) that the
         *  argument is a port contained by the associated actor.
         *  If the actor is an input, then it returns a collection of
         *  all the outputs. If the actor is output, then it returns
         *  a collection of all the inputs.
         *  @param port The port to find the dependents of.
         *  @return a collection of ports that depend on or are depended on
         *   by the specified port.
         *  @exception IllegalActionException Not thrown in this base class.
         */
        @Override
        public Collection<IOPort> dependentPorts(IOPort port)
                throws IllegalActionException {
            if (port.isOutput()) {
                if (port.isInput()) {
                    // Port is both input and output.
                    HashSet<IOPort> result = new HashSet<IOPort>();
                    result.addAll(_actor.inputPortList());
                    result.addAll(_actor.outputPortList());
                    return result;
                }
                // Port is output and not input.
                return _actor.inputPortList();
            } else if (port.isInput()) {
                // Port is input and not output.
                return _actor.outputPortList();
            } else {
                // Port is neither input nor output.
                return _EMPTY_COLLECTION;
            }
        }

        /** Return a collection of the input ports in this actor that are
         *  in the same equivalence class with the specified input
         *  port. This class returns a collection of all
         *  the input ports of the container actor.
         *  @param input The port to find the equivalence class of.
         *  @return set of the input ports in this actor that are
         *  in an equivalence class with the specified input.
         *  @exception IllegalArgumentException If the argument is not
         *   contained by the associated actor.
         *  @exception IllegalActionException Not thrown in this base class.
         */
        @Override
        public Collection<IOPort> equivalentPorts(IOPort input)
                throws IllegalActionException {
            if (input.getContainer() != _actor || !input.isInput()) {
                throw new IllegalArgumentException(
                        "equivalentPort() called with argument "
                                + input.getFullName()
                                + " that is not an input port for "
                                + _actor.getFullName());
            }
            return _actor.inputPortList();
        }

        /** Return the dependency between the specified input port
         *  and the specified output port.  This class returns
         *  the default dependency if the first port is an input
         *  port owned by this actor and the second one is an output
         *  port owned by this actor. Otherwise, it returns the
         *  additive identity of the dependency.
         *  @param input The input port.
         *  @param output The output port.
         *  @return The dependency between the specified input port
         *   and the specified output port.
         *  @exception IllegalActionException Not thrown in this base class.
         */
        @Override
        public Dependency getDependency(IOPort input, IOPort output)
                throws IllegalActionException {
            if (input.isInput() && input.getContainer() == _actor
                    && output.isOutput() && output.getContainer() == _actor) {
                return _defaultDependency;
            }
            return _defaultDependency.oPlusIdentity();
        }
    }
}
