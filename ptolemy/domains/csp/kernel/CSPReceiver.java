/* Receiver for CSP style communication.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating none

*/

package ptolemy.domains.csp.kernel;

import ptolemy.actor.*;
import ptolemy.data.Token;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// CSPReceiver
/**
Receiver for CSP style communication. For rendezvous, the receiver is the key
synchronization point. It is assumed each receiver has at most one
thread trying to send to it and at most one thread trying to receive
from it at any one time. The receiver performs the synchronization
necessary for simple rendezvous (get() and put() operations). It
also stores the flags that allow conditionalSends and conditionalReceives
to know when they can proceed.
<p>
FIXME: If more than receiver or sender were allowed, what would this mean?
Is the synchronization below provable? Or is it just reasoned?

@author Neil Smyth
@version $Id$

*/

public class CSPReceiver implements ProcessReceiver {

    /** Construct a CSPReceiver with no container.
     */
    public CSPReceiver() {}

    /** Construct a CSPReceiver with the specified container.
     *  @param container The container.
     */
    public CSPReceiver(IOPort container) {
        _container = container;
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Retrieve a Token from the receiver. If a put has already been reached,
     *  it notifies the waiting put and returns the Token. If a put has not
     *  yet been reached, the method delays until a put is reached.
     *  Currently, each receiver assumes it has at most one channels trying
     *  to receive from it and at most one channel send to it.
     *  @exception NoTokenException If there is no token, or we are 
     *   trying to to get from a floating(disconnected, legacy reference) 
     *   receiver.
     *  @return The Token transferred by the rendezvous.
     */
    public synchronized Token get() throws NoTokenException {
        Token tmp = _token;
        try {
            if (isPutWaiting()) {
                _setPutWaiting(false);  //needs to be done here
                tmp = _token;
                _setPutDone(false);
                notifyAll(); //wake up the waiting put;
                while (!isPutDone()) {
                    _checkAndWait();
                }
            } else {
                // get got there first, so have to wait for a put;
                // System.out.println(Thread.currentThread().getName() + ": " + getContainer().getName() + ": get got here before put");
                _setGetWaiting(true);
                notifyAll();

                // This is needed for the case when a condSend reaches 
                // the receiver before a get. When the condSend continues, 
                // it resets the condSend flag, the getWaiting flag is set to 
                // false and the rendezvous proceeds normally.
                while (isConditionalSendWaiting()) {
                    _checkAndWait();
                }

                _registerBlocked();
                while (isGetWaiting()) {
                    _checkAndWait();
                }
                _registerUnblocked();
                tmp = _token;
                _setGetDone(true);
                notifyAll();
            }
        } catch (InterruptedException ex) {
            System.out.println("get interrupted: " + ex.getMessage());
            /* FIXME */
        }
        return tmp;
    }

    /** Place a Token in the receiver. If a get has already been reached,
     *  the Token is transferred and the method returns. If a get has not
     *  yet been reached, the method delays until a get is reached.
     *  Currently, each receiver assumes it has at most one channels trying
     *  to receive from it and at most one channel send to it.
     *  @exception NoRoomException If the token cannot be put, mainly 
     *   if this receiver does not have a container. FIXME: this 
     *   comment is bogus.
     *  @param t The token being transferred in the rendezvous.
     */
    public synchronized void put(Token t) {
        try {
            _token = t; // perform transfer
            if (isGetWaiting()) {
                _setGetWaiting(false);  //needs to be done here
                _setGetDone(false);
                notifyAll(); //wake up the waiting get
                while (!isGetDone()) {
                    _checkAndWait();
                }
                return;
            } else { 
                // put got there first, so have to wait for a get
                // System.out.println(Thread.currentThread().getName() + ": " + getContainer().getName() + ": put got here before get");
                _setPutWaiting(true);
                notifyAll();

                // This is needed for the case when a condRec reaches 
                // the receiver before a put. When the condRec continues, 
                // it resets the condRec flag, the putWaiting flag is set to 
                // false and the rendezvous proceeds normally.
                while (isConditionalReceiveWaiting()) {
                    _checkAndWait();
                }

                _registerBlocked();
                while(isPutWaiting()) {
                    _checkAndWait();
                }
                _registerUnblocked();
                _setPutDone(true);
                notifyAll();
                return;
            }
        } catch (InterruptedException ex) {
            System.out.println("put interrupted :" + ex.getMessage());
            // FIXME: what should be done here?
        }
    }

    /** Return the IOPort containing this recciever.
     *  @return The port to which this receiver is atteached.
     */
    public IOPort getContainer() {
        return _container;
    }

    /** FIXME: is hasRoom() the same as isGetWaiting()?
     */
    public boolean hasRoom() {
        return false;
    }

    /** FIXME: is hasToken() the same as putWaiting()?
     */
    public boolean hasToken() {
        return false;
    }

    /** May be obsolete...The parent CSPActor of the conditional branch
     * to reach the
     * rendezvous point first. It is needed in the conditionalBranch
     * that arrives second to check if both branches are the first to
     * succeed for both parent actors.
     * @return The parent actor which created the first conditional
     *   branch to arrive.
     */
    public CSPActor getOtherParent() {
        return _otherParent;
    }

    /** Flag indicating whether or not a conditionalReceive is trying
     *  to rendezvous with this receiver.
     *  @return Flag indicating if a conditionalReceive branch is
     *   trying to rendezvous with this receiver.
     */
    public boolean isConditionalReceiveWaiting() {
        return _conditionalReceiveWaiting;
    }

    /** Flag indicating whether or not a conditionalSend is trying
     *  to rendezvous with this receiver.
     *  @return Flag indicating if a conditionalSend branch is
     *   trying to rendezvous with this receiver.
     */
    public boolean isConditionalSendWaiting() {
        return _conditionalSendWaiting;
    }

    /** Flag indicating whether or not a get, which reached the 
     *  rendezvous point first, is done yet.
     *  @return Flag indicating if first side of rendezvous is done yet.
     */
    public boolean isGetDone() {
        return _getDone;
    }

    /** Flag indicating whether or not a put, which reached the 
     *  rendezvous point first, is done yet.
     *  @return Flag indicating if first side of rendezvous is done yet.
     */
    public boolean isPutDone() {
        return _putDone;
    }

    /** Flag indicating whether or not a get is waiting to rendezvous
     *  at this receiver.
     *  @return Flag indicating if  get is waiting to rendezvous.
     */
    public boolean isGetWaiting() {
        return _getWaiting;
    }

    /** Flag indicating whether or not a put is waiting to rendezvous
     *  at this receiver.
     *  @return Flag indicating if  put is waiting to rendezvous.
     */
    public boolean isPutWaiting() {
        return _putWaiting;
    }

    /** Set the container of this CSPReceiver to the specified IOPort.
     *  FIXME: a null argument should remove it from the IOPort it
     *  currently belongs to.
     *  @param parent The IOPort this receiver is to be contained by.
     */
    public void setContainer(IOPort parent) {
        _container = parent;
    }

    /** Set a flag so that a conditional send branch knows whether or
     *  not a conditional receive is ready to rendezvous with it.
     *  @param val boolean indicating whether or not a conditional
     *   receive is waiting to rendezvous.
     *  @param par The CSPActor which contains the ConditionalReceive
     *   branch that is trying to rendezvous. It is stored in the
     *   receiver so that if a ConditionalSend arrives, it can easily
     *   check whether the ConditionalReceive branch was the first
     *   branch of its conditional construct(CIF or CDO) to succeed.
     */
    public synchronized void setConditionalReceive(boolean val, CSPActor par) {
        _conditionalReceiveWaiting = val;
	_otherParent = par;
    }

    /** Set a flag so that a conditional receive branch knows whether or
     *  not a conditional send is ready to rendezvous with it.
     *  @param value boolean indicating whether or not a conditional
     *   send is waiting to rendezvous.
     *  @param par The CSPActor which contains the ConditionalSend
     *   branch that is trying to rendezvous. It is stored in the
     *   receiver so that if a ConditionalReceive arrives, it can easily
     *   check whether the ConditionalSend branch was the first
     *   branch of its conditional construct(CIF or CDO) to succeed.
     */
    public synchronized void setConditionalSend(boolean value, CSPActor par) {
        _conditionalSendWaiting = value;
	_otherParent = par;
    }

    /** The simulation has been paused, so set a flag so that the
     *  next time an actor tries to get or put it knows to pause.
     *  @param value The new value of the paused flag.
     */
    public synchronized void setPause(boolean value) {
        _simulationPaused = value;
    }
    /** The simulation has finished, so set a flag so that the
     *  next time an actor tries to get or put it gets a
     *  TerminateProcessException which will cause it to finish.
     */
    public synchronized void setFinish() {
        System.out.println(getContainer().getName() + ": receiver finished.");
        _simulationFinished = true;
        _simulationPaused = false; // needed?
    }

    /** Returns a String description of this CSPReceiver.
     *  FIXME: not finished, but good for now.
     *  @retun String description of this CSPReceiver.
     */
    public String toString() {
        if (_container != null) {
	    return "CSPReceiver: container is " + getContainer().getName();
	}
	return "CSPReceiver: no container.";
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////
    /** This method wraps the wait() call on the CSPReceiver object
     *  so that if the simulation has terminated, then a
     *  TerminateProcessException should be thrown.
     *  Note: It should only be called from CSPReceiver and conditional
     *  rendezvous branches, and then only from code that already has
     *  the lock on this receiver.
     *  <p>
     *  FIXME: will duplication always be needed below?
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e it was not allowed to run to completion.
     *  @exception InterruptedException If the actor is
     *   interrupted while waiting.
     */
    protected synchronized void _checkAndWait() throws
              TerminateProcessException, InterruptedException {
        _checkFlags();
        wait();
        _checkFlags();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    /* Check the flags controlling the state of the receiver and 
     * hence the actor process trying to rendezvous with it. If the 
     * simulation has been finished the _simulationFinished flag will 
     * have been set and a TerminateProcessException will be thrown 
     * which will cause the actor process to finish.
     * <p>
     * If the simulation has been paused, register the the current 
     * thread as being paused with director, and after the pause 
     * reset the simulationPaused flag.
     *  @exception TerminateProcessException If the actor to
     *   which this receiver belongs has been terminated while still
     *   running i.e. it was not allowed to run to completion.
     *  @exception InterruptedException If the thread is
     *   interrupted while waiting.
     */
    private synchronized void _checkFlags() throws InterruptedException {
        if (_simulationFinished) {
            throw new TerminateProcessException(getContainer().getName() + 
                    ": terminated1");
        } else if (_simulationPaused) {
            _getDirector().increasePausedCount();
            while (_simulationPaused) {
                wait();
            }
            // The simulation may have ended while we were paused...
            // Need to do this as wait is used above.
            if (_simulationFinished) {
                throw new TerminateProcessException(getContainer().getName() + 
                        ": terminated1");
            }
        }
    }

    /* Return the director that is controlling this simulation.
     *  The director is cached as it is accessed often.
     *  @return The CSPDirector controlling this simulation.
     */
    private CSPDirector _getDirector() {
        if (getContainer() == null) {
            // If a thread has a reference to a receiver with no container it 
            // is an error so terminate the process.
	    throw new TerminateProcessException("CSPReceiver: trying to " +
                    " rendezvous with a receiver with no " +
                    "container => terminate.");
	}
        long workversion = ((NamedObj)getContainer()).workspace().getVersion();
	if ((_director == null) || (_directorVersion != workversion)) {
	    _directorVersion = workversion;
	    Actor cont = (Actor)getContainer().getContainer();
	    _director = (CSPDirector)cont.getDirector();
	}
	return _director;
    }

    /* Register with the director that an actor has blocked while
     * trying to rendezvous at this receiver.
     *  @exception InterruptedException If the thread is
     *   interrupted while waiting.
     */
    private void _registerBlocked() throws InterruptedException {
        _checkFlags();
        _getDirector().actorBlocked();
    }

    /* Register with the director that an actor has unblocked after
     * rendezvousing at this receiver.
     *  @exception InterruptedException If the thread is
     *   interrupted while waiting.
     */
    private void _registerUnblocked() throws InterruptedException {
        _checkFlags();
        _getDirector().actorUnblocked();
    }

    /* Called only by the get and put methods of this class to indicate
     * that a get is completed after the corresponding
     * put arrived.
     * @param value boolean indicating whether a get is finished or not.
     */
    private void _setGetDone(boolean value) {
        System.out.println(Thread.currentThread().getName() + ": setting get done flag to " + value);
        _getDone = value;
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
     * that a put is completed after the corresponding
     * get arrived.
     * @param value boolean indicating whether a put is finished or not.
     */
    private void _setPutDone(boolean value) {
        System.out.println(Thread.currentThread().getName() + ": setting put done flag to " + value);
        _putDone = value;
    }

   /* Called only by the get and put methods of this class to indicate
     * that a put is waiting(value is true) or that the corresponding
     * get has arrived(value is false).
     * @param value boolean indicating whether a put is waiting or not.
     */
    private void _setPutWaiting(boolean value) {
        _putWaiting = value;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////


    // Container.
    private IOPort _container = null;
    
    // The director controlling this simulation, and its version.
    private CSPDirector _director;
    private long _directorVersion = -1;

    // Flag indicating whether get is done yet.
    private boolean _getDone = true;

    // Flag indicating whether put is done yet.
    private boolean _putDone = true;
  
    // Flag indicating whether or not a get is waiting at this receiver.
    private boolean _getWaiting = false;

    // Flag indicating whether or not a get is waiting at this receiver.
    private boolean _putWaiting = false;

    // obsolete when implement containment
    private CSPActor _otherParent = null;

    // Flag indicating whether or not a conditional receive is waiting
    // to rendezvous.
    private boolean _conditionalReceiveWaiting = false;

    // Flag indicating whether or not a conditional send is waiting
    // to rendezvous.
    private boolean _conditionalSendWaiting = false;

    // Flag indicating that the director controlling the actor this
    //receiver is contained by has terminated the simulation.
    private boolean _simulationFinished = false;

    // Flag indicating that the director controlling the actor this
    // receiver is contained by has been paused.
    private boolean _simulationPaused = false;

    // The token being transferred during the rendezvous.
    private Token _token;

    // Flags for controlling whether or not the receiver should register 
    // with the director as being block waiting to rendezvous.
    private boolean _registerBlock = false;
    private boolean _registerUnblock = false;
    private boolean _blockRegistered = false;

}




