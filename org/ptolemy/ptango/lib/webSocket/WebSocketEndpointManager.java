/* A singleton class that manages websocket endpoints and their connection
 * openers (clients, servlets).

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

package org.ptolemy.ptango.lib.webSocket;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;

import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.ptolemy.ptango.lib.webSocket.WebSocketEndpoint;
import org.ptolemy.ptango.lib.webSocket.WebSocketService;

import ptolemy.kernel.util.IllegalActionException;

/** A singleton class that manages websocket endpoints and their connection
* openers (clients, servlets).
*
*  @author Elizabeth Latronico
*  @version $Id$
*  @since Ptolemy II 10.0
*  @Pt.ProposedRating Red (ltrnc)
*  @Pt.AcceptedRating Red (ltrnc)
*/

public class WebSocketEndpointManager {

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////

   /** Get the factory instance, creating a new one if none created yet.
    * @return The websocket client factory instance.
    */
   public static WebSocketEndpointManager getInstance() {
       return WebSocketEndpointFactoryHolder.INSTANCE;
   }

   /** Create a new WebSocketConnectionFactory.  The private
    *  constructor prevents instantiation from other classes.
    */
   private WebSocketEndpointManager() {

       _webSocketClientFactory = new WebSocketClientFactory();
       _pathToServlet = new Hashtable();
       _pathToClientEndpoint = new Hashtable();
       _pathToServletEndpoint = new Hashtable();
       
       try {
           _webSocketClientFactory.start();
           _webSocketClient = _webSocketClientFactory.newWebSocketClient();
       } catch (Exception e) {
           // If an exception occurs, _webSocketClient will remain null.
           // This is checked for later.
       }
   }

   /** The Holder is loaded on the first execution of getInstance()
   * or the first access to INSTANCE, allowing on-demand loading since
   * not all models use websockets.
   */
   private static class WebSocketEndpointFactoryHolder {
       private static final WebSocketEndpointManager INSTANCE = 
               new WebSocketEndpointManager();
   }
   
   /** Close all servlet websocket endpoints.  Called by webserver in wrapup().
    * Synchronized to avoid interleaving opening and closing endpoints.
    */
   public synchronized void closeServlets() {
       for (String path : _pathToServletEndpoint.keySet()) {
           if (_pathToServletEndpoint.get(path).isOpen()) {
               _pathToServletEndpoint.get(path).close();
           }
           _pathToServletEndpoint.remove(path);
           _pathToServlet.remove(path);
       }
   }
   
   /** Get the servlet for the given path.  If the servlet does not exist, 
    *  create it.  Called by the web server.  Synchronized to avoid interleaving
    *  creating and deleting a servlet.
    * @param path  The URI the servlet should be mapped to.
    * @return The servlet mapped to this path.
    */
   public synchronized PtolemyWebSocketServlet getServlet(String path)
       throws IllegalActionException {
       if (_pathToServlet.containsKey(path)) {
           return _pathToServlet.get(path);
       } else {
           
           WebSocketEndpoint endpoint;
           
           // If we already have an endpoint, retrieve it
           if (_pathToServletEndpoint.containsKey(path)) {
               endpoint = _pathToServletEndpoint.get(path);
           } else {
                endpoint = new WebSocketEndpoint();
                _pathToServletEndpoint.put(path, endpoint);
           }
           
           PtolemyWebSocketServlet servlet = 
                   new PtolemyWebSocketServlet(endpoint);
           _pathToServlet.put(path, servlet);
           return servlet;
       }
   }
   
   /** Return true if the path refers to a remote resource; false otherwise.
    * Throw an exception if the path is not a valid URI.
    * @param path  The candidate URI.
    * @return True if the path refers to a remote resource; false otherwise.
    * @throws IllegalActionException If the path is not a valid URI.
    */
   public static boolean isRemoteURI(String path) throws IllegalActionException{
       String uri = pathToURI(path).toString();
       if (uri.startsWith("ws://") || uri.startsWith("wss://")) {
           return true;
       }
       return false;
   }
   
   /** Return true if the path refers to a remote resource; false otherwise.
    * Throw an exception if the path is not a valid URI.
    * @param uri The candidate URI.
    * @return True if the path refers to a remote resource; false otherwise.
    */
   public static boolean isRemoteURI(URI uri) {
       if (uri.toString().startsWith("ws://") || 
               uri.toString().startsWith("wss://")) {
           return true;
       }
       return false;
   }
   
