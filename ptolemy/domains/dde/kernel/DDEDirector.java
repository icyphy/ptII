/* An DDEDirector governs the execution of actors operating according
to the Ordered Dataflow model of computation.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import java.util.Enumeration;
import collections.LinkedList;
import collections.Comparator;

//////////////////////////////////////////////////////////////////////////
//// DDEDirector
/**
An DDEDirector governs the execution of actors operating according
to the Ordered Dataflow (DDE) model of computation (MoC). The actors
which a given DDEDirector "governs" are those which are contained
by the container of the director. The Ordered Dataflow MoC incorporates
a distributed notion of time into a dataflow style communication semantic.
Much of the functionality of the DDEDirector is consistent with the Process
Networks director PNDirector. In particular, the mechanism for dealing
with blocking due to empty or full queues is functionally identical for
the DDEDirector and PNDirector.
<P>
The DDE domain's use of time serves as the point of divergence in
the respective designs of the DDE and PN directors. In a network of
actors governed by an DDEDirector each actor has a local notion of
time. Several features of the DDEDirector are intended to facilitate
these local notions of time.
<P>
All DDE models have a completion time. The completion time is a preset time
after which all execution ceases. The completion time for an DDEDirector is
specified via setCompletionTime() and this information is passed to the
receivers of all actors that the DDEDirector governs via newReceiver().
<P>
Deadlock due to feedback loops is dealt with via NullTokens. When an
actor in an DDE model receivers a NullToken, it may advance its local
time value even though it does not consume the NullToken.
<P>
The DDE model of computation assumes that valid time stamps have non-negative
values. A time stamp value of -1.0 is reserved to indicate the termination of
a receiver. A time stamp value of -5.0 is reserved to indicate that a
receiver has not begun to participate in a model's execution.


@author John S. Davis II, Mudit Goel
@version $Id$
@see ptolemy.domains.pn.kernel.PNDirector
@see ptolemy.domains.dde.kernel.DDEActor
*/
public class DDEDirector extends ProcessDirector {

    /** Construct a DDEDirector in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in the
     *  workspace. Increment the version number of the workspace.
     */
    public DDEDirector() {
        super();
    }

    /** Construct a DDEDirector in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public DDEDirector(String name) {
        super(name);
    }

    /** Construct an DDEDirector in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public DDEDirector(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Increment the count of actors blocked on a read.
     */
    synchronized void addReadBlock() {
        _readBlocks++;
	notifyAll();
    }

    /** 
     */
    public synchronized void registerWriteBlock(DDEReceiver rcvr) {
        _writeBlocks++;
	if( _writeBlockedQs == null ) {
	    _writeBlockedQs = new LinkedList();
	} 
        _writeBlockedQs.insertFirst(rcvr);
    }
    
    /** Increment the count of actors blocked on a write.
     *  FIXME
     */
    synchronized void addWriteBlock(DDEReceiver rcvr) {
        System.out.println("Added write block.");
        _writeBlocks++;
	if( _writeBlockedQs == null ) {
	    _writeBlockedQs = new LinkedList();
	} 
	_writeBlockedQs.insertFirst(rcvr);
	notifyAll();
    }

    /** Execute all deeply contained actors that are governed by this
     *  DDEDirector. Check for deadlocks when they occur, and where possible,
     *  resolve them.
     * @exception IllegalActionException If any called method of the
     *  container or one of the deeply contained actors throws it.
     */
    public void fire() throws IllegalActionException {
        Workspace wkSpace = workspace();
        synchronized( this ) {
	    while( !_isDeadlocked() ) {
	        wkSpace.wait(this);
	    }
	    _notdone = !_handleDeadlock();
        }
    }

