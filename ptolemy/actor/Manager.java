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
*/

package ptolemy.actor;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.mutation.*;
import ptolemy.data.*;

import collections.LinkedList;
import collections.HashedSet;
import java.util.Enumeration;
import java.lang.reflect.*;


//////////////////////////////////////////////////////////////////////////
//// Manager
/**
A Manager is a domain-independant object that manages the execution of 
a model.   It provides several methods to control execution with: go() 
blockingGo, pause, resume, terminate, and finish. 
Most often, methods in this object will be called by a 
graphical user interface.  However, it is possible to manually call 
these methods from a java object, a java applet, or an interactive 
prompt, such as TclBlend.   
Because user interaction will likely be occuring asynchronously to the 
execution of the model, it is important that all the processing for the 
model occur in a separate thread.   The Manager is responsible for creating 
and managing the java thread in which execution begins, although some 
domains may spawn additional threads of their own.  
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
@version: $Id$
*/

public final class Manager extends NamedObj {

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

    /** Start a sequence of execution.   This method is a blocking version of
     *  go(), intended for use with test scripts and batch mode runs.   Note
     *  that this method is not synchronized because it would grab the whole
     *  object until it finishes.   if you call this method, then just about
     *  the only way to terminate execution is by having postfire return false.
     *  In actuality,
     *  go is implemented by starting a ManagerExecutionThread that calls
     *  back to this method.  
     *  It begins by calling initialize on the toplevel composite actor.   
     *  It then continually calls iterate based on
     *  the variables _isRunning, and _isPaused.  It finally 
     *  calls wrapup on its container to clean up after the execution.
     */
    public void blockingGo() {
        CompositeActor toplevel = ((CompositeActor)getToplevel());
               
        // ensure that we only have one execution running.
        synchronized(this) {
            if(_isRunning) return;
            _isRunning = true;
            _isPaused = false;
            _iteration = 0;
	    _typeResolved = false;
        }

        // Notify all the listeners that execution has started.
        ExecutionEvent event = new ExecutionEvent(this);
        Enumeration listeners = _ExecutionListeners.elements();
        while(listeners.hasMoreElements()) {
            ExecutionListener l = 
                (ExecutionListener) listeners.nextElement();
            l.executionStarted(event);
        }

        try {
            try {
                // Initialize the topology
                toplevel.initialize();
                
                // Figure out the appropriate write access.
                _needWriteAccessDuringIteration = _checkIfWriteAccessNeededDuringIteration();

                // Call _iterate() until:
                // _isRunning is set to false (presumably by stop())
                // postfire() returns false.
                while (_isRunning && _iterate()) {
                    
                    try {
                        // if a pause has been requested
                        if(_isPaused) {
                            // Notify listeners that we are paused.
                            event = 
                                new ExecutionEvent(this,_iteration);
                            listeners = 
                                _ExecutionListeners.elements();
                            while(listeners.hasMoreElements()) {
                                ExecutionListener l = 
                                    (ExecutionListener) 
                                    listeners.nextElement();
                                l.executionPaused(event);
                            }

                            // suspend this thread until somebody wakes us up.
                            _runningthread.wait();

                            // Somebody woke us up, so notify all the 
                            // listeners that we are resuming.
                            event = 
                                new ExecutionEvent(this,_iteration);
                            listeners = 
                                _ExecutionListeners.elements();
                            while(listeners.hasMoreElements()) {
                                ExecutionListener l = 
                                    (ExecutionListener) 
                                    listeners.nextElement();
                                l.executionResumed(event);
                            }
                        }
                    }
                    catch (InterruptedException e) {
                        // We don't care if we were interrupted..
                        // Just ignore.
                    }
                    
                } // while (_isRunning && _iterate())
            }
            finally {
                // if we are done, then always be sure to reset the flags.
                _isRunning = false;
                _isPaused = false;
                
                // try to wrapup the topology.
                toplevel.wrapup();

                // notify all listeners that we have been stopped.
                event = 
                    new ExecutionEvent(this,_iteration);
                listeners = 
                    _ExecutionListeners.elements();
                while(listeners.hasMoreElements()) {
                    ExecutionListener l = 
                        (ExecutionListener) 
                        listeners.nextElement();
                    l.executionFinished(event);
                }
            }
        }
        catch (Exception e) {
            fireExecutionError(e);
        }
    }
     
