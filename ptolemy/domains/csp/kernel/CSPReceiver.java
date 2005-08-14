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
import ptolemy.actor.process.Branch;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

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
 The static method groupReceivers(Receiver[]) can be used to create
 a group of receivers. A put() or get() to any receiver in
 a group of receivers performs  a multi-way rendezvous.  That is,
 a put() is executed on one receiver in the group only when a get()
 is executed on all other receivers in the group.  For multi-way
 rendezvous, a query about the status of any one receiver in
 the group returns the status of all the receivers in the group.
 <p>
 @author Neil Smyth, John S. Davis II, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (nsmyth)
 @Pt.AcceptedRating Green (kienhuis)
 */
public class CSPReceiver extends AbstractReceiver implements ProcessReceiver {
    
    // FIXME: Downgraded to Red when changing to support receiver groups.
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
    public void clear() {
        reset();
    }

    /** Get a token from the mailbox receiver and specify a null
     *  Branch to control the execution of this method. This method
     *  does not return until the rendezvous has been completed. If
     *  this receiver is in a receiver group, then this method does
     *  not return until all rendezvous in the group have completed.
     *  <p>
     *  This method is internally synchronized on either this
     *  receiver, or if this receiver is in a group, on the first
     *  receiver in the group.
     *  @return The token contained by this receiver.
     */
    public Token get() {
        return get(null);
    }

    /** Retrieve a Token from the receiver by rendezvous. This method
     *  does not return until the rendezvous has been completed. If
     *  this receiver is in a receiver group, then this method does
     *  not return until all rendezvous in the group have completed.
     *  If a put has already been reached, it notifies the waiting put
     *  and waits for the rendezvous to complete. When the rendezvous is
     *  complete it returns with the token.
     *  If a put has not yet been reached, the method delays until a
     *  put is reached.
     *  It is assumed that at most one process is trying to receive
     *  from and send to the channel associated with this receiver.
     *  <p>
     *  This method is internally synchronized on either this
     *  receiver, or if this receiver is in a group, on the first
     *  receiver in the group.
     *
     *  @return The token transferred by the rendezvous.
     *  @exception TerminateProcessException If execution termination
     *   has been requested, or if the execution is abruptly terminated
     *   from the outside (via an InterruptedException).
     */
    public Token get(Branch branch) {
        Object lock = _getLock();
        synchronized(lock) {
            Token result = null;
            boolean blocked = false;
            
            try {
                if (_putWaiting) {
                    // This will unblock the put() thread.
                    _putWaiting = false;

                    result = _token;
                    // The put() will set the following to true.
                    _rendezvousComplete = false;
                    // This will wake up the put() thread.
                    lock.notifyAll();
                    
                    // Wait for the put() thread to wake up,
                    // and also for all the rendezvous in the group
                    // to complete.
                    while (!_isRendezvousComplete()) {
                        _checkFlagsAndWait();
                    }
                } else {
                    // get() got there first, so have to wait for a put;
                    _getWaiting = true;
                    lock.notifyAll();
                    
                    // If there is a conditional send waiting on
                    // this receiver, then the above notify will wake
                    // it up and when it continues, it will
                    // reset the _isConditionalSendWaiting flag and
                    // do a get() which sets the _getWaiting flag to
                    // false and the rendezvous proceeds normally.
                    while (_conditionalSendWaiting) {
                        _checkFlagsAndWait();
                    }

                    // This is needed for the case when a conditionalSend
                    // reaches the receiver before a get. When the
                    // conditionalSend continues, it resets the
                    // _isConditionalSendWaiting flag and does a put()
                    // which sets the getWaiting flag to false and the
                    // rendezvous proceeds normally.
                    while (_isConditionalSendWaiting()) {
                        _checkFlagsAndWait();
                    }
                    
                    _checkFlags();
                    markBlocked(branch);
                    
                    blocked = true;
                    
                    while (_getWaiting) {
                        _checkFlagsAndWait();
                    }
                    
                    _checkFlags();
                    
                    // FIXME: This is a race condition that could
                    // lead to a deadlock false alarm. This should
                    // be done as soon as _getWaiting is set to
                    // false, which is done in put() by the other
                    // thread.  Why not just do markUnblocked() there?
                    markUnblocked();
                    
                    blocked = false;
                    result = _token;
                    _rendezvousComplete = true;
                    notifyAll();
                    
                    // In case this is a multi-way rendezvous,
                    // need to wait for all of them to complete.
                    while (!_isRendezvousComplete()) {
                        _checkFlagsAndWait();
                    }
                }
            } catch (InterruptedException ex) {
                throw new TerminateProcessException(
                "CSPReceiver.get() interrupted.");
            } finally {
                if (blocked) {
                    // process was blocked, woken up and terminated.
                    // register process as being unblocked
                    markUnblocked();
                }
            }
            
            return result;
        }
    }

