/* AdaptLinQuant, CGC domain: CGCAdaptLinQuant.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCAdaptLinQuant.pl by ptlang
*/
/*
Copyright (c) 1990-1996 The Regents of the University of California.
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
//// CGCAdaptLinQuant
/**
Uniform linear quantizer symmetric about 0 with variable step size.
<p>
The input is quantized to the number of levels given by 2^<i>bits</i>.
The quantization levels are uniformly spaced at the step size given by
the <i>inStep</i> input value and are odd symmetric about zero.
Therefore, the "high" threshold is (2^<i>bits</i> - 1)*<i>inStep</i>/2, and
the "low" threshold is the negative of the "high" threshold.
Rounding to nearest level is performed. Output level will equal "high" only
if the input level equals or exceeds "high".
If the input is below "low", then the quantized output will equal "low".
The quantized value is output on the <i>amplitude</i> port as a floating-point
value, the step size is output on the <i>outStep</i> port as a floating-point
value, and the index of the quantization level on the <i>stepLevel</i> port
as a non-negative integer between 0 and 2^<i>bits</i> - 1, inclusive.

 @Author William Chen
 @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/stars/CGCAdaptLinQuant.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCAdaptLinQuant extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCAdaptLinQuant(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        inStep = new ClassicPort(this, "inStep", true, false);
        inStep.setTypeEquals(BaseType.DOUBLE);
        amplitude = new ClassicPort(this, "amplitude", false, true);
        amplitude.setTypeEquals(BaseType.DOUBLE);
        outStep = new ClassicPort(this, "outStep", false, true);
        outStep.setTypeEquals(BaseType.DOUBLE);
        stepLevel = new ClassicPort(this, "stepLevel", false, true);
        stepLevel.setTypeEquals(BaseType.INT);

        // bits IntState
        bits = new Parameter(this, "bits");
        bits.setExpression("8");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type double.
     */
    public ClassicPort input;

    /**
     * inStep of type double.
     */
    public ClassicPort inStep;

    /**
     * amplitude of type double.
     */
    public ClassicPort amplitude;

    /**
     * outStep of type double.
     */
    public ClassicPort outStep;

    /**
     * stepLevel of type int.
     */
    public ClassicPort stepLevel;

    /**
     *  bits parameter with initial value "8".
     */
     public Parameter bits;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generatePreinitializeCode() {

addInclude("<stdio.h>");
     }

    /**
     */
    public void  generateFireCode() {

addCode(adaptlinquant);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String adaptlinquant =
        "                double stepsize = (double) $ref(inStep);\n"
        + "                int numbits, twoPowerB, quantLevel;\n"
        + "                double high, low, in;\n"
        + "\n"
        + "                /* check the current value of the stepsize */\n"
        + "                if ( stepsize <= 0.0 ) {\n"
        + "                        fprintf(stderr, \"Error: Non-positive step size for AdaptLinQuant star \\n\");\n"
        + "                        exit(1);\n"
        + "                }\n"
        + "\n"
        + "                /* high threshold is ((2^B - 1)/2) * stepsize; low = -high\n"
        + "                   where B is the number of bits */\n"
        + "                numbits = (int) $val(bits);\n"
        + "                twoPowerB = (1 << numbits);\n"
        + "                high = ((double)(twoPowerB - 1)) * stepsize/ 2.0;\n"
        + "                low = -high;\n"
        + "\n"
        + "                /* compute the quantized output */\n"
        + "                    in = $ref(input);\n"
        + "                quantLevel = 0;\n"
        + "\n"
        + "                    if ( in >= high ) {\n"
        + "                        quantLevel = twoPowerB - 1;\n"
        + "                        $ref(amplitude) = high;\n"
        + "                }\n"
        + "                else if (in <= low) {\n"
        + "                        quantLevel = 0;\n"
        + "                        $ref(amplitude) = low;\n"
        + "                }\n"
        + "                else {\n"
        + "                        quantLevel = (int)((in-low)/stepsize + 0.5);\n"
        + "                        $ref(amplitude) = (double)(low + quantLevel*stepsize);\n"
        + "                }\n"
        + "\n"
        + "                $ref(outStep) = stepsize;\n"
        + "                $ref(stepLevel) = quantLevel;\n";
}
