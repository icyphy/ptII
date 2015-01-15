/* A token for QSS integration that contains a double and a derivative.

   Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.data;

import java.util.Arrays;

import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// SmoothToken

/**
   A double-valued token that contains zero or more derivatives.
   In mathematical analysis, smoothness has to do with how many derivatives
   a function possesses. A smooth function is one that has derivatives of
   all orders everywhere in its domain. An instance of this class represents
   a sample of a function at a point together with some finite number of
   derivatives of the function at that same point.
   <p>
   This token will be treated exactly
   like a {@link DoubleToken} by any actor or operation that does not
   specifically support it, and it will be represented in the type systems
   as a "double." But it can (potentially) carry additional information giving
   one or more derivatives of the function from which it is a sample.
   This token, therefore, gives a way for actors that either generate or
   use this derivative information to make that information available to other
   actors that can use it. Such actors should declare their input ports to
   be of type double, but when they receive an input token, they should
   check (using instanceof) whether the token is a SmoothToken, and if so,
   access these derivatives using the {@link #derivativeValues()} method.
   <p>
   Note that if two SmoothTokens are added or subtracted, then the derivatives also
   add or subtract.
   If a SmoothToken is added to a DoubleToken, the derivatives of the DoubleToken
   are assumed to be zero, and similarly for subtraction.
   <p>
   If a SmoothToken is multiplied by a SmoothToken, then the product rule of
   calculus is used to determine the derivatives of the product.
   The product rule stipulates that
   <pre>
      (xy)' = x'y + xy'
   </pre>
   If a SmoothToken is multiplied by a DoubleToken, then the derivatives 
   of the DoubleToken are assumed to be zero.
   <p>
   Division works similarly:
   <pre>
      (x/y)' = x'/y + x(1/y)' = x'/y - xy'/y^2
   </pre>
   where the last equality follows from the reciprocal rule of calculus.
   The second derivative of a multiplication or division is obtained by
   applying the above rules to x' and y' rather than to x and y.
   Higher-order derivatives are similarly obtained.
   <p>
   By default, instances of SmoothToken have no more than three derivatives.
   This can be changed using the {@link #setOrderLimit(int)} method.

   @author Thierry S. Nouidui, Michael Wetter, Edward A. Lee
   @version $Id$
   @since Ptolemy II 10
   @Pt.ProposedRating Red (mw)
   @Pt.AcceptedRating Red (mw)
*/
public class SmoothToken extends DoubleToken {

    /** Construct a SmoothToken with value 0.0 and no derivatives.
     */
    public SmoothToken() {
        super();
    }

    /** Construct a SmoothToken with the specified value and no derivatives.
     *  @param value The specified value.
     */
    public SmoothToken(double value) {
    	super(value);
    }
	
    /** Construct a SmoothToken with the specified value and derivatives.
     *  This constructor does not copy the derivatives argument, so it is up
     *  to the caller to ensure that the array passed in does not later get
     *  modified (tokens are required to be immutable).
     *  @param value The specified value.
     *  @param derivatives The specified derivatives.
     */
    public SmoothToken(double value, 
            double[] derivatives) {
    	super(value);
    	if (derivatives != null && derivatives.length > _maxOrder){
    		_derivatives = new double[_maxOrder];
    		System.arraycopy(derivatives, 0, _derivatives, 0, _maxOrder);
    	}
    	else{
    		_derivatives = derivatives;
    	}
    }

