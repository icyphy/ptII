/* A token that contains a boolean variable.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Green (wbwu@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// BooleanToken
/*
A token that contains a boolean variable. Arithmetic on booleans is
that of a two-element Galois field (modulo two arithmetic). Thus,
add() is logical xor, multiply() is logical and, zero() is false,
one() is true.
<p>
In order to reduce the number of instances of this object that are created,
it is highly recommended that the getInstance() method be used, instead of
the constructor that takes a boolean argument.

@author Neil Smyth, Yuhong Xiong, Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/

public class BooleanToken extends AbstractConvertibleToken
    implements BitwiseOperationToken {

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
     *  @exception IllegalActionException If the token could not
     *   be created with the given String.
     */
    public BooleanToken(String init) throws IllegalActionException {
        _value = init.toLowerCase().equals("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** True-valued token. */
    public static BooleanToken TRUE = new BooleanToken(true);

    /** False-valued token. */
    public static BooleanToken FALSE = new BooleanToken(false);

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the logical AND of the value
     *  of this token and the the value of the argument token.
     *  @param rightArgument The BooleanToken to OR with this one.
     *  @return A new BooleanToken containing the result.
     */
    public BooleanToken and(BooleanToken rightArgument) {
        if (_value && rightArgument.booleanValue()) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.
     *  @return The boolean AND.
     */
    public BitwiseOperationToken bitwiseAnd(Token rightArgument)
            throws IllegalActionException {
        if (!(rightArgument instanceof BooleanToken)) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("bitwiseAnd",
                            this, rightArgument));
        }
        return (BooleanToken)_multiply(rightArgument);
    }

    /** Returns a token representing the bitwise NOT of this token.
     *  @return The boolean negation.
     */
    public BitwiseOperationToken bitwiseNot() {
        return not();
    }

    /** Returns a token representing the bitwise OR of this token and
     *  the given token.
     *  @return The boolean OR.
     */
    public BitwiseOperationToken bitwiseOr(Token rightArgument)
            throws IllegalActionException {
        if (!(rightArgument instanceof BooleanToken)) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("bitwiseOr",
                            this, rightArgument));
        }
        boolean rightValue = ((BooleanToken)rightArgument).booleanValue();
        if (_value || rightValue) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /** Returns a token representing the bitwise XOR of this token and
     *  the given token.
     *  @return The boolean XOR.
     */
    public BitwiseOperationToken bitwiseXor(Token rightArgument)
            throws IllegalActionException {
        if (!(rightArgument instanceof BooleanToken)) {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("bitwiseXor",
                            this, rightArgument));
        }
        return (BooleanToken)_add(rightArgument);
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
    public static BooleanToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof BooleanToken) {
            return (BooleanToken)token;
        }

        int compare = TypeLattice.compare(BaseType.BOOLEAN, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "boolean"));
        }

        throw new IllegalActionException(
                notSupportedConversionMessage(token, "boolean"));
    }

    /** Return true if the argument's class is BooleanToken and it has the
     *  same values as this token.
     *  @param object An instance of Object.
     *  @return True if the argument is a BooleanToken with the
     *  same value.
     */
    public boolean equals(Object object) {
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
            return false;
        }

        if (((BooleanToken)object).booleanValue() == _value) {
            return true;
        }
        return false;
    }

    /** Return the instance of this class corresponding to the given
     *  boolean value.
     *  @return BooleanToken.TRUE if the argument is true, or
     *  BooleanToken.FALSE otherwise.
     */
    public static BooleanToken getInstance(boolean value) {
        if (value) {
            return BooleanToken.TRUE;
        } else {
            return BooleanToken.FALSE;
        }
    }

    /** Return the type of this token.
     *  @return BaseType.BOOLEAN
     */
    public Type getType() {
        return BaseType.BOOLEAN;
    }

    /** Return a hash code value for this token. This method returns 1
     *  if this token has value true, and 0 if this token has value
     *  false.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        if (_value) {
            return 1;
        }
        return 0;
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

    /** Return a new token whose value is the logical OR of the value
     *  of this token and the the value of the argument token.
     *  @param rightArgument The BooleanToken to OR with this one.
     *  @return A new BooleanToken containing the result.
     */
    public BooleanToken or(BooleanToken rightArgument) {
        if (_value || rightArgument.booleanValue()) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return The string "true" if this token represents true, or the
     *  string "false" if it represents false.
     */
    public String toString() {
        if (booleanValue()) {
            return "true";
        } else {
            return "false";
        }
    }

    /** Return a new token whose value is the logical XOR of the value
     *  of this token and the the value of the argument token.
     *  @param rightArgument The BooleanToken to XOR with this one.
     *  @return A new BooleanToken containing the result.
     */
    public BooleanToken xor(BooleanToken rightArgument) {
        if (_value ^ rightArgument.booleanValue()) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /** Returns a token representing the additive identity.
     *  @return FALSE.
     */
    public Token zero() {
        return FALSE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an BooleanToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new BooleanToken containing the result.
     */
    protected Token _add(Token rightArgument) {
        return this.or((BooleanToken)rightArgument);
    }

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is an BooleanToken.  For booleans,
     *  division is defined by multiplication (which is logical and).
     *  Thus, if <i>c</i> = <i>a</i>/<i>b</i> then <i>c</i> is defined
     *  so that <i>c</i><i>b</i> = <i>a</i>.  If <i>b</i> is
     *  <i>false</i> then this result is not well defined, so this
     *  method will throw an exception.  Specifically, if the argument
     *  is <i>true</i>, then this method returns this token.
     *  Otherwise it throws an exception.
     *  @param rightArgument The token to divide this token by
     *  @return A new BooleanToken containing the result.
     *  @exception IllegalActionException If the argument token is
     *  FALSE.
     */
    protected Token _divide(Token rightArgument)
            throws IllegalActionException {
        boolean denomValue = ((BooleanToken)rightArgument).booleanValue();
        if (denomValue) {
            return this;
        } else {
            throw new IllegalActionException("BooleanToken: division "
                    + "by false-valued token (analogous to division by "
                    + "zero).");
        }
    }

    /** Return a true-valued token if the first argument is close to this
     *  token, where in this class, "close" means "identical to."
     *  It is assumed that the type of the argument is BooleanToken.
     *  @param token The token to compare to this token.
     *  @return A token containing the result.
     */
    protected BooleanToken _isCloseTo(Token token, double epsilon) {
        return _isEqualTo(token);
    }

    /** Test for equality of the values of this token and the argument.
     *  It is assumed that the type of the argument is BooleanToken.
     *  @param token The token to compare to this token.
     *  @return A token containing the result.
     */
    protected BooleanToken _isEqualTo(Token token) {
        boolean argumentValue = ((BooleanToken)token).booleanValue();
        if (_value == argumentValue) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed
     *  that the type of the argument is BooleanToken.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result that is of the same
     *  class as this token.
     */
    protected Token _modulo(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("modulo", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is an BooleanToken.  For booleans,
     *  this corresponds to the logical AND.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new BooleanToken containing the result.
     */
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        return this.and((BooleanToken)rightArgument);
    }

    /** Subtraction is not supported in Boolean algebras.
     */
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("subtract", this, rightArgument));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _value;
}
