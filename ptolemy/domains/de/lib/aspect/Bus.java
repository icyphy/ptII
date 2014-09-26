/* This actor implements a Network Bus.

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

//package ptolemy.actor.lib.aspect;
package ptolemy.domains.de.lib.aspect;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.CommunicationAspectAttributes;
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
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/** This actor is a {@link CommunicationAspect} that, when its
 *  {@link #sendToken(Receiver, Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule. Specifically, if the actor is
 *  not currently servicing a previous token, then it delivers
 *  the token with a delay given by the <i>serviceTimeMultiplicationFactor</i>
 *  parameter multiplied by the <i>messageLength</i> parameter specified in the port.
 *  If the actor is currently servicing a previous token, then it waits
 *  until it has finished servicing that token (and any other pending
 *  tokens), and then delays for an additional amount given by
 *  <i>serviceTimeMultiplicationFactor</i> * <i>messageLength</i>.
 *  In the default case of the <i>messageLength</i> = 1, the behavior is similar to
 *  the {@link Server} actor.
 *  Tokens are processed in FIFO order.
 *  <p>
 *  To use this communication aspect, drag an instance of this Bus
 *  into the model, and (optionally)
 *  assign it a name. Then, on any input port whose communication is to be
 *  mediated by this instance of Bus, open the configuration dialogue,
 *  select the tab with the name of the bus in the title and select the
 *  <i>enable</i> attribute. The message length is by default set to 1
 *  but can be configured in this tab.
 *  <p>
 *  Several Bus communication aspects can be used in sequence. The order in which
 *  Tokens are sent through Buses depends on the order in which these are
 *  enabled via the DecoratorAttributes.
 *  <p>
 *  This actor is tested in continuous and DE.
 *  @author Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class Bus extends AtomicCommunicationAspect {

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
    public Bus(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _tokens = new FIFOQueue();
        _receiversAndTokensToSendTo = new HashMap<Receiver, Token>();
        _tempReceiverQueue = new FIFOQueue();
        _messageLengths = new Hashtable<IOPort, Double>();

        serviceTimeMultiplicationFactor = new Parameter(this,
                "serviceTimeMultiplicationFactor");
        serviceTimeMultiplicationFactor.setExpression("0.1");
        serviceTimeMultiplicationFactor.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The service time for the default messageLength of 1. This is a double with default 0.1.
     *  It is required to be positive.
     */
    public Parameter serviceTimeMultiplicationFactor;

    /** If the attribute is <i>serviceTime</i>, then ensure that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serviceTimeMultiplicationFactor) {
            double value = ((DoubleToken) serviceTimeMultiplicationFactor
                    .getToken()).doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative serviceTime: " + value);
            }
            _serviceTimeMultiplicationFactorValue = value;
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the decorated attributes for the target NamedObj.
     *  If the specified target is not an Actor, return null.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or
     *   null if the specified target is not an Actor.
     */
    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof IOPort && ((IOPort) target).isInput()) {
            try {
                return new BusAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     * @exception IllegalActionException Thrown if Bus is used in container different from the container of the bus.
     */
    @Override
    public IntermediateReceiver createIntermediateReceiver(Receiver receiver)
            throws IllegalActionException {
        // Only allow use of Bus on Ports in the same hierarchy level.
        if (receiver.getContainer().getContainer().getContainer() != this
                .getContainer()) {
            throw new IllegalActionException(
                    "This Bus can only be used on Ports in the same"
                            + " container as the Bus.");
        }
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        return intermediateReceiver;
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
        Bus newObject = (Bus) super.clone(workspace);
        newObject._tokens = new FIFOQueue();
        newObject._receiversAndTokensToSendTo = new HashMap<Receiver, Token>();
        newObject._tempReceiverQueue = new FIFOQueue();
        newObject._messageLengths = new Hashtable<IOPort, Double>();
        newObject._parameters = new HashMap<IOPort, List<Attribute>>();

        newObject._nextReceiver = null;
        newObject._nextTimeFree = null;

        newObject._serviceTimeMultiplicationFactorValue = 0.1;
        return newObject;
    }

    /** Send first token in the queue to the target receiver.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();
        // In a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null.
        if (_nextTimeFree != null && _tokens.size() > 0
                && currentTime.compareTo(_nextTimeFree) == 0) {
            Object[] output = (Object[]) _tokens.get(0);
            Receiver receiver = (Receiver) output[0];
            Token token = (Token) output[1];
            _sendToReceiver(receiver, token);

            if (_debugging) {
                _debug("At time " + currentTime + ", completing send to "
                        + receiver.getContainer().getFullName() + ": " + token);
            }
        }
    }

    /** Initialize the actor.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _receiversAndTokensToSendTo.clear();
        _tempReceiverQueue.clear();
        _tokens.clear();
        _tokenCount = 0;
        _nextReceiver = null;
        _nextTimeFree = null;
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
            _tokenCount--;
            sendCommunicationEvent(null, 0, _tokenCount, EventType.SENT);
        }
        // If sendToken() was called in the current iteration,
        // then append the token to the queue. If this is the
        // only token on the queue, then request a firing at
        // the time that token should be delivered to the
        // delegated receiver.
        if ((getDirector() instanceof FixedPointDirector)) {
            while (_tempReceiverQueue.size() > 0) {
                Receiver receiver = (Receiver) _tempReceiverQueue.take();
                Token token = _receiversAndTokensToSendTo.get(receiver);
                if (token != null) {
                    _tokens.put(new Object[] { receiver, token });
                    _tokenCount++;
                    sendCommunicationEvent((Actor) receiver.getContainer()
                            .getContainer(), 0, _tokenCount, EventType.RECEIVED);
                }
            }
        }
        if (_tokens.size() > 0
                && (_nextTimeFree == null || currentTime
                        .compareTo(_nextTimeFree) >= 0)) {
            _scheduleRefire();
        }
        _receiversAndTokensToSendTo.clear();
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
        // If the token is null, then this means there is not actually
        // something to send. Do not take up bus resources for this.
        // FIXME: Is this the right thing to do?
        // Presumably, this is an issue with the Continuous domain.
        if (getDirector() instanceof DEDirector && token == null) {
            return;
        }
        Time currentTime = getDirector().getModelTime();
        // Send "absent" if there is nothing to send.
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
            // In the Continuous domain, this actor gets fired whether tokens are available
            // or not. In the DE domain we need to schedule a refiring.
            if (token != null) {
                _receiversAndTokensToSendTo.put(receiver, token);
                _tempReceiverQueue.put(receiver);

                if (!(getDirector() instanceof FixedPointDirector)) {
                    _tokens.put(new Object[] { receiver, token });
                    _tokenCount++;
                    sendCommunicationEvent((Actor) source.getContainer()
                            .getContainer(), 0, _tokenCount, EventType.RECEIVED);
                    if (_tokens.size() == 1) { // no refiring has been scheduled
                        _scheduleRefire();
                    }
                    _receiversAndTokensToSendTo.clear();
                }
            }
        }

        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }

    /** Override the base class to first set the container, then establish
     *  a connection with any decorated objects it finds in scope in the new
     *  container.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            List<NamedObj> decoratedObjects = decoratedObjects();
            for (NamedObj decoratedObject : decoratedObjects) {
                // The following will create the DecoratorAttributes if it does not
                // already exist, and associate it with this decorator.
                BusAttributes decoratorAttributes = (BusAttributes) decoratedObject
                        .getDecoratorAttributes(this);
                setMessageLength((IOPort) decoratedObject,
                        decoratorAttributes._messageLength);
            }
        }
    }

    /** Set the message length for tokens sent to this actor port.
     *  @param port The actor port.
     *  @param messageLength The message length.
     */
    public void setMessageLength(IOPort port, double messageLength) {
        if (_messageLengths == null) {
            _messageLengths = new Hashtable<IOPort, Double>();
        }
        _messageLengths.put(port, messageLength);
    }

    /**
     * Nothing to do.
     */
    @Override
    public void reset() {
    }

    ///////////////////////////////////////////////////////////////////
    //                          public variables                     //

    /** Schedule a refiring of the actor.
     *  @exception IllegalActionException Thrown if the actor cannot be rescheduled
     */
    protected void _scheduleRefire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        _nextReceiver = (Receiver) ((Object[]) _tokens.get(0))[0];
        IOPort port = _nextReceiver.getContainer();

        Double messageLength = _messageLengths.get(port);
        if (messageLength == null) {
            messageLength = 1.0;
        }

        _nextTimeFree = currentTime.add(_serviceTimeMultiplicationFactorValue
                * messageLength);
        _fireAt(_nextTimeFree);
    }

    ///////////////////////////////////////////////////////////////////
    //                           private variables                   //

    /** Message length per port. If this table does not contain an entry
     *  for a port, a default of 1 is assumed.
     */
    private Hashtable<IOPort, Double> _messageLengths;

    /** Next receiver to which the next token to be sent is destined. */
    private Receiver _nextReceiver;

    /** Next time a token is sent and the next token can be processed. */
    private Time _nextTimeFree;

    /** Map of receivers and tokens to which the token provided via
     *  sendToken() should be sent to. This is used with FixedPointDirectors.
     */
    private HashMap<Receiver, Token> _receiversAndTokensToSendTo;

    /** During the fix point iteration keep track of the order of tokens sent to
     *  receivers. The tokens are stored in _receiversAndTokensToSendTo.
     */
    private FIFOQueue _tempReceiverQueue;

    /** The double value of the serviceTimeMultiplicationFactor parameter. */
    private double _serviceTimeMultiplicationFactorValue;

    /** Tokens stored for processing. This is used with the DE Director. */
    private FIFOQueue _tokens;

    /** The port specific attributes for ports mediated by a Bus.
     *
     *  @author Patricia Derler
     */
    public static class BusAttributes extends CommunicationAspectAttributes {

        /** Constructor to use when editing a model.
         *  @param target The object being decorated.
         *  @param decorator The decorator.
         *  @exception IllegalActionException If the superclass throws it.
         *  @exception NameDuplicationException If the superclass throws it.
         */
        public BusAttributes(NamedObj target,
                AtomicCommunicationAspect decorator)
                throws IllegalActionException, NameDuplicationException {
            super(target, decorator);
            _init();
        }

        /** Constructor to use when parsing a MoML file.
         *  @param target The object being decorated.
         *  @param name The name of this attribute.
         *  @exception IllegalActionException If the superclass throws it.
         *  @exception NameDuplicationException If the superclass throws it.
         */
        public BusAttributes(NamedObj target, String name)
                throws IllegalActionException, NameDuplicationException {
            super(target, name);
            _init();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         parameters                        ////

        /** Message length per port. The total time the token is
         *  delayed at a quantity manger is the serviceTimeMultiplicationFactor
         *  * messageLength + the time other tokens need to be serviced.
         */
        public Parameter messageLength;

        /** If attribute is <i>messageLength</i> report the new value
         *  to the communication aspect.
         *  @param attribute The changed parameter.
         *  @exception IllegalActionException If the parameter set is not valid.
         *  Not thrown in this class.
         */
        @Override
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
            if (attribute == messageLength) {
                if (((BooleanToken) enable.getToken()).booleanValue()) {
                    IOPort port = (IOPort) getContainer();
                    Bus bus = (Bus) getDecorator();
                    if (bus != null) {
                        Token token = messageLength.getToken();
                        if (token != null) {
                            _messageLength = ((ScalarToken) token)
                                    .doubleValue();
                            bus.setMessageLength(port, _messageLength);
                        }
                    }
                }
            } else {
                super.attributeChanged(attribute);
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                        private methods                    ////

        /** Create the parameters.
         */
        private void _init() {
            try {
                messageLength = new Parameter(this, "messageLength");
                messageLength.setExpression("1");
                _messageLength = 1;
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        }

        private double _messageLength;
    }

}
