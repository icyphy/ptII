/* A token that contains a long integer.

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
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// LongToken
/**
 * A token that contains an long integer.
 *
 * @author Neil Smyth, Yuhong Xiong, Steve Neuendorffer
 * @version $Id$
 * @since Ptolemy II 0.2
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
     *  @exception IllegalActionException If the Token could not
     *   be created with the given String.
     */
    public LongToken(String init) throws IllegalActionException {
        try {
	    _value = (Long.valueOf(init)).longValue();
	} catch (NumberFormatException e) {
	    throw new IllegalActionException(e.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
     *   cannot be carried out.
     */
    public static LongToken convert(Token token)
	    throws IllegalActionException {
	if (token instanceof LongToken) {
	    return (LongToken)token;
	}

	int compare = TypeLattice.compare(BaseType.LONG, token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "long"));
	}

	compare = TypeLattice.compare(BaseType.INT, token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    IntToken inttoken = IntToken.convert(token);
	    return new LongToken(inttoken.longValue());
	}

	throw new IllegalActionException(
                notSupportedConversionMessage(token, "long"));
    }

    /** Return true if the argument is an instance of LongToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of LongToken with the
     *  same value.
     */
    public boolean equals(Object object) {
	// This test rules out subclasses.
	if (object.getClass() != LongToken.class) {
	    return false;
	}

	if (((LongToken)object).longValue() == _value) {
	    return true;
	}
	return false;
    }

    /** Return the type of this token.
     *  @return BaseType.LONG_MATRIX
     */
    public Type getType() {
	return BaseType.LONG;
    }

    /** Return a hash code value for this token. This method returns the
     *  value of this token, casted to integer.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	return (int)_value;
    }

    /** Return the value in the token as a long.
     */
    public long longValue() {
	return (long)_value;
    }

    /** Returns a new LongToken with value 1.
     *  @return A new LongToken with value 1.
     */
    public Token one() {
        return new LongToken(1);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A String formed using java.lang.Long.toString().
     */
    public String toString() {
        return Long.toString(_value);
    }

    /** Returns a new LongToken with value 0.
     *  @return A new LongToken with value 0.
     */
    public Token zero() {
        return new LongToken(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.
     *  @return An LongToken.
     */
    protected ScalarToken _absolute() {
        LongToken result;
        if (_value >= 0) {
            result = this;
        } else {
            result = new LongToken(-_value);
        }
        return result;
    }

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an LongToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new LongToken containing the result.
     */
    protected ScalarToken _add(ScalarToken rightArgument) {
        long sum = _value + ((LongToken)rightArgument).longValue();
        return new LongToken(sum);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an LongToken
     *  @param rightArgument The token to divide this token by.
     *  @return A new LongToken containing the result.
     */
    protected ScalarToken _divide(ScalarToken rightArgument) {
        long quotient = _value / ((LongToken)rightArgument).longValue();
        return new LongToken(quotient);
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  LongToken.
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
     *  LongToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isEqualTo(ScalarToken rightArgument)
            throws IllegalActionException {
        LongToken convertedArgument = (LongToken)rightArgument;
        return BooleanToken.getInstance(
                _value == convertedArgument.longValue());
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is LongToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        LongToken convertedArgument = (LongToken)rightArgument;
        return BooleanToken.getInstance(
                _value < convertedArgument.longValue());
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is an LongToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new LongToken containing the result.
     */
    protected ScalarToken _modulo(ScalarToken rightArgument) {
        long remainder = _value % ((LongToken)rightArgument).longValue();
        return new LongToken(remainder);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is an LongToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new LongToken containing the result.
     */
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        long product = _value * ((LongToken)rightArgument).longValue();
        return new LongToken(product);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is an LongToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new LongToken containing the result.
     */
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        long difference = _value - ((LongToken)rightArgument).longValue();
        return new LongToken(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private long _value;
}
