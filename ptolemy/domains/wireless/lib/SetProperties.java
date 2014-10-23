/* An actor that sets the transmit properties of a connected wireless
 output port.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.domains.wireless.lib;

import java.util.Iterator;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SetProperties

/**
 On each firing, this actor reads at most one token from the <i>payload</i>
 and <i>properties</i> input ports, outputs the payload on the
 <i>output</i> port, and set the <i>outsideTransmitProperties</i> of the
 wireless output port connected to the <i>output</i> port with the specified
 transmit properties received from the <i>properties</i> input port. If
 there is no token received on the <i>properties</i> input port, this actor
 will not modify the <i>outsideTransmitProperties</i> of the connected
 wirelessIOPort, i.e. the payload will be transmitted with the previous
 transmit properties.

 @author Yang Zhao, Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)

 */
public class SetProperties extends TypedAtomicActor {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SetProperties(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        payload = new TypedIOPort(this, "payload", true, false);
        new SingletonParameter(payload, "_showName")
        .setToken(BooleanToken.TRUE);

        properties = new TypedIOPort(this, "properties", true, false);
        new SingletonParameter(properties, "_showName")
        .setToken(BooleanToken.TRUE);

        // FIXME: This should be constrained to be a record token.
        // How to do that?
        // Create and configure the ports.
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeSameAs(payload);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:green\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port that receives the payload to be transmitted.
     */
    public TypedIOPort payload;

    /** Output port that output the received payload. This has the same type as
     *  the <i>payload</i> port.
     */
    public TypedIOPort output;

    /** Input port that receives the properties to be used for transmission
     *  on the connected wireless output port. The type of this port
     *  is a record type.
     */
    public TypedIOPort properties;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SetProperties newObject = (SetProperties) super.clone(workspace);

        // set the type constraints
        newObject.output.setTypeSameAs(newObject.payload);
        return newObject;
    }

    /** reads one token from the <i>payload</i> input port, and simply output
     *  the token on the output port. If there is token at the <i>properties</i>
     *  input port, read the properties value and use it to set the
     *  <i>outsideTransmitProperties</i> of the connected wireless output port.
     *  @exception IllegalActionException If the specified port is not
     *   an instance of WirelessIOPort, or if there is no such port.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (properties.hasToken(0)) {
            Token propertiesValue = properties.get(0);

            // The following will throw an exception if the value is
            // not a RecordToken.
            Iterator connectedPorts = output.sinkPortList().iterator();

            while (connectedPorts.hasNext()) {
                IOPort port = (IOPort) connectedPorts.next();

                if (port.isOutput() && port instanceof WirelessIOPort) {
                    // Found the port.
                    ((WirelessIOPort) port).outsideTransmitProperties
                    .setToken(propertiesValue);
                }
            }
        }

        if (payload.hasToken(0)) {
            Token inputValue = payload.get(0);

            if (_debugging) {
                _debug("Input data received: " + inputValue.toString());
            }

            output.send(0, inputValue);
        }
    }
}
