/* A DE domain director.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.*;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.LinkedList;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// DEDirector
//
/** This director implements the discrete-event model of computation (MoC).
 *  It should be used as the local director of a CompositeActor that is
 *  to be executed according to this MoC. This director maintain a notion
 *  of current time, and processes events chronologically in this time.
 *  An <i>event</i> is a token with a time stamp.  Much of the sophistication
 *  in this director is aimed at handling simultaneous events intelligently,
 *  so that deterministic behavior can be achieved.
 *  <p>
 *  The bottleneck in a typical DE simulator is in the maintenance of the
 *  global event queue. By default, a DE director uses the calendar queue
 *  as the global event queue. This is an efficient algorithm
 *  with O(1) time complexity in both enqueue and dequeue operations.
 *  <p>
 *  Sorting in the CalendarQueue class is done according to the order
 *  defined by the DEEvent class, which implements the java.lang.Comparable
 *  interface. A DE event has a time stamp (double), a microstep (integer)
 *  and a depth (integer). The time stamp
 *  indicates the time when the event occurs, the microstep indicates the
 *  phase of execution at a fixed time, and the depth
 *  indicates the relative priority of events with the same time stamp
 *  (simultaneous events).  The depth is determined by topologically
 *  sorting the actors according to data dependencies over which there
 *  is no time delay.
 *  <p>
 *  Ports in the DE domain may be instances of DEIOPort. The DEIOPort class
 *  should be used whenever an actor introduces time delays between the
 *  inputs and the outputs. When an ordinary IOPort is used, the director
 *  assumes, for the purpose of calculating priorities, that the delay
 *  across the actor is zero. On the other hand, when DEIOPort is used,
 *  the delay across the actor can be declared to be non-zero by calling
 *  the delayTo() method.
 *  <p>
 *  Input ports in a DE model contain instances of DEReceiver.
 *  When a token is put into a DEReceiver, that receiver enqueues the
 *  event by calling the _enqueueEvent() method of this director.
 *  This director sorts all such events in a global event queue
 *  (a priority queue).
 *  <p>
 *  Directed loops with no delay actors are not permitted; they would make it
 *  impossible to assign priorities.  Such a loop can be broken by inserting
 *  an instance of the Delay actor.  If zero delay around the loop is
 *  truly required, then simply set the <i>delay</i> parameter of that
 *  actor to zero.
 *  <p>
 *  At the beginning of the fire() method, this director dequeues
 *  a subset of the oldest events (the ones with smallest time
 *  stamp, microstep, and depth) from the global event queue,
 *  and puts those events into
 *  their corresponding receivers. The actor(s) to which these
 *  events are destined are the ones to be fired.  The depth of
 *  an event is the depth of the actor to which it is destined.
 *  The depth of an actor is its position in a topological sort of the graph.
 *  The microstep is usually zero, but is incremented when a pure event
 *  is queued with time stamp equal to the current time.
 *  <p>
 *  The actor that is fired must consume tokens from
 *  its input port(s), and will usually produce new events on its output
 *  port(s). These new events will be enqueued in the global event queue
 *  until their time stamps equal the current time.  It is important that
 *  the actor actually consume tokens from its inputs, because it will
 *  be fired repeatedly until there are no more tokens in its input
 *  ports with the current time stamp.  Alternatively, if the actor
 *  returns false in prefire(), then it will not be invoked again
 *  in the same iteration even if there are events in its receivers.
 *  <p>
 *  Execution of a DE model ends when the time stamp of the oldest events
 *  exceeds a preset stop time. This stopping condition is checked inside
 *  the prefire() method of this director. By default, execution also ends
 *  when the global event queue becomes empty. Sometimes, the desired
 *  behaviour is for the director to wait on an empty queue until another
 *  thread makes new events available.  For example, a DE actor may produce
 *  events when a user hits a button on the screen. To prevent ending the
 *  execution when there are no more events, call
 *  stopWhenQueueIsEmpty() with argument <code>false</code>.
 *  <p>
 *  The DE director has a single parameter, "stopTime".  The stop
 *  time can be set using that parameter, or by directly calling
 *  setStopTime().  If the stop time is not explicitly set, then it
 *  defaults to Double.MAX_VALUE, resulting in a model that never stops.
 *  <p>
 *  This director tolerates changes to the model during execution.
 *  The change should be queued with the director or manager using
 *  requestChange().  While invoking those changes, the method
 *  invalidateSchedule() is expected to be called, notifying the director
 *  that the topology it used to calculate the priorities of the actors
 *  is no longer valid.  This will result in the priorities being
 *  recalculated the next time prefire() is invoked.
 *  <p>
 *  However, there is one subtlety.  If an actor produces events in the
 *  future via DEIOPort, then the destination actor will be fired even
 *  if it has been removed from the topology by the time the execution
 *  reaches that future time.  This may not always be the expected behavior.
 *  The Delay actor in the DE library behaves this way.
 *
 *  @author Lukito Muliadi, Edward A. Lee, Jie Liu
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 */
public class DEDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DEDirector() {
	this(null);
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
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     */
    public DEDirector(CompositeActor container , String name)
            throws IllegalActionException {
        super(container, name);
        _initParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The stop time of the model.  This parameter must contain a
     *  DoubleToken.  Its value defaults to Double.MaxValue.
     */
    public Parameter stopTime;

    /** Stop when queue is empty.  If this parameter is true, the
     *  execution of the model will be stop when the queue is empty.
     *  Its value defaults to true.
     */
    public Parameter stopWhenQueueIsEmpty;

    /** Whether the calender queue adjust its bin number at run time.
     *  If this parameter is true, the calender queue will adapt
     *  its bin number with respect to the distribution of events.
     *  This parameter is unable to be changed after the model runs.
     *  Bu default, it is true.
     */
    public Parameter isCQAdaptive;

    /** The minimum (initial) number of bins in the calender queue.
     *  This parameter is unable to be changed after the model
     *  runs.
     *  The default value is 2.
     */
    public Parameter minBinCount;

    /** The factor when adjusting the bin number. This parameter
     *  has type integer.  This parameter is unable to be
     *  changed after the model runs.  The default value is 2.
     */
    public Parameter binCountFactor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of debug listeners.
     *  If the listener is already in the set, do not add it again.
     *  @param listener The listener to which to send debug messages.
     */
    public void addDebugListener(DebugListener listener) {
        _eventQueue.addDebugListener(listener);
        super.addDebugListener(listener);
    }

    /** Update the director parameters when the attributes are changed.
     *  Only the stopWhenQueueIsEmpty parameter is effective at run time.
     *  @param attr The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *     Not thrown in this class. May be needed by derived classes.
     */
    public void attributeChanged(Attribute attr)
            throws IllegalActionException {
        if (_debugging) _debug("Updating DEDirector parameter", attr.getName());
        if (attr == stopWhenQueueIsEmpty) {
            _stopWhenQueueIsEmpty =
                ((BooleanToken)stopWhenQueueIsEmpty.getToken()).booleanValue();
        }
    }

    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            DEDirector newobj = (DEDirector)(super.clone(ws));
            newobj.stopTime = 
                (Parameter)newobj.getAttribute("stopTime");
            newobj.stopWhenQueueIsEmpty = 
                (Parameter)newobj.getAttribute("stopWhenQueueIsEmpty");
            newobj.isCQAdaptive =
                (Parameter)newobj.getAttribute("isCQAdaptive");
            newobj.minBinCount = 
                (Parameter)newobj.getAttribute("minBinCount");
            newobj.binCountFactor = 
                (Parameter)newobj.getAttribute("binCountFactor");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Disable the specified actor.  All events destined to this actor
     *  will be ignored.
     *  @param actor The actor to disable.
     */
    public void disableActor(Actor actor) {
        if (_deadActors == null) {
            _deadActors = new HashSet();
        }
        _deadActors.add(actor);
    }

    /** Return the event queue.
     *  @return The event queue.
     */
    public DEEventQueue getEventQueue() {
        return _eventQueue;
    }

    /** Advance current time to the next event in the event queue,
     *  and fire one or more actors that have events at that time.
     *  Each actor is iterated repeatedly (prefire(), fire(), postfire()),
     *  until either it has no more input tokens at the current time, or
     *  its prefire() method returns false. If there are no events in the
     *  event queue, then the behavior depends on whether
     *  stopWhenQueueIsEmpty() has been called.  If it has, and was given
     *  the argument false, then this thread will stall until inputs
     *  become available on the input queue.  Otherwise, time will advance
     *  to the stop time and the execution will halt.
     *
     *  @exception IllegalActionException If the firing actor throws it.
     */
    public void fire() throws IllegalActionException {

        boolean _timeHasNotAdvanced = true;
        while (true) {
            Actor actorToFire = _dequeueEvents();
            if (actorToFire == null) {
                // There is nothing more to do.
                if (_debugging) _debug("No more events on the event queue.");
                _noMoreActorsToFire = true;
                return;
            }
            // It is possible that the next event to be processed is on
            // an inside receiver of an output port of an opaque composite
            // actor containing this director.  In this case, we simply
            // return, giving the outside domain a chance to react to
            // event.
            if (actorToFire == getContainer()) {
                return;
            }
            // Repeatedly fire the actor until there are no more input
            // tokens available, or until prefire() return false.
            boolean refire = false;
            do {
                if (_debugging) {
                    _debug("Iterating actor", ((Entity)actorToFire).getName(),
                            "at time ", Double.toString(getCurrentTime()));
                }
                if (!actorToFire.prefire()) {
                    if (_debugging) _debug("Prefire returned false.");
                    break;
                }
                actorToFire.fire();
                if (!actorToFire.postfire()) {
                    if (_debugging) _debug("Postfire returned false:",
                            ((Entity)actorToFire).getName());
                    // Actor requests that it not be fired again.
                    disableActor(actorToFire);
                }
                // Check the input ports of the actor see whether there
                // is additional input data available.
                refire = false;
                Iterator inputPorts = actorToFire.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort port = (IOPort)inputPorts.next();
                    for (int i = 0; i < port.getWidth(); i++) {
                        if (port.hasToken(i)) {
                            refire = true;
                            break;
                        }
                    }
                    if (refire == true) break;
                }
            } while (refire);

            // Check whether the next time stamp is equal to current time.
            try {
                DEEvent next = _eventQueue.get();
                // If the next event is in the future, proceed to postfire().
                if (next.timeStamp() > getCurrentTime()) break;
                else if (next.timeStamp() < getCurrentTime()) {
                    throw new InternalErrorException(
                            "fire(): the next event has smaller time stamp" +
                            " than the current time!");
                }
            } catch (IllegalActionException e) {
                // The queue is empty. Proceed to postfire().
                break;
            }
        }
    }

