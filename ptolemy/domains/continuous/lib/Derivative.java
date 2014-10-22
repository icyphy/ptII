/* The derivative in the continuous domain.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// Derivative

/**
 A crude approximation to a derivative in the continuous domain.
 In continuous-time modeling, one should generally avoid taking derivatives
 directly. It is better to use an {@link Integrator} actor in a feedback loop.
 The input to the Integrator is the derivative of its output.
 The reason for avoiding taking derivatives directly
 is that small amounts of noise on the input of
 a derivative actor result in large output fluctuations.
 Since continuous-time simulation involves choosing step sizes,
 the choice of step size will strongly affect the resulting
 output of the derivative. Derivatives tend to be very noisy,
 with considerable fluctuations in value. Moreover, if the
 input to this actor has discontinuities, the output will not
 be piecewise continuous, and at the discontinuity, the results
 could be difficult to control. If an Integrator is downstream,
 then the solver will be forced to use its smallest step size.
 <p>
 That said, if you have read this far, you are probably determined
 to compute a derivative. Hence, we provide this actor, which performs
 a simple operation and provides a simple (partial) guarantee. Specifically,
 a correctly connected Derivative followed by an Integrator is (almost)
 an identity function. And an Integrator followed by a Derivative is
 also (almost) an identity function. The reason for the "almost" is
 that very first <i>derivative</i> output of the Derivative actor
 is always zero. Determining a derivative without any past history
 requires seeing the future. Although in principle it might be
 possible for this actor to collaborate with the solver to
 speculatively execute into the future to get the derivative,
 we have not done that here.
 <p>
 Upon firing, this actor produces an output on the <i>derivative</i>
 port, and may also produce an output on the <i>impulse</i> port.
 The <i>derivative</i> output value is the difference between the
 input at the current time and the previous input divided by the
 time between these inputs, unless that time is zero. If the time
 between this input and the previous one is zero, and the value
 of the previous input and the current one is non-zero, then this
 actor will be produce the value difference on the <i>impulse</i>
 output and will produce whatever
 it previously produced on the <i>derivative</i> output.
 <p>
 On the very first firing after being initialized,
 this actor always produces zero
 on the <i>derivative</i> output. If the input is
 non-zero, then it will produce the value of the input on
 the <i>impulse</i> output. This ensures that if the
 <i>impulse</i> output is connected to the <i>impulse</i>
 input of a downstream Integrator, that the Integrator will
 be correctly initialized.
 <p>
 The <i>impulse</i> output should be interpreted as a Dirac
 delta function. It is a discrete output. If it is connected to
 the <i>impulse</i> input of the Integrator actor, and the
 <i>derivative</i> output is connected to the <i>derivative</i>
 input of the Integrator actor, then the cascade of two actors
 will be an identity function for all input signals.
 <p>
 If upon any firing the input is absent, then both outputs
 will be absent, and the actor will reinitialize. Hence, on
 the next firing where the input is present, this actor will
 behave as if that firing is a first firing.
 <p>
 Note that this actor exercises no control at all over step sizes.
 It simply works with whatever step sizes are provided. Thus,
 it is mathematically questionable to use it in any model except
 where its input comes from an Integrator or its outputs go
 to an Integrator. The Integrator actor will exercise control
 over step sizes.

 @author Edward A. Lee and Janette Cardoso
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class Derivative extends TypedAtomicActor {

    /** Construct a derivative actor.
     *  @param container The container.
     *  @param name The name.
     *  @exception NameDuplicationException If the name is used by
     *  another actor in the container.
     *  @exception IllegalActionException If ports can not be created, or
     *   thrown by the super class.
     *  @see ptolemy.domains.continuous.kernel.ContinuousIntegrator
     */
    public Derivative(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);

        derivative = new TypedIOPort(this, "derivative", false, true);
        derivative.setTypeEquals(BaseType.DOUBLE);

        impulse = new TypedIOPort(this, "impulse", false, true);
        impulse.setTypeEquals(BaseType.DOUBLE);
        StringAttribute cardinality = new StringAttribute(impulse, "_cardinal");
        cardinality.setExpression("SOUTH");

        // icon
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"-25\" y=\"0\" "
                + "style=\"font-size:14\">\n" + "d/dt \n" + "</text>\n"
                + "style=\"fill:blue\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The derivative output port. This port has type double.
     */
    public TypedIOPort derivative;

    /** The impulse output port. This port has type double.
     */
    public TypedIOPort impulse;

    /** The input port. This port has type double.
     */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce outputs as specified in the class comment.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (!input.isKnown(0)) {
            if (_debugging) {
                _debug("fire: Input is not known.");
            }
            return;
        }

        if (!input.hasToken(0)) {
            if (_debugging) {
                _debug("fire: Input has no token.");
            }
            return;
        }
        // Have to use the time from the port, not the director,
        // because if this domain is embedded, then the
        // director returns global time.
        Time currentTime = input.getModelTime(0);
        DoubleToken currentInput = (DoubleToken) input.get(0);

        if (_debugging) {
            _debug("fire at time: " + currentTime + ", microstep "
                    + ((ContinuousDirector) getDirector()).getIndex()
                    + "\n-- current input: " + currentInput
                    + "\n-- _previousOutput: " + _previousOutput
                    + "\n-- _previousInput: " + _previousInput
                    + "\n-- _previousTime: " + _previousTime);
        }
        if (_previousTime == null) {
            // First firing.
            derivative.send(0, DoubleToken.ZERO);

            if (_debugging) {
                _debug("fire: first firing. Sending zero.");
            }

            if (currentInput.doubleValue() != 0.0) {
                impulse.send(0, currentInput);
                if (_debugging) {
                    _debug("fire: Initial value is not zero. Sending impulse: "
                            + currentInput);
                }
            }
        } else {
            // Not the first firing.
            if (currentTime.equals(_previousTime)) {
                // No time has elapsed.
                derivative.send(0, _previousOutput);
                if (_debugging) {
                    _debug("fire: No time has elapsed. Sending previous output: "
                            + _previousOutput);
                }
                if (_previousInput != currentInput.doubleValue()) {
                    impulse.send(0, new DoubleToken(currentInput.doubleValue()
                            - _previousInput));
                    if (_debugging) {
                        _debug("fire: Discontinuity. Sending impulse: "
                                + (currentInput.doubleValue() - _previousInput));
                    }
                }
            } else {
                // Time has elapsed.
                double timeGap = currentTime.subtract(_previousTime)
                        .getDoubleValue();
                double derivativeValue = (currentInput.doubleValue() - _previousInput)
                        / timeGap;
                derivative.send(0, new DoubleToken(derivativeValue));
                if (_debugging) {
                    _debug("fire: Time has elapsed. Sending output: "
                            + derivativeValue);
                }
            }
        }
    }

    /** Ensure that the next invocation of the fire() method is treated
     *  as a first firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _previousTime = null;
        _previousOutput = null;
        _previousInput = 0.0;
    }

    /** Record the current input and time.
     *  @exception IllegalActionException If the superclass throws it.
     *  @return Whatever the superclass returns (true).
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();

        if (!input.hasToken(0)) {
            initialize();
            if (_debugging) {
                _debug("Postfire: Input has no token. Initializing.");
            }
            return result;
        }
        // Have to completely recalculate the current output
        // because the last invocation of fire() is not
        // necessarily at the current time.
        // Have to use the time from the port, not the director,
        // because if this domain is embedded, then the
        // director returns global time.
        Time currentTime = input.getModelTime(0);
        DoubleToken currentInput = (DoubleToken) input.get(0);
        if (_previousTime == null) {
            // First firing.
            _previousOutput = DoubleToken.ZERO;
            if (_debugging) {
                _debug("First postfire");
            }
        } else {
            // Not the first firing.
            if (!currentTime.equals(_previousTime)) {
                // Time has elapsed.
                double timeGap = currentTime.subtract(_previousTime)
                        .getDoubleValue();
                double derivativeValue = (currentInput.doubleValue() - _previousInput)
                        / timeGap;
                _previousOutput = new DoubleToken(derivativeValue);
            }
        }
        _previousInput = ((DoubleToken) input.get(0)).doubleValue();
        _previousTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("postfire at time: " + currentTime + ", microstep "
                    + ((ContinuousDirector) getDirector()).getIndex()
                    + "\n-- current input: " + currentInput
                    + "\n-- _previousOutput updated to: " + _previousOutput
                    + "\n-- _previousInput updated to: " + _previousInput
                    + "\n-- _previousTime updated to: " + _previousTime + "\n");
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The time of the previous input, or null on the first firing. */
    private Time _previousTime;

    /** The value of the previous output, or null on the first firing. */
    private DoubleToken _previousOutput;

    /** The value of the previous input, or 0.0 on the first firing. */
    private double _previousInput;
}
