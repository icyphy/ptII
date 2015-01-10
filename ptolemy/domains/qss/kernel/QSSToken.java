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
package ptolemy.domains.qss.kernel;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
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

   @author Thierry S. Nouidui and Michael Wetter. Contributor: Edward A. Lee
   @version $Id$
   @since Ptolemy II 10
   @Pt.ProposedRating Red (mw)
   @Pt.AcceptedRating Red (mw)
*/
public class QSSToken extends DoubleToken {

    /** Construct a QSSToken with the specified value and no derivatives.
     *  @param value The specified value.
     */
    public QSSToken(double value) {
    	super(value);
    }
	
    /** Construct a QSSToken with the specified value and derivatives.
     *  @param value The specified value.
     *  @param derivatives The specified derivatives.
     */
    public QSSToken(double value, 
            double[] derivatives) {
    	super(value);
    	_derivatives = derivatives;
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

    /** Return the type of this token.
     *  @return BaseType.QSS
     */
    @Override
    public Type getType() {
    	//FIXME: Not sure what to do here.
        return super.getType();
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the contained double.
     *  @return A hash code value for this token.
     *  @exception IllegalActionException If the method is called.
     */
    @Override
    public int hashCode(){
    	// FIXME Not sure how to do this. Shouldn't the hash code be unique,
    	// which is also not the case for the parent class.
        return super.hashCode();
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

    /** Returns a QSSToken with value 1.0.
     *  @return A QSSToken with value 1.0.
     */
    @Override
    public Token one() {
        return ONE;
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  The exact form of the number depends on its value, and may be either
     *  decimal or exponential.  In general, exponential is used for numbers
     *  whose magnitudes are very large or very small, except for zero which
     *  is always represented as 0.0.  The behavior is roughly the same as
     *  Double.toString(), except that we limit the precision to seven
     *  fractional digits.  If you really must have better precision,
     *  then use <code>Double.toString(token.doubleValue())</code>.
     *  If this token has a unit, the return string also includes a unit
     *  string produced by the unitsString() method in the super class.
     *  @return A String representing the double value and the units (if
     *   any) of this token.
     *  @see ptolemy.data.ScalarToken#unitsString
     */
    @Override
    public String toString() {
    	String s = super.toString();
    	s += ". Higher order derivatives = " + derivativeValues() + ".";
    	return s;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an QSSToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new QSSToken containing the result.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument) {
        final double sum = super.doubleValue() + ((QSSToken) rightArgument).doubleValue();
        return new QSSToken(sum, _derivatives);
    }


    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an QSSToken
     *  @param divisor The token to divide this token by.
     *  @return A new QSSToken containing the result.
     */
    @Override
    protected ScalarToken _divide(ScalarToken divisor) {
    	final double div = ((QSSToken) divisor).doubleValue();
        final double quotient = super.doubleValue() / div;
        double[] der = new double[_derivatives.length];
        for(int i = 0; i < _derivatives.length; i++)
            der[i] = _derivatives[i]/div;
        return new QSSToken(quotient, der);
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
        final double factor = ((QSSToken) rightArgument).doubleValue();
        return _divide(new DoubleToken(1./factor));
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is an QSSToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new QSSToken containing the result.
     */
    @Override
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        final double difference = super.doubleValue() - ((QSSToken) rightArgument).doubleValue();
        return new QSSToken(difference, _derivatives);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The derivatives. */
    private double[] _derivatives;
}
