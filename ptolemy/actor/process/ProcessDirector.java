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
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.LinkedList;


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
        newObject._activeActorCount = 0;
        newObject._actorThreadList = new LinkedList();
        newObject._blockedActorCount = 0;
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

        synchronized (this) {
            while (!_areActorsDeadlocked() && !_areAllActorsStopped()) {
                if (_debugging) {
                    _debug("Waiting for actors to stop.");
                }

                try {
                    workspace.wait(this);
                } catch (InterruptedException e) {
                    // stop all threads
                    stop();
                    return;
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
        _stoppedActorCount = 0;
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

        // Initialize threads
        ProcessThread processThread = _getProcessThread(actor, this);
        _actorThreadList.addFirst(processThread);
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

        Iterator threads = _newActorThreadList.iterator();
        threads = _newActorThreadList.iterator();

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
        _activeActorCount = 0;
        _blockedActorCount = 0;
        _actorThreadList = new LinkedList();
        _newActorThreadList = new LinkedList();
        super.preinitialize();
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
            _debug("stop() has been called. Active thread count is: "
                    + _activeActorCount);
        }

        _stopRequested = true;
        _stopFireRequested = true;

        if (_actorThreadList != null) {
            Iterator threads = _actorThreadList.iterator();

            while (threads.hasNext()) {
                ProcessThread thread = (ProcessThread) threads.next();

                try {
                    thread.getActor().stop();

                    // I'm not sure why we need the interrupt here...but it
                    // seems to help.
                    thread.interrupt();
                } catch (Exception ex) {
                    // Ignore..  probably InterruptedIOException
                }
            }
        }
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

        if (_actorThreadList != null) {
            Iterator threads = _actorThreadList.iterator();

            while (threads.hasNext()) {
                ProcessThread thread = (ProcessThread) threads.next();
                thread.getActor().stopFire();
            }
        }
    }

    /** Terminates all threads under control of this director immediately.
     *  This abrupt termination will not allow normal cleanup actions
     *  to be performed, and the model should be recreated after calling
     *  this method.
     */

    //  Note: for now call Thread.stop() but should change to use
    //  Thread.destroy() when it is eventually implemented.
    public void terminate() {
        // First need to invoke terminate on all actors under the
        // control of this director.
        super.terminate();

        // Now stop any threads created by this director.
        LinkedList list = new LinkedList();
        list.addAll(_actorThreadList);
        _actorThreadList.clear();

        Iterator threads = list.iterator();

        while (threads.hasNext()) {
            ((Thread) threads.next()).stop();
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

        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor) container).deepEntityList()
                .iterator();
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

            // Now wake up threads that depend on the manager.
            Manager manager = ((Actor) getContainer()).getManager();

            // NOTE: Used to do the notification in a new thread.
            // For some reason, however, this isn't sufficient.
            // Have to click the stop button twice.
            // (new NotifyThread(manager)).start();
            synchronized (manager) {
                manager.notifyAll();
            }

            // Wait until all process threads stop.
            synchronized (this) {
                while (_activeActorCount > 0) {
                    try {
                        workspace().wait(this);
                    } catch (InterruptedException ex) {
                        // ignore, wait until all process threads stop
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Increase the count of blocked actors by one and register the
     *  receiver that instigated the newly blocked actor. This method
     *  may be overridden in derived classes to added domain specific
     *  functionality. Implementations of this method must be synchronized.
     *
     *  @param receiver The receiver whose data transfer is blocked.
     */
    protected synchronized void _actorBlocked(ProcessReceiver receiver) {
        _blockedActorCount++;
        notifyAll();
    }

    /** Increase the count of blocked actors by one and register the
     *  receivers that instigated the newly blocked actor. This method
     *  may be overridden in derived classes to added domain specific
     *  functionality. Implementations of this method must be synchronized.
     *
     *  @param receivers The receivers whose data transfer is blocked.
     */
    protected synchronized void _actorBlocked(LinkedList receivers) {
        _blockedActorCount++;
        notifyAll();
    }

    /** Decrease the count of stopped actors by one.  This method is
     *  called by instances of ProcessThread after detecting that the
     *  stopFire() flag has been cleared. This method may be
     *  overridden in derived classes to added domain specific
     *  functionality. Implementations of this method must be
     *  synchronized.
     */
    protected synchronized void _actorHasRestarted() {
        _stoppedActorCount--;
        notifyAll();
    }

    /** Increase the count of stopped actors by one.  This method is
     *  called by instances of ProcessThread in response to a call to
     *  their stopThread() method. This method may be overridden in
     *  derived classes to added domain specific
     *  functionality. Implementations of this method must be
     *  synchronized.
     */
    protected synchronized void _actorHasStopped() {
        _stoppedActorCount++;
        notifyAll();
    }

    /** Decrease the count of blocked actors by one and unregister the
     *  receiver that was previously blocked. This method may be
     *  overridden in derived classes to added domain specific
     *  functionality. Implementations of this method must be synchronized.
     *
     *  @param receiver The receiver whose data transfer was
     *  previously blocked.
     */
    protected synchronized void _actorUnBlocked(ProcessReceiver receiver) {
        _blockedActorCount--;
        notifyAll();
    }

    /** Decrease the count of blocked actors by one and unregister the
     *  receivers that were previously blocked. This method may be
     *  overridden in derived classes to added domain specific
     *  functionality. Implementations of this method must be synchronized.
     *
     *  @param receivers The receivers whose data transfer was
     *  previously blocked.
     */
    protected synchronized void _actorUnBlocked(LinkedList receivers) {
        _blockedActorCount--;
        notifyAll();
    }

    /** Add a thread to the list of threads in the model.
     *  This list is used in case of abrupt termination of the model.
     *  @param thread The newly created thread.
     */
    protected synchronized void _addNewThread(ProcessThread thread) {
        _actorThreadList.addFirst(thread);
    }

    /** Return true if the count of active processes in the container is 0.
     *  Otherwise return false. Derived classes must override this method to
     *  return true to any other forms of deadlocks that they might introduce.
     *
     *  @return true if there are no active processes in the container.
     */
    protected synchronized boolean _areActorsDeadlocked() {
        return (_activeActorCount == 0);
    }

    /** Return true if the count of active processes equals the number
     *  of stopped threads.  Otherwise return false.
     *
     *  @return true if there are no active processes in the container.
     */
    protected synchronized boolean _areAllActorsStopped() {
        return (_activeActorCount == (_stoppedActorCount + _blockedActorCount));
    }

    /** Decrease by one the count of active processes under the control of
     *  this director.
     *  This method should be called only when an active thread that was
     *  registered using _increaseActiveCount() is terminated.
     *  This count is used to detect deadlocks for termination and other
     *  reasons.
     */
    protected synchronized void _decreaseActiveCount() {
        if (_debugging) {
            _debug("Thread for actor "
                    + ((ProcessThread) Thread.currentThread()).getActor()
                    + " is deactivating.");
        }

        _activeActorCount--;

        if (_debugging) {
            _debug("Decreasing active count to: " + _activeActorCount);
        }

        notifyAll();
    }

    /** Return the number of active processes under the control of this
     *  director.
     *
     *  @return The number of active actors.
     */
    protected final synchronized long _getActiveActorsCount() {
        return _activeActorCount;
    }

    /** Return the number of actors that are currently blocked.
     *  @return Return the number of actors that are currently blocked.
     */
    protected final synchronized int _getBlockedActorsCount() {
        return _blockedActorCount;
    }

    /** Create a new ProcessThread for controlling the actor that
     *  is passed as a parameter of this method. Subclasses are
     *  encouraged to override this method as necessary for domain
     *  specific functionality.
     *  @param actor The actor that the created ProcessThread will
     *   control.
     *  @param director The director that manages the model that the
     *   created thread is associated with.
     *  @return Return a new ProcessThread that will control the
     *   actor passed as a parameter for this method.
     *  @exception IllegalActionException If creating an new ProcessThread
     *  throws it.
     */
    protected ProcessThread _getProcessThread(Actor actor,
            ProcessDirector director) throws IllegalActionException {
        return new ProcessThread(actor, director);
    }

    /** Return the number of actors that are currently stopped.
     *  @return Return the number of actors that are currently stopped.
     */
    protected final synchronized int _getStoppedActorsCount() {
        return _stoppedActorCount;
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
        _activeActorCount++;

        if (_debugging) {
            _debug("Increasing active count to: " + _activeActorCount);
        }

        notifyAll();
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
    // Count of the number of processes that were started by this
    // director but have not yet finished.
    private long _activeActorCount;

    // The count of blocked actors
    private int _blockedActorCount = 0;

    // The count of stopped actors
    private int _stoppedActorCount = 0;

    // The threads started by this director.
    private LinkedList _actorThreadList;

    //A copy of threads started since the last invocation of prefire().
    private LinkedList _newActorThreadList;
}
