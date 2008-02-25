package ptolemy.domains.ptides.kernel;

import java.util.Hashtable;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.ProcessThread;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.actor.util.Time;
import ptolemy.domains.dde.kernel.DDEDirector;
import ptolemy.domains.dde.kernel.DDEReceiver;
import ptolemy.domains.dde.kernel.DDEThread;
import ptolemy.domains.dde.kernel.PrioritizedTimedQueue;
import ptolemy.domains.dde.kernel.TimeKeeper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

public class DDEThread4Ptides extends ProcessThread {

	public DDEThread4Ptides(Actor actor, ProcessDirector director)
			throws IllegalActionException {
		super(actor, director);
		_timeKeeper = new TimeKeeper4Ptides(actor);
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Return the time keeper that keeps time for the actor that this thread
	 * controls.
	 * 
	 * @return The TimeKeeper of the actor that this thread controls.
	 */
	public TimeKeeper4Ptides getTimeKeeper() {
		return _timeKeeper;
	}

	/**
	 * Notify output-connected actors that the actor controlled by this thread
	 * is ending execution. <I>Output-connected actors</I> are those that are
	 * connected to the actor controlled by this thread via output ports of this
	 * thread's actor. Send events with time stamps of
	 * PrioritizedTimedQueue.INACTIVE to these "downstream" actors.
	 * 
	 * @see ptolemy.domains.dde.kernel.PrioritizedTimedQueue
	 */
	public synchronized void noticeOfTermination() {
		Actor actor = getActor();
		Iterator outputPorts = actor.outputPortList().iterator();

		if (outputPorts != null) {
			while (outputPorts.hasNext()) {
				IOPort port = (IOPort) outputPorts.next();
				Receiver[][] receivers = port.getRemoteReceivers();

				if (receivers == null) {
					break;
				}
			}
		}
	}

	/**
	 * Start this thread and initialize the time keeper to a future time if
	 * specified in the director's initial time table. Use this method to
	 * facilitate any calls to DDEDirector.fireAt() that occur prior to the
	 * creation of this thread. If fireAt() was called for time 'T' with respect
	 * to the actor that this thread controls, then set the current time of this
	 * threads TimeKeeper to time 'T.'
	 * <P>
	 * NOTE: This method assumes an implementation of fireAt() that would be
	 * more appropriately named <I>continueAt()</I>.
	 */
	public void start() {
		Actor actor = getActor();
		DEDirector4Ptides director = (DEDirector4Ptides) actor.getExecutiveDirector();
		super.start();
	}
	
	

	/**
	 * End the execution of the actor under the control of this thread. Notify
	 * all actors connected to this actor that this actor is preparing to cease
	 * execution.
	 * 
	 * @exception IllegalActionException
	 *                If an error occurs while ending execution of the actor
	 *                under the control of this thread.
	 */
	public void wrapup() throws IllegalActionException {
		noticeOfTermination();
		super.wrapup();
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////
	private TimeKeeper4Ptides _timeKeeper = null;
}
