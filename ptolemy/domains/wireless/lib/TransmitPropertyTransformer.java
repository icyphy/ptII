/* An actor that transforms transmission properties using another model.

 Copyright (c) 2004 The Regents of the University of California.
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

package ptolemy.domains.wireless.lib;

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.RunCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.wireless.kernel.PropertyTransformer;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TransmitPropertyTransformer

/**
This actor reads input tokens and sends them unmodified to the output;
its role is not to operate on input tokens, but rather to modify the
properties of a transmission.

This actor implements the PropertyTransformer interface with a callback
that can be use to modify the transmit properties of a transmission.
It register itself and its connected wireless
output port with the channel that the wireless output port uses.
The channel will call its transformProperties() method for each
transmission from the registed output port.

<p>FIXME: this is going to be changed to work like RunCompositeActor.
This actor has a <i>modelFileOrURL</i> parameter that specify a model
used to calculate the properties. When transformProperties() is
called, it calls the ModelUtilities.executeModel() method to execute
the specified model and return the (possibly) modified property to the
channel.

<p>The specified model should calculate/modify the properties based on
the sender's location and the receiver's location. It should contains
attributes of "SenderLocation", "ReceiverLocation" and
"Properties". This actor will use this attributes to pass the sender
and receiver's location and the current properties information to the
specified model and get the new properties back from it.

@author Yang Zhao, Edward Lee
@version $Id$
*/
public class TransmitPropertyTransformer extends RunCompositeActor
    implements PropertyTransformer {
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
        // Create and configure the parameters.
        senderLocation = new Parameter(this, "senderLocation");
        //senderLocation.setTypeEquals(BaseType.GENERAL);
        senderLocation.setExpression("{0.0, 0.0}");

        receiverLocation = new Parameter(this, "receiverLocation");
        //receiverLocation.setTypeEquals(BaseType.GENERAL);
        receiverLocation.setExpression("{0.0, 0.0}");

        property = new Parameter(this, "property");
        //property.setTypeEquals(BaseType.GENERAL);
        property.setExpression("{power = 0.0, range = 0.0}");

        //modelFileOrURL = new FileParameter(this, "modelFileOrURL");
        // Create the icon.
        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
        getMoMLInfo().className =
            "ptolemy.domains.wireless.lib.TransmitPropertyTransformer";
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

    /** The x/y location of the sender.  The default value is a double
     *  array of length 2: {0.0, 0.0}
     */   
    public Parameter senderLocation;

    /** The x/y location of the receiver.  The default value is a double
     *  array of length 2: {0.0, 0.0}
     */   
    public Parameter receiverLocation;

    /** The properties of the transformer.  The default value is a 
     *  record token with value {power = 0.0, range = 0.0}.
     */
    public Parameter property;

    /** The file name or URL of the model that this actor invokes to
     *  transform properties.
     */
    //public FileParameter modelFileOrURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and dissociates itself with the specified model.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        TransmitPropertyTransformer newObject =
            (TransmitPropertyTransformer)(super.clone(workspace));

        newObject._model = null;
        // set the type constraints
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Read at most one token from the <i>input</i>
     *  port and simply transmit the data on the <i>output</i> port.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    public void fire() throws IllegalActionException {

        if (input.hasToken(0)) {
            Token inputValue = input.get(0);
            if (_debugging) {
                _debug("Input data received: " + inputValue.toString());
            }
            output.send(0, inputValue);
        }
    }

    /** Register itself with the channel as a PropertyTransformer
     *  for its connected wireless output port.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize()");
        }
        _isSubclassOfThis = true;
        Iterator connectedPorts = output.sinkPortList().iterator();
        while (connectedPorts.hasNext()) {
            IOPort port = (IOPort)connectedPorts.next();
            if (!port.isInput() && port instanceof WirelessIOPort) {
                // Found the port.
                Entity container = (Entity)(port.getContainer());
                String channelName
                    = ((WirelessIOPort)port).outsideChannel.stringValue();
                CompositeEntity container2 = (CompositeEntity)
                    container.getContainer();
                if (container2 == null) {
                    throw new IllegalActionException(this,
                            "The container does not have a container.");
                }
                Entity channel = container2.getEntity(channelName);
                if (channel instanceof WirelessChannel) {
                    //Cach it here, so no need to do it again in wrapup().
                    _channel = (WirelessChannel)channel;
                    _wirelessIOPort = (WirelessIOPort)port;
                    ((WirelessChannel)channel).
                        registerPropertyTransformer(this,
                                (WirelessIOPort)port);
                } else {
                    throw new IllegalActionException(this,
                            "The connected port does not refer to a "
                            + "valid channel.");
                }
            }
        }
    }

    /** Return true, indicating that execution can continue.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public boolean postfire() throws IllegalActionException {
        return true;
    }

    /** Invoke the execution of the subsysteml and return the result.
     *  see RunCompositeActor.fire().
     * @param properties The transform properties.
     * @param sender The sending port.
     * @param destination The receiving port.
     * @return The modified transform properties.
     * @exception IllegalActionException If failed to execute the model.
     */
    public RecordToken transformProperties(RecordToken properties,
            WirelessIOPort sender, WirelessIOPort destination)
            throws IllegalActionException {
        double[] p1 = _locationOf(sender);
        double[] p2 = _locationOf(destination);

        DoubleToken[] t1 = new DoubleToken[p1.length];
        DoubleToken[] t2 = new DoubleToken[p2.length];
        for(int i = 0; i < p1.length; i++) {
            t1[i] = new DoubleToken(p1[i]);
        }
        for(int i = 0; i < p2.length; i++) {
            t2[i] = new DoubleToken(p2[i]);
        }
        senderLocation.setToken(new ArrayToken(t1));
        receiverLocation.setToken(new ArrayToken(t2));
        property.setToken(properties);

        if (_debugging) {
            _debug("----transformProperties is called, "
                    + "execute the subsystem.");
        }

        try {
            setDeferChangeRequests(true);
            _executeInsideModel();
        } finally {
            try {
                super.wrapup();
                if (_debugging) {
                    _debug("---- Firing of RunCompositeActor is complete.");
                }
            } finally {
                // Indicate that it is now safe to execute
                // change requests when they are requested.
                setDeferChangeRequests(false);
            }
        }
        RecordToken result = (RecordToken)property.getToken();
        if (_debugging) {
            _debug("---- the modified property is. "
                    + result.toString());
        }
        return result;
    }

    /** Override the base class to call wrap up to unregister this with the
     *  channel and do nothing else.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public void wrapup() throws IllegalActionException{
        if (_debugging) {
            _debug("Called wrapup()");
        }
        //setDeferChangeRequests(false);
        if (_channel != null) {
            if (_debugging) {
                _debug("unregister property transformer in wrapup()");
            }
            _channel.unregisterPropertyTransformer(this,
                    _wirelessIOPort);
        }
    }

    /** Return the location of the given WirelessIOPort.
     *  @param port A port with a location.
     *  @return The location of the port.
     *  @exception IllegalActionException If a valid location attribute cannot
     *   be found.
     */
    private double[] _locationOf(WirelessIOPort port)
            throws IllegalActionException {
        Entity container = (Entity)port.getContainer();
        Locatable location = null;
        location = (Locatable)container.getAttribute(
                LOCATION_ATTRIBUTE_NAME, Locatable.class);
        if (location == null) {
            throw new IllegalActionException(
                    "Cannot determine location for port "
                    + port.getName()
                    + ".");
        }
        return location.getLocation();
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    private CompositeActor _executable;
    private NamedObj _model;
    private WirelessChannel _channel;
    private WirelessIOPort _wirelessIOPort;
    /** Indicator of what the last call to iterate() returned. */
    private int _lastIterateResult = NOT_READY;
    // Name of the location attribute.
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";
}
