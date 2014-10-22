/* A token that contains a long integer.

 Copyright (c) 1998-2014 The Regents of the University of California.
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


 added truncatedUnsignedByteValue.  Note that this needs to be greatly
 extended to be made useful.
 */
package ptolemy.data;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.unit.UnitUtilities;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// LongToken

/**
 A token that contains a signed 64-bit long integer.  Generally, this
 class handles overflow the same way that overflow for Java native
 types are handled.  In other words, overflow just past
 java.lang.Long.MAX_VALUE results in negative values close to
 java.lang.Long.MIN_VALUE.

 @author Neil Smyth, Yuhong Xiong, Steve Neuendorffer, contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (cxh) nil token, ONE, ZERO
 @Pt.AcceptedRating Red (cxh)
 */
public class LongToken extends ScalarToken {
    /** Construct a token with long integer 0.
     */
    public LongToken() {
        _value = 0;
    }

    /** Construct a token with the specified value.
     *  @param value The specified value.
     */
    public LongToken(long value) {
        _value = value;
    }

    /** Construct a token from the given String.
     *  @param init The specified string, for example <code>1L</code>
     *  <code>2L</code>.  Note that <code>3</code> will also result
     *  a LongToken with a value of 3 being created.
     *  @exception IllegalActionException If the Token could not
     *   be created with the given String.
     */
    public LongToken(String init) throws IllegalActionException {
        if (init == null || init.equals("nil")) {
            throw new IllegalActionException(notSupportedNullNilStringMessage(
                    "LongToken", init));
        }

        // Throw away the ending L or l, if necessary.
        init = init.trim();

        if (init.endsWith("L") || init.endsWith("l")) {
            init = init.substring(0, init.length() - 1);
        }

        try {
            _value = Long.parseLong(init);
        } catch (NumberFormatException e) {
            throw new IllegalActionException(null, e, "Failed to parse \""
                    + init + "\" as a number.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an instance of LongToken.
     *  This method does lossless conversion.  The units of the
     *  returned token will be the same as the units of the given
     *  token.  If the argument is already an instance of LongToken,
     *  it is returned without any change.  If the argument is
     *  a nil token, then a new nil Token is returned, see {@link
     *  #NIL}.  Otherwise, if the argument is below LongToken in the
     *  type hierarchy, it is converted to an instance of LongToken or
     *  one of the subclasses of LongToken and returned. If none of
     *  the above condition is met, an exception is thrown.
     *
     *  @param token The token to be converted to a LongToken.
     *  @return A LongToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static LongToken convert(Token token) throws IllegalActionException {
        if (token instanceof LongToken) {
            return (LongToken) token;
        }

        if (token.isNil()) {
            return LongToken.NIL;
        }

        int compare = TypeLattice.compare(BaseType.LONG, token);

        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(token, "long"));
        }

        compare = TypeLattice.compare(BaseType.INT, token);

        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntToken intToken = IntToken.convert(token);
            LongToken result = new LongToken(intToken.longValue());
            if (intToken._unitCategoryExponents != null
                    && !UnitUtilities
                            .isUnitless(intToken._unitCategoryExponents)) {
                result._unitCategoryExponents = intToken
                        ._copyOfCategoryExponents();
            }
            return result;
        }

        throw new IllegalActionException(notSupportedConversionMessage(token,
                "long"));
    }

    /**  Return true if the argument's class is LongToken and it has the
     *  same values as this token.
     *  @param object An instance of Object.
     *  @return True if the argument is an IntToken with the same
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

        if (isNil() || ((LongToken) object).isNil()) {
            return false;
        }

        if (((LongToken) object).longValue() == _value) {
            return true;
        }

        return false;
    }

    /** Return the type of this token.
     *  @return BaseType.LONG_MATRIX
     */
    @Override
    public Type getType() {
        return BaseType.LONG;
    }

    /** Return a hash code value for this token. This method returns the
     *  value of this token, casted to integer.
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
        // Can't use equals() here, or we'll go into an infinite loop.
        return this == LongToken.NIL;
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
            return IntToken.NIL;
        }
        return new LongToken(_value << bits);
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
            return IntToken.NIL;
        }
        return new LongToken(_value >>> bits);
    }

    /** Return the value in the token as a long.
     */
    @Override
    public long longValue() {
        return _value;
    }

    /** Returns a LongToken with value 1.
     *  @return A LongToken with value 1.
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
            return IntToken.NIL;
        }
        return new LongToken(_value >> bits);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A String formed using java.lang.Long.toString().
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

        return Long.toString(_value) + "L" + unitString;
    }

    /** Return the value in the token truncated to an unsignedByte.
     *  @return The truncated value
     *  @exception IllegalActionException If the value is not in the
     *  range of an unsigned byte.
     */
    public UnsignedByteToken truncatedUnsignedByteValue()
            throws IllegalActionException {
        if (isNil()) {
            return UnsignedByteToken.NIL;
        }
        if (_value < 0 || _value > 255) {
            throw new IllegalActionException("Value cannot be represented"
                    + " as an unsigned Byte");
        } else {
            return new UnsignedByteToken((int) _value);
        }
    }

    /** Returns a LongToken with value 0.
     *  @return A LongToken with value 0.
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
    public static final LongToken NIL = new LongToken(Long.MAX_VALUE);

    /** A LongToken with the value 1.0. */
    public static final LongToken ONE = new LongToken(1);

    /** A LongToken with the value 0.0. */
    public static final LongToken ZERO = new LongToken(0);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.
     *  @return An LongToken.
     */
    @Override
    protected ScalarToken _absolute() {
        LongToken result;

        if (_value >= 0) {
            result = this;
        } else {
            result = new LongToken(-_value);
        }

        return result;
    }

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an LongToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new LongToken containing the result.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument) {
        long sum = _value + ((LongToken) rightArgument).longValue();
        return new LongToken(sum);
    }

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.  It is assumed that the type of the argument
     *  is an LongToken.
     *  @param rightArgument The LongToken to bitwise AND with this one.
     *  @return The bitwise AND.
     */
    @Override
    protected ScalarToken _bitwiseAnd(ScalarToken rightArgument) {
        long sum = _value & ((LongToken) rightArgument).longValue();
        return new LongToken(sum);
    }