    /** Schedule an actor to be fired at the specified time.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If the specified time is in the past.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {

        // NOTE: This does not check whether the actor is in the
        // composite actor containing this
        // director. I.e. the specified actor is under this director
        // responsibility. This error would be fairly hard to make,
        // so we don't check for it here.

        // Set the depth equal to the depth of the actor.
        _enqueueEvent(actor, time);
    }

    /** Return the time stamp of the next event in the queue with time stamp
     *  strictly greater than the current time.  If there is nothing on
     *  the event queue, then return the stop time.
     *  @return The next larger time on the event queue.
     */
    public double getNextIterationTime() {
        try {
            DEEvent next = _eventQueue.get();
            double nextTime = next.timeStamp();
            // The next event on the queue may have the current time.
            // May need to look deeper in the queue.
            // Save items to reinsert into queue.
            LinkedList eventsToPutBack = new LinkedList();
            while (nextTime <= getCurrentTime()) {
                if(_debugging) _debug("temporarily remove event.");
                eventsToPutBack.add(_eventQueue.take());
                next = _eventQueue.get();
                nextTime = next.timeStamp();
            }
            // Put back events that need to be put back.
            Iterator events = eventsToPutBack.iterator();
            while (events.hasNext()) {
                if(_debugging) _debug("put dequeue current event back.");
                _eventQueue.put((DEEvent)events.next());
            }
            return nextTime;
        } catch (IllegalActionException e) {
            // The queue is empty.
            //System.out.println(e.getMessage());
            return getStopTime();
        }
    }

