/* An actor that displays link properties based on the interpretation of the token.

 Copyright (c) 2006 The Regents of the University of California.
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
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.ptinyos.kernel.PtinyOSDirector;
import ptolemy.domains.ptinyos.kernel.PtinyOSIntegerParameter;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.domains.wireless.lib.LinkVisualizer;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.kernel.attributes.LineAttribute;

//////////////////////////////////////////////////////////////////////////
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

 This actor extends LinkVisualizer and implements the
 TokenProcessor interface.  It processes the token being
 transmitted and, based on the data, may create a line between
 two communicating nodes that are within range of one
 another. It registers itself with the wireless channel
 specified by the <i>channelName</i> parameter. The default
 channel is set to AtomicWirelessChannel.

 @author Heather Taylor
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (htaylor)
 @Pt.AcceptedRating Red (htaylor)
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

    /** This method parses the token and creates a line between the
     * sender and the destination containers, by creating a MoMLChangeRequest.
     * We only create a line if the type field in the packet stored in
     * <i>token</i> corresponds to a Surge packet AND
     * the nodeID of the <i>destination</i> node
     * (i.e., the node that contains the <i>destination</i> WirelessIOPort)
     * is the same as the address field in the Surge packet.
     *
     * @param properties The properties of this transmission.
     * @param token The token of this transmission, which is processed here.
     * @param sender The sending port.
     * @param destination The receiving port.
     * @exception IllegalActionException If failed to execute the model.
     */
    public void processTokens(RecordToken properties, Token token,
            WirelessIOPort sender, WirelessIOPort destination)
            throws IllegalActionException {
        synchronized (_isOff) {
            // If the line is not currently drawn, draw it.
            if (_isOff.booleanValue()) {
                String tokenString = (String) token.toString();
                String type = tokenString.substring(5, 7);

                // The "00" corresponds to Messages of type SurgeMsg (Should be 0x11).

                // Note: When processTokens() is called a second time
                // (see AtomicWirelessChannel#_transmitTo()) abd a line
                // was not created the first time, we recheck the type field
                // of the same packet (stored in "token").
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
                                Attribute nodeID = director
                                        .getAttribute("nodeID");
                                if (nodeID instanceof PtinyOSIntegerParameter) {
                                    // Get the token that stores the node ID.
                                    Token nodeIDToken = ((PtinyOSIntegerParameter) nodeID)
                                            .getToken();
                                    if (nodeIDToken instanceof IntToken) {
                                        // Get the integer value of nodeID
                                        int nodeIDValue = ((IntToken) nodeIDToken)
                                                .intValue();
                                        String addr = tokenString.substring(1,
                                                5);
                                        int addrInt = Integer
                                                .parseInt(addr, 16);

                                        if (addrInt == nodeIDValue) {
                                            Location senderLocation = (Location) sender
                                                    .getContainer()
                                                    .getAttribute("_location");
                                            Location destinationLocation = (Location) destination
                                                    .getContainer()
                                                    .getAttribute("_location");
                                            double x = (destinationLocation
                                                    .getLocation())[0]
                                                    - (senderLocation
                                                            .getLocation())[0];
                                            double y = (destinationLocation
                                                    .getLocation())[1]
                                                    - (senderLocation
                                                            .getLocation())[1];
                                            String moml = "<property name=\"_senderDestLine\" class=\"ptolemy.vergil.kernel.attributes.LineAttribute\">"
                                                    + senderLocation
                                                            .exportMoML()
                                                    + "<property name=\"x\" value=\""
                                                    + x
                                                    + "\"/>"
                                                    + "<property name=\"y\" value=\""
                                                    + y
                                                    + "\"/>"
                                                    + "</property>";
                                            ChangeRequest request = new MoMLChangeRequest(
                                                    this, getContainer(), moml) {
                                                protected void _execute()
                                                        throws Exception {
                                                    super._execute();
                                                    LineAttribute line = (LineAttribute) getContainer()
                                                            .getAttribute(
                                                                    "_senderDestLine");
                                                    line.moveToFirst();
                                                    line.setPersistent(false);
                                                }
                                            };
                                            requestChange(request);
                                            // Indicate that the line has been drawn.
                                            _isOff = Boolean.FALSE;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // If the line was drawn previously, remove it.
                if (getContainer().getAttribute("_senderDestLine") != null) {
                    String moml = "<deleteProperty name=\"_senderDestLine\"/>";
                    ChangeRequest request = new MoMLChangeRequest(this,
                            getContainer(), moml);
                    requestChange(request);
                    // Indicate that the line is not drawn.
                    _isOff = Boolean.TRUE;
                }
            }
        }
    }
}