    /** Construct a SmoothToken from the specified string.
     *  @param init The initialization string, which is in a format
     *  suitable for java.lang.Double.parseDouble(String).
     *  @exception IllegalActionException If the Token could not
     *   be created with the given String.
     */
    public SmoothToken(String init) throws IllegalActionException {
        if (init == null || init.equals("nil")) {
            throw new IllegalActionException(notSupportedNullNilStringMessage(
                    "SmoothToken", init));
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
    ////                         static initializer                ////

    static {
         // Specify that the QSSToken class is an alternate implementation
         // of the double type. This allows the expression language to
         // recognize a return type of QSSToken from a static function
         // registered in the previous call as a double.

         // Commented out because it broke the build.
         BaseType.addType(BaseType.DOUBLE, "smoothToken", SmoothToken.class);
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
    
    /** Return true if the argument's class is SmoothToken and it has the
     *  same values as this token.
     *  @param object An object to compare for equality.
     *  @return True if the argument is a SmoothToken with the same
     *   value and derivatives. If either this object or the argument is a nil Token, return
     *  false.
     */
    @Override
    public boolean equals(Object object) {
	// The superclass checks class equality, doubleValue equality, and handles nil.
    	if (super.equals(object)) {
    	    // Now we just have to check the derivatives.
            double[] derivatives = ((SmoothToken) object).derivativeValues();
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
    
    /** Get the maximum order of any token (the number of derivatives).
     *  E.g., if maxOrder = 2, the token will have one value, the first
     *  and the 2nd derivative.
     *  By default, tokens will have maxOrder = 3.
     *  @param maxOrder The maximum order of the token.
     *  @see #setOrderLimit(int)
     */
    public static int getOrderLimit(){
        return _maxOrder;
    }
    

    /** Return the hash code for the SmoothToken object. If two SmoothToken
     *  objects have the same double value and their derivatives
     *  have the same hashCode, then the two SmoothTokens will have 
     *  the same hashcode.
     *  @return The hash code for this SmoothToken object.
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
        return this == SmoothToken.NIL;
    }

    /** Return a new token that is the negative of this one.
     *  @return The negative, where all the derivatives are also negated.
     */
    public SmoothToken negate() {
	if (_derivatives == null || _derivatives.length == 0) {
	    return new SmoothToken(-_value);
	}
	double[] derivatives = new double[_derivatives.length];
	for (int i = 0; i < _derivatives.length; i++) {
	    derivatives[i] = - _derivatives[i];
	}
	return new SmoothToken(-_value, derivatives);
    }

    /** Set the maximum order of any token (the number of derivatives).
     *  This is static, so calling it will affect <i>all</i>
     *  instances of SmoothToken in the same JVM.
     *  Its effect is not even limited to a single Ptolemy model.
     *  E.g., if maxOrder = 2, the token will have one value, the first
     *  and the 2nd derivative.
     *  By default, tokens will have maxOrder = 3.
     *  @param maxOrder The maximum order of the token.
     *  @see #getOrderLimit()
     */
    public static void setOrderLimit(int maxOrder){
        assert maxOrder >= 0: "maxOrder must be non-zero.";
        _maxOrder = maxOrder;
    }
    
    /** Return a SmoothToken with the specified value and no derivatives.
     *  This function gets registered by PtParser, after which it becomes
     *  available in the expression language.
     *  @param value The value.
     */
    public static SmoothToken smoothToken(double value) {
	return new SmoothToken(value, null);
    }

    /** Return a SmoothToken with the specified value and derivatives.
     *  This function gets registered by PtParser, after which it becomes
     *  available in the expression language.
     *  @param value The value.
     *  @param derivatives An array containing the first derivative,
     *   the second derivative, etc.
     */
    public static SmoothToken smoothToken(double value, double[] derivatives) {
	return new SmoothToken(value, derivatives);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  If there are no derivatives, then this just returns what the superclass
     *  returns to represent a double. Otherwise, the returned
     *  string has the form "smoothToken(value, derivatives)", where
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
	for (int i = 0; i < Math.min(_derivatives.length, _maxOrder); i++) {
	    if (first) {
		first = false;
	    } else {
		derivatives.append(",");
	    }
	    derivatives.append(Double.toString(_derivatives[i]));
	}
	derivatives.append("}");
    	return "smoothToken(" 
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
    public static final SmoothToken NIL = new SmoothToken(Double.NaN);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.
     *  The argument is guaranteed to be either a DoubleToken or
     *  a SmoothToken by the caller. If the argument is a DoubleToken,
     *  then its value is simply added to the value of this token, and
     *  a new SmoothToken is returned with the sum value and the derivatives
     *  of this token. If the argument is a SmoothToken, then returned SmoothToken
     *  will have the maximum of the number of derivatives of this token and
     *  the derivatives of the argument, and for derivatives given by both
     *  tokens, the derivative will be the sum of the two derivatives.
     *  @param rightArgument The token to add to this token.
     *  @return A new SmoothToken containing the result.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument) {
        final double sum = super.doubleValue() + ((DoubleToken) rightArgument).doubleValue();
        if (rightArgument instanceof SmoothToken) {
            double[] derivatives = ((SmoothToken) rightArgument).derivativeValues();
            if (derivatives == null) {
                // Just use the derivatives of this token.
                // This should be safe because, by policy, their value is immutable.
                return new SmoothToken(sum, _derivatives);
            } else if (_derivatives == null) {
                // Just use the derivatives of that token.
                // This should be safe because, by policy, their value is immutable.
                return new SmoothToken(sum, derivatives);
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
            return new SmoothToken(sum, result);
        } else {
            // Just use the derivatives of this token.
            // This should be safe because, by policy, their value is immutable.
            return new SmoothToken(sum, _derivatives);
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is a SmoothToken
     *  @param divisor The token to divide this token by.
     *  @return A new SmoothToken containing the result.
     */
    @Override
    protected ScalarToken _divide(ScalarToken divisor) {

	// FIXME: Need to implement the rule in the class comment.

        if (_derivatives == null || _derivatives.length == 0) {
            return super._divide(divisor);
        } else {
            final double div = ((DoubleToken) divisor).doubleValue();
            final double quotient = super.doubleValue() / div;
            double[] der = new double[_derivatives.length];
            for(int i = 0; i < _derivatives.length; i++) {
                der[i] = _derivatives[i]/div;
            }
            return new SmoothToken(quotient, der);
        }
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is SmoothToken.
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
     *  multiplied by the value of the argument token.  The derivatives
     *  of the result are calculated using the product rule.
     *  The argument is assumed to be a DoubleToken.
     *  It may also be a SmoothToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new SmoothToken containing the result.
     */
    @Override
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        double x = doubleValue();
        double y = ((DoubleToken)rightArgument).doubleValue();
        double product = x*y;
        if (rightArgument instanceof SmoothToken) {
            double[] derivatives = ((SmoothToken)rightArgument).derivativeValues();
            // Check whether one or both tokens lack derivatives.
            if (_derivatives == null || _derivatives.length == 0) {
                if (derivatives == null || derivatives.length == 0) {
                    // Both lack derivatives.
                    return new SmoothToken(product);
                }
                // Only x lacks derivatives. Hence, x should scale y's derivatives.
                double[] result = new double[derivatives.length];
                for (int i = 0; i < derivatives.length; i++) {
            	    result[i] = derivatives[i]*x;
                }
                return new SmoothToken(product, result);
            }
            // derivatives may be null. In this case, y should scale x's derivatives.
            if (derivatives == null){
                double[] result = new double[_derivatives.length];
                for (int i = 0; i < _derivatives.length; i++) {
                    result[i] = _derivatives[i]*y;
                }
                return new SmoothToken(product, result);
            }
            else{
                // Both have derivatives.
                // Multiply the tokens as if they were Taylor polynomials.

                // Build arrays whose elements are the coefficients of the polynomials.        
                double[] p1 = new double[_derivatives.length+1];
                double[] p2 = new double[ derivatives.length+1];
                p1[0] = x;
                p2[0] = y;
                System.arraycopy(_derivatives, 0, p1, 1, _derivatives.length);
                System.arraycopy( derivatives, 0, p2, 1,  derivatives.length);
                // Multiply the polynomials
                double[] pro = _multiplyPolynomials(p1, p2);
                double[] derRes = new double[pro.length-1];
                System.arraycopy(pro, 1, derRes, 0, derRes.length);
                return new SmoothToken(pro[0], derRes);
            }
        } else {
            // Assume the y derivatives are zero, so the returned result just
            // has the derivatives of this token scaled by y.
            if (_derivatives == null || _derivatives.length == 0) {
        	return new SmoothToken(product);
            }
            double[] result = new double[_derivatives.length];
            for (int i = 0; i < _derivatives.length; i++) {
        	result[i] = _derivatives[i]*y;
            }
            return new SmoothToken(product, result);
        }
    }
    
    /** Multiplies two polynomials.
     * 
     * @param p1 First polynomial.
     * @param p2 Second polynomial
     * @return The product of the polynomials
     */
    protected static double[] _multiplyPolynomials(final double[] p1, 
                                                   final double[] p2){
    
        double[] res = new double[(p1.length-1) + (p2.length-1) + 1];
        // Set all coefficients to zero.
        for(int i = 0; i < res.length; i++){
            res[i] = 0;
        }
        // Multiply the polynomials
        for(int i1=0; i1 < p1.length; i1++){
            for(int i2=0; i2 < p2.length; i2++){
                final int exponent = i1+i2;
                if(res[exponent] == 0){
                    res[exponent] = p1[i1]*p2[i2];
                }
                else{
                    res[exponent] += p1[i1]*p2[i2];
                }
            }
        }
        return res;
    }

    
    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is a DoubleToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new SmoothToken containing the result.
     */
    @Override
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        final double difference = super.doubleValue() - ((DoubleToken) rightArgument).doubleValue();
        if (rightArgument instanceof SmoothToken) {
            double[] derivatives = ((SmoothToken) rightArgument).derivativeValues();
            if (derivatives == null) {
                // Just use the derivatives of this token.
                // This should be safe because, by policy, their value is immutable.
                return new SmoothToken(difference, _derivatives);
            } else if (_derivatives == null) {
                // The derivatives should be negated.
        	double[] result = new double[derivatives.length];
        	for (int i = 0; i < result.length; i++) {
        	    result[i] = - derivatives[i];
        	}
                return new SmoothToken(difference, result);
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
            return new SmoothToken(difference, result);
        } else {
            // Just use the derivatives of this token.
            // This should be safe because, by policy, their value is immutable.
            return new SmoothToken(difference, _derivatives);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /* Maximum order of the token. 
     * 
     * A token with _maxOrder=3 will have a value and three derivatives.
     */
    static int _maxOrder = 3;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The derivatives. */
    private double[] _derivatives;
}
