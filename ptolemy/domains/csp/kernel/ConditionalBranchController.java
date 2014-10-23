/* A controller that manages the conditional branches for performing
 conditional communication in the CSP domain.

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

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// ConditionalBranchController

/**
 A controller that manages the conditional branches for performing
 conditional communication within CSP (Communication Sequential Processes)
 domain. Any CSP actors (either atomic or composite) that need the
 functionality of conditional communication must contain and instantiate
 an object of this class. In addition, they also needs to implement the
 interface BranchActor.
 <p>
 The conditional branches are supposed to be created within the parent
 actor that contains this controller.
 <p>The chooseBranch() method takes those branches (an array) as an
 argument, and controls which branch is successful. The successful
 branch is the branch that succeeds with its communication. To
 determine which branch is successful, the guards of <I>all</I>
 branches are checked. If the guard for a branch is true then that
 branch is <I>enabled</I>. If no branches are enabled, i.e. if all
 the guards are false, then -1 is returned to indicate this.  If
 exactly one branch is enabled, the corresponding communication is
 carried out and the identification number of the branch is
 returned.  If more than one branch is enabled, a separate thread is
 created and started for each enabled branch. The method then waits
 for one of the branches to succeed, after which it wakes up and
 terminates the remaining branches. When the last conditional branch
 thread has finished, the method returns allowing the parent actor
 thread to continue.

 @author Neil Smyth, Bilung Lee, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (bilung)
 @see ConditionalBranch
 @see BranchActor
 @see ConditionalReceive
 @see ConditionalSend
 */
