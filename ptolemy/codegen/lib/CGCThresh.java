/* Thresh, CGC domain: CGCThresh.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCThresh.pl by ptlang
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
//// CGCThresh
/**
Compares input values to "threshold" (default 0.5).  Output is 0
if input is greater than or equal to threshold; otherwise, it is 1.

 @Author J. Buck
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCThresh.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCThresh extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCThresh(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // Threshold applied to input. FloatState
        threshold = new Parameter(this, "threshold");
        threshold.setExpression("0.5");

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
     * output of type int.
     */
    public ClassicPort output;

    /**
     *  Threshold applied to input. parameter with initial value "0.5".
     */
     public Parameter threshold;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateFireCode() {

addCode(gen);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String gen =
        "		$ref(output) = ($ref(input) > $val(threshold)) ? 1 : 0;\n";
}
