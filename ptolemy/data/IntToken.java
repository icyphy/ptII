/* A token that contains an integer number.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu, nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.data.type.*;

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
     */
    public IntToken(String init) throws IllegalArgumentException {
        try {
            _value = (Integer.valueOf(init)).intValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an IntToken containing the absolute value of the
     *  value of this token.
     *  @return An IntToken.
     */
    public ScalarToken absolute() {
        return _value >= 0 ? this : new IntToken(-_value);
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned Token type chosen to achieve a lossless conversion.
     *  @param rightArg The token to add to this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token add(ptolemy.data.Token rightArg)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArg);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightArg.addReverse(this);
            } else if (rightArg instanceof IntToken) {
                int result = _value + ((IntToken)rightArg).intValue();
                return new IntToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                IntToken tmp = (IntToken)this.convert(rightArg);
                int result = _value + tmp.intValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "add method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + rightArg.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param leftArg The token to add this Token to.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token addReverse(ptolemy.data.Token leftArg)
            throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(leftArg);
        int result = tmp.intValue() + _value;
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

    /** Convert the specified token into an instance of IntToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of IntToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below IntToken in the type hierarchy, it is converted to
     *  an instance of IntToken or one of the subclasses of
     *  IntToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a IntToken.
     *  @return A IntToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
            throws IllegalActionException {

        int compare = TypeLattice.compare(new IntToken(), token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("IntToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with IntToken in the type " +
                    "hierarchy.");
        }

        if (token instanceof IntToken) {
            return token;
        }
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a DoubleToken");
    }

    /** Return a new Token whose value is the value of this token
     *  divided by the value of the argument token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion. If two integers are divided,
     *  the result will be an integer which is the quotient.
     *  @param divisor The token to divide this Token by
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be divide this Tokens value by in a
     *  lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token divide(Token divisor) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, divisor);
        try {
            if (typeInfo == CPO.LOWER) {
                return divisor.divideReverse(this);
            } else if (divisor instanceof IntToken) {
                return new IntToken(_value / ((IntToken)divisor).intValue());
            } else if (typeInfo == CPO.HIGHER) {
                IntToken tmp = (IntToken)this.convert(divisor);
                return new IntToken(_value / (tmp.intValue()));
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
    public Token divideReverse(Token dividend)
            throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(dividend);
        return new IntToken(tmp.intValue() / _value);
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    public double doubleValue() {
        return (double)_value;
    }

    /** Return the type of this token.
     *  @return BaseType.INT
     */
    public Type getType() {
        return BaseType.INT;
    }

    /** Test the value of this Token and the argument Token for equality.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param token The token with which to test equality.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be compared with this Tokens value.
     *  @return A new Token containing the result.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, token);
        try {
            if (typeInfo == CPO.LOWER) {
                return token.isEqualTo(this);
            } else if (token instanceof IntToken) {
                if ( _value == ((IntToken)token).intValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else if (typeInfo == CPO.HIGHER) {
                IntToken tmp = (IntToken)this.convert(token);
                if ( _value == tmp.intValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "isEqualTo method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + token.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Check if the value of this token is strictly less than that of the
     *  argument token.
     *  @param arg A ScalarToken.
     *  @return A BooleanToken with value true if this token is strictly
     *   less than the argument.
     *  @exception IllegalActionException If the type of the argument token
     *   is incomparable with the type of this token.
     */
    public BooleanToken isLessThan(ScalarToken arg)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, arg);
        if (typeInfo == CPO.INCOMPARABLE) {
            throw new IllegalActionException("IntToken.isLessThan: The type" +
                    " of the argument token is incomparable with the type of " +
                    "this token. argType: " + arg.getType());
        }

        if (typeInfo == CPO.LOWER) {
            return arg.isLessThan(this);
        }

        // Argument type is lower or equal to this token.
        ScalarToken intArg = arg;
        if (typeInfo == CPO.HIGHER) {
            intArg = (ScalarToken)convert(arg);
        }

        if (_value < intArg.intValue()) {
            return new BooleanToken(true);
        }
        return new BooleanToken(false);
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
     *  @param rightArg The token to modulo this Token by.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be  used with modulo in a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token modulo(Token rightArg) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArg);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightArg.moduloReverse(this);
            } else if (rightArg instanceof IntToken) {
                int result = _value % ((IntToken)rightArg).intValue();
                return new IntToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                IntToken tmp = (IntToken)this.convert(rightArg);
                int result = _value % tmp.intValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "modulo method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + rightArg.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of the argument token
     *  modulo the value of this token.
     *  Type resolution also occurs here, with the returned Token
     *  type chosen to achieve a lossless conversion.
     *  @param leftArg The token to apply modulo to by the value of this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can apply modulo by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token moduloReverse(Token leftArg) throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(leftArg);
        int result = tmp.intValue() %  _value;
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
        int typeInfo = TypeLattice.compare(this, rightFactor);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightFactor.multiplyReverse(this);
            } else if (rightFactor instanceof IntToken) {
                int result = _value * ((IntToken)rightFactor).intValue();
                return new IntToken(result);
            } else if (typeInfo == CPO.HIGHER){
                IntToken tmp = (IntToken)this.convert(rightFactor);
                int result = _value * tmp.intValue();
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
    public Token multiplyReverse(Token leftFactor)
            throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(leftFactor);
        int result = tmp.intValue() * _value;
        return new IntToken(result);
    }

    /** Returns a new Token representing the multiplicative identity.
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() {
        return new IntToken(1);
    }

    /** Return the value contained in this Token as a String.
     *  @return A String.
     *  @deprecated Use toString() instead.
     */
    public String stringValue() {
        return toString();
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
        int typeInfo = TypeLattice.compare(this, rightArg);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightArg.subtractReverse(this);
            } else if (rightArg instanceof IntToken) {
                int result = _value -  ((IntToken)rightArg).intValue();
                return new IntToken(result);
            } else if (typeInfo == CPO.HIGHER){
                IntToken tmp = (IntToken)this.convert(rightArg);
                int result = _value - tmp.intValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new IllegalActionException("IntToken: subtract method not" +
                    " supported between: " + getClass().getName() + " and " +
                    rightArg.getClass().getName() + ",: " + ex.getMessage());
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
    public Token subtractReverse(Token leftArg) throws IllegalActionException {
        IntToken tmp = (IntToken)this.convert(leftArg);
        int result = tmp.intValue() - _value;
        return new IntToken(result);
    }

    /** Return the value contained in this Token as a String.
     *  @return A String.
     */
    public String toString() {
        return Integer.toString(_value);
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
