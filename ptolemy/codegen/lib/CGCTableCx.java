/* TableCx, CGC domain: CGCTableCx.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCTableCx.pl by ptlang
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
//// CGCTableCx
/**
Implements a complex-valued lookup table.  The "values" state contains the
values to output; its first element is element zero.  An error occurs if
an out-of-bounds value is received.
<p>
<a name="table lookup"></a>

 @Author J. T. Buck
 @Version $Id$, based on version 1.3 of /users/ptolemy/src/domains/cgc/stars/CGCTableCx.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCTableCx extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCTableCx(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

        // \"table of values to output\" ComplexArrayState
        values = new Parameter(this, "values");
        values.setExpression("(-1,0) (1,0)");

        // \"If YES, check range of index at runtime, else omit\" IntState
        runTimeCheck = new Parameter(this, "runTimeCheck");
        runTimeCheck.setExpression("YES");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type int.
     */
    public ClassicPort input;

    /**
     * output of type complex.
     */
    public ClassicPort output;

    /**
     *  "table of values to output" parameter with initial value "(-1,0) (1,0)".
     */
     public Parameter values;

    /**
     *  "If YES, check range of index at runtime, else omit" parameter with initial value "YES".
     */
     public Parameter runTimeCheck;

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

addCode(readIdx);
                if (runTimeCheck) addCode(check);
                addCode(lookup);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String readIdx =
        "                int idx = $ref(input);\n";

    public String check =
        "                if (idx < 0 || idx >= $size(values)) {\n"
        + "                        fprintf(stderr, \"ERROR: input to CGCTable out of range\\n\");\n"
        + "                        exit(1);\n"
        + "                }\n";

    public String lookup =
        "                $ref(output).real = $ref2(values,idx).real;\n"
        + "                $ref(output).imag = $ref2(values,idx).imag;\n";
}