    /** Return the time of the earliest event seen in the model.
     *  Before execution begins, this is java.lang.Double.MAX_VALUE.
     *  @return The start time of the execution.
     */
    public double getStartTime() {
        return _startTime;
    }

    /** Return the stop time of the execution as set by setStopTime().
     *  @return The stop time of the execution.
     */
    public double getStopTime() {
        try {
            return ((DoubleToken)(stopTime.getToken())).doubleValue();
        } catch (IllegalActionException e) {
            throw new InternalErrorException(
                    "Cannot read stopTime parameter:\n" +
                    e.getMessage());
        }
    }

    /** Invoke the initialize() method of each deeply contained actor,
     *  and then check the event queue for any events. If there are any,
     *  and the director is embedded in an opaque composite actor, then
     *  request a firing of the outside director.
     *  This method should be invoked once per execution, after the
     *  initialization phase, but before any iteration.  Since type
     *  resolution has been completed, the initialize() method of a contained
     *  actor may produce output or schedule events.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Request a firing to the outer director if the queue is not empty.
        if (_isEmbedded() && !_eventQueue.isEmpty()) {
            _requestFiring();
        }
        //if (_debugging) _debug(getContainer().description());
    }

    /** Indicate that the topological depth of the ports in the model may
     *  no longer be valid. This method should be called when topology
     *  changes are made.  It sets a flag which will cause the topological
     *  sort to be redone next time prefire() is called.
     */
    public void invalidateSchedule() {
        _sortValid = false;
    }

