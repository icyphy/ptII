/* A receiver that stores time stamped tokens according to DDE semantics.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (davisj@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;

import java.util.NoSuchElementException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DDEReceiver
/**
A receiver that stores time stamped tokens according to DDE semantics.
A "time stamped token" is a token that has a time stamp associated with it.
An DDEReceiver stores time stamped tokens by enforcing a blocking read and
blocking write style. Time stamped tokens are appended to the queue with
either of the put() methods, both of which block on a write if the queue
is full. Time stamped tokens are removed from the queue via the get()
method that blocks on a read if the queue is empty. If a process blocks on
a read or a write, the director is informed. Blocks are removed (and the
director is informed) if the conditions of the queue contents that led to
blocking no longer exist.
<P>
The key difference between DDEReceiver and TimedQueueReceiver is that 
get() and put() block as described above. In fact, the block mechanism
of DDEReceiver is such that hasToken() blocks as well. If hasToken() is
called while the receiver is empty, then hasToken() will block until a
token is available. 
<P>
This class assumes that valid time stamps have non-negative values. In
other words, simulation time starts at 0.0 or later. At the conclusion 
of a simulation run the receiver time is set to INACTIVE. Prior to the
beginning of a simulation run, the reciever time is set to NOTSTARTED.


@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.TimedQueueReceiver
@see ptolemy.domains.dde.kernel.DDEThread
*/
public class DDEReceiver extends TimedQueueReceiver
        implements ProcessReceiver {

    /** Construct an empty receiver with no container.
     */
    public DDEReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     * @param container The IOPort that contains this receiver.
     */
    public DDEReceiver(IOPort container) {
        super(container);
    }

    /** Construct an empty queue with the specified IOPort container
     *  and priority.
     * @param container The IOPort that contains this receiver.
     * @param priority The priority of this receiver.
     */
    public DDEReceiver(IOPort container, int priority) {
        super(container, priority);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Removed ignored tokens from this receiver. If the receiver
     *  time of this receiver is equal to TimedQueueReceiver.IGNORE,
     *  then take the oldest token off of the queue. If after doing
     *  so no other tokens are on the queue, then set the receiver 
     *  time of this receiver to be equal to the current time of the 
     *  time keeper controlling this receiver. 
     */
    public synchronized void clearIgnoredTokens() {
	// Remove Ignored Token
	super.get();

	// Set the receiver time if queue is empty
	if( (getRcvrTime() == TimedQueueReceiver.IGNORE) 
		&& !super.hasToken() ) {
	    Thread thread = Thread.currentThread();
	    DDEThread ddeThread;
	    if( thread instanceof DDEThread ) {
		ddeThread = (DDEThread)thread;
		TimeKeeper keeper = ddeThread.getTimeKeeper();
		setRcvrTime( keeper.getCurrentTime() );
	    }
	}
    }

    /** Do a blocking read on the queue. If no token is available,
     *  then inform the director that this receiver is blocking on
     *  a read and wait until a token becomes available. When a
     *  token becomes available, determine if this queue has the
     *  unique oldest receiver time with respect to all of the receivers
     *  contained by the actor that contains this receiver; if so,
     *  return the token. If the receiver time is a non-unique minimum 
     *  then determine if this receiver has the highest priority of all
     *  receivers that share the non-unique minimum receiver time and 
     *  if so, return the token. Otherwise throw a NoTokenException. If 
     *  at any point during this method this receiver is scheduled for 
     *  termination, then throw a TerminateProcessException to cease 
     *  execution of the actor that contains this receiver. IMPORTANT: 
     *  This method is designed to be called after hasToken() has been 
     *  called. Verify that this method is safe to call by calling 
     *  hasToken() first.
     * @return Token The oldest token on this queue if this queue has 
     *  the minimum receiver time of all receivers contained by the
     *  actor that contains this receiver.
     * @exception NoTokenException If this method is called while
     *  hasToken() returns false.
     */
    public synchronized Token get() throws NoTokenException {
	if( hasToken() ) {
	    Token token = super.get(); 
	    notifyAll(); 
	    Thread thread = Thread.currentThread();
	    if( thread instanceof DDEThread ) {
		TimeKeeper timeKeeper = 
                        ((DDEThread)thread).getTimeKeeper();
		timeKeeper.sendOutNullTokens();
	    }
	    return token;
	} else {
	    throw new NoTokenException(getContainer(), "No tokens " 
		    + "available in the DDE receiver");
	}
    }

    /** Return true if the get() method of this receiver will return a
     *  token without throwing a NoTokenException. If this receiver has
     *  a receiver time that is not less than or equal to the receiver
     *  time of all receivers contained by the actor that contains this 
     *  receiver then return false. If this receiver has a receiver time 
     *  that is equal to the minimum receiver time of all receivers 
     *  contained by the actor that contains this receiver and at least 
     *  one receiver has a receiver time equal to that of this receiver, 
     *  then return false if this receiver has a lower priority when 
     *  compared to all receivers sharing its receiver time. Otherwise, 
     *  block until this receiver contains a token. If at any point during 
     *  this method this receiver is scheduled for termination, then throw 
     *  a TerminateProcessException to cease execution of the actor that 
     *  contains this receiver.
     * @return Return true if the get() method of this receiver will
     *  return a token without throwing a NoTokenException.
     */
    public boolean hasToken() {
	Workspace workspace = getContainer().workspace();
        DDEDirector director = (DDEDirector)
	        ((Actor)getContainer().getContainer()).getDirector();
	Thread thread = Thread.currentThread();
	if( thread instanceof DDEThread ) {
	    TimeKeeper timeKeeper = ((DDEThread)thread).getTimeKeeper();
	    return _hasToken( workspace, director, timeKeeper );
	}
	return false;
    }

    /** Do a blocking write on the queue. Set the time stamp to be the
     *  current time of the sending actor. If the time stamp of the 
     *  token is greater than the completionTime of this receiver, then 
     *  set the time stamp to INACTIVE and the token to null. If the queue 
     *  is full, then inform the director that this receiver is blocking 
     *  on a write and wait until room becomes available. When room 
     *  becomes available, put the token and time stamp in the queue and 
     *  inform the director that the block no longer exists. If at any 
     *  point during this method this receiver is scheduled for 
     *  termination, then throw a TerminateProcessException which will 
     *  cease activity for the actor that contains this receiver.
     * @param token The token to put on the queue.
     */
    public void put(Token token) {
	Thread thread = Thread.currentThread();
	double time = getLastTime();
	if( thread instanceof DDEThread ) {
	    TimeKeeper timeKeeper = ((DDEThread)thread).getTimeKeeper();
	    time = timeKeeper.getCurrentTime(); 
	    if( time != INACTIVE ) {
                time += timeKeeper.getDelayTime();
	    }
	}

	put( token, time );
    }

    /** Do a blocking write on the queue. If at any point during this
     *  method this receiver is scheduled for termination, then throw
     *  a TerminateProcessException which will cease activity for the
     *  actor that contains this receiver. If the specified time stamp
     *  of the token is greater than the completionTime of this receiver,
     *  then set the time stamp to INACTIVE. If the queue is full, then
     *  inform the director that this receiver is blocking on a write
     *  and wait until room becomes available. When room becomes
     *  available, put the token and time stamp in the queue and inform
     *  the director that the block no longer exists.
     * @param token The token to put on the queue.
     * @param time The time stamp associated with the token.
     */
    public void put(Token token, double time) {
        Workspace workspace = getContainer().workspace();
        DDEDirector director = (DDEDirector)
                ((Actor)getContainer().getContainer()).getDirector();
	Thread thread = Thread.currentThread();

        synchronized(this) {
            if( time > getCompletionTime() &&
                    getCompletionTime() != NOTSTARTED && !_terminate ) {
	        time = INACTIVE;
	    }

            if( super.hasRoom() && !_terminate ) {
                super.put(token, time);
                notifyAll();
                return;
            }

            director.addWriteBlock(this);
            while( !super.hasRoom() && !_terminate ) {
                notifyAll();
                workspace.wait( this );
            }
            if( _terminate ) {
                director.removeWriteBlock(this);
                throw new TerminateProcessException( getContainer(),
                        "This receiver has been terminated "
                        + "during put()");
            } else {
                director.removeWriteBlock(this);
                put(token, time);
            }
        }
    }

    /** Schedule this receiver to terminate. After this method is 
     *  called, a TerminateProcessException will be thrown during
     *  the next call to get() or put(...) of this class.
     */
    public synchronized void requestFinish() {
        _terminate = true;
	notifyAll();
    }

    /** Reset local flags. The local flag of this receiver indicates
     *  whether this receiver is scheduled for termination. Resetting
     *  the termination flag will make sure that this receiver is not
     *  scheduled for termination.
     */
    public void reset() {
	_terminate = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods 		   ////

    // This method provides the recursive 
    // functionality of hasToken()
    private synchronized boolean _hasToken(Workspace workspace,
	    DDEDirector director, TimeKeeper timeKeeper ) {

	timeKeeper.resortRcvrList();

        if( timeKeeper.getNextTime() == INACTIVE ) {
            requestFinish();
        } else if( timeKeeper.getNextTime() == IGNORE ) {
            requestFinish();
	}
	if( getRcvrTime() > timeKeeper.getNextTime() && !_terminate ) {
	    /*
	    System.out.println("Time is not minimum");
	    System.out.println("RcvrTime = " + getRcvrTime() + 
		    "; NextTime = " + timeKeeper.getNextTime() );
	    */
	    return false;
	} else if( !timeKeeper.hasMinRcvrTime() && !_terminate ) {
	    // System.out.println("Time is minimum but not unique");
            if( this != timeKeeper.getHighestPriorityReceiver() ) {
                timeKeeper.updateRcvrList(this);
		return false;
	    }
	}
        if( super.hasToken() && !_terminate ) {
	    // System.out.println("Time is minimum but not unique");
            return true;
	}
	director.addReadBlock();
	while( !super.hasToken() && !_terminate ) {
	    notifyAll();
            workspace.wait( this );
	}
	if( _terminate ) {
	    director.removeReadBlock();
            throw new TerminateProcessException( getContainer(),
                    "This receiver has been terminated during "
                    + "_hasToken()");
	} else {
            director.removeReadBlock();
            return _hasToken(workspace, director, timeKeeper);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _terminate = false; 

}



