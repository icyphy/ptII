/* Sinc, CGC domain: CGCSinc.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCSinc.pl by ptlang
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
//// CGCSinc
/**
   This star computes the sinc of its input given in radians.
   The sinc function is defined as sin(x)/x, with value 1.0 when x = 0.
   <p>
   The discrete-time Fourier transform (DTFT) of a sampled sinc function is
   an ideal lowpass filter [1-2].
   Modulating a sampled sinc function by a cosine function gives an
   ideal bandpass signal.
   This star defines the sinc function <i>without</i> using <i>pi</i>,
   as is the convention in [2-3].
   <h3>References</h3>
   <p>[1]
   A. V. Oppenheim and R. W. Schafer, <i>Discrete-Time Signal Processing</i>,
   Prentice-Hall: Englewood Cliffs, NJ, 1989.
   <p>[2]
   A. V. Oppenheim and A. Willsky, <i>Signals and Systems</i>,
   Prentice-Hall: Englewood Cliffs, NJ, 1983.
   <p>[3]
   R. N. Bracewell, <i>The Fourier Transform and Its Applications</i>,
   McGraw-Hill: New York, 1986.

   @Author Brian L. Evans
   @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCSinc.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSinc extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSinc(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
       The input x to the sinc function.
    */
    public ClassicPort input;

    /**
       The output of the sinc function.
    */
    public ClassicPort output;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generatePreinitializeCode() {
        //# line 47 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCSinc.pl"
        addModuleFromLibrary("ptdspSinc", "src/utils/libptdsp", "ptdsp");
    }

    /**
     */
    public void  generateFireCode() {
        //# line 50 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCSinc.pl"
        addCode(sinc);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String sinc =
    "                $ref(output) = Ptdsp_Sinc((double)$ref(input));\n";
}
