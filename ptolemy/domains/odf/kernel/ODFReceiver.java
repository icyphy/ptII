/* A receiver that stores time stamped tokens according to ODF semantics.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.odf.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;

import java.util.NoSuchElementException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ODFReceiver
/**
A receiver that stores time stamped tokens according to ODF semantics.
A "time stamped token" is a token that has a time stamp associated with it.
An ODFReceiver stores time stamped tokens by enforcing a blocking read and
blocking write format. Time stamped tokens are appended to the queue with
either of the put() methods, both of which block on a write if the queue
is full. Time stamped tokens are removed from the queue via the get()
method that blocks on a read if the queue is empty. If a process blocks on
a read or a write, the director is informed. Blocks are removed (and the
director is informed) if the conditions of the queue contents that led to
blocking no longer apply.
<P>
Since ODFReceiver is derived from TimedQueueReceiver, it inherits both the
lastTime and rcvrTime flags. ODFReceiver sets these flags in a manner
similar to TimedQueueReceiver. The key difference between ODFReceiver and
TimedQueueReceiver is that get() and put() block as described above.
<P>
This class assumes that valid time stamps have non-negative values. A
time stamp value of -1.0 is reserved to indicate the termination of a
receiver. A time stamp value of -5.0 is reserved to indicate that a
receiver has not begun to participate in a model's execution.


@author John S. Davis II
@version $Id$
@see ptolemy.domains.odf.kernel.TimedQueueReceiver
@see ptolemy.domains.odf.kernel.ODFThread
*/
public class ODFReceiver extends TimedQueueReceiver
        implements ProcessReceiver {

    /** Construct an empty receiver with no container.
     */
    public ODFReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     * @param container The IOPort that contains this receiver.
     */
    public ODFReceiver(IOPort container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do a blocking read on the queue. If no token is available,
     *  then inform the director that this receiver is blocking on
     *  a read and wait until a token becomes available. When a
     *  token becomes available, determine if this queue has the
     *  unique oldest rcvrTime with respect to all of the receivers
     *  contained by the actor that contains this receiver; if so,
     *  return the token. If the rcvrTime is a non-unique minimum then
     *  determine if this receiver has the highest priority of all
     *  receivers that share the non-unique minimum rcvrTime and if
     *  so, return the token. Otherwise return false. If at any point
     *  during this method this receiver is scheduled for termination,
     *  then throw a TerminateProcessException to cease execution of
     *  the actor that contains this receiver. IMPORTANT: This method
     *  is designed to be called after hasToken() has been called.
     *  Verify that this method is safe to call by calling hasToken()
     *  first.
     * @return Token The oldest token on this queue if this queue
     *  has the minimum rcvrTime of all receivers contained by the
     *  actor that contains this receiver.
     * @exception NoTokenException If this method is called while
     *  hasToken() returns false.
     */
    public Token get() {
	synchronized(this) {
	    if( hasToken() ) {
		Token token = super.get();
		notifyAll();
		return token;
	    } else {
		throw new NoTokenException(getContainer(), "No tokens "
			+ "available in the ODF receiver");
	    }
	}
    }

    /** Return true if the get() method of this receiver will return a
     *  token without throwing a NoTokenException. If this receiver has
     *  a rcvrTime that is not less than or equal to the rcvrTime of all
     *  receivers contained by the actor that contains this receiver then
     *  return false. If this receiver has a rcvrTime that is equal to
     *  the minimum rcvrTime of all receivers contained by the actor that
     *  contains this receiver and at least one receiver has a rcvrTime
     *  equal to that of this receiver, then return false if this receiver
     *  has a lower priority when compared to all receivers sharing its
     *  rcvrTime. Otherwise, block until this receiver contains a token. If
     *  at any point during this method this receiver is scheduled for
     *  termination, then throw a TerminateProcessException to cease
     *  execution of the actor that contains this receiver.
     * @return Return true if the get() method of this receiver will
     *  return a token without throwing a NoTokenException.
     */
    public boolean hasToken() {
	Workspace workspace = getContainer().workspace();
	// ODFThread thread = getThread();
	TimeKeeper timeKeeper = getReceivingTimeKeeper();
        ODFDirector director = (ODFDirector)
	        ((Actor)getContainer().getContainer()).getDirector();

	return _hasToken( workspace, director, timeKeeper );
    }

    /** FIXME
    public void put(double time) {
	Token token = null;
        put( token, time );
    }
     */

    /** Do a blocking write on the queue. Set the time stamp to the
     *  lastTime of this receiver. If the time stamp of the token is
     *  greater than the completionTime of this receiver, then set the
     *  time stamp to -1.0 and the token to null. If the queue is full,
     *  then inform the director that this receiver is blocking on a
     *  write and wait until room becomes available. When room becomes
     *  available, put the token and time stamp in the queue and inform
     *  the director that the block no longer exists. If at any point
     *  during this method this receiver is scheduled for termination,
     *  then throw a TerminateProcessException which will cease activity
     *  for the actor that contains this receiver.
     * @param token The token to put on the queue.
     */
    public void put(Token token) {
	TimeKeeper timeKeeper = getSendingTimeKeeper();
	double time = timeKeeper.getCurrentTime(); 
	if( time != -1.0 ) {
            time += timeKeeper.getDelayTime();
	}
	put( token, time );
	/* 
	Thread thread = Thread.currentThread();
	if( thread instanceof ODFThread ) {
	    // FIXME: This is an error. It should be _lastTime!!!
	    double time = ((ODFThread)thread).getCurrentTime();
            put( token, time );
	} else {
            put( token, getLastTime() );
	}
	put( token, getLastTime() );
	*/ 
    }

    /** Do a blocking write on the queue. If at any point during this
     *  method this receiver is scheduled for termination, then throw
     *  a TerminateProcessException which will cease activity for the
     *  actor that contains this receiver. If the specified time stamp
     *  of the token is greater than the completionTime of this receiver,
     *  then set the time stamp to -1.0. If the queue is full, then
     *  inform the director that this receiver is blocking on a write
     *  and wait until room becomes available. When room becomes
     *  available, put the token and time stamp in the queue and inform
     *  the director that the block no longer exists.
     * @param token The token to put on the queue.
     * @param time The time stamp associated with the token.
     */
    public void put(Token token, double time) {
        Workspace workspace = getContainer().workspace();
        ODFDirector director = (ODFDirector)
                ((Actor)getContainer().getContainer()).getDirector();
	// Thread thread = Thread.currentThread();
	TimeKeeper timeKeeper = getSendingTimeKeeper();

        synchronized(this) {
            if( time > getCompletionTime() &&
                    getCompletionTime() != -5.0 && !_terminate ) {
	        time = -1.0;
	    }

            if( super.hasRoom() && !_terminate ) {
                super.put(token, time);
                notifyAll();
		timeKeeper.sendOutNullTokens();
		/*
		if( thread instanceof ODFThread ) {
		    ((ODFThread)thread).sendOutNullTokens();
		}
		*/
                return;
            }

            director.addWriteBlock();
            while( !super.hasRoom() && !_terminate ) {
                notifyAll();
                workspace.wait( this );
            }
            if( _terminate ) {
                director.removeWriteBlock();
                throw new TerminateProcessException( getContainer(),
                        "This receiver has been terminated "
                        + "during put()");
            } else {
                director.removeWriteBlock();
                put(token, time);
            }
        }
    }

    /** Schedule this receiver to terminate.
     */
    public synchronized void requestFinish() {
        _terminate = true;
	notifyAll();
    }

    /** Set the pause flag of this receiver.
     * @param flag The boolean pause flag of this receiver.
     */
    public void requestPause(boolean flag) {
        ;
    }

    /** Reset local flags.
     */
    public void reset() {
	_terminate = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods 		   ////

    // This method provides the recursive functionality
    // of hasToken()
    private synchronized boolean _hasToken(Workspace workspace,
	    ODFDirector director, TimeKeeper timeKeeper ) {
	// ODFDirector director, ODFThread thread ) {
        if( timeKeeper.getNextTime() == -1.0 ) {
            requestFinish();
        }
	if( getRcvrTime() > timeKeeper.getNextTime() && !_terminate ) {
	    return false;
	} else if( !timeKeeper.hasMinRcvrTime() && !_terminate ) {
            RcvrTimeTriple triple;
            triple = timeKeeper.getHighestPriorityTriple();
            if( this != triple.getReceiver() ) {
                triple = new RcvrTimeTriple( this, getRcvrTime(),
                        getPriority() );
                timeKeeper.updateRcvrList( triple );
		return false;
	    }
	}
	/*
        if( thread.getNextTime() == -1.0 ) {
            requestFinish();
        }
	if( getRcvrTime() > thread.getNextTime() && !_terminate ) {
	    return false;
	} else if( !thread.hasMinRcvrTime() && !_terminate ) {
            RcvrTimeTriple triple;
            triple = thread.getHighestPriorityTriple();
            if( this != triple.getReceiver() ) {
                triple = new RcvrTimeTriple( this, getRcvrTime(),
                        getPriority() );
                thread.updateRcvrList( triple );
		return false;
	    }
	}
	*/
        if( super.hasToken() && !_terminate ) {
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



