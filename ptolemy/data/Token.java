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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu, nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;

import java.io.Serializable;

//////////////////////////////////////////////////////////////////////////
//// Token
/**
Token is the base class for data capsules.  Tokens are immutable,
meaning that their value cannot change after construction.  They have
a set of polymorphic methods providing a set of basic arithmetic and
logical operations.  Not all derived classes are required to implement
these methods, so the default implementation in this base class throws
an exception.

<p> Instances of this base class can be used to represent pure events,
i.e., to indicate that an event is present. To support this use, the
toString() method returns the String "present".


@author Neil Smyth, Yuhong Xiong, Edward A. Lee, Christopher Hylands
@version $Id$

*/
public class Token implements Serializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.
     *  It should be overridden in derived
     *  classes to provide type specific actions for add.
     *  @param rightArgument The token whose value we add to the value of
     *   this token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token add(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException("Addition not supported between "
                + this.getClass().getName() + " and "
                + rightArgument.getClass().getName() + ".");
    }

    /** Return a new token whose value is the value of this
     *  Token added to the value of the argument Token.
     *  It should be overridden in derived classes
     *  to provide type specific actions for add.
     *  @param leftArgument The token containing the value to which we add the
     *   value of this token to get the value of the new token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token addReverse(Token leftArgument) throws IllegalActionException {
        throw new IllegalActionException("Addition not supported between "
                + leftArgument.getClass().getName() + " and "
                + this.getClass().getName() + ".");
    }

    /** Convert the specified token to an instance of this class, if it
     *  is not already such an instance.
     *  Since any token is an instance of this class, no conversion is
     *  necessary, so this base class method simply returns the argument.
     *  Derived classes <i>must</i> override this method, or very subtle
     *  errors will result.  Notice that since this is a static method,
     *  Java does not do late binding.  That means that to convert a
     *  token <code>t</code> to an IntToken, say, you call
     *  <code>x.convert(t)</code>, where <code>x</code> is a reference
     *  of type IntToken.  It cannot be a reference of type
     *  Token, or this implementation of the method, not the one in
     *  IntToken will be called.
     *  @param token A Token to be converted.
     *  @return The argument.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public static Token convert(Token token) throws IllegalActionException {
        return token;
    }

    /** Return a new Token whose value is the value of this token
     *  divided by the value of the argument token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for divide.
     *  @param divisor The Token whose value we divide the value of this
     *   Token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token divide(Token divisor) throws  IllegalActionException {
        throw new IllegalActionException("Division not supported for "
                + this.getClass().getName() + " divided by "
                + divisor.getClass().getName() + ".");
    }

    /** Return a new Token whose value is the value of the argument token
     *  divided by the value of this token.
     *  It  should be overridden in derived classes to provide type specific
     *  actions for divide.
     *  @param dividend The Token whose value we divide by the value of
     *   this Token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token divideReverse(Token dividend)
            throws  IllegalActionException {
        throw new IllegalActionException("Division not supported for "
                + dividend.getClass().getName() + " divided by "
                + this.getClass().getName() + ".");
    }

    /** Override the base class method to check whether the value of this
     *  token is equal to that of the argument.
     *  Since this base token class does not have any state, this method
     *  returns true if the argument is an instance of Token, but not an
     *  instance of a subclass of Token or any other classes.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of Token, but not an
     *   instance of a subclass of Token or any other classes.
     */
    public boolean equals(Object object) {
	if (object.getClass() == Token.class) {
	    return true;
	}
	return false;
    }

    /** Return the type of this token.
     *  @return BaseType.GENERAL
     */
    public Type getType() {
        return BaseType.GENERAL;
    }

    /** Return a hash code value for this token. Since the equals() method
     *  in this base Token class returns true for all instances of Token,
     *  all instances of Token must have the same hash code. To achieve this,
     *  this method simply returns the value 0.
     *  @return The integer 0.
     */
    public int hashCode() {
	return 0;
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
     *   value and units of this token are close to those of the argument
     *   token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token) throws IllegalActionException{
	return isCloseTo(token, 0.0);
    }

    /** Test that the value of this Token is close to the argument
     *  Token.  In this base class, we call isEqualTo() and the
     *  epsilon argument is ignored.  This method should be overridden
     *  in derived classes such as DoubleToken and ComplexToken to
     *  provide type specific actions for equality testing using the
     *  epsilon argument
     *
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.  In this base class, the epsilon argument is
     *  ignored.
     *  @return a boolean token that contains the value true if the
     *   value and units of this token are close to those of the argument
     *   token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        // Note that if we had absolute(), subtraction() and islessThan()
        // we could perhaps define this method for all tokens.  However,
        // Precise classes like IntToken not bother doing the absolute(),
        // subtraction(), and isLessThan() method calls and should go
        // straight to isEqualTo().  Also, these methods might introduce
        // exceptions because of type conversion issues.
        try {
            return isEqualTo(token);
        } catch (IllegalActionException ex) {
            // Catch any errors and rethrow them with a message
            // that includes the "closeness" instead of equality.
            // For example, if we see if a String and Double are close
            // then if we don't catch and rethrow the exception, the
            // message will say something about "equality"
            // instead of "closeness".
            throw new IllegalActionException(null, null, ex,
                    _notSupportedMessage("closeness", this, token));
        }
    }

    /** Test for equality of the values of this Token and the argument Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for equality testing.
     *
     *  @see #isCloseTo
     *  @param token The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        throw new IllegalActionException("Equality test not supported between "
                + this.getClass().getName() + " and "
                + token.getClass().getName() + ".");
    }

    /** Return a new Token whose value is the value of this token
     *  modulo the value of the argument token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for modulo.
     *  @param rightArgument The token whose value we do modulo with.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token modulo(Token rightArgument) throws  IllegalActionException {
        throw new IllegalActionException("Modulo operation not supported: "
                + this.getClass().getName() + " modulo "
                + rightArgument.getClass().getName() + ".");
    }

    /** Return a new Token whose value is the value of the argument token
     *  modulo the value of this token.
     *  This should be overridden in derived classes to provide type specific
     *  actions for modulo.
     *  @param leftArgument The token whose value we modulo on.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException("Modulo operation not supported on "
                + leftArgument.getClass().getName() + " objects modulo "
                + this.getClass().getName() + " objects.");
    }

    /** Return a new Token whose value is the value of this Token
     *  multiplied by the value of the argument Token.
     *  This should be overridden in derived classes to provide type specific
     *  actions for multiply.
     *  @param rightFactor The token whose value we multiply the value of this
     *   Token with.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token multiply(Token rightFactor) throws  IllegalActionException {
        throw new IllegalActionException("Multiplication not supported on "
                + this.getClass().getName() + " by "
                + rightFactor.getClass().getName() + ".");
    }

    /** Return a new Token whose value is the value of the argument Token
     *  multiplied by the value of this Token.
     *  It  should be overridden in derived classes to provide type specific
     *  actions for multiply.
     *  @param leftFactor The token whose value we multiply the value of this
     *   Token with.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token multiplyReverse(Token leftFactor)
            throws  IllegalActionException {
        throw new IllegalActionException("Multiplication not supported on "
                + leftFactor.getClass().getName() + " by "
                + this.getClass().getName() + ".");
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

    /** Return a new Token whose value is the value of the argument Token
     *  subtracted from the value of this Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for subtract.
     *  @param rightArgument The token whose value we subtract from this Token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token subtract(Token rightArgument) throws  IllegalActionException {
        throw new IllegalActionException("Subtraction not supported on "
                + this.getClass().getName() + " minus "
                + rightArgument.getClass().getName() + ".");
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for subtract.
     *  @param leftArgument The token to subtract the value of this Token from.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token subtractReverse(Token leftArgument)
            throws  IllegalActionException {
        throw new IllegalActionException("Subtraction not supported on "
                + leftArgument.getClass().getName() + " minus "
                + this.getClass().getName() + ".");
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
                "Token.zero: Additive identity not supported on "
                + this.getClass().getName() + ".");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a string with an error message that states that
     *  operation is not supported between two tokens.
     *  @param operation A string naming the unsupported token
     *  @param firstToken The first token in the message.
     *  @param secondToken The first token in the message.
     *  @return A string error message.
     */
    protected String _notSupportedMessage(String operation,
            Token firstToken, Token secondToken) {
        // We use this method to factor out a very common message
        return (operation + " method not supported between "
                + firstToken.getClass().getName()
                + " '" + firstToken.toString()
                + "' and "
                + secondToken.getClass().getName()
                + " '" + secondToken.toString() + "'");
    }
}
