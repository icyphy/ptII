/* Limit, CGC domain: CGCLimit.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCLimit.pl by ptlang
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
//// CGCLimit
/**
   Hard limiter.
   <p>
   This star hard limits input samples to keep the in the range
   of (<i>bottom, top</i>).
   <a name="hard limiter"></a>

   @Author Kennard White Contributor(s): SDF version by E. A. Lee
   @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCLimit.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCLimit extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCLimit(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Lower limit of the output. FloatState
        bottom = new Parameter(this, "bottom");
        bottom.setExpression("0.0");

        // Upper limit of the output. FloatState
        top = new Parameter(this, "top");
        top.setExpression("1.0");

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
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  Lower limit of the output. parameter with initial value "0.0".
     */
    public Parameter bottom;

    /**
     *  Upper limit of the output. parameter with initial value "1.0".
     */
    public Parameter top;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generateFireCode() {

        addCode(cbBody);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String cbBody =
    "            /*IF*/ if ( $ref(input) > $val(top) ) {\n"
    + "                $ref(output) = $val(top);\n"
    + "            } else if ( $ref(input) < $val(bottom) ) {\n"
    + "                $ref(output) = $val(bottom);\n"
    + "            } else {\n"
    + "                $ref(output) = $ref(input);\n"
    + "            }\n";
}
