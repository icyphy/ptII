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

@ProposedRating Yellow (davisj@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// TimedQueueReceiver
/**
A FIFO queue-based receiver for storing tokens with time stamps. A "time
stamp" is a time value that is associated with a token and is used to order
the consumption of a token with respect to other time stamped tokens. To
help organize the tokens contained by this queue, two flags are maintained:
<I>last time</I> and <I>receiver time</I>. The last time flag is defined to
be equivalent to the time stamp of the token that was most recently placed
in the queue. The receiver time flag is defined as the time stamp of the
oldest token in the queue or the last token to be removed from the queue if
the queue is empty. Both of these flags must have monotonically,
non-decreasing values. At the conclusion of a simulation run the receiver
time is set to INACTIVE.
<P>
A TimeKeeper object is assigned to each actor that operates according to
a DDE model of computation. The TimeKeeper manages each of the receivers
that are contained by an actor by keeping track of the receiver times of
each receiver. As information flows through a TimedQueueReceiver, the
TimeKeeper must be kept up to date with respect to the receiver times.
<P>
If the oldest token in the queue has a time stamp of IGNORE, then the
next oldest token from the other receivers contained by the actor in
question will be consumed and the token time stamped IGNORE will be
dropped. The IGNORE time stamp is useful in feedback topologies in which
an actor should ignore inputs from a feedback cycle when the model's
execution is just beginning. FBDelay actors output a single IGNORE token
during their initialize() methods for just this reason. In general,
IGNORE tokens should not be handled unless fundamental changes to the
DDE kernel are intended.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.DDEReceiver
@see ptolemy.domains.dde.kernel.TimeKeeper
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

    /** Construct an empty queue with the specified IOPort container
     *  and priority.
     * @param container The IOPort that contains this receiver.
     * @param priority The priority of this receiver.
     */
    public TimedQueueReceiver(IOPort container, int priority) {
        super();
	_container = container;
	setPriority(priority);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // This time value indicates that the receiver is no longer active.
    public static final double INACTIVE = -2.0;

    // This time value indicates that the receiver has not begun
    // activity.
    public static final double ETERNITY = -5.0;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Take the the oldest token off of the queue and return it.
     *  If the queue is empty, throw a NoTokenException. If there are
     *  other tokens left on the queue after this removal, then set
     *  the receiver time of this receiver to equal that of the next
     *  oldest token. Update the TimeKeeper that manages this
     *  TimedQueueReceiver. If there are any receivers in the
     *  TimeKeeper with receiver times of TimedQueueReceiver.IGNORE,
     *  remove the first token from these receivers.
     * @exception NoTokenException If the queue is empty.
     */
    public Token get() {
        // Get the a token and set all relevant 
        // local time parameters
	Token token = null;
        Event event = (Event)_queue.take();
        if (event == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get token from an empty "
                    + "TimedQueueReceiver.");
        }
        token = event.getToken();
        if( _queue.size() > 0 ) {
            Event nextEvent = (Event)_queue.get(0);
            _rcvrTime = nextEvent.getTime();
        }
        
        // Set relevant TimeKeeper time parameters
        // based on whether this receiver is contained
        // in a boundary port or not.
	Thread thread = Thread.currentThread();
        try {
            if( thread instanceof DDEThread ) {
                TimeKeeper timeKeeper =
                        ((DDEThread)thread).getTimeKeeper();
            
                if( isInsideBoundary() ) {
                    timeKeeper.setOutputTime( event.getTime() );
                    return token;
                } 
                
                else if( isOutsideBoundary() ) {
                    timeKeeper.setCurrentTime( event.getTime() );
                } 
                
                else {
                    timeKeeper.setCurrentTime( event.getTime() );
                    timeKeeper.setOutputTime( timeKeeper.getCurrentTime() );
                }
            }
        } catch( IllegalActionException e ) {
            // FIXME: Do Something
        }

        // Call updateRcvrList() even if _queue.size() == 0,
        // so that the triple is no longer in front.
        if( thread instanceof DDEThread ) {
            TimeKeeper timeKeeper =
                ((DDEThread)thread).getTimeKeeper();

	    if( !isInsideBoundary() && !isOutsideBoundary() ) {
		if( !timeKeeper.searchingForIgnoredTokens() ) {
		    timeKeeper.setSearchForIgnoredTokens( true );
		    timeKeeper.updateIgnoredReceivers();
		}
		if( !timeKeeper.searchingForIgnoredTokens() ) {
		    timeKeeper.updateRcvrList(this);
		}
	    } else if( !isInsideBoundary() ) {
		timeKeeper.updateRcvrList(this);
	    }
        }


	/*
        // Call updateRcvrList() even if _queue.size() == 0,
        // so that the triple is no longer in front.
        if( thread instanceof DDEThread ) {
            TimeKeeper timeKeeper =
                ((DDEThread)thread).getTimeKeeper();

            if( !timeKeeper.searchingForIgnoredTokens() ) {
                timeKeeper.setSearchForIgnoredTokens( true );
                timeKeeper.updateIgnoredReceivers();
            }
            if( !timeKeeper.searchingForIgnoredTokens() ) {
                timeKeeper.updateRcvrList(this);
            }
        }
	*/
        return token;
    }

    /** Get the queue capacity of this receiver.
     * @return The capacity of this receiver's queue.
     */
    public int getCapacity() {
        return _queue.getCapacity();
    }

    /** Return the IOPort container of this receiver.
     * @return IOPort The containing IOPort.
     */
    public IOPort getContainer() {
        return _container;
    }

    /** Return the last time of this receiver. The last time is
     *  the time associated with the token that was most recently
     *  placed in the queue. If no tokens have been placed on the
     *  queue, then the last time equals 0.0.
     * @return double The last time of this TimedQueueReceiver.
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

    /** Return the receiver time of this receiver. The receiver
     *  time is the time stamp associated with the oldest token that
     *  is currently on the queue. If the queue is empty, then the
     *  receiver time value is equal to the "last time."
     * @return double The receiver time of this TimedQueueReceiver.
     */
    public double getRcvrTime() {
        return _rcvrTime;
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

    /** Return true if this receiver is connected to the inside of a 
     *  boundary port. A boundary port is an opaque port that is contained 
     *  by a composite actor. If this receiver is connected to the inside 
     *  of a boundary port, then return true. Otherwise return false. 
     *  Note that this method will return false if this receiver is 
     *  contained in a boundary port.
     *  This method is not synchronized so the caller should be.
     * @return True if this receiver is connected to the inside of a 
     *  boundary port; return false otherwise.
     */
     public boolean isConnectedToBoundary() {
         IOPort innerPort = (IOPort)getContainer();
         if( innerPort == null ) {
             return false;
         }
         ComponentEntity innerEntity = 
                 (ComponentEntity)innerPort.getContainer(); 

         Port outerPort = null; 
         Enumeration enum = innerPort.connectedPorts(); 
         ComponentEntity outerEntity = null; 
         while( enum.hasMoreElements() ) {
             outerPort = (Port)enum.nextElement();
             outerEntity = (ComponentEntity)outerPort.getContainer();
             // if( !outerEntity.isAtomic() && outerPort.isOpaque() ) {
             if( outerEntity == innerEntity.getContainer() ) {
		 // We are connected to a boundary port. Now
		 // determine if the boundary port is connected
		 // to this relation.
                 try {
		 Receiver[][] rcvrs = 
                         ((IOPort)outerPort).deepGetReceivers();
		 for( int i = 0; i < rcvrs.length; i++ ) {
		     for( int j = 0; j < rcvrs[i].length; j++ ) {
		         if( this == rcvrs[i][j] ) {
			     return true;
			 }
		     }
		 }
                 } catch( IllegalActionException e) {
                     // FIXME: Do Something!
                 }
             }
         }
         return false;
     }
     
    /** Return true if this receiver is contained on the inside of a 
     *  boundary port. A boundary port is an opaque port that is 
     *  contained by a composite actor. If this receiver is contained 
     *  on the inside of a boundary port then return true. Otherwise 
     *  return false. This method is not synchronized so the caller 
     *  should be.
     * @return True if this receiver is contained on the inside of
     *  a boundary port; return false otherwise.
     */
     public boolean isInsideBoundary() {
         IOPort innerPort = (IOPort)getContainer();
         if( innerPort == null ) {
             return false;
         }
         ComponentEntity innerEntity = 
                 (ComponentEntity)innerPort.getContainer(); 
         if( !innerEntity.isAtomic() && innerPort.isOpaque() ) {
             // This receiver is contained by the port 
             // of a composite actor.
             if( innerPort.isOutput() && !innerPort.isInput() ) {
                 return true;
             } else if( !innerPort.isOutput() && innerPort.isInput() ) {
                 return false;
             } else if( !innerPort.isOutput() && !innerPort.isInput() ) {
                 return false;
             } else {
                 // FIXME: The following only works if the port is not 
                 // both an input and output.
                 throw new IllegalArgumentException("A port that is "
                         + "both an input and output can not be " 
                         + "properly dealt with by "
                         + "DDEReceiver.isInsideBoundary");
             }
         } 
         return false;
     }

    /** Return true if this receiver is contained on the inside of a 
     *  boundary port. A boundary port is an opaque port that is 
     *  contained by a composite actor. If this receiver is contained 
     *  on the inside of a boundary port then return true. Otherwise 
     *  return false. This method is not synchronized so the caller 
     *  should be.
     * @return True if this receiver is contained on the inside of
     *  a boundary port; return false otherwise.
     */
     public boolean isOutsideBoundary() {
         IOPort innerPort = (IOPort)getContainer();
         if( innerPort == null ) {
             return false;
         }
         ComponentEntity innerEntity = 
                 (ComponentEntity)innerPort.getContainer(); 
         if( !innerEntity.isAtomic() && innerPort.isOpaque() ) {
             // This receiver is contained by the port 
             // of a composite actor.
             if( innerPort.isOutput() && !innerPort.isInput() ) {
                 return false;
             } else if( !innerPort.isOutput() && innerPort.isInput() ) {
                 return true;
             } else if( !innerPort.isOutput() && !innerPort.isInput() ) {
                 return false;
             } else {
                 // FIXME: The following only works if the port is not 
                 // both an input and output.
                 throw new IllegalArgumentException("A port that is "
                         + "both an input and output can not be " 
                         + "properly dealt with by "
                         + "DDEReceiver.isInsideBoundary");
             }
         } 
         return false;
     }

    /** Put a token on the queue with the specified time stamp and set
     *  the last time value to be equal to this time stamp. If the
     *  queue is empty immediately prior to putting the token on
     *  the queue, then set the receiver time value to be equal to the
     *  last time value. If the queue is full, throw a NoRoomException.
     * @param token The token to put on the queue.
     * @param time The time stamp of the token.
     * @exception NoRoomException If the queue is full.
     */
    public void put(Token token, double time) throws NoRoomException {
	if( time < _lastTime && time != INACTIVE && time != IGNORE ) {
	    if( token instanceof NullToken ) {
		return;
	    }
	    IOPort port = (IOPort)getContainer();
	    NamedObj actor = (NamedObj)port.getContainer();
	    // Note: Maintain the following IllegalArgumentException
	    // message as it is used by DDEIOPort.send().
	    throw new IllegalArgumentException(actor.getName() +
		    " - Attempt to set current time in the past.");
	}
        Event event;
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

    ///////////////////////////////////////////////////////////////////
    ////                  package friendly variables 		   ////

    // This time value indicates that the receiver contents should
    // be ignored.
    static final double IGNORE = -1.0;

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods                ////

    /** Return the completion time of this receiver.
     * @return double The completion time.
     */
    synchronized double getCompletionTime() {
        return _completionTime;
    }

    /** Return true if this receiver has a NullToken at the front
     *  of the queue; return false otherwise.
     * @return True if this receiver contains a NullToken in the
     *  oldest queue position; return false otherwise.
     */
    synchronized boolean hasNullToken() {
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
     */
    public void reset() {
        DDEDirector director = (DDEDirector)
            ((Actor)getContainer().getContainer()).getDirector();
	double time = director.getCurrentTime();
	_rcvrTime = time;
	_lastTime = time;
	_queue = new FIFOQueue();
    }

    /** Set the completion time of this receiver. If the
     *  completion time argument is negative but is not
     *  equal to TimedQueueReceiver.ETERNITY, then throw
     *  an IllegalArgumentException.
     * @param time The completion time of this receiver.
     */
    void setCompletionTime(double time) {
	if( time < 0.0 && time != TimedQueueReceiver.ETERNITY ) {
	    throw new IllegalArgumentException("Attempt to set "
            	    + "completion time to a negative value.");
	}
        _completionTime = time;
    }

    /** Set the priority of this receiver.
     * @param int The priority of this receiver.
     */
    synchronized void setPriority(int priority) {
        _priority = priority;
    }

    /** Set the receiver time of this receiver to the specified
     *  value. If this queue is not empty, then the receiver
     *  time will not be set to the specified value.
     * @param time The new rcvr time.
     */
    synchronized void setRcvrTime(double time) {
	if( !(_queue.size() > 0) ) {
            _rcvrTime = time;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The time stamp of the newest token to be placed in the queue.
    private double _lastTime = 0.0;

    // The time stamp of the earliest token that is still in the queue.
    private double _rcvrTime = 0.0;

    // The time after which execution of this receiver will cease.
    private double _completionTime = ETERNITY;

    // The priority of this receiver.
    private int _priority = 0;

    // The queue in which this receiver stores tokens.
    // FIXME
    FIFOQueue _queue = new FIFOQueue();

    // The IOPort which contains this receiver.
    private IOPort _container;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // An Event is an aggregation consisting of a Token, a
    // time stamp and destination Receiver. Both the token
    // and destination receiver are allowed to have null
    // values. This is particularly useful in situations
    // where the specification of the destination receiver
    // may be considered redundant.

    // FIXME
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