    /** Schedule an actor to be fired at the specified time.
     *  If the thread that calls this method is a DDEThread,
     *  then the specified actor must be contained by this 
     *  thread. It the thread that calls this method is not
     *  an instance of DDEThread, then this method returns
     *  without performing any actions.
     * @param actor The actor scheduled to fire.
     * @param time The scheduled time to fire.
     * @exception IllegalActionException If the specified 
     *  time is in the past or if the thread calling this
     *  method is a DDEThread but the specified actor is 
     *  not contained by the DDEThread.
     */
    public void fireAt(Actor actor, double time) 
            throws IllegalActionException {
        
        DDEThread ddeThread;
        Thread thread = Thread.currentThread();
        if( thread instanceof DDEThread ) {
            ddeThread = (DDEThread)thread;
        } else {
            return;
        }
        
        Actor threadActor = ddeThread.getActor();
        if( threadActor != actor ) {
            throw new IllegalActionException("Actor argument of " 
                    + "must DDEDirector.fireAt() must be contained "
                    + "by the DDEThread that calls fireAt()");
        }
        
        TimeKeeper timeKeeper = ddeThread.getTimeKeeper();
        try {
            timeKeeper.setCurrentTime(time);
        } catch( IllegalArgumentException e ) {
	    throw new IllegalActionException(
		    ((NamedObj)actor).getName() + " - Attempt to "
		    + "set current time in the past.");
        }
    }
    
    /** Return true if one of the actors governed by this director
     *  has a pending mutation; return false otherwise.
     * @return True if a pending mutation exists; return false otherwise.
     */
    public boolean hasMutation() {
        return false;
    }

    /** Initialize this director and the actors it contains and set
     *  variables to their initial values. Create a DDEThread for 
     *  each actor that this director controls but do not start
     *  the thread. 
     * @exception IllegalActionException If there is an error
     *  during the creation of the threads or initialization of
     *  the actors.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _completionTime = -5.0;
        _readBlocks = 0;
        _writeBlocks = 0;
        _pendingMutations = false;
        _writeBlockedQs = new LinkedList();
    }
    
    /** Return a new receiver of a type compatible with this director.
     *  If the completion time of this director has been explicitly set
     *  to a particular value then set the completion time of the receiver
     *  to this same value; otherwise set the completion time to -5.0
     *  which indicates that the receivers should ignore the completion
     *  time.
     *  @return A new DDEReceiver.
     */
    public Receiver newReceiver() {
        DDEReceiver rcvr = new DDEReceiver();
	rcvr.setCompletionTime( _completionTime );
        return rcvr;
    }

    /** Return true if the actors governed by this director can continue
     *  execution; return false otherwise. Continuation of execution is
     *  dependent upon whether the system is deadlocked in a manner that
     *  can not be resolved even if external communication occurs.
     * @return True if execution can continue; false otherwise.
     * @exception IllegalActionException Not thrown in this class. May be
     *  thrown in derived classes.
     */
    public boolean postfire() throws IllegalActionException {
	return _notdone;
    }

    /** Decrement the count of actors blocked on a read.
     */
    public synchronized void removeReadBlock() {
        if( _readBlocks > 0 ) {
            _readBlocks--;
        }
    }

    /** Decrement the count of actors blocked on a write.
     *  FIXME
     */
    public synchronized void removeWriteBlock(DDEReceiver rcvr) {
        System.out.println("Removed write block.");
        if( _writeBlocks > 0 ) {
            _writeBlocks--;
        }
	if( _writeBlockedQs == null ) {
	    _writeBlockedQs = new LinkedList();
	} 
        _writeBlockedQs.removeOneOf(rcvr);
    }

