/* A FixPoint data type.

Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.io.Serializable;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.text.NumberFormat;
import ptolemy.math.Precision;

//////////////////////////////////////////////////////////////////////////
//// FixPoint
/** 
This class provides a Fixpoint data type and a library of functions
that operate on and return Fixpoint data. An instance of the class is
immutable, meaning that its value is set in the constructor and cannot
then be modified.  This is similar to the Java built-in classes like
Double, Integer, etc.

<p>

In a Fixpoint, a value is represented by a finite numbers of bits, as
defined by the precision of the Fixpoint. The precision can be
expressed in different ways. The precision indicates how many bits are
used to represent the integer part and the fractional part of a
Fixpoint value. The total number of bits used is thus equal to the
total number of bits used for the integer and fractional part.

<p>

The precision of a Fixpoint can be noted in different ways

<ul>

<li> <b>m/n</b> <br> The total bit length of the Fixpoint is equal to
<i>m</i> bits and the integer part is equal to <i>n</i> bits. The
fractional part it thus equal to <i>m-n</i> bits.

<li> <b>m.n</b> <br> The total length of the Fixpoint is equal to
<i>n+m</i> bits. The integer part is <i>m</i> bits long and the
fractional part is <i>n</i> bits long.

<li> <b>m^e</b> <br> The Fixpoint can represent all numbers between
-2^m < number < 2^m with a resolution of <i>e</i> bits. This is
equivalent to saying that the the total number of bits available is
<i>e</i> and that <i>m</i> bits are used to describe the integer part.

</ul>

<p> 

Because a Fixpoint uses a finite number of bits to represent a value,
a real value is rounded to the nearest number that can be expressed
with a the given precision of the Fixpoint, thereby introducing
quantization errors.

<p> 

In designing the FixPoint class, the main assumption is that all
operators work lossless, i.e. the precision of the result is changed
such that no rounding is done. Rounding errors are only possible when
one casts a Fixpoint result into a Fixpoint with less precision.

<p>


<p> 

The code for this Fixpoint implementation is written from scratch. At
it's core, it uses the Java class BigInteger and BigDecimal to
represent the finite value captured in Fixpoint. The use of the
BigInteger and BigDecimal classes, makes this Fixpoint implementation
truly platform independent. Furthermore, the use of BigInteger and
BigDecimal does not put any restrictions on the maximal number of bits
used to represents a value.

<p>

The Fixpoint uses three innerclasses. It uses the <i>Fixvalue</i>
innerclass to keep the BigInteger value and the state of the value
together. The <i>Error</i> innerclass is used to get a type safe
enumerations of the state a <i>Fixpoint</i> resides. 

@author Bart Kienhuis
@version $Id$ */

