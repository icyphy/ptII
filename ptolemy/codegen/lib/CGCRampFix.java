/* RampFix, CGC domain: CGCRampFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCRampFix.pl by ptlang
*/
/*
Copyright (c) 1990-1997 The Regents of the University of California.
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
//// CGCRampFix
/**
Generate a fixed-point ramp signal, starting at "value" (default 0.0)
with step size "step" (default 1.0).
A precision and an initial value can be specified for a parameter by using
the notation ("initial_value","precision").
<p>
The value of the "step" and "value" parameters and their precision 
in bits can currently be specified using two different notations. 
Specifying only a value by itself in the dialog box would create a 
fixed-point number with the default precision, which has a total length
of 24 bits with the number of range bits set as required by the value
of the parameter.
For example, the default value 1.0 creates a fixed-point object with 
precision 2.22, and a value like 0.5 would create one with precision 1.23.
An alternate way of specifying the value and the precision of this parameter
is to use the parenthesis notation which will be interpreted as
(value, precision).
For example, filling the dialog box of this parameter by (2.546, 3.5) would
create a fixed-point object by casting the double-precision floating-point
number 2.546 to a fixed-point precision of 3.5.

 @Author Juergen Weiss
 @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/CGCRampFix.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCRampFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCRampFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.FIX);

        // Precision of the output in bits and precision of the accumulation.\nWhen the value of the accumulation extends outside of the precision,\nthe output is set to its maximum value (or minimum for negative\nmagnitudes). PrecisionState
        OutputPrecision = new Parameter(this, "OutputPrecision");
        OutputPrecision.setExpression("2.14");

        // Increment from one sample to the next. FixState
        step = new Parameter(this, "step");
        step.setExpression("1.0");

        // Initial value output by the ramp.\nDuring simulation, this parameter holds the current value output by the ramp.\nThe precision of this state is the precision of the accumulation. FixState
        value = new Parameter(this, "value");
        value.setExpression("0.0");

        // Precision of the step in bits. PrecisionState
        StepPrecision = new Parameter(this, "StepPrecision");
        StepPrecision.setExpression("2.14");

        // Precision of the value in bits. PrecisionState
        ValuePrecision = new Parameter(this, "ValuePrecision");
        ValuePrecision.setExpression("2.14");

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
     *  Precision of the output in bits and precision of the accumulation.
When the value of the accumulation extends outside of the precision,
the output is set to its maximum value (or minimum for negative
magnitudes). parameter with initial value "2.14".
     */
     public Parameter OutputPrecision;

    /**
     *  Increment from one sample to the next. parameter with initial value "1.0".
     */
     public Parameter step;

    /**
     *  Initial value output by the ramp.
During simulation, this parameter holds the current value output by the ramp.
The precision of this state is the precision of the accumulation. parameter with initial value "0.0".
     */
     public Parameter value;

    /**
     *  Precision of the step in bits. parameter with initial value "2.14".
     */
     public Parameter StepPrecision;

    /**
     *  Precision of the value in bits. parameter with initial value "2.14".
     */
     public Parameter ValuePrecision;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {
        
super.generateInitializeCode();
		// if the user specified an invalid precision string, the error
		// will be automatically reported in the initialize method of
		// class PrecisionState
		output.setPrecision(OutputPrecision);
		value.setPrecision(ValuePrecision);
		step.setPrecision(StepPrecision);
     }

    /**
     */
    public void  generateFireCode() {
        
// insert code to clear overflow flag
		super.clearOverflow();

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	FIX_Assign($ref(output), $ref(value));\n"
"	FIX_Add($ref(value), $ref(value),$ref(step));\n"

); 	 addCode(_str_);  } 

		// insert code to test overflow flag
		super.checkOverflow();
     }
}
