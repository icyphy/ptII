/* A token that contains a byte number in the range 0 through 255.

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

@ProposedRating Yellow (neuendor@robotics.eecs.berkeley.edu)
@AcceptedRating Red (winthrop@robotics.eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;


//////////////////////////////////////////////////////////////////////////
//// ByteToken
/**
A token that contains a byte number in the range 0 through 255.
This is in contrast to Java's default that a byte is in the range
-128 through 127.  To get our desired behavior we need only apply
a custom conversion <i>unsignedConvert()</i> from byte to integer.
Conversion to byte already gives the desired behavior of truncating
the value, keeping the lowest 8 bits.  Thus, for example, the integers
-1 and 1023 both truncate to the byte 255.  Throughout the code, casts
(byte) to byte occur.  These are necessary because Java converts to
integer or higher by default when doing arithmetic.  Java does this
even when the types of both operands are byte.

@author Winthrop Williams, Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ByteToken extends ScalarToken {

    /** Construct a token with byte 0.
     */
    public ByteToken() {
        _value = 0;
    }

    /** Construct a ByteToken with the specified byte value.
     */
    public ByteToken(byte value) {
        _value = value;
    }

    /** Construct a ByteToken from the specified integer.  This takes
     * the low 8 bits and discards the rest.  Having this form (which
     * takes an integer) in addition to the one above (which takes a
     * byte) avoids us having to cast to byte when calling
     * constructors such as those called from within the one() and
     * zero() methods.
     */
    public ByteToken(int value) {
        _value = (byte)value;
    }

    /** Construct a ByteToken from the specified string.
     *  @exception IllegalActionException If the token could not
     *   be created from the given string.
     */
    public ByteToken(String init) throws IllegalActionException {
	try {
            _value = (Byte.valueOf(init)).byteValue();
        } catch (NumberFormatException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value of this token as a Complex. The real part
     *  of the Complex is the value of this token, the imaginary part
     *  is set to 0.
     *  @return A Complex.
     */
    public Complex complexValue() {
        return new Complex((double)unsignedConvert(_value));
    }

    /** Convert the specified token into an instance of ByteToken.
     *  This method does lossless conversion, or throws an exception
     *  if it cannot.  If the argument is already an instance of
     *  ByteToken, it is returned without any change. Otherwise, if
     *  the argument is below ByteToken in the type hierarchy, it is
     *  converted to an instance of ByteToken or one of the subclasses
     *  of ByteToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a ByteToken.
     *  @return A ByteToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static ByteToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof ByteToken) {
            return (ByteToken)token;
        }

        int compare = TypeLattice.compare(BaseType.BYTE, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "byte"));
        }

        throw new IllegalActionException(
                notSupportedConversionMessage(token, "byte"));
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    public double doubleValue() {
        return (double)unsignedConvert(_value);
    }

    /** Return true if the argument is an instance of ByteToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of ByteToken with the
     *  same value.
     */
    public boolean equals(Object object) {
	// This test rules out subclasses.
	if (object.getClass() != ByteToken.class) {
	    return false;
	}

	if (((ByteToken)object).byteValue() == _value) {
	    return true;
	}
	return false;
    }

    /** Return the type of this token.
     *  @return BaseType.BYTE
     */
    public Type getType() {
        return BaseType.BYTE;
    }

    /** Return a hash code value for this token. This method just returns
     *  the value of this token.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	return _value;
    }

    /** Return the value in the token as a byte.
     *  @return The byte value contained in this token.
     */
    public byte byteValue() {
        return _value;
    }

    //FIXME - Seems this caused some problem so I took it out.
    // Now that it is back in it may cause trouble again.
    /** Return the value in the token as an int.
     *  @return The byte  value contained in this token as a int.
     */
    public int intValue() {
        return unsignedConvert(_value);
    }

    /** Return the value in the token as a long.
     *  @return The byte  value contained in this token as a long.
     */
    public long longValue() {
        return (long) unsignedConvert(_value);
    }

    /** Returns a new ByteToken with value 1.
     *  @return A new ByteToken with value 1.
     */
    public Token one() {
        return new ByteToken(1);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  If this token has a unit, the return string also includes a unit
     *  string produced by the unitsString() method in the super class.
     *  @return A String representing the byte value and the units (if
     *   any) of this token.
     *  @see ptolemy.data.ScalarToken#unitsString
     */
    public String toString() {
	String unitString = "";
	if ( !_isUnitless()) {
	    unitString = " * " + unitsString();
	}
        return Byte.toString(_value) + unitString;
    }

    /** Convert the given unsigned byte to an integer.  This is
     *  different from the default <i>(int)</i> conversion.  The
     *  default (int) conversion yields negative values from bytes
     *  whose high bit is true.  This is necessary because Java, by
     *  default, interprets bytes as signed numbers.  We are
     *  interested in unsigned bytes.  Conversion from integers to
     *  bytes requires no hand coding.  This is because Java converts
     *  to bytes by keeping just the least significant 8 bits.  It
     *  pays no attention to sign in that conversion.
     *  @param byte The byte to convert.
     *  @return An integer between 0 and 255.
     */
    public int unsignedConvert(byte value) {
        int intValue = value;
        if (intValue < 0) {
	    intValue += 256;
	}
	return intValue;
    }

    /** Returns a new ByteToken with value 0.
     *  @return A new ByteToken with value 0.
     */
    public Token zero() {
        return new ByteToken(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.  Since we have defined
     *  the byte as ranging from 0 through 255, its absolute value is
     *  equal to itself.  Although this method does not actually do
     *  anything, it is included to make the ByteToken interface
     *  compatible with the interfaces of the other types.
     *  @return An ByteToken.
     */
    protected ScalarToken _absolute() {
        return this;
    }

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an ByteToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new ByteToken containing the result.
     */
    protected ScalarToken _add(ScalarToken rightArgument) {
        byte sum = (byte)(_value + ((ByteToken)rightArgument).byteValue());
        return new ByteToken(sum);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an ByteToken. If two bytes are
     *  divided, then the result will be a byte containing the
     *  quotient.  For example, 255 divided by 10 returns 25.
     *  @param rightArgument The token to divide this token by.
     *  @return A new ByteToken containing the result.
     */
    protected ScalarToken _divide(ScalarToken rightArgument) {
        byte quotient = (byte) (unsignedConvert(_value)
                / unsignedConvert(((ByteToken)rightArgument).byteValue()));
        return new ByteToken(quotient);
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  ByteToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isCloseTo(
            ScalarToken rightArgument, double epsilon)
            throws IllegalActionException {
        return _isEqualTo(rightArgument);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  ByteToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isEqualTo(ScalarToken rightArgument)
            throws IllegalActionException {
        ByteToken convertedArgument = (ByteToken)rightArgument;
        return BooleanToken.getInstance(
                _value == convertedArgument.byteValue());
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is ByteToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        ByteToken convertedArgument = (ByteToken)rightArgument;
        return BooleanToken.getInstance(
                _value < convertedArgument.byteValue());
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is ByteToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new ByteToken containing the result.
     */
    protected ScalarToken _modulo(ScalarToken rightArgument) {
        byte remainder = (byte) (unsignedConvert(_value)
                % unsignedConvert(((ByteToken)rightArgument).byteValue()));
        return new ByteToken(remainder);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is ByteToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new ByteToken containing the result.
     */
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        byte product = (byte) (unsignedConvert(_value)
                * unsignedConvert(((ByteToken)rightArgument).byteValue()));
        return new ByteToken(product);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is ByteToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new ByteToken containing the result.
     */
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        byte difference = (byte) (unsignedConvert(_value)
                * unsignedConvert(((ByteToken)rightArgument).byteValue()));
        return new ByteToken(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private byte _value;
}
