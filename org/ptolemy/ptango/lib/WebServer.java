/* An attribute that runs a Jetty web server and routes requests to objects 
 * in the model.

 Copyright (c) 1997-2012 The Regents of the University of California.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.ResourceCollection;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.CompositeActor;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
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
 *  {@link #wrapup()}.  The <i>resourceBase</i>
 *  parameter gives a directory or URL relative to which this
 *  web server should look for resources (like image files and
 *  the like).
 *  You can add additional resource bases by adding additional
 *  parameters of type ptolemy.data.expr.FileParameter to
 *  this WebServer (select Configure in the context menu).
 *  
 *   @see http://wiki.eclipse.org/Jetty/Tutorial
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
     * @throws IllegalActionException If the superclass throws it.
     * @throws NameDuplicationException If the superclass throws it.
     */
    public WebServer(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        port = new Parameter(this, "port");
        port.setTypeEquals(BaseType.INT);
        port.setExpression("8080");
        
        applicationPath = new StringParameter(this, "applicationPath");
        applicationPath.setExpression("/");
        
        resourcePath = new StringParameter(this, "resourcePath");
        resourcePath.setExpression("/files");
        
        // Set up a parameter to specify the location for reading and writing 
        // resources (files).  This parameter defaults to the directory that
        // the current model is located in.
        
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
        // FIXME:  Better way to do this?
        String path = modelURI.getPath().toString();
        int slash = path.lastIndexOf("/");
        if (slash != -1) {
            path = path.substring(0, slash);
        }
        resourceLocation.setExpression(path);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** The URL prefix to map this application to.  Defaults to "/"
     *  which will cause the model to receive all URLs.  Individual 
     *  servlets will be mapped to URLs relative to this context path.
     *  
     * Other choices are possible.  For example, for web applications, it's
     * common to host several applications on the same server.  It's typical
     * to have each application use setContextPath("/appName") here, 
     * (e.g. setContextPath("/myCalendarApp"), setContextPath("/tetris")
     * Each application can contain multiple servlets, which are registered
     * to URLs relative to this path, e.g.:
     * /myCalendarApp/view, /myCalendarApp/print, /tetris/view
     * That way the separate applications have a separate URL namespace and 
     * don't interfere with each other.  A web server often offers some 
     * default content at the root / then.  E.g. Tomcat provides the Tomcat
     * manager screen to load/unload web applications.
     */ 
    public StringParameter applicationPath;
    
    /** The port number to respond to. This is a integer that
     *  defaults to 8080.
     */
    public Parameter port;
    
    /** The URL prefix which web services (e.g. an HTML page) will use to 
     *  refer to resources (files).  Used by the ResourceHandler. Of the form:
     *  protocol://hostname:portname/applicationPath/resourcePath/filename.ext
     *  e.g.
     *  http://localhost:8080/myAppName/files/PtolemyIcon.gif
     *  for an applicationPath of "/myAppName" and a resourcePath of "/files"
     *  or
     *  http://localhost:8080/files/PtolemyIcon.gif 
     *  for an applicationPath of "/" and a resourcePath of "/files"
     *  
     *  The ResourceHandler will look in resourceLocation for this file.
     *  Note that ResourceHandler supports subdirectories, for example
     *  http://localhost:8080/myAppName/files/img/PtolemyIcon.gif 
     *  and a resourceLocation of $PTII/org/ptolemy/ptango/demo
     *  will tell the ResourceHandler to get the file at 
     *  $PTII/org/ptolemy/ptango/demo/img/PtolemyIcon.gif
     *  
     *  The ResourceHandler can support multiple resourceLocations, in 
     *  which case they will be searched in some order (what order?)
     *  for the file
     * 
     *  The resourcePath should be something other than "/", because then
     *  all incoming requests will be passed to the ResourceHandler 
     *  (assuming the ResourceHandler is passed in first in the list of handlers
     *  to the server.setHandler() method, which it needs to be, 
     *  see _setHandlers() 
     */
    public StringParameter resourcePath;
    
    /** The directory or URL where the web server should look for resources 
     * (like image files and the like). This defaults to the current model's
     *  directory.
     *  You can add additional resource bases by adding additional
     *  parameters of type ptolemy.data.expr.FileParameter to
     *  this WebServer (select Configure in the context menu).
     */
    public FileParameter resourceLocation;

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
    // FIXME:  Add checks for changes to applicationPath and resourcePath
    // Make sure resourcePath is not / and that the user did not specify 
    // overlapping URLs (e.g. /files for both the applicationPath and 
    // the resourcePath, or common prefixes like /files and /files/images)
    
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute instanceof FileParameter) {
            // Resource handlers are being changed. If the
            // server is running, reset the resource handler.
            if (_server != null && _server.isRunning()) {
                // FIXME:  Test this.  I think calling _setHandler() on the 
                // server, as _setResourceHandlers() does, will throw an 
                // exception if the server is running.  Therefore the server 
                // needs to be stopped and restarted.  Test to see if it 
                // restarts properly.
               try { 
                       _server.stop();
                       // FIXME: Does this need to be synchronized on the server?
                       // Sadly, Jetty is undocumented.
                       _createResourceHandler();
                       _server.start();
               } catch(Exception e){
                   try {
                       _server.stop();
                   } catch(Exception e2){
                       throw new IllegalActionException(this, 
                       "Can't update resource handlers of the WebServer.");
                   }
                   throw new IllegalActionException(this, 
                       "Can't update resource handlers of the WebServer.  " +
                       "Stopping the server.");
               };

            }
        } else if (attribute == port) {
            _portNumber = ((IntToken)port.getToken()).intValue();
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
        newObject._server = null;
        newObject._serverThread = null;
        return newObject;
    }

    /** Collect servlets from all model objects implementing HttpService 
     *  and start the web server in a new thread.
     *  
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
     *  http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
     *  http://ptrthomas.wordpress.com/2009/01/24/how-to-start-and-stop-jetty-revisited/
     *  http://draconianoverlord.com/2009/01/10/war-less-dev-with-jetty.html
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize() throws IllegalActionException {
        // Create the server here rather than in the constructor
        // so that it is not created by an instance of this actor in the library.
        if (_server == null) {
            // Create a Jetty server and set its properties
            _server = new Server();
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setPort(_portNumber);
            connector.setMaxIdleTime(_maxIdleTime);
            _server.setConnectors(new Connector[] {connector});
        }
        
        // Create a handler to map incoming requests to servlets registered by
        // other actors in this model (for example, HttpActor)
        ContextHandler servletHandler = _createServletHandler();
        
        // Create a handler to serve files such as images, audio, etc.,
        // from parameters of type FileParameter contained by this WebServer.
        ContextHandler fileHandler = _createResourceHandler();
        
        // Assign the newly created handlers to the server 
        // The server passes requests to handlers in the same order as the array
        // in setHandlers()
        // Therefore, make sure fileHandler is first so that any request for a
        // file (e.g. an image file) is handled by the fileHandler
        // FIXME:  Reference for this?  I read it somewhere on the internet...
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {fileHandler, servletHandler});
        _server.setHandler(handlers);
        
        //Start the server in a new thread with our handlers
        _serverThread = new Thread(new RunnableServer());
        _serverThread.start();
    }
    
    /** Instruct the thread running the web server to the stop the server and
     *  terminate itself.
     */
     public void wrapup() throws IllegalActionException {
         _serverThread.interrupt();
         _serverThread = null;
     }
     
     ///////////////////////////////////////////////////////////////////
     ////                         protected methods                 ////

     /** Create a resource handler to serve files such as images, audio, etc.,
      *  from parameters of type FileParameter contained by this WebServer.
      *  This will also set the _actorContextHandler (for servlets)
      *  that is set up in initialize().  Note that this method is called
      *  in initialize() and may be called again during execution to change
      *  the resource handler.
      *  
      *  @return A ContextHandler containing the created ResourceHandler
      *  @throws IllegalActionException If a FileParameter is found that is
      *   not a valid URI or references a resource that cannot be found.
      */
     protected ContextHandler _createResourceHandler() 
         throws IllegalActionException {
         // Create a resource handler to serve files such as images, audio, ...
         // See also http://restlet-discuss.1400322.n2.nabble.com/Jetty-Webapp-td7313234.html
         ContextHandler fileHandler = new ContextHandler();
         
         // Set the path which other web applications (e.g. an HTML page) would
         // use to request resources (files)
         // Please see comments for resourcePath parameter and example
         // $PTII/org/ptolemy/ptango/demo/WebServerDE/WebServerDE.xml
         fileHandler.setContextPath(resourcePath.getExpression());       
         
         ResourceHandler resourceHandler = new ResourceHandler();
         // FIXME:  Should this be false?  How to prevent listing contents
         // of hard drive??
         resourceHandler.setDirectoriesListed(true);

         // Think this is not needed since we create a ContextHandler 
         // fileHandler, set up our resourceHandler, then call 
         // fileHandler.setHandler(resourceHandler)
         // I think this is only needed if we directly add resourceHandler to 
         // the server's handler list, since resourceHandler does not have
         // a setContextPath() method
         //resourceHandler.setResourceBase(".");

         // Specify directories or URLs in which to look for resources.
         // These are given by all instances of FileParameter in this
         // WebServer.
         // ResourceHandler example:
         // http://cxf.547215.n5.nabble.com/serve-static-content-through-jetty-td5467064.html
         // ResourceCollection example:
         // http://stackoverflow.com/questions/2405038/multiple-webroot-folders-with-jetty
         ArrayList<FileResource> resources = new ArrayList<FileResource>();
         List<FileParameter> bases = attributeList(FileParameter.class);
         for (FileParameter base : bases) {
             try {
                 URL baseURL = base.asURL();
                 if (baseURL != null) {
                     resources.add(new FileResource(base.asURL()));
                 }
             } catch(URISyntaxException e2){
                 throw new IllegalActionException(this,
                         "Resource base is not a valid URI: " + base.stringValue());
             } catch(IOException e3){
                 throw new IllegalActionException(this, 
                         "Can't access resource base: " + base.stringValue());
             };
         }
         resourceHandler.setBaseResource(
                 new ResourceCollection(resources.toArray(new FileResource[resources.size()])));    

         fileHandler.setHandler(resourceHandler);
         return fileHandler;
     }   
     
     /** Create a ContextHandler to find and store all of the servlets
      *  registered by other actors (e.g. HttpCompositeActor).
      *  
      *  @return A ContextHandler containing servlets from the WebServer's
      *  containing model
      */
     
     protected ContextHandler _createServletHandler() {
         
         // Create a new handler to hold servlets from the actors
         ServletContextHandler servletHandler = 
                 new ServletContextHandler(ServletContextHandler.SESSIONS);
         
         servletHandler.setContextPath(applicationPath.getExpression());          
         
         // Collect servlets from all model objects implementing HttpService   
         // FIXME:  Check for overlapping URLs
         NamedObj topLevel = toplevel();      
         Iterator objects = topLevel.containedObjectsIterator();
         while(objects.hasNext()) { 
             Object object = objects.next(); 
             if (object instanceof HttpService) {   
                 HttpService service = (HttpService) object;
                 String path = service.getRelativePath().getPath();
                 servletHandler
                     .addServlet(new ServletHolder(service.getServlet()), path);
             }
         }
         
         return servletHandler; 
     }
     
     ///////////////////////////////////////////////////////////////////
     ////                         private variables                 ////
     
     /** The maximum idle time for a connection.
      */
     private int _maxIdleTime = 30000;
     
     /** The port number the server receives requests on.
      */
     private int _portNumber = 8080;
     
     /** The Jetty web server. */
     private Server _server;
     
     /** The thread that runs the web server. */
     private Thread _serverThread;   

     ///////////////////////////////////////////////////////////////////
     ////                         private variables                 ////
     
     /** A Runnable class to run a Jetty web server in a separate thread.
      */
     private class RunnableServer implements Runnable {

        /** Run the Jetty web server.  Stop the server if this thread is 
         *  interrupted (for example, when the model is finished executing, 
         *  WebServer's wrapup() will interrupt this thread) or if an 
         *  exception occurs.
         */
        public void run() {

            while(!Thread.interrupted()){
                     
            // Start the server.  The .join() method blocks the thread until the 
            // server terminates.
             try {
                _server.start();
                _server.join();
           
             } catch(Exception e){
                 // Notify thread users and terminate the server and this thread 
                 // if an exception occurs
                 try {
                     _server.stop();
                 } catch(Exception e2){
                     Thread.currentThread().interrupt();
                     return;
                 }
                 Thread.currentThread().interrupt();
                 return;
             };
           }
            try {
                _server.stop();
            } catch(Exception e) {
                // Nothing to do here since the thread will terminate next
            }
            return;
        }
         
     }        
}