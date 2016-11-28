/* Converts a Token to a string containing JSON-formatted data.

 Copyright (c) 2012-2016 The Regents of the University of California.
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

package ptolemy.actor.lib.conversions.json;

import java.util.Set;

import ptolemy.actor.lib.conversions.Converter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DateToken;
import ptolemy.data.LongToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
An actor that converts a Token into a StringToken containing JSON-formatted
data. Nested structures in ArrayToken or RecordToken translate into
correspondingly nested JSON output.

<p><a href="http://www.json.org/">http://www.json.org/</a>
- a description of the JSON format.</p>

@see JSONToToken
@author  Marten Lohstroh and Edward A. Lee
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (marten)
@Pt.AcceptedRating Red (chx)
 */
public class TokenToJSON extends Converter {

    /** Construct a TokenToJSON actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TokenToJSON(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a string that represents the argument in JSON format.
     *  If the argument is a RecordToken, then a JSON object is returned
     *  (a string that starts with '{' and ends with '}').
     *  If the argument is an ArrayToken, then a JSON array is returned
     *  (a string that starts with '[' and ends with ']').
     *  In both cases, the contents of the record and array are constructed
     *  recursively.
     *  If the argument is any of the ScalarTokens, then a string representation
     *  of the number or boolean is returned.
     *  If the argument is null or a nil token, then the string "null" is returned.
     *  If the argument is a StringToken, return its value (with quotation marks).
     *  If the argument is a MatrixToken, then the matrix is represented as a
     *  JSON array with the elements in row-scanned order (raster scan).
     *
     *  @param input Data to represent in JSON.
     *  @return a string that represent the input in JSON format
     *  @exception IllegalActionException If the Token found on the input cannot
     *  be expressed in JSON format
     */
    public static String constructJSON(Token input) throws IllegalActionException {
        if (input == null || input.isNil()) {
            return "null";
        } else if (input instanceof LongToken) {
            // The 'L' suffix is not supported in JSON.
            String result = input.toString();
            return result.substring(0, result.length() - 1);
        } else if (input instanceof ScalarToken
                || input instanceof StringToken
                || input instanceof DateToken) {
            return input.toString();
        } else if (input instanceof ArrayToken) {
            return _scanArrayToken((ArrayToken) input);
        } else if (input instanceof MatrixToken) {
            return _scanArrayToken(((MatrixToken)input).toArray());
        } else if (input instanceof RecordToken) {
            return _scanRecordToken((RecordToken) input);
        } else {
            throw new IllegalActionException(
                    "Conversion to JSON not supported for: " + input.toString());
        }
    }

    /** Read a Token from the input and produce a corresponding JSON-formatted
     *  string on the output.
     *  @exception IllegalActionException If the input Token cannot be
     *  converted to JSON.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, new StringToken(constructJSON(input.get(0))));
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Do not establish the usual default type constraints.
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Iterate over the elements in an ArrayToken and return a string starting
     *  with '[' and ending with ']' that has the JSON representation of the elements
     *  of the array separated by ",".
     *
     *  @param token An ArrayToken.
     *  @return A JSON representation of the array.
     *  @exception IllegalActionException If an element of the array cannot be expressed in JSON.
     */
    private static String _scanArrayToken(ArrayToken token) throws IllegalActionException {
        StringBuffer result = new StringBuffer("[");
        boolean first = true;
        for (Token element : token.arrayValue()) {
            if (!first) {
                result.append(",");
            }
            first = false;
            result.append(constructJSON(element));
        }
        result.append("]");
        return result.toString();
    }

    /** Iterate over the fields in an RecordToken and return a string starting
     *  with '{' and ending with '}' that has the JSON representation of the fields
     *  of the record separated by ", ".
     *
     *  @param token A RecordToken.
     *  @return A JSON representation of the record.
     *  @exception IllegalActionException If a field of the record cannot be expressed in JSON.
     */
    private static String _scanRecordToken(RecordToken token) throws IllegalActionException {
        StringBuffer result = new StringBuffer("{");
        boolean first = true;
        for (String label : token.labelSet()) {
            if (!first) {
                result.append(",");
            }
            first = false;
            result.append("\"");
            result.append(label);
            result.append("\":");
            result.append(constructJSON(token.get(label)));
        }
        result.append("}");
        return result.toString();
    }
}
