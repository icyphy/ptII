/* IntToBits, CGC domain: CGCIntToBits.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCIntToBits.pl by ptlang
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
//// CGCIntToBits
/**
   Read the least significant nBits bits from an integer input,
   and output the bits as integers serially on the output,
   most significant bit first.
   <p>


   @Author Jose Luis Pino
   @Version $Id$, based on version 1.8 of /users/ptolemy/src/domains/cgc/stars/CGCIntToBits.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCIntToBits extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCIntToBits(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // Number of bits to place in one word IntState
        nBits = new Parameter(this, "nBits");
        nBits.setExpression("4");

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
     *  Number of bits to place in one word parameter with initial value "4".
     */
    public Parameter nBits;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

        return ((IntToken)((nBits).getToken())).intValue()*2;
    }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

        /* Need (int) cast on sizeof to eliminate gcc warning */
        if (((IntToken)((nBits).getToken())).intValue() > int(sizeof(int)*8)) {
            StringBuffer message = new StringBuffer("nBits needs to be less than ");
            message.append(/*(unsigned int)(sizeof(int)*/ ( 16 *8));
            throw new IllegalActionException(this, message);
            return;
        }
        if (((IntToken)((nBits).getToken())).intValue() < 0) {
            throw new IllegalActionException(this, "nBits must be positive");
            return;
        }
        output.setSDFParams(((IntToken)((nBits).getToken())).intValue(),((IntToken)((nBits).getToken())).intValue()-1);
    }

    /**
     */
    public void  generateFireCode() {

        addCode(readNwrite);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String readNwrite =
    "        int word;\n"
    + "        int i = 0;\n"
    + "        word = $ref(input);\n"
    + "        for (; i < $val(nBits); i++) {\n"
    + "             $ref(output,i) = (word & 1);\n"
    + "            word >>= 1;\n"
    + "        }\n";
}
