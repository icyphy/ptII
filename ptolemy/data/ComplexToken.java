/* A token that contains a Complex.

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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// ComplexToken
/**
A token that contains a Complex.
<p>
@author Yuhong Xiong, Neil Smyth
@version $Id$
@see ptolemy.data.Token

*/
public class ComplexToken extends ScalarToken {

    /** Construct a ComplexToken with Complex 0.0+0.0i
     */
    public ComplexToken() {
	_value = new Complex(0.0, 0.0);
    }

    /** Construct a ComplexToken with the specified value.
     */
    public ComplexToken(Complex value) {
	_value = value;
    }

    /* FIXME: It seems that initializing from String should be supported
       // in the Complex class. This constructor will be supported after
       // the Complex class supports that.
       // Construct a ComplexToken from the specified string.
       // @exception IllegalArgumentException If the Token could not
       //  be created with the given String.

       public ComplexToken(String init) throws IllegalArgumentException {
       }
    */

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a DoubleToken containing the magnitude of the complex
     *  value of this token.
     *  @return A DoubleToken.
     */
    public ScalarToken absolute() {
	return new DoubleToken(_value.magnitude());
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *
     *  @param token The token to add to this Token.
     *  @return A new Token containing the result.
     *  @exception IllegalActionException If the specified token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token add(Token token)
	    throws IllegalActionException {

        int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
            String msg = "add method not supported between " +
                this.getClass().getName() + " and " +
                token.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            return token.addReverse(this);
        } else {
	    // type of the specified token <= ComplexToken
	    ComplexToken tem = (ComplexToken)convert(token);
	    Complex result = _value.add(tem.complexValue());
	    return new ComplexToken(result);
	}
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than ComplexToken.
     *  @param token The token to add this Token to.
     *  @return A new Token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than ComplexToken.
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

	// add is commutative on Complex.
        return add(token);
    }

    /** Return the value of this token as a Complex.
     *  @return A Complex
     */
    public Complex complexValue() {
        // Complex is immutable, so we can just return the value.
        return _value;
    }

