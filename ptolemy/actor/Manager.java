/* A Manager governs the execution of a model.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.data.*;

import collections.LinkedList;
import collections.HashedSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.lang.reflect.*;

import java.util.Date;			// For timing measurements

//////////////////////////////////////////////////////////////////////////
//// Manager
/**
A Manager governs the execution of a model in a domain-independent way.
Its methods are designed to be called by a GUI, an applet, an command-line
interface, or the top-level code of an application.  The manager can
execute the model in the calling thread or in a separate thread.
The latter is useful when the caller wishes to remain live during
the execution of the model.
<p>
A manager provides services for cleanly handling changes to the
topology.  These include such changes as adding or removing an entity,
port, or relation, creating or destroying a link, and changing the value
or type of a parameter.  Collectively, such changes are called
<i>mutations</i>. Usually, mutations
cannot safely occur at arbitrary points in the execution of
a model.  Models can queue changes with the director or
the manager using the requestChange() method.  The director simply delegates
the request to the manager, which performs the change at the earliest
opportunity.  In this implementation of Manager, the changes are
executed between iterations.
<p>
A service is also provided whereby an object can be registered with the
director as a change listener.  A change listener is informed when
changes that are requested via requestChange() are executed.
<p>
Manager can optimize the performance of an execution by making
the workspace <i>write protected</i> during an iteration, if all
relevant directors permit this.  This removes some of the overhead
of obtaining read and write permission on the workspace.
By default, directors do not permit this, but
many directors explicitly relenquish write access to allow faster execution.
Such directors are declaring that they will not make changes to the
topology during execution.  Instead, any desired changes are delegated
to the director via the requestChange() method.

@author Steve Neuendorffer, Lukito Muliadi, Edward A. Lee
// Contributors: Mudit Goel, John S. Davis II
@version $Id$
*/

public final class Manager extends NamedObj implements Runnable {

    /** Construct a manager in the default workspace with an empty string
     *  as its name. The manager is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public Manager() {
        super();
    }

    /** Construct a manager in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The manager is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this Manager.
     */
    public Manager(String name) {
        super(name);
    }

