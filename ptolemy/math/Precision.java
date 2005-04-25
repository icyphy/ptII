/** A class representing the precision of a FixPoint number.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.math;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.StringTokenizer;


/**
   This class describes the precision of an instance of FixPoint.
   An instance of FixPoint represents a fixed point number as
   two finite bit sequences: an integer part and a fractional part.
   The total length of a fixed point value is the combined length
   of the integer part and the fractional part.
   <p>
   There are two string formats for precision, either of which is
   understood by the constructor that takes a string argument:
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
   In both cases, the parentheses are optional.
   <p>
   The FixPoint class represents signed numbers in a 2's complement
   format. As a consequence, a
   single bit is used to represent the sign of the number.
   Therefore, when a precision is
   given of, for example, <i>(6, 3)</i>, then 6 bits are used for the
   integer part and 3 are used for the fractional part. Of the 6 integer
   bits, 5 are used to represent a number and 1 is used for the sign.
   <p>
   In giving a precision, at least a single bit is required to describe
   the sign and thus the minimum precision that can be given to a
   FixPoint is equal to (1.0). Since a single bit is used, the
   FixPoint represents the numbers 0 and -1. Note that a
   precision of (2.0) represents the numbers 1, 0, -1, and -2.
   The representation is two's complement.
   <p>
   An instance of the class is immutable, meaning
   that its value is set in the constructor and cannot then be modified.

   @author Bart Kienhuis
   @version $Id$
   @since Ptolemy II 0.4
   @Pt.ProposedRating Yellow (kienhuis)
   @Pt.AcceptedRating Red (kienhuis)
   @see FixPoint
*/
public class Precision implements Cloneable, Serializable {
    /** Construct a Precision object based on the provided string. The
     *  string can describe the precision in either of the syntaxes
     *  explained in the description of this class.
     *
     *  @param precision The string representing the precision.
     *  @exception IllegalArgumentException If the precision string
     *   supplied does not match one of the known formats.
     */
    public Precision(String precision) throws IllegalArgumentException {
        // Check which format is used
        boolean done = false;
        int type = 0;
        StringTokenizer st = new StringTokenizer(precision);

        if (precision.indexOf('/', 0) != -1) {
            done = true;
            type = 1;
            st = new StringTokenizer(precision, "/()\"");
        }

        if (precision.indexOf('.', 0) != -1) {
            done = true;
            type = 2;
            st = new StringTokenizer(precision, ".()\"");
        }

        // throw an exception
        if ((done == false) || (st.countTokens() < 1) || (st.countTokens() > 2)) {
            throw new IllegalArgumentException("The precision string "
                + precision + " uses an incorrect " + "precision format");
        }

        int first = 0;
        int second = 0;

        // The string might contain only a single number...
        try {
            first = (new Integer(st.nextToken())).intValue();
            second = (new Integer(st.nextToken())).intValue();
        } catch (Exception e) {
            throw new IllegalArgumentException("A precision string "
                + " consists of two integers separated "
                + " by a '/', or '.' token");
        }

        // Depending on the type, interpret the two values
        if (type == 1) {
            _length = first;
            _integerBits = second;
        }

        if (type == 2) {
            _length = first + second;
            _integerBits = first;
        }

        _fraction = _length - _integerBits;

        if (_length <= 0) {
            throw new IllegalArgumentException("Incorrect definition of "
                + "Precision. Do not use negative total length ");
        }
    }

    /** Construct a Precision object based on the provided numbers.
     *  @param length The total number of bits.
     *  @param integerBits The number of integer bits.
     *  @exception IllegalArgumentException If the given values are
     *   negative or when the integer number of bits is larger than the
     *   total number of bits.
     */
    public Precision(int length, int integerBits)
        throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Incorrect definition of "
                + "Precision. Do not use negative total length ");
        }

        _length = length;
        _integerBits = integerBits;
        _fraction = length - integerBits;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return This Precision.
     */
    public Object clone() {
        return this;
    }

    /** Return true if the indicated object is an instance of Precision
     *  and the number of integer and fractional bits both match.
     *  @return True if the precisions are equal.
     */
    public boolean equals(Object object) {
        if (object instanceof Precision) {
            Precision other = (Precision) object;

            if ((other._fraction == _fraction)
                            && (other._integerBits == _integerBits)) {
                return true;
            }
        }

        return false;
    }

    /** Return the maximum obtainable value in this precision. When
     *  <i>m</i> represents the total number of bits and <i>n</i> the
     *  number of integer bits, this is equal to 2^(n-1) -
     *  1/(2^(m-n)).
     *  NOTE: This method should be package friendly, not public.
     *  It is not intended to be used outside this package.
     *  @return The maximum value obtainable for this precision.
     */
    public BigDecimal findMaximum() {
        // FIXME: Why does this return a BigDecimal instead of a FixPoint?
        // Because it's intended for internal use.
        // It should be package friendly.
        BigInteger intValue = BigInteger.ZERO.setBit(_length - 1);
        intValue = intValue.subtract(BigInteger.ONE);

        double val = intValue.doubleValue() * Math.pow(0.5, _fraction);
        return new BigDecimal(val);
    }

    /** Return the minimum obtainable value for this precision. When
     *  <i>m</i> represents the total number of bits and <i>n</i> the
     *  number of integer bits, this is equal to -2^(n-1).
     *  NOTE: This method should be package friendly, not public.
     *  It is not intended to be used outside this package.
     *  @return The minimum value obtainable for the given precision..
     */
    public BigDecimal findMinimum() {
        // FIXME: Why does this return a BigDecimal instead of a FixPoint?
        // Because it's intended for internal use.
        // It should be package friendly.
        BigInteger intValue = BigInteger.ZERO.setBit(_length - 1);
        intValue = intValue.negate();

        double val = intValue.doubleValue() * Math.pow(0.5, _fraction);
        return new BigDecimal(val);
    }

    /** Return the number of bits representing the fractional part.
     *  @return The length of the fractional part.
     */
    public int getFractionBitLength() {
        return _fraction;
    }

    /** Return the number of bits representing the integer part.
     *  @return the length of the integer part.
     */
    public int getIntegerBitLength() {
        return _integerBits;
    }

    /** Return the total number of bits.
     *  @return the total number of bits.
     */
    public int getNumberOfBits() {
        return _length;
    }

    /** Return the precision that is the maximum of the two supplied
     *  precisions in both the integer and fractional part. This
     *  method is used to align instances of FixPoint onto a single
     *  precision representation.
     *  @param precisionA A precision
     *  @param precisionB Another precision
     *  @return A precision at least as precise as the two arguments.
     */
    public static Precision matchThePoint(Precision precisionA,
        Precision precisionB) {
        int bitright = Math.max(precisionA.getFractionBitLength(),
                precisionB.getFractionBitLength());
        int newIntLength = Math.max(precisionA.getIntegerBitLength(),
                precisionB.getIntegerBitLength());
        int newLength = newIntLength + bitright;
        return new Precision(newLength, newIntLength);
    }

    /** Return a string representing this precision. The string is
     *  expressed as "(<i>m.n</i>)", where <i>m</i>
     *  indicates the number of integer bits and <i>n</i> represents
     *  the number of fractional bits.
     *  @return A string representing this precision.
     */
    public String toString() {
        String x = "(" + _integerBits + "." + (_length - _integerBits) + ")";
        return x;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The total number of bits. */
    private int _length = 0;

    /** The number of bits in the integer part. */
    private int _integerBits = 0;

    /** The number of bits in the fractional part. */
    private int _fraction = 0;
}
