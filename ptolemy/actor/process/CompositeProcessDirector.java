/* Base class of directors for the process oriented domains.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow

*/

package ptolemy.actor.process;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.data.*;

import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CompositeProcessDirector
/**
Base class of directors for the process oriented domains. It provides
default implementations for methods that are common across such domains.
<p>
In the process oriented domains, the director controlling a model
needs to keep track of the state of the model. In particular it needs
to maintain an accurate count of the number of active processes under
its control and any processes that are blocked for whatever reason (trying
to read from an empty channel as in PN). 
These counts, and perhaps other counts, are needed by the
director to control and respond when deadlock is detected (no processes
can make progress), or to respond to requests from higher in the hierarchy.
<p>
The methods that control how the director detects and responds to deadlocks
are _areActorsDeadlocked() and _handleDeadlock(). These methods should be
overridden in derived classes to get domain-specific behaviour. The
implementations given here are trivial and suffice only to illustrate
the approach that should be followed.
<p>
@author John S. Davis II
@version $Id$
@see Director
*/
public class CompositeProcessDirector extends ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public CompositeProcessDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public CompositeProcessDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name contains a period,
     *   or if the director is not compatible with the specified container.
     */
    public CompositeProcessDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
        _name = name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active processes is zero.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new ProcessDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        // FIXME
        CompositeProcessDirector newObj = 
	        (CompositeProcessDirector)super.clone(ws);
	newObj._onFirstIteration = _onFirstIteration;
	newObj._inputBranchController = _inputBranchController;
	newObj._outputBranchController = _outputBranchController;
	newObj._blockedRcvrs = _blockedRcvrs;
        return newObj;
    }

    /** Return the input branch controller of this director. If
     *  this method is called prior to the invocation of 
     *  initialize(), then this method will return null.
     *  @return The input branch controller of this director.
     */
    public BranchController getInputController() {
        return _inputBranchController;
    }
    
    /** Return the output branch controller of this director. If
     *  this method is called prior to the invocation of 
     *  initialize(), then this method will return null.
     *  @return The output branch controller of this director.
     */
    public BranchController getOutputController() {
        return _outputBranchController;
    }
    
    /** Invoke the initialize() methods of all the deeply contained
     *  actors in the container (a composite actor) of this director.
     *  These are expected to call initialize(Actor), which will result
     *  in the creation of a new thread for each actor.
     *  Also, set current time to 0.0, or to the current time of
     *  the executive director of the container, if there is one.
     *
     *  @exception IllegalActionException If the initialize() method
     *   of one of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container != null) {
            CompositeActor containersContainer =
                (CompositeActor)container.getContainer();
            if( containersContainer == null ) {
                setCurrentTime(0.0);
            } else {
                double time =
                    containersContainer.getDirector().getCurrentTime();
                setCurrentTime(time);
            }
        }
        
        super.initialize();
       
        _blockedRcvrs = new LinkedList();
        _blockedActorCount = 0;
        
        _inputBranchController = new BranchController(container);
        _outputBranchController = new BranchController(container);

	// Instantiate Input/Output Branch Controllers
	if( container != null ) {
            Iterator inports = container.inputPortList().iterator();
            createBranchController(inports);
	    Iterator outports = container.outputPortList().iterator();
            createBranchController(outports);
	}
        
        _inputControllerIsBlocked = _inputBranchController.isBlocked();
        _outputControllerIsBlocked = _outputBranchController.isBlocked();
        
    }

    /** Return a new receiver of a type compatible with this director.
     *  In class, this returns an instance of MailboxBoundaryReceiver.
     *  @return A new MailboxBoundaryReceiver.
     */
    public Receiver newReceiver() {
        return new MailboxBoundaryReceiver();
    }

    /** Return false if the model has reached a deadlock and can
     *  be terminated if desired. Return true otherwise.
     *  This flag is set on detection of a deadlock in the fire() method.
     *  @return false if the director has detected a deadlock and can be
     *  terminated if desired.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if( _debugging ) _debug(_name+": returning _notDone = " 
                + _notDone);
	return _notDone;
    }

    /** Start threads for all actors that have not had threads started
     *  already (this might include actors initialized since the last
     *  invocation of prefire). This starts the threads, corresponding
     *  to all the actors, that were created in the initialize() method.
     *  @return true Always returns true.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean prefire() throws IllegalActionException  {
        /* FIXME
        System.out.println("PROCESSDIRECTOR.prefire() ending. ");
        */
        
        super.prefire();
        
        Thread thread = null;
        if( _inputBranchController.hasBranches() && _onFirstIteration ) {
            thread = new Thread(_inputBranchController);
            thread.start();
        }
        if( _outputBranchController.hasBranches() && _onFirstIteration ) {
            thread = new Thread(_outputBranchController);
            thread.start();
        }
        _onFirstIteration = false;
        return true;
    }

    /** Request that execution of the current iteration stop. Call
     *  stopThread on each of the process threads that contain actors
     *  controlled by this director and call stopFire on the actors
     *  that are contained by these threads. This method is non-blocking.
     *  After calling this method, the fire() method of this director
     *  is guaranteed to return in finite time.
    public void stopFire() {
        super.stopFire();
    }
     */

    /**
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        // Do nothing
        return true;
    }
    
    /**
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        // Do nothing
        return true;
    }
    
    /**
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public void createBranchController(Iterator ports) 
    	    throws IllegalActionException {
            
        // Create Branches in the BranchController
        IOPort port = null;
        while( ports.hasNext() ) {
            port = (IOPort)ports.next();
            if (!port.isOpaque()) {
                    throw new IllegalActionException(this, port,
                    "port argument is not an opaque" +
                    "input port.");
            }
	    if( port.isInput() ) {
		_inputBranchController.addBranches(port);
	    }
	    if( port.isOutput() ) {
		_outputBranchController.addBranches(port);
	    }
        }
    }

    /** Stop the input branch controller of this director by 
     *  ending the current iteration of the controller. This
     *  method will block until the output branch controller
     *  has stopped due to all of the branches it controls
     *  stopping.
     */
    public void stopInputBranchController() {
        Workspace workspace = workspace();
        if( !_inputBranchController.hasBranches() ) {
            return;
        }
        System.out.println(_name+": controller about to end iteration");
        _inputBranchController.deactivateBranches();
        System.out.println(_name+": controller finished ending iteration");
//         try {
//             throw new Exception();
//         } catch(Exception e) {
//             e.printStackTrace();
//         }
        while( !_inputBranchController.isBlocked() ) {
            System.out.println(_name+": controller about to wait");
            workspace.wait(this);
        }
        System.out.println(_name+": controller ending stopInputBranchController()");
    }
    
    /** Stop the output branch controller of this director by 
     *  ending the current iteration of the controller. This
     *  method will block until the output branch controller
     *  has stopped due to all of the branches it controls
     *  stopping.
     */
    public void stopOutputBranchController() {
        Workspace workspace = workspace();
        if( !_outputBranchController.hasBranches() ) {
            return;
        }
        _outputBranchController.deactivateBranches();
        while( !_outputBranchController.isBlocked() ) {
            workspace.wait(this);
        }
    }
    
    /** Terminates all threads under control of this director immediately.
     *  This abrupt termination will not allow normal cleanup actions
     *  to be performed, and the model should be recreated after calling
     *  this method.
    //  Note: for now call Thread.stop() but should change to use
    //  Thread.destroy() when it is eventually implemented.
    public void terminate() {
        super.terminate();
        // Now stop any threads created by this director.
	LinkedList list = new LinkedList();
	list.addAll(_actorThreadList);
	_actorThreadList.clear();
        Iterator threads = list.iterator();
        while (threads.hasNext()) {
	    ((Thread)threads.next()).stop();
        }
    }
     */

    /** End the execution of the model under the control of this
     *  director. A flag is set in all the receivers that causes
     *  each process to terminate at the earliest communication point.
     *  <P>
     *  Prior to setting receiver flags, this method wakes up the
     *  threads if they all are stopped.
     *  <P>
     *  This method is not synchronized on the workspace, so the caller
     *  should be.
     * @exception IllegalActionException If an error occurs while
     *  accessing the receivers of all actors under the control of
     *  this director.
     */
    public void wrapup() throws IllegalActionException {
        if( _debugging ) _debug(_name+": calling wrapup()");
        
        // Kill all branch controllers
        stopInputBranchController();
        stopOutputBranchController();
        
        if( _debugging ) _debug(_name+": finished deactivating branches");
        
        super.wrapup();
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if the count of active processes in the container is 0.
     *  Otherwise return true. Derived classes must override this method to
     *  return true to any other forms of deadlocks that they might introduce.
     * @return true if there are no active processes in the container.
     */
    protected synchronized boolean _areActorsDeadlocked() {
        if( _getBlockedActorsCount() >= _getActiveActorsCount() ) {
            return true;
        }
	return false;
    }

    /**
     */
    protected synchronized int _getBlockedActorsCount() {
	return _blockedActorCount;
    }

    /** Create a new ProcessThread for controlling the actor that
     *  is passed as a parameter of this method. Subclasses are
     *  encouraged to override this method as necessary for domain
     *  specific functionality.
     * @param actor The actor that the created ProcessThread will
     *  control.
     * @param director The director that manages the model that the
     *  created thread is associated with.
     * @return Return a new ProcessThread that will control the
     *  actor passed as a parameter for this method.
     */
    protected ProcessThread _getProcessThread(Actor actor,
	    ProcessDirector director) throws IllegalActionException {
	return new ProcessThread(actor, director);
    }

    /** Return true.
     *  In derived classes, override this method to obtain domain
     *  specific handling of deadlocks. It should return true if a
     *  real deadlock has occurred and the simulation can be ended.
     *  It should return false if the simulation has data to proceed and
     *  need not be terminated.
     * @return True.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected boolean _handleDeadlock() throws IllegalActionException {
	return true;
    }

    /** Return false.
     *  In derived classes, override this method to obtain domain
     *  specific handling of deadlocks. Return false if a
     *  real deadlock has occurred and the simulation can be ended.
     *  Return true if the simulation can proceed given additional
     *  data and need not be terminated.
     * @return False.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected boolean _resolveDeadlock() throws IllegalActionException {
        System.out.println("Entered CompositeProcessDirector._resolveDeadlock");
	Workspace workspace = workspace();
	if( _areActorsExternallyBlocked() && _areActorsDeadlocked() ) {
	    if( _inputBranchController.isBlocked() ) {
                while( !_outputBranchController.isBlocked() ) {
                    workspace.wait(this);
                }
		stopInputBranchController();
		stopOutputBranchController();
                _registerBlockedRcvrsWithContainer();
		return true;
	    } else if( _outputBranchController.isBlocked() ) {
		stopInputBranchController();
		stopOutputBranchController();
                _registerBlockedRcvrsWithContainer();
		return true;
	    }
	}

	if( !_areActorsExternallyBlocked() && _areActorsDeadlocked() ) {
	    if( _inputBranchController.isBlocked() ) {
                while( !_outputBranchController.isBlocked() ) {
                    workspace.wait(this);
                }
		stopInputBranchController();
		stopOutputBranchController();
		return _resolveInternalDeadlock();
	    } else if( _outputBranchController.isBlocked() ) {
		stopInputBranchController();
		stopOutputBranchController();
		return _resolveInternalDeadlock();
	    } else {
                while( !_outputBranchController.isBlocked() ) {
                    workspace.wait(this);
                }
		stopInputBranchController();
		stopOutputBranchController();
		return _resolveInternalDeadlock();
	    }
	}

	return false;
    }

    /**
     */
    protected boolean _resolveInternalDeadlock() throws IllegalActionException {
	return false;
    }

    /** 
     */
    protected boolean _areActorsExternallyBlocked() {
    	Iterator blockedRcvrIter = _blockedRcvrs.iterator();
        while( blockedRcvrIter.hasNext() ) {
            ProcessReceiver rcvr = (ProcessReceiver)blockedRcvrIter.next();
            if( rcvr.isConnectedToBoundaryInside() ) {
                return true;
            }
        }
        return false;
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorBlocked(ProcessReceiver rcvr) {
        _blockedRcvrs.add(rcvr);
        _blockedActorCount++;
        notifyAll();
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorBlocked(LinkedList rcvrs) {
        if( rcvrs == _blockedRcvrs ) {
            return;
        }
        _blockedRcvrs.addAll(rcvrs);
        _blockedActorCount++;
        notifyAll();
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorUnBlocked(ProcessReceiver rcvr) {
        _blockedRcvrs.remove(rcvr); 
        _blockedActorCount--;
        notifyAll();
        /*
        if( !foundItem ) {
            Thread thread = Thread.currentThread();
            ProcessThread pThread = null;
            if( thread instanceof ProcessThread ) {
            	pThread = (ProcessThread)thread;
                Actor actor = pThread.getActor();
                String name = ((Nameable)actor).getName();
            	System.out.println("ITEM NOT FOUND; Called by " + name);
            } else {
            	System.out.println("ITEM NOT FOUND; Actor name unknown");
            }
        }
        */
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorUnBlocked(LinkedList rcvrs) {
        Iterator rcvrIterator = rcvrs.iterator();
        while( rcvrIterator.hasNext() ) {
            ProcessReceiver rcvr = (ProcessReceiver)rcvrIterator.next();
            _blockedRcvrs.remove(rcvr);
        }
        _blockedActorCount--;
        notifyAll();
    }

    /** 
     */
    protected synchronized void _controllerBlocked(BranchController cntlr) {
        /*
        if( cntlr.isBlocked() ) {
            // Determine which controller
            // Set appropriate flag
            notifyAll();
        }
        */
        
        if( cntlr == _inputBranchController ) {
            _inputControllerIsBlocked = cntlr.isBlocked();
        }
        if( cntlr == _outputBranchController ) {
            _outputControllerIsBlocked = cntlr.isBlocked();
        }
        notifyAll();
    }

    /** 
     */
    protected synchronized void wakeUpDirector() {
    	notifyAll();
    }
    
    /** 
     */
    protected void _controllerUnBlocked(BranchController cntlr) {
        synchronized(_branchCntlrLock) {
            if( cntlr == _inputBranchController ) {
                _inputControllerIsBlocked = cntlr.isBlocked();
            }
            if( cntlr == _outputBranchController ) {
                _outputControllerIsBlocked = cntlr.isBlocked();
            }
	    Thread wakeDirThread = new PtolemyThread( new Runnable() {
                public void run() {
                    System.out.println(_name + ": calling wakeUpDirector()");
                    wakeUpDirector();
                    System.out.println(_name + ": finished calling wakeUpDirector()");
                }
            });
            wakeDirThread.start();
            _debug(_name + ": finished calling wakeDirThread.start()");
        }
    }

    /** JFIXME: This method can lead to deadlock
     */
    protected synchronized boolean _isInputControllerBlocked() {
        return _inputControllerIsBlocked;
    }
        
    /**
     */
    protected void _registerBlockedRcvrsWithContainer() {
    }

    /** 
     */
    public synchronized boolean registerBlockedBranchReceiversWithExecutive() {
        /*
    	Director execDir = ((Actor)getContainer()).getExecutiveDirector();
        if( execDir == this ) {
            return false;
        } else if( !(execDir instanceof ProcessDirector) ) {
            if(_debugging) _debug(_name+": Blocked branch registration didn't work; director not ProcessDirector");
            return false;
        } else if( !_inputBranchController.hasBranches() ) {
            return false;
        }
    
	if( _debugging ) _debug(_name+": registering blocked branches to executive director");
        
        if( _inputBranchController.isBlocked() ) {
            Iterator branches = 
                    _inputBranchController.getEngagedBranchList().iterator();
            Branch branch = null;
            ProcessReceiver rcvr = null;
            LinkedList blockedRcvrs = new LinkedList();
            while ( branches.hasNext() ) {
                branch = (Branch) branches.next();
                rcvr = branch.getProdReceiver();
                if( !rcvr.isReadBlocked() ) {
                    // FIXME: Throw Exception???
                } else {
                    blockedRcvrs.add(rcvr);
                }
            }
            
            if( blockedRcvrs.size() > 0 ) { 
                if(_debugging) _debug(_name+": ProcessDirector blocking due to branches");
                ((ProcessDirector)execDir)._actorBlocked(blockedRcvrs);
                Director dir = ((Actor)getContainer()).getDirector();
                if( dir instanceof ProcessDirector ) {
                    if(_debugging) _debug(_name+": ProcessDirector blocking due to branches");
                    ProcessDirector pDir = (ProcessDirector)dir;
                    pDir._actorBlocked(blockedRcvrs);
                } else {
                    if(_debugging) _debug(_name+": Blocked branch registration didn't work; director not ProcessDirector");
                    // FIXME: Do something; Set _notDone = false???
                }
                notifyAll();
                return true;
            }
        }
        */
        return false;
    }

    /** 
     */
    protected synchronized boolean _isOutputControllerBlocked() {
        return _outputControllerIsBlocked;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _onFirstIteration = true;
    
    // The Branch Controllers of this director
    private BranchController _inputBranchController;
    private BranchController _outputBranchController;
    
    private boolean _inputControllerIsBlocked = true;
    private boolean _outputControllerIsBlocked = true;
    
    private LinkedList _blockedRcvrs;
    private int _blockedActorCount = 0;
    
    private Object _branchCntlrLock = new Object();
    
    
    private String _name = null;
    
}
