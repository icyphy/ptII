/* An attribute that provides connectivity through an XMPP server.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JFrame;

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
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// XMPPGateway

/**
 *  This attribute sets up and maintains a connection to a XMPP server. It
 *  keeps track of actors within the model that implement the XMPPSubscriber
 *  or XMPPPublisher interface and relays messages between those actors
 *  and the XMPP server.
 *
 *  The Executable interface is implemented merely to make sure that the
 *  connection maintained to the server is alive every iteration, before
 *  any firings occur in which actors may attempt to publish something
 *  by invoking the <code>XMPP.publish()</code> method.
 *
 *  This implementation makes use of the
 *  <a href="http://www.igniterealtime.org/projects/smack/">Smack</a>
 *  library.
 *
 *  @see XMPPSource
 *  @see XMPPSink
 *  @see Executable
 *  @author Marten Lohstroh and Ben Zhang
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (marten)
 *  @Pt.AcceptedRating Red (marten)
 */
public class XMPPGateway extends AbstractInitializableAttribute implements
Executable {

    /** Construct an instance of the XMPPGateway attribute.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public XMPPGateway(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        server = new StringParameter(this, "server");
        server.setExpression("localhost");

        port = new Parameter(this, "port");
        port.setTypeEquals(BaseType.INT);
        port.setExpression("5222");

        userName = new StringParameter(this, "userName");
        userName.setExpression("ptolemy");

        passwordFile = new FileParameter(this, "passwordFile");
        passwordFile.setExpression("");

        // Set the Debug Flag.
        debugEnable = new Parameter(this, "debugEnable", BooleanToken.FALSE);
        debugEnable.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The file that contains the password.  If this parameter is
     *  non-empty, then it is assumed to refer to a file that contains
     *  the password.  If this parameter is empty, or names a file
     *  that cannot be read, then a dialog is displayed for the user
     *  to enter the password.  It is up to the user to properly
     *  protect the file from unauthorized readers by using the file
     *  system permissions.  The default value is the empty string,
     *  meaning that the dialog will be displayed.
     */
    public FileParameter passwordFile;

    /** Port number to connect to. This is a integer that
     *  defaults to 5222.
     */
    public Parameter port;

    /** Server to connect to. This is a string that
     *  defaults to "localhost".
     */
    public Parameter server;

    /** User name to authenticate with. This is a string that
     *  defaults to "guest".
     */
    public Parameter userName;

    /** The flag indicates whether or not to enable the smack library debug
     *  If this is true, the raw XML stream will be shown for debug use.
     *  defaults to BooleanToken false
     */
    public Parameter debugEnable;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the local variable associated with the changed attribute
     *  and disconnect from the server.
     *  @param attribute The changed attribute.
     *  @exception IllegalActionException If thrown while getting the
     *  value of the port, server or userName token, or if thrown
     *  by the method in the super class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == port) {
            _portNumber = ((IntToken) port.getToken()).intValue();
            _disconnect();

        } else if (attribute == server) {
            _serverName = ((StringToken) server.getToken()).stringValue();
            _disconnect();
        } else if (attribute == userName) {
            _userName = ((StringToken) userName.getToken()).stringValue();
            _disconnect();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return immediately. */
    @Override
    public void fire() throws IllegalActionException {
        _debug("Called fire()");
        return;
    }

    /** Attempt to connect to the server and login. Discover subscribers
     *  and them as listeners. If a subscriber wants to subscribe to a
     *  non-existent node, create it.
     *  discover publishers and give them a reference to this attribute.
     *  This might change in the future, as changes to running models require
     *  actors to register themselves instead of being discovered.
     *  @exception IllegalActionException If unable to login, create a node,
     *  find a node, or subscribe to a node.
     *
     */
    @Override
    public void initialize() throws IllegalActionException {
        // Invoke any registered initializables in the super class.
        super.initialize();

        // FIXME: if the server runs on localhost, it doesn't accept 'localhost',
        // but requires 127.0.0.1, look into this
        String jid = _userName + '@' + _serverName;

        boolean debugEnableValue = ((BooleanToken) debugEnable.getToken())
                .booleanValue();

        System.setProperty("smack.debugEnabled",
                String.valueOf(debugEnableValue));
        Connection.DEBUG_ENABLED = debugEnableValue;

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
                        /* ConfigureForm form = new ConfigureForm(FormType.submit);
                         // FIXME: figure out configuration options
                         form.setAccessModel(AccessModel.open);
                         form.setDeliverPayloads(false);
                         form.setNotifyRetract(true);
                         form.setPersistentItems(true);
                         form.setPublishModel(PublishModel.open); */
                        node = _manager.createNode(nodeId);

                    } catch (XMPPException ex) {
                        throw new IllegalActionException(this, ex,
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
                    } catch (XMPPException ex) {
                        throw new IllegalActionException(this, ex,
                                "Unable subscribe to node: " + nodeId + ".");
                    }
                } else {
                    throw new IllegalActionException(this,
                            "Unable find or create node: " + nodeId + ".");
                }
            } else if (object instanceof XMPPPublisher) {
                ((XMPPPublisher) object).setGateway(this);
            }
        }

    }

    /** Return false. */
    @Override
    public boolean isFireFunctional() {
        return false;
    }

    /** Return true. */
    @Override
    public boolean isStrict() throws IllegalActionException {
        return true;
    }

    /** Return immediately. */
    @Override
    public int iterate(int count) throws IllegalActionException {
        return Executable.COMPLETED;
    }

    /** Check the connection, reconnect and login if required. */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_connection == null) {
            initialize();
        } else if (!_connection.isConnected() || !_connection.isAuthenticated()) {
            _connectAndLogin();
        }
        return true;
    }

    /** Return immediately. */
    @Override
    public boolean postfire() throws IllegalActionException {
        return true;
    }

    /** Publish a value to a node. The value is wrapped into a message stanza.
     *  @param nodeId The node to publish to.
     *  @param value The value to publish.
     *  @exception IllegalActionException If publishing failed.
     */
    public void publish(String nodeId, String value)
            throws IllegalActionException {

        Node n;
        LeafNode ln;

        try {
            if ((n = _manager.getNode(nodeId)) instanceof LeafNode) {
                ln = (LeafNode) n;
            } else {
                throw new IllegalActionException(this,
                        "Unable to publish a node that is not a leaf.");
            }
        } catch (XMPPException e) {
            try {
                ln = _manager.createNode(nodeId);
            } catch (XMPPException e1) {
                throw new IllegalActionException(this,
                        "Unable to create node with id: " + nodeId + ".");
            }
        }

        SimplePayload payload = new SimplePayload("message", "null",
                "<message>" + value + "</message>");
        PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(null,
                payload);
        ln.publish(item);
    }

    /** Remove a node from the server configuration.
     *  @param nodeId The node to remove.
     *  @exception IllegalActionException If unable to remove the node.
     */
    public void removeNode(String nodeId) throws IllegalActionException {
        try {
            _manager.deleteNode(nodeId);
        } catch (XMPPException e) {
            throw new IllegalActionException(this,
                    "Unable to remove node with id: " + nodeId + ".");
        }
    }

    /** Return immediately. */
    @Override
    public void stop() {
        return;
    }

    /** Return immediately. */
    @Override
    public void stopFire() {
        return;
    }

    /** Return immediately. */
    @Override
    public void terminate() {
        return;
    }

    /** Disconnect from the server.
     *  @exception IllegalActionException If thrown in the base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // Invoke any registered initializables in the super class.
        super.wrapup();
        _disconnect();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Disconnect and leave old connection for the garbage collector.
     *  Note that once disconnected, a Connection cannot be
     *  reused immediately, therefore it is discarded. A new Connection
     *  object shall be instantiated for reconnecting.
     *  @see Connection
     *  @see #_connectAndLogin()
     */
    private void _disconnect() {
        if (_connection != null) {
            _connection.disconnect();
        }
        _connection = null;
    }

    /** Connect to the server and login using the provided credentials.
     *  @exception IllegalActionException If connection or authentication
     *  process fails.
     */
    private void _connectAndLogin() throws IllegalActionException {

        // already connected
        if (_connection != null && _connection.isConnected()
                && _connection.isAuthenticated()) {
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
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Unable to connect to XMPP server.");
        }

        try {
            // login to the server
            if (!_connection.isAuthenticated()) {
                if (_password == null) {
                    if (passwordFile.stringValue().length() > 0) {
                        // Read the password from a file
                        BufferedReader reader = null;
                        try {
                            reader = passwordFile.openForReading();
                            String line = reader.readLine();
                            if (line != null) {
                                _password = line.toCharArray();
                                line = "";
                            } else {
                                throw new IllegalActionException(this,
                                        "Failed to read a line from "
                                                + passwordFile);
                            }
                        } catch (Exception ex) {
                            throw new IllegalActionException(this, ex,
                                    "Failed to read "
                                            + passwordFile.stringValue());
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException ex) {
                                    throw new IllegalActionException(this, ex,
                                            "Failed to close "
                                                    + passwordFile
                                                    .stringValue());
                                }
                            }
                        }
                    } // if (passwordFile.stringValue().length() > 0)
                    if (_password == null) {
                        // Open a dialog to get the password.
                        Effigy effigy = Configuration.findEffigy(toplevel());
                        JFrame frame = null;
                        if (effigy != null) {
                            Tableau tableau = effigy.showTableaux();
                            if (tableau != null) {
                                frame = tableau.getFrame();
                            }
                        }

                        // Next construct a query for user name and password.
                        Query query = new Query();
                        query.setTextWidth(60);
                        query.addPassword("password", "Password", "");
                        ComponentDialog dialog = new ComponentDialog(frame,
                                "Open Connection", query);

                        if (dialog.buttonPressed().equals("OK")) {
                            // The password is not stored as a parameter.;
                            _password = query.getCharArrayValue("password");
                        } else {
                            return;
                        }
                    }
                }
                _connection.login(_userName, new String(_password), "ptolemy");
                // After use, set password to null for security
                _password = null;
            }
        } catch (XMPPException ex) {
            throw new IllegalActionException(this, ex,
                    "Unable to login to XMPP server.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Maintains the connection to the server. */
    private Connection _connection;

    /** Manager responsible for brokering publications and subscriptions. */
    private PubSubManager _manager;

    /** The password last entered. Use a char array instead of a String for
     * security since a char array can be explictly cleared, whereas a String
     * is immutable and persists in memory until garbage collection.  See
     * http://stackoverflow.com/questions/8881291/why-is-char-preferred-over-string-for-passwords */
    private char[] _password;

    /** Port number of the server to connect to. */
    private int _portNumber = 5222;

    /** Address of the server to connect to. */
    private String _serverName = "localhost";

    /** User name on the server to connect to. */
    private String _userName = "ptolemy";

}
