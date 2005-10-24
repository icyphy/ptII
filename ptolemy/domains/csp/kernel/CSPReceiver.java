/* Receiver for CSP style communication.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.csp.kernel;

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

//////////////////////////////////////////////////////////////////////////
//// CSPReceiver

/**
 Receiver for CSP style communication. In CSP all communication is via
 synchronous message passing, so both the the sending and receiving
 process need to rendezvous at the receiver. For rendezvous, the
 receiver is the key synchronization point. It is assumed each receiver
 has at most one thread trying to send to it and at most one thread
 trying to receive from it at any one time. The receiver performs the
 synchronization necessary for simple rendezvous (get() and put()
 operations). It also stores the flags that allow the ConditionalSend
 and ConditionalReceive branches to know when they can proceed.
 <p>
 @author John S. Davis II, Thomas Feng, Edward A. Lee, Neil Smyth, Yang Zhao
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (nsmyth)
 @Pt.AcceptedRating Red (cxh)
 @deprecated Use RendezvousReceiver instead.
 */
public class CSPReceiver extends AbstractReceiver implements ProcessReceiver {
    // FIXME: Downgraded to Red when changing deadlock detection mechanism.
    // EAL 8/05

    /** Construct a CSPReceiver with no container.
     */
    public CSPReceiver() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
        _thisReceiver[0][0] = this;
    }

    /** Construct a CSPReceiver with the specified container.
     *  @param container The port containing this receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public CSPReceiver(IOPort container) throws IllegalActionException {
        super(container);
        _boundaryDetector = new BoundaryDetector(this);
        _thisReceiver[0][0] = this;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset local flags.
     */
    public void clear() {
        reset();
    }

    /** Get a token from this receiver. This method
     *  does not return until the rendezvous has been completed.
     *  This method is internally synchronized on the director.
     *  @return The token contained by this receiver.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e it was not allowed to run to completion.
     */
    public Token get() throws TerminateProcessException {
        return getFromAll(_thisReceiver, _getDirector())[0][0];
    }

    /** Get from all receivers in the specified array.
     *  This method does not return until all the gets are complete.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of CSPReceiver.
     *  @param director The director, on which this method synchronizes.
     *  @return An array of arrays tokens, where the structure of the array is
     *   the same as the structure of the specified array of receivers. Note that
     *   if the argument has any null values in the array of arrays, then so will
     *   the returned array or arrays.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs is to be terminated.
     */
    public static Token[][] getFromAll(Receiver[][] receivers,
            CSPDirector director) throws TerminateProcessException {
        if ((receivers == null) || (receivers.length == 0)) {
            throw new InternalErrorException("No receivers!");
        }

        synchronized (director) {
            Thread thisThread = Thread.currentThread();

            // Prior to returning, a previous invocation of this method will
            // have set _putWaiting to null, but the put thread may not have
            // been given a chance to react to this by returning. Thus, we
            // have to wait here until all put threads have had a chance to
            // react and return.
            while (_isPutInProgressOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            }

            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];
                            castReceiver._getWaiting = thisThread;
                            castReceiver._getReceivers = receivers;
                            castReceiver._getConditional = false;

                            // If there is a put waiting, mark its thread unblocked.
                            // Note that if the put side is conditional,
                            // then this get may not actually unblock it.
                            // But this is harmless since that thread will be
                            // notified in any case, and it will mark itself
                            // blocked if it is still blocked.
                            if (castReceiver._putWaiting != null) {
                                director.threadUnblocked(
                                        castReceiver._putWaiting, null);
                            }
                        }
                    }
                }
            }

            while (!_areReadyToPut(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            }

            // We are now committed to this get.
            // At this point, _putWaiting is non-null on all receivers.
            // This should mean that _putReceivers is also non-null on all receivers.
            // Perform the transfers and set these to null on each recevier.
            // Setting _putWaiting to null will allow the put thread to return
            // from its corresponding put method.
            Token[][] result = new Token[receivers.length][];

            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    result[i] = new Token[receivers[i].length];

                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            // Perform the transfer.
                            CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];
                            result[i][j] = castReceiver._token;

                            // Indicate to the corresponding put() thread that the put completed.
                            Thread putThread = castReceiver._putWaiting;
                            Receiver[][] putReceivers = castReceiver._putReceivers;
                            castReceiver._putWaiting = null;
                            castReceiver._putReceivers = null;

                            // Set a flag indicating that the put thread can now
                            // return but has not yet returned.
                            castReceiver._putInProgress = thisThread;

                            // The following does a notify on the director.
                            director.threadUnblocked(putThread, null);
                        }
                    }
                }
            }

            // Wait for the put to complete on all my receivers.
            // The put thread will set _getWaiting to null on each receiver as it completes.
            while (_isGetWaitingOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            }

            // Finally, reset the _getInProgress flag.
            // This indicates that this thread has had a chance to react to the
            // null value of _getWaiting that was set by the put thread.
            // This will allow the next invocation of a put method to continue.
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];

                            // This could unblock the put thread.
                            director.threadUnblocked(
                                    castReceiver._getInProgress, castReceiver);
                            castReceiver._getInProgress = null;
                        }
                    }
                }
            }

            return result;
        } // synchronized(director)
    }

    /** Get from any receiver in the specified array.
     *  This method does not return until one of the gets is complete.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of CSPReceiver.
     *  @param director The director, on which this method synchronizes.
     *  @return A token from one of the receivers.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs is to be terminated.
     */
    public static Token getFromAny(Receiver[][] receivers, CSPDirector director)
            throws TerminateProcessException {
        // FIXME: As with putToAny(), this should nondeterministically select
        // a channel, but then perform a multiway rendezvous with each receiver
        // on the channel.  In Ptolemy II, this situation never currently occurs,
        // and there will only be one receiver per channel. The only way to get
        // multiple receivers per channel is if the input is in a composite
        // actor and the receivers linked on the inside (obtained through
        // deepGetReceivers()) are used.
        if ((receivers == null) || (receivers.length == 0)) {
            throw new InternalErrorException("No receivers!");
        }

        synchronized (director) {
            Thread thisThread = Thread.currentThread();

            // Prior to returning, a previous invocation of this method will
            // have set _putWaiting to null, but the put thread may not have
            // been given a chance to react to this by returning. Thus, we
            // have to wait here until all put threads have had a chance to
            // react and return.
            while (_isPutInProgressOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            }

            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];
                            castReceiver._getWaiting = thisThread;
                            castReceiver._getReceivers = receivers;
                            castReceiver._getConditional = true;

                            // Any receiver that is blocked could get unblocked by this.
                            // NOTE: If the put side is multiway, we could be more
                            // specific here, but there is no harm in marking unblocked.
                            // It will be notified in any case, and will mark itself
                            // blocked again if it is still blocked.
                            if (castReceiver._putWaiting != null) {
                                director.threadUnblocked(
                                        castReceiver._putWaiting, null);
                            }
                        }
                    }
                }
            }

            // Iterate until a receiver is found whose put side is
            // ready to commit.
            while (true) {
                // If there are no puts pending on any of these receivers, then wait
                // until there is at least one.
                while (!_isPutWaitingOnAny(receivers)) {
                    try {
                        // The following does a notifyAll() on the director.
                        director.threadBlocked(thisThread, null);
                        waitForChange(director);
                    } finally {
                        director.threadUnblocked(thisThread, null);
                    }
                }

                // At this point, we have a put waiting on at least one
                // sender.  If this method was called before any put()
                // was ready, then by the time we get here, some other thread
                // will have picked the winning receiver by setting
                // _wins. Iterate through the receivers
                // until we find one that won the put.
                CSPReceiver winner = null;

                for (int i = 0; i < receivers.length; i++) {
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            if (receivers[i][j] != null) {
                                if (((CSPReceiver) receivers[i][j])._wins) {
                                    winner = (CSPReceiver) receivers[i][j];
                                    break;
                                }
                            }
                        }
                    }

                    if (winner != null) {
                        break;
                    }
                }

                // If no previously designated winner has been
                // found, then in a second pass, find
                // the first one that is ready to complete the rendezvous.
                // FIXME: Here, we could implement some fairness
                // scheme rather than always selecting the first receiver
                // that is ready to commit.
                if (winner == null) {
                    for (int i = 0; i < receivers.length; i++) {
                        if (receivers[i] != null) {
                            for (int j = 0; j < receivers[i].length; j++) {
                                if (receivers[i][j] != null) {
                                    if (_isReadyToPut((CSPReceiver) receivers[i][j])) {
                                        winner = (CSPReceiver) receivers[i][j];
                                        break;
                                    }
                                }
                            }
                        }

                        if (winner != null) {
                            break;
                        }
                    }
                }

                // It is possible that we have still not found a
                // winning put, in which case, we just wait.
                // But if we have found a winning put, then we are
                // ready to complete the rendezvous and return.
                if (winner != null) {
                    // The put is ready to complete.
                    // We are now committed.
                    // Complete the rendezvous.
                    // First, retract the _getWaiting on all receivers
                    // except the one to which we are committing.
                    for (int k = 0; k < receivers.length; k++) {
                        if (receivers[k] != null) {
                            for (int m = 0; m < receivers[k].length; m++) {
                                if (receivers[k][m] != winner) {
                                    CSPReceiver otherCastReceiver = (CSPReceiver) receivers[k][m];
                                    otherCastReceiver._getWaiting = null;
                                    otherCastReceiver._getReceivers = null;
                                }
                            }
                        }
                    }

                    // Have to read the token before waiting for the put
                    // to complete, or we end up with a race condition
                    // where the token may be overwritten before we get to it.
                    Token result = winner._token;

                    // Indicate to the corresponding put() thread that the put completed.
                    Thread putThread = winner._putWaiting;
                    Receiver[][] putReceivers = winner._putReceivers;
                    winner._putWaiting = null;
                    winner._putReceivers = null;

                    // Indicate that a put is in progress.
                    winner._putInProgress = thisThread;

                    // The following does a notify on the director.
                    director.threadUnblocked(putThread, null);

                    // Wait for the put to complete on the selected receiver.
                    while (winner._getWaiting != null) {
                        try {
                            // The following does a notifyAll() on the director.
                            director.threadBlocked(thisThread, null);
                            waitForChange(director);
                        } finally {
                            director.threadUnblocked(thisThread, null);
                        }
                    }

                    // Finally, reset the _getInProgress flag.
                    director.threadUnblocked(winner._getInProgress, winner);
                    winner._getInProgress = null;
                    winner._wins = false;

                    return result;
                }

                // Wait for a change.
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            } // while(true)
        } // synchronized(director)
    }

    /** Return true. This method returns true in all cases
     *  to indicate that the next call to put() will succeed
     *  without throwing a NoRoomException, as indeed it will,
     *  even if not right away.  Note that if this were to return
     *  true only if a rendezvous was pending, then polymorphic actors
     *  would busy wait.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true. This method returns true in all cases
     *  to indicate that any number of calls to put() will succeed
     *  without throwing a NoRoomException, as indeed they will,
     *  even if not right away.  Note that if this were to return
     *  true only if a rendezvous was pending, then polymorphic actors
     *  would busy wait.
     *  @param tokens Ignored by this method.
     *  @return True.
     */
    public boolean hasRoom(int tokens) {
        return true;
    }

    /** Return true. This method returns true in all cases
     *  to indicate that the next call to get() will succeed
     *  without throwing a NoTokenException, as indeed it will,
     *  even if not right away.  Note that if this were to return
     *  true only if a rendezvous was pending, then polymorphic actors
     *  would busy wait.
     *  @return True.
     */
    public boolean hasToken() {
        return true;
    }

    /** Return true. This method returns true in all cases
     *  to indicate that any number of calls to get() will succeed
     *  without throwing a NoTokenException, as indeed they will,
     *  even if not right away.  Note that if this were to return
     *  true only if a rendezvous was pending, then polymorphic actors
     *  would busy wait.
     *  @param tokens Ignored by this method.
     *  @return True.
     */
    public boolean hasToken(int tokens) {
        return true;
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is contained
     *  by a composite actor. If this receiver is connected to the inside
     *  of a boundary port, then return true; otherwise return false.
     *  @return True if this receiver is connected to the inside of a
     *   boundary port; return false otherwise.
     */
    public boolean isConnectedToBoundary() {
        return _boundaryDetector.isConnectedToBoundary();
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
     *  @return True if this receiver is connected to the inside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isConnectedToBoundaryInside() {
        return _boundaryDetector.isConnectedToBoundaryInside();
    }

    /** Return true if this receiver is connected to the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the outside of a boundary port, then return true; otherwise
     *  return false.
     *  @return True if this receiver is connected to the outside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isConnectedToBoundaryOutside() {
        return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /** This class serves as an example of a ConsumerReceiver and
     *  hence this method returns true.
     */
    public boolean isConsumerReceiver() {
        if (isConnectedToBoundary()) {
            return true;
        }

        return false;
    }

    /** Return true if this receiver is contained on the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the inside of a boundary port then return true; otherwise
     *  return false.
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     */
    public boolean isInsideBoundary() {
        return _boundaryDetector.isInsideBoundary();
    }

    /** Return true if this receiver is contained on the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the outside of a boundary port then return true; otherwise
     *  return false.
     *  @return True if this receiver is contained on the outside of
     *   a boundary port; return false otherwise.
     */
    public boolean isOutsideBoundary() {
        return _boundaryDetector.isOutsideBoundary();
    }

    /** Return true if this receiver is on an outside or
     *  an inside boundary.
     */
    public boolean isProducerReceiver() {
        if (isOutsideBoundary() || isInsideBoundary()) {
            return true;
        }

        return false;
    }

    /** Return true if there is a get or a conditional receive
     *  waiting on this receiver.
     *  @return True if a read is pending on this receiver.
     */
    public boolean isReadBlocked() {
        synchronized (_getDirector()) {
            return (_getWaiting != null)
                    || (_conditionalReceiveWaiting != null);
        }
    }

    /** Return true if there is either a put or a conditional send
     *  waiting on this receiver.
     *  @return A boolean indicating whether a write is pending on this
     *   receiver.
     */
    public boolean isWriteBlocked() {
        synchronized (_getDirector()) {
            return (_putWaiting != null) || (_conditionalSendWaiting != null);
        }
    }

    /** Put a token into the mailbox receiver. This method does
     *  not return until the rendezvous is complete.
     *  This method is internally synchronized on the director.
     *  @param token The token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to the port (e.g., wrong type).
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e it was not allowed to run to completion.
     */
    public void put(Token token) throws IllegalActionException,
            TerminateProcessException {
        Token[][] tokens = new Token[1][1];
        tokens[0][0] = token;
        putToAll(tokens, _thisReceiver, _getDirector());
    }

    /** Put a sequence of tokens to all receivers in the specified array.
     *  This method sequentially calls putToAll() for each token in the
     *  tokens array.
     *  @param tokens The sequence of token to put.
     *  @param numberOfTokens The number of tokens to put (the array might
     *   be longer).
     *  @param receivers The receivers.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type), or if the tokens array
     *   does not have at least the specified number of tokens.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e it was not allowed to run to completion.
     */
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

    /** Put to all receivers in the specified array.
     *  This method does not return until all the puts are complete.
     *  @param token The token to put.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of CSPReceiver.
     *  @param director The director, on which this method synchronizes.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs is to be terminated.
     */
    public void putToAll(Token token, Receiver[] receivers, CSPDirector director)
            throws IllegalActionException, TerminateProcessException {
        if ((receivers == null) || (receivers.length == 0)) {
            return;
        }

        Receiver[][] argument = new Receiver[1][];
        argument[0] = receivers;

        Token[][] tokens = new Token[1][1];
        tokens[0][0] = token;
        putToAll(tokens, argument, director);
    }

    /** Put to all receivers in the specified array.
     *  This method does not return until all the puts are complete.
     *  The tokens argument can have fewer tokens than receivers argument
     *  has receivers. If only one token is given (the argument has
     *  dimension [1][1]), then that one token is copied to all
     *  destination receivers (with possible type changes).
     *  If only one token in each dimension is given, then that
     *  one token is copied to all destination receivers in the
     *  corresponding dimension of the <i>receivers</i> array.
     *  @param tokens The tokens to put.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of CSPReceiver.
     *  @param director The director, on which this method synchronizes.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs is to be terminated.
     */
    public static void putToAll(Token[][] tokens, Receiver[][] receivers,
            CSPDirector director) throws IllegalActionException,
            TerminateProcessException {
        if ((receivers == null) || (receivers.length == 0)) {
            return;
        }

        synchronized (director) {
            Thread thisThread = Thread.currentThread();

            // Prior to returning, a previous invocation of this method will
            // have set _getWaiting to null, but the get thread may not have
            // been given a chance to react to this by returning. Thus, we
            // have to wait here until all get threads have had a chance to
            // react and return.
            while (_isGetInProgressOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            }

            Token token = null;

            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            if ((tokens.length > i) && (tokens[i] != null)
                                    && (tokens[i].length > j)) {
                                token = tokens[i][j];
                            }

                            CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];
                            castReceiver._putWaiting = thisThread;
                            castReceiver._putReceivers = receivers;

                            // Perform the transfer.
                            IOPort port = castReceiver.getContainer();
                            castReceiver._token = port.convert(token);

                            // If there is a get waiting, mark its thread unblocked.
                            // Note that if the get side is conditional,
                            // then this put may not actually unblock it.
                            // But this is harmless since that thread will be
                            // notified in any case, and it will mark itself
                            // blocked if it is still blocked.
                            if (castReceiver._getWaiting != null) {
                                director.threadUnblocked(
                                        castReceiver._getWaiting, null);
                            }
                        }
                    }
                }
            }

            while (!_areReadyToGet(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            }

            // We are now committed to this put.
            // At this point, _getWaiting is non-null on all receivers.
            // This should mean that _getReceivers is also non-null on all receivers.
            // Perform the transfers and set these to null on each recevier.
            // Setting _getWaiting to null will allow the get thread to return
            // from its corresponding get method.
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];

                            // Indicate to the corresponding get() thread that the put completed.
                            Thread getThread = castReceiver._getWaiting;
                            Receiver[][] getReceivers = castReceiver._getReceivers;
                            castReceiver._getWaiting = null;
                            castReceiver._getReceivers = null;

                            // Set a flag indicating that the get thread can now
                            // return but has not yet returned.
                            castReceiver._getInProgress = thisThread;

                            // The following does a notify on the director.
                            director.threadUnblocked(getThread, null);
                        }
                    }
                }
            }

            // Wait for the get to complete on all my receivers.
            // The get thread will set _putWaiting to null on each receiver as it completes.
            while (_isPutWaitingOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            }

            // Finally, reset the _putInProgress flag.
            // This indicates that this thread has had a chance to react to the
            // null value of _putWaiting that was set by the get thread.
            // This will allow the next invocation of a get method to continue.
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];
                            director.threadUnblocked(
                                    castReceiver._putInProgress, castReceiver);
                            castReceiver._putInProgress = null;
                        }
                    }
                }
            }
        } // synchronized(director)
    }

    /** Put the specified token to all receivers on ony of the channels
     *  in the specified array.   The first index of the specified array
     *  specifies the channel number.  The second index specifies the
     *  receiver number within the group of receivers that get copies from
     *  the same channel. This method blends a conditional rendezvous with
     *  a multiway rendezvous in that it nondeterministically selects a
     *  channel and performs a multiway rendezvous with all the receivers
     *  on that channel.
     *  This method does not return until one of the puts is complete.
     *  @param token The token to put.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of CSPReceiver.
     *  @param director The director, on which this method synchronizes.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs is to be terminated.
     */
    public static void putToAny(Token token, Receiver[][] receivers,
            CSPDirector director) throws IllegalActionException,
            TerminateProcessException {
        if ((receivers == null) || (receivers.length == 0)) {
            throw new InternalErrorException("No receivers!");
        }

        synchronized (director) {
            Thread thisThread = Thread.currentThread();

            // Prior to returning, a previous invocation of this method will
            // have set _getWaiting to null, but the get thread may not have
            // been given a chance to react to this by returning. Thus, we
            // have to wait here until all get threads have had a chance to
            // react and return.
            while (_isGetInProgressOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            }

            // For each channel.
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    // For each copy within the channel.
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];
                            castReceiver._putWaiting = thisThread;
                            castReceiver._putReceivers = receivers;
                            castReceiver._putConditional = true;

                            // Perform the transfer.
                            // Note that this makes the token available to all
                            // receivers, although the one that completes the
                            // rendezvous will use it.
                            IOPort port = castReceiver.getContainer();
                            castReceiver._token = port.convert(token);

                            // Any receiver that is blocked could get unblocked by this.
                            // NOTE: If the get side is multiway, we could be more
                            // specific here, but there is no harm in marking unblocked.
                            // It will be notified in any case, and will mark itself
                            // blocked again if it is still blocked.
                            if (castReceiver._getWaiting != null) {
                                director.threadUnblocked(
                                        castReceiver._getWaiting, null);
                            }
                        }
                    }
                }
            }

            // Iterate until a receiver is found whose get side is
            // ready to commit.
            while (true) {
                // If there are no gets pending on any channel, then wait
                // until all the receivers on at least one channel are ready.
                while (!_isGetWaitingOnAnyChannel(receivers)) {
                    try {
                        // The following does a notifyAll() on the director.
                        director.threadBlocked(thisThread, null);
                        waitForChange(director);
                    } finally {
                        director.threadUnblocked(thisThread, null);
                    }
                }

                // At this point, we have a get waiting on all the
                // receivers at least one channel.
                // If this method was called before any get()
                // was ready, then by the time we get here, some other thread
                // will have picked the winning receivers by setting their
                // _wins flags. Iterate through the receivers
                // until we find one that won the get. We must then
                // select the channel that that receiver is in.
                // NOTE: It should be that the _wins flag is set
                // in all the receivers in the channel.
                Receiver[] winners = null;
                int winningChannel = 0;

                for (int i = 0; i < receivers.length; i++) {
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            if (receivers[i][j] != null) {
                                if (((CSPReceiver) receivers[i][j])._wins) {
                                    // The winner is the entire channel.
                                    winners = receivers[i];
                                    winningChannel = i;
                                    break;
                                }
                            }
                        }
                    }

                    if (winners != null) {
                        break;
                    }
                }

                // If no previously designated winning channel has been
                // found, then in a second pass, find
                // the first channel that is ready to complete the rendezvous.
                // FIXME: Here, we could implement some fairness
                // scheme rather than always selecting the first channel
                // that is ready to commit.
                if (winners == null) {
                    for (int i = 0; i < receivers.length; i++) {
                        if (receivers[i] != null) {
                            if (_areReadyToGet(receivers[i])) {
                                winners = receivers[i];
                                winningChannel = i;
                                break;
                            }
                        }
                    }
                }

                // It is possible that we have still not found a
                // winning get, in which case, we just wait.
                // But if we have found a winning get, then we are
                // ready to complete the rendezvous and return.
                if (winners != null) {
                    // The get is ready to complete.
                    // We are now committed.
                    // Complete the rendezvous.
                    // First, retract the _putWaiting on all receivers
                    // except the ones in the channel to which we are committing.
                    for (int k = 0; k < receivers.length; k++) {
                        if ((k != winningChannel) && (receivers[k] != null)) {
                            for (int m = 0; m < receivers[k].length; m++) {
                                if (receivers[k][m] != null) {
                                    CSPReceiver otherCastReceiver = (CSPReceiver) receivers[k][m];
                                    otherCastReceiver._putWaiting = null;
                                    otherCastReceiver._putReceivers = null;
                                }
                            }
                        }
                    }

                    // When we get here, all receivers in the winners array
                    // are ready to complete the rendezvous.
                    for (int i = 0; i < winners.length; i++) {
                        CSPReceiver winner = (CSPReceiver) winners[i];
                        Thread getThread = winner._getWaiting;
                        Receiver[][] getReceivers = winner._getReceivers;
                        winner._getWaiting = null;
                        winner._getReceivers = null;

                        // Indicate that a get is in progress.
                        winner._getInProgress = thisThread;

                        // The following does a notify on the director.
                        director.threadUnblocked(getThread, null);
                    }

                    // Wait for the get to complete on the selected receivers.
                    while (_isPutWaitingOnAny(winners)) {
                        try {
                            // The following does a notifyAll() on the director.
                            director.threadBlocked(thisThread, null);
                            waitForChange(director);
                        } finally {
                            director.threadUnblocked(thisThread, null);
                        }
                    }

                    // Finally, reset the _putInProgress flag.
                    for (int i = 0; i < winners.length; i++) {
                        CSPReceiver winner = (CSPReceiver) winners[i];
                        director.threadUnblocked(winner._putInProgress, winner);
                        winner._putInProgress = null;
                        winner._wins = false;
                    }

                    return;
                }

                // Wait for a change.
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(thisThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(thisThread, null);
                }
            } // while(true)
        } // synchronized(director)
    }

    /** The model has finished executing, so set a flag so that the
     *  next time an actor tries to get or put it gets a
     *  TerminateProcessException which will cause it to finish.
     */
    public void requestFinish() {
        Object lock = _getDirector();

        synchronized (lock) {
            // Need to reset the state of the receiver.
            _setConditionalReceive(false, null, -1);
            _setConditionalSend(false, null, -1);

            if (_putWaiting != null) {
                _getDirector().threadUnblocked(_putWaiting, this);
            }

            if (_getWaiting != null) {
                _getDirector().threadUnblocked(_getWaiting, this);
            }

            _putWaiting = null;
            _getWaiting = null;

            // Wake up any pending threads. EAL 12/04
            lock.notifyAll();
        }
    }

    /** Reset local flags.
     */
    public void reset() {
        Object lock = _getDirector();

        synchronized (lock) {
            _getWaiting = null;
            _putWaiting = null;
            _setConditionalReceive(false, null, -1);
            _setConditionalSend(false, null, -1);
            _boundaryDetector.reset();
            _putInProgress = null;
            _getInProgress = null;
            _wins = false;
        }
    }

    /** Wait on the specified director.
     *  This is not synchronized on the specified director, so the called should be.
     *  @param director The director on which to wait.
     *  @exception TerminateProcessException If a finish has been requested
     *   of the specified director, or if the calling thread is interrupted while waiting.
     */
    public static void waitForChange(CSPDirector director)
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
    // FIXME: Most of these should be private.
    // Prune the ones that are no longer needed.

    /** Return true if the specified receivers are all ready to complete
     *  a rendezvous, assuming they are ready to put.  A receiver is ready if
     *  <ul>
     *  <li> it has a get waiting, and
     *  <li> if the get is not conditional, then all its get receivers
     *       are ready to put.
     *  </ul>
     *  If this method returns true, then any receivers that are
     *  involved that are doing a conditional put or conditional get
     *  commit to this particular rendezvous. That is, they become
     *  unwilling to perform an alternative rendezvous. Thus, if this
     *  method returns true, the caller is obligated to complete
     *  the rendezvous.
     *  @param receivers The receivers.
     *  @return True if the specified receiver is ready to complete
     *   a rendezvous.
     */
    protected static boolean _areReadyToGet(Receiver[][] receivers) {
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (!_checkReadyToGet((CSPReceiver) receivers[i][j],
                                null)) {
                            // No need to continue. One of the receivers is not ready.
                            return false;
                        }
                    }
                }
            }
        }

        // If we get here, then all receivers were ready. Commit.
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        _commitGet((CSPReceiver) receivers[i][j]);
                    }
                }
            }
        }

        return true;
    }

    /** Return true if the specified receivers are all ready to complete
     *  a rendezvous, assuming they are ready to put.  A receiver is ready if
     *  <ul>
     *  <li> it has a get waiting, and
     *  <li> if the get is not conditional, then all its get receivers
     *       are ready to put.
     *  </ul>
     *  If this method returns true, then any receivers that are
     *  involved that are doing a conditional put or conditional get
     *  commit to this particular rendezvous. That is, they become
     *  unwilling to perform an alternative rendezvous. Thus, if this
     *  method returns true, the caller is obligated to complete
     *  the rendezvous.
     *  @param receivers The receiver.
     *  @return True if the specified receiver is ready to complete
     *   a rendezvous.
     */
    protected static boolean _areReadyToGet(Receiver[] receivers) {
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                if (!_checkReadyToGet((CSPReceiver) receivers[i], null)) {
                    // No need to continue. One of the receivers is not ready.
                    return false;
                }
            }
        }

        // If we get here, then all receivers were ready. Commit.
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                _commitGet((CSPReceiver) receivers[i]);
            }
        }

        return true;
    }

    /** Return true if the specified receivers are all ready to complete
     *  a rendezvous, assuming they are ready to get.  A receiver is ready if
     *  <ul>
     *  <li> it has a put waiting, and
     *  <li> if the put is not conditional, then all its put receivers
     *       are ready to get, and
     *  <li> if the put is conditional, then all the receivers in
     *       same channel are ready to put.
     *  </ul>
     *  If this method returns true, then any receivers that are
     *  involved that are doing a conditional put or conditional get
     *  commit to this particular rendezvous. That is, they become
     *  unwilling to perform an alternative rendezvous. Thus, if this
     *  method returns true, the caller is obligated to complete
     *  the rendezvous.
     *  @param receivers The receiver.
     *  @return True if the specified receiver is ready to complete
     *   a rendezvous.
     */
    protected static boolean _areReadyToPut(Receiver[][] receivers) {
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (!_checkReadyToPut((CSPReceiver) receivers[i][j],
                                null)) {
                            // No need to continue. One of the receivers is not ready.
                            return false;
                        }
                    }
                }
            }
        }

        // If we get here, then all receivers were ready. Commit.
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        _commitPut((CSPReceiver) receivers[i][j]);
                    }
                }
            }
        }

        return true;
    }

    /** Return the controller of the conditional branch to reach the
     *  rendezvous point first. For a rendezvous to occur when both
     *  communications at the receiver are from conditional branches,
     *  then the rendezvous can only proceed if <I>both</I> the branches
     *  are the first branches to be ready to succeed for their
     *  respective controllers. This is checked by the second branch to
     *  arrive at the rendezvous point, for which it requires the actor
     *  that created the other branch. Thus the first branch to arrive
     *  stores its controller in the receiver when it is setting the
     *  appropriate flag, which is what is returned by this method.
     *  @return The controller which controls the first conditional
     *   branch to arrive.
     */
    protected AbstractBranchController _getOtherController() {
        return _otherController;
    }

    /** Return the branch ID of the branch that requested the
     *  conditional receive.
     *  @return The branch ID.
     */
    protected int _getOtherID() {
        return _otherID;
    }

    /** Return the director that is controlling the execution of this model.
     *  If this receiver is an inside receiver, then it is the director
     *  of the container (actor) of the container (port). Otherwise, it
     *  is the executive director of the container (actor) of the container
     *  (port).
     *  @return The CSPDirector controlling this model.
     */
    protected CSPDirector _getDirector() {
        try {
            Actor container = (Actor) getContainer().getContainer();

            if (isInsideBoundary()) {
                return (CSPDirector) container.getDirector();
            } else {
                return (CSPDirector) container.getExecutiveDirector();
            }
        } catch (NullPointerException ex) {
            // If a thread has a reference to a receiver with no director it
            // is an error so terminate the process.
            throw new TerminateProcessException("CSPReceiver: trying to "
                    + " rendezvous with a receiver with no "
                    + "director => terminate.");
        }
    }

    /** Return whether a getFromAny() is pending on this receiver.
     *  @return True if a getFromAny() is pending on this receiver.
     */
    protected boolean _isConditionalReceiveWaiting() {
        // FIXME: Obsolete.
        return (_getWaiting != null) && _getConditional;
    }

    /** Return whether a putToAny() is pending on this receiver.
     *  @return True if a putToAny() is pending on this receiver.
     */
    protected boolean _isConditionalSendWaiting() {
        // FIXME: Obsolete.
        return (_putWaiting != null) && _putConditional;
    }

    /** Return true if a get() is pending on all the specified
     *  receivers. If the argument is null, then this method
     *  returns false.
     *  @param receivers The receivers, which are assumed to be
     *   instances of CSPReceiver.
     *  @param except A receiver to not consider, or null to
     *   consider all receivers.
     *  @return True if a get() is pending on the specified receivers.
     */
    protected static boolean _isGetWaitingOnAll(Receiver[][] receivers,
            Receiver except) {
        if (receivers == null) {
            return false;
        }

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != except) {
                        if (((CSPReceiver) receivers[i][j])._getWaiting == null) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /** Return true if a get is in progress on any of the specified
     *  receivers. If the argument is null, then this method
     *  returns false.
     *  @param receivers The receivers, which are assumed to be
     *   instances of CSPReceiver.
     *  @return True if a get is in progress on any of the
     *   specified receivers.
     */
    protected static boolean _isGetInProgressOnAny(Receiver[][] receivers) {
        if (receivers == null) {
            return false;
        }

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (((CSPReceiver) receivers[i][j])._getInProgress != null) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** Return true if a get() is pending on any of the specified
     *  receivers. If the argument is null, then this method
     *  returns false.
     *  @param receivers The receivers, which are assumed to be
     *   instances of CSPReceiver.
     *  @return True if a get() is pending on any of the
     *   specified receivers.
     */
    protected static boolean _isGetWaitingOnAny(Receiver[][] receivers) {
        if (receivers == null) {
            return false;
        }

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (((CSPReceiver) receivers[i][j])._getWaiting != null) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** Return true if a get() is pending on all of the
     *  receivers in any of the channels of the specified array
     *  of receivers. If the argument is null, then this method
     *  returns false.
     *  @param receivers The receivers, which are assumed to be
     *   instances of CSPReceiver.
     *  @return True if a get() is pending on all the receivers
     *   of any channel of the specified receivers.
     */
    protected static boolean _isGetWaitingOnAnyChannel(Receiver[][] receivers) {
        if (receivers == null) {
            return false;
        }

        // Channel number is i.
        for (int i = 0; i < receivers.length; i++) {
            if ((receivers[i] != null) && (receivers[i].length > 0)) {
                boolean result = true;
                boolean foundOne = false;

                // Copy number is j.
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        foundOne = true;

                        if (((CSPReceiver) receivers[i][j])._getWaiting == null) {
                            result = false;
                            break;
                        }
                    }
                }

                if (foundOne && result) {
                    return result;
                }
            }
        }

        return false;
    }

    /** Return whether a get() is waiting to rendezvous
     *  at this receiver.
     *  @return True if a get() is waiting to rendezvous.
     */
    protected boolean _isGetWaiting() {
        // FIXME: Obsolete.
        return _getWaiting != null;
    }

    /** Return true if a put is in progress on any of the specified
     *  receivers. If the argument is null, then this method
     *  returns false.
     *  @param receivers The receivers, which are assumed to be
     *   instances of CSPReceiver.
     *  @return True if a put is in progress on any of the
     *   specified receivers.
     */
    protected static boolean _isPutInProgressOnAny(Receiver[][] receivers) {
        if (receivers == null) {
            return false;
        }

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (((CSPReceiver) receivers[i][j])._putInProgress != null) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** Flag indicating whether or not a put() is waiting to rendezvous
     *  at this receiver.
     *  @return True if a put() is waiting to rendezvous.
     */
    protected boolean _isPutWaiting() {
        // FIXME: Obsolete.
        return _putWaiting != null;
    }

    /** Return true if a put() is pending on all the specified
     *  receivers. If the argument is null, then this method
     *  returns false.
     *  @param receivers The receivers, which are assumed to be
     *   instances of CSPReceiver.
     *  @return True if a put() is pending on the specified receivers.
     */
    protected static boolean _isPutWaitingOnAll(Receiver[][] receivers) {
        if (receivers == null) {
            return false;
        }

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (((CSPReceiver) receivers[i][j])._putWaiting == null) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /** Return true if a put() is pending on any of the specified
     *  receivers. If the argument is null, then this method
     *  returns false.
     *  @param receivers The receivers, which are assumed to be
     *   instances of CSPReceiver.
     *  @return True if a put() is pending on any of
     *   the specified receivers.
     */
    protected static boolean _isPutWaitingOnAny(Receiver[][] receivers) {
        if (receivers == null) {
            return false;
        }

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (((CSPReceiver) receivers[i][j])._putWaiting != null) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** Return true if a put() is pending on any of the specified
     *  receivers. If the argument is null, then this method
     *  returns false.
     *  @param receivers The receivers, which are assumed to be
     *   instances of CSPReceiver.
     *  @return True if a put() is pending on any of
     *   the specified receivers.
     */
    protected static boolean _isPutWaitingOnAny(Receiver[] receivers) {
        if (receivers == null) {
            return false;
        }

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                if (((CSPReceiver) receivers[i])._putWaiting != null) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Return true if the specified receiver is ready to complete
     *  a rendezvous, assuming it is ready to get.  It is ready if
     *  <ul>
     *  <li> it has a put waiting, and
     *  <li> if it is not conditional, then all its put receivers
     *       are ready to get.
     *  </ul>
     *  If this method returns true, then any receivers that are
     *  involved that are doing a conditional put or conditional get
     *  commit to this particular rendezvous. That is, they become
     *  unwilling to perform an alternative rendezvous. Thus, if this
     *  method returns true, the caller is obligated to complete
     *  the rendezvous.
     *  @param receiver The receiver.
     *  @return True if the specified receiver is ready to complete
     *   a rendezvous.
     */
    protected static boolean _isReadyToPut(CSPReceiver receiver) {
        boolean result = _checkReadyToPut(receiver, null);

        if (result) {
            _commitPut(receiver);
        }

        return result;
    }

    /** Set a flag so that a ConditionalReceive branch knows whether or
     *  not a ConditionalSend is ready to rendezvous with it.
     *  @param ready Boolean indicating whether or not a conditional
     *   send is waiting to rendezvous.
     *  @param controller The controller which contains the ConditionalSend
     *   branch that is trying to rendezvous. It is stored in the
     *   receiver so that if a ConditionalReceive arrives, it can easily
     *   check whether the ConditionalSend branch was the first
     *   branch of its conditional construct(CIF or CDO) to succeed.
     *  @param otherID The branch ID of the branch requesting the
     *   conditional send.
     */
    protected void _setConditionalSend(boolean ready,
            AbstractBranchController controller, int otherID) {
        CSPDirector director = _getDirector();

        synchronized (director) {
            if (ready) {
                _conditionalSendWaiting = Thread.currentThread();
            } else {
                if (_conditionalSendWaiting != null) {
                    director.threadUnblocked(_conditionalSendWaiting, this);
                }

                _conditionalSendWaiting = null;
            }

            _otherController = controller;
            _otherID = otherID;
            director.notifyAll();
        }
    }

    /** Set a flag so that a ConditionalSend branch knows whether or
     *  not a ConditionalReceive is ready to rendezvous with it.
     *  @param ready Boolean indicating whether or not a conditional
     *   receive is waiting to rendezvous.
     *  @param controller The CSPActor which contains the ConditionalReceive
     *   branch that is trying to rendezvous. It is stored in the
     *   receiver so that if a ConditionalSend arrives, it can easily
     *   check whether the ConditionalReceive branch was the first
     *   branch of its conditional construct(CIF or CDO) to succeed.
     *  @param otherID The branch ID of the branch requesting the
     *   conditional receive.
     */
    protected void _setConditionalReceive(boolean ready,
            AbstractBranchController controller, int otherID) {
        CSPDirector director = _getDirector();

        synchronized (director) {
            if (ready) {
                _conditionalReceiveWaiting = Thread.currentThread();
            } else {
                if (_conditionalReceiveWaiting != null) {
                    director.threadUnblocked(_conditionalReceiveWaiting, this);
                }

                _conditionalReceiveWaiting = null;
            }

            _otherController = controller;
            _otherID = otherID;
            director.notifyAll();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if the specified receiver is ready to complete
     *  a rendezvous, assuming it is ready to put.  It is ready if
     *  <ul>
     *  <li> it has a get waiting, and
     *  <li> if the get is not conditional, then all its get receivers
     *       are ready to get.
     *  </ul>
     *  This method does not commit. The caller should call _commitPut()
     *  if the rendezvous is ready to complete.
     *  @param receiver The receiver.
     *  @param except A receiver to skip, or null to not skip any.
     *  @return True if the specified receiver is ready to complete
     *   a rendezvous.
     */
    private static boolean _checkReadyToGet(CSPReceiver receiver,
            CSPReceiver except) {
        // It is possible that the rendezvous has partly completed,
        // in which case we return true.
        if ((receiver._getInProgress != null)
                || (receiver._putInProgress != null)) {
            return true;
        }

        // Next check that there is a get waiting. If not, return false.
        if (receiver._getWaiting == null) {
            return false;
        }

        // If the get is conditional, then having a get waiting
        // is sufficient, and we don't have to go any further.
        if (receiver._getConditional) {
            return true;
        }

        // The get is not conditional. All other get receivers
        // have to be ready. We assume that since recevier is
        // ready to get, that all other get receivers are
        // ready to get. Thus, we only have to check whether
        // they are ready to put.
        // Skip the argument receiver when checking
        // (or we'll get an infinite loop).
        Receiver[][] receivers = receiver._getReceivers;

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if ((receivers[i][j] != receiver)
                            && (receivers[i][j] != except)) {
                        // Check that each receiver is ready to put (we
                        // know it's ready to get because it's in the _getReceivers list).
                        // Specify to skip this receiver, to avoid infinite loop.
                        if (!_checkReadyToPut((CSPReceiver) receivers[i][j],
                                receiver)) {
                            return false;
                        }
                    }
                }
            }
        }

        // If we haven't returned false by now, then we are ready to commit.
        return true;
    }

    /** Return true if the specified receiver is ready to complete
     *  a rendezvous, assuming it is ready to get.  It is ready if
     *  <ul>
     *  <li> it has a put waiting, and
     *  <li> if the put is not conditional, then all its put receivers
     *       are ready to get, and
     *  <li> if the put is conditional, then all its put receivers
     *       in the same channel are ready to get.
     *  </ul>
     *  This method does not commit. The caller should call _commitPut()
     *  if the rendezvous is ready to complete.
     *  @param receiver The receiver.
     *  @param except A receiver to skip, or null to not skip any.
     *  @return True if the specified receiver is ready to complete
     *   a rendezvous.
     */
    private static boolean _checkReadyToPut(CSPReceiver receiver,
            CSPReceiver except) {
        // It is possible that the rendezvous has partly completed,
        // in which case we return true.
        if ((receiver._getInProgress != null)
                || (receiver._putInProgress != null)) {
            return true;
        }

        // Next check that there is a put waiting. If not, return false.
        if (receiver._putWaiting == null) {
            return false;
        }

        Receiver[][] receivers = receiver._putReceivers;

        // If the put is conditional, then need to determine whether
        // all the receivers in the same channel are ready to put.
        if (receiver._putConditional) {
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    // Regrettably, we first need to determine whether
                    // channel i is the channel containing this receiver.
                    boolean isChannel = false;

                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] == receiver) {
                            isChannel = true;
                            break;
                        }
                    }

                    if (isChannel) {
                        // Check that all the receivers in the channel are ready.
                        for (int j = 0; j < receivers[i].length; j++) {
                            if ((receivers[i][j] != receiver)
                                    && (receivers[i][j] != except)) {
                                // Check that each receiver is ready to get (we know
                                // it's ready to put because it's in the _putReceivers list).
                                if (!_checkReadyToGet(
                                        (CSPReceiver) receivers[i][j], receiver)) {
                                    return false;
                                }
                            }
                        }

                        // If we get here, then all the receivers in the channel are ready.
                        return true;
                    }
                }
            }

            // If we get here, then the receiver was not found in its own
            // _putReceivers array, which is an error.
            throw new InternalErrorException(
                    "Receiver not found in its own _putReceivers array!");
        }

        // The put is not conditional. All other put receivers
        // have to be ready. We assume that since recevier is
        // ready to put, that all other put receivers are
        // ready to put. Thus, we only have to check whether
        // they are ready to get.
        // Skip the argument receiver when checking
        // (or we'll get an infinite loop).
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if ((receivers[i][j] != receiver)
                            && (receivers[i][j] != except)) {
                        // Check that each receiver is ready to get (we know
                        // it's ready to put because it's in the _putReceivers list).
                        if (!_checkReadyToGet((CSPReceiver) receivers[i][j],
                                receiver)) {
                            return false;
                        }
                    }
                }
            }
        }

        // If we haven't returned false by now, then we are ready to commit.
        return true;
    }

    /** Commit the specified receiver to a get. This sets the _wins
     *  flag, and if the get is conditional, then it retracts the get waiting flag
     *  on all other get receivers. If it is not conditional, then it
     *  will call _commitPut() on all other get receivers (besides
     *  the specified receiver).
     *  @param receiver The receiver.
     */
    private static void _commitGet(CSPReceiver receiver) {
        if (receiver._getWaiting == null) {
            // Nothing to do. This should not happen.
            return;
        }

        if (receiver._getConditional) {
            receiver._wins = true;
        }

        Receiver[][] receivers = receiver._getReceivers;

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != receiver) {
                        CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];

                        // If the get is conditional, then retract get waiting
                        // on all other get receivers.
                        if (receiver._getConditional) {
                            castReceiver._getWaiting = null;
                            castReceiver._getReceivers = null;
                        } else {
                            // If the get is not conditional, then propagate
                            // the commit to the other put receivers.
                            _commitPut(castReceiver);
                        }
                    }
                }
            }
        }
    }

    /** Commit the specified receiver to a put. This sets the _wins
     *  flag, and if the put is conditional, then it retracts the put waiting flag
     *  on all other put receivers that are not in the same channel and call
     *  _commitGet() on all other receivers in the same channel.
     *  If it is not conditional, then it
     *  will call _commitGet() on all other put receivers (besides
     *  the specified receiver).
     */
    private static void _commitPut(CSPReceiver receiver) {
        if (receiver._putWaiting == null) {
            // Nothing to do. This should not happen.
            return;
        }

        if (receiver._putConditional) {
            receiver._wins = true;
        }

        Receiver[][] receivers = receiver._putReceivers;

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                // Regrettably, we first need to determine whether
                // channel i is the channel containing this receiver.
                boolean isChannel = false;

                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] == receiver) {
                        isChannel = true;
                        break;
                    }
                }

                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != receiver) {
                        CSPReceiver castReceiver = (CSPReceiver) receivers[i][j];

                        // If the get is conditional, then retract get waiting
                        // on all other get receivers not in the same channel.
                        if (receiver._putConditional) {
                            if (isChannel) {
                                _commitGet(castReceiver);
                            } else {
                                castReceiver._putWaiting = null;
                                castReceiver._putReceivers = null;
                            }
                        } else {
                            // If the put is not conditional, then propagate
                            // the commit to the other get receivers.
                            _commitGet(castReceiver);
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The boundary detector. */
    private BoundaryDetector _boundaryDetector;

    /** Flag indicating whether or not a conditional receive is waiting to rendezvous. */

    // FIXME: Obsolete.
    private Thread _conditionalReceiveWaiting = null;

    /** Flag indicating whether or not a conditional send is waiting to rendezvous. */

    // FIXME: Obsolete.
    private Thread _conditionalSendWaiting = null;

    /** Flag indicating that the _getWaiting thread is a conditional rendezvous. */
    private boolean _getConditional = false;

    /** Indicator that a get method can now return but has not yet returned. */
    private Thread _getInProgress = null;

    /** The receivers currently being gotten data from. */
    private Receiver[][] _getReceivers = null;

    /** Indicator that a get() is waiting on this receiver. */
    private Thread _getWaiting = null;

    /** The controller in charge of the conditional send or get. */

    // FIXME: Obsolete.
    private AbstractBranchController _otherController = null;

    /** The ID of the branch in a conditional send or get. */

    // FIXME: Obsolete.
    private int _otherID = -1;

    /** Flag indicating that the _putWaiting thread is a conditional rendezvous. */
    private boolean _putConditional = false;

    /** Indicator that a put method can now return but has not yet returned. */
    private Thread _putInProgress = null;

    /** The receivers currently being put data to. */
    private Receiver[][] _putReceivers = null;

    /** Indicator that a put() is waiting on this receiver. */
    private Thread _putWaiting = null;

    /** Array with just one receiver, this one, for convenience. */
    private Receiver[][] _thisReceiver = new Receiver[1][1];

    /** The token being transferred during the rendezvous. */
    private Token _token;

    /** Flag indicating that this receiver wins a conditional rendezvous competition. */
    private boolean _wins = false;
}
