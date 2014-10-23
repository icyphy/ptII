/* An actor that transforms transmission properties using another model.

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
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.hoc.LifeCycleManager;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.PropertyTransformer;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TransmitPropertyTransformer

/**
 This actor reads input tokens and sends them unmodified to the output;
 its role is not to operate on input tokens, but rather to modify the
 properties of a transmission.
 <p>
 This actor implements the PropertyTransformer interface, which provides
 a callback that can be use to modify the transmit properties of a
 transmission.  It register itself and its connected wireless
 output port with the channel that the wireless output port uses.
 The channel will call its transformProperties() method for each
 transmission from the registered output port.
 <p>
 When transformProperties() is called, this actor sets the value
 of three variables and then performs a complete execution of the
 contained model. The three variables are <i>senderLocation</i>
 (an array of doubles), <i>receiverLocation</i> (also an array of
 doubles), and <i>properties</i> (a record token containing the
 transmit properties to be modified). After execution of the contained
 model, the (possibly modified) value of the record <i>properties</i>
 is taken to be the modified properties. Thus, a contained model would
 normally read the variable <i>properties</i>, change it, and use
 a SetVariable actor to set the new value of <i>properties</i>.
 <p>
 This actor expects its output port to be connected directly
 to the inside of a WirelessIOPort belonging to this actor's container.
 It looks for this port in the preinitialize() method, and registers
 with the channel specified by that port.  If there is no such port,
 or no such channel, then preinitialize() throws an exception.
 Note that since this connectivity is checked only during preinitialize(),
 this actor does not support dynamically reconnecting its output port
 during execution of the model.

 @author Yang Zhao, Edward Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (pjb2e)
 */
