/* A BoundaryReceiver that stores tokens via a mailbox.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow

*/

package ptolemy.actor.process;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// MailboxBoundaryReceiver
/**
A MailboxBoundaryReceiver stores tokens via a mailbox.

@author John S. Davis II
@version $Id$

*/
public class MailboxBoundaryReceiver extends Mailbox 
	implements BoundaryReceiver {

    /** Construct an empty MailboxBoundaryReceiver with no container.
     */
    public MailboxBoundaryReceiver() {
	super();
	_boundaryDetector = new BoundaryDetector(this);
    }
    
    /** Construct an empty MailboxBoundaryReceiver with the specified
     *  container.
     */
    public MailboxBoundaryReceiver(IOPort container) {
        super(container);
	_boundaryDetector = new BoundaryDetector(this);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this receiver is read blocked; return false
     *  otherwise.
     */
    public boolean isReadBlocked() {
        return _readBlock;
    }
    
    /** Return true if this receiver is write blocked; return false
     *  otherwise.
     */
    public boolean isWriteBlocked() {
        return _writeBlock;
    }
    
    /** Get a token from the mailbox receiver and specify a null
     *  Branch to control the execution of this method. 
     * @return The token contained by this receiver.
     */
    public Token get() {
        return get(null);
    }
    
    /** Get a token from the mailbox receiver and specify a Branch
     *  to control the execution of this method. If the controlling
     *  branch becomes inactive during the execution of this method,
     *  then throw a TerminateBranchException. If this receiver is
     *  terminated during the execution of this method, then throw
     *  a TerminateProcessException.
     * @param controllingBranch The Branch controlling execution of
     *  this method.
     * @return The token contained by this receiver.
     */
    public Token get(Branch branch) {
        Workspace workspace = getContainer().workspace();
        Token result = null;
        synchronized(this) {
            if( !_terminate && !hasToken() ) {
                _readBlock = true;
                prepareToBlock(branch);
                while( _readBlock && !_terminate ) {
                    checkIfBranchIterationIsOver(branch);
                    workspace.wait(this);
                }
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else {
                checkIfBranchIterationIsOver(branch);
                waitForBranchPermission(branch);
            	result = super.get();
                if( _writeBlock ) {
                    wakeUpBlockedPartner();
                    _writeBlock = false;
                    notifyAll();
                }
            	return result;
            }
        }
    }
    
    /**
     */
    public synchronized void wakeUpBlockedPartner() {
        if( _otherBranch != null ) {
            // _otherBranch.registerRcvrUnBlocked(this);
        } else {
            ProcessDirector director = ((ProcessDirector)((Actor)
        	    (getContainer().getContainer())).getDirector());
            director._actorUnBlocked(this);
            
        }
        notifyAll();
    }
    
    /**
     */
    public synchronized void waitForBranchPermission(Branch branch) 
    	    throws TerminateBranchException {
        if( branch == null ) {
            return;
        }
        
	/*
        Workspace workspace = getContainer().workspace();
        while( !branch.isBranchPermitted() && !branch.isIterationOver() ) {
            branch.registerRcvrBlocked(this);
            workspace.wait(this);
        }
        branch.registerRcvrUnBlocked(this);
        checkIfBranchIterationIsOver(branch);
	*/
    }
            
    /**
     */
    public synchronized void checkIfBranchIterationIsOver(Branch branch) 
    	    throws TerminateBranchException {
        if( branch != null ) {
	    /*
            if( branch.isIterationOver() ) {
                throw new TerminateBranchException("The current "
                        + "iteration has ended.");
            }
	    */
        }
    }
    
    /**
     */
    public synchronized void prepareToBlock(Branch branch) 
            throws TerminateBranchException {
        if( branch != null ) {
	    /*
            branch.registerRcvrBlocked(this);
            _otherBranch = branch;
	    */
        } else {
            ProcessDirector director = ((ProcessDirector)((Actor)
        	    (getContainer().getContainer())).getDirector());
            director._actorBlocked(this);
            _otherBranch = branch;
        }
    }
            
    /**
     */
    public boolean isConnectedToBoundary() {
	return _boundaryDetector.isConnectedToBoundary();
    }

    /**
     */
    public boolean isConnectedToBoundaryInside() {
	return _boundaryDetector.isConnectedToBoundaryInside();
    }

    /**
     */
    public boolean isConnectedToBoundaryOutside() {
	return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /** This class serves as an example of a ConsumerReceiver and
     *  hence this method returns true;
     */
    public boolean isConsumerReceiver() {
        if( isConnectedToBoundary() ) {
             return true;
        }
    	return false;
    }

    /**
     */
    public boolean isInsideBoundary() {
	return _boundaryDetector.isInsideBoundary();
    }

    /**
     */
    public boolean isOutsideBoundary() {
	return _boundaryDetector.isOutsideBoundary();
    }

    /** This class serves as an example of a ProducerReceiver and
     *  hence this method returns true;
     */
    public boolean isProducerReceiver() {
        if( isOutsideBoundary() || isInsideBoundary() ) {
            return true;
        }
    	return false;
    }

    /** Put a token into the mailbox receiver and specify a Branch
     *  to control the execution of this method. If the controlling
     *  branch becomes inactive during the execution of this method,
     *  then throw a TerminateBranchException. If this receiver is
     *  terminated during the execution of this method, then throw
     *  a TerminateProcessException.
     * @param token The token being placed in this receiver.
     * @param controllingBranch The Branch controlling execution of
     *  this method.
     */
    public void put(Token token, Branch branch) {
        Workspace workspace = getContainer().workspace();
        synchronized(this) {
            if( !_terminate && !hasRoom() ) {
                _writeBlock = true;
                prepareToBlock(branch);
                while( _writeBlock && !_terminate ) {
                    checkIfBranchIterationIsOver(branch);
                    workspace.wait(this);
                }
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else {
                
                checkIfBranchIterationIsOver(branch);
                waitForBranchPermission(branch);
                
                super.put(token);
                if( _readBlock ) {
                    wakeUpBlockedPartner();
                    _readBlock = false;
                    notifyAll();
                }
            }
        }
    }
            
    /** Put a token into the mailbox receiver and specify a null
     *  Branch to control the execution of this method. 
     */
    public void put(Token token) {
        put(token, null);
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

    /** Set a local flag requesting that the simulation be finished.
     */
    public synchronized void requestFinish() {
    	_terminate = true;
        notifyAll();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////
    
    private boolean _terminate = false;
    private boolean _readBlock = false;
    private boolean _writeBlock = false;
    
    private Branch _otherBranch = null;
    private BoundaryDetector _boundaryDetector;

}
