/* Class for performing Conditional Sends.

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
//// ConditionalSend
/**
Represents a guarded communication statement in which the 
communication is a send(). Thus is represents 
<br>
     guard; send() => statements
<br>
It is one branch of either a CDO or a CIF conditional 
communication construct. 
<p>
The branches used in a conditional communication construct are 
controlled by the chooseBranch() method of CSPActor. Thus any actor 
that wishes to use a CDO or a CIF must derive from CSPActor.
<p>
Each branch is created to perfrom one communication. If more than 
one branch is enabled (the guard is true or absent), then a thread 
is created for each enabled branch to try and perfrom 
the appropriate rendezvous. If the branch 
succeeds and is allowed to rendezvous, then it registers itself with 
the parent actor and the thread it is running in dies. Otherwsise it 
continues to trya nd rendezvous until it suceeds or it is notified that 
another branch has suceeded in with its rendezvous, in which case this 
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
Case 1: There is a get already waiting at the rendezvous point. In this case
the branch attempts to register itself, with the parent actor, as the first
branch ready to rendezvous. If it succeeds it performs the rendezvous,
notifies the parent that it succeeded and returns. If it is not the first, it
keeps on trying to register itself until it finally succeeds or another
branch successfully rendezvoused in which case it fails and terminates. Note
that a put cannot "go away" so it remains in an innerloop trying to
rendezvous or failing.
<br>
Case 2: There is a conditional receive waiting. In this case it tries to
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
Case 3: If there is neither a get or a conditional receive waiting, it sets a
flag in the receiver that a conditional send is trying to rendezvous. It
then waits until a get is executed on the receiver, or until another branch
succeeds and this branch fails. If this branch fails, it resets the flag in
the receiver, notifies the parent actor and returns. Note that it only needs
to wait on a put as if a conditional receive is executed on the receiver, it
is the branch which is responsible for checking that the rendezvous can
proceed. Thus, in the case where two conditional branches are trying
to rendezvous at a receiver, it is the responsibilty of the branch arriving
second to check that the rendezvous can proceed(see case 2).
<p>
@author  Neil Smyth
@version $Id$
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
     *  @param branch The identification number assigned to this branch
     *   upon creation by the CSPActor.
     *  @exception IllegalActionException If the channel has more
     *   than one receiver or if the receiver is not of type CSPReceiver.
     */
    public ConditionalSend(boolean guard, IOPort port, int channel, 
            int branchID, Token t) throws IllegalActionException {
         super(guard, port, branchID);
         Receiver[][] receivers;
         try {
             port.workspace().getReadAccess();
             if (!port.isOutput()) {
                 throw new IllegalActionException(port, "ConditionalSend: " +
                         "tokens only sent from an output port.");
             }
             if (channel >= port.getWidth() || channel < 0) {
		 throw new IllegalActionException(port, "ConditionalSend: " +
                         "channel index out of range.");
             }
             receivers = port.getRemoteReceivers();
             if (receivers == null || receivers[channel] == null) {
                  throw new IllegalActionException(port, "ConditionalSend: " +
                          "Trying to rendezvous with null receiver");
             }
             if (receivers[channel].length != 1) {
                 throw new IllegalActionException(port, "ConditionalSend: " +
                         "channel " + channel + " does not have exactly" +
                         " one receiver");
             }
             if (!(receivers[channel][0] instanceof CSPReceiver)) {
                 throw new IllegalActionException(port,"ConditionalSend: " +
                         "channel " + channel + " does not have a receiver " +
                         "of type CSPReceiver." );
             }
             _receiver = (CSPReceiver)receivers[channel][0];
	 } finally {
             port.workspace().doneReading();
	 }
	 _token = t;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** The run method has roughly three parts: (1) where there is already
     * a get waiting, (2) where there is a conditional receive waiting, and
     * (3) where the conditional send is the first to arrive at the
     * receiver.
     */
    public void run() {
        try {
            // FIXME: For testing purposes only. Needed so that threads are 
            // not always executed in the same order.
            Random rand = new Random();
            //Thread.currentThread().sleep((long)(rand.nextDouble()*1000));
               
            synchronized(getReceiver()) {
                if (getReceiver()._isConditionalSendWaiting()
                     || getReceiver()._isPutWaiting() ) {
                    // Should never happen that a put or a ConditionalSend 
                    // is already at the receiver.                 
                    throw new InvalidStateException(getParent().getName() +
                         ": ConditionalSend branch trying to rendezvous " +
                         "with a receiver that already has a put or a " + 
                         "ConditionalSend waiting.");
                }
                
                // MAIN LOOP
                while (true) {
                    if (!isAlive()) {
                        getParent()._branchFailed(getID());
                        return;
                    } else if (getReceiver()._isGetWaiting()) {
                        // CASE 1: a get is already waiting
                        // A get cannot disappear, so once enter this
                        // part of the loop stay here until branch
                        // successfully rendezvous or dies.
                       while (true) {
                            if (getParent()._amIFirst(getID())) {
                                // I am the branch that succeeds
                                getReceiver().put(_token);
                                getParent()._branchSucceeded(getID());
                                return;
                            } else {
                                _checkAndWait();
                            }
                            if (!isAlive()) {
                                getParent()._branchFailed(getID());
                                return;
                            }
                        }
                    } else if (getReceiver()._isConditionalReceiveWaiting()) {
                        // CASE 2: a conditionalReceive is already waiting.
                        // As this condionalSend arrived second, it has
                        // to check if both branches are "first" and if
                        // so perform transfer & reset state of the receiver.
                        // A ConditionalReceive may dissappear,
                        // so if fail to rendezvous, go back to top
                        // of main loop.
                        if (getParent()._amIFirst(getID())) {
                            // send side ok, need to check that receive
                            // side also ok
                            CSPReceiver rec = getReceiver();
                            CSPActor side2 = getReceiver()._getOtherParent();
                            if (side2._amIFirst(getID())) {
                                rec.put(_token);
                                rec._setConditionalReceive(false, null);
                                getParent()._branchSucceeded(getID());
                                return;
                            } else {
                                // receive side not first, so release "first"
                                getParent()._releaseFirst(getID());
                                getReceiver().notifyAll();
                            }
                        }
                        _checkAndWait();
                    } else {
                        // CASE 3: ConditionalSend got here before a get or a
                        // ConditionalReceive. Once enter this part of main
                        // loop, do not leave.
                        getReceiver()._setConditionalSend(true, getParent());
                        _checkAndWait();
                        while (true) {
                            if (!isAlive()) {
                                // reset state of receiver controlling
                                // conditional rendezvous
                                getReceiver()._setConditionalSend(false, null);
                                getParent()._branchFailed(getID());
                                // wakes up a get if it is waiting
                                getReceiver().notifyAll();
                                return;
                            } else if (getReceiver()._isGetWaiting()) {
                                if (getParent()._amIFirst(getID())) {
                                    // I am the branch that succeeds
                                    // Note that need to reset condSend 
                                    // flag BEFORE doing put.
                                    CSPReceiver rec = getReceiver();
                                    rec._setConditionalSend(false, null);
                                    rec.put(_token);
                                    getParent()._branchSucceeded(getID());
                                    return;
                                }
                            }
                            //cannot rendezvous this time, still alive
                            _checkAndWait();
                        }
                    }
                }
            }
        } catch (InterruptedException ex) {
            System.out.println(getParent().getName() + 
                    ": ConditionalSend interrupted: " + ex.getMessage());
            getParent()._branchFailed(getID());
        } catch (TerminateProcessException ex) {
            System.out.println(getParent().getName() + 
                    ": ConditionalSend terminated: " + ex.getMessage());
            getParent()._branchFailed(getID());
        } finally {
            _token = null;
            getReceiver()._setConditionalSend(false, null);
        }
    }
}






