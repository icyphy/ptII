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

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;

//////////////////////////////////////////////////////////////////////////
//// TimedQueueReceiver
/**
/* A FIFO queue receiver for storing tokens with time stamps.

The "lastTime" of this queue is defined as the time stamp of the newest
token within the queue and is updated every time put() is called. The 
"rcvrTime" of this queue is defined as the time stamp of the oldest token 
within the queue and is called every time get() is called. 

Synchronization Notes:
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
*/
public class TimedQueueReceiver implements Receiver {

    /** Construct an empty queue with no container.
     */
    public TimedQueueReceiver() {
        super();
    }

    /** Construct an empty queue with the specified container.
     */
    public TimedQueueReceiver(IOPort container) {
        super();
	_container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Take the first token (the oldest one) off the queue and return it.
     *  If the queue is empty, throw an exception. If there are other
     *  tokens left on the queue, set the rcvr time to equal that of the
     *  new leading token.
     * @exception NoTokenException If the queue is empty.
     */
    public Token get() {
        // System.out.println("Call to TimedQueueReceiver.get()");
        // System.out.println("Previous rcvrTime = " + getRcvrTime() );
        // System.out.println("rcvrTime = " + getRcvrTime() );
        ODActor odactor = (ODActor)getContainer().getContainer();
        // System.out.println("actor time = " + odactor.getCurrentTime() );
        Event event = (Event)_queue.take();
        if (event == null) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get token from an empty FIFO queue.");
        }
        Token token = event.getToken();
        
        // Set the rcvr time based on the next token
        if( getSize() > 0 ) {
            // System.out.println("Size after get is " + getSize());
            // FIXME: get gives FIFO info now
            // Event nextEvent = (Event)_queue.get( _queue.size() - 1 ); 
            Event nextEvent = (Event)_queue.get(0); 
            _rcvrTime = nextEvent.getTime();
            // System.out.println("Update via get(): _rcvrTime = " + _rcvrTime );
            // FIXME We should update the actor rcvrtripletable here 
        }
        // Call update even if getSize == 0, so that triple is 
        // no longer in front
            
        RcvrTimeTriple triple; 
        triple = new RcvrTimeTriple( this, _rcvrTime, getPriority() ); 
        ODActor actor = (ODActor)getContainer().getContainer(); 
        actor.updateRcvrTable( triple );
            
        /*
        // System.out.println(((ComponentEntity)odactor).getName()
                + " completed TimedQueueReceiver.get().");
        */
        return token;
    }

    /** Get the queue capacity. 
     */
    public int getCapacity() {
        return _queue.getCapacity();
    }

    /** Return the container. 
     */
    public IOPort getContainer() {
        return _container;
    }

    /** 
     */
    public double getLastTime() {
        return _lastTime;
    }

    /** Get the priority of this receiver. 
     */
    public synchronized int getPriority() {
        return _priority;
    }

    /** Get the queue size. 
     *  FIXME: Make this private
     */
    public int getSize() {
        return _queue.size();
    }

    /** Set the priority of this receiver. 
     */
    public synchronized void setPriority(int priority) {
        _priority = priority;
    }

    /** 
     */
    public double getRcvrTime() {
        return _rcvrTime;
    }

    /** Return true if put() will succeed in accepting a token. 
     */
    public boolean hasRoom() {
        return !_queue.isFull();
    }

    /** Return true if get() will succeed in returning a token. 
     */
    public boolean hasToken() {
        return _queue.size() > 0;
    }

    /** Put a token on the queue with a time stamp equivalent to the
     *  current rcvr time of the Receiver. If the queue is full, throw an 
     *  exception. Set the last time of the queue to equal the time of
     *  the newly added token.
     * @param token The token to put on the queue.
     * @exception NoRoomException If the queue is full.
     *  FIXME: I need an IllegalActionException here to deal with
     *         time stamps that are decreasing.
     */
    public void put(Token token) {
        put( token, _lastTime );
    }

    /** Put a token on the queue with a specified time stamp and set 
     *  the time of the Receiver to the time stamp value. If the 
     *  queue is full, throw an exception. Set the last time of the 
     *  queue to equal the time of the newly added token.
     * @param token The token to put on the queue.
     * @param time The time stamp of the token.
     *  FIXME: I need an IllegalActionException here to deal with
     *         time stamps that are decreasing.
     */
    public void put(Token token, double time) {
        // System.out.println("Call to TimedQueueReceiver.put()");
        // System.out.println("Previous queue size = " + getSize() );
        Event event = new Event(token, time);
        ODIOPort port = (ODIOPort)getContainer();
        ODActor actor = (ODActor)port.getContainer();
        
        synchronized(this) {
            _lastTime = time; 
            
            if( getSize() == 0 ) {
                RcvrTimeTriple triple; 
                triple = new RcvrTimeTriple( this, time, _priority ); 
                _rcvrTime = time; 
                // System.out.println("Update: _rcvrTime = " + _rcvrTime); 
                actor.updateRcvrTable( triple ); 
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

    /** Set the queue capacity. 
     */
    public void setCapacity(int capacity) throws IllegalActionException {
        _queue.setCapacity(capacity);
    }

    /** Set the container. */
    public void setContainer(IOPort port) {
        _container = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods 		   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The time stamp of the last token to be placed in the queue.
    private double _lastTime = 0.0;
    
    // The time stamp of the oldest token that is still in the queue.
    private double _rcvrTime = 0.0;

    private int _priority = 0;
    
    private FIFOQueue _queue = new FIFOQueue();
    private IOPort _container;
}
