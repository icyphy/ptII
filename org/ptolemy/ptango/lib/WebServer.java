/* An actor that runs a Jetty web server and routes requests to other actors 
 * in the model.

 Copyright (c) 1997-2011 The Regents of the University of California.
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

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// WebServer

/** An actor that runs a Jetty web server and routes requests to other actors 
 *  in the model.  HttpService actors will register their servlets in 
 *  preinitialize().  Then, the WebServer will route requests to the appropriate
 *  servlet while the model is running.
 *  

 *  @author ltrnc
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.HttpService
 */

public class WebServer extends TypedAtomicActor {
    public WebServer(CompositeActor container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // Create an empty collection to store the context handlers and
        // a new context handler to hold servlets from the actors
        // This is done here since other actors will register their servlets in
        // preinitialize
        _contextHandlers = new ContextHandlerCollection();
        
        _actorContextHandler = 
            new ServletContextHandler(ServletContextHandler.SESSIONS);
        _actorContextHandler.setContextPath("/");
        
        // Create a Jetty server and set its properties
        _server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(_portNumber);
        connector.setMaxIdleTime(_maxIdleTime);
        _server.setConnectors(new Connector[] {connector});

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Start the web server in a new thread.
     *  Reference:  http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
     *  http://ptrthomas.wordpress.com/2009/01/24/how-to-start-and-stop-jetty-revisited/
     *  http://draconianoverlord.com/2009/01/10/war-less-dev-with-jetty.html
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();        
        
        // Start the server in a new thread
        _serverThread = new Thread(new RunnableServer());
        _serverThread.start();

    }
    
    /** Clear the list of context handlers so that an completely new list is 
     *  generated during the next preinitialize() in case the model changes.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException {
        
        // Create an empty collection to store the context handlers and
        // a new context handler to hold servlets from the actors
        // This is done in fire() in case the model is re-started, since the
        // collection must be cleared before other actors register their 
        // servlets in preinitialize()
        if (_contextHandlers.getHandlers() != null &&
                _contextHandlers.getHandlers().length > 0) {
            _contextHandlers = new ContextHandlerCollection();
        
            _actorContextHandler = 
                new ServletContextHandler(ServletContextHandler.SESSIONS);
            _actorContextHandler.setContextPath("/");
        }
    }
    
    /** Check if the server is still running.  If so, return true.  If not,
     *  return false.
     *  
     *  @return True if the server is still running, false otherwise
     */
    // TODO:  This is not working...
    /*
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        
        
        if (_serverThread.isInterrupted()) {
            return false;
        } 

        return true;
        
    }
    */

    
    /** Register a servlet with this WebServer.  An actor with a servlet should 
     * call this in its initialize() method to register its servlet. 
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
     * @param servlet  The servlet to register with this web server
     * @param path  The relative path to map this servlet to
     */
    public void registerServlet(HttpServlet servlet, String path) {
        
        // FIXME:  How to check that this path is legal?
        _actorContextHandler.addServlet(new ServletHolder(servlet), path); 
        
        _contextHandlers.setHandlers(new Handler[] {_actorContextHandler});
        _server.setHandler(_contextHandlers);
    }
    
    /** Instruct the thread running the web server to the stop the server and
     *  terminate itself.
     */
     public void wrapup() throws IllegalActionException {
         super.wrapup();
         
         _serverThread.interrupt();
         _serverThread = null;
     }
     
     /** A Runnable class to run a Jetty web server in a separate thread.
      * 
      * @author ltrnc
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
     
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
     
     /** A context handler which stores all of the servlets registered 
      *  by other actors.
      */
     private ServletContextHandler _actorContextHandler;
     
     /** A context handler collection which stores the context handlers.  
      *  It is possible to have multiple kinds of context handlers. For example, 
      *  some content in a WebContext handler plus the servlets registered by 
      *  actors stored in the _actorContextHanlder.
      */
     private ContextHandlerCollection _contextHandlers;
     
     /** The maximum idle time for a connection.
      */
     private int _maxIdleTime = 30000;
     
     /** The port number the server receives requests on.
      */
     private int _portNumber = 8080;
     
     /** The Jetty web server. 
      */
     private Server _server;
     
     /** The thread that runs the web server.
      */
     private Thread _serverThread;   
        
}