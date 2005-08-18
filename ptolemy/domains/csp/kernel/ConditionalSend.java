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
 @author  Neil Smyth and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Green (kienhuis)
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
    public ConditionalSend(
            IOPort port, int channel, int branchID, Token token)
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

            if ((channel >= port.getWidth()) || (channel < 0)) {
                throw new IllegalActionException(port, "ConditionalSend: "
                        + "channel index out of range.");
            }

            Receiver[][] receivers = port.getRemoteReceivers();

            if ((receivers == null) || (receivers[channel] == null)) {
                throw new IllegalActionException(port, "ConditionalSend: "
                        + "Trying to rendezvous with null receiver");
            }
            if (!(receivers[channel][0] instanceof CSPReceiver)) {
                throw new IllegalActionException(port, "ConditionalSend: "
                        + "channel " + channel + " does not have a receiver "
                        + "of type CSPReceiver.");
            }

            _receivers = receivers[channel];
            _copyIndex = 0;
            setReceiver((CSPReceiver)_receivers[_copyIndex]);
        } finally {
            port.workspace().doneReading();
        }

        setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the next receiver for this branch to rendezvous with.
     *  The first time this is called, it will return the first receiver
     *  that the send should rendezvous with. The next time, it returns
     *  the next one. When there are no more receivers, return null.
     *  There is more than one receiver to rendezvous with when an
     *  output port is linked via a relation or relation group to more
     *  than one input port.
     *  @return The next receiver for this branch to rendezvous with,
     *   or null if there are no more.
     */
    public CSPReceiver getReceiver() {
        CSPReceiver result = super.getReceiver();
        if (_copyIndex < _receivers.length - 1) {
            _copyIndex += 1;
            setReceiver((CSPReceiver)_receivers[_copyIndex]);
        } else {
            setReceiver(null);
        }
        return result;
    }
    
    /** Return an array with all the receivers that
     *  this branch is trying to rendezvous with.
     *  This includes all the receivers that are destinations
     *  of the port and channel specified in the constructor.
     *  @return An array of receivers that this branch is trying to rendezvous with.
     */
    public Receiver[] getReceivers() {
        return _receivers;
    }

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
        // Get the first receiver in the set of receivers that
        // are being broadcast to.
        CSPReceiver receiver = getReceiver();
        ConditionalBranchController controller = getController();
        String identifier = "";
        if (_debugging) {
            identifier = "send() on " + _port.getFullName() + " on channel " + _channel;
            _debug(identifier + ": Trying conditional send.");
        }
        // Flag that we use to keep track of whether any one of the puts
        // to forked receivers has succeeded.
        boolean putSucceeded = false;
        try {
            // FIXME: This needs to implement a mult-way rendezvous with all receivers.
            while (receiver != null) {
                synchronized (receiver) {
                    if (receiver._isConditionalSendWaiting() || receiver._isPutWaiting()) {
                        // Should never happen that a put or a ConditionalSend
                        // is already at the receiver. This would mean there
                        // was more than one output connected to input port.
                        throw new InvalidStateException(((Nameable) controller
                                .getParent()).getName()
                                + ": ConditionalSend branch trying to rendezvous "
                                + "with a receiver that already has a put or a "
                                + "ConditionalSend waiting.");
                    }
                    
                    // Loop until either the rendezvous succeeds or the branch
                    // is no longer alive (presumably because some other branch succeeded).
                    while (true) {
                        if (!isAlive()) {
                            if (_debugging) {
                                _debug("send() on channel " + _channel + ": No longer alive.");
                            }
                            receiver._setConditionalSend(false, null, -1);
                            controller._branchFailed(getID());
                            receiver.notifyAll();
                            // Do not attempt the remaining receivers.
                            return;
                        }
                        if (receiver._isGetWaiting()) {
                            if (_debugging) {
                                _debug("send() on channel " + _channel + ": get() is waiting at "
                                        + receiver.getContainer().getFullName());
                            }
                            // The get will not disappear, so we can do a local loop.
                            while(true) {
                                if (controller._isBranchFirst(getID())) {
                                    // I am the branch that succeeds
                                    if (_debugging) {
                                        _debug("send() on channel " + _channel + ": Putting token.");
                                    }
                                    receiver._setConditionalSend(false, null, -1);
                                    receiver.put(getToken());
                                    putSucceeded = true;
                                    break;
                                }
                                // Wait for the next event.
                                controller._branchBlocked(receiver);
                                receiver._checkFlagsAndWait();
                                controller._branchUnblocked(receiver);
                                
                                // If I am no longer alive, then quit.
                                if (!isAlive()) {
                                    if (_debugging) {
                                        _debug("send() on channel " + _channel + ": No longer alive.");
                                    }
                                    controller._branchFailed(getID());
                                    receiver._setConditionalSend(false, null, -1);
                                    // Do not attempt the remaining receivers.
                                    return;
                                }
                            }
                            // Go on to the next receiver.
                            break;
                        } else if (receiver._isConditionalReceiveWaiting()) {
                            if (_debugging) {
                                _debug("send() on channel " + _channel
                                        + ": conditional receive is waiting at "
                                        + receiver.getContainer().getFullName());
                            }
                            if (controller._isBranchFirst(getID())) {
                                // Send side ok, need to check that receive side also ok
                                ConditionalBranchController side2 = receiver._getOtherController();
                                if ((side2 != null) && side2._isBranchFirst(receiver._getOtherID())) {
                                    if (_debugging) {
                                        _debug("send() on channel " + _channel + ": Putting token.");
                                    }
                                    receiver.put(getToken());
                                    receiver._setConditionalReceive(false, null, -1);
                                    putSucceeded = true;
                                    // Go on to the next receiver.
                                    break;
                                } else {
                                    // Receive side not first, so release "first", but only
                                    // if we haven't already succeeded in completing a rendezvous.
                                    // If we have, then we are committed to being first, and
                                    // have to stick to it.
                                    if (!putSucceeded) {
                                        controller._releaseFirst(getID());
                                        receiver.notifyAll();
                                    }
                                    // Wait for something to happen.
                                    controller._branchBlocked(receiver);
                                    receiver._checkFlagsAndWait();
                                    controller._branchUnblocked(receiver);
                                }
                            }
                        } else {
                            // Arriving first.
                            if (_debugging) {
                                _debug("send() on channel " + _channel + ": No request yet at "
                                        + receiver.getContainer().getFullName());
                            }
                            receiver._setConditionalSend(true, controller, getID());                            

                            // Wait for something to happen.
                            controller._branchBlocked(receiver);
                            receiver._checkFlagsAndWait();
                            controller._branchUnblocked(receiver);

                            while(true) {
                                if (!isAlive()) {
                                    if (_debugging) {
                                        _debug("send() on channel " + _channel + ": No longer alive.");
                                    }
                                    receiver._setConditionalSend(false, null, -1);
                                    controller._branchFailed(getID());
                                    receiver.notifyAll();
                                    // Do not attempt the remaining receivers.
                                    return;
                                } else if (controller._isBranchFirst(getID())) {
                                    if (receiver._isGetWaiting()) {
                                        if (_debugging) {
                                            _debug("send() on channel " + _channel + ": get() is waiting. Put token.");
                                        }
                                        // I am the branch that succeeds
                                        // Note that need to reset conditionalSend
                                        // flag BEFORE doing put.
                                        receiver._setConditionalSend(false, null, -1);
                                        receiver.put(getToken());
                                        putSucceeded = true;
                                        // Done with this receiver.
                                        break;
                                    } else {
                                        // If we have already succeeded with another
                                        // receiver, then we are committed to this branch.
                                        // Do not yield the first position.
                                        if (!putSucceeded) {
                                            controller._releaseFirst(getID());
                                            receiver.notifyAll();
                                        }
                                    }
                                }

                                //cannot rendezvous this time, still alive
                                controller._branchBlocked(receiver);
                                receiver._checkFlagsAndWait();
                                controller._branchUnblocked(receiver);
                            }
                            // Go on to the next receiver.
                            break;
                        }
                    } // while(true)
                } // synchronized(receiver)
                receiver._setConditionalSend(false, null, -1);
                // If the branch failed on the current receiver, it should
                // not try the remaining ones, and in particular should not
                // notify again that the branch failed.
                if (!isAlive()) {
                    break;
                }
                // Get the next receiver with which to rendezvous, if there is one.
                receiver = getReceiver();
            }
            if (putSucceeded) {
                // This should be called at most once, after all puts in
                // a forked set have suceeded.
                controller._branchSucceeded(getID());
            } else {
                controller._branchFailed(getID());                
            }
        } catch (InterruptedException ex) {
            // Fall out.
        } catch (TerminateProcessException ex) {
            // Fall out.
        } finally {
            // Make sure that the current token doesn't get used
            // in the next rendezvous.
            setToken(null);
            // If we exited with an exception, we may not have set the
            // state of the receiver properly.
            if (receiver != null) {
                receiver._setConditionalSend(false, null, -1);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The channel on which we are sending. */
    private int _channel;
    
    /** The index of the receiver currently being sent to. */
    private int _copyIndex = 0;

    /** The array of receivers being sent to. */
    private Receiver[] _receivers;
    
    /** The port from which we are sending. */
    private IOPort _port;
}
