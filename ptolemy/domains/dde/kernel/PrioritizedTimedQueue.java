/* A FIFO queue with time and priority attributes that is used for
 storing tokens with time stamps.

 Copyright (c) 1997-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.domains.dde.kernel;

import java.util.List;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PrioritizedTimedQueue

/**
 A FIFO queue with time and priority attributes that is used for
 storing tokens with time stamps. A "time stamp" is a time value that
 is associated with a token and is used to order the consumption of a
 token with respect to other time stamped tokens. To help organize the
 tokens contained by this queue, two flags are maintained: <I>last time</I>
 and <I>receiver time</I>. The last time flag is defined to be equivalent
 to the time stamp of the token that was most recently placed in the queue.
 The receiver time flag is defined as the time stamp of the oldest token in
 the queue or the last token to be removed from the queue if the queue is
 empty. Both of these flags must have monotonically, non-decreasing values
 (with the exception of the IGNORE, INACTIVE and ETERNITY values). At the
 conclusion of a simulation run the receiver time is set to INACTIVE.
 <P>
 A PrioritizedTimedQueue is subclassed by DDEReceiver. Hence,
 PrioritizedTimedQueues serve as the foundation for receivers
 contained in the IO ports of actors operating within DDE models.
 A TimeKeeper object is assigned to each actor that operates according to
 the DDE model of computation. The TimeKeeper manages each of the receivers
 that are contained by an actor by keeping track of the receiver times of
 each receiver. As information flows through a PrioritizedTimedQueue, the
 TimeKeeper must be kept up to date with respect to the receiver times.
 The TimeKeeper orders the PrioritizedTimedQueues according to their receiver
 times and priorities. PrioritizedTimedQueues with smaller receiver times are
 ordered first.
 <P>
 PrioritizedTimedQueues with identical receiver times are sorted according
 to their respective priorities. PrioritizedTimedQueues are assigned
 priorities (a nonnegative integer) by a TimeKeeper when the TimeKeeper
 is instantiated. Receivers with higher receiver priorities are ordered
 before receivers with lower priorities. The priority of a receiver can be
 explicitly specified or it can be implicitly determined based on the
 topology. In the latter case, the priority of a receiver is set according
 to the inverse order in which it was connected to the model topology.
 I.e., if two input receivers (receiver A and receiver B) are added to
 an actor such that receiver A is connected in the model topology before
 receiver B, then receiver B will have a higher priority than receiver A.
 <P>
 If the oldest token in the queue has a time stamp of IGNORE, then the
 next oldest token from the other receivers contained by the actor in
 question will be consumed and the token time stamped IGNORE will be
 dropped. The IGNORE time stamp is useful in feedback topologies in which
 an actor should ignore inputs from a feedback cycle when the execution of
 the model is just beginning. FeedBackDelay actors output a single IGNORE
 token during their initialize() methods for just this reason. In general,
 IGNORE tokens should not be handled unless fundamental changes to the
 DDE kernel are intended.
 <P>
 The values of the package friendly variables IGNORE, INACTIVE and ETERNITY
 are arbitrary as long as they have unique, negative values. ETERNITY is
 used in conjunction with the completionTime to indicate that an actor
 should continue executing indefinitely.
 <P>
 Note that a PrioritizedTimedQueue is intended for use within a
 multi-threaded environment. PrioritizedTimedQueue does not
 require the synchronization facilities provided by
 ptolemy.kernel.util.Workspace. PrioritizedTimedQueue is subclassed
 by DDEReceiver which adds significant synchronization facilities
 and where appropriate employs workspace.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Green (davisj)
 @Pt.AcceptedRating Green (kienhuis)
 @see ptolemy.domains.dde.kernel.DDEReceiver
 @see ptolemy.domains.dde.kernel.TimeKeeper
 */
public class PrioritizedTimedQueue extends AbstractReceiver {
    /** Construct an empty queue with no container.
     */
    public PrioritizedTimedQueue() {
        // because the container is not specified, we can not
        // call the _initializeTimeVariables() method.
        // The caller of this constructor is responsible to
        // initialize the time variables.
    }

