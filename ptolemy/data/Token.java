/* Abstract base class for data capsules.

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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating none

*/

//////////////////////////////////////////////////////////////////////////
//// Token
/**
Abstract base class for data capsules. Tokens are immutable.
FIXME: This class declares that tokens are cloneable and promotes
the protected clone() method of the Object base class to public.
<p>
Operator overloading between tokens is supported with methods
for each operator. The operators that are overloaded
are +, -, *, / == and %. These methods carry out the operation
if it can be performed in a lossless manner.
Not all derived classes are required to implement these methods,
so the default implementation in this base class triggers an exception.
<p>

@author Neil Smyth, Edward A. Lee
@version $Id$

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;

public abstract class Token implements Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token
     *  and the argument. It should be overridden in derived
     *  classes to provide type specific actions for add.
     *  @param a The token whose value we add to the value of this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token add(Token a) throws IllegalActionException {
        String str = "Add method not supported on ";
        str = str + this.getClass().getName() + " objects";
        throw new IllegalActionException(str);
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. It should be overridden in derived classes
     *  to provide type specific actions for add.
     *  @param a The token containing the value to which we add the
     *   value of this token to get the value of the new token.
     *  @exception IllegalActionException Thrown if this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token addR(Token a) throws IllegalActionException {
        String str = "Addr method not supported on ";
        str = str + this.getClass().getName() + " objects";
        throw new IllegalActionException(str);
    }

    /** Promote the clone method of the base class Object to public.
     *  @see java.lang.Object#clone()
     *  @exception CloneNotSupportedException May be thrown by derived classes.
     *  @return The cloned Token.
     */
    public Object clone()
            throws CloneNotSupportedException {
        Token result = (Token)super.clone();
        return result;

    }

    /** Convert the specified token into an instance of this class.
     *  Since all the token classes are drived from this base class,
     *  no conversion is needed. This method just returns the specified
     *  token. The derived classes should override this method to
     *  convert a token type further down the type hierarchy to the
     *  type of itself.
     *  @param token The Token to be converted to an instance of this class.
     *  @return A new Token of the same type as this token.
     *  @exception IllegalActionException not thrown in this base class.
     */
    public Token convert(Token token) throws IllegalActionException {
	return token;
    }

    /** Return a new Token whose value is the value of this token
     *  divided by the value of the argument token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for divide.
     *  @param divisor The Token whose value we divide the value of this
     *   Token by.
     *  @exception IllegalActionException Thrown if this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token divide(Token divisor) throws  IllegalActionException {
        String str = "Divide method not supported on ";
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }

    /** Return a new Token whose value is the value of the argument token
     *  divided by the value of this token.
     *  It  should be overridden in derived classes to provide type specific
     *  actions for divide.
     *  @param dividend The Token whose value we divide by the value of
     *   this Token.
     *  @exception IllegalActionException Thrown if this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token divideR(Token dividend) throws  IllegalActionException {
        String str = "DivideR method not supported on ";
        str = str + this.getClass().getName() + "objects";
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
        String str = "Equality method not supported on ";
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }

    /** This method should be overridden where appropriate in subclasses.
     *  @return Whether this Token is an array or not.
     */
    public boolean isArray() {
        return false;
    }

    /** Return a new Token whose value is the value of this token
     *  modulo the value of the argument token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for modulo.
     *  @param token The token whose value we do modulo with.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token modulo(Token token) throws  IllegalActionException {
        String str = "Modulo method not supported on ";
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }

    /** Return a new Token whose value is the value of the argument token
     *  modulo the value of this token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for modulo.
     *  @param token The token whose value we modulo on.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token moduloR(Token token) throws IllegalActionException {
        String str = "ModuloR method not supported on ";
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }

    /** Return a new Token whose value is the value of this Token
     *  multiplied with the value of the argument Token.
     *  @param rightFactor The token whose value we multiply the value of this
     *   Token with.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token multiply(Token rightFactor) throws  IllegalActionException {
        String str = "Multiply method not supported on ";
        str = str + this.getClass().getName() + "objects";
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
    public Token multiplyR(Token leftFactor) throws  IllegalActionException {
        String str = "MultiplyR method not supported on ";
        str = str + this.getClass().getName() + "objects";
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
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }

    /** Return the value of the Token as a String.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return The value of the token as a String.
     */
    public String stringValue() throws IllegalActionException {
        Class myclass = getClass();
        throw new IllegalActionException("Tokens of class "
                + myclass.getName() + " cannot be returned as a String");
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
        String str = "Subtract method not supported on ";
        str = str + this.getClass().getName() + "objects";
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
    public Token subtractR(Token leftArg) throws  IllegalActionException {
        String str = "Subtract method not supported on ";
        str = str + this.getClass().getName() + "objects";
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
        str = str + this.getClass().getName() + "objects";
        throw new IllegalActionException(str);
    }
}








