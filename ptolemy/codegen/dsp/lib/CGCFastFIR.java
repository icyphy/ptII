/* FastFIR, CGC domain: CGCFastFIR.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCFastFIR.pl by ptlang
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
//// CGCFastFIR
/**
   A Finite Impulse Response (FIR) filter.
   Coefficients are in the "taps" state variable.
   Default coefficients give an 8th order, linear phase lowpass
   filter. To read coefficients from a file, replace the default
   coefficients with "fileName".
   <p>
   This FIR filter produces fast code by eliminating the need for a
   Circular buffer.

   @Author Soonhoi Ha, Bill Chen, and John Reekie
   @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCFastFIR.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCFastFIR extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCFastFIR(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        signalIn = new ClassicPort(this, "signalIn", true, false);
        signalIn.setTypeEquals(BaseType.DOUBLE);
        signalOut = new ClassicPort(this, "signalOut", false, true);
        signalOut.setTypeEquals(BaseType.DOUBLE);

        // Filter tap values. FloatArrayState
        taps = new Parameter(this, "taps");
        taps.setExpression("{-.040609 -.001628 .17853 .37665 .37665 .17853 -.001628 -.040609}");

        // tapSize IntState
        tapSize = new Parameter(this, "tapSize");
        tapSize.setExpression("0");

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * signalIn of type double.
     */
    public ClassicPort signalIn;

    /**
     * signalOut of type double.
     */
    public ClassicPort signalOut;

    /**
     *  Filter tap values. parameter with initial value "-.040609 -.001628 .17853 .37665 .37665 .17853 -.001628 -.040609".
     */
    public Parameter taps;

    /**
     *  tapSize parameter with initial value "0".
     */
    public Parameter tapSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generatePreinitializeCode() {
        //# line 64 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFastFIR.pl"
        addDeclaration(mainDecl);
        addCode(initialize);
    }

    /**
     */
    public void  generateInitializeCode() {
        //# line 47 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFastFIR.pl"
        tapSize = taps.size();
        signalIn.setSDFParams(1,0);
        signalOut.setSDFParams(1,0);
    }

    /**
     */
    public void  generateFireCode() {
        //# line 95 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFastFIR.pl"
        addCode(bodyDecl);
        addCode(body);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String mainDecl =
    "          int currentValue,i;\n"
    + "          double src[$val(tapSize)], fir[$val(tapSize)];\n";

    public String initialize =
    "          currentValue = 0;\n"
    + "          for (i=0;i<$val(tapSize);i++){\n"
    + "            fir[i]=$ref2(taps,i);\n"
    + "            src[i] = 0.0;\n"
    + "          }\n";

    public String bodyDecl =
    "          double accum;\n"
    + "          int nminusk,k;\n";

    public String body =
    "\n"
    + "          if (currentValue > $val(tapSize)-1){\n"
    + "            currentValue -= $val(tapSize);\n"
    + "          }\n"
    + "          \n"
    + "          accum = 0.0;\n"
    + "          nminusk = currentValue;\n"
    + "          src[nminusk] = $ref(signalIn);\n"
    + "          for (k=0; k < currentValue + 1; k++){\n"
    + "            accum += fir[k] * src[nminusk];\n"
    + "            nminusk--;\n"
    + "          }\n"
    + "\n"
    + "          nminusk = $val(tapSize)-1;\n"
    + "          for (k = currentValue+1; k < $val(tapSize); k++){\n"
    + "            accum += fir[k] * src[nminusk];\n"
    + "            nminusk--;\n"
    + "          }\n"
    + "\n"
    + "          $ref(signalOut) = accum;\n"
    + "          currentValue++;\n";
}
