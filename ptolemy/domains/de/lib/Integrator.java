/* An actor that outputs the discrete integration over successive inputs.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Integrator

/** Output the discrete integral of the input. Inputs are multiplied by the time
 *  gap from the previous input and accumulated. Output is not generated
 *   until two inputs have been consumed.
 *  <p>
 *  The output type of this actor is forced to be double.
 *  <p>
 *  In postfire(), if an event is present on the <i>reset</i> port, this
 *  actor resets to its initial state, and will not output until two
 *  subsequent inputs have been consumed.  This is useful if the input signal is
 *  switched on and off, in which case the time gap between events becomes large
 *  and would otherwise effect the value of the integral.
 *  <p>
 *  The integrator performs linear interpolation between input events,
 *  where the output of the integrator follows the equation
 *  y[n] = y[n-1] + (x[n-1] + x[n])*dt/2 where <i>dt</i> is the time
 *  differential between events. This equates to the trapezoidal method of
 *  approximating a Riemann integral.

 @author Jeff C. Jensen
 @version $Id$
 @since Ptolemy II 8.0
 @see ptolemy.actor.lib.Accumulator
 */
public class Integrator extends DETransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Integrator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        reset = new TypedIOPort(this, "reset", true, false);
        reset.setMultiport(true);
        output.setTypeAtLeast(input);
        output.setWidthEquals(input, false);
        initialValue = new Parameter(this, "initialValue");
        initialValue.setExpression("0.0");
    }

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
        Integrator newObject = (Integrator) super.clone(workspace);

        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setWidthEquals(newObject.input, false);

        // This is not strictly needed (since it is always recreated
        // in preinitialize) but it is safer.
        newObject._lastInput = null;
        newObject._currentInput = null;
        newObject._accumulated = null;

        return newObject;
    }

    /** Consume at most one token from the <i>input</i> port and output
     *  the average of it and the previous input (linear interpolation),
     *  multiplied by the time gap between the two events.
     *  If there has been no previous iteration, no output is sent unless
     *  an initial token has been set.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If subtraction or division is not
     *   supported by the supplied tokens.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            Time currentTime = getDirector().getModelTime();
            Token currentToken = input.get(0);
            _currentInput = new TimedEvent(currentTime, currentToken);

            if (_lastInput != null) {
                Token lastToken = (Token) _lastInput.contents;
                Time lastTime = _lastInput.timeStamp;
                Token timeGap = new DoubleToken(currentTime.subtract(lastTime)
                        .getDoubleValue());
                Token integrand = new DoubleToken(0.0);

                //Calculate the interpolated value, multiply by dt
                integrand = currentToken.add(lastToken).multiply(timeGap)
                        .divide(new DoubleToken(2));

                //Accumulate the integrand
                if (_accumulated != null) {
                    _accumulated = _accumulated.add(integrand);
                } else {
                    _accumulated = integrand;
                }
            }
        }

        //If we have accumulated a value, output it here; otherwise,
        //   we did not have an initial value and have not yet received
        //   two inputs.
        if (_accumulated != null) {
            output.broadcast(_accumulated);
        }
    }

    /** Reset to indicate that no input has yet been seen.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastInput = null;

        resetAccumulation();
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
                //Consume reset token
                reset.get(0);

                //Reset the current input
                _currentInput = null;

                //Reset accumulation
                resetAccumulation();
            }
        }
        _lastInput = _currentInput;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Reset value of the accumulator to either an initial value or null.
     * @exception IllegalActionException If the base class throws it
     */
    protected void resetAccumulation() throws IllegalActionException {
        Token initialToken = initialValue.getToken();

        if (initialToken != null) {
            _accumulated = initialToken;
        } else {
            _accumulated = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The reset port, which has undeclared type. If this port
     *  receives a token, this actor resets to its initial state,
     *  and no output is generated until two inputs have been received.
     */
    public TypedIOPort reset;

    /** The value produced by the actor on its first iteration.
     *  The default value of this parameter is the double 0.0.
     */
    public Parameter initialValue;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private TimedEvent _currentInput;

    private TimedEvent _lastInput;

    private Token _accumulated;
}
