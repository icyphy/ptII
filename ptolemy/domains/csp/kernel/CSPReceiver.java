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
import ptolemy.kernel.util.Nameable;

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
  
  /** FIXME: look at CTReceiver, PNReceiver for choice of constructors
   * @param name The name of this receiver.
   */
  public CSPReceiver(String name) {
    _name = name;
  }
  
  public CSPReceiver() {
    _name = "";
  }

  ////////////////////////////////////////////////////////////////////////
  ////                         public methods                         ////
  
  public boolean isConditionalReceiveWaiting() {
    return _conditionalReceiveWaiting;
  }

  public boolean isConditionalSendWaiting() {
    return _conditionalSendWaiting;
  }
  
 /** Retrieve a Token from the receiver. If a put has already been reached, 
   * it notifies the waiting put and returns the Token. If a put has not 
   * yet been reached, the method delays until a put is reached.
   * Currently, each receiver assumes it has at most one channels trying 
   * to receive from it and at most one channel send to it.
   * @return The Token transfered by the rendezvous.
   */
  public synchronized Token get() {   
    Token tmp = _token;
    try {
      if (isPutWaiting()) {
	_setPutWaiting(false);  //needs to be done here
	tmp = _token;
	notifyAll(); //wake up the waiting put
	_checkAndWait();
      } else { // get got there first, so have to wait for a put
	//System.out.println(getName() + ": get got here before put");
	_setGetWaiting(true);
	notifyAll();
	while(isGetWaiting()) {
	  _checkAndWait();
	}
	tmp = _token;
	notifyAll();
      }
    } catch (Exception ex) {
      System.out.println(ex.getClass().getName() + " :" + ex.getMessage());
      /* FIXME */ 
    }
    return tmp;
  }

  /** Place a Token in the receiver. If a get has already been reached, 
   * the Token is transfered and the method returns. If a get has not 
   * yet been reached, the method delays until a get is reached.
   * Currently, each receiver assumes it has at most one channels trying 
   * to receive from it and at most one channel send to it.
   * @param t The token being transfered in the rendezvous.
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
	//System.out.println(getName() + ": put got here before get");
	_setPutWaiting(true);
	notifyAll();
	while(isPutWaiting()) {
	  _checkAndWait();  
	}
	if (isGetWaiting()) {
	  System.out.println("Error:getWaiting is true!");
	}
	notifyAll();
	return;
      }
    } catch (Exception ex) { 
      System.out.println(ex.getClass().getName() + " :" + ex.getMessage());
      // FIXME: what should be done here?
    }
  }
 
  public Nameable getContainer() {
    return null; // FIXME: look at PN code
  }
  
  public String getName() {
    return _name;
  }

  public boolean isGetWaiting() {
    return _getWaiting;
  }

  /** is hasRoom() the same as isGetWaiting()?
   */
  public boolean hasRoom() {
    return false; // perhaps should throw exception as does not belong in CSP
  }
 
  /** is hasToken() the same as putWaiting()?
   */
  public boolean hasToken() {
    return false; // FIXME: perhaps should throw exception?
  }

  /** WILL be obsolete...The parent CSPActor of the conditional branch 
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

  public boolean isPutWaiting() {
    return _putWaiting;
  }
  
  public void setContainer(IOPort parent) {
    //FIXME: look at PN code to see what goes here
  }

 /** Set a flag so that a conditional send branch knows whether or
   *  not a conditional receive is ready to rendezvous with it.
   * @param value boolean indicating whether or not a conditional 
   *  receive is waiting to rendezvous.
   */
  public synchronized void setConditionalReceive(boolean value, CSPActor par) {
    _conditionalReceiveWaiting = value;
    _otherParent = par;
  }

  /** Set a flag so that a conditional receive branch knows whether or
   *  not a conditional send is ready to rendezvous with it.
   * @param value boolean indicating whether or not a conditional 
   *  send is waiting to rendezvous.
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

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////
    
    /** This method wraps the wait() call on the CSPReceiver object 
     *  so that if the simulation has terminated, then a 
     *  TerminateProcessException should be thrown.
     */
    protected synchronized void _checkAndWait() throws InterruptedException {
        if (_simulationTerminated) {
            throw new TerminateProcessException(getName() + ": simulation terminated");
        }
        wait();
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
    
    // Flag indicating whather or not a get is waiting at this receiver.  
    private boolean _getWaiting = false;
    
    // Flag indicating whather or not a get is waiting at this receiver.
    private boolean _putWaiting = false;
    
    // obsolete when implement containment
    private CSPActor _otherParent; 
    
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
    
    // The name of this receiver.
    private String _name;
}  

  

	
