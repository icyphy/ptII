/* Class for performing Conditional Sends.

Copyright (c) 1998-2005 The Regents of the University of California.
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
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;


//////////////////////////////////////////////////////////////////////////
//// ConditionalSend

/**
   Represents a guarded communication statement in which the
   communication is a send(). Thus is represents
   <P>
   <CENTER>guard; send() => statements </CENTER>
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
   @author  Neil Smyth
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (nsmyth)
   @Pt.AcceptedRating Green (kienhuis)
   <p>
   @see ptolemy.domains.csp.kernel.ConditionalBranch
*/
public class ConditionalSend extends ConditionalBranch implements Runnable {
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
        super(guard, port, branchID);

        Receiver[][] receivers;

        try {
            port.workspace().getReadAccess();

            if (!port.isOutput()) {
                throw new IllegalActionException(port,
                        "ConditionalSend: "
                        + "tokens only sent from an output port.");
            }

            if ((channel >= port.getWidth()) || (channel < 0)) {
                throw new IllegalActionException(port,
                        "ConditionalSend: " + "channel index out of range.");
            }

            receivers = port.getRemoteReceivers();

            if ((receivers == null) || (receivers[channel] == null)) {
                throw new IllegalActionException(port,
                        "ConditionalSend: "
                        + "Trying to rendezvous with null receiver");
            }

            if (receivers[channel].length != 1) {
                throw new IllegalActionException(port,
                        "ConditionalSend: " + "channel " + channel
                        + " does not have exactly" + " one receiver");
            }

            if (!(receivers[channel][0] instanceof CSPReceiver)) {
                throw new IllegalActionException(port,
                        "ConditionalSend: " + "channel " + channel
                        + " does not have a receiver " + "of type CSPReceiver.");
            }

            setReceiver((CSPReceiver) receivers[channel][0]);
        } finally {
            port.workspace().doneReading();
        }

