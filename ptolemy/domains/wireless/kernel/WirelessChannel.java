/* Interface for channels in the wireless domain.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import java.util.List;

import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.domains.wireless.kernel.PropertyTransformer;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// WirelessChannel
/**
Interface for wireless channels.

@author Yang Zhao and Edward A. Lee
@version $Id$
@since Ptolemy II 3.1
*/
public interface WirelessChannel extends Nameable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a channel port that can be used to set type constraints
     *  between senders and receivers.
     */
    public ChannelPort getChannelPort();
    
    /** Return a list of input ports that can potentially receive data
     *  from this channel.  This must include input ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel.
     *  @return A new list of input ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>outsideChannel</i> parameter cannot be evaluated.
     */
    public List listeningInputPorts() throws IllegalActionException;

    /** Return a list of output ports that can potentially receive data
     *  from this channel.  This must include output ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return The list of output ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>insideChannel</i> parameter cannot be evaluated.
     */
    public List listeningOutputPorts() throws IllegalActionException;

    /** Register a PropertyTransformer for a wirelessIOPort.
     */
    public void registerPropertyTransformer(WirelessIOPort port, 
            PropertyTransformer transformer);
    /** Return a list of input ports that can potentially send data
     *  to this channel.  This must include input ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return The list of output ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>insideChannel</i> parameter cannot be evaluated.
     */
    public List sendingInputPorts() throws IllegalActionException;

    /** Return a list of output ports that can potentially send data
     *  to this channel.  This must include output ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return A new list of input ports of class WirelessIOPort
     *   using this channel.
     *  @exception IllegalActionException If a port is encountered
     *   whose <i>outsideChannel</i> parameter cannot be evaluated.
     */
    public List sendingOutputPorts() throws IllegalActionException;

    /** Transmit the specified token from the specified port with the
     *  specified properties.  All ports that are in range must receive
     *  the token if they have room in their receiver.
     *  @param token The token to transmit, or null to clear all
     *   receivers that are in range.
     *  @param port The port from which this is being transmitted.
     *  @param properties The transmit properties.
     *  @exception IllegalActionException If a location cannot be evaluated
     *   for a port, or if a type conflict occurs, or the director is not
     *   a WirelessDirector.
     */
    public void transmit(Token token, WirelessIOPort port,
            RecordToken properties)
            throws IllegalActionException;
}
