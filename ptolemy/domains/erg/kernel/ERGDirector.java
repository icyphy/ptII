/* The ERG director.

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

package ptolemy.domains.erg.kernel;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 An ERG director implements the Event Relationship Graph semantics, and can be
 used by ERG controllers (instances of {@link ERGController}) in ERG modal
 models (instances of {@link ERGModalModel}). This director has an event queue
 where events can be scheduled and are ordered by their time stamps. Events are
 processed according to their time-stamp order. Actions of the events are
 executed when the events are processed.
 <p>
 Between events there may be scheduling relations. If an event schedules another
 to occur after a certain amount of model time, then the other event is placed
 in the event queue when the first event is processed. Scheduling relations may
 be guarded by boolean expressions.
 <p>
 Each ERG controller transparently creates an ERG director inside. For an ERG
 controller that serves as refinement of an event in another ERG controller, the
 ERG director in it invokes the fireAt() method of the ERG director in the
 containing ERG controller. When multiple events are scheduled in one firing of
 the inner ERG director, only one invocation of fireAt() is made in postfire()
 with the most imminent event as the parameter.
 <p>
 This director can be used in DE as a contained model of computation. It can
 also be used to control timed or untimed models of computation, such as DE,
 dataflow, and FSM.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 @see DEDirector
 */
