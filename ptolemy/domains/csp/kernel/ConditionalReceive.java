/* Class for performing Conditional Receives.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// ConditionalReceive

/**
 Represents a <I>guarded communication statement</I> in which the
 communication is a get(). Thus it represents
 <P>
 <CENTER> guard; get() => statements</CENTER>
 <P>
 It is one branch of either a CDO or a CIF conditional
 communication construct.
 <p>
 The branches used in a conditional communication construct are
 controlled by the chooseBranch() method of ConditionalBranchController.
 <p>
 Each branch is created to perform one communication. If more than
 one branch is enabled (the guard is true or absent), then a thread
 is created for each enabled branch to try and perform
 the appropriate rendezvous. If the branch
 succeeds and is allowed to rendezvous, then it registers itself with
 the controller and the thread it is running in dies. Otherwise it
 continues to try and rendezvous until it succeeds or it is notified that
 another branch has succeeded in with its rendezvous, in which case this
 branch has failed and the thread it is running in dies.
 <p>
 For rendezvous, the receiver is the key synchronization point. The
 receiver with which this branch will try to rendezvous is set upon
 instantiation. It is determined from the port and channel which is
 passed in in the constructor.
 <p>
 The algorithm by which a branch determines whether or not it has
 succeeded with its rendezvous is executed in the run method. There are
 roughly three parts to the algorithm, each of which is relevant
 to the different rendezvous scenarios.
 <br>
 <I>Case 1:</I> There is a put already waiting
 at the rendezvous point. In this case
 the branch attempts to register itself, with the controller, as the first
 branch ready to rendezvous. If it succeeds, it performs the rendezvous,
 notifies the controller that it succeeded and returns. If it is not the first,
 it keeps on trying to register itself until it finally succeeds or another
 branch successfully rendezvoused in which case it fails and terminates. Note
 that a put cannot "go away" so it remains in an inner-loop trying to
 rendezvous or failing.
 <br>
 <I>Case 2:</I> There is a conditional send
 waiting. In this case it tries to register both branches with their
 controllers as the first to try. If it
 succeeds it performs the transfer, notifies the controller and returns. It
 performs the registration in two steps, first registering this branch and
 then registering the other branch. If it successfully registers this branch,
 but cannot register the other, it unregisters itself as the first branch
 trying, and starts trying to rendezvous from the beginning. This is because
 the conditional send could "go away". If it is unable to register itself as
 the first branch to try, it again starts trying to rendezvous from the
 beginning.
 <br>
 <I>Case 3:</I> If there is neither a put or a
 conditional send waiting, it sets a
 flag in the receiver that a conditional receive is trying to rendezvous. It
 then waits until a put is executed on the receiver, or until another branch
 succeeds and this branch fails. If this branch fails, it resets the flag in
 the receiver, notifies the controller and returns. Note that it only needs
 to wait on a put as if a conditional send is executed on the receiver, it is
 the branch which is responsible for checking that the rendezvous can proceed.
 Thus, in the case where two conditional branches are trying to rendezvous
 at a receiver, it is the responsibility of the branch arriving second to
 check that the rendezvous can proceed(see case 2).
 <p>
 @author  Neil Smyth and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (eal)
 <p>
 @see ptolemy.domains.csp.kernel.ConditionalBranch
 */
public class ConditionalReceive extends ConditionalBranch implements Runnable {

    // FIXME: Downgraded from green with major redesign. EAL.

    /** Create a conditional receive.
     *  @param port The IOPort containing the channel (and thus receiver)
     *   that this branch will try to rendezvous with.
     *  @param channel The channel in the IOPort that this branch is
     *   trying to rendezvous with.
     *  @param branch The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @exception IllegalActionException If the channel has more
     *   than one receiver or if the receiver is not of type CSPReceiver.
     */
    public ConditionalReceive(IOPort port, int channel, int branch)
            throws IllegalActionException {
        this(true, port, channel, branch);
    }

