/* An ODFDirector governs the execution of actors operating according
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

package ptolemy.domains.odf.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;

//////////////////////////////////////////////////////////////////////////
//// ODFDirector
/**
An ODFDirector governs the execution of actors operating according
to the Ordered Dataflow (ODF) model of computation (MoC). The actors
which a given ODFDirector "governs" are those which are contained
by the container of the director. The Ordered Dataflow MoC incorporates
a distributed notion of time into a dataflow style communication semantic.
Much of the functionality of the ODFDirector is consistent with the Process
Networks director PNDirector. In particular, the mechanism for dealing
with blocking due to empty or full queues is functionally identical for
the ODFDirector and PNDirector.
<P>
The ODF domain's use of time serves as the point of divergence in
the respective designs of the ODF and PN directors. In a network of
actors governed by an ODFDirector each actor has a local notion of
time. Several features of the ODFDirector are intended to facilitate
these local notions of time.
<P>
All ODF models have a completion time. The completion time is a preset time
after which all execution ceases. The completion time for an ODFDirector is
specified via setCompletionTime() and this information is passed to the
receivers of all actors that the ODFDirector governs via newReceiver().
<P>
Deadlock due to feedback loops is dealt with via NullTokens. When an
actor in an ODF model receivers a NullToken, it may advance its local
time value even though it does not consume the NullToken.
<P>
The ODF model of computation assumes that valid time stamps have non-negative
values. A time stamp value of -1.0 is reserved to indicate the termination of
a receiver. A time stamp value of -5.0 is reserved to indicate that a
receiver has not begun to participate in a model's execution.


@author John S. Davis II
@version @(#)ODFDirector.java	1.3	11/16/98
@see ptolemy.domains.pn.kernel.PNDirector
@see ptolemy.domains.odf.kernel.ODFActor
*/
public class ODFDirector extends ProcessDirector {

    /** Construct a ODFDirector in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in the
     *  workspace. Increment the version number of the workspace.
     */
    public ODFDirector() {
        super();
    }

    /** Construct a ODFDirector in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public ODFDirector(String name) {
        super(name);
    }

    /** Construct an ODFDirector in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public ODFDirector(Workspace workspace, String name) {
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

    /** Increment the count of actors blocked on a write.
     */
    synchronized void addWriteBlock() {
        _writeBlocks++;
	notifyAll();
    }

    /** Execute all deeply contained actors that are governed by this
     *  ODFDirector. Check for deadlocks when they occur, and where possible,
     *  resolve them.
     * @exception IllegalActionException If any called method of the
     *  container or one of the deeply contained actors throws it.
     */
    public void fire() throws IllegalActionException {
        Workspace wkSpace = workspace();

        synchronized( this ) {
	    while( !_checkForDeadlock() ) {
	        wkSpace.wait(this);
	    }
	    _notdone = !_handleDeadlock();
        }
    }

    /** Return true if one of the actors governed by this director
     *  has a pending mutation; return false otherwise.
     * @return True if a pending mutation exists; return false otherwise.
     */
    public boolean hasMutation() {
        return false;
    }

    /** Return a new receiver of a type compatible with this director.
     *  If the completion time of this director has been explicitly set
     *  to a particular value then set the completion time of the receiver
     *  to this same value; otherwise set the completion time to -5.0
     *  which indicates that the receivers should ignore the completion
     *  time.
     *  @return A new ODFReceiver.
     */
    public Receiver newReceiver() {
        ODFReceiver rcvr = new ODFReceiver();
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
     */
    public synchronized void removeWriteBlock() {
        if( _writeBlocks > 0 ) {
            _writeBlocks--;
        }
    }

    /** Set the completion time of all actors governed by this
     *  director. If this method is not called then the governed
     *  actors will act as if there is no completion time.
     * @param time The specified completion time.
     *  FIXME: What if this value is negative?
     */
    public void setCompletionTime(double time) {
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
    protected synchronized boolean _checkForDeadlock() {
        if( _getActiveActorsCount() == _readBlocks + _writeBlocks ) {
            return true;
        }
        return false;
    }

    /**
     */
    protected ProcessThread _getProcessThread(Actor actor,
	    ProcessDirector director) throws IllegalActionException {
	return new ODFThread(actor, director);
    }

    /** Resolve any deadlocks of the actors governed by this director.
     *  Return true if the deadlock has successfully been resolved;
     *  return false otherwise.
     *  <P>
     *  Currently this method assumes that all queues have infinite
     *  capacity and hence that deadlocks based on writes can not
     *  occur. FIXME: Should this return false then??
     * @return True if deadlocks no longer exist; return false otherwise.
     */
    protected boolean _handleDeadlock() {
        // System.out.println("*** Deadlock Needs To Be Resolved!!!");
        // Currently assume only real deadlocks.
        return true;
    }

    /**
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

}















