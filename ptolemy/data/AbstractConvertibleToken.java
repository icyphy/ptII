/* Base class for data capsules.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// AbstractConvertibleToken

/**
 The Token base class provides a very general interface for building
 new data types.  However, in many ways, the interface is rather complex
 in order to allow consistent implementation of Token operations that
 operate on tokens that are defined in different classes.  In particular,
 this requires the duplicate operation and operationReverse methods.

 <p> This base class is intended to make it easy to implement tokens
 that perform operations on other data types.  In most (but not all)
 cases, these operations to be performed only on types that can be
 losslessly converted to this type (or vice versa).  The operations are
 implemented to first perform the conversion to the same type, and then
 to perform the operation.  This class provides a base class
 implementation of the operation methods which performs the appropriate
 conversion, and then defers to a protected _operation method if the
 type of the argument of the operation is less than or equal to this
 type.  If the argument type is greater than this type, then the
 appropriate method is called on that type.  If the types are
 incomparable, then this class throws an exception. The protected
 methods should be overridden in derived classes to provide
 type-specific operations.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red

 */
public abstract class AbstractConvertibleToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token and
     *  the argument. Type conversion also occurs here, so that the
     *  operation is performed at the least type necessary to ensure
     *  precision.  The returned type is the same as the type chosen
     *  for the operation.  Generally, this is higher of the type of
     *  this token and the argument type.  Subclasses should not
     *  generally override this method, but override the protected
     *  _add() method to ensure that type conversion is performed
     *  consistently.
     *  @param rightArgument The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    @Override
    public Token add(Token rightArgument) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);

        if (typeInfo == CPO.SAME) {
            Token result = _add(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(rightArgument);

            try {
                Token result = _add(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "add", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.addReverse(this);
            return result;
        } else {
            // FIXME: do conversion here?
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "add", this, rightArgument));
        }
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type conversion also occurs here, so that the
     *  operation is performed at the least type necessary to ensure
     *  precision.  The returned type is the same as the type chosen
     *  for the operation.  Generally, this is higher of the type of
     *  this token and the argument type.  Subclasses should not
     *  generally override this method, but override the protected
     *  _add() method to ensure that type conversion is performed
     *  consistently.
     *  @param leftArgument The token to add this token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    @Override
    public Token addReverse(ptolemy.data.Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());

        // We would normally expect this to be LOWER, since this will almost
        // always be called by subtract, so put that case first.
        if (typeInfo == CPO.LOWER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(leftArgument);

            try {
                Token result = convertedArgument._add(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "addReverse", this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((AbstractConvertibleToken) leftArgument)._add(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.add(this);
            return result;
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "addReverse", this, leftArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation.  Generally,
     *  this is higher of the type of this token and the argument
     *  type.  Subclasses should not generally override this method,
     *  but override the protected _divide() method to ensure that type
     *  conversion is performed consistently.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   and this token are of incomparable types, or the operation
     *   does not make sense for the given types.
     */
    @Override
    public final Token divide(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);

        if (typeInfo == CPO.SAME) {
            Token result = _divide(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(rightArgument);

            try {
                Token result = _divide(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "divide", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.divideReverse(this);
            return result;
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "divide", this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  divided by the value of this token. Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation.  Generally,
     *  this is higher of the type of this token and the argument
     *  type.  Subclasses should not generally override this method,
     *  but override the protected _divide() method to ensure that type
     *  conversion is performed consistently.
     *  @param leftArgument The token to be divided by the value of this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    @Override
    public final Token divideReverse(Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());

        // We would normally expect this to be LOWER, since this will almost
        // always be called by subtract, so put that case first.
        if (typeInfo == CPO.LOWER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(leftArgument);

            try {
                Token result = convertedArgument._divide(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "divideReverse", this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((AbstractConvertibleToken) leftArgument)
                    ._divide(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.divide(this);
            return result;
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "divideReverse", this, leftArgument));
        }
    }

    /** Test that the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. If the argument token is not of
     *  the same type as this token, then either this token or the
     *  argument will be converted, if possible, to the type of the other.
     *  <p>
     *  Subclasses should not
     *  generally override this method, but override the protected
     *  _isCloseTo() method to ensure that type conversion is performed
     *  consistently.
     *  @param token The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @return A boolean token that contains the value true if the
     *   value and units of this token are close to those of the
     *   argument token.
     *  @exception IllegalActionException If the argument token and
     *   this token are of incomparable types, or the operation does
     *   not make sense for the given types.
     */
    @Override
    public final BooleanToken isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        // Note that if we had absolute(), subtraction() and islessThan()
        // we could perhaps define this method for all tokens.
        int typeInfo = TypeLattice.compare(getType(), token);

        if (typeInfo == CPO.SAME) {
            return _isCloseTo(token, epsilon);
        } else if (typeInfo == CPO.HIGHER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(token);

            try {
                BooleanToken result = _isCloseTo(convertedArgument, epsilon);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "isCloseTo", this, token));
            }
        } else if (typeInfo == CPO.LOWER) {
            return token.isCloseTo(this, epsilon);
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "isCloseTo", this, token));
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
     *  @param rightArgument The token with which to test equality.
     *  @return A BooleanToken which contains the result of the test.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    @Override
    public final BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);

        if (typeInfo == CPO.SAME) {
            return _isEqualTo(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(rightArgument);

            try {
                BooleanToken result = _isEqualTo(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "isEqualTo", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.isEqualTo(this);
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "isEqualTo", this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation.  Generally,
     *  this is higher of the type of this token and the argument
     *  type.  Subclasses should not override this method,
     *  but override the protected _modulo() method to ensure that type
     *  conversion is performed consistently.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    @Override
    public final Token modulo(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);

        if (typeInfo == CPO.SAME) {
            Token result = _modulo(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(rightArgument);

            try {
                Token result = _modulo(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "modulo", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.moduloReverse(this);
            return result;
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "modulo", this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  modulo the value of this token.  Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation.  Generally,
     *  this is higher of the type of this token and the argument
     *  type.  Subclasses should not override this method,
     *  but override the protected _modulo() method to ensure that type
     *  conversion is performed consistently.
     *  @param leftArgument The token to apply modulo to by the value
     *  of this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    @Override
    public final Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());

        // We would normally expect this to be LOWER, since this will almost
        // always be called by modulo, so put that case first.
        if (typeInfo == CPO.LOWER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(leftArgument);

            try {
                Token result = convertedArgument._modulo(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "moduloReverse", this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((AbstractConvertibleToken) leftArgument)
                    ._modulo(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.modulo(this);
            return result;
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "moduloReverse", this, leftArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation.  Generally,
     *  this is higher of the type of this token and the argument
     *  type.  Subclasses should not generally override this method,
     *  but override the protected _multiply() method to ensure that type
     *  conversion is performed consistently.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   and this token are of incomparable types, or the operation
     *   does not make sense for the given types.
     */
    @Override
    public final Token multiply(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);

        if (typeInfo == CPO.SAME) {
            Token result = _multiply(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(rightArgument);

            try {
                Token result = _multiply(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "multiply", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.multiplyReverse(this);
            return result;
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "multiply", this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  multiplied by the value of this token.  Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation.  Generally,
     *  this is higher of the type of this token and the argument
     *  type.  Subclasses should not generally override this method,
     *  but override the protected _multiply() method to ensure that type
     *  conversion is performed consistently.
     *  @param leftArgument The token to be multiplied by the value of
     *  this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    @Override
    public final Token multiplyReverse(Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());

        // We would normally expect this to be LOWER, since this will almost
        // always be called by multiply, so put that case first.
        if (typeInfo == CPO.LOWER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(leftArgument);

            try {
                Token result = convertedArgument._multiply(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "multiplyReverse", this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((AbstractConvertibleToken) leftArgument)
                    ._multiply(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.multiply(this);
            return result;
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "multiplyReverse", this, leftArgument));
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.   Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation.  Generally,
     *  this is higher of the type of this token and the argument
     *  type.  Subclasses should not override this method,
     *  but override the protected _subtract() method to ensure that type
     *  conversion is performed consistently.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    @Override
    public final Token subtract(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);

        if (typeInfo == CPO.SAME) {
            Token result = _subtract(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(rightArgument);

            try {
                Token result = convertedArgument._subtract(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "subtract", this, rightArgument));
            }

        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.subtractReverse(this);
            return result;
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "subtract", this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.   Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation.  Generally,
     *  this is higher of the type of this token and the argument
     *  type.  Subclasses should not override this method,
     *  but override the protected _subtract() method to ensure that type
     *  conversion is performed consistently.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    @Override
    public final Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(leftArgument, getType());

        // We would normally expect this to be LOWER, since this will almost
        // always be called by subtract, so put that case first.
        if (typeInfo == CPO.LOWER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(leftArgument);

            try {
                Token result = convertedArgument._subtract(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "subtractReverse", this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((AbstractConvertibleToken) leftArgument)
                    ._subtract(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.subtract(this);
            return result;
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "subtractReverse", this, leftArgument));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is the same as the type of this class.
     *  This method should be overridden in derived
     *  classes to provide type specific actions for add.
     *  @param rightArgument The token whose value we add to the value of
     *   this token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected abstract Token _add(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed
     *  that the type of the argument is the same as the type of this class.
     *  This method should be overridden in derived
     *  classes to provide type specific actions for divide.
     *  @param rightArgument The token to divide this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected abstract Token _divide(Token rightArgument)
            throws IllegalActionException;

    /** Test that the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. It is assumed that the type of
     *  the argument is the same as the type of this class.
     *  This method should be overridden in derived classes to
     *  provide type specific actions for the comparison.
     *  @param token The token with which to test closeness.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @exception IllegalActionException If this method is not
     *   supported by a derived class.
     *  @return A token that contains the result of the test.
     */
    protected abstract BooleanToken _isCloseTo(Token token, double epsilon)
            throws IllegalActionException;

    /** Test for equality of the values of this token and the argument.
     *  It is assumed that the type of the argument is the
     *  same as the type of this class.  This method should be
     *  overridden in derived classes to provide type specific actions
     *  for the comparison.
     *  @param token The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *   supported by a derived class.
     *  @return A token that contains the result of the test.
     */
    protected abstract BooleanToken _isEqualTo(Token token)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed
     *  that the type of the argument is the same as the type of this class.
     *  This method should be overridden in derived
     *  classes to provide type specific actions for modulo.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected abstract Token _modulo(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is the same as the type of this class.
     *  This method should be overridden in derived
     *  classes to provide type specific actions for multiply.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected abstract Token _multiply(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed
     *  that the type of the argument is the same as the type of this class.
     *  This method should be overridden in derived
     *  classes to provide type specific actions for subtract.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected abstract Token _subtract(Token rightArgument)
            throws IllegalActionException;
}
