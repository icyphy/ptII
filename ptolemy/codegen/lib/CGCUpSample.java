/* UpSample, CGC domain: CGCUpSample.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCUpSample.pl by ptlang
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
//// CGCUpSample
/**
Upsample by a factor (default 2), filling with fill (default 0.0).  The
"phase" tells where to put the sample in an output block.  The default
is to output it first (phase = 0). The maximum phase is "factor" - 1.

 @Author E. A. Lee and S. Ha
 @Version $Id$, based on version 1.12 of /users/ptolemy/src/domains/cgc/stars/CGCUpSample.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCUpSample extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCUpSample(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Number of samples produced. IntState
        factor = new Parameter(this, "factor");
        factor.setExpression("2");

        // Where to put the input in the output block. IntState
        phase = new Parameter(this, "phase");
        phase.setExpression("0");

        // Value to fill the output block. FloatState
        fill = new Parameter(this, "fill");
        fill.setExpression("0.0");

/*
noInternalState();
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
     *  Number of samples produced. parameter with initial value "2".
     */
     public Parameter factor;

    /**
     *  Where to put the input in the output block. parameter with initial value "0".
     */
     public Parameter phase;

    /**
     *  Value to fill the output block. parameter with initial value "0.0".
     */
     public Parameter fill;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

return 1 + ((IntToken)((factor).getToken())).intValue();
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

output.setSDFParams(((IntToken)((factor).getToken())).intValue(),((IntToken)((factor).getToken())).intValue()-1);
                if (((IntToken)((phase).getToken())).intValue() >= ((IntToken)((factor).getToken())).intValue())
                        throw new IllegalActionException(this, ": phase must be < factor");
     }

    /**
     */
    public void  generateFireCode() {

int index = ((IntToken)((factor).getToken())).intValue() - ((IntToken)((phase).getToken())).intValue() - 1;
                if (output.staticBuf() && output.linearBuf())
                        addCode(sendOne(index));
                else
                        addCode(sendAll(index));
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String sendOne (int index) {
        return
        "        $ref2(output," + index + ") = $ref(input);\n";
    }

    public String sendAll (int index) {
        return
        "        int i;\n"
        + "        for (i = 0; i < $val(factor); i++) {\n"
        + "                $ref2(output,i) = $val(fill);\n"
        + "        }\n"
        + "        $ref2(output," + index + ") = $ref(input);\n";
    }
}
