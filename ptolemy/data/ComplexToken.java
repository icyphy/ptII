/* A token that contains a Complex.

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
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// ComplexToken
/**
A token that contains a Complex number represented by a 64-bit
double-precision floating point real and imaginary parts.

@see ptolemy.data.Token
@see ptolemy.math.Complex
@author Yuhong Xiong, Neil Smyth, Christopher Hylands, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class ComplexToken extends ScalarToken {

    /** Construct a ComplexToken with Complex 0.0+0.0i
     */
    public ComplexToken() {
        _value = Complex.ZERO;
    }

    /** Construct a ComplexToken with the specified value.
     */
    public ComplexToken(Complex value) {
        _value = value;
    }

    /** Construct a ComplexToken from the specified string.
     *  @exception IllegalActionException If the string does not represent
     *   a parsable complex number.
     */
    public ComplexToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        Token token = (new ParseTreeEvaluator()).evaluateParseTree(tree);
        if (token instanceof ComplexToken) {
            _value = ((ComplexToken)token).complexValue();
        } else {
            throw new IllegalActionException("A ComplexToken cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value of this token as a Complex.
     *  @return The value of this token as a Complex
     */
    public Complex complexValue() {
        // Complex is immutable, so we can just return the value.
        return _value;
    }

    /** Convert the specified token into an instance of ComplexToken.
     *  This method does lossless conversion.  The units of the
     *  returned token will be the same as the units of the given
     *  token.
     *  If the argument is already an instance of ComplexToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below ComplexToken in the type hierarchy, it is converted to
     *  an instance of ComplexToken or one of the subclasses of
     *  ComplexToken and returned. If none of the above conditions are
     *  met, an exception is thrown.
     *  @param token The token to be converted to a ComplexToken.
     *  @return A ComplexToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static ComplexToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof ComplexToken) {
            return (ComplexToken)token;
        }

        int compare = TypeLattice.compare(BaseType.COMPLEX, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "complex"));
        }

        compare = TypeLattice.compare(BaseType.DOUBLE, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            DoubleToken doubleToken = DoubleToken.convert(token);
            ComplexToken result = new ComplexToken(doubleToken.complexValue());
            result._unitCategoryExponents =
                doubleToken._copyOfCategoryExponents();
            return result;
        }

        // The argument is below ComplexToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(
                notSupportedConversionMessage(token, "complex"));
    }

    /** Return true if the argument's class is IntToken and it has the
     *  same values as this token.
     *  @param object An instance of Object.
     *  @return True if the argument is a ComplexToken with the
     *  same value.
     */
    public boolean equals(Object object) {
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
            return false;
        }

        if (((ComplexToken)object).complexValue().equals(_value)) {
            return true;
        }
        return false;
    }

    /** Return the type of this token.
     *  @return BaseType.COMPLEX
     */
    public Type getType() {
        return BaseType.COMPLEX;
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the magnitude of the contained complex number.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        return (int)_value.magnitude();
    }

    /** Returns a new ComplexToken with value 1.0.
     *  @return A new ComplexToken with value 1.0.
     */
    public Token one() {
        return new ComplexToken(new Complex(1.0));
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A String formed using java.lang.Complex.toString().
     */
    public String toString() {
        return _value.toString();
    }

    /** Returns a new ComplexToken with value Complex.ZERO.
     *  @return A new ComplexToken with value Complex.ZERO.
     */
    public Token zero() {
        return new ComplexToken(Complex.ZERO);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.
     *  @return A DoubleToken.
     */
    protected ScalarToken _absolute() {
        DoubleToken result = new DoubleToken(_value.magnitude());
        return result;
    }

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an ComplexToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new ComplexToken containing the result.
     */
    protected ScalarToken _add(ScalarToken rightArgument) {
        Complex result =
            _value.add(((ComplexToken)rightArgument).complexValue());
        return new ComplexToken(result);
    }

    /** Throw an exception because bitwise AND is not supported.
     *  @exception IllegalActionException Always thrown.
     *  @return An exception.
     */
    protected ScalarToken _bitwiseAnd(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("bitwiseAnd", this, rightArgument));
    }

    /** Throw an exception because bitwise NOT is not supported.
     *  @exception IllegalActionException Always thrown.
     *  @return An exception.
     */
    protected ScalarToken _bitwiseNot()
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("bitwiseNot", this, this));
    }

    /** Throw an exception because bitwise OR is not supported.
     *  @exception IllegalActionException Always thrown.
     *  @return An exception.
     */
    protected ScalarToken _bitwiseOr(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("bitwiseOr", this, rightArgument));
    }

    /** Throw an exception because bitwise XOR is not supported.
     *  @exception IllegalActionException Always thrown.
     *  @return An exception.
     */
    protected ScalarToken _bitwiseXor(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("bitwiseXor", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an ComplexToken
     *  @param rightArgument The token to divide this token by.
     *  @return A new ComplexToken containing the result.
     */
    protected ScalarToken _divide(ScalarToken rightArgument)
            throws IllegalActionException {
        Complex result =
            _value.divide(((ComplexToken)rightArgument).complexValue());
        return new ComplexToken(result);
    }

    /** Test that the value of this rightArgument is close to the
     *  first argument, where "close" means that the distance between
     *  their values is less than or equal to the second argument. It
     *  is assumed that the type of the argument is ComplexToken.
     *
     *  @param rightArgument The rightArgument to compare to this
     *  rightArgument.
     *  @return A true-valued rightArgument if the first argument is
     *  close in value to this rightArgument.
     */
    protected BooleanToken _isCloseTo(
            ScalarToken rightArgument, double epsilon) {
        return BooleanToken.getInstance(
                complexValue().isCloseTo(((ComplexToken)rightArgument)
                        .complexValue(),
                        epsilon));
    }

    /** Throw an exception because complex values cannot be compared.
     *  @param rightArgument The token to compare to this token.
     *  @exception IllegalActionException Always thrown.
     *  @return An exception.
     */
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("isLessThan", this, rightArgument) +
                " because complex numbers cannot be compared.");
    }

    /** Throw an exception because the modulo operation does not
     *  make sense for complex values.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException Always thrown.
     *  @return An exception.
     */
    protected ScalarToken _modulo(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("modulo", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is an ComplexToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new ComplexToken containing the result.
     */
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        Complex result =
            _value.multiply(((ComplexToken)rightArgument).complexValue());
        return new ComplexToken(result);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is an ComplexToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new ComplexToken containing the result.
     */
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        Complex result =
            _value.subtract(((ComplexToken)rightArgument).complexValue());
        return new ComplexToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Complex _value = null;
}
