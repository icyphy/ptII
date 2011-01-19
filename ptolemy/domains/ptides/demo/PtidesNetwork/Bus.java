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

import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.lib.Server;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This actor is an {@link QuantityManager} that, when its
 *  {@link #sendToken(Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule. Specifically, if the actor is
 *  not currently servicing a previous token, then it delivers
 *  the token with a delay given by the <i>serviceTime</i> parameter.
 *  If the actor is currently servicing a previous token, then it waits
 *  until it has finished servicing that token (and any other pending
 *  tokens), and then delays an additional amount given by <i>serviceTime</i>.
 *  This is similar to the {@link Server} actor.
 *  Tokens are processed in FIFO order.
 *  <p>
 *  This actor will be used on any communication where the receiving
 *  port has a parameter named "QuantityManager" that refers by name
 *  to the instance of this actor.
 *  @author Patricia Derler
 */
public class Bus extends TypedAtomicActor implements QuantityManager {

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

        serviceTime = new Parameter(this, "serviceTime");
        serviceTime.setExpression("0.1");
        serviceTime.setTypeEquals(BaseType.DOUBLE);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     */
    public IntermediateReceiver getReceiver(Receiver receiver) {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(this, receiver);
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
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero serviceTime: " + value);
            }
            _serviceTimeValue = value;
        }
    }

    /** Send first token in the queue to the target receiver.
     */
    public void fire() throws IllegalActionException { 
        Time currentTime = getDirector().getModelTime();
        // in a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null
        if (_nextTimeFree != null && _tokens.size() > 0 && currentTime.compareTo(_nextTimeFree) == 0) {
            Object[] output = (Object[]) _tokens.get(0);
            Receiver receiver = (Receiver) output[0];
            Token token = (Token) output[1];
            receiver.put(token);
            if (_debugging) {
                _debug("At time " + currentTime + ", completing send to " +
                        receiver.getContainer().getFullName() +
                        ": " + token);
            }
        }
    }

    /** If there are still tokens in the queue and a token has been produced in the fire, 
     *  schedule a refiring.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (_nextTimeFree != null && _tokens.size() > 0 && currentTime.compareTo(_nextTimeFree) == 0) {
            // Discard the token that was sent to the output in fire().
            Object[] output = (Object[]) _tokens.take();
            // Determine the time of the next firing.
            _nextTimeFree = currentTime.add(_serviceTimeValue);
            _nextReceiver = (Receiver) output[0];
            _fireAt(_nextTimeFree);
        }
        return super.postfire();
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param receiver The receiver to send to.
     *  @param token The token to send.
     *  @throws IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver receiver, Token token)
            throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (_nextTimeFree == null || _tokens.size() == 0 
                || currentTime.compareTo(_nextTimeFree) != 0 || receiver != _nextReceiver) {
            // At the current time, there is no token to send.
            // At least in the Continuous domain, we need to make sure
            // the delegated receiver knows this so that it becomes
            // known and absent.
            receiver.put(null);
        }
        
        // If the token is null, then this means there is not actually
        // something to send. Do not take up bus resources for this.
        if (token == null) {
            return;
        }

        _tokens.put(new Object[] { receiver, token });
        // if there was no token in the queue, schedule a refiring.
        if (_tokens.size() == 1) {
            _nextTimeFree = currentTime.add(_serviceTimeValue);
            _nextReceiver = receiver;
            _fireAt(_nextTimeFree);
        }
        if (_debugging) {
            _debug("At time " + getDirector().getModelTime() + ", initiating send to " +
                    receiver.getContainer().getFullName() +
                    ": " + token);
        }
    }

    /**
     * Reset the quantity manager and clear the tokens.
     */
    public void reset() {
        _tokens.clear();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                    ////
    
    /** The service time. This is a double with default 0.1.
     *  It is required to be positive.
     */
    public Parameter serviceTime;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Delay imposed on every token. */
    private double _serviceTimeValue;

    /** Tokens stored for processing. */
    private FIFOQueue _tokens;

    /** Next time a token is sent and the next token can be processed. */
    private Time _nextTimeFree;

    /** Next receiver to which the next token to be sent is destined. */
    private Receiver _nextReceiver;
}
