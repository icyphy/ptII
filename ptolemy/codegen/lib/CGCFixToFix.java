/* FixToFix, CGC domain: CGCFixToFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCFixToFix.pl by ptlang
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
//// CGCFixToFix
/**
Convert a fixed-point value into one with a different precision.

 @Author J.Weiss
 @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCFixToFix.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCFixToFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCFixToFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.FIX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.FIX);

        // Precision of the output in bits or empty if unspecified.\nIf the input value cannot be represented by the number of bits specified in\nthe precision parameter, then the output is set to its maximum value (or\nminimum for negative magnitudes). PrecisionState
        OutputPrecision = new Parameter(this, "OutputPrecision");
        OutputPrecision.setExpression("");

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
Output fix type
     */
    public ClassicPort output;

    /**
     *  Precision of the output in bits or empty if unspecified.
If the input value cannot be represented by the number of bits specified in
the precision parameter, then the output is set to its maximum value (or
minimum for negative magnitudes). parameter with initial value "".
     */
     public Parameter OutputPrecision;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

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

		// do the assignment
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	FIX_Assign($ref(output),$ref(input));\n"

); 	 addCode(_str_);  } 

		// insert code to test overflow flag
		super.checkOverflow();
     }
}
