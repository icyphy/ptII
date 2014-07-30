/* A receiver with a FIFO queue and performing blocking reads
 and blocking writes.

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
package ptolemy.domains.pn.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.process.BoundaryDetector;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PNQueueReceiver

/**

 A receiver with a FIFO queue that blocks the calling process on a read if the
 FIFO queue is empty and on a write if the queue is full. Blocking read provides
 the basic functionality of a FIFO channel in the process networks model of
 computation. Blocking write supports the implementation suggested by Parks for
 bounded memory execution of process networks.
 <p>
 Tokens are appended to the queue with the put() method, which blocks on a write
 if the queue is full. Tokens are removed from the queue with the get() method,
 which blocks on a read if the queue is empty.
 In case a process blocks on a read or a write, the receiver informs the
 director about the same.
 The receiver also unblocks processes blocked on a read or a write. In case
 a process is blocked on a read (read-blocked), it is unblocked on availability
 of a token.  If a process is blocked on a write (write-blocked), it
 is unblocked on the availability of room in the queue and informs the director
 of the same.
 <p>
 This class is also responsible for pausing or terminating a process that tries
 to read from or write to the receiver. In case of termination, the receiver
 throws a TerminateProcessException when a process tries to read from or write
 to the receiver. This terminates the process.
 In case of pausing, the receiver suspends the process when it tries to read
 from or write to the receiver and resumes it only after a request to resume the
 process has been received.g
 <p>

 @author Mudit Goel, John S. Davis II, Edward A. Lee, Xiaowen Xin
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (hyzheng)
 @see QueueReceiver
 @see ptolemy.actor.QueueReceiver
 */
public class PNQueueReceiver extends QueueReceiver implements ProcessReceiver {
    /** Construct an empty receiver with no container.
     */
    public PNQueueReceiver() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of this receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public PNQueueReceiver(IOPort container) throws IllegalActionException {
        super(container);
        _boundaryDetector = new BoundaryDetector(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the container. This overrides the base class to record
     *  the director.
     *  @param port The container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort, or if the container's director
     *   is not an instance of PNDirector.
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

            if (!(director instanceof PNDirector)) {
                throw new IllegalActionException(port,
                        "Cannot use an instance of PNQueueReceiver "
                                + "since the director is not a PNDirector.");
            }

            _director = (PNDirector) director;
        }
    }

    /** Get a token from this receiver. If the receiver is empty then
     *  block until a token becomes available. If this receiver is
     *  terminated during the execution of this method, then throw a
     *  TerminateProcessException.
     *  @return The token contained by this receiver.
     */
    @Override
    public Token get() {
        Token result = null;
        Workspace workspace = getContainer().workspace();
        while (!_terminate) {
            int depth = 0;
            try {
                // NOTE: This used to synchronize on this, but since it calls
                // director methods that are synchronized on the director,
                // this can cause deadlock.
                synchronized (_director) {
                    // Need to check this again after acquiring the lock.
                    // Otherwise, could end up calling wait() below _after_
                    // notification has occurred.
                    if (_terminate) {
                        break;
                    }

                    // Try to read.
                    if (super.hasToken()) {
                        result = super.get();

                        // Need to mark any thread that is write blocked on
                        // this receiver unblocked now, before any notification,
                        // or we will detect deadlock and increase the buffer sizes.
                        // Note that there is no need to clear the _readPending
                        // reference because that will have been cleared by the write.
                        if (_writePending != null) {
                            _director.threadUnblocked(_writePending, this,
                                    PNDirector.WRITE_BLOCKED);
                            _writePending = null;
                        }

                        break;
                    }
                    _readPending = Thread.currentThread();
                    _director.threadBlocked(Thread.currentThread(), this,
                            PNDirector.READ_BLOCKED);

                    // NOTE: We cannot use workspace.wait(Object) here without
                    // introducing a race condition, because we have to release
                    // the lock on the _director before calling workspace.wait(_director).
                    depth = workspace.releaseReadPermission();
                    _director.wait();
                } // release lock on _director before reacquiring read permissions.
            } catch (InterruptedException e) {
                _terminate = true;
            } finally {
                if (depth > 0) {
                    workspace.reacquireReadPermission(depth);
                }
            }
        }
        if (_terminate) {
            throw new TerminateProcessException("");
        }
        return result;
    }

    /** Return the director in charge of this receiver, or null
     *  if there is none.
     *  @return The director in charge of this receiver.
     */
    public PNDirector getDirector() {
        return _director;
    }

    /** Return true, since a channel in the Kahn process networks
     *  model of computation is of infinite capacity and always has room.
     *  @return True.
     */
    @Override
    public boolean hasRoom() {
        return true;
    }

    /** Return true, since a channel in the Kahn process networks
     *  model of computation is of infinite capacity and always has room.
     *  @param tokens The number of tokens, which is ignored in this method.
     *  @return True.
     */
    @Override
    public boolean hasRoom(int tokens) {
        return true;
    }

    /** Return true, since a call to the get() method of the receiver will
     *  always return a token if the call to get() ever returns.
     *  @return True.
     */
    @Override
    public boolean hasToken() {
        return true;
    }

