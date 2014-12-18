/* A class that represents model time.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.util;

import java.math.BigInteger;

import ptolemy.util.DoubleUtils;

import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.ExtendedMath;

///////////////////////////////////////////////////////////////////
//// Time

/**
 * An object of the Time class represents time in a model. An instance of Time
 * has a value that is immutable. It has no limit on the magnitude, and
 * it does not lose resolution as the magnitude increases. There are
 * two time constants: NEGATIVE_INFINITY and POSITIVE_INFINITY.
 * <p>
 * The time value is quantized to the time resolution specified by the
 * <i>timeResolution</i> parameter of the associated director. The reason for
 * this is that without quantization, it is extremely difficult to compare two
 * time values with digit-to-digit accuracy because of the unpredictable
 * numerical errors introduced during computation. In practice, two time values
 * can only be distinguished if their difference can be detected by some
 * measuring instrument, which always has a smallest unit for measurement. This
 * smallest unit measurement gives the physical meaning of the time resolution
 * used for quantization.
 * <p>
 * This implementation of Time does not lose resolution as the magnitude of
 * the time increases (unlike floating point numbers). This is because
 * Time is represented internally as a multiple of the resolution, and
 * the multiple is not constrained to any limited magnitude.
 * <p>
 * The time value can be retrieved in three ways, the {@link #toString()}method
 * and the {@link #getDoubleValue()}method and the {@link #getLongValue()}
 * method. The first method returns a string representation while the second
 * method returns a double value. There are some limitations on both methods.
 * For the toString() method, we cannot directly do numerical operations on
 * strings. For the getDoubleValue() method, we can not guarantee that the
 * returned double value preserves the time resolution because of the limited
 * digits for double representation. We recommend to operate on time objects
 * directly instead of the time values of time objects. getLongValue() returns
 * an integer multiple of the director resolution.
 * <p>
 * Two operations, add and subtract, can be performed on a time object, where
 * the argument can be a double or a time object. If the argument is not a time
 * object, the argument is quantized before the operations are performed. These
 * operations return a new time object with a quantized result.
 * <p>
 * The time value of a time object can be infinite. The add and subtract
 * operations on infinite time values follow rules similar to the IEEE Standard
 * 754 for Floating Point Numbers. In particular, adding two positive or negative
 * infinities yield a positive or negative infinity; adding a positive infinity to
 * a negative infinity, however, triggers an ArithmeticException; the negation
 * of a positive or negative infinity is a negative or positive infinity,
 * respectively.
 * <p>
 * This class implements the Comparable interface, where two time objects can be
 * compared in the following way. If any of the two time objects contains an
 * infinite time value, the rules are: a negative infinity is equal to a
 * negative infinity and less than anything else; a positive infinity is equal
 * to a positive infinity and bigger than anything else. If none of the time
 * objects has an infinite time value, the time values of two time objects are
 * compared. If the time values are the same, the two time objects are treated
 * equal, or they represent the same model time. Otherwise, the time object
 * containing a bigger time value is regarded to happen after the time object
 * with a smaller time value.
 * <p>
 * All time objects share the same time resolution, which is provided by the
 * top-level director. In some domains, such as CT and DE, users can change the
 * time resolution by configuring the <i>timeResolution </i> parameter. The
 * default value for this parameter "1E-10", which has value 10 <sup>-10 </sup>.
 * To preserve the consistency of time values, timeResolution cannot be changed
 * when a model is running (attempting to do so will trigger an exception).
 *
 * @author Haiyang Zheng, Edward A. Lee, Elaine Cheong
 * @version $Id$
 * @since Ptolemy II 4.1
 * @Pt.ProposedRating Yellow (hyzheng)
 * @Pt.AcceptedRating Red (hyzheng)
 */
public class Time implements Comparable {
    /** Construct a Time object with zero as the time value. This object
     *  is associated with the given director, which provides the necessary
     *  information for quantization.
     *  @param director The director with which this time object is associated.
     *   This must not be null, or subsequent uses of the class will fail.
     */
    public Time(Director director) {
        _director = director;
        _timeValue = BigInteger.ZERO;
    }

