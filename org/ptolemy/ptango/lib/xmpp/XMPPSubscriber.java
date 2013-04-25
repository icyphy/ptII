/* An interface for actors that subscribe to an XMPP XEP-0060 node

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
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

///////////////////////////////////////////////////////////////////
//// XMPPSubscriber

/** An interface for actors that subscribe to an XMPP XEP-0060 node.
 *
 *  @see XMPPGateway
 *  @author Marten Lohstroh
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (marten)
 *  @Pt.AcceptedRating Red (marten)
 */
public interface XMPPSubscriber extends ItemEventListener<Item> {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the nodeId to subscribe to.
     *  @return A string representing the node to subscribe to.
     */
    public String getNodeId();

    /** Set the subscription id returned by
     *  SmackPubSubAccess.subscribeToNode(). // FIXME out-dated
     *  @param subId A string identifying this subscriber.
     */
    public void setSubId(String subId);

    public String getSubId();

    /** Register the gateway to the subscriber, so that if
     *  the subscription changes, it can notify the gateway.
     * @param gateway The attribute responsible for managing the XMPP session.
     */
    public void setGateway(XMPPGateway gateway);

}
