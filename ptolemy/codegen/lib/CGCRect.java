/* Rect, CGC domain: CGCRect.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCRect.pl by ptlang
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
//// CGCRect
/**
   Generates a rectangular pulse of height "height" (default 1.0).
   and width "width" (default 8).  If "period" is greater than zero,
   then the pulse is repeated with the given period.

   @Author Kennard White Contributor(s): SDF version by J. T. Buck
   @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/stars/CGCRect.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCRect extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCRect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Height of the rectangular pulse. FloatState
        height = new Parameter(this, "height");
        height.setExpression("1.0");

        // Width of the rectangular pulse. IntState
        width = new Parameter(this, "width");
        width.setExpression("8");

        // If greater than zero, the period of the pulse stream. IntState
        period = new Parameter(this, "period");
        period.setExpression("0");

        // Internal counting state. IntState
        count = new Parameter(this, "count");
        count.setExpression("0");

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  Height of the rectangular pulse. parameter with initial value "1.0".
     */
    public Parameter height;

    /**
     *  Width of the rectangular pulse. parameter with initial value "8".
     */
    public Parameter width;

    /**
     *  If greater than zero, the period of the pulse stream. parameter with initial value "0".
     */
    public Parameter period;

    /**
     *  Internal counting state. parameter with initial value "0".
     */
    public Parameter count;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generateFireCode() {

        addCode(cbMain);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String cbMain =
    "                $ref(output) = ($ref(count) < $val(width)) ? $val(height) : 0.0;\n"
    + "                ++$ref(count);\n"
    + "                if ( $val(period) > 0 && $ref(count) >= $val(period) )\n"
    + "                    $ref(count) = 0;\n";
}
