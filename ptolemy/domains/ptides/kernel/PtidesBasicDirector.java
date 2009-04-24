/* Basic Ptides director that uses DE and delivers correct
 * but not necessarily optimal execution.
 *
@Copyright (c) 2008-2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.kernel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.kernel.attributes.VisibleAttribute;

/** FIXME
 *
 *  This director has a local notion time, decoupled from that of the
 *  enclosing director. The enclosing director's time
 *  represents physical time, whereas this time represents model
 *  time in the Ptides model.
 *  Assume the incoming event always has higher priority, so preemption always occurs.
 *
 *  @author Patricia Derler, Edward A. Lee, Ben Lickly, Isaac Liu, Slobodan Matic, Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Yellow (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class PtidesBasicDirector extends DEDirector {

    /** Construct a director with the specified container and name.
     *  @param container The container
     *  @param name The name
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public PtidesBasicDirector(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        animateExecution = new Parameter(this, "animateExecution");
        animateExecution.setExpression("false");
        animateExecution.setTypeEquals(BaseType.BOOLEAN);

        _zero = new Time(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then modify the icon for this director to indicate
     *  the state of execution. This is a boolean that defaults to false.
     */
    public Parameter animateExecution;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

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
     *  @exception IllegalActionException If the firing actor throws it, or
     *   event queue is not ready, or an event is missed, or time is set
     *   backwards.
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
            if (actorToFire == getContainer()) {
                // Since we are now actually stopping the firing, we can set this false.
                _stopFireRequested = false;
                return;
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
                    if (!((CompositeEntity)getContainer()).deepContains((NamedObj)actorToFire)) {
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
                    if (!((CompositeEntity)getContainer()).deepContains((NamedObj)actorToFire)) {
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
                    } else if (next.timeStamp().compareTo(getModelTime()) < 0) {
                        _microstep = 0;
                        // FIXME: reset microstep and keep firing in the current iteration??
                    } else if (next.microstep() < _microstep) {
                        // FIXME: what should happen in this case??
                        _microstep = next.microstep();
                        // FIXME: same timestamp, but microstep is smaller, so we want to
                        // reset the microstep and firing again at the current iteration?
                    } else {
                        // The next event has the same tag as the current tag,
                        // indicating that at least one actor is going to be
                        // fired at the current iteration.
                        // Continue the current iteration.
                    }
                }
            }
        } // Close the BIG while loop.

        // Since we are now actually stopping the firing, we can set this false.
        _stopFireRequested = false;

        if (_debugging) {
            _debug("DE director fired!");
        }
    }
    
    /** Initialize the actors and request a refiring at the current
     *  time of the executive director. This overrides the base class to
     *  throw an exception if there is no executive director.
     *  @exception IllegalActionException If the superclass throws
     *   it or if there is no executive director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentlyExecutingStack = new Stack<DoubleTimedEvent>();
        realTimeOutputEventQueue = new PriorityQueue<RealTimeEvent>();
        realTimeInputEventQueue = new PriorityQueue<RealTimeEvent>();
        _physicalTimeExecutionStarted = null;
        
        // _calculateModelTimeOffsets();

        NamedObj container = getContainer();
        if (!(container instanceof Actor)) {
            throw new IllegalActionException(this,
                    "No container, or container is not an Actor.");
        }
        Director executiveDirector = ((Actor)container).getExecutiveDirector();
        if (executiveDirector == null) {
            throw new IllegalActionException(this,
                    "The PtidesBasicDirector can only be used within an enclosing director.");
        }
        executiveDirector.fireAtCurrentTime((Actor)container);

        _setIcon(_getIdleIcon(), true);
    }

    /** Return false if there are no more actors to be fired or the stop()
     *  method has been called.
     *  FIXME: This assumes no sensors and actuators
     *  @return True If this director will be fired again.
     *  @exception IllegalActionException If stopWhenQueueIsEmpty parameter
     *   does not contain a valid token, or refiring can not be requested.
     */
    public boolean postfire() throws IllegalActionException {
        // Do not call super.postfire() because that requests a
        // refiring at the next event time on the event queue.

        Boolean result = !_stopRequested;
        if (getModelTime().compareTo(getModelStopTime()) >= 0) {
            // If there is a still event on the event queue with time stamp
            // equal to the stop time, we want to process that event before
            // we declare that we are done.
            if (!getEventQueue().get().timeStamp().equals(getModelStopTime())) {
                result = false;
            }
        }
        return result;
    }

    /** Override the base class to not set model time to that of the
     *  enclosing director. This method always returns true, deferring the
     *  decision about whether to fire an actor to the fire() method.
     *  @return True.
     */
    public boolean prefire() throws IllegalActionException {
        // Do not invoke the superclass prefire() because that
        // sets model time to match the enclosing director's time.
        if (_debugging) {
            _debug("Prefiring: Current time is: " + getModelTime());
        }
        return true;
    }

    /** Set a new value to the current time of the model, where
     *  the new time must be no earlier than the current time.
     *  Derived classes will likely override this method to ensure that
     *  the time is valid.
     *
     *  @exception IllegalActionException If the new time is less than
     *   the current time returned by getCurrentTime().
     *  @param newTime The new current simulation time.
     *  @see #getModelTime()
     */
    public void setModelTime(Time newTime) throws IllegalActionException {
        int comparisonResult = _currentTime.compareTo(newTime);

        if (comparisonResult > 0) {
            if (_debugging) {
                _debug("==== Set current time backwards from " + getModelTime() + " to: " + newTime);
            }
        } else if (comparisonResult < 0) {
            if (_debugging) {
                _debug("==== Set current time to: " + newTime);
            }
        } else {
            // the new time is equal to the current time, do nothing.
        }
        _currentTime = newTime;
    }

    /** Override the base class to reset the icon idle if animation
     *  is turned on.
     *  @exception IllegalActionException If the wrapup() method of
     *  one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _setIcon(_getIdleIcon(), false);
        if (_lastExecutingActor != null) {
            _clearHighlight(_lastExecutingActor);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Clear any highlights on the specified actor.
     *  @param actor The actor to clear.
     *  @exception IllegalActionException If the animateExecution
     *   parameter cannot be evaluated.
     */
    protected void _clearHighlight(Actor actor) throws IllegalActionException {
        if (((BooleanToken)animateExecution.getToken()).booleanValue()) {
            String completeMoML =
                "<deleteProperty name=\"_highlightColor\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this, (NamedObj)actor, completeMoML);
            Actor container = (Actor) getContainer();
            ((TypedCompositeActor)container).requestChange(request);
        }
    }

    /** Return a MoML string describing the icon appearance for a Ptides
     *  director that is currently executing the specified actor.
     *  The returned MoML can include a sequence of instances of VisibleAttribute
     *  or its subclasses. In this base class, this returns a rectangle like
     *  the usual director green rectangle used by default for directors,
     *  but filled with red instead of green.
     *  @see VisibleAttribute
     *  @return A MoML string.
     *  @exception IllegalActionException If the animateExecution parameter cannot
     *   be evaluated.
     */
    protected String _getExecutingIcon(Actor actorExecuting) throws IllegalActionException {
        _highlightActor(actorExecuting, "{0.0, 0.0, 1.0, 1.0}");
        return "  <property name=\"rectangle\" class=\"ptolemy.vergil.kernel.attributes.RectangleAttribute\">" +
                "    <property name=\"height\" value=\"30\"/>" +
                "    <property name=\"fillColor\" value=\"{0.0, 0.0, 1.0, 1.0}\"/>" +
                "  </property>";
    }

    /** Return a MoML string describing the icon appearance for an idle
     *  director. This can include a sequence of instances of VisibleAttribute
     *  or its subclasses. In this base class, this returns a rectangle like
     *  the usual director green rectangle used by default for directors.
     *  @see VisibleAttribute
     *  @return A MoML string.
     */
    protected String _getIdleIcon() {
        return "  <property name=\"rectangle\" class=\"ptolemy.vergil.kernel.attributes.RectangleAttribute\">" +
                "    <property name=\"height\" value=\"30\"/>" +
                "    <property name=\"fillColor\" value=\"{0.0, 1.0, 0.0, 1.0}\"/>" +
                "  </property>";
    }

    /** Return the actor to fire in this iteration, or null if no actor
     *  should be fired.
     *  In this base class, this method first checks whether the top event from
     *  the event queue is destined for an actuator. If it is, then we check
     *  if physical time has reached the timestamp of the actuation event. If it
     *  has, then we fire the actuator. If it has not, then we take the actuator
     *  event from the event queue and put it onto the _realTimeEventQueue, and
     *  call fireAt() of the executive director. We then check if a real-time event
     *  should be processed by looking at the top event of the
     *  _realTimeEventQueue. If there is on that should be fired, that
     *  actor is returned for firing. If not, we go on and considers two
     *  cases, depending whether there is an actor currently executing,
     *  as follows:
     *  <p>
     *  <b>Case 1</b>: If there is no actor currently
     *  executing, then this method checks the event queue and returns
     *  null if it is empty. If it is not empty, it checks the destination actor of the
     *  earliest event on the event queue, and if it has a non-zero execution
     *  time, then it pushes it onto the currently executing stack and
     *  returns null. Otherwise, if the execution time of the actor is
     *  zero, it sets the current model time to the time stamp of
     *  that earliest event and returns that actor.
     *  <p>
     *  <b>Case 2</b>: If there is an actor currently executing, then this
     *  method checks whether it has a remaining execution time of zero.
     *  If it does, then it returns the currently executing actor.
     *  If it does not, then it checks whether
     *  the earliest event on the event queue should
     *  preempt it (by invoking _preemptExecutingActor()),
     *  and if so, checks the destination actor of that event
     *  and removes the event from the event queue. If that destination
     *  actor has an execution time of zero, then it sets the current
     *  model time to the time stamp of that event, and returns that actor.
     *  Else if the destination actor has an execution time of bigger than
     *  zero, then it calls fireAt()
     *  on the enclosing director passing it the time it expects the currently
     *  executing actor to finish executing, and returns null.
     *  If there is no
     *  event on the event queue or that event should not preempt the
     *  currently executing actor, then it calls fireAt()
     *  on the enclosing director passing it the time it expects the currently
     *  executing actor to finish executing, and returns null.
     *  @return The next actor to be fired, which can be null.
     *  @exception IllegalActionException If event queue is not ready, or
     *  an event is missed, or time is set backwards, or if the enclosing
     *  director does not respect the fireAt call.
     *  @see #_preemptExecutingActor()
     */
    protected Actor _getNextActorToFire() throws IllegalActionException {
        // FIXME: This method changes persistent state, yet it is called in fire().
        // This means that this director cannot be used inside a director that
        // does a fixed point iteration, which includes (currently), Continuous
        // and CT and SR, but in the future may also include DE.
        Time physicalTime = _getPhysicalTime();
        Actor container = (Actor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();
        DEEventQueue eventQueue = getEventQueue();

        if (!_currentlyExecutingStack.isEmpty()) {
            // Case 2: We are currently executing an actor.
            DoubleTimedEvent currentEvent = (DoubleTimedEvent)_currentlyExecutingStack.peek();
            // First check whether its remaining execution time is zero.
            Time remainingExecutionTime = currentEvent.remainingExecutionTime;
            Time finishTime = _physicalTimeExecutionStarted.add(remainingExecutionTime);
            int comparison = finishTime.compareTo(physicalTime);
            if (comparison < 0) {
                // NOTE: This should not happen, so if it does, throw an exception.
                throw new IllegalActionException(this,
                        (NamedObj)currentEvent.contents,
                        "Physical time passed the finish time of the currently executing actor");
            } else if (comparison == 0) {
                // Currently executing actor finishes now, so we want to return it.
                // First set current model time.
                setModelTime(currentEvent.timeStamp);
                _currentlyExecutingStack.pop();
                // If there is now something on _currentlyExecutingStack,
                // then we are resuming its execution now.
                _physicalTimeExecutionStarted = physicalTime;

                if (_debugging) {
                    _debug("Actor "
                            + ((NamedObj)currentEvent.contents).getName(getContainer())
                            + " finishes executing at physical time "
                            + physicalTime);
                }

                // Animate, if appropriate.
                _setIcon(_getIdleIcon(), false);
                _clearHighlight((Actor)currentEvent.contents);
                _lastExecutingActor = null;

                // Request a refiring so we can process the next event
                // on the event queue at the current physical time.
                executiveDirector.fireAtCurrentTime((Actor)container);

                return (Actor) currentEvent.contents;
            } else {
                // Currently executing actor needs more execution time.
                // Decide whether to preempt it.
                if (eventQueue.isEmpty() || !_preemptExecutingActor()) {
                    // Either the event queue is empty or the
                    // currently executing actor does not get preempted
                    // and it has remaining execution time. We should just
                    // return because we previously called fireAt() with
                    // the expected completion time, so we will be fired
                    // again at that time. There is no need to change
                    // the remaining execution time on the stack nor
                    // the _physicalTimeExecutionStarted value because
                    // those will be checked when we are refired.
                    return null;
                }
            }
        }
        // If we get here, then we want to execute the actor destination
        // of the earliest event on the event queue, either because there
        // is no currently executing actor or the currently executing actor
        // got preempted.
        if (eventQueue.isEmpty()) {
            // Nothing to fire.

            // Animate if appropriate.
            _setIcon(_getIdleIcon(), false);

            return null;
        }

        DEEvent eventFromQueue = getEventQueue().get();
        Time timeStampOfEventFromQueue = eventFromQueue.timeStamp();

        if (!_safeToProcess(eventFromQueue)) {
            // not safe to process, wait until some physical time.
            return null;
        }

        Actor actorToFire = super._getNextActorToFire();

        Time executionTime = new Time(this, PtidesActorProperties.getExecutionTime(actorToFire));

        if (executionTime.compareTo(_zero) == 0) {
            // If execution time is zero, return the actor.
            // It will be fired now.
            setModelTime(timeStampOfEventFromQueue);

            // Request a refiring so we can process the next event
            // on the event queue at the current physical time.
            executiveDirector.fireAtCurrentTime((Actor)container);

            return actorToFire;
        } else {
            // Execution time is not zero. Push the execution onto
            // the stack, call fireAt() on the enclosing director,
            // and return null.

            Time expectedCompletionTime = physicalTime.add(executionTime);
            Time fireAtTime = executiveDirector.fireAt(container, expectedCompletionTime);

            if (!fireAtTime.equals(expectedCompletionTime)) {
                throw new IllegalActionException(actorToFire, executiveDirector,
                        "Ptides director requires refiring at time "
                        + expectedCompletionTime
                        + ", but the enclosing director replied that it will refire at time "
                        + fireAtTime);
            }

            // If we are preempting a current execution, then
            // update information on the preempted event.
            if (!_currentlyExecutingStack.isEmpty()) {
                // We are preempting a current execution.
                DoubleTimedEvent currentlyExecutingEvent = _currentlyExecutingStack.peek();
                // FIXME: Perhaps execution time should be a Time rather than a double?
                Time elapsedTime = physicalTime.subtract(_physicalTimeExecutionStarted);
                currentlyExecutingEvent.remainingExecutionTime
                        = currentlyExecutingEvent.remainingExecutionTime.subtract(elapsedTime);
                if (currentlyExecutingEvent.remainingExecutionTime.compareTo(_zero) < 0) {
                    // This should not occur.
                    throw new IllegalActionException(this, (NamedObj)currentlyExecutingEvent.contents,
                            "Remaining execution is negative!");
                }
                if (_debugging) {
                    _debug("Preempting actor "
                            + ((NamedObj)currentlyExecutingEvent.contents).getName((NamedObj)container)
                            + " at physical time "
                            + physicalTime
                            + ", which has remaining execution time "
                            + currentlyExecutingEvent.remainingExecutionTime);
                }
            }
            _currentlyExecutingStack.push(new DoubleTimedEvent(timeStampOfEventFromQueue, actorToFire, executionTime));
            _physicalTimeExecutionStarted = physicalTime;

            // Animate if appropriate.
            _setIcon(_getExecutingIcon(actorToFire), false);
            _lastExecutingActor = actorToFire;

            return null;
        }
    }

    /** Return the model time of the enclosing director, which is our model
     *  of physical time.
     *  @return Physical time.
     */
    protected Time _getPhysicalTime() {
        Actor container = (Actor) getContainer();
        Director director = container.getExecutiveDirector();
        return director.getModelTime();
    }

    /** Highlight the specified actor with the specified color.
     *  @param actor The actor to highlight.
     *  @param color The color, given as a string description in
     *   the form "{red, green, blue, alpha}", where each of these
     *   is a number between 0.0 and 1.0.
     *  @exception IllegalActionException If the animateExecution
     *   parameter cannot be evaluated.
     */
    protected void _highlightActor(Actor actor, String color) throws IllegalActionException {
        if (((BooleanToken)animateExecution.getToken()).booleanValue()) {
            String completeMoML =
                "<property name=\"_highlightColor\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"" +
                color +
                "\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this, (NamedObj)actor, completeMoML);
            Actor container = (Actor) getContainer();
            ((TypedCompositeActor)container).requestChange(request);
        }
    }

    /** Return false to get the superclass DE director to behave exactly
     *  as if it is executing at the top level.
     *  @return False.
     */
    protected boolean _isEmbedded() {
        return false;
    }

    /** Return whether we want to preempt the currently executing actor
     *  and instead execute the earliest event on the event queue.
     *  This base class returns false, indicating that the currently
     *  executing actor is never preempted.
     *  @return False.
     */
    protected boolean _preemptExecutingActor() {
        return false;
    }

    /** If the destination port is the only input port of the actor, or if there doesn't exist
     *  a destination port (in case of pure event) then the event is
     *  always safe to process. Otherwise:
     *  If the current physical time has passed the timestamp of the event minus minDelay of
     *  the port, then the event is safe to process. Otherwise the event is not safe to
     *  process, and we calculate the physical time when the event is safe to process and
     *  setup a timed interrupt.
     *
     *  FIXME: assumes each input port is not multiport.
     *
     *  @param event The event checked for safe to process
     *  @return True if the event is safe to process, otherwise return false.
     *  @exception IllegalActionException
     *  @see #setTimedInterrupt()
     */
    protected boolean _safeToProcess(DEEvent event) {
        IOPort port = event.ioPort();
        if (port != null) {
            try {
                Parameter parameter = (Parameter)((NamedObj) port).getAttribute("minDelay");
                if (parameter != null) {
                    DoubleToken token;
                    token = (DoubleToken) parameter.getToken();
                    Time waitUntilPhysicalTime = event.timeStamp().subtract(token.doubleValue());
                    if (_getPhysicalTime().subtract(waitUntilPhysicalTime).compareTo(_zero) >= 0) {
                        return true;
                    } else {
                        _setTimedInterrupt(waitUntilPhysicalTime);
                        return false;
                    }
                } else {
                    return true;
                }
            } catch (ClassCastException ex) {
                return true;
            } catch (IllegalActionException e) {
                // no minDelay, should only happen if the destination port is the only input port of
                // the destination actor.
                return true;
            }
        } else {
            // event does not have a destination port, must be a pure event.
            return true;
        }
    }

    /** Set the icon for this director if the <i>animateExecution</i>
     *  parameter is set to true.
     *  @param moml A MoML string describing the contents of the icon.
     *  @param clearFirst If true, remove the previous icon before creating a
     *   new one.
     *  @exception IllegalActionException If the <i>animateExecution</i> parameter
     *   cannot be evaluated.
     */
    protected void _setIcon(String moml, boolean clearFirst) throws IllegalActionException {
        if (((BooleanToken)animateExecution.getToken()).booleanValue()) {
            String completeMoML =
                "<property name=\"_icon\" class=\"ptolemy.vergil.icon.EditorIcon\">" +
                moml +
                "</property>";
            if (clearFirst) {
                completeMoML = "<group><deleteProperty name=\"_icon\"/>"
                        + completeMoML
                        + "</group>";
            }
            MoMLChangeRequest request = new MoMLChangeRequest(this, this, completeMoML);
            Actor container = (Actor) getContainer();
            ((TypedCompositeActor)container).requestChange(request);
        }
    }
    
    /** For all events in the sensorEventQueue, transfer input events that are ready.
     *  For all events that are currently sitting at the input port, if the realTimeDelay
     *  is 0.0, then transfer them into the platform, otherwise move them into the 
     *  sensorEventQueue and call fireAt() of the executive director.
     *  In either case, if the input port is a networkPort, we make sure the timestamp of
     *  the data token transmitted is set to the timestamp of the local event associated 
     *  with this token.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     */
    protected boolean _transferInputs(IOPort port) throws IllegalActionException {

        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque"
                            + "input port.");
        }
        
        boolean result = false;
        Time physicalTime = _getPhysicalTime();
        // First transfer all tokens that are already in the event queue for the sensor.
        // FIXME: notice this is done NOT for the specific port
        // in question. Instead, we do it for ALL events that can be transferred out of
        // this platform.
        // FIXME: there is _NO_ guarantee from the priorityQueue that these events are sent out
        // in the order they arrive at the actuator. We can only be sure that they are sent
        // in the order of the timestamps, but for two events of the same timestamp at an
        // actuator, there's no guarantee on the order of events sent to the outside.
        while (true) {
            if (realTimeOutputEventQueue.isEmpty()) {
                break;
            }

            RealTimeEvent realTimeEvent = (RealTimeEvent)realTimeOutputEventQueue.peek();
            int compare = realTimeEvent.deliveryTime.compareTo(physicalTime);
            
            if (compare > 0) {
                break;
            } else if (compare == 0) {
                // FIXME: Are these needed here? 
                Parameter parameter = (Parameter)((NamedObj) realTimeEvent.port).getAttribute("realTimeDelay");
                double realTimeDelay = 0.0;
                if (parameter != null) {
                    realTimeDelay = ((DoubleToken)parameter.getToken()).doubleValue();
                } else {
                    // this shouldn't happen.
                    throw new IllegalActionException("real time delay should not be 0.0");
                }
                
                Time lastModelTime = _currentTime;
                if (_isNetworkPort(realTimeEvent.port)) {
                    // If the token is transferred from a network port, then there is no need to
                    // set the proper timestamp associated with the token. This is because we rely
                    // on the fact every network input port is directly connected to a networkReceiver,
                    // which will set the correct timestamp associated with the token.
                    realTimeOutputEventQueue.poll();
                    realTimeEvent.port.sendInside(realTimeEvent.channel, realTimeEvent.token);
                } else {                
                    setModelTime(realTimeEvent.deliveryTime.subtract(realTimeDelay));
                    realTimeOutputEventQueue.poll();
                    realTimeEvent.port.sendInside(realTimeEvent.channel, realTimeEvent.token);
                    setModelTime(lastModelTime);
                }
                if (_debugging) {
                    _debug(getName(), "transferring input from "
                            + realTimeEvent.port.getName());
                }
                result = true;
                
            } else {
                // FIXME: we should probably do something else here.
                throw new IllegalArgumentException("missed transferring at the sensor. " +
                		"Should transfer input at time = "
                        + realTimeEvent.deliveryTime + ", and current physical time = " + physicalTime);
            }
        }

        Parameter parameter = (Parameter)((NamedObj) port).getAttribute("realTimeDelay");
        // realTimeDelay is default to 0.0;
        double realTimeDelay = 0.0;
        if (parameter != null) {
            realTimeDelay = ((DoubleToken)parameter.getToken()).doubleValue();
        }
        if (realTimeDelay == 0.0) {
            Time lastModelTime = _currentTime;
            if (_isNetworkPort(port)) {
                // If the token is transferred from a network port, then there is no need to
                // set the proper timestamp associated with the token. This is because we rely
                // on the fact every network input port is directly connected to a networkReceiver,
                // which will set the correct timestamp associated with the token.
                super._transferInputs(port);
            } else {
                setModelTime(physicalTime);
                result = result || super._transferInputs(port);
                setModelTime(lastModelTime);
            }
        } else {
            for (int i = 0; i < port.getWidth(); i++) {
                try {
                    if (i < port.getWidthInside()) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);
                            Time waitUntilTime = physicalTime.add(realTimeDelay);
                            RealTimeEvent realTimeEvent = new RealTimeEvent(port, i, t, waitUntilTime);
                            realTimeOutputEventQueue.add(realTimeEvent);
                            result = true;
                            
                            // wait until physical time to transfer the token into the platform
                            Actor container = (Actor) getContainer();
                            container.getExecutiveDirector().fireAt((Actor)container, waitUntilTime);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        }
        return result;
    }
    
    /** Overwrite the _transferOutputs() function.
     *  First, for tokens that are stored in the actuator event queue and
     *  send them to the outside of the platform if physical time has arrived.
     *  The second step is to check if this port is a networkedOutput port, if it is, transfer
     *  data tokens immediately to the outside by calling _transferNetworkingOutputs().
     *  Finally, we check for current model time, if the current model time is equal to the physical
     *  time, we can send the tokens to the outside. Else if current model time has exceeded
     *  the physical time, and we still have tokens to transfer, then we have missed the deadline.
     *  Else if current model time has not arrived at the physical time, then we put the token along
     *  with the port and channel into the actuator event queue, and call fireAt of the executive
     *  director so we could send it at a later physical time.
     *  @see #_transferNetworkingOutputs()
     */
    protected boolean _transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque input port.");
        }
        
        // first check for current time, and transfer any tokens that are already ready to output.
        boolean result = false;
        Time physicalTime = _getPhysicalTime();
        int compare = 0;
        // FIXME: notice this is done NOT for the specific port
        // in question. Instead, we do it for ALL events that can be transferred out of
        // this platform.
        // FIXME: there is _NO_ guarantee from the priorityQueue that these events are sent out
        // in the order they arrive at the actuator. We can only be sure that they are sent
        // in the order of the timestamps, but for two events of the same timestamp at an
        // actuator, there's no guarantee on the order of events sent to the outside.
        while (true) {
            if (realTimeInputEventQueue.isEmpty()) {
                break;
            }
            RealTimeEvent tokenEvent = (RealTimeEvent)realTimeInputEventQueue.peek();
            compare = tokenEvent.deliveryTime.compareTo(physicalTime);
            
            if (compare > 0) {
                break;
            } else if (compare == 0) {
                if (_isNetworkPort(tokenEvent.port)) {
                    throw new IllegalActionException("transferring network event from the"
                            + "actuator event queue");
                }
                realTimeInputEventQueue.poll();
                tokenEvent.port.send(tokenEvent.channel, tokenEvent.token);
                if (_debugging) {
                    _debug(getName(), "transferring output "
                            + tokenEvent.token
                            + " from "
                            + tokenEvent.port.getName());
                }
                result = true;
            } else if (compare < 0) {
                // FIXME: we should probably do something else here.
                throw new IllegalArgumentException("missed deadline at the actuator. Deadline = "
                        + tokenEvent.deliveryTime + ", and current physical time = " + physicalTime);
            }
        }
        
        if (_isNetworkPort(port)) {
            // if we transferred once to the network output, then return true,
            // and go through this once again.
            while (true) {
                if (!super._transferOutputs(port)) {
                    break;
                }
            }
            // do not need to update the result, because this loop ensures
            // we have transmitted all network output events, so no need
            // to enter here again.
        }
        
        compare = _currentTime.compareTo(physicalTime);
        // if physical time has reached the timestamp of the last event, transmit data to the output
        // now. Notice this does not guarantee tokens are transmitted, simply because there might
        // not be any tokens to transmit.
        if (compare == 0){
            result = result || super._transferOutputs(port);
        } else if (compare < 0) {
            for (int i = 0; i < port.getWidthInside(); i++) {
                if (port.hasTokenInside(i)) {
                    // FIXME: we should probably do something else here.
                    throw new IllegalArgumentException("missed deadline at the actuator. Deadline = "
                            + _currentTime + ", and current physical time = " + physicalTime);
                }
            }
        } else {
            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    if (port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        RealTimeEvent tokenEvent = new RealTimeEvent(port, i, t, _currentTime);
                        realTimeInputEventQueue.add(tokenEvent);
                        // wait until physical time to transfer the output to the actuator
                        Actor container = (Actor) getContainer();
                        container.getExecutiveDirector().fireAt((Actor)container, _currentTime);
                    }
                } catch (NoTokenException ex) {
                        // this shouldn't happen.
                        throw new InternalErrorException(this, ex, null);
                }
            }
        }
        
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////
    
    /** Causality analysis that happens at the initialization phase.
     *  The goal is to annotate each port with a minDelay parameter,
     *  which is the offset used for safe to process analysis.
     *  
     *  Start from each input port that is connected to the outside of the platform
     *  (These input ports indicate sensors and network interfaces), and traverse
     *  the graph until we reach the output port connected to the outside of
     *  the platform (actuators). For each input port in between, first calcualte
     *  the real time delay - model time delay from the sensor/network to the input
     *  port. Then for each actor, calculate the minimum model time offset by
     *  studying the real time delay - model time delay values at each of its input
     *  ports. Finally annotate the offset as the minDelay.
     *  @throws IllegalActionException 
     */
    private boolean _calculateModelTimeOffsets() throws IllegalActionException {
        
        // FIXME: If there are composite actors within the top level composite actor, 
        // does this algorithm work? 
        // initialize all port model delays to infinity.
        HashMap portDelays = new HashMap<IOPort, Time>();
        for (Actor actor : (List<Actor>)(((TypedCompositeActor)getContainer()).deepEntityList())) {
            for (TypedIOPort inputPort : (List<TypedIOPort>)(actor.inputPortList())) {
                portDelays.put(inputPort, new Time(this, Double.MAX_VALUE));
            }
            for (TypedIOPort outputPort : (List<TypedIOPort>)(actor.outputPortList())) {
                portDelays.put(outputPort, new Time(this, Double.MAX_VALUE));
            }
        }
        
        // Now start from each sensor (input port at the top level), traverse through all
        // ports.
        for (TypedIOPort startPort : (List<TypedIOPort>)(((TypedCompositeActor)getContainer()).portList())) {
            if (startPort.isInput()) {
                // Setup a local priority queue to store all reached ports
                HashMap localPortDelays = new HashMap<IOPort, Time>(portDelays);
                
                PriorityQueue distQueue = new PriorityQueue<TimedEvent>();
                Time timeDelay = new Time(this, -_getMinDelay(startPort));
                distQueue.add(new TimedEvent(timeDelay, startPort));

                // Dijkstra's algorithm to find all shortest time delays.
                while (!distQueue.isEmpty()) {
                    IOPort port = (IOPort)((TimedEvent)distQueue.remove()).contents;
                    Time prevModelTime = (Time)localPortDelays.get(port);
                    Actor actor = (Actor)port.getContainer();
                    if (port.isInput() && port.isOutput()){
                        throw new IllegalActionException("the causality anlysis cannot deal with" +
                        		"port that are both input and output");
                    }
                    // we do not want to traverse to the outside of the platform.
                    if (actor != getContainer()) {
                        if (port.isInput()) {
                            // Time modelTimeDelay = 
                        } else { // port is an output port
                            for (IOPort sinkPort: (List<IOPort>)port.sinkPortList() ) {
                                // need to make sure
                                if (((Time)localPortDelays.get(sinkPort)).compareTo(prevModelTime) > 0) {
                                    localPortDelays.put(sinkPort, prevModelTime);                                
                                }
                            }
                        }
                    } // else do nothing
                }
                portDelays = localPortDelays;
            }
        }
        
        // Each port should now have a delay associated with it. Now for each actor, go
        // through all ports and annotate the minDelay parameter.
        return true;
    }
    
    /** Returns the minDelay parameter
     *  @param port
     *  @return minDelay parameter
     *  @throws IllegalActionException
     */
    private double _getMinDelay(IOPort port) throws IllegalActionException {
        Parameter parameter = (Parameter)((NamedObj) port).getAttribute("minDelay");
        if (parameter != null) {
            return ((DoubleToken)parameter.getToken()).doubleValue();
        } else {
            return 0.0;
        }
    }
    
    /** check if the port is a networkPort
     *  this method is default to return false, i.e., an output port to the outside of the
     *  platform is by default an actuator port.
     * @throws IllegalActionException 
     */
    private boolean _isNetworkPort(IOPort port) throws IllegalActionException {
        Parameter parameter = (Parameter)((NamedObj) port).getAttribute("networkPort");
        if (parameter != null) {
            return ((BooleanToken)parameter.getToken()).booleanValue();
        }
        return false;
    }
    
    /** Call fireAt() of the executive director, which is in charge of bookkeeping the
     *  physical time.
     */
    private void _setTimedInterrupt(Time wakeUpTime) {
        Actor container = (Actor) getContainer();
        Director executiveDirector = ((Actor)container).getExecutiveDirector();
        try {
            executiveDirector.fireAt((Actor)container, wakeUpTime);
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////


    /** The list of currently executing actors and their remaining execution time
     */
    private Stack<DoubleTimedEvent> _currentlyExecutingStack;

    /** a sorted queue of RealTimeEvents that buffer events before they are sent to the output.
     */
    private PriorityQueue realTimeInputEventQueue;
    
    /** a sorted queue of RealTimeEvents that stores events when they arrive at the input of
     *  the platform, but are not yet visible to the platform (because of real time delay d_o)
     */
    private PriorityQueue realTimeOutputEventQueue;

    /** The physical time at which the currently executing actor, if any,
     *  last resumed execution.
     */
    private Time _physicalTimeExecutionStarted;

    /** Zero time.
     */
    private Time _zero;

    /** Last executing actor
     *  Keeps track of the last actor with non-zero executing time that was executing
     *  This helps to clear the highlighting of that actor when executing stops.
     */
    private Actor _lastExecutingActor;

    ///////////////////////////////////////////////////////////////////
    ////                     inner classes                         ////

    /** A TimedEvent extended with an additional field to represent
     *  the remaining execution time (in physical time) for processing
     *  the event.
     */
    public class DoubleTimedEvent extends TimedEvent {

        /** Construct a new event with the specified time stamp,
         *  destination actor, and execution time.
         * @param timeStamp The time stamp.
         * @param actor The destination actor.
         * @param executionTime The execution time of the actor.
         */
        public DoubleTimedEvent(Time timeStamp, Object actor, Time executionTime) {
            super(timeStamp, actor);
            remainingExecutionTime = executionTime;
        }

        public Time remainingExecutionTime;

        public String toString() {
            return super.toString() + ", remainingExecutionTime = " + remainingExecutionTime;
        }
    }
    
    /** A structure that holds a token with the port and channel it's connected to,
     *  as well as the timestamp associated with this token.
     *  This object is used to hold sensor and actuation events.
     */
    public class RealTimeEvent implements Comparable {
        public IOPort port;
        public int channel;
        public Token token;
        public Time deliveryTime;
        
        public RealTimeEvent(IOPort port, int channel, Token token, Time timestamp) {
            this.port = port;
            this.channel = channel;
            this.token = token;
            this.deliveryTime = timestamp;
        }
        
        public int compareTo(Object other) {
            return deliveryTime.compareTo(((RealTimeEvent)other).deliveryTime);
        }
    }
}
