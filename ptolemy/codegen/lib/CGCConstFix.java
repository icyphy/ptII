/* ConstFix, CGC domain: CGCConstFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCConstFix.pl by ptlang
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
//// CGCConstFix
/**
Output a fixed-point constant output with value "level" (default 0.0).
<p>
The "OutputPrecision" is specified using an "l.r" format, where
"l" is the number of bits to the left of the decimal place
(including the sign bit) and "r" is the number of bits to the
right of the sign bit.
For example, the precision "2.22" would represent a 24-bit fixed-point
number with 1 sign bit, 1 integer bit, and 22 fractional bits.

 @Author Y. K. Lim
 @Version $Id$, based on version 1.2 of /users/ptolemy/src/domains/cgc/stars/CGCConstFix.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCConstFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCConstFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.FIX);

        // The constant value. FixState
        level = new Parameter(this, "level");
        level.setExpression("0.0");

        // Precision of the output in bits. PrecisionState
        OutputPrecision = new Parameter(this, "OutputPrecision");
        OutputPrecision.setExpression("2.14");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type fix.
     */
    public ClassicPort output;

    /**
     *  The constant value. parameter with initial value "0.0".
     */
     public Parameter level;

    /**
     *  Precision of the output in bits. parameter with initial value "2.14".
     */
     public Parameter OutputPrecision;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

super.generateInitializeCode();

		output.setPrecision(OutputPrecision);
     }

    /**
     */
    public void  generateFireCode() {

super.clearOverflow();
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"               FIX_Assign($ref(output), $ref(level));\n"

); 	 addCode(_str_);  }
                super.checkOverflow();
     }
}
