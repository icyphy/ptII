/* An actor with a wireless input port and wired output ports.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.AtomicWirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// WirelessToWired

/**
On each firing, this actor reads at most one token from the input
port and output the data on the <i>data</i> port and the properties
on the <i>properties</i> port.  If there are no properties, then
output a token only on the <i>data</i> port.
<p>
NOTE: The type of the properties port is inferred from the
<i>defaultProperties</i> field of the channel at preinitialize()
time. If the channel is changed during execution, or this
field is changed, then the type of the port will not be updated,
and a run-time type error could occur.
Thus, this actor assumes that these types do not change.
If the channel has no default properties (as in the base class
AtomicWirelessChannel), then the type of the properties port will
be undefined. If it is left disconnected, then this is fine,
but if it is connected, then its type will need to be declared
explicitly.

@author Edward A. Lee
@version $Id$
*/
public class WirelessToWired extends TypedAtomicActor {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public WirelessToWired(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        properties = new TypedIOPort(this, "properties", false, true);
        new Attribute(properties, "_showName");

        // Create and configure the parameters.
        inputChannelName = new StringParameter(this, "inputChannelName");
        inputChannelName.setExpression("AtomicWirelessChannel");

        // Create and configure the ports.       
        input = new WirelessIOPort(this, "input", true, false);
        input.outsideChannel.setExpression("$inputChannelName");

        data = new TypedIOPort(this, "data", false, true);
        data.setTypeSameAs(input);
        new Attribute(data, "_showName");
        
        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Port that transmits the data received on the <i>input</i>
     *  port.
     */
    public TypedIOPort data;

    /** Port that receives a wireless input.
     */
    public WirelessIOPort input;

    /** Name of the input channel. This is a string that defaults to
     *  "AtomicWirelessChannel".
     */
    public StringParameter inputChannelName;

    /** Port that transmits the properties received on the <i>input</i>
     *  port.
     */
    public TypedIOPort properties;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 
    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        WirelessToWired newObject = (WirelessToWired)(super.clone(workspace));

        // set the type constraints
        newObject.data.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Read at most one token from the input port and output the data
     *  on the <i>data</i> port and the properties on the <i>properties</i>
     *  port.  If there are no properties, then output a token only
     *  on the <i>data</i> port.
     */
    public void fire() throws IllegalActionException {

        super.fire();
      
        if (input.hasToken(0)) {
            Token inputValue = input.get(0);
            if (_debugging) {
                _debug("Input signal received: " + inputValue.toString());
            }
            data.send(0, inputValue);
            
            // Do not send properties if the port has no destinations.
            // This prevents run-time type errors from occurring.
            if (properties.numberOfSinks() == 0) {
                return;
            }
            Token propertiesValue = input.getProperties(0);
            if (propertiesValue != null) {
                properties.send(0, propertiesValue);
            }
        }
    }

    /** Create receivers and set up the type constraints on the
     *  <i>properties</i> port.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Find the channel.
        CompositeEntity container = (CompositeEntity)getContainer();
        if (container != null) {
            Entity channel = container.getEntity(
                    inputChannelName.stringValue());
            if (channel instanceof AtomicWirelessChannel) {
                Parameter channelProperties = ((AtomicWirelessChannel)channel)
                        .defaultProperties;
                // Only set up the type constraint if the type of the
                // of the properties field is known.
                if (channelProperties.getType() != BaseType.UNKNOWN) {
                    properties.setTypeSameAs(channelProperties);
                }
            }
        }
    }
}
