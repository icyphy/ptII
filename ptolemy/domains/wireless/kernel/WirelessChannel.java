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

@ProposedRating Green (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// WirelessChannel
/**
Interface for wireless channels in the wireless domain. 
Wireless channels are special components in a wireless model. 
They are used to determine the conectivity and deliver
tokens from the transmiter to receiver(s). This interface 
defines a set of methods for the wireless channel components.
<p>
The transmiter can call the channel's transmit() method to 
deliver data to its correspoding receivers. It can specify
the transmission properties by a record token including the
transimission range, transmission power. The transmit 
properties may be modified by the channel model or by some
property transformers(@see PropertyTransformer) during the 
transmission to take into account channel losses, antenna 
gain, noise, etc.

@author Yang Zhao and Edward A. Lee
@version $Id$
@since Ptolemy II 3.1
*/
public interface WirelessChannel extends Actor, Nameable, PropertyTransformer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a channel port that can be used to set type constraints
     *  between senders and receivers. An channel contains a single port,
     *  which is an instance of ChannelPort. The port is merely used to
     *  set up n type constrains instead of n*n, where n is the number of
     *  ports using the channel.
     *  @return The channel port.
     */
    public ChannelPort getChannelPort();

    /** Return a list of input ports that can potentially receive data
     *  from this channel.  This must include input ports contained by
     *  entities contained by the container of this channel that
     *  have their <i>outsideChannel</i> parameter set to the name
     *  of this channel. Transparent hierarchy is not supported. 
     *  @return The list of input ports of class WirelessIOPort
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
    
    /** Register a property transformer for transmissions from the
     *  specified port. A PropertyTransformer modifies the transmission
     *  property and returns the modified property. If null is
     *  given for the port, then the property transformer will
     *  be used for all transmissions through this channel.
     *  If multiple property transformers are registered that can operate
     *  on a given transmission, then the order in which they are applied
     *  is arbitrary.  Thus, property transformers should implement
     *  commutative operations on the properties (such as multiplying
     *  a field by a value).
     *  If the property transformer is already registered with a particular
     *  port, then an implementer of this method must not register it again with
     *  that port.  Similarly, if a property transformer is registered with
     *  no port, then an implementer of this method must not register
     *  it again with no port.
     *  @param transformer The property transformer to be registered.
     *  @param port The port whose transmissions should be subject to the
     *   property transformer, or null to make them subject to all
     *   transmissions through this channel.
     */
    public void registerPropertyTransformer(
            PropertyTransformer transformer, WirelessIOPort port);

    /** Return a list of input ports that can potentially send data
     *  to this channel.  This must include input ports contained by
     *  the container of this channel that
     *  have their <i>insideChannel</i> parameter set to the name
     *  of this channel. This method gets read access on the workspace.
     *  @return The list of input ports of class WirelessIOPort
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
     *  @return The list of output ports of class WirelessIOPort
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
     *  @param properties The transmission properties.
     *  @exception IllegalActionException If something goes wrong during
     *   the transmission.
     */
    public void transmit(Token token, WirelessIOPort port,
            RecordToken properties)
            throws IllegalActionException;
            
    /** Unregister a property transformer for transmissions from the specified
     *  port (or from null for a generic property transformer). If the transformer
     *  has not been registered, then do nothing.
     *  @param transformer The property transformer to be unregistered.
     *  @param port The port whose transmissions should be subject to the
     *   property transformer, or null for a generic transformer.
     *  @see #registerPropertyTransformer(PropertyTransformer, WirelessIOPort)
     */
    public void unregisterPropertyTransformer(
            PropertyTransformer transformer, WirelessIOPort port);
}