    /** Construct an empty queue with the specified IOPort container.
     *  @param container The IOPort that contains this receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public PrioritizedTimedQueue(IOPort container)
            throws IllegalActionException {
        super(container);
        _initializeTimeVariables();
    }

    /** Construct an empty queue with the specified IOPort container
     *  and priority.
     *  @param container The IOPort that contains this receiver.
     *  @param priority The priority of this receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public PrioritizedTimedQueue(IOPort container, int priority)
            throws IllegalActionException {
        super(container);
        _priority = priority;
        _initializeTimeVariables();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list with the tokens currently in the receiver, or
     *  an empty list if there is no such token.
     *  @return A list of instances of Token.
     */
    @Override
    public List<Token> elementList() {
        return _queue.elementList();
    }

    /** Take the the oldest token off of the queue and return it.
     *  If the queue is empty, throw a NoTokenException. If there are
     *  other tokens left on the queue after this removal, then set
     *  the receiver time of this receiver to equal that of the next
     *  oldest token. Update the TimeKeeper that manages this
     *  PrioritizedTimedQueue. If there are any receivers in the
     *  TimeKeeper with receiver times of PrioritizedTimedQueue.IGNORE,
     *  remove the first token from these receivers.
     * @return The oldest token off of the queue.
     * @exception NoTokenException If the queue is empty.
     */
    @Override
    public Token get() {
        // Get a token and set all relevant
        // local time parameters
        Token token = null;
        Event event = (Event) _queue.take();

        if (event == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get token from an empty "
                            + "PrioritizedTimedQueue.");
        }

        token = event.getToken();

        if (_queue.size() > 0) {
            Event nextEvent = (Event) _queue.get(0);
            _receiverTime = nextEvent.getTime();
        }

        // Set relevant TimeKeeper time parameters
        Thread thread = Thread.currentThread();

        try {
            if (thread instanceof DDEThread) {
                TimeKeeper timeKeeper = ((DDEThread) thread).getTimeKeeper();
                timeKeeper.setCurrentTime(event.getTime());
                timeKeeper._setOutputTime(timeKeeper.getModelTime());
            }
        } catch (IllegalActionException e) {
            System.err.println("An exception thrown while setting"
                    + " the output time of TimeKeeper");
        }

        // Call updateReceiverList() even if _queue.size() == 0,
        // so that the triple is no longer in front.
        if (thread instanceof DDEThread) {
            TimeKeeper timeKeeper = ((DDEThread) thread).getTimeKeeper();

            timeKeeper.removeAllIgnoreTokens();
        }

