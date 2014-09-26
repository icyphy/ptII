/* This communication aspect actor implements an AFDX switch.

@Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.domains.de.lib.aspect;

import java.util.HashMap;
import java.util.LinkedList;

import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.CommunicationAspectAttributes;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.aspect.AtomicCommunicationAspect;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/** A {@link CommunicationAspect} actor that, when its
 *  {@link #sendToken(Receiver, Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule. This communication aspect is used on
 *  input ports by setting a parameter with an ObjectToken that refers
 *  to this CommunicationAspect at the port. Note that the name of this
 *  parameter is irrelevant.
 *
 *  <p>This communication aspect implements an AFDX switch. It has a parameter
 *  specifying the number of ports. On each port, an actor is connected.
 *  Note that these ports are not represented as ptolemy actor ports.
 *  This actor can send tokens to the switch and receive tokens from the
 *  switch. The mapping of ports to actors is done via parameters of this
 *  communication aspect.
 *
 *  <p>Internally, this switch has a buffer for every input, a buffer
 *  for the switch fabric and a buffer for every output. The delays
 *  introduced by the buffers are configured via parameters. Tokens are
 *  processed simultaneously on the buffers.
 *
 *  <p> This switch implements the specific switch for the AFDX network.
 *  This implementation is based on the basic switch implementation.
 *
 *  @author Gilles Lasnier, Based on BasiSwitch.java by Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (glasnier)
 *  @Pt.AcceptedRating Yellow (glasnier)
 */
public class AFDXSwitch extends AtomicCommunicationAspect {

