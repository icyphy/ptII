/* A DDEDirector governs the execution of actors operating according
to the DDE model of computation.

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
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

import java.util.Hashtable;
import java.util.Enumeration;

import collections.LinkedList;
import collections.Comparator;

//////////////////////////////////////////////////////////////////////////
//// DDEDirector
/**
A DDEDirector governs the execution of actors operating according
to the DDE model of computation (MoC). The DDE MoC incorporates
a distributed notion of time into a dataflow style communication
semantic. Much of the functionality of the DDEDirector is consistent
with the Process Networks director PNDirector. In particular, the
mechanism for dealing with blocking due to empty or full queues is
functionally identical for the DDEDirector and PNDirector.
<P>
The DDE domain's use of time serves as the point of divergence in
the respective designs of the DDE and PN directors. In a network of
actors governed by a DDEDirector each actor has a local notion of
time. Several features of the DDEDirector are intended to facilitate
these local notions of time.
<P>
All DDE models have a completion time. The completion time is a preset
time after which all execution ceases. The completion time for a
DDEDirector is specified via the <I>stopTime</I> parameter. The value
of the stopTime parameter is passed to the receivers of all actors
that the DDEDirector governs via newReceiver() during initialize().
After initialize() has been called, the value of stopTime can not be
changed.
<P>
The default value of the stopTime parameter is
TimedQueueReceiver.ETERNITY. Given this value, a DDE model will
continue executing without regard for a completion time.
<P>
Deadlock due to feedback loops is dealt with via NullTokens. When an
actor in a DDE model receives a NullToken, it may advance its local
time value even though no computation results directly from
consumption of the NullToken. For models with feedback topologies,
the actor FBDelay should be used.
<P>
The DDE model of computation assumes that valid time stamps have
non-negative values. Three special purpose negative time values
are reserved with the following meanings. The value of
TimedQueueReceiver.INACTIVE is reserved to indicate the termination
of a receiver. The value of TimedQueueReceiver.ETERNITY is reserved
to indicate that a receiver has not begun to participate in a model's
execution. The value of TimedQueueReceiver.IGNORE is reserved to
indicate that the current token at the head of a DDEReceiver should
be ignored in favor of the tokens contained in the other receivers
of the actor in question. More details of IGNORE can be found in
FBDelay.
<P>
NOTE: The current implementation of this director does not
include an infrastructure for mutations. Hence, ChangeRequest
and other facilities for changing the topology of a model are
not included in this director.


@author John S. Davis II, Mudit Goel
@version $Id$
@see ptolemy.domains.pn.kernel.PNDirector
@see ptolemy.domains.dde.kernel.FBDelay
@see ptolemy.domains.dde.kernel.NullToken
*/
public class DDEDirector extends ProcessDirector {

    /** Construct a DDEDirector in the default workspace with
     *  an empty string as its name. The director is added to
     *  the list of objects in the workspace. Increment the
     *  version number of the workspace.
     */
    public DDEDirector() {
        super();

	try {
            double val = TimedQueueReceiver.ETERNITY;
	    stopTime = new
                Parameter(this, "stopTime", new DoubleToken(val) );
	} catch( IllegalActionException e ) {
	    throw new InternalErrorException( e.toString() );
        } catch (NameDuplicationException e) {
            throw new InvalidStateException( e.toString() );
	}
    }

    /** Construct a director in the  workspace with an empty
     *  string as a name. The director is added to the list of
     *  objects in the workspace. Increment the version number
     *  of the workspace.
     * @param workspace The workspace of this object.
     */
    public DDEDirector(Workspace workspace) {
        super(workspace);

	try {
            double val = TimedQueueReceiver.ETERNITY;
	    stopTime = new
                Parameter(this, "stopTime", new DoubleToken(val) );
	} catch( IllegalActionException e ) {
	    throw new InternalErrorException( e.toString() );
        } catch (NameDuplicationException e) {
            throw new InvalidStateException( e.toString() );
	}
    }

