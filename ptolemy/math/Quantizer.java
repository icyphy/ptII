/* A collection of methods for creating fixed point values.

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

import java.math.BigDecimal;
import java.math.BigInteger;

//////////////////////////////////////////////////////////////////////////
//// Quantizer

/**
This class provides a set of static methods for creating instances
of the FixPoint class from doubles, integers, or fixed point numbers.
The various round() methods return a fixed point value that is nearest
to the specified number, but has the specified precision.
The various truncate() methods return a fixed point value that is
nearest to the specified number, but no greater in magnitude.
All of these methods may introduce quantization errors and/or overflow.

@author Bart Kienhuis
@contributor Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
@see FixPoint
@see Precision
*/

public class Quantizer {

    // The only constructor is private so that this class cannot
    // be instantiated.
    private Quantizer() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the fixed point number that is nearest to the specified
     *  value, but has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number. In this case, a flag is set in the returned
     *  value to indicate that an overflow error occurred.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint round(double value, Precision precision) {
        BigDecimal newValue = new BigDecimal( value );
        return round( newValue, precision);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number. In this case, a flag is set in the returned
     *  value to indicate that an overflow error occurred.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint round(BigDecimal value, Precision precision) {
	BigInteger tmpValue;
        BigInteger fxvalue;
	boolean overflow = false;

	BigDecimal x = value;
	BigDecimal maxValue = precision.findMaximum();
	BigDecimal minValue = precision.findMinimum();

	// check if 'x' falls within the range of this FixPoint
	// possible with the given precision
        if ( x.compareTo(maxValue) > 0 ) {
	    overflow = true;
	    x = maxValue;
	}
        if ( x.compareTo(minValue) < 0 ) {
            overflow = true;
            x = minValue;
        }

        // determine the scale factor by calculating
        // 2^fractionBitLength By multiply the given value 'x' with
        // this scale factor. An value is obtained of the fraction
        // part is dropped. The integer remaining after the scaling
        // will be represented by the BigInteger.
        int number = precision.getFractionBitLength();

        // This division divides two number in a precision of 40
        // decimal behind the point. This is equivalent with a
        // fractional precision of 128 bits. ( ln(1-^40)/ln(2) > 128)
        BigDecimal resolution = _one.divide( _getTwoRaisedTo(number+1),
                40, BigDecimal.ROUND_HALF_EVEN);

        BigDecimal multiplier;
        if ( x.signum() >= 0 ) {
            multiplier = x.add(resolution );
        } else {
            multiplier = x.subtract(resolution);
        }
        BigDecimal kl = _getTwoRaisedTo(number).multiply( multiplier );

        // By going from BigDecimal to BigInteger, remove the fraction
        // part. This part introduces a quantization error.
        fxvalue = kl.toBigInteger();

        // Create a new FixPoint
        FixPoint fxp = new FixPoint( precision, fxvalue );

        // and set the overflow flag, if overflow occurred
        if ( overflow ) {
            fxp.setError( FixPoint.OVERFLOW );
        }
        return fxp;
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value depends on the specified mode.
     *  If the mode is SATURATE, then the return value is either
     *  the maximum or minimum value possible with the given
     *  precision, depending on the sign of the
     *  specified number.  If the mode is OVERFLOW_TO_ZERO,
     *  then the return value is zero.
     *  In either case, a flag is set in the returned
     *  value to indicate that an overflow error occurred.
     *
     *  @param value The value to represent.
     *  @param newPrecision The precision of the representation.
     *  @param mode The overflow mode.
     *  @return A new fixed-point representation of the value.
     */
    public static FixPoint round(
            FixPoint value,
            Precision newPrecision,
            int mode) {

        FixPoint newValue = null;
	BigDecimal x = value.bigDecimalValue();
	BigDecimal maxValue = newPrecision.findMaximum();
	BigDecimal minValue = newPrecision.findMinimum();
	// check if 'x' falls within the range of this FixPoint
	// possible with the given precision
        if ( x.compareTo(maxValue) < 0 &&
                x.compareTo(minValue) > 0 ) {
            // In range, thus leads at most to a quantization error
            newValue = FixPoint._scaleBits(value, newPrecision, mode);
        } else {
            FixPoint result;
            //Not in range. Can lead to an overflow problem.
            switch(mode) {
            case SATURATE:
                if ( x.signum() >= 0) {
                    result = Quantizer.round( maxValue, newPrecision );
                } else {
                    result = Quantizer.round( minValue, newPrecision );
                }
                result.setError(FixPoint.OVERFLOW);
                //return result;
                break;
            case OVERFLOW_TO_ZERO:
                result = new FixPoint(newPrecision, BigInteger.ZERO);
                result.setError(FixPoint.OVERFLOW);
                //return result;
                break;
            default:
                throw new IllegalArgumentException("Illegal Mode of " +
                        "overflow handling");
            }
            newValue = result;
        }
        return newValue;
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has magnitude no greater that the specified value,
     *  and has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number. In this case, a flag is set in the returned
     *  value to indicate that an overflow error occurred.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint truncate(double value, Precision precision) {
        BigDecimal newValue = new BigDecimal( value );
        return truncate( newValue, precision);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has magnitude no greater that the specified value,
     *  and has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number. In this case, a flag is set in the returned
     *  value to indicate that an overflow error occurred.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint truncate(BigDecimal value, Precision precision) {

	BigInteger tmpValue;
        BigInteger fxvalue;
	boolean overflow = false;

	BigDecimal x = value;
	BigDecimal maxValue = precision.findMaximum();
	BigDecimal minValue = precision.findMinimum();

	// check if 'x' falls within the range of this FixPoint with
	// given precision
        if ( x.compareTo(maxValue) > 0 ) {
	    overflow = true;
	    x = maxValue;
	}
        if ( x.compareTo(minValue) < 0 ) {
            overflow = true;
            x = minValue;
        }

        // determine the scale factor by calculating 2^fractionBitLength
        // By multiply the given value 'x' with this scale factor, we get
        // a value of which we drop the fraction part. The integer remaining
        // will be represented by the BigInteger.
        int number = precision.getFractionBitLength();
        BigDecimal multiplier;
        BigDecimal epsilon;
        BigDecimal tmp;

        if ( x.signum() >= 0 ) {
            // calculate epsilon
            // This division divides two number in a precision of 40
            // decimal behind the point. This is equivalent with a
            // fractional precision of 128 bits. ( ln(1-^40)/ln(2) > 128)
            epsilon = _one.divide(_getTwoRaisedTo(number+5),
                    40, BigDecimal.ROUND_HALF_EVEN);
            multiplier = x.add( epsilon );
        } else {
            // calculate epsilon
            // This division divides two number in a precision of 40
            // decimal behind the point. This is equivalent with a
            // fractional precision of 128 bits. ( ln(1-^40)/ln(2) > 128)
            tmp = _one.divide(_two, 40, BigDecimal.ROUND_HALF_EVEN);
            epsilon = tmp.subtract( _one.divide(_getTwoRaisedTo(number+11),
                    40, BigDecimal.ROUND_HALF_EVEN));
            multiplier = x.subtract(epsilon);
        }
        // determine the scale factor.
        BigDecimal kl = _getTwoRaisedTo(number).multiply( multiplier );

        // By going from BigDecimal to BigInteger, remove the fraction
        // part introducing a quantization error.
        fxvalue = kl.toBigInteger();

        // Create a new FixPoint
        FixPoint fxp = new FixPoint( precision, fxvalue );

        if ( overflow ) {
            fxp.setError( FixPoint.OVERFLOW );
        }
        return fxp;
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has magnitude no greater than the specified value,
     *  and has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value depends on the specified mode.
     *  If the mode is SATURATE, then the return value is either
     *  the maximum or minimum value possible with the given
     *  precision, depending on the sign of the
     *  specified number.  If the mode is OVERFLOW_TO_ZERO,
     *  then the return value is zero.
     *  In either case, a flag is set in the returned
     *  value to indicate that an overflow error occurred.
     *
     *  @param value The value to represent.
     *  @param newPrecision The precision of the representation.
     *  @param mode The overflow mode.
     *  @return A new fixed-point representation of the value.
     */
    public static FixPoint truncate(
            FixPoint value,
            Precision newPrecision,
            int mode) {

        FixPoint newValue = null;
	BigDecimal x = value.bigDecimalValue();
	BigDecimal maxValue = newPrecision.findMaximum();
	BigDecimal minValue = newPrecision.findMinimum();
	// check if 'x' falls within the range of this FixPoint
	// possible with the given precision
        if ( x.compareTo(maxValue) < 0 &&
                x.compareTo(minValue) > 0 ) {
            // In range, thus leads at most to a quantization error
            newValue = FixPoint._scaleBits(value, newPrecision, mode);
        } else {
            FixPoint result;
            //Not in range. Can lead to an overflow problem.
            switch(mode) {
            case SATURATE:
                if ( x.signum() >= 0) {
                    result = Quantizer.truncate( maxValue, newPrecision );
                } else {
                    result = Quantizer.truncate( minValue, newPrecision );
                }
                result.setError(FixPoint.OVERFLOW);
                //return result;
                break;
            case OVERFLOW_TO_ZERO:
                result = new FixPoint(newPrecision, BigInteger.ZERO);
                result.setError(FixPoint.OVERFLOW);
                //return result;
                break;
            default:
                throw new IllegalArgumentException("Illegal Mode of " +
                        "overflow handling");
            }
            newValue = result;
        }
        return newValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that overflow should saturate. */
    public static final int SATURATE = 0;

    /** Indicate that overflow should result in a zero value. */
    public static final int OVERFLOW_TO_ZERO = 1;


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the BigDecimal which is the 2^exponent. If the value is
     *  already calculated, return this cached value, else calculate
     *  the value.
     *
     *  @param number the exponent.
     *  @return the BigDecimal representing 2^exponent.
     */
    private static BigDecimal _getTwoRaisedTo(int number) {
        if ( number < 32 && number >= 0 ) {
            return _twoRaisedTo[number];
        } else {
            BigInteger two = _two.toBigInteger();
            return new BigDecimal( two.pow( number ) );
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Static reference to the BigDecimal representation of one. */
    private static BigDecimal _one  = new BigDecimal("1");

    /** Calculate the table containing 2^x, with 0 < x < 64. Purpose
     *  is to speed up calculations involving calculating 2^x. The table is
     *  calculated using BigDecimal, since this make the transformation from
     *  string of bits to a double easier.
     */
    private static BigDecimal[] _twoRaisedTo = new BigDecimal[32];

    /** Static reference to the BigDecimal representation of two. */
    private static BigDecimal _two = new BigDecimal("2");

    //////////////////////
    // static initializer
    //////////////////////

    static {
        BigDecimal p2  = _one;
        for (int i = 0; i < 32; i++) {
            _twoRaisedTo[i] = p2;
            p2 = p2.multiply( _two );
        }
    }
}
