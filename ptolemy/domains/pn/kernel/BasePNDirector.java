/* Base class for governing the execution of a CompositeActor with restricted
Kahn-MacQueen process network semantics.

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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Green (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.kernel;
import ptolemy.kernel.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;
import ptolemy.domains.pn.kernel.event.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

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
This directing thread might be the main thread responsible for the execution
of the entire simulation or might be the thread created by the executive
director of the containing composite actor.
<p>
In the PN domain, the director creates a thread (an instance of
ProcessThread) representing a Kahn process, for each actor in the model.
The threads are created in initialize() and started in the prefire() method
of the ProcessDirector. A process is considered <i>active</i> from its
creation until its termination. An active process can block when trying to
read from a channel (<i>read-blocked</i>) or when trying to write to a channel
(<i>write-blocked</i>). Derived directors can define additional ways in which
active processes can block.
<p>
A <i>deadlock</i> is when all the active processes are blocked.
The director is responsible for handling deadlocks during execution.
This director handles two different sorts of deadlocks, <i>real deadlock</i>
and <i>artificial deadlock</i>. Derived directors might introduce additional
forms of deadlock.
<p>
A real deadlock is when all the processes are blocked on a read meaning that
no process can proceed until it receives new data. The execution can be
terminated, if desired, in such a situation. If the container of this director
does not have any input ports (as is in the case of a top-level composite
actor), then the executive director or manager terminates the execution.
If the container has input ports, then it is upto the
executive director of the container to decide on the termination of the
execution. To terminate the execution after detection of a real deadlock, the
manager or the executive director calls wrapup() on the director.
<p>
An artificial deadlock is when all processes are blocked and at least one
process is blocked on a write. In this case the director increases the
capacity of the receiver with the smallest capacity amongst all the
receivers on which a process is blocked on a write.
This breaks the deadlock and the execution can resume.
<p>
This base director does not support any form of mutations or topology changes
but provides the basic infrastructure in the form of methods that the derived
directors can use.
<p>
This director also permits pausing of the execution. An execution is paused
when all active processes are blocked or paused (at least one process is
paused). In case of PN, a process can be paused only when it tries to
communicate with other processes. Thus a process can be paused in the get()
or put() methods of the receivers alone. If there is a process that does
not communicate with other processes in the model, then the execution can
never pause in that model.
<p>
Though this class defines and uses a event-listener mechanism for notifying
the listeners of the various states a process is in, this mechanism is expected
to change to a great extent in the later versions of this class. A developer
must keep that in mind while building applications by using this mechanism. It
is highly recommended that the user do not use this mechanism as the future
changes might not be compatible with the current listener mechanism.<p>


@author Mudit Goel
@version $Id$
*/
public class BasePNDirector extends ProcessDirector {

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
            //As parameter is a valid parameter, this exception is not thrown
            throw new InternalErrorException(e.toString());
        } catch (NameDuplicationException e) {
            //As this is being called from the constructor, we cannot have
            //a parameter by the same name already existing in the object
            throw new InvalidStateException(e.toString());
        }
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  Create a director parameter "Initial_queue_capacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @param workspace The workspace of this object.
     */
    public BasePNDirector(Workspace workspace) {
        super(workspace);
        try {
            Parameter param = new Parameter(this,"Initial_queue_capacity",
                    new IntToken(1));
        } catch (IllegalActionException e) {
            //As parameter is a valid parameter, this exception is not thrown
            throw new InternalErrorException(e.toString());
        } catch (NameDuplicationException e) {
            //As this is being called from the constructor, we cannot have
            //a parameter by the same name already existing in the object
            throw new InvalidStateException(e.toString());
        }
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  Create a director parameter "Initial_queue_capacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  Thrown in derived classes.
     */
    public BasePNDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
        try {
            Parameter param = new Parameter(this,"Initial_queue_capacity",
                    new IntToken(1));
        } catch (IllegalActionException e) {
            //As parameter is a valid parameter, this exception is not thrown
            throw new InternalErrorException(e.toString());
        } catch (NameDuplicationException e) {
            //As this is being called from the constructor, we cannot have
            //a parameter by the same name already existing in the object
            throw new InvalidStateException(e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a process state change listener to this director. The listener
     *  will be notified of each change to the state of a process.
     *  @param listener The PNProcessListener to add.
     */
    public void addProcessListener(PNProcessListener listener) {
        _processlisteners.add(listener);
    }

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active processes is zero
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
        newobj._writeblockedQueues = new LinkedList();
        return newobj;
    }

    /** Wait until the detection of a deadlock. If the deadlock is real, then
     *  return. Else (for an artificial deadlock), handle the deadlock by
     *  incrementing the capacity of a receiver with the smallest capacity
     *  amongst the receivers on which a process is blocked on a write.
     *  The derived directors can override this method to handle other forms
     *  of deadlock that they define and perform actions accordingly.
     *  This method is synchronized on the director.
     *  @exception IllegalActionException Not thrown in this base class. Maybe
     *  thrown by derived classes.
     */
    public void fire() throws IllegalActionException {
	Workspace workspace = workspace();
	// Wait while no deadlock is detected.
	while (_readBlockCount != _getActiveActorsCount()) {
	    //In this case, wait until a real deadlock occurs.
	    synchronized (this) {
		while (!_isDeadlocked()) {
		    //Wait until a deadlock is detected.
		    workspace.wait(this);
		}
		//Set this flag as a derived class might use this variable.
		_notDone = _resolveDeadlock();
	    }
	}
        return;
    }

    /** Invoke the initialize() method of ProcessDirector. Also set all the
     *  state variables to the their initial values. The list of process
     *  listeners is not reset as the developer might want to reuse the
     *  list of listeners.
     *  @exception IllegalActionException If the initialize() method of one
     *  of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
        _readBlockCount = 0;
	_mutationBlockCount = 0;
	_writeBlockCount = 0;
	_writeblockedQueues = new LinkedList();
        //processlisteners is not initialized as we might want to continue
        //with the same listeners.
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

    /** Return true if the containing composite actor contains active
     *  processes and the composite actor has input ports. Return
     *  false otherwise. This method should normally be called only after
     *  detecting a real deadlock. True is returned to indicate that the
     *  composite actor can start its execution again if it receives data on
     *  any of its input ports.
     *  @return true to indicate that the composite actor can continue
     *  executing on receiving additional input on its input ports.
     *  @exception IllegalActionException Not thrown in this base class. May be
     *  thrown by derived classes.
     */
    public boolean postfire() throws IllegalActionException {
	//If the container has input ports and there are active processes
	//in the container, then the execution might restart on receiving
	// additional data.
	if (!((((CompositeActor)getContainer()).
		inputPortList()).isEmpty())
		&& _getActiveActorsCount() != 0 ){
	    return true;
	} else {
	    return false;
	}
    }

    /** Remove a process listener from this director.
     *  If the listener is not attached to this director, do nothing.
     *
     *  @param listener The PNProcessListener to be removed.
     */
    public void removeProcessListener(PNProcessListener listener) {
        _processlisteners.remove(listener);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Decrease by 1 the count of active processes under the control of this
     *  director. Also check whether this results in the model arriving at a
     *  deadlock or a pause. If either of these is detected, then notify
     *  the directing thread of the same. Inform all the process listeners
     *  that the relevant process has finished its execution.
     */
    protected synchronized void _decreaseActiveCount() {
	super._decreaseActiveCount();
        if (!_processlisteners.isEmpty()) {
            ProcessThread pro = (ProcessThread)Thread.currentThread();
            Actor actor = pro.getActor();
            PNProcessEvent event = new PNProcessEvent(actor,
                    PNProcessEvent.PROCESS_FINISHED,
		    PNProcessEvent.FINISHED_PROPERLY);
            Iterator enum = _processlisteners.iterator();
            while (enum.hasNext()) {
                PNProcessListener lis = (PNProcessListener)enum.next();
                lis.processFinished(event);
            }
        }
    }

    /** Handle (resolve) an artificial deadlock and return false. If the
     *  deadlock is not an artificial deadlock (it is a real deadlock), then
     *  return true.
     *  If it is an artificial deadlock, select the
     *  receiver with the smallest queue capacity on which any process is
     *  blocked on a write and increment the capacity of the contained queue.
     *  If the capacity is non-negative, then increment the capacity by 1.
     *  Otherwise set the capacity to 1. Unblock the process blocked on
     *  this receiver. Notify the thread corresponding to the blocked
     *  process and return false.
     *  <pP
     *  If derived classes introduce new forms of deadlocks, they should
     *  override this method to introduce mechanisms of handling those
     *  deadlocks. This method is called from the fire() method of the director
     *  alone.
     *  @return false after handling an artificial deadlock. Otherwise return
     *  true.
     *  @exception IllegalActionException Not thrown in this base class. This
     *  might be thrown by derived classes.
     protected boolean _handleDeadlock() throws IllegalActionException {
     if (_writeBlockCount == 0) {
     //There is a real deadlock. Hence return with a value true.
     return true;
     } else {
     //This is an artificial deadlock. Hence find the input port with
     //lowest capacity queue that is blocked on a write and increment
     //its capacity;
     _incrementLowestWriteCapacityPort();
     return false;
     }
     }
    */

    /** Double the capacity of one of the queues with the smallest
     *  capacity belonging to a receiver on which a process is blocked
     *  while attempting to write. <p>Traverse through the list of receivers
     *  on which a process is blocked on a write and choose the one containing
     *  the queue with the smallest capacity. Double the capacity
     *  if the capacity is non-negative. In case the capacity is
     *  negative, set the capacity to 1.
     *  Unblock the process blocked on a write to the receiver containing this
     *  queue.
     *  Notify the thread corresponding to the blocked process to resume
     *  its execution and return.
     */
    protected void _incrementLowestWriteCapacityPort() {
        PNQueueReceiver smallestCapacityQueue = null;
        int smallestCapacity = -1;
	Iterator receivers = _writeblockedQueues.iterator();
	while (receivers.hasNext()) {
	    PNQueueReceiver queue = (PNQueueReceiver)receivers.next();
	    if (smallestCapacity == -1) {
	        smallestCapacityQueue = queue;
		smallestCapacity = queue.getCapacity();
	    } else if (smallestCapacity > queue.getCapacity()) {
	        smallestCapacityQueue = queue;
	        smallestCapacity = queue.getCapacity();
	    }
	}
        try {
            if (smallestCapacityQueue.getCapacity() <= 0) {
                smallestCapacityQueue.setCapacity(1);
            } else {
	        smallestCapacityQueue.setCapacity(
			smallestCapacityQueue.getCapacity()*2);
            }
	    _informOfWriteUnblock(smallestCapacityQueue);
	    smallestCapacityQueue.setWritePending(false);
            synchronized(smallestCapacityQueue) {
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
     *  use by the deriveed classes.
     */
    protected synchronized void _informOfMutationBlock() {
	_mutationBlockCount++;
	if (_isDeadlocked() || _isPaused()) {
	    notifyAll();
	}
	return;
    }

    /** Decrement the count of processes waiting for the topology change
     *  requests (mutation requests) to be processed. This method is not used
     *  by the base director and is provided for use by the derived classes.
     */
    protected synchronized void _informOfMutationUnblock() {
	_mutationBlockCount--;
	return;
    }


    /** Increment by 1 the count of processes blocked while reading from a
     *  receiver and notify all process listeners of the blocking of the
     *  process. Check for a deadlock or pausing of the execution as a result
     *  of the process blocking on a read. If either of them is detected,
     *  then notify the directing thread of the same.
     */
    protected synchronized void _actorReadBlocked(boolean internal) {
	_readBlockCount++;
        /*
        if (!_processlisteners.isEmpty()) {
            Actor actor = receiver.getReadBlockedActor();
            PNProcessEvent event = new PNProcessEvent(actor,
                    PNProcessEvent.PROCESS_BLOCKED,
                    PNProcessEvent.BLOCKED_ON_READ);
            Iterator enum = _processlisteners.iterator();
            while (enum.hasNext()) {
                PNProcessListener lis = (PNProcessListener)enum.next();
                lis.processStateChanged(event);
            }
        }
        */
	if (_isDeadlocked() || _isPaused()) {
	    notifyAll();
	}
	return;
    }


    /** Decrease by 1 the count of processes blocked on a read and inform all
     *  the process listeners that the relevant process has resumed its
     *  execution.
     */
    protected synchronized void _informOfReadUnblock
    (PNQueueReceiver receiver, boolean internal) {
	_readBlockCount--;
        /*
        if (!_processlisteners.isEmpty()) {
            Actor actor = receiver.getReadBlockedActor();
            PNProcessEvent event = new PNProcessEvent(actor,
                    PNProcessEvent.PROCESS_RUNNING);
            Iterator enum = _processlisteners.iterator();
            while (enum.hasNext()) {
                PNProcessListener lis = (PNProcessListener)enum.next();
                lis.processStateChanged(event);
            }
        }
        */
	return;
    }


    /** Increment by 1 the count of processes blocked while writing to a
     *  receiver and inform all the process listeners that the relevant process
     *  has blocked on a write. Also check for a resultant deadlock or a
     *  pausing of the
     *  execution. If either of them is detected, then notify the directing
     *  thread of the same.
     *  @param receiver The receiver to which the blocking process was trying
     *  to write.
     */
    protected synchronized void _actorWriteBlocked(PNQueueReceiver receiver) {
	_writeBlockCount++;
        // FIXME: is add correct or should it have been addFirst
        // used to be insertFirst
	_writeblockedQueues.add(receiver);
        /*
        //Inform the listeners
        if (!_processlisteners.isEmpty()) {
            Actor actor = receiver.getWriteBlockedActor();
            PNProcessEvent event = new PNProcessEvent(actor,
                    PNProcessEvent.PROCESS_BLOCKED,
                    PNProcessEvent.BLOCKED_ON_WRITE);
            Iterator enum = _processlisteners.iterator();
            while (enum.hasNext()) {
                PNProcessListener lis = (PNProcessListener)enum.next();
                lis.processStateChanged(event);
            }
        }
        */
	if (_isDeadlocked() || _isPaused()) {
	    notifyAll();
	}
	return;
    }



    /** Decrease by 1 the count of processes blocked on a write to a receiver.
     *  Inform all the process listeners that the relevant process has resumed
     *  its execution.
     *
     *  @param receiver The receiver to which the blocked process was trying
     *  to write.
     */
    protected synchronized void _informOfWriteUnblock(PNQueueReceiver queue) {
	_writeBlockCount--;
	_writeblockedQueues.remove(queue);
        if (!_processlisteners.isEmpty()) {
            Actor actor = queue.getWriteBlockedActor();
            PNProcessEvent event = new PNProcessEvent(actor,
                    PNProcessEvent.PROCESS_RUNNING);
            Iterator enum = _processlisteners.iterator();
            while (enum.hasNext()) {
                PNProcessListener lis = (PNProcessListener)enum.next();
                lis.processStateChanged(event);
            }
        }
	return;
    }

    /** Return true if a real or artificial deadlock is detected.
     *  If derived classes introduce any
     *  additional forms of deadlocks, they should override this method to
     *  return true on detection of the introduced deadlocks.
     *  This method is synchronized on this object.
     *  @return true if a real or artificial deadlock is detected.
     */
    protected synchronized boolean _isDeadlocked() {
	if (_readBlockCount + _writeBlockCount >= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }


    /** Return true if the execution has paused. This base class returns true
     *  if all the active processes are either blocked (on a read or a write)
     *  or paused. Derived classes should override this method if they
     *  introduce any new forms of blocking of processes.
     *  @return true if the execution has paused.
     */
    protected synchronized boolean _isPaused() {
	if (_readBlockCount + _writeBlockCount + _getPausedActorsCount()
		>= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Resolve an artificial deadlock and return true. If the
     *  deadlock is not an artificial deadlock (it is a real deadlock),
     *  then return false.
     *  If it is an artificial deadlock, select the
     *  receiver with the smallest queue capacity on which any process is
     *  blocked on a write and increment the capacity of the contained queue.
     *  If the capacity is non-negative, then increment the capacity by 1.
     *  Otherwise set the capacity to 1. Unblock the process blocked on
     *  this receiver. Notify the thread corresponding to the blocked
     *  process and return true.
     *  <pP
     *  If derived classes introduce new forms of deadlocks, they should
     *  override this method to introduce mechanisms of handling those
     *  deadlocks. This method is called from the fire() method of the director
     *  alone.
     *  @return True after handling an artificial deadlock. Otherwise return
     *  false.
     *  @exception IllegalActionException Not thrown in this base class.
     *  This might be thrown by derived classes.
     */
    protected boolean _resolveDeadlock() throws IllegalActionException {
        if (_writeBlockCount == 0) {
	    //There is a real deadlock.
	    return false;
        } else {
            //This is an artificial deadlock. Hence find the input port with
	    //lowest capacity queue that is blocked on a write and increment
	    //its capacity;
            _incrementLowestWriteCapacityPort();
	    return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    //The variables are initialized at declaration, despite having an
    //initialize() method so that the tests can be run.

    /** The count of processes blocked on a read from a receiver. */
    protected int _readBlockCount = 0;

    /** The count of processes waiting for the requests for topology changes
     *  to be processed.
     */
    protected int _mutationBlockCount = 0;

    /** The count of processes blocked on a write to a receiver. */
    protected int _writeBlockCount = 0;

    /** The list of receivers blocked on a write to a receiver. */
    protected LinkedList _writeblockedQueues = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList _processlisteners = new LinkedList();
}
