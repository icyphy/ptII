/* Receiver for rendezvous style communication.

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

//////////////////////////////////////////////////////////////////////////
//// RendezvousReceiver

/**
 Receiver for rendezvous style communication. In the rendezvous
 domain, all communication is via
 synchronous message passing, so both the the sending and receiving
 processes need to rendezvous at the receiver. For rendezvous, the
 receiver is the key synchronization point. It is assumed each receiver
 has at most one thread trying to send to it and at most one thread
 trying to receive from it at any one time. The receiver performs the
 synchronization necessary for simple rendezvous (get() and put()
 operations). This receiver is based on the CSPReceiver class by
 John S. Davis II, Thomas Feng, Edward A. Lee, Neil Smyth, and Yang Zhao. 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Green (acataldo)
 @Pt.AcceptedRating Green (acataldo)
 */
public class RendezvousReceiver extends AbstractReceiver implements
        ProcessReceiver {
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
     *  all be instances of RendezvousReceiver.
     *  @param director The director, on which this method synchronizes.
     *  @return An array of token arrays, where the structure of the
     *  array is the same as the structure of the specified array of
     *  receivers. Note that if the receivers argument has any null values in
     *  the array of arrays, then so will the returned array or
     *  arrays.
     *  @exception TerminateProcessException If the actor to
     *  which this receiver belongs is to be terminated.
     */
    public static Token[][] getFromAll(Receiver[][] receivers,
            RendezvousDirector director) throws TerminateProcessException {
        Map result = null;
        try {
            result = _getOrPutTokens(receivers, null, director, null,
                    GET_FROM_ALL);
        } catch (IllegalActionException iae) {
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

    /** Get from any receiver in the specified array.
     *  This method does not return until one of the gets is complete.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of RendezvousReceiver.
     *  @param director The director, on which this method synchronizes.
     *  @return A token from one of the receivers.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs is to be terminated.
     */
    public static Token getFromAny(Receiver[][] receivers,
            RendezvousDirector director) throws TerminateProcessException {
        Map result = null;
        try {
            result = _getOrPutTokens(receivers, null, director, null,
                    GET_FROM_ANY);
        } catch (IllegalActionException iae) {
        }

        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        if (result.containsKey(receivers[i][j])) {
                            return (Token)result.get(receivers[i][j]);
                        }
                    }
                }
            }
        }

        throw new InternalErrorException("No token is received.");
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
    public static void getFromAnyPutToAll(Receiver[][] getReceivers,
            Receiver[][] putReceivers, RendezvousDirector director)
            throws IllegalActionException, TerminateProcessException {
        _getOrPutTokens(getReceivers, putReceivers, director, null,
                GET_FROM_ANY_PUT_TO_ALL);
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
            return _getWaiting != null;
        }
    }

    /** Return true if there is either a put or a conditional send
     *  waiting on this receiver.
     *  @return A boolean indicating whether a write is pending on this
     *   receiver.
     */
    public boolean isWriteBlocked() {
        synchronized (_getDirector()) {
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
    public void put(Token token) throws IllegalActionException,
            TerminateProcessException {
        putToAll(new Token[][] { { token } }, _thisReceiver, _getDirector());
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
     *  Implementers will assume that all such receivers
     *  are of the same class.
     *  @param token The token to put.
     *  @param receivers The receivers.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putToAll(Token token, Receiver[] receivers)
            throws NoRoomException, IllegalActionException {
        putToAll(token, receivers, _getDirector());
    }

    /** Put to all receivers in the specified array.
     *  This method does not return until all the puts are complete.
     *  This method differs from its counterpart in the superclass in that it
     *  puts the token to all receivers in an atomic step. The method in the
     *  superclass puts the token to one receiver in the receiver array at a
     *  time.
     *  @param token The token to put.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of RendezvousReceiver.
     *  @param director The director, on which this method synchronizes.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs is to be terminated.
     */
    public void putToAll(Token token, Receiver[] receivers,
            RendezvousDirector director) throws IllegalActionException,
            TerminateProcessException {
        if ((receivers == null) || (receivers.length == 0)) {
            return;
        }

        putToAll(new Token[][] { { token } }, new Receiver[][] { receivers },
                director);
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
    public static void putToAll(Token[][] tokens, Receiver[][] receivers,
            RendezvousDirector director) throws IllegalActionException,
            TerminateProcessException {
        _getOrPutTokens(null, receivers, director, tokens, PUT_TO_ALL);
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
    public static void putToAny(Token token, Receiver[][] receivers,
            RendezvousDirector director) throws IllegalActionException,
            TerminateProcessException {
        _getOrPutTokens(null, receivers, director, token, PUT_TO_ANY);
    }

    /** The model has finished executing, so set a flag so that the
     *  next time an actor tries to get or put it gets a
     *  TerminateProcessException which will cause it to finish.
     */
    public void requestFinish() {
        Object lock = _getDirector();

        synchronized (lock) {
            reset();
            lock.notifyAll();
        }
    }

    /** Reset local flags.
     */
    public void reset() {
        synchronized (_getDirector()) {
            _resetFlags(true, true);
        }
    }

    /** Wait on the specified director.  This is not synchronized on
     *  the specified director, so the called should be.
     *  @param director The director on which to wait.
     *  @exception TerminateProcessException If a finish has been
     *  requested of the specified director, or if the calling thread
     *  is interrupted while waiting.
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

    /** Commit the rendezvous formed by the set of receivers that agree to
     *  send and receive at the same time.
     *
     *  @param receivers The receivers that participate in the rendezvous.
     *  @param director The director.
     *  @see #_receiversReadyToCommit(Receiver[][], boolean)
     */
    protected static void _commitRendezvous(Set receivers,
            RendezvousDirector director) {
        // The result table for all the receivers.
        Map result = new HashMap();
        
        // Backup result tokens for the receivers, and release the threads
        // blocked at those receivers.
        TopologicalSort sort = new TopologicalSort(receivers);
        while (sort.hasNext()) {
            RendezvousReceiver castReceiver =
                (RendezvousReceiver) sort.next();
            result.put(castReceiver, castReceiver._token);

            if (_getData(castReceiver._getWaiting) == null) {
                director.threadUnblocked(castReceiver._getWaiting, null);
                _setData(castReceiver._getWaiting, result);
            }

            if (_getData(castReceiver._putWaiting) == null) {
                director.threadUnblocked(castReceiver._putWaiting, null);
                _setData(castReceiver._putWaiting, result);
            }
        }

        // Reset the flags for all the receivers.
        Iterator receiverIterator = receivers.iterator();
        while (receiverIterator.hasNext()) {
            RendezvousReceiver castReceiver =
                (RendezvousReceiver) receiverIterator.next();

            // If the receiver does conditional get, clear the get request on
            // all the channels.
            if ((castReceiver._getReceivers != null)
                    && castReceiver._getConditional) {
                _resetReceiversFlags(castReceiver._getReceivers, true, false);
            }

            // If the receiver does conditional put, clear the put request on
            // all the channels.
            if ((castReceiver._putReceivers != null)
                    && castReceiver._putConditional) {
                _resetReceiversFlags(castReceiver._putReceivers, false, true);
            }

            // Clear the get and put requests on this receiver.
            castReceiver._resetFlags(true, true);
        }
    }
    
    /** Return the director that is controlling the execution of this model.
     *  If this receiver is an inside receiver, then it is the director
     *  of the container (actor) of the container (port). Otherwise, it
     *  is the executive director of the container (actor) of the container
     *  (port).
     *  @return The RendezvousDirector controlling this model.
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
    
    /** Get the receivers that are ready to form a rendezvous according to the
     *  rendezvous semantics. If no rendezvous can be formed starting for the
     *  given array of receivers, null is returned.
     *
     *  @param receivers The array of receivers to be put to or get from.
     *  @param isPut If true, the rendezvous is to put tokens to the
     *         receivers; if false, the rendezvous is to get tokens from
     *         the receivers.
     *  @return A set of receivers that participate in the rendezvous if
     *         it can be formed, or null if no rendezvous can be formed.
     *  @see #_commitRendezvous(Set, RendezvousDirector)
     */
    protected static Set _receiversReadyToCommit(Receiver[][] receivers,
            boolean isPut) {
        Set ready = new HashSet();
        if (_checkRendezvous(receivers, isPut, new HashSet(), ready,
                new HashSet(), false, false)) {
            return ready;
        } else {
            return null;
        }
    }
    
    /** Return a string describing the status of the receiver.
     *  @return A string describing the status of the specified receiver.
     */
    protected String _status() {
        // TODO: Should be implemented when used.
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    private static boolean _checkRendezvous(Receiver[][] receivers,
            boolean isPut, Set beingChecked, Set ready, Set notReady,
            boolean isSymmetricGet, boolean isSymmetricPut) {
        
        boolean isConditional = false;
        boolean branchReady = false;
        
        int selectedBranch = _getSelectedBranch(receivers, beingChecked);
        if (selectedBranch >= 0 && (isSymmetricGet || isSymmetricPut)) {
            return false;
        }
        
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                boolean hasBackup = false;
                Thread backupThread = null;
                branchReady = true;
                
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        RendezvousReceiver receiver =
                            (RendezvousReceiver)receivers[i][j];
                        
                        // Whether the receiver is in a conditional branch.
                        // isConditional for all the receivers should be the
                        // same in one invocation of this method.
                        isConditional = isConditional ||
                                (isPut && receiver._putConditional) ||
                                (!isPut && receiver._getConditional);
                        
                        if (isConditional && (selectedBranch != -1 &&
                                selectedBranch != i)) {
                            branchReady = false;
                            break;
                        }

                        if (beingChecked.contains(receiver) ||
                                ready.contains(receiver)) {
                            // Do nothing
                        } else if (notReady.contains(receiver)) {
                            branchReady = false;
                            break;
                        } else if ((receiver._putWaiting == null) ||
                                (receiver._getWaiting == null)) {
                            branchReady = false;
                            notReady.add(receiver);
                            break;
                        } else {
                            beingChecked.add(receiver);
                            
                            Receiver[][] farSideReceivers =
                                isPut ? receiver._getReceivers
                                    : receiver._putReceivers;
                            
                            Receiver[][][] dependencies =
                                new Receiver[][][] {
                                    farSideReceivers,
                                    isSymmetricPut ? null :
                                        receiver._symmetricGetReceivers,
                                    isSymmetricGet ? null :
                                        receiver._symmetricPutReceivers
                            };
                            for (int k = 0; k < dependencies.length; k++) {
                                if (dependencies[k] != null) {
                                    if (!_checkRendezvous(dependencies[k],
                                            (k == 0 && !isPut) || k == 2,
                                            beingChecked, ready, notReady,
                                            k == 1, k == 2)) {
                                        branchReady = false;
                                        break;
                                    }
                                }
                            }

                            beingChecked.remove(receiver);
                            if (branchReady) {
                                ready.add(receiver);
                            } else {
                                notReady.add(receiver);
                                break;
                            }
                        }
                    }
                }
                
                if ((isConditional && branchReady)
                        || (!isConditional && !branchReady)) {
                    break;
                }
            }
        }
        return branchReady;
    }

    /** Get the data associated with the thread.
     *  
     *  @param thread The thread with data associated with it.
     *  @return The data associated with the thread, or null if no data is
     *   associated with it.
     *  @see #_setData(Thread, Object)
     */
    private static Object _getData(Thread thread) {
        synchronized (thread) {
            ClassLoader oldLoader = thread.getContextClassLoader();
            if (oldLoader != null && oldLoader instanceof ClassLoaderWrapper) {
                return ((ClassLoaderWrapper)oldLoader).getData();
            } else {
                return null;
            }
        }
    }
    
    /** Get or put token(s) to the array of receivers. This method is commonly
     *  used by {@link #getFromAll(Receiver[][], RendezvousDirector)},
     *  {@link #getFromAny(Receiver[][], RendezvousDirector)},
     *  {@link #putToAll(Token[][], Receiver[][], RendezvousDirector)} and
     *  {@link #putToAny(Token, Receiver[][], RendezvousDirector)}. If the
     *  tokens parameter is null, it does a get. isCondition indicates whether
     *  the get is from all or from any. If the tokens parameter is a single
     *  token (of type {@link Token}), it does put to any with that token. If
     *  the tokens parameter is a two-dimentional array of tokens, it does put
     *  to all with those tokens.
     *  
     *  This method does not return until the get or put is finished.
     *  
     *  @param receivers The receivers to be got from or put to.
     *  @param director The director.
     *  @param tokens null if this method is used to get tokens; a token of
     *         this method is used to put to any of the receivers; or a
     *         two-dimentional array of tokens if this method is used to put to
     *         all of the receivers.
     *  @param isConditional Whether the get or put is conditional.
     *  @return The map of results on the receivers that participate in the
     *          rendezvous. Keys of the map are receivers; values of the map
     *          are the tokens on those receivers.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type). This can happen only if the
     *   operation is put to all or put to any.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs is to be terminated.
     */
    private static Map _getOrPutTokens(Receiver[][] getReceivers,
            Receiver[][] putReceivers, RendezvousDirector director,
            Object tokens, int flag)
            throws IllegalActionException, TerminateProcessException {
        boolean isGet = (flag & GET) == GET;
        boolean isPut = (flag & PUT) == PUT;
        boolean isGetConditional =
            (flag & GET_CONDITIONAL) == GET_CONDITIONAL;
        boolean isPutConditional =
            (flag & PUT_CONDITIONAL) == PUT_CONDITIONAL;
        boolean isSymmetric = isPut && isGet;

        Map result = null;
        synchronized (director) {
            Thread theThread = Thread.currentThread();
            
            // The token of the put to any operation, or null.
            Token token =
                tokens instanceof Token ? (Token)tokens : null;
            // The token array of the put to all operation, or null.
            Token[][] tokenArray =
                tokens instanceof Token[][] ? (Token[][])tokens : null;

            if (isGet) {
                for (int i = 0; i < getReceivers.length; i++) {
                    if (getReceivers[i] != null) {
                        for (int j = 0; j < getReceivers[i].length; j++) {
                            RendezvousReceiver receiver =
                                (RendezvousReceiver) getReceivers[i][j];
                            
                            if (receiver != null) {
                                receiver._getWaiting = theThread;
                                receiver._getReceivers = getReceivers;
                                receiver._getConditional =
                                    isGetConditional;
                                
                                if (isSymmetric) {
                                    receiver._symmetricPutReceivers =
                                        putReceivers;
                                }
                            }
                        }
                    }
                }
            }
            
            if (isPut) {
                for (int i = 0; i < putReceivers.length; i++) {
                    if (putReceivers[i] != null) {
                        for (int j = 0; j < putReceivers[i].length; j++) {
                            RendezvousReceiver receiver =
                                (RendezvousReceiver) putReceivers[i][j];

                            if (receiver != null) {
                                receiver._putWaiting = theThread;
                                receiver._putReceivers = putReceivers;
                                
                                IOPort port = receiver.getContainer();
                                if (isPutConditional) {
                                    receiver._putConditional = true;
                                    receiver._token =
                                        token == null ? null :
                                            port.convert(token);
                                } else {
                                    receiver._putConditional = false;
                                    try {
                                        token = tokenArray[i][j];
                                    } catch (Throwable e) {
                                    }
                                    receiver._token =
                                        token == null ? null :
                                            port.convert(token);
                                }
                                
                                if (isSymmetric) {
                                    receiver._symmetricGetReceivers =
                                        getReceivers;
                                }
                            }
                        }
                    }
                }
            }

            Set rendezvousReceivers;
            if (getReceivers != null) {
                rendezvousReceivers =
                    _receiversReadyToCommit(getReceivers, false);
            } else {
                rendezvousReceivers =
                    _receiversReadyToCommit(putReceivers, true);
            }
            
            if (rendezvousReceivers == null) {
                // A rendezvous cannot be formed at this time.
                // This thread just waits until another thread commits a
                // rendezvous with the given receivers.
                director.threadBlocked(theThread, null);
                while (result == null) {
                    waitForChange(director);
                    result = (Map) _getData(theThread);
                }
                _setData(theThread, null);
            } else {
                // A rendezvous is formed, so commit the rendezvous, and wake
                // up the other threads in this rendezvous.
                _commitRendezvous(rendezvousReceivers, director);
                result = (Map) _setData(theThread, null);
            }
        }
        return result;
    }
    
    private static int _getSelectedBranch(Receiver[][] receivers, Set beingChecked) {
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i] != null) {
                for (int j = 0; j < receivers[i].length; j++) {
                    Receiver receiver = (Receiver) receivers[i][j];
                    if (beingChecked.contains(receiver)) {
                        return i;
                    }
                }
            }
        }
        return -1;
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
            _symmetricPutReceivers = null;
        }

        if (clearPut) {
            _putReceivers = null;
            _putWaiting = null;
            _symmetricGetReceivers = null;
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
                        ((RendezvousReceiver) receivers[i][j])._resetFlags(
                                clearGet, clearPut);
                    }
                }
            }
        }
    }
    
    /** Set the data associated with the thread.
     *  
     *  @param thread The thread with which the data will be associated.
     *  @param data The data to be associated with the thread, or null if no
     *   data is to be associated with it.
     *  @see #_getData(Thread)
     */
    private static Object _setData(Thread thread, Object data) {
        synchronized (thread) {
            ClassLoader oldLoader = thread.getContextClassLoader();
            if (oldLoader != null && oldLoader instanceof ClassLoaderWrapper) {
                ClassLoaderWrapper castOldLoader =
                    (ClassLoaderWrapper)oldLoader;
                Object oldData = castOldLoader.getData();
                castOldLoader.setData(data);
                return oldData;
            } else {
                if (data != null) {
                    ClassLoaderWrapper newLoader =
                        new ClassLoaderWrapper(oldLoader);
                    newLoader.setData(data);
                    thread.setContextClassLoader(newLoader);
                }
                return null;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private fields                   ////

    /** The boundary detector. */
    private BoundaryDetector _boundaryDetector;

    /** Flag indicating that the _getWaiting thread is a conditional
     * rendezvous.
     */
    private boolean _getConditional = false;

    /** The receivers currently being gotten data from. */
    private Receiver[][] _getReceivers = null;

    /** Indicator that a get() is waiting on this receiver. */
    private Thread _getWaiting = null;

    /** Whether this receiver has been visited in the __receiversReadyToCommit
     *  method.
     */
    private boolean _isVisited = false;

    /** Flag indicating that the _putWaiting thread is a conditional
     * rendezvous.
     */
    private boolean _putConditional = false;

    /** The receivers currently being put data to. */
    private Receiver[][] _putReceivers = null;

    /** Indicator that a put() is waiting on this receiver. */
    private Thread _putWaiting = null;
    
    private Receiver[][] _symmetricGetReceivers;
    
    private Receiver[][] _symmetricPutReceivers;

    /** Array with just one receiver, this one, for convenience. */
    private Receiver[][] _thisReceiver = new Receiver[1][1];

    /** The token being transferred during the rendezvous. */
    private Token _token;
    
    private static final int GET                     =  4; // 0100
    private static final int PUT                     =  8; // 1000
    private static final int GET_CONDITIONAL         =  1; // 0001
    private static final int PUT_CONDITIONAL         =  2; // 0010
    
    private static final int GET_FROM_ALL            =  4; // 0100
    private static final int GET_FROM_ANY            =  5; // 0101
    private static final int PUT_TO_ALL              =  8; // 1000
    private static final int PUT_TO_ANY              = 10; // 1010
    private static final int GET_FROM_ANY_PUT_TO_ALL = 13; // 1101
    
    static class TopologicalSort {
        
        public boolean hasNext() {
            return !_zeroInDegree.isEmpty();
        }
        
        public Receiver next() {
            Iterator zeroInDegreeIterator = _zeroInDegree.iterator();
            if (!zeroInDegreeIterator.hasNext()) {
                return null;
            }
            
            RendezvousReceiver next =
                (RendezvousReceiver)zeroInDegreeIterator.next();
            zeroInDegreeIterator.remove();
            
            if (next._symmetricPutReceivers != null) {
                Token token = next._token;
                Receiver[][] putReceivers = next._symmetricPutReceivers;

                // Delete the out-edges.
                if (next._getConditional) {
                    for (int i = 0; i < putReceivers.length; i++) {
                        if (putReceivers[i] != null) {
                            for (int j = 0; j < putReceivers[i].length; j++) {
                                if (putReceivers[i][j] != null &&
                                        _receivers.contains(putReceivers[i][j])) {
                                    
                                    RendezvousReceiver castReceiver =
                                        (RendezvousReceiver)putReceivers[i][j];
                                    
                                    castReceiver._token = token;
                                    _zeroInDegree.add(putReceivers[i][j]);
                                }
                            }
                        }
                    }
                } else {
                    // TODO: put the token to only one channel.
                    throw new InternalErrorException("Function not " +
                            "implemented (possibly for Barrier and other " +
                            "primitive actors).");
                }
            }
            return next;
        }
        
        protected TopologicalSort(Set receivers) {
            _receivers = receivers;
            _initialize();
        }
        
        private void _initialize() {
            _zeroInDegree = new HashSet();
            Iterator iterator = _receivers.iterator();
            while (iterator.hasNext()) {
                RendezvousReceiver receiver =
                    (RendezvousReceiver)iterator.next();
                if (receiver._symmetricGetReceivers == null) {
                    _zeroInDegree.add(receiver);
                }
            }
            if (_zeroInDegree.isEmpty()) {
                throw new InternalErrorException("No entry point.");
            }
        }
        
        private Set _receivers;
        
        private Set _zeroInDegree;
    }
}