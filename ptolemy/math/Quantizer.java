/* This object quantizes a double value into a Fixpoint value.

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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.lang.*;
import java.math.BigInteger;
import java.math.BigDecimal;
import ptolemy.math.*;

//////////////////////////////////////////////////////////////////////////
//// Quantizer
/**
 *
 * @author Bart Kienhuis
 * @version $Id$
 */

public class Quantizer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
       Return a Fixvalue for the value and precision given. The value is
       rounded to the nearest value that can be presented with the given
       precision, possibly introducing quantization errors.
       @param value The value for which to create a Fixpoint
       @param precision The precision of the Fixpoint
       @return A Fixvalue for the value with a given precision
    */
    public static FixPoint round(double value, Precision precision) {

	BigInteger tmpValue;
        BigInteger fxvalue;
	boolean overflow = false;

	double x = value;
	double maxValue = precision.findMax();
	double minValue = precision.findMin();

	// check if 'x' falls within the range of this FixPoint with
	// given precision
        if ( x > maxValue ) {
	    overflow = true;
	    x = maxValue;
	}
        if ( x < minValue ) {
            overflow = true;
            x = minValue;
        }

        // determine the scale factor by calculating 2^fractionbitlength
        // By multiply the given value 'x' with this scale factor, we get
        // a value of which we drop the fraction part. The integer remaining
        // will be represented by the BigInteger.
        int number = precision.getFractionBitLength();
        double resolution = Math.pow(2, -(number+1));

        BigDecimal multiplier;
        if ( x >= 0 ) {
            multiplier = new BigDecimal( x + resolution );
        } else {
            multiplier = new BigDecimal( x - resolution );
        }
        BigDecimal kl = _twoRaisedTo[number].multiply( multiplier );

        // By going from BigDecimal to BigInteger, remove the fraction
        // part introducing a quantization error.
        fxvalue = kl.toBigInteger();

        // Create a new FixPoint
        FixPoint fxp = new FixPoint( precision, fxvalue );

        // and set the overflow flag, if overflow occurred
        if ( overflow ) {
            fxp.setError( FixPoint.OVERFLOW );
        }

        return fxp;
    }


    /**
       Return a Fixvalue for the value and precision given. The value is
       rounded to the nearest value that can be presented with the given
       precision, possibly introducing quantization errors.
       @param value The value for which to create a Fixpoint
       @param precision The precision of the Fixpoint
       @return A Fixvalue for the value with a given precision
    */
    public static FixPoint truncate(double value, Precision precision) {

        BigInteger tmpValue;
        boolean overflow = false;
        BigInteger fxvalue;

        double x = value;
        double maxValue = precision.findMax();
        double minValue = precision.findMin();

	// check if 'x' falls within the range of this FixPoint with
	// given precision
        if ( x > maxValue ) {
	    overflow = true;
	    x = maxValue;
	}
        if ( x < minValue ) {
            overflow = true;
            x = minValue;
        }

        // determine the scale factor by calculating 2^fractionbitlength
        // By multiply the given value 'x' with this scale factor, we get
        // a value of which we drop the fraction part. The integer remaining
        // will be represented by the BigInteger.

//         double resolution = 0;
//         int number = precision.getFractionBitLength();
//         double tmp;
//         resolution = Math.pow(2, -(number+1));
//         tmp = 1/(Math.pow(2, number) + 1.0 / Math.pow(2, number));
//         if ( x >= 0 ) {
//             resolution = 0;
//         } else {
//             resolution = tmp;
//         }

//         BigDecimal multiplier;
//         if ( x >= 0 ) {
//             multiplier = new BigDecimal( x + resolution );
//         } else {
//             multiplier = new BigDecimal( x - resolution );
//         }
//         BigDecimal kl =
//             _twoRaisedTo[
//                   precision.getFractionBitLength()].multiply( multiplier );

        int number = precision.getFractionBitLength();
        // double resolution = Math.pow(2, -(number+1)) - 
        //       Math.pow(2, -(number+2));
        double resolution = 0;
        int i = 0;

        BigDecimal multiplier;
        if ( x >= 0 ) {

            // When  positive number, add a small
            // number bringing the number closer
            // to the x = y line.
            for(i = 5; i < 10; i++) {
             resolution += Math.pow(2, -(number+i));
            }
            multiplier = new BigDecimal( x + resolution );
        } else {

            // When negative, subtract a small number
            // to bring the number close to x = y line
            for(i = 1; i < 10; i++) {
                resolution += Math.pow(2, -(number+i));
            }
            multiplier = new BigDecimal( x - resolution );
        }
        BigDecimal kl = _twoRaisedTo[number].multiply( multiplier );

        // By going from BigDecimal to BigInteger, remove the fraction
        // part introducing a quantization error.
        fxvalue = kl.toBigInteger();

        // Create a new FixPoint
        FixPoint fxp = new FixPoint( precision, fxvalue );

        if ( overflow ) {
            //fxp.setError( OVERFLOW );
        }
        return fxp;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

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
