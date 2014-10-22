/* A receiver that stores time stamped tokens according to DDE semantics.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.dde.kernel;

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
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
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
public class DDEReceiver extends PrioritizedTimedQueue implements
        ProcessReceiver {
    /** Construct an empty receiver with no container.
     */
    public DDEReceiver() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The IOPort that contains this receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public DDEReceiver(IOPort container) throws IllegalActionException {
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
    public DDEReceiver(IOPort container, int priority)
            throws IllegalActionException {
        super(container, priority);
        _boundaryDetector = new BoundaryDetector(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear this receiver of any contained tokens.
     */
    @Override
    public void clear() {
        // FIXME
        //queue.clear();
    }

    /** Get a token from the mailbox receiver.
     *  @return The token contained by this receiver.
     *  @exception NoTokenException If there is no token.
     */
    @Override
    public Token get() {
        if (!_hasTokenCache) {
            throw new NoTokenException(getContainer(),
                    "Attempt to get token that does not have "
                            + "have the earliest time stamp.");
        }

        synchronized (_director) {
            if (_terminate) {
                throw new TerminateProcessException("");
            }

            Token token = super.get();

            // Need to mark any thread that is write blocked on
            // this receiver unblocked now, before any notification,
            // or we will detect deadlock and increase the buffer sizes.
            // Note that there is no need to clear the _readPending
            // reference because that will have been cleared by the write.
            if (_writePending != null) {
                _director.threadUnblocked(_writePending, this,
                        DDEDirector.WRITE_BLOCKED);
                _writePending = null;
            }

            Thread thread = Thread.currentThread();

            if (thread instanceof DDEThread) {
                TimeKeeper timeKeeper = ((DDEThread) thread).getTimeKeeper();
                timeKeeper.sendOutNullTokens(this);
            }

            _hasTokenCache = false;
            return token;
        }
    }

    /** Return the director in charge of this receiver, or null
     *  if there is none.
     *  @return The director in charge of this receiver.
     */
    public DDEDirector getDirector() {
        return _director;
    }

    /** Return true if the receiver has room for putting the given number of
     *  tokens into it (via the put() method).
     *  Returning true in this method should also guarantee that calling
     *  the put() method will not result in an exception.
     */
    @Override
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
     *  ptolemy.domains.dde.kernel.ReceiverComparator.
     *  <P>
     *  If at any point during this method this receiver is scheduled
     *  for termination, then throw a TerminateProcessException to
     *  cease execution of the actor that contains this receiver.
     *  @return Return true if the get() method of this receiver will
     *   return a token without throwing a NoTokenException.  Return
     *   false if the current thread is not a DDEThread.
     */
    @Override
    public boolean hasToken() {
        Workspace workspace = getContainer().workspace();
        Thread thread = Thread.currentThread();

        if (!(thread instanceof DDEThread)) {
            return false;
        }

        TimeKeeper timeKeeper = ((DDEThread) thread).getTimeKeeper();

        boolean sendNullTokens = false;

        synchronized (_director) {
            //////////////////////
            // Update the ReceiverList
            //////////////////////
            timeKeeper.updateReceiverList(this);

            /////////////////////////////////////////
            // Determine if this Receiver is in Front
            /////////////////////////////////////////
            if (this != timeKeeper.getFirstReceiver()) {
                return false;
            }

            //////////////////////////////////////////
            // Determine if the TimeKeeper is inactive
            //////////////////////////////////////////
            if (timeKeeper.getNextTime().getDoubleValue() == INACTIVE) {
                requestFinish();
            }

            ///////////////////
            // Check Receiver Times
            ///////////////////
            if (getReceiverTime().getDoubleValue() == IGNORE && !_terminate) {
                timeKeeper.removeAllIgnoreTokens();

                sendNullTokens = true;
            }

            ///////////////////////////
            // Check Token Availability
            ///////////////////////////
            if (super.hasToken() && !_terminate && !sendNullTokens) {
                if (!_hasNullToken()) {
                    _hasTokenCache = true;
                    return true;
                } else {
                    // Treat Null Tokens Normally For Feedback
                    if (!_hideNullTokens) {
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
            if (!super.hasToken() && !_terminate && !sendNullTokens) {
                _readPending = thread;
                _director.threadBlocked(thread, this, DDEDirector.READ_BLOCKED);

                while (_readPending != null && !_terminate) {
                    try {
                        workspace.wait(_director);
                    } catch (InterruptedException e) {
                        _terminate = true;
                        break;
                    }
                }
            }

            ////////////////////
            // Check Termination
            ////////////////////
            if (_terminate) {
                if (_readPending != null) {
                    _director.threadUnblocked(_readPending, this,
                            DDEDirector.READ_BLOCKED);
                    _readPending = null;
                }

                throw new TerminateProcessException("");
            }
        }

        if (sendNullTokens) {
            timeKeeper.sendOutNullTokens(this);
        }

        // FIXME: This is a silly way to implement while (true).
        return hasToken();
    }

    /** Return true if the receiver contains the given number of tokens
     *  that can be obtained by calling the get() method.
     *  Returning true in this method should also guarantee that calling
     *  the get() method will not result in an exception.
     */
    @Override
    public boolean hasToken(int tokens) {
        return true;

        // FIXME This is wrong!
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     * @exception IllegalActionException
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    @Override
    public boolean isConnectedToBoundary() throws IllegalActionException {
        return _boundaryDetector.isConnectedToBoundary();
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
     *  @return True if this receiver is connected to the inside of
     *   a boundary port; return false otherwise.
     * @exception IllegalActionException
     * @exception InvalidStateException
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    @Override
    public boolean isConnectedToBoundaryInside() throws InvalidStateException,
            IllegalActionException {
        return _boundaryDetector.isConnectedToBoundaryInside();
    }

    /** Return true if this receiver is connected to the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the outside of a boundary port, then return true; otherwise
     *  return false.
     *  @return True if this receiver is connected to the outside of
     *   a boundary port; return false otherwise.
     * @exception IllegalActionException
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    @Override
    public boolean isConnectedToBoundaryOutside() throws IllegalActionException {
        return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /** Return true if this receiver is a consumer receiver. A
     *  receiver is a consumer receiver if it is connected to a
     *  boundary port.
     *
     *  @return True if this is a consumer receiver; return
     *   false otherwise.
     * @exception IllegalActionException
     */
    @Override
    public boolean isConsumerReceiver() throws IllegalActionException {
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean isReadBlocked() {
        synchronized (_director) {
            return _readPending != null;
        }
    }

    /** Return a true or false to indicate whether there is a write block
     *  on this receiver or not.
     *  @return A boolean indicating whether a write is blocked  on this
     *  receiver or not.
     */
    @Override
    public boolean isWriteBlocked() {
        synchronized (_director) {
            return _writePending != null;
        }
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
     *  @param token The token to put in the queue, or null to put no token.
     *  @exception TerminateProcessException If activity is scheduled
     *   to cease.
     */
    @Override
    public void put(Token token) {
        if (token == null) {
            return;
        }
        Thread thread = Thread.currentThread();
        Time time = _lastTime;

        if (thread instanceof DDEThread) {
            TimeKeeper timeKeeper = ((DDEThread) thread).getTimeKeeper();
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
     *  @param token The token to put in the queue, or null to put no token.
     *  @param time The specified time stamp.
     *  @exception TerminateProcessException If activity is scheduled
     *   to cease.
     */
    @Override
    public void put(Token token, Time time) {
        Workspace workspace = getContainer().workspace();

        synchronized (_director) {
            if (time.compareTo(_getCompletionTime()) > 0
                    && _getCompletionTime().getDoubleValue() != ETERNITY
                    && !_terminate) {
                try {
                    time = new Time(_director, INACTIVE);
                } catch (IllegalActionException e) {
                    // If the time resolution of the director is invalid,
                    // it should have been caught before this.
                    throw new InternalErrorException(e);
                }
            }

            if (super.hasRoom() && !_terminate) {
                super.put(token, time);

                // If any thread is blocked on a get(), then it will become
                // unblocked. Notify the director now so that there isn't a
                // spurious deadlock detection.
                if (_readPending != null) {
                    _director.threadUnblocked(_readPending, this,
                            DDEDirector.READ_BLOCKED);
                    _readPending = null;
                }

                return;
            }

            if (!super.hasRoom() && !_terminate) {
                _writePending = Thread.currentThread();
                _director.threadBlocked(_writePending, this,
                        DDEDirector.WRITE_BLOCKED);

                while (_writePending != null && !_terminate) {
                    try {
                        workspace.wait(_director);
                    } catch (InterruptedException e) {
                        _terminate = true;
                        break;
                    }
                }
            }

            if (_terminate) {
                if (_writePending != null) {
                    _director.threadUnblocked(_writePending, this,
                            DDEDirector.WRITE_BLOCKED);
                    _writePending = null;
                }

                throw new TerminateProcessException(getContainer(),
                        "This receiver has been terminated during _put()");
            }
        }

        put(token, time);
    }

    /** Schedule this receiver to terminate. After this method is
     *  called, a TerminateProcessException will be thrown during
     *  the next call to get() or put() of this class.
     */
    @Override
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
    @Override
    public void reset() {
        super.reset();

        if (_readPending != null) {
            _director.threadUnblocked(_readPending, this,
                    DDEDirector.READ_BLOCKED);
        }

        if (_writePending != null) {
            _director.threadUnblocked(_writePending, this,
                    DDEDirector.WRITE_BLOCKED);
        }

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
    @Override
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

            if (!(director instanceof DDEDirector)) {
                throw new IllegalActionException(port,
                        "Cannot use an instance of PNQueueReceiver "
                                + "since the director is not a PNDirector.");
            }

            _director = (DDEDirector) director;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     package friendly methods              ////

    /** Indicate whether hasToken() should return true if the only
     *  available tokens it finds are NullTokens. Specify that
     *  NullTokens should not be taken into consideration by
     *  hasToken() if the parameter is true; otherwise do consider
     *  NullTokens. This method is used in special circumstances
     *  in NullTokens must be manipulated at the actor level. In
     *  particular, FeedBackDelay uses this method so that it can
     *  "see" NullTokens that it receives and give them appropriate
     *  delay values. For this reason this method is package friendly.
     *  @param hide The parameter indicating whether NullTokens
     *   should be taken into consideration by hasToken().
     */
    void _hideNullTokens(boolean hide) {
        _hideNullTokens = hide;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The boundary detector. */
    private BoundaryDetector _boundaryDetector;

    /** The director in charge of this receiver. */
    private DDEDirector _director;

    /** Indicator of the result of the most recent call to hasToken(). */
    private boolean _hasTokenCache = false;

    /** Reference to a thread that is read blocked on this receiver. */
    private Thread _readPending = null;

    /** Flag indicating that termination has been requested. */
    private boolean _terminate = false;

    /** Reference to a thread that is write blocked on this receiver. */
    private Thread _writePending = null;

    // FIXME: Comments and ordering
    private boolean _hideNullTokens = true;
}