public class ConditionalBranchController extends AbstractBranchController {
    /** Construct a controller in the specified container, which should
     *  be an actor.
     *  @param container The parent actor that contains this object.
     */
    public ConditionalBranchController(Actor container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine which branch succeeds with a rendezvous. This method is
     *  central to nondeterministic rendezvous. It is given an array
     *  of branches, each element of which represents one of the
     *  conditional rendezvous branches. If the guard for the branch is
     *  false, then the branch is not enabled.  It returns the ID of
     *  the successful branch, or -1 if none of the branches is enabled.
     *  <p>
     *  If exactly one branch is enabled, then the communication is
     *  performed directly and the id of the enabled branch  is returned.
     *  If more than one branch is enabled, a thread  is created and
     *  started for each enabled branch. These threads try to
     *  rendezvous until one succeeds. After a thread succeeds the
     *  other threads are killed, and the id of the successful
     *  branch is returned.
     *  <p>
     *  @param branches The set of conditional branches involved.
     *  @return The ID of the successful branch, or -1 if none of the
     *   branches is enabled.
     *  @exception IllegalActionException If the rendezvous fails
     *   (e.g. because of incompatible types).
     */
    public int chooseBranch(ConditionalBranch[] branches)
            throws IllegalActionException {
        try {
            CSPDirector director = _getDirector();
            synchronized (director) {
                if (_debugging) {
                    _debug("** Choosing branches.");
                }
                _branches = branches;

                // reset the state that controls the conditional branches
                _resetConditionalState();

                // Create the threads for the branches.
                _threadList = new LinkedList();

                ConditionalBranch onlyBranch = null;

                for (ConditionalBranch branche : branches) {
                    // If the guard is false, then the branch is not enabled.
                    if (branche.getGuard()) {
                        // Create a thread for this enabled branch
                        Nameable actor = branche.getController().getParent();
                        String name = actor.getName() + branche.getID();
                        if (_debugging) {
                            _debug("** Creating branch: " + name);
                        }
                        Thread thread = new Thread((Runnable) branche, name);
                        _threadList.add(0, thread);
                        onlyBranch = branche;
                    }
                }

                // Three cases: 1) No guards were true so return -1
                // 2) Only one guard was true so perform the rendezvous
                // directly, 3) More than one guard was true, so start
                // the thread for each branch and wait for one of them
                // to rendezvous.
                int threadListSize = _threadList.size();

                if (threadListSize == 0) {
                    // The guards preceding all the conditional
                    // communications were false, so no branches to create.
                    if (_debugging) {
                        _debug("** No branches enabled.");
                    }
                    return _successfulBranch; // will be -1
                } else if (threadListSize == 1) {
                    // Only one guard was true, so perform simple rendezvous.
                    if (onlyBranch instanceof ConditionalSend) {
                        Token token = onlyBranch.getToken();
                        Receiver[] receivers = onlyBranch.getReceivers();
                        if (receivers != null && receivers.length >= 1) {
                            receivers[0].putToAll(token, receivers);
                        }
                        int result = onlyBranch.getID();
                        if (_debugging) {
                            _debug("** Succeessful branch is the only branch (ConditionalSend): "
                                    + result);
                        }
                        return result;
                    } else {
                        // branch is a ConditionalReceive
                        Receiver[] receivers = onlyBranch.getReceivers();
                        Token token = receivers[0].get();
                        onlyBranch._setToken(token);
                        int result = onlyBranch.getID();
                        if (_debugging) {
                            _debug("** Succeessful branch is the only branch (ConditionalReceive): "
                                    + result);
                        }
                        return result;
                    }
                } else {
                    // Have a proper conditional communication.
                    // Start the threads for each branch.
                    Iterator threads = _threadList.iterator();

                    while (threads.hasNext()) {
                        Thread thread = (Thread) threads.next();
                        _branchesActive++;
                        director.addThread(thread);
                        thread.start();
                    }

                    if (_debugging) {
                        _debug("** Waiting for branch to succeed.");
                    }
                    _blockedController = Thread.currentThread();
                    director.threadBlocked(_blockedController, null);
                    // wait for a branch to succeed
                    while (_successfulBranch == -1 && _branchesActive > 0
                            && !director.isStopRequested()) {
                        director.wait();
                    }
                    // In case the successful branch didn't mark this unblocked...
                    if (_blockedController != null) {
                        director.threadUnblocked(_blockedController, null);
                        _blockedController = null;
                    }
                }
                // NOTE: Below used to be outside the synchronized block. Why?
                // EAL 8/05

                // If we get to here, we have more than one conditional branch,
                // at most one of which has succeeded.
                // Now terminate non-successful branches.
                for (int i = 0; i < branches.length; i++) {
                    // If the guard for a branch is false, it means a
                    // thread was not created for that branch.
                    if (i != _successfulBranch && branches[i].getGuard()) {
                        branches[i]._setAlive(false);
                        if (_debugging) {
                            _debug("** Killing branch: " + branches[i].getID());
                        }
                    }
                }
                // Mark each of the threads unblocked, since
                // they will all terminate when awakened.
                // This has to be done in this thread, or for
                // a transitory time, it will appear as if there
                // were a deadlock.
                Iterator threads = _threadList.iterator();
                while (threads.hasNext()) {
                    Thread thread = (Thread) threads.next();
                    director.threadUnblocked(thread, null);
                }

                // Now wake up anyone waiting for something to change.
                director.notifyAll();

                // When there are no more active branches, branchFailed()
                // should issue a notifyAll() on the internal lock.
                if (_debugging) {
                    _debug("** Waiting for branches to die.");
                }
                while (_branchesActive != 0) {
                    director.wait();
                }
            }
            if (_successfulBranch == -1) {
                // Conditional construct was ended prematurely
                throw new TerminateProcessException(
                        ((Nameable) getParent()).getName()
                        + ": exiting conditional"
                        + " branching due to TerminateProcessException.");
            }

            _threadList = null;

            // Is it necessary to copy this? Note the finally clause below.
            int result = _successfulBranch;
            if (_debugging) {
                _debug("** Succeessful branch: " + result);
            }
            return result;
        } catch (InterruptedException ex) {
            throw new TerminateProcessException(
                    ((Nameable) getParent()).getName()
                    + ".chooseBranch interrupted.");
        } finally {
            _branches = null;
            _successfulBranch = -1;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Register the calling branch as failed. This reduces the count
     *  of active branches, and if all the active branches have
     *  finished, it notifies the internal lock so any threads
     *  that are blocked on it can continue.
     *  This is called by a conditional branch just before it dies.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     */
    @Override
    protected void _branchFailed(int branchNumber) {
        if (_successfulBranch == branchNumber) {
            // the execution of the model must have finished.
            _successfulBranch = -1;
        }
        super._branchFailed(branchNumber);
    }

    /** Release the status of the calling branch as the first branch
     *  to be ready to rendezvous. This method is only called when both
     *  sides of a communication at a receiver are conditional. In
     *  this case, both of the branches have to be the first branches,
     *  for their respective actors, for the rendezvous to go ahead. If
     *  one branch registers as being first, for its actor, but the
     *  other branch cannot, then the status of the first branch needs
     *  to be released to allow other branches the possibility of succeeding.
     *  @param branchNumber The ID assigned to the branch upon creation.
     */
    @Override
    protected void _branchNotReady(int branchNumber) {
        Object director = _getDirector();
        synchronized (director) {
            if (branchNumber == _branchTrying) {
                _branchTrying = -1;
                // FIXME: This has the potential of unblocking a blocked
                // thread, but we will need to mark that thread unblocked
                // in this thread or we will get a spurious deadlock detection.
                // How do we do that? How do we tell what thread will
                // be unblocked?  I guess it is associated with the
                // _branchTrying ID, but are we sure it will unblock?
                // Find out whether _isBranchReady() is called only
                // when the other thread can actually succeed, and
                // consider using the thread instead of the branch ID.
                director.notifyAll();
                return;
            }
        }
        throw new InvalidStateException(((Nameable) getParent()).getName()
                + ": Error: branch releasing first without possessing it! :"
                + _branchTrying + " & " + branchNumber);
    }

    /** Registers the calling branch as the successful branch. It
     *  reduces the count of active branches, and notifies chooseBranch()
     *  that a branch has succeeded. The chooseBranch() method then
     *  proceeds to terminate the remaining branches. It is called by
     *  the first branch that succeeds with a rendezvous.
     *  @param branchID The ID assigned to the calling branch upon creation.
     */
    @Override
    protected void _branchSucceeded(int branchID) {
        CSPDirector director = _getDirector();
        synchronized (director) {
            if (_branchTrying != branchID) {
                throw new InvalidStateException(
                        ((Nameable) getParent()).getName()
                        + ": branchSucceeded called with a branch id "
                        + branchID
                        + ", which is not "
                        + "equal to the id of the branch registered as trying,"
                        + _branchTrying);
            }
            _successfulBranch = _branchTrying;
            // Have to mark the controller thread unblocked in this thread
            // to prevent spurious deadlock detection.
            if (_blockedController != null) {
                director.threadUnblocked(_blockedController, null);
                _blockedController = null;
            }
            super._branchSucceeded(branchID);
        }
    }

    /** Called by ConditionalSend and ConditionalReceive to check whether
     *  the calling branch is the first branch to be ready to rendezvous.
     *  If it is, this method sets a private variable to its branch ID so that
     *  subsequent calls to this method by other branches know that they
     *  are not first.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     *  @return True if the calling branch is the first branch to try
     *   to rendezvous, otherwise false.
     */
    @Override
    protected boolean _isBranchReady(int branchNumber) {
        Object director = _getDirector();
        synchronized (director) {
            if (_branchTrying == -1 || _branchTrying == branchNumber) {
                // store branchNumber
                _branchTrying = branchNumber;
                director.notifyAll();
                return true;
            }

            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Resets the internal state controlling the execution of a conditional
     * branching construct (CIF or CDO). It is only called by chooseBranch()
     * so that it starts with a consistent state each time.
     */
    private void _resetConditionalState() {
        synchronized (_getDirector()) {
            _branchesActive = 0;
            _branchTrying = -1;
            _successfulBranch = -1;
            _threadList = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The ID of the branch currently trying to rendezvous. It
     *  is -1 if no branch is currently trying.
     */
    private int _branchTrying = -1;

    /** The ID of the branch that has successfully completed a rendezvous. */
    private int _successfulBranch = -1;

    /** The thread running chooseBranch if it is blocked. */
    private Thread _blockedController = null;
}
