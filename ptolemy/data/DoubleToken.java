/* A token that contains a double precision number.

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
import java.text.NumberFormat;

//////////////////////////////////////////////////////////////////////////
//// DoubleToken
/**
 * A token that contains a double precision number.
 *
 * @author Yuhong Xiong, Neil Smyth
 * @see ptolemy.data.Token
 * @see java.text.NumberFormat
 * @version $Id$
 */
public class DoubleToken extends ScalarToken {

    /** Construct a token with double 0.0.
     */
    public DoubleToken() {
	_value = 0.0;
    }

    /** Construct a token with the specified value.
     */
    public DoubleToken(double value) {
	_value = value;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////


    /** Add the value of the argument Token to this Token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     * FIXME: what do do about long in the next six methods?
     *  @param tok The token to add to this Token.
     *  @exception IllegalActionException Thrown if the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token add(ptolemy.data.Token tok) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.STRICT_LESS) {
                return tok.addR(this);
            } else if (tok instanceof DoubleToken) {
                double result = _value + ((DoubleToken)tok).getValue();
                return new DoubleToken(result);
            } else  if (typeInfo == CPO.STRICT_GREATER) {
                DoubleToken tmp = this.convert(tok);
                double result = _value + tmp.getValue();
                return new DoubleToken(result);
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
        DoubleToken tmp = this.convert(tok);
        double result = tmp.getValue() + _value;
        return new DoubleToken(result);
    }

    // Return a reference to a Complex. The real part of the Complex
    // is the value in the token, the imaginary part is set to 0.
    // FIXME: finish after the Complex class is moved to this package.

    //    public Complex complexValue() {
    //    }

    /** Used to convert Token types further down the type hierarchy to
     *  the type of this Token
     *  @param tok The token to be converted to a DoubleToken.
     *  @exception IllegalActionException Thrown if the conversion
     *  cannot be carried out in a lossless fashion.
     */
    static public DoubleToken convert(Token tok) throws IllegalActionException{
        if (tok instanceof IntToken) {
            double result = ((IntToken)tok).doubleValue();
            return new DoubleToken(result);
        } else {
            try {
                IntToken res = IntToken.convert(tok);
                return convert(res);
            } catch (Exception ex) {
                String str = "cannot convert from token type: ";
                str = str + tok.getClass().getName() + " to a ";
                throw new IllegalActionException(str + "DoubleToken");
            }
        }
    }

    /** Divide the value of this Token with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param tok The token to divide this Token by
     *  @exception IllegalActionException Thrown if the passed token is
     *  not of a type that can be divide this Tokens value by in a
     *  lossless fashion.
     */
    public Token divide(Token tok) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.STRICT_LESS) {
                return tok.divideR(this);
            } else if (tok instanceof DoubleToken) {
                double result = _value / ((DoubleToken)tok).getValue();
                return new DoubleToken(result);
            } else if (typeInfo == CPO.STRICT_GREATER) {
                DoubleToken tmp = this.convert(tok);
                double result = _value / tmp.getValue();
                return new DoubleToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "divide method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Divide the value of the argument Token by this Token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to be divided by the value of this Token.
     *  @exception IllegalActionException Thrown if the passed token
     *   is not of a type that can be divided by this Tokens value in
     *   a lossless fashion.
     */
    public Token divideR(ptolemy.data.Token tok) throws IllegalActionException {
        DoubleToken tmp = this.convert(tok);
        double result = tmp.getValue() / _value;
        return new DoubleToken(result);
    }

    /** Return the value in the token as a double.
     */
    public double doubleValue() {
	return _value;
    }

    /** Test the values of this Token and the argument Token for equality.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param tok The token to divide this Token by
     *  @exception IllegalActionException Thrown if the passed token is
     *  not of a type that can be compared with this Tokens value.
     */
    public BooleanToken equality(Token tok) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.STRICT_LESS) {
                return tok.equality(this);
            } else if (tok instanceof DoubleToken) {
                if ( _value == ((DoubleToken)tok).getValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else if (typeInfo == CPO.STRICT_GREATER) {
                DoubleToken tmp = this.convert(tok);
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
	    _value = (Double.valueOf(init)).doubleValue();
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /** Get the double value contained by this token.
     */
    public double getValue() {
        return _value;
    }

    /** Get the value of this Token modulo the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param tok The token to modulo this Token by
     *  @exception IllegalActionException Thrown if the passed token is
     *  not of a type that can be  used with modulo in a lossless fashion.
     */
    public Token modulo(Token tok) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.STRICT_LESS) {
                return tok.moduloR(this);
            } else if (tok instanceof DoubleToken) {
                double result = _value % ((DoubleToken)tok).getValue();
                return new DoubleToken(result);
            } else if (typeInfo == CPO.STRICT_GREATER) {
                DoubleToken tmp = this.convert(tok);
                double result = _value % tmp.getValue();
                return new DoubleToken(result);
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
        DoubleToken tmp = this.convert(tok);
        double result = tmp.getValue() %  _value;
        return new DoubleToken(result);
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
        int typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.STRICT_LESS) {
                return tok.multiplyR(this);
            } else if (tok instanceof DoubleToken) {
                double result = _value * ((DoubleToken)tok).getValue();
                return new DoubleToken(result);
            } else if (typeInfo == CPO.STRICT_GREATER) {
                DoubleToken tmp = this.convert(tok);
                double result = _value * tmp.getValue();
                return new DoubleToken(result);
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
        DoubleToken tmp = this.convert(tok);
        double result = tmp.getValue() * _value;
        return new DoubleToken(result);
    }

    /** Returns the multiplicativeive identity.
     */
    public Token one() {
        return new DoubleToken(1.0);
    }

    /** Set the value in the token
     *  @param d The new value for the token
     */
    public void setValue(double d) {
        _value = d;
    }

    /** Get the value contained in this Token as a String.
     *  It uses java.text.NumberFormat to format the number.
     */
    public String stringValue() {
        NumberFormat nf = NumberFormat.getNumberInstance();
        return nf.format(_value);
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
        int typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.STRICT_LESS) {
                return tok.addR(this);
            } else if (tok instanceof DoubleToken) {
                double result = _value -  ((DoubleToken)tok).getValue();
                return new DoubleToken(result);
            } else if (typeInfo == CPO.STRICT_GREATER) {
                DoubleToken tmp = this.convert(tok);
                double result = _value - tmp.getValue();
                return new DoubleToken(result);
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
        DoubleToken tmp = this.convert(tok);
        double result = _value - tmp.getValue();
        return new DoubleToken(result);
    }

    /** Return a description of the token as a string.
     *  In this base class, we return the fully qualified class name.
     */
    public String toString() {
        String str =  getClass().getName() + "(" + stringValue() + ")";
        return str;
    }

    /** Returns the additive identity.
     */
    public Token zero() {
        return new DoubleToken(0);
    }


    //////////////////////////////////////////////////////////////////////////
    ////                        private variables                         ////
    private double _value;
}

