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
import ptolemy.actor.TypedIORelation;
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
This is the base class of the communication channels in the sensor domain.

@author Xiaojun Liu and Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
*/
public class WirelessChannel extends TypedIORelation {

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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Transmit the specified token from the specified port at the specified
     *  power.
     *  @param token The token to transmit.
     *  @param port The port from which this is being transmitted.
     *  @param power The transmit power.
     *  @exception IllegalActionException If a location cannot be evaluated
     *   for a port, or if a type conflict occurs.
     */
    public void transmit(Token token, WirelessIOPort port, double power)
            throws IllegalActionException {
        try {
            workspace().getReadAccess();
            Iterator receivers = _receiversInRange(port, power).iterator();
            while (receivers.hasNext()) {
                Receiver receiver = (Receiver)receivers.next();
                // FIXME: Do the delay here in a subclass.
                receiver.put(token);
            }
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the distance between two ports.
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
     *  specified source port, assuming the source port transmits at
     *  the specified power.  In this base class, this method returns
     *  true always.  The method can assume that the two ports are
     *  communicating on the same channel.  This should be checked by
     *  the calling method.
     *  @param source The source port.
     *  @param destination The destination port.
     *  @param power The transmit power.
     *  @return True if the destination is in range of the source.
     *  @throws IllegalActionException If it cannot be determined
     *   whether the destination is in range (not thrown in this base
     *   class).
     */
    protected boolean _isInRange(
            WirelessIOPort source, WirelessIOPort destination, double power)
            throws IllegalActionException {
        return true;
    }

    /** Return the location of the given port. If the container of the
     *  port is the container of this channel, then use the
     *  "_location" attribute of the port.  Otherwise, use the
     *  "_location" attribute of its container.
     *  The calling method is expected to have read access on the workspace.
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
     *  port at the specified transmit power. Ports that are contained
     *  by the same container as the specified <i>sourcePort</i> are
     *  not included.  Note that this method does
     *  not guarantee that those receivers will receive.  That is determined
     *  by the transmit() method, which subclasses may override to, for
     *  example, introduce probabilitic message losses.
     *  The calling method is expected to have read access on the workspace.
     *  @param sourcePort The sending port.
     *  @param power The transmit power.
     *  @return A list of instances of DEReceiver.
     *  @exception IllegalActionException If a location of a port cannot be
     *   evaluated.
     */
    protected List _receiversInRange(
            WirelessIOPort sourcePort, double power)
            throws IllegalActionException {
        // FIXME: Cache result.
        LinkedList result = new LinkedList();
        Iterator ports = _listeningInputPorts().iterator();
        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort)ports.next();
            
            // Skip ports contained by the same container as the source.
            if (port.getContainer() == sourcePort.getContainer()) continue;
            
            if (_isInRange(sourcePort, port, power)) {
                Receiver[][] receivers = port.getReceivers();
                // FIXME: Suspicious... what if there are more receivers?
                result.add(receivers[0][0]);
            }
        }
        ports = _listeningOutputPorts().iterator();
        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort)ports.next();
                        
            if (_isInRange(sourcePort, port, power)) {
                Receiver[][] receivers = port.getInsideReceivers();
                // FIXME: Suspicious... what if there are more receivers?
                result.add(receivers[0][0]);
            }
        }
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    /** Return the list of input ports that can potentially receive data
     *  from this channel.  This includes input ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel.
     *  The calling method is expected to have read access on the workspace.
     *  @return The list of input ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>outsideChannel</i> parameter cannot be evaluated.
     */
    private List _listeningInputPorts() throws IllegalActionException {
        if (workspace().getVersion() == _listeningInputPortListVersion) {
            return _listeningInputPortList;
        }
        _listeningInputPortList = new LinkedList();
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
                            _listeningInputPortList.add(port);
                        }
                    }
                }
            }
        }
        _listeningInputPortListVersion = workspace().getVersion();
        return _listeningInputPortList;
    }
    
    /** Return the list of output ports that can potentially receive data
     *  from this channel.  This includes output ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel.
     *  The calling method is expected to have read access on the workspace.
     *  @return The list of output ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>insideChannel</i> parameter cannot be evaluated.
     */
    private List _listeningOutputPorts() throws IllegalActionException {
        if (workspace().getVersion() == _listeningOutputPortListVersion) {
            return _listeningOutputPortList;
        }
        _listeningOutputPortList = new LinkedList();
        CompositeEntity container = (CompositeEntity)getContainer();
        Iterator ports = container.portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port)ports.next();
            if (port instanceof WirelessIOPort) {
                WirelessIOPort castPort = (WirelessIOPort)port;
                if (castPort.isOutput()) {
                    String channelName = castPort.insideChannel.stringValue();
                    if (channelName.equals(getName())) {
                        _listeningOutputPortList.add(port);
                    }
                }
            }
        }
        _listeningOutputPortListVersion = workspace().getVersion();
        return _listeningOutputPortList;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached listening input port list.
    private List _listeningInputPortList;
    private long _listeningInputPortListVersion = -1;

    // Cached listening output port list.
    private List _listeningOutputPortList;
    private long _listeningOutputPortListVersion = -1;

    public static final Receiver[][] EMPTY_RECEIVERS = new Receiver[0][0];
    public static final String LOCATION_ATTRIBUTE_NAME = "_location";

}
