/* An actor that outputs the sine of the input.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Sine
/**
Produce an output token on each firing with a value that is
equal to the sine of the input, scaled and shifted according to the
parameters. The input and output types
are DoubleToken. The actor implements the function:
<br><i>
output = amplitude*sin(omega*input+phase)
</i><br>
<i>amplitude</i> and <i>omega</i> have default value 1. When <i>input</i>
is time, <i>omega</i> is the frequency in radians. <i>phase</i> has default
value 0.
<p>
A cosine function can be implemented using this actor by setting
the phase to pi/2.

@author Edward A. Lee, Jie Liu
@version $Id$
*/

public class Sine extends VectorizableTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Sine(TypedCompositeActor container, String name)
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
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        Sine newobj = (Sine)super.clone(ws);
        newobj.amplitude = (Parameter)newobj.getAttribute("amplitude");
        newobj.omega = (Parameter)newobj.getAttribute("omega");
        newobj.phase = (Parameter)newobj.getAttribute("phase");
        return newobj;
    }

    /** Compute the sine of the input.  If there is no input, then
     *  produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0, _vectorLength)) {
            input.get(0, (DoubleToken[])_inArray);
            double A = ((DoubleToken)amplitude.getToken()).doubleValue();
            double w = ((DoubleToken)omega.getToken()).doubleValue();
            double p = ((DoubleToken)phase.getToken()).doubleValue();
	    for (int i = 0; i < _inArray.length; i++) {
		double result = A*Math.sin(w*_inArray[i].doubleValue()+p);
		_resultArray[i] = new DoubleToken(result);
	    }
            output.send(0, _resultArray);
        }
    }

    /**  Allocate DoubleToken arrays for use in the fire() method.
      *  @exception IllegalActionException If the parent class throws it.
      */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _inArray = new DoubleToken[_vectorLength];
	_resultArray = new DoubleToken[_vectorLength];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private DoubleToken[] _inArray;
    private DoubleToken[] _resultArray;

}
