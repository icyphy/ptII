/* A helper for a web socket server.

@Copyright (c) 2015-2016 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.lib.jjs.modules.webSocket;

import java.io.File;
import java.io.IOException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.PfxOptions;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;
import ptolemy.util.FileUtilities;

///////////////////////////////////////////////////////////////////
//// WebSocketServerHelper

/**
   A helper class for the webSocket module's Server object in JavaScript.
   Instances of this class are helpers for a server that can support multiple sockets.
   See the documentation of that module for instructions.
   This uses Vert.x for the implementation.

   @see WebSocketHelper
   @author Hokeun Kim and Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class WebSocketServerHelper extends VertxHelperBase {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the web socket server. Note that this closing happens
     *  asynchronously. The server may not be closed when this returns.
     */
    public void closeServer() {
            // Ask the verticle to perform the close.
            submit(() -> {
                if (_server != null) {
                    _server.close();
                    _server = null;
                }
            });
    }

    /** Create a WebSocketServerHelper instance to help a JavaScript Server instance.
     *  @param actor The actor associated with this helper.
     *  @param currentObj The JavaScript Server instance for which this is a helper.
     *  @param hostInterface The host interface to use, in case there the host has more
     *   than one interface (e.g. Ethernet and WiFi). This is IP address or name, and if
     *   the argument is null, then "localhost" will be used.
     *  @param sslTls Whether SSL/TLS is enabled. This defaults to false.
     *  @param port The port number that the server will use.
     *  @param pfxKeyCertPassword The password to open the file specified in pfxKeyCertPath.
     *  @param pfxKeyCertPath The path of the file that stores the private key and certificate in PFX (PKCS#12) format for the server.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     *  @return A new WebSocketServerHelper instance.
     */
    public static WebSocketServerHelper createServer(Object actor,
            ScriptObjectMirror currentObj, String hostInterface, boolean sslTls,
            String pfxKeyCertPassword, String pfxKeyCertPath,
            int port, String receiveType, String sendType) {
        return new WebSocketServerHelper(actor,
                currentObj, hostInterface, sslTls, pfxKeyCertPassword, pfxKeyCertPath,
                port, receiveType, sendType);
    }

    /** Create and start the server and beginning listening for
     *  connections. When a new connection request is received and
     *  a socket has been opened, emit the 'connection' event with the
     *  socket as an argument.
     */
    public void startServer() {
            // Ask the verticle to start the server.
            submit(() -> {
                HttpServerOptions serverOptions = new HttpServerOptions();
                serverOptions.setSsl(_sslTls);
                if (serverOptions.isSsl()) {
                PfxOptions pfxOptions = new PfxOptions();
                File pfxKeyCertFile = FileUtilities.nameToFile(_pfxKeyCertPath, null);
                if (pfxKeyCertFile == null) {
                    _error(_currentObj, "Empty pemCertPath option. Can't find the server key-certificate.");
                    return;
                }
                try {
                    pfxOptions.setPath(pfxKeyCertFile.getCanonicalPath());
                } catch (IOException e) {
                    _error(_currentObj, "Failed to find the server key-certificate at " + pfxKeyCertFile);
                    return;
                }
                pfxOptions.setPassword(_pfxKeyCertPassword);
                serverOptions.setPfxKeyCertOptions(pfxOptions);
                }
                _server = _vertx.createHttpServer(serverOptions);
                _server.websocketHandler(new Handler<ServerWebSocket>() {
                    @Override
                    public void handle(ServerWebSocket serverWebSocket) {
                        // Notify of a new connection by emitting a 'connection' event.
                        // This will have the side effect of creating a new JS Socket
                        // object, which is an event emitter.
                        // This has to be done in the verticle thread, not later in the
                        // director thread, because it will set up listeners to the socket.
                        // If that is deferred, then the server could miss messages that are
                        // sent after the connection is established.
                        // Pass this helper to ensure that the verticle and event bus handler
                        // of this verticle is used rather than creating a new one.
                        _currentObj.callMember(
                                "_socketCreated", serverWebSocket, WebSocketServerHelper.this);
                    }
                });
                _server.listen(_port, _hostInterface,
                        new Handler<AsyncResult<HttpServer>>() {
                    @Override
                    public void handle(AsyncResult<HttpServer> result) {
                        // Do this in the vertx thread, not the director thread, so that the
                        // listening event is assured of occurring before the 'connection'
                        // event, which is emitted above by _socketCreated().
                        _currentObj.callMember("emit", "listening");
                    }
                });
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketServerHelper to create a web socket server.
     *  @param actor The actor associated with this helper.
     *  @param currentObj The JavaScript Server instance for which this a helper.
     *  @param hostInterface The host interface to use, in case there the host has more
     *   than one interface (e.g. Ethernet and WiFi). This is IP address or name, and if
     *   the argument is null, then "localhost" will be used.
     *  @param port The port on which to create the server.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     */
    private WebSocketServerHelper(Object actor,
            ScriptObjectMirror currentObj,
            String hostInterface, boolean sslTls,
            String pfxKeyCertPassword, String pfxKeyCertPath,
            int port, String receiveType,
            String sendType) {
            // NOTE: Really should have only one of these per actor,
            // and the argument below should be the actor.
        super(actor, currentObj);
        _hostInterface = hostInterface;
        if (hostInterface == null) {
            _hostInterface = "localhost";
        }
        _sslTls = sslTls;
        _pfxKeyCertPassword = pfxKeyCertPassword;
        _pfxKeyCertPath = pfxKeyCertPath;
        _port = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The host interface. */
    private String _hostInterface;

    /** Whether the server runs on SSL/TLS. */
    private boolean _sslTls;

    /** The password for pfx key-cert file. */
    private String _pfxKeyCertPassword;

    /** The path for pfx key-cert file. */
    private String _pfxKeyCertPath;

    /** The port on which the server listens. */
    private int _port;

    /** The internal http server created by Vert.x */
    private HttpServer _server = null;
}
