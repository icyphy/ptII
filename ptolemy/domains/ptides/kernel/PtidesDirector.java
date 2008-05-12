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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.process.CompositeProcessDirector;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.ProcessThread;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.lib.ScheduleListener;
import ptolemy.domains.ptides.lib.SchedulePlotter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * Top-level director for PTIDES models. The model time of this director is used
 * as the physical time of the whole model. Actors inside a PTIDES domain are
 * run in threads and ask for being refired at a certain time in the future.
 * This director saves those times in a list and if all actors are waiting for a
 * future time, the model time is increased.
 * 
 * @author Patricia Derler
 */
public class PtidesDirector extends CompositeProcessDirector implements
		TimedDirector {

	/**
	 * Construct a director in the default workspace with an empty string as its
	 * name. The director is added to the list of objects in the workspace.
	 * Increment the version number of the workspace.
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

	/**
	 * if this Parameter is set to true, minimum delays according to Ptides on
	 * the platform level are calculated. Otherwise, given minimum delays are
	 * used.
	 */
	public Parameter calculateMinDelays;

	/**
	 * global clock synchronization error
	 */
	public Parameter clockSyncError;

	/**
	 * global network delay - in future developments, network delays could be
	 * specified per network and not globally
	 */
	public Parameter networkDelay;

	/**
	 * defines if ptides execution strategy should be used. this is only
	 * interesting when other distributed event simulations should be tried with
	 * this framework.
	 */
	public Parameter usePtidesExecutionSemantics;

	/**
	 * time at which the simulation should be stopped
	 */
	public Parameter stopTime;

	/**
	 * Add a new schedule listener that will receive events in the
	 * _displaySchedule method
	 * 
	 * @param plotter
	 */
	public void addScheduleListener(SchedulePlotter plotter) {
		_scheduleListeners.add(plotter);
	}

	/**
	 * Override the base class to update local variables.
	 */
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {
		if (attribute == clockSyncError) {
			_clockSyncError = ((DoubleToken) clockSyncError.getToken())
					.doubleValue();
		} else if (attribute == networkDelay) {
			_networkDelay = ((DoubleToken) networkDelay.getToken())
					.doubleValue();
		} else if (attribute == usePtidesExecutionSemantics) {
			_usePtidesExecutionSemantics = ((BooleanToken) usePtidesExecutionSemantics
					.getToken()).booleanValue();
		} else if (attribute == calculateMinDelays) {
			_calculateMinDelays = ((BooleanToken) calculateMinDelays.getToken())
					.booleanValue();
		} else
			super.attributeChanged(attribute);
	}

	/**
	 * get physical time
	 */
	public synchronized Time getModelTime() {
		return _currentTime;
	}

	/**
	 * initialize parameters, calculate minimum delays for ports on platforms
	 * according to Ptides and initalize scheduleplotters
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();
		_completionTime = Time.POSITIVE_INFINITY;

		//_nextFirings = new TreeSet<Time>();
		_completionTime = new Time(this, ((DoubleToken) stopTime.getToken())
				.doubleValue());
		if (!_completionTime.equals(Time.POSITIVE_INFINITY))
			_nextFirings.add(_completionTime);

		if (_calculateMinDelays) {
			PtidesGraphUtilities utilities = new PtidesGraphUtilities(this
					.getContainer());
			utilities.calculateMinDelays();
			
		}

		Hashtable<Actor, List> table = new Hashtable<Actor, List>();
		for (Iterator it = ((CompositeActor) getContainer()).entityList()
				.iterator(); it.hasNext();) {
			Object obj = it.next();
			if (obj instanceof CompositeActor) {
				CompositeActor actor = (CompositeActor) obj;
				if (actor.getDirector() instanceof PtidesEmbeddedDirector) {
					PtidesEmbeddedDirector dir = (PtidesEmbeddedDirector) actor
							.getDirector();
					dir.setUsePtidesExecutionSemantics(_usePtidesExecutionSemantics);
					dir.setClockSyncError(_clockSyncError);
					dir.setNetworkDelay(_networkDelay);
				}
				List<Actor> actors = new ArrayList<Actor>();
				for (Iterator it2 = actor.entityList().iterator(); it2
						.hasNext();) {
					Object o = it2.next();
					if (o instanceof Actor)
						actors.add((Actor) o);
				}
				table.put(actor, actors);
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
	 * called by platforms to schedule a future time firing. This director does
	 * not remember which platform wants to be fired again. Performance can be
	 * improved here
	 */
	public void fireAt(Actor actor, Time time) throws IllegalActionException {
		if (time.compareTo(getModelTime()) > 0)
			_nextFirings.add(time);
	}

	/**
	 * return a new PtidesDEEReceiver
	 */
	public Receiver newReceiver() {
		PtidesReceiver receiver = new PtidesReceiver();
		double timeValue;

		try {
			timeValue = ((DoubleToken) stopTime.getToken()).doubleValue() + 1;
			receiver._setCompletionTime(new Time(this, timeValue));
			receiver._lastTime = new Time(this);
		} catch (IllegalActionException e) {
			// If the time resolution of the director or the stop
			// time is invalid, it should have been caught before this.
			throw new InternalErrorException(e);
		}

		return receiver;
	}

	/**
	 * wake up all waiting threads. The threads decide themselves if they have
	 * anything to do.
	 */
	public void notifyWaitingThreads() {
		try {
		Set set = (Set) _waitingPlatforms.clone();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Thread thread = (Thread) it.next();
			if (_debugging)
				_debug("unblock: " + thread.getName() + " ");
			threadUnblocked(thread, null);
		}
		_waitingPlatforms.clear();
		} catch (Exception ex) {
			// concurrent modification exceptions can occur here
		}
	}

	/**
	 * set physical time
	 */
	public synchronized void setModelTime(Time newTime)
			throws IllegalActionException {
		_currentTime = newTime;
	}

	/**
	 * If a platform has nothing to do at the current physical time, it waits
	 * for the next physical time which means that the platform thread is
	 * blocked. If all threads are about to be blocked, the physical time is
	 * increased to the next physical time any platform is interested in being
	 * fired again.
	 * 
	 * @return new physical time
	 * @throws IllegalActionException
	 */
	public synchronized Time waitForFuturePhysicalTime()
			throws IllegalActionException {
		if (_debugging)
			_debug("wait for "
					+ ((PtidesPlatformThread) Thread.currentThread())
							.getActor().getName()
					+ ", number of active threads: " + _getActiveThreadsCount()
					+ ", number of blocked threads: "
					+ _getBlockedThreadsCount()
					+ ", number of waiting threads: "
					+ _waitingPlatforms.size());
		if ((!_waitingPlatforms.contains(Thread.currentThread()) && _getActiveThreadsCount()
				- _waitingPlatforms.size() == 1)) {
			_increasePhysicalTime();
			return getModelTime();
		}
		if (_stopFireRequested)
			return getModelTime();
		_waitingPlatforms.add(Thread.currentThread());
		threadBlocked(Thread.currentThread(), null);
		try {
			workspace().wait(this);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!_stopRequested)
			_waitingPlatforms.remove(Thread.currentThread());
		return getModelTime();
	}

	/**
	 * clear list containing times platforms are interested in being fired in
	 * the future clear list of actors waiting for being refired reset physical
	 * time
	 */
	public void wrapup() throws IllegalActionException {
		super.wrapup();
		_waitingPlatforms.clear();
		//_nextFirings.clear();
		setModelTime(new Time(this, 0.0));
	}

	/**
	 * Forward display events from platforms to the schedule listeners.
	 * 
	 * @param node
	 *            platform that forwards the event.
	 * @param actor
	 *            actor inside a platform for which the event was created. If
	 *            the actor is null, the event is a platform event, e.g. input
	 *            ports read or output ports written.
	 * @param time
	 *            physical time at which the event occured.
	 * @param scheduleEvent
	 *            type of event.
	 */
	protected final void _displaySchedule(Actor node, Actor actor, double time,
			int scheduleEvent) {
		if (_scheduleListeners != null) {
			Iterator listeners = _scheduleListeners.iterator();

			while (listeners.hasNext()) {
				((ScheduleListener) listeners.next()).event(node, actor, time,
						scheduleEvent);
			}
		}
	}

	/**
	 * creates a new thread for a platform. A platform is a composite at the top
	 * level of the model.
	 * 
	 * @param actor
	 *            composite actor that represents a platform
	 * @param director
	 */
	protected ProcessThread _newProcessThread(Actor actor,
			ProcessDirector director) throws IllegalActionException {
		return new PtidesPlatformThread(actor, director);
	}

	/**
	 * Deadlocks can occur due to read or write accesses to the workspace. In
	 * case of a deadlock, wake up all threads. They will decide themselves if
	 * they have anything to do at current physical time.
	 */
	protected synchronized boolean _resolveDeadlock()
			throws IllegalActionException {
		if (_debugging) {
			_debug("resolveDeadlock");
		}
		notifyWaitingThreads();
		return true;
	}

	/**
	 * increase physical time to next time that any of the platforms is
	 * interested in doing something
	 * 
	 * @throws IllegalActionException
	 */
	private synchronized void _increasePhysicalTime()
			throws IllegalActionException {
		if (_nextFirings.size() == 0)
			return;
		Time time = (Time) _nextFirings.first();
		_nextFirings.remove(time);
		if (time.compareTo(_completionTime) > 0) {
			//stopFire();
			stop();
			return;
		}
		_currentTime = time;
		if (_debugging)
			_debug("physical time " + time + " set by "
					+ Thread.currentThread().getName());
		notifyWaitingThreads();
	}

	/**
	 * initialize parameters
	 * 
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 *             could occur if parameter with same name already exists
	 */
	private void _initialize() throws IllegalActionException,
			NameDuplicationException {
		double value = PrioritizedTimedQueue.ETERNITY;
		stopTime = new Parameter(this, "stopTime", new DoubleToken(value));
		timeResolution.setVisibility(Settable.FULL);

		try {
			clockSyncError = new Parameter(this, "clockSyncError");
			clockSyncError.setExpression("0.1");
			clockSyncError.setTypeEquals(BaseType.DOUBLE);

			networkDelay = new Parameter(this, "networkDelay");
			networkDelay.setExpression("0.1");
			networkDelay.setTypeEquals(BaseType.DOUBLE);

			usePtidesExecutionSemantics = new Parameter(this,
					"usePtidesExecutionSemantics");
			usePtidesExecutionSemantics.setExpression("true");
			usePtidesExecutionSemantics.setTypeEquals(BaseType.BOOLEAN);

			calculateMinDelays = new Parameter(this, "calculateMinDelays");
			calculateMinDelays.setExpression("true");
			calculateMinDelays.setTypeEquals(BaseType.BOOLEAN);
		} catch (KernelException e) {
			throw new InternalErrorException("Cannot set parameter:\n"
					+ e.getMessage());
		}
	}

	/**
	 * calcuate minimum delays or use specified minimum delays in the model
	 */
	private boolean _calculateMinDelays;

	/**
	 * global clock sychronization error
	 */
	private double _clockSyncError;

	/**
	 * The completion time. Since the completionTime is a constant, we do not
	 * convert it to a time object.
	 */
	private Time _completionTime;

	/**
	 * list of times that platforms want to be refired
	 */
	private TreeSet<Time> _nextFirings = new TreeSet<Time>();

	/**
	 * global network delay
	 */
	private double _networkDelay;

	/**
	 * registered schedule listeners, this is used for the schedule plotter
	 */
	private Collection<ScheduleListener> _scheduleListeners = new LinkedList<ScheduleListener>();

	/**
	 * if true, minimum delays according to ptides should be used
	 */
	private boolean _usePtidesExecutionSemantics;

	/**
	 * list of threads (=platforms) that have nothing to do at current time and
	 * want to be refired in the future.
	 */
	private HashSet<Thread> _waitingPlatforms = new HashSet<Thread>();

}
