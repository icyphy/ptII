/* A Manager governs the execution of an entire simulation.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.actor;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;

import collections.LinkedList;
import collections.HashedSet;
import java.util.Enumeration;
import java.lang.reflect.*;


//////////////////////////////////////////////////////////////////////////
//// Manager
/**
A Manager is a domain-independent object that manages the execution of
a model.   It provides several methods to control execution with: run()
startRun(), pause(), resume(), terminate(), and finish().
Most often, methods in this object will be called by a
graphical user interface.  However, it is possible to manually call
these methods from a java object, a java applet, or an interactive
prompt, such as TclBlend.
Because user interaction will likely be occuring asynchronously to the
execution of the model, it is important that all the processing for the
model occur in a separate thread.   The Manager is responsible for creating
and managing the Java thread in which execution begins, although some
domains may spawn additional threads of their own.
<p>
Note that the Manager class implements the Runnable interface with the run()
method implements the execution of the model. The run() method call is a
blocking method call, in the sense that the flow of execution will be returned
to the caller only after the run() method finishes. On the other hand, the
startRun() method call is a non-blocking method call. It creates a separate
thread running the execution and the flow of execution will be returned
immediately to the caller.
<p>
Manager also tries to optimize the simulation by making the workspace
<i>write-protected</i> during the iteration period when all the
directors 'agree'.
Calling getReadAccess() and doneReading() on a <i>write-protected</i>
workspace will return immediately (No writer, no problem). On the other hand,
calling getWriteAccess() and doneWriting() on a <i>write-protected</i>
workspace will result in an exception being thrown, so don't write-protect the
workspace if write access will ever be needed. A domain-specific director, by
default, will not 'agree' to have the workspace write-protected. To override
the default behaviour, override the Director._writeAccessPreference() method.

@author Steve Neuendorffer, Lukito Muliadi
// Contributors: Mudit Goel, Edward A. Lee
@version $Id$
*/

public final class Manager extends NamedObj implements Runnable {

    /** Construct a manager in the default workspace with an empty string
     *  as its name. The manager is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public Manager() {
        super();
        _ExecutionListeners = new HashedSet();
    }

    /** Construct a manager in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The manager is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this Manager.
     */
    public Manager(String name) {
        super(name);
        _ExecutionListeners = new HashedSet();
    }