    /** Set a flag to request that the thread in which execution is running 
     *  complete by calling wrapup() and then terminating.   
     *  This thread is synchronized so that it runs atomically with respect to 
     *  the other methods in manager that control the ManagerExecutionThread.
     *  This method is non-blocking.   
     */
    public synchronized void finish() {
        _isRunning = false;
        _isPaused = false;
        if(_runningthread != null)
            _runningthread.notify();
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
            ExecutionEvent event = new ExecutionEvent(this,_iteration,e);
            Enumeration listeners = _ExecutionListeners.elements();
            // if nobody is listening, then just dump the stack trace.
            if(!listeners.hasMoreElements()) {
                e.printStackTrace();
            }
            while(listeners.hasMoreElements()) {
                ExecutionListener l = 
                    (ExecutionListener) listeners.nextElement();
                l.executionError(event);
            }
     }


    /** Return the toplevel composite actor for which this manager
     *  controls execution.   This composite actor does not have a parent, and 
     *  contains the entire hierarchy for an execution.
     *  @return The CompositeActor that this Manager is responsible for.
     */
    public CompositeActor getToplevel() {
        return _toplevel;
    }
    
    /** Start an execution that will run for an unspecified number of 
     *  toplevel iterations.   This will normally be stopped by 
     *  calling finish, terminate, or returning false in a postfire method. 
     *  This method is non-blocking.
     */
    public synchronized void go() {
        _startExecution();
    }

