/* Real Time Operating System Director

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.rtos.kernel;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;


//////////////////////////////////////////////////////////////////////////
//// RTOSDirector
/**
A director that implements a priority-based nonpreemptive scheduling
model of computation. This model of computation is sometimes seen in
real-time operating systems.
<P>
This director is an extension of the DEDirector. It uses piorities 
rather than time stamps to sort events. All events go to a priority
queue and are sorted in the queue. At any given time, events with
the highest priority is dequeued from the event queue and the destination
actor is fired. The priority of an event is the priority of its
destination port. The priority of an input port is specified by
the <i>priority</i> parameter of the port. If a port does not have
a <i>priority</i> parameter, then it may inherit its
priority from its container, an actor. If neither the port nor its
container has the <i>priority</i> parameter, then the priority of the
port is the default priority.
<p>
This director uses the system time as its time. So getCurrentTime()
will return the duration between exectution starting time and the
time that getCurrentTime() is called. The execution starting time
is the time when the initialize() method of this director is called.
This director uesed the system timer to implement fireAt(). If an
actor request a fire at a future time <i>t</i>, then a timer is set,
and the actor will be fired when the timer expires.  

@author Edward A. Lee, Jie Liu
@version $Id$
*/

public class RTOSDirector extends DEDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public RTOSDirector() {
        super();
        _initParameters();
    }

    /**  Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public RTOSDirector(Workspace workspace) {
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
    public RTOSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** Update the director parameters when the attributes are changed.
     *  If the change is <i>stopTime</i>, check whether it is less than
     *  zero. If it is, throw an exception. Note that the changes of
     *  <i>stopTime</i> are ignored during the execution.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *     Not thrown in this class. May be needed by derived classes.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (_debugging) _debug("Updating RTOSDirector parameter",
                attribute.getName());
        if (attribute == stopTime) {
            if (((DoubleToken)stopTime.getToken()).doubleValue() < 0.0) {
                throw new IllegalActionException(this,
                        " stopTime cannot be less than 0.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return the real elapsed time from the start of execution and
     *  the time that this method is called.
     *  @return The real execution time so far.
     */
    public double getCurrentTime() {
        double elaps = (double)(System.currentTimeMillis() - _startingTime);
        return elaps/1000.0;
    }

    /** Override the super class such that this method does nothing.
     *  @param newTime Do nothing.
     */
    public void setCurrentTime(double newTime) throws IllegalActionException {
    }

    /** Record the real time that the execution starts, and then 
     *  initialize the execution.
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        long stop = 
            (long)(((DoubleToken)stopTime.getToken()).doubleValue()*1000.0);
        _terminate = false;
        if (_actorTimers == null) {
            _actorTimers = new HashMap();
        } else {
            _actorTimers.clear();
        }
        
        // Start the timer that counts the termination time.
        TerminationTask task = new TerminationTask(this);
        _terminationTimer = new Timer();
        _startingTime = System.currentTimeMillis();
        _terminationTimer.schedule(task, stop);
        
        super.initialize();
    }

    /** Dequeue the event with the highest priority, and execute
     *  the destination actor. If there are multiple events for 
     *  this actor with the same priority as the one dequeued, then
     *  these events are dequeued, too. The actor will get
     *  all the dequeued events.
     *  @exception IllegalActionException If the firing actor throws it.
     */
    public void fire() throws IllegalActionException {
        _stopRequested = false;
        Actor actorToFire = _dequeueEvents();
        if (actorToFire == null) {
            // There is nothing more to do.
            if (_debugging) _debug("No more events on the event queue.");
            _noMoreActorsToFire = true;
            return;
        }
        if (_debugging) {
            _debug("Found actor to fire: "
                    + ((NamedObj)actorToFire).getFullName());
        }
        // It is possible that the next event to be processed is on
        // an inside receiver of an output port of an opaque composite
        // actor containing this director.  In this case, we simply
        // return, giving the outside domain a chance to react to
        // event.
        if (actorToFire == getContainer()) {
            return;
        }
        if (((Nameable)actorToFire).getContainer() == null) {
            if (_debugging) _debug(
                    "Actor has no container. Disabling actor.");
            _disableActor(actorToFire);
        } else {
            if (!actorToFire.prefire()) {
                if (_debugging) _debug("Prefire returned false.");
            } else {
                actorToFire.fire();
                if (!actorToFire.postfire()) {
                    if (_debugging) _debug("Postfire returned false:",
                           ((Nameable)actorToFire).getName());
                    // Actor requests that it not be fired again.
                    _disableActor(actorToFire);
                }
            }
        }
        Thread.currentThread().yield();
    }

    
    /** Set up a time for the request firing at the future time.
     *  The actor will be fired when the timer expires.
     *  If the requested time is later than the stop time, do nothing.
     *  If the requested time is less than the current time,
     *  then fire the actor immediately.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If the firing of the actor
     *  throws it.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        // ignore requests that are later than the stop time.
        if (time <= ((DoubleToken)stopTime.getToken()).doubleValue()) {
            long requestTime = (long)(time*1000.0) + _startingTime;
            
            long currentTime = System.currentTimeMillis();
            if (requestTime < currentTime) {
                throw new IllegalActionException((Nameable)actor,
                        "Requested fire time: " + time + " is earlier than" +
                        " the current time." + 
                        ((double)getCurrentTime())/1000.0);
            }
            ActorTask actorTask = new ActorTask(actor);
            Timer timer = new Timer();
            _actorTimers.put(actor, timer);
            try {
                timer.schedule(actorTask, requestTime - 
                        System.currentTimeMillis());
            } catch (IllegalArgumentException ex) {
                _debug("Missed the real time deadline. Execute the task.");
                actorTask.run();
            }
        }
    }
        
    /** Return a new RTOSReceiver.
     *  @return a new RTOSReceiver.
     */
    public Receiver newReceiver() {
        if(_debugging) _debug("Creating new DE receiver.");
	return new RTOSReceiver();
    }

    /** Return false if the stop time has reached or the stop request is
     *  made, otherwise, return true.
     *  @return Whether the stop time has reached.
     */
    public boolean postfire() throws IllegalActionException {     
        return super.postfire() && !_terminate;;
    }

    /** Cancel pending timers in additional to warping up actors.
     */
    public void wrapup() throws IllegalActionException {
        if (_actorTimers != null && !_actorTimers.isEmpty()) {
            Iterator timers = _actorTimers.values().iterator();
            while(timers.hasNext()) {
                Timer next = (Timer)timers.next();
                next.cancel();
                System.out.println("Cancel timer");
            }
            _actorTimers.clear();
        }
        if (_terminationTimer != null) {
            _terminationTimer.cancel();
        }
        super.wrapup();
    }


    ////////////////////////////////////////////////////////////////////////
    ////                    protected methods                           ////

    /** Override the base class to throw an exception, since
     *  pure events are not supported in this domain.
     */
    protected void _enqueueEvent(Actor actor, double priority)
            throws IllegalActionException {
        throw new IllegalActionException(this, (NamedObj)actor,
                "Pure events are not supported in this domain.");
    }
    
    /** Put an event into the event queue with the specified destination
     *  receiver, token, and priority.
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param time The time stamp of the event.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token,
            double time) throws IllegalActionException {

        if (_eventQueue == null) return;
        int microstep = 0;
        Actor destination = (Actor)(receiver.getContainer()).getContainer();
        int depth = _getDepth(destination);
        if(_debugging) _debug("enqueue event: to",
                receiver.getContainer().getName()+ " ("+token.toString()+") ",
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(receiver, token, time, microstep, depth));
    }

    /** Override this method in the super class to throw an excption.
     *  Events in this domain must have a priority. The default priority
     *  is defined and used in RTOSReceiver.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @exception IllegalActionException Always thrown.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token)
            throws IllegalActionException {
        throw new IllegalActionException(this, 
                (NamedObj)receiver.getContainer(),
                "IOPort must have priorities.");
    }

    ////////////////////////////////////////////////////////////////////////
    ////                    private methods                           ////
    
    // Remove useless parameters inherited from DEDirector.
    private void _initParameters() {
        try {
            getAttribute("startTime").setContainer(null);
            getAttribute("synchronizeToRealTime").setContainer(null);
            getAttribute("isCQAdaptive").setContainer(null);
            getAttribute("minBinCount").setContainer(null);
            getAttribute("binCountFactor").setContainer(null);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(getName() + 
                    "fail to initialize parameters.");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(getName() + 
                    "fail to initialize parameters.");
        }
    }
    

    ////////////////////////////////////////////////////////////////////////
    ////                    private variables                           ////
    
    // A Hash map for all pending timers.
    private HashMap _actorTimers;

    // starting time as the system clock time
    private long _startingTime;     

    // Indicate that the execution is requested to be terminated.
    private boolean _terminate;

    // The termination task timer.
    private Timer _terminationTimer;
    
    ////////////////////////////////////////////////////////////////////////
    ////                         inner class                            ////
     
    // The task that schedule a firing of an actor in the future time.
    // This task is supposed to be used by the Timer. When the Timer
    // expires, this task will be executed.
    private class ActorTask extends TimerTask {
        public ActorTask(Actor actor) {
            _actor = actor;
        }

        ///////////////////////////////////////////////////////////////////
        ////                      public methods                       ////
     
        /** iterate the actor for one iteration when the timer expires.
         */
        public void run() {
            if (_actor != null) {
                try {
                    // remove the time from the map first.
                    _actorTimers.remove(_actor);
                    _actor.iterate(1);
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException((NamedObj)_actor,
                            ex.getMessage());
                }
            }
        }
        ///////////////////////////////////////////////////////////////////
        ////                      private variables                    ////
     
        private Actor _actor;
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         inner class                            ////
     
    // The task that terminates the execution.
    // This task is supposed to be used with the Timer. When the Timer
    // expires, this task will be executed.
    private class TerminationTask extends TimerTask {
        public TerminationTask(Director director) {
            _director = director;
        }

        ///////////////////////////////////////////////////////////////////
        ////                      public methods                       ////
     
        /** iterate the actor for one iteration when the timer expires.
         */
        public void run() {
            if (_director != null) {
                _terminate = true;
                _director.stopFire();
            }
        }
        ///////////////////////////////////////////////////////////////////
        ////                      private variables                    ////
     
        private Director _director;
    }
}
