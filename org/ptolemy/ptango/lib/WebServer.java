/* An attribute that runs a Jetty web server and routes requests to objects
 * in the model.

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

package org.ptolemy.ptango.lib;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.jetty.util.resource.Resource;
import org.ptolemy.ptango.lib.websocket.WebSocketReader;
import org.ptolemy.ptango.lib.websocket.WebSocketService;
import org.ptolemy.ptango.lib.websocket.WebSocketWriter;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// WebServer

/** An attribute that runs a Jetty web server and routes incoming
 *  HTTP requests to objects in the model that implement
 *  {@link HttpService}. The server is set up during
 *  {@link #initialize()} and taken down during
 *  {@link #wrapup()}.  The <i>resourceLocation</i>
 *  parameter gives a directory or URL relative to which this
 *  web server should look for resources (like image files and
 *  the like).
 *  You can add additional resource bases by adding additional
 *  parameters of type ptolemy.data.expr.FileParameter to
 *  this WebServer (select Configure in the context menu).
 *
 *  <p><a href="http://wiki.eclipse.org/Jetty/Tutorial">http://wiki.eclipse.org/Jetty/Tutorial</a>
 *  - The Jetty Tutorial</p>
 *
 *   @author Elizabeth Latronico and Edward A. Lee
 *   @version $Id$
 *   @since Ptolemy II 10.0
 *   @Pt.ProposedRating Yellow (eal)
 *   @Pt.AcceptedRating Red (ltrnc)
 */
public class WebServer extends AbstractInitializableAttribute {