    /** If an execution is currently running, then set a flag requesting that
     *  execution pause at the next available opportunity between toplevel 
     *  iterations.   When the pause flag is detected, the 
     *  ManagerExecutionThread will suspend itself and issue the 
     *  ExecutionPaused ExecutionEvent to all ExecutionListeners.
     *  This thread is synchronized so that it runs atomically with respect to 
     *  the other methods in manager that control the ManagerExecutionThread.
     *  This call is non-blocking.
     */
    public synchronized void pause() {
        if(_isRunning) _isPaused = true;
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
     *  turning off the paused flag and waking the ManagerExecutionThread up.
     *  This thread is synchronized so that it runs atomically with respect to 
     *  the other methods in manager that control the ManagerExecutionThread.
     */
    public synchronized void resume() {
        if(_isRunning && _isPaused) {
            _isPaused = false;
            if(_runningthread != null)
                _runningthread.notify();
        }
    }

    /** Terminate any currently executing simulation with extreme prejudice.  
     *  Kill the main execution thread and call terminate on the toplevel 
     *  container.   This should cause any actors to free up any resources 
     *  they have allocated and Directors should kill any threads they have 
     *  created.   However, a consistant state is not guaraunteed.   The 
     *  topology should probably be recreated before attemping any
     *  further operations.   This is not synchronized because we want it to
     *  happen as soon as possible, no matter what.
     */
    public void terminate() {

        // kill the main thread and wait for it to die.
        if(_runningthread != null) { 
            _runningthread.stop();
            try {
                _runningthread.join();
            }
            catch (InterruptedException e) {
                // This will usually get thrown, since we are
                // forcibly terminating
                // the thread.   We just ignore it.
            }
            _runningthread = null;
        }
        // Terminate the entire hierarchy as best we can.
        CompositeActor toplevel = ((CompositeActor)getToplevel());
        toplevel.terminate();

        // notify all execution listeners that execution was terminated.
        ExecutionEvent event = new ExecutionEvent(this);
        Enumeration listeners = _ExecutionListeners.elements();
        while(listeners.hasMoreElements()) {
            ExecutionListener l = (ExecutionListener) listeners.nextElement();
            l.executionTerminated(event);
        }
        _isRunning = false;
        _isPaused = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private classes                   ////

    private class ManagerExecutionThread extends PtolemyThread {
        
        /** Construct a thread that will call back to the Manager and 
         *  Request a certain number of iterations of execution.
         *
         *  @param m The manager that created the ManagerExecutionThread
         *  @param iterations The number of iterations to execute for     
         */
        public ManagerExecutionThread(Manager m) {
            super();
            _manager = m;
        }
        
        /** This thread makes a single call back to the blockingGo 
         *  method of the Manager that it was created with.
         */
        public void run() {
            _manager.blockingGo();
        }
        
        private Manager _manager;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Invoke one iteration.  An iteration consists of
     *  invocations of prefire(), fire(), and postfire(), in that
     *  order.  Prefire() will be called multiple times until it returns true. 
     *  If prefire() return false, then fire() and postfire() are not
     *  invoked.   Fire() will be called a single time.   If postfire()
     *  returns false, then the execution should be terminated.
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
        
        ExecutionEvent event = new ExecutionEvent(this,_iteration);
        Enumeration listeners = _ExecutionListeners.elements();
        while(listeners.hasMoreElements()) {
            ExecutionListener l = 
                (ExecutionListener) listeners.nextElement();
            l.executionIterationStarted(event);
        }
        
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
                event = new ExecutionEvent(this,_iteration, e);
                listeners = _ExecutionListeners.elements();
                while(listeners.hasMoreElements()) {
                    ExecutionListener l = 
                        (ExecutionListener) listeners.nextElement();
                    l.executionError(event);
                }
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

    /** Make this Manager the Manager of the specified composite
     *  actor.  This method should not be called directly.  Instead, call
     *  setManager of the CompositeActor class (or a derived class).
     *  If the argument is not the toplevel CompositeActor, then we throw
     *  an InvalidStateException.
     */
    protected void _makeManagerOf (CompositeActor ca) {
        if (ca != null) {
            if(ca.getContainer() != null) 
                throw new InvalidStateException("Manager's container must " +
                        "be the toplevel CompositeActor!");
            workspace().remove(this);
        }
        _toplevel = ca;
    }
    
    /** Check if write access in the workspace will be needed during an
     *  iteration.
     *  An iteration is defined to be one invocation of prefire(), fire(), and
     *  postfire() methods of the top level composite actor. 
     *  <p>
     *  This method recursively call the needWriteAccess() method of all lower
     *  level directors. Intuitively, the workspace will only be made write-
     *  protected, if all the directors permit it.
     */
    // FIXME: What's the appropriate protection level for this method ?
    // FIXME: public ? private ?
    protected boolean _checkIfWriteAccessNeededDuringIteration() {
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

    /** This method serves at the implementation for both go() and go(int).
     *  Starts a new ManagerExecutionThread which will call
     *  back to this object in 'blockingGo'.
     */
    private void _startExecution() {
         if(_isRunning) return;
         
         // If the previous run hasn't totally finished yet, then be sure 
         // it is good and dead before continuing.
         if(_runningthread!=null) {
             _runningthread.stop();
             try {
                 _runningthread.join();
             }
             catch (InterruptedException e) {
                 // Well, if we bothered to kill it, then this should 
                 // always get thrown, so just ignore it.
             }
             _runningthread = null;
         }
         _runningthread = new ManagerExecutionThread(this);
         _runningthread.start();

     }
         
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The toplevel CompositeActor that contains this Manager
    private CompositeActor _toplevel = null;

    private boolean _isRunning;
    private boolean _isPaused;
    private int _iteration;
    private boolean _needWriteAccessDuringIteration;
    private Thread _runningthread;
    private HashedSet _ExecutionListeners;

    // FIXME: a hack until mutation got implemented.
    private boolean _typeResolved = false;
}




