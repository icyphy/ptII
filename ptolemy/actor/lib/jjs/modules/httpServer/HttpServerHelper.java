/* A helper class for the httpServer JavaScript module.

@Copyright (c) 2015-2018 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.httpServer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;

import javax.imageio.ImageIO;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// HttpServerHelper

/**
   A helper class for the httpSocket module's Server object in JavaScript.
   Instances of this class are helpers for a server that can support multiple sockets.
   See the documentation of that module for instructions.
   This uses Vert.x for the implementation.

   @author Edward A. Lee amd Elizabeth Osyk
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class HttpServerHelper extends VertxHelperBase {

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

    /** Convert a string to an image token.
     *  If there is a problem then _error() is called.
     *  @param body The html to be converted.
     *  @return The ImageToken that is created.
     */
    public AWTImageToken convertImageBody(String body) {
        // If the body includes image information, e.g. data:image/png;base64, remove this.
        int comma = body.indexOf(",");

        if (comma > -1) {
            body = body.substring(comma + 1, body.length());
        }

        try {
            // This is tested by
            // $PTII/ptolemy/actor/lib/jjs/modules/httpClient/test/auto/RESTSendImage.xml

            // Don't use
            // javax.xml.bind.DatatypeConverter.parse64Binary()
            // here because javax.xml.bind.DatatypeConverter is
            // not directly available in Java 9.  To use it
            // requires compiling with --add-modules
            // java.xml.bind, which seems to not be easily
            // supported in Eclipse.
            // An alternative would be to use Apache commons codec,
            // but this would introduce a compile and runtime dependency.

            byte[] bytes = Base64.getDecoder().decode(body);

            // byte [] bytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(body);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            AWTImageToken token = new AWTImageToken(image);
            return token;
        } catch (Throwable throwable) {
            System.out.println("Failed to parse image: " + throwable);
            throwable.printStackTrace();
            _error(_actor + "Received unparseable image: "
                    + throwable.toString(), throwable);
            AWTImageToken imageToken = new AWTImageToken(null);
            return imageToken;
        }
    }

    /** Create a HttpServerHelper instance to help a JavaScript HttpServer instance.
     *  @param actor The actor associated with this helper.
     *  @param currentObj The JavaScript HttpServer instance for which this is a helper.
     *  @param hostInterface The host interface to use, in case there the host has more
     *   than one interface (e.g. Ethernet and WiFi). This is IP address or name, and if
     *   the argument is null, then "localhost" will be used.
     *  @param port The port number that the server will use.
     *  @return A new HttpServerHelper instance.
     */
    public static HttpServerHelper getOrCreateServer(Object actor,
            ScriptObjectMirror currentObj, String hostInterface, int port) {
        // If there already is a server for this actor with matching hostInterface and port, use it.
        VertxHelperBase helper = VertxHelperBase.getHelper(actor);
        if (helper instanceof HttpServerHelper) {
            if (((hostInterface == null
                    && ((HttpServerHelper) helper)._hostInterface == null)
                    || (hostInterface != null && hostInterface.equals(
                            ((HttpServerHelper) helper)._hostInterface)))
                    && port == ((HttpServerHelper) helper)._port) {
                // All the interface match.
                // May have a new JavaScript object, however.
                ((HttpServerHelper) helper)._currentObj = currentObj;
                return (HttpServerHelper) helper;
            }
        }
        // If there is some other helper associated with this actor, the following will
        // share the same verticle with that helper.
        return new HttpServerHelper(actor, currentObj, hostInterface, port);
    }

    /** Respond to the request with the specified ID by sending the specified text.
     *  @param requestID The request ID.
     *  @param responseText The text with which to respond.
     *  @param responseCode The response code
     */
    public void respond(int requestID, String responseText, int responseCode) {
        // If request has already timed out, then ignore this response.
        if (_pendingRequests.get(requestID) != null) {
            HttpServerRequest request = _pendingRequests.remove(requestID);
            // The request ID is the timeoutID.
            _actor.clearTimeout(requestID);

            HttpServerResponse response = request.response();

            if (response.closed()) {
                // Response has been closed already. Nothing to do.
                return;
            }

            // Note that there is a slight chance that the response
            // could close before the write()
            response.setStatusCode(responseCode);
            response.headers()
                    .add("Content-Length",
                            String.valueOf(responseText.length()))
                    .add("Content-Type", "text/html");
            response.write(responseText);

            if (response.closed()) {
                // Response has been closed during the write, perhaps
                // because write() took a long time. Nothing to do.

                // Under Travis,
                // ptolemy/actor/lib/jjs/modules/httpClient/test/auto/RESTSendImage.xml
                // was failing with a "Response is closed" exception
                // here.
                return;
            }
            response.end();
        }
    }

    /** Create and start the server and beginning listening for
     *  requests. When a new request is received, call the _request() function
     *  of the associated JavaScript HttpServer object.
     */
    public void startServer() {
        // Ask the verticle to start the server.
        submit(() -> {
            _server = _vertx.createHttpServer();
            _server.requestHandler(new Handler<HttpServerRequest>() {
                @Override
                public void handle(final HttpServerRequest request) {

                    request.exceptionHandler((throwable) -> {
                        _error("Request failed.", throwable);
                    });

                    // Register a body handler.  If a body is present, this handler calls httpServer's _request().
                    // Otherwise, call httpServer's _request() directly.
                    request.bodyHandler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {

                            // Sometimes the body handler is invoked even when there is no body (i.e. buffer length 0).
                            // Do not call _request in this situation.  It will be called elsewhere.
                            if (buffer.toString().length() > 0) {
                                String method = request.method().toString();
                                String path = request.path();

                                MultiMap headers = request.headers();
                                MultiMap params = request.params();

                                // Make this callback in the director thread instead of
                                // the verticle thread so that all outputs associated with
                                // a request are emitted simultaneously.
                                _issueResponse(() -> {
                                    // Set up a timeout handler.
                                    // FIXME: timeout 10000 needs to be an option given in JS constructor.
                                    TimeoutResponse timeoutResponse = new TimeoutResponse(
                                            request, 10000);
                                    // FIXME: Only handling string bodies for now.
                                    _currentObj.callMember("_request",
                                            timeoutResponse.getTimeoutID(),
                                            method, path, buffer.toString(),
                                            headers.entries(),
                                            params.entries());
                                });
                            }
                        }
                    });

                    String method = request.method().toString();
                    String path = request.path();

                    MultiMap headers = request.headers();
                    MultiMap params = request.params();

                    // Check for a body by checking for a Content-Length header or for chunked encoding.
                    // Content-Length header is omitted for chunked encoding.
                    // Handle all not-yet-handled requests (i.e. no body).
                    if ((headers.contains("Content-Length") && !headers
                            .get("Content-Length").equalsIgnoreCase("0"))
                            || (headers.contains("transfer-encoding")
                                    && headers.get("transfer-encoding")
                                            .equalsIgnoreCase("chunked"))) {
                        // Do nothing.  Request will have been handled by request.bodyHandler.
                    } else {
                        // Make this callback in the director thread instead of
                        // the verticle thread so that all outputs associated with
                        // a request are emitted simultaneously.
                        _issueResponse(() -> {
                            // Set up a timeout handler.
                            // FIXME: timeout 10000 needs to be an option given in JS constructor.
                            TimeoutResponse timeoutResponse = new TimeoutResponse(
                                    request, 10000);
                            _currentObj.callMember("_request",
                                    timeoutResponse.getTimeoutID(), method,
                                    path, null, headers.entries(),
                                    params.entries());
                        });
                    }
                }
            });
            _server.listen(_port, _hostInterface,
                    new Handler<AsyncResult<HttpServer>>() {
                        @Override
                        public void handle(AsyncResult<HttpServer> result) {
                            // Do this in the vertx thread, not the director thread, so that the
                            // listening event is assured of occurring before any requests are
                            // emitted.
                            _currentObj.callMember("emit", "listening");
                        }
                    });
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for HttpServerHelper to create a web server.
     *  @param actor The actor associated with this server.
     *  @param currentObj The JavaScript Server instance for which this a helper.
     *  @param hostInterface The host interface to use, in case there the host has more
     *   than one interface (e.g. Ethernet and WiFi). This is IP address or name, and if
     *   the argument is null, then "localhost" will be used.
     *  @param port The port on which to create the server.
     */
    private HttpServerHelper(Object actor, ScriptObjectMirror currentObj,
            String hostInterface, int port) {
        super(actor, currentObj);
        _hostInterface = hostInterface;
        if (hostInterface == null) {
            _hostInterface = "localhost";
        }
        _port = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The host interface. */
    private String _hostInterface;

    /** Pending timeout ID to requests map. */
    private HashMap<Object, HttpServerRequest> _pendingRequests = new HashMap<Object, HttpServerRequest>();

    /** The port on which the server listens. */
    private int _port;

    /** The internal http server created by Vert.x */
    private HttpServer _server = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A task to execute when no response has been received by the user of this class
     *  after a timeout has expired. To use this class, specify an HTTP request and a
     *  timeout (in milliseconds) to the constructor. The constructor will set a timeout
     *  to execute after the specified time. Use cancel() to cancel the request.
     */
    private class TimeoutResponse implements Runnable {
        public TimeoutResponse(HttpServerRequest request, int timeout) {
            _request = request;
            try {
                _timeoutID = _actor.setTimeout(this, timeout);
            } catch (IllegalActionException e) {
                _error("Setting timeout for response failed.", e);
            }
            _pendingRequests.put(_timeoutID, request);
        }

        public Object getTimeoutID() {
            return _timeoutID;
        }

        @Override
        public void run() {
            if (_pendingRequests.get(_timeoutID) != null) {
                _pendingRequests.remove(_timeoutID);
            }

            HttpServerResponse response = _request.response();
            // Response code 500 is "Internal Server Error".
            response.setStatusCode(500);
            String responseText = "<html><h1>Internal Server Error (code 500)</h1>"
                    + "<p>Server failed to generate a response within its internal "
                    + "timeout period.</p></html>";
            response.headers()
                    .add("Content-Length",
                            String.valueOf(responseText.length()))
                    .add("Content-Type", "text/html");
            response.end(responseText);
        }

        private Object _timeoutID;
        private HttpServerRequest _request;
    }
}
