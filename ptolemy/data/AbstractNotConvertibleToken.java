/* Base class for data capsules.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;

import java.io.Serializable;

//////////////////////////////////////////////////////////////////////////
//// AbstractNotConvertibleToken
/**
The Token base class provides a very general interface for building
new data types.  However, in many ways, the interface is rather complex
in order to allow consistent implementation of Token operations that
operate on tokens that are defined in different classes.  In particular,
this requires the duplicate operation and operationReverse methods.

<p> This base class is intended to make it easy to implement tokens
that only require operations on the same data type.  In these cases,
the operations and their reverses perform exactly the same operation.
This class provides a base class implementation of the operation
methods which checks to make sure that the arguments are actually both
implemented in the same class, and then defers to a protected
_operation method.  These protected methods should be overridden in
derived classes to provide type-specific operations.

<p> Note that the tokens operated on must only be implemented in the
same class, which does not require that they have the same Ptolemy
data type.  This commonly happens when StructuredTypes are used, as
with record and array tokens.

@author Steve Neuendorffer
@version $Id$
*/
public abstract class AbstractNotConvertibleToken extends Token
    implements Serializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token and
     *  the argument.  This base class ensures that the
     *  arguments are implemented in the same class, and then defers
     *  to the _add() method.  Subclasses should override that method to
     *  perform type-specific operation
     *  @param rightArgument The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token add(Token rightArgument) throws IllegalActionException {
        if(!getClass().equals(rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("add",
                            this, rightArgument));
        }

        Token result = _add(rightArgument);
        return result;
    }

    /** Return a new token whose value is the sum of this token and
     *  the argument.  This base class ensures that the
     *  arguments are implemented in the same class, and then defers
     *  to the _add() method.  Subclasses should override that method to
     *  perform type-specific operation.
     *  @param leftArgument The token to add this token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be added to this token, or the units of
     *  this token and the argument token are not the same.
     */
    public Token addReverse(ptolemy.data.Token leftArgument)
            throws IllegalActionException {
        if(!getClass().equals(leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("addReverse",
                            this, leftArgument));
        }

        Token result =
            ((AbstractNotConvertibleToken)leftArgument)._add(this);
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _divide() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token divide(Token rightArgument) throws IllegalActionException {
        if(!getClass().equals(rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("divide",
                            this, rightArgument));
        }

        Token result = _divide(rightArgument);
        return result;
    }

    /** Return a new token whose value is the value of the argument
     *  token divided by the value of this token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _divide() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param leftArgument The token to be divided by the value of
     *  this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token divideReverse(Token leftArgument)
            throws IllegalActionException {
        if(!getClass().equals(leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("divideReverse",
                            this, leftArgument));
        }

        Token result =
            ((AbstractNotConvertibleToken)leftArgument)._divide(this);
        return result;
    }

    /** Test that the value of this Token is close to the argument
     *  Token.  This base class ensures that the arguments are
     *  implemented in the same class, and then defers to the
     *  _isCloseTo() method.  Subclasses should override that method to
     *  perform type-specific operation.
     *
     *  @param rightArgument The token to test closeness of this token with.
     *  @param epsilon  The value that we use to determine whether two
     *  tokens are close.
     *  @return A boolean token that contains the value true if the
     *   value of this token is close to those of the argument
     *   token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        if(!getClass().equals(rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("isCloseTo",
                            this, rightArgument));
        }

        BooleanToken result = _isCloseTo(rightArgument, epsilon);
        return result;
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.   This base class ensures that the arguments are
     *  implemented in the same class, and then defers to the
     *  _isEqualTo() method.  Subclasses should override that method to
     *  perform type-specific operation.
     *
     *  @param rightArgument The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    public BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        if(!getClass().equals(rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("isEqualTo",
                            this, rightArgument));
        }

        BooleanToken result = _isEqualTo(rightArgument);
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _modulo() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token modulo(Token rightArgument) throws IllegalActionException {
        if(!getClass().equals(rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("modulo",
                            this, rightArgument));
        }

        Token result = _modulo(rightArgument);
        return result;
    }

    /** Return a new token whose value is the value of the argument token
     *  modulo the value of this token.   This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _modulo() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param leftArgument The token to apply modulo to by the value
     *  of this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        if(!getClass().equals(leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("moduloReverse",
                            this, leftArgument));
        }

        Token result =
            ((AbstractNotConvertibleToken)leftArgument)._modulo(this);
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _multiply() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   and this token are of incomparable types, or the operation
     *   does not make sense for the given types.
     */
    public Token multiply(Token rightArgument) throws IllegalActionException {
        if(!getClass().equals(rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("multiply",
                            this, rightArgument));
        }

        Token result = _multiply(rightArgument);
        return result;
    }

    /** Return a new token whose value is the value of the argument token
     *  multiplied by the value of this token. This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _multiply() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param leftArgument The token to be multiplied by the value of
     *  this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token multiplyReverse(Token leftArgument)
            throws IllegalActionException {
        if(!getClass().equals(leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("multiplyReverse",
                            this, leftArgument));
        }

        Token result =
            ((AbstractNotConvertibleToken)leftArgument)._multiply(this);
        return result;
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _multiply() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token subtract(Token rightArgument)
            throws IllegalActionException {
        if(!getClass().equals(rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("subtract",
                            this, rightArgument));
        }

        Token result = _subtract(rightArgument);
        return result;
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _multiply() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        if(!getClass().equals(leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("subtractReverse",
                            this, leftArgument));
        }

        Token result =
            ((AbstractNotConvertibleToken)leftArgument)._subtract(this);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is the same as the type of this
     *  class.  This method should be overridden in derived classes to
     *  provide type specific actions for add.
     *  @param rightArgument The token whose value we add to the value
     *  of this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _add(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is the same as the type of this
     *  class.  This method should be overridden in derived classes to
     *  provide type specific actions for divide.
     *  @param rightArgument The token to divide this token by.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _divide(Token rightArgument)
            throws IllegalActionException;

    /** Test for closeness of the values of this Token and the
     *  argument Token.  It is assumed that the type of the argument
     *  is the same as the type of this class.  This method should be
     *  overridden in derived classes to provide type specific actions
     *  for divide.
     *  @param rightArgument The token with which to test closeness.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    protected abstract BooleanToken _isCloseTo(
            Token rightArgument, double epsilon)
            throws IllegalActionException;

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is the
     *  same as the type of this class.  This method should be
     *  overridden in derived classes to provide type specific actions
     *  for divide.
     *  @param rightArgument The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    protected abstract BooleanToken _isEqualTo(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is the same as the type of this
     *  class.  This method should be overridden in derived classes to
     *  provide type specific actions for modulo.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _modulo(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is the same as the type of this
     *  class.  This method should be overridden in derived classes to
     *  provide type specific actions for multiply.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _multiply(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is assumed
     *  that the type of the argument is the same as the type of this
     *  class.  This method should be overridden in derived classes to
     *  provide type specific actions for subtract.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _subtract(Token rightArgument)
            throws IllegalActionException;
}
