/* An actor that outputs the sine of the input.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Sine
/**
Produce an output token on each firing with a value that is
equal to the sine of the input, scaled and shifted according to the
parameters. The input and output types
are DoubleToken. The actor implements the function:
<br>
y = A*sin(w*x+p)
<br>
where: <br>
x is the input;<br>
y is the output; <br>
A is the amplitude, which has default value 1; <br>
w is the frequency, which has default value 1; <br>
p is the phase, which has default value 0.<br>
<p>
A cosine function can be implemented using this actor by setting
the phase to pi/2.

@author Edward A. Lee, Jie Liu
@version $Id$
*/

public class Sine extends Transformer {

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
        frequency = new Parameter(this, "frequency", new DoubleToken(1.0));
        phase = new Parameter(this, "phase", new DoubleToken(0.0));


        input.setTypeEquals(DoubleToken.class);
        output.setTypeEquals(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The magnitude.
     *  The default value of this parameter is the double 1.0.
     */
    public Parameter amplitude;

    /** The frequency (in radians).  Note that this is a frequency
     *  only if the input is time.
     *  The default value of this parameter is the double 1.0.
     */
    public Parameter frequency;

    /** The phase.
     *  The default value of this parameter is the double 0.0.
     */
    public Parameter phase;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute the sine of the input.  If there is no input, then
     *  produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            DoubleToken in = (DoubleToken)input.get(0);
            double A = ((DoubleToken)amplitude.getToken()).doubleValue();
            double w = ((DoubleToken)frequency.getToken()).doubleValue();
            double p = ((DoubleToken)phase.getToken()).doubleValue();

            double result = A*Math.sin(w*in.doubleValue()+p);
            output.broadcast(new DoubleToken(result));
        }
    }
}

