/*
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
package ptolemy.apps.ptides.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.RealDependency;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.apps.ptides.kernel.PtidesReceiver.Event;
import ptolemy.apps.ptides.lib.ScheduleListener.ScheduleEventType;
import ptolemy.apps.ptides.platform.NonPreemptivePlatformExecutionStrategy;
import ptolemy.apps.ptides.platform.PlatformExecutionStrategy;
import ptolemy.apps.ptides.platform.PreemptivePlatformExecutionStrategy;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.tdl.kernel.TDLCausalityInterface;
import ptolemy.domains.tdl.kernel.TDLModule;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * This director implements the Ptides (= Programming Temporally Integrated
 * Distributed Systems) model of computation. It executes actors according to
 * event time stamps. The difference to the DE director is that events can be
 * executed out of time stamped order.
 * <p>
 * Composite actors using this director can contain sensors, actuators,
 * computation actors with worst case execution times and model time delay
 * actors. The execution of these actors is governed by this director. Some of
 * these actors require a that the model time when an actor is fired is
 * equal to the physical time. The model time is defined by the event time stamp
 * that is responsible for firing the actor, the physical time is defined by the model time of the
 * executive director. Actors that require this mapping of model time to real time are sensors and
 * actuators. This mapping is also required to simulate the execution of actors
 * with a worst case execution time > 0.
 * <p>
 * Executing actors in platforms is done according to their model time stamps.
 * The execution does not have to be in time stamp order, the only
 * requirement is that events on ports in the same equivalence class
 * (@see CausalityInterface.equivalentPorts()) are processed in
 * time stamped order. To satisfy this requirement, an analysis on events is
 * used to determine whether they are safe to process. For this analysis, the
 * causality interface and the RealDependency - link - between ports are used.
 * <p>
 * This director executes actors in an infinite loop. At every iteration in the
 * loop, a set of all events is selected that are safe to process. Out of this
 * set of events, one event is selected to be processed. This selection is done
 * by the PlatformExecutionStrategy - link - which takes into account platform
 * characteristics like preemption and priorities. The firing of an actor is
 * divided into two steps, a start and a terminate. At the start of a firing, an
 * actor is put into a list of actors currently executing. If the actor has a
 * worst case execution time > 0, the platform schedules a refiring for the
 * current physical time + WCET by calling the fireAt() of the enclosing
 * director. When the WCET passed, the actor is taken out of the list of actors
 * currently in execution in the second step of the firing. If another actor
 * should be fired in the meantime, it preempts the currently executing actor
 * thus is also added to the list of actors in execution and the finishing times
 * of those actors are adjusted. When using a platform
 * execution strategy that does not support preemption, this list will have at
 * most one entry. The actual firing of an actor is done either at the start or
 * the termination time. This depends on whether the WCET is a static property
 * of the actor or it is only known after firing the actor. E.g. actors that
 * require simulation to determine the WCET such as TDLModules have to be fired
 * at the beginning of the execution.
 *
 * TODO: currently, this director only supports fixed priorities; e.g. actor a
 * preempts b and then b preempts a would not work.
 * <p>
 * Events are not maintained by a single event queue. There are two types of
 * events used in this domain: regular events and pure events. A regular event
 * is a combination of time stamp and value sent from the output port of one
 * actor to the input port of another actor. A pure event is a time stamp
 * that schedules firing an actor, there is no connection to any value or
 * input port. Regular events
 * are stored in the receivers as pairs of timestamp and value. For the pure events
 * for every actor, a list is created that saves time stamps of pure events for
 * that actor.
 *
 * <p>
 * This director is used to simulate the execution of actors, thus actors might define
 * execution times. Some actors will define worst case execution times, for other actors
 * a real execution time might be determined by simulating the actor and thus deriving
 * the execution time.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtidesEmbeddedDirector extends Director implements TimedDirector {

    /**
     * Construct a director in the default workspace with an empty string as its
     * name. The director is added to the list of objects in the workspace.
     * Increment the version number of the workspace.
     *
     * @exception IllegalActionException
         *                                   If the director is not compatible with the specified
     *                                  container.
     * @exception NameDuplicationException
         *                                  If the container not a CompositeActor and the name
     *                                  collides with an entity in the container.
     */
    public PtidesEmbeddedDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
        _initialize();
    }

    /**
     * Construct a director in the workspace with an empty name. The director is
     * added to the list of objects in the workspace. Increment the version
     * number of the workspace.
     *
     * @param workspace
     *            The workspace of this object.
     * @exception IllegalActionException
     *                If the director is not compatible with the specified
     *                container.
     * @exception NameDuplicationException
     *                If the container not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public PtidesEmbeddedDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _initialize();
    }

    /**
     * Construct a director in the given container with the given name. The
     * container argument must not be null, or a NullPointerException will be
     * thrown. If the name argument is null, then the name is set to the empty
     * string. Increment the version number of the workspace.
     *
     * @param container
     *            Container of the director.
     * @param name
     *            Name of this director.
     * @exception IllegalActionException
     *                If the director is not compatible with the specified
     *                container.
     * @exception NameDuplicationException
     *                If the container not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public PtidesEmbeddedDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public parameter                       ////

    /**
     * The executionStrategy defines the order of execution of events. This
     * parameter defaults to a basic non preemptive execution strategy.
     */
    public StringParameter executionStrategy;

    ///////////////////////////////////////////////////////////////////
    ////                    public methods                         ////

    /**
     * Update the director parameters when attributes are changed.
     *
     * @param attribute
     *            The changed parameter.
     * @exception IllegalActionException
     *                If the parameter set is not valid. Not thrown in this
     *                class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == executionStrategy) {
            String strategy = ((StringToken) executionStrategy.getToken())
                    .stringValue();
            _chooseExecutionStrategy(strategy);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new PtidesEmbeddedDirector.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PtidesEmbeddedDirector newObject = (PtidesEmbeddedDirector) super
                .clone(workspace);
        newObject._eventQueues = new Hashtable<Actor, TreeSet<Time>>();
        try {
            // Don't call getContainer here because we might be cloning and the
            // container is not an Actor.
            if (_currentPhysicalTime != null) {
                newObject._currentPhysicalTime = new Time(newObject,
                        _currentPhysicalTime.getDoubleValue());
            }
        } catch (Exception ex) {
            // CloneNotSupportedException does not have a constructor
            // that takes a cause argument, so we use initCause
            CloneNotSupportedException throwable = new CloneNotSupportedException();
            throwable.initCause(ex);
            throw throwable;
        }

        String strategy = null;
        try {
            strategy = ((StringToken) executionStrategy.getToken())
                    .stringValue();
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Should not produce an exception,"
                            + " this is checked before.");
        }
        newObject._chooseExecutionStrategy(strategy);
        return newObject;
    }

    /**
     * Return the default dependency between input and output ports which for
     * the Ptides domain is a RealDependency.
     *
     * @return The default dependency which describes a delay of 0.0 between
     *         ports.
     */
    public Dependency defaultDependency() {
        return RealDependency.OTIMES_IDENTITY;
    }

    /**
     * Return a real dependency representing a model-time delay of the
     * specified amount.
     *
     * @param delay
     *            A non-negative delay.
     * @return A boolean dependency representing a delay.
     */
    public Dependency delayDependency(double delay) {
        return RealDependency.valueOf(delay);
    }

    /**
     * If the enclosing director is a PtidesDirector, send these events to the
     * enclosing director. The PtidesDirector collects all display events from
     * all platforms and sends them to schedule listeners. If the enclosing
     * director is not a PtidesDirector, do nothing.
     *
     * @param actor
     *            Actor for which the event occured.
     * @param time
     *            physical time at which the event occured.
     * @param scheduleEvent
     *            Type of event.
     */
    public final void displaySchedule(Actor actor, double time,
            ScheduleEventType scheduleEvent) {
        if (this.getContainer() != null
                && ((Actor) this.getContainer()).getExecutiveDirector() instanceof PtidesDirector) {
            PtidesDirector dir = (PtidesDirector) ((Actor) getContainer())
                    .getExecutiveDirector();
            if (dir != null) {
                dir._displaySchedule((Actor) getContainer(), actor, time,
                        scheduleEvent);
            }
        }
    }

    /** Return a causality interface for the composite actor that
     *  contains this director. This class returns an
     *  instance of {@link TDLCausalityInterface}.
     *  @return A representation of the dependencies between input ports
     *   and output ports of the container.
     */
    public CausalityInterface getCausalityInterface() {
        return new TDLCausalityInterface((Actor) getContainer(),
                defaultDependency());
    }

    /**
     * Get finishing time of actor in execution. The finishing time is the point
     * in time when the WCET of the actor has passed.
     * @see #setFinishingTime(Actor, Time)
     * @param actor
     *            The actor in execution.
     * @return The finishing time of the actor.
     */
    public Time getFinishingTime(Actor actor) {
        if (_finishingTimesOfActorsInExecution.get(actor) != null) {
            return _finishingTimesOfActorsInExecution.get(actor);
        }
        return Time.POSITIVE_INFINITY;
    }

    /**
     * Return the current model time. When an actor is in execution, this method
     * returns the model time of the event responsible for firing the actor
     * currently in execution. Otherwise, this method returns the time 0.0.
     *
     * @return The current model time.
     */
    public Time getModelTime() {
        if (_currentModelTime == null) {
            return new Time(this, 0);
        } else {
            return _currentModelTime;
        }
    }

    /**
     * This method fires all actors that are safe to fire at the current
     * physical time. In a loop, a set of events which are safe to process is
     * selected. Then, one event is chosen that will really be processed. This
     * choice is taken by the PlatformExecutionStrategy.
     * <p>
     * If there is no event selected, this director schedules a refiring for the
     * actor by calling the fireAt() method of the enclosing director. The
     * enclosing director will stall this platform until the model time of the
     * enclosing director which is used as the physical time is equal to the
     * time requested in the fireAt() or if an event was sent to the composite
     * actor governed by this director.
     * <p>
     * If an event was selected, the actor is added to a set of actors in
     * execution. If the actor has a worst case execution time > 0, this
     * director calls the fireAt() method of the enclosing director with the
     * current physical time increased by the WCET. After that time passed, the
     * actor is taken out of the list of actors in execution.
     *
     * @exception IllegalActionException
     *             Thrown if an execution was missed.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("--- fired: " + this.getContainer().getName() + " "
                    + _currentPhysicalTime);
        }
        List<TimedEvent> eventsToFire = null;
        TimedEvent event = null;
        boolean iterate = true;
        while (iterate) {

            if (_stopRequested) {
                return;
            }
            _transferAllInputs();
            // take events out of the list of events in execution if the WCET
            // of the actor has passed
            if (_eventsInExecution.size() > 0) {
                TimedEvent eventInExecution = _eventsInExecution.getFirst();
                Actor actorToFire = (Actor) eventInExecution.contents;
                Time time = getFinishingTime(actorToFire);
                if (time.equals(_currentPhysicalTime)) {
                    if (_debugging) {
                        _debug(" x " + _currentPhysicalTime + " "
                                + _currentTime + " " + actorToFire);
                    }
                    _eventsInExecution.removeFirst();
                    _currentModelTime = eventInExecution.timeStamp;
                    if (!_fireAtTheBeginningOfTheWcet(actorToFire)) {
                        _fireActorInZeroModelTime(actorToFire);
                    }

                    // TODO do not transfer all outputs but only necessary ones
                    _transferAllOutputs();
                    displaySchedule(actorToFire, _currentPhysicalTime
                            .getDoubleValue(), ScheduleEventType.STOP);
                    if (_eventsInExecution.size() > 0) {
                        displaySchedule(
                                (Actor) _eventsInExecution.getFirst().contents,
                                _currentPhysicalTime.getDoubleValue(),
                                ScheduleEventType.START);
                    }
                    if (_eventsInExecution.size() > 0) {
                        _currentModelTime = _eventsInExecution.getFirst().timeStamp;
                    } else {
                        _currentModelTime = null;
                    }
                }
            }

            // get the next event
            eventsToFire = _getNextEventsToFire();
            Time nextRealTimeEventTime = _getNextRealTimeEventTime(
                    eventsToFire, _eventsInExecution);
            event = _executionStrategy.getNextEventToFire(_eventsInExecution,
                    eventsToFire, nextRealTimeEventTime, _currentPhysicalTime);
            if (_debugging) {
                _debug(_currentPhysicalTime + " " + _currentTime + " " + event);
            }
            // start firing an actor defined by the previously selected event
            if (event != null) {
                _currentModelTime = event.timeStamp;
                Actor actorToFire = (Actor) event.contents;
                if (!actorToFire.prefire()) { // TODO what to do with those actors?
                    _currentModelTime = null;
                    continue;
                } else {
                    // remove events that caused the firing of that actor
                    TreeSet<Time> eventsForActorAndTime = _eventQueues
                            .get(actorToFire);
                    if (!eventsForActorAndTime.isEmpty()) {
                        Time time = eventsForActorAndTime.first();
                        if (time.equals(getModelTime())) {
                            eventsForActorAndTime.remove(time);
                        }
                    }
                    if (_eventsInExecution.size() > 0) {
                        displaySchedule(
                                (Actor) _eventsInExecution.getFirst().contents,
                                _currentPhysicalTime.getDoubleValue(),
                                ScheduleEventType.STOP);
                    }
                    displaySchedule(actorToFire, _currentPhysicalTime
                            .getDoubleValue(), ScheduleEventType.START);
                    if (_fireAtTheBeginningOfTheWcet(actorToFire)) {
                        _fireActorInZeroModelTime(actorToFire);
                    }
                    double WCET = PtidesActorProperties.getWCET(actorToFire);
                    setFinishingTime(actorToFire, _currentPhysicalTime
                            .add(WCET));
                    for (TimedEvent eventInExecution : _eventsInExecution) {
                        Actor actor = (Actor) eventInExecution.contents;
                        setFinishingTime(actor, getFinishingTime(actor).add(
                                WCET));
                    }
                    _eventsInExecution.addFirst(event);
                }
            } else {
                // if new inputs are read, continue firing, otherwise call
                // the fireAt() of the enclosing director
                if (_transferAllInputs()) {
                    continue;
                }
                // The following will throw an exception if the executive director does
                // not exactly respect the request to refire at the given time.
                _fireContainerAt(nextRealTimeEventTime);

                Director executiveDirector = ((Actor) getContainer())
                        .getExecutiveDirector();
                while (executiveDirector != null
                        && (executiveDirector.getContainer() != executiveDirector
                                .toplevel())) {
                    executiveDirector = ((Actor) executiveDirector
                            .getContainer()).getExecutiveDirector();
                }
                if (executiveDirector == null) {
                    throw new IllegalActionException(
                            "This director can only be used as an embedded director.");
                }
                _currentPhysicalTime = executiveDirector.getModelTime();
                iterate = false;
            }
        }
    }

    /**
     * Schedule an actor to be fired at the specified time by posting a pure
     * event to the director.
     *
     * @param actor
     *            The scheduled actor to fire.
     * @param time
     *            The scheduled time to fire.
     *  @return The time at which the actor passed as an argument
     *   will be fired.
     * @exception IllegalActionException
     *                If event queue is not ready.
     */
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        if (((CompositeActor) getContainer()).entityList().contains(actor)) {
            _enqueueEvent(actor, time);
        } else {
            Actor containingActor = (Actor) actor.getContainer();
            while (!((CompositeActor) getContainer()).entityList().contains(
                    actor)) {
                containingActor = (Actor) containingActor.getContainer();
            }
            _enqueueEvent(containingActor, time);
        }
        Director executiveDirector = ((Actor) actor.getContainer())
                .getExecutiveDirector();

        if (!(executiveDirector instanceof PtidesDirector)) {
            return executiveDirector.fireAt((Actor) actor.getContainer(), time);
        }
        return time;
    }

    /**
     * This method returns true if it is safe to read inputs on the ports of the
     * composite actor containing this director. Inputs are safe to process if
     * there cannot appear another event with an earlier time stamp on any port
     * in the same port group as the given port. This method implements a static
     * analysis which determines that events are safe to process if
     * eventTimestamp - minimumDelay + clockSyncError + networkDelay <=
     * physicalTime. The minimum delay is determined by the causality interface
     * This method might return false but an event is still safe to process
     * because there are events upstream which will certainly arrive at this
     * port and all other ports in the same port group with a greater time
     * stamp.
     *
     * @param time
     *            Time stamp of the event.
     * @param port
     *            Port where inputs should be read.
     * @return True if tokens can be transferred.
     * @exception IllegalActionException
     *                Thrown if minimum delay cannot be computed.
     */
    public boolean isSafeToProcessStaticallyOnNetwork(Time time, IOPort port)
            throws IllegalActionException {
        TDLCausalityInterface causalityInterface = (TDLCausalityInterface) ((CompositeActor) ((CompositeActor) this
                .getContainer()).getContainer()).getCausalityInterface();
        RealDependency minimumDelay = (RealDependency) causalityInterface
                .getMinimumDelay(port);
        return minimumDelay.value() == Double.MAX_VALUE
                || time.subtract(minimumDelay.value()).add(
                        _clockSyncronizationError).add(_networkDelay)
                        .compareTo(_currentPhysicalTime) <= 0;
    }

    /**
     * Create a new PtidesActorReceiver.
     */
    public Receiver newReceiver() {
        return new PtidesActorReceiver();
    }

    /**
     * Set the expected finishing time of an actor. This method is called after
     * the execution of an actor is started. It could be greater if preempted.
     * @see #getFinishingTime(Actor)
     * @param actor
     *            The actor in execution.
     * @param finishingTime
     *            The time the actor is expected finish.
     */
    public void setFinishingTime(Actor actor, Time finishingTime) {
        if (_finishingTimesOfActorsInExecution.get(actor) != null) {
            _finishingTimesOfActorsInExecution.remove(actor);
        }
        _finishingTimesOfActorsInExecution.put(actor, finishingTime);
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        Director executiveDirector = ((Actor) getContainer())
                .getExecutiveDirector();
        while (executiveDirector != null
                && (executiveDirector.getContainer() != executiveDirector
                        .toplevel())) {
            executiveDirector = ((Actor) executiveDirector.getContainer())
                    .getExecutiveDirector();
        }
        _currentPhysicalTime = executiveDirector.getModelTime();

        executiveDirector = ((Actor) getContainer()).getExecutiveDirector();
        _transferAllInputs();
        if (!(executiveDirector instanceof PtidesDirector)) {
            if (_eventsInExecution.size() > 0) {
                TimedEvent eventInExecution = _eventsInExecution.getFirst();
                Actor actorToFire = (Actor) eventInExecution.contents;
                Time time = getFinishingTime(actorToFire);
                if (time.equals(_currentPhysicalTime)) {
                    return true;
                }
            }
            List eventsToFire = _getNextEventsToFire();
            Time nextRealTimeEventTime = _getNextRealTimeEventTime(
                    eventsToFire, _eventsInExecution);
            TimedEvent event = _executionStrategy.getNextEventToFire(
                    _eventsInExecution, eventsToFire, nextRealTimeEventTime,
                    _currentPhysicalTime);
            // start firing an actor defined by the previously selected event
            if (event != null) {
                return true;
            }
            if (!nextRealTimeEventTime.equals(Time.POSITIVE_INFINITY)) {
                // The following will throw an exception if the executive director
                // does not exactly respect the fireAt() request.
                _fireContainerAt(nextRealTimeEventTime);
            }
            return false;
        } else {
            return super.prefire();
        }
    }

    /**
     * Initialize variables.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _eventQueues = new Hashtable<Actor, TreeSet<Time>>();
        List<ComponentEntity> platforms = ((CompositeActor) getContainer())
                .entityList();
        for (ComponentEntity platform : platforms) {
            if (platform instanceof Actor) {
                _eventQueues.put((Actor) platform, new TreeSet<Time>());
            }
        }
        _currentPhysicalTime = ((Actor) getContainer()).getExecutiveDirector()
                .getModelTime();
    }

    /**
     * Transfer outputs and return true if any tokens were transfered.
     *
     * @exception IllegalActionException
     *                If the port is not an opaque output port.
     * @param port
     *            The port to transfer tokens from.
     * @return True if data are transferred.
     * @exception IllegalActionException
     *             Thrown if output cannot be transfered.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        boolean anyWereTransferred = false;
        boolean moreTransfersRemaining = true;

        while (moreTransfersRemaining) {
            moreTransfersRemaining = _transferOutputs(port);
            anyWereTransferred |= moreTransfersRemaining;
        }
        return anyWereTransferred;
    }

    /**
     * Invoke the wrapup method of the super class. Reset the private state
     * variables.
     *
     * @exception IllegalActionException
     *                If the wrapup() method of one of the associated actors
     *                throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _eventQueues.clear();
        _currentModelTime = new Time(this, 0.0);
        _inputSafeToProcess.clear();
        _finishingTimesOfActorsInExecution.clear();
        _eventsInExecution.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected variables                    ////

    /**
     * Clock synchronization error specified in the top level director.
     */
    protected double _clockSyncronizationError;

    /**
     * Current physical time which is retrieved by getting the model time of the
     * enclosing director.
     */
    protected Time _currentPhysicalTime;

    /**
     * Network delay time specified in the top-level director.
     */
    protected double _networkDelay;

    ///////////////////////////////////////////////////////////////////
    ////                    protected methods                      ////

    /**
     * Put a pure event into the event queue for the given actor to fire at the
     * specified timestamp.
     *
     * @param actor
     *            The actor to be fired.
     * @param time
     *            The timestamp of the event.
     */
    protected synchronized void _enqueueEvent(Actor actor, Time time) {
        _eventQueues.get(actor).add(time);
    }

    /**
     * Transfer input ports if they are safe to process. If the event is not
     * safe to process, put the event into a list of inputs with the time stamp
     * when this event will become safe to process. This list is used in the
     * fire method when no event is selected to process and this director
     * schedules a refiring at a future time.
     *
     * @param port
     *            Transfer input on this port.
     * @return True if inputs were transferred.
     * @exception IllegalActionException
     *             Thrown if inputs could not be transferred.
     */
    protected boolean _transferInputs(IOPort port)
            throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque"
                            + "input port.");
        }
        boolean wasTransferred = false;

        for (int i = 0; i < port.getWidth(); i++) {
            boolean nextTokenNotTested = true;
            while (port.hasToken(i) && nextTokenNotTested) {
                Receiver[][] receivers = port.getReceivers();

                for (int k = 0; k < receivers.length; k++) {
                    for (int l = 0; l < receivers[k].length; l++) {
                        Token t = null;
                        Time time = null;
                        if (receivers[k][l] instanceof PtidesPlatformReceiver) {
                            PtidesPlatformReceiver receiver = (PtidesPlatformReceiver) receivers[k][l];
                            Event event = receiver.getEvent();
                            time = event._timeStamp;
                            t = event._token;
                        } else {
                            time = ((Actor) ((Actor) getContainer())
                                    .getContainer()).getDirector()
                                    .getModelTime();
                            t = receivers[k][l].get();
                        }
                        if (time.compareTo(_currentPhysicalTime) < 0) {
                            throw new IllegalActionException(
                                    "Network interface constraints violated at "
                                            + this.getContainer().getName()
                                            + ", tried to transfer event with timestamp "
                                            + time + " at physical time "
                                            + _currentPhysicalTime);
                        }
                        // TODO implement the dynamic analysis too
                        if (!((isSafeToProcessStaticallyOnNetwork(time, port)) || (time
                                .compareTo(_currentPhysicalTime) > 0))) {
                            CompositeActor platform = ((CompositeActor) this
                                    .getContainer());
                            CompositeActor model = ((CompositeActor) platform
                                    .getContainer());
                            TDLCausalityInterface causalityInterface = (TDLCausalityInterface) model
                                    .getCausalityInterface();
                            RealDependency minimumDelay = (RealDependency) causalityInterface
                                    .getMinimumDelay(port);
                            _inputSafeToProcess.put(new TimedEvent(time
                                    .subtract(minimumDelay.value()).add(
                                            _clockSyncronizationError).add(
                                            _networkDelay), port));
                            //receiver.put(t, time);
                            nextTokenNotTested = false;
                            continue;
                        }
                        Receiver[][] farReceivers = port.deepGetReceivers();
                        if ((farReceivers == null)
                                || (farReceivers.length <= i)
                                || (farReceivers[i] == null)) {
                            nextTokenNotTested = false;
                            continue;
                        }
                        for (int m = 0; m < farReceivers.length; m++) {
                            for (int n = 0; n < farReceivers[m].length; n++) {
                                PtidesActorReceiver farReceiver = (PtidesActorReceiver) farReceivers[m][n];
                                farReceiver.put(t, time);
                                displaySchedule((Actor) port.getContainer(),
                                        _currentPhysicalTime.getDoubleValue(),
                                        ScheduleEventType.TRANSFERINPUT);
                                wasTransferred = true;
                            }
                        }
                    }
                }
            }
        }
        return wasTransferred;
    }

    /**
     * Transfer output ports and return true if outputs were transferred.
     *
     * @exception IllegalActionException
     *             Attempted to transferOutputs on a port that is not an opaque
     *             input port.
     */
    protected boolean _transferOutputs(IOPort port)
            throws IllegalActionException {
        Token token;
        boolean result = false;

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque input port.");
        }
        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                if (port.hasTokenInside(i)) {
                    token = port.getInside(i);
                    Receiver[][] outReceivers = port.getRemoteReceivers();
                    for (int k = 0; k < outReceivers.length; k++) {
                        for (int l = 0; l < outReceivers[k].length; l++) {
                            port.send(l, token);
                            //                            PtidesPlatformReceiver outReceiver = (PtidesPlatformReceiver) outReceivers[k][l];
                            //                            outReceiver.put(token,
                            //                                    ((Actor) port.getContainer()).getDirector()
                            //                                            .getModelTime());
                            displaySchedule((Actor) port.getContainer(),
                                    _currentPhysicalTime.getDoubleValue(),
                                    ScheduleEventType.TRANSFEROUTPUT);
                        }
                    }
                    result = true;
                }
            } catch (NoTokenException ex) {
                throw new InternalErrorException(this, ex, null);
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                    protected methods                      ////

    /**
     * Choose the ExecutionStrategy object according to the selected string
     * parameter.
     *
     * @param executionStrategy
     *            Given string parameter.
     */
    private void _chooseExecutionStrategy(String executionStrategy) {

        if (executionStrategy
                .equals(PlatformExecutionStrategy.BASIC_NON_PREEMPTIVE)) {
            _executionStrategy = new NonPreemptivePlatformExecutionStrategy(
                    this);
        } else if (executionStrategy
                .equals(PlatformExecutionStrategy.BASIC_PREEMPTIVE)) {
            _executionStrategy = new PreemptivePlatformExecutionStrategy(this);
        }
    }

    /**
     * This method fires an actor. The actual firing of an actor takes zero
     * model time, the WCET is simulated by either firing an actor at the
     * beginning or the end of the WCET.
     *
     * @param actorToFire
     *            Actor that has to be fired.
     * @exception IllegalActionException
     *             Thrown if actor cannot be fired.
     */
    private void _fireActorInZeroModelTime(Actor actorToFire)
            throws IllegalActionException {
        if (actorToFire instanceof CompositeActor) {
            actorToFire.getDirector().setModelTime(getModelTime());
        }
        actorToFire.fire();
        actorToFire.postfire();
    }

    /**
     * Determines if the actor has to be fired at the beginning of the WCET. An
     * actor must be fired at the beginning of the WCET if the firing is
     * required to determine the WCET. This is the case for actors that do not
     * have a static WCET but the WCET is defined for contained actors. An
     * example for such an actor is a TDLModule.
     *
     * @param actor
     *            Actor that has to be fired.
     * @return True if actor has to be fired at the beginning of the WCET.
     */
    private boolean _fireAtTheBeginningOfTheWcet(Actor actor) {
        if (actor instanceof TDLModule) {
            return true;
        }
        return false;
    }

    /**
     * Get the list of events that are safe to fire. Those events contain pure
     * events and triggered events.
     *
     * @return List of events that can be fired next.
     */
    private List<TimedEvent> _getNextEventsToFire()
            throws IllegalActionException {
        List<TimedEvent> events = new LinkedList<TimedEvent>();
        for (Actor actor : _eventQueues.keySet()) {
            TreeSet<Time> set = _eventQueues.get(actor);
            // don't take actor if it is already in execution
            for (TimedEvent event : _eventsInExecution) {
                if (event.contents == actor) {
                    continue;
                }
            }

            // take pure events
            if (!set.isEmpty()) {
                Time time = set.first();
                _currentModelTime = time;
                if (actor.prefire()) {
                    events.add(new TimedEvent(time, actor));
                }
                _currentModelTime = null;
            }
            // take trigger events
            List<IOPort> inputPorts = actor.inputPortList();
            for (IOPort port : inputPorts) {

                if (PtidesActorProperties.portIsTriggerPort(port)) {
                    Receiver[][] receivers = port.getReceivers();
                    for (int i = 0; i < receivers.length; i++) {
                        Receiver[] recv = receivers[i];
                        for (int j = 0; j < recv.length; j++) {
                            PtidesActorReceiver receiver = (PtidesActorReceiver) recv[j];
                            Time time = receiver.getNextTime();
                            if (_debugging) {
                                _debug(((Actor) getContainer()).getDirector()
                                        .getModelTime()
                                        + " "
                                        + getModelTime()
                                        + " "
                                        + time
                                        + " " + actor);
                            }
                            if (time != null
                                    && (time.compareTo(((Actor) getContainer())
                                            .getDirector().getModelTime()) <= 0 || _allUpstreamEventsHaveHigherTimestamps(
                                            port, port, new ArrayList(),
                                            new Time(this, 0.0), time))) {
                                if (_debugging) {
                                    _debug(actor.getFullName());
                                }
                                List<TimedEvent> toRemove = new ArrayList<TimedEvent>();
                                for (int k = 0; k < events.size(); k++) {
                                    TimedEvent event = events.get(k);
                                    if (event.contents == actor
                                            && event.timeStamp.equals(time)) {
                                        toRemove.add(event);
                                    }
                                }
                                for (int k = 0; k < toRemove.size(); k++) {
                                    events.remove(toRemove.get(k));
                                }
                                events.add(new TimedEvent(time, port));
                            }
                        }
                    }
                }
            }

        }
        return events;
    }

    /**
     * Return time stamp of next event on that port or null if that port has no
     * events.
     *
     * @param port
     *            Port for which time stamp of next event is requested.
     * @return Time stamp of next event or null if there are no events.
     */
    private Time _getNextEventTimeStamp(IOPort port) {
        Time time = Time.POSITIVE_INFINITY;
        Receiver[][] receivers = port.getReceivers();
        for (int i = 0; i < receivers.length; i++) {
            Receiver[] recv = receivers[i];
            for (int j = 0; j < recv.length; j++) {
                if (recv[j] instanceof PtidesReceiver) {
                    PtidesReceiver receiver = (PtidesReceiver) recv[j];
                    if (receiver.getNextTime() != null
                            && time.compareTo(receiver.getNextTime()) > 0) {
                        time = receiver.getNextTime();
                    }
                }
            }
        }
        return time;
    }

    /**
     * Return time stamp of next real time event. A real time event is either an
     * event for a sensor or an actuator, the end of the WCET of an actor in
     * execution or the time stamp when an input event to the composite actor
     * which uses this director that becomes safe to process.
     *
     * @param eventsToFire
     *            Set of events that are safe to process.
     * @param eventsInExecution
     *            Set of events currently in execution
     * @return Time of next real time event.
     */
    private Time _getNextRealTimeEventTime(List<TimedEvent> eventsToFire,
            Queue<TimedEvent> eventsInExecution) {
        Time nextRealTimeEvent = Time.POSITIVE_INFINITY;
        for (TimedEvent event : eventsToFire) {
            if (PtidesActorProperties.mustBeFiredAtRealTime(event.contents)) {
                if (nextRealTimeEvent != null
                        && event.timeStamp.compareTo(nextRealTimeEvent) < 0) {
                    nextRealTimeEvent = event.timeStamp;
                }
            }
        }
        if (eventsInExecution.size() > 0) {
            Time time = getFinishingTime((Actor) eventsInExecution.peek().contents);
            if (nextRealTimeEvent != null
                    && time.compareTo(nextRealTimeEvent) < 0) {
                nextRealTimeEvent = time;
            }
        }
        if (_inputSafeToProcess.size() > 0) {
            TimedEvent event = (TimedEvent) _inputSafeToProcess.get();
            if (event.timeStamp.compareTo(nextRealTimeEvent) < 0) {
                nextRealTimeEvent = event.timeStamp;
                _inputSafeToProcess.take();
            }
        }
        return nextRealTimeEvent;
    }

    /**
     * Initialize the execution strategy parameter.
     */
    private void _initialize() {
        try {
            executionStrategy = new StringParameter(this, "executionStrategy");
            executionStrategy
                    .addChoice(PlatformExecutionStrategy.BASIC_NON_PREEMPTIVE);
            executionStrategy
                    .setExpression(PlatformExecutionStrategy.BASIC_NON_PREEMPTIVE);
            executionStrategy
                    .addChoice(PlatformExecutionStrategy.BASIC_PREEMPTIVE);
        } catch (KernelException e) {
            throw new InternalErrorException("Cannot set parameter:\n"
                    + e.getMessage());
        }
    }

    /**
     * This recursive method returns true if an event exists on the given port
     * that has a time stamp greater than the eventTimeStamp - delay. This
     * method is used to dynamically determine if an event is safe to process.
     *
     * @param currentPort
     *            Port that might have an event with time stamp + delay >
     *            eventTimeStamp.
     * @param visitedPorts
     *            Already visited ports on this analysis. This method is
     *            recursive and the visitedPorts are used to detect loops.
     * @param delay
     *            The delay between the port with the event that is checked for
     *            being safe to process and the current port.
     * @param eventTimeStamp
     *            Time stamp of the event which is checked.
     * @return True if the given port has an event with the time stamp + delay >
     *         eventTimeStamp.
     * @exception IllegalActionException
     */
    private boolean _allUpstreamEventsHaveHigherTimestamps(IOPort port,
            IOPort currentPort, Collection<IOPort> visitedPorts, Time delay,
            Time eventTimeStamp) throws IllegalActionException {
        if (visitedPorts == null) {
            visitedPorts = new ArrayList<IOPort>();
        }
        visitedPorts.add(currentPort);

        CompositeActor cplatform = (CompositeActor) this.getContainer();
        Actor currentActor = (Actor) currentPort.getContainer();
        Time time = _getNextEventTimeStamp(currentPort);

        // min delay between sensor and port
        if (PtidesActorProperties.isSensor(currentActor)) {
            TDLCausalityInterface causalityInterface = (TDLCausalityInterface) cplatform
                    .getCausalityInterface();
            RealDependency minimumDelay = (RealDependency) causalityInterface
                    .getMinimumDelay(port);
            return minimumDelay.value() == Double.MAX_VALUE
                    || time.subtract(minimumDelay.value()).compareTo(
                            _currentPhysicalTime) <= 0;
        }

        if (currentPort.isInput()) {

            if (port != currentPort && time != null
                    && !time.equals(Time.POSITIVE_INFINITY)) {
                if (time.add(delay).compareTo(eventTimeStamp) > 0) { // current port has an event with a time stamp that will occur at port at a later time than eventtimestamp
                    return true;
                }
            }
            Collection<IOPort> equivalentPorts = (currentActor
                    .getCausalityInterface()).equivalentPorts(currentPort);
            boolean allAreSafeToProcess = true;
            for (IOPort equivalentPort : equivalentPorts) {
                Time eqtime = _getNextEventTimeStamp(equivalentPort);
                if (eqtime == Time.POSITIVE_INFINITY
                        || (equivalentPort == port && eqtime.add(delay)
                                .compareTo(eventTimeStamp) < 0)
                        || (equivalentPort != port && eqtime.add(delay)
                                .compareTo(eventTimeStamp) <= 0)) { // search upstream
                    Collection<IOPort> sourcePorts = equivalentPort
                            .sourcePortList();
                    if (sourcePorts.size() == 0) {
                        return false;
                    }
                    for (IOPort sourcePort : sourcePorts) {
                        allAreSafeToProcess = allAreSafeToProcess
                                & _allUpstreamEventsHaveHigherTimestamps(port,
                                        sourcePort, visitedPorts, delay,
                                        eventTimeStamp);
                    }
                }
            }
            return allAreSafeToProcess;
        } else if (currentPort.isOutput()) {
            if (currentActor instanceof CompositeActor) {
                CausalityInterface causalityInterface = currentActor
                        .getCausalityInterface();
                Collection<IOPort> deepInputPorts = currentPort
                        .deepInsidePortList();
                for (IOPort inputPort : deepInputPorts) {
                    delay = delay.add(((RealDependency) causalityInterface
                            .getDependency(inputPort, currentPort)).value());
                    return _allUpstreamEventsHaveHigherTimestamps(port,
                            inputPort, visitedPorts, delay, eventTimeStamp);
                }
            } else {
                CausalityInterface causalityInterface = currentActor
                        .getCausalityInterface();
                Collection<IOPort> inputPorts = causalityInterface
                        .dependentPorts(currentPort);
                for (IOPort inputPort : inputPorts) {
                    delay = delay.add(((RealDependency) causalityInterface
                            .getDependency(inputPort, currentPort)).value());
                    return _allUpstreamEventsHaveHigherTimestamps(port,
                            inputPort, visitedPorts, delay, eventTimeStamp);
                }
                if (inputPorts.size() == 0) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Transfer all input tokens and return true if inputs were transfered.
     *
     * @return True if input ports were transferred.
     * @exception IllegalActionException
     *             If reading from the associated port throws it or inputs could
     *             not be transferred.
     */
    private boolean _transferAllInputs() throws IllegalActionException {
        boolean inputTransferred = false;
        List<IOPort> inputPorts = ((Actor) getContainer()).inputPortList();
        for (IOPort port : inputPorts) {
            if (port instanceof ParameterPort) {
                ((ParameterPort) port).getParameter().update();
            } else {
                inputTransferred = transferInputs(port);
            }
        }
        return inputTransferred;
    }

    /**
     * Transfer all output tokens and return true if outputs were transfered.
     *
     * @return True if output tokens were transfered.
     * @exception IllegalActionException
     *             Attempted to transferOutputs on a port that is not an opaque
     *             input port.
     */
    private boolean _transferAllOutputs() throws IllegalActionException {
        boolean transferedOutputs = false;
        List<IOPort> outputPorts = ((Actor) getContainer()).outputPortList();
        for (IOPort port : outputPorts) {
            transferedOutputs = transferedOutputs | _transferOutputs(port);
        }
        return transferedOutputs;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    /**
     * The current model time is adjusted when an actor is fired and set to the
     * event time stamp of the event causing the firing.
     */
    private Time _currentModelTime;

    /**
     * Contains an event queue for every actor.
     */
    private Hashtable<Actor, TreeSet<Time>> _eventQueues;

    /**
     * List of actors in execution. In a non-preemptive execution, the list only
     * contains one item.
     */
    private LinkedList<TimedEvent> _eventsInExecution = new LinkedList<TimedEvent>();

    /**
     * Used execution strategy which is set according to a parameter.
     */
    private PlatformExecutionStrategy _executionStrategy;

    /**
     * Events on input ports of the composite actor directed by this director
     * with the time stamp set to the physical time when they are safe to
     * process.
     */
    private CalendarQueue _inputSafeToProcess = new CalendarQueue(
            new TimedEvent.TimeComparator());

    /**
     * Contains finishing times of actors in execution.
     */
    private Hashtable<Actor, Time> _finishingTimesOfActorsInExecution = new Hashtable();

}
