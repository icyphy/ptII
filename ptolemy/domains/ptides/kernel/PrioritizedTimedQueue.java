package ptolemy.domains.ptides.kernel;

import java.util.Comparator;
import java.util.TreeSet;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.util.CQComparator;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DECQEventQueue;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// PrioritizedTimedQueue

/**
 * @author Patricia Derler
 */
public class PrioritizedTimedQueue extends AbstractReceiver {
	/**
	 * Construct an empty queue with no container.
	 */
	public PrioritizedTimedQueue() {
		// because the container is not specified, we can not
		// call the _initializeTimeVariables() method.
		// The caller of this constructor is responsible to
		// initialize the time variables.
	}

	/**
	 * Construct an empty queue with the specified IOPort container.
	 * 
	 * @param container
	 *            The IOPort that contains this receiver.
	 * @exception IllegalActionException
	 *                If this receiver cannot be contained by the proposed
	 *                container.
	 */
	public PrioritizedTimedQueue(IOPort container)
			throws IllegalActionException {
		super(container);
		_initializeTimeVariables();
	}

	/**
	 * Construct an empty queue with the specified IOPort container and
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
	public PrioritizedTimedQueue(IOPort container, int priority)
			throws IllegalActionException {
		super(container);
		_priority = priority;
		_initializeTimeVariables();
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Take the the oldest token off of the queue and return it. If the queue is
	 * empty, throw a NoTokenException. If there are other tokens left on the
	 * queue after this removal, then set the receiver time of this receiver to
	 * equal that of the next oldest token. Update the TimeKeeper that manages
	 * this PrioritizedTimedQueue. If there are any receivers in the TimeKeeper
	 * with receiver times of PrioritizedTimedQueue.IGNORE, remove the first
	 * token from these receivers.
	 * 
	 * @return The oldest token off of the queue.
	 * @exception NoTokenException
	 *                If the queue is empty.
	 */
	public Token get() {
		// Get a token and set all relevant
		// local time parameters
		Token token = null;
		Event event = (Event) _queue.first();
		_queue.remove(event);
		_lastTime = event._timeStamp;
		token = event.getToken();
		return token;
	}

	/**
	 * Similar to get() but if not only token ut also timestamp is required,
	 * this method is used.
	 * 
	 * @return Event containing token and timestamp.
	 */
	public Event getEvent() {
		// Get a token and set all relevant
		// local time parameters
		Event event = (Event) _queue.first();
		_queue.remove(event);
		_lastTime = event._timeStamp;
		return event;
	}

	/**
	 * Get the queue capacity of this receiver.
	 * 
	 * @return The capacity of this receiver's queue.
	 * @see #setCapacity(int)
	 */
	public int getCapacity() {
		return _queue.size();
	}

	/**
	 * Return true if the number of tokens stored in the queue is less than the
	 * capacity of the queue. Return false otherwise.
	 * 
	 * @return True if the queue is not full; return false otherwise.
	 */
	public boolean hasRoom() {
		return true;
		// implement bounds on ressources here
	}

	/**
	 * Return true if the queue capacity minus the queue size is greater than
	 * the argument.
	 * 
	 * @param numberOfTokens
	 *            The number of tokens to put into the queue.
	 * @return True if the queue is not full; return false otherwise.
	 * @exception IllegalArgumentException
	 *                If the argument is not positive. This is a runtime
	 *                exception, so it does not need to be declared explicitly.
	 */
	public boolean hasRoom(int numberOfTokens) throws IllegalArgumentException {
		return true;
		// implement bounds on ressources here
	}

	/**
	 * Return true if there are tokens stored on the queue. Return false if the
	 * queue is empty.
	 * 
	 * @return True if the queue is not empty; return false otherwise.
	 */
	public boolean hasToken() {
		return _queue.size() > 0;
	}

	/**
	 * returns true if there is a token with the specified timestamp
	 * 
	 * @param time
	 * @return
	 */
	public boolean hasToken(Time time) {
		if (_queue.size() == 0)
			return false;
		return (((Event) _queue.first())._timeStamp.equals(time));
	}

	/**
	 * Return true if queue size is at least the argument.
	 * 
	 * @param numberOfTokens
	 *            The number of tokens to get from the queue.
	 * @return True if the queue has enough tokens.
	 * @exception IllegalArgumentException
	 *                If the argument is not positive. This is a runtime
	 *                exception, so it does not need to be declared explicitly.
	 */
	public boolean hasToken(int numberOfTokens) throws IllegalArgumentException {
		if (numberOfTokens < 1) {
			throw new IllegalArgumentException(
					"hasToken() requires a positive argument.");
		}

		return (_queue.size() > numberOfTokens);
	}

	/**
	 * Throw an exception, since this method is not used in DDE.
	 * 
	 * @param token
	 *            The token to be put to the receiver.
	 * @exception NoRoomException
	 *                If the receiver is full.
	 */
	public void put(Token token) {
		throw new NoRoomException("put(Token) is not used in the "
				+ "DDE domain.");
	}

	/**
	 * Put a token on the queue with the specified time stamp and set the last
	 * time value to be equal to this time stamp. If the queue is empty
	 * immediately prior to putting the token on the queue, then set the
	 * receiver time value to be equal to the last time value. If the queue is
	 * full, throw a NoRoomException. Time stamps can not be set to negative
	 * values that are not equal to IGNORE or INACTIVE; otherwise an
	 * IllegalArgumentException will be thrown.
	 * 
	 * @param token
	 *            The token to put on the queue.
	 * @param time
	 *            The time stamp of the token.
	 * @exception NoRoomException
	 *                If the queue is full.
	 */
	public void put(Token token, Time time) throws NoRoomException {
		double timeValue = time.getDoubleValue();

		Event event = new Event(token, time);
		_lastTime = time;

		try {
			if (!_queue.add(event)) {
				// same event already exists
			}
		} catch (NoRoomException e) {

			throw e;
		}
	}

