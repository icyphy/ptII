/* A manager for creating and managing instances of a Jetty web server.

 Copyright (c) 2013-2014 The Regents of the University of California.
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
package org.ptolemy.ptango.lib.http;

import java.util.HashSet;

///////////////////////////////////////////////////////////////////
////WebServerManager

/** A manager for creating and managing instances of a Jetty web server.
 *  Models with the {@link org.ptolemy.ptango.lib.WebServer} attribute use this
 *  manager to register, start, and stop web applications while checking for
 *  conflicts with other web applications.  This manager creates and starts up
 *  new web servers, one server per unique port, and shuts down web servers when
 *  no more models are using them.
 *
 *   @author Elizabeth Latronico and Edward A. Lee
 *   @version $Id$
 *   @since Ptolemy II 10.0
 *   @Pt.ProposedRating Red (ltrnc)
 *   @Pt.AcceptedRating Red (ltrnc)
 */

// Declare class as final to disallow subclassing.  If subclasses are needed
// in the future, then the private constructor will need to be changed to a
// protected implementation.
// http://www.javaworld.com/javaworld/jw-04-2003/jw-0425-designpatterns.html
public final class WebServerManager {

    // Note:  The constructor is private.  See private methods section.

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return the existing web server manager; create and return a new one if 
     * none is present yet.
     * 
     * @return The web server manager 
     */
    public static WebServerManager getInstance() {
        if (_instance == null) {
            _instance = new WebServerManager();
        }
        return _instance;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the given application is registered with the
     * server associated with the given port; false otherwise. The model name
     * is used to identify the application - only one instance of a model may
     * be registered to a server.  (Note that multiple instances of a model are
     * possible by different servers on different ports.)
     *
     * @param fullModelName The full name of the model
     * @param portNumber The port this application would be hosted on
     * @return true if the given application is registered with the
     * server associated with the given port; false otherwise.
     */
    public boolean isRegistered(String fullModelName, int portNumber) {
        // Check if a server is associated with this port
        WebServerUtilities server = null;

        for (WebServerUtilities theServer : _servers) {
            if (theServer.getPortNumber() == portNumber) {
                server = theServer;
                break;
            }
        }

        if (server != null) {
            // FIXME:  Use just model name, or whole application info object?
            if (server.isHostingModel(fullModelName)) {
                return true;
            }
        }
        return false;
    }

    /** Return true if the given application is registered with the
     * server associated with the given port and this server is running;
     * false otherwise.  The model name is used to identify the application -
     * only one instance of a model may be registered to a server.  (Note that
     * multiple instances of a model are possible by different servers on
     * different ports.)
     *
     * @param fullModelName The full name of the model
     * @param portNumber The port this application would be hosted on
     * @return true if the given application is registered with the
     * server associated with the given port and this server is running;
     * false otherwise.
     */
    public boolean isRunning(String fullModelName, int portNumber) {
        // Check if a server is associated with this port
        WebServerUtilities server = null;

        for (WebServerUtilities theServer : _servers) {
            if (theServer.getPortNumber() == portNumber) {
                server = theServer;
                break;
            }
        }

        if (server != null) {
            // FIXME:  Use just model name, or whole application info object?
            if (server.isHostingModel(fullModelName) && server.isRunning()) {
                return true;
            }
        }

        return false;
    }

    /** Register the following application with the manager.  Check if the
     * model in appInfo is already associated with a server.  If not, assign
     * the web application to the server, checking for conflicts with
     * existing applications, creating new handlers where appropriate and
     * pausing and restarting the server if necessary.  Returns the port number if the
     * application was successfully registered, and -1 otherwise.
     *
     * @param appInfo Information about the registered web application
     * @param portNumber The desired port number to host this web application on
     * @return The actual port number the web application is running on (may be 
     * different from the portNumber parameter if dynamic selection is enabled)
     * @exception Exception thrown if web server cannot be instantiated or if
     * application conflicts with an already-registered application
     */

    public int register(WebApplicationInfo appInfo, int portNumber,
            boolean dynamicPortSelection)
            throws Exception {
        // Fetch server at this port, if any.  If none, create and start a new
        // server for this port.  If the port number is <=0, WebServerUtilities
        // uses a default port
        WebServerUtilities server = null;

        // If dynamic port selection is enabled, first try to reuse any server 
        // currently hosting models for this Ptolemy instance.
        // TODO:  Server sharing could be prohibited in the future if desired
        // for security reasons, depending on the application
        // Otherwise, retrieve any server at the specified port
        // Create a new server if no suitable server is found
      
        if (dynamicPortSelection) {
            if (!_servers.isEmpty()) {
                for (WebServerUtilities reuseServer : _servers) {
                    server = reuseServer;
                    break;
                }
            }
            
            if (server == null) {
                server = new WebServerUtilities();
                server.setDynamicPortSelection(dynamicPortSelection);
            }
        } else {
            for (WebServerUtilities theServer : _servers) {
                if (theServer.getPortNumber() == portNumber) {
                    server = theServer;
                    break;
                }
            }
            
            if (server == null) {
                server = new WebServerUtilities(portNumber);
                server.setDynamicPortSelection(dynamicPortSelection);
            }
        }

        // Register this application.  This will check for URL conflicts,
        // create handlers for this app (context and resource), and start the
        // server if not already started
        server.register(appInfo);

        // Update information about this server in the server table
        _servers.add(server);
        
        return server.getPortNumber();
    }

    /** Unregister the following application with the manager.  Remove all
     * context handlers specific to this application and stop the web server
     * if this is the only application running on it.  Providing a port number
     * allows other instances of the application on other ports to continue
     * running.
     *
     * @param appInfo Information about the unregistered web application
     * @param portNumber  The port number that this application is running on
     * @exception Exception if there is no server associated with this port,
     * the application is not running on the server associated with this port,
     * or the application cannot be stopped.
     */
    public void unregister(WebApplicationInfo appInfo, int portNumber)
            throws Exception {
        // Check if this application has been registered to a server.
        // First, get the server running on this port.
        WebServerUtilities server = null;
        for (WebServerUtilities theServer : _servers) {
            if (theServer.getPortNumber() == portNumber) {
                server = theServer;
                break;
            }
        }

        if (server == null) {
            throw new Exception("Application " + appInfo.getModelName()
                    + " attempted to unregister itself for port number "
                    + portNumber
                    + ", but there is no server associated with this port.");
        }

        // This will throw an exception if the application is not registered on
        // this server
        server.unregister(appInfo);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // WebServerManager is a singleton class; therefore, make the constructor
    // private so other objects cannot directly construct a new one
    // http://www.javaworld.com/javaworld/jw-04-2003/jw-0425-designpatterns.html
    private WebServerManager() {

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The singleton instance of the WebServerManager class */
    private static WebServerManager _instance = null;

    private HashSet<WebServerUtilities> _servers = 
            new HashSet<WebServerUtilities>();
}
