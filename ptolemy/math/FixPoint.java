/* A FixPoint data type.

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

@ProposedRating Yellow (kienhuis@eecs.berkeley.edu)
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

This class provides a fixed point data type and a set of functions
that operate on and return fixed point data. An instance of the class
is immutable, meaning that its value is set in the constructor and
cannot then be modified.  This is similar to the Java built-in classes
like Double, Integer, etc.

<p>

In an instance of FixPoint, a value is represented by a finite numbers
of bits, as defined by the precision of the FixPoint. The precision
indicates how many bits are used to represent the integer part and the
fractional part of a FixPoint value. The total number of bits used is
thus equal to the total number of bits used for the integer and
fractional part.  The precision of a FixPoint can be expressed in
different ways.

<ul>

<li> <b>m/n</b> <br> The total bit length of the FixPoint is equal to
<i>m</i> bits and the integer part is equal to <i>n</i> bits. The
fractional part it thus equal to <i>m-n</i> bits.

<li> <b>m.n</b> <br> The total length of the FixPoint is equal to
<i>n+m</i> bits. The integer part is <i>m</i> bits long and the
fractional part is <i>n</i> bits long.

</ul>

<p>

The FixPoint class represents signed numbers. As a consequence, a
single bit is used to represent the sign of the number, i.e where it
is a positive or a negative number. Therefore, when a precision is
given of, for example, <i>(6,3)</i>, then 6 bits are used for the
integer part and 3 are used for the fractional part. Of the 6 integer
bits, 5 are used to represent a number and 1 is used for the sign.

<p>

Because a fixed point data type uses a finite number of bits to
represent a value, a real value is rounded to the nearest number that
can be expressed with a given precision of the fixed point, thereby
introducing quantization and overflow errors.

<p>

In designing the FixPoint class, the main assumption is that all
operators work losslessly, i.e. the precision of the result is changed
such that no precision loss happens. These changes are different for
the various operations.

<p>

When the precision of a fixed point value is change, it might be that
the new precision is less than the required precision to losslessly
represent the fixed point. If the integer number of bits is changed, a
overflow may occur. When the fractional number of bits is changed, a
quantiziation error may occur.

In case an overflow error occurs, it is resolved using two different
overflow mechanism.

<ul>

<li> <B>Saturate</B>: The new fix point is set to the maximum or
minimum value possible, depending on its sign, with the new given
precision.

<li> <B>Zero Saturate</B>: The new fix point is set to zero with the
new given precision.

</ul>

<p>

To create an instance of a FixPoint, one has to use a Quantizer. In
class Quantizer, different quantizers are provided that convert, for
example, a double value or integer value into an instance of
FixPoint. Currently the following quantizers exist:

<ul>

<li> <b>Quantizer.Round</b>: Return a FixPoint that is nearest to the
value that can be represented with the given precision, possibly
introducing quantization/overflow errors.

<li> <b>Quantizer.Truncate</b>: Return a Fixvalue that is the nearest
value towards zero that can be represented with the given precision,
possibly introducing quantization/overflow errors.

</ul>

<p>

The code for this FixPoint implementation is written from scratch. At
its core, it uses the Java class BigInteger to represent the finite
value captured in FixPoint. The use of the BigInteger class, makes
this FixPoint implementation truly platform independent. In some cases
FixPoint also makes use of the BigDecimal class. Note that the
FixPoint does not put any restrictions on the maximal number of bits
used to represent a value.

@author Bart Kienhuis
@version $Id$
@see Precision
@see Quantizer
*/

// FIXME: Indicate and determine what the new precision is going to be.
public class FixPoint implements Cloneable, Serializable {

