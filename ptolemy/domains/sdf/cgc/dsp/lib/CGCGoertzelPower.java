/* GoertzelPower, CGC domain: CGCGoertzelPower.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzelPower.pl by ptlang
*/
/*
Copyright (c) 1990-1997 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty provisions.
 */
package ptolemy.domains.sdf.cgc.dsp.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCGoertzelPower
/**
Second-order recursive computation of the power of the kth coefficient of
an N-point DFT using Goertzel's algorithm.
This form is used in touchtone decoding.
<p>
<p>
This two-pole, one-zero IIR filter has a transfer function of
<pre>
           1 + z<sup>-1</sup>
    ---------------------------
    1 - 2cos(2pik/N)z<sup>-1</sup> + z<sup>-2</sup>

</pre>
This filter is a biquad filter with
<p>
<ul>
<li> n<sub>0</sub> = 1
<li> n<sub>1</sub> = 1
<li> n<sub>2</sub> = 0
<li> d<sub>1</sub> = -2 cos(2 pi k / N)
<li> d<sub>2</sub> = 1
</ul>
<p>
This implementation efficiently implements the biquad section based
on the values of these coefficients, and computes d<sub>1</sub> from the
parameters k and N.
It is implemented in direct form II, and requires three additions and
one multiply.
<h3>References</h3>
<p>[1]  
A. V. Oppenheim and R. W. Schafer, <i>Discrete-Time Signal Processing</i>,
Prentice-Hall: Englewood Cliffs, NJ, 1989.

 @Author Brian L. Evans
 @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzelPower.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCGoertzelPower extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCGoertzelPower(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

/* 
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type double.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  go() {
        //# line 60 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzelPower.pl"
        //// Generate code for the Goertzel second-order IIR filter
        //		CGCGoertzelBase::go();
        //
        //		// Compute the power of the kth DFT coefficient.
        //		// The output of the Goertzel filter is
        //		//   state1 - Wn*state2 =
        //		//       state1 - [state2*cos(theta) - j*state2*sin(theta)]
        //		// where Wn is the twiddle factor Wn = exp(-j theta), such
        //		// that theta = 2 pi k / N.
        //		// Power is complex number times its conjugate, z z*:
        //		//   z = state1 - [state2*cos(theta) - j*state2*sin(theta)]
        //		//   z = [state1 - state2*cos(theta)] - j [state2*sin(theta)]
        //		//   z z* = state1*state1 - 2*state1*state2*cos(theta) +
        //		//     state2*state2*cos^2(theta) + state2*state2*sin^2(theta)
        //		//   z z* = state1*state1 - state1*state2*2*cos(theta) +
        //		//          state2*state2
        //		// where 2*cos(theta) = d1, by definition in CGCGoertzelBase.
        //		addCode(finalValue);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String finalValue = 
        "		/* Compute z z* where z is the last ouput of Goertzel filter */\n"
        + "		$ref(output) = $ref(state1)*$ref(state1) -\n"
        + "			       $ref(state1)*$ref(state2)*$ref(d1) +\n"
        + "			       $ref(state2)*$ref(state2);\n";
}
