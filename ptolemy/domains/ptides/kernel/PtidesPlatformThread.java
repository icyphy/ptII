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

/**
 * This thread is created for a CompositeActor that represents a platform inside
 * a PTIDES domain. The PtidesDirector creates this thread.
 * 
 * @author Patricia Derler
 */
public class PtidesPlatformThread extends ProcessThread {

	/**
	 * Construct a thread to be used for the execution of the iteration methods
	 * of the actor. This increases the count of active actors in the director.
	 * 
	 * @param actor
	 *            The actor that needs to be executed.
	 * @param director
	 *            The director responsible for the execution of this actor.
	 */
	public PtidesPlatformThread(Actor actor, ProcessDirector director)
			throws IllegalActionException {
		super(actor, director);
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

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

}
