/* A FIFO queue-based receiver for storing tokens with time stamps.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.odf.kernel;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;

//////////////////////////////////////////////////////////////////////////
//// TimedQueueReceiver
/**
A FIFO queue-based receiver for storing tokens with time stamps. A "time
stamp" is a time value that is associated with a token and is used to order
the consumption of a token with respect to other time stamped tokens. To
help organize the tokens contained by this queue, two flags are maintained:
"_lastTime" and "_rcvrTime." The _lastTime flag is defined to be equivalent
to the time stamp of the token that was most recently placed in the queue.
The _rcvrTime flag is defined as the time stamp of the oldest token in the
queue or the last token to be removed from the queue if the queue is empty.
Both of these flags must have monotonically, non-decreasing values. At the 
conclusion of a simulation run the receiver time is set to INACTIVE.
<P>
To facilitate the transfer of local time information in a manner that
is amenable to polymorphic actors, it is necessary to have a reference
to a TimeKeeper for both the sending and receiving actors associated with
this receiver.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.odf.kernel.ODFReceiver
*/

public class TimedQueueReceiver {

    /** Construct an empty queue with no container.
     */
    public TimedQueueReceiver() {
        super();
    }

    /** Construct an empty queue with the specified IOPort container.
     * @param container The IOPort that contains this receiver.
     */
    public TimedQueueReceiver(IOPort container) {
        super();
	_container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Take the the oldest token off of the queue and return it.
     *  If the queue is empty, throw a NoTokenException. If there are
     *  other tokens left on the queue after this removal, then set
     *  the _rcvrTime of this receiver to equal that of the next oldest
     *  token. Update the RcvrTimeTriple entry in the TimeKeeper that 
     *  manages this receiver.
     * @exception NoTokenException If the queue is empty.
     * @see ptolemy.domains.odf.kernel.RcvrTimeTriple
     */
    public Token get() {
	Thread thread = Thread.currentThread();
	// TimeKeeper timeKeeper = getReceivingTimeKeeper();
	Token token = null;
	synchronized( this ) {
            Event event = (Event)_queue.take();
	    if (event == null) {
                throw new NoTokenException(getContainer(),
	                "Attempt to get token from an empty "
                        + "TimedQueueReceiver.");
            }
	    token = event.getToken();
	    if( thread instanceof ODFThread ) {
	        TimeKeeper timeKeeper = ((ODFThread)thread).getTimeKeeper();
	        timeKeeper.setCurrentTime( event.getTime() );
	    }

	    if( _queue.size() > 0 ) {
	        Event nextEvent = (Event)_queue.get(0);
	        _rcvrTime = nextEvent.getTime();
            }

	    // Call updateRcvrList() even if _queue.size()==0, so that
	    // the triple is no longer in front.
	    if( thread instanceof ODFThread ) {
		TimeKeeper timeKeeper = ((ODFThread)thread).getTimeKeeper();
	        timeKeeper.updateRcvrList( this, _rcvrTime, _priority );
	    }
	}
        return token;
    }

    /** Return the IOPort container of this receiver.
     * @return IOPort The containing IOPort.
     */
    public IOPort getContainer() {
        return _container;
    }

    /** Return the _lastTime value of this receiver. The _lastTime is
     *  the time associated with the token that was most recently
     *  placed in the queue. If no tokens have been placed on the
     *  queue, then _lastTime equals 0.0.
     * @return double The value of the _lastTime flag.
     */
    public double getLastTime() {
        return _lastTime;
    }

    /** Get the priority of this receiver.
     * @return Return the priority of this receiver.
     */
    public int getPriority() {
        return _priority;
    }

    /** Return the _rcvrTime value of this receiver. The _rcvrTime is
     *  the time associated with the oldest token that is currently
     *  on the queue. If the queue is empty, then the _rcvrTime value
     *  is equal to the _lastTime value.
     * @return double The value of the _rcvrTime flag.
     */
    public double getRcvrTime() {
        return _rcvrTime;
    }

    /** Return the time keeper that maintains time for the actor that
     *  contains this receiver.
     * @return The time keeper that maintains time for the actor that
     *  contains this receiver.
    public TimeKeeper getReceivingTimeKeeper() {
        return _rcvingTimeKeeper;
    }
     */

    /** Return the time keeper that maintains time for the actor that
     *  sends through this receiver.
     * @return The time keeper that maintains time for the actor 
     *  that sends through this receiver.
    public TimeKeeper getSendingTimeKeeper() {
        return _sendingTimeKeeper;
    }
     */

    /** Return true if the number of tokens stored in the queue is
     *  less than the capacity of the queue. Return false otherwise.
     * @return boolean Return true if the queue is not full; return
     *  false otherwise.
     */
    public boolean hasRoom() {
        return !_queue.isFull();
    }

    /** Return true if there are tokens stored on the queue. Return
     *  false if the queue is empty.
     * @return boolean Return true if the queue is not empty; return
     *  false otherwise.
     */
    public boolean hasToken() {
        return _queue.size() > 0;
    }

    /** Put a token on the queue with the specified time stamp and set
     *  the value of the _lastTime flag to be equal to this time stamp.
     *  If the queue is empty immediately prior to putting the token on
     *  the queue, then set the _rcvrTime flag value to be equal to the
     *  _lastTime flag value. If the queue is full, throw a NoRoomException.
     *  Update the RcvrTimeTriple entry in the ODFThread which manages
     *  this receiver.
     * @param token The token to put on the queue.
     * @param time The time stamp of the token.
     * @exception NoRoomException If the queue is full.
     */
    public void put(Token token, double time) throws NoRoomException {
	if( time < _lastTime && time != INACTIVE ) {
	    // if( time < _lastTime && time != -1.0 ) {
	    IOPort port = (IOPort)getContainer(); 
	    NamedObj actor = (NamedObj)port.getContainer(); 
	    throw new IllegalArgumentException(actor.getName() + 
		    " - Attempt to set current time in the past.");
	}
        Event event;
	// TimeKeeper timeKeeper = getReceivingTimeKeeper();
        synchronized(this) {
            _lastTime = time;
            event = new Event(token, _lastTime);

            if( _queue.size() == 0 ) {
                _rcvrTime = _lastTime;
            }

            if (!_queue.put(event)) {
                throw new NoRoomException (getContainer(),
                        "Queue is at capacity. Cannot insert token.");
            }
        }
    }

    /** Set the queue capacity of this receiver.
     * @param capacity The capacity of this receiver's queue.
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

    /** Set the time keeper that maintains time for the actor that
     *  contains this receiver.
     * @param timeKeeper The time keeper that maintains time for 
     *  the actor that contains this receiver.
    public void setReceivingTimeKeeper(TimeKeeper timeKeeper) {
        _rcvingTimeKeeper = timeKeeper;
    }
     */

    /** Set the time keeper that maintains time for the actor that
     *  sends through this receiver.
     * @param timeKeeper The time keeper that maintains time for 
     *  the actor that sends through this receiver.
    public void setSendingTimeKeeper(TimeKeeper timeKeeper) {
        _sendingTimeKeeper = timeKeeper;
    }
     */

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods                ////

    /** Return the completion time of this receiver.
     * @return double The completion time.
     */
    synchronized double getCompletionTime() {
        return _completionTime;
    }

    /** Set the completion time of this receiver.
     * @param time The completion time of this receiver.
     */
    void setCompletionTime(double time) {
        _completionTime = time;
    }

    /** Set the priority of this receiver.
     * @param int The priority of this receiver.
     */
    synchronized void setPriority(int priority) {
        _priority = priority;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // This time value indicates that the receiver is no longer active.
    protected static double INACTIVE = -1.0;

    // This time value indicates that the receiver is no longer active.
    protected static double NOTSTARTED = -5.0;

    // The time stamp of the newest token to be placed in the queue.
    private double _lastTime = 0.0;

    // The time stamp of the earliest token that is still in the queue.
    private double _rcvrTime = 0.0;

    // The time after which execution of this receiver will cease.
    private double _completionTime = NOTSTARTED;

    // The priority of this receiver.
    private int _priority = 0;

    // The queue in which this receiver stores tokens.
    private FIFOQueue _queue = new FIFOQueue();

    // The IOPort which contains this receiver.
    private IOPort _container;

    // The thread controlling the actor that contains this receiver.
    private ODFThread _controllingThread;

    // The time keeper for the actor that receives through this receiver.
    private TimeKeeper _rcvingTimeKeeper;

    // The time keeper for the actor that sends through this receiver.
    private TimeKeeper _sendingTimeKeeper;

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
	////                     public methods                ////

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
        ////                     private variables             ////

	double _timeStamp = 0.0;
	Token _token = null;
	Receiver _receiver = null;
    }

}
