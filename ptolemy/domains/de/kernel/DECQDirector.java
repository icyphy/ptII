/* A DE director that uses the CalendarQueue class for scheduling.

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
import ptolemy.graph.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DECQDirector
//
/** This director implements the discrete-event model of computation (MoC).
 *  It should be used as the local director of a CompositeActor that is
 *  to be executed according to this MoC.  This director maintains a notion
 *  of current time, and processes events chronologically in this time.
 *  An <i>event</i> is a token with a time stamp.  Much of the sophistication
 *  in this director is aimed at handling simultaneous events intelligently,
 *  so that deterministic behavior can be achieved.
 *  <p>
 *  Input ports in a DE simulation are given instances of DEReceiver.
 *  When a token is put into a DEReceiver, that receiver enqueues the
 *  event by calling the enqueueEvent() method of this director.
 *  This director sorts all such events in a global event queue
 *  (a priority queue) implemented as an instance of the CalendarQueue class.
 *  <p>
 *  Sorting in the CalendarQueue class is done with respect to sort-keys
 *  which are implemented by the DESortKey class. DESortKey consists of a
 *  time stamp (double) and a depth (long). The time stamp
 *  indicates the time when the event occurs, and the depth
 *  indicates the relative priority of events with the same time stamp
 *  (i.e. simultaneous events).  The depth is determined by topologically
 *  sorting the ports according to data dependencies over which there
 *  is no time delay.
 *  <p>
 *  Ports in the DE domain may be instances of DEIOPort. The DEIOPort class
 *  should be used whenever an actor introduces time delays between the
 *  inputs and the outputs. When ordinary IOPort is used, the scheduler
 *  assumes, for the purpose of calculating priorities, that the delay
 *  across the actor is zero.
 *  <p>
 *  Directed loops with no delay actors are not permitted; they would make
 *  impossible to assign priorities.  Such a loop can be broken with an
 *  instance of the Delay actor with its delay set to zero.
 *  <p>
 *  On invocation of the prefire() method, this director dequeues
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
 *  In the fire() method, the 'firing actor' is fired (i.e. its fire() method
 *  is invoked). The actor will consume events from
 *  its input port(s) and will usually produce new events on its output
 *  port(s). These new events will be enqueued in the global event queue
 *  until their time stamps equal the current time.
 *  <p>
 *  A DE domain simulation ends when the time stamp of the oldest events
 *  exceeds a preset stop time. This stopping condition is checked inside
 *  the prefire() method.
 *  <p>
 *  NOTE: as mentioned before, all oldest events for the 'firing actor'
 *  are dequeued and put into the corresponding receivers. It is thus
 *  possible to have multiple simultaneous events put into the same receiver.
 *  These events will all be accessible by the actor during the firing
 *  phase, but it is not clear which one is ahead of which. This is, in fact,
 *  one source of nondeterminancy in discrete-event semantics. How to handle
 *  this is up to the designer of the actor.
 *
 *  @author Lukito Muliadi, Edward A. Lee
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 */
// FIXME:
// The topological depth of the receivers are static and computed once
// in the initialization() method. This means that mutations are not
// currently supported.
public class DECQDirector extends DEDirector {
    
    /** Construct a director with empty string as name in the
     *  default workspace.
     */
    public DECQDirector() {
        super();
    }
    
