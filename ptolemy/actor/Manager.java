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
a model.   Most often, methods in this object will be called by a 
graphical user interface.  However, it is possible to manually call 
these methods from a java object, a java applet, or an interactive 
prompt, such as TclBlend.   
Because user interaction will likely be occuring asynchronously to the 
execution of the model, it is important that all the processing for the 
model occur in a separate thread.   The Manager is responsible for creating 
and managing the java thread in which execution begins, although some 
domains may spawn additional threads of their own.  

@author Steve Neuendorffer, Mudit Goel, Edward A. Lee, Lukito Muliadi
@version: $Id$
*/
public class Manager extends NamedObj {

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
     *  @param name Name of this object.
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
     *  @param name Name of this director.
     */
    public Manager(Workspace workspace, String name) {
        super(workspace, name);
        _ExecutionListeners = new HashedSet();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the container, which is the composite actor for which this
     *  is the Manager.   This composite actor does not have a parent, and 
     *  contains the entire hierarchy for an execution.
     *  @return The CompositeActor that this Manager is responsible for.
     */
    public Nameable getContainer() {
        return _container;
    }


    /** Start an execution that will run for an unspecified number of 
     *  iterations.   This will normally be terminated by calling wrapup, 
     *  terminate, or returning false in a postfire method.
     */
    public void go() {
        go(-1);
    }

    /** Start an execution that will run for no more than a specified
     *  number of iterations.   The execution may be terminated early 
     *  by calling abort, finish, or returning false in a postfire method.
     *  This method starts a new ManagerExecutionThread which will call
     *  back to this object in 'blockingGo'.
     */
     public synchronized void go(int iterations) {

         if(_isRunning) return;
         
         _iterations = iterations;
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
         
    /** Invoke one iteration.  In this base class, one iteration consists of
     *  exactly one invocation of prefire(), fire(), and postfire(), in that
     *  order. If prefire() return false, then fire() and postfire() are not
     *  invoked. In derived classes, there may be more than one invocation of
     *  fire(). This method is read-synchronized on the workspace.
     *  @return True if postfire() returns true.
     *  @exception IllegalActionException If any of the called methods
     *   throws it.
     */
    public boolean iterate(int iteration) throws IllegalActionException {
        CompositeActor actor = (CompositeActor)getContainer();

         ExecutionEvent event = new ExecutionEvent(this,iteration);
         Enumeration listeners = _ExecutionListeners.elements();
         while(listeners.hasMoreElements()) {
             ExecutionListener l = 
                 (ExecutionListener) listeners.nextElement();
             l.executionIterationStarted(event);
         }

        // if mutations
        try {
            workspace().getWriteAccess();
            //While mutations remain
            //process mutations
            //initialize new actors
            //create receivers?
        } 
        finally {
            workspace().doneWriting();
        }
        
        try {
            workspace().getReadAccess();
            // Check types.
	    // resolveTypes();

            if (actor.prefire()) {
                actor.fire();
                return actor.postfire();
            }
            return false;
        } finally {
            workspace().doneReading();
        }
    }

    /** If not paused, then pause the simulation.
     */
    public synchronized void pause() {
        if(_isRunning) _isPaused = true;
    }
    
    public void registerExecutionListener(ExecutionListener el) {
        _ExecutionListeners.include((Object) el);
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

    /** If paused, resume the currently paused simulation.
     */
    public synchronized void resume() {
        if(_isRunning && _isPaused) {
            _isPaused = false;
            if(_runningthread != null)
                _runningthread.notify();
        }
    }
 
    public void blockingGo() {
        blockingGo(-1);
    }

    /** This is the method that is called to actually perform the execution.
     *  It begins by calling initialize on its container, which is the
     *  toplevel composite actor.   It then continually calls iterate based on
     *  the variables _isRunning, _isPaused, and _iterations.  It finally 
     *  calls wrapup on its container to clean up after the execution.
     *  This method may be called directly to provide 'batch mode' execution,
     *  or it may be called by ManagerExecutionThread.
     */
    public void blockingGo(int iterations) {
        CompositeActor container = ((CompositeActor)getContainer());
        int count = 0;

        // ensure that we only have one execution running.
        synchronized(this) {
            if(_isRunning) return;
            _isRunning = true;
            _isPaused = false;
        }

        ExecutionEvent event = new ExecutionEvent(this);
        Enumeration listeners = _ExecutionListeners.elements();
        while(listeners.hasMoreElements()) {
            ExecutionListener l = 
                (ExecutionListener) listeners.nextElement();
            l.executionStarted(event);
        }

        try {
            try {
                container.initialize();
                
                while (_isRunning && 
                        ((iterations < 0) || (count++ < iterations)) 
                        && iterate(iterations))

                    try {
                        if(_isPaused) {
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

                            _runningthread.wait();

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
                _isRunning = false;
                _isPaused = false;

                container.wrapup();
                event = 
                    new ExecutionEvent(this,iterations);
                listeners = 
                    _ExecutionListeners.elements();
                while(listeners.hasMoreElements()) {
                    ExecutionListener l = 
                        (ExecutionListener) 
                        listeners.nextElement();
                    l.executionWrappedup(event);
                }
            }
        }
        catch (IllegalActionException e) {
            event = new ExecutionEvent(this,iterations,e);
            listeners = _ExecutionListeners.elements();
            while(listeners.hasMoreElements()) {
                ExecutionListener l = 
                    (ExecutionListener) listeners.nextElement();
                l.executionError(event);
            }
        }
    }

    /** Terminate any currently executing simulation with extreme prejudice.  
     *  Kill the main execution thread and call terminate on the toplevel 
     *  container.   This should cause any actors to free up any resources 
     *  they have allocated and directors should kill any threads they have 
     *  created.   However, a consistant state is not guaraunteed.   The 
     *  environment should probably be restarted before attemping any
     *  further operations.
     */
    public void terminate() {
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
        CompositeActor container = ((CompositeActor)getContainer());
        container.terminate();
        ExecutionEvent event = new ExecutionEvent(this);
        Enumeration listeners = _ExecutionListeners.elements();
        while(listeners.hasMoreElements()) {
            ExecutionListener l = (ExecutionListener) listeners.nextElement();
            l.executionTerminated(event);
        }
        _isRunning = false;
        _isPaused = false;
    }
      
    /** Stop any currently executing simulation and cleanup nicely.
     */
    public synchronized void wrapup() {
        _isRunning = false;
        _isPaused = false;
        if(_runningthread != null)
            _runningthread.notify();
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make this director the Manager of the specified composite
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _isRunning;
    private boolean _isPaused;
    private int _iterations;
    private Thread _runningthread;
    private CompositeActor _container = null;
    private HashedSet _ExecutionListeners;

}


