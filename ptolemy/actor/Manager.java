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

@author Steve Neuendorffer
// Contributors: Mudit Goel, Edward A. Lee, Lukito Muliadi
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
     */
    public void blockingGo() {
        blockingGo(-1);
    }

    /** This method is a blocking version of the go() method.  In actuality,
     *  go is implemented by starting a ManagerExecutionThread that calls
     *  back to this method.   This is made public for use with test scripts
     *  and other batch mode processing.
     *  It begins by calling initialize on its container, which is the
     *  toplevel composite actor.   It then continually calls iterate based on
     *  the variables _isRunning, _isPaused, and _iterations.  It finally 
     *  calls wrapup on its container to clean up after the execution.
     *  
     *  @param iterations The number of toplevel iterations to run for.   
     *  If iterations = -1, then execution will have no preset limit on the
     *  number of iterations.
     */
    public void blockingGo(int iterations) {
        CompositeActor container = ((CompositeActor)getContainer());
               
        // ensure that we only have one execution running.
        synchronized(this) {
            if(_isRunning) return;
            _isRunning = true;
            _isPaused = false;
            _iteration = 0;
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
                container.initialize();
                
                // Call _iterate() until:
                // _isRunning is set to false (presumably by stop())
                // iteration limit is reached
                // postfire() returns false.
                while (_isRunning && 
                        ((iterations < 0) || (_iteration++ < iterations)) 
                        && _iterate())

                    try {
                        // if a pause has been requested
                        if(_isPaused) {
                            // Notify listeners that we are paused.
                            event = 
                                new ExecutionEvent(this,iterations);
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
                                new ExecutionEvent(this,iterations);
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
            }
            finally {
                // if we are done, then always be sure to reset the flags.
                _isRunning = false;
                _isPaused = false;
                
                // try to wrapup the topology.
                container.wrapup();

                // notify all listeners that we have been stopped.
                event = 
                    new ExecutionEvent(this,iterations);
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
            // if any exceptions get up to this level, then we have to tell
            // the gui by enscapsulating in an event.
            event = new ExecutionEvent(this,iterations,e);
            listeners = _ExecutionListeners.elements();
            while(listeners.hasMoreElements()) {
                ExecutionListener l = 
                    (ExecutionListener) listeners.nextElement();
                l.executionError(event);
            }
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


    /** Return the container, which is the composite actor for which this
     *  is the Manager.   This composite actor does not have a parent, and 
     *  contains the entire hierarchy for an execution.
     *  @return The CompositeActor that this Manager is responsible for.
     */
       public Nameable getContainer() {
        return _container;
    }
    
    
    /** Start an execution that will run for an unspecified number of 
     *  iterations.   This will normally be terminated by calling finish, 
     *  terminate, or returning false in a postfire method.   This method
     *  is equivalent to calling go(-1).   This method is non-blocking.
     */
    public synchronized void go() {
        _startExecution(-1);
    }

    /** Start an execution that will run for no more than a specified
     *  number of iterations.   The execution may be interrupted 
     *  by calling finish, terminate, or returning false in a postfire method.
     *  This method is non-blocking.   This method is synchronized to 
     *  prevent interaction with simultaneous calls to finish, terminate, 
     *  pause, and resume.
     *  @param iterations The number of toplevel iterations to execute for.
     *  If iterations = -1, then execution will have no preset limit on the
     *  number of iterations.
     */
    public synchronized void go(int iterations) {
        _startExecution(iterations);
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
            CompositeActor container = (CompositeActor)getContainer();
            if ( !(container instanceof TypedCompositeActor)) {
                return;
            }
            Enumeration constraints =
                ((TypedCompositeActor)container).typeConstraints();

            InequalitySolver solver = new InequalitySolver(TypeCPO.cpo());
	    while (constraints.hasMoreElements()) {
                Object ineq = constraints.nextElement();
                solver.addInequality((Inequality)ineq);
	    }

            // find the greatest solution (most general types)
            boolean resolved = solver.solveLeast();
            if ( !resolved) {
		Enumeration unsatisfied = solver.unsatisfiedInequalities();
		// exception only contains info. on first unsatisfied ineq.
		if (unsatisfied.hasMoreElements()) {
		    Inequality ineq = (Inequality)unsatisfied.nextElement();
		    TypeTerm term = (TypeTerm)ineq.getLesserTerm();
		    TypedIOPort arg1 = term.getPort();
		    term = (TypeTerm)ineq.getGreaterTerm();
		    TypedIOPort arg2 = term.getPort();
                    throw new TypeConflictException(arg1, arg2,
					"cannot satisfy constraint.");
		}
            }

	    // check if any resolved type is NaT
	    Enumeration nats = solver.bottomVariables();
	    if (nats.hasMoreElements()) {
		TypeTerm term = (TypeTerm)nats.nextElement();
		TypedIOPort port = term.getPort();
		throw new TypeConflictException(port, "port resolved to NaT.");
	    }

	    // check if any resolved to abstract class, but is not CPO top.
	    Class top = (Class)TypeCPO.cpo().top();
	    Enumeration abs = solver.variables();
	    while (abs.hasMoreElements()) {
		TypeTerm term = (TypeTerm)abs.nextElement();
		TypedIOPort port = term.getPort();
		Class type = port.getResolvedType();
		if (type.isInterface()) {
		    throw new TypeConflictException(port, "port resolved " +
			"to an abstract type.");
		}
		int mod = type.getModifiers();
		if (Modifier.isAbstract(mod) && !type.equals(top)) {
		    throw new TypeConflictException(port, "port resolved " +
			"to an abstract class other than the hierarchy top.");
		}
	    }
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
        CompositeActor container = ((CompositeActor)getContainer());
        container.terminate();

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

    private class ManagerExecutionThread extends Thread {
        
        /** Construct a thread that will call back to the Manager and 
         *  Request a certain number of iterations of execution.
         *
         *  @param m The manager that created the ManagerExecutionThread
         *  @param iterations The number of iterations to execute for     
         */
        public ManagerExecutionThread(Manager m, int iterations) {
            super();
            _manager = m;
            _iterations = iterations;
        }
        
        /** This thread makes a single call back to the blockingGo 
         *  method of the Manager that it was created with.
         */
        public void run() {
            _manager.blockingGo(_iterations);
        }
        
        private Manager _manager;
        private int _iterations;
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
        CompositeActor container = (CompositeActor)getContainer();
        if (container == null) {
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
                resolveTypes();
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
                
            if (container.prefire()) {
                container.fire();
                return container.postfire();
            }
            return false;
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
        _container = ca;
        if (ca != null) {
            if(ca.getContainer() != null) 
                throw new InvalidStateException("Manager's container must " +
                        "be the toplevel CompositeActor!");
            workspace().remove(this);
        }
    }

    /** This method serves at the implementation for both go() and go(int).
     *  Starts a new ManagerExecutionThread which will call
     *  back to this object in 'blockingGo'.
     */
    private void _startExecution(int iterations) {
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
         _runningthread = new ManagerExecutionThread(this,iterations);
         _runningthread.start();

     }
         
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The toplevel CompositeActor that contains this Manager
    private CompositeActor _container = null;

    private boolean _isRunning;
    private boolean _isPaused;
    private int _iteration;
    private Thread _runningthread;
    private HashedSet _ExecutionListeners;

}


