/* A token that contains an integer number.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// IntToken
/**
A token that contains an integer number.

@author Neil Smyth, Yuhong Xiong, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class IntToken extends ScalarToken {

    /** Construct a token with integer 0.
     */
    public IntToken() {
        _value = 0;
    }

    /** Construct a token with the specified value.
     */
    public IntToken(int value) {
        _value = value;
    }

    /** Construct an IntToken from the specified string.
     *  @exception IllegalActionException If the token could not
     *   be created with the given String.
     */
    public IntToken(String init) throws IllegalActionException {
        try {
            _value = (Integer.valueOf(init)).intValue();
        } catch (NumberFormatException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value of this token as a Complex. The real part
     *  of the Complex is the value of this token, the imaginary part
     *  is set to 0.
     *  @return A Complex.
     */
    public Complex complexValue() {
        return new Complex((double)_value);
    }

    /** Convert the specified token into an instance of IntToken.
     *  This method does lossless conversion.  If the argument is
     *  already an instance of IntToken, it is returned without any
     *  change. Otherwise, if the argument is below IntToken in the
     *  type hierarchy, it is converted to an instance of IntToken or
     *  one of the subclasses of IntToken and returned. If none of the
     *  above condition is met, an exception is thrown.
     *  @param token The token to be converted to a IntToken.
     *  @return A IntToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static IntToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof IntToken) {
            return (IntToken)token;
        }

        int compare = TypeLattice.compare(BaseType.INT, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "int"));
        }

        compare = TypeLattice.compare(BaseType.BYTE, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            ByteToken byteToken = ByteToken.convert(token);
            return new IntToken(byteToken.intValue());
        }

        // The argument is below ByteToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(
                notSupportedConversionMessage(token, "int"));
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    public double doubleValue() {
        return (double)_value;
    }

    /** Return true if the argument is an instance of IntToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of IntToken with the
     *  same value.
     */
    public boolean equals(Object object) {
        // This test rules out subclasses.
        if (object.getClass() != IntToken.class) {
            return false;
        }

        if (((IntToken)object).intValue() == _value) {
            return true;
        }
        return false;
    }

    /** Return the type of this token.
     *  @return BaseType.INT
     */
    public Type getType() {
        return BaseType.INT;
    }

    /** Return a hash code value for this token. This method just returns the
     *  contained integer.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        return _value;
    }

    /** Return the value in the token as an int.
     *  @return The int value contained in this token.
     */
    public int intValue() {
        return _value;
    }

    /** Return the value in the token as a long.
     *  @return The int value contained in this token as a long.
     */
    public long longValue() {
        return (long)_value;
    }

    /** Returns a new IntToken with value 1.
     *  @return A new IntToken with value 1.
     */
    public Token one() {
        return new IntToken(1);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  If this token has a unit, the return string also includes a unit
     *  string produced by the unitsString() method in the super class.
     *  @return A String representing the int value and the units (if
     *   any) of this token.
     *  @see ptolemy.data.ScalarToken#unitsString
     */
    public String toString() {
        String unitString = "";
        if ( !_isUnitless()) {
            unitString = " * " + unitsString();
        }
        return Integer.toString(_value) + unitString;
    }

    /** Returns a new IntToken with value 0.
     *  @return A new IntToken with value 0.
     */
    public Token zero() {
        return new IntToken(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.
     *  @return An IntToken.
     */
    protected ScalarToken _absolute() {
        IntToken result;
        if (_value >= 0) {
            result = this;
        } else {
            result = new IntToken(-_value);
        }
        return result;
    }

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an IntToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new IntToken containing the result.
     */
    protected ScalarToken _add(ScalarToken rightArgument) {
        int sum = _value + ((IntToken)rightArgument).intValue();
        return new IntToken(sum);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an IntToken
     *  @param rightArgument The token to divide this token by.
     *  @return A new IntToken containing the result.
     */
    protected ScalarToken _divide(ScalarToken rightArgument) {
        int quotient = _value / ((IntToken)rightArgument).intValue();
        return new IntToken(quotient);
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  IntToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isCloseTo(
            ScalarToken rightArgument, double epsilon)
            throws IllegalActionException {
        return _isEqualTo(rightArgument);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  IntToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isEqualTo(ScalarToken rightArgument)
            throws IllegalActionException {
        IntToken convertedArgument = (IntToken)rightArgument;
        return BooleanToken.getInstance(
                _value == convertedArgument.intValue());
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is IntToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        IntToken convertedArgument = (IntToken)rightArgument;
        return BooleanToken.getInstance(
                _value < convertedArgument.intValue());
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is an IntToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new IntToken containing the result.
     */
    protected ScalarToken _modulo(ScalarToken rightArgument) {
        int remainder = _value % ((IntToken)rightArgument).intValue();
        return new IntToken(remainder);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is an IntToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new IntToken containing the result.
     */
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        int product = _value * ((IntToken)rightArgument).intValue();
        return new IntToken(product);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is an IntToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new IntToken containing the result.
     */
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        int difference = _value - ((IntToken)rightArgument).intValue();
        return new IntToken(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _value;
}
