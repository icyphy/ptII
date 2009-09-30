/* An actor that implements a discrete PID controller.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
import ptolemy.data.type.Typeable;
import ptolemy.data.type.BaseType.DoubleType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Differential

/**
 Generate PID output for a given input. The output is the sum of a proportional
 gain (P), discrete integration (I), and discrete derivative (D).
 <p>
 The proportional component of the output is immediately available, such that
 yp[n]=Kp*x[n], where <i>yp</i> is the proportional component of the output,
 <i>Kp</i> is the proportional gain, and <i>x</i> is the input value.
 <p>
 For integral gain, the output is available after two input symbols have been
 received, such that yi[n]=Ki*(yi[n-1]+(x[n] + x[n-1]))*dt[n]/2, where <i>yi</i>
 is the integral component of the output, <i>Ki</i> is the integral gain, and
 <i>dt[n]</i> is the time differential between input events x[n] and x[n-1].
 <p>
 For derivative gain, the output is available after two input symbols have been
 received, such that yd[n] = Kd*(x[n]-x[n-1])/dt, where <i>yd</i> is the
 derivative component of the output, <i>Kd</i> is the derivative gain, and
 <i>dt</i> is the time differential between input events events x[n] and x[n-1].
 <p>
 The output <i>y</i> of this actor is the sum of the three gains; if only one
 input symbol has been received, only proportional control is available, so
 <p>
 y[0]=Kp*x[0]
 <br>y[n] = yp[n] + yi[n] + yd[n]
 <br>y[n] = Kp*x[n] + Ki*sum{x=1}{n}{(x[n]+x[n-1])/2*dt[n]} + Kd*(x[n]-x[n-1]/dt[n])
 <p>
 The output type of this actor is forced to be double.
 <p>
 In postfire(), if an event is present on the <i>reset</i> port, this
 actor resets to its initial state, where integral and derivative components
 of output will not be present until two subsequent inputs have been consumed.
 <p>
 @author Jeff C. Jensen
 @version $Id: PID.java 39805 2005-10-28 20:19:33Z cxh $
 @since Ptolemy II 8.1
 @see ptolemy.actor.lib.Integrator, ptolemy.actor.lib.Derivative
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
        output.setTypeAtLeast(input);
        output.setWidthEquals(input, false);
        Kp = new Parameter(this, "Kp");
        Kp.setExpression("1.0");
        Ki = new Parameter(this, "Ki");
        Ki.setExpression("0.0");
        Kd = new Parameter(this, "Kd");
        Kd.setExpression("0.0");
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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PID newObject = (PID) super.clone(workspace);

        newObject.output.setTypeAtLeast((Typeable) DoubleType.DOUBLE);
        newObject.output.setWidthEquals(newObject.input, false);

        // This is not strictly needed (since it is always recreated
        // in preinitialize) but it is safer.
        newObject._lastInput = null;
        newObject._currentInput = null;
        newObject._accumulated = new DoubleToken(0.0);

        return newObject;
    }

    /** Consume at most one token from the <i>input</i> port and output
     *  the PID control. If there has been no previous iteration, only
     *  proportional output is generated.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If addition, multiplication,
     *  subtraction, or division is not supported by the supplied tokens.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        //Consume input, generate output only if input provided
        if (input.hasToken(0)) {
            Time  currentTime = getDirector().getModelTime(); 
            Token currentToken = input.get(0);
            Token outputComponents = new DoubleToken(0.0);
            
            _currentInput = new TimedEvent(currentTime, currentToken);
            
            //Add proportional component to controller output
            outputComponents = currentToken.multiply(Kp.getToken());
            
            //If a previous input was given, then add integral and derivative components
            if(_lastInput != null){
                Token lastToken = (Token)_lastInput.contents;
                Time  lastTime = _lastInput.timeStamp;
                Token timeGap = new DoubleToken(currentTime.subtract(lastTime).getDoubleValue());
                
                //Calculate integral component and accumulate
                _accumulated = _accumulated.add(currentToken.add(lastToken)
                        .multiply(timeGap)
                        .divide(new DoubleToken(2)));

                //Add integral component to controller output
                outputComponents = outputComponents.add(_accumulated.multiply(Ki.getToken()));
                
                //Add derivative component to controller output
                outputComponents = outputComponents.add(
                        currentToken.subtract(lastToken)
                                    .divide(timeGap)
                                    .multiply(Kd.getToken()));
            }
            
            output.broadcast(outputComponents);
        }
    }

    /** Reset to indicate that no input has yet been seen.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastInput = null;
        _accumulated = (Token)(new DoubleToken(0.0)); 
    }

    /** Record the most recent input as the latest input. If a reset
     *  event has been received, process it here.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        //If reset port is connected and has a token, reset state.
        if(reset.getWidth() > 0){
            if(reset.hasToken(0)){
                //Consume reset token
                reset.get(0);
                
                //Reset the current input
                _currentInput = null;
                
                //Reset accumulation
                _accumulated = (Token)(new DoubleToken(0.0)); 
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
    
    /** Proportional gain of the controller. Default value is 1.0.
     * */
    public Parameter Kp;
    
    /** Integral gain of the controller. Default value is 0.0,
     *  which effectively disables integral control.
     * */
    public Parameter Ki;
    
    /** Derivative gain of the controller. Default value is 0.0,
     *  which effectively disables derivative control.
     * */
    public Parameter Kd;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private TimedEvent _currentInput;

    private TimedEvent _lastInput;
    
    private Token _accumulated;
}
