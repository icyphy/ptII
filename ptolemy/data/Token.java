/* Base class for data capsules.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// Token
/**
Token is the base class for data capsules.
Tokens are immutable, meaning that their
value cannot change after construction.
They have a set of polymorphic methods providing a set of basic
arithmetic and logical operations.
Not all derived classes are required to implement these methods,
so the default implementation in this base class throws an exception.
<p>
Instances of this
base class can be used to represent pure events, i.e., to
indicate that an event is present. To support this use, the toString()
method returns the String "present".
<p>

@author Neil Smyth, Yuhong Xiong, Edward A. Lee
@version $Id$

*/
public class Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.
     *  It should be overridden in derived
     *  classes to provide type specific actions for add.
     *  @param rightArg The token whose value we add to the value of
     *   this token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token add(Token rightArg) throws IllegalActionException {
        throw new IllegalActionException("Addition not supported between "
        + this.getClass().getName() + " and "
        + rightArg.getClass().getName() + ".");
    }

    /** Return a new token whose value is the value of this
     *  Token added to the value of the argument Token.
     *  It should be overridden in derived classes
     *  to provide type specific actions for add.
     *  @param leftArg The token containing the value to which we add the
     *   value of this token to get the value of the new token.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token addReverse(Token leftArg) throws IllegalActionException {
        throw new IllegalActionException("Addition not supported between "
        + leftArg.getClass().getName() + " and "
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

    /** Return the type of this token.
     *  @return BaseType.GENERAL
     */
    public Type getType() {
	return BaseType.GENERAL;
    }

    /** Test for equality of the values of this Token and the argument Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for equality testing.
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
     *  @param rightArg The token whose value we do modulo with.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token modulo(Token rightArg) throws  IllegalActionException {
        throw new IllegalActionException("Modulo operation not supported: "
        + this.getClass().getName() + " modulo "
        + rightArg.getClass().getName() + ".");
    }

    /** Return a new Token whose value is the value of the argument token
     *  modulo the value of this token.
     *  This should be overridden in derived classes to provide type specific
     *  actions for modulo.
     *  @param leftArg The token whose value we modulo on.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token moduloReverse(Token leftArg)
            throws IllegalActionException {
        throw new IllegalActionException("Modulo operation not supported on "
        + leftArg.getClass().getName() + " objects modulo "
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
     *  multiplied with the value of this Token.
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

    /** Return a description of the token as a string.
     *  In this base class, we return the fully qualified class name.
     *  @return A description of this object as a String.
     *  @deprecated Use toString() instead.
     */
    public String stringValue() {
        return toString();
    }

    /** Return a new Token whose value is the value of the argument Token
     *  subtracted from the value of this Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for subtract.
     *  @param rightArg The token whose value we subtract from this Token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token subtract(Token rightArg) throws  IllegalActionException {
        throw new IllegalActionException("Subtraction not supported on "
        + this.getClass().getName() + " minus "
        + rightArg.getClass().getName() + ".");
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for subtract.
     *  @param leftArg The token to subtract the value of this Token from.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token subtractReverse(Token leftArg)
	    throws  IllegalActionException {
        throw new IllegalActionException("Subtraction not supported on "
        + leftArg.getClass().getName() + " minus "
        + this.getClass().getName() + ".");
    }

    /** Return the String "present" to indicate that an event is present.
     *  This method should be overridden in the derived classes to
     *  return the data in the token as a String.
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
}
