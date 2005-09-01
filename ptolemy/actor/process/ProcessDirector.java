/* The base class for directors for the process oriented domains.

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

 Semantics of initialize(Actor) have changed.
 */
package ptolemy.actor.process;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ProcessDirector

/**
 The base class for directors for the process oriented domains. It provides
 default implementations for methods that are common across such domains.
 <P>
 In the process oriented domains, the director controlling a model
 needs to keep track of the state of the model. In particular it needs
 to maintain an accurate count of the number of active processes under
 its control and any processes that are blocked for whatever reason (trying
 to read from an empty channel as in PN).
 These counts, and perhaps other counts, are needed by the
 director to control and respond when deadlock is detected (no processes
 can make progress), or to respond to requests from higher in the hierarchy.
 <P>
 The methods that control how the director detects and responds to deadlocks
 are _areActorsDeadlocked() and _resolveDeadlock(). These methods should be
 overridden in derived classes to get domain-specific behaviour. The
 implementations given here are trivial and suffice only to illustrate
 the approach that should be followed.
 <P>
 This base class is not sufficient for executing hierarchical, heterogeneous
 models. In order to accommodate hierarchical, heterogeneity the subclass
 CompositeProcessDirector must be used.
 <P>
 <P>
 @author Mudit Goel, Neil Smyth, John S. Davis II
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (mudit)
 @Pt.AcceptedRating Yellow (mudit)
 @see Director
 */
public class ProcessDirector extends Director {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public ProcessDirector() {
        super();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public ProcessDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container The container
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name contains a period,
     *   or if the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public ProcessDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify this director that the specified thread is part of
     *  the execution of this model. This is used
     *  to keep track of whether the model is deadlocked, and also to
     *  terminate threads if necessary. It is important that the thread
     *  call _removeThread() upon exiting. Note further that this
     *  should be called before the thread is started to avoid race
     *  conditions where some threads have been started and others
     *  have not been started and deadlock is falsely detected because
     *  the not-yet-started threads are not counted.
     *  @param thread The thread.
     *  @see #removeThread(Thread)
     */
    public synchronized void addThread(Thread thread) {
        _activeThreads.add(thread);
        if (_debugging) {
            _debug("Adding a thread: " + thread.getName());
        }
        notifyAll();
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
     *  @return The new ProcessDirector.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ProcessDirector newObject = (ProcessDirector) super.clone(workspace);
        // Is it really necessary to do this?
        newObject._blockedThreads = new HashSet();
        newObject._pausedThreads = new HashSet();
        newObject._activeThreads = new HashSet();
        newObject._notDone = true;
        return newObject;
    }

    /** Wait until a deadlock is detected. Then deal with the deadlock
     *  by calling the protected method _resolveDeadlock() and return.
     *  This method is synchronized on the director.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire().");
        }

        Workspace workspace = workspace();
        
        // In case we have an eclosing process director,
        // we identify it so that we can notify it when we are blocked.
        CompositeActor container = (CompositeActor)getContainer();
        Director outsideDirector = container.getExecutiveDirector();
        if (!(outsideDirector instanceof ProcessDirector)) {
            outsideDirector = null;
        }

        synchronized (this) {
            while (!_areActorsDeadlocked() && !_areAllActorsStopped() && !_stopRequested) {
                // Added to get thread to stop reliably on pushing stop button.
                // EAL 8/05
                if (_stopRequested) {
                    return;
                }
                if (_debugging) {
                    _debug("Waiting for actors to stop.");
                }

                try {
                    if (outsideDirector != null) {
                        ((ProcessDirector)outsideDirector).threadBlocked(
                                Thread.currentThread(), null);
                    }
                    workspace.wait(this);
                } catch (InterruptedException e) {
                    // stop all threads
                    stop();
                    return;
                } finally {
                    if (outsideDirector != null) {
                        ((ProcessDirector)outsideDirector).threadUnblocked(
                                Thread.currentThread(), null);
                    }
                }
            }
            if (_debugging) {
                _debug("Actors have stopped.");
            }

            // Don't resolve deadlock if we are just pausing
            // or if a stop has been requested.
            // NOTE: Added !_stopRequested.  EAL 3/12/03.
            if (_areActorsDeadlocked() && !_stopRequested) {
                if (_debugging) {
                    _debug("Deadlock detected.");
                }

                try {
                    _notDone = _resolveDeadlock();
                } catch (IllegalActionException e) {
                    // stop all threads.
                    stop();
                    throw e;
                }
            }
        }
    }

    /** Invoke the initialize() methods of all the deeply contained
     *  actors in the container (a composite actor) of this director.
     *  These are expected to call initialize(Actor), which will
     *  result in the creation of a new thread for each actor.
     *  Also, set current time to 0.0, or to the current time of
     *  the executive director of the container, if there is one.
     *
     *  @exception IllegalActionException If the initialize() method
     *   of one of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    /** Initialize the given actor.  This class overrides the base
     *  class to reset the flags for all of the receivers, and to
     *  create a new ProcessThread for each actor being controlled.
     *  This class does *NOT* directly call the initialize method of the
     *  actor. That method is instead called by the actor's thread itself.
     *  This allows actors in process domains to create tokens during
     *  initialization, since sending data in a process-based domain
     *  requires threads for each actor.
     *  @exception IllegalActionException If the actor is not
     *  acceptable to the domain.  Not thrown in this base class.
     */
    public void initialize(Actor actor) throws IllegalActionException {
        if (_debugging) {
            _debug("Initializing actor: " + ((NamedObj) actor).getFullName());
        }

        // Reset the receivers.
        Iterator ports = actor.inputPortList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            Receiver[][] receivers = port.getReceivers();

            for (int i = 0; i < receivers.length; i++) {
                for (int j = 0; j < receivers[i].length; j++) {
                    ((ProcessReceiver) receivers[i][j]).reset();
                }
            }
        }

