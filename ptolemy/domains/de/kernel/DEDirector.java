/* A DE domain director.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating red (lmuliadi@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEDirector
//
/** This director implements the discrete-event model of computation (MoC).
 *  It should be used as the local director of a CompositeActor that is
 *  to be executed according to this MoC. This director maintains a notion
 *  of current time, and processes events chronologically in this time.
 *  An <i>event</i> is a token with a time stamp.  Much of the sophistication
 *  in this director is aimed at handling simultaneous events intelligently,
 *  so that deterministic behavior can be achieved.
 *  <p>
 *  The bottleneck in a typical DE simulator is in the mantainance of the
 *  global event queue. By default, a DE director uses the calendar queue 
 *  as the global event queue. It is currently the most efficient algorithm
 *  with O(1) time complexity in both enqueue and dequeue operations.
 *  <p>
 *  Sorting in the CalendarQueue class is done with respect to sort-keys
 *  which are implemented by the DEEventTag class. DEEventTag consists of a
 *  time stamp (double) and a depth (long). The time stamp
 *  indicates the time when the event occurs, and the depth
 *  indicates the relative priority of events with the same time stamp
 *  (i.e. simultaneous events).  The depth is determined by topologically
 *  sorting the ports according to data dependencies over which there
 *  is no time delay.
 *  <p>
 *  Several of the methods provided in this base class have two versions.
 *  One that deals with relative time (with respect to the current time) and
 *  another that deals with absolute time. While they are theoretically 
 *  equivalent, it is practically better to use the one with
 *  absolute time in case the data is already in that form. This will
 *  eliminate unnecessary quantization error. For example,
 *  if the current time is 10.0 and the actor needs to be refired at time
 *  20.0, then use fireAt(20.0) rather than fireAfterDelay(10.0).
 *  <p>
 *  Ports in the DE domain may be instances of DEIOPort. The DEIOPort class
 *  should be used whenever an actor introduces time delays between the
 *  inputs and the outputs. When ordinary IOPort is used, the scheduler
 *  assumes, for the purpose of calculating priorities, that the delay
 *  across the actor is zero. On the other hand, when DEIOPort is used,
 *  the delay across the actor is assumed to be non-zero, by default. We
 *  apologize for this potential confusion, but it is somewhat necessary for
 *  implementation efficiency in calculating receiver depths.
 *  <p>
 *  Input ports in a DE simulation contain instances of DEReceiver.
 *  When a token is put into a DEReceiver, that receiver enqueues the
 *  event by calling the _enqueueEvent() method of this director.
 *  This director sorts all such events in a global event queue
 *  (a priority queue) implemented as an instance of the CalendarQueue class.
 *  <p>
 *  Directed loops with no delay actors are not permitted; they would make
 *  impossible to assign priorities.  Such a loop can be broken by inserting
 *  an instance of the Delay actor with its delay set to zero.
 *  <p>
 *  At the beginning of the fire() method, this director dequeues
 *  the 'appropriate' oldest events (i.e. ones with smallest time
 *  stamp) from the global event queue, and puts those events into
 *  their corresponding receivers. The term 'appropriate' means that
 *  the events dequeued are chosen such that all events that are destined for
 *  the same actor and have the same time stamp are visible to that actor
 *  when it fires. That particular actor will then be called the 'firing
 *  actor'. If the oldest events are destined for multiple actors, then
 *  the choice of the firing actor is determined by the topological depth
 *  of the input ports of the actors.
 *  <p>
 *  Next step in the fire() method, the 'firing actor' is fired (i.e. its
 *  fire() method is invoked). The actor will consume events from
 *  its input port(s) and will usually produce new events on its output
 *  port(s). These new events will be enqueued in the global event queue
 *  until their time stamps equal the current time.
 *  <p>
 *  A DE domain simulation ends when the time stamp of the oldest events
 *  exceeds a preset stop time. This stopping condition is checked inside
 *  the prefire() method. It could also be ended when the global event queue
 *  becomes empty, which is the default behaviour. Sometimes, the desired
 *  behaviour is for the director to wait on an empty queue until another
 *  thread comes in and put events in it, e.g. wait for button pushes. Invoke
 *  the stopWhenQueueIsEmpty(boolean) method with <code>false</code> argument
 *  to achieve this behaviour.
 *  <p>
 *  NOTE: as mentioned before, all oldest events for the 'firing actor'
 *  are dequeued and put into the corresponding receivers. It is thus
 *  possible to have multiple simultaneous events put into the same receiver.
 *  These events will all be accessible by the actor during the firing
 *  phase, but it is not clear which one is ahead of which. This is, in fact,
 *  one source of nondeterminancy in discrete-event semantics. How to handle
 *  this is up to the designer of the actor.
 *  <p>
 *  Abstract base class for DE domain directors. In general, the methods
 *  provided in this base class are ones that do not depend on the
 *  implementation of the global event queue. This will enable different
 *  implementations to be compared in term of efficiency. The bottleneck
 *  in a typical DE simulator is in the mantainance of the global event
 *  queue. One good implementation is the calendar queue algorithm
 *  in ptolemy.actor.util.CalendarQueue class. This
 *  algorithm gives us O(1) time in both enqueue and dequeue operation.
 *  The DECQDirector class, which derives from DEDirector, uses
 *  this implementation.
 *  <p>
 *  Several of the methods provided in this base class have two versions.
 *  One that deals with relative time (with regards to the current time) and
 *  another that deal with absolute time. While it is theoretically equivalent
 *  to use one or the other, it is practically better to use the one with
 *  absolute time in case your data are already in that form. This will
 *  eliminate unnecessary quantization error, e.g. <i>A-B+B</i>. For example,
 *  if the current time is 10.0 and the actor needs to be refired at time
 *  20.0, then use fireAt(20.0) rather than fireAfterDelay(10.0).
 *  <p>
 *
 *  @author Lukito Muliadi, Edward A. Lee
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 *  @see DEDirector
 */
