/* A Queue with optional history and capacity, performing blocking reads
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
import ptolemy.actor.process.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PNQueueReceiver
/**
A first-in, first-out (FIFO) queue with optional capacity and
history, with blocking reads and writes. Objects are appended to the queue
with the put() method, performing a blocking write, and removed from the queue
with the get() method using blocking reads. The object removed is the oldest
one in the queue. 
If the history capacity is greater than zero (or infinite, indicated by 
a capacity of -1), then objects removed from the queue are transferred to a
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
public class PNQueueReceiver extends QueueReceiver implements ProcessReceiver {
    /** Construct an empty queue with no container
     */
    public PNQueueReceiver() {
        super();
    }

    /** Construct an empty queue with the specified container.
     *  @param container is the port containing this receiver.
     */
    public PNQueueReceiver(IOPort container) {
        super(container);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove and return the oldest token from the FIFO queue. If there 
     *  are no tokens in the queue, then the method blocks on it and 
     *  informs the director of it. Otherwise or after resuming, it takes
     *  the token and checks if any process is blocked on a write to this 
     *  receiver. If a process is indeed blocked, then it unblocks the 
     *  process, and informs the director. It then checks if a pause is
     *  requested, in which case it pauses. Otherwise it returns.
     *  In case the simulation is to be terminated, it throws a 
     *  TerminateProcessException.
     *  @return Token read from the queue
     */
    public Token get() {
	Workspace workspace = getContainer().workspace();
	PNDirector director = ((PNDirector)((Actor)(getContainer().getContainer())).getDirector());
        Token result = null;
	//System.out.println(getContainer().getFullName() +" in receiver.get");
        synchronized (this) {
            while (!_terminate && !super.hasToken()) {
                //System.out.println(getContainer().getFullName()+" Reading block");
                director._readBlock();
                //System.out.println("After the readblocking.. I am "+getContainer().getFullName());
                _readpending = true;
                while (_readpending && !_terminate) {
                    //System.out.println("Waiting in the workspace");
                    workspace.wait(this);
                }
            }

            //System.out.println("Halfway thru receiver.get()");
            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                result = super.get();
                //Check if pending write to the Queue;
                if (_writepending) {
                    //System.out.println(getContainer().getFullName()+" being unblocked");
                    director._writeUnblock(this);
                    _writepending = false;
                    notifyAll(); //Wake up threads waiting on a write;
                }
            }
            
            while (_pause) {
                //System.out.println(" Actually pausing");
                director.increasePausedCount();
                workspace.wait(this);
            }
            return result;
        }
    }
    
    /** Return true as ideally a channel in the Kahn process networks
     *  model of computation is of infinite capacity and always has room.
     *  Also in our implementation, polling is not permitted as we implement
     *  blocking writes.
     *  @return true
     */
    public boolean hasRoom() {
	return true;
    }

    /** Return true as the Kahn process networks model of computation does
     *  not allow polling for data.
     * @return true
     * @exception IllegalActionException never thrown in this class.
     */
    public boolean hasToken() {
	return true;
    }

    /** Return a true or false to indicate if there is a read pending
     *  on this receiver or not.
     * @return true if a read is pending else false
     */
    public synchronized boolean isReadPending() {
	return _readpending;
    }

    /** Return a true or false to indicate if there is a write pending
     *  on this receiver.
     * @return true if a write is pending else false
     */
    public synchronized boolean isWritePending() {
	return _writepending;
    }

    /** Put a token on the queue.  If the queue is full, then the method
     *  blocks on it and informs the director of it. Otherwise or after 
     *  resuming, it puts a token and checks whether any process is blocked 
     *  on a read from this receiver. If a process is indeed blocked, then 
     *  it unblocks the process, and informs the director. It then checks 
     *  whether a pause is requested, in which case it pauses. Otherwise it 
     *  returns.
     *  In case the simulation is to be terminated, it throws a 
     *  TerminateProcessException.
     *  @param token The token to put in the FIFO queue.
     */
    public void put(Token token) {
	Workspace workspace = getContainer().workspace();
	PNDirector director = (PNDirector)((Actor)(getContainer().getContainer())).getDirector();
	//System.out.println("putting token in PNQueueReceiver and pause = "+_pause);

        synchronized(this) {
            if (!super.hasRoom()) {
                _writepending = true;
                //System.out.println(getContainer().getFullName()+" being writeblocked");
                director._writeBlock(this);
                while (!_terminate && !super.hasRoom()) {
                    //System.out.println(getContainer().getFullName()+" waiting on write");
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
                    director._readUnblock();
                    _readpending = false;
                    notifyAll();
                    //Wake up all threads waiting on a write to this receiver;
                }
            }
            while (_pause) {
                //System.out.println("Pausing in puuuuuuuuuut");
                director.increasePausedCount();
                workspace.wait(this);
            }
        }
    }

    /** Pause or resume the thread that tries to read or write to this 
     *  receiver. 
     *  @param pause true if requesting a pause and false if requesting a 
     *  resumption of the paused thread, if any.
     */
    public synchronized void setPause(boolean pause) {
	if (pause) {
	    _pause = true;
	} else {
	    _pause = false;
	    notifyAll();
	}
    }

    /** Set the flag indicating a pending read from the receiver.
     *  @param readpending true if the calling process is blocking on a 
     *  read, false otherwise.
     */
    public synchronized void setReadPending(boolean readpending) {
	_readpending = readpending;
    }

    /** Set the flag indicating a pending write from the receiver. 
     *  @param writepending is true if the calling process is blocking on 
     *  a write, false otherwise.
     */
    public synchronized void setWritePending(boolean writepending) {
	_writepending = writepending;
    }

    /** Set the flag in the receiver to indicate the onset of termination.
     *  This will result in termination of any process that is either blocked
     *  on the receiver or is trying to read or write from it.
     */
    public synchronized void setFinish() {
	_terminate = true;
        //System.out.println("Terminating a receiver");
	notifyAll();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _readpending = false;
    private boolean _writepending = false;
    private boolean _pause = false;
    private boolean _terminate = false;
}








