/* LMS, CGC domain: CGCLMS.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCLMS.pl by ptlang
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
//// CGCLMS
/**
   Adaptive filter using LMS adaptation algorithm.
   Initial coefficients are in the "taps" state variable.
   Default initial coefficients give an 8th order, linear phase
   lowpass filter.  To read default coefficients from a file,
   replace the default coefficients with "fileName".
   Supports decimation, but not interpolation.
   <p>
   When correctly used, this filter will adapt to try to minimize
   the mean-squared error of the signal at its <i>error</i> input.
   In order for this to be possible, the output of the filter should
   be compared (subtracted from) some reference signal to produce
   an error signal.
   That error signal should be fed back to the <i>error</i> input.
   The <i>delay</i> parameter must equal the total number of delays
   in the path from the output of the filter back to the error input.
   This ensures correct alignment of the adaptation algorithm.
   The number of delays must be greater than zero or the dataflow
   graph will deadlock.
   The adaptation algorithm used is the well-known LMS, or stochastic-gradient
   algorithm.
   <a name="stochastic gradient algorithm"></a>
   <p>
   If the <i>saveTapsFile</i> string is non-null, a file will
   be created by the name given by that string, and the final tap values
   will be stored there after the run has completed.

   @Author Soonhoi Ha
   @Version $Id$, based on version 1.15 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCLMS.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCLMS extends CGCFIR {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCLMS(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        error = new ClassicPort(this, "error", true, false);
        error.setTypeEquals(BaseType.DOUBLE);

        // Adaptation step size. FloatState
        stepSize = new Parameter(this, "stepSize");
        stepSize.setExpression("0.01");

        // Delay in the update loop. IntState
        errorDelay = new Parameter(this, "errorDelay");
        errorDelay.setExpression("1");

        // File to save final tap values. StringState
        saveTapsFile = new Parameter(this, "saveTapsFile");
        saveTapsFile.setExpression("");

        /*     //# line 66 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCLMS.pl"
        // remove interpolation as a settable parameter
        interpolation.clearAttributes(A_SETTABLE);
        // taps are no longer constant
        taps.clearAttributes(A_CONSTANT);
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * error of type double.
     */
    public ClassicPort error;

    /**
     *  Adaptation step size. parameter with initial value "0.01".
     */
    public Parameter stepSize;

    /**
     *  Delay in the update loop. parameter with initial value "1".
     */
    public Parameter errorDelay;

    /**
     *  File to save final tap values. parameter with initial value "".
     */
    public Parameter saveTapsFile;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  wrapup() {
        //# line 107 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCLMS.pl"
        const char* sf = saveTapsFile;
        if (sf != NULL && *sf != 0) {
            addCode("    {\n");
            addCode(save);
            addCode("    }\n");
        }
    }

    /**
     */
    public void  generatePreinitializeCode() {
        //# line 85 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCLMS.pl"
        addInclude("<stdio.h>");
    }

    /**
     */
    public void  generateInitializeCode() {
        //# line 72 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCLMS.pl"
        // First check to be sure that interpolation is 1.
        interpolation = 1;

        // Next run the FIR setup routine
        CGCFIR :: setup();

        // Then reset the signalIn number of samples in the past
        // to account for the error delay.
        signalIn.setSDFParams(int(decimation),
                int(decimation) + 1 + int(errorDelay)
                + taps.size());
    }

    /**
     */
    public void  generateFireCode() {
        //# line 88 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCLMS.pl"
        addCode(bodyDecl);        // from FIR
        addCode(update);
        addCode(body);                // from FIR
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String update =
    "        int ix;\n"
    + "        /* First update the taps */\n"
    + "        double e = $ref(error);\n"
    + "        int index = $val(errorDelay)*$val(decimation) + $val(decimationPhase);\n"
    + "\n"
    + "        for (ix = 0; ix < $val(tapSize); ix++) {\n"
    + "                $ref2(taps,ix) = $ref2(taps,ix) +\n"
    + "                        e * $ref2(signalIn,index) * $ref(stepSize);\n"
    + "                index++;\n"
    + "        }\n";

    public String save =
    "    FILE* fp;\n"
    + "    int i;\n"
    + "    if (!(fp = fopen(saveFileName,\"w\"))) {\n"
    + "        /* File cannot be opened */\n"
    + "        fprintf(stderr,\"ERROR: Cannot open saveTapsFile for writing:\\n\");\n"
    + "            exit(1);\n"
    + "    }\n"
    + "    for (i = 0; i < $val(tapSize); i++)\n"
    + "        fprintf(fp, \"%d %g\\n\", i, $ref2(taps,i));\n"
    + "    fclose(fp);\n";
}