    /** Cancel a relationship with a group of receivers.
     *  If a group has been previously specified, then
     *  this will cancel the group relationship for all
     *  the receivers in the group.
     *  @see #groupReceivers(Receiver[])
     */
    public void groupCancel() {
        if (_group != null) {
            for (int i = 0; i < _group.length; i++) {
                ((CSPReceiver)_group[i])._group = null;
            }
        }
    }

    /** Specify a group of receivers. These receivers will
     *  henceforth implement multi-way rendezvous. If
     *  the argument is null or a length 0 or 1 array, this
     *  method does nothing.
     *  @param group The group of receivers, which are required
     *   to be instances of CSPReceiver, or a ClassCastException
     *   will be thrown.
     *  @see #groupCancel()
     */
    public static void groupReceivers(Receiver[] group) {
        if (group != null && group.length > 1) {
            for (int i = 0; i < group.length; i++) {
                ((CSPReceiver)group[i])._group = group;
            }
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

    /** Return a true or false to indicate whether there is a read block
     *  on this receiver or not, respectively.
     *  @return True if a read is blocked on this receiver.
     */
    public boolean isReadBlocked() {
        synchronized(_getLock()) {
            return _readBlocked;
        }
    }

    /** Return a true or false to indicate whether there is a write block
     *  on this receiver or not.
     *  @return A boolean indicating whether a write is blocked  on this
     *   receiver or not.
     */
    public boolean isWriteBlocked() {
        synchronized(_getLock()) {
            return _writeBlocked;
        }
    }

    /** If this receiver is involved in a branch, then mark the receiver
     *  blocked; otherwise mark the actor blocked.
     *  @param branch The branch.
     */
    public void markBlocked(Branch branch) {
        synchronized(_getLock()) {
            if (branch != null) {
                branch.registerReceiverBlocked(this);
                _otherBranch = branch;
            } else {
                _getDirector()._actorBlocked(this);
                _otherBranch = branch;
            }
        }
    }

    /** If this receiver is involved in a branch, then mark the receiver
     *  unblocked; otherwise mark the actor unblocked.
     */
    public void markUnblocked() {
        Object lock = _getLock();
        synchronized(lock) {
            if (_otherBranch != null) {
                _otherBranch.registerReceiverUnBlocked(this);
            } else {
                _getDirector()._actorUnBlocked(this);
            }
            lock.notifyAll();
        }
    }

    /** Put a token into the mailbox receiver and specify a null
     *  Branch to control the execution of this method.
     *  <p>
     *  If this receiver is a member of a receiver group, then
     *  a put to this receiver is a put to all receivers in the
     *  group, and this method returns only when the puts are all
     *  complete.
     *  <p>
     *  This method is internally synchronized on either this
     *  receiver, or if this receiver is in a group, on the first
     *  receiver in the group.
     *  @param token The token.
     */
    public void put(Token token) {
        put(token, null);
    }

    /** Place a Token into the receiver via rendezvous. This method
     *  does not return until the rendezvous has been completed.
     *  If get has already been reached, it notifies the waiting get
     *  and waits for the rendezvous to complete. When the rendezvous is
     *  complete it returns.
     *  If a get has not yet been reached, the method delays until a
     *  get is reached.
     *  It is assumed that at most one process is trying to receive
     *  from and send to the channel associated with this receiver.
     *  <p>
     *  If this receiver is a member of a receiver group, then
     *  this method returns only when puts are all complete on
     *  the members of the group.
     *  <p>
     *  This method is internally synchronized on either this
     *  receiver, or if this receiver is in a group, on the first
     *  receiver in the group.
     *  @param token The token being transferred in the rendezvous.
     *  @param branch The branch, or null if this is not part of a
     *   conditional branch.
     *  @exception TerminateProcessException If execution termination
     *   has been requested, or if the execution is abruptly terminated
     *   from the outside (via an InterruptedException).
     */
    public void put(Token token, Branch branch) {
        Object lock = this;
        if (_group != null) {
            lock = _group[0];
        }
        synchronized(lock) {
            // Local flag keeping track of whether this thread is
            // marked as blocked.
            boolean blocked = false;

            try {
                // Perform the transfer.
                _token = token;

                if (_getWaiting) {
                    // A get() is waiting on this receiver.
                    // Reset the get waiting flag in this thread
                    // rather than the other one.
                    if (_group == null) {
                        _getWaiting = false;
                        // When the get() completes, it will reset this to true.
                        _rendezvousComplete = false;
                    } else {
                        for (int i = 0; i < _group.length; i++) {
                            ((CSPReceiver)_group[i])._getWaiting = false;
                            ((CSPReceiver)_group[i])._rendezvousComplete = false;
                        }
                    }

                    // Wake up the waiting get(s).
                    lock.notifyAll();

                    // Wait for the get() to complete, and also
                    // any other rendezvous in the group.
                    while (!_isRendezvousComplete()) {
                        _checkFlagsAndWait();
                    }

                    return;
                } else {
                    // No get() is waiting on this receiver.
                    _putWaiting = true;
                    lock.notifyAll();

                    // If there is a conditional receive waiting on
                    // this receiver, then the above notify will wake
                    // it up and when it continues, it will
                    // reset the _isConditionalReceiveWaiting flag and
                    // do a get() which sets the putWaiting flag to
                    // false and the rendezvous proceeds normally.
                    while (_conditionalReceiveWaiting) {
                        _checkFlagsAndWait();
                    }

                    _checkFlags();
                    markBlocked(branch);

                    blocked = true;

                    // Wait for the corresponding get() to occur.
                    while (_putWaiting) {
                        _checkFlagsAndWait();
                    }

                    _checkFlags();

                    // FIXME: This is a race condition that could
                    // lead to a deadlock false alarm. This should
                    // be done as soon as setGetWaiting(false) is
                    // called.
                    markUnblocked();

                    blocked = false;
                    _rendezvousComplete = true;
                    notifyAll();
                    
                    // If this receiver is in a group, some
                    // of the rendezvous may not yet be complete.
                    while (!_isRendezvousComplete()) {
                        _checkFlagsAndWait();
                    }
                    return;
                }
            } catch (InterruptedException ex) {
                throw new TerminateProcessException(
                        "CSPReceiver.put() interrupted.");
            } finally {
                if (blocked) {
                    // process was blocked, awakened and terminated.
                    // register process as being unblocked
                    markUnblocked();
                }
            }            
        }
    }

    /** Put to all receivers in the specified array.
     *  This method creates a receiver group for the specified
     *  receivers and then starts a thread for each receiver
     *  after the first one to perform the put() on that
     *  receiver, and then calls put() on the first receiver.
     *  @param token The token to put.
     *  @param receivers The receivers, which are assumed to
     *   all be instances of CSPReceiver.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putToAll(final Token token, final Receiver[] receivers)
            throws NoRoomException, IllegalActionException {
        if (receivers == null || receivers.length == 0) {
            return;
        }
        groupReceivers(receivers);
        
        final CSPDirector director = _getDirector();
        
        // Spawn a thread for each rendezvous.
        // If an exception occurs in one of the threads,
        // then this thread will find out about it and
        // report it.
        _exception = null;
        
        for (int j = 1; j < receivers.length; j++) {
            final CSPReceiver receiver = (CSPReceiver)receivers[j];
            Thread putThread = new Thread() {
                public void run() {
                    director._increaseActiveCount();
                    IOPort port = receiver.getContainer();
                    try {
                        receiver.put(port.convert(token));
                    } catch (IllegalActionException e) {
                        _exception = e;
                    }
                    receiver.groupCancel();
                    director._decreaseActiveCount();
                }
            };
            putThread.start();
        }
        IOPort port = getContainer();
        // The following does not return until all
        // the rendezvous are complete.
        receivers[0].put(port.convert(token));
        groupCancel();
        if (_exception != null) {
            throw _exception;
        }
    }

    /** The model has finished executing, so set a flag so that the
     *  next time an actor tries to get or put it gets a
     *  TerminateProcessException which will cause it to finish.
     */
    public void requestFinish() {
        Object lock = _getLock();
        synchronized(lock) {
            _setModelFinished(true);
            
            // Need to reset the state of the receiver.
            _setConditionalReceive(false, null, -1);
            _setConditionalSend(false, null, -1);
            _putWaiting = false;
            _getWaiting = false;
            _rendezvousComplete = false;
            
            // Wake up any pending threads. EAL 12/04
            lock.notifyAll();
        }
    }

    /** Reset local flags.
     */
    public void reset() {
        Object lock = _getLock();
        synchronized(lock) {
            _getWaiting = false;
            _putWaiting = false;
            _conditionalReceiveWaiting = false;
            _setConditionalSend(false, null, -1);
            _rendezvousComplete = false;
            _setModelFinished(false);
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
     *  This method is internally synchronized on either this
     *  receiver, or if this receiver is in a group, on the first
     *  receiver in the group.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e it was not allowed to run to completion.
     *  @exception InterruptedException If the actor is
     *   interrupted while waiting(for a rendezvous to complete).
     */
    protected void _checkFlagsAndWait()
            throws TerminateProcessException, InterruptedException {
        Object lock = _getLock();
        synchronized(lock) {
            _checkFlags();
            lock.wait();
            _checkFlags();
        }
    }
    
    /** Return this receiver, or if this receiver is in a receiver
     *  group, the first receiver in the group.
     *  @return The receiver on which to synchronize.
     */
    protected Object _getLock() {
        if (_group == null) {
            return this;
        } else {
            return _group[0];
        }
    }

    /** The controller of the conditional branch to reach the
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
    protected ConditionalBranchController _getOtherController() {
        // FIXME: Handle groups. How? Return all controllers?
        return _otherController;
    }

    /** Return the branch ID of the branch that requested the
     *  conditional receive.
     *  @return The branch ID.
     */
    protected int getOtherID() {
        // FIXME: Handle groups. How? Return all IDs?
        return _otherID;
    }

    /** Return whether a ConditionalReceive is trying
     *  to rendezvous with this receiver. If this receiver is a member
     *  of a group, then this returns true only if all receivers
     *  in the group have either a conditional receive waiting or
     *  a get waiting.
     *  @return True if a ConditionalReceive branch is trying to
     *   rendezvous with this receiver, or if this is a member of
     *   a group and all members of the group have either a
     *   conditional receive or a get waiting.
     */
    protected boolean _isConditionalReceiveWaiting() {
        if (_group == null) {
            return _conditionalReceiveWaiting;
        } else {
            for (int i = 0; i < _group.length; i++) {
                // If any member of the group is not waiting,
                // we can return false.
                if (!((CSPReceiver)_group[i])._conditionalReceiveWaiting
                        && !((CSPReceiver)_group[i])._getWaiting) {
                    return false;
                }
            }
            return true;
        }
    }

    /** Return whether a ConditionalSend is trying
     *  to rendezvous with this receiver.
     *  @return True if a ConditionalSend branch is trying to
     *   rendezvous with this receiver.
     */
    protected boolean _isConditionalSendWaiting() {
        return _conditionalSendWaiting;
    }

    /** Return whether a get() is waiting to rendezvous
     *  at this receiver. If this receiver is a member of a group, then
     *  this returns true only if all receivers in the group have a get()
     *  waiting.
     *  @return True if a get() is waiting to rendezvous.
     */
    protected boolean _isGetWaiting() {
        if (_group == null) {
            return _getWaiting;
        } else {
            for (int i = 0; i < _group.length; i++) {
                // If any member of the group is not waiting,
                // we can return false.
                if (!((CSPReceiver)_group[i])._getWaiting) {
                    return false;
                }
            }
            return true;
        }
    }

    /** Flag indicating whether or not a put() is waiting to rendezvous
     *  at this receiver.
     *  @return True if a put() is waiting to rendezvous.
     */
    protected boolean _isPutWaiting() {
        return _putWaiting;
    }

    /** Set a flag so that a ConditionalReceive branch knows whether or
     *  not a ConditionalSend is ready to rendezvous with it.
     *  If this receiver is a member of a group, then set the
     *  flag for all receivers in the group.
     *  @param ready Boolean indicating whether or not a conditional
     *   send is waiting to rendezvous.
     *  @param controller The CSPActor which contains the ConditionalSend
     *   branch that is trying to rendezvous. It is stored in the
     *   receiver so that if a ConditionalReceive arrives, it can easily
     *   check whether the ConditionalSend branch was the first
     *   branch of its conditional construct(CIF or CDO) to succeed.
     *  @param otherID The branch ID of the branch requesting the
     *   conditional send.
     */
    protected void _setConditionalSend(boolean ready,
            ConditionalBranchController controller, int otherID) {
        synchronized(_getLock()) {
            if (_group == null) {
                _conditionalSendWaiting = ready;
                _otherController = controller;
                _otherID = otherID;
            } else {
                for (int i = 0; i < _group.length; i++) {
                    ((CSPReceiver)_group[i])._conditionalSendWaiting = ready;
                    ((CSPReceiver)_group[i])._otherController = controller;
                    ((CSPReceiver)_group[i])._otherID = otherID;
                }
            }
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
            ConditionalBranchController controller, int otherID) {
        synchronized(_getLock()) {
            _conditionalReceiveWaiting = ready;
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
        synchronized(_getLock()) {
            if (_modelFinished) {
                throw new TerminateProcessException(getContainer().getName()
                        + ": terminated.");
            }
        }
    }
    
    /** Return true if the rendezvous is complete. If this receiver
     *  is a member of a group, then this returns true only if the
     *  rendezvous is complete for all receivers in the group.
     *  @return True if the rendezvous is complete.
     */
    private boolean _isRendezvousComplete() {
        if (_group == null) {
            return _rendezvousComplete;
        } else {
            for (int i = 0; i < _group.length; i++) {
                // If any member of the group is not waiting,
                // we can return false.
                if (!((CSPReceiver)_group[i])._rendezvousComplete) {
                    return false;
                }
            }
            return true;
        }
    }

    /** Return the director that is controlling the execution of this model.
     *  If this receiver is an inside receiver, then it is the director
     *  of the container (actor) of the container (port). Otherwise, it
     *  is the executive director of the container (actor) of the container
     *  (port).
     *  @return The CSPDirector controlling this model.
     */
    private CSPDirector _getDirector() {
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
    
    /** Indicate that the model has finished executing. If this
     *  receiver is part of the group, then the indication is provided
     *  to all members of the group.
     *  @param finished True to indicate that the model is finished.
     */
    private void _setModelFinished(boolean finished) {
        if (_group == null) {
            _modelFinished = finished;
        } else {
            for (int i = 0; i < _group.length; i++) {
                ((CSPReceiver)_group[i])._modelFinished = finished;
            }
        }        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Flag inidcating that this receiver is read blocked. */
    private boolean _readBlocked = false;

    /** Flag inidcating that this receiver is write blocked. */
    private boolean _writeBlocked = false;

    /** Flag indicating whether or not a get is waiting at this receiver. */
    private boolean _getWaiting = false;

    /** Flag indicating whether or not a get is waiting at this receiver. */
    private boolean _putWaiting = false;

    /** obsolete when implement containment */
    private ConditionalBranchController _otherController = null;

    private int _otherID = -1;

    /** Flag indicating whether or not a conditional receive is waiting to rendezvous. */
    private boolean _conditionalReceiveWaiting = false;

    /** Flag indicating whether or not a conditional send is waiting to rendezvous. */
    private boolean _conditionalSendWaiting = false;
    
    /** Exception that might be set in putToAll(). */
    private IllegalActionException _exception = null;
    
    /** Group of receivers for multi-way rendezvous.
     *  NOTE: This really should be a CSPReceiver[], but
     *  since arrays can't be cast in Java, this would require
     *  copying the array into each receiver. Since this
     *  occurs on every rendezvous, the cost would be high.
     */
    private Receiver[] _group;

    /** Flag indicating that any subsequent attempts to rendezvous
     * at this receiver should cause the attempting processes to terminate.
     */
    private boolean _modelFinished = false;

    /** Flag indicating whether state of rendezvous. */
    private boolean _rendezvousComplete = false;

    /** The token being transferred during the rendezvous. */
    private Token _token;

    private BoundaryDetector _boundaryDetector;

    private Branch _otherBranch = null;
}
