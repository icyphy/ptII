/* GoertzelBase, CGC domain: CGCGoertzelBase.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzelBase.pl by ptlang
*/
/*
Copyright (c) 1990-1997 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty provisions.
 */
package ptolemy.domains.sdf.cgc.dsp.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCGoertzelBase
/**
Base class for Goertzel algorithm stars.

 @Author Brian L. Evans
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzelBase.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCGoertzelBase extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCGoertzelBase(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);

        // the DFT coefficient to compute (k less than N)
        k = new Parameter(this, "k");
        k.setExpression("0");

        // length of the DFT
        N = new Parameter(this, "N");
        N.setExpression("32");

        // amount of data to read (N less than or equal to size)
        size = new Parameter(this, "size");
        size.setExpression("32");

        // first-order feedback coefficient which is a function of k and N
        d1 = new Parameter(this, "d1");
        d1.setExpression("0.0");

        // internal state.
        state1 = new Parameter(this, "state1");
        state1.setExpression("0.0");

        // internal state.
        state2 = new Parameter(this, "state2");
        state2.setExpression("0.0");

/*     //# line 64 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzelBase.pl"
theta = 0.0;
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type double.
     */
    public TypedIOPort input;

    /**
     *  the DFT coefficient to compute (k less than N) parameter with initial value "0".
     */
     public Parameter k;

    /**
     *  length of the DFT parameter with initial value "32".
     */
     public Parameter N;

    /**
     *  amount of data to read (N less than or equal to size) parameter with initial value "32".
     */
     public Parameter size;

    /**
     *  first-order feedback coefficient which is a function of k and N parameter with initial value "0.0".
     */
     public Parameter d1;

    /**
     *  internal state. parameter with initial value "0.0".
     */
     public Parameter state1;

    /**
     *  internal state. parameter with initial value "0.0".
     */
     public Parameter state2;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        //# line 141 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzelBase.pl"
        //return (3 + 3 + 7*int(N));
        // Dummy return value.
        return 0;
     }

    /**
     */
    public void  setup() {
        //# line 102 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzelBase.pl"
        //// FIXME: Parameters are not always resolved properly
        //                // before setup but should be.  For now, check parameters
        //                // in go method and guard against division by N = 0
        //                // CheckParameterValues();
        //                // double Nd = double(int(N));
        //		double Nd = double(int(N) ? int(N) : 1);
        //		double kd = int(k);
        //		theta = -2.0 * M_PI * kd / Nd;
        //		d1 = 2.0 * cos(theta);
        //		input.setSDFParams(int(size), int(size)-1);
     }

    /**
     */
    public void  go() {
        //# line 136 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCGoertzelBase.pl"
        //CheckParameterValues();
        //		addCode(decl);
        //		addCode(filter);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String decl = 
        "		double acc = 0.0;\n"
        + "		double d1val = $ref(d1);\n"
        + "		int i;\n";

    public String filter = 
        "		/* Run all-pole section of Goertzel's algorithm N iterations.\n"
        + "		   Only one multiplier (d1) in iteration.\n"
        + "		   Zero the IIR state for each DFT calculation; otherwise,\n"
        + "		   the filter output could grow without bound.\n"
        + "		   state1 and state2 are states and not local variables\n"
        + "		   ONLY to pass their values to derived stars */\n"
        + "		$ref(state1) = 0.0;\n"
        + "		$ref(state2) = 0.0;\n"
        + "		for (i = $val(N)-1; i >= 0; i--) {\n"
        + "		  acc = $ref2(input,i);\n"
        + "		  acc += d1val * $ref(state1);\n"
        + "		  acc -= $ref(state2);\n"
        + "		  $ref(state2) = $ref(state1);\n"
        + "		  $ref(state1) = acc;\n"
        + "		}\n";
}
