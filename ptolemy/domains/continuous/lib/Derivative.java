/* The derivative in the continuous domain.

 Copyright (c) 1998-2009 The Regents of the University of California.
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
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// Derivative

/**
 The derivative in the continuous domain.
 In continuous-time modeling, one should generally avoid taking derivatives
 directly. It is better to use an {@see Integrator} actor in a feedback loop.
 The input to the Integrator is the derivative its output.
 The reason is that small amounts of noise on the input of
 a derivative actor result in large output fluctuations.
 Since continuous-time simulation involves choosing step sizes,
 the choice of step size will strongly affect the resulting
 output of the derivative.
 <p>
 That said, if you have read this far, you are probably determined
 to compute a derivative. Hence, we provide this actor, which performs
 a simple operation and provides a simple guarantee. Specifically,
 a correctly connected Derivative followed by an Integrator is an
 identity function. And an Integrator followed by a Derivative is
 also an identity function.
 <p>
 Upon firing, this actor produces an output on the <i>derivative</i>
 port, and may also produce an output on the <i>impulse</i> port.
 The <i>derivative</i> output value is the difference between the
 input at the current time and the previous input divided by the
 time between these inputs, unless that time is zero. If the time
 between this input and the previous one is zero, and the value
 of the previous input and the current one is non-zero, then this
 actor will be produce the value difference on the <i>impulse</i>
 output and will produce zero on the <i>derivative</i> output.
 <p>
 On the very first firing after being initialized,
 this actor always produces zero
 on the <i>derivative</i> output. If the input is
 non-zero, then it will produce the value of the input on
 the <i>impulse</i> output.
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
 behave as if that firing is very first firing.
 <p>
 Note that this actor exercises no control at all over step sizes.
 It simply works with whatever step sizes are provided. Thus,
 it is mathematically questionable to use it in any model except
 where its input comes from an Integrator or its outputs go
 to an Integrator. The Integrator actor will exercise control
 over step sizes.
 
 @author Edward A. Lee and Janette Cardoso
 @version $Id$
 @since Ptolemy II 8.2
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
    public void fire() throws IllegalActionException {
        super.fire();
        
        if (!(input.hasToken(0))) {
            initialize();
            return;
        }
        DoubleToken currentInput = (DoubleToken)input.get(0);
        
        Time currentTime = getDirector().getModelTime();
        if (_previousTime == null) {
            // First firing.
            _previousTime = currentTime;
            derivative.send(0, _zeroToken);
            _previousOutput = _zeroToken;
            
            _previousValue = currentInput.doubleValue();
            if (_previousValue != 0.0) {
                impulse.send(0, currentInput);
            }
        } else {
            // Not the first firing.
            if (currentTime.equals(_previousTime)) {
                // No time has elapsed.
                derivative.send(0, _previousOutput);
                if (_previousValue != currentInput.doubleValue()) {
                    impulse.send(0, new DoubleToken(
                            currentInput.doubleValue() - _previousValue));
                    _previousValue = currentInput.doubleValue();
                }
            } else {
                // Time has elapsed.
                double timeGap = currentTime.subtract(_previousTime).getDoubleValue();
                double derivativeValue = (currentInput.doubleValue() - _previousValue) / timeGap;
                _previousOutput = new DoubleToken(derivativeValue);
                derivative.send(0, _previousOutput);
                _previousValue = currentInput.doubleValue();
                _previousTime = currentTime;
            }
        }
    }
    
    /** Ensure that the next invocation of the fire() method is treated
     *  as a first firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _previousTime = null;
        _previousOutput = null;
        _previousValue = 0.0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The time of the previous input, or null on the first firing. */
    private Time _previousTime;
    
    /** The value of the previous output, or null on the first firing. */
    private DoubleToken _previousOutput;

    /** The value of the previous input, or 0.0 on the first firing. */
    private double _previousValue;
    
    /** A zero token. */
    private DoubleToken _zeroToken = new DoubleToken(0.0);
}
