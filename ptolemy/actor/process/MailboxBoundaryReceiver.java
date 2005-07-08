/* A process receiver that stores tokens via a mailbox and can be used by
 composite actors.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
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
    public Token get() {
        return get(null);
    }

    /** Get a token from the receiver and use the specified branch object
     *  to manage the method invocation. If the receiver is empty then
     *  block until a token becomes available. If the specified branch
     *  object is non-null then use the branch object to coordinate the
     *  blocking read with a branch controller. If the specified branch
     *  object is null then use the local director to coordinate the block.
     *  If this receiver is terminated during the execution of this method,
     *  then throw a TerminateProcessException.
     *
     *  @param branch The Branch managing execution of this method.
     *  @return The token contained by this receiver.
     */
    public Token get(Branch branch) {
        Workspace workspace = getContainer().workspace();
        Token result = null;

        synchronized (this) {
            if (!_terminate && !hasToken()) {
                _readBlock = true;
                prepareToBlock(branch);

                while (_readBlock && !_terminate) {
                    try {
                        workspace.wait(this);
                    } catch (InterruptedException e) {
                        _terminate = true;
                        break;
                    }
                }
            }

            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                result = super.get();

                if (_writeBlock) {
                    wakeUpBlockedPartner();
                    _writeBlock = false;
                    notifyAll();
                }

                return result;
            }
        }
    }

    /** Return true if this receiver is connected to a boundary port.
     *  A boundary port is an opaque port that is contained by a
     *  composite actor. If this receiver is connected to a boundary
     *  port, then return true; otherwise return false.
     *  This method is not synchronized so the caller should be.
     *
     *  @return True if this receiver is connected to boundary port;
     *   return false otherwise.
     */
    public boolean isConnectedToBoundary() {
        return _boundaryDetector.isConnectedToBoundary();
    }

    /** Return true if this receiver is connected to the inside of an
     *  input boundary port; return false otherwise. A boundary port is
     *  an opaque port that is contained by a composite actor. This
     *  method is not synchronized so the caller should be.
     *
     *  @return True if this receiver is connected to the inside of a
     *   boundary port; return false otherwise.
     */
    public boolean isConnectedToBoundaryInside() {
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
     */
    public boolean isConnectedToBoundaryOutside() {
        return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /** Return true if this is a consumer receiver; return false otherwise.
     *  A consumer receiver is defined as a receiver that is connected to
     *  a boundary port.
     *
     *  @return True if this is a consumer receiver; return false otherwise.
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
     *  return false. This method is not synchronized so the caller
     *  should be.
     *
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
     *  return false. This method is not synchronized so the caller
     *  should be.
     *
     *  @return True if this receiver is contained on the outside of a
     *   boundary port; return false otherwise.
     */
    public boolean isOutsideBoundary() {
        return _boundaryDetector.isOutsideBoundary();
    }

    /** Return true if this is a producer receiver; return false otherwise.
     *  A producer receiver is defined as a receiver that is connected to
     *  a boundary port.
     *
     *  @return True if this is a producer receiver; return false otherwise.
     */
    public boolean isProducerReceiver() {
        if (isOutsideBoundary() || isInsideBoundary()) {
            return true;
        }

        return false;
    }

    /** Return true if this receiver is read blocked; return false
     *  otherwise.
     *
     *  @return True if this receiver is read blocked; return false
     *   otherwise.
     */
    public boolean isReadBlocked() {
        return _readBlock;
    }

    /** Return true if this receiver is write blocked; return false
     *  otherwise.
     *
     *  @return True if this receiver is write blocked; return false
     *   otherwise.
     */
    public boolean isWriteBlocked() {
        return _writeBlock;
    }

    /** Prepare to register a block. If the branch object specified as
     *  a parameter is non-null then register the block with the branch.
     *  If the branch object specified as a parameter is null then
     *  register the block with the local director.
     *
     *  @param branch The Branch managing execution of this method.
     */
    public synchronized void prepareToBlock(Branch branch) {
        if (branch != null) {
            branch.registerReceiverBlocked(this);
            _otherBranch = branch;
        } else {
            ProcessDirector director = ((ProcessDirector) ((Actor) (getContainer()
                    .getContainer())).getDirector());
            director._actorBlocked(this);
            _otherBranch = branch;
        }
    }

    /** Put a token in the receiver and use the specified branch object
     *  to manage the method invocation. If the receiver is full (contains
     *  a token) then block until room becomes available. If the specified
     *  branch object is non-null then use the branch object to coordinate the
     *  blocking read with a branch controller. If the specified branch
     *  object is null then use the local director to coordinate the block.
     *  If this receiver is terminated during the execution of this method,
     *  then throw a TerminateProcessException.
     *
     *  @param branch The Branch managing execution of this method.
     *  @param token The token to be placed in this receiver.
     */
    public void put(Token token, Branch branch) {
        Workspace workspace = getContainer().workspace();

        synchronized (this) {
            if (!_terminate && !hasRoom()) {
                _writeBlock = true;
                prepareToBlock(branch);

                while (_writeBlock && !_terminate) {
                    try {
                        workspace.wait(this);
                    } catch (InterruptedException e) {
                        _terminate = true;
                        break;
                    }
                }
            }

            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                super.put(token);

                if (_readBlock) {
                    wakeUpBlockedPartner();
                    _readBlock = false;
                    notifyAll();
                }
            }
        }
    }

    /** Put a token into this receiver. If the receiver is full
     *  (contains a token) then block until room becomes available.
     *  Use the local director to manage blocking writes that occur.
     *  If this receiver is terminated during the execution of this
     *  method, then throw a TerminateProcessException.
     *
     *  @param token The token being placed in this receiver.
     */
    public void put(Token token) {
        put(token, null);
    }

    /** Set a local flag requesting that execution of the actor
     *  containing this receiver discontinue.
     */
    public synchronized void requestFinish() {
        _terminate = true;
        notifyAll();
    }

    /** Reset the local flags of this receiver. Use this method when
     *  restarting execution.
     */
    public void reset() {
        _terminate = false;
        _readBlock = false;
        _writeBlock = false;
        _boundaryDetector.reset();
    }

    /** Unblock this receiver and register this new state with
     *  either the monitoring branch or the local director. If
     *  there is no blocked branch waiting, then register the
     *  new state with the local director; otherwise, register
     *  the new state with the blocked branch.
     */
    public synchronized void wakeUpBlockedPartner() {
        if (_otherBranch != null) {
            _otherBranch.registerReceiverUnBlocked(this);
        } else {
            ProcessDirector director = ((ProcessDirector) ((Actor) (getContainer()
                    .getContainer())).getDirector());
            director._actorUnBlocked(this);
        }

        notifyAll();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private boolean _terminate = false;

    private boolean _readBlock = false;

    private boolean _writeBlock = false;

    private Branch _otherBranch = null;

    private BoundaryDetector _boundaryDetector;
}
