/* Reverse, CGC domain: CGCReverse.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCReverse.pl by ptlang
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
//// CGCReverse
/**
On each execution, reads a block of "N" samples (default 64)
and writes them out backwards.

 @Author S. Ha
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCReverse.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCReverse extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCReverse(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        output = new ClassicPort(this, "output", false, true);

        // Number of particles read and written. IntState
        N = new Parameter(this, "N");
        N.setExpression("64");

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
     *  Number of particles read and written. parameter with initial value "64".
     */
     public Parameter N;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

return ((IntToken)((N).getToken())).intValue();
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

input.setSDFParams(((IntToken)((N).getToken())).intValue(),((IntToken)((N).getToken())).intValue()-1);
                output.setSDFParams(((IntToken)((N).getToken())).intValue(),((IntToken)((N).getToken())).intValue()-1);
     }

    /**
     */
    public void  generateFireCode() {

addCode(out);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String out =
        "        int i, j;\n"
        + "        for (i = 0, j = $val(N) - 1; j >= 0; i++, j--)\n"
        + "                $ref(output,i) = $ref(input,j);\n";
}
