/* A FixPoint data type.

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

@ProposedRating Yellow (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

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
of bits, as defined by the precision. The precision
indicates how many bits are used to represent the integer part and the
fractional part. The total number of bits used is
thus equal to the total number of bits used for the integer and
fractional part.  The precision of a FixPoint can be expressed in
different ways:
<ul>
<li>
(<i>m</i>/<i>n</i>): The total precision of the output is <i>m</i> bits,
with the integer part having <i>n</i> bits.
The fractional part thus has <i>m</i>-<i>n</i> bits.
<li>
(<i>m</i>.<i>n</i>): The total precision of the output is <i>n</i> + <i>m</i>
 bits, with the integer part having m bits, and the fractional part
having <i>n</i> bits.
</ul>
<p>
The FixPoint class represents signed numbers in a two's-complement
format.
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
When the precision of a fixed point value is changed, it might be that
the new precision is less than the required precision to losslessly
represent the fixed point. If the integer number of bits is changed,
overflow may occur. When the fractional number of bits is changed, a
quantization error may occur.
<p>
Overflows are handled in one of two ways:
<ul>
<li> <B>Saturate</B>: The new fix point is set to the maximum or
minimum value possible, depending on its sign, with the new given
precision.
<li> <B>Zero Saturate</B>: The new fix point is set to zero with the
new given precision.
</ul>
<p>
The Quantizer class provides a number of convenient static methods
for creating an instance of FixPoint. Indeed, the intent is that only
those methods will be used to create instances of FixPoint.  For this
reason, this class has only a package friendly constructor.
<p>
The code for this FixPoint implementation is written from scratch. At
its core, it uses the Java class BigInteger to represent the finite
value captured in FixPoint. The use of the BigInteger class makes
this FixPoint implementation truly platform independent. In some cases
FixPoint also makes use of the BigDecimal class. Note that the
FixPoint does not put any restrictions on the maximum number of bits
used to represent a value.

@author Bart Kienhuis
@contributor Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
@see Precision
@see Quantizer
*/

public class FixPoint implements Cloneable, Serializable {

    /** Construct a FixPoint with a particular precision and a value
     *  given as a BigInteger. To create an instance of a FixPoint,
     *  it is most convenient to use one of the static methods of
     *  the Quantizer class. In class Quantizer, different
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
	    _value     = new FixValue(value, NOOVERFLOW);
	} catch (IllegalArgumentException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /** Construct a FixPoint with a particular fixed point value and
     *  precision.
     *  @param precision The precision of the fixed point.
     *  @param value The value that represents the fixed point data.
     */
    private FixPoint(Precision precision, FixValue value) {
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
        return new FixPoint(_precision, _value.fixValue.abs());
    }

    /** Return a new FixPoint with a value equal to the sum of this
     *  FixPoint and the argument. The operation
     *  is lossless because the precision of the result is set to
     *  accommodate the result.
     *  <p>
     *  The precision of the result is equal to the maximum of the
     *  integer part and fractional part of the fixed point values
     *  added. So when a number with precision <i>(6, 3)</i> and
     *  <i>(12.4)</i> are added, the resulting precision is equal to
     *  <i>(max(6, 12), max(3, 4))</i>, which is <i>(12.4)</i>. If the
     *  resulting value doesn't fits into this new precision, then the
     *  precision is changed to accommodate it. In particular, the number
     *  of bits for the integer part might be increased by one.
     *
     *  @param arg A number to add to this one.
     *  @return The sum of this number and the argument.
     */
    public FixPoint add(FixPoint arg) {
	// Align the precision of the two FixPoints
	Precision  cp = Precision.matchThePoint(
                this._precision, arg.getPrecision());
	FixValue argX = this._alignToPrecision(cp);
	FixValue argY =  arg._alignToPrecision(cp);

	// Determine the new FixValue
	FixValue argZ =  argX.add(argY);

        // Determine the new precision
        Precision newPrecision = cp;

        // The bit length doesn't include the sign, hence the +1.
        if ( argZ.fixValue.bitLength() + 1 > cp.getNumberOfBits() ) {
            // Precision change, to integer part + 1
            // fractional part remains the same
            newPrecision = new Precision(cp.getNumberOfBits()+1,
                    cp.getIntegerBitLength()+1);
        }

	// return the FixPoint with the correct precision and result
        return new FixPoint( newPrecision, argZ);
    }