public class ERGDirector extends Director implements TimedDirector {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  Create the timeResolution parameter.
     *
     *  @param container The container.
     *  @param name The name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container, or if
     *   the time resolution parameter is malformed.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public ERGDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        controllerName = new StringAttribute(this, "controllerName");
    }

    /** React to a change in an attribute. If the changed attribute is
     *  the {@link #controllerName} attribute, then make note that this
     *  has changed.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *  attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == controllerName) {
            _controllerVersion = -1;
        }
    }

    /** Cancel an event that was previously scheduled in the event queue. This
     *  method can be called by an event that has an outgoing canceling edge (a
     *  {@link SchedulingRelation} object with the canceling attribute set to
     *  true.
     *
     *  @param event The event to be cancelled.
     *  @return The model time at which the cancelled event was previously
     *  scheduled, if that event is found in the event queue. If the event is
     *  not found, the return is null.
     *  @exception IllegalActionException If the refinement of the given event
     *  (if any) cannot be obtained.
     */
    public Time cancel(Event event) throws IllegalActionException {
        Iterator<TimedEvent> iterator = _eventQueue.iterator();
        Set<TypedActor> refinementSet = new HashSet<TypedActor>();
        TypedActor[] refinements = event.getRefinement();
        if (refinements != null) {
            for (TypedActor refinement : refinements) {
                refinementSet.add(refinement);
            }
        }
        while (iterator.hasNext()) {
            TimedEvent timedEvent = iterator.next();
            if (timedEvent.contents == event
                    || refinementSet.contains(timedEvent.contents)) {
                iterator.remove();
                _inputQueue.remove(timedEvent);
                return timedEvent.timeStamp;
            }
        }
        return null;
    }

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new director.
     *
     *  @param workspace The workspace for the new director.
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ERGDirector newObject = (ERGDirector) super.clone(workspace);
        newObject._eventQueue = new PriorityQueue<TimedEvent>(10,
                _EVENT_COMPARATOR);
        newObject._inputQueue = new PriorityQueue<TimedEvent>(5,
                _EVENT_COMPARATOR);
        newObject._controller = null;
        newObject._controllerVersion = -1;
        return newObject;
    }

    /** Fire the director and process the imminent events or the events that
     *  react to inputs if inputs are available. If refinements of events are
     *  being executed, fire() of those refinements are called to allow them to
     *  fire. Next, if there are events that react to inputs and inputs are
     *  available at some input ports, then those events are taken from the
     *  event queue and are processed. After these steps, if there are still
     *  events that are scheduled at the current model time in the event queue,
     *  those events are processed. If those events schedule new events to occur
     *  at exactly the same model time, then those newly scheduled events are
     *  also processed in the same firing.
     *
     *  @exception IllegalActionException If inputs cannot be read at the input
     *  ports, the controller that contains this director cannot be found, or
     *  this exception is raised when firing a refinement or processing an
     *  event.
     */
    public void fire() throws IllegalActionException {
        if (!_isInController()) {
            ERGModalModel modalModel = (ERGModalModel) getContainer();
            List<?> ergControllers = modalModel.entityList(ERGController.class);
            for (Object controllerObject : ergControllers) {
                ((ERGController) controllerObject).readInputs();
            }
        }

        ERGController controller = getController();
        boolean hasInput = controller.hasInput();
        boolean synchronize = controller.synchronizeToRealtime();
        Time modelTime = getModelTime();

        PriorityQueue<TimedEvent> eventQueue = new PriorityQueue<TimedEvent>(
                _eventQueue);
        PriorityQueue<TimedEvent> inputQueue = new PriorityQueue<TimedEvent>(
                _inputQueue);
        Set<TimedEvent> firedEvents = new HashSet<TimedEvent>();

        if (hasInput) {
            // Fire the refinements of all input events.
            Iterator<TimedEvent> iterator = inputQueue.iterator();
            while (iterator.hasNext() && !_stopRequested) {
                TimedEvent timedEvent = iterator.next();
                if (!(timedEvent.contents instanceof Event)) {
                    firedEvents.add(timedEvent);
                    _fire(timedEvent);
                }
            }

            // Fire scheduled input events.
            iterator = inputQueue.iterator();
            while (iterator.hasNext() && !_stopRequested) {
                TimedEvent timedEvent = iterator.next();
                if (timedEvent.contents instanceof Event) {
                    firedEvents.add(timedEvent);
                    _fire(timedEvent);
                }
            }
        }

        // Fire the next imminent event.
        if (!eventQueue.isEmpty() && !_stopRequested) {
            TimedEvent timedEvent = eventQueue.peek();
            Time nextEventTime = timedEvent.timeStamp;
            if (nextEventTime.compareTo(modelTime) <= 0
                    && !firedEvents.contains(timedEvent)) {
                if (synchronize) {
                    if (!_synchronizeToRealtime(nextEventTime)) {
                        return;
                    }
                    synchronize = false;
                }

                _fire(timedEvent);
            }
        }
    }

    /** Request a firing of the given actor at the given absolute
     *  time.  This method is only intended to be called from within
     *  main simulation thread.  Actors that create their own
     *  asynchronous threads should used the fireAtCurrentTime()
     *  method to schedule firings.
     *
     *  This method puts the actor in the event queue in time-stamp order. The
     *  actor will be fired later when the model time reaches the scheduled time
     *  in a way similar to events being processed from the event queue.
     *
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @exception IllegalActionException If the operation is not
     *  permissible (e.g. the given time is in the past).
     */
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        _fireAt(actor, time, null);
    }

    /** Request to process an event at the given absolute time. This method puts
     *  the event in the event queue in time-stamp order. The event will be
     *  processed later when the model time reaches the scheduled time.
     *
     *  @param event The event scheduled to be processed.
     *  @param time The scheduled time.
     *  @param arguments The arguments to the event.
     *  @exception IllegalActionException If the operation is not
     *  permissible (e.g. the given time is in the past).
     */
    public void fireAt(Event event, Time time, ArrayToken arguments)
    throws IllegalActionException {
        _fireAt(event, time, arguments);
    }

    /** Return the ERG controller has the same container as this director. The
     *  name of the ERG controller is specified by the <i>controllerName</i>
     *  attribute. The mode controller must have the same container as
     *  this director.
     *  This method is read-synchronized on the workspace.
     *
     *  This method is a duplication of {@link
     *  ptolemy.domains.fsm.kernel.FSMDirector#getController()}. However, due to
     *  the class hierarchy, there is no easy way to reuse the code.
     *
     *  @return The mode controller of this director.
     *  @exception IllegalActionException If no controller is found.
     */
    public ERGController getController() throws IllegalActionException {
        if (_controllerVersion == workspace().getVersion()) {
            return _controller;
        }

        try {
            workspace().getReadAccess();

            NamedObj container = getContainer();
            if (_isInController()) {
                _controller = (ERGController) container;
            } else {
                String name = controllerName.getExpression();

                if (name == null) {
                    throw new IllegalActionException(this,
                            "No name for mode controller is set.");
                }

                if (!(container instanceof CompositeActor)) {
                    throw new IllegalActionException(this,
                            "No controller found.");
                }

                CompositeActor composite = (CompositeActor) container;
                Entity entity = composite.getEntity(name);

                if (entity == null) {
                    throw new IllegalActionException(this,
                            "No controller found with name " + name);
                }

                if (!(entity instanceof FSMActor)) {
                    throw new IllegalActionException(this, entity,
                            "mode controller must be an instance of FSMActor.");
                }

                _controller = (ERGController) entity;
            }
            _controllerVersion = workspace().getVersion();
            return _controller;
        } finally {
            workspace().doneReading();
        }
    }

    /** Initialize the model controlled by this director. The initialize()
     *  method of the superclass is called. After that, the initial schedule is
     *  computed. To initialize a schedule, the initial events are placed in the
     *  event queue.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _initializeSchedule();
    }

    /** Return true if the director wishes to be scheduled for another
     *  iteration. The postfire() method of the superclass is called. If it
     *  returns true, this director further checks whether there is still any
     *  event in the event queue. If so, it returns true. Otherwise, it returns
     *  false. If this director is top-level, it sets the model time to be the
     *  time stamp of the next event.
     *
     *  @return True to continue execution, and false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        if (result) {
            if (!_eventQueue.isEmpty()) {
                if (_isTopLevel()) {
                    TimedEvent event = (TimedEvent) _eventQueue.peek();
                    setModelTime(event.timeStamp);
                }
            } else {
                result = false;
            }
        }
        if (_isEmbedded()) {
            _requestFiring();
        }
        return result;
    }

    /** Return true if the director is ready to fire. This method is
     *  called by the container of this director to determine whether the
     *  director is ready to execute. If the prefire() method of the superclass
     *  returns true, it checks whether either of the following conditions is
     *  true: 1) some input ports have received tokens and there are events that
     *  are scheduled to react to inputs, and 2) some events have been scheduled
     *  at the current model time. If either condition is true, this method
     *  returns true.
     *
     *  @return True if the director is ready to execute, or false otherwise.
     *  @exception IllegalActionException If the superclass throws it, or if the
     *  tokens at the input ports cannot be checked.
     */
    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();

        if (_hasInput() && !_inputQueue.isEmpty()) {
            return result;
        }

        if (!_eventQueue.isEmpty()) {
            Time modelTime = getModelTime();
            Time nextEventTime = ((TimedEvent) _eventQueue.peek()).timeStamp;
            while (modelTime.compareTo(nextEventTime) > 0) {
                _eventQueue.poll();

                if (!_eventQueue.isEmpty()) {
                    TimedEvent event = (TimedEvent) _eventQueue.peek();
                    nextEventTime = event.timeStamp;
                } else {
                    nextEventTime = Time.POSITIVE_INFINITY;
                }
            }

            if (!nextEventTime.equals(modelTime)) {
                result = false;
            }

        }

        if (_debugging) {
            _debug("Prefire returns: " + result);
        }

        return result;
    }

    /** Invoke the preinitialize() method of the superclass. If this director is
     *  directly associated with a modal model (i.e., not in any controller),
     *  then preinitialize() of the director in the controller of the modal
     *  model is also called.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (!_isInController()) {
            getController().director.preinitialize();
        }
        _realStartTime = System.currentTimeMillis();
    }

    /** Request that the director cease execution altogether. stop() of the
     *  superclass is called.
     */
    public void stop() {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _stopRequested = true;
            }
        }

        super.stop();
    }

    /** Invoke the wrapup() method of the superclass, and clear the event queue.
     *
     *  @exception IllegalActionException If the wrapup() method of
     *   the superclass throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _eventQueue.clear();
    }

    /** Attribute specifying the name of the ERG controller in the
     *  container of this director. This director must have an ERG
     *  controller that has the same container as this director,
     *  otherwise an IllegalActionException will be thrown when action
     *  methods of this director are called.
     */
    public StringAttribute controllerName = null;

    /** Initialize the schedule by putting the initial events in the event
     *  queue. If this director is directly associated with a modal model, it
     *  schedules itself to be fired immediately.
     *
     *  @exception IllegalActionException If whether an event is initial event
     *  cannot be checked.
     */
    protected void _initializeSchedule() throws IllegalActionException {
        _eventQueue.clear();
        _inputQueue.clear();

        ERGController controller = getController();
        if (_isInController()) {
            ERGModalModel modalModel =
                (ERGModalModel) getContainer().getContainer();
            _currentTime = modalModel.getDirector().getModelTime();

            Iterator<?> entities = controller.deepEntityList().iterator();
            while (entities.hasNext()) {
                Event event = (Event) entities.next();
                boolean isInitial = ((BooleanToken) event.isInitialState
                        .getToken()).booleanValue();
                if (isInitial) {
                    _eventQueue.add(new TimedEvent(_currentTime, event, null));
                }
            }
        } else {
            _eventQueue.add(new TimedEvent(_currentTime, controller, null));
            if (_isEmbedded()) {
                _requestFiring();
            }
        }
    }

    /** Return whether this director is top-level. An ERG director is top-level
     *  if _isTopLevel() of the superclass returns true, and this director is
     *  directly associated with a modal model.
     *
     *  @return True if this director is top-level.
     */
    protected boolean _isTopLevel() {
        return super._isTopLevel() && !_isInController();
    }

    /** Fire an entry in the event queue. If the entry contains information
     *  about a scheduled actor, then the prefire(), fire() and postfire()
     *  methods of the actor are called. If the entry contains an event, then
     *  the event is processed (which means more events may be placed into the
     *  event queue, or existing ones may be cancelled). If the event has a
     *  refinement, the the refinement is also fired.
     *
     *  @param timedEvent The entry in the event queue.
     *  @return True if an event is processed or an actor is fired, or false if
     *  the prefire() method of the actor returns false.
     *  @exception IllegalActionException If firing the actor or processing the
     *  event throws it, or if the contents of the given entry cannot be
     *  recognized.
     */
    private boolean _fire(TimedEvent timedEvent) throws IllegalActionException {
        ERGController controller = getController();
        Object contents = timedEvent.contents;
        if (contents instanceof Actor) {
            Actor actor = (Actor) contents;
            boolean prefire = actor.prefire();
            if (prefire) {
                _eventQueue.remove(timedEvent);
                _inputQueue.remove(timedEvent);

                actor.fire();
                actor.postfire();
                return true;
            } else {
                return false;
            }
        } else if (contents instanceof Event) {
            _eventQueue.remove(timedEvent);
            _inputQueue.remove(timedEvent);

            Event event = (Event) timedEvent.contents;
            controller._setCurrentEvent(event);
            event.fire(timedEvent._arguments);

            Actor[] refinements = event.getRefinement();
            if (refinements != null) {
                for (Actor refinement : refinements) {
                    if (_stopRequested) {
                        break;
                    }
                    if (refinement instanceof ERGController) {
                        ((ERGController) refinement).director
                                ._initializeSchedule();
                        _fireAt(refinement, _currentTime, null);
                    } else {
                        if (refinement.prefire()) {
                            refinement.fire();
                            refinement.postfire();
                        }
                    }
                }
            }

            if (((BooleanToken) event.isFinalState.getToken()).booleanValue()) {
                _eventQueue.clear();
            }

            return true;
        } else {
            throw new InternalErrorException(this, null, "The contents of a "
                    + "TimedEvent can only be Actor or Event.");
        }
    }

    /** Schedule an actor or an event at the given time with the given arguments
     *  (for events only).
     *
     *  @param object The actor or the event.
     *  @param time The time.
     *  @param arguments Arguments to the event.
     *  @exception IllegalActionException If the actor or event is to be
     *   scheduled at a time in the past.
     */
    private void _fireAt(Object object, Time time, ArrayToken arguments)
    throws IllegalActionException {
        if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(this,
                    "Attempt to schedule an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + time);
        }

        TimedEvent timedEvent = new TimedEvent(time, object, arguments);
        _eventQueue.add(timedEvent);
        if (object instanceof Actor) {
            _inputQueue.add(timedEvent);
        } else if (object instanceof Event) {
            Event event = (Event) object;
            if (event.fireOnInput()) {
                _inputQueue.add(timedEvent);
            }
        }
    }

    /** Return whether inputs have been received at input ports.
     *
     *  @return True if inputs have been received.
     *  @exception IllegalActionException If thrown when trying to test whether
     *   input ports have tokens.
     */
    private boolean _hasInput() throws IllegalActionException {
        if (_isInController()) {
            return ((ERGController) getContainer()).hasInput();
        } else {
            NamedObj container = getContainer();
            List<?> inputPorts;
            if (container instanceof ERGController) {
                inputPorts = ((ERGController) container).inputPortList();
            } else {
                inputPorts = ((CompositeActor) getContainer()).inputPortList();
            }
            for (Object portObject : inputPorts) {
                IOPort port = (IOPort) portObject;
                for (int i = 0; i < port.getWidth(); i++) {
                    if (port.hasToken(i)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /** Return whether this director is in an ERG controller.
     *
     *  @return True if this director is in an ERG controller. False if this
     *   director is directly associated with an ERG modal model.
     */
    private boolean _isInController() {
        return getContainer() instanceof ERGController;
    }

    /** Request to fire this director by invoking the fireAt() method of the
     *  executive director of the container.
     *
     *  @exception IllegalActionException If the invoked fireAt() method throws
     *   it.
     */
    private void _requestFiring() throws IllegalActionException {
        NamedObj container = getContainer();
        if (!_eventQueue.isEmpty()) {
            Time time = _eventQueue.peek().timeStamp;
            if (_isInController()) {
                ERGController controller = (ERGController) container;
                controller.getExecutiveDirector().fireAt(controller, time);
            } else {
                CompositeActor composite = (CompositeActor) container;
                composite.getExecutiveDirector().fireAt(composite, time);
            }
        } else if (!_inputQueue.isEmpty()) {
            ERGController controller = (ERGController) container;
            controller.getExecutiveDirector().fireAt(controller,
                    Time.POSITIVE_INFINITY);
        }
    }

    /** Wait for real time to elapse if the current model time is greater than
     *  the real time that have elapsed since the start of execution.
     *
     *  @param nextEventTime The model time of the next event that needs to be
     *   synchronized to.
     *  @return True if the wait is successful. False if the wait is
     *   interrupted.
     */
    private boolean _synchronizeToRealtime(Time nextEventTime) {
        long elapsedTime = System.currentTimeMillis() - _realStartTime;
        double elapsedTimeInSeconds = elapsedTime / 1000.0;
        long timeToWait = (long) (nextEventTime.subtract(elapsedTimeInSeconds)
                .getDoubleValue() * 1000.0);
        if (timeToWait > 0) {
            try {
                _workspace.wait(_eventQueue, timeToWait);
                if (_stopRequested) {
                    return false;
                }
            } catch (InterruptedException ex) {
                return false;
            }
        }
        return true;
    }

    /** The comparator used to compare the time stamps of any two events. */
    @SuppressWarnings("unchecked")
    private static final Comparator<TimedEvent> _EVENT_COMPARATOR =
        new TimedEvent.TimeComparator();

    /** Cached reference to mode controller. */
    private ERGController _controller = null;

    /** Version of cached reference to mode controller. */
    private long _controllerVersion = -1;

    /** The event queue. */
    private PriorityQueue<TimedEvent> _eventQueue =
        new PriorityQueue<TimedEvent>(10, _EVENT_COMPARATOR);

    /** The event queue that contains only events that can react to inputs. */
    private PriorityQueue<TimedEvent> _inputQueue =
        new PriorityQueue<TimedEvent>(5, _EVENT_COMPARATOR);

    /** The real time at which the execution started. */
    private long _realStartTime;

    //////////////////////////////////////////////////////////////////////////
    //// TimedEvent

    /**
     The class to encapsulate information to be stored in an entry in the event
     queue.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class TimedEvent extends ptolemy.actor.util.TimedEvent {

        /** Construct a TimedEvent.
         *
         *  @param time The model time at which the actor or event is scheduled.
         *  @param object The actor or event.
         *  @param arguments Arguments to the event.
         */
        public TimedEvent(Time time, Object object, ArrayToken arguments) {
            super(time, object);
            _arguments = arguments;
        }

        /** Display timeStamp and contents.
         */
        public String toString() {
            String result = "timeStamp: " + timeStamp + ", contents: " +
                    contents;
            if (_arguments != null) {
                result += "(" + _arguments + ")";
            }
            return result;
        }

        /** Arguments to the event. */
        private ArrayToken _arguments;
    }
}
