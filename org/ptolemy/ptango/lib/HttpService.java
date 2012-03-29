package org.ptolemy.ptango.lib;
import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

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
 *  @author ltrnc
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.WebServer
 */

public class HttpService extends TypedAtomicActor {
    public HttpService(CompositeActor container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        message = new StringParameter(this, "message");
        message.setExpression("Hello World");
        
        path = new StringParameter(this, "path");
        path.setExpression("/*");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Register this actor's servlet(s) with the WebServer.
     *
     *  @exception IllegalActionException If no WebServer actor is found at the
     *  top level of the model or if this actor is the top level actor.
     */
    public void preinitialize() throws IllegalActionException {
        // Create a new servlet mapped to the given URL that displays the
        // given message
        // Note that if the parameters are updated while the model is 
        // running - e.g. during fire() - the changes will not propagate
        
        // Find the WebServer actor in this model.  
        // Throw an IllegalActionException if none is found
        NamedObj topLevel = this;
        topLevel = topLevel.toplevel();
        
        if (topLevel.equals(this)) {
            throw new IllegalActionException(this, "HttpRequestActor is not" +
            		"allowed to be the top-level actor");
        }
        
        // Find the WebServer actor.  Currently this code requires it to be
        // immediately contained by the top-level actor.
        // (is that true or does containedObjectsIterator() traverse hierarchy?)
        Iterator objects = topLevel.containedObjectsIterator();
        WebServer webServerActor = null;
        Object object = null;
        
        while(objects.hasNext()) {
            object = objects.next();
            if (object instanceof WebServer) {
                webServerActor = (WebServer) object; 
            }
        }
        
        if (webServerActor == null){
            throw new IllegalActionException(this, "No WebServer actor found. "+
             "Please make sure one is present at the top level of the model.");	
        }
        
        // Register this actor's servlet with the WebServer
        // TODO:  Check if path is valid (how?).  Check for duplicate paths?
        // I think that will run OK (last added servlet takes precedence)
        // but would be confusing to the user.
        String mappedPath = "/*";
        if (!path.getExpression().isEmpty()) {
            mappedPath = path.getExpression().toString();
        }  
        
        String displayMessage = "Hello World";
        if (message.getExpression() != null) {
            displayMessage = message.getExpression().toString();
        }
        
        webServerActor
            .registerServlet(new HelloServlet(displayMessage), mappedPath);
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
}