/* Quant, CGC domain: CGCQuant.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCQuant.pl by ptlang
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
//// CGCQuant
/**
Quantizes input to one of N+1 possible output levels using N thresholds.
For an input less than or equal to the n-th threshold, but larger than all
previous thresholds, the output will be the n-th level.  If the input is
greater than all thresholds, the output is the N+1-th level.  If level is
specified, there must be one more level than thresholds; the default
value for level is 0, 1, 2, ... N.

 @Author E. A. Lee and J. Buck
 @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCQuant.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCQuant extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCQuant(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Quantization thresholds, in increasing order FloatArrayState
        thresholds = new Parameter(this, "thresholds");
        thresholds.setExpression("{0.0}");

        // Output levels.  If empty, use 0, 1, 2, ... FloatArrayState
        levels = new Parameter(this, "levels");
        levels.setExpression("{}");

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
     *  Quantization thresholds, in increasing order parameter with initial value "0.0".
     */
     public Parameter thresholds;

    /**
     *  Output levels.  If empty, use 0, 1, 2, ... parameter with initial value "".
     */
     public Parameter levels;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

double x = log(thresholds.size()) / log(2.0);
                return 8 + 5 * int(x-0.01);
     }

    /**
     */
    public void  generatePreinitializeCode() {

addDeclaration(declarations);
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

int n = thresholds.size();
                if (levels.size() == 0) {
                        // default: 0, 1, 2...

                        levels.resize(n + 1);
                        for (int i = 0; i <= n; i++)
                                levels[i] = i;
                }
                else if (levels.size() != n+1) {
                        throw new IllegalActionException(this,
                              "must have one more level than thresholds");
                }
     }

    /**
     */
    public void  generateFireCode() {

addCode (maindecl);
StringBuffer st = new StringBuffer("\t\t$starSymbol(siz) = ");
            st.append(thresholds.size());
            st.append(";\n");
            addCode((String)st);
            addCode (main);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String declarations =
        "            int $starSymbol(siz);\n";

    public String maindecl =
        "                float in;\n"
        + "                int lo, hi, mid;\n";

    public String main =
        "                in = $ref(input);\n"
        + "                lo = 0;\n"
        + "                hi = $starSymbol(siz);\n"
        + "                mid = (hi+lo)/2;\n"
        + "                do {\n"
        + "                        if (in <= $ref2(thresholds,mid)) {\n"
        + "                                hi = mid;\n"
        + "                        } else {\n"
        + "                                lo = mid+1;\n"
        + "                        }\n"
        + "                        mid = (hi+lo)/2;\n"
        + "                } while (mid < $starSymbol(siz) && hi > lo);\n"
        + "\n"
        + "                /* now in is <= thresholds[mid] but > all smaller ones. */\n"
        + "                /* (where thresholds[$starSymbol(siz)] is infinity) */\n"
        + "\n"
        + "                $ref(output) = $ref2(levels,mid);\n";
}
