package ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
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
import ptolemy.actor.lib.Clock;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.de.kernel.DECQEventQueue;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.domains.de.lib.TimedDelay;
import ptolemy.domains.ptides.kernel.PrioritizedTimedQueue.Event;
import ptolemy.domains.ptides.lib.ScheduleListener;
import ptolemy.domains.ptides.platform.NonPreemptivePlatformExecutionStrategy;
import ptolemy.domains.ptides.platform.PlatformExecutionStrategy;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
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
 * This director is used on a platform in the PTIDES domain. The execution is
 * simular to the DEDirector but enables "smarter" processing of events. By
 * adding minimum delays to actors and determining independent subgraphs inside
 * a domain, events can be processed out of the timestamped order.
 * 
 * @author Patricia Derler
 */
public class PtidesEmbeddedDirector extends DEDirector {

	/**
	 * Construct a director in the default workspace with an empty string as its
	 * name. The director is added to the list of objects in the workspace.
	 * Increment the version number of the workspace.
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
	 */
	public StringParameter executionStrategy;

	/**
	 * Update the director parameters when attributes are changed. Changes to
	 * <i>isCQAdaptive</i>, <i>minBinCount</i>, and <i>binCountFactor</i>
	 * parameters will only be effective on the next time when the model is
	 * executed.
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
						_physicalTime, this);
			}
		} else {
			super.attributeChanged(attribute);
		}
	}

	/**
	 * Display event in Scheduleplotter.
	 * 
	 * @param actor
	 *            Actor for which the event occured.
	 * @param time
	 *            physical time at which the event occured.
	 * @param scheduleEvent
	 *            Type of event.
	 */
	public final void displaySchedule(Actor actor, double time,
			int scheduleEvent) {
		PtidesDirector dir = _getExecutiveDirector();
		if (dir != null)
			dir._displaySchedule((Actor) getContainer(), actor, time,
					scheduleEvent);
	}

	/**
	 * Get deadline of actor in execution.
	 * 
	 * @param actor
	 * @return
	 */
	public double getDeadline(NamedObj actor) {
		if (_deadlinesOfActorsInExecution.get(actor) != null) {
			return (Double) _deadlinesOfActorsInExecution.get(actor);
		}
		return 0.0;
	}

	/**
	 * Return the timestamp of the next event in the queue. The next iteration
	 * time, for example, is used to estimate the run-ahead time, when a
	 * continuous time composite actor is embedded in a DE model. If there is no
	 * event in the event queue, a positive infinity object is returned.
	 * 
	 * @return The time stamp of the next event in the event queue.
	 */
	public Time getModelNextIterationTime() {
		try {
			Time nextIterationTime = Time.POSITIVE_INFINITY;
			List eventsToFire = _getNextEventsToFire();
			if (eventsToFire == null || eventsToFire.size() == 0)
				return _physicalTime;
			for (Iterator it = eventsToFire.iterator(); it.hasNext();) {
				DEEvent event = (DEEvent) it.next();
				if (event.timeStamp().compareTo(nextIterationTime) < 0)
					nextIterationTime = event.timeStamp();
			}
			return nextIterationTime;
		} catch (IllegalActionException e) {
			return null;
		}
	}

	/**
	 * Return the current model time. This director does not have a valid model
	 * time except during the time an actor is being fired. Then, the model time
	 * is the time of the event which caused the firing of that actor.
	 */
	public Time getModelTime() {
		if (_currentModelTime == null)
			return _currentTime;
		else {
			return _currentModelTime;
		}
	}

