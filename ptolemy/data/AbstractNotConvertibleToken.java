/* Base class for data capsules.

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
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AbstractNotConvertibleToken
/**
The Token base class provides a very general interface for building
new data types.  However, in many ways, the interface is rather
complex in order to allow operations between tokens that are defined
in different classes.  In particular, this requires the duplicate
operation() and operationReverse() methods.

<p> This base class is intended to make it easy to implement tokens
where operations are only defined for other tokens that are defined in
the same class.  In these cases, the operation() and
operationReverse() method can share the same code.  This class
implements these methods to ensure that the argument of each operation
is actually an instance of the correct class.  The implementation then
defers to a protected _operation() method.  These protected methods
should be overridden in derived classes to provide type-specific
operations.

<p> This class is used a base class for ArrayToken and RecordToken.
Note that these classes actually represent tokens having different
types in the type lattice.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
*/
public abstract class AbstractNotConvertibleToken extends Token {

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
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token add(Token rightArgument) throws IllegalActionException {
        if (getClass() != (rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("add",
                            this, rightArgument));
        }
        try {
            return _add(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("add",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the sum of this token and
     *  the argument.  This base class ensures that the
     *  arguments are implemented in the same class, and then defers
     *  to the _add() method.  Subclasses should override that method to
     *  perform type-specific operation.
     *  @param leftArgument The token to add this token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token addReverse(ptolemy.data.Token leftArgument)
            throws IllegalActionException {
        if (getClass() != (leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("addReverse",
                            this, leftArgument));
        }
        try {
            return ((AbstractNotConvertibleToken)leftArgument)._add(this);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("addReverse",
                            this, leftArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _divide() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token divide(Token rightArgument) throws IllegalActionException {
        if (getClass() != (rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("divide",
                            this, rightArgument));
        }
        try {
            return _divide(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("divide",
                            this, rightArgument));
        }
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
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token divideReverse(Token leftArgument)
            throws IllegalActionException {
        if (getClass() != (leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("divideReverse",
                            this, leftArgument));
        }

        try {
            return ((AbstractNotConvertibleToken)leftArgument)._divide(this);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("divideReverse",
                            this, leftArgument));
        }
    }

    /** Test that the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. This base class ensures that the arguments are
     *  implemented in the same class, and then defers to the
     *  _isCloseTo() method.  Subclasses should override that method to
     *  perform type-specific operation.
     *  @param token The token to test closeness of this token with.
     *  @param epsilon  The value that we use to determine whether two
     *   tokens are close.
     *  @return A boolean token that contains the value true if the
     *   value of this token is close to that of the argument token.
     *  @exception IllegalActionException If the argument token and
     *   this token are implemented in different classes.
     */
    public BooleanToken isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        if (getClass() != (token.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("isCloseTo",
                            this, token));
        }

        try {
            return _isCloseTo(token, epsilon);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("isCloseTo",
                            this, token));
        }
    }

    /** Test for equality of the values of this token and the argument
     *  token.   This base class ensures that the arguments are
     *  implemented in the same class, and then defers to the
     *  _isEqualTo() method.  Subclasses should override that method to
     *  perform type-specific operation.
     *
     *  @param rightArgument The token with which to test equality.
     *  @return A boolean token that contains the value true if the
     *   value of this token is equal to that of the argument token.
     *  @exception IllegalActionException If the argument token and
     *   this token are implemented in different classes.
     */
    public BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        if (getClass() != (rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("isEqualTo",
                            this, rightArgument));
        }
        return _isEqualTo(rightArgument);
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _modulo() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token modulo(Token rightArgument) throws IllegalActionException {
        if (getClass() != (rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("modulo",
                            this, rightArgument));
        }
        try {
            return _modulo(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("modulo",
                            this, rightArgument));
        }
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
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        if (getClass() != (leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("moduloReverse",
                            this, leftArgument));
        }

        try {
            return ((AbstractNotConvertibleToken)leftArgument)._modulo(this);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("moduloReverse",
                            this, leftArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _multiply() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token multiply(Token rightArgument) throws IllegalActionException {
        if (getClass() != (rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("multiply",
                            this, rightArgument));
        }

        try {
            return _multiply(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("multiply",
                            this, rightArgument));
        }
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
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token multiplyReverse(Token leftArgument)
            throws IllegalActionException {
        if (getClass() != (leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("multiplyReverse",
                            this, leftArgument));
        }

        try {
            return ((AbstractNotConvertibleToken)leftArgument)._multiply(this);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("multiplyReverse",
                            this, leftArgument));
        }
    }

    /** Return a string with an error message that states that the
     *  given operation is not supported between two tokens, because
     *  they have incomparable types and cannot be converted to the
     *  same type.
     *  @param operation A string naming the unsupported token
     *  operation.
     *  @param firstToken The first token in the message.
     *  @param secondToken The second token in the message.
     *  @return A string error message.
     */
    public static String notSupportedDifferentClassesMessage(String operation,
            Token firstToken, Token secondToken) {
        // We use this method to factor out a very common message
        return (operation + " method not supported between "
                + firstToken.getClass().getName()
                + " '" + firstToken.toString()
                + "' and "
                + secondToken.getClass().getName()
                + " '" + secondToken.toString()
                + "' because the tokens have different classes.");
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _subtract() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token subtract(Token rightArgument)
            throws IllegalActionException {
        if (getClass() != (rightArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("subtract",
                            this, rightArgument));
        }
        try {
            return _subtract(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("subtract",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.  This base class
     *  ensures that the arguments are implemented in the same class,
     *  and then defers to the _subtract() method.  Subclasses should
     *  override that method to perform type-specific operation.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    public Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        if (getClass() != (leftArgument.getClass())) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("subtractReverse",
                            this, leftArgument));
        }

        try {
            return ((AbstractNotConvertibleToken)leftArgument)._subtract(this);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex,
                    notSupportedMessage("subtractReverse",
                            this, leftArgument));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  token added to the value of this token.  It is guaranteed by
     *  the caller that the type of the argument is the same as the
     *  type of this class.  This method should be overridden in
     *  derived classes to provide type specific actions for add.
     *  @param rightArgument The token whose value we add to the value
     *  of this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _add(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  It is guaranteed
     *  by the caller that the type of the argument is the same as the
     *  type of this class.  This method should be overridden in
     *  derived classes to provide type specific actions for divide.
     *  @param rightArgument The token to divide this token by.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _divide(Token rightArgument)
            throws IllegalActionException;

    /** Test that the value of this Token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.
     *  It is guaranteed by the caller that the type
     *  of the argument is the same as the type of this class.  This
     *  method should be overridden in derived classes to provide type
     *  specific actions for divide.
     *  @param rightArgument The token with which to test closeness.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A token that contains the result of the test.
     */
    protected abstract BooleanToken _isCloseTo(
            Token rightArgument, double epsilon)
            throws IllegalActionException;

    /** Test for equality of the values of this token and the argument.
     *  This base class delegates to the equals() method.
     *  @param token The token to compare to this token.
     *  @return A token containing true if the value element of the first
     *   argument is equal to the value of this token.
     *         @exception IllegalActionException Not thrown in this base class.
     */
    protected BooleanToken _isEqualTo(Token token)
            throws IllegalActionException {
        return BooleanToken.getInstance(equals(token));
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is guaranteed by
     *  the caller that the type of the argument is the same as the
     *  type of this class.  This method should be overridden in
     *  derived classes to provide type specific actions for modulo.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _modulo(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class.  This method should be
     *  overridden in derived classes to provide type specific actions
     *  for multiply.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _multiply(Token rightArgument)
            throws IllegalActionException;

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class.  This method should be
     *  overridden in derived classes to provide type specific actions
     *  for subtract.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new token containing the result that is of the same
     *  class as this token.
     */
    protected abstract Token _subtract(Token rightArgument)
            throws IllegalActionException;
}
