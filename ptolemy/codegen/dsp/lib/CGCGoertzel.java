/* Goertzel, CGC domain: CGCGoertzel.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzel.pl by ptlang
 */
/*
  Copyright (c) 1990-2005 The Regents of the University of California.
  All rights reserved.
  See the file $PTOLEMY/copyright for copyright notice,
  limitation of liability, and disclaimer of warranty provisions.
*/
package ptolemy.codegen.lib;

import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.codegen.kernel.ClassicCGCActor;
import ptolemy.codegen.kernel.ClassicPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCGoertzel
/**
   Second-order recursive computation of the kth coefficient of an
   N-point DFT using Goertzel's algorithm.
   <p>
   <p>
   This two-pole, one-zero IIR filter has a transfer function of
   <pre>
   1 - (W<sub>N</sub>)<sup>k</sup> z<sup>-1</sup>
   ---------------------------
   1 - 2cos(2*pi*k/N)z<sup>-1</sup> + z<sup>-2</sup>

   </pre>
   This filter is a biquad filter with
   <p>
   <ul>
   <li> n<sub>0</sub> = 1
   <li> n<sub>1</sub> = -W<sub>n</sub> = exp(j 2 pi k / N)
   <li> n<sub>2</sub> = 0
   <li> d<sub>1</sub> = -2 cos(2 pi k / N)
   <li> d<sub>2</sub> = 1
   </ul>
   <p>
   The Goertzel's implementation takes the simpler forms of n<sub>0</sub>, n<sub>2</sub>, and d<sub>2</sub>
   into account, and computes n<sub>1</sub> and d<sub>1</sub> from the parameters k and N.
   It is implemented in direct form II.
   <h3>References</h3>
   <p>[1]
   A. V. Oppenheim and R. W. Schafer, <i>Discrete-Time Signal Processing</i>,
   Prentice-Hall: Englewood Cliffs, NJ, 1989.

   @Author Brian L. Evans
   @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzel.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCGoertzel extends CGCGoertzelBase {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCGoertzel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

        // internal state for the storage of the negative of the real part of the\ntwiddle factor, which is a function of k and N FloatState
        negWnReal = new Parameter(this, "negWnReal");
        negWnReal.setExpression("0.0");

        // internal state for the storage of the imaginary part of the twiddle factor,\nwhich is a function of k and N FloatState
        negWnImag = new Parameter(this, "negWnImag");
        negWnImag.setExpression("0.0");

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type complex.
     */
    public ClassicPort output;

    /**
     *  internal state for the storage of the negative of the real part of the
     twiddle factor, which is a function of k and N parameter with initial value "0.0".
    */
    public Parameter negWnReal;

    /**
     *  internal state for the storage of the imaginary part of the twiddle factor,
     which is a function of k and N parameter with initial value "0.0".
    */
    public Parameter negWnImag;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generateInitializeCode() {
        //# line 70 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzel.pl"
        // check parameter values and compute theta
        CGCGoertzelBase::setup();

        negWnReal = -cos(theta);
        negWnImag = -sin(theta);
    }

    /**
     */
    public void  generateFireCode() {
        //# line 82 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzel.pl"
        // Discard all but the last sample
        CGCGoertzelBase::go();

        // Final value is function of the two real IIR state values
        addCode(finalValue);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String finalValue =
    "                $ref(output).real = $ref(state1) +\n"
    + "                                    $ref(state2) * $ref(negWnReal);\n"
    + "                $ref(output).imag = $ref(state2) * $ref(negWnImag);\n";
}
