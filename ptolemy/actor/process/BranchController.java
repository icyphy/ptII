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

@author John S. Davis II
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
    ////                        public variables		   ////
    
    // public Parameter engagements;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the Actor that creates the branch and owns this
     *  controller when performing a CIF or CDO.
     *  @return The CSPActor that created this branch.
     */
    public CompositeActor getParent() {
        return _parentActor;
    }

    /**
     */
    public void runBranches() {
        while( true ) {
    	    startBranches();
            waitOnBranches();
        }
    }
    
    /**
     */
    public void createBranches(IOPort port) throws 
            IllegalActionException {
	if( _branches == null ) {
	    _branches = new LinkedList();
	}

	Branch branch = null;
	BoundaryReceiver prodRcvr = null;
	BoundaryReceiver consRcvr = null;
	Receiver[][] prodRcvrs = null;
	Receiver[][] consRcvrs = null;

	for( int i=0; i < port.getWidth(); i++ ) {
	    if( port.isInput() ) {
		prodRcvrs = port.getReceivers();
		consRcvrs = port.deepGetReceivers();
	    } else if( port.isOutput() ) {
		prodRcvrs = port.getReceivers();
		consRcvrs = port.getRemoteReceivers();
	    } else {
		throw new IllegalActionException("Bad news");
	    }
	    prodRcvr = (BoundaryReceiver)prodRcvrs[i][0];
	    consRcvr = (BoundaryReceiver)consRcvrs[i][0];

	    branch = new Branch( true, prodRcvr, consRcvr, this );
	    _branches.add(branch);
	}
    }

    /**
     */
    public void startBranches() {
        synchronized(this) {
	    if( _branches == null ) {
		return;
	    }
            if( _threadList == null ) {
                _threadList = new LinkedList();
                BranchThread bThread = null;
		Branch branch = null;
                for( int i=0; i < _branches.size(); i++ ) {
		    branch = (Branch)_branches.get(i);
                    if( branch.getGuard() ) {
                        bThread = new BranchThread( branch );
                        _threadList.add(bThread);
                    }
                    // FIXME: should we optimize for a single branch?
		}
            }
                
            Iterator threads = _threadList.iterator();
            BranchThread thread = null;
            Branch branch = null;
            while( threads.hasNext() ) {
                thread = (BranchThread)threads.next();
                branch = thread.getBranch();
                branch.setActive(true);
                thread.start();
            }
        }
    }
    
    /** Terminate abruptly any threads created by this actor. Note that
     *  this method does not allow the threads to terminate gracefully.
     */
    public void terminate() {
        if (_threadList != null) {
            Iterator threads = _threadList.iterator();
            BranchThread bThread = null;
            Branch branch = null;
            BoundaryReceiver bRcvr = null;
            while (threads.hasNext()) {
                bThread = (BranchThread)threads.next();
                branch = bThread.getBranch();
                branch.setActive(false);
                bRcvr = branch.getConsReceiver();
                synchronized(bRcvr) {
                    bRcvr.notifyAll();
                }
                bRcvr = branch.getProdReceiver();
                synchronized(bRcvr) {
                    bRcvr.notifyAll();
                }
            }
        }
    }

    /**
     */
    public void waitOnBranches() {
        try {
            synchronized(this) {
    		while( _branchesActive != 0 ) {
                    this.wait();
        	}
            }
        } catch( InterruptedException e ) {
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

   /** Increase the count of branches that are blocked trying to do
     *  a rendezvous read. If all the enabled branches (for the CIF 
     *  or CDO currently being executed) are blocked, register this 
     *  actor as being blocked.
     */
    protected void _branchBlocked() {
        synchronized(this) {
            _branchesBlocked++;
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

    /** Registers the calling branch as the successful branch. It
     *  reduces the count of active branches, and notifies chooseBranch()
     *  that a branch has succeeded. The chooseBranch() method then
     *  proceeds to terminate the remaining branches. It is called by
     *  the first branch that succeeds with a rendezvous.
     *  @param branch The calling Branch.
     */
    protected void _engagementSucceeded(Branch branch) {
        synchronized(this) {
            if( _maxEngagements < 0 && _maxEngagers < 0 ) {
                return;
            }
            
            if( !_engagements.contains(branch) ) {
                // throw exception here.
            }
            
            if( branch.numberOfEngagements() < _maxEngagements ) {
                branch.engagementWasSuccessful();
            } else {
                // throw exception here.
            }
            
            this.notifyAll();
        }
    }

    /** Decrease the count of branches that are read blocked.
     *  If the actor was previously registered as being blocked, 
     *  register this actor with the director as no longer being 
     *  blocked.
     */
    protected void _branchUnBlocked() {
        synchronized(this) {
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
    protected boolean _canBranchEngage(Branch branch) {
        synchronized(this) {
            if( _maxEngagements < 0 && _maxEngagers < 0 ) {
                return true;
            }
            
            if( _engagements == null ) {
            	_engagements = new LinkedList();
            }
            
            if( _engagements.contains(branch) ) {
                if( branch.numberOfEngagements() < _maxEngagements ) {
                    return true;
                } 
                return false;
            } else if( _engagements.size() < _maxEngagers ) {
            	_engagements.add( branch );
                return true;
            }
            return false;
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
    
    /**
     */
    protected synchronized boolean _isDeadlocked() {
        if( _branchesBlocked == _branchesStarted ) {
            return true;
        } 
        return false;
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
    protected void _disengageBranch(Branch branch) {
        synchronized(this) {
            if( _maxEngagements < 0 && _maxEngagers < 0 ) {
                return;
            }
            if( _engagements.contains(branch) ) {
                if( branch.numberOfEngagements() == 0 ) {
                    _engagements.remove(branch);
                }
            } else {
                // throw exception here.
            }
        }
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
        synchronized(this) {
            _blocked = false;
            _branchesActive = 0;
	    _branchesBlocked = 0;
            _branchesStarted = 0;
            _branchTrying = -1;
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

    // Contains the number of branches that were actually started for
    // the most recent conditional rendezvous.
    private int _branchesStarted = 0;

    // Contains the ID of the branch currently trying to rendezvous. It
    // is -1 if no branch is currently trying.
    private int _branchTrying = -1;

    // Point to the actor who owns this controller object.
    private CompositeActor _parentActor;

    // Threads created by this actor to perform a conditional rendezvous.
    // Need to keep a list of them in case the execution of the model is
    // terminated abruptly.
    private List _threadList = null;
    
    private LinkedList _branches;
    
    private LinkedList _engagements;
    
    private int _maxEngagements = 0;;
    private int _maxEngagers = 0;;
}
