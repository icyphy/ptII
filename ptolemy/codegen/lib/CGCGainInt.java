/* GainInt, CGC domain: CGCGainInt.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCGainInt.pl by ptlang
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
//// CGCGainInt
/**
   Amplifier: output is input times "gain" (default 1).

   @Author Brian L. Evans Contributor(s): S. Ha
   @Version $Id$, based on version 1.3 of /users/ptolemy/src/domains/cgc/stars/CGCGainInt.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCGainInt extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCGainInt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // Gain of the star. IntState
        gain = new Parameter(this, "gain");
        gain.setExpression("1");

        /*
          noInternalState();
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type int.
     */
    public ClassicPort input;

    /**
     * output of type int.
     */
    public ClassicPort output;

    /**
     *  Gain of the star. parameter with initial value "1".
     */
    public Parameter gain;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

        int time = 1;
        if ( ((IntToken)((gain).getToken())).intValue() == 1 ) time = 0;
        return time;
    }

    /**
     */
    public void  generateFireCode() {

        // Check for simple multiple of 2
        int reg = 0x01;
        int found = false;
        int gainValue = gain;
        int i = -1;
        while (++i < 7) {
            if ( gainValue == reg ) {
                found = true;
                break;
            }
            reg <<= 1;
        }

        StringBuffer code = new StringBuffer("\t$ref(output) = ");
        if ( found ) {
            if ( i == 0 ) code.append("$ref(input)");
            else code.append("$ref(input)  + " + i);
        }
        else {
            code.append("$val(gain) * $ref(input)");
        }
        code.append(";\n");
        addCode(code);
    }
}
