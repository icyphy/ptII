/* A token that contains a single precision floating point number.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.unit.UnitUtilities;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// FloatToken

/**
 A token that contains a 32-bit signed mantissa, signed exponent single
 precision floating-point number (IEEE 754).  This class handles overflow and
 underflow as does normal java arithmetic on floats.

 <p> Note that a float cannot be losslessly converted to an int, and
 vice versa, as both have 32-bit representations in Java.

 @see ptolemy.data.Token
 @author Ben Lickly; Based on DoubleToken by Neil Smyth, Yuhong Xiong, Christopher Hylands, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (blickly) nil token, ONE, ZERO
 @Pt.AcceptedRating Red (blickly)
 */
public class FloatToken extends ScalarToken {
    /** Construct a FloatToken with value 0.0.
     */
    public FloatToken() {
        _value = 0.0f;
    }

    /** Construct a FloatToken with the specified value.
     *  @param value The specified value.
     */
    public FloatToken(float value) {
        _value = value;
    }

    /** Construct a FloatToken from the specified string.
     *  @param init The initialization string, which is in a format
     *  suitable for java.lang.Float.parseFloat(String).
     *  @exception IllegalActionException If the Token could not
     *   be created with the given String.
     */
    public FloatToken(String init) throws IllegalActionException {
        if (init == null || init.equals("nil")) {
            throw new IllegalActionException(notSupportedNullNilStringMessage(
                    "FloatToken", init));
        }
        try {
            _value = Float.parseFloat(init);
        } catch (NumberFormatException e) {
            throw new IllegalActionException(null, e, "Failed to parse \""
                    + init + "\" as a number.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an instance of FloatToken.
     *  This method does lossless conversion.  The units of the
     *  returned token will be the same as the units of the given
     *  token.  If the argument is already an instance of FloatToken,
     *  it is returned without any change.  If the argument
     *  is a nil token, then {@link #NIL} is
     *  returned.  Otherwise, if the argument is below FloatToken in
     *  the type hierarchy, it is converted to an instance of
     *  FloatToken or one of the subclasses of FloatToken and
     *  returned. If none of the above condition is met, an exception
     *  is thrown.
     *
     *  @param token The token to be converted to a FloatToken.
     *  @return A FloatToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static FloatToken convert(Token token) throws IllegalActionException {
        if (token instanceof FloatToken) {
            return (FloatToken) token;
        }
        if (token.isNil()) {
            return FloatToken.NIL;
        }
        int compare = TypeLattice.compare(BaseType.FLOAT, token);

        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(token, "float"));
        }

        compare = TypeLattice.compare(BaseType.SHORT, token);

        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            ShortToken shortToken = ShortToken.convert(token);
            FloatToken result = new FloatToken(shortToken.floatValue());
            if (shortToken._unitCategoryExponents != null
                    && !UnitUtilities
                            .isUnitless(shortToken._unitCategoryExponents)) {
                result._unitCategoryExponents = shortToken
                        ._copyOfCategoryExponents();
            }
            return result;
        } else {
            throw new IllegalActionException(notSupportedConversionMessage(
                    token, "float"));
        }
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token represented as a double.
     */
    @Override
    public double doubleValue() {
        return _value;
    }

    /** Return true if the argument's class is FloatToken and it has the
     *  same values as this token.
     *  @param object An instance of Object.
     *  @return True if the argument is a FloatToken with the same
     *  value. If either this object or the argument is a nil Token, return
     *  false.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
            return false;
        }

        if (isNil() || ((FloatToken) object).isNil()) {
            return false;
        }

        if (((FloatToken) object).floatValue() == _value) {
            return true;
        }

        return false;
    }

    /** Return the value in the token as a float.
     *  @return The value contained in this token as a float.
     */
    @Override
    public float floatValue() {
        return _value;
    }

    /** Return the type of this token.
     *  @return BaseType.FLOAT
     */
    @Override
    public Type getType() {
        return BaseType.FLOAT;
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the contained float.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        return (int) _value;
    }

    /** Return true if the token is nil, (aka null or missing).
     *  Nil or missing tokens occur when a data source is sparsely populated.
     *  @return True if the token is the {@link #NIL} token.
     */
    @Override
    public boolean isNil() {
        // We use a method here so that we can easily change how
        // we determine if a token is nil without modify lots of classes.
        return this == FloatToken.NIL;
    }

    /** Returns a FloatToken with value 1.0.
     *  @return A FloatToken with value 1.0.
     */
    @Override
    public Token one() {
        return ONE;
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  The exact form of the number depends on its value, and may be either
     *  decimal or exponential.  In general, exponential is used for numbers
     *  whose magnitudes are very large or very small, except for zero which
     *  is always represented as 0.0.  The behavior is roughly the same as
     *  Float.toString(), except that we limit the precision to seven
     *  fractional digits.  If you really must have better precision,
     *  then use <code>Float.toString(token.floatValue())</code>.
     *  If this token has a unit, the return string also includes a unit
     *  string produced by the unitsString() method in the super class.
     *  @return A String representing the float value and the units (if
     *   any) of this token.
     *  @see ptolemy.data.ScalarToken#unitsString
     */
    @Override
    public String toString() {
        String unitString = "";

        if (!_isUnitless()) {
            unitString = " * " + unitsString();
        }

        if (isNil()) {
            // FIXME: what about units?
            return super.toString();
        }

        if (Float.isNaN(_value) || Float.isInfinite(_value)) {
            return Float.toString(_value) + "f" + unitString;
        } else {
            float mag = Math.abs(_value);

            if (mag == 0.0f || mag < 1000000 && mag > .001) {
                return TokenUtilities.regularFormat.format(_value) + "f"
                        + unitString;
            } else {
                return TokenUtilities.exponentialFormat.format(_value) + "f"
                        + unitString;
            }
        }
    }

    /** Returns a FloatToken with value 0.0.
     *  @return A FloatToken with value 0.0.
     */
    @Override
    public Token zero() {
        return ZERO;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A token that represents a missing value.
     *  Null or missing tokens are common in analytical systems
     *  like R and SAS where they are used to handle sparsely populated data
     *  sources.  In database parlance, missing tokens are sometimes called
     *  null tokens.  Since null is a Java keyword, we use the term "nil".
     *  The toString() method on a nil token returns the string "nil".
     */
    public static final FloatToken NIL = new FloatToken(Float.NaN);

    /** A FloatToken with the value 1.0. */
    public static final FloatToken ONE = new FloatToken(1);

    /** A FloatToken with the value 0.0. */
    public static final FloatToken ZERO = new FloatToken(0);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.
     *  @return An FloatToken.
     */
    @Override
    protected ScalarToken _absolute() {
        FloatToken result;

        if (_value >= 0.0f) {
            result = this;
        } else {
            result = new FloatToken(-_value);
        }

        return result;
    }

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is a FloatToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new FloatToken containing the result.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument) {
        float sum = _value + ((FloatToken) rightArgument).floatValue();
        return new FloatToken(sum);
    }

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.
     *  @param rightArgument The FloatToken to bitwise AND with this one.
     *  @return The bitwise AND.
     *  @exception IllegalActionException Always thrown by this base class.
     */
    @Override
    protected ScalarToken _bitwiseAnd(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("bitwiseAnd",
                this, rightArgument));
    }

    /** Returns a token representing the bitwise NOT of this token.
     *  @return The bitwise NOT of this token.
     *  @exception IllegalActionException Always thrown by this base class.
     */
    @Override
    protected ScalarToken _bitwiseNot() throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("bitwiseNot",
                this, this));
    }

