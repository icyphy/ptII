/* An SDF actor that outputs the sine of the input, and reads the
frequency value from input ports.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SineFM
/**
This actor computes the sine of the input signal. This actor,
unlike SDFSine, reads the frequency value from the input port.
This allows the frequency value to be updated at the sample rate,
making this actor useful for FM applications, such as FM synthesis.
<p>
Produce an output token on each firing with a value that is
equal to the sine of the input, scaled and shifted according to the
parameters and the current <i>omega</i> input port value.
In the actual implementation, <i>rate</i> tokens are
consumed and produced by the input and output ports, respectively,
on each call to fire(). The parameter values are reread on each
call to fire(), and therefore are updated every <i>rate</i> tokens.
The frequency and amplitude values are updated every token.
 The input and output types
are DoubleToken. The actor implements the function:
<br><i>
output = amplitude*sin(omega*input+phase)
</i><br>
Note that here, <i>amplitude</i> and <i>omega</i> values are read from
input ports, whereas <i>phase</i> is a parameter value.
When <i>input</i>
is time, <i>omega</i> is the frequency in radians. <i>phase</i> has default
value 0.
<p>
A cosine function can be implemented using this actor by setting
the phase to pi/2.

@author Brian K. Vogel.
@version $Id$
*/

public class SineFM extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SineFM(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	// parameters
	omega = new SDFIOPort(this, "omega", true, false);
	omega.setTypeEquals(BaseType.DOUBLE);

        amplitude = new Parameter(this, "amplitude", new DoubleToken(1.0));
	amplitude.setTypeEquals(BaseType.DOUBLE);

	phase = new Parameter(this, "phase", new DoubleToken(0.0));
	phase.setTypeEquals(BaseType.DOUBLE);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The omega (in radians).  Note that this is a frequency
     *  only if the input is time.
     */
    public SDFIOPort omega;

    /** The magnitude.
     *  The default value of this parameter is the double 1.0.
     */
    public Parameter amplitude;

    /** The phase.
     *  The default value of this parameter is the double 0.0.
     */
    // FIXME: Consider making this an input port instead.
    public Parameter phase;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        SineFM newobj = (SineFM)super.clone(ws);
	newobj.omega = (SDFIOPort)newobj.getPort("omega");
	newobj.amplitude = (Parameter)newobj.getAttribute("amplitude");
        newobj.phase = (Parameter)newobj.getAttribute("phase");
        return newobj;
    }

    /** Compute the sine of the input, using the current parameter
     *  and input port values. <i>rate</i> tokens are consumed
     *  and produced on each call to this method.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
	// Check parameter values.
	double p = ((DoubleToken)phase.getToken()).doubleValue();
	double A = ((DoubleToken)amplitude.getToken()).doubleValue();

	omega.getArray(0, _omegaTokenArray);
	input.getArray(0, _tokenArray);
	double result;
	// For each samples in the current channel:
	for (int i = 0; i < _rate; i++) {
	    // Convert to double[].
	    _resultTokenArray[i] = new DoubleToken(A*Math.sin(
                        (_omegaTokenArray[i].doubleValue()) *
                        (_tokenArray[i].doubleValue())+p));
	}

	output.sendArray(0, _resultTokenArray);
    }

    /**  Allocate DoubleToken arrays for use in the fire() method.
      *  @exception IllegalActionException If the parent class throws it.
      */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _tokenArray = new DoubleToken[_rate];
	_omegaTokenArray = new DoubleToken[_rate];
	_resultTokenArray = new DoubleToken[_rate];
    }

    /** Set up the port's consumption rates.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
	super.preinitialize();
	omega.setTokenConsumptionRate(_rate);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    DoubleToken[] _tokenArray;
    DoubleToken[] _omegaTokenArray;
    DoubleToken[] _resultTokenArray;

}
