/* An actor that outputs a random sequence with a Gaussian distribution.

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
package ptolemy.domains.continuous.kernel;

import ptolemy.actor.Director;
import ptolemy.actor.lib.Gaussian;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Noise

/**
This actor generates continuous-time noise with a Gaussian distribution.
It provides two approximations to a white noise process, depending
on the value of the <i>linearlyInterpolate</i> parameter. Specifically,
if this parameter is true (the default), then the output signal is
a continuous signal that linearly interpolates between
Gaussian random numbers generated at each sample time chosen by
the solver. If the solver step size is constant or increasing, then these Gaussian
random numbers will be independent. However, if the solver finds itself having
to reduce the step size after performing a speculative execution into the
future, then the random number produced at the end of the reduced
integration step will be correlated with that produced at the beginning
of the integration step. (FIXME: Need a figure to illustrate this.)
<p>
If <i>linearlyInterpolate</i> is set to false, then this actor will
hold the value of its output constant for the duration of an integration
step. Thus, the output signal is piecewise constant. At each time step
chosen by the solver, the value is given by a new independent Gaussian
random number.  This method has the advantage that samples at all
times chosen by the solver are uncorrelated.
<p>
In both cases, whether <i>linearlyInterpolate</i> is true or false, if the
solver holds its step size constant, then the resulting signal is
statistically equivalent to filtered white noise. If <i>linearlyInterpolate</i>
is true, then the power spectrum has the shape of a sinc squared.
If it is false, then it has the shape of the absolute value of a sinc
function. In the latter case, the power is infinite, so the approximation
is not physically realizable. In the former case, the power is finite.
In both cases, sampling the process at the rate of one over the step
size yields a discrete-time white noise process.
<p>
It is worth explaining why we must approximate white noise.
In general, it is not possible in any discretized approximation of a continuous
random process to exactly simulate a white noise process. By definition, a
white noise process is one where any two values at distinct times are
uncorrelated. A naive attempt to simulate this might simply generate
a new random number at each sample time at which the solver chooses
to fire the actor. However, this cannot work in general.
Specifically, the semantics of the continuous domain assumes
that signals are piecewise continuous. The signal resulting from
the above strategy will not be piecewise continuous. If the solver
refines a step size and chooses a point close to a previously calculated
point, the new value produced by such an actor would not be close to the
previously value previously produced. This can result in the solver
assuming that its step size is too large and reducing it until it can
reduce it no more.
<p>
To demonstrate this effect, try connecting a GaussianActor to a
LevelCrossingDetector actor under a ContinuousDirector. An execution
of the model will immediately trigger an exception with a message
like "The refined step size is less than the time resolution, at time..."
Conceptually, with a true white noise process, the level crossing
occurs <i>at all times</i>, and therefore the exception is,
in fact, the correct response.
<p>
If you modify the above example by sending the output of the
Gaussian actor directly to an Integrator, and then the output
of the Integrator to the LevelCrossingDetector, then the exception
disappears. The Integrator ensures that the signal is piecewise
continuous. This might seem like a reasonable approximation to a
Weiner process, but in fact it is problematic. In particular,
at the times that the LevelCrossingDetector triggers, the
Gaussian actor will actually produce two distinct random numbers
at the same time (at different microsteps). This changes the
statistics of the output in a very subtle way.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class Noise extends Gaussian {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Noise(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        linearlyInterpolate = new Parameter(this, "linearlyInterpolate");
        linearlyInterpolate.setTypeEquals(BaseType.BOOLEAN);
        linearlyInterpolate.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If true, linearly between random number for multistep solvers,
     *  and otherwise, perform zero-order hold. This is a boolean that
     *  defaults to true.
     */
    public Parameter linearlyInterpolate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce a number that is linearly interpolated within the
     *  current integration step, if <i>linearlyInterpolate</i> is true, or
     *  the random number for the beginning of the integration
     *  step otherwise.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_needNewGenerator) {
            _createGenerator();
        }
        if (_needNew) {
            // Generate a random number for the _end_ of the current
            // integration period.
            _generateRandomNumber();
            _needNew = false;
        }
        // Get required information from the director.
        Director director = getDirector();
        double stepSize = ((ContinuousDirector) director)._getCurrentStepSize();
        boolean interpolating = ((BooleanToken) linearlyInterpolate.getToken())
                .booleanValue();
        if (!interpolating) {
            // Doing zero-order hold.
            // Output value should be the value at the beginning
            // of the integration period.
            _current = _valueAtStart;
        } else if (stepSize == 0.0) {
            // Interpolating and step size is zero.
            // If the step size is zero then any newly
            // generated random number is ignored.
            _current = _valueAtStart;
        } else {
            // Interpolating and step size is greater than zero.
            Time iterationBeginTime = ((ContinuousDirector) director)._iterationBeginTime;
            Time currentTime = ((ContinuousDirector) director).getModelTime();
            double interval = currentTime.subtract(iterationBeginTime)
                    .getDoubleValue();
            if (interval == 0.0) {
                _current = _valueAtStart;
            } else {
                double timeGapBetweenValues = _timeOfValueAtEnd.subtract(
                        iterationBeginTime).getDoubleValue();
                _current = _valueAtStart + (_valueAtEnd - _valueAtStart)
                        * interval / timeGapBetweenValues;
            }
        }
        // The superclass produces on the output the _current value.
        // Since above we set _needNew to false if it was true,
        // the superclass will never produce a new random number.
        super.fire();
    }

    /** Initialize the random number generator with the seed, if it
     *  has been given.  A seed of zero is interpreted to mean that no
     *  seed is specified.  In such cases, a seed based on the current
     *  time and this instance of a RandomSource is used to be fairly
     *  sure that two identical sequences will not be returned.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Generate the first random number.
        super._generateRandomNumber();
        // The first step size will be zero, so the value at
        // the start and end of the integration period will be
        // the same, namely the random number generated above.
        _valueAtStart = _current;
        _valueAtEnd = _current;
        _timeOfValueAtEnd = getDirector().getModelTime();
        // Suppress generation of the first random number in the
        // next invocation of fire().
        _needNew = false;
    }

    /** Set a flag to cause a new random number to be generated the next
     *  time fire() is called.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is OK to continue.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // The next line will set _needNew = true.
        boolean result = super.postfire();

        Director director = getDirector();
        // The cast is safe because it was checked in fire().
        double stepSize = ((ContinuousDirector) director)._getCurrentStepSize();

        // If we are not interpolating, then request a refiring at the current time
        // so that the new value is then produced at the current time.
        boolean interpolating = ((BooleanToken) linearlyInterpolate.getToken())
                .booleanValue();
        if (!interpolating) {
            // Request a refiring at the current time in order to get a zero-order
            // hold effect, but only if the step size is non-zero.
            // If the step size is zero, the new random number will be
            // the same as the old so there is no need to refire.
            if (stepSize != 0.0) {
                // Request a refiring at the current time in order to get
                // the zero-order hold effect.
                director.fireAtCurrentTime(this);
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate a new random number. This gets called in initialize()
     *  and in the first fire() method of an iteration. It produces a number
     *  that is to be the random number at the <i>end</i> of the current
     *  iteration.
     *  @exception IllegalActionException If parameter values are incorrect.
     */
    @Override
    protected void _generateRandomNumber() throws IllegalActionException {
        Director director = getDirector();
        if (!(director instanceof ContinuousDirector)) {
            throw new IllegalActionException(this, director,
                    "WhiteNoise actor is designed to work with ContinuousDirector,"
                            + " but the director is " + director.getClass());
        }
        double stepSize = ((ContinuousDirector) director)._getCurrentStepSize();
        boolean interpolating = ((BooleanToken) linearlyInterpolate.getToken())
                .booleanValue();
        if (!interpolating) {
            // Doing zero-order hold.
            _valueAtStart = _valueAtEnd;
            _timeOfValueAtEnd = director.getModelTime();
            // Generate a new random number only if the step size is
            // non-zero.
            if (stepSize != 0.0) {
                super._generateRandomNumber();
                _valueAtEnd = _current;
            }
        } else {
            // Interpolating.
            // The value at the start of the new period should be equal
            // to the interpolated value at the current time.
            // Presumably, this is the most recently produced output.
            _valueAtStart = _current;
            _timeOfValueAtEnd = director.getModelTime().add(stepSize);
            // If the step size is non-zero, generate a new random number
            // for the end of the period. Otherwise, use the current value.
            if (stepSize != 0.0) {
                super._generateRandomNumber();
                _valueAtEnd = _current;
            } else {
                _valueAtEnd = _valueAtStart;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Time assigned to a new random number. This will be the integration end
     *  time for the first firing with a non-zero step size after the random
     *  number is generated.
     */
    private Time _timeOfValueAtEnd;

    /** The random number at the start of the current integration period. */
    private double _valueAtStart;

    /** The random number at the end of the current integration period. */
    private double _valueAtEnd;
}
