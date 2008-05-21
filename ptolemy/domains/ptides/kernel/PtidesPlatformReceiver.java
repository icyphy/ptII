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

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

/**
 * Receiver used inside platforms of a ptides domain.
 * 
 * This Receiver will not work with non-opaque actors inside a platform.
 * 
 * @author Patricia Derler
 */
public class PtidesPlatformReceiver extends PrioritizedTimedQueue {

	/**
	 * Creates a new Ptides platform receiver.
	 */
	public PtidesPlatformReceiver() {
		super();
	}

	/**
	 * Construct an empty DEReceiver with the specified container.
	 * 
	 * @param container
	 *            The container.
	 * @exception IllegalActionException
	 *                If the container does not accept this receiver.
	 */
	public PtidesPlatformReceiver(IOPort container) throws IllegalActionException {
		super(container);
	}

	/**
	 * Return true if there is at least one token available to the get() method.
	 * 
	 * @return True if there are more tokens.
	 */
	public boolean hasToken() {
		return (getContainer().isOutput() && super.hasToken()) 
			|| (!(((Actor) ((IOPort) getContainer())
					.getContainer()).getDirector() instanceof PtidesEmbeddedDirector) && super.hasToken())			
			|| (hasToken(getModelTime())
				&& ((PtidesEmbeddedDirector) ((Actor) ((IOPort) getContainer())
						.getContainer()).getDirector())
						.isSafeToProcessOnPlatform(getModelTime(),
								getContainer()));
	}

	/**
	 * Put a token into this receiver.	
	 * @param token
	 *            The token to be put.
	 */
	public void put(Token token) {
		IOPort containerPort = getContainer();
        Actor containerActor = (Actor) containerPort.getContainer();
        Director dir;
        if (containerActor instanceof CompositeActor) {
        	dir = containerActor.getExecutiveDirector();
        } else {
        	dir = containerActor.getDirector();
        }
        Time modelTime = dir.getModelTime();
		put(token, modelTime);
	}

	/**
	 * Put a token into this receiver and post a trigger event to the director.
	 * The director will be responsible to dequeue the trigger event at the
	 * correct timestamp and microstep and invoke the corresponding actor whose
	 * input port contains this receiver. This receiver may contain more than
	 * one events.
	 * 
	 * @param token
	 *            The token to be put.
	 * @param time
	 * 			  The time stamp for the token.
	 */
	public void put(Token token, Time time) {
		try {
			PtidesEmbeddedDirector dir = _getDirector();
			dir._enqueueTriggerEvent(getContainer(), time);
			super.put(token, time);
		} catch (IllegalActionException ex) {
			throw new InternalErrorException(null, ex, null);
		}
	}

	/**
	 * Puts a token into all receivers.
	 * @param token The token to be put.
	 * @param receivers The receivers that get the token.
	 * @param time The time stamp for the token.
	 * @throws NoRoomException Thrown if the receiver is full.
	 * @throws IllegalActionException Thrown if container cannot convert token.
	 */
	public void putToAll(Token token, Receiver[] receivers, Time time)
			throws NoRoomException, IllegalActionException {
		for (int j = 0; j < receivers.length; j++) {
			IOPort container = receivers[j].getContainer();

			// If there is no container, then perform no
			// conversion.
			if (container != null) {
				((PtidesPlatformReceiver) receivers[j]).put(container.convert(token),
						time);
			}
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // private methods ////

	/**
	 * Return the director that created this receiver. If this receiver is an
	 * inside receiver of an output port of an opaque composite actor, then the
	 * director will be the local director of the container of its port.
	 * Otherwise, it's the executive director of the container of its port.Note
	 * that the director returned is guaranteed to be non-null. This method is
	 * read synchronized on the workspace.
	 * 
	 * @return An instance of DEDirector.
	 * @exception IllegalActionException
	 *                If there is no container port, or if the port has no
	 *                container actor, or if the actor has no director, or if
	 *                the director is not an instance of DEDirector.
	 */
	private PtidesEmbeddedDirector _getDirector() throws IllegalActionException {
		IOPort port = getContainer();

		if (port != null) {
			if (_directorVersion == port.workspace().getVersion()) {
				return _director;
			}

			// Cache is invalid. Reconstruct it.
			try {
				port.workspace().getReadAccess();
				Actor actor = (Actor) port.getContainer();
				if (actor != null) {
					Director dir;

					if (!port.isInput() && (actor instanceof CompositeActor)
							&& ((CompositeActor) actor).isOpaque()) {
						dir = actor.getDirector();
					} else {
						dir = actor.getExecutiveDirector();
					}

					if (dir != null) {
						if (dir instanceof PtidesEmbeddedDirector) {
							_director = (PtidesEmbeddedDirector) dir;
							_directorVersion = port.workspace().getVersion();
							return _director;
						} else {
							throw new IllegalActionException(getContainer(),
									"Does not have a EmbeddedDEDirector4Ptides.");
						}
					}
				}
			} finally {
				port.workspace().doneReading();
			}
		}

		throw new IllegalActionException(getContainer(),
				"Does not have a IOPort as the container of the receiver.");
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////
	/** The director where this DEReceiver should register for De events.  */
	private PtidesEmbeddedDirector _director;

	/**
	 * version of the directorl.
	 */
	private long _directorVersion = -1;

	/**
	 * Returns time stamp of next event in the receiver queue.
	 * @return The time stamp.
	 */
	public Time getNextTime() {
		if (_queue.isEmpty())
			return null;
		Time time = ((Event) _queue.first())._timeStamp;
		return time;
	}

}
