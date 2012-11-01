/* An actor that subscribe to an XMPP XEP-0060 node and fires upon an event.

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

import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;

import ptolemy.actor.lib.Source;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// XMPPSource

/** An actor that subscribe to an XMPP XEP-0060 node and fires upon an event.
 *  FIXME: comments
 *  @see XMPPGateway
 *  @author Marten Lohstroh
 *  @version $Id: XMPPReader.java 64744 2012-10-24 22:51:43Z marten $
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (marten)
 *  @Pt.AcceptedRating Red (marten)
 */
public class XMPPSource extends Source implements XMPPSubscriber {
    
    
    public XMPPSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        nodeId = new StringParameter(this, "NodeId");
        output.setTypeEquals(BaseType.STRING);
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
        output.send(0, new StringToken(_currentValue));
    }
    
    @Override
    public void handlePublishedItems(ItemPublishEvent<Item> items) {
        _currentValue = items.getItems().toString();
        System.out.println("Event!");
        try {
            getDirector().fireAtCurrentTime(this);
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String getNodeId() {
        return _nodeId;
    }

    @Override
    public void setSubId(String subId) {
        _subscriptionId = subId;
    }

    @Override
    public void setGateway(XMPPGateway gateway) {
        _gateway = gateway;
        
    }
    
    private String _currentValue;
    
    private String _subscriptionId;
    
    private XMPPGateway _gateway;

    /** */
    private String _nodeId;

    
    @Override
    public String getSubId() {
        // TODO Auto-generated method stub
        return null;
    }
}
