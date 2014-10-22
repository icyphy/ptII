/* An actor that implements a discrete PID controller.

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
package ptolemy.domains.de.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Complex;

///////////////////////////////////////////////////////////////////
//// PID

/**
 Generate PID output for a given input. The output is the sum of a proportional
 gain (P), discrete integration (I), and discrete derivative (D).
 <p>
 The proportional component of the output is immediately available, such that
 yp[n]=Kp*x[n], where <i>yp</i> is the proportional component of the output,
 <i>Kp</i> is the proportional gain, and <i>x</i> is the input value.
 <p>
 For integral gain, the output is available after two input symbols have been
 received, such that yi[n]=Ki*(yi[n-1]+(x[n] + x[n-1])*dt[n]/2), where <i>yi</i>
 is the integral component of the output, <i>Ki</i> is the integral gain, and
 <i>dt[n]</i> is the time differential between input events x[n] and x[n-1].
 <p>
 For derivative gain, the output is available after two input symbols have been
 received, such that yd[n] = Kd*(x[n]-x[n-1])/dt, where <i>yd</i> is the
 derivative component of the output, <i>Kd</i> is the derivative gain, and
 <i>dt</i> is the time differential between input events events x[n] and x[n-1].
 <p>
 The output of this actor is constrained to be a double, and input must be castable
 to a double. If the input signal is not left-continuous and the derivative constant
 is nonzero, then this actor will throw an exception as the derivative will be either infinite
 or undefined. If the derivative constant is zero, then this actor may receive
 discontinuous input.
 <p>
 y[0]=Kp*x[0]
 <br>y[n] = yp[n] + yi[n] + yd[n]
 <br>y[n] = Kp*x[n] + Ki*sum{x=1}{n}{(x[n]+x[n-1])/2*dt[n]} + Kd*(x[n]-x[n-1]/dt[n])
 <p>
 In postfire(), if an event is present on the <i>reset</i> port, this
 actor resets to its initial state, where integral and derivative components
 of output will not be present until two subsequent inputs have been consumed.
 This is useful if the input signal is switched on and off, in which case the
 time gap between events becomes large and would otherwise effect the value of
 the derivative (for one sample) and the integral.
 <p>
 @author Jeff C. Jensen
 @version $Id$
 @since Ptolemy II 8.0
 @see ptolemy.domains.de.lib.Integrator
 @see ptolemy.domains.de.lib.Derivative
 */
