/* Ramp, CGC domain: CGCRamp.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCRamp.pl by ptlang
 */
/*
  Copyright (c) 1990-1996 The Regents of the University of California.
  All rights reserved.
  See the file $PTOLEMY/copyright for copyright notice,
  limitation of liability, and disclaimer of warranty provisions.
*/
package ptolemy.codegen.lib;

import ptolemy.codegen.kernel.ClassicCGCActor;
import ptolemy.codegen.kernel.ClassicPort;
import ptolemy.codegen.kernel.CodeGeneratingActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCRamp
/**
   Generates a ramp signal, starting at "value" (default 0)
   with step size "step" (default 1).

   @Author E. A. Lee
   @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/CGCRamp.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCRamp extends ClassicCGCActor implements CodeGeneratingActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCRamp(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Increment from one sample to the next. FloatState
        step = new Parameter(this, "step");
        step.setExpression("1.0");

        // Initial (or latest) value output by Ramp. FloatState
        value = new Parameter(this, "value");
        value.setExpression("0.0");

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  Increment from one sample to the next. parameter with initial value "1.0".
     */
    public Parameter step;

    /**
     *  Initial (or latest) value output by Ramp. parameter with initial value "0.0".
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

        return 2;
    }

    /**
     */
    public void  generateFireCode() {

        addCode(std);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String std =
    "        $ref(output) = $ref(value);\n"
    + "        $ref(value) += $val(step);\n";
}