        // Create threads.
        ProcessThread processThread = new ProcessThread(actor, this);
        _activeThreads.add(processThread);
        _newActorThreadList.addFirst(processThread);
    }

    /** Return true if a stop has been requested on the director.
     *  This is used by the ProcessThread to tell the difference
     *  between a request to pause and a request to stop.
     *  @return True if stop() has been called.
     */
    public boolean isStopFireRequested() {
        return _stopFireRequested;
    }

    /** Return true if a stop has been requested on the director.
     *  This is used by the ProcessThread to tell the difference
     *  between a request to pause and a request to stop.
     *  @return True if stop() has been called.
     */
    public boolean isStopRequested() {
        return _stopRequested;
    }

    /** Return a new receiver of a type compatible with this director.
     *  In class, this returns a new Mailbox.
     *  @return A new Mailbox.
     */
    public Receiver newReceiver() {
        return new Mailbox();
    }

    /** Return false if a stop has been requested or if
     *  the model has reached deadlock. Return true otherwise.
     *  @return False if the director has detected a deadlock or
     *   a stop has been requested.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called postfire().");
            _debug("_notDone = " + _notDone);
            _debug("_stopRequested = " + _stopRequested);
            _debug("_stopFireRequested = " + _stopFireRequested);
        }

        _notDone = _notDone && !_stopRequested;

        if (_debugging) {
            _debug("Returning from postfire(): " + _notDone);
        }

        return _notDone;
    }

    /** Start threads for all actors that have not had threads started
     *  already (this might include actors initialized since the last
     *  invocation of prefire). This starts the threads, corresponding
     *  to all the actors, that were created in the initialize() method.
     *  @return true.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean prefire() throws IllegalActionException {
        // Clear the stopFire flag and trigger all of the actor threads.
        _stopFireRequested = false;
        synchronized (this) {
            notifyAll();
        }
        // Start threads for actors created since the last invocation
        // of this prefire() method.
        Iterator threads = _newActorThreadList.iterator();
        while (threads.hasNext()) {
            ProcessThread procThread = (ProcessThread) threads.next();
            procThread.start();
        }
        _newActorThreadList.clear();

        return true;
    }

    /** Preinitialize the model controlled by this director.  This
     *  subclass overrides the base class to initialize the number of
     *  running threads before proceeding with preinitialization of
     *  the model.
     *
     *  @exception IllegalActionException If creating an actor thread
     *  throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _notDone = true;
        _activeThreads.clear();
        _blockedThreads.clear();
        _pausedThreads.clear();
        _newActorThreadList = new LinkedList();
        super.preinitialize();
    }

    /** Notify this director that the specified thread has finished
     *  executing. This is used to keep track of whether the model
     *  is deadlocked, and also to terminate threads if necessary.
     *  @param thread The thread.
     *  @see #addThread(Thread)
     */
    public synchronized void removeThread(Thread thread) {
        if (_debugging) {
            _debug("Thread "
                    + thread.getName()
                    + " is exiting.");
        }
        _activeThreads.remove(thread);
        _pausedThreads.remove(thread);
        _blockedThreads.remove(thread);
        notifyAll();
    }

