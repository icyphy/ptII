package org.ptolemy.ptango.lib;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
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
 *  @author ltrnc
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.WebServer
 */

public class HttpCompositeServiceProvider extends TypedCompositeActor 
        implements HttpService{
    public HttpCompositeServiceProvider(CompositeEntity container, String name)
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
        return new HttpServiceServlet();
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
    

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    
    /** A servlet that handles HTTP requests.  It maps any input and output 
     * ports to information from the input and output web pages.
     */
    private class HttpServiceServlet extends HttpServlet
    {
        public HttpServiceServlet(){}

        protected void doGet(HttpServletRequest request, 
                HttpServletResponse response) 
                throws ServletException, IOException
        {

            // Display a page with an input form and the results of any
            // prior computation, if any
            
            // TODO:  Return HTML from a file (or better, return a "view" like
            // in the Spring framework)
            // Way to do this using relative file names based on the context?
            // Possible for web apps, but I'm not sure about standlone servlets
            
            // TODO:  A get request is ambiguous here - do we want to get the
            // input form or the results of the last computation?
            
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter writer = response.getWriter();                
            
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(getServletContext()
                    .getResourceAsStream("/pages/index.html"))); 
           
            String line;
            while((line = reader.readLine()) != null) {
                writer.println(line);
            }

            reader.close();
            writer.flush();
        }
        
        /** Respond to an HTTP POST request.  The actor reads information from 
         *  the web form into its input ports, executes the contained model, 
         *  and produces a web page with the result.
         *  
         *  HTML forms only allow GET and POST:
         *  http://objectmix.com/php/728829-anyone-have-html-snippet-example-http-method-%3D-put.html
         *  
         *  However it seems like PUT might be more RESTful?
         *  See slides 18-22:  
         *  http://chess.eecs.berkeley.edu/ptango/wiki/uploads/Main/ComposingRESTInterfaceJOpera.pdf
         *  
         *  @param HttpServletRequest request  The HTTP request
         *  @param HttpServletResponse response The HTTP response
         */
        protected void doPost(HttpServletRequest request, 
                HttpServletResponse response) 
                throws ServletException, IOException
        {
            String report = "";
            
            // Map request parameters to input ports
            Iterator inputPorts = inputPortList().iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                // How to handle typing here?  getParameter() always returns
                // a string.
                if (request.getParameter(inputPort.getName()) != null) {
                    // Note that request.getParameter() is case sensitive!
                    String data = request.getParameter(inputPort.getName());
                    
                    // TODO:  Require that the ports have declared types?
                    // Otherwise, how to tell what the type is?
                    // For now, assume double
                    // Extract a token from the request and broadcast this
                    // to the input port's receivers
                    try {
                        DoubleToken token = new DoubleToken(data);
                        report = 
                              report + inputPort.getName() + ": " + data + ",";
                        inputPort.broadcast(token);
                        _response = response;
                        
                    } catch(IllegalActionException e){
                        response.setContentType("text/html");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        PrintWriter writer = response.getWriter();                
                        
                        writer.println("<!DOCTYPE html>");
                        writer.println("<html>");
                        writer.println("<body>");
                        writer.println("Problem with data value for " 
                                + inputPort.getName());
                        Enumeration elements = request.getParameterNames();
                        while(elements.hasMoreElements()) {
                            writer.println("Name: " + elements.nextElement().toString());
                        }
                    
                        writer.println(request.getParameterNames().toString());
                        writer.println("</body>");
                        writer.println("</html>");                   
                        writer.flush();
                        
                        throw new IOException("Input element for port " + 
                       inputPort.getName() + " is incompatible with double.");
                        
                    }
        
                } else {
                    response.setContentType("text/html");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    PrintWriter writer = response.getWriter();                
                    
                    writer.println("<!DOCTYPE html>");
                    writer.println("<html>");
                    writer.println("<body>");
                    writer.println("Problem with data value for " 
                            + inputPort.getName());
                    Enumeration elements = request.getParameterNames();
                    while(elements.hasMoreElements()) {
                        writer.println("Name: " 
                                + elements.nextElement().toString());
                    }
                
                    writer.println(request.getParameterNames().toString());
                    writer.println("</body>");
                    writer.println("</html>");                   
                    writer.flush();
                    
                    throw new IOException("Input element is missing " +
                           "from HTTP request for port " + inputPort.getName());
                    // Throw exception if value is missing for one of the ports?
                    // Can I throw a Ptolemy exception?
                    // How can I propagate this exception however?
                    // Could I call a method from superclass which would throw it?
                }
            }
            
            // Write the response
            // Do this after all ports are read, because if there is 
            // problem with any port value, we want to return an error
            // Currently this prints a list of port names and values
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter writer = response.getWriter();                
            
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<body>");
            writer.println(report);
            writer.println("</body>");
            writer.println("</html>");                   
            writer.flush();
        }
    }  
    
    /** The URI for the relative path from the "path" parameter.  
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions. 
     */
    private URI _URIpath;
    
    /** A copy of the HttpServletReponse to write the result to once the actor
     *  has fired.
     */
    // TODO:  Actually print the response in fire()
    private HttpServletResponse _response;  
    
}