/* A DE domain director.

Copyright (c) 1998-2004 The Regents of the University of California.
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.lib.Source;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.DirectedGraph;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// DEDirector

/**
   This director implements the discrete-event model of computation (MoC).
   It should be used as the local director of a CompositeActor that is
   to be executed according to this MoC. This director maintains a notion
   of model time, and processes events in a chronological order of this time.
   <p>
   An <i>event</i> is a token with a tag, which is a tuple of time stamp, 
   micro step, and depth. The time stamp indicates the time when the event 
   occurs. The microstep represents the phase of execution when processing 
   events with the same time stamp. For example, when an actor requests for 
   firing itself again at the current model time, this director generates a
   new pure event with the same time stamp but a bigger micro step. The depth 
   is the index of the destination actor in a topological sort. A larger value 
   of depth represents a lower priority when processing events.  The depth is 
   determined by topologically sorting the actors according to data 
   dependencies over which there is no time delay. Note that the zero-delay 
   data dependencies are determined on a per-port basis.
   <p>
   Much of the sophistication in this director is aimed at handling 
   simultaneous events (with the same time stamp) intelligently, so that 
   deterministic behavior can be achieved. The bottleneck in a typical DE 
   simulator is in the maintenance of the global event queue. By default, 
   this director uses the calendar queue as the global event queue. This is 
   an efficient algorithm with O(1) time complexity in both enqueue and dequeue 
   operations. Sorting in the {@link ptolemy.actor.util.CalendarQueue} class 
   is done according to the order defined on the tags by the {@link DEEvent} 
   class, which implements the java.lang.Comparable interface.  
   <p> 
   The complexity of the calendar algorithm is sensitive to the length of the
   queue. When the size of the event queue becomes too long or changes quite
   often, the performance of simulation suffers from the penalties of queuing
   and dequeing events. Two mechanisms are implemented to reduce such penalties 
   by keeping the event queue short. The first mechanism is to only store the
   token events that happen at the current model time and pure events 
   in the global event queue. The other mechanism is that in hierarchical 
   models, the lower level model only reports the first event of its local
   event queue to the upper level to schedule its future firing.  
   <p>
   Directed loops with no delay are not permitted because it is impossible 
   to do topology sort to assign depths.  Such a loop can be broken by 
   inserting some special actors, such as the <i>TimedDelay</i> actor.
   If zero delay in the loop is truly required, then set the <i>delay</i> 
   parameter of those actors to zero. This zero-delay actor plays the same
   role as that of delta delay in VHDL. The directed loops are based on the port 
   connections rather than the actor connections because the port connections 
   reflect the data dependencies more accurately. The information of port 
   connections are stored in a nonpersistent attribute 
   <i>FunctionDependency</i>, which is constructed during the initialization 
   phase of the execution.
   <p>
   The priorities (depths) of actors are calculated in the initalize method.
   To be able to calculate the priorities, a function dependency analysis is 
   performed. If any delay-free loops are detected, an exception will be thrown. 
   The priorities are not calculated in the preinitialize method because 
   hierarchical models may change their structures during the preinitialize 
   method. For example, a modal model does not specify its initial state 
   until the end of its preinitialize method. See 
   {@link ptolemy.domains.fsm.kernel.FSMActor}. This director supports mutation.
   When a topology changes, the priorities of actors are recalculated.   
   <p>
   An input port in a DE model contains an instance of DEReceiver.
   When a token is put into a DEReceiver, that receiver enqueues the
   event to the director by calling the _enqueueEvent() method of
   this director. This director sorts all such events in a global event queue.
   <p>
   An iteration, in the DE domain, is defined as processing all the events 
   whose time stamp equals to the current model time of the director. 
   At the beginning of the fire() method, this director dequeues
   a subset of the oldest events (the ones with smallest time
   stamp, microstep, and depth) from the global event queue,
   and puts those events into their destination receivers. The actor(s) to 
   which these events are destined are the ones to be fired.  The depth of
   an event is the depth of the actor to which it is destined.
   The microstep is usually zero, but is incremented when a pure event
   is queued with time stamp equal to the current model time.
   <p>
   The actor that is fired must consume tokens from
   its input port(s), and usually produces new events on its output
   port(s). These new events will be enqueued in the global event queue
   until their time stamps equal the current time.  It is important that
   the actor actually consume tokens from its inputs, even if the tokens are
   solely used to trigger reactions. This is how polymorphic actors are
   used in the DE domain. The actor will
   be fired repeatedly until there are no more tokens in its input
   ports with the current time stamp.  Alternatively, if the actor
   returns false in prefire(), then it will not be invoked again
   in the same iteration even if there are events in its receivers.
   <p>
   A model starts from the time specified by <i>startTime</i>, which
   has default value 0.0. The stop time of the execution can be set 
   using the <i>stopTime</i> parameter. The parameter has default value
   Double.POSITIVE_INFINITY, which means the execution runs for ever.
   <P>
   Execution of a DE model ends when the time stamp of the oldest events
   exceeds a preset stop time. This stopping condition is checked inside
   the prefire() method of this director. By default, execution also ends
   when the global event queue becomes empty. Sometimes, the desired
   behaviour is for the director to wait on an empty queue until another
   thread makes new events available.  For example, a DE actor may produce
   events when a user hits a button on the screen. To prevent ending the
   execution when there are no more events, set the
   <i>stopWhenQueueIsEmpty</i> parameter to <code>false</code>.
   <p>
   Parameters, <i>isCQAdaptive</i>, <i>minBinCount</i>, and
   <i>binCountFactor</i>, are
   used to configure the calendar queue. Changes to these parameters
   are ignored when the model is running.
   <p>
   If the parameter <i>synchronizeToRealTime</i> is set to <code>true</code>,
   then the director will not process events until the real time elapsed
   since the model started matches the time stamp of the event.
   This ensures that the director does not get ahead of real time,
   but, of course, it does not ensure that the director keeps up with
   real time.
   <p>
   This director tolerates changes to the model during execution.
   The change should be queued with a component in the hierarchy using
   requestChange().  While invoking those changes, the method
   invalidateSchedule() is expected to be called, notifying the director
   that the topology it used to calculate the priorities of the actors
   is no longer valid.  This will result in the priorities (depths of actors) 
   being recalculated the next time prefire() is invoked.
   <p>
   FIXME: Review transferOutputs().
   FIXME: Review changes in fire() and _dequeueEvents().
   
   @author Lukito Muliadi, Edward A. Lee, Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Yellow (hyzheng)
   @see DEReceiver
   @see ptolemy.actor.util.CalendarQueue
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

    /**  Construct a director in the  workspace with an empty name.
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
     *
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
     *  DoubleToken.  The value defaults to Double.POSITIVE_INFINITY.
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
     *  If a calendar queue has been created,
     *  register the listener to the calendar queue, too.
     *  @param listener The listener to add to the list of listeners
     *   to which debug messages are sent.
     */
    public void addDebugListener(DebugListener listener) {
        if (_eventQueue != null) {
            _eventQueue.addDebugListener(listener);
        }
        super.addDebugListener(listener);
    }

    /** Update the director parameters when the attributes are changed.
     *  Changes to <i>isCQAdaptive</i>, <i>minBinCount</i>, and
     *  <i>binCountFactor</i> parameters will only be effective on
     *  the next time the model is executed.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *     Not thrown in this class. May be needed by derived classes.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // NOTE: Note all parameters can be changed when the model is running.
        if (attribute == startTime) {
            double startTimeValue = 
                ((DoubleToken)startTime.getToken()).doubleValue();
            _startTime = new Time(this, startTimeValue);
        } else if (attribute == stopTime) {
            double stopTimeValue = 
                ((DoubleToken)stopTime.getToken()).doubleValue();
            _stopTime = new Time(this, stopTimeValue);
        } else if (attribute == stopWhenQueueIsEmpty) {
            _stopWhenQueueIsEmpty =
                ((BooleanToken)stopWhenQueueIsEmpty.getToken()).booleanValue();
        } else if (attribute == synchronizeToRealTime) {
            _synchronizeToRealTime =
                ((BooleanToken)synchronizeToRealTime.getToken())
                .booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Advance current time to the next event in the event queue,
     *  and fire one or more actors that have events at that time.
     *  If <i>synchronizeToRealTime</i> is true, then before firing,
     *  wait until real time matches or exceeds the time stamp of the event.
     *  Note that the default unit for time is seconds. 
     *  Each actor is iterated repeatedly (prefire(), fire(), postfire()),
     *  until either it has no more input tokens at the current time, or
     *  its prefire() method returns false. If there are no events in the
     *  event queue, then the behavior depends on the 
     *  <i>stopWhenQueueIsEmpty</i> parameter.  If it is false,
     *  then this thread will stall until events
     *  become available on the event queue.  Otherwise, time will advance
     *  to the stop time and the execution will halt.
     *
     *  @exception IllegalActionException If the firing actor throws it.
     */
    public void fire() throws IllegalActionException {

        // NOTE: This fire method does not call super.fire() 
        // because this method is very different from that of the super class.

        if (_debugging) {
            _debug("DE director fires at " + getModelTime());
        }

        // The big while loop.
        while (true) {
            // Find the next actor to be fired.
            Actor actorToFire = _getNextActorToFire();
            
            // Check whether the actor to fire is null.
            
            // -- If the actor is null,
            if (actorToFire == null) {
                // NOTE: There are two conditions that the actor to fire 
                // can be null:
                if (_isTopLevel()) {
                    // Case 1:
                    // If this director is an executive director at
                    // the top level, a null actor means that there are 
                    // no events in the event queue.
                    if (_debugging) 
                        _debug("No more events on the event queue.");
                    // Setting the follow variable to true makes the
                    // postfire method return false.
                    _noMoreActorsToFire = true;
                } else { 
                    // Case 2: 
                    // If this director belongs to an opaque composite model,
                    // the director may be invoked by an update of the 
                    // external parameter port of the embedded model. 
                    // Therefore, no actors contained by the composite model 
                    // need to fire.
                    // NOTE: There may still be events in the local event queue
                    // of this director that are scheduled for the future. 
                    if (_debugging) {
                        _debug("No actor requests to be fired " +
                                "at the current time.");
                    }
                }
                // Nothing more needs to be done in the current iteration.
                // Simply return.
                return;
            }
            
            // -- If the actor to fire is not null.
            
            // It is possible that the next event to be processed is in
            // an inside receiver of an output port of an opaque composite
            // actor containing this director.  In this case, we simply
            // return, giving the outside domain a chance to react to that 
            // event.
            // NOTE: Topology sort always assigns the composite actor the 
            // biggest depth (lowest priority). This guarantees that all the 
            // inside actors are fired before the outside actor fires. 
            if (actorToFire == getContainer()) {
                return;
            }
            
            // Repeatedly fire the actor until there are no more input
            // tokens available in the input ports of this actor, 
            // or until its prefire() method returns false.
            boolean refire;
            do {
                refire = false;
                // NOTE: There are enough tests here against the
                // _debugging variable that it makes sense to split
                // into two duplicate versions.
                if (_debugging) {
                    // Debugging. Report everything.
                    // FIXME: this may not be true any more. Suppose 
                    // the top level contains an external input or output.
                    if (((Nameable)actorToFire).getContainer() == null) {
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
                                ((Nameable)actorToFire).getName());
                        // This actor requests not to be fired again. 
                        _disableActor(actorToFire);
                    }
                    _debug(new FiringEvent(this, actorToFire,
                                   FiringEvent.AFTER_POSTFIRE));
                } else {
                    // No debugging.
                    // If the actor to be fired does not have a container,
                    // it may just be deleted. Put this actor to the 
                    // list of disabled actors.
                    if (((Nameable)actorToFire).getContainer() == null) {
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

                // Check all the input ports of the actor see whether there
                // are more input data to process.
                Iterator inputPorts = actorToFire.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort port = (IOPort)inputPorts.next();
                    // iterate all the channels of the current input port.
                    for (int i = 0; i < port.getWidth(); i++) {
                        if (port.hasToken(i)) {
                            refire = true;
                            // Found a channel that has input data,
                            // jump out of the for loop.
                            break; 
                        }
                    }
                    if (refire == true) {
                        // Found an input that has input data,
                        // jump out of the while loop.
                        break; 
                    }
                }
            } while (refire); // close the do{...}while() loop

            // The following block of code also appears in the _dequeueEvents() 
            // method. However, its functions are different. 
            
            // In the _dequeueEvents() method, the code is only applied 
            // on an embedded DE director to prevent it from reacting to 
            // the future events later than the current model time. 
            // The code is not applied on a top-level DE director because 
            // the top level is responsible to advance time.
            
            // In this method, the code enforces that a firing (or iteration) 
            // of the DE director only handles the events with their time stamp
            // the same as the current model time. The code is used to 
            // terminate an iteration and applied on both embedded and top-level
            // directors.
            synchronized(_eventQueue) {
                if (!_eventQueue.isEmpty()) {
                    DEEvent next = _eventQueue.get();
                    if (next.timeStamp().compareTo(getModelTime()) > 0) {
                        // If the next event is in the future,
                        // jump out of the big while loop and 
                        // proceed to postfire().
                        break;
                    } else if (next.timeStamp().compareTo(getModelTime()) < 0) {
                        throw new InternalErrorException(
                                "The time stamp of the next event " 
                                + next.timeStamp() + " can not be less than"
                                + " the current time " + getModelTime() + " !");
                    } else {
                        // The next event has a time stamp as the current model 
                        // time, indicating at least one actor is going to fire 
                        // at the current model time.
                    }
                } else {
                    // The queue is empty, proceed to postfire().
                    // Jump out of the big while loop.
                    break;
                }
            }
        } // Close the big while loop.

        if (_debugging) {
            _debug("DE director fired!");
        }
    }

    /** Schedule an actor to be fired at the specified time.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If this method is called
     *  before the model is running.
     */
    public void fireAt(Actor actor, Time time)
            throws IllegalActionException {

        if (_eventQueue == null) {
            throw new IllegalActionException(this,
                    "Calling fireAt() before preinitialize().");
        }

        // We want to keep the event queues at all levels in hierarchy
        // as short as possible. So, this pure event is not reported
        // to higher level in hierarchy. The postfire method of this
        // director is responsible to report the next nearest event
        // in the future to the higher level.

        synchronized(_eventQueue) {
            _enqueueEvent(actor, time);
            _eventQueue.notifyAll();
        }
    }

    /** Schedule a firing of the given actor at the current time.  
     *  @param actor The actor to be fired.
     *  @exception IllegalActionException If this method is called
     *  before the model is running.
     */
    public void fireAtCurrentTime(Actor actor)
            throws IllegalActionException {
        fireAt(actor, getModelTime());
    }

    /** Schedule an actor to be fired in the specified time relative to
     *  the current model time.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If the specified time contains 
     *  a negative time value.
     */ 
    public void fireAtRelativeTime(Actor actor, Time time)
            throws IllegalActionException {
          fireAt(actor, time.add(getModelTime()));
    }

    /** Return the event queue. Note that this method is not synchronized.
     *  Any further accesses to this event queue need synchronization.
     *  @return The event queue.
     */
    public DEEventQueue getEventQueue() {
        return _eventQueue;
    }

    /** Return the time stamp of the next event in the queue with time stamp
     *  strictly greater than the current time.  If there is nothing on
     *  the event queue, then return the stop time. The next iteration time,
     *  for example, is used to estimate the run-ahead time, when a continuous
     *  time composite actor is embedded in the DE domain.
     *  <p>
     *  When the iteration time is too big, the double representation loses
     *  the specified time resolution. To avoid this loss, use the 
     *  {@link #getModelNextIterationTime()} instead.
     *  @return The next larger time on the event queue.
     *  @deprecated As Ptolemy II 4.1, use {@link #getModelNextIterationTime()}
     *  instead.
     */
    public double getNextIterationTime() {
        return _eventQueue.get().timeStamp().getDoubleValue();
    }

    /** Return the time stamp of the next event in the queue with time stamp
     *  strictly greater than the current time.  If there is nothing on
     *  the event queue, then return the stop time. The next iteration time,
     *  for example, is used to estimate the run-ahead time, when a continuous
     *  time composite actor is embedded in the DE domain.
     *  @return The next larger time on the event queue.
     */
    public Time getModelNextIterationTime() {
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
     *  {@link #getModelStartTime()} instead.
     *  @return the start time.
     *  @deprecated As Ptolemy II 4.1, use {@link #getModelStartTime()}
     *  instead.
     */
    public final double getStartTime() {
        // This method is final for performance reason.
        return getModelStartTime().getDoubleValue();
    }

    /** Return the start time parameter value. 
     *  @return the start time.
     */
    public final Time getModelStartTime() {
        // This method is final for performance reason.
        return _startTime;
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

    /** Return the stop time. 
     *  @return the stop time.
     */
    public final Time getModelStopTime() {
        // This method is final for performance reason.
        return _stopTime;
    }

    /** Initialize all the contained actors by invoke the initialize() method 
     *  of the super class, and if any events are generated during the 
     *  initialization, and the container is not at top level, request a 
     *  refiring.
     *  <p> The real start time of the model is recorded when this method
     *  is called. 
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        Actor container = (Actor)getContainer();
        
        // Register two pure events, one for starting time and the other
        // for ending time.
//        fireAt(container, getModelTime());
//        fireAt(container, getModelStopTime());
        
        _exceedStopTime = false;
        _realStartTime = System.currentTimeMillis();
        if (_isEmbedded() && !_eventQueue.isEmpty()) {
            // if the event queue is not empty and the container is not at
            // the top level, ask the upper level director in the 
            // hierarchy to refire the container at the time stamp of
            // the first event of the local event queue.
            // This design allows the upper level director to keep a
            // relatively short event queue. 
            _requestFiring();
        }
    }

    /** Indicate that the topological depth of the ports in the model may
     *  no longer be valid. This method should be called when topology
     *  changes are made.  It sets a flag which will cause the topological
     *  sort to be redone next time when an event is enqueued.
     */
    public void invalidateSchedule() {
        _sortValid = -1;
    }

    /** Return a new receiver of the type DEReceiver.
     *  @return A new DEReceiver.
     */
    public Receiver newReceiver() {
        if (_debugging) _debug("Creating new DE receiver.");
        return new DEReceiver();
    }

    /** Return false if there are no more actors to fire or if stop()
     *  has been called. Otherwise, if
     *  the director is an embedded director and the queue is not empty,
     *  then request that the executive director refires the container of
     *  this director at the time of the first event in the event queue
     *  of this director.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        // stop when event queue is empty
        boolean stop = ((BooleanToken)stopWhenQueueIsEmpty.getToken())
            .booleanValue();
        
        // If no more actors will be fired (i.e. event queue is empty) 
        // and either of the following conditions is satisfied:
        // 1. the stopWhenWueueIsEmpty parameter is set to true.
        // 2. the current model time equals the model stop time.
        // stop the model.
        // Or,
        // if the event queue is not empty, but the current time exceeds 
        // the stop time, stop the model.
        if (_noMoreActorsToFire && (stop || 
                getModelTime().compareTo(getModelStopTime()) == 0 )) {
            _exceedStopTime = true;
            result = result && false;
        } else if (_exceedStopTime) {
            // If the current time is bigger than the stop time, 
            // stop the model execution.
            result = result && false;
        } else if (_isEmbedded() && !_eventQueue.isEmpty()) {
            // if the event queue is not empty and the container is an
            // embedded model, ask the upper level director in the 
            // hierarchy to refire the container at the time stamp of the
            // first event of the local event queue.
            // This design allows the upper level director to keep a
            // relatively short event queue. 
            _requestFiring();
        }

        // FIXME: the following commented block guarantees that
        // no events with different tags appear in the same firing.
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
//
        return result;
    }

    /** Set the model time to the outside time if this director is not
     *  at the top level. Check the next event time to decide whether to
     *  fire. Return true if there are inputs to this composite actor, or
     *  the next event time is equal to the current model time. Otherwise,
     *  return false. Throw an exception if the current model time is greater 
     *  than the next event time.
     *  @return True if the composite actor is ready to run for one
     *          iteration.
     *  @exception IllegalActionException If the current model time is larger
     *  than the time of the first event in the event queue.
     */
    public boolean prefire() throws IllegalActionException {
        // Set the modal time to the outside time, if this director
        // is not at the top level.
        boolean result = super.prefire();
        
        // A top-level DE director is always ready to fire.
        if (_isTopLevel()) {
            return result;
        }

        // If embedded, check the next event time to decide
        // whether this director is ready to fire.           
        Time modelTime = getModelTime();
        Time nextEventTime = Time.POSITIVE_INFINITY;
        if (!_eventQueue.isEmpty()) {
            nextEventTime =  _eventQueue.get().timeStamp();
        }

        // If the model time is larger (later) than the first event
        // in the queue, then there's a missing firing.
        if (modelTime.compareTo(nextEventTime) > 0) {
            throw new IllegalActionException(this,
                    "Missed a firing. This director is scheduled to fire at "
                    + nextEventTime + ", while"
                    + " the outside time is already "
                    + modelTime + ".");
        }

        // Now, the model time is either less than or equal to the 
        // next event time. We check if there's any external input.
        CompositeActor container = (CompositeActor)getContainer();
        Iterator inputPorts = container.inputPortList().iterator();
        boolean hasInput = false;
        while (inputPorts.hasNext() && !hasInput) {
            IOPort port = (IOPort)inputPorts.next();
            for (int i = 0; i < port.getWidth(); i++) {
                if (port.hasToken(i)) {
                    hasInput = true;
                    break;
                }
            } 
        }

        if (hasInput) {
            // If there is at least one external input.
            result = result && true;
        } else {
            // If there is no external input.
            if (nextEventTime.equals(modelTime)) {
                // If there is an internal event scheduled to happen 
                // at the current time, it is the right time to fire.
                result = result && true;
            } else {
                // If there is no internal event, it is not the correct 
                // time to fire. 
                // NOTE: This may happen because the container is statically
                // scheduled by its director to fire at this time.
                // For example, a DE model in a Giotto model.
                result = result &&  false;
            }
        }
        return result;
    }

    /** Set current time to the model start time, invoke the preinitialize() 
     *  methods of all actors deeply contained by the container.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. Actors cannot produce output data in their preinitialize()
     *  methods. If initial events are needed, e.g. pure events for source
     *  actor, the actors should do so in their initialize() method.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _eventQueue = new DECQEventQueue(this,
                ((IntToken)minBinCount.getToken()).intValue(),
                ((IntToken)binCountFactor.getToken()).intValue(),
                ((BooleanToken)isCQAdaptive.getToken()).booleanValue());

        // Add debug listeners.
        if (_debugListeners != null) {
            Iterator listeners = _debugListeners.iterator();
            while (listeners.hasNext()) {
                DebugListener listener = (DebugListener)listeners.next();
                _eventQueue.addDebugListener(listener);
            }
        }
        _disabledActors = null;
        _noMoreActorsToFire = false;
        _microstep = 0;
        // Call the parent preinitialize method to create the receivers.
        super.preinitialize();
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     */
    public void removeDebugListener(DebugListener listener) {
        if (_eventQueue != null) {
            _eventQueue.removeDebugListener(listener);
        }
        super.removeDebugListener(listener);
    }

    /** Request that execution of the current iteration stop.
     *  This is similar to stopFire(), except that the current iteration
     *  is not allowed to complete.  This is useful if there is actor
     *  in the model that has a bug where it fails to consume inputs.
     *  An iteration will never terminate if such an actor receives
     *  an event.
     *  If the director is paused waiting for events to appear in the
     *  event queue, then it stops waiting,
     *  and calls stopFire() for all actors
     *  that are deeply contained by the container of this director.
     */
    public void stop() {
        if (_eventQueue != null) {
            synchronized(_eventQueue) {
                // NOTE: The stopFire() method, unlike this one, allows
                // the iteration to complete. This does not.
                _stopRequested = true;
                _eventQueue.notifyAll();
            }
        }
        super.stop();
    }

    /** Request that execution of the current iteration complete.
     *  If the director is paused waiting for events to appear in the
     *  event queue, then it stops waiting,
     *  and calls stopFire() for all actors
     *  that are deeply contained by the container of this director.
     */
    public void stopFire() {
        if (_eventQueue != null) {
            synchronized(_eventQueue) {
                // NOTE: The stop() method requests stopping,
                // which does not allow the current iteration to
                // complete.
                _eventQueue.notifyAll();
            }
        }
        super.stopFire();
    }

    /** Override the base class method to transfer all the available
     *  tokens from a DE model.  No data remains at the boundary of a
     *  DE model after the model has been fired.  This facilitates
     *  building multirate DE models.  The port argument must be an
     *  opaque output port. If any channel of the output port has no
     *  data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
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
     *   one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _eventQueue.clear();
        _disabledActors = null;
        _noMoreActorsToFire = false;
        _microstep = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Disable the specified actor.  All events destined to this actor
     *  will be ignored. If the argument is null, then do nothing.
     *  @param actor The actor to disable.
     */
    protected void _disableActor(Actor actor) {
        if (actor != null) {
            if (_debugging) _debug("Actor ", ((Nameable)actor).getName(),
                    " is disabled.");
            if (_disabledActors == null) {
                _disabledActors = new HashSet();
            }
            _disabledActors.add(actor);
        }
    }

    /** Put a pure event into the event queue with the specified time stamp.
     *  A "pure event" is one with no token, used to request
     *  a firing of the specified actor at some model time.
     *  Note that the actor may have no new data at its input ports
     *  when it is fired.
     *  The depth for the queued event is the minimum of the depth of 
     *  this actor and those of the actors that receive tokens from this actor.
     *  A smaller depth corresponds to a higher priority.
     *  The microstep for the queued event is equal to zero,
     *  unless the time is equal to the current time.
     *  If it is, then the event is queued with the current microstep
     *  plus one.  If there is no event queue, then this method does
     *  nothing.
     *
     *  @param actor The destination actor.
     *  @param time The time stamp of the "pure event".
     *  @exception IllegalActionException If the time argument is in the past.
     */
    protected void _enqueueEvent(Actor actor, Time time)
            throws IllegalActionException {

        if (_eventQueue == null || 
            (_disabledActors != null && _disabledActors.contains(actor))) 
            return;
        
        // Adjust the micro step.
        int microstep = 0;
        if (time.compareTo(getModelTime()) == 0) {
            microstep = _microstep + 1;
        } else if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException((Nameable)actor,
                    "Attempt to queue an event in the past:"
                    + " Current time is " + getModelTime()
                    + " while event time is " + time);
        }
        // Calculate the depth of the pure event
        // The depth is calculated in the following way:
        // 1. Find the depth of the actor that requested refirng.
        // 2. Find the minimum of the depths of the actors that receive
        //    tokens from the above actor. (The smaller the depth, the
        //    higher the priority.)
        // 3. Choose the smaller one of the above two depths 
        //    as the depth of the pure event.
        // Why?
        // Here is the example: a feedback loop contains a non-zero TimedDelay. 
        // When the TimedDelay actor requests a refiring, the
        // pure event should have a depth no greater than the depths of the
        // consumer actors such that the consumer actors can fire with 
        // the proper and enough tokens.  
        
        // NOTE: This procedure also applies to FIXME (DEE? SDE?) except that
        // the depths are associated with ports and the result depth is
        // more accurate.
        
        // NOTE: An alternative semantics is to give the depth of the pure 
        // events the highest priority, depth as -1. If multiple pure events 
        // exist, their order in the event queue reflects the order they are 
        // executed before. 

        // step 1.
        int depth = _getDepth(actor);
        // step 2.
        Iterator outputs = actor.outputPortList().iterator();
        while (outputs.hasNext()) {
            IOPort outPort = (IOPort) outputs.next();
            Receiver[][] receivers = outPort.getRemoteReceivers();
            for (int i = 0; i < receivers.length; i++) {
                // FIXME: For ParameterPort, it is possible that
                // the downstream receivers are null. It is a
                // unresolved issue about the semantics of Parameter
                // Port considering the lazy evaluation of variables.
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        IOPort ioPort =
                            receivers[i][j].getContainer();
                        Actor successor = (Actor) ioPort.getContainer();
                        int successorDepth = _getDepth(successor);
                        if (successorDepth < depth) {
                            depth = successorDepth;
                        }
                    }
                }
            }            
        }
        
        if (_debugging) 
            _debug("enqueue a pure event: ", ((NamedObj)actor).getName(),
                "time = " + time + " microstep = " + microstep + " depth = "
                + depth);
        // Register the new pure event.
        DEEvent newEvent = new DEEvent(actor, time, microstep, depth); 
        _eventQueue.put(newEvent);
    }

    /** Put an event into the event queue with the specified destination
     *  receiver, token, and time stamp. The depth of the event is the
     *  depth of the actor that has the receiver.
     *  A smaller depth corresponds
     *  to a higher priority.  The microstep is always equal to zero,
     *  unless the time argument is equal to the current time, in which
     *  case, the microstep is equal to the current microstep (determined
     *  by the last dequeue, or zero if there has been none). If there is
     *  no event queue, then this method does nothing.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param time The time stamp of the event.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token,
            Time time) throws IllegalActionException {

        Actor actor = (Actor) receiver.getContainer().getContainer();
        if (_eventQueue == null || 
            (_disabledActors != null && _disabledActors.contains(actor))) 
            return;

        int microstep = 0;
        if (time == getModelTime()) {
            microstep = _microstep;
        } else if (time.compareTo(getModelTime()) < 0) {
            Nameable destination = receiver.getContainer();
            throw new IllegalActionException(destination,
                    "Attempt to queue an event in the past: "
                    + " Current time is " + getModelTime()
                    + " while event time is " + time);
        }

        Actor destination = (Actor)(receiver.getContainer()).getContainer();
        int depth = _getDepth(destination);
        if (_debugging) _debug("enqueue event: to",
                receiver.getContainer().getFullName()
                + " ("+token.toString()+") ",
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(receiver, token, time, microstep, depth));
    }

    /** Return the depth of the actor.
     *  @exception IllegalActionException If the actor is not sorted and
     *  without a depth.
     */
    protected int _getDepth(Actor actor) throws IllegalActionException {
        if (_sortValid != workspace().getVersion()) {
            _computeDepth();
            // the sort is now valid.
            _sortValid = workspace().getVersion();
            // FIXME: There may be events in the event queue. The
            // depth of these events may become invalid after this new
            // topological sory. 
            // There are at least two choices:
            // 1. Discard all remaining events in the event queue. This 
            // actually disables mutation.  
            // 2. Update depths of the remaining events with the newly
            // calculated ones.
        }
        Integer depth = (Integer)_actorToDepth.get(actor);
        if (depth != null) {
            return depth.intValue();
        }
        throw new IllegalActionException("Attempt to get the depth of a actor " 
                + ((NamedObj)actor).getName() + " that was not sorted.");
    }

    /** Dequeue the events from the event queue that have the smallest
     *  time stamp and depth and return their destination actor. 
     *  Advance the model time to their time stamp.
     *  If the time stamp is greater than the stop time then return null.
     *  If there are no events on the event queue, and _stopWhenQueueIsEmpty
     *  flag is true (which is set to true by default) then return null,
     *  which will have the effect of stopping the simulation.
     *  If _stopWhenQueueIsEmpty is false and the queue is empty, then
     *  stall the current thread by calling wait() on the _eventQueue
     *  until there are events available.  If _synchronizeToRealTime
     *  is true, then this method may suspend the calling thread using
     *  Object.wait(long) to let elapsed real time catch up with the
     *  current event.
     *  @return The next actor to fire.
     */
    protected Actor _getNextActorToFire() {
        Actor actorToFire = null;
        DEEvent currentEvent = null, nextEvent = null;

        // If there is no event queue, then there is obviously no
        // actor to fire. (preinitialize() has not been called).
        if (_eventQueue == null) return null;

        // Keep taking events out until there are no more events with the same
        // time stamp or until the queue is empty, or until a stop is requested.
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
                // If the directory is not at the top level.
                if (_eventQueue.isEmpty()) {
                    // NOTE: when could this happen?
                    // The containsr of this director, an embedded model,  
                    // may be invoked by an update of the 
                    // parameter port of the model. Therefore, no 
                    // actors inside this container need to fire.
                    // jump out of the loop: LOOPLABEL::GetNextEvent
                    break;
                } else {
                    nextEvent = (DEEvent)_eventQueue.get();
                    // An embedded director should process events 
                    // that only happen at the current time.
                    // If the event is in the past, that is an error.
                    if (nextEvent.timeStamp().compareTo(getModelTime()) < 0){
                        //missed an event
                        nextEvent = null;
                        throw new InternalErrorException(
                            "Fire: Missed an event: the next event time "
                            + nextEvent.timeStamp() + " is earlier than the"
                            + " current time " + getModelTime() + " !");
                    }
                    // If the event is in the future, it is ignored
                    // and will be processed later.
                    if (nextEvent.timeStamp().compareTo(getModelTime()) > 0) {
                        //reset the next event
                        nextEvent = null;
                        // jump out of the loop: LOOPLABEL::GetNextEvent
                        break;
                    }
                }
            } else {
                // If the director is at the top level

                // If the event queue is empty, 
                // a blocking read is performed on the queue.
                // However, there are two conditions that the blocking 
                // read is not performed, which are checked below.
                if (_eventQueue.isEmpty()) {
                    // The two conditions are:
                    // 1. An actor to be fired has been found.
                    // 2. There are no more events in the event queue,
                    // and the current time is the stop time. 
                    if (actorToFire != null || 
                        (getModelTime() == getModelStopTime())) {
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
                    synchronized(_eventQueue) {
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
                }// Close the blocking read while loop
                
                // To reach this point, either the event queue is not empty,
                // , the _stopRequested is true, or an inturrupted exception
                // happened.
                if (_eventQueue.isEmpty()) {
                    // Stop is requested or be inturrupted.
                    // jump out of the loop: LOOPLABEL::GetNextEvent
                    break;
                } else {
                    // At least one event is found in the event queue.
                    nextEvent = (DEEvent)_eventQueue.get();
                }
            } 
            // End of the different behaviors on getting the next event 
            // between embedded and top-level directors.

            // When this point is reached, the nextEvent can not be null.
            if (nextEvent == null) {
                throw new InternalErrorException("The event to be handled"
                    + " can not be null!");
            }
            
            // If the actorToFire is null, find the actor associated with
            // the next event. Otherwise, check whether the event just found 
            // goes to the same actor to be fired. 
            if (actorToFire == null) {
                // If the actorToFire is not set yet, 
                // find the actor associated with the event just found,
                // and update the current time with the event time.
                Time currentTime;
                // If not synchronized to the real time.
                if (!_synchronizeToRealTime) {
                    currentEvent = (DEEvent)_eventQueue.get();
                    currentTime = currentEvent.timeStamp();
                } else {
                    // If synchronized to the real time.
                    synchronized(_eventQueue) {
                        while (true) {
                            currentEvent = (DEEvent)_eventQueue.get();
                            currentTime = currentEvent.timeStamp();
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
                            double elapsedTimeInSeconds =
                                ((double)elapsedTime)/1000.0;
                            if (currentTime.getDoubleValue() 
                                    <= elapsedTimeInSeconds) {
                                break;
                            }
                            long timeToWait = (long)(currentTime.subtract(
                                elapsedTimeInSeconds).getDoubleValue()*1000.0);
                            if (timeToWait > 0) {
                                if (_debugging) {
                                    _debug("Waiting for real time to pass: "
                                            + timeToWait);
                                }
                                try {
                                    // FIXME: Wait() does not realease the
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

                // Consume the event from the queue.  The event must be
                // obtained here, since a new event could have been enqueued
                // into the queue while the queue was waiting. For example,
                // an IO interrupt event. 
                // NOTE: The newly inserted event may happen earlier than the
                // previously first event in the queue. 
                synchronized(_eventQueue) {
                    currentEvent = (DEEvent) _eventQueue.take();
                    currentTime = currentEvent.timeStamp();
                    actorToFire = currentEvent.actor();

                    // NOTE: The _enqueEvent method discard the events 
                    // for disabled actors.
                    if (_disabledActors != null &&
                            _disabledActors.contains(actorToFire)) {
                        // This actor has requested not to be fired again.
                        if (_debugging) _debug("Skipping diabled actor: ",
                                ((Nameable)actorToFire).getFullName());
                        actorToFire = null;
                        // start a new iteration of the loop: 
                        // LOOPLABEL::GetNextEvent
                        continue;
                    }

                    // Advance current time to event time.
                    // NOTE: This is the only place the model time changes.
                    try {
                        setModelTime(currentTime);
                    } catch (IllegalActionException ex) {
                        // Thrown if time moves backwards.
                        throw new InternalErrorException(this, ex, null);
                    }
                }

                _microstep = currentEvent.microstep();

                // Exceeding stop time means the current time is strictly 
                // bigger than the model stop time. 
                if (currentTime.compareTo(getModelStopTime()) > 0) {
                    if (_debugging) {
                        _debug("Current time has passed the stop time.");
                    }
                    _exceedStopTime = true;
                    return null;
                }

                // Transfer the event to the receiver and keep track
                // of which receiver is filled.
                DEReceiver receiver = (DEReceiver) currentEvent.receiver();

                // If the receiver is null, then it's a 'pure event', 
                // and there's no need to put events into that receiver.
                if (receiver != null) {
                    // Transfer the event to the receiver.
                    if (_debugging) 
                        _debug(getName(), " puts trigger event to",
                            receiver.getContainer().getFullName());
                    receiver._triggerEvent(currentEvent.token());
                }
            } else {
                
                // Already found an event and the actor to react to it.
                // Check whether the newly found event has the same time 
                // stamp and depth.  
                // If so, the destination actor should be the same, but check 
                // anyway.

                // NOTE: Simultaneous events are handled at the same
                // iteration, which is one of the key differences from the
                // SDEDirector.
                
                // NOTE: A pure event to an actor may not have the 
                // same depth of that actor because the dedicated depth 
                // calculation process. See _enqueueEvent(Actor, Time) method.
                // When dequeuing events, this difference has to be considered.
                  
                // NOTE: The above assumption will not always hold for 
                // non-strict actors. In which case, we need to use the 
                // SDEDirector.
                boolean isPureEvent = (nextEvent.receiver() == null) 
                    || (currentEvent.receiver() == null);
                boolean theSameActor = nextEvent.actor().equals(
                    currentEvent.actor());
                
                // The following predicate is true if either of the two
                // conditions is satisfied:
                // 1. Two token events that go to same port of the same actor.
                // 2. A pure event and a token event that go to the actor.
                if (nextEvent.hasTheSameTagAndDepthAs(currentEvent) ||
                     (nextEvent.hasTheSameTagAs(currentEvent) && isPureEvent 
                             && theSameActor)){

                    // Consume the event from the queue.
                    _eventQueue.take();

                    // Transfer the event into the receiver.
                    DEReceiver receiver = (DEReceiver) nextEvent.receiver();
                    // If receiver is null, then it's a 'pure event' and
                    // there's no need to put event into receiver.
                    if (receiver != null) {
                        // Transfer the event to the receiver.
                        receiver._triggerEvent(nextEvent.token());
                    }
                } else {
                    // Next event has a future tag or different destination.
                    break;
                }
            }
        }// close the loop: LOOPLABEL::GetNextEvent
        
        // Note that the actor to be fired can be null.
        return actorToFire;
    }

    /** Override the default Director implementation, because in DE
     *  domain, we don't need write access inside an iteration.
     *  @return false.
     */
    protected boolean _writeAccessRequired() {
        // Return false to let the workspace be write-protected.
        // Return true to debug the PtolemyThread.
        return false;
    }

    /** Request that the container of this director be refired in some
     *  future time specified by the first event of the local event queue.
     *  This method is used when the director is embedded inside an opaque
     *  composite actor. If the queue is empty, then throw an 
     *  IllegalActionException.
     */
    protected void _requestFiring() throws IllegalActionException {
        DEEvent nextEvent = null;
        nextEvent = _eventQueue.get();
        // Enqueue a refire for the container of this director.
        CompositeActor container = (CompositeActor)getContainer();
        if (_debugging) {
            _debug("Request refiring of opaque composite actor: " +
                    container.getName());
        }
        container.getExecutiveDirector().fireAt(
                container, nextEvent.timeStamp());
    }
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A Hashtable stores the mapping of each actor to its depth.*/
    protected Hashtable _actorToDepth = null;

    /** The set of actors that have returned false in their postfire() methods.
     *  Events destined for these actors are discarded and the actors are 
     *  never fired.
     */
    protected Set _disabledActors;

    /** The queue used for sorting events. */
    protected DEEventQueue _eventQueue;

    /** Set to true when the time stamp of the token to be dequeue has
     * exceeded the stopTime. 
     */
    protected boolean _exceedStopTime = false;

    /** The current microstep. */
    protected int _microstep = 0;

    /** Set to true when it is time to end the execution. */
    protected boolean _noMoreActorsToFire = false;

    /** The real time at which the model begins executing. */
    protected long _realStartTime = 0;

    /** Indicator of whether the topological sort giving ports their
     *  priorities is valid. 
     */
    protected long _sortValid = -1;

    /** Decide whether the simulation should be stopped when there's no more 
     *  events in the global event queue.
     * By default, its value is 'true', meaning that the simulation will stop
     * under that circumstances. Setting it to 'false', instruct the director
     * to wait on the queue while some other threads might enqueue events in
     * it.
     */
    protected boolean _stopWhenQueueIsEmpty = true;

    /** Specify whether the director should wait for elapsed real time to
     *  catch up with model time.
     */ 
    protected boolean _synchronizeToRealTime;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Categorize the given list of actors into three kinds: sinks, sources,
    //  and transformers. 
    private void _categorizeActors(List actorList) {
        Iterator actors = actorList.listIterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof AtomicActor) {
                // Atomic actors have type.
                if (actor instanceof Source) {
                    _sourceActors.add(actor);
                } else if (actor instanceof Sink) {
                    _sinkActors.add(actor);
                } else {
                    _otherActors.add(actor);
                }
            } else {
                // Composite actors are categorized based
                // on their ports
                int numberOfInputs = actor.inputPortList().size();
                int numberOfOutputs = actor.outputPortList().size();
                if (numberOfInputs == 0) {
                    _sourceActors.add(actor);
                } else if (numberOfOutputs == 0) {
                    _sinkActors.add(actor);
                } else {
                    _otherActors.add(actor);
                }
            }
        }
    }

    // Perform topological sort on the directed graph and use the result
    // to set the depth for each actor. A new Hashtable is created each
    // time this method is called.
    private void _computeDepth() throws IllegalActionException {
        DirectedGraph dag = _constructDirectedGraph();
        // The returned directed graph may contain cycle loops.
        // Do the check in the following code.
        // NOTE: there is a possibility that the function dependency
        // analysis says no cycle loops but the actors graph have 
        // cycle loops. This is one of the limitations of the current
        // DEDirector.
        Object[] cycleNodes = dag.cycleNodes();
        if (cycleNodes.length != 0) {
            StringBuffer names = new StringBuffer();
            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) names.append(", ");
                    names.append(((Nameable)cycleNodes[i]).getFullName());
                }
            }
            throw new IllegalActionException(this.getContainer(),
                    "Found zero delay loop including: " + names.toString()
                    + "\n Model requires a TimedDelay in this " +                        "directed cycle.");
        }
        
        // now we can safely cast the dag into an acyclic graph.
        Object[] sort = 
            (Object[]) ((DirectedAcyclicGraph)dag).topologicalSort();
        if (_debugging) {
            _debug("## Result of topological sort (highest depth to lowest):");
        }
        // Allocate a new hash table with the equal to the
        // number of actors sorted + 1. The extra entry is
        // for the composite actor that contains this director.
        // This composite actor is set to the highest depth.
        _actorToDepth = new Hashtable(sort.length+1);
        if (_debugging) _debug(getContainer().getFullName(),
                "depth: " + sort.length);
        _actorToDepth.put(getContainer(), new Integer(sort.length));
        for (int i = sort.length-1; i >= 0; i--) {
            Actor actor = (Actor)sort[i];
            if (_debugging) _debug(((Nameable)actor).getFullName(),
                    "depth: " + i);
            // Insert the hashtable entry.
            _actorToDepth.put(actor, new Integer(i));
        }
        if (_debugging) _debug("## End of topological sort.");
    }

    // Construct a directed graph with the nodes representing actors and
    // directed edges representing their dependencies. The directed graph
    // is returned.
    // NOTE: SDEDirector does not need this because ports are directly used
    // for depth calculation and scheduling.
    private DirectedAcyclicGraph _constructDirectedGraph()
            throws IllegalActionException {
        // Clear the graph
        DirectedAcyclicGraph dag = new DirectedAcyclicGraph();

        Nameable container = getContainer();
        // If the container is not a composite actor,
        // there are no actors.
        if (!(container instanceof CompositeActor)) {
            return dag;
        }
        
        CompositeActor castContainer = (CompositeActor)container;

        // Get the FunctionDependency attribute of the container. 
        FunctionDependency functionDependency = 
            castContainer.getFunctionDependency();

        // NOTE: The following may be a very costly test.
        // -- from the comments of former implementation.
        // If the port based data flow graph contains directed
        // loops, the model is invalid. An IllegalActionException
        // is thrown with the names of the actors in the loop.
        Object[] cycleNodes = 
            ((FunctionDependencyOfCompositeActor)functionDependency)
                .getCycleNodes();
        if (cycleNodes.length != 0) {
            StringBuffer names = new StringBuffer();
            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) names.append(", ");
                    names.append(((Nameable)cycleNodes[i])
                            .getContainer().getFullName());
                }
            }
            throw new IllegalActionException(this.getContainer(),
                    "Found zero delay loop including: " + names.toString());
        }

        // First, get all the contained actors
        List embeddedActors = castContainer.deepEntityList();

        // initialize the list of actors
        _sinkActors = new LinkedList();
        _sourceActors = new LinkedList();
        _otherActors = new LinkedList();
        
        _categorizeActors(embeddedActors);

        // and add them into the actors graph
        // NOTE: Source acotrs are added first and the sink actors 
        // are added last. The purpose is to let the dag give the source 
        // actors higher prioprities, meaning smaller depths, during
        // the topologial sorting process.
        dag.addNodeWeights(_sourceActors);
        dag.addNodeWeights(_otherActors);
        dag.addNodeWeights(_sinkActors);
        
        // clean the list of actors to avoid potential memory leakage.
        _sinkActors = null;
        _sourceActors = null;
        _otherActors = null;

        // Next, create the directed edges by iterating the actors again.
        Iterator actors = embeddedActors.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();

            // Get the FunctionDependency attribute of current actor.
            functionDependency = actor.getFunctionDependency();
            if (functionDependency == null) {
                throw new IllegalActionException(this, "doesn't " +
                        "contain a valid FunctionDependency attribute.");
            }

            // get all the input ports of the current actor
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort)inputPorts.next();

                Set directlyDependentOutputPorts =
                    functionDependency.getDependentOutputPorts(inputPort);

                // get all the output ports of the current actor.
                Iterator outputPorts = actor.outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort) outputPorts.next();

                    if (directlyDependentOutputPorts != null &&
                            !directlyDependentOutputPorts.contains(
                                outputPort)) {
                        // Skip the port without direct dependence.
                        continue;
                    }
                    // find the inside input ports connected to outputPort
                    Iterator inPortIterator =
                        outputPort.deepConnectedInPortList().iterator();
                    int referenceDepth = outputPort.depthInHierarchy();
                    while (inPortIterator.hasNext()) {
                        IOPort port = (IOPort)inPortIterator.next();
                        if (port.depthInHierarchy() < referenceDepth) {
                            // This destination port is higher in the hierarchy.
                            // We may be connected to it on the inside,
                            // in which case, we do not want to record
                            // this link.  To check whether we are connected
                            // on the inside, we check whether the container
                            // of the destination port deeply contains
                            // source port.
                            if (((NamedObj)port.getContainer())
                                    .deepContains(outputPort)) {
                                continue;
                            }
                        }

                        Actor successor = (Actor)(port.getContainer());
                        // If the destination is the same as the current
                        // actor, skip the destination.
                        // NOTE: This situation happens when a loop of actors
                        // exists in a model. As long as the loop contains at
                        // least one actor whose output does not depend its
                        // input, such as the TimedDelay actor, the loop is
                        // valid. The following statements prevents infinite
                        // iterations of the actors in a loop.
                        if (successor.equals(actor)) {
                            continue;
                        }

                        // create an arc from the current actor to the successor.
                        if (dag.containsNodeWeight(successor)) {
                            dag.addEdge(actor, successor);
                        } else {
                            // This happens if there is a
                            // level-crossing transition.
                            throw new IllegalActionException(this,
                                    "Level-crossing transition from "
                                    + ((Nameable)actor).getFullName() + " to "
                                    + ((Nameable)successor).getFullName());
                        }
                    }
                }
            }
        }

        return dag;
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

            stopWhenQueueIsEmpty = new Parameter(this, "stopWhenQueueIsEmpty",
                    new BooleanToken(true));
            stopWhenQueueIsEmpty.setTypeEquals(BaseType.BOOLEAN);

            synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime",
                    new BooleanToken(false));
            synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

            isCQAdaptive = new Parameter(this, "isCQAdaptive",
                    new BooleanToken(true));
            isCQAdaptive.setTypeEquals(BaseType.BOOLEAN);

            minBinCount = new Parameter(this, "minBinCount",
                    new IntToken(2));
            minBinCount.setTypeEquals(BaseType.INT);

            binCountFactor = new Parameter(this, "binCountFactor",
                    new IntToken(2));
            binCountFactor.setTypeEquals(BaseType.INT);
        } catch (KernelException e) {
            throw new InternalErrorException(
                    "Cannot set parameter:\n" + e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Local copies of parameters.
    private Time _startTime;
    private Time _stopTime;
    // Sink actors
    private List _sinkActors;
    // Source actors
    private List _sourceActors;
    // Actors other than sink and source.
    private List _otherActors;

}