   /** Check if the given path is a valid URI.  Return true if so; false 
    * otherwise.
    * @param path  The candidate URI.
    * @return True if the path is a valid URI; false otherwise.
    */
   public static boolean isValidURI(String path) {
       try {
           pathToURI(path);
       } catch(IllegalActionException e) {
           return false;
       }
       return true;
   }
   
   /** Open connections for the given set of services by looking up each 
    * service's affiliated endpoint and opening a connection for that endpoint.
    * Assumes that these are local services.  The WebServer uses this to open
    * local services after it has started.  Synchronized so that subscribing and 
    * unsubscribing are not interleaved.
    * @param services  The set of services to open connections for.
    * @param portNumber The port number of the local web server.
    * @throws IllegalActionException If one or more services cannot be opened.
    */
  public synchronized void openLocalServices(HashSet<WebSocketService> services, 
           int portNumber) throws IllegalActionException {
       for (WebSocketService service: services) {
           String path = service.getRelativePath().toString();
           
           // Subscribe this service.  This creates a new endpoint if needed.
           // Called by the WebServer.  Needed as initialize() in WebServer 
           // might be called before initialize() in the reader / writer actors.
           subscribe(service, path);
           
           WebSocketEndpoint endpoint;
           if (service.isClient()) {
               endpoint = _pathToClientEndpoint.get(path);
           } else {
               endpoint = _pathToServletEndpoint.get(path);
           }

           URI connectPath = URI.create("ws://localhost:" + portNumber 
                       + service.getRelativePath().toString());

           _open(endpoint, connectPath);
       }
   }
   
   /** Check the given path to see if it is a valid websocket URI and return a 
    *  URI.  Throw an exception if the path is invalid. 
    * @param path The string representation of a websocket URI.
    * @return The URI corresponding to this path string.
    * @throws IllegalActionException If the path is not a valid URI.
    */
   public static URI pathToURI(String path) throws IllegalActionException {
       
       try {
           // Paths connecting to remote websockets should start with ws://
           // or wss:// (for secure websockets)
           // Paths not starting with these are assumed to be local
           // For locally hosted websockets should start with a "/"
           // or be "*"
           if (!path.trim().equals("")) {
               // Check for common incorrect protocols
               if (path.startsWith("http") || path.startsWith("ftp")) {
                   throw new IllegalActionException("Remote websocket"
                        + " paths must start with ws:// or wss://");
               }

               if (path.startsWith("ws://") || path.startsWith("wss://")) {
                   return URI.create(path);
               } else if (!path.trim().startsWith("/")) {
                   return URI.create("/" + path);
               } else {
                   return URI.create(path);
               }
           } else {
               return URI.create("/*");
           }
       } catch (IllegalArgumentException e2) {
           throw new IllegalActionException("Path is not a valid URI: " + path);
       }
   }
   
   /** Add the given service as a subscriber to the endpoint for this path.
    * Create a new endpoint if none exists for this path.  Synchronized to avoid
    * interleaved subscribing and unsubscribing.
    * Generally, subscribers should subscribe in initialize() and should
    * unsubscribe in wrapup(). 
    * @param service The service to add as a subscriber.
    * @param path  The URL to subscribe to.
    * @throws IllegalActionException If the URI is not valid.
    * @see #unsubscribe(WebSocketService, String)
    */
   // TODO:  Allow non-shared websockets.
   public synchronized void subscribe(WebSocketService service, String path) 
       throws IllegalActionException {
       
       // Check for existing endpoint
       WebSocketEndpoint endpoint;
       
       if (service.isClient()) {
           if (_pathToClientEndpoint.containsKey(path)) {
               endpoint = _pathToClientEndpoint.get(path);
           } else {
               
               // If no existing endpoint, create a new endpoint
               // First, check that the URI is valid
               if (!isValidURI(path)) {
                   throw new IllegalActionException("Path is not a valid URI: " 
                           + path);
               }
               
               endpoint = new WebSocketEndpoint();
               _pathToClientEndpoint.put(path, endpoint);
               
               // Open any new remote endpoints.
               // Local client-side endpoints will be opened by the webserver.
               // If a server-side endpoint, create a servlet.  This servlet 
               // will automatically open the socket when an incoming request is 
               // received.
               if (isRemoteURI(path)) {
                   _open(endpoint, pathToURI(path));
               }
           }
       } else {
           
           if (_pathToServletEndpoint.containsKey(path)) {
               endpoint = _pathToServletEndpoint.get(path);
           } else {
               // If no existing endpoint, create a new endpoint
               // First, check that the URI is valid
               if (!isValidURI(path)) {
                   throw new IllegalActionException("Path is not a valid URI: " 
                           + path);
               }
               
               endpoint = new WebSocketEndpoint();
               _pathToServletEndpoint.put(path, endpoint);
               
           
               // Create a new servlet for server-side endpoints
               // _pathToServlet should not contain an endpoint if 
               // _pathToEndpoint did not contain the endpoint
               _pathToServlet.put(path, new PtolemyWebSocketServlet(endpoint));
           }  
       }
       
       endpoint.addSubscriber(service);
       service.setEndpoint(endpoint);
   }
   
