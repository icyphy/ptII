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
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;

//////////////////////////////////////////////////////////////////////////
//// ModelTopology
/**
Define the mothods that deal with the model topology here statically for the
convenience to build a set of actors to be used for a composite wireless channel.
 

@author Yang Zhao and Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
*/
public class ModelTopology {

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
    private ModelTopology() {}
     
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return a list of input ports that can potentially receive data
     *  from this channel.  This includes input ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  Note: This class doesn't try to deal with read access to the workspace,
     *  the caller should be.
     *  @param container The composite entity that contains the wireless channel.
     *  @param theChannelName The name of the wireless channel.
     *  @return A new list of input ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>outsideChannel</i> parameter cannot be evaluated.
     */
    public static List listeningInputPorts(CompositeEntity container, 
            String theChannelName) throws IllegalActionException {
        List result = new LinkedList();
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
                            if (channelName.equals(theChannelName)) {
                                result.add(port);
                            }
                        }
                    }
                }
            }
            return result;
        
    }

    /** Return a list of output ports that can potentially receive data
     *  from this channel.  This includes output ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @param container The composite entity that contains the wireless channel.
     *  @param theChannelName The name of the wireless channel.
     *  @return The list of output ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>insideChannel</i> parameter cannot be evaluated.
     */
    public static List listeningOutputPorts(CompositeEntity container, String theChannelName)
            throws IllegalActionException {
        List result = new LinkedList();
        Iterator ports = container.portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                if (port instanceof WirelessIOPort) {
                    WirelessIOPort castPort = (WirelessIOPort)port;
                    if (castPort.isOutput()) {
                        String channelName = castPort.insideChannel.stringValue();
                        if (channelName.equals(theChannelName)) {
                            result.add(port);
                        }
                    }
                }
            }
            return result;
    }
    
    /** Return a list of output ports that can potentially send data
     *  to this channel.  This includes output ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @param container The composite entity that contains the wireless channel.
     *  @param theChannelName The name of the wireless channel.
     *  @return A new list of input ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>outsideChannel</i> parameter cannot be evaluated.
     */
    public static List sendingOutputPorts(CompositeEntity container, 
            String theChannelName) throws IllegalActionException {
        List result = new LinkedList();
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
                            if (channelName.equals(theChannelName)) {
                                result.add(port);
                            }
                        }
                    }
                }
            }
            return result;
    }

    /** Return a list of input ports that can potentially send data
     *  to this channel.  This includes input ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @param container The composite entity that contains the wireless channel.
     *  @param theChannelName The name of the wireless channel.
     *  @return The list of output ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>insideChannel</i> parameter cannot be evaluated.
     */
    public static List sendingInputPorts(CompositeEntity container, 
            String theChannelName) throws IllegalActionException {
        List result = new LinkedList();
        Iterator ports = container.portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                if (port instanceof WirelessIOPort) {
                    WirelessIOPort castPort = (WirelessIOPort)port;
                    if (castPort.isInput()) {
                        String channelName = castPort.insideChannel.stringValue();
                        if (channelName.equals(theChannelName)) {
                            result.add(port);
                        }
                    }
                }
            }
            return result;
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
    public static double distanceBetween(
            WirelessIOPort port1, WirelessIOPort port2)
            throws IllegalActionException {
        double[] p1 = locationOf(port1);
        double[] p2 = locationOf(port2);
        return Math.sqrt((p1[0] - p2[0])*(p1[0] - p2[0])
                + (p1[1] - p2[1])*(p1[1] - p2[1]));
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
    public static double[] locationOf( 
            IOPort port) throws IllegalActionException {
        Entity portContainer = (Entity)port.getContainer();
        Locatable location = null;
       
        //FIXME: What should I do here...
        //if (portContainer == container) {
        //    location = (Locatable)port.getAttribute(LOCATION_ATTRIBUTE_NAME,
        //            Locatable.class);
        //} else {
            location = (Locatable)portContainer.getAttribute(
                    LOCATION_ATTRIBUTE_NAME, Locatable.class);
        //}
        if (location == null) {
            throw new IllegalActionException(
                    "Cannot determine location for port "
                    + port.getName()
                    + ".");
        }
        return location.getLocation();
    }
    

    

        
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Name of the location attribute.
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";
}
