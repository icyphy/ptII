/* A token that contains an integer number.

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
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// IntToken
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
     *  @exception IllegalActionException If the token could not
     *   be created with the given String.
     */
    public IntToken(String init) throws IllegalActionException {
	try {
            _value = (Integer.valueOf(init)).intValue();
        } catch (NumberFormatException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an IntToken containing the absolute value of the
     *  value of this token.
     *  @return An IntToken.
     */
    public ScalarToken absolute() {
        IntToken result;
        if (_value >= 0) {
            result = new IntToken(_value);
        } else {
            result = new IntToken(-_value);
        }

        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
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
            IntToken intToken;
            if (typeInfo == CPO.HIGHER) {
               intToken = (IntToken)IntToken.convert(rightArgument);
            } else {
               intToken = (IntToken)rightArgument;
            }
            int sum = _value + intToken.intValue();
            IntToken result = new IntToken(sum);
            if ( !_areUnitsEqual(intToken)) {
                throw new IllegalActionException("IntToken.add: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + intToken.unitsString());
            }
            result._unitCategoryExponents = this._copyOfCategoryExponents();
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.addReverse(this);
        } else {
            throw new IllegalActionException("IntToken.add: Cannot add "
                    + this.getClass().getName() + " " + this.toString()
                    + " and "
                    + rightArgument.getClass().getName() + " "
                    + rightArgument.toString());
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
     *   cannot be carried out.
     */
    public static Token convert(Token token) throws IllegalActionException {
        int compare = TypeLattice.compare(BaseType.INT, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("IntToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    " is higher or incomparable with IntToken in the type " +
                    "hierarchy.");
        }

        if (token instanceof IntToken) {
            return token;
        }

        // This section added when ByteToken.java created.
	compare = TypeLattice.compare(BaseType.BYTE, token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    ByteToken bytetoken = (ByteToken)ByteToken.convert(token);
	    return new IntToken(bytetoken.intValue());
	}

        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to an IntToken");
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion. If two integers are divided,
     *  the result will be an integer which is the quotient.
     *  @param divisor The token to divide this token by
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *  not of a type that can be divide this token by.
     */
    public Token divide(Token divisor) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, divisor);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            IntToken intToken;
            if (typeInfo == CPO.HIGHER) {
               intToken = (IntToken)IntToken.convert(divisor);
            } else {
               intToken = (IntToken)divisor;
            }
            int quotient = _value / intToken.intValue();
            IntToken result = new IntToken(quotient);
            // compute units
            result._unitCategoryExponents =
                            _subtractCategoryExponents(intToken);
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return divisor.divideReverse(this);
        } else {
            throw new IllegalActionException(
                    "IntToken.divide: Cannot divide "
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
     *   is not of a type that can be divided by this tokens value.
     *  @return A new token containing the result.
     */
    public Token divideReverse(Token dividend) throws IllegalActionException {
        IntToken intToken = (IntToken)IntToken.convert(dividend);
        int quotient = intToken.intValue() / _value;
        IntToken result = new IntToken(quotient);

        // compute units
        result._unitCategoryExponents =
                        intToken._subtractCategoryExponents(this);
        return result;
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    public double doubleValue() {
        return (double)_value;
    }

    /** Return true if the argument is an instance of IntToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of IntToken with the
     *  same value.
     */
    public boolean equals(Object object) {
	// This test rules out subclasses.
	if (object.getClass() != IntToken.class) {
	    return false;
	}

	if (((IntToken)object).intValue() == _value) {
	    return true;
	}
	return false;
    }

    /** Return the type of this token.
     *  @return BaseType.INT
     */
    public Type getType() {
        return BaseType.INT;
    }

    /** Return a hash code value for this token. This method just returns the
     *  contained integer.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	return _value;
    }

    /** Test the value and units of this token and the argument token
     *  for equality.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param token The token with which to test equality.
     *  @return a boolean token that contains the value true if the
     *   value and units of this token are equal to those of the argument
     *   token.
     *  @exception IllegalActionException If the argument token is
     *  not of a type that can be compared with this token.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, token);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            IntToken intToken;
            if (typeInfo == CPO.HIGHER) {
               intToken = (IntToken)IntToken.convert(token);
            } else {
               intToken = (IntToken)token;
            }

            if (_value == intToken.intValue()
                && _areUnitsEqual(intToken)) {
                return new BooleanToken(true);
            } else {
                return new BooleanToken(false);
            }

        } else if (typeInfo == CPO.LOWER) {
            return token.isEqualTo(this);
        } else {
            throw new IllegalActionException("IntToken.isEqualTo: "
                    + "Cannot compare "
                    + this.getClass().getName() + " " + this.toString()
                    + " and "
                    + token.getClass().getName() + " " + token.toString()
                    + " for equality.");
        }
    }

    /** Check if the value of this token is strictly less than that of the
     *  argument token.
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
            IntToken intToken;
            if (typeInfo == CPO.HIGHER) {
               intToken = (IntToken)IntToken.convert(token);
            } else {
               intToken = (IntToken)token;
            }
            if ( !_areUnitsEqual(intToken)) {
                throw new IllegalActionException("IntToken.isLessThan: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + intToken.unitsString());
            }
            if (_value < intToken.intValue()) {
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
            throw new IllegalActionException("IntToken.isLessThan: "
                    + "Cannot check whether "
                    + this.getClass().getName() + " " + this.toString()
                    + " is less than "
                    + token.getClass().getName() + " " + token.toString());
        }
    }

    /** Return the value in the token as an int.
     *  @return The int value contained in this token.
     */
    public int intValue() {
        return _value;
    }

    /** Return the value in the token as a long.
     *  @return The int  value contained in this token as a long.
     */
    public long longValue() {
        return (long)_value;
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *  not of a type that can be  used with modulo, or the units of
     *   this token and the argument token are not the same.
     */
    public Token modulo(Token rightArgument) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArgument);
        if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
            IntToken intToken;
            if (typeInfo == CPO.HIGHER) {
               intToken = (IntToken)IntToken.convert(rightArgument);
            } else {
               intToken = (IntToken)rightArgument;
            }
            int remainder = _value % intToken.intValue();
            IntToken result = new IntToken(remainder);
            if ( !_areUnitsEqual(intToken)) {
                throw new IllegalActionException("IntToken.modulo: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + intToken.unitsString());
            }
            result._unitCategoryExponents = this._copyOfCategoryExponents();
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.moduloReverse(this);
        } else {
            throw new IllegalActionException("IntToken.modulo: "
                    + "Cannot compute the modulo of "
                    + this.getClass().getName() + " " + this.toString()
                    + " and "
                    + rightArgument.getClass().getName() + " "
                    + rightArgument.toString());
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  modulo the value of this token.
     *  Type resolution also occurs here, with the returned token
     *  type chosen to achieve a lossless conversion.
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
        IntToken intToken =
                (IntToken)IntToken.convert(leftArgument);

        int remainder = intToken.intValue() % _value;
        IntToken result = new IntToken(remainder);
        if ( !_areUnitsEqual(intToken)) {
            throw new IllegalActionException("IntToken.moduloReverse: "
                    + "The units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + intToken.unitsString());
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
            IntToken intToken;
            if (typeInfo == CPO.HIGHER) {
               intToken = (IntToken)IntToken.convert(rightFactor);
            } else {
               intToken = (IntToken)rightFactor;
            }
            int product = _value * intToken.intValue();
            IntToken result = new IntToken(product);
            // compute units
            result._unitCategoryExponents = _addCategoryExponents(intToken);
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightFactor.multiplyReverse(this);
        } else {
            throw new IllegalActionException(
                    "IntToken.multiply: Cannot multiply "
                    + this.getClass().getName() + " " + this.toString()
                    + " with "
                    + rightFactor.getClass().getName() + " "
                    + rightFactor.toString());
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

    /** Returns a new IntToken with value 1.
     *  @return A new IntToken with value 1.
     */
    public Token one() {
        return new IntToken(1);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.
     *  Type resolution also occurs here, with the returned token type
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
            IntToken intToken;
            if (typeInfo == CPO.HIGHER) {
               intToken = (IntToken)IntToken.convert(rightArgument);
            } else {
               intToken = (IntToken)rightArgument;
            }
            int difference = _value - intToken.intValue();
            IntToken result = new IntToken(difference);
            if ( !_areUnitsEqual(intToken)) {
                throw new IllegalActionException("IntToken.subtract: "
                        + "The units of this token: " + unitsString()
                        + " are not the same as those of the argument: "
                        + intToken.unitsString());
            }
            result._unitCategoryExponents = this._copyOfCategoryExponents();
            return result;
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.subtractReverse(this);
        } else {
            throw new IllegalActionException("IntToken.subtract: "
                    + "Cannot subtract "
                    + this.getClass().getName() + " " + this.toString()
                    + " by "
                    + rightArgument.getClass().getName() + " "
                    + rightArgument.toString());
        }
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param leftArgument The token to add this token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be added to this token, or the units
     *   of this token and the argument token are not the same.
     */
    public Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        IntToken intToken = (IntToken)IntToken.convert(leftArgument);
        int difference = intToken.intValue() - _value;
        IntToken result = new IntToken(difference);
        if ( !_areUnitsEqual(intToken)) {
            throw new IllegalActionException("IntToken.subtractReverse: "
                    + "The units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + intToken.unitsString());
        }
        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  If this token has a unit, the return string also includes a unit
     *  string produced by the unitsString() method in the super class.
     *  @return A String representing the int value and the units (if
     *   any) of this token.
     *  @see ptolemy.data.ScalarToken#unitsString
     */
    public String toString() {
	String unitString = "";
	if ( !_isUnitless()) {
	    unitString = " * " + unitsString();
	}
        return Integer.toString(_value) + unitString;
    }

    /** Returns a new IntToken with value 0.
     *  @return A new IntToken with value 0.
     */
    public Token zero() {
        return new IntToken(0);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _value;
}