public class PID extends DETransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PID(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        reset = new TypedIOPort(this, "reset", true, false);
        reset.setMultiport(true);
        input.setTypeAtMost(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
        Kp = new Parameter(this, "Kp");
        Kp.setExpression("1.0");
        Ki = new Parameter(this, "Ki");
        Ki.setExpression("0.0");
        Kd = new Parameter(this, "Kd");
        Kd.setExpression("0.0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The reset port, which has undeclared type. If this port
     *  receives a token, this actor resets to its initial state,
     *  and no output is generated until two inputs have been received.
     */
    public TypedIOPort reset;

    /** Proportional gain of the controller. Default value is 1.0.
     * */
    public Parameter Kp;

    /** Integral gain of the controller. Default value is 0.0,
     *  which disables integral control.
     * */
    public Parameter Ki;

    /** Derivative gain of the controller. Default value is 0.0, which disables
     *  derivative control. If Kd=0.0, this actor can receive discontinuous
     *  signals as input; otherwise, if Kd is nonzero and a discontinuous signal
     *  is received, an exception will be thrown.
     */
    public Parameter Kd;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PID newObject = (PID) super.clone(workspace);

        newObject.input.setTypeAtMost(BaseType.DOUBLE);
        newObject.output.setTypeEquals(BaseType.DOUBLE);

        // This is not strictly needed (since it is always recreated
        // in preinitialize) but it is safer.
        newObject._lastInput = null;
        newObject._currentInput = null;
        newObject._accumulated = new DoubleToken(0.0);

        return newObject;
    }

    /** If the attribute is <i>Kp</i>, <i>Ki</i>, or <i>Kd</i> then ensure
     *  that the value is numeric.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the value is non-numeric.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == Kp || attribute == Ki || attribute == Kd) {
            try {
                Parameter value = (Parameter) attribute;
                if (value.getToken() == null
                        || ((DoubleToken) value.getToken()).isNil()) {
                    throw new IllegalActionException(this,
                            "Must have a numeric value for gains.");
                }
            } catch (ClassCastException e) {
                throw new IllegalActionException(this,
                        "Gain values must be castable to a double.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Reset to indicate that no input has yet been seen.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastInput = null;
        _accumulated = new DoubleToken(0.0);
    }

    /** Consume at most one token from the <i>input</i> port and output
     *  the PID control. If there has been no previous iteration, only
     *  proportional output is generated.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If addition, multiplication,
     *  subtraction, or division is not supported by the supplied tokens.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Consume input, generate output only if input provided.
        if (input.hasToken(0)) {
            Time currentTime = getDirector().getModelTime();
            DoubleToken currentToken = (DoubleToken) input.get(0);
            _currentInput = new TimedEvent(currentTime, currentToken);

            // Add proportional component to controller output.
            DoubleToken currentOutput = (DoubleToken) currentToken.multiply(Kp
                    .getToken());

            // If a previous input was given, then add integral and
            // derivative components.
            if (_lastInput != null) {
                DoubleToken lastToken = (DoubleToken) _lastInput.contents;
                Time lastTime = _lastInput.timeStamp;
                DoubleToken timeGap = new DoubleToken(currentTime.subtract(
                        lastTime).getDoubleValue());

                //If the timeGap is zero, then we have received a
                // simultaneous event. If the value of the input has
                // not changed, then we can ignore this input, as a
                // control signal was already generated. However if
                // the value has changed, then the signal is
                // discontinuous and we should throw an exception
                // unless derivative control is disabled (Kd=0).

                if (timeGap.isCloseTo(DoubleToken.ZERO, Complex.EPSILON)
                        .booleanValue()) {
                    if (!((DoubleToken) Kd.getToken()).isCloseTo(
                            DoubleToken.ZERO, Complex.EPSILON).booleanValue()
                            && !currentToken.equals(lastToken)) {
                        throw new IllegalActionException(this,
                                "PID controller recevied discontinuous input.");
                    }
                }
                // Otherwise, the signal is continuous and we add
                // integral and derivative components.
                else {
                    if (!((DoubleToken) Ki.getToken()).isCloseTo(
                            DoubleToken.ZERO, Complex.EPSILON).booleanValue()) {
                        //Calculate integral component and accumulate
                        _accumulated = (DoubleToken) _accumulated
                                .add(currentToken.add(lastToken)
                                        .multiply(timeGap)
                                        .multiply(new DoubleToken(0.5)));
                        // Add integral component to controller output.
                        currentOutput = (DoubleToken) currentOutput
                                .add(_accumulated.multiply(Ki.getToken()));
                    }

                    // Add derivative component to controller output.
                    if (!((DoubleToken) Kd.getToken()).isCloseTo(
                            DoubleToken.ZERO, Complex.EPSILON).booleanValue()) {
                        currentOutput = (DoubleToken) currentOutput
                                .add(currentToken.subtract(lastToken)
                                        .divide(timeGap)
                                        .multiply(Kd.getToken()));
                    }
                }
            }

            output.broadcast(currentOutput);
        }
    }

    /** Record the most recent input as the latest input. If a reset
     *  event has been received, process it here.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        //If reset port is connected and has a token, reset state.
        if (reset.getWidth() > 0) {
            if (reset.hasToken(0)) {
                // Consume reset token.
                reset.get(0);

                // Reset the current input.
                _currentInput = null;

                // Reset accumulation.
                _accumulated = new DoubleToken(0.0);
            }
        }
        _lastInput = _currentInput;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private TimedEvent _currentInput;

    private TimedEvent _lastInput;

    private DoubleToken _accumulated;
}
