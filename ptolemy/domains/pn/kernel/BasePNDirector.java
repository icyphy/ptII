/* Base class for governing the execution of a CompositeActor with restricted
Kahn-MacQueen process network semantics.

 Copyright (c)  The Regents of the University of California.
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
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// BasePNDirector
/**
This is the base class for the directors that govern the execution of a 
CompositeActor with Kahn process networks (PN) semantics. This base class 
attaches the Kahn-McQueen process networks semantics to a composite actor. 
This base class does not support mutations although it provides the basic 
infrastructure for mutations. The derived directors can use this 
infrastructure to support mutations.
<p>
The thread that calls the various execution methods (initialize, prefire, fire
and postfire) on the director is referred to as the <i>directing thread</i>. 
This directing thread might be the main thread reponsible for the execution 
of the entire simulation or might be the thread created by the executive 
director of the containing composite actor.
<p>
In the PN domain, the director creates a thread (an instance of 
ProcessThread) representing a Kahn process, for each actor in the model. 
The threads are created in initialize() and started in the prefire() method 
of the ProcessDirector. A process is considered <i>active</i> from its 
creation until its termination. An active process can block when trying to 
read from a channel (read-blocked) or when trying to write to a channel 
(write-blocked). Derived directors can define additional ways in which active
processes can block.
<p>
A <i>deadlock</i> is when all the active processes are blocked.
The director is responsible for handling deadlocks during execution.
This director handles two different sorts of deadlocks, real deadlock and
artificial deadlock. Derived directors might introduce additional forms of 
deadlock.
<p>
A real deadlock is when all the processes are blocked on a read meaning that
no process can proceed until it receives new data. The execution can be 
terminated, if desired, in such a situation. If the container of this director
is the top-level composite actor, then the manager terminates the execution. 
If the container is not the top-level composite actor, then it is upto the 
executive director of the container to decide on the termination of the 
execution. To terminate the execution, the manager or the executive director
calls wrapup() on the director.
<p>
An artificial deadlock is when all processes are blocked and atleast one 
process is blocked on a write. In this case the director increases the 
capacity of the receiver with the smallest capacity amongst all the 
receivers on which a process is blocked on a write. 
This breaks the deadlock and the execution can proceed.
<p>
This base director does not support any form of mutations or topology changes
but provides the basic infrastructure in the form of methods that the derived 
directors can use.
<p>
This director also permits pausing of the execution. An execution is paused
when all active processes are blocked or paused (atleast one process is 
paused). In case of PN, a process can be paused only when it tries to 
communicate with other processes. Thus a process can be paused in the get() 
or put() methods of the receivers alone. If there is a process that does 
not communicate with other processes in the model, then the simulation can 
never pause in that model.
<p>


@author Mudit Goel
@version $Id$
*/
public class BasePNDirector extends ptolemy.actor.process.ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace. 
     *  Create a director parameter "Initial_queue_capacity" with the default 
     *  value 1. This sets the initial capacities of the queues in all 
     *  the receivers created in the PN domain.
     */
    public BasePNDirector() {
        super();
        try {
            Parameter param = new Parameter(this,"Initial_queue_capacity",
                    new IntToken(1));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e.toString());
        } catch (NameDuplicationException e) {
            throw new InvalidStateException(e.toString());
        }
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace. 
     *  Create a director parameter "Initial_queue_capacity" with the default 
     *  value 1. This sets the initial capacities of the queues in all 
     *  the receivers created in the PN domain.
     *  @param name Name of this director.
     */
    public BasePNDirector(String name) {
        super(name);
        try {
            Parameter param = new Parameter(this,"Initial_queue_capacity",
                    new IntToken(1));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e.toString());
        } catch (NameDuplicationException e) {
            throw new InvalidStateException(e.toString());
        }
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace. 
     *  Create a director parameter "Initial_queue_capacity" with the default 
     *  value 1. This sets the initial capacities of the queues in all 
     *  the receivers created in the PN domain.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public BasePNDirector(Workspace workspace, String name) {
        super(workspace, name);
        try {
            Parameter param = new Parameter(this,"Initial_queue_capacity",
                    new IntToken(1));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e.toString());
        } catch (NameDuplicationException e) {
            throw new InvalidStateException(e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active proceses is zero 
     *  and it is not paused. The parameter "Initial_queue_capacity" has the 
     *  same value as the director being cloned.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new BasePNDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        BasePNDirector newobj = (BasePNDirector)super.clone(ws);
        newobj._readBlockCount = 0;
        newobj._mutationBlockCount = 0;
        newobj._writeBlockCount = 0;
        newobj._writeblockedQs = new LinkedList();
        return newobj;
    }


    /** Return a new receiver compatible with this director. The receiver
     *  is an instance of PNQueueReceiver. Set the initial capacity
     *  of the FIFO queue in the receiver to the value specified by the
     *  director parameter "Initial_queue_capacity". The default value
     *  of the parameter is 1.
     *  @return A new PNQueueReceiver.
     */
    public Receiver newReceiver() {
        PNQueueReceiver rec =  new PNQueueReceiver();
        try {
            Parameter par = (Parameter)getAttribute("Initial_queue_capacity");
            int cap = ((IntToken)par.getToken()).intValue();
            rec.setCapacity(cap);
        } catch (IllegalActionException e) {
            //This exception should never be thrown, as size of queue should
            //be 0, and capacity should be set to a non-negative number
            throw new InternalErrorException(e.toString());
        }
        return rec;
    }

    /** Return false if the simulation has reached a real deadlock. Return
     *  true otherwise.
     *  @return false if the director has detected a real deadlock.
     *  @exception IllegalActionException Not thrown in the PN domain.
     */
    public boolean postfire() throws IllegalActionException {
        //System.out.println("Postifre printing " +_notdone);
	if (_readBlockCount == _getActiveActorsCount()) {
	    //return _notdone;
	    return false;
	} else {
	    return true;
	}
    }


    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return true if a deadlock is detected. The base director detects real
     *  and artificial deadlocks alone. If derived classes introduce any 
     *  additional forms of deadlocks, they should override this method to 
     *  return true on detection of the new deadlocks as well. 
     *  @return true if a real or artificial deadlock is detected.
     */
    protected synchronized boolean _checkForDeadlock() {
	if (_readBlockCount + _writeBlockCount >= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Return true if the execution has paused. This base class returns true
     *  if all the active processes are either blocked (on a read or a write) 
     *  or paused. Derived classes should override this method if they 
     *  introduce any new forms of deadlock.
     *  @return true if the execution has paused.
     */
    protected synchronized boolean _checkForPause() {
	if (_readBlockCount + _writeBlockCount + _getPausedActorsCount()
		>= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Return true on detection of a real deadlock or break the deadlock 
     *  and return false otherwise. 
     *  On detection of an artificial deadlock, select the
     *  receiver with the smallest queue capacity on which any process is 
     *  blocked on a write and increment the capacity of the contained queue. 
     *  If the capacity is non-negative, then increment the capacity by 1. 
     *  Otherwise set the capacity to 1. Unblock the process blocked on 
     *  this receiver. Notify the thread corresponding to the blocked 
     *  process and return false.
     *  
     *  @return true if a real deadlock is detected, false otherwise.
     *  @exception IllegalActionException Not thrown in PN.
     */
    protected boolean _handleDeadlock()
	    throws IllegalActionException {
        if (_writeBlockCount==0) {
            //Check if there are any events in the future.
	    System.out.println("real deadlock. Everyone would be erased");
	    return true;
        } else {
            //its an artificial deadlock;
            System.out.println("Artificial deadlock");
            // find the input port with lowest capacity queue;
            // that is blocked on a write and increment its capacity;
            _incrementLowestWriteCapacityPort();
        }
        return false;
    }

    /** Increment the capacity by 1 of one of the queues with the smallest 
     *  capacity belonging to a receiver on which a process is blocked 
     *  while attempting to write. Traverse through the list of receivers
     *  on which a process is blocked on a write and choose the one containing
     *  the queue with the smallest capacity. Increment the capacity of the
     *  queue by 1 if the capacity is non-negative. In case the capacity is 
     *  negative, set the capacity to 1. 
     *  Unblock the process blocked on a write to this receiver.
     *  Notify the thread corresponding to the blocked process and return.
     */
    protected void _incrementLowestWriteCapacityPort() {
        //System.out.println("Incrementing capacity");
        PNQueueReceiver smallestCapacityQueue = null;
        int smallestCapacity = -1;
        //FIXME: Should I traverse the topology and get receivers blocked on 
        // a write or should I stick with this strategy?
	Enumeration receps = _writeblockedQs.elements();
	//System.out.println("Enumeration receps done");
	while (receps.hasMoreElements()) {
	    PNQueueReceiver queue = (PNQueueReceiver)receps.nextElement();
	    if (smallestCapacity == -1) {
	        smallestCapacityQueue = queue;
		smallestCapacity = queue.getCapacity();
		//smallestCapacityRecep = flowqueue;
	    } else if (smallestCapacity > queue.getCapacity()) {
	        smallestCapacityQueue = queue;
	        smallestCapacity = queue.getCapacity();
	    }
	}
        //System.out.println("I am here");
        try {
            if (smallestCapacityQueue.getCapacity() <= 0) {
                smallestCapacityQueue.setCapacity(1);
                //System.out.println("Setting capacity of "+smallestCapacityQueue.getContainer().getFullName()+" to 1");
            } else {
	        smallestCapacityQueue.setCapacity(smallestCapacityQueue.getCapacity()*2);
                //System.out.println("Setting capacity of "+smallestCapacityQueue.getContainer().getFullName()+" to "+(smallestCapacityQueue.getCapacity()+1) );
            }
	    _writeUnblock(smallestCapacityQueue);
	    smallestCapacityQueue.setWritePending(false);
            synchronized(smallestCapacityQueue) {
                System.out.println("Notifying ........ All");
                smallestCapacityQueue.notifyAll();
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e.toString());
	    //Should not be thrown as this exception is thrown
            //only if port is not an input port, checked above
        }
        return;
    }

    /** Increment the count of actors waiting for the queued topology changes
     *  to be processed. Check for a resultant deadlock or pausing of the 
     *  execution. If either of them is detected, then notify the directing 
     *  thread of the same.
     *  This method is normally called by processes or actors that queue
     *  requests for changes to the topology and do not wish to continue
     *  their execution until their requests are processed.
     *  This method is not used by the base director and is provided for
     *  use by the derived classes.
     */
    synchronized void _mutationBlock() {
	_mutationBlockCount++;
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
	return;
    }
	
    /** Decrement the count of actors waiting for the topology change requests
     *  (mutation requests) to be processed. This method is not used by the
     *  base director and is provided for use by the derived classes.
     */
    synchronized void _mutationUnblock() {
	_mutationBlockCount--;
    }


    /** Increment by 1 the count of processes blocked while reading from a 
     *  receiver. Check for a resultant deadlock or pausing of the 
     *  execution. If either of them is detected, then notify the directing
     *  thread of the same.
     */
    synchronized void _readBlock() {
	_readBlockCount++;
	//System.out.println("Readblocked with count "+_readBlockCount);
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
	return;
    }


    /** Decrease by 1 the count of processes blocked on a read.
     */
    synchronized void _readUnblock() {
	_readBlockCount--;
	return;
    }


    /** Increment by 1 the count of processes blocked while writing to a 
     *  receiver. Check for a resultant deadlock or a pausing of the 
     *  execution. If either of them is detected, then notify the directing
     *  thread of the same.
     *  @param receiver The receiver to which the blocking process was trying
     *  to write.
     */
    synchronized void _writeBlock(PNQueueReceiver receiver) {
	_writeBlockCount++;
	_writeblockedQs.insertFirst(receiver);
	//System.out.println("WriteBlockedQ "+_writeBlockCount );
	if (_checkForDeadlock() || _checkForPause()) {
	    notifyAll();
	}
	return;
    }


    /** Decrease by 1 the count of processes blocked on a write to a receiver.
     *  @param receiver The receiver to which the blocked process was trying
     *  to write.
     */
    synchronized protected void _writeUnblock(PNQueueReceiver queue) {
	_writeBlockCount--;
	_writeblockedQs.removeOneOf(queue);
	return;
    }



    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** The count of processes blocked on a read from a receiver. */
    protected int _readBlockCount = 0;

    /** The count of processes waiting for the requests for topology changes
     *  to be processed. 
     */
    protected int _mutationBlockCount = 0;

    /** The count of processes blocked on a write to a receiver. */
    protected int _writeBlockCount = 0;

    /** The list of receivers blocked on a write to a receiver. */
    protected LinkedList _writeblockedQs = new LinkedList();
}










