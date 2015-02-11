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
package ptolemy.data;

import java.util.Arrays;

import ptolemy.data.type.BaseType;
import ptolemy.actor.util.Time;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// SmoothToken

/**
   A double-valued token that contains zero or more derivatives, representing
   the value of a function of time at a particular time.
   In mathematical analysis, smoothness has to do with how many derivatives
   a function possesses. A smooth function is one that has derivatives of
   all orders everywhere in its domain. An instance of this class represents
   a sample of a function at a point in time together with some finite number of
   derivatives of the function at that same point.
   <p>
   This token will be treated exactly
   like a {@link DoubleToken} by any actor or operation that does not
   specifically support it, and it will be represented in the type systems
   as a "double." But it can (potentially) carry additional information giving
   one or more derivatives of the function from which it is a sample and
   giving the time at which it represents a sample of the signal.
   This token, therefore, gives a way for actors that either generate or
   use this derivative information to make that information available to other
   actors that can use it. Such actors should declare their input ports to
   be of type double, but when they receive an input token, they should
   check (using instanceof) whether the token is a SmoothToken, and if so,
   access these derivatives using the {@link #derivativeValues()} method,
   or extrapolate the value to a specified time using the {@link #extrapolate(Time)}
   method.
   <p>
   Note that if two SmoothTokens are added or subtracted, then the derivatives also
   add or subtract. If the times of the two tokens that are added or subtracted are
   not the same, then the one with the lesser time is extrapolated to the larger time,
   and the result will be the sum at the later time.
   If a SmoothToken is added to a DoubleToken, the derivatives of the DoubleToken
   are assumed to be zero, and similarly for subtraction.
   <p>
   If a SmoothToken is multiplied by a SmoothToken, then the product rule of
   calculus is used to determine the derivatives of the product.
   The product rule stipulates that
   <pre>
      (xy)' = x'y + xy'
   </pre>
   Again, if the times of the two tokens are not equal, then the one with the lesser
   time will be extrapolated to the larger time before being multiplied, and the time
   of the result will be the larger time.
   If a SmoothToken is multiplied by a DoubleToken, then the derivatives 
   of the DoubleToken are assumed to be zero.
   <p>
   Division works similarly:</p>
   <pre>
      (x/y)' = x'/y + x(1/y)' = x'/y - xy'/y^2
   </pre>
   <p>where the last equality follows from the reciprocal rule of calculus.
   The second derivative of a multiplication or division is obtained by
   applying the above rules to x' and y' rather than to x and y.
   Higher-order derivatives are similarly obtained.
   </p><p>
   You can construct an instance of this token in the Ptolemy expression
   language using the <code>smoothToken(double, double, {double})</code> function.
   The first argument specifies the value, and the second argument specifies
   the time, and the third specifies the derivatives. Also provided in the
   expression language are one and two argument versions of this function that
   assume the time is zero. These should be used only during initialization, and
   only if the start time of the model is actually zero.
   </p><p>
   By default, instances of SmoothToken have no more than three derivatives.
   This can be changed using the {@link #setOrderLimit(int)} method.
   </p><p>
   FIXME: Division is not implemented yet.
   </p>

   @author Thierry S. Nouidui, Michael Wetter, Edward A. Lee
   @version $Id$
   @since Ptolemy II 10
   @Pt.ProposedRating Red (mw)
   @Pt.AcceptedRating Red (mw)
*/
public class SmoothToken extends DoubleToken {

    /** Construct a SmoothToken with value 0.0 at time zero and no derivatives.
     */
    public SmoothToken() {
        this(0.0, Time.ZERO, null);
    }

    /** Construct a SmoothToken with the specified value at time zero and no derivatives.
     *  @param value The specified value.
     */
    public SmoothToken(double value) {
        this(value, Time.ZERO, null);
    }
	
    /** Construct a SmoothToken with the specified value at time zero
     *  and the specified derivatives.
     *  This constructor does not copy the derivatives argument, so it is up
     *  to the caller to ensure that the array passed in does not later get
     *  modified (tokens are required to be immutable).
     *  @param value The specified value.
     *  @param derivatives The specified derivatives.
     */
    public SmoothToken(double value, double[] derivatives) {
        this(value, Time.ZERO, derivatives);
    }

