/* Base class for data capsules.

 Copyright (c) 1997- The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// Token
/**
Base class for data capsules. Tokens are immutable.
<p>
Operator overloading between tokens is supported with methods
for each operator. The operators that are overloaded
are +, -, *, / == and %. These methods carry out the operation
if it can be performed in a lossless manner.
Not all derived classes are required to implement these methods,
so the default implementation in this base class triggers an exception.
<p>
This base class can be used to represent a pure event, i.e., to
indicate that an event is present. For this purpose, the stringValue()
method returns the String "present".
<p>

@author Neil Smyth, Yuhong Xiong
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
        String str = "add method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
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
        String str = "addReverse method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
    }

    /** Convert the specified token to an instance of this class.
     *  Since any token is an instance of this class, no conversion is
     *  necessary. However, this implementation does not return the
     *  specified token, but returns a brand new instance of Token.
     *  This is for reducing the chance of undetected error caused by
     *  accidental use of this method by the derived class (when the
     *  derived class fails to override this method).
     *  @param token A Token to be converted, ignored in this implementation.
     *  @return A new instance of Token.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {
	return new Token();
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
        String str = "divide method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
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
        String str = "divideReverse method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
    }

    /** Test for equality of the values of this Token and the argument Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for equality testing.
     *  @param token The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A  new BooleanToken which contains the result of the test.
     */
    public BooleanToken equals(Token token) throws IllegalActionException {
        String str = "equals method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
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
        String str = "modulo method not supported on " +
        	this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
    }

    /** Return a new Token whose value is the value of the argument token
     *  modulo the value of this token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for modulo.
     *  @param leftArg The token whose value we modulo on.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token moduloReverse(Token leftArg)
	     throws IllegalActionException {
        String str = "moduloReverse method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
    }

    /** Return a new Token whose value is the value of this Token
     *  multiplied with the value of the argument Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for multiply.
     *  @param rightFactor The token whose value we multiply the value of this
     *   Token with.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token multiply(Token rightFactor) throws  IllegalActionException {
        String str = "multiply method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
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
        String str = "multiplyReverse method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
    }

    /** Returns a new Token representing the multiplicative identity.
     *  It should be overridden in subclasses.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() throws IllegalActionException {
        String str = "Multiplicative identity not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
    }

    /** Return the String "present" to indicate that an event is present.
     *  This method should be overridden in the derived classes to
     *  return the data in the token as a String.
     *  @exception IllegalActionException Not thrown in this base class,
     *  @return The String "present".
     */
    public String stringValue() throws IllegalActionException {
        return "present";
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
        String str = "subtract method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
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
        String str = "subtractReverse method not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
    }

    /** Return a description of the token as a string.
     *  In this base class, we return the fully qualified class name.
     *  @return A description of this object as a String.
     */
    public String toString() {
        return getClass().getName();
    }

    /** Returns a new token representing the additive identity.
     *  It should be overridden in subclasses.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the additive identity.
     */
    public Token zero() throws IllegalActionException {
        String str = "Additive identity not supported on ";
        str = str + this.getClass().getName() + " objects.";
        throw new IllegalActionException(str);
    }
}

