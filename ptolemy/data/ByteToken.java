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

@ProposedRating Red (winthrop@robotics.eecs.berkeley.edu)
@AcceptedRating Red (winthrop@robotics.eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
//import ptolemy.data.expr.ASTPtRootNode;
//import ptolemy.data.expr.PtParser;
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

@author Winthrop Williams
@version $Id$
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

    /** Return a ByteToken containing the absolute value (which, for
     *  bytes, equals the value) of this token.  Since we have defined
     *  the byte as ranging from 0 through 255, its absolute value is
     *  equal to itself.  Although this method does not actually do
     *  anything, it is included to make the ByteToken interface
     *  compatible with the interfaces of the other types.
     *  @return An ByteToken.
     */
    public ScalarToken absolute() {
        ByteToken result;
	result = new ByteToken(_value);
        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument.  Type resolution also occurs here, with
     *  the returned token type chosen to achieve a lossless conversion.
     *  @param rightArgument The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be added to this token, or
     *   the units of this token and the argument token are not the same.
     */
    public Token add(ptolemy.data.Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArgument);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            ByteToken byteToken;
            if (typeInfo == CPO.HIGHER) {
                byteToken = (ByteToken)ByteToken.convert(rightArgument);
            } else {
                byteToken = (ByteToken)rightArgument;
            }
            byte sum = (byte)(_value + byteToken.byteValue());
            ByteToken result = new ByteToken(sum);
            if ( !_areUnitsEqual(byteToken)) {
                throw new IllegalActionException("ByteToken.add: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + byteToken.unitsString());
            }
            result._unitCategoryExponents = this._copyOfCategoryExponents();
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.addReverse(this);
        } else {
            throw new IllegalActionException("ByteToken.add: Cannot add "
                    + this.getClass().getName() + " " + this.toString()
                    + " and "
                    + rightArgument.getClass().getName() + " "
                    + rightArgument.toString());
        }
    }

    /** Return a new token whose value is the sum of this token and
     *  the argument. Type resolution also occurs here, with the
     *  returned token type chosen to achieve a lossless conversion.
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
     *   cannot be carried out.  */
    public static Token convert(Token token) throws IllegalActionException {
        int compare = TypeLattice.compare(BaseType.BYTE, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("ByteToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    " is higher or incomparable with ByteToken in the type " +
                    "hierarchy.");
        }

        if (token instanceof ByteToken) {
            return token;
        }

	// This is where conversion from a lower type (such as nibble)
        // would be carried out.  But byte is the bottom of this string
        // in the CPO so nothing is done here.  (However, when creating
        // this byte type, I had to also add a section here to the int
        // type!  Previously int was bottom.)

        throw new IllegalActionException("Cannot convert from token " +
                "type: " + token.getClass().getName() + " to an ByteToken");
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  Type resolution
     *  also occurs here, with the returned token type chosen to
     *  achieve a lossless conversion.  If two bytes are divided, then
     *  the result will be a byte containing the quotient.  For
     *  example, 255 divided by 10 returns 25.
     *  @param divisor The token to divide this token by
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *  of a type that cannot divide this token.
     */
    public Token divide(Token divisor) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, divisor);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            ByteToken byteToken;
            if (typeInfo == CPO.HIGHER) {
                byteToken = (ByteToken)ByteToken.convert(divisor);
            } else {
                byteToken = (ByteToken)divisor;
            }
            byte quotient = (byte) (unsignedConvert(_value)
	            / unsignedConvert(byteToken.byteValue()));
            ByteToken result = new ByteToken(quotient);
            // compute units
            result._unitCategoryExponents =
                _subtractCategoryExponents(byteToken);
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return divisor.divideReverse(this);
        } else {
            throw new IllegalActionException(
                    "ByteToken.divide: Cannot divide "
                    + this.getClass().getName() + " " + this.toString()
                    + " by "
                    + divisor.getClass().getName() + " " + divisor.toString());
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  divided by the value of this token. Type resolution
     *  also occurs here, with the returned token type chosen to achieve
     *  a lossless conversion.
     *  @param dividend The token to be divided by the value of this token.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be divided by this token's value.
     *  @return A new token containing the result.
     */
    public Token divideReverse(Token dividend) throws IllegalActionException {
        ByteToken byteToken = (ByteToken)ByteToken.convert(dividend);
        byte quotient = (byte)(unsignedConvert(byteToken.byteValue())
                / unsignedConvert(_value));
        ByteToken result = new ByteToken(quotient);

        // compute units
        result._unitCategoryExponents =
            byteToken._subtractCategoryExponents(this);
        return result;
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

    /** Test the value and units of this token and the argument token
     *  for equality.  Units need not match, this merely causes the
     *  test to return false.  Type resolution also occurs here, and
     *  lossless conversion is performed prior to comparison.  The
     *  returned type is boolean, regardless of the types compared.
     *  @param token The token with which to test equality.
     *  @return a boolean token that contains the value true if both
     *   the value and the units of this token are equal to those of
     *   the argument token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, token);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            ByteToken byteToken;
            if (typeInfo == CPO.HIGHER) {
                byteToken = (ByteToken)ByteToken.convert(token);
            } else {
                byteToken = (ByteToken)token;
            }

            if (_value == byteToken.byteValue()
                    && _areUnitsEqual(byteToken)) {
                return new BooleanToken(true);
            } else {
                return new BooleanToken(false);
            }

        } else if (typeInfo == CPO.LOWER) {
            return token.isEqualTo(this);
        } else {
            throw new IllegalActionException("ByteToken.isEqualTo: "
                    + "Cannot compare "
                    + this.getClass().getName() + " " + this.toString()
                    + " and "
                    + token.getClass().getName() + " " + token.toString()
                    + " for equality.");
        }
    }

    /** Check if the value of this token is strictly less than that of the
     *  argument token.  Units must match or an exception is thrown.
     *  @param token A ScalarToken.
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
            ByteToken byteToken;
            if (typeInfo == CPO.HIGHER) {
                byteToken = (ByteToken)ByteToken.convert(token);
            } else {
                byteToken = (ByteToken)token;
            }
            if ( !_areUnitsEqual(byteToken)) {
                throw new IllegalActionException("ByteToken.isLessThan: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + byteToken.unitsString());
            }
            if (_value < byteToken.byteValue()) {
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
            throw new IllegalActionException("ByteToken.isLessThan: "
                    + "Cannot check whether "
                    + this.getClass().getName() + " " + this.toString()
                    + " is less than "
                    + token.getClass().getName() + " " + token.toString());
        }
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

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  Type resolution also
     *  occurs here, with the returned token type chosen to achieve a
     *  lossless conversion.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be  used with modulo, or the units of
     *   this token and the argument token are not the same.
     */
    public Token modulo(Token rightArgument) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArgument);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            ByteToken byteToken;
            if (typeInfo == CPO.HIGHER) {
                byteToken = (ByteToken)ByteToken.convert(rightArgument);
            } else {
                byteToken = (ByteToken)rightArgument;
            }
            byte remainder = (byte) (unsignedConvert(_value)
                    % unsignedConvert(byteToken.byteValue()));
            ByteToken result = new ByteToken(remainder);
            if ( !_areUnitsEqual(byteToken)) {
                throw new IllegalActionException("ByteToken.modulo: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + byteToken.unitsString());
            }
            result._unitCategoryExponents = this._copyOfCategoryExponents();
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.moduloReverse(this);
        } else {
            throw new IllegalActionException("ByteToken.modulo: "
                    + "Cannot compute the modulo of "
                    + this.getClass().getName() + " " + this.toString()
                    + " and "
                    + rightArgument.getClass().getName() + " "
                    + rightArgument.toString());
        }
    }

    /** Return a new token whose value is the value of the argument
     *  token modulo the value of this token.  Type resolution also
     *  occurs here, with the returned token type chosen to achieve a
     *  lossless conversion.
     *  @param leftArgument The token to apply modulo to by the value of
     *   this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can apply modulo by this token, or
     *   if the units of this token are not the same as those of the
     *   argument.
     */
    public Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        ByteToken byteToken =
            (ByteToken)ByteToken.convert(leftArgument);

        byte remainder = (byte) (unsignedConvert(byteToken.byteValue())
                % unsignedConvert(_value));
        ByteToken result = new ByteToken(remainder);
        if ( !_areUnitsEqual(byteToken)) {
            throw new IllegalActionException("ByteToken.moduloReverse: "
                    + "The units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + byteToken.unitsString());
        }
        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  Type
     *  resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightFactor The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *  not of a type that can be multiplied to this token.
     */
    public Token multiply(Token rightFactor) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightFactor);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            ByteToken byteToken;
            if (typeInfo == CPO.HIGHER) {
                byteToken = (ByteToken)ByteToken.convert(rightFactor);
            } else {
                byteToken = (ByteToken)rightFactor;
            }
            byte product = (byte)(unsignedConvert(_value)
                    * unsignedConvert(byteToken.byteValue()));
            ByteToken result = new ByteToken(product);
            // compute units
            result._unitCategoryExponents = _addCategoryExponents(byteToken);
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightFactor.multiplyReverse(this);
        } else {
            throw new IllegalActionException(
                    "ByteToken.multiply: Cannot multiply "
                    + this.getClass().getName() + " " + this.toString()
                    + " with "
                    + rightFactor.getClass().getName() + " "
                    + rightFactor.toString());
        }
    }

    /** Return a new token whose value is the value of the argument
     *  token multiplied by the value of this token.  Type resolution
     *  also occurs here, with the returned token type chosen to
     *  achieve a lossless conversion.
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

    /** Returns a new ByteToken with value 1.
     *  @return A new ByteToken with value 1.
     */
    public Token one() {
        return new ByteToken(1);
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  Type
     *  resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be subtracted from this token, or the units
     *   of this token and the argument token are not the same.
     */
    public Token subtract(Token rightArgument) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArgument);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            ByteToken byteToken;
            if (typeInfo == CPO.HIGHER) {
                byteToken = (ByteToken)ByteToken.convert(rightArgument);
            } else {
                byteToken = (ByteToken)rightArgument;
            }
            byte difference = (byte) (unsignedConvert(_value)
                    - unsignedConvert(byteToken.byteValue()));
            ByteToken result = new ByteToken(difference);
            if ( !_areUnitsEqual(byteToken)) {
                throw new IllegalActionException("ByteToken.subtract: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + byteToken.unitsString());
            }
            result._unitCategoryExponents = this._copyOfCategoryExponents();
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.subtractReverse(this);
        } else {
            throw new IllegalActionException("ByteToken.subtract: "
                    + "Cannot subtract "
                    + this.getClass().getName() + " " + this.toString()
                    + " by "
                    + rightArgument.getClass().getName() + " "
                    + rightArgument.toString());
        }
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.  Type
     *  resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be added to this token, or the units of
     *  this token and the argument token are not the same.
     *  FIXME: byte (and int too) have less code in the
     *  subtractReverse() method.  Something is not being handled
     *  here.  CPO is not even being checked!!
     */
    public Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        ByteToken byteToken = (ByteToken)ByteToken.convert(leftArgument);
        byte difference = (byte) (unsignedConvert(byteToken.byteValue())
                - unsignedConvert(_value));
        ByteToken result = new ByteToken(difference);
        if ( !_areUnitsEqual(byteToken)) {
            throw new IllegalActionException("ByteToken.subtractReverse: "
                    + "The units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + byteToken.unitsString());
        }
        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
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
    ////                         private variables                 ////
    private byte _value;
}

