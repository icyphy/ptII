/* A DE-extended domain director.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)

//FIXME: The forllowings are from DE Director.
Review transferOutputs().
Review changes in fire() and _dequeueEvents().
Review fireAtCurrentTime()'s use of the time Double.NEGATIVE_INFINITY
*/

package ptolemy.domains.de.kernel;

import java.util.Hashtable;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IODependency;
import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// DEEDirector

/**
This director extends DE director and handles hierarchical DE models.

This director uses IODependencies, which specify the input and output
relations of an actor, to do topological sort and scheduling. This director
excludes the models with cyclic loops in the graph composed of IO ports 
of actors, and throws an exception complaining that no valid schedules
can be found.

Note that the pure events are treated differently in DEDirector and this
director. In DEDirector, the pure event has the same depth with the actor
requesting refiring. This may delay the event passing when the event has
to pass across hierarchy boundary. The current solution is to make the pure
events highest priority (0) and they will be processed at the very beginning
of each iteration. For simultaneous pure events, they will be processed
based on the order they are inserted into the event queue, which reflects
the topological order of the actors producing these pure events.

Another interesting point was pointed out by Steve, that the equivalence 
of the send method of Delay actor and the fireAt method of general actor 
on breaking the loop. A simple but intuitive example is an opaque composite
actor with a Delay actor embedded inside. 

@author Haiyang Zheng
@version $Id$
@since Ptolemy II 0.2
@see DEReceiver
@see ptolemy.actor.util.CalendarQueue
 */
