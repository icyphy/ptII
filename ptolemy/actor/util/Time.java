/* A class that contains a double precision number as time.

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

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.data.TokenUtilities;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.Utilities;


//////////////////////////////////////////////////////////////////////////
//// Time
/**
   A Time class represents model time in a model. It is different from
   the real time, which is time in hardware. It contains a double value that 
   represents the time value. 
   
   <p> The time value has a precision specified by the time resolution of 
   the director that executes the model. Therefore, a time object needs to be
   constructed during the initialization phase. Another reason is that 
   when the time resolution changes, the precision of time objects also needs
   updated.  
   
   <p> This class supports two operations only, add and subtract, where the 
   operand can be either a double or a Time object. This class also implements
   the Comparable interface, where two time objects can compare with each other.

   @author Haiyang Zheng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class Time implements Comparable {
    
    /** Construct a Time object with 0.0 as the time value in
     *  the given actor. 
     *  @param container The atomic actor that contains this time object.
     */
    public Time(Actor container) {
        // FIXME: cannot use AtomicActor because the TimeKeeper for DDE
        // does not specify the container of time as an AtomicActor 
        Director director = container.getDirector();
        _init(director);
    }

    /** Construct a Time object with the specified value as its
     *  time value in the given actor. 
     *  @param container The atomic actor that contains this time object.
     *  @param value The time value.
     */
    public Time(Actor container, double value) {
        Director director = container.getDirector();
        _init(director);
        _setTimeValue(value);
    }

    /** Construct a Time object with 0.0 as the time value in
     *  the given director. 
     *  @param container The director that contains this time object.
     */
    public Time(Director container) {
        _init(container);
    }

    /** Construct a Time object with the specified value as its
     *  time value in the given director. 
     *  @param container The director that contains this time object.
     *  @param value The time value.
     */
    public Time(Director container, double value) {
        _init(container);
        _setTimeValue(value);
    }

    /** Increase the time by the given double value.
     *  @param timeValue The amount of time increment.
     */
    public Time add(double timeValue) {
        return new Time(_container, _time + timeValue);
    }

    /** Increase the time by the time value specified by the time object.
     *  @param time The time object the specifies the time increment.
     */
    public Time add(Time time) {
        return add(time.getTimeValue());
    }

    /** Return -1, 0, or 1 if this time object is less than, equal to, or 
     *  greater than the given argument object. Note that a ClassCastException 
     *  will be thrown if the argument is not an instance of Time.     
     *  @param time An object.
     *  @return a integer as -1, 0, or 1.
     */
    public int compareTo(Object time) {
        return compareTo((Time) time);
    }

    /** Return -1, 0, or 1 if this time object is less than, equal to, or 
     *  greater than the given time argument. Note that a ClassCastException 
     *  will be thrown if the argument is not an instance of Time.     
     *  @param time An object of Time.
     *  @return a integer as -1, 0, or 1.
     */
    public int compareTo(Time time) {
        double difference = _time - time.getTimeValue();
        if (difference < 0) {
            return -1;
        } else if (difference == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    /** Return true if this time object has the same time value as
     *  that of the given time object. Note that it is different from
     *  the equals method.
     * 
     *  @param time The time object that this time object is compared to.
     *  @return True if the two time objects have the same time value.
     */    
    public boolean equalTo(Time time) {
        // Since comparison for equality is very common,
        // the equalTo method is created. 
        return (this.compareTo(time) == 0);    
    }
    
    /** Return the time value.
     *  @return The time value contained in this class as a double.
     */
    public double getTimeValue() {
        return _time;
    }

    /** Decrease the time by the given double value.
     *  @param timeValue The amount of time decrement.
     */
    public Time subtract(double timeValue) {
        return new Time(_container, _time - timeValue);
    }

    /** Decrease the time by the time value specified by the time object.
     *  @param time The time object the specifies the time decrement.
     */
    public Time subtract(Time time) {
        return subtract(time.getTimeValue());
    }

    /** Return the time value of this class as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  The exact form of the number depends on its value, and may be either
     *  decimal or exponential.  In general, exponential is used for numbers
     *  whose magnitudes are very large or very small, except for zero which
     *  is always represented as 0.0.  The behavior is roughly the same as
     *  Double.toString(), except that we limit the precision to seven
     *  fractional digits.  If you really must have better precision,
     *  then use <code>Double.toString(token.doubleValue())</code>.
     *  @return A String representing the time value of this class.
     */
    public String toString() {
        // FIXME: support unit system
        if (Double.isNaN(_time) || Double.isInfinite(_time)) {
            return Double.toString(_time);
        } else {
            double mag = Math.abs(_time);
            if (mag == 0.0 || (mag < 1000000 && mag > .001)) {
                return TokenUtilities.regularFormat.format(_time);
            } else {
                return TokenUtilities.exponentialFormat.format(_time);
            }
        }
    }

    // Initialize the states. 
    private void _init(Director container) {
        _container = container;
        _time = 0.0;
    }

    // Get the time resolution of the container director. 
    private double _getTimeResolution() {
        if (_container == null) {
            // FIXME: This should not happen if we constrain all time objects
            // to be instantiated duirng initialize method.
            
            throw new InternalErrorException("Director is null. Cannot " +
                "initialize a Time object. An actor should only initialize "  
                + "a Time object in the initialize method.");
            // return the default value for time resolution if there
            // is no director.
            // return 1e-10;
        } else {
            return _container.getTimeResolution();
        }
    }

    // Set the time value.
    private void _setTimeValue(double newTime) {
        _time = Utilities.round(newTime, _getTimeResolution());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The director that contains this time object.
    private Director _container;
    // The time value.
    private double _time;
}