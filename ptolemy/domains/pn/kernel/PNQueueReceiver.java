/* A receiver with a FIFO queue and performing blocking reads
   and blocking writes.

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
@AcceptedRating Green (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// PNQueueReceiver
/**

A receiver with a FIFO queue that blocks the calling process on a read if the
FIFO queue is empty and on a write if the queue is full. Blocking read provides
the basic functionality of a FIFO channel in the process networks model of
computation. Blocking write supports the implementation suggested by Parks for
bounded memory execution of process networks.
<p>
Tokens are appended to the queue with the put() method, which blocks on a write
if the queue is full. Tokens are removed from the queue with the get() method,
which blocks on a read if the queue is empty.
In case a process blocks on a read or a write, the receiver informs the
director about the same.
The receiver also unblocks processes blocked on a read or a write. In case
a process is blocked on a read (read-blocked), it is unblocked on availability
of a token.  If a process is blocked on a write (write-blocked), it
is unblocked on the availability of room in the queue and informs the director
of the same.
<p>
This class is also responsible for pausing or terminating a process that tries
to read from or write to the receiver. In case of termination, the receiver
throws a TerminateProcessException when a process tries to read from or write
to the receiver. This terminates the process.
In case of pausing, the receiver suspends the process when it tries to read
from or write to the receiver and resumes it only after a request to resume the
process has been received.
<p>

@author Mudit Goel, John S. Davis II
@version $Id$
@see QueueReceiver
@see ptolemy.actor.QueueReceiver
*/
public class PNQueueReceiver extends QueueReceiver implements ProcessReceiver {
    /** Construct an empty receiver with no container
     */
    public PNQueueReceiver() {
        super();
	_boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of this receiver.
     */
    public PNQueueReceiver(IOPort container) {
        super(container);
	_boundaryDetector = new BoundaryDetector(this);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
    public Token get() {
	Workspace workspace = getContainer().workspace();
	BasePNDirector director = ((BasePNDirector)
                ((Actor)(getContainer().getContainer())).getDirector());
        Token result = null;
        synchronized (this) {
            while (!_terminate && !super.hasToken()) {
                director._actorReadBlocked(true);
                _readpending = true;
                while (_readpending && !_terminate) {
                    workspace.wait(this);
                }
            }
            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                result = super.get();
                //Check if pending write to the Queue;
                if (_writepending) {
                    director._informOfWriteUnblock(this);
                    _writepending = false;
                    notifyAll(); // Wake up threads waiting on a write;
                }
            }
            return result;
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
	    return consumerGet(branch);
	}
	return get();

    }

    /** Return true since a channel in the Kahn process networks
     *  model of computation is of infinite capacity and always has room.
     *  @return true
     *  FIXME
     */
    public boolean hasRoom() {
	return true;
    }

    /** Return true since a call to the get() method of the receiver will
     *  always return a token if the call to get() ever returns.
     *  @return true
     *  FIXME
     */
    public boolean hasToken() {
	return true;
    }

    /**
     */
    public boolean isConnectedToBoundary() {
	return _boundaryDetector.isConnectedToBoundary();
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

    /** Associated with Atomic Get/Composite Put
     *  @param branch The invoking branch.
     *  @return The oldest Token read from the queue
     */
    public Token consumerGet(Branch branch) {
	Workspace workspace = getContainer().workspace();
	BasePNDirector director = ((BasePNDirector)
                ((Actor)(getContainer().getContainer())).getDirector());
        Token result = null;
        synchronized (this) {
            while (!_terminate && !super.hasToken()) {
                director._actorReadBlocked(true);
                _readpending = true;
		// _otherBranch = branch;
                while (_readpending && !_terminate) {
                    workspace.wait(this);
                }
            }
            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                result = super.get();
                //Check if pending write to the Queue;
                if (_writepending) {
		    _otherBranch.registerRcvrUnBlocked();
		    _otherBranch = null;
                    _writepending = false;
		    // Wake up threads waiting on a write;
                    notifyAll(); 
                }
            }
            return result;
        }
    }
    
    /** Associated with Composite Get/Composite Put
     *  @param branch The invoking branch.
     *  @return The oldest Token read from the queue
     */
    public Token consumerProducerGet(Branch branch) {
	Workspace workspace = getContainer().workspace();
	BasePNDirector director = ((BasePNDirector)
                ((Actor)(getContainer().getContainer())).getDirector());
        Token result = null;
        synchronized (this) {
            while (!_terminate && !super.hasToken()) {
                branch.registerRcvrBlocked();
		_otherBranch = branch;
                _readpending = true;
                while (_readpending && !_terminate) {
                    workspace.wait(this);
                }
            }
            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                result = super.get();
                //Check if pending write to the Queue;
                if (_writepending) {
		    _otherBranch.registerRcvrUnBlocked();
		    _otherBranch = null;
                    _writepending = false;
		    // Wake up threads waiting on a write;
                    notifyAll(); 
                }
            }
            return result;
        }
    }
    
