/* A particle that contains a string.

 Copyright (c) 1997- The Regents of the University of California.
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

package ptolemy.data;

import ptolemy.kernel.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;

//////////////////////////////////////////////////////////////////////////
//// StringToken
/**
A token that contains a string, or more specifically, a reference
to an instance of String.  The reference is never null, although it may
be an empty string ("").
Note that when this token is cloned, the clone will refer to exactly
the same String object.  However, a String object in Java is immutable,
so there is no risk when two tokens refer to the same string that
one of the strings will be changed.

@author Edward A. Lee, Neil Smyth
@version $Id$
*/
public class StringToken extends Token {

    /** Construct a token with an empty string.
     */
    public StringToken() {
        this("");
    }

    /** Construct a token with the specified string.
     */
    public StringToken(String value) {
        if (value != null) {
            _value = value;
        } else {
            _value = new String("");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new StringToken whose value is the string value
     *  of the argument token appended to the String contained in
     *  this Token.
     *  @param token The token to add to this Token
     *  @exception IllegalActionException If the passed token is
     *   not of a type that can be added to this Tokens value in a
     *   lossless fashion.
     *  @return A new StringToken containing the result.
     */
    public Token add(Token token) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, token);
        try {
            if (typeInfo == CPO.HIGHER) {
                StringToken tmp = (StringToken)this.convert(token);
                String result = _value + tmp.getValue();
                return new StringToken(result);
            } else if (token instanceof StringToken) {
                String result = _value + ((StringToken)token).getValue();
                return new StringToken(result);
            } else if (typeInfo == CPO.LOWER) {
                return token.addR(this);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "add method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + token.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new StringToken whose value is the string value
     *  of the argument token prepended to the String contained in
     *  this Token.
     *  @param token The token to concatenate this Token to.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new StringToken containing the result.
     */
    public Token addR(ptolemy.data.Token token) throws IllegalActionException {
        StringToken tmp = (StringToken)this.convert(token);
        String result = tmp.getValue() + _value;
        return new StringToken(result);
    }

    /** Convert the specified token into an instance of StringToken.
     *  This method does lossly conversion.
     *  If the argument is already an instance of StringToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below StringToken in the type hierarchy, it is converted to
     *  an instance of StringToken or one of the subclasses of
     *  StringToken and returned. If non of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a StringToken.
     *  @return A StringToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeCPO.compare(new StringToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("StringToken.convert: " +
                "type of argument: " + token.getClass().getName() +
                "is higher or incomparable with StringToken in the " +
	 	"type hierarchy.");
	}

	if (token instanceof StringToken) {
	    return token;
	}

	if (token instanceof MatrixToken || token instanceof ScalarToken ||
	    token instanceof BooleanToken) {
	    String str = token.stringValue();
	    return new StringToken(str);
	}

	// FIXME: token must be user defined. what to do?
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
		"StringToken.");
    }

    /** Lexicographically test the values of this Token and the
     *  argument Token for equality. Return a new BooleanToken containing
     *  the result.
     *  @param token The token to lexicographically compare the value this
     *  Token with.
     *  @return BooleanToken indicating result of comparison.
     *  @exception IllegalActionException Thrown if the passed token
     *  is not of a type that can be compared this Tokens value.
     */
    public BooleanToken equals(Token token) throws IllegalActionException {
        if (token instanceof StringToken) {
            if ( _value.compareTo(token.stringValue()) == 0) {
                return new BooleanToken(true);
            } else {
                return new BooleanToken(false);
            }
        } else {
            String str = "equality method not supported between ";
            str = str + this.getClass().getName();
            str = str + " and " + token.getClass().getName();
            throw new IllegalActionException(str);
        }
    }

    /**  Get the value of the String currently contained in this token.
     *   @return The value currently contained.
     */
    public String getValue() {
        return _value;
    }

    /** Return the value of this Token as a String.
     *  @return The value of this Token as a String.
     */
    public String stringValue() {
        return _value;
    }

    /** Return the string description of the object.  If there is no such
     *  object, then return a description of the token.
     *  @return The String description of this token.
     */
    public String toString() {
        String str = getClass().getName() + "(" + _value + ")";
        return str;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private String _value;
}
