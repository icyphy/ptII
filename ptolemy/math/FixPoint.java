/* A FixPoint data type.

Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (Ed.Willink@uk.thalesgroup.com)
@AcceptedRating Red
*/

package ptolemy.math;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

//////////////////////////////////////////////////////////////////////////
//// FixPoint
/**
The FixPoint class provides a fixed point data type and a set of functions
that operate on and return fixed point data. An instance of the class
is immutable, meaning that its value is set in the constructor and
cannot then be modified.  This is similar to the Java built-in classes
like Double, Integer, etc.
<p>
The FixPoint class represents signed numbers in a two's-complement
format with unlimited dynamic range and a resolution defined by a finite
number of fractional bits.
<p>
Because a fixed point data type uses a finite number of bits to
represent a value, a real value is converted to a number that
can be expressed with a given precision of the fixed point, thereby
introducing a quantization error. The overflow and rounding strategies
used for this quantization are defined by a Quantization instance.
<p>
The design of the FixPoint class complies with a philosophy that all
operators work losslessly, i.e. the fractional precision of the result
is determined such there is no loss of precision.
These changes are different for the various operations.
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

@author Bart Kienhuis, Ed Willink, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 0.4, rewritten for Ptolemy II 2.2
@see Precision
@see Quantization
*/

public class FixPoint implements Cloneable, Serializable {

    // For compatibility with an earlier implementation and in order to
    // support the current use of a FixPoint as a type exemplar, a FixPoint
    // also maintains a count of the number integer bits and consequently
    // the number of bits needed for the representation. This is
    // an annotation since it has no effect on the arithmetic results, which
    // is important since the annotations fail to account for numeric growth.

