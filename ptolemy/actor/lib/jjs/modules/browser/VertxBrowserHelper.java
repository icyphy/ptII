/* A Javascript helper for Vert.x.

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
package ptolemy.actor.lib.jjs.modules.browser;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

///////////////////////////////////////////////////////////////////
//// VertxHelper

/** A helper class for the Vert.x browser API.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (pd)
   @Pt.AcceptedRating Red (pd)
 */
public class VertxBrowserHelper {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a web server that serves the specified string.
     *  @param port The port to listen for requests on.
     *  @return A new VertxHelper.
     */
    public static Server createServer(int port) {
        return new Server(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A http server that gets and sets responses.
     */
    public static class Server {

        ///////////////////////////////////////////////////////////////////
        ////                     public methods                        ////

        /** Instantiate a http server that listens only on localhost.
         *  @param port The port number
         */
        public Server(int port) {
            boolean firstStart = false;

            if (_server == null) {
                _server = _vertx.createHttpServer();

                // Serve static content.  This assumes the accessors repo is
                // installed locally at $PTII/org/terraswarm/accessor
                _router = Router.router(_vertx);
                _router.route("/accessors/*").handler(StaticHandler.create("org/terraswarm/accessor/accessors/web"));

                // new Exception("Start of Server(" + port + ")").printStackTrace();

                // Handle dynamic content (requests to / ).
                _router.route().handler(routingContext -> {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "text/html");
                    response.setChunked(true);
                    response.write(_getResponse());
                    response.end();
                });

                _server.requestHandler(_router::accept);

                firstStart = true;
            }

            // The second argument specifies to listen
            // on localhost only (interface lo0).
            if (firstStart || port != _port) {
                _port = port;
                firstStart = false;
                _server.listen(port, "127.0.0.1",
                        new Handler<AsyncResult<HttpServer>>() {
                            public void handle(AsyncResult<HttpServer> asyncResult) {
                                System.err.println("Server(" + port
                                        + ").handle(<AsyncResult> " + asyncResult
                                        + ")" + " Listen succeeded? "
                                        + asyncResult.succeeded() + " cause: "
                                        + asyncResult.cause());
                                // FIXME: Called when the server actually starts listening.
                                // Probably need to have a callback back to JavaScript here.
                            }
                        });
            }

        }

        /** Set the response.
         *  @param response The response.
         */
        public void setResponse(String response) {
            System.err.println("setResponse(" + response + ")");
            _response = response;
        }

        /** Shut down the server, if running.
         */
        public void shutdown() {
            if (_server != null) {
                _server.close();
                _server = null;
            }
            // FIXME:  Wait for callback to ensure no shutdown errors?
        }

        ///////////////////////////////////////////////////////////////////
        ////                     private methods                       ////

        /** Get the response.
         *  @return The response.
         */
        private String _getResponse() {
            System.err.println("getResponse(): " + _response);
            return _response;
        }

        ///////////////////////////////////////////////////////////////////
        ////                     private fields                        ////

        /** The port that the server is listening on.
         */
        private int _port = 8080;

        /** The server's response.
         */
        private String _response = "No data yet";

        /** The HTTP server.  Currently, only a single server at a time is
         * supported.
         */
        private HttpServer _server = null;

        /** A router to route incoming requests to the appropriate handlers.
         */
        private Router _router = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Instance of Vertx. Apparently we need only one. */
    private static Vertx _vertx = Vertx.vertx();
}
