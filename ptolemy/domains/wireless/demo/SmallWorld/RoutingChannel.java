/* A channel costomized for the SmallWorld demo.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (pjb2e@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.demo.SmallWorld;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessDirector;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.domains.wireless.kernel.WirelessReceiver;
import ptolemy.domains.wireless.lib.LimitedRangeChannel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// RoutingChannel

/**
This channel is specially designed for the SmallWorld demo. Since the routing
algrithm assumes that the SmallWorldRoute to have knowledge of which
node can receive message from it, it is more naturally to determine the 
connected node set in SmallWorldRoute actor rather than in the Channel. However, 
in order to deliver the message to all the corresponding receiving node, 
the channel needs to know the connected set also when the actor call transmit.
This is the major reason to have this channel.


@author Yang Zhao
@version $ $
*/
public class RoutingChannel extends LimitedRangeChannel {

    /** Construct a channel with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public RoutingChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        longLinkFailProbability = new Parameter(this, "longLinkFailProbability");
        longLinkFailProbability.setTypeEquals(BaseType.DOUBLE);
        longLinkFailProbability.setExpression("0.0");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The probability that a call to transmit() will fail to deliver
     *  the token to a receiver that is outside the sure range.
     *  This is a double that defaults to 0.0, which means that
     *  no loss occurs.
     */
    public Parameter longLinkFailProbability;
    
    /** Transmit the specified token from the specified port with the
     *  specified properties.  All ports that are in range will receive
     *  the token if they have room in their receiver.
     *  Note that in this class, a port is in range is determined by 
     *  whether its container is in the connected node set of the
     *  source port's container.
     *  @param token The token to transmit, or null to clear all
     *   receivers that are in range.
     *  @param port The port from which this is being transmitted.
     *  @param properties The transmit properties (ignored in this base class).
     *  @exception IllegalActionException If a location cannot be evaluated
     *   for a port, or if a type conflict occurs, or the director is not
     *   a WirelessDirector.
     */
    public void transmit(Token token, WirelessIOPort port,
            RecordToken properties)
            throws IllegalActionException {
        try {
            workspace().getReadAccess();
            // The following check will ensure that receivers are of type
            // WirelessReceiver.
            if (!(getDirector() instanceof WirelessDirector)) {
                throw new IllegalActionException(this,
                        "WirelessChannel can only work with a WirelessDirector.");
            }
            Entity container = (Entity) port.getContainer();
            if (container instanceof SmallWorldRouter){
                double range = Double.POSITIVE_INFINITY;
                boolean rangeIsSet = false;
                if (properties != null) {
                    Token field = properties.get("range");
                    if (field instanceof ScalarToken) {
                        // NOTE: This may throw a NotConvertibleException, if,
                        // example, a Complex or a Long is given.
                        range = ((ScalarToken)field).doubleValue();
                        rangeIsSet = true;
                    }
                }
                if (!rangeIsSet) {
                    // Type constraints in the constructor make the casts safe.
                    RecordToken defaultPropertiesValue
                            = (RecordToken)defaultProperties.getToken();
                    // Type of the field must be convertible to double, but
                    // need not actually be a double.
                    ScalarToken field = (ScalarToken)defaultPropertiesValue.get("range");
                    range = field.doubleValue();
                }
                Iterator nodes = 
                        ((SmallWorldRouter)container)._connectedNodes.iterator();
                while (nodes.hasNext()) {
                    Actor node = (Actor) nodes.next();
                    
                    
                    Iterator ports = node.inputPortList().iterator();
                    while(ports.hasNext()){
                        TypedIOPort inPort = (TypedIOPort) ports.next();
                        if (inPort instanceof WirelessIOPort) {
                            WirelessIOPort in = (WirelessIOPort)inPort;
                            String channelName = in.outsideChannel.stringValue();
                            if (channelName.equals(getName())){
                                double d = _distanceBetween(port, in);
                                double experiment = _random.nextDouble();
                                double probability = ((DoubleToken)
                                        longLinkFailProbability.getToken())
                                        .doubleValue();
                                if (d<=range) probability = 0.0;
                                if (probability <1.0 && experiment >= probability) {
                                Receiver[][] receivers = in.getReceivers();
                                for (int i = 0; i < receivers.length; i++) {
                                    for (int j = 0; j < receivers[i].length; j++) {
                                        WirelessReceiver r = 
                                                (WirelessReceiver)receivers[i][j];
                                        _transmitTo(token, port, r, properties);
                                    }
                                }
                            }    
                        }    
                    }
                    }
                }
            } else{
                super.transmit(token, port, properties);
            }
        } finally {
            workspace().doneReading();
        }
    }
    
}
