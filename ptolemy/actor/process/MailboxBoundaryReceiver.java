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

    /** Associated with Atomic Get/Composite Put
     *
     *  Get a token from the mailbox receiver and specify a Branch
     *  to control the execution of this method. If the controlling
     *  branch becomes inactive during the execution of this method,
     *  then throw a TerminateBranchException. If this receiver is
     *  terminated during the execution of this method, then throw
     *  a TerminateProcessException.
     * @param controllingBranch The Branch controlling execution of
     *  this method.
     * @return The token contained by this receiver.
     */
    public Token consumerGet() throws TerminateBranchException {
        Workspace workspace = getContainer().workspace();
        ProcessDirector director = ((ProcessDirector)((Actor)
        	(getContainer().getContainer())).getDirector());
        
        synchronized(this) {
            if( !_terminate && !hasToken() ) {
            	director._actorReadBlocked(true);
                _readPending = true;
                while( _readPending && !_terminate ) {
                    workspace.wait(this);
                }
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else {
                if( _writePending ) {
                    _otherBranch.registerRcvrUnBlocked();
                    _writePending = false;
                    notifyAll();
                }
            	return super.get();
            }
        }
    }

    /** Throw a TerminateBranchException. This method should never
     *  be called. Instead, calls should be made to get(Branch).
     */
    public Token get() {
        Workspace workspace = getContainer().workspace();
        ProcessDirector director = ((ProcessDirector)((Actor)
        	(getContainer().getContainer())).getDirector());
        
        synchronized(this) {
            if( !_terminate && !hasToken() ) {
            	director._actorReadBlocked(true);
                _readPending = true;
                while( _readPending && !_terminate ) {
                    workspace.wait(this);
                }
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else {
                if( _writePending ) {
                    director._actorWriteUnBlocked();
                    _writePending = false;
                    notifyAll();
                }
            	return super.get();
            }
        }
    }
            
    /** Remove and return the oldest token from the FIFO queue contained
     *  in the receiver. Terminate the calling process by throwing a
     *  TerminateProcessException if requested.
     *  Otherwise, if the FIFO queue is empty, then suspend the calling
     *  process and inform the director of the same.
     *  If a new token becomes available to the FIFO queue, then resume the
     *  suspended process.
     *  If the queue was not empty, or on availability of a new token (calling
     *  process was suspended), take the oldest token from the FIFO queue.
     *  Check if any process is blocked on a write to this
     *  receiver. If a process is indeed blocked, then unblock the
     *  process, and inform the director of the same. 
     *  Otherwise return.
     *  @return The oldest Token read from the queue
     */
    public Token get(Branch branch) {
	if( isInsideBoundary() ) {
	    if( isConnectedToBoundary() ) {
		return consumerProducerGet(branch);
	    }
	    return producerGet(branch);
	} else if( isOutsideBoundary() ) {
	    if( isConnectedToBoundary() ) {
		return consumerProducerGet(branch);
	    }
	    return producerGet(branch);
	} else if( isConnectedToBoundary() ) {
	    return consumerGet();
	}
	return get();

    }

    /** Remove and return the oldest token from the FIFO queue contained
     *  in the receiver. Terminate the calling process by throwing a
     *  TerminateProcessException if requested.
     *  Otherwise, if the FIFO queue is empty, then suspend the calling
     *  process and inform the director of the same.
     *  If a new token becomes available to the FIFO queue, then resume the
     *  suspended process.
     *  If the queue was not empty, or on availability of a new token (calling
     *  process was suspended), take the oldest token from the FIFO queue.
     *  Check if any process is blocked on a write to this
     *  receiver. If a process is indeed blocked, then unblock the
     *  process, and inform the director of the same. 
     *  Otherwise return.
     *  @return The oldest Token read from the queue
     */
    public void put(Token token, Branch branch) {
	if( isInsideBoundary() ) {
	    if( isConnectedToBoundary() ) {
		consumerProducerPut(token, branch);
	    }
	    producerPut(token);
	} else if( isOutsideBoundary() ) {
	    if( isConnectedToBoundary() ) {
		consumerProducerPut(token, branch);
	    }
	    producerPut(token);
	} else if( isConnectedToBoundary() ) {
	    consumerPut(token, branch);
	}
	put(token);

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
    public Token consumerProducerGet(Branch controllingBranch) throws 
    	    TerminateBranchException {
        Workspace workspace = getContainer().workspace();
        Branch brnch = controllingBranch;
        
        synchronized(this) {
            if( !_terminate && !hasToken() && !brnch.isIterationOver() ) {
            	brnch.registerRcvrBlocked();
                _otherBranch = brnch;
                _readPending = true;
                while( _readPending && !_terminate && !brnch.isIterationOver() ) {
                    workspace.wait(this);
                }
            	brnch.registerRcvrUnBlocked();
                _readPending = false;
                _otherBranch = null;
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else if( brnch.isIterationOver() ) {
            	throw new TerminateBranchException("");
            } else {
                //
                // Get Permission From Controlling Branch
                //
                while( !brnch.isBranchPermitted() && !brnch.isIterationOver() ) {
                    brnch.registerRcvrBlocked();
                    workspace.wait(this);
                }
                brnch.registerRcvrUnBlocked();
                if( brnch.isIterationOver() ) {
                    throw new TerminateBranchException("");
                }
                
                if( _writePending ) {
                    _otherBranch.registerRcvrUnBlocked();
                    _writePending = false;
                    notifyAll();
                }
                
                //
                // Inform The Controlling Branch Of Success
                //
            	brnch.completeEngagement();
            	return super.get();
            }
        }
    }

    /**
     */
    public boolean isConnectedToBoundary() {
	return _boundaryDetector.isConnectedToBoundary();
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

    /** Associated with Composite Get/Atomic Put
     *  Get a token from the mailbox receiver and specify a Branch
     *  to control the execution of this method. If the controlling
     *  branch becomes inactive during the execution of this method,
     *  then throw a TerminateBranchException. If this receiver is
     *  terminated during the execution of this method, then throw
     *  a TerminateProcessException.
     * @param controllingBranch The Branch controlling execution of
     *  this method.
     * @return The token contained by this receiver.
     */
    public Token producerGet(Branch controllingBranch) throws 
    	    TerminateBranchException {
        Workspace workspace = getContainer().workspace();
        ProcessDirector director = ((ProcessDirector)((Actor)
        	(getContainer().getContainer())).getDirector());
        Branch brnch = controllingBranch;
        
        synchronized(this) {
            if( !_terminate && !hasToken() && !brnch.isIterationOver() ) {
            	brnch.registerRcvrBlocked();
                _otherBranch = brnch;
                _readPending = true;
                while( _readPending && !_terminate && !brnch.isIterationOver() ) {
                    workspace.wait(this);
                }
            	brnch.registerRcvrUnBlocked();
                _otherBranch = null;
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else if( brnch.isIterationOver() ) {
            	throw new TerminateBranchException("");
            } else {
                //
                // Get Permission From Controlling Branch
                //
                while( !brnch.isBranchPermitted() && !brnch.isIterationOver() ) {
                    brnch.registerRcvrBlocked();
                    try {
                        wait();
                    } catch( InterruptedException e ) {
                        throw new TerminateProcessException(
                        	"InterruptedException thrown");
                    }
                }
                brnch.registerRcvrUnBlocked();
                if( brnch.isIterationOver() ) {
                    throw new TerminateBranchException("");
                }
                
                if( _writePending ) {
                    director._actorWriteUnBlocked();
                    _writePending = false;
                    notifyAll();
                }
                
                //
                // Inform The Controlling Branch Of Success
                //
            	brnch.completeEngagement();
            	return super.get();
            }
        }
    }

    /** Throw a TerminateBranchException. This method should never
     *  be called. Instead, calls should be made to put(Token, Branch).
     */
    public void put(Token token) {
        ProcessDirector director = ((ProcessDirector)((Actor)
        	(getContainer().getContainer())).getDirector());
        
        synchronized(this) {
            if( !_terminate && !hasRoom() ) {
                _writePending = true;
                while( _writePending && !_terminate ) {
                    director._actorWriteBlocked();
                    try {
                        wait();
                    } catch( InterruptedException e ) {
                        throw new TerminateProcessException(
                        	"InterruptedException thrown");
                    }
                }
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else {
                super.put(token);
                if( _readPending ) {
                    director._actorReadUnBlocked(true);
                    _readPending = false;
                    notifyAll();
                }
            }
        }
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
    public void consumerPut(Token token, Branch controllingBranch) throws 
    	    TerminateBranchException {
        Workspace workspace = getContainer().workspace();
        ProcessDirector director = ((ProcessDirector)((Actor)
        	(getContainer().getContainer())).getDirector());
        Branch brnch = controllingBranch;
        
        synchronized(this) {
            if( !_terminate && !hasRoom() && !brnch.isIterationOver() ) {
            	brnch.registerRcvrBlocked();
                _otherBranch = brnch;
                _writePending = true;
                while( _writePending && !_terminate && !brnch.isIterationOver() ) {
                    workspace.wait(this);
                }
            	brnch.registerRcvrUnBlocked();
                _otherBranch = null;
                _writePending = false;
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else if( brnch.isIterationOver() ) {
            	throw new TerminateBranchException("");
            } else {
                //
                // Get Permission From Controlling Branch
                //
                while( !brnch.isBranchPermitted() && !brnch.isIterationOver() ) {
                    brnch.registerRcvrBlocked();
                    workspace.wait(this);
                }
                brnch.registerRcvrUnBlocked();
                if( brnch.isIterationOver() ) {
                    throw new TerminateBranchException("");
                }
            
                super.put(token);
                if( _readPending ) {
                    director._actorReadUnBlocked(true);
                    _readPending = false;
                    notifyAll();
                }
                
                //
                // Inform The Controlling Branch Of Success
                //
            	brnch.completeEngagement();
            }
        }
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
    public void consumerProducerPut(Token token, Branch controllingBranch) throws 
    	    TerminateBranchException {
        ProcessDirector director = ((ProcessDirector)((Actor)
        	(getContainer().getContainer())).getDirector());
        Branch brnch = controllingBranch;
        
        synchronized(this) {
            if( !_terminate && !hasRoom() && !brnch.isIterationOver() ) {
            	brnch.registerRcvrBlocked();
                _otherBranch = brnch;
                _writePending = true;
                while( _writePending && !_terminate && !brnch.isIterationOver() ) {
                    try {
                        wait();
                    } catch( InterruptedException e ) {
                        throw new TerminateProcessException(
                        	"InterruptedException thrown");
                    }
                }
            	brnch.registerRcvrUnBlocked();
                _otherBranch = null;
                _writePending = false;
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else if( brnch.isIterationOver() ) {
            	throw new TerminateBranchException("");
            } else {
                //
                // Get Permission From Controlling Branch
                //
                while( !brnch.isBranchPermitted() && !brnch.isIterationOver() ) {
                    brnch.registerRcvrBlocked();
                    try {
                        wait();
                    } catch( InterruptedException e ) {
                        throw new TerminateProcessException(
                        	"InterruptedException thrown");
                    }
                }
                brnch.registerRcvrUnBlocked();
                if( brnch.isIterationOver() ) {
                    throw new TerminateBranchException("");
                }
            
                super.put(token);
                if( _readPending ) {
                    _otherBranch.registerRcvrUnBlocked();
                    _readPending = false;
                    notifyAll();
                }
                
                //
                // Inform The Controlling Branch Of Success
                //
            	brnch.completeEngagement();
            }
        }
    }

    /** Atomic Put/Composite Get
     *  Put a token into the mailbox receiver and specify a Branch
     *  to control the execution of this method. If the controlling
     *  branch becomes inactive during the execution of this method,
     *  then throw a TerminateBranchException. If this receiver is
     *  terminated during the execution of this method, then throw
     *  a TerminateProcessException.
     * @param token The token being placed in this receiver.
     * @param controllingBranch The Branch controlling execution of
     *  this method.
     */
    public void producerPut(Token token) throws 
    	    TerminateBranchException {
        ProcessDirector director = ((ProcessDirector)((Actor)
        	(getContainer().getContainer())).getDirector());
        
        synchronized(this) {
            if( !_terminate && !hasRoom() ) {
                director._actorWriteBlocked();
                _writePending = true;
                while( _writePending && !_terminate ) {
                    try {
                        wait();
                    } catch( InterruptedException e ) {
                        throw new TerminateProcessException(
                        	"InterruptedException thrown");
                    }
                }
            }
            
            if( _terminate ) {
            	throw new TerminateProcessException("");
            } else {
                super.put(token);
                if( _readPending ) {
                    _otherBranch.registerRcvrUnBlocked();
                    _readPending = false;
                    notifyAll();
                }
            }
        }
    }

    /** Reset the local flags of this receiver. Use this method when
     *  restarting execution.
     */
    public void reset() {
    	_terminate = false;
        _readPending = false;
        _writePending = false;
	_boundaryDetector.reset();
    }

    /** Set a local flag that requests that the simulation be paused
     *  or resumed.
     *  @param value The flag indicating a requested pause or resume.
     */
    public void requestPause(boolean value) {
    }

    /** Set a local flag requesting that the simulation be finished.
     */
    public void requestFinish() {
    	_terminate = true;
        notifyAll();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////
    
    private boolean _terminate = false;
    private boolean _readPending = false;
    private boolean _writePending = false;
    
    private Branch _otherBranch = null;
    private BoundaryDetector _boundaryDetector;

}
