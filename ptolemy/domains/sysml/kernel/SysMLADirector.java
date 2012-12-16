/* Director for SysML in the style of IBM Rational Rhapsody.

 Copyright (c) 1998-2012 The Regents of the University of California.
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
package ptolemy.domains.sysml.kernel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.ProcessThread;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SysMLADirector

/**
 <p>
 Version A of a SysML director. This version is inspired by a
 subset of the semantics of IBM Rational's Rhapsody SysML tool.
 In this MoC, each actor executes in its own thread (corresponding
 to an "active object" in SysML). Inputs provided to an input
 port (by the thread of another actor) are put into a single queue
 belonging to the destination actor. The thread for the
 destination actor retrieves the first input in the queue
 and uses it to set the value of exactly one input port.
 All other input ports are marked absent. The actor then
 fires, possibly producing one or more outputs which are
 directed to their destination actors.
 <p>
 When multiple actors send tokens to an actor,
 whether to the same port or to distinct ports,
 this MoC is nondeterministic. The order in which the
 tokens are processed will depend on the happenstances
 of scheduling, since the tokens are put into a single queue
 in the order in which they arrive.
 <p>
 In this MoC, we assume that an actor iterates within its
 thread only if either it has called fireAt() to request a
 future firing (or a re-firing at the current time), or
 it has at least one event in its input queue. Thus, the
 actor's thread will block until one of those conditions
 is satisfied.
 <p>
 When all threads are blocked, then if at least one has
 called fireAt() to request a future firing, then this director
 will advance model time to the smallest time of such a request,
 and then again begin executing actors until they all block.
 <p>
 When all actors are blocked, and none has called fireAt(),
 the model terminates.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class SysMLADirector extends ProcessDirector {

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
    public SysMLADirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Clone the director into the specified workspace.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new PNDirector.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SysMLADirector newObject = (SysMLADirector) super.clone(workspace);
        newObject._threadDirectory = new ConcurrentHashMap<Actor,SingleQueueProcessThread>();
        return newObject;
    }

    /** Override the base class to make a local record of the requested
     *  firing.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param microstep The requested microstep.
     *  @return An instance of Time with the current time value, or
     *   if there is an executive director, the time at which the
     *   container of this director will next be fired
     *   in response to this request.
     *  @exception IllegalActionException If there is an executive director
     *   and it throws it. Derived classes may choose to throw this
     *   exception for other reasons.
     */
    public synchronized Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        SingleQueueProcessThread thread = _threadDirectory.get(actor);
        if (thread != null) {
            // Add the request to the list only if it not already there.
            thread.fireAtTimes.add(time);
        }
        if (_debugging) {
            _debug(actor.getFullName()
                    + " requests firing at time "
                    + time);
        }
        // The following passes the request up the hierarchy.
        if (isEmbedded()) {
            return super.fireAt(actor, time, microstep);
        }
        return time;
    }

    /** Invoke the initialize() method of ProcessDirector. Also set all the
     *  state variables to the their initial values. The list of process
     *  listeners is not reset as the developer might want to reuse the
     *  list of listeners.
     *  @exception IllegalActionException If the initialize() method of one
     *  of the deeply contained actors throws it.
     */
    public synchronized void initialize() throws IllegalActionException {
        // Recreate the collection of actors.
        _threadDirectory.clear();
        // FIXME: Should the following also be cleared in postfire()?
        // This will create the threads and start them.
        // It also initializes the _queueDirectory structure.
        super.initialize();
    }

    /** Return a new receiver SysMLAReceiver.
     *  @return A new SysMLAReceiver.
     */
    public Receiver newReceiver() {
        try {
            return new SysMLAReceiver();
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to return true if all active threads are blocked.
     *  @return True if all active threads are blocked.
     */
    protected synchronized boolean _areThreadsDeadlocked() {
        return (_getBlockedThreadsCount() >= _getActiveThreadsCount());
    }

    /** Create a new ProcessThread for controlling the actor that
     *  is passed as a parameter of this method.
     *  @param actor The actor that the created ProcessThread will
     *   control.
     *  @param director The director that manages the model that the
     *   created thread is associated with.
     *  @return Return a new ProcessThread that will control the
     *   actor passed as a parameter for this method.
     *  @exception IllegalActionException If creating a new ProcessThread
     *   throws it.
     */
    protected ProcessThread _newProcessThread(Actor actor,
            ProcessDirector director) throws IllegalActionException {
        return new SingleQueueProcessThread(actor, director);
    }

    /** Return false indicating that deadlock has not been resolved
     *  and that execution will be discontinued. In derived classes,
     *  override this method to obtain domain specific handling of
     *  deadlocks. Return false if a real deadlock has occurred and
     *  the simulation can be ended. Return true if the simulation
     *  can proceed given additional data and need not be terminated.
     *  @return False.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected synchronized boolean _resolveDeadlock() throws IllegalActionException {
        // Determine the earliest time at which an actor wants to be fired next.
        Time earliestFireAtTime = null;
        List<SingleQueueProcessThread> winningThreads = new LinkedList<SingleQueueProcessThread>();
        for (Actor otherActor : _threadDirectory.keySet()) {
            SingleQueueProcessThread otherActorThread = _threadDirectory.get(otherActor);
            
            if (otherActorThread.fireAtTimes.size() > 0) {
                Time otherTime = otherActorThread.fireAtTimes.peek();
                if (earliestFireAtTime == null || earliestFireAtTime.compareTo(otherTime) > 0) {
                    earliestFireAtTime = otherTime;
                    winningThreads.add(otherActorThread);
                }
            }
        }
        if (earliestFireAtTime == null) {
            // Time does not advance.
            if (_debugging) {
                _debug("No pending firing request. Stopping execution.");
            }
            stop();
            return false;
        }
        if (earliestFireAtTime.compareTo(getModelStopTime()) > 0) {
            // The next available time is past the stop time.
            if (_debugging) {
                _debug("Next firing request is beyond the model stop time of " + getModelStopTime());
            }
            stop();
            return false;
        }
        if (_debugging) {
            _debug("Next earliest fire at request is at time " + earliestFireAtTime);
            _debug("Setting model time to " + earliestFireAtTime);
        }
        
        // FIXME: Mark all threads unblocked now that we are at a new time?
        // Mark all threads unblocked because we will either move to a new time
        // or terminate the execution.
        // _blockedThreads.clear();
        for (SingleQueueProcessThread thread : winningThreads) {
            threadUnblocked(thread, null);
        }

        // Advance model time.
        // FIXME: This will only work if this director is at the top level.
        setModelTime(earliestFireAtTime);
                
        notifyAll();
        // Allow execution to continue.
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
        
    /** Directory of queues by actor. */
    private Map<Actor,SingleQueueProcessThread> _threadDirectory = new ConcurrentHashMap<Actor,SingleQueueProcessThread>();
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Data structure for storing inputs in an actor's queue. */
    private class Input {
        public IOPort port;
        public Token token;
    }
    
    /** A process thread that clears all input receivers, extracts one
     *  input from the input queue (if there is one), deposits that one
     *  input into the corresponding receiver, and iterates the actor.
     */
    private class SingleQueueProcessThread extends ProcessThread {
        public SingleQueueProcessThread(Actor actor, ProcessDirector director) {
            super(actor, director);
            inputQueue = Collections.synchronizedList(new LinkedList<Input>());
            // NOTE: This is not thread safe.
            fireAtTimes = new PriorityQueue<Time>();
            _threadDirectory.put(actor, this);
        }
        public List<Input> inputQueue;
        // Seems like the following ought to be a set to avoid duplicate
        // calls to fireAt() causing multiple firings. But this doesn't work.
        // Nondeterministically causes premature termination of the model!
        public PriorityQueue<Time> fireAtTimes;
        
        /** Initialize the actor, iterate it through the execution cycle
         *  until it terminates. At the end of the termination, calls wrapup
         *  on the actor.
         */
        public void run() {
            inputQueue.clear();
            super.run();
        }
        
        protected boolean _iterateActor()
                throws IllegalActionException {
            // Check whether the actor has been deleted.
            // FIXME: this is not really sufficient as the actor
            // could be in a transparent composite actor that has
            // been deleted.
            if (((Entity) _actor).getContainer() == null) {
                // Remove the actor from the active actors.
                _threadDirectory.remove(_actor);
                return false;
            }
            
            // First, clear all input receivers.
            // Record whether the actor actually has any input receivers.
            List<IOPort> inputPorts = _actor.inputPortList();
            for (IOPort inputPort : inputPorts) {
                Receiver[][] receivers = inputPort.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers.length; j++) {
                        if (receivers[i][j] != null) {
                            receivers[i][j].clear();
                        }
                    }
                }
            }
            boolean deleteTimeAfterIterating = false;
            // Block until either the input queue is non-empty or
            // a firing has been requested.
            synchronized (SysMLADirector.this) {
                while (inputQueue.size() == 0) {
                    // Input queue is empty.
                    if (_stopRequested) {
                        return false;
                    }
                    // If this actor has requested a future firing,
                    // then block until that time is reached.
                    if (fireAtTimes.size() > 0) {
                        // Actor has requested a firing. Get the time for the request.
                        Time targetTime = fireAtTimes.peek();
                        // Indicate to delete the time from the queue upon unblocking.
                        deleteTimeAfterIterating = true;
                        
                        // Wait for time to advance.
                        while (getModelTime().compareTo(targetTime) < 0) {
                            if (_stopRequested) {
                                return false;
                            }
                            if (SysMLADirector.this._debugging) {
                                SysMLADirector.this._debug(
                                        _actor.getFullName()
                                        + " blocked at time "
                                        + getModelTime()
                                        + " waiting for time to advance to "
                                        + targetTime);
                            }

                            // Notify the director that this thread is blocked.
                            threadBlocked(this, null);
                            try {
                                SysMLADirector.this.wait();
                            } catch (InterruptedException e) {
                                if (SysMLADirector.this._debugging) {
                                    SysMLADirector.this._debug(
                                            _actor.getFullName()
                                            + " thread interrupted. Requesting stop.");
                                }
                                SysMLADirector.this.stop();
                            }
                        }
                        if (SysMLADirector.this._debugging) {
                            SysMLADirector.this._debug(
                                    _actor.getFullName()
                                    + " unblocked at time "
                                    + getModelTime()
                                    + ".");
                        }
                        break; // Break out of while loop blocked on empty queue.
                    } else {
                        // Input queue is empty and no future firing
                        // has been requested. Block until the input
                        // queue is non-empty.
                        if (SysMLADirector.this._debugging) {
                            SysMLADirector.this._debug(
                                    _actor.getFullName()
                                    + " blocked at time "
                                    + getModelTime()
                                    + " waiting for input.");
                        }
                        // Second argument indicates that no particular receiver is involved.
                        threadBlocked(this, null);
                        try {
                            SysMLADirector.this.wait();
                        } catch (InterruptedException e) {
                            if (SysMLADirector.this._debugging) {
                                SysMLADirector.this._debug(
                                        _actor.getFullName()
                                        + " thread interrupted. Requesting stop.");
                            }
                            SysMLADirector.this.stop();
                        }
                    }
                }
            }

            // Either queue is non-empty, or time has passed.
            threadUnblocked(this, null);

            // Either the input queue is non-empty, or time has passed
            // to match a requested firing. If the former, then extract
            // an input from the queue and deposit in the receiver.
            if (inputQueue.size() > 0) {
                Input input = inputQueue.remove(0);
                Receiver[][] receivers = input.port.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers.length; j++) {
                        if (receivers[i][j] != null) {
                            ((SysMLAReceiver)receivers[i][j]).reallyPut(input.token);
                            if (SysMLADirector.this._debugging) {
                                synchronized (SysMLADirector.this) {
                                    SysMLADirector.this._debug(
                                            _actor.getFullName()
                                            + ": Providing input to port "
                                            + input.port.getName()
                                            + " with value: "
                                            + input.token);
                                }
                            }
                        }
                    }
                }
            }
            
            // Now, finally, actually iterate the actor.
            // Note that actor may have an empty input queue now,
            // and also the input ports may not have any data.
            // Catch any exceptions so that we can be sure to
            // remove the actor from the active list.
            try {
                if (SysMLADirector.this._debugging) {
                    synchronized (SysMLADirector.this) {
                        SysMLADirector.this._debug(
                                _actor.getFullName()
                                + ": Iterating.");
                    }
                }

                boolean result = super._iterateActor();
                
                if (deleteTimeAfterIterating) {
                    // After iterating the actor, if in fact the input queue
                    // was empty and this firing was caused by time advancing to
                    // match the requested time.
                    fireAtTimes.poll();
                }

                if (result == false) {
                    // Postfire returned false. Remove the actor from
                    // the active actors.
                    synchronized(SysMLADirector.this) {
                        if (SysMLADirector.this._debugging) {
                            SysMLADirector.this._debug(
                                    _actor.getFullName()
                                    + " postfire() returns false. Ending thread.");
                        }
                        removeThread(this);
                        _threadDirectory.remove(_actor);
                        SysMLADirector.this.notifyAll();
                    }
                }
                return result;
            } catch (Throwable ex) {
                // Actor threw an exception.
                synchronized(SysMLADirector.this) {
                    removeThread(this);
                    _threadDirectory.remove(_actor);
                    SysMLADirector.this.stop();
                    SysMLADirector.this.notifyAll();
                }
                if (ex instanceof IllegalActionException) {
                    throw (IllegalActionException)ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException)ex;
                }
                return false;
            }
        }
    }
    
    /** Variant of a Mailbox that overrides the put() method to
     *  divert the input to the queue associated with the actor
     *  and then provides a method to really put a token into
     *  a receiver.
     */
    private class SysMLAReceiver extends Mailbox {
        public SysMLAReceiver() throws IllegalActionException {
            this(null);
        }
        public SysMLAReceiver(IOPort container) throws IllegalActionException {
            super(container);
        }

        /** Put a token into the queue for containing actor.
         *  @param token The token to be put into the queue.
         */
        public void put(Token token) {
            IOPort port = getContainer();
            Actor actor = (Actor)port.getContainer();
            SingleQueueProcessThread thread = _threadDirectory.get(actor);
            if (thread != null) {
                List<Input> queue = thread.inputQueue;
                Input input = new Input();
                input.port = port;
                input.token = token;
                // Notify the director that this queue is not empty.
                synchronized(SysMLADirector.this) {
                    queue.add(input);
                    threadUnblocked(thread, null);
                    SysMLADirector.this.notifyAll();
                }
            }
        }
        
        /** Put a token into the mailbox.
         *  @param token The token to be put into the mailbox.
         *  @exception NoRoomException If this mailbox is not empty.
         */
        public void reallyPut(Token token) throws NoRoomException {
            super.put(token);
        }
    }
}
