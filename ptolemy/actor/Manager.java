/* A Manager governs the execution of a model.

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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.ChangeRequest;
import ptolemy.kernel.event.ChangeListener;
import ptolemy.kernel.event.ChangeFailedException;
import ptolemy.data.type.TypeLattice;

import java.util.Enumeration;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import java.util.Date;			// For timing measurements

//////////////////////////////////////////////////////////////////////////
//// Manager
/**
A Manager governs the execution of a model in a domain-independent way.
Its methods are designed to be called by a GUI, an applet, a command-line
interface, or the top-level code of an application.  The manager can
execute the model in the calling thread or in a separate thread.
The latter is useful when the caller wishes to remain live during
the execution of the model.
<p>
There are three methods that can be used to start execution of a system
attached to the manager.  The execute() method is the most basic way to
execute a model.  The model will be executed <i>synchronously</i>,
meaning that the execute()
method will return when execution has completed.  Any exceptions that
occur will be thrown by the execute method to the calling thread, and will
not be reported to any execution listeners.  The run() method also
initiates synchronous execution of a model, but additionally catches all
exceptions and passes them to the notifyListenersOfException method
<i>without throwing them to the calling thread</i>.
The startRun() method, unlike the previous two
techniques, begins <i>asynchronous</i> execution of a model.   This method
starts a new thread for execution of the model and then returns immediately.
Exceptions are reported using the notifyListenersOfException method.
<p>
In addition, execution can be manually driven, one phase at a time, using the
methods initialize(), iterate() and wrapup().  This is most useful for
testing purposes.  For example, a type system check only needs to get the
resolved types, which are found during initialize, so the test can avoid
actually executing the system.  Also, when testing mutations, the model can
be examined after each toplevel iteration to ensure the proper behavior.
<p>
A manager provides services for cleanly handling changes to the
topology.  These include such changes as adding or removing an entity,
port, or relation, creating or destroying a link, and changing the value
or type of a parameter.  Collectively, such changes are called
<i>mutations</i>. Usually, mutations
cannot safely occur at arbitrary points in the execution of
a model.  Models can queue mutations with the director or
the manager using the requestChange() method.  The director simply delegates
the request to the manager, which performs the change at the earliest
opportunity.  In this implementation of Manager, the changes are
executed between iterations.
<p>
A service is also provided whereby an object can be registered with the
director as a change listener.  A change listener is informed when
mutations that are requested via requestChange() are executed.
<p>
Manager can optimize the performance of an execution by making
the workspace <i>write protected</i> during an iteration, if all
relevant directors permit this.  This removes some of the overhead
of obtaining read and write permission on the workspace.
By default, directors do not permit this, but
many directors explicitly relinquish write access to allow faster execution.
Such directors are declaring that they will not make changes to the
topology during execution.  Instead, any desired mutations are delegated
to the manager via the requestChange() method.

@author Steve Neuendorffer, Lukito Muliadi, Edward A. Lee
// Contributors: Mudit Goel, John S. Davis II
@version $Id$
*/

