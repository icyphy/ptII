/* AddCx, CGC domain: CGCAddCx.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCAddCx.pl by ptlang
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
//// CGCAddCx
/**
Output the complex product of the inputs.

 @Author Jose Luis Pino
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCAddCx.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCAddCx extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCAddCx(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.COMPLEX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type complex.
     */
    public ClassicPort input;

    /**
     * output of type complex.
     */
    public ClassicPort output;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return 2*input.numberPorts();
     }

    /**
     */
    public void  generateFireCode() {

addCode(startOp);
        int i;
        for (i=2;i<=input.numberPorts();i++)
            addCode(doOp(i));
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String startOp =
        "        $ref(output).real = $ref(input#1).real;\n"
        + "        $ref(output).imag = $ref(input#1).imag;\n";

    public String doOp (int i) {
        return
        "        $ref(output).real += $ref(input#" + i + ").real;\n"
        + "        $ref(output).imag += $ref(input#" + i + ").imag;\n";
    }
}
