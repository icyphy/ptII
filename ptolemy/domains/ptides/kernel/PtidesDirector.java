/*
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.pn.kernel.TimedPNDirector;
import ptolemy.domains.ptides.lib.ScheduleListener;
import ptolemy.domains.ptides.lib.ScheduleListener.ScheduleEventType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * Top-level director for PTIDES models. A Ptides model consists of platforms
 * represented by CompositeActors that communicate via events. Those platforms
 * run in threads. This director is a timed director, the time represents the
 * global <i>physical time</i>. This physical time is used in all platforms, for
 * sending events between platforms, a global bounded clock synchronization
 * error and a global bounded network delay is considered.
 * 
 * <p>
 * Platforms contain sensors, actuators, computation actors with worst case
 * execution times and model time delay actors. The execution of actors inside a
 * platform is governed by a director that executes events according to their
 * <i>model time</i> time stamps. This director can be either a DEDirector or
 * the PtidesEmbeddedDirector which is a smarter DEDirector in the sense that it
 * allows an out of time stamp order execution of events. Some actors inside a
 * platform require a mapping of the model time defined in event time stamps to
 * real time. Those actors are sensors and actuators. Also, the execution of
 * actors with a worst case execution time > 0 is simulated which requires the
 * simulation of real time passing between the start and the termination of an
 * actor.
 * 
 * <p>
 * A platform executes events as long as there are events safe to process and
 * possible to process. Safe to process means that there is no chance of an
 * event arriving at the same port with an earlier time stamp. Whether it is
 * possible to process an event is determined by the platform characteristics,
 * e.g. an event cannot be processed if the actor has a worst case execution
 * time that is bigger than the time stamp of an event for an actuator and the
 * actuator event cannot preempt the execution of the first event.
 * 
 * <p>
 * When a platform has no more events safe and possible to process at the
 * current real time, it schedules a refiring at a future physical time. This
 * platform calls the fireAt() method of this director which blocks the
 * platform. A platform is resumed when:
 * <ul>
 * <li>The real time has been increased to the new real time at which the
 * platform wanted to be refired or</li>
 * <li>Another platform sent an event to the platform. This event might be safe
 * to process, it is up to the platform to find that out.</li>
 * </ul>
 * 
 * <p>
 * A Ptides model never terminates thus a stop time must be specified.
 * 
 * <p>
 * Difference to DE with threads: This director does not call the fire of the
 * contained actors, the contained actors fire in an infinite loop. If there is
 * nothing to fire, the actor waits until the physical time of the
 * PtidesDirector is increased.
 * 
 * <p>
 * Difference to PN, TimedPN and DDE: There are no blocking writes or reads
 * between actors. The only way an actor can be blocked is because it waits for
 * time to pass.
 * 
 * <p>
 * Usage: Every actor in a model controlled by the PtidesDirector must be a
 * CompositeActor.
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
     * The bounded clock synchonization error for all platforms. This parameter
     * must contain a DoubleToken. The value defaults to 0.1.
     */
    public Parameter clockSyncError;

    /**
     * The bounded network delay for sending events between platforms. This
     * parameter must contain a DoubleToken. The value defaults to 0.1.
     */
    public Parameter networkDelay;

    /**
     * The stop time of the model. This parameter must contain a DoubleToken.
     * The value defaults to Infinity.
     */
    public Parameter stopTime;

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
            _clockSyncError = ((DoubleToken) clockSyncError.getToken())
                    .doubleValue();
        } else if (attribute == networkDelay) {
            _networkDelay = ((DoubleToken) networkDelay.getToken())
                    .doubleValue();
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
        _platformsToUnblock = new ArrayList<Actor>();
        _scheduleListeners = new LinkedList<ScheduleListener>();
        return newObject;
    }

    /**
     * Suspend the calling process until either time has advanced to the time
     * specified by the method argument or another process scheduled a resuming
     * for the actor. Add the actor corresponding to the calling process to
     * queue of waiting processes and sort it by the time specified by the
     * method argument. Increment the count of the actors blocked on a delay.
     * 
     * @param actor
     *            Actor that schedules to be refired.
     * @param newFiringTime
     *            Future time actor wants to be refired at.
     * @exception IllegalActionException
     *                If the operation is not permissible (e.g. the given time
     *                is in the past).
     */
    public synchronized void fireAt(Actor actor, Time newFiringTime)
            throws IllegalActionException {
        if (newFiringTime.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(this, "The process wants to "
                    + " get fired in the past!");
        }

        _eventQueue.put(new TimedEvent(newFiringTime, actor));
        _informOfDelayBlock();

        try {
            while (!_stopRequested
                    && getModelTime().compareTo(newFiringTime) < 0) {
                if (_platformsToUnblock.contains(actor)) {
                    _platformsToUnblock.remove(actor);
                    _informOfDelayUnblock();
                    break;
                }
                workspace().wait(this);
            }
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Initialize parameters and the schedule listeners. Calculate minimum
     * delays for ports on platforms according to Ptides.
     * 
     * @throws IllegalActionException
     *             Thrown if other actors than CompositeActors are used in this
     *             model.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentTime = new Time(this, 0.0);
        _stopTime = new Time(this, ((DoubleToken) stopTime.getToken())
                .doubleValue());

        PtidesGraphUtilities utilities = new PtidesGraphUtilities(this
                .getContainer());
        utilities.calculateMinDelays();

        // Iterate through all actors in the model and add them to a List. This
        // List is handed to the schedule listeners.
        Hashtable<Actor, List> table = new Hashtable<Actor, List>();
        for (Iterator it = ((CompositeActor) getContainer()).entityList()
                .iterator(); it.hasNext();) {
            Object obj = it.next();
            if (obj instanceof CompositeActor) {
                CompositeActor actor = (CompositeActor) obj;
                if (actor.getDirector() instanceof PtidesEmbeddedDirector) {
                    PtidesEmbeddedDirector dir = (PtidesEmbeddedDirector) actor
                            .getDirector();
                    dir._clockSyncError = _clockSyncError;
                    dir._networkDelay = _networkDelay;
                }
                List<Actor> actors = new ArrayList<Actor>();
                for (Iterator it2 = actor.entityList().iterator(); it2
                        .hasNext();) {
                    Object o = it2.next();
                    if (o instanceof Actor) {
                        actors.add((Actor) o);
                    }
                }
                table.put(actor, actors);
            } else {
                throw new IllegalActionException(
                        "Only composite actors are allowed to "
                                + "be used here");
            }
        }
        synchronized (this) {
            if (_scheduleListeners != null) {
                Iterator listeners = _scheduleListeners.iterator();

                while (listeners.hasNext()) {
                    ((ScheduleListener) listeners.next()).initialize(table);
                }
            }
        }

    }

    /**
     * Create a new PtidesReceiver.
     * 
     * @return A new PtidesReceiver.
     */
    public Receiver newReceiver() {
        return new PtidesReceiver();
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
     * Schedule the thread that runs the platform represented by this actor to
     * be resumed. The actual resuming is done in the fireAt() method.
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
     * Return false on detection of a real deadlock. Otherwise break the
     * deadlock and return true. To break a timed deadlock, the time is advanced
     * to the earliest time a delayed process is waiting for. If there are
     * threads to unblock because they received new events, don't increase time
     * but notify all platforms.
     * 
     * @return true if a real deadlock is detected, false otherwise.
     * @exception IllegalActionException
     *                Not thrown in this base class. This might be thrown by
     *                derived classes.
     */
    protected boolean _resolveDeadlock() throws IllegalActionException {
        if (_platformsToUnblock.size() > 0) {
            notifyAll();
            return true;
        } else {
            return super._resolveDeadlock();
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    /**
     * Initialize parameters of the director.
     * 
     * @throws NameDuplicationException
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

            networkDelay = new Parameter(this, "networkDelay");
            networkDelay.setExpression("0.1");
            networkDelay.setTypeEquals(BaseType.DOUBLE);
        } catch (KernelException e) {
            throw new InternalErrorException("Cannot set parameter:\n"
                    + e.getMessage());
        }
        _platformsToUnblock = new ArrayList<Actor>();
        _scheduleListeners = new LinkedList<ScheduleListener>();
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * The bounded clock synchonization error for all platforms.
     */
    private double _clockSyncError;

    /**
     * The bounded network delay for sending events between platforms.
     */
    private double _networkDelay;

    /**
     * Platforms that are currently blocked but received new events and should
     * be resumed at current model time.
     */
    private List<Actor> _platformsToUnblock;

    /**
     * Registered schedule listeners that want to be informed about all schedule
     * events on the platforms.
     */
    private Collection<ScheduleListener> _scheduleListeners;

    /**
     * The stop time of the model.
     */
    private Time _stopTime;

}
