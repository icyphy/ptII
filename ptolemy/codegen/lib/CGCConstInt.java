/* ConstInt, CGC domain: CGCConstInt.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCConstInt.pl by ptlang
 */
/*
  Copyright (c) 1990-1996 The Regents of the University of California.
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
//// CGCConstInt
/**
   Output a constant signal with value level (default 0.0).

   @Author S. Ha
   @Version $Id$, based on version 1.1 of /users/ptolemy/src/domains/cgc/stars/CGCConstInt.pl, from Ptolemy Classic 
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCConstInt extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCConstInt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // The constant value. FloatState
        level = new Parameter(this, "level");
        level.setExpression("0");

        /*     //# line 25 "/users/ptolemy/src/domains/cgc/stars/CGCConstInt.pl"
               noInternalState();
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type int.
     */
    public ClassicPort output;

    /**
     *  The constant value. parameter with initial value "0".
     */
    public Parameter level;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        //# line 31 "/users/ptolemy/src/domains/cgc/stars/CGCConstInt.pl"
        return 0;
    }

    /**
     */
    public void  generateFireCode() {
        //# line 28 "/users/ptolemy/src/domains/cgc/stars/CGCConstInt.pl"
        addCode("\t$ref(output) = $val(level);\n");
    }
}