    /** Construct a director in the given container with the
     *  given name. If the container argument must not be null,
     *  or a NullPointerException will be thrown. The given
     *  name must be unique with respect to the container. If
     *  the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     * @param workspace Object for synchronization and version
     *  tracking
     * @param name Name of this director.
     * @exception IllegalActionException It may be thrown in derived
     *  classes if the director is not compatible with the specified
     *  container.
     */
    public DDEDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);

	try {
            double val = TimedQueueReceiver.ETERNITY;
	    stopTime = new
                Parameter(this, "stopTime", new DoubleToken(val) );
	} catch( IllegalActionException e ) {
	    throw new InternalErrorException( e.toString() );
        } catch (NameDuplicationException e) {
            throw new InvalidStateException( e.toString() );
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public Parameter stopTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of the DDEThread that calls this
     *  method on behalf of an actor. If this method is called by
     *  other than a DDEThread, then return the current time as
     *  specified by the superclass of this method.
     * @return The current time of the DDEThread that calls this
     *  method.
     */
    public double getCurrentTime() {
	Thread thread = Thread.currentThread();
	if( thread instanceof DDEThread ) {
	    TimeKeeper tk = ((DDEThread)thread).getTimeKeeper();
	    return tk.getCurrentTime();
	} else {
	    return super.getCurrentTime();
	}
    }

    /** Schedule an actor to be fired at the specified time.
     *  If the thread that calls this method is an instance
     *  of DDEThread, then the specified actor must be
     *  contained by this thread. If the thread that calls
     *  this method is not an instance of DDEThread, then
     *  store the actor and refire time in the initial time
     *  table of this director.
     *  <P>
     *  NOTE: The current implementation of this method is such
     *  that a more appropriate name might be <I>continueAt()</I>
     *  rather than <I>fireAt()</I>.
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
	    // Add the start time of actor to initialize table
	    if( _initialTimeTable == null ) {
		_initialTimeTable = new Hashtable();
	    }
	    _initialTimeTable.put( actor, new Double(time) );
            return;
        }

	if( _completionTime != -5.0 && time > _completionTime ) {
	    return;
	}

        Actor threadActor = ddeThread.getActor();
        if( threadActor != actor ) {
            throw new IllegalActionException("Actor argument of "
                    + "DDEDirector.fireAt() must be contained "
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

    /** Return true if one of the actors governed by this
     *  director has a pending mutation; return false
     *  otherwise.
     * @return True if a pending mutation exists; return
     *  false otherwise.
     */
    public boolean hasMutation() {
        return false;
    }

    /** Initialize this director and the actors it contains
     *  and set variables to their initial values. Create a
     *  DDEThread for each actor that this director controls
     *  but do not start the thread.
     * @exception IllegalActionException If there is an
     *  error during the creation of the threads or
     *  initialization of the actors.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	_completionTime = -5.0;
        _readBlocks = 0;
        _writeBlocks = 0;
        _pendingMutations = false;
        _writeBlockedQs = new LinkedList();
   }

    /** Return a new receiver of a type compatible with this
     *  director. If the completion time of this director has
     *  been explicitly set to a particular value then set the
     *  completion time of the receiver to this same value;
     *  otherwise set the completion time to
     *  TimedQueueReceiver.ETERNITY which indicates that the
     *  receivers should ignore the completion time.
     *  @return A new DDEReceiver.
     */
    public Receiver newReceiver() {
        DDEReceiver rcvr = new DDEReceiver();
	double time = _completionTime;
	try {
	    time = ((DoubleToken)stopTime.getToken()).doubleValue();
	} catch( IllegalActionException e ) {
	    throw new InternalErrorException( e.toString() );
	}
	rcvr.setCompletionTime( time );
        return rcvr;
    }

    /** Return true if the actors governed by this director can
     *  continue execution; return false otherwise. Continuation
     *  of execution is dependent upon whether the system is
     *  deadlocked in a manner that can not be resolved even if
     *  external communication occurs.
     * @return True if execution can continue; false otherwise.
     * @exception IllegalActionException Not thrown in this class.
     *  May be thrown in derived classes.
     */
    public boolean postfire() throws IllegalActionException {
	return _notdone;
    }

    ///////////////////////////////////////////////////////////////////
    ////                  package friendly methods                 ////

    /** Increment the count of actors blocked on a read.
     */
    synchronized void addReadBlock() {
        _readBlocks++;
	if( _isDeadlocked() ) {
	    notifyAll();
	}
    }

    /** Increment the count of actors blocked on a write.
     * @param rcvr The DDEReceiver that has a write block.
     */
    synchronized void addWriteBlock(DDEReceiver rcvr) {
        _writeBlocks++;
	if( _writeBlockedQs == null ) {
	    _writeBlockedQs = new LinkedList();
	}
	_writeBlockedQs.insertFirst(rcvr);
	if( _isDeadlocked() ) {
	    notifyAll();
	}
    }

    /** Return the initial time table of this director.
     * @return The initial time table of this actor.
     */
    Hashtable getInitialTimeTable() {
	if( _initialTimeTable == null ) {
	    _initialTimeTable = new Hashtable();
	}
	return _initialTimeTable;
    }

    /** Decrement the count of actors blocked on a read.
     */
    synchronized void removeReadBlock() {
        if( _readBlocks > 0 ) {
            _readBlocks--;
        }
    }

    /** Decrement the count of actors blocked on a write.
     * @param rcvr The DDEReceiver that is no longer write blocked.
     */
    synchronized void removeWriteBlock(DDEReceiver rcvr) {
        if( _writeBlocks > 0 ) {
            _writeBlocks--;
        }
	if( _writeBlockedQs == null ) {
	    _writeBlockedQs = new LinkedList();
	}
        _writeBlockedQs.removeOneOf(rcvr);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Determine if all of the threads containing actors controlled
     *  by this director have stopped due to a call of stopFire() or
     *  because they are blocked on a read or write.
     * @return True if all active threads containing actors controlled
     *  by this thread have stopped; otherwise return false.
     */
    protected synchronized boolean _areAllThreadsStopped() {
	long threadsStopped = _getStoppedProcessesCount();
	long actorsActive = _getActiveActorsCount();

	// All threads are stopped due to stopFire()
	if( threadsStopped > 0 && threadsStopped >= actorsActive ) {
	    return true; 
	} 

	// Some threads are stopped due to stopFire() while others
	// are blocked waiting to read or write data.
	if( threadsStopped + _readBlocks + _writeBlocks >= actorsActive ) {
	    if( threadsStopped > 0 ) {
	        return true; 
	    }
	}

	return false;
    }

    /** Return a new ProcessThread of a type compatible with this
     *  director.
     * @param actor The actor that the new ProcessThread will
     *  control.
     * @param director The director that manages the new
     *  ProcessThread.
     * @exception IllegalActionException If an error occurs while
     *  instantiating the new ProcessThread.
     */
    protected ProcessThread _getProcessThread(Actor actor,
	    ProcessDirector director) throws IllegalActionException {
	return new DDEThread(actor, director);
    }

    /** Resolve any deadlocks of the actors governed by this
     *  director. Return true if the deadlock has successfully
     *  been resolved; return false otherwise.
     * @return True if deadlocks no longer exist; return
     *  false otherwise.
     */
    protected boolean _handleDeadlock() throws
    	    IllegalActionException {
        if( _writeBlocks != 0 ) {
            // Artificial Non-timed Deadlock
            System.out.println("Artificial deadlock!! Write blocks = "
                    + _writeBlocks);
            _incrementLowestCapacityPort();
        } else {
            // Real Non-timed Deadlock
            System.out.println("Real deadlock!! Read blocks = "
            	    + _readBlocks);
            return true;
        }

        if( _pendingMutations ) {
	    /* FIXME
               try {
               _processTopologyRequests();
               } catch( TopologyChangeFailedException e ) {
               throw new IllegalActionException("TopologyChangeFailed: "
               + e.getMessage());
               }
	    */
        }
        return false;
    }

    /** Increment the port capacity's according to Tom Parks'
     *  algorithm. Select the port with the smallest capacity
     *  and double the capacity.
     * @exceptions IllegalActionException If there is an error
     *  while attempting to set the capacity of a DDE receiver.
     */
    protected void _incrementLowestCapacityPort()
            throws IllegalActionException {
	System.out.println("Call to "
        	+ "_incrementLowestCapacityPort()");
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

    /** Mutate the model that this director controls.
     *  FIXME
     protected void _processTopologyRequests() throws
     TopologyChangeFailedException {
     }
    */

    /** Mutate the model that this director controls.
     *  FIXME
     */
    protected void _performMutations() {
        ;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _completionTime = TimedQueueReceiver.ETERNITY;
    private int _readBlocks = 0;
    private int _writeBlocks = 0;
    private boolean _pendingMutations = false;
    private LinkedList _writeBlockedQs;
    private Hashtable _initialTimeTable;


    ///////////////////////////////////////////////////////////////////
    ////                         inner class			   ////

    private class RcvrCapacityComparator implements Comparator {

        /**
         * @exception ClassCastException If fst and scd are
         *  not instances of DDEReceiver.
         */
        public int compare(Object fst, Object scd) {
            DDEReceiver first = null;
            DDEReceiver second = null;

            if( fst instanceof DDEReceiver ) {
                first = (DDEReceiver)fst;
            }
            if( scd instanceof DDEReceiver ) {
                second = (DDEReceiver)scd;
            }

            if( first.getCapacity() < second.getCapacity() ) {
                return 1;
            } else if( first.getCapacity() > second.getCapacity() ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
