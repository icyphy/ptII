/* A Queue with optional history and capacity, preforming blocking reads 
   and blocking writes.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
*/

package ptolemy.domains.pn.kernel;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PNQueueReceiver
/** 
A first-in, first-out (FIFO) queue with optional capacity and
history, with blocking reads and writes. Objects are appended to the queue 
with the put() method, performing a blocking write, and removed from the queue
with the get() method using blocking reads. The object removed is the oldest 
one in the queue. By default, the capacity is unbounded, but it can be set to
any nonnegative size. If the history
capacity is greater than zero (or infinite, indicated by a capacity
of -1), then objects removed from the queue are transferred to a
second queue rather than simply deleted. By default, the history
capacity is zero. In case the queue is empty, the get() method blocks till
a token is introduced into the queue or a termination exception is thrown.
In case the queue is full, the put() method blocks till there is enough 
room in the queue to introduce the token.

@author Mudit Goel
@version $Id$
@see QueueReceiver    
@see ptolemy.actor.QueueReceiver
*/
public class PNQueueReceiver extends QueueReceiver {
    /** Construct an empty queue with no container
     */
    public PNQueueReceiver() {
        super();
    }

    /** Construct an empty queue with the specified container.
     */
    public PNQueueReceiver(IOPort container) {
        super(container);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads the oldest token from the Queue and returns it. If there are 
     *  no tokens in the Queue, then the method blocks on it. It throws
     *  an exception in case the simulation has to be terminated.
     *  @return Token read from the queue
     *  @exception NoTokenException No Token in the queue and the process
     *  could not block. Should never occur in PN. This is a runtime exception
     *  @exception TerminateProcessException Terminate the simulation. 
     *  This is a runtime exception
     */
    public Token get() {
	try {
	    while (!_terminate && !super.hasToken()) {
		((PNDirector)((Actor)(getContainer().getContainer())).getDirector()).readBlock();
		_readpending = true;
		_waitOnRead();
	    }
	} catch (IllegalActionException e) {
	    System.err.println(e.toString());
	}
	
	if (_terminate) {
	    throw new TerminateProcessException("");
	} else {
	    //_canContinue(); //Checking for pause currently
	    synchronized (this) {
		Token result = super.get();
		//Check if pending write to the Queue
		if (_writepending) {
		    ((PNDirector)((Actor)(getContainer().getContainer())).getDirector()).writeUnblock(this);
		    _writepending = false;
		    notifyAll(); //Wake up threads waiting on a write
		}
		return result;
	    }
	}
    }
	
    public boolean hasRoom() throws IllegalActionException {
	return true;
    }

    public boolean hasToken() throws IllegalActionException {
	return true;
    }

    /** Returns a true or false to indicate if there is a read pending
     *  on this queue or not.
     * @param recep is the receptionist/queue on which the check is being made
     * @return val true if a read is pending else false
     */
    public boolean isReadPending() {
	return _readpending;
    }

    /** Returns a true or false to indicate if there is a write pending
     *  on this queue or not.
     * @param recep is the receptionist/queue on which the check is being made
     * @return val true if a write is pending else false
     */
    public boolean isWritePending() {
	return _writepending;
    }

    /** Put a token on the queue.  If the queue is full, throw an exception.
     *  @param token The token to put on the queue.
     *  @exception NoRoomException If the queue is full.
     */
    public void put(Token token) {
	try {
	    if (!super.hasRoom()) {
		_writepending = true;
		((PNDirector)((Actor)(getContainer().getContainer())).getDirector()).writeBlock(this);
		while(!_terminate && !super.hasRoom()) {
		    _waitOnWrite();
		}
	    }
	} catch (IllegalActionException ex) {
	    System.out.println("Fixme in PNQueueReceiver");
	}
	if (_terminate) {
	    throw new TerminateProcessException("");
	} else { //token can be put in the queue
	    //_canContinue();
	    super.put(token);
	    //Check if pending write to the Queue
	    synchronized (this) {
		if (_writepending) {
		    ((PNDirector)((Actor)(getContainer().getContainer())).getDirector()).writeUnblock(this);
		    _writepending = false;
		    notifyAll(); 
		    //Wake up all threads waiting on a write to this receiver
		}
	    }
	}
    }
    

    /** Add or remove the queue from list of queues blocked on a read depending
     * on the value of the boolean val
     * @param val is true f 
     */
    public void setReadPending(boolean readpending) {
	_readpending = readpending;
    }
    
    /** Set the write Pending flag to a true or a false
     * @param val is the value to which the flag should be set to
     */
    public void setWritePending(boolean writepending) {
	_writepending = writepending;
    }

    public synchronized void setTerminate() {
	_terminate = true;
	notifyAll();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
//     private synchronized void _canContinue() {
// 	if (!_pause) return;
// 	else { //paused
// 	    int depth = 0;
// 	    Port port = (Port)getContainer();
// 	    Workspace _workspace = ((Workspace)port.getContainer());
// 	    //wait();
// 	    try {
// 		try {
// 		    depth = _workspace.releaseAllReadPermissions();
// 		    while (_pause) {
// 			wait();
// 		    }
// 		} catch (InterruptedException ex) {
// 		    throw new InternalErrorException(
// 			    "Thread interrupted while paused! " +
// 			    ex.getMessage());
// 		}
// 	    } finally {
// 		_workspace.reacquireReadPermissions(depth);
// 	    }
// 	    return;
// 	}
//     }

    private synchronized void _waitOnRead() {
	int depth = 0;
	Port port = (Port)getContainer();
	Workspace _workspace = ((Workspace)port.getContainer());
	try {
	    try {
		depth = _workspace.releaseAllReadPermissions();
		while (_readpending) {
		    wait();
		}
	    } catch (InterruptedException ex) {
		throw new InternalErrorException(
			"Thread interrupted while blocked on a read! " +
			ex.getMessage());
	    }   
	} finally {
	    _workspace.reacquireReadPermissions(depth);
	}
    }

    private synchronized void _waitOnWrite() {
	int depth = 0;
	Port port = (Port)getContainer();
	Workspace _workspace = ((Workspace)port.getContainer());
	try {
	    try {
		depth = _workspace.releaseAllReadPermissions();
		while (_writepending) {
		    wait();
		}
		//_workspace.getReadAccess(depth);
	    } catch (InterruptedException ex) {
		throw new InternalErrorException(
			"Thread interrupted while blocked on a read! " +
			ex.getMessage());
	    }   
	} finally {
	    _workspace.reacquireReadPermissions(depth);
	}
    }    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _readpending = false;
    private boolean _writepending = false; 
    private boolean _pause = false;
    private boolean _terminate = false;
}








