/* Impulse, CGC domain: CGCImpulse.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCImpulse.pl by ptlang
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
//// CGCImpulse
/**
Generate a single impulse or an impulse train.  The impulse(s) have
amplitude "level" (default 1.0).  If "period" (default 0) is equal to 0,
then only a single impulse is generated; otherwise, it specifies the
period of the impulse train.  The impulse or impulse train is delayed
by the amount specified by "delay".

 @Author J. Weiss Contributor(s): SDF version by J. T. Buck
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCImpulse.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCImpulse extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCImpulse(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Height of the impulse FloatState
        level = new Parameter(this, "level");
        level.setExpression("1.0");

        // Non-negative period of the impulse train (0 means aperiodic) IntState
        period = new Parameter(this, "period");
        period.setExpression("0");

        // Non-negative delay on the output (0 means no delay) IntState
        delay = new Parameter(this, "delay");
        delay.setExpression("0");

        // An internal state IntState
        count = new Parameter(this, "count");
        count.setExpression("0");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  Height of the impulse parameter with initial value "1.0".
     */
     public Parameter level;

    /**
     *  Non-negative period of the impulse train (0 means aperiodic) parameter with initial value "0".
     */
     public Parameter period;

    /**
     *  Non-negative delay on the output (0 means no delay) parameter with initial value "0".
     */
     public Parameter delay;

    /**
     *  An internal state parameter with initial value "0".
     */
     public Parameter count;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if (((IntToken)((period).getToken())).intValue() < 0) {
                        throw new IllegalActionException(this, "Period must be non-negative");
                }
                if (((IntToken)((delay).getToken())).intValue() < 0) {
                        throw new IllegalActionException(this, "Delay must be non-negative");
                }
                if (((IntToken)((period).getToken())).intValue() == 0) {
                        count = - ((IntToken)((delay).getToken())).intValue();
                }
                else {
                        count = - (((IntToken)((delay).getToken())).intValue()%((IntToken)((period).getToken())).intValue());
                }
     }

    /**
     */
    public void  generateFireCode() {

addCode(init);
                if (((IntToken)((period).getToken())).intValue() > 0) addCode(periodic);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String init =
        "        /* If count != 0, then output 0.0 else output \"level\" */\n"
        + "        /* Increment count */\n"
        + "        $ref(output) = ($ref(count)++) ? 0.0 : $val(level);\n";

    public String periodic =
        "        /* Reset the counter to zero if one period has elapsed */\n"
        + "        if ($ref(count) >= $val(period)) $ref(count) = 0;\n";
}
