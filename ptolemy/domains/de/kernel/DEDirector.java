/* The DE domain director.

Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.DirectedAcyclicGraph;
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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


//////////////////////////////////////////////////////////////////////////
//// DEDirector

/**
   This director implements the discrete-event (DE) model of computation (MoC).
   It should be used as the local director of a CompositeActor that is
   to be executed according to the DE MoC. This director maintains a totally
   ordered set of events and processes these events in the order defined on
   their tags and depths.
   <p>
   An event is associated with a tag, which is a tuple of timestamp and
   microstep. A timestamp indicates the model time when this event occurs. It
   is an object of the {@link ptolemy.actor.util.Time} class. A microstep is an
   integer which represents the index of the sequence of execution phases when
   this director processes events with the same timestamp. Two tags are equal
   if they have the same timestamp and microstep. If two events have the same
   tag, they are called simultaneous events.
   <p>
   Microsteps can only be increased by calling the fireAt() method. For example,
   when an actor requests to be fired again at the current model time, a
   new event with the same timestamp but a bigger microstep (incremented by 1)
   will be generated.
   <p>
   An event is also associated with a depth reflecting its priority, based
   on which a DE director chooses the execution order for simultaneous events.
   A depth is an integer and a larger value of depth indicates a lower priority.
   The depth of an event is determined by topologically sorting all the ports
   of actors according to their data dependencies over which there is no time
   delay.
   <p>
   The order of events is defined as follows. An event A is said to be earlier
   than another event B if A's timestamp is smaller than B's; or if A's
   timestamp is the same as B's, and A's microstep is smaller than B's; or if
   A's tag is the same as B's, and A's depth is smaller than B's. By giving
   events this well-defined order, this director can handle simultaneous events
   in a deterministic way.
   <p>
   The bottleneck in a typical DE simulator is in the maintenance of the
   global event queue. This director uses the calendar queue as the global
   event queue. This is an efficient algorithm with O(1) time complexity in
   both enqueue and dequeue operations. Sorting in the
   {@link ptolemy.actor.util.CalendarQueue} class is done according to the
   order defined above.
   <p>
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
   <p>
   In the initialize() method, depths of actors and IO ports are statically
   analyzed and calculated. They are not calculated in the preinitialize()
   method because hierarchical models may change their structures during their
   preinitialize() method. For example, a modal model does not specify its
   initial state (and its refinement) until the end of its preinitialize()
   method. See {@link ptolemy.domains.fsm.kernel.FSMActor}. In order to support
   mutation, this director recalculates the depths at the beginning of its next
   iteration.
   <p>
   There are two types of depths: one is associated with IO ports, which
   reflects the order of trigger events; the other one is associated with
   actors, which is for pure events. The relationship between the depths of IO
   ports and actors is that the depth of an actor is the smallest of the depths
   of its IO ports. Pure events can only be produced by calling the fireAt()
   method, and trigger events can only be produced by actors that produce
   outputs. See {@link ptolemy.domains.de.kernel.DEReceiver#put}.
   <p>
   Directed loops of IO ports with no delay are not permitted because it is
   impossible to do a topological sort to assign depths. Such a loop can be
   broken by inserting some special actors, such as the <i>TimedDelay</i> actor.
   If zero delay in the loop is truly required, then set the <i>delay</i>
   parameter of those actors to zero. This zero-delay actor plays the same
   role as that of delta delay in VHDL. Note that the detection of directed
   loops are based on port connections rather than data dependencies between
   actors because port connections reflect the data dependencies more
   accurately. The information of port connections are stored in the
   nonpersistent attribute <i>FunctionDependency</i>.
   <p>
   An input port in a DE model contains an instance of DEReceiver.
   When a token is put into a DEReceiver, that receiver posts a trigger
   event to the director. This director sorts trigger events in a global event
   queue.
   <p>
   An iteration, in the DE domain, is defined as processing all the events
   whose tags are equal to the current tag of the director (also called the
   model tag). At the beginning of the fire() method, this director dequeues
   a subset of the earliest events (the ones with smallest timestamp, microstep,
   and depth) from the global event queue. These events have the same
   destination actor. Then, this director invokes that actor to iterate.
   This actor must consume tokens from its input port(s),
   and usually produces new events on its output port(s). These new events will
   be trigger the receiving actors to fire. It is important that the actor
   actually consumes tokens from its inputs, even if the tokens are solely
   used to trigger reactions. This is how polymorphic actors are used in the
   DE domain. The actor will be fired repeatedly until there are no more tokens
   in its input ports, or the actor returns false in its prefire() method. Then,
   this director keeps dequeuing and processing the earliest events from the
   event queue until no more events have the same tag as the model tag.
   After calling the postfire() method, this director finishes an iteration.
   This director is responsible to advance the model tag to perform another
   iteration.
   <p>
   A model starts from the time specified by <i>startTime</i>, which
   has default value 0.0. The stop time of the execution can be set
   using the <i>stopTime</i> parameter. The parameter has a default value
   <i>Infinity</i>, which means the execution runs forever.
   <P>
   Execution of a DE model ends when the timestamp of the earliest event
   exceeds the stop time. This stopping condition is checked inside
   the postfire() method of this director. By default, execution also ends
   when the global event queue becomes empty. Sometimes, the desired
   behaviour is for the director to wait on an empty queue until another
   thread makes new events available. For example, a DE actor may produce
   events when a user hits a button on the screen. To prevent ending the
   execution when there are no more events, set the
   <i>stopWhenQueueIsEmpty</i> parameter to <code>false</code>.
   <p>
   Parameters <i>isCQAdaptive</i>, <i>minBinCount</i>, and
   <i>binCountFactor</i>, are used to configure the calendar queue.
   Changes to these parameters are ignored when the model is running.
   <p>
   If the parameter <i>synchronizeToRealTime</i> is set to <code>true</code>,
   then the director will not process events until the real time elapsed
   since the model started matches the timestamp of the event.
   This ensures that the director does not get ahead of real time. However,
   of course, this does not ensure that the director keeps up with real time.
   <p>
   This director tolerates changes to the model during execution.
   The change should be queued with a component in the hierarchy using
   requestChange().  While invoking those changes, the method
   invalidateSchedule() is expected to be called, notifying the director
   that the topology it used to calculate the priorities of the actors
   is no longer valid.  This will result in the priorities (depths of actors)
   being recalculated the next time prefire() is invoked.

   @author Lukito Muliadi, Edward A. Lee, Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Yellow (hyzheng)
*/
public class DEDirector extends Director implements TimedDirector {
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

