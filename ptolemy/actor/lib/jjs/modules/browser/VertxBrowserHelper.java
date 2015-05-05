/* A Javascript helper for Vert.x.

   Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.browser;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

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
    ////                     public methods                        ////
    
    /** Create a web server that serves the specified string.
     *  @param currentObj The JavaScript object on which this method is called.
     *  @param html The string to serve.
     *  @param port The port to listen for requests on.
     *	@return A new VertxHelper.
     */
    public static Server createServer(int port) {
	return new Server(port);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     inner classes                         ////

    public static class Server {
	public Server(int port) {
	    HttpServer server = _vertx.createHttpServer();
            // new Exception("Start of Server(" + port + ")").printStackTrace();
            server.requestHandler(new Handler<HttpServerRequest>() {
        	public void handle(HttpServerRequest request) {		        
        	    // System.err.println("Server(" + port + ").handle(HttpServerRequest " + request + "), server: " + server + " _response: " + _getResponse());
        	    HttpServerResponse response = request.response();
        	    response.putHeader("content-type", "text/html");
        	    response.setChunked(true);
        	    response.write(_getResponse());
        	    response.end();

        	    // Need to close the server after writing to it
        	    // otherwise subsequent firings of the accessor
        	    // will not write new material
        	    //server.close();
        	}
            });
            
            // The second argument specifies to listen
            // on localhost only (interface lo0).
            server.listen(port, "127.0.0.1", new Handler<AsyncResult<HttpServer>>() {
        	public void handle(AsyncResult<HttpServer> asyncResult) {
        	    System.err.println("Server(" + port + ").handle(<AsyncResult> " + asyncResult + ")" + " Listen succeeded? " + asyncResult.succeeded() + " cause: " + asyncResult.cause() );
        	    // FIXME: Called when the server actually starts listening.
        	    // Probably need to have a callback back to JavaScript here.
        	}
            });
	}
	public void setResponse(String response) {
            System.err.println("setResponse(" + response + ")");
	    _response = response;
	}

        private String _getResponse() {
            System.err.println("getResponse(): " + _response);
            return _response;
        }

        private  String  _response = "No data yet";
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
    
    /** Instance of Vertx. Apparently we need only one. */
    private static Vertx _vertx = VertxFactory.newVertx();
}
