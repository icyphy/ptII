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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// WirelessChannel
/**
This is the base class for communication channels in the wireless domain.
To use it, place it in a model that contains wireless actors (actors
whose ports are instances of WirelessIOPort).  Then set the
<i>outsideChannel</i> parameter of those ports to match the name of
this channel.  The model can also itself contain ports that are
instances of WirelessIOPort, in which case their <i>insideChannel</i>
parameter should contain the name of this channel.
<p>
In this base class, tranmission on a channel reaches all ports at the
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
public class WirelessChannel extends TypedAtomicActor {

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
    public WirelessChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-25,0 8,-8 2,2 25,0 -8,8 -2,-2 -25,0\" " +
                "style=\"fill:red\"/>\n" +
                "</svg>\n");        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
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
            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity)getContainer();
            Iterator ports = container.portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                if (port instanceof WirelessIOPort) {
                    WirelessIOPort castPort = (WirelessIOPort)port;
                    if (castPort.isOutput()) {
                        String channelName = castPort.insideChannel.stringValue();
                        if (channelName.equals(getName())) {
                            result.add(port);
                        }
                    }
                }
            }
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
            return result;
        } finally {
            workspace().doneReading();
        }
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
            List result = new LinkedList();
            CompositeEntity container = (CompositeEntity)getContainer();
            Iterator ports = container.portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                if (port instanceof WirelessIOPort) {
                    WirelessIOPort castPort = (WirelessIOPort)port;
                    if (castPort.isInput()) {
                        String channelName = castPort.insideChannel.stringValue();
                        if (channelName.equals(getName())) {
                            result.add(port);
                        }
                    }
                }
            }
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
     *   for a port, or if a type conflict occurs.
     */
    public void transmit(Token token, WirelessIOPort port, Token properties)
            throws IllegalActionException {
        try {
            workspace().getReadAccess();
            Iterator receivers = _receiversInRange(port, properties).iterator();
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
                Receiver receiver = (Receiver)receivers.next();
                if (_debugging) {
                    _debug(" * " + receiver.getContainer().getFullName());
                }
                // FIXME: Check types?
                if (token != null) {
                    if (receiver.hasRoom()) {
                        receiver.put(token);
                    }
                } else {
                    receiver.clear();
                }
            }
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the distance between two ports.  This is a convenience
     *  method provided to make it easier to write subclasses that
     *  limit transmision range using position information.
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
     *  @throws IllegalActionException If it cannot be determined
     *   whether the destination is in range (not thrown in this base
     *   class).
     */
    protected boolean _isInRange(
            WirelessIOPort source, WirelessIOPort destination, Token properties)
            throws IllegalActionException {
        return true;
    }

    /** Return the location of the given port. If the container of the
     *  port is the container of this channel, then use the
     *  "_location" attribute of the port.  Otherwise, use the
     *  "_location" attribute of its container.
     *  The calling method is expected to have read access on the workspace.
     *  This is a convenience method provided for subclasses.
     *  @param port A port with a location.
     *  @return The location of the port.
     *  @throws IllegalActionException If a valid location attribute cannot
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
        return location.getLocation();
    }
    
    /** Return the list of receivers that can receive from the specified
     *  port with the specified transmit properties. Ports that are contained
     *  by the same container as the specified <i>sourcePort</i> are
     *  not included.  Note that this method does
     *  not guarantee that those receivers will receive.  That is determined
     *  by the transmit() method, which subclasses may override to, for
     *  example, introduce probabilitic message losses.
     *  The calling method is expected to have read access on the workspace.
     *  @param sourcePort The sending port.
     *  @param properties The transmit properties (ignored in this base class).
     *  @return A list of objects implementing the Receiver interface.
     *  @exception IllegalActionException If a location of a port cannot be
     *   evaluated.
     */
    protected List _receiversInRange(
            WirelessIOPort sourcePort, Token properties)
            throws IllegalActionException {
        if (workspace().getVersion() == _receiversInRangeListVersion) {
            return _receiversInRangeList;
        }
        _receiversInRangeList = new LinkedList();
        Iterator ports = listeningInputPorts().iterator();
        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort)ports.next();
            
            // Skip ports contained by the same container as the source.
            if (port.getContainer() == sourcePort.getContainer()) continue;
            
            if (_isInRange(sourcePort, port, properties)) {
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        _receiversInRangeList.add(receivers[i][j]);
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
                        _receiversInRangeList.add(receivers[i][j]);
                    }
                }
            }
        }
        _receiversInRangeListVersion = workspace().getVersion();
        return _receiversInRangeList;
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached port list.
    private List _receiversInRangeList;
    private long _receiversInRangeListVersion = -1;

    // Name of the location attribute.
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";
}
