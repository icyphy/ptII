/* An experimental receiver to be used in place of PNQueueReceiver.
   The latter is a receiver with a FIFO queue and performing blocking reads
   and blocking writes.  The purpose of this experimental receiver is
   to attempt to compose a DE within PN.  This new PN receiver will
   do ... to achieve this.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

@ProposedRating Red (winthrop@robotics.eecs.berkeley.edu)
@AcceptedRating Red (winthrop@robotics.eecs.berkeley.edu)
*/

package ptolemy.domains.pn.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.process.BoundaryDetector;
import ptolemy.actor.process.Branch;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.kernel.util.Workspace;

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

@author Winthrop Williams
@version $Id$
@see PNQueueReceiver
@see ptolemy.actor.QueueReceiver
*/
public class NPNQueueReceiver extends PNQueueReceiver
        implements ProcessReceiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Unlike PNQueueReceiver, which always returns true, return
     *  true if the next call to get() will succeed without a
     *  NoTokenException.
     *  @return True if the queue has at least one token in it.  */
    public boolean hasToken() {
        return _queue.size() > 0;
    }

    /** Return true if the specified number of tokens is available in the
     *  queue.
     *  @param numberOfTokens The number of tokens to get from the queue.
     *  @return True if the specified number of tokens is available.
     *  @exception IllegalArgumentException If the number of tokens is less
     *   than one.  This is a runtime exception, and hence does not need to
     *   be explicitly declared by the caller.
     */
    public boolean hasToken(int numberOfTokens)
            throws IllegalArgumentException {
	if(numberOfTokens < 1)
	    throw new IllegalArgumentException(
                    "The number of tokens must be greater than 0");
        return _queue.size() >= numberOfTokens;
    }

    /** Put a token on the queue contained in this receiver.
     *  Unlike PNQueueReceiver, wake up actor in case composite with
     *  possibly stalled director inside.
     *  If the queue is full, then suspend the calling process (blocking
     *  write) and inform the director of the same. Resume the process on
     *  detecting room in the queue.
     *  If a termination is requested, then initiate the termination of the
     *  calling process by throwing a TerminateProcessException.
     *  On detecting room in the queue, put a token in the queue.
     *  Check whether any process is blocked
     *  on a read from this receiver. If a process is indeed blocked, then
     *  unblock the process, and inform the director of the same.
     *  @param token The token to be put in the receiver.
     */
    public void put(Token token, Branch branch) {
	Workspace workspace = getContainer().workspace();
	BasePNDirector director = (BasePNDirector)
            ((Actor)(getContainer().getContainer())).getExecutiveDirector();
        synchronized(this) {
            // if (!super.hasRoom()) {
            while (!_terminate && !super.hasRoom()) {
                _writeBlocked = true;
                prepareToBlock(branch);
                // director._actorBlocked(this);
                while (_writeBlocked && !_terminate ) {
                    // while (!_terminate && !super.hasRoom()) {
                    // while(_writeBlocked) {
                    // checkIfBranchIterationIsOver(branch);
                    workspace.wait(this);
                    // }
                }
            }
            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                //token can be put in the queue;
                super.put(token);
                //Check if pending write to the Queue;
                if (_readBlocked) {
                    wakeUpBlockedPartner();
                    // director._actorUnBlocked(this);
                    _readBlocked = false;
                    notifyAll();
                    //Wake up all threads waiting on a write to this receiver;
                }
            }
        }
        synchronized(getContainer().getContainer()) {
            notifyAll();
        }
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _readBlocked = false;
    private boolean _writeBlocked = false;
    private boolean _terminate = false;

    private Branch _otherBranch = null;
    private BoundaryDetector _boundaryDetector;
}

