    /** Return true, since a call to the get() method of the receiver will
     *  always return a token if the call to get() ever returns.
     *  @param tokens The number of tokens, which is ignored in this method.
     *  @return True.
     */
    @Override
    public boolean hasToken(int tokens) {
        return true;
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
     *  @return True if this receiver is connected to the inside of
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

    /** Return true if this receiver is connected to the boundary.
     *  That is, it is in an input port that is connected on
     *  the outside to the inside of an input port, or it is on
     *  the inside of an output port that is connected on the
     *  outside to an input port higher in the hierarchy.
     *  @see #isConnectedToBoundary()
     *  @return True if this is connected to the boundary.
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

    /** Return true if this receiver is at a boundary.
     *  @return True if this receiver is at a boundary.
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
        // NOTE: This method used to be synchronized on this
        // receiver, but since it is called by synchronized methods in
        // the director, that can cause deadlock.
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
        // NOTE: This method used to be synchronized on this
        // receiver, but since it is called by synchronized methods in
        // the director, that can cause deadlock.
        synchronized (_director) {
            return _writePending != null;
        }
    }

    /** Put a token on the queue contained in this receiver.
     *  If the queue is full, then suspend the calling thread (blocking
     *  write) and inform the director of the same. Resume the process on
     *  detecting room in the queue.
     *  If a termination is requested, then initiate the termination of the
     *  calling process by throwing a TerminateProcessException.
     *  On detecting a room in the queue, put a token in the queue.
     *  Check whether any process is blocked
     *  on a read from this receiver. If a process is indeed blocked, then
     *  unblock the process, and inform the director of the same.
     *  @param token The token to be put in the receiver, or null to not put anything.
     *  @exception NoRoomException If during initialization, capacity cannot be increased
     *   enough to accommodate initial tokens.
     */
    @Override
    public void put(Token token) throws NoRoomException {
        IOPort port = getContainer();
        if (port == null || token == null) {
            return; // Nothing to do.
        }
        Workspace workspace = port.workspace();
        while (!_terminate) {
            int depth = 0;
            try {
                // NOTE: Avoid acquiring read access on the workspace
                // while holding the lock on the director because if
                // some other process is trying to acquire write access,
                // the request for read access will be deferred.
                Nameable container = getContainer().getContainer();
                Manager manager = ((Actor) container).getManager();
                // NOTE: This used to synchronize on this, but since it calls
                // director methods that are synchronized on the director,
                // this can cause deadlock.
                synchronized (_director) {
                    // Need to check this again after acquiring the lock.
                    // Otherwise, could end up calling wait() below _after_
                    // notification has occurred.
                    if (_terminate) {
                        break;
                    }

                    // If we are in the initialization phase, then we may have
                    // to increase the queue capacity before proceeding. This
                    // may be needed to support PublisherPorts that produce
                    // initial tokens (or, I suppose, any actor that produces
                    // initial tokens during initialize()?).
                    if (!super.hasRoom()) {
                        if (container instanceof Actor) {
                            if (manager.getState().equals(Manager.INITIALIZING)) {
                                try {
                                    _queue.setCapacity(_queue.getCapacity() + 1);
                                } catch (IllegalActionException e) {
                                    throw new NoRoomException(getContainer(),
                                            "Failed to increase queue capacity enough to accommodate initial tokens");
                                }
                            }
                        }
                    }
                    // Try to write.
                    if (super.hasRoom()) {
                        super.put(token);

                        // If any thread is blocked on a get(), then it will become
                        // unblocked. Notify the director now so that there isn't a
                        // spurious deadlock detection.
                        if (_readPending != null) {
                            _director.threadUnblocked(_readPending, this,
                                    PNDirector.READ_BLOCKED);
                            _readPending = null;
                        }

                        // Normally, the _writePending reference will have
                        // been cleared by the read that unblocked this write.
                        // However, it might be that the director increased the
                        // buffer size, which would also have the affect of unblocking
                        // this write. Hence, we clear it here if it is set.
                        if (_writePending != null) {
                            _director.threadUnblocked(_writePending, this,
                                    PNDirector.WRITE_BLOCKED);
                            _writePending = null;
                        }

                        break;
                    }

                    // Wait to try again.
                    _writePending = Thread.currentThread();
                    _director.threadBlocked(_writePending, this,
                            PNDirector.WRITE_BLOCKED);

                    // NOTE: We cannot use workspace.wait(Object) here without
                    // introducing a race condition, because we have to release
                    // the lock on the _director before calling workspace.wait(_director).
                    depth = workspace.releaseReadPermission();
                    _director.wait();
                } // release lock on _director before reacquiring read permissions.
            } catch (InterruptedException e) {
                _terminate = true;
            } finally {
                if (depth > 0) {
                    workspace.reacquireReadPermission(depth);
                }
            }
        }

        if (_terminate) {
            throw new TerminateProcessException("Process terminated.");
        }
    }

    /** Reset the state variables in the receiver.
     */
    @Override
    public void reset() {
        if (_readPending != null) {
            _director.threadUnblocked(_readPending, this,
                    PNDirector.READ_BLOCKED);
        }

        if (_writePending != null) {
            _director.threadUnblocked(_writePending, this,
                    PNDirector.WRITE_BLOCKED);
        }

        _readPending = null;
        _writePending = null;
        _terminate = false;
        _boundaryDetector.reset();
    }

    /** Set a flag in the receiver to indicate the onset of termination.
     *  This will result in termination of any process that is either blocked
     *  on the receiver or is trying to read from or write to it.
     */
    @Override
    public void requestFinish() {
        // NOTE: This method used to be synchronized on this
        // receiver, but since it calls synchronized methods in
        // the director, that can cause deadlock.
        synchronized (_director) {
            _terminate = true;
            _director.notifyAll();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The director in charge of this receiver. */
    protected PNDirector _director;

    /** Reference to a thread that is read blocked on this receiver. */
    protected Thread _readPending = null;

    /** Reference to a thread that is write blocked on this receiver. */
    protected Thread _writePending = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Flag indicating whether finish has been requested. */
    protected boolean _terminate = false;

    /** A BoundaryDetector determines the topological relationship of
     *  a Receiver with respect to boundary ports.
     */
    protected BoundaryDetector _boundaryDetector;
}
