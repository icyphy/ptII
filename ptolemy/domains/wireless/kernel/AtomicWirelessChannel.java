/* The base class of communication channels in the sensor domain.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (sanjeev@eecs.berkeley.edu)
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/
package ptolemy.domains.wireless.kernel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;

//////////////////////////////////////////////////////////////////////////
//// AtomicWirelessChannel
/**

The base class for communication channels in the wireless domain.

<p>To use this class, place it in a model that contains wireless actors
(actors whose ports are instances of WirelessIOPort).  Then set the
<i>outsideChannel</i> parameter of those ports to match the name of
this channel.  The model can also itself contain ports that are
instances of WirelessIOPort, in which case their <i>insideChannel</i>
parameter should contain the name of this channel.

<p>
In this base class, transmission on a channel reaches all ports at the
same level of the hierarchy that are instances of WirelessIOPort and
that specify that they use this channel. These ports include those
contained by entities that have the container as this channel and
that have their <i>outsideChannel</i> parameter set to the name
of this channel.  They also include those ports whose containers
are the same as the container of this channel and whose
<i>insideChannel</i> parameter matches this channel name.
<p>
Derived classes will typically limit the range of the transmission,
using for example location information from the ports. They
may also introduce random losses or corruption of data.  To do this,
derived classes can override the _isInRange() protected method,
or the transmit() public method.

@author Xiaojun Liu and Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
*/
public class AtomicWirelessChannel extends TypedAtomicActor
    implements WirelessChannel, ValueListener {

    /** Construct a relation with the given name contained by the specified
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
    public AtomicWirelessChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        defaultProperties = new Parameter(this, "defaultProperties");
        // Force this to be a record type without specifying the fields.
        // NOTE: This doesn't actually work because the type remains
        // unknown, which triggers an error message. Instead, we check
        // the type in attributeChanged().
        // defaultProperties.setTypeAtMost(
        //      new RecordType(new String[0], new Type[0]));

        _channelPort = new ChannelPort(this, "_channelPort");

        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-25,0 8,-8 2,2 25,0 -8,8 -2,-2 -25,0\" " +
                "style=\"fill:red\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////

    /** The default properties for transmission. In this base class,
     *  the type and contents are left undefined.  Derived classes
     *  will define this to be a record.  The fields of the record
     *  determine what properties are seen by the receiver.  Any
     *  fields that are not in this parameter value will be discarded
     *  before properties are delivered to the receiver.
     */
    public Parameter defaultProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is defaultProperties, make sure
     *  its value is a record token.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == defaultProperties) {
            Token value = defaultProperties.getToken();
            if (value != null && !(value instanceof RecordToken)) {
                throw new IllegalActionException(this,
                        "Expected a record for defaultProperties but got: "
                        + value);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return a channel port that can be used to set type constraints
     *  between senders and receivers.
     */
    public ChannelPort getChannelPort() {
        return _channelPort;
    }

    /** Initialize the _portPropertyTransformer set.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _portPropertyTransformer = new HashMap();
    }

    /** Return a list of input ports that can potentially receive data
     *  from this channel.  This includes input ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return A new list of input ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>outsideChannel</i> parameter cannot be evaluated.
     */
    public List listeningInputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            // NOTE: This caching relies on the fact that WirelessIOPort
            // will increment the workspace version number if any
            // parameter identifying the channel changes.
            if (workspace().getVersion() == _listeningInputPortsVersion) {
                return _listeningInputPorts;
            }
            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity)getContainer();
            Iterator entities = container.entityList().iterator();
            while (entities.hasNext()) {
                Entity entity = (Entity)entities.next();
                Iterator ports = entity.portList().iterator();
                while (ports.hasNext()) {
                    Port port = (Port)ports.next();
                    if (port instanceof WirelessIOPort) {
                        WirelessIOPort castPort = (WirelessIOPort)port;
                        if (castPort.isInput()) {
                            String channelName
                                = castPort.outsideChannel.stringValue();
                            if (channelName.equals(getName())) {
                                result.add(port);
                            }
                        }
                    }
                }
            }
            _listeningInputPorts = result;
            _listeningInputPortsVersion = workspace().getVersion();
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return a list of output ports that can potentially receive data
     *  from this channel.  This includes output ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return The list of output ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>insideChannel</i> parameter cannot be evaluated.
     */
    public List listeningOutputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            // NOTE: This caching relies on the fact that WirelessIOPort
            // will increment the workspace version number if any
            // parameter identifying the channel changes.
            if (workspace().getVersion() == _listeningOutputPortsVersion) {
                return _listeningOutputPorts;
            }
            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity)getContainer();
            Iterator ports = container.portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                if (port instanceof WirelessIOPort) {
                    WirelessIOPort castPort = (WirelessIOPort)port;
                    if (castPort.isOutput()) {
                        String channelName =
                            castPort.insideChannel.stringValue();
                        if (channelName.equals(getName())) {
                            result.add(port);
                        }
                    }
                }
            }
            _listeningOutputPorts = result;
            _listeningOutputPortsVersion = workspace().getVersion();
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Remove the dependency between the dummy port returned by
     *  getChannelPort() and itself.
     */
    public void removeDependencies() {
        removeDependency(_channelPort, _channelPort);
    }
    
    /** Register a PropertyTransformer for a channel. The channel may 
     *  invoke its PropertyTransformers for each token delivered by this
     *  channel.
     */
    public void registerPropertyTransformer(PropertyTransformer transformer) {
        //do nothing here, the extended class should over write this method
        //when it is needed. see TerrainChannel.
    }
    
    /** Register a PropertyTransformer for a wirelessIOPort.
     */
    public void registerPropertyTransformer(WirelessIOPort port, 
            PropertyTransformer transformer) {
        _portPropertyTransformer.put(port, transformer);
    }
    /** Return a list of input ports that can potentially send data
     *  to this channel.  This includes input ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return The list of output ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>insideChannel</i> parameter cannot be evaluated.
     */
    public List sendingInputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            // NOTE: This caching relies on the fact that WirelessIOPort
            // will increment the workspace version number if any
            // parameter identifying the channel changes.
            if (workspace().getVersion() == _sendingInputPortsVersion) {
                return _sendingInputPorts;
            }
            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity)getContainer();
            Iterator ports = container.portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                if (port instanceof WirelessIOPort) {
                    WirelessIOPort castPort = (WirelessIOPort)port;
                    if (castPort.isInput()) {
                        String channelName =
                            castPort.insideChannel.stringValue();
                        if (channelName.equals(getName())) {
                            result.add(port);
                        }
                    }
                }
            }
            _sendingInputPorts = result;
            _sendingInputPortsVersion = workspace().getVersion();
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return a list of output ports that can potentially send data
     *  to this channel.  This includes output ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return A new list of input ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>outsideChannel</i> parameter cannot be evaluated.
     */
    public List sendingOutputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            // NOTE: This caching relies on the fact that WirelessIOPort
            // will increment the workspace version number if any
            // parameter identifying the channel changes.
            if (workspace().getVersion() == _sendingOutputPortsVersion) {
                return _sendingOutputPorts;
            }
            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity)getContainer();
            Iterator entities = container.entityList().iterator();
            while (entities.hasNext()) {
                Entity entity = (Entity)entities.next();
                Iterator ports = entity.portList().iterator();
                while (ports.hasNext()) {
                    Port port = (Port)ports.next();
                    if (port instanceof WirelessIOPort) {
                        WirelessIOPort castPort = (WirelessIOPort)port;
                        if (castPort.isOutput()) {
                            String channelName
                                = castPort.outsideChannel.stringValue();
                            if (channelName.equals(getName())) {
                                result.add(port);
                            }
                        }
                    }
                }
            }
            _sendingOutputPorts = result;
            _sendingOutputPortsVersion = workspace().getVersion();
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Transmit the specified token from the specified port with the
     *  specified properties.  All ports that are in range will receive
     *  the token if they have room in their receiver.
     *  Note that in this base class, a port is in range if it refers to
     *  this channel by name and is at the right place in the hierarchy.
     *  This base class makes no use of the properties argument.
     *  But derived classes may limit the range or otherwise change
     *  transmission properties using this argument.
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
                        "AtomicWirelessChannel can only work "
                        + "with a WirelessDirector.");
            }
            Iterator receivers =
                _receiversInRange(port, properties).iterator();
            if (_debugging) {
                _debug("----\nTransmitting from port: " + port.getFullName());
                _debug("Token value: " + token.toString());
                if (receivers.hasNext()) {
                    _debug("Receivers in range:");
                } else {
                    _debug("No receivers in range.");
                }
            }
            while (receivers.hasNext()) {
                WirelessReceiver receiver
                    = (WirelessReceiver)receivers.next();
                _transmitTo(token, port, receiver, properties);
            }
        } finally {
            workspace().doneReading();
        }
    }

    /** React to the fact that the specified Settable has changed.
     *  This base class registers as a listener to attributes that
     *  specify the location of objects (and implement the Locatable
     *  interface) so that it is notified by a call to this method
     *  when the location changes.  In this base class, this method
     *  only sets a flag to invalidate its cached list of receivers
     *  in range.  Subclass may do more, for example to determine
     *  whether a receiver that is in process of receiving a message
     *  with a non-zero duration is still in range.
     *  @param settable The object that has changed value.
     */
    public void valueChanged(Settable settable) {
        if (settable instanceof Locatable) {
            _receiversInRangeCacheValid = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the distance between two ports.  This is a convenience
     *  method provided to make it easier to write subclasses that
     *  limit transmission range using position information.
     *  @param port1 The first port.
     *  @param port2 The second port.
     *  @return The distance between the two ports.
     *  @exception IllegalActionException If the distance
     *   cannot be determined.
     */
    protected double _distanceBetween(
            WirelessIOPort port1, WirelessIOPort port2)
            throws IllegalActionException {
        double[] p1 = _locationOf(port1);
        double[] p2 = _locationOf(port2);
        return Math.sqrt((p1[0] - p2[0])*(p1[0] - p2[0])
                + (p1[1] - p2[1])*(p1[1] - p2[1]));
    }

    /** Return true if the specified port is in range of the
     *  specified source port, assuming the source port transmits with
     *  the specified properties.  In this base class, this method returns
     *  true always.  The method assumes that the two ports are
     *  communicating on the same channel, but it does not check
     *  this.  This should be checked by the calling method.
     *  Derived classes will typically use position information
     *  in the source or destination to determine whether ports
     *  are in range.
     *  @param source The source port.
     *  @param destination The destination port.
     *  @param properties The transmit properties (ignored in this base class).
     *  @return True if the destination is in range of the source.
     *  @exception IllegalActionException If it cannot be determined
     *   whether the destination is in range (not thrown in this base
     *   class).
     */
    protected boolean _isInRange(
            WirelessIOPort source,
            WirelessIOPort destination,
            RecordToken properties)
            throws IllegalActionException {
        return true;
    }

    /** Return the location of the given port. If the container of the
     *  specified port is the container of this channel, then use the
     *  "_location" attribute of the port.  Otherwise, use the
     *  "_location" attribute of its container. In either case,
     *  register a listener to the location attribute so that valueChanged()
     *  will be called if and when that location changes.
     *  The calling method is expected to have read access on the workspace.
     *  Subclasses may override this method to provide some other way of
     *  obtaining location information.
     *  @param port A port with a location.
     *  @return The location of the port.
     *  @exception IllegalActionException If a valid location attribute cannot
     *   be found.
     */
    protected double[] _locationOf(IOPort port) throws IllegalActionException {
        Entity container = (Entity)port.getContainer();
        Locatable location = null;
        if (container == getContainer()) {
            location = (Locatable)port.getAttribute(LOCATION_ATTRIBUTE_NAME,
                    Locatable.class);
        } else {
            location = (Locatable)container.getAttribute(
                    LOCATION_ATTRIBUTE_NAME, Locatable.class);
        }
        if (location == null) {
            throw new IllegalActionException(
                    "Cannot determine location for port "
                    + port.getName()
                    + ".");
        }
        // NOTE: We assume here that the implementation
        // of addValueListener() is smart enough to not add
        // this if it is already a listener.
        location.addValueListener(this);
        return location.getLocation();
    }

    /** Return the list of receivers that can receive from the specified
     *  port with the specified transmit properties. Ports that are contained
     *  by the same container as the specified <i>sourcePort</i> are
     *  not included.  Note that this method does
     *  not guarantee that those receivers will receive.  That is determined
     *  by the transmit() method, which subclasses may override to, for
     *  example, introduce probabilistic message losses.
     *  The calling method is expected to have read access on the workspace.
     *  @param sourcePort The sending port.
     *  @param properties The transmit properties (ignored in this base class).
     *  @return A list of instances of WirelessReceiver.
     *  @exception IllegalActionException If a location of a port cannot be
     *   evaluated.
     */
    protected List _receiversInRange(
            WirelessIOPort sourcePort,
            RecordToken properties)
            throws IllegalActionException {
        // This information is carefully cached in
        // a hashtable indexed by the source port.  The cache should
        // be invalidated if:
        //  1) The workspace version changes (which will happen if
        //     any node changes the channel it uses, or if nodes
        //     appear or disappear).
        //  2) The sourcePort has changed its properties parameters
        //     (because this could affect whether other ports are in range).
        //     This handled by a subclass that uses these properties, like
        //     LimitedRangeChannel.
        //  3) Any listening port has changed its location.  Any
        //     subclass that is using location information needs to
        //     listen for changes in that location information and
        //     invalidate the cache if it changes.
        //  Use the performance.xml test to determine whether/how much
        //  this helps.
        if (_receiversInRangeCache != null
                && _receiversInRangeCache.containsKey(sourcePort)
                && ((Long)_receiversInRangeCacheVersion.get(sourcePort))
                .longValue() == workspace().getVersion()
                && _receiversInRangeCacheValid) {
            // Cached list is valid. Return that.
            return (List)_receiversInRangeCache.get(sourcePort);
        }
        List receiversInRangeList = new LinkedList();
        Iterator ports = listeningInputPorts().iterator();
        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort)ports.next();

            // Skip ports contained by the same container as the source.
            if (port.getContainer() == sourcePort.getContainer()) continue;

            if (_isInRange(sourcePort, port, properties)) {
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        receiversInRangeList.add(receivers[i][j]);
                    }
                }
            }
        }
        ports = listeningOutputPorts().iterator();
        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort)ports.next();

            if (_isInRange(sourcePort, port, properties)) {
                Receiver[][] receivers = port.getInsideReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        receiversInRangeList.add(receivers[i][j]);
                    }
                }
            }
        }
        if (_receiversInRangeCache == null) {
            _receiversInRangeCache = new HashMap();
            _receiversInRangeCacheVersion = new HashMap();
        }
        _receiversInRangeCache.put(sourcePort, receiversInRangeList);
        _receiversInRangeCacheVersion.put(
                sourcePort, new Long(workspace().getVersion()));
        _receiversInRangeCacheValid = true;
        return receiversInRangeList;
    }

    /** Transform the properties to take into account channel losses,
     *  noise, etc., for transmission between the specified sender
     *  and the specified receiver.  In this base class, the
     *  specified properties are merged with the defaultProperties
     *  so that the resulting properties contain at least all the
     *  fields of the defaultProperties.
     *  @param properties The transmit properties.
     *  @param sender The sending port.
     *  @param receiver The receiving port.
     *  @return The transformed properties.
     *  @exception IllegalActionException If the properties cannot
     *   be transformed. Not thrown in this base class.
     */
    protected RecordToken _transformProperties(
            RecordToken properties,
            WirelessIOPort sender,
            WirelessReceiver receiver)
            throws IllegalActionException {
        RecordToken result = properties;
        Token defaultPropertiesValue = defaultProperties.getToken();
        if (properties != null &&
                defaultPropertiesValue instanceof RecordToken) {
            result = RecordToken.merge(
                    properties, (RecordToken)defaultPropertiesValue);
        }
        if (_debugging) {
            if (result != null) {
                _debug(" * transmit properties: \""
                        + result.toString()
                        + "\".");
            } else {
                _debug(" * no transmit properties.\"");
            }
        }

        WirelessIOPort destination = (WirelessIOPort)receiver.getContainer();

        if(_portPropertyTransformer.get(sender) != null) {
            PropertyTransformer propertyTransformer = (PropertyTransformer)
                _portPropertyTransformer.get(sender);
            result = propertyTransformer.
                getProperty(result, sender, destination);
        }
        if(_portPropertyTransformer.get(destination) != null) {
            PropertyTransformer propertyTransformer = (PropertyTransformer)
                _portPropertyTransformer.get(destination);
            result = propertyTransformer.
                getProperty(result, sender, destination);
        }
        return result;
    }

    /** Transmit the specified token to the specified receiver.
     *  If necessary, the token will be converted to the resolved
     *  type of the port containing the specified receiver.
     *  @param token The token to transmit, or null to clear
     *   the specified receiver.
     *  @param sender The sending port.
     *  @param receiver The receiver to which to transmit.
     *  @param properties The transmit properties (ignored in this base class).
     *  @exception IllegalActionException If the token cannot be converted
     *   or if the token argument is null and the destination receiver
     *   does not support clear.
     */
    protected void _transmitTo(
            Token token,
            WirelessIOPort sender,
            WirelessReceiver receiver,
            RecordToken properties)
            throws IllegalActionException {
        if (_debugging) {
            _debug(" * transmitting to: "
                    + receiver.getContainer().getFullName());
        }
        if (token != null) {
            if (receiver.hasRoom()) {
                WirelessIOPort destination = (WirelessIOPort)
                    receiver.getContainer();
                Token newToken = destination.convert(token);
                // Bundle the properties.
                Token transformedProperties = _transformProperties(
                        properties,
                        sender,
                        receiver);
                receiver.put(newToken, transformedProperties);
            }
        } else {
            receiver.clear();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Flag indicating that the cached list of receivers in range
     *  is valid.  This gets set to false whenever
     *  a location for some object whose location
     *  has been obtained by _locationOf() has changed since
     *  the last time this cached list was constructed. In addition,
     *  subclasses may invalidate this if anything else that affects
     *  whether a receiver is in range changes (such as the transmit
     *  properties of a port).
     */
    protected boolean _receiversInRangeCacheValid = false;

    /** The PropertyTransformers that have been registered
     *  @see #registerPropertyTransformer(WirelessIOPort, PropertyTransformer)
     */  
    protected HashMap _portPropertyTransformer;

    //protected boolean _portPropertyTransformerInitialized = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Caches of port lists.
    private List _listeningInputPorts;
    private long _listeningInputPortsVersion = -1L;
    private List _listeningOutputPorts;
    private long _listeningOutputPortsVersion = -1L;

    private HashMap _receiversInRangeCache;
    private HashMap _receiversInRangeCacheVersion;

    private List _sendingInputPorts;
    private long _sendingInputPortsVersion = -1L;
    private List _sendingOutputPorts;
    private long _sendingOutputPortsVersion = -1L;

    // Name of the location attribute.
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";

    /** Dummy port used to reduce the type constraints to 2N
     *  rather than N^2.  This port is returned by instances of
     *  WirelessIOPort when asked for sink ports.  Do not send
     *  data to this port, however. Instead, use the transmit()
     *  method.
     */
    private ChannelPort _channelPort;
}
