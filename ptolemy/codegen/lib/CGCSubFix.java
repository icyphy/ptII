/* SubFix, CGC domain: CGCSubFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSubFix.pl by ptlang
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
//// CGCSubFix
/**
Output as a fixed-point number the "pos" input minus all "neg" inputs.

 @Author Juergen Weiss
 @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/stars/CGCSubFix.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSubFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSubFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        pos = new ClassicPort(this, "pos", true, false);
        pos.setTypeEquals(BaseType.FIX);
        neg = new ClassicPort(this, "neg", true, false);
        neg.setMultiport(true);
        neg.setTypeEquals(BaseType.FIX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.FIX);

        // Flag indicating whether or not to use the precision of the arriving particles\n(the \"pos\" input and the \"neg\" inputs) as they are:  YES processes them\nunaltered, and NO casts them to the precision specified by the\nparameter \"InputPrecision\". IntState
        ArrivingPrecision = new Parameter(this, "ArrivingPrecision");
        ArrivingPrecision.setExpression("YES");

        // Precision of the \"neg\" input in bits.\nThe input particles are only cast to this precision if the\nparameter \"ArrivingPrecision\" is set to NO. PrecisionState
        InputPrecision = new Parameter(this, "InputPrecision");
        InputPrecision.setExpression("2.14");

        // Precision of the output in bits.\nThis is the precision that will hold the result of the difference\nof the inputs.\nWhen the value of the accumulation extends outside of the precision,\nthe output is set to its maximum value (or minimum for negative\nmagnitudes). PrecisionState
        OutputPrecision = new Parameter(this, "OutputPrecision");
        OutputPrecision.setExpression("2.14");

        // index for multiple input trace IntState
        index = new Parameter(this, "index");
        index.setExpression("1");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * pos of type fix.
     */
    public ClassicPort pos;

    /**
     * neg of type fix.
     */
    public ClassicPort neg;

    /**
     * output of type fix.
     */
    public ClassicPort output;

    /**
     *  Flag indicating whether or not to use the precision of the arriving particles
(the "pos" input and the "neg" inputs) as they are:  YES processes them
unaltered, and NO casts them to the precision specified by the
parameter "InputPrecision". parameter with initial value "YES".
     */
     public Parameter ArrivingPrecision;

    /**
     *  Precision of the "neg" input in bits.
The input particles are only cast to this precision if the
parameter "ArrivingPrecision" is set to NO. parameter with initial value "2.14".
     */
     public Parameter InputPrecision;

    /**
     *  Precision of the output in bits.
This is the precision that will hold the result of the difference
of the inputs.
When the value of the accumulation extends outside of the precision,
the output is set to its maximum value (or minimum for negative
magnitudes). parameter with initial value "2.14".
     */
     public Parameter OutputPrecision;

    /**
     *  index for multiple input trace parameter with initial value "1".
     */
     public Parameter index;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

super.generateInitializeCode();
                if (((IntToken)((ArrivingPrecision).getToken())).intValue() == 0) // FIXME ArrivingPrecision should be a Boolean{
                    pos.setPrecision(InputPrecision);
                    neg.setPrecision(InputPrecision);
                }
                output.setPrecision(OutputPrecision);
     }

    /**
     */
    public void  generateFireCode() {

// insert code to clear overflow flag
                super.clearOverflow();

                // avoid FIX_Assign if possible
                if (neg.numberPorts() == 1)
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        FIX_Sub($ref(output), $ref(pos),$ref(neg#1));\n"

);          addCode(_str_);  }

                else {
                        // initialize sum
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        FIX_Assign($ref(output),$ref(pos));\n"

);          addCode(_str_);  }

                        for (int i=1; i <= neg.numberPorts(); i++) {
                            index = i;
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        FIX_Sub($ref(output), $ref(output),$ref(neg#index));\n"

);          addCode(_str_);  }
                        }
                }

                // insert code to test overflow flag
                super.checkOverflow();
     }
}
