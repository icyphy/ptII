/* A class that represents model time.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import java.math.BigDecimal;

import ptolemy.actor.Director;
import ptolemy.kernel.util.InternalErrorException;


//////////////////////////////////////////////////////////////////////////
//// Time
/**
   An object of the Time class represents model time in a model. It is 
   different from the real time of the physical world. This object is 
   immutable. It contains a BigDecimal number to record the time value, 
   which provides an arbitrary precision and an accuracy as small as 10^(-2^32).
   There are two time constants specified in this class: NEGATIVEINFINITY and 
   POSITIVEINFINITY.
   
   <p> The time value contained by a time object is quantized according to
   the time resolution specified by the <i>timeScale</i> parameter. The reason 
   for this is that without quantization, it is extremely difficult to compare 
   two time values with bit-to-bit accuracy because of the unpredictable 
   numerical errors introduced during computation. 
   In practice, two time values can only be distinguished if their difference 
   can be detected by some measuring instrument, which always has a smallest 
   unit for measurement. This smallest unit measurement gives the physical 
   meaning of the time resolution used for quantization. The quantization is 
   performed with the half-even rounding mode by default.        
   
   <p> The time value can be retrived in two ways, the 
   {@link #getBigDecimalValue()} method 
   and the {@link #getDoubleValue()} method. The first method returns a 
   BigDecimal while the second method returns a double value. There are some
   limitations on both methods. First, the getBigDecimalValue method can not 
   be applied on the above two time constants, because BigDecimal can not be 
   used to represent infinities. Second, the getDoubleValue method can not 
   garantee that the returned double value preserves the time resolution 
   because of the limited digits for double representation. The 
   getBigDecimalValue method is recommanded and the getDoubleValue is 
   deprecated but kept to support backwards compatibility. 
   
   <p> Two operations, add and subtract, can be performed on a time object, 
   where the argument can be a double, a BigDecimal, or a time object. If the 
   argument is not a time object, the argument is quantized before the 
   operations are performed. These operations return a new time object with a 
   quantized result. 
   
   <p> The time value of a time object can be infinite. The add and subtract
   operations on infinite time values follow the rules of the IEEE Standard 754 
   Floating Point Numbers. In particular, adding two positive/negative 
   infinities yield a positive/negative infinity; adding a positive infinity 
   and a negative infinity yields NaN; the negation of a positive/negative 
   infinity is a negative/positive infinity.

   <p> This class implements the Comparable interface, where two time 
   objects can be compared in the following way. If any of the two time objects
   contains an infinite time value, the rules are: a negative infinity is 
   equal to a negative infinity and less than anything else; a positive 
   infinity is equal to a positive infinity and bigger than anything else. 
   If none of the time objects has an infinite time value, the time values of 
   two time objects are compared. If the time values are the same, the two time 
   objects are treated equal, or they represent the same model time. 
   Otherwise, the time object containing a bigger time value is 
   regared to happen after the time object with a smaller time value. 
   
   <p> All time objects share the same time resolution, which is provided by
   the top-level director. In some domains, such as CT and DE, users can 
   change the time resolution by configuring the <i>timeScale</i> parameter. In 
   decimal arithmetics, a scale is the number of the digits to the right of
   the decimal point (the fractional part). The default value for time scale
   is 10. The corresponding time resolution is 10^(-1*scale). Suppose a time 
   object with a time value 1.636, if scale is 3, the fractional part is 0.636; 
   if scale is 1, the fraction part is 0.6. Note that only the change of 
   timeScale at the top-level director takes effect. What is more, to preserve 
   the consistency of time values, scale can not be changed when a model 
   is running. See {@link ptolemy.actor.Director#setTimeScale}.
   
   @author Haiyang Zheng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class Time implements Comparable {
    
    /** Construct a Time object with zero as the time value. This object
     *  is associated with the given director, which provides the necessary
     *  information for quantization. 
     *  @param director The director with which this time object is associated.
     */
    public Time(Director director) {
        _director = director;
        _timeValue = _quantizeTimeValue(new BigDecimal(0));
    }

    /** Construct a Time object with the specified BigDecimal value as its
     *  time value. The time object is associated with the given director, 
     *  which provides the necessary information for quantization.  
     *  @param director The director with which this time object is associated.
     *  @param timeValue A BidDecimal value as the specified time value.
     */
    public Time(Director director, BigDecimal timeValue) {
        _director = director;
        _timeValue = _quantizeTimeValue(timeValue);
    }

    /** Construct a Time object with the specified double value as its
     *  time value. The time object is associated with the given director, 
     *  which provides the necessary information for quantization.  
     *  The double value can not be NaN, otherwise, a NumberFormatException
     *  will be thrown.  
     *  @param director The director with which this time object is associated.
     *  @param timeValue A double value as the specified time value.
     */
    public Time(Director director, double timeValue) {
        _director = director;
        if (Double.isNaN(timeValue))
            throw new NumberFormatException("Time value can not be NaN.");
        if (Double.isInfinite(timeValue)) {
            _timeValue = null;
            if (timeValue < 0) {
                _isNegativeInfinite = true;
            } else {
                _isPositiveInfinite = true;
            }
        } else {
            _timeValue = _quantizeTimeValue(new BigDecimal(timeValue));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public  fields                   ////
    // NOTE: BigDecimal does not support infinity. 
    // NOTE: For the following constants, the director argument is null 
    // because these constants are invariant to any time resolution.
    /** A static and final time constant holding a negative infinity. 
     */
    public static final Time NEGATIVEINFINITY 
        = new Time(null, Double.NEGATIVE_INFINITY); 
    
    /** A static and final time constant holding a positive infinity.
     */
    public static final Time POSITIVEINFINITY 
        = new Time(null, Double.POSITIVE_INFINITY); 
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new time object whose time value is increased by the 
     *  time value of the given BigDecimal. If the time value of this
     *  time object is infinite, return this time object. Quantization 
     *  is performed on both the timeValue argument and the result. 
     *  @param timeValue The amount of time increment.
     *  @return A new time object with the quantized and incremented time value.
     */
    public Time add(BigDecimal timeValue) {
        // NOTE: A BigDecimal can not have its value being infinite or NaN, 
        // so we only need to check whether this time object is infinite.
        if (isInfinite()) {
            // NOTE: we do not create a new time object since infinities
            // are static and final constants of the time class.
            return this;
        } else {
            BigDecimal quantizedTimeValue = _quantizeTimeValue(timeValue);
            return new Time(_director, _timeValue.add(quantizedTimeValue));
        }
    }

    /** Return a new time object whose time value is increased by the 
     *  given double value. Quantization is performed on both the timeValue 
     *  argument and the result. 
     *  The double value can not be NaN and the result time value can not
     *  be NaN. 
     *  @param timeValue The amount of time increment.
     *  @return A new time object with the quantized and incremented time value.
     */
    public Time add(double timeValue) {
        // NOTE: a double time value can be either positive infinite,
        // negative infinite, or a NaN.
        if (Double.isNaN(timeValue))
            throw new InternalErrorException("Time value can not be a NaN.");
        if (Double.isInfinite(timeValue)) {
            if (timeValue < 0) {
                // time value is a negative infinity
                if (isPositiveInfinite()) {
                    throw new InternalErrorException(
                            "Adding a positive infinity to a negative " +
                            "infinity yields a NaN.");
                } else {
                    return NEGATIVEINFINITY;
                }
            } else {
                // time value is a positive infinity
                if (isNegativeInfinite()) {
                    throw new InternalErrorException(
                            "Adding a negative infinity to a positive " +
                            "infinity yields a NaN.");
                } else {
                    return POSITIVEINFINITY;
                }
            }
        } else {
            return add(new BigDecimal(timeValue));
        }
    }

    /** Return a new time object whose time value is increased by the 
     *  time value of the time object. Quantization is performed on the result. 
     *  The result time value can not be NaN.
     *  @param time The time object contains the amount of time increment.
     *  @return A new time object with the quantized and incremented time value.
     */
    public Time add(Time time) {
        // NOTE: a time value of a time object can be either positive infinite
        // or negative infinite.
        if (time.isNegativeInfinite()) {
            // the time object has a negative infinity time value
            if (isPositiveInfinite()) {
                throw new InternalErrorException(
                        "Adding a positive infinity to a negative " +
                        "infinity yields a NaN.");
            } else {
                return NEGATIVEINFINITY;
            }
        } else if (time.isPositiveInfinite()){
            // the time object has a positive infinity time value
            if (isNegativeInfinite()) {
                throw new InternalErrorException(
                        "Adding a negative infinity to a positive " +
                        "infinity yields a NaN.");
            } else {
                return POSITIVEINFINITY;
            }
        } else {
            return add(time.getBigDecimalValue());
        }
    }

    /** Return -1, 0, or 1 if this time object is less than, equal to, or 
     *  greater than the given argument object. Note that a ClassCastException 
     *  will be thrown if the argument is not an instance of Time.     
     *  @param time A time object to compare to.
     *  @return an integer as -1, 0, or 1.
     */
    public int compareTo(Object time) {
        // NOTE: a time object may contain infinite time values, which can 
        // not be quantized. 
        Time castedTime = (Time)time;
        // If at least one of the time objects has an infinite time value,
        if (castedTime.isInfinite() || isInfinite()) {
            if (castedTime.isNegativeInfinite()) {
                // the castedTime object is a negative infinity.
                if (isNegativeInfinite()) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (castedTime.isPositiveInfinite()) {
                // the castedTime object is a positive infinity.
                if (isPositiveInfinite()) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                // the castedTime object is not infinite, this object must
                // be infinite.
                if (isNegativeInfinite()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        } else {
            return _timeValue.compareTo(castedTime.getBigDecimalValue());
        }
    }

    /** Return true if this time object has the same time value as
     *  that of the given time object. 
     *  @param time The time object that this time object is compared to.
     *  @return True if the two time objects have the same time value.
     */    
    public boolean equals(Object time) {
        return (this.compareTo(time) == 0);    
    }

    /** Return the time value of this time object as a BigDecimal.
     *  The returned value is not quantized. This method can not be applied
     *  on time objects that have infinite time values. 
     *  @return The BigDecimal time value.
     */
    public BigDecimal getBigDecimalValue() {
        if (isInfinite()) {
            throw new InternalErrorException("The getBigDecimalValue method " +
                    "can not be applied on time objects with infinite " +
                    "time values.");
        } else {
            return _timeValue;
        }
    }

    /** Return the double representation of the time value of this time object.
     *  Note limitations may apply, see BigDecimal.doubleValue(). 
     *  In particular, due to the fixed and limited number of bits of
     *  double representation in Java, the returned double value may not 
     *  have the specified time resoution if it is too big.
     *  @return The double representation of the time value.
     *  @deprecated As Ptolemy II 4.1, use {@link #getBigDecimalValue()} 
     *  instead.
     */
    public double getDoubleValue() {
        if (isPositiveInfinite()){
            return Double.POSITIVE_INFINITY;
        } else if (isNegativeInfinite()) {
            return Double.NEGATIVE_INFINITY;
        } else {
            // NOTE: A simple computation may help to warn users that
            // the returned double value loses the specified precisoin.
            // One example: if timeScale = 12, and time resolution is 1E-12,
            // any double that is bigger than 8192.0 can distinguish from itself
            // from a value slighter bigger (with the difference as time 
            // resolution). 8192 is the LUB of the set of double values have
            // the time resolution.
            // NOTE: The strategy to find the LUB for a given time resolution r:
            // find the smallest N such that time resolution r >=  2^(-1*N);
            // get M = 52 - N, which is the multiplication we can apply on the
            // significand without loss of time resolution;
            // the LUB is approximately (1+1)*2^M. 
            // NOTE: the formula to calculate a decimal value from a binary 
            // representation is (-1)^(sign)x(1+significand)x2^(exponent-127). 
            return getBigDecimalValue().doubleValue();
        }
    }

    /** Return the hash code for the time object. If two time objects contains
     *  the same quantized time value, they have the same hash code. 
     *  Note that the quantization is performed on the time value before 
     *  calculating the hash code. 
     *  @return The hash code for the time object.
     */
    public int hashCode() {
        if (isNegativeInfinite()) {
            return Integer.MIN_VALUE;
        } else if (isPositiveInfinite()){
            return Integer.MAX_VALUE;
        } else {
            return getBigDecimalValue().hashCode();
        }
    }
    
    /** Return true if the current time value is infinite.
     *  @return true if the current time value is infinite.
     */ 
    public boolean isInfinite() {
        return isNegativeInfinite() || isPositiveInfinite();
    }
    
    /** Return true if the current time value is a negative infinity.
     *  @return true if the current time value is a negative infinity.
     */ 
    public boolean isNegativeInfinite() {
        return _isNegativeInfinite;
    }
    
    /** Return true if the current time value is a positive infinity.
     *  @return true if the current time value is a positive infinity.
     */ 
    public boolean isPositiveInfinite() {
        return _isPositiveInfinite;
    }
    
    /** Return a new time object whose time value is decreased by the 
     *  time value of the given BigDecimal. Quantization 
     *  is performed on both the timeValue argument and the result.  
     *  @param timeValue The amount of time decrement.
     *  @return A new time object with time value decremented.
     */
    public Time subtract(BigDecimal timeValue) {
        return add(timeValue.negate());
    }

    /** Return a new time object whose time value is decreased by the 
     *  given double value. Quantization is performed on both the 
     *  timeValue argument and the result. 
     *  @param timeValue The amount of time decrement.
     *  @return A new time object with time value decremented.
     */
    public Time subtract(double timeValue) {
        return add(-1*timeValue);
    }

    /** Return a new time object whose time value is decreased by the 
     *  time value of the time object. Quantization 
     *  is performed on the result. 
     *  @param time The time object contains the amount of time decrement.
     *  @return A new time object with time value decremented.
     */
    public Time subtract(Time time) {
        // NOTE: a time value of a time object can be either a 
        // positive infinity or a negative infinity.
        if (time.isNegativeInfinite()) {
            // the time object has a negative infinity time value
            if (isNegativeInfinite()) {
                throw new InternalErrorException(
                        "Subtracting a negative infinity from a negative " +
                        "infinity yields a NaN.");
            } else {
                return POSITIVEINFINITY;
            }
        } else if (time.isPositiveInfinite()){
            // the time object has a positive infinity time value
            if (isPositiveInfinite()) {
                throw new InternalErrorException(
                        "Subtracting a positive infinity from a positive " +
                        "infinity yields a NaN.");
            } else {
                return NEGATIVEINFINITY;
            }
        } else {
            return add(time.getBigDecimalValue().negate());
        }
    }

    /** Return the string representation of this time object.
     *  Note that the string representation of infinities can not be
     *  used to construct the time objects containing infinite time values.
     *  @return A String represention of this time object.
     */
    public String toString() {
        if (isPositiveInfinite()) {
            return "A positive infinity.";
        } else if (isNegativeInfinite()) {
            return "A negative infinity.";
        } else {
            return getBigDecimalValue().toString();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Get the scale of the container director. 
    // This method is only called when quantization happens.
    private int _getTimeScale() {
        if (_director == null) {
            throw new InternalErrorException("Director is null. Cannot " +
                "quantize the time value of a Time object without having " +
                "a director providing a time scale value (a time resolution).");
        } else {
            return _director.getTimeScale();
        }
    }

    // Return the quantized time value of the original time value. 
    // This method can not be applied on time objects with infinite 
    // time values. 
    private BigDecimal _quantizeTimeValue(BigDecimal originalTimeValue) {
        if (isInfinite()) {
            // This should not happen. Otherwise, there is a bug of using
            // this method.
            throw new InternalErrorException("Quantization can not " +
                    "be performed on time objects with infinite time " +
                    "values.");
        } else {
            return originalTimeValue.setScale(
                _getTimeScale(), BigDecimal.ROUND_HALF_EVEN);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // The director that this time object is associated with.
    private Director _director;
    // The time value, which is quantized.
    private BigDecimal _timeValue;
    // A boolean variable is true if the time value is a positive infinity.
    // By default, it is false.
    private boolean _isPositiveInfinite = false;
    // A boolean variable is true if the time value is a negative infinity.
    // By default, it is false.
    private boolean _isNegativeInfinite = false;
}
