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
public class StringToken extends ObjectToken {

    /** Contruct a token with an empty string.
     */
    public StringToken() {
        this("");
    }

    /** Contruct a token with the specified string.
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

    /** Add the value of the argument Token to this Token. The value of
     *  the argument token is converted to a String and concatenated
     *  with the value stored in this token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to add to this Token
     *  @exception IllegalActionException Thrown if the passed token is
     *  not of a type that can be added to this Tokens value in a
     *  lossless fashion.
     */
    public Token add(Token tok) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.STRICT_GREATER) {
                StringToken tmp = (StringToken)this.convert(tok);
                String result = _value + tmp.getValue();
                return new StringToken(result);
            } else if (tok instanceof StringToken) {
                String result = _value + ((StringToken)tok).getValue();
                return new StringToken(result);
            } else if (typeInfo == CPO.STRICT_LESS) {
                return tok.addR(this);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "add method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Add the value of this Token to the argument Token. The value of
     *  the argument token is converted to a String and concatenated
     *  with the value stored in this token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to concatenate this Token to.
     *  @exception IllegalActionException Thrown if the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token addR(ptolemy.data.Token tok) throws IllegalActionException {
        StringToken tmp = (StringToken)this.convert(tok);
        String result = tmp.getValue() + _value;
        return new StringToken(result);
    }

    /** Used to convert Token types further down the type hierarchy to
     *  the a StringToken.
     *  @param tok The token to be converted to a StringToken.
     *  @exception IllegalActionException Thrown if the conversion
     *  cannot be carried out in a lossless fashion.
     *  FIXME: does not currently support the lossless hierarchy fully
     *  as do not currently have LongMatrix, CompleMatrix or ComplexToken
     *  classes. It skips down to the DoubleToken part of the lossless
     *  type hierarchy.
     */
    public Token convert(Token tok) throws IllegalActionException{
        if (tok instanceof BooleanToken) {
            String result = ((BooleanToken)tok).stringValue();
            return new StringToken(result);
        } else if (tok instanceof DoubleToken) {
            String result = ((DoubleToken)tok).stringValue();
            return new StringToken(result);
        } else {
            try {
                DoubleToken res = (DoubleToken)(new DoubleToken()).convert(tok);
                return convert(res);
            } catch (Exception ex) {
                String str = "cannot convert from token type: ";
                str = str + tok.getClass().getName() + " to a ";
                throw new IllegalActionException(str + "DoubleToken");
            }
        }
    }

    /** Lexicographically test the values of this Token and the
     *  argument Token for equality.
     *  @param a The token to lexicographically compare the value this
     *  Token with.
     *  @return BooleanToken indicating result of comparision.
     *  @exception IllegalActionException Thrown if the passed token
     *  is not of a type that can be compared this Tokens value.
     */
    public BooleanToken equality(Token a) throws IllegalActionException {
        if (a instanceof StringToken) {
            if ( _value.compareTo(a.stringValue()) == 0) {
                return new BooleanToken(true);
            } else {
                return new BooleanToken(false);
            }
        } else {
            String str = "equality method not supported between ";
            str = str + this.getClass().getName();
            str = str + " and " + a.getClass().getName();
            throw new IllegalActionException(str);
        }
    }


    /** Set the value of the token to the specified string.
     *  If the argument is null, then the value is set to an empty string
     *  rather than null.
     *  @param init The String to be stored in this token.
     */
    public void fromString(String init) {
        if (init != null) {
            _value = init;
        } else {
            _value = new String("");
        }
    }

    /**  Get the value of the String currently contained in this token.
     *   @return The value currently contained.
     */
    public String getValue() {
        return _value;
    }


    /** Set the value of the token to be a reference to the specified string.
     *  If the argument is null, then the value is set to an empty string
     *  rather than null.
     *  @param value The String to store in this token.
     */
    public void setValue(String value) {
        if (value != null) {
            _value = value;
        } else {
            _value = new String("");
        }
    }

    /** Return the value of this Token as a String
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