        return token;
    }

    /** Get the queue capacity of this receiver.
     * @return The capacity of this receiver's queue.
     * @see #setCapacity(int)
     */
    public int getCapacity() {
        return _queue.getCapacity();
    }

    /** Return the last time of this receiver. This method
     *  is not synchronized so the caller should be.
     * @return The last time.
     * @deprecated Only used for testing purposes
     */
    @Deprecated
    public Time getLastTime() {
        return _lastTime;
    }

    /** Return the receiver time of this receiver. The receiver
     *  time is the time stamp associated with the oldest token that
     *  is currently on the queue. If the queue is empty, then the
     *  receiver time value is equal to the "last time."
     * @return The receiver time of this PrioritizedTimedQueue.
     */
    public synchronized Time getReceiverTime() {
        return _receiverTime;
    }

    /** Return true if the number of tokens stored in the queue is
     *  less than the capacity of the queue. Return false otherwise.
     * @return True if the queue is not full; return false otherwise.
     */
    @Override
    public boolean hasRoom() {
        return !_queue.isFull();
    }

    /** Return true if the queue capacity minus the queue size is
     *  greater than the argument.
     *  @param numberOfTokens The number of tokens to put into the queue.
     *  @return True if the queue is not full; return false otherwise.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     */
    @Override
    public boolean hasRoom(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "hasRoom() requires a positive argument.");
        }

        return _queue.getCapacity() - _queue.size() > numberOfTokens;
    }

    /** Return true if there are tokens stored on the queue. Return
     *  false if the queue is empty.
     * @return True if the queue is not empty; return
     *  false otherwise.
     */
    @Override
    public boolean hasToken() {
        return _queue.size() > 0;
    }

    /** Return true if queue size is at least the argument.
     *  @param numberOfTokens The number of tokens to get from the queue.
     *  @return True if the queue has enough tokens.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     */
    @Override
    public boolean hasToken(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "hasToken() requires a positive argument.");
        }

        return _queue.size() > numberOfTokens;
    }

    /** Throw an exception, since this method is not used in
     *  DDE.
     *  @param token The token to be put to the receiver.
     *  @exception NoRoomException If the receiver is full.
     */
    @Override
    public void put(Token token) {
        throw new NoRoomException("put(Token) is not used in the "
                + "DDE domain.");
    }

    /** Put a token on the queue with the specified time stamp and set
     *  the last time value to be equal to this time stamp. If the
     *  queue is empty immediately prior to putting the token on
     *  the queue, then set the receiver time value to be equal to the
     *  last time value. If the queue is full, throw a NoRoomException.
     *  Time stamps can not be set to negative values that are not equal
     *  to IGNORE or INACTIVE; otherwise an IllegalArgumentException
     *  will be thrown.
     * @param token The token to put on the queue.
     * @param time The time stamp of the token.
     * @exception NoRoomException If the queue is full.
     */
    public void put(Token token, Time time) throws NoRoomException {
        double timeValue = time.getDoubleValue();

        if (time.compareTo(_lastTime) < 0 && timeValue != INACTIVE
                && timeValue != IGNORE) {
            NamedObj actor = getContainer().getContainer();
            throw new IllegalArgumentException(actor.getName()
                    + " - Attempt to set current time to the past; time = "
                    + time + ". The _lastTime was " + _lastTime);
        } else if (timeValue < 0.0 && timeValue != INACTIVE
                && timeValue != IGNORE) {
            NamedObj actor = getContainer().getContainer();
            throw new IllegalArgumentException(actor.getName()
                    + " - Attempt to set current time to a"
                    + " a negative value = " + time);
        }

        /*
         String containerName = actor.getName();
         Thread thread = Thread.currentThread();
         String invokerName = "";
         if ( thread instanceof DDEThread ) {
         DDEThread ddeThread = (DDEThread)thread;
         invokerName = ((Nameable)ddeThread.getActor()).getName();
         }

         if ( containerName.endsWith("2") || invokerName.endsWith("2") ) {
         if ( token instanceof NullToken ) {
         System.out.println(invokerName + " put NullToken into the receiver of "
         + containerName + " at " + time + ". Last time is " + _lastTime);
         } else {
         System.out.println(invokerName + " put RealToken into the receiver of "
         + containerName + " at " + time + ". Last time is " + _lastTime);
         }
         }
         */
        Time _lastTimeCache = _lastTime;
        Time _receiverTimeCache = _receiverTime;
        _lastTime = time;

        Event event = new Event(token, time);

        if (_queue.size() == 0) {
            _receiverTime = _lastTime;
        }

        try {
            if (!_queue.put(event)) {
                throw new NoRoomException(getContainer(),
                        "Queue is at capacity. Cannot insert token.");
            }
        } catch (NoRoomException e) {
            _lastTime = _lastTimeCache;
            _receiverTime = _receiverTimeCache;
            throw e;
        }
    }

    /** Remove the oldest token off of this queue if it has a
     *  time stamp with a value of IGNORE. Reset the receiver
     *  time and update the time keeper's receiver list.
     * @exception NoTokenException If the queue is empty.
     */
    public synchronized void removeIgnoredToken() {
        if (getReceiverTime().getDoubleValue() != PrioritizedTimedQueue.IGNORE) {
            return;
        }

        // Get the token and set all relevant
        // local time parameters
        Event event = (Event) _queue.take();

        if (event == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get token from an empty "
                            + "PrioritizedTimedQueue.");
        }

        event.getToken();

        if (_queue.size() > 0) {
            Event nextEvent = (Event) _queue.get(0);
            _receiverTime = nextEvent.getTime();
        }

        // Set relevant time keeper time parameters
        Thread thread = Thread.currentThread();

        try {
            if (thread instanceof DDEThread) {
                TimeKeeper timeKeeper = ((DDEThread) thread).getTimeKeeper();
                timeKeeper._setOutputTime(timeKeeper.getModelTime());
            }
        } catch (IllegalActionException e) {
            System.err.println("An exception thrown while setting"
                    + " the output time of TimeKeeper");
        }

        // Set the receiver time if value is still IGNORE
        if (getReceiverTime().getDoubleValue() == PrioritizedTimedQueue.IGNORE) {
            if (thread instanceof DDEThread) {
                TimeKeeper timeKeeper = ((DDEThread) thread).getTimeKeeper();
                _setReceiverTime(timeKeeper.getModelTime());
            }
        }
    }

    /** Reset local flags. The local flags of this receiver impact
     *  the local notion of time of the actor that contains this
     *  receiver.
     *  This method is not synchronized so the caller should be.
     */
    @Override
    public void reset() {
        DDEDirector director = (DDEDirector) ((Actor) getContainer()
                .getContainer()).getDirector();
        Time time = director.getModelTime();
        _receiverTime = time;
        _lastTime = time;

        // I believe this is not needed anymore, because receivers are
        // automatically recreated by the kernel each execution. -SN 3/19/2002
        // _queue.clear();
    }

    /** Set the queue capacity of this receiver.
     * @param capacity The capacity of this receiver's queue.
     * @exception IllegalActionException If the superclass throws it.
     * @see #getCapacity()
     */
    public void setCapacity(int capacity) throws IllegalActionException {
        _queue.setCapacity(capacity);
    }

    ///////////////////////////////////////////////////////////////////
    ////                  package friendly variables                    ////
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
    Time _lastTime;

    // The priority of this receiver.
    int _priority = 0;

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods                ////

    /** Return the completion time of this receiver. This method
     *  is not synchronized so the caller should be.
     * @return The completion time.
     */
    Time _getCompletionTime() {
        return _completionTime;
    }

    /** Return true if this receiver has a NullToken at the front
     *  of the queue; return false otherwise.
     *  This method is not synchronized so the caller should be.
     * @return True if this receiver contains a NullToken in the
     *  oldest queue position; return false otherwise.
     */
    boolean _hasNullToken() {
        if (_queue.size() > 0) {
            Event event = (Event) _queue.get(0);

            if (event.getToken() instanceof NullToken) {
                return true;
            }
        }

        return false;
    }

    /** Set the completion time of this receiver. If the
     *  completion time argument is negative but is not
     *  equal to PrioritizedTimedQueue.ETERNITY, then throw
     *  an IllegalArgumentException.
     *  This method is not synchronized so the caller should be.
     * @param time The completion time of this receiver.
     */
    void _setCompletionTime(Time time) {
        double timeValue = time.getDoubleValue();

        if (timeValue < 0.0 && timeValue != PrioritizedTimedQueue.ETERNITY) {
            throw new IllegalArgumentException("Attempt to set "
                    + "completion time to a negative value.");
        }

        _completionTime = time;
    }

    /** Set the receiver time of this receiver to the specified
     *  value. If this queue is not empty, then the receiver
     *  time will not be set to the specified value. This method
     *  is not synchronized so the caller should be.
     * @param time The new receiver time.
     */
    synchronized void _setReceiverTime(Time time) {
        if (!(_queue.size() > 0)) {
            _receiverTime = time;
        }
    }

    // Initialize some time variables.
    private void _initializeTimeVariables() {
        Actor actor = (Actor) getContainer().getContainer();

        try {
            _completionTime = new Time(actor.getDirector(), ETERNITY);
            _lastTime = new Time(actor.getDirector());
            _receiverTime = new Time(actor.getDirector());
        } catch (IllegalActionException e) {
            // If the time resolution of the director is invalid,
            // it should have been caught before this.
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The time after which execution of this receiver will cease.
    // Initially the value is set so that execution will continue
    // indefinitely.
    private Time _completionTime;

    // The queue in which this receiver stores tokens.
    private FIFOQueue _queue = new FIFOQueue();

    // The time stamp of the earliest token that is still in the queue.
    private Time _receiverTime;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    // An Event is an aggregation consisting of a Token, a
    // time stamp and destination Receiver. Both the token
    // and destination receiver are allowed to have null
    // values. This is particularly useful in situations
    // where the specification of the destination receiver
    // may be considered redundant.
    private static class Event {
        // Construct an Event with a token and time stamp.
        public Event(Token token, Time time) {
            _token = token;
            _timeStamp = time;
        }

        ///////////////////////////////////////////////////////////
        ////                     public inner methods          ////

        // Return the time stamp of this event.
        public Time getTime() {
            return _timeStamp;
        }

        // Return the token of this event.
        public Token getToken() {
            return _token;
        }

        ///////////////////////////////////////////////////////////
        ////                     private inner variables       ////
        Time _timeStamp;

        Token _token = null;
    }
}
