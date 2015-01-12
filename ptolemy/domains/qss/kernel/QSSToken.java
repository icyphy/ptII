/* A token for QSS integration that contains a double and a derivative.

   Copyright (c) 2014-2015 The Regents of the University of California.
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

*/
package ptolemy.domains.qss.kernel;

import java.util.Arrays;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ScalarToken;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// QSSToken

/**
   A token for QSS integration that contains a double and
   zero or more derivatives. As far as the Ptolemy II type system is concerned,
   this is a DoubleToken, and it can be accepted by any actor that operates
   on instances of DoubleToken. But it potentially carries as an additional
   payload one more derivatives of the current value, and 
   actors in the QSS domain may access these derivatives using the
   {@link #derivativeValues()} method.  Actors in the QSS domain may construct
   this token instead of a DoubleToken in order to convey derivative information
   to downstream QSS actors.
   <p>
   Note that if two QSSTokens are added or subtracted, then the derivatives also
   add or subtract.
   If a QSSToken is added to a DoubleToken, the derivatives of the DoubleToken
   are assumed to be zero.
   <p>
   If a QSSToken is divided by a DoubleToken or QSSToken, then the value
   and derivatives are divided by the value of the other token.
   Its derivatives, if any, are ignored.
   FIXME: Is that the right thing to do?

   @author Thierry S. Nouidui, Michael Wetter, Edward A. Lee, Christopher Brooks
   @version $Id$
   @since Ptolemy II 10
   @Pt.ProposedRating Red (mw)
   @Pt.AcceptedRating Red (mw)
*/
public class QSSToken extends DoubleToken {
    /** Construct a QSSToken with value 0.0 and no derivatives.
     */
    public QSSToken() {
        super();
    }

    /** Construct a QSSToken with the specified value and no derivatives.
     *  @param value The specified value.
     */
    public QSSToken(double value) {
    	super(value);
    }
	
    /** Construct a QSSToken with the specified value and derivatives.
     *  This constructor does not copy the derivatives argument, so it is up
     *  to the caller to ensure that the array passed in does not later get
     *  modified (tokens are required to be immutable).
     *  @param value The specified value.
     *  @param derivatives The specified derivatives.
     */
    public QSSToken(double value, 
            double[] derivatives) {
    	super(value);
    	_derivatives = derivatives;
    }

