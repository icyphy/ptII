/* A FIFO queue receiver with variable capacity and optional history.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (liuj@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.util.*;
import ptolemy.actor.util.FIFOQueue;	/* Needed by javadoc */


import java.util.NoSuchElementException;
import java.util.Enumeration;
import java.util.Collections;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// QueueReceiver
/**
A first-in, first-out (FIFO) queue receiver with variable capacity and
optional history. Tokens are put into the receiver with the put() method,
and removed from the receiver with the get() method. The token removed is
the oldest one in the receiver. By default, the capacity is unbounded, but
it can be set to any nonnegative size. If the history capacity is greater
than zero (or infinite, indicated by a capacity of INFINITE_CAPACITY),
then tokens removed from the receiver are stored in a history queue rather
than simply removed. By default, the history capacity is zero.

@author Edward A. Lee, Lukito Muliadi, Xiaojun Liu
@version $Id$
@see ptolemy.actor.util.FIFOQueue
*/
public class QueueReceiver implements Receiver {

    /** Construct an empty receiver with no container.
     */
    public QueueReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of the receiver.
     */
    public QueueReceiver(IOPort container) {
        super();
	_container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enumerate the tokens in the receiver, beginning with the oldest.
     *  @deprecated Used elementList() instead.
     *  @return An enumeration of tokens.
     */
    public Enumeration elements() {
        return Collections.enumeration(elementList());
    }

    /** List the tokens in the receiver, beginning with the oldest.
     *  @return An enumeration of tokens.
     */
    public List elementList() {
        return _queue.elementList();
    }


    /** Remove the first token (the oldest one) from the receiver and
     *  return it. If there is no token in the receiver, throw an
     *  exception.
     *  @return The oldest token in the receiver.
     *  @exception NoTokenException If there is no token in the receiver.
     */
    public Token get() {
        Token t = null;
        try {
            t = (Token)_queue.take();
        } catch (NoSuchElementException ex) {
            // The queue is empty.
            throw new NoTokenException(getContainer(),
                    "Attempt to get token from an empty QueueReceiver.");
        }
        return t;
    }

    /** Return a token in the receiver or its history. If the offset
     *  argument is zero, return the oldest token in the receiver.
     *  If the offset is 1, return the second oldest token, etc. The
     *  token is not removed from the receiver. If there is no such
     *  token in the receiver (the offset is greater than or equal
     *  to the number of tokens currently in the receiver), throw an
     *  exception. If the offset is -1, return the most recent token
     *  removed from the receiver. If it is -2, return the second
     *  most recent token removed from the receiver, etc. If there is
     *  no such token in the receiver's history (the history capacity
     *  is zero or the absolute value of offset is greater than the
     *  number of tokens currently in the receiver's history), an
     *  exception is thrown.
     *  @param offset The offset from the oldest token in the receiver.
     *  @return The token at the desired offset in the receiver or its
     history.
     *  @exception NoTokenException If the offset is out of range.
     */
    public Token get(int offset) {
        try {
            return (Token)_queue.get(offset);
        } catch (NoSuchElementException ex) {
            throw new NoTokenException(getContainer(),
                    "Offset " + offset + " out of range with " + _queue.size()
                    + " tokens in the receiver and " + _queue.historySize()
                    + " in history.");
        }
    }

    /** Return the capacity, or INFINITE_CAPACITY if it is unbounded.
     *  @return The capacity of the receiver.
     */
    public int getCapacity() {
        return _queue.getCapacity();
    }

    /** Return the container of this receiver, or null if there is none.
     *  @return The IOPort containing this receiver.
     */
    public IOPort getContainer() {
        return _container;
    }

    /** Return the capacity of the history queue.
     *  This will be zero if the history mechanism is disabled
     *  and INFINITE_CAPACITY if the history capacity is unbounded.
     *  @return The capacity of the history queue.
     */
    public int getHistoryCapacity() {
        return _queue.getHistoryCapacity();
    }

    /** Return true if put() will succeed in accepting a token.
     *  @return A boolean indicating whether a token can be put in this
     *   receiver.
     */
    public boolean hasRoom() {
        return !_queue.isFull();
    }

    /** Return true if get() will succeed in returning a token.
     *  @return A boolean indicating whether there is a token in this
     *   receiver.
     */
    public boolean hasToken() {
        return _queue.size() > 0;
    }

    /** Enumerate the tokens stored in the history queue, which are
     *  the N most recent tokens taken from the receiver, beginning with
     *  the oldest, where N is less than or equal to the history capacity.
     *  If the history capacity is INFINITE_CAPACITY, then the enumeration
     *  includes all tokens previously taken from the receiver. If the
     *  history capacity is zero, then return an empty enumeration.
     *  @return An enumeration of tokens.
     *  @deprecated Used historyElementList() instead.
     */
    public Enumeration historyElements() {
        return Collections.enumeration(historyElementList());
    }

    /** List the tokens stored in the history queue, which are
     *  the N most recent tokens taken from the receiver, beginning with
     *  the oldest, where N is less than or equal to the history capacity.
     *  If the history capacity is INFINITE_CAPACITY, then the enumeration
     *  includes all tokens previously taken from the receiver. If the
     *  history capacity is zero, then return an empty enumeration.
     *  @return An enumeration of tokens.
     *  @deprecated Used historyElementList() instead.
     */
    public List historyElementList() {
        return _queue.historyElementList();
    }

    /** Return the number of tokens in history.
     *  @return The number of tokens in history.
     */
    public int historySize() {
        return _queue.historySize();
    }

    /** Reset the state of the receiver. Clear the FIFO queue. This is needed
     *  to restart the simulation
     *  @deprecated Use reset() instead.
     */
    public void initialize() {
        while (_queue.size() > 0) {
            _queue.take();
        }
    }

    /** Put a token to the receiver. If the receiver is full, throw an
     *  exception.
     *  @param token The token to be put to the receiver.
     *  @exception NoRoomException If the receiver is full.
     */
    public void put(Token token) {
        if (!_queue.put(token)) {
            throw new NoRoomException(getContainer(),
                    "Queue is at capacity. Cannot put a token.");
        }
    }

    /** Reset the state of the receiver. Clear the FIFO queue. This is needed
     *  to restart the simulation
     */
    public void reset() {
        while (_queue.size() > 0) {
            _queue.take();
        }
    }

    /** Set receiver capacity. Use INFINITE_CAPACITY to indicate unbounded
     *  capacity (which is the default). If the number of tokens currently
     *  in the receiver exceeds the desired capacity, throw an exception.
     *  @param capacity The desired receiver capacity.
     *  @exception IllegalActionException If the receiver has more tokens
     *   than the proposed capacity or the proposed capacity is illegal.
     */
    public void setCapacity(int capacity) throws IllegalActionException {
        try {
            _queue.setCapacity(capacity);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(getContainer(), ex.getMessage());
        }
    }

    /** Set the container.
     *  @param port The IOPort containing this receiver.
     */
    public void setContainer(IOPort port) {
        _container = port;
    }

    /** Set the capacity of the history queue. Use 0 to disable the
     *  history mechanism and INFINITE_CAPACITY to make the history
     *  capacity unbounded. If the size of the history queue exceeds
     *  the desired capacity, then remove the oldest tokens from the
     *  history queue until its size equals the proposed capacity.
     *  Note that this can be used to clear the history queue by
     *  supplying 0 as the argument.
     *  @param capacity The desired history capacity.
     *  @exception IllegalActionException If the desired capacity is illegal.
     */
    public void setHistoryCapacity(int capacity)
            throws IllegalActionException {
        try {
            _queue.setHistoryCapacity(capacity);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(getContainer(), ex.getMessage());
        }
    }

    /** Return the number of tokens in the receiver.
     *  @return The number of tokens in the receiver.
     */
    public int size() {
        return _queue.size();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static final int INFINITE_CAPACITY =
    FIFOQueue.INFINITE_CAPACITY;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private FIFOQueue _queue = new FIFOQueue();
    private IOPort _container;
}
