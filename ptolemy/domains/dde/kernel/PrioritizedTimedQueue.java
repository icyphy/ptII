/* A FIFO queue with time and priority attributes that is used for
storing tokens with time stamps.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Green (davisj@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;

import java.util.List;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// PrioritizedTimedQueue
/**
/* A FIFO queue with time and priority attributes that is used for
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
before receivers with lower priorities. A receiver's priority can be
explicitly specified or it can be implicitly determined based on the
topology. In the latter case, a receiver's priority is set according
to the inverse order in which it was connected to the model topology.
I.e., if two input receivers (receiver A and receiver B) are added to
an actor such that receiver A is connected in the model topology before
receiver B, then receiver B will have a higher priority than receiver A.
<P>
If the oldest token in the queue has a time stamp of IGNORE, then the
next oldest token from the other receivers contained by the actor in
question will be consumed and the token time stamped IGNORE will be
dropped. The IGNORE time stamp is useful in feedback topologies in which
an actor should ignore inputs from a feedback cycle when the model's
execution is just beginning. FeedBackDelay actors output a single IGNORE token
during their initialize() methods for just this reason. In general,
IGNORE tokens should not be handled unless fundamental changes to the
DDE kernel are intended.
<P>
The values of the variables IGNORE, INACTIVE and ETERNITY are arbitrary
as long as they have unique, negative values. ETERNITY is used in
conjunction with the completionTime to indicate that an actor should
continue executing indefinitely.
<P>
Note that a PrioritizedTimedQueue is intended for use within a
multi-threaded environment. PrioritizedTimedQueue does not
require the synchronization facilities provided by
ptolemy.kernel.util.Workspace. PrioritizedTimedQueue is subclassed
by DDEReceiver which add significant synchronization facilities
and where appropriate employs workspace.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.DDEReceiver
@see ptolemy.domains.dde.kernel.TimeKeeper
*/

public class PrioritizedTimedQueue {

    /** Construct an empty queue with no container.
     */
    public PrioritizedTimedQueue() {
    }

    /** Construct an empty queue with the specified IOPort container.
     * @param container The IOPort that contains this receiver.
     */
    public PrioritizedTimedQueue(IOPort container) {
	_container = container;
    }

