/* The integrator in the continuous domain.

 Copyright (c) 1998-2010 The Regents of the University of California.
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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Noise

/**
This actor generates continuous-time noise with a Gaussian distribution.
It provides two rather ad-hoc approximations to a white noise process, depending
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
<p>
Note that a much more principled noise process is generated
by the {@link BandlimitedNoise} actor.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class Noise extends ptolemy.domains.continuous.kernel.Noise {

    // NOTE: This is simply a wrapper for continuous.kernel.Noise to make
    // it appear in the lib package.

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
    }
}
