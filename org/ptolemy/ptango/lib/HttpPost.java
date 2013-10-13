/* An actor that writes input data to the specified file.

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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 Post the contents of input record to a specified URL.
 The string representation of each field of the input
 record is encoded by {@link URLEncoder#encode} and provided
 as a value for the post parameter whose name is the name of
 the record field. The field name is also encoded like the value.

 @see HttpGet
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class HttpPost extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HttpPost(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        url = new PortParameter(this, "url");
        url.setStringMode(true);
        url.setExpression("http://localhost");
        new SingletonParameter(url.getPort(), "_showName").setToken(BooleanToken.TRUE);
        StringAttribute cardinal = new StringAttribute(url.getPort(), "_cardinal");
        cardinal.setExpression("SOUTH");

        input = new TypedIOPort(this, "input", true, false);
        // The type record is the top of the record sublattice, hence the most general record type.
        // This constraint ensures that the input will be a record token.
        input.setTypeAtMost(BaseType.RECORD);
        new SingletonParameter(input, "_showName").setToken(BooleanToken.TRUE);
        
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port, which accepts a record.
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
        HttpPost newObject = (HttpPost) super.clone(workspace);
        newObject.input.setTypeAtMost(BaseType.RECORD);
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
        // If there is no input, do nothing.
        if (input.hasToken(0)) {
            RecordToken record = (RecordToken)input.get(0);
            if (_debugging) {
                _debug("Read input: " + record);
            }
            StringBuffer data = new StringBuffer();
            boolean first = true;
            for (String field : record.labelSet()) {
                if (!first) {
                    data.append("&");
                }
                first = false;
                try {
                    data.append(URLEncoder.encode(field, "UTF-8"));
                    data.append("=");
                    // If the value of the record is a StringToken, may strip surrounding quotation marks.
                    Token value = record.get(field);
                    String string = value.toString();
                    if (value instanceof StringToken) {
                        string = ((StringToken) value).stringValue();
                    }
                    data.append(URLEncoder.encode(string, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new InternalErrorException(e);
                }
            }
            String urlValue = ((StringToken) url.getToken()).stringValue();
            if (urlValue == null || urlValue.isEmpty()) {
                throw new IllegalActionException("No URL provided.");
            }
            StringBuffer response = new StringBuffer();
            try {
                URL url = new URL(urlValue);
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                OutputStreamWriter writer = null;
                try {
                    writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(data.toString());
                    writer.flush();
                
                    if (_debugging) {
                        _debug("Posted: " + data.toString());
                        _debug("To URL: " + url.toString());
                        _debug("Waiting for response.");
                    }

                    BufferedReader reader = null;
                    try {
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
                } finally {
                    if (writer != null) {
                        writer.close();
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
