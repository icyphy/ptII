/* A controller that manages the conditional branches for performing
   conditional communication within CSP domain.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (bilung@eecs.berkeley.edu)
@AcceptedRating Red (bilung@eecs.berkeley.edu)

*/

package ptolemy.actor.process;

// Ptolemy imports.
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.data.Token;
import ptolemy.kernel.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;

// Java imports
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// BranchController
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

   <p>
@author Neil Smyth, John S. Davis II
@version $Id$
*/

public class BranchController {

    /** Construct a controller in the specified container, which should
        be an actor.
        @param container The parent actor that contains this object.
    */
    public BranchController(CompositeActor container) {
        _parentActor = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine which branch succeeds with a rendezvous. This method is
     *  central to nondeterministic rendezvous. It is passed in an array
     *  of branches, each element of which represents one of the
     *  conditional rendezvous branches. If the guard for the branch is
     *  false then the branch is not enabled.  It returns the id of
     *  the successful branch, or -1 if none of the branches were enabled.
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
     *   branches were enabled.
    public int chooseBranch(Branch[] branches) {
        try {
            synchronized(_internalLock) {
                // reset the state that controls the conditional branches
                _resetConditionalState();

                // Create the threads for the branches.
                _threadList = new LinkedList();
                Branch onlyBranch = null;
                for (int i = 0; i< branches.length; i++) {
                    // If the guard is false, then the branch is not enabled.
                    if (branches[i].getGuard()) {
                        // Create a thread for this enabled branch
                        Nameable act =
			    (Nameable)branches[i].getController().getParent();
                        String name = act.getName() + branches[i].getID();
                        Thread t = new Thread((Runnable)branches[i], name);
                        _threadList.add(0, t);
                        onlyBranch = branches[i];
                    }
                }

                // Three cases: 1) No guards were true so return -1
                // 2) Only one guard was true so perform the rendezvous
                // directly, 3) More than one guard was true, so start
                // the thread for each branch and wait for one of them
                // to rendezvous.
                int num = _threadList.size();

                if (num == 0) {
                    // The guards preceding all the conditional
                    // communications were false, so no branches to create.
                    return _successfulBranch; // will be -1
                } else if (num == 1) {
                    // FIXME!!!
                    // Call optimize method here; should I
                    // Only one guard was true, so perform simple rendezvous.
                    if (onlyBranch.isWriteBranch()) {
                        Token t = onlyBranch.getToken();
                        onlyBranch.getReceiver().put(t);
                        return onlyBranch.getID();
                    } else {
                        // branch is a ConditionalReceive
                        Token tmp = onlyBranch.getReceiver().get();
                        onlyBranch.setToken(tmp);
                        return onlyBranch.getID();
                    }
                } else {
                    // Have a proper conditional communication.
                    // Start the threads for each branch.
                    Iterator threads = _threadList.iterator();
                    while (threads.hasNext()) {
                        Thread thread = (Thread)threads.next();
                        thread.start();
                        _branchesActive++;
                    }
                    _branchesStarted = _branchesActive;
		    // wait for a branch to succeed
                    while ((_successfulBranch == -1) &&
                            (_branchesActive > 0)) {
                        _internalLock.wait();
                    }
                }
            }
            // If we get to here, we have more than one conditional branch.
            LinkedList tmp = new LinkedList();

            // Now terminate non-successful branches
            for (int i = 0; i < branches.length; i++) {
                // If the guard for a branch is false, it means a
                // thread was not created for that branch.
                if ( (i!= _successfulBranch) && (branches[i].getGuard()) ) {
                    // to terminate a branch, need to set a flag
                    // on the receiver it is rendezvousing with & wake it up
                    Receiver rec = branches[i].getReceiver();
                    tmp.add(0, rec);
                    branches[i].setAlive(false);
                }
            }
            // Now wake up all the receivers.
            (new NotifyThread(tmp)).start();

            // when there are no more active branches, branchFailed()
            // should issue a notifyAll() on the internal lock.
            synchronized(_internalLock) {
                while (_branchesActive != 0) {
                    _internalLock.wait();
                }
                // counter indicating # active branches, should be zero
                if (_branchesActive != 0) {
                    throw new InvalidStateException(
			    ((Nameable)getParent()).getName() +
                            ": chooseBranch() is exiting with branches" +
                            " still active.");
                }
            }
        } catch (InterruptedException ex) {
            throw new TerminateProcessException(
		    ((Nameable)getParent()).getName() +
		    ".chooseBranch interrupted.");
        }
        if (_successfulBranch == -1) {
            // Conditional construct was ended prematurely
            if (_blocked) {
                // Actor was registered as blocked when the
                // construct was terminated.
                if( _extReadBranchesBlocked > 0 ) {
                    _getDirector()._actorReadUnBlocked(false);
		} else if( _writeBranchesBlocked == _branchesStarted ) {
                    _getDirector()._actorWriteUnBlocked();
		} else {
                    _getDirector()._actorReadUnBlocked(true);
                }
		// _getDirector()._actorUnblocked();
            }
            throw new TerminateProcessException(
                    ((Nameable)getParent()).getName() +
                    ": exiting conditional" +
                    " branching due to TerminateProcessException.");
        }
        _threadList = null;
        return _successfulBranch;
    }
     */

    /** Return the Actor that creates the branch and owns this
     *  controller when performing a CIF or CDO.
     *  @return The CSPActor that created this branch.
     */
    public CompositeActor getParent() {
        return _parentActor;
    }

    /**
     */
    public void setBranches(Branch[] branches) {
    	_branches = branches;
    }
    
    /** Terminate abruptly any threads created by this actor. Note that
     *  this method does not allow the threads to terminate gracefully.
     */
    public void terminate() {
        synchronized(_internalLock) {
            // Now stop any threads created by this director.
            if (_threadList != null) {
                Iterator threads = _threadList.iterator();
                while (threads.hasNext()) {
                    Thread next = (Thread)threads.next();
                    if (next.isAlive()) {
                        next.stop();
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     */
    protected boolean _isDeadlocked() {
        if( _branchesBlocked == _branchesStarted ) {
            return true;
        } 
        return false;
    }
    
    /** Called by ConditionalSend and ConditionalReceive to check if
     *  the calling branch is the first branch to be ready to rendezvous.
     *  If it is, it sets a private variable to its branch ID so that
     *  subsequent calls to this method by other branches know that they
     *  are not first.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     *  @return True if the calling branch is the first branch to try
     *   to rendezvous, otherwise false.
     */
    protected boolean _canBranchContinue(int branchNumber) {
        synchronized(_internalLock) {
            if ((_branchTrying == -1) || (_branchTrying == branchNumber)) {
                // store branchNumber
                _branchTrying = branchNumber;
                return true;
            }
            return false;
        }
    }

    /** Increase the count of branches that are blocked trying to do
     *  a rendezvous read. If all the enabled branches (for the CIF 
     *  or CDO currently being executed) are blocked, register this 
     *  actor as being blocked.
     */
    protected void _branchBlocked() {
        synchronized(_internalLock) {
            if ( _isDeadlocked() ) {
                // Note: acquiring a second lock, need to be careful.
                if( !_hasReadBlock() ) {
                    _getDirector()._actorWriteBlocked();
                } else if( _hasInternalBlock() ) {
                    _getDirector()._actorReadBlocked(true);
                } else {
                    _getDirector()._actorReadBlocked(false);
                }
                _blocked = true;
            }
        }
    }

    /** Registers the calling branch as failed. It reduces the count
     *  of active branches, and if all the active branches have
     *  finished, it wakes notifies chooseBranch() to continue.
     *  It is called by a conditional branch just before it dies.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     */
    protected void _branchFailed(int branchNumber) {
        if (_successfulBranch == branchNumber) {
            // the execution of the model must have finished.
            _successfulBranch = -1;
        }
        synchronized(_internalLock) {
            _branchesActive--;
            if (_branchesActive == 0) {
                //System.out.println(getName() + ": Last branch finished, " +
                //      "waking up chooseBranch");
                _internalLock.notifyAll();
            }
        }
    }

    /** Registers the calling branch as the successful branch. It
     *  reduces the count of active branches, and notifies chooseBranch()
     *  that a branch has succeeded. The chooseBranch() method then
     *  proceeds to terminate the remaining branches. It is called by
     *  the first branch that succeeds with a rendezvous.
     *  @param branchID The ID assigned to the calling branch upon creation.
     */
    protected void _branchSucceeded(int branchID) {
        synchronized(_internalLock ) {
            if (_branchTrying != branchID) {
                throw new InvalidStateException(
			((Nameable)getParent()).getName() +
                        ": branchSucceeded called with a branch id not " +
                        "equal to the id of the branch registered as trying.");
            }
            _successfulBranch = _branchTrying;
            _branchesActive--;
            // wakes up chooseBranch() which wakes up parent thread
            _internalLock.notifyAll();
        }
    }

    /** Decrease the count of branches that are read blocked.
     *  If the actor was previously registered as being blocked, 
     *  register this actor with the director as no longer being 
     *  blocked.
     */
    protected void _branchUnblocked() {
        synchronized(_internalLock) {
 	    if (_blocked) {
            	if ( !_isDeadlocked() ) {
                    throw new InternalErrorException(
			    ((Nameable)getParent()).getName() +
                            ": blocked when not all enabled branches are " +
                            "blocked.");
		}
                if( !_hasReadBlock() ) {
                    _getDirector()._actorWriteUnBlocked();
                } else if( _hasInternalBlock() ) {
                    _getDirector()._actorReadUnBlocked(true);
                } else {
                    _getDirector()._actorReadUnBlocked(false);
                }
                _blocked = false;
            }
            _branchesBlocked--;
        }
    }
 
    /**
     */
    protected boolean _hasInternalBlock() {
    	return true;
    }
    
    /**
     */
    protected boolean _hasReadBlock() {
    	return true;
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
    protected void _releaseFirst(int branchNumber) {
        synchronized(_internalLock) {
            if (branchNumber == _branchTrying) {
                _branchTrying = -1;
                return;
            }
        }
        throw new InvalidStateException(
		((Nameable)getParent()).getName() +
		": Error: branch releasing first without possessing it! :"
		+ _branchTrying + " & " + branchNumber);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the director that controlls the execution of its parent actor.
     */
    private ProcessDirector _getDirector() {
        try {
	    if (_parentActor instanceof CompositeActor) {
		return  (ProcessDirector)_parentActor.getExecutiveDirector();
	    } else {
		return  (ProcessDirector)_parentActor.getDirector();
	    }
        } catch (NullPointerException ex) {
            // If a thread has a reference to a receiver with no director it
            // is an error so terminate the process.
	    throw new TerminateProcessException("CSPReceiver: trying to " +
                    " rendezvous with a receiver with no " +
                    "director => terminate.");
	}
    }

    /* Resets the internal state controlling the execution of a conditional
     * branching construct (CIF or CDO). It is only called by chooseBranch()
     * so that it starts with a consistent state each time.
     */
    private void _resetConditionalState() {
        synchronized(_internalLock) {
            _blocked = false;
            _branchesActive = 0;
	    _branchesBlocked = 0;
            _branchesStarted = 0;
            _branchTrying = -1;
            _successfulBranch = -1;
	    _threadList = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag indicating whether this actor is currently registered
    // as blocked while in the midst of a CDO or CIF.
    boolean _blocked = false;

    // Contains the number of conditional branches that are still
    // active.
    private int _branchesActive = 0;

    // Contains the number of conditional branches that are blocked
    // trying to rendezvous.
    private int _branchesBlocked = 0;

    // Contains the number of conditional write branches that are 
    // blocked trying to rendezvous.
    private int _writeBranchesBlocked = 0;

    // Contains the number of branches that were actually started for
    // the most recent conditional rendezvous.
    private int _branchesStarted = 0;

    // Contains the ID of the branch currently trying to rendezvous. It
    // is -1 if no branch is currently trying.
    private int _branchTrying = -1;

    // This lock is only used internally by the actor. It is used to
    // avoid having to synchronize on the actor itself. The chooseBranch()
    // method waits on it so it knows when a branch has succeeded and when
    // the last branch it created has died.
    private Object _internalLock = new Object();

    // Point to the actor who owns this controller object.
    private CompositeActor _parentActor;

    // Contains the ID of the branch that successfully rendezvoused.
    private int _successfulBranch = -1;

    // Threads created by this actor to perform a conditional rendezvous.
    // Need to keep a list of them in case the execution of the model is
    // terminated abruptly.
    private List _threadList = null;
    
    private Branch[] _branches;
}
