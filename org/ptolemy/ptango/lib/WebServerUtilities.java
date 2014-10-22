/* A web server and information about the applications registered to it.

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

package org.ptolemy.ptango.lib;

import java.net.BindException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.ptolemy.ptango.lib.websocket.PtolemyWebSocketServlet;

///////////////////////////////////////////////////////////////////
////WebServerUtilities

/** A web server and information about the applications registered to it.  Used
 * by {@link org.ptolemy.ptango.lib.WebServerManager}
 *
 *   @author Elizabeth Latronico and Edward A. Lee
 *   @version $Id$
 *   @since Ptolemy II 10.0
 *   @Pt.ProposedRating Red (ltrnc)
 *   @Pt.AcceptedRating Red (ltrnc)
 */

public class WebServerUtilities {

    /** Construct a new instance of this class with the default port number
     * and maximum idle time.
     */
    public WebServerUtilities() {

        _applications = new HashSet<WebApplicationInfo>();
        _dynamicPortSelection = false;
        _portNumber = DEFAULT_PORT_NUMBER;
        _maxIdleTime = DEFAULT_MAX_IDLE_TIME;

        _server = null;
        _selectChannelConnector = null;
    }

    /** Construct a new instance of this class with the specified port number.
     *
     * @param portNumber The port number the web server receives requests on
     */
    public WebServerUtilities(int portNumber) {

        _applications = new HashSet<WebApplicationInfo>();
        _dynamicPortSelection = false;
        _maxIdleTime = 30000;

        // If port number is <= 0 or maximum idle time is <= 0 use defaults
        if (portNumber <= 0) {
            _portNumber = DEFAULT_PORT_NUMBER;
        } else {
            _portNumber = portNumber;
        }

        _server = null;
        _selectChannelConnector = null;
    }

