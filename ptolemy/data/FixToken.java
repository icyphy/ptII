/** A token that contains a FixPoint number.

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

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCL5AIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (kienhuis@eecs.berkeley.edu)
@AcceptedRating Yellow (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.FixPoint;
import ptolemy.math.Quantizer;
import ptolemy.math.Precision;
import ptolemy.graph.CPO;
import ptolemy.data.type.*;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;

//////////////////////////////////////////////////////////////////////////
//// FixToken
/**
A token that contains an instance of FixPoint.

@author Bart Kienhuis
@contributor Edward A. Lee
@see ptolemy.data.Token
@see ptolemy.math.FixPoint
@see ptolemy.math.Precision
@see ptolemy.math.Quantizer
@version $Id$
@since Ptolemy II 0.4
*/

public class FixToken extends ScalarToken {

    /** Construct a FixToken with the supplied FixPoint value.
     *  @param value A FixPoint value.
     */
    public FixToken(FixPoint value) {
        _value = value;
    }

    // FIXME: The constructors should throw IllegalActionException instead of
    // IllegalArgumentException. But since the FixPointFunctions class in the
    // expression package does not catch IllegalActionException, leave
    // IllegalArgumentException for now.

    /** Construct a FixToken representing the specified value with the
     *  specified precision.  The specified value is quantized to the
     *  closest value representable with the specified precision.
     *
     *  @param value The value to represent.
     *  @param precision The precision to use.
     *  @exception IllegalArgumentException If the supplied precision
     *   is invalid.
     */
    public FixToken(double value, Precision precision)
            throws IllegalArgumentException {
        try {
            _value = Quantizer.round(value, precision);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /** Construct a FixToken representing the specified value with the
     *  specified precision.  The specified value is quantized to the
     *  closest value representable with the specified precision.
     *
     *  @param value The value to represent.
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of integer bits.
     *  @exception IllegalArgumentException If the supplied precision
     *   is invalid.
     */
    public FixToken(double value, int numberOfBits, int integerBits)
            throws IllegalArgumentException {
        try {
            Precision precision =
                new Precision( numberOfBits, integerBits);
            _value = Quantizer.round(value, precision);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /** Construct a FixToken from the specified string.
     *  @param init A string expression of a fixed point number in Ptolemy II
     *   expression language syntax.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable fixed point number.
     */
    public FixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
	FixToken token = (FixToken)tree.evaluateParseTree();
        _value = token.fixValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new FixToken containing the absolute value of this token.
     *  @return A new instance of FixToken.
     */
    public ScalarToken absolute() {
	return new FixToken(_value.abs());
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than FixToken in the type lattice, meaning that
     *  it can be losslessly converted to an instance of FixToken.
     *  @param token The token to add to this one.
     *  @return A new token representing the sum.
     *  @exception IllegalActionException If the specified token
     *   is not of a type that can be added to this token.
     */
    public Token add(Token token)
	    throws IllegalActionException {

        int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    _notSupportedMessage("add", this, token));
        } else if (compare == CPO.LOWER) {
            return token.addReverse(this);
        } else {
	    // type of the specified token <= FixToken
	    FixToken tem = (FixToken)convert(token);
	    FixPoint result = _value.add(tem.fixValue());
	    return new FixToken(result);
	}
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than FixToken in the type lattice, meaning that
     *  it can be losslessly converted to an instance of FixToken.
     *  @param token The token to add this Token to.
     *  @return A new Token containing the result.
     *  @exception IllegalActionException If the specified token
     *   is not of a type that can be added to this token.
     */
    public Token addReverse(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
                    + " is not lower than "
                    + getClass().getName());
        }

	// add is commutative on FixPoint.
        return add(token);
    }
    // FIXME: convert????

    /** Return the fixed point value of this token as a double. The
     *  conversion from a fixed point to a double is not lossless, so
     *  the doubleValue() cannot be used. Therefore an explicit lossy
     *  conversion method is provided.
     *  @return A double representation of the value of this token.
     */
    public double convertToDouble() {
        return _value.doubleValue();
    }

    /** Return a new Token whose value is the value of this token
     *  divided by the value of the argument token. The type of the
     *  specified token must be such that either it can be converted
     *  to the type of this token, or the type of this token can be
     *  converted to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *
     *  @param divisor A FixToken.
     *  @return A new FixToken.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be divide this Tokens value by.
     */
    public Token divide(Token divisor)
	    throws IllegalActionException {
        int compare = TypeLattice.compare(this, divisor);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("FixToken.divide: " +
                    "type of argument: " + divisor.getClass().getName() +
                    "is incomparable with FixToken in the type " +
                    "hierarchy.");
        }

        if (compare == CPO.LOWER) {
            return divisor.divideReverse(this);
        } else {
	    // argument type is lower or the same as FixPoint.
	    FixToken convertedToken = (FixToken)convert(divisor);
            FixPoint result = _value.divide(convertedToken.fixValue());
            return new FixToken(result);
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  divided by the value of this token. The type of the specified
     *  token must be lower than FixToken.
     *  @param dividend The token to be divided by the value of this Token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than FixToken.
     */
    public Token divideReverse(Token dividend)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, dividend);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the dividend "
                    + dividend.getClass().getName() + " is not lower than "
                    + getClass().getName());
        }

