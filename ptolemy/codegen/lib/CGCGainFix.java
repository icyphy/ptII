/* GainFix, CGC domain: CGCGainFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCGainFix.pl by ptlang
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
//// CGCGainFix
/**
This is an amplifier; the fixed-point output is the fixed-point input
multiplied by the "gain" (default 1.0).  The precision of "gain", the
input, and the output can be specified in bits.
<p>
The value of the "gain" parameter and its precision in bits can currently
be specified using two different notations.
Specifying only a value by itself in the dialog box would create a
fixed-point number with the default precision which has a total length
of 24 bits with the number of range bits as required by the value.
For example, the default value 1.0 creates a fixed-point object with
precision 2.22, and a value like 0.5 would create one with precision
1.23.  An alternative way of specifying the value and the
precision of this parameter is to use the parenthesis notation
of (value, precision).  For example, filling the dialog
box for the gain parameter with (2.546, 3.5) would create a fixed-point
object formed by casting the double-precision floating-point number
2.546 to a fixed-point number with a precision of 3.5.

 @Author Juergen Weiss
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCGainFix.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCGainFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCGainFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.FIX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.FIX);

        // Gain of the star. FixState
        gain = new Parameter(this, "gain");
        gain.setExpression("1.0");

        // Flag indicated whether or not to use the arriving particles as they are:\nYES keeps the same precision, and NO casts them to the\nprecision specified by the parameter \"InputPrecision\". IntState
        ArrivingPrecision = new Parameter(this, "ArrivingPrecision");
        ArrivingPrecision.setExpression("YES");

        // Precision of the input in bits.  The input particles are only cast\nto this precision if the parameter \"ArrivingPrecision\" is set to NO. PrecisionState
        InputPrecision = new Parameter(this, "InputPrecision");
        InputPrecision.setExpression("2.14");

        // Precision of the output in bits.\nThis is the precision that will hold the result of the arithmetic operation\non the inputs.\nWhen the value of the product extends outside of the precision,\nthe output is set to its maximum value (or minimum for negative magnitudes). PrecisionState
        OutputPrecision = new Parameter(this, "OutputPrecision");
        OutputPrecision.setExpression("2.14");

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
     *  Gain of the star. parameter with initial value "1.0".
     */
     public Parameter gain;

    /**
     *  Flag indicated whether or not to use the arriving particles as they are:
YES keeps the same precision, and NO casts them to the
precision specified by the parameter "InputPrecision". parameter with initial value "YES".
     */
     public Parameter ArrivingPrecision;

    /**
     *  Precision of the input in bits.  The input particles are only cast
to this precision if the parameter "ArrivingPrecision" is set to NO. parameter with initial value "2.14".
     */
     public Parameter InputPrecision;

    /**
     *  Precision of the output in bits.
This is the precision that will hold the result of the arithmetic operation
on the inputs.
When the value of the product extends outside of the precision,
the output is set to its maximum value (or minimum for negative magnitudes). parameter with initial value "2.14".
     */
     public Parameter OutputPrecision;

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

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	FIX_Mul($ref(output),$ref(input),$ref(gain));\n"

); 	 addCode(_str_);  }

		// insert code to test overflow flag
		super.checkOverflow();
     }
}
