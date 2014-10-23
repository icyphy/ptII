/* A CSPDirector governs the execution of a CompositeActor with CSP semantics.

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
package ptolemy.domains.csp.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.CompositeProcessDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

// Java imports.
///////////////////////////////////////////////////////////////////
//// CSPDirector

/**
 CSPDirector governs the execution of a composite actor with the semantics
 of the Communicating Sequential Processes (CSP) domain.
 <p>
 In the CSP domain, the director creates a thread for executing each
 actor under its control. Each actor corresponds to a
 process in the model. The threads are created in the initialize
 method and started in the prefire method.  After the thread for an actor
 is started it is <i>active</i> until the thread finishes. While the
 process is active, it can also be <i>blocked</i> or <i>delayed</i>, but
 not both. A process is blocked if it is trying to communicate but
 the process with which it is trying to communicate is not
 ready to do so yet. A process is delayed if it is waiting for
 time to advance, or if it is waiting for a deadlock to occur.
 <p>
 The director is responsible for handling deadlocks, both real
 and timed.  It is also responsible for carrying out any requests for
 changes to the topology that have been made when a deadlock occurs.
 It maintains counts of the number of active
 processes, the number of blocked processes, and the number of
 delayed processes. <i>Deadlock</i> occurs when the number of blocked processes
 plus the number of delayed processes equals the number of active processes.
 <i>Time deadlock</i> occurs if at least one of the active processes
 is delayed. <i>Real deadlock</i> occurs if all of the active processes
 under the control of this director are blocked trying to communicate.
 The fire method controls and responds to deadlocks and carries out
 changes to the topology when it is appropriate.
 <p>
 If real deadlock occurs, the fire method returns. If there are no
 levels above this level in the hierarchy then this marks the end
 of execution of the model. The model execution is terminated by setting
 a flag in every receiver contained in actors controlled by this director.
 When a process tries to send or receive from a receiver with the terminated
 flag set, a TerminateProcessException is thrown which causes the
 actors execution thread to terminate.
 <p>
 Time is controlled by the director. Each process can delay for some
 delta time, and it will continue when the director has advanced time
 by that length of time from the current time. A process is delayed by
 calling delay(double) method. The director <i>advances</i> time each
 occasion a time deadlock occurs and no changes to the topology  are
 pending. If a process specifies zero delay, then the process
 continues immediately. A process may delay itself until the next
 time deadlock occurs by calling waitForDeadlock(). Then the next
 occasion time deadlock occurs, the director wakes up any processes
 waiting for deadlock, and does not advance the current time. Otherwise
 the current model time is increased as well as being advanced.  By default
 the model of computation used in the CSP domain is timed. To use CSP
 without a notion of time, do not use the delay(double) method in any process.
 <p>
 Changes to the topology can occur when deadlock, real or timed, is
 reached. The director carries out any changes that have been queued
 with it. Note that the result of the topology changes may remove the
 deadlock that caused the changes to be carried out.
 <p>
 @author Neil Smyth, Mudit Goel, John S. Davis II
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (nsmyth)
 @Pt.AcceptedRating Green (kienhuis)
 @see ptolemy.actor.Director
 */
