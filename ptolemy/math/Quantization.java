/** A class defining quantization to a FixPoint number.

Copyright (c) 2002-2005 The Regents of the University of California.
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
import java.math.BigInteger;


/**
   The Quantization class defines the mapping of numeric values with unlimited
   precision to finite precision.
   <p>
   It comprises a
   <ul>
   <li>
   <i>precision</i>: to define the accuracy of the finite precision numbers.
   <li>
   <b>Overflow</b>: to define the treatment of out-of-range numbers.
   <li>
   <b>Rounding</b>: to define the loss of precision for in-range numbers.
   </ul>
   The way in which <i>precision</i> is realised is a sub-class
   responsibility. The standard fixed point binary functionality using a
   Precision is provided by FixPointQuantization.
   <p>
   This abstract class is designed to support specification of required
   numeric behavior through use of the yet-to-be-written FloatingQuantization
   and FixedQuantization classes. Knowledge of the required numeric behavior,
   rather than precise specification of an exact implementation, provides an
   opportunity for code generation to select appropriate types supported by a
   target architecture.
   <p>
   FixedQuantization provides for <i>modulo</i> equi-spaced values between
   the inclusive <i>maximum</i> and <i>minimum</i> limits. <i>epsilon</i>,
   the separation between values is given by
   <i>(maximum - minimum) / (modulo - 1)</i>. Whether values beyond the
   <i>maximum</i> and <i>minimum</i> wrap-around is determined by the overflow
   strategy. FixedQuantization therefore describes the requirements of a
   single fixed point range comprising just a mantissa.
   <p>
   FloatingQuantization adds an exponent to support multiple
   floating point ranges; <i>maximum</i> defines the upper limit of the
   coarsest scale and <i>tiny</i> defines the smallest representable non-zero
   number on the finest scale. (<i>tiny</i> is the same as <i>epsilon</i> for
   FixedQuantization.)
   <p>
   If <i>exactRounding</i> is specified, code generation must ensure that
   arithmetic rounds to the specified <i>epsilon</i>. Otherwise, the code
   generator may use arithmetic with higher precision.
   <p>
   If <i>exactOverflow</i> is specified, code generation must ensure that
   arithmetic overflows saturate or wrap-around at the specified
   <i>maximum</i> and <i>minimum</i>. Otherwise, the code generator may use
   arithmetic with greater range.
   <p>
   The active class functionality is provided by the quantize method, which
   is normally invoked from FixPoint.quantize or ScalarToken.quantize
   to enforce quantization constraints upon the result of an unconstrained
   computation.
   <p>
   An instance of the class is immutable, meaning
   that its value is set in the constructor and cannot then be modified.

   @author Ed Willink
   @version $Id$
   @since Ptolemy II 2.1
   @Pt.ProposedRating Red (Ed.Willink)
   @Pt.AcceptedRating Red
   @see FixPoint
   @see FixPointQuantization
   @see Precision
   @see Overflow
   @see Rounding
*/
public abstract class Quantization implements Cloneable, Serializable {
    /** Construct a Quantization with the given precision, overflow
     *  strategy, and rounding strategy.
     */
    public Quantization(Overflow overflow, Rounding rounding) {
        _overflow = overflow;
        _rounding = rounding;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return This Quantization.
     */
    public Object clone() {
        return this;
    }

    /** Return true if the indicated object describes the same
     *  mapping to quantized values.
     *  @return True if the quantizations are equal.
     */
    public boolean equals(Object object) {
        if (object instanceof Quantization) {
            Quantization other = (Quantization) object;

            if (_overflow.equals(other._overflow)
                    && _rounding.equals(other._rounding)) {
                return true;
            }
        }

        return false;
    }

    /** Return the separation between quantized values.
     *  @return The quantization interval.
     */
    public double getEpsilonValue() {
        double maxVal = getMaximumValue();
        double minVal = getMinimumValue();
        double modVal = getNumberOfLevels();
        return (maxVal - minVal) / (modVal - 1.0);
    }

    /** Return the overflow bit-truth.
     *  @return True if overflow must occur at the <i>maximum</i> and
     *  <i>minimum</i> values, false if greater range is acceptable.
     */
    public boolean getExactOverflow() {
        return false;
    }

    /** Return the rounding bit-truth.
     *  @return True if rounding must occur exactly at <i>epsilon</i>,
     *  false if finer resolution is acceptable.
     */
    public boolean getExactRounding() {
        return false;
    }

    /** Return the number of bits to represent the exponent.
     *  @return The length of the exponent.
     */
    public int getExponentBitLength() {
        return 0;
    }

    /** Return the number of bits representing the fractional part
     *  of the mantissa.
     *  @return The length of the fractional part of the mantissa.
     */
    public int getFractionBitLength() {
        return getMantissaPrecision().getFractionBitLength();
    }

    /** Return the number of bits representing the integer part
     *  of the mantissa.
     *  @return The length of the integer part of the mantissa.
     */
    public int getIntegerBitLength() {
        return getMantissaPrecision().getIntegerBitLength();
    }

    /** Return the number of bits to represent the mantissa.
     *  @return The length of the mantissa.
     */
    public int getMantissaBitLength() {
        return getIntegerBitLength() + getFractionBitLength();
    }

    /** Return the precision fore the mantissa of a compliant
     *  2's complement representation,
     *  @return The precision.
     */
    public abstract Precision getMantissaPrecision();

    /** Return the maximum quantizable value after scaling so that
     *  quantization levels are represented by adjacent integers.
     *  @return The maximum inclusive value.
     */
    public BigInteger getMaximumUnscaledValue() {
        int numBits = getNumberOfBits();
        return BigInteger.ZERO.setBit(numBits - 1).subtract(BigInteger.ONE);
    }

    /** Return the maximum quantizable value.
     *  @return The maximum inclusive value.
     */
    public double getMaximumValue() {
        double maxVal = getMaximumUnscaledValue().doubleValue();
        return maxVal * Math.pow(0.5, getFractionBitLength());
    }

    /** Return the minimum quantizable value after scaling so that
     *  quantization levels are represented by adjacent integers.
     *  @return The minimum inclusive value.
     */
    public BigInteger getMinimumUnscaledValue() {
        int numBits = getNumberOfBits();
        return BigInteger.ZERO.setBit(numBits - 1).negate();
    }

    /** Return the minimum quantizable value.
     *  @return The minimum inclusive value.
     */
    public double getMinimumValue() {
        double minVal = getMinimumUnscaledValue().doubleValue();
        return minVal * Math.pow(0.5, getFractionBitLength());
    }

    /** Return the modulo quantization range after scaling so that
     *  quantization levels are represented by adjacent integers.
     *  This is the same as the number of quantization levels in
     *  the mantissa..
     *  @return The modulo value.
     */
    public BigInteger getModuloUnscaledValue() {
        int numBits = getNumberOfBits();
        return BigInteger.ZERO.setBit(numBits);
    }

    /** Return the number of bits to represent the value.
     *  @return The number of bits.
     */
    public int getNumberOfBits() {
        return getMantissaBitLength() + getExponentBitLength();
    }

    /** Return the number of quantization levels in the mantissa.
     *  @return The number of levels.
     */
    public double getNumberOfLevels() {
        return Math.pow(2.0, getMantissaBitLength());
    }

    /** Return the overflow strategy.
     *  @return The overflow strategy.
     */
    public Overflow getOverflow() {
        return _overflow;
    }

    /** Return the rounding strategy.
     *  @return The rounding strategy.
     */
    public Rounding getRounding() {
        return _rounding;
    }

    /** Return the quantizable value nearest to and above zero.
     *  @return The positive value nearest to zero.
     */
    public double getTinyValue() {
        return getEpsilonValue();
    }

    /** Return the quantized value of integerPart + fracPart.
     *  fracPart must be greater than -1.0 and less than +1.0.
     *  @param integerPart The integer value to be bounded.
     *  @param fracPart The fractional part to be rounded away.
     *  @return The bounded and rounded integer value.
     */
    public BigInteger quantize(BigInteger integerPart, double fracPart) {
        BigInteger roundedVal = _rounding.quantize(integerPart, fracPart);
        BigInteger boundedVal = _overflow.quantize(roundedVal, this);
        return boundedVal;
    }

    /** Return a string representing this quantization.
     *  @return A string representing this quantization.
     */
    public abstract String toString();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The overflow strategy. */
    protected Overflow _overflow = Overflow.GENERAL;

    /** The rounding strategy. */
    protected Rounding _rounding = Rounding.GENERAL;
}