    /** Request that the director cease execution altogether.
     *  This causes a call to stop() on all actors contained by
     *  the container of this director, and a call to stopThread()
     *  on each of the process threads that contain actors
     *  controlled by this director. This also sets a flag
     *  so that the next call to postfire() returns false.
     */
    public void stop() {
        // Set this before calling stopThread(), in case the thread
        // needs to distinguish between stopFire() and this method.
        if (_debugging) {
            _debug("Requesting stop of all threads.");
        }

        _stopRequested = true;
        _stopFireRequested = true;

        // Need to copy the active threads set because
        // when stop() is called on each thread, the
        // set itself is modified. We could get a
        // ConcurrentModificationException.
        LinkedList threadsCopy = new LinkedList(_activeThreads);
        Iterator threads = threadsCopy.iterator();
        while (threads.hasNext()) {
            Thread thread = (Thread) threads.next();
            if (thread instanceof ProcessThread) {
                // NOTE: We used to catch and ignore all exceptions
                // here, but that doesn't look right to me. EAL 8/05.
                ((ProcessThread)thread).getActor().stop();                    
            }
            // NOTE: Used to call thread.interrupt() here, with a comment
            // about how it probably wasn't necessary.  But
            // in applets, this gives a security violation.
            // If threads fail to stop, the probably the call
            // below to _requestFinishOnReceivers() isn't doing its
            // job.
        }
        // Added to get stop button to work consistently the first time.
        // EAL 8/05
        _requestFinishOnReceivers();
        // Create a notification thread so that this returns immediately
        // (doesn't have to get a synchronized lock).
        (new NotifyThread(this)).start();
    }

    /** Request that execution stop at the conclusion of the current
     *  iteration. Call stopThread() on each of the process threads that
     *  contain actors controlled by this director and call stopFire() on
     *  the actors that are contained by these threads. This method is
     *  non-blocking.
     */
    public void stopFire() {
        if (_debugging) {
            _debug("stopFire() has been called.");
        }
        _stopFireRequested = true;
        Iterator threads = _activeThreads.iterator();
        while (threads.hasNext()) {
            Thread thread = (Thread) threads.next();
            if (thread instanceof ProcessThread) {
                ((ProcessThread)thread).getActor().stopFire();
            }
        }
    }

    /** Terminate all threads under control of this director immediately.
     *  This abrupt termination will not allow normal cleanup actions
     *  to be performed, and the model should be recreated after calling
     *  this method. This method uses Thread.stop(), a deprecated method
     *  in Java.
     */
    public void terminate() {
        // First need to invoke terminate on all actors under the
        // control of this director.
        super.terminate();

        // Now stop any threads created by this director.
        LinkedList list = new LinkedList();
        list.addAll(_activeThreads);
        _activeThreads.clear();

        Iterator threads = list.iterator();
        while (threads.hasNext()) {
            ((Thread) threads.next()).stop();
        }
    }
    
    /** Notify the director that the specified thread is blocked
     *  on an I/O operation.  If the thread has
     *  not been registered with addThread(), then this call is
     *  ignored.
     *  @param thread The thread.
     *  @param receiver The receiver handling the I/O operation,
     *   or null if it is not a specific receiver.
     *  @see #addThread(Thread)
     */
    public synchronized void threadBlocked(
            Thread thread, ProcessReceiver receiver) {
        if (_activeThreads.contains(thread) && !_blockedThreads.contains(thread)) {
            _blockedThreads.add(thread);
            notifyAll();
        }
    }

    /** Notify the director that the specified thread has paused
     *  in response to a call to stopFire().  If the thread has
     *  not been registered with addThread(), then this call is
     *  ignored. If the thread has been identified as blocked,
     *  it is removed from the set of blocked threads (so it
     *  doesn't get counted twice).
     *  @param thread The thread.
     *  @see #addThread(Thread)
     */
    public synchronized void threadHasPaused(Thread thread) {
        if (_activeThreads.contains(thread) && !_pausedThreads.contains(thread)) {
            _pausedThreads.add(thread);
            _blockedThreads.remove(thread);
            notifyAll();
        }
    }

    /** Notify the director that the specified thread has resumed.
     *  If the director has not previously been notified that it was
     *  paused, then this call is ignored.
     *  @param thread The thread.
     *  @see #threadHasPaused(Thread)
     */
    public synchronized void threadHasResumed(Thread thread) {
        if (_pausedThreads.remove(thread)) {
            notifyAll();
        }
    }

    /** Notify the director that the specified thread is unblocked
     *  on an I/O operation.  If the thread has
     *  not been registered with threadBlocked(), then this call is
     *  ignored.
     *  @param thread The thread.
     *  @param receiver The receiver handling the I/O operation,
     *   or null if it is not a specific receiver.
     *  @see #threadBlocked(Thread)
     */
    public synchronized void threadUnblocked(
            Thread thread, ProcessReceiver receiver) {
        if (_blockedThreads.remove(thread)) {
            notifyAll();
        }
    }