    /** Construct a Time object with the specified double value as its
     *  time value. The specified director provides the resolution that
     *  is used to quantize the double value so that the value of the
     *  resulting Time object is a multiple of the precision.
     *  @param director The director with which this time object is associated.
     *  @param timeValue A double value as the specified time value.
     *  @exception ArithmeticException If the argument is NaN.
     *  @exception IllegalActionException If the given double time value does
     *  not match the time resolution.
     */
    public Time(Director director, double timeValue)
            throws IllegalActionException {
        _director = director;

        if (Double.isNaN(timeValue)) {
            throw new ArithmeticException("Time value can not be NaN.");
        }

        if (Double.isInfinite(timeValue)) {
            _timeValue = null;

            if (timeValue < 0) {
                _isNegativeInfinite = true;
            } else {
                _isPositiveInfinite = true;
            }
        } else {
            _timeValue = _doubleToMultiple(timeValue);
        }
    }

    /** Construct a Time object with the specified long value as its time value.
     *  @param director The director with which this time object is associated.
     *  @param timeValue A long value as the specified time value, as a multiple
     *   of the resolution.
     */
    public Time(Director director, long timeValue) {
        _director = director;
        _timeValue = new BigInteger(Long.toString(timeValue));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private constructor               ////

    /** Construct a Time object with the specified BigInteger value as its
     *  time value (as a multiple of the precision). The time object
     *  is associated with the given director,
     *  which provides the necessary information for quantization.
     *  This constructor is private and can only be accessed by the methods
     *  defined inside this class.
     *  @param director The director with which this time object is associated.
     *  @param timeValue The multiple of the precision that is the time value.
     */
    private Time(Director director, BigInteger timeValue) {
        _director = director;
        _timeValue = timeValue;
    }

    /** Construct a Time object with value that is one of _POSITIVE_INFINITY
     *  or _NEGATIVE_INFINITY.
     *  @param value One of _POSITIVE_INFINITY or _NEGATIVE_INFINITY.
     */
    private Time(int value) {
        if (value == _POSITIVE_INFINITY) {
            _timeValue = null;
            _isPositiveInfinite = true;
        } else if (value == _NEGATIVE_INFINITY) {
            _timeValue = null;
            _isNegativeInfinite = true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                          static  fields                   ////
    // Indicator to create negative infinite value.
    private static int _NEGATIVE_INFINITY = 0;

    // Indicator to create positve infinite value.
    private static int _POSITIVE_INFINITY = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    // NOTE: For the following constants, the director argument is null
    // because these constants are invariant to any time resolution.

    /** A static and final time constant holding a negative infinity.
     */
    public static final Time NEGATIVE_INFINITY = new Time(_NEGATIVE_INFINITY);

    /** A static and final time constant holding a positive infinity.
     */
    public static final Time POSITIVE_INFINITY = new Time(_POSITIVE_INFINITY);

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new time object whose time value is increased by the
     *  given double value. The specified double value is quantized
     *  to a multiple of the precision before it is added.
     *  @param timeValue The amount of the time increment.
     *  @return A new time object with the incremented time value.
     *  @exception ArithmeticException If the result is not a valid
     *  number (the argument is NaN or the sum would be), or the given time
     *  value does not match the time resolution.
     */
    public Time add(double timeValue) {
        // NOTE: a double time value can be either positive infinite,
        // negative infinite, or a NaN.
        if (Double.isNaN(timeValue)) {
            throw new ArithmeticException("Time: Time value can not be NaN.");
        }

        if (Double.isInfinite(timeValue)) {
            if (timeValue < 0) {
                // time value is a negative infinity
                if (_isPositiveInfinite) {
                    throw new ArithmeticException(
                            "Time: Adding a positive infinity to a negative "
                                    + "infinity results in an invalid time.");
                } else {
                    return NEGATIVE_INFINITY;
                }
            } else {
                // time value is a positive infinity
                if (_isNegativeInfinite) {
                    throw new ArithmeticException(
                            "Time: Adding a negative infinity to a positive "
                                    + "infinity results in an invalid time.");
                } else {
                    return POSITIVE_INFINITY;
                }
            }
        } else if (isInfinite()) {
            return this;
        } else {
            BigInteger quantizedValue;

            try {
                quantizedValue = _doubleToMultiple(timeValue);
            } catch (IllegalActionException e) {
                throw new ArithmeticException("Cannot guarantee the specified "
                        + "time resolution " + _timeResolution()
                        + " for the time value " + timeValue + ".\nTry "
                        + "choosing a greater time resolution "
                        + "by configuring the timeResolution parameter of the "
                        + "director.\n"
                        + "Check the stack trace to see which actor or "
                        + "parameter caused this exception.");
            }

            return new Time(_director, _timeValue.add(quantizedValue));
        }
    }

    /** Return a new time object whose time value is the sum of that of
     *  this time object and of the specified time object. The two time
     *  objects are expected to have directors with the same time resolution.
     *  If they do not, then the returned result is a new Time object
     *  representing the sum of the double values of the two Time objects.
     *  This would not be as accurate.
     *  @param time The time object contains the amount of time increment.
     *  @return A new time object with the quantized and incremented time value.
     *  @exception ArithmeticException If the result is not a valid number
     *   (it is the sum of positive and negative infinity).
     */
    public Time add(Time time) {
        // Note: a time value of a time object can be either positive infinite
        // or negative infinite.
        if (time._isNegativeInfinite) {
            // the time object has a negative infinity time value
            if (_isPositiveInfinite) {
                throw new ArithmeticException(
                        "Time: Adding a positive infinity to a negative "
                                + "infinity yields an invalid time.");
            } else {
                return NEGATIVE_INFINITY;
            }
        } else if (time._isPositiveInfinite) {
            // the time object has a positive infinity time value
            if (_isNegativeInfinite) {
                throw new ArithmeticException(
                        "Adding a negative infinity to a positive "
                                + "infinity yields an invalid time.");
            } else {
                return POSITIVE_INFINITY;
            }
        } else if (isInfinite()) {
            return this;
        }

        // Ensure the resolutions are the same.
        try {
            double resolution = _timeResolution();

            if (resolution != time._timeResolution()) {
                double thisValue = getDoubleValue();
                double thatValue = time.getDoubleValue();
                return new Time(_director, thisValue + thatValue);
            }
        } catch (IllegalActionException e) {
            // If the time resolution values are malformed this
            // should have been caught before this.
            throw new InternalErrorException(e);
        }

        return new Time(_director, _timeValue.add(time._timeValue));
    }

    /** Return -1, 0, or 1 if this time object is less than, equal to, or
     *  greater than the given argument. Note that a ClassCastException
     *  will be thrown if the argument is not an instance of Time.
     *  This object expects the directors associated with this and the
     *  specified Time objects to have the same time resolution. If this
     *  is not the case, then it compares the double representations of
     *  those time values, which is not as accurate.
     *  @param time A time object to compare to.
     *  @return The integer -1, 0, or 1 if this is less than, equal to, or
     *   greater than the argument.
     */
    @Override
    public int compareTo(Object time) {
        // NOTE: a time object may contain infinite time values.
        Time castTime = (Time) time;

        // If at least one of the time objects has an infinite time value,
        if (castTime.isInfinite() || isInfinite()) {
            if (castTime._isNegativeInfinite) {
                // the castTime object is a negative infinity.
                if (_isNegativeInfinite) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (castTime._isPositiveInfinite) {
                // the castTime object is a positive infinity.
                if (_isPositiveInfinite) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                // the castTime object is not infinite, this object must
                // be infinite.
                if (_isNegativeInfinite) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }

        double resolution = _timeResolution();

        if (resolution == castTime._timeResolution()) {
            return _timeValue.compareTo(castTime._timeValue);
        } else {
            double thisValue = getDoubleValue();
            double thatValue = castTime.getDoubleValue();

            if (thisValue < thatValue) {
                return -1;
            } else if (thisValue > thatValue) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /** Return true if this time object has the same time value as
     *  that of the given time object.
     *  @param time The time object that this time object is compared to.
     *  @return True if the two time objects have the same time value.
     */
    @Override
    public boolean equals(Object time) {
        if (time instanceof Time) {
            return this.compareTo(time) == 0;
        }
        return false;
    }

    /** Return the double representation of the time value of this
     *  time object.  Note that the returned result is not necessarily
     *  as accurate as the internal representation. In particular, if
     *  the internal representation is too large, then then the
     *  returned result may be infinite.  In addition, if the
     *  magnitude of the returned number is large relative to the time
     *  resolution of the associated director, then the result may be
     *  inaccurate by more than the time resolution.
     *  @return The double representation of the time value.
     */
    public double getDoubleValue() {
        if (_isPositiveInfinite) {
            return Double.POSITIVE_INFINITY;
        } else if (_isNegativeInfinite) {
            return Double.NEGATIVE_INFINITY;
        } else {
            // NOTE: Using doubleValue() here hugely increases the
            // execution time... Could instead use longValue(), but the
            // result would not necessarily be accurate.
            //return _timeValue.doubleValue() * _timeResolution();
            return DoubleUtils.bigToDouble(_timeValue) * _timeResolution();
        }
    }

    /** Return the long representation of the time value of this time
     *  object.  The long representation is a multiple of the
     *  resolution of the associated director.  Note that a Time value
     *  of positive infinity will return Long.MAX_VALUE and a Time
     *  value of negative infinity will return Long.MIN_VALUE.
     *  @return The long representation of the time value.
     */
    public long getLongValue() {
        if (_isPositiveInfinite) {
            return Long.MAX_VALUE;
        } else if (_isNegativeInfinite) {
            return Long.MIN_VALUE;
        } else {
            return _timeValue.longValue();
        }
    }

    /** Return the hash code for the time object. If two time objects contains
     *  the same quantized time value, they have the same hash code.
     *  Note that the quantization is performed on the time value before
     *  calculating the hash code.
     *  @return The hash code for the time object.
     */
    @Override
    public int hashCode() {
        if (_isNegativeInfinite) {
            return Integer.MIN_VALUE;
        } else if (_isPositiveInfinite) {
            return Integer.MAX_VALUE;
        } else {
            return _timeValue.hashCode();
        }
    }

    // FIXME: profiling shows that the following six methods are called
    // enormous times and performance can be greatly improved if no infinities
    // are supported.

    /** Return true if the current time value is infinite.
     *  @return true if the current time value is infinite.
     */
    public final boolean isInfinite() {
        return _isNegativeInfinite || _isPositiveInfinite;
    }

    /** Return true if the current time value is a negative value
     *  (including negative infinity).
     *  @return true if the current time value is a negative value
     *  (including negative infinity).
     */
    public final boolean isNegative() {
        if (_timeValue != null) {
            return _timeValue.signum() == -1;
        }
        return _isNegativeInfinite;
    }

    /** Return true if the current time value is a negative infinity.
     *  @return true if the current time value is a negative infinity.
     */
    public final boolean isNegativeInfinite() {
        return _isNegativeInfinite;
    }

    /** Return true if the current time value is a positive value
     *  (including positive infinity).
     *  @return true if the current time value is a positive value
     *  (including positive infinity).
     */
    public final boolean isPositive() {
        if (_timeValue != null) {
            return _timeValue.signum() == 1;
        }
        return _isPositiveInfinite;
    }

    /** Return true if the current time value is a positive infinity.
     *  @return true if the current time value is a positive infinity.
     */
    public final boolean isPositiveInfinite() {
        return _isPositiveInfinite;
    }

    /** Return true if the current time value is zero.
     *  @return true if the current time value is a zero.
     */
    public final boolean isZero() {
        if (_timeValue != null) {
            return _timeValue.signum() == 0;
        }
        return false;
    }

    /** Return the maximum value of time whose representation as a double
     *  is always accurate to the specified time resolution. In other words,
     *  if you ask this instance of Time for its value as a double, if the
     *  returned double is larger than the number returned by this method,
     *  then the double representation is not necessarily accurate to the
     *  specified resolution.
     *  @return The maximum value of time above which the double representation
     *   may not be accurate to the specified resolution.
     */
    public double maximumAccurateValueAsDouble() {
        // NOTE: when the time value is too big a multiple of the
        // resolution, the double representation
        // fails to deliver adequate precision.
        // Here is an example: if time resolution is 1E-12,
        // any double that is bigger than 8191.999999999999 cannot
        // distinguish itself from other bigger values (even
        // slightly bigger with the difference as small as the time
        // resolution). Therefore, 8191.999999999999 is the LUB of the
        // set of double values have the specified time resolution.
        // NOTE: The strategy to find the LUB for a given time
        // resolution r: find the smallest N such that time resolution
        // r >=  2^(-1*N); get M = 52 - N, which is the multiplication
        // we can apply on the significand without loss of time
        // resolution; the LUB is (1 + 1 - 1.0/2^(-52)) * 2^M.
        // For example: with the above example time resolution 1e-12,
        // we get N = 40, M = 12. Then we get the LUB as
        // 8191.999999999999. For time resolution as 1e-10, the lub is
        // 524287.99999999994.
        // NOTE: according to the IEEE754 floating point standard,
        // the formula to calculate a decimal value from a binary
        // representation is
        // (-1)^(sign)x(1+significand)x2^(exponent-127) for
        // signal precision and
        // (-1)^(sign)x(1+significand)x2^(exponent-1023)
        // for double presision.
        int minimumNumberOfBits = (int) Math.floor(-1
                * ExtendedMath.log2(_timeResolution())) + 1;
        int maximumGain = 52 - minimumNumberOfBits;

        return ExtendedMath.DOUBLE_PRECISION_SIGNIFICAND_ONLY
                * Math.pow(2.0, maximumGain);
    }

    /** Return a new time object whose time value is decreased by the
     *  given double value. Quantization is performed on both the
     *  timeValue argument and the result.
     *  @param timeValue The amount of time decrement.
     *  @return A new time object with time value decremented.
     */
    public Time subtract(double timeValue) {
        return add(-1 * timeValue);
    }

    /** Return a new time object whose time value is decreased by the
     *  time value of the specified time object. This method assumes that the two
     *  time values have directors with the same time resolution. If
     *  this is not the case, then the result is a new Time object whose
     *  value is constructed from the difference between the double values
     *  for this and the specified Time objects, using the time resolution
     *  of the director of this one.
     *  @param time The time object contains the amount of time decrement.
     *  @return A new time object with time value decremented.
     */
    public Time subtract(Time time) {
        if (time.isNegativeInfinite()) {
            return add(POSITIVE_INFINITY);
        } else if (time.isPositiveInfinite()) {
            return add(NEGATIVE_INFINITY);
        } else {
            return add(new Time(time._director, time._timeValue.negate()));
        }
    }

    /** Return the string representation of this time object.
     *  This is actually an approximation generated by first converting to a double.
     *  Note that the string representation of infinities can not be
     *  used to construct the time objects containing infinite time values.
     *  @return A String representation of this time object.
     */
    @Override
    public String toString() {
        if (_isPositiveInfinite) {
            return "Infinity";
        } else if (_isNegativeInfinite) {
            return "-Infinity";
        } else {
            return "" + getDoubleValue();

            // NOTE: Could use BigDecimal to get full resolution, as follows,
            // but the resulution is absurd.

            /*
             BigDecimal resolution = new BigDecimal(_director.getTimeResolution());
             BigDecimal scale = new BigDecimal(_timeValue);
             return resolution.multiply(scale).toString();
             */
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the time resolution for this Time object, which in this
     *  class is the time resolution of the director given in the
     *  constructor.
     *  This is a protected method to allow subclasses to use their
     *  own time resolution.
     *  @return The time resolution of the director.
     */
    protected double _timeResolution() {
        return _director.getTimeResolution();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a double, return the BigInteger that represents its
     *  quantized value. The BigInteger is the rounded result of dividing
     *  the double by the time resolution.
     *  @param value The value as a double.
     *  @return A BigInteger that specifies this double value as a multiple
     *  of the resolution given by the associated director.
     *  @exception IllegalActionException If the given double time value does
     *  not match the time resolution.
     */
    private BigInteger _doubleToMultiple(double value)
            throws IllegalActionException {
        // NOTE: when the value is too big a multiple of the resolution,
        // the division fails to deliver adequate precision. If this happens,
        // an illegal action exception will be thrown indicating the double
        // value does not match the specified time resolution.
        // Here is an example: if time resolution is 1E-12,
        // any double that is bigger than 8191.999999999999 cannot distinguish
        // itself from any other bigger values (even slightly bigger with the
        // difference as small as the time resolution). Therefore,
        // 8191.999999999999 is the LUB of the set of double values have the
        // specified time resolution.
        // NOTE: The strategy to find the LUB for a given time resolution r:
        // find the smallest N such that time resolution r >=  2^(-1*N);
        // get M = 52 - N, which is the multiplication we can apply on the
        // significand without loss of time resolution;
        // the LUB is (1 + 1 - 1.0/2^(-52)) * 2^M.
        // For example: with the above example time resolution 1e-12, we get
        // N = 40, M = 12. Then we get the LUB as 8191.999999999999.
        // NOTE: according to the IEEE754 floating point standard,
        // the formula to calculate a decimal value from a binary
        // representation is (-1)^(sign)x(1+significand)x2^(exponent-127) for
        // signal precision and (-1)^(sign)x(1+significand)x2^(exponent-1023)
        // for double precision.
        double precision = _timeResolution();
        long multiple = Math.round(value / precision);

        if (Math.abs(multiple * precision - value) > precision) {
            throw new IllegalActionException(
                    "The given time value "
                            + value
                            + " is too large to be converted precisely to an "
                            + "instance of Time with the specified time resolution of "
                            + precision
                            + ". The maximum value that can always be precisely converted is "
                            + maximumAccurateValueAsDouble()
                            + ". A number close to your value that can be converted is "
                            + multiple * precision);
        }

        return BigInteger.valueOf(multiple);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The director that this time object is associated with. */
    private Director _director;

    /** A boolean variable is true if the time value is a positive infinity.
     *  By default, it is false.
     */
    private boolean _isPositiveInfinite = false;

    /** A boolean variable is true if the time value is a negative infinity.
     *  By default, it is false.
     */
    private boolean _isNegativeInfinite = false;

    /** The time value, as a multiple of the resolution. */
    private BigInteger _timeValue = null;
}
