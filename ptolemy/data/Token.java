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
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
Added pow() method for integer exponentiation.
Don't use to represent pure events.
*/

package ptolemy.data;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

import java.io.Serializable;

//////////////////////////////////////////////////////////////////////////
//// Token
/**
Token is the base class for data capsules.  Tokens are immutable,
meaning that their value cannot change after construction.  They have
a set of polymorphic methods providing a set of basic arithmetic and
logical operations.  Generally, derived classes should override the
methods to implement type specific operations that make sense for a
given type.  For operations that are non-sensical for a given type,
such as division of matrices, the implementation of this base class
can be used, which simply throws an exception.

<p> Generally, it is painful to implement both the operation and
operationReverse methods of this class.  It is also painful to
implement tokens that are automatically converted to other tokens in a
consistent fashion.  As such, there are several subclasses of this
class that implement these methods and provide a somewhat nicer
abstraction.  The ScalarToken derived class is useful for many types
that are losslessly convertible to other types and may be associated
with units, such as IntToken.  The MatrixToken derived class is useful
for implementing matrices of ScalarTokens, such as IntMatrixToken.
The AbstractNotConvertible derived class is useful for implementing
tokens that are not losslessly convertible to a token in implemented
in another class, such as ArrayToken.  Lastly,
AbstractConvertibleToken is useful for implementing tokens that are
losslessly convertible to a token in another class, but don't have
units, such as BooleanToken.

<p> Instances of this base class *should not* be used to represent
pure events, i.e., to indicate that an event is present. To represent
pure events, it is better to use the EventToken class.  The reasoning
is that the type BaseType.GENERAL is reserved to represent types which
the type system cannot represent exactly.  Using the EventToken class,
and the type BaseType.EVENT allows typesafe use of pure events.

@author Neil Smyth, Yuhong Xiong, Edward A. Lee, Christopher Hylands,
Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2

@see ScalarToken
@see AbstractConvertibleToken
@see AbstractNotConvertibleToken
@see MatrixToken
*/
public class Token implements Serializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token and
     *  the argument.
     *  @param rightArgument The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token add(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("add", this, rightArgument));
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument.
     *  @param leftArgument The token to add this token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token addReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("addReverse", this, leftArgument));
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token divide(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("divide", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument
     *  token divided by the value of this token.
     *  @param leftArgument The token to be divided by the value of
     *  this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token divideReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("divideReverse", this, leftArgument));
    }

    /** Return the type of this token.
     *  @return BaseType.GENERAL
     */
    public Type getType() {
        return BaseType.GENERAL;
    }

    /** Test that the value of this Token is close to the argument
     *  Token.  In this base class, we call isEqualTo().  This method
     *  should be overridden in derived classes such as DoubleToken
     *  and ComplexToken to provide type specific actions for
     *  equality testing using an epsilon factor.
     *
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @return a boolean token that contains the value true if the
     *  value and units of this token are close to those of the
     *  argument token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be compared with this token.
     */
    public final BooleanToken isCloseTo(Token token)
            throws IllegalActionException{
        return isCloseTo(token, ptolemy.math.Complex.EPSILON);
    }

    /** Test that the value of this Token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double.
     *  @param token The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @return A boolean token that contains the value true if the
     *   value of this token are close to those of the
     *   argument token.
     *  @exception IllegalActionException If the argument token is not
     *   of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("isCloseTo", this, token));
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.
     *
     *  @param rightArgument The token with which to test equality.
     *  @return A BooleanToken which contains the result of the test.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be compared with this token.
     */
    public BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("isEqualTo", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token modulo(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("modulo", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument token
     *  modulo the value of this token.
     *  @param leftArgument The token to apply modulo to by the value
     *  of this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("moduloReverse", this, leftArgument));
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token multiply(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("multiply", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument
     *  token multiplied by the value of this token.
     *  @param leftArgument The token to be multiplied by the value of
     *  this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token multiplyReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("multiplyReverse", this, leftArgument));
    }

    /** Return a string with an error message that states that
     *  the given token cannot be converted to the given token type.
     *  @param token The token being converted.
     *  @param typeString A string representing the type that is being
     *  converted to.
     *  @return A string error message.
     */
    public static String notSupportedConversionMessage(
            Token token, String typeString) {
        // We use this method to factor out a very common message
        return ("Conversion is not supported from "
                + token.getClass().getName()
                + " '" + token.toString()
                + "' to the type "
                + typeString + ".");
    }

    /** Return a string with an error message that states that
     *  the given token cannot be converted to the given token type.
     *  @param token The token being converted.
     *  @param typeString A string representing the type that is being
     *  converted to.
     *  @return A string error message.
     */
    public static String notSupportedIncomparableConversionMessage(
            Token token, String typeString) {
        // We use this method to factor out a very common message
        return ("Conversion is not supported from "
                + token.getClass().getName()
                + " '" + token.toString()
                + "' to the type " + typeString
                + " because the type of the token is higher "
                + "or incomparable with the given type.");
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
    public static String notSupportedIncomparableMessage(String operation,
            Token firstToken, Token secondToken) {
        // We use this method to factor out a very common message
        return (operation + " method not supported between "
                + firstToken.getClass().getName()
                + " '" + firstToken.toString()
                + "' and "
                + secondToken.getClass().getName()
                + " '" + secondToken.toString()
                + "' because the types are incomparable.");
    }

    /** Return a string with an error message that states that the
     *  given operation is not supported between two tokens.
     *  @param operation A string naming the unsupported token
     *  operation.
     *  @param firstToken The first token in the message.
     *  @param secondToken The second token in the message.
     *  @return A string error message.
     */
    public static String notSupportedMessage(String operation,
            Token firstToken, Token secondToken) {
        // We use this method to factor out a very common message
        return (operation + " operation not supported between "
                + firstToken.getClass().getName()
                + " '" + firstToken.toString()
                + "' and "
                + secondToken.getClass().getName()
                + " '" + secondToken.toString() + "'");
    }

    /** Returns a new Token representing the multiplicative identity.
     *  It should be overridden in subclasses.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() throws IllegalActionException {
        throw new IllegalActionException(
                "Multiplicative identity not supported on "
                + this.getClass().getName() + ".");
    }

    /** Return a new token computed as follows:
     *  <ul>
     *  <li> For positive <i>times</i> arguments, the result represents
     *  the product of this token multiplied by itself the number of
     *  times given by the argument.
     *  <li> For negative <i>times</i> arguments, the result
     *  represents the multiplicative inverse of the product of this
     *  token multiplied by itself the number of times given by the
     *  absolute value of the argument.
     *  <br> More succinctly: one().divide(pow(-times))
     *  <li> If the argument is zero, then the result is defined to be
     *  the result of applying the one() method to this token.
     *  <ul>
     *  The token type returned by this method is the same as
     *  the type of this token.  Note that the method is different
     *  from java.lang.Math.pow(), since it returns an integer given
     *  an integer token type, and is also well defined for matrix
     *  types.
     *  @param times The number of times to multiply.
     *  @return The power.
     *  @exception IllegalActionException If the token is not
     *  compatible for this operation.  Specifically, if the Token
     *  type does not support division (for example matrices) then
     *  using a negative <i>times</i> argument may throw an exception.
     */
    public ptolemy.data.Token pow(int times)
            throws IllegalActionException {
        if (times == 0) {
            // anything to the zero is one.
            return one();
        } else if (times < 0) {
            ptolemy.data.Token result = this;
            for ( int k = times; k < -1; k++ ) {
                result = result.multiply(this);
            }
            return one().divide(result);
        } else {
            ptolemy.data.Token result = this;
            for ( int k = 0; k < times - 1; k++ ) {
                result = result.multiply(this);
            }
            return result;
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token subtract(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("subtract", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("subtractReverse", this, leftArgument));
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  This method should be overridden by derived classes.
     *  In this base class, return the String "present" to indicate
     *  that an event is present.
     *  @return The String "present".
     */
    public String toString() {
        return "present";
    }

    /** Returns a new token representing the additive identity.
     *  It should be overridden in subclasses.
     *  @return A new Token containing the additive identity.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     */
    public Token zero() throws IllegalActionException {
        throw new IllegalActionException(
                "Additive identity not supported on "
                + this.getClass().getName() + ".");
    }
    
    /** Return the (exact) return type of the zero function above.
     *  The argument type is always returned
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type zeroReturnType(Type type) {
        return type;
    }
}
