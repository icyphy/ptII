/* DownSample, CGC domain: CGCDownSample.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCDownSample.pl by ptlang
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
//// CGCDownSample
/**
   A decimator by "factor" (default 2).  The "phase" tells which sample to
   output.  If phase = 0, the most recent sample is the output, while if
   phase = factor-1 the oldest sample is the output.  Phase = 0 is the
   default.

   @Author Soonhoi Ha
   @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/CGCDownSample.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCDownSample extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCDownSample(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Downsample factor. IntState
        factor = new Parameter(this, "factor");
        factor.setExpression("2");

        // Downsample phase. IntState
        phase = new Parameter(this, "phase");
        phase.setExpression("0");

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
     *  Downsample factor. parameter with initial value "2".
     */
    public Parameter factor;

    /**
     *  Downsample phase. parameter with initial value "0".
     */
    public Parameter phase;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

        return 1;
    }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

        input.setSDFParams(((IntToken)((factor).getToken())).intValue(),((IntToken)((factor).getToken())).intValue()-1);
        if (((IntToken)((phase).getToken())).intValue() >= ((IntToken)((factor).getToken())).intValue())
            throw new IllegalActionException(this, ": phase must be < factor");
    }

    /**
     */
    public void  generateFireCode() {

        addCode(sendsample);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String sendsample =
    "        $ref(output) = $ref2(input,phase);\n";
}
