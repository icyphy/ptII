/* Exp, CGC domain: CGCExp.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCExp.pl by ptlang
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
//// CGCExp
/**
Outputs the exponential function of the input.
<p>
<b>Bugs</b>: Overflow is not handled well.
<a name="exponential"></a>

 @Author E. A. Lee
 @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/CGCExp.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCExp extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCExp(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

return 33;  /* use the same value as log */
     }

    /**
     */
    public void  generatePreinitializeCode() {

addInclude("<math.h>");
     }

    /**
     */
    public void  generateFireCode() {

addCode(exp);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String exp =
        "        $ref(output) = exp($ref(input));\n";
}
