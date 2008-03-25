package ptolemy.domains.ptides.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.process.BoundaryDetector;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DDEReceiver

/**
 * Receiver used on the top level in the ptides domain
 * 
 * @author Patricia Derler
 */
public class PtidesReceiver extends PrioritizedTimedQueue implements
		ProcessReceiver {
	/**
	 * Construct an empty receiver with no container.
	 */
	public PtidesReceiver() {
		super();
		_boundaryDetector = new BoundaryDetector(this);
	}

	/**
	 * Construct an empty receiver with the specified container.
	 * 
	 * @param container
	 *            The IOPort that contains this receiver.
	 * @exception IllegalActionException
	 *                If this receiver cannot be contained by the proposed
	 *                container.
	 */
	public PtidesReceiver(IOPort container) throws IllegalActionException {
		super(container);
		_boundaryDetector = new BoundaryDetector(this);
	}

	/**
	 * Construct an empty receiver with the specified IOPort container and
	 * priority.
	 * 
	 * @param container
	 *            The IOPort that contains this receiver.
	 * @param priority
	 *            The priority of this receiver.
	 * @exception IllegalActionException
	 *                If this receiver cannot be contained by the proposed
	 *                container.
	 */
	public PtidesReceiver(IOPort container, int priority)
			throws IllegalActionException {
		super(container, priority);
		_boundaryDetector = new BoundaryDetector(this);
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Clear this receiver of any contained tokens.
	 */
	public void clear() {
		// FIXME
		// queue.clear();
	}

	public Token get() {
		if (!super.hasToken()) {
			throw new NoTokenException(getContainer(),
					"Attempt to get token that does not have "
							+ "have the earliest time stamp.");
		}
		synchronized (_director) {
			if (_terminate) {
				throw new TerminateProcessException("");
			}
			Token token = super.get();
			Thread thread = Thread.currentThread();
			return token;
		}
	}

	public PtidesDirector getDirector() {
		return _director;
	}

	/**
	 * Return true if the receiver contains the given number of tokens that can
	 * be obtained by calling the get() method. Returning true in this method
	 * should also guarantee that calling the get() method will not result in an
	 * exception.
	 */
	public boolean hasToken(int tokens) {
		return super.hasToken(tokens);

		// FIXME This is wrong!
	}

	/**
	 * Return true if this receiver is connected to the inside of a boundary
	 * port. A boundary port is an opaque port that is contained by a composite
	 * actor. If this receiver is connected to the inside of a boundary port,
	 * then return true; otherwise return false.
	 * 
	 * @return True if this receiver is contained on the inside of a boundary
	 *         port; return false otherwise.
	 * @see ptolemy.actor.process.BoundaryDetector
	 */
	public boolean isConnectedToBoundary() {
		return _boundaryDetector.isConnectedToBoundary();
	}

	/**
	 * Return true if this receiver is connected to the inside of a boundary
	 * port. A boundary port is an opaque port that is contained by a composite
	 * actor. If this receiver is connected to the inside of a boundary port,
	 * then return true; otherwise return false.
	 * 
	 * @return True if this receiver is connected to the inside of a boundary
	 *         port; return false otherwise.
	 * @see ptolemy.actor.process.BoundaryDetector
	 */
	public boolean isConnectedToBoundaryInside() {
		return _boundaryDetector.isConnectedToBoundaryInside();
	}

	/**
	 * Return true if this receiver is connected to the outside of a boundary
	 * port. A boundary port is an opaque port that is contained by a composite
	 * actor. If this receiver is connected to the outside of a boundary port,
	 * then return true; otherwise return false.
	 * 
	 * @return True if this receiver is connected to the outside of a boundary
	 *         port; return false otherwise.
	 * @see ptolemy.actor.process.BoundaryDetector
	 */
	public boolean isConnectedToBoundaryOutside() {
		return _boundaryDetector.isConnectedToBoundaryOutside();
	}

	/**
	 * Return true if this receiver is a consumer receiver. A receiver is a
	 * consumer receiver if it is connected to a boundary port.
	 * 
	 * @return True if this is a consumer receiver; return false otherwise.
	 */
	public boolean isConsumerReceiver() {
		if (isConnectedToBoundary()) {
			return true;
		}
		return false;
	}

	/**
	 * Return true if this receiver is contained on the inside of a boundary
	 * port. A boundary port is an opaque port that is contained by a composite
	 * actor. If this receiver is contained on the inside of a boundary port
	 * then return true; otherwise return false.
	 * 
	 * @return True if this receiver is contained on the inside of a boundary
	 *         port; return false otherwise.
	 * @see ptolemy.actor.process.BoundaryDetector
	 */
	public boolean isInsideBoundary() {
		return _boundaryDetector.isInsideBoundary();
	}

	/**
	 * Return true if this receiver is contained on the outside of a boundary
	 * port. A boundary port is an opaque port that is contained by a composite
	 * actor. If this receiver is contained on the outside of a boundary port
	 * then return true; otherwise return false.
	 * 
	 * @return True if this receiver is contained on the outside of a boundary
	 *         port; return false otherwise.
	 * @see BoundaryDetector
	 */
	public boolean isOutsideBoundary() {
		return _boundaryDetector.isOutsideBoundary();
	}

	/**
	 * Return true if this receiver is a producer receiver. A receiver is a
	 * producer receiver if it is contained on the inside or outside of a
	 * boundary port.
	 * 
	 * @return True if this is a producer receiver; return false otherwise.
	 */
	public boolean isProducerReceiver() {
		if (isOutsideBoundary() || isInsideBoundary()) {
			return true;
		}
		return false;
	}

	/**
	 * Return a true or false to indicate whether there is a read block on this
	 * receiver or not, respectively.
	 * 
	 * @return a boolean indicating whether a read is blocked on this receiver
	 *         or not.
	 */
	public boolean isReadBlocked() {
		return false;
	}

	/**
	 * Return a true or false to indicate whether there is a write block on this
	 * receiver or not.
	 * 
	 * @return A boolean indicating whether a write is blocked on this receiver
	 *         or not.
	 */
	public boolean isWriteBlocked() {
		return false;
	}

	/**
	 * Do a blocking write on the queue. Set the time stamp to be the current
	 * time of the sending actor. If the current time is greater than the
	 * completionTime of this receiver, then set the time stamp to INACTIVE and
	 * the token to null. If the queue is full, then inform the director that
	 * this receiver is blocking on a write and wait until room becomes
	 * available. When room becomes available, put the token and time stamp in
	 * the queue and inform the director that the block no longer exists. If at
	 * any point during this method this receiver is scheduled for termination,
	 * then throw a TerminateProcessException which will cease activity for the
	 * actor that contains this receiver.
	 * 
	 * @param token
	 *            The token to put in the queue.
	 * @exception TerminateProcessException
	 *                If activity is scheduled to cease.
	 */
	public void put(Token token) {
		Thread thread = Thread.currentThread();
		Time time = _lastTime;

		if (thread instanceof PtidesPlatformThread) {
			time = ((PtidesPlatformThread) thread).getActor().getDirector()
					.getModelTime();
		}

		put(token, time);
	}

	/**
	 * Do a blocking write on the queue. Set the time stamp to be the time
	 * specified by the time parameter. If the specified time is greater than
	 * the completionTime of this receiver, then set the time stamp to INACTIVE
	 * and the token to null. If the queue is full, then inform the director
	 * that this receiver is blocking on a write and wait until room becomes
	 * available. When room becomes available, put the token and time stamp in
	 * the queue and inform the director that the block no longer exists. If at
	 * any point during this method this receiver is scheduled for termination,
	 * then throw a TerminateProcessException which will cease activity for the
	 * actor that contains this receiver.
	 * 
	 * @param token
	 *            The token to put in the queue.
	 * @param time
	 *            The specified time stamp.
	 * @exception TerminateProcessException
	 *                If activity is scheduled to cease.
	 */
	public void put(Token token, Time time) {
		Workspace workspace = getContainer().workspace();
		if (super.hasRoom() && !_terminate) { // super will always have room
												// for now
			super.put(token, time);
			this._director.notifyWaitingThreads();
			return;
		}

	}

	/**
	 * Schedule this receiver to terminate. After this method is called, a
	 * TerminateProcessException will be thrown during the next call to get() or
	 * put() of this class.
	 */
	public void requestFinish() {
		synchronized (_director) {
			_terminate = true;
			_director.notifyAll();
		}
	}

	/**
	 * Reset local flags. The local flag of this receiver indicates whether this
	 * receiver is scheduled for termination. Resetting the termination flag
	 * will make sure that this receiver is not scheduled for termination.
	 */
	public void reset() {
		super.reset();
		_terminate = false;
		_boundaryDetector.reset();
	}

	/**
	 * Set the container. This overrides the base class to record the director.
	 * 
	 * @param port
	 *            The container.
	 * @exception IllegalActionException
	 *                If the container is not of an appropriate subclass of
	 *                IOPort, or if the container's director is not an instance
	 *                of DDEDirector.
	 */
	public void setContainer(IOPort port) throws IllegalActionException {
		super.setContainer(port);

		if (port == null) {
			_director = null;
		} else {
			Actor actor = (Actor) port.getContainer();
			Director director;

			// For a composite actor,
			// the receiver type of an input port is decided by
			// the executive director.
			// While the receiver type of an output is decided by the director.
			// NOTE: getExecutiveDirector() and getDirector() yield the same
			// result for actors that do not contain directors.
			if (port.isInput()) {
				director = actor.getExecutiveDirector();
			} else {
				director = actor.getDirector();
			}

			if (!(director instanceof PtidesDirector)) {
				throw new IllegalActionException(port,
						"Cannot use an instance of PNQueueReceiver "
								+ "since the director is not a PNDirector.");
			}

			_director = (PtidesDirector) director;
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/** The boundary detector. */
	private BoundaryDetector _boundaryDetector;

	/** The director in charge of this receiver. */
	private PtidesDirector _director;

	/** Reference to a thread that is read blocked on this receiver. */
	private Thread _readtPending = null;

	/** Flag indicating that termination has been requested. */
	private boolean _terminate = false;

	/** Reference to a thread that is write blocked on this receiver. */
	private Thread _writetPending = null;
}
