/* An SDF actor that outputs the sine of the input.

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

//////////////////////////////////////////////////////////////////////////
//// SDFSine
/**
This actor is functionally equivalent to the actor.lib.Sine actor, but
is optimized to provide better performance in the SDF domain.
<p>
Produce an output token on each firing with a value that is
equal to the sine of the input, scaled and shifted according to the
parameters. In the actual implementation, <i>rate</i> tokens are
consumed and produced by the input and output ports, respectively,
on each call to fire(). The parameter values are reread on each
call to fire(), and therefore are updated every <i>rate</i> tokens.
 The input and output types
are DoubleToken. The type is constrained to DoubleToken instead of just
at least IntToken because it would not be useful to take sin(integer).
 The actor implements the function:
<br><i>
output = amplitude*sin(omega*input+phase)
</i><br>
<i>amplitude</i> and <i>omega</i> have default value 1. When <i>input</i>
is time, <i>omega</i> is the frequency in radians. <i>phase</i> has default
value 0.
<p>
A cosine function can be implemented using this actor by setting
the phase to pi/2.

@author Brian K. Vogel. Based on Sine, by Edward A. Lee, Jie Liu
@version $Id$
*/

public class SDFSine extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFSine(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
	
	// parameters
        amplitude = new Parameter(this, "amplitude", new DoubleToken(1.0));
	amplitude.setTypeEquals(BaseType.DOUBLE);
        omega = new Parameter(this, "omega", new DoubleToken(1.0));
	omega.setTypeEquals(BaseType.DOUBLE);
        phase = new Parameter(this, "phase", new DoubleToken(0.0));
	phase.setTypeEquals(BaseType.DOUBLE);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The magnitude.
     *  The default value of this parameter is the double 1.0.
     */
    public Parameter amplitude;

    /** The omega (in radians).  Note that this is a frequency
     *  only if the input is time.
     *  The default value of this parameter is the double 1.0.
     */
    public Parameter omega;

    /** The phase.
     *  The default value of this parameter is the double 0.0.
     */
    public Parameter phase;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        SDFSine newobj = (SDFSine)super.clone(ws);
        newobj.amplitude = (Parameter)newobj.getAttribute("amplitude");
        newobj.omega = (Parameter)newobj.getAttribute("omega");
        newobj.phase = (Parameter)newobj.getAttribute("phase");
        return newobj;
    }

    /** Compute the sine of the input, using the current parameter
     *  values. <i>rate</i> tokens are consumed and produced on
     *  each call to this method.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {

	// Check parameter values.
	double A = ((DoubleToken)amplitude.getToken()).doubleValue();
	double w = ((DoubleToken)omega.getToken()).doubleValue();
	double p = ((DoubleToken)phase.getToken()).doubleValue();

	
	input.getArray(0, _tokenArray);
	double result;
	// For each samples in the current channel:
	for (int i = 0; i < _rate; i++) {
	    // Convert to double[].
	    _resultTokenArray[i] = 
		new DoubleToken(A*Math.sin(w*(_tokenArray[i].doubleValue())+p));
	}

	output.sendArray(0, _resultTokenArray);
    }

    /**  Allocate DoubleToken arrays for use in the fire() method.
      *  @exception IllegalActionException If the parent class throws it.
      */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _tokenArray = new DoubleToken[_rate];
	_resultTokenArray = new DoubleToken[_rate];
    }  

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    DoubleToken[] _tokenArray;
    DoubleToken[] _resultTokenArray;

}
