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
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;
import ptolemy.actor.util.CalendarQueue;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashSet;
import java.util.Set;

//FIXME: -- Need to support use real time (for pure event).
//       -- Need to fully support disabled actors.
//       -- Need to implement transferInput and transfer time.



//////////////////////////////////////////////////////////////////////////
//// RTOSDirector
/**
FIXME: Need to update documents.

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

public class RTOSDirector extends Director {

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

    /** Indicating whether the execution of the actors are preemptive.
     *  The default value is false, of type boolean.
     */
    public Parameter preemptive;

    /** The default execution time of a task. If an actor (or its ports)
     *  does not specify its execution time, then this number will
     *  be used. The default value is 0.0, of type double. Note that
     *  using 0.0 as the task execution time indicates that the
     *  task is implemented as hardware.
     */
    public Parameter defaultTaskExecutionTime;

    /** The stop time of the model.  This parameter must contain a
     *  DoubleToken.  The value defaults to Double.MAX_VALUE.
     */
    public Parameter stopTime;

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////
    
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
        } else if (attribute == defaultTaskExecutionTime) {
            if (((DoubleToken)defaultTaskExecutionTime.getToken())
                    .doubleValue() < 0.0) {
                throw new IllegalActionException(this,
                        " task execution time cannot be less than 0.");
            }
        } else if (attribute == preemptive) {
            _preemptive = ((BooleanToken)preemptive.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return the time, at which the current actor being fired
     *  finishes its execution. 
     *  @return The fire end time of the current actor.
     */
    public double getCurrentTime() {
        return _currentTime;
    }

    /** Request a refire from the executive director.
     *  
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        
        super.initialize();
        
        if (_isEmbedded() && !_pureEventQueue.isEmpty()) {
            double nextPureEventTime = 
                ((DEEvent)_pureEventQueue.get()).timeStamp();
            _requestFiringAt(nextPureEventTime);
        }
    }
    
    /** Disable the specified actor.  All events destined to this actor
     *  will be ignored. If the argument is null, then do nothing.
     *  @param actor The actor to disable.
     */
    protected void _disableActor(Actor actor) {
        if (actor != null) {
            if(_debugging) _debug("Actor ", ((Nameable)actor).getName(),
                    " is disabled.");
            if (_disabledActors == null) {
                _disabledActors = new HashSet();
            }
            _disabledActors.add(actor);
        }
    }

    /** Dequeue the event with the highest priority, and execute
     *  the destination actor. If there are multiple events for
     *  this actor with the same priority as the one dequeued, then
     *  these events are dequeued, too. The actor will get
     *  all the dequeued events.
     *  @exception IllegalActionException If the firing actor throws it.
     *
     *
     *  Compare the current time to the time stamp of the first
     *  event in the pure event queue. If they are euqal, then
     *  dequeue the events at the current time from the pure event
     *  queue, and fire the destination actors.
     *  If the current time is less than the time stamp of the
     *  first event in the pure event queue, then look at the 
     *  event queue. In particular, let current time be <i>Tc<i>,
     *  the actor to be triggered
     *  by the first events in the event queue be <B>A</b>, and
     *  <B>A</B> has WCET <i>Ta</i>. Further, let the time stamp
     *  of the first event in the pure event
     *  queue be <i>Tp</i>.
     *  And the  behavior may be different depending on 
     *  the preemptive parameter. 
     *  <p>
     *  
     *   
     */
    public void fire() throws IllegalActionException {
        // First look at the pure event queue.
        double nextPureEventTime = ((DoubleToken)stopTime.getToken()).
                doubleValue();
        while (!_pureEventQueue.isEmpty()) {
            DEEvent pureEvent = (DEEvent)_pureEventQueue.get();
            double timeStamp = pureEvent.timeStamp();
            if (timeStamp < getCurrentTime()) {
                if (_preemptive) {
                    throw new IllegalActionException(this, 
                            "missed an external input message.");
                } else {
                    _firePureEvent(_pureEventQueue.take());
                }
            } else if (timeStamp == getCurrentTime()) {
                 _firePureEvent(_pureEventQueue.take());
            } else {
                // All pure event are in the future.
                // Get the time for the next pure event.
                // It will be used under preemptive execution.
                nextPureEventTime = timeStamp;
                break;
            }
        }
        // Then we process RTOS events. Fire one actor at a time.
        // Dequeue all events with the same priority.
        if (!_eventQueue.isEmpty()) {
            RTOSEvent event = (RTOSEvent)_eventQueue.get();
            double processingTime = event.processingTime();
            if (!_preemptive || ((getCurrentTime() + processingTime) 
                    <= nextPureEventTime)) {
                // Put the event back to its receiver.
                if(_debugging) _debug(getName(),
                        "put trigger event to",
                        ((NamedObj)event.actor()).getName());
                _eventQueue.take();
                event.receiver()._triggerEvent(event.token());
                // FIXME: Need to put all the tokens at the same time?
                Actor actor = event.actor();
                if (actor.prefire()) {
                    setCurrentTime(getCurrentTime() + event.processingTime());
                    actor.fire();
                    actor.postfire();
                }
            } else {
                // Preemptive execution and the execution is preempted by
                // a pure event.
                event.preemptAfter(nextPureEventTime - getCurrentTime());
                setCurrentTime(nextPureEventTime);
            }
        } else {
            // All RTOS events are processed. It is save to move to the
            // next pure event.
            setCurrentTime(nextPureEventTime);
        }
    }
    
    /** Insert pure event.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If the firing of the actor
     *  throws it.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {

        // ignore requests that are later than the stop time.
        if (_debugging) {
            _debug("+ requesting firing of "
                   + ((Nameable)actor).getFullName()
                   + " at time "
                   + time);
        }
        if (time <= ((DoubleToken)stopTime.getToken()).doubleValue()) {
            // create a DE pure event.
            DEEvent pureEvent = new DEEvent(actor, time, 0, 0);
            _pureEventQueue.put(pureEvent);
        }
    }

    /** Return a new RTOSReceiver.
     *  @return a new RTOSReceiver.
     */
    public Receiver newReceiver() {
        if(_debugging) _debug("Creating new RTOS receiver.");
	return new RTOSReceiver();
    }

    /** Inaddition to the preinitialization implemented in the super
     *  class, create the pure event queue.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void preinitialize() throws IllegalActionException {
        _eventQueue = new CalendarQueue(new RTOSEventComparator(), 16, 2);
        _pureEventQueue =  new DECQEventQueue(2, 2, true);
        
        _disabledActors = null;
        //_noMoreActorsToFire = false;

        // Add debug listeners.
        if (_debugListeners != null) {
            Iterator listeners = _debugListeners.iterator();
            while (listeners.hasNext()) {
                DebugListener listener = (DebugListener)listeners.next();
                _pureEventQueue.addDebugListener(listener);
            }
        }
        super.preinitialize();
    }

    /** Test for the current time, if is not embedded.
     *  @return Whether the stop time has reached.
     */
    public boolean postfire() throws IllegalActionException {
        if(_debugging) _debug("Finish one iteration at time:" + 
                getCurrentTime());
        if (getCurrentTime() >= ((DoubleToken)stopTime.getToken()).
                doubleValue()) {
            return false;
        }
        if (_eventQueue.isEmpty() && _pureEventQueue.isEmpty()) {
            return false;
        }
        return true;

    }
    
    /** Override the default implementation. Time is allowed to go backward.
     *  @param newTime Do nothing.
     */
    public void setCurrentTime(double newTime) throws IllegalActionException {
        _currentTime = newTime;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                    protected methods                           ////

    /** Put an event into the event queue with the specified destination
     *  receiver, token, and priority.
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param time The time stamp of the event.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(RTOSEvent event) {
        if (_eventQueue == null) return;
        if(_debugging) _debug("enqueue event: to",
                event.toString());
        _eventQueue.put(event);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                    private methods                           ////


    // Execute the destination actor of a pure event.
    private void _firePureEvent(DEEvent event) throws IllegalActionException {
        double time = getCurrentTime();
        setCurrentTime(event.timeStamp());
        Actor actor = event.actor();
        if (actor != null) {
            if (actor.prefire()) {
                actor.fire();
                if(!actor.postfire()) {
                    // FIXME: add the actor to the dead actor list.
                }
            }
        }
        setCurrentTime(time);
    }

    // Remove useless parameters inherited from DEDirector, and set
    // different defaults.
    private void _initParameters() {
        try {

            stopTime = new Parameter(this, "stopTime",
                    new DoubleToken(Double.MAX_VALUE));
	    stopTime.setTypeEquals(BaseType.DOUBLE);
            preemptive = new Parameter(this, "preemptive", 
                    new BooleanToken(false));
            preemptive.setTypeEquals(BaseType.BOOLEAN);
            defaultTaskExecutionTime = new Parameter(this, 
                    "defaultTaskExecutionTime", new DoubleToken(0.0));
            defaultTaskExecutionTime.setTypeEquals(BaseType.DOUBLE);
            
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(getName() +
                    "fail to initialize parameters.");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(getName() +
                    "fail to initialize parameters.");
        }
    }

    // Return true if this director is embedded inside an opaque composite
    // actor contained by another composite actor.
    private boolean _isEmbedded() {
        return (getContainer() != null &&
                getContainer().getContainer() != null);
    }

    // Request that the container of this director be refired in the future.
    // This method is used when the director is embedded inside an opaque
    // composite actor (i.e. a wormhole in Ptolemy Classic terminology).
    // If the queue is empty, then throw an InvalidStateException
    private void _requestFiringAt(double time) throws IllegalActionException {
        if (_debugging) _debug("Request refiring of composite actor.",
                getContainer().getName(), "at " + time);
        // Enqueue a refire for the container of this director.
        ((CompositeActor)getContainer()).getExecutiveDirector().fireAt(
                (Actor)getContainer(), time);
    }
    

    ////////////////////////////////////////////////////////////////////////
    ////                    private variables                           ////

    // The RTOS event queue.
    private CalendarQueue _eventQueue;

    // The pure event queue.
    // Time in this queue is absolute, and not affected by the execution
    // of other tasks.
    private DECQEventQueue _pureEventQueue; 

    // local cache of the parameter
    private boolean _preemptive = false;

    // disabled actors list.
    private Set _disabledActors = null;
}
