/* Trainer, CGC domain: CGCTrainer.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCTrainer.pl by ptlang
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
//// CGCTrainer
/**
Passes the "train" input to the output for the first "trainLength"
samples, then passes the "decision" input to the output.  Designed
for use in decision feedback equalizers, but can be used for other
purposes.
<p>
<a name="DFE training"></a>
<a name="decision feedback equalizer training"></a>
<a name="equalizer, decision feedback, training"></a>

 @Author S. Ha
 @Version $Id$, based on version 1.8 of /users/ptolemy/src/domains/cgc/stars/CGCTrainer.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCTrainer extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCTrainer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Number of training samples to use IntState
        trainLength = new Parameter(this, "trainLength");
        trainLength.setExpression("100");
        train = new ClassicPort(this, "train", true, false);
        decision = new ClassicPort(this, "decision", true, false);
        output = new ClassicPort(this, "output", false, true);

        // local variable for counting inputs IntState
        count = new Parameter(this, "count");
        count.setExpression("0");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     *  Number of training samples to use parameter with initial value "100".
     */
     public Parameter trainLength;

    /**
     * train of type anytype.
     */
    public ClassicPort train;

    /**
     * decision of type anytype.
     */
    public ClassicPort decision;

    /**
     * output of type anytype.
     */
    public ClassicPort output;

    /**
     *  local variable for counting inputs parameter with initial value "0".
     */
     public Parameter count;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

return 1;
     }

    /**
     */
    public void  generatePreinitializeCode() {

if (train.resolvedType() == FIX) {
                        super.initCode();
                }
     }

    /**
     */
    public void  begin() {

// handle precisions for fix types
                if (train.resolvedType() == FIX) {

                        // if the precision for the output port is not defined
                        // by the successor star, the precisions of the input
                        // ports are passed through to the output ports

                        if (!output.precision().isValid())
                                output.setAttributes(A_VARPREC);
                }
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

count = 0;
     }

    /**
     */
    public void  generateFireCode() {

// check for fix types
                if (train.resolvedType() == FIX) {

                        // if we use variable precision representation,
                        // set the precision of the output port from
                        // the source
                        if (output.attributes() & AB_VARPREC) {

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        if ($ref(count) < $val(trainLength)) {\n"
"                $precision(output).len  = FIX_GetLength($ref(train));\n"
"                $precision(output).intb = FIX_GetIntBits($ref(train));\n"
"                FIX_Assign($ref(output),$ref(train));\n"
"                $ref(count)++;\n"
"        } else {\n"
"                $precision(output).len  = FIX_GetLength($ref(decision));\n"
"                $precision(output).intb = FIX_GetIntBits($ref(decision));\n"
"                FIX_Assign($ref(output),$ref(decision));\n"
"        }\n"

);          addCode(_str_);  }

                        } else {

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        if ($ref(count) < $val(trainLength)) {\n"
"                FIX_Assign($ref(output),$ref(train));\n"
"                $ref(count)++;\n"
"        } else {\n"
"                FIX_Assign($ref(output),$ref(decision));\n"
"        }\n"

);          addCode(_str_);  }

                        }

                } else {

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        if ($ref(count) < $val(trainLength)) {\n"
"                $ref(output) = $ref(train);\n"
"                $ref(count)++;\n"
"        } else {\n"
"                $ref(output) = $ref(decision);\n"
"        }\n"

);          addCode(_str_);  }

                }
     }
}
