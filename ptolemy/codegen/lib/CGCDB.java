/* DB, CGC domain: CGCDB.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCDB.pl by ptlang
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
//// CGCDB
/**
Converts input to dB.  Zero and negative values are
converted to "min" (default -100).
<p>
For inputs that are greater than zero, the output either
<i>N </i>log<sub>10</sub>(<i>input</i>) or <i>min</i>, whichever is larger, where
<i>N  </i>=<i>  </i>10 if <i>inputIsPower</i> is true, and <i>N  </i>=<i>  </i>20 otherwise.
<a name="decibel"></a>
The default is <i>N  </i>=<i>  </i>20.
For inputs that are zero or negative, the output is <i>min</i>.

 @Author Soonhoi Ha
 @Version $Id$, based on version 1.13 of /users/ptolemy/src/domains/cgc/stars/CGCDB.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCDB extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCDB(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Minimum output value. FloatState
        min = new Parameter(this, "min");
        min.setExpression("-100");

        // true if input is a power measurement, false if it's an amplitude measurement. IntState
        inputIsPower = new Parameter(this, "inputIsPower");
        inputIsPower.setExpression("false");

        // gain FloatState
        gain = new Parameter(this, "gain");
        gain.setExpression("20.0");

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
     *  Minimum output value. parameter with initial value "-100".
     */
     public Parameter min;

    /**
     *  true if input is a power measurement, false if it's an amplitude measurement. parameter with initial value "false".
     */
     public Parameter inputIsPower;

    /**
     *  gain parameter with initial value "20.0".
     */
     public Parameter gain;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
return 37;   /* approximate value from LOG */
     }

    /**
     */
    public void  generatePreinitializeCode() {
        
addInclude("<math.h>");
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {
        
if (((IntToken)((inputIsPower).getToken())).intValue() == 1) // FIXME inputIsPower should be a Boolean gain=10.0;
     }

    /**
     */
    public void  generateFireCode() {
        
addCode(body); 
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String body = 
        "	double f = $ref(input);\n"
        + "	if (f <= 0.0) $ref(output) = $val(min);\n"
        + "	else {\n"
        + "		f = $val(gain) * log10 (f);\n"
        + "		if (f < $val(min)) f = $val(min);\n"
        + "		$ref(output) = f;\n"
        + "	}\n";
}