// FIXME:
// The topological depth of the receivers are static and computed once
// in the initialization() method. This means that mutations are not
// currently supported.
public class DEDirector extends Director {

    /** Construct a director with empty string as name in the
     *  default workspace.
     */
    public DEDirector() {
	this(null, null);
    }

    /** Construct a director with the specified name in the default
     *  workspace. If the name argument is null, then the name is set to the
     *  empty string. This director is added to the directory of the workspace,
     *  and the version of the workspace is incremented.
     *  @param name The name of this director.
     */
    public DEDirector(String name) {
	this(null, name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     *  @param name The name of this director.
     */
    public DEDirector(Workspace workspace, String name) {
        this(workspace, name, null);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     *  @param name The name of this director.
     */
    public DEDirector(Workspace workspace, 
            String name, 
            DEEventQueue eventQueue) {
	super(workspace, name);
        try {
            _stopTime = new Parameter(this,
                    "StopTime",
                    new DoubleToken(0.0));
        } catch (IllegalActionException e) {
            // shouldn't happen, because we know the Parameter class is an
            // acceptable type for this director.
            e.printStackTrace();
            throw new InternalErrorException("IllegalActionException: " +
                    e.getMessage());
        } catch (NameDuplicationException e) {
            // The name is guaranteed to be unique here..
            e.printStackTrace();
            throw new InternalErrorException("NameDuplicationException: " +
                    e.getMessage());
        }
        // Assign the appropriate event queue.
        if (eventQueue == null) {
            _eventQueue = new DECQEventQueue();
        } else {
            _eventQueue = eventQueue;
            _eventQueue.clear();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the one actor identified by the _prepareActorToFire() method
     *  as ready to fire.
     *  If there are multiple simultaneous events destined to this actor,
     *  then they will have all been dequeued from the global queue and put
     *  into the corresponding receivers.
     *  <p>
     *  The actor will be fired multiple times until it has consumed all tokens
     *  in all of its receivers that don't tolerate pending tokens. If the
     *  actor firing resulted from a 'pure event' then the actor
     *  will be fired exactly once.
     *
     *  @exception IllegalActionException If the firing actor throws it.
     */
    public void fire() throws IllegalActionException {

        boolean _timeHasNotAdvanced = true;

        while (true) {

            if (!_prepareActorToFire()) {
                return;
            }

            if (_actorToFire == getContainer()) {
                // The actor to be fired is it's container.. so it must be that
                // this director is a local director of an opaque composite
                // actor.
                if (!isEmbedded()) {
                    throw new InternalErrorException("The director of this " +
                            "composite actor doesn't " +
                            "realize that it's embedded.");
                }
                // Since the tokens is already in the right place,
                // we just return.
                return;
            }


            if (DEBUG) {
                System.out.print(getFullName() + ":");
                System.out.println("Prefiring actor: " +
                        ((Entity)_actorToFire).description(FULLNAME)+
                        " at time: " +
                        _currentTime +
                        " and returns ....");
                System.out.println("<<<");
            }


            if (_actorToFire.prefire()) {

                if (DEBUG) {
                    System.out.println(">>>");
                    System.out.println("Well... it returned true.");
                }

                // Repeatedly fire the actor until it doesn't have any
                // more filled receivers. In the case of 'pure event' the
                // actor is fired once.
                //
                boolean refire = false;

                do {
                    if (DEBUG) {
                        System.out.print(getFullName() + ":");
                        System.out.println("Firing actor: " +
                                ((Entity)_actorToFire).description(FULLNAME)+
                                " at time: " +
                                _currentTime);
                    }
                    _actorToFire.fire();
                    // check _filledReceivers to see if there are any 
                    // receivers left that are not emptied.
                    refire = false;
                    Enumeration enum = _filledReceivers.elements();
                    while (enum.hasMoreElements()) {
                        DEReceiver r = (DEReceiver)enum.nextElement();
                        if (!r._isPendingTokenAllowed() && r.hasToken()) {
                            refire = true;
                            break;
                        }
                    }
                } while (refire);
                if (!_actorToFire.postfire()) {
                    // If one actor is dead, then stop the simulation.
                    _shouldPostfireReturnFalse = true;
                }
            } else {
                if (DEBUG) {
                    System.out.println(">>>");
                    System.out.println("Well... it returned false.");
                }
                //_shouldPostfireReturnFalse = true;
            }

            // Check if the next time stamp is equal to current time.
            DEEventTag nextKey = null;
            try {
                nextKey = _eventQueue.getNextTag();
            } catch (IllegalAccessException e) {
                // the queue is empty.
                // So, just get on with the current iteration.
                break;
            }
            if (nextKey.timeStamp() > getCurrentTime()) {
                // if the next event is in the future then proceed to the
                // next iteration.
                break;
            } else if (nextKey.timeStamp() == getCurrentTime()) {
                // otherwise, the next event is at the same time, so keep
                // going.
            } else {
                throw new InternalErrorException("Bug in DEDirector."+
                        "fire(), the next event has smaller time stamp than" +
                        " the current time.");
            }

        } // while (true)

    }

    /** Schedule an actor to be fired after the specified delay. If the delay
     *  argument is equal to zero, then the actor will be refired after all
     *  actors enabled at current time are fired.
     *
     *  @param actor The actor scheduled to fire.
     *  @param delay The scheduled time to fire.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void fireAfterDelay(Actor actor, double delay)
            throws IllegalActionException {
        // FIXME: Check if the actor is in the composite actor containing this
        // director. I.e. the specified actor is under this director
        // responsibility. This could however be an expensive operation. So,
        // leave it out for now, and see if this will turn out to be an issue.

        // Check the special case, when the delay is equal to zero
        if (delay == 0 && _isInitialized) {
            this._enqueueEvent(actor, getCurrentTime(), Long.MAX_VALUE);
            return;
        }

        fireAt(actor, getCurrentTime() + delay);
    }

    /** Schedule an actor to be fired at the specified time.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If the specified time is in the past.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {

        // FIXME: Check if the actor is in the composite actor containing this
        // director. I.e. the specified actor is under this director
        // responsibility. This could however be an expensive operation. So,
        // leave it out for now, and see if this will turn out to be an issue.

        // Check the special case, when the delay is equal to zero
        if (time == getCurrentTime() && _isInitialized) {
            this._enqueueEvent(actor, getCurrentTime(), Long.MAX_VALUE);
            return;
        }

        // If this actor has input ports, then the depth is set to be
        // one higher than the max depth of the input ports.
        // If this actor has no input ports, then the depth is set to
        // to be zero.
        long maxdepth = -1;
        Enumeration iports = actor.inputPorts();
        while (iports.hasMoreElements()) {
            IOPort p = (IOPort) iports.nextElement();
            Receiver[][] r = p.getReceivers();
            if (r == null) continue;
            DEReceiver rr = (DEReceiver) r[0][0];
            if (rr._depth > maxdepth) {
                maxdepth = rr._depth;
            }
        }
        this._enqueueEvent(actor, time, maxdepth+1);
    }

    /** Return the current time of the simulation. Firing actors that need to
     *  know the current time (e.g. for calculating the time stamp of the
     *  delayed outputs) call this method.
     */
    public double getCurrentTime() {
	return _currentTime;
    }

    /** Return the time stamp of the next event in the queue with time stamp
     *  strictly greater than the current time.
     */
    public double getNextIterationTime() {
        return _nextIterationTime;
    }

    /** Return the time of the earliest event seen in the simulation.
     *  Before the simulation begins, this is java.lang.Double.MAX_VALUE.
     *  @return The start time of the simulation.
     */
    public double getStartTime() {
        return _startTime;
    }

    /** Return the stop time of the simulation, as set by setStopTime().
     *  @return The stop time of the simulation.
     */
    public double getStopTime() {
        // since _stopTime field is set in the constructor, it is guarantee
        // to be non-null.
        DoubleToken token = (DoubleToken)_stopTime.getToken();
        return token.doubleValue();
    }

    /** Set current time to zero, calculate priorities for simultaneous
     *  events, and invoke the initialize() methods of all actors deeply
     *  contained by the container.  To be able to calculate the priorities,
     *  it is essential that the graph not have a delay-free loop.  If it
     *  does, then this can be corrected by inserting a DEDelay actor
     *  with a zero-valued delay.  This has the effect of breaking the
     *  loop for the purposes of calculating priorities, without introducing
     *  a time delay.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. Actors may produce output data in their initialize()
     *  methods, or more commonly, they may schedule pure events.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If there is a delay-free loop, or
     *   if the initialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {

	// initialize the global event queue.
	_eventQueue.clear();

	// initialize the directed graph for scheduling.
	_dag = new DirectedAcyclicGraph();

        _currentTime = 0.0;
        // Haven't seen any events yet, so...
        _startTime = Double.MAX_VALUE;

	_startTimeInitialized = false;
        // Update _dag, the directed graph that indicates priorities.
        _constructDirectedGraph();
	if (!_dag.isAcyclic()) {
	    throw new IllegalActionException("Can't initialize a "+
		    "cyclic graph in DEDirector.initialize()");
	}

        _isInitialized = false;

        // Call the parent initialize method to create the receivers.
        // Some events might be scheduled in the global event queue as
        // a result of this operation.
        super.initialize();

        _isInitialized = true;

        // Set the depth field of the receivers.
        _computeDepth();

        // Request a firing to the outer director if the queue is not empty.
        if (isEmbedded() && !_eventQueue.isEmpty()) {
            _requestFiring();
        }

    }

    /** Return true if this director is embedded inside an opaque composite
     *  actor contained by another composite actor.
     *  @return True is the above condition is satisfied, false otherwise.
     */
    public boolean isEmbedded() {
        if (getContainer().getContainer() == null) {
            return false;
        } else {
            return true;
        }
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

        if (_shouldPostfireReturnFalse) {
            return false;
        } else if (isEmbedded() && !_eventQueue.isEmpty()) {
            _requestFiring();
        } 
        return true;
    }

    /** Set the stop time of the simulation.
     *  @param stopTime The new stop time.
     */
    public void setStopTime(double stopTime) {
        // since the _stopTime is field is set in the constructor,
        // it's guarantee to be non-null here.
        _stopTime.setToken(new DoubleToken(stopTime));
    }

    /** Decide whether the simulation should be stopped when there's no more
     *  events in the global event queue.
     *  By default, the value is 'true', meaning that the simulation will stop
     *  under that circumstances. Setting it to 'false', instruct the director
     *  to wait on the queue while some other threads might enqueue events in
     *  it.
     *  @param flag The new value for the flag.
     */
    public void stopWhenQueueIsEmpty(boolean flag) {
        _stopWhenQueueIsEmpty = flag;
    }

    /** Transfer data from an input port of the container to the
     *  ports it is connected to on the inside.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @param port The input port from which tokens are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    // FIXME: Maybe this can be removed and update current time differently...
    public void transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque input port.");
        }
        Receiver[][] insiderecs = port.deepGetReceivers();
        for (int i=0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                try {
                    Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        for (int j=0; j < insiderecs[i].length; j++) {
                            DEReceiver deRec =
                                (DEReceiver)insiderecs[i][j];

                            Actor container = (Actor)getContainer();
                            double outsideCurrTime = container.getExecutiveDirector().getCurrentTime();

                            deRec.put(t, outsideCurrTime - _currentTime);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "Director.transferInputs: Internal error: " +
                            ex.getMessage());
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Put a "pure event" into the event queue with the specified time stamp
     *  and depth. The depth is used to prioritize events that have equal
     *  time stamps.  A smaller depth corresponds to a higher priority.
     *  A "pure event" is one where no token is transferred.  The event
     *  is associated with a destination actor.  That actor will be fired
     *  when the time stamp of the event is the oldest in the system.
     *  Note that the actor may have no new data at its input ports
     *  when it is fired.
     *
     *  @param actor The destination actor.
     *  @param time The time stamp of the "pure event".
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(Actor actor, double time, long depth)
            throws IllegalActionException {

        // This check is only done after the start time is initialized.
        if (_startTimeInitialized) {
            if (time < _currentTime) {
                throw new IllegalActionException(getContainer(),
                        "Attempt to queue a token with a past time stamp " +
                        "after start time is fixed.");
            }
        }

        // FIXME: Provide a mechanism for listening for events.
        if (DEBUG) {
            System.out.print(getFullName() + ":");
            System.out.println("Enqueue event for actor: " +
                    ((Entity)actor).description(FULLNAME)+
                    " at time " + time +
                    " with depth = " + depth +
                    " .");
        }


        DEEventTag key = new DEEventTag(time, depth);
        DEEvent event = new DEEvent(actor, key);
        _eventQueue.put(event);
    }

    /** Put an event into the event queue with the specified destination
     *  receiver, transferred token, time stamp and depth. The depth
     *  is used to prioritize
     *  events that have equal time stamps.  A smaller depth corresponds
     *  to a higher priority.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param time The time stamp of the event.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token,
            double time, long depth) throws IllegalActionException {

        // FIXME: Should this check that the depth is not negative?
        if (time < _currentTime) {
            throw new IllegalActionException(getContainer(),
                    "Attempt to queue a token with a past time stamp = " +
                    time + ", while current time " +
                    "is equal to " + _currentTime + " .");
        }

        // FIXME: Provide a mechanism for listening for events.

        DEEventTag key = new DEEventTag(time, depth);
        DEEvent event = new DEEvent(receiver, token, key);
        if (DEBUG) {
            System.out.print(getFullName()+":");
            System.out.println("Enqueue event for port: " +
                    receiver.getContainer().description(FULLNAME)+
                    " on actor: " +
                    ((Entity)event.getDestinationActor()).description(FULLNAME) +
                    " at time " + time +
                    " with depth = " + depth +
                    " .");
        }

        _eventQueue.put(event);
    }

    /** Override the default Director implementation, because in DE
     *  domain, we don't need write access inside an iteration.
     *  @return false.
     */
    protected boolean _writeAccessPreference() {
        // Return false to let the workspace be write-protected.
        // Return true to debug the PtolemyThread.
        return false;
    } 

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The current time of the simulation.
    // Firing actors may get the current time by calling getCurrentTime()
    protected double _currentTime = 0.0;

    // Indicate whether the actors (not the director) is initialized.
    protected boolean _isInitialized = false;

    // The time of the next iteration.
    protected double _nextIterationTime;

    // Set to true when it's time to end the simulation.
    // e.g. The earliest time in the global event queue is greater than
    // the stop time.
    // FIXME: This is a hack :(
    protected boolean _shouldPostfireReturnFalse = false;

    // The time of the earliest event seen in the current simulation.
    protected double _startTime = Double.MAX_VALUE;

    // Decide whether the simulation should be stopped when there's no more
    // events in the global event queue.
    // By default, its value is 'true', meaning that the simulation will stop
    // under that circumstances. Setting it to 'false', instruct the director
    // to wait on the queue while some other threads might enqueue events in
    // it.
    protected boolean _stopWhenQueueIsEmpty = true;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////   
    // The stop time parameter.
    private Parameter _stopTime;

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    // Dequeue the next event from the event queue, advance time to its
    // time stamp, and mark the destination actor for firing.
    // If there are multiple events on the queue with the same time
    // stamp that are destined for the same actor, dequeue all of them,
    // making them available in the input ports of the destination actor.
    // The firing actor may be fired repeatedly until all its
    // receivers that don't tolerate pending tokens are empty.
    // If the time stamp is greater than the stop time then return false.
    // If there are no events on the event queue, and _stopWhenQueueIsEmpty
    // flag is true (which is set to true by default) then return false,
    // which will have the effect of stopping the simulation.
    private boolean _prepareActorToFire() {
        // During prefire, new actor will be chosen to fire
	// therefore, initialize _actorToFire field to null.

        _actorToFire = null;
	// Initialize the _filledReceivers field.
	_filledReceivers.clear();
        // FIXME: This is just temporary, to see if it works.

        DEEvent currentEvent = null;
        // Keep taking events out until there are no more simultaneous
        // events or until the queue is empty. Some events get put back
        // into the queue.  We collect those in the following fifo
        // to put them back outside the loop.
        FIFOQueue fifo = new FIFOQueue();

        while (true) {

            if (_stopWhenQueueIsEmpty) {
                try {
                    currentEvent = (DEEvent)_eventQueue.take();
                } catch (IllegalAccessException ex) {
                    // Queue is empty.
                    // The next iteration time will be equal to stop time.
                    _nextIterationTime = getStopTime();
                    break;
                }
            } else {
                // In this case, effectively, we want to do a blocking
                // take(). So, keep invoking take() until an exception
                // is not thrown.
                while (true) {
                    try {
                        currentEvent = (DEEvent)_eventQueue.take();
                    } catch (IllegalAccessException ex) {
                        synchronized(_eventQueue) {
                            try {
                                _eventQueue.wait();
                            } catch (InterruptedException e) {
                                // this shouldn't happen.
                                throw new InternalErrorException(
                                        "In DEDirector._prepareActor"+
                                        "ToFire(), a thread got " +
                                        "interrupted.");
                            }
                        }
                        continue;
                    }
                    break;
                }
            }

            if (_actorToFire == null) {
                // This is first time we're in the loop, therefore
                // always accept the event.
                _actorToFire = currentEvent.getDestinationActor();

                // Advance current time.
                _currentTime = currentEvent.getEventTag().timeStamp();

                // FIXME: The following line should happen only during the
                // first prefire(), because subsequent enqueue is
                // restricted to be ahead of _currentTime.
                // FIXME: debug structure here...
                if (_currentTime < _startTime) {
                    if (_startTimeInitialized) {
                        throw new InternalErrorException("DEDirector "+
                                "_prepareActorToFire() bug.. trying " +
                                "to initialize " +
                                "start time twice.");
                    }

                    _startTime = _currentTime;
                    _startTimeInitialized = true;
                }

                if (_currentTime > getStopTime() && !isEmbedded()) {
                    // The stopping condition is met.
                    // Note that, if this director is embedded then
                    // he doesn't determine the stopping condition, rather
                    // outer director should do that...
                    // FIXME: might be wrong approach
                    _shouldPostfireReturnFalse = true;
                    if (DEBUG) {
                        System.out.println("Stopping time is met " +
                                "in DEDirector.prefire() of " +
                                getFullName() + ".");
                    }
                    return false;
                }

                // Transfer the event to the receiver and keep track
                // of which receiver is filled.
                DEReceiver rec = currentEvent.getDestinationReceiver();
                // If rec is null, then it's a 'pure event', and there's
                // no need to put event into receiver.
                if (rec != null) {
                    // Adds the receiver to the _filledreceivers list.
                    if (!_filledReceivers.includes(rec)) {
                        _filledReceivers.insertFirst(rec);
                    }
                    // Transfer the event to the receiver.
                    rec._triggerEvent(currentEvent.getTransferredToken());
                }
            } else {
                // Not the first time through the loop; check if the event
                // has time stamp equal to previously obtained current
                // time. Then check if it's for the same actor.

                // Check whether the event occurred at current time.
                if (currentEvent.getEventTag().timeStamp() < _currentTime) {
                    throw new InternalErrorException("Event that was "+
                            "dequeued later has smaller time stamp. " +
                            "Check DEDirector for bug.");
                }
                if (currentEvent.getEventTag().timeStamp() > _currentTime) {
                    // The event has a later time stamp, so we put it back
                    fifo.put(currentEvent);
                    // Save the next iteration time, because some inner
                    // domains might require this information.
                    _nextIterationTime = currentEvent.getEventTag().timeStamp();
                    // Break the loop, since all events after this will
                    // all have time stamp later or equal to this one.
                    break;
                } else {
                    // The event has the same time stamp as the first
                    // event seen.  Check whether it is for the same actor.
                    if (currentEvent.getDestinationActor() == _actorToFire) {
                        // FIXME: Currently, this might put the event
                        // into a receiver that already has an event.
                        // The actors may not be written to look for
                        // multiple events in the same receiver.
                        // Perhaps this should check to see whether there
                        // is an event in the receiver and save this
                        // one if so.  That's still not quite right though
                        // because the event in the receiver may be an
                        // old one...
                        DEReceiver rec = currentEvent.getDestinationReceiver();
                        // if rec is null, then it's a 'pure event' and
                        // there's no need to put event into receiver.
                        if (rec != null) {
                            // Adds the receiver to the _filledreceivers
			    // list.
                            if (!_filledReceivers.includes(rec)) {
                                _filledReceivers.insertFirst(rec);
                            }
			    // Transfer the event to the receiver.
                            rec._triggerEvent(currentEvent.getTransferredToken());
                        }
                    } else {
                        // Put it back in the queue.
                        fifo.put(currentEvent);
                    }
                }
            }
        }
        // Transfer back the events from the fifo queue into the calendar
        // queue.
        while (fifo.size() > 0) {
            DEEvent event = (DEEvent)fifo.take();

            _eventQueue.put(event);
        }

        if (_actorToFire == null) {
            System.out.println("No actor to fire anymore");
            _shouldPostfireReturnFalse = true;
        } else {
            _shouldPostfireReturnFalse = false;
        }
        return _actorToFire != null;
    }



    // Construct a directed graph with the nodes representing input ports and
    // directed edges representing zero delay path.  The directed graph
    // is put in the private variable _dag, replacing whatever was there
    // before.
    private void _constructDirectedGraph() {
        LinkedList portList = new LinkedList();

        // Clear the graph
        _dag = new DirectedAcyclicGraph();

        // First, include all input ports to be nodes in the graph.
        CompositeActor container = ((CompositeActor)getContainer());
        if (container != null) {
            // get all the contained actors.
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
		// get all the input ports in that actor
		Actor actor = (Actor)allactors.nextElement();
		Enumeration allports = actor.inputPorts();
		while (allports.hasMoreElements()) {
		    IOPort port = (IOPort)allports.nextElement();
		    // create the nodes in the graph.
		    _dag.add(port);
		    portList.insertLast(port);
		}
	    }
        }

        // Next, create the directed edges.
        Enumeration copiedPorts = portList.elements();
        while (copiedPorts.hasMoreElements()) {
            IOPort ioPort = (IOPort)copiedPorts.nextElement();

	    // Find the successor of p
            if (ioPort instanceof DEIOPort) {
                DEIOPort p = (DEIOPort) ioPort;
                Enumeration befores = p.beforePorts();
                while (befores.hasMoreElements()) {
                    IOPort after = (IOPort) befores.nextElement();
                    // create an arc from p to after
                    if (_dag.contains(after)) {
                        _dag.addEdge(p, after);
                    } else {
                        // Note: Could this exception be triggered by
                        // level-crossing transitions?  In this case,
                        // we need a more reasonable way to handle it.
                        throw new InternalErrorException(
                            "Port missing from DAG.");
		    }
		}
		Enumeration triggers = p.triggersPorts();
		while (triggers.hasMoreElements()) {
		    IOPort outPort = (IOPort) triggers.nextElement();
		    // IOPort deltaInPort = _searchDeltaPort(outPort);
		    // find the input ports connected to outPort
		    Enumeration inPortEnum = outPort.deepConnectedInPorts();
		    while (inPortEnum.hasMoreElements()) {
                        IOPort pp = (IOPort)inPortEnum.nextElement();
                        // create an arc from p to pp
                        if (_dag.contains(pp)) {
			    //if (pp != deltaInPort)
			    _dag.addEdge(p,pp);
                        } else {
                            // Note: Could this exception be triggered by
                            // level-crossing transitions?  In this case,
                            // we need a more reasonable way to handle it.
			    throw new InternalErrorException(
                                "Port missing from DAG.");
			}
		    }
		}
	    } else {
		// It is not a DEIOPort, so assume zero delay actor.
		// I.e., an input triggers immediate events on all outputs.
		Enumeration triggers =
                        ((Actor)ioPort.getContainer()).outputPorts();
                while (triggers.hasMoreElements()) {
		    IOPort outPort = (IOPort) triggers.nextElement();
		    //IOPort deltaInPort = _searchDeltaPort(outPort);
                    // find out the input ports connected to outPort
                    Enumeration inPortEnum = outPort.deepConnectedInPorts();
                    while (inPortEnum.hasMoreElements()) {
                        IOPort pp = (IOPort)inPortEnum.nextElement();
                        // create an arc from p to pp
                        if (_dag.contains(pp)) {
			    //if (pp != deltaInPort)
			    _dag.addEdge(ioPort,pp);
                        } else {
                            // Note: Could this exception be triggered by
                            // level-crossing transitions?  In this case,
                            // we need a more reasonable way to handle it.
			    throw new InternalErrorException(
                                "Port missing from DAG.");
                        }
                    }
                }
	    }
        }
    }

    // Perform topological sort on the directed graph and use the result
    // to set the depth field of the DEReceiver objects.
    private void _computeDepth() {
        Object[] sort = (Object[]) _dag.topologicalSort();
        if (DEBUG) {
            System.out.println("*** Result of topological sort: ***");
        }
	for(int i=sort.length-1; i >= 0; i--) {
            IOPort p = (IOPort)sort[i];
            // FIXME: Debugging topological sort
            if (DEBUG) {
                System.out.println(p.description(FULLNAME) + ":" + i);
            }
            // FIXME: End debugging
            // set the fine levels of all DEReceiver instances in IOPort p
            // to be i
            // FIXME: should I use deepGetReceivers() here ?
            Receiver[][] r;
	    try {
                r = p.getReceivers();
            } catch (IllegalActionException e) {
                // do nothing
                // FIXME: Replace with InternalErrorException and a more
                // meaningful message.
                throw new InternalErrorException("Bug in DEDirector."+
                        "computeDepth() (3)");
            }
	    if (r == null) {
		// dangling input port..
		continue;
	    }
	    for (int j=r.length-1; j >= 0; j--) {
                for (int k=r[j].length-1; k >= 0; k--) {
                    DEReceiver der = (DEReceiver)r[j][k];
                    der._setDepth(i);
                }
            }
	}
    }

    // Request that the container of this director to be refired in the future.
    // This method is used when the director is embedded inside an opaque
    // composite actor (i.e. a wormhole in Ptolemy 0.x terminology)
    private void _requestFiring() throws IllegalActionException {

        if (DEBUG) {
            System.out.println(getFullName() + " requests firing from " +
                    ((CompositeActor)getContainer()).getExecutiveDirector().getFullName());
        }

        DEEventTag sortkey = null;

        try {
            sortkey = _eventQueue.getNextTag();
        } catch (IllegalAccessException e) {
            throw new IllegalActionException(e.getMessage());
        }
        double nextRefire = sortkey.timeStamp();

        // enqueue a refire for the container of this director.
        ((CompositeActor)getContainer()).getExecutiveDirector().fireAt((Actor)getContainer(), nextRefire);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static final boolean DEBUG = false;

    //_eventQueue: an instance of DEEventQueue is used for sorting events.
    private DEEventQueue _eventQueue;

    // variables to keep track of the objects currently firing.
    private Actor _actorToFire = null;

    // Directed Graph whose nodes represent input ports and whose
    // edges represent delay free paths.  This is used for prioritizing
    // simultaneous events.
    private DirectedAcyclicGraph _dag=null;

    // Access with insertFirst(), take().
    private LinkedList _filledReceivers = new LinkedList();

    // FIXME: debug variables
    private boolean _startTimeInitialized = false;

}
