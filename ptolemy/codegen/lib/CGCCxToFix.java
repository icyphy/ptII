/* CxToFix, CGC domain: CGCCxToFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCCxToFix.pl by ptlang
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
//// CGCCxToFix
/**
Convert a complex input to a fixed-point output.
<p>
This star converts a complex value to a fix value with the specified precision
by computing the absolute value.
If the output precision is not specified, the precision is determined at
runtime according to the incoming float value.

 @Author Juergen Weiss
 @Version $Id$, based on version 1.8 of /users/ptolemy/src/domains/cgc/stars/CGCCxToFix.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCCxToFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCCxToFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.COMPLEX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.FIX);

        // number of samples to be copied IntState
        numSample = new Parameter(this, "numSample");
        numSample.setExpression("1");

        // Precision of the output in bits or empty if unspecified.\nThe absolute value of the complex number is computed and then converted to\nthis precision.\nIf the converted value cannot be represented by the number of bits specified\nin the precision parameter, then the output is set to its maximum value (or\nminimum for negative magnitudes). PrecisionState
        OutputPrecision = new Parameter(this, "OutputPrecision");
        OutputPrecision.setExpression("");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
Input complex type
     */
    public ClassicPort input;

    /**
Output fix type
     */
    public ClassicPort output;

    /**
     *  number of samples to be copied parameter with initial value "1".
     */
     public Parameter numSample;

    /**
     *  Precision of the output in bits or empty if unspecified.
The absolute value of the complex number is computed and then converted to
this precision.
If the converted value cannot be represented by the number of bits specified
in the precision parameter, then the output is set to its maximum value (or
minimum for negative magnitudes). parameter with initial value "".
     */
     public Parameter OutputPrecision;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generatePreinitializeCode() {

super.initCode();
                numSample = output.numXfer();
                addInclude("<math.h>");
     }

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

                if (((IntToken)((numSample).getToken())).intValue() > 1) {
                        input.setSDFParams(((IntToken)((numSample).getToken())).intValue());
                        output.setSDFParams(((IntToken)((numSample).getToken())).intValue());
                }

                output.setPrecision(OutputPrecision);
     }

    /**
     */
    public void  generateFireCode() {

// insert code to clear overflow flag
                super.clearOverflow();

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"     {        int i = 0;\n"
"        double p, q;\n"
"        for (; i < $val(numSample); i++) {\n"
"                p = $ref(input, i).real;\n"
"                q = $ref(input, i).imag;\n"
"                p = sqrt(p*p+q*q);\n"

);          addCode(_str_);  }

            if (output.attributes() & AB_VARPREC)
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"                FIX_SetPrecisionFromDouble($precision(output),p);\n"

);          addCode(_str_);  }

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"                FIX_DoubleAssign($ref(output,i),p);\n"
"     } }\n"

);          addCode(_str_);  }

                // insert code to test overflow flag
                super.checkOverflow();
     }
}
