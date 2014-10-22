/* A singleton factory class that creates and tracks websocket connections.

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

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// WebSocketConnectionFactory

/** A singleton factory class that creates and tracks websocket connections.
 *
 *  @author Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
*/

public class WebSocketConnectionManager {

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Get the factory instance, creating a new one if none created yet.
         * @return The websocket client factory instance.
         */
        public static WebSocketConnectionManager getInstance() {
                return WebSocketConnectionFactoryHolder.INSTANCE;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /** Create a new WebSocketConnectionFactory.  The private
         *  constructor prevents instantiation from other classes.
         */
        private WebSocketConnectionManager() {
            _createdEndpoints = new Hashtable();
            _futureConnections = new Hashtable();
            _futureServices = new Hashtable();
            _openConnections = new Hashtable();
            _openServices = new Hashtable();
            _tableLock = new Object();

            _webSocketClientFactory = new WebSocketClientFactory();
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
        private static class WebSocketConnectionFactoryHolder {
                private static final WebSocketConnectionManager INSTANCE =
                        new WebSocketConnectionManager();
        }

        /** Return a new, unshared connection.
         * @param path The URL to connect to.
         * @param service The WebSocketService requesting the connection.
         */
        public void newConnection(URI path, WebSocketService service)
            throws IllegalActionException {

            // Based on http://download.eclipse.org/jetty/stable-8/apidocs/org/eclipse/jetty/websocket/WebSocketClient.html
            // and http://stackoverflow.com/questions/19770278/jetty-8-1-1-websocket-client-handshake

            try {
                WebSocketEndpoint endpoint = new WebSocketEndpoint(service);
                Future<Connection> connectionFuture =
                            _webSocketClient.open(path, endpoint);

                // Create and start new runnable to obtain result of Future
                Thread thread = new Thread(new RunnableConnectionCreator(
                        connectionFuture, service));
                thread.start();
            } catch (IOException e) {
                throw new IllegalActionException("Cannot open "
                        + "websocket to path " + path);
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Set a connection if one has already been established for the
         *  given path; return null otherwise.  Parts are synchronized so that
         *  only one object may request or release a connection at a time.
         *  This method does NOT block waiting for a connection.
         * @param path  The path to which the connection is established.
         * @return A connection if one has already been established for the
         *  given path; null otherwise.
         *  @see #releaseConnection(WebSocketService)
         */
        public void requestConnection(URI path,
                WebSocketService service)
            throws IllegalActionException {
            if (_openConnections.containsKey(path)) {
                _openServices.get(path).add(service);
                service.setConnection(_openConnections.get(path));

                // TODO:  Check the futures table and clear out??
            } else {
                if (_webSocketClient == null) {
                    throw new IllegalActionException("Websocket client"
                            + " factory could not be started.");
                } else {

                    // Check if another object has initiated a pending
                    // connection request
                    if (_createdEndpoints.containsKey(path)) {
                        Thread thread = new Thread(new
                                RunnableConnectionAdder(service,
                                        path));
                             thread.start();

                    } else {
                        // Should be OK to add new entries unsynchronized.
                        // Nothing is going to remove them.
                        // TODO:  OR, put this all in one thread?
                        /// The waiting on the connection future is the part
                        // that causes the delay that we don't want to block for.
                        WebSocketEndpoint endpoint =
                                new WebSocketEndpoint(service);
                        _createdEndpoints.put(path, endpoint);
                        _futureServices.put(path, new HashSet());

                        try {
                            Future<Connection> connectionFuture =
                                _webSocketClient.open(path, endpoint);
                            Thread thread = new Thread(new
                               RunnableConnectionGetter(connectionFuture,
                                       service, path));
                            thread.start();
                        } catch (IOException e) {
                            throw new IllegalActionException("Cannot open "
                                    + "websocket to path " + path);
                        }
                    }
                }
            }
        }

        /** Release access to a websocket connection.
         *
         * @param path The path of the service.
         * @param service Theservice requesting release.
         */
        public void releaseConnection(URI path,
                WebSocketService service) {

            Thread thread = new Thread(new RunnableConnectionReleaser(service,
                            path));
                 thread.start();

        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        private Hashtable<URI, WebSocketEndpoint> _createdEndpoints;

        /** A table of all of the connection futures.  Used to keep track of
         * pending connections so that only the first incoming shared
         * connection request will create a new connection; others will block.
         */
        private Hashtable<URI, Future<Connection>> _futureConnections;

        // TODO:  Comment
        private Hashtable<URI, HashSet<WebSocketService>> _futureServices;

        /** The timeout for establishing a connection.  In milliseconds. */
        private int _connectionTimeout = 5000;

        /** A lock for accessing the tables storing info about connections. */
        private Object _tableLock;

        /** A table of paths and shared clients.  Actors can choose to share
         * a client connection (for example, to read from and write to the
         * same socket of a remote service) or to have a unique client
         * (giving a unique connection to a remote web service).
         * A third possibility would be "groups" that share a client.  This is
         * not implemented.
         */
        private Hashtable<URI, Connection> _openConnections;

        // TODO:  Comment
        private Hashtable<URI,
            HashSet<WebSocketService>> _openServices;

        /** A websocket client for creating connections.  */
        private WebSocketClient _webSocketClient;

        /** A factory for creating WebSocketClients. */
        private static WebSocketClientFactory _webSocketClientFactory;

        /** A Runnable class to obtain a new, unshared connection.
         */
        private class RunnableConnectionCreator implements Runnable {

            public RunnableConnectionCreator(Future<Connection>
                    connectionFuture, WebSocketService service) {
                _connectionFuture = connectionFuture;
                _service = service;
            }

            /** Obtain a Connection object. */
            @Override
            public void run() {
                Connection connection;

                try {
                    connection = _connectionFuture
                            .get(_connectionTimeout, TimeUnit.MILLISECONDS);

                    if (connection != null) {
                        _service.setConnection(connection);
                    }
                } catch (Exception e) {
                    // TODO:  Need an onError() method for
                    // WebSocketService
                    _connectionException = e;
                }
            }

            // TODO:  Error notification
            /* Some errors:
             *             } catch (TimeoutException e) {
                throw new IllegalActionException(this, "Timeout establishing"
                        + " WebSocket connection");
            } catch (ExecutionException e) {
                throw new IllegalActionException(this, "Can't establish "
                        + "WebSocket connection");
            } catch (InterruptedException e) {
                throw new IllegalActionException(this, "WebSocket connection "
                        + "establishment interrupted");
            }
             */

            /** The exception, if any, raised from obtaining a WebSocket
             * connection.
             */
            private Exception _connectionException;

            /** The future object that will provide a connection. */
            private Future<Connection> _connectionFuture;

            /** The websocket service that wants a connection. */
            private WebSocketService _service;
        }

        /** A Runnable class to obtain a Connection object in a separate thread,
         *  so as not to block the main thread.
         */
        private class RunnableConnectionGetter implements Runnable {

            public RunnableConnectionGetter(Future<Connection> connectionFuture,
                    WebSocketService service, URI path) {
                _connectionFuture = connectionFuture;
                _path = path;
                _service = service;
            }

            /** Obtain a Connection object. */
            @Override
            public void run() {
                Connection connection;

                synchronized(_tableLock) {
                    try {
                        connection = _connectionFuture
                                .get(_connectionTimeout, TimeUnit.MILLISECONDS);

                        if (connection != null) {
                            _openConnections.put(_path, connection);
                            if (_openServices.get(_path).isEmpty()) {
                                _openServices.put(_path, new HashSet());
                            }

                            // Add the original service to the list of
                            // services waiting for a connection
                            _futureServices.get(_path).add(_service);

                            // Set connection for all services waiting on this
                            // future.
                            for (WebSocketService service :
                                _futureServices.get(_path)) {
                                service.setConnection(connection);
                                _openServices.get(_path).add(service);
                            }

                            _futureServices.remove(_path);
                        }
                    } catch (Exception e) {
                        // TODO:  Need an onError() method for
                        // WebSocketService
                        _connectionException = e;
                    }
                }
            }

            // Another synchronized method to wait on future if other thread
            // is waiting for the same one??

            /** The exception, if any, raised from obtaining a WebSocket
             * connection.
             */
            private Exception _connectionException;

            /** The future object that will provide a connection. */
            private Future<Connection> _connectionFuture;

            /** The path associated with the connection. */
            private URI _path;

            /** The websocket service that wants a connection. */
            private WebSocketService _service;
        }

        /** A Runnable class to request a Connection object that has already
         * been initiated.
         */
        private class RunnableConnectionAdder implements Runnable {
            public RunnableConnectionAdder(WebSocketService service,
                    URI path) {
                _path = path;
                _service = service;
            }

            /** Request a Connection object that has already been initiated.
             */
            @Override
            public void run() {
                synchronized(_tableLock) {
                    _createdEndpoints.get(_path).addParentService(_service);
                    _futureServices.get(_path).add(_service);
                }
            }

            /** The path associated with the connection. */
            private URI _path;

            /** The websocket service that wants a connection. */
            private WebSocketService _service;
        }

        /** A Runnable class to release access to a connection.
         */
        private class RunnableConnectionReleaser implements Runnable {
            public RunnableConnectionReleaser(WebSocketService service,
                    URI path) {
                _path = path;
                _service = service;
            }

            /** Request a Connection object that has already been initiated.
             */
            @Override
            public void run() {
                synchronized(_tableLock) {
                    // Remove service from tables
                    if (_futureServices.containsKey(_path)) {
                        _futureServices.get(_path).remove(_service);

                        // Remove paths that no longer have services
                        if (_futureServices.get(_path).isEmpty()) {
                            _futureServices.remove(_path);
                            _futureConnections.get(_path).cancel(true);
                        }
                    }

                    if (_openServices.containsKey(_path)) {
                        _openServices.get(_path).remove(_service);

                        // Remove paths that no longer have services
                        if (_openServices.get(_path).isEmpty()) {
                            _openServices.remove(_path);
                            _openConnections.get(_path).close();
                            _openConnections.remove(_path);
                            _createdEndpoints.remove(_path);
                        }
                    }
                }
            }

            /** The path associated with the connection. */
            private URI _path;

            /** The websocket service that wants a connection. */
            private WebSocketService _service;
        }
}
