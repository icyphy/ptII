/* ModuloInt, CGC domain: CGCModuloInt.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCModuloInt.pl by ptlang
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
//// CGCModuloInt
/**
   The output of this star is the input modulo the
   "modulo" parameter. The input, output, and modulo
   are all integers.

   @Author Siamak Modjtahedi
   @Version $Id$, based on version 1.2 of /users/ptolemy/src/domains/cgc/stars/CGCModuloInt.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCModuloInt extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCModuloInt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // The modulo parameter IntState
        modulo = new Parameter(this, "modulo");
        modulo.setExpression("1");

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
     * output of type int.
     */
    public ClassicPort output;

    /**
     *  The modulo parameter parameter with initial value "1".
     */
    public Parameter modulo;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

        return 1;
    }

    /**
     */
    public void  generatePreinitializeCode() {

        addInclude("<math.h>");
    }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

        if ( ((IntToken)((modulo).getToken())).intValue() == 0 ) {
            throw new IllegalActionException(this,
                    "The modulo parameter cannot be zero");
            return;
        }
    }

    /**
     */
    public void  generateFireCode() {

        addCode(out);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String out =
    "        $ref(output) = $ref(input) % $ref(modulo);\n";
}
