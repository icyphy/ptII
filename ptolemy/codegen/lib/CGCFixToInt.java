/* FixToInt, CGC domain: CGCFixToInt.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCFixToInt.pl by ptlang
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
//// CGCFixToInt
/**
Convert a fixed-point input to an integer output.

 @Author Yu Kee Lim
 @Version $Id$, based on version 1.2 of /users/ptolemy/src/domains/cgc/stars/CGCFixToInt.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCFixToInt extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCFixToInt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.FIX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
Input fix type
     */
    public ClassicPort input;

    /**
Output int type
     */
    public ClassicPort output;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateFireCode() {

addCode(fixtoint);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String fixtoint =
        "	        $ref(output) = FIX_Fix2Int($ref(input));\n";
}