    /** Set the completion time of all actors governed by this
     *  director to a nonnegative value. If this method is not 
     *  called then the governed actors will act as if there is 
     *  no completion time. If the completion time argument is
     *  negative, then throw an IllegalArgumentException.
     * @param time The specified completion time.
     */
    public void setCompletionTime(double time) {
	if( time < 0.0 ) {
	    throw new IllegalArgumentException(getName() +
		    " - Attempt to set completion time to a " +
		    "negative value.");
	}
        _completionTime = time;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Check to see if the actors governed by this director are
     *  deadlocked. Return true in the affirmative and false
     *  otherwise.
     * @return True if the actors governed by this director are
     *  deadlocked; return false otherwise.
     */
    protected synchronized boolean _isDeadlocked() {
        if( _getActiveActorsCount() == _readBlocks + _writeBlocks ) {
            return true;
        }
        return false;
    }

    /** Return a new ProcessThread of a type compatible with this
     *  director.
     * @param actor The actor that the new ProcessThread will control.
     * @param director The director that manages the new ProcessThread.
     * @exception IllegalActionException If an error occurs while
     *  instantiating the new ProcessThread.
     */
    protected ProcessThread _getProcessThread(Actor actor,
	    ProcessDirector director) throws IllegalActionException {
	return new DDEThread(actor, director);
    }

    /** Resolve any deadlocks of the actors governed by this director.
     *  Return true if the deadlock has successfully been resolved;
     *  return false otherwise.
     * @return True if deadlocks no longer exist; return false otherwise.
     */
    protected boolean _handleDeadlock() throws IllegalActionException {
        if( _writeBlocks != 0 ) {
            // Artificial Non-timed Deadlock
            incrementLowestCapacityPort();
        } else {
            // Real Non-timed Deadlock
            return true;
        }
        
        if( _pendingMutations ) {
            try {
                _processTopologyRequests();
            } catch( TopologyChangeFailedException e ) {
                throw new IllegalActionException("TopologyChangeFailed: " 
                        + e.getMessage());
            }
        }
        return false;
    }

    /** Increment the port capacity's according to Tom Parks' 
     *  algorithm.
     *  FIXME
     * @exceptions IllegalActionException If there is an error
     *  while attempting to set the capacity of a DDE receiver.
     */
    protected void incrementLowestCapacityPort() 
            throws IllegalActionException {
	if( _writeBlockedQs == null ) {
	    _writeBlockedQs = new LinkedList();
	} 
        _writeBlockedQs.sort(new RcvrCapacityComparator() );
        DDEReceiver smallestQueue;
        smallestQueue = (DDEReceiver)_writeBlockedQs.first();
        
        if( smallestQueue.getCapacity() <= 0 ) {
            smallestQueue.setCapacity(1);
        } else {
            int cap = smallestQueue.getCapacity(); 
            smallestQueue.setCapacity(cap * 2);
        }
        removeWriteBlock( smallestQueue );
        synchronized( smallestQueue ) {
            smallestQueue.notifyAll();
        }
    }
    
    /** Mutate the model that this director controls.
     *  FIXME
     */
    protected void _processTopologyRequests() throws
            TopologyChangeFailedException {
    }
    
    /** Mutate the model that this director controls.
     *  FIXME
     */
    protected void _performMutations() {
        ;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    private double _completionTime = -5.0;
    private int _readBlocks = 0;
    private int _writeBlocks = 0;
    private boolean _pendingMutations = false;
    private LinkedList _writeBlockedQs;
    

    ///////////////////////////////////////////////////////////////////
    ////                         inner class			   ////
    
    private class RcvrCapacityComparator implements Comparator {
            
       /**
        * @exception ClassCastException If fst and scd are
        *  not instances of DDEReceiver.
        */
       public int compare( Object fst, Object scd ) {
           DDEReceiver first = null;
           DDEReceiver second = null;
           
           if( fst instanceof DDEReceiver ) {
               first = (DDEReceiver)fst;
           }
           if( scd instanceof DDEReceiver ) {
               second = (DDEReceiver)scd;
           }
           
           if( first.getCapacity() > second.getCapacity() ) {
               return 1;
           } else if( first.getCapacity() < second.getCapacity() ) {
               return -1;
           } else {
               return 0;
           }
       }
    }
}















