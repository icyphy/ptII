/* An actor that writes an input string to the specified file.

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
package org.ptolemy.ptango.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 Post the contents of input record to a specified URL.
 

 @see HttpGet
 @author  Edward A. Lee Marten Lohstroh
 @version $Id: HttpPut.java 67693 2013-10-17 15:59:01Z hudson@moog.eecs.berkeley.edu $
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class HttpPut extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HttpPut(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        contentType = new PortParameter(this, "contentType");
        contentType.setStringMode(true);
        contentType.setExpression("application/x-www-form-urlencoded");
        
        new SingletonParameter(contentType.getPort(), "_showName").setToken(BooleanToken.TRUE);
        (new StringAttribute(contentType.getPort(), "_cardinal")).setExpression("SOUTH");
        
        url = new PortParameter(this, "url");
        url.setStringMode(true);
        url.setExpression("http://localhost");
        
        new SingletonParameter(url.getPort(), "_showName").setToken(BooleanToken.TRUE);
        (new StringAttribute(url.getPort(), "_cardinal")).setExpression("SOUTH");

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);
        
        new SingletonParameter(input, "_showName").setToken(BooleanToken.TRUE);
        
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The content-type to go along with the request.
     *  This is a string that defaults to "application/x-www-form-urlencoded".
     */
    public PortParameter contentType;
    
    /** The input port, which accepts a string that must be formatted and 
     *  encoded in accordance with the given content-type.
     */
    public TypedIOPort input;

    /** The output port, which delivers a string, the response to the post.
     */
    public TypedIOPort output;

    /** The URL to post to.
     *  This is a string that defaults to "http://localhost", which
     *  refers to a web server on the local host.
     */
    public PortParameter url;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HttpPut newObject = (HttpPut) super.clone(workspace);
        newObject.input.setTypeEquals(BaseType.STRING); // FIXME: need this?
        return newObject;
    }

    /** If there is an input, then post to the specified URL the
     *  data on the input record, wait for a response, and output
     *  the response on the output port.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        url.update();
        contentType.update();
        // If there is no input, do nothing.
        if (input.hasToken(0)) {
            String body = ((StringToken)input.get(0)).stringValue();
            if (_debugging) {
                _debug("Read input: " + body);
            }
            
            String urlValue = ((StringToken) url.getToken()).stringValue();
            if (urlValue == null || urlValue.isEmpty()) {
                throw new IllegalActionException("No URL provided.");
            }
            OutputStreamWriter writer = null;

            try {
                URL url = new URL(urlValue);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("Content-Type", contentType.getExpression());
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                try {
                    writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(body);
                    writer.flush();
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
                
                _debug("Put: " + body);
                _debug("To URL: " + url.toString());
                _debug("Waiting for response.");
                
                BufferedReader reader = null;
                StringBuffer response = new StringBuffer();
                try {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String line;
                    // FIXME: Need a timeout here!
                    while ((line = reader.readLine()) != null) {
                    response.append(line);
                    if (!line.endsWith("\n")) {
                        response.append("\n");
                    }
                    }
                    if (_debugging) {
                        _debug("Received response: " + response.toString());
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
                output.send(0, new StringToken(response.toString()));
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex, "postfire() failed");
            }
        } else if (_debugging) {
            _debug("No input token.");
        }
    }
}
