/* This object quantizes a value into a Fixed point value.

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

import java.math.BigInteger;
import java.math.BigDecimal;

//////////////////////////////////////////////////////////////////////////
//// Quantizer

/**

To create an instance of a FixPoint, one has to use static function of
this Quantizer class. These functions implement different quantizers
to convert a double value or integer value into a FixPoint value.

<p>

Currently the following quantizers exist:

<ol>

<li> <b>Quantizer.round</b>: Return a FixPoint that is nearest to the
value that can be represented with the given precision, possibly
introducing quantization errors.

<li> <b>Quantizer.truncate</b>: Return a FixPoint that is the nearest
value towards zero that can be represented with the given precision,
possibly introducing quantization errors.

</ol>

In case a value is given that falls outside the range of values that
can be achieved with the given precision, the value is set to the
maximum or depending on the sign of the value to the minimum value
possible with the precision. Also, the overflow flag is set to
indicate that an overflow occured for the FixPoint value.

@author Bart Kienhuis
@version $Id$
@see FixPoint
@see Precision
*/

public class Quantizer {

    // The only constructor is private so that this class cannot
    // be instantiated.
    private Quantizer() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a FixPoint that is nearest to the value that can be
     *  represented with the given precision, possibly introducing
     *  quantization or overflow errors. If the value does not fit
     *  within the range, possible with the precision, an overflow
     *  error occurs. In that case, the FixPoint is set depending on
     *  the sign of the value to either the maximum or minimum value
     *  possible with the given precision. Also a flag is set for the
     *  FixPoint to indicate that an overflow error took place.
     *
     *  @param value The value for which to create a FixPoint
     *  @param precision The precision of the FixPoint
     *  @return A FixPoint for the value with a given precision
     */
    public static FixPoint round(double value, Precision precision) {
        BigDecimal newValue = new BigDecimal( value );
        return round( newValue, precision);;
    }

    /** Return a FixPoint that is nearest to the value that can be
     *  represented with the given precision, possibly introducing
     *  quantization or overflow errors. If the value does not fit
     *  within the range, possible with the precision, an overflow
     *  error occurs. In that case, the FixPoint is set depending on
     *  the sign of the value to either the maximum or minimum value
     *  possible with the given precision. Also a flag is set for the
     *  FixPoint to indicate that an overflow error took place.
     *
     *  @param value The value for which to create a FixPoint
     *  @param precision The precision of the FixPoint
     *  @return A FixPoint for the value with a given precision
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
        // 2^fractionbitlength By multiply the given value 'x' with
        // this scale factor. An value is obtained of the fraction
        // part is dropped. The integer remaining after the scaleing
        // will be represented by the BigInteger.
        int number = precision.getFractionBitLength();

        // This division divides two number in a precision of 40
        // decimal behind the point. This is equvalent with a
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

    /** Return a new FixPoint number which is the given FixPoint
     *  rounded to the next value possible with the given new
     *  precision, possibly introducing quantization or overflow
     *  errors. In case of an overflow, the value of the FixPoint is
     *  determined depending on the overflow mode selected.
     *
     *   <ul>
     *
     *   <li> mode = 0, <b>Saturate</b>: The fixed point value is set,
     *   depending on its sign, equal to the Maxium or Minimum value
     *   possible with the new given precision.
     *
     *   <li> mode = 1, <b>Zero Saturate</b>: The fixed point value is
     *   set equal to zero.
     *
     *   </ul>
     *
     *  @param newprecision The new precision of the FixPoint.
     *  @param mode The overflow mode.
     *  @return A new FixPoint with the given precision.
     */
    public static FixPoint round(
            FixPoint value,
            Precision newprecision,
            int mode) {

        FixPoint newvalue = null;
	BigDecimal x = value.bigDecimalValue();
	BigDecimal maxValue = newprecision.findMaximum();
	BigDecimal minValue = newprecision.findMinimum();
	// check if 'x' falls within the range of this FixPoint
	// possible with the given precision
        if ( x.compareTo(maxValue) < 0 &&
                x.compareTo(minValue) > 0 ) {
            // In range, thus leads at most to a quantization error
            newvalue = FixPoint._scaleBits(value, newprecision, mode);
        } else {
            FixPoint result;
            //Not in range. Can lead to an overflow problem.
            switch(mode) {
            case 0: //SATURATE
                if ( x.signum() >= 0) {
                    result = Quantizer.round( maxValue, newprecision );
                } else {
                    result = Quantizer.round( minValue, newprecision );
                }
                result.setError(FixPoint.OVERFLOW);
                //return result;
                break;
             case 1: //ZERO_SATURATE:
                result = new FixPoint(newprecision, BigInteger.ZERO);
                result.setError(FixPoint.OVERFLOW);
                //return result;
                break;
            default:
                throw new IllegalArgumentException("Illegal Mode of " +
                        "overflow handling");
            }
            newvalue = result;
        }
        return newvalue;
    }

