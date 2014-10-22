/* An interpolator for a specified array of times and values.

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
package ptolemy.domains.continuous.lib;

import ptolemy.actor.lib.DiscreteClock;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.DoubleMatrixMath;

///////////////////////////////////////////////////////////////////
//// Waveform

/**
 This actor produces a periodic continuous-time signal defined by a
 periodic sequence of samples and an interpolation method.
 The <i>interpolation</i> parameter specifies the interpolation method,
 which is either "linear",   which indicates linear interpolation, or "hermite",
 which indicates  third order interpolation based on the Hermite curves in chapter
 11 of "Computer Graphic, Principles and Practice", by Foley, van Dam, Feiner
 and Hughes, 2nd ed. in C, 1996. Interpolation is calculated assuming that
 the waveform is periodic.
 <p>
 At the beginning of each time interval of length given by <i>period</i>,
 starting from the time at which initialize() is invoked,
 this actor initiates a continuous output that passes through the values given by
 <i>values</i> at the time offsets into the period given by <i>offsets</i>.
 These parameters contain arrays, which are required to have the same length.
 The <i>offsets</i> array contains doubles, which
 must be nondecreasing and nonnegative,
 or an exception will be thrown when it is set.
 <p>
 You can provide a finite <i>stopTime</i>. Upon reaching that stop time,
 postfire() returns false, which requests that the director
 not fire this actor again. The output will be absent after that time.
 The clock can also be started and stopped repeatedly
 during an execution. A token at the <i>start</i> input will start the clock
 at the beginning of a period. A token
 at the <i>stop</i> input will stop the clock, if it is still running.
 If both <i>start</i> and <i>stop</i> are received simultaneously, then
 the clock will be stopped. When the clock is stopped, the output is absent.
 <p>
 The <i>values</i> parameter by default
 contains the array {1.0, -1.0}.  The default
 <i>offsets</i> array is {0.0, 1.0}.
 The default period is 2.0. This results in a triangle
 wave (for linear interpolation) and a smooth sinusoid-like
 waveform (for hermite interpolation).
 <p>
 The type of the output is double.
 <p>
 If two offsets are equal, or if one is equal to the period,
 then two events will be produced at the same time, but with
 different microsteps. This will cause strange effects with
 hermite interpolation, and hence is not recommended. But
 it can sometimes be useful with linear interpolation to get
 discontinuous outputs.
 <p>
 If the <i>period</i> is changed at any time, either by
 provided by an input or by changing the parameter, then the
 new period will take effect immediately if the new period
 is provided at the same time (including the
 microstep) that the current cycle starts,
 or after the current cycle completes otherwise.

 @author Sarah Packman, Yuhong Xiong, Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (yuhong)
 @see ptolemy.math.Interpolation
 */
