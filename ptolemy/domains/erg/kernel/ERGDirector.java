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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.erg.kernel.Event.RefiringData;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.RefinementActor;
import ptolemy.domains.fsm.modal.ModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelRuntimeException;
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
        LIFO = new Parameter(this, "LIFO");
        LIFO.setTypeEquals(BaseType.BOOLEAN);
        LIFO.setToken(BooleanToken.TRUE);
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
    public TimedEvent cancel(Event event) throws IllegalActionException {
        TimedEvent timedEvent = findFirst(event, true);
        if (timedEvent != null) {
            List<TimedEvent> events = _eventInstanceTable.get(timedEvent);
            Object contents = timedEvent.contents;
            if (events == null) {
                _eventQueue.remove(timedEvent);
                _inputQueue.remove(timedEvent);
                if (contents instanceof Actor) {
                    _scheduledRefinements.remove(contents);
                }
            } else {
                for (TimedEvent eventInstance : events) {
                    _eventQueue.remove(eventInstance);
                    _inputQueue.remove(eventInstance);
                    contents = eventInstance.contents;
                    if (contents instanceof Actor) {
                        _scheduledRefinements.remove(contents);
                    }
                }
            }
        }
        return timedEvent;
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
        newObject._controller = null;
        newObject._controllerVersion = -1;
        newObject._eventComparator = new EventComparator();
        newObject._eventQueue = new PriorityQueue<TimedEvent>(10,
                newObject._eventComparator);
        newObject._eventInstanceList = null;
        newObject._eventInstanceTable = new HashMap<TimedEvent,
                List<TimedEvent>>();
        newObject._inputQueue = new PriorityQueue<TimedEvent>(5,
                newObject._eventComparator);
        newObject._scheduledRefinements = new HashSet<TypedActor>();
        return newObject;
    }

    /** Return a boolean dependency representing a model-time delay
     *  of the specified amount.
     *  @param delay A non-negative delay.
     *  @return A boolean dependency representing a delay.
     */
    public Dependency delayDependency(double delay) {
        return BooleanDependency.OTIMES_IDENTITY;
    }

    /** Find the first occurrence of the given event in the event queue. If
     *  <code>findRefinements</code> is true, then the refinements of the given
     *  event are also searched for.
     *
     *  @param event The event.
     *  @param findRefinements Whether refinements of the given event should be
     *   searched for.
     *  @return The TimedEvent that contains the event or its refinement.
     *  @exception IllegalActionException If the refinements of the given event
     *   cannot be retrieved.
     */
    public TimedEvent findFirst(Event event, boolean findRefinements)
    throws IllegalActionException {
        Set<TypedActor> refinementSet = new HashSet<TypedActor>();
        TypedActor[] refinements = event.getRefinement();
        if (refinements != null) {
            for (TypedActor refinement : refinements) {
                refinementSet.add(refinement);
            }
        }
        Iterator<TimedEvent> iterator = _eventQueue.iterator();
        while (iterator.hasNext()) {
            TimedEvent timedEvent = iterator.next();
            if (timedEvent.contents == event
                    || refinementSet.contains(timedEvent.contents)) {
                return timedEvent;
            }
        }
        return null;
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
        _fireAt(actor, time, null, null);
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
        _fireAt(event, time, arguments, null);
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

    /** Return the current time object of the model being executed by this
     *  director.
     *  This time can be set with the setModelTime() method. In this base
     *  class, time never increases, and there are no restrictions on valid
     *  times.
     *
     *  @return The current time.
     *  @see #setModelTime(Time)
     */
    public Time getModelTime() {
        if (_delegateFireAt && _isEmbedded()) {
            NamedObj container = getContainer();
            if (container instanceof Actor) {
                return ((Actor) container).getExecutiveDirector()
                        .getModelTime();
            }
        }
        return super.getModelTime();
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

        NamedObj container = getContainer();
        if (container instanceof ModalModel) {
            ((ModalModel) container).getController().initialize();
        }
    }

    /** Initialize the given actor, unless it is a RefinementActor (which will
     *  be initialized when the event that it refines is fired).  This method is
     *  generally called
     *  by the initialize() method of the director, and by the manager
     *  whenever an actor is added to an executing model as a
     *  mutation.  This method will generally perform domain-specific
     *  initialization on the specified actor and call its
     *  initialize() method.  In this base class, only the actor's
     *  initialize() method of the actor is called and no
     *  domain-specific initialization is performed.  Typical actions
     *  a director might perform include starting threads to execute
     *  the actor or checking to see whether the actor can be managed
     *  by this director.  For example, a time-based domain (such as
     *  CT) might reject sequence based actors.
     *  @param actor The actor that is to be initialized.
     *  @exception IllegalActionException If the actor is not
     *  acceptable to the domain.  Not thrown in this base class.
     */
    public void initialize(Actor actor) throws IllegalActionException {
        if (!(actor instanceof RefinementActor)) {
            super.initialize(actor);
        }
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
            if (_eventQueue.isEmpty()) {
                result = false;
            } else {
                if (_isTopLevel()) {
                    TimedEvent event = _eventQueue.peek();
                    setModelTime(event.timeStamp);
                }
                if (_isEmbedded()) {
                    _requestFiring();
                    _delegateFireAt = true;
                }
            }
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

        if (!_hasInput() || _inputQueue.isEmpty()) {
            if (!_eventQueue.isEmpty()) {
                Time modelTime = getModelTime();
                Time nextEventTime = (_eventQueue.peek()).timeStamp;
                while (modelTime.compareTo(nextEventTime) > 0) {
                    _eventQueue.poll();

                    if (!_eventQueue.isEmpty()) {
                        TimedEvent event = _eventQueue.peek();
                        nextEventTime = event.timeStamp;
                    } else {
                        nextEventTime = Time.POSITIVE_INFINITY;
                    }
                }

                if (!nextEventTime.equals(modelTime)) {
                    result = false;
                }

            }
        }

        if (_debugging) {
            _debug("Prefire returns: " + result);
        }

        _delegateFireAt = !result;

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

    /** Request that the director cease execution altogether.
     */
    public void stop() {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _stopRequested = true;
                _eventQueue.notifyAll();
            }
        }

        ERGController controller;
        try {
            controller = getController();
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        List<Event> events = controller.entityList(Event.class);
        for (Event event : events) {
            event.stop();
        }

        super.stop();
    }

    /** Request the execution of the current iteration to complete.
     *  If the director is paused waiting for events to appear in the
     *  event queue, then it stops waiting,
     *  and calls stopFire() for all actors
     *  that are deeply contained by the container of this director.
     */
    public void stopFire() {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _eventQueue.notifyAll();
            }
        }

        super.stopFire();
    }

    /** Invoke the wrapup() method of the superclass, and clear the event queue.
     *
     *  @exception IllegalActionException If the wrapup() method of
     *   the superclass throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _eventQueue.clear();
        _inputQueue.clear();
        _eventInstanceTable.clear();
        _eventInstanceList = null;
        _scheduledRefinements.clear();
    }

    /** A Boolean parameter that decides whether simultaneous events should be
     *  placed in the event queue in the last-in-first-out (LIFO) fashion or
     *  not.
     */
    public Parameter LIFO;

    /** Attribute specifying the name of the ERG controller in the
     *  container of this director. This director must have an ERG
     *  controller that has the same container as this director,
     *  otherwise an IllegalActionException will be thrown when action
     *  methods of this director are called.
     */
    public StringAttribute controllerName;

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
    public static class TimedEvent extends ptolemy.actor.util.TimedEvent {

        /** Construct a TimedEvent.
         *
         *  @param time The model time at which the actor or event is scheduled.
         *  @param object The actor or event.
         *  @param arguments Arguments to the event.
         */
        public TimedEvent(Time time, Object object, ArrayToken arguments,
                RefiringData data) {
            super(time, object);
            this.arguments = arguments;
            this.data = data;
        }

        /** Display timeStamp and contents.
         *
         *  @return A string that describes this timed event.
         */
        public String toString() {
            StringBuffer result = new StringBuffer(contents + ".");
            if (data == null) {
                result.append("fire");
            } else {
                result.append("refire");
            }
            if (arguments != null) {
                result.append('(');
                result.append(arguments);
                result.append(')');
            }
            result.append(" at ");
            result.append(timeStamp);
            return result.toString();
        }

        /** Arguments to the event. */
        public ArrayToken arguments;

        /** The refiring data returned from the previous fire() or refire(), or
            null if the scheduled firing is the first one. */
        public RefiringData data;
    }

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
        _eventInstanceTable.clear();
        _eventInstanceList = null;
        _scheduledRefinements.clear();

        ERGController controller = getController();
        if (_isInController()) {
            ERGModalModel modalModel =
                (ERGModalModel) getContainer().getContainer();
            _currentTime = modalModel.getDirector().getModelTime();

            Iterator<?> entities = controller.deepEntityList().iterator();
            while (entities.hasNext()) {
                Event event = (Event) entities.next();
                if (event.isInitialEvent()) {
                    TimedEvent newEvent = new TimedEvent(_currentTime, event,
                            null, null);
                    _addEvent(newEvent, false);
                }
            }
        } else {
            TimedEvent newEvent = new TimedEvent(_currentTime, controller,
                    null, null);
            _addEvent(newEvent, false);
            if (_isEmbedded()) {
                _requestFiring();
            }
        }

        if (_isEmbedded()) {
            _delegateFireAt = true;
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

    /** Add an event to the event queue in this director. If addToInputQueue is
     *  true, add that event to the input queue as well.
     *
     *  @param event The event.
     *  @param addToInputQueue Whether the event should be added to the input
     *  queue as well.
     */
    private void _addEvent(TimedEvent event, boolean addToInputQueue) {
        try {
            _eventComparator.setNewEvent(event);
            _eventQueue.add(event);
            if (addToInputQueue) {
                _inputQueue.add(event);
            }
            List<TimedEvent> list = _eventInstanceTable.get(event);
            if (list == null) {
                list = _eventInstanceList;
                if (list == null) {
                    list = new LinkedList<TimedEvent>();
                }
                _eventInstanceTable.put(event, list);
            }
            list.add(event);
        } finally {
            _eventComparator.setNewEvent(null);
        }
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
                _eventInstanceList = _eventInstanceTable.remove(timedEvent);
                try {
                    _eventInstanceList.remove(timedEvent);
                    _eventQueue.remove(timedEvent);
                    _inputQueue.remove(timedEvent);

                    actor.fire();
                    boolean postfire = actor.postfire();
                    if (!postfire) {
                        _scheduledRefinements.remove(actor);
                    }
                    boolean scheduleNext = !postfire && _eventInstanceList
                            .isEmpty();
                    _eventInstanceList = null;
                    if (scheduleNext) {
                        List<Event> events = getController().entityList(
                                Event.class);
                        for (Event event : events) {
                            TypedActor[] refinements = event.getRefinement();
                            boolean scheduled = false;
                            if (refinements != null) {
                                for (TypedActor refinement : refinements) {
                                    if (refinement == actor) {
                                        if (event.isFinalEvent()) {
                                            _eventQueue.clear();
                                        } else {
                                            event.scheduleEvents();
                                        }
                                        scheduled = true;
                                        break;
                                    }
                                }
                            }
                            if (scheduled) {
                                break;
                            }
                        }
                    }
                    return true;
                } finally {
                    // Set the list to null in any case.
                    _eventInstanceList = null;
                }
            } else {
                return false;
            }
        } else if (contents instanceof Event) {
            Event event = (Event) contents;
            _eventInstanceList = _eventInstanceTable.remove(timedEvent);
            try {
                _eventInstanceList.remove(timedEvent);
                _eventQueue.remove(timedEvent);
                _inputQueue.remove(timedEvent);

                controller._setCurrentEvent(event);
                RefiringData data;
                if (timedEvent.data == null) {
                    data = event.fire(timedEvent.arguments);
                } else {
                    data = event.refire(timedEvent.arguments, timedEvent.data);
                }
                if (data != null) {
                    _fireAt(event, getModelTime().add(data.getTimeAdvance()),
                            timedEvent.arguments, data);
                }

                boolean scheduled = false;
                if (timedEvent.data == null) {
                    TypedActor[] refinements = event.getRefinement();
                    if (refinements != null) {
                        for (TypedActor refinement : refinements) {
                            if (!_scheduledRefinements.contains(refinement) &&
                                    event._scheduleRefinement(refinement)) {
                                _scheduledRefinements.add(refinement);
                                scheduled = true;
                            }
                        }
                    }
                }

                boolean scheduleNext = !scheduled && data == null &&
                        _eventInstanceList.isEmpty();
                _eventInstanceList = null;
                if (scheduleNext) {
                    if (event.isFinalEvent()) {
                        _eventQueue.clear();
                    } else {
                        event.scheduleEvents();
                    }
                }
            } finally {
                // Set the list to null in any case.
                _eventInstanceList = null;
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
    private void _fireAt(Object object, Time time, ArrayToken arguments,
            RefiringData data) throws IllegalActionException {
        if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(this,
                    "Attempt to schedule an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + time);
        }

        TimedEvent timedEvent = new TimedEvent(time, object, arguments, data);
        Time topTime = null;
        if (!_eventQueue.isEmpty()) {
            topTime = _eventQueue.peek().timeStamp;
        }

        boolean addToInputQueue = false;
        if (object instanceof Actor) {
            addToInputQueue = true;
        } else if (object instanceof Event) {
            Event event = (Event) object;
            if (event.fireOnInput()) {
                addToInputQueue = true;
            }
        }
        _addEvent(timedEvent, addToInputQueue);

        if (_delegateFireAt && (topTime == null || topTime.compareTo(time)
                > 0)) {
            _requestFiring();
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

    /** Cached reference to mode controller. */
    private ERGController _controller = null;

    /** Version of cached reference to mode controller. */
    private long _controllerVersion = -1;

    /** Whether fireAt() invocations should be delegated to the director at the
    higher level. */
    private boolean _delegateFireAt = false;

    /** The comparator used to compare the time stamps of any two events. */
    private EventComparator _eventComparator = new EventComparator();

    /** The list containing the event and refinements belonging to the current
        instance of events. */
    private List<TimedEvent> _eventInstanceList;

    /** A table that maps any refiring of an event or refinement of an event to
    the list of refirings and refinements of the same event. The completion of
    processing the event is the completion of all those tasks in the list. */
    private Map<TimedEvent, List<TimedEvent>> _eventInstanceTable =
        new HashMap<TimedEvent, List<TimedEvent>>();

    /** The event queue. */
    private PriorityQueue<TimedEvent> _eventQueue =
        new PriorityQueue<TimedEvent>(10, _eventComparator);

    /** The event queue that contains only events that can react to inputs. */
    private PriorityQueue<TimedEvent> _inputQueue =
        new PriorityQueue<TimedEvent>(5, _eventComparator);

    /** The real time at which the execution started. */
    private long _realStartTime;

    /** The refinements that have been scheduled. A refinement cannot be
        scheduled again before it has finished executing. */
    private Set<TypedActor> _scheduledRefinements = new HashSet<TypedActor>();

    //////////////////////////////////////////////////////////////////////////
    //// EventComparator

    /**
     A comparator that compares the time stamps of two timed events in the event
     queue.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class EventComparator implements Comparator<TimedEvent> {

        /** Compare the two timed events. If the first timed event has a time
         *  stamp less than that of the second event, then a negative integer is
         *  returned. If the first timed event has a time stamp greater than
         *  that of the second event, then a positive integer is returned. If
         *  they have exactly the same time stamp, then either a negative
         *  integer or a positive integer is returned, depending on the LIFO
         *  parameter of the director. If LIFO is set to true, then a negative
         *  integer is returned in that case. Otherwise, a positive integer is
         *  returned instead.
         *
         *  @param a The first timed event.
         *  @param b The second timed event.
         *  @return -1 or +1 depending on whether the time stamp of the first
         *   timed event is less than, equal to, or greater than that of the
         *   second.
         */
        public int compare(TimedEvent a, TimedEvent b) {
            if (a == b) {
                return 0;
            }

            int result = a.timeStamp.compareTo(b.timeStamp);
            if (result == 0) {
                if (_newEvent == null || a != _newEvent && b != _newEvent) {
                    return 0;
                } else {
                    try {
                        boolean isLIFO =
                            ((BooleanToken) LIFO.getToken()).booleanValue();
                        if (a == _newEvent) {
                            return isLIFO ? -1 : 1;
                        } else if (b == _newEvent) {
                            return isLIFO ? 1 : -1;
                        } else {
                            // Shouldn't reach here.
                            return 0;
                        }
                    } catch (IllegalActionException e) {
                        throw new KernelRuntimeException(e, "Unable to " +
                                "obtain value for the LIFO parameter.");
                    }
                }
            } else {
                return result;
            }
        }

        /** Set the new event to be added to a queue that uses this comparator,
         *  so that the return value of {@link #compare(TimedEvent, TimedEvent)}
         *  depends on the LIFO setting.
         *
         *  @param newEvent The new event, or null if this comparator is not
         *  being used for adding a new event.
         */
        public void setNewEvent(TimedEvent newEvent) {
            _newEvent = newEvent;
        }

        /** The new event, or null. */
        private TimedEvent _newEvent;
    }
}