    /** Construct a SmoothToken with the specified value at the specified
     *  time, and with the specified derivatives.
     *  This constructor does not copy the derivatives argument, so it is up
     *  to the caller to ensure that the array passed in does not later get
     *  modified (tokens are required to be immutable).
     *  @param value The specified value.
     *  @param time The specified time.
     *  @param derivatives The specified derivatives.
     */
    public SmoothToken(double value, Time time, double[] derivatives) {
    	super(value);
    	_time = time;
    	if (_time == null) {
    	    _time = Time.ZERO;
    	}
    	if (derivatives != null && derivatives.length > _maxOrder){
    	    _derivatives = new double[_maxOrder];
    	    System.arraycopy(derivatives, 0, _derivatives, 0, _maxOrder);
    	} else{
    	    _derivatives = derivatives;
    	}
    }

    /** Construct a SmoothToken with the specified value and derivatives, given
     *  as a single array, at the specified time.
     *  This constructor copies the data from argument, so the caller is free
     *  to modify the array after this method returns.
     *  @param x An array where the first element is the value, and optionally any
     *   other elements can be present to specify the first, second, etc. derivatives.
     *  @param time The time at which this token is a sample.
     */
    public SmoothToken(double[] x, Time time) {
    	this(x[0], time, null);
    	if (x.length > 1) {
    	    final int nDer = (x.length > _maxOrder) ? _maxOrder : (x.length-1);
    	    _derivatives = new double[nDer];
    	    System.arraycopy(x, 1, _derivatives, 0, nDer);
    	}
    }
    
    /** Construct a SmoothToken from the specified string, which specifies only
     *  a value. The resulting token will have no derivatives and will represent
     *  a sample at time zero.
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
        _time = Time.ZERO;
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
   
    /** Given an array of Tokens and a time, align them by
     *  extrapolating all tokens that are instances of
     *  SmoothToken to that time, and returning
     *  an array of tokens with the extrapolated values and derivatives.
     *  If any of the tokens is not a SmoothToken, it is returned unmodified
     *  in the result.
     *  The returned array will have the same size as the argument array, and
     *  all the tokens will have the same maximum time.
     *  @param args The tokens to be aligned.
     *  @param time The Time to which the tokens will be aligned.
     *  @return An array of aligned tokens.
     */
    static public Token[] align(Token[] args, Time time) {
	Token[] result = new Token[args.length];
	for (int i = 0; i < args.length; i++) {
	    if (args[i] instanceof SmoothToken) {
		result[i] = ((SmoothToken)args[i]).extrapolate(time);
	    } else {
		result[i] = args[i];
	    }
	}
	return result;
    }


    /** Given an array of Tokens, align them by finding the maximum time
     *  of all the tokens, extrapolating all tokens that are instances of
     *  SmoothToken to that time, and returning
     *  an array of tokens with the extrapolated values and derivatives.
     *  If any of the tokens is not a SmoothToken, it is returned unmodified
     *  in the result.
     *  The returned array will have the same size as the argument array, and
     *  all the tokens will have the same maximum time.
     *  @param args The tokens to be aligned.
     *  @return An array of aligned tokens.
     */
    static public Token[] align(Token[] args) {
	// First, find the maximum time.
	Time latestTime = null;
	for (int i = 0; i < args.length; i++) {
	    if (args[i] instanceof SmoothToken) {
		SmoothToken smooth = (SmoothToken)args[i];
		if (latestTime == null || latestTime.compareTo(smooth._time) < 0) {
		    latestTime = smooth._time;
		}
	    }
	}
	// Align the tokens
	Token[] result = align(args, latestTime);
	return result;
    }
    
