/** Precision indicates the precision of a FixPoint number.

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

import ptolemy.kernel.util.IllegalActionException;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.StringTokenizer;

/**

This class describes the precision of a FixPoint. A FixPoint reprents
a fixed point number which consists of two finite bit strings: an
integer part and a fractional part. The total length of a fixed point
value is determined by the combined length of the integer part and the
fractional part. The precision determines the length of the integer
and fractional part of a FixPoint using either a particular string
representation, or by supplying a particular length for the integer
and fractional part. <p>

The precision of a FixPoint can be noted using different string
formats

<ul>

<li> <b>m/n</b> <br> The total bit length of the FixPoint is equal to
<i>m</i> bits and the integer part is equal to <i>n</i> bits. The
fractional part it thus equal to <i>m-n</i> bits.

<li> <b>m.n</b> <br> The total length of the FixPoint is equal to
<i>n+m</i> bits. The integer part is <i>m</i> bits long and the
fractional part is <i>n</i> bits long.

</ul>

The FixPoint class represents signed numbers. As a consequence, a
single bit is used to represent the sign of the number, i.e where it
is a positive or a negative number. Therefore, when a precision is
given of, for example, <i>(6,3)</i>, then 6 bits are used for the
integer part and 3 are used for the fractional part. Of the 6 integer
bits, 5 are used to represent a number and 1 is used for the sign. 

<p>

In giving a precision, at least a single bit is required to describe
the sign and thus the minimum precision that can be given to a
FixPoint is equal to <i>(1.0)</i>. Since a single bit is used, the
FixPoint represents the numbers <i>0</i> and <i>-1</i>. Note that a
precision of <i>(2.0)</i> represents the numbers <i>1,0,-1</i>.

<p>

In describing the precision, one has the optional to put brackets
around the precision description. Thus "(16/4)" and "16/4" represent
the same precision. An instance of the class is immutable, meaning
that its value is set in the constructor and cannot then be modified.

@author Bart Kienhuis
@version $Id$
@see FixPoint

*/

public class Precision {

    /** Construct a Precision object based on the provided string. The
     *  string can described the precision in two different syntaxes
     *  namely: (m/n) or (m.n) as explained in the description of this
     *  class.
     *
     *  @param precision The string representing the precision.
     *  @exception IllegalArgumentException If the precision string
     *  supplied does not match one of the known formats.
     */
    public Precision(String precision) throws IllegalArgumentException
        {
            // Check which format is used
            boolean done = false;
            int type = 0;
            StringTokenizer st = new StringTokenizer(precision);
            if ( precision.indexOf('/', 0) != -1 ) {
                done = true;
                type = 1;
                st = new StringTokenizer(precision,"/()\"");
            }
            if ( precision.indexOf('.', 0) != -1 ) {
                done = true;
                type = 2;
                st = new StringTokenizer(precision,".()\"");
            }
            // throw an exception
            if (( done == false ) || ( st.countTokens() <1) ||
                    (st.countTokens() > 2 )) {
                throw new IllegalArgumentException("The precision string " +
                        precision + " uses an incorrect " +
                        "precision format" );
            }
            int  first = 0;
            int second = 0;

            // The string might contain only a single number...
            try {
                first = (new Integer(st.nextToken())).intValue();
                second = (new Integer(st.nextToken())).intValue();
            } catch ( Exception e ) {
                throw new IllegalArgumentException("A precision string " +
                        " consists of two integers separated " +
                        " by a '/', or '.' token" );
            }

            // Depending on the type, interpret the two values
            if ( type == 1 ) {
                _length = first;
                _integerBits = second;
            }

            if ( type == 2 ) {
                _length = first + second;
                _integerBits = first;
            }
            _fraction = _length - _integerBits;

            if ( _integerBits == 0 ) {
                throw new IllegalArgumentException("Incorrect definition of " +
                        "Precision. A FixPoint requires the use of at least " +
                        "a single integer bit to represent the sign.");
            }
            if (_length <= 0 || _integerBits < 0 || _integerBits > _length) {
                throw new IllegalArgumentException("Incorrect definition of " +
                        "Precision. Do not use negative values or have an " +
                        "integer part larger than the total length ");
            }
        }

