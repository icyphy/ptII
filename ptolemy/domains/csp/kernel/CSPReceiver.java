/* Receiver for CSP style communication.

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

@ProposedRating Green (nsmyth@eecs.berkeley.edu)
@AcceptedRating Green (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.kernel;

import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.data.Token;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.util.InvalidStateException;

import java.util.List;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// CSPReceiver
/**
Receiver for CSP style communication. In CSP all communication is via
synchronous message passing, so both the the sending and receiving
process need to rendezvous at the receiver. For rendezvous, the
receiver is the key synchronization point. It is assumed each receiver
has at most one thread trying to send to it and at most one thread
trying to receive from it at any one time. The receiver performs the
synchronization necessary for simple rendezvous (get() and put()
operations). It also stores the flags that allow the ConditionalSend
and ConditionalReceive branches to know when they can proceed.
<p>
@author Neil Smyth, John S. Davis II
@version $Id$
*/

public class CSPReceiver implements ProcessReceiver {

    /** Construct a CSPReceiver with no container.
     */
    public CSPReceiver() {}

    /** Construct a CSPReceiver with the specified container.
     *  @param container The port containing this receiver.
     */
    public CSPReceiver(IOPort container) {
        _container = container;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Retrieve a Token from the receiver by rendezvous. This method
     *  does not return until the rendezvous has been completed.
     *  If a put has already been reached, it notifies the waiting put
     *  and waits for the rendezvous to complete. When the rendezvous is
     *  complete it returns with the token.
     *  If a put has not yet been reached, the method delays until a
     *  put is reached.
     *  It is assumed that at most one process is trying to receive
     *  from and send to the channel associated with this receiver.
     *
     *  @return The token transferred by the rendezvous.
     *  @exception TerminateProcessException If execution termination
     *   has been requested, or if the execution is abruptly terminated
     *   from the outside (via an InterruptedException).
     */
    public synchronized Token get() {
        Token tmp = null;
        boolean blocked = false;
        try {
            if (_isPutWaiting()) {
                _setPutWaiting(false);  //needs to be done here
                tmp = _token;
                _setRendezvousComplete(false);
                notifyAll(); //wake up the waiting put;
                while (!_isRendezvousComplete()) {
                    _checkFlagsAndWait();
                }
            } else {
                // get got there first, so have to wait for a put;
                _setGetWaiting(true);
                notifyAll();

                // This is needed for the case when a condSend reaches
                // the receiver before a get. When the condSend continues,
                // it resets the condSendWaiting flag and does a put()
                // which sets the getWaiting flag to
                // false and the rendezvous proceeds normally.
                while (_isConditionalSendWaiting()) {
                    _checkFlagsAndWait();
                }

                
        	_checkFlags();
                _getDirector()._actorReadBlocked(true);
                blocked = true;
                while (_isGetWaiting()) {
                    _checkFlagsAndWait();
                }
        	_checkFlags();
                _getDirector()._actorReadUnBlocked(true);
                blocked = false;
                tmp = _token;
                _setRendezvousComplete(true);
                notifyAll();
            }
        } catch (InterruptedException ex) {
            throw new TerminateProcessException(
                    "CSPReceiver.get() interrupted.");
        } finally {
            if (blocked) {
                // process was blocked, woken up and terminated.
                // register process as being unblocked
                _getDirector()._actorReadUnBlocked(true);
                // _getDirector()._actorUnblocked();
            }
        }
        return tmp;
    }

    /** Return the IOPort containing this receiver.
     *  @return The port to which this receiver is attached.
     */
    public IOPort getContainer() {
        return _container;
    }

    /** True if a get is waiting to rendezvous.
     *  @return True if a get is waiting to rendezvous.
     */
    public synchronized boolean hasRoom() {
        return _isGetWaiting();
    }

    /** True if a put is waiting to rendezvous.
     *  @return True if a put is waiting to rendezvous.
     */
    public synchronized boolean hasToken() {
        return _isPutWaiting();
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is contained
     *  by a composite actor. If this receiver is connected to the inside
     *  of a boundary port, then return true; otherwise return false.
     *  This method is not synchronized so the caller should be.
     * @return True if this receiver is connected to the inside of a
     *  boundary port; return false otherwise.
     */
     public boolean isConnectedToBoundary() {
         if( _connectedBoundaryCacheIsOn ) {
             return _isConnectedBoundaryValue;
         } else {
             IOPort innerPort = (IOPort)getContainer();
             if( innerPort == null ) {
                 _connectedBoundaryCacheIsOn = false;
                 _isConnectedBoundaryValue = false;
                 return _isConnectedBoundaryValue;
             }
             ComponentEntity innerEntity =
                     (ComponentEntity)innerPort.getContainer();
             Port outerPort = null;
             ComponentEntity outerEntity = null;
             List portList = innerPort.connectedPortList();
             Iterator ports = portList.iterator();

             try {
                 while( ports.hasNext() ) {
                     outerPort = (Port)ports.next();
                     outerEntity = (ComponentEntity)outerPort.getContainer();
                     if( outerEntity == innerEntity.getContainer() ) {
                         // The port container of this receiver is
                         // connected to a boundary port. Now determine
                         // if this receiver's channel is connected to
                         // a boundary port.
                         Receiver[][] rcvrs =
                        	 ((IOPort)outerPort).deepGetReceivers();
		     	 for( int i = 0; i < rcvrs.length; i++ ) {
                             for( int j = 0; j < rcvrs[i].length; j++ ) {
                                 if( this == rcvrs[i][j] ) {
                                     _connectedBoundaryCacheIsOn = true;
                                     _isConnectedBoundaryValue = true;
                                     return true;
                                 }
                             }
                         }
                     }
                 }
             }  catch (IllegalActionException ex) {
                 throw new InternalErrorException("No director? " + ex);
             }
             _connectedBoundaryCacheIsOn = true;
             _isConnectedBoundaryValue = false;
             return _isConnectedBoundaryValue;
         }
     }

    /** Return true if this receiver is contained on the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the inside of a boundary port then return true; otherwise
     *  return false. This method is not synchronized so the caller
     *  should be.
     * @return True if this receiver is contained on the inside of
     *  a boundary port; return false otherwise.
     */
     public boolean isInsideBoundary() {
         if( _insideBoundaryCacheIsOn ) {
             return _isInsideBoundaryValue;
         } else {
             IOPort innerPort = (IOPort)getContainer();
             if( innerPort == null ) {
                 _insideBoundaryCacheIsOn = false;
                 _isInsideBoundaryValue = false;
                 return _isInsideBoundaryValue;
             }
             ComponentEntity innerEntity =
             	     (ComponentEntity)innerPort.getContainer();
             if( !innerEntity.isAtomic() && innerPort.isOpaque() ) {
                 // This receiver is contained by the port
                 // of a composite actor.
                 if( innerPort.isOutput() && !innerPort.isInput() ) {
                     _isInsideBoundaryValue = true;
                 } else if( !innerPort.isOutput() && innerPort.isInput() ) {
                     _isInsideBoundaryValue = false;
                 } else if( !innerPort.isOutput() && !innerPort.isInput() ) {
                     _isInsideBoundaryValue = false;
                 } else {
                     // CONCERN: The following only works if the port
                     // is not both an input and output.
                     throw new IllegalArgumentException("A port that "
                             + "is both an input and output can not be "
                             + "properly dealt with by "
                             + "CSPReceiver.isInsideBoundary");
                 }
                 _insideBoundaryCacheIsOn = true;
                 return _isInsideBoundaryValue;
             }
             _insideBoundaryCacheIsOn = true;
             _isInsideBoundaryValue = false;
             return _isInsideBoundaryValue;
         }
     }

    /** Return true if this receiver is contained on the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the outside of a boundary port then return true; otherwise
     *  return false. This method is not synchronized so the caller
     *  should be.
     * @return True if this receiver is contained on the outside of
     *  a boundary port; return false otherwise.
     */
     public boolean isOutsideBoundary() {
         if( _outsideBoundaryCacheIsOn ) {
             return _isInsideBoundaryValue;
         } else {
             IOPort innerPort = (IOPort)getContainer();
             if( innerPort == null ) {
                 _outsideBoundaryCacheIsOn = false;
                 _isOutsideBoundaryValue = false;
                 return _isOutsideBoundaryValue;
             }
             ComponentEntity innerEntity =
                     (ComponentEntity)innerPort.getContainer();
             if( !innerEntity.isAtomic() && innerPort.isOpaque() ) {
                 // This receiver is contained by the port
                 // of a composite actor.
                 if( innerPort.isOutput() && !innerPort.isInput() ) {
                     _isOutsideBoundaryValue = false;
                 } else if( !innerPort.isOutput() && innerPort.isInput() ) {
                     _isOutsideBoundaryValue = true;
                 } else if( !innerPort.isOutput() && !innerPort.isInput() ) {
                     _isOutsideBoundaryValue = false;
                 } else {
                     // CONCERN: The following only works if the port
                     // is not both an input and output.
                     throw new IllegalArgumentException("A port that "
                             + "is both an input and output can not be "
                             + "properly dealt with by "
                             + "CSPReceiver.isInsideBoundary");
                 }
                 _outsideBoundaryCacheIsOn = true;
                 return _isOutsideBoundaryValue;
             }
             _outsideBoundaryCacheIsOn = true;
             _isOutsideBoundaryValue = false;
             return _isOutsideBoundaryValue;
         }
     }

    /** Place a Token into the receiver via rendezvous. This method
     *  does not return until the rendezvous has been completed.
     *  If get has already been reached, it notifies the waiting get
     *  and waits for the rendezvous to complete. When the rendezvous is
     *  complete it returns.
     *  If a get has not yet been reached, the method delays until a
     *  get is reached.
     *  It is assumed that at most one process is trying to receive
     *  from and send to the channel associated with this receiver.
     *  to receive from it and at most one channel send to it.
     *
     *  @param t The token being transferred in the rendezvous.
     *  @exception TerminateProcessException If execution termination
     *   has been requested, or if the execution is abruptly terminated
     *   from the outside (via an InterruptedException).
     */
    public synchronized void put(Token t) {
        boolean blocked = false;
        try {
            _token = t; // perform transfer
            if (_isGetWaiting()) {
                _setGetWaiting(false);  //needs to be done here
                _setRendezvousComplete(false);
                notifyAll(); //wake up the waiting get
                while (!_isRendezvousComplete()) {
                    _checkFlagsAndWait();
                }
                return;
            } else {
                // put got there first, so have to wait for a get
                _setPutWaiting(true);
                notifyAll();

                // This is needed for the case when a condRec reaches
                // the receiver before a put. When the condRec continues,
                // it resets the condRecWaiting flag and does a get()
                // which sets the putWaiting flag to
                // false and the rendezvous proceeds normally.
                while (_isConditionalReceiveWaiting()) {
                    _checkFlagsAndWait();
                }

        	_checkFlags();
                _getDirector()._actorWriteBlocked();
                blocked = true;
                while(_isPutWaiting()) {
                    _checkFlagsAndWait();
                }
        	_checkFlags();
                _getDirector()._actorWriteUnBlocked();
                blocked = false;
                _setRendezvousComplete(true);
                notifyAll();
                return;
            }
        } catch (InterruptedException ex) {
            throw new TerminateProcessException(
                    "CSPReceiver.put() interrupted.");
        } finally {
            if (blocked) {
                // process was blocked, awakened and terminated.
                // register process as being unblocked
                _getDirector()._actorWriteUnBlocked();
                // _getDirector()._actorUnblocked();
            }
        }
    }

    /** The model has finished executing, so set a flag so that the
     *  next time an actor tries to get or put it gets a
     *  TerminateProcessException which will cause it to finish.
     */
    public synchronized void requestFinish() {
        _modelFinished = true;
        // Need to reset the state of the receiver.
        _setConditionalReceive(false, null);
        _setConditionalSend(false, null);
        _setPutWaiting(false);
        _setGetWaiting(false);
        _setRendezvousComplete(false);
    }

    /** Reset local flags.
     */
    public void reset() {
	_getWaiting = false;
	_putWaiting = false;
	_conditionalReceiveWaiting = false;
        _conditionalSendWaiting = false;
	_rendezvousComplete = false;
	_modelFinished = false;
    	_insideBoundaryCacheIsOn = false;
    	_isInsideBoundaryValue = false;
    	_outsideBoundaryCacheIsOn = false;
    	_isOutsideBoundaryValue = false;
    	_connectedBoundaryCacheIsOn = false;
    	_isConnectedBoundaryValue = false;
    }

    /** Set the container of this CSPReceiver to the specified IOPort.
     *  @param parent The IOPort this receiver is to be contained by.
     */
    public void setContainer(IOPort parent) {
        _container = parent;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** This method wraps the wait() call between checks on the state
     *  of the receiver. The flags checked are whether the receiver
     *  has been finished. The actions taken depending on the flags 
     *  apply to whatever process this method was invoked from.
     *  Note: It should only be called from CSPReceiver and conditional
     *  rendezvous branches, and then only from code that already has
     *  the lock on this receiver.
     *  <p>
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e it was not allowed to run to completion.
     *  @exception InterruptedException If the actor is
     *   interrupted while waiting(for a rendezvous to complete).
     */
    protected synchronized void _checkFlagsAndWait() throws
            TerminateProcessException, InterruptedException {
        _checkFlags();
        wait();
        _checkFlags();
    }

    /** The controller of the conditional branch to reach the
     *  rendezvous point first. For a rendezvous to occur when both
     *  communications at the receiver are from conditional branches,
     *  then the rendezvous can only proceed if <I>both</I> the branches
     *  are the first branches to be ready to succeed for their
     *  respective controllers. This is checked by the second branch to
     *  arrive at the rendezvous point, for which it requires the actor
     *  that created the other branch. Thus the first branch to arrive
     *  stores its controller in the receiver when it is setting the
     *  appropriate flag, which is what is returned by this method.
     *  @return The controller which controls the first conditional
     *   branch to arrive.
     */
    protected ConditionalBranchController _getOtherController() {
        return _otherController;
    }

    /** Flag indicating whether or not a ConditionalReceive is trying
     *  to rendezvous with this receiver.
     *  @return True if a ConditionalReceive branch is trying to
     *   rendezvous with this receiver.
     */
    protected boolean _isConditionalReceiveWaiting() {
        return _conditionalReceiveWaiting;
    }

    /** Flag indicating whether or not a ConditionalSend is trying
     *  to rendezvous with this receiver.
     *  @return True if a ConditionalSend branch is trying to
     *   rendezvous with this receiver.
     */
    protected boolean _isConditionalSendWaiting() {
        return _conditionalSendWaiting;
    }

    /** Flag indicating whether or not a get() is waiting to rendezvous
     *  at this receiver.
     *  @return True if a get() is waiting to rendezvous.
     */
    protected boolean _isGetWaiting() {
        return _getWaiting;
    }

    /** Flag indicating whether or not a put() is waiting to rendezvous
     *  at this receiver.
     *  @return True if a put() is waiting to rendezvous.
     */
    protected boolean _isPutWaiting() {
        return _putWaiting;
    }

    /** Set a flag so that a ConditionalReceive branch knows whether or
     *  not a ConditionalSend is ready to rendezvous with it.
     *  @param v Boolean indicating whether or not a conditional
     *   send is waiting to rendezvous.
     *  @param p The CSPActor which contains the ConditionalSend
     *   branch that is trying to rendezvous. It is stored in the
     *   receiver so that if a ConditionalReceive arrives, it can easily
     *   check whether the ConditionalSend branch was the first
     *   branch of its conditional construct(CIF or CDO) to succeed.
     */
    protected synchronized void _setConditionalSend(boolean v,
	      ConditionalBranchController p) {
        _conditionalSendWaiting = v;
	_otherController = p;
    }

    /** Set a flag so that a ConditionalSend branch knows whether or
     *  not a ConditionalReceive is ready to rendezvous with it.
     *  @param v Boolean indicating whether or not a conditional
     *   receive is waiting to rendezvous.
     *  @param p The CSPActor which contains the ConditionalReceive
     *   branch that is trying to rendezvous. It is stored in the
     *   receiver so that if a ConditionalSend arrives, it can easily
     *   check whether the ConditionalReceive branch was the first
     *   branch of its conditional construct(CIF or CDO) to succeed.
     */
    protected synchronized void _setConditionalReceive(boolean v,
	      ConditionalBranchController p) {
        _conditionalReceiveWaiting = v;
	_otherController = p;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Check the flags controlling the state of the receiver and
     * hence the actor process trying to rendezvous with it. If the
     * model has finished executing, the _modelFinished flag will
     * have been set and a TerminateProcessException will be thrown
     * causing the actor process to finish.
     * <p>
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e. it was not allowed to run to completion.
     */
    private synchronized void _checkFlags()
            throws TerminateProcessException {
        if (_modelFinished) {
            throw new TerminateProcessException(getContainer().getName() +
                    ": terminated.");
        }
    }

    /* Return the director that is controlling the execution of this model.
     * The director is cached as it is accessed often.
     * @return The CSPDirector controlling this model.
     */
    private CSPDirector _getDirector() {
        try {
            Actor cont = (Actor)getContainer().getContainer();
	    if (cont instanceof CompositeActor) {
		return  (CSPDirector)cont.getExecutiveDirector();
	    } else {
		return  (CSPDirector)cont.getDirector();
	    }
        } catch (NullPointerException ex) {
            // If a thread has a reference to a receiver with no director it
            // is an error so terminate the process.
	    throw new TerminateProcessException("CSPReceiver: trying to " +
                    " rendezvous with a receiver with no " +
                    "director => terminate.");
	}
    }

    /*  Flag indicating the state of the rendezvous. It returns false if
     *  one side has tried to rendezvous and the second side has not.
     *  Otherwise it returns true.
     *  @return True if the rendezvous is complete.
     */
    private boolean _isRendezvousComplete() {
        return _rendezvousComplete;
    }

    /* Called only by the get and put methods of this class to indicate
     * that a get is waiting(value is true) or that the corresponding
     * put has arrived(value is false).
     * @param value boolean indicating whether a get is waiting or not.
     */
    private void _setGetWaiting(boolean value) {
        _getWaiting = value;
    }

    /* Called only by the get and put methods of this class to indicate
     * that a put is waiting(value is true) or that the corresponding
     * get has arrived(value is false).
     * @param value boolean indicating whether a put is waiting or not.
     */
    private void _setPutWaiting(boolean value) {
        _putWaiting = value;
    }

    /* Called only by the get and put methods of this class to indicate
     * the state of a rendezvous. The first side of the rendezvous to
     * arrive sets it to false, and waits until the second side sets
     * it to true allowing it to continue.
     * @param value boolean indicating whether a rendezvous is finished
     *  or not.
     */
    private void _setRendezvousComplete(boolean value) {
        _rendezvousComplete = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Container.
    private IOPort _container = null;

    // Flag indicating whether or not a get is waiting at this receiver.
    private boolean _getWaiting = false;

    // Flag indicating whether or not a get is waiting at this receiver.
    private boolean _putWaiting = false;

    // obsolete when implement containment
    private ConditionalBranchController _otherController = null;

    // Flag indicating whether or not a conditional receive is waiting
    // to rendezvous.
    private boolean _conditionalReceiveWaiting = false;

    // Flag indicating whether or not a conditional send is waiting
    // to rendezvous.
    private boolean _conditionalSendWaiting = false;

    // Flag indicating whether state of rendezvous.
    private boolean _rendezvousComplete = false;

    // Flag indicating that any subsequent attempts to rendezvous
    // at this receiver should cause the attempting processes to terminate.
    private boolean _modelFinished = false;

    // The token being transferred during the rendezvous.
    private Token _token;

    private boolean _insideBoundaryCacheIsOn = false;
    private boolean _isInsideBoundaryValue = false;

    private boolean _outsideBoundaryCacheIsOn = false;
    private boolean _isOutsideBoundaryValue = false;

    private boolean _connectedBoundaryCacheIsOn = false;
    private boolean _isConnectedBoundaryValue = false;
}
