/* Biquad, CGC domain: CGCBiquad.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCBiquad.pl by ptlang
 */
/*
  Copyright (c) 1990-1997 The Regents of the University of California.
  All rights reserved.
  See the file $PTOLEMY/copyright for copyright notice,
  limitation of liability, and disclaimer of warranty provisions.
*/
package ptolemy.codegen.dsp.lib;

import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.codegen.kernel.ClassicCGCActor;
import ptolemy.codegen.kernel.ClassicPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCBiquad
/**
   A two-pole, two-zero parametric digital IIR filter (a biquad).
   <p>
   A two-pole, two-zero IIR filter.

   @Author J. T. Buck and William Chen
   @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCBiquad.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCBiquad extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCBiquad(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // d1 FloatState
        d1 = new Parameter(this, "d1");
        d1.setExpression("-1.1430");

        // d2 FloatState
        d2 = new Parameter(this, "d2");
        d2.setExpression("0.41280");

        // n0 FloatState
        n0 = new Parameter(this, "n0");
        n0.setExpression("0.067455");

        // n1 FloatState
        n1 = new Parameter(this, "n1");
        n1.setExpression("0.135");

        // n2 FloatState
        n2 = new Parameter(this, "n2");
        n2.setExpression("0.067455");

        // internal state. FloatState
        state1 = new Parameter(this, "state1");
        state1.setExpression("0.0");

        // internal state. FloatState
        state2 = new Parameter(this, "state2");
        state2.setExpression("0.0");

        /*
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
     *  d1 parameter with initial value "-1.1430".
     */
    public Parameter d1;

    /**
     *  d2 parameter with initial value "0.41280".
     */
    public Parameter d2;

    /**
     *  n0 parameter with initial value "0.067455".
     */
    public Parameter n0;

    /**
     *  n1 parameter with initial value "0.135".
     */
    public Parameter n1;

    /**
     *  n2 parameter with initial value "0.067455".
     */
    public Parameter n2;

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
    public void  generatePreinitializeCode() {
        //# line 77 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCBiquad.pl"
        addInclude("<math.h>");
        addGlobal(mainDecl);
        addCode(settapDef);
    }

    /**
     */
    public void  generateFireCode() {
        //# line 96 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCBiquad.pl"
        addCode(localDecl);
        addCode(iirfilter);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String mainDecl =
    "          double $starSymbol(filtertaps)[5];\n";

    public String settapDef =
    "          $starSymbol(filtertaps)[0]=$val(d1);\n"
    + "          $starSymbol(filtertaps)[1]=$val(d2);\n"
    + "          $starSymbol(filtertaps)[2]=$val(n0);\n"
    + "          $starSymbol(filtertaps)[3]=$val(n1);\n"
    + "          $starSymbol(filtertaps)[4]=$val(n2);\n";

    public String localDecl =
    "          double nextstate,out;\n";

    public String iirfilter =
    "          nextstate = $ref(input) - $starSymbol(filtertaps)[0] *\n"
    + "            (double)$ref(state1) - $starSymbol(filtertaps)[1] *\n"
    + "            (double)$ref(state2);\n"
    + "          out = nextstate * $starSymbol(filtertaps)[2] +\n"
    + "            (double)$ref(state1) * $starSymbol(filtertaps)[3] +\n"
    + "            (double)$ref(state2) * $starSymbol(filtertaps)[4];\n"
    + "          $ref(output)=out;\n"
    + "          $ref(state2)=$ref(state1);\n"
    + "          $ref(state1)=nextstate;\n";
}