    /** Return a FixPoint that is the nearest value towards zero that
     *  can be represented with the given precision, possibly
     *  introducing quantization or overflow errors. If the value does
     *  not fit within the range, possible with the precision, an
     *  overflow error occurs. In that case, the FixPoint is set
     *  depending on the sign of the value to either the maximum or
     *  minimum value possible with the given precision. Also a flag
     *  is set for the FixPoint to indicate that an overflow error
     *  took place.
     *
     *  @param value The double value for which to create a FixPoint
     *  @param precision The precision of the FixPoint
     *  @return A FixPoint for the value with a given precision
     */
    public static FixPoint truncate(double value, Precision precision) {
        BigDecimal newValue = new BigDecimal( value );
        return truncate( newValue, precision);
    }

    /** Return a FixPoint that is the nearest value towards zero that
     *  can be represented with the given precision, possibly
     *  introducing quantization or overflow errors. If the value does
     *  not fit within the range, possible with the precision, an
     *  overflow error occurs. In that case, the FixPoint is set
     *  depending on the sign of the value to either the maximum or
     *  minimum value possible with the given precision. Also a flag
     *  is set for the FixPoint to indicate that an overflow error
     *  took place.
     *
     *  @param value The value for which to create a FixPoint
     *  @param precision The precision of the FixPoint
     *  @return A FixPoint for the value with a given precision
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

        // determine the scale factor by calculating 2^fractionbitlength
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
        // decimal behind the point. This is equvalent with a
        // fractional precision of 128 bits. ( ln(1-^40)/ln(2) > 128)
            epsilon = _one.divide(_getTwoRaisedTo(number+5),
                    40, BigDecimal.ROUND_HALF_EVEN);
            multiplier = x.add( epsilon );
        } else {
            // calculate epsilon
        // This division divides two number in a precision of 40
        // decimal behind the point. This is equvalent with a
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

    /** Return a new FixPoint number which is the given FixPoint
     *  truncated to the next value possible with the given new
     *  precision, possibly introducing quantization or overflow
     *  errors. In case of an overflow, the value of the FixPoint is
     *  determined depending on the overflow mode selected.
     *
     *   <ul>
     *
     *   <li> mode = 0, <b>Saturate</b>: The fixed point value is set,
     *   depending on its sign, equal to the Maxium or Minimum value
     *   possible with the new given precision.
     *
     *   <li> mode = 1, <b>Zero Saturate</b>: The fixed point value is
     *   set equal to zero.
     *
     *   </ul>
     *
     *  @param newprecision The new precision of the FixPoint.
     *  @param mode The overflow mode.
     *  @return A new FixPoint with the given precision.
     */
    public static FixPoint truncate(
            FixPoint value,
            Precision newprecision,
            int mode) {

        FixPoint newvalue = null;
	BigDecimal x = value.bigDecimalValue();
	BigDecimal maxValue = newprecision.findMaximum();
	BigDecimal minValue = newprecision.findMinimum();
	// check if 'x' falls within the range of this FixPoint
	// possible with the given precision
        if ( x.compareTo(maxValue) < 0 &&
                x.compareTo(minValue) > 0 ) {
            // In range, thus leads at most to a quantization error
            newvalue = FixPoint._scaleBits(value, newprecision, mode);
        } else {
            FixPoint result;
            //Not in range. Can lead to an overflow problem.
            switch(mode) {
            case 0: //SATURATE
                if ( x.signum() >= 0) {
                    result = Quantizer.truncate( maxValue, newprecision );
                } else {
                    result = Quantizer.truncate( minValue, newprecision );
                }
                result.setError(FixPoint.OVERFLOW);
                //return result;
                break;
             case 1: //ZERO_SATURATE:
                result = new FixPoint(newprecision, BigInteger.ZERO);
                result.setError(FixPoint.OVERFLOW);
                //return result;
                break;
            default:
                throw new IllegalArgumentException("Illegal Mode of " +
                        "overflow handling");
            }
            newvalue = result;
        }
        return newvalue;
    }

    /** Get the BigDecimal which is the 2^exponent. If the value is
     *  already calculated, return this cached value, else calculate
     *  the value.
     *
     *  @param number the exponent.
     *  @return the BigDecimal representing 2^exponent.
     */
    private static BigDecimal _getTwoRaisedTo(int number) {
        if ( number <= 128 || number >= 0 ) {
            return _twoRaisedTo[number];
        } else {
            BigInteger two = _two.toBigInteger();
            return new BigDecimal( two.pow( number ) );
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

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

    //////////////////////
    // static class
    //////////////////////

    // Static Class Constructor
    static {
        BigDecimal p2  = _one;
        for (int i = 0; i <= 64; i++) {
            _twoRaisedTo[i] = p2;
            p2 = p2.multiply( _two );
        }
    }

}
