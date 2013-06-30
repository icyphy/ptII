/* An attribute that runs a Jetty web server and routes requests to objects
 * in the model.

 Copyright (c) 1997-2013 The Regents of the University of California.
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

import org.eclipse.jetty.util.resource.FileResource;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.data.IntToken;
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
 *   @since Ptolemy II 9.0
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

        port = new Parameter(this, "port");
        port.setTypeEquals(BaseType.INT);
        port.setExpression(Integer.toString(_portNumber));

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
        URI modelURI = URIAttribute.getModelURI(this);
        // Get the directory excluding the model's name
        // This may be null for newly created models that have not been saved
        // In that case, default to the temporary directory
        // FIXME:  Register an attributeChanged event for when a model is saved
        // to update this directory?
        String path;
        if (modelURI != null && modelURI.getPath() != null
                && !modelURI.getPath().isEmpty()) {
            path = modelURI.getPath().toString();
            int slash = path.lastIndexOf("/");
            if (slash != -1) {
                path = path.substring(0, slash);
            }
        } else {
            path = "$TMPDIR";
        }
        resourceLocation.setExpression(path);

        temporaryFileLocation = new FileParameter(this,"temporaryFileLocation");
        temporaryFileLocation.setExpression("$TMPDIR");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The URL prefix to map this application to.  This defaults to "/",
     *  which will cause the model to receive all URLs directed to this
     *  server. For example, if this WebServer is handling requests on
     *  {@link #port} 8078 of localhost, then with value "/", this
     *  WebServer will handle all requests to
     *  <pre>
     *  http://localhost:8078/
     *  </pre>
     *  Individual servlets (actors or attributes in a model that implement
     *  {@link HttpService}) will be mapped to URLs relative to this context path.
     *  For example, if the model contains a servlet with relative path "myService",
     *  then this WebServer will delegate to it requests of the form
     *  <pre>
     *  http://localhost:8078/myService
     *  </pre>
     * <p>
     * Other choices besides "/" are possible.  For example, for web applications, it's
     * common to host several applications on the same server.  It's typical
     * to have each application use setContextPath("/appName") here,
     * (e.g. setContextPath("/myCalendarApp"), setContextPath("/tetris")
     * Each application can contain multiple servlets, which are registered
     * to URLs relative to this path, e.g.:
     * /myCalendarApp/view, /myCalendarApp/print, /tetris/view
     * That way the separate applications have a separate URL namespaces and
     * don't interfere with each other.  A web server often offers some
     * default content at the root / then.  E.g. Tomcat provides the Tomcat
     * manager screen to load/unload web applications.
     */
    public StringParameter applicationPath;

    /** The port number to respond to. This is a integer that
     *  defaults to 8078.
     */
    public Parameter port;

    /** The URL prefix used to request resources (files) from this web service.  
     *  For example, an HTML page requesting an image file.
     *  
     *  The web server creates a ResourceHandler object to accept incoming HTTP 
     *  requests for files, such as images, and return those files.  
     *  The ResourceHandler is assigned a URL prefix, specified in resourcePath, 
     *  which clients use to submit requests to the ResourceHandler.

     *  The resourcePath should be distinct from all other HttpActor paths in 
     *  the model.  Otherwise, the ResourceHandler will intercept requests 
     *  intended for an HttpActor.
     *  
     *  Examples:
     *  A file can be retrieved using an absolute URL or a relative URL.  
     *  Absolute URLs follow the pattern:
     *  <pre>
     *  protocol://hostname:portname/applicationPath/resourcePath/filename.ext
     *  </pre>
     *  For example:
     *  <pre>
     *  http://localhost:8078/myAppName/files/PtolemyIcon.gif
     *  </pre>
     *  for an {@link #applicationPath} of "/myAppName" and a resourcePath of "/files"
     *  or
     *  <pre>
     *  http://localhost:8078/files/PtolemyIcon.gif
     *  </pre>
     *  for an {@link #applicationPath} of "/" and a resourcePath of "/files".
     *  The resource may also be referenced by the relative URL
     *  <pre>
     *  /files/PtolemyIcon.gif
     *  </pre>
     *  from within the application; e.g., a web page served by URL
     *  <pre>
     *  protocol://hostname:portname/applicationPath/
     *  </pre>
     *  <p>
     *  The ResourceHandler will look in resourceLocation for this file,
     *  and if it does not find it there, will also look in any additional
     *  resource locations that have been added to this web server
     *  (see #resourceLocation).
     *  </p>
     *  Note that ResourceHandler supports subdirectories, for example
     *  http://localhost:8078/myAppName/files/img/PtolemyIcon.gif
     *  and a resourceLocation of $PTII/org/ptolemy/ptango/demo
     *  will tell the ResourceHandler to get the file at
     *  $PTII/org/ptolemy/ptango/demo/img/PtolemyIcon.gif
     *
     *  The ResourceHandler can support multiple resourceLocations, in
     *  which case they will be searched in the order of the parameters.
     *  To added locations to search for files, just add parameters
     *  that are instances of "ptolemy.data.expr.FileParameter" to
     *  this WebServer.
     */
    public StringParameter resourcePath;

    /** A directory or URL where the web server will look for resources
     *  (like image files and the like).
     *  This defaults to the current model's directory.
     *  You can add additional resource bases by adding additional
     *  parameters of type ptolemy.data.expr.FileParameter to
     *  this WebServer (select Configure in the context menu).
     *  <p>
     *  To refer to resources in these locations, a web service
     *  (such as an HTML page) uses the {@link #resourcePath}.
     *  See the explanation of {@link #resourcePath}.
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

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        // Changes to attributes are not currently propagated to the 
        // web server directly.  They will take effect the next time 
        // initialize() is called.  
        
        // In the future, changes could be propagated immediately to the 
        // web server.
        
        if (attribute == port) {
            _portNumber = ((IntToken) port.getToken()).intValue();
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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        WebServer newObject = (WebServer) super.clone(workspace);
        // _appInfo is set in initialize()
        newObject._appInfo = null;
        newObject._portNumber = _portNumber;
        newObject._serverManager = WebServerManager.getInstance();

        return newObject;
    }

    /** Collect servlets from all model objects implementing HttpService
     *  and start the web server in a new thread.
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
    public void initialize() throws IllegalActionException {
        
        super.initialize();
        
        if(_debugging) {
            _debug("Initializing web server.");
        }
        
        // Get the web server manager to register this application with
        if (_serverManager == null) {
            _serverManager = WebServerManager.getInstance();
        }
            
        // Get the model name, application path and temporary file location
        String modelName = getFullName();
        
        String applicationPathString = "/";
        if (applicationPath != null) {
            applicationPathString = applicationPath.getExpression();
        }        
        
        // Assemble info about this model into a WebApplicationInfo object
        // Throw an exception if the model does not have a name
        try {
            _appInfo = new WebApplicationInfo(modelName, applicationPathString, 
                temporaryFileLocation);
        }catch(Exception e) {
            throw new IllegalActionException(this, e, "Failed to create WebApplicationInfo");
        }
        
        // Collect requested servlet mappings from all model objects 
        // implementing HttpService.  Check for duplicates.
        // NOTE: This used to use the top level, but it makes more sense to use the container.
        // Also, now it only looks for entities, and it does not penetrate opaque composites.
        NamedObj container = getContainer();
        if (!(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, "Container is required to be a CompositeEntity.");
        }
        List<Entity> entities = ((CompositeEntity)container).deepEntityList();
        for (Entity entity : entities) {
            if (entity instanceof HttpService) {
                HttpService service = (HttpService) entity;
                // Tell the HttpService that this is its WebServer,
                // so that it can get, for example, critical information such
                // as resourcePath.
                service.setWebServer(this);
                
                if(_debugging) {
                    _debug("Found web service: " + entity.getFullName());
                }

                // Add this path to the list of servlet paths
                URI path = service.getRelativePath();
                try {
                    _appInfo.addServletInfo(path, service.getServlet()); 
                } catch(Exception e) {
                    throw new IllegalActionException(this, "Actor " + 
                    entity.getName() + " requested the web service URL " 
                    + path + " , but this URL has already been claimed " 
                    + "by another actor or by a resource in this WebServer." 
                    + "  Please specify a unique URL.");
                }
            }
        }
               
        // Specify directories or URLs in which to look for resources.
        // These are given by all instances of FileParameter in this
        // WebServer. Use a LinkedHashSet to preserve the order.
        LinkedHashSet<FileResource> resourceLocations = new LinkedHashSet<FileResource>();
        List<FileParameter> bases = attributeList(FileParameter.class);
        // To prevent duplicates, keep track of bases added.
        HashSet<URL> seen = new HashSet<URL>();
        for (FileParameter base : bases) {
            try {
                URL baseURL = base.asURL();
                if (baseURL != null) {
                    URL baseAsURL = base.asURL();
                    if (seen.contains(baseAsURL)) {
                        continue;
                    }
                    seen.add(baseAsURL);
                    if (_debugging) {
                        _debug("Adding resource location: " + baseAsURL);
                    }
                    resourceLocations.add(new FileResource(baseAsURL));
                }
            } catch (URISyntaxException e2) {
                throw new IllegalActionException(this,
                        "Resource base is not a valid URI: "
                                + base.stringValue());
            } catch (IOException e3) {
                throw new IllegalActionException(this,
                        "Can't access resource base: " + base.stringValue());
            }
        }
        
        // Throw an exception if resource path is not a valid URI or if a 
        // duplicate path is requested
        try {
            _appInfo.addResourceInfo(new URI(resourcePath.stringValue()), 
                resourceLocations);
        } catch(URISyntaxException e) {
            throw new IllegalActionException(this, "Resource path is not a " +
            		"valid URI.");
        } catch(Exception e2) {
            throw new IllegalActionException(this, e2, "Failed to add resource info.");
        }
        
        try {
            _serverManager.register(_appInfo, _portNumber);
        } catch(Exception e){
            throw new IllegalActionException(this, e, "Failed to register web server.");
        }      
    }

    
    /** Unregister this application with the web server manager. 
     * 
     * @exception IllegalActionException if there is a problem unregistering
     * the application */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if(_debugging) {
            _debug("Unregistering web server.");
        }
        try {
            _serverManager.unregister(_appInfo, _portNumber);
        } catch (Exception e) {
            // Do not throw an exception here, because it will mask an exception
            // that occurred during trying to register the server.
            System.err.println("Warning: Failed to unregister web server.\n" + e);
        }
    }    

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Info about the web application defined by the model. 
     */
    private WebApplicationInfo _appInfo;   

    /** The port number the server receives requests on.
     */
    private int _portNumber = 8078;
    
    /** The manager for this web application. */
    private WebServerManager _serverManager;

}