    /** Return a new receiver of a type DEReceiver.
     *  @return A new DEReceiver.
     */
    public Receiver newReceiver() {
	return new DEReceiver();
    }

    /** Return false when the base class method return false, else request
     *  firing from outer domain (if embedded) then return true.
     *  @exception IllegalActionException If super.postfire() throws it.
     */
    public boolean postfire() throws IllegalActionException {

        if (_noMoreActorsToFire) {
            return false;
        } else if (_isEmbedded() && !_eventQueue.isEmpty()) {
            _requestFiring();
        }
        return true;
    }

    /** Set current time to zero, invoke the preinitialize() methods of
     *  all actors deeply contained by the container, and calculate
     *  priorities for simultaneous events.
     *  To be able to calculate the priorities,
     *  it is essential that the graph not have a delay-free loop.  If it
     *  does, then this can be corrected by inserting a DEDelay actor
     *  with a zero-valued delay.  This has the effect of breaking the
     *  loop for the purposes of calculating priorities, without introducing
     *  a time delay.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. Actors may produce output data in their preinitialize()
     *  methods, or more commonly, they may schedule pure events.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If there is a delay-free loop, or
     *   if the preinitialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _eventQueue = new DECQEventQueue(
                ((IntToken)minBinCount.getToken()).intValue(),
                ((IntToken)binCountFactor.getToken()).intValue(),
                ((BooleanToken)isCQAdaptive.getToken()).booleanValue());
        _deadActors = null;
        _currentTime = 0.0;
        _noMoreActorsToFire = false;
        _microstep = 0;

        // Haven't seen any events yet, so...
        _startTime = Double.MAX_VALUE;


        // Call the parent preinitialize method to create the receivers.
        super.preinitialize();
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     */
    public void removeDebugListener(DebugListener listener) {
        _eventQueue.removeDebugListener(listener);
        super.removeDebugListener(listener);
    }