    /** Convert the specified token into an instance of ComplexToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of ComplexToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below ComplexToken in the type hierarchy, it is converted to
     *  an instance of ComplexToken or one of the subclasses of
     *  ComplexToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a ComplexToken.
     *  @return A ComplexToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(new ComplexToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("DoubleToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with ComplexToken in the type " +
                    "hierarchy.");
	}

	if (token instanceof ComplexToken) {
	    return token;
	}

	compare = TypeLattice.compare(new DoubleToken(), token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    DoubleToken doubletoken = (DoubleToken)DoubleToken.convert(token);
	    return new ComplexToken(doubletoken.complexValue());
	}

	// The argument is below ComplexToken in the type hierarchy,
        // but I don't recognize it.
	throw new IllegalActionException("cannot convert from token " +
		"type: " + token.getClass().getName() + " to a ComplexToken");
    }

    /** Return a new Token whose value is the value of this token
     *  divided by the value of the argument token. The type of the
     *  specified token must be such that either it can be converted
     *  to the type of this token, or the type of this token can be
     *  converted to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *
     *  @param divisor The token to divide this Token by
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be divide this Tokens value by in a
     *  lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token divide(Token divisor)
	    throws IllegalActionException {
        int compare = TypeLattice.compare(this, divisor);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("ComplexToken.divide: " +
                    "type of argument: " + divisor.getClass().getName() +
                    "is incomparable with ComplexToken in the type " +
                    "hierarchy.");
        }

        if (compare == CPO.LOWER) {
            return divisor.divideReverse(this);
        } else {
	    // argument type is lower or the same as Complex.
	    ComplexToken comptoken = (ComplexToken)convert(divisor);
            Complex result = _value.divide(comptoken.complexValue());
            return new ComplexToken(result);
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  divided by the value of this token. The type of the specified
     *  token must be lower than ComplexToken.
     *  @param dividend The token to be divided by the value of this Token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than ComplexToken;
     */
    public Token divideReverse(Token dividend)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, dividend);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the dividend "
                    + dividend.getClass().getName() + " is not lower than "
                    + getClass().getName());
        }

        ComplexToken tem = (ComplexToken)this.convert(dividend);
        Complex result = tem.complexValue().divide(_value);
        return new ComplexToken(result);
    }

    /** Return the type of this token.
     *  @return BaseType.COMPLEX
     */
    public Type getType() {
	return BaseType.COMPLEX;
    }

    /** Test the values of this Token and the argument Token for equality.
     *  The type of the specified token must be such that either it can be
     *  converted to the type of this token, or the type of this token can
     *  be converted to the type of the specified token, without loss of
     *  information.
     *  @param token The token to test equality of this token with.
     *  @return BooleanToken indicating whether the values are equal.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be compared with this Token.
     */
    public BooleanToken isEqualTo(Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("ComplexToken.isEqualTo: " +
                    "type of argument: " + token.getClass().getName() +
                    "is incomparable with ComplexToken in the type " +
                    "hierarchy.");
        }

        if (compare == CPO.LOWER) {
            return token.isEqualTo(this);
        } else {
	    // argument type is lower or the same as Complex.
	    ComplexToken comptoken = (ComplexToken)convert(token);
            Complex tem = comptoken.complexValue();
	    if (_value.equals(tem)) {
                return new BooleanToken(true);
	    }
	    return new BooleanToken(false);
        }
    }

    /** Check if the magnitude of the value of this ComplexToken is strictly
     *  less than that of the argument token.
     *  @param arg A ScalarToken.
     *  @return A BooleanToken with value true if the magnitude of this token
     *   is strictly less than the argument.
     *  @exception IllegalActionException If the type of the argument token
     *   is incomparable with the type of this token.
     */
    public BooleanToken isLessThan(ScalarToken arg)
	    throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, arg);
        if (typeInfo == CPO.INCOMPARABLE) {
            throw new IllegalActionException("ComplexToken.isLessThan: The " +
                    "type of the argument token is incomparable with the type " +
                    "of this token. argType: " + arg.getType());
	}

	if (typeInfo == CPO.LOWER) {
	    return arg.isLessThan(this);
	}

	// Argument type is lower or equal to this token.
	ScalarToken complexArg = arg;
	if (typeInfo == CPO.HIGHER) {
	    complexArg = (ScalarToken)convert(arg);
	}

	// using magnitudeSquared() saves the square root calculation
	if (_value.magnitudeSquared() <
		complexArg.complexValue().magnitudeSquared()) {
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
     *  @param token The token to multiply to this Token.
     *  @return A new Token containing the result.
     *  @exception IllegalActionException If the specified token
     *   is not of a type that can be multiplied to this Token in
     *   a lossless fashion.
     */
    public Token multiply(Token token)
	    throws IllegalActionException {

        int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
            String msg = "multiply method not supported between " +
                this.getClass().getName() + " and " +
                token.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            return token.multiplyReverse(this);
        } else {
	    // type of the specified token <= ComplexToken
	    ComplexToken tem = (ComplexToken)convert(token);
	    Complex result = _value.multiply(tem.complexValue());
	    return new ComplexToken(result);
	}
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token must
     *  be lower than ComplexToken.
     *  @param token The token to multiply this Token to.
     *  @return A new Token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than ComplexToken.
     */
    public Token multiplyReverse(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("Complex.multiplyReverse: "
                    + "The type of the specified token "
                    + token.getClass().getName()
                    + " is not lower than " + getClass().getName());
        }

	// multiply is commutative on Complex.
        return multiply(token);
    }

    /** Returns a new Token representing the multiplicative identity.
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() {
        return new ComplexToken(new Complex(1.0));
    }

    /** Return the value contained in this Token as a String.
     *  @return A String.
     *  @deprecated Use toString() instead.
     */
    public String stringValue() {
	return toString();
    }

    /** Return a new Token whose value is the value of the argument token
     *  subtracted by the value of this token. The type of the
     *  specified token must be such that either it can be converted
     *  to the type of this token, or the type of this token can be
     *  converted to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *
     *  @param rightArg The token to subtract this Token by
     *  @return A new Token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be subtracted from this Token in a
     *   lossless fashion.
     */
    public Token subtract(Token rightArg)
	    throws IllegalActionException {
        int compare = TypeLattice.compare(this, rightArg);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("ComplexToken.subtract: " +
                    "type of argument: " + rightArg.getClass().getName() +
                    "is incomparable with ComplexToken in the type " +
                    "hierarchy.");
        }

        if (compare == CPO.LOWER) {
            return rightArg.subtractReverse(this);
        } else {
	    // argument type is lower or the same as Complex.
	    ComplexToken comptoken = (ComplexToken)convert(rightArg);
            Complex result = _value.subtract(comptoken.complexValue());
            return new ComplexToken(result);
        }
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token. The type of
     *  the specified token must be lower than ComplexToken.
     *  @param leftArg The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than ComplexToken;
     */
    public Token subtractReverse(Token leftArg)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, leftArg);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + leftArg.getClass().getName() + " is not lower "
                    + "than " + getClass().getName());
        }

        ComplexToken tem = (ComplexToken)this.convert(leftArg);
        Complex result = tem.complexValue().subtract(_value);
        return new ComplexToken(result);
    }

    /** Return the value contained in this Token as a String.
     *  @return A String.
     */
    public String toString() {
        return _value.toString();
    }

    /** Returns a new token representing the additive identity.
     *  @return A new Token containing the additive identity.
     */
    public Token zero() {
        return new ComplexToken(new Complex(0.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Complex _value = null;
}