public class Waveform extends DiscreteClock {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Waveform(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        interpolation = new StringParameter(this, "interpolation");
        interpolation.setExpression("linear");
        interpolation.addChoice("linear");
        interpolation.addChoice("hermite");
        attributeChanged(interpolation);

        // Constrain the values to be doubles and change defaults.
        values.setExpression("{1.0, -1.0}");
        values.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        period.setExpression("2.0");

        offsets.setExpression("{0.0, 1.0}");

        // Trigger is not supported.
        Parameter hide = new SingletonParameter(trigger, "_hide");
        hide.setExpression("true");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-20\" " + "width=\"40\" height=\"40\" "
                + "style=\"fill:lightGrey\"/>\n"
                + "<line x1=\"-20\" y1=\"-15\" x2=\"-10\" y2=\"15\"/>\n"
                + "<line x1=\"-10\" y1=\"15\" x2=\"0\" y2=\"-15\"/>\n"
                + "<line x1=\"0\" y1=\"-15\" x2=\"10\" y2=\"15\"/>\n"
                + "<line x1=\"10\" y1=\"15\" x2=\"20\" y2=\"-15\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The interpolation method, which must be "linear" or "hermite". */
    public StringParameter interpolation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check the validity of the parameter.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the argument is the
     *   <i>values</i> parameter and it does not contain an one dimensional
     *   array; or the argument is the <i>times</i> parameter and it does
     *   not contain an one dimensional array or is not increasing and
     *   non-negative; or the argument is the <i>period</i> parameter and is
     *   negative; or the argument is the <i>order</i> parameter and the order
     *   is not supported by the Interpolation class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == interpolation) {
            _interpolation = _LINEAR;
            String interpolationValue = interpolation.stringValue();
            if (interpolationValue.equals("hermite")) {
                _interpolation = _HERMITE;
            }
        } else if (attribute == period) {
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();
            if (periodValue == Double.POSITIVE_INFINITY) {
                throw new IllegalActionException(this,
                        "Period is required to be finite.  " + "Period given: "
                                + periodValue);
            }
            super.attributeChanged(attribute);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to set the output microstep to zero.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it, or if the director does not
     *   agree to fire the actor at the specified time.
     */
    @Override
    public synchronized void initialize() throws IllegalActionException {
        super.initialize();
        _nextOutputIndex = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the Hermite curve interpolation.
     *  @param index The interpolation point index.
     *  @param startTime The time of the starting reference point.
     *  @param startValue The value of the starting reference point.
     *  @param tanStart The tangent of the starting reference point.
     *  @param endTime The time of the ending reference point.
     *  @param endValue The value of the ending reference point.
     *  @param tanEnd The tangent of the ending reference point.
     *  @return The Hermite curve interpolation.
     */
    protected double _hermite(double index, double startTime,
            double startValue, double tanStart, double endTime,
            double endValue, double tanEnd) {
        // forming the Hermite matrix M
        double[][] M = new double[4][4];
        double iStartSqr = startTime * startTime;
        double iEndSqr = endTime * endTime;
        M[0][0] = iStartSqr * startTime;
        M[0][1] = iStartSqr;
        M[0][2] = startTime;
        M[0][3] = 1;

        M[1][0] = iEndSqr * endTime;
        M[1][1] = iEndSqr;
        M[1][2] = endTime;
        M[1][3] = 1;

        M[2][0] = 3 * iStartSqr;
        M[2][1] = 2 * startTime;
        M[2][2] = 1;
        M[2][3] = 0;

        M[3][0] = 3 * iEndSqr;
        M[3][1] = 2 * endTime;
        M[3][2] = 1;
        M[3][3] = 0;

        double[][] MInverse = DoubleMatrixMath.inverse(M);

        // forming the column vector of values and tangents
        double[] Gh = new double[4];
        Gh[0] = startValue;
        Gh[1] = endValue;
        Gh[2] = tanStart;
        Gh[3] = tanEnd;

        // compute the coefficients vector coef[a, b, c, d] or the 3rd order
        // curve.
        double[] coef = DoubleMatrixMath.multiply(Gh, MInverse);

        // compute the interpolated value
        double indexSqr = index * index;
        return coef[0] * indexSqr * index + coef[1] * indexSqr + coef[2]
                * index + coef[3];
    }

    /** Return the interpolation result for the current model time.
     *  @return A double.
     *  @exception IllegalActionException If the values parameter is malformed.
     *  @exception IllegalStateException If the index and value arrays
     *   do not have the same length, or the period is not 0 and not
     *   greater than the largest index.
     */
    private Token _interpolate() throws IllegalActionException {
        int numRefPoints = _offsets.length;

        // index is now within [0, period-1]. If it is outside the range of
        // the smallest and the largest index, values must be periodic.
        // Handle a special case where the number of reference points is
        // 1. The code for order 3 later won't work for this case.
        if (numRefPoints == 1) {
            return _getValue(0);
        }

        // Time into the current cycle.
        Time currentTime = getDirector().getModelTime();
        double time = currentTime.subtract(_cycleStartTime).getDoubleValue();

        // indexIndexStart is the index to _offsets whose entry is the
        // index to the left of the interpolation point.
        int indexIndexStart = _phase - 1;

        double periodValue = ((DoubleToken) period.getToken()).doubleValue();

        // Need at least the two points surrounding
        // the interpolation point.
        double startTime;
        double endTime;
        double startValue;
        double endValue;

        if (indexIndexStart == -1) {
            startTime = _offsets[numRefPoints - 1] - periodValue;
            startValue = ((DoubleToken) _getValue(numRefPoints - 1))
                    .doubleValue();
        } else {
            startTime = _offsets[indexIndexStart];
            startValue = ((DoubleToken) _getValue(indexIndexStart))
                    .doubleValue();
        }

        if (indexIndexStart == numRefPoints - 1) {
            endTime = _offsets[0] + periodValue;
            endValue = ((DoubleToken) _getValue(0)).doubleValue();
        } else {
            endTime = _offsets[indexIndexStart + 1];
            endValue = ((DoubleToken) _getValue(indexIndexStart + 1))
                    .doubleValue();
        }

        if (_interpolation == _LINEAR) {
            return new DoubleToken(startValue + (time - startTime)
                    * (endValue - startValue) / (endTime - startTime));
        }

        // order is 3. Need the points before Start and the point after End
        // to compute the tangent at Start and End.
        double timeBeforeStart;
        double timeAfterEnd;
        double valueBeforeStart;
        double valueAfterEnd;

        if (indexIndexStart == -1) {
            timeBeforeStart = _offsets[numRefPoints - 2] - periodValue;
            valueBeforeStart = ((DoubleToken) _getValue(numRefPoints - 2))
                    .doubleValue();
        } else if (indexIndexStart == 0) {
            if (periodValue > 0) {
                timeBeforeStart = _offsets[numRefPoints - 1] - periodValue;
                valueBeforeStart = ((DoubleToken) _getValue(numRefPoints - 1))
                        .doubleValue();
            } else {
                // Not periodic
                timeBeforeStart = _offsets[0] - 1;
                valueBeforeStart = 0.0;
            }
        } else {
            timeBeforeStart = _offsets[indexIndexStart - 1];
            valueBeforeStart = ((DoubleToken) _getValue(indexIndexStart - 1))
                    .doubleValue();
        }

        if (indexIndexStart == numRefPoints - 1) {
            timeAfterEnd = _offsets[1] + periodValue;
            valueAfterEnd = ((DoubleToken) _getValue(1)).doubleValue();
        } else if (indexIndexStart == numRefPoints - 2) {
            if (periodValue > 0 && periodValue != Double.POSITIVE_INFINITY) {
                timeAfterEnd = _offsets[0] + periodValue;
                valueAfterEnd = ((DoubleToken) _getValue(0)).doubleValue();
            } else {
                // Not periodic
                timeAfterEnd = _offsets[numRefPoints - 1] + 1;
                // FIXME: Assuming 0.0 doesn't seem right.
                valueAfterEnd = 0.0;
            }
        } else {
            timeAfterEnd = _offsets[indexIndexStart + 2];
            valueAfterEnd = ((DoubleToken) _getValue(indexIndexStart + 2))
                    .doubleValue();
        }

        // Compute the tangent at Start and End.
        double tanBefore2Start = (startValue - valueBeforeStart)
                / (startTime - timeBeforeStart);
        double tanStart2End = (endValue - startValue) / (endTime - startTime);
        double tanEnd2After = (valueAfterEnd - endValue)
                / (timeAfterEnd - endTime);

        double tanStart = 0.5 * (tanBefore2Start + tanStart2End);
        double tanEnd = 0.5 * (tanStart2End + tanEnd2After);

        return new DoubleToken(_hermite(time, startTime, startValue, tanStart,
                endTime, endValue, tanEnd));
    }

    /** Produce the output required at times between the specified times
     *  using the specified interpolation method.
     *  @exception IllegalActionException If sending the output fails.
     */
    @Override
    protected void _produceIntermediateOutput() throws IllegalActionException {
        if (!_enabled) {
            output.sendClear(0);
            return;
        }
        if (_debugging) {
            _debug("Interpolating output.");
        }
        output.send(0, _interpolate());
    }

    /** Skip the current firing phase and request a refiring at the
     *  time of the next one.
     *  @exception IllegalActionException If the period cannot be evaluated.
     */
    @Override
    protected void _skipToNextPhase() throws IllegalActionException {
        _phase++;
        if (_phase >= _offsets.length) {
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();
            _phase = 0;
            _cycleStartTime = _cycleStartTime.add(periodValue);
        }
        Time nextOutputTime = _cycleStartTime.add(_offsets[_phase]);
        if (_nextOutputTime.equals(nextOutputTime)) {
            _nextOutputIndex++;
        } else {
            _nextOutputTime = nextOutputTime;
            _nextOutputIndex = 0;
        }
        _fireAt(_nextOutputTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator for third order interpolation. */
    private static final int _HERMITE = 1;

    /** The value of the interpolation parameter. */
    private int _interpolation = _LINEAR;

    /** Indicator for linear interpolation. */
    private static final int _LINEAR = 0;
}
