/* 

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (davisj@eecs.berkeley.edu)

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
A BranchController manages the execution of a set of Branches. The
BranchController monitors whether the branches have deadlocked and
whether the iteration of branch executions is over. 

An iteration lasts until no branches are allowed additional communication.
Based on the BranchController parameters, it is possible that the
Branches will be allowed indefinite communication implying that 
iterations will continue into perpetuity. 

If all branches are deadlocked and the iteration is not over, the 
controller will notify its director by calling the director's 
_branchBlocked() method.

The BranchController's method isActive() will return true for the 
duration of the BranchController's life. If isActive() returns false, 
then isIterationOver() will return true.
Once isActive() returns false, then the BranchController will die, 
as will all Branches that it controls, and the BranchController 
reference should be set to null.

There are several methods of this class that reveal internal state. 
The names of these methods all begin with the verb "is" or "are" 
and have boolean return types. These state methods are organized in 
a partial ordering. A method is considered
to be "subordinate" with respect to a "dominant" method if the return value
of the subordinate method is directly impacted by the return value of the
dominant method. All state methods methods are subordinate to isActive().
If isActive()


@author John S. Davis II
@version $Id$
*/

public class BranchController implements Runnable {

    /** Construct a controller in the specified container, which should
        be an actor.
        @param container The parent actor that contains this object.
    */
    public BranchController(CompositeActor container) {
        _parentActor = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public variables		   ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Activate the branches that are managed by this Branch
     *  Controller. This method should be invoked once when
     *  a BranchController first starts the Branches it controls.
     *  Invokation of this method will cause the Branches to
     *  begin iterating. Each iteration will last until all
     *  branch engagements for that iteration are complete. 
     *  To begin a subsequent iteration, call startIteration().
     */
    public void activateBranches() {
        synchronized(this) {
            if( !hasBranches() ) {
                return;
            }
            restart();
            setActive(true);
            LinkedList threadList = new LinkedList();
            BranchThread bThread = null;
            Branch branch = null;
            for( int i=0; i < _branches.size(); i++ ) {
                branch = (Branch)_branches.get(i);
                bThread = new BranchThread( branch );
                threadList.add(bThread);
                // FIXME: should we optimize for a single branch?
            }
                
            Iterator threads = threadList.iterator();
            while( threads.hasNext() ) {
                bThread = (BranchThread)threads.next();
                branch = bThread.getBranch();
                branch.reset();
                branch.newIteration();
                bThread.start();
            }
        }
    }
    
    /** Add branches corresponding to the channels of the port
     *  argument. The port must be contained by the same actor
     *  that contains this controller. If branches corresponding
     *  to the port have already been added to this controller,
     *  then an IllegalActionException will be thrown. If the
     *  input/output polarity of this port does not match that
     *  of ports for whom branches have been previously added
     *  to this controller, then throw an IllegalActionException.
     * @param port The port for which branches will be added to this
     *  controller.
     * @exception IllegalActionException If branches for the
     *  port have been previously added to this controller or
     *  if the port input/output polarity does not match that
     *  of ports for whom branches were previously add to this
     *  controller.
     */
    public void addBranches(IOPort port) throws 
            IllegalActionException {
	if( port.getContainer() != getParent() ) {
	    throw new IllegalActionException("Can not contain "
		    + "a port that is not contained by this "
		    + "BranchController's container.");
	}
        
        if( _ports.contains(port) ) {
            throw new IllegalActionException(port, "This port "
            	    + "is already controlled by this " 
                    + "BranchController");
        }
        // Careful; maintain order of following test in case
        // Java is like C
        if( _hasInputPorts() && !port.isInput() ) {
	    throw new IllegalActionException("BranchControllers "
            	    + "must contain only input ports or only output "
                    + "ports; not both");
        }
        if( _hasOutputPorts() && !port.isOutput() ) {
	    throw new IllegalActionException("BranchControllers "
            	    + "must contain only input ports or only output "
                    + "ports; not both");
        }
        _ports.add(port);

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
		prodRcvrs = port.getInsideReceivers();
		consRcvrs = port.getRemoteReceivers();
	    } else {
		throw new IllegalActionException("Bad news");
	    }
            
	    prodRcvr = (BoundaryReceiver)prodRcvrs[i][0];
	    consRcvr = (BoundaryReceiver)consRcvrs[i][0];

	    branch = new Branch( prodRcvr, consRcvr, this );
	    _branches.add(branch);
	}
    }

    /** Terminate abruptly any threads created by this actor. Note that
     *  this method does not allow the threads to terminate gracefully.
     */
    public synchronized void deactivateBranches() {
	setActive(false);
        Iterator branches = _branches.iterator();
        Branch branch = null;
        BoundaryReceiver bRcvr = null;
        while (branches.hasNext()) {
            branch = (Branch)branches.next();
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
	notifyAll();
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
    public void disengageBranch(Branch branch) {
        synchronized(this) {
            if( _maxEngagements < 0 && _maxEngagers < 0 ) {
                return;
            }
            if( _engagements.contains(branch) ) {
                if( branch.numberOfCompletedEngagements() == 0 ) {
                    _engagements.remove(branch);
                }
            }
        }
    }

    /** End the current iteration of this branch controller. Clear
     *  all engagements that were successful for the iteration that
     *  is ending.
     */
    public synchronized void endIteration() {
        _iterationIsOverCache = true;
        _engagements.clear();
        notifyAll();
    }
    
    /** Register the branch passed as an argument as having 
     *  successfully completed an engagement. If the number
     *  of engagements permitted during an iteration of this
     *  branch controller is bounded, then increment the
     *  number of completed engagements for the branch. If
     *  the number of engagements per iteration is unbounded
     *  then or this controller is not active, then simply 
     *  return.
     * @param branch The Branch with a successful engagement.
     * @exception TerminateBranchException If this controller
     *  is inactive, its iteration is over or the branch is
     *  not currently engaged.
     */
    public void engagementSucceeded(Branch branch) throws 
            TerminateBranchException {
        synchronized(this) {
            if( !isActive() || _iterationIsOverCache ) {
                throw new TerminateBranchException("Branch "
                        + "can not succeed while controller "
                        + "is not active or iteration is over");
            }
            if( _maxEngagements < 0 && _maxEngagers < 0 ) {
                return;
            }
            
            if( !_engagements.contains(branch) ) {
                throw new TerminateBranchException("Branch "
                        + "can not succeed if not previously "
                        + "engaged to controller.");
            }
            
            if( branch.numberOfCompletedEngagements() < _maxEngagements ) {
                branch.completeEngagement();
            } else {
                throw new TerminateBranchException("Branch "
                        + "can not succeed if it already has "
                        + "more successful engagements than "
                        + "permitted.");
            }
            notifyAll();
        }
    }

    /** Return the list of branches controlled by this controller.
     *  @return The list of branches controlled by this controller.
     */
    public LinkedList getBranchList() {
        return _branches;
    }
    
    /** Return the CompositeActor that creates the branch 
     *  and owns this controller.
     *  @return The CompositeActor that owns this controller.
     */
    public CompositeActor getParent() {
        return _parentActor;
    }

    /** Return true if this controller controls one or more branches;
     *  return false otherwise.
     * @return True if this controller controls one or more branche;
     *  return false otherwise.
     */
    public boolean hasBranches() {
        return _branches.size() > 0;
    }
    
    /** Return true if this controller is active; return false 
     *  otherwise.
     * @return True if this controller is active; false otherwise.
     */
    public boolean isActive() {
	return _isActive;
    }

    /** Return true if all of the branches controlled by this
     *  controller are stopped and at least one of the branches
     *  is blocked or if this controller has no branches. Stopped 
     *  branches are defined as either being blocked or waiting 
     *  for the next iteration. 
     * @return True if all branches controlled by this 
     *  controller are stopped and at least one branch is
     *  blocked or if there are no branches; return false otherwise.
     */
    public boolean isBlocked() {
        if( !hasBranches() ) {
            return true;
        }
        if( _branchesBlocked >= _branches.size() ) {
            if( _branchesBlocked > 0 ) {
                return true;
            }
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
    public boolean isEngagementEnabled(Branch branch) {
        synchronized(this) {
	    if( _iterationIsOverCache || !isActive() ) {
		return false;
	    }
            if( _maxEngagements < 0 && _maxEngagers < 0 ) {
                branch.beginEngagement();
                return true;
            }
            
            if( _engagements.contains(branch) ) {
                if( branch.numberOfCompletedEngagements() < _maxEngagements ) {
                    branch.beginEngagement();
                    return true;
                } 
                return false;
            } else if( _engagements.size() < _maxEngagers ) {
                if( branch.numberOfCompletedEngagements() < _maxEngagements ) {
            	    _engagements.add( branch );
                    branch.beginEngagement();
                    return true;
                } 
                return false ;
            }
            return false;
        }
    }

    /** Return true if this controller is not in the midst of an
     *  iteration. This controller is not in an iteration if it
     *  is not active or if all engagement attempts are complete.
     * @return True if this controller is not active or if all
     *  engagements are complete.
     */
    public synchronized boolean isIterationOver() {
	if( !isActive() ) {
	    return true;
	} else if( _iterationIsOverCache ) {
	    return true;
        }
	if( _engagements.size() == _maxEngagers ) {
	    Iterator engagements = _engagements.iterator();
	    Branch branch = null;
	    while( engagements.hasNext() ) {
		branch = (Branch)engagements.next();
		if( branch.numberOfCompletedEngagements() 
			< _maxEngagements ) {
		    return false;
		}
	    }
	    return true;
	}
	return false;
    }

    /** Restart this controller by resetting the branches that
     *  it controls and setting flags so engagements can take
     *  place. This method is synchronized and will notify any 
     *  threads that are synchronized to this object.
     */
    public synchronized void restart() {
	_iterationIsOverCache = true;

	_engagements.clear();
	_branchesBlocked = 0;

	Branch branch = null;
	Iterator branches = _branches.iterator();
	while( branches.hasNext() ) {
	    branch = (Branch)branches.next();
	    branch.reset();
	}
	_iterationIsOverCache = false;

	notifyAll();
    }

    /**
     */
    public void run() {
	synchronized(this) {
	    try {
		activateBranches();
		while( isActive() ) {
		    while( !isIterationOver() ) {
			wait();

			if( isBlocked() && !isIterationOver() ) {
			    while( isBlocked() && 
				    !isIterationOver() ) {
				_getDirector()._controllerBlocked(this);
				wait();
			    }
			    _getDirector()._controllerUnBlocked(this);
			}
		    }

		    if( isIterationOver() && isActive() ) {
			while( isIterationOver() && isActive() ) {
			    _getDirector()._controllerBlocked(this);
			    wait();
			}
			_getDirector()._controllerUnBlocked(this);
		    }
		}
	    } catch( InterruptedException e ) {
		// Do something
	    }
	}
    }

    /**
     */
    public void setActive(boolean active) {
	_isActive = active;
    }

    /**
     */
    public void setMaximumEngagements(int maxEngagements) {
	_maxEngagements = maxEngagements;
    }

    /**
     */
    public void setMaximumEngagers(int maxEngagers) {
	_maxEngagers = maxEngagers;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Increase the count of branches that are blocked trying to do
     *  a rendezvous read. If all the enabled branches (for the CIF 
     *  or CDO currently being executed) are blocked, register this 
     *  actor as being blocked.
     */
    protected void _branchBlocked(ProcessReceiver rcvr) {
        synchronized(this) {
            _branchesBlocked++;
	    _blockedReceivers.addFirst(rcvr);
	    notifyAll();
        }
    }

    /** Decrease the count of branches that are read blocked.
     *  If the actor was previously registered as being blocked, 
     *  register this actor with the director as no longer being 
     *  blocked.
     */
    protected void _branchUnBlocked(ProcessReceiver rcvr) {
        synchronized(this) {
            if( _branchesBlocked > 0 ) {
                _branchesBlocked--;
            }
	    _blockedReceivers.remove(rcvr);
	    notifyAll();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the director that controlls the execution of its parent actor.
     */
    private ProcessDirector _getDirector() {
        try {
	    return  (ProcessDirector)_parentActor.getDirector();
        } catch (NullPointerException ex) {
            // If a thread has a reference to a receiver with no director it
            // is an error so terminate the process.
	    throw new TerminateProcessException("Error.");
	}
    }

    /**
     */
    private boolean _hasInputPorts() {
    	if( _ports.size() == 0 ) {
            return false;
        }
        Iterator ports = _ports.iterator();
        while( ports.hasNext() ) {
            IOPort port = (IOPort)ports.next();
            return port.isInput();
        }
        return false;
    }
    
    /**
     */
    private boolean _hasOutputPorts() {
    	if( _ports.size() == 0 ) {
            return false;
        }
        Iterator ports = _ports.iterator();
        while( ports.hasNext() ) {
            IOPort port = (IOPort)ports.next();
            return port.isOutput();
        }
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The number of branches that are blocked
    private int _branchesBlocked = 0;
    
    // The CompositeActor who owns this controller object.
    private CompositeActor _parentActor;

    private LinkedList _branches = new LinkedList(); 
    private LinkedList _ports = new LinkedList();
    private LinkedList _engagements = new LinkedList();
    private LinkedList _blockedReceivers = new LinkedList();
    
    private int _maxEngagements = -1;
    private int _maxEngagers = -1;

    private boolean _iterationIsOverCache = true;
    private boolean _isActive = false;

}