    /** Construct a manager in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The manager is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     *  @param name Name of this Manager.
     */
    public Manager(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicator that the model may be corrupted.
     */
    public final State CORRUPTED = new State(
            "Model terminated and may be corrupted");

    /** Indicator that there is no currently active execution.
     */
    public final State IDLE = new State("Idle");

    /** Indicator that the execution is in the initialize phase.
     */
    public final State INITIALIZING = new State("Initializing");

    /** Indicator that the execution is in an interation.
     */
    public final State ITERATING = new State("Executing iteration");

    /** Indicator that the execution is in the mutations phase.
     */
    public final State MUTATING = new State("Processing mutations");

    /** Indicator that the execution is paused.
     */
    public final State PAUSED = new State("Execution paused");

    /** Indicator that type resolution is being done.
     */
    public final State RESOLVING_TYPES = new State("Resolving types");

    /** Indicator that the execution is in the wrapup phase.
     */
    public final State WRAPPING_UP = new State("Wrapping up");

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a change listener. The listener
     *  will be notified of the execution of each change requested
     *  via the requestChange() method.
     *  If the listener is already in the list, do not add it again.
     *  @param listener The listener to add.
     */
    public void addChangeListener(ChangeListener listener) {
        if (_changeListeners == null) {
            _changeListeners = new LinkedList();
        } else {
            if (_changeListeners.includes(listener)) {
                return;
            }
        }
        _changeListeners.insertLast(listener);
    }

    /** Add a listener to be notified when the model execution changes state.
     *  @param listener The listener.
     */
    public void addExecutionListener(ExecutionListener listener) {
        if(listener == null) return;
        if(_executionListeners == null) {
            _executionListeners = new HashedSet();
        }
        _executionListeners.include(listener);
    }

    /** Execute the model.  Begin with the initialization phase, followed
     *  by a sequence of iterations, followed by a wrapup phase.
     *  The sequence of iterations concludes when the postfire() method
     *  of the container (the top-level composite actor) returns false,
     *  or when the finish() method is called.
     *  <p>
     *  The execution is performed in the calling thread (the current thread),
     *  so this method returns only after execution finishes.
     *  If you wish to perform execution in a new thread, use startRun() 
     *  instead.  Even if an exception occurs during the execution, the
     *  wrapup() method is called (in a finally clause).  It is up to the
     *  caller to handle (i.e. report) the exception.
     *  If you do not wish to handle exceptions, but want to execute
     *  within the calling thread, use run().
     *  @exception KernelException If the model throws it.
     *  @exception IllegalActionException If the model is already running, or
     *   if there is no container.
     */
    public synchronized void execute()
            throws KernelException, IllegalActionException {

        // Make a record of the time execution starts.
        long startTime = (new Date()).getTime();

        boolean completedSuccessfully = false;
        try {            
            initialize();
            // Call iterate() until finish() is called or postfire()
            // returns false.
            _debug(getName() + ": begin to iterate.");
            while (!_finishRequested) {
                if (!iterate()) break;
                if (_pauseRequested) {
                    _setState(PAUSED);
                    while (_pauseRequested) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            // ignore.
                        }
                    }
                }
            }
            completedSuccessfully = true;
        } 
        finally {
            wrapup();
            if (completedSuccessfully) {
                _notifyListenersOfCompletion();
            }
            if (_state != IDLE) {
                _setState(IDLE);
            }
        }
        // Report the execution time.
        long endTime = (new Date()).getTime();
        System.out.println("ptolemy.actor.Manager run(): elapsed time: "
                + (endTime - startTime) + " ms");
    }

    /** Set a flag to request that execution stop and exit gracefully.
     *  This will result in finish() being called on the top level
     *  CompositeActor, although not necessarily immediately.
     *  This method sets the flag, then calls stopFire() on the 
     *  toplevel composite actor to ensure that the flag will actually get
     *  seen.  Finally, resume() is called to ensure that the model is not
     *  currently paused.  Note that the flag is set before
     *  calling resume so that it is visible as
     *  as soon as possible.  This is important since another thread may
     *  be holding a synchronization lock on the manager, preventing 
     *  resume from running.
     */
    public void finish() {
        if(_state == IDLE) return;
        _finishRequested = true;
        CompositeActor container = (CompositeActor) getContainer();
        if(container == null) throw new InternalErrorException(
                "Attempted to call finish on an executing manager with no" +
                " associated model");
        container.stopFire();
        resume();
    }

    /** Return the top-level composite actor for which this manager
     *  controls execution.
     *  @return The composite actor that this manager is responsible for.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Return the iteration count, which is the number of iterations
     *  that have been started (but not necessarily completed).
     *  @return The number of iterations started.
     */
    public int getIterationCount() {
        return _iterationCount;
    }

    /** Return the current state of execution of the manager.
     *  @return The state of execution.
     */
    public State getState() {
        return _state;
    }

    /** Initialize the model.
     *  @exception KernelException If the model throws it.
     *  @exception IllegalActionException If the model is already running, or
     *   if there is no container.
     */
    public synchronized void initialize()
            throws KernelException, IllegalActionException {
        if (_state != IDLE) {
            throw new IllegalActionException(this,
                    "The model is already running.");
        }
        if (_container == null) {
            throw new IllegalActionException(this,
                    "No model to run!");
        }
        _setState(INITIALIZING);

        _pauseRequested = false;
        _finishRequested = false;
        _typesResolved = false;
        _iterationCount = 0;

        // Initialize the topology
        _container.initialize();
    }

    /** Indicate that resolved types in the system may no longer be valid.
     *  This will force type resolution to be redone on the next iteration.
     */
    public void invalidateResolvedTypes() {
        _typesResolved = false;
    }

    /** Invoke one iteration of the model.  An iteration consists of
     *  first performing changes queued with requestChange()
     *  and type resolution, if necessary, and then
     *  invoking prefire(), fire(), and postfire(), in that
     *  order. If prefire() returns false, then fire() and postfire() are not
     *  invoked, and true is returned.
     *  Otherwise, fire() will be called once, followed by
     *  postfire(). The return value of postfire() is returned.
     *  Note that this method ignores finish and pause requests.
     *  If you wish to use finish() or pause() to control the execution,
     *  then you should execute the model using execute(), run(), or
     *  startRun().
     *  This method is read synchronized on the workspace.
     *
     *  @return True if postfire() returns true.
     *  @exception KernelException If the model throws it, or if there
     *   is no container, or if one of the requested changes fails.
     */
    public boolean iterate() throws KernelException {
        if (_container == null) {
            throw new IllegalActionException(this,
                    "No model to execute!");
        }
        boolean result = true;
        try {
            workspace().getReadAccess();
            _debug(getName() + " process change requests.");
            _processChangeRequests();

            if (!_typesResolved) {
                _debug(getName() + ": need resolve type.");
                resolveTypes();
                _typesResolved = true;
                _debug(getName() + ": typed resolved.");
            }

            _iterationCount++;
            _setState(ITERATING);

            // Set the appropriate write access, because we're about to
            // go into an iteration.
            if (!_needWriteAccess()) {
                workspace().setReadOnly(true);
            }
            _debug(getName() + ": prefire the system.");
            if (_container.prefire()) {
                _debug(getName() + ": fire the system.");
                _container.fire();
                result = _container.postfire();
            }
            _debug(getName() + ": finish one iteration, returning " + result);
        } finally {
            workspace().setReadOnly(false);
            workspace().doneReading();
        }
        return result;
    }

    /** Notify all the execution listeners of an exception.
     *  If there are no listeners, then print the exception information
     *  to standard error stream. This is intended to be used by threads
     *  that are involved an execution as a mechanism for reporting
     *  errors.
     *  @param ex The exeception.
     */
    public void notifyListenersOfException(Exception ex) {
        _debug(ex.getMessage());
        if (_executionListeners == null) {
            System.err.println(ex.getMessage());
        } else {
            Enumeration listeners = _executionListeners.elements();
            while(listeners.hasMoreElements()) {
                ExecutionListener listener =
                    (ExecutionListener) listeners.nextElement();
                listener.executionError(this, ex);
            }
        }
    }

    /** Set a flag requesting that execution pause at the next opportunity
     *  (between iterations).  Call stopFire() on the toplevel composite 
     *  actor to ensure that the manager's execution thread becomes active 
     *  again.   The thread controlling the execution will be
     *  suspended the next time through the iteration loop.  To resume 
     *  execution, call resume() from another thread.
     */
    public void pause() {
        _pauseRequested = true;
        CompositeActor container = (CompositeActor) getContainer();
        if(container == null) throw new InternalErrorException(
                "Attempted to call finish on an executing manager with no" +
                " associated model");
        container.stopFire();
    }

    /** Remove a listener from the list of listeners that are notified
     *  of execution events.  If the specified listener is not on the list,
     *  do nothing.
     *  @param listener The listener to remove.
     */
    public void removeExecutionListener(ExecutionListener listener) {
        if(listener == null || _executionListeners == null) return;
        _executionListeners.exclude(listener);
    }

    /** Remove a change listener. If the specified listener is not
     *  on the list, do nothing.
     *  @param listener The listener to remove.
     */
    public void removeChangeListener(ChangeListener change) {
        if (_changeListeners == null) {
            return;
        }
        _changeListeners.exclude(change);
    }

    /** Queue a change request.
     *  The indicated change will be executed at the next opportunity,
     *  typically between top-level iterations of the model. For the
     *  benefit of process-oriented domains, which may not have finite
     *  iterations, this method also calls stopFire() on the top-level
     *  composite actor, requesting that directors in such domains
     *  return from their fire() method as soon as practical.
     *  @param change The requested change.
     */
    public void requestChange(ChangeRequest change) {
        // Create the list of requests if it doesn't already exist
        if (_changeRequests == null) {
            _changeRequests = new LinkedList();
        }
        _changeRequests.insertLast(change);
        CompositeActor container = (CompositeActor) getContainer();
        container.stopFire();
    }

    /** Check types on all the connections and resolve undeclared types.
     *  If the container is not an instance of TypedCompositeActor,
     *  do nothing.
     *  This method is write-synchronized on the workspace.
     *  @exception TypeConflictException If type conflict is detected.
     */
    public void resolveTypes() throws TypeConflictException {
        if ( !(_container instanceof TypedCompositeActor)) {
            return;
        }
	try {
	    workspace().getWriteAccess();
            _setState(RESOLVING_TYPES);

	    LinkedList conflicts = new LinkedList();
	    conflicts.appendElements(
                    ((TypedCompositeActor)_container).checkTypes());

            Enumeration constraints =
                ((TypedCompositeActor)_container).typeConstraints();
	    if (constraints.hasMoreElements()) {
                InequalitySolver solver = new InequalitySolver(
                        TypeLattice.lattice());
	        while (constraints.hasMoreElements()) {
                    Inequality ineq = (Inequality)constraints.nextElement();
                    solver.addInequality(ineq);
	        }

                // find the least solution (most specific types)
                boolean resolved = solver.solveLeast();
                if ( !resolved) {
		    Iterator unsatisfied = solver.unsatisfiedInequalities();
		    while (unsatisfied.hasNext()) {
		        Inequality ineq =
                            (Inequality)unsatisfied.next();
		        InequalityTerm term =
                            (InequalityTerm)ineq.getLesserTerm();
		        Object typeObj = term.getAssociatedObject();
		        if (typeObj != null) {
			    // typeObj is a Typeable
			    conflicts.insertLast(typeObj);
		        }
		        term = (InequalityTerm)ineq.getGreaterTerm();
		        typeObj = term.getAssociatedObject();
		        if (typeObj != null) {
			    // typeObj is a Typeable
			    conflicts.insertLast(typeObj);
		        }
		    }
                }

	        // check whether resolved types are acceptable.
                // They might be, for example, NaT.
	        Iterator var = solver.variables();
	        while (var.hasNext()) {
		    InequalityTerm term = (InequalityTerm)var.next();
		    if ( !term.isValueAcceptable()) {
		        conflicts.insertLast(term.getAssociatedObject());
		    }
	        }
	    }

	    if (conflicts.size() > 0) {
		throw new TypeConflictException(conflicts.elements(),
                        "Type conflicts occurred in " + _container.getFullName()
			+ " on the following Typeables:");
	    }
	} catch (IllegalActionException iae) {
	    // this should not happen.
	    throw new InternalErrorException(iae.getMessage());
	} finally {
	    workspace().doneWriting();
	}
    }

    /** If the model is paused, resume execution.  This method must be called
     *  from a different thread than that controlling the execution, since
     *  the thread controlling the execution is suspended.
     */
    public synchronized void resume() {
        if(_state == PAUSED) {
            _pauseRequested = false;
            notifyAll();
        }
    }

    /** Execute the model, catching all exceptions. Use this method to
     *  execute the model within the calling thread, but to not throw
     *  exceptions.  Instead, any registered listeners are notified of
     *  the exceptions, or if there are no registered listeners, then
     *  the exception is printed to standard output.  Except for its
     *  exception handling, this method has exactly the same behavior
     *  as execute().
     */
    public void run() {
        try {
            execute();
        } catch (Exception ex) {
            // Notify listeners.
            notifyListenersOfException(ex);
        } finally {
            _thread = null;
        }
    }

    /** Start an execution in another thread and return.  Any exceptions
     *  that occur during the execution of the model are handled by
     *  reporting them to any registered execution listeners.  If there
     *  are no registered execution listeners, then the exceptions are
     *  printed to standard output together with a stack trace.
     *  @exception IllegalActionException If the model is already running.
     */
    public void startRun() throws IllegalActionException {
        if (_state != IDLE) {
            throw new IllegalActionException(this,
                    "Model is " + _state.getDescription());
        }
        _thread = new PtolemyThread(this);
        _thread.start();
    }

    /** Terminate the currently executing model with extreme prejudice.
     *  This leaves the state of the manager in CORRUPTED, which means
     *  that the model cannot be executed again.  A new model must be
     *  created, with a new manager, to execute again.
     *  This method is not intended to be used as a normal route of 
     *  stopping execution. To normally stop exceution, call the finish() 
     *  method instead. This method should be called only 
     *  when execution fails to terminate by normal means due to certain
     *  kinds of programming errors (infinite loops, threading errors, etc.).
     *  <p>
     *  If the model execution was started in a separate thread (using
     *  startRun()), then that thread is killed unceremoniously (using
     *  a method that is now deprecated in Java, for obvious reasons).
     *  This method also calls terminate on the toplevel composite actor.
     *  <p>
     *  This method is not synchronized because we want it to
     *  happen as soon as possible, no matter what.
     *  @deprecated
     */
    public void terminate() {
        // If the execution was started in a separate thread, kill that thread.
        // NOTE: This uses the stop() method, which is now deprecated in Java.
        // Indeed it should be, since it terminates a thread
        // nondeterministically, and can leave any objects that the thread
        // operating on in an inconsistent state.
        if(_thread != null) {
            _thread.stop();
            try {
                _thread.join();
            }
            catch (InterruptedException e) {
                // This will usually get thrown, since we are
                // forcibly terminating
                // the thread.   We just ignore it.
            }
            _thread = null;
        }
        // Terminate the entire hierarchy as best we can.
        _container.terminate();
        _setState(CORRUPTED);
    }

    /** Wrap up the model.
     *  @exception KernelException If the model throws it.
     *  @exception IllegalActionException If the model is idle or already
     *   wrapping up, or if there is no container.
     */
    public synchronized void wrapup()
            throws KernelException, IllegalActionException {
        if (_state == IDLE || _state == WRAPPING_UP) {
            throw new IllegalActionException(this,
                    "Cannot wrap up. The current state is: "
                    + _state.getDescription());
        }
        if (_container == null) {
            throw new IllegalActionException(this,
                    "No model to run!");
        }
        _setState(WRAPPING_UP);

        // Wrap up the topology
        _container.wrapup();

        // Wrapup completed successfully
        _setState(IDLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make this manager the manager of the specified composite
     *  actor. If the composite actor is not null, then the manager is 
     *  removed from the directory of the workspace.  If the composite actor
     *  is null, then the mnager is <b>not</b> returned to the directory of the
     *  workspace, which may result in it being garbage collected.
     *  This method should not be called directly.  Instead, call
     *  setManager in the CompositeActor class (or a derived class).
     */
    protected void _makeManagerOf(CompositeActor ca) {
        if (ca != null) {
            workspace().remove(this);
        }
        _container = ca;
    }

    /** Set the state of execution and notify listeners.
     *  @param newstate The new state.
     */
    protected void _setState(State newstate) {
        _state = newstate;
        _notifyListenersOfStateChange();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Notify listeners that execution has completed successfully.
     */
    private void _notifyListenersOfCompletion() {
        _debug("Completed execution with " + _iterationCount + " iterations");
        if (_executionListeners != null) {
            Enumeration listeners = _executionListeners.elements();
            while(listeners.hasMoreElements()) {
                ExecutionListener listener =
                    (ExecutionListener) listeners.nextElement();
                listener.executionFinished(this);
            }
        }
    }

    /*  Propagate the state change event to all the execution listeners.
     */
    private void _notifyListenersOfStateChange() {
        String msg;
        if (_state == ITERATING) {
            msg = _state.getDescription() + " number "
                + _iterationCount;
        } else {
            msg = _state.getDescription();
        }
        _debug(msg);
        if (_executionListeners != null) {
            Enumeration listeners = _executionListeners.elements();
            while(listeners.hasMoreElements()) {
                ExecutionListener listener =
                    (ExecutionListener) listeners.nextElement();
                listener.managerStateChanged(this);
            }
        }
    }

    /*  Check whether write access is needed during an
     *  iteration. This is done by asking the directors.
     *  This method calls the needWriteAccess() method of 
     *  the top level director, which will in turn query any inside
     *  directors.
     */
    private boolean _needWriteAccess() {
        if (_writeAccessVersion == workspace().getVersion()) {
            return _writeAccessNeeded;
        }
        _writeAccessNeeded = _container.getDirector().needWriteAccess();
        _writeAccessVersion = workspace().getVersion();
        return _writeAccessNeeded;
    }

    /** Process the queued change requests that have been added with
     *  requestChange(). Registered change
     *  listeners are informed of each change in a series of calls
     *  after successful completion of each request. If any queued
     *  request itself makes requests using requestChange(), then those
     *  requests are processed in the same way
     *  after the first batch is completed.  If any
     *  request fails with an exception, then the change list is cleared,
     *  and no further requests are processed.
     *  Note that change requests processed successfully
     *  prior to the failed request are not undone.
     *
     *  @exception IllegalActionException If any of the pending requests have
     *   already been implemented.
     *  @exception ChangeFailedException If any of the requests fails.
     */
    protected void _processChangeRequests()
            throws IllegalActionException, ChangeFailedException {
        while (_changeRequests != null) {
            _setState(MUTATING);

            // Clone the change request list before iterating through it
            // in case any of the changes themselves post change requests.
            // Regrettably, LinkedList makes its clone() method protected,
            // so we have to copy by hand...
            LinkedList clonedList = new LinkedList();
            Enumeration enum = _changeRequests.elements();
            while (enum.hasMoreElements()) {
                clonedList.insertLast(enum.nextElement());
            }

            // Clear the request queue.  We want to discard the queue even
            // if the changes fail.
            // Otherwise, we could get stuck not being able to do anything
            // further with the model.
            _changeRequests = null;

            enum = clonedList.elements();
            while (enum.hasMoreElements()) {
                ChangeRequest request = (ChangeRequest)enum.nextElement();
                request.execute();

                // Inform all listeners. Of course, this won't happen
                // if the change request failed
                if (_changeListeners != null) {
                    Enumeration listeners = _changeListeners.elements();
                    while(listeners.hasMoreElements()) {
                        ChangeListener listener
                            = (ChangeListener)listeners.nextElement();
                        request.notify(listener);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A list of change listeners.
    private LinkedList _changeListeners;

    // A list of pending changes.
    private LinkedList _changeRequests;

    // The top-level CompositeActor that contains this Manager
    private CompositeActor _container = null;

    // Listeners for execution events.
    private HashedSet _executionListeners;

    // Flag indicating that finish() has been called.
    private boolean _finishRequested = false;

    // Count the number of iterations completed.
    private int _iterationCount;

    // Flag indicating that pause() has been called.
    private boolean _pauseRequested = false;

    // The state of the execution.
    private State _state = IDLE;

    // If startRun() is used, then this points to the thread that was
    // created.
    private PtolemyThread _thread;

    // An indicator of whether type resolution needs to be done.
    private boolean _typesResolved = false;

    // A flag to indicate whether write access is needed by any of of
    // of the domains in the model during an iteration.
    private boolean _writeAccessNeeded = true;
    private long _writeAccessVersion;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** Instances of this class represent phases of execution, or the
     *  state of the manager.
     */
    public class State {

        // Constructor is private because only Manager instantiates this class.
        private State(String description) {
            _description = description;
        }

        /** Get a description of the state.
         *  @return A description of the state.
         */
        public String getDescription() {
            return _description;
        }

        /** Get the manager.
         *  @return The manager that is in this state.
         */
        public Manager getManager() {
            return Manager.this;
        }

        private String _description;
    }
}