    /** Returns a token representing the bitwise OR of this token and
     *  the given token.
     *  @param rightArgument The FloatToken to bitwise OR with this one.
     *  @return The bitwise OR.
     *  @exception IllegalActionException Always thrown by this base class.
     */
    @Override
    protected ScalarToken _bitwiseOr(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("bitwiseOr", this,
                rightArgument));
    }

    /** Returns a token representing the bitwise XOR of this token and
     *  the given token.
     *  @param rightArgument The FloatToken to bitwise XOR with this one.
     *  @return The bitwise XOR.
     *  @exception IllegalActionException Always thrown by this base class.
     */
    @Override
    protected ScalarToken _bitwiseXor(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("bitwiseXor",
                this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is a FloatToken
     *  @param divisor The token to divide this token by.
     *  @return A new FloatToken containing the result.
     */
    @Override
    protected ScalarToken _divide(ScalarToken divisor) {
        float quotient = _value / ((FloatToken) divisor).floatValue();
        return new FloatToken(quotient);
    }

    /** Test that the value of this token is close to the first
     *  argument, where "close" means that the distance between their
     *  values is less than or equal to the second argument. It is
     *  assumed that the type of the first argument is FloatToken.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon The distance.
     *  @return A token containing tue if the value of this token is close
     *   to that of the argument.
     */
    @Override
    protected BooleanToken _isCloseTo(ScalarToken rightArgument, double epsilon) {
        // NOTE: Used to compare against epsilon the following expression:
        // Math.abs(floatValue() - ((FloatToken)rightArgument).floatValue()))
        // However, because of quantization errors, this did not work well.
        double right = ((FloatToken) rightArgument).doubleValue();
        double left = doubleValue();

        if (right > left + epsilon || right < left - epsilon) {
            return BooleanToken.FALSE;
        } else {
            return BooleanToken.TRUE;
        }
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is FloatToken.
     *  @param rightArgument The token to compare to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        FloatToken convertedArgument = (FloatToken) rightArgument;
        return BooleanToken
                .getInstance(_value < convertedArgument.floatValue());
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is a FloatToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new FloatToken containing the result.
     */
    @Override
    protected ScalarToken _modulo(ScalarToken rightArgument) {
        float remainder = _value % ((FloatToken) rightArgument).floatValue();
        return new FloatToken(remainder);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is a FloatToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new FloatToken containing the result.
     */
    @Override
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        float product = _value * ((FloatToken) rightArgument).floatValue();
        return new FloatToken(product);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is a FloatToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new FloatToken containing the result.
     */
    @Override
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        float difference = _value - ((FloatToken) rightArgument).floatValue();
        return new FloatToken(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private float _value;
}
