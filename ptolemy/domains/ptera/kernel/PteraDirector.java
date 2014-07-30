/* The Ptera director.

@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.domains.ptera.kernel;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.RefinementActor;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.ptera.kernel.Event.RefiringData;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

/**
 A Ptera director implements the Event Relationship Graph semantics, and can be
 used by Ptera controllers (instances of {@link PteraController}) in Ptera modal
 models (instances of {@link PteraModalModel}). This director has an event queue
 where events can be scheduled and are ordered by their time stamps. Events are
 processed according to their time-stamp order. Actions of the events are
 executed when the events are processed.
 <p>
 Between events there may be scheduling relations. If an event schedules another
 to occur after a certain amount of model time, then the other event is placed
 in the event queue when the first event is processed. Scheduling relations may
 be guarded by boolean expressions.
 <p>
 Each Ptera controller transparently creates a Ptera director inside. For a
 Ptera controller that serves as refinement of an event in another Ptera
 controller, the Ptera director in it invokes the fireAt() method of the Ptera
 director in the containing Ptera controller. When multiple events are scheduled
 in one firing of the inner Ptera director, only one invocation of fireAt() is
 made in postfire() with the most imminent event as the parameter.
 <p>
 This director can be used in DE as a contained model of computation. It can
 also be used to control timed or untimed models of computation, such as DE,
 dataflow, and FSM.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 @see DEDirector
 */
