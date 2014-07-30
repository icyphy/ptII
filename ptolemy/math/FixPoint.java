/* A FixPoint data type.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import java.math.BigDecimal;
import java.math.BigInteger;

///////////////////////////////////////////////////////////////////
//// FixPoint

/**
 The FixPoint class provides a fixed point data type and a set of functions
 that operate on and return fixed point data. An instance of the class
 is immutable, meaning that its value is set in the constructor and
 cannot then be modified.  This is similar to the Java built-in classes
 like Double, Integer, etc.
 <p>
 The FixPoint class represents signed numbers in a two's-complement
 format or unsigned numbers in a binary format with unlimited dynamic
 range and a resolution defined by a finite number of fractional bits.
 The precision is defined by the
 {@link ptolemy.math.Precision Precision} class.
 <p>
 Because a fixed point data type uses a finite number of bits to
 represent a value, a real value is converted to a number that
 can be expressed with a given precision of the fixed point, thereby
 introducing a quantization error.

 The overflow and rounding strategies
 used for this quantization are defined by an instance of
 the {@link ptolemy.math.Quantization Quantization} class.
 <p>
 The design of the FixPoint class complies with a philosophy that all
 operators work losslessly, i.e. the fractional precision of the result
 is determined such there is no loss of precision. To insure lossless
 operations, the resulting Precision of these operators will expand as
 necessary. Subsequent results can be quantized as necessary.
 <p>
 Violations of the loss-less philosophy occur during construction, division
 and conversion to floating point. During construction from floating point
 values, the nearest fixed point representation is created. The preferred
 divide operator provides for explicit specification of the quantization.
 A deprecated divide operator guesses at the result precision.
 Conversion to floating point is limited by the available floating point
 accuracy.
 <p>
 The FixPoint implementation uses the Java class BigInteger to represent the
 finite value and so this implementation is truly platform independent.
 Note that the FixPoint does not put any restrictions on the maximum number
 of bits in the representation of a value.

 @author Bart Kienhuis, Ed Willink, Contributor: Edward A. Lee, Mike Wirthlin
 @version $Id$
 @since Ptolemy II 0.4, rewritten for Ptolemy II 2.2
 @Pt.ProposedRating Red (Ed.Willink)
 @Pt.AcceptedRating Red
 @see ptolemy.math.Precision
 @see ptolemy.math.Quantization
 */
public class FixPoint implements Cloneable {

    /** Construct a FixPoint by converting a bigDecimal to comply
     *  with a quantization specification.
     *
     *  @param bigDecimal The floating point value.
     *  @param quant The quantization specification.
     */
    public FixPoint(BigDecimal bigDecimal, Quantization quant) {
        _initFromBigDecimal(bigDecimal, quant);
    }

    /** Construct a new FixPoint object by requantizing the
     *  given FixPoint object with a new quantization specification.
     *
     *  TODO: This isn't the most efficient way of requantizing
     *  a value. Need to look into more efficient techniques for
     *  doing simple requantizations rather than converting into
     *  a BigDecimal. Proposal:
     *  - Create methods in Rounding to support Rounding
     *    of "shifted" BigInteger values
     *  - Perform rounding using shifting rather than
     *    conversion to BigDecimal
     *
     *  @param fix The existing FixPoint value
     *  @param quant The quantization specification.
     */
    public FixPoint(FixPoint fix, Quantization quant) {
        _initFromBigDecimal(fix.bigDecimalValue(), quant);
    }

