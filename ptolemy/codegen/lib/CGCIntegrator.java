/* Integrator, CGC domain: CGCIntegrator.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCIntegrator.pl by ptlang
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
//// CGCIntegrator
/**
An integrator with leakage, limits, and reset.
With the default parameters, input samples are simply accumulated,
and the running sum is the output.  To prevent any resetting in the
middle of a run, connect a d.c. source with value 0.0 to the "reset"
input.  Otherwise, whenever a non-zero is received on this input,
the accumulated sum is reset to the current input (i.e. no feedback).

Limits are controlled by the "top" and "bottom" parameters.
If top greater than or equal to bottom, no limiting is performed (default).  Otherwise,
the output is kept between "bottom" and "top".  If "saturate" = YES,
saturation is performed.  If "saturate" = NO, wrap-around is performed
(default).  Limiting is performed before output.

Leakage is controlled by the "feedbackGain" state (default 1.0).
The output is the data input plus feedbackGain*state, where state
is the previous output.
<p>
<a name="filter, integrator"></a>

 @Author E. A. Lee
 @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/CGCIntegrator.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCIntegrator extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCIntegrator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        data = new ClassicPort(this, "data", true, false);
        data.setTypeEquals(BaseType.DOUBLE);
        reset = new ClassicPort(this, "reset", true, false);
        reset.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // The gain on the feedback path. FloatState
        feedbackGain = new Parameter(this, "feedbackGain");
        feedbackGain.setExpression("1.0");

        // The upper limit. FloatState
        top = new Parameter(this, "top");
        top.setExpression("0.0");

        // The lower limit. FloatState
        bottom = new Parameter(this, "bottom");
        bottom.setExpression("0.0");

        // Saturate if YES, wrap around otherwise. IntState
        saturate = new Parameter(this, "saturate");
        saturate.setExpression("YES");

        // An internal state. FloatState
        state = new Parameter(this, "state");
        state.setExpression("0.0");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * data of type double.
     */
    public ClassicPort data;

    /**
     * reset of type int.
     */
    public ClassicPort reset;

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  The gain on the feedback path. parameter with initial value "1.0".
     */
     public Parameter feedbackGain;

    /**
     *  The upper limit. parameter with initial value "0.0".
     */
     public Parameter top;

    /**
     *  The lower limit. parameter with initial value "0.0".
     */
     public Parameter bottom;

    /**
     *  Saturate if YES, wrap around otherwise. parameter with initial value "YES".
     */
     public Parameter saturate;

    /**
     *  An internal state. parameter with initial value "0.0".
     */
     public Parameter state;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

int x = 0;
                if (spread > 0.0) {
                        if (((IntToken)((saturate).getToken())).intValue() == 1) // FIXME saturate should be a Boolean x = 3;
                        else x = 5;
                }
                return 3 + x + 2;
     }

    /**
     */
    public void  generatePreinitializeCode() {

addDeclaration(declarations);
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

spread = ((DoubleToken)((top).getToken())).doubleValue() - ((DoubleToken)((bottom).getToken())).doubleValue();
     }

    /**
     */
    public void  generateFireCode() {

addCode(integrate);
            if (spread > 0.0)
                if (((IntToken)((saturate).getToken())).intValue() == 1) // FIXME saturate should be a Boolean
                    addCode(limitWithSat);
                else
                    addCode(limitWithoutSat);
            addCode(write);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String declarations =
        "            double $starSymbol(t);\n";

    public String integrate =
        "            if ($ref(reset) != 0) {\n"
        + "                $starSymbol(t) = $ref(data);\n"
        + "            } else {\n"
        + "                $starSymbol(t) = $ref(data) +\n"
        + "                        $val(feedbackGain) * $ref(state);\n"
        + "            }\n";

    public String limitWithSat =
        "            /* Limiting is in effect */\n"
        + "            /* Take care of the top */\n"
        + "            if ($starSymbol(t) > $val(top)) $starSymbol(t) = $val(top);\n"
        + "            /* Take care of the bottom */\n"
        + "            if ($starSymbol(t) < $val(bottom)) $starSymbol(t) = $val(bottom);\n";

    public String limitWithoutSat =
        "            /* Limiting is in effect */\n"
        + "            /* Take care of the top */\n"
        + "            if ($starSymbol(t) > $val(top))\n"
        + "                do $starSymbol(t) -= ($val(top) - $val(bottom));\n"
        + "                while ($starSymbol(t) > $val(top));\n"
        + "            /* Take care of the bottom */\n"
        + "            if ($starSymbol(t) < $val(bottom))\n"
        + "                do $starSymbol(t) += ($val(top) - $val(bottom));\n"
        + "                while ($starSymbol(t) < $val(bottom));\n";

    public String write =
        "            $ref(output) = $starSymbol(t);\n"
        + "            $ref(state) = $starSymbol(t);\n";
}
