/*
@Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.apps.ptides.kernel;

import java.util.Comparator;
import java.util.TreeSet;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Receivers in the Ptides domain use a timed queue to sort events in
 * the receivers.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtidesReceiver extends AbstractReceiver {

    /**
     * Construct an empty queue with no container.
     */
    public PtidesReceiver() {
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
    public PtidesReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    @Override
    public void clear() throws IllegalActionException {
        _queue.clear();
    }

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
        Token token;
        Event event = _queue.first();
        _queue.remove(event);
        token = event.getToken();
        return token;
    }

    /**
     * Returns time stamp of next event in the receiver queue.
     *
     * @return The time stamp.
     */
    public Time getNextTime() {
        if (_queue.isEmpty()) {
            return null;
        }
        Time time = (_queue.first())._timeStamp;
        return time;
    }

    /**
     * Get the queue capacity of this receiver.
     *
     * @return The capacity of this receiver's queue.
     */
    public int getCapacity() {
        return _queue.size();
    }

    /**
     * Similar to get() but if not only token but also time stamp is required,
     * this method is used.
     *
     * @return Event containing token and time stamp.
     */
    public Event getEvent() {
        Event event = _queue.first();
        _queue.remove(event);
        return event;
    }

    /**
     * This is an unbounded receiver thus always returns true.
     *
     * @return true because this is an unbounded receiver.
     */
    public boolean hasRoom() {
        return true;
    }

    /**
     * This is an unbounded receiver thus always returns true.
     *
     * @param numberOfTokens
     *            Number of tokens to be stored.
     * @return true because this is an unbounded receiver.
     */
    public boolean hasRoom(int numberOfTokens) {
        return true;
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
     * Returns true if there is a token with the specified time stamp.
     *
     * @param time
     *            Time for which a token is required.
     * @return True if the first element in the queue has the specified time
     *         stamp.
     */
    public boolean hasToken(Time time) {
        if (_queue.size() == 0) {
            return false;
        }
        return ((_queue.first())._timeStamp.compareTo(time) <= 0);
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
     * Throw an exception, since this method is not used in Ptides.
     *
     * @param token
     *            The token to be put to the receiver, or null to put no token.
     * @exception NoRoomException
     *                If the receiver is full.
     */
    public void put(Token token) {
        if (token == null) {
            return;
        }
        throw new NoRoomException("put(Token) is not used in the "
                + "Ptides domain.");
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
     *            The token to put on the queue, or null to put no token.
     * @param time
     *            The time stamp of the token.
     * @exception NoRoomException
     *                If the queue is full.
     */
    public void put(Token token, Time time) throws NoRoomException {
        if (token == null) {
            return;
        }
        Event event = new Event(token, time);
        _queue.add(event); // is only inserted if same event not already
        // exists
    }

    ///////////////////////////////////////////////////////////////////
    //// protected variables ////

    /** The set in which this receiver stores tokens. */
    protected TreeSet<Event> _queue = new TreeSet<Event>(new TimeComparator());

    /**
     * An Event is an aggregation consisting of a Token, a time stamp and
     * destination Receiver. Both the token and destination receiver are allowed
     * to have null values. This is particularly useful in situations where the
     * specification of the destination receiver may be considered redundant.
     */
    public static class Event {

        /**
         * Construct an Event with a token and time stamp.
         *
         * @param token
         *            Token for the event.
         * @param time
         *            Time stamp of the event.
         */
        public Event(Token token, Time time) {
            _token = token;
            _timeStamp = time;
        }

        // /////////////////////////////////////////////////////////
        // // public inner methods ////

        /**
         * Return the time stamp of this event.
         *
         * @return The time stamp of the event.
         */
        public Time getTime() {
            return _timeStamp;
        }

        /**
         * Return the token of this event.
         *
         * @return The token of the event.
         */
        public Token getToken() {
            return _token;
        }

        // /////////////////////////////////////////////////////////
        // // private inner variables ////
        /** Time stamp of this event. */
        Time _timeStamp;

        /** Token of this event. */
        Token _token = null;
    }

    /**
     * Compare two events according to - time stamp - value did not find a way
     * to compare Tokens, therefore am comparing DoubleTokens and IntTokens
     * here. If other kinds of Tokens are used, this Comparer needs to be
     * extended.
     *
     * @author Patricia Derler
     *
     */
    public static class TimeComparator implements Comparator {

        /**
         * Compare two events according to time stamps and values.
         *
         * FIXME Because there is no general compare method for tokens, I
         * implemented the comparison for int and double tokens. A more general
         * compare is required.
         *
         * @param arg0
         *            First event.
         * @param arg1
         *            Second event.
         * @return -1 if event arg0 should be processed before event arg1, 0 if
         *         they should be processed at the same time, 1 if arg1 should
         *         be processed before arg0.
         */
        public int compare(Object arg0, Object arg1) {
            Event event1 = (Event) arg0;
            Event event2 = (Event) arg1;
            Time time1 = event1._timeStamp;
            Time time2 = event2._timeStamp;
            if (time1.compareTo(time2) != 0) {
                return time1.compareTo(time2);
            }

            if (event1._token instanceof DoubleToken) {
                DoubleToken token1 = (DoubleToken) event1._token;
                DoubleToken token2 = (DoubleToken) event2._token;
                if (token1.doubleValue() < token2.doubleValue()) {
                    return -1;
                } else if (token1.doubleValue() > token2.doubleValue()) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (event1._token instanceof IntToken) {
                IntToken token1 = (IntToken) event1._token;
                IntToken token2 = (IntToken) event2._token;
                if (token1.intValue() < token2.intValue()) {
                    return -1;
                } else if (token1.intValue() > token2.intValue()) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return 0;
        }

    }
}