    /** Construct a QSSToken from the specified string.
     *  @param init The initialization string, which is in a format
     *  suitable for java.lang.Double.parseDouble(String).
     *  @exception IllegalActionException If the Token could not
     *   be created with the given String.
     */
    public QSSToken(String init) throws IllegalActionException {
        if (init == null || init.equals("nil")) {
            throw new IllegalActionException(notSupportedNullNilStringMessage(
                    "QSSToken", init));
        }

        // It would be nice to call super(init) here, but we can't, so
        // we copy the code from the parent.

        // FIXME: Parsing the array of derivatives is not yet supported.
        try {
            _value = Double.parseDouble(init);
        } catch (NumberFormatException e) {
            throw new IllegalActionException(null, e, "Failed to parse \""
                    + init + "\" as a number.");
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the derivatives in the token as a double[], or null if there are
     *  no derivatives. Since tokens are immutable, the caller of this method must
     *  copy the returned array if it intends to modify the array.
     *  @return The value of the derivatives contained in this token.
     */
    public double[] derivativeValues() {
	if (_derivatives == null || _derivatives.length == 0) {
	    return null;
	}
	return _derivatives;
    }

    /** Return true if the argument's class is QSSToken and it has the
     *  same values as this token.
     *  @param object An object to compare for equality.
     *  @return True if the argument is a QSSToken with the same
     *   value and derivatives. If either this object or the argument is a nil Token, return
     *  false.
     */
    @Override
    public boolean equals(Object object) {
	// The superclass checks class equality, doubleValue equality, and handles nil.
    	if (super.equals(object)) {
    	    // Now we just have to check the derivatives.
            double[] derivatives = ((QSSToken) object).derivativeValues();
            if (derivatives == _derivatives) {
        	// Derivatives are identical (should be true only if null).
        	return true;
            }
            if (derivatives == null && _derivatives != null
        	    || derivatives != null && _derivatives == null) {
        	return false;
            }
            // Both tokens have derivatives.
            if (derivatives.length != _derivatives.length) {
                return false;
            }
            // Both tokens have the same number of derivatives.
            for(int i = 0; i < _derivatives.length; i++){
                if (derivatives[i] != _derivatives[i]) {
                    return false;
                }
            }
            return true;
    	} else {
            return false;
    	}
    }
    
    /** Return the hash code for the QSSToken object. If two QSSToken
     *  objects have the same double value and their derivatives
     *  have the same hashCode, then the two QSSTokens will have 
     *  the same hashcode.
     *  @return The hash code for this QSSToken object.
     */
    @Override
    public int hashCode() {
        // See http://www.technofundo.com/tech/java/equalhash.html
        int hashCode = super.hashCode();
        if (_derivatives != null) {
            hashCode = 31 * hashCode + Arrays.hashCode(_derivatives);
        }
        return hashCode;
    }

    /** Return true if the token is nil, (aka null or missing).
     *  Nil or missing tokens occur when a data source is sparsely populated.
     *  @return True if the token is the {@link #NIL} token.
     */
    @Override
    public boolean isNil() {
        // We use a method here so that we can easily change how
        // we determine if a token is nil without modify lots of classes.
        return this == QSSToken.NIL;
    }

    /** Return a QSSToken with the specified value and no derivatives.
     *  This function gets registered when the {@link QSSDirector}
     *  class is loaded, after which it becomes available in the
     *  expression language.
     *  @param value The value.
     */
    public static QSSToken qssToken(double value) {
	return new QSSToken(value, null);
    }

    /** Return a QSSToken with the specified value and derivatives.
     *  This function gets registered when the {@link QSSDirector}
     *  class is loaded, after which it becomes available in the
     *  expression language.
     *  @param value The value.
     *  @param derivatives An array containing the first derivative,
     *   the second derivative, etc.
     */
    public static QSSToken qssToken(double value, double[] derivatives) {
	return new QSSToken(value, derivatives);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  If there are no derivatives, then this just returns what the superclass
     *  returns to represent a double. Otherwise, the returned
     *  string has the form "qssToken(value, derivatives)", where
     *  the value is the value returned by {@link #doubleValue()}, and
     *  derivatives is an array of doubles.
     */
    @Override
    public String toString() {
	if (_derivatives == null || _derivatives.length == 0) {
	    return super.toString();
	}
	StringBuffer derivatives = new StringBuffer("{");
	boolean first = true;
	for (int i = 0; i < _derivatives.length; i++) {
	    if (first) {
		first = false;
	    } else {
		derivatives.append(",");
	    }
	    derivatives.append(Double.toString(_derivatives[i]));
	}
	derivatives.append("}");
    	return "qssToken(" 
    		+ super.toString()
    		+ ", "
    		+ derivatives.toString()
    		+ ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A token that represents a missing value.
     *  Null or missing tokens are common in analytical systems
     *  like R and SAS where they are used to handle sparsely populated data
     *  sources.  In database parlance, missing tokens are sometimes called
     *  null tokens.  Since null is a Java keyword, we use the term "nil".
     *  The toString() method on a nil token returns the string "nil".
     */
    public static final QSSToken NIL = new QSSToken(Double.NaN);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.
     *  The argument is guaranteed to be either a DoubleToken or
     *  a QSSToken by the caller. If the argument is a DoubleToken,
     *  then its value is simply added to the value of this token, and
     *  a new QSSToken is returned with the sum value and the derivatives
     *  of this token. If the argument is a QSSToken, then returned QSSToken
     *  will have the maximum of the number of derivatives of this token and
     *  the derivatives of the argument, and for derivatives given by both
     *  tokens, the derivative will be the sum of the two derivatives.
     *  @param rightArgument The token to add to this token.
     *  @return A new QSSToken containing the result.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument) {
        final double sum = super.doubleValue() + ((DoubleToken) rightArgument).doubleValue();
        if (rightArgument instanceof QSSToken) {
            double[] derivatives = ((QSSToken) rightArgument).derivativeValues();
            if (derivatives == null) {
                // Just use the derivatives of this token.
                // This should be safe because, by policy, their value is immutable.
                return new QSSToken(sum, _derivatives);
            } else if (_derivatives == null) {
                // Just use the derivatives of that token.
                // This should be safe because, by policy, their value is immutable.
                return new QSSToken(sum, derivatives);
            }
            // Create a sum of derivatives.
            int max = derivatives.length;
            if (max < _derivatives.length) {
        	max = _derivatives.length;
            }
            double[] result = new double[max];
            for (int i=0; i < max; i++) {
        	if (i < _derivatives.length && i < derivatives.length) {
        	    result[i] = _derivatives[i] + derivatives[i];
        	} else if (i < _derivatives.length) {
        	    result[i] = _derivatives[i];
        	} else {
        	    result[i] = derivatives[i];
        	}
            }
            return new QSSToken(sum, result);
        } else {
            // Just use the derivatives of this token.
            // This should be safe because, by policy, their value is immutable.
            return new QSSToken(sum, _derivatives);
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an QSSToken.  If this token
     *
     *  @param divisor The token to divide this token by.
     *  @return A new QSSToken containing the result.
     */
    @Override
    protected ScalarToken _divide(ScalarToken divisor) {
        if (_derivatives == null || _derivatives.length == 0) {
            return super._divide(divisor);
        } else {
            final double div = ((DoubleToken) divisor).doubleValue();
            final double quotient = super.doubleValue() / div;

            double[] der = new double[_derivatives.length];
            for (int i = 0; i < _derivatives.length; i++) {
                der[i] = _derivatives[i]/div;
            }
            return new QSSToken(quotient, der);
        }
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is QSSToken.
     *  @param rightArgument The token to compare to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException("Method not implemented.");
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is an QSSToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new QSSToken containing the result.
     */
    @Override
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        final double factor = ((DoubleToken) rightArgument).doubleValue();
        // FIXME: Yow, this seems backwards. Division is more expensive than addition.
        return _divide(new DoubleToken(1./factor));
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is a DoubleToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new QSSToken containing the result.
     */
    @Override
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        final double difference = super.doubleValue() - ((DoubleToken) rightArgument).doubleValue();
        if (rightArgument instanceof QSSToken) {
            double[] derivatives = ((QSSToken) rightArgument).derivativeValues();
            if (derivatives == null) {
                // Just use the derivatives of this token.
                // This should be safe because, by policy, their value is immutable.
                return new QSSToken(difference, _derivatives);
            } else if (_derivatives == null) {
                // The derivatives should be negated.
        	double[] result = new double[derivatives.length];
        	for (int i = 0; i < result.length; i++) {
        	    result[i] = - derivatives[i];
        	}
                return new QSSToken(difference, result);
            }
            // Create a difference of derivatives.
            int max = derivatives.length;
            if (max < _derivatives.length) {
        	max = _derivatives.length;
            }
            double[] result = new double[max];
            for (int i=0; i < max; i++) {
        	if (i < _derivatives.length && i < derivatives.length) {
        	    result[i] = _derivatives[i] - derivatives[i];
        	} else if (i < _derivatives.length) {
        	    result[i] = _derivatives[i];
        	} else {
        	    result[i] = -derivatives[i];
        	}
            }
            return new QSSToken(difference, result);
        } else {
            // Just use the derivatives of this token.
            // This should be safe because, by policy, their value is immutable.
            return new QSSToken(difference, _derivatives);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The derivatives. */
    private double[] _derivatives;
}
