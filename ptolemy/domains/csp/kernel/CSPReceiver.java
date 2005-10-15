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
 @author Neil Smyth, John S. Davis II, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (nsmyth)
 @Pt.AcceptedRating Red (cxh)
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
    public static Token[][] getFromAll(Receiver[][] receivers, CSPDirector director)
            throws TerminateProcessException {
        if (receivers == null || receivers.length == 0) {
            throw new InternalErrorException(
                    "No receivers!");
        }
        synchronized(director) {
            Thread getFromAllThread = Thread.currentThread();
            // If there is a put in progress for any of the receivers, wait
            // for it to complete.
            /* FIXME: needed? */
            while (_isPutInProgressOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(getFromAllThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(getFromAllThread, null);
                }
            }
                
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver)receivers[i][j];
                            castReceiver._getWaiting = getFromAllThread;
                            castReceiver._getReceivers = receivers;
                            castReceiver._getConditional = false;
                            // If this particular get unblocks a getting thread,
                            // then mark it unblocked now.
                            // Note that if the put side is multiway,
                            // then this may not actually unblock it.
                            // But this is harmless since that thread will be
                            // notified in any case, and it will mark itself
                            // blocked if it is still blocked.
                            if (castReceiver._putWaiting != null) {
                                director.threadUnblocked(castReceiver._putWaiting, null);
                            }
                        }
                    }
                }
            }
            while (!_areReadyToPut(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(getFromAllThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(getFromAllThread, null);
                }
            }
            // At this point, _putWaiting is non-null on all receivers.
            // This should mean that _putReceivers is also non-null on all receivers.
            Token[][] result = new Token[receivers.length][];
            
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    result[i] = new Token[receivers[i].length];
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            // Perform the transfer.
                            CSPReceiver castReceiver = (CSPReceiver)receivers[i][j];
                            result[i][j] = castReceiver._token;
                            // Indicate to the corresponding put() thread that the put completed.
                            Thread putThread = castReceiver._putWaiting;
                            Receiver[][] putReceivers = castReceiver._putReceivers;
                            castReceiver._putWaiting = null;
                            castReceiver._putReceivers = null;
                            if (castReceiver._putConditional) {
                                // The far side (the put side) is conditional.
                                // Since this get succeeds, we now need to retract
                                // all the _putWaiting flags on the far side receivers.
                                for (int k = 0; k < putReceivers.length; k++) {
                                    if (putReceivers[k] != null) {
                                        for (int m = 0; m < putReceivers[k].length; m++) {
                                            if (putReceivers[k][m] != null) {
                                                CSPReceiver castFarReceiver = (CSPReceiver)putReceivers[k][m];
                                                castFarReceiver._putWaiting = null;
                                                castFarReceiver._putReceivers = null;
                                            }
                                        }
                                    }
                                }
                                // We also need to mark this receiver as the
                                // winning one to ensure that the putToAny()
                                // selects the same winner.
                                castReceiver._winsThePut = true;
                            }
                            // Indicate that a put is in progress.
                            castReceiver._putInProgress = true;
                            // The following does a notify on the director.
                            director.threadUnblocked(putThread, null);
                        }
                    }
                }
            }
            // Wait for the put to complete on all my receivers.
            while (_isGetWaitingOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(getFromAllThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(getFromAllThread, null);
                }
            }
            
            // Finally, reset the _getInProgress flag.
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            ((CSPReceiver)receivers[i][j])._getInProgress = false;
                            ((CSPReceiver)receivers[i][j])._winsTheGet = false;
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
        if (receivers == null || receivers.length == 0) {
            throw new InternalErrorException(
                    "No receivers!");
        }
        synchronized(director) {
            Thread getFromAnyThread = Thread.currentThread();
            // If there is a put in progress for any of the receivers, wait
            // for it to complete.
            /* FIXME: needed? */
            while (_isPutInProgressOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(getFromAnyThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(getFromAnyThread, null);
                }
            }
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver)receivers[i][j];
                            castReceiver._getWaiting = getFromAnyThread;
                            castReceiver._getReceivers = receivers;
                            castReceiver._getConditional = true;
                            // Any receiver that is blocked could get unblocked by this.
                            // NOTE: If the put side is multiway, we could be more
                            // specific here, but there is no harm in marking unblocked.
                            // It will be notified in any case, and will mark itself
                            // blocked again if it is still blocked.
                            if (castReceiver._putWaiting != null) {
                                director.threadUnblocked(castReceiver._putWaiting, null);
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
                        director.threadBlocked(getFromAnyThread, null);
                        waitForChange(director);
                    } finally {
                        director.threadUnblocked(getFromAnyThread, null);
                    }
                }
                // At this point, we have a put waiting on at least one
                // sender.  If this method was called before any put()
                // was ready, then by the time we get here, some other thread
                // will have picked the winning receiver by setting
                // _winsTheGet. Iterate through the receivers
                // until we find one that won the put, or if there
                // is none, one that is ready to complete
                // the rendezvous.
                // FIXME: Here, we could implement some fairness
                // scheme rather than always selecting the first receiver
                // that is ready to commit.
                CSPReceiver winner = null;
                for (int i = 0; i < receivers.length; i++) {
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            if (receivers[i][j] != null) {
                                winner = (CSPReceiver)receivers[i][j];
                                if (_isReadyToPut(winner)) {
                                    if (winner._winsTheGet) {
                                        // Do not search any further.
                                        // This receiver won the get by
                                        // getting there first on the other side.
                                        break;
                                    }
                                } else {
                                    winner = null;
                                }
                            }
                        }
                    }
                    if (winner != null && winner._winsTheGet) {
                        break;
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
                                    CSPReceiver otherCastReceiver = (CSPReceiver)receivers[k][m];
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
                    if (winner._putConditional) {
                        // The far side (the put side) is conditional.
                        // Since this get succeeds, we now need to retract
                        // all the _putWaiting flags on the far side receivers.
                        for (int k = 0; k < putReceivers.length; k++) {
                            if (putReceivers[k] != null) {
                                for (int m = 0; m < putReceivers[k].length; m++) {
                                    if (putReceivers[k][m] != null) {
                                        CSPReceiver castFarReceiver = (CSPReceiver)putReceivers[k][m];
                                        castFarReceiver._putWaiting = null;
                                        castFarReceiver._putReceivers = null;
                                    }
                                }
                            }
                        }
                        // We also need to mark this receiver as the
                        // winning one to ensure that the putToAny()
                        // selects the same winner.
                        winner._winsThePut = true;
                    }
                    // Indicate that a put is in progress.
                    winner._putInProgress = true;
                    // The following does a notify on the director.
                    director.threadUnblocked(putThread, null);

                    // Wait for the put to complete on the selected receiver.
                    while (winner._getWaiting != null) {
                        try {
                            // The following does a notifyAll() on the director.
                            director.threadBlocked(getFromAnyThread, null);
                            waitForChange(director);
                        } finally {
                            director.threadUnblocked(getFromAnyThread, null);
                        }                                
                    }
                    // Finally, reset the _getInProgress flag.
                    winner._getInProgress = false;
                    winner._winsTheGet = false;

                    return result;
                }
                // Wait for a change.
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(getFromAnyThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(getFromAnyThread, null);
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
        synchronized(_getDirector()) {
            return (_getWaiting != null) || (_conditionalReceiveWaiting != null);
        }
    }

    /** Return true if there is either a put or a conditional send
     *  waiting on this receiver.
     *  @return A boolean indicating whether a write is pending on this
     *   receiver.
     */
    public boolean isWriteBlocked() {
        synchronized(_getDirector()) {
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
    public void put(Token token) throws IllegalActionException, TerminateProcessException {
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
    public void putArrayToAll(
            Token[] tokens, int numberOfTokens, Receiver[] receivers)
            throws NoRoomException, IllegalActionException, TerminateProcessException {
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
        if (receivers == null || receivers.length == 0) {
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
    public static void putToAll(Token[][] tokens, Receiver[][] receivers, CSPDirector director)
            throws IllegalActionException, TerminateProcessException {
        if (receivers == null || receivers.length == 0) {
            return;
        }
        synchronized(director) {
            Thread putToAllThread = Thread.currentThread();
            
            // If there is a get in progress for any of the receivers, wait
            // for it to complete.
            /* FIXME: needed? */
            while (_isGetInProgressOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(putToAllThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(putToAllThread, null);
                }
            }
            Token token = null;
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            if (tokens.length > i && tokens[i] != null && tokens[i].length > j) {
                                token = tokens[i][j];
                            }
                            CSPReceiver castReceiver = (CSPReceiver)receivers[i][j];
                            castReceiver._putWaiting = putToAllThread;
                            castReceiver._putReceivers = receivers;
                            // Perform the transfer.
                            IOPort port = castReceiver.getContainer();
                            castReceiver._token = port.convert(token);
                            // If the get side is not a conditional get and this put
                            // is its last required put, then the get side thread is
                            // now unblocked. Mark it so.
                            // NOTE: If the get side is conditional, then it is not
                            // assured that this action will unblock it, but it might.
                            // There is no harm in marking it unblocked.
                            if (castReceiver._getWaiting != null) {
                                if (castReceiver._getConditional
                                        || _isPutWaitingOnAll(castReceiver._getReceivers)) {
                                    director.threadUnblocked(castReceiver._getWaiting, null);
                                }
                            }
                        }
                    }
                }
            }
            while (!_areReadyToGet(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(putToAllThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(putToAllThread, null);
                }
            }
            // When we get here, all receivers are ready to complete the rendezvous.
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver)receivers[i][j];
                            // Indicate to the corresponding get() thread that the put completed.
                            Thread getThread = castReceiver._getWaiting;
                            Receiver[][] getReceivers = castReceiver._getReceivers;
                            castReceiver._getWaiting = null;
                            castReceiver._getReceivers = null;
                            if (castReceiver._getConditional) {
                                // The far side (get side) is conditional.
                                // Since this put succeeds, we now need to retract
                                // all the _getWaiting flags on far side receivers.
                                for (int k = 0; k < getReceivers.length; k++) {
                                    if (getReceivers[k] != null) {
                                        for (int m = 0; m < getReceivers[k].length; m++) {
                                            if (getReceivers[k][m] != null) {
                                                CSPReceiver castFarReceiver = (CSPReceiver)getReceivers[k][m];
                                                castFarReceiver._getWaiting = null;
                                                castFarReceiver._getReceivers = null;
                                            }
                                        }
                                    }
                                }
                                // We also need to mark this receiver as the
                                // winning one to ensure that the getFromAny()
                                // selects the same winner.
                                castReceiver._winsTheGet = true;
                            }
                            // Indicate that a get is in progress.
                            castReceiver._getInProgress = true;
                            // The following does a notify on the director.
                            director.threadUnblocked(getThread, null);
                        }
                    }
                }
            }
            while (_isPutWaitingOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(putToAllThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(putToAllThread, null);
                }
            }
            // Finally, reset the _putInProgress flag.
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            ((CSPReceiver)receivers[i][j])._putInProgress = false;
                            ((CSPReceiver)receivers[i][j])._winsThePut = false;
                        }
                    }
                }
            }
        } // synchronized(director)
    }
    
    /** Put the specified token to any receiver in the specified array.
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
    public static void putToAny(Token token, Receiver[][] receivers, CSPDirector director)
            throws IllegalActionException, TerminateProcessException {
        if (receivers == null || receivers.length == 0) {
            throw new InternalErrorException(
                    "No receivers!");
        }
        synchronized(director) {
            Thread putToAnyThread = Thread.currentThread();

            // If there is a get in progress for any of the receivers, wait
            // for it to complete.
            /* FIXME: needed? */
            while (_isGetInProgressOnAny(receivers)) {
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(putToAnyThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(putToAnyThread, null);
                }
            }
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            CSPReceiver castReceiver = (CSPReceiver)receivers[i][j];
                            castReceiver._putWaiting = putToAnyThread;
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
                                director.threadUnblocked(castReceiver._getWaiting, null);
                            }
                        }
                    }
                }
            }
            // Iterate until a receiver is found whose get side is
            // ready to commit.
            while (true) {
                // If there are no gets pending on any of these receivers, then wait
                // until there is at least one.
                while (!_isGetWaitingOnAny(receivers)) {
                    try {
                        // The following does a notifyAll() on the director.
                        director.threadBlocked(putToAnyThread, null);
                        waitForChange(director);
                    } finally {
                        director.threadUnblocked(putToAnyThread, null);
                    }
                }
                // At this point, we have a get waiting on at least one
                // recipient.  If this got here first, before any corresponding
                // gets, then it may be that by the time we get here, another
                // thread has choosen the winning receiver. Iterate through the receivers
                // until we find a winner, or if there is no winner, one that is ready to complete
                // the rendezvous.
                // FIXME: Here, we could implement some fairness
                // scheme rather than always selecting the first receiver
                // that is ready to commit.
                CSPReceiver winner = null;
                for (int i = 0; i < receivers.length; i++) {
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            if (receivers[i][j] != null) {
                                winner = (CSPReceiver)receivers[i][j];
                                if (_isReadyToGet(winner)) {
                                    if (winner._winsThePut) {
                                        // Do not search any further.
                                        // This receiver won the get by
                                        // getting there first on the other side.
                                        break;
                                    }
                                } else {
                                    winner = null;
                                }
                            }
                        }
                    }
                    if (winner != null && winner._winsThePut) {
                        break;
                    }
                }
                // It is possible that we have still not found a
                // winning get, in which case, we just wait.
                // But if we have found a winning get, then we are
                // ready to complete the rendezvous and return.
                if (winner != null) {
                    // The get is ready to complete.
                    // We are now committed.
                    // Complete the rendezvous.

                    // First, retract the _putWaiting on all receivers
                    // except the one to which we are committing.
                    for (int k = 0; k < receivers.length; k++) {
                        if (receivers[k] != null) {
                            for (int m = 0; m < receivers[k].length; m++) {
                                if (receivers[k][m] != winner) {
                                    CSPReceiver otherCastReceiver = (CSPReceiver)receivers[k][m];
                                    otherCastReceiver._putWaiting = null;
                                    otherCastReceiver._putReceivers = null;
                                }
                            }
                        }
                    }
                    // Mark the put thread unblocked.
                    Thread getThread = winner._getWaiting;
                    Receiver[][] getReceivers = winner._getReceivers;
                    winner._getWaiting = null;
                    winner._getReceivers = null;
                    if (winner._getConditional) {
                        // The far side (get side) is conditional.
                        // Since this put succeeds, we now need to retract
                        // all the _getWaiting flags on far side receivers.
                        for (int k = 0; k < getReceivers.length; k++) {
                            if (getReceivers[k] != null) {
                                for (int m = 0; m < getReceivers[k].length; m++) {
                                    if (getReceivers[k][m] != null) {
                                        CSPReceiver castFarReceiver = (CSPReceiver)getReceivers[k][m];
                                        castFarReceiver._getWaiting = null;
                                        castFarReceiver._getReceivers = null;
                                    }
                                }
                            }
                        }
                        // We also need to mark this receiver as the
                        // winning one to ensure that the getFromAny()
                        // selects the same winner.
                        winner._winsTheGet = true;
                    }
                    // Indicate that a get is in progress.
                    winner._getInProgress = true;
                    // The following does a notify on the director.
                    director.threadUnblocked(getThread, null);
                    
                    // Wait for the get to complete on the selected receiver.
                    while (winner._putWaiting != null) {
                        try {
                            // The following does a notifyAll() on the director.
                            director.threadBlocked(putToAnyThread, null);
                            waitForChange(director);
                        } finally {
                            director.threadUnblocked(putToAnyThread, null);
                        }                                
                    }
                    // Finally, reset the _putInProgress flag.
                    winner._putInProgress = false;
                    winner._winsThePut = false;

                    return;
                }
                // Wait for a change.
                try {
                    // The following does a notifyAll() on the director.
                    director.threadBlocked(putToAnyThread, null);
                    waitForChange(director);
                } finally {
                    director.threadUnblocked(putToAnyThread, null);
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
        synchronized(lock) {
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
        synchronized(lock) {
            _getWaiting = null;
            _putWaiting = null;
            _setConditionalReceive(false, null, -1);
            _setConditionalSend(false, null, -1);
            _boundaryDetector.reset();
            _putInProgress = false;
            _getInProgress = false;
            _winsThePut = false;
            _winsTheGet = false;
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

    /** Return true if the specified receivers are all ready to complete
     *  a rendezvous, assuming they are ready to put.  A receiver is ready if
     *  <ul>
     *  <li> it has a get waiting, and
     *  <li> if it is not conditional, then all its get receivers
     *       are ready to put.
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
    protected static boolean _areReadyToGet(Receiver[][] receivers) {
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (!_checkReadyToGet((CSPReceiver)receivers[i][j])) {
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
                        _commitGet((CSPReceiver)receivers[i][j]);
                    }
                }
            }
        }
        return true;
    }

    /** Return true if the specified receivers are all ready to complete
     *  a rendezvous, assuming they are ready to get.  A receiver is ready if
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
    protected static boolean _areReadyToPut(Receiver[][] receivers) {
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (!_checkReadyToPut((CSPReceiver)receivers[i][j])) {
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
                        _commitPut((CSPReceiver)receivers[i][j]);
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
        return (_getWaiting != null) && _getConditional;
    }

    /** Return whether a putToAny() is pending on this receiver.
     *  @return True if a putToAny() is pending on this receiver.
     */
    protected boolean _isConditionalSendWaiting() {
        return (_putWaiting != null) && _putConditional;
    }
    
    /** Return true if a get() is pending on all the specified
     *  receivers. If the argument is null, then this method
     *  returns false.
     *  @param receivers The receivers, which are assumed to be
     *   instances of CSPReceiver.
     *  @return True if a get() is pending on the specified receivers.
     */
    protected static boolean _isGetWaitingOnAll(Receiver[][] receivers) {
        return _isGetWaitingOnAll(receivers, null);
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
    protected static boolean _isGetWaitingOnAll(Receiver[][] receivers, Receiver except) {
        if (receivers == null) {
            return false;
        }
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != except) {
                        if (((CSPReceiver)receivers[i][j])._getWaiting == null) {
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
                        if (((CSPReceiver)receivers[i][j])._getInProgress) {
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
                        if (((CSPReceiver)receivers[i][j])._getWaiting != null) {
                            return true;
                        }
                    }
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
                        if (((CSPReceiver)receivers[i][j])._putInProgress) {
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
                        if (((CSPReceiver)receivers[i][j])._putWaiting == null) {
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
                        if (((CSPReceiver)receivers[i][j])._putWaiting != null) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Return true if the specified receiver is ready to complete
     *  a rendezvous, assuming it is ready to put.  It is ready if
     *  <ul>
     *  <li> it has a get waiting, and
     *  <li> if it is not conditional, then all its get receivers
     *       are ready to put.
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
    protected static boolean _isReadyToGet(CSPReceiver receiver) {
        boolean result = _checkReadyToGet(receiver);
        if (result) {
            _commitGet(receiver);
        }
        return result;
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
        boolean result = _checkReadyToPut(receiver);
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
        synchronized(director) {
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
        synchronized(director) {
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
     *  <li> if it is not conditional, then all its get receivers
     *       are ready to get, as indicated by the _isReadyToGet() method.
     *  </ul>
     *  This method does not commit. The caller should call _commitPut()
     *  if the rendezvous is ready to complete.
     *  @param receiver The receiver.
     *  @return True if the specified receiver is ready to complete
     *   a rendezvous.
     */
    private static boolean _checkReadyToGet(CSPReceiver receiver) {
        // We assume the specified receiver is ready to put, so
        // we don't need to check the _putReceivers.
        // First, check to see whether a put is in progress, because
        // this would have set _getWaiting to null.
        if (receiver._putInProgress) {
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
        // have to be ready.
        // Check the _getReceivers, but without commiting to
        // this transaction. Skip the argument receiver when checking
        // (or we'll get an infinite loop).
        Receiver[][] receivers = receiver._getReceivers;
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != receiver) {
                        // Check that each receiver is ready to put (we
                        // know it's ready to get because it's in the _getReceivers list).
                        if (!_checkReadyToPut((CSPReceiver)receivers[i][j])) {
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
     *  <li> if it is not conditional, then all its put receivers
     *       are ready to get, as indicated by the _isReadyToGet() method.
     *  </ul>
     *  This method does not commit. The caller should call _commitPut()
     *  if the rendezvous is ready to complete.
     *  @param receiver The receiver.
     *  @return True if the specified receiver is ready to complete
     *   a rendezvous.
     */
    private static boolean _checkReadyToPut(CSPReceiver receiver) {
        // We assume the specified receiver is ready to get, so
        // we don't need to check the _getReceivers.
        // First check to see whether there is a get in progress,
        // because this would have set _putWaiting to null.
        if (receiver._getInProgress) {
            return true;
        }
        // Next check that there is a put waiting. If not, return false.
        if (receiver._putWaiting == null) {
            return false;
        }
        // If the put is conditional, then having a put waiting
        // is sufficient, and we don't have to go any further.
        if (receiver._putConditional) {
            return true;
        }
        // The put is not conditional. All other put receivers
        // have to be ready.
        // Check the _putReceivers, but without commiting to
        // this transaction. Skip the argument receiver when checking
        // (or we'll get an infinite loop).
        Receiver[][] receivers = receiver._putReceivers;
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != receiver) {
                        // Check that each receiver is ready to get (we know
                        // it's ready to put because it's in the _putReceivers list).
                        if (!_checkReadyToGet((CSPReceiver)receivers[i][j])) {
                            return false;
                        }
                    }
                }
            }
        }
        // If we haven't returned false by now, then we are ready to commit.
        return true;
    }

    /** Commit the specified receiver to a get.  If its get is
     *  conditional, then this will retract the get waiting flag
     *  on all other get receivers, and set the _getInProgress flag.
     *  @param receiver The receiver.
     */
    private static void _commitGet(CSPReceiver receiver) {
        if (receiver._getWaiting == null) {
            // Nothing to do. This should not happen.
            return;
        }
        receiver._winsTheGet = true;
        Receiver[][] receivers = receiver._getReceivers;
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != receiver) {
                        CSPReceiver castReceiver = (CSPReceiver)receivers[i][j];
                        // If the get is conditional, then retract get waiting
                        // on all other get receivers.
                        if (receiver._getConditional) {
                            castReceiver._getWaiting = null;
                            castReceiver._getReceivers = null;
                        } else {
                            // If the get is not conditional, then propagate
                            // the commit to the other getReceivers.
                            _commitPut(castReceiver);
                        }
                    }
                }
            }
        }
    }

    /** Commit the specified receiver to a put.  If its put is
     *  conditional, then this will retract the put waiting flag
     *  on all other put receivers and set the _winsThePut flag.
     *  @param receiver The receiver.
     */
    private static void _commitPut(CSPReceiver receiver) {
        if (receiver._putWaiting == null) {
            // Nothing to do. This should not happen.
            return;
        }
        receiver._winsThePut = true;
        Receiver[][] receivers = receiver._putReceivers;
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != receiver) {
                        CSPReceiver castReceiver = (CSPReceiver)receivers[i][j];
                        // If the get is conditional, then retract get waiting
                        // on all other get receivers.
                        if (receiver._getConditional) {
                            castReceiver._getWaiting = null;
                            castReceiver._getReceivers = null;
                        } else {
                            // If the get is not conditional, then propagate
                            // the commit to the other getReceivers.
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
    
    /** Indicator that is true in a receiver with a get() in progress. */
    private boolean _getInProgress = false;
    
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

    /** Indicator that is true in a receiver with a put in progress. */
    private boolean _putInProgress = false;
    
    /** The receivers currently being put data to. */
    private Receiver[][] _putReceivers = null;

    /** Indicator that a put() is waiting on this receiver. */
    private Thread _putWaiting = null;
    
    /** Array with just one receiver, this one, for convenience. */
    private Receiver[][] _thisReceiver = new Receiver[1][1];
    
    /** The token being transferred during the rendezvous. */
    private Token _token;
    
    /** Flag indicating that this receiver wins a conditional get competition. */
    private boolean _winsTheGet = false;

    /** Flag indicating that this receiver wins a conditional put competition. */
    private boolean _winsThePut = false;
}
