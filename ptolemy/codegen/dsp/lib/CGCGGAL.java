/* GGAL, CGC domain: CGCGGAL.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCGGAL.pl by ptlang
 */
/*
  Copyright (c) 1992-2005 The Regents of the University of California.
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
//// CGCGGAL
/**
   Ganged Gradient Adaptive Lattice filters.

   @Author T. M. Parks
   @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCGGAL.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCGGAL extends CGCGAL {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCGGAL(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        synthIn = new ClassicPort(this, "synthIn", true, false);
        synthIn.setTypeEquals(BaseType.DOUBLE);
        synthOut = new ClassicPort(this, "synthOut", false, true);
        synthOut.setTypeEquals(BaseType.DOUBLE);

        // Backward prediction error. FloatArrayState
        B = new Parameter(this, "B");
        B.setExpression("{0.0 0.0}");

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * synthIn of type double.
     */
    public ClassicPort synthIn;

    /**
     * synthOut of type double.
     */
    public ClassicPort synthOut;

    /**
     *  Backward prediction error. parameter with initial value "0.0 0.0".
     */
    public Parameter B;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generateInitializeCode() {
        //# line 57 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGGAL.pl"
        B.resize(order+1);
        CGCGAL::setup();
    }

    /**
     */
    public void  generateFireCode() {
        //# line 63 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGGAL.pl"
        addCode(main);
        CGCGAL::go();
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String main =
    "        {\n"
    + "            double F;\n"
    + "            int m;\n"
    + "\n"
    + "            F = $ref(synthIn);\n"
    + "            for (m = $val(order)-1; m >= 0; m--)\n"
    + "            {\n"
    + "                F += $ref(k,m+1) * $ref(B,m);\n"
    + "                $ref(B,m+1) = $ref(B,m) - $ref(k,m+1) * F;\n"
    + "            }\n"
    + "            $ref(B,0) = F;\n"
    + "            $ref(synthOut) = F;\n"
    + "        }\n";
}
