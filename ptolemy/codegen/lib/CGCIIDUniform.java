/* IIDUniform, CGC domain: CGCIIDUniform.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCIIDUniform.pl by ptlang
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
//// CGCIIDUniform
/**
Generate random variables that are approximately independent identically
distributed uniform random variables.  The values range from "lower"
to "upper".

 @Author Soonhoi Ha
 @Version $Id$, based on version 1.10 of /users/ptolemy/src/domains/cgc/stars/CGCIIDUniform.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCIIDUniform extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCIIDUniform(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // lower limit of uniform random number generator FloatState
        lower = new Parameter(this, "lower");
        lower.setExpression("0.0");

        // upper limit of uniform random number generator FloatState
        upper = new Parameter(this, "upper");
        upper.setExpression("1.0");

        // seed IntState
        seed = new Parameter(this, "seed");
        seed.setExpression("1");

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
     *  lower limit of uniform random number generator parameter with initial value "0.0".
     */
     public Parameter lower;

    /**
     *  upper limit of uniform random number generator parameter with initial value "1.0".
     */
     public Parameter upper;

    /**
     *  seed parameter with initial value "1".
     */
     public Parameter seed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

return 10 + 5;        /* based on CG96IIDUniform */
     }

    /**
     */
    public void  generatePreinitializeCode() {

// Pull in prototypes for srand and rand
                addInclude("<stdlib.h>");
                // Initialize the random number generator
                addCode(initSeed);
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if ( ((DoubleToken)((lower).getToken())).doubleValue() > ((DoubleToken)((upper).getToken())).doubleValue() ) {
                    throw new IllegalActionException(this, "The upper limit must be greater ",
                                    "than the lower limit");
                    return;
                }
     }

    /**
     */
    public void  generateFireCode() {

addCode(randomGen);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String initSeed =
        "/* Initialize the random number generator */\n"
        + "srand($val(seed));\n";

    public String randomGen =
        "                /* Generate a random number on the interval [0,1] and */\n"
        + "                /* map it into the interval [$val(lower),$val(upper)] */\n"
        + "                double randomValue = 0.0;\n"
        + "                int randomInt = rand();\n"
        + "                double scale = $val(upper) - ($val(lower));\n"
        + "                double center = ($val(upper) + ($val(lower)))/2.0;\n"
        + "\n"
        + "                /* RAND_MAX is an ANSI C standard constant */\n"
        + "                /* If not defined, then just use the lower 15 bits */\n"
        + "#ifdef RAND_MAX\n"
        + "                randomValue = ((double) randomInt) / ((double) RAND_MAX);\n"
        + "#else\n"
        + "                randomInt &= 0x7FFF;\n"
        + "                randomValue = ((double) randomInt) / 32767.0;\n"
        + "#endif\n"
        + "                $ref(output) = scale * (randomValue - 0.5) + center;\n";
}
