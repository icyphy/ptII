/* A JavaScript helper for HttpClient.

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
package ptolemy.actor.lib.jjs.modules.httpClient;

import java.util.HashMap;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;

import ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelperBase;

///////////////////////////////////////////////////////////////////
//// HttpClientHelper

/**
   A helper class for the HttpClient module in JavaScript.
   
   @author Marten Lohstroh
   @version $Id: HttpClientHelper.java 71938 2015-04-08 21:55:19Z hokeunkim $
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
 */
public class HttpClientHelper extends WebSocketHelperBase { // FIXME: rename this class to VertxHelperBase

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Create a HttpClientHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @return A new HttpClientHelper instance.
     */
    public static HttpClientHelper createHttpClient(
            ScriptObjectMirror currentObj, HashMap<String, Object> options) {
        return new HttpClientHelper(currentObj, options);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketHelper to open a client-side web socket.
     *  Open an internal web socket using Vert.x.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param address The URL of the WebSocket host with an optional port number
     *   (e.g. 'ws://localhost:8000'). If no port number is given, 80 is used.
     */
    private HttpClientHelper(ScriptObjectMirror currentObj, HashMap<String, Object> options) {
        _currentObj = currentObj;

        String host = "localhost"; // test (String)options.get("host")
        int port = 80; // (Integer)options.get("port")
        HttpClient client = _vertx.createHttpClient();
        client.setHost(host); 
        client.setPort(port); 
        client.exceptionHandler(new HttpClientExceptionHandler());
        // FIXME: Provide a timeout. Use setTimeout() of the client.
        
        // FIXME: Why does Vertx require the URI here in addition to setHost() and setPort() above? Seems lame.
        // FIXME: also get the protocol from options
        String address = "http://" + host + ":" + port;
   
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
        
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;

    
    /** The event handler that is triggered when an error occurs in the web socket connection.
     */
    private class HttpClientExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable throwable) {
            synchronized(HttpClientHelper.this) {
            _currentObj.callMember("emit", "error", throwable.getMessage());
            //_wsIsOpen = false;
            }
        }
    }
    
}
