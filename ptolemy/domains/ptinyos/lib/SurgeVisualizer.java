/* An actor that displays link properties based on the interpretation
   of the token.

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.domains.ptinyos.lib;

import ptolemy.actor.Director;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.domains.ptinyos.kernel.PtinyOSDirector;
import ptolemy.domains.ptinyos.kernel.PtinyOSNodeParameter;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.domains.wireless.lib.LinkVisualizer;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SurgeVisualizer

/**
 This actor is used with the Surge TinyOS application to visualize
 links between nodes in a multihop network.  In Surge, there are two
 types of packets transmitted.  The first type is a beacon message sent
 from the base station.  The second type is a Surge message, which originates
 from a sensing node and contains sensor data.  The beginning of the packet
 format is as follows:

 bits  1-16: address field
 bits 17-24: type field

 The address field is set to 0xFFFF if it is a broadcast message.  Otherwise,
 it contains the destination node id.

 The type field is set to 0xFA if it is a beacon message (implemented in
 TinyOS as MultiHopMsg).  Beacon messages are sent with address field set
 to 0xFFFF.  The type field is set to 0x11 if it is a Surge message.

 FIXME: Surge doesn't correctly set the type field for Surge messages.
 The value that we see is 0x00, so we filter for this instead.

 This actor extends LinkVisualizer and by inheritance, implements the
 ChannelListener interface.  It inspects the token transmitted,
 and based on the data, may create a line between
 two communicating nodes that are within range of one
 another. It registers itself with the wireless channel
 specified by the <i>channelName</i> parameter.

 @author Heather Taylor, Elaine Cheong
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (celaine)
 @Pt.AcceptedRating Yellow (celaine)
 */
public class SurgeVisualizer extends LinkVisualizer {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SurgeVisualizer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Parse the token, and visualize a line between the sender and destination
     *  container only if the type field in the packet stored in
     *  <i>token</i> corresponds to a Surge packet AND
     *  the nodeID of the <i>destination</i> node
     *  (i.e., the node that contains the <i>destination</i> WirelessIOPort)
     *  is the same as the address field in the Surge packet.
     *
     *  The line is visualized by starting a thread that will create and
     *  remove the line after some amount of time.
     *  @param properties The properties of this transmission.
     *  @param token The token of this transmission, which can be processed here.
     *  @param sender The sending port.
     *  @param destination The receiving port.
     */
    public void channelNotify(RecordToken properties, Token token,
            WirelessIOPort sender, WirelessIOPort destination) {
        String tokenString = token.toString();
        String type = tokenString.substring(5, 7);

        // The "00" corresponds to Messages of type SurgeMsg (Should be 0x11).
        if (type.equals("00")) {
            // Look for the PtinyOSDirector inside the destination TinyOS node.
            NamedObj wirelessNode = destination.getContainer();
            if (wirelessNode instanceof CompositeEntity) {
                // Note: this relies on the fact that the MicaBoard
                // always contains a MicaCompositeActor
                ComponentEntity micaCompositeActor = ((CompositeEntity) wirelessNode)
                        .getEntity("MicaCompositeActor");
                if (micaCompositeActor instanceof MicaCompositeActor) {
                    // Get the director of the MicaCompositeActor.
                    Director director = ((MicaCompositeActor) micaCompositeActor)
                            .getDirector();
                    if (director instanceof PtinyOSDirector) {
                        Attribute nodeID = director.getAttribute("nodeID");
                        if (nodeID instanceof PtinyOSNodeParameter) {
                            // Get the token that stores the node ID.
                            try {
                                Token nodeIDToken = ((PtinyOSNodeParameter) nodeID)
                                        .getToken();

                                if (nodeIDToken instanceof IntToken) {
                                    // Get the integer value of nodeID
                                    int nodeIDValue = ((IntToken) nodeIDToken)
                                            .intValue();
                                    String addr = tokenString.substring(1, 5);
                                    int addrInt = Integer.parseInt(addr, 16);

                                    if (addrInt == nodeIDValue) {
                                        // Create a name for the line to be visualized.
                                        String lineName = getContainer()
                                                .uniqueName("_senderDestLine");
                                        // Create a thread to visualize the line.
                                        _LinkVisualizerThread linkVisualizerThread = new _LinkVisualizerThread(
                                                sender, destination, lineName);
                                        // Start the thread.
                                        linkVisualizerThread.start();
                                    }
                                }
                            } catch (IllegalActionException ex) {
                                // Do nothing.
                            }
                        }
                    }
                }
            }
        }
    }
}