    /** Returns a token representing the bitwise NOT of this token.  It
     *  is assumed that the type of the argument is an LongToken.
     *  @return The bitwise NOT of this token.
     */
    @Override
    protected ScalarToken _bitwiseNot() {
        LongToken result = new LongToken(~_value);
        return result;
    }

    /** Returns a token representing the bitwise OR of this token and
     *  the given token.  It is assumed that the type of the argument
     *  is an LongToken.
     *  @param rightArgument The LongToken to bitwise OR with this one.
     *  @return The bitwise OR.
     */
    @Override
    protected ScalarToken _bitwiseOr(ScalarToken rightArgument) {
        long sum = _value | ((LongToken) rightArgument).longValue();
        return new LongToken(sum);
    }

    /** Returns a token representing the bitwise XOR of this token and
     *  the given token.  It is assumed that the type of the argument
     *  is an LongToken.
     *  @param rightArgument The LongToken to bitwise XOR with this one.
     *  @return The bitwise XOR.
     */
    @Override
    protected ScalarToken _bitwiseXor(ScalarToken rightArgument) {
        long sum = _value ^ ((LongToken) rightArgument).longValue();
        return new LongToken(sum);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an LongToken
     *  @param rightArgument The token to divide this token by.
     *  @return A new LongToken containing the result.
     */
    @Override
    protected ScalarToken _divide(ScalarToken rightArgument) {
        long quotient = _value / ((LongToken) rightArgument).longValue();
        return new LongToken(quotient);
    }

    /** Test whether the value of this token is close to the first
     *  argument, where "close" means that the distance between their
     *  values is less than or equal to the second argument. It is
     *  assumed that the type of the first argument is LongToken.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon The distance.
     *  @return A token containing true if the value of the first
     *   argument is close to the value of this token.
     */
    @Override
    protected BooleanToken _isCloseTo(ScalarToken rightArgument, double epsilon) {
        // NOTE: This code is duplicated in
        // ptolemy.math.LongMatrixMath.within(); if this
        // implementation changes, also change the corresponding
        // implementation there.
        long right = ((LongToken) rightArgument).longValue();
        long left = longValue();
        long distance = Math.round(Math.floor(epsilon));

        if (right > left + distance || right < left - distance) {
            return BooleanToken.FALSE;
        } else {
            return BooleanToken.TRUE;
        }
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is LongToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        LongToken convertedArgument = (LongToken) rightArgument;
        return BooleanToken.getInstance(_value < convertedArgument.longValue());
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is an LongToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new LongToken containing the result.
     */
    @Override
    protected ScalarToken _modulo(ScalarToken rightArgument) {
        long remainder = _value % ((LongToken) rightArgument).longValue();
        return new LongToken(remainder);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is an LongToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new LongToken containing the result.
     */
    @Override
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        long product = _value * ((LongToken) rightArgument).longValue();
        return new LongToken(product);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is an LongToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new LongToken containing the result.
     */
    @Override
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        long difference = _value - ((LongToken) rightArgument).longValue();
        return new LongToken(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private long _value;
}
