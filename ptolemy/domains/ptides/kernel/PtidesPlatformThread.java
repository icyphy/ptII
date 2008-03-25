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

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.ProcessThread;
import ptolemy.kernel.util.IllegalActionException;

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