    /** Construct a director with the specified name in the default
     *  workspace. If the name argument is null, then the name is set to the
     *  empty string. This director is added to the directory of the workspace,
     *  and the version of the workspace is incremented.
     *  @param name The name of this director.
     */
    public DECQDirector(String name) {
        super(name);
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
    public DECQDirector(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Put a "pure event" into the event queue with the specified delay and
     *  depth. The time stamp of the event is the current time plus the
     *  delay.  The depth is used to prioritize events that have equal
     *  time stamps.  A smaller depth corresponds to a higher priority.
     *  A "pure event" is one where no token is transfered.  The event
     *  is associated with a destination actor.  That actor will be fired
     *  when the time stamp of the event is the oldest in the system.
     *  Note that the actor may have no new data at its input ports
     *  when it is fired.
     *
     *  @param actor The destination actor.
     *  @param delay The delay, relative to current time.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void enqueueEvent(Actor actor, double delay, long depth)
            throws IllegalActionException {

        // FIXME: Should this check that the depth is not negative?
        if (delay < 0.0) throw new IllegalActionException(getContainer(),
        "Attempt to queue a token with a past time stamp.");

        // FIXME: Provide a mechanism for listening for events.

        DESortKey key = new DESortKey(_currentTime + delay, depth);
        DEEvent event = new DEEvent(actor, key);
        _cQueue.put(key, event);
    }

    /** Put a token into the event queue with the specified destination
     *  receiver, delay and depth. The time stamp of the token is the
     *  current time plus the delay.  The depth is used to prioritize
     *  events that have equal time stamps.  A smaller depth corresponds
     *  to a higher priority.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param delay The delay, relative to current time.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void enqueueEvent(DEReceiver receiver, Token token,
            double delay, long depth) throws IllegalActionException {

        // FIXME: Should this check that the depth is not negative?
        if (delay < 0.0) throw new IllegalActionException(getContainer(),
        "Attempt to queue a token with a past time stamp.");

        // FIXME: Provide a mechanism for listening for events.

        DESortKey key = new DESortKey(_currentTime + delay, depth);
        DEEvent event = new DEEvent(receiver, token, key);
        _cQueue.put(key, event);
    }

    /** Fire the one actor identified by the prefire() method as ready to fire.
     *  If there are multiple simultaneous events destined to this actor,
     *  then they will have all been dequeued from the global queue and put 
     *  into the corresponding receivers.
     *  <p>
     *  The actor will be fired multiple times until it has consumed all tokens
     *  in all of its receivers. If the firing actor resulted from a 'pure
     *  event' then the actor will be fired exactly once.
     */
    public void fire()
	 throws CloneNotSupportedException, IllegalActionException {
		
	// Repeatedly fire the actor until it doesn't have any more filled 
	// receivers. In the case of 'pure event' the actor is fired once.
	// 
	boolean refire = false;
        /*
        System.out.println("Firing actor: " + 
                ((Nameable)_actorToFire).description(FULLNAME|CLASSNAME) + 
                " at time: " + 
                _currentTime);
        */
        System.out.println("Firing actor: " +
                ((Entity)_actorToFire).description(FULLNAME|CLASSNAME) +
                           " at time: " +
                           _currentTime);
	do {
	    _actorToFire.fire();
	    // check _filledReceivers to see if there's any receivers left
	    // that's not emptied.
	    refire = false;
	    Enumeration enum = _filledReceivers.elements();
	    while (enum.hasMoreElements()) {
		DEReceiver r = (DEReceiver)enum.nextElement();
		if (r.hasToken()) {
		    refire = true;
		}
	    }
	} while (refire);
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
     *  @exception CloneNotSupportedException If the initialize() method of the
     *   container or one of the deeply contained actors throws it.
     *  @exception IllegalActionException If there is a delay-free loop, or
     *   if the initialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void initialize()
            throws CloneNotSupportedException, IllegalActionException {
        
        // FIXME: Something weird going on here with respect to the
        // order of method invocation.

	// initialize the global event queue.
	_cQueue = new CalendarQueue(new DECQComparator());
        
	// initialize the directed graph for scheduling.
	_dag = new DirectedAcyclicGraph();
	// FIXME: why current time is started to be 0.0 ???
        _currentTime = 0.0;
        // Haven't seen any events yet, so...
        _startTime = Double.MAX_VALUE;
	// FIXME: _startTimeInitialized is only used for bug catching..
	// FIXME: It could be removed later...
	_startTimeInitialized = false;
        // Update _dag, the directed graph that indicates priorities.
        _constructDirectedGraph();
	if (!_dag.isAcyclic()) {
	    throw new IllegalActionException("Can't initialize a "+
		    "cyclic graph in DECQDirector.initialize()");
	}
        // Call the parent initialize method to create the receivers.
        super.initialize();
        // Set the depth field of the receivers.
        _computeDepth();
    }

    /** Invoke the base class prefire() method, and if it returns true,
     *  dequeue the next event from the event queue, advance time to its
     *  time stamp, and mark its destination actor for firing.
     *  If there are multiple events on the queue with the same time
     *  stamp that are destined for the same actor, dequeue all of them,
     *  making them available in the input ports of the destination actor.
     *  If the time stamp is greater than the stop time, or there are no
     *  events on the event queue, then return false,
     *  which will have the effect of stopping the simulation.
     *
     *  @return True if there is an actor to fire.
     *  @exception CloneNotSupportedException If the base class throws it.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    // FIXME: This isn't quite right in that it may put multiple simultaneous
    // events into the same receiver.  Actors are unlikely to be written
    // in such a way as to look for these.  Perhaps such actors need to
    // be fired repeatedly?
    public boolean prefire()
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
	// During prefire, new actor will be chosen to fire
	// therefore, initialize _actorToFire field.

        _actorToFire = null;
	// Initialize the _filledReceivers field.
	_filledReceivers.clear();
	if (super.prefire()) {

            DEEvent currentEvent = null;
            // Keep taking events out until there are no more simultaneous
            // events or until the queue is empty. Some events get put back
            // into the queue.  We collect those in the following fifo
            // to put them back outside the loop.
            FIFOQueue fifo = new FIFOQueue();
            while (true) {
                try {
                    currentEvent = (DEEvent)_cQueue.take();
                } catch (IllegalAccessException ex) {
                    // Queue is empty.
                    break;
                }
                if (_actorToFire == null) {
                    // This is first time we're in the loop, therefore always
		    // accept the event.
                    _actorToFire = currentEvent.actor;
                    
                    // Advance current time.
                    _currentTime = currentEvent.key.timeStamp();

		    // FIXME: The following line should happen only during the 
		    // first prefire(), because subsequent enqueue is 
		    // restricted to be ahead of _currentTime.
		    // FIXME: debug structure here...
		    if (_currentTime < _startTime) {
			if (_startTimeInitialized) {
			    throw new InternalErrorException("DECQDirector "+
				    "prefire bug.. trying to initialize " +
				    "start time twice.");
			}

			_startTime = _currentTime;
			_startTimeInitialized = true;
		    }

                    if (_currentTime > _stopTime) {
			// The stopping condition is met.
			return false;
		    }

                    // Transfer the event to the receiver and keep track
		    // of which receiver is filled.
                    DEReceiver rec = currentEvent.receiver;
		    // If rec is null, then it's a 'pure event', and there's
		    // no need to put event into receiver.
                    if (rec != null) {
			// Adds the receiver to the _filledreceivers list.
			if (!_filledReceivers.includes(rec)) {
			    _filledReceivers.insertFirst(rec);
			}
			// Transfer the event to the receiver.
                        rec._triggerEvent(currentEvent.token);
                    } 
                } else {
		    // Not the first time through the loop; check if the event
		    // has time stamp equal to previously obtained current
		    // time. Then check if it's for the same actor.

                    // Check whether the event occurred at current time.
		    if (currentEvent.key.timeStamp() < _currentTime) {
			throw new InternalErrorException("Event that was "+
				"dequeued later has smaller time stamp. " +
				"Check DECQDirector for bug.");
		    }
                    if (currentEvent.key.timeStamp() > _currentTime) {
                        // The event has a later time stamp, so we put it back
                        fifo.put(currentEvent);
			// Break the loop, since all events after this will
			// all have time stamp later or equal to this one.
                        break;
                    } else {
                        // The event has the same time stamp as the first
                        // event seen.  Check whether it is for the same actor.
                        if (currentEvent.actor == _actorToFire) {
                            // FIXME: Currently, this might put the event
                            // into a receiver that already has an event.
                            // The actors may not be written to look for
                            // multiple events in the same receiver.
                            // Perhaps this should check to see whether there
                            // is an event in the receiver and save this
                            // one if so.  That's still not quite right though
                            // because the event in the receiver may be an
                            // old one...
                            DEReceiver rec = currentEvent.receiver;
			    // if rec is null, then it's a 'pure event' and
			    // there's no need to put event into receiver.
                            if (rec != null) {
				// Adds the receiver to the _filledreceivers 
				// list.
				if (!_filledReceivers.includes(rec)) {
				    _filledReceivers.insertFirst(rec);
				}
				// Transfer the event to the receiver.
                                rec._triggerEvent(currentEvent.token);
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

                _cQueue.put(event.key,event);
            }
        }
        return _actorToFire != null;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    // Construct a directed graph with the nodes representing input ports and
    // directed edges representing zero delay path.  The directed graph
    // is put in the private variable _dag, replacing whatever was there
    // before.
    // FIXME: this method is too complicated and probably means
    // there's a flaw in the design.
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
                        // FIXME: Could this exception be triggered by
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
                            // FIXME: Could this exception be triggered by
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
                            // FIXME: Could this exception be triggered by
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
	for(int i=sort.length-1; i >= 0; i--) {
            IOPort p = (IOPort)sort[i];
            // FIXME: Debugging topological sort
            System.out.println(p.description(FULLNAME) + ":" + i);
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
                throw new InternalErrorException("Bug in DECQDirector."+
                        "computeDepth() (3)");
            }
	    if (r == null) {
		// dangling input port..
		continue;
	    }
	    for (int j=r.length-1; j >= 0; j--) {
                for (int k=r[j].length-1; k >= 0; k--) {
                    DEReceiver der = (DEReceiver)r[j][k];
                    der.setDepth(i);
                }
            }
	}
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private inner class               ////

    // Wrapper for the data to store in the queue.
    private class DEEvent {

        // Constructor to use when there is a token and destination receiver.
        DEEvent(DEReceiver r, Token t, DESortKey k)
                throws IllegalActionException {
	    // check the validity of the receiver.
	    if(r != null) {
                Nameable port = r.getContainer();
                if(port != null) {
                    actor = (Actor)port.getContainer();
                }
            }
            if (actor == null) {
                throw new IllegalActionException(
                    "Attempt to queue an event with an invalid receiver.");
            }
	    
            receiver = r;
            token = t;
            key = k;
        }

        // Constructor to use when only an actor firing is being requested.
        DEEvent(Actor a, DESortKey k) {
            actor = a;
            key = k;
        }

        // public fields
        public Actor actor;
        public DEReceiver receiver;
        public Token token;
        public DESortKey key;
    }

    private class DECQComparator implements CQComparator {
	
	/** Compare its two argument for order. Return a negative integer,
	 *  zero, or a positive integer as the first argument is less than,
	 *  equal to, or greater than the second.
	 *  <p>
	 *  Both arguments have to be instances of DESortKey, otherwise a
	 *  ClassCastException will be thrown.
	 *  <p>
	 *  The comparison is done based on their time stamps, and in case the
	 *  time stamps are equal, then their receiverDepth values is used.
	 * @param object1 the first DESortKey argument
	 * @param object2 the second DESortKey argument
	 * @return a negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 * @exception ClassCastException object1 and object2 have to be instances
	 *            of DESortKey
	 */
	public int compare(Object object1, Object object2) {
	    
	    DESortKey a = (DESortKey) object1;
	    DESortKey b = (DESortKey) object2;
	    
	    if ( a.timeStamp() < b.timeStamp() )  {
		return -1;
	    } else if ( a.timeStamp() > b.timeStamp() ) {
		return 1;
	    } else if ( a.receiverDepth() < b.receiverDepth() ) {
		return -1;
	    } else if ( a.receiverDepth() > b.receiverDepth() ) {
		return 1;
	    } else {
		return 0;
	    }
    }
	
	/** Given a key, a zero reference, and a bin width, return the index of
	 *  the bin containing the key.
	 *  <p>
	 *  If the arguments are not instances of DESortKey, then a
	 *  ClassCastException will be thrown.
	 *  @param key the key
	 *  @param zeroReference the zero reference.
	 *  @param binWidth the width of the bin
	 *  @return The index of the bin containing the key, according to the
	 *          zero reference, and the bin width.
	 *  @exception ClassCastException Arguments need to be instances of
	 *          DESortKey.
	 */
	public long getBinIndex(Object key, Object zeroReference, Object binWidth) {
	    DESortKey a = (DESortKey) key;
	    DESortKey w = (DESortKey) binWidth;
	    DESortKey zero = (DESortKey) zeroReference;
	    
	    return (long)((a.timeStamp() - zero.timeStamp())/w.timeStamp());
	}
	
	
	/** Given an array of DESortKey objects, find the appropriate bin
	 *  width. By 'appropriate', the bin width is chosen such that on average
	 *  the number of entry in all non-empty bins is equal to one.
	 *  If the argument is null, return the default bin width which is 1.0
	 *  for this implementation.
	 *  <p>
	 *  If the argument is not an instance of DESortKey[], then a
	 *  ClassCastException will be thrown.
	 *
	 *  @param keyArray an array of DESortKey objects.
	 *  @return The bin width.
	 *  @exception ClassCastException keyArray need to be an array of
	 *          DESortKey.
	 *
	 */
	public Object getBinWidth(Object[] keyArray) {
	    
	    if ( keyArray == null ) {
		return new DESortKey(1.0, 0);
	    }
	    
	    double[] diff = new double[keyArray.length - 1];
	    
	    double average = 0;
	    for (int i = 1; i < keyArray.length; ++i) {
		diff[i-1] = ((DESortKey)keyArray[i]).timeStamp() -
		    ((DESortKey)keyArray[i-1]).timeStamp();
		average = average + diff[i-1];
	    }
	    average = average / diff.length;
	    double effAverage = 0;
	    int nEffSamples = 0;
	    for (int i = 1; i < keyArray.length; ++i) {
		if ( diff[i-1] < 2*average ) {
		    nEffSamples++;
		    effAverage = effAverage + diff[i-1];
		}
	    }
	    effAverage = effAverage / nEffSamples;
	    return new DESortKey(3.0 * effAverage, 0);
	    
	}
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //_cQueue: an instance of CalendarQueue is used for sorting.
    private CalendarQueue _cQueue = new CalendarQueue(new DECQComparator());

    // variables to keep track of the objects currently firing.
    private Actor _actorToFire = null;

    // Directed Graph whose nodes represent input ports and whose
    // edges represent delay free paths.  This is used for prioritzing
    // simultaneous events.
    private DirectedAcyclicGraph _dag=null;

    // Access with insertFirst(), take().
    private LinkedList _filledReceivers = new LinkedList();

    // FIXME: debug variables
    private boolean _startTimeInitialized = false;
}