    /** Given two SmoothTokens, align them by finding the maximum time
     *  of the tokens, extrapolating the other token to that time, and returning
     *  an array of tokens with the extrapolated values and derivatives.
     *  The returned array will have size 2, and
     *  all the tokens will have the same maximum time.
     *  @param arg1 The first SmoothToken to be aligned.
     *  @param arg2 The second SmoothToken to be aligned.
     *  @return An array of tokens with the extrapolated values and
     *  derivatives.
     */
    SmoothToken[] align(SmoothToken arg1, SmoothToken arg2) {
	// First, find the maximum time.
	Time latestTime = arg1._time;
	if (latestTime.compareTo(arg2._time) < 0) {
	    latestTime = arg2._time;
	}
	SmoothToken[] result = new SmoothToken[2];
	result[0] = arg1.extrapolate(latestTime);
	result[1] = arg2.extrapolate(latestTime);
	return result;
    }

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
     *  same value and derivatives as this token.
     *  Note that this ignores the time of the tokens.
     *  This is needed to be able to use this in tests.
     *  @param object An object to compare for equality.
     *  @return True if the argument is a SmoothToken with the same
     *   value and derivatives. If either this object or the argument is a nil Token, return
     *  false.
     */
    @Override
    public boolean equals(Object object) {
	// The superclass checks class equality, doubleValue equality, and handles nil.
    	if (super.equals(object)) {
    	    // Check the times.
    	    /* No, don't. See above.
    	    if (!_time.equals(((SmoothToken)object)._time)) {
    		return false;
    	    }
    	    */
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
    
    /** Return a SmoothToken at the specified time whose value and derivatives
     *  are the result of extrapolating this token to the specified time.
     *  @param time The time to which to extrapolate this token.
     *  @return A SmoothToken at the specified time.
     */
    public SmoothToken extrapolate(Time time){
	// If the time matches, return this token.
	if (_time == time || (_time != null && _time.equals(time))) {
	    return this;
	}
    	// If _derivatives == null, simply return the current token.
    	if (_derivatives == null || _derivatives.length == 0) {
    	    return new SmoothToken(_value, time, null);
    	}
    	else if(_derivatives.length == 1) {
    	    // A common case is QSS2, which has a value and a derivative.
    	    // We handle this case special to stay computationally efficient.
    	    final double dt = time.subtractToDouble(_time);
    	    final double x = _value + dt * _derivatives[0];
    	    return new SmoothToken(x, time, _derivatives);
    	} else {
    	    // This is the case for tokens with second or higher order derivatives.
    	    // Build an array with value and derivatives
    	    double[] coef = new double[_derivatives.length+1];
    	    coef[0] = _value;
    	    System.arraycopy(_derivatives, 0, coef, 1, _derivatives.length);

    	    // Create vector with factorial coefficients times dt 
    	    // raised to the corresponding power.
    	    double[] fact = new double[coef.length];
    	    fact[0] = 1;
    	    final double dt = time.subtractToDouble(_time);
    	    for(int i = 1; i < coef.length; i++){
    		fact[i] = dt*fact[i-1]/i;
    	    }

    	    // Advance time for all values in coef and store in new array res
    	    double[] res = new double[coef.length];
    	    for(int i = 0; i < coef.length; i++) {
    		for(int j = 0; j < coef.length-i; j++) {
    		    res[i] += coef[j+i] * fact[j];
    		}
    	    }
    	    double[] der = new double[_derivatives.length];
    	    System.arraycopy(res, 1, der, 0, _derivatives.length);

    	    return new SmoothToken(res[0], time, der);
    	}
    }

    /** Get the maximum order of any token (the number of derivatives).
     *  E.g., if maxOrder = 2, the token will have one value, the first
     *  and the 2nd derivative.
     *  By default, tokens will have maxOrder = 3.
     *  @return the maximum order.
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
	    return new SmoothToken(-_value, _time, null);
	}
	double[] derivatives = new double[_derivatives.length];
	for (int i = 0; i < _derivatives.length; i++) {
	    derivatives[i] = - _derivatives[i];
	}
	return new SmoothToken(-_value, _time, derivatives);
    }

    /** Set the maximum order of any token (the number of derivatives).
     *  This is static, so calling it will affect <i>all</i>
     *  instances of SmoothToken in the same JVM.
     *  Its effect is not even limited to a single Ptolemy model.
     *  E.g., if maxOrder = 2, the token will have one value, the first
     *  and the 2nd derivative.
     *  By default, tokens will have maxOrder = 3.
     *  The maxOrder must be non-negative.
     *  @param maxOrder The maximum order of the token.
     *  @see #getOrderLimit()
     */
    public static void setOrderLimit(int maxOrder){
        if (maxOrder < 0) {
            throw new IllegalArgumentException("maxOrder must be non-negative, not "
                    + maxOrder + ".");
        }
        _maxOrder = maxOrder;
    }
    
    /** Return a SmoothToken with the specified value at time zero and no derivatives.
     *  This function gets registered by PtParser, after which it becomes
     *  available in the expression language.
     *  Note that there is no way in the expression language to construct a
     *  SmoothToken with a time other than zero. This makes sense because usually
     *  expressions are evaluated only once when a model is opened.
     *  @param value The value.
     *  @return The SmoothToken with the specified value at time zero
     *  and no derivatives.
     */
    public static SmoothToken smoothToken(double value) {
	return new SmoothToken(value, Time.ZERO, null);
    }

    /** Return a SmoothToken with the specified value at time zero and derivatives.
     *  This function gets registered by PtParser, after which it becomes
     *  available in the expression language.
     *  Note that there is no way in the expression language to construct a
     *  SmoothToken with a time other than zero. This makes sense because usually
     *  expressions are evaluated only once when a model is opened.
     *  @param value The value.
     *  @param derivatives An array containing the first derivative,
     *   the second derivative, etc.
     *  @return The SmoothToken with the specified value at time zero
     *  and derivatives.
     */
    public static SmoothToken smoothToken(double value, double[] derivatives) {
	return new SmoothToken(value, Time.ZERO, derivatives);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value and derivatives.
     *  However, the parsed token will not have the same time. It will have time zero.
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
     *  a new SmoothToken is returned with the sum value, time, and the derivatives
     *  of this token. If the argument is a SmoothToken, then this token and
     *  the argument are first aligned using 
     *  {@link ptolemy.data.SmoothToken#align(SmoothToken, SmoothToken)},
     *  and then added.  The returned SmoothToken
     *  will have the maximum of the number of derivatives of this token and
     *  the derivatives of the argument, and for derivatives given by both
     *  tokens, the derivative will be the sum of the two derivatives.
     *  The time of the returned token will be the maximum of the time of this
     *  token and the argument.
     *  @param rightArgument The token to add to this token.
     *  @return A new SmoothToken containing the result.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument) {
        if (rightArgument instanceof SmoothToken) {
            // First align the tokens.
            SmoothToken[] aligned = align(this, (SmoothToken)rightArgument);
            
            // Compute the sum of the values.
            final double sum = aligned[0].doubleValue() + aligned[1].doubleValue();
            
            // Compute the derivatives of the result.
            double[] derivatives = aligned[1].derivativeValues();
            if (derivatives == null) {
                // Just use the derivatives of this token.
                // This should be safe because, by policy, their value is immutable.
                return new SmoothToken(sum, aligned[0]._time, aligned[0]._derivatives);
            } else if (aligned[0]._derivatives == null) {
                // Just use the derivatives of the second token.
                // This should be safe because, by policy, their value is immutable.
                return new SmoothToken(sum, aligned[0]._time, derivatives);
            }
            // Both tokens have derivatives.
            // Create a sum of derivatives.
            int max = derivatives.length;
            if (max < aligned[0]._derivatives.length) {
        	max = aligned[0]._derivatives.length;
            }
            double[] result = new double[max];
            for (int i=0; i < max; i++) {
        	if (i < aligned[0]._derivatives.length && i < derivatives.length) {
        	    result[i] = aligned[0]._derivatives[i] + derivatives[i];
        	} else if (i < aligned[0]._derivatives.length) {
        	    result[i] = aligned[0]._derivatives[i];
        	} else {
        	    result[i] = derivatives[i];
        	}
            }
            return new SmoothToken(sum, aligned[0]._time, result);
        } else {
            final double sum = super.doubleValue() + ((DoubleToken) rightArgument).doubleValue();
            // Just use the derivatives of this token.
            // This should be safe because, by policy, their value is immutable.
            return new SmoothToken(sum, _time, _derivatives);
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
        if (divisor instanceof SmoothToken) {
            // First align the tokens.
            SmoothToken[] aligned = align(this, (SmoothToken)divisor);
            double x = aligned[0].doubleValue();
            double y = aligned[1].doubleValue();
            double quotient = x / y;

            // FIXME: Need to implement the rule in the class comment.
            // FIXME: Should use: (x/y)' = x'/y + x(1/y)' = x'/y - xy'/y^2

            if (_derivatives == null || _derivatives.length == 0) {
                return new DoubleToken(quotient);
            } else {
        	double[] der = new double[_derivatives.length];
        	for(int i = 0; i < _derivatives.length; i++) {
        	    der[i] = _derivatives[i]/y;
        	}
        	return new SmoothToken(quotient, aligned[0]._time, der);
            }
        } else {
            if (_derivatives == null || _derivatives.length == 0) {
        	return super._divide(divisor);
            } else {
        	final double div = ((DoubleToken) divisor).doubleValue();
        	final double quotient = super.doubleValue() / div;
        	double[] der = new double[_derivatives.length];
        	for(int i = 0; i < _derivatives.length; i++) {
        	    der[i] = _derivatives[i]/div;
        	}
        	return new SmoothToken(quotient, _time, der);
            }
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
            if (rightArgument instanceof DoubleToken){
                return super._isLessThan(rightArgument);
            }
            else{
                SmoothToken convertedArgument = (SmoothToken) rightArgument;
                return BooleanToken.getInstance(_value < convertedArgument
                    .doubleValue());
            }
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
        if (rightArgument instanceof SmoothToken) {
            // First align the tokens.
            SmoothToken[] aligned = align(this, (SmoothToken)rightArgument);
    	
            double x = aligned[0].doubleValue();
            double y = aligned[1].doubleValue();
            double product = x*y;
            double[] derivatives = aligned[1].derivativeValues();
            
            // Check whether one or both tokens lack derivatives.
            if (aligned[0]._derivatives == null || aligned[0]._derivatives.length == 0) {
        	// x lacks derivatives.
                if (derivatives == null || derivatives.length == 0) {
                    // Both lack derivatives.
                    return new SmoothToken(product, aligned[0]._time, null);
                }
                // Only x lacks derivatives. Hence, x should scale y's derivatives.
                double[] result = new double[derivatives.length];
                for (int i = 0; i < derivatives.length; i++) {
            	    result[i] = derivatives[i]*x;
                }
                return new SmoothToken(product, aligned[0]._time, result);
            }
            // y derivatives may be null. In this case, y should scale x's derivatives.
            if (derivatives == null){
                double[] result = new double[aligned[0]._derivatives.length];
                for (int i = 0; i < aligned[0]._derivatives.length; i++) {
                    result[i] = aligned[0]._derivatives[i]*y;
                }
                return new SmoothToken(product, aligned[0]._time, result);
            } else {
                // Both have derivatives.
                // Multiply the tokens as if they were Taylor polynomials.

                // Build arrays whose elements are the coefficients of the polynomials.        
                double[] p1 = new double[aligned[0]._derivatives.length+1];
                double[] p2 = new double[derivatives.length + 1];
                p1[0] = x;
                p2[0] = y;
        	// FIXME: Consider avoiding this copy by changing the internal representation
                // so that the the value and derivatives are in one array.
                // There are a few other places in the code that will be helped.
                System.arraycopy(_derivatives, 0, p1, 1, _derivatives.length);
                System.arraycopy( derivatives, 0, p2, 1,  derivatives.length);
                // Multiply the polynomials
                double[] pro = _multiplyPolynomials(p1, p2);
                double[] derRes = new double[pro.length-1];
                System.arraycopy(pro, 1, derRes, 0, derRes.length);
                return new SmoothToken(pro[0], aligned[0]._time, derRes);
            }
        } else {
            // Assume the rightArgument derivatives are zero, so the returned result just
            // has the derivatives of this token scaled by y.
            double y = ((DoubleToken)rightArgument).doubleValue();
            double product = doubleValue() * y;

            if (_derivatives == null || _derivatives.length == 0) {
        	return new SmoothToken(product, _time, null);
            }
            double[] result = new double[_derivatives.length];
            for (int i = 0; i < _derivatives.length; i++) {
        	result[i] = _derivatives[i]*y;
            }
            return new SmoothToken(product, _time, result);
        }
    }
    
    /** Multiply two polynomials.
     *  @param p1 First polynomial.
     *  @param p2 Second polynomial
     *  @return The product of the polynomials
     */
    protected static double[] _multiplyPolynomials(
	    final double[] p1, final double[] p2){
    
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
        if (rightArgument instanceof SmoothToken) {
            // First align the tokens.
            SmoothToken[] aligned = align(this, (SmoothToken)rightArgument);
            double x = aligned[0].doubleValue();
            double y = aligned[1].doubleValue();
            final double difference = x - y;

            double[] xderivatives = aligned[0].derivativeValues();
            double[] yderivatives = aligned[1].derivativeValues();
            
            if (yderivatives == null) {
                // Just use the xderivatives.
                // This should be safe because, by policy, their value is immutable.
                return new SmoothToken(difference, aligned[0]._time, xderivatives);
            } else if (xderivatives == null) {
                // The derivatives should be negated.
        	double[] result = new double[yderivatives.length];
        	for (int i = 0; i < result.length; i++) {
        	    result[i] = - yderivatives[i];
        	}
                return new SmoothToken(difference, aligned[0]._time, result);
            }
            // Create a difference of derivatives.
            int max = yderivatives.length;
            if (max < xderivatives.length) {
        	max = xderivatives.length;
            }
            double[] result = new double[max];
            for (int i=0; i < max; i++) {
        	if (i < xderivatives.length && i < yderivatives.length) {
        	    result[i] = xderivatives[i] - yderivatives[i];
        	} else if (i < xderivatives.length) {
        	    result[i] = xderivatives[i];
        	} else {
        	    result[i] = -yderivatives[i];
        	}
            }
            return new SmoothToken(difference, aligned[0]._time, result);
        } else {
            final double difference = super.doubleValue() - ((DoubleToken) rightArgument).doubleValue();
            // Just use the derivatives of this token.
            // This should be safe because, by policy, their value is immutable.
            return new SmoothToken(difference, _time, _derivatives);
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

    /** The time at which this token is a sample of a function. */
    private Time _time;
}
