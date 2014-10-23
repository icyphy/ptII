/* This actor implements a time-triggered ethernet switch.

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

package ptolemy.domains.de.lib.aspect;

import java.util.Hashtable;

import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.aspect.AtomicCommunicationAspect;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** This actor is an {@link CommunicationAspect} that, when its
 *  {@link #sendToken(Receiver, Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule.
 *
 *  The actor differentiates between two kinds of tokens: time-triggered
 *  and event-triggered, which is defined by the parameter <i>type</i> in
 *  the port which is associated with this communication aspect.
 *
 *  When tokens are received they are delivered with a delay given by the
 *  <i>serviceTime</i> parameter. If the actor is currently servicing a previous
 *  event-triggered token when it receives a time-triggered token, the event-triggered
 *  token is queued again and the time-triggered token is serviced. After the
 *  time-triggered token is sent, the event-triggered token in the queue is selected
 *  and delivered after the full <i>serviceTime</i>. If an event-triggered token arrives
 *  while another event-triggered token arrives, the new event-triggered token is queued.
 *  If a time-triggered token is received while another time-triggered token is serviced
 *  an exception is thrown. Time-triggered messages should have a fixed delay. In a
 *  time-triggered ethernet implementation an offline calculated schedule ensures that only
 *  one time-triggered message is received by the TTESwitch at a time.
 *  Event-triggered tokens are processed in FIFO order.
 *  <p>.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class TTESwitch extends AtomicCommunicationAspect {

    /** Construct a TTESwitch with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TTESwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _receivers = new Hashtable<Receiver, IntermediateReceiver>();
        _receiverType = new Hashtable<Receiver, Boolean>();
        _etTokens = new FIFOQueue();
        _ttTokens = new FIFOQueue();

        serviceTime = new Parameter(this, "serviceTime");
        serviceTime.setExpression("0.1");
        serviceTime.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver and determine type of tokens received on the
     *  port associated with this receiver. The type is specified in a parameter of the port
     *  (time-triggered or event-triggered).
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     *  @exception IllegalActionException If parameter
     */
    @Override
    public IntermediateReceiver createIntermediateReceiver(Receiver receiver)
            throws IllegalActionException {
        IntermediateReceiver intermediateReceiver = _receivers.get(receiver);
        if (intermediateReceiver == null) {
            intermediateReceiver = new IntermediateReceiver(this, receiver);
            _receivers.put(receiver, intermediateReceiver);
            Parameter timeTriggeredParameter = (Parameter) receiver
                    .getContainer().getAttribute("type");
            boolean timeTriggered = false;
            if (timeTriggeredParameter != null) {
                String timeTriggeredString = ((StringToken) timeTriggeredParameter
                        .getToken()).stringValue();
                if (!timeTriggeredString.equals("time-triggered")
                        && !timeTriggeredString.equals("event-triggered")) {
                    throw new IllegalActionException(
                            "Value of parameter 'type' must be either 'time-triggered' or"
                                    + "'event-triggered. Value of port "
                                    + receiver.getContainer() + " is '"
                                    + timeTriggeredString + "'");
                }
                timeTriggered = timeTriggeredString.equals("time-triggered");
            } else {
                throw new IllegalActionException("Type of port "
                        + receiver.getContainer() + " must be specified");
            }
            _receiverType.put(receiver, timeTriggered);
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
        return createIntermediateReceiver(receiver);
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
     *  @return A new TTESwitch.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TTESwitch newObject = (TTESwitch) super.clone(workspace);
        newObject._etTokens = new FIFOQueue();
        newObject._ttTokens = new FIFOQueue();
        newObject._nextTimeFree = null;
        newObject._receivers = new Hashtable();
        newObject._serviceTimeValue = 0.1;
        newObject._receiverType = new Hashtable();
        return newObject;
    }

    /** If the attribute is <i>serviceTime</i>, then ensure that the value
     *  is positive.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serviceTime) {
            double value = ((DoubleToken) serviceTime.getToken()).doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero serviceTime: " + value);
            }
            _serviceTimeValue = value;
        }
    }

    /** If there is a time-triggered token scheduled to be sent then deliver this
     *  token, otherwise send first token in the queue of event-triggered tokens.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();
        if (_nextTimeFree != null && currentTime.compareTo(_nextTimeFree) == 0) {
            Object[] output;
            if (_ttTokens.size() > 0) {
                output = (Object[]) _ttTokens.take();
            } else {
                output = (Object[]) _etTokens.take();
            }
            Receiver receiver = (Receiver) output[0];
            Token token = (Token) output[1];
            receiver.put(token);
        }
    }

    /** If a token has been sent in the fire method then schedule the next firing.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (_nextTimeFree != null
                && currentTime.compareTo(_nextTimeFree) == 0
                && (_ttTokens.size() == 1 || _ttTokens.size() == 0
                && _etTokens.size() > 0)) {
            _nextTimeFree = currentTime.add(_serviceTimeValue);
            _fireAt(_nextTimeFree);
        }
        return super.postfire();
    }

    /** Receive a token and store it in the queue. Schedule a refiring.
     */
    @Override
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        if (_receiverType.get(receiver)) { // time-triggered
            _ttTokens.put(new Object[] { receiver, token });
        } else { // event-triggered
            _etTokens.put(new Object[] { receiver, token });
        }
        if (_ttTokens.size() > 1) {
            throw new IllegalActionException(
                    "Schedule violation: A time-triggered message is "
                            + "being sent at port "
                            + ((Receiver) ((Object[]) _ttTokens.get(0))[0])
                            .getContainer()
                            + " while a new time-triggered message is received at port "
                            + receiver.getContainer() + " at time "
                            + getDirector().getModelTime());
        } else if (_ttTokens.size() == 1 || _ttTokens.size() == 0
                && _etTokens.size() > 0) {
            Time currentTime = getDirector().getModelTime();
            _nextTimeFree = currentTime.add(_serviceTimeValue);
            _fireAt(_nextTimeFree);
        }
    }

    /** Reset the communication aspect and clear the tokens.
     */
    @Override
    public void reset() {
        _etTokens.clear();
        _ttTokens.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The service time. This is a double with default 0.1.
     *  It is required to be positive.
     */
    public Parameter serviceTime;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Delay imposed on every token.
     */
    private double _serviceTimeValue;

    /**
     * Map target receivers to intermediate receivers.
     */
    private Hashtable<Receiver, IntermediateReceiver> _receivers;

    /**
     * Store type of receiver: true if time-triggered, false if event-triggered.
     */
    private Hashtable<Receiver, Boolean> _receiverType;

    /**
     * Tokens for time-triggered traffic.
     */
    private FIFOQueue _ttTokens;

    /**
     * Tokens for event-triggered traffic.
     */
    private FIFOQueue _etTokens;

    /**
     * Next time a token is sent and the next token can be processed.
     */
    private Time _nextTimeFree;

}
