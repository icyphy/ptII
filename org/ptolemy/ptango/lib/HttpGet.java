/* An actor that sends an HTTP GET request to a Web server.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
package org.ptolemy.ptango.lib;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.ptolemy.ptango.lib.HttpRequest.Method;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
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

/** Get the contents of the specified URL and output as a string.
 *  This is similar to the {@link FileReader}, except that it has
 *  a timeout parameter. If the timeout expires while
 *  performing the get, and the timeoutResponse parameter has been
 *  set, then send the specified response. If no timeoutResponse
 *  has been set, then throw an exception on timeout.
 *
 *  @see HttpPost
 *  @author  Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class HttpGet extends LimitedFiringSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HttpGet(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        url = new PortParameter(this, "url");
        url.setStringMode(true);
        url.setExpression("http://localhost");
        new SingletonParameter(url.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);
        StringAttribute cardinal = new StringAttribute(url.getPort(),
                "_cardinal");
        cardinal.setExpression("SOUTH");

        timeout = new Parameter(this, "timeout");
        timeout.setTypeEquals(BaseType.INT);
        timeout.setExpression("30000");
        timeout.addChoice("NONE");

        timeoutResponse = new StringParameter(this, "timeoutResponse");

        newline = new Parameter(this, "newline");
        newline.setExpression("property(\"line.separator\")");

        output.setTypeEquals(BaseType.STRING);
        new SingletonParameter(output, "_showName").setToken(BooleanToken.TRUE);

        status = new TypedIOPort(this, "status", false, true);
        status.setTypeEquals(HttpResponse.getStatusType());
        new SingletonParameter(status, "_showName").setToken(BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The end of line character(s).  The default value is the value
     *  of the line.separator property
     */
    public Parameter newline;

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

    /** The URL to send the request to.
     *  This is a string that defaults to "http://localhost", which
     *  refers to a web server on the local host.
     */
    public PortParameter url;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is an input, then post to the specified URL the
     *  data on the input record, wait for a response, and output
     *  the response on the output port.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        url.update();

        if (_request == null) {
            _request = new HttpRequest();
        }

        String urlValue = ((StringToken) url.getToken()).stringValue();
        if (urlValue == null || urlValue.isEmpty()) {
            throw new IllegalActionException("No URL provided.");
        }

        try {
            _request.setUrl(new URL(urlValue));

            _request.setMethod(Method.GET);

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
        } catch (IOException e) {
            throw new IllegalActionException(this, e, "HTTP request failed");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Http request **/
    HttpRequest _request;
}
