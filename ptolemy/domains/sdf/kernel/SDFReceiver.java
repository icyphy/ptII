/* A FIFO queue receiver with variable capacity and optional history.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Green (neuendor@eecs.berkeley.edu)

*/

package ptolemy.domains.sdf.kernel;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

import java.util.Enumeration;
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// SDFReceiver
/**
A first-in, first-out (FIFO) queue receiver with variable capacity and
optional history. Tokens are put into the receiver with the put() method,
and removed from the receiver with the get() method. The token removed is
the one placed in the receiver before any other (i.e. the "oldest", although
this has nothing to do with time in the model.
By default, the capacity is unbounded, but
it can be set to any nonnegative size. If the history capacity is greater
than zero (or infinite, indicated by a capacity of INFINITE_CAPACITY),
then tokens removed from the receiver are stored in a history queue rather
than simply removed. By default, the history capacity is zero.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
@see ArrayFIFOQueue
*/
public class SDFReceiver extends AbstractReceiver {

    /** Construct an empty receiver with no container.
     */
    public SDFReceiver() {
        super();
        _queue = new ArrayFIFOQueue();
    }

    /** Construct an empty receiver with no container and given size.
     *  @param size The size of the queue in the receiver.
     */
    public SDFReceiver(int size) {
        super();
        _queue = new ArrayFIFOQueue(size);
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of the receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public SDFReceiver(IOPort container) throws IllegalActionException {
        super(container);
        _queue = new ArrayFIFOQueue();
    }

    /** Construct an empty receiver with the specified container and size.
     *  @param container The container of the receiver.
     *  @param size The size of the queue in the receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public SDFReceiver(IOPort container, int size)
            throws IllegalActionException {
        super(container);
        _queue = new ArrayFIFOQueue(size);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver of any contained tokens.
     */
    public void clear() {
        _queue.clear();
    }

    /** Remove the first token (the oldest one) from the receiver and
     *  return it. If there is no token in the receiver, throw an
     *  exception.
     *  @return The oldest token in the receiver.
     *  @exception NoTokenException If there is no token in the receiver.
     */
    public Token get() {
        if (_queue.isEmpty()) {
            // The queue is empty.
            throw new NoTokenException(getContainer(),
                    "Attempt to get token from an empty QueueReceiver.");
        }
        return (Token)_queue.take();
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
            return (Token)_queue.get(offset);
        } catch (NoSuchElementException ex) {
            throw new NoTokenException(getContainer(),
                    "Offset " + offset + " out of range with " + _queue.size()
                    + " tokens in the receiver and " + _queue.historySize()
                    + " in history.");
        }
    }

    /** Get an array of tokens from this receiver. The parameter
     *  specifies the number of valid tokens to get in the returned
     *  array. The length of the returned array will be at least equal to
     *  <i>count</i>. This method may sometimes return an array with
     *  length greater than <i>count</i>, in which case, only the first
     *  <i>count</i> elements are valid. This behavior is allowed so that
     *  this method can choose to reallocate the returned token array
     *  only when the vector length is increased.
     *
     *  @param count The number of valid tokens to get in the
     *   returned array.
     *  @return An array containing <i>count</i> tokens from the
     *   receiver.
     *  @exception NoTokenException If there are not <i>count</i>
     *   tokens.
     */
    public Token[] getArray(int count) {
        // Check if we need to reallocate the cached
        // token array.
        if (_tokenArray == null || count != _tokenArray.length) {
            // Reallocate token array.
            _tokenArray = new Token[count];
        }
        _queue.takeArray(_tokenArray, count);
        return _tokenArray;
    }

    /** Return the capacity, or INFINITE_CAPACITY if it is unbounded.
     *  @return The capacity of the receiver.
     */
    public int getCapacity() {
        return _queue.getCapacity();
    }

    /** Return the capacity of the history queue.
     *  This will be zero if the history mechanism is disabled
     *  and INFINITE_CAPACITY if the history capacity is unbounded.
     *  @return The capacity of the history queue.
     *  @see #historyElements()
     *  @see #historySize()
     *  @see #setHistoryCapacity(int)
     */
    public int getHistoryCapacity() {
        // As of Ptolemy II 2.0.1, we are not using the SDFReceiver.*History*
        // methods, but these are here for future use.
        return _queue.getHistoryCapacity();
    }

