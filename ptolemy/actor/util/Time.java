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
import ptolemy.actor.Executable;
import ptolemy.data.TokenUtilities;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Utilities;


//////////////////////////////////////////////////////////////////////////
//// Time
/**
   A Time class represents time in a model. It contains two double values, 
   one is the time value and the other one is the time resolution.  The 
   time resolution has a default value 1e-10 and can be configured through
   directors. The time value has a precision specified by the time resolution.
   
   <p> This class supports two operations only, add and subtract, where the 
   operand can be either a double or a Time object. This class also implements
   the Comparable interface, where two time objects can compare with each other. 

   @author Haiyang Zheng
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class Time implements Comparable {

    /** Construct a Time object with 0.0 as the time value in
     *  the given container. 
     *  @param container The container of this time object.
     *  @exception IllegalActionException If the container is 
     *  neither a director nor an actor.
     */
    public Time(Executable container) throws IllegalActionException {
        _init(container);
    }

    /** Construct a Time object with the specified value as its
     *  time value in the given container. 
     *  @param container The container of this time object.
     *  @param value The time value.
     *  @exception IllegalActionException If the container is 
     *  neither a director nor an actor.
     */
    public Time(Executable container, double value) 
        throws IllegalActionException {
        _init(container);
        setTimeValue(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Increase the time by the given double value.
     *  @param timeValue The amount of time increment.
     */
    public void add(double timeValue) {
        _time = _time + timeValue;
        _time = Utilities.round(_time, _timeResolution);
    }

    /** Increase the time by the time value specified by the time object.
     *  @param time The time object the specifies the time increment.
     */
    public void add(Time time) {
        add(time.getTimeValue());
    }

    /** Return -1, 0, or 1 if this time object is less than, equal to, or 
     *  greater than the given argument object. Note that a ClassCastException 
     *  will be thrown if the argument is not an instance of Time.     
     *  @param time An object.
     *  @return a integer as -1, 0, or 1.
     */
    public int compareTo(Object time) {
        return compareTo((Time) time);;
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

    /** Return the time value.
     *  @return The time value contained in this class as a double.
     */
    public double getTimeValue() {
        return _time;
    }

    /** Set the time resolution.
     *  @throws IllegalActionException If the timeResolution parameter
     *  is not in scientific notation or the mantissa is not 1. 
     */
    public void setTimeResolution(double timeResolution) 
        throws IllegalActionException {
        // FIXME: check whether the new time resolution is
        // in scientific notation or the mantissa is 1.
        // FIXME: Time resolution may only be changed by a director.
        // How to enforce that? 
        _timeResolution = timeResolution;
    }

    /** Set the time value.
     *  @param newTime The new time value.
     */
    public void setTimeValue(double newTime) {
        _time = Utilities.round(newTime, _timeResolution);
    }

    /** Decrease the time by the given double value.
     *  @param timeValue The amount of time decrement.
     */
    public void subtract(double timeValue) {
        _time = _time - timeValue;
        _time = Utilities.round(_time, _timeResolution);
    }

    /** Decrease the time by the time value specified by the time object.
     *  @param time The time object the specifies the time decrement.
     */
    public void subtract(Time time) {
        subtract(time.getTimeValue());
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Initialize the states. Throw an illegalActionException if the
    // given container argument is neither a director or an actor.
    private void _init(Executable container) throws IllegalActionException {
        if (container instanceof Actor) {
            _timeResolution = 
                ((Actor)container).getDirector().getTimeResolution();
        } else if (container instanceof Director) {
            _timeResolution = 
                ((Director)container).getTimeResolution();
        } else{
            throw new IllegalActionException("Can not create " +
                "a Time object because the container is neither " +
                "a director nor an actor.");
        }
        _container = container;
        _time = Utilities.round(0.0, _timeResolution);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The container of this time object.
    private Executable _container;
    // The time value.
    private double _time;
    // The time resolution.
    private double _timeResolution;
}