    /** Do nothing.  Input transfers in process domains are handled by
     *  branches, which transfer inputs in a separate thread.
     *  @param port The port.
     *  @return False, to indicate that no tokens were transferred.
     */
    public boolean transferInputs(IOPort port) {
        return false;
    }

    /** Do nothing.  Output transfers in process domains are handled by
     *  branches, which transfer inputs in a separate thread.
     *  @param port The port.
     *  @return False, to indicate that no tokens were transferred.
     */
    public boolean transferOutputs(IOPort port) {
        return false;
    }

    /** End the execution of the model under the control of this
     *  director. A flag is set in all the receivers that causes
     *  each process to terminate at the earliest communication point.
     *  Prior to setting receiver flags, this method wakes up the
     *  threads if they all are stopped.  If the container is not
     *  an instance of CompositeActor, then this method does nothing.
     *  <P>
     *  This method is not synchronized on the workspace, so the caller
     *  should be.
     *
     *  @exception IllegalActionException If an error occurs while
     *   accessing the receivers of all actors under the control of
     *   this director.
     */
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Called wrapup().");
        }

        CompositeActor container = (CompositeActor)getContainer();

        _requestFinishOnReceivers();

        // Now wake up threads that depend on the manager.
        Manager manager = container.getManager();

        // NOTE: Used to do the notification in a new thread.
        // For some reason, however, this isn't sufficient.
        // Have to click the stop button twice.
        // (new NotifyThread(manager)).start();
        synchronized (manager) {
            manager.notifyAll();
        }

        // Wait until all threads stop.
        synchronized (this) {
            while (_activeThreads.size() > 0) {
                try {
                    workspace().wait(this);
                } catch (InterruptedException ex) {
                    // ignore, wait until all process threads stop
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if the count of active processes in the container is 0.
     *  Otherwise return false. Derived classes must override this method to
     *  return true to any other forms of deadlocks that they might introduce.
     *  @return True if there are no active processes in the container.
     */
    protected synchronized boolean _areActorsDeadlocked() {
        return (_activeThreads.size() == 0);
    }

    /** Return true if the count of active processes equals the number
     *  of paused and blocked threads.  Otherwise return false.
     *  @return True if there are no active processes in the container.
     */
    protected synchronized boolean _areAllActorsStopped() {
        return (_getActiveThreadsCount() == (_getStoppedActorsCount() + _getBlockedActorsCount()));
    }

    /** Return the number of active threads under the control of this
     *  director.
     *  @return The number of active threads.
     */
    protected final synchronized int _getActiveThreadsCount() {
        return _activeThreads.size();
    }

    /** Return the number of threads that are currently blocked.
     *  @return Return the number of threads that are currently blocked.
     */
    protected final synchronized int _getBlockedActorsCount() {
        return _blockedThreads.size();
    }

    /** Return the number of threads that are currently stopped.
     *  @return Return the number of threads that are currently stopped.
     */
    protected final synchronized int _getStoppedActorsCount() {
        return _pausedThreads.size();
    }

    /** Call requestFinish() on all receviers.
     */
    private void _requestFinishOnReceivers() {
        CompositeActor container = (CompositeActor)getContainer();
        Iterator actors = container.deepEntityList().iterator();
        Iterator actorPorts;
        ProcessReceiver nextReceiver;

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            actorPorts = actor.inputPortList().iterator();

            while (actorPorts.hasNext()) {
                IOPort port = (IOPort) actorPorts.next();

                // Setting finished flag in the receivers.
                Receiver[][] receivers = port.getReceivers();

                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        nextReceiver = (ProcessReceiver) receivers[i][j];
                        nextReceiver.requestFinish();
                    }
                }
            }
        }
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
    protected boolean _resolveDeadlock() throws IllegalActionException {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A flag for determining whether successive iterations will be
     *  permitted.
     */
    protected boolean _notDone = true;

    /** Indicator that a stopFire has been requested by a call to
     *  stopFire().
     */
    protected boolean _stopFireRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The set of threads that are blocked on an IO operation. */
    private HashSet _blockedThreads = new HashSet();

    /** The set of threads that have been paused in response to stopFire(). */
    private HashSet _pausedThreads = new HashSet();

    /** The threads created by this director. */
    private HashSet _activeThreads = new HashSet();

    /** A list of threads created but not started. */
    private LinkedList _newActorThreadList;
}