    /** Set the stop time for the execution.
     *  @param stopTime The new stop time.
     */
    public void setStopTime(double time) {
        try {
            if (stopTime == null) {
                stopTime = new Parameter(this, "stopTime");
                stopTime.setTypeEquals(BaseType.DOUBLE);
            }
            stopTime.setToken(new DoubleToken(time));
        } catch (KernelException e) {
            throw new InternalErrorException(
                    "Cannot set stopTime parameter:\n" +
                    e.getMessage());
        }
    }

    /** Specify whether the simulation should be stopped when there are no
     *  more events in the event queue. By default, an execution will stop
     *  in that case. Calling this method with a <i>false</i> argument
     *  causes the director to wait on the queue in the fire() method when
     *  it discovers that the queue is empty.  Another thread must insert
     *  an event into the queue to get the director going again.  This would
     *  be typically used when events are generated at a user interface.
     *  @param flag False to prevent the director from halting when there are
     *   no more events.
     */
    public void stopWhenQueueIsEmpty(boolean flag) {
        _stopWhenQueueIsEmpty = flag;
    }

    /** Advance current time to the current time of the executive director,
     *  and then call the superclass method.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @return True if data are transferred.
     *  @param port The input port from which tokens are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port, or if the current time of the executive director
     *   is in the past.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        Actor container = (Actor)getContainer();
        double outsideCurrTime =
            container.getExecutiveDirector().getCurrentTime();
        if (outsideCurrTime < getCurrentTime()) {
            throw new IllegalActionException(this,
                    "Received an event in the past at "
                    + "an opaque composite actor boundary.");
        }
        setCurrentTime(outsideCurrTime);
        return super.transferInputs(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Put a pure event into the event queue with the specified time stamp.
     *  A "pure event" is one with no token, used to request
     *  a firing of the specified actor.
     *  Note that the actor may have no new data at its input ports
     *  when it is fired.
     *  The depth for the queued event is equal to the depth of the actor.
     *  A smaller depth corresponds to a higher priority.
     *  The microstep for the queued event is equal to zero,
     *  unless the time is equal to the current time.
     *  If it is, then the event is queued with the current microstep
     *  plus one.
     *
     *  @param actor The destination actor.
     *  @param time The time stamp of the "pure event".
     *  @exception IllegalActionException If the time argument is in the past.
     */
    protected void _enqueueEvent(Actor actor, double time)
            throws IllegalActionException {

        int microstep = 0;
        if (_startTime != Double.MAX_VALUE) {
            // At least one firing has occurred, so current time has
            // some meaning.
            if (time == getCurrentTime()) {
                microstep = _microstep + 1;
            } else if ( time < getCurrentTime()) {
                throw new IllegalActionException((Entity)actor,
                        "Attempt to queue an event in the past.");
            }
        }
        int depth = _getDepth(actor);
        if(_debugging) _debug("enqueue a pure event: ",
                ((NamedObj)actor).getName(),
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(actor, time, microstep, depth));
    }

