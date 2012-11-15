/* An actor that subscribes to an XMPP XEP-0060 node and fires upon an event.

 Copyright (c) 2012 The Regents of the University of California.
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

package org.ptolemy.ptango.lib.xmpp;

import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;

import ptolemy.actor.lib.Source;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// XMPPSource

/** An actor that subscribes to an XMPP XEP-0060 node and fires upon an event.
 *  An XMPP node is created by a user, where data can be published and subscribed.
 *  An XMPP node has a <i>nodeId</i>, which is set at creation time. 
 *  This actor refers XMPP node via its <i>nodeId</i>.   
 *  FIXME: comments, explain XEP-0060
 *  @see XMPPGateway
 *  @author Marten Lohstroh
 *  @version $Id: XMPPReader.java 64744 2012-10-24 22:51:43Z marten $
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (marten)
 *  @Pt.AcceptedRating Red (marten)
 */
public class XMPPSource extends Source implements XMPPSubscriber {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public XMPPSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        nodeId = new StringParameter(this, "nodeId");
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public variables                      ////
    
    /** XMPPSource subscribes to an XMPP node, the name of which is 
     *  specified by the value of nodeId.
     *  The nodeId is set when the XMPP node is created by a user.
     *  The default value is empty string.
     */
    public StringParameter nodeId;    
    
    
    ///////////////////////////////////////////////////////////////////
    ////                      public methods                       ////
    
    /** If the argument is the <i>nodeId</i> parameter, then reset the
     *  state to the specified value.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If <i>nodeId<i> cannot be evaluated
     *   or cannot be converted to the output type, or if the superclass
     *   throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == nodeId) {
            _nodeId = ((StringToken) nodeId.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Send the current value of the state of this actor to the output.
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, new StringToken(_currentValue));
    }

    /** Return the nodeId. 
     *  @return the nodeId.
     */
    @Override
    public String getNodeId() {
        return _nodeId;
    }

    /** Return the id of this subscriber.
     * @return the id of this subscriber.
     * @see #setSubId(String)
     */
    @Override
    public String getSubId() {
        return _subscriptionId;
    }
    
    /** Parse the published item from the XMPP node, save the XML and 
     *  call fireAtCurrentTime().
     *  This method is declared in the smack ItemEventListener interface.
     *  This method is called when an item is published.
     */
    @Override
    public void handlePublishedItems(ItemPublishEvent<Item> items) {
        // FIXME: unchecked cast
        PayloadItem<SimplePayload> item = (PayloadItem<SimplePayload>) items.getItems().get(0);
        SimplePayload payload = item.getPayload();
        _currentValue = payload.toXML();
        System.out.println("Event!");
        try {
            getDirector().fireAtCurrentTime(this);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(this, e, "Failed to fire at the current time.");
        }
    }

    /** Register the gateway to the subscriber, so that if
     *  the subscription changes, it can notify the gateway.
     *  @param gateway The attribute responsible for managing the XMPP session.
     */
    @Override
    public void setGateway(XMPPGateway gateway) {
        _gateway = gateway;
        
    }

    /** Set the id of the subscriber. 
     *  @param subId A string identifying this subscriber. 
     *  @see #getSubId()
     */
    @Override
    public void setSubId(String subId) {
        // XEP 0060 uses the term subId, not subscriberId.
        _subscriptionId = subId;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The XML payload from XMPP node. */
    private String _currentValue;

    /** The gateway that handles the connection between this actor and 
     *  the XMPP server. 
     */
    private XMPPGateway _gateway;

    /** The cached value of nodeId parameter.
     *  @see #nodeId
     */
    private String _nodeId;

    /** The id of the subscriber. */
    private String _subscriptionId;   
}
