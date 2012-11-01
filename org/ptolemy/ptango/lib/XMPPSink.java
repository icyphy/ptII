/* An actor that publishes to an XMPP XEP-0060 node upon firing.

 Copyright (c) 1997-2012 The Regents of the University of California.
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

package org.ptolemy.ptango.lib;

import ptolemy.actor.lib.Sink;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// XMPPSink

/** An actor that publishes to an XMPP XEP-0060 node upon firing.
 *  @see XMPPGateway
 *  @author Marten Lohstroh
 *  @version $Id: XMPPSink.java 64744 2012-10-24 22:51:43Z marten $
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (marten)
 *  @Pt.AcceptedRating Red (marten)
 */
public class XMPPSink extends Sink implements XMPPPublisher {
    
    /**
     * 
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public XMPPSink(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        nodeId = new StringParameter(this, "NodeId");
        input.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public variables                      ////
    
    /** */
    public Parameter nodeId;
    
    ///////////////////////////////////////////////////////////////////
    ////                      public methods                       ////
    
    /**
     * 
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == nodeId) {
            _nodeId = ((StringToken) nodeId.getToken()).stringValue();
        }
    }

    
    /**
     * 
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        int width = input.getWidth();

        for (int i = 0; i < width; i++) {
            String value = _getInputString(i);
            if (value != null) {
                _gateway.publish(_nodeId, value);        
            }
        }
    }

    /**
     * 
     */
    @Override
    public void setGateway(XMPPGateway gateway) {
        _gateway = gateway;
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////
    
    /** Return a string describing the input on channel i.
     *  This is a protected method to allow subclasses to override
     *  how inputs are observed.
     *  @param i The channel
     *  @return A string representation of the input, or null
     *   if there is nothing to display.
     *  @exception IllegalActionException If reading the input fails.
     */
    protected String _getInputString(int i) throws IllegalActionException {
        if (input.hasToken(i)) {
            Token token = input.get(i);
            String value = token.toString();
            if (token instanceof StringToken) {
                value = ((StringToken) token).stringValue();
            }
            return value;
        }
        return null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    private variables                      ////
    
    
    /** */
    private XMPPGateway _gateway;
    
    /** */
    private String _nodeId;

}
