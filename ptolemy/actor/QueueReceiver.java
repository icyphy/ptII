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

@ProposedRating Yellow (eal@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.util.*;

import collections.LinkedList;
import java.util.NoSuchElementException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// QueueReceiver
/**
A first-in, first-out (FIFO) queue with optional capacity and
history. Objects are appended to the queue with the put() method,
and removed from the queue with the take() method. The object
removed is the oldest one in the queue. By default, the capacity is
unbounded, but it can be set to any nonnegative size. If the history
capacity is greater than zero (or infinite, indicated by a capacity
of -1), then objects removed from the queue are transferred to a
second queue rather than simply deleted. By default, the history
capacity is zero.

@author Edward A. Lee
@version $Id$
*/
public class QueueReceiver implements Receiver {

    /** Construct an empty queue with no container.
     */
    public QueueReceiver() {
        super();
    }

    /** Construct an empty queue with the specified container.
     */
    public QueueReceiver(IOPort container) {
        super();
	_container = container;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the capacity, or -1 if it is unbounded.
     */
    public int capacity() {
        return _queue.capacity();
    }

    /** Enumerate the tokens on the queue, beginning with the oldest.
     *  @return An enumeration of Tokens.
     */
    public Enumeration elements() {
        return _queue.elements();
    }

    /** Take the first token (the oldest one) off the queue and return it.
     *  If the queue is empty, throw an exception.
     */
    public Token get() throws NoSuchItemException {
        Token t = (Token)_queue.take();
        if (t == null) {
            throw new NoSuchItemException(getContainer(),
                    "Attempt to get data from an empty FIFO queue.");
        }
        return t;
    }

    /** Return a token on the queue.  If the offset argument is
     *  zero, return the most recent token that was put on the queue.
     *  If the offset is 1, return second most recent the token, etc.
     *  Do not remove the token from the queue.
     *  If there is no such token on the queue (the offset is greater
     *  than or equal to the size, or is negative), throw an exception.
     *
     *  @param offset The offset from the most recent item on the queue.
     *  @exception NoSuchItemException The offset is out of range.
     */
    public Token get(int offset) throws NoSuchItemException {
        try {
            return (Token)_queue.get(offset);
        } catch (NoSuchElementException ex) {
            throw new NoSuchItemException(getContainer(),
                    "Offset " + offset + " out of range in queue of size " +
                    _queue.size());
        }
    }


    /** Return the container of this receiver, or null if there is none.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Return true if put() will succeed in accepting a token. */
    public boolean hasRoom() {
        return !_queue.full();
    }

    /** Return true if get() will succeed in returning a token. */
    public boolean hasToken() {
        return _queue.size() > 0;
    }

    /** Enumerate the items stored in the history queue, which are
     *  the N most recent items taken from the queue, beginning with
     *  the oldest, where N is less than or equal to the history capacity.
     *  If the history capacity is -1, then the enumeration includes all
     *  items previously taken from the queue.  If the history capacity
     *  is zero, then return an empty enumeration.
     *
     *  @return An enumeration of Tokens.
     */
    public Enumeration history() {
        return _queue.history();
    }

    /** Return the capacity of the history queue.
     *  This will be zero if the history mechanism is disabled
     *  and -1 if the history capacity is unbounded.
     */
    public int historyCapacity() {
        return _queue.historyCapacity();
    }

    /** Return the number of objects in the history.
     */
    public int historySize() {
        return _queue.historySize();
    }

    /** Return an element from the history.  If the offset argument is
     *  zero, return the most recent token in the history, which is
     *  token most recently taken from the queue.
     *  If the offset is 1, return the second most recent token, etc.
     *  If there is no such token in the history (the offset is greater
     *  than or equal to the number of objects in the history, or is
     *  negative), throw an exception.
     *  @exception NoSuchItemException The offset is out of range.
     */
    public Token previous(int offset) throws NoSuchItemException {
        try {
            return (Token)_queue.previous(offset);
        } catch (NoSuchElementException ex) {
            throw new NoSuchItemException(getContainer(),
                    "Offset " + offset + " out of range in history queue of " +
                    "size " + historySize());
        }
    }

    /** Put a token on the queue.  If the queue is full, throw an exception.
     *  @param token The token to put on the queue.
     *  @exception IllegalActionException If the queue is full.
     */
    public void put(Token token) throws IllegalActionException {
        if (!_queue.put(token)) {
            throw new IllegalActionException(getContainer(),
            "Queue is at capacity. Cannot put a token.");
        }
    }

    /** Set the capacity.  Use -1 to indicate unbounded capacity
     *  (which is the default).  If the size of the queue exceeds the
     *  desired capacity, throw an exception.
     *  @exception IllegalActionException Queue contains more elements
     *   than the proposed capacity.
     */
    public void setCapacity(int capacity) throws IllegalActionException {
        _queue.setCapacity(capacity);
    }

    /** Set the capacity of the history queue.
     *  Use 0 to disable the history mechanism
     *  and -1 to make the history capacity unbounded.
     *  If the size of the history list exceeds the
     *  desired capacity, then remove the oldest items from
     *  the history list until its size equals the proposed capacity.
     *  Note that this can be used to clear the history list.
     */
    public void setHistoryCapacity(int capacity) {
        _queue.setHistoryCapacity(capacity);
    }

    /** Set the container. */
    public void setContainer(IOPort port) {
        _container = port;
    }

    /** Return the number of objects in the queue.
     */
    public int size() {
        return _queue.size();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private FIFOQueue _queue = new FIFOQueue();
    private IOPort _container;
}