        FixToken tem = (FixToken)this.convert(dividend);
        FixPoint result = tem.fixValue().divide(_value);
        return new FixToken(result);
    }

    /** Return true if the argument is an instance of FixToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of FixToken with the
     *  same value.
     */
    public boolean equals(Object object) {
	// This test rules out subclasses.
	if (object.getClass() != FixToken.class) {
	    return false;
	}

	if (((FixToken)object).fixValue().equals(_value)) {
	    return true;
	}

	return false;
    }

    /** Return the value of this token as a FixPoint.
     *  @return A FixPoint.
     */
    public FixPoint fixValue() {
        return _value;
    }

    /** Return the type of this token.
     *  @return BaseType.FIX.
     */
    public Type getType() {
	return BaseType.FIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the contained fixed point number.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	double code = _value.doubleValue();
	return (int)code;
    }

    /** Test the values of this Token and the argument Token for equality.
     *  The type of the specified token must be such that either it can be
     *  converted to the type of this token, or the type of this token can
     *  be converted to the type of the specified token, without loss of
     *  information.
     *  @param token The token to test equality of this token with.
     *  @return BooleanToken indicating whether the values are equal.
     *  @exception IllegalActionException If the specified token is
     *  not of a type that can be compared with this Token.
     */
    public BooleanToken isEqualTo(Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("FixToken.isEqualTo: " +
                    "type of argument: " + token.getClass().getName() +
                    " is incomparable with FixToken in the type " +
                    " hierarchy.");
        }

        if (compare == CPO.LOWER) {
            return token.isEqualTo(this);
        } else {
	    // argument type is lower or the same as FixPoint.
	    FixToken convertedToken = (FixToken)convert(token);
            FixPoint tem = convertedToken.fixValue();
	    if (_value.equals(tem)) {
                return new BooleanToken(true);
	    }
	    return new BooleanToken(false);
        }
    }

    /** Check whether the value of this token is strictly less than
     *  that of the argument token.
     *
     *  @param arg A ScalarToken.
     *  @return A BooleanToken with value true if this token is strictly
     *   less than the argument.
     *  @exception IllegalActionException If the type of the argument token
     *   is incomparable with the type of this token.
     */
    public BooleanToken isLessThan(ScalarToken arg)
	    throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, arg);
        if (typeInfo == CPO.INCOMPARABLE) {
            throw new IllegalActionException("FixToken.isLessThan: The type" +
                    " of the argument token is incomparable with the type" +
                    " of this token. argType: " + arg.getType());
	}

	if (typeInfo == CPO.LOWER) {
	    if (arg.isEqualTo(this).booleanValue()) {
	        return new BooleanToken(false);
	    } else {
	        return arg.isLessThan(this).not();
	    }
	}

	// Argument type is lower or equal to this token.
	ScalarToken fixArg = arg;
	if (typeInfo == CPO.HIGHER) {
	    fixArg = (ScalarToken)convert(arg);
	}

        // Use double value of the fix point.
	if (_value.doubleValue() < fixArg.fixValue().doubleValue()) {
            return new BooleanToken(true);
        }
	return new BooleanToken(false);
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *
     *  @param arg A FixToken.
     *  @return A new FixToken.
     *  @exception IllegalActionException If the specified token
     *  is not of a type that can be multiplied to this Token.
     */
    public Token multiply(Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    _notSupportedMessage("multiply", this, token));
        } else if (compare == CPO.LOWER) {
            return token.multiplyReverse(this);
        } else {
	    // type of the specified token <= FixToken
	    FixToken tem = (FixToken)convert(token);
	    FixPoint result = _value.multiply(tem.fixValue());
	    return new FixToken(result);
        }
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token must
     *  be lower than FixToken.
     *
     *  @param token The token with which to multiply this Token.
     *  @return A new Token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than FixToken.
     */
    public Token multiplyReverse(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("FixToken.multiplyReverse: "
                    + "The type of the specified token "
                    + token.getClass().getName()
                    + " is not lower than " + getClass().getName());
        }

	// multiply is commutative on FixToken.
        return multiply(token);
    }

    /** Returns a new Token representing the multiplicative identity
     *  with the same precision as this FixToken.
     *  @return A new FixToken with value 1.0.
     */
    public Token one() {
        return new FixToken( 1.0, _value.getPrecision() );
    }

    /** Return a new Token whose value is the value of the argument
     *  token subtracted from the value of this token. The type of the
     *  specified token must be such that either it can be converted
     *  to the type of this token, or the type of this token can be
     *  converted to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the
     *  other.
     *
     *  @param token A FixToken.
     *  @return A new FixToken.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be subtracted from this Token.
     */
    public Token subtract(Token rightArgument)
	    throws IllegalActionException {
        int compare = TypeLattice.compare(this, rightArgument);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("FixToken.subtract: " +
                    "type of argument: " + rightArgument.getClass().getName() +
                    "is incomparable with FixToken in the type " +
                    "hierarchy.");
        }

        if (compare == CPO.LOWER) {
            return rightArgument.subtractReverse(this);
        } else {
	    // argument type is lower or the same as FixPoint.
	    FixToken convertedToken = (FixToken)convert(rightArgument);
            FixPoint result = _value.subtract(convertedToken.fixValue());
            return new FixToken(result);
        }
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token. The type of
     *  the specified token must be lower than FixToken.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than FixToken;
     */
    public Token subtractReverse(Token leftArgument)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, leftArgument);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + leftArgument.getClass().getName() + " is not "
                    + "lower than " + getClass().getName());
        }

        FixToken tem = (FixToken)this.convert(leftArgument);
        FixPoint result = tem.fixValue().subtract(_value);
        return new FixToken(result);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same
     *  value. The "fix" keyword indicates it is a FixToken. The first
     *  argument is the decimal value, the second is the total number
     *  of bits and the third is the number of bits for the integer
     *  portion. For more information about these arguments, see the
     *  three argument constructor.
     *
     *  @return A String representing of this Token.
     */
    public String toString() {
        Precision precision = _value.getPrecision();
	return "fix(" + _value.toString() +
            "," + precision.getNumberOfBits() +
            "," + precision.getIntegerBitLength() + ")";
    }

    /** Return a new token representing the additive identity with
     *  the same precision as this FixToken.
     *  @return A new FixToken with value 0.0.
     */
    public Token zero() {
        return new FixToken( 0.0, _value.getPrecision() );
    }

    /** Print the content of this FixToken: This is used for debugging
     *  only.
     */
    public void print() {
        _value.printFix();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The FixPoint value contained in this FixToken. */
    private FixPoint _value;
}
