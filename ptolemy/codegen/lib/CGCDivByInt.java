/* DivByInt, CGC domain: CGCDivByInt.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCDivByInt.pl by ptlang
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
//// CGCDivByInt
/**
This is an amplifier.  The integer output is the integer input
divided by the integer "divisor" (default 1).  Truncated integer
division is used.

 @Author Brian L. Evans
 @Version $Id$, based on version 1.2 of /users/ptolemy/src/domains/cgc/stars/CGCDivByInt.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCDivByInt extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCDivByInt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // Inverse of the gain of the amplifier. IntState
        divisor = new Parameter(this, "divisor");
        divisor.setExpression("2");

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
     *  Inverse of the gain of the amplifier. parameter with initial value "2".
     */
     public Parameter divisor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if ( ((IntToken)((divisor).getToken())).intValue() == 0 ) {
                     throw new IllegalActionException(this, "divisor cannot be zero");
                }
     }

    /**
     */
    public void  generateFireCode() {

addCode(divide);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String divide =
        "        $ref(output) = $ref(input) / $val(divisor);\n";
}
