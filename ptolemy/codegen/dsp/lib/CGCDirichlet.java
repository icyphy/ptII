/* Dirichlet, CGC domain: CGCDirichlet.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCDirichlet.pl by ptlang
*/
/*
Copyright (c) 1995-1997 The Regents of the University of California.
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
//// CGCDirichlet
/**
This star computes the normalized Dirichlet kernel (also called the aliased
sinc function):

d(x) = sin(N x / 2) / (N sin(x/2))

The value of the normalized Dirichlet kernel at x = 0 is always 1, and the
normalized Dirichlet kernel oscillates between -1 and +1.
The normalized Dirichlet kernel is periodic in x with a period of either
2*pi when N is odd or 4*pi when N is even.
<p>
The Dirichlet kernel (a.k.a. the aliased sinc function) is the
discrete-time Fourier transform (DTFT) of a sampled pulse function.
The parameter <i>N</i> is the length of the pulse [1].
<h3>References</h3>
<p>[1]  
A. V. Oppenheim and R. W. Schafer, <i>Discrete-Time Signal Processing</i>,
Prentice-Hall: Englewood Cliffs, NJ, 1989.

 @Author Brian Evans
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCDirichlet.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCDirichlet extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCDirichlet(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Length of the Dirichlet kernel. IntState
        N = new Parameter(this, "N");
        N.setExpression("10");

/* 
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
The input x to the Dirichlet kernel.
     */
    public ClassicPort input;

    /**
The output of the Dirichlet kernel.
     */
    public ClassicPort output;

    /**
     *  Length of the Dirichlet kernel. parameter with initial value "10".
     */
     public Parameter N;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generatePreinitializeCode() {
        //# line 51 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCDirichlet.pl"
addInclude("<math.h>");
     }

    /**
     */
    public void  generateFireCode() {
        //# line 54 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCDirichlet.pl"
addCode(dirichlet);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String dirichlet = 
        "		const double DELTA = 1.0e-9;\n"
        + "		double x = $ref(input);\n"
        + "    		double sinInDenom = sin(x/2);\n"
        + "		double length = (double)$val(N);\n"
        + "		double dirichletValue;\n"
        + "\n"
        + "		if ( ( -DELTA < sinInDenom ) && ( sinInDenom < DELTA ) )\n"
        + "		  // Use L'Hopital's Rule when sin(x/2) is approx. 0\n"
        + "		  dirichletValue = cos(length * x / 2) / cos(x / 2);\n"
        + "		else\n"
        + "		  // Otherwise, compute it using the definition\n"
        + "		  dirichletValue = sin(length * x / 2) / (length * sinInDenom);\n"
        + "\n"
        + "		$ref(output) = dirichletValue;\n";
}
