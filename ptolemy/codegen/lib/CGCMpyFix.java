/* MpyFix, CGC domain: CGCMpyFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCMpyFix.pl by ptlang
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
//// CGCMpyFix
/**
Output the product of the inputs, as a fixed-point value.

 @Author Jyergen Weiss
 @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/stars/CGCMpyFix.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCMpyFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCMpyFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.FIX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.FIX);

        // Indicates whether or not to keep the precision of the arriving particles\nas they are:  YES keeps the same precision, and NO casts the inputs\nto the precision specified by the parameter \"InputPrecision\". IntState
        ArrivingPrecision = new Parameter(this, "ArrivingPrecision");
        ArrivingPrecision.setExpression("YES");

        // Sets the precision of the input in bits.\nThe input particles are only cast to this precision if the\nparameter \"ArrivingPrecision\" is set to NO. PrecisionState
        InputPrecision = new Parameter(this, "InputPrecision");
        InputPrecision.setExpression("2.14");

        // Sets the precision of the output in bits.\nThis is the precision that will hold the result of the product of the inputs.\nWhen the value of the product extends outside of the precision,\nthe output is set to its maximum value (or minimum for negative magnitudes). PrecisionState
        OutputPrecision = new Parameter(this, "OutputPrecision");
        OutputPrecision.setExpression("2.14");

        // index for multiple input trace IntState
        index = new Parameter(this, "index");
        index.setExpression("1");

/*     
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type fix.
     */
    public ClassicPort input;

    /**
     * output of type fix.
     */
    public ClassicPort output;

    /**
     *  Indicates whether or not to keep the precision of the arriving particles
as they are:  YES keeps the same precision, and NO casts the inputs
to the precision specified by the parameter "InputPrecision". parameter with initial value "YES".
     */
     public Parameter ArrivingPrecision;

    /**
     *  Sets the precision of the input in bits.
The input particles are only cast to this precision if the
parameter "ArrivingPrecision" is set to NO. parameter with initial value "2.14".
     */
     public Parameter InputPrecision;

    /**
     *  Sets the precision of the output in bits.
This is the precision that will hold the result of the product of the inputs.
When the value of the product extends outside of the precision,
the output is set to its maximum value (or minimum for negative magnitudes). parameter with initial value "2.14".
     */
     public Parameter OutputPrecision;

    /**
     *  index for multiple input trace parameter with initial value "1".
     */
     public Parameter index;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {
        
super.generateInitializeCode();

                if (!((IntToken)((ArrivingPrecision).getToken())).intValue())
		    input.setPrecision(InputPrecision);
		output.setPrecision(OutputPrecision);
     }

    /**
     */
    public void  generateFireCode() {
        
// insert code to clear overflow flag
		super.clearOverflow();

		// avoid FIX_Assign if possible
		if (input.numberPorts() == 2)
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	FIX_Mul($ref(output), $ref(input#1),$ref(input#2));\n"

); 	 addCode(_str_);  } 

		else {
			// initialize the product
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	FIX_Assign($ref(output),$ref(input#1));\n"

); 	 addCode(_str_);  } 

			for (int i=2; i <= input.numberPorts(); i++) {
			    index = i;
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	FIX_Mul($ref(output), $ref(output),$ref(input#index));\n"

); 	 addCode(_str_);  } 
			}
		}

		// insert code to test overflow flag
		super.checkOverflow();
     }
}