public class CSPDirector extends CompositeProcessDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public CSPDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public CSPDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not
     *   compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CSPDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new
     *  object is <i>not</i> added to the directory of that
     *  workspace (you must do this yourself if you want it there).
     *  The result is a new director with no container, no pending
     *  changes to the topology, current time is 0.0, and no actors
     *  are delayed or blocked.
     *  <p>
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new CSPDirector.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CSPDirector newObject = (CSPDirector) super.clone(workspace);

        // newObject._actorsBlocked = 0;

        // Findbugs:
        //  [M M IS] Inconsistent synchronization [IS2_INCONSISTENT_SYNC]
        // Actually this is not a problem since the object is
        // being created and hence nobody else has access to it.

        newObject._actorsDelayed = 0;
        newObject._delayedActorList = new LinkedList();
        return newObject;
    }

    /** Reset flags to initialize values.
     * @exception IllegalActionException If the super class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // _actorsBlocked = 0;
        _actorsDelayed = 0;
        _delayedActorList = new LinkedList();
        super.initialize();
    }

    /** Return a new CSPReceiver compatible with this director.
     *  In the CSP domain, we use CSPReceivers.
     *  @return A new CSPReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new CSPReceiver();
    }

    /** Return false if deadlock has occurred and there are no
     *  input ports, or if stop() has been called, and true otherwise.
     *  <P>
     *  @return False if no more execution is possible, and true otherwise.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // This is a little odd because ProcessDirector.postfire()
        // sets _notDone, but we want to be sure to call it and
        // react to the return value;
        _notDone = super.postfire();
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
    @Override
    public String[] suggestedModalModelDirectors() {
        // This method does not call the method defined in the super class,
        // because this method provides complete new information.
        // Default is a NonStrictFSMDirector, while FSMDirector is also
        // in the array.
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[1] = "ptolemy.domains.modal.kernel.NonStrictFSMDirector";
        defaultSuggestions[0] = "ptolemy.domains.modal.kernel.FSMDirector";
        return defaultSuggestions;
    }

    /** Override the base class to stop any actors that might be stalled
     *  in a call to delay().
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        try {
            _inWrapup = true;

            // Generating WebStart calls wrapup() after preinitialize(),
            // so the model might not have been initialized.
            while (_delayedActorList != null && _delayedActorList.size() > 0) {
                DelayListLink value = (DelayListLink) _delayedActorList.get(0);
                value._actor._cancelDelay();
                _delayedActorList.remove(0);
                _actorsDelayed--;
            }

            super.wrapup();
        } finally {
            _inWrapup = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Called by a CSPActor when it wants to delay. When the
     *  director has advanced time to "getCurrentTime() + delta", the process
     *  corresponding to the actor will continue. Note that actors
     *  can only deal with delta time.
     *  <P>
     *  The method waitForDeadlock() in CSPActor calls this method
     *  with a zero argument. Thus the process will continue the
     *  next occasion time deadlock occurs.
     *  @param delta The length of time to delay the actor.
     *  @param actor The actor being delayed.
     *  @exception InvalidStateException If an actor is delayed for
     *   negative time.
     */
    protected synchronized void _actorDelayed(double delta, CSPActor actor)
            throws InvalidStateException {
        if (_inWrapup) {
            actor._cancelDelay();
            return;
        }

        if (delta < 0.0) {
            throw new InvalidStateException(((Nameable) actor).getName()
                    + ": delayed for negative time.");
        } else {
            _actorsDelayed++;

            // Enter the actor and the time to wake it up into the
            // LinkedList of delayed actors.
            _registerDelayedActor(getModelTime().add(delta), actor);
            notifyAll();
            return;
        }
    }

    /** Returns true if all active processes are either blocked or
     *  delayed, false otherwise.
     */
    @Override
    protected synchronized boolean _areThreadsDeadlocked() {
        if (_getActiveThreadsCount() == _getBlockedThreadsCount()
                + _actorsDelayed) {
            return true;
        }

        return false;
    }

    /** Return true if the count of active processes equals the number
     *  of stopped, blocked, and delayed threads.  Otherwise return false.
     *  @return True if all threads are stopped.
     */
    @Override
    protected synchronized boolean _areAllThreadsStopped() {
        return _getActiveThreadsCount() == _getStoppedThreadsCount()
                + _getBlockedThreadsCount() + _actorsDelayed;
    }

    /** Return a string describing the status of each receiver.
     *  @return A string describing the status of each receiver.
     * @exception IllegalActionException
     * @exception InvalidStateException
     */
    private String _receiverStatus() throws InvalidStateException,
    IllegalActionException {
        StringBuffer result = new StringBuffer();
        CompositeActor container = (CompositeActor) getContainer();

        // Start with the input ports of the composite, which
        // may have forked connections on the inside.
        Iterator inputPorts = container.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            result.append("Send inside from " + inputPort.getFullName() + "\n");
            Receiver[][] destinations = inputPort.deepGetReceivers();
            for (int channel = 0; channel < destinations.length; channel++) {
                if (destinations[channel] != null) {
                    result.append("   on channel " + channel + ":\n");
                    for (int copy = 0; copy < destinations[channel].length; copy++) {
                        result.append("-- to "
                                + _receiverStatus(destinations[channel][copy])
                                + "\n");
                    }
                }
            }
        }

        // Next do the output ports of all contained actors.
        Iterator actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();
                result.append("Send from " + outputPort.getFullName() + "\n");
                Receiver[][] destinations = outputPort.getRemoteReceivers();
                for (int channel = 0; channel < destinations.length; channel++) {
                    if (destinations[channel] != null) {
                        result.append("   on channel " + channel + ":\n");
                        for (int copy = 0; copy < destinations[channel].length; copy++) {
                            result.append("-- to "
                                    + _receiverStatus(destinations[channel][copy])
                                    + "\n");
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    /** Return a string describing the status of the specified receiver.
     *  @param receiver The receiver to describe.
     *  @return A string describing the status of the specified receiver.
     */
    protected static String _receiverStatus(Receiver receiver) {
        StringBuffer result = new StringBuffer();
        result.append(receiver.getContainer().getFullName());
        if (receiver instanceof CSPReceiver) {
            CSPReceiver castReceiver = (CSPReceiver) receiver;
            if (castReceiver._isGetWaiting()) {
                result.append(" get() waiting");
            }
            if (castReceiver._isPutWaiting()) {
                result.append(" put() waiting");
            }
            if (castReceiver._isConditionalReceiveWaiting()) {
                result.append(" conditional receive waiting");
            }
            if (castReceiver._isConditionalSendWaiting()) {
                result.append(" conditional send waiting");
            }
        }
        return result.toString();
    }

    /** Respond to a deadlock. This is where nearly all the control for the
     *  model at this level in the hierarchy is located.
     *  <p>
     *  Deadlock occurs if the number of blocked and delayed processes
     *  equals the number of active processes. The method looks for
     *  three cases in the following order: are there topology changes
     *  waiting to happen, are there any processes delayed, are all the
     *  processes blocked trying to rendezvous.
     *  <p>
     *  If there are changes to the topology waiting to happen, they are
     *  performed and the execution of the model continues.
     *  Note that the result of performing the topology changes may be
     *  to remove the deadlock that had occurred.
     *  <p>
     *  If the number of delayed processes is greater than zero, then
     *  <i>time deadlock</i> has occurred. If one or more processes
     *  are delayed waiting for deadlock to occur, then those processes
     *  are resumed and time is not advanced. Otherwise time is advanced
     *  and the earliest delayed process is resumed.
     *  <p>
     *  If all the processes are blocked, then <i>real deadlock</i> has
     *  occurred, and this method returns false. If there are no levels
     *  above this one in the hierarchy, then real deadlock marks the
     *  end of executing the model.
     *  @return False if real deadlock occurred, true otherwise.
     *  @exception IllegalActionException If setting the model time,
     *  or getting a parameter throws it.
     */
    @Override
    protected synchronized boolean _resolveInternalDeadlock()
            throws IllegalActionException {
        if (_actorsDelayed > 0) {
            // Time deadlock.
            Time nextTime = _getNextTime();
            setModelTime(nextTime);

            // Now go through list of delayed actors
            // and wake up those at this time
            boolean done = false;

            while (!done && _delayedActorList.size() > 0) {
                DelayListLink value = (DelayListLink) _delayedActorList.get(0);

                if (value._resumeTime.compareTo(nextTime) == 0) {
                    _delayedActorList.remove(0);
                    value._actor._continue();
                    _actorsDelayed--;
                } else {
                    done = true;
                }
            }
        } else if (_getBlockedThreadsCount() == _getActiveThreadsCount()) {
            // Report deadlock.
            Parameter suppress = (Parameter) getContainer().getAttribute(
                    "SuppressDeadlockReporting", Parameter.class);
            if (suppress == null
                    || !(suppress.getToken() instanceof BooleanToken)
                    || !((BooleanToken) suppress.getToken()).booleanValue()) {
                String message = "Model ended with a deadlock (this may be normal for this model).\n"
                        + "A parameter with name SuppressDeadlockReporting and value true will suppress this message.\n"
                        + "Status of receivers:\n" + _receiverStatus();
                MessageHandler.message(message);
            }
            return false;
        }

        // Return true for topology changes and time deadlock.
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the earliest time which an actor has been delayed to. This
     *  should always be the top link on the list.
     */
    private Time _getNextTime() {
        if (_delayedActorList.size() > 0) {
            return ((DelayListLink) _delayedActorList.get(0))._resumeTime;
        } else {
            throw new InvalidStateException("CSPDirector.getNextTime(): "
                    + " called in error.");
        }
    }

    /** Keep track of when and for how long processes are delayed.
     *  @param actor The delayed actor.
     *  @param actorTime The time at which to resume the actor.
     */
    private void _registerDelayedActor(Time actorTime, CSPActor actor) {
        DelayListLink newLink = new DelayListLink();
        newLink._resumeTime = actorTime;
        newLink._actor = actor;

        int size = _delayedActorList.size();

        boolean done = false;

        for (int i = 0; i < size; i++) {
            DelayListLink tmp = (DelayListLink) _delayedActorList.get(i);

            if (!done && actorTime.compareTo(tmp._resumeTime) < 0) {
                _delayedActorList.add(i, newLink);
                done = true;
            }
        }

        if (!done) {
            _delayedActorList.add(newLink);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Count of the number of processes delayed until time
     *  sufficiently advances.
     */
    private int _actorsDelayed = 0;

    // A sorted list of the times of delayed actors. The time the model
    // will next be advanced to is the time at the top of the list.
    private List _delayedActorList;

    // Set to true when the director enters the wrapup() method. Any call
    // to _actorDelayed() when this flag is true will simply cancel the
    // delay and return. The purpose is to avoid the deadlock that happens
    // when an actor is delayed after the director calls super.wrapup() in
    // which it waits for all actors to stop.
    private boolean _inWrapup = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    /** Keep track of the actor that is delayed and the time
     *  at which to resume it.
     */
    private static class DelayListLink {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
        public Time _resumeTime;

        public CSPActor _actor;
    }
}
