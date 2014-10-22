/* This actor implements a StuckAt Fault with a fixed probability.

@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package org.ptolemy.faultModels;

import java.util.HashMap;

import ptolemy.actor.Actor;
import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.CommunicationAspectListener.EventType;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.aspect.AtomicCommunicationAspect;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** This actor is an {@link CommunicationAspect} that, when its
 *  ...
 *  @author Ilge Akkaya, Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class StuckAtFaultGenerator extends AtomicCommunicationAspect {

    /** Construct a Bus with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public StuckAtFaultGenerator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _tokens = new FIFOQueue();
        _receiversAndTokensToSendTo = new HashMap();
        _isStuck = new HashMap();
        _lastKnownHealthyTokens = new HashMap();
        _wrappedReceivers = new HashMap();

        stuckAtFaultProbability = new Parameter(this, "stuckAtFaultProbability");
        stuckAtFaultProbability.setExpression("0.1");
        stuckAtFaultProbability.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     */
    public IntermediateReceiver getReceiver(Receiver receiver) {
        IntermediateReceiver intermediateReceiver;
        if (_wrappedReceivers.get(receiver) == null) {
            intermediateReceiver = new IntermediateReceiver(this, receiver);
            _wrappedReceivers.put(receiver, intermediateReceiver);
        } else {
            intermediateReceiver = (IntermediateReceiver) _wrappedReceivers
                    .get(receiver);
        }

        return intermediateReceiver;
    }

    /** Create a receiver to mediate a communication via the specified receiver. This
     *  receiver is linked to a specific port of the communication aspect.
     *  @param receiver Receiver whose communication is to be mediated.
     *  @param port Port of the communication aspect.
     *  @return A new receiver.
     *  @exception IllegalActionException If the receiver cannot be created.
     */
    public Receiver getReceiver(Receiver receiver, IOPort port)
            throws IllegalActionException {
        return getReceiver(receiver);
    }

    /** If the attribute is <i>serviceTime</i>, then ensure that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == stuckAtFaultProbability) {
            double value = ((DoubleToken) stuckAtFaultProbability.getToken())
                    .doubleValue();
            if (value < 0.0 || value > 1.0) {
                throw new IllegalActionException(this,
                        "Cannot have a probability value outside range [0.0,1.0]: "
                                + value);
            }
            _stuckAtFaultProbabilityValue = value;
        }
        super.attributeChanged(attribute);
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new Bus.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StuckAtFaultGenerator newObject = (StuckAtFaultGenerator) super
                .clone(workspace);
        newObject._nextReceiver = null;
        newObject._nextTimeFree = null;
        newObject._receiversAndTokensToSendTo = new HashMap();
        newObject._isStuck = new HashMap();
        newObject._lastKnownHealthyTokens = new HashMap();
        newObject._tokens = new FIFOQueue();
        newObject._wrappedReceivers = new HashMap();
        return newObject;
    }

    /** Initialize the actor.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _receiversAndTokensToSendTo.clear();
        _isStuck.clear();
        _lastKnownHealthyTokens.clear();
        _tokens.clear();
        _wrappedReceivers.clear();

        _nextTimeFree = null;
    }

    /** Send first token in the queue to the target receiver.
     */
    @Override
    public void fire() throws IllegalActionException {

        //if (getContainer() == null) {
        //    return;
        //}
        //Time currentTime = getDirector().getModelTime();
        // In a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null.
        if (_tokens.size() > 0) {
            Object[] output = (Object[]) _tokens.get(0);
            //current receiver being processed
            Receiver receiver = (Receiver) output[0];
            Token token = (Token) output[1];

            // if the particular receiver is new (no stuckAt status)
            if (null == _isStuck.get(receiver) || false) {
                boolean decideOnHealth = false;
                //FIXME: Math.random() is seeded from the current time, use
                // the Ptolemy version here instead that allows to set the seed
                if (Math.random() > 1.0 - _stuckAtFaultProbabilityValue) {
                    decideOnHealth = true;
                }

                _isStuck.put(receiver, new BooleanToken(decideOnHealth));
            } else if (_isStuck.get(receiver).booleanValue() == true) {
                // the receiver is stuck, send the last known healthy token value
                if (_lastKnownHealthyTokens.get(receiver) == null) {
                    _lastKnownHealthyTokens.put(receiver, token);
                } else {
                    token = _lastKnownHealthyTokens.get(receiver);
                }
            } else {
                //roll the dice
                boolean decideOnHealth = false;
                if (Math.random() > 1.0 - _stuckAtFaultProbabilityValue) {
                    decideOnHealth = true;

                }
                _isStuck.put(receiver, new BooleanToken(decideOnHealth));
            }

            _sendToReceiver(receiver, token);
            //            }

            if (_debugging) {
                Time currentTime = getDirector().getModelTime();
                _debug("At time " + currentTime + ", completing send to "
                        + receiver.getContainer().getFullName() + ": " + token);
            }
        }
    }

    /** If there are still tokens in the queue and a token has been produced in the fire,
     *  schedule a refiring.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // This method contains two places where refirings can be
        // scheduled. We only want to schedule a refiring once.
        Time currentTime = getDirector().getModelTime();

        // If a token was actually sent to a delegated receiver
        // by the fire() method, then remove that token from
        // the queue and, if there are still tokens in the queue,
        // request another firing at the time those tokens should
        // be delivered to the delegated receiver.
        if (_nextTimeFree != null && _tokens.size() > 0
                && currentTime.compareTo(_nextTimeFree) == 0) {
            // Discard the token that was sent to the output in fire().
            _tokens.take();

            //            if (_tokens.size() > 0) {
            //                _scheduleRefire();
            //                refiringScheduled = true;
            //                // FIXME:
            //                // Not only does this bus need to be fired
            //                // at the _nextTimeFree, but so does the destination
            //                // actor. In particular, that actor may be under
            //                // the control of a _different director_ than the
            //                // bus, and the order in which that actor is fired
            //                // vs. this Bus is important. How to control this?
            //                // Maybe global scope of a QuantityManager is not
            //                // a good idea, but we really do want to be able
            //                // to share a QuantityManager across modes of a
            //                // modal model. How to fix???
            //            } else {
            //                refiringScheduled = false;
            //            }
        }
        // If sendToken() was called in the current iteration,
        // then append the token to the queue. If this is the
        // only token on the queue, then request a firing at
        // the time that token should be delivered to the
        // delegated receiver.
        if (getDirector() instanceof FixedPointDirector
                && _receiversAndTokensToSendTo != null) {
            for (Receiver receiver : _receiversAndTokensToSendTo.keySet()) {
                Token token = _receiversAndTokensToSendTo.get(receiver);
                if (token != null) {
                    _tokens.put(new Object[] { receiver, token });
                }
            }
            _receiversAndTokensToSendTo.clear();
        }
        // if there was no token in the queue, schedule a refiring.
        // FIXME: wrong, more than one token can be received at a time instant! if (_tokens.size() == 1) {
        if (_tokens.size() > 0
                && (_nextTimeFree == null || currentTime
                        .compareTo(_nextTimeFree) >= 0)) {
            _scheduleRefire();
            // FIXME:
            // Not only does this bus need to be fired
            // at the _nextTimeFree, but so does the destination
            // actor. In particular, that actor may be under
            // the control of a _different director_ than the
            // bus, and the order in which that actor is fired
            // vs. this Bus is important. How to control this?
            // Maybe global scope of a QuantityManager is not
            // a good idea, but we really do want to be able
            // to share a QuantityManager across modes of a
            // modal model. How to fix???
        }

        return super.postfire();
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param source Sender of the token.
     *  @param receiver The receiver to send to.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    @Override
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        // FIXME: Why is the following needed?
        if (_nextTimeFree == null || _tokens.size() == 0
                || currentTime.compareTo(_nextTimeFree) != 0
                || receiver != _nextReceiver) {
            // At the current time, there is no token to send.
            // At least in the Continuous domain, we need to make sure
            // the delegated receiver knows this so that it becomes
            // known and absent.

            if (getDirector() instanceof FixedPointDirector) {
                receiver.put(null);
            }
        }

        // If previously in the current iteration we have
        // sent a token, then we require the token to have the
        // same value. Thus, this Bus can be used only in domains
        // that either call fire() at most once per iteration,
        // or domains that have a fixed-point semantics.
        Token tokenToSend = _receiversAndTokensToSendTo.get(receiver);
        if (tokenToSend != null) {
            if (!tokenToSend.equals(token)) {
                throw new IllegalActionException(this, receiver.getContainer(),
                        "Previously initiated a transmission with value "
                                + tokenToSend
                                + ", but now trying to send value " + token
                                + " in the same iteration.");
            }
        } else {

            // In the Continuous domain, this actor gets fired if tokens are available
            // or not. In the DE domain we need to schedule a refiring.
            if (getDirector() instanceof FixedPointDirector) {
                _receiversAndTokensToSendTo.put(receiver, token);
            } else {
                _tokens.put(new Object[] { receiver, token });
                _tokenCount++;
                sendCommunicationEvent((Actor) source.getContainer()
                        .getContainer(), 0, _tokenCount, EventType.RECEIVED);
                if (_tokens.size() == 1) { // no refiring has been scheduled
                    _scheduleRefire();
                }
            }
        }

        // If the token is null, then this means there is not actually
        // something to send. Do not take up bus resources for this.
        if (token == null) {
            return;
        }
        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }

    /**
     * Reset the communication aspect and clear the tokens.
     */
    @Override
    public void reset() {
        //_tokens.clear();
    }

    ///////////////////////////////////////////////////////////////////
    //                          public variables                     //

    /** The service time. This is a double with default 0.1.
     *  It is required to be positive.
     */
    public Parameter stuckAtFaultProbability;

    ///////////////////////////////////////////////////////////////////
    //                          protected methods                    //

    /** Schedule a refiring of the actor.
     *  @exception IllegalActionException Thrown if the actor cannot be rescheduled
     */
    protected void _scheduleRefire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        _nextTimeFree = currentTime;
        _nextReceiver = (Receiver) ((Object[]) _tokens.get(0))[0];
        _fireAt(_nextTimeFree);
    }

    ///////////////////////////////////////////////////////////////////
    //                           private variables                   //

    /** Next receiver to which the next token to be sent is destined. */
    private Receiver _nextReceiver;

    /** Next time a token is sent and the next token can be processed. */
    private Time _nextTimeFree;

    /** Map of receivers and tokens to which the token provided via sendToken() should
     *  be sent to.
     */
    private HashMap<Receiver, Token> _receiversAndTokensToSendTo;

    /** Map of receivers and the last-received token from that receiver
     *  be sent to.
     */
    private HashMap<Receiver, Token> _lastKnownHealthyTokens;

    private HashMap<Receiver, BooleanToken> _isStuck;

    /** Fault probability per token. */
    private double _stuckAtFaultProbabilityValue;

    /** Tokens stored for processing. */
    private FIFOQueue _tokens;

    /** Hold the receivers tied to this communication aspect in an array, to avoid duplicates */
    private HashMap<Receiver, Receiver> _wrappedReceivers;

}
