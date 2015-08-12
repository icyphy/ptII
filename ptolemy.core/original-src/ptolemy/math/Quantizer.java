/* A collection of methods for creating fixed point values.

 Copyright (c) 1998-2014 The Regents of the University of California
 and Research in Motion Limited.  All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND
 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED HAS NO
 OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.math;

import java.math.BigDecimal;

///////////////////////////////////////////////////////////////////
//// Quantizer

/**
 This class provides a set of static methods for creating instances of
 the FixPoint class from doubles, integers, or fixed point numbers.
 The various round() methods return a fixed point value that is nearest
 to the specified number, but has the specified precision.  The various
 roundToZero() and truncate() methods return a fixed point value that
 is nearest to the specified number, but no greater in magnitude.  The
 various roundDown() methods return a the nearest fixed point less than
 the argument. The intention is to fill out this class with roundUp(),
 and roundNearestEven().  All of these methods may introduce
 quantization errors and/or overflow.

 This class has been reimplemented to perform all rounding by first
 constructing a FixPointQuantization and then using either the quantizing
 constructor of FixPoint or FixPoint.quantize. Users may find that their
 code simplifies if they do likewise.

 @author Bart Kienhuis, Edward A. Lee, Ed Willink
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (kienhuis)
 @Pt.AcceptedRating Red (kienhuis)
 @see FixPoint
 @see Overflow
 @see Precision
 @see Rounding
 @see Quantization
 */
public class Quantizer {
    // The only constructor is private so that this class cannot
    // be instantiated.
    private Quantizer() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the fixed point number that is nearest to the specified
     *  value, but has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint round(double value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.NEAREST);
        return new FixPoint(new BigDecimal(value), q);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint round(BigDecimal value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.NEAREST);
        return new FixPoint(value, q);
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
     *
     *  @param value The value to represent.
     *  @param newPrecision The precision of the representation.
     *  @param mode The overflow mode.
     *  @return A new fixed-point representation of the value.
     */
    public static FixPoint round(FixPoint value, Precision newPrecision,
            Overflow mode) {
        Quantization q = new FixPointQuantization(newPrecision, mode,
                Rounding.NEAREST);
        return value.quantize(q);
    }

