/* A JavaScript helper for HttpClient.

@Copyright (c) 2015 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.httpClient;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

import ptolemy.actor.lib.jjs.modules.VertxHelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// HttpClientHelper

/**
   A helper class for the HttpClient module in JavaScript.
   
   @author Marten Lohstroh, Edward A. Lee
   @version $Id: HttpClientHelper.java 71938 2015-04-08 21:55:19Z hokeunkim $
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
 */
public class HttpClientHelper extends VertxHelperBase {

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Create a HttpClientHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  The options argument can be a string URL
     *  or a map with the following fields (this helper class assumes
     *  all fields are present, so please be sure they are):
     *  <ul>
     *  <li> headers: An object containing request headers. By default this
     *       is an empty object. Items may have a value that is an array of values,
     *       for headers with more than one value.
     *  <li> keepAlive: A boolean that specified whether to keep sockets around
     *       in a pool to be used by other requests in the future. This defaults to false.
     *  <li> method: A string specifying the HTTP request method.
     *       This defaults to 'GET', but can also be 'PUT', 'POST', 'DELETE', etc.
     *  <li> url: A string that can be parsed as a URL, or an object containing
     *       the following fields:
     *       <ul>
     *       <li> host: A string giving the domain name or IP address of
     *            the server to issue the request to. This defaults to 'localhost'.
     *       <li> path: Request path as a string. This defaults to '/'. This can
     *            include a query string, e.g. '/index.html?page=12', or the query
     *            string can be specified as a separate field (see below). 
     *            An exception is thrown if the request path contains illegal characters.
     *       <li> protocol: The protocol. This is a string that defaults to 'http'.
     *       <li> port: Port of remote server. This defaults to 80. 
     *       <li> query: A query string to be appended to the path, such as '?page=12'.
     *       </ul>
     *  </ul>
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param options The options.
     *  @return A new HttpClientHelper instance.
     */
    public static HttpClientHelper createHttpClient(
            ScriptObjectMirror currentObj, Map<String, Object> options) {
        return new HttpClientHelper(currentObj, options);
    }

    /** End a request. */
    public void end() {
	if (_request != null) {
	    _request.end();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor to open an HTTP client.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param address The URL of the WebSocket host with an optional port number
     *   (e.g. 'ws://localhost:8000'). If no port number is given, 80 is used.
     */
    private HttpClientHelper(ScriptObjectMirror currentObj, Map<String, Object> options) {
        super(currentObj);
        HttpClient client = _vertx.createHttpClient();
        
        // NOTE: Vert.x documentation states about HttpClient:
        // "If an instance is instantiated from some other arbitrary Java thread
        // (i.e. when running embedded) then and event loop will be assigned
        // to the instance and used when any of its handlers are called."
        // Hence, the HttpClient we just created will be assigned its own
        // event loop. We need to ensure that callbacks are mutually exclusive
        // with other Java code here.
        
        Map<String, Object> urlSpec = (Map<String, Object>) options.get("url");
        
        client.setHost((String) urlSpec.get("host")); 
        client.setPort((int) urlSpec.get("port")); 
        client.exceptionHandler(new HttpClientExceptionHandler());
        if ((boolean)options.get("keepAlive")) {
            client.setKeepAlive(true);
        }
        // FIXME: Provide a timeout. Use setTimeout() of the client.
        
        String query = "";
        Object queryObject = urlSpec.get("query");
        if (queryObject != null) {
            String querySpec = queryObject.toString().trim();
            if(!querySpec.equals("") && !querySpec.startsWith("?")) {
        	query = "?" + querySpec;
            }
        }
        
        // NOTE: Documentation of Vertx 2.15 is wrong.
        // The argument is a path with a query, not a URI.
        String uri = urlSpec.get("path")
        	+ query;
        
        // FIXME: How do we set the protocol?
        // It is specified in urlSpec.get("protocol").
        
        _request = client.request(
        	(String)options.get("method"),
        	uri,
        	new HttpClientResponseHandler());
   
        // Handle the headers.
        Map headers = (Map)options.get("headers");
        if (!headers.isEmpty()) {
            for (Object key : headers.keySet()) {
        	Object value = headers.get(key);
        	if (value instanceof String) {
        	    _request.putHeader((String)key, (String)value);
        	} else if (value instanceof Iterable) {
        	    _request.putHeader((String)key, (Iterable<String>)value);
        	}
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
    
    /** The request built in the constructor. */
    private HttpClientRequest _request;

    ///////////////////////////////////////////////////////////////////
    ////                     inner classes                         ////

    /** The event handler that is triggered when an error occurs in the HTTP connection.
     */
    private class HttpClientExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable throwable) {
            synchronized(_actor) {
        	_currentObj.callMember("emit", "error", throwable.getMessage());
            }
        }
    }
    
    /** The event handler that is triggered when a response arrives from the server.
     */
    private class HttpClientResponseHandler implements Handler<HttpClientResponse> {
        @Override
        public void handle(HttpClientResponse response) {
            // The response is not yet complete, but we have some information.
            int status = response.statusCode();
            if (status >= 400) {
                // An error occurred.
                _currentObj.callMember("emit", "error", "Request failed with code "
                        + status
                        + ": "
                        + response.statusMessage());
                return;
            }
            MultiMap headers = response.headers();
            
            // If the response is a redirect, handle that here.
            if(status >=300 && status <= 308 && status != 306) {
                String newLocation = headers.get("Location");
                if (newLocation != null) {
                    // FIXME: How to handle the redirect?
                    _currentObj.callMember("emit", "error", "Redirect to "
                            + newLocation
                            + " not yet handled by HttpClientHelper. "
                            + status
                            + ": "
                            + response.statusMessage());
                    return;
                }
            }
            
            String contentType = headers.get("Content-Type");
            boolean isText = (contentType == null)
                    | (contentType.startsWith("text"));
                        
            // A bodyHandler is invoked after the response is complete.
            // Note that large responses could create a memory problem here.
            // Could look at the Content-Length header.
            response.bodyHandler(new Handler<Buffer>() {
                public void handle(Buffer body) {
                    if (isText) {
                        _currentObj.callMember("_response", response, body.toString());
                    } else if (contentType.startsWith("image")) {
                        InputStream stream = new ByteArrayInputStream(body.getBytes());
                        try {
                            Image image = ImageIO.read(stream);
                            Token token = new AWTImageToken(image);
                            _currentObj.callMember("_response", response, token);
                        } catch (IOException e) {
                            // FIXME: What to do here?
                            _currentObj.callMember("_response", response, body.getBytes());
                        }
                    } else {
                        // FIXME: Need to handle other MIME types.Z
                        _currentObj.callMember("_response", response, body.getBytes()); 
                    }
                }
            });
        }
    }
}