    /** Advance the current model tag to that of the earliest event in
     *  the event queue, and fire all actors that have requested or
     *  are triggered to be fired at the current tag. If
     *  <i>synchronizeToRealTime</i> is true, then before firing, wait
     *  until real time matches or exceeds the timestamp of the
     *  event. Note that the default unit for time is seconds.
     *  <p>
     *  Each actor is iterated repeatedly (prefire(), fire(), postfire()),
     *  until either it has no more input tokens, or its prefire() method
     *  returns false.
     *  <p>
     *  If there are no events in the event queue, then the behavior
     *  depends on the <i>stopWhenQueueIsEmpty</i> parameter. If it is
     *  false, then this thread will stall until events become
     *  available in the event queue. Otherwise, time will advance to
     *  the stop time and the execution will halt.
     *
     *  @exception IllegalActionException If the firing actor throws it, or
     *  event queue is not ready, or an event is missed, or time is set
     *  backwards.
     */
    public void fire() throws IllegalActionException {
        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A BIG while loop that handles all events with the same tag.
        while (true) {
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

                    // Setting the follow variable to true makes the
                    // postfire method return false.
                    _noMoreActorsToFire = true;
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
                return;
            }

            // -- If the actor to be fired is not null.
            // If the actor to be fired is the container of this director,
            // the next event to be processed is in an inside receiver of
            // an output port of the container. In this case, this method
            // simply returns, and gives the outside domain a chance to react
            // to that event.
            // NOTE: Topological sort always assigns the composite actor the
            // lowest priority. This guarantees that all the inside actors
            // have fired (reacted to their triggers) before the composite
            // actor fires.
            // TESTIT.
            if (actorToFire == getContainer()) {
                return;
            }

            if (_debugging) {
                _debug("DE director fires at " + getModelTime()
                        + "  with microstep as " + _microstep);
            }

            // Keep firing the actor to be fired until there are no more input
            // tokens available in any of its input ports, or its prefire()
            // method returns false.
            boolean refire;

            do {
                refire = false;

                // NOTE: There are enough tests here against the
                // _debugging variable that it makes sense to split
                // into two duplicate versions.
                if (_debugging) {
                    // Debugging. Report everything.
                    if (((Nameable) actorToFire).getContainer() == null) {
                        // If the actor to be fired does not have a container,
                        // it may just be deleted. Put this actor to the
                        // list of disabled actors.
                        // TESTIT: what if the actor to fire is
                        // a top-level composite actor?
                        _debug("Actor has no container. Disabling actor.");
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
                    if (((Nameable) actorToFire).getContainer() == null) {
                        // If the actor to be fired does not have a container,
                        // it may just be deleted. Put this actor to the
                        // list of disabled actors.
                        _disableActor(actorToFire);
                        break;
                    }

                    if (!actorToFire.prefire()) {
                        break;
                    }

                    actorToFire.fire();

                    if (!actorToFire.postfire()) {
                        // This actor requests not to be fired again.
                        _disableActor(actorToFire);
                        break;
                    }
                }

                // Check all the input ports of the actor to see whether there
                // are more input tokens to be processed.
                Iterator inputPorts = actorToFire.inputPortList().iterator();

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
                        // jump out of the big while loop and
                        // proceed to postfire().
                        // NOTE: we reset the microstep to 0 because it is
                        // the contract that if the event queue has some events
                        // at a time point, the first event must have the
                        // microstep as 0. See the
                        // _enqueueEvent(Actor actor, Time time) method.
                        _microstep = 0;
                        break;
                    } else if (next.microstep() > _microstep) {
                        // If the next event is has a bigger microstep,
                        // jump out of the big while loop and
                        // proceed to postfire().
                        break;
                    } else if ((next.timeStamp().compareTo(getModelTime()) < 0)
                            || (next.microstep() < _microstep)) {
                        throw new IllegalActionException(
                                "The tag of the next event (" + next.timeStamp()
                                + "." + next.microstep() + ") can not be less than"
                                + " the current tag (" + getModelTime() + "."
                                + _microstep + ") !");
                    } else {
                        // The next event has the same tag as the current tag,
                        // indicating that at least one actor is going to be
                        // fired at the current iteration.
                        // Continue the current iteration.
                    }
                }
            }
        } // Close the BIG while loop.

        if (_debugging) {
            _debug("DE director fired!");
        }
    }

    /** Schedule an actor to be fired at the specified time by posting
     *  a pure event to the director.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If event queue is not ready.
     */
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        if (_eventQueue == null) {
            throw new IllegalActionException(this,
                    "Calling fireAt() before preinitialize().");
        }

        // We want to keep event queues at all levels in hierarchy
        // as short as possible. So, this pure event is not reported
        // to the higher level in hierarchy immediately. The postfire
        // method of this director is responsible to report the next
        // earliest event in the event queue to the higher level.
        synchronized (_eventQueue) {
            _enqueueEvent(actor, time);
            _eventQueue.notifyAll();
        }
    }

    /** Schedule a firing of the given actor at the current time.
     *  @param actor The actor to be fired.
     *  @exception IllegalActionException If event queue is not ready.
     */
    public void fireAtCurrentTime(Actor actor) throws IllegalActionException {
        fireAt(actor, getModelTime());
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

        if (_eventQueue.size() > 0) {
            aFutureTime = _eventQueue.get().timeStamp();
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

    /** Return the timestamp of the next event in the queue.
     *  The next iteration time, for example, is used to estimate the
     *  run-ahead time, when a continuous time composite actor is embedded
     *  in a DE model.
     *  <p>
     *  When the timestamp is very big (e.g. bigger than the maximum double),
     *  the double representation loses the specified time resolution.
     *  To avoid this loss, use the {@link #getModelNextIterationTime()}
     *  instead.
     *  @return The time value of the next event in the event queue.
     *  @deprecated As Ptolemy II 4.1, use {@link #getModelNextIterationTime()}
     *  instead.
     */
    public double getNextIterationTime() {
        return getModelNextIterationTime().getDoubleValue();
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
     *  {@link #getModelStartTime()} instead.
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
     *  {@link #getModelStopTime()} instead.
     *  @return the stop time.
     *  @deprecated As Ptolemy II 4.1, use {@link #getModelStopTime()}
     *  instead.
     */
    public final double getStopTime() {
        // This method is final for performance reason.
        return getModelStopTime().getDoubleValue();
    }

    /** Initialize all the contained actors by invoke the initialize() method
     *  of the super class. If any events are generated during the
     *  initialization, and the container is not at the top level, request a
     *  refiring.
     *  <p>
     *  The real start time of the model is recorded when this method
     *  is called. This method is <i>not</i> synchronized on the workspace,
     *  so the caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        _isInitializing = true;
        super.initialize();

        // Reset the following private variables.
        _disabledActors = null;
        _exceedStopTime = false;
        _microstep = 0;
        _noMoreActorsToFire = false;
        _realStartTime = System.currentTimeMillis();

        if (_isEmbedded() && !_eventQueue.isEmpty()) {
            // If the event queue is not empty and the container is not at
            // the top level, ask the upper level director in the
            // hierarchy to refire the container at the timestamp of
            // the earliest event of the local event queue.
            // This design allows the upper level director to keep a
            // relatively short event queue.
            _requestFiring();
        }

        _isInitializing = false;
    }

    /** Indicate that the topological sort of the model may no longer be valid.
     *  This method should be called when topology changes are made.
     *  It sets a flag which will cause the topological
     *  sort to be redone next time when an event is enqueued.
     */
    public void invalidateSchedule() {
        _sortValid = -1;
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
                && (stop
                        || (getModelTime().compareTo(getModelStopTime()) == 0))) {
            _exceedStopTime = true;
            result = result && false;
        } else if (_exceedStopTime) {
            // If the current time is bigger than the stop time,
            // stop the model execution.
            result = result && false;
        } else if (_isEmbedded() && !_eventQueue.isEmpty()) {
            // If the event queue is not empty and the container is an
            // embedded model, ask the upper level director in the
            // hierarchy to refire the container at the timestamp of the
            // first event of the local event queue.
            // This design allows the upper level director (actually all
            // levels in hierarchy) to keep a relatively short event queue.
            _requestFiring();
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
     *  <p>
     *  Throw an exception if the current model time is greater than the next
     *  event timestamp.
     *  @return True if the composite actor is ready to fire.
     *  @exception IllegalActionException If there is a missed event,
     *  or the prefire method of the super class throws it, or can not
     *  query the tokens of the input ports of the container of this
     *  director.
     */
    public boolean prefire() throws IllegalActionException {
        // NOTE: The inside model does not need to have the same
        // microstep as that of the outside model (if it has one.)
        // However, an increment of microstep of the inside model must
        // trigger an increment of microstep of the outside model.
        // Set the model timestamp to the outside timestamp,
        // if this director is not at the top level.
        boolean result = super.prefire();

        if (_debugging) {
            _debug("Current time is: " + getModelTime());
        }

        // A top-level DE director is always ready to fire.
        if (_isTopLevel()) {
            return result;
        }

        // If embedded, check the timestamp of the next event to decide
        // whether this director is ready to fire.
        Time modelTime = getModelTime();
        Time nextEventTime = Time.POSITIVE_INFINITY;

        if (!_eventQueue.isEmpty()) {
            nextEventTime = _eventQueue.get().timeStamp();
        }

        while (modelTime.compareTo(nextEventTime) > 0) {
            _eventQueue.take();

            if (!_eventQueue.isEmpty()) {
                nextEventTime = _eventQueue.get().timeStamp();
            } else {
                nextEventTime = Time.POSITIVE_INFINITY;
            }
        }

        // If the model time is larger (later) than the first event
        // in the queue, then there's a missing firing.
        if (modelTime.compareTo(nextEventTime) > 0) {
            throw new IllegalActionException(this,
                    "Missed a firing. This director is scheduled to fire at "
                    + nextEventTime + ", while" + " the outside time is already "
                    + modelTime + ".");
        }

        // Now, the model time is either less than or equal to the
        // next event time.
        // If there is an internal event scheduled to happen
        // at the current time, it is the right time to fire
        // regardless whether there are external inputs.
        if (!nextEventTime.equals(modelTime)) {
            // If the event timestamp is greater than the model timestamp,
            // we check if there's any external input.
            CompositeActor container = (CompositeActor) getContainer();
            Iterator inputPorts = container.inputPortList().iterator();
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
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method of the
     *  container or one of the deeply contained actors throws it, or the
     *  parameters, minBinCount, binCountFactor, and isCQAdaptive, do not have
     *  valid tokens.
     */
    public void preinitialize() throws IllegalActionException {
        // Initialize an event queue.
        _eventQueue = new DECQEventQueue(this,
                ((IntToken) minBinCount.getToken()).intValue(),
                ((IntToken) binCountFactor.getToken()).intValue(),
                ((BooleanToken) isCQAdaptive.getToken()).booleanValue());

        // Add debug listeners.
        if (_debugListeners != null) {
            Iterator listeners = _debugListeners.iterator();

            while (listeners.hasNext()) {
                DebugListener listener = (DebugListener) listeners.next();
                _eventQueue.addDebugListener(listener);
            }
        }

        // Reset the hashtables for actor and port depths.
        // These two variables have to be reset here because the initialize
        // method constructs them.
        _actorToDepth = null;
        _portToDepth = null;

        // Call the preinitialize method of the super class.
        super.preinitialize();
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
                _eventQueue.notifyAll();
            }
        }

        super.stopFire();
    }

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

    /** Put a pure event into the event queue to schedule the given actor to
     *  fire at the specified timestamp.
     *  <p>
     *  The default microstep for the queued event is equal to zero,
     *  unless the time is equal to the current time, where the microstep
     *  will be the current microstep plus one.
     *  <p>
     *  The depth for the queued event is the minimum of the depths of
     *  all the ports of the destination actor.
     *  <p>
     *  If there is no event queue or the given actor is disabled, then
     *  this method does nothing.
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
                || ((_disabledActors != null)
                        && _disabledActors.contains(actor))) {
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
            throw new IllegalActionException((Nameable) actor,
                    "Attempt to queue an event in the past:" + " Current time is "
                    + getModelTime() + " while event time is " + time);
        }

        int depth = _getDepthOfActor(actor);

        if (_debugging) {
            _debug("enqueue a pure event: ", ((NamedObj) actor).getName(),
                    "time = " + time + " microstep = " + microstep + " depth = "
                    + depth);
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
     *  <p>
     *  If the event queue is not ready or the actor contains the destination
     *  port is disabled, do nothing.
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
                || ((_disabledActors != null)
                        && _disabledActors.contains(actor))) {
            return;
        }

        int depth = _getDepthOfIOPort(ioPort);

        if (_debugging) {
            _debug("enqueue a trigger event for ",
                    ((NamedObj) actor).getName(),
                    " time = " + getModelTime() + " microstep = " + _microstep
                    + " depth = " + depth);
        }

        // Register this trigger event.
        DEEvent newEvent = new DEEvent(ioPort, getModelTime(), _microstep, depth);
        _eventQueue.put(newEvent);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Based on the depths of IO ports, calculate the depths of actors.
    // The results are cached in a hashtable _actorToDepth.
    // Update the depths of existing events in the event queue.
    private void _computeActorDepth() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        LinkedList actors = (LinkedList) container.deepEntityList();
        _actorToDepth = new Hashtable(actors.size());

        Iterator actorsIterator = actors.iterator();

        while (actorsIterator.hasNext()) {
            Actor actor = (Actor) actorsIterator.next();

            // Calculate the depth of the given actor, which is the
            // smallest depth of all the input and output ports.
            // Why?
            // Here is the example: A model with a feedback loop, which
            // contains a non-zero TimedDelay actor. When the TimedDelay actor
            // requests a refiring, the depth of the event should have the
            // depth of its output.
            // The reason to include the depths of input ports for calculation
            // is to reduce unnecessary number of firings. In particular,
            // if an actor receives a trigger event that has the same tag as
            // one of its pure event, one firing is sufficient.
            int depth = -1;
            Iterator inputs = actor.inputPortList().iterator();

            while (inputs.hasNext()) {
                IOPort inputPort = (IOPort) inputs.next();
                int inputDepth = _getDepthOfIOPort(inputPort);

                if ((inputDepth < depth) || (depth == -1)) {
                    depth = inputDepth;
                }
            }

            Iterator outputs = actor.outputPortList().iterator();

            while (outputs.hasNext()) {
                IOPort outputPort = (IOPort) outputs.next();
                int outputDepth = _getDepthOfIOPort(outputPort);

                if ((outputDepth < depth) || (depth == -1)) {
                    depth = outputDepth;
                }
            }

            _actorToDepth.put(actor, new Integer(depth));
        }

        // If the event queue is not empty, we should update the depths of
        // the existing events with new depths.
        // NOTE: After update, we must use the same _eventQueue to keep the
        // existing references to it. For example, the debug listeners.
        if (!_eventQueue.isEmpty()) {
            // Setup a temporary repository for the existing events
            // in the queue.
            LinkedList updatedEventList = new LinkedList();

            while (!_eventQueue.isEmpty()) {
                DEEvent event = _eventQueue.take();
                IOPort ioPort = event.ioPort();
                Actor actor = event.actor();

                // Treat pure events and trigger events differently.
                // Must check ioPort first and then actor, because
                // if ioPort is not null, its actor can not be null.
                if (ioPort != null) {
                    event._updateDepth(_getDepthOfIOPort(ioPort));
                } else if (actor != null) {
                    event._updateDepth(_getDepthOfActor(actor));
                } else {
                    // do nothing.
                }

                updatedEventList.add(event);
            }

            Iterator updatedEvents = updatedEventList.iterator();

            while (updatedEvents.hasNext()) {
                DEEvent updatedEvent = (DEEvent) updatedEvents.next();
                _eventQueue.put(updatedEvent);
            }
        }
    }

    // Perform a topological sort on the directed graph and use the result
    // to set the depth for each IO port. A new Hashtable is created each
    // time this method is called.
    private void _computePortDepth() throws IllegalActionException {
        DirectedAcyclicGraph portsGraph = _constructDirectedGraph();

        if (_debugging && _verbose) {
            _debug("## ports graph is:" + portsGraph.toString());
        }

        // NOTE: this topologicalSort can be smarter.
        // In particular, the dependency between ports belonging
        // to the same actor may be considered.
        Object[] sort = (Object[]) portsGraph.topologicalSort();

        if (_debugging && _verbose) {
            _debug("## Result of topological sort (highest depth to lowest):");
        }

        // Allocate a new hash table with the equal to the
        // number of IO ports sorted.
        // This composite actor is set to the highest depth
        // (the lowest priority).
        _portToDepth = new Hashtable(sort.length);

        // assign depths to ports based on the topological sorting result.
        LinkedList ports = new LinkedList();

        for (int i = 0; i <= (sort.length - 1); i++) {
            IOPort ioPort = (IOPort) sort[i];
            ports.add(ioPort);

            if (_debugging && _verbose) {
                _debug(((Nameable) ioPort).getFullName(), "depth: " + i);
            }

            // Insert the hashtable entry.
            _portToDepth.put(ioPort, new Integer(i));
        }

        if (_debugging && _verbose) {
            _debug("## adjusting port depths based "
                    + "on the strictness constraints.");
        }

        LinkedList actorsWithPortDepthsAdjusted = new LinkedList();

        // The rule is simple. If an output depends on several inputs directly,
        // all inputs must have the same depth, the biggest one.
        for (int i = sort.length - 1; i >= 0; i--) {
            IOPort ioPort = (IOPort) sort[i];

            // Get the container actor of the current output port.
            Actor portContainer = (Actor) ioPort.getContainer();

            // get the strictnessAttribute of actor.
            Attribute strictnessAttribute = ((NamedObj) portContainer)
                .getAttribute(STRICT_ATTRIBUTE_NAME);

            // Normally, we adjust port depths based on output ports.
            // However, if this input port belongs to a sink actor, and
            // the sink actor has more than one input ports, adjust the depths
            // of all the input ports to their maximum value.
            // For exmaple, the XYPlotter in the WirelessSoundDetection demo.
            // By default, all composite actors are non-strict. However, if
            // a composite actor declares its strictness with an attribute,
            // we adjust its input ports depths to their maximum. One example
            // is the ModalModel.
            // A third case is that if a composite actor has some of its input
            // ports as parameter ports and the others as reguler IO ports,
            // we need to adjust the depths of paramter ports also.
            // The TimedSinewave (with SDF implementation) is an example.
            // Since this actor is supposed to be a strict actor, we need to
            // add a strictness marker such that the depths of all its inputs
            // are adjusted to their maximum value.
            // For non-strict composite actors, one solution is to iterate
            // each output port and find all the parameter ports that affect
            // that output port. Note that a parameter may depend on another
            // parameter at the same level of hierarchy, which makes the
            // analysis harder. One reference will be the context analysis by
            // Steve.
            // I prefer to leave the parameter analysis to be independent of the
            // function dependency analysis. 02/2005 hyzheng
            if (ioPort.isInput()) {
                boolean depthNeedsAdjusted = false;
                int numberOfOutputPorts = portContainer.outputPortList().size();

                // If an actor has no output ports, adjustment is necessary.
                if (numberOfOutputPorts == 0) {
                    depthNeedsAdjusted = true;
                }

                // If the actor declares itself as a strict actor,
                // adjustment is necessary.
                if (strictnessAttribute != null) {
                    depthNeedsAdjusted = true;
                }

                // If the port is a parameter port, adjustment is necessary.
                // If depth needs adjusted:
                if (depthNeedsAdjusted) {
                    List inputPorts = portContainer.inputPortList();

                    if (inputPorts.size() <= 1) {
                        // If the sink actor has only one input port, there is
                        // no need to adjust its depth.
                        continue;
                    }

                    if (actorsWithPortDepthsAdjusted.contains(portContainer)) {
                        // The depths of the input ports of this acotr
                        // have been adjusted.
                        continue;
                    } else {
                        actorsWithPortDepthsAdjusted.add(portContainer);
                    }

                    Iterator inputsIterator = inputPorts.iterator();

                    // Iterate all input ports of the sink actor.
                    int maximumPortDepth = -1;

                    while (inputsIterator.hasNext()) {
                        Object object = inputsIterator.next();
                        IOPort input = (IOPort) object;
                        int inputPortDepth = ports.indexOf(input);

                        if (maximumPortDepth < inputPortDepth) {
                            maximumPortDepth = inputPortDepth;
                        }
                    }

                    // Set the depths of the input ports to the maximum one.
                    inputsIterator = inputPorts.iterator();

                    while (inputsIterator.hasNext()) {
                        IOPort input = (IOPort) inputsIterator.next();

                        if (_debugging && _verbose) {
                            _debug(((Nameable) input).getFullName(),
                                    "depth is adjusted to: " + maximumPortDepth);
                        }

                        // Insert the hashtable entry.
                        _portToDepth.put(input, new Integer(maximumPortDepth));
                    }
                }
            }

            // we skip the ports of the container and their depths are handled
            // by the upper level executive director of this container.
            if (portContainer.equals((Actor) getContainer())) {
                continue;
            }

            // Get the function dependency of the container actor
            FunctionDependency functionDependency = portContainer
                .getFunctionDependency();

            Set inputPorts = functionDependency.getInputPortsDependentOn(ioPort);
            Iterator inputsIterator = inputPorts.iterator();

            // Iterate all input ports the current output depends on,
            // find their maximum depth.
            int maximumPortDepth = -1;

            while (inputsIterator.hasNext()) {
                Object object = inputsIterator.next();
                IOPort input = (IOPort) object;
                int inputPortDepth = ports.indexOf(input);

                if (maximumPortDepth < inputPortDepth) {
                    maximumPortDepth = inputPortDepth;
                }
            }

            // Set the depths of the input ports to the maximum one.
            inputsIterator = inputPorts.iterator();

            while (inputsIterator.hasNext()) {
                IOPort input = (IOPort) inputsIterator.next();

                if (_debugging && _verbose) {
                    _debug(((Nameable) input).getFullName(),
                            "depth is adjusted to: " + maximumPortDepth);
                }

                // Insert the hashtable entry.
                _portToDepth.put(input, new Integer(maximumPortDepth));
            }
        }

        if (_debugging) {
            _debug("## End of topological sort of ports.");
        }

        // the sort is now valid.
        _sortValid = workspace().getVersion();
    }

    // Construct a directed graph with nodes representing IO ports and
    // directed edges representing their dependencies. The directed graph
    // is returned.
    private DirectedAcyclicGraph _constructDirectedGraph()
            throws IllegalActionException {
        // Clear the graph
        DirectedAcyclicGraph portsGraph = new DirectedAcyclicGraph();

        Nameable container = getContainer();

        // If the container is not composite actor, there are no actors.
        if (!(container instanceof CompositeActor)) {
            return portsGraph;
        }

        CompositeActor castContainer = (CompositeActor) container;

        // Get the functionDependency attribute of the container of this
        // director. If there is no such attribute, construct one.
        FunctionDependencyOfCompositeActor functionDependency = (FunctionDependencyOfCompositeActor) castContainer
            .getFunctionDependency();

        // NOTE: The following may be a very costly test.
        //       -- from the comments of previous implementations.
        // If the port based data flow graph contains directed
        // loops, the model is invalid. An IllegalActionException
        // is thrown with the names of the actors in the loop.
        Object[] cycleNodes = functionDependency.getCycleNodes();

        if (cycleNodes.length != 0) {
            StringBuffer names = new StringBuffer();

            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) {
                        names.append(", ");
                    }

                    names.append(((Nameable) cycleNodes[i]).getContainer()
                            .getFullName());
                }
            }

            throw new IllegalActionException(this.getContainer(),
                    "Found zero delay loop including: " + names.toString());
        }

        portsGraph = functionDependency.getDetailedDependencyGraph()
            .toDirectedAcyclicGraph();

        return portsGraph;
    }

    /** Disable the specified actor.  All events destined to this actor
     *  will be ignored. If the argument is null, then do nothing.
     *  @param actor The actor to disable.
     */
    private void _disableActor(Actor actor) {
        if (actor != null) {
            if (_debugging) {
                _debug("Actor ", ((Nameable) actor).getName(), " is disabled.");
            }

            if (_disabledActors == null) {
                _disabledActors = new HashSet();
            }

            _disabledActors.add(actor);
        }
    }

    /** Calculate the depth of an actor.
     *  @param actor An actor whose depth is requested.
     *  @return An integer indicating the depth of the given actor.
     *  @exception IllegalActionException If any port of this actor
     *  is not sorted.
     */
    private int _getDepthOfActor(Actor actor) throws IllegalActionException {
        if ((_sortValid != workspace().getVersion()) || (_actorToDepth == null)) {
            _computePortDepth();
            _computeActorDepth();
        }

        Integer depth = (Integer) _actorToDepth.get(actor);

        if (depth != null) {
            return depth.intValue();
        } else {
            throw new IllegalActionException("Attempt to get depth of actor "
                    + ((NamedObj) actor).getName() + " that was not sorted.");
        }
    }

    /** Return the depth of an ioPort, which is the index of this ioPort in
     *  topological sort.
     *  @param ioPort An IOPort whose depth is requested.
     *  @return An int representing the depth of the given ioPort.
     *  @exception IllegalActionException If the ioPort is not sorted.
     */
    private int _getDepthOfIOPort(IOPort ioPort) throws IllegalActionException {
        if ((_sortValid != workspace().getVersion()) || (_portToDepth == null)) {
            _computePortDepth();
            _computeActorDepth();
        }

        Integer depth = (Integer) _portToDepth.get(ioPort);

        if (depth != null) {
            return depth.intValue();
        } else {
            throw new IllegalActionException("Attempt to get depth of ioPort "
                    + ((NamedObj) ioPort).getName() + " that was not sorted.");
        }
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
     *  current model time.
     *  @return The next actor to be fired, which can be null.
     *  @exception IllegalActionException If event queue is not ready, or
     *  an event is missed, or time is set backwards.
     */
    private Actor _getNextActorToFire() throws IllegalActionException {
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

            if (!_isTopLevel()) {
                // If the director is not at the top level.
                if (_eventQueue.isEmpty()) {
                    // NOTE: when could this happen?
                    // The container of this director, an opaque composite
                    // actor, may be invoked by an update of its parameter
                    // port. Therefore, no actors inside this container need
                    // to be fired.
                    // jump out of the loop: LOOPLABEL::GetNextEvent
                    // TESTIT
                    break;
                } else {
                    // For an embedded DE director, the following code prevents
                    // the director from reacting to future events with bigger
                    // time values in their tags.
                    // For a top-level DE director, there is no such constraint
                    // because the top-level director is responsible to advance
                    // simulation by increasing the model tag.
                    nextEvent = (DEEvent) _eventQueue.get();

                    // An embedded director should process events
                    // that only happen at the current tag.
                    // If the event is in the past, that is an error.
                    if ((nextEvent.timeStamp().compareTo(getModelTime()) < 0)
                            || (nextEvent.timeStamp().equals(getModelTime())
                                    && (nextEvent.microstep() < _microstep))) {
                        // missed an event
                        throw new IllegalActionException(
                                "Fire: Missed an event: the next event tag "
                                + nextEvent.timeStamp() + " :: "
                                + nextEvent.microstep()
                                + " is earlier than the current model tag "
                                + getModelTime() + " :: " + _microstep + " !");
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
                }
            } else {
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
                while (_eventQueue.isEmpty() && !_stopRequested) {
                    if (_debugging) {
                        _debug("Queue is empty. Waiting for input events.");
                    }

                    Thread.yield();

                    synchronized (_eventQueue) {
                        if (_eventQueue.isEmpty()) {
                            try {
                                // NOTE: Release the read access held
                                // by this thread to prevent deadlocks.
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
                // or the _stopRequested is true, or an interrupted exception
                // happened.
                if (_eventQueue.isEmpty()) {
                    // Stop is requested or this method is interrupted.
                    // jump out of the loop: LOOPLABEL::GetNextEvent
                    break;
                } else {
                    // At least one event is found in the event queue.
                    nextEvent = (DEEvent) _eventQueue.get();
                }
            }

            // This is the end of the different behaviors of embedded and
            // top-level directors on getting the next event.
            // When this point is reached, the nextEvent can not be null.
            // In the rest of this method, this is not checked any more.
            if (nextEvent == null) {
                throw new IllegalActionException("The event to be handled"
                        + " can not be null!");
            }

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
                    synchronized (_eventQueue) {
                        while (true) {
                            lastFoundEvent = (DEEvent) _eventQueue.get();
                            currentTime = lastFoundEvent.timeStamp();

                            long elapsedTime = System.currentTimeMillis()
                                - _realStartTime;

                            // NOTE: We assume that the elapsed time can be
                            // safely cast to a double.  This means that
                            // the DE domain has an upper limit on running
                            // time of Double.MAX_VALUE milliseconds.
                            // NOTE: When synchronized to real time, time
                            // resolution is millisecond, which is different
                            // from the assumption that the smallest
                            // time interval between any two events is the
                            // minimum double value.
                            double elapsedTimeInSeconds = ((double) elapsedTime) / 1000.0;

                            if (currentTime.getDoubleValue() <= elapsedTimeInSeconds) {
                                break;
                            }

                            long timeToWait = (long) (currentTime.subtract(elapsedTimeInSeconds)
                                    .getDoubleValue() * 1000.0);

                            if (timeToWait > 0) {
                                if (_debugging) {
                                    _debug("Waiting for real time to pass: "
                                            + timeToWait);
                                }

                                try {
                                    // FIXME: Wait() does not release the
                                    // locks on the workspace, this blocks
                                    // UI interactions and may cause deadlocks.
                                    // SOLUTION: workspace.wait(object, long).
                                    _eventQueue.wait(timeToWait);
                                } catch (InterruptedException ex) {
                                    // Continue executing.
                                }
                            }
                        } // while
                    } // sync
                }

                // Consume the earliest event from the queue. The event must be
                // obtained here, since a new event could have been enqueued
                // into the queue while the queue was waiting. For example,
                // an IO interrupt event.
                // FIXME: The above statement is misleading. How could the
                // newly inserted event happen earlier than the previously
                // first event in the queue? It may be possible in the
                // distributed DE models, but should not happen in DE models.
                // Will this cause problems, such as setting time backwards?
                // TESTIT How to??
                synchronized (_eventQueue) {
                    lastFoundEvent = (DEEvent) _eventQueue.take();
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
            } else {
                // We have already found an event and the actor to react to it.
                // Check whether the newly found event has the same tag
                // and depth. If so, the destination actor should be the same,
                // and they are handled at the same time. For example, a pure
                // event and a trigger event that go to the same actor.
                // TESTIT: An actor has two inputs with different depths. If
                // this actor requests to be fired again at some timestamp T.
                // At time T, it receives a trigger sent to the port with a
                // bigger depth, then the pure event happens before the trigger
                // event. If the actor receivers a trigger from the port with a
                // smaller depth at T, then the trigger event and pure event are
                // dequeued together.
                if (nextEvent.hasTheSameTagAndDepthAs(lastFoundEvent)) {
                    // Consume the event from the queue and discard it.
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

    // initialize parameters. Set all parameters to their default values.
    private void _initParameters() {
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
     *  @exception IllegalActionException If the queue is empty.
     */
    private void _requestFiring() throws IllegalActionException {
        DEEvent nextEvent = null;
        nextEvent = _eventQueue.get();

        CompositeActor container = (CompositeActor) getContainer();

        if (_debugging) {
            _debug("Request refiring of an opaque composite actor: "
                    + container.getName());
        }

        // Enqueue a pure event to fire the container of this director.
        container.getExecutiveDirector().fireAt(container, nextEvent.timeStamp());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A hashtable that caches the depths of actors.
    private Hashtable _actorToDepth = null;

    /**
     * The set of actors that have returned false in their postfire()
     * methods. Events destined for these actors are discarded and
     * the actors are  never fired.
     */
    private Set _disabledActors;

    /**
     * The queue used for sorting events.
     */
    private DEEventQueue _eventQueue;

    /**
     * Set to true when the time stamp of the token to be dequeue
     * has exceeded the stopTime.
     */
    private boolean _exceedStopTime = false;

    // A local boolean variable indicating whether this director is in
    // initialization phase execution.
    private boolean _isInitializing = false;

    /**
     * The current microstep.
     */
    private int _microstep = 0;

    /**
     * Set to true when it is time to end the execution.
     */
    private boolean _noMoreActorsToFire = false;

    // A hashtable that caches the depths of ports.
    private Hashtable _portToDepth = null;

    /**
     * The real time at which the model begins executing.
     */
    private long _realStartTime = 0;

    /**
     * Indicator of whether the topological sort giving ports their
     * priorities is valid.
     */
    private long _sortValid = -1;

    // Start and stop times.
    private Time _startTime;
    private Time _stopTime;

    /**
     * Decide whether the simulation should be stopped when there's no more
     * events in the global event queue. By default, its value is 'true',
     * meaning that the simulation will stop under that circumstances.
     * Setting it to 'false', instruct the director to wait on the queue
     * while some other threads might enqueue events in it.
     */
    private boolean _stopWhenQueueIsEmpty = true;

    /**
     * Specify whether the director should wait for elapsed real time to
     * catch up with model time.
     */
    private boolean _synchronizeToRealTime;

    // The name of an attribute that marks an actor as nonstrict.
    private static final String NON_STRICT_ATTRIBUTE_NAME = "_nonStrictMarker";

    // The name of an attribute that marks an actor as strict.
    private static final String STRICT_ATTRIBUTE_NAME = "_strictMarker";
}
