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

import ptolemy.actor.*;
import ptolemy.data.Token;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Branch
/**
A Branch serves as a proxy for a BranchController by getting or
putting tokens in to the BoundaryReceiver to which it is assigned.
The execution of a Branch is controlled by a BranchController in
that a BranchController can deny a Branch permission to get or
put data in the receiver. 

An iteration of a Branch lasts until the BranchController notifies
the Branch that the current iteration is done. Such notification 
occurs via a TerminateBranchException that is thrown as soon as a
Branch determines that isIterationOver() returns true. Until an
iteration is over, a Branch will indefinitely attempt to get and
put data to the receivers it controls. This may result in the
Branch blocking on a read or write, only to be awakened by a
TerminateBranchException.

Once an iteration has ended, a Branch will immediately begin a new
iteration unless isActive() returns false. The method isActive()
will return true for the duration of the Branch's life. If 
isActive() returns false, then isIterationOver() will return true.
Once isActive() returns false, then the Branch will die, as will
the thread (BranchThread) controlling the Branch, and the Branch
reference should be set to null.


@author John S. Davis II
@version $Id$
*/

public class Branch {

    /** Construct a Branch object.
     */
    public Branch(BoundaryReceiver prodRcvr, BoundaryReceiver consRcvr, 
	    BranchController cntlr) throws IllegalActionException {
        _controller = cntlr;
        
        Receiver[][] receivers;
        BoundaryReceiver receiver;
        
        if( !prodRcvr.isProducerReceiver() ) {
            throw new IllegalActionException("Not producer "
            	    + "receiver");
        }
	_prodRcvr = prodRcvr;
        
        if( !_consRcvr.isConsumerReceiver() ) {
            throw new IllegalActionException("Not consumer "
            	    + "receiver");
        }
	_consRcvr = consRcvr;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void beginEngagement() {
	if( !_currentlyEngaged ) {
	    _currentlyEngaged = true;
	} else {
	    throw new TerminateBranchException("Can not begin "
		    + "an engagement if currently engaged.");
	}
    }

    /** 
     */
    public void completeEngagement() {
	if( _currentlyEngaged ) {
	    _completedEngagements++;
	    _currentlyEngaged = false;
	} else {
	    throw new TerminateBranchException("Can not complete "
		    + "an engagement if not currently engaged.");
	}
    }

    /**
     */
    public int numberOfCompletedEngagements() {
        return _completedEngagements;
    }
    
    /** Return the Consumer BoundaryReceiver that this branch puts data into.
     *  @return The Consumer BoundaryReceiver that this branch puts data into.
     */
    public BoundaryReceiver getConsReceiver() {
        return _consRcvr;
    }

    /** Return the Producer BoundaryReceiver that this branch gets data from.
     *  @return The Producer BoundaryReceiver that this branch gets data from.
     */
    public BoundaryReceiver getProdReceiver() {
        return _prodRcvr;
    }

    /** Boolean indicating if this branch is still alive. If it is false, it
     *  indicates another conditional branch was able to rendezvous before
     *  this branch, and this branch should stop trying to rendezvous with
     *  its receiver and terminate. If it is true, the branch should
     *  continue trying to rendezvous.
     *  @return True if this branch is still alive.
     */
    public boolean isActive() {
        return _active;
    }

    /** 
     */
    public boolean isBranchPermitted() {
        if( _controller.canBranchEngage(this) ) {
            return true;
        }
    	return false;
    }

    /** Register that the receiver controlled by this branch
     *  is blocked.
     */
    public void registerRcvrBlocked() {
    	if( !_rcvrBlocked ) {
            _controller._branchBlocked();
        }
    }

    /** Register that the receiver controlled by this branch
     *  is no longer blocked.
     */
    public void registerRcvrUnBlocked() {
    	if( _rcvrBlocked ) {
            _controller._branchUnBlocked();
        }
    }

    /**
     */
    protected void reset() {
	_active = true;
	_rcvrBlocked = false;
	_completedEngagements = 0;
	_currentlyEngaged = false;
	endIteration(true);
    }

    /**
     */
    public boolean isIterationOver() {
	if( !isActive() ) {
	    return false;
	}
	return _isIterationOver;
    }

    /**
     */
    public synchronized void endIteration(boolean endIteration) {
	_isIterationOver = endIteration;
	// FIXME: Here I wake up the branch; What about the receiver?
	notifyAll();
    }

    /** 
     * FIXME: Can we optimize this?
     */
    public void transferTokens() {
        try {
	    // beginEngagement();
            Token token = _prodRcvr.get(this);
            _consRcvr.put(token, this);
	    // completeEngagement();
        } catch( TerminateBranchException e ) {
	    // Iteration is over
	    reset();
            return;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    package friendly methods               ////

    //////////////////////////////////////////////////////////////////
    ////                       protected methods                  ////

    /** Set a flag indicating this branch should fail.
     *  @param value Boolean indicating whether this branch is still alive.
     */
    protected void setActive(boolean value) {
        _active = value;
    }

    /** 
    protected void setStopped(boolean value) {
        _stopped = value;
    }
     */

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Has another branch successfully rendezvoused? If so, then _active
    // is set to false. Otherwise, this branch still can potentially
    // rendezvous. _active remains true until it is no longer possible
    // for this branch to successfully rendezvous.
    private boolean _active = true;

    // The controller of this thread is trying to perform a conditional
    // rendezvous for.
    private BranchController _controller;
    private BoundaryReceiver _prodRcvr;
    private BoundaryReceiver _consRcvr;
    
    private boolean _rcvrBlocked = false;
    private int _completedEngagements = 0;
    private boolean _currentlyEngaged = false;
    // private boolean _stopped = false;
    private boolean _isIterationOver = false;

}
