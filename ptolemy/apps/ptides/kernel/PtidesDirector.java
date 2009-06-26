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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.RealDependency;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.apps.ptides.lib.ScheduleListener;
import ptolemy.apps.ptides.lib.ScheduleListener.ScheduleEventType;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.pn.kernel.TimedPNDirector;
import ptolemy.domains.tdl.kernel.TDLCausalityInterface;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * Top-level director for Ptides (= Programming Temporally Integrated
 * Distributed Systems) models. A Ptides model consists of platforms represented
 * by CompositeActors that communicate via events. Those platforms run in
 * threads. This director is a timed director, the time represents the global
 * <i>physical time</i>. This physical time is used in all platforms, for
 * sending events between platforms, a global bounded clock synchronization
 * error and a global bounded network delay is considered.
 *
 * <p>
 * Platforms contain sensors, actuators, computation actors with worst case
 * execution times and model time delay actors. The execution of actors inside a
 * platform is governed by a director that executes events according to their
 * <i>model time</i> time stamps. This director can be either a DEDirector or
 * the PtidesEmbeddedDirector. The PtidesEmbeddedDirector is a smarter
 * DEDirector in the sense that it allows an out of time stamp order execution
 * of events. ??? Some actors inside a platform require a mapping of the model
 * time defined in event time stamps to physical time. Those actors are sensors
 * and actuators. Also, the execution of actors with a worst case execution time
 * > 0 is simulated which requires the simulation of physical time passing
 * between the start and the termination of an actor.
 *
 * <p>
 * A platform executes events as long as there are events that are safe to
 * process and schedulable to process. Safe to process means that there is no
 * chance of an event arriving at the same port or port group with an earlier
 * time stamp. Whether an event is schedulable is determined by the platform
 * characteristics. For example, if the execution of an event would take longer
 * than the time until the next execution of an actuator and the platform cannot
 * preempt actor executions, this event cannot be executed.
 *
 * <p>
 * When a platform has no more events safe and possible to process at the
 * current physical time, it schedules a refiring at a future physical time.
 * This platform calls the fireAt() method of this director which blocks the
 * platform. A platform is resumed when:
 * <ul>
 * <li>The physical time has been increased to the new physical time at which
 * the platform wanted to be refired or</li>
 * <li>Another platform sent an event to the platform. This event might be safe
 * to process, it is up to the platform to find that out.</li>
 * </ul>
 *
 * <p>
 * A Ptides model never terminates thus a stop time must be specified.
 *
 * <p>
 * The difference between the PtidesDirector and a DE director with threads is
 * that the PtidesDirector does not call the fire of the contained actors, the
 * contained actors fire in an infinite loop. If there is nothing to fire, the
 * actor waits until the physical time of the PtidesDirector is increased.
 *
 * <p>
 * The difference between the PtidesDirector and a PN, TimedPN or DDE director
 * is that there are no blocking writes or reads between actors. The only way an
 * actor can be blocked is because it waits for physical time to pass.
 *
 * <p>
 * Usage: Every actor in a model controlled by the PtidesDirector must be a
 * CompositeActor containing a director that executes actors according to event
 * time stamps.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtidesDirector extends TimedPNDirector {

    /**
     * Construct a director in the default workspace with an empty string as its
     * name. The director is added to the list of objects in the workspace.
     * Increment the version number of the workspace.
     *
     * @exception IllegalActionException
     *                If the name contains a period, or if the director is not
     *                compatible with the specified container.
     * @exception NameDuplicationException
     *                If the container not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public PtidesDirector() throws IllegalActionException,
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
     *                If the name contains a period, or if the director is not
     *                compatible with the specified container.
     * @exception NameDuplicationException
     *                If the container not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public PtidesDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);

        _initialize();
    }

    /**
     * Construct a director in the given container with the given name. If the
     * container argument must not be null, or a NullPointerException will be
     * thrown. If the name argument is null, then the name is set to the empty
     * string. Increment the version number of the workspace.
     *
     * @param container
     *            The container.
     * @param name
     *            Name of this director.
     * @exception IllegalActionException
     *                If the name contains a period, or if the director is not
     *                compatible with the specified container.
     * @exception NameDuplicationException
     *                If the container not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public PtidesDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _initialize();
    }

    // /////////////////////////////////////////////////////////////////
    // // public parameters ////

    /**
     * The upper bound on the clock synchonization error for all platforms. This
     * parameter must contain a DoubleToken. The value defaults to 0.1.
     */
    public Parameter clockSyncError;

    /**
     * The upper bound on the network delay for sending events between
     * platforms. This parameter must contain a DoubleToken. The value defaults
     * to 0.1.
     */
    public Parameter networkDelay;

    /**
     * The stop time of the model. This parameter must contain a DoubleToken.
     * The value defaults to Infinity.
     */
    public Parameter stopTime;

    /** Specify whether the execution should synchronize to the
     *  real time. This parameter must contain a BooleanToken.
     *  If this parameter is true, then do not process events until the
     *  elapsed real time matches the time stamp of the events.
     *  The value defaults to false.
     */
    public Parameter synchronizeToRealTime;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add a new schedule listener that will receive all schedule events created
     * by the platforms.
     *
     * @param listener
     *            The schedule listener to be added.
     */
    public void addScheduleListener(ScheduleListener listener) {
        _scheduleListeners.add(listener);
    }

    /**
     * Override the base class to update local variables.
     *
     * @param attribute
     *            Attribute that changed.
     * @exception IllegalActionException
     *                Thrown if parameter cannot be read.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == clockSyncError) {
            _clockSyncronizationError = ((DoubleToken) clockSyncError
                    .getToken()).doubleValue();
        } else if (attribute == networkDelay) {
            _networkDelay = ((DoubleToken) networkDelay.getToken())
                    .doubleValue();
        } else if (attribute == synchronizeToRealTime) {
            _synchronizeToRealTime = ((BooleanToken) synchronizeToRealTime
                    .getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Clone the director into the specified workspace. The new object is
     * <i>not</i> added to the directory of that workspace (It must be added by
     * the user if he wants it to be there). The result is a new director with
     * no container and no topology listeners. All variables are set to their
     * initial values.
     *
     * @param workspace
     *            The workspace for the cloned object.
     * @exception CloneNotSupportedException
     *                If one of the attributes cannot be cloned.
     * @return The new TimedPNDirector.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PtidesDirector newObject = (PtidesDirector) super.clone(workspace);
        _platformsToUnblock = new HashSet<Actor>();
        _scheduleListeners = new LinkedList<ScheduleListener>();
        return newObject;
    }

    // Don't use @Override because the nightly build can't handle it
    //@Override
    public Dependency defaultDependency() {
        return RealDependency.OTIMES_IDENTITY;
    }

    /**
     * Suspend the calling process that runs a platform until either time has
     * advanced to the physical time specified by the method argument or the
     * platform is scheduled for unblocking without increasing the time. This
     * can happen because an event is sent from another platform to this
     * platform. Add the actor corresponding to the calling process to the queue
     * of waiting processes which is sorted by the time specified by the method
     * argument. Increment the count of the actors blocked waiting on a future
     * physical time.
     *
     * @param actor
     *            Actor that schedules to be refired.
     * @param newFiringTime
     *            Future time actor wants to be refired at.
     * @exception IllegalActionException
     *                If the operation is not permissible (e.g. the given time
     *                is in the past).
     */
    public synchronized Time fireAt(Actor actor, Time newFiringTime)
            throws IllegalActionException {
        if (newFiringTime.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(this, "The process wants to "
                    + " get fired in the past! (current time: "
                    + getModelTime() + ", " + "requestedFiring: "
                    + newFiringTime + ")");
        }

        _eventQueue.put(new TimedEvent(newFiringTime, actor));
        _informOfDelayBlock();

        try {
            while (!_stopRequested
                    && getModelTime().compareTo(newFiringTime) < 0) {
                if (_platformsToUnblock.remove(actor)) {
                    _informOfDelayUnblock();
                    break;
                }
                workspace().wait(this);
            }
        } catch (InterruptedException e) {
            throw new IllegalActionException(this, e.getCause(), e.getMessage());
        }
        return newFiringTime;
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
     * Initialize parameters and the schedule listeners. Calculate minimum
     * delays for ports on platforms according to Ptides.
     *
     * @exception IllegalActionException
     *             Thrown if other actors than CompositeActors are used in this
     *             model or embedded directors of these CompositeActors are not
     *             TimedDirectors.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentTime = new Time(this, 0.0);
        _realStartTime = System.currentTimeMillis();
        _stopTime = new Time(this, ((DoubleToken) stopTime.getToken())
                .doubleValue());

        TDLCausalityInterface causalityInterface = (TDLCausalityInterface) ((CompositeActor) this
                .getContainer()).getCausalityInterface();

        // Iterate through all actors in the model and add them to a List. This
        // List is handed to the schedule listeners.
        Hashtable<Actor, List> table = new Hashtable<Actor, List>();

        List<Actor> actors = ((CompositeEntity) getContainer())
                .deepEntityList();
        for (Actor actor : actors) {
            if (actor instanceof CompositeActor) {
                CompositeActor compositeActor = (CompositeActor) actor;
                // better checking for director that can handle events
                // the following check cannot be done - modal models for instance
                // are not timed directors but the refinements have to be timed
                // directors
                //                if (!(compositeActor.getDirector() instanceof TimedDirector)) {
                //                    throw new IllegalActionException(
                //                            "Director of a CompositeActor in "
                //                                    + "the Ptides domain must be a TimedDirector");
                //                }
                if (compositeActor.getDirector() instanceof PtidesEmbeddedDirector) {
                    PtidesEmbeddedDirector director = (PtidesEmbeddedDirector) actor
                            .getDirector();
                    director._clockSyncronizationError = _clockSyncronizationError;
                    director._networkDelay = _networkDelay;

                    List<IOPort> inputPorts = compositeActor.inputPortList();
                    for (IOPort port : inputPorts) {
                        if (_debugging) {
                            _debug("minDelay "
                                    + port
                                    + ": "
                                    + ((RealDependency) causalityInterface
                                            .getMinimumDelay(port)).value());
                        }
                    }

                }
                List<Actor> containedActors = compositeActor.entityList();
                table.put(actor, containedActors);
            }
            //            else {
            //                throw new IllegalActionException(
            //                        "Only composite actors are allowed to "
            //                                + "be used here");
            //            }
        }

        if (_scheduleListeners != null) {
            Iterator listeners = _scheduleListeners.iterator();

            while (listeners.hasNext()) {
                ((ScheduleListener) listeners.next()).initialize(table);
            }
        }
    }

    /**
     * Create a new PtidesReceiver.
     *
     * @return A new PtidesReceiver.
     */
    public Receiver newReceiver() {
        return new PtidesPlatformReceiver();
    }

    /**
     * Set a new value to the current time of the model, where the new time must
     * be no earlier than the current time. If the new time is bigger than the
     * stop time, stop the execution of the model.
     *
     * @param newTime
     *            The new time of the model.
     * @exception IllegalActionException
     *                If an attempt is made to change the time to less than the
     *                current time.
     */
    public void setModelTime(Time newTime) throws IllegalActionException {
        super.setModelTime(newTime);
        if (_stopTime != null && newTime.compareTo(_stopTime) > 0) {
            stop();
        }
    }

    /**
     * Schedule a platform to be resumed. Platforms are either executing or
     * stalled in the fireAt() method of this director. In the latter case, a
     * platform is resumed if the physical time was increased or because this
     * method was previously called by another platform. A platform calls this
     * method because it sends a new event to the platform specified by the
     * actor in the method parameter.
     *
     * @param actor
     *            Platform that should be resumed.
     */
    public void unblockWaitingPlatform(Actor actor) {
        _platformsToUnblock.add(actor);
    }

    /**
     * Reset local variables.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _platformsToUnblock.clear();

        TDLCausalityInterface causalityInterface = (TDLCausalityInterface) ((CompositeActor) this
                .getContainer()).getCausalityInterface();
        causalityInterface.wrapup();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Forward display events to the schedule listeners. This method is used as
     * the single point to monitor events from all actors in the model.
     *
     * @param node
     *            Platform that forwards the event.
     * @param actor
     *            Actor inside a platform for which the event was created. If
     *            the actor is null, the event is a platform event, e.g. input
     *            ports read or output ports written.
     * @param time
     *            Physical time at which the event occurred.
     * @param eventType
     *            Type of schedule event.
     */
    protected final void _displaySchedule(Actor node, Actor actor, double time,
            ScheduleEventType eventType) {
        if (_scheduleListeners != null) {
            Iterator listeners = _scheduleListeners.iterator();

            while (listeners.hasNext()) {
                ((ScheduleListener) listeners.next()).event(node, actor, time,
                        eventType);
            }
        }
    }

    /**
     * If there are platforms to unblock without increasing the physical time,
     * notify all platforms. Otherwise, resolve the deadlock. This method is
     * reached if all platforms are stalled in the fireAt() method of this
     * director because they are waiting for a future physical time.
     *
     * @return true if a real deadlock (see super class) is detected, false otherwise.
     * @exception IllegalActionException
     *                Not thrown in this base class. This might be thrown by
     *                derived classes.
     */
    protected boolean _resolveDeadlock() throws IllegalActionException {
        if (_platformsToUnblock.size() > 0) {
            notifyAll();
            return true;
        } else {
            if (_writeBlockedQueues.size() != 0) {
                // Artificial deadlock based on write blocks.
                _incrementLowestWriteCapacityPort();
                return true;
            } else if (_delayBlockCount == 0) {
                // Real deadlock with no delayed processes.
                return false;
            } else {
                // Artificial deadlock due to delayed processes.
                // Advance time to next possible time.
                synchronized (this) {
                    // There could be multiple events for the same
                    // actor for the same time (e.g. by sending events
                    // to this actor with same time stamps on different
                    // input ports. Thus, only _informOfDelayUnblock()
                    // for events with the same time stamp but different
                    // actors. 7/15/08 Patricia Derler
                    List unblockedActors = new ArrayList();
                    if (!_eventQueue.isEmpty()) {
                        //Take the first time-blocked process from the queue.
                        TimedEvent event = (TimedEvent) _eventQueue.take();
                        unblockedActors.add(event.contents);
                        //Advance time to the resumption time of this process.
                        if (_synchronizeToRealTime) {
                            // If synchronized to the real time.
                            Time currentTime;
                            //synchronized (this)
                            {
                                while (!_stopRequested && !_stopFireRequested) {
                                    currentTime = getModelTime();

                                    long elapsedTime = System
                                            .currentTimeMillis()
                                            - _realStartTime;
                                    double elapsedTimeInSeconds = elapsedTime / 1000.0;
                                    ptolemy.actor.util.Time elapsed = new ptolemy.actor.util.Time(
                                            this, elapsedTimeInSeconds);
                                    if (currentTime.compareTo(elapsed) <= 0) {
                                        break;
                                    }
                                    long timeToWait = (long) (currentTime
                                            .subtract(elapsed).getDoubleValue() * 1000.0);

                                    if (timeToWait > 0) {
                                        if (_debugging) {
                                            _debug("Waiting for real time to pass: "
                                                    + timeToWait);
                                        }

                                        try {
                                            _workspace.wait(this, timeToWait);
                                        } catch (InterruptedException ex) {
                                            throw new IllegalActionException(
                                                    this,
                                                    ex,
                                                    "Thread interrupted when waiting for"
                                                            + " real time to match model time.");
                                        }
                                    }
                                } // while
                            } // sync
                        } // if (_synchronizeToRealTime)
                        setModelTime(event.timeStamp);
                        _informOfDelayUnblock();
                    } else {
                        throw new InternalErrorException(
                                "Inconsistency"
                                        + " in number of actors blocked on delays count"
                                        + " and the entries in the CalendarQueue");
                    }

                    //Remove any other process waiting to be resumed at the new
                    //advanced time (the new currentTime).
                    boolean sameTime = true;

                    while (sameTime) {
                        //If queue is not empty, then determine the resumption
                        //time of the next process.
                        if (!_eventQueue.isEmpty()) {
                            //Remove the first process from the queue.
                            TimedEvent event = (TimedEvent) _eventQueue.take();
                            Actor actor = (Actor) event.contents;

                            //Get the resumption time of the newly removed
                            //process.
                            Time newTime = event.timeStamp;

                            //If the resumption time of the newly removed
                            //process is the same as the newly advanced time
                            //then unblock it. Else put the newly removed
                            //process back on the event queue.
                            if (newTime.equals(getModelTime())) {
                                if (unblockedActors.contains(actor)) {
                                    continue;
                                } else {
                                    unblockedActors.add(actor);
                                }
                                _informOfDelayUnblock();
                            } else {
                                _eventQueue.put(new TimedEvent(newTime, actor));
                                sameTime = false;
                            }
                        } else {
                            sameTime = false;
                        }
                    }

                    //Wake up all delayed actors
                    notifyAll();
                }
            }

            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Initialize parameters of the director.
     *
     * @exception NameDuplicationException
     *             Could occur if parameter with same name already exists.
     */
    private void _initialize() throws IllegalActionException,
            NameDuplicationException {
        stopTime = new Parameter(this, "stopTime");
        stopTime.setExpression("Infinity");
        stopTime.setTypeEquals(BaseType.DOUBLE);

        timeResolution.setVisibility(Settable.FULL);

        try {
            clockSyncError = new Parameter(this, "clockSyncError");
            clockSyncError.setExpression("0.1");
            clockSyncError.setTypeEquals(BaseType.DOUBLE);

            synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime");
            synchronizeToRealTime.setExpression("false");
            synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

            networkDelay = new Parameter(this, "networkDelay");
            networkDelay.setExpression("0.1");
            networkDelay.setTypeEquals(BaseType.DOUBLE);
        } catch (KernelException e) {
            throw new IllegalActionException(this, "Cannot set parameter:\n"
                    + e.getMessage());
        }
        _platformsToUnblock = new HashSet<Actor>();
        _scheduleListeners = new LinkedList<ScheduleListener>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /**
     * The bounded clock synchonization error for all platforms.
     */
    private double _clockSyncronizationError;

    /**
     * The bounded network delay for sending events between platforms.
     */
    private double _networkDelay;

    /**
     * Platforms that are currently blocked but received new events and should
     * be resumed at current model time.
     */
    private Set<Actor> _platformsToUnblock;

    /**
     * Registered schedule listeners that want to be informed about all schedule
     * events on the platforms.
     */
    private Collection<ScheduleListener> _scheduleListeners;

    /**
     * The stop time of the model.
     */
    private transient Time _stopTime;

    /** Specify whether the director should wait for elapsed real time to
     *  catch up with model time.
     */
    volatile private boolean _synchronizeToRealTime;

    /** The real time at which the model begins executing. */
    private long _realStartTime = 0;

}