    /** Construct a FixPoint by converting a double to comply
     *  with a quantization specification.
     *
     *  @param doubleValue The floating point value.
     *  @param quant The quantization specification.
     *  @exception IllegalArgumentException If the doubleValue is equal
     *   to Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
     *   or Double.NaN.
     */
    public FixPoint(double doubleValue, Quantization quant) {
        _frac_bits = quant.getFractionBitLength();
        _int_bits = quant.getIntegerBitLength();
        _value = null;
        try {
            BigDecimal bigDecimal = new BigDecimal(doubleValue);
            _value = _integerValue(bigDecimal, quant);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NumberFormatException " +
                    "while converting \"" + doubleValue + "\" to a FixPoint.");
        }
    }

    /** Construct a FixPoint by converting a bigDecimal to comply
     *  with a quantization specification.
     *
     *  @param bigDecimal The floating point value.
     *  @param quant The quantization specification.
     */
    public FixPoint(BigDecimal bigDecimal, Quantization quant) {
        _frac_bits = quant.getFractionBitLength();
        _int_bits = quant.getIntegerBitLength();
        _value = _integerValue(bigDecimal, quant);
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
        _frac_bits = quant.getFractionBitLength();
        _int_bits = quant.getIntegerBitLength();
        _value = null;
        try {
            BigDecimal bigDecimal = new BigDecimal(string);
            _value = _integerValue(bigDecimal, quant);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NumberFormatException " +
                    "while converting \"" + string + "\" to a FixPoint.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a FixPoint with a value equal to the absolute
     *  value of this FixPoint. The operation is lossless.
     *  <p>
     *  The fractional precision of the result is equal to the
     *  fractional precision of the input.
     *  <p>
     *  The integer precision annotation of the result is equal to the
     *  the integer precision annotation of the input!
     *
     *  @return A non-negative fixed point.
     */
    public FixPoint abs() {
        return new FixPoint(_value.abs(), _int_bits, _frac_bits);
    }

    /** Return a FixPoint with a value equal to the sum of this
     *  FixPoint and the argument. The operation is lossless.
     *  <p>
     *  The fractional precision of the result is equal to the maximum of
     *  the fractional precisions of the inputs.
     *  <p>
     *  The integer precision annotation of the result is equal to the
     *  maximum of the integer precision annotations of the inputs!
     *
     *  @param arg The FixPoint addend.
     *  @return The FixPoint sum.
     */
    public FixPoint add(FixPoint arg) {
        int fracBits = Math.max(_frac_bits, arg._frac_bits);
        int intBits = Math.max(_int_bits, arg._int_bits);
        BigInteger thisValue = _alignToFraction(fracBits);
        BigInteger thatValue = arg._alignToFraction(fracBits);
        return new FixPoint(thisValue.add(thatValue), intBits, fracBits);
    }

    /** Return the value of this FixPoint as a BigDecimal number.
     *  This is lossless, since the scale of the BigDecimal is set
     *  to the number of fractional bits.
     *
     *  @return The BigDecimal value of this FixPoint.
     */
    public BigDecimal bigDecimalValue() {
        if (_frac_bits > 0) {
            // In order to avoid loss we must use the same number of powers
            // of ten in BigDecimal as we use powers of two in FixPoint.
            BigDecimal bigDecimal = new BigDecimal(_value);
            return bigDecimal.divide(_getTwoRaisedTo(_frac_bits),
                    _frac_bits, BigDecimal.ROUND_UNNECESSARY);
        }
        else if (_frac_bits == 0)
            return new BigDecimal(_value);
        else {
            BigDecimal bigDecimal = new BigDecimal(_value);
            return bigDecimal.multiply(_getTwoRaisedTo(-_frac_bits));
        }
    }

    /** Return this, that is, return the reference to this object.
     *  @return This XixPoint.
     */
    public Object clone() {
        return this;
    }

    /** Return a FixPoint with a value equal to the division of
     *  this FixPoint by the argument. The operation is <b>not</b>
     *  lossless.
     *  <p>
     *  The fractional precision of the result is equal to the maximum of
     *  the fractional precision of the divisor and dividend. Thus when a
     *  number with 3 fractional bits is divided by a FixPoint with 4
     *  fractional bits, the resulting FixPoint will be rounded to 4
     *  fractional bits.
     *  <p>
     *  The integer precision annotation of the result is equal to the
     *  maximum of the integer precision annotations of the inputs!
     *
     *  @param arg The FixPoint.divisor.
     *  @return The FixPoint quotient.
     *  @deprecated Use divide(FixPoint arg, Quantization quant).
     *  @exception IllegalArgumentException If division by zero and
     *  infinity not quantizable.
     */
    public FixPoint divide(FixPoint arg) throws IllegalArgumentException {
        // Align the precision of the two FixPoints
        int netFrac = Math.max(_frac_bits, arg._frac_bits);
        int netInt = Math.max(_int_bits, arg._int_bits);
        Precision netPrecision = new Precision(netInt + netFrac, netInt);
        Quantization netQuantization = new FixPointQuantization(
                netPrecision, Overflow.TRAP, Rounding.NEAREST);
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
        // Determine the extra results bits with respect to integer divide
        int fracBits = quant.getFractionBitLength();
        int intBits = quant.getIntegerBitLength();
        int extraBits = _frac_bits - arg._frac_bits + fracBits;
        BigInteger num = extraBits > 0 ?
            _value.shiftLeft(extraBits) : _value;
        BigInteger denom = extraBits < 0 ?
            arg._value.shiftLeft(-extraBits) : arg._value;
        try {
            BigInteger[] result = num.divideAndRemainder(denom);
            double rem = result[1].doubleValue() / denom.doubleValue();
            Rounding aRounding = quant.getRounding();
            return new FixPoint(aRounding.quantize(result[0], rem),
                    intBits, fracBits);
        } catch (ArithmeticException e) {
            Overflow anOverflow = quant.getOverflow();
            BigInteger infinity = _value.signum() >= 0
                ?  anOverflow.plusInfinity(quant)
                :  anOverflow.minusInfinity(quant);
            if (infinity != null)
                return new FixPoint(infinity, intBits, fracBits);
            throw new IllegalArgumentException("ArithmeticException while "
                    + "dividing " + toString() + " by " + arg.toString() + '.');
        }
    }

    /** Return the value of this FixPoint as a double.  This
     *  is not necessarily lossless, since the precision of the fixed point
     *  number may exceed that of the double.
     *  @return The double value of this FixPoint.
     */
    public double doubleValue() {
        return _value.doubleValue() * Math.pow(0.5, _frac_bits);
    }

    /** Return true if this FixPoint is equal to the argument. Two
     *  FixPoints are considered equal when the two values are
     *  precisely the same. The two FixPoints need not therefore have
     *  the same number of fraction bits, but any additional fractional
     *  bits in one value must be zero in the other.
     *  @return True if the FixPoints are equal; false otherwise.
     */
    public boolean equals(FixPoint arg) {
        int fracBits = Math.max(_frac_bits, arg._frac_bits);
        BigInteger thisValue = _alignToFraction(fracBits);
        BigInteger thatValue = arg._alignToFraction(fracBits);
        return thisValue.equals(thatValue);
    }

    /** Get the Error condition from the FixValue.
     *  @return The error condition of the FixValue.
     *  @deprecated Overflow and rounding status no longer form part of
     *  the functionality. Use an overflow and rounding mode that gives the
     *  required behaviour or use Overflow.TRAP and/or Rounding.UNNECESSARY
     *  to throw exceptions if external interaction is required.
     */
    public Error getError() {
        return _error;
    }

    /** Return a precision to represent this number. This is constructed
     *  from the necessary fraction precision and the integer precision
     *  annotation.
     *  @return The Precision of this number.
     */
    public Precision getPrecision() {
        return new Precision(_int_bits + _frac_bits, _int_bits);
    }

    /** Return a hash code value for this value. This method returns the
     *  low order 32 bits of the integer representation.
     *  @return A hash code value for this value.
     */
    public int hashCode() {
        return _value.intValue();
    }

    /** Return a FixPoint number with a value equal to the
     *  product of this number and the argument.
     *  The operation is lossless.
     *  <p>
     *  The fractional precision of the result is equal to the sum of the
     *  fractional precisions of the inputs.
     *  The integer precision of the result is equal to the sum of the
     *  integer precisions of the inputs.
     *
     *  @param arg The FixPoint multiplier.
     *  @return The FixPoint product.
     */
    public FixPoint multiply(FixPoint arg) {
        int fracBits = _frac_bits + arg._frac_bits;
        int intBits = _int_bits + arg._int_bits;
        BigInteger netValue = _value.multiply(arg._value);
        return new FixPoint(netValue, intBits, fracBits);
    }

    /** Print useful debug information about the FixPoint to standard
     *  out. This is used for debugging.
     */
    public void printFix() {
        System.out.println (" unscale Value  (2) " +
                _value.toString(2));
        System.out.println (" unscaled Value (10) " +
                _value.toString(10));
        System.out.println (" scale Value (10) " + doubleValue()
                + " Precision: " + getPrecision().toString());
        System.out.println (" BitCount:   " + _value.bitCount());
        System.out.println (" BitLength   " + _value.bitLength());
        BigInteger j = _value.abs();
        System.out.println (" ABS value   " + j.toString(2));
        System.out.println (" ABS bit count:  " + j.bitCount());
        System.out.println (" ABD bitLength:  " + j.bitLength());
        System.out.println (" Max value:  " +
                getPrecision().findMaximum().doubleValue());
        System.out.println (" Min value:  " +
                getPrecision().findMinimum().doubleValue());
    }

    /** Return the value after conversion to comply with a quantization
     *  specification.
     *  @param quant The quantization constraints.
     *  @return The bounded integer value.
     */
    public FixPoint quantize(Quantization quant) {
        int fracBits = quant.getFractionBitLength();
        int intBits = quant.getIntegerBitLength();
        int extraFracBits = fracBits - _frac_bits;
        if (extraFracBits > 0) {
            BigInteger bigInt = _value.shiftLeft(extraFracBits);
            return new FixPoint(quant.quantize(bigInt, 0.0),
                    intBits, fracBits);
        }
        else if (extraFracBits == 0)
            return new FixPoint(quant.quantize(_value, 0.0),
                    intBits, fracBits);
        else {
            BigInteger bigZero = BigInteger.ZERO;
            BigInteger bigOne = BigInteger.ONE;
            BigInteger fracWeight = bigZero.setBit(-extraFracBits);
            BigInteger fracMask = fracWeight.subtract(bigOne);
            BigInteger bigInt = _value.shiftRight(-extraFracBits);
            BigInteger bigFrac = _value.and(fracMask);
            double doubleFrac = bigFrac.doubleValue();
            double doubleWeight = fracWeight.doubleValue();
            double fracValue = doubleFrac / doubleWeight;
            return new FixPoint(quant.quantize(bigInt, fracValue),
                    intBits, fracBits);
        }
    }

    /** Return a FixPoint number with a value equal to this
     *  number minus the argument. The operation is lossless.
     *  <p>
     *  The fractional precision of the result is equal to the maximum of
     *  the fractional precisions of the inputs.
     *  <p>
     *  The integer precision annotation of the result is equal to the
     *  maximum of the integer precision annotations of the inputs!
     *
     *  @param arg The FixPoint subtrahend.
     *  @return The FixPoint difference.
     */
    public FixPoint subtract(FixPoint arg) {
        int fracBits = Math.max(_frac_bits, arg._frac_bits);
        int intBits = Math.max(_int_bits, arg._int_bits);
        BigInteger thisValue = _alignToFraction(fracBits);
        BigInteger thatValue = arg._alignToFraction(fracBits);
        return new FixPoint(thisValue.subtract(thatValue),
                intBits, fracBits);
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
        BigInteger integerPart = _value.shiftRight(_frac_bits);
        String ln = integerPart.toString(2);
        if (_frac_bits > 0) {
            // The the toString(2) method of BigInteger removes the most
            // significant bits that are zeros, this method recreates
            // these zeroes to get the correct representation of the
            // fractional part.
            BigInteger bigZero = BigInteger.ZERO;
            BigInteger bigOne = BigInteger.ONE;
            BigInteger fractionModulus = bigZero.setBit(_frac_bits);
            BigInteger fractionMask = fractionModulus.subtract(bigOne);
            BigInteger fractionPart = _value.and(fractionMask);
            int minFracBits = fractionPart.bitLength();
            int extraLeadingFracBits = _frac_bits - minFracBits;
            ln +=  ".";
            // Append the zeros
            for (int i = 0; i < extraLeadingFracBits; i++) {
                ln += "0";
            }
            if (minFracBits > 0) {
                ln += fractionPart.toString(2);
            }
        }
        return ln;
    }

    /** Return a string representation of this number.
     *  This is calculated by first converting the number to a BigDecimal,
     *  and then returning its string representation. In order to avoid
     *  loss there may be as many decimal digits following the decimal
     *  point as there fractional bits in this FixPoint.
     * @return A decimal string representation of the value.
     */
    public String toString() {

        // Java 1.5: The DecimalFormat class has been enhanced to format and
        // parse BigDecimal and BigInteger values without loss of
        // precision. Formatting of such values is enhanced
        // automatically; parsing into BigDecimal needs to be enabled
        // using the setParseBigDecimal method.

        String bigString = bigDecimalValue().toString();
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
        while ((bigString.charAt(i) == '0')
                && (bigString.charAt(i-1) != '.'))
            --i;
        return bigString.substring(0, i+1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private constructor                 ////

    /** Construct a FixPoint from an integerValue that has been shifted to
     *  incorporate fracBits from the logical floating point value.
     *
     *  @param integerValue The integer value of the scaled floating point
     *    value.
     *  @param fracBits The number of bits of fractional accuracy.
     *  @param intBits The number of bits of integer representation.
     */
    private FixPoint(BigInteger integerValue, int intBits, int fracBits) {
        _frac_bits = fracBits;
        _int_bits = intBits;
        _value = integerValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the BigInteger whose fractional part is aligned with the
     *  provided precision. This never involves rounding, but only
     *  padding the fractional part with zeros
     *  @param The precision with which to align
     *  @return A BigInteger with aligned precision
     */
    private BigInteger _alignToFraction(int fracBits) {
        int extraBits = fracBits - _frac_bits;
        if (extraBits > 0)
            return _value.shiftLeft(extraBits);
        else  // This is a private method so we know that extraBits is +ve
            return _value;
    }

    /** Get the BigDecimal which is the 2^exponent. If the value is
     *  already calculated, return this cached value, else calculate
     *  the value.
     *
     *  @param number the exponent.
     *  @return the BigDecimal representing 2^exponent.
     */
    private BigDecimal _getTwoRaisedTo(int number) {
        // Since this is a private method we know that number is positive.
        if (number < _twoRaisedTo.length)
            return _twoRaisedTo[number];
        else
            return new BigDecimal(BigInteger.ZERO.setBit(number));
    }

    /** Return the rounded integer value of
     *  bigDecimal * pow(2, quant.getFractionBitLength()).
     *
     *  @param bigDecimal The floating point value.
     *  @param quant The quantization specification.
     *  @return The rounded BigInteger value.
     */
    private BigInteger _integerValue(BigDecimal bigDecimal,
            Quantization quant) {
        int fracBits = quant.getFractionBitLength();
        if (fracBits > 0)
            bigDecimal = bigDecimal.multiply(_getTwoRaisedTo(fracBits));
        else if (fracBits < 0) {
            int netScale = bigDecimal.scale() - fracBits;
            bigDecimal = bigDecimal.divide(_getTwoRaisedTo(-fracBits),
                    netScale, BigDecimal.ROUND_UNNECESSARY);
        }
        BigInteger bigInteger = bigDecimal.toBigInteger();
        BigDecimal bigIntDecimal = new BigDecimal(bigInteger);
        BigDecimal bigRemainder = bigDecimal.subtract(bigIntDecimal);
        double remainder = bigRemainder.doubleValue();
        return quant.quantize(bigInteger, remainder);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The BigInteger comprising the logical floating point value
     *  multiplied by (1 << _frac_bits)
     */
    private BigInteger _value;

    /** The number of fraction bits within _value */
    private int _frac_bits;

    /** The number of integer bits within _value.
     *  This value is maintained to support FixType which relies on a
     *  value exemplar to define the full type characteristics. It only
     *  affects this class in so far as new instances may have a changed
     *  value, but this changed value is of limited utility since it just
     *  maintains backward compatibility. It is certainly wrong
     *  for cases such as the abs of max negative.
     */
    private int _int_bits;

    ///////////////////////////////////////////////////////////////////
    ////                         static variables                  ////

    /** The size of the pre-computed _twoRaisedTo powers of two array.
     *   65 entries are used to cache all powers of 2 from 0 to 64.
     **/
    private static final int TWORAISEDTOSIZE = 64+1;

    /** Calculate the table containing 2^x, with 0 <= x < TWORAISEDTOSIZE.
     *   Purpose is to speed up calculations involving calculating
     *   2^x. The table is calculated using BigDecimal, since this
     *   make the transformation from string of bits to a double
     *   easier.
     **/
    private static BigDecimal[] _twoRaisedTo =
    new BigDecimal[TWORAISEDTOSIZE];

    /** The obsolete error condition of the FixValue */
    private Error _error = new Error();

    ///////////////////////////////////////////////////////////////////
    ////                      static initializers                  ////

    static {
        BigDecimal powerOf2 = BigDecimal.valueOf(1);
        for (int i = 0; i < _twoRaisedTo.length; i++) {
            _twoRaisedTo[i] = powerOf2;
            powerOf2 = powerOf2.add(powerOf2);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An instance of this class is used preserve backwards interface
     *  compatibility. It contains a description that now states that
     *  there is no tracking of applied overflow or rounding.
     */
    public static class Error {
        // The only constructor is private so that this class cannot
        // be instantiated.
        private Error() {}

        /** Get a description of the Error.
         * @return A description of the Error.
         * @deprecated This functionality is obsolete.
         */
        public String getDescription() {
            return " Overflow status is no longer tracked.";
        }
    }
}
