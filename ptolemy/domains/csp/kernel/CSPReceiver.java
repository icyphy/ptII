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
neccessary for simple rendezvous (get() and put() operations). It 
also stores the flags that allow conditionalSends and conditionalReceives 
to know when they can proceed.
<p>
FIXME: If more than receiver or sender were alowed, what would this mean?
Is the synchronization below provable? Or is it just reasoned?

@author Neil Smyth
@version $Id$

*/

public class CSPReceiver implements Receiver {
  
    /** Construct a CSPReceiver with no container.
     */
    public CSPReceiver() {}
    
    /** Construct a CSPReceiver with the specified container.
     *  @param container The container.
     */
    public CSPReceiver(IOPort container) {
        _container = container;
        _firstContainer = false;
    }
    
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    
    /** Retrieve a Token from the receiver. If a put has already been reached, 
     *  it notifies the waiting put and returns the Token. If a put has not 
     *  yet been reached, the method delays until a put is reached.
     *  Currently, each receiver assumes it has at most one channels trying 
     *  to receive from it and at most one channel send to it.
     *  @exception NoSuchItemException Thrown if this receiver does 
     *   not have a container. It is not possible to get from a 
     *   floating(disconnected, legacy reference) receiver.
     *  @return The Token transfered by the rendezvous.
     */
    public synchronized Token get() throws NoSuchItemException {   
        Token tmp = _token;
        try {
            if (isPutWaiting()) {
                _setPutWaiting(false);  //needs to be done here
                tmp = _token;
                notifyAll(); //wake up the waiting put
                _checkAndWait();
            } else { // get got there first, so have to wait for a put
                //System.out.println(getContainer().getName() + ": get got here before put");
                _setGetWaiting(true);
                notifyAll();
                _getDirector().actorBlocked();
                while(isGetWaiting()) {
                    _checkAndWait();
                }
                _getDirector().actorUnblocked();
                tmp = _token;
                notifyAll();
            }
        } catch (InterruptedException ex) {
            System.out.println("get interrupted: " + ex.getMessage());
            /* FIXME */ 
        }
        return tmp;
    }
    
    /** Place a Token in the receiver. If a get has already been reached, 
     *  the Token is transfered and the method returns. If a get has not 
     *  yet been reached, the method delays until a get is reached.
     *  Currently, each receiver assumes it has at most one channels trying 
     *  to receive from it and at most one channel send to it.
     *  @param t The token being transfered in the rendezvous.
     */
    /*  /@exception NoSuchItemException Thrown if this receiver does 
     *   not have a container. It is not possible to put to a 
     *   floating(disconnected, legacy reference) receiver.
     */
    public synchronized void put(Token t) {
        if (isPutWaiting()) {System.out.println("ERROR:put true");}
        try {
            _token = t; // perform transfer
            if (isGetWaiting()) {
                _setGetWaiting(false);  //needs to be done here
                notifyAll(); //wake up the waiting get
                _checkAndWait();
                return;
            } else { // put got there first, so have to wait for a get
                //System.out.println(getContainer().getName() + ": put got here before get");
                _setPutWaiting(true);
                notifyAll();
                _getDirector().actorBlocked();
                while(isPutWaiting()) {
                    _checkAndWait();  
                }
                _getDirector().actorUnblocked();
                if (isGetWaiting()) {
                    System.out.println("Error:getWaiting is true!");
                }
                notifyAll();
                return;
            }
        } catch (InterruptedException ex) { 
            System.out.println("put interrupted :" + ex.getMessage());
            // FIXME: what should be done here?
        } catch (NoSuchItemException ex) {
            //FIXME: this should not be caught here, should be in signature of put 
            System.out.println("ERROR in CSPReceiver.");
        }
    }
    
    /** Return the container.
     *  @return The container.
     */
    public Nameable getContainer() {
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
     * that arives second to check if both branches are the first to 
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
     *  A receiver can only ever have one container. If the argument is 
     *  null, this CSPReceiver is removed from the list of receivers 
     *  in its container. 
     *  FIXME: should this method be write synchronized on the workspace?
     *  FIXME: a null argument should remove it from the IOPort it 
     *  currently belongs to.
     *  @exception IllegalActionException Thrown if this receiver has 
     *   already benn placed into an IOPort. A receiver can only ever 
     *   be contained by ne IOPort during its life.
     *  @param parent The IOPort this receiver is to be contained by.
     */
    public void setContainer(IOPort parent) throws IllegalActionException {
        if (parent == null) {
            _container = null;
            // FIXME: remove receiver from list of IOPorts receivers
        }
        if (_firstContainer) {
            _container = parent;
            _firstContainer = false;
        } else {
            String str = "CSPReceiver: A Receiver can only ever";
            throw new IllegalActionException(str + " have one container");
        }
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
    
    /** The simulation has terminated, so set a flag so that the 
     *  next time an actor tries to get or put it gets a 
     *  TerminateProcessException which will cause it to finish.
     */
    public void setSimulationTerminated() {
        _simulationTerminated = true;
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
     *  @exception TerminateProcessException Thrown if the actor to 
     *   which this receiver belongs has been terminated while still 
     *   running i.e it was not allowed to run to completion.
     *  @exception InterruptedException Thrown if the actor is 
     *   interrupted while waiting.
     */
    protected synchronized void _checkAndWait() throws 
      TerminateProcessException, InterruptedException {
	if (_simulationTerminated) {
            throw new TerminateProcessException(getContainer().getName() + ": simulation terminated");
        }
        wait();
	if (_simulationTerminated) {
            throw new TerminateProcessException(getContainer().getName() + ": simulation terminated");
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////
    
    /** Return the director that is controlling this simulation.
     *  The director is cached as it is accessed often.
     *  @exception NoSuchItemException Thrown if this receiver does 
     *   not have a container. In order to get or put to a CSPReceiver, 
     *   it must have a container. If it does not it indicates that 
     *   either the receiver was removed (container set to null) or 
     *   it has not been placed in an IOPort yet.
     *  @return The CSPDirector controlling this simulation.
     */
    private CSPDirector _getDirector() throws NoSuchItemException{
        if (getContainer() == null) {
	    throw new NoSuchItemException("CSPReceiver: needs a container");
	}
        long workversion = ((NamedObj)getContainer()).workspace().getVersion();
	if ((_director == null) || (_directorVersion != workversion)) {
	    _directorVersion = workversion;
	    Actor cont = (Actor)getContainer().getContainer();
	    _director = (CSPDirector)cont.getDirector();
	}
	return _director;
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
    
    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////
    
    
    // Container is not changeable.
    private IOPort _container = null;
    private boolean _firstContainer = true;

    // The director controlling this simulation, and its version.
    private CSPDirector _director;
    private long _directorVersion = -1;

    // Flag indicating whather or not a get is waiting at this receiver.  
    private boolean _getWaiting = false;
    
    // Flag indicating whather or not a get is waiting at this receiver.
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
    private boolean _simulationTerminated = false;
    
    // The token being transfered during the rendezvous.
    private Token _token;
    
}  

  

	
