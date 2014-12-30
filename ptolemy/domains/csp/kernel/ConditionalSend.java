/* Class for performing Conditional Sends.

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// ConditionalSend

/**
 Represents a guarded communication statement in which the
 communication is a send(). 

 <p>Thus is represents:</p>
 <center>guard; send() =&gt; statements </center>

 <p> It is one branch of either a CDO or a CIF conditional
 communication construct.</p>

 <p> The branches used in a conditional communication construct are
 controlled by the chooseBranch() method of ConditionalBranchController.</p>

 <p> Each branch is created to perform one communication. If more than
 one branch is enabled (the guard is true or absent), then a thread
 is created for each enabled branch to try and perform
 the appropriate rendezvous. If the branch
 succeeds and is allowed to rendezvous, then it registers itself with
 the controller and the thread it is running in dies. Otherwise it
 continues to try and rendezvous until it succeeds or it is notified that
 another branch has succeeded in with its rendezvous, in which case this
 branch has failed and the thread it is running in dies.</p>
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
 <I>Case 1:</I> There is a get already waiting at the rendezvous point. In
 this case the branch attempts to register itself, with the controller, as
 the first branch ready to rendezvous. If it succeeds, it performs the
 rendezvous, notifies the controller that it succeeded and returns. If it
 is not the first, it keeps on trying to register itself until it
 finally succeeds or another branch succeeds with a rendezvous in which
 case it fails and terminates. Note that a put cannot "go away" so it
 remains in an inner-loop trying to rendezvous or failing.
 <br>
 <I>Case 2:</I> There is a conditional receive waiting. In this case it tries to
 register both branches with their controllers as the first to try. If it
 succeeds it performs the transfer, notifies the controller and returns. It
 performs the registration in two steps, first registering this branch and
 then registering the other branch. If it successfully registers this branch,
 but cannot register the other, it unregisters itself as the first branch
 trying, and starts trying to rendezvous from the beginning. This is because
 the conditional send could "go away". If it is unable to register itself as
 the first branch to try, it again starts trying to rendezvous from the
 beginning.
 <br>
 <I>Case 3:</I> If there is neither a get or a conditional receive waiting,
 it sets a flag in the receiver that a conditional send is trying to
 rendezvous. It then waits until a get is executed on the receiver, or
 until another branch succeeds and this branch fails. If this branch fails,
 it resets the flag in the receiver, notifies the controller and
 returns. Note that it only needs to wait on a get as if a
 conditional receive is executed on the receiver, it is the branch
 which is responsible for checking that the rendezvous can proceed. Thus,
 in the case where two conditional branches are trying to rendezvous at
 a receiver, it is the responsibility of the branch arriving
 second to check that the rendezvous can proceed(see case 2).
 <p>
 @author  Neil Smyth and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (eal)
 <p>
 @see ptolemy.domains.csp.kernel.ConditionalBranch
 */
public class ConditionalSend extends ConditionalBranch implements Runnable {

    // FIXME: Need to review for major changes to support forked connections.

    /** Create a conditional send.
     *  @param port The IOPort containing the channel (and thus receiver)
     *   that this branch will try to rendezvous with.
     *  @param channel The channel in the IOPort that this branch is
     *   trying to rendezvous with.
     *  @param branchID The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @param token The token this branch is trying to send.
     *  @exception IllegalActionException If the channel has more
     *   than one receiver or if the receiver is not of type CSPReceiver.
     */
    public ConditionalSend(IOPort port, int channel, int branchID, Token token)
            throws IllegalActionException {
        this(true, port, channel, branchID, token, null);
    }

    /** Create a guarded communication with a send communication.
     *  @param guard The guard for the guarded communication statement
     *   represented by this object.
     *  @param port The IOPort containing the channel (and thus receiver)
     *   that this branch will try to rendezvous with.
     *  @param channel The channel in the IOPort that this branch is
     *   trying to rendezvous with.
     *  @param branchID The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @param token The token this branch is trying to send.
     *  @exception IllegalActionException If the channel has more
     *   than one receiver or if the receiver is not of type CSPReceiver.
     */
    public ConditionalSend(boolean guard, IOPort port, int channel,
            int branchID, Token token) throws IllegalActionException {
        this(guard, port, channel, branchID, token, null);
    }

