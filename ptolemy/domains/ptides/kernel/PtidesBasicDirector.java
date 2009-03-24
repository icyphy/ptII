/* Basic Ptides director that uses DE and delivers correct
 * but not necessarily optimal execution.
 * 
@Copyright (c) 2008 The Regents of the University of California.
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

import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
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
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
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

    /** Initialize the actors and request a refiring at the current
     *  time of the executive director. This overrides the base class to
     *  throw an exception if there is no executive director.
     *  @throws IllegalActionException If the superclass throws
     *   it or if there is no executive director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentlyExecutingStack = new Stack<DoubleTimedEvent>();
        _physicalTimeExecutionStarted = null;

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
     *  method has been called. Otherwise, if the director is an embedded
     *  director and the local event queue is not empty, request a refiring
     *  at the current time of the enclosing director.
     *  FIXME: This assumes no sensors and actuators, and that
     *  execution time of all actors is zero.
     *  @return True If this director will be fired again.
     *  @exception IllegalActionException If stopWhenQueueIsEmpty parameter
     *   does not contain a valid token, or refiring can not be requested.
     */
    public boolean postfire() throws IllegalActionException {
        // Do not call super.postfire() because that requests a
        // refiring at the next event time on the event queue.

        boolean stop = ((BooleanToken) stopWhenQueueIsEmpty.getToken()).booleanValue();
        DEEventQueue eventQueue = getEventQueue();

        Boolean result = !_stopRequested;
        if (getModelTime().compareTo(getModelStopTime()) >= 0) {
            // If there is a still event on the event queue with time stamp
            // equal to the stop time, we want to process that event before
            // we declare that we are done.
            if (!eventQueue.get().timeStamp().equals(getModelStopTime())) {
                result = false;
            }
        }
        if (eventQueue.isEmpty() && stop && _currentlyExecutingStack.isEmpty()) {
            result = false;
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
    
    /** Override the base class to reset the icon idle if animation
     *  is turned on.
     *  @exception IllegalActionException If the wrapup() method of
     *  one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _setIcon(_getIdleIcon(), false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Clear any highlights on the specified actor.
     *  @param actor The actor to clear.
     *  @throws IllegalActionException If the animateExecution
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
     *  @throws IllegalActionException If the animateExecution parameter cannot
     *   be evaluated.
     */
    protected String _getExecutingIcon(Actor actorExecuting) throws IllegalActionException {
        _highlightActor(actorExecuting, "{1.0, 0.0, 0.0, 1.0}");
        return "  <property name=\"rectangle\" class=\"ptolemy.vergil.kernel.attributes.RectangleAttribute\">" +
                "    <property name=\"height\" value=\"30\"/>" +
                "    <property name=\"fillColor\" value=\"{1.0, 0.0, 0.0, 1.0}\"/>" +
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
     *  should be fired. In this base class, this method considers two
     *  cases,  depending whether there is an actor currently executing,
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
        DEEventQueue eventQueue = getEventQueue();
        
        Actor container = (Actor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();
        
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

        Time modelTime = getEventQueue().get().timeStamp();
        Actor actorToFire = super._getNextActorToFire();

        Time executionTime = new Time(this, PtidesActorProperties.getExecutionTime(actorToFire));

        if (executionTime.compareTo(_zero) == 0) {
            // If execution time is zero, return the actor.
            // It will be fired now.
            setModelTime(modelTime);
            
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
            DoubleTimedEvent event = new DoubleTimedEvent(modelTime, actorToFire, executionTime);
            _currentlyExecutingStack.push(event);
            _physicalTimeExecutionStarted = physicalTime;

            // Animate if appropriate.
            _setIcon(_getExecutingIcon(actorToFire), false);

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
     *  @throws IllegalActionException If the animateExecution
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
    
    /** Set the icon for this director if the <i>animateExecution</i>
     *  parameter is set to true.
     *  @param moml A MoML string describing the contents of the icon.
     *  @param clearFirst If true, remove the previous icon before creating a
     *   new one.
     *  @throws IllegalActionException If the <i>animateExecution</i> parameter
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

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////


    /** The list of currently executing actors and their remaining execution time
     */
    private Stack<DoubleTimedEvent> _currentlyExecutingStack;


    /** The physical time at which the currently executing actor, if any,
     *  last resumed execution.
     */
    private Time _physicalTimeExecutionStarted;
    
    /** Zero time.
     */
    private Time _zero;

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
}