    /** Return true if put() will succeed in accepting a token.
     *  @return A boolean indicating whether a token can be put in this
     *   receiver.
     */
    public boolean hasRoom() {
        return !_queue.isFull();
    }

    /** Return true if put() will succeed in accepting the specified
     *  number of tokens.
     *  @param tokens The number of tokens.
     *  @return A boolean indicating whether a token can be put in this
     *   receiver.
     *  @exception IllegalArgumentException If the argument is less
     *   than one.  This is a runtime exception, so it need not be
     *   declared explicitly by the caller.
     */
    public boolean hasRoom(int tokens) throws IllegalArgumentException {
        if (tokens < 1) {
            throw new IllegalArgumentException("The argument "
                    + "must not be negative. It was: " + tokens);
        }
        if (_queue.getCapacity() == INFINITE_CAPACITY) {
            // Queue has infinite capacity, so it can accept any
            // finite number of tokens.
            return true;
        }
        return (_queue.size() + tokens) <= _queue.getCapacity();
    }

    /** Return true if get() will succeed in returning a token.
     *  @return A boolean indicating whether there is a token in this
     *   receiver.
     */
    public boolean hasToken() {
        return !_queue.isEmpty();
    }

    /** Return true if get() will succeed in returning a token the given
     *  number of times.
     *  @param tokens The number of tokens.
     *  @return A boolean indicating whether there are the given number of
     *  tokens in this receiver.
     *  @exception IllegalArgumentException If the argument is less
     *   than one.  This is a runtime exception, so it need not be
     *   declared explicitly by the caller.
     */
    public boolean hasToken(int tokens) throws IllegalArgumentException {
        if (tokens < 0) {
            throw new IllegalArgumentException("The argument "
                    + "must not be negative. It was: " + tokens);
        }
        return _queue.size() >= tokens;
    }

    /** Enumerate the tokens stored in the history queue, which are
     *  the N most recent tokens taken from the receiver, beginning with
     *  the oldest, where N is less than or equal to the history capacity.
     *  If the history capacity is INFINITE_CAPACITY, then the enumeration
     *  includes all tokens previously taken from the receiver. If the
     *  history capacity is zero, then return an empty enumeration.
     *  @return An enumeration of tokens.
     *  @see #getHistoryCapacity()
     *  @see #historySize()
     *  @see #setHistoryCapacity(int)
     */
    public Enumeration historyElements() {
        // As of Ptolemy II 2.0.1, we are not using the SDFReceiver.*History*
        // methods, but these are here for future use.
        return _queue.historyElements();
    }

    /** Return the number of tokens in history.
     *  @return The number of tokens in history.
     *  @see #getHistoryCapacity()
     *  @see #historyElements()
     *  @see #setHistoryCapacity(int)
     */
    public int historySize() {
        // As of Ptolemy II 2.0.1, we are not using the SDFReceiver.*History*
        // methods, but these are here for future use.
        return _queue.historySize();
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

    /** Put a specified number of token from an array into the receiver.
     *  If the receiver has insufficient room, throw an
     *  exception, and add none of the tokens to the receiver. An
     *  exception is thrown if <i>count</i> is greater than
     *  the length of the token array.
     *
     *  @param token The token array that contains tokens to be put
     *  into the receiver.
     *  @param count The number of tokens from <i>token</i> to
     *  put in the receiver.  This should be less than the length
     *  of <i>token</i>.
     *  @exception NoRoomException If the receiver is full.
     *  @exception IndexOutOfBoundException If the array does not contain
     *  at least count elements.
     */
    public void putArray(Token token[], int count) {
        // Note: There has been a suggestion that this method also be
        // able to take an offset.  When we figure out how to use this,
        // we should implement it.
        if (!_queue.putArray(token, count)) {
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
     *  @see #historyElements()
     *  @see #historySize()
     */
    public void setHistoryCapacity(int capacity)
            throws IllegalActionException {
        // As of Ptolemy II 2.0.1, we are not using the SDFReceiver.*History*
        // methods, but these are here for future use.
        try {
            _queue.setHistoryCapacity(capacity);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(getContainer(), ex,
                    "Failed to set history capacity to " + capacity);
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

    /** A constant indicating that the capacity of the receiver is
     *  unbounded.
     */
    public static final int INFINITE_CAPACITY =
    ArrayFIFOQueue.INFINITE_CAPACITY;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The queue containing the receiver data.
    private ArrayFIFOQueue _queue;

    // The token array used by this receiver to return
    // data.
    private Token[] _tokenArray;
}
