/* A FIFO queue receiver with variable capacity and optional history.

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
package ptolemy.actor;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import ptolemy.actor.util.FIFOQueue;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
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
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (liuj)
 @see ptolemy.actor.util.FIFOQueue
 */
public class QueueReceiver extends AbstractReceiver {
    /** Construct an empty receiver with no container.
     */
    public QueueReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of the receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public QueueReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver of any contained tokens.
     */
    @Override
    public void clear() {
        _queue.clear();
    }

    /** List the tokens in the receiver, beginning with the oldest.
     *  @return An enumeration of tokens.
     */
    @Override
    public List<Token> elementList() {
        return _queue.elementList();
    }

    /** Enumerate the tokens in the receiver, beginning with the oldest.
     *  @deprecated Used elementList() instead.
     *  @return An enumeration of tokens.
     */
    @Deprecated
    public Enumeration elements() {
        return Collections.enumeration(elementList());
    }

    /** Remove the first token (the oldest one) from the receiver and
     *  return it. If there is no token in the receiver, throw an
     *  exception.
     *  @return The oldest token in the receiver.
     *  @exception NoTokenException If there is no token in the receiver.
     */
    @Override
    public Token get() {
        Token t = null;

        try {
            t = (Token) _queue.take();
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
     *   history.
     *  @exception NoTokenException If the offset is out of range.
     */
    public Token get(int offset) {
        try {
            return (Token) _queue.get(offset);
        } catch (NoSuchElementException ex) {
            throw new NoTokenException(getContainer(), "Offset " + offset
                    + " out of range with " + _queue.size()
                    + " tokens in the receiver and " + _queue.historySize()
                    + " in history.");
        }
    }

    /** Return the capacity, or INFINITE_CAPACITY if it is unbounded.
     *  @return The capacity of the receiver.
     *  @see #setCapacity(int)
     */
    public int getCapacity() {
        return _queue.getCapacity();
    }

    /** Return the capacity of the history queue.
     *  This will be zero if the history mechanism is disabled
     *  and INFINITE_CAPACITY if the history capacity is unbounded.
     *  @return The capacity of the history queue.
     *  @see #setHistoryCapacity(int)
     */
    public int getHistoryCapacity() {
        return _queue.getHistoryCapacity();
    }

    /** Return true if the next call to put() will succeed without
     *  a NoRoomException.
     *  @return True if the queue has room for one more token.
     */
    @Override
    public boolean hasRoom() {
        return !_queue.isFull();
    }

    /** Return true if the queue has room to put the given number of
     *  tokens into it (via the put() method).
     *  @param numberOfTokens The number of tokens to put into the queue.
     *  @return True if the queue has room for the specified number of tokens.
     *  @exception IllegalArgumentException If the number of tokens is less
     *   than one.  This is a runtime exception, and hence does not need to
     *   be explicitly declared by the caller.
     */
    @Override
    public boolean hasRoom(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "The number of tokens must be greater than 0");
        }

        return _queue.size() + numberOfTokens < _queue.getCapacity();
    }

    /** Return true if the next call to get() will succeed without a
     *  a NoTokenException.
     *  @return True if the queue has at least one token in it.
     */
    @Override
    public boolean hasToken() {
        return _queue.size() > 0;
    }

    /** Return true if the specified number of tokens is available in the
     *  queue.
     *  @param numberOfTokens The number of tokens to get from the queue.
     *  @return True if the specified number of tokens is available.
     *  @exception IllegalArgumentException If the number of tokens is less
     *   than one.  This is a runtime exception, and hence does not need to
     *   be explicitly declared by the caller.
     */
    @Override
    public boolean hasToken(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "The number of tokens must be greater than 0");
        }

        return _queue.size() >= numberOfTokens;
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
    @Deprecated
    public List historyElementList() {
        return _queue.historyElementList();
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
    @Deprecated
    public Enumeration historyElements() {
        return Collections.enumeration(historyElementList());
    }

    /** Return the number of tokens in history.
     *  @return The number of tokens in history.
     */
    public int historySize() {
        return _queue.historySize();
    }

    /** Put a token to the receiver. If the receiver is full, throw an
     *  exception. If the argument is null, do nothing.
     *  @param token The token to be put to the receiver.
     *  @exception NoRoomException If the receiver is full.
     */
    @Override
    public void put(Token token) {
        if (token == null) {
            return;
        }
        if (!_queue.put(token)) {
            throw new NoRoomException(getContainer(),
                    "Queue is at capacity. Cannot put a token.");
        }
    }

    /** Set receiver capacity. Use INFINITE_CAPACITY to indicate unbounded
     *  capacity (which is the default). If the number of tokens currently
     *  in the receiver exceeds the desired capacity, throw an exception.
     *  @param capacity The desired receiver capacity.
     *  @exception IllegalActionException If the receiver has more tokens
     *   than the proposed capacity or the proposed capacity is illegal.
     *  @see #getCapacity()
     */
    public void setCapacity(int capacity) throws IllegalActionException {
        try {
            _queue.setCapacity(capacity);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(getContainer(), ex,
                    "Failed to set capacity to " + capacity);
        }
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
     *  @see #getHistoryCapacity()
     */
    public void setHistoryCapacity(int capacity) throws IllegalActionException {
        try {
            _queue.setHistoryCapacity(capacity);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(getContainer(), ex,
                    "Failed to setHistoryCapacity to " + capacity);
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

    /** Used to indicate that the size of this queue receiver is infinite.
     */
    public static final int INFINITE_CAPACITY = FIFOQueue.INFINITE_CAPACITY;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** This is the queue in which data is stored.
     */
    protected FIFOQueue _queue = new FIFOQueue();
}
