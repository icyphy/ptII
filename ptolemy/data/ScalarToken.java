/* Abstract base class for tokens that contain a scalar.

 Copyright (c) 1997-2003 The Regents of the University of California.
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
@AcceptedRating Green (yuhong@eecs.berkeley.edu)
FIXME: setUnitCategory seems to violate immutability.
*/

package ptolemy.data;

import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.unit.UnitUtilities;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;

import java.util.Arrays;

//////////////////////////////////////////////////////////////////////////
//// ScalarToken
/**
Abstract base class for tokens that contain a scalar.  This base class
extends the Token class to properly implement type conversion and the
units portion of the standard operations for scalar tokens.  It also
adds methods for querying the natural ordering between scalars.

<p> This class has a number of protected abstract methods that subclasses
must implement.  These methods need only implement the numerical
portion of the operation between two tokens of the same type.  This
base class will handle the conversion of tokens from different types
to the same type before calling the protected method, and the proper
computation of the units of the returned token afterwards.

<p> In general, any instance of a scalar token may be optionally
associated with a set of units.  In the arithmetic methods add(),
modulo(), and subtract(), the two operands must have the same
units. Otherwise, an exception will be thrown. In the methods
multiply() and divide(), the units of the resulting token will be
computed automatically.  IMPORTANT: The protected methods implemented
in derived classes are expected to return a new token in the case of
multiply and divide.  This new token will automatically have its units
set correctly by this base class implementation.  Certain cases, such
as multiplication by one, cannot be optimized to simply return an the
input token without performing the multiplication, since the units of
the result may be different than the units of either input token.

@author Yuhong Xiong, Mudit Goel, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public abstract class ScalarToken extends Token
    implements BitwiseOperationToken {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is
     *  returned.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.  This method defers to
     *  the _absolute() method to perform the operation, and derived
     *  classes should implement that method to provide type-specific
     *  behavior.
     *  @return A ScalarToken with the same units, and likely to be of
     *  the same type as this token.
     */
    public final ScalarToken absolute() {
        ScalarToken result = _absolute();
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the sum of this token and
     *  the argument. Type conversion also occurs here, so that the
     *  operation is performed at the least type necessary to ensure
     *  precision.  The returned type is the same as the type chosen
     *  for the operation, which is the higher of the type of this
     *  token and the argument type.  Subclasses should implement the
     *  protected _add() method to perform the correct type-specific
     *  operation.
     *  @param rightArgument The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public final Token add(Token rightArgument) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doAdd(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doAdd(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("add", this, rightArgument));
            }
        } else if ((typeInfo == CPO.LOWER)
                || (rightArgument instanceof MatrixToken)) {
            // NOTE: If the right argument is an instance of MatrixToken,
            // then we try reversing the add. This is because the
            // code below for incomparable types won't work because
            // automatic conversion from double to [double] is not
            // supported.  Perhaps it should be?
            return rightArgument.addReverse(this);
        } else {
            // Items being multiplied are incomparable.
            // However, addition may still be possible because
            // the LUB of the types might support it. E.g., [double]+complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), rightArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.add(rightArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("add",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the sum of this token and
     *  the argument.  Type conversion also occurs here, so that the
     *  operation is performed at the least type necessary to ensure
     *  precision.  The returned type is the same as the type chosen
     *  for the operation, which is the higher of the type of this
     *  token and the argument type.  Subclasses should implement the
     *  protected _add() method to perform the correct type-specific
     *  operation.
     *  @param leftArgument The token to add this token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public final Token addReverse(ptolemy.data.Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());
        // We would normally expect this to be LOWER, since this will almost
        // always be called by add, so put that case first.
        if (typeInfo == CPO.LOWER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(leftArgument);
            try {
                return convertedArgument._doAdd(this);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("addReverse",
                                this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            return ((ScalarToken)leftArgument)._doAdd(this);
        } else if (typeInfo == CPO.HIGHER) {
            return leftArgument.add(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("addReverse",
                            this, leftArgument));
        }
    }

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.
     *  @return The bitwise AND.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public BitwiseOperationToken bitwiseAnd(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doBitwiseAnd(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doBitwiseAnd(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("bitwiseAnd",
                                this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            if (!(rightArgument instanceof BitwiseOperationToken)) {
                throw new IllegalActionException(
                        notSupportedMessage("bitwiseAnd",
                                this, rightArgument));
            } else {
                // This code uses the fact that bitwise AND is always
                // commutative, there is no need to add a bitwiseAndReverse
                // method.
                return ((BitwiseOperationToken)rightArgument).bitwiseAnd(this);
            }
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("bitwiseAnd",
                            this, rightArgument));
        }
    }

    /** Returns a token representing the bitwise NOT of this token.
     *  @return The bitwise NOT of this token.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public BitwiseOperationToken bitwiseNot()
            throws IllegalActionException {
        ScalarToken result = _bitwiseNot();
        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
    }

    /** Returns a token representing the bitwise OR of this token and
     *  the given token.
     *  @return The bitwise OR.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public BitwiseOperationToken bitwiseOr(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doBitwiseOr(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doBitwiseOr(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("bitwiseOr",
                                this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            if (!(rightArgument instanceof BitwiseOperationToken)) {
                throw new IllegalActionException(
                        notSupportedMessage("bitwiseOr",
                                this, rightArgument));
            } else {
                // This code uses the fact that bitwise OR is always
                // commutative, there is no need to add a bitwiseOrReverse
                // method.
                return ((BitwiseOperationToken)rightArgument).bitwiseOr(this);
            }
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("bitwiseOr",
                            this, rightArgument));
        }
    }

    /** Returns a token representing the bitwise XOR of this token and
     *  the given token.
     *  @return The bitwise XOR.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public BitwiseOperationToken bitwiseXor(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doBitwiseXor(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doBitwiseXor(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("bitwiseXor",
                                this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            if (!(rightArgument instanceof BitwiseOperationToken)) {
                throw new IllegalActionException(
                        notSupportedMessage("bitwiseXor",
                                this, rightArgument));
            } else {
                // This code uses the fact that bitwise XOR is always
                // commutative, there is no need to add a bitwiseXorReverse
                // method.
                return ((BitwiseOperationToken)rightArgument).bitwiseXor(this);
            }
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("bitwiseXor",
                            this, rightArgument));
        }
    }

    /** Return the value in the token as a byte.
     *  In this base class, we just throw an exception.
     *  @return The byte value contained in this token.
     *  @exception IllegalActionException Always thrown.
     */
    public byte byteValue() throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "byte"));
    }

    /** Return the value of this token as a Complex.
     *  In this base class, we just throw an exception.
     *  @return A Complex
     *  @exception IllegalActionException Always thrown.
     */
    public Complex complexValue() throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "Complex"));
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation, which is the
     *  higher of the type of this token and the argument type.  The
     *  returned token will also have the correct units.  Subclasses
     *  should implement the protected _divide() method to perform the
     *  correct type-specific operation.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public final Token divide(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doDivide(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);

            try {
                return _doDivide(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("divide", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.divideReverse(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("divide",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided into the value of the argument token.  Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation, which is the
     *  higher of the type of this token and the argument type.  The
     *  returned token will also have the correct units.  Subclasses
     *  should implement the protected _divide() method to perform the
     *  correct type-specific operation.
     *  @param leftArgument The token to be divided into the value of
     *  this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public final Token divideReverse(Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());
        // We would normally expect this to be LOWER, since this will almost
        // always be called by divide, so put that case first.
        if (typeInfo == CPO.LOWER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(leftArgument);
            try {
                return convertedArgument._doDivide(this);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("divideReverse",
                                this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            return ((ScalarToken)leftArgument)._doDivide(this);
        } else if (typeInfo == CPO.HIGHER) {
            return leftArgument.divide(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("divideReverse",
                            this, leftArgument));
        }
    }

    /** Return the value of this token as a double.
     *  In this base class, we just throw an exception.
     *  @return A double
     *  @exception IllegalActionException Always thrown
     */
    public double doubleValue() throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "double"));
    }

    /** Return the value of this token as a FixPoint.
     *  In this base class, we just throw an exception.
     *  @return A FixPoint
     *  @exception IllegalActionException Always thrown.
     */
    public FixPoint fixValue() throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "fixedpoint"));
    }

    /** Return the type of this token.  Subclasses must implement this method
     *  to return the correct type.
     *  @return BaseType.SCALAR
     */
    public abstract Type getType();

    /** Return a scalar token that contains the value of this token in the
     *  units of the argument token. The unit category of the argument token
     *  must be the same as that of this token, otherwise, an exception will
     *  be thrown. The returned token is unitless.
     *  @param units A scalar token that represents a unit.
     *  @return A scalar token that does not have a unit.
     *  @exception IllegalActionException If the unit category of the
     *  argument token is not the same as that of this one.
     */
    public ScalarToken inUnitsOf(ScalarToken units)
            throws IllegalActionException {
        if ( !_areUnitsEqual(units)) {
            throw new IllegalActionException(
                    notSupportedMessage("inUnitsOf", this, units) +
                    " because the units are not the same.");
        }
        return (ScalarToken)this.divide(units);
    }

    /** Return the value of this token as an int.
     *  In this base class, we just throw an exception.
     *  @return The value of this token as an int.
     *  @exception IllegalActionException Always thrown.
     */
    public int intValue() throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "int"));
    }

    /** Test whether the value of this Token is close to the argument
     *  Token.  The argument and this token are converted to
     *  equivalent types, and then compared.  Generally, this is the
     *  higher of the type of this token and the argument type.
     *  Subclasses should implement the protected _isCloseTo() method
     *  to perform the correct type-specific operation.
     *  @see #isEqualTo
     *  @param rightArgument The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @return A boolean token that contains the value true if the
     *   units of this token and the argument token are the same, and their
     *   values are close.
     *  @exception IllegalActionException If the argument token is not
     *   of a type that can be compared with this token, or the units
     *   are not the same.
     */
    public final BooleanToken isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        // Note that if we had absolute(), subtraction() and islessThan()
        // we could perhaps define this method for all tokens.  However,
        // Precise classes like IntToken not bother doing the absolute(),
        // subtraction(), and isLessThan() method calls and should go
        // straight to isEqualTo().  Also, these methods might introduce
        // exceptions because of type conversion issues.
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doIsCloseTo(rightArgument, epsilon);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doIsCloseTo(convertedArgument, epsilon);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("isCloseTo", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.isCloseTo(this, epsilon);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("isCloseTo",
                            this, rightArgument));
        }
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  The argument and this token are converted to
     *  equivalent types, and then compared.  Generally, this is the
     *  higher of the type of this token and the argument type.  This
     *  method defers to the _isEqualTo() method to perform a
     *  type-specific equality check.  Derived classes should implement
     *  that method to provide type specific actions for equality
     *  testing.
     *
     *  @see #isCloseTo
     *  @param rightArgument The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A boolean token that contains the value true if the
     *  values and units of this token and the argument token are the same.
     */
    public final BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doIsEqualTo(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doIsEqualTo(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("isEqualTo", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.isEqualTo(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("isEqualTo",
                            this, rightArgument));
        }
    }

    /** Check whether the value of this token is strictly greater than
     *  that of the argument token.  The argument and this token are
     *  converted to equivalent types, and then compared.  Generally,
     *  this is the higher of the type of this token and the argument
     *  type.  This method defers to the _isLessThan() method to perform
     *  a type-specific equality check.  Derived classes should
     *  implement that method to provide type specific actions for
     *  equality testing.
     *
     *  @param rightArgument The token to compare against.
     *  @return A boolean token with value true if this token has the
     *  same units as the argument, and is strictly greater than the
     *  argument.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public final BooleanToken isGreaterThan(ScalarToken rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return rightArgument._doIsLessThan(this);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return convertedArgument._doIsLessThan(this);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("isGreaterThan", this,
                                rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.isLessThan(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("isGreaterThan",
                            this, rightArgument));
        }
    }

    /** Check whether the value of this token is strictly less than that of the
     *  argument token.  The argument and this token are converted to
     *  equivalent types, and then compared.  Generally, this is the
     *  higher of the type of this token and the argument type.  This
     *  method defers to the _isLessThan() method to perform a
     *  type-specific equality check.  Derived classes should implement
     *  that method to provide type specific actions for equality
     *  testing.
     *
     *  @param rightArgument The token to compare against.
     *  @return A boolean token with value true if this token has the
     *  same units as the argument, and is strictly less than the
     *  argument.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public final BooleanToken isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doIsLessThan(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doIsLessThan(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("isLessThan", this,
                                rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.isGreaterThan(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("isLessThan",
                            this, rightArgument));
        }
    }

    /** Returns a token representing the result of shifting the bits
     *  of this token towards the most significant bit, filling the
     *  least significant bits with zeros.
     *  @param bits The number of bits to shift.
     *  @return The left shift.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public ScalarToken leftShift(int bits)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("leftShift", this, this));
    }

    /** Returns a token representing the result of shifting the bits
     *  of this token towards the least significant bit, filling the
     *  most significant bits with zeros.  This treats the value as an
     *  unsigned number, which may have the effect of destroying the
     *  sign of the value.
     *  @param bits The number of bits to shift.
     *  @return The right shift.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public ScalarToken logicalRightShift(int bits)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("logicalRightShift", this, this));
    }

    /** Return the value of this token as a long integer.
     *  In this base class, we just throw an exception.
     *  @return The value of this token as a long.
     *  @exception IllegalActionException Always thrown.
     */
    public long longValue() throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "long"));
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  Type conversion also
     *  occurs here, so that the operation is performed at the least
     *  type necessary to ensure precision.  The returned type is the
     *  same as the type chosen for the operation, which is the higher
     *  of the type of this token and the argument type.  Subclasses
     *  should implement the protected _modulo() method to perform the
     *  correct type-specific operation.
     *  @param rightArgument The token to modulo with this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public final Token modulo(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doModulo(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doModulo(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("modulo", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.moduloReverse(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("modulo",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  Type conversion also
     *  occurs here, so that the operation is performed at the least
     *  type necessary to ensure precision.  The returned type is the
     *  same as the type chosen for the operation, which is the higher
     *  of the type of this token and the argument type.  Subclasses
     *  should implement the protected _modulo() method to perform the
     *  correct type-specific operation.
     *  @param leftArgument The token to apply modulo to by the value
     *  of this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public final Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());
        // We would normally expect this to be LOWER, since this will almost
        // always be called by modulo, so put that case first.
        if (typeInfo == CPO.LOWER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(leftArgument);
            try {
                return  convertedArgument._doModulo(this);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("moduloReverse",
                                this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            return ((ScalarToken)leftArgument)._doModulo(this);
        } else if (typeInfo == CPO.HIGHER) {
            return leftArgument.modulo(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("moduloReverse",
                            this, leftArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  Type
     *  conversion also occurs here, so that the operation is
     *  performed at the least type necessary to ensure precision.
     *  The returned type is the same as the type chosen for the
     *  operation, which is the higher of the type of this token and
     *  the argument type.  Subclasses should implement the protected
     *  _multiply() method to perform the correct type-specific
     *  operation.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public final Token multiply(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doMultiply(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doMultiply(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("multiply", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER
                || rightArgument instanceof MatrixToken) {
            // NOTE: If the right argument is an instance of MatrixToken,
            // then we try reversing the multiply.  This is because the
            // code below for incomparable types won't work because
            // automatic conversion from double to [double] is not
            // supported.  Perhaps it should be?
            return rightArgument.multiplyReverse(this);
        } else {
            // Items being multiplied are incomparable.
            // However, multiplication may still be possible because
            // the LUB of the types might support it. E.g., [double]*complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), rightArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.multiply(rightArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("multiply",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  Type
     *  conversion also occurs here, so that the operation is
     *  performed at the least type necessary to ensure precision.
     *  The returned type is the same as the type chosen for the
     *  operation, which is the higher of the type of this token and
     *  the argument type.  Subclasses should implement the protected
     *  _multiply() method to perform the correct type-specific
     *  operation.
     *  @param leftArgument The token to be multiplied by the value of
     *  this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public final Token multiplyReverse(Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());
        // We would normally expect this to be LOWER, since this will almost
        // always be called by multiply, so put that case first.
        if (typeInfo == CPO.LOWER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(leftArgument);
            try {
                return convertedArgument._doMultiply(this);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("multiplyReverse",
                                this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            return ((ScalarToken)leftArgument)._doMultiply(this);
        } else if (typeInfo == CPO.HIGHER
                || leftArgument instanceof MatrixToken) {
            // NOTE: If the left argument is an instance of MatrixToken,
            // then we try reversing the multiply.  This is because the
            // code below for incomparable types won't work because
            // automatic conversion from double to [double] is not
            // supported.  Perhaps it should be?
            return leftArgument.multiply(this);
        } else {
            // Items being multiplied are incomparable.
            // However, multiplication may still be possible because
            // the LUB of the types might support it. E.g., [double]*complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), leftArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.multiplyReverse(leftArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("multiplyReverse",
                            leftArgument, this));
        }
    }

    /** Returns a token representing the result of shifting the bits
     *  of this token towards the least significant bit, filling the
     *  most significant bits with the sign of the value.  This preserves
     *  the sign of the result.
     *  @param bits The number of bits to shift.
     *  @return The right shift.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public ScalarToken rightShift(int bits)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("rightShift", this, this));
    }

    /** Set the unit category this token belongs to.  This method is
     *  called from within the units system to create tokens
     *  representing base units.  This method should not be called by
     *  user code.
     *  @param index The unit category index.
     *  @deprecated We need a better way of manufacturing the tokens
     *  for base units, since this method violates the immutability of
     *  tokens.
     */
    // FIXME: shouldn't this be protected???  it violates the immutability of
    // tokens.
    public void setUnitCategory(int index) {
        _unitCategoryExponents = UnitUtilities.newUnitArrayInCategory(index);
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  Type
     *  conversion also occurs here, so that the operation is
     *  performed at the least type necessary to ensure precision.
     *  The returned type is the same as the type chosen for the
     *  operation, which is the higher of the type of this token and
     *  the argument type.  Subclasses should implement the protected
     *  _subtract() method to perform the correct type-specific
     *  operation.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public final Token subtract(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doSubtract(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                return _doSubtract(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("subtract", this, rightArgument));
            }
        } else if ((typeInfo == CPO.LOWER)
                || (rightArgument instanceof MatrixToken)) {
            // NOTE: If the right argument is an instance of MatrixToken,
            // then we try reversing the subtract. This is because the
            // code below for incomparable types won't work because
            // automatic conversion from double to [double] is not
            // supported.  Perhaps it should be?
            return rightArgument.subtractReverse(this);
        } else {
            // Items being subtracted are incomparable.
            // However, addition may still be possible because
            // the LUB of the types might support it. E.g., [double]-complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), rightArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.subtract(rightArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("subtract",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  Type
     *  conversion also occurs here, so that the operation is
     *  performed at the least type necessary to ensure precision.
     *  The returned type is the same as the type chosen for the
     *  operation, which is the higher of the type of this token and
     *  the argument type.  Subclasses should implement the protected
     *  _subtract() method to perform the correct type-specific
     *  operation.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public final Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());
        // We would normally expect this to be LOWER, since this will almost
        // always be called by subtract, so put that case first.
        if (typeInfo == CPO.LOWER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(leftArgument);
            try {
                return convertedArgument._doSubtract(this);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("subtractReverse",
                                this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            return ((ScalarToken)leftArgument)._doSubtract(this);
        } else if (typeInfo == CPO.HIGHER) {
            return leftArgument.subtract(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("subtractReverse",
                            this, leftArgument));
        }
    }

    /** Return the string representation of the units of this token.
     *  The general format of the returned string is
     *  "(l_1 * l_2 * ... * l_m) / (s_1 * s_2 * ... * s_n)".
     *  For example: "(meter * kilogram) / (second * second)".
     *  If m or n is 1, then the parenthesis above or below "/" is
     *  omited. For example: "meter / second".
     *  If there is no term above "/", the format becomes
     *  "1 / (s_1 * s_2 * ... * s_n)". For example: "1 / meter".
     *  If this token does not have a unit, return an empty string.
     *  @return A string representation of the units of this token.
     */
    public String unitsString() {
        return UnitUtilities.unitsString(_unitCategoryExponents);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is
     *  returned.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.  Derived classes must implement
     *  this method in a type-specific fashion.
     *  @return A ScalarToken, which is likely, but not required to be
     *  the same type as this token.
     */
    protected abstract ScalarToken _absolute();

    /** Return a new token whose value is the value of the argument
     *  token added to the value of this token.  It is guaranteed by
     *  the caller that the type of the argument is the same as the
     *  type of this class.  This method should be overridden in
     *  derived classes to provide type-specific operation and return
     *  a token of the appropriate subclass.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new token containing the result.
     */
    protected abstract ScalarToken _add(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Add the corresponding unit category exponents.
     *  @param token A token whose exponent will be added with the
     *   exponents of this token.
     *  @return An int array containing the addition result, or null
     *   if the result is unitless.
     */
    protected int[] _addCategoryExponents(ScalarToken token) {
        return UnitUtilities.addUnitsArray(
                _unitCategoryExponents, token._unitCategoryExponents);
    }

    /** Return true if the units of this token are the same as that of the
     *  argument token. If both tokens do not have units, return true.
     *  @param scalarToken A scalar token.
     *  @return True if the units of this token is the same as that of the
     *   argument token; false otherwise.
     */
    protected boolean _areUnitsEqual(ScalarToken scalarToken) {
        return UnitUtilities.areUnitArraysEqual(
                _unitCategoryExponents, scalarToken._unitCategoryExponents);
    }

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.
     *  @return The bitwise AND.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    protected abstract ScalarToken _bitwiseAnd(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Returns a token representing the bitwise NOT of this token.
     *  @return The bitwise NOT of this token.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    protected abstract ScalarToken _bitwiseNot()
            throws IllegalActionException;

    /** Returns a token representing the bitwise OR of this token and
     *  the given token.
     *  @return The bitwise OR.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    protected abstract ScalarToken _bitwiseOr(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Returns a token representing the bitwise XOR of this token and
     *  the given token.
     *  @return The bitwise XOR.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    protected abstract ScalarToken _bitwiseXor(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Return a copy of the unit category exponents array. If this
     *  token does not have a unit, return null;
     *  @return An int array that is a copy of the unit category
     *  exponents of this token.
     */
    protected int[] _copyOfCategoryExponents() {
        return UnitUtilities.copyUnitsArray(_unitCategoryExponents);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  It is guaranteed
     *  by the caller that the type of the argument is the same as the
     *  type of this class.  This method should be overridden in
     *  derived classes to provide type-specific operation and return
     *  a token of the appropriate subclass.
     *  @param rightArgument The token to divide this token by.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract ScalarToken _divide(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Test whether the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @return A token containing true if the value of the first
     *   argument is close to the value of this token.
     */
    protected abstract BooleanToken _isCloseTo(
            ScalarToken rightArgument, double epsilon)
            throws IllegalActionException;

    /** Test for equality of the values of this token and the argument.
     *  This base class delegates to the equals() method.
     *  @param token The token to compare to this token.
     *  @return A token containing true if the value element of the first
     *   argument is equal to the value of this token.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected BooleanToken _isEqualTo(ScalarToken token)
            throws IllegalActionException {
        return BooleanToken.getInstance(equals(token));
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is guaranteed by the caller that the type and
     *  units of the argument is the same as the type of this class.
     *  This method should be overridden in derived classes to provide
     *  type-specific operation and return a token of the appropriate
     *  subclass.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Return true if this token does not have a unit.
     *  @return True if this token does not have a unit.
     */
    protected boolean _isUnitless() {
        return UnitUtilities.isUnitless(_unitCategoryExponents);
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is guaranteed by
     *  the caller that the type of the argument is the same as the
     *  type of this class.  This method should be overridden in
     *  derived classes to provide type-specific operation and return
     *  a token of the appropriate subclass.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract ScalarToken _modulo(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class.  This method should be
     *  overridden in derived classes to provide type-specific
     *  operation and return a token of the appropriate subclass.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract ScalarToken _multiply(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class.  This method should be
     *  overridden in derived classes to provide type-specific
     *  operation and return a token of the appropriate subclass.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract ScalarToken _subtract(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Subtract the corresponding unit category exponents of the
     *  argument token from that of this token.
     *  @param token A token whose exponent will be subtracted from
     *  the exponents of this token.
     *  @return An array of int containing the result, or null if the
     *  result is unitless.
     */
    protected int[] _subtractCategoryExponents(ScalarToken token) {
        return UnitUtilities.subtractUnitsArray(
                _unitCategoryExponents, token._unitCategoryExponents);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The unit category exponents.
     *  The unit system contains a set of base unit categories and derived
     *  categories. The base categories are customizable by the user.
     *  For example, the user may choose to use the SI unit system which
     *  has 7 base categories: length, mass, time, electric current,
     *  thermodynamic temperature, amount of substance, and luminous
     *  intensity. The customization is done by defining a MoML file to specify
     *  the categories and the units in each category. Each category has an
     *  index, assigned by the order the category appears in the MoML file.
     *  Derived units are recorded by the exponents of the category. For
     *  example, the category speed, which is length/time, is stored by an
     *  exponent of 1 for the length category, and an exponent of -1 for the
     *  time category.
     *  This array records the exponents of the base categories.
     */
    protected int[] _unitCategoryExponents = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is guaranteed by
     *  the caller that the type of the argument is the same as the
     *  type of this class and has the same units as this token.  The
     *  resulting token will also have the same type and units.  This
     *  method defers to the _add() method that takes a ScalarToken.
     *  Derived classes should implement that method instead to
     *  provide type-specific operation.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new Token containing the result.
     */
    private Token _doAdd(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("add", this, rightArgument)
                    + " because the units are not the same.");
        }
        ScalarToken result = _add(convertedArgument);
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }

    /*  Return a new token whose value is the value of the argument
     *  Token bitwise ANDed to the value of this Token.  It is guaranteed
     *  by the caller that the type of the argument is the same as the
     *  type of this class.  The resulting token will also have the same
     *  type. This method checks that the two tokens have the same units
     *  and throws an exception if the units are not the same. The resulting
     *  token will have the same units as the operands. This method defers
     *  to the _bitwiseAnd() method that takes a ScalarToken.
     *  @param rightArgument The token to bitwise AND to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new Token containing the result.
     */
    private BitwiseOperationToken _doBitwiseAnd(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("bitwiseAnd", this, rightArgument)
                    + " because the units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + convertedArgument.unitsString());
        }
        ScalarToken result = _bitwiseAnd(convertedArgument);
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }

    /*  Return a new token whose value is the value of the argument
     *  Token bitwise ORed to the value of this Token.  It is guaranteed
     *  by the caller that the type of the argument is the same as the
     *  type of this class.  The resulting token will also have the same
     *  type. This method checks that the two tokens have the same units
     *  and throws an exception if the units are not the same. The resulting
     *  token will have the same units as the operands. This method defers
     *  to the _bitwiseOr() method that takes a ScalarToken.
     *  @param rightArgument The token to bitwise OR to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new Token containing the result.
     */
    private BitwiseOperationToken _doBitwiseOr(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("bitwiseOr", this, rightArgument)
                    + " because the units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + convertedArgument.unitsString());
        }
        ScalarToken result = _bitwiseOr(convertedArgument);
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }

    /*  Return a new token whose value is the value of the argument
     *  Token bitwise XORed to the value of this Token.  It is guaranteed
     *  by the caller that the type of the argument is the same as the
     *  type of this class.  The resulting token will also have the same
     *  type. This method checks that the two tokens have the same units
     *  and throws an exception if the units are not the same. The resulting
     *  token will have the same units as the operands. This method defers
     *  to the _bitwiseXOR() method that takes a ScalarToken.
     *  @param rightArgument The token to bitwise XOR to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new Token containing the result.
     */
    private BitwiseOperationToken _doBitwiseXor(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("bitwiseXor", this, rightArgument)
                    + " because the units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + convertedArgument.unitsString());
        }
        ScalarToken result = _bitwiseXor(convertedArgument);
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  It is guaranteed
     *  by the caller that the type of the argument is the same as the
     *  type of this class.  The resulting token will also have the
     *  same type and appropriate units.  This method defers to the
     *  _divide method that takes a ScalarToken.  Derived classes
     *  should implement that method instead to provide type-specific
     *  operation.
     *  @param rightArgument The token to divide this token by.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     */
    private Token _doDivide(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        ScalarToken result = _divide(convertedArgument);
        // compute units
        result._unitCategoryExponents =
            _subtractCategoryExponents(convertedArgument);
        return result;
    }

    /** Test for closeness of the values of this Token and the
     *  argument Token.  It is guaranteed by the caller that the type
     *  and units of the argument is the same as the type of this
     *  class. This method may defer to the _isCloseTo() method that
     *  takes a ScalarToken.  Derived classes should implement that
     *  method instead to provide type-specific operation.
     *  @param rightArgument The token with which to test closeness.
     *  @exception IllegalActionException If the units of the argument
     *  are not the same as the units of this token, or the method is
     *  not supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    private BooleanToken _doIsCloseTo(
            Token rightArgument, double epsilon)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("isCloseTo", this, rightArgument)
                    + " because the units are not the same.");
        }

        return _isCloseTo(convertedArgument, epsilon);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is guaranteed by the caller that the type of the
     *  argument is the same as the type of this class.  This method
     *  returns BooleanToken.FALSE if the units of this token and the
     *  given token are not identical.  This method may defer to the
     *  _isEqualTo() method that takes a ScalarToken.  Derived classes
     *  should implement that method instead to provide type-specific
     *  operation.
     *  @param rightArgument The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    private BooleanToken _doIsEqualTo(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            return BooleanToken.FALSE;
        }

        return _isEqualTo(convertedArgument);
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is guaranteed by the caller that the type and
     *  units of the argument is the same as the type of this class.
     *  This method may defer to the _isLessThan() method that takes a
     *  ScalarToken.  Derived classes should implement that method
     *  instead to provide type-specific operation.
     *  @param rightArgument The token with which to test ordering.
     *  @exception IllegalActionException If the units of the argument
     *  are not the same as the units of this token, or the method is
     *  not supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    private BooleanToken _doIsLessThan(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("isLessThan", this, rightArgument)
                    + " because the units are not the same.");
        }

        return _isLessThan(convertedArgument);
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is guaranteed by
     *  the caller that the type of the argument is the same as the
     *  type of this class and has the same units as this token.  The
     *  resulting token will also have the same type and units.  This
     *  method defers to the _modulo() method that takes a
     *  ScalarToken.  Derived classes should implement that method
     *  instead to provide type-specific operation.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new Token containing the result.
     */
    private Token _doModulo(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("modulo", this, rightArgument)
                    + " because the units are not the same.");
        }
        ScalarToken result = _modulo(convertedArgument);
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class.  The resulting token will also
     *  have the same type and appropriate units.  This method defers
     *  to the _multiply() method that takes a ScalarToken.  Derived
     *  classes should implement that method instead to provide
     *  type-specific operation.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    private Token _doMultiply(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        ScalarToken result = _multiply(convertedArgument);
        // compute units
        result._unitCategoryExponents =
            _addCategoryExponents(convertedArgument);
        return result;
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class and has the same units as this
     *  token.  The resulting token will also have the same type and
     *  units.  This method defers to the _subtract method that takes
     *  a ScalarToken.  Derived classes should implement that method
     *  instead to provide type-specific operation.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new Token containing the result.
     */
    private Token _doSubtract(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if ( !_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("subtract", this, rightArgument)
                    + " because the units are not the same.");
        }
        ScalarToken result = _subtract(convertedArgument);
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }
}
