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
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// WirelessToWired

/**
On each firing, this actor reads at most one token from the input
port and output the data on the <i>data</i> port and the properties
on the <i>properties</i> port.  If there are no properties, then
output a token only on the <i>data</i> port.

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
        
        // Create and configure the parameters.
        inputChannelName = new StringParameter(this, "inputChannelName");
        inputChannelName.setExpression("WirelessChannel");

        // Create and configure the ports.       
        input = new WirelessIOPort(this, "input", true, false);
        input.outsideChannel.setExpression("$inputChannelName");

        data = new TypedIOPort(this, "data", false, true);
        data.setTypeSameAs(input);
        new Attribute(data, "_showName");

        properties = new TypedIOPort(this, "properties", false, true);
        new Attribute(properties, "_showName");
        // FIXME: How to set the type of the properties?
        // Fix it in the director?
        
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
     *  "WirelessChannel".
     */
    public StringParameter inputChannelName;

    /** Port that transmits the properties received on the <i>input</i>
     *  port.
     */
    public TypedIOPort properties;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 
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
                     
            Token propertiesValue = input.getProperties(0);
            if (propertiesValue != null) {
                properties.send(0, propertiesValue);
            }
        }
    }
}