    /** Construct a manager in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The manager is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this Manager.
     */
    public Manager(Workspace workspace, String name) {
        super(workspace, name);
        _ExecutionListeners = new HashedSet();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the ExecutionListener to the set of ExecutionListeners.
     *  The ExecutionListener will be notified when the appropriate
     *  ExecutionEvents occur.
     *  @param an ExecutionListener
     */
    public void addExecutionListener(ExecutionListener el) {
        if(el == null) return;
        _ExecutionListeners.include((Object) el);
    }

    /**
     * Set a flag to request that the thread in which execution is running
     * stop execution and exit gracefully.    The next time that execution
     * control is returned to the manager, the manager will act as if the
     * toplevel composite actor returned false in postfire.
     * Execution can be stopped immediately by calling the terminate() method.
     * This thread is synchronized so that it runs atomically with respect to
     * other methods in Manager that control the simulation thread.
     * This method is non-blocking.
     */
    public synchronized void finish() {
        _keepIterating = false;
        _isPaused = false;
        if(_simulationThread != null) {
            synchronized(_simulationThread) {
                _simulationThread.notify();
            }
        }
    }

    /** Encapsulate the Exception with an ExecutionEvent and call
     *  ExecutionError in all the ExecutionListeners.   If there are
     *  no ExecutionListeners, then print the exception's stack trace on
     *  the console.
     *
     *  @param e The Exception
     **/
    public void fireExecutionError(Exception e) {
        // if any exceptions get up to this level, then we have to tell
        // the gui by enscapsulating in an event.
        ExecutionEvent event = new ExecutionEvent(this, _iteration, e);
        Enumeration listeners = _ExecutionListeners.elements();
        // if nobody is listening, then just dump the stack trace.
        if(!listeners.hasMoreElements()) {
            e.printStackTrace();
        }
        _fireExecutionEvent(_ExecutionEventType.EXECUTIONERROR, event);
    }


    /** Return the toplevel composite actor for which this manager
     *  controls execution.   This composite actor does not have a parent, and
     *  contains the entire hierarchy for an execution.
     *  @return The CompositeActor that this Manager is responsible for.
     */
    public CompositeActor getToplevel() {
        return _toplevel;
    }

    /** If an execution is currently running, then set a flag requesting that
     *  execution pause at the next available opportunity between toplevel
     *  iterations.   When the pause flag is detected, the
     *  simulation thread will suspend itself and issue the
     *  ExecutionPaused ExecutionEvent to all ExecutionListeners.
     *  This thread is synchronized so that it runs atomically with respect to
     *  the other methods in manager that control the simulation thread.
     *  This call is non-blocking.
     */
    public synchronized void pause() {
        if(_keepIterating) _isPaused = true;
    }

    /** Remove the ExecutionListener to the set of ExecutionListeners.
     *  The ExecutionListener will be no longer be notified when
     *  ExecutionEvents occur.
     *  @param an ExecutionListener
     */
    public void removeExecutionListener(ExecutionListener el) {
        if(el == null) return;
        _ExecutionListeners.exclude((Object) el);
    }

    /** Check types on all the connections and resolve undeclared types.
     *  If the container is not an instance of TypedCompositeActor,
     *  do nothing.
     *  This method is write-synchronized on the workspace.
     *  @exception TypeConflictException If type conflict is detected in
     *   the containing TypedCompositeActor.
     */
    public void resolveTypes()
	    throws TypeConflictException {
	try {
	    workspace().getWriteAccess();
            CompositeActor toplevel = (CompositeActor)getToplevel();
            if ( !(toplevel instanceof TypedCompositeActor)) {
                return;
            }

	    LinkedList conflicts = new LinkedList();
	    conflicts.appendElements(
                    ((TypedCompositeActor)toplevel).checkTypes());

            Enumeration constraints =
                ((TypedCompositeActor)toplevel).typeConstraints();

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
		    Enumeration unsatisfied = solver.unsatisfiedInequalities();
		    while (unsatisfied.hasMoreElements()) {
		        Inequality ineq =
                            (Inequality)unsatisfied.nextElement();
		        TypeTerm term = (TypeTerm)ineq.getLesserTerm();
		        TypedIOPort port = term.getPort();
		        if (port != null) {
			    conflicts.insertLast(port);
		        }
		        term = (TypeTerm)ineq.getGreaterTerm();
		        port = term.getPort();
		        if (port != null) {
			    conflicts.insertLast(port);
		        }
		    }
                }

	        // check if any resolved type is NaT, or abstract, or interface
	        Enumeration var = solver.variables();
	        while (var.hasMoreElements()) {
		    TypeTerm term = (TypeTerm)var.nextElement();
		    TypedIOPort port = term.getPort();
		    Class type = port.getResolvedType();
		    if ( !TypeLattice.isInstantiableType(type)) {
		        conflicts.insertLast(port);
		    }
	        }
	    }

	    if (conflicts.size() > 0) {
		throw new TypeConflictException(conflicts.elements(),
                        "Type conflicts occurred in " + toplevel.getFullName());
	    }
	} catch (IllegalActionException iae) {
	    // this should not happen.
	    throw new InternalErrorException(iae.getMessage());
	} finally {
	    workspace().doneWriting();
	}
    }

    /** If running and paused, resume the currently paused simulation by
     *  turning off the paused flag and waking the simulation thread up.
     *  This thread is synchronized so that it runs atomically with respect to
     *  the other methods in manager that control the simulation thread.
     */
    public synchronized void resume() {
        if(_keepIterating && _isPaused) {
            _isPaused = false;
            if(_simulationThread != null) {
                synchronized (_simulationThread) {
                    _simulationThread.notify();
                }
            }
        }
    }

    /**
     * Run the sequence of execution.  This method executes the toplevel
     * composite actor until it returns false in postfire.
     * The execution begins by calling initialize() on the toplevel
     * composite actor.   Each iteration of execution consists of calling
     * prefire(), fire() and postfire() on the top-level composite actor.
     * If postfire returns false, then execution is finished by calling
     * wrapup() on the toplevel composite actor to clean up the execution.
     * <p>
     * The execution is performed by the
     * current thread. I.e. The execution is performed in 'foreground' and
     * the method returns only after the execution finishes.  However, it is
     * possible for other threads to come in and pause(), resume(),
     * terminate() or finish() the execution of the simulation.
     * To start the simulation in 'background', use the startRun() method.
     * startRun() creates a new thread which executes this run() method and
     * then returns immediately.
     */
    public void run() {

        if (_simulationThread == null) {
            // If the simulation is started by calling the run() method
            // (as opposed to calling the startRun() method) then
            // _simulationThread will not be initialized, i.e. equal to null.
            _simulationThread = Thread.currentThread();
        } else {
            System.out.println("There's already a thread running the " +
                    "simulation. Either wait for it to end, or call " +
                    "terminate() or finish().");
            return;
        }

        CompositeActor toplevel = ((CompositeActor)getToplevel());

        // ensure that we only have one execution running.
        synchronized(this) {
            // Check if the execution is still iterating.
            if(_keepIterating) {
                System.out.println("The simulation is still iterating.");
                return;
            }
            _keepIterating = true;
            _isPaused = false;
            _iteration = 0;
	    _typeResolved = false;
        }

        // Notify all the listeners that execution has started.
        ExecutionEvent event = new ExecutionEvent(this);
        _fireExecutionEvent(_ExecutionEventType.EXECUTIONSTARTED, event);

        try {
            try {
                // Initialize the topology
                toplevel.initialize();

                // Figure out the appropriate write access.
                _needWriteAccessDuringIteration =
                    _needWriteAccess();

                // Call _iterate() until:
                // _keepIterating is set to false (presumably by stop())
                // postfire() returns false.
                while (_keepIterating && _iterate()) {

                    try {
                        // if a pause has been requested
                        if(_isPaused) {
                            // Notify listeners that we are paused.
                            event =
                                new ExecutionEvent(this, _iteration);
                            _fireExecutionEvent(
                                    _ExecutionEventType.EXECUTIONPAUSED, event);

                            synchronized (_simulationThread) {
                                // suspend this thread until
                                // somebody wakes us up.
                                while(_isPaused)
                                    _simulationThread.wait();
                            }

                            // Somebody woke us up, so notify all the
                            // listeners that we are resuming.
                            event =
                                new ExecutionEvent(this, _iteration);
                            _fireExecutionEvent(
                                    _ExecutionEventType.EXECUTIONRESUMED,
                                    event);
                        }
                    }
                    catch (InterruptedException e) {
                        // We don't care if we were interrupted..
                        // Just ignore.
                    }

                } // while (_keepIterating && _iterate())
            }
            finally {
                // if we are done, then always be sure to reset the flags.
                _keepIterating = false;
                _isPaused = false;

                // try to wrapup the topology.
                toplevel.wrapup();

                // notify all listeners that we have been stopped.
                event =
                    new ExecutionEvent(this, _iteration);
                _fireExecutionEvent(_ExecutionEventType.EXECUTIONFINISHED,
                        event);
            }
        } catch (Exception e) {
            fireExecutionError(e);
        }

        // The simulation is finished, the thread has finished its job.
        _simulationThread = null;

    }

    /** Start an execution that will run for an unspecified number of
     *  toplevel iterations.   This will normally be stopped by
     *  calling finish(), terminate(), or returning false in a postfire method.
     *  This method is non-blocking, i.e. it runs on 'background'
     *  <p>
     *  If this method is called while the simulation is still running, then
     *  the call is 'buffered' and the simulation will rerun itself
     *  immediately after the current running simulation ends. Since this
     *  method is synchronized, it is not possible to call this method
     *  while there is a 'buffered' run request.
     *  @see Manager#run()
     */
    public synchronized void startRun() {

        if(_keepIterating) return;

        // If the previous run hasn't totally finished yet, then be sure
        // it is good and dead before continuing.

        if(_simulationThread != null) {
            _simulationThread.stop();
            try {
                _simulationThread.join();
            }
            catch (InterruptedException e) {
                // Well, if we bothered to kill it, then this should
                // always get thrown, so just ignore it.
            }
            _simulationThread = null;
        }
        Thread futureRunningThread = new PtolemyThread(this);
        futureRunningThread.start();
    }

    /** Terminate any currently executing simulation with extreme prejudice.
     *  Kill the main simulation thread and call terminate on the toplevel
     *  container.   This should cause any actors to free up any resources
     *  they have allocated and Directors should kill any threads they have
     *  created.   However, a consistent state is not guaranteed.   The
     *  topology should probably be recreated before attempting any
     *  further operations.   This is not synchronized because we want it to
     *  happen as soon as possible, no matter what.
     */
    public void terminate() {

        // kill the main thread and wait for it to die.
        if(_simulationThread != null) {
            _simulationThread.stop();
            try {
                _simulationThread.join();
            }
            catch (InterruptedException e) {
                // This will usually get thrown, since we are
                // forcibly terminating
                // the thread.   We just ignore it.
            }
            _simulationThread = null;
        }
        // Terminate the entire hierarchy as best we can.
        CompositeActor toplevel = ((CompositeActor)getToplevel());
        toplevel.terminate();

        // notify all execution listeners that execution was terminated.
        ExecutionEvent event = new ExecutionEvent(this);
        _fireExecutionEvent(_ExecutionEventType.EXECUTIONTERMINATED, event);

        _keepIterating = false;
        _isPaused = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make this Manager the Manager of the specified composite
     *  actor.  This method should not be called directly.  Instead, call
     *  setManager of the CompositeActor class (or a derived class).
     *  If the argument is not the toplevel CompositeActor, then we throw
     *  an InvalidStateException.
     */
    protected void _makeManagerOf(CompositeActor ca) {
        if (ca != null) {
            if(ca.getContainer() != null)
                throw new InvalidStateException("Manager's container must " +
                        "be the toplevel CompositeActor!");
            workspace().remove(this);
        }
        _toplevel = ca;
    }




    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Propagate the execution event to all the execution listeners.
     */
    private void _fireExecutionEvent(_ExecutionEventType type,
            ExecutionEvent event) {
        Enumeration listeners = _ExecutionListeners.elements();
        while(listeners.hasMoreElements()) {
            ExecutionListener l =
                (ExecutionListener) listeners.nextElement();
            if(type == _ExecutionEventType.EXECUTIONSTARTED)
                l.executionStarted(event);
            else if(type == _ExecutionEventType.EXECUTIONPAUSED)
                l.executionPaused(event);
            else if(type == _ExecutionEventType.EXECUTIONRESUMED)
                l.executionResumed(event);
            else if(type == _ExecutionEventType.EXECUTIONERROR)
                l.executionError(event);
            else if(type == _ExecutionEventType.EXECUTIONFINISHED)
                l.executionFinished(event);
            else if(type == _ExecutionEventType.EXECUTIONTERMINATED)
                l.executionTerminated(event);
            else if(type == _ExecutionEventType.ITERATIONSTARTED)
                l.executionIterationStarted(event);

        }
    }

    /** Invoke one iteration.  An iteration consists of
     *  invocations of prefire(), fire(), and postfire(), in that
     *  order.  Prefire() will be called at the beginning of an iteration.
     *  If prefire() returns false, then fire() and postfire() are not
     *  invoked.   Otherwise, fire() will be called once, followed by
     *  invocation of postfire(). If postfire()
     *  returns false, then the execution will be terminated.
     *  This method is read-synchronized on the workspace.
     *
     *  @return True if postfire() returns true.
     *  @exception IllegalActionException If any of the called methods
     *   throws it.
     */
    private boolean _iterate() throws IllegalActionException {

        _iteration++;

        CompositeActor toplevel = (CompositeActor)getToplevel();
        if (toplevel == null) {
            throw new InvalidStateException("Manager "+ getName() +
                    " attempted execution with no topology to execute!");
        }

        ExecutionEvent event = new ExecutionEvent(this, _iteration);
        _fireExecutionEvent(_ExecutionEventType.ITERATIONSTARTED, event);

        // Toplevel mutations will occur here.

        try {
            workspace().getReadAccess();

	    try {
		if (!_typeResolved) {
                    resolveTypes();
                    _typeResolved = true;
                }
            }
            catch (TypeConflictException e) {
                fireExecutionError(e);
            }

            // Set the appropriate write access, because we're about to
            // go into an interation.
            try {
                if (!_needWriteAccessDuringIteration) {
                    workspace().setReadOnly(true);
                }

                if (toplevel.prefire()) {
                    toplevel.fire();
                    return toplevel.postfire();
                }
                return false;
            } finally {
                if (!_needWriteAccessDuringIteration) {
                    workspace().setReadOnly(false);
                }
            }
        } finally {
            workspace().doneReading();
        }
    }



    /** Check if write access will be needed in the workspace during an
     *  iteration. An iteration consists of invocations of the prefire(),
     *  fire(), and postfire() methods of the top level composite actor in
     *  that order.
     *  <p>
     *  This method recursively calls the needWriteAccess() method of all lower
     *  level directors. Intuitively, the workspace will only be made
     *  read-only, if all the directors permit it.
     */
    private boolean _needWriteAccess() {
        // Get the top level composite actor.
        CompositeActor toplevel = (CompositeActor)getToplevel();
        if (toplevel == null) {
            throw new InvalidStateException("Manager "+ getName() +
                    " attempted execution with no topology to execute!");
        }
        // Call the needWriteAccess() method of the local director of the
        // top level composite actor.
        return toplevel.getDirector().needWriteAccess();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Inner Class                       ////

    private static final class _ExecutionEventType {

        private _ExecutionEventType(String name) {this._name = name;}

        public static final _ExecutionEventType EXECUTIONSTARTED =
        new _ExecutionEventType("Execution Started");
        public static final _ExecutionEventType EXECUTIONPAUSED =
        new _ExecutionEventType("Execution Paused");
        public static final _ExecutionEventType EXECUTIONRESUMED =
        new _ExecutionEventType("Execution Resumed");
        public static final _ExecutionEventType EXECUTIONERROR =
        new _ExecutionEventType("Execution Error");
        public static final _ExecutionEventType EXECUTIONFINISHED =
        new _ExecutionEventType("Execution Finished");
        public static final _ExecutionEventType EXECUTIONTERMINATED =
        new _ExecutionEventType("Execution Terminated");
        public static final _ExecutionEventType ITERATIONSTARTED =
        new _ExecutionEventType("Iteration Started");

        public String toString() {return _name;}

        private String _name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The toplevel CompositeActor that contains this Manager
    private CompositeActor _toplevel = null;

    // indicate whether the execution should keep iterating.
    // This flag is set to false to indicate an early end. The execution will
    // progress until the end of this iteration and that's it.
    private boolean _keepIterating;

    // a flag to request that simulation is paused.
    private boolean _isPaused;

    // Count the number of iterations completed.
    private int _iteration;

    // A flag to indicate whether the workspace will be read-only during
    // an iteration (i.e. during prefire(), fire() and postfire()).
    private boolean _needWriteAccessDuringIteration;

    // _simulationThread is the thread that's executing the run() method.
    // It should be non-null whenever the simulation is still running (i.e.
    // the run() method hasn't finish yet) and should be set to null after
    // simulation ends.
    private Thread _simulationThread;

    // Listeners for ExecutionEvent.
    private HashedSet _ExecutionListeners;

    // FIXME: a hack until mutation got implemented.
    private boolean _typeResolved = false;
}




