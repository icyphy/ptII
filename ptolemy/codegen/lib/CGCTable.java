/* Table, CGC domain: CGCTable.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCTable.pl by ptlang
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
//// CGCTable
/**
Implements a real-valued lookup table.  The "values" state contains the
values to output; its first element is element zero.  An error occurs if
an out of bounds value is received.
<p>
<a name="table lookup"></a>

 @Author Joseph T. Buck
 @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCTable.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCTable extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCTable(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // \"table of values to output\" FloatArrayState
        values = new Parameter(this, "values");
        values.setExpression("{-1 1}");

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
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  "table of values to output" parameter with initial value "-1 1".
     */
     public Parameter values;

    /**
     *  "If YES, check range of index at runtime, else omit" parameter with initial value "YES".
     */
     public Parameter runTimeCheck;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generatePreinitializeCode() {

addInclude("<stdio.h>");
     }

    /**
     */
    public void  generateFireCode() {

addCode(readIdx);
                if (((IntToken)((runTimeCheck).getToken())).intValue() == 1) // FIXME runTimeCheck should be a Boolean addCode(check);
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
        "                $ref(output) = $ref2(values,idx);\n";
}