    /** Construct a Bus with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.f
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public AFDXSwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _inputTokens = new HashMap<Integer, LinkedList<TimedEvent>>();
        _outputTokens = new HashMap<Integer, LinkedList<TimedEvent>>();
        _ioPortToSwitchInPort = new HashMap<Port, Integer>();
        _ioPortToSwitchOutPort = new HashMap<Port, Integer>();
        _switchFabricQueue = new LinkedList<TimedEvent>();
        //_actorPorts = new HashMap<Actor, Integer>();
        _tokenCount = 0;

        bitRate = new Parameter(this, "bitRate");
        bitRate.setDisplayName("bitRate (Mbit/s)");
        bitRate.setExpression("100");
        bitRate.setTypeEquals(BaseType.DOUBLE);
        _bitRate = 100;

        technologicalDelay = new Parameter(this, "technologicalDelay");
        technologicalDelay.setDisplayName("technologicalDelay (us)");
        technologicalDelay.setExpression("140");
        technologicalDelay.setTypeEquals(BaseType.DOUBLE);
        _technologicalDelay = 140 / 1000000;

        numberOfPorts = new Parameter(this, "numberOfPorts");
        numberOfPorts.setDisplayName("Number of ports");
        numberOfPorts.setExpression("2");
        numberOfPorts.setTypeEquals(BaseType.INT);
        _numberOfPorts = 2;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver that wraps a given receiver.
     *  For now, we only support wrapping input ports.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     *  @exception IllegalActionException If the receiver is an
     *  output port.
     */
    public IntermediateReceiver getReceiver(Receiver receiver)
            throws IllegalActionException {
        if (receiver.getContainer().isOutput()) {
            throw new IllegalActionException(receiver.getContainer(),
                    "This communication aspect cannot be " + "used on port "
                            + receiver.getContainer()
                            + ", it only be specified on input port.");
        }
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
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

    /** Make sure that this communication aspect is only used in the DE domain.
     *  @param container The container of this actor.
     *  @exception IllegalActionException If thrown by the super class or if the
     *  director of this actor is not a DEDirector.
     *  @exception NameDuplicationException If thrown by the super class.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (getDirector() != null && !(getDirector() instanceof DEDirector)) {
            throw new IllegalActionException(this,
                    "This communication aspect is currently only supported in the DE domain.");
        }
    }

    /** If the attribute for the input, output, technological delay or bit rate is
     *  changed, then ensure that the value is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the buffer delays are negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == technologicalDelay) {
            double value = ((DoubleToken) technologicalDelay.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative serviceTime: " + value);
            }
            _technologicalDelay = value / 1000000;
        } else if (attribute == numberOfPorts) {
            int ports = ((IntToken) numberOfPorts.getToken()).intValue();
            _numberOfPorts = ports;
            for (int i = 0; i < ports; i++) {
                _inputTokens.put(i, new LinkedList<TimedEvent>());
                _outputTokens.put(i, new LinkedList<TimedEvent>());
            }
        } else if (attribute == bitRate) {
            double value = ((DoubleToken) bitRate.getToken()).doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative bitRate: " + value);
            }
            _bitRate = value;
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
        AFDXSwitch newObject = (AFDXSwitch) super.clone(workspace);
        //newObject._actorPorts = new HashMap();
        newObject._nextFireTime = null;
        newObject._inputTokens = new HashMap();
        newObject._outputTokens = new HashMap();
        _ioPortToSwitchInPort = new HashMap<Port, Integer>();
        _ioPortToSwitchOutPort = new HashMap<Port, Integer>();
        newObject._switchFabricQueue = new LinkedList<TimedEvent>();
        return newObject;
    }

    /** Return the decorated attributes for the target NamedObj.
     *  If the specified target is not an Actor, return null.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or
     *   null if the specified target is not an Actor.
     */
    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof IOPort) {
            try {
                return new AfdxSwitchAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    /** Initialize the actor variables.
     *  @exception IllegalActionException If the superclass throws it or
     *  the switch table could not be parsed from the actor parameters.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextFireTime = null;

        /*
        for (int i = 0; i < _numberOfPorts; i++) {
            _inputTokens.put(i, new LinkedList<TimedEvent>());
            _outputTokens.put(i, new LinkedList<TimedEvent>());
        }

        // Read the switching table from the parameters.
        for (int i = 0; i < attributeList().size(); i++) {
            Attribute attribute = (Attribute) attributeList().get(i);
            try {
                int portNumber = Integer.parseInt(attribute.getName());
                Parameter param = (Parameter) attribute;
                Token token = param.getToken();
                Actor actor = (Actor) ((ObjectToken) token).getValue();
                _actorPorts.put(actor, portNumber);
            } catch (NumberFormatException ex) {
                // Parameter was not a number and therefore not a part of
                // the routing table.
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalActionException(this, "There was an error"
                        + "in the routing table information for "
                        + this.getName());
            }
        }
         */

        for (int i = 0; i < _numberOfPorts; i++) {
            _inputTokens.put(i, new LinkedList<TimedEvent>());
            _outputTokens.put(i, new LinkedList<TimedEvent>());
        }
        _switchFabricQueue = new LinkedList<TimedEvent>();

    }

    /** Move tokens from the input queue to the switch fabric, move tokens
     *  from the switch fabric queue to the output queues and send tokens from the
     *  output queues to the target receivers. When moving tokens between
     *  queues the appropriate delays are considered.
     *  @exception IllegalActionException If the token cannot be sent to
     *  target receiver.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();
        Time computedTimeStamp = null;
        boolean multicast = false;

        // In a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null.
        if (_nextFireTime != null && currentTime.compareTo(_nextFireTime) == 0) {

            // Move tokens from input queue to switch fabric.

            TimedEvent event;
            for (int i = 0; i < _numberOfPorts; i++) {
                if (_inputTokens.get(i).size() > 0) {
                    event = _inputTokens.get(i).getFirst();
                    if (event.timeStamp.compareTo(currentTime) == 0) {
                        Time lastTimeStamp = currentTime;
                        if (_switchFabricQueue.size() > 0) {
                            Object[] last = (Object[]) _switchFabricQueue
                                    .getLast().contents;
                            Object[] eObj = (Object[]) event.contents;
                            if (((AFDXVlink) last[2]).getSource().equals(
                                    ((AFDXVlink) eObj[2]).getSource())
                                    && ((Time) last[3]).compareTo(eObj[3]) == 0) {
                                multicast = true;
                            }

                            lastTimeStamp = _switchFabricQueue.getLast().timeStamp;
                        }

                        if (multicast) {
                            computedTimeStamp = lastTimeStamp;
                            multicast = false;
                        } else {
                            computedTimeStamp = currentTime
                                    .add(_technologicalDelay);
                        }
                        _switchFabricQueue.add(new TimedEvent(
                                computedTimeStamp, event.contents));

                        _inputTokens.get(i).remove(event);
                    }
                }
            }

            // Move tokens from switch fabric to output queue.

            if (_switchFabricQueue.size() > 0) {
                computedTimeStamp = null;
                multicast = false;

                event = _switchFabricQueue.getFirst();
                if (event.timeStamp.compareTo(currentTime) == 0) {
                    Object[] output = (Object[]) event.contents;
                    Receiver receiver = (Receiver) output[0];

                    if (receiver instanceof IntermediateReceiver) {
                    } else {
                        receiver.getContainer().getContainer();
                    }
                    //int actorPort = _actorPorts.get(actor);
                    int outputPortID = _getPortID(receiver, false);

                    Time lastTimeStamp = currentTime;
                    if (_outputTokens.get(outputPortID).size() > 0) {
                        Object[] last = (Object[]) _outputTokens.get(
                                outputPortID).getLast().contents;
                        if (((AFDXVlink) last[2]).getSource().equals(
                                ((AFDXVlink) output[2]).getSource())
                                && ((Time) last[3]).compareTo(output[3]) == 0) {
                            multicast = true;
                        }

                        lastTimeStamp = _outputTokens.get(outputPortID)
                                .getLast().timeStamp;
                    }

                    if (multicast) {
                        computedTimeStamp = lastTimeStamp;
                        multicast = false;
                    } else {
                        AFDXVlink vl = (AFDXVlink) output[2];
                        computedTimeStamp = lastTimeStamp.add(vl.getFrameSize()
                                / (_bitRate * 1000000));
                    }
                    _outputTokens.get(outputPortID).add(
                            new TimedEvent(computedTimeStamp, event.contents));

                    _switchFabricQueue.remove(event);
                }
            }
            // _scheduleRefire();

            // Send tokens to target receiver.

            for (int i = 0; i < _numberOfPorts; i++) {
                if (_outputTokens.get(i).size() > 0) {
                    event = _outputTokens.get(i).getFirst();
                    if (event.timeStamp.compareTo(currentTime) == 0) {
                        Object[] output = (Object[]) event.contents;

                        // The receiver is an AFDXSwitch (qm).
                        Receiver receiver = (Receiver) output[0];
                        if (receiver instanceof IntermediateReceiver) {
                            String[] labels = new String[] { timestamp, vlink,
                                    payload };
                            Token[] values = new Token[] {
                                    new DoubleToken(
                                            event.timeStamp.getDoubleValue()),
                                    new ObjectToken(output[2]),
                                    (Token) output[1] };
                            RecordToken record = new RecordToken(labels, values);
                            _sendToReceiver((Receiver) output[0], record);
                        } else { // Else the receiver is an actor.
                            Token token = (Token) output[1];
                            _sendToReceiver((Receiver) output[0], token);
                        }
                        _outputTokens.get(i).remove(event);
                    }
                }

                if (_debugging) {
                    _debug("At time " + currentTime + ", completing send");
                }
            }
            _scheduleRefire();
        }
    }

    /** If there are still tokens in the queue and a token has been
     *  produced in the fire, schedule a refiring.
     *  @exception IllegalActionException If the refiring cannot be scheduled or
     *  by super class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _scheduleRefire();
        return super.postfire();
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param source Sender of the token.
     *  @param receiver The sending receiver.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    @Override
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time computedTimeStamp = null;
        AFDXVlink vl = null;
        Token tok = null;
        boolean multicast = false;

        // FIXME add Continuous support.

        if (token instanceof RecordToken) {
            vl = (AFDXVlink) ((ObjectToken) ((RecordToken) token).get("vlink"))
                    .getValue();
            tok = ((RecordToken) token).get("payload");
        }

        int inputPortID = _getPortID(receiver, true);

        Time lastTimeStamp = currentTime;
        if (_inputTokens.get(inputPortID).size() > 0) {
            if (currentTime.compareTo(((Object[]) _inputTokens.get(inputPortID)
                    .getLast().contents)[3]) == 0) {
                multicast = true;
            }

            lastTimeStamp = _inputTokens.get(inputPortID).getLast().timeStamp;
        }

        if (multicast) {
            computedTimeStamp = lastTimeStamp;
            multicast = false;
        } else {
            computedTimeStamp = lastTimeStamp;
            // GL: XXX: FIXME: .add(_inputBufferDelay);
        }

        _inputTokens.get(inputPortID).add(
                new TimedEvent(computedTimeStamp, new Object[] { receiver, tok,
                        vl, currentTime }));

        _tokenCount++;
        //sendQMTokenEvent((Actor) source.getContainer().getContainer(), 0,
        //        _tokenCount, EventType.RECEIVED);
        _scheduleRefire();

        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }

    /** Set the id of the switch input that is receiving tokens from this actor port.
     *  @param port The actor port.
     *  @param portIn The id of the switch port.
     */
    public void setPortIn(Port port, int portIn) {
        _ioPortToSwitchInPort.put(port, portIn);
    }

    /** Set the id of the switch output that is sending tokens to this actor port.
     * @param port The actor port.
     * @param portOut The id of the switch port.
     */
    public void setPortOut(Port port, int portOut) {
        _ioPortToSwitchOutPort.put(port, portOut);
    }

    /** Reset the communication aspect and clear the tokens.
     */
    @Override
    public void reset() {
        _inputTokens.clear();
        _outputTokens.clear();
        _switchFabricQueue.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Number of ports on the switch. This parameter must contain an
     *  IntToken.  The value defaults to 2. */
    public Parameter numberOfPorts;

    /** Time it takes for a token to be put into the input queue.
     *  This parameter must contain a DoubleToken. The value defaults
     *  to 0.0. */
    public Parameter inputBufferDelay;

    /** Time it takes for a token to be put into the output queue.
     *  This parameter must contain a DoubleToken. The value defaults
     *  to 0.0. */
    public Parameter outputBufferDelay;

    /** Technological latency according to the AFDX specification.
     *  This parameter must contain a DoubleToken. The value defaults
     *  to 0.000140 (140 us). */
    public Parameter technologicalDelay;

    /** The bit rate of the bus. The value defaults to 100 Mbits/s.
     */
    public Parameter bitRate;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the IO of the switch port where this receiver is
     *  connected to. The port ID's are set via parameters.
     *  @param receiver The actor receiver.
     *  @param input Whether the port is an input port.
     *  @return The port ID.
     */
    protected int _getPortID(Receiver receiver, boolean input) {
        NamedObj containerPort = receiver.getContainer();
        while (!(receiver.getContainer() instanceof Port)) {
            containerPort = containerPort.getContainer();
        }
        Port port = (Port) containerPort;

        if (input) {
            return _ioPortToSwitchInPort.get(port);
        } else {
            return _ioPortToSwitchOutPort.get(port);
        }
    }

    /** Get next fire time for a set of tokens which is either the minimum
     *  next fire time passed as an argument or the smallest timestamp of
     *  the tokens in the set.
     *  @param nextFireTime Minimum next fire time.
     *  @param tokens The set of tokens.
     *  @return The next time this actor should be fired based on the tokens
     *  in the queue.
     */
    protected Time _getNextFireTime(Time nextFireTime,
            LinkedList<TimedEvent> tokens) {
        if (tokens.size() > 0) {
            TimedEvent event = tokens.getFirst();
            if (event.timeStamp.compareTo(nextFireTime) < 0) {
                nextFireTime = event.timeStamp;
            }
        }
        return nextFireTime;
    }

    /** Schedule a refiring of this actor based on the tokens in the queues.
     *  @exception IllegalActionException If actor cannot be refired
     *  at the computed time.
     */
    protected void _scheduleRefire() throws IllegalActionException {
        _nextFireTime = Time.POSITIVE_INFINITY;
        for (int i = 0; i < _numberOfPorts; i++) {
            _nextFireTime = _getNextFireTime(_nextFireTime, _inputTokens.get(i));
            _nextFireTime = _getNextFireTime(_nextFireTime,
                    _outputTokens.get(i));
        }
        _nextFireTime = _getNextFireTime(_nextFireTime, _switchFabricQueue);
        _fireAt(_nextFireTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Time it takes for a token to be processed by the switch fabric. */
    protected double _technologicalDelay;

    /** Mapping of actors to switch ports. */
    //protected HashMap<Actor, Integer> _actorPorts;

    /** Next time a token is sent and the next token can be processed. */
    protected Time _nextFireTime;

    /** Tokens received by the switch. */
    protected HashMap<Integer, LinkedList<TimedEvent>> _inputTokens;

    /** Tokens to be sent to outputs. */
    protected HashMap<Integer, LinkedList<TimedEvent>> _outputTokens;

    /** Tokens sent to ports mediated by this communication aspect
     *  are rerouted to the switch ports with the IDs specified in this
     *  map.
     */
    protected HashMap<Port, Integer> _ioPortToSwitchInPort;

    /** Tokens set to ports mediated by this communication aspect are
     *  processed by this communication aspect and then forwarded
     *  to the port through the switch port with ID specified here.
     */
    protected HashMap<Port, Integer> _ioPortToSwitchOutPort;

    /** Number of switch ports. */
    protected int _numberOfPorts;

    /** Value of the bit rate of the bus. */
    protected double _bitRate;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Tokens processed by the switch fabric. */
    private LinkedList<TimedEvent> _switchFabricQueue;

    /** Label of the timestamp that is transmitted within the RecordToken. */
    private static final String timestamp = "timestamp";

    /** Label of the vlink that is transmitted within the RecordToken. */
    private static final String vlink = "vlink";

    /** Label of the payload that is transmitted within the RecordToken. */
    private static final String payload = "payload";

    /** The attributes configured per port which is mediated by a
     *  AfdxSwitch. The mediation where (which switch port) messages
     *  are going into the switch and where (which switch port) messages
     *  are going out of the switch.
     *  @author Gilles Lasnier, Based on BasiSwitch.java by Patricia Derler
     */
    public static class AfdxSwitchAttributes extends
            CommunicationAspectAttributes {

        /** Constructor to use when editing a model.
         *  @param container The object being decorated.
         *  @param decorator The decorator.
         *  @exception IllegalActionException If the superclass throws it.
         *  @exception NameDuplicationException If the superclass throws it.
         */
        public AfdxSwitchAttributes(NamedObj container, Decorator decorator)
                throws IllegalActionException, NameDuplicationException {
            super(container, decorator);
            _init();
        }

        /** Constructor to use when parsing a MoML file.
         *  @param container The object being decorated.
         *  @param name The name of this attribute.
         *  @exception IllegalActionException If the superclass throws it.
         *  @exception NameDuplicationException If the superclass throws it.
         */
        public AfdxSwitchAttributes(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            _init();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         parameters                        ////

        /** The id of the port on the switch on which incoming messages are
         *  received.
         *  This parameter defaults to the integer value 0.
         */
        public Parameter portIn;

        /** The id of the port on the switch to which outgoing messages are
         *  routed to.
         *  This parameter defaults to the integer value 1.
         */
        public Parameter portOut;

        /** If attribute is <i>portIn</i> or <i>portOut</i>,
         *  report the new values to the communication aspect.
         *  @param attribute The changed parameter.
         *  @exception IllegalActionException If the parameter set is not valid.
         *  Not thrown in this class.
         */
        @Override
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
            IOPort port = (IOPort) getContainer();
            AFDXSwitch afdxSwitch = (AFDXSwitch) getDecorator();
            if (afdxSwitch != null) {
                if (attribute == portIn) {
                    _portIn = ((IntToken) ((Parameter) attribute).getToken())
                            .intValue();
                    afdxSwitch.setPortIn(port, _portIn);
                } else if (attribute == portOut) {
                    _portOut = ((IntToken) ((Parameter) attribute).getToken())
                            .intValue();
                    afdxSwitch.setPortOut(port, _portOut);
                } else {
                    super.attributeChanged(attribute);
                }
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                        private methods                    ////

        /** Create the parameters.
         */
        private void _init() {
            try {
                portIn = new Parameter(this, "portIn", new IntToken(0));
                portOut = new Parameter(this, "portOut", new IntToken(1));
                _portIn = 0;
                _portOut = 1;
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        }

        private int _portIn;

        private int _portOut;
    }

}
