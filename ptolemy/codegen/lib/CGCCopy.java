/* Copy, CGC domain: CGCCopy.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCCopy.pl by ptlang
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
//// CGCCopy
/**
This star is required by the default target for the CGC domain which is
implemented by the CGCTarget class.  The CGCTarget class files say that
"'Copy' stars are added if an input/output PortHole is a host/embedded
PortHole and the buffer size is greater than the number of Particles
transferred."

 @Author Soonhoi Ha
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCCopy.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCCopy extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCCopy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        output = new ClassicPort(this, "output", false, true);

        // number of samples to be copied IntState
        numSample = new Parameter(this, "numSample");
        numSample.setExpression("1");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type anytype.
     */
    public ClassicPort input;

    /**
     * output of type anytype.
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
    public int  myExecTime() {

if (input.resolvedType() == COMPLEX)
                        return 2 * ((IntToken)((numSample).getToken())).intValue();
                else
                        return ((IntToken)((numSample).getToken())).intValue();
     }

    /**
     */
    public void  generatePreinitializeCode() {

numSample = output.numXfer();
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if (((IntToken)((numSample).getToken())).intValue() > 1) {
                        input.setSDFParams(((IntToken)((numSample).getToken())).intValue());
                        output.setSDFParams(((IntToken)((numSample).getToken())).intValue());
                }
     }

    /**
     */
    public void  generateFireCode() {

if (input.resolvedType() == COMPLEX)
                        addCode(complexBody);
                else
                        addCode(floatBody);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String floatBody =
        "        int i;\n"
        + "        for (i = 0; i < $val(numSample); i++) {\n"
        + "                $ref(output, i) = $ref(input, i);\n"
        + "        }\n";

    public String complexBody =
        "        int i;\n"
        + "        for (i = 0; i < $val(numSample); i++) {\n"
        + "                $ref(output, i).real = $ref(input, i).real;\n"
        + "                $ref(output, i).imag = $ref(input, i).imag;\n"
        + "        }\n";
}