public final class FixPoint implements Cloneable, Serializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a FixPoint with value of zero and a default
     *  precision.  The default precision is (16/16), i.e. a is
     *  represented with 16 bits for the integer part and 0 bits for
     *  the fractional part.
     *  */
    public FixPoint() {
 	_initialize();
	try {
	    _precision = new Precision("16/16");
	} catch (IllegalArgumentException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
 	_value     = _makeBits( 0.0, _precision );
    }

    /** Construct a FixPoint with a particular precision and a value
     *  given as a string. The value of this Fixpoint will be rounded
     *  to the nearest Fixpoint value given the finite precision. When
     *  the value does not fit the given precision, a rounding error
     *  will occur and the value of the Fixpoint will be saturated to
     *  the largest positive or largest negative value possible given
     *  the precision.  
     *  @param precision The precision of this Fixpoint.  
     *  @param value The value that will be represented by
     *  the fixpoint given the finite precision.  */
    public FixPoint(String precision, String value) {
 	double tmpValue = (Double.valueOf(value)).doubleValue();
 	_initialize();
	try {
	    _precision = new Precision(precision);
	    _value     = _makeBits( tmpValue, _precision );
	} catch (IllegalArgumentException e ) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /** Construct a FixPoint with a particular precision and
     *  value. The value of this Fixpoint will be rounded to the
     *  nearest Fixpoint value given the finite precision. When the
     *  value does not fit the given precision, a rounding error will
     *  occur and the value of the Fixpoint will be saturated to the
     *  largest positive or largest negative value possible given the
     *  precision.  
     *  @param precision The precision of the fixpoint.
     *  @param value The value that will be represented by the fixpoint 
     *  given the finite precision.  */
    public FixPoint(String precision, double value) {
 	_initialize();
	try {
	    _precision = new Precision(precision);
	    _value     = _makeBits( value, _precision );
 	} catch (IllegalArgumentException e ) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /** Construct a FixPoint with a particular precision and value
     *  given as a double. When the value does not fit in the given
     *  precision the value of this Fixpoint will be saturated.
     *  @param precision The precision of the fixpoint.  
     *  @param value The value that will be represented by the fixpoint 
     *  given the finite precision.  */
    private FixPoint(Precision precision, Fixvalue value) {
 	_initialize();
 	_precision = precision;
 	_value     =  value;
    }

    /** Return a new Fixpoint number with value equal to the sum
     *  of this Fixpoint number and the argument. The operation is
     *  lossless because the precision of the result is changed to
     *  accommodate the result.
     *  @param arg A Fixpoint number.
     *  @return A new Fixpoint number.
     */
    public FixPoint add(FixPoint arg ) {
	// Align the precision of the two Fixpoints
	Precision  cp = _precision.matchThePoint(this._precision, arg.getPrecision());
	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY =  arg._alignToPrecision(cp);

	// Determine the new Fixvalue
	Fixvalue argZ =  argX.add( argY );

	// return the FixPoint with the correct precision and result
        return new FixPoint(cp, argZ);
    }

    /** Return a new Fixpoint number with value equal to the division
     *   of this Fixpoint number and the argument. This implementation
     *   uses the division operation of BigDecimal, instead of the
     *   division operator of BigInteger. This is much easier to
     *   implement. The operation is lossless because the precision of
     *   the result is changed to accommodate the result.  
     *   @param arg A Fixpoint number.  
     *   @return A new Fixpoint number.  */
    public FixPoint divide(FixPoint arg ) {
	// Align the precision of the two Fixpoints
	Precision cp = _precision.matchThePoint(this._precision, 
						arg.getPrecision());
	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY = arg._alignToPrecision(cp);

	// To compute the division, we use a trick, we make a
	// BigDecimal from the BigIntegers and use the division
	// operator of BigDecimal
	BigDecimal dx = new BigDecimal( argX.fixvalue );
	BigDecimal dy = new BigDecimal( argY.fixvalue );

	// FIXME: Should we use as scale factor (2^fractionBits)/log(10)?
	BigDecimal dz = dx.divide( dy, 32, BigDecimal.ROUND_HALF_UP );

	// Create a Fixvalue with the additional bits set
	Fixvalue result = _makeBits( dz.doubleValue(), cp );
	result.setError( _value.getError() );

	// return the FixPoint with the correct precision and result
	return new FixPoint(cp, result);
    }
    /** Return the fixvalue in the Fixpoint as a double. 
     *  @return The fixvalue in the Fixpoint as a double.
     */
    public double doubleValue() {
	int ln = _precision.getNumberOfBits(); 
	int ib = _precision.getIntegerBitLength();

	long h = (_value.fixvalue).longValue();
	double y = h/(_twoRaisedTo[ln - ib]).doubleValue();	 

	return y;
    }
    /** Get a description of the error condition of the Fixpoint 
	@return The description of the error condition of the Fixpoint
    */
    public String getErrorDescription() {
	return _value.getErrorDescription();
    }

    /** Returns the precision of the Fixpoint.  
     *  @return the precision of the Fixpoint.
     */
    public Precision getPrecision() {
	return _precision;
    }


    /** Return a new Fixpoint number with value equal to the multiplication
     *  of this Fixpoint number and the argument. The operation is
     *  lossless because the precision of the result is changed to
     *  accommodate the result.
     *  @param arg A Fixpoint number.
     *  @return A new Fixpoint number.
     */
    public FixPoint multiply(FixPoint arg ) {
	// Align the precision of the two Fixpoints
	Precision  cp = _precision.matchThePoint(this._precision, 
						 arg.getPrecision());
	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY = arg._alignToPrecision(cp);

	// Determine the new Fixvalue
	Fixvalue argZ =  argX.multiply( argY );

	// FIXME: Shouldn't we calculated simply the sommation of 
	// of the lenght of both fixvalue instead of really determining
	// the final used number of bits.
	int intLa = (argX.getIntegerBits(cp)).fixvalue.bitLength();
	int intLb = (argY.getIntegerBits(cp)).fixvalue.bitLength();

	Precision np = new Precision(argX.fixvalue.bitLength() + 
				     argY.fixvalue.bitLength(), 
				     intLa+intLb);

	// return the FixPoint with the correct precision and result
	return new FixPoint(np, argZ);
    }



    /** Return a bit string representation of the value of this Fixpoint. 
     * @return A bit string of the form "<i>integerbits . fractionbits</i>".
     */
    public String toBitString() {
	return  _value.getIntegerBits(_precision).toString() 
	    + "." + _value.getFractionBits(_precision).toString();
    }

    public String toString(){        
  	return "" + doubleValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicator that quantization has occurred */
    public final Error OVERFLOW   = new Error("Overflow Occurred");   

    /** Indicator that no quantization has occurred */
    public final Error NOOVERFLOW = new Error("No Overflow Occurred");

    /** Indicator that a rounding error has occurred */
    public final Error ROUNDING   = new Error("Rounding Occurred");


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a new Fixpoint number with value equal to the subtraction
     *  of this Fixpoint number and the argument. The operation is
     *  lossless because the precision of the result is changed to
     *  accommodate the result.
     *  @param arg A Fixpoint number.
     *  @return A new Fixpoint number.
     */

    public FixPoint subtract(FixPoint arg ) {
	// Align the precision of the two Fixpoints
	Precision  cp = _precision.matchThePoint(this._precision, arg.getPrecision());
	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY =  arg._alignToPrecision(cp);

	// Determine the new Fixvalue
	Fixvalue argZ =  argX.add( argY.negate() );

	// return the FixPoint with the correct precision and result
        return new FixPoint(cp, argZ);
    }
    /** Prints useful debug information about the Fixpoint. Is used
     *  many for Debug purposes.  */
    public void printFix() {
	System.out.println (" unscale Value  (2) " + 
			    _value.fixvalue.toString(2));
	System.out.println (" unscaled Value (10) " + 
			    _value.fixvalue.toString(10));
	System.out.println (" scale Value (10) " + doubleValue() 
			    + " Precision: " + _precision.toString() );
	System.out.println (" Errors:     " + _value.getErrorDescription());
    }

    /** Return a Fixvalue which fractional part is aligned with the
     *  provided precision. This never involves rounding, but only
     *  padding the fractional part with zeros
     *  @param The precision with which to align 
     *  @return A Fixvalue with aligned precision */
    private Fixvalue _alignToPrecision( Precision p) {

	// Delta is always positive
	int delta = p.getFractionBitLength() -
	    _precision.getFractionBitLength() ;

	// Shift the BigInteger to the left, adding zeros at the end.
	// Therefore the precision is only increased, never decreased.
	BigInteger arg = (_value.fixvalue).shiftLeft(delta);
	
	// return the Fixvalue with aligned value
	return new Fixvalue( arg, _value.getError());
    }

    /** Initialize the Fixpoint */
    private void _initialize() {
	_value     = null;
	_precision = null;
    }    


    /** Returns the maximal obtainable value for the given precision 
	@param p The precision
	@return The maximal value obtainable for the given precision
    */
    private double _findMax(Precision p) 
    {
	int ln = p.getNumberOfBits();
	int ib = p.getIntegerBitLength();
	double tmp = Math.pow(2,ib-1) - 1.0 / Math.pow(2, (ln - ib));
	return tmp;
    }

    /** Returns the minimal obtainable value for the given precision 
	@param p The precision
	@return The minimal value obtainable for the given precision
    */
    private double _findMin(Precision p)
    {
	int ib = p.getIntegerBitLength();
	double tmp = -1*Math.pow(2,ib-1);
	return tmp;
    }

    /**
       Return a Fixvalue for the value and precision given. The value is
       rounded to the nearest value that can be presented with the given
       precision, possibly introducing quantization errors.  
       @param value The value for which to create a Fixpoint
       @param precision The precision of the Fixpoint
       @return A Fixvalue for the value with a given precision
    */
    private Fixvalue _makeBits(double value, Precision precision) {
	BigInteger tmpValue;
	int errors = 0;

	double x = value;
	double maxValue = _findMax(precision);
	double minValue = _findMin(precision);

	Fixvalue fxv = new Fixvalue();

	// check if 'x' falls within the range of this FixPoint with
	// given precision
	if ( x > maxValue ) {
	    fxv.setError(OVERFLOW);
	    x = maxValue;
	}
	if ( x < minValue ) {
	    fxv.setError(OVERFLOW);
	    x = minValue;
	}

	// determine the scale factor by calculating 2^fractionbitlength
	// By multiply the given value 'x' with this scale factor, we get
	// a value of which we drop the fraction part. The integer remaining
	// will be represented by the BigInteger. 
	BigDecimal kl = _twoRaisedTo[precision.getFractionBitLength()].
	    multiply( new BigDecimal( x ));

	// By going from BigDecimal to BigInteger, remove the fraction
	// part introducing a quantization error.
	fxv.fixvalue = new BigInteger( kl.toBigInteger().toByteArray() );

	return fxv;
    }
   

    /////////////////////////////////////////////////////////////////////////
    ////                       private variables                          ////

    /** The precision part of the FixPoint. */
    private Precision _precision;

    /** The Fixvalue containing the BigInteger bit string */
    private Fixvalue  _value;

    //////////////////////////////////////////////////////////////
    /////////                 Innerclass                  ////////

    /** 
	The innerclass Fixvalue encapsulates a BigInteger representing
	the finite number of bits of a Fixpoint together with fields
	that account for rounding errors and rounding mode.  */

    public final class Fixvalue {

	/** Create a Fixvalue with value Zero and set the error field
            to Nooverflow and the overflow mode to Saturate 
	*/
	public Fixvalue() {
	    fixvalue     = BigInteger.ZERO;
	    _error       = NOOVERFLOW;
	}

	/** Create a Fixvalue with value Zero and set the error field
            to Nooverflow and the overflow mode to Saturate 
	    @param value Set the BigInteger of this Fixvale to value
	*/
	public Fixvalue(BigInteger value) {
	    fixvalue     = value;
	    _error       = NOOVERFLOW;
	}

	/** Create a Fixvalue with value Zero and set the error field
            to Nooverflow and the overflow mode to Saturate 
	    @param value Set the BigInteger of this Fixvale to value
	    @param err   The error of this Fixvalue
	*/
	public Fixvalue(BigInteger value, Error err) {
	    fixvalue     = value;
	    _error       = err;

	}

	/** Return a new Fixvalue with value equal to the sum of this
	 *  Fixvalue number and the argument. The addition uses the
	 *  add method of BigInteger. It copies the error and overflow
	 *  fields of this Fixvalue to the new Fixvalue.  
	 *  @param arg A Fixvalue number.
	 *  @return A new Fixvalue number.  
	 */
        public Fixvalue add(Fixvalue aValue ) {
	    BigInteger result = fixvalue.add( aValue.fixvalue );
	    return new Fixvalue(result, _error);
	}

	/** Return a new Fixvalue with value equal to the
	 *  multiplication of this Fixvalue number and the
	 *  argument. Uses the multiply method of BigInteger. The
	 *  resulting Fixvalue contains the error and overflow fields
	 *  of this Fixvalue.  
	 *  @param arg A Fixvalue number.  
	 *  @return A new Fixvalue.  
	 */
	public Fixvalue multiply(Fixvalue aValue ) {
	    BigInteger result = fixvalue.multiply( aValue.fixvalue );
	    return new Fixvalue(result, _error);
	}

	/** Return the negated value of this Fixvalue. Uses the negate
	 *  method of BigInteger.  
	 *  @return A new Fixvalue. 
	 */
 	public Fixvalue negate() {
 	    BigInteger result = fixvalue.negate();
 	    return new Fixvalue(result, _error);
	}

	/** Return only the fractional part of the Fixvalue. Because
	 *  the BigInteger does not have a point, we have to supply
	 *  the precision of the fixvalue, to extract the correct
	 *  number of bits for the fractional part.
	 *  @param precision Precision of the fixvalue.  
	 *  @return fractional part of the Fixvalue.  */
	public Fixvalue getFractionBits(Precision precision) {
	    BigInteger tmp = (_twoRaisedTo[precision.getFractionBitLength()]).toBigInteger();
	    BigInteger mask = tmp.subtract( BigInteger.ONE );
	    BigInteger result = fixvalue.and(mask);
	    return new Fixvalue(result, _error);
	}

	/** Return only the integer part of the Fixvalue. Because
	 *  the BigInteger does not have a point, we have to supply
	 *  the precision of the fixvalue, to extract the correct
	 *  number of bits for the integer part.
	 *  @param precision Precision of the fixvalue.  
	 *  @return integer part of the Fixvalue.  
	 */
    	public Fixvalue getIntegerBits(Precision precision) {
	    BigInteger result = fixvalue.shiftRight(precision.getFractionBitLength());
	    return new Fixvalue(result, _error);
	}

	/** Return a bit string representation of the Fixvalue. The
            representation is a bit string giving the same
            representation on all possible platforms, facilitating a
            more robust testing of Fixpoints.  
	    @return bit string representation of the Fixvalue */
	public String toString() { return fixvalue.toString(2); }

	/** Set the Error of the Fixvalue 
	    @param error The error condition of the Fixvalue
	*/
	public void setError(Error error) { _error = error; }

	/** Get the Error condition from the Fixvalue 
	    @return The error condition of the Fixvalue
	*/
	public Error getError() { return _error; }


	/** Get a description of the error condition of the Fixvalue 
	    @return The description of the error condition of the Fixvalue
	*/
	public String getErrorDescription() { return _error.getDescription(); }

	/////////////////////////////////////////////////////////////////////
	////                      private variables                      ////

	/** The BigInteger representing the finite bit string */
	public  BigInteger fixvalue;

	/** The error condition of the Fixvalue */
	private Error      _error;
	
    }


    /** Instances of this class represent error conditions of the Fixvalue.
     */
    public final class Error {
	// Constructor is private because only Manager instantiates this class.
	private Error(String description) {
	    _description = description;
	}
	
	/** Get a description of the Error.
	 * @return A description of the Error.  */
	public String getDescription() {
	    return _description;
	}
	   
	private String _description;
	
    }

    //////////////////////
    // static class
    //////////////////////

    /** Calculate the table containing 2^x, with 0 < x < 64. Purpose
	is to speed up calculations involving calculating 2^x. The table is
	calculated using BigDecimal, since this make the transformation from
	string of bits to a double easier.  
    */
    private static BigDecimal[] _twoRaisedTo = new BigDecimal[128];

    // Static Class Constructor
    static {
	BigDecimal two = new BigDecimal( "2" );
	BigDecimal p2  = new BigDecimal( "1" );
	for (int i = 0; i <= 64; i++) {
	    _twoRaisedTo[i] = p2;
	    p2 = p2.multiply( two );
	}
    }


}


