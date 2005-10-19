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

import java.util.HashSet;
import java.util.Hashtable;
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
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 */
public class RendezvousReceiver extends AbstractReceiver implements ProcessReceiver {
    
    // FIXME: Downgraded to Red when changing deadlock detection mechanism.
    // EAL 8/05
    
    /** Construct a RendezvousReceiver with no container.
     */
    public RendezvousReceiver() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
        _thisReceiver[0][0] = this;
    }

    /** Construct a RendezvousReceiver with the specified container.
     *  @param container The port containing this receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public RendezvousReceiver(IOPort container) throws IllegalActionException {
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
     *   all be instances of RendezvousReceiver.
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
        boolean needWait = false;
        Hashtable result;
        synchronized(director) {
            Thread theThread = Thread.currentThread();
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            RendezvousReceiver castReceiver = (RendezvousReceiver)receivers[i][j];
                            castReceiver._getWaiting = theThread;
                            castReceiver._getReceivers = receivers;
                            castReceiver._getConditional = false;
                        }
                    }
                }
            }
            
            Receiver[] transactionReceivers = _testTransaction(receivers, false);
            if (transactionReceivers == null) {
                director.threadBlocked(theThread, null);
                while (!_releasedThreads.containsKey(theThread)) {
                    waitForChange(director);
                }
            } else {
                _commitTransaction(transactionReceivers, director);
            }
            
            result = (Hashtable)_releasedThreads.remove(theThread);
        }
        Token[][] tokens = new Token[receivers.length][];
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                tokens[i] = new Token[receivers[i].length];
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        tokens[i][j] = (Token)result.get(receivers[i][j]);
                    }
                }
            }
        }
        return tokens;
    }
    
    /** Get from any receiver in the specified array.
     *  This method does not return until one of the gets is complete.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of RendezvousReceiver.
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
        Hashtable result;
        synchronized(director) {
            Thread theThread = Thread.currentThread();
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            RendezvousReceiver castReceiver = (RendezvousReceiver)receivers[i][j];
                            castReceiver._getWaiting = theThread;
                            castReceiver._getReceivers = receivers;
                            castReceiver._getConditional = true;
                        }
                    }
                }
            }
            
            Receiver[] transactionReceivers = _testTransaction(receivers, false);
            if (transactionReceivers == null) {
                director.threadBlocked(theThread, null);
                while (!_releasedThreads.containsKey(theThread)) {
                    waitForChange(director);
                }
            } else {
                _commitTransaction(transactionReceivers, director);
            }
            
            result = (Hashtable)_releasedThreads.remove(theThread);
        }
        Token token = null;
        for (int i = 0; i < receivers.length && token == null; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length && token == null; j++) {
                    if (receivers[i][j] != null) {
                        if (result.containsKey(receivers[i][j])) {
                            token = (Token)result.get(receivers[i][j]);
                        }
                    }
                }
            }
        }
        return token;
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
            return _getWaiting != null;
        }
    }

    /** Return true if there is either a put or a conditional send
     *  waiting on this receiver.
     *  @return A boolean indicating whether a write is pending on this
     *   receiver.
     */
    public boolean isWriteBlocked() {
        synchronized(_getDirector()) {
            return _putWaiting != null;
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
        putToAll(new Token[][]{{token}}, _thisReceiver, _getDirector());
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
    
    public void putToAll(Token token, Receiver[] receivers)
            throws NoRoomException, IllegalActionException {
        putToAll(token, receivers, _getDirector());
    }
    
    /** Put to all receivers in the specified array.
     *  This method does not return until all the puts are complete.
     *  @param token The token to put.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of RendezvousReceiver.
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
        putToAll(new Token[][]{{token}}, new Receiver[][]{receivers}, director);
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
     *   all be instances of RendezvousReceiver.
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
            Thread theThread = Thread.currentThread();
            Token token = null;
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            if (tokens.length > i && tokens[i] != null && tokens[i].length > j) {
                                token = tokens[i][j];
                            }
                            RendezvousReceiver castReceiver = (RendezvousReceiver)receivers[i][j];
                            castReceiver._putWaiting = theThread;
                            castReceiver._putReceivers = receivers;
                            castReceiver._putConditional = false;
                            
                            IOPort port = castReceiver.getContainer();
                            castReceiver._token = port.convert(token);
                        }
                    }
                }
            }
            
            Receiver[] transactionReceivers = _testTransaction(receivers, true);
            if (transactionReceivers == null) {
                director.threadBlocked(theThread, null);
                while (!_releasedThreads.containsKey(theThread)) {
                    waitForChange(director);
                }
            } else {
                _commitTransaction(transactionReceivers, director);
            }
            
            _releasedThreads.remove(theThread);
        }
    }
    
    /** Put the specified token to any receiver in the specified array.
     *  This method does not return until one of the puts is complete.
     *  @param token The token to put.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of RendezvousReceiver.
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
            Thread theThread = Thread.currentThread();
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        if (receivers[i][j] != null) {
                            RendezvousReceiver castReceiver = (RendezvousReceiver)receivers[i][j];
                            castReceiver._putWaiting = theThread;
                            castReceiver._putReceivers = receivers;
                            castReceiver._putConditional = true;
                            
                            IOPort port = castReceiver.getContainer();
                            castReceiver._token = port.convert(token);
                        }
                    }
                }
            }
            
            Receiver[] transactionReceivers = _testTransaction(receivers, true);
            if (transactionReceivers == null) {
                director.threadBlocked(theThread, null);
                while (!_releasedThreads.containsKey(theThread)) {
                    waitForChange(director);
                }
            } else {
                _commitTransaction(transactionReceivers, director);
            }
            
            _releasedThreads.remove(theThread);
        }
    }

    /** The model has finished executing, so set a flag so that the
     *  next time an actor tries to get or put it gets a
     *  TerminateProcessException which will cause it to finish.
     */
    public void requestFinish() {
        Object lock = _getDirector();
        synchronized(lock) {
            reset();
            lock.notifyAll();
        }
    }

    /** Reset local flags.
     */
    public void reset() {
        synchronized(_getDirector()) {
            _resetFlags(true, true);
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
            throw new TerminateProcessException("RendezvousReceiver: trying to "
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
    
    /** Return whether a get() is waiting to rendezvous
     *  at this receiver.
     *  @return True if a get() is waiting to rendezvous.
     */
    protected boolean _isGetWaiting() {
        return _getWaiting != null;
    }

    /** Flag indicating whether or not a put() is waiting to rendezvous
     *  at this receiver.
     *  @return True if a put() is waiting to rendezvous.
     */
    protected boolean _isPutWaiting() {
        return _putWaiting != null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    /** Commit the transaction formed by the set of receivers that agree to
     *  send and receive at the same time.
     * 
     *  @param receivers The receivers that participate in the transaction.
     *  @param director The director.
     */
    private static void _commitTransaction(Receiver[] receivers,
            CSPDirector director) {
        // The result table for all the receivers.
        Hashtable result = new Hashtable();
        // Backup result tokens for the receivers, and release the threads
        // blocked at those receivers.
        for (int i = 0; i < receivers.length; i++) {
            RendezvousReceiver castReceiver = (RendezvousReceiver)receivers[i];
            result.put(castReceiver, castReceiver._token);
            if (!_releasedThreads.containsKey(castReceiver._getWaiting)) {
                director.threadUnblocked(castReceiver._getWaiting, null);
                _releasedThreads.put(castReceiver._getWaiting, result);
            }
            if (!_releasedThreads.containsKey(castReceiver._putWaiting)) {
                director.threadUnblocked(castReceiver._putWaiting, null);
                _releasedThreads.put(castReceiver._putWaiting, result);
            }
        }
        // Reset the flags for all the receivers.
        for (int i = 0; i < receivers.length; i++) {
            RendezvousReceiver castReceiver = (RendezvousReceiver)receivers[i];
            // If the receiver does conditional get, clear the get request on
            // all the channels.
            if (castReceiver._getReceivers != null &&
                    castReceiver._getConditional) {
                _resetReceiversFlags(castReceiver._getReceivers, true, false);
            }
            // If the receiver does conditional put, clear the put request on
            // all the channels.
            if (castReceiver._putReceivers != null &&
                    castReceiver._putConditional) {
                _resetReceiversFlags(castReceiver._putReceivers, false, true);
            }
            // Clear the get and put requests on this receiver.
            castReceiver._resetFlags(true, true);
        }
    }
    
    /** Reset the flags of this receiver.
     * 
     *  @param clearGet Whether to reset the flags related to the get methods.
     *  @param clearPut Whether to reset the flags related to the put methods.
     */
    private void _resetFlags(boolean clearGet, boolean clearPut) {
        if (clearGet) {
            _getReceivers = null;
            _getWaiting = null;
        }
        if (clearPut) {
            _putReceivers = null;
            _putWaiting = null;
        }
    }
    
    /** Reset the flags of the receivers in the given array.
     * 
     *  @param receivers An array of receivers to be reset.
     *  @param clearGet Whether to reset the flags related to the get methods.
     *  @param clearPut Whether to reset the flags related to the put methods.
     */
    private static void _resetReceiversFlags(Receiver[][] receivers,
            boolean clearGet, boolean clearPut) {
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        ((RendezvousReceiver)receivers[i][j])._resetFlags(clearGet,
                                clearPut);
                    }
                }
            }
        }
    }
    
    /** Test whether a transaction can be formed according to the CSP
     *  semantics.
     * 
     *  @param receivers The array of receivers to be put to or get from.
     *  @param isPut If true, the transaction is to put tokens to the
     *         receivers; if false, the transaction is to get tokens from
     *         the receivers.
     *  @return An array of receivers that participate in the transaction, if
     *         it can be formed, or null if no transaction can be formed.
     */
    private static Receiver[] _testTransaction(Receiver[][] receivers,
            boolean isPut) {
        Set result = _testTransactionRecursive(receivers, isPut);
        if (result == null) {
            return null;
        } else {
            // Convert the result into an array.
            return (Receiver[])result.toArray(new Receiver[result.size()]);
        }
    }
    
    /** Test whether a transaction can be formed according to the CSP
     *  semantics.
     * 
     *  @param receivers The array of receivers to be put to or get from.
     *  @param isPut If true, the transaction is to put tokens to the
     *         receivers; if false, the transaction is to get tokens from
     *         the receivers.
     *  @return A set of receivers that participate in the transaction, if
     *         it can be formed, or null if no transaction can be formed.
     */
    private static Set _testTransactionRecursive(Receiver[][] receivers,
            boolean isPut) {
        Set readyReceivers = new HashSet();
        boolean isConditional = false;
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                   if (receivers[i][j] != null) {
                       RendezvousReceiver castReceiver = (RendezvousReceiver)receivers[i][j];
                       // Whether the receiver is in a conditional branch.
                       // isConditional for all the receivers should be the
                       // same in one invocation of this method.
                       isConditional =
                           (isPut && castReceiver._putConditional) ||
                           (!isPut && castReceiver._getConditional);
                       if (castReceiver._isVisited) {
                           // If the receiver is visited in a previous traversal
                           // step, a loop is found. In this case, simply assume
                           // that it is OK with the transaction.
                           readyReceivers.add(castReceiver);
                       } else {
                           // If the receiver is not visited yet, first test
                           // whether itself is OK with the transaction. A
                           // receiver that agrees to take a transaction always
                           // has 2 threads waiting on it: one waiting to get;
                           // the other waiting to put.
                           if (castReceiver._putWaiting == null ||
                                   castReceiver._getWaiting == null) {
                               // This conditional branch does not work because
                               // at least one receiver is not ready.
                               readyReceivers.clear();
                               break;
                           } else {
                               // Get the far-side receivers of the current
                               // receiver, and visit them in a depth-first
                               // manner.
                               Receiver[][] farSideReceivers =
                                   isPut ? castReceiver._getReceivers :
                                       castReceiver._putReceivers;
                               castReceiver._isVisited = true;
                               Set nestedReadyReceivers =
                                   _testTransactionRecursive(farSideReceivers,
                                           !isPut);
                               castReceiver._isVisited = false;

                               if (nestedReadyReceivers == null) {
                                   // If the traversal in this branch fails,
                                   // it is not ready for a transaction.
                                   readyReceivers.clear();
                                   break;
                               } else {
                                   // Otherwise, add the current receiver and
                                   // the receivers visited in the sub-tree to
                                   // the set of ready receivers.
                                   readyReceivers.add(castReceiver);
                                   readyReceivers.addAll(nestedReadyReceivers);
                               }
                           }
                       }
                   }
                }
                if (isConditional && readyReceivers.size() > 0 ||
                        !isConditional && readyReceivers.size() == 0) {
                    // If either condition is true, the transaction cannot be
                    // formed, so just return.
                    break;
                }
            }
        }
        
        return readyReceivers.size() > 0 ? readyReceivers : null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                          private fields                   ////

    /** The boundary detector. */
    private BoundaryDetector _boundaryDetector;

    /** Flag indicating that the _getWaiting thread is a conditional rendezvous. */
    private boolean _getConditional = false;
    
    /** The receivers currently being gotten data from. */
    private Receiver[][] _getReceivers = null;

    /** Indicator that a get() is waiting on this receiver. */
    private Thread _getWaiting = null;

    /** Whether this receiver has been visited in the _testTransaction method. */
    private boolean _isVisited = false;

    /** Flag indicating that the _putWaiting thread is a conditional rendezvous. */
    private boolean _putConditional = false;

    /** The receivers currently being put data to. */
    private Receiver[][] _putReceivers = null;

    /** Indicator that a put() is waiting on this receiver. */
    private Thread _putWaiting = null;
    
    /** The threads to be released from their blocking state, and the results
     *  associated with them. The keys are threads; the values are Hashtables.
     *  Each Hashtable in the values maps receivers to the tokens that they
     *  contain.
     */
    private static Hashtable _releasedThreads = new Hashtable();
    
    /** Array with just one receiver, this one, for convenience. */
    private Receiver[][] _thisReceiver = new Receiver[1][1];
    
    /** The token being transferred during the rendezvous. */
    private Token _token;
}