public class DEEDirector extends DEDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DEEDirector() {
        this(null);
    }

    /**  Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public DEEDirector(Workspace workspace) {
        super(workspace);
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
    public DEEDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
   }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Dequeue the events from the event queue that have the smallest
     *  time stamp and depth. Advance the model time to their
     *  time stamp, and mark the destination actor for firing.
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
    protected Actor _dequeueEvents() {
        Actor actorToFire = null;
        DEEvent currentEvent = null, nextEvent = null;

        // If there is no event queue, then there is obviously no
        // actor to fire. (preinitialize() has not been called).
        if (_eventQueue == null) return null;

        // Keep taking events out until there are no more event with the same
        // tag or until the queue is empty, or until a stop is requested.
        while (!_stopRequested) {
            // Get the next event off the event queue.
            if (_stopWhenQueueIsEmpty) {
                if (_eventQueue.isEmpty()) {
                    // Nothing more to read from queue.
                    break;
                } else {
                    nextEvent = (DEEvent)_eventQueue.get();
                }
            } else if (!_isTopLevel() && _eventQueue.isEmpty()) {
                // FIXME: changed by liuxj, to be reviewed
                // Should the behavior depend on whether the container
                // is a "source"?
                // This essentially disables the stopWhenQueueIsEmpty
                // parameter for DE directors not at the top level.
                break;
            } else {
                // In this case we want to do a blocking read of the queue,
                // unless we have already found an actor to fire.
                if (actorToFire != null && _eventQueue.isEmpty()) break;
                while (_eventQueue.isEmpty() && !_stopRequested) {
                    if (_debugging) {
                        _debug("Queue is empty. Waiting for input events.");
                    }
                    Thread.yield();
                    synchronized(_eventQueue) {
                        if (_eventQueue.isEmpty()) {
                            try {
                                // FIXME: If the manager gets a change request
                                // during this wait, the change request will
                                // not be executed until we emerge from this
                                // wait.  This can lead to deadlock if the UI
                                // waits for the change request to complete
                                // (which it typically does).
                                //_eventQueue.wait();
                                workspace().wait(_eventQueue);
                            } catch (InterruptedException e) {
                                // If the wait is interrupted,
                                // then stop waiting.
                                break;
                            } catch (Exception e) {
                                if (_debugging) {
                                    _debug(e.toString());
                                }
                            }
                        }
                    } // Close synchronized block
                }
                if (_eventQueue.isEmpty()) {
                    // Nothing more to read from queue.
                    break;
                } else {
                    nextEvent = (DEEvent)_eventQueue.get();
                }
            }

            // An embedded director should not process events in the future.
            if (!_isTopLevel() &&
                    _eventQueue.get().timeStamp() > getCurrentTime()) {
                break;
            }

            if (actorToFire == null) {
                // No previously seen event at this tag, so
                // always accept the event.

                // If necessary, let elapsed real time catch up with
                // the event time.
                double currentTime;
                if (!_synchronizeToRealTime) {
                    currentEvent = (DEEvent)_eventQueue.get();
                    currentTime = currentEvent.timeStamp();
                } else {
                    synchronized(_eventQueue) {
                        while (true) {
                            currentEvent = (DEEvent)_eventQueue.get();

                            currentTime = currentEvent.timeStamp();

                            long elapsedTime = System.currentTimeMillis()
                                - _realStartTime;
                            // NOTE: We assume that the elapsed time can be
                            // safely cast to a double.  This means that
                            // the DE domain has an upper limit on running
                            // time of Double.MAX_VALUE milliseconds, which
                            // is probably longer than the sun is going to last
                            // (and maybe even longer than Sun Microsystems).
                            double elapsedTimeInSeconds =
                                ((double)elapsedTime)/1000.0;
                            if (currentTime <= elapsedTimeInSeconds) {
                                break;
                            }
                            long timeToWait = (long)((currentTime -
                                    elapsedTimeInSeconds)*1000.0);
                            if (timeToWait > 0) {
                                if (_debugging) {
                                    _debug("Waiting for real time to pass: "
                                            + timeToWait);
                                }
                                //synchronized(_eventQueue) {
                                try {
                                    _eventQueue.wait(timeToWait);
                                } catch (InterruptedException ex) {
                                    // Continue executing.
                                }
                                //}
                            }
                        } // while
                    } // sync
                }

                // Consume the event from the queue.  The event must be
                // obtained here, since a new event could have been injected
                // into the queue while the queue was waiting.
                synchronized(_eventQueue) {
                    currentEvent = (DEEvent) _eventQueue.take();
                    currentTime = currentEvent.timeStamp();
                    actorToFire = currentEvent.actor();
                    
                    // Deal with a fireAtCurrentTime event.
                    if (currentTime == Double.NEGATIVE_INFINITY) {
                        currentTime = getCurrentTime();
                    }

                    if (_disabledActors != null &&
                            _disabledActors.contains(actorToFire)) {
                        // This actor has requested that it not be fired again.
                        if (_debugging) _debug("Skipping actor: ",
                                ((Nameable)actorToFire).getFullName());
                        actorToFire = null;
                        continue;
                    }

                    // Advance current time.
                    try {
                        setCurrentTime(currentTime);
                    } catch (IllegalActionException ex) {
                        // Thrown if time moves backwards.
                        throw new InternalErrorException(this, ex, null);
                    }
                }

                _microstep = currentEvent.microstep();

                if (currentTime > getStopTime()) {
                    if (_debugging) {
                        _debug("Current time has passed the stop time.");
                    }
                    _exceedStopTime = true;
                    return null;
                }

                // Transfer the event to the receiver and keep track
                // of which receiver is filled.
                DEReceiver receiver = currentEvent.receiver();

                // If receiver is null, then it's a 'pure event', and there's
                // no need to put event into receiver.
                if (receiver != null) {
                    // Transfer the event to the receiver.
                    if (_debugging) _debug(getName(),
                            "put trigger event to",
                            receiver.getContainer().getFullName());
                    receiver._triggerEvent(currentEvent.token());
                }
            } else {
                // Already seen an event.
                // Check whether the next event has equal tag.
                // If so, the destination actor should
                // be the same, but check anyway.
                
                // FIXME: the same ioPort requirement is not correct.
                // Consider the multi-input atomic actors, e.g. the 
                // BooleanSelect and Inhibit.
                if ((nextEvent.timeStamp() == Double.NEGATIVE_INFINITY ||
                        nextEvent.isSimultaneousWith(currentEvent)) &&
                        nextEvent.actor() == currentEvent.actor() &&
                        nextEvent.ioPort() == currentEvent.ioPort()) {
                    // Consume the event from the queue.

                    _eventQueue.take();

                    // Transfer the event into the receiver.
                    DEReceiver receiver = nextEvent.receiver();
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
        } // Close while () loop
        return actorToFire;
    }


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
     *  plus one.  If there is no event queue, then this method does
     *  nothing.
     *
     *  @param actor The destination actor.
     *  @param time The time stamp of the "pure event".
     *  @exception IllegalActionException If the time argument is in the past.
     */
    protected void _enqueueEvent(Actor actor, double time)
            throws IllegalActionException {
        if (_eventQueue == null) return;
        int microstep = 0;
        if (time == getCurrentTime()) {
            microstep = _microstep + 1;
        } else if (time != Double.NEGATIVE_INFINITY &&
                time < getCurrentTime()) {
            throw new IllegalActionException((Nameable)actor,
                    "Attempt to queue an event in the past:"
                    + " Current time is " + getCurrentTime()
                    + " while event time is " + time);
        }
        // FIXME: what depth for this pure event? 
        // If the actor has different depths...
        //int depth = _getDepth(actor);
        int depth = 0;
        
        if (_debugging) _debug("FIXME: possible issues may arise here. " +
                "enqueue a pure event: ",
                ((NamedObj)actor).getName(),
                "time = " + time + " microstep = " + microstep + " depth = "
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
     *  by the last dequeue, or zero if there has been none). If there is
     *  no event queue, then this method does nothing.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param time The time stamp of the event.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token,
            double time) throws IllegalActionException {

        if (_eventQueue == null) return;
        int microstep = 0;

        if (time == getCurrentTime()) {
            microstep = _microstep;
        } else if (time != Double.NEGATIVE_INFINITY &&
                time < getCurrentTime()) {
            Nameable destination = receiver.getContainer();
            throw new IllegalActionException(destination,
                    "Attempt to queue an event in the past: "
                    + " Current time is " + getCurrentTime()
                    + " while event time is " + time);
        }

        IOPort destination = (IOPort)(receiver.getContainer());
        int depth = _getDepth(destination);
        if (_debugging) _debug("enqueue event: to",
                receiver.getContainer().getFullName()
                + " ("+token.toString()+") ",
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
     *  If there is no event queue, then this method does nothing.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token)
            throws IllegalActionException {

        if (_eventQueue == null) return;

        IOPort destination = (IOPort) receiver.getContainer();
        int depth = _getDepth(destination);
        
        if (_debugging) _debug("enqueue event: to", 
        receiver.getContainer().getFullName() + " ("+token.toString()+") ",
        "time = "+ getCurrentTime() + " microstep = "+ (_microstep + 1) + " depth = "
        + depth);
        _eventQueue.put(new DEEvent(receiver, token,
                getCurrentTime(), _microstep + 1, depth));
    }

    /** Return the depth of an ioPort.
     *  @exception IllegalActionException If the actor is not accessible.
     */
    protected int _getDepth(IOPort ioPort) throws IllegalActionException {
        if (_sortValid != workspace().getVersion()) {
            _computeDepth();
        }
        Integer depth = (Integer)_portToDepth.get(ioPort);
        if (depth != null) {
            return depth.intValue();
        }
        throw new IllegalActionException("Attempt to get depth ioPort " +
                ((NamedObj)ioPort).getName() + " that was not sorted.");
    }

    // Construct a directed graph with the nodes representing ioPorts and
    // directed edges representing dependencies.  The directed graph
    // is returned.
    private DirectedAcyclicGraph _constructDirectedGraph()
            throws IllegalActionException {
        // Clear the graph
        DirectedAcyclicGraph portsGraph = new DirectedAcyclicGraph();

        Nameable container = getContainer();
        // If the container is not composite actor, 
        // there are no actors.
        if (!(container instanceof CompositeActor)) return portsGraph;
        CompositeActor castContainer = (CompositeActor)container;

        // Get the IODependence attribute of the container of this 
        // director. If there is no such attribute, construct one.
        IODependency ioDependency = castContainer.getIODependencies();
         
//        Since the ioDependency is synchronized to workspace, 
//        there is no need to invalidate ioDependency here.
//        // The IODependence attribute is used to construct
//        // the schedule. If the schedule needs recalculation,
//        // the IODependence also needs recalculation.
//        ioDependency.invalidate();
      
        // FIXME: The following may be a very costly test. 
        // -- from the comments of former implementation. 
        // If the port based data flow graph contains directed
        // loops, the model is invalid. An IllegalActionException
        // is thrown with the names of the actors in the loop.
        Object[] cycleNodes = ioDependency.getCycleNodes();
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

        portsGraph = ioDependency.getDetailedPortsGraph().
            toDirectedAcyclicGraph();

        return portsGraph;
    }

    // Perform topological sort on the directed graph and use the result
    // to set the depth for each ioPort. A new Hashtable is created each
    // time this method is called.
    private void _computeDepth() throws IllegalActionException {
        DirectedAcyclicGraph portsGraph = _constructDirectedGraph();
        if (_debugging) {
            _debug("## ports graph is:" + portsGraph.toString());
        }
        Object[] sort = (Object[]) portsGraph.topologicalSort();
        if (_debugging) {
            _debug("## Result of topological sort (highest depth to lowest):");
        }
        // Allocate a new hash table with the equal to the
        // number of ioPorts sorted + 1. The extra entry is
        // for the composite actor that contains this director.
        // This composite actor is set to the highest depth.
        _portToDepth = new Hashtable(sort.length+1);
        if (_debugging) _debug(getContainer().getFullName(),
                "depth: " + sort.length);
        _portToDepth.put(getContainer(), new Integer(sort.length));
        for (int i = sort.length-1; i >= 0; i--) {
            IOPort ioPort = (IOPort)sort[i];
            if (_debugging) _debug(((Nameable)ioPort).getFullName(),
                    "depth: " + i);
            // Insert the hashtable entry.
            _portToDepth.put(ioPort, new Integer(i));
        }
        if (_debugging) _debug("## End of topological sort.");
        // the sort is now valid.
        _sortValid = workspace().getVersion();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The following variables are private. Compared with DE director, there is
    // one private variable: _portToDepth, instead of _actorToDepth.

    // The current microstep.
    private int _microstep = 0;

    // Set to true when the time stamp of the token to be dequeue has
    // exceeded the stopTime.
    private boolean _exceedStopTime = false;

    // The real time at which the model begins executing.
    private long _realStartTime = 0;

    // Decide whether the simulation should be stopped when there's no more
    // events in the global event queue.
    // By default, its value is 'true', meaning that the simulation will stop
    // under that circumstances. Setting it to 'false', instruct the director
    // to wait on the queue while some other threads might enqueue events in
    // it.
    private boolean _stopWhenQueueIsEmpty = true;

    // Specify whether the director should wait for elapsed real time to
    // catch up with model time.
    private boolean _synchronizeToRealTime;

    // The set of actors that have returned false in their postfire() methods.
    // Events destined for these actors are discarded and the actors are
    // never fired.
    private Set _disabledActors;

    // Indicator of whether the topological sort giving ports their
    // priorities is valid.
    private long _sortValid = -1;

    // A Hashtable stores the mapping of each ioPort to its depth.
    private Hashtable _portToDepth = null;
}