public class PteraDirector extends Director implements ValueListener {

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
    public PteraDirector(CompositeEntity container, String name)
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
    @Override
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
            timedEvent.canceled = true;
            Object contents = timedEvent.contents;
            if (contents instanceof TypedActor) {
                _initializedRefinements.remove(contents);
                if (contents instanceof PteraController) {
                    ((PteraController) contents).director._clearState();
                }
            }
            for (Set<TimedEvent> set : _eventsListeningToPorts.values()) {
                set.remove(timedEvent);
            }
            for (Set<TimedEvent> set : _eventsListeningToVariables.values()) {
                set.remove(timedEvent);
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PteraDirector newObject = (PteraDirector) super.clone(workspace);
        newObject._controller = null;
        newObject._controllerVersion = -1;
        newObject._eventQueue = new LinkedList<TimedEvent>();
        newObject._eventsListeningToPorts = new HashMap<Port, Set<TimedEvent>>();
        newObject._eventsListeningToVariables = new HashMap<Variable, Set<TimedEvent>>();
        newObject._initializedRefinements = new HashSet<TypedActor>();
        return newObject;
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
                if (event._isActiveRefinement(refinement)) {
                    refinementSet.add(refinement);
                }
            }
        }
        for (TimedEvent timedEvent : _eventQueue) {
            if (!timedEvent.canceled
                    // FindBugs: GC: Suspicious calls to generic collection methods
                    && timedEvent instanceof TypedActor
                    && (timedEvent.contents == event || refinementSet
                    .contains(timedEvent.contents))) {
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
    @Override
    public void fire() throws IllegalActionException {
        _debug(new PteraDebugEvent(this, "Fire at " + getModelTime() + "."));

        _ending = false;

        if (!_isInController()) {
            PteraModalModel modalModel = (PteraModalModel) getContainer();
            List<?> controllers = modalModel.entityList(PteraController.class);
            for (Object controllerObject : controllers) {
                ((PteraController) controllerObject).readInputs();
            }
        }

        PteraController controller = getController();
        boolean hasInput = controller.hasInput();
        boolean synchronize = controller.synchronizeToRealtime();
        Time modelTime = getModelTime();

        Iterator<TimedEvent> eventQueueIterator = _eventQueue.iterator();
        int position = 0;
        while (eventQueueIterator.hasNext()) {
            TimedEvent event = eventQueueIterator.next();
            if (event.canceled) {
                eventQueueIterator.remove();
                _notifyEventQueueDebugListeners(false, true, position, null,
                        null, null);
            } else {
                break;
            }
            position++;
        }
        List<TimedEvent> eventQueue = new LinkedList<TimedEvent>(_eventQueue);
        Set<TimedEvent> firedEvents = new HashSet<TimedEvent>();

        if (hasInput) {
            // Fire the refinements of all input events.
            for (TypedActor refinement : _initializedRefinements) {
                _fireActor(refinement, null);
                if (_ending) {
                    return;
                }
            }

            Set<TimedEvent> timedEvents = new LinkedHashSet<TimedEvent>();
            Iterator<Map.Entry<Port, Set<TimedEvent>>> entryIterator = _eventsListeningToPorts
                    .entrySet().iterator();
            ParserScope scope = controller.getPortScope();
            while (entryIterator.hasNext()) {
                Map.Entry<Port, Set<TimedEvent>> entry = entryIterator.next();
                Port port = entry.getKey();
                BooleanToken token = (BooleanToken) scope.get(port.getName()
                        + "_isPresent");
                if (token != null && token.booleanValue()) {
                    timedEvents.addAll(entry.getValue());
                    entryIterator.remove();
                }
            }
            for (TimedEvent event : timedEvents) {
                if (!event.canceled && event.contents instanceof Event) {
                    firedEvents.add(event);
                    _fire(event);
                    if (_ending) {
                        return;
                    }
                }
            }
        }

        // Fire the next imminent event.
        if (!_stopRequested) {
            TimedEvent timedEvent = null;
            for (TimedEvent event : eventQueue) {
                if (!event.canceled) {
                    timedEvent = event;
                    break;
                }
            }
            if (timedEvent != null) {
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
                    if (_ending) {
                        return;
                    }
                }
            }
        }
    }

    /** Request a firing of the given actor at the given model
     *  time.
     *  This method puts the actor in the event queue in time-stamp order. The
     *  actor will be fired later when the model time reaches the scheduled time
     *  in a way similar to events being processed from the event queue.
     *
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @param microstep The microstep (ignored by this director).
     *  @return The time at which the actor passed as an argument
     *   will be fired.
     *  @exception IllegalActionException If the operation is not
     *   permissible (e.g. the given time is in the past), or if the executive director
     *   does not support fireAt() precisely (it does not agree to refire
     *   this Ptera model at the requested time).
     */
    @Override
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        if (getContainer() instanceof ModalModel
                && actor instanceof RefinementActor) {
            Event event = (Event) ((RefinementActor) actor).getRefinedState();
            if (event != null) {
                TimedEvent timedEvent = new TimedEvent(actor, time, null, null,
                        false);
                event.getController().director.fireAt(timedEvent, null);
                return time;
            }
        }
        TimedEvent timedEvent = new TimedEvent(actor, time, null, null, false);
        fireAt(timedEvent, null);
        return time;
    }

    /** Request to process an event at the given model time. This method puts
     *  the event in the event queue in time-stamp order. The event will be
     *  processed later when the model time reaches the scheduled time.
     *
     *  @param event The event scheduled to be processed.
     *  @param time The scheduled time.
     *  @param arguments The arguments to the event, which must be either an
     *   ArrayToken or a RecordToken.
     *  @param triggers A list of ports and variables that triggers the event
     *   before its scheduled time is reached.
     *  @param reset Whether the refinement of the scheduled event should be
     *   reinitialized when the event is processed.
     *  @exception IllegalActionException If the operation is not
     *   permissible (e.g. the given time is in the past), or if the executive
     *   director does not support fireAt() precisely (it does not agree to
     *   refire this Ptera model at the requested time)
     */
    public void fireAt(Event event, Time time, Token arguments,
            List<NamedObj> triggers, boolean reset)
                    throws IllegalActionException {
        fireAt(new TimedEvent(event, time, arguments, null, reset), triggers);
    }

    /** Request to process an event at the given model time. This method puts
     *  the event in the event queue in time-stamp order. The event will be
     *  processed later when the model time reaches the scheduled time.
     *
     *  @param timedEvent The TimedEvent object to be scheduled.
     *  @param triggers A list of ports and variables that triggers the event
     *   before its scheduled time is reached.
     *  @exception IllegalActionException If the operation is not
     *   permissible (e.g. the given time is in the past), or if the executive
     *   director does not support fireAt() precisely (it does not agree to
     *   refire this Ptera model at the requested time)
     */
    public synchronized void fireAt(TimedEvent timedEvent,
            List<NamedObj> triggers) throws IllegalActionException {

        _fireAtReceived = true;

        Time topTime = null;
        if (!_eventQueue.isEmpty()) {
            topTime = _eventQueue.get(0).timeStamp;
        }

        _addEvent(timedEvent);

        if (triggers != null) {
            for (NamedObj trigger : triggers) {
                Set<TimedEvent> eventSet = null;
                if (trigger instanceof Port) {
                    Port port = (Port) trigger;
                    eventSet = _eventsListeningToPorts.get(port);
                    if (eventSet == null) {
                        eventSet = new LinkedHashSet<TimedEvent>();
                        _eventsListeningToPorts.put(port, eventSet);
                    }
                } else if (trigger instanceof Variable) {
                    Variable variable = (Variable) trigger;
                    eventSet = _eventsListeningToVariables.get(variable);
                    if (eventSet == null) {
                        eventSet = new LinkedHashSet<TimedEvent>();
                        _eventsListeningToVariables.put(variable, eventSet);
                        variable.addValueListener(this);
                    }
                }
                if (eventSet != null) {
                    eventSet.add(timedEvent);
                }
            }
        }

        if (_delegateFireAt
                && (topTime == null || topTime.compareTo(timedEvent.timeStamp) > 0)) {
            _requestFiring();
        }
    }

    /** Return the Ptera controller has the same container as this director. The
     *  name of the Ptera controller is specified by the <i>controllerName</i>
     *  attribute. The mode controller must have the same container as
     *  this director.
     *  This method is read-synchronized on the workspace.
     *
     *  This method is a duplication of {@link
     *  ptolemy.domains.modal.kernel.FSMDirector#getController()}. However, due to
     *  the class hierarchy, there is no easy way to reuse the code.
     *
     *  @return The mode controller of this director.
     *  @exception IllegalActionException If no controller is found.
     */
    public PteraController getController() throws IllegalActionException {
        if (_controllerVersion == workspace().getVersion()) {
            return _controller;
        }

        try {
            workspace().getReadAccess();

            NamedObj container = getContainer();
            if (_isInController()) {
                _controller = (PteraController) container;
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

                _controller = (PteraController) entity;
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
    @Override
    public Time getModelTime() {
        if (_delegateFireAt && isEmbedded()) {
            NamedObj container = getContainer();
            if (container instanceof Actor) {
                return ((Actor) container).getExecutiveDirector()
                        .getModelTime();
            }
        }
        return super.getModelTime();
    }

    /** Return the exception (if any) that occurred when the refinement was fired.
     *  @return The exception.
     */
    public IllegalActionException getModelException() {
        return _refinementException;
    }

    /** Initialize the model controlled by this director. The initialize()
     *  method of the superclass is called. After that, the initial schedule is
     *  computed. To initialize a schedule, the initial events are placed in the
     *  event queue.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        NamedObj container = getContainer();
        _insertInitialEvents();
        if (container instanceof ModalModel) {
            ((ModalModel) container).getController().initialize();
        }

        _ending = false;
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
    @Override
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
     *  @exception IllegalActionException If the executive director does
     *   not support fireAt() precisely (that is, it does not agree to
     *   refire this Ptera model at the requested time).
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();

        Iterator<TimedEvent> eventQueueIterator = _eventQueue.iterator();
        int position = 0;
        while (eventQueueIterator.hasNext()) {
            TimedEvent event = eventQueueIterator.next();
            if (event.canceled) {
                eventQueueIterator.remove();
                _notifyEventQueueDebugListeners(false, true, position, null,
                        null, null);
            } else {
                break;
            }
            position++;
        }

        if (result) {
            if (_eventQueue.isEmpty()) {
                result = false;
            } else {
                if (_isTopLevel()) {
                    TimedEvent event = _eventQueue.get(0);
                    setModelTime(event.timeStamp);
                }
                if (isEmbedded()) {
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
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();

        if (!_hasInput()) {
            if (_eventQueue.isEmpty()) {
                result = false;
            } else {
                Time modelTime = getModelTime();
                Time nextEventTime = _eventQueue.get(0).timeStamp;
                while (modelTime.compareTo(nextEventTime) > 0) {
                    _eventQueue.remove(0);
                    _notifyEventQueueDebugListeners(false, true, 0, null, null,
                            null);

                    if (!_eventQueue.isEmpty()) {
                        TimedEvent event = _eventQueue.get(0);
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
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _clearState();
        _realStartTime = System.currentTimeMillis();
    }

    /** Request that the director cease execution altogether.
     */
    @Override
    public void stop() {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _stopRequested = true;
                _eventQueue.notifyAll();
            }
        }

        PteraController controller;
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
    @Override
    public void stopFire() {
        if (_eventQueue != null) {
            synchronized (_eventQueue) {
                _eventQueue.notifyAll();
            }
        }

        super.stopFire();
    }

    /** Monitor the change of a variable specified by the <code>settable</code>
     *  argument if the execution has started, and invokes fireAt() to request
     *  to fire all the events that are listening to that variable at the
     *  current model time.
     *
     *  @param settable The variable that has been changed.
     */
    @Override
    public void valueChanged(Settable settable) {
        Variable variable = (Variable) settable;
        Set<TimedEvent> eventList = _eventsListeningToVariables
                .remove(variable);
        if (eventList != null) {
            variable.removeValueListener(this);
            Time modelTime = getModelTime();
            for (TimedEvent timedEvent : eventList) {
                if (modelTime.compareTo(timedEvent.timeStamp) < 0) {
                    Event event = (Event) timedEvent.contents;
                    try {
                        cancel(event);
                        TimedEvent timedEvent2 = new TimedEvent(event,
                                modelTime, timedEvent.arguments, null,
                                timedEvent.reset);
                        fireAt(timedEvent2, null);
                    } catch (IllegalActionException e) {
                        // This shouldn't happen because this event does not
                        // have any refinement.
                        throw new InternalErrorException(e);
                    }
                }
            }
        }
    }

    /** Invoke the wrapup() method of the superclass, and clear the event queue.
     *
     *  @exception IllegalActionException If the wrapup() method of
     *   the superclass throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _clearState();
        for (Variable variable : _eventsListeningToVariables.keySet()) {
            variable.removeValueListener(this);
        }
    }

    /** A Boolean parameter that decides whether simultaneous events should be
     *  placed in the event queue in the last-in-first-out (LIFO) fashion or
     *  not.
     */
    public Parameter LIFO;

    /** Attribute specifying the name of the Ptera controller in the
     *  container of this director. This director must have a Ptera
     *  controller that has the same container as this director,
     *  otherwise an IllegalActionException will be thrown when action
     *  methods of this director are called.
     */
    public StringAttribute controllerName;

    /**
     The class to encapsulate information to be stored in an entry in the event
     queue.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class TimedEvent extends ptolemy.actor.util.TimedEvent {

        /** Construct a TimedEvent.
         *
         *  @param object The actor or event.
         *  @param time The model time at which the actor or event is scheduled.
         *  @param arguments Arguments to the event, which must be either an
         *   ArrayToken or a RecordToken.
         *  @param data The refiring data for the next refire() invocation of
         *   the event that causes this method to be called, or null if none.
         *  @param reset Whether the refinement of the scheduled event should be
         *   reinitialized when the event is processed.
         */
        public TimedEvent(Object object, Time time, Token arguments,
                RefiringData data, boolean reset) {
            super(time, object);
            this.arguments = arguments;
            this.data = data;
            this.reset = reset;
            this.canceled = false;
        }

        /** Display timeStamp and contents.
         *
         *  @return A string that describes this timed event.
         */
        @Override
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
        public Token arguments;

        /** Whether this event has been canceled. */
        public boolean canceled;

        /** The refiring data returned from the previous fire() or refire(), or
            null if the scheduled firing is the first one. */
        public RefiringData data;

        /** Whether the refinement of the scheduled event should be
         *  (re)initialized when the event is processed. */
        public boolean reset;
    }

    ///////////////////////////////////////////////////////////////////
    //// TimedEvent

    /** Insert initial events into the event queue, and request firing from the
     *  director at the higher level, if any.
     *
     *  @exception IllegalActionException If whether an event is initial event
     *  cannot be checked, or if the executive director does not support
     *  fireAt() precisely (it does not agree to refire this Ptera at the
     *  requested time).
     */
    protected void _insertInitialEvents() throws IllegalActionException {
        PteraController controller = getController();
        if (_isInController()) {
            PteraModalModel modalModel = (PteraModalModel) getContainer()
                    .getContainer();
            localClock.setLocalTime(modalModel.getDirector().getModelTime());

            Iterator<?> entities = controller.deepEntityList().iterator();
            List<Event> initialEvents = new LinkedList<Event>();
            while (entities.hasNext()) {
                Event event = (Event) entities.next();
                if (event.isInitialEvent()) {
                    initialEvents.add(event);
                }
            }
            final boolean lifo = ((BooleanToken) LIFO.getToken())
                    .booleanValue();
            Collections.sort(initialEvents, new Comparator<Event>() {
                @Override
                public int compare(Event event1, Event event2) {
                    int eventCompare = event1.getName().compareTo(
                            event2.getName());
                    // FIXME: FindBugs: "RV: Negating the result of
                    // compareTo()/compare()
                    // (RV_NEGATING_RESULT_OF_COMPARETO)
                    //
                    // "This code negatives the return value
                    // of a compareTo or compare method. This
                    // is a questionable or bad programming
                    // practice, since if the return value is
                    // Integer.MIN_VALUE, negating the return
                    // value won't negate the sign of the
                    // result. You can achieve the same
                    // intended result by reversing the order
                    // of the operands rather than by negating
                    // the results."
                    //return lifo ? -eventCompare : eventCompare;
                    if (lifo) {
                        if (eventCompare == Integer.MIN_VALUE) {
                            return Integer.MAX_VALUE;
                        } else {
                            return -eventCompare;
                        }
                    } else {
                        return eventCompare;
                    }
                }
            });
            for (Event event : initialEvents) {
                Event refinedEvent = (Event) controller.getRefinedState();
                RecordToken arguments = null;
                if (refinedEvent != null) {
                    List<String> names = event.parameters.getParameterNames();
                    if (names.size() > 0) {
                        String[] tokenNames = new String[names.size()];
                        Token[] tokenValues = new Token[names.size()];
                        int i = 0;
                        for (String name : names) {
                            tokenNames[i] = name;
                            tokenValues[i] = ((Variable) refinedEvent
                                    .getAttribute(name)).getToken();
                            i++;
                        }
                        arguments = new RecordToken(tokenNames, tokenValues);
                    }
                }
                TimedEvent newEvent = new TimedEvent(event, getModelTime(),
                        arguments, null, false);
                _addEvent(newEvent);
            }
            if (getController().getRefinedState() != null) {
                _requestFiring();
            }
        } else {
            TimedEvent newEvent = new TimedEvent(controller, getModelTime(),
                    null, null, false);
            _addEvent(newEvent);
            _initializedRefinements.add(controller);
            if (isEmbedded()) {
                _requestFiring();
            }
        }

        if (isEmbedded()) {
            _delegateFireAt = true;
        }
    }

    /** Return whether this director is top-level. A Ptera director is top-level
     *  if _isTopLevel() of the superclass returns true, and this director is
     *  directly associated with a modal model.
     *
     *  @return True if this director is top-level.
     */
    @Override
    protected boolean _isTopLevel() {
        return super._isTopLevel() && !_isInController();
    }

    /** Add an event to the given event queue that remains to be sorted after
     *  the addition.
     *
     *  @param eventQueue The event queue.
     *  @param event The event.
     *  @exception IllegalActionException If the LIFO parameter of this director
     *   cannot be retrieved.
     */
    private void _addEvent(List<TimedEvent> eventQueue, TimedEvent event)
            throws IllegalActionException {
        if (event.timeStamp.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(this,
                    "Attempt to schedule an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + event.timeStamp);
        }

        ListIterator<TimedEvent> iterator = eventQueue.listIterator();
        Object contents = event.contents;
        Time time = event.timeStamp;
        boolean lifo = ((BooleanToken) LIFO.getToken()).booleanValue();
        int position = 0;
        while (true) {
            if (iterator.hasNext()) {
                TimedEvent next = iterator.next();
                int timeCompare = time.compareTo(next.timeStamp);
                if (timeCompare < 0 || timeCompare == 0 && lifo) {
                    iterator.previous();
                    iterator.add(event);

                    _notifyEventQueueDebugListeners(true, false, position,
                            time, contents, event.arguments);

                    break;
                }
            } else {
                iterator.add(event);

                _notifyEventQueueDebugListeners(true, false, position, time,
                        contents, event.arguments);

                break;
            }
            position++;
        }
    }

    /** Add an event to the event queue in this director. If the event contains
     *  an actor as its contents, add the event to the refinement queue as well.
     *
     *  @param event The event.
     *  @exception IllegalActionException If the LIFO parameter of this director
     *   cannot be retrieved.
     */
    private void _addEvent(TimedEvent event) throws IllegalActionException {
        _addEvent(_eventQueue, event);
    }

    /** Clear the state of this Ptera director and the Ptera directors of the
     *  refinements recursively, so that the event queues become empty.
     */
    private void _clearState() {
        for (TypedActor refinement : _initializedRefinements) {
            if (refinement instanceof PteraController) {
                PteraDirector director = ((PteraController) refinement).director;
                director._clearState();
            }
        }
        _eventQueue.clear();
        _eventsListeningToPorts.clear();
        _eventsListeningToVariables.clear();
        _initializedRefinements.clear();
        _delegateFireAt = false;
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
        int position = _eventQueue.indexOf(timedEvent);
        if (position >= 0) {
            _eventQueue.remove(position);
            _notifyEventQueueDebugListeners(false, false, position, null, null,
                    null);
        }

        PteraController controller = getController();
        Object contents = timedEvent.contents;
        if (contents instanceof Actor) {
            return _fireActor((Actor) contents, timedEvent);
        } else if (contents instanceof Event) {
            Event event = (Event) contents;
            for (Set<TimedEvent> set : _eventsListeningToPorts.values()) {
                set.remove(timedEvent);
            }
            for (Set<TimedEvent> set : _eventsListeningToVariables.values()) {
                set.remove(timedEvent);
            }

            controller._setCurrentEvent(event);
            RefiringData data;
            if (timedEvent.data == null) {
                controller.event(new EventDebugEvent(controller, event, false));
                data = event.fire(timedEvent.arguments);
            } else {
                data = event.refire(timedEvent.arguments, timedEvent.data);
            }
            if (data != null) {
                TimedEvent timedEvent2 = new TimedEvent(event, getModelTime()
                        .add(data.getTimeAdvance()), timedEvent.arguments,
                        data,
                        // If it is a refiring, do not reinitialize the
                        // refinement.
                        false);
                fireAt(timedEvent2, null);
            }

            boolean scheduled = false;
            if (timedEvent.data == null) {
                TypedActor[] refinements = event.getRefinement();
                if (refinements != null) {
                    for (TypedActor refinement : refinements) {
                        if (!event._isActiveRefinement(refinement)) {
                            continue;
                        }

                        try {
                            _refinementException = null;
                            _initializeAndFireRefinement(refinement,
                                    timedEvent.reset);
                            scheduled = true;
                        } catch (IllegalActionException ex) { //FIXME: Dai
                            _refinementException = ex;
                        }

                    }
                }
            }

            boolean scheduleNext = !scheduled && data == null;
            if (scheduleNext) {
                controller.event(new EventDebugEvent(controller, event, true));

                if (event.isFinalEvent()) {
                    for (TimedEvent eventToCancel : _eventQueue) {
                        eventToCancel.canceled = true;
                    }
                } else {
                    event.scheduleEvents();

                    if (!_ending && event.isEndingEvent()) {
                        _ending = true;
                    }
                }
            }

            return true;
        } else {
            throw new InternalErrorException(this, null, "The contents of a "
                    + "TimedEvent can only be Actor or Event.");
        }
    }

    /** Fire an actor. The prefire(), fire() and postfire() methods of the actor
     *  are called. If timedEvent is not null, then it is removed from the event
     *  queues.
     *
     *  @param actor The actor to fire.
     *  @param timedEvent The timed event that contains the actor as its
     *  contents, or null if none.
     *  @return True if an event is processed or an actor is fired, or false if
     *  the prefire() method of the actor returns false.
     *  @exception IllegalActionException If firing the actor throws it.
     */
    private boolean _fireActor(Actor actor, TimedEvent timedEvent)
            throws IllegalActionException {
        if (actor.prefire()) {
            if (timedEvent != null) {
                int position = _eventQueue.indexOf(timedEvent);
                if (position >= 0) {
                    _eventQueue.remove(position);
                    _notifyEventQueueDebugListeners(false, false, position,
                            null, null, null);
                }
            }

            actor.fire();
            boolean postfire = actor.postfire();

            boolean ending = false;
            if (postfire && actor instanceof PteraController) {
                PteraDirector director = (PteraDirector) ((PteraController) actor)
                        .getDirector();
                ending = director._ending;
            }
            if (!postfire || ending) {
                if (!postfire) {
                    _initializedRefinements.remove(actor);
                }
                Event event = (Event) ((RefinementActor) actor)
                        .getRefinedState();
                if (event != null) {
                    PteraController controller = getController();
                    controller.event(new EventDebugEvent(controller, event,
                            true));

                    if (!postfire && event.isFinalEvent()) {
                        for (TimedEvent eventToCancel : _eventQueue) {
                            eventToCancel.canceled = true;
                        }
                    } else {
                        event.scheduleEvents();

                        if (!_ending && event.isEndingEvent()) {
                            _ending = true;
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
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
            return ((PteraController) getContainer()).hasInput();
        } else {
            NamedObj container = getContainer();
            List<?> inputPorts = ((Actor) container).inputPortList();
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

    /** Initialize a refinement, usually invoked when the event that the
     *  refinement is associated to is processed, and fire the refinement if it
     *  does not produce a fireAt() request during initialization, such as a
     *  dataflow refinement.
     *
     *  @param refinement The refinement to be initialized and fired.
     *  @param reset Whether the scheduling relation has the reset property.
     *  @exception IllegalActionException If thrown when the refinement is
     *   initialized or fired.
     */
    private synchronized void _initializeAndFireRefinement(
            TypedActor refinement, boolean reset) throws IllegalActionException {
        _fireAtReceived = false;
        if (reset || !_initializedRefinements.contains(refinement)) {
            refinement.initialize();
            _initializedRefinements.add(refinement);
        }
        if (!_fireAtReceived) {
            _fireActor(refinement, null);
        }
    }

    /** Return whether this director is in a Ptera controller.
     *
     *  @return True if this director is in a Ptera controller. False if this
     *   director is directly associated with a Ptera modal model.
     */
    private boolean _isInController() {
        return getContainer() instanceof PteraController;
    }

    /** Notify all the EventQueueDebugListeners in the list of DebugListeners of
     *  either an event/actor has been inserted into event queue, or it has been
     *  removed from the event queue.
     *
     *  @param isInsert Whether an event/actor has been inserted or removed.
     *  @param isCancelled If it is removed, whether it is because of canceling
     *   or it is because of event being processed. Not used if
     *   isInsert is true.
     *  @param position The position of the event/actor in the event queue.
     *  @param time The time at which the event/actor is scheduled. Null if
     *   isInserted is false.
     *  @param contents The event or actor. Null if isInserted is false.
     *  @param arguments Arguments to the event or actor, which must be either an
     *   ArrayToken or a RecordToken. Null if isInserted is false.
     */
    private void _notifyEventQueueDebugListeners(boolean isInsert,
            boolean isCancelled, int position, Time time, Object contents,
            Token arguments) {
        List<DebugListener> listeners = _debugListeners;
        if (listeners == null) {
            return;
        }

        for (DebugListener listener : listeners) {
            if (listener instanceof EventQueueDebugListener) {
                EventQueueDebugListener eventListener = (EventQueueDebugListener) listener;
                if (isInsert) {
                    if (contents instanceof Event) {
                        eventListener.insertEvent(position, time,
                                (Event) contents, arguments);
                    } else if (contents instanceof Actor) {
                        eventListener.insertActor(position, time,
                                (Actor) contents, arguments);
                    }
                } else {
                    eventListener.removeEvent(position, isCancelled);
                }
            }
        }
    }

    /** Request to fire this director by invoking the fireAt() method of the
     *  executive director of the container.
     *
     *  @exception IllegalActionException If the invoked fireAt() method throws
     *   it, or if the executive director
     *   does not support fireAt() precisely (it does not agree to refire
     *   this Ptera model at the requested time).
     */
    private void _requestFiring() throws IllegalActionException {
        if (!_eventQueue.isEmpty()) {
            Time time = _eventQueue.get(0).timeStamp;
            fireContainerAt(time);
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
    private PteraController _controller = null;

    /** Version of cached reference to mode controller. */
    private long _controllerVersion = -1;

    /** Whether fireAt() invocations should be delegated to the director at the
        higher level. */
    private boolean _delegateFireAt = false;

    /** Whether an ending event is processed in the current firing.
     *  @see ptolemy.domains.ptera.kernel.Event#isEndingEvent()
     */
    private boolean _ending;

    /** The event queue. */
    private List<TimedEvent> _eventQueue = new LinkedList<TimedEvent>();

    /** A table that maps any port to the set of events that are listening to it
        during an execution. */
    private Map<Port, Set<TimedEvent>> _eventsListeningToPorts = new HashMap<Port, Set<TimedEvent>>();

    /** A table that maps any variable to the set of events that are listening
        to it during an execution. */
    private Map<Variable, Set<TimedEvent>> _eventsListeningToVariables = new HashMap<Variable, Set<TimedEvent>>();

    /** Whether fireAt() has been received from a refinement when that
     *  refinement is initialized.
     */
    private boolean _fireAtReceived;

    /** The set of refinements that have been initialized and have not returned
     *  false in their directors' postfire(). */
    private Set<TypedActor> _initializedRefinements = new HashSet<TypedActor>();

    /** The real time at which the execution started. */
    private long _realStartTime;

    /** The exception (if any) that occurred when the refinement was fired. */
    private IllegalActionException _refinementException = null;

}
