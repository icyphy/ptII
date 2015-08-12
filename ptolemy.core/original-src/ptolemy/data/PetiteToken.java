/* A token that contains a petite number.
 Copyright (c) 2006-2014 The Regents of the University of California.
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
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// PetiteToken

/**
 A token that contains a number which is essentially a
 simulation equivalent for fixed point numbers in embedded processors.
 By definition it is between -1 (inclusive) and 1 (exclusive). It is
 a extension of the Double Token and maintains the same precision as
 Double Token.
 @see ptolemy.data.Token
 @author Shamik Bandyopadhyay
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Green (cxh)
 */
public class PetiteToken extends ScalarToken {
    /** Construct a PetiteToken with value 0.0.
     */
    public PetiteToken() {
        _value = 0.0;
    }

    /** Construct a PetiteToken with the specified value.
     *  Ensure that the value is adjusted within limits [-1,1).
     *  @param value The specified value.
     */
    public PetiteToken(double value) {
        _value = _adjust(value);
    }

    /** Construct a PetiteToken from the specified string.Ensure
     *  the value is adjusted within the range of a PetiteToken.
     *  @param init The specified string, for example <code>1.0p</code>
     *  @exception IllegalActionException If the Token could not
     *   be created with the given String.
     */
    public PetiteToken(String init) throws IllegalActionException {
        try {
            _value = _adjust(Double.parseDouble(init));
        } catch (NumberFormatException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an instance of PetiteToken.
     *  This method does lossless conversion.  The units of the returned
     *  token will be the same as the units of the given token.
     *  If the argument is already an instance of PetiteToken,
     *  it is returned without any change. Since the PetiteToken is cannot be
     *  losslessly converted to any other token an exception is thrown, in all
     *  other cases.
     *  @param token The token to be converted to a PetiteToken.
     *  @return A PetiteToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static PetiteToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof PetiteToken) {
            return (PetiteToken) token;
        } else {
            if (token instanceof DoubleToken) {
                // Need to get the value of the DoubleToken and _adjust it
                return new PetiteToken(((DoubleToken) token).doubleValue());
            } else {
                throw new IllegalActionException(notSupportedConversionMessage(
                        token, "petite"));
            }
        }
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    @Override
    public double doubleValue() {
        return _value;
    }

    /** Return the value in the token.
     *  @return The value contained in this token.
     */
    public double petiteValue() {
        return _value;
    }

    /** Return true if the argument's class is PetiteToken and it has the
     *  same values as this token.
     *  @param object An instance of Object.
     *  @return True if the argument is a PetiteToken with the
     *  same value.
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

        if (((PetiteToken) object).petiteValue() == _value) {
            return true;
        }

        return false;
    }

    /** Return the type of this token.
     *  @return BaseType.PETITE
     */
    @Override
    public Type getType() {
        return BaseType.PETITE;
    }

    /** Return a hash code value for this token. This method returns the
     *  hash code of the contained double.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        return Double.valueOf(_value).hashCode();
    }

    /** Returns a PetiteToken with value nearest 1.0.......
     *  @return A PetiteToken with value nearest 1.0.
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
     *  Double.toString(), except that we limit the precision to seven
     *  fractional digits.  If you really must have better precision,
     *  then use <code>Double.toString(token.doubleValue())</code>.
     *  If this token has a unit, the return string also includes a unit
     *  string produced by the unitsString() method in the super class.
     *  @return A String representing the double value and the units (if
     *   any) of this token.
     *  @see ptolemy.data.ScalarToken#unitsString
     */
    @Override
    public String toString() {
        String unitString = "";

        if (!_isUnitless()) {
            unitString = " * " + unitsString();
        }
        return TokenUtilities.regularFormat.format(_value) + "p" + unitString;
    }

    /** Returns a PetiteToken with value 0.0.
     *  @return A PetiteToken with value 0.0.
     */
    @Override
    public Token zero() {
        return ZERO;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variablesds                ////

    /** A PetiteToken with the value 1.0. */
    public static final PetiteToken ONE = new PetiteToken(1.0);

    /** A PetiteToken with the value 0.0. */
    public static final PetiteToken ZERO = new PetiteToken(0.0);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same. The call to the constructor
     *  ensures the value is within the range defined by a PetiteToken.
     *  It thus automatically converts -1 to (1 - Double.MIN_VALUE).
     *  @return An PetiteToken.
     */
    @Override
    protected ScalarToken _absolute() {
        PetiteToken result;

        if (_value >= 0.0) {
            result = this;
        } else {
            result = new PetiteToken(-_value);
        }

        return result;
    }

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is an PetiteToken.The token to add is
     *  first adjusted to the range defined by a petite token. After
     *  division, the result is adjusted again to maintain the range
     *  of a PetiteToken. The final adjustment happens automatically
     *  given the call to _adjust in the PetiteToken constructor.
     *  @param rightArgument The token to add to this token.
     *  @return A new PetiteToken containing the result.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument) {
        double sum = _value + ((PetiteToken) rightArgument).doubleValue();
        return new PetiteToken(sum);
    }

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.
     *  @param rightArgument The PetiteToken to bitwise AND with this one.
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
     *  @param rightArgument The PetiteToken to bitwise OR with this one.
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
     *  @param rightArgument The PetiteToken to bitwise XOR with this one.
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
     *  the type of the argument is an PetiteToken. The token to
     *  divide by is first adjusted to the range defined by a petite
     *  token. After division, the result is adjusted again to
     *  maintain the range of a PetiteToken. The final adjustment
     *  happens automatically given the call to _adjust in the
     *  PetiteToken constructor.
     *  @param divisor The token to divide this token by.
     *  @return A new PetiteToken containing the result.
     */
    @Override
    protected ScalarToken _divide(ScalarToken divisor) {
        double quotient = _value / ((PetiteToken) divisor).doubleValue();
        return new PetiteToken(quotient);
    }

    /** Test that the value of this token is close to the first argument,
     *  where "close" means that the distance between their values is less than
     *  or equal to the second argument. It is assumed that the type of
     *  the first argument is PetiteToken. The right argument is adjusted to
     *  be in the range of a PetiteToken. The second argument is also adjusted
     *  likewise.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon The distance.
     *  @return A token containing true if the value of this token is close
     *   to that of the argument.
     */
    @Override
    protected BooleanToken _isCloseTo(ScalarToken rightArgument, double epsilon) {
        // NOTE: This code is duplicated in
        // ptolemy.math.DoubleMatrixMath.within(); if this
        // implementation changes, also change the corresponding
        // implementation there.
        // NOTE: Used to compare against epsilon the following expression:
        // Math.abs(doubleValue() - ((PetiteToken)rightArgument).doubleValue()))
        // However, because of quantization errors, this did not work well.
        double right = ((PetiteToken) rightArgument).doubleValue();
        double left = petiteValue();

        if (right > left + epsilon || right < left - epsilon) {
            return BooleanToken.FALSE;
        } else {
            return BooleanToken.TRUE;
        }
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is PetiteToken.
     *  The argument token is then adjusted to the range of a PetiteToken.
     *  @param rightArgument The token to compare this token with.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        PetiteToken convertedArgument = (PetiteToken) rightArgument;
        return BooleanToken.getInstance(_value < convertedArgument
                .doubleValue());
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is an PetiteToken.The token to take
     *  modulo by is first adjusted to the range defined by a petite
     *  token. After the modulo operation, the result is adjusted again to
     *  maintain the range of a PetiteToken. The final adjustment happens
     *  automatically given the call to _adjust in the PetiteToken constructor
     *  @param rightArgument The token to modulo this token by.
     *  @return A new PetiteToken containing the result.
     */
    @Override
    protected ScalarToken _modulo(ScalarToken rightArgument) {
        double remainder = _value % ((PetiteToken) rightArgument).doubleValue();
        return new PetiteToken(remainder);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is an PetiteToken.The token to multiply
     *  is first adjusted to the range defined by a petite token. After
     *  multiplication, the result is adjusted again to
     *  maintain the range of a PetiteToken. The final adjustment happens
     *  automatically given the call to _adjust in the PetiteToken constructor
     *  @param rightArgument The token to multiply this token by.
     *  @return A new PetiteToken containing the result.
     */
    @Override
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        double product = _value * ((PetiteToken) rightArgument).doubleValue();
        return new PetiteToken(product);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is an PetiteToken. The token to subtract
     *  is first adjusted to the range defined by a petite token. After
     *  subtraction from this token, the result is adjusted again to
     *  maintain the range of a PetiteToken. The final adjustment happens
     *  automatically given the call to _adjust in the PetiteToken constructor
     *  @param rightArgument The token to subtract from this token.
     *  @return A new PetiteToken containing the result.
     */
    @Override
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        double difference = _value
                - ((PetiteToken) rightArgument).doubleValue();
        return new PetiteToken(difference);
    }

    /**
     * Adjust the value of the PetiteToken to limit it to the range
     * [-1,1) while maintaining the precision of PetiteToken.
     * @param number The value to be adjusted.
     * @return The adjusted value.
     */
    protected static double _adjust(double number) {
        if (number >= 1.0) {
            return 1.0 - Double.MIN_VALUE;
        } else {
            if (number < -1.0) {
                return -1.0;
            }
        }
        return number;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double _value;
}
