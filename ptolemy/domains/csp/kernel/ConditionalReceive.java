/* Class for performing Conditional Receives.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Green (nsmyth@eecs.berkeley.edu)
@AcceptedRating Green (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.kernel;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;


//////////////////////////////////////////////////////////////////////////
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
<I>Case 1:</I> Realized by _arriveAfterPut(). There is a put already waiting
at the rendezvous point. In this case
the branch attempts to register itself, with the controller, as the first
branch ready to rendezvous. If it succeeds, it performs the rendezvous,
notifies the controller that it succeeded and returns. If it is not the first,
it keeps on trying to register itself until it finally succeeds or another
branch successfully rendezvoused in which case it fails and terminates. Note
that a put cannot "go away" so it remains in an inner-loop trying to
rendezvous or failing.
<br>
<I>Case 2:</I> Realized by _arriveAfterCondSend(). There is a conditional send
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
<I>Case 3:</I> Realized by _arriveFirst(). If there is neither a put or a
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
@author  Neil Smyth
@version $Id$
<p>
@see ptolemy.domains.csp.kernel.ConditionalBranch
*/

public class ConditionalReceive extends ConditionalBranch implements Runnable {

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
        Receiver[][] receivers;
        try {
            port.workspace().getReadAccess();
            if (!port.isInput()) {
                throw new IllegalActionException(port, "ConditionalRec: " +
                        "tokens only received from an input port.");
            }
            if (channel >= port.getWidth() || channel < 0) {
                throw new IllegalActionException(port, "ConditionalRec: " +
                        "channel index out of range.");
            }
            receivers = port.getReceivers();
            if (receivers == null || receivers[channel] == null) {
                throw new IllegalActionException(port, "ConditionalRec: " +
                        "Trying to rendezvous with a null receiver");
            }
            if (receivers[channel].length != 1) {
                throw new IllegalActionException(port, "ConditionalRec: " +
                        "channel " + channel + " does not have exactly " +
                        "one receiver");
            }
            if (!(receivers[channel][0] instanceof CSPReceiver)) {
                throw new IllegalActionException(port, "ConditionalRec: " +
                        "channel " + channel + " does not have a receiver" +
                        " of type CSPReceiver.");
            }
            setReceiver( (CSPReceiver)receivers[channel][0]);
        } finally {
            port.workspace().doneReading();
        }
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
    public void run() {
        try {
            CSPReceiver rcvr = getReceiver();
            ConditionalBranchController controller = getController();
            synchronized( rcvr ) {
                if (rcvr._isConditionalReceiveWaiting()
                        || rcvr._isGetWaiting() ) {
                    // Should never happen that a get or a ConditionalReceive
                    // is already at the receiver.
                    throw new InvalidStateException(
			    ((Nameable)controller.getParent()).getName() +
                            ": ConditionalReceive branch trying to " +
                            " rendezvous with a receiver that already " +
                            " has a get or a ConditionalReceive waiting.");
                }

                // MAIN LOOP
                while (true) {
                    if (!isAlive()) {
                        controller._branchFailed(getID());
                        return;
                    } else if (rcvr._isPutWaiting()) {
                        _arriveAfterPut(rcvr, controller);
                        return;
                    } else if (rcvr._isConditionalSendWaiting()) {
                        if( !_arriveAfterCondSend(rcvr, controller) ) {
                            return;
                        }
                    } else {
                        _arriveFirst(rcvr, controller);
                        return;
                    }
                }
            }
        } catch (InterruptedException ex) {
            getController()._branchFailed(getID());
        } catch (TerminateProcessException ex) {
            getController()._branchFailed(getID());
        } finally {
            getReceiver()._setConditionalReceive(false, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Encounter a conditionalsend that is already waiting on this
     *  conditionalreceive. Since this conditionalreceive arrived second,
     *  check if both branches are "first" and if so perform the data
     *  transfer and reset the state of the receiver. Since a
     *  conditionalsend can "disappear," then this method can return to
     *  the top of the main loop in the run method that calls this method.
     *  Return true if this method should go to the top of the calling
     *  loop; return false otherwise.
     * @return true if this method should continue at the beginning of
     *  the loop that calls this method; otherwise return false.
     * @param controller The conditional branch controller that control this
     *  conditional receive.
     * @param rcvr The CSPReceiver through which a rendezvous attempt is
     *  taking place.
     */
    protected boolean _arriveAfterCondSend(CSPReceiver rcvr,
	    ConditionalBranchController controller)
            throws InterruptedException {
        if (controller._isBranchFirst(getID())) {
            // receive side ok, need to check that send
            // side also ok
            // CSPReceiver rec = getReceiver();
            if (rcvr._getOtherController()._isBranchFirst(getID())) {
                setToken( rcvr.get() );
                rcvr._setConditionalSend(false, null);
                controller._branchSucceeded(getID());
                return false;
            } else {
                controller._releaseFirst(getID());
                rcvr.notifyAll();
            }
        }
        getController()._readBranchBlocked(true);
        getReceiver()._checkFlagsAndWait();
        getController()._readBranchUnblocked(true);
        return true;
    }

    /** Encounter a non-conditional put that is already waiting on this
     *  conditionalreceive. Since a non-conditional put can not disappear,
     *  wait (via a while loop) until it successfully rendezvouses or dies.
     * @param controller The conditional branch that control this conditional
     *  receive.
     * @param rcvr The CSPReceiver through which a rendezvous attempt is
     *  taking place.
     */
    protected void _arriveAfterPut(CSPReceiver rcvr,
            ConditionalBranchController controller)
            throws InterruptedException {
        while (true) {
            if (controller._isBranchFirst(getID())) {
                // I am the branch that succeeds
                setToken( rcvr.get() );
                controller._branchSucceeded(getID());
                return;
            }
            getController()._readBranchBlocked(true);
            getReceiver()._checkFlagsAndWait();
            getController()._readBranchUnblocked(true);
            if (!isAlive()) {
                controller._branchFailed(getID());
                return;
            }
        }
    }

    /** Begin a rendezvous attempt prior to any other conditionalsends
     *  or (non-conditional) put attempts. Wait until a put or
     *  conditionalsend attempt is made by another branch.
     * @param controller The conditional branch controller that control this
     *  conditional receive.
     * @param rcvr The CSPReceiver through which a rendezvous attempt is
     *  taking place.
     */
    protected void _arriveFirst(CSPReceiver rcvr,
            ConditionalBranchController controller)
            throws InterruptedException {
        rcvr._setConditionalReceive(true, controller);
        getController()._readBranchBlocked(true);
        getReceiver()._checkFlagsAndWait();
        getController()._readBranchUnblocked(true);
        while (true) {
            if (!isAlive()) {
                // reset state of receiver
                // CSPReceiver rec = getReceiver();
                rcvr._setConditionalReceive(false, null);
                controller._branchFailed(getID());
                // wakes up a put if it is waiting
                rcvr.notifyAll();
                return;
            } else if (rcvr._isPutWaiting()) {
                if (controller._isBranchFirst(getID())) {
                    // I am the branch that succeeds
                    // Note that need to reset condSend
                    // flag BEFORE doing put.
                    // CSPReceiver rec = getReceiver();
                    rcvr._setConditionalReceive(false, null);
                    setToken( rcvr.get() );
                    controller._branchSucceeded(getID());
                    return;
                }
            }
            //can't rendezvous this time, but still alive
            getController()._readBranchBlocked(true);
            getReceiver()._checkFlagsAndWait();
            getController()._readBranchUnblocked(true);
        }
    }
}
