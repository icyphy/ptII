/* Reciprocal, CGC domain: CGCReciprocal.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCReciprocal.pl by ptlang
*/
/*
Copyright (c) 1990-1997 The Regents of the University of California.
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
//// CGCReciprocal
/**
1/x, with an optional magnitude limit.
<p>
This star computes 1<i>/x</i>, where <i>x</i> is the input.
If the <i>magLimit</i> parameter is not 0.0, then the output is
&#177;max(<i>magLimit, </i>1<i>/x </i>).  In this case, <i>x</i> can be zero
without causing an floating exception.
The sign of the output is determined by the sign of the input.

 @Author E. A. Lee
 @Version $Id$, based on version 1.10 of /users/ptolemy/src/domains/cgc/stars/CGCReciprocal.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCReciprocal extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCReciprocal(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // If non-zero, limits the magnitude of the output. FloatState
        magLimit = new Parameter(this, "magLimit");
        magLimit.setExpression("0.0");

/*
// Indicate that there is no dynamically changing internal
                // state, so the star can be parallelized.
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
     *  If non-zero, limits the magnitude of the output. parameter with initial value "0.0".
     */
     public Parameter magLimit;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

/* based on CG96Reciprocal */
                if (((DoubleToken)((magLimit).getToken())).doubleValue() == 0.0) return 8;
                else return 12;
     }

    /**
     */
    public void  generateFireCode() {

if (((DoubleToken)((magLimit).getToken())).doubleValue() == 0.0)
                addCode(reciprocal);
            else
                addCode(satrec);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String reciprocal =
        "        $ref(output) = 1/$ref(input);\n";

    public String satrec =
        "        if ($ref(input) == 0.0)\n"
        + "            $ref(output) = $val(magLimit);\n"
        + "        else {\n"
        + "            double t;\n"
        + "            t = 1/$ref(input);\n"
        + "            if (t>$val(magLimit))\n"
        + "                $ref(output) = $val(magLimit);\n"
        + "            else if (t < - $val(magLimit))\n"
        + "                $ref(output) = - $val(magLimit);\n"
        + "            else\n"
        + "                $ref(output) = t;\n"
        + "        }\n";
}
