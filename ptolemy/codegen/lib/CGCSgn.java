/* Sgn, CGC domain: CGCSgn.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSgn.pl by ptlang
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
//// CGCSgn
/**
   This star computes the signum of its input.
   The output is +/- 1.
   Note that 0.0 maps to 1.
   <p>
   <a name="signum"></a>

   @Author E. A. Lee
   @Version $Id$, based on version 1.3 of /users/ptolemy/src/domains/cgc/stars/CGCSgn.pl, from Ptolemy Classic 
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSgn extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSgn(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        /* 
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

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateFireCode() {
        
        addCode(std); 
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String std = 
    "	  double x;\n"
    + "	  int sgn;\n"
    + "	  x = $ref(input);\n"
    + "	  sgn = (x >= 0.0) ? 1 : -1;\n"
    + "	  $ref(output) = sgn;\n";
}
