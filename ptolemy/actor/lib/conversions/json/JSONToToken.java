/* Converts a string containing JSON-formatted data to a Token.

 Copyright (c) 2012-2013 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ptolemy.actor.lib.conversions.Converter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
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
An actor that converts a string containing JSON-formatted data into a Token.
Depending on the top level structure found in the JSON string, it produces
either a RecordToken or an ArrayToken on its output port. Nested structures
in the JSON data will translate to correspondingly nested structures in the
Token.
<p>The JSONObject parser processes values as follows:
Delimited values are always parsed as a String. Values that are not delimited
are tested in the order noted below. The first test that succeeds determines
the type.
<ul>
  <li>'true' | 'false' => Boolean (case insensitive)</li>
  <li>'null' => JSONObject.NULL (case insensitive)</li>
  <li>'0x..' => Integer (hexadecimal)</li>
  <li>x'.'y | exponent encoded => Double</li>
  <li>x => Long, or Integer if value remains the same after conversion</li>
</ul>
If non of the above apply, the value is interpreted as a String.</p>
<p>Note that JSON allows array elements to have different types, whereas the
<code>ArrayToken</code> does not. Conversion of such mixed array will result
in an <code>ArrayToken</code> of which the types of all elements are cast to
the least upper bound of the entire collection.</p>

<p><a href="http://www.json.org/">http://www.json.org/</a>
- a description of the JSON format.</p>

@see TokenToJSON
@author  Marten Lohstroh, Contributor: Beth Latronico
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (marten)
@Pt.AcceptedRating Red (chx)
 */
public class JSONToToken extends Converter {

    /** Construct a JSONToToken actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JSONToToken(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a JSON-formatted String of name/value pairs from the input
     *  and produce a corresponding array or record on the output.
     *  @exception IllegalActionException If the input string does not
     *  contain properly formatted JSON.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, _parseJSON(((StringToken) input.get(0)).stringValue()));
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

    /**
     * Do not establish the usual default type constraints.
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

    /**
     * Parse the input string by instantiating a new JSONObject, then translate
     * the JSONObject to a Token and return it.
     * @param input An input string that contains JSON-formatted data
     * @return A Token that represents the JSON-formatted input string
     * @exception IllegalActionException If the given input string cannot be parsed.
     */
    protected Token _parseJSON(String input) throws IllegalActionException {
        try {
            input = input.trim();
            if (input.length() == 0 || input.equals("nil")) {
                return Token.NIL;
            }
            if (input.charAt(0) == '{') {
                return _scanJSONObject(new JSONObject(input));
            } else {
                return _scanJSONArray(new JSONArray(input));
            }
        } catch (JSONException e) {
            throw new IllegalActionException(this, e,
                    "Unable to parse JSON data: " + input);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Map an given value to the appropriate Token class and return the
     *  new Token.
     *  @param value An Object representing some value
     *  @return A Token representing the given value
     *  @exception JSONException If a non-existent value is requested from the
     *  given object or array.
     *  @exception IllegalActionException Upon failing to instantiate a new
     *  Token.
     */
    private Token _mapValueToToken(Object value) throws IllegalActionException,
            JSONException {

        // The value can be any of these types:
        // Boolean, Number, String, or the JSONObject.NULL
        if (value instanceof JSONArray) {
            return _scanJSONArray((JSONArray) value);
        } else if (value instanceof JSONObject) {
            return _scanJSONObject((JSONObject) value);
        } else {
            Token t;
            if (value instanceof Boolean) {
                t = new BooleanToken((Boolean) value);
            } else if (value instanceof Integer) {
                t = new IntToken((Integer) value);
            } else if (value instanceof Long) {
                t = new LongToken((Long) value);
            } else if (value instanceof Double) {
                t = new DoubleToken((Double) value);
            } else if (value instanceof String) {
                t = new StringToken((String) value);
            } else if (value.equals(JSONObject.NULL)) {
                t = new ObjectToken(null);
            } else {
                throw new IllegalActionException("Unable to map value of "
                        + value.getClass().toString() + " to token.");
            }
            return t;

        }

    }

    /** Iterate over the elements inside a JSONArray and put them inside a
     *  new ArrayToken. Apply recursion for JSONObjects and JSONArrays.
     *  When a new ArrayToken is instantiated, all elements are converted to
     *  the least upper bound of the types found in the JSONArray. If the
     *  conversion fails, an IllegalActionException is thrown.
     *  @param array A JSONArray
     *  @return An ArrayToken containing the values that corresponding to those
     *  found in the given array
     *  @exception JSONException If a non-existent value is requested from the
     *  given array.
     *  @exception IllegalActionException Upon failing to instantiate a new
     *  ArrayToken.
     */
    private ArrayToken _scanJSONArray(JSONArray array) throws JSONException,
            IllegalActionException {
        ArrayList<Token> values = new ArrayList<Token>();

        Object value;

        for (int i = 0; i < array.length(); ++i) {
            value = array.get(i);
            values.add(_mapValueToToken(value));
        }

        // If there are no values, ArrayToken() requires a special constructor
        if (values.isEmpty()) {
            return new ArrayToken(BaseType.UNKNOWN);
        } else {
            return new ArrayToken(values.toArray(new Token[values.size()]));
        }
    }

    /** Iterate over the elements inside a JSONObject and put them inside a
     *  new RecordToken. Apply recursion for JSONObjects and JSONArrays.
     *
     *  @param object A JSONObject
     *  @return A RecordToken containing fields and values that correspond
     *  with those found in the given object
     *  @exception JSONException If a non-existent value is requested from the
     *  given object.
     *  @exception IllegalActionException Upon failing to instantiate a new
     *  RecordToken.
     */
    private RecordToken _scanJSONObject(JSONObject object)
            throws IllegalActionException, JSONException {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Token> values = new ArrayList<Token>();

        Object value;
        String name;
        Iterator<?> i = object.keys();

        while (i.hasNext()) {
            name = (String) i.next();
            value = object.get(name);
            names.add(name);
            values.add(_mapValueToToken(value));
        }

        return new RecordToken(names.toArray(new String[names.size()]),
                values.toArray(new Token[values.size()]));
    }

}
