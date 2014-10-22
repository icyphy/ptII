/* A process receiver that stores tokens via a mailbox and can be used by
 composite actors.

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
package ptolemy.actor.process;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MailboxBoundaryReceiver

/**
 A process receiver that stores tokens via a mailbox and can be used by
 composite actors. This receiver extends the functionality of the mailbox
 receiver found in the actor package in two key ways. First it facilitates
 blocking reads and writes. If a read (a call to get()) is attempted when
 this mailbox is empty then the call will block until a token is placed in
 the mailbox. Similarly if a write (a call to put()) is attempted when
 this mailbox is full (has a token) then the call will block until the
 token is removed from the mailbox.
 <P>
 The second key feature of this mailbox receiver is that it can be used
 by opaque composite actors operating in process-oriented models of
 computation. Indeed the name "MailboxBoundaryReceiver" is used to
 indicate that this receiver can be contained on the boundary of an
 opaque composite actor. The get() and put() methods of mailbox boundary
 receiver can be invoked by a Branch object. In such cases any blocks that
 occur are registered with the calling branch. The branch will then serve
 as a proxy by communicating to the director through the branch controller.
 <P>
 Note that it is not necessary for a mailbox boundary receiver to be used
 in the ports of an opaque composite actor. It is perfectly fine for a
 mailbox boundary receiver to be used in the ports of an atomic actor. In
 such cases the get() and put() methods are called without the use of a
 branch object. If blocking reads or writes occur they are registered with
 the controlling director without the need for a branch or branch
 controller.


 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (mudit)
 @Pt.AcceptedRating Yellow (davisj)
 @see ptolemy.actor.process.Branch
 @see ptolemy.actor.process.BranchController

 */
