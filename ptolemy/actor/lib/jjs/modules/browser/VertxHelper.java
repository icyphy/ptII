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

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

///////////////////////////////////////////////////////////////////
//// VertxHelper

/** A helper class for the Vert.x browser API.
   
   @author FIXME
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (pd)
   @Pt.AcceptedRating Red (pd)
 */
public class VertxHelper {
    
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
	    Handler handler = new AsyncResultHandler<Void>() {
		public void handle(AsyncResult<Void> asyncResult) {
		    // FIXME: Called when the server actually starts listening.
		    // Probably need to have a callback back to JavaScript here.
		}
	    };
	    _vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
		public void handle(HttpServerRequest req) {
		    HttpServerResponse response = req.response();
		    response.setChunked(true);
		    response.write(_response);
		    response.end();
		}
	    }).listen(port, "0.0.0.0", handler);
	}
	public void setResponse(String response) {
	    _response = response;
	}
	private String _response = "No data yet";
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
    
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;
    
    /** Instance of Vertx. Apparently we need only one. */
    private static Vertx _vertx = VertxFactory.newVertx();
}
