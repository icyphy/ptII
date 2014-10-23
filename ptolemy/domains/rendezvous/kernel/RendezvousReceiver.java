/* Receiver for rendezvous style communication.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.domains.rendezvous.kernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.BoundaryDetector;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;

///////////////////////////////////////////////////////////////////
//// RendezvousReceiver

/**
 * Receiver for rendezvous style communication. In the rendezvous
 * domain, all communication is via synchronous message passing, so
 * both the the sending and receiving processes need to rendezvous at
 * the receiver. For rendezvous, the receiver is the key
 * synchronization point. It is assumed each receiver has at most one
 * thread trying to send to it and at most one thread trying to
 * receive from it at any one time. The receiver performs the
 * synchronization necessary for simple rendezvous (get() and put()
 * operations). This receiver is based on the CSPReceiver class by
 * John S. Davis II, Thomas Feng, Edward A. Lee, Neil Smyth, and Yang
 * Zhao.
 *
 * @author Thomas Feng
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Green (tfeng)
 * @Pt.AcceptedRating Green (tfeng)
 */
public class RendezvousReceiver extends AbstractReceiver implements
ProcessReceiver {

    /**
     * Construct a RendezvousReceiver with no container.
     */
    public RendezvousReceiver() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
        _thisReceiver[0][0] = this;
    }

    /**
     * Construct a RendezvousReceiver with the specified container.
     *
     * @param container The port containing this receiver.
     * @exception IllegalActionException If this receiver cannot be
     * contained by the proposed container.
     */
    public RendezvousReceiver(IOPort container) throws IllegalActionException {
        super(container);
        _boundaryDetector = new BoundaryDetector(this);
        _thisReceiver[0][0] = this;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Reset local flags.
     */
    @Override
    public void clear() {
        reset();
    }

    /**
     * Get a token from this receiver. This method does not return until the
     * rendezvous has been completed. This method is internally synchronized on
     * the director.
     *
     * @return The token contained by this receiver.
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs has been terminated while still running i.e it
     * was not allowed to run to completion.
     */
    @Override
    public Token get() throws TerminateProcessException {
        return getFromAll(_thisReceiver, _getDirector())[0][0];
    }

    /**
     * Get from all receivers in the specified array. This method does not
     * return until all the gets are complete.
     *
     * @param receivers The receivers, which are assumed to all be
     * instances of RendezvousReceiver.
     * @param director The director, on which this method
     * synchronizes.
     * @return An array of token arrays, where the structure of the
     * array is the same as the structure of the specified array of
     * receivers. Note that if the receivers argument has any null
     * values in the array of arrays, then so will the returned array
     * or arrays.
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs is to be terminated.
     */
    public static Token[][] getFromAll(Receiver[][] receivers,
            RendezvousDirector director) throws TerminateProcessException {
        Map result = null;
        try {
            result = _getOrPutTokens(receivers, null, director, null, null,
                    GET_FROM_ALL);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }

        Token[][] tokens = new Token[receivers.length][];

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                tokens[i] = new Token[receivers[i].length];

                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        tokens[i][j] = (Token) result.get(receivers[i][j]);
                    }
                }
            }
        }

        return tokens;
    }

    /**
     * Get from any receiver in the specified array. This method does
     * not return until one of the gets is complete.
     *
     * @param receivers The receivers, which are assumed to all be
     * instances of RendezvousReceiver.
     * @param director The director, on which this method
     * synchronizes.
     * @return A token from one of the receivers.
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs is to be terminated.
     */
    public static Token getFromAny(Receiver[][] receivers,
            RendezvousDirector director) throws TerminateProcessException {
        Map result = null;
        try {
            result = _getOrPutTokens(receivers, null, director, null, null,
                    GET_FROM_ANY);
        } catch (IllegalActionException ex) {
        }

        for (Receiver[] receiver : receivers) {
            if (receiver != null) {
                for (int j = 0; j < receiver.length; j++) {
                    if (receiver[j] != null) {
                        if (result.containsKey(receiver[j])) {
                            return (Token) result.get(receiver[j]);
                        }
                    }
                }
            }
        }

        throw new InternalErrorException("No token is received.");
    }

    /**
     * Get from any receiver in the getReceivers array, and put the token
     * received to all receivers in putReceivers array. The put and get are to
     * be accomplished simultaneously in a rendezvous. This method does not
     * return until both the get and put are complete.
     *
     * @param getReceivers The receivers, which are assumed to all be
     * instances of RendezvousReceiver, to get tokens from.
     * @param putReceivers The receivers, which are assumed to all be
     * instances of RendezvousReceiver, to put tokens to.
     * @param director The director, on which this method
     * synchronizes.
     * @exception IllegalActionException If the token is not
     * acceptable to one of the ports (e.g., wrong type).
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs is to be terminated.
     */
    public static void getFromAnyPutToAll(Receiver[][] getReceivers,
            Receiver[][] putReceivers, RendezvousDirector director)
                    throws IllegalActionException, TerminateProcessException {
        _getOrPutTokens(getReceivers, putReceivers, director, null, null,
                GET_FROM_ANY_PUT_TO_ALL);
    }

    /**
     * Return true. This method returns true in all cases to indicate
     * that the next call to put() will succeed without throwing a
     * NoRoomException, as indeed it will, even if not right
     * away. Note that if this were to return true only if a
     * rendezvous was pending, then polymorphic actors would busy
     * wait.
     *
     * @return True.
     */
    @Override
    public boolean hasRoom() {
        return true;
    }

    /**
     * Return true. This method returns true in all cases to indicate that any
     * number of calls to put() will succeed without throwing a NoRoomException,
     * as indeed they will, even if not right away. Note that if this were to
     * return true only if a rendezvous was pending, then polymorphic actors
     * would busy wait.
     *
     * @param tokens Ignored by this method.
     * @return True.
     */
    @Override
    public boolean hasRoom(int tokens) {
        return true;
    }

    /**
     * Return true. This method returns true in all cases to indicate
     * that the next call to get() will succeed without throwing a
     * NoTokenException, as indeed it will, even if not right
     * away. Note that if this were to return true only if a
     * rendezvous was pending, then polymorphic actors would busy
     * wait.
     *
     * @return True.
     */
    @Override
    public boolean hasToken() {
        return true;
    }

    /**
     * Return true. This method returns true in all cases to indicate
     * that any number of calls to get() will succeed without throwing
     * a NoTokenException, as indeed they will, even if not right
     * away. Note that if this were to return true only if a
     * rendezvous was pending, then polymorphic actors would busy
     * wait.
     *
     * @param tokens Ignored by this method.
     * @return True.
     */
    @Override
    public boolean hasToken(int tokens) {
        return true;
    }

    /**
     * Return true if this receiver is connected to the inside of a boundary
     * port. A boundary port is an opaque port that is contained by a composite
     * actor. If this receiver is connected to the inside of a boundary port,
     * then return true; otherwise return false.
     *
     * @return True if this receiver is connected to the inside of a
     * boundary port; return false otherwise.
     * @exception IllegalActionException
     */
    @Override
    public boolean isConnectedToBoundary() throws IllegalActionException {
        return _boundaryDetector.isConnectedToBoundary();
    }

    /**
     * Return true if this receiver is connected to the inside of a boundary
     * port. A boundary port is an opaque port that is contained by a composite
     * actor. If this receiver is connected to the inside of a boundary port,
     * then return true; otherwise return false.
     *
     * @return True if this receiver is connected to the inside of a
     * boundary port; return false otherwise.
     * @exception IllegalActionException
     * @exception InvalidStateException
     * @see ptolemy.actor.process.BoundaryDetector
     */
    @Override
    public boolean isConnectedToBoundaryInside() throws InvalidStateException,
    IllegalActionException {
        return _boundaryDetector.isConnectedToBoundaryInside();
    }

    /**
     * Return true if this receiver is connected to the outside of a boundary
     * port. A boundary port is an opaque port that is contained by a composite
     * actor. If this receiver is connected to the outside of a boundary port,
     * then return true; otherwise return false.
     *
     * @return True if this receiver is connected to the outside of a boundary
     * port; return false otherwise.
     * @exception IllegalActionException
     * @see ptolemy.actor.process.BoundaryDetector
     */
    @Override
    public boolean isConnectedToBoundaryOutside() throws IllegalActionException {
        return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /**
     * This class serves as an example of a ConsumerReceiver and hence this
     * method returns true if this port is connected to a boundary.
     * @exception IllegalActionException
     * @see #isConnectedToBoundary
     */
    @Override
    public boolean isConsumerReceiver() throws IllegalActionException {
        if (isConnectedToBoundary()) {
            return true;
        }

        return false;
    }

    /**
     * Return true if this receiver is contained on the inside of a
     * boundary port. A boundary port is an opaque port that is
     * contained by a composite actor. If this receiver is contained
     * on the inside of a boundary port then return true; otherwise
     * return false.
     *
     * @return True if this receiver is contained on the inside of a boundary
     * port; return false otherwise.
     */
    @Override
    public boolean isInsideBoundary() {
        return _boundaryDetector.isInsideBoundary();
    }

    /**
     * Return true if this receiver is contained on the outside of a
     * boundary port. A boundary port is an opaque port that is
     * contained by a composite actor. If this receiver is contained
     * on the outside of a boundary port then return true; otherwise
     * return false.
     *
     * @return True if this receiver is contained on the outside of a
     * boundary port; return false otherwise.
     */
    @Override
    public boolean isOutsideBoundary() {
        return _boundaryDetector.isOutsideBoundary();
    }

    /**
     * Return true if this receiver is on an outside or an inside boundary.
     */
    @Override
    public boolean isProducerReceiver() {
        if (isOutsideBoundary() || isInsideBoundary()) {
            return true;
        }

        return false;
    }

    /**
     * Return true if there is a get or a conditional receive waiting on this
     * receiver.
     *
     * @return True if a read is pending on this receiver.
     */
    @Override
    public boolean isReadBlocked() {
        synchronized (_getDirector()) {
            return _getWaiting != null;
        }
    }

    /**
     * Return true if there is either a put or a conditional send waiting on
     * this receiver.
     *
     * @return A boolean indicating whether a write is pending on this
     * receiver.
     */
    @Override
    public boolean isWriteBlocked() {
        synchronized (_getDirector()) {
            return _putWaiting != null;
        }
    }

    /**
     * Put a token into the mailbox receiver. This method does not
     * return until the rendezvous is complete. This method is
     * internally synchronized on the director.
     *
     * @param token The token, or null to not put any token.
     * @exception IllegalActionException If the token is not
     * acceptable to the port (e.g., wrong type).
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs has been terminated while still running i.e it
     * was not allowed to run to completion.
     */
    @Override
    public void put(Token token) throws IllegalActionException,
    TerminateProcessException {
        if (token == null) {
            return;
        }
        putToAll(new Token[][] { { token } }, _thisReceiver, _getDirector());
    }

    /**
     * Put a sequence of tokens to all receivers in the specified
     * array. This method sequentially calls putToAll() for each token
     * in the tokens array.
     *
     * @param tokens The sequence of token to put.
     * @param numberOfTokens The number of tokens to put (the array might be
     * longer).
     * @param receivers The receivers.
     * @exception NoRoomException If there is no room for the token.
     * @exception IllegalActionException If the token is not
     * acceptable to one of the ports (e.g., wrong type), or if the
     * tokens array does not have at least the specified number of
     * tokens.
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs has been terminated while still running i.e it
     * was not allowed to run to completion.

     */
    @Override
    public void putArrayToAll(Token[] tokens, int numberOfTokens,
            Receiver[] receivers) throws NoRoomException,
            IllegalActionException, TerminateProcessException {
        if (numberOfTokens > tokens.length) {
            IOPort container = getContainer();
            throw new IllegalActionException(container,
                    "Not enough tokens supplied.");
        }

        for (int i = 0; i < numberOfTokens; i++) {
            putToAll(tokens[i], receivers);
        }
    }

    /**
     * Put to all receivers in the specified array. Implementers will assume
     * that all such receivers are of the same class.
     *
     * @param token The token to put, or null to not put any token.
     * @param receivers The receivers.
     * @exception NoRoomException If there is no room for the token.
     * @exception IllegalActionException If the token is not
     * acceptable to one of the ports (e.g., wrong type).
     */
    @Override
    public void putToAll(Token token, Receiver[] receivers)
            throws NoRoomException, IllegalActionException {
        putToAll(token, receivers, _getDirector());
    }

    /**
     * Put to all receivers in the specified array. This method does
     * not return until all the puts are complete. This method differs
     * from its counterpart in the superclass in that it puts the
     * token to all receivers in an atomic step. The method in the
     * superclass puts the token to one receiver in the receiver array
     * at a time.
     *
     * @param token The token to put, or null to not put any token.
     * @param receivers The receivers, which are assumed to all be
     * instances of RendezvousReceiver.
     * @param director The director, on which this method
     * synchronizes.
     * @exception IllegalActionException If the token is not
     * acceptable to one of the ports (e.g., wrong type).
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs is to be terminated.
     */
    public void putToAll(Token token, Receiver[] receivers,
            RendezvousDirector director) throws IllegalActionException,
            TerminateProcessException {
        if (token == null || receivers == null || receivers.length == 0) {
            return;
        }

        putToAll(new Token[][] { { token } }, new Receiver[][] { receivers },
                director);
    }

    /**
     * Put to all receivers in the specified array. This method does
     * not return until all the puts are complete. The tokens argument
     * can have fewer tokens than receivers argument has receivers. If
     * only one token is given (the argument has dimension [1][1]),
     * then that one token is copied to all destination receivers
     * (with possible type changes). If only one token in each
     * dimension is given, then that one token is copied to all
     * destination receivers in the corresponding dimension of the
     * <i>receivers</i> array.
     *
     * @param tokens The tokens to put.
     * @param receivers * The receivers, which are assumed to all be
     * instances of RendezvousReceiver.
     * @param director The director, on which this method
     * synchronizes.
     * @exception IllegalActionException If the token is not
     * acceptable to one of the ports (e.g., wrong type).
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs is to be terminated.
     */
    public static void putToAll(Token[][] tokens, Receiver[][] receivers,
            RendezvousDirector director) throws IllegalActionException,
            TerminateProcessException {
        _getOrPutTokens(null, receivers, director, null, tokens, PUT_TO_ALL);
    }

    /**
     * Put the specified token to any receiver in the specified array. This
     * method does not return until one of the puts is complete.
     *
     * @param token The token to put.
     * @param receivers The receivers, which are assumed to all be
     * instances of RendezvousReceiver.
     * @param director The director, on which this method
     * synchronizes.
     * @exception IllegalActionException If the token is not
     * acceptable to one of the ports (e.g., wrong type).
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs is to be terminated.
     */
    public static void putToAny(Token token, Receiver[][] receivers,
            RendezvousDirector director) throws IllegalActionException,
            TerminateProcessException {
        _getOrPutTokens(null, receivers, director, token, null, PUT_TO_ANY);
    }

    /**
     * The model has finished executing, so set a flag so that the
     * next time an actor tries to get or put it gets a
     * TerminateProcessException which will cause it to finish.
     */
    @Override
    public void requestFinish() {
        Object lock = _getDirector();

        synchronized (lock) {
            reset();
            lock.notifyAll();
        }
    }

    /**
     * Reset local flags.
     */
    @Override
    public void reset() {
        synchronized (_getDirector()) {
            _resetFlags(true, true);
        }
    }

    /**
     * Wait on the specified director. This is not synchronized on the
     * specified director, so the called should be.
     *
     * @param director  The director on which to wait.
     * @exception TerminateProcessException If a finish has been
     * requested of the specified director, or if the calling thread
     * is interrupted while waiting.
     */
    public static void waitForChange(RendezvousDirector director)
            throws TerminateProcessException {
        if (director.isStopRequested() || director._inWrapup) {
            throw new TerminateProcessException("Thread terminated.");
        }

        try {
            director.wait();
        } catch (InterruptedException e) {
            throw new TerminateProcessException("Thread interrupted.");
        }

        if (director.isStopRequested() || director._inWrapup) {
            throw new TerminateProcessException("Thread terminated.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Commit the rendezvous formed by the set of receivers that agree to send
     * and receive at the same time.
     *
     * @param receivers The receivers that participate in the rendezvous.
     * @param director The director.
     * @see #_receiversReadyToCommit(Receiver[][], boolean)
     */
    protected static void _commitRendezvous(Set receivers,
            RendezvousDirector director) {
        // The result table for all the receivers.
        Map result = new HashMap();

        // Backup result tokens for the receivers, and release the threads
        // blocked at those receivers.
        TopologicalSort sort = new TopologicalSort(receivers);
        while (sort.hasNext()) {
            RendezvousReceiver castReceiver = (RendezvousReceiver) sort.next();
            result.put(castReceiver, castReceiver._token);

            if (director._getResultMap(castReceiver._getWaiting) == null) {
                director.threadUnblocked(castReceiver._getWaiting, null);
                director._setResultMap(castReceiver._getWaiting, result);
            }

            if (director._getResultMap(castReceiver._putWaiting) == null) {
                director.threadUnblocked(castReceiver._putWaiting, null);
                director._setResultMap(castReceiver._putWaiting, result);
            }
        }

        // Reset the flags for all the receivers.
        Iterator receiverIterator = receivers.iterator();
        while (receiverIterator.hasNext()) {
            RendezvousReceiver castReceiver = (RendezvousReceiver) receiverIterator
                    .next();

            // If the receiver does conditional get, clear the get request on
            // all the channels.
            if (castReceiver._getReceivers != null
                    && castReceiver._getConditional) {
                _resetReceiversFlags(castReceiver._getReceivers, true, false);
            }

            // If the receiver does conditional put, clear the put request on
            // all the channels.
            if (castReceiver._putReceivers != null
                    && castReceiver._putConditional) {
                _resetReceiversFlags(castReceiver._putReceivers, false, true);
            }

            // Clear the get and put requests on this receiver.
            castReceiver._resetFlags(true, true);
        }
    }

    /**
     * Return the director that is controlling the execution of this
     * model. If this receiver is an inside receiver, then it is the
     * director of the container (actor) of the container
     * (port). Otherwise, it is the executive director of the
     * container (actor) of the container (port).
     *
     * @return The RendezvousDirector controlling this model.
     */
    protected RendezvousDirector _getDirector() {
        try {
            Actor container = (Actor) getContainer().getContainer();

            if (isInsideBoundary()) {
                return (RendezvousDirector) container.getDirector();
            } else {
                return (RendezvousDirector) container.getExecutiveDirector();
            }
        } catch (NullPointerException ex) {
            // If a thread has a reference to a receiver with no director it
            // is an error so terminate the process.
            throw new TerminateProcessException(
                    "RendezvousReceiver: trying to "
                            + "rendezvous with a receiver with no "
                            + "director => terminate.");
        }
    }

    //     /**
    //      * Return whether a getFromAny() is pending on this receiver.
    //      *
    //      * @return True if a getFromAny() is pending on this receiver.
    //      */
    //     protected boolean _isConditionalReceiveWaiting() {
    //         return (_getWaiting != null) && _getConditional;
    //     }

    //     /**
    //      * Return whether a putToAny() is pending on this receiver.
    //      *
    //      * @return True if a putToAny() is pending on this receiver.
    //      */
    //     protected boolean _isConditionalSendWaiting() {
    //         return (_putWaiting != null) && _putConditional;
    //     }

    //     /**
    //      * Return whether a get() is waiting to rendezvous at this receiver.
    //      *
    //      * @return True if a get() is waiting to rendezvous.
    //      */
    //     protected boolean _isGetWaiting() {
    //         return _getWaiting != null;
    //     }

    //     /**
    //      * Flag indicating whether or not a put() is waiting to rendezvous at this
    //      * receiver.
    //      *
    //      * @return True if a put() is waiting to rendezvous.
    //      */
    //     protected boolean _isPutWaiting() {
    //         return _putWaiting != null;
    //     }

    /**
     * Get the receivers that are ready to form a rendezvous according to the
     * rendezvous semantics. If no rendezvous can be formed starting for the
     * given array of receivers, null is returned.
     *
     * @param receivers The array of receivers to be put to or get
     * from.
     * @param isPut If true, the rendezvous is to put tokens to the
     * receivers; if false, the rendezvous is to get tokens from the
     * receivers.
     * @return A set of receivers that participate in the rendezvous
     * if it can be formed, or null if no rendezvous can be formed.
     * @see #_commitRendezvous(Set, RendezvousDirector)
     */
    protected static Set _receiversReadyToCommit(Receiver[][] receivers,
            boolean isPut) {
        Set ready = new HashSet();
        if (_checkRendezvous(receivers, isPut, new HashSet(), ready,
                new HashSet(), new HashSet(), false, false, null)) {
            return ready;
        } else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Check whether a rendezvous can be formed starting from the given
     * two-dimensional array of receivers. If a rendezvous can be formed, the
     * ready set contains all the receivers participating in the rendezvous
     * after the call, and true is returned; Otherwise, the ready set is not
     * meaningful.
     *
     * @param receivers The initial two-dimensional array of receivers
     * on an actor.
     * @param isPut Whether the request is put.
     * @param beingChecked The set of receivers that are being checked
     * by previous recursive calls.
     * @param ready The set of receivers that are ready for a
     * rendezvous.
     * @param notReady The set of receivers that are not ready for a
     * rendezvous.
     * @param symmetricReceivers The set of symmetric receivers that
     * have been visited during the traversal.
     * @param isSymmetricGet Whether the previous recursive call is
     * from the other side (the get side) of a Merge or Barrier.
     * @param isSymmetricPut Whether the previous recursive call is
     * from the other side (the put side) of a Merge or Barrier.
     * @param farSideReceiver The receiver that is being checked on
     * the far side, or null if this method is not called from the far
     * side.
     * @return Whether a rendezvous can be formed.
     */
    private static boolean _checkRendezvous(Receiver[][] receivers,
            boolean isPut, Set beingChecked, Set ready, Set notReady,
            Set symmetricReceivers, boolean isSymmetricGet,
            boolean isSymmetricPut, Receiver farSideReceiver) {

        // Trivially return true or false if there is no receiver is to be put
        // tokens to or get tokens from, respectively.
        if (receivers.length == 0) {
            return isPut;
        }

        // Whether the receivers are conditional. To be initialized in the
        // loops.
        boolean isConditional = _isConditional(receivers, isPut);

        // Whether the last conditional branch is ready.
        boolean branchReady = false;

        // Test which branch of the given receivers has been chosen previously.
        int selectedBranch = _getSelectedBranch(receivers, beingChecked, ready);

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] == null) {
                continue;
            }

            // If the call comes from a far side receiver, test whether it is in
            // this channel.
            if (isConditional && farSideReceiver != null) {
                boolean found = false;
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] == farSideReceiver) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
            }

            // If the put/get is conditional and another branch was
            // chosen previously, cancel the current branch.
            if (isConditional && selectedBranch >= 0 && selectedBranch != i) {
                continue;
            }

            // Assume the branch is ready, to be disproved later.
            branchReady = true;

            for (int j = 0; j < receivers[i].length; j++) {
                RendezvousReceiver receiver = (RendezvousReceiver) receivers[i][j];
                if (receiver == null) {
                    continue;
                }

                // Loop detection.
                if (symmetricReceivers.contains(receiver)) {
                    // Zero-delay loop detected.
                    return false;
                }

                if (beingChecked.contains(receiver) || ready.contains(receiver)) {
                    // If the receiver has been visited or is ready, do
                    // nothing.
                } else if (notReady.contains(receiver)) {
                    // If the receiver is not ready, cancel the current
                    // branch.
                    branchReady = false;
                    break;
                } else if (receiver._putWaiting == null
                        || receiver._getWaiting == null) {
                    // If not putWaiting and getWaiting at the same
                    // time, the receiver is not ready, so record this
                    // and cancel the current branch.
                    branchReady = false;
                    notReady.add(receiver);
                    break;
                } else {
                    // Otherwise, assume the receiver is ready, and
                    // continue to check its dependencies.

                    Receiver[][] farSideReceivers = isPut ? receiver._getReceivers
                            : receiver._putReceivers;

                    beingChecked.add(receiver);
                    symmetricReceivers.add(receiver);

                    // Test the symmetric get side receivers, if this
                    // call is not from a symmetric put call. If this
                    // call is from a symmetric put call, there is no
                    // need to go backwards.
                    Receiver[][] symmetric = receiver._symmetricGetReceivers;
                    if (branchReady && !isSymmetricPut && symmetric != null) {
                        if (!_checkRendezvous(symmetric, false, beingChecked,
                                ready, notReady, symmetricReceivers, true,
                                false, null)) {
                            branchReady = false;
                        }
                    }

                    // Test the symmetric put side receivers, if this
                    // call is not from a symmetric get call. If this
                    // call is from a symmetric get call, there is no
                    // need to go backwards.
                    symmetric = receiver._symmetricPutReceivers;
                    if (branchReady && !isSymmetricGet && symmetric != null) {
                        if (!_checkRendezvous(symmetric, true, beingChecked,
                                ready, notReady, symmetricReceivers, false,
                                true, null)) {
                            branchReady = false;
                        }
                    }

                    // Test the far side receivers.
                    if (branchReady
                            && !_checkRendezvous(farSideReceivers, !isPut,
                                    beingChecked, ready, notReady,
                                    new HashSet(), false, false, receiver)) {
                        branchReady = false;
                    }

                    beingChecked.remove(receiver);
                    symmetricReceivers.remove(receiver);

                    if (branchReady) {
                        ready.add(receiver);
                    } else {
                        notReady.add(receiver);
                        break;
                    }
                }
            }

            if (isConditional && branchReady || !isConditional && !branchReady) {
                // If either is true, no further test is needed.
                break;
            }
        }

        // Return whether the last branch checked is ready.
        return branchReady;
    }

    /**
     * Get or put token(s) to the array of receivers, or both put and
     * get at the same time. The operation that it performs depends on
     * the flag parameter. If a get is requested in the flag,
     * getReceivers should contain the receivers to receive tokens;
     * otherwise, getReceivers is ignored. If a put is requested in
     * the flag, putReceivers should contain the receivers to put
     * tokens to.  The tokens are stored either in the token parameter
     * or the tokenArray parameter, depending on the operation. If
     * the put is to any of the receivers, the token parameter is the
     * single token to put, and the tokenArray parameter is ignored; if
     * the put is to all of the receivers, the tokenArray parameter is
     * the two-dimensional array of tokens, one corresponding
     * to a receiver in the two-dimensional array putReceivers, and the
     * token parameter is ignored. This method does not return until the
     * requested operation is finished.
     *
     * @param getReceivers The receivers from with tokens are
     * received.
     * @param putReceivers The receivers to which tokens are put.
     * @param director The director.
     * @param token The token of the put to any operation, or null.
     * @param tokenArray The token array of the put to all operation, or
     * null.
     * @param flag The flag representing the operation to be
     * performed.
     * @return The map of results on the receivers that participate in
     * the rendezvous. Keys of the map are receivers; values of the
     * map are the tokens on those receivers.
     * @exception IllegalActionException If the token is not
     * acceptable to one of the ports (e.g., wrong type). This can
     * happen only if the operation is put to all or put to any.
     * @exception TerminateProcessException If the actor to which this
     * receiver belongs is to be terminated.
     */
    private static Map _getOrPutTokens(Receiver[][] getReceivers,
            Receiver[][] putReceivers, RendezvousDirector director,
            Token token, Token[][] tokenArray, int flag)
                    throws IllegalActionException, TerminateProcessException {

        // Extract information from the flag.
        boolean isGet = (flag & GET) == GET;
        boolean isPut = (flag & PUT) == PUT;
        boolean isGetConditional = (flag & GET_CONDITIONAL) == GET_CONDITIONAL;
        boolean isPutConditional = (flag & PUT_CONDITIONAL) == PUT_CONDITIONAL;
        boolean isSymmetric = isPut && isGet;

        Map result = null;
        synchronized (director) {
            Thread theThread = Thread.currentThread();

            // Test whether the cardinality of a "put to all and get from all"
            // operation is correct. If there are more channels to put to than
            // the channels to get from, then this function does nothing but
            // locks the current thread.
            boolean cardinalityTest = true;
            if (!isGetConditional && !isPutConditional && getReceivers != null
                    && putReceivers != null
                    && getReceivers.length < putReceivers.length) {
                cardinalityTest = false;
            }

            if (cardinalityTest && isGet) {
                for (int i = 0; i < getReceivers.length; i++) {
                    if (getReceivers[i] != null) {
                        for (int j = 0; j < getReceivers[i].length; j++) {
                            RendezvousReceiver receiver = (RendezvousReceiver) getReceivers[i][j];

                            if (receiver != null) {
                                receiver._getWaiting = theThread;
                                receiver._getReceivers = getReceivers;
                                receiver._getConditional = isGetConditional;

                                if (isSymmetric) {
                                    receiver._channelIndex = i;
                                    receiver._symmetricPutReceivers = putReceivers;
                                }
                            }
                        }
                    }
                }
            }

            if (cardinalityTest && isPut) {
                for (int i = 0; i < putReceivers.length; i++) {
                    if (putReceivers[i] != null) {
                        for (int j = 0; j < putReceivers[i].length; j++) {
                            RendezvousReceiver receiver = (RendezvousReceiver) putReceivers[i][j];

                            if (receiver != null) {
                                receiver._putWaiting = theThread;
                                receiver._putReceivers = putReceivers;

                                IOPort port = receiver.getContainer();
                                if (isPutConditional) {
                                    receiver._putConditional = true;
                                    receiver._token = token == null ? null
                                            : port.convert(token);
                                } else {
                                    receiver._putConditional = false;
                                    try {
                                        token = tokenArray[i][j];
                                    } catch (Throwable e) {
                                    }
                                    receiver._token = token == null ? null
                                            : port.convert(token);
                                }

                                if (isSymmetric) {
                                    receiver._symmetricGetReceivers = getReceivers;
                                }
                            }
                        }
                    }
                }
            }

            Set rendezvousReceivers = null;
            // Test the rendezvous. If both put and get are to be done at the
            // same time, it does not matter whether the test starts from the
            // get receivers or the put receivers.
            if (cardinalityTest && getReceivers != null) {
                rendezvousReceivers = _receiversReadyToCommit(getReceivers,
                        false);
            } else if (cardinalityTest) {
                rendezvousReceivers = _receiversReadyToCommit(putReceivers,
                        true);
            }

            if (rendezvousReceivers == null) {
                // A rendezvous cannot be formed at this time.
                // This thread just waits until another thread commits a
                // rendezvous with the given receivers.
                director.threadBlocked(theThread, null);
                while (result == null) {
                    waitForChange(director);
                    result = director._getResultMap(theThread);
                }
                director._setResultMap(theThread, null);
            } else {
                // A rendezvous is formed, so commit the rendezvous, and wake
                // up the other threads in this rendezvous.
                _commitRendezvous(rendezvousReceivers, director);
                result = director._setResultMap(theThread, null);
            }
        }
        return result;
    }

    /**
     * Get the branch of the two-dimensional array of receivers that
     * has been selected by previous recursive calls of {@link
     * #_checkRendezvous(Receiver[][], boolean, Set, Set, Set,
     * Set, boolean, boolean, Receiver)}.
     *
     * @param receivers The two-dimensional array of receivers.
     * @param beingChecked The set of receivers that are being checked
     * by previous recursive calls.
     * @param ready The set of receivers that are ready for a
     * rendezvous.
     * @return The index of the selected branch, or -1 if no branch
     * has been selected yet.
     */
    private static int _getSelectedBranch(Receiver[][] receivers,
            Set beingChecked, Set ready) {
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] == null) {
                continue;
            }
            for (int j = 0; j < receivers[i].length; j++) {
                Receiver receiver = receivers[i][j];
                if (receiver == null) {
                    continue;
                }
                if (beingChecked.contains(receiver) || ready.contains(receiver)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /** Test whether a two-dimensional array of receivers are conditional.
     *
     *  @param receivers The two-dimensional array of receivers.
     *  @param isPut Whether to test put conditional (true) or to test
     *  get conditional (false).
     *  @return Whether the receivers are conditional.
     */
    private static boolean _isConditional(Receiver[][] receivers, boolean isPut) {
        for (Receiver[] receiver2 : receivers) {
            if (receiver2 != null) {
                for (int j = 0; j < receiver2.length; j++) {
                    RendezvousReceiver receiver = (RendezvousReceiver) receiver2[j];
                    if (receiver != null) {
                        if (isPut) {
                            return receiver._putConditional;
                        } else {
                            return receiver._getConditional;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Reset the flags of this receiver.
     *
     * @param clearGet Whether to reset the flags related to the get methods.
     * @param clearPut Whether to reset the flags related to the put methods.
     */
    private void _resetFlags(boolean clearGet, boolean clearPut) {
        if (clearGet) {
            _channelIndex = -1;
            _getReceivers = null;
            _getWaiting = null;
            _symmetricPutReceivers = null;
        }

        if (clearPut) {
            _putReceivers = null;
            _putWaiting = null;
            _symmetricGetReceivers = null;
        }
    }

    /**
     * Reset the flags of the receivers in the given array.
     *
     * @param receivers An array of receivers to be reset.
     * @param clearGet Whether to reset the flags related to the get methods.
     * @param clearPut Whether to reset the flags related to the put methods.
     */
    private static void _resetReceiversFlags(Receiver[][] receivers,
            boolean clearGet, boolean clearPut) {
        for (Receiver[] receiver : receivers) {
            if (receiver != null) {
                for (int j = 0; j < receiver.length; j++) {
                    if (receiver[j] != null) {
                        ((RendezvousReceiver) receiver[j])._resetFlags(
                                clearGet, clearPut);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Flag to test whether an operation does a get. */
    private static final int GET = 4; // 0100

    /** Flag to test whether an operation does a put. */
    private static final int PUT = 8; // 1000

    /** Flag to test whether an operation does a conditional get. */
    private static final int GET_CONDITIONAL = 1; // 0001

    /** Flag to test whether an operation does a conditional put. */
    private static final int PUT_CONDITIONAL = 2; // 0010

    /** Flag for the "get from all" operation. */
    private static final int GET_FROM_ALL = 4; // 0100

    /** Flag for the "get from any" operation. */
    private static final int GET_FROM_ANY = 5; // 0101

    /** Flag for the "put to all" operation. */
    private static final int PUT_TO_ALL = 8; // 1000

    /** Flag for the "put to any" operation. */
    private static final int PUT_TO_ANY = 10; // 1010

    /** Flag for the "get from all and put to all" operation. */
    //private static final int GET_FROM_ALL_PUT_TO_ALL = 12; // 1100
    /** Flag for the "get from any and put to all" operation. */
    private static final int GET_FROM_ANY_PUT_TO_ALL = 13; // 1101

    /** The boundary detector. */
    private BoundaryDetector _boundaryDetector;

    /** The index of the channel that this receiver is in. */
    private int _channelIndex = -1;

    /**
     * Flag indicating that the _getWaiting thread is a conditional rendezvous.
     */
    private boolean _getConditional = false;

    /** The receivers currently being gotten data from. */
    private Receiver[][] _getReceivers = null;

    /** Indicator that a get() is waiting on this receiver. */
    private Thread _getWaiting = null;

    /**
     * Flag indicating that the _putWaiting thread is a conditional rendezvous.
     */
    private boolean _putConditional = false;

    /** The receivers currently being put data to. */
    private Receiver[][] _putReceivers = null;

    /** Indicator that a put() is waiting on this receiver. */
    private Thread _putWaiting = null;

    /** The receivers for a get operation on the other side, or null. */
    private Receiver[][] _symmetricGetReceivers;

    /** The receivers for a put operation on the other side, or null. */
    private Receiver[][] _symmetricPutReceivers;

    /** Array with just one receiver, this one, for convenience. */
    private Receiver[][] _thisReceiver = new Receiver[1][1];

    /** The token being transferred during the rendezvous. */
    private Token _token;

    ///////////////////////////////////////////////////////////////////
    ////                           private classes                 ////
    /**
     * Topological sort for the set of receivers to be committed. The
     * set of receivers may have dependencies among them, because
     * receivers in the up-stream of a Merge or Barrier must be
     * committed before those in the down-stream. This sort takes a
     * set of receivers, and returns one of them at a time in the
     * topological order. Cycles must not exist in the set of
     * receivers; Otherwise, there will be {@link
     * InternalErrorException} or infinite loop.
     *
     * @author Thomas Feng
     * @version $Id: RendezvousReceiver.java,v 1.18 2005/11/03 21:27:29 tfeng
     *          Exp $
     * @since Ptolemy II 5.1
     */
    private static class TopologicalSort {

        /**
         * Construct a topological sort object with a set of receivers ready to
         * commit.
         *
         * @param receivers
         *            The set of receivers.
         */
        TopologicalSort(Set receivers) {
            _receivers = receivers;
            _initialize();
        }

        /**
         * Test whether there are more receiver to be returned.
         *
         * @return true if there are more receiver; false otherwise.
         */
        public boolean hasNext() {
            return !_zeroInDegree.isEmpty();
        }

        /**
         * Return the next receiver. There must be some receiver left.
         *
         * @return The next receiver.
         */
        public Receiver next() {
            Iterator zeroInDegreeIterator = _zeroInDegree.iterator();
            if (!zeroInDegreeIterator.hasNext()) {
                return null;
            }

            RendezvousReceiver next = (RendezvousReceiver) zeroInDegreeIterator
                    .next();
            zeroInDegreeIterator.remove();

            if (next._symmetricPutReceivers != null) {
                Token token = next._token;
                Receiver[][] putReceivers = next._symmetricPutReceivers;

                // Delete the out-edges.
                if (next._getConditional) {
                    for (Receiver[] putReceiver : putReceivers) {
                        if (putReceiver != null) {
                            for (int j = 0; j < putReceiver.length; j++) {
                                RendezvousReceiver receiver = (RendezvousReceiver) putReceiver[j];

                                if (receiver != null
                                        && _receivers.contains(receiver)) {
                                    receiver._token = token;
                                    _zeroInDegree.add(receiver);
                                }
                            }
                        }
                    }
                } else {
                    // Kept for the old implementation of Barrier, which sends
                    // outputs. Should not be reached in the current
                    // implementation.
                    int i = next._channelIndex;
                    if (i < putReceivers.length && putReceivers[i] != null) {
                        for (int j = 0; j < putReceivers[i].length; j++) {
                            RendezvousReceiver receiver = (RendezvousReceiver) putReceivers[i][j];

                            if (receiver != null
                                    && _receivers.contains(receiver)) {
                                receiver._token = token;
                                _zeroInDegree.add(receiver);
                            }
                        }
                    }
                }
            }
            return next;
        }

        /**
         * Initialize the set of zero in-degree receivers.
         */
        private void _initialize() {
            _zeroInDegree = new HashSet();
            Iterator iterator = _receivers.iterator();
            while (iterator.hasNext()) {
                RendezvousReceiver receiver = (RendezvousReceiver) iterator
                        .next();
                if (receiver._symmetricGetReceivers == null) {
                    _zeroInDegree.add(receiver);
                }
            }
            if (_zeroInDegree.isEmpty()) {
                throw new InternalErrorException("No entry point.");
            }
        }

        /** The set of receivers given to the constructor. */
        private Set _receivers;

        /**
         * The set of receivers with zero in-degree, with can be immediately
         * returned by {@link #next()}.
         */
        private Set _zeroInDegree;
    }
}