    /** Return the nearest fixed point number with less than or equal
     *  magnitude that has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint roundDown(double value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.DOWN);
        return new FixPoint(new BigDecimal(value), q);
    }

    /** Return the nearest fixed point number with less than or equal
     *  magnitude that has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint roundDown(BigDecimal value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.DOWN);
        return new FixPoint(value, q);
    }

    /** Return the nearest fixed point number with less than or equal
     *  magnitude that has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value depends on the specified mode.
     *  If the mode is SATURATE, then the return value is either
     *  the maximum or minimum value possible with the given
     *  precision, depending on the sign of the
     *  specified number.  If the mode is OVERFLOW_TO_ZERO,
     *  then the return value is zero.
     *
     *  @param value The value to represent.
     *  @param newPrecision The precision of the representation.
     *  @param mode The overflow mode.
     *  @return A new fixed-point representation of the value.
     */
    public static FixPoint roundDown(FixPoint value, Precision newPrecision,
            Overflow mode) {
        Quantization q = new FixPointQuantization(newPrecision, mode,
                Rounding.DOWN);
        return value.quantize(q);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has the given precision, possibly introducing
     *  quantization or overflow errors.  If the rounded digit is
     *  five, then the last digit that is not discarded will be
     *  rounded to make it even.  An overflow error occurs if the
     *  specified number does not fit within the range possible with
     *  the specified precision. In that case, the returned value is
     *  either the maximum or minimum value possible with the given
     *  precision, depending on the sign of the specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint roundNearestEven(double value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.HALF_EVEN);
        return new FixPoint(new BigDecimal(value), q);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has the given precision, possibly introducing
     *  quantization or overflow errors.  If the rounded digit is
     *  five, then the last digit that is not discarded will be
     *  rounded to make it even.  An overflow error occurs if the
     *  specified number does not fit within the range possible with
     *  the specified precision. In that case, the returned value is
     *  either the maximum or minimum value possible with the given
     *  precision, depending on the sign of the specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint roundNearestEven(BigDecimal value,
            Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.HALF_EVEN);
        return new FixPoint(value, q);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has the given precision, possibly introducing
     *  quantization or overflow errors.  If the rounded digit is
     *  five, then the last digit that is not discarded will be
     *  rounded to make it even.  An overflow error occurs if the
     *  specified number does not fit within the range possible with
     *  the specified precision. In that case, the returned value
     *  depends on the specified mode.  If the mode is SATURATE, then
     *  the return value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of
     *  the specified number.  If the mode is OVERFLOW_TO_ZERO, then
     *  the return value is zero.
     *
     *  @param value The value to represent.
     *  @param newPrecision The precision of the representation.
     *  @param mode The overflow mode.
     *  @return A new fixed-point representation of the value.
     */
    public static FixPoint roundNearestEven(FixPoint value,
            Precision newPrecision, Overflow mode) {
        Quantization q = new FixPointQuantization(newPrecision, mode,
                Rounding.HALF_EVEN);
        return value.quantize(q);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has magnitude no greater that the specified value,
     *  and has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint roundToZero(double value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.DOWN);
        return new FixPoint(new BigDecimal(value), q);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has magnitude no greater that the specified value,
     *  and has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint roundToZero(BigDecimal value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.DOWN);
        return new FixPoint(value, q);
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
     *
     *  @param value The value to represent.
     *  @param newPrecision The precision of the representation.
     *  @param mode The overflow mode.
     *  @return A new fixed-point representation of the value.
     */
    public static FixPoint roundToZero(FixPoint value, Precision newPrecision,
            Overflow mode) {
        Quantization q = new FixPointQuantization(newPrecision, mode,
                Rounding.DOWN);
        return value.quantize(q);
    }

    /** Return the smallest greater than or equal fixed point number
     *  that has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint roundUp(double value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.UP);
        return new FixPoint(new BigDecimal(value), q);
    }

    /** Return the smallest greater than or equal fixed point number
     *  that has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number.
     *
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     */
    public static FixPoint roundUp(BigDecimal value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.UP);
        return new FixPoint(value, q);
    }

    /** Return the smallest greater than or equal fixed point number
     *  that has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value depends on the specified mode.
     *  If the mode is SATURATE, then the return value is either
     *  the maximum or minimum value possible with the given
     *  precision, depending on the sign of the
     *  specified number.  If the mode is OVERFLOW_TO_ZERO,
     *  then the return value is zero.
     *
     *  @param value The value to represent.
     *  @param newPrecision The precision of the representation.
     *  @param mode The overflow mode.
     *  @return A new fixed-point representation of the value.
     */
    public static FixPoint roundUp(FixPoint value, Precision newPrecision,
            Overflow mode) {
        Quantization q = new FixPointQuantization(newPrecision, mode,
                Rounding.UP);
        return value.quantize(q);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has magnitude no greater that the specified value,
     *  and has the given precision, possibly introducing quantization
     *  or overflow errors.  An overflow error occurs if the specified
     *  number does not fit within the range possible with the
     *  specified precision. In that case, the returned value is
     *  either the maximum or minimum value possible with the given
     *  precision, depending on the sign of the specified number.
     *
     *  <p> Note: This method does NOT perform truncation per most
     *  fixed-point DSP implementations, which simply drop the
     *  fractional bits.  Most models of fixed-point algorithms will
     *  use the roundDown methods in this class instead.
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     *  @deprecated Use roundToZero instead.
     */
    @Deprecated
    public static FixPoint truncate(double value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.TRUNCATE);
        return new FixPoint(new BigDecimal(value), q);
    }

    /** Return the fixed point number that is nearest to the specified
     *  value, but has magnitude no greater that the specified value,
     *  and has the given precision, possibly introducing
     *  quantization or overflow errors.
     *  An overflow error occurs if the specified number does not fit
     *  within the range possible with the specified precision. In that
     *  case, the returned value is either the maximum or minimum value
     *  possible with the given precision, depending on the sign of the
     *  specified number.
     *
     *  <p> Note: This method does NOT perform truncation per most
     *  fixed-point DSP implementations, which simply drop the
     *  fractional bits.  Most models of fixed-point algorithms will
     *  use the roundDown methods in this class instead.
     *  @param value The value to represent.
     *  @param precision The precision of the representation.
     *  @return A fixed-point representation of the value.
     *  @deprecated Use roundToZero instead.
     */
    @Deprecated
    public static FixPoint truncate(BigDecimal value, Precision precision) {
        Quantization q = new FixPointQuantization(precision, Overflow.SATURATE,
                Rounding.TRUNCATE);
        return new FixPoint(value, q);
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
     *
     *  <p> Note: This method does NOT perform truncation per most
     *  fixed-point DSP implementations, which simply drop the
     *  fractional bits.  Most models of fixed-point algorithms will
     *  use the roundDown methods in this class instead.
     *  @param value The value to represent.
     *  @param newPrecision The precision of the representation.
     *  @param mode The overflow mode.
     *  @return A new fixed-point representation of the value.
     *  @deprecated Use roundToZero instead.
     */
    @Deprecated
    public static FixPoint truncate(FixPoint value, Precision newPrecision,
            Overflow mode) {
        Quantization q = new FixPointQuantization(newPrecision, mode,
                Rounding.TRUNCATE);
        return value.quantize(q);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that overflow should saturate. */
    public static final Overflow SATURATE = Overflow.SATURATE;

    /** Indicate that overflow should result in a zero value. */
    public static final Overflow OVERFLOW_TO_ZERO = Overflow.TO_ZERO;
}
