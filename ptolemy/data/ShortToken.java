/* A token that contains a short (16 bit integer)

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
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;

///////////////////////////////////////////////////////////////////
//// ShortToken

/**
 A token that contains a signed 16-bit integer number.  Generally, this
 class handles overflow the same way that overflow Java native types
 are handled. In other words, overflow just past java.lang.Short.MAX_VALUE
 results in negative values close to java.lang.Short.MIN_VALUE.

 @author Isaac Liu, based on IntToken by Neil Smyth, Yuhong Xiong, Steve Neuendorffer, contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (iliu)
 @Pt.AcceptedRating Red (iliu)
 */
public class ShortToken extends ScalarToken {
    /** Construct a token with short 0.
     */
    public ShortToken() {
        _value = 0;
    }

    /** Construct a token with the specified value.
     *  @param value The specified value.
     */
    public ShortToken(final short value) {
        _value = value;
    }

    /** Construct a ShortToken with the specified integer value.
     *  This method's cast to (byte) keeps only the low
     *  order 16 bits of the integer.
     *  @param value The specified value.
     */
    public ShortToken(int value) {
        _value = (short) value;
    }

    /** Construct an ShortToken from the specified string.
     *  @param init The specified string.
     *  @exception IllegalActionException If the token could not
     *  be created with the given String.
     */
    public ShortToken(String init) throws IllegalActionException {
        if (init == null || init.equals("nil")) {
            throw new IllegalActionException(notSupportedNullNilStringMessage(
                    "ShortToken", init));
        }
        try {
            _value = Short.parseShort(init);
        } catch (NumberFormatException e) {
            throw new IllegalActionException(null, e, "Failed to parse \""
                    + init + "\" as a number.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value of this token as a Complex. The real part
     *  of the Complex is the value of this token, the imaginary part
     *  is set to 0.
     *  @return A Complex.
     */
    @Override
    public Complex complexValue() {
        return new Complex(_value);
    }

    /** Convert the specified token into an instance of ShortToken.
     *  This method does lossless conversion.  The units of the
     *  returned token will be the same as the units of the given
     *  token.  If the argument is already an instance of ShortToken, it
     *  is returned without any change.  If the argument is a
     *  nil token, then {@link #NIL} is returned.
     *  Otherwise, if the argument is below ShortToken in the type
     *  hierarchy, it is converted to an instance of ShortToken or one
     *  of the subclasses of ShortToken and returned. If none of the
     *  above condition is met, an exception is thrown.
     *
     *  @param token The token to be converted to a ShortToken.
     *  @return A ShortToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static ShortToken convert(Token token) throws IllegalActionException {
        if (token instanceof ShortToken) {
            return (ShortToken) token;
        }
        if (token.isNil()) {
            return ShortToken.NIL;
        }

        int compare = TypeLattice.compare(BaseType.SHORT, token);

        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(token, "short"));
        }

        compare = TypeLattice.compare(BaseType.UNSIGNED_BYTE, token);

        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            UnsignedByteToken unsignedByteToken = UnsignedByteToken
                    .convert(token);
            ShortToken result = new ShortToken(unsignedByteToken.shortValue());
            if (unsignedByteToken._unitCategoryExponents != null
                    && !UnitUtilities
                            .isUnitless(unsignedByteToken._unitCategoryExponents)) {
                result._unitCategoryExponents = unsignedByteToken
                        ._copyOfCategoryExponents();
            }
            return result;
        }

        // The argument is below UnsignedByteToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(notSupportedConversionMessage(token,
                "short"));
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    @Override
    public double doubleValue() {
        return _value;
    }

    /** Return true if the argument's class is ShortToken and it has the
     *  same values as this token.
     *  @param object An instance of Object.
     *  @return True if the argument is an ShortToken with the same
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

        if (this.isNil() || ((ShortToken) object).isNil()) {
            return false;
        }

        if (((ShortToken) object).shortValue() == _value) {
            return true;
        }

        return false;
    }

    /** Return the value in the token as a fixpoint.
     *  @return The value contained in this token as a fixpoint.
     */
    @Override
    public FixPoint fixValue() {
        // FIXME: Does FixPoint need to specially handle Short?
        return new FixPoint(_value);
    }

    /** Return the value in the token as a float.
     *  @return The value contained in this token as a float.
     */
    @Override
    public float floatValue() {
        return _value;
    }

    /** Return the type of this token.
     *  @return BaseType.SHORT
     */
    @Override
    public Type getType() {
        return BaseType.SHORT;
    }

    /** Return a hash code value for this token. This method just returns the
     *  contained short casted to integer.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        return _value;
    }

    /** Return the value in the token as an int.
     *  @return The int value contained in this token.
     */
    @Override
    public int intValue() {
        return _value;
    }

    /** Return true if the token is nil, (aka null or missing).
     *  Nil or missing tokens occur when a data source is sparsely populated.
     *  @return True if the token is the {@link #NIL} token.
     */
    @Override
    public boolean isNil() {
        // We use a method here so that we can easily change how
        // we determine if a token is nil without modify lots of classes.
        // Can't use equals() here, or we'll go into an infinite loop.
        return this == ShortToken.NIL;
    }

    /** Returns a token representing the result of shifting the bits
     *  of this token towards the most significant bit, filling the
     *  least significant bits with zeros.
     *  @param bits The number of bits to shift.
     *  @return The left shift.
     *  If this token is nil, then {@link #NIL} is returned.
     */
    @Override
    public ScalarToken leftShift(int bits) {
        if (isNil()) {
            return ShortToken.NIL;
        }
        return new ShortToken(_value << bits);
    }

    /** Returns a token representing the result of shifting the bits
     *  of this token towards the least significant bit, filling the
     *  most significant bits with zeros.  This treats the value as an
     *  unsigned number, which may have the effect of destroying the
     *  sign of the value.
     *  @param bits The number of bits to shift.
     *  @return The logical right shift.
     *  If this token is nil, then {@link #NIL} is returned.
     */
    @Override
    public ScalarToken logicalRightShift(int bits) {
        if (isNil()) {
            return ShortToken.NIL;
        }
        short returnValue = (short) (_value >>> bits);
        returnValue = (short) (returnValue & 0x7FFF);
        return new ShortToken(returnValue);
    }

    /** Return the value in the token as a long.
     *  @return The short value contained in this token as a long.
     */
    @Override
    public long longValue() {
        return _value;
    }

    /** Returns an ShortToken with value 1.
     *  @return An ShortToken with value 1.
     */
    @Override
    public Token one() {
        return ONE;
    }

    /** Returns a token representing the result of shifting the bits
     *  of this token towards the least significant bit, filling the
     *  most significant bits with the sign of the value.  This preserves
     *  the sign of the result.
     *  @param bits The number of bits to shift.
     *  @return The right shift.
     *  If this token is nil, then {@link #NIL} is returned.
     */
    @Override
    public ScalarToken rightShift(int bits) {
        if (isNil()) {
            return ShortToken.NIL;
        }
        return new ShortToken(_value >> bits);
    }

    /** Return the value in the token as a short.
     *  @return The value contained in this token as a short.
     */
    @Override
    public short shortValue() {
        return _value;
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  If this token has a unit, the return string also includes a unit
     *  string produced by the unitsString() method in the super class.
     *  @return A String representing the int value and the units (if
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
        return Short.toString(_value) + "s" + unitString;
    }

    /** Returns an ShortToken with value 0.
     *  @return An ShortToken with value 0.
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
    public static final ShortToken NIL = new ShortToken(Short.MAX_VALUE);

    /** A ShortToken with the value 1.0. */
    public static final ShortToken ONE = new ShortToken(1);

    /** A ShortToken with the value 0.0. */
    public static final ShortToken ZERO = new ShortToken(0);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is
     *  returned.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.
     *  @return An ShortToken.
     */
    @Override
    protected ScalarToken _absolute() {
        ShortToken result;

        if (_value >= 0) {
            result = this;
        } else {
            result = new ShortToken(-_value);
        }

        return result;
    }

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an ShortToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new ShortToken containing the result.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument) {
        short sum = (short) (_value + ((ShortToken) rightArgument).shortValue());
        return new ShortToken(sum);
    }

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.  It is assumed that the type of the argument
     *  is an ShortToken.
     *  @param rightArgument The ShortToken to bitwise AND with this one.
     *  @return The bitwise AND.
     */
    @Override
    protected ScalarToken _bitwiseAnd(ScalarToken rightArgument) {
        short sum = (short) (_value & ((ShortToken) rightArgument).shortValue());
        return new ShortToken(sum);
    }

    /** Returns a token representing the bitwise NOT of this token.
     *  @return The bitwise NOT of this token.
     */
    @Override
    protected ScalarToken _bitwiseNot() {
        ShortToken result = new ShortToken(~_value);
        return result;
    }

    /** Returns a token representing the bitwise OR of this token and
     *  the given token.  It is assumed that
     *  the type of the argument is an ShortToken.
     *  @param rightArgument The ShortToken to bitwise OR with this one.
     *  @return The bitwise OR.
     */
    @Override
    protected ScalarToken _bitwiseOr(ScalarToken rightArgument) {
        short sum = (short) (_value | ((ShortToken) rightArgument).shortValue());
        return new ShortToken(sum);
    }

    /** Returns a token representing the bitwise XOR of this token and
     *  the given token.  It is assumed that
     *  the type of the argument is an ShortToken.
     *  @param rightArgument The ShortToken to bitwise XOR with this one.
     *  @return The bitwise XOR.
     */
    @Override
    protected ScalarToken _bitwiseXor(ScalarToken rightArgument) {
        short sum = (short) (_value ^ ((ShortToken) rightArgument).shortValue());
        return new ShortToken(sum);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an ShortToken.
     *  @param rightArgument The token to divide this token by.
     *  @return A new ShortToken containing the result.
     */
    @Override
    protected ScalarToken _divide(ScalarToken rightArgument) {
        short quotient = (short) (_value / ((ShortToken) rightArgument)
                .shortValue());
        return new ShortToken(quotient);
    }

    /** Test whether the value of this token is close to the first
     *  argument, where "close" means that the distance between their
     *  values is less than or equal to the second argument. It is
     *  assumed that the type of the first argument is ShortToken.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon The distance.
     *  @return A token containing true if the value of the first
     *   argument is close to the value of this token.
     */
    @Override
    protected BooleanToken _isCloseTo(ScalarToken rightArgument, double epsilon) {
        // NOTE: This code is duplicated in
        // ptolemy.math.IntegerMatrixMath.within(); if this
        // implementation changes, also change the corresponding
        // implementation there.
        double right = ((ShortToken) rightArgument).doubleValue();
        double left = doubleValue();

        if (right > left + epsilon || right < left - epsilon) {
            return BooleanToken.FALSE;
        } else {
            return BooleanToken.TRUE;
        }
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is ShortToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        ShortToken convertedArgument = (ShortToken) rightArgument;
        return BooleanToken
                .getInstance(_value < convertedArgument.shortValue());
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is an ShortToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new ShortToken containing the result.
     */
    @Override
    protected ScalarToken _modulo(ScalarToken rightArgument) {
        short remainder = (short) (_value % ((ShortToken) rightArgument)
                .shortValue());
        return new ShortToken(remainder);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is an ShortToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new ShortToken containing the result.
     */
    @Override
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        short product = (short) (_value * ((ShortToken) rightArgument)
                .shortValue());
        return new ShortToken(product);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is an ShortToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new ShortToken containing the result.
     */
    @Override
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        short difference = (short) (_value - ((ShortToken) rightArgument)
                .shortValue());
        return new ShortToken(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private final short _value;
}
