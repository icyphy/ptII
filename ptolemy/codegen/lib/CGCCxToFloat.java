/* CxToFloat, CGC domain: CGCCxToFloat.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCCxToFloat.pl by ptlang
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
//// CGCCxToFloat
/**
type conversion from complex to float/int

 @Author S. Ha
 @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/stars/CGCCxToFloat.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCCxToFloat extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCCxToFloat(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.COMPLEX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // number of samples to be copied IntState
        numSample = new Parameter(this, "numSample");
        numSample.setExpression("1");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type complex.
     */
    public ClassicPort input;

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  number of samples to be copied parameter with initial value "1".
     */
     public Parameter numSample;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return ((IntToken)((numSample).getToken())).intValue() * 200;
     }

    /**
     */
    public void  generatePreinitializeCode() {

numSample = output.numXfer();
		addInclude("<math.h>");
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if (((IntToken)((numSample).getToken())).intValue() > 1) {
			input.setSDFParams(((IntToken)((numSample).getToken())).intValue());
			output.setSDFParams(((IntToken)((numSample).getToken())).intValue());
		}
     }

    /**
     */
    public void  generateFireCode() {

addCode(body);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String body =
        "	int i = 0;\n"
        + "	double p, q;\n"
        + "	for ( ; i < $val(numSample); i++) {\n"
        + "		p = $ref(input, i).real;\n"
        + "		q = $ref(input, i).imag;\n"
        + "		$ref(output, i) = sqrt(p*p + q*q);\n"
        + "	}\n";
}
