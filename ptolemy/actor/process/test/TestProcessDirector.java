/* An atomic actor for testing Process Director.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.actor.process.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TestProcessDirector
/**
A TestProcessDirector is a simple atomic actor that is used for testing the
actor package constructs for Processes. It overrides the action methods to
return false in the postfire after the first invocation of fire method.

@author John S. Davis II
@version $Id$
*/
public class TestProcessDirector extends ProcessDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public TestProcessDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public TestProcessDirector(Workspace workspace) {
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
    public TestProcessDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
	_name = name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Wait until a deadlock is detected. Then handle the deadlock
     *  (by calling the protected method _handleDeadlock()) and return.
     *  This method is synchronized on the director.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void fire() throws IllegalActionException {
        Workspace workspace = workspace();
        synchronized(this) {
            _continueFireMethod = true;
            while( _continueFireMethod ) {
                _continueFireMethod = false;
		if( _debugging ) _debug(_name+": beginning fire() cycle;"
                +"there are "+_actorsBlocked+" actors blocked, " 
                +_getStoppedProcessesCount()+" actors stopped and "
                +_getActiveActorsCount()+" actors active.");
                while( !_areActorsDeadlocked() && !_areActorsStopped() ) {
                    if(_debugging) _debug(_name+": about to wait");
                    // workspace.wait(this);
                    try {
                        wait();
                    } catch(InterruptedException e) {
                        // Do nothing
                    }
                    if(_debugging) _debug(_name+": waking up");
                }
		if( _debugging ) _debug(_name+": actors deadlocked or stopped");
                if( _areActorsDeadlocked() ) {
		    if( _debugging ) _debug(_name+": beginning deadlock resolution");
                    _attemptToResolveDeadlock();
                } else if( _areActorsStopped() ) {
                    stopInputBranchController();
                    stopOutputBranchController();
                    _continueFireMethod = false;
                    _notDone = true;
                }
            }
        }
    }
            
    /**
     *  @exception IllegalActionException If a derived class throws it.
     */
    protected boolean haveExternalReadDeadlock() throws IllegalActionException {
        /*
        */
        boolean externalReadBlock = false;
	if( _debugging ) _debug(_name+": inside haveExternalReadDeadlock;"
                +"there are "+_actorsBlocked+" actors blocked, " 
                +_getStoppedProcessesCount()+" actors stopped and "
                +_getActiveActorsCount()+" actors active.");
        if( _areActorsDeadlocked() ) {
            Iterator rcvrs = _blockedRcvrList.iterator();
            /*
            if( !rcvrs.hasNext() ) {
                if(_debugging) _debug("EEEEEEEE: No blocked receivers");
                externalReadBlock = true;
            }
            */
            while( rcvrs.hasNext() ) {
                ProcessReceiver rcvr = (ProcessReceiver)rcvrs.next();
                if(_debugging) _debug("EEEEEEEE: Have a blocked receiver");
                if( rcvr.isReadBlocked() ) {
                    if(_debugging) _debug("EEEEEEEE: One receiver blocked on read");
                    if( rcvr.isConnectedToBoundaryInside() ) {
                        if(_debugging) _debug("EEEEEEEE: One receiver blocked on external read");
        		externalReadBlock = true;
                    }
                }
            }
        }
        /*
        if( _getActiveActorsCount() == 0 ) { 
            externalReadBlock = false;
        }
        */
        if(_debugging) _debug(_name+": haveExternalReadDeadlock() returning " + externalReadBlock);
        return externalReadBlock;
        // return true;
    }
            