    /** Construct a Precision object based on the provided
     *  numbers. These numbers define the precision in the
     *  <i>(m/n)</i> format. Thus the precision is given by the total
     *  number of bits used <i>(m)</I and the number of bits used to
     *  represent the integer part <i>(n)</i>.
     *
     *  @param length The total number of bits.
     *  @param integerBits Total number of integer bits.
     *  @exception IllegalArgumentException If the given values are
     *  negative or when the integer number of bits is larger than the
     *  total number of bits.
     */
    public Precision(int length, int integerBits)
            throws IllegalArgumentException {
	if (length <= 0 || integerBits < 0 || integerBits > length) {
	    throw new IllegalArgumentException("Incorrect definition of " +
                    "Precision. Do not use negative values or have an " +
                    "integer part larger than the total length ");
	}
	_length   = length;
	_integerBits  = integerBits;
	_fraction = length - integerBits;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of bits representing the fractional part of
     *  a FixPoint.
     *
     *  @return the length of the fractional part.
     */
    public int getFractionBitLength() {
	return _fraction;
    }

    /** Return the number of bits representing the integer part of a
     *  FixPoint.
     *
     * @return the length of the integer part.
     */
    public int getIntegerBitLength() {
	return _integerBits;
    }

    /** Return the total number of bits representing a FixPoint.
     *
     *  @return the total number of bits.
     */
    public int getNumberOfBits() {
	return _length;
    }

    /** Return the precision that is the maximum of the two supplied
     *  precisions in both the integer and fractional part. This
     *  method is used to align instances of FixPoint onto a single
     *  precision representation.
     *
     *  @param precisionA a Precision
     *  @param precisionB a Precision
     *  @return Maximum Precision
     */
    public static Precision matchThePoint(Precision precisionA,
            Precision precisionB)
        {
            int bitright   = Math.max(precisionA.getFractionBitLength(),
                    precisionB.getFractionBitLength());
            int newIntLength = Math.max(precisionA.getIntegerBitLength(),
                    precisionB.getIntegerBitLength());
            int newLength  = newIntLength+bitright;
            return new Precision(newLength, newIntLength);
        }

    /** Return a string representing the Precision. The string is
     *  expressed using the <i>m.n</i> notation, where <i>m</i>
     *  indicates the number of bits used to represent the integer
     *  part and <i>n</i> represents the fractional part of a
     *  FixPoint.
     *
     *  @return the string representing the Precision.
     */
    public String toString() {
	String x = "(" + _integerBits + "." + (_length - _integerBits) + ")";
	return x;
    }

    /** Returns the maximum obtainable value this precision. When
     *  <i>m</i> represents the total number of bits and <i>n</i> the
     *  number of integer bits, this is equal to<p> <pre> 2^(n-1) -
     *  1/(2^(m-n)) </pre>
     *
     *  @return The maximum value obtainable for the given precision.
     */
    public BigDecimal findMaximum() {
        int ln = getNumberOfBits();
        int ib = getIntegerBitLength();
        BigDecimal tmp = new BigDecimal(getTwoRaisedTo(ln - ib));
        BigDecimal one = new BigDecimal( _one );
        BigDecimal tmp2 = one.divide( tmp, 40, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal tmp1 = new BigDecimal(getTwoRaisedTo(ib-1)).subtract( tmp2 );
        //System.out.println("Find Max: " + tmp1.doubleValue());
        return tmp1;
    }

    /** Returns the minimum obtainable value for this precision. When
     *  <i>m</i> represents the total number of bits and <i>n</i> the
     *  number of integer bits, this is equal to<p> <pre> -2^(n-1)
     *  </pre>
     *
     *  @return The minimum value obtainable for the given precision..
     */
    public BigDecimal findMinimum() {
        int ib = getIntegerBitLength();
        BigInteger tmp = _twoRaisedTo[ib-1].negate();
        // System.out.println("Find Min: " + tmp.doubleValue());
        return new BigDecimal(tmp);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the BigInteger which is the 2^exponent. If the value is
     *  already calculated, return this cached value, else calculate
     *  the value.
     *
     *  @param number the exponent.
     *  @return the BigInteger 2^exponent.
     */
    BigInteger getTwoRaisedTo(int number) {
        if ( number < 128 ) {
            return _twoRaisedTo[number];
        } else {
            return _two.pow( number );
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** total numer of bits. */
    private int _length   = 0;

    /** number of bits in the integer part. */
    private int _integerBits  = 0;

    /** number of bits in the fractional part. */
    private int _fraction = 0;

    /** Array with cashed values. */
    private static BigInteger[] _twoRaisedTo = new BigInteger[129];

    /** Static reference to the BigInteger representation of two. */
    private static BigInteger _two = new BigInteger("2");

    /** Static reference to the BigInteger representation of one. */
    private static BigInteger _one  = new BigInteger("1");


    //////////////////////
    // static class
    //////////////////////

    /** Calculate the table containing 2^x, with 0 < x < 128. Purpose
     *  is to speed up calculations involving calculating 2^x. The
     *  table is calculated using BigDecimal, since this make the
     *  transformation from string of bits to a double easier.
     */
    static {
        BigInteger p2  = _one;;
	for (int i = 0; i <= 128; i++) {
	    _twoRaisedTo[i] = p2;
	    p2 = p2.multiply( _two );
	}
    }
}