public class Manager extends NamedObj implements Runnable {

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
     *  @exception IllegalActionException If the name has a period.
     */
    public Manager(String name) throws IllegalActionException {
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
     *  @exception IllegalActionException If the name has a period.
     */
    public Manager(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // NOTE: The following names of states should fit into the sentence:
    // "The model is ... "

    /** Indicator that the model may be corrupted.
     */
    public final State CORRUPTED = new State("corrupted");

    /** Indicator that there is no currently active execution.
     */
    public final State IDLE = new State("idle");

    /** Indicator that the execution is in the initialize phase.
     */
    public final State INITIALIZING = new State("initializing");

    /** Indicator that the execution is in an iteration.
     */
    public final State ITERATING = new State("executing");

    /** Indicator that the execution is in the mutations phase.
     */
    public final State MUTATING = new State("processing mutations");

    /** Indicator that the execution is paused.
     */
    public final State PAUSED = new State("pausing execution");

    /** Indicator that the execution is in the preinitialize phase.
     */
    public final State PREINITIALIZING = new State("preinitializing");

    /** Indicator that type resolution is being done.
     */
    public final State RESOLVING_TYPES = new State("resolving types");

    /** Indicator that the execution is in the wrapup phase.
     */
    public final State WRAPPING_UP = new State("wrapping up");

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a change listener. The listener
     *  will be notified of the execution of each change requested
     *  via the requestChange() method.
     *  If the listener is already in the list, do not add it again.
     *  @param listener The listener to add.
     *  @deprecated use addChangeListener on the toplevel composite actor 
     *  instead.
     */
    public void addChangeListener(ChangeListener listener) {
	((CompositeActor)getContainer()).addChangeListener(listener);
    }

    /** Add a listener to be notified when the model execution changes state.
     *  @param listener The listener.
     */
    public void addExecutionListener(ExecutionListener listener) {
        if(listener == null) return;
        if(_executionListeners == null) {
            _executionListeners = new LinkedList();
        }
        _executionListeners.add(listener);
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
            if (_debugging) _debug("Begin to iterate.");

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
        } finally {
	    try {
		wrapup();
	    } finally {
		// Wrapup may also throw an exception,
		// So be sure to reset the state to idle!
		if (_state != IDLE) {
		    _setState(IDLE);
		}
		// Reset this for the next run.
		_finishRequested = false;
		if (completedSuccessfully) {
		    _notifyListenersOfCompletion();
		}
	    }
        }
        // Report the execution time.
        long endTime = (new Date()).getTime();
        System.out.println("ptolemy.actor.Manager run(): elapsed time: "
                + (endTime - startTime) + " ms");
    }

    /** If the state is not IDLE, set a flag to request that
     *  execution stop and exit gracefully.
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
        // Set this regardless of whether the model is running to
        // avoid race conditions.  The model may not have gotten around
        // to starting when finish is requested.
        _finishRequested = true;
        if(_state == IDLE) return;

        CompositeActor container = (CompositeActor) getContainer();
        if(container == null) throw new InternalErrorException(
                "Attempted to call finish on an executing manager with no" +
                " associated model");
        container.stopFire();

	// Since Manager.resume() is synchronized, start a thread
	// to call resume() in order to avoid deadlock
	Thread resumeThread = new PtolemyThread( new Runnable() {
	    public void run() {
		resume();
	    }
	});
	resumeThread.start();
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

    /** Initialize the model.  This calls the preinitialize() method of
     *  the container, followed by the resolveTypes() and initialize() methods.
     *  Set the Manager's state to PREINITIALIZING and INITIALIZING as
     *  appropriate.
     *  This method is read synchronized on the workspace.
     *  @exception KernelException If the model throws it.
     *  @exception IllegalActionException If the model is already running, or
     *   if there is no container.
     */
    public synchronized void initialize()
            throws KernelException, IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (_state != IDLE) {
                throw new IllegalActionException(this,
                        "The model is already running.");
            }
            if (_container == null) {
                throw new IllegalActionException(this,
                        "No model to run!");
            }
            _setState(PREINITIALIZING);

            _pauseRequested = false;
            _typesResolved = false;
            _iterationCount = 0;

            // Initialize the topology
            _container.preinitialize();

            resolveTypes();
            _typesResolved = true;
            _setState(INITIALIZING);
            _container.initialize();

            // Since we have just initialized all actors, clear the
            // list of actors pending initialization.
            _actorsToInitialize.clear();
        } finally {
            _workspace.doneReading();
        }
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
     *  Set the state of the manager to ITERATING.
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
            _workspace.getReadAccess();
            if(_debugging) _debug("Process change requests.");
            _processChangeRequests();

            // Initialize actors that have been added.
            if (_actorsToInitialize.size() > 0) {
                Iterator actors = _actorsToInitialize.iterator();
                while (actors.hasNext()) {
                    Actor actor = (Actor)actors.next();
                    actor.preinitialize();
                }
            }
            if (!_typesResolved) {
                resolveTypes();
                _typesResolved = true;
            }

            _iterationCount++;
            _setState(ITERATING);

