/* A token that contains a double precision number.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;


//////////////////////////////////////////////////////////////////////////
//// DoubleToken
/**
A token that contains a 64-bit signed mantissa, signed exponent double
precision floating-point number (IEEE 754).  This class handles overflow and
underflow as does normal java arithmetic on doubles.

 <p> Note that a double cannot be losslessly converted to a long, and
vice versa, as both have 64-bit representations in Java.

@see ptolemy.data.Token
@author Neil Smyth, Yuhong Xiong, Christopher Hylands, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
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
            _value = Double.parseDouble(init);
        } catch (NumberFormatException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value of this token as a Complex. The real part
     *  of the Complex is the value of this token, the imaginary part
     *  is set to 0.
     *  @return A Complex
     */
    public Complex complexValue() {
        return new Complex(_value, 0.0);
    }

    /** Convert the specified token into an instance of DoubleToken.
     *  This method does lossless conversion.  The units of the returned
     *  token will be the same as the units of the given token.
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
    public static DoubleToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof DoubleToken) {
            return (DoubleToken)token;
        }

        int compare = TypeLattice.compare(BaseType.DOUBLE, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "double"));
        }

        compare = TypeLattice.compare(BaseType.INT, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntToken intToken = IntToken.convert(token);
            DoubleToken result = new DoubleToken(intToken.doubleValue());
            result._unitCategoryExponents =
                intToken._copyOfCategoryExponents();
            return result;
        } else {
            throw new IllegalActionException(
                    notSupportedConversionMessage(token, "double"));
        }
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    public double doubleValue() {
        return _value;
    }

    /** Return true if the argument's class is DoubleToken and it has the
     *  same values as this token.
     *  @param object An instance of Object.
     *  @return True if the argument is a DoubleToken with the
     *  same value.
     */
    public boolean equals(Object object) {
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
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

    /** Returns a new DoubleToken with value 1.0.
     *  @return A new DoubleToken with value 1.0.
     */
    public Token one() {
        return new DoubleToken(1.0);
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
        if (Double.isNaN(_value) || Double.isInfinite(_value)) {
            return Double.toString(_value) + unitString;
        } else {
            double mag = Math.abs(_value);
            if (mag == 0.0 || (mag < 1000000 && mag > .001)) {
                return TokenUtilities.regularFormat.format(_value) + unitString;
            } else {
                return TokenUtilities.exponentialFormat.format(_value)
                    + unitString;
            }
        }
    }

    /** Returns a new DoubleToken with value 0.0.
     *  @return A DoubleToken with value 0.0.
     */
    public Token zero() {
        return new DoubleToken(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.
     *  @return An DoubleToken.
     */
    protected ScalarToken _absolute() {
        DoubleToken result;
        if (_value >= 0.0) {
            result = this;
        } else {
            result = new DoubleToken(-_value);
        }
        return result;
    }

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an DoubleToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new DoubleToken containing the result.
     */
    protected ScalarToken _add(ScalarToken rightArgument) {
        double sum = _value + ((DoubleToken)rightArgument).doubleValue();
        return new DoubleToken(sum);
    }

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.
     *  @return The bitwise AND.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    protected ScalarToken _bitwiseAnd(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("bitwiseAnd", this, rightArgument));
    }

    /** Returns a token representing the bitwise NOT of this token.
     *  @return The bitwise NOT of this token.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    protected ScalarToken _bitwiseNot()
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("bitwiseNot", this, this));
    }

    /** Returns a token representing the bitwise OR of this token and
     *  the given token.
     *  @return The bitwise OR.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    protected ScalarToken _bitwiseOr(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("bitwiseOr", this, rightArgument));
    }

    /** Returns a token representing the bitwise XOR of this token and
     *  the given token.
     *  @return The bitwise XOR.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    protected ScalarToken _bitwiseXor(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("bitwiseXor", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an DoubleToken
     *  @param divisor The token to divide this token by.
     *  @return A new DoubleToken containing the result.
     */
    protected ScalarToken _divide(ScalarToken divisor) {
        double quotient = _value / ((DoubleToken)divisor).doubleValue();
        return new DoubleToken(quotient);
    }

    /** Test that the value of this token is close to the first argument,
     *  where "close" means that the distance between their values is less than
     *  or equal to the second argument. It is assumed that the type of
     *  the first argument is DoubleToken.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon The distance.
     *  @return A token containing true if the value of this token is close
     *   to that of the argument.
     */
    protected BooleanToken _isCloseTo(
            ScalarToken rightArgument, double epsilon) {

        // NOTE: This code is duplicated in
        // ptolemy.math.DoubleMatrixMath.within(); if this
        // implementation changes, also change the corresponding
        // implementation there.
        // NOTE: Used to compare against epsilon the following expression:

        // Math.abs(doubleValue() - ((DoubleToken)rightArgument).doubleValue()))
        // However, because of quantization errors, this did not work well.
        double right = ((DoubleToken)rightArgument).doubleValue();
        double left = doubleValue();
        if (right > left + epsilon || right < left - epsilon) {
            return BooleanToken.FALSE;
        } else {
            return BooleanToken.TRUE;
        }
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is DoubleToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        DoubleToken convertedArgument = (DoubleToken)rightArgument;
        return BooleanToken.getInstance(
                _value < convertedArgument.doubleValue());
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is an DoubleToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new DoubleToken containing the result.
     */
    protected ScalarToken _modulo(ScalarToken rightArgument) {
        double remainder = _value % ((DoubleToken)rightArgument).doubleValue();
        return new DoubleToken(remainder);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is an DoubleToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new DoubleToken containing the result.
     */
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        double product = _value * ((DoubleToken)rightArgument).doubleValue();
        return new DoubleToken(product);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is an DoubleToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new DoubleToken containing the result.
     */
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        double difference = _value -
            ((DoubleToken)rightArgument).doubleValue();
        return new DoubleToken(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double _value;
}
