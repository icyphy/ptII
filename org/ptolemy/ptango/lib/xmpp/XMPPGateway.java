/* An attribute that provides connectivity through an XMPP server.

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

package org.ptolemy.ptango.lib.xmpp;

import java.util.Iterator;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.Subscription.State;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.Executable;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// XMPPGateway

/** FIXME: comments
 * 
 *  @see XMPPGateway
 *  @author Marten Lohstroh
 *  @version $Id: XMPPGateway.java 64744 2012-10-24 22:51:43Z marten $
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (marten)
 *  @Pt.AcceptedRating Red (marten)
 */
public class XMPPGateway extends AbstractInitializableAttribute implements
        Executable {

    /** Construct an instance of the attribute.
     * @param container The container.
     * @param name The name.
     * @exception IllegalActionException If the superclass throws it.
     * @exception NameDuplicationException If the superclass throws it.
     */
    public XMPPGateway(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        server = new StringParameter(this, "server");
        server.setExpression("localhost");

        port = new Parameter(this, "port");
        port.setTypeEquals(BaseType.INT);
        port.setExpression("5222");

        username = new StringParameter(this, "username");
        username.setExpression("ptolemy"); // FIXME: set default to guest

        password = new StringParameter(this, "password");
        password.setExpression("tUkM6Prj"); // FIXME: set default to guest

    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The port number to connect to. This is a integer that
     *  defaults to 5222.
     */
    public Parameter port;

    /** The server to connect to. This is a string that
     *  defaults to "localhost".
     */
    public Parameter server;

    /** The username to authenticate with. This is a string that
     *  defaults to "guest".
     */
    public Parameter username;

    /** The password to authenticate with. This is a string that
     *  defaults to "guest".
     */
    public Parameter password;

    /** 
     * FIXME: reconnect to server upon changes (how to know what is the last change?)
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == port) {
            _portNumber = ((IntToken) port.getToken()).intValue();
            _disconnect();

        } else if (attribute == server) {
            _serverName = ((StringToken) server.getToken()).stringValue();
            _disconnect();
        } else if (attribute == username) {
            _userName = ((StringToken) username.getToken()).stringValue();
            _disconnect();
        } else {
            super.attributeChanged(attribute);
        }
    }

    public void initialize() throws IllegalActionException {
        // FIXME
        String jid = _userName + '@' + _serverName; //"ptolemy@dhcp-45-24.eecs.berkeley.edu";//127.0.0.1"; //_userName + '@' + _serverName + "/ptolemy";
        //System.out.println(_connection.getHost());

        System.setProperty("smack.debugEnabled", "true");
        XMPPConnection.DEBUG_ENABLED = true;

        _connectAndLogin();
        
        _manager = new PubSubManager(_connection);
        // discover XMPPSubscribers FIXME: how deep is this search?
        Iterator<?> objects = toplevel().containedObjectsIterator();
        while (objects.hasNext()) {
            Object object = objects.next();
            // XMPPSubscriber found
            if (object instanceof XMPPSubscriber) {
                LeafNode node;
                XMPPSubscriber subscriber = (XMPPSubscriber) object;
                String nodeId = subscriber.getNodeId();
                try {
                    //_manager.deleteNode(nodeId);
                    node = (LeafNode) _manager.getNode(nodeId); // FIXME: cast problem here
                    // FIXME: get rid of duplicate subscriptions here
                } catch (Exception e) {
                    try {
                        /* ConfigureForm form = new ConfigureForm(FormType.submit); // FIXME: figure out configuration options
                         form.setAccessModel(AccessModel.open);
                         form.setDeliverPayloads(false);
                         form.setNotifyRetract(true);
                         form.setPersistentItems(true);
                         form.setPublishModel(PublishModel.open); */
                        node = _manager.createNode(nodeId);

                    } catch (XMPPException e1) {
                        throw new IllegalActionException(
                                "Unable find or create node: " + nodeId + ".");
                    }
                }
                if (node != null) {
                    // register listener
                    node.addItemEventListener(subscriber);
                    try {
                        Subscription subscription = null;
                        for (Subscription s : node.getSubscriptions()) {
                            if (s.getJid().equals(jid)) {
                                // check if state is OK
                                if (s.getState().equals(State.subscribed)) {
                                    subscription = s;
                                    break;
                                } else {
                                    node.unsubscribe(jid, s.getId());
                                }
                            }
                        }
                        // subscribe to node if not exists
                        if (subscription == null) {
                            subscription = node.subscribe(jid); // FIXME: subscription fails here
                        }
                        // FIXME: check subscription.state here too
                    } catch (XMPPException e) {
                        throw new IllegalActionException(
                                "Unable subscribe to node: " + nodeId + " ("
                                        + e.getMessage() + ").");
                    }
                } else {
                    throw new IllegalActionException(
                            "Unable find or create node: " + nodeId + ".");
                }
            } else if (object instanceof XMPPPublisher) {
                ((XMPPPublisher)object).setGateway(this);
            }
        }

    }

    public void wrapup() throws IllegalActionException {
        _disconnect();
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    private int _portNumber = 5222;

    private String _userName = "ptolemy";

    private String _password = "tUkM6Prj";

    private String _serverName = "localhost";

    private PubSubManager _manager;

    private Connection _connection;

    @Override
    public void fire() throws IllegalActionException {
        return;
    }

    @Override
    public boolean isFireFunctional() {
        return false;
    }

    @Override
    public boolean isStrict() throws IllegalActionException {
        return true;
    }

    @Override
    public int iterate(int count) throws IllegalActionException {
        return Executable.COMPLETED;
    }

    @Override
    public boolean postfire() throws IllegalActionException {
        return true;
    }

    private void _disconnect() throws IllegalActionException {
        if (_connection != null) {
            _connection.disconnect();
        }
        _connection = null;
    }
    
    private void _connectAndLogin() throws IllegalActionException {
        
        // already connected
        if (_connection != null && _connection.isConnected() && _connection.isAuthenticated()) {
            return;
        }
        
        // no active connection, establish one
        if (_connection == null) {
            ConnectionConfiguration config = new ConnectionConfiguration(
                    _serverName, _portNumber);
            config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
            config.setCompressionEnabled(true);
            config.setSASLAuthenticationEnabled(true);
            _connection = new XMPPConnection(config);
        }
        try {
            // connect to the server
            if (!_connection.isConnected()) {
                _connection.connect();
            }
        } catch (Exception e) {
            throw new IllegalActionException(
                    "Unable to connect to XMPP server.");
        }

        try {
            // login to the server
            if (!_connection.isAuthenticated()) {
                _connection.login(_userName, _password, "ptolemy");
            }
        } catch (Exception e) {
            throw new IllegalActionException("Unable to login to XMPP server.");
        }
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        if (_connection == null) {
            initialize();
        }
        else if (!_connection.isConnected() || !_connection.isAuthenticated()) {
            _connectAndLogin();
        }
        return true;
    }

    @Override
    public void stop() {
        return;

    }

    @Override
    public void stopFire() {
        return;
    }

    @Override
    public void terminate() {
        return;
    }

    public void publish(String nodeId, String value) throws IllegalActionException {
        
        Node n;
        LeafNode ln;
        
        try {
            if ((n = _manager.getNode(nodeId)) instanceof LeafNode) {
                ln = (LeafNode) n;
            } else {
                throw new IllegalActionException("Unable to publish a node that is not a leaf.");
            }
        } catch (XMPPException e) {
            try {
                ln = _manager.createNode(nodeId);
            } catch (XMPPException e1) {
                throw new IllegalActionException("Unable to create node with id: " + nodeId + ".");
            }
        }

        SimplePayload payload = new SimplePayload("message","null", "<message>" + value + "</message>");
        PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(null, payload);
        ln.publish(item);
    }
    
    public void removeNode(String nodeId) throws IllegalActionException {
        try {
            _manager.deleteNode(nodeId);
        } catch (XMPPException e) {
            throw new IllegalActionException("Unable to remove node with id: " + nodeId + ".");
        }
    }

}