public class MailboxBoundaryReceiver extends Mailbox implements ProcessReceiver {
    /** Construct an empty MailboxBoundaryReceiver with no container.
     */
    public MailboxBoundaryReceiver() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty MailboxBoundaryReceiver with the specified
     *  container.
     *  @param container The container.
     *  @exception IllegalActionException If the container cannot contain
     *   this receiver.
     */
    public MailboxBoundaryReceiver(IOPort container)
            throws IllegalActionException {
        super(container);
        _boundaryDetector = new BoundaryDetector(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a token from this receiver. If the receiver is empty then
     *  block until a token becomes available. Use the local director
     *  to manage blocking reads that occur. If this receiver is
     *  terminated during the execution of this method, then throw a
     *  TerminateProcessException.
     *
     *  @return The token contained by this receiver.
     */
    @Override
    public Token get() {
        Workspace workspace = getContainer().workspace();
        Token result = null;

        int depth = 0;
        try {
            // NOTE: This used to synchronize on this, but since it calls
            // director methods that are synchronized on the director,
            // this can cause deadlock.
            synchronized (_director) {
                while (!_terminate) {
                    // Try to read.
                    if (super.hasToken()) {
                        result = super.get();

                        // Need to mark any thread that is write blocked
                        // on this receiver unblocked now, before any
                        // notification, or we will detect deadlock and
                        // increase the buffer sizes.  Note that there is
                        // no need to clear the _readPending reference
                        // because that will have been cleared by the
                        // write.
                        if (_writePending != null) {
                            _director.threadUnblocked(_writePending, this);
                            _writePending = null;
                        }

                        break;
                    }

                    // Wait to try again.
                    try {
                        _readPending = Thread.currentThread();
                        _director.threadBlocked(Thread.currentThread(), this);
                        // NOTE: We cannot use workspace.wait(Object) here without
                        // introducing a race condition, because we have to release
                        // the lock on the _director before calling workspace.wait(_director).
                        if (depth == 0) {
                            depth = workspace.releaseReadPermission();
                        }
                        _director.wait();
                    } catch (InterruptedException e) {
                        _terminate = true;
                    }
                }

                if (_terminate) {
                    throw new TerminateProcessException("");
                }
            }
        } finally {
            if (depth > 0) {
                workspace.reacquireReadPermission(depth);
            }
        }
        return result;
    }

    /** Return the director in charge of this receiver, or null
     *  if there is none.
     *  @return The director in charge of this receiver.
     */
    public ProcessDirector getDirector() {
        return _director;
    }

    /** Return true if this receiver is connected to a boundary port.
     *  A boundary port is an opaque port that is contained by a
     *  composite actor. If this receiver is connected to a boundary
     *  port, then return true; otherwise return false.
     *  This method is not synchronized so the caller should be.
     *
     *  @return True if this receiver is connected to boundary port;
     *   return false otherwise.
     * @exception IllegalActionException
     */
    @Override
    public boolean isConnectedToBoundary() throws IllegalActionException {
        return _boundaryDetector.isConnectedToBoundary();
    }

    /** Return true if this receiver is connected to the inside of an
     *  input boundary port; return false otherwise. A boundary port is
     *  an opaque port that is contained by a composite actor. This
     *  method is not synchronized so the caller should be.
     *
     *  @return True if this receiver is connected to the inside of a
     *   boundary port; return false otherwise.
     * @exception IllegalActionException
     * @exception InvalidStateException
     */
    @Override
    public boolean isConnectedToBoundaryInside() throws InvalidStateException,
            IllegalActionException {
        return _boundaryDetector.isConnectedToBoundaryInside();
    }

    /** Return true if this receiver is connected to the outside of an
     *  output boundary port; return false otherwise. A boundary port is
     *  an opaque port that is contained by a composite actor. If this
     *  receiver is contained on the inside of a boundary port, then return
     *  false. This method is not synchronized so the caller should be.
     *
     *  @return True if this receiver is connected to the outside of a
     *   boundary port; return false otherwise.
     * @exception IllegalActionException
     */
    @Override
    public boolean isConnectedToBoundaryOutside() throws IllegalActionException {
        return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /** Return true if this is a consumer receiver; return false otherwise.
     *  A consumer receiver is defined as a receiver that is connected to
     *  a boundary port.
     *
     *  @return True if this is a consumer receiver; return false otherwise.
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
     *  return false. This method is not synchronized so the caller
     *  should be.
     *
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
     *  return false. This method is not synchronized so the caller
     *  should be.
     *
     *  @return True if this receiver is contained on the outside of a
     *   boundary port; return false otherwise.
     */
    @Override
    public boolean isOutsideBoundary() {
        return _boundaryDetector.isOutsideBoundary();
    }

    /** Return true if this is a producer receiver; return false otherwise.
     *  A producer receiver is defined as a receiver that is connected to
     *  a boundary port.
     *
     *  @return True if this is a producer receiver; return false otherwise.
     */
    @Override
    public boolean isProducerReceiver() {
        if (isOutsideBoundary() || isInsideBoundary()) {
            return true;
        }

        return false;
    }

    /** Return a true or false to indicate whether there is a read block
     *  on this receiver or not, respectively.
     *  @return a boolean indicating whether a read is blocked on this
     *  receiver or not.
     */
    @Override
    public boolean isReadBlocked() {
        // NOTE: This method used to be synchronized on this
        // receiver, but since it is called by synchronized methods in
        // the director, that can cause deadlock.
        synchronized (_director) {
            return _readPending != null;
        }
    }

    /** Return a true or false to indicate whether there is a write block
     *  on this receiver or not.
     *  @return A boolean indicating whether a write is blocked  on this
     *  receiver or not.
     */
    @Override
    public boolean isWriteBlocked() {
        // NOTE: This method used to be synchronized on this
        // receiver, but since it is called by synchronized methods in
        // the director, that can cause deadlock.
        synchronized (_director) {
            return _writePending != null;
        }
    }

    /** Put a token into this receiver. If the receiver is full
     *  (contains a token) then block until room becomes available.
     *  Use the local director to manage blocking writes that occur.
     *  If this receiver is terminated during the execution of this
     *  method, then throw a TerminateProcessException.
     *  If the specified token is null, this method does nothing.
     *
     *  @param token The token being placed in this receiver, or null
     *   to do nothing.
     */
    @Override
    public void put(Token token) {
        if (token == null) {
            return;
        }
        Workspace workspace = getContainer().workspace();
        int depth = 0;
        try {
            // NOTE: This used to synchronize on this, but since it calls
            // director methods that are synchronized on the director,
            // this can cause deadlock.
            synchronized (_director) {
                while (!_terminate) {
                    // Try to write.
                    if (super.hasRoom()) {
                        super.put(token);

                        // If any thread is blocked on a get(), then it will become
                        // unblocked. Notify the director now so that there isn't a
                        // spurious deadlock detection.
                        if (_readPending != null) {
                            _director.threadUnblocked(_readPending, this);
                            _readPending = null;
                        }

                        // Normally, the _writePending reference will have
                        // been cleared by the read that unblocked this
                        // write.  However, it might be that the director
                        // increased the buffer size, which would also
                        // have the affect of unblocking this
                        // write. Hence, we clear it here if it is set.
                        if (_writePending != null) {
                            _director.threadUnblocked(_writePending, this);
                            _writePending = null;
                        }

                        break;
                    }

                    // Wait to try again.
                    try {
                        _writePending = Thread.currentThread();
                        _director.threadBlocked(_writePending, this);

                        // NOTE: We cannot use workspace.wait(Object) here without
                        // introducing a race condition, because we have to release
                        // the lock on the _director before calling workspace.wait(_director).
                        if (depth == 0) {
                            depth = workspace.releaseReadPermission();
                        }
                        _director.wait();
                    } catch (InterruptedException e) {
                        _terminate = true;
                    }
                }

                if (_terminate) {
                    throw new TerminateProcessException("Process terminated.");
                }
            }
        } finally {
            if (depth > 0) {
                workspace.reacquireReadPermission(depth);
            }
        }
    }

    /** Set a local flag requesting that execution of the actor
     *  containing this receiver discontinue.
     */
    @Override
    public synchronized void requestFinish() {
        _terminate = true;
        notifyAll();
    }

    /** Reset the local flags of this receiver. Use this method when
     *  restarting execution.
     */
    @Override
    public void reset() {
        if (_readPending != null) {
            _director.threadUnblocked(_readPending, this);
        }

        if (_writePending != null) {
            _director.threadUnblocked(_writePending, this);
        }

        _terminate = false;
        _boundaryDetector.reset();
    }

    /** Set the container. This overrides the base class to record
     *  the director.
     *  @param port The container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort, or if the container's director
     *   is not an instance of ProcessDirector.
     */
    @Override
    public void setContainer(IOPort port) throws IllegalActionException {
        super.setContainer(port);

        if (port == null) {
            _director = null;
        } else {
            Actor actor = (Actor) port.getContainer();
            Director director;

            // For a composite actor,
            // the receiver type of an input port is decided by
            // the executive director.
            // While the receiver type of an output is decided by the director.
            // NOTE: getExecutiveDirector() and getDirector() yield the same
            // result for actors that do not contain directors.
            if (port.isInput()) {
                director = actor.getExecutiveDirector();
            } else {
                director = actor.getDirector();
            }

            if (!(director instanceof ProcessDirector)) {
                throw new IllegalActionException(port,
                        "Cannot use an instance of PNQueueReceiver "
                                + "since the director is not a PNDirector.");
            }

            _director = (ProcessDirector) director;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** The boundary detector. */
    private BoundaryDetector _boundaryDetector;

    /** The director in charge of this receiver. */
    private ProcessDirector _director;

    /** Reference to a thread that is read blocked on this receiver. */
    private Thread _readPending = null;

    /** Flag indicating that termination has been requested. */
    private boolean _terminate = false;

    /** Reference to a thread that is write blocked on this receiver. */
    private Thread _writePending = null;
}
