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
one casts a fix point result into another fix point with less
precision. In that case, the rounding can occur and is resolved using
different Overflow mechanisms.

<ul>

<li> <B>Saturate</B>: The new fix point is set to the maximum or
minimum value possible, depending on its sign, possible with the new
given precision.

<li> <B>Zero Saturate</B>: The new fix point is set to zero.

</ul>

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
enumerations of the state a <i>Fixpoint</i> resides. The Quantize
innerclass is used to get a type safe enumeration of the different
modes of rounding.

@author Bart Kienhuis
@version $Id$ */

public final class FixPoint implements Cloneable, Serializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a FixPoint with a particular precision and a value
     *  given as a BigInteger. The value of this Fixpoint will be rounded
     *  to the nearest Fixpoint value given the finite precision. When
     *  the value does not fit the given precision, a rounding error
     *  will occur and the value of the Fixpoint will be saturated to
     *  the largest positive or largest negative value possible given
     *  the precision.
     *  @param precision The precision of this Fixpoint.
     *  @param value The value that will be represented by
     *  the fixpoint given the finite precision.  */
    public FixPoint(Precision precision, BigInteger value ) {
 	_initialize();
	try {
	    _precision = precision;
	    _value     = new Fixvalue( value );
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

    /** Return the absolute value of this fix point number.
     *  @return A non-negative fix point.
     */
    public final FixPoint absolute() {
        return new FixPoint( _precision, _value.fixvalue.abs());
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
	FixPoint result = Quantizer.round( dz.doubleValue(), cp );

        // return the new FixPoint
	return result;
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

    /** Return true if this fix point number is equal to those of the
     *  argument.
     *  @return True if the fix points are equal; false otherwise.
     */
    public final boolean equals(FixPoint arg) {
	// Align the precision of the two Fixpoints
	Precision  cp = _precision.matchThePoint(this._precision,
						 arg.getPrecision());
	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY = arg._alignToPrecision(cp);

        return ( argX.fixvalue.equals( argY.fixvalue) );
    }


    /** Get a description of the error condition of the Fixpoint
	@return The description of the error condition of the Fixpoint
    */
    public String getErrorDescription() {
	return " " + _value.getErrorDescription();
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

        // Determine the precision of the result
	int intLa = (argX.getIntegerBits(cp)).fixvalue.bitLength();
	int intLb = (argY.getIntegerBits(cp)).fixvalue.bitLength();
	Precision np = new Precision( 2*cp.getFractionBitLength() + intLa+intLb, intLa+intLb);

	// return the FixPoint with the correct precision and result
        return new FixPoint(np, argZ);
    }



    /** Return a bit string representation of the fix value of this
        Fixpoint in the form "<i>integerbits . fractionbits</i>. Since
        the toString method of BigInteger always removes the most
        significant bits that are zero, this method has to recreate
        and append these zero to get the correct representation of the
        fractional part of a fix point.
        @return A bit string of the form "<i>integerbits . fractionbits</i>".
     */
    public String toBitString() {
        Fixvalue fractionPart = _value.getFractionBits( _precision );
        int num = fractionPart.fixvalue.bitLength();
        int delta = _precision.getFractionBitLength() - num;
        String ln = _value.getIntegerBits(_precision).toString();
        //System.err.println(" Num: " + num );
        //System.err.println(" REAL: " + _precision.getFractionBitLength() );
        //System.err.println(" Delta: " + delta );
        if ( _precision.getFractionBitLength() > 0 ) {
            ln +=  ".";
            // Append the zeros
            for(int i=0; i<delta; i++ ) {
                ln += "0";
            }
            if ( num > 0 ) {
                ln += _value.getFractionBits(_precision).toString();
            }
        }
        return ln;
    }

    /** Return a string representation of the value of this Fixpoint.
     * @return A string representation of the value of this Fixpoint
     */
    public String toString(){
  	return "" + doubleValue();
    }

   /**  Return a new Fixpoint number scaled to the give precision. To
     *  fit the new precision, a rounding error can occur. In that
     *  case the value of the Fixpoint is determined, depending on the
     *  quanitzation mode selected.
     *
     *  <ul>
     *  <li> mode = 0, <b>Saturate</b>: The fix point value is set,
     *  depending on its sign, equal to the Max or Min value possible
     *  with the new given precision.
     *  <li> mode = 1, <b>Zero Saturate</b>: The fix point value is
     *  set equal to zero.
     *  </ul>

     *  @param newprecision The new precision of the Fixpoint.
     *  @param mode The mode of quantization.
     *  @return A new Fixpoint with the given precision.
     */
    public FixPoint scaleToPrecision(Precision newprecision, int mode ) {

        double maxValue = newprecision.findMax();
        double minValue = newprecision.findMin();

        double value = this.doubleValue();

        if ( minValue < value && value < maxValue ) {
            Fixvalue newvalue =
                _scaleBits(_value, _precision, newprecision, mode );
            return new FixPoint(newprecision, newvalue);
        } else {

            FixPoint result;

	    // Check how to resolve the rounding of the fractional part
	    switch( mode ) {
	    case 0: //SATURATE

                if ( _value.fixvalue.signum() >= 0 ) {
                    result = Quantizer.round( maxValue, newprecision);
                } else {
                    result = Quantizer.round( minValue, newprecision);
                }
                result.setError( OVERFLOW );
                return result;

	    case 1: //ZERO_SATURATE:
                result = new FixPoint( newprecision, BigInteger.ZERO );
                result.setError( OVERFLOW );
                return result;

            default:
                throw new IllegalArgumentException("Illegal Mode of " +
                        "Rounding Selected");
            }
        }
    }

    /** Set the Error of the FixPoint
        @param error The error condition of the FixPoint
    */
    public void setError(Error error) {
        _value.setError( error );
    }

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
	System.out.println (" BitCount:   " + _value.fixvalue.bitCount());
	System.out.println (" BitLength   " + _value.fixvalue.bitLength());
        BigInteger j = _value.fixvalue.abs();
	System.out.println (" ABS value   " + j.toString(2) );
	System.out.println (" ABS bit count:  " + j.bitCount());
	System.out.println (" ABD bitLength:  " + j.bitLength());
        System.out.println (" Max value:  " + _precision.findMax());
	System.out.println (" Min value:  " + _precision.findMin());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////


    /** Indicator that no overflow has occurred */
    public static final Error OVERFLOW   = new Error("Overflow Occurred");

    /** Indicator that an overflow has occurred */
    public static final Error NOOVERFLOW = new Error("No overflow Occurred");

    /** Indicator that a rounding error has occurred */
    public static final Error ROUNDING   = new Error("Rounding Occurred");

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


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
	return new Fixvalue( arg, _value.getError() );
    }

    /** Initialize the Fixpoint */
    private void _initialize() {
	_value     = null;
	_precision = null;
    }

    /** Returns a Fixvalue which is a copy of the supplied Fixvalue,
	but with it's precision scaled from the old precision to the
	new precision. If the new Fixvalue cannot be contained by the
	new precision, a rounding error occurs and depending on the
	quantization mode selected, the appropriate Fixvalue is
	determined.
	@param x Fixvalue that needs to be scaled
	@param oldprecision The old precision of the Fixvalue
	@param newprecision The new precision of the Fixvalue
	@return Fixvalue with the desired new precision
    */
    private Fixvalue _scaleBits(Fixvalue x, Precision oldprecision,
            Precision newprecision, int mode ) {

	int delta, a, b = 0;

	Fixvalue intResult;
	Fixvalue fractionResult;

        // Get the absolute value of the FixPoint. In this case
        // The rounding of the fractional part always goes toward
        // zero.
        // Remember the sign.
        int sign = x.fixvalue.signum();
        Fixvalue absValue = x.abs();

	Fixvalue integerPart  = absValue.getIntegerBits( oldprecision );
	Fixvalue fractionPart = absValue.getFractionBits( oldprecision );

        // The FixPoint should fit between the min/max of the
        // new supplied precision. Only the fractional part can
        // become smaller. This is checked here.

        // Check Fractional Part
        a = oldprecision.getFractionBitLength();
        b = newprecision.getFractionBitLength();
        delta = b-a;

        // scale the fractional part
        fractionResult = fractionPart.scaleLeft(delta);

	// Reconstruct a single Fixpoint from the separate integer and
	// fractional part
	BigInteger total =
	    integerPart.fixvalue.shiftLeft(
                    newprecision.getFractionBitLength());
	total = total.add( fractionResult.fixvalue );

	// Return the Fixvalue cast to the new precision
        if ( sign >= 0 ) {
            //System.out.println(" -- RETURN a positive Fixvalue -- ");
            return new Fixvalue( total, fractionResult.getError());
        } else {
            //System.out.println(" -- RETURN a negative Fixvalue -- ");
            return new Fixvalue( total.negate(), fractionResult.getError());
        }
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
            to NOOVERFLOW and the quanitzation mode to Saturate.
	*/
	public Fixvalue() {
	    fixvalue     = BigInteger.ZERO;
	    _error       = NOOVERFLOW;
	}

	/** Create a Fixvalue with value Zero and set the error field
            to NOOVERFLOW and the overflow mode to SATURATE.
	    @param value Set the BigInteger of this Fixvale to value
	*/
	public Fixvalue(BigInteger value) {
	    fixvalue     = value;
	    _error       = NOOVERFLOW;
	}

	/** Create a Fixvalue with value Zero and set the error field
            to NOOVERFLOW and the overflow mode to SATURATE.
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

	/** Return the absoluate value of this Fixvalue. Uses the absolute
	 *  method of BigInteger.
	 *  @return A new Fixvalue.
	 */
 	public Fixvalue abs() {
 	    BigInteger result = fixvalue.abs();
 	    return new Fixvalue(result, _error);
	}

	/** Get the Error condition from the Fixvalue
	    @return The error condition of the Fixvalue
	*/
	public Error getError() { return _error; }

	/** Get a description of the error condition of the Fixvalue
	    @return The description of the error condition of the Fixvalue
	*/
	public String getErrorDescription() { return _error.getDescription(); }


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
         *  representation is a bit string giving the same
         *  representation on all possible platforms, facilitating a
         *  more robust testing of Fixpoints.
 	 *  @return bit string representation of the Fixvalue
         */
	public String toString() { return fixvalue.toString(2); }

        /** Return a scaled Fixvalue by scaling this fixvalue. Scale
         *  the fixvalue is done by truncating delta bits from the
         *  lefthand side. In case delta<0, no truncation will occurs
         *  and thus no overflow error occurs. If delta>0, then the
         *  fixvalue is truncated by doing an AND on the result which
         *  always leads to rounding.
         *  @param delta Number of positions the fixvalue is scaled
         *  from right.
         *  @return A scaled Fixvalue.
         */
        public Fixvalue scaleRight(int delta) {
            Fixvalue work = new Fixvalue(fixvalue, _error);
            Fixvalue result = new Fixvalue();
            int length = work.fixvalue.bitLength();
            if (delta>0) {
                work.setError(OVERFLOW);
                // Create the MASK for truncating the fixvalue
                BigInteger mask =
                    _twoRaisedTo[length-delta].toBigInteger().
                    subtract( BigInteger.ONE );
                // AND the fixvalue with the MASK.
                result.fixvalue = work.fixvalue.and(mask);
            } else {
                result.fixvalue = work.fixvalue;
            }
            // Keep the error status of this fixvalue.
            result.setError( work.getError() );
            return result;
        }

        /** Return a scaled Fixvalue by scaling this fixvalue. Scale
         *  the fixvalue by shifting it delta positions from the
         *  left hand side. If delta<0, then the fixvalue is reduced in
         *  length leading to possible rounding. Select on the basis
         *  of the overflow mode what the final Fixvalue should look
         *  like.
         *  @param delta Number of positions the fixvalue is
         *  scaled from left.
         *  @return A scaled Fixvalue.
         */
        public Fixvalue scaleLeft(int delta) {
            // copy the previous fixvalue
            Fixvalue work = new Fixvalue(fixvalue, _error);
            Fixvalue result = new Fixvalue();

            // Delta >0, shift Left
            // Delta <0, shift Right
            if ( delta < 0) {

                // Check if last delta bits are zero
                // Because then no rounding takes place
                for(int i=0;i<-delta;i++){
                    if ( work.fixvalue.testBit( i ) == true ) {
                        work.setError(ROUNDING);
                    }
                }
                result.fixvalue = work.fixvalue.shiftLeft( delta );
            } else {
                result.fixvalue = work.fixvalue.shiftLeft( delta );
            }
            result.setError( work.getError() );
            return result;
        }

	/** Set the Error of the Fixvalue
	    @param error The error condition of the Fixvalue
	*/
	public void setError(Error error) { _error = error; }


	/////////////////////////////////////////////////////////////////////
	////                      private variables                      ////

	/** The BigInteger representing the finite bit string */
	public  BigInteger fixvalue;

	/** The error condition of the Fixvalue */
	private Error      _error;
    }


    /** Instances of this class represent error conditions of the Fixvalue.
     */
    public static class Error {
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