    /**
     *  @exception IllegalActionException If a derived class throws it.
     */
    protected void _attemptToResolveDeadlock() throws IllegalActionException {
        Workspace workspace = workspace();
        // if( true ) {
        if( haveExternalReadDeadlock() ) {
            // We know it will be external read deadlocked
            // by virtue of the test.
                
            // Since this is an external read block
            // we might as well wait to see what
            // happens. Either the input will block
            // or the input will not block and the
            // actors will no longer be deadlocked.
	    while( _areActorsDeadlocked() && !_isInputControllerBlocked() ) {
		if( _debugging ) _debug(_name+": actors deadlocked; waiting for input to block");
		workspace.wait(this);
	    }
            if( _areActorsDeadlocked() ) {
		if( _debugging ) _debug(_name+": actors deadlocked");
            } else if( !_areActorsDeadlocked() ) {
		if( _debugging ) _debug(_name+": actors no longer deadlocked");
            }
            if( _isInputControllerBlocked() ) {
		if( _debugging ) _debug(_name+": input controller stopped");
            } else if( !_isInputControllerBlocked() ) {
		if( _debugging ) _debug(_name+": input controller is not stopped");
            }
	    // if( _debugging ) _debug(_name+": deadlocked or input controller stopped");

	    while( _areActorsDeadlocked() && _isInputControllerBlocked() ) {
		// Reaching this point means that both the
		// actors and the input have blocked. 
		// However, it is possible that the input
		// could awaken resulting in an end to the
		// external read block
		if( _debugging ) _debug(_name+": actors deadlocked and input controller stopped");
		CompositeActor container = (CompositeActor)getContainer();
		if( container.getContainer() != null ) {
                    if( registerBlockedBranchReceiversWithExecutive() ) {
		        if( _debugging ) _debug(_name+": just registered blocked branches to executive director; there are "+_actorsBlocked+" actors blocked; now waiting");
			workspace.wait(this);
		    } else {
			// Since the higher level actor is not a 
			// process, let's end this iteration and
			// request another iteration. Recall that
			// calling stopInputBranchController()
			// is a blocking call that when finished
			// will guarantee that the (input) branches
			// will not restart during this iteration.
			stopInputBranchController();

			if( _areActorsDeadlocked() ) {
			    _notDone = false;
			    return;
			} else if( !_areActorsDeadlocked() ) {
			    // It is possible that prior to
			    // stopping the branch controller
			    // a token stuck that caused the
			    // deadlocked actors to awaken.
			    _continueFireMethod = true;
			}
		    }
	        } else {
		    _notDone = false;
		    return;
		}
	    }
	    if( !_areActorsDeadlocked() || !_isInputControllerBlocked() ) {
		// Reaching this point means that the
		// fire method should continue
		_continueFireMethod = true;
            }
   	} else {
            // This is not an external read deadlock. Additional
            // inputs will do no good. Stop the input controllers
            // and wait for the output controllers.
            if(_debugging) _debug(_name+": calling stopInputBranchController()");
            stopInputBranchController();
            if(_debugging) _debug(_name+": finished calling stopInputBranchController()");
            /*
            while( _areActorsDeadlocked() && !_isOutputControllerBlocked() ) {
                workspace.wait(this);
            }
            */
	    // if( !_areActorsDeadlocked() || !_isOutputControllerBlocked() ) {
	    if( !_areActorsDeadlocked() ) {
                if(_debugging) _debug(_name+": actors not deadlocked; will continue fire method");
		// Reaching this point means that the
		// fire method should continue
		_continueFireMethod = true;
            } else {
                if(_debugging) _debug(_name+": actors are deadlocked; will not continue fire method");
		// Reaching this point means that the
	        _continueFireMethod = false;
                _notDone = false;
            }
        }
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorBlocked(ProcessReceiver rcvr) {
    	_blockedRcvrList.addFirst(rcvr);
        _actorsBlocked++;
        notifyAll();
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorBlocked(LinkedList blockedRcvrs) {
        Iterator rcvrs = blockedRcvrs.iterator();
        ProcessReceiver rcvr = null;
        while( rcvrs.hasNext() ) {
            rcvr = (ProcessReceiver)rcvrs.next();
            _blockedRcvrList.addFirst( rcvr ) ;
    	}
        _actorsBlocked++;
        notifyAll();
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorUnBlocked(ProcessReceiver rcvr) {
        while( _blockedRcvrList.contains(rcvr) ) {
    	    _blockedRcvrList.remove(rcvr);
        }
        if( _actorsBlocked > 0 ) {
            _actorsBlocked--;
        }
        notifyAll();
    }

    /** Implementations of this method must be synchronized.
     * @param internal True if internal read block.
     */
    protected synchronized void _actorUnBlocked(LinkedList blockedRcvrs) {
        Iterator rcvrs = blockedRcvrs.iterator();
        ProcessReceiver rcvr = null;
        while( rcvrs.hasNext() ) {
            while( _blockedRcvrList.contains(rcvr) ) {
    		_blockedRcvrList.remove(rcvr);
            }
        }
        if( _actorsBlocked > 0 ) {
            _actorsBlocked--;
        }
        notifyAll();
    }

    /**
     */
    protected synchronized boolean _areActorsDeadlocked() {
        long activeActors = _getActiveActorsCount();
        if( _actorsBlocked >= activeActors ) {
            return true;
        }
        return false;
    }
    
    /** 
     */
    protected synchronized boolean _areActorsStopped() {
        long activeActors = _getActiveActorsCount();
        long stoppedActors = _getStoppedProcessesCount();
        
        if( activeActors == 0 ) {
            return true;
        }
        if( stoppedActors + _actorsBlocked >= activeActors ) {
            if( stoppedActors > 0 ) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean _continueFireMethod;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private LinkedList _blockedRcvrList = new LinkedList();
    
    private int _actorsBlocked = 0;

    private String _name = null;

}
