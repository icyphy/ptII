/* This actor implements a Network Bus.

@Copyright (c) 2010 The Regents of the University of California.
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

package ptolemy.domains.ptides.demo.PtidesNetwork;

import java.util.Hashtable;

import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * Receives tokens and after a fixed delay forwards the tokens to specified 
 * receivers. Tokens are processed in FiFo order. 
 * 
 * The functionality is similar to the functionality of the Server 
 * (@see Server) but without input and output ports.
 * @author Patricia Derler
 */
public class TTESwitch extends QuantityManager {
    /** Construct a Bus in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public TTESwitch() throws IllegalActionException, NameDuplicationException {
        super();
        _initialize();
    }

    /** Construct a Bus in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public TTESwitch(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _initialize();
    }

    /** Construct a Bus with a name and a container.
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
        _initialize();
    }
    
    /** The service time. This is a double with default 1.0.
     *  It is required to be non-negative.
     */
    public Parameter serviceTime;

    /**
     * Create an intermediate receiver that wraps a given receiver.
     * @param receiver The receiver that is being wrapped.
     * @return A new intermediate receiver.
     * @throws IllegalActionException 
     */
    public IntermediateReceiver getReceiver(Receiver receiver) throws IllegalActionException {
        IntermediateReceiver intermediateReceiver = _receivers.get(receiver);
        if (intermediateReceiver == null) {
            intermediateReceiver = new IntermediateReceiver(this, receiver);
            _receivers.put(receiver, intermediateReceiver);
            Parameter timeTriggeredParameter = (Parameter) receiver.getContainer().getAttribute("type");
            boolean timeTriggered = false;
            if (timeTriggeredParameter != null) {
                String timeTriggeredString = ((StringToken)timeTriggeredParameter.getToken()).stringValue();
                if (!timeTriggeredString.equals("time-triggered") && !timeTriggeredString.equals("event-triggered")) {
                    throw new IllegalActionException("Value of parameter 'type' must be either 'time-triggered' or" +
                                "'event-triggered. Value of port " + receiver.getContainer() + " is '" + 
                                timeTriggeredString +"'");
                }
                timeTriggered = timeTriggeredString.equals("time-triggered");
            } else { 
                throw new IllegalActionException("Type of port " + receiver.getContainer() +" must be specified");
            }
            _receiverType.put(receiver, timeTriggered);
        }
        return intermediateReceiver;
    }
    

    /** If the attribute is <i>serviceTime</i>, then ensure that the value
     *  is non-negative
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serviceTime) {
            double value = ((DoubleToken) serviceTime.getToken()).doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative serviceTime: " + value);
            }
            _serviceTimeValue = value;
        }
    }

    /**
     * Send first token in the queue to the target receiver.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (currentTime.compareTo(_nextTimeFree) == 0) {
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

    /**
     * If there are still tokens in the queue schedule a refiring.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (currentTime.compareTo(_nextTimeFree) == 0 && 
                (_ttTokens.size() == 1 || 
                (_ttTokens.size() == 0 && _etTokens.size() > 0))) {   
            _nextTimeFree = currentTime.add(_serviceTimeValue);
            _fireAt(_nextTimeFree);
        } 
        return super.postfire();
    }

    /**
     * Receive a token and store it in the queue. Schedule a refiring.
     */
    public void sendToken(Receiver receiver, Token token)
            throws IllegalActionException {
        if (_receiverType.get(receiver)) { // time-triggered
            _ttTokens.put(new Object[] { receiver, token });
        } else { // event-triggered
            _etTokens.put(new Object[] { receiver, token });
        }
        if (_ttTokens.size() > 1) {
            throw new IllegalActionException("Schedule violation: A time-triggered message is " +
                    "being sent at port " + ((Receiver)((Object[])_ttTokens.get(0))[0]).getContainer() +
                    " while a new time-triggered message is received at port " + 
                    receiver.getContainer() + " at time " + getDirector().getModelTime());
        } else if (_ttTokens.size() == 1 || 
                (_ttTokens.size() == 0 && _etTokens.size() > 0)) {  
            Time currentTime = getDirector().getModelTime();
            _nextTimeFree = currentTime.add(_serviceTimeValue);
            _fireAt(_nextTimeFree);
        } 
    }

    /**
     * Reset the quantity manager and clear the tokens.
     */
    public void reset() {
        _etTokens.clear();
        _ttTokens.clear();
    }
    
    /**
     * Initialize local variables.
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    private void _initialize() throws IllegalActionException,
            NameDuplicationException {
        _receivers = new Hashtable<Receiver, IntermediateReceiver>();
        _receiverType = new Hashtable<Receiver, Boolean>();
        _etTokens = new FIFOQueue();
        _ttTokens = new FIFOQueue();

        serviceTime = new Parameter(this, "serviceTime");
        serviceTime.setExpression("0.1");
        serviceTime.setTypeEquals(BaseType.DOUBLE);
    }

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
