/* Receiver for CSP style communication.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.List;

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
 @Pt.AcceptedRating Green (kienhuis)
 */
public class CSPReceiver extends AbstractReceiver implements ProcessReceiver {

    // FIXME: Downgraded to Red when changing deadlock detection mechanism.
    // EAL 8/05

    /** Construct a CSPReceiver with no container.
     */
    public CSPReceiver() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct a CSPReceiver with the specified container.
     *  @param container The port containing this receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public CSPReceiver(IOPort container) throws IllegalActionException {
        super(container);
        _boundaryDetector = new BoundaryDetector(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset local flags.
     */
    @Override
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
    @Override
    public Token get() throws TerminateProcessException {
        CSPDirector director = _getDirector();
        synchronized (director) {
            Token result = null;
            try {
                if (_putWaiting != null) {
                    Thread otherThread = _putWaiting;
                    // This will unblock the put() thread.
                    _putWaiting = null;
                    // Notify the director that the put() is no longer blocked.
                    // NOTE: This should be done here rather than
                    // in the put() method because since the put()
                    // method is running in another thread, it could
                    // take arbitrarily long for it to resume and
                    // notify the director. This could result in
                    // spurious deadlock detection.
                    director.threadUnblocked(otherThread, this);

                    result = _token;
                    // When the corresponding put() completes, it will reset this to true.
                    _rendezvousComplete = false;
                    // This will wake up the put() thread.
                    director.notifyAll();

                    // Wait for the put() thread to wake up.
                    while (!_rendezvousComplete) {
                        _checkFlagsAndWait();
                    }
                } else {
                    // System.out.println("++++++++ get got here first");

                    // get() got there first, so have to wait for a put;
                    _getWaiting = Thread.currentThread();
                    if (_isConditionalSendWaiting()) {
                        // Mark the other thread unblocked. This must be done
                        // in this thread to prevent spurious deadlock detection.
                        director.threadUnblocked(_conditionalSendWaiting, this);
                    }
                    // Notify the director that we are blocked.
                    // NOTE: Spurious deadlock detection is prevented by
                    // notifying the director above that the other thread
                    // is unblocked.
                    director.threadBlocked(_getWaiting, this);

                    // Wake other receivers, possibly causing a conditional
                    // send to call put(), which will result in resetting
                    // _getWaiting and notifying the director that we are
                    // no longer blocked.
                    director.notifyAll();
                    // Give the above notify a chance to work.
                    // System.out.println("++++++++ waiting to clear _conditionalSendWaiting.");

                    while (_conditionalSendWaiting != null) {
                        _checkFlagsAndWait();
                    }
                    _checkFlags();

                    // System.out.println("++++++++ cleared");

                    // If _getWaiting is still true, then there was no
                    // conditional send pending, so we just have to wait
                    // for a put() or a conditional send. Do that now.

                    // System.out.println("++++++++ waiting to clear _getWaiting.");

                    while (_getWaiting != null) {
                        _checkFlagsAndWait();
                    }
                    _checkFlags();

                    // System.out.println("++++++++ cleared");

                    // By the time we get here, the put() is complete,
                    // and _token has the value sent.
                    result = _token;

                    _rendezvousComplete = true;

                    // Notify any receivers that might be waiting for
                    // the rendevous to complete.
                    director.notifyAll();
                }
            } catch (InterruptedException ex) {
                throw new TerminateProcessException(
                        "CSPReceiver.get() interrupted.");
            } finally {
                if (_getWaiting != null) {
                    // If the _getWaiting flag is still true, then this
                    // process was blocked, woken up and terminated.
                    // Notify the director that this is unblocked.
                    director.threadUnblocked(_getWaiting, this);
                    _getWaiting = null;
                }
            }
            return result;
        }
    }

    /** Return true. This method returns true in all cases
     *  to indicate that the next call to put() will succeed
     *  without throwing a NoRoomException, as indeed it will,
     *  even if not right away.  Note that if this were to return
     *  true only if a rendezvous was pending, then polymorphic actors
     *  would busy wait.
     *  @return True.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean hasToken(int tokens) {
        return true;
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is contained
     *  by a composite actor. If this receiver is connected to the inside
     *  of a boundary port, then return true; otherwise return false.
     *  @return True if this receiver is connected to the inside of a
     *   boundary port; return false otherwise.
     * @exception IllegalActionException
     */
    @Override
    public boolean isConnectedToBoundary() throws IllegalActionException {
        return _boundaryDetector.isConnectedToBoundary();
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
     *  @return True if this receiver is connected to the inside of
     *   a boundary port; return false otherwise.
     * @exception IllegalActionException
     * @exception InvalidStateException
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    @Override
    public boolean isConnectedToBoundaryInside() throws InvalidStateException,
    IllegalActionException {
        return _boundaryDetector.isConnectedToBoundaryInside();
    }

    /** Return true if this receiver is connected to the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the outside of a boundary port, then return true; otherwise
     *  return false.
     *  @return True if this receiver is connected to the outside of
     *   a boundary port; return false otherwise.
     * @exception IllegalActionException
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    @Override
    public boolean isConnectedToBoundaryOutside() throws IllegalActionException {
        return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /** This class serves as an example of a ConsumerReceiver and
     *  hence this method returns true.
     * @exception IllegalActionException
     */
    @Override
    public boolean isConsumerReceiver() throws IllegalActionException {
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
    @Override
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
    @Override
    public boolean isOutsideBoundary() {
        return _boundaryDetector.isOutsideBoundary();
    }

    /** Return true if this receiver is on an outside or
     *  an inside boundary.
     */
    @Override
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
    @Override
    public boolean isReadBlocked() {
        synchronized (_getDirector()) {
            return _getWaiting != null || _conditionalReceiveWaiting != null;
        }
    }

    /** Return true if there is either a put or a conditional send
     *  waiting on this receiver.
     *  @return A boolean indicating whether a write is pending on this
     *   receiver.
     */
    @Override
    public boolean isWriteBlocked() {
        synchronized (_getDirector()) {
            return _putWaiting != null || _conditionalSendWaiting != null;
        }
    }

    /** Put a token into the mailbox receiver. This method does
     *  not return until the rendezvous is complete.
     *  This method is internally synchronized on the director.
     *  If the specified token is null, this method does nothing.
     *  @param token The token, or null to do nothing.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e it was not allowed to run to completion.
     */
    @Override
    public void put(Token token) throws TerminateProcessException {
        if (token == null) {
            return;
        }
        CSPDirector director = _getDirector();
        synchronized (director) {
            try {
                // Perform the transfer.
                _token = token;

                if (_getWaiting != null) {

                    // System.out.println("-------- get waiting");

                    // Reset the get waiting flag in this thread
                    // rather than the other one.
                    Thread otherThread = _getWaiting;
                    _getWaiting = null;

                    // A get() is waiting on this receiver.
                    // Notify the director that the get() is no longer blocked.
                    // NOTE: This should be done here rather than
                    // in the get() method because since the get()
                    // method is running in another thread, it could
                    // take arbitrarily long for it to resume and
                    // notify the director. This could result in
                    // spurious deadlock detection.
                    director.threadUnblocked(otherThread, this);

                    // Wake up the waiting get(s).
                    director.notifyAll();

                    // Wait for the get() to complete.
                    // When the corresponding get() completes, it will reset this to true.
                    // System.out.println("-------- waiting for rendezvous to complete on " + this);
                    _rendezvousComplete = false;
                    while (!_rendezvousComplete) {
                        _checkFlagsAndWait();
                    }
                    // System.out.println("-------- rendezvous complete");

                    return;
                } else {
                    // No get() is waiting on this receiver.
                    _putWaiting = Thread.currentThread();
                    if (_isConditionalReceiveWaiting()) {
                        // Mark the other thread unblocked. This
                        // must be done here to prevent spurious
                        // deadlock detection.
                        director.threadUnblocked(_conditionalReceiveWaiting,
                                this);
                    }
                    // Notify the director that we are blocked.
                    // NOTE: Spurious deadlock detection is prevented by
                    // notifying the director above that the other thread
                    // is unblocked.
                    director.threadBlocked(_putWaiting, this);

                    // There might be a conditional receive pending,
                    // in which case, we wake it up. It will see that
                    // _putWaiting is true, and call get(), which
                    // will reset _putWaiting and notify the director
                    // that we are no longer blocked.
                    director.notifyAll();
                    // Give the above notify a chance to work.
                    while (_conditionalReceiveWaiting != null) {
                        _checkFlagsAndWait();
                    }
                    _checkFlags();

                    // If _putWaiting is still non-null, then there
                    // was no conditional receiver, or it wasn't chosen.
                    // Wait for the corresponding get() to occur.
                    while (_putWaiting != null) {
                        _checkFlagsAndWait();
                    }
                    _checkFlags();
                    _rendezvousComplete = true;
                    // Notify any receivers that might be waiting for
                    // the rendezvous to complete.
                    director.notifyAll();
                    return;
                }
            } catch (InterruptedException ex) {
                throw new TerminateProcessException(
                        "CSPReceiver.put() interrupted.");
            } finally {
                if (_putWaiting != null) {
                    // If the put is still marked as waiting, then
                    // process was blocked, awakened and terminated.
                    // Notify the director that this actor is not blocked.
                    director.threadUnblocked(_putWaiting, this);
                }
            }
        }
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

    /** Put to all receivers in the specified array.
     *  This method starts a thread for each receiver
     *  after the first one to perform the put() on that
     *  receiver, and then calls put() on the first receiver.
     *  Thus, each of the put() calls occurs in a different
     *  thread. This method does not return until all the
     *  put() calls have succeeded.
     *  If the specified token is null, this method does nothing.
     *  @param token The token to put, or null to put no token.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of CSPReceiver.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e it was not allowed to run to completion.
     */
    @Override
    public void putToAll(final Token token, final Receiver[] receivers)
            throws NoRoomException, IllegalActionException,
            TerminateProcessException {
        if (token == null || receivers == null || receivers.length == 0) {
            return;
        }

        // Spawn a thread for each rendezvous.
        // If an exception occurs in one of the threads,
        // then this thread will find out about it and
        // report it.
        _exception = null;
        _terminateException = null;

        List<Thread> threads = null;
        final CSPDirector director = _getDirector();
        synchronized (director) {
            if (receivers.length == 1) {
                receivers[0].put(getContainer().convert(token));
            } else {
                // Create a thread for each destination.
                // List to keep track of created threads.
                threads = new LinkedList<Thread>();
                final Thread putToAllThread = Thread.currentThread();
                // NOTE: Use _threadCount to determine when the last
                // thread exits.  This assumes that we do not have
                // more than one invocation of this method active at
                // a time.  Check to be sure.
                if (_threadCount != 0) {
                    throw new InternalErrorException(
                            "putToAll() method is simultaneously active in more than one thread!"
                                    + " This is not permitted.");
                }
                _threadCount = receivers.length;
                for (Receiver receiver2 : receivers) {
                    final CSPReceiver receiver = (CSPReceiver) receiver2;
                    String name = "Send to "
                            + receiver.getContainer().getFullName();
                    Thread putThread = new Thread(name) {
                        @Override
                        public void run() {
                            // System.out.println("**** starting thread on: " + CSPDirector._receiverStatus(receiver));
                            try {
                                IOPort port = receiver.getContainer();
                                receiver.put(port.convert(token));
                            } catch (IllegalActionException e) {
                                _exception = e;
                            } catch (TerminateProcessException e) {
                                // To stop the actor thread, we have
                                // to throw this exception.
                                _terminateException = e;
                            } finally {
                                // Have to synchronize on the director to avoid
                                // race condition between decrement of _threadCount
                                // and testing it.
                                synchronized (director) {
                                    director.removeThread(this);
                                    _threadCount--;
                                    if (_threadCount == 0) {
                                        // The last thread to complete has to mark the
                                        // thread calling putToAll() unblocked. It has to
                                        // done in this last thread to complete or a
                                        // spurious deadlock will be detected between the
                                        // time this thread completes and the time that
                                        // that the putToAllThread gets around to marking
                                        // itself unblocked.
                                        director.threadUnblocked(
                                                putToAllThread,
                                                CSPReceiver.this);
                                    }
                                }
                            }
                        }
                    };
                    threads.add(putThread);
                    director.addThread(putThread);
                    putThread.start();
                }
                // Wait for each of the threads to die.
                // First notify the director that this actor is blocked.
                // The last thread to complete will unblock it.
                director.threadBlocked(Thread.currentThread(), this);
                for (Thread thread : threads) {
                    try {
                        // NOTE: Cannot use Thread.join() here because we
                        // have to be in a synchronized block to prevent
                        // a race condition (see below), and if we call
                        // thread.join(), then we will block while holding
                        // a lock on the director, which will lead to deadlock.
                        while (director.isThreadActive(thread)) {
                            director.wait();
                        }
                    } catch (InterruptedException ex) {
                        // Ignore and continue to the next thread.
                    }
                }
                // This should be zero, but just in case.
                _threadCount = 0;
            }
            // System.out.println("**** put() returned on: " + CSPDirector._receiverStatus(this));
            if (_exception != null) {
                throw _exception;
            }
            if (_terminateException != null) {
                throw _terminateException;
            }
        }
    }

    /** The model has finished executing, so set a flag so that the
     *  next time an actor tries to get or put it gets a
     *  TerminateProcessException which will cause it to finish.
     */
    @Override
    public void requestFinish() {
        Object lock = _getDirector();
        synchronized (lock) {
            _modelFinished = true;

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
            _rendezvousComplete = false;

            // Wake up any pending threads. EAL 12/04
            lock.notifyAll();
        }
    }

    /** Reset local flags.
     */
    @Override
    public void reset() {
        Object lock = _getDirector();
        synchronized (lock) {
            _getWaiting = null;
            _putWaiting = null;
            _setConditionalReceive(false, null, -1);
            _setConditionalSend(false, null, -1);
            _rendezvousComplete = false;
            _modelFinished = false;
            _boundaryDetector.reset();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** This method wraps the wait() call between checks on the state
     *  of the receiver. The flags checked are whether the receiver
     *  has been finished. The actions taken depending on the flags
     *  apply to whatever process this method was invoked from.
     *  <p>
     *  This method is internally synchronized on the director.
     *  To avoid missing events you should the callers also need
     *  to be synchronized on the director (this is currently the case).
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e it was not allowed to run to completion.
     *  @exception InterruptedException If the actor is
     *   interrupted while waiting(for a rendezvous to complete).
     */
    protected void _checkFlagsAndWait() throws TerminateProcessException,
    InterruptedException {
        // Actually you should already have a lock before calling this
        // method. Otherwise you will miss notifies and cause deadlocks.
        Object lock = _getDirector();
        synchronized (lock) {
            // FindBugs: Multithreaded correctness
            //  [M M Wa] Wait not in loop [WA_NOT_IN_LOOP]
            // Actually this wait does not need to be in a
            // loop since callers of this method will put this method
            // in a loop.

            // FindBugs: Multithreaded correctness
            // [M M UW] Unconditional wait [UW_UNCOND_WAIT]
            // Actually this wait does not to have a conditional
            // wait since the callers of this method are doing this.

            _checkFlags();
            lock.wait();
            _checkFlags();
        }
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

    /** Return whether a ConditionalReceive is trying
     *  to rendezvous with this receiver.
     *  @return True if a ConditionalReceive branch is trying to
     *   rendezvous with this receiver.
     */
    protected boolean _isConditionalReceiveWaiting() {
        return _conditionalReceiveWaiting != null;
    }

    /** Return whether a ConditionalSend is trying
     *  to rendezvous with this receiver.
     *  @return True if a ConditionalSend branch is trying to
     *   rendezvous with this receiver.
     */
    protected boolean _isConditionalSendWaiting() {
        return _conditionalSendWaiting != null;
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
        synchronized (_getDirector()) {
            if (ready) {
                _conditionalSendWaiting = Thread.currentThread();
            } else {
                if (_conditionalSendWaiting != null) {
                    _getDirector().threadUnblocked(_conditionalSendWaiting,
                            this);
                }
                _conditionalSendWaiting = null;
            }
            _otherController = controller;
            _otherID = otherID;
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
        synchronized (_getDirector()) {
            if (ready) {
                _conditionalReceiveWaiting = Thread.currentThread();
            } else {
                if (_conditionalReceiveWaiting != null) {
                    _getDirector().threadUnblocked(_conditionalReceiveWaiting,
                            this);
                }
                _conditionalReceiveWaiting = null;
            }
            _otherController = controller;
            _otherID = otherID;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check the flags controlling the state of the receiver and
     *  hence the actor process trying to rendezvous with it. If the
     *  model has finished executing, the _modelFinished flag will
     *  have been set and a TerminateProcessException will be thrown
     *  causing the actor process to finish.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e. it was not allowed to run to completion.
     */
    private void _checkFlags() throws TerminateProcessException {
        synchronized (_getDirector()) {
            if (_modelFinished) {
                throw new TerminateProcessException(getContainer().getName()
                        + ": terminated.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private BoundaryDetector _boundaryDetector;

    /** Flag indicating whether or not a conditional receive is waiting to rendezvous. */
    private Thread _conditionalReceiveWaiting = null;

    /** Flag indicating whether or not a conditional send is waiting to rendezvous. */
    private Thread _conditionalSendWaiting = null;

    /** Exception that might be set in putToAll(). */
    private IllegalActionException _exception = null;

    /** Thread blocked on a get(), if any. */
    private Thread _getWaiting = null;

    /** Flag indicating that any subsequent attempts to rendezvous
     * at this receiver should cause the attempting processes to terminate.
     */
    private boolean _modelFinished = false;

    /** obsolete when implement containment */
    private AbstractBranchController _otherController = null;

    private int _otherID = -1;

    /** Thread waiting on a put(), if any. */
    private Thread _putWaiting = null;

    /** Flag indicating whether state of rendezvous. */
    private boolean _rendezvousComplete = false;

    /** Exception that might be set in putToAll(). */
    private TerminateProcessException _terminateException = null;

    /** Thread count used in putToAll(). */
    private int _threadCount = 0;

    /** The token being transferred during the rendezvous. */
    private Token _token;
}
