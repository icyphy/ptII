/* An actor that outputs the response to an HTTP-request.

 @Copyright (c) 1998-2013 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package org.ptolemy.ptango.lib.http;

import java.net.MalformedURLException;
import java.net.URL;

import org.ptolemy.ptango.lib.HttpGet;
import org.ptolemy.ptango.lib.HttpRequest;
import org.ptolemy.ptango.lib.HttpRequest.Method;
import org.ptolemy.ptango.lib.HttpResponse;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 An abstract base class for an actor that outputs the response to an HTTP-request.
 
 @see HttpGet
 @author Edward A. Lee and Marten Lohstroh
 @version $Id: HttpRequestIssuer.java 67693 2013-10-17 15:59:01Z hudson@moog.eecs.berkeley.edu $
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public abstract class HttpSink extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HttpSink(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        contentType = new PortParameter(this, "contentType");
        contentType.setStringMode(true);
        contentType.setExpression("application/x-www-form-urlencoded");

        input = new TypedIOPort(this, "input", true, false);

        input.setTypeEquals(BaseType.STRING);
        new SingletonParameter(input, "_showName").setToken(BooleanToken.TRUE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);

        properties = new PortParameter(this, "properties");
        properties.setExpression("{}");
        properties.setTypeAtMost(BaseType.RECORD);
        new SingletonParameter(properties.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);
        (new StringAttribute(properties.getPort(), "_cardinal"))
                .setExpression("SOUTH");

        status = new TypedIOPort(this, "status", false, true);
        status.setTypeEquals(HttpResponse.getStatusType());
        new SingletonParameter(status, "_showName").setToken(BooleanToken.TRUE);

        timeout = new Parameter(this, "timeout");
        timeout.setTypeEquals(BaseType.INT);
        timeout.setExpression("30000");
        timeout.addChoice("NONE");

        timeoutResponse = new StringParameter(this, "timeoutResponse");

        // FIXME: what about URL-encoded parameters? Should we be able to manipulate them using inputs? 
        uri = new PortParameter(this, "uri");
        uri.setStringMode(true);
        uri.setExpression("http://localhost");
        new SingletonParameter(uri.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);
        (new StringAttribute(uri.getPort(), "_cardinal"))
                .setExpression("SOUTH");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The content-type to go along with the request.
     *  This is a string that defaults to "application/x-www-form-urlencoded".
     *  In case the a content-type is defined in the properties record, either
     *  set as a parameter, or set using an input token, that definition has
     *  precedence over this one.
     */
    public Parameter contentType;

    /** The input port, which accepts a string that must be formatted and 
     *  encoded in accordance with the given content-type.
     */
    public TypedIOPort input;

    /** The output port, which delivers a string, the response to the request.
     */
    public TypedIOPort output;

    /** A record of which each element represents a property to be included in
     *  the header of the request.
     */
    public PortParameter properties;

    /** An output port for transmitting a token containing the status of the
     * request.  This is a RecordToken comprised of the response code,
     * response message, a boolean indicating if the request was successful,
     * and a boolean indicating if further action is expected.
     */
    public TypedIOPort status;

    /** The timeout in milliseconds for establishing a connection or reading a value.
     *  Set to NONE to specify no timeout.
     *  This is an integer that defaults to 30000, giving a timeout of 30 seconds.
     */
    public Parameter timeout;

    /** The response to send upon timeout.
     *  If this is empty, then this actor will throw an exception rather than
     *  send a response.  This is a string that defaults to empty.
     */
    public StringParameter timeoutResponse;

    /** The address of the service to invoke.
     *  This is a string that defaults to "http://localhost", which
     *  refers to a web server on the local host.
     */
    public PortParameter uri;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HttpSink newObject = (HttpSink) super.clone(workspace);
        // FIXME:
        //newObject.properties.setTypeAtMost(BaseType.RECORD);
        return newObject;
    }

    /** If there is an input, then issue a request.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        uri.update();
        properties.update();

        // If there is no input, do nothing.
        if (input.hasToken(0)) {
            // Make sure we have prepared an http request.
            if (_request == null) {
                _request = new HttpRequest();
            }

            try {
                _request.setUrl(new URL(((StringToken) uri.getToken())
                        .stringValue()));
            } catch (MalformedURLException e) {
                throw new IllegalActionException(this, e, "Invalid URI.");
            }
            _request.setMethod(getMethod());
            _request.setBody(((StringToken) input.get(0)).stringValue());

            if (_debugging) {
                _debug("Request body: " + _request.getBody().toString());
                _debug("Request URL: " + _request.getUrl().toString());
                _debug("Waiting for response.");
            }

            // If a timeout has been specified, set it.
            int timeoutValue = ((IntToken) timeout.getToken()).intValue();
            if (timeoutValue >= 0) {
                _request.setTimeout(timeoutValue);
            }

            HttpResponse response = _request.execute();
            // If a timeout occurs, check if an exception should be thrown
            if (response.timedOut()) {
                if (_debugging) {
                    _debug("*** Timeout occurred.");
                }
                String timeout = timeoutResponse.stringValue();
                if (timeout.trim().equals("")) {
                    throw new IllegalActionException(this, "HTTP "
                            + _request.getMethod() + " "
                            + response.getResponseMessage());
                }
            }

            // FIXME: default response upon failure or empty string?
            output.send(0, new StringToken(response.getBody()));
            status.send(0, response.getStatus());

        } else if (_debugging) {
            _debug("No payload. Request ignored.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////
    protected abstract Method getMethod();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The request to be issued **/
    HttpRequest _request;
}
