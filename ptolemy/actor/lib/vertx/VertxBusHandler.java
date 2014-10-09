/* An actor that subscribes to and publishes to a Vert.x eventbus.

 Copyright (c) 1998-2014 The Regents of the University of California.
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


package ptolemy.actor.lib.vertx;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketVersion;
import org.vertx.java.core.json.JsonObject;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** An actor that subscribes to and publishes to a Vert.x eventbus
 *  hosted on a server.
 * @author Patricia Derler
@version $Id: DateToEvent.java 70268 2014-10-01 17:28:35Z pd $
@since Ptolemy II 10.0
 * @Pt.ProposedRating Red (pd)
 * @Pt.AcceptedRating Red (pd)
 */
public class VertxBusHandler extends TypedAtomicActor {

    /**Construct an actor in the default workspace with an empty string as its
     * name. The object is added to the workspace directory. Increment the
     * version number of the workspace.
     * 
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public VertxBusHandler() throws IllegalActionException,
            NameDuplicationException {
        super();
        _init();
    }

    /**Construct an actor in the specified workspace with an empty string as a
     * name. You can then change the name with setName(). If the workspace
     * argument is null, then use the default workspace. The object is added to
     * the workspace directory. Increment the version number of the workspace.
     * 
     * @param workspace
     *            The workspace that will list the entity.
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public VertxBusHandler(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    /**
     * Create a new actor in the specified container with the specified name.
     * The name must be unique within the container or an exception is thrown.
     * The container argument must not be null, or a NullPointerException will
     * be thrown.
     *
     * @param container
     *            The container.
     * @param name
     *            The name of this actor within the container.
     * @exception IllegalActionException
     *                If this actor cannot be contained by the proposed
     *                container (see the setContainer() method).
     * @exception NameDuplicationException
     *                If the name coincides with an entity already in the
     *                container.
     */
    public VertxBusHandler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Input port that receives tokens to be published on the eventbus.
     */
    public TypedIOPort publish;
    
    /** Output port that outputs tokens received from the eventbus.
     */
    public TypedIOPort subscribe;

    /** Event bus address that this actor publishes to and subscribes to.
     */
    public Parameter address;
    
    /** Host address of the server that runs the event bus.
     */
    public Parameter host;
    
    /** Port on the host that provides access to the event bus.
     */
    public Parameter port;

    /** If there is a token on the input, convert the token to
     *  a json object and publish that to the event bus.
     */
    @Override
    public void fire() throws IllegalActionException {
        for (int i = 0; i < publish.getWidth(); i++) {
            if (publish.hasToken(i)) {
                StringToken token = (StringToken) publish.get(i);
                String tokenString = token.stringValue();
                // Remove leading and trailing double quotes.
                JsonObject msg = new JsonObject().putString("type", "publish")
                        .putString("address", _address)
                        .putString("body", tokenString);
                _websocket.writeTextFrame(msg.encode());
            }
        }
    }

    /** Handle changes in attributes.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == address) {
            if (address.getToken() != null) {
                _address = ((StringToken) address.getToken()).stringValue();
            }
        } else if (attribute == host) {
            if (host.getToken() != null) {
                _host = ((StringToken) host.getToken()).stringValue();
            }
        } else if (attribute == port) {
            if (port.getToken() != null) {
                _port = ((IntToken) port.getToken()).intValue();
            }
        }
    }
    
    /** Initialize verticle, create http client and open web socket to connect
     *  to event bus.
     */
    @Override
    public void initialize() {
        _vertx = VertxFactory.newVertx();
        _client = _vertx.createHttpClient().setHost(_host).setPort(_port);
        _openWebSocket();
    }

    /** Wrap up, close web socket if open, stop vertx.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _websocket.close();
        _client.close();
        _vertx.stop();
    }

    /** Initialize ports and parameters.
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        publish = new TypedIOPort(this, "publish", true, false);
        subscribe = new TypedIOPort(this, "subscribe", false, true);
        address = new Parameter(this, "address");
        host = new Parameter(this, "host");
        port = new Parameter(this, "port");
    }

    /** Open a web socket that serves as a connection to the event bus.
     */
    private void _openWebSocket() {
        MultiMap map = new CaseInsensitiveMultiMap();
        map.add("connectTimeout", "10000000");
        _client.connectWebsocket("/eventbus/websocket", 
                WebSocketVersion.RFC6455, map, new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
                //register
                JsonObject msg = new JsonObject().putString("type",
                        "register").putString("address", _address);
                websocket.writeTextFrame(msg.encode());
                _websocket = websocket;
    
                websocket.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buff) {
                        String msg = buff.toString();
                        JsonObject received = new JsonObject(msg);
                        try {
                            subscribe.send(0,
                                    new StringToken(received.getField("body")));
                        } catch (NoRoomException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IllegalActionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } 
                });
                
                _websocket.closeHandler(new Handler<Void>() {
                    @Override
                    public void handle(final Void event) {
                        _openWebSocket();
                    }
                });
            }
        });
    }

    private HttpClient _client;
    private Vertx _vertx;
    private String _address;
    private String _host;
    private int _port;
    private WebSocket _websocket;

}
