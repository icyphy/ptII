/* A Manager governs the execution of a model.

 Copyright (c) 1997-2003 The Regents of the University of California.
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
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
execute catches Exceptions and rethrows them as runtime exceptions. why?
requestInitialization is pickier about what actors are initialized
*/

package ptolemy.actor;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.PtolemyThread;
import ptolemy.kernel.util.Workspace;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// Manager
/**
A Manager governs the execution of a model in a domain-independent way.
Its methods are designed to be called by a GUI, an applet, a command-line
interface, or the top-level code of an application.  The manager can
execute the model in the calling thread or in a separate thread.
The latter is useful when the caller wishes to remain live during
the execution of the model.

<p> There are three methods that can be used to start execution of a
system attached to the manager.  The execute() method is the most
basic way to execute a model.  The model will be executed
<i>synchronously</i>, meaning that the execute() method will return
when execution has completed.  Any exceptions that occur will be
thrown by the execute method to the calling thread, and will not be
reported to any execution listeners.  The run() method also initiates
synchronous execution of a model, but additionally catches all
exceptions and passes them to the notifyListenersOfException() method
<i>without throwing them to the calling thread</i>.  The startRun()
method, unlike the previous two techniques, begins <i>asynchronous</i>
execution of a model.  This method starts a new thread for execution
of the model and then returns immediately.  Exceptions are reported
using the notifyListenersOfException() method.

<p> In addition, execution can be manually driven, one phase at a
time, using the methods initialize(), iterate() and wrapup().  This is
most useful for testing purposes.  For example, a type system check
only needs to get the resolved types, which are found during
initialize, so the test can avoid actually executing the system.
Also, when testing mutations, the model can be examined after each
toplevel iteration to ensure the proper behavior.

<p> A manager provides services for cleanly handling changes to the
topology.  These include such changes as adding or removing an entity,
port, or relation, creating or destroying a link, and changing the
value or type of a parameter.  Collectively, such changes are called
<i>mutations</i>. Usually, mutations cannot safely occur at arbitrary
points in the execution of a model.  Models can queue mutations with
any object in the hierarchy or with the manager using the
requestChange() method.  An object in the hierarchy simply delegates
the request to its container, so the request propagates up the
hierarchy until it gets to the top level composite actor, which
delegates to the manager, which performs the change at the earliest
opportunity.  In this implementation of Manager, the changes are
executed between iterations.

<p> A service is also provided whereby an object can be registered
with the composite actor as a change listener.  A change listener is
informed when mutations that are requested via requestChange() are
executed successfully, or when they fail with an exception.

<p> Manager can optimize the performance of an execution by making the
workspace <i>write protected</i> during an iteration, if all relevant
directors permit this.  This removes some of the overhead of obtaining
read and write permission on the workspace.  By default, directors do
not permit this, but many directors explicitly relinquish write access
to allow faster execution.  Such directors are declaring that they
will not make changes to the topology during execution.  Instead, any
desired mutations are delegated to the manager via the requestChange()
method.

<p> Many domains make use of static analyses for performing, e.g.,
static scheduling of actor firings.  In some cases, these analyses
must make use of global information.  The class provides a centralized
mechanism for managing such global analyses.  During preinitialize,
domains can invoke the getAnalysis and addAnalysis methods to create a
global analysis.  It is up to the users of this mechanism to ensure
that a particular type of analysis is only created once, if that is
what is required.  After preinitialize, the manager clears the list of
analyses, to avoid unnecessary memory usage, and to ensure that the
analyses are performed again on the next invocation of the model.
This is somewhat preferable to tying a cache of analysis information
to the version of the workspace, since the version number of the
workspace itself may change during preinitialize as domains add
annotation to the model.

@author Steve Neuendorffer, Lukito Muliadi, Edward A. Lee, Elaine Cheong
// Contributors: Mudit Goel, John S. Davis II
@version $Id$
@since Ptolemy II 0.2
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
    public final static State CORRUPTED = new State("corrupted");

    /** Indicator that there is no currently active execution.
     */
    public final static State IDLE = new State("idle");

    /** Indicator that the execution is in the initialize phase.
     */
    public final static State INITIALIZING = new State("initializing");

    /** Indicator that the execution is in an iteration.
     */
    public final static State ITERATING = new State("executing");

    /** Indicator that the execution is in the mutations phase.
     */
    public final static State MUTATING = new State("processing mutations");

    /** Indicator that the execution is paused.
     */
    public final static State PAUSED = new State("pausing execution");

    /** Indicator that the execution is paused on a breakpoint.
     */
    public final static State PAUSED_ON_BREAKPOINT = new State(
            "pausing execution on a breakpoint");

    /** Indicator that the execution is in the preinitialize phase.
     */
    public final static State PREINITIALIZING = new State("preinitializing");

    /** Indicator that type resolution is being done.
     */
    public final static State RESOLVING_TYPES = new State("resolving types");

    /** Indicator that the execution is in the wrapup phase.
     */
    public final static State WRAPPING_UP = new State("wrapping up");

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a static analysis to this manager.
     */
    public void addAnalysis(String name, Object analysis) {
        if(_nameToAnalysis == null) {
            _nameToAnalysis = new HashMap();
        }
        _nameToAnalysis.put(name, analysis);
    }

    /** Add a listener to be notified when the model execution changes state.
     *  @param listener The listener.
     */
    public void addExecutionListener(ExecutionListener listener) {
        if (listener == null) return;
        if (_executionListeners == null) {
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

        _debug("-- Manager execute() called.");

        // Reset this in case finish() has been called since the last run.
        _finishRequested = false;

        boolean completedSuccessfully = false;
        try {
            initialize();
            // Call iterate() until finish() is called or postfire()
            // returns false.
            _debug("-- Manager beginning to iterate.");

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

            //   } catch (Exception e) {
            //       throw new InternalErrorException(this, e, "Manager");

            //   } catch (Exception e) {
            //       System.err.println("Error caught by manager.");
            //       e.printStackTrace();
            //       throw new RuntimeException("Manager: " + e.getMessage());

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
        System.out.println(timeAndMemory(startTime));
    }

    /** If the state is not IDLE, set a flag to request that
     *  execution stop and exit in a completely deterministic fashion
     *  at the end of the next toplevel iteration.  This method may
     *  be called from within an actor to stop the execution of the model.
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
        if (_debugging) {
            _debug("finish() has been called.");
        }
        if (_state == IDLE) return;

        Nameable container = getContainer();
        if (!(container instanceof CompositeActor)) {
            throw new InternalErrorException(
                    "Attempted to call finish() on an executing manager " +
                    "with no associated CompositeActor model");
        }
        // Used to just call stopFire() here, but this does not set
        // the flag in the director so that isStopRequested() returns
        // true. We have to set that flag or the actor threads in
        // threaded domains will not know that a stop has been requested
        // (vs. a pause).
        ((CompositeActor)container).stop();

        // Since Manager.resume() is synchronized, start a thread
        // to call resume() in order to avoid deadlock
        Thread resumeThread = new PtolemyThread( new Runnable() {
                public void run() {
                    resume();
                }
            });
        resumeThread.start();
    }

    /** Get the analysis with the given name, or return null if no such 
     *  analysis exists.
     */
    public Object getAnalysis(String name) {
        if(_nameToAnalysis == null) {
            return null;
        } else {
            return _nameToAnalysis.get(name);
        }
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

            preinitializeAndResolveTypes();

            _setState(INITIALIZING);
            _container.initialize();

            // Since we have just initialized all actors, clear the
            // list of actors pending initialization.
            _actorsToInitialize.clear();

            _processChangeRequests();
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
     *  Note that this method ignores finish and pause requests
     *  and thus determines a minimum granularity of the execution.
     *  Set the state of the manager to ITERATING.
     *  This method is read synchronized on the workspace.
     *
     *  @return True if postfire() returns true.
     *  @exception KernelException If the model throws it, or if there
     *   is no container.
     */
    public boolean iterate() throws KernelException {
        if (_container == null) {
            throw new IllegalActionException(this,
                    "No model to execute!");
        }
        boolean result = true;
        try {
            _workspace.getReadAccess();
            _processChangeRequests();

            // Pre-initialize actors that have been added.
            if (_actorsToInitialize.size() > 0) {
                Iterator actors = _actorsToInitialize.iterator();
                while (actors.hasNext()) {
                    Actor actor = (Actor)actors.next();
                    actor.preinitialize();
                    // NOTE: To see why this is needed, see the comment
                    // above for the call to validateSettables().
                    if (actor instanceof NamedObj) {
                        ((NamedObj)actor).validateSettables();
                    }
                }
            }
            if (!_typesResolved) {
                resolveTypes();
                _typesResolved = true;
            }

            _iterationCount++;
            _setState(ITERATING);

            // Perform domain-specific initialization on the actor.
            if (_actorsToInitialize.size() > 0) {
                Iterator actors = _actorsToInitialize.iterator();
                while (actors.hasNext()) {
                    Actor actor = (Actor)actors.next();
                    actor.getExecutiveDirector().initialize(actor);
                }
                _actorsToInitialize.clear();
            }
            if (_container.prefire()) {
                _container.fire();
                result = _container.postfire();
            }
        } finally {
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
     *  This method is merely calls
     *  {@link #notifyListenersOfThrowable(Throwable)}.
     *  @param exception The exception.
     */
    public void notifyListenersOfException(Exception exception) {
        notifyListenersOfThrowable(exception);
    }

    /** Notify all the execution listeners of a Throwable
     *  If there are no listeners, then print the throwable information
     *  to the standard error stream. This is intended to be used by threads
     *  that are involved in an execution as a mechanism for reporting
     *  errors.  As an example, in a threaded domain, each thread
     *  should catch all exceptions and report them using this method.
     *  @param throwable The throwable
     */
    public synchronized void notifyListenersOfThrowable(Throwable throwable) {
        // We use Throwables instead of Exceptions so that we can catch
        // Errors like java.lang.UnsatisfiedLink.
        String errorMessage = shortDescription(throwable)
            + " occurred: " + throwable.getClass()
            + "(" + throwable.getMessage() + ")";
        _debug("-- Manager notifying listeners of exception: " + throwable);
        if (_executionListeners == null) {
            System.err.println(errorMessage);
            throwable.printStackTrace();
        } else {
            Iterator listeners = _executionListeners.iterator();
            while (listeners.hasNext()) {
                ExecutionListener listener =
                    (ExecutionListener) listeners.next();
                listener.executionError(this, throwable);
            }
        }
    }

    /** Set a flag requesting that execution pause at the next opportunity
     *  (between iterations).  This method calls stopFire() on the
     *  toplevel composite actor to ensure that the manager's execution
     *  thread becomes active again. Actors are expected to react to
     *  stopFire() by returning as soon as possible from their fire()
     *  methods, thus completing an iteration.  For example, in the case
     *  of PN, an iteration only ends if deadlock occurs, which may
     *  never happen.  Calling stopFire() truncates the iteration.
     *  The thread controlling the execution (the one that calls execute())
     *  will be suspended the next time through the iteration loop.
     *  To resume execution, call resume().
     *  @see Executable#stopFire
     */
    public void pause() {
        _pauseRequested = true;
        Nameable container = getContainer();
        if (!(container instanceof CompositeActor)) {
            throw new InternalErrorException(
                    "Attempted to call pause() on an executing manager " +
                    "with no associated CompositeActor model");
        }
        ((CompositeActor)container).stopFire();
    }


    /** The thread that calls this method will wait until resume() has
     *  been called.
     *
     *  Note: This method will block.  Should only be called from the
     *  executing thread (the thread that is executing the model).  Do
     *  not call this method from the same thread that will call
     *  resume().
     *
     *  FIXME: Added by celaine.  Review this.  Works with
     *  DebugController to resume execution after a breakpoint.
     *  FIXME: in PN this could be called multiple times.  make sure
     *  this still works with multiple threads.
     */
    public void pauseOnBreakpoint(String breakpointMessage) {
        try {
            if (_state == ITERATING) {
                // This will deadlock if called from, say, the UI
                // thread, because execute() holds the lock.
                synchronized(this) {
                    if (_state == ITERATING) {
                        // Set the new state to show that execution is paused
                        // on a breakpoint.
                        PAUSED_ON_BREAKPOINT.setDescription(
                                "pausing on breakpoint: "
                                + breakpointMessage
                                + ".  Click Resume to continue.");
                        _setState(PAUSED_ON_BREAKPOINT);

                        _resumeNotifyWaiting = true;

                        // Wait until resume() is called.
                        while (_resumeNotifyWaiting) {
                            wait();
                        }

                        // resume() has been called, so reset the state of the
                        // execution.
                        _setState(ITERATING);
                    } else { //if (_state == ITERATING) {
                        throw new InternalErrorException("State was changed "
                                + "while pauseOnBreakpoint was called.");
                    }
                } //synchronized(this) {
            } else { //if (_state == ITERATING) {
                throw new InternalErrorException("pauseOnBreakpoint occurred "
                        + "while not iterating the model.");
            }
        } catch (InterruptedException error) {
            throw new InternalErrorException("Interrupted while trying to "
                    + "wait for resume() method to be called.");
        }
    }

    /** Preinitialize the model.  This calls the preinitialize()
     *  method of the container, followed by the resolveTypes()
     *  methods.  Set the Manager's state to PREINITIALIZING.  Note
     *  that this method may be invoked without actually running the
     *  method, but the calling code must make sure that the Manager's
     *  state is reset to IDLE.  This method is read synchronized on
     *  the workspace.
     *  @exception KernelException If the model throws it.
     *  @exception IllegalActionException If the model is already running, or
     *   if there is no container.
     */
    public synchronized void preinitializeAndResolveTypes()
            throws KernelException {
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

            _resumeNotifyWaiting = false;

            // NOTE: This is needed because setExpression() on parameters
            // does not necessarily trigger their evaluation. Thus,
            // if one calls setExpression() without calling validate(),
            // then the new value will never be seen.  Note that the
            // MoML parser and Vergil's parameter editor both validate
            // variables.  But if a model is created some other way,
            // for example in a test suite using Tcl or in Java,
            // then the user might not think to call validate(), and
            // it would seem counterintuitive to have to do so.
            // EAL 5/30/02
            _container.validateSettables();

            // Initialize the topology.
            // NOTE: Some actors require that parameters be set prior
            // to preinitialize().  Hence, this occurs after the call
            // above to validateSettables(). This makes sense, since the
            // preinitialize() method may depend on these parameters.
            // E.g., in CT higher-order components, such as
            // ContinuousTransferFunction, during preinitialize(),
            // the inside of the higher-order components is constructed
            // based on the parameter values.
            // EAL 5/31/02.
            _container.preinitialize();

            // Clear the preinitialization analyses.
            _nameToAnalysis.clear();
            _nameToAnalysis = null;

            _processChangeRequests();

            resolveTypes();
            _typesResolved = true;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Remove a listener from the list of listeners that are notified
     *  of execution events.  If the specified listener is not on the list,
     *  do nothing.
     *  @param listener The listener to remove.
     */
    public void removeExecutionListener(ExecutionListener listener) {
        if (listener == null || _executionListeners == null) return;
        _executionListeners.remove(listener);
    }

    /** Queue a change request, or if the model is idle, execute it
     *  immediately.  If the request is queued, then it will be executed
     *  at the next opportunity, between top-level iterations of the model.
     *  Notify any change listeners when the change is executed.
     *  @param change The requested change.
     */
    public void requestChange(ChangeRequest change) {
        // If the model is idle (i.e., initialize() has not yet been
        // invoked), then process the change request right now.
        if (_state == IDLE) {
            change.execute();
        } else {
            // Otherwise, we must be executing, so queue the request
            // to happen later.
            // Create the list of requests if it doesn't already exist
            if (_changeRequests == null) {
                _changeRequests = new LinkedList();
            }
            _changeRequests.add(change);
            // Now call stopFire so we can be sure the model will give us
            // back control.
            _container.stopFire();
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
        // Only initialize once.
        if(_actorsToInitialize.contains(actor)) {
            return;
        }
        // Only initialize containers.  This avoids initializing an
        // actor twice when it is added as part of a container that is
        // being added.
        {
            NamedObj container = (NamedObj)((NamedObj)actor).getContainer();
            while(container != null) {
                if(_actorsToInitialize.contains(container)) {
                    return;
                }
                container = (NamedObj)container.getContainer();
            }
        }
        // OK, then we need to initialize this actor.  However, we
        // don't need to initialize any actors contained by this
        // actor.
        List list = new LinkedList(_actorsToInitialize);
        for(Iterator actors = list.iterator();
            actors.hasNext();) {
            NamedObj otherActor = (NamedObj)actors.next();
            NamedObj otherActorContainer = (NamedObj)otherActor.getContainer();
            while(otherActorContainer != null) {
                // If otherActor is contained by actor, then remove it.
                if(otherActorContainer == actor) {
                    _actorsToInitialize.remove(otherActor);
                    otherActorContainer = null;
                } else {
                    otherActorContainer = (NamedObj)otherActorContainer.getContainer();
                }
            }
        }
        // Lastly, add this actor to the actors to initialize.
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

            TypedCompositeActor.resolveTypes((TypedCompositeActor)_container);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** If the model is paused, resume execution.  This method must
     *  be called from a different thread than that controlling the
     *  execution, since the thread controlling the execution is
     *  suspended.
     */
    public void resume() {
        // Avoid the case when the director is not actually paused causing the
        // swing thread to block.
        if (_state == PAUSED) {
            synchronized(this) {
                if (_state == PAUSED) {
                    _pauseRequested = false;
                    notifyAll();
                } else {
                    throw new InternalErrorException("resume() should be the "
                            + "only method that goes from "
                            + "PAUSED to not paused");
                }
            }
        } else if (_state == PAUSED_ON_BREAKPOINT) {
            synchronized(this) {
                if (_state == PAUSED_ON_BREAKPOINT) {
                    // Notify all threads waiting to know whether resume() has
                    // been called (threads that called waitForResume()).
                    //
                    // Works with DebugController to resume execution after a
                    // breakpoint.
                    if (_resumeNotifyWaiting) {
                        _resumeNotifyWaiting = false;
                        notifyAll();
                    }
                }
            }
        }
    }

    /** Execute the model, catching all exceptions. Use this method to
     *  execute the model within the calling thread, but to not throw
     *  exceptions.  Instead, the exception is handled using the
     *  notifyListenersOfException() method.  Except for its
     *  exception handling, this method has exactly the same behavior
     *  as execute().
     */
    public void run() {
        try {
            execute();
        } catch (Throwable throwable) {
            // If running tried to load in some native code using JNI
            // then we may get an Error here
            notifyListenersOfThrowable(throwable);
        } finally {
            _thread = null;
        }
    }

    /** Return a short description of the throwable.
     *  @param throwable The throwable
     *  @return If the throwable is an Exception, return "Exception",
     *  if it is an Error, return "Error", if it is a Throwable, return
     *  "Throwable".
     */
    public static String shortDescription(Throwable throwable) {
        // FIXME: This code is a duplicate of MessageHandler.shortDescription()
        // but we don't want to import ptolemy.actor.gui.MessageHandler here.
        String throwableType = null;
        if (throwable instanceof Exception) {
            throwableType = "Exception";
        } else if (throwable instanceof Error) {
            throwableType = "Error";
        } else {
            throwableType = "Throwable";
        }
        return throwableType;
    }

    /** Start an execution in another thread and return.  Any exceptions
     *  that occur during the execution of the model are handled by
     *  the notifyListenersOfException() method.
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
        _thread = new PtolemyThread(this) {
            public void run() {
                // The run() method will set _thread to null
                // upon completion of the run.
                Manager.this.run();
            }
        };
        // Priority set to the minimum to get responsive UI during execution.
        _thread.setPriority(Thread.MIN_PRIORITY);
        _thread.start();
    }

    /** If the state is not IDLE, set a flag to request that
     *  execution stop and exit gracefully and immediately.  The
     *  result of this is non-deterministic and determining the
     *  exact state of the model after this method is called is difficult.
     *  However, it is guaranteed that the model will be be in a
     *  state where it can be executed again.  Because of this, it
     *  is not generally useful to call this method from within an actor.
     *  This method will result in stop() being called on the top level
     *  CompositeActor, although not necessarily immediately.
     */
    public void stop() {
        // Set this regardless of whether the model is running to
        // avoid race conditions.  The model may not have gotten around
        // to starting when finish is requested.
        _finishRequested = true;
        if (_state == IDLE) return;

        Nameable container = getContainer();
        if (!(container instanceof CompositeActor)) {
            throw new InternalErrorException(
                    "Attempted to call step() on an executing manager " +
                    "with no associated CompositeActor model");
        }
        ((CompositeActor)container).stop();

        // Since Manager.resume() is synchronized, start a thread
        // to call resume() in order to avoid deadlock
        Thread resumeThread = new PtolemyThread( new Runnable() {
                public void run() {
                    resume();
                }
            });
        resumeThread.start();
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
     *  execute as soon as possible.
     *  @deprecated
     */
    public void terminate() {
        // If the execution was started in a separate thread, kill that thread.
        // NOTE: This uses the stop() method, which is now deprecated in Java.
        // Indeed it should be, since it terminates a thread
        // nondeterministically, and can leave any objects that the thread
        // operating on in an inconsistent state.
        if (_thread != null) {
            // NOTE:  stop() in java.lang.Thread has been deprecated
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

    /** Return a string with the elapsed time since startTime, and
     *  the amount of memory used.
     */
    public static String timeAndMemory(long startTime) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory()/1024;
        long freeMemory = runtime.freeMemory()/1024;
        return timeAndMemory(startTime, totalMemory, freeMemory);
    }

    /** Return a string with the elapsed time since startTime,
     *  and the amount of memory used.
     */
    public static String timeAndMemory(long startTime,
            long totalMemory, long freeMemory) {
        return System.currentTimeMillis() - startTime
            + " ms. Memory: "
            + totalMemory + "K Free: " + freeMemory + "K ("
            + Math.round( (((double)freeMemory)/((double)totalMemory))
                    * 100.0)
            + "%)";
    }
    
    /** If there is an active thread created by startRun(), then wait
     *  for it to complete and return. The wait is accomplished by
     *  calling the join() method on the thread.  If there is no
     *  active thread, then wait until the manager state is idle
     *  by calling wait().
     *  @see #startRun()
     */
    public void waitForCompletion() {
        if (_thread != null) {
            try {
                _thread.join();
            } catch (InterruptedException ex) {
                // Ignore this and return.
            }
        } else {
            // FIXME: This will almost certainly deadlock if called
            // from a thread apart from the executing thread because execute()
            // is synchronized, so the running thread holds the lock!
            synchronized(this) {
                while (getState() != IDLE && getState() != CORRUPTED) {
                    try {
                        workspace().wait(this);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        }
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

        // Process all change requests. If the model reaches this wrap up
        // state due to the occurrence of an exception during execution,
        // some change requests may be pending. If these requests
        // are not processed, they will be left to the next execution.
        // Also, wrapping up execution may cause change requests to be queued.
        _processChangeRequests();

        _workspace.incrVersion();
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
    protected void _makeManagerOf(CompositeActor compositeActor) {
        if (compositeActor != null) {
            _workspace.remove(this);
        }
        _container = compositeActor;
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

    /** Notify listeners that execution has completed successfully.
     */
    protected synchronized void _notifyListenersOfCompletion() {
        if (_debugging) {
            _debug("-- Manager completed execution with "
                    + _iterationCount
                    + " iterations");
        }
        if (_executionListeners != null) {
            Iterator listeners = _executionListeners.iterator();
            while (listeners.hasNext()) {
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
            _debug("-- Manager state is now: " + _state.getDescription());
        }
        if (_executionListeners != null) {
            Iterator listeners = _executionListeners.iterator();
            while (listeners.hasNext()) {
                ExecutionListener listener =
                    (ExecutionListener) listeners.next();
                listener.managerStateChanged(this);
            }
        }
    }

    /** Process the queued change requests that have been added with
     *  requestChange(). If any queued request itself makes requests
     *  using requestChange(), then those requests are processed in
     *  the same way after the first batch is completed.  If any
     *  request fails with an exception, then the change list is cleared,
     *  and no further requests are processed.
     *  Note that change requests processed successfully
     *  prior to the failed request are not undone.
     */
    protected void _processChangeRequests() {
        _debug("-- Manager checking for change requests");
        while (_changeRequests != null) {
            try {
                // Get write access once on the outside, to make
                // getting write access on each individual
                // modification faster.
                _workspace.getWriteAccess();
                
                _setState(MUTATING);
                
                // Clone the change request list before iterating through it
                // in case any of the changes themselves post change requests.
                LinkedList clonedList = new LinkedList(_changeRequests);
                
                // Clear the request queue.  We want to discard the queue even
                // if the changes fail.
                // Otherwise, we could get stuck not being able to do anything
                // further with the model.
                _changeRequests = null;
                
                Iterator changeRequests = clonedList.iterator();
                while (changeRequests.hasNext()) {
                    ChangeRequest request = 
                        (ChangeRequest)changeRequests.next();
                    request.execute();
                    if (_debugging) {
                        _debug("-- Manager executed change request "
                                + "with description: "
                                + request.getDescription());
                    }
                }
            } finally {
                _workspace.doneWriting();
            }   
        }
    }

    /** Set the state of execution and notify listeners if the state
     *  actually changes.
     *  @param newState The new state.
     */
    protected synchronized void _setState(State newState) {
        if (_state != newState) {
            _state = newState;
            _notifyListenersOfStateChange();
            notifyAll();
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

    // The map that keeps track of analyses.
    private HashMap _nameToAnalysis;

    // Flag indicating that pause() has been called.
    private boolean _pauseRequested = false;

    // Flag for waiting on resume();
    private boolean _resumeNotifyWaiting = false;

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
    public static class State {

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

        //  An utter hack...
        private void setDescription(String description) {
            _description = description;
        }

        /** Print out the current state.
         */
        public String toString() {
            return "The model is " + getDescription();
        }

        private String _description;
    }
}
