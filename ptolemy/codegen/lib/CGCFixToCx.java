/* FixToCx, CGC domain: CGCFixToCx.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCFixToCx.pl by ptlang
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
//// CGCFixToCx
/**
Convert a fixed-point input to a complex output.

 @Author J. Weiss
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCFixToCx.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCFixToCx extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCFixToCx(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.FIX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

        // number of samples to be copied IntState
        numSample = new Parameter(this, "numSample");
        numSample.setExpression("1");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
Input fix type
     */
    public ClassicPort input;

    /**
Output complex type
     */
    public ClassicPort output;

    /**
     *  number of samples to be copied parameter with initial value "1".
     */
     public Parameter numSample;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if (((IntToken)((numSample).getToken())).intValue() <= 0) {
                        throw new IllegalActionException(this, "numSample must be positive");
                        return;
                }
                input.setSDFParams(((IntToken)((numSample).getToken())).intValue());
                output.setSDFParams(((IntToken)((numSample).getToken())).intValue());
     }

    /**
     */
    public void  generateFireCode() {

addCode(body);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String body =
        "                int i = 0;\n"
        + "                for (; i < $val(numSample); i++) {\n"
        + "                        $ref(output,i).real = FIX_Fix2Double($ref(input,i));\n"
        + "                        $ref(output,i).imag = 0;\n"
        + "                }\n";
}