    /** Construct a FixPoint with a particular precision and a value
     *  given as a BigInteger. To create an instance of a FixPoint,
     *  one has to use a Quantizer. In class Quantizer, different
     *  methods implement various quantizers that convert, for
     *  example, a double value or integer value into an instance of
     *  FixPoint. This constructor is made 'package friendly' because
     *  only the Quantizer class should access this constructor.
     *
     *  @param precision The precision of this FixPoint.
     *  @param value The value that will be represented by
     *  the fixpoint given the finite precision.
     *  @see Quantizer
     */
    FixPoint(Precision precision, BigInteger value) {
 	_initialize();
	try {
	    _precision = precision;
	    _value     = new Fixvalue(value, NOOVERFLOW);
	} catch (IllegalArgumentException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /** Construct a FixPoint with a particular fixed point value and
     *  precision.
     *
     *  @param precision The precision of the fixed point.
     *  @param value The value that represents the fixed point data.
     */
    private FixPoint(Precision precision, Fixvalue value) {
 	_initialize();
 	_precision = precision;
 	_value     =  value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new FixPoint with a value equal to the absolute
     *  value of this fixed point.
     *
     *  @return A non-negative fixed point.
     */
    public final FixPoint abs() {
        return new FixPoint(_precision, _value.fixvalue.abs());
    }


    /** Return a new FixPoint with a value equal to the sum of this
     *  FixPoint and the argument (i.e., this + value). The operation
     *  is lossless because the precision of the result is changed if
     *  needed to accommodate the result. <p>
     *
     *  The precision of the result is equal to the maximum of the
     *  integer part and fractional part of the fixed point values
     *  added. So when a number with precision <i>(6,3)</i> and
     *  <i>(12.4)</i> are added, the resulting precision is equal to
     *  <i>(max(6,12), max(3,4))</i>, which is <i>(12.4)</i>. If the
     *  resulting value doesn't fits into this new precision, then the
     *  precision is changed to accomodate the new value with loss of
     *  precision. For an addition this means that the number of bits
     *  for the integer part might be increased by one. The fractional
     *  part remains unchanged
     *
     *  @param arg A FixPoint.
     *  @return A new FixPoint.
     */
    public FixPoint add(FixPoint arg) {
	// Align the precision of the two FixPoints
	Precision  cp = Precision.matchThePoint(
                this._precision, arg.getPrecision());
	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY =  arg._alignToPrecision(cp);

	// Determine the new Fixvalue
	Fixvalue argZ =  argX.add(argY);

        // Determine the new precision
        Precision newPrecision = cp;

        // The bitlength doesn't include the sign, hence the +1.
        if ( argZ.fixvalue.bitLength() + 1 > cp.getNumberOfBits() ) {
            // Precision change, to integer part + 1
            // fractional part remains the same
            newPrecision = new Precision(cp.getNumberOfBits()+1,
                    cp.getIntegerBitLength()+1);
        }

	// return the FixPoint with the correct precision and result
        return new FixPoint( newPrecision, argZ);
    }

    /** Return a new FixPoint with a value equal to the division of
     *  this FixPoint and the argument (i.e., this / value). The
     *  operation is lossless.
     *
     *  The precision of the result is equal to the maximum of the
     *  integer part and fractional part of the fixed point values
     *  divided. So when a number with precision <i>(6,3)</i> and
     *  <i>(12.4)</i> are added, the resulting precision is equal to
     *  <i>(max(6,12), max(3,4))</i>, which is <i>(12.4)</i>. In case
     *  of a division, the result always fits the new precision,
     *
     *  @param arg A FixPoint.
     *  @return A new FixPoint.
     */
    public FixPoint divide(FixPoint arg) {
	// Align the precision of the two FixPoints
	Precision cp = Precision.matchThePoint(this._precision,
                arg.getPrecision());
	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY = arg._alignToPrecision(cp);

	// To compute the division, we use a trick, we make a
	// BigDecimal from the BigIntegers and use the division
	// operator of BigDecimal
	BigDecimal dx = new BigDecimal(argX.fixvalue);
	BigDecimal dy = new BigDecimal(argY.fixvalue);

        // This division divides two number in a precision of 40
        // decimal behind the point. This is equvalent with a
        // fractional precision of 128 bits. ( ln(1-^40)/ln(2) > 128)
        // Alternatively, we calculated the precision on a
        // time-to-time basis, but this is much more involved.
        // double decimalPrecision =
        // Math.log(Math.pow(2,cp.getFractionBitLength()))/Math.log(10);
	BigDecimal dz = dx.divide(dy, 40, BigDecimal.ROUND_HALF_EVEN);

	// Create a Fixvalue with the additional bits set
	FixPoint result = Quantizer.round(dz, cp);

        // return the new FixPoint
	return result;
    }

    /** Return the value of this FixPoint as a double, which is not
     *  lossless.
     *
     *  @return The double value of this FixPoint.
     */
    public double doubleValue() {
	int ln = _precision.getNumberOfBits();
	int ib = _precision.getIntegerBitLength();

	long h = (_value.fixvalue).longValue();
	double y = h/(_getTwoRaisedTo(ln - ib)).doubleValue();

	return y;
    }

    /** Return the value of this FixPoint as a BigDecimal number,
     *  which is a lossless conversion.
     *
     *  @return The BigDecimal value of this FixPoint.
     */
    public BigDecimal bigDecimalValue() {
	int ln = _precision.getNumberOfBits();
	int ib = _precision.getIntegerBitLength();

	BigDecimal h = new BigDecimal(_value.fixvalue);

        // This division divides two number in a precision of 40
        // decimal behind the point. This is equvalent with a
        // fractional precision of 128 bits. ( ln(1-^40)/ln(2) > 128)
	BigDecimal y = h.divide(_getTwoRaisedTo(ln - ib), 40,
                BigDecimal.ROUND_HALF_EVEN);

	return y;
    }

    /** Return true if this FixPoint is equal to the argument. Two
     *  FixPoints are considered equal when the bitstring representing
     *  the fixed point value of a FixPoint are numerical the same
     *  given the precision of both instances of FixPoint.
     *
     *  @return True if the FixPoints are equal; false otherwise.
     */
    public boolean equals(FixPoint arg) {
	// Align the precision of the two FixPoints
	Precision  cp = Precision.matchThePoint(this._precision,
                arg.getPrecision());
	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY = arg._alignToPrecision(cp);
        return (argX.fixvalue.equals(argY.fixvalue));
    }


    /** Get the error condition of this FixPoint
	@return The error condition
    */
    public Error getError() {
	return _value.getError();
    }

    /** Returns the precision of the FixPoint.
     *  @return the precision of the FixPoint.
     */
    public Precision getPrecision() {
	return _precision;
    }

    /** Return a new FixPoint number with a value equal to the
     *  multiplication of this FixPoint number and the argument (i.e.,
     *  this * value). The operation is lossless because the precision
     *  of the result is changed to accommodate the result.
     *
     *  The precision of the result is equal to the maximum of the
     *  integer parts and 2 times the maximum of the fractional parts
     *  of the fixed point values multiplied. If the new computed
     *  value doesn't fits into this new precision, then this
     *  precision is changed to accomodate the result. For
     *  multiplication this means that the number of bits for the
     *  integer part might be changed between the maximum value of the
     *  integer parts and the sum of the two integer parts.
     *
     *  @param arg A FixPoint number.
     *  @return A new FixPoint number.
     */
    public FixPoint multiply(FixPoint arg) {
	// Align the precision of the two instances of FixPoint.
	Precision  cp = Precision.matchThePoint(this._precision,
                arg.getPrecision());

	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY = arg._alignToPrecision(cp);

        // Determine the number of integer bits used bu the two values
	int intLa = (argX.getIntegerBits(cp)).fixvalue.bitLength();
	int intLb = (argY.getIntegerBits(cp)).fixvalue.bitLength();

	// Determine the new Fixvalue
	Fixvalue argZ =  argX.multiply(argY);

        int fractionalBitLength = cp.getFractionBitLength();
        int totalBitLength = argZ.fixvalue.bitLength();

        // add +1 to account for the sign bit
        int integerBitLength = totalBitLength-2*fractionalBitLength + 1;


	Precision np = null;
        if ( cp.getIntegerBitLength() < integerBitLength ) {
            np = new Precision( totalBitLength + 1, integerBitLength);
        } else {
            // It still fits the given integer bit precision.
            np = new Precision(2*cp.getFractionBitLength() +
                    cp.getIntegerBitLength(), cp.getIntegerBitLength());
        }

	// return the FixPoint with the correct precision and result
        return new FixPoint(np, argZ);
    }



    /** Return a bit string representation, which is a string of "0"
     *  and "1" and a single period, of this fixed point in the form
     *  "<i>integerbits . fractionbits</i>".
     *
     *  @return A bit string of the form "<i>integerbits . fractionbits</i>".
     */
    public String toBitString() {
        // The the toString(2) method of BigInteger removes the most
        // significant bits that are zeros, this method recreates
        // these zero to get the correct representation of the
        // fractional part of a fix point.
        Fixvalue fractionPart = _value.getFractionBits(_precision);
        int num = fractionPart.fixvalue.bitLength();
        int delta = _precision.getFractionBitLength() - num;
        String ln = _value.getIntegerBits(_precision).toString();
        if (_precision.getFractionBitLength() > 0) {
            ln +=  ".";
            // Append the zeros
            for(int i = 0; i < delta; i++) {
                ln += "0";
            }
            if (num > 0) {
                ln += _value.getFractionBits(_precision).toString();
            }
        }
        return ln;
    }

    /** Return a string representation of the value of this FixPoint,
     *  which is the double value of this FixPoint.
     *
     * @return A string representation of the value of this FixPoint.
     */
    public String toString() {
  	return "" + doubleValue();
    }

    /** Set the error of the FixPoint. This method is made 'package
     *  friendly' because only the Quantizer class should access this
     *  method to set an error condition on a FixPoint.
     *
     *  @param error The error condition of the FixPoint.
     */
    void setError(Error error) {
        _value.setError(error);
    }

    /** Return a new FixPoint number with a value equal to this
     *  FixPoint number subtracted by the argument (i.e., this -
     *  value). The operation is lossless because the precision of the
     *  result is changed if needed to accommodate the result. <p>
     *
     *  The precision of the result is equal to the maximum of the
     *  integer part and fractional part of the fixed point values
     *  subtracted. So when a number with precision <i>(6,3)</i> and
     *  <i>(12.4)</i> are subtracted, the resulting precision is
     *  equal to <i>(max(6,12), max(3,4))</i>, which is
     *  <i>(12.4)</i>. If the resulting value doesn't fits into this
     *  new precision, then the precision is changed to accomodate the
     *  new value with loss of precision. For a subtraction this means
     *  that the number of bits for the integer part might be
     *  increased by one, The fractional part remains unchanged.
     *
     *  @param arg A FixPoint number.
     *  @return A new FixPoint number.
     */
    public FixPoint subtract(FixPoint arg) {
	// Align the precision of the two FixPoints
	Precision  cp = Precision.matchThePoint(this._precision, arg.getPrecision());
	Fixvalue argX = this._alignToPrecision(cp);
	Fixvalue argY =  arg._alignToPrecision(cp);

	// Determine the new Fixvalue
	Fixvalue argZ =  argX.add(argY.negate());

        // Determine the new precision
        Precision newPrecision = cp;
        // The bitlength doesn't include the sign, hence the +1.
        if ( argZ.fixvalue.bitLength() + 1 > cp.getNumberOfBits() ) {
            // Precision change, to integer part + 1
            // fractional part remains the same
            newPrecision = new Precision(cp.getNumberOfBits()+1,
                    cp.getIntegerBitLength()+1);
        }

	// return the FixPoint with the correct precision and result
        return new FixPoint(newPrecision, argZ);
    }

    /** Prints useful debug information about the FixPoint to standard
     *  out. This is used mainly for debug purposes.
     */
    public void printFix() {
	System.out.println (" unscale Value  (2) " +
                _value.fixvalue.toString(2));
	System.out.println (" unscaled Value (10) " +
                _value.fixvalue.toString(10));
	System.out.println (" scale Value (10) " + doubleValue()
                + " Precision: " + _precision.toString());
	System.out.println (" Errors:     " +
                _value.getError().getDescription());
	System.out.println (" BitCount:   " + _value.fixvalue.bitCount());
	System.out.println (" BitLength   " + _value.fixvalue.bitLength());
        BigInteger j = _value.fixvalue.abs();
	System.out.println (" ABS value   " + j.toString(2));
	System.out.println (" ABS bit count:  " + j.bitCount());
	System.out.println (" ABD bitLength:  " + j.bitLength());
        System.out.println (" Max value:  " +
                _precision.findMaximum().doubleValue());
	System.out.println (" Min value:  " +
                _precision.findMinimum().doubleValue());
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
     *  @return A Fixvalue with aligned precision
     */
    private Fixvalue _alignToPrecision(Precision p) {

	// Delta is always positive
	int delta = p.getFractionBitLength() -
	    _precision.getFractionBitLength() ;

	// Shift the BigInteger to the left, adding zeros at the end.
	// Therefore the precision is only increased, never decreased.
	BigInteger arg = (_value.fixvalue).shiftLeft(delta);

	// return the Fixvalue with aligned value
	return new Fixvalue(arg, _value.getError());
    }

    /** Initialize the FixPoint */
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
    // Package friendly, is used by Quantizer
    static FixPoint _scaleBits(FixPoint value,
            Precision newprecision, int mode) {

	int delta, a, b = 0;

	Fixvalue intResult;
	Fixvalue fractionResult;

        // Get the absolute value of the FixPoint. In this case
        // The rounding of the fractional part always goes toward
        // zero.
        // Remember the sign.
        int sign = value._value.fixvalue.signum();
        Fixvalue absValue = value._value.abs();

        Precision oldprecision = value.getPrecision();
	Fixvalue integerPart  = absValue.getIntegerBits(oldprecision);
	Fixvalue fractionPart = absValue.getFractionBits(oldprecision);

        // The FixPoint should fit between the min/max of the
        // new supplied precision. Only the fractional part can
        // become smaller. This is checked here.

        // Check Fractional Part
        a = oldprecision.getFractionBitLength();
        b = newprecision.getFractionBitLength();
        delta = b-a;

        // scale the fractional part
        fractionResult = fractionPart.scaleLeft(delta);

	// Reconstruct a single FixPoint from the separate integer and
	// fractional part
	BigInteger total =
	    integerPart.fixvalue.shiftLeft(
                    newprecision.getFractionBitLength());
	total = total.add(fractionResult.fixvalue);

        FixPoint result = null;
	// Return the Fixvalue cast to the new precision
        if (sign >= 0) {
            result = new FixPoint(newprecision, total);
        } else {
            result = new FixPoint(newprecision, total.negate());
        }
        result.setError(fractionResult.getError());

        return result;
    }

    /** Get the BigDecimal which is the 2^exponent. If the value is
     *  already calculated, return this cached value, else calculate
     *  the value.
     *
     *  @param number the exponent.
     *  @return the BigDecimal representing 2^exponent.
     */
    private BigDecimal _getTwoRaisedTo(int number) {
        if ( number <= 128 || number >= 0 ) {
            return _twoRaisedTo[number];
        } else {
            BigInteger two = _two.toBigInteger();
            return new BigDecimal( two.pow( number ) );
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

    /** The innerclass Fixvalue encapsulates a BigInteger representing
	the finite number of bits of a FixPoint together with fields
	that account for rounding or overflow errors.
    */

    private final class Fixvalue {

	/** Create a Fixvalue with value Zero and set the error field
            such to indicate that no overflow took place.
	*/
	public Fixvalue() {
	    fixvalue     = BigInteger.ZERO;
	    _error       = NOOVERFLOW;
	}

	/** Create a Fixvalue with a particular valie and set the error field
            to the given error condition.
	    @param value Set the BigInteger of this Fixvale to value
	    @param err   The error condition of this Fixvalue
	*/
	public Fixvalue(BigInteger value, Error err) {
	    fixvalue     = value;
	    _error       = err;
	}

	/** Return a new Fixvalue with a value equal to the sum of this
	 *  Fixvalue number and the argument. The addition uses the
	 *  add method of class BigInteger. It copies the error field
	 *  of this Fixvalue to the new Fixvalue.
         *  @param arg A Fixvalue number.
         *  @return A new Fixvalue number.
	 */
        public Fixvalue add(Fixvalue aValue) {
	    BigInteger result = fixvalue.add(aValue.fixvalue);
	    return new Fixvalue(result, _error);
	}

	/** Return a new Fixvalue with a value equal to the
	 *  multiplication of this Fixvalue number and the
	 *  argument. Uses the multiply method of class
	 *  BigInteger. The resulting Fixvalue copies the error
	 *  field of this Fixvalue.
         *  @param arg A Fixvalue number.
	 *  @return A new Fixvalue.
	 */
	public Fixvalue multiply(Fixvalue aValue) {
	    BigInteger result = fixvalue.multiply(aValue.fixvalue);
	    return new Fixvalue(result, _error);
	}

	/** Return the negated value of this Fixvalue. Uses the negate
	 *  method of class BigInteger. The resulting Fixvalue copies
	 *  the error field of this Fixvalue.
         *  @return A new Fixvalue.
	 */
 	public Fixvalue negate() {
 	    BigInteger result = fixvalue.negate();
 	    return new Fixvalue(result, _error);
	}

	/** Return the absolute value of this Fixvalue. Uses the
	 *  absolute method of class BigInteger. The resulting
	 *  Fixvalue copies the error field of this Fixvalue.
         *  @return  A new Fixvalue.
	 */
 	public Fixvalue abs() {
 	    BigInteger result = fixvalue.abs();
 	    return new Fixvalue(result, _error);
	}

	/** Get the Error condition from the Fixvalue.
	    @return The error condition of the Fixvalue.
	*/
	public Error getError() { return _error; }

	/** Return only the fractional part of the Fixvalue. Because a
	 *  BigInteger number does not have the notion of a point, the
	 *  precision of the fixvalue has to be supplied, to extract
	 *  the correct number of bits for the fractional part.
	 *  @param precision Precision of the fixvalue.
         *  @return fractional part of the Fixvalue.
         */
	public Fixvalue getFractionBits(Precision precision) {
	    BigInteger tmp = (_getTwoRaisedTo(
                    precision.getFractionBitLength())).toBigInteger();
	    BigInteger mask = tmp.subtract(BigInteger.ONE);
	    BigInteger result = fixvalue.and(mask);
	    return new Fixvalue(result, _error);
	}

	/** Return only the integer part of the Fixvalue. Because a
	 *  BigInteger number does not have the notion of a point, the
	 *  precision of the fixvalue has to be supply, to extract the
	 *  correct number of bits for the integer part.
         *  @param precision Precision of the fixvalue.
         *  @return integer part of the Fixvalue.
	 */
    	public Fixvalue getIntegerBits(Precision precision) {
	    BigInteger result =
                fixvalue.shiftRight(precision.getFractionBitLength());
	    return new Fixvalue(result, _error);
	}

	/** Return a bit string representation of the Fixvalue. The
         *  representation is a bit string giving the same
         *  representation on all possible platforms, facilitating a
         *  more robust testing of isntances of FixPoint.
         *
 	 *  @return bit string representation of the Fixvalue
         */
	public String toString() { return fixvalue.toString(2); }

        /** Return a scaled Fixvalue by scaling this fixvalue. Scale
         *  the fixvalue is done by truncating delta bits from the
         *  lefthand side. In case delta<0, no truncation will occurs
         *  and thus no overflow error occurs. If delta>0, then the
         *  fixvalue is truncated by doing an AND on the result
         *  stripping bits to fit the precision. This always leads to
         *  rounding.
         *  @param delta Number of positions the fixvalue
         *  is scaled from right.
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
                    _getTwoRaisedTo(length-delta).toBigInteger().
                    subtract(BigInteger.ONE);
                // AND the fixvalue with the MASK.
                result.fixvalue = work.fixvalue.and(mask);
            } else {
                result.fixvalue = work.fixvalue;
            }
            // Keep the error status of this fixvalue.
            result.setError(work.getError());
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
            if (delta < 0) {

                // Check if last delta bits are zero
                // Because then no rounding takes place
                for(int i = 0; i < -delta; i++){
                    if (work.fixvalue.testBit(i) == true) {
                        work.setError(ROUNDING);
                    }
                }
                result.fixvalue = work.fixvalue.shiftLeft(delta);
            } else {
                result.fixvalue = work.fixvalue.shiftLeft(delta);
            }
            result.setError(work.getError());
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


    /** Instances of this class represent a type safe enumeration of
     *  error conditions of the Fixvalue.
     */
    protected static class Error {
	// Constructor is private because only Manager instantiates this class.
	private Error(String description) {
	    _description = description;
	}

	/** Get a description of the Error.
	 * @return A description of the Error.  */
	public String getDescription() {
	    return " " + _description;
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

    /** Static reference to the BigDecimal representation of two. */
    private static BigDecimal _two = new BigDecimal("2");

    /** Static reference to the BigDecimal representation of one. */
    private static BigDecimal _one  = new BigDecimal("1");

    // Static Class Constructor
    static {
	BigDecimal two = _two;
	BigDecimal p2  = _one;
	for (int i = 0; i <= 64; i++) {
	    _twoRaisedTo[i] = p2;
	    p2 = p2.multiply(two);
	}
    }
}
