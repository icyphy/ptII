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

public class Branch implements Runnable {

    /** Construct a Branch object.
     * @deprecated Use this constructor for testing purposes only.
     */
    // public Branch() throws 
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

    /** FIXME
     */
    public void wrapup() throws IllegalActionException {
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

    /** Transfer a single token between the producer receiver and 
     *  the consumer receiver. If a TerminateBranchException is
     *  thrown, then reset this receiver and return. 
     *  FIXME: Can we optimize this?
     */
    public void run() {
        try {
            setActive(true);
            while( isActive() ) {
            	Token token = _prodRcvr.get(this); 
                _consRcvr.put(token, this);
            }
        } catch( TerminateBranchException e ) {
	    // Iteration is over
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

}
