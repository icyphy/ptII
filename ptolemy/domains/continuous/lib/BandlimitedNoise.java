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
package ptolemy.domains.continuous.lib;

import ptolemy.actor.Director;
import ptolemy.actor.lib.Gaussian;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BandlimitedNoise

/**
This actor generates continuous-time noise with a Gaussian distribution
and controlled bandwidth. The power spectrum of the noise produced
is given by
<pre>
  S(f) = T s^2 sinc^4(pi f T)
</pre>
where f is frequency, s is the standard deviation,
and the sinc function is given by
<pre>
  sinc(a) = sin(a)/a
</pre>
Here, T = 1/b, where b is the value of the <i>bandwidth</i> parameter.
Notice that the power declines as the fourth power
of one over the frequency. The <i>bandwidth</i> parameter specifies
the frequency (in Hertz) at which the first zero occurs, or,
equivalently, roughly the width of the main lobe.
<p>
This actor may affect the step size taken by the solver. Specifically,
it ensures that the solver provides executions at least as frequently
as twice the specified bandwidth. This is nominally the Nyquist frequency
of an ideally bandlimited noise frequency, but since this noise process
is not ideally bandlimited, the solver samples will typically have
aliasing distortion. If you need to control that aliasing distortion,
then you can set the <i>maxStepSize</i> parameter to something smaller
than 1/2b, where b is the <i>bandwidth</i>.
<p>
For some uses, the effect that this actor has on the step size may
be undesirable because it increases the cost of simulation.
If a less rigorous form of noise is desired (for rough models or
simple demonstrations), you can use the {@link Noise} actor.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class BandlimitedNoise extends Gaussian {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BandlimitedNoise(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        bandwidth = new Parameter(this, "bandwidth");
        bandwidth.setTypeEquals(BaseType.DOUBLE);
        bandwidth.setExpression("10.0");

        // Move the mean port to first and hide the trigger port.
        // It makes no sense to trigger this actor.
        mean.getPort().moveToFirst();
        new Parameter(trigger, "_hide", BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The bandwidth of the noise random process in Hertz.
     *  The bandwidth is the frequency where the power spectral
     *  density first hits zero. This is a double that defaults to
     *  10.0 Hertz.
     */
    public Parameter bandwidth;

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
        // NOTE: The superclass fire() doesn't do what we want,
        // so we have to replicate the parts of its superclass
        // that we do want, which isn't much.
        if (_debugging) {
            _debug("Called fire()");
        }

        standardDeviation.update();
        mean.update();

        if (_needNewGenerator) {
            _createGenerator();
        }
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        if (_timeOfValueAtStart != null) {
            // Not the first firing after initialize.
            // Use the quantized version of the interval.
            // Note that this will never be zero.
            double timeGapBetweenValues = _timeOfValueAtEnd.subtract(
                    _timeOfValueAtStart).getDoubleValue();
            // The time interval, however, may be zero.
            double interval = currentTime.subtract(_timeOfValueAtStart)
                    .getDoubleValue();
            // FIXME: Linear iterpolation is not a good choice here.
            // Should be doing filtering.
            _current = _valueAtStart + (_valueAtEnd - _valueAtStart) * interval
                    / timeGapBetweenValues;
        }
        output.send(0, new DoubleToken(_current));
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
        _valueAtEnd = _current;
        _timeOfValueAtEnd = getDirector().getModelTime();
        _timeOfValueAtStart = null;

        // Generate the second random number.
        _generateRandomNumber();
        getDirector().fireAt(this, _timeOfValueAtEnd);
    }

    /** If we are at the end of the current interval, then generate
     *  a new random number for the new interval, and request a
     *  refiring at the end of that interval.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is OK to continue.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();

        Director director = getDirector();
        Time currentTime = director.getModelTime();

        // On the first firing at the time matching the _timeValueOfEnd,
        // generate a new random number for the end of the current interval.
        // FIXME: NO!  This might be a speculative execution at that time!!!
        if (currentTime.equals(_timeOfValueAtEnd)) {
            // Generate a random number for the _end_ of the current
            // integration period. This will update _timeOfValueAtEnd.
            _generateRandomNumber();
            director.fireAt(this, _timeOfValueAtEnd);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate a new random number.
     *  @return A random number.
     *  @exception IllegalActionException If parameter values are incorrect.
     */
    protected double _generateGaussian() throws IllegalActionException {
        double standardDeviationValue = ((DoubleToken) standardDeviation
                .getToken()).doubleValue();
        double rawNum = _random.nextGaussian();
        return rawNum * standardDeviationValue;
    }

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
        _valueAtStart = _valueAtEnd;
        _timeOfValueAtStart = _timeOfValueAtEnd;
        super._generateRandomNumber();
        _valueAtEnd = _current;

        double period = 1.0 / ((DoubleToken) bandwidth.getToken())
                .doubleValue();
        _timeOfValueAtEnd = _timeOfValueAtStart.add(period);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Time associated with the second random number of the current interval. */
    private Time _timeOfValueAtEnd;

    /** Time associated with the first random number of the current interval. */
    private Time _timeOfValueAtStart;

    /** The random number at the start of the current interval. */
    private double _valueAtStart;

    /** The random number at the end of the end of the current interval. */
    private double _valueAtEnd;
}