            // Set the appropriate write access, because we're about to
            // go into an iteration.
            if (!_needWriteAccess()) {
                _workspace.setReadOnly(true);
            }
            if(_debugging) _debug("Prefire container.");
            if (_container.prefire()) {
                // Invoke initialize on actors that have been added.
                if (_actorsToInitialize.size() > 0) {
                    Iterator actors = _actorsToInitialize.iterator();
                    while (actors.hasNext()) {
                        Actor actor = (Actor)actors.next();
                        actor.initialize();
                    }
                    _actorsToInitialize.clear();
                }
                if(_debugging) _debug("Fire container.");
                _container.fire();
                if(_debugging) _debug("Postfire container.");
                result = _container.postfire();
            }
            if(_debugging) {
                if (result) _debug("Finish one iteration, returning true.");
                else _debug("Finish one iteration, returning false.");
            }
        } finally {
            _workspace.setReadOnly(false);
            _workspace.doneReading();
        }
        return result;
    }

    /** Notify all the execution listeners of an exception.
     *  If there are no listeners, then print the exception information
     *  to the standard error stream. This is intended to be used by threads
     *  that are involved in an execution as a mechanism for reporting
     *  errors.  As an example, in a threaded domain, each thread
     *  should catch all exceptions and report them using this method.
     *  @param ex The exception.
     */
    public void notifyListenersOfException(Exception ex) {
	String errorMessage = new String("Exception Caught:" + ex.getClass());
	errorMessage += "(" + ex.getMessage() + ")";
        _debug(errorMessage);
        if (_executionListeners == null) {
            System.err.println(errorMessage);
            ex.printStackTrace();
        } else {
            Iterator listeners = _executionListeners.iterator();
            while(listeners.hasNext()) {
                ExecutionListener listener =
                    (ExecutionListener) listeners.next();
                listener.executionError(this, ex);
            }
        }
    }

    /** Set a flag requesting that execution pause at the next opportunity
     *  (between iterations).  Call stopFire() on the toplevel composite
     *  actor to ensure that the manager's execution thread becomes active
     *  again.   This is necessary in the case of PN, where an iteration
     *  only ends if deadlock occurs, which may never happen.
     *  The thread controlling the execution will be
     *  suspended the next time through the iteration loop.  To resume
     *  execution, call resume() from another thread.
     *  @see Executable#stopFire
     */
    public void pause() {
        _pauseRequested = true;
        CompositeActor container = (CompositeActor) getContainer();
        if(container == null) throw new InternalErrorException(
                "Attempted to call finish on an executing manager with no" +
                " associated model");
        container.stopFire();
    }

    /** Remove a change listener. If the specified listener is not
     *  on the list, do nothing.
     *  @param listener The listener to remove.
     *  @deprecated use method in CompsiteActor instead.
     */
    public void removeChangeListener(ChangeListener listener) {
	((CompositeActor)getContainer()).removeChangeListener(listener);
    }

    /** Remove a listener from the list of listeners that are notified
     *  of execution events.  If the specified listener is not on the list,
     *  do nothing.
     *  @param listener The listener to remove.
     */
    public void removeExecutionListener(ExecutionListener listener) {
        if(listener == null || _executionListeners == null) return;
        _executionListeners.remove(listener);
    }

    /** Queue a change request.
     *  The indicated change will be executed at the next opportunity
     *  between top-level iterations of the model. For the
     *  benefit of process-oriented domains, which may not have finite
     *  iterations, this method also calls stopFire() on the top-level
     *  composite actor, requesting that directors in such domains
     *  return from their fire() method as soon as practical.
     *  When the model is idle (initialize() has not yet been
     *  invoked), carry out the change request before returning. That is,
     *  this method will block until the change request has been
     *  processed. An exception is thrown by this method if the change 
     *  request fails, but this can only occur when the model is idle.
     *  @param change The requested change.
     *  @exception ChangeFailedException If the model is idle and the
     *  change request fails.
     */
    public void requestChange(ChangeRequest change) 
	throws ChangeFailedException {
	// If the model is idle (i.e., initialize() has not yet been
	// invoked), then process the change request right now.
	if (_state == IDLE) {
	    change.execute();
	    _notifyChangeListeners(change);
	} else {
	    // Otherwise, we must be executing, so queue the request
	    // to happen later.
	    // Create the list of requests if it doesn't already exist
	    if (_changeRequests == null) {
		_changeRequests = new LinkedList();
	    }
	    _changeRequests.add(change);

	    // Request that the toplevel composite stop executing.
	    // This is mainly for use with process oriented domains.
	    CompositeActor container = (CompositeActor) getContainer();
	    container.stopFire();
	}
    }

    /** Queue an initialization request.
     *  The specified actor will be initialized at an appropriate time,
     *  in the iterate() method, by calling its preinitialize()
     *  and initialize() methods.  This method should be called when an
     *  actor is added to a model through a mutation in order to properly
     *  initialize the actor.
     *  @param actor The actor to initialize.
     */
    public void requestInitialization(Actor actor) {
        _actorsToInitialize.add(actor);
    }

    /** Check types on all the connections and resolve undeclared types.
     *  If the container is not an instance of TypedCompositeActor,
     *  do nothing.
     *  Set the Manager's state to RESOLVING_TYPES.
     *  This method is write-synchronized on the workspace.
     *  @exception TypeConflictException If a type conflict is detected.
     */
    public void resolveTypes() throws TypeConflictException {
        if ( !(_container instanceof TypedCompositeActor)) {
            return;
        }
	try {
	    _workspace.getWriteAccess();
            _setState(RESOLVING_TYPES);
            if (_debugging) _debug("Resolving types.");

	    List conflicts = new LinkedList();
            List typeConflicts =
                ((TypedCompositeActor)_container).checkTypes();
            conflicts.addAll(typeConflicts);

            List constraintList =
                ((TypedCompositeActor)_container).typeConstraintList();
	    if (constraintList.size() > 0) {
                InequalitySolver solver = new InequalitySolver(
                        TypeLattice.lattice());
            	Iterator constraints = constraintList.iterator();
	        while (constraints.hasNext()) {
                    Inequality ineq = (Inequality)constraints.next();
                    solver.addInequality(ineq);
	        }

                // find the least solution (most specific types)
                boolean resolved = solver.solveLeast();
                if ( !resolved) {
		    Iterator unsatisfied = solver.unsatisfiedInequalities();
		    while (unsatisfied.hasNext()) {
		        Inequality ineq = (Inequality)unsatisfied.next();
		        InequalityTerm term =
                            (InequalityTerm)ineq.getLesserTerm();
		        Object typeObj = term.getAssociatedObject();
		        if (typeObj != null) {
			    // typeObj is a Typeable
			    conflicts.add(typeObj);
		        }

		        term = (InequalityTerm)ineq.getGreaterTerm();
		        typeObj = term.getAssociatedObject();
		        if (typeObj != null) {
			    // typeObj is a Typeable
			    conflicts.add(typeObj);
		        }
		    }
                }

	        // check whether resolved types are acceptable.
                // They might be, for example, NaT.
	        Iterator variableTerms = solver.variables();
	        while (variableTerms.hasNext()) {
		    InequalityTerm term = (InequalityTerm)variableTerms.next();
		    if ( !term.isValueAcceptable()) {
		        conflicts.add(term.getAssociatedObject());
		    }
	        }
	    }

	    if (conflicts.size() > 0) {
		throw new TypeConflictException(conflicts,
                        "Type conflicts occurred in " + _container.getFullName()
                        + " on the following Typeables:");
	    }
	} catch (IllegalActionException iae) {
	    // this should not happen.
	    throw new InternalErrorException(iae.getMessage());
	} finally {
	    _workspace.doneWriting();
	}
    }

    /** If the model is paused, resume execution.  This method must
     *  be called from a different thread than that controlling the
     *  execution, since the thread controlling the execution is
     *  suspended.
     */
    public synchronized void resume() {
        if(_state == PAUSED) {
            _pauseRequested = false;
            notifyAll();
        }
    }

    /** Execute the model, catching all exceptions. Use this method to
     *  execute the model within the calling thread, but to not throw
     *  exceptions.  Instead, the exception is handled using the
     *  <code>notifyListenersOfException</code> method.  Except for its
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
     *  the <code>notifyListenersOfException</code> method.
     *  @exception IllegalActionException If the model is already running,
     *  e.g. the state is not IDLE.
     */
    public void startRun() throws IllegalActionException {
        if (_state != IDLE) {
            throw new IllegalActionException(this,
                    "Model is " + _state.getDescription());
        }
        // Set this within the calling thread to avoid race conditions
        // where finish() might be called before the spawned thread
        // actually starts up.
        _finishRequested = false;
        _thread = new PtolemyThread(this);
	_thread.setPriority(Thread.MIN_PRIORITY);
        _thread.start();
    }

    /** Terminate the currently executing model with extreme prejudice.
     *  This leaves the state of the manager in CORRUPTED, which means
     *  that the model cannot be executed again.  A new model must be
     *  created, with a new manager, to execute again.
     *  This method is not intended to be used as a normal route of
     *  stopping execution. To normally stop execution, call the finish()
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
     *  happen as soon as possible.
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

    /** Wrap up the model by invoking the wrapup method of the toplevel
     *  composite actor.  The state of the manager will be set to
     *  WRAPPING_UP.
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
     *  removed from the directory of the workspace.  If the composite
     *  actor is null, then the manager is <b>not</b> returned to the
     *  directory of the workspace, which may result in it being
     *  garbage collected.  This method should not be called directly.
     *  Instead, call setManager in the CompositeActor class (or a
     *  derived class).
     */
    protected void _makeManagerOf(CompositeActor ca) {
        if (ca != null) {
            _workspace.remove(this);
        }
        _container = ca;
    }

    /** Notify listeners that execution has completed successfully.
     */
    protected void _notifyListenersOfCompletion() {
        if (_debugging) {
            _debug("Completed execution with "
                    + _iterationCount + " iterations");
        }
        if (_executionListeners != null) {
            Iterator listeners = _executionListeners.iterator();
            while(listeners.hasNext()) {
                ExecutionListener listener =
                    (ExecutionListener) listeners.next();
                listener.executionFinished(this);
            }
        }
    }

    /** Propagate the state change event to all the execution listeners.
     */
    protected void _notifyListenersOfStateChange() {
        if (_debugging) {
            _debug(_state.getDescription());
        }
        if (_executionListeners != null) {
            String msg = _state.getDescription();
            Iterator listeners = _executionListeners.iterator();
            while(listeners.hasNext()) {
                ExecutionListener listener =
                    (ExecutionListener) listeners.next();
                listener.managerStateChanged(this);
            }
        }
    }

    /** Check whether write access is needed during an
     *  iteration. This is done by asking the directors.
     *  This method calls the needWriteAccess() method of
     *  the top level director, which will in turn query any inside
     *  directors.
     */
    protected boolean _needWriteAccess() {
        if (_writeAccessVersion == _workspace.getVersion()) {
            return _writeAccessNeeded;
        }
        _writeAccessNeeded = _container.getDirector().needWriteAccess();
        _writeAccessVersion = _workspace.getVersion();
        return _writeAccessNeeded;
    }

    /** Notify all change listeners that the given request has completed
     *  without throwing an exception.  The notification is deferred to
     *  the containing actor, which has had all the change listeners attached
     *  to it.
     */
    protected void _notifyChangeListeners(ChangeRequest request) {
	((CompositeActor)getContainer())._notifyChangeListeners(request);
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
            LinkedList clonedList = new LinkedList(_changeRequests);

            // Clear the request queue.  We want to discard the queue even
            // if the changes fail.
            // Otherwise, we could get stuck not being able to do anything
            // further with the model.
            _changeRequests = null;

            Iterator enum = clonedList.iterator();
            while (enum.hasNext()) {
                ChangeRequest request = (ChangeRequest)enum.next();
                request.execute();

                // Inform all listeners. Of course, this won't happen
                // if the change request failed
		_notifyChangeListeners(request);
            }
        }
    }

    /** Set the state of execution and notify listeners if the state
     *  actually changes.
     *  @param newState The new state.
     */
    protected void _setState(State newState) {
        if (_state != newState) {
            _state = newState;
            _notifyListenersOfStateChange();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A list of actors with pending initialization.
    private List _actorsToInitialize = new LinkedList();

    // A list of pending changes.
    private List _changeRequests;

    // The top-level CompositeActor that contains this Manager
    private CompositeActor _container = null;

    // Listeners for execution events.
    private List _executionListeners;

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

	/** Print out the current state.
	 */
	public String toString() {
	    return new String("Manager " + getManager() + 
			      " is in state " + getDescription());
	}

        private String _description;
    }
}