public class TransmitPropertyTransformer extends LifeCycleManager implements
PropertyTransformer {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TransmitPropertyTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeSameAs(input);

        // Create and configure the variables.
        senderLocation = new Parameter(this, "senderLocation");
        senderLocation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        senderLocation.setExpression("{0.0, 0.0}");
        senderLocation.setVisibility(Settable.EXPERT);

        receiverLocation = new Parameter(this, "receiverLocation");
        receiverLocation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        receiverLocation.setExpression("{0.0, 0.0}");
        receiverLocation.setVisibility(Settable.EXPERT);

        properties = new Parameter(this, "properties");

        // FIXME: properties type should be at least an empty record.
        properties.setExpression("{power = 1.0, range = Infinity}");
        properties.setVisibility(Settable.EXPERT);

        // Create the icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");

        // To ensure that exported MoML does not represent this as
        // an ordinary TypedCompositeActor.
        setClassName("ptolemy.domains.wireless.lib.TransmitPropertyTransformer");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Port that receives the data to be transmitted on the <i>output</i>
     *  port. The type is unconstrained.
     */
    public TypedIOPort input;

    /** Port that sends data to a wireless output. The type is constrained
     *  to be the same as the input.
     */
    public TypedIOPort output;

    /** The location of the sender. This is a double array with default
     *  value {0.0, 0.0}.
     */
    public Parameter senderLocation;

    /** The location of the receiver. This is a double array with default
     *  value {0.0, 0.0}.
     */
    public Parameter receiverLocation;

    /** The properties to be transformed. This is a
     *  record token with value {power = 0.0, range = 0.0}.
     */
    public Parameter properties;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TransmitPropertyTransformer newObject = (TransmitPropertyTransformer) super
                .clone(workspace);

        // set the type constraints
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Read at most one token from the <i>input</i>
     *  port and simply transmit the data to the <i>output</i> port.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token inputValue = input.get(0);
            output.send(0, inputValue);
        }
        // Call super.fire() at the end of fire() instead of at the
        // start of fire() or else
        // wireless/demo/AntennaPattern/TransmitAntennaPattern.xml
        // will not plot.
        super.fire();
    }

    /** Return true, indicating that execution can continue.
     *  @exception IllegalActionException Not thrown in this class,
     *   but declared so the subclasses can throw it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // Do not call the superclass postfire(), as that will
        // call postfire() on the diretor.
        if (_debugging) {
            _debug("Called postfire(), which returns true.");
        }

        return true;
    }

    /** Return true, indicating that this actor is always ready to fire.
     *  @exception IllegalActionException Not thrown in this class,
     *   but declared so the subclasses can throw it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // Do not call the superclass prefire(), as that will
        // call prefire() on the diretor.
        if (_debugging) {
            _debug("Called prefire(), which returns true.");
        }

        return true;
    }

    /** Register with the channel as a PropertyTransformer
     *  for its connected wireless output port. If the output
     *  is not connected directly to a WirelessIOPort, then throw
     *  an exception.
     *  @exception IllegalActionException If the output is not
     *   connected directly to a WirelessIOPort, or if the port's
     *   container does not have a container, or if no channel is
     *   found.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        if (_debugging) {
            _debug("Called preinitialize()");
        }

        boolean foundOne = false;

        //register this property transformer for the connected wireless
        //output port. It assumes there is only one.
        Iterator connectedOutputPorts = output.sinkPortList().iterator();

        while (connectedOutputPorts.hasNext()) {
            IOPort port = (IOPort) connectedOutputPorts.next();

            if (port.isOutput() && port instanceof WirelessIOPort) {
                // Found the port.
                foundOne = true;

                Entity container = (Entity) port.getContainer();
                String channelName = ((WirelessIOPort) port).outsideChannel
                        .stringValue();
                CompositeEntity container2 = (CompositeEntity) container
                        .getContainer();

                if (container2 == null) {
                    throw new IllegalActionException(this,
                            "The port's container does not have a container.");
                }

                Entity channel = container2.getEntity(channelName);

                if (channel instanceof WirelessChannel) {
                    // Cache it here, so no need to do it again in wrapup().
                    _outputWirelessChannel = (WirelessChannel) channel;
                    _wirelessOutputPort = (WirelessIOPort) port;
                    ((WirelessChannel) channel).registerPropertyTransformer(
                            this, (WirelessIOPort) port);
                } else {
                    throw new IllegalActionException(this,
                            "The connected output port does not refer to a "
                                    + "valid channel.");
                }
            }
        }

        //register this property transformer for the connected wireless
        //input port. It assumes there is only one.
        Iterator connectedInputPorts = input.sourcePortList().iterator();

        while (connectedInputPorts.hasNext()) {
            //register this property transformer for the connected wireless
            //output port. It assumes there is only one.
            IOPort port = (IOPort) connectedInputPorts.next();

            if (port.isInput() && port instanceof WirelessIOPort) {
                // Found the port.
                foundOne = true;

                Entity container = (Entity) port.getContainer();
                String channelName = ((WirelessIOPort) port).outsideChannel
                        .stringValue();
                CompositeEntity container2 = (CompositeEntity) container
                        .getContainer();

                if (container2 == null) {
                    throw new IllegalActionException(this,
                            "The port's container does not have a container.");
                }

                Entity channel = container2.getEntity(channelName);

                if (channel instanceof WirelessChannel) {
                    // Cache it here, so no need to do it again in wrapup().
                    _inputWirelessChannel = (WirelessChannel) channel;
                    _wirelessInputPort = (WirelessIOPort) port;
                    ((WirelessChannel) channel).registerPropertyTransformer(
                            this, (WirelessIOPort) port);
                } else {
                    throw new IllegalActionException(this,
                            "The connected input port does not refer to a "
                                    + "valid channel.");
                }
            }
        }

        if (!foundOne) {
            throw new IllegalActionException(this,
                    "Output is not connected to a WirelessIOPort.");
        }
    }

    /** Set the <i>senderLocation</i>, <i>receiverLocation</i>, and
     *  <i>properties</i> variables and execute the contained model.
     *  Return the final value of the <i>properties</i> variable.
     *  @param initialProperties The initial value of the properties.
     *  @param sender The sending port.
     *  @param destination The receiving port.
     *  @return The modified transform properties.
     *  @exception IllegalActionException If executing the model
     *   throws it.
     */
    @Override
    public RecordToken transformProperties(RecordToken initialProperties,
            WirelessIOPort sender, WirelessIOPort destination)
                    throws IllegalActionException {
        double[] p1 = _locationOf(sender);
        double[] p2 = _locationOf(destination);

        DoubleToken[] t1 = new DoubleToken[p1.length];
        DoubleToken[] t2 = new DoubleToken[p2.length];

        for (int i = 0; i < p1.length; i++) {
            t1[i] = new DoubleToken(p1[i]);
        }

        for (int i = 0; i < p2.length; i++) {
            t2[i] = new DoubleToken(p2[i]);
        }

        senderLocation.setToken(new ArrayToken(t1));
        receiverLocation.setToken(new ArrayToken(t2));
        properties.setToken(initialProperties);

        if (_debugging) {
            _debug("----transformProperties is called; "
                    + "execute the subsystem.");
        }

        // FIXME: Should use return value to determine what postfire() returns?
        _executeInsideModel();

        RecordToken result = (RecordToken) properties.getToken();

        if (_debugging) {
            _debug("---- the modified property value is. " + result.toString());
        }

        return result;
    }

    /** Override the base class to unregister this actor with the
     *  channel.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // Do not call the superclass wrapup(), as that will
        // call wrapup() on the diretor.
        if (_debugging) {
            _debug("Called wrapup(), which unregisters the property transformer.");
        }

        if (_outputWirelessChannel != null) {
            _outputWirelessChannel.unregisterPropertyTransformer(this,
                    _wirelessOutputPort);
        }

        if (_inputWirelessChannel != null) {
            _inputWirelessChannel.unregisterPropertyTransformer(this,
                    _wirelessInputPort);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to not read any inputs.
     */
    @Override
    protected void _readInputs() {
    }

    /** Override the base class to not write any outputs.
     */
    @Override
    protected void _writeOutputs() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the location of the given WirelessIOPort.
     *  @param port A port with a location.
     *  @return The location of the port.
     *  @exception IllegalActionException If a valid location attribute cannot
     *   be found.
     */
    private double[] _locationOf(WirelessIOPort port)
            throws IllegalActionException {
        Entity container = (Entity) port.getContainer();
        Locatable location = null;
        location = (Locatable) container.getAttribute(LOCATION_ATTRIBUTE_NAME,
                Locatable.class);

        if (location == null) {
            throw new IllegalActionException(
                    "Cannot determine location for port " + port.getName()
                    + ".");
        }

        return location.getLocation();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The wireless channel for the connected input port found in
     *  preinitialize().
     */
    private WirelessChannel _inputWirelessChannel;

    /** The connected wireless input port found in preinitialize(). */
    private WirelessIOPort _wirelessInputPort;

    /** The wireless channel for the connected output port found in
     *  preinitialize().
     */
    private WirelessChannel _outputWirelessChannel;

    /** The connected wireless output port found in preinitialize(). */
    private WirelessIOPort _wirelessOutputPort;

    /** Name of the location attribute. */
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";
}