	/**
	 * Reset local flags. The local flags of this receiver impact the local
	 * notion of time of the actor that contains this receiver. This method is
	 * not synchronized so the caller should be.
	 */
	public void reset() {
		Director director = ((Actor) getContainer().getContainer()
				.getContainer()).getDirector();
		Time time;
		try {
			time = new Time(director, 0.0);
			_lastTime = time;
		} catch (IllegalActionException e) {

			e.printStackTrace();
		}

	}

	/**
	 * Set the queue capacity of this receiver.
	 * 
	 * @param capacity
	 *            The capacity of this receiver's queue.
	 * @exception IllegalActionException
	 *                If the superclass throws it.
	 * @see #getCapacity()
	 */
	public void setCapacity(int capacity) throws IllegalActionException {
		// TODO
	}

	// /////////////////////////////////////////////////////////////////
	// // package friendly variables ////
	// This time value is used in conjunction with completionTime to
	// indicate that a receiver will continue operating indefinitely.
	static final double ETERNITY = -5.0;

	// This time value indicates that the receiver contents should
	// be ignored.
	static final double IGNORE = -1.0;

	// This time value indicates that the receiver is no longer
	// active.
	static final double INACTIVE = -2.0;

	// The time stamp of the newest token to be placed in the queue.
	protected Time _lastTime;

	// The priority of this receiver.
	public int _priority = 0;

	// /////////////////////////////////////////////////////////////////
	// // package friendly methods ////

	/**
	 * Return the completion time of this receiver. This method is not
	 * synchronized so the caller should be.
	 * 
	 * @return The completion time.
	 */
	Time _getCompletionTime() {
		return _completionTime;
	}

	/**
	 * Return true if this receiver has a NullToken at the front of the queue;
	 * return false otherwise. This method is not synchronized so the caller
	 * should be.
	 * 
	 * @return True if this receiver contains a NullToken in the oldest queue
	 *         position; return false otherwise.
	 */
	boolean _hasNullToken() {

		return false;
	}

	/**
	 * Set the completion time of this receiver. If the completion time argument
	 * is negative but is not equal to PrioritizedTimedQueue.ETERNITY, then
	 * throw an IllegalArgumentException. This method is not synchronized so the
	 * caller should be.
	 * 
	 * @param time
	 *            The completion time of this receiver.
	 */
	public void _setCompletionTime(Time time) {
		double timeValue = time.getDoubleValue();

		if ((timeValue < 0.0) && (timeValue != PrioritizedTimedQueue.ETERNITY)) {
			throw new IllegalArgumentException("Attempt to set "
					+ "completion time to a negative value.");
		}

		_completionTime = time;
	}

	/**
	 * Initialize some time variables.
	 */
	private void _initializeTimeVariables() {
		Actor actor = (Actor) getContainer().getContainer();

		try {
			_completionTime = new Time(actor.getDirector(), ETERNITY);
			_lastTime = new Time(actor.getDirector());
			// _receiverTime = new Time(actor.getDirector());
		} catch (IllegalActionException e) {
			// If the time resolution of the director is invalid,
			// it should have been caught before this.
			throw new InternalErrorException(e);
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////
	// The time after which execution of this receiver will cease.
	// Initially the value is set so that execution will continue
	// indefinitely.
	private Time _completionTime;

	// The queue in which this receiver stores tokens.
	protected TreeSet _queue = new TreeSet(new TimeComparator());

	// The time stamp of the earliest token that is still in the queue.
	// private Time _receiverTime;

	// /////////////////////////////////////////////////////////////////
	// // inner class ////
	// An Event is an aggregation consisting of a Token, a
	// time stamp and destination Receiver. Both the token
	// and destination receiver are allowed to have null
	// values. This is particularly useful in situations
	// where the specification of the destination receiver
	// may be considered redundant.
	public static class Event {
		// Construct an Event with a token and time stamp.
		public Event(Token token, Time time) {
			_token = token;
			_timeStamp = time;
		}

		// /////////////////////////////////////////////////////////
		// // public inner methods ////

		// Return the time stamp of this event.
		public Time getTime() {
			return _timeStamp;
		}

		// Return the token of this event.
		public Token getToken() {
			return _token;
		}

		// /////////////////////////////////////////////////////////
		// // private inner variables ////
		Time _timeStamp;

		Token _token = null;
	}

	/**
	 * Compare two events according to - timestamp - value did not find a way to
	 * compare Tokens, therefore am comparing DoubleTokens and IntTokens here.
	 * If other kinds of Tokens are used, this Comparer needs to be extended.
	 * 
	 * @author Patricia Derler
	 * 
	 */
	public class TimeComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			Event event1 = (Event) arg0;
			Event event2 = (Event) arg1;
			Time time1 = event1._timeStamp;
			Time time2 = event2._timeStamp;
			if (time1.compareTo(time2) != 0)
				return time1.compareTo(time2);

			if (event1._token instanceof DoubleToken) {
				DoubleToken token1 = (DoubleToken) event1._token;
				DoubleToken token2 = (DoubleToken) event2._token;
				if (token1.doubleValue() < token2.doubleValue())
					return -1;
				else if (token1.doubleValue() > token2.doubleValue())
					return 1;
				else
					return 0;
			} else if (event1._token instanceof IntToken) {
				IntToken token1 = (IntToken) event1._token;
				IntToken token2 = (IntToken) event2._token;
				if (token1.intValue() < token2.intValue())
					return -1;
				else if (token1.intValue() > token2.intValue())
					return 1;
				else
					return 0;
			}
			return 0;

		}

	}
}