    /** Return the value of this FixPoint as a BigDecimal number.
     *  This is lossless.
     *  @return The BigDecimal value of this FixPoint.
     */
    public BigDecimal bigDecimalValue() {
	int ln = _precision.getNumberOfBits();
	int ib = _precision.getIntegerBitLength();

	BigDecimal h = new BigDecimal(_value.fixValue);

        // This division divides two number in a precision of 40
        // decimal behind the point. This is equivalent with a
        // fractional precision of 128 bits. ( ln(1-^40)/ln(2) > 128)
	BigDecimal y = h.divide(_getTwoRaisedTo(ln - ib), 40,
                BigDecimal.ROUND_HALF_EVEN);

	return y;
    }

    /** Return a new FixPoint with a value equal to the division of
     *  this FixPoint by the argument. The operation is <b>not</b>
     *  lossless. To realize the division, a trick is used to
     *  simplify the implementation of division for FixPoint
     *  values. The FixPoints are converted losslessly into BigDecimals,
     *  on which the actual division takes place. The result of the
     *  division is converted back to FixPoint with a precision
     *  equal to the maximum of the integer part and fractional part
     *  of the fixed point values divided. Thus when a number with
     *  precision <i>(6, 3)</i> is divided by a FixPoint with precision
     *  <i>(12.4)</i>, then the resulting FixPoint will have a
     *  precision equal to <i>(max(6, 12), max(3, 4))</i>, which is
     *  <i>(12.4)</i>. If the BigDecimal resulting from the division
     *  doesn't fit this precision, then this BigDecimal value is
     *  quantized to the closest value representable with the
     *  specified precision.
     *
     *  @param arg A FixPoint.
     *  @return A new FixPoint.
     */
    public FixPoint divide(FixPoint arg) {
	// Align the precision of the two FixPoints
	Precision cp = Precision.matchThePoint(this._precision,
                arg.getPrecision());
	FixValue argX = this._alignToPrecision(cp);
	FixValue argY = arg._alignToPrecision(cp);

	// To compute the division, we use a trick, we make a
	// BigDecimal from the BigIntegers and use the division
	// operator of BigDecimal
	BigDecimal dx = new BigDecimal(argX.fixValue);
	BigDecimal dy = new BigDecimal(argY.fixValue);

        // This division divides two number in a precision of 40
        // decimal behind the point. This is equivalent with a
        // fractional precision of 128 bits. ( ln(1-^40)/ln(2) > 128)
        // Alternatively, we calculated the precision on a
        // time-to-time basis, but this is much more involved.
        // double decimalPrecision =
        // Math.log(Math.pow(2, cp.getFractionBitLength()))/Math.log(10);
	BigDecimal dz = dx.divide(dy, 40, BigDecimal.ROUND_HALF_EVEN);

	// Create a FixValue with the additional bits set
	FixPoint result = Quantizer.round(dz, cp);

        // return the new FixPoint
	return result;
    }

    /** Return the value of this FixPoint as a double.  Note that this
     *  is not necessarily lossless, since the precision of the fixed point
     *  number may exceed that of the double.
     *  @return The double value of this FixPoint.
     */
    public double doubleValue() {
	int ln = _precision.getNumberOfBits();
	int ib = _precision.getIntegerBitLength();

	long h = (_value.fixValue).longValue();
	double y = h/(_getTwoRaisedTo(ln - ib)).doubleValue();

	return y;
    }

    /** Return true if this FixPoint is equal to the argument. Two
     *  FixPoints are considered equal when the bit string representing
     *  them is the same.
     *  @see #toBitString()
     *  @return True if the FixPoints are equal; false otherwise.
     */
    public boolean equals(FixPoint arg) {
	// Align the precision of the two FixPoints
	Precision  cp = Precision.matchThePoint(this._precision,
                arg.getPrecision());
	FixValue argX = this._alignToPrecision(cp);
	FixValue argY = arg._alignToPrecision(cp);
        return (argX.fixValue.equals(argY.fixValue));
    }

    /** Get the error condition of this FixPoint, which is one of
     *  OVERFLOW, ROUNDING, or NOOVERFLOW indicating that an overflow
     *  error has occurred, a rounding error has occurred, or no
     *  error has occurred.
     *  @return The error condition
     */
    public Error getError() {
	return _value.getError();
    }

    /** Return the precision of this number.
     *  @return the precision of this number.
     */
    public Precision getPrecision() {
	return _precision;
    }

