/* Director for Kahn-MacQueen process network semantics.

Copyright (c) 1998-2005 The Regents of the University of California.
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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IORelation;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.CompositeProcessDirector;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.pn.kernel.event.PNProcessListener;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// PNDirector

/**
   A PNDirector governs the execution of a CompositeActor with extended
   Kahn-MacQueen process networks (PN) semantics. This model of computation has
   been extended to support mutations of graphs in a non-deterministic way.
   <p>
   The thread that calls the various execution methods (initialize, prefire, fire
   and postfire) on the director is referred to as the <i>directing thread</i>.
   This directing thread might be the main thread responsible for the execution
   of the entire simulation or might be the thread created by the executive
   director of the containing composite actor.
   <p>
   In the PN domain, the director creates a thread (an instance of
   ProcessThread), representing a Kahn process, for each actor in the model.
   The threads are created in initialize() and started in the prefire() method
   of the ProcessDirector. A process is considered <i>active</i> from its
   creation until its termination. An active process can block when trying to
   read from a channel (read-blocked), when trying to write to a channel
   (write-blocked) or when waiting for a queued topology change request to be
   processed (mutation-blocked).
   <p>
   A <i>deadlock</i> is when all the active processes are blocked.
   The director is responsible for handling deadlocks during execution.
   This director handles two different sorts of deadlocks, <i>real deadlock</i>
   and <i>artificial deadlock</i>.
   <p>
   A real deadlock is when all the processes are blocked on a read meaning that
   no process can proceed until it receives new data. The execution can be
   terminated, if desired, in such a situation. If the container of this director
   does not have any input ports (as is in the case of a top-level composite
   actor), then the executive director or manager terminates the execution.
   If the container has input ports, then it is up to the
   executive director of the container to decide on the termination of the
   execution. To terminate the execution after detection of a real deadlock, the
   manager or the executive director calls wrapup() on the director.
   <p>
   An artificial deadlock is when all processes are blocked and at least one
   process is blocked on a write. In this case the director increases the
   capacity of the receiver with the smallest capacity amongst all the
   receivers on which a process is blocked on a write.
   This breaks the deadlock and the execution can resume.
   If the increase results in a capacity that exceeds the value of
   <i>maximumQueueCapacity</i>, then instead of breaking the deadlock,
   an exception is thrown.  This can be used to detect erroneous models
   that require unbounded queues.

   @author Mudit Goel, Edward A. Lee, Xiaowen Xin
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (mudit)
   @Pt.AcceptedRating Green (davisj)
*/
public class PNDirector extends CompositeProcessDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  Create a director parameter "initialQueueCapacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PNDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  Create a director parameter "initialQueueCapacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @param workspace The workspace of this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PNDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  Create a director parameter "initialQueueCapacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  Thrown in derived classes.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public PNDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The initial size of the queues for each communication channel.
     *  This is an integer that defaults to 1.
     */
    public Parameter initialQueueCapacity;

    /** The maximum size of the queues for each communication channel.
     *  This is an integer that defaults to 65536.  To specify unbounded
     *  queues, set this to 0.
     */
    public Parameter maximumQueueCapacity;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a process state change listener to this director. The listener
     *  will be notified of each change to the state of a process.
     *  @param listener The PNProcessListener to add.
     *  @see #removeProcessListener(PNProcessListener)
     */
    public void addProcessListener(PNProcessListener listener) {
        _processListeners.add(listener);
    }

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active processes is zero.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new PNDirector.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PNDirector newObject = (PNDirector) super.clone(workspace);
        newObject._readBlockCount = 0;
        newObject._writeBlockCount = 0;
        newObject._writeBlockedQueues = new LinkedList();
        return newObject;
    }

    /** Invoke the initialize() method of ProcessDirector. Also set all the
     *  state variables to the their initial values. The list of process
     *  listeners is not reset as the developer might want to reuse the
     *  list of listeners.
     *  @exception IllegalActionException If the initialize() method of one
     *  of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
        // Initialize these counts BEFORE creating threads.
        _readBlockCount = 0;
        _writeBlockCount = 0;
        _writeBlockedQueues = new LinkedList();

        super.initialize();
    }

    /** Return a new receiver compatible with this director. The receiver
     *  is an instance of PNQueueReceiver. Set the initial capacity
     *  of the FIFO queue in the receiver to the value specified by the
     *  director parameter "initialQueueCapacity". The default value
     *  of the parameter is 1.
     *  @return A new PNQueueReceiver.
     */
    public Receiver newReceiver() {
        PNQueueReceiver receiver = new PNQueueReceiver();
        _receivers.add(new WeakReference(receiver));

        // Set the capacity to the default. Note that it will also
        // be set in preinitialize().
        try {
            int capacity = ((IntToken) initialQueueCapacity.getToken())
                .intValue();
            receiver.setCapacity(capacity);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        return receiver;
    }

    /** Return true if the containing composite actor contains active
     *  processes and the composite actor has input ports and if stop()
     *  has not been called. Return false otherwise. This method is
     *  normally called only after detecting a real deadlock, or if
     *  stopFire() is called. True is returned to indicate that the
     *  composite actor can start its execution again if it
     *  receives data on any of its input ports.
     *  @return true to indicate that the composite actor can continue
     *  executing on receiving additional input on its input ports.
     *  @exception IllegalActionException Not thrown in this base class. May be
     *  thrown by derived classes.
     */
    public boolean postfire() throws IllegalActionException {
        _notDone = super.postfire();

        // If the container has input ports and there are active processes
        // in the container, then the execution might restart on receiving
        // additional data.
        if (!((((CompositeActor) getContainer()).inputPortList()).isEmpty())
                && (_getActiveActorsCount() != 0)) {
            // Avoid returning false on detected deadlock.
            return !_stopRequested;
        } else {
            return _notDone;
        }
    }

    /** Override the base class to reset the capacities of all the receivers.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        
        // Check that no relation has multiple sources of data connected to it.
        // FIXME: This only detects the error at this level of the hierarchy.
        // Probably need to recursively descend into composite actors.
        CompositeEntity container = (CompositeEntity)getContainer();
        Iterator relations = container.relationList().iterator();
        while (relations.hasNext()) {
        	IORelation relation = (IORelation)relations.next();
            if (relation.linkedSourcePortList().size() > 1) {
                throw new IllegalActionException(relation,
                        "Relation has multiple sources of data,"
                        + " which is not allowed in PN."
                        + " If you want nondeterministic merge,"
                        + " use the NondeterministicMerge actor.");   
            }
        }

        // Reset the capacities of all the receivers.
        Parameter parameter = (Parameter) getAttribute("initialQueueCapacity");
        int capacity = ((IntToken) parameter.getToken()).intValue();
        ListIterator receivers = _receivers.listIterator();

        while (receivers.hasNext()) {
            WeakReference reference = (WeakReference) receivers.next();

            if (reference.get() == null) {
                // Reference has been garbage collected.
                receivers.remove();
            } else {
                PNQueueReceiver receiver = (PNQueueReceiver) reference.get();
                receiver.clear();
                receiver.setCapacity(capacity);
            }
        }
    }

    /** Remove a process listener from this director.
     *  If the listener is not attached to this director, do nothing.
     *
     *  @param listener The PNProcessListener to be removed.
     *  @see #addProcessListener(PNProcessListener)
     */
    public void removeProcessListener(PNProcessListener listener) {
        _processListeners.remove(listener);
    }

    /** Return an array of suggested ModalModel directors  to use with
     *  PNDirector. The default director is MultirateFSMDirector, the
     *  alternative director is FSMDirector.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    public String[] suggestedModalModelDirectors() {
        return new String [] {
            "ptolemy.domains.fsm.kernel.MultirateFSMDirector",
            "ptolemy.domains.fsm.kernel.FSMDirector"};
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Increase the count of stopped actors by one.  This method is
     *  called by instances of ProcessThread in response to a call to
     *  their stopThread() method. This method may be overridden in
     *  derived classes to added domain specific
     *  functionality. Implementations of this method must be
     *  synchronized.
     */
    protected synchronized void _actorHasStopped() {
        // This method is here only to make it visible within the package.
        super._actorHasStopped();
    }

    /** Decrease the count of stopped actors by one.  This method is
     *  called by instances of ProcessThread after detecting that the
     *  stopFire() flag has been cleared. This method may be
     *  overridden in derived classes to added domain specific
     *  functionality. Implementations of this method must be
     *  synchronized.
     */
    protected synchronized void _actorHasRestarted() {
        // This method is here only to make it visible within the package.
        super._actorHasRestarted();
    }

    /** Decrease by one the count of active processes under the control of
     *  this director.
     *  This method should be called only when an active thread that was
     *  registered using _increaseActiveCount() is terminated.
     *  This count is used to detect deadlocks for termination and other
     *  reasons.
     */
    protected synchronized void _decreaseActiveCount() {
        // This method is here only to make it visible within the package.
        super._decreaseActiveCount();
    }

    /** Increase the count of active actors in the composite actor
     *  corresponding to this director by 1. This method should be
     *  called when a new thread corresponding to an actor is started
     *  in the model under the control of this director. This method
     *  is required for detection of deadlocks.
     *  The corresponding method _decreaseActiveCount should be called
     *  when the thread is terminated.
     */
    protected synchronized void _increaseActiveCount() {
        // This method is here only to make it visible within the package.
        super._increaseActiveCount();
    }

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
     *  @exception IllegalActionException If the resulting capacity would
     *   exceed the value of <i>maximumQueueCapacity</i>.
     */
    protected synchronized void _incrementLowestWriteCapacityPort()
            throws IllegalActionException {
        // NOTE: This is synchronized as a precaution, although in theory
        // it gets called only within a synchronized block of the fire()
        // method of the parent ProcessDirector. It must be synchronized
        // because of the notifyAll() call at the end.
        PNQueueReceiver smallestCapacityQueue = null;
        int smallestCapacity = -1;
        Iterator receivers = _writeBlockedQueues.iterator();

        if (!receivers.hasNext()) {
            return;
        }

        while (receivers.hasNext()) {
            PNQueueReceiver queue = (PNQueueReceiver) receivers.next();

            if (smallestCapacity == -1) {
                smallestCapacityQueue = queue;
                smallestCapacity = queue.getCapacity();
            } else if (smallestCapacity > queue.getCapacity()) {
                smallestCapacityQueue = queue;
                smallestCapacity = queue.getCapacity();
            }
        }

        int capacity = smallestCapacityQueue.getCapacity();

        if (capacity <= 0) {
            smallestCapacityQueue.setCapacity(1);
            capacity = 1;
        } else {
            int maximumCapacity = ((IntToken) maximumQueueCapacity.getToken())
                .intValue();

            if ((maximumCapacity > 0) && ((capacity * 2) > maximumCapacity)) {
                String msg =
                        "Queue size exceeds the maximum capacity in port "
                        + smallestCapacityQueue.getContainer().getFullName()
						+ ". Perhaps you have an unbounded queue?";
                if (_debugging) {
                	_debug(msg);
                }
                throw new IllegalActionException(this, msg);
            }

            smallestCapacityQueue.setCapacity(capacity * 2);
        }

        if (_debugging) {
            _debug("increasing the capacity of receiver "
                    + smallestCapacityQueue.getContainer() + " to "
                    + smallestCapacityQueue.getCapacity());
        }

        smallestCapacityQueue.setWritePending();
        notifyAll();

        return;
    }

    /** Increment by 1 the count of processes blocked while reading from a
     *  receiver and notify all process listeners of the blocking of the
     *  process. Check for a deadlock or pausing of the execution as a result
     *  of the process blocking on a read. If either of them is detected,
     *  then notify the directing thread of the same.
     *  @param receiver The receiver that is blocked.
     *  @param readBlocked Whether this is read or write blocked.
     */
    protected synchronized void _actorBlocked(
        PNQueueReceiver receiver, boolean readBlocked) {
        if(readBlocked) {
            _readBlockCount++;
        } else {
            _writeBlockCount++;
            _writeBlockedQueues.add(receiver);
        }

        super._actorBlocked(receiver);
        notifyAll();
    }

    /** Decrease by 1 the count of processes blocked on a read and inform all
     *  the process listeners that the relevant process has resumed its
     *  execution.
     *  @param receiver The receiver that is unblocked.
     *  @param readBlocked Whether this is read or write blocked.
     */
    protected synchronized void _actorUnBlocked(
        PNQueueReceiver receiver, boolean readBlocked) {
        if(readBlocked) {
            _readBlockCount--;
        } else {
            _writeBlockCount--;
            _writeBlockedQueues.remove(receiver);
        }

        super._actorUnBlocked(receiver);
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
     *  <p>
     *  If derived classes introduce new forms of deadlocks, they should
     *  override this method to introduce mechanisms of handling those
     *  deadlocks. This method is called from the fire() method of the director
     *  alone.
     *  @return True after handling an artificial deadlock. Otherwise return
     *  false.
     *  @exception IllegalActionException If the maximum queue capacity
     *   is exceeded.
     *  This might be thrown by derived classes.
     */
    protected boolean _resolveInternalDeadlock() throws IllegalActionException {
        if ((_writeBlockCount == 0) && (_readBlockCount > 0)) {
            // There is a real deadlock.
            if (_debugging) {
                _debug("Deadlock detected: no processes blocked on write, but some are blocked on read.");
            }
            return false;
        } else if (_getActiveActorsCount() == 0) {
            // There is a real deadlock as no processes are active.
            if (_debugging) {
                _debug("No more active processes.");
            }

            return false;
        } else {
            //This is an artificial deadlock. Hence find the input port with
            //lowest capacity queue that is blocked on a write and increment
            //its capacity;
            if (_debugging) {
                _debug("Artificial Deadlock - increasing queue capacity.");
            }

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

    /** The count of processes blocked on a write to a receiver. */
    protected int _writeBlockCount = 0;

    /** The list of receivers blocked on a write to a receiver. */
    protected LinkedList _writeBlockedQueues = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _init()
            throws IllegalActionException, NameDuplicationException {
        initialQueueCapacity = new Parameter(this, "initialQueueCapacity",
                new IntToken(1));
        initialQueueCapacity.setTypeEquals(BaseType.INT);

        maximumQueueCapacity = new Parameter(this, "maximumQueueCapacity",
                new IntToken(65536));
        maximumQueueCapacity.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of process listeners. */
    private LinkedList _processListeners = new LinkedList();
    
    /** The list of all receivers that this director has created. */
    private LinkedList _receivers = new LinkedList();
}