    /** Create a guarded communication with a get() communication.
     *  @param guard The guard for the guarded communication statement
     *   represented by this object.
     *  @param port The IOPort containing the channel (and thus receiver)
     *   that this branch will try to rendezvous with.
     *  @param channel The channel in the IOPort that this branch is
     *   trying to rendezvous with.
     *  @param branch The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @exception IllegalActionException If the channel has more
     *   than one receiver or if the receiver is not of type CSPReceiver.
     */
    public ConditionalReceive(boolean guard, IOPort port, int channel,
            int branch) throws IllegalActionException {
        super(guard, port, branch);
        _init(port, channel);
    }

    /** Create a guarded communication with a get() communication. This
     *  constructor allows actors which are not CSPActors access to
     *  CSP functionality by providing their own ConditionalBranchController.
     *  @param guard The guard for the guarded communication statement
     *   represented by this object.
     *  @param port The IOPort containing the channel (and thus receiver)
     *   that this branch will try to rendezvous with.
     *  @param channel The channel in the IOPort that this branch is
     *   trying to rendezvous with.
     *  @param branch The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @param cbc The ConditionalBranchController that this branch uses.
     *  @exception IllegalActionException If the channel has more
     *   than one receiver or if the receiver is not of type CSPReceiver.
     */
    public ConditionalReceive(boolean guard, IOPort port, int channel,
            int branch, ConditionalBranchController cbc)
                    throws IllegalActionException {
        super(guard, port, branch, cbc);
        _init(port, channel);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The run method has roughly three parts: (1) where there is already
     *  a put waiting, (2) where there is a ConditionalSend waiting, and
     *  (3) where the ConditionalReceive is the first to arrive at the
     *  receiver.
     *  <P>
     *  The algorithm used in this method, together with some methods in
     *  ConditionalBranchController, control how conditional communication
     *  takes place in the CSP domain.
     */
    @Override
    public void run() {
        _completed = false;
        CSPReceiver receiver = (CSPReceiver) getReceivers()[0];
        AbstractBranchController controller = getController();

        // Note that the lock has to be in the first receiver
        // in a group, if this receiver is part of a group.
        Object lock = receiver._getDirector();
        synchronized (lock) {
            try {
                String identifier = "";
                if (_debugging) {
                    identifier = "ConditionalReceive: get() on "
                            + _port.getFullName() + " on channel " + _channel;
                    _debug(identifier + ": Trying conditional receive.");
                }

                if (receiver._isConditionalReceiveWaiting()
                        || receiver._isGetWaiting()) {
                    if (_debugging) {
                        _debug(identifier
                                + ": A receive or get is already waiting!");
                    }
                    // Should never happen that a get or a ConditionalReceive
                    // is already at the receiver.
                    throw new InvalidStateException(
                            ((Nameable) controller.getParent()).getName()
                            + ": ConditionalReceive branch trying to "
                            + " rendezvous with a receiver that already "
                            + " has a get or a ConditionalReceive waiting.");
                }

                // Loop until either the rendezvous succeeds or the branch fails.
                while (true) {
                    if (!isAlive()) {
                        if (_debugging) {
                            _debug(identifier + ": No longer alive");
                        }
                        receiver._setConditionalReceive(false, null, -1);
                        controller._branchFailed(getID());
                        lock.notifyAll();
                        return;
                    } else if (receiver._isPutWaiting()) {
                        if (_debugging) {
                            _debug(identifier + ": Put is waiting.");
                        }
                        if (controller._isBranchReady(getID())) {
                            // I am the branch that succeeds, so convert the conditional receive
                            // to a get.
                            // Have to reset this flag _before_ the get().
                            receiver._setConditionalReceive(false, null, -1);
                            _setToken(receiver.get());
                            _completed = true;
                            controller._branchSucceeded(getID());

                            // Rendezvous complete.
                            break; // exit while (true).
                        }
                    } else if (receiver._isConditionalSendWaiting()) {
                        if (_debugging) {
                            _debug(identifier
                                    + ": Conditional send is waiting!");
                        }
                        if (controller._isBranchReady(getID())) {
                            // receive side ok, need to check that send
                            // side also ok
                            AbstractBranchController side2 = receiver
                                    ._getOtherController();

                            if (side2 != null
                                    && side2._isBranchReady(receiver
                                            ._getOtherID())) {
                                // Convert the conditional receive to a get().
                                receiver._setConditionalReceive(false, null, -1);
                                _setToken(receiver.get());
                                // Reset the conditional send flag on the other side.
                                receiver._setConditionalSend(false, null, -1);
                                _completed = true;
                                controller._branchSucceeded(getID());
                                return;
                            } else {
                                // Release the first position here since the
                                // other side is not first.
                                controller._branchNotReady(getID());
                                lock.notifyAll();
                            }
                        }
                    }
                    // If we get here, then the receiver has neither a put()
                    // nor a conditional send waiting, or the conditional
                    // send is not first, so we mark the receiver
                    // as having a conditional receive waiting and then wait.
                    receiver._setConditionalReceive(true, controller, getID());

                    // NOTE: This may not be necessary, but it seems harmless.
                    // receiver._getDirector().notifyAll();

                    // Wait for something to happen.
                    if (_debugging) {
                        _debug("ConditionalReceive: Waiting for new information.");
                    }
                    controller._branchBlocked(receiver);
                    receiver._checkFlagsAndWait();
                    // FIXME: This is probably too soon to mark this unblocked!
                    // controller._branchUnblocked(receiver);
                } // while (true)
            } catch (InterruptedException ex) {
                receiver._setConditionalReceive(false, null, -1);
                controller._branchFailed(getID());
            } catch (TerminateProcessException ex) {
                receiver._setConditionalReceive(false, null, -1);
                controller._branchFailed(getID());
            } finally {
                receiver._getDirector().removeThread(Thread.currentThread());
            }
        } // synchronized(lock)
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if this conditional branch is ready to rendezvous
     *  or has already completed its rendezvous.
     *  @return True if the associated receivers have either a pending
     *   conditional send or a put waiting.
     */
    @Override
    protected boolean _isReady() {
        if (_completed) {
            return true;
        }
        Receiver[] receivers = getReceivers();
        synchronized (((CSPReceiver) receivers[0])._getDirector()) {
            for (int i = 0; i < receivers.length; i++) {
                if (!((CSPReceiver) receivers[i])._isPutWaiting()
                        && !((CSPReceiver) receivers[i])
                        ._isConditionalSendWaiting()) {
                    return false;
                }
            }
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the branch.
     */
    private void _init(IOPort port, int channel) throws IllegalActionException {
        _port = port;
        _channel = channel;
        Receiver[][] receivers;

        try {
            port.workspace().getReadAccess();

            if (!port.isInput()) {
                throw new IllegalActionException(port, "ConditionalReceive: "
                        + "tokens only received from an input port.");
            }

            if (channel >= port.getWidth() || channel < 0) {
                throw new IllegalActionException(port, "ConditionalReceive: "
                        + "channel index out of range.");
            }

            receivers = port.getReceivers();

            if (receivers == null || receivers[channel] == null) {
                throw new IllegalActionException(port, "ConditionalReceive: "
                        + "Trying to rendezvous with a null receiver");
            }

            if (receivers[channel].length != 1) {
                throw new IllegalActionException(port, "ConditionalReceive: "
                        + "channel " + channel + " does not have exactly "
                        + "one receiver");
            }

            if (!(receivers[channel][0] instanceof CSPReceiver)) {
                throw new IllegalActionException(port, "ConditionalReceive: "
                        + "channel " + channel + " does not have a receiver"
                        + " of type CSPReceiver.");
            }

            _setReceivers(receivers[channel]);
        } finally {
            port.workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The channel on which we are sending. */
    private int _channel;

    /** Flag indicating that this rendezvous has completed successfully. */
    private boolean _completed = false;

    /** The port from which we are sending. */
    private IOPort _port;

}
