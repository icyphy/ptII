/* IntToFix, CGC domain: CGCIntToFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCIntToFix.pl by ptlang
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
//// CGCIntToFix
/**
Convert a floating-point input to an fixed-point output.
<p>
This star converts a int value to a fix value with the specified precision.
If the output precision is not specified, the precision is determined at
runtime according to the incoming int value.

 @Author J.Weiss
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCIntToFix.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCIntToFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCIntToFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.FIX);

        // Precision of the output in bits or empty if unspecified.\nIf the value of the double cannot be represented by the number of bits\nspecified in the precision parameter, then the output is set to its maximum\nvalue (or minimum for negative magnitudes). PrecisionState
        OutputPrecision = new Parameter(this, "OutputPrecision");
        OutputPrecision.setExpression("");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
Input int type
     */
    public ClassicPort input;

    /**
Output fix type
     */
    public ClassicPort output;

    /**
     *  Precision of the output in bits or empty if unspecified.
If the value of the double cannot be represented by the number of bits
specified in the precision parameter, then the output is set to its maximum
value (or minimum for negative magnitudes). parameter with initial value "".
     */
     public Parameter OutputPrecision;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  begin() {

// if the precision for the output port is not defined
                // - neither by this nor the successor star -, the actual
                // precision is determined at runtime

                if (!output.precision().isValid())
                        output.setAttributes(A_VARPREC);
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

super.generateInitializeCode();
                output.setPrecision(OutputPrecision);
     }

    /**
     */
    public void  generateFireCode() {

// insert code to clear overflow flag
                super.clearOverflow();

                if (output.attributes() & AB_VARPREC)
                     addCode(setprec);

                addCode(assign);

                // insert code to test overflow flag
                super.checkOverflow();
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String setprec =
        "                FIX_SetPrecisionFromDouble($precision(output),(double)((int)$ref(input)));\n";

    public String assign =
        "                FIX_DoubleAssign($ref(output),(double)((int)$ref(input)));\n";
}