    /** Construct an instance of the attribute.
     * @param container The container.
     * @param name The name.
     * @exception IllegalActionException If the superclass throws it.
     * @exception NameDuplicationException If the superclass throws it.
     */
    public WebServer(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        preferredPort = new Parameter(this, "preferredPort");
        preferredPort.setTypeEquals(BaseType.INT);
        preferredPort.setExpression(Integer
                .toString(WebServerUtilities.DEFAULT_PORT_NUMBER));

        applicationPath = new StringParameter(this, "applicationPath");
        applicationPath.setExpression("/");

        resourcePath = new StringParameter(this, "resourcePath");
        resourcePath.setExpression("/");

        // Set up a parameter to specify the location for reading and writing
        // resources (files).  This parameter defaults to the directory that
        // the current model is located in.
        // FIXME: The full path is encoded. If this file is in the $PTII tree,
        // then the path should begin with $PTII.

        // The Jetty web server supports searching multiple directories/URLs for
        // resources (files) to return as part of an HttpResponse.  However, if
        // there are two different files with the same names in different
        // directories, it is not clear which file will be served.  Right now,
        // the directories are searched in alphabetical order by parameter name.
        // WebServer itself currently only contains a reader
        // (the resourceHandler in setResourceHandlers()
        // Other actors (e.g. HttpCompositeServiceActor) are writers, and
        // will look for a WebServer in the model to determine the directory
        // to write to
        resourceLocation = new FileParameter(this, "resourceLocation");

        // Want resourceLocation to correspond to a directory
        // Add parameters to specify that only directories are allowed
        Parameter allowDirectories = new Parameter(resourceLocation,
                "allowDirectories");
        allowDirectories.setToken("true");
        allowDirectories.setVisibility(Settable.NONE);

        Parameter allowFiles = new Parameter(resourceLocation, "allowFiles");
        allowFiles.setToken("false");
        allowFiles.setVisibility(Settable.NONE);

        URI modelURI = URIAttribute.getModelURI(this);
        // Get the directory excluding the model's name
        // This may be null for newly created models that have not been saved
        // In that case, default to the temporary directory
        // FIXME:  Register an attributeChanged event for when a model is saved
        // to update this directory?
        String path;
        if (modelURI != null && modelURI.getPath() != null
                && !modelURI.getPath().isEmpty()) {
            path = modelURI.getPath();
            int slash = path.lastIndexOf("/");
            if (slash != -1) {
                path = path.substring(0, slash);
            }
        } else {
            path = "$TMPDIR";
        }
        resourceLocation.setExpression(path);

        temporaryFileLocation = new FileParameter(this, "temporaryFileLocation");
        temporaryFileLocation.setExpression("$TMPDIR");

        deployedPort = new Parameter(this, "deployedPort");
        deployedPort.setExpression("-1");
        deployedPort.setVisibility(Settable.NOT_EDITABLE);
        // Don't save deployed port in MoML.  Otherwise, Ptolemy will ask you to
        // save the model upon exiting every time a new deployed port is chosen
        deployedPort.setPersistent(false);

        _dynamicPortSelection = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The URL prefix to map this model to.  A model can be thought of as a
     *  single application.
     *
     *  This defaults to "/", meaning, accept requests for all URLs.
     *  For example, if the WebServer is handling requests on {@link #preferredPort} 8078
     *  of localhost, the model will handle all requests that begin with
     *  <pre>
     *  http://localhost:8078/
     *  </pre>
     *
     *  If multiple applications are running on the same server, it's typical
     *  to assign a unique application prefix for each.  For example, two
     *  applications with application paths of "/calendar" and "/music" would
     *  receive requests directed to
     *  <pre>
     *  http://localhost:8078/calendar
     *  http://localhost:8078/music
     *  </pre>
     *  respectively.
     *
     *  Applications may contain multiple servlets (provided by e.g.
     *  {@link HttpActor}s).
     *  Each servlet typically defines a servlet path which is appended to
     *  the application path for request routing.  For example, two servlets
     *  with servlet paths "/play" and "/upload" in the music application
     *  would receive requests directed to:
     *  <pre>
     *  http://localhost:8078/music/play
     *  http://localhost:8078/music/upload
     *  </pre>
     *  respectively.
     */
    public StringParameter applicationPath;

    /** The port number the server is listening to.  Can be different from
     *  preferredPort if dynamic port selection is enabled.
     */
    public Parameter deployedPort;

    /** The preferred port number for the server to listen to. This is a integer
     *  that defaults to 8078.
     */
    public Parameter preferredPort;

    /** The URL prefix used to request resources (files) from this web service.
     *
     *  This defaults to "/", meaning, clients should issue requests for files
     *  to the base URL.  For example, if the WebServer is handling requests on
     *  {@link #preferredPort} 8078 of localhost, the client can request file "image.png"
     *  using:
     *  <pre>
     *  http://localhost:8078/image.png
     *  </pre>
     *
     *  The resourcePath is added as a prefix before the file's name in the URL.
     *  For example, given the resourcePath "/files", the client can request
     *  file "image.png" using:
     *  <pre>
     *  http://localhost:8078/files/image.png
     *  </pre>
     *
     *  Relative URLs are permissible for files requested by pages served by
     *  this WebServer (common for e.g. custom Javascript libraries).
     *  For example, given the resourcePath "/files", a file named "custom.js"
     *  may be retrieved at the relative URL:
     *  <pre>
     *  /files/custom.js
     *  </pre>
     *
     *  Note that the name of the directory containing resources does NOT have
     *  to match the URL used to access these resources.  See
     *  {@link #resourceLocation} regarding the resource directory.
     *
     *  Subdirectory search is supported (here, for subdirectories to the
     *  directory specified by {@link #resourceLocation}). Add the subdirectory
     *  path to the URL.  For example, given the resourcePath "/files",
     *  a file in subdirectory /user/photos named "selfie.png" may be
     *  retrieved at URL:
     *
     *  <pre>
     *  http://localhost:8078/files/user/photos/selfie.png
     *  </pre>
     */

    public StringParameter resourcePath;

    /** A directory or URL where the web server will look for resources
     *  (like image files and the like).
     *  This defaults to the current model's directory.
     *
     *  You can add additional resource locations by adding additional
     *  parameters of type ptolemy.data.expr.FileParameter to
     *  this WebServer (select Configure in the context menu).
     *
     *  If multiple resourceLocations are given, they will be searched in the
     *  order that the parameters were instantiated.  The first file located
     *  will be returned.
     *
     *  Note that the name of the directory containing resources does NOT have
     *  to match the URL used to access these resources.  See
     *  {@link #resourcePath} regarding the URL.
     */
    public FileParameter resourceLocation;

    /** A directory where the web server will look for resources
     *  (like image files and the like). This specifies an additional
     *  resource location after {@link #resourceLocation}, but the
     *  directory specified here may be used by components implementing
     *  {@link HttpService} as a place to write temporary files
     *  that will be found by the web server.
     *  This defaults to "$TMPDIR", a built-in variable
     *  that specifies a temporary file location.
     *  See the explanation of {@link #resourcePath}.
     */
    public FileParameter temporaryFileLocation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  If the attribute is an
     *  instance of FileParameter and the server is running (initialize()
     *  has been called and wrapup() has not), then update the resource
     *  handlers in the server.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        // Changes to attributes are not currently propagated to the
        // web server directly.  They will take effect the next time
        // initialize() is called.

        // In the future, changes could be propagated immediately to the
        // web server.

        if (attribute == preferredPort) {
            if (preferredPort == null ||
                    preferredPort.getExpression().isEmpty()) {
                _dynamicPortSelection = true;
            } else {
                _dynamicPortSelection = false;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the attribute.
     *  @param workspace The workspace in which to place the cloned attribute.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        WebServer newObject = (WebServer) super.clone(workspace);
        // _appInfo is set in initialize()
        newObject._appInfo = null;
        newObject._dynamicPortSelection = false;
        newObject._serverManager = WebServerManager.getInstance();

        return newObject;
    }

    /** Collect servlets from all model objects implementing HttpService
     *  and WebSocketService and start the web server in a new thread.
     *  <p>
     * In the current implementation, servlets must be registered before the
     * Jetty server starts.  Servlets are not allowed to be added to a running
     * ContextHandler.  Currently, the Jetty server is started once and runs
     * until the model finishes executing.  It would also be possible to pause
     * the server, add a servlet, and restart the server, which would allow
     * a model to dynamically add servlets.  This might cause strange behavior
     * to an outside observer, however, since some HttpRequests could fail
     * if a servlet has not been loaded yet.
     *
     *  References:
     *  <ul>
     *  <li> <a href="http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty">http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty</a></li>
     *  <li> <a href="http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty">http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty</a></li>
     *  <li> <a href="http://draconianoverlord.com/2009/01/10/war-less-dev-with-jetty.html">http://draconianoverlord.com/2009/01/10/war-less-dev-with-jetty.html</a></li>
     *  </ul>
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void initialize() throws IllegalActionException {

        super.initialize();

        if (_debugging) {
            _debug("Initializing web server.");
        }

        // Remember local websocket services so the websockets can be
        // opened after the web server is started and its port number determined
        // TODO:  Refactor to use e.g. an interface instead of class names
        // Want local clients only.  Does not apply to remote clients or
        // local server-side socket endpoints
        HashSet<WebSocketReader> readers = new HashSet();
        HashSet<WebSocketWriter> writers = new HashSet();

        int preferredPortValue = WebServerUtilities.DEFAULT_PORT_NUMBER;

        if (preferredPort != null && !preferredPort.getExpression().isEmpty()) {
           preferredPortValue = Integer.parseInt(preferredPort.getExpression());
           _dynamicPortSelection = false;
        } else {
            _dynamicPortSelection = true;
        }

        // Get the web server manager to register this application with
        if (_serverManager == null) {
            _serverManager = WebServerManager.getInstance();
        }

        // Get the model name, application path and temporary file location
        String modelName = getFullName();

        String applicationPathString = "/";
        if (applicationPath != null) {

            // Application path must begin with an "/"; add one if missing
            if (applicationPath.getExpression().startsWith("/")) {
                applicationPathString = applicationPath.getExpression();
            } else {
                applicationPathString = "/" + applicationPath.getExpression();
            }
        }

        // Assemble info about this model into a WebApplicationInfo object
        // Throw an exception if the model does not have a name
        try {
            _appInfo = new WebApplicationInfo(modelName, applicationPathString,
                    temporaryFileLocation);
        } catch (Exception e) {
            throw new IllegalActionException(this, e,
                    "Failed to create WebApplicationInfo");
        }

        // Collect requested servlet mappings from all model objects
        // implementing HttpService.  Check for duplicates.
        // NOTE: This used to use the top level, but it makes more sense to use the container.
        // Also, now it only looks for entities, and it does not penetrate opaque composites.
        NamedObj container = getContainer();
        if (!(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this,
                    "Container is required to be a CompositeEntity.");
        }
        List<Entity> entities = ((CompositeEntity) container)
                .allAtomicEntityList();
        for (Entity entity : entities) {
            if (entity instanceof HttpService) {
                HttpService service = (HttpService) entity;
                // Tell the HttpService that this is its WebServer,
                // so that it can get, for example, critical information such
                // as resourcePath.
                service.setWebServer(this);

                if (_debugging) {
                    _debug("Found web service: " + entity.getFullName());
                }

                // Add this path to the list of servlet paths
                URI path = service.getRelativePath();

                try {
                    _appInfo.addServletInfo(path, service.getServlet());
                } catch (Exception e) {
                    throw new IllegalActionException(
                            this, "Actor " + entity.getName()
                              + " requested the web service URL "
                              + path
                              + " , but this URL has already been claimed "
                              + "by another actor or by a resource in this "
                              + "WebServer.  Please specify a unique URL.");
                }
            } else if (entity instanceof WebSocketService)  {
                // Set up support for local websocket services.  Remote
                // websocket services do not require anything from the server
                WebSocketService service = (WebSocketService) entity;
                boolean isLocal = true;

                // TODO:  Refactor to use e.g. an interface for local clients
                // only.  Add isLocal to interface?
                if (service instanceof WebSocketReader) {
                    isLocal = ((WebSocketReader)service).isLocal();
                    if (isLocal) {
                        readers.add((WebSocketReader) service);
                    }
                } else if (service instanceof WebSocketWriter) {
                    isLocal = ((WebSocketWriter)service).isLocal();
                    if (isLocal) {
                        writers.add((WebSocketWriter) service);
                    }
                }

                if (_debugging) {
                    _debug("Found WebSocket actor: " + entity.getFullName());
                }

                if (isLocal) {
                    // Add this path to the list of socket paths
                    URI path = service.getRelativePath();

                    try {
                        _appInfo.addSocketInfo(path, entity);
                    } catch (Exception e) {
                        throw new IllegalActionException(
                                this, "Actor " + entity.getName()
                                + " requested the web service URL "
                                + path
                                + " , but this URL has already been claimed "
                                + "by another actor or by a resource in this "
                                + "WebServer.  Please specify a unique URL.");
                    }
                }
            }
        }

        // Specify directories or URLs in which to look for resources.
        // These are given by all instances of FileParameter in this
        // WebServer. Use a LinkedHashSet to preserve the order.
        LinkedHashSet<Resource> resourceLocations =
                new LinkedHashSet<Resource>();
        List<FileParameter> bases = attributeList(FileParameter.class);
        // To prevent duplicates, keep track of bases added
        // This set includes the temporary file location
        HashSet<URL> seen = new HashSet<URL>();
        for (FileParameter base : bases) {
            // If blank, omit
            if (base.getExpression() != null && !base.getExpression().isEmpty()) {
                try {

                    // Use the ClassLoader to obtain the location (vs. specifying
                    // a directory on the file system), so that the demos
                    // can also be run from within a .jar file

                    // If expression starts with $PTII/ , strip this
                    // Also strip out $CLASSPATH (WebStart uses this)
                    // Assumes .jar file uses $PTII as root location
                    // Assumes full path is given
                    // TODO:  What to do about paths relative to model's location?
                    // Would these work?  None are used so far.

                    String expression = base.getExpression();
                    if (expression.startsWith("$PTII/")) {
                        expression = expression.substring(6);
                    }

                    // Get directory
                    // Try ClassLoader first to resolve any directories within
                    // Ptolemy tree.  If the directory is not part of the tree
                    // (e.g. $TMPDIR), the ClassLoader will not find it, so then
                    // use the expression directly
                    URL baseURL;

                    if (expression.startsWith("$CLASSPATH/")) {
                        baseURL = base.asURL();
                    }
                    else if (this.getClass().getClassLoader()
                            .getResource(expression) != null) {
                        baseURL = new URL(this.getClass().getClassLoader()
                                .getResource(expression).toExternalForm());
                    } else {
                        baseURL = base.asURL();
                    }

                    if (baseURL != null) {
                        URL baseAsURL = base.asURL();
                        if (seen.contains(baseAsURL)) {
                            continue;
                        }
                        seen.add(baseAsURL);
                        if (_debugging) {
                            _debug("Adding resource location: " + baseAsURL);
                        }

                        // Use Resource.newResource() to support WebStart
                        // http://67-23-9-112.static.slicehost.net/faq?s=960-WebStart&t=Eclipse
                        String stringURL = baseAsURL.toExternalForm();
                        if (!stringURL.endsWith("/")) {
                            stringURL = stringURL + "/";
                        }

                        resourceLocations.add(Resource.newResource(stringURL));
                    }
                } catch (IOException e3) {
                    throw new IllegalActionException(this,
                            "Can't access resource base: " + base.stringValue());
                }
            }
        }


        // Throw an exception if resource path is not a valid URI, if a
        // duplicate path is requested or if the directory does not exist
        try {
            _appInfo.addResourceInfo(new URI(resourcePath.stringValue()),
                    resourceLocations);
        } catch (URISyntaxException e) {
            throw new IllegalActionException(this, "Resource path is not a "
                    + "valid URI.");
        } catch (Exception e2) {
            throw new IllegalActionException(this, e2,
                    "Failed to add resource info.");
        }

        try {
            int actualPort =
            _serverManager.register(_appInfo, preferredPortValue,
               _dynamicPortSelection);
            if (actualPort != -1) {

                deployedPort.setExpression(Integer.toString(actualPort));
                deployedPort.validate();
            }

            // Open all local web sockets
            // Prepend ws://localhost:port to URI

            for (WebSocketReader reader : readers){
                // TODO:  Allow secure websockets, with wss://
                URI path = URI.create("ws://localhost:" + actualPort +
                        reader.getRelativePath().toString());
                reader.open(path);
            }

            for (WebSocketWriter writer : writers){
                // TODO:  Allow secure websockets, with wss://
                URI path = URI.create("ws://localhost:" + actualPort +
                        writer.getRelativePath().toString());
                writer.open(path);
            }

        } catch (Exception e) {
            throw new IllegalActionException(this, e,
                    "Failed to register web server.");
        }
    }

    /** Unregister this application with the web server manager.
     *
     * @exception IllegalActionException if there is a problem unregistering
     * the application */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_debugging) {
            _debug("Unregistering web application.");
        }

        if (deployedPort != null && deployedPort.getExpression() != null) {
            // Only attempt to unregister application if registration was
            // successful.

            // If we are exporting to JNLP, then initialize might not
            // have been called.

            int deployedPortValue =
                    Integer.parseInt(deployedPort.getExpression());
            if (_serverManager != null
                    && _appInfo != null
                    && _serverManager.isRegistered(_appInfo.getModelName(),
                            deployedPortValue)) {
                try {
                    _serverManager.unregister(_appInfo, deployedPortValue);
                } catch (Exception e) {
                    throw new IllegalActionException(this, e,
                            "Failed to stop web application.");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Info about the web application defined by the model.
     */
    private WebApplicationInfo _appInfo;

    /** A flag indicating if dynamic port allocation is enabled. */
    private boolean _dynamicPortSelection;

    /** The manager for this web application. */
    private WebServerManager _serverManager;
}
