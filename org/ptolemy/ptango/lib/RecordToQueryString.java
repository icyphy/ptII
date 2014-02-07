/* An actor that converts records to URL-encoded query strings.

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ptolemy.actor.lib.conversions.Converter;
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
import ptolemy.kernel.util.Workspace;

/**
 Convert an input record to a URL-encoded query string. A query string can be
 part of a URL (e.g., <code>http://server/program/path/?query_string</code>), 
 or be send as part of a HTTP request like <code>POST</code> 
 or <code>PUT</code> with a header that specifies 
 <code>Content-Type: "application/x-www-form-urlencoded"</code>.

 The string representation of each field of the input record is encoded 
 by {@link URLEncoder#encode}.

 @see URLEncoder
 @author Marten Lohstroh and Edward Lee
 @version $Id: RecordToQueryString.java 67693 2013-10-17 15:59:01Z hudson@moog.eecs.berkeley.edu $
 @since Ptolemy II 9.0
 @Pt.ProposedRating Red (marten)
 @Pt.AcceptedRating Red (marten)
 */
public class RecordToQueryString extends Converter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RecordToQueryString(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The type record is the top of the record sub-lattice, hence 
        // the most general record type.
        // This constraint ensures that the input will be a record token.
        input.setTypeAtMost(BaseType.RECORD);
        new SingletonParameter(input, "_showName").setToken(BooleanToken.TRUE);

        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        RecordToQueryString newObject = (RecordToQueryString) super
                .clone(workspace);
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

        // If there is no input, do nothing.
        if (input.hasToken(0)) {
            RecordToken record = (RecordToken) input.get(0);

            _debug("Read input: " + record);

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
            output.send(0, new StringToken(data.toString()));

        } else if (_debugging) {
            _debug("No input token.");
        }
    }
}
