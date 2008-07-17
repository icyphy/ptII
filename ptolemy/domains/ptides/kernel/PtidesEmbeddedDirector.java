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
import java.util.Set;
import java.util.TreeSet; 
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Initializable;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector; 
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent; 
import ptolemy.data.DoubleToken; 
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;    
import ptolemy.domains.de.lib.TimedDelay;
import ptolemy.domains.ptides.kernel.TimedQueue.Event; 
import ptolemy.domains.ptides.lib.ScheduleListener.ScheduleEventType;
import ptolemy.domains.ptides.platform.NonPreemptivePlatformExecutionStrategy;
import ptolemy.domains.ptides.platform.PlatformExecutionStrategy;
import ptolemy.domains.ptides.platform.PreemptivePlatformExecutionStrategy;
import ptolemy.domains.tt.tdl.kernel.TDLModule;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.CompositeEntity; 
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * This director implements the Ptides model of computation. It should be used in 
 * composite actors that represent Ptides Platforms. It executes actors 
 * according to event time stamps. The difference to the DE director is that events
 * can be executed out of time stamped order. 
 * 
 * <p>
 * Platforms contain sensors, actuators, computation actors with worst 
 * case execution times and model time delay actors. The execution of 
 * these actors is governed by this director. Some actors inside a platform 
 * require a mapping of the model time defined in event time stamps to
 * real time. Those actors are sensors and actuators. Also, the 
 * execution of actors with a worst case execution time > 0 is 
 * simulated which requires the simulation of real time passing between 
 * the start and the termination of an actor. The enclosing director of 
 * a Ptides platform maintains the simulated real time; this director receives 
 * this time by calling the getModelTime() method of the enclosing director.
 * This enclosing director can be the PtidesDirector.
 * 
 * <p>
 * Executing actors in platforms happens according to their model timestamps. The order
 * of execution does not have to be in time stamp order, the only requirement is that
 * events on a port are only processed in time stamped order. To satisfy this 
 * requirement, an analysis on events is used to determine if they are safe to process,
 * i.e. if no events with earlier time stamps can occur on the same port or on 
 * another port in the port group. For this
 * analysis, the causality interface and the real dependencies between ports are used.
 *  
 * <p> 
 * This director executes actions in an infinite loop. At every iteration in the loop,
 * a set of all events is selected. Those events are safe to process. Out of this 
 * set of events, one event is selected to be actually processed. This selection is done
 * by the PlatformExecutionStrategy by taking into account platform characteristics
 * like preemption and priorities. The firing of an actor is divided into two steps, 
 * a start and a terminate. At the start of a firing, an actor is put into a list of
 * actors currently executing. For a platform that does not support preemption, this list
 * will have at most one entry. If the actor has a worst case execution time > 0, 
 * the platform schedules a refiring for the time = current physical time + WCET by
 * calling the fireAt() of the enclosing director. After that WCET passed, the actor
 * is taken out of the list of actors currently in execution. The actual firing of 
 * an actor is done either at the start or the termination time. This depends on 
 * whether the WCET is a static property of the actor or it is only known after
 * firing the actor. Most actomic actors will fall into the first category whereas
 * composite actors will fall into the second category. Also, actors for which the 
 * simulation is required to determine the WCET fall into the second category.
 * 
 * <p> 
 * There are two types of events used in this domain: regular events and pure events.
 * Regular events are stored in the receivers as pairs of timestamp and value. 
 * Additionally, for every actor, a list is created that saves time stamps of pure 
 * events for that actor.
 *  
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
	 *                If the director is not compatible with the specified
	 *                container.
	 * @exception NameDuplicationException
	 *                If the container not a CompositeActor and the name
	 *                collides with an entity in the container.
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

	/**
	 * The executionStrategy defines the order of execution of events.
	 * This parameter defaults to a basic non preemptive execution 
	 * strategy.
	 */
	public StringParameter executionStrategy;

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
			if (strategy.equals(PlatformExecutionStrategy.BASIC_NON_PREEMPTIVE)) {
				_executionStrategy = new NonPreemptivePlatformExecutionStrategy(
						this);
			} else if (strategy.equals(PlatformExecutionStrategy.BASIC_PREEMPTIVE)) {
                _executionStrategy = new PreemptivePlatformExecutionStrategy(
                        this);
            }
		} else {
			super.attributeChanged(attribute);
		}
	}

	/**
	 * If the enclosing director is a PtidesDirector, send these events 
	 * to the enclosing director. The PtidesDirector collects all display
	 * events from all platforms and sends them to schedule listeners. 
	 * If the enclosing director is not a PtidesDirector, do nothing.
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
	    if (this.getContainer() != null && ((Actor) this.getContainer()).getExecutiveDirector() instanceof PtidesDirector) {
    		PtidesDirector dir = (PtidesDirector) ((Actor) getContainer()).getExecutiveDirector();
    		if (dir != null)
    			dir._displaySchedule((Actor) getContainer(), actor, time,
    					scheduleEvent);
	    }
	}

	/**
	 * Get finishing time of actor in execution. The finishing time is the point
	 * in time when the WCET of the actor has passed.
	 * 
	 * @param actor
	 *            The actor in execution.
	 * @return The finishing time of the actor.
	 * @see #setFinishingTime(Actor, double)
	 */
	public Time getFinishingTime(Actor actor) {
		if (_finishingTimesOfActorsInExecution.get(actor) != null) {
			return _finishingTimesOfActorsInExecution.get(actor);
		}
		return Time.POSITIVE_INFINITY;
	}


	/**
	 * Return the current model time. When an actor is in 
	 * execution, this method returns the model time of the event responsible
	 * for firing the actor currently in 
	 * execution. Otherwise, this method returns 0. 
	 * @return The current model time.
	 */
	public Time getModelTime() {
		if (_currentModelTime == null)
			return new Time(this, 0);
		else {
			return _currentModelTime;
		}
	}
	
	/**
	 * The fire implements an infinite loop. In every iteration, a set of 
	 * events safe to process is selected. Out of these events. the platform 
	 * execution strategy selects on event that will be processed. If there is 
	 * no event selected, the platform waits. Otherwise, the actor targeted by
	 * the event is added to a set of actors in execution. If the actor has 
	 * a worst case execution time > 0, this Director calls the fireAt() with the
	 * current physical time increased by the WCET. Then, the actor is taken 
	 * out of the list of actors in execution.  
	 */
	public void fire() throws IllegalActionException {
		if (_stopRequested)
			return;
		List eventsToFire = null;
		TimedEvent event;
		while (true) {
		    if (_stopRequested)
	            return;
			if (_eventsInExecution.size() > 0) {
				Actor actorToFire = (Actor) _eventsInExecution
						.get(_eventsInExecution.size() - 1).contents; 
				Time time = getFinishingTime(actorToFire);
				if (time.equals(_currentPhysicalTime)) {
					_eventsInExecution.remove(_eventsInExecution
	                        .get(_eventsInExecution.size() - 1));
					if (!_fireAtTheBeginningOfTheWcet(actorToFire))
						_fireActorInZeroModelTime(actorToFire);
					_transferAllOutputs();
			        
					displaySchedule(actorToFire,
							_currentPhysicalTime.getDoubleValue(),
							ScheduleEventType.STOP);
					if (_eventsInExecution.size() > 0)
					    _currentModelTime = _eventsInExecution.get(_eventsInExecution.size() - 1).timeStamp;
					else 
					    _currentModelTime = null;
				}
			} 
			eventsToFire = _getNextEventsToFire();
			Time nextRealTimeEventTime = _getNextRealTimeEventTime(eventsToFire, _eventsInExecution);
			event = _executionStrategy.getNextEventToFire(_eventsInExecution,
					eventsToFire, nextRealTimeEventTime, _currentPhysicalTime);
			eventsToFire.remove(event);
			if (event != null) {
			 
				Actor actorToFire = (Actor) event.contents;
				
				
				
				// TODO remove first condition
				if (actorToFire.getDirector() == this && !actorToFire.prefire()) {
                    continue;
                } else {
                    // start firing an actor
    				_currentModelTime = event.timeStamp; 
    				_removeEvents(actorToFire);
    				if (_debugging)
                        _debug(this.getContainer().getName() + "-fired "
                                        + actorToFire.getName());
                    displaySchedule(actorToFire,
                                    _currentPhysicalTime.getDoubleValue(),
                                    ScheduleEventType.START);     
    				if (_fireAtTheBeginningOfTheWcet(actorToFire))
    					_fireActorInZeroModelTime(actorToFire);
    				double wcet = PtidesActorProperties.getWCET(actorToFire);
    				setFinishingTime(actorToFire, _currentPhysicalTime.add(wcet));
					for (int i = 0; i < _eventsInExecution.size(); i++) {
						Actor actor = (Actor) _eventsInExecution.get(i).contents;
						setFinishingTime(actor,
								getFinishingTime(actor).add(wcet)); 
					}
					_eventsInExecution.add(event);
				}
			} else {
				if (_transferAllInputs()) {
					continue;
				}
				((Actor) getContainer()).getExecutiveDirector().fireAt((Actor) this.getContainer(), nextRealTimeEventTime);
				_currentPhysicalTime = ((Actor) getContainer()).getExecutiveDirector().getModelTime();
				if (_stopRequested)
					return;
				_transferAllInputs();
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
	 * @exception IllegalActionException
	 *                If event queue is not ready.
	 */
	public void fireAt(Actor actor, Time time) throws IllegalActionException {
		_enqueueEvent(actor, time);
	}

	/**
	 * Inputs are only transfered from the outside of the container to the
	 * actors inside if they are safe to process according to PTIDES. Tokens are
	 * safe to process if eventTimestamp - minDelay + clockSyncError +
	 * networkDelay <= physicalTime
	 * 
	 * @param time
	 *            Timestamp of the event.
	 * @param object
	 *            Port or Actor
	 * @return true if tokens can be transferred.
	 */
	public boolean isSafeToProcessOnNetwork(Time time, NamedObj object) {
		double minDelayTime = PtidesActorProperties.getMinDelayTime(object);
		System.out.println(minDelayTime + " " + Double.MAX_VALUE);
		return minDelayTime == Double.MAX_VALUE 
				|| time.subtract(minDelayTime).add(_clockSyncError).add(
						_networkDelay).compareTo(_currentPhysicalTime) <= 0;
	}

	/**
	 * Tokens between Platform elements are only safe to process if timestamp -
	 * minimumDelay <= physicalTime. The minimum delay must be specified as a
	 * parameter at the port.
	 * 
	 * @param time
	 *            The time.
	 * @param object
	 *            The object to to check the minimum delay time.
	 * @return True if it is safe to process a token.
	 */
	public boolean isSafeToProcessOnPlatform(Time time, NamedObj object) {
		double minDelayTime = PtidesActorProperties.getMinDelayTime(object);
		return minDelayTime == Double.MAX_VALUE
				|| time.subtract(minDelayTime).compareTo(_currentPhysicalTime) <= 0;
	}

	/**
	 * Create a PtidesPlatformReceiver.
	 */
	public Receiver newReceiver() {
		if (_debugging && _verbose) {
			_debug("Creating a new DE receiver.");
		}
		return new PtidesPlatformReceiver();
	}

	/**
	 * Prefire of the director.
	 * 
	 * @return True if the composite actor is ready to fire.
	 * @exception IllegalActionException
	 *                If there is a missed event, or the prefire method of the
	 *                super class throws it, or can not query the tokens of the
	 *                input ports of the container of this director.
	 *                </p>
	 */
	public boolean prefire() throws IllegalActionException {
		return !_stopRequested;
	}

	/**
	 * Set the finishing time of an actor. This method is called after the
	 * execution of an actor is started.
	 * 
	 * @param actor
	 *            The actor in execution.
	 * @param finishingTime
	 *            The time the actor will finish.
	 * @see #getFinishingTime(NamedObj)
	 */
	public void setFinishingTime(Actor actor, Time finishingTime) {
		if (_finishingTimesOfActorsInExecution.get(actor) != null)
			_finishingTimesOfActorsInExecution.remove(actor);
		_finishingTimesOfActorsInExecution.put(actor, finishingTime);
	}

	/**
	 * Initialize variables.
	 */
	public void preinitialize() throws IllegalActionException { 
	    super.preinitialize();
	    _eventQueues = new Hashtable();
        for (Iterator it = ((CompositeActor) getContainer()).entityList()
                .iterator(); it.hasNext();) {
            _eventQueues.put((Actor) it.next(), new TreeSet<Time>());
        }

        _currentPhysicalTime = ((Actor) getContainer()).getExecutiveDirector()
                .getModelTime();
        _eventsInExecution.clear(); 
	}
	
	/**
	 * Transfer outputs and return true if any tokens were transfered.
	 * 
	 * @exception IllegalActionException
	 *                If the port is not an opaque output port.
	 * @param port
	 *            The port to transfer tokens from.
	 * @return True if data are transferred.
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

	// /////////////////////////////////////////////////////////////////
	// // protected methods ////

	/**
     * Clock synchronization error specified in the top level director.
     */
    protected double _clockSyncError;

    /**
     * Current physical time which is retrieved by getting the model time 
     * of the enclosing director.
     */
    protected Time _currentPhysicalTime;

    /**
     * Events on input ports of the composite actor directed by this 
     * director with the time stamp set to the physical time when they
     * are safe to process.
     */
    protected CalendarQueue _inputSafeToProcess = new CalendarQueue(
            new TimedEvent.TimeComparator());

    /**
     * Network delay time specified in the top-level director.
     */
    protected double _networkDelay;

    /**
	 * Put a pure event into the event queue for the given actor to fire at the
	 * specified timestamp. 
	 * 
	 * @param actor
	 *            The actor to be fired.
	 * @param time
	 *            The timestamp of the event.
	 * @exception IllegalActionException
	 *                If the time argument is less than the current model time,
	 *                or the depth of the actor has not be calculated, or the
	 *                new event can not be enqueued.
	 */
	protected synchronized void _enqueueEvent(Actor actor, Time time)
			throws IllegalActionException { 
		_eventQueues.get(actor).add(time); 
	}

	/**
	 * Transfer input ports if they are safe to process. If the token is not
	 * safe to process, inform the executive director to be refired at the time
	 * it is safe to process the token.
	 * @param port Transfer input on this port.
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
			try {
				while (port.hasToken(i)) {
				      System.out.println("in");
					Receiver[][] recv = port.getReceivers();
					PtidesReceiver receiver = (PtidesReceiver) recv[0][0];
					Event event = receiver.getEvent();
					Time time = event._timeStamp;
					Token t = event._token;
					if (time.compareTo(_currentPhysicalTime) < 0)
						throw new IllegalActionException(
								"Network interface constraints violated at "
										+ this.getContainer().getName()
										+ ", tried to transfer event with timestamp "
										+ time + " at physical time "
										+ _currentPhysicalTime);
					if (!((isSafeToProcessOnNetwork(time, port)) || (time
							.compareTo(_currentPhysicalTime) > 0))) {
					    _inputSafeToProcess.put(new TimedEvent(time.subtract(
					            PtidesActorProperties.getMinDelayTime(port))
								.add(_clockSyncError).add(_networkDelay), port));
						// at this time, the new input should be read
						receiver.put(t, time);
						continue; // couldn't transfer newest token
					}
					Receiver[][] farReceivers = port.deepGetReceivers();
					if ((farReceivers == null) || (farReceivers.length <= i)
							|| (farReceivers[i] == null)) {
						continue;
					}
					for (int k = 0; k < farReceivers.length; k++) {
						for (int l = 0; l < farReceivers[k].length; l++) {
							PtidesPlatformReceiver farReceiver = (PtidesPlatformReceiver) farReceivers[k][l];
							farReceiver.put(t, time);
							displaySchedule((Actor) port.getContainer(),
									_currentPhysicalTime.getDoubleValue(),
									ScheduleEventType.TRANSFERINPUT);
							System.out.println("transfer input value " + t + " at "
                                                                                + time);
							if (_debugging)
								_debug("transfer input value " + t + " at "
										+ time);
							wasTransferred = true;
						}
					}
				}
			} catch (ArithmeticException ex) {
				return false;
			} catch (NoTokenException ex) {
				throw new InternalErrorException(this, ex, null);
			}
		}
		return wasTransferred;
	}

	/**
	 * Transfer output ports.
	 * 
	 * @throws IllegalActionException
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
							PtidesReceiver outReceiver = (PtidesReceiver) outReceivers[k][l];
							outReceiver.put(token,
									((Actor) port.getContainer()).getDirector()
											.getModelTime());
							displaySchedule((Actor) port.getContainer(),
									_currentPhysicalTime.getDoubleValue(),
									ScheduleEventType.TRANSFEROUTPUT);
							System.out.println("transfer output value " + token
                                                                                + " at rt: " + _currentPhysicalTime + "/mt: "
                                                                                + getModelTime());
							if (_debugging)
								_debug("transfer output value " + token
										+ " at rt: " + _currentPhysicalTime + "/mt: "
										+ getModelTime());
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

	// /////////////////////////////////////////////////////////////////
	// // private methods ////

	/**
	 * Return time stamp of next real time event. A real time event is either
	 * an event for a sensor or an actuator, the end of the WCET of an actor
	 * in execution or the time stamp when an input event becomes safe to 
	 * process.
	 * @param eventsToFire Set of events that are safe to process.
	 * @param eventsInExecution Set of events currently in execution
	 * @return Time of next real time event.
	 */
	private Time _getNextRealTimeEventTime(List<TimedEvent> eventsToFire, 
            List<TimedEvent> eventsInExecution) {
        Time nextRealTimeEvent = Time.POSITIVE_INFINITY;
        for (int i = 0; i < eventsToFire.size(); i++) {
            TimedEvent event = (TimedEvent) eventsToFire.get(i); 
            if (PtidesActorProperties.mustBeFiredAtRealTime(event.contents)) {
                if (nextRealTimeEvent != null && 
                        event.timeStamp.compareTo(nextRealTimeEvent) < 0) {
                    nextRealTimeEvent = event.timeStamp;
                }
            }
        }
        if (eventsInExecution.size() > 0) {
            Time time = getFinishingTime((Actor)eventsInExecution.get(0).contents);
            if (nextRealTimeEvent != null && 
                    time.compareTo(nextRealTimeEvent) < 0) 
                nextRealTimeEvent = time;
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
	 * Determine if there is an event with time stamp - minDelay > time stamp of
	 * event at current node upstream. if so, the current event can be
	 * processed.
	 * 
	 * @param eventTimestamp
	 *            Time stamp of current event.
	 * @param node
	 *            Node containing the port which contains the current event.
	 * @param traversedEdges
	 *            Edges that have been traversed already.
	 * @return True if the event is safe to process.
	 * @throws IllegalActionException
	 *             Thrown if the delay of a DelayActor could not be retrieved.
	 */
	private boolean _isSafeToProcess(Time eventTimestamp, Node node,
			Set<Edge> traversedEdges) throws IllegalActionException {
		Collection inputs = graph.inputEdges(node);
		if (inputs.size() == 0)
			return false;
		Iterator inputIt = inputs.iterator();
		while (inputIt.hasNext()) {
			Edge edge = (Edge) inputIt.next();
			Node nextNode = edge.source();
			Actor inputActor = (Actor) ((IOPort) nextNode.getWeight())
					.getContainer();
			if (!traversedEdges.contains(edge)) {
				traversedEdges.add(edge);
				if (inputActor instanceof TimedDelay) {
					TimedDelay delayActor = (TimedDelay) inputActor;
					double delay = ((DoubleToken) (delayActor.delay.getToken()))
							.doubleValue();
					eventTimestamp = eventTimestamp.subtract(delay);
				}
				if (PtidesActorProperties
						.mustBeFiredAtRealTime(inputActor))
					return false; // didn't find earlier events
				else if (inputActor.equals(this.getContainer()))
					return false; // didn't find earlier events in platform
				for (Iterator it = inputActor.inputPortList().iterator(); it
						.hasNext();) {
					IOPort p = (IOPort) it.next();
					Receiver[][] receivers = p.getReceivers();
					for (int i = 0; i < receivers.length; i++) {
						Receiver[] recv = receivers[i];
						for (int j = 0; j < recv.length; j++) {
							PtidesPlatformReceiver receiver = (PtidesPlatformReceiver) recv[j];
							Time time = receiver.getNextTime();
							if (time.compareTo(eventTimestamp) > 0)
								return true;
						}
					}
				}
			}
			for (Iterator it = inputActor.inputPortList().iterator(); it
					.hasNext();) {
				return _isSafeToProcess(eventTimestamp, graph.node(it.next()),
						traversedEdges);
			}
		}
		return false; // should never come here
	}
	
    /**
     * The actual firing of an actor takes zero model time, the wcet is simulated by
     * either firing an actor at the beginning or the end of the wcet.
     * @param actorToFire Actor that has to be fired.
     * @throws IllegalActionException Thrown if actor cannot be fired.
     */
    private void _fireActorInZeroModelTime(Actor actorToFire) throws IllegalActionException {
        if (actorToFire instanceof CompositeActor)
                actorToFire.getDirector().setModelTime(getModelTime());
        actorToFire.fire(); 
        actorToFire.postfire();
    }
    
    /**
     * Determines if the actor has to be fired at the beginning of the WCET. An actor
     * must be fired at the beginning of the WCET the WCET can only
     * be determined after firing the actor. An example for this actor is a TDLModule.
     * An actor can only be fired at the beginning
     * of the LET if the output values are not updated immediately.
     * @param actor Actor that has to be fired.
     * @return True if actor has to be fired at the beginning of the WCET.
     */
    private boolean _fireAtTheBeginningOfTheWcet(Actor actor) {
            if (actor instanceof TDLModule)
                    return true;
            return false;
    }
    
    /**
     * Remove all events for the given actor for the current model time. This method is
     * called after firing an actor.
     * @param actor Actor which was just fired, thus the events have to be removed.
     */
    private void _removeEvents(Actor actor) {
        TreeSet<Time> set = _eventQueues.get(actor); 
        // take pure events
        if (!set.isEmpty()) {
            Time time = set.first(); 
            if (time.equals(getModelTime())) {
                set.remove(time);
            }
        }
    }

	/**
	 * Get the list of events that are safe to fire. Those events contain pure
	 * events and triggered events.
	 * 
	 * @return List of events that can be fired next.
	 */
	private List<TimedEvent> _getNextEventsToFire() throws IllegalActionException {
		List<TimedEvent> events = new LinkedList<TimedEvent>();
		for (Iterator ait = _eventQueues.keySet().iterator(); ait.hasNext();) {
			Actor actor = (Actor) ait.next();
			TreeSet<Time> set = _eventQueues.get(actor);

			// take pure events
			if (!set.isEmpty()) {
			    Time time = set.first();
				boolean isSafe = isSafeToProcessOnPlatform(time,
						(NamedObj) actor);
				if (isSafe) {
					events.add(new TimedEvent(time, actor)); 
				}
			}
			// take trigger events
			if (!_eventsInExecution.contains(actor)) {
				for (Iterator<IOPort> it = actor.inputPortList().iterator(); it
						.hasNext();) {
					IOPort port = it.next();
					if (PtidesActorProperties.portIsTriggerPort(port)) {
    					Receiver[][] receivers = port.getReceivers();
    					for (int i = 0; i < receivers.length; i++) {
    						Receiver[] recv = receivers[i];
    						for (int j = 0; j < recv.length; j++) {
    							PtidesPlatformReceiver receiver = (PtidesPlatformReceiver) recv[j];
    							Time time = receiver.getNextTime();
    							if (time != null
    									&& (isSafeToProcessOnPlatform(time, port) || _isSafeToProcess(
    											time, graph.node(port),
    											new TreeSet()))) {
    							    List<TimedEvent> toRemove = new ArrayList<TimedEvent>();
    						        for (int k = 0; k < events.size(); k++) {
    						            TimedEvent event = (TimedEvent) events.get(k);
    						            if (event.contents == actor
    						                    && event.timeStamp.equals(time))
    						                toRemove.add(event);
    						        }
    						        for (int k = 0; k < toRemove.size(); k++)
    						            events.remove(toRemove.get(k));
    								events.add(new TimedEvent(time, port));
    							}
    						}
    					}
					}
				}
			}
		}
		if (_debugging)
			_debug("events that are safe to fire: " + events.size() + " "
					+ events);
		return events;
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
	 * Transfer all input tokens. If input ports were transferred, the next
	 * actor to be fired might have changed.
	 * 
	 * @return True if input ports were transferred.
	 * @throws IllegalActionException
	 *             If reading from the associated port throws it or inputs could
	 *             not be transferred.
	 */
	private boolean _transferAllInputs() throws IllegalActionException {
		boolean inputTransferred = false;
		for (Iterator inputPorts = ((Actor) getContainer()).inputPortList()
				.iterator(); inputPorts.hasNext() && !_stopRequested;) {
			IOPort p = (IOPort) inputPorts.next();
			if (p instanceof ParameterPort) {
				((ParameterPort) p).getParameter().update();
			} else {
				inputTransferred = transferInputs(p);
			}
		}
		return inputTransferred;
	}

	/**
	 * Transfer all output tokens.
	 * 
	 * @return true if output tokens were transfered.
	 * @throws IllegalActionException
	 *             Attempted to transferOutputs on a port that is not an opaque
	 *             input port.
	 */
	private boolean _transferAllOutputs() throws IllegalActionException {
	    boolean transferedOutputs = false;
        Iterator outports = ((Actor) getContainer()).outputPortList()
                .iterator();
        while (outports.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) outports.next();
            transferedOutputs = transferedOutputs | _transferOutputs(p);
        }
        return transferedOutputs;
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/**
	 * List of actors in execution. In a non-preemptive execution, the list only
	 * contains one item.
	 */
	private List<TimedEvent> _eventsInExecution = new ArrayList<TimedEvent>();

	/**
	 * The current model time is adjusted when an actor is fired and set to the
	 * event time stamp of the event causing the firing.
	 */
	private Time _currentModelTime;

	/**
	 * Contains finishing times of actors in execution.
	 */
	private Hashtable<Actor, Time> _finishingTimesOfActorsInExecution = new Hashtable();

	/**
	 * Contains an event queue for every actor.
	 */
	private Hashtable<Actor, TreeSet<Time>> _eventQueues;

	/**
	 * Used execution strategy which is set according to a parameter.
	 */
	private PlatformExecutionStrategy _executionStrategy;

	/**TODO: remove
	 */
	public DirectedGraph graph;

}