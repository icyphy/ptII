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
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
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
public class Bus extends QuantityManager {
    /** Construct a Bus in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public Bus() throws IllegalActionException, NameDuplicationException {
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
    public Bus(Workspace workspace) throws IllegalActionException,
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
    public Bus(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }

    /**
     * Create an intermediate receiver that wraps a given receiver.
     * @param receiver The receiver that is being wrapped.
     * @return A new intermediate receiver.
     */
    public IntermediateReceiver getReceiver(Receiver receiver) {
        IntermediateReceiver intermediateReceiver = _receivers.get(receiver);
        if (intermediateReceiver == null) {
            intermediateReceiver = new IntermediateReceiver(this, receiver);
            _receivers.put(receiver, intermediateReceiver);
        }
        return intermediateReceiver;
    }

    /**
     * Initialize local variables.
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    private void _initialize() throws IllegalActionException,
            NameDuplicationException {
        _receivers = new Hashtable();
        _tokens = new FIFOQueue();

        serviceTime = new Parameter(this, "serviceTime");
        serviceTime.setExpression("0.1");
        serviceTime.setTypeEquals(BaseType.DOUBLE);
    }

    /** The service time. This is a double with default 1.0.
     *  It is required to be non-negative.
     */
    public Parameter serviceTime;

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
        if (_tokens.size() > 0 && currentTime.compareTo(_nextTimeFree) == 0) {
            Object[] output = (Object[]) _tokens.take();
            Receiver receiver = (Receiver) output[0];
            Token token = (Token) output[1];
            receiver.put(token);
        }
    }

    /**
     * If there are still tokens in the queue schedule a refiring.
     */
    public boolean postfire() throws IllegalActionException {
        if (_tokens.size() > 0) {
            Time currentTime = getDirector().getModelTime();
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
        _tokens.put(new Object[] { receiver, token });
        // if there was no token in the queue, schedule a refiring.
        if (_tokens.size() == 1) {
            Time currentTime = getDirector().getModelTime();
            _nextTimeFree = currentTime.add(_serviceTimeValue);
            _fireAt(_nextTimeFree);
        }
    }

    /**
     * Reset the quantity manager and clear the tokens.
     */
    public void reset() {
        _tokens.clear();
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
     * Tokens stored for processing.
     */
    private FIFOQueue _tokens;

    /**
     * Next time a token is sent and the next token can be processed.
     */
    private Time _nextTimeFree;

}