   /** Remove the given service as a subscriber to the endpoint for this path.
    * Delete the endpoint if no services are associated with it. Synchronized to
    * avoid conflicting writes to tables.
    * @param service  The service to remove as a subscriber
    * @param path  The URL to subscribe to
    * @see #subscribe(WebSocketService, String)
    */
   // TODO:  Allow non-shared websockets.
   public synchronized void unsubscribe(WebSocketService service, String path) {
       WebSocketEndpoint endpoint;
       
       if (_pathToClientEndpoint.containsKey(path)) {
           endpoint = _pathToClientEndpoint.get(path);
           endpoint.removeSubscriber(service);
           service.setEndpoint(null);
           
           if (endpoint.getSubscriberCount() <= 0) {
               endpoint.close();
               _pathToClientEndpoint.remove(path);
           }
       } else if (_pathToServletEndpoint.containsKey(path)) {
           endpoint = _pathToServletEndpoint.get(path);
           endpoint.removeSubscriber(service);
           service.setEndpoint(null);
     
           // Close connection if no subscribers left
           if (endpoint.getSubscriberCount() <= 0) {
               endpoint.close();
               _pathToServletEndpoint.remove(path);
           }
       }
       
       // The webserver closes server-side connections on wrapup
       // Server-side connections also close automatically if the client closes
   }

   ///////////////////////////////////////////////////////////////////
   ////                         private methods                   ////
   
   /** Open a websocket connection for the given endpoint.
    * @param endpoint  The endpoint to open a connection for.
    * @param uri  The URI to connect to.
    * @throws IllegalActionException If the connection cannot be opened.
    */
   private void _open(WebSocketEndpoint endpoint, URI uri) 
           throws IllegalActionException{
       if (!endpoint.isOpen()) {

           // .open() will call .onOpen() on the endpoint.
           // TODO:  Add timeout here?
           try {
               _webSocketClient.open(uri, endpoint);
           } catch (IOException e) {
               throw new IllegalActionException("Cannot open "
                       + "websocket to path " + uri.toString());
           }
       }
   }
   
   ///////////////////////////////////////////////////////////////////
   ////                         private variables                 ////
   
   /** The timeout for establishing a connection.  In milliseconds. */
   // TODO:  Not used currently.
   //private static int _connectionTimeout = 5000;
   
   /** Paths and their corresponding client endpoints.  Assumes all connections 
    * are shared. */
   // TODO:  Enable non-shared connections.  
   private Hashtable<String, WebSocketEndpoint> _pathToClientEndpoint;
   
   /** Websocket servlets for creating connections to local URLs.  */
   private Hashtable<String, PtolemyWebSocketServlet> _pathToServlet;
   
   /** Paths and their corresponding servlet endpoints.  (Note that a client 
    * endpoint and a servlet endpoint may have the same path, so these 
    * endpoints cannot be stored in the same hashtable).  Assumes all 
    * connections are shared.
    */
   private Hashtable<String, WebSocketEndpoint> _pathToServletEndpoint;
   
   /** A websocket client for creating connections to remote URLs.  */
   private static WebSocketClient _webSocketClient;

   /** A factory for creating WebSocketClients. */
   private static WebSocketClientFactory _webSocketClientFactory;

}
   
