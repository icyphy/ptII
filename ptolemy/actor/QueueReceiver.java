/* A queue with optional capacity and history.

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

@ProposedRating Red (eal@eecs.berkeley.edu)

*/

package pt.actors;

import pt.kernel.*;
import pt.data.*;
import collections.LinkedList;
import collections.CollectionEnumeration;

//////////////////////////////////////////////////////////////////////////
//// FlowFifoQ
/** 
A first-in, first-out (FIFO) queue with optional capacity and
history. Objects are appended to the queue with the put() method,
and removed from the queue with the take() method. The object
removed is the oldest one in the queue. By default, the capacity is
unbounded, but it can be set to any nonnegative size. If the history
capacity is greater than zero (or infinite, indicated by a capacity
of -1), then objects removed from the queue are transfered to a
second queue rather than simply deleted. By default, the history
capacity is zero.

@author Edward A. Lee
@version $Id$
*/
public class FlowFifoQ extends NamedObj implements Receiver {

    /** Construct an empty queue with the specified container.
     */	
    public FlowFifoQ(IOPort container) {
        super();
	_container = container;
	_queue = new FIFOQueue(this);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return an element on the queue.  If the offset argument is
     *  zero, return the most recent object that was put on the queue.
     *  If the offset is 1, return second most recent the object, etc.
     *  If there is no such element on the queue (the offset is greater
     *  than or equal to the size, or is negative), throw an exception.
     */	
    public Token get() { 
        synchronized (workspace()) {
	    return (Token)_queue.take();
	}
    }

    /** Does not remove the element
     *  @exception NoSuchItemException The offset is out of range.
     */
    public Token get(int offset) 
            throws NoSuchItemException {
	synchronized (workspace()) {
	    return (Token)_queue.get(offset);
	}
    }
      

    /** Return the container of the queue, or null if there is none.
     */	
    public Nameable getContainer() {
        return _container;
    }

    /** Returns the FIFOQueue */
    public FIFOQueue getQueue() {
        return _queue;
    }


    /** Put an object on the queue and return true if this will not
     *  cause the capacity to be exceeded.  Otherwise, do not put
     *  the object on the queue and return false.
     *  @param element An object to put on the queue.
     *  @return A boolean indicating success.
     */	
    public void put(Token element) throws TokenHolderFullException {
        synchronized (workspace()){
	    if (_queue.size() >= _queue.capacity()) {
	        throw new TokenHolderFullException();
	    }
	    _queue.put(element);
	}

    }



    private FIFOQueue _queue;
    private IOPort _container;
}





