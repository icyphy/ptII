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
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of this receiver.
     */
    public PNQueueReceiver(IOPort container) {
        super(container);
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
     *  process, and inform the director of the same. Then check if a pause is
     *  requested, in which case suspend the calling process until the
     *  execution is resumed.
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
                //Only for listeners. Keep it before inform
                _readblockedactor = (Actor)getContainer().getContainer();
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
                    //For listeners alone. Keep it after informOfWriteU
                    _writeblockedactor = null;
                    _writepending = false;
                    notifyAll(); //Wake up threads waiting on a write;
                }
            }
            while (_pause) {
                director.increasePausedCount();
                workspace.wait(this);
            }
            return result;
        }
    }

    /** Return the actor blocked on a read from this receiver, if any. Return
     *  null if there is no actor blocked on a read from this receiver.
     *  @return the blocked actor if any, else null.
     */
    public Actor getReadBlockedActor() {
        return _readblockedactor;
    }

    /** Return the actor blocked on a write to this receiver, if any. Return
     *  null if there is no actor blocked on a write to this receiver.
     *  @return the blocked actor if any, else null.
     */
    public Actor getWriteBlockedActor() {
        return _writeblockedactor;
    }

    /** Return true since a channel in the Kahn process networks
     *  model of computation is of infinite capacity and always has room.
     *  @return true
     */
    public boolean hasRoom() {
	return true;
    }

    /** Return true since a call to the get() method of the receiver will
     *  always return a token if the call to get() ever returns.
     *  @return true
     */
    public boolean hasToken() {
	return true;
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is contained
     *  by a composite actor. If this receiver is connected to the inside
     *  of a boundary port, then return true; otherwise return false.
     *  This method is not synchronized so the caller should be.
     * @return True if this receiver is connected to the inside of a
     *  boundary port; return false otherwise.
     */
    public boolean isConnectedToBoundary() {
        if( _connectedBoundaryCacheIsOn ) {
            return _isConnectedBoundaryValue;
        } else {
            IOPort innerPort = (IOPort)getContainer();
            if( innerPort == null ) {
                _connectedBoundaryCacheIsOn = false;
                _isConnectedBoundaryValue = false;
                return _isConnectedBoundaryValue;
            }
            ComponentEntity innerEntity =
                (ComponentEntity)innerPort.getContainer();
            Port outerPort = null;
            ComponentEntity outerEntity = null;

            Iterator ports = innerPort.connectedPortList().iterator();
            while( ports.hasNext()) {
                outerPort = (Port)ports.next();
                outerEntity = (ComponentEntity)outerPort.getContainer();
                if( outerEntity == innerEntity.getContainer() ) {
                    // The port container of this receiver is
                    // connected to a boundary port. Now determine
                    // if this receiver's channel is connected to
                    // a boundary port.
                    try {
                        Receiver[][] rcvrs =
                            ((IOPort)outerPort).deepGetReceivers();
                        for( int i = 0; i < rcvrs.length; i++ ) {
                            for( int j = 0; j < rcvrs[i].length; j++ ) {
                                if( this == rcvrs[i][j] ) {
                                    _connectedBoundaryCacheIsOn = true;
                                    _isConnectedBoundaryValue = true;
                                    return true;
                                }
                            }
                        }
                    } catch( IllegalActionException e) {
                        // FIXME: Do Something!
                    }
                }
            }
            _connectedBoundaryCacheIsOn = true;
            _isConnectedBoundaryValue = false;
            return _isConnectedBoundaryValue;
        }
    }

    /** Return true if this receiver is contained on the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the inside of a boundary port then return true; otherwise
     *  return false. This method is not synchronized so the caller
     *  should be.
     * @return True if this receiver is contained on the inside of
     *  a boundary port; return false otherwise.
     */
    public boolean isInsideBoundary() {
        if( _insideBoundaryCacheIsOn ) {
            return _isInsideBoundaryValue;
        } else {
            IOPort innerPort = (IOPort)getContainer();
            if( innerPort == null ) {
                _insideBoundaryCacheIsOn = false;
                _isInsideBoundaryValue = false;
                return _isInsideBoundaryValue;
            }
            ComponentEntity innerEntity =
                (ComponentEntity)innerPort.getContainer();
            if( !innerEntity.isAtomic() && innerPort.isOpaque() ) {
                // This receiver is contained by the port
                // of a composite actor.
                if( innerPort.isOutput() && !innerPort.isInput() ) {
                    _isInsideBoundaryValue = true;
                } else if( !innerPort.isOutput() && innerPort.isInput() ) {
                    _isInsideBoundaryValue = false;
                } else if( !innerPort.isOutput() && !innerPort.isInput() ) {
                    _isInsideBoundaryValue = false;
                } else {
                    // CONCERN: The following only works if the port
                    // is not both an input and output.
                    throw new IllegalArgumentException("A port that "
                            + "is both an input and output can not be "
                            + "properly dealt with by "
                            + "PNQueueReceiver.isInsideBoundary");
                }
                _insideBoundaryCacheIsOn = true;
                return _isInsideBoundaryValue;
            }
            _insideBoundaryCacheIsOn = true;
            _isInsideBoundaryValue = false;
            return _isInsideBoundaryValue;
        }
    }

    /** Return true if this receiver is contained on the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the outside of a boundary port then return true; otherwise
     *  return false. This method is not synchronized so the caller
     *  should be.
     * @return True if this receiver is contained on the outside of
     *  a boundary port; return false otherwise.
     */
    public boolean isOutsideBoundary() {
        if( _outsideBoundaryCacheIsOn ) {
            return _isInsideBoundaryValue;
        } else {
            IOPort innerPort = (IOPort)getContainer();
            if( innerPort == null ) {
                _outsideBoundaryCacheIsOn = false;
                _isOutsideBoundaryValue = false;
                return _isOutsideBoundaryValue;
            }
            ComponentEntity innerEntity =
                (ComponentEntity)innerPort.getContainer();
            if( !innerEntity.isAtomic() && innerPort.isOpaque() ) {
                // This receiver is contained by the port
                // of a composite actor.
                if( innerPort.isOutput() && !innerPort.isInput() ) {
                    _isOutsideBoundaryValue = false;
                } else if( !innerPort.isOutput() && innerPort.isInput() ) {
                    _isOutsideBoundaryValue = true;
                } else if( !innerPort.isOutput() && !innerPort.isInput() ) {
                    _isOutsideBoundaryValue = false;
                } else {
                    // CONCERN: The following only works if the port
                    // is not both an input and output.
                    throw new IllegalArgumentException("A port that "
                            + "is both an input and output can not be "
                            + "properly dealt with by "
                            + "PNQueueReceiver.isInsideBoundary");
                }
                _outsideBoundaryCacheIsOn = true;
                return _isOutsideBoundaryValue;
            }
            _outsideBoundaryCacheIsOn = true;
            _isOutsideBoundaryValue = false;
            return _isOutsideBoundaryValue;
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
     *  If a pause is requested, then suspend the calling process until a
     *  resumption is requested.
     *  @param token The token to be put in the receiver.
     */
    public void put(Token token) {
	Workspace workspace = getContainer().workspace();
	BasePNDirector director = (BasePNDirector)
            ((Actor)(getContainer().getContainer())).getDirector();
        synchronized(this) {
            if (!super.hasRoom()) {
                _writepending = true;
                //Note: Required only to inform the listeners
                _writeblockedactor =
                    ((ProcessThread)Thread.currentThread()).getActor();
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
                    //NOTE: Only for listeners. Please keep it after informU
                    _readblockedactor = null;
                    _readpending = false;
                    notifyAll();
                    //Wake up all threads waiting on a write to this receiver;
                }
            }
            while (_pause) {
                director.increasePausedCount();
                workspace.wait(this);
            }
        }
    }

    /** Reset the state variables in the receiver.
     */
    public void reset() {
	super.reset();
	_readblockedactor = null;
	_writeblockedactor = null;
	_readpending = false;
	_writepending = false;
	_pause = false;
	_terminate = false;
    	_insideBoundaryCacheIsOn = false;
    	_isInsideBoundaryValue = false;
    	_outsideBoundaryCacheIsOn = false;
    	_isOutsideBoundaryValue = false;
    	_connectedBoundaryCacheIsOn = false;
    	_isConnectedBoundaryValue = false;
    }

    /** Pause any process that tries to read from or write to this receiver
     *  if the argument is true. If the argument is false, then resume any
     *  process paused while reading from or writing to this receiver.
     *  @param pause true if requesting a pause and false if requesting a
     *  resumption of the paused thread.
     */
    public synchronized void requestPause(boolean pause) {
	if (pause) {
	    _pause = true;
	} else {
	    _pause = false;
	    notifyAll();
	}
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

    private Actor _readblockedactor = null;
    private Actor _writeblockedactor = null;
    private boolean _readpending = false;
    private boolean _writepending = false;
    private boolean _pause = false;
    private boolean _terminate = false;

    private boolean _insideBoundaryCacheIsOn = false;
    private boolean _isInsideBoundaryValue = false;

    private boolean _outsideBoundaryCacheIsOn = false;
    private boolean _isOutsideBoundaryValue = false;

    private boolean _connectedBoundaryCacheIsOn = false;
    private boolean _isConnectedBoundaryValue = false;
}
