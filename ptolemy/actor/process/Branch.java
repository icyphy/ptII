/* A Branch serves as a proxy for a BranchController by transfer tokens
between the producer receiver and the consumer receiver to which it
is assigned. 

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
A Branch serves as a proxy for a BranchController by transfer tokens
between the producer receiver and the consumer receiver to which it
is assigned. The execution of a Branch is controlled by a 
BranchController in that a BranchController can deny a Branch permission 
to get or put data in the receiver. 

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
     * @deprecated Use this constructor for testing purposes only.
     */
    public Branch(BranchController cntlr) throws 
    	    IllegalActionException {
        _controller = cntlr;
    }
 
    /** Construct a Branch object.
     */
    public Branch(BoundaryReceiver prodRcvr, BoundaryReceiver consRcvr, 
	    BranchController cntlr) throws IllegalActionException {
        _controller = cntlr;
        
        if( prodRcvr == null || consRcvr == null ) {
            throw new IllegalActionException("The boundary "
            	    + "receivers of this branch are null.");
        }
        if( !prodRcvr.isProducerReceiver() ) {
            throw new IllegalActionException("Not producer "
            	    + "receiver");
        }
	_prodRcvr = prodRcvr;
        
        if( !consRcvr.isConsumerReceiver() ) {
	    String name = ((Nameable)consRcvr.getContainer()).getName();
            throw new IllegalActionException("Receiver: " + name + 
		    " Not consumer receiver");
        }
	_consRcvr = consRcvr;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Begin an engagement with the controller of this Branch.
     *  This method should be called prior to attempts to 
     *  transfer a token between a pair of receivers.
     */
    public void beginEngagement() {
	if( !_currentlyEngaged ) {
	    _currentlyEngaged = true;
	}
    }

    /** Complete an engagement with the controller of this Branch.
     *  This method should be called at the completion of a token
     *  transfer between a pair of receivers. Calls to this method
     *  must be made in an alternating fashion with beginEngagement().
     *  If this method is called successively without an intervening
     *  call to beginEngagement(), a TerminateBranchException will
     *  be thrown.
     * @exception TerminateBranchException If this method is called
     *  is called successively without an intervening call to
     *  beginEngagement.
     * @see #beginEngagement
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

    /** End the current iteration of this branch. Notify both the 
     *  producer and consumer receivers that the iteration is over.
     */
    public synchronized void endIteration() {
	_iterationIsOverCache = true;
        BoundaryReceiver rcvr = null;
        rcvr = getProdReceiver();
        synchronized(rcvr) {
            rcvr.notifyAll();
        }
        rcvr = getConsReceiver();
        synchronized(rcvr) {
            rcvr.notifyAll();
        }
        notifyAll();
    }

    /** Return the consumer receiver that this branch puts data into.
     *  A consumer receiver is defined as being a receiver whose  
     *  containing port is connected to a boundary port.
     * @return The consumer receiver that this branch puts data into.
     * @see ptolemy.actor.process.BoundaryDetector
     */
    public BoundaryReceiver getConsReceiver() {
        return _consRcvr;
    }

    /** Return the producer receiver that this branch gets data from.
     *  A producer receiver is defined as being a receiver that is
     *  contained in a boundary port.
     * @return The producer receiver that this branch gets data from.
     * @see ptolemy.actor.process.BoundaryDetector
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

    /** Return true if this branch is permitted to begin an 
     *  engagement with the branch controller; return false
     *  otherwise. During an engagement a branch is able
     *  transfer a token between its producer receiver and
     *  its consumer receiver.
     * @return True if this branch may engage with its
     *  branch controller.
     */
    public boolean isBranchPermitted() {
        try {
            if( !_prodRcvr.hasRoom() ) {
                return false;
            }
        } catch( IllegalActionException e ) {
            // FIXME
            // This should not be thrown but for 
            // now let's ignore.
        }
        if( _controller.isEngagementEnabled(this) ) {
            return true;
        }
    	return false;
    }

    /** Return true if this branch has been informed that the
     *  current iteration is over or if this branch is no 
     *  longer active. 
     * @return True if this branch is not active or if its
     *  iteration is over.
     */
    public boolean isIterationOver() {
	if( !isActive() ) {
	    return true;
	}
	return _iterationIsOverCache;
    }

    /** Return the number of engagements that have been
     *  successfully completed by this branch.
     * @return The number of successful engagements completed
     *  by this branch.
     */
    public int numberOfCompletedEngagements() {
        return _completedEngagements;
    }
    
    /** Register that the receiver controlled by this branch
     *  is blocked.
     */
    public void registerRcvrBlocked(ProcessReceiver rcvr) {
    	if( !_rcvrBlocked ) {
    	    _rcvrBlocked = true;
            _controller._branchBlocked(rcvr);
        }
    }

    /** Register that the receiver controlled by this branch
     *  is no longer blocked.
     */
    public void registerRcvrUnBlocked(ProcessReceiver rcvr) {
    	if( _rcvrBlocked ) {
    	    _rcvrBlocked = false;
            _controller._branchUnBlocked(rcvr);
        }
    }

    /** Reset this branch so that it may begin a new iteration.
     *  End the current iteration and wake up the consumer and
     *  producer receivers. 
     */
    public void reset() {
	_active = true;
	_rcvrBlocked = false;
	_completedEngagements = 0;
	_currentlyEngaged = false;
	endIteration();
    }

    /** Transfer a single token between the producer receiver and 
     *  the consumer receiver. If a TerminateBranchException is
     *  thrown, then reset this receiver and return. 
     *  FIXME: Can we optimize this?
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
    
    //////////////////////////////////////////////////////////////////
    ////                       protected methods                  ////

    /** Set a flag indicating this branch should fail.
     *  @param value Boolean indicating whether this branch is still alive.
     */
    public void setActive(boolean value) {
        _active = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Has another branch successfully rendezvoused? If so, then _active
    // is set to false. Otherwise, this branch still can potentially
    // rendezvous. _active remains true until it is no longer possible
    // for this branch to successfully rendezvous.
    private boolean _active = false;

    // The controller of this thread is trying to perform a conditional
    // rendezvous for.
    private BranchController _controller;
    private BoundaryReceiver _prodRcvr;
    private BoundaryReceiver _consRcvr;
    
    private boolean _rcvrBlocked = false;
    private int _completedEngagements = 0;
    private boolean _currentlyEngaged = false;
    private boolean _iterationIsOverCache = false;

}
