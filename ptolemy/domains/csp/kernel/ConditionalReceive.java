/* Class for performing Conditional Receives.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating none

*/

package ptolemy.domains.csp.kernel;

import ptolemy.actor.*;
import ptolemy.data.Token;
import ptolemy.kernel.util.*;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// ConditionalReceive
/**
Class for executing a conditional receive in a separate thread.
For rendezvous, the receiver is the key synchronization point.
ConditionalReceive branches are designed to be used once. Upon instantiation,
private members are set to the receiver to try to receive from, the parent
object they are performing the conditional rendezvous for, and the
identification number of the branch according to the parent.
<p>
A ConditionalReceive branch is created to perform a single conditional
communication. The information it contains in its private members is
immutable and fixed upon creation. This class is designed to be executed
in a separate thread. If it succeeds in rendezvousing, it notifies the
parent actor and terminates. If it cannot rendezvous immediately, it waits
until it can rendezvous or until it receives notification that another
branch has succeeded , in which case it notifies the parent actor that
it failed to rendezvous and terminates.
<p>
There are roughly three parts to the run method, each of which is relevant
to the different rendezvous scenarios.
<br>
Case 1: There is a put already waiting at the rendezvous point. In this case
the branch attempts to register itself, with the parent actor, as the first
branch ready to rendezvous. If it succeeds it performs the rendezvous,
notifies the parent that it succeeded and returns. If it is not the first, it
keeps on trying to register itself until it finally succeeds or another
branch successfully rendezvoused in which case it fails and terminates. Note
that a put cannot "go away" so it remains in an innerloop trying to
rendezvous or failing.
<br>
Case 2: There is a conditional send waiting. In this case it tries to
register both branches with their parents as the first to try. If it
succeeds it performs the transfer, notifies the parent and returns. It
performs the registration in two steps, first registering this branch and
then registering the other branch. If it successfully registers this branch,
but cannot register the other, it unregisters itself as the first branch
trying, and starts trying to rendezvous from the beginning. This is because
the conditional send could "go away". If it is unable to register itself as
the first branch to try, it again starts trying to rendezvous from the
beginning.
<br>
Case 3: If there is neither a put or a conditional send waiting, it sets a
flag in the receiver that a conditional receive is trying to rendezvous. It
then waits until a put is executed on the receiver, or until another branch
succeeds and this branch fails. If this branch fails, it resets the flag in
the receiver, notifies the parent actor and returns. Note that it only needs
to wait on a put as if a conditional send is executed on the receiver, it is
the branch which is responsible for checking that the rendezvous can proceed.
Thus, in the case where two conditional branches are trying to rendezvous
at a receiver, it is the responsibilty of the branch arriving second to
check that the rendezvous can proceed(see case 2).
<p>
@author  Neil Smyth
@version $Id$

*/

public class ConditionalReceive extends ConditionalBranch implements Runnable {

    /** Create a conditional receive branch.
     *  FIXME: perhaps could do away with a lot of these tests if conditional
     *  branches are only called by parser generated code?
     *  @param port The IOPort containing the channel (and thus receiver)
     *   that this branch will try to rendezvous with.
     *  @param channel The channel in the IOPort that this branch is
     *   trying to rendezvous with.
     *  @param branch The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @exception IllegalActionException thrown if the channel has more
     *   than one receiver or if the receiver is not of type CSPReceiver.
     */
    public ConditionalReceive(IOPort port, int channel, int branch)
          throws IllegalActionException {
         super(port, branch);
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
             _receiver = (CSPReceiver)receivers[channel][0];

	 } finally {
             port.workspace().doneReading();
	 }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** The run method has roughly three parts: (1) where there is already
     *  a put waiting, (2) where there is a conditional send waiting, and
     *  (3) where the conditional receive is the first to arrive at the
     *  receiver.
     */
    public void run() {
        try {
            // FIXME: For testing purposes only. Needed so that threads are 
            // not always executed in the same order.
            // Random rand = new Random();
            //Thread.currentThread().sleep((long)(rand.nextDouble()*1000));

            Token res = null;

            synchronized(getReceiver()) {
                if (getReceiver()._isConditionalReceiveWaiting()
                     || getReceiver()._isGetWaiting() ) {
                    // Should never happen that a get or a ConditionalReceive 
                    // is already at the receiver.
                    throw new InvalidStateException(getParent().getName() +
                         ": ConditionalReceive branch trying to rendezvous " +
                         "with a receiver that already has a get or a " + 
                         "ConditionalReceive waiting.");
                }

                // MAIN LOOP
                while (true) {
                    if (!isAlive()) {
                        getParent()._branchFailed(getID());
                        return;
                    } else if (getReceiver()._isPutWaiting()) {
                        // CASE 1: a put is already waiting at the receiver.
                        // A put cannot disappear so remain in this part
                        // of the loop until this branch successfully
                        // rendezvous or dies.
                        while (true) {
                            if (getParent()._amIFirst(getID())) {
                                // I am the branch that succeeds
                                res = getReceiver().get();
                                getParent()._branchSucceeded(getID(), res);
                                return;
                            } 
                            _checkAndWait();
                            if (!isAlive()) {
                                getParent()._branchFailed(getID());
                                return;
                            }
                        }
                    } else if (getReceiver()._isConditionalSendWaiting()) {
                        // CASE 2: a conditionalSend is already waiting. As
                        // this condionalReceive arrived second, it has to
                        // check if both branches are "first" and if so
                        // perform transfer & reset state of the receiver.
                        // A ConditionalSend can "disappear" so this
                        // part of the loop can exit & return to the top of
                        // main loop.
                        int id = getID();
                        if (getParent()._amIFirst(id)) {
                            // receive side ok, need to check that send
                            // side also ok
                            CSPReceiver rec = getReceiver();
                            if (rec._getOtherParent()._amIFirst(id)) {
                                res = getReceiver().get();
                                rec._setConditionalSend(false, null);
                                getParent()._branchSucceeded(id, res);
                                return;
                            } else {
                                getParent()._releaseFirst(getID());
                                getReceiver().notifyAll();
                            }
                        }
                        _checkAndWait();
                    } else {
                        // CASE 3: ConditionalReceive tried to rendezvous
                        // before a put or a ConditionalSend.
                        getReceiver()._setConditionalReceive(true,getParent());
                        _checkAndWait();
                        while (true) {
                            if (!isAlive()) {
                                // reset state of receiver
                                CSPReceiver rec = getReceiver();
                                rec._setConditionalReceive(false, null);
                                getParent()._branchFailed(getID());
                                return;
                            } else if (getReceiver()._isPutWaiting()) {
                                if (getParent()._amIFirst(getID())) {
                                    // I am the branch that succeeds
                                    // Note that need to reset condSend 
                                    // flag BEFORE doing put.
                                    CSPReceiver rec = getReceiver();
                                    rec._setConditionalReceive(false, null);
                                    res = getReceiver().get();
                                    getParent()._branchSucceeded(getID(), res);
                                    return;
                                }
                            }
                            //can't rendezvous this time, but still alive
                            _checkAndWait();
                        }
                    }
                }
            }
        } catch (InterruptedException ex) {
            System.out.println( getParent().getName() + 
                    ": ConditionalReceive interrupted: " + ex.getMessage());
            getParent()._branchFailed(getID());
        } catch (TerminateProcessException ex) {
            System.out.println("ConditionalRecieve terminated by exception.");
            getParent()._branchFailed(getID());
        } finally {
            getReceiver()._setConditionalReceive(false, null);
        }
    }
}





