/* Abstract base class for tokens that contain a scalar.

 Copyright (c) 1997-2002 The Regents of the University of California.
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
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)
setUnitCategory seems to violate immutability.
_unitString should use StringBuffer.
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;
import ptolemy.data.type.Type;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.unit.UnitSystem;
import ptolemy.graph.CPO;

import java.util.Arrays;

//////////////////////////////////////////////////////////////////////////
//// ScalarToken
/**
Abstract base class for tokens that contain a scalar.  This class
defines methods for type conversion among different scalar tokens. The
implementation in this base class just throws an exception.  Derived
class should override the methods that the corresponding conversion
can be achieved without loss of information.

<p> Instances of ScalarToken may also have units. In the arithmetic
methods add(), modulo(), and subtract(), the two operands must have the same
units. Otherwise, an exception will be thrown. In the methods
multiply() and divide(), the units of the resulting token will be
computed automatically.  The methods in this class properly compute the
units and derived classes to not need ot compute them separately.  However,
it is important in some cases that derived classes actually return a new
token, since the units may be different.  For instance, multiplication by
one cannot generally be optimized to simply return the input token without
performing the multiplication, since the one may actually change the units
of the token.

<p> The operation methods from the Token base class are implemented in
this class to perform the proper units conversion for all scalar
types.  This class in turn defines a new set of protected methods that
scalar tokens should override to inherit the proper type conversion
and unit conversion operations.

@author Yuhong Xiong, Mudit Goel, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public abstract class ScalarToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is
     *  returned.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.  This method defers to
     *  the _absolute() method to perform the operation, and derived
     *  classes should override that method to provide type-specific
     *  behavior
     *  @return A ScalarToken of the same units, and likely to be of
     *  the same type as this token.
     */
    public final ScalarToken absolute() {
        ScalarToken result = _absolute();
        result._unitCategoryExponents = this._copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the sum of this token and
     *  the argument. Type conversion also occurs here, so that the
     *  operation is performed at the least type necessary to ensure
     *  precision.  The returned type is the same as the type chosen
     *  for the operation, which is the higher of the type of this
     *  token and the argument type.  Subclasses should override the
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
            Token result = _doAdd(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
             try {
                Token result = _doAdd(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
               throw new IllegalActionException(null, ex,
                        notSupportedMessage("add", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.addReverse(this);
            return result;
        } else {
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
     *  token and the argument type.  Subclasses should override the
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
        // always be called by subtract, so put that case first.
        if (typeInfo == CPO.LOWER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(leftArgument);
            try {
                Token result = convertedArgument._doAdd(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                    notSupportedMessage("addReverse",
                            this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((ScalarToken)leftArgument)._doAdd(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.add(this);
            return result;
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("addReverse",
                            this, leftArgument));
        }
    }

    /** Return the value of this token as a Complex.
     *  In this base class, we just throw an exception.
     *  @return A Complex
     *  @exception IllegalActionException Always thrown
     */
    public Complex complexValue() throws IllegalActionException {
        throw new IllegalActionException("Cannot convert the value in " +
                getClass().getName() + " to a Complex losslessly.");
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation, which is the
     *  higher of the type of this token and the argument type.
     *  Subclasses should override the protected _divide() method to
     *  perform the correct type-specific operation.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public final Token divide(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if(typeInfo == CPO.SAME) {
            Token result = _doDivide(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);

            try {
                Token result = _doDivide(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("divide", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.divideReverse(this);
            return result;
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("divide",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation, which is the
     *  higher of the type of this token and the argument type.
     *  Subclasses should override the protected _divide() method to
     *  perform the correct type-specific operation.
     *  @param leftArgument The token to be divided by the value of
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
        // always be called by subtract, so put that case first.
        if (typeInfo == CPO.LOWER) {
           ScalarToken convertedArgument = (ScalarToken)
                getType().convert(leftArgument);
            try {
                Token result = convertedArgument._doDivide(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                    notSupportedMessage("divideReverse",
                            this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((ScalarToken)leftArgument)._doDivide(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.divide(this);
            return result;
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
        throw new IllegalActionException("Cannot convert the value in " +
                getClass().getName() + " to a double losslessly.");
    }

    /** Return the type of this token.
     *  @return BaseType.SCALAR
     */
    public Type getType() {
        return BaseType.SCALAR;
    }

    /** Return the value of this token as a FixPoint.
     *  In this base class, we just throw an exception.
     *  @return A FixPoint
     *  @exception IllegalActionException Always thrown.
     */
    public FixPoint fixValue() throws IllegalActionException {
        throw new IllegalActionException("Cannot convert the value in " +
                getClass().getName() + " to a FixPoint losslessly.");
    }

    /** Test that the value of this Token is close to the argument
     *  Token.  The argument and this token are converted to
     *  equivalent types, and then compared.  Generally, this is the
     *  higher of the type of this token and the argument type.
     *  Subclasses should override the protected _isCloseTo() method
     *  to perform the correct type-specific operation.
     *
     *  @see #isEqualTo
     *  @param rightArgument The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.
     *  @return A boolean token that contains the value true if the
     *  units of this token and the argument token are the same, and their
     *  values are close.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be compared with this token.
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
        if(typeInfo == CPO.SAME) {
            return _doIsCloseTo(rightArgument, epsilon);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
             try {
                BooleanToken result = _doIsCloseTo(convertedArgument, epsilon);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
               throw new IllegalActionException(null, ex,
                        notSupportedMessage("closeness", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
             return rightArgument.isCloseTo(this, epsilon);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("closeness",
                            this, rightArgument));
        }
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  The argument and this token are converted to
     *  equivalent types, and then compared.  Generally, this is the
     *  higher of the type of this token and the argument type.  This
     *  method defers to the _isEqualTo method to perform a
     *  type-specific equality check.  Derived classes should override
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
        if(typeInfo == CPO.SAME) {
            return _doIsEqualTo(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
             try {
                BooleanToken result = _doIsEqualTo(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
               throw new IllegalActionException(null, ex,
                        notSupportedMessage("equality", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
             return rightArgument.isEqualTo(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("equality",
                            this, rightArgument));
        }
    }

    /** Return the value of this token as an int.
     *  In this base class, we just throw an exception.
     *  @return The value of this token.
     *  @exception IllegalActionException Always thrown.
     */
    public int intValue() throws IllegalActionException {
        throw new IllegalActionException("Cannot convert the value in " +
                getClass().getName() + " to an int losslessly.");
    }

    /** Check whether the value of this token is strictly greater than
     *  that of the argument token.  The argument and this token are
     *  converted to equivalent types, and then compared.  Generally,
     *  this is the higher of the type of this token and the argument
     *  type.  This method defers to the _isLessThan method to perform
     *  a type-specific equality check.  Derived classes should
     *  override that method to provide type specific actions for
     *  equality testing.
     *
     *  @param rightArgument The token to compare against.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A boolean token with value true if this token is
     *  strictly greater than the argument.
     */
    public final BooleanToken isGreaterThan(ScalarToken rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if(typeInfo == CPO.SAME) {
            return rightArgument._doIsLessThan(this);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
            try {
                BooleanToken result = convertedArgument._doIsLessThan(this);
                return result;
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
     *  method defers to the _isLessThan method to perform a
     *  type-specific equality check.  Derived classes should override
     *  that method to provide type specific actions for equality
     *  testing.
     *
     *  @param rightArgument The token to compare against.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A boolean token with value true if this token is
     *  strictly less than the argument.
     */
    public final BooleanToken isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if(typeInfo == CPO.SAME) {
            return _doIsLessThan(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
             try {
                BooleanToken result = _doIsLessThan(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("isLessThan", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
             return rightArgument.isGreaterThan(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("isLessThan",
                            this, rightArgument));
        }
    }

    /** Return a scalar token that contains the value of this token in the
     *  units of the argument token. The unit category of the argument token
     *  must be the same as that of this token, otherwise, an exception will
     *  be thrown. The returned token is unitless.
     *  @param units A scalar token that represents a unit.
     *  @return A scalar token that do not have a unit.
     *  @exception IllegalActionException If the unit category of the
     *   argument token is not the same as that of this one.
     */
    public ScalarToken inUnitsOf(ScalarToken units)
            throws IllegalActionException {
        if ( !_areUnitsEqual(units)) {
            throw new IllegalActionException("ScalarToken.inUnitsOf: "
                    + "The units of this token: " + unitsString()
                    + " are not the same as the units of the argument: "
                    + units.unitsString());
        }
        return (ScalarToken)this.divide(units);
    }

    /** Return the value of this token as a long integer.
     *  In this base class, we just throw an exception.
     *  @return A long
     *  @exception IllegalActionException Always thrown.
     */
    public long longValue() throws IllegalActionException {
        throw new IllegalActionException("Cannot convert the value in " +
                getClass().getName() + " to a long losslessly.");
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  Type conversion also
     *  occurs here, so that the operation is performed at the least
     *  type necessary to ensure precision.  The returned type is the
     *  same as the type chosen for the operation, which is the higher
     *  of the type of this token and the argument type.  Subclasses
     *  should override the protected _modulo() method to perform the
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
        if(typeInfo == CPO.SAME) {
            Token result = _doModulo(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
             try {
                Token result = _doModulo(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
               throw new IllegalActionException(null, ex,
                        notSupportedMessage("modulo", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
             Token result = rightArgument.moduloReverse(this);
             return result;
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
     *  should override the protected _modulo() method to perform the
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
                Token result = convertedArgument._doModulo(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                    notSupportedMessage("moduloReverse",
                            this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((ScalarToken)leftArgument)._doModulo(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.modulo(this);
            return result;
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
     *  the argument type.  Subclasses should override the protected
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
        if(typeInfo == CPO.SAME) {
            Token result = _doMultiply(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
             try {
                Token result = _doMultiply(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
               throw new IllegalActionException(null, ex,
                        notSupportedMessage("multiply", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
             Token result = rightArgument.multiplyReverse(this);
             return result;
        } else {
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
     *  the argument type.  Subclasses should override the protected
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
                Token result = convertedArgument._doMultiply(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                    notSupportedMessage("multiplyReverse",
                            this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((ScalarToken)leftArgument)._doMultiply(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.multiply(this);
            return result;
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("multiplyReverse",
                            this, leftArgument));
        }
    }

    /** Set the unit category this token belongs to.
     *  @param index The unit category index.
     */
    // FIXME: shouldn't this be protected???  it violates the immutability of
    // tokens.
    public void setUnitCategory(int index) {
        _unitCategoryExponents = new int[index+1];
        Arrays.fill(_unitCategoryExponents, 0);
        _unitCategoryExponents[index] = 1;
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  Type
     *  conversion also occurs here, so that the operation is
     *  performed at the least type necessary to ensure precision.
     *  The returned type is the same as the type chosen for the
     *  operation, which is the higher of the type of this token and
     *  the argument type.  Subclasses should override the protected
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
        if(typeInfo == CPO.SAME) {
            Token result = _doSubtract(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            ScalarToken convertedArgument = (ScalarToken)
                getType().convert(rightArgument);
             try {
                Token result = _doSubtract(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
               throw new IllegalActionException(null, ex,
                        notSupportedMessage("subtract", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
             Token result = rightArgument.subtractReverse(this);
             return result;
        } else {
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
     *  the argument type.  Subclasses should override the protected
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
                Token result = convertedArgument._doSubtract(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                    notSupportedMessage("subtractReverse",
                            this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((ScalarToken)leftArgument)._doSubtract(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.subtract(this);
            return result;
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
        if (_isUnitless(_unitCategoryExponents)) {
            return "";
        }

        String positiveUnits = "";
        String negativeUnits = "";
        boolean justOnePositive = true;
        boolean justOneNegative = true;
        for (int i = 0; i<_unitCategoryExponents.length; i++) {
            int exponent = _unitCategoryExponents[i];
            if (exponent != 0) {
                String baseString = null;
                baseString = UnitSystem.getBaseUnitName(i);
                if (exponent > 0) {
                    for (int j = 0; j < exponent; j++) {
                        if (positiveUnits.equals("")) {
                            positiveUnits = baseString;
                        } else {
                            positiveUnits += " * " + baseString;
                            justOnePositive = false;
                        }
                    }
                } else {
                    for (int j = 0; j < -exponent; j++) {
                        if (negativeUnits.equals("")) {
                            negativeUnits = baseString;
                        } else {
                            negativeUnits += " * " + baseString;
                            justOneNegative = false;
                        }
                    }
                }
            }
        }

        if (positiveUnits.equals("") && negativeUnits.equals("")) {
            return "";
        }

        if (positiveUnits.equals("")) {
            positiveUnits = "1";
        } else if (justOnePositive) {
            positiveUnits = positiveUnits;
        } else {
            positiveUnits = "(" + positiveUnits + ")";
        }

        if (negativeUnits.equals("")) {
            return positiveUnits;
        } else if (justOneNegative) {
            return positiveUnits + " / " + negativeUnits;
        } else {
            return positiveUnits + " / (" + negativeUnits + ")";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is
     *  returned.  Note that it is explicitly allowable to return this
     *  token, since the units are the same. Derived classes must implement
     *  this method in a type-specific fashion
     *  @return A ScalarToken, which is likely, but not required to be
     *  the same type as this token.
     */
    protected abstract ScalarToken _absolute();

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is the same as the type of this
     *  class.  This method should be overridden in derived classes to
     *  provide type-specific operation and return a token of the
     *  appropriate subclass.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
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
        return _addCategoryExponents(token._unitCategoryExponents);
    }

    /** Return a copy of the unit category exponents array. If this
     *  token does not have a unit, return null;
     *  @return An int array that is a copy of the unit category
     *   exponents of this token.
     */
    protected int[] _copyOfCategoryExponents() {
        if (_isUnitless()) {
            return null;
        }

        int length = _unitCategoryExponents.length;
        int[] exponents = new int[length];
        System.arraycopy(_unitCategoryExponents, 0, exponents, 0, length);
        return exponents;
    }

    /** Return true if the units of this token are the same as that of the
     *  argument token. If both tokens do not have units, return true.
     *  @param scalarToken A scalar token.
     *  @return True if the units of this token is the same as that of the
     *   argument token; false otherwise.
     */
    protected boolean _areUnitsEqual(ScalarToken scalarToken) {
        boolean isThisUnitless = _isUnitless(this._unitCategoryExponents);
        boolean isArgumentUnitless =
            _isUnitless(scalarToken._unitCategoryExponents);

        // Either this token, or the argument token, or both have non null
        // exponent arrays.
        if (isThisUnitless && isArgumentUnitless) {
            return true;
        } else if (isThisUnitless || isArgumentUnitless) {
            // one is unitless, the other is not.
            return false;
        } else {
            // both have units.
            int thisLength = _unitCategoryExponents.length;
            int argumentLength = scalarToken._unitCategoryExponents.length;
            int shorterLength = (thisLength <= argumentLength) ? thisLength :
                argumentLength;
            for (int i = 0; i < shorterLength; i++) {
                if (_unitCategoryExponents[i] !=
                        scalarToken._unitCategoryExponents[i]) {
                    return false;
                }
            }

            for (int i = shorterLength; i < thisLength; i++) {
                if (_unitCategoryExponents[i] != 0) {
                    return false;
                }
            }
            for (int i = shorterLength; i < argumentLength; i++) {
                if (scalarToken._unitCategoryExponents[i] != 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  It is assumed that
     *  the type of the argument is the same as the type of this
     *  class.  This method should be overridden in derived classes to
     *  provide type-specific operation and return a token of the
     *  appropriate subclass.
     *  @param rightArgument The token to divide this token by.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract ScalarToken _divide(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type and units of the argument
     *  is the same as the type of this class.  This method should be
     *  overridden in derived classes to provide type-specific
     *  operation and return a token of the appropriate subclass.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract BooleanToken _isCloseTo(
            ScalarToken rightArgument, double epsilon)
            throws IllegalActionException;

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is assumed that the type and units of the argument
     *  is the same as the type of this class.  This method should be
     *  overridden in derived classes to provide type-specific
     *  operation and return a token of the appropriate subclass.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract BooleanToken _isEqualTo(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type and units of the argument
     *  is the same as the type of this class.  This method should be
     *  overridden in derived classes to provide type-specific
     *  operation and return a token of the appropriate subclass.
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
        return _isUnitless(_unitCategoryExponents);
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is the same as the type of this
     *  class.  This method should be overridden in derived classes to
     *  provide type-specific operation and return a token of the
     *  appropriate subclass.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract ScalarToken _modulo(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is the same as the type of this class.
     *  This method should be overridden in derived
     *  classes to provide type-specific operation and return a token of the
     *  appropriate subclass.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected abstract ScalarToken _multiply(ScalarToken rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is assumed
     *  that the type of the argument is the same as the type of this
     *  class.  This method should be overridden in derived classes to
     *  provide type-specific operation and return a token of the
     *  appropriate subclass.
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
        // negate the exponents of the argument token and add to
        // this token.
        int[] negation = null;
        if ( !token._isUnitless()) {
            int length = token._unitCategoryExponents.length;
            negation = new int[length];
            for (int i = 0; i < length; i++) {
                negation[i] = -token._unitCategoryExponents[i];
            }
        }
        return _addCategoryExponents(negation);
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

    // Add the exponent array of this token with the argument array,
    // and return the result in a new array.
    private int[] _addCategoryExponents(int[] exponents) {
        boolean isThisUnitless = _isUnitless(this._unitCategoryExponents);
        boolean isArgumentUnitless = _isUnitless(exponents);

        if (isThisUnitless && isArgumentUnitless) {
            return null;
        }

        // Either this token, or the argument token, or both have non null
        // exponent arrays.
        if (isThisUnitless) {
            // exponents is not unitless.
            int length = exponents.length;
            int[] result = new int[length];
            System.arraycopy(exponents, 0, result, 0, length);
            return result;
        }
        if (isArgumentUnitless) {
            // this._unitCategoryExponents is not unitless.
            int length = this._unitCategoryExponents.length;
            int[] result = new int[length];
            System.arraycopy(_unitCategoryExponents, 0, result, 0, length);
            return result;
        }

        // both have units.
        int thisLength = _unitCategoryExponents.length;
        int argumentLength = exponents.length;
        int[] result;
        if (thisLength <= argumentLength) {
            result = new int[argumentLength];
            System.arraycopy(exponents, 0, result, 0, argumentLength);
            for (int i = 0; i < thisLength; i++) {
                result[i] += _unitCategoryExponents[i];
            }
        } else {
            result = new int[thisLength];
            System.arraycopy(_unitCategoryExponents, 0, result, 0, thisLength);
            for (int i = 0; i < argumentLength; i++) {
                result[i] += exponents[i];
            }
        }

        if (_isUnitless(result)) {
            return null;
        }
        return result;
    }

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is the same as the type of this class
     *  and has the same units as this token.  The resulting token
     *  will also have the same type and units.  This method defers to
     *  the _add method that takes a ScalarToken.  Derived classes
     *  should override that method instead to provide type-specific
     *  operation.
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
                    + " because the units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + convertedArgument.unitsString());
        }
        ScalarToken result = _add(convertedArgument);
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  It is assumed
     *  that the type of the argument is the same as the type of this
     *  class.  The resulting token will also have the same type and
     *  appropriate units.  This method defers to the _divide method
     *  that takes a ScalarToken.  Derived classes should override
     *  that method instead to provide type-specific operation.
     *  @param rightArgument The token to divide this token by.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
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

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is the
     *  same as the type of this class.  This class overrides the base
     *  class to return BooleanToken.FALSE if the units of this token
     *  and the given token are not identical.  This method may defer
     *  to the _isCloseTo method that takes a ScalarToken.  Derived
     *  classes should override that method instead to provide
     *  type-specific operation.
     *  @param rightArgument The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    private BooleanToken _doIsCloseTo(
            Token rightArgument, double epsilon)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("isCloseTo", this, rightArgument)
                    + " because the units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + convertedArgument.unitsString());
        }

        return _isCloseTo(convertedArgument, epsilon);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is the
     *  same as the type of this class.  This method returns
     *  BooleanToken.FALSE if the units of this token and the given
     *  token are not identical.  This method may defer to the
     *  _isEqualTo method that takes a ScalarToken.  Derived classes
     *  should override that method instead to provide type-specific
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
     *  Token.  It is assumed that the type of the argument is the
     *  same as the type of this class.  This method returns
     *  BooleanToken.FALSE if the units of this token and the given
     *  token are not identical.  This method may defer to the
     *  _isLessThan method that takes a ScalarToken.  Derived classes
     *  should override that method instead to provide type-specific
     *  operation.
     *  @param rightArgument The token with which to test ordering.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    private BooleanToken _doIsLessThan(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("isLessThan", this, rightArgument)
                    + " because the units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + convertedArgument.unitsString());
        }

        return _isLessThan(convertedArgument);
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is the same as the type of this class
     *  and has the same units as this token.  The resulting token
     *  will also have the same type and units.  This method defers to
     *  the _modulo method that takes a ScalarToken.  Derived classes
     *  should override that method instead to provide type-specific
     *  operation.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new Token containing the result.
     */
    private Token _doModulo(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        ScalarToken result = _modulo(convertedArgument);
        if (!_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("modulo", this, rightArgument)
                    + " because the units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + convertedArgument.unitsString());
        }
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is the same as the type of this
     *  class.  The resulting token will also have the same type and
     *  appropriate units.  This method defers to the _multiply method
     *  that takes a ScalarToken.  Derived classes should override
     *  that method instead to provide type-specific operation.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
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
     *  token subtracted from the value of this token.  It is assumed
     *  that the type of the argument is the same as the type of this
     *  class and has the same units as this token.  The resulting
     *  token will also have the same type and units.  This method
     *  defers to the _subtract method that takes a ScalarToken.
     *  Derived classes should override that method instead to provide
     *  type-specific operation.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new Token containing the result.
     */
    private Token _doSubtract(Token rightArgument)
            throws IllegalActionException {
        ScalarToken convertedArgument = (ScalarToken)rightArgument;
        ScalarToken result = _subtract(convertedArgument);
        if ( !_areUnitsEqual(convertedArgument)) {
            throw new IllegalActionException(
                    notSupportedMessage("subtract", this, rightArgument)
                    + " because the units of this token: " + unitsString()
                    + " are not the same as those of the argument: "
                    + convertedArgument.unitsString());
        }
        result._unitCategoryExponents = _copyOfCategoryExponents();
        return result;
    }

    // Return true if this token does not have a unit.
    private boolean _isUnitless(int[] exponents) {
        if (exponents != null) {
            for (int i = 0; i < exponents.length; i++) {
                if (exponents[i] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
