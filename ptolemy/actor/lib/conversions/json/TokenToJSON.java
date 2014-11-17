/* Converts a Token to a string containing JSON-formatted data.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ptolemy.actor.lib.conversions.Converter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DateToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
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
correspondingly nested JSON output. If the input Token is not a structured
type, the value of the Token is wrapped into an array of size one.

<p><a href="http://www.json.org/">http://www.json.org/</a>
- a description of the JSON format.</p>

@see JSONToToken
@author  Marten Lohstroh
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

    /** Read a Token from the input and produce a corresponding JSON-formatted
     *  string on the output.
     *  @exception IllegalActionException If the input Token cannot be
     *  converted to JSON.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, constructJSONString(input.get(0)));
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

    /** Construct a StringToken that represents the input in JSON format.
     *  Populate a JSONObject or JSONArray by recursively scanning the input.
     *  If the input is not a structured type, wrap the the value in a
     *  JSONArray. Then convert the populated JSON structure into a StringToken
     *  and return it.
     *  @param input an arbitrary Token
     *  @return a StringToken that represent the input in JSON format
     *  @exception IllegalActionException If the Token found on the input cannot
     *  be expressed in JSON format
     */
    public Token constructJSONString(Token input) throws IllegalActionException {
        try {
            if (input.getType().equals(BaseType.NIL)) {
                return new StringToken("");
            }
            if (input instanceof ArrayToken) {
                return new StringToken(_scanArrayToken((ArrayToken) input)
                        .toString());
            } else if (input instanceof RecordToken) {
                return new StringToken(_scanRecordToken((RecordToken) input)
                        .toString());
            } else {
                // wrap single value into json array
                Object[] wrapper = new Object[1];
                wrapper[0] = _mapTokenToValue(input);
                return new StringToken(new JSONArray(wrapper).toString());
            }
        } catch (JSONException e) {
            throw new IllegalActionException(
                    "Unable to convert Token into JSON string.");
        }
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

    /** Map an given Token to the corresponding Java Object and return it.
     *  @param token An arbitrary Token
     *  @return An Object representing the value of the given input Token
     *  @exception JSONException If unable to instantiate a new JSONObject
     *  or JSONArray
     *  @exception IllegalActionException If the given Token cannot be
     *  expressed in JSON.
     */
    private Object _mapTokenToValue(Token token) throws IllegalActionException,
    JSONException {

        // The value can be any of these types:
        // Boolean, Number, String, or the JSONObject.NULL
        if (token instanceof RecordToken) {
            return _scanRecordToken((RecordToken) token);
        } else if (token instanceof ArrayToken) {
            return _scanArrayToken((ArrayToken) token);
        } else {
            Object o;
            if (token instanceof BooleanToken) {
                o = ((BooleanToken) token).booleanValue();
            } else if (token instanceof IntToken) {
                o = ((IntToken) token).intValue();
            } else if (token instanceof LongToken) {
                o = ((LongToken) token).longValue();
            } else if (token instanceof DoubleToken) {
                o = ((DoubleToken) token).doubleValue();
            } else if (token instanceof StringToken) {
                o = ((StringToken) token).stringValue();
            } else if (token instanceof DateToken) {
                o = ((DateToken) token).stringValue();
            } else if (token.equals(new ObjectToken(null))) {
                o = JSONObject.NULL;
            } else {
                throw new IllegalActionException("Unable to map token of type "
                        + token.getClass().toString() + " to value.");
            }
            return o;
        }
    }

    /** Iterate over the elements inside an ArrayToken and put them inside a
     *  new JSONArray. Apply recursion for ArrayTokens and RecordTokens.
     *
     *  @param token An ArrayToken
     *  @return An JSONArray containing the values corresponding to those found
     *  in the given ArrayToken
     *  @exception JSONException If unable to instantiate a new JSONObject
     *  or JSONArray
     *  @exception IllegalActionException If a value inside the given
     *  ArrayToken cannot be expressed in JSON.
     */
    private JSONArray _scanArrayToken(ArrayToken token) throws JSONException,
    IllegalActionException {
        int i = 0;
        Object[] array = new Object[token.length()];

        for (Token t : token.arrayValue()) {
            array[i] = _mapTokenToValue(t);
            i++;
        }
        return new JSONArray(array);
    }

    /** Iterate over the elements inside an RecordToken and put them inside a
     *  new JSONObject. Apply recursion for ArrayTokens and RecordTokens.
     *
     *  @param token An RecordToken
     *  @return An JSONArray containing the values corresponding to those found
     *  in the given RecordToken
     *  @exception JSONException If unable to instantiate a new JSONObject
     *  or JSONArray
     *  @exception IllegalActionException If a value inside the given
     *  RecordToken cannot be expressed in JSON.
     */
    private JSONObject _scanRecordToken(RecordToken token)
            throws JSONException, IllegalActionException {
        JSONObject object = new JSONObject();
        for (String label : token.labelSet()) {
            object.put(label, _mapTokenToValue(token.get(label)));
        }
        return object;
    }
}
