/* A token that contains an integer number.

 Copyright (c) 1997 The Regents of the University of California.
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
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// IntegerToken
/**
 * A token that contains an integer number.
 *
 * @author Neil Smyth, Yuhong Xiong
 * @version $Id$
 */
public class IntToken extends ScalarToken {

    /** Construct a token with integer 0.
     */
    public IntToken() {
        _value = 0;
    }

    /** Construct a token with the specified value.
     */
    public IntToken(int value) {
	_value = value;
    }

    /** Construct an IntToken from the specified string.
     *  @exception IllegalArgumentException If the Token could not
     *   be created with the given String.
    public IntToken(String init) throws IllegalArgumentException {
	try {
	    _value = (Integer.valueOf(init)).intValue();
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }
    */

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned Token type chosen to achieve a lossless conversion.
     *  @param tok The token to add to this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token add(ptolemy.data.Token tok) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.LOWER) {
                return tok.addR(this);
            } else if (tok instanceof IntToken) {
                int result = _value + ((IntToken)tok).getValue();
                return new IntToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                IntToken tmp = (IntToken)this.convert(tok);
                int result = _value + tmp.getValue();
                return new IntToken(result);
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

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to add this Token to.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token addR(ptolemy.data.Token tok) throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(tok);
        int result = tmp.getValue() + _value;
        return new IntToken(result);
    }

    /** Return the value of this token as a Complex. The real part
     *  of the Complex is the value of this token, the imaginary part
     *  is set to 0.
     *  @return A Complex.
     */
    public Complex complexValue() {
	return new Complex((double)_value);
    }

    /** Used to convert Token types further down the type hierarchy to
     *  the type of this Token. There are no types below IntToken in the
     *  lossless type hierarchy, so throw an exception if reach here.
     *  @param tok The token to be converted to a IntToken.
     *  @exception IllegalActionException If the conversion
     *  cannot be carried out in a lossless fashion.
     *  @return A new Token containing the argument Token converted
     *   to the type of this Token.
     */
    public Token convert(Token tok) throws IllegalActionException{
        String str = "cannot convert from token type: ";
        str = str + tok.getClass().getName() + " to a ";
        throw new IllegalActionException(str + "IntToken");
    }

    /** Return a new Token whose value is the value of this token
     *  divided by the value of the argument token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion. If two integers are divided,
     *  the result may or may not be an integer.
     *  @param divisor The token to divide this Token by
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be divide this Tokens value by in a
     *  lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token divide(Token divisor) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, divisor);
        try {
            if (typeInfo == CPO.LOWER) {
                return divisor.divideR(this);
            } else if (divisor instanceof IntToken) {
                double result = _value / ((IntToken)divisor).doubleValue();
                if ((result - (int)result) == 0) {
                    return new IntToken((int)result);
                } else {
                    return new DoubleToken(result);
                }
            } else if (typeInfo == CPO.HIGHER) {
                IntToken tmp = (IntToken)this.convert(divisor);
                double result = _value / tmp.doubleValue();
                if ((result - (int)result) == 0) {
                    return new IntToken((int)result);
                } else {
                    return new DoubleToken(result);
                }
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "divide method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + divisor.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of the argument token
     *  divided by the value of this token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param dividend The token to be divided by the value of this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be divided by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token divideR(Token dividend) throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(dividend);
        double result = tmp.getValue() / _value;
        if (result == (int)result) {
            return new IntToken((int)result);
        } else {
            return new DoubleToken(result);
        }
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    public double doubleValue() {
        return (double)_value;
    }

    /** Test the values of this Token and the argument Token for equality.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param token The token with which to test equality.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be compared with this Tokens value.
     *  @return A new Token containing the result.
     */
    public BooleanToken equals(Token token) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, token);
        try {
            if (typeInfo == CPO.LOWER) {
                return token.equals(this);
            } else if (token instanceof IntToken) {
                if ( _value == ((IntToken)token).getValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else if (typeInfo == CPO.HIGHER) {
                IntToken tmp = (IntToken)this.convert(token);
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
            str = str + token.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Get the int value contained by this token.
     *  @return The int value contained in this token.
     */
    public int getValue() {
        return _value;
    }

    /** Return the value in the token as a int.
     *  @return The int value contained in this token.
     */
    public int intValue() {
	return _value;
    }

    /** Return the value in the token as a int.
     *  @return The int  value contained in this token as a long.
     */
    public long longValue() {
	return (long)_value;
    }

    /** Return a new Token whose value is the value of this token
     *  modulo the value of the argument token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param token The token to modulo this Token by.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be  used with modulo in a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token modulo(Token token) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, token);
        try {
            if (typeInfo == CPO.LOWER) {
                return token.moduloR(this);
            } else if (token instanceof IntToken) {
                int result = _value % ((IntToken)token).getValue();
                return new IntToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                IntToken tmp = (IntToken)this.convert(token);
                int result = _value % tmp.getValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "modulo method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + token.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }
    /** Return a new Token whose value is the value of the argument token
     *  modulo the value of this token.
     *  Type resolution also occurs here, with the returned Token
     *  type chosen to achieve a lossless conversion.
     *  @param token The token to apply modulo to by the value of this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can apply modulo by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token moduloR(Token token) throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(token);
        int result = tmp.getValue() %  _value;
        return new IntToken(result);
    }


    /** Return a new Token whose value is the value of this Token
     *  multiplied with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param rightFactor The token to multiply this Token by.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be multiplied by this Tokens value in
     *  a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token multiply(Token rightFactor) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, rightFactor);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightFactor.multiplyR(this);
            } else if (rightFactor instanceof IntToken) {
                int result = _value * ((IntToken)rightFactor).getValue();
                return new IntToken(result);
            } else if (typeInfo == CPO.HIGHER){
                IntToken tmp = (IntToken)this.convert(rightFactor);
                int result = _value * tmp.getValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "multiply method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + rightFactor.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of the argument Token
     *  multiplied with the value of this Token.
     *  Type resolution also occurs here, with the returned Token
     *  type chosen to achieve a lossless conversion.
     *  @param leftFactor The token to be multiplied by the value of
     *   this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be multiplied by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token multiplyR(Token leftFactor) throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(leftFactor);
        int result = tmp.getValue() * _value;
        return new IntToken(result);
    }

    /** Returns a new Token representing the multiplicative identity.
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() {
        return new IntToken(1);
    }

    /** Get the value contained in this Token as a String.
     *  @return The value contained in this token as a String.
     */
    public String stringValue() {
        return Integer.toString(_value);
    }

    /** Return a new Token whose value is the value of the argument Token
     *  subtracted from the value of this Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArg The token to subtract to this Token.
     *  @exception IllegalActionException If the passed token is
     *   not of a type that can be subtracted from this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token subtract(Token rightArg) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, rightArg);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightArg.addR(this);
            } else if (rightArg instanceof IntToken) {
                int result = _value -  ((IntToken)rightArg).getValue();
                return new IntToken(result);
            } else if (typeInfo == CPO.HIGHER){
                IntToken tmp = (IntToken)this.convert(rightArg);
                int result = _value - tmp.getValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "subtract method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + rightArg.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param leftArg The token to add this Token to.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token subtractR(Token leftArg) throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(leftArg);
        int result = _value - tmp.getValue();
        return new IntToken(result);
    }

    /** Return a representation of the token as a String.
     *  @return A String representation of this token.
     */
    public String toString() {
        String str = getClass().getName() + "(" + stringValue() + ")";
        return str;
    }

    /** Returns a new token representing the additive identity.
     *  @return A new Token containing the additive identity.
     */
    public Token zero() {
        return new IntToken(0);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _value;
}

