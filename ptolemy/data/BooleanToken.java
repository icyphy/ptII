/* A token that contains a boolean variable.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
import ptolemy.graph.CPO;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// BooleanToken
/**
 * A token that contains a boolean variable. Arithmetic on
 * booleans is that of a two-element Galois field (modulo two
 * arithmetic). Thus, add() is logical xor, multiply() is logical
 * and, zero() is false, one() is true.
 *
 * @author Neil Smyth, Yuhong Xiong, Edward A. Lee
 * @version $Id$
*/

public class BooleanToken extends Token {

    /** Construct a token with value false
     */
    public BooleanToken() {
	_value = false;
    }

    /** Construct a token with the specified value.
     */
    public BooleanToken(boolean b) {
	_value = b;
    }

    /** Construct a token with the specified string.
     *  @exception IllegalArgumentException If the token could not
     *   be created with the given String.
     */
    public BooleanToken(String init) throws IllegalArgumentException {
        _value = (Boolean.valueOf(init)).booleanValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** True-valued token. */
    public static BooleanToken TRUE = new BooleanToken(true);

    /** False-valued token. */
    public static BooleanToken FALSE = new BooleanToken(false);

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this token plus the argument.
     *  A BooleanToken can be added to another
     *  BooleanToken or to a StringToken. For booleans, addition
     *  is logical exclusive or.
     *  @param rightArg The token to add to.
     *  @exception IllegalActionException If the passed token
     *   is not a StringToken or a BooleanToken.
     *  @return A BooleanToken containing the logical exclusive or.
     */
    public Token add(ptolemy.data.Token rightArg)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArg);
        if (typeInfo == CPO.LOWER) {
            return rightArg.addReverse(this);
        } else if (rightArg instanceof BooleanToken) {
            boolean rightValue = ((BooleanToken)rightArg).booleanValue();
            if ((_value || rightValue) && !(_value && rightValue)) {
                return TRUE;
            } else {
                return FALSE;
            }
        } else {
            return super.add(rightArg);
        }
    }

    /** Return the argument plus this token.  The argument is assumed
     *  to be convertible to a BooleanToken.  For booleans, addition
     *  is logical exclusive or.
     *  @param leftArg The token to add to this token.
     *  @exception IllegalActionException If the argument cannot be
     *   converted to a BooleanToken.
     *  @return A BooleanToken containing the logical exclusive or.
     */
    public Token addReverse(Token leftArg) throws IllegalActionException {
        BooleanToken converted = (BooleanToken)this.convert(leftArg);
        boolean leftValue = converted.booleanValue();
        if ((_value || leftValue) && !(_value && leftValue)) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /** Return the value as a boolean.
     *  @return The value.
     */
    public boolean booleanValue() {
        return _value;
    }

    /** Convert the specified token into an instance of BooleanToken.
     *  This method does lossless conversion, which in the case of
     *  booleans, means that the argument can only be already
     *  an instance of BooleanToken.  It is returned unchanged.
     *  @param token The token to be converted to a BooleanToken.
     *  @return A BooleanToken.
     *  @exception IllegalActionException If the argument is not
     *   a BooleanToken.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {
	if (token instanceof BooleanToken) {
	    return token;
	} else {
            throw new IllegalActionException("cannot convert from token " +
                    "type: " + token.getClass().getName() + " to a BooleanToken");
        }
    }

    /** Return this token divided by the argument.  For booleans, division
     *  is defined by multiplication (which is logical and).  Thus, if
     *  <i>c</i> = <i>a</i>/<i>b</i> then <i>c</i> is defined so that
     *  <i>c</i><i>b</i> = <i>a</i>.  If <i>b</i> is <i>false</i> then
     *  this result is not well defined, so this method will throw
     *  an exception.  Specifically, if the argument is <i>true</i>,
     *  then this method returns this token.  Otherwise it throws an
     *  exception.
     *  @param rightFactor The token to divide by.
     *  @exception IllegalActionException If the passed token is
     *   not a BooleanToken.
     *  @exception IllegalArgumentException If the argument has value
     *   <i>false</i>.  Note that this is a run-time exception, so it
     *   need not be declared explicitly.
     *  @return The result of division.
     */
    public Token divide(Token denominator) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, denominator);
        if (typeInfo == CPO.LOWER) {
            return denominator.divideReverse(this);
        } else if (denominator instanceof BooleanToken) {
            boolean denomValue = ((BooleanToken)denominator).booleanValue();
            if (denomValue) {
                return this;
            } else {
                throw new IllegalArgumentException("BooleanToken: division "
                        + "by false-valued token (analogous to division by zero).");
            }
        } else {
            return super.multiply(denominator);
        }
    }

    /** Return the argument divided by this token.  For booleans, division
     *  is defined by multiplication (which is logical and).  Thus, if
     *  <i>c</i> = <i>a</i>/<i>b</i> then <i>c</i> is defined so that
     *  <i>c</i><i>b</i> = <i>a</i>.  If <i>b</i> is <i>false</i> then
     *  this result is not well defined, so this method will throw
     *  an exception. Specifically, if this token is <i>true</i>,
     *  then this method returns the argument.  Otherwise it throws an
     *  exception.
     *  The argument is assumed to be convertible to a BooleanToken.
     *  @param rightFactor The token to divide into.
     *  @exception IllegalActionException If the passed token is
     *   not convertible to a BooleanToken.
     *  @exception IllegalArgumentException If this token has value
     *   <i>false</i>.  Note that this is a run-time exception, so it
     *   need not be declared explicitly.
     *  @return A new token containing the result.
     */
    public Token divideReverse(Token numerator) throws IllegalActionException {
        BooleanToken converted = (BooleanToken)this.convert(numerator);
        if (_value) {
            return converted;
        } else {
            throw new IllegalArgumentException("BooleanToken: division "
                    + "by false-valued token (analogous to division by zero).");
        }
    }

    /** Return the type of this token.
     *  @return BaseType.BOOLEAN
     */
    public Type getType() {
	return BaseType.BOOLEAN;
    }

    /** Return TRUE if the argument has the same boolean value as this token.
     *  Otherwise, return FALSE;
     *  @param token The token to compare.
     *  @exception IllegalActionException If the argument
     *   is not a BooleanToken.
     *  @return A BooleanToken indicating whether this token has the same
     *   value as the argument.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        if ( !(token instanceof BooleanToken)) {
            throw new IllegalActionException("Cannot compare a BooleanToken"
                    + " with a non-BooleanToken");
        }
        boolean arg = ((BooleanToken)token).booleanValue();
        if ((_value && arg) || !(_value || arg)) {
            return TRUE;
        }
        return FALSE;
    }

    /** Return the product of this token and the argument.
     *  For booleans, multiplication is logical and.
     *  @param rightFactor The token to multiply by this token.
     *  @exception IllegalActionException If the argument is
     *   not a BooleanToken.
     *  @return A BooleanToken with the product.
     */
    public Token multiply(Token rightFactor) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightFactor);
        if (typeInfo == CPO.LOWER) {
            return rightFactor.multiplyReverse(this);
        } else if (rightFactor instanceof BooleanToken) {
            boolean rightValue = ((BooleanToken)rightFactor).booleanValue();
            if (rightValue && _value) {
                return TRUE;
            } else {
                return FALSE;
            }
        } else {
            return super.multiply(rightFactor);
        }
    }

    /** Return the product of this token and the argument.
     *  For booleans, multiplication is logical and.
     *  The argument is assumed to be convertible to a BooleanToken.
     *  @param leftFactor The token to multiply by this token.
     *  @exception IllegalActionException If the argument is not
     *   convertible to a BooleanToken.
     *  @return A BooleanToken containing the product.
     */
    public Token multiplyReverse(Token leftArg) throws IllegalActionException {
        BooleanToken converted = (BooleanToken)this.convert(leftArg);
        boolean leftValue = converted.booleanValue();
        if (leftValue && _value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /** Return a new BooleanToken with the logical not of the value
     *  stored in this token.
     *  @return The logical converse of this token.
     */
    public BooleanToken not() {
        if (booleanValue()) {
            return FALSE;
        } else {
            return TRUE;
        }
    }

    /** Returns a token representing the multiplicative identity.
     *  @return TRUE.
     */
    public Token one() {
        return TRUE;
    }

    /** Return this token minus the argument.  For booleans, subtraction
     *  is defined by addition (which is logical exclusive or).  Thus, if
     *  <i>c</i> = <i>a</i> - <i>b</i> then <i>c</i> is defined so that
     *  <i>c</i> + <i>b</i> = <i>a</i>. Adding <i>b</i> to both sides
     *  and observing that <i>b</i> + <i>b</i> = <i>false</i> and
     *  <i>c</i> + <i>false</i> = <i>c</i>, we note that subtraction
     *  is identical to addition.
     *  @param rightArg The token to subtract from this token.
     *  @exception IllegalActionException If the passed token is
     *   not a boolean.
     *  @return A new BooleanToken containing the difference.
     */
    public Token subtract(Token rightArg) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, rightArg);
        if (typeInfo == CPO.LOWER) {
            return rightArg.subtractReverse(this);
        } else if (rightArg instanceof BooleanToken) {
            return add(rightArg);
        } else if (typeInfo == CPO.HIGHER){
            BooleanToken converted = (BooleanToken)this.convert(rightArg);
            return add(converted);
        } else {
            return super.subtract(rightArg);
        }
    }

    /** Return the argument minus this token.  The argument is assumed
     *  to be convertible to a BooleanToken.
     *  @param leftArg The token to subtract this token from.
     *  @exception IllegalActionException If the argument cannot be
     *   converted to a BooleanToken.
     *  @return A new BooleanToken containing the difference.
     */
    public Token subtractReverse(Token leftArg) throws IllegalActionException {
        return addReverse(leftArg);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A String formed using java.lang.Boolean.toString().
     */
    public String toString() {
        return (new Boolean(_value)).toString();
    }

    /** Returns a token representing the additive identity.
     *  @return FALSE.
     */
    public Token zero() {
        return FALSE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _value;
}
