/* LinQuantIdx, CGC domain: CGCLinQuantIdx.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCLinQuantIdx.pl by ptlang
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
//// CGCLinQuantIdx
/**
The input is quantized to the number of levels given by the "levels"
parameter plus 1.  The quantization levels are uniformly spaced between "low"
and "high" inclusive.  Rounding down is performed, so that output level
will equal "high" only if the input level equals or exceeds "high".
If the input is below "low", then the quantized output will equal "low".
The quantized value is output to the "amplitude" port, while the index
of the quantization level is output to the "stepNumber" port.  This
integer output is useful for stars that need an integer input, such as
Thor stars.

 @Author Asawaree Kalavade
 @Version $Id$, based on version 1.1 of /users/ptolemy/src/domains/cgc/stars/CGCLinQuantIdx.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCLinQuantIdx extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCLinQuantIdx(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        amplitude = new ClassicPort(this, "amplitude", false, true);
        amplitude.setTypeEquals(BaseType.DOUBLE);
        stepNumber = new ClassicPort(this, "stepNumber", false, true);
        stepNumber.setTypeEquals(BaseType.INT);

        // number of levels to quantize to IntState
        levels = new Parameter(this, "levels");
        levels.setExpression("128");

        // lower limit of signal excursion FloatState
        low = new Parameter(this, "low");
        low.setExpression("-3.0");

        // upper limit of signal excursion FloatState
        high = new Parameter(this, "high");
        high.setExpression("3.0");

        // height of each quantization step, which is determined by the states\nhigh, low, and levels. FloatState
        height = new Parameter(this, "height");
        height.setExpression("1.0");

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
     * amplitude of type double.
     */
    public ClassicPort amplitude;

    /**
     * stepNumber of type int.
     */
    public ClassicPort stepNumber;

    /**
     *  number of levels to quantize to parameter with initial value "128".
     */
     public Parameter levels;

    /**
     *  lower limit of signal excursion parameter with initial value "-3.0".
     */
     public Parameter low;

    /**
     *  upper limit of signal excursion parameter with initial value "3.0".
     */
     public Parameter high;

    /**
     *  height of each quantization step, which is determined by the states
high, low, and levels. parameter with initial value "1.0".
     */
     public Parameter height;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if (((IntToken)((levels).getToken())).intValue() <= 0 ) {
                    throw new IllegalActionException(this, "levels must be positive");
                }
                else if (((DoubleToken)((high).getToken())).doubleValue() <= ((DoubleToken)((low).getToken())).doubleValue()) {
                    throw new IllegalActionException(this,
                                    "quantization range incorrectly ",
                                    "specified: high <= low");
                }
                else {
                    height = (((DoubleToken)((high).getToken())).doubleValue() - ((DoubleToken)((low).getToken())).doubleValue())/(((IntToken)((levels).getToken())).intValue() - 0);
                }
     }

    /**
     */
    public void  generateFireCode() {

addCode(linquantidx);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String linquantidx =
        "                    double in = (double)$ref(input);\n"
        + "                double highvalue = (double)$val(high);\n"
        + "                double lowvalue = (double)$val(low);\n"
        + "\n"
        + "                    if ( in >= highvalue ) {\n"
        + "                    $ref(amplitude) = highvalue;\n"
        + "                    $ref(stepNumber) << (int)$val(levels) - 1;\n"
        + "                }\n"
        + "                else if ( in <= lowvalue ) {\n"
        + "                    $ref(amplitude) = lowvalue;\n"
        + "                    $ref(stepNumber) = 0;\n"
        + "                }\n"
        + "                else {\n"
        + "                    int step = (int)((in - lowvalue)/((double)$val(height)));\n"
        + "                    $ref(stepNumber) = step;\n"
        + "                    $ref(amplitude) = (double)(lowvalue + step * ((double)$val(height)));\n"
        + "                }\n";
}