    /** Construct a FixPoint from a double.
     *  Perform the conversion using the given
     *  quantization specification.
     *
     *  @param doubleValue The floating point value.
     *  @param quant The quantization specification.
     *  @exception IllegalArgumentException If the doubleValue is equal
     *   to Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
     *   or Double.NaN.
     */
    public FixPoint(double doubleValue, Quantization quant) {
        try {
            BigDecimal bigDecimal = new BigDecimal(doubleValue);
            _initFromBigDecimal(bigDecimal, quant);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NumberFormatException "
                    + "while converting \"" + doubleValue + "\" to a FixPoint.");
        }
    }

    /** Construct a FixPoint from an integer.
     *  Perform the conversion using the given quantization
     *  specification. During conversion, overflow handling
     *  may result in a lossy conversion.
     *
     *  @param intValue The integer value.
     *  @param quant The quantization specification.
     */
    public FixPoint(int intValue, Quantization quant) {
        BigInteger bigInt = new BigInteger(Integer.toString(intValue));
        _initFromBigInteger(bigInt, quant);
    }

    /** Construct a FixPoint from an integer.
     *  This is a loss-less conversion with a precision having
     *  as few bits as possible to represent the value and
     *  represented as a signed number.
     *
     *  @param intValue The integer value.
     */
    public FixPoint(int intValue) {
        this(intValue, true);
    }

    /** Construct a FixPoint from an integer.
     *  This is a loss-less conversion with a precision having
     *  as few bits as possible to represent the value and the
     *  signed determined by the signed parameter.
     *
     *  @param intValue The integer value.
     *  @param signed if true, represent value as a signed number;
     *  otherwise represent value as an unsigned value.
     */
    public FixPoint(int intValue, boolean signed) {
        // Create a new integer FixPoint value with
        // a small precision but with a "grow" overflow strategy.
        this(intValue, new FixPointQuantization(new Precision(signed ? 1 : 0,
                (signed ? 1 : 0) + 1, 0), Overflow.GROW, Rounding.HALF_EVEN));
    }

    /** Construct a FixPoint by converting the BigDecimal interpretation of
     *  a string to comply with a quantization specification.
     *
     *  @param string A string representation of the floating point value.
     *  @param quant The quantization specification.
     *  @exception IllegalArgumentException If string is not a valid
     *    representation of a BigDecimal.
     */
    public FixPoint(String string, Quantization quant) {

        try {
            BigDecimal bigDecimal = new BigDecimal(string);
            _initFromBigDecimal(bigDecimal, quant);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NumberFormatException "
                    + "while converting \"" + string + "\" to a FixPoint.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determines the Precision of an add operation between two
     *  FixPoint values.
     *
     *  The Precision of the result of an add between two FixPoint
     *  values is the Precision union of the two arguments plus
     *  one (to allow for bit growth).
     */
    public static Precision addPrecision(Precision leftArgument,
            Precision rightArgument) {
        Precision union = Precision.union(leftArgument, rightArgument);
        Precision newPrecision = new Precision(union.getSign(),
                union.getNumberOfBits() + 1, union.getExponent());
        return newPrecision;
    }

    /** Determines the Precision of an subtract operation between two
     *  FixPoint values.
     *
     *  The subtract precision will increment by one and always
     *  be signed.
     */
    public static Precision subtractPrecision(Precision leftArgument,
            Precision rightArgument) {
        Precision union = Precision.union(leftArgument, rightArgument);
        int length = union.getNumberOfBits() + 1;
        Precision newPrecision = new Precision(1, length, union.getExponent());
        return newPrecision;
    }

    /** Determines the Precision of an multiply operation between
     *  two FixPoint values.
     *
     *  The resulting Precision of a multiply between two FixPoint
     *  arguments is as follows: the integer location is the sum
     *  of the integer locations of the two arguments and the
     *  fractional location is the sum of the fractional locations
     *  of the two arguments.
     */
    public static Precision multiplyPrecision(Precision leftArgument,
            Precision rightArgument) {
        int sign = leftArgument.getSign() == 1 || rightArgument.getSign() == 1 ? 1
                : 0;
        int fractionBits = leftArgument.getFractionBitLength()
                + rightArgument.getFractionBitLength();
        int integerBits = leftArgument.getIntegerBitLength()
                + rightArgument.getIntegerBitLength();
        Precision newPrecision = new Precision(sign,
                fractionBits + integerBits, -fractionBits);
        return newPrecision;
    }

    /** Determines the Precision of a divide operation between
     *  two FixPoint values.
     *
     *  It is not possible to represent the result of an arbitrary
     *  divide with a finite precision. As such, this precision
     *  conversion rule is lossless. The rule for divide is as
     *  follows:
     *  - Integer part = left integer bits + right fraction bits + sign
     *  - Fraction part = left fractional bits + right integer bits + 1 - sign
     */
    public static Precision dividePrecision(Precision leftArgument,
            Precision rightArgument) {
        int sign = leftArgument.getSign() == 1 || rightArgument.getSign() == 1 ? 1
                : 0;
        int integerBits = leftArgument.getIntegerBitLength()
                + rightArgument.getFractionBitLength() + sign;
        int fractionBits = leftArgument.getFractionBitLength()
                + rightArgument.getIntegerBitLength() + 1 - sign;
        Precision newPrecision = new Precision(sign, sign + fractionBits
                + integerBits, -fractionBits);
        return newPrecision;
    }

    /** Return a FixPoint with a value equal to the absolute
     *  value of this FixPoint. The operation is lossless and
     *  the Precision of the result is the same Precision as
     *  this object.
     *
     *  @return A non-negative fixed point.
     */
    public FixPoint abs() {
        return new FixPoint(_value.abs(), _precision);
    }

    /** Return a FixPoint with a value equal to the sum of this
     *  FixPoint and the argument. The operation is lossless and
     *  will "expand" the Precision of the resulting FixPoint
     *  value to include the full precision of the result.
     *
     *  This operation will operate on either signed or unsigned
     *  fixed values. If both operands of an operation are unsigned,
     *  the result will be unsigned. If either of the operations are
     *  signed, the result will be signed.
     *
     *  @param arg The FixPoint addend.
     *  @return The FixPoint sum.
     */
    public FixPoint add(FixPoint arg) {

        // Align the two arguments
        int minExponent = Math.min(_precision.getExponent(),
                arg._precision.getExponent());
        BigInteger thisValue = _alignToExponent(minExponent);
        BigInteger thatValue = arg._alignToExponent(minExponent);

        // Perform the addition
        BigInteger newValue = thisValue.add(thatValue);

        // Create new precision
        /*
         int sign = _determineSign(_precision, arg._precision);
         int new_bits = newValue.bitLength() + sign;
         int max_bits = Math.max(_precision.getNumberOfBits(), arg._precision
         .getNumberOfBits());
         int bits = (new_bits > max_bits ? new_bits : max_bits);

         Precision newPrecision = new Precision(sign, bits, minExponent);
         */
        Precision newPrecision = addPrecision(_precision, arg._precision);
        return new FixPoint(newValue, newPrecision);
    }

    /** Return a FixPoint with a value equal to the sum of this
     *  FixPoint and the argument. The precision of this operation
     *  is set by the Quantization parameter and the result of the
     *  operation may be lossy as dictated by the desired quantization.
     *  <p>
     *
     *  @param arg The FixPoint addend.
     *  @param quant The quantization specification.
     *  @return The FixPoint sum.
     */
    public FixPoint add(FixPoint arg, Quantization quant) {
        return add(arg).quantize(quant);
    }

    /** Return the value of this FixPoint as a BigDecimal number.
     *  This is lossless, since the scale of the BigDecimal is set
     *  to the number of fractional bits.
     *
     *  @return The BigDecimal value of this FixPoint.
     */
    public BigDecimal bigDecimalValue() {
        return Precision.shiftBigDecimal(new BigDecimal(_value),
                _precision.getExponent());
    }

    /** Return this, that is, return the reference to this object.
     *  @return This FixPoint.
     */
    @Override
    public Object clone() {
        // FIXME: Note that we do not call super.clone() here.  Is that right?
        return this;
    }

    /** Return a FixPoint with a value equal to the division of
     *  this FixPoint by the argument. The operation is <b>not</b>
     *  lossless.
     *  <p>
     *
     *  @param arg The FixPoint.divisor.
     *  @return The FixPoint quotient.
     *  @exception IllegalArgumentException If division by zero and
     *  infinity not quantizable.
     */
    public FixPoint divide(FixPoint arg) throws IllegalArgumentException {

        /*
         int minExponent = Math.min(_precision.getExponent(), arg._precision
         .getExponent());
         int sign = _determineSign(_precision, arg._precision);
         int maxLength = Math.max(_precision.getNumberOfBits(), arg._precision
         .getNumberOfBits());
         Precision netPrecision = new Precision(sign, maxLength, minExponent);
         */
        Precision newPrecision = dividePrecision(this._precision,
                arg._precision);
        Quantization netQuantization = new FixPointQuantization(newPrecision,
                Overflow.TRAP, Rounding.NEAREST);
        return divide(arg, netQuantization);
    }

    /** Return a FixPoint equal to the division of this FixPoint by the
     *  argument, after application of a quantization specification to
     *  the result. The operation is <b>not</b> lossless.
     *  <p>
     *  Division by zero results in a value of plus or minus infinity,
     *  which throws an exception if the overflow strategy defined by
     *  the quantization specification returns
     *  null for plusInfinity or minusInfinity.
     *
     *  @param arg The FixPoint.divisor.
     *  @param quant The quantization specification.
     *  @return The FixPoint quotient.
     *  @exception IllegalArgumentException If division by zero.
     */
    public FixPoint divide(FixPoint arg, Quantization quant)
            throws IllegalArgumentException {

        try {

            BigDecimal numerator = new BigDecimal(this._value);
            BigDecimal denominator = new BigDecimal(arg._value);

            // Perform the division using the BigDecimal.divide
            // method. When calling this method, we need to know
            // how many bits to the right of the decimal place
            // to save (there can be an uncountable number of digits
            // to the right after the division). This is specified by
            // the base 10 "scale" value. We will set this base-10
            // scale value to the desired base 2 scale value
            // (i.e. the -exponent) and add one to give us an
            // extra digit.
            int resultExp = quant.getPrecision().getExponent();
            int scale = resultExp < 0 ? -resultExp : 0;
            scale++;
            BigDecimal result = numerator.divide(denominator, scale,
                    BigDecimal.ROUND_HALF_EVEN);

            int result_shift = this._precision.getExponent()
                    - arg.getPrecision().getExponent();
            result = Precision.shiftBigDecimal(result, result_shift);

            return new FixPoint(result, quant);

        } catch (ArithmeticException e) {
            Overflow anOverflow = quant.getOverflow();
            BigInteger infinity = _value.signum() >= 0 ? anOverflow
                    .plusInfinity(quant) : anOverflow.minusInfinity(quant);

                    if (infinity != null) {
                        return new FixPoint(infinity, quant.getPrecision());
                    }

                    throw new IllegalArgumentException("ArithmeticException "
                            + "while dividing " + toString() + " by " + arg.toString()
                            + '.');
        }
    }

    /** Return the value of this FixPoint as a double.  This
     *  is not necessarily lossless, since the precision of the fixed point
     *  number may exceed that of the double.
     *  @return The double value of this FixPoint.
     */
    public double doubleValue() {
        return _value.doubleValue() * Math.pow(2.0, _precision.getExponent());
    }

    /** Return true if this FixPoint is equal to the argument. Two
     *  FixPoints are considered equal when the two values are
     *  precisely the same. The two FixPoints need not therefore have
     *  the same number of fraction bits, but any additional fractional
     *  bits in one value must be zero in the other.
     *
     *  @param arg The FixPoint object to use for equality
     *  checking.
     *  @return True if the FixPoints are equal; false otherwise.
     */
    @Override
    public boolean equals(Object arg) {
        if (arg instanceof FixPoint) {
            int exponentBits = Math.min(_precision.getExponent(),
                    ((FixPoint) arg)._precision.getExponent());
            BigInteger thisValue = _alignToExponent(exponentBits);
            BigInteger thatValue = ((FixPoint) arg)
                    ._alignToExponent(exponentBits);
            return thisValue.equals(thatValue);
        }
        return false;
    }

    /** Get the Error condition from the FixValue.
     *  @return The error condition of the FixValue.
     *  @deprecated Overflow and rounding status no longer form part of
     *  the functionality. Use an overflow and rounding mode that gives the
     *  required behaviour or use Overflow.TRAP and/or Rounding.UNNECESSARY
     *  to throw exceptions if external interaction is required.
     */
    @Deprecated
    public Error getError() {
        return _error;
    }

    /** Return a precision to represent this number. This is constructed
     *  from the necessary fraction precision and the integer precision
     *  annotation.
     *  @return The Precision of this number.
     */
    public Precision getPrecision() {
        return _precision;
    }

    /** Return the unscaled BigInteger value used to represent this
     *  FixPoint value.
     *
     *  @return The BigInteger unscaled value of this number.
     */
    public BigInteger getUnscaledValue() {
        return _value;
    }

    /** Return a hash code value for this value. This method returns the
     *  low order 32 bits of the integer representation.
     *  @return A hash code value for this value.
     */
    @Override
    public int hashCode() {
        return _value.intValue();
    }

    /** Return a new FixPoint value that has the same value as this
     *  FixPoint value but with the minimum quantization necessary for
     *  representing the value.
     *
     *  The first step of this requantization is to check for
     *  "zero" bits at the LSB positions of the unscaled value.
     *  The value is shifted until the first "1" is found and
     *  the exponent is adjusted. Next, the length of the unscaled
     *  value is examined and the precision is set to the minimum
     *  length possible to represent the value.
     *
     *  @return The minimum quantize FixPoint value.
     */
    public FixPoint minimumQuantization() {

        int new_sign = _precision.getSign();

        // determine wasted bits in LSB locations & update exponent
        int shiftVal = _value.getLowestSetBit();
        BigInteger newVal = _value.shiftRight(shiftVal);
        int new_exponent = _precision.getExponent() + shiftVal;

        // determine minimum length of "new value"
        int new_bitlength = newVal.bitLength() + new_sign;

        Precision newPrecision = new Precision(new_sign, new_bitlength,
                new_exponent);
        return new FixPoint(newVal, newPrecision);
    }

    /** Return a FixPoint with a value equal to the product of this
     *  FixPoint and the argument. The operation is lossless and
     *  will "expand" the Precision of the resulting FixPoint
     *  value to include the full precision of the result.
     *
     *  This operation will operate on either signed or unsigned
     *  fixed values. If both operands of an operation are unsigned,
     *  the result will be unsigned. If either of the operations are
     *  signed, the result will be signed.
     *
     *  @param arg The FixPoint multiplier.
     *  @return The FixPoint product.
     */
    public FixPoint multiply(FixPoint arg) {

        // 1. Create FixPoint value with "worst case" precision
        BigInteger newValue = _value.multiply(arg._value);

        /*
         int new_sign = _determineSign(_precision, arg._precision);
         int new_exponent = _precision.getExponent()
         + arg._precision.getExponent();
         Precision worstCasePrecision = new Precision(new_sign, new_sign
         + newValue.bitLength(), new_exponent);
         FixPoint newVal = new FixPoint(newValue, worstCasePrecision);

         // 2. Requantize the value with the minimum quantization necessary
         newVal = newVal.minimumQuantization();

         // 3. Determine "Growth" precision for result. This will be the
         //    "maximium" of the precision of the arguments and the result.
         Precision newPrecision =
         Precision.union(_precision, arg._precision);
         newPrecision = Precision.union(newPrecision, newVal._precision);
         */

        Precision newPrecision = multiplyPrecision(_precision, arg._precision);
        FixPoint newVal = new FixPoint(newValue, newPrecision);

        // 4. Requantize the result with the new precision (this
        //    precision should be adequate and will not cause overflow
        //    or rounding.
        FixPoint fp = new FixPoint(newVal, new FixPointQuantization(
                newPrecision, Overflow.GENERAL, Rounding.GENERAL));
        return fp;
    }

    /** Return a FixPoint with a value equal to the product of this
     *  FixPoint and the argument. The precision of this operation
     *  is set by the Quantization parameter and the result of the
     *  operation may be lossy as dictated by the desired quantization.
     *  <p>
     *
     *  @param arg The FixPoint multiplicand.
     *  @param quant The quantization specification.
     *  @return The FixPoint product.
     */
    public FixPoint multiply(FixPoint arg, Quantization quant) {
        return multiply(arg).quantize(quant);
    }

    /** Print useful debug information about the FixPoint to standard
     *  out. This is used for debugging.
     */
    public void printFix() {
        System.out.println(" unscale Value  (2) " + _value.toString(2));
        System.out.println(" unscaled Value (10) " + _value.toString(10));
        System.out.println(" scale Value (10) " + doubleValue()
                + " Precision: " + getPrecision().toString());
        System.out.println(" BitCount:   " + _value.bitCount());
        System.out.println(" BitLength   " + _value.bitLength());

        BigInteger j = _value.abs();
        System.out.println(" ABS value   " + j.toString(2));
        System.out.println(" ABS bit count:  " + j.bitCount());
        System.out.println(" ABD bitLength:  " + j.bitLength());
        System.out.println(" Max value:  "
                + getPrecision().findMaximum().doubleValue());
        System.out.println(" Min value:  "
                + getPrecision().findMinimum().doubleValue());
    }

    /** Return the value after conversion to comply with a
     *  quantization specification. This method calls
     *  {@link #FixPoint(FixPoint, Quantization)}.
     *
     *  @param quant The quantization constraints.
     *  @return The bounded integer value.
     */
    public FixPoint quantize(Quantization quant) {
        return new FixPoint(this, quant);
    }

    /** Return a FixPoint with a value equal to the difference
     *  between this FixPoint and the argument. The operation is
     *  lossless and will "expand" the Precision of the resulting
     *  FixPoint value to include the full precision of the result.
     *
     *  This operation will operate on either signed or unsigned
     *  fixed values. If either of the operations are
     *  signed, the result will be signed. If both operands are
     *  unsigned and the result is positive, an unsigned value
     *  will be produced. If both operands are unsigned and the
     *  result is negative, a signed value will be generated.
     *
     *  @param arg The FixPoint subtrahend.
     *  @return The FixPoint difference.
     */
    public FixPoint subtract(FixPoint arg) {

        // Align the two arguments
        int minExponent = Math.min(_precision.getExponent(),
                arg._precision.getExponent());
        BigInteger thisValue = _alignToExponent(minExponent);
        BigInteger thatValue = arg._alignToExponent(minExponent);

        // Perform the subtraction
        BigInteger newValue = thisValue.subtract(thatValue);

        /*
         // Create new precision
         int sign = _determineSign(_precision, arg._precision);
         // If the sign of the subtract is negative and both arguments
         // were unsigned, make the result signed.
         if (sign == 0 && newValue.signum() == -1)
         sign = 1;

         int new_bits = newValue.bitLength() + sign;
         int max_bits = Math.max(_precision.getNumberOfBits(), arg._precision
         .getNumberOfBits());
         int bits = (new_bits > max_bits ? new_bits : max_bits);

         Precision newPrecision = new Precision(sign, bits, minExponent);
         */
        Precision newPrecision = subtractPrecision(_precision, arg._precision);
        return new FixPoint(newValue, newPrecision);
    }

    /** Return a FixPoint with a value equal to the difference of this
     *  FixPoint and the argument. The precision of this operation
     *  is set by the Quantization parameter and the result of the
     *  operation may be lossy as dictated by the desired quantization.
     *  <p>
     *
     *  @param arg The FixPoint addend.
     *  @param quant The quantization specification.
     *  @return The FixPoint sum.
     */
    public FixPoint subtract(FixPoint arg, Quantization quant) {
        return subtract(arg).quantize(quant);
    }

    /** Return a bit string representation of this number.
     *  The string takes the form "<i>sign integerBits . fractionBits</i>",
     *  where <i>sign</i> is - or nothing, <i>integerBits</i>
     *  and  <i>fractionBits</i> are
     *  each a sequence of "0" and "1". <i>integerBits</i> comprises no
     *  leading zeroes. <i>fractionBits</i> has exactly one digit per bit.
     *  If the fixed point number has zero or negative number of fraction
     *  bits, the dot is omitted.
     *  @return A binary string representation of the value.
     */
    public String toBitString() {

        // This simple line used to preserve as much of code as possible
        int _frac_bits = -_precision.getExponent();

        BigInteger integerPart = _value.shiftRight(_frac_bits);
        StringBuffer ln = new StringBuffer(integerPart.toString(2));

        if (_frac_bits > 0) {
            // The the toString(2) method of BigInteger removes the most
            // significant bits that are zeros, this method recreates
            // these zeroes to get the correct representation of the
            // fractional part.

            BigInteger fractionModulus = BigInteger.ZERO.setBit(_frac_bits);
            BigInteger fractionMask = fractionModulus.subtract(BigInteger.ONE);
            BigInteger fractionPart = _value.and(fractionMask);
            int minFracBits = fractionPart.bitLength();
            int extraLeadingFracBits = _frac_bits - minFracBits;
            ln.append(".");

            // Append the zeros
            for (int i = 0; i < extraLeadingFracBits; i++) {
                ln.append("0");
            }

            if (minFracBits > 0) {
                ln.append(fractionPart.toString(2));
            }
        }

        return ln.toString();
    }

    /** Return a string representation of this number.
     *  This is calculated by first converting the number to a BigDecimal,
     *  and then returning its string representation. In order to avoid
     *  loss there may be as many decimal digits following the decimal
     *  point as there fractional bits in this FixPoint.
     * @return A decimal string representation of the value.
     */
    @Override
    public String toString() {
        BigDecimal decimal = bigDecimalValue();
        String bigString = decimal.toString();

        if (bigString.indexOf('.') < 0) {
            if (bigString.indexOf('E') < 0) {
                return bigString;
            } else {
                // Java 1.5 release notes:
                // "The DecimalFormat class has been enhanced
                // to format and parse BigDecimal and BigInteger
                // values without loss of precision. Formatting of
                // such values is enhanced automatically; parsing into
                // BigDecimal needs to be enabled using the
                // setParseBigDecimal method."
                //
                // Formatter "An interpreter for printf-style format
                // strings, the Formatter class provides support for
                // layout justification and alignment, common formats
                // for numeric, string, and date/time data, and
                // locale-specific output. Common Java types such as
                // byte, java.math.BigDecimal , and java.util.Calendar
                // are supported. Limited formatting customization for
                // arbitrary user types is provided through the
                // java.util.Formattable interface."
                // As a result, sometimes we get 0E-12 or 0E-8 instead
                // of 0.0.
                if (bigString.startsWith("0E-")) {
                    // FIXME: This is a bit of a hack, we could be more
                    // robust and use regular expressions.
                    return "0.0";
                } else {
                    // This is probably an error, but give the user
                    // a chance.
                    return bigString;
                }
            }
        }

        // In order to preserve backward compatibility
        // we need to strip redundant trailing 0's.
        int i = bigString.length() - 1;

        while (bigString.charAt(i) == '0' && bigString.charAt(i - 1) != '.') {
            --i;
        }

        return bigString.substring(0, i + 1);
    }

    /** Return a string representation of this number along with
     *  the Precision of the number.
     *
     * @return A decimal string representation of the value and its
     *  precision.
     */
    public String toStringPrecision() {
        return toString() + _precision.toString();
    }

    /** Return a string representation of this number along with
     *  the unscaled value and Precision of the number.
     *
     * @return A decimal string representation of the value and its
     *  precision.
     */
    public String toStringValuePrecision() {
        return toString() + " [" + _precision.toString() + "=" + _value + "]";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         package methods                   ////

    /** Construct a FixPoint from an unscaled integerValue that
     *  with a given Precision constraint. This method will
     *  throw an exception if the precision is not sufficient to
     *  represent the given integer value.
     *
     *  @param unscaledIntegerValue The integer value of the scaled
     *  floating point value.
     *  @param precision The precision to represent the new unscaled value.
     *  @exception ArithmeticException when precision is not sufficient
     *  to represent integerValue.
     *
     *  Note that this is package scope
     */
    FixPoint(BigInteger unscaledIntegerValue, Precision precision) {
        if (Overflow.isOutOfRange(unscaledIntegerValue, precision)) {
            throw new ArithmeticException("Precision " + precision
                    + " not sufficient to represent " + unscaledIntegerValue);
        }
        _precision = precision;
        _value = unscaledIntegerValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private constructor                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the BigInteger whose fractional part is aligned with the
     *  provided precision. This never involves rounding, but only
     *  padding the fractional part with zeros
     *  @param exponent The precision with which to align
     *  @return A BigInteger with aligned precision
     */
    private BigInteger _alignToExponent(int exponent) {

        int exponentChange = exponent - _precision.getExponent();

        if (exponentChange < 0) {
            return _value.shiftLeft(-exponentChange);
        } else { // This is a private method so we know that extraBits is +ve
            return _value;
        }
    }

    /** Initialize the _value and _precision of this object from
     *  a BigDecimal value.
     *
     *  There are three steps involved with converting a BigDecimal
     *  value into a FixPoint value. The first step is to
     *  shift the BigDecimal value in a way that preserves
     *  all of the desired precision. Next, the shifted
     *  BigDecimal value is rounded using the appropriate
     *  rounding mode. The third and final step
     *  is to apply the appropriate Overflow strategy to
     *  the resulting BigInteger object.
     *
     *  After completing this method, the _value and _precision
     *  are set.
     *
     *  @param bigDecimal The floating point value.
     *  @param quant The quantization specification.
     */
    private void _initFromBigDecimal(BigDecimal bigDecimal, Quantization quant) {

        // Step 1: - Check to see if the number is negative and the
        //           precision is unsigned. If so, throw an exception.
        if (!quant.getPrecision().isSigned() && bigDecimal.signum() < 0) {
            throw new ArithmeticException("Attempting to create a unsigned"
                    + " FixPoint from a negative double:" + bigDecimal);
        }

        // Step 2: - Handle the right side (lsb) of the BigDecimal value
        //           by shifting.
        //   If the exponent is less than zero, shift the result left.
        //   If the exponent is greater than zero, shift the result right.
        BigDecimal shiftedDecimal = Precision.shiftBigDecimal(bigDecimal,
                -quant.getPrecision().getExponent());

        // Step 3: Round the shifted decimal value
        BigInteger roundedInteger = quant.getRounding().round(shiftedDecimal);

        // Step 4: Handle any overflow associated with this precision
        FixPoint newFix = quant.getOverflow().quantize(roundedInteger,
                quant.getPrecision());

        this._value = newFix._value;
        this._precision = newFix._precision;
    }

    /** Initialize the _value and _precision of this object from
     *  a BigInteger value.
     *
     *  The primary step in this method is to perform the
     *  "overflow" check and requantization. The BigInteger
     *  will be quantized using the overflow policy provided
     *  by the quantization parameter. See
     *  {@link Overflow#quantize(BigInteger, Precision)}.
     *
     *  After completing this method, the _value and _precision
     *  are set.
     *
     *  @param bigInteger The integer value.
     *  @param quant The quantization specification.
     */
    private void _initFromBigInteger(BigInteger bigInteger, Quantization quant) {

        // Step 1: - Check to see if the number is negative and the
        //           precision is unsigned. If so, throw an exception.
        if (!quant.getPrecision().isSigned() && bigInteger.signum() < 0) {
            throw new ArithmeticException("Attempting to create a unsigned"
                    + " FixPoint from a negative integer:" + bigInteger);
        }

        // Step 2: Shift the BigInteger value to match the desired
        //         Precision. There are two cases. If the exponent
        //         is greater than 0, then the integer will be
        //         shifted "right" and rounding may occur. In this
        //         case, call _initFromBigInteger to take advantage
        //         of the rounding in that method. If the exponent
        //         is less than 0, the integer must be shifted
        //         left with zeros in the lsbs. This is handled here.
        int desiredExponent = quant.getPrecision().getExponent();
        if (desiredExponent > 0) {
            // FIXME: It is possible that the BigInteger has all zeros
            // in the least significant bits that are shifted away. To
            // improve performance, check to see if this is a case and
            // perform the shift locally rather than going through the
            // BigDecimal code.
            _initFromBigDecimal(new BigDecimal(bigInteger), quant);
            return;
        } else if (desiredExponent < 0) {
            bigInteger = bigInteger.shiftLeft(-desiredExponent);
        }

        // Step 3: Handle any overflow associated with this precision
        FixPoint newFix = quant.getOverflow().quantize(bigInteger,
                quant.getPrecision());

        this._value = newFix._value;
        this._precision = newFix._precision;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The BigInteger comprising the logical floating point value
     *  multiplied by 2^exponent.
     */
    private BigInteger _value;

    /** The Precision of the given FixPoint value. **/
    private Precision _precision;

    /** The obsolete error condition of the FixValue */
    private Error _error = new Error();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An instance of this class is used preserve backwards interface
     *  compatibility. It contains a description that now states that
     *  there is no tracking of applied overflow or rounding.
     */
    public static class Error {
        // The only constructor is private so that this class cannot
        // be instantiated.
        private Error() {
        }

        /** Get a description of the Error.
         * @return A description of the Error.
         * @deprecated This functionality is obsolete.
         */
        @Deprecated
        public String getDescription() {
            return " Overflow status is no longer tracked.";
        }
    }
}