    /** Construct an empty queue with the specified IOPort container
     *  and priority.
     * @param container The IOPort that contains this receiver.
     * @param priority The priority of this receiver.
     */
    public PrioritizedTimedQueue(IOPort container, int priority) {
	_container = container;
	_priority = priority;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // This time value indicates that the receiver is no longer active.
    public static final double INACTIVE = -2.0;

    // This time value is used in conjunction with completionTime to
    // indicate that a receiver will continue operating indefinitely.
    public static final double ETERNITY = -5.0;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
    public Token get() {
        // Get a token and set all relevant
        // local time parameters
	Token token = null;
        Event event = (Event)_queue.take();
        if (event == null) {
            throw new NoTokenException(_container,
                    "Attempt to get token from an empty "
                    + "PrioritizedTimedQueue.");
        }
        token = event.getToken();
        if( _queue.size() > 0 ) {
            Event nextEvent = (Event)_queue.get(0);
            _rcvrTime = nextEvent.getTime();
        }

        // Set relevant TimeKeeper time parameters
	Thread thread = Thread.currentThread();
        try {
            if( thread instanceof DDEThread ) {
                TimeKeeper timeKeeper =
                        ((DDEThread)thread).getTimeKeeper();
                timeKeeper.setCurrentTime( event.getTime() );
                timeKeeper.setOutputTime( timeKeeper.getCurrentTime() );
            }
        } catch( IllegalActionException e ) {
            System.err.println("An exception thrown while setting"
                    + " the output time of TimeKeeper");
        }

        // Call updateRcvrList() even if _queue.size() == 0,
        // so that the triple is no longer in front.
        if( thread instanceof DDEThread ) {
            TimeKeeper timeKeeper =
                ((DDEThread)thread).getTimeKeeper();

	    timeKeeper.removeAllIgnoreTokens();
        }

        return token;
    }

    /** Get the queue capacity of this receiver.
     * @return The capacity of this receiver's queue.
     */
    public int getCapacity() {
        return _queue.getCapacity();
    }

    /** Return the IOPort container of this receiver.
     * @return The containing IOPort.
     */
    public IOPort getContainer() {
        return _container;
    }

    /** Return the last time of this receiver. This method
     *  is not synchronized so the caller should be.
     * @return The last time.
     * @deprecated Only used for testing purposes
     */
    public double getLastTime() {
        return _lastTime;
    }

    /** Return the receiver time of this receiver. The receiver
     *  time is the time stamp associated with the oldest token that
     *  is currently on the queue. If the queue is empty, then the
     *  receiver time value is equal to the "last time."
     * @return The receiver time of this PrioritizedTimedQueue.
     */
    public synchronized double getRcvrTime() {
        return _rcvrTime;
    }

    /** Return true if the number of tokens stored in the queue is
     *  less than the capacity of the queue. Return false otherwise.
     * @return True if the queue is not full; return
     *  false otherwise.
     */
    public boolean hasRoom() {
        return !_queue.isFull();
    }

    /** Return true if there are tokens stored on the queue. Return
     *  false if the queue is empty.
     * @return True if the queue is not empty; return
     *  false otherwise.
     */
    public boolean hasToken() {
        return _queue.size() > 0;
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
    public void put(Token token, double time) throws NoRoomException {
	if( time < _lastTime && time != INACTIVE && time != IGNORE ) {
	    NamedObj actor = (NamedObj)_container.getContainer();
	    // NamedObj actor = (NamedObj)getContainer().getContainer();
	    throw new IllegalArgumentException(actor.getName() +
		    " - Attempt to set current time in the past.");
	} else if( time < 0.0 && time != INACTIVE && time != IGNORE ) {
	    NamedObj actor = (NamedObj)_container.getContainer();
	    // NamedObj actor = (NamedObj)getContainer().getContainer();
	    throw new IllegalArgumentException(actor.getName() +
		    " - Attempt to set current time to a" +
		    " a negative value.");
	}

	double _lastTimeCache = _lastTime;
	double _rcvrTimeCache = _rcvrTime;
	_lastTime = time;
	Event event = new Event(token, time);
        if( _queue.size() == 0 ) {
            _rcvrTime = _lastTime;
        }

	try {
	    if (!_queue.put(event)) {
		throw new NoRoomException (_container,
                    "Queue is at capacity. Cannot insert token.");
	    }
	} catch( NoRoomException e ) {
	    _lastTime = _lastTimeCache;
	    _rcvrTime = _rcvrTimeCache;
	    throw e;
	}
    }

    /** Remove the oldest token off of this queue if it has a
     *  time stamp with a value of IGNORE. Reset the receiver
     *  time and update the time keeper's receiver list.
     * @exception NoTokenException If the queue is empty.
     */
    public synchronized void removeIgnoredToken() {

        if( getRcvrTime() != PrioritizedTimedQueue.IGNORE ) {
            return;
        }
        // Get the token and set all relevant
        // local time parameters
        Event event = (Event)_queue.take();
        if (event == null) {
            throw new NoTokenException(_container,
                    "Attempt to get token from an empty "
                    + "PrioritizedTimedQueue.");
        }
        event.getToken();
        if( _queue.size() > 0 ) {
            Event nextEvent = (Event)_queue.get(0);
            _rcvrTime = nextEvent.getTime();
        }

        // Set relevant time keeper time parameters
	Thread thread = Thread.currentThread();
        try {
            if( thread instanceof DDEThread ) {
                TimeKeeper timeKeeper =
                        ((DDEThread)thread).getTimeKeeper();
		timeKeeper.setOutputTime( timeKeeper.getCurrentTime() );
            }
        } catch( IllegalActionException e ) {
            System.err.println("An exception thrown while setting"
                    + " the output time of TimeKeeper");
        }

	// Set the receiver time if value is still IGNORE
	if( getRcvrTime() == PrioritizedTimedQueue.IGNORE ) {
	    if( thread instanceof DDEThread ) {
                TimeKeeper timeKeeper =
                        ((DDEThread)thread).getTimeKeeper();
		setRcvrTime( timeKeeper.getCurrentTime() );
	    }
	}
    }

    /** Set the queue capacity of this receiver.
     * @param capacity The capacity of this receiver's queue.
     * @exception IllegalActionException If the superclass throws it.
     */
    public void setCapacity(int capacity) throws IllegalActionException {
        _queue.setCapacity(capacity);
    }

    /** Set the IOPort container.
     * @param port The IOPort which contains this receiver.
     */
    public void setContainer(IOPort port) {
        _container = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                  package friendly variables 		   ////

    // This time value indicates that the receiver contents should
    // be ignored.
    static final double IGNORE = -1.0;

    // The time stamp of the newest token to be placed in the queue.
    double _lastTime = 0.0;

    // The priority of this receiver.
    int _priority = 0;

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods                ////

    /** Return the completion time of this receiver. This method
     *  is not synchronized so the caller should be.
     * @return The completion time.
     */
    double getCompletionTime() {
        return _completionTime;
    }

    /** Return true if this receiver has a NullToken at the front
     *  of the queue; return false otherwise.
     *  This method is not synchronized so the caller should be.
     * @return True if this receiver contains a NullToken in the
     *  oldest queue position; return false otherwise.
     */
    boolean hasNullToken() {
	if( _queue.size() > 0 ) {
	    Event event = (Event)_queue.get(0);
	    if( event.getToken() instanceof NullToken ) {
		return true;
	    }
	}
        return false;
    }

    /** Reset local flags. The local flags of this receiver impact
     *  the local notion of time of the actor that contains this
     *  receiver.
     *  This method is not synchronized so the caller should be.
     */
    void reset() {
        DDEDirector director = (DDEDirector)
            ((Actor)_container.getContainer()).getDirector();
	double time = director.getCurrentTime();
	_rcvrTime = time;
	_lastTime = time;
	_queue.clear();
    }

    /** Set the completion time of this receiver. If the
     *  completion time argument is negative but is not
     *  equal to PrioritizedTimedQueue.ETERNITY, then throw
     *  an IllegalArgumentException.
     *  This method is not synchronized so the caller should be.
     * @param time The completion time of this receiver.
     */
    void setCompletionTime(double time) {
	if( time < 0.0 && time != PrioritizedTimedQueue.ETERNITY ) {
	    throw new IllegalArgumentException("Attempt to set "
            	    + "completion time to a negative value.");
	}
        _completionTime = time;
    }

    /** Set the receiver time of this receiver to the specified
     *  value. If this queue is not empty, then the receiver
     *  time will not be set to the specified value. This method
     *  is not synchronized so the caller should be.
     * @param time The new rcvr time.
     */
    synchronized void setRcvrTime(double time) {
	if( !(_queue.size() > 0) ) {
            _rcvrTime = time;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The time after which execution of this receiver will cease.
    // Initially the value is set so that execution will continue
    // indefinitely.
    private double _completionTime = ETERNITY;

    // The IOPort which contains this receiver.
    private IOPort _container;

    // The queue in which this receiver stores tokens.
    private FIFOQueue _queue = new FIFOQueue();

    // The time stamp of the earliest token that is still in the queue.
    private double _rcvrTime = 0.0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // An Event is an aggregation consisting of a Token, a
    // time stamp and destination Receiver. Both the token
    // and destination receiver are allowed to have null
    // values. This is particularly useful in situations
    // where the specification of the destination receiver
    // may be considered redundant.
    private class Event {

	// Construct an Event with a token and time stamp.
	public Event(Token token, double time) {
	    _token = token;
	    _timeStamp = time;
	}

	// Construct an Event with a token, a time stamp and a
	// destination receiver.
	public Event(Token token, double time, Receiver receiver) {
	    this(token, time);
	    _receiver = receiver;
	}


        ///////////////////////////////////////////////////////////
	////                     public inner methods          ////

	// Return the destination receiver of this event.
	public Receiver getReceiver() {
	    return _receiver;
	}

	// Return the time stamp of this event.
	public double getTime() {
	    return _timeStamp;
	}

	// Return the token of this event.
	public Token getToken() {
	    return _token;
	}

        ///////////////////////////////////////////////////////////
        ////                     private inner variables       ////

	double _timeStamp = 0.0;
	Token _token = null;
	Receiver _receiver = null;
    }

}
