/* SubInt, CGC domain: CGCSubInt.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSubInt.pl by ptlang
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
//// CGCSubInt
/**
   Output the "pos" input minus all "neg" inputs.

   @Author Brian L. Evans and Jose Luis Pino
   @Version $Id$, based on version 1.2 of /users/ptolemy/src/domains/cgc/stars/CGCSubInt.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSubInt extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSubInt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        pos = new ClassicPort(this, "pos", true, false);
        pos.setTypeEquals(BaseType.INT);
        neg = new ClassicPort(this, "neg", true, false);
        neg.setMultiport(true);
        neg.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        /*
          noInternalState();
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * pos of type int.
     */
    public ClassicPort pos;

    /**
     * neg of type int.
     */
    public ClassicPort neg;

    /**
     * output of type int.
     */
    public ClassicPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

        return neg.numberPorts() + 1;
    }

    /**
     */
    public void  generateFireCode() {

        addCode(startOp);
        for (int i = 1; i <= neg.numberPorts(); i++)
            addCode(doOp(i));
        addCode(saveResult);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String startOp =
    "        int diff = $ref(pos);\n";

    public String doOp (int i) {
        return
            "        diff -= $ref(neg#" + i + ");\n";
    }

    public String saveResult =
    "        $ref(output) = diff;\n";
}