    /** Create a guarded communication with a send communication. This
     *  constructor allows actors which are not CSPActors access to
     *  CSP functionality by providing their own ConditionalBranchController.
     *  @param guard The guard for the guarded communication statement
     *   represented by this object.
     *  @param port The IOPort containing the channel (and thus receiver)
     *   that this branch will try to rendezvous with.
     *  @param channel The channel in the IOPort that this branch is
     *   trying to rendezvous with.
     *  @param branchID The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @param token The token this branch is trying to send.
     *  @param controller The controller that this branch uses.
     *  @exception IllegalActionException If the channel has more
     *   than one receiver or if the receiver is not of type CSPReceiver.
     */
    public ConditionalSend(boolean guard, IOPort port, int channel,
            int branchID, Token token, ConditionalBranchController controller)
                    throws IllegalActionException {
        super(guard, port, branchID, controller);
        _port = port;
        _channel = channel;

        try {
            port.workspace().getReadAccess();

            if (!port.isOutput()) {
                throw new IllegalActionException(port, "ConditionalSend: "
                        + "tokens only sent from an output port.");
            }

            if (channel >= port.getWidth() || channel < 0) {
                throw new IllegalActionException(port, "ConditionalSend: "
                        + "channel index out of range.");
            }

            Receiver[][] receivers = port.getRemoteReceivers();

            if (receivers == null || receivers[channel] == null) {
                throw new IllegalActionException(port, "ConditionalSend: "
                        + "Trying to rendezvous with null receiver");
            }
            if (!(receivers[channel][0] instanceof CSPReceiver)) {
                throw new IllegalActionException(port, "ConditionalSend: "
                        + "channel " + channel + " does not have a receiver "
                        + "of type CSPReceiver.");
            }
            _setReceivers(receivers[channel]);
        } finally {
            port.workspace().doneReading();
        }

        _setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The run method has roughly three parts: (1) when there is already
     *  a get waiting, (2) when there is a ConditionalReceive waiting, and
     *  (3) where this ConditionalSend is the first to arrive at the
     *  receiver.
     *  <P>
     *  The algorithm used in this method, together with some methods in
     *  ConditionalBranchController, control how conditional communication
     *  takes place in the CSP domain.
     */
    @Override
    public void run() {
        // Get the array of receivers to send to.
        Receiver[] receivers = getReceivers();
        CSPDirector director = ((CSPReceiver) receivers[0])._getDirector();
        AbstractBranchController controller = getController();
        String identifier = "";
        if (_debugging) {
            identifier = "ConditionalSend: send() on " + _port.getFullName()
                    + " on channel " + _channel;
            _debug(identifier + ": Trying conditional send.");
        }
        // Note that there are two distinct sources of complexity here.
        // First, this conditional send may have multiple destinations.
        // This means that it will need to perform a multi-way rendezvous
        // with each of those destinations. Second, it is part of conditional
        // send, which means that another conditional branch (send or receive)
        // may win out, in which case this send will "fail".
        // For the multi-way rendezvous, we always synchronize
        // on the first receiver in the group.
        synchronized (director) {
            try {
                // Check that none of the receivers already has a put or conditional send waiting
                for (Receiver receiver : receivers) {
                    if (((CSPReceiver) receiver)._isConditionalSendWaiting()
                            || ((CSPReceiver) receiver)._isPutWaiting()) {
                        // Should never happen that a put or a ConditionalSend
                        // is already at the receiver. This would mean there
                        // was more than one output connected to input port.
                        throw new InvalidStateException(
                                ((Nameable) controller.getParent()).getName()
                                + ": ConditionalSend branch is trying to rendezvous "
                                + "with a receiver that already has a put or a "
                                + "ConditionalSend waiting.");
                    }
                }
                // Loop until either the rendezvous succeeds or the branch
                // is no longer alive (presumably because some other branch succeeded).
                // I.e., the branch "fails".
                while (true) {
                    if (!isAlive()) {
                        if (_debugging) {
                            _debug("ConditionalSend: send() on channel "
                                    + _channel + ": No longer alive.");
                        }
                        for (Receiver receiver : receivers) {
                            ((CSPReceiver) receiver)._setConditionalSend(false,
                                    null, -1);
                        }
                        controller._branchFailed(getID());
                        director.notifyAll();
                        // Nothing more to do.
                        return;
                    }
                    if (_isGetWaitingOnAll(receivers)) {
                        if (_debugging) {
                            _debug("ConditionalSend: send() on channel "
                                    + _channel
                                    + ": get() is waiting on all receivers.");
                        }
                        if (controller._isBranchReady(getID())) {
                            // I am the branch that succeeds, so convert the conditional send
                            // to a put on each receiver.
                            // The order doesn't matter here, since all recipients are waiting.
                            if (_debugging) {
                                _debug("ConditionalSend: send() on channel "
                                        + _channel + ": Putting token.");
                            }
                            // Convert the conditional send to a put.
                            // Do this by falling out of the loop.
                            break; // exit while (true).
                        }
                    } else if (_isGetOrConditionalReceiveWaitingOnAll(receivers)) {
                        if (_debugging) {
                            _debug("ConditionalSend: send() on channel "
                                    + _channel
                                    + ": conditional receive or get is waiting on each destination.");
                        }
                        if (controller._isBranchReady(getID())) {
                            if (_debugging) {
                                _debug("ConditionalSend: send() on channel "
                                        + _channel + ": send branch is first.");
                            }
                            // Send side OK, need to check that receive side also OK.
                            // It has to be OK for all the receivers, so we have to keep
                            // track of which ones say they are first so we can release
                            // the flag indicating their first if we don't have unanimity.
                            List markedFirst = new LinkedList();
                            boolean succeeded = true;
                            for (Receiver receiver2 : receivers) {
                                CSPReceiver receiver = (CSPReceiver) receiver2;
                                if (receiver._isConditionalReceiveWaiting()) {
                                    AbstractBranchController side2 = receiver
                                            ._getOtherController();
                                    if (side2 != null
                                            && side2._isBranchReady(receiver
                                                    ._getOtherID())) {
                                        if (_debugging) {
                                            _debug("ConditionalSend: send() on channel "
                                                    + _channel
                                                    + ": the other side is also first: "
                                                    + side2.getParent()
                                                    .getFullName());
                                        }
                                        markedFirst.add(receiver);
                                    } else {
                                        if (_debugging) {
                                            if (side2 != null) {
                                                _debug("ConditionalSend: send() on channel "
                                                        + _channel
                                                        + ": the other side is NOT first: "
                                                        + side2.getParent()
                                                        .getFullName());
                                            } else {
                                                _debug("ConditionalSend: send() on channel "
                                                        + _channel
                                                        + ": THERE IS NO OTHER SIDE CONTROLLER!");
                                            }
                                        }
                                        succeeded = false;
                                        break;
                                    }
                                }
                            }
                            if (succeeded) {
                                if (_debugging) {
                                    _debug("ConditionalSend: send() on channel "
                                            + _channel + ": Putting token.");
                                }
                                // Convert the conditional send to a put.
                                // Do this by falling out of the loop.
                                break; // exit while (true).
                            } else {
                                // At least one conditional receive is not first.
                                // Release those that have grabbed the "first" flag,
                                // including of course this controller.
                                controller._branchNotReady(getID());
                                Iterator iterator = markedFirst.iterator();
                                while (iterator.hasNext()) {
                                    CSPReceiver receiver = (CSPReceiver) iterator
                                            .next();
                                    AbstractBranchController side2 = receiver
                                            ._getOtherController();
                                    side2._branchNotReady(receiver
                                            ._getOtherID());
                                }
                                director.notifyAll();
                            }
                        }
                    }

                    // If we get here, then at least one receiver has neither a get()
                    // nor a conditional receive waiting, so we mark the receivers
                    // as having a conditional send waiting and then wait.
                    for (Receiver receiver : receivers) {
                        ((CSPReceiver) receiver)._setConditionalSend(true,
                                controller, getID());
                    }

                    // FIXME: Is this necessary?
                    director.notifyAll();

                    if (_debugging) {
                        _debug("ConditionalSend: Waiting for new information.");
                    }

                    // Wait for something to happen.
                    // The null argument is because we are not blocked on a specific
                    // receiver, but rather on multiple receivers.
                    controller._branchBlocked(null);
                    ((CSPReceiver) receivers[0])._checkFlagsAndWait();
                    controller._branchUnblocked(null);
                } // while (true)

                // When we get here, it is time to convert the conditional
                // send to a put.

                // Have to reset the conditional send flag _before_ the put().
                for (Receiver receiver : receivers) {
                    ((CSPReceiver) receiver)._setConditionalSend(false, null,
                            -1);
                    // Reset the other side's conditional receive flag.
                    // NOTE: This used to be done after the putToAll, outside
                    // the synchronized block, but that led to unpredictable
                    // behavior.  Why?
                    ((CSPReceiver) receiver)._setConditionalReceive(false,
                            null, -1);
                }

                receivers[0].putToAll(getToken(), receivers);

                if (_debugging) {
                    _debug("ConditionalSend: put is complete.");
                }

                // This should be called at most once, after all puts in
                // a forked set have suceeded.
                controller._branchSucceeded(getID());
            } catch (Throwable throwable) {
                controller._branchFailed(getID());
                // If we exited with an exception, we may not have set the
                // state of the receiver properly.
                for (Receiver receiver : receivers) {
                    ((CSPReceiver) receiver)._setConditionalSend(false, null,
                            -1);
                }
            } finally {
                // Make sure that the current token doesn't get used
                // in the next rendezvous.
                _setToken(null);

                // Notify the director that this thread has exited.
                director.removeThread(Thread.currentThread());
            }
        } // synchronized
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return whether all specified receivers
     *  have either a conditional receive waiting or a get waiting.
     *  @param receivers The receivers to check.
     *  @return True all specified receivers have either a
     *   conditional receive or a get waiting.
     */
    protected boolean _isGetOrConditionalReceiveWaitingOnAll(
            Receiver[] receivers) {
        for (int i = 0; i < receivers.length; i++) {
            // If any member of the group is not waiting,
            // we can return false.
            if (!((CSPReceiver) receivers[i])._isConditionalReceiveWaiting()
                    && !((CSPReceiver) receivers[i])._isGetWaiting()) {
                return false;
            }
        }
        return true;
    }

    /** Return whether a get() is waiting to rendezvous at all the
     *  specified receivers.
     *  @param receivers The receivers to check.
     *  @return True if a get() is waiting to rendezvous.
     */
    protected boolean _isGetWaitingOnAll(Receiver[] receivers) {
        for (int i = 0; i < receivers.length; i++) {
            // If any member of the group is not waiting,
            // we can return false.
            if (!((CSPReceiver) receivers[i])._isGetWaiting()) {
                return false;
            }
        }
        return true;
    }

    /** Return true if this conditional branch is ready to rendezvous.
     *  @return True if the associated receivers all have either a pending
     *   conditional receive or a get waiting.
     */
    @Override
    protected boolean _isReady() {
        Receiver[] receivers = getReceivers();
        for (int i = 0; i < receivers.length; i++) {
            if (!((CSPReceiver) receivers[i])._isGetWaiting()
                    && !((CSPReceiver) receivers[i])
                    ._isConditionalReceiveWaiting()) {
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The channel on which we are sending. */
    private int _channel;

    /** The port from which we are sending. */
    private IOPort _port;
}