    /** Associated with Composite Get/Atomic Put
     *  @param branch The invoking branch.
     *  @return The oldest Token read from the queue
     */
    public Token producerGet(Branch branch) {
	Workspace workspace = getContainer().workspace();
	BasePNDirector director = ((BasePNDirector)
                ((Actor)(getContainer().getContainer())).getDirector());
        Token result = null;
        synchronized (this) {
            while (!_terminate && !super.hasToken()) {
                branch.registerRcvrBlocked();
		_otherBranch = branch;
                _readpending = true;
                while (_readpending && !_terminate) {
                    workspace.wait(this);
                }
            }
            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                result = super.get();
                //Check if pending write to the Queue;
                if (_writepending) {
                    director._informOfWriteUnblock(this);
                    _writepending = false;
		    // Wake up threads waiting on a write;
                    notifyAll(); 
                }
            }
            return result;
        }
    }

    /** Return a true or false to indicate whether there is a read pending
     *  on this receiver or not, respectively.
     *  @return a boolean indicating whether a read is pending on this
     *  receiver or not.
     */
    public synchronized boolean isReadPending() {
	return _readpending;
    }

    /** Return a true or false to indicate whether there is a write pending
     *  on this receiver or not.
     *  @return A boolean indicating whether a write is pending on this
     *  receiver or not.
     */
    public synchronized boolean isWritePending() {
	return _writepending;
    }

    /** Put a token on the queue contained in this receiver.
     *  If the queue is full, then suspend the calling process (blocking
     *  write) and inform the director of the same. Resume the process on
     *  detecting room in the queue.
     *  If a termination is requested, then initiate the termination of the
     *  calling process by throwing a TerminateProcessException.
     *  On detecting a room in the queue, put a token in the queue.
     *  Check whether any process is blocked
     *  on a read from this receiver. If a process is indeed blocked, then
     *  unblock the process, and inform the director of the same.
     *  @param token The token to be put in the receiver.
     */
    public void put(Token token, Branch branch) {
    	if( isOutsideBoundary() ) {
        } else if( isConnectedToBoundary() ) {
        }
        put(token);
    }
    
    /** Put a token on the queue contained in this receiver.
     *  If the queue is full, then suspend the calling process (blocking
     *  write) and inform the director of the same. Resume the process on
     *  detecting room in the queue.
     *  If a termination is requested, then initiate the termination of the
     *  calling process by throwing a TerminateProcessException.
     *  On detecting a room in the queue, put a token in the queue.
     *  Check whether any process is blocked
     *  on a read from this receiver. If a process is indeed blocked, then
     *  unblock the process, and inform the director of the same.
     *  @param token The token to be put in the receiver.
     */
    public void put(Token token) {
	Workspace workspace = getContainer().workspace();
	BasePNDirector director = (BasePNDirector)
            ((Actor)(getContainer().getContainer())).getDirector();
        synchronized(this) {
            if (!super.hasRoom()) {
                _writepending = true;
                director._actorWriteBlocked(this);
                while (!_terminate && !super.hasRoom()) {
                    while(_writepending) {
                        workspace.wait(this);
                    }
                }
            }
            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                //token can be put in the queue;
                super.put(token);
                //Check if pending write to the Queue;
                if (_readpending) {
                    director._informOfReadUnblock(this, true);
                    _readpending = false;
                    notifyAll();
                    //Wake up all threads waiting on a write to this receiver;
                }
            }
        }
    }

    /** Reset the state variables in the receiver.
     */
    public void reset() {
	super.reset();
	_readpending = false;
	_writepending = false;
	_terminate = false;
	_boundaryDetector.reset();
    }

    /** Set a state flag indicating that there is a process blocked while
     *  trying to read from this receiver.
     *  @param readpending true if the calling process is blocking on a
     *  read, false otherwise.
     */
    public synchronized void setReadPending(boolean readpending) {
	_readpending = readpending;
    }

    /** Set a state flag indicating that there is a process blocked
     *  (write-blocked) while trying to write to the receiver.
     *  @param writepending true if the calling process is blocking on
     *  a write, false otherwise.
     */
    public synchronized void setWritePending(boolean writepending) {
	_writepending = writepending;
    }

    /** Set a flag in the receiver to indicate the onset of termination.
     *  This will result in termination of any process that is either blocked
     *  on the receiver or is trying to read from or write to it.
     */
    public synchronized void requestFinish() {
	_terminate = true;
	notifyAll();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _readpending = false;
    private boolean _writepending = false;
    private boolean _terminate = false;

    private Branch _otherBranch = null;
    private BoundaryDetector _boundaryDetector;
}