        setToken(token);
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
     *  @param cbc The ConditionalBranchController that this branch uses.
     *  @exception IllegalActionException If the channel has more
     *   than one receiver or if the receiver is not of type CSPReceiver.
     */
    public ConditionalSend(boolean guard, IOPort port, int channel,
            int branchID, Token token, ConditionalBranchController cbc)
            throws IllegalActionException {
        super(guard, port, branchID, cbc);

        Receiver[][] receivers;

        try {
            port.workspace().getReadAccess();

            if (!port.isOutput()) {
                throw new IllegalActionException(port,
                        "ConditionalSend: "
                        + "tokens only sent from an output port.");
            }

            if ((channel >= port.getWidth()) || (channel < 0)) {
                throw new IllegalActionException(port,
                        "ConditionalSend: " + "channel index out of range.");
            }

            receivers = port.getRemoteReceivers();

            if ((receivers == null) || (receivers[channel] == null)) {
                throw new IllegalActionException(port,
                        "ConditionalSend: "
                        + "Trying to rendezvous with null receiver");
            }

            if (receivers[channel].length != 1) {
                throw new IllegalActionException(port,
                        "ConditionalSend: " + "channel " + channel
                        + " does not have exactly" + " one receiver");
            }

            if (!(receivers[channel][0] instanceof CSPReceiver)) {
                throw new IllegalActionException(port,
                        "ConditionalSend: " + "channel " + channel
                        + " does not have a receiver " + "of type CSPReceiver.");
            }

            setReceiver((CSPReceiver) receivers[channel][0]);
        } finally {
            port.workspace().doneReading();
        }

        setToken(token);
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
    public void run() {
        try {
            CSPReceiver receiver = getReceiver();
            ConditionalBranchController controller = getController();

            synchronized (receiver) {
                if (receiver._isConditionalSendWaiting()
                        || receiver._isPutWaiting()) {
                    // Should never happen that a put or a ConditionalSend
                    // is already at the receiver.
                    throw new InvalidStateException(((Nameable) controller
                                                            .getParent()).getName()
                            + ": ConditionalSend branch trying to rendezvous "
                            + "with a receiver that already has a put or a "
                            + "ConditionalSend waiting.");
                }

                // MAIN LOOP
                while (true) {
                    if (!isAlive()) {
                        controller._branchFailed(getID());
                        return;
                    } else if (receiver._isGetWaiting()) {
                        _arriveAfterGet(receiver, controller);
                        return;
                    } else if (receiver._isConditionalReceiveWaiting()) {
                        if (!_arriveAfterCondRec(receiver, controller)) {
                            return;
                        }
                    } else {
                        _arriveFirst(receiver, controller);
                        return;
                    }
                }
            }
        } catch (InterruptedException ex) {
            getController()._branchFailed(getID());
        } catch (TerminateProcessException ex) {
            getController()._branchFailed(getID());
        } finally {
            // Make sure that the current token doesn't get used
            // in the next rendezvous.
            setToken(null);
            getReceiver()._setConditionalSend(false, null, -1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * @param controller The conditional branch controller that controls
     *  this conditional receive.
     * @param receiver The CSPReceiver through which a rendezvous attempt is
     *  taking place.
     */
    protected boolean _arriveAfterCondRec(CSPReceiver receiver,
            ConditionalBranchController controller) throws InterruptedException {
        // CASE 2: a conditionalReceive is already waiting.
        // As this conditionalSend arrived second, it has
        // to check if both branches are "first" and if
        // so perform transfer & reset state of the receiver.
        // A ConditionalReceive may disappear,
        // so if fail to rendezvous, go back to top
        // of main loop.
        if (controller._isBranchFirst(getID())) {
            // send side ok, need to check that receive
            // side also ok
            ConditionalBranchController side2 = receiver._getOtherController();

            if ((side2 != null) && side2._isBranchFirst(receiver.getOtherID())) {
                receiver.put(getToken());
                receiver._setConditionalReceive(false, null, -1);
                controller._branchSucceeded(getID());
                return false;
            } else {
                // receive side not first, so release "first"
                controller._releaseFirst(getID());
                receiver.notifyAll();
            }
        }

        getController()._branchBlocked(this.getReceiver());
        getReceiver()._checkFlagsAndWait();
        getController()._branchUnblocked(this.getReceiver());
        return true;
    }

    /**
     * @param controller The conditional branch controller that controls
     *  this conditional receive.
     * @param receiver The CSPReceiver through which a rendezvous attempt is
     *  taking place.
     */
    protected void _arriveAfterGet(CSPReceiver receiver,
            ConditionalBranchController controller) throws InterruptedException {
        // CASE 1: a get is already waiting
        // A get cannot disappear, so once enter this
        // part of the loop stay here until branch
        // successfully rendezvous or dies.
        while (true) {
            if (controller._isBranchFirst(getID())) {
                // I am the branch that succeeds
                receiver.put(getToken());
                controller._branchSucceeded(getID());
                return;
            } else {
                getController()._branchBlocked(this.getReceiver());
                getReceiver()._checkFlagsAndWait();
                getController()._branchUnblocked(this.getReceiver());
            }

            if (!isAlive()) {
                controller._branchFailed(getID());
                return;
            }
        }
    }

    /**
     * @param controller The conditional branch controller that controls
     *  this conditional receive.
     * @param receiver The CSPReceiver through which a rendezvous attempt is
     *  taking place.
     */
    protected void _arriveFirst(CSPReceiver receiver,
            ConditionalBranchController controller) throws InterruptedException {
        // CASE 3: ConditionalSend got here before a get or a
        // ConditionalReceive. Once enter this part of main
        // loop, do not leave.
        receiver._setConditionalSend(true, controller, getID());
        getController()._branchBlocked(this.getReceiver());
        getReceiver()._checkFlagsAndWait();
        getController()._branchUnblocked(this.getReceiver());

        while (true) {
            if (!isAlive()) {
                // reset state of receiver controlling
                // conditional rendezvous
                receiver._setConditionalSend(false, null, -1);
                controller._branchFailed(getID());

                // wakes up a get if it is waiting
                receiver.notifyAll();
                return;
            } else if (controller._isBranchFirst(getID())) {
                if (receiver._isGetWaiting()) {
                    // I am the branch that succeeds
                    // Note that need to reset conditionalSend
                    // flag BEFORE doing put.
                    receiver._setConditionalSend(false, null, -1);
                    receiver.put(getToken());
                    controller._branchSucceeded(getID());
                    return;
                } else {
                    controller._releaseFirst(getID());
                    receiver.notifyAll();
                }
            }

            //cannot rendezvous this time, still alive
            getController()._branchBlocked(this.getReceiver());
            getReceiver()._checkFlagsAndWait();
            getController()._branchUnblocked(this.getReceiver());
        }
    }
}
