/* A token that contains a long integer.

 Copyright (c) 1998 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;

//////////////////////////////////////////////////////////////////////////
//// LongToken
/**
 * A token that contains an long integer.
 *
 * @author Neil Smyth, Yuhong Xiong
 * @version $Id$
 */
public class LongToken extends ScalarToken {

    /** Construct a token with long integer 0.
     */
    public LongToken() {
	_value = 0;
    }

    /** Construct a token with the specified value.
     */
    public LongToken(long value) {
	_value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Add the value of the argument Token to this Token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to add to this Token.
     *  @exception IllegalActionException Thrown if the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token add(ptolemy.data.Token tok) throws IllegalActionException {
        long typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.LOWER) {
                return tok.addR(this);
            } else if (tok instanceof LongToken) {
                long result = _value + ((LongToken)tok).getValue();
                return new LongToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                LongToken tmp = (LongToken)this.convert(tok);
                long result = _value + tmp.getValue();
                return new LongToken(result);
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

    /** Add the value of this Token to the argument Token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to add this Token to.
     *  @exception IllegalActionException Thrown if the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token addR(ptolemy.data.Token tok) throws IllegalActionException {
        LongToken tmp = (LongToken)this.convert(tok);
        long result = tmp.getValue() + _value;
        return new LongToken(result);
    }

    /** Used to convert Token types further down the type hierarchy to
     *  the type of this Token
     *  @param tok The token to be converted to a LongToken.
     *  @exception IllegalActionException Thrown if the conversion
     *  cannot be carried out in a lossless fashion.
     */
    public Token convert(Token tok) throws IllegalActionException{
        if (tok instanceof IntToken) {
            long result = ((IntToken)tok).longValue();
            return new LongToken(result);
        } else {
            try {
                IntToken res = (IntToken)(new IntToken()).convert(tok);
                return convert(res);
            } catch (Exception ex) {
                String str = "cannot convert from token type: ";
                str = str + tok.getClass().getName() + " to a ";
                throw new IllegalActionException(str + "LongToken");
            }
        }
    }


    /** Test the values of this Token and the argument Token for equality.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param tok The token to divide this Token by
     *  @exception IllegalActionException Thrown if the passed token is
     *  not of a type that can be compared with this Tokens value.
     */
    public BooleanToken equality(Token tok) throws IllegalActionException {
        long typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.LOWER) {
                return tok.equality(this);
            } else if (tok instanceof LongToken) {
                if ( _value == ((LongToken)tok).getValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else if (typeInfo == CPO.HIGHER) {
                LongToken tmp = (LongToken)this.convert(tok);
                if ( _value == tmp.getValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "equality method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Set the value in the token to the value represented by the
     *  specified string.
     *  @exception IllegalArgumentException The string does not contain
     *  a parsable number.
     */
    public void fromString(String init)
	    throws IllegalArgumentException {
	try {
	    _value = (Long.valueOf(init)).longValue();
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /** Get the long value contained by this token.
     */
    public long getValue() {
        return _value;
    }

    /** Return the value in the token as a long.
     */
    public long longValue() {
	return (long)_value;
    }

    /** Get the value of this Token modulo the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param tok The token to modulo this Token by
     *  @exception IllegalActionException Thrown if the passed token is
     *  not of a type that can be  used with modulo in a lossless fashion.
     */
    public Token modulo(Token tok) throws IllegalActionException {
        long typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.LOWER) {
                return tok.moduloR(this);
            } else if (tok instanceof LongToken) {
                long result = _value % ((LongToken)tok).getValue();
                return new LongToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                LongToken tmp = (LongToken)this.convert(tok);
                long result = _value % tmp.getValue();
                return new LongToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "modulo method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }
    /** Modulo the value of the argument Token by this Token.
     *  Type resolution also occurs here, with the returned Token
     *  type chosen to achieve a lossless conversion.
     *  @param tok The token to apply modulo to by the value of this Token.
     *  @exception IllegalActionException Thrown if the passed token
     *   is not of a type that can apply modulo by this Tokens value in
     *   a lossless fashion.
     */
    public Token moduloR(ptolemy.data.Token tok) throws IllegalActionException {
        LongToken tmp = (LongToken)this.convert(tok);
        long result = tmp.getValue() %  _value;
        return new LongToken(result);
    }


    /** Multiply the value of this Token with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param tok The token to multiply this Token by.
     *  @exception IllegalActionException Thrown if the passed token is
     *  not of a type that can be multiplied by this Tokens value in
     *  a lossless fashion.
     */
    public Token multiply(Token tok) throws IllegalActionException {
        long typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.LOWER) {
                return tok.multiplyR(this);
            } else if (tok instanceof LongToken) {
                long result = _value * ((LongToken)tok).getValue();
                return new LongToken(result);
            } else if (typeInfo == CPO.HIGHER){
                LongToken tmp = (LongToken)this.convert(tok);
                long result = _value * tmp.getValue();
                return new LongToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "multiply method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Multiply the value of the argument Token by this Token.
     *  Type resolution also occurs here, with the returned Token
     *  type chosen to achieve a lossless conversion.
     *  @param tok The token to be multiplied by the value of this Token.
     *  @exception IllegalActionException Thrown if the passed token
     *   is not of a type that can be multiplied by this Tokens value in
     *   a lossless fashion.
     */
    public Token multiplyR(ptolemy.data.Token tok) throws IllegalActionException {
        LongToken tmp = (LongToken)this.convert(tok);
        long result = tmp.getValue() * _value;
        return new LongToken(result);
    }

    /** Returns the multiplicative identity.
     */
    public Token one() {
        return new LongToken(1);
    }

    /** Set the value in the token
     *  @param d The new value for the token
     */
    public void setValue(long d) {
        _value = d;
    }

    /** Get the value contained in this Token as a String.
     */
    public String stringValue() {
        return Long.toString(_value);
    }

    /** Subtract the value of the argument Token from this Token. Type
     *  resolution also occurs here, with the returned Token type chosen to
     *  achieve a lossless conversion.
     *  @param tok The token to subtract to this Token.
     *  @exception IllegalActionException Thrown if the passed token is
     *   not of a type that can be subtracted from this Tokens value in
     *   a lossless fashion.
     */
    public Token subtract(ptolemy.data.Token tok) throws IllegalActionException {
        long typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.LOWER) {
                return tok.addR(this);
            } else if (tok instanceof LongToken) {
                long result = _value -  ((LongToken)tok).getValue();
                return new LongToken(result);
            } else if (typeInfo == CPO.HIGHER){
                LongToken tmp = (LongToken)this.convert(tok);
                long result = _value - tmp.getValue();
                return new LongToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "subtract method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Subtract the value of this Token from the argument Token. Type
     *  resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param tok The token to add this Token to.
     *  @exception IllegalActionException Thrown if the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token subtractR(ptolemy.data.Token tok) throws IllegalActionException {
        LongToken tmp = (LongToken)this.convert(tok);
        long result = _value - tmp.getValue();
        return new LongToken(result);
    }

    /** Return a representation of the token as a String.
     */
    public String toString() {
        String str = getClass().getName() + "(" + stringValue() + ")";
        return str;
    }

    /** Returns the additive identity.
     */
    public Token zero() {
        return new LongToken(0);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private long _value;
}

