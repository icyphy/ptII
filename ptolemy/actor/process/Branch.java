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

@ProposedRating Green (nsmyth@eecs.berkeley.edu)
@AcceptedRating Green (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.actor.process;

import ptolemy.actor.*;
import ptolemy.data.Token;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Branch
/**

@author  John S. Davis II
@version $Id$
*/

public abstract class Branch {

    /** Create a guarded communication statement. 
    public Branch(boolean guard, int branchID, BranchController cntlr)
            throws IllegalActionException {
     */
    public Branch(boolean guard, int prodChannel, int consChannel, 
    	    int branchID, IOPort prodPort, IOPort consPort, 
            BranchController cntlr) throws IllegalActionException {
        _guard = guard;
        _branchID = branchID;
        _controller = cntlr;
        
        Receiver[][] receivers;
        BoundaryReceiver receiver;
        
        receivers = prodPort.getReceivers();
        receiver = (BoundaryReceiver)receivers[prodChannel][0];
        if( !receiver.isProducerReceiver() ) {
            throw new IllegalActionException(prodPort, "Not producer "
            	    + "receiver");
        }
        _prodRcvr = (BoundaryReceiver)receivers[prodChannel][0];
        
        receivers = consPort.getRemoteReceivers();
        receiver = (BoundaryReceiver)receivers[consChannel][0];
        if( !receiver.isConsumerReceiver() ) {
            throw new IllegalActionException(consPort, "Not consumer "
            	    + "receiver");
        }
        _consRcvr = (BoundaryReceiver)receivers[consChannel][0];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void branchWasSuccessful() {
    }

    /** Return the controller that manges conditional rendezvous for this
     *  branch when performing a CIF or CDO.
     *  @return The controller that manages conditional rendezvous for
     *  this branch.
     */
    public BranchController getController() {
        return _controller;
    }

    /** Returns the guard for this guarded communication statement.
     *  If it is true the branch is said to be enabled.
     *  @return True if the branch is  enabled.
     */
    public boolean getGuard() {
        return _guard;
    }

    /** Returns the identification number of this branch(according to its
     *  controller).
     *  @return The identification number of this branch.
     */
    public int getID() {
        return _branchID;
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
    public boolean isBranchCommitted() {
        if( _controller._canBranchContinue( getID() ) ) {
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
    public void transferTokens() {
        try {
            Token token = null;
            token = _prodRcvr.get(this);
            _consRcvr.put(token, this);
        } catch( TerminateBranchException e ) {
            // Do nothing
            return;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    package friendly methods               ////

    /** Set a flag indicating this branch should fail.
     *  @param value Boolean indicating whether this branch is still alive.
     */
    void setActive(boolean value) {
        _active = value;
    }

    //////////////////////////////////////////////////////////////////
    ////                       protected methods                  ////

    /** Set the BoundaryReceiver this branch is trying to rendezvous with.
     *  This method should only be called from derived classes.
     *  @param rec The BoundaryReceiver this branch is trying to 
     *   rendezvous with.
    protected void setReceiver(BoundaryReceiver rec) {
        _receiver = rec;
    }
     */

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    // The guard for this guarded communication statement.
    protected boolean _guard;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The identification number of this branch (according to its controller)
    private int _branchID;

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

}
