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
"lastTime" and "rcvrTime." The lastTime flag is defined to be equivalent 
to the time stamp of the token that was most recently placed in the queue. 
The rcvrTime flag is defined as the time stamp of the oldest token in the 
queue or the last token to be removed from the queue if the queue is empty. 
Both of these flags must have monotonically, non-decreasing values with the 
exception that their values will be set to -1.0 at the conclusion of a 
simulation run. 
<P>
To facilitate the transfer of local time information in a manner that
is amenable to polymorphic actors, it is necessary to have a reference
to the ODFThread that controls the actor that contains this receiver.
The setThread() and getThread() methods serve this purpose.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.odf.kernel.ODFReceiver
*/

public class TimedQueueReceiver implements Receiver {

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
     *  the rcvrTime of this receiver to equal that of the next oldest 
     *  token. Update the RcvrTimeTriple entry in the ODFThread which 
     *  manages this receiver.
     * @exception NoTokenException If the queue is empty.
     * @see ptolemy.domains.odf.kernel.RcvrTimeTriple
     */
    public Token get() {
	Thread thread = Thread.currentThread();
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
	        ((ODFThread)thread).setCurrentTime( event.getTime() );
	    } else {
                /*
                System.err.println("ERROR: TimedQueueReceiver.get()"
                        + " being invoked by non-ODFThread.");
                */
	    }

	    if( getSize() > 0 ) {
	        Event nextEvent = (Event)_queue.get(0); 
	        _rcvrTime = nextEvent.getTime(); 
            } 

	    // Call updateRcvrList() even if getSize()==0, so that
	    // the triple is no longer in front.
	    RcvrTimeTriple triple; 
	    triple = new RcvrTimeTriple( this, _rcvrTime, _priority ); 
	    if( thread instanceof ODFThread ) {
	        ((ODFThread)thread).updateRcvrList( triple );
	    }
	}
        return token;
    }

    /** Return the completion time of this receiver. 
     * @return double The completion time.
     */
    public synchronized double getCompletionTime() {
        return _completionTime;
    }

    /** Return the IOPort container of this receiver. 
     * @return IOPort The containing IOPort.
     */
    public IOPort getContainer() {
        return _container;
    }

    /** Return the lastTime value of this receiver. The lastTime is
     *  the time associated with the token that was most recently
     *  placed in the queue. If no tokens have been placed on the
     *  queue, then lastTime equals 0.0. 
     * @return double The value of the lastTime flag.
     */
    public double getLastTime() {
        return _lastTime;
    }

    /** Get the priority of this receiver. 
     * @return Return the priority of this receiver.
     */
    public synchronized int getPriority() {
        return _priority;
    }

    /** Return the rcvrTime value of this receiver. The rcvrTime is 
     *  the time associated with the oldest token that is currently
     *  on the queue. If the queue is empty, then the rcvrTime value
     *  is equal to the lastTime value.
     * @return double The value of the rcvrTime flag.
     */
    public double getRcvrTime() {
        return _rcvrTime;
    }

    /** Get the queue size. 
     * @return Return the size of the queue.
     */
    public int getSize() {
        return _queue.size();
    }

    /** Return the ODFThread that controls the actor that contains
     *  this receiver.
     * @return Return the ODFThread that controls the actor that 
     *  contains this receiver.
     */
    public ODFThread getThread() {
	return _controllingThread;
    }

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

    /** Put a token on the queue and set the time stamp of the token to
     *  the value of the lastTime flag of this receiver. If the queue is 
     *  empty immediately prior to putting the token on the queue, then 
     *  set the rcvrTime flag value to be equal to the lastTime flag value. 
     *  If the queue is full, throw a NoRoomException. Update the 
     *  RcvrTimeTriple entry in the ODFThread which manages this receiver.
     * @param token The token to put on the queue.
     */
    public void put(Token token) {
        put( token, _lastTime );
    }

    /** Put a token on the queue with the specified time stamp and set 
     *  the value of the lastTime flag to be equal to this time stamp.
     *  If the queue is empty immediately prior to putting the token on 
     *  the queue, then set the rcvrTime flag value to be equal to the 
     *  lastTime flag value. If the queue is full, throw a NoRoomException. 
     *  Update the RcvrTimeTriple entry in the ODFThread which manages
     *  this receiver.
     * @param token The token to put on the queue.
     * @param time The time stamp of the token.
     */
    public void put(Token token, double time) {
        Event event;
        IOPort port = (IOPort)getContainer();
	ODFThread thread = getThread();
        ODFActor actor = (ODFActor)port.getContainer();
        
        synchronized(this) {
            _lastTime = time; 
            event = new Event(token, _lastTime);
            
            if( getSize() == 0 ) {
                RcvrTimeTriple triple; 
                _rcvrTime = _lastTime; 
                triple = new RcvrTimeTriple( this, _rcvrTime, _priority ); 
		if( thread instanceof ODFThread ) {
		    if( actor.getName().equals("printer") ) {
                        System.out.println("\t\t***" + actor.getName() +
				" calling update at time " + _rcvrTime); 
		    }
                    ((ODFThread)thread).updateRcvrList( triple ); 
		} else {
                    /*
		    System.err.println("ERROR: Non-ODFThread calling "
			    + "TimedQueueReceiver.put()");
                    */
		}
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

    /** Set the completion time of this receiver. 
     * @param time The completion time of this receiver.
     */
    public void setCompletionTime(double time) {
        _completionTime = time;
    }

    /** Set the IOPort container. 
     * @param port The IOPort which contains this receiver.
     */
    public void setContainer(IOPort port) {
        _container = port;
    }

    /** Set the priority of this receiver. 
     * @param int The priority of this receiver.
     */
    public synchronized void setPriority(int priority) {
        _priority = priority;
    }

    /** Set the ODFThread that controls the actor that contains
     *  this receiver.
     * @param thread The ODFThread that controls the actor that 
     *  contains this receiver.
     */
    public void setThread(ODFThread thread) {
	_controllingThread = thread;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The time stamp of the newest token to be placed in the queue.
    private double _lastTime = 0.0;
    
    // The time stamp of the earliest token that is still in the queue.
    private double _rcvrTime = 0.0;

    // The time after which execution of this receiver will cease.
    private double _completionTime = -5.0;

    // The priority of this receiver.
    private int _priority = 0;
    
    // The queue in which this receiver stores tokens.
    public FIFOQueue _queue = new FIFOQueue();

    // The IOPort which contains this receiver.
    private IOPort _container;

    // The thread controlling the actor that contains this receiver.
    private ODFThread _controllingThread;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // An Event is an aggregation consisting of a Token, a 
    // time stamp and destination Receiver. Both the token 
    // and destination receiver are allowed to have null 
    // values. This is particularly useful in situations 
    // where the specification of the destination receiver 
    // may be considered redundant.

    public class Event {

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
