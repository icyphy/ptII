/* IIDGaussian, CGC domain: CGCIIDGaussian.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCIIDGaussian.pl by ptlang
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
//// CGCIIDGaussian
/**
   Generate random variables that are approximately independent identically
   distributed Gaussian random variables.  The values range from "lower"
   to "upper".

   @Author Soonhoi Ha
   @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/CGCIIDGaussian.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCIIDGaussian extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCIIDGaussian(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // variance FloatState
        var = new Parameter(this, "var");
        var.setExpression("1.0");

        // mean FloatState
        mean = new Parameter(this, "mean");
        mean.setExpression("0.0");

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
     *  variance parameter with initial value "1.0".
     */
    public Parameter var;

    /**
     *  mean parameter with initial value "0.0".
     */
    public Parameter mean;

    /**
     *  seed parameter with initial value "1".
     */
    public Parameter seed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

        return 300;
    }

    /**
     */
    public void  generatePreinitializeCode() {

        // Declare prototypes for srand and rand functions
        addInclude("<stdlib.h>");
        // Declare prototype for sqrt
        addInclude("<math.h>");
        // Initialize the random number generator
        addCode(initSeed);
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
    "                int i;\n"
    + "                double sum = 0.0;\n"
    + "                /* Sum a large number of zero-mean random numbers that  */\n"
    + "                /* are uniformly distributed on the interval [-0.5,0.5] */\n"
    + "                /* to approximate a Gaussian distribution                */\n"
    + "                for (i = 0; i < 27; i++) {\n"
    + "                    /* RAND_MAX is an ANSI C standard constant */\n"
    + "                    /* If not defined, then just use the lower 15 bits */\n"
    + "                    double randomValue = 0.0;\n"
    + "                    int randomInt = rand();\n"
    + "#ifdef RAND_MAX\n"
    + "                    randomValue = ((double) randomInt) / ((double) RAND_MAX);\n"
    + "#else\n"
    + "                    randomInt &= 0x7FFF;\n"
    + "                    randomValue = ((double) randomInt) / 32767.0;\n"
    + "#endif\n"
    + "                    sum += (randomValue - 0.5);\n"
    + "                }\n"
    + "                if ($val(var) != 1.0) sum *= sqrt($val(var));\n"
    + "                $ref(output) = (2.0/3.0) * sum + $val(mean);\n";
}
