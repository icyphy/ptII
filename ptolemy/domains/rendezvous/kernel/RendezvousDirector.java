/* A director that supports threaded actors with rendezvous communication.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.domains.rendezvous.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.CompositeProcessDirector;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

// Java imports.
//////////////////////////////////////////////////////////////////////////
//// RendezvousDirector

/**
 This director executes actors in their own threads
 and provides a receiver that implements rendezvous communication.
 The threads are created in the initialize()
 method and started in the prefire() method.  After the thread for an actor
 is started it is <i>active</i> until the thread finishes. While the
 thread is active, it can also be <i>blocked</i>.
 A thread is blocked if it is trying to communicate but
 the thread with which it is trying to communicate is not
 ready to do so yet. A deadlock occurs when all threads are
 blocked. If this director is used at the top level,
 the model stops executing when a deadlock occurs.
 This director is based on the CSPDirector by
 Neil Smyth, Mudit Goel, and John S. Davis II.
 @author Thomas Feng, Edward A. Lee, Yang Zhao
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating green (acataldo)
 @Pt.AcceptedRating green (acataldo)
 */
public class RendezvousDirector extends CompositeProcessDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public RendezvousDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public RendezvousDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not
     *   compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public RendezvousDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new instance of RendezvousReceiver compatible with
     *  this director.
     *  @return A new instance of RendezvousReceiver.
     */
    public Receiver newReceiver() {
        return new RendezvousReceiver();
    }

    /** Return false if the model should not continue to execute.
     *  @return False if no more execution is possible, and true otherwise.
     */
    public boolean postfire() {
        // FIXME: Should this call super.postfire?
        List ports = ((CompositeActor) getContainer()).inputPortList();

        if (ports.iterator().hasNext()) {
            return !_stopRequested;
        } else {
            return _notDone && !_stopRequested;
        }
    }

    /** Return an array of suggested directors to be used with ModalModel.
     *  This is the FSMDirector followed by the NonStrictFSMDirector.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    public String[] suggestedModalModelDirectors() {
        // This method does not call the method defined in the super class,
        // because this method provides complete new information.
        // Default is a NonStrictFSMDirector, while FSMDirector is also
        // in the array.
        String[] defaultSuggestions = new String[] {
                "ptolemy.domains.fsm.kernel.FSMDirector",
                "ptolemy.domains.fsm.kernel.NonStrictFSMDirector"
        };
        return defaultSuggestions;
    }

    /** Override the base class to set a flag indicating we are in the
     *  wrapup phase.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
        try {
            _inWrapup = true;
            super.wrapup();
        } finally {
            _inWrapup = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if the count of active threads equals the number
     *  of stopped (paused) or blocked threads.  Otherwise return false.
     *  @return True if all threads are stopped or blocked.
     */
    protected synchronized boolean _areAllThreadsStopped() {
        return (_getActiveThreadsCount()
                == (_getStoppedThreadsCount() + _getBlockedThreadsCount()));
    }

    /** Return true if all active threads are blocked.
     *  @return True if all active threads are blocked.
     */
    protected synchronized boolean _areThreadsDeadlocked() {
        if (_getActiveThreadsCount() == _getBlockedThreadsCount()) {
            return true;
        } else {
            return false;
        }
    }

    /** If the model is deadlocked, report the deadlock if parameter
     *  "SuppressDeadlockReporting" is not set to boolean true, and return
     *  false.
     *  Otherwise, return true. Deadlock occurs if the number of blocked threads
     *  equals the number of active threads.
     *  @return False if deadlock occurred, true otherwise.
     */
    protected synchronized boolean _resolveInternalDeadlock()
            throws IllegalActionException {
        if (_getBlockedThreadsCount() == _getActiveThreadsCount()) {
            // Report deadlock.
            Parameter suppress = (Parameter) getContainer().getAttribute(
                    "SuppressDeadlockReporting", Parameter.class);

            if ((suppress == null)
                    || !(suppress.getToken() instanceof BooleanToken)
                    || !((BooleanToken) suppress.getToken()).booleanValue()) {
                MessageHandler.message("Model ended with a deadlock "
                    + "(this may be normal for this model).\n"
                    + "A parameter with name SuppressDeadlockReporting and "
                    + "value true will suppress this message.");
            }

            return false;
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Set to true when the director enters the wrapup() method.
     *  The purpose is to avoid the deadlock that happens
     *  when an actor is delayed after the director calls super.wrapup() in
     *  which it waits for all actors to stop.
     */
    protected boolean _inWrapup = false;
}
