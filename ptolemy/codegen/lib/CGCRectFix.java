/* RectFix, CGC domain: CGCRectFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCRectFix.pl by ptlang
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
//// CGCRectFix
/**
Generate a fixed-point rectangular pulse of height "height"
(default 1.0) and width "width" (default 8).
If "period" is greater than zero, then the pulse is repeated with the
given period.

 @Author Juergen Weiss
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCRectFix.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCRectFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCRectFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.FIX);

        // Height of the rectangular pulse. FixState
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

        // Precision of the output in bits.\nThe value of the \"height\" parameter is cast to this precision and then output.\nIf the value cannot be represented by this precision,\nthe output is set to its maximum value (or minimum for negative magnitudes). PrecisionState
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

    /**
     *  Precision of the output in bits.
The value of the "height" parameter is cast to this precision and then output.
If the value cannot be represented by this precision,
the output is set to its maximum value (or minimum for negative magnitudes). parameter with initial value "2.14".
     */
     public Parameter OutputPrecision;

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
     }

    /**
     */
    public void  generateFireCode() {

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	FIX_SetToZero($ref(output));\n"

); 	 addCode(_str_);  }

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	if ($ref(count) < $ref(width)) {\n"

); 	 addCode(_str_);  }
		super.clearOverflow();
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"		FIX_Assign($ref(output),$ref(height));\n"

); 	 addCode(_str_);  }
		super.checkOverflow();
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	}\n"
"	$ref(count)++;\n"
"	if ($ref(period) > 0 && $ref(count) > $ref(period))\n"
"		$ref(count) = 0;"

); 	 addCode(_str_);  }

     }
}
