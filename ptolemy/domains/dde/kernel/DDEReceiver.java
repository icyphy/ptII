/* A receiver that stores time stamped tokens according to DDE semantics.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (davisj@eecs.berkeley.edu)
@AcceptedRating Green (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;

import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// DDEReceiver
/**
A DDEReceiver stores time stamped tokens according to distributed
discrete event semantics. A <I>time stamped token</I> is a token
that has a time stamp associated with it. A DDEReceiver stores time
stamped tokens by enforcing a blocking read and blocking write style.
Time stamped tokens are appended to the queue with one of the two put()
methods, both of which block on a write if the queue is full. Time
stamped tokens are removed from the queue via the get() method. The
get() method will throw a NoTokenException if it is invoked when the
hasToken() method returns false.
<P>
Each DDEReceiver is managed by a TimeKeeper. A single time keeper is
assigned to manage all of the receivers of a given actor by keeping
track of the actor's local notion of time. As tokens are consumed
(returned by the get() method) in a receiver, the local time of the
actor will advance to the value of the consumed token's time stamp.
The hasToken() method of a receiver will return true only if the
receiver's get() method will result in the minimum advancement of local
time with respect to all of the receivers controlled by the TimeKeeper.
If the get() method of multiple receivers will result in a minimum
but identical local time advancement, then the hasToken() method of the
receiver with the highest priority will true (the others will return
false).
<P>
If a receiver with a nonnegative receiver time is empty, then the
hasToken() method will perform a blocking read. Once, a token is
available then hasToken() will return true or false according to
the minimum time advancement rules cited in the preceding paragraph.
Note that hasToken() blocks while get() does not block.
<P>
DDEReceivers process certain events that are hidden from view by
ports and actors. In particular, NullTokens and time stamps of
value PrioritizedTimedQueue.IGNORE. NullTokens allow actors to
communicate information on their local time advancement to
neighboring actors without the need for an actual data exchange.
NullTokens are passed at the receiver level and circumvent the
Ptolemy II data typing mechanism.
<P>
Time stamps of value PrioritizedTimedQueue.IGNORE are used to initiate
execution in feedback cycles. If a receiver has a time stamp with
value IGNORE, then it will not be considered when determining which
receiver's get() method will result in the minimum local time
advancement. Once a single token has been consumed by any other
receiver, then the event with time stamp of value IGNORE will be
removed. If all receivers have receiver times of IGNORE, then all
such events will be removed.
<P>
IMPORTANT: This class assumes that valid time stamps have non-negative
values. Reserved negative values exist for special purposes: INACTIVE,
IGNORE and RECEIVER. These values are attributes of PrioritizedTimedQueue.


@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.PrioritizedTimedQueue
@see ptolemy.domains.dde.kernel.DDEThread
*/
public class DDEReceiver extends PrioritizedTimedQueue
    implements ProcessReceiver {

    /** Construct an empty receiver with no container.
     */
    public DDEReceiver() {
        super();
	_boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty receiver with the specified container.
     * @param container The IOPort that contains this receiver.
     */
    public DDEReceiver(IOPort container) {
        super(container);
	_boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty queue with the specified IOPort container
     *  and priority.
     * @param container The IOPort that contains this receiver.
     * @param priority The priority of this receiver.
     */
    public DDEReceiver(IOPort container, int priority) {
        super(container, priority);
	_boundaryDetector = new BoundaryDetector(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a token from the queue. If no token is available,
     *  then throw a NoTokenException. If at any point during
     *  this method this receiver is scheduled for termination,
     *  then throw a TerminateProcessException to cease execution
     *  of the actor that contains this receiver.
     *  <P>
     *  IMPORTANT: This method is designed to be called after
     *  hasToken() has been called. Verify that this method is
     *  safe to call by calling hasToken() first. Note that
     *  this method does not perform a blocking read but hasToken()
     *  does.
     * @return The oldest token on this queue.
     * @exception NoTokenException If this method is called while
     *  hasToken() returns false.
     * @see #hasToken()
     */
    public Token get() throws NoTokenException {
	if( !_hasTokenCache ) {
            throw new NoTokenException( getContainer(),
                    "Attempt to get token that does not have "
		    + "have the earliest time stamp.");
	}
        DDEDirector director = (DDEDirector)
            ((Actor)getContainer().getContainer()).getDirector();
	synchronized( this ) {
	    if( _terminate ) {
		throw new TerminateProcessException("");
	    }
	    Token token = super.get();
	    if( _writeBlocked ) {
                director._actorUnBlocked(this);
		_writeBlocked = false;
		notifyAll();
	    }

	    Thread thread = Thread.currentThread();
	    if( thread instanceof DDEThread ) {
		TimeKeeper timeKeeper =
                    ((DDEThread)thread).getTimeKeeper();
		timeKeeper.sendOutNullTokens(this);
	    }
	    _hasTokenCache = false;
	    return token;
	}
    }

    /** Return true if the receiver has room for putting the given number of 
     *  tokens into it (via the put() method).
     *  Returning true in this method should also guarantee that calling
     *  the put() method will not result in an exception.
     *
     *  @exception IllegalActionException If the Receiver implementation
     *    does not support this query.
     */
    public boolean hasRoom(int tokens) {
	return true;
    }

    /** Return true if the get() method of this receiver will return a
     *  token without throwing a NoTokenException. This method will
     *  perform a blocking read if this receiver is empty and has a
     *  nonnegative receiver time. Once the receiver is no longer empty,
     *  this method will return true only if this receiver is sorted
     *  first with respect to the other receivers contained by this
     *  receiver's actor. The sorting rules are found in
     *  ptolemy.domains.dde.kernel.RcvrComparator.
     *  <P>
     *  If at any point during this method this receiver is scheduled
     *  for termination, then throw a TerminateProcessException to
     *  cease execution of the actor that contains this receiver.
     * @return Return true if the get() method of this receiver will
     *  return a token without throwing a NoTokenException.
     */
    public boolean hasToken() {
	Workspace workspace = getContainer().workspace();
        DDEDirector director = (DDEDirector)((Actor)
		getContainer().getContainer()).getDirector();
	Thread thread = Thread.currentThread();
	TimeKeeper timeKeeper = ((DDEThread)thread).getTimeKeeper();

	if( !(thread instanceof DDEThread) ) {
            return false;
        }

        boolean sendNullTokens = false;

	synchronized(this) {

	    //////////////////////
	    // Update the RcvrList
	    //////////////////////
	    timeKeeper.updateRcvrList( this );

	    /////////////////////////////////////////
	    // Determine if this Receiver is in Front
	    /////////////////////////////////////////
	    if( this != timeKeeper.getFirstRcvr() ) {
	        return false;
	    }

	    //////////////////////////////////////////
	    // Determine if the TimeKeeper is inactive
	    //////////////////////////////////////////
            if( timeKeeper.getNextTime() == INACTIVE ) {
                requestFinish();
	    }

	    ///////////////////
	    // Check Rcvr Times
	    ///////////////////
            if( getRcvrTime() == IGNORE && !_terminate ) {
	        timeKeeper.removeAllIgnoreTokens();

                sendNullTokens = true;
            }

	    ///////////////////////////
	    // Check Token Availability
	    ///////////////////////////
            if( super.hasToken() && !_terminate && !sendNullTokens ) {
	        if ( !_hasNullToken() ) {
		    _hasTokenCache = true;
	            return true;
	        } else {
		    // Treat Null Tokens Normally For Feedback
		    if( !_hideNullTokens ) {
			_hasTokenCache = true;
		        return true;
		    }

		    // Deal With Null Tokens Separately
		    super.get();
                    sendNullTokens = true;
	        }
	    }

	    ////////////////////////
	    // Perform Blocking Read
	    ////////////////////////
	    if( !super.hasToken() && !_terminate && !sendNullTokens ) {
	        _readBlocked = true;
                director._actorBlocked(this);
	        while( _readBlocked && !_terminate ) {
		    workspace.wait( this );
	        }
	    }

	    ////////////////////
	    // Check Termination
	    ////////////////////
	    if( _terminate ) {
	        if( _readBlocked ) {
		    _readBlocked = false;
		    director._actorBlocked(this);
	        }
                throw new TerminateProcessException("");
	    }
        }

        if( sendNullTokens ) {
	    timeKeeper.sendOutNullTokens(this);
        }
	return hasToken();
    }

    /** Return true if the receiver contains the given number of tokens
     *  that can be obtained by calling the get() method.
     *  Returning true in this method should also guarantee that calling
     *  the get() method will not result in an exception.
     *
     *  @exception IllegalActionException If the Receiver implementation
     *    does not support this query.
     */
    public boolean hasToken(int tokens) throws IllegalActionException {
        return true;
	// FIXME hack
    }

    /** Return true if this receiver is connected to the inside of a 
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false. 
     *  <P>
     *  This method is not synchronized so the caller
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isConnectedToBoundary() {
	return _boundaryDetector.isConnectedToBoundary();
    }

    /** Return true if this receiver is connected to the inside of a 
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false. 
     *  <P>
     *  This method is not synchronized so the caller
     *  @return True if this receiver is connected to the inside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isConnectedToBoundaryInside() {
	return _boundaryDetector.isConnectedToBoundaryInside();
    }

    /** Return true if this receiver is connected to the outside of a 
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the outside of a boundary port, then return true; otherwise
     *  return false. 
     *  <P>
     *  This method is not synchronized so the caller
     *  @return True if this receiver is connected to the outside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isConnectedToBoundaryOutside() {
	return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /** Return true if this receiver is contained on the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the inside of a boundary port then return true; otherwise
     *  return false. 
     *  <P>
     *  This method is not synchronized so the caller should be.
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isInsideBoundary() {
	return _boundaryDetector.isInsideBoundary();
    }

    /** Return true if this receiver is contained on the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the outside of a boundary port then return true; otherwise
     *  return false. 
     *  <P>
     *  This method is not synchronized so the caller should be.
     *  @return True if this receiver is contained on the outside of
     *   a boundary port; return false otherwise.
     *  @see BoundaryDetector
     */
    public boolean isOutsideBoundary() {
	return _boundaryDetector.isOutsideBoundary();
    }

    /** Return true if this receiver is read blocked; return false
     *  otherwise.
     */
    public boolean isReadBlocked() {
        return _readBlocked;
    }
    
    /** Return true if this receiver is read blocked; return false
     *  otherwise.
     */
    public boolean isWriteBlocked() {
        return _writeBlocked;
    }
    
    /** Do a blocking write on the queue. Set the time stamp to be
     *  the current time of the sending actor. If this receiver is
     *  connected to a boundary port, then set the time stamp to
     *  be the current time of the time keeper that is associated
     *  with the composite actor that contains the boundary port.
     *  If the time stamp of the token is greater than the
     *  completionTime of this receiver, then set the time stamp to
     *  INACTIVE and the token to null. If the queue is full, then
     *  inform the director that this receiver is blocking on a write
     *  and wait until room becomes available. When room becomes
     *  available, put the token and time stamp in the queue and
     *  inform the director that the block no longer exists. If at
     *  any point during this method this receiver is scheduled for
     *  termination, then throw a TerminateProcessException which
     *  will cease activity for the actor that contains this receiver.
     * @param token The token to put on the queue.
     */
    public void put(Token token) {
	Thread thread = Thread.currentThread();
	double time = _lastTime;
	if( thread instanceof DDEThread ) {
	    TimeKeeper timeKeeper = ((DDEThread)thread).getTimeKeeper();
	    time = timeKeeper.getOutputTime();
	}
	put( token, time );
    }

    /** Do a blocking write on the queue. If at any point during
     *  this method this receiver is scheduled for termination,
     *  then throw a TerminateProcessException which will cease
     *  activity for the actor that contains this receiver. If
     *  the specified time stamp of the token is greater than the
     *  completionTime of this receiver, then set the time stamp
     *  to INACTIVE. If the queue is full, then inform the director
     *  that this receiver is blocking on a write and wait until
     *  room becomes available. When room becomes available, put
     *  the token and time stamp in the queue and inform the director
     *  that the block no longer exists.
     * @param token The token to put on the queue.
     * @param time The time stamp associated with the token.
     */
    public void put(Token token, double time) {
	Thread thread = Thread.currentThread();
        Workspace workspace = getContainer().workspace();
        DDEDirector director = null;
        director = (DDEDirector)((Actor)
		getContainer().getContainer()).getDirector();

        synchronized(this) {

            if( time > _getCompletionTime() &&
                    _getCompletionTime() != ETERNITY && !_terminate ) {
	        time = INACTIVE;
	    }

            if( super.hasRoom() && !_terminate ) {
                super.put(token, time);
		if( _readBlocked ) {
		    director._actorUnBlocked(this);
		    _readBlocked = false;
		    notifyAll();
		}
                return;
            }

            if ( !super.hasRoom() && !_terminate ) {
		_writeBlocked = true;
                director._actorBlocked(this);
		while( _writeBlocked && !_terminate ) {
		    workspace.wait( this );
		}
            }

            if( _terminate ) {
		if( _writeBlocked ) {
		    _writeBlocked = false;
                    director._actorBlocked(this);
		}
                throw new TerminateProcessException( getContainer(),
                        "This receiver has been terminated "
                        + "during _put()");
            }
	}

        put(token, time);
    }

    /** Schedule this receiver to terminate. After this method is
     *  called, a TerminateProcessException will be thrown during
     *  the next call to get() or put() of this class.
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
	super.reset();
	_terminate = false;
    	_readBlocked = false;
    	_writeBlocked = false;
	_hasTokenCache = false;
	_boundaryDetector.reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     package friendly methods   	   ////

    /** Indicate whether hasToken() should return true if the only
     *  available tokens it finds are NullTokens. Specify that
     *  NullTokens should not be taken into consideration by
     *  hasToken() if the parameter is true; otherwise do consider
     *  NullTokens. This method is used in special circumstances
     *  in NullTokens must be manipulated at the actor level. In
     *  particular, FeedBackDelay use this method so that it can
     *  "see" NullTokens that it receives and give them appropriate
     *  delay values.
     * @parameter hide The parameter indicating whether NullTokens
     *  should be taken into consideration by hasToken().
     */
    void _hideNullTokens(boolean hide) {
	_hideNullTokens = hide;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods 		   ////

    ///////////////////////////////////////////////////////////////////
    ////                      package friendly variables           ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _terminate = false;
    private boolean _readBlocked = false;
    private boolean _writeBlocked = false;
    private boolean _hideNullTokens = true;
    private boolean _hasTokenCache = false;

    private BoundaryDetector _boundaryDetector;

}