	/**
	 * Until _stopRequested, the director tries to fire actors or waits. First,
	 * a set of events is calculated that can be fired at current time. Out of
	 * this set of events, one event is chosen that is fired. The actor for this
	 * event is added to the list of actors in execution and its deadline is set
	 * to event.timestamp() + wcet. After the expiration of the deadline, the
	 * actor is removed from the list and the fire() of the actor is executed.
	 * If no actor is found that can execute now, this actor waits. It continues
	 * execution when the phyiscal time is increased or when tokens are received
	 * on the input ports. If an actor is preempted, the preempting actor is
	 * added to the list of actors in execution and the deadlines for all other
	 * actors in execution are updated to oldDeadline + wcet of the preempting
	 * actor.
	 */
	public void fire() throws IllegalActionException {
		if (_stopRequested)
			return;
		List eventsToFire = null;
		DEEvent event = null;
		while (true) {
			if (_actorsInExecution.size() > 0) {
				Actor actorToFire = (Actor) _actorsInExecution
						.get(_actorsInExecution.size() - 1);
				double d = getDeadline((NamedObj) actorToFire);
				if (d < _physicalTime.getDoubleValue()) {
					// should not happen but would mean that a synchronization point was missed
				}
				if (d == _physicalTime.getDoubleValue()) {
					_actorsInExecution.remove(actorToFire);
					displaySchedule(actorToFire,
							_physicalTime.getDoubleValue(),
							ScheduleListener.STOP);
					if (actorToFire instanceof CompositeActor)
						actorToFire.getDirector().setModelTime(getModelTime());
					actorToFire.fire();
					if (!actorToFire.postfire())
						_disableActor(actorToFire);
					_transferAllOutputs();
					_currentModelTime = null;
				}
			}
			_enqueueRemainingEventsAgain(eventsToFire);
			eventsToFire = _getNextEventsToFire();
			for (int i = 0; i < eventsToFire.size(); i++)
				_addSynchronizationPoint(((DEEvent) eventsToFire.get(i))
						.timeStamp());
			event = _executionStrategy.getNextEventToFire(_actorsInExecution,
					eventsToFire);
			eventsToFire.remove(event);
			if (event != null) {
				Actor actorToFire = event.actor();
				double wcet = PtidesGraphUtilities.getWCET(actorToFire);
				if (actorToFire.getDirector() == this && !actorToFire.prefire()) {
					_enqueueEventAgain(event);
				} else {
					_currentModelTime = event.timeStamp();
					displaySchedule(actorToFire,
							_physicalTime.getDoubleValue(),
							ScheduleListener.START);
					if (_debugging)
						_debug(this.getContainer().getName()
							+ "-fired " + actorToFire.getName());
					_addSynchronizationPoint(_physicalTime.add(wcet));
					setDeadline(actorToFire, _physicalTime.add(wcet)
							.getDoubleValue());
					for (int i = 0; i < _actorsInExecution.size(); i++) {
						Actor actor = (Actor) _actorsInExecution.get(i);
						setDeadline(actor, getDeadline((NamedObj) actor) + wcet);
					}
					_actorsInExecution.add(actorToFire);
				}
			} else {
				if (_transferAllInputs()) {
					continue;
				}
				_setPhysicalTime(_getExecutiveDirector()
						.waitForFuturePhysicalTime());
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
		if (_eventQueues == null || _eventQueues.get(actor) == null) {
			throw new IllegalActionException(this,
					"Calling fireAt() before preinitialize().");
		}
		if (_debugging)
			_debug("DEDirector: Actor " + actor.getFullName()
					+ " requests refiring at " + time);

		synchronized (_eventQueues) {
			_enqueueEvent(actor, time);
			_eventQueues.notifyAll();
		}
	}

	/**
	 * Initialize all the contained actors by invoke the initialize() method of
	 * the super class. If any events are generated during the initialization,
	 * and the container is not at the top level, request a refiring.
	 * <p>
	 * The real start time of the model is recorded when this method is called.
	 * This method is <i>not</i> synchronized on the workspace, so the caller
	 * should be.
	 * </p>
	 * 
	 * @exception IllegalActionException
	 *                If the initialize() method of the super class throws it.
	 */
	public void initialize() throws IllegalActionException {
		_isInitializing = true;
		_disabledActors = null;
		_microstep = 0;
		super.initialize();
		_isInitializing = false;
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
		double minDelayTime = PtidesGraphUtilities.getMinDelayTime(object);
		return minDelayTime == Double.MAX_VALUE
				|| time.subtract(minDelayTime).add(_clockSyncError).add(
						_networkDelay).compareTo(_physicalTime) <= 0;
	}

	/**
	 * Tokens between Platform elements are only safe to process if timestamp -
	 * minimumDelay <= physicalTime. The minimum delay must be specified als a
	 * parameter at the port.
	 * 
	 * @param time
	 * @param object
	 * @return
	 */
	public boolean isSafeToProcessOnPlatform(Time time, NamedObj object) {
		double minDelayTime = PtidesGraphUtilities.getMinDelayTime(object);
		return minDelayTime == Double.MAX_VALUE
				|| time.subtract(minDelayTime).compareTo(_physicalTime) <= 0;
	}

	/**
	 * Create a PtidesDEReceiver.
	 */
	public Receiver newReceiver() {
		if (_debugging && _verbose) {
			_debug("Creating a new DE receiver.");
		}
		return new PtidesPlatformReceiver();
	}

	/**
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
	 * Set the current timestamp to the model start time, invoke the
	 * preinitialize() methods of all actors deeply contained by the container.
	 * <p>
	 * This method should be invoked once per execution, before any iteration.
	 * Actors cannot produce output data in their preinitialize() methods. If
	 * initial events are needed, e.g. pure events for source actor, the actors
	 * should do so in their initialize() method.
	 * </p>
	 * <p>
	 * This method is <i>not</i> synchronized on the workspace, so the caller
	 * should be.
	 * </p>
	 * 
	 * @exception IllegalActionException
	 *                If the preinitialize() method of the container or one of
	 *                the deeply contained actors throws it, or the parameters,
	 *                minBinCount, binCountFactor, and isCQAdaptive, do not have
	 *                valid tokens.
	 */
	public void preinitialize() throws IllegalActionException {
		super.preinitialize(); // bad style, but has to be done because
								// otherwise the eventqueue that is not used is
								// not initialized and this causes problems in
								// wrapup

		_eventQueues = new Hashtable();
		for (Iterator it = ((CompositeActor) getContainer()).entityList()
				.iterator(); it.hasNext();) {
			_eventQueues.put(it.next(), new DECQEventQueue(
					((IntToken) minBinCount.getToken()).intValue(),
					((IntToken) binCountFactor.getToken()).intValue(),
					((BooleanToken) isCQAdaptive.getToken()).booleanValue()));
		}

		_physicalTime = ((Actor) getContainer()).getExecutiveDirector()
				.getModelTime();
		_actorsInExecution.clear();

		// Call the preinitialize method of the super class.
		if (_debugging) {
			_debug(getFullName(), "Preinitializing ...");
		}

		// First invoke initializable methods.
		if (_initializables != null) {
			for (Initializable initializable : _initializables) {
				initializable.preinitialize();
			}
		}

		// validate all settable attributes.
		Iterator attributes = attributeList(Settable.class).iterator();
		while (attributes.hasNext()) {
			Settable attribute = (Settable) attributes.next();
			attribute.validate();
		}
		// preinitialize protected variables.
		_currentTime = getModelStartTime();
		_stopRequested = false;
		// preinitialize all the contained actors.
		Nameable container = getContainer();
		if (container instanceof CompositeActor) {
			Iterator actors = ((CompositeActor) container).deepEntityList()
					.iterator();
			while (actors.hasNext()) {
				Actor actor = (Actor) actors.next();
				if (_debugging) {
					_debug("Invoking preinitialize(): ", ((NamedObj) actor)
							.getFullName());
				}
				actor.preinitialize();
			}
		}
		if (_debugging) {
			_debug(getFullName(), "Finished preinitialize().");
		}

		// Do this here because it updates the workspace version.
		if (-1 != workspace().getVersion()) {
			// Reset the hashtables for actor and port depths.
			// These two variables have to be reset here because the initialize
			// method constructs them.

			_computeActorDepth();
		}
	}

	/**
	 * the top level director (PtidesDirector) sets the clock synchronization
	 * error.
	 * 
	 * @param syncError
	 */
	public void setClockSyncError(double syncError) {
		_clockSyncError = syncError;
	}

	/**
	 * Set the deadline of an actor. This method is called after the execution
	 * of an actor is started.
	 * 
	 * @param actor
	 * @param deadline
	 * @throws IllegalActionException
	 */
	public void setDeadline(Actor actor, double deadline)
			throws IllegalActionException {
		if (_deadlinesOfActorsInExecution.get(actor) != null)
			_deadlinesOfActorsInExecution.remove(actor);
		_deadlinesOfActorsInExecution.put(actor, deadline);
	}

	/**
	 * The top-level director (PtidesDirector) sets the network delay.
	 * 
	 * @param delay
	 */
	public void setNetworkDelay(double delay) {
		_networkDelay = delay;
	}

	/**
	 * The top-level director (PtidesDirector) specifies if the ptides execution
	 * semantics should be used.
	 * 
	 * @param ptidesExecutionSemantics
	 */
	public void setUsePtidesExecutionSemantics(boolean ptidesExecutionSemantics) {
		_usePtidesExecutionSemantics = ptidesExecutionSemantics;
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
		_disabledActors = null;
		_eventQueues.clear();
		_currentModelTime = new Time(this, 0.0);
		_microstep = 0;
	}

	// /////////////////////////////////////////////////////////////////
	// // protected methods ////

	/**
	 * Put a pure event into the event queue for the given actor to fire at the
	 * specified timestamp. The depth for the queued event is the minimum of the
	 * depths of all the ports of the destination actor. If there is no event
	 * queue or the given actor is disabled, then this method does nothing.
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

		// Adjust the microstep.
		int microstep = 0;

		if (time.compareTo(getModelTime()) == 0) {
			// If during initialization, do not increase the microstep.
			// This is based on the assumption that an actor only requests
			// one firing during initialization. In fact, if an actor requests
			// several firings at the same time,
			// only the first request will be granted.
			if (_isInitializing) {
				microstep = _microstep;
			} else {
				microstep = _microstep + 1;
			}
		} else if (time.compareTo(getModelTime()) < 0) {
			throw new IllegalActionException(actor,
					"Attempt to queue an event in the past:"
							+ " Current time is " + getModelTime()
							+ " while event time is " + time);
		}

		int depth = _getDepthOfActor(actor);

		if (_debugging) {
			_debug("enqueue a pure event: ", ((NamedObj) actor).getName(),
					"time = " + time + " microstep = " + microstep
							+ " depth = " + depth);
		}

		DEEvent newEvent = new DEEvent(actor, time, microstep, depth);
		((DEEventQueue) _eventQueues.get(actor)).put(newEvent);
		_addSynchronizationPoint(newEvent.timeStamp());
	}

	/**
	 * Put a trigger event into the event queue.
	 * <p>
	 * The trigger event has the same timestamp as that of the director. The
	 * microstep of this event is always equal to the current microstep of this
	 * director. The depth for the queued event is the depth of the destination
	 * IO port.
	 * </p>
	 * <p>
	 * If the event queue is not ready or the actor contains the destination
	 * port is disabled, do nothing.
	 * </p>
	 * 
	 * @param ioPort
	 *            The destination IO port.
	 * @exception IllegalActionException
	 *                If the time argument is not the current time, or the depth
	 *                of the given IO port has not be calculated, or the new
	 *                event can not be enqueued.
	 */
	protected synchronized void _enqueueTriggerEvent(IOPort ioPort)
			throws IllegalActionException {
		Actor actor = (Actor) ioPort.getContainer();

		int depth = _getDepthOfIOPort(ioPort);

		if (_debugging) {
			_debug("enqueue a trigger event for ",
					((NamedObj) actor).getName(), " time = " + getModelTime()
							+ " microstep = " + _microstep + " depth = "
							+ depth);
		}

		DEEvent newEvent = new DEEvent(ioPort, getModelTime(), _microstep,
				depth);
		if (((DEEventQueue) _eventQueues.get(actor)) != null) {// TODO why is
																// compositeactor
																// scheduled
																// after
																// timeddelay=
		// ((DEEventQueue)_eventQueues.get(actor)).put(newEvent);
			_addSynchronizationPoint(newEvent.timeStamp());
		}
	}

	/**
	 * Put an event into the event queue for the given actor to fire at the
	 * specified timestamp. The depth for the queued event is the minimum of the
	 * depths of all the ports of the destination actor. If there is no event
	 * queue or the given actor is disabled, then this method does nothing.
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
	protected synchronized void _enqueueTriggerEvent(IOPort ioPort, Time time)
			throws IllegalActionException {
		Actor actor = (Actor) ioPort.getContainer();

		if ((getEventQueue() == null)
				|| ((_disabledActors != null) && _disabledActors
						.contains(actor))) {
			return;
		}

		int depth = _getDepthOfIOPort(ioPort);

		DEEvent newEvent = new DEEvent(ioPort, time, _microstep, depth);
		// getEventQueue().put(newEvent);
		// if (((DEEventQueue)_eventQueues.get(actor)) != null) // TODO why is
		// compositeactor scheduled after timeddelay=
		// ((DEEventQueue)_eventQueues.get(actor)).put(newEvent);
		_addSynchronizationPoint(newEvent.timeStamp());
	}

	/**
	 * Transfer input ports if they are safe to process. If they token is not
	 * safe to process, inform the executive director to be refired at the time
	 * it is safe to process the token.
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
					Receiver[][] recv = port.getReceivers();
					PtidesReceiver receiver = (PtidesReceiver) recv[0][0];
					Event event = receiver.getEvent();
					Time time = event._timeStamp;
					Token t = event._token;
					if (time.compareTo(_physicalTime) < 0)
						throw new IllegalActionException(
								"Network interface constraints violated at "
										+ this.getContainer().getName()
										+ ", tried to transfer event with timestamp "
										+ time + " at physical time "
										+ _physicalTime);
					if (!((isSafeToProcessOnNetwork(
							time, port)) || (time
							.compareTo(_physicalTime) > 0))) {
						_addSynchronizationPoint(time.subtract(
								PtidesGraphUtilities.getMinDelayTime(port))
								.add(_clockSyncError).add(_networkDelay)); // at
																			// this
																			// time,
																			// the
																			// new
																			// input
																			// should
																			// be
																			// read
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
									_physicalTime.getDoubleValue(),
									ScheduleListener.TRANSFERINPUT);
							if (_debugging)
								_debug("transfer input value " + t + " at " + time);
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
	 */
	protected boolean _transferOutputs(IOPort port)
			throws IllegalActionException {
		Token token = null;
		boolean result = false;

		if (!port.isOutput() || !port.isOpaque()) {
			throw new IllegalActionException(this, port,
					"Attempted to transferOutputs on a port that "
							+ "is not an opaque input port.");
		}
		for (int i = 0; i < port.getWidthInside(); i++) {
			try {
//				Receiver[][] receivers = port.getInsideReceivers();
//				if (receivers[i] != null) {
//					for (int j = 0; j < receivers[i].length; j++) {
//						if (receivers[i][j] != null
//								&& ((PtidesPlatformReceiver) receivers[i][j])
//										.getNextTime() != null
//								&& ((PtidesPlatformReceiver) receivers[i][j])
//										.getNextTime().compareTo(_physicalTime) < 0) {
//							throw new IllegalActionException(
//									"Network constraints violated, token with timestamp "
//											+ ((PtidesPlatformReceiver) receivers[i][j])
//													.getNextTime()
//											+ " cannot be transferred to the output at physical time "
//											+ _physicalTime);
//						}
//					}
//				}
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
									_physicalTime.getDoubleValue(),
									ScheduleListener.TRANSFEROUTPUT);
							if (_debugging)
								_debug("transfer output value " + token + " at rt: "
									+ _physicalTime + "/mt: " + getModelTime());
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
	 * Inform the executive director to be refired at the specified time.
	 */
	private void _addSynchronizationPoint(Time time)
			throws IllegalActionException {
		((Actor) getContainer()).getExecutiveDirector().fireAt(
				(Actor) this.getContainer(), time);
	}

	/**
	 * Put event back into the queue because it was not processed yet.
	 * 
	 * @param event
	 * @throws IllegalActionException
	 */
	private void _enqueueEventAgain(DEEvent event)
			throws IllegalActionException {
		if (event.ioPort() == null) { // then it is a pure event
			Actor actor = event.actor();
			((DEEventQueue) _eventQueues.get(actor)).put(event);
		}
	}

	/**
	 * return executive director which is the PtidesDirector.
	 * 
	 * @return
	 */
	private PtidesDirector _getExecutiveDirector() {
		Director dir = ((Actor) getContainer()).getExecutiveDirector();
		if (dir != null && dir instanceof PtidesDirector)
			return (PtidesDirector) dir;
		else
			return null;
	}

	/**
	 * determine if there is an event with timestamp - minDelay > timestamp
	 * of event at current node upstream. if so, the current event can be 
	 * processed. 
	 * 
	 * @param eventTimestamp
	 * @param node
	 * @param traversedEdges
	 * @return
	 * @throws IllegalActionException
	 */
	private boolean _isSafeToProcess(Time eventTimestamp, Node node, Set traversedEdges) throws IllegalActionException {
		if (_usePtidesExecutionSemantics) 
			return false;
		IOPort port = (IOPort) (node).getWeight();
		Collection inputs;
		inputs = graph.inputEdges(node);
		if (inputs.size() == 0) 
			return false;
		Iterator inputIt = inputs.iterator();
		while (inputIt.hasNext()) {
			Edge edge = (Edge) inputIt.next();
			Node nextNode = edge.source();
			Actor inputActor = (Actor) ((IOPort) nextNode.getWeight()).getContainer();
			if (!traversedEdges.contains(edge)) {
				traversedEdges.add(edge);
				if (inputActor instanceof TimedDelay) {
					TimedDelay delayActor = (TimedDelay) inputActor;
					double delay = ((DoubleToken) (delayActor.delay.getToken())).doubleValue();
					eventTimestamp = eventTimestamp.subtract(delay);
				}
				if (PtidesGraphUtilities.mustBeFiredAtRealTime(inputActor, null)) 
					return false; // didn't find earlier events
				else if (inputActor.equals(this.getContainer()))
					return false; // didn't find earlier events in platform
				for (Iterator it = inputActor.inputPortList().iterator(); it.hasNext();) {
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
			for (Iterator it = inputActor.inputPortList().iterator(); it.hasNext();) {
				return _isSafeToProcess(eventTimestamp, graph.node(it.next()), traversedEdges);
			}
		}
		return false; // should never come here
	}

	/**
	 * Get the set of events that are safe to fire. Those events contain pure
	 * events and triggered events.
	 * 
	 * @return
	 * @throws IllegalActionException
	 */
	private List _getNextEventsToFire() throws IllegalActionException {
		List events = new LinkedList();
		for (Enumeration actors = _eventQueues.keys(); actors.hasMoreElements();) {
			Actor actor = (Actor) actors.nextElement();
			DEEventQueue queue = (DEEventQueue) _eventQueues.get(actor);
			ArrayList taken = new ArrayList();

			// take pure events
			if (!queue.isEmpty()) {
				DEEvent event = queue.get();
				boolean isSafe = isSafeToProcessOnPlatform(event.timeStamp(),
						(NamedObj) actor);
				if (isSafe) {
					events.add(event);
					queue.take();
				}
			}
			// take trigger events
			if (!_actorsInExecution.contains(actor)) {
				for (Iterator it = actor.inputPortList().iterator(); it
						.hasNext();) {
					IOPort port = (IOPort) it.next();
					Receiver[][] receivers = port.getReceivers();
					for (int i = 0; i < receivers.length; i++) {
						Receiver[] recv = receivers[i];
						for (int j = 0; j < recv.length; j++) {
							PtidesPlatformReceiver receiver = (PtidesPlatformReceiver) recv[j];
							Time time = receiver.getNextTime();
							if (time != null
									&& (isSafeToProcessOnPlatform(time, port) || _isSafeToProcess(time, graph.node(port), new TreeSet()))) {
								_removePureEvent(events, actor, time);
								events.add(new DEEvent(port, time, 0,
										//_getDepthOfIOPort(port)));
										0));
							}
						}
					}
				}
			}
		}
		if (_debugging)
			_debug("events that are safe to fire: " + events.size()
				+ " " + events);
		return events;
	}

	/**
	 * if there is a pure event and a triggered event for the same actor with
	 * the same timestamp, then the pure event can be deleted
	 * 
	 * @param events
	 * @param actor
	 * @param time
	 */
	private void _removePureEvent(List events, Actor actor, Time time) {
		List<DEEvent> toRemove = new ArrayList<DEEvent>();
		for (int i = 0; i < events.size(); i++) {
			DEEvent event = (DEEvent) events.get(i);
			if (event.ioPort() == null && event.actor().equals(actor)
					&& event.timeStamp().equals(time))
				toRemove.add(event);
		}
		for (int i = 0; i < toRemove.size(); i++)
			events.remove(toRemove.get(i));
	}

	/**
	 * enqueue all remaining events because they were not processed.
	 * 
	 * @param list
	 * @throws IllegalActionException
	 */
	private void _enqueueRemainingEventsAgain(List list)
			throws IllegalActionException {
		if (list == null)
			return;
		for (int i = 0; i < list.size(); i++) {
			DEEvent event = (DEEvent) list.get(i);
			_enqueueEventAgain(event);
		}
	}

	/**
	 * initialize execution strategy parameter
	 * 
	 */
	private void _initialize() {
		try {
			executionStrategy = new StringParameter(this, "executionStrategy");
			executionStrategy
					.addChoice(PlatformExecutionStrategy.BASIC_NON_PREEMPTIVE);
			executionStrategy
					.setExpression(PlatformExecutionStrategy.BASIC_NON_PREEMPTIVE);
		} catch (KernelException e) {
			throw new InternalErrorException("Cannot set parameter:\n"
					+ e.getMessage());
		}
	}

	/**
	 * Set the new physical time from the executive director.
	 * 
	 * @param time
	 */
	private void _setPhysicalTime(Time time) {
		_physicalTime = time;
		_executionStrategy.setPhysicalTime(time);
	}

	/**
	 * transfer all input tokens
	 * 
	 * @return
	 * @throws IllegalActionException
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
	 * transfer all output tokens
	 * 
	 * @return
	 * @throws IllegalActionException
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
	 * list of actors in execution. In a non-preemptive execution, the list only
	 * contains one item.
	 */
	private List<Actor> _actorsInExecution = new ArrayList<Actor>();

	/**
	 * clock synchonization error specified in the top level director
	 */
	private double _clockSyncError;

	/**
	 * The current model time is adjusted when an actor is fired and set to the
	 * event timestamp of the event causing the firing.
	 */
	private Time _currentModelTime;

	/**
	 * Contains deadlines of actors in execution.
	 */
	private Hashtable _deadlinesOfActorsInExecution = new Hashtable();

	/**
	 * The set of actors that have returned false in their postfire() methods.
	 * Events destined for these actors are discarded and the actors are never
	 * fired.
	 */
	private Set _disabledActors;

	/**
	 * Contains an eventqueue for every actor.
	 */
	private Hashtable _eventQueues;

	/**
	 * Used execution strategy which is set according to a parameter.
	 */
	private PlatformExecutionStrategy _executionStrategy;

	/**
	 * A local boolean variable indicating whether this director is in
	 * initialization phase execution.
	 */
	private boolean _isInitializing = false;

	/**
	 * The current microstep.
	 */
	private int _microstep = 0;

	/**
	 * network delay time specified in the top-level director
	 */
	private double _networkDelay;

	/**
	 * Physical time that is managed by the top level director (PtidesDirector).
	 */
	private Time _physicalTime;

	/**
	 * determines if safeToProcess is checked
	 */
	private boolean _usePtidesExecutionSemantics;
	
	public DirectedGraph graph; 

}
