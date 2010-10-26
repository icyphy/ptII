/* The DE domain director.

 Copyright (c) 1998-2010 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
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
 method. See {@link ptolemy.domains.fsm.kernel.FSMActor}. In order to support
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
 input ports, such as the <i>TimedDelay</i> actor.  Notice that the
 <i>TimedDelay</i> actor breaks a causality loop even if the time
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
 event queue until no more events have the same tag. At that point, and
 only at that point, it invokes postfire() on all the actors that were
 fired during the iteration, and concludes the iteration.
 Note that under this policy, it is possible for an actor to be fired
 multiple times in an iteration prior to invocation of its postfire() method.
 </p><p>
 A model starts from the time specified by <i>startTime</i>, which
 has default value 0.0. The stop time of the execution can be set
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
 being recalculated the next time prefire() is invoked.</p>

 @author Lukito Muliadi, Edward A. Lee, Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
public class DEDirector extends Director implements SuperdenseTimeDirector,
        TimedDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DEDirector() {
        super();
        _initParameters();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public DEDirector(Workspace workspace) {
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

    /** The start time of model. This parameter must contain a
     *  DoubleToken.  The value defaults to 0.0.
     */
    public Parameter startTime;

    /** The stop time of the model.  This parameter must contain a
     *  DoubleToken. The value defaults to Infinity.
     */
    public Parameter stopTime;

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
    public void addDebugListener(DebugListener listener) {
        if (_eventQueue != null) {
            _eventQueue.addDebugListener(listener);
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
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == startTime) {
            double startTimeValue = ((DoubleToken) startTime.getToken())
                    .doubleValue();
            _startTime = new Time(this, startTimeValue);
        } else if (attribute == stopTime) {
            double stopTimeValue = ((DoubleToken) stopTime.getToken())
                    .doubleValue();
            _stopTime = new Time(this, stopTimeValue);
        } else if (attribute == stopWhenQueueIsEmpty) {
            _stopWhenQueueIsEmpty = ((BooleanToken) stopWhenQueueIsEmpty
                    .getToken()).booleanValue();
        } else if (attribute == synchronizeToRealTime) {
            _synchronizeToRealTime = ((BooleanToken) synchronizeToRealTime
                    .getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DEDirector newObject = (DEDirector) super.clone(workspace);
        newObject._disabledActors = null;
        newObject._eventQueue = null;
        newObject._exceedStopTime = false;
        newObject._isInitializing = false;
        newObject._microstep = 0;
        newObject._noMoreActorsToFire = false;
        newObject._realStartTime = 0;
        newObject._stopFireRequested = false;
        return newObject;
    }

    /** Return a boolean dependency representing a model-time delay
     *  of the specified amount.
     *  @param delay A non-negative delay.
     *  @return A boolean dependency representing a delay.
     */
    public Dependency delayDependency(double delay) {
        return BooleanDependency.OTIMES_IDENTITY;
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
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("========= DE director fires at " + getModelTime()
                    + "  with microstep as " + _microstep);
        }

        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A BIG while loop that handles all events with the same tag.
        while (true) {
            int result = _fire();
            assert (result <= 1 && result >= -1);
            if (result == 1) {
                continue;
            } else if (result == -1) {
                _getNextActuationEvent();
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
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        if (_eventQueue == null) {
            throw new IllegalActionException(this,
                    "Calling fireAt() before preinitialize().");
        }
        if (_debugging) {
            _debug("DEDirector: Actor " + actor.getFullName()
                    + " requests refiring at " + time);
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
                _debug("DEDirector: Requests refiring of: "
                        + container.getName() + " at time " + time);
            }
            // Enqueue a pure event to fire the container of this director.
            // Note that if the enclosing director is ignoring fireAt(),
            // or if it cannot refire at exactly the requested time,
            // then the following will throw an exception.
            result = _fireContainerAt(result);
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
            _enqueueEvent(actor, result);

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

    // NOTE: We do not need to override fireAtCurrentTime(Actor) because in
    // the base class it does the right thing. In particular, it attempts
    // to fire at the time returned by getModelTime(), but if by the time
    // it enters the synchronized block that time is in the past, it
    // adjusts the time to match current time. This is exactly what
    // fireAtCurrentTime(Actor) should do.

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
    public DEEventQueue getEventQueue() {
        return _eventQueue;
    }

    /** Return the timestamp of the next event in the queue.
     *  The next iteration time, for example, is used to estimate the
     *  run-ahead time, when a continuous time composite actor is embedded
     *  in a DE model. If there is no event in the event queue, a positive
     *  infinity object is returned.
     *  @return The time stamp of the next event in the event queue.
     */
    public Time getModelNextIterationTime() {
        Time aFutureTime = Time.POSITIVE_INFINITY;

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
        for (int i = 0; i < events.length; i++) {
            DEEvent event = (DEEvent) events[i];
            Time eventTime = event.timeStamp();
            int eventMicrostep = event.microstep();
            if (eventTime.compareTo(getModelTime()) > 0
                    || eventMicrostep > _microstep) {
                aFutureTime = eventTime;
                break;
            }
        }

        // Go through hierarchy to find the minimum step.
        Director executiveDirector = ((CompositeActor) getContainer())
                .getExecutiveDirector();
        if (executiveDirector != null) {
            Time aFutureTimeOfUpperLevel = executiveDirector
                    .getModelNextIterationTime();
            if (aFutureTime.compareTo(aFutureTimeOfUpperLevel) > 0) {
                aFutureTime = aFutureTimeOfUpperLevel;
            }
        }

        return aFutureTime;
    }

    /** Return the start time parameter value.
     *  @return the start time parameter value.
     */
    public final Time getModelStartTime() {
        // This method is final for performance reason.
        return _startTime;
    }

    /** Return the stop time parameter value.
     *  @return the stop time parameter value.
     */
    public final Time getModelStopTime() {
        // This method is final for performance reason.
        return _stopTime;
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
     *  @deprecated As Ptolemy II 4.1, use {@link #getModelStartTime()}
     *  instead.
     */
    public final double getStartTime() {
        // This method is final for performance reason.
        return getModelStartTime().getDoubleValue();
    }

    /** Return the stop time.
     *  <p>
     *  When the stop time is too big, the double representation loses
     *  the specified time resolution. To avoid this loss, use the
     *  {@link #getModelStopTime()} instead.</p>
     *  @return the stop time.
     *  @deprecated As Ptolemy II 4.1, use {@link #getModelStopTime()}
     *  instead.
     */
    public final double getStopTime() {
        // This method is final for performance reason.
        return getModelStopTime().getDoubleValue();
    }

    /** Return a superdense time index for the current time,
     *  where the index is equal to the microstep.
     *  @return A superdense time index.
     *  @see #setIndex(int)
     *  @see ptolemy.actor.SuperdenseTimeDirector
     */
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
    public void initialize() throws IllegalActionException {
        _isInitializing = true;
        _eventQueue.clear();

        // Reset the following private variables.
        _disabledActors = null;
        _exceedStopTime = false;
        _noMoreActorsToFire = false;
        _realStartTime = System.currentTimeMillis();
        _stopFireRequested = false;

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
                if (executiveDirector instanceof SuperdenseTimeDirector) {
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
        if (!_stopTime.isPositiveInfinite()) {
            fireAt((Actor) getContainer(), _stopTime);
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

    /** Indicate that a schedule for the model may no longer be valid.
     *  This forces the actor depths to be recalculated the next time
     *  they are accessed.
     */
    public void invalidateSchedule() {
        CompositeActor container = (CompositeActor) getContainer();
        CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) container
                .getCausalityInterface();
        causality.invalidate();
    }

    /** Return a new receiver of the type DEReceiver.
     *  @return A new DEReceiver.
     */
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
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        boolean stop = ((BooleanToken) stopWhenQueueIsEmpty.getToken())
                .booleanValue();

        // There are two conditions to stop the model.
        // 1. There are no more actors to be fired (i.e. event queue is
        // empty), and either of the following conditions is satisfied:
        //     a. the stopWhenQueueIsEmpty parameter is set to true.
        //     b. the current model time equals the model stop time.
        // 2. The event queue is not empty, but the current time exceeds
        // the stop time.
        if (_noMoreActorsToFire
                && (stop || (getModelTime().compareTo(getModelStopTime()) == 0))) {
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

    /** Set the model timestamp to the outside timestamp if this director is
     *  not at the top level. Check the timestamp of the next event to decide
     *  whether to fire. Return true if there are inputs to this composite
     *  actor, or the timestamp of the next event is equal to the current model
     *  timestamp. Otherwise, return false.
     *  <p>
     *  Note that microsteps are not synchronized.
     *  </p><p>
     *  Throw an exception if the current model time is greater than the next
     *  event timestamp.
     *  @return True if the composite actor is ready to fire.
     *  @exception IllegalActionException If there is a missed event,
     *  or the prefire method of the super class throws it, or can not
     *  query the tokens of the input ports of the container of this
     *  director.</p>
     */
    public boolean prefire() throws IllegalActionException {
        // NOTE: The inside model does not need to have the same
        // microstep as that of the outside model (if it has one.)
        // However, an increment of microstep of the inside model must
        // trigger an increment of microstep of the outside model.
        // Set the model timestamp to the outside timestamp,
        // if this director is not at the top level.
        boolean result = super.prefire();
        // Have to also do this for the microstep.
        if (isEmbedded()) {
            Nameable container = getContainer();
            if (container instanceof CompositeActor) {
                Director executiveDirector = ((CompositeActor) container)
                        .getExecutiveDirector();
                if (executiveDirector instanceof SuperdenseTimeDirector) {
                    _microstep = ((SuperdenseTimeDirector) executiveDirector)
                            .getIndex();
                }
            }
        }

        if (_debugging) {
            _debug("Current time is: (" + getModelTime() + ", " + getIndex()
                    + ")");
        }

        // A top-level DE director is always ready to fire.
        if (_isTopLevel()) {
            if (_debugging) {
                _debug("Prefire returns: " + result);
            }
            return result;
        }

        // If embedded, check the timestamp of the next event to decide
        // whether this director is ready to fire.
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
        if (!nextEventTime.equals(modelTime)) {
            // If the event timestamp is greater than the model timestamp,
            // we check if there's any external input.
            CompositeActor container = (CompositeActor) getContainer();
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

            /* The following is no longer correct.
             * We need to ensure that postfire() is invoked so that fireAt()
             * gets called. Although fireAt() should have already been called
             * for pending events in the event queue, it may need to be done again
             * because we may have been suspending when the resulting fire occurred.
             * EAL 9/18/09
            if (!hasInput) {
                // If there is no internal event, it is not the correct
                // time to fire.
                // NOTE: This may happen because the container is statically
                // scheduled by its director to fire at this time.
                // For example, a DE model in a Giotto model.
                result = false;
            }
            */
        }

        if (_debugging) {
            _debug("Prefire returns: " + result);
        }
        if (result) {
            _delegateFireAt = false;
        } else {
            _delegateFireAt = true;
        }
        return result;
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
    public void preinitialize() throws IllegalActionException {
        // Initialize an event queue.
        _eventQueue = new DECQEventQueue(((IntToken) minBinCount.getToken())
                .intValue(), ((IntToken) binCountFactor.getToken()).intValue(),
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

        if (_debugging && _verbose) {
            _debug("## Depths assigned to actors and ports:");
            _debug(describePriorities());
        }
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    public void removeDebugListener(DebugListener listener) {
        if (_eventQueue != null) {
            _eventQueue.removeDebugListener(listener);
        }

        super.removeDebugListener(listener);
    }

    /** Set the superdense time index. This should only be
     *  called by an enclosing director.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @see #getIndex()
     *  @see ptolemy.actor.SuperdenseTimeDirector
     */
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
    public String[] suggestedModalModelDirectors() {
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[1] = "ptolemy.domains.fsm.kernel.MultirateFSMDirector";
        defaultSuggestions[0] = "ptolemy.domains.fsm.kernel.FSMDirector";
        return defaultSuggestions;
    }

    // NOTE: Why do we need an overridden transferOutputs method?
    // This director needs to transfer ALL output tokens at boundary of
    // hierarchy to outside. Without this overriden method, only one
    // output token is produced. See de/test/auto/transferInputsandOutputs.xml.
    // Do we need an overridden transferInputs method?
    // No. Because the DEDirector will keep firing an actor until it returns
    // false from its prefire() method, meaning that the actor has not enough
    // input tokens.

    /** Override the base class method to transfer all the available
     *  tokens at the boundary output port to outside.
     *  No data remains at the boundary after the model has been fired.
     *  This facilitates building multirate DE models.
     *  The port argument must be an opaque output port. If any channel
     *  of the output port has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        boolean anyWereTransferred = false;
        boolean moreTransfersRemaining = true;

        while (moreTransfersRemaining) {
            moreTransfersRemaining = super.transferOutputs(port);
            anyWereTransferred |= moreTransfersRemaining;
        }

        return anyWereTransferred;
    }

    /** Invoke the wrapup method of the super class. Reset the private
     *  state variables.
     *  @exception IllegalActionException If the wrapup() method of
     *  one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _disabledActors = null;
        _eventQueue.clear();
        _noMoreActorsToFire = false;
        _microstep = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Placeholder for book keeping procedures after actor firing. This
     *  method does nothing in this class, instead it's a placeholder for
     *  subclasses to override.
     */
    protected void _actorFired() {
    }
    
    /** Enforces a firing of a DE director only handles events with the
     *  same tag. Checks what is the model time of the earliest event
     *  in the event queue.
     *  @return true if the earliest event in the event queue is at the
     *  same model time as the event that was just processed. Else if
     *  that event's timestamp is in the future, return false.
     *  @throws IllegalActionException if model time is set backwords.
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

              if ((next.timeStamp().compareTo(getModelTime()) > 0)) {
                  // If the next event is in the future time,
                  // jump out of the big while loop in fire() and
                  // proceed to postfire().
                  // NOTE: we reset the microstep to 0 because it is
                  // the contract that if the event queue has some events
                  // at a time point, the first event must have the
                  // microstep as 0. See the
                  // _enqueueEvent(Actor actor, Time time) method.
                  _microstep = 0;
                  return false;
              } else if (next.microstep() > _microstep) {
                  // If the next event has a bigger microstep,
                  // jump out of the big while loop in fire() and
                  // proceed to postfire().
                  return false;
              } else if ((next.timeStamp().compareTo(getModelTime()) < 0)
                      || (next.microstep() < _microstep)) {
                  throw new IllegalActionException(
                          "The tag of the next event ("
                                  + next.timeStamp() + "."
                                  + next.microstep()
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
     *  The default microstep for the queued event is equal to zero,
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
     *  @exception IllegalActionException If the time argument is less than
     *  the current model time, or the depth of the actor has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected void _enqueueEvent(Actor actor, Time time)
            throws IllegalActionException {
        if ((_eventQueue == null)
                || ((_disabledActors != null) && _disabledActors
                        .contains(actor))) {
            return;
        }

        // Adjust the microstep.
        int microstep = 0;

        if (time.compareTo(getModelTime()) == 0) {
            // If during initialization, do not increase the microstep.
            // This is based on the assumption that an actor only requests
            // one firing during initialization. In fact, if an actor requests
            // several firings at the same time,
            // only the first request will be granted.
            if (_isInitializing) {
                microstep = _microstep;
            } else {
                microstep = _microstep + 1;
            }
        } else if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(actor,
                    "Attempt to queue an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + time);
        }

        int depth = _getDepthOfActor(actor);

        if (_debugging) {
            _debug("enqueue a pure event: ", ((NamedObj) actor).getName(),
                    "time = " + time + " microstep = " + microstep
                            + " depth = " + depth);
        }

        DEEvent newEvent = new DEEvent(actor, time, microstep, depth);
        _eventQueue.put(newEvent);
    }

    /** Put a trigger event into the event queue.
     *  <p>
     *  The trigger event has the same timestamp as that of the director.
     *  The microstep of this event is always equal to the current microstep
     *  of this director. The depth for the queued event is the
     *  depth of the destination IO port.
     *  </p><p>
     *  If the event queue is not ready or the actor contains the destination
     *  port is disabled, do nothing.</p>
     *
     *  @param ioPort The destination IO port.
     *  @exception IllegalActionException If the time argument is not the
     *  current time, or the depth of the given IO port has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected void _enqueueTriggerEvent(IOPort ioPort)
            throws IllegalActionException {
        Actor actor = (Actor) ioPort.getContainer();

        if ((_eventQueue == null)
                || ((_disabledActors != null) && _disabledActors
                        .contains(actor))) {
            return;
        }

        int depth = _getDepthOfIOPort(ioPort);

        if (_debugging) {
            _debug("enqueue a trigger event for ",
                    ((NamedObj) actor).getName(), " time = " + getModelTime()
                            + " microstep = " + _microstep + " depth = "
                            + depth);
        }

        // Register this trigger event.
        DEEvent newEvent = new DEEvent(ioPort, getModelTime(), _microstep,
                depth);
        _eventQueue.put(newEvent);
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
     *  @throws IllegalActionException If the firing actor throws it, or
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
                    if (port.hasToken(i)) {
                        refire = true;

                        // Found a channel that has input data,
                        // jump out of the for loop.
                        break;
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
                if ((nextEvent.timeStamp().compareTo(getModelTime()) < 0)) {
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
                // and will be processed later.
                // Note that it is fine for the new event to have a bigger
                // microstep. This indicates that the embedded director is
                // going to advance microstep.
                // Note that conceptually, the outside and inside DE models
                // share the same microstep and the current design and
                // implementation assures that. However, the embedded DE
                // director does ask for the microstep of the upper level
                // DE director. They keep their own count of their
                // microsteps. The reason for this is to avoid the
                // difficulties caused by passing information across modal
                // model layers.
                if ((nextEvent.timeStamp().compareTo(getModelTime()) > 0)) {
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
                    if ((actorToFire != null)
                            || (getModelTime().equals(getModelStopTime()))) {
                        // jump out of the loop: LOOPLABEL::GetNextEvent
                        break;
                    }
                }

                // Otherwise, if the event queue is empty,
                // a blocking read is performed on the queue.
                // stopFire() needs to also cause this to fall out!
                while (_eventQueue.isEmpty() && !_stopRequested
                        && !_stopFireRequested) {
                    if (_debugging) {
                        _debug("Queue is empty. Waiting for input events.");
                    }

                    Thread.yield();

                    synchronized (_eventQueue) {
                        // Need to check _stopFireRequested again inside
                        // the synchronized block, because it may have changed.
                        if (_eventQueue.isEmpty() && !_stopRequested
                                && !_stopFireRequested) {
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
                        }
                    } // Close synchronized block
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

                if (_synchronizeToRealTime) {
                    // If synchronized to the real time.
                    int depth = 0;
                    try {
                        synchronized (_eventQueue) {
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
                        } // sync
                    } finally {
                        if (depth > 0) {
                            _workspace.reacquireReadPermission(depth);
                        }
                    }
                } // if (_synchronizeToRealTime)

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
                    if ((_disabledActors != null)
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
                    _eventQueue.take();
                } else {
                    // Next event has a future tag or a different destination.
                    break;
                }
            }
        } // close the loop: LOOPLABEL::GetNextEvent

        // Note that the actor to be fired can be null.
        return actorToFire;
    }

    /** Place holder that gets the next actuation event. This method does nothing
     *  here. It's used for the Ptides subclass.
     */
    protected void _getNextActuationEvent() {
    }
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
    protected int _microstep = 0;

    /** Set to true when it is time to end the execution. */
    protected boolean _noMoreActorsToFire = false;

    /** Flag that stopFire() has been called. */
    protected boolean _stopFireRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** initialize parameters. Set all parameters to their default values.
     */
    private void _initParameters() {
        _verbose = true;
        try {
            startTime = new Parameter(this, "startTime");
            startTime.setExpression("0.0");
            startTime.setTypeEquals(BaseType.DOUBLE);

            stopTime = new Parameter(this, "stopTime");
            stopTime.setExpression("Infinity");
            stopTime.setTypeEquals(BaseType.DOUBLE);

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

            timeResolution.setVisibility(Settable.FULL);
            timeResolution.moveToLast();
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
    private void _requestFiring() throws IllegalActionException {
        DEEvent nextEvent = null;
        nextEvent = _eventQueue.get();

        if (_debugging) {
            CompositeActor container = (CompositeActor) getContainer();
            _debug("DEDirector: Requests refiring of: " + container.getName()
                    + " at time " + nextEvent.timeStamp());
        }

        // Enqueue a pure event to fire the container of this director.
        _fireContainerAt(nextEvent.timeStamp());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that calls to fireAt() should be delegated
     *  to the executive director.
     */
    private boolean _delegateFireAt = false;

    /** Set to true when the time stamp of the token to be dequeue
     *  has exceeded the stopTime.
     */
    private boolean _exceedStopTime = false;

    /** The real time at which the model begins executing. */
    private long _realStartTime = 0;

    /** Start time. */
    private transient Time _startTime;

    /** Stop time. */
    private transient Time _stopTime;

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
}
