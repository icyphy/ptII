/* A token that contains a long integer.

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
//// LongToken
/**
 * A token that contains an long integer.
 *
 * @author Neil Smyth, Yuhong Xiong
 * @version $Id$
 */
public class LongToken extends ScalarToken {

    /** Construct a token with long integer 0.
     */
    public LongToken() {
	_value = 0;
    }

    /** Construct a token with the specified value.
     */
    public LongToken(long value) {
	_value = value;
    }

    /** Construct a token from the given String.
     *  @exception IllegalArgumentException If the Token could not
     *   be created with the given String.
     */
    public LongToken(String init) throws IllegalArgumentException {
        try {
	    _value = (Long.valueOf(init)).longValue();
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a LongToken containing the absolute value of the
     *  value of this token.
     *  @return A LongToken.
     */
    public ScalarToken absolute() {
	return _value >= 0 ? this : new LongToken(-_value);
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned Token type chosen to achieve a lossless conversion.
     *  @param token The token to add to this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token add(Token token) throws IllegalActionException {
        long typeInfo = TypeLattice.compare(this, token);
        try {
            if (typeInfo == CPO.LOWER) {
                return token.addReverse(this);
            } else if (token instanceof LongToken) {
                long result = _value + ((LongToken)token).longValue();
                return new LongToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                LongToken tmp = (LongToken)this.convert(token);
                long result = _value + tmp.longValue();
                return new LongToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new IllegalActionException("LongToken: add method not " +
                    "supported between " + getClass().getName() + " and "
                    + token.getClass().getName());
        }
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param token The token to add this Token to.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token addReverse(Token token) throws IllegalActionException {
        LongToken tmp = (LongToken)this.convert(token);
        long result = tmp.longValue() + _value;
        return new LongToken(result);
    }

    /** Convert the specified token into an instance of LongToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of LongToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below LongToken in the type hierarchy, it is converted to
     *  an instance of LongToken or one of the subclasses of
     *  LongToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a LongToken.
     *  @return A LongToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(new LongToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("LongToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with LongToken in the type " +
                    "hierarchy.");
	}

	if (token instanceof LongToken) {
	    return token;
	}

	compare = TypeLattice.compare(new IntToken(), token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    IntToken inttoken = (IntToken)IntToken.convert(token);
	    return new LongToken(inttoken.longValue());
	}

	throw new IllegalActionException("cannot convert from token " +
		"type: " + token.getClass().getName() + " to a LongToken");
    }

    /** Return a new Token whose value is the value of this token
     *  divided by the value of the argument token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion. If two integers are divided,
     *  the result will be an integer which is the quotient.
     *  @param divisor The token to divide this Token by
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be divide this Tokens value by in a
     *  lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token divide(Token divisor)
	    throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, divisor);
        try {
            if (typeInfo == CPO.LOWER) {
                return divisor.divideReverse(this);
            } else if (divisor instanceof LongToken) {
		return new LongToken(_value / ((LongToken)divisor).longValue());
            } else if (typeInfo == CPO.HIGHER) {
                LongToken tmp = (LongToken)this.convert(divisor);
		return new LongToken(_value / (tmp.longValue()));
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new IllegalActionException(
                    "divide method not supported between"
                    + this.getClass().getName() + " and "
                    + divisor.getClass().getName()
                    + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of the argument token
     *  divided by the value of this token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param dividend The token to be divided by the value of this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be divided by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token divideReverse(Token dividend)
	    throws IllegalActionException {
        LongToken tmp = (LongToken)this.convert(dividend);
	return new LongToken(tmp.longValue() / _value);
    }

    /** Return the type of this token.
     *  @return BaseType.LONG_MATRIX
     */
    public Type getType() {
	return BaseType.LONG;
    }

    /** Test the values of this Token and the argument Token for equality.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param token The token to test equality of this token with.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be compared with this Tokens value.
     *  @return BooleanToken indicating whether the values are equal.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        long typeInfo = TypeLattice.compare(this, token);
        try {
            if (typeInfo == CPO.LOWER) {
                return token.isEqualTo(this);
            } else if (token instanceof LongToken) {
                if ( _value == ((LongToken)token).longValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else if (typeInfo == CPO.HIGHER) {
                LongToken tmp = (LongToken)this.convert(token);
                if ( _value == tmp.longValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "equality method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + token.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Check if the value of this token is strictly less than that of the
     *  argument token.
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
            throw new IllegalActionException("LongToken.isLessThan: The type" +
                    " of the argument token is incomparable with the type of " +
                    "this token. argType: " + arg.getType());
	}

	if (typeInfo == CPO.LOWER) {
	    return arg.isLessThan(this);
	}

	// Argument type is lower or equal to this token.
	ScalarToken longArg = arg;
	if (typeInfo == CPO.HIGHER) {
	    longArg = (ScalarToken)convert(arg);
	}

	if (_value < longArg.longValue()) {
	    return new BooleanToken(true);
	}
	return new BooleanToken(false);
    }

    /** Return the value in the token as a long.
     */
    public long longValue() {
	return (long)_value;
    }

    /** Return a new Token whose value is the value of this token
     *  modulo the value of the argument token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param token The token to modulo this Token by.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be  used with modulo in a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token modulo(Token token) throws IllegalActionException {
        long typeInfo = TypeLattice.compare(this, token);
        try {
            if (typeInfo == CPO.LOWER) {
                return token.moduloReverse(this);
            } else if (token instanceof LongToken) {
                long result = _value % ((LongToken)token).longValue();
                return new LongToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                LongToken tmp = (LongToken)this.convert(token);
                long result = _value % tmp.longValue();
                return new LongToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "modulo method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + token.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of the argument token
     *  modulo the value of this token.
     *  Type resolution also occurs here, with the returned Token
     *  type chosen to achieve a lossless conversion.
     *  @param token The token to apply modulo to by the value of this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can apply modulo by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token moduloReverse(Token token) throws IllegalActionException {
        LongToken tmp = (LongToken)this.convert(token);
        long result = tmp.longValue() %  _value;
        return new LongToken(result);
    }


    /** Return a new Token whose value is the value of this Token
     *  multiplied with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param rightFactor The token to multiply this Token by.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be multiplied by this Tokens value in
     *  a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token multiply(Token rightFactor) throws IllegalActionException {
        long typeInfo = TypeLattice.compare(this, rightFactor);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightFactor.multiplyReverse(this);
            } else if (rightFactor instanceof LongToken) {
                long result = _value * ((LongToken)rightFactor).longValue();
                return new LongToken(result);
            } else if (typeInfo == CPO.HIGHER){
                LongToken tmp = (LongToken)this.convert(rightFactor);
                long result = _value * tmp.longValue();
                return new LongToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "multiply method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + rightFactor.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of the argument Token
     *  multiplied with the value of this Token.
     *  Type resolution also occurs here, with the returned Token
     *  type chosen to achieve a lossless conversion.
     *  @param leftFactor The token to be multiplied by the value of
     *   this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be multiplied by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token multiplyReverse(Token leftFactor)
	    throws IllegalActionException {
        LongToken tmp = (LongToken)this.convert(leftFactor);
        long result = tmp.longValue() * _value;
        return new LongToken(result);
    }

    /** Returns a new Token representing the multiplicative identity.
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() {
        return new LongToken(1);
    }

    /** Return a new Token whose value is the value of the argument Token
     *  subtracted from the value of this Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArg The token to subtract to this Token.
     *  @exception IllegalActionException If the passed token is
     *   not of a type that can be subtracted from this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token subtract(Token rightArg) throws IllegalActionException {
        long typeInfo = TypeLattice.compare(this, rightArg);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightArg.subtractReverse(this);
            } else if (rightArg instanceof LongToken) {
                long result = _value -  ((LongToken)rightArg).longValue();
                return new LongToken(result);
            } else if (typeInfo == CPO.HIGHER){
                LongToken tmp = (LongToken)this.convert(rightArg);
                long result = _value - tmp.longValue();
                return new LongToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "subtract method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + rightArg.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param leftArg The token to add this Token to.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token subtractReverse(Token leftArg) throws IllegalActionException {
        LongToken tmp = (LongToken)this.convert(leftArg);
        long result = tmp.longValue() - _value;
        return new LongToken(result);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A String formed using java.lang.Long.toString().
     */
    public String toString() {
        return Long.toString(_value);
    }

    /** Returns a new token representing the additive identity.
     *  @return A new Token containing the additive identity.
     */
    public Token zero() {
        return new LongToken(0);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private long _value;
}
