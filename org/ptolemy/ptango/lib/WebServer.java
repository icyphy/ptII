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
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.attributes.URIAttribute;
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
 *  {@link #wrapup()}.
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
        
        resourceBase = new FileParameter(this, "resourceBase");
        resourceBase.setExpression("$PTII/org/ptolemy/ptango/demo/files/");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** The resource base, a directory or URL relative to which this
     *  web server should look for resources (like image files and
     *  the like). This defaults to
     *  $PTII/org/ptolemy/ptango/demo/files/.
     *  You can add additional resource bases by adding additional
     *  parameters of type ptolemy.data.expr.FileParameter to
     *  this WebServer (select Configure in the context menu).
     */
    public FileParameter resourceBase;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Clone the attribute.
     *  @param workspace The workspace in which to place the cloned attribute.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        WebServer newObject = (WebServer) super.clone(workspace);
        newObject._actorContextHandler = null;
        newObject._handlers = null;
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
        
        // Create a new handler to hold servlets from the actors
        _actorContextHandler = 
                new ServletContextHandler(ServletContextHandler.SESSIONS);
        _actorContextHandler.setContextPath("/");   
        
        // Set the resource base to the path of the model that contains this WebServer.
        URI modelURI = URIAttribute.getModelURI(this);
        // FIXME: The following doesn't work! Doesn't find files relative to model.
        _actorContextHandler.setResourceBase(modelURI.toString());
        
        // Collect servlets from all model objects implementing HttpService       
        NamedObj topLevel = toplevel();      
        Iterator objects = topLevel.containedObjectsIterator();
        while(objects.hasNext()) { 
            Object object = objects.next(); 
            if (object instanceof HttpService) {   
                HttpService service = (HttpService) object;
                _actorContextHandler
                    .addServlet(new ServletHolder(service.getServlet()), 
                            service.getRelativePath().getPath());
            }
        }
        
        // Create a resource handler to serve files such as images, audio, ...
        // See also http://restlet-discuss.1400322.n2.nabble.com/Jetty-Webapp-td7313234.html
        ContextHandler fileHandler = new ContextHandler();
        // FIXME: The following is inappropriate and doesn't seem to do anything anyway.
        // fileHandler.setContextPath("/files");
        
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        // Sadly, Jetty is completely undocumented, but it appears that
        // setResourceBase() is just like setBaseResource() except that it
        // takes a string. So I think the setBaseResource() below will override
        // the next line.
        resourceHandler.setResourceBase(".");
        fileHandler.setHandler(resourceHandler);

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
                resources.add(new FileResource(base.asURL()));
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

        //Start the server in a new thread with our handlers
        //The server passes requests to handlers in the same order as this array
        //Therefore, make sure resourceHandler so that any request for a file
        //(e.g. an image file) is handled by the resourceHandler
        _handlers = new HandlerList();
        _handlers
           .setHandlers(new Handler[] {fileHandler, _actorContextHandler});
        _server.setHandler(_handlers);
        
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
     ////                         private variables                 ////

     /** An enumeration indicating the location to store files 
      */
     public static enum DirectoryType {BASE, TEMP, PERMANENT};
     
     /** A context handler which stores all of the servlets registered 
      *  by other actors.
      */
     private ServletContextHandler _actorContextHandler;
     
     /** A list of handlers for web requests.  It is possible to have different
      *  handlers for different content.  For example, the WebServer actor has
      *  a ResourceHandler to serve files such as images, and a 
      *  ServletContextHandler to serve content from servlets in the Ptolemy
      *  model.  
      */
     private HandlerList _handlers;
     
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