/* A token that contains a double precision number.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;

import java.text.DecimalFormat;

//////////////////////////////////////////////////////////////////////////
//// DoubleToken
/**
A token that contains a double precision number.
<p>
Note that a double cannot be losslessly converted to a long, and vice
versa, as both have 64 bit representations in Java.

@see ptolemy.data.Token
@see java.text.NumberFormat
@author Neil Smyth, Yuhong Xiong, Christopher Hylands
@version $Id$
*/
public class DoubleToken extends ScalarToken {

    /** Construct a DoubleToken with value 0.0.
     */
    public DoubleToken() {
        _value = 0.0;
    }

    /** Construct a DoubleToken with the specified value.
     */
    public DoubleToken(double value) {
        _value = value;
    }

    /** Construct a DoubleToken from the specified string.
     *  @exception IllegalActionException If the Token could not
     *   be created with the given String.
     */
    public DoubleToken(String init) throws IllegalActionException {
        try {
            _value = (Double.valueOf(init)).doubleValue();
        } catch (NumberFormatException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a DoubleToken containing the absolute value of the
     *  value of this token.
     *  @return A DoubleToken.
     */
    public ScalarToken absolute() {
        DoubleToken result;
        if (_value >= 0.0) {
            result = new DoubleToken(_value);
        } else {
            result = new DoubleToken(-_value);
        }

        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned Token type chosen to achieve a lossless conversion.
     *  @param rightArgument The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be added to this token, or
     *   the units of this token and the argument token are not the same.
     */
    public Token add(Token rightArgument) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArgument);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            DoubleToken doubleToken;
            if (typeInfo == CPO.HIGHER) {
                doubleToken = (DoubleToken)DoubleToken.convert(rightArgument);
            } else {
                doubleToken = (DoubleToken)rightArgument;
            }
            double sum = _value + doubleToken.doubleValue();
            DoubleToken result = new DoubleToken(sum);
            if ( !_areUnitsEqual(doubleToken)) {
                throw new IllegalActionException("DoubleToken.add: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + doubleToken.unitsString());
            }
            result._unitCategoryExponents = this._copyOfCategoryExponents();
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.addReverse(this);
        } else {
            throw new IllegalActionException(
                    _notSupportedMessage("add", this, rightArgument));
        }
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned token type chosen to achieve
     *  a lossless conversion.
     *  @param leftArgument The token to add this token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be added to this token, or
     *   the units of this token and the argument token are not the same.
     */
    public Token addReverse(ptolemy.data.Token leftArgument)
            throws IllegalActionException {
        return this.add(leftArgument);
    }

    /** Return the value of this token as a Complex. The real part
     *  of the Complex is the value of this token, the imaginary part
     *  is set to 0.
     *  @return A Complex
     */
    public Complex complexValue() {
        return new Complex(_value, 0.0);
    }

    /** Convert the specified token into an instance of DoubleToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of DoubleToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below DoubleToken in the type hierarchy, it is converted to
     *  an instance of DoubleToken or one of the subclasses of
     *  DoubleToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a DoubleToken.
     *  @return A DoubleToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static Token convert(Token token) throws IllegalActionException {
        int compare = TypeLattice.compare(BaseType.DOUBLE, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("DoubleToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with DoubleToken in the type " +
                    "hierarchy.");
        }

        if (token instanceof DoubleToken) {
            return token;
        }

        compare = TypeLattice.compare(BaseType.INT, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntToken intToken = (IntToken)IntToken.convert(token);
            DoubleToken result = new DoubleToken(intToken.doubleValue());
            result._unitCategoryExponents = intToken._copyOfCategoryExponents();
            return result;
        } else {
            throw new IllegalActionException("Cannot convert from token "
                    + "type: " + token.getClass().getName()
                    + " to a DoubleToken");
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param divisor The token to divide this token by
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *  not of a type that can divide the value of this token.
     */
    public Token divide(Token divisor) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, divisor);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            DoubleToken doubleToken;
            if (typeInfo == CPO.HIGHER) {
                doubleToken = (DoubleToken)DoubleToken.convert(divisor);
            } else {
                doubleToken = (DoubleToken)divisor;
            }
            double quotient = _value / doubleToken.doubleValue();
            DoubleToken result = new DoubleToken(quotient);
            // compute units
            result._unitCategoryExponents =
                _subtractCategoryExponents(doubleToken);
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return divisor.divideReverse(this);
        } else {
            throw new IllegalActionException(
                    _notSupportedMessage("divide", this, divisor));
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  divided by the value of this token. Type resolution
     *  also occurs here, with the returned token type chosen to achieve
     *  a lossless conversion.
     *  @param dividend The token to be divided by the value of this token.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be divided by this token.
     *  @return A new token containing the result.
     */
    public Token divideReverse(Token dividend) throws IllegalActionException {
        DoubleToken doubleToken = (DoubleToken)DoubleToken.convert(dividend);
        double quotient = doubleToken.doubleValue() / _value;
        DoubleToken result = new DoubleToken(quotient);

        // compute units
        result._unitCategoryExponents =
            doubleToken._subtractCategoryExponents(this);
        return result;
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    public double doubleValue() {
        return _value;
    }

    /** Return true if the argument is an instance of DoubleToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of DoubleToken with the
     *  same value.
     */
    public boolean equals(Object object) {
	// This test rules out subclasses.
	if (object.getClass() != DoubleToken.class) {
	    return false;
	}

	if (((DoubleToken)object).doubleValue() == _value) {
	    return true;
	}
	return false;
    }

    /** Return the type of this token.
     *  @return BaseType.DOUBLE
     */
    public Type getType() {
        return BaseType.DOUBLE;
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the contained double.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	return (int)_value;
    }

    /** Test that the value of this token is close to the argument
     *  token and that the units of this TOken and the argument token
     *  are equal.  The value of the ptolemy.math.Complex epsilon
     *  field is used to determine whether the two Tokens are close.
     *
     *  <p>If A and B are the values of the tokens, and if
     *  the following is true:
     *  <pre>
     *  absolute(A-B) < epsilon
     *  </pre>
     *  and the units of A and B are equal, then A and B are considered close.
     *
     *  @see ptolemy.math.Complex#epsilon
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @return a boolean token that contains the value true if the
     *   value of this token is close to the value of the argument
     *   token and the units of both tokens are equal.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token) throws IllegalActionException{
	return isCloseTo(token, ptolemy.math.Complex.epsilon);
    }

    /** Test that the value of this token is close to the argument
     *  token and that the units of this token and the argument token
     *  equal.
     *  The value of the epsilon argument is used to determine
     *  whether the two Tokens are close.
     *
     *  <p>If A and B are the values of the tokens, and if
     *  the following is true:
     *  <pre>
     *  abs(A-B) < epsilon
     *  </pre>
     *  and the units of A and B are equal, then A and B are considered close.
     *
     *  <p>There are two isCloseTo() methods so that we can use
     *  different values of epsilon in different threads without
     *  modifying the value of math.Complex.epsilon.
     *
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.
     *  @return a boolean token that contains the value true if the
     *   value of this token is close to the value of the argument
     *   token and the units of both tokens are equal.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token,
            double epsilon)
            throws IllegalActionException {

        // We need to do type conversion here to handle isCloseTo(Complex).
        int typeInfo = TypeLattice.compare(this, token);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            DoubleToken doubleToken;
            if (typeInfo == CPO.HIGHER) {
                doubleToken = (DoubleToken)DoubleToken.convert(token);
            } else {
                doubleToken = (DoubleToken)token;
            }
            DoubleToken difference = (DoubleToken)subtract(doubleToken);
            return difference.absolute().isLessThan(new DoubleToken(epsilon));
        } else if (typeInfo == CPO.LOWER) {
            return token.isCloseTo(this);
        } else {
            throw new IllegalActionException(
                    _notSupportedMessage("isCloseTo", this, token));
        }
    }

    /** Test the value and units of this token and the argument token
     *  for equality.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @see #isCloseTo
     *  @param token The token to test equality of this token with.
     *  @return a boolean token that contains the value true if the
     *   value and units of this token are equal to those of the argument
     *   token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, token);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            DoubleToken doubleToken;
            if (typeInfo == CPO.HIGHER) {
                doubleToken = (DoubleToken)DoubleToken.convert(token);
            } else {
                doubleToken = (DoubleToken)token;
            }

            if (_value == doubleToken.doubleValue()
                    && _areUnitsEqual(doubleToken)) {
                return new BooleanToken(true);
            } else {
                return new BooleanToken(false);
            }

        } else if (typeInfo == CPO.LOWER) {
            return token.isEqualTo(this);
        } else {
            throw new IllegalActionException(
                    _notSupportedMessage("isEqualTo", this, token));
        }
    }

    /** Check if the value of this token is strictly less than that of the
     *  argument token.
     *  @param arg A ScalarToken.
     *  @return A BooleanToken with value true if this token is strictly
     *   less than the argument.
     *  @exception IllegalActionException If the type of the argument token
     *   is incomparable with the type of this token, or the units of this
     *   token and the argument are not the same.
     */
    public BooleanToken isLessThan(ScalarToken token)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, token);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            DoubleToken doubleToken;
            if (typeInfo == CPO.HIGHER) {
                doubleToken = (DoubleToken)DoubleToken.convert(token);
            } else {
                doubleToken = (DoubleToken)token;
            }
            if ( !_areUnitsEqual(doubleToken)) {
                throw new IllegalActionException("DoubleToken.isLessThan: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + doubleToken.unitsString());
            }
            if (_value < doubleToken.doubleValue()) {
                return new BooleanToken(true);
            } else {
                return new BooleanToken(false);
            }
        } else if (typeInfo == CPO.LOWER) {
            if (token.isEqualTo(this).booleanValue()) {
                return new BooleanToken(false);
            } else {
                return token.isLessThan(this).not();
            }
        } else {
            throw new IllegalActionException(
                    _notSupportedMessage("isLessThan", this, token));
        }
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be  used with modulo, or the units of
     *   this token and the argument token are not the same.
     */
    public Token modulo(Token rightArgument) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArgument);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            DoubleToken doubleToken;
            if (typeInfo == CPO.HIGHER) {
                doubleToken = (DoubleToken)DoubleToken.convert(rightArgument);
            } else {
                doubleToken = (DoubleToken)rightArgument;
            }
            double remainder = _value % doubleToken.doubleValue();
            DoubleToken result = new DoubleToken(remainder);
            if ( !_areUnitsEqual(doubleToken)) {
                throw new IllegalActionException("DoubleToken.modulo: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + doubleToken.unitsString());
            }
            result._unitCategoryExponents = this._copyOfCategoryExponents();
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.moduloReverse(this);
        } else {
            throw new IllegalActionException(
                    _notSupportedMessage("modulo", this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  modulo the value of this token.
     *  Type resolution also occurs here, with the returned token
     *  type chosen to achieve a lossless conversion.
     *  @param leftArgument The token to apply modulo to by the value of this
     *   token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can apply modulo by this token, or
     *   if the units of this token are not the same as those of the
     *   argument.
     */
    public Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        DoubleToken doubleToken =
            (DoubleToken)DoubleToken.convert(leftArgument);

        double remainder = doubleToken.doubleValue() % _value;
        DoubleToken result = new DoubleToken(remainder);
        if ( !_areUnitsEqual(doubleToken)) {
            throw new IllegalActionException("DoubleToken.moduloReverse: "
                    + "The units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + doubleToken.unitsString());
        }
        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightFactor The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *  not of a type that can be multiplied to this token.
     */
    public Token multiply(Token rightFactor) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightFactor);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            DoubleToken doubleToken;
            if (typeInfo == CPO.HIGHER) {
                doubleToken = (DoubleToken)DoubleToken.convert(rightFactor);
            } else {
                doubleToken = (DoubleToken)rightFactor;
            }
            double product = _value * doubleToken.doubleValue();
            DoubleToken result = new DoubleToken(product);
            // compute units
            result._unitCategoryExponents = _addCategoryExponents(doubleToken);
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightFactor.multiplyReverse(this);
        } else {
            throw new IllegalActionException(
                    _notSupportedMessage("multiply", this, rightFactor));
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  multiplied by the value of this token.
     *  Type resolution also occurs here, with the returned token
     *  type chosen to achieve a lossless conversion.
     *  @param leftFactor The token to be multiplied by the value of
     *   this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be multiplied by this token.
     */
    public Token multiplyReverse(Token leftFactor)
            throws IllegalActionException {
        return this.multiply(leftFactor);
    }

    /** Returns a new DoubleToken with value 1.0.
     *  @return A new DoubleToken with value 1.0.
     */
    public Token one() {
        return new DoubleToken(1.0);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArgument The token to subtract to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be subtracted from this token, or the units
     *   of this token and the argument token are not the same.
     */
    public Token subtract(Token rightArgument) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArgument);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            DoubleToken doubleToken;
            if (typeInfo == CPO.HIGHER) {
                doubleToken = (DoubleToken)DoubleToken.convert(rightArgument);
            } else {
                doubleToken = (DoubleToken)rightArgument;
            }
            double difference = _value - doubleToken.doubleValue();
            DoubleToken result = new DoubleToken(difference);
            if ( !_areUnitsEqual(doubleToken)) {
                throw new IllegalActionException("DoubleToken.subtract: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + doubleToken.unitsString());
            }
            result._unitCategoryExponents = this._copyOfCategoryExponents();
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.subtractReverse(this);
        } else {
            throw new IllegalActionException(
                    _notSupportedMessage("subtract", this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be added to this token, or the units
     *   of this token and the argument token are not the same.
     */
    public Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        DoubleToken doubleToken =
            (DoubleToken)DoubleToken.convert(leftArgument);
        double difference = doubleToken.doubleValue() - _value;
        DoubleToken result = new DoubleToken(difference);
        if ( !_areUnitsEqual(doubleToken)) {
            throw new IllegalActionException("DoubleToken.subtractReverse: "
                    + "The units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + doubleToken.unitsString());
        }
        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  The exact form of the number depends on its value, and may be either
     *  decimal or exponential.  In general, exponential is used for numbers
     *  whose magnitudes are very large or very small, except for zero which
     *  is always represented as 0.0.  The behavior is roughly the same as
     *  Double.toString(), except that we limit the precision to seven
     *  fractional digits.  If you really must have better precision,
     *  then use <code>Double.toString(token.doubleValue())</code>.
     *  If this token has a unit, the return string also includes a unit
     *  string produced by the unitsString() method in the super class.
     *  @return A String representing the double value and the units (if
     *   any) of this token.
     *  @see ptolemy.data.ScalarToken#unitsString
     */
    public String toString() {
	String unitString = "";
	if ( !_isUnitless()) {
	    unitString = " * " + unitsString();
	}

        double mag = Math.abs(_value);
        if (mag == 0.0 || (mag < 1000000 && mag > .001)) {
            return _regularFormat.format(_value) + unitString;
        } else {
            return _exponentialFormat.format(_value) + unitString;
        }
    }

    /** Returns a new DoubleToken with value 0.0.
     *  @return A DoubleToken with value 0.0.
     */
    public Token zero() {
        return new DoubleToken(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double _value;

    // The number of fractional digits here is determined by the place
    // at which common numbers, such as one half, will get rounded to
    // display nicely.
    private static DecimalFormat _regularFormat =
    new DecimalFormat("####0.0############");

    // Note: This used to be new DecimalFormat("0.0############E0##"),
    // but compiling with gcj resulted in the following error:
    //  'Exception in thread "main" class
    //  java.lang.ExceptionInInitializerError:
    //  java.lang.IllegalArgumentException: digit mark following zero
    //  in exponent - index: 17'
    private static DecimalFormat _exponentialFormat =
    new DecimalFormat("0.0############E0");
}
