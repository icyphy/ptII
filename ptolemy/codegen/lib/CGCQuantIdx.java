/* QuantIdx, CGC domain: CGCQuantIdx.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCQuantIdx.pl by ptlang
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
//// CGCQuantIdx
/**
   Quantize the input value to one of N+1 possible output levels using
   N thresholds, and output both the quantized result and the
   quantization level.  See the Quant star for more information.

   @Author Edward A. Lee, Joseph T. Buck, and Brian L. Evans
   @Version $Id$, based on version 1.1 of /users/ptolemy/src/domains/cgc/stars/CGCQuantIdx.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCQuantIdx extends CGCQuant {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCQuantIdx(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        stepNumber = new ClassicPort(this, "stepNumber", false, true);
        stepNumber.setTypeEquals(BaseType.INT);

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
       Level of the quantization from 0 to N-1
    */
    public ClassicPort stepNumber;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generateFireCode() {

        CGCQuant.go();
        addCode(writeStep);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String writeStep =
    "        $ref(stepNumber) = mid;\n";
}