    /** Put an event into the event queue with the specified destination
     *  receiver, token, and time stamp. The depth of the event is the
     *  depth of the actor that has the receiver.
     *  A smaller depth corresponds
     *  to a higher priority.  The microstep is always equal to zero,
     *  unless the time argument is equal to the current time, in which
     *  case, the microstep is equal to the current microstep (determined
     *  by the last dequeue, or zero if there has been none).
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param time The time stamp of the event.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token,
            double time) throws IllegalActionException {

        int microstep = 0;
        if (_startTime != Double.MAX_VALUE) {
            // At least one firing has occurred, so current time has
            // some meaning.
            if (time == getCurrentTime()) {
                microstep = _microstep;
            } else if ( time < getCurrentTime()) {
                Nameable destination = receiver.getContainer();
                throw new IllegalActionException(destination,
                        "Attempt to queue an event in the past.");
            }
        }
        Actor destn = (Actor)(receiver.getContainer()).getContainer();
        int depth = _getDepth(destn);
        if(_debugging) _debug("enqueue event: to",
                receiver.getContainer().getName()+ " ("+token.toString()+") ",
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(receiver, token, time, microstep, depth));
    }

    /** Put an event into the event queue with the specified destination
     *  receiver, and token.
     *  The time stamp of the event is the
     *  current time, but the microstep is one larger than the current
     *  microstep. The depth is the depth of the actor.
     *  This method is used by actors that declare that they
     *  introduce delay, but where the value of the delay is zero.
     *  This method must not be used before any firings have occurred
     *  (i.e. in the initialize() method) because current time has no
     *  meaning there.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative, or if
     *   current time has not been set.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token)
            throws IllegalActionException {

        if (_startTime == Double.MAX_VALUE) {
            Nameable destination = receiver.getContainer();
            throw new IllegalActionException(destination, "Attempt to queue an"
                    + " event at the current time before current time is set.");
        }
        Actor destn = (Actor)(receiver.getContainer()).getContainer();
        int depth = _getDepth(destn);
        _eventQueue.put(new DEEvent(receiver, token,
                getCurrentTime(), _microstep + 1, depth));
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Dequeue the events from the event queue that have the smallest
    // time stamp and depth. Advance the model time to their
    // time stamp, and mark the destination actor for firing.
    // If the time stamp is greater than the stop time then return null.
    // If there are no events on the event queue, and _stopWhenQueueIsEmpty
    // flag is true (which is set to true by default) then return null,
    // which will have the effect of stopping the simulation.
    // If _stopWhenQueueIsEmpty is false and the queue is empty, then
    // stall the current thread by calling wait() on the _eventQueue
    // until there are events available.
    //
    private Actor _dequeueEvents() {
        Actor actorToFire = null;
        DEEvent currentEvent = null, nextEvent = null;
        int currentDepth = 0;

        // Keep taking events out until there are no more event with the same
        // tag or until the queue is empty.
        while (true) {
            // Get the next event off the event queue.
            if (_stopWhenQueueIsEmpty) {
                try {
                    nextEvent = (DEEvent)_eventQueue.get();
                } catch (IllegalActionException ex) {
                    // Nothing more to read from queue.
                    break;
                }
            } else {
                // In this case we want to do a blocking read of the queue.
                while (_eventQueue.isEmpty()) {
                    if (_debugging) {
                        _debug("Queue is empty. Waiting for input events.");
                    }
                    synchronized(_eventQueue) {
                        try {
                            _eventQueue.wait();
                        } catch (InterruptedException e) {
                            // ignore... Keep waiting
                        }
                    }
                }
                try {
                    nextEvent = (DEEvent)_eventQueue.get();
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(ex.toString());
                }
            }

            if (actorToFire == null) {
                // No previously seen event at this tag, so
                // always accept the event.  Consume it from the queue.
                try {
                    _eventQueue.take();
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(ex.toString());
                }
                currentEvent = nextEvent;
                actorToFire = currentEvent.actor();

                if (_deadActors != null && _deadActors.contains(actorToFire)) {
                    // This actor has requested that it not be fired again.
                    if (_debugging) _debug("Skipping actor: ",
                            ((Entity)actorToFire).getFullName());
                    actorToFire = null;
                    continue;
                }

                double currentTime = currentEvent.timeStamp();
                // Advance current time.
                if (_debugging) _debug("******* Setting current time to: ",
                        Double.toString(currentTime));
                try {
                    setCurrentTime(currentTime);
                } catch (IllegalActionException ex) {
                    // Thrown if time moves backwards.
                    throw new InternalErrorException(ex.toString());
                }

                // Note: The following comparison is true
                // only during the first firing, before the start time
                // is initialized to the smallest time stamp in the
                // event queue.
                if (currentTime < _startTime) {
                    _startTime = currentTime;
                }

                currentDepth = currentEvent.depth();
                _microstep = currentEvent.microstep();

                if (currentTime > getStopTime()) {
                    if (_debugging) {
                        _debug("Current time has passed the stop time.");
                    }
                    return null;
                }

                // Transfer the event to the receiver and keep track
                // of which receiver is filled.
                DEReceiver rec = currentEvent.receiver();

                // If rec is null, then it's a 'pure event', and there's
                // no need to put event into receiver.
                if (rec != null) {
                    // Transfer the event to the receiver.
                    if(_debugging) _debug(getName(),
                            "put trigger event to",
                            rec.getContainer().getName());
                    rec._triggerEvent(currentEvent.token());
                }
            } else {
                // Already seen an event.
                // Check whether the next event has equal tag.
                // If so, the destination actor should
                // be the same, but check anyway.
                if (nextEvent.isSimultaneousWith(currentEvent) &&
                        nextEvent.actor() == currentEvent.actor()) {
                    // Consume the event from the queue.
                    try {
                        _eventQueue.take();
                    } catch (IllegalActionException ex) {
                        throw new InternalErrorException(ex.toString());
                    }

                    // Transfer the event into the receiver.
                    DEReceiver rec = nextEvent.receiver();
                    // If rec is null, then it's a 'pure event' and
                    // there's no need to put event into receiver.
                    if (rec != null) {
                        // Transfer the event to the receiver.
                        rec._triggerEvent(nextEvent.token());
                    }
                } else {
                    // Next event has a future tag or different destination.
                    break;
                }
            }
        }
        return actorToFire;
    }

    // Construct a directed graph with the nodes representing actors and
    // directed edges representing dependencies.  The directed graph
    // is returned.
    private DirectedAcyclicGraph _constructDirectedGraph()
            throws IllegalActionException {
        // Clear the graph
        DirectedAcyclicGraph dag = new DirectedAcyclicGraph();

        CompositeActor container = ((CompositeActor)getContainer());

        // If there is no container, there are no actors.
        if (container == null) return dag;

        // First, include all actors as nodes in the graph.
        // get all the contained actors.
        Iterator actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            dag.add(actors.next());
        }

        // Next, create the directed edges by iterating again.
        actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            // get all the input ports in that actor
            Iterator ports = actor.inputPortList().iterator();
            while (ports.hasNext()) {
                IOPort inputPort = (IOPort)ports.next();

                Set delayPorts = null;
                if (inputPort instanceof DEIOPort) {
                    DEIOPort dePort = (DEIOPort) inputPort;
                    delayPorts = dePort.getDelayToPorts();
                }

                // Find the successor of the port.
                Iterator triggers =
                    ((Actor)inputPort.getContainer()).outputPortList().iterator();
                while (triggers.hasNext()) {
                    IOPort outPort = (IOPort) triggers.next();

                    if (delayPorts != null && delayPorts.contains(outPort)) {
                        // Skip this port since there is a declared delay.
                        continue;
                    }
                    // find the input ports connected to outPort
                    Iterator inPortIter =
                        outPort.deepConnectedInPortList().iterator();
                    while (inPortIter.hasNext()) {
                        IOPort pp = (IOPort)inPortIter.next();
                        Actor destination = (Actor)(pp.getContainer());
                        if(destination.equals(actor)) {
                            throw new IllegalActionException(this,
                                    "Zero delay self-loop on actor: "
                                    + ((Nameable)actor).getFullName());
                        }
                        // create an arc from this actor to the successor.
                        if (dag.contains(destination)) {
                            dag.addEdge(actor, destination);
                        } else {
                            // This happens if there is a
                            // level-crossing transition.
                            throw new IllegalActionException(this,
                                    "Level-crossing transition from "
                                    + ((Nameable)actor).getFullName() + " to "
                                    + ((Nameable)destination).getFullName());
                        }
                    }
                }
            }
        }
        // NOTE: The following may be a very costly test, which is why
        // it it done at the end.  However, this means that we cannot
        // report an actor in the directed cycle.  Probably DirectedGraph
        // should be modified to enable such reporting.
        if (!dag.isAcyclic()) {
            Object[] cycleNodes = dag.cycleNodes();
            StringBuffer names = new StringBuffer();
            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) names.append(", ");
                    names.append(((Nameable)cycleNodes[i]).getFullName());
                }
            }
            throw new IllegalActionException(this,
                    "Found zero delay loop including: " + names.toString());
        }
        return dag;
    }

    // Perform topological sort on the directed graph and use the result
    // to set the depth for each actor. A new Hashtable is created each
    // time this method is called.
    private void _computeDepth() throws IllegalActionException {
        DirectedAcyclicGraph dag = _constructDirectedGraph();
        Object[] sort = (Object[]) dag.topologicalSort();
        if (_debugging) {
            _debug("## Result of topological sort (highest depth to lowest):");
        }
        // Allocate a new hash table with the equal to the
        // number of actors sorted + 1. The extra entry is
        // for the composite actor that contains this director.
        // This composite actor is set to the highest depth.
        _actorToDepth = new Hashtable(sort.length+1);
        if (_debugging) _debug(((Nameable)getContainer()).getFullName(),
                "depth: " + sort.length);
        _actorToDepth.put(getContainer(), new Integer(sort.length));
	for(int i = sort.length-1; i >= 0; i--) {
            Actor actor = (Actor)sort[i];
            if (_debugging) _debug(((Nameable)actor).getFullName(),
                    "depth: " + i);
            // Insert the hashtable entry.
            _actorToDepth.put(actor, new Integer(i));
	}
        if (_debugging) _debug("## End of topological sort.");
        // the sort is now valid.
        _sortValid = true;
    }

    // Return the depth of the actor. Throws IllegalActionException
    // if the actor is not in the table.
    private int _getDepth(Actor actor) throws IllegalActionException {
        if (!_sortValid) {
            _computeDepth();
        }
        Integer depth = (Integer)_actorToDepth.get(actor);
        if (depth != null) {
            return depth.intValue();
        }
        throw new IllegalActionException("Attempt to get depth of actor " +
                "that was not sorted.");
    }

    // initialize parameters. Set all parameters to their default
    // values.
    private void _initParameters() {
        try {
            stopTime = new Parameter(this, "stopTime",
                    new DoubleToken(Double.MAX_VALUE));
	    stopTime.setTypeEquals(BaseType.DOUBLE);
           
	    stopWhenQueueIsEmpty = new Parameter(this, "stopWhenQueueIsEmpty",
                    new BooleanToken(true));
            stopWhenQueueIsEmpty.setTypeEquals(BaseType.BOOLEAN);
           
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
                    "Cannot set stopTime parameter:\n" +
                    e.getMessage());
        }
    }

    // Request that the container of this director be refired in the future.
    // This method is used when the director is embedded inside an opaque
    // composite actor (i.e. a wormhole in Ptolemy Classic terminology).
    private void _requestFiring() throws IllegalActionException {
        DEEvent nextEvent = null;
        nextEvent = _eventQueue.get();

        if (_debugging) _debug("Request refiring of opaque composite actor.");
        // Enqueue a refire for the container of this director.
        ((CompositeActor)getContainer()).getExecutiveDirector().fireAt(
                (Actor)getContainer(), nextEvent.timeStamp());
    }

    // Return true if this director is embedded inside an opaque composite
    // actor contained by another composite actor.
    private boolean _isEmbedded() {
        return (getContainer().getContainer() != null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The queue used for sorting events.
    private DEEventQueue _eventQueue;

    // The current microstep.
    private int _microstep = 0;

    // Set to true when it's time to end the execution.
    private boolean _noMoreActorsToFire = false;

    // The time of the earliest event seen in the current simulation.
    private double _startTime = Double.MAX_VALUE;

    // Decide whether the simulation should be stopped when there's no more
    // events in the global event queue.
    // By default, its value is 'true', meaning that the simulation will stop
    // under that circumstances. Setting it to 'false', instruct the director
    // to wait on the queue while some other threads might enqueue events in
    // it.
    private boolean _stopWhenQueueIsEmpty = true;

    // The set of actors that have returned false in their postfire() methods.
    // Events destined for these actors are discarded and the actors are
    // never fired.
    private Set _deadActors;

    // Indicator of whether the topological sort giving ports their
    // priorities is valid.
    private boolean _sortValid = false;

    // A Hashtable stores the mapping of each actor to its depth.
    private Hashtable _actorToDepth = null;
}
