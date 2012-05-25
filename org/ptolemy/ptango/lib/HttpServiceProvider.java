package org.ptolemy.ptango.lib;
import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/* An actor that handles an HttpRequest to the given path.

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

/** An actor that handles an HttpRequest to the given path.  This actor creates
 *  a servlet, registers this servlet with the WebServer during preinitialize(),
 *  and displays its content at the specified path when a request is received.
 *  
 *  @author Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.WebServer
 */

public class HttpServiceProvider extends TypedAtomicActor implements HttpService{
    
    /** Create an instance of the actor.
     * @param container The container
     * @param name The name.
     * @throws IllegalActionException If the superclass throws it.
     * @throws NameDuplicationException If the super
     */
    public HttpServiceProvider(CompositeActor container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        message = new StringParameter(this, "message");
        message.setExpression("Hello World");
        
        path = new StringParameter(this, "path");
        path.setExpression("/*");
        setRelativePath(path.getExpression().toString());
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Returns the relative path that this HttpService is mapped to. 
     * 
     * @return The relative path that this HttpService is mapped to.
     */
    public URI getRelativePath() {
        return _URIpath;
    }

    /** Creates and returns an HttpServlet which is used to handle requests that
     *  arrive at the given path.
     * 
     * @return An HttpServlet to handle requests. 
     */
    public HttpServlet getServlet() {
        // The relative path for the servlet is calculated in preinitialize
        // since the path might not be a valid URI and could throw an exception
        // The getServlet() method does not throw an exception

        String displayMessage = "Hello World";
        if (message.getExpression() != null) {
            displayMessage = message.getExpression().toString();
        }

        return new HelloServlet(displayMessage);
    }
    
    /** Set the relative path that this actor will receive requests to.  This is
     *  set here in case the path does not conform to URI syntax rules.  If the
     *  path does not conform, an IllegalActionException is thrown.
     *
     *  @exception IllegalActionException If the path is invalid URI syntax
     */
    
    public void preinitialize() throws IllegalActionException {
        
        if (!path.getExpression().isEmpty()) {
            setRelativePath(path.getExpression().toString());
        }
    }
    
    /** Set the relative path that this HttpService is mapped to, and ensure
     * that this path conforms to URI naming conventions.
     * See:  http://docs.oracle.com/javase/1.4.2/docs/api/java/net/URI.html
     * 
     * @param path The relative path that this HtppService is mapped to.
     */
    public void setRelativePath(String path) throws IllegalActionException {
        
        try {
            _URIpath = URI.create(path);
        } catch(NullPointerException e){
            throw new IllegalActionException(this, "Path cannot be null.");
        } catch(IllegalArgumentException e2){
            // TODO:  Even better - transform this path to a legal one.
            throw new IllegalActionException(this, "Path contains illegal " +
            		"characters according to URI definition");
        }
    }
    
    /** Set the relative path that this HttpService is mapped to.
     * 
     * @param path The relative path that this HttpService is mapped to.
     */
    public void setRelativePath(URI path) {
        _URIpath = path;
        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** The message to display when a get request is received.
     */
    public StringParameter message;
    
    /** The relative URL to map this servlet to. 
     */
    public StringParameter path;
    
    
    /** A HelloWorld servlet example from 
     *  http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
     */
    private class HelloServlet extends HttpServlet
    {
        private String greeting="Hello World";
        
        /** Construct a servlet with the default greeting.
         */     
        public HelloServlet(){}
        
        /** Construct a servlet with the specified greeting.
         * 
         * @param greeting The message to display on the returned web page
         */
        public HelloServlet(String greeting)
        {
            this.greeting=greeting;
        }
        
        /** Handle an HTTP get request by creating a web page as the HTTP 
         *  response.
         * 
         * @param request  The HTTP get request
         * @param response  The HTTP response to write a web page to
         */
        protected void doGet(HttpServletRequest request, 
                HttpServletResponse response) 
                throws ServletException, IOException
        {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>"+greeting+"</h1>");
            // response.getWriter().println("session=" + request.getSession(true).getId());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    
    /** The URI for the relative path from the "path" parameter.  
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions. 
     */
    private URI _URIpath;
   

}