/* A Manager governs the execution of a model.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

 execute catches Exceptions and rethrows them as runtime exceptions. why?
 requestInitialization is pickier about what actors are initialized.
 added preinitialization analyses (i.e. constVariableModelAnalysis)
 Look over exitAfterWrapup().
 */
package ptolemy.actor;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ExceptionHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.PtolemyThread;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
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

 @author Steve Neuendorffer, Lukito Muliadi, Edward A. Lee, Elaine Cheong, Contributor: Mudit Goel, John S. Davis II, Bert Rodiers, Daniel Crawl
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class Manager extends NamedObj implements Runnable {

    /** Construct a manager in the default workspace with an empty string
     *  as its name. The manager is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public Manager() {
        super();
        _registerShutdownHook();
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
        _registerShutdownHook();
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
        _registerShutdownHook();
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

    /** Indicator that width inference is being done.
     */
    public final static State INFERING_WIDTHS = new State("infering widths");

    /** Indicator that the execution is in the initialize phase.
     */
    public final static State INITIALIZING = new State("initializing");

    /** Indicator that the execution is in an iteration.
     */
    public final static State ITERATING = new State("executing");

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

    /** Indicator that the execution is throwing a throwable.
     */
    public final static State THROWING_A_THROWABLE = new State(
            "throwing a throwable");

    /** Indicator that the execution is in the wrapup phase.
     */
    public final static State WRAPPING_UP = new State("wrapping up");

    /** Indicator that the execution is in the wrapup phase and about
     *  to exit.
     */
    public final static State EXITING = new State("exiting");

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a static analysis to this manager. A static analysis is
     *  simply an object that is recorded in a hash table and cleared
     *  at the end of preinitialize(), with no semantics associated
     *  with that object.  The intent is for that object to serve as a
     *  repository for static analysis results, but clearing it and
     *  the end of preinitialize isn't quite right. The idea is that
     *  it is cleared at a point when the analysis has to be redone
     *  after that point. But doing this at the end of preinitialize
     *  means this won't work with models that mutate either during
     *  preinitialize() (as in higher- order actors) or during
     *  execution.
     *
     *  @param name The name of the analysis.
     *  @param analysis The analysis to record.
     *  @see #getAnalysis(String)
     */
    public void addAnalysis(String name, Object analysis) {
        if (_nameToAnalysis == null) {
            _nameToAnalysis = new HashMap<String, Object>();
        }

        _nameToAnalysis.put(name, analysis);
    }

    /** Add a listener to be notified when the model execution changes state.
     *  If the specified listener is already a listener, do nothing.
     *  @param listener The listener.
     *  @see #removeExecutionListener(ExecutionListener)
     */
    public void addExecutionListener(ExecutionListener listener) {
        if (listener == null) {
            return;
        }

        if (_executionListeners == null) {
            _executionListeners = new LinkedList<WeakReference<ExecutionListener>>();
        }

        // Do not add the same listener twice, so if it is already in the list,
        // remove it. See NamedObj.addChangeListener(). -- tfeng
        removeExecutionListener(listener);

        _executionListeners.add(new WeakReference<ExecutionListener>(listener));
    }

    /** Enable or disable printing time and memory usage at the end of model
     *  execution.
     *
     *  @param enabled Whether time and memory printing is enabled.
     */
    public void enablePrintTimeAndMemory(boolean enabled) {
        _printTimeAndMemory = enabled;
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
     *  wrapup() method is called (in a finally clause).
     *  <p>
     *  If an exception occurs during the execution, delegate to the
     *  exception handlers (if there are any) to handle these exceptions.
     *  If there are no exception handlers, it is up to the
     *  caller to handle (e.g. report) the exception.
     *  If you do not wish to handle exceptions, but want to execute
     *  within the calling thread, use run().
     *  @see #run()
     *  @exception KernelException If the model throws it.
     *  @exception IllegalActionException If the model is already running, or
     *   if there is no container.
     */
    public void execute() throws KernelException, IllegalActionException {
        // NOTE: This method used to be synchronized, but holding the
        // lock on the Manager for the duration of an execution creates
        // endless possibilities for deadlock.  So instead, we put
        // a small barrier here and throw an exception if the model
        // is already running.
        synchronized (this) {
            if (_state != IDLE) {
                throw new IllegalActionException(this,
                        "Model is already running.");
            }
        }

        // Make a record of the time execution starts.
        long startTime = new Date().getTime();

        _debug("-- Manager execute() called.");

        // Reset this in case finish() has been called since the last run.
        _finishRequested = false;

        boolean completedSuccessfully = false;

        // If we throw an throwable, we save it until after the
        // finally clauses.
        Throwable initialThrowable = null;

        try {
            try {
                initialize();
                if (System.currentTimeMillis() - startTime > minimumStatisticsTime) {
                    setStatusMessage(timeAndMemory(startTime));
                    System.out.println("Manager.initialize() finished: "
                            + getStatusMessage());
                }

                // Call iterate() until finish() is called or postfire()
                // returns false.
                _debug("-- Manager beginning to iterate.");

                while (!_finishRequested) {
                    if (!iterate()) {
                        break;
                    }

                    if (_pauseRequested) {
                        // Have to synchronize on this to be able to wait
                        // during the pause.
                        synchronized (this) {
                            _setState(PAUSED);
                            try {
                                // Enable processing of change requests so that editing is live.
                                setDeferringChangeRequests(false);
                                while (_pauseRequested && !_finishRequested) {
                                    try {
                                        wait();
                                    } catch (InterruptedException e) {
                                        // ignore.
                                    }
                                }
                            } finally {
                                setDeferringChangeRequests(true);
                            }
                        }
                    }
                }

                completedSuccessfully = true;
            } catch (Throwable throwable) {
                // Catch anything that can be throw (Error, Exception etc.)
                // Catching just KernelException will not work, since
                // we want to be sure to rethrow things like LinkErrors.
                // We use THROWING_A_THROWABLE in NonStrictTest.wrapup()
                // so we be sure the actor fires and reads enough inputs.
                _setState(THROWING_A_THROWABLE);
                initialThrowable = throwable;
            }
        } finally {
            try {
                wrapup();
            } catch (Exception exception) {
                // Caught an exception. Discard this exception
                // if there is an exception thrown during the iteration.
                // Otherwise, save the exception to be handled later.
                if (initialThrowable == null) {
                    initialThrowable = exception;
                }
            } finally {
                // Indicate that it is now safe to execute
                // change requests when they are requested.
                setDeferringChangeRequests(false);

                // NOTE: This used to increment the workspace
                // version, which would require that everything
                // be re-done on subsequent runs. EAL 9/16/06
                // _workspace.incrVersion();

                // Reset this for the next run.
                _finishRequested = false;

                // Wrapup may also throw an exception,
                // So be sure to reset the state to idle!
                if (_state != IDLE) {
                    _setState(IDLE);
                }

                // Wrapup may throw an exception, so put the following
                // statement inside the finally block.
                if (_printTimeAndMemory) {
                    setStatusMessage(timeAndMemory(startTime));
                    System.out.println(getStatusMessage());
                }

                if (completedSuccessfully && initialThrowable == null) {
                    // The Exit.tcl test needs this because otherwise
                    // we throw an exception in wrapup(), but
                    // notifyListenersOfSuccessfulCompletion() was
                    // called which causes MoMLSimpleApplication to
                    // exit before MoMLSimpleApplication was notified
                    // of the exception.

                    _notifyListenersOfSuccessfulCompletion();
                }

                // Handle throwable with exception handlers,
                // if there are any.
                if (initialThrowable != null) {
                    if (_container == null) {
                        throw new InternalErrorException(this,
                                initialThrowable,
                                "The container of the manager was null. "
                                        + "Try calling composite.setManager().");
                    }
                    // Look for attributes and entities that implement ExceptionHandler.
                    List<ExceptionHandler> exceptionHandlersList = _container
                            .attributeList(ExceptionHandler.class);
                    exceptionHandlersList.addAll(_container
                            .entityList(ExceptionHandler.class));
                    boolean exceptionHandled = false;
                    for (ExceptionHandler exceptionHandler : exceptionHandlersList) {
                        // Note that we allow multiple exception handlers
                        // to handle the same exception. So we iterate all
                        // of the exception handlers, at least until one
                        // those throws an exception.
                        if (exceptionHandler.handleException(_container,
                                initialThrowable)) {
                            exceptionHandled = true;
                        }
                    }
                    if (exceptionHandled) {
                        initialThrowable = null;
                        _notifyListenersOfSuccessfulCompletion();
                    }
                }

                // If the throwable has not been handled by exception handlers,
                // throw it.
                if (initialThrowable != null) {
                    if (initialThrowable instanceof RuntimeException) {
                        // We do not handle runtime exception.
                        // Throw it.
                        throw (RuntimeException) initialThrowable;
                    } else if (initialThrowable instanceof KernelException) {
                        // Since this class is declared to throw
                        // KernelException, if we have one, we throw it.
                        throw (KernelException) initialThrowable;
                    } else if (initialThrowable instanceof RuntimeException) {
                        // We do not handle runtime exception.
                        // Throw it.
                        throw (RuntimeException) initialThrowable;
                    } else {
                        // Ok, it is not a KernelException, so we
                        // rethrow it as the cause of a KernelException
                        throw new IllegalActionException(this,
                                initialThrowable, null);
                    }
                }
            }
        }
        /*
        System.out.println("Manager.execute() finished: "
                + timeAndMemory(startTime));
         */
    }

    /** Cause the system to exit after wrapup().
     *  If the ptolemy.ptII.exitAfterWrapup property is <b>not</b>
     *  set, then when wrapup() is almost finished, we call System.exit().
     *  If the ptolemy.ptII.exitAfterWrapup property is set, then
     *  we throw an Exception.
     */
    public void exitAfterWrapup() {
        _exitAfterWrapup = true;
        _setState(EXITING);
    }

    /** If the state is not IDLE, set a flag to request that execution
     *  stop and exit in a completely deterministic fashion at the end
     *  of the next toplevel iteration.  This method may be called
     *  from within an actor to stop the execution of the model.  This
     *  will result in stop() being called on the top level
     *  CompositeActor, although not necessarily immediately.  Note
     *  that this method is allowed to be called if the model is
     *  paused and actors that implement the stop() method must
     *  properly respond to that method even if the model is paused.
     *  @see Executable#stop
     */
    public void finish() {
        // Set this regardless of whether the model is running to
        // avoid race conditions.  The model may not have gotten around
        // to starting when finish is requested.
        _finishRequested = true;

        if (_debugging) {
            _debug("finish() has been called.");
        }

        if (_state == IDLE) {
            return;
        }

        Nameable container = getContainer();

        if (!(container instanceof CompositeActor)) {
            throw new InternalErrorException(
                    "Attempted to call finish() on an executing manager "
                            + "with no associated CompositeActor model");
        }

        // Used to just call stopFire() here, but this does not set
        // the flag in the director so that isStopRequested() returns
        // true. We have to set that flag or the actor threads in
        // threaded domains will not know that a stop has been requested
        // (vs. a pause).
        ((CompositeActor) container).stop();

        Thread unpauser = new UnpauserThread();
        unpauser.start();
    }

    /** Get the analysis with the given name, or return null if no such
     *  analysis exists.
     *  @param name The name of the analysis.
     *  @return the analysis with the given name, or null.
     *  @see #addAnalysis(String, Object)
     */
    public Object getAnalysis(String name) {
        if (_nameToAnalysis == null) {
            return null;
        } else {
            return _nameToAnalysis.get(name);
        }
    }

    /** Return the top-level composite actor for which this manager
     *  controls execution.
     *  @return The composite actor that this manager is responsible for.
     */
    @Override
    public NamedObj getContainer() {
        return _container;
    }

    /** Return the iteration count, which is the number of iterations
     *  that have been started (but not necessarily completed).
     *  @return The number of iterations started.
     */
    public int getIterationCount() {
        return _iterationCount;
    }

    /** Return the workspace version on the last completion of
     *  preinitializeAndResolveTypes(). This can be used to optimize
     *  away actions on repeated runs when the workspace hasn't changed
     *  between runs.
     *  @return the workspace version on the last completion of
     *  {@link #preinitializeAndResolveTypes()}.
     */
    public long getPreinitializeVersion() {
        return _preinitializeVersion;
    }

    /** Return the current state of execution of the manager.
     *  @return The state of execution.
     */
    public State getState() {
        return _state;
    }

    /** Return any messages, such as the amount of time consumed.
     *  This method is called to update the status bar, so the
     *  output should be short.
     *  @return the message
     *  @see #setStatusMessage(String)
     */
    public String getStatusMessage() {
        return _statusMessage;
    }

    /** Get the execution identifier object for a throwable.
     *
     * <p> An execution identifier is an object given to the Manager
     * via setExecutionIdentifier(), and cleared during preinitialize().
     * If the Manager notifies a listener of a model error,
     * getExecutionIdentifier() can be used to map that error back to
     * the executionIdentifier object.
     *
     * @param throwable The throwable.
     * @return The execution identifier object if one was set, otherwise null.
     * @see #setExecutionIdentifier(Object)
     */
    public Object getExecutionIdentifier(Throwable throwable) {
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4230

        // "In SDF provenance records to the error table an exception
        // thrown after the workflow stops. In PN, multiple exceptions
        // may be thrown, and this happens during execution. Right now
        // there's a problem with these errors getting
        // recorded. Related/in addition: there's also difficulty
        // knowing which execution errors are correlated with, since
        // the same workflow may be executing and throwing errors in
        // multiple windows. As Dan notes: Manager reports exceptions
        // asynchronously; need a way to tie an exception to
        // corresponding execution."

        // "The issue is that the Manager notifies listeners of model
        // errors asynchronously to avoid deadlocks.  However, this
        // makes it impossible for a listener to associate a specific
        // execution cycle with a given error."
        return _throwableToExecutionIdentifier.get(throwable);
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
    public synchronized void initialize() throws KernelException,
            IllegalActionException {
        try {
            _workspace.getReadAccess();

            // Make sure that change requests are not executed when requested,
            // but rather only executed when executeChangeRequests() is called.
            setDeferringChangeRequests(true);

            long startTime = new Date().getTime();
            preinitializeAndResolveTypes();
            if (System.currentTimeMillis() - startTime > minimumStatisticsTime) {
                setStatusMessage(timeAndMemory(startTime));
                System.out.println("preinitialize() finished: "
                        + getStatusMessage());
            }

            _setState(INITIALIZING);
            _container.initialize();

            // Since we have just initialized all actors, clear the
            // list of actors pending initialization.
            _actorsToInitialize.clear();

            executeChangeRequests();
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

    /** Return true if exitAfterWrapup() was called.
     *  @return true if exitAfterWrapup was called.
     */
    public boolean isExitingAfterWrapup() {
        return _exitAfterWrapup;
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
     *  @return True if postfire() is not called, otherwise, return
     *  the value returned by postfire().
     *
     *  @exception KernelException If the model throws it, or if there
     *   is no container.
     */
    public boolean iterate() throws KernelException {
        if (_container == null) {
            throw new IllegalActionException(this, "No model to execute!");
        }

        boolean result = true;

        long startTime = new Date().getTime();
        // Execute the change requests before acquiring read access on the
        // workspace. The reason for this is that the change requests are safe
        // anyway, since they acquire write access on the workspace.
        // But while copying the change request list in the NamedObj
        // executeChangeRequest method, this thread will try to acquire a lock
        // on a local NamedObj object called _changeLock.
        // If it blocks, it will hold a read lock on the workspace.
        // Meanwhile, the thread that holds the lock on _changeLock
        // may attempt to acquire write permission on the workspace (WHY???).
        executeChangeRequests();
        try {
            _workspace.getReadAccess();

            // We should infer the widths before preinitializing the container, since the latter
            // will create the receivers for which it needs the widths of the relations.
            if (IORelation._USE_NEW_WIDTH_INFERENCE_ALGO) {
                _inferWidths();
            }

            // Pre-initialize actors that have been added.
            for (Actor actor : _actorsToInitialize) {
                // Do not attempt to preinitialize transparent composite actors.
                // Note that the cast is safe, as everything in Ptolemy that
                // is an actor is also a ComponentEntity.
                if (((ComponentEntity) actor).isOpaque()) {
                    actor.getExecutiveDirector().preinitialize(actor);
                }

                // NOTE: To see why this is no longer needed, see the comment
                // above for the commented out call to validateSettables().
                /*
                 if (actor instanceof NamedObj) {
                 ((NamedObj) actor).validateSettables();
                 }
                 */
            }
            if (System.currentTimeMillis() - startTime > minimumStatisticsTime) {
                setStatusMessage(timeAndMemory(startTime));
                System.out
                        .println("Manager.iterate(): preinitialize() finished: "
                                + getStatusMessage());
            }

            if (!_typesResolved) {
                resolveTypes();
                _typesResolved = true;
            }

            _iterationCount++;
            _setState(ITERATING);
            // Perform domain-specific initialization on the actor.
            for (Actor actor : _actorsToInitialize) {
                actor.getExecutiveDirector().initialize(actor);
            }

            _actorsToInitialize.clear();

            // Note that if prefire() returns false, then postfire()
            // is *not* called and this method will return true, which
            // indicate to a caller of this method that iterate()
            // could be called again. This is because if prefire
            // returns false, it means "I don't want to be fired now."
            // If postfire returns false, it means "I don't want to
            // ever be fired again."
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

    /** Notify all the execution listeners of a Throwable.
     *  If there are no listeners, then print the throwable information
     *  to the standard error stream. This is intended to be used by threads
     *  that are involved in an execution as a mechanism for reporting
     *  errors.  As an example, in a threaded domain, each thread
     *  should catch all exceptions and report them using this method.
     *  This method defers the actual reporting to a new thread
     *  because it requires obtaining a lock on this manager, and that
     *  could cause deadlock.
     *  @param throwable The throwable
     */
    public void notifyListenersOfThrowable(final Throwable throwable) {
        Thread thread = new ErrorReportingThread("Error reporting thread",
                throwable);
        thread.start();
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
                    "Attempted to call pause() on an executing manager "
                            + "with no associated CompositeActor model");
        }

        ((CompositeActor) container).stopFire();
    }

    /** The thread that calls this method will wait until resume() has
     *  been called.
     *
     *  <p>Note: This method will block.  It should only be called
     *  from the executing thread (the thread that is executing the
     *  model).  Do not call this method from the same thread that
     *  will call resume().
     *  @param breakpointMessage The message to print when paused on a
     *  breakpoint.
     */
    public void pauseOnBreakpoint(String breakpointMessage) {
        //  FIXME: Added by celaine.  Review this.  Works with
        //  DebugController to resume execution after a breakpoint.
        //  FIXME: in PN this could be called multiple times.  make sure
        //  this still works with multiple threads.
        try {
            if (_state == ITERATING) {
                // This will deadlock if called from, say, the UI
                // thread, because execute() holds the lock.
                synchronized (this) {
                    if (_state == ITERATING) {
                        // Set the new state to show that execution is paused
                        // on a breakpoint.
                        PAUSED_ON_BREAKPOINT
                                .setDescription("pausing on breakpoint: "
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
                throw new IllegalActionException(this, "No model to run!");
            }

            _setState(PREINITIALIZING);

            _exitAfterWrapup = false;
            _pauseRequested = false;
            _typesResolved = false;
            _iterationCount = 0;
            _executionIdentifier = null;

            _resumeNotifyWaiting = false;

            // NOTE: Used to call validateSettables() here with the following
            // note.  However, this call is very expensive and means that
            // second runs are no faster than first runs.
            // NOTE: This is needed because setExpression() on parameters
            // does not necessarily trigger their evaluation. Thus,
            // if one calls setExpression() without calling validate(),
            // then the new value will never be seen.  Note that the
            // MoML parser and Vergil's parameter editor both validate
            // variables.  But if a model is created some other way,
            // for example in a test suite using Tcl or in Java,
            // then the user might not think to call validate(), and
            // it would seem counterintuitive to have to do so.
            // _container.validateSettables();

            // Clear the preinitialization analyses... Ensure that
            // We get current analysis.
            if (_nameToAnalysis != null) {
                _nameToAnalysis.clear();
                _nameToAnalysis = null;
            }

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
            //_container.createReceivers(); // Undid this change temporarily since the move of createReceivers breaks HDF
            //_container.createSchedule(); // Undid this change temporarily since the move of createReceivers breaks HDF
            executeChangeRequests();

            // Infer widths (if not already done)
            // NOTE: This has most likely already been done, since it is done
            // when receivers are created, which occurs earlier in preinitialize().
            if (IORelation._USE_NEW_WIDTH_INFERENCE_ALGO) {
                _inferWidths();
            }

            resolveTypes();
            _typesResolved = true;
            _preinitializeVersion = _workspace.getVersion();
        } finally {
            // Clear the preinitialization analyses.
            if (_nameToAnalysis != null) {
                _nameToAnalysis.clear();
                _nameToAnalysis = null;
            }

            _workspace.doneReading();
        }
    }

    /** If the workspace version has changed since the last invocation
     *  of preinitializeAndResolveTypes(), then invoke it now and set
     *  the state of the Manager to IDLE upon completion. This can be used
     *  during editing a model to check types, to expand higher-order
     *  components, or to establish connections between Publisher and
     *  Subscriber actors.
     *  @exception KernelException If thrown while preinitializing
     *  and resolving types.
     */
    public void preinitializeIfNecessary() throws KernelException {
        try {
            if (_preinitializeVersion != _workspace.getVersion()) {
                preinitializeAndResolveTypes();
            }
        } finally {
            _setState(IDLE);
        }
    }

    /** Invoke preinitialize() and wrapup().
     *
     *  <p>If the model has not been preinitialized since the last
     *  change to its structure, that must be done now for the result
     *  to be accurate. This is because higher-order components
     *  and Publisher and Subscriber connections may not have yet
     *   been created.</p>
     *
     *  @param actor The actor upon which to call preinitialize() on
     *  its toplevel.
     *  @exception KernelException If thrown when a Manager is added to
     *  the top level or if preinitialize() fails.
     */
    public static void preinitializeThenWrapup(Actor actor)
            throws KernelException {
        // If preinitialize() has not yet been called, then we don't
        // yet know what Subscribers are using connected to the
        // Publisher.
        Manager manager = actor.getManager();
        if (manager == null) {
            CompositeActor toplevel = (CompositeActor) ((NamedObj) actor)
                    .toplevel();
            manager = new Manager(toplevel.workspace(), "PubManager");
            toplevel.setManager(manager);
        }
        try {
            // Create connections between Publishers and Subscribers.
            manager.preinitializeIfNecessary();
        } finally {
            try {
                // FIXME: should this be synchronized on this?
                if (manager.getState() != IDLE
                        && manager.getState() != WRAPPING_UP) {
                    manager.wrapup();
                }
            } catch (Throwable throwable) {
                // The Exit actor causes Manager.wrapup() to throw this.
                if (!manager.isExitingAfterWrapup()) {
                    throw new IllegalActionException(actor, throwable,
                            "Manager.wrapup() failed.");
                }
            }
        }
    }

    /** Remove a listener from the list of listeners that are notified
     *  of execution events.  If the specified listener is not on the list,
     *  do nothing.
     *  @param listener The listener to remove.
     *  @see #addExecutionListener(ExecutionListener)
     */
    public void removeExecutionListener(ExecutionListener listener) {
        if (listener == null || _executionListeners == null) {
            return;
        }
        List<WeakReference<ExecutionListener>> toRemove = new LinkedList<WeakReference<ExecutionListener>>();
        for (WeakReference<ExecutionListener> reference : _executionListeners) {
            if (reference.get() == listener) {
                toRemove.add(reference);
            }
        }
        _executionListeners.removeAll(toRemove);
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
        if (_actorsToInitialize.contains(actor)) {
            return;
        }
        // Only initialize containers.  This avoids initializing an
        // actor twice when it is added as part of a container that is
        // being added.
        {
            NamedObj container = actor.getContainer();

            while (container != null) {
                if (_actorsToInitialize.contains(container)) {
                    return;
                }

                container = container.getContainer();
            }
        }

        // OK, then we need to initialize this actor.  However, we
        // don't need to initialize any actors contained by this
        // actor.
    	if (actor instanceof CompositeEntity) {
    		List<Actor> list = new LinkedList<Actor>(_actorsToInitialize);
    		for (Actor otherActor : list) {
    			if (((CompositeEntity)actor).deepContains((NamedObj)otherActor)) {
        			_actorsToInitialize.remove(otherActor);    				
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
        if (!(_container instanceof TypedCompositeActor)) {
            return;
        }

        try {
            _workspace.getReadAccess();
            _setState(RESOLVING_TYPES);

            TypedCompositeActor.resolveTypes((TypedCompositeActor) _container);
        } finally {
            _workspace.doneReading();
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
            synchronized (this) {
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
            synchronized (this) {
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
    @Override
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

    /** Set the execution identifier object. Any throwables that occur
     * in current execution cycle are associated with this
     * identifier. The execution identifier is reset in
     * preinitialize().
     *
     * @param executionIdentifier The execution identifier object
     * @see #getExecutionIdentifier(Throwable)
     */
    public void setExecutionIdentifier(Object executionIdentifier) {
        _executionIdentifier = executionIdentifier;
    }

    /** Set the status message, such as the amount of time consumed.
     *  This method is called to update the status bar, so the
     *  output should be short.
     *  @param message The message to be displayed.
     *  @see #getStatusMessage()
     */
    public void setStatusMessage(String message) {
        _statusMessage = message;
    }

    /** Return a short description of the throwable.
     *  @param throwable The throwable
     *  @return If the throwable is an Exception, return "Exception",
     *  if it is an Error, return "Error", if it is a Throwable, return
     *  "Throwable".
     *  @deprecated Instead ptolemy.util.MessageHandler.shortDescription()
     */
    @Deprecated
    public static String shortDescription(Throwable throwable) {
        return MessageHandler.shortDescription(throwable);
    }

    /** Start an execution in another thread and return.  Any exceptions
     *  that occur during the execution of the model are handled by
     *  the notifyListenersOfException() method.
     *  @exception IllegalActionException If the model is already running,
     *  e.g. the state is not IDLE.
     */
    public void startRun() throws IllegalActionException {
        if (_state != IDLE) {
            throw new IllegalActionException(this, "Model is "
                    + _state.getDescription());
        }

        // Set this within the calling thread to avoid race conditions
        // where finish() might be called before the spawned thread
        // actually starts up.
        _finishRequested = false;
        _thread = new PtolemyRunThread(this, _container.getName());
        // Priority set to the minimum to get responsive UI during execution.
        _thread.setPriority(Thread.MIN_PRIORITY);
        _thread.start();
    }

    /** If the state is not IDLE, set a flag to request that
     *  execution stop and exit in a completely deterministic fashion
     *  at the end of the next toplevel iteration.  This method may
     *  be called from within an actor to stop the execution of the model.
     *  This will result in stop() being called on the top level
     *  CompositeActor, although not necessarily immediately.
     *  This is basically an alias for finish().
     *
     *  <p> Note that in general, it is best to get that director
     *  and call finish() and stopFire() on the director than
     *  it is to call Manager.stop().</p>
     *
     *  <p> The reason that it is better to call Director.finish()
     *  is that if Manager.stop() is called inside a RunCompositeActor
     *  then only the inside execution will be stopped, not
     *  the outside one.  It is also not correct to call stop()
     *  on the directory because Director.stop() requests immediate
     *  stopping.  To give determinate stopping, call Director.finish()
     *  so that the current iteration is completed.</p>
     *
     *  <p>In multithreaded domains, Director.stopFire is called
     *  to request that all actors conclude ongoing firings.</p>
     */
    public void stop() {
        finish();
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
    @Deprecated
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
            } catch (InterruptedException e) {
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
     *  @param startTime The start time in milliseconds.  For example,
     *  the value returned by <code>(new Date()).getTime()</code>.
     *  @return A string with the elapsed time since startTime, and
     *  the amount of memory used.
     */
    public static String timeAndMemory(long startTime) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024;
        long freeMemory = runtime.freeMemory() / 1024;
        return timeAndMemory(startTime, totalMemory, freeMemory);
    }

    /** Return a string with the elapsed time since startTime,
     *  and the amount of memory used.
     *  the value returned by <code>(new Date()).getTime()</code>.
     *  @param startTime The start time in milliseconds.  For example,
     *  the value returned by <code>(new Date()).getTime()</code>.
     *  @param totalMemory The total amount of memory used in kilobytes.
     *  @param freeMemory The total amount of memory free in kilobytes.
     *  @return A string with the elapsed time since startTime, and
     *  the amount of memory used.
     */
    public static String timeAndMemory(long startTime, long totalMemory,
            long freeMemory) {
        return System.currentTimeMillis()
                - startTime
                + " ms. Memory: "
                + totalMemory
                + "K Free: "
                + freeMemory
                + "K ("
                + Math.round((double) freeMemory / (double) totalMemory * 100.0)
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
            // NOTE: Previously, execute() was a synchronized method.
            // Result: This would almost certainly deadlock if called
            // from a thread apart from the executing thread because execute()
            // is synchronized, so the running thread holds the lock!
            // NOTE: Synchronizing here is not correct.
            // See Workspace.wait(Object)
            // synchronized (this) {
            while (getState() != IDLE && getState() != CORRUPTED) {
                try {
                    workspace().wait(this);
                } catch (InterruptedException ex) {
                    break;
                }
            }
            // }
        }
    }

    /** Wrap up the model by invoking the wrapup method of the toplevel
     *  composite actor.  The state of the manager will be set to
     *  WRAPPING_UP.
     *  @exception KernelException If the model throws it.
     *  @exception IllegalActionException If the model is idle or already
     *   wrapping up, or if there is no container.
     */
    public void wrapup() throws KernelException, IllegalActionException {
        // NOTE: This method used to be synchronized, but we cannot
        // hold the lock on the director during wrapup because it
        // will cause deadlock. Instead, we use a small barrier
        // here to check and set the state.
        synchronized (this) {
            if (_state == IDLE || _state == WRAPPING_UP) {
                throw new IllegalActionException(this,
                        "Cannot wrap up. The current state is: "
                                + _state.getDescription());
            }

            if (_container == null) {
                throw new IllegalActionException(this, "No model to run!");
            }

            _setState(WRAPPING_UP);
        }

        // Wrap up the topology
        _container.wrapup();

        // Process all change requests. If the model reaches this wrap up
        // state due to the occurrence of an exception during execution,
        // some change requests may be pending. If these requests
        // are not processed, they will be left to the next execution.
        // Also, wrapping up execution may cause change requests to be queued.
        // Also, at the same time, re-enable immediate execution of
        // change requests.
        setDeferringChangeRequests(false);

        // NOTE: This used to increment the workspace
        // version, which would require that everything
        // be re-done on subsequent runs. EAL 9/16/06
        // _workspace.incrVersion();

        if (_exitAfterWrapup) {
            // If the ptolemy.ptII.exitAfterWrapup property is set,
            // then we don't actually exit.
            StringUtilities.exit(0);
        }

        // Wrapup completed successfully
        _setState(IDLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The minimum amount of time that may elapse during certain
     *  operations before statistics are generated.  The Manager and
     *  other classes in Ptolemy will print out statistics if the
     *  amount of time (in milliseconds) named by this variable
     *  elapses during certain steps such as preinitialize().
     *  The initial default value is 10000, meaning that certain
     *  operations can take up to 10 seconds before statistics
     *  are printed.
     *  @see ptolemy.kernel.CompositeEntity#statistics(String)
     */
    public static int minimumStatisticsTime = 10000;

    /** The amount of time to allow for the model to terminate
     *  gracefully before shutting it down when the JVM is shut down
     *  due to control-C, user logging out, etc.
     */
    public static long SHUTDOWN_TIME = 30000L;

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
     *  @param compositeActor The composite actor that this manager will
     *  manage.
     */
    protected void _makeManagerOf(CompositeActor compositeActor) {
        if (compositeActor != null) {
            _workspace.remove(this);
        }

        _container = compositeActor;
    }

    /** Notify listeners that execution has completed.
     *  @deprecated Incomplete name, use
     *  {@link #_notifyListenersOfSuccessfulCompletion()}
     */
    @Deprecated
    protected synchronized void _notifyListenersOfCompletion() {
        _notifyListenersOfSuccessfulCompletion();
    }

    /** Notify listeners that execution has completed successfully.
     */
    protected synchronized void _notifyListenersOfSuccessfulCompletion() {
        if (_debugging) {
            _debug("-- Manager completed execution with " + _iterationCount
                    + " iterations");
        }

        if (_executionListeners != null) {
            ListIterator<WeakReference<ExecutionListener>> listeners = _executionListeners
                    .listIterator();

            while (listeners.hasNext()) {
                WeakReference<ExecutionListener> reference = listeners.next();
                ExecutionListener listener = reference.get();

                if (listener != null) {
                    listener.executionFinished(this);
                } else {
                    listeners.remove();
                }
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
            ListIterator<WeakReference<ExecutionListener>> listeners = _executionListeners
                    .listIterator();

            while (listeners.hasNext()) {
                WeakReference<ExecutionListener> reference = listeners.next();
                ExecutionListener listener = reference.get();

                if (listener != null) {
                    listener.managerStateChanged(this);
                } else {
                    listeners.remove();
                }
            }
        }
    }

    /** Register a shutdown hook to gracefully stop the execution of a model
     *  if the JVM is shut down (by control-C, the user logging out, etc.).
     */
    protected void _registerShutdownHook() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        if (_state != IDLE) {
                            System.out.println("# Manager._registerShutdownHook(): State is " + _state
                                    + ", which is not IDLE.  Waiting for model to stop.");
                        }
                        finish();
                        if (_thread != null && _thread.isAlive()) {
                            try {
                                // This seems dangerous. Could prevent the process
                                // from dying. We use a timeout here of 30 seconds.
                                _thread.join(SHUTDOWN_TIME);
                            } catch (InterruptedException e) {
                                // Failed to stop the thread.
                                e.printStackTrace();
                            }
                        }
                    }
                });
        } catch (java.security.AccessControlException ex) {
            // This exception gets triggered by
            // http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII10.0/ptII10.0.devel/ptolemy/vergil/Vergil.htm
            System.out.println("Warning: failed to add a shutdown hook."
                    + "(Running a model in an applet always causes this)");
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
    ////                         private methods                   ////

    /**
     *  Infer the width of the relations for which no width has been
     *  specified yet.
     *  This method will set the state to INFERING_WIDTHS and change
     *  it back afterwards.
     *  @exception IllegalActionException If the widths of the relations at
     *          port are not consistent or if the width cannot be inferred
     *          for a relation.
     */
    private void _inferWidths() throws IllegalActionException {
        if (_container.needsWidthInference()) {
            State previousState = _state;
            try {
                _setState(INFERING_WIDTHS);
                _container.inferWidths();
            } finally {
                _setState(previousState);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A list of actors with pending initialization.
    private List<Actor> _actorsToInitialize = new LinkedList<Actor>();

    // The top-level CompositeActor that contains this Manager
    private CompositeActor _container = null;

    /** An execution identifier.  See
     * #getExecutionIdentifier(Throwable throwable)
     */
    private Object _executionIdentifier;

    // Listeners for execution events. This list has weak references.
    private List<WeakReference<ExecutionListener>> _executionListeners;

    // Set to true if we should exit after wrapup().
    private boolean _exitAfterWrapup = false;

    // Flag indicating that finish() has been called.
    private boolean _finishRequested = false;

    // Count the number of iterations completed.
    private int _iterationCount;

    // The map that keeps track of analyses.
    private HashMap<String, Object> _nameToAnalysis;

    // Flag indicating that pause() has been called.
    private boolean _pauseRequested = false;

    // Version at which preinitialize last successfully executed.
    private long _preinitializeVersion = -1;

    // Whether time and memory usage are printed at the end of model execution.
    private boolean _printTimeAndMemory = true;

    // Flag for waiting on resume();
    private boolean _resumeNotifyWaiting = false;

    // The state of the execution.
    private volatile State _state = IDLE;

    // A short status message.
    private String _statusMessage = "";

    // If startRun() is used, then this points to the thread that was
    // created.
    private PtolemyThread _thread;

    // A map that associates throwables with execution identifiers. The map is
    // a WeakHashMap so that execution identifiers can be garbage collected.
    private Map<Throwable, Object> _throwableToExecutionIdentifier = new WeakHashMap<Throwable, Object>();

    // An indicator of whether type resolution needs to be done.
    private boolean _typesResolved = false;

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
        @Override
        public String toString() {
            return "The model is " + getDescription();
        }

        private String _description;
    }

    /** Notify listeners of an exception.
     */
    private class ErrorReportingThread extends Thread {
        // This thread is a named inner class so that we can find it
        // by name when looking for memory leaks.

        /** Construct an thread that reports errors.
         *  @param name The name of the thread.
         */
        public ErrorReportingThread(String name, Throwable throwable) {
            super(name);
            _throwable = throwable;
        }

        /** Run the thread.
         */
        @Override
        public void run() {
            synchronized (Manager.this) {
                // We use Throwables instead of Exceptions so that
                // we can catch Errors like
                // java.lang.UnsatisfiedLink.
                String errorMessage = MessageHandler
                        .shortDescription(_throwable)
                        + " occurred: "
                        + _throwable.getClass()
                        + "("
                        + _throwable.getMessage()
                        + ")";
                _debug("-- Manager notifying listeners of exception: "
                        + _throwable);

                if (_executionListeners == null) {
                    System.err
                            .println("No executionListeners? Error message was: "
                                    + errorMessage);
                    _throwable.printStackTrace();
                } else {

                    // If the execution id is not null, map the
                    // throwable to the id.
                    if (_executionIdentifier != null) {
                        _throwableToExecutionIdentifier.put(_throwable,
                                _executionIdentifier);
                    }

                    ListIterator<WeakReference<ExecutionListener>> listeners = _executionListeners
                            .listIterator();

                    while (listeners.hasNext()) {
                        WeakReference<ExecutionListener> reference = listeners
                                .next();
                        ExecutionListener listener = reference.get();

                        if (listener != null) {
                            listener.executionError(Manager.this, _throwable);
                        } else {
                            listeners.remove();
                        }
                    }
                }
            }
        }

        /** The throwable that is to be passed to the listeners. */
        private Throwable _throwable;
    }

    private class PtolemyRunThread extends PtolemyThread {
        // This thread is a named inner class so that we can find it
        // by name when looking for memory leaks.

        public PtolemyRunThread(Runnable target, String name) {
            super(target, name);
        }

        @Override
        public void run() {
            // The run() method will set _thread to null
            // upon completion of the run.
            Manager.this.run();
        }
    }

    private class UnpauserThread extends Thread {
        // This thread is a named inner class so that we can find it
        // by name when looking for memory leaks.

        @Override
        public void run() {
            // NOTE: The execute() method used to be synchronized,
            // which would cause deadlock with this.
            // During wrapup, if the Manager is locked
            // and the ProcessDirector is waiting for threads
            // to end, then these unpausers
            // can't run, and the processes can't end.
            synchronized (Manager.this) {
                Manager.this.notifyAll();
            }
        }
    }
}
