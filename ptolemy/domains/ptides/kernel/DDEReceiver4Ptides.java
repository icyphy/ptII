package ptolemy.domains.ptides.kernel;

/* A receiver that stores time stamped tokens according to DDE semantics.

 Copyright (c) 1997-2006 The Regents of the University of California.
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


 */


import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.process.BoundaryDetector;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;

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
 receiver with the highest priority will return true (the others will
 return false).
 <P>
 If a receiver with a nonnegative receiver time is empty, then the
 hasToken() method will perform a blocking read. Once, a token is
 available then hasToken() will return true or false according to
 the minimum time advancement rules cited in the preceding paragraph.
 Note that hasToken() blocks while get() does not block.
 <P>
 DDEReceivers process certain events that are hidden from view by
 ports and actors. In particular, NullTokens have time stamps with
 a value of PrioritizedTimedQueue.IGNORE. NullTokens allow actors
 to communicate information on their local time advancement to
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
 values. Reserved negative values exist for special purposes: INACTIVE
 and IGNORE. These values are attributes of PrioritizedTimedQueue.


 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Green (davisj)
 @Pt.AcceptedRating Green (kienhuis)
 @see ptolemy.domains.dde.kernel.PrioritizedTimedQueue
 @see ptolemy.domains.dde.kernel.DDEThread
 */
