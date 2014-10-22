/* A singleton factory class that creates websocket clients.

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

package org.ptolemy.ptango.lib.websocket;

import java.net.URI;
import java.util.Hashtable;

import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

///////////////////////////////////////////////////////////////////
//// PtolemyWebSocketClientFactory

/** A singleton factory class that creates websocket clients.    
 *
 *  @author Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
*/

public class PtolemyWebSocketClientFactory {
        
        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    //// 
 
        /** Get the factory instance, creating a new one if none created yet. 
         * @return The websocket client factory instance.
         */
        public static PtolemyWebSocketClientFactory getInstance() {
                return PtolemyWebSocketClientFactoryHolder.INSTANCE;
        }
        
        /** Create a new WebSocket client.
         * @return A new WebSocketClient.
         * @throws Exception If the WebSocketClientFactory cannot be started.
         */
        public WebSocketClient newWebSocketClient() 
                throws Exception {
            if (_webSocketClientFactory.isStopped()) {
                _webSocketClientFactory.start();
            }
            
            return _webSocketClientFactory.newWebSocketClient();
        }
        
        /** Create a new WebSocket client that can be shared among other 
         * WebSocketServices affiliated with this path.
         * @return A new WebSocketClient.
         * @throws Exception If the WebSocketClientFactory cannot be started.
         */
        public WebSocketClient newSharedWebSocketClient(URI path) 
                throws Exception {
            if (_webSocketClientFactory.isStopped()) {
                _webSocketClientFactory.start();
            }
            
            if(_sharedClients.containsKey(path)) {
                return _sharedClients.get(path); 
            } else {
                WebSocketClient client 
                    = _webSocketClientFactory.newWebSocketClient();
                _sharedClients.put(path, client);
                return client;
            }
        }
        
        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   //// 
        
        /** Create a new WebSocketClientFactory and start it.  The private 
         *  constructor prevents instantiation from other classes.
         */
        private PtolemyWebSocketClientFactory() { 
            _webSocketClientFactory = new WebSocketClientFactory();
            _sharedClients = new Hashtable();
        }
 
        /** The Holder is loaded on the first execution of getInstance() 
        * or the first access to INSTANCE, allowing on-demand loading since
        * not all models use websockets.
        */
        private static class PtolemyWebSocketClientFactoryHolder { 
                private static final PtolemyWebSocketClientFactory INSTANCE = 
                        new PtolemyWebSocketClientFactory();
        }
        
        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 //// 
        
        /** A table of paths and shared clients.  Actors can choose to share
         * a client connection (for example, to read from and write to the 
         * same socket of a remote service) or to have a unique client
         * (giving a unique connection to a remote web service).  
         * A third possibility would be "groups" that share a client.  This is 
         * not implemented.
         */
        private Hashtable<URI, WebSocketClient> _sharedClients;
        
        /** A factory for creating WebSocketClients. */
        private static WebSocketClientFactory _webSocketClientFactory;
}
