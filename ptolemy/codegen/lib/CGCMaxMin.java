/* MaxMin, CGC domain: CGCMaxMin.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCMaxMin.pl by ptlang
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
//// CGCMaxMin
/**
Finds maximum or minimum, value or magnitude.
<p>
If <i>cmpareMagnitude</i> is "no", the star finds from among
the <i>input</i> inputs the one with the maximum or minimum value;
otherwise, it finds from among the <i>input</i> inputs the one with
the maximum or minimum magnitude.
if <i>outputMagnitude</i> is "yes", the magnitude of the result is
written to the output, else the result itself is written to the output.
Returns maximum value among N (default 10) samples.
Also, the index of the output is provided (count starts at 0).
<p>
This star is based on the MaxMin star in the CG56 domain.

 @Author Brian L. Evans Contributor(s): Chih-Tsung Huang
 @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCMaxMin.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCMaxMin extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCMaxMin(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
        index = new ClassicPort(this, "index", false, true);
        index.setTypeEquals(BaseType.INT);

        // default samples IntState
        N = new Parameter(this, "N");
        N.setExpression("10");

        // output maximum value else minimum is the output IntState
        MAX = new Parameter(this, "MAX");
        MAX.setExpression("YES");

        // default is not to compare magnitude IntState
        compareMagnitude = new Parameter(this, "compareMagnitude");
        compareMagnitude.setExpression("NO");

        // default is not to output magnitude IntState
        outputMagnitude = new Parameter(this, "outputMagnitude");
        outputMagnitude.setExpression("NO");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type double.
     */
    public ClassicPort input;

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     * index of type int.
     */
    public ClassicPort index;

    /**
     *  default samples parameter with initial value "10".
     */
     public Parameter N;

    /**
     *  output maximum value else minimum is the output parameter with initial value "YES".
     */
     public Parameter MAX;

    /**
     *  default is not to compare magnitude parameter with initial value "NO".
     */
     public Parameter compareMagnitude;

    /**
     *  default is not to output magnitude parameter with initial value "NO".
     */
     public Parameter outputMagnitude;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if ( ((IntToken)((N).getToken())).intValue() <= 0 ) {
                    throw new IllegalActionException(this,
                                    "Number of samples, N, must be positive.");
                    return;
                }
                input.setSDFParams(((IntToken)((N).getToken())).intValue(), ((IntToken)((N).getToken())).intValue()-1);
     }

    /**
     */
    public void  generateFireCode() {

addCode(macros);
                addCode(decl);
                addCode(initData);
                addCode(compareData);
                addCode(outputValue);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String macros =
        "#define FABS(a)        ( ((a) > 0.0) ? (a) : -(a) )\n";

    public String decl =
        "        double current, currentCmp, value, valueCmp;\n"
        + "        int i, minChangeFlag, valueIndex;\n"
        + "        int cmpMagFlag, maxflag;\n";

    public String initData =
        "        cmpMagFlag = $val(compareMagnitude);\n"
        + "        maxflag = $val(MAX);\n"
        + "        i = $val(N) - 1;\n"
        + "        value = $ref(input,i);\n"
        + "        valueCmp = cmpMagFlag ? FABS(value) : value;\n"
        + "        valueIndex = i;\n";

    public String compareData =
        "        while ( i-- > 0 ) {\n"
        + "                current = $ref(input,i);\n"
        + "                currentCmp = cmpMagFlag ? FABS(current) : current;\n"
        + "                minChangeFlag = ( currentCmp < valueCmp );\n"
        + "\n"
        + "                /* Logical exclusive OR between maxflag and minChangeFlag\n"
        + "                   but we cannot use the bitwise xor ^ */\n"
        + "                if ( (maxflag && !minChangeFlag) ||\n"
        + "                     (!maxflag && minChangeFlag) ) {\n"
        + "                        value = current;\n"
        + "                        valueCmp = currentCmp;\n"
        + "                        valueIndex = i;\n"
        + "                }\n"
        + "        }\n";

    public String outputValue =
        "        /* Output the value or the magnitude of the value */\n"
        + "        if ( $val(outputMagnitude) ) {\n"
        + "                if ( value < 0.0 ) value = -value;\n"
        + "        }\n"
        + "\n"
        + "        /* Send the maximum/minimum value to the output port */\n"
        + "        $ref(output) = value;\n"
        + "\n"
        + "        /* Adjust the index due to the LIFO nature of input data */\n"
        + "        $ref(index) = $val(N) - valueIndex - 1;\n";
}
