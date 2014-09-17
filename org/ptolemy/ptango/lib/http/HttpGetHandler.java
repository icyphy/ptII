/* An actor that handles an HttpRequest by producing an output and waiting for an input.

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

package org.ptolemy.ptango.lib.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ptolemy.actor.lib.MicrostepDelay;
import ptolemy.actor.lib.TimeDelay;
import ptolemy.actor.lib.io.FileReader;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** An actor that handles an HTTP request by producing output
 *  and waiting for an input that provides a response.
 *  This actor requires that the model that contains it includes an
 *  instance of {@link WebServer}, which discovers this actor and
 *  delegates HTTP requests to a servlet that this actor creates.
 *  It also requires that the model containing this actor provide
 *  exactly one input to either the {@link #response} or {@link #setCookies}
 *  input port (or both) in response to every output request,
 *  and that such responses are delivered in the same order as
 *  the requests they are responding to.
 *  <p>
 *  The <i>path</i> parameter specifies which HTTP requests will
 *  be delegated to this actor. If the base URL for the web server
 *  is "http://localhost:8080", say, then request of the form
 *  "http://localhost:8080/<i>path</i>" will be delegated to this
 *  actor.
 *  <p>
 *  When this actor receives an HTTP request from the WebServer, it
 *  issues a request for the director to fire it at the greater of
 *  the current time of the director or the time elapsed since
 *  the last invocation of initialize() (in seconds).
 *  When the actor fires, it produces on its output ports the details
 *  of the request, time stamped by the elapsed time since the model
 *  started executing. It expects to be in a model that will send
 *  it data to either the {@link #response} or {@link #setCookies}
 *  input port (or both) with the response to HTTP request.
 *  If that response does not arrive within <i>timeout</i>
 *  (default 30000) milliseconds, then this actor will a issue
 *  timeout response and will discard the response when it eventually
 *  arrives.
 *  <p>
 *  This actor is designed to be used in a DE model (with a DEDirector).
 *  Since a model using this actor must deliver a response for
 *  every request, the model must put this actor in a feedback
 *  loop. The requests appear on the outputs of this actor,
 *  and the responses appear on the inputs. Each such feedback
 *  loop is required to include an instance of {@link MicrostepDelay}
 *  (or {@link TimeDelay}, if you wish to model response times) in order
 *  to break the causality loop that the feedback loop would
 *  otherwise incur.
 *  The downstream model should be used to construct a response.
 *  For example, to simply serve a web page, put a
 *  {@link FileReader} actor to read the response from a local
 *  file and a {@link MicrostepDelay} downstream in a feedback
 *  loop connected back to the response input.
 *
 *  @author Elizabeth Latronico and Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.lib.WebServer
 */
public class HttpGetHandler extends HttpRequestHandler {

    /** Create an instance of the actor.
     *  @param container The container
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the super
     */
    public HttpGetHandler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create and return an HttpServlet that is used to handle requests that
     *  arrive at the path given by the <i>path</i> parameter.
     *  This method is required by the HttpService interface.
     *  @return An HttpServlet to handle requests.
     */
    @Override
    public HttpServlet getServlet() {
        if (_debugging) {
            _debug("*** Creating new servlet.");
        }
        // The relative path for the servlet is calculated in attributeChanged()
        // since the path might not be a valid URI and could throw an exception
        // The getServlet() method does not throw an exception
        return new GetServlet();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// ActorServlet

    /** A servlet providing implementations of get and post.
     *  The way this servlet works is that when a get or post
     *  HTTP request is received, it records the properties of
     *  the request (the URI and parameters) and requests a
     *  firing of the enclosing actor at model time equal to
     *  the time elapsed since the start of execution of the model.
     *  The thread making the get or post request then suspends,
     *  giving the model a chance to execute. When the model has
     *  determined what the response to the request should be,
     *  it notifies this servlet thread, which then completes
     *  the handling of the request, sending back the response
     *  that has been provided by the enclosing actor.
     *
     *  See <a href"http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty">http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty</a>
     */
    @SuppressWarnings("serial")
    protected class GetServlet extends HttpServlet {

        /** Handle an HTTP get request by creating a web page as the HTTP
         *  response.
         *  NOTE: This method is synchronized, and the lock is _not_ released
         *  while waiting for the response. This strategy helps prevent a pending
         *  get request from overlapping with a pending put request. The second
         *  request will be postponed until the first has been completely handled.
         *
         *  @param request The HTTP get request.
         *  @param response The HTTP response to write to.
         *  @exception ServletException  If there is a problem reading from the
         *  servlet request or other servlet problem
         *  @exception IOException  If there is a problem writing to the servlet
         *  response
         */
        @Override
        protected synchronized void doGet(HttpServletRequest request,
                HttpServletResponse response) throws ServletException,
                IOException {
            _handleRequest(request, response, 0);
        }
    }
}
