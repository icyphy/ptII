/* DTMFPostTest, CGC domain: CGCDTMFPostTest.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCDTMFPostTest.pl by ptlang
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
//// CGCDTMFPostTest
/**
   Returns whether or not a valid dual-tone modulated-frequency has
   been correctly detected based on the last three detection results.
   <p>
   The assumption is that the 100 msec DTMF interval has been split into
   roughly four parts.  This star looks at the last three detection results,
   which are represented as integers.  A new digit has been detected
   if two consecutive detected digits are the same followed by a third
   detected digit that is different.
   <p>
   This test is useful for two reasons.
   First, it filters redundant hits so that only one is reported.
   Second, it improves robustness against noisy DTMF signals and speech input.
   <h3>References</h3>
   <p>[1]
   Pat Mock, "Add DTMF Generation and Decoding to DSP-uP Designs,"
   Electronic Data News, March 21, 1985.  Reprinted in
   <i>Digital Signal Processing Applications with the TMS320 Family</i>,
   Texas Instruments, 1986.

   @Author Brian L. Evans
   @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCDTMFPostTest.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCDTMFPostTest extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCDTMFPostTest(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        valid = new ClassicPort(this, "valid", true, false);
        valid.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // The value to assign to last and secondToLast internal states, which hold the\nprevious two valid inputs.  For the purposes of DTMF detection, it is\nset to an integer that does not represent a DTMF digit. IntState
        initialLastInput = new Parameter(this, "initialLastInput");
        initialLastInput.setExpression("-1");

        // internal state to store the last valid integer IntState
        last = new Parameter(this, "last");
        last.setExpression("-1");

        // internal state to store the second-to-last valid integer FloatState
        secondToLast = new Parameter(this, "secondToLast");
        secondToLast.setExpression("-1");

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type int.
     */
    public ClassicPort input;

    /**
     * valid of type int.
     */
    public ClassicPort valid;

    /**
     * output of type int.
     */
    public ClassicPort output;

    /**
     *  The value to assign to last and secondToLast internal states, which hold the
     previous two valid inputs.  For the purposes of DTMF detection, it is
     set to an integer that does not represent a DTMF digit. parameter with initial value "-1".
    */
    public Parameter initialLastInput;

    /**
     *  internal state to store the last valid integer parameter with initial value "-1".
     */
    public Parameter last;

    /**
     *  internal state to store the second-to-last valid integer parameter with initial value "-1".
     */
    public Parameter secondToLast;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() {
        //# line 75 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCDTMFPostTest.pl"
        last = int(initialLastInput);
        secondToLast = int(initialLastInput);
    }

    /**
     */
    public void  generateFireCode() {
        //# line 102 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCDTMFPostTest.pl"
        addCode(decl);
        addCode(test);
        addCode(sendOutput);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String decl =
    "		int retval = 0;\n"
    + "		int inputValue = $val(initialLastInput);\n"
    + "		int lastValue = $ref(last);\n"
    + "\n";

    public String test =
    "		/* if the current input is valid, then compare it with the\n"
    + "		   last and second-to-last valid inputs; else, return FALSE */\n"
    + "		if ( $ref(valid) ) {\n"
    + "		  inputValue = $ref(input);\n"
    + "		  retval = ( inputValue == lastValue &&\n"
    + "		             lastValue != $ref(secondToLast) );\n"
    + "		}\n"
    + "\n"
    + "		/* Update the last and second-to-last input storage */\n"
    + "		$ref(secondToLast) = lastValue;\n"
    + "		$ref(last) = inputValue;\n";

    public String sendOutput =
    "\n"
    + "		$ref(output) = retval;\n";
}
