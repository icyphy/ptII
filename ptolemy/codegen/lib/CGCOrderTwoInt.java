/* OrderTwoInt, CGC domain: CGCOrderTwoInt.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCOrderTwoInt.pl by ptlang
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
//// CGCOrderTwoInt
/**
   Takes two inputs and outputs the greater and lesser of the two integers.

   @Author Brian L. Evans
   @Version $Id$, based on version 1.1 of /users/ptolemy/src/domains/cgc/stars/CGCOrderTwoInt.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCOrderTwoInt extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCOrderTwoInt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        upper = new ClassicPort(this, "upper", true, false);
        upper.setTypeEquals(BaseType.INT);
        lower = new ClassicPort(this, "lower", true, false);
        lower.setTypeEquals(BaseType.INT);
        greater = new ClassicPort(this, "greater", false, true);
        greater.setTypeEquals(BaseType.INT);
        lesser = new ClassicPort(this, "lesser", false, true);
        lesser.setTypeEquals(BaseType.INT);

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * upper of type int.
     */
    public ClassicPort upper;

    /**
     * lower of type int.
     */
    public ClassicPort lower;

    /**
     * greater of type int.
     */
    public ClassicPort greater;

    /**
     * lesser of type int.
     */
    public ClassicPort lesser;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generateFireCode() {

        addCode(ordertwoint);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String ordertwoint =
    "                int greaterValue = (int)$ref(upper);\n"
    + "                int lesserValue = (int) $ref(lower);\n"
    + "\n"
    + "                if ( greaterValue < lesserValue ) {\n"
    + "                    int swap = greaterValue;\n"
    + "                    greaterValue = lesserValue;\n"
    + "                    lesserValue = swap;\n"
    + "                }\n"
    + "\n"
    + "                $ref(greater) = greaterValue;\n"
    + "                $ref(lesser) = lesserValue;\n";
}
