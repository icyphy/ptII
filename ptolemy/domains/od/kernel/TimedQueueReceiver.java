/* A FIFO queue for storing tokens with time stamps.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

*/

package ptolemy.domains.od.kernel;

import ptolemy.kernel.*; // For ComponentEntity
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;

//////////////////////////////////////////////////////////////////////////
//// TimedQueueReceiver
/**
A FIFO queue receiver for storing tokens with time stamps. A "time stamp"
is a time value that is associated with a token and is used to order the
consumption of a token with respect to other time stamped tokens. To help 
organize the tokens contained by this queue, two flags are maintained: 
"lastTime" and "rcvrTime." The lastTime flag is defined to be equivalent 
to the time stamp of the token that was most recently placed in the queue. 
The rcvrTime flag is defined as the time stamp of the oldest token in the 
queue. Both of these flags must have monotonically non-decreasing values 
with the exception that their values will be set to -1.0 at the conclusion 
of a simulation run. 

***
Synchronization Notes:
***
This domain observes a hierarchy of synchronization locks. When multiple
synchronization locks are required, they must be obtained in an order that
is consistent with this hierarchy. Adherence to this hierarchical ordering
ensures that deadlock can not occur due to circular lock dependencies.
 
The following synchronization hierarchy is utilized:
 
        1. read/write access on the workspace
        2. synchronization on the receiver
        3. synchronization on the director
        4. synchronization on the actor
        5. (other) synchronization on the workspace
 
We say that lock #1 is at the highest level in the hierarchy and lock #5
is at the lowest level.
 
As an example, a method that synchronizes on a receiver can not contain
read/write access on the workspace; such accesses must occur outside of
the receiver synchronization. Similarly, a method which synchronizes on a
director must not synchronize on the receiver or contain read/write
accesses on the workspace; it can contain synchronizations on actors or
the workspace.
 
The justification of the chosen ordering of this hierarchy is based on
the access a method has to the fields of its object versus the fields of
other objects. The more (less) a method focuses on the internal state of
its object and non-synchronized methods of external objects, the lower
(higher) the method is placed in the synchronization hierarchy. In the
case of read/write access on the workspace, the corresponding methods,
i.e, getReadAccess() and getWriteAccess(), access the current thread
running in the JVM. This external access deems these methods as being at
the top of the hierarchy. All other synchronizations on the workspace only
focus on the internal state of the workspace and hence are at the bottom
of the synchronization hierarchy.


@author John S. Davis II
@version @(#)TimedQueueReceiver.java	1.17	11/18/98
@see ptolemy.domains.od.kernel.ODReceiver
*/

public class TimedQueueReceiver implements Receiver {

    /** Construct an empty queue with no container.
     */
    public TimedQueueReceiver() {
        super();
    }

    /** Construct an empty queue with the specified IOPort container.
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
     *  token. Update the RcvrTimeTriple entry in the ODActor which 
     *  contains this receiver.
     * @exception NoTokenException If the queue is empty.
     * @see ptolemy.domains.od.kernel.RcvrTimeTriple
     */
    public Token get() {
        // System.out.println("Call to TimedQueueReceiver.get()");
        // System.out.println("Previous rcvrTime = " + getRcvrTime() );
        // System.out.println("rcvrTime = " + getRcvrTime() );
        ODActor actor = (ODActor)getContainer().getContainer();
        // System.out.println("actor time = " + actor.getCurrentTime() );
	Token token = null;
	synchronized( this ) {
            Event event = (Event)_queue.take(); 
	    if (event == null) {
                throw new NoTokenException(getContainer(), 
	                "Attempt to get token from an empty FIFO queue.");
            } 
	    token = event.getToken(); 

	    // Set the rcvr time based on the next token 
	    if( getSize() > 0 ) {
                // System.out.println("Size after get is " + getSize()); 
	        Event nextEvent = (Event)_queue.get(0); 
	        _rcvrTime = nextEvent.getTime(); 
	        // System.out.println("Update via get(): _rcvrTime = " 
                // + _rcvrTime );
            } 

	    // Call update even if getSize == 0, so that triple is 
	    // no longer in front 

	    RcvrTimeTriple triple; 
	    triple = new RcvrTimeTriple( this, _rcvrTime, _priority ); 
	    // triple = new RcvrTimeTriple( this, _rcvrTime, getPriority() ); 
	    // ODActor actor = (ODActor)getContainer().getContainer(); 
	    actor.updateRcvrList( triple );
            
            /* 
	    // System.out.println(((ComponentEntity)actor).getName()
                + " completed TimedQueueReceiver.get().");
            */
	}
        return token;
    }

    /** Return the queue capacity of this receiver. 
     * @return int The queue capacity.
     * @deprecated
    public int getCapacity() {
        return _queue.getCapacity();
    }
     */

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

    /** Return the priority of this receiver. 
     * @return The priority of this receiver.
     * @deprecated
    public synchronized int getPriority() {
        return _priority;
    }
     */

    /** Get the queue size. 
     *  FIXME: Eventually remove this and access _queue.size() directly
     */
    public int getSize() {
        return _queue.size();
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
     *  RcvrTimeTriple entry in the ODActor which contains this receiver.
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
     *  Update the RcvrTimeTriple entry in the ODActor which contains 
     *  this receiver.
     * @param token The token to put on the queue.
     * @param time The time stamp of the token.
     *  FIXME: Check for completion time here instead of ODReceiver.put()
     */
    public void put(Token token, double time) {
        // System.out.println("Call to TimedQueueReceiver.put()");
        // System.out.println("Previous queue size = " + getSize() );
        Event event;
        ODIOPort port = (ODIOPort)getContainer();
        ODActor actor = (ODActor)port.getContainer();
	// String myName = ((ComponentEntity)actor).getName();
        
        synchronized(this) {
            _lastTime = time; 
            /*
	    if( _lastTime > 19.0 && myName.equals("printer") ) {
                System.out.println("Update: _lastTime = " + _lastTime); 
	    }
            */

            event = new Event(token, _lastTime);
            
            if( getSize() == 0 ) {
                RcvrTimeTriple triple; 
                _rcvrTime = _lastTime; 
                triple = new RcvrTimeTriple( this, _rcvrTime, _priority ); 
                // System.out.println("Update: _rcvrTime = " + _rcvrTime); 
                actor.updateRcvrList( triple ); 
            }

            if (!_queue.put(event)) {
                throw new NoRoomException (getContainer(), 
                        "Queue is at capacity. Cannot insert token.");
            }
            
            /*
            // System.out.println(((ComponentEntity)actor).getName()
                + " completed TimedQueueReceiver.put().");
            */
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The time stamp of the newest token to be placed in the queue.
    private double _lastTime = 0.0;
    
    // The time stamp of the earliest token that is still in the queue.
    private double _rcvrTime = 0.0;

    // The time after which this server will become defunct.
    // FIXME: perhaps this should be package private
    private double _completionTime = -5.0;

    // The priority of this receiver.
    private int _priority = 0;
    
    // The queue in which this receiver stores tokens.
    public FIFOQueue _queue = new FIFOQueue();

    // The IOPort which contains this receiver.
    private IOPort _container;
}