    /** Construct a new instance of this class with the specified port number
     * and maximum idle time.
     *
     * @param portNumber The port number the web server receives requests on
     * @param maxIdleTime The maximum amount of time the web server will wait
     * before sending a timeout response page
     */
    public WebServerUtilities(int portNumber, int maxIdleTime) {

        _applications = new HashSet<WebApplicationInfo>();
        _dynamicPortSelection = false;

        // If port number is <= 0 or maximum idle time is <= 0 use defaults
        if (portNumber <= 0) {
            _portNumber = DEFAULT_PORT_NUMBER;
        } else {
            _portNumber = portNumber;
        }

        if (maxIdleTime <= 0) {
            _maxIdleTime = DEFAULT_MAX_IDLE_TIME;
        } else {
            _maxIdleTime = maxIdleTime;
        }

        _server = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if dynamic port selection is permitted; false otherwise.
     *
     * @return True if dynamic port selection is permitted; false otherwise.
     * @see #setDynamicPortSelection(boolean)
     */
    public boolean getDynamicPortSelection() {
        return _dynamicPortSelection;
    }

    /** Return the maximum amount of time the server will wait before returning
     * a timeout response page.
     *
     * @return The maximum amount of time the server will wait before returning
     * a timeout response page.
     * @see #setMaxIdleTime(int)
     */
    public int getMaxIdleTime() {
        return _maxIdleTime;
    }

    /** Return the port number that the server listens to for requests.
     * Note that the port number is only settable in the constructor - there
     * is no setPortNumber() method - since web applications ask for a specific
     * port number and changing the port number will cause client requests
     * to the old port number to fail.
     *
     * @return The port number that the server listens to for requests.
     */
    public int getPortNumber() {
        return _portNumber;
    }

    /** Return true if this web server is hosting the given model; false
     * otherwise.  The full model name should be provided.
     *
     * @param fullModelName  The full name of the model to search for
     * @return true if the web server is hosting the given model; false
     * otherwise
     */
    public boolean isHostingModel(String fullModelName) {
        for (WebApplicationInfo app : _applications) {
            if (fullModelName != null
                    && app.getModelName().equalsIgnoreCase(fullModelName)) {
                return true;
            }
        }
        return false;
    }

    /** Return true if the web server is running; false otherwise.
     *
     * @return true if the web server is running; false otherwise */
    public boolean isRunning() {
        if (_server != null) {
            return _server.isRunning();
        } else {
            return false;
        }
    }

    /** Register the following application on this server.  Start the server
     * if it has not already been started.  Throw an exception if the new
     * application conflicts with existing applications, for example, by
     * requesting the same URL mapping.
     *
     * @param appInfo  The application to add to this server
     * @exception Exception   If the new application conflicts with existing
     * applications, for example, by requesting the same URL mapping.
     */
    public void register(WebApplicationInfo appInfo) throws Exception {

        // FIXME:  Require the application paths to be distinct?  Distinct
        // application paths are not strictly necessary, but matching
        // application paths could be confusing since it's unclear which
        // Ptolemy model is serving the response
        // If applicationPath is just "/", don't add this as a prefix to
        // the servlet path, since that would create a path like "//myApp"
        // instead of "/myApp"
        String applicationPath = appInfo.getApplicationPath().toString();
        if (applicationPath.equals("/")) {
            applicationPath = "";
        }

        // Ensure that new servlet mappings are distinct from existing
        // servlet and resource handler mappings.  If not, throw exception
        for (URI servletPath : appInfo.getServletInfo().keySet()) {
            for (WebApplicationInfo application : _applications) {
                if (application.hasPath(applicationPath
                        + servletPath.toString())) {
                    throw new Exception("Model " + appInfo.getModelName()
                            + " requested a conflicting URL mapping, "
                            + applicationPath + servletPath.toString());
                }
            }
        }

        // If no conflicts, create a handler for the new application
        // If this is the first application, need to create a Jetty server and
        // set the server's properties
        if (_applications.isEmpty()) {
            startServer();
        }

        // Add this application to the list of registered applications
        _applications.add(appInfo);

        // Create handlers for standard HTTP and WebSocket requests
        _createServletHandler(appInfo);

        // Create / re-use handlers for resource requests
        // Check if this web app wants to re-use the existing resource handler,
        // or if a new one is needed
        _createResourceHandlers(appInfo);
    }

    /** Set a flag indicating if dynamic port selection is permitted.
     *
     * @param dynamicPortSelection True if dynamic port selection is permitted;
     * false otherwise
     * @see #getDynamicPortSelection()
     */
    public void setDynamicPortSelection(boolean dynamicPortSelection) {
        _dynamicPortSelection = dynamicPortSelection;
    }

    /** Set the maximum amount of time, in milliseconds, that the server will
     * wait before returning a timeout response page.
     *
     * @param maxIdleTime The maximum amount of time, in milliseconds, that the
     * server will wait before returning a timeout response page
     * @see #getMaxIdleTime()
     */
    public void setMaxIdleTime(int maxIdleTime) {
        // If the specified max idle time is <=0, use the default
        if (maxIdleTime <= 0) {
            _maxIdleTime = DEFAULT_MAX_IDLE_TIME;
        } else {
            _maxIdleTime = maxIdleTime;
        }

        // If there is a server instantiated, change the server's max idle time
        if (_server != null) {
            // Get the server's SelectChannelConnector to change the idle time
            // There should be only one connector, since the current
            // implementation runs one server per port, so that web applications
            // can be developed and tested independently.  The idea is that
            // each developer could be assigned a separate port for his or
            // her applications.  If there were a single server listening to
            // multiple ports, then all web applications would listen to all
            // ports, meaning web applications from other developers could
            // intercept requests.

            // FIXME:  Need to pause the server if it is running, or OK to
            // do this while running?
            _selectChannelConnector.setMaxIdleTime(_maxIdleTime);

        }
    }

    /** Unregister the given application from this server.  If this leaves no
     * applications on the server, stop the server.
     *
     * @param appInfo  The application to remove from the server.
     * @exception Exception if the application is not registered with this
     * server or if the server cannot be stopped when the last application
     * is unregistered
     */
    public void unregister(WebApplicationInfo appInfo) throws Exception {

        // Check if this application is registered on this server.  The model
        // name is used as a key - there should be only one instance of a
        // model registered on any server at one time.  This restriction could
        // be removed in the future if necessary.  But, running two instances
        // of the same model simultaneously is confusing and probably
        // unintentional.
        boolean found = false;

        if (_applications.contains(appInfo)) {
            // Find the handler associated with this application, stop it and
            // remove it from the server
            ContextHandlerCollection handlers = (ContextHandlerCollection) _server
                    .getHandler();
            for (int i = 0; i < handlers.getHandlers().length; i++) {
                Handler handler = handlers.getHandlers()[i];

                // Only need to check servlet handlers (e.g. not the
                // DefaultHandler, not resource handlers...)
                // Check for matching application path

                // TODO:  Update for WebContextHandler
                if (handler instanceof ServletContextHandler
                        && ((ServletContextHandler) handler)
                                .getContextPath()
                                .equalsIgnoreCase(
                                        appInfo.getApplicationPath().toString())) {

                    // Check for matching HTTP request and WebSocket servlets
                    Set<URI> keySet =
                            new HashSet(appInfo.getServletInfo().keySet());
                    keySet.addAll(appInfo.getWebSocketInfo().keySet());

                    for (URI servletPath : keySet) {
                        ServletHandler servletHandler =
                                ((ServletContextHandler) handler)
                                .getServletHandler();
                        for (ServletMapping mapping : servletHandler
                                .getServletMappings()) {

                            // Any matching path means this is the servlet we
                            // want to stop and remove
                            for (String path : mapping.getPathSpecs()) {
                                if (path.equalsIgnoreCase(servletPath
                                        .toString())) {
                                    handler.stop();
                                    handlers.removeHandler(handler);
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Throw an exception if this application was not found
            if (!found) {
                throw new Exception(
                        "Application "
                                + appInfo.getModelName()
                                + " requested to be unregistered, but it was not found "
                                + " on the server.");
            }

            // What to do about the resource handler?  This might be shared
            // by other applications.  Need to keep a list.
            // FIXME:  For now, just leave it running.
            _applications.remove(appInfo);

        } else {
            throw new Exception(
                    "Application "
                            + appInfo.getModelName()
                            + " attempted to unregister itself, but this application is "
                            + " not registered with the server on port "
                            + _portNumber);
        }

        // If no applications are left, stop the server
        if (_applications.isEmpty()) {
            // Explicitly close the port connection so that the port will be
            // available soon.  Otherwise, it was taking 30 seconds plus for
            // the port to be relinquished, blocking any new web servers
            _selectChannelConnector.close();
            _serverThread.interrupt();
            _serverThread = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Use 8078 as the default port number that the server listens to for
     * incoming requests.
     */
    static final int DEFAULT_PORT_NUMBER = 8078;

    /** Use 30 seconds (30000 milliseconds) as the default time the server
     * will wait before returning a timeout response page.
     */
    static final int DEFAULT_MAX_IDLE_TIME = 30000;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a ContextHandler to store all of the servlets defined in the
     *  given application (e.g. a Ptolemy model).  This includes servlets for
     *  both standard HTTP requests and WebSockets.  Add this handler to the
     *  collection of handlers for this web server.
     *
     *  @param appInfo Information about the web application.
     *  @exception Exception If the servlet cannot be started on the server
     */
    protected void _createServletHandler(WebApplicationInfo appInfo)
            throws Exception {
        // Create a new handler to hold servlets from the actors
        ServletContextHandler servletHandler = new ServletContextHandler(
                ServletContextHandler.SESSIONS);

        servletHandler.setContextPath(appInfo.getApplicationPath().toString());

        // Add handlers for standard HTTP services
        for (URI path : appInfo.getServletInfo().keySet()) {
            servletHandler
                    .addServlet(
                            new ServletHolder(appInfo.getServletInfo().get(
                                    path)), path.toString());
        }

        // Add handlers for WebSockets
        // FIXME:  In app info, keep track of servlets instead of actors?
        // Since WebSocket knows web services it is affiliated with?
        // TODO:  Open all of these connections here for local readers/writers
        // Remote readers/writers are expected to open their own connection
        // TODO:  Determine if connection is local of remote based on URL
        // Need to pass the client to reader and writer??

        for (URI path : appInfo.getWebSocketInfo().keySet()) {
            PtolemyWebSocketServlet servlet = new PtolemyWebSocketServlet();
            servletHandler.addServlet(new ServletHolder(
                    servlet), path.toString());
        }

        ((ContextHandlerCollection) _server.getHandler())
                .addHandler(servletHandler);

        // Need to explicitly start the handler since it is added to the
        // server's handler list AFTER the server has already started
        servletHandler.start();
    }

    /** Create or re-use resource handler(s) to serve files such as images,
     *  audio, etc.  If the application requests a handler for a path that is
     *  already used by an existing resource handler, then the existing handler
     *  will be re-used, and the new search locations will be added to its
     *  search list.  If a new path is provided, a new resource handler will be
     *  created, as long as at least one search location is given.  New
     *  handlers are added to the _handlers list.
     *
     *  @param appInfo An object containing information needed to register the
     *   new application
     *  @exception Exception If a FileParameter is found that is not a valid URI or
     *  references a resource that cannot be found, or if the handler cannot
     *  be started on the server
     */

    // FIXME:  Throw exception instead of allowing an empty list?  An
    // application is not required to have a resource handler, so an empty
    // list is permitted, but how to notify user in case the model has a
    // mistake e.g. the user forgot to add resource locations?

    protected void _createResourceHandlers(WebApplicationInfo appInfo)
            throws Exception {

        // Create resource handlers to serve files such as images, audio, ...
        // See http://restlet-discuss.1400322.n2.nabble.com/Jetty-Webapp-td7313234.html
        // There will be one resource handler per requested path
        ArrayList<ContextHandler> handlers = new ArrayList<ContextHandler>();

        for (URI path : appInfo.getResourceInfo().keySet()) {
            boolean shareHandler = false;

            for (WebApplicationInfo application : _applications) {
                if (application.hasResourcePath(path.toString())) {
                    // Share a handler.  Find the handler and add any new
                    // resource locations.
                    for (int i = 0; i < ((ContextHandlerCollection) _server
                            .getHandler()).getHandlers().length; i++) {
                        ContextHandler handler = (ContextHandler) ((ContextHandlerCollection) _server
                                .getHandler()).getHandlers()[i];

                        if (handler.getContextPath().equalsIgnoreCase(
                                path.toString())) {

                            // Stop this resource handler
                            handler.stop();

                            // Add the new resource locations (if any)
                            // FIXME:  Check if this cast is OK.  Otherwise
                            // will have to manually remember the resources
                            ResourceCollection resources = (ResourceCollection)
                                    handler.getBaseResource();

                            for (Resource resource : appInfo
                                    .getResourceInfo().get(path)) {
                                if (!resource.isContainedIn(resources)) {
                                    // Jetty doesn't seem to offer a method
                                    // to add a resource to a
                                    // ResourceCollection?
                                    // Has addPath(String) but this returns
                                    // a Resource, but not sure if it
                                    // creates one...
                                    ArrayList<Resource> newResources = new ArrayList<Resource>(
                                            Arrays.asList(resources
                                                    .getResources()));
                                    newResources.add(resource);
                                    resources
                                            .setResources(newResources
                                                    .toArray(new Resource[newResources
                                                            .size()]));

                                }
                            }

                            // Add the temporary file location (if any), if
                            // not already included

                            if (appInfo.getTemporaryFileLocation() != null
                                    && !appInfo.getTemporaryFileLocation()
                                            .toString().isEmpty()) {
                                FileResource tempResource = new FileResource(
                                        appInfo.getTemporaryFileLocation()
                                                .asURL());
                                if (!tempResource.isContainedIn(resources)) {
                                    ArrayList<Resource> newResources = new ArrayList<Resource>(
                                            Arrays.asList(resources
                                                    .getResources()));
                                    newResources.add(tempResource);
                                    resources
                                            .setResources(newResources
                                                    .toArray(new Resource[newResources
                                                            .size()]));
                                }
                            }

                            // Restart the handler
                            handler.start();

                            shareHandler = true;
                            break;
                        }
                    }
                }

                // Create a new handler if this is a new resource path
                if (!shareHandler) {
                    ContextHandler fileHandler = new ContextHandler();

                    // Set the path used by other web apps to request files
                    // Example:
                    // $PTII/org/ptolemy/ptango/demo/WebServerDE/WebServerDE.xml
                    fileHandler.setContextPath(path.toString());

                    // Enable aliases so that we can use $TMPDIR under Mac OS X
                    // because $TMPDIR is in /var, which is a symbolic link.
                    // FIXME: this opens up a series of security holes.
                    fileHandler.setAliases(true);

                    // Create a new resource handler
                    ResourceHandler resourceHandler = new ResourceHandler();
                    // For security, do not support listing of directories in the local
                    // resource locations.
                    resourceHandler.setDirectoriesListed(false);

                    // Tell handler to search requested locations and temporary file
                    // location (if any) for files
                    ArrayList<Resource> resources = new ArrayList<Resource>();
                    resources.addAll(appInfo.getResourceInfo().get(path));

                    if (appInfo.getTemporaryFileLocation() != null
                            && (appInfo.getTemporaryFileLocation()
                                    .getExpression() != null)
                            && (!appInfo.getTemporaryFileLocation()
                                    .getExpression().isEmpty())) {

                        try {
                            resources.add(new FileResource(appInfo
                                    .getTemporaryFileLocation().asURL()));
                        } catch (MalformedURLException e) {
                            throw new Exception("Temporary file location "
                                    + appInfo.getTemporaryFileLocation()
                                    + " cannot be accessed.");
                        } catch (URISyntaxException e2) {
                            throw new Exception("Temporary file location "
                                    + appInfo.getTemporaryFileLocation()
                                    + " cannot be accessed.");
                        }
                    }

                    // Ensure that at least one resource location has been
                    // specified. Otherwise, don't add new handler to collection

                    if (resources.size() > 0) {
                        // Use setBaseResource(ResourceCollection) instead of
                        // setResouceBase(String) so that we may add multiple
                        // file locations.
                        // setResourceBase(String) is a wrapper for
                        // setBaseResource(ResourceCollection) that only allows
                        // one location.

                        ResourceCollection collection
                            = new ResourceCollection();
                        collection.setResources(resources
                                .toArray(new Resource[resources.size()]));

                        resourceHandler.setBaseResource(collection);

                        fileHandler.setHandler(resourceHandler);
                        handlers.add(fileHandler);
                    }
                }
            }
        }

        for (ContextHandler handler : handlers) {
            ((ContextHandlerCollection) _server.getHandler())
                    .addHandler(handler);

            // Need to explicitly start each new handler since they are added to
            // the server's handler list AFTER the server has already started
            handler.start();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void startServer() throws Exception {
        _server = new Server();
        _selectChannelConnector = new SelectChannelConnector();
        _selectChannelConnector.setPort(_portNumber);
        _selectChannelConnector.setMaxIdleTime(_maxIdleTime);

        // Don't allow other programs to use this port (e.g. another
        // Jetty instance)
        // FIXME:  Need to catch exception
        _selectChannelConnector.setReuseAddress(false);
        _server.setConnectors(new Connector[] { _selectChannelConnector });

        // Create a ContextHandlerCollection containing only a
        // DefaultHandler.  Other handlers will be added later for web apps
        // See "Configuring the Server - Handlers"
        // http://www.eclipse.org/jetty/documentation/current/quickstart-config-what.html

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.addHandler(new DefaultHandler());
        _server.setHandler(handlers);

        // Start the server in a new thread. Use a custom uncaught exception
        // handler so the server thread can log information about exceptions
        // to the _exceptionMessage variable that the main thread can access
        _startAttempted = false;
        _exception = null;
        _serverThread = new Thread(new RunnableServer());
        _serverThread.start();

        // Wait until the server has attempted to start
        // The server thread will set _startAttempted to true
        // Then, see if an exception has occurred
        synchronized (_lock) {
            while (!_startAttempted) {
                try {
                    _lock.wait();
                } catch (InterruptedException e) {
                    // FIXME: Do anything special if thread is interrupted?
                    break;
                }
            }
        }

        boolean success = false;

        // If an exception occurred, re-throw it
        // One alternative considered was to use a Callable instead of a
        // a Runnable, since a Callable can throw an exception
        // However, the calling thread gets the exception back by calling
        // Future.get(), which blocks the calling thread
        // Since in normal operation the server thread runs indefinitely,
        // Future.get() would block Ptolemy model execution indefinitely
        if (_exception != null) {
            if (_exception instanceof BindException) {
                if (_dynamicPortSelection) {
                    _portNumber = _initialDynamicPortNumber;

                    while (_portNumber <= _maxDynamicPortNumber) {
                        _startAttempted = false;
                        _exception = null;
                        _serverThread = new Thread(new RunnableServer());

                        _selectChannelConnector.setPort(_portNumber);
                        _serverThread.start();

                        // Wait until the server has attempted to start
                        // The server thread will set _startAttempted to true
                        // Then, see if an exception has occurred
                        synchronized (_lock) {
                            while (!_startAttempted) {
                                try {
                                    _lock.wait();
                                } catch (InterruptedException e) {
                                    // FIXME: Do anything special if thread is interrupted?
                                    break;
                                }
                            }
                        }

                        if (_exception != null) {
                            if (_exception instanceof BindException) {
                                _portNumber++;
                            } else {
                                throw new Exception(_exception);
                            }
                        } else {
                            // No exception means server started successfully
                            success = true;
                            break;
                        }
                    }

                    // Ran out of port numbers to try
                    if (!success) {
                        throw new Exception("The web server could not find an "
                                + "available port between "
                                + _initialDynamicPortNumber + " and "
                                + _maxDynamicPortNumber);
                    }
                } else {
                throw new Exception("The web server attempted to start on"
                        + " port " + _portNumber + ", but this port is "
                        + "already in use.  Perhaps another instance of "
                        + "Ptolemy is running a web server on this port?");
                }
            } else {
                throw new Exception(_exception);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The set of applications running on this web server. */
    private HashSet<WebApplicationInfo> _applications;

    /** A flag indicating if dynamic port selection is allowed. */
    private boolean _dynamicPortSelection;

    /** An exception thrown (if any) when the server is started.  The main
     * thread will check if this exception has been set by the server thread. */
    private Exception _exception;

    /** The initial port number to try, under dynamic port selection.
     *  Note that the port specified in the constructor will be tried first. */
    private int _initialDynamicPortNumber = 8001;

    /** A lock to synchronize the main and server threads when starting the
     * server.
     */
    private Object _lock = new Object();

    /** The maximum port number to try, under dynamic port selection.  A
     * maximum is specified so the server won't try indefinitely. */
    private int _maxDynamicPortNumber = 8999;

    /** The maximum idle time for a connection, in milliseconds.
     */
    private int _maxIdleTime;

    /** The port number the server receives requests on.
     */
    private int _portNumber;

    /** The connector binding a port to the server.  */
    SelectChannelConnector _selectChannelConnector;

    /** The Jetty web server that runs the associated applications. */
    private Server _server;

    /** The thread that runs the web server. */
    private Thread _serverThread;

    /** A flag indicating that the server has attempted to start */
    private boolean _startAttempted;

    /** A Runnable class to run a Jetty web server in a separate thread.
     */
    private class RunnableServer implements Runnable {

        /** Run the Jetty web server.  Stop the server if this thread is
         *  interrupted (for example, when the model is finished executing,
         *  WebServer's wrapup() will interrupt this thread) or if an
         *  exception occurs.
         */
        @Override
        public void run() {

            while (!Thread.interrupted()) {

                // Start the server.
                // This is synchronized with the register() method, so that
                // register() will wait for the server to either start properly
                // or throw an exception
                synchronized (_lock) {
                    try {
                        _startAttempted = true;
                        _server.start();
                        _lock.notify();
                    } catch (Exception e) {
                        // Notify thread users and terminate the server and this
                        // thread if an exception occurs
                        try {
                            _exception = e;
                            _server.stop();
                            _lock.notify();
                        } catch (Exception e2) {
                            _exception = e;
                            _lock.notify();
                            Thread.currentThread().interrupt();
                            return;
                        }
                        _lock.notify();
                        Thread.currentThread().interrupt();
                        return;
                    }
                    ;
                }

                // The .join() method blocks the thread until the server
                // terminates.
                try {
                    _server.join();

                } catch (InterruptedException e) {
                    // Notify thread users and terminate the server and this
                    // thread if an exception occurs
                    try {
                        _server.stop();
                    } catch (Exception e2) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    Thread.currentThread().interrupt();
                    return;
                }
                ;
            }

            try {
                _server.stop();
            } catch (Throwable throwable) {
                // Nothing to do here since the thread will terminate next
            }
            return;
        }
    }
}
