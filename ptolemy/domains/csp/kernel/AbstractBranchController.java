/* A base class for controllers that manages multiway or conditional
 branches within the CSP domain.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.domains.csp.kernel;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.Debuggable;

///////////////////////////////////////////////////////////////////
//// AbstractBranchController

/**
 This is a base class containing the common code for controllers
 that manage branches for performing conditional or
 multiway rendezvous within the CSP (Communication Sequential Processes)
 domain. Any CSP actors (either atomic or composite) that
 multiway rendezvous should contain an instance of this class.
 In addition, they also needs to implement the
 BranchActor interface.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (bilung)
 @see ConditionalBranch
 @see BranchActor
 @see ConditionalReceive
 @see ConditionalSend
 */
public abstract class AbstractBranchController implements Debuggable {

    /** Construct a controller in the specified container, which should
     *  be an actor that implements BranchActor.
     *  @param container The parent actor that contains this object.
     */
    public AbstractBranchController(Actor container) {
        _parentActor = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a debug listener.
     *  If the listener is already in the list, do not add it again.
     *  @param listener The listener to which to send debug messages.
     *  @see #removeDebugListener(DebugListener)
     */
    @Override
    public void addDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            _debugListeners = new LinkedList();
        }
        if (_debugListeners.contains(listener)) {
            return;
        } else {
            _debugListeners.add(listener);
        }
        _debugging = true;
    }

    /** Return the Actor that creates the branch and owns this
     *  controller when performing a CIF or CDO.
     *  @return The CSPActor that created this branch.
     */
    public Actor getParent() {
        return _parentActor;
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    @Override
    public void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }
        _debugListeners.remove(listener);

        if (_debugListeners.size() == 0) {
            _debugging = false;
        }
    }

    /** Terminate abruptly any threads created by this actor. Note that
     *  this method does not allow the threads to terminate gracefully.
     */
    public void terminate() {
        synchronized (_getDirector()) {
            // Now stop any threads created by this director.
            if (_threadList != null) {
                Iterator threads = _threadList.iterator();

                while (threads.hasNext()) {
                    Thread next = (Thread) threads.next();

                    if (next.isAlive()) {
                        next.stop();
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Notify the director that the current thread is blocked.
     *  @param receiver The receiver handling the I/O operation,
     *   or null if it is not a specific receiver.
     */
    protected void _branchBlocked(CSPReceiver receiver) {
        _getDirector().threadBlocked(Thread.currentThread(), receiver);
    }

    /** Register the calling branch as failed. This reduces the count
     *  of active branches, and if all the active branches have
     *  finished, it notifies the internal lock so any threads
     *  that are blocked on it can continue.
     *  This is called by a conditional branch just before it dies.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     */
    protected void _branchFailed(int branchNumber) {
        if (_debugging) {
            _debug("** Branch failed: " + branchNumber);
        }
        Object director = _getDirector();
        synchronized (director) {
            _branchesActive--;

            if (_branchesActive == 0) {
                director.notifyAll();
            }
        }
    }

    /** Indicate that the branch is not ready to rendezvous.
     *  @param branchNumber The ID assigned to the branch upon creation.
     */
    protected abstract void _branchNotReady(int branchNumber);

    /** Register the calling branch as a successful branch. This
     *  reduces the count of active branches, and notifies the internal
     *  lock so that any threads blocked on it can continue.
     *  @param branchID The ID assigned to the calling branch upon creation.
     */
    protected void _branchSucceeded(int branchID) {
        Object director = _getDirector();
        synchronized (director) {
            if (_debugging) {
                _debug("** Branch succeeded: " + branchID);
            }
            _branchesActive--;

            // wakes up chooseBranch() which wakes up parent thread
            director.notifyAll();
        }
    }

    /** Notify the director that the current thread is unblocked.
     *  @param receiver The receiver handling the I/O operation,
     *   or null if it is not a specific receiver.
     */
    protected void _branchUnblocked(CSPReceiver receiver) {
        _getDirector().threadUnblocked(Thread.currentThread(), receiver);
    }

    /** Send a debug message to all debug listeners that have registered.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param message The message.
     */
    protected final void _debug(String message) {
        if (_debugging) {
            Iterator listeners = _debugListeners.iterator();
            while (listeners.hasNext()) {
                ((DebugListener) listeners.next()).message(message);
            }
        }
    }

    /** Get the director that controls the execution of its parent actor.
     *  @return The executive director if the actor is composite, and
     *   otherwise, the director.
     */
    protected CSPDirector _getDirector() {
        try {
            return (CSPDirector) _parentActor.getExecutiveDirector();
        } catch (Exception ex) {
            // If a thread has a reference to a receiver with no director it
            // is an error so terminate the process.
            throw new TerminateProcessException("CSPReceiver: trying to "
                    + " rendezvous with a receiver with no "
                    + "director => terminate.");
        }
    }

    /** Called by ConditionalSend and ConditionalReceive to check whether
     *  the calling branch is ready to rendezvous.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     *  @return True if the calling branch is ready
     *   to rendezvous, otherwise false.
     */
    protected abstract boolean _isBranchReady(int branchNumber);

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The set of branches currently being chosen from in
     *  chooseBranch().
     */
    protected ConditionalBranch[] _branches;

    /** The number of conditional branches that are still active,
     *  meaning that they are capable of succeeding.
     */
    protected int _branchesActive = 0;

    /** Flag that is true if there are debug listeners. */
    protected boolean _debugging = false;

    /** List of threads created by this actor to perform a conditional rendezvous.
     *  Need to keep a list of them in case the execution of the model is
     *  terminated abruptly.
     */
    protected LinkedList _threadList = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of DebugListeners registered with this object. */
    private LinkedList _debugListeners = null;

    /** The actor who owns this controller. */
    private Actor _parentActor;
}