    /** Return a new FixPoint number with a value equal to the
     *  product of this number and the argument.
     *  The operation is lossless because the precision
     *  of the result is set to accommodate the result.
     *  The precision of the result is equal to the maximum of the
     *  integer parts and 2 times the maximum of the fractional parts
     *  of the fixed point values multiplied. If the new computed
     *  value doesn't fit into this new precision, then this
     *  precision is changed to accommodate the result. For
     *  multiplication this means that the number of bits for the
     *  integer part might be changed to lie between the maximum value of the
     *  integer parts and the sum of the two integer parts.
     *
     *  @param arg The number to multiply this number by.
     *  @return A new FixPoint number representing the product.
     */
    public FixPoint multiply(FixPoint arg) {
	// Align the precision of the two instances of FixPoint.
	Precision  cp = Precision.matchThePoint(this._precision,
                arg.getPrecision());

	FixValue argX = this._alignToPrecision(cp);
	FixValue argY = arg._alignToPrecision(cp);

        // Determine the number of integer bits used bu the two values
	int intLa = (argX.getIntegerBits(cp)).fixValue.bitLength();
	int intLb = (argY.getIntegerBits(cp)).fixValue.bitLength();

	// Determine the new FixValue
	FixValue argZ =  argX.multiply(argY);

        int fractionalBitLength = cp.getFractionBitLength();
        int totalBitLength = argZ.fixValue.bitLength();

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

    /** Print useful debug information about the FixPoint to standard
     *  out. This is used for debugging.
     */
    public void printFix() {
	System.out.println (" unscale Value  (2) " +
                _value.fixValue.toString(2));
	System.out.println (" unscaled Value (10) " +
                _value.fixValue.toString(10));
	System.out.println (" scale Value (10) " + doubleValue()
                + " Precision: " + _precision.toString());
	System.out.println (" Errors:     " +
                _value.getError().getDescription());
	System.out.println (" BitCount:   " + _value.fixValue.bitCount());
	System.out.println (" BitLength   " + _value.fixValue.bitLength());
        BigInteger j = _value.fixValue.abs();
	System.out.println (" ABS value   " + j.toString(2));
	System.out.println (" ABS bit count:  " + j.bitCount());
	System.out.println (" ABD bitLength:  " + j.bitLength());
        System.out.println (" Max value:  " +
                _precision.findMaximum().doubleValue());
	System.out.println (" Min value:  " +
                _precision.findMinimum().doubleValue());
    }

    /** Return a new FixPoint number with a value equal to this
     *  number minus the argument.
     *  The operation is lossless because the precision of the
     *  result is set to accommodate the result.
     *  The precision of the result is equal to the maximum of the
     *  integer part and fractional part of the fixed point values
     *  subtracted. So when a number with precision <i>(6, 3)</i> and
     *  <i>(12.4)</i> are subtracted, the resulting precision is
     *  equal to <i>(max(6, 12), max(3, 4))</i>, which is
     *  <i>(12.4)</i>. If the resulting value doesn't fit into this
     *  new precision, then the precision is changed to accommodate the
     *  new value without loss of precision. For a subtraction this means
     *  that the number of bits for the integer part might be
     *  increased by one, The fractional part remains unchanged.
     *
     *  @param arg The number to subtract from this number.
     *  @return This number minus the argument.
     */
    public FixPoint subtract(FixPoint arg) {
	// Align the precision of the two FixPoints
	Precision  cp = Precision.matchThePoint(this._precision, arg.getPrecision());
	FixValue argX = this._alignToPrecision(cp);
	FixValue argY =  arg._alignToPrecision(cp);

	// Determine the new FixValue
	FixValue argZ =  argX.add(argY.negate());

        // Determine the new precision
        Precision newPrecision = cp;
        // The bit length doesn't include the sign, hence the +1.
        if ( argZ.fixValue.bitLength() + 1 > cp.getNumberOfBits() ) {
            // Precision change, to integer part + 1
            // fractional part remains the same
            newPrecision = new Precision(cp.getNumberOfBits()+1,
                    cp.getIntegerBitLength()+1);
        }

	// return the FixPoint with the correct precision and result
        return new FixPoint(newPrecision, argZ);
    }


    /** Return a bit string representation of this fixed point in the form
     *  "<i>integerBits . fractionBits</i>", where <i>integerBits</i> and
     *  <i>fractionBits</i> are each a sequence of "0" and "1".
     *  @return A bit string of the form "<i>integerBits . fractionBits</i>".
     */
    public String toBitString() {
        // The the toString(2) method of BigInteger removes the most
        // significant bits that are zeros, this method recreates
        // these zero to get the correct representation of the
        // fractional part of a fix point.
        FixValue fractionPart = _value.getFractionBits(_precision);
        int num = fractionPart.fixValue.bitLength();
        int delta = _precision.getFractionBitLength() - num;
        String ln = _value.getIntegerBits(_precision).toString();
        if (_precision.getFractionBitLength() > 0) {
            ln +=  ".";
            // Append the zeros
            for (int i = 0; i < delta; i++) {
                ln += "0";
            }
            if (num > 0) {
                ln += _value.getFractionBits(_precision).toString();
            }
        }
        return ln;
    }

    /** Return a string representation of the value of this number.
     *  This is calculated by first converting the number to a double,
     *  and then returning a string representation of the double.
     * @return A string representation of the value of this FixPoint.
     */
    public String toString() {
        // FIXME: This is really not the right thing to do.
        // We should give a precise string representation.
  	return "" + doubleValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicator that no overflow has occurred */
    public static final Error OVERFLOW   = new Error("Overflow occurred");

    /** Indicator that an overflow has occurred */
    public static final Error NOOVERFLOW = new Error("No overflow occurred");

    /** Indicator that a rounding error has occurred */
    public static final Error ROUNDING   = new Error("Rounding occurred");

    ///////////////////////////////////////////////////////////////////
    ////                         friendly methods                  ////

    /** Return a value which is a copy of the supplied value,
     *  but with it's precision scaled from the old precision to the
     *  new precision. If the new value cannot be contained by the
     *  new precision, a rounding error occurs and depending on the
     *  quantization mode selected, the appropriate value is
     *  determined.
     *  @param value The value that needs to be scaled.
     *  @param newPrecision The new precision.
     *  @return Value with the desired new precision.
     */
    static FixPoint _scaleBits(
            FixPoint value, Precision newPrecision, int mode) {
        // Package friendly, is used by Quantizer
	int delta, a, b = 0;

	FixValue intResult;
	FixValue fractionResult;

        // Get the absolute value of the FixPoint. In this case
        // The rounding of the fractional part always goes toward
        // zero.
        // Remember the sign.
        int sign = value._value.fixValue.signum();
        FixValue absValue = value._value.abs();

        Precision oldPrecision = value.getPrecision();
	FixValue integerPart  = absValue.getIntegerBits(oldPrecision);
	FixValue fractionPart = absValue.getFractionBits(oldPrecision);

        // The FixPoint should fit between the min/max of the
        // new supplied precision. Only the fractional part can
        // become smaller. This is checked here.

        // Check Fractional Part
        a = oldPrecision.getFractionBitLength();
        b = newPrecision.getFractionBitLength();
        delta = b-a;

        // scale the fractional part
        fractionResult = fractionPart.scaleLeft(delta);

	// Reconstruct a single FixPoint from the separate integer and
	// fractional part
	BigInteger total =
	    integerPart.fixValue.shiftLeft(
                    newPrecision.getFractionBitLength());
	total = total.add(fractionResult.fixValue);

        FixPoint result = null;
	// Return the FixValue cast to the new precision
        if (sign >= 0) {
            result = new FixPoint(newPrecision, total);
        } else {
            result = new FixPoint(newPrecision, total.negate());
        }
        result.setError(fractionResult.getError());

        return result;
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a FixValue which fractional part is aligned with the
     *  provided precision. This never involves rounding, but only
     *  padding the fractional part with zeros
     *  @param The precision with which to align
     *  @return A FixValue with aligned precision
     */
    private FixValue _alignToPrecision(Precision p) {

	// Delta is always positive
	int delta = p.getFractionBitLength() -
	    _precision.getFractionBitLength() ;

	// Shift the BigInteger to the left, adding zeros at the end.
	// Therefore the precision is only increased, never decreased.
	BigInteger arg = (_value.fixValue).shiftLeft(delta);

	// return the FixValue with aligned value
	return new FixValue(arg, _value.getError());
    }

    /** Initialize the FixPoint */
    private void _initialize() {
	_value     = null;
	_precision = null;
    }


    /** Get the BigDecimal which is the 2^exponent. If the value is
     *  already calculated, return this cached value, else calculate
     *  the value.
     *
     *  @param number the exponent.
     *  @return the BigDecimal representing 2^exponent.
     */
    private BigDecimal _getTwoRaisedTo(int number) {
        if ( number <= _twoRaisedTo.length && number >= 0 ) {
            return _twoRaisedTo[number];
        } else {
            BigInteger two = _two.toBigInteger();
            return new BigDecimal( two.pow( number ) );
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The precision part of the FixPoint. */
    private Precision _precision;

    /** Static reference to the BigDecimal representation of one. */
    private static BigDecimal _one  = new BigDecimal("1");

    /** The size of the pre-computed _twoRaisedTo powers of two array.
    *   65 entries are used to cache all powers of 2 from 0 to 64.
    **/
    private static final int TWORAISEDTOSIZE = 64+1;

    /** Calculate the table containing 2^x, with 0 < x < TWORAISEDTOSIZE.
     *   Purpose is to speed up calculations involving calculating
     *   2^x. The table is calculated using BigDecimal, since this
     *   make the transformation from string of bits to a double
     *   easier.
     **/
    private static BigDecimal[] _twoRaisedTo = new BigDecimal[TWORAISEDTOSIZE];

    /** Static reference to the BigDecimal representation of two. */
    private static BigDecimal _two = new BigDecimal("2");

    /** The FixValue containing the BigInteger bit string */
    private FixValue  _value;


    ///////////////////////////////////////////////////////////////////
    ////                       static initializers                       ////

    static {
	BigDecimal two = _two;
	BigDecimal powerOf2  = _one;
	for (int i = 0; i < _twoRaisedTo.length; i++) {
	    _twoRaisedTo[i] = powerOf2;
	    powerOf2 = powerOf2.multiply(two);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The innerclass FixValue encapsulates a BigInteger representing
     *  the finite number of bits of a FixPoint together with fields
     *  that account for rounding or overflow errors.
     */
    private final class FixValue {

	/** Create a FixValue with value Zero and set the error field
         *  such to indicate that no overflow took place.
         */
	public FixValue() {
	    fixValue     = BigInteger.ZERO;
	    _error       = NOOVERFLOW;
	}

	/** Create a FixValue with a particular value and set the error field
         *  to the given error condition.
         *  @param value Set the BigInteger of this FixValue to value
         *  @param err   The error condition of this FixValue
         */
	public FixValue(BigInteger value, Error err) {
	    fixValue     = value;
	    _error       = err;
	}

        /////////////////////////////////////////////////////////////////////
        ////                     public methods                          ////

	/** Return the absolute value of this FixValue. Uses the
	 *  absolute method of class BigInteger. The resulting
	 *  FixValue copies the error field of this FixValue.
         *  @return  A new FixValue.
	 */
 	public FixValue abs() {
 	    BigInteger result = fixValue.abs();
 	    return new FixValue(result, _error);
	}

	/** Return a new FixValue with a value equal to the sum of this
	 *  FixValue number and the argument. The addition uses the
	 *  add method of class BigInteger. It copies the error field
	 *  of this FixValue to the new FixValue.
         *  @param arg A FixValue number.
         *  @return A new FixValue number.
	 */
        public FixValue add(FixValue aValue) {
	    BigInteger result = fixValue.add(aValue.fixValue);
	    return new FixValue(result, _error);
	}

	/** Get the Error condition from the FixValue.
         *  @return The error condition of the FixValue.
         */
	public Error getError() { return _error; }

	/** Return only the fractional part of the FixValue. Because a
	 *  BigInteger number does not have the notion of a point, the
	 *  precision of the fixValue has to be supplied, to extract
	 *  the correct number of bits for the fractional part.
	 *  @param precision Precision of the fixValue.
         *  @return fractional part of the FixValue.
         */
	public FixValue getFractionBits(Precision precision) {
	    BigInteger tmp = (_getTwoRaisedTo(
                    precision.getFractionBitLength())).toBigInteger();
	    BigInteger mask = tmp.subtract(BigInteger.ONE);
	    BigInteger result = fixValue.and(mask);
	    return new FixValue(result, _error);
	}

	/** Return only the integer part of the FixValue. Because a
	 *  BigInteger number does not have the notion of a point, the
	 *  precision of the fixValue has to be supply, to extract the
	 *  correct number of bits for the integer part.
         *  @param precision Precision of the fixValue.
         *  @return integer part of the FixValue.
	 */
    	public FixValue getIntegerBits(Precision precision) {
	    BigInteger result =
                fixValue.shiftRight(precision.getFractionBitLength());
	    return new FixValue(result, _error);
	}

	/** Return a new FixValue with a value equal to the
	 *  multiplication of this FixValue number and the
	 *  argument. Uses the multiply method of class
	 *  BigInteger. The resulting FixValue copies the error
	 *  field of this FixValue.
         *  @param arg A FixValue number.
	 *  @return A new FixValue.
	 */
	public FixValue multiply(FixValue aValue) {
	    BigInteger result = fixValue.multiply(aValue.fixValue);
	    return new FixValue(result, _error);
	}

	/** Return the negated value of this FixValue. Uses the negate
	 *  method of class BigInteger. The resulting FixValue copies
	 *  the error field of this FixValue.
         *  @return A new FixValue.
	 */
 	public FixValue negate() {
 	    BigInteger result = fixValue.negate();
 	    return new FixValue(result, _error);
	}

        /** Return a scaled FixValue by scaling this fixValue. Scale
         *  the fixValue by shifting it delta positions from the
         *  left hand side. If delta<0, then the fixValue is reduced in
         *  length leading to possible rounding. Select on the basis
         *  of the overflow mode what the final FixValue should look
         *  like.
         *  @param delta Number of positions the fixValue is
         *  scaled from left.
         *  @return A scaled FixValue.
         */
        public FixValue scaleLeft(int delta) {
            // copy the previous fixValue
            FixValue work = new FixValue(fixValue, _error);
            FixValue result = new FixValue();

            // Delta >0, shift Left
            // Delta <0, shift Right
            if (delta < 0) {

                // Check if last delta bits are zero
                // Because then no rounding takes place
                for (int i = 0; i < -delta; i++){
                    if (work.fixValue.testBit(i) == true) {
                        work.setError(ROUNDING);
                    }
                }
                result.fixValue = work.fixValue.shiftLeft(delta);
            } else {
                result.fixValue = work.fixValue.shiftLeft(delta);
            }
            result.setError(work.getError());
            return result;
        }

        /** Return a scaled FixValue by scaling this fixValue. Scale
         *  the fixValue is done by truncating delta bits from the
         *  left hand side. In case delta<0, no truncation will occurs
         *  and thus no overflow error occurs. If delta>0, then the
         *  fixValue is truncated by doing an AND on the result
         *  stripping bits to fit the precision. This always leads to
         *  rounding.
         *  @param delta Number of positions the fixValue
         *  is scaled from right.
         *  @return A scaled FixValue.
         */
        public FixValue scaleRight(int delta) {
            FixValue work = new FixValue(fixValue, _error);
            FixValue result = new FixValue();
            int length = work.fixValue.bitLength();
            if (delta>0) {
                work.setError(OVERFLOW);
                // Create the MASK for truncating the fixValue
                BigInteger mask =
                    _getTwoRaisedTo(length-delta).toBigInteger().
                    subtract(BigInteger.ONE);
                // AND the fixValue with the MASK.
                result.fixValue = work.fixValue.and(mask);
            } else {
                result.fixValue = work.fixValue;
            }
            // Keep the error status of this fixValue.
            result.setError(work.getError());
            return result;
        }

	/** Set the Error of the FixValue
         *  @param error The error condition of the FixValue
         */
	public void setError(Error error) { _error = error; }

	/** Return a bit string representation of the FixValue. The
         *  representation is a bit string giving the same
         *  representation on all possible platforms, facilitating a
         *  more robust testing of instances of FixPoint.
         *
 	 *  @return bit string representation of the FixValue
         */
	public String toString() {
            return fixValue.toString(2);
        }

	/////////////////////////////////////////////////////////////////////
	////                      private variables                      ////

	/** The BigInteger representing the finite bit string */
	public  BigInteger fixValue;


	/////////////////////////////////////////////////////////////////////
	////                      private variables                      ////

	/** The error condition of the FixValue */
	private Error      _error;
    }


    /** Instances of this class represent a type safe enumeration of
     *  error conditions of the FixValue.
     */
    public static class Error {
        // This inner class is public so that we can test it under JDK1.4

	// Constructor is private because only Manager instantiates this class.
	private Error(String description) {
	    _description = description;
	}

	/** Get a description of the Error.
	 * @return A description of the Error.
         */
	public String getDescription() {
	    return " " + _description;
	}

	private String _description;
    }
}