public class DDEReceiver4Ptides extends PrioritizedTimedQueue implements
        ProcessReceiver {
    /** Construct an empty receiver with no container.
     */
    public DDEReceiver4Ptides() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The IOPort that contains this receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public DDEReceiver4Ptides(IOPort container) throws IllegalActionException {
        super(container);
        _boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty receiver with the specified IOPort
     *  container and priority.
     *  @param container The IOPort that contains this receiver.
     *  @param priority The priority of this receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public DDEReceiver4Ptides(IOPort container, int priority)
            throws IllegalActionException {
        super(container, priority);
        _boundaryDetector = new BoundaryDetector(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver of any contained tokens.
     */
    public void clear() {
        // FIXME
        //queue.clear();
    }

    public Token get() {
        if (!_hasTokenCache) {
            throw new NoTokenException(getContainer(), "Attempt to get token that does not have " + "have the earliest time stamp.");
        }

        synchronized (_director) {
            if (_terminate) {
                throw new TerminateProcessException("");
            }
            Token token = super.get();
            
//            if (_writePending != null) {
//                _director.threadUnblocked(_writePending, this, DEDirector4Ptides.WRITE_BLOCKED);
//                _writePending = null;
//            }
            Thread thread = Thread.currentThread();
            _hasTokenCache = false;
            return token;
        }
    }

    public DEDirector4Ptides getDirector() {
        return _director;
    }

    public boolean hasRoom(int tokens) {
        return true;
    }

    public boolean hasToken() {
    	_hasTokenCache = super.hasToken();
        return super.hasToken();
    }

    /** Return true if the receiver contains the given number of tokens
     *  that can be obtained by calling the get() method.
     *  Returning true in this method should also guarantee that calling
     *  the get() method will not result in an exception.
     */
    public boolean hasToken(int tokens) {
        return super.hasToken(tokens);

        // FIXME This is wrong!
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
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
     *  @return True if this receiver is connected to the outside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isConnectedToBoundaryOutside() {
        return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /** Return true if this receiver is a consumer receiver. A
     *  receiver is a consumer receiver if it is connected to a
     *  boundary port.
     *
     *  @return True if this is a consumer receiver; return
     *   false otherwise.
     */
    public boolean isConsumerReceiver() {
        if (isConnectedToBoundary()) {
            return true;
        }
        return false;
    }

    /** Return true if this receiver is contained on the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the inside of a boundary port then return true; otherwise
     *  return false.
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
     *  @return True if this receiver is contained on the outside of
     *   a boundary port; return false otherwise.
     *  @see BoundaryDetector
     */
    public boolean isOutsideBoundary() {
        return _boundaryDetector.isOutsideBoundary();
    }

    /** Return true if this receiver is a producer receiver. A
     *  receiver is a producer receiver if it is contained on the
     *  inside or outside of a boundary port.
     *
     *  @return True if this is a producer receiver; return false
     *   otherwise.
     */
    public boolean isProducerReceiver() {
        if (isOutsideBoundary() || isInsideBoundary()) {
            return true;
        }
        return false;
    }

    /** Return a true or false to indicate whether there is a read block
     *  on this receiver or not, respectively.
     *  @return a boolean indicating whether a read is blocked on this
     *  receiver or not.
     */
    public boolean isReadBlocked() {
    	return false;
    }

    /** Return a true or false to indicate whether there is a write block
     *  on this receiver or not.
     *  @return A boolean indicating whether a write is blocked  on this
     *  receiver or not.
     */
    public boolean isWriteBlocked() {
    	return false;
    }

    /** Do a blocking write on the queue. Set the time stamp to be
     *  the current time of the sending actor. If the current time
     *  is greater than the completionTime of this
     *  receiver, then set the time stamp to INACTIVE and the token
     *  to null. If the queue is full, then inform the director that
     *  this receiver is blocking on a write and wait until room
     *  becomes available. When room becomes available, put the
     *  token and time stamp in the queue and inform the director
     *  that the block no longer exists. If at any point during this
     *  method this receiver is scheduled for termination, then throw
     *  a TerminateProcessException which will cease activity for the
     *  actor that contains this receiver.
     *  @param token The token to put in the queue.
     *  @exception TerminateProcessException If activity is scheduled
     *   to cease.
     */
    public void put(Token token) {
        Thread thread = Thread.currentThread();
        Time time = _lastTime;

        if (thread instanceof DDEThread4Ptides) {
            TimeKeeper timeKeeper = ((DDEThread4Ptides) thread).getTimeKeeper();
            time = timeKeeper.getOutputTime();
        }

        put(token, time);
    }

    /** Do a blocking write on the queue. Set the time stamp to be
     *  the time specified by the time parameter. If the specified time
     *  is greater than the completionTime of this receiver, then set
     *  the time stamp to INACTIVE and the token to null. If the queue is
     *  full, then inform the director that this receiver is blocking on a
     *  write and wait until room becomes available. When room becomes
     *  available, put the token and time stamp in the queue and inform the
     *  director that the block no longer exists. If at any point during this
     *  method this receiver is scheduled for termination, then throw a
     *  TerminateProcessException which will cease activity for the actor
     *  that contains this receiver.
     *  @param token The token to put in the queue.
     *  @param time The specified time stamp.
     *  @exception TerminateProcessException If activity is scheduled
     *   to cease.
     */
    public void put(Token token, Time time) {
        Workspace workspace = getContainer().workspace();

        synchronized (_director) {
            if (super.hasRoom() && !_terminate) {
                super.put(token, time);
                return;
            }
            if (_terminate) {
                throw new TerminateProcessException(getContainer(), "This receiver has been terminated during _put()");
            }
        }
        
        put(token, time);
    }

    /** Schedule this receiver to terminate. After this method is
     *  called, a TerminateProcessException will be thrown during
     *  the next call to get() or put() of this class.
     */
    public void requestFinish() {
        synchronized (_director) {
            _terminate = true;
            _director.notifyAll();
        }
    }

    /** Reset local flags. The local flag of this receiver indicates
     *  whether this receiver is scheduled for termination. Resetting
     *  the termination flag will make sure that this receiver is not
     *  scheduled for termination.
     */
    public void reset() {
        super.reset();

//        if (_readPending != null) {
//            _director.threadUnblocked(_readPending, this,
//                    DEDirector4Ptides.READ_BLOCKED);
//        }
//
//        if (_writePending != null) {
//            _director.threadUnblocked(_writePending, this,
//            		DEDirector4Ptides.WRITE_BLOCKED);
//        }

        _terminate = false;
        _hasTokenCache = false;
        _boundaryDetector.reset();
    }

    /** Set the container. This overrides the base class to record
     *  the director.
     *  @param port The container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort, or if the container's director
     *   is not an instance of DDEDirector.
     */
    public void setContainer(IOPort port) throws IllegalActionException {
        super.setContainer(port);

        if (port == null) {
            _director = null;
        } else {
            Actor actor = (Actor) port.getContainer();
            Director director;

            // For a composite actor,
            // the receiver type of an input port is decided by
            // the executive director.
            // While the receiver type of an output is decided by the director.
            // NOTE: getExecutiveDirector() and getDirector() yield the same
            // result for actors that do not contain directors.
            if (port.isInput()) {
                director = actor.getExecutiveDirector();
            } else {
                director = actor.getDirector();
            }

            if (!(director instanceof DEDirector4Ptides)) {
                throw new IllegalActionException(port,
                        "Cannot use an instance of PNQueueReceiver "
                                + "since the director is not a PNDirector.");
            }

            _director = (DEDirector4Ptides) director;
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The boundary detector. */
    private BoundaryDetector _boundaryDetector;

    /** The director in charge of this receiver. */
    private DEDirector4Ptides _director;

    /** Indicator of the result of the most recent call to hasToken(). */
    private boolean _hasTokenCache = false;

    /** Reference to a thread that is read blocked on this receiver. */
    private Thread _readtPending = null;

    /** Flag indicating that termination has been requested. */
    private boolean _terminate = false;

    /** Reference to a thread that is write blocked on this receiver. */
    private Thread _writetPending = null;
}
