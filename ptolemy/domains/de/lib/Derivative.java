/* An actor that outputs the discrete derivative between successive inputs.

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
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Derivative

/**
 Output the discrete derivative of the input, y[n] = (x[n] - x[n-1])/dt,
 where <i>dt</i> is the time gap between input events. Output is not generated
 until two inputs have been consumed.
 <p>
 The output of this actor is constrained to be a double, and input must be castable
 to a double. If the input signal is not left-continuous, the derivative will be either
 infinite or undefined and an exception is thrown.
 <p>
 In postfire(), if an event is present on the <i>reset</i> port, this
 actor resets to its initial state, and will not output until two
 subsequent inputs have been consumed.  This is useful if the input signal is
 switched on and off, in which case the time gap between events becomes large
 and would otherwise effect the value of the derivative for one sample.
 <p>
 @author Jeff C. Jensen
 @version $Id: Derivative.java$
 @since Ptolemy II 8.0
 @see ptolemy.actor.lib.Differential
 */
public class Derivative extends DETransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Derivative(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        reset = new TypedIOPort(this, "reset", true, false);
        reset.setMultiport(true);
        input.setTypeAtMost(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
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
        Derivative newObject = (Derivative) super.clone(workspace);

        newObject.input.setTypeAtMost(BaseType.DOUBLE);
        newObject.output.setTypeEquals(BaseType.DOUBLE);

        // This is not strictly needed (since it is always recreated
        // in preinitialize) but it is safer.
        newObject._lastInput = null;

        return newObject;
    }

    /** Consume at most one token from the <i>input</i> port and output
     *  its value minus the value of the input read in the previous
     *  iteration, divided by the time gap between the two events.
     *  If there has been no previous iteration, no output is sent.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If subtraction or division is not
     *   supported by the supplied tokens.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            Time currentTime = getDirector().getModelTime();
            DoubleToken currentToken = (DoubleToken) input.get(0);
            _currentInput = new TimedEvent(currentTime, currentToken);

            if (_lastInput != null) {
                Time lastTime = _lastInput.timeStamp;
                DoubleToken lastToken = (DoubleToken) _lastInput.contents;
                DoubleToken timeGap = new DoubleToken(currentTime.subtract(
                        lastTime).getDoubleValue());

                //If the timeGap is zero, then we have received a simultaneous event. If the
                // value of the input has not changed, then we can ignore this input, as a control
                // signal was already generated. However if the value has changed, then the signal
                // is discontinuous and an exception will be thrown.
                if (timeGap.doubleValue() == 0
                        && !currentToken.equals(lastToken)) {
                    throw new IllegalActionException(
                            "Derivative received discontinuous input.");
                }

                output.broadcast(currentToken.subtract(lastToken).divide(
                        timeGap));
            }
        }
    }

    /** Reset to indicate that no input has yet been seen.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastInput = null;
        _currentInput = null;
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
            }
        }
        _lastInput = _currentInput;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The reset port, which has undeclared type. If this port
     *  receives a token, this actor resets to its initial state,
     *  and no output is generated until two inputs have been received.
     */
    public TypedIOPort reset;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private TimedEvent _currentInput;

    private TimedEvent _lastInput;
}
