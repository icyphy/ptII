/* MpyCx, CGC domain: CGCMpyCx.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCMpyCx.pl by ptlang
*/
/*
Copyright (c) 1990-1996 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty provisions.
 */
package ptolemy.domains.sdf.cgc.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCMpyCx
/**
Output the complex product of the inputs.

 @Author Jose Luis Pino
 @Version $Id$, based on version 1.3 of /users/ptolemy/src/domains/cgc/stars/CGCMpyCx.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCMpyCx extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCMpyCx(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.COMPLEX);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

/*     //# line 22 "/users/ptolemy/src/domains/cgc/stars/CGCMpyCx.pl"
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type complex.
     */
    public TypedIOPort input;

    /**
     * output of type complex.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        //# line 46 "/users/ptolemy/src/domains/cgc/stars/CGCMpyCx.pl"
        //return 4*input.numberPorts();
        // Dummy return value.
        return 0;
     }

    /**
     */
    public void  go() {
        //# line 25 "/users/ptolemy/src/domains/cgc/stars/CGCMpyCx.pl"
        //addCode(startOp(input.numberPorts()));
        //	int i;
        //	for (i=2;i<=input.numberPorts();i++) 
        //	    addCode(doOp(i));
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String startOp (int i) {
        return
        "	" + (i>1?"complex temp;":"/*Unit gain - no multiplication required*/") + "\n"
        + "	$ref(output).real = $ref(input#1).real;\n"
        + "	$ref(output).imag = $ref(input#1).imag;\n";
    }

    public String doOp (int i) {
        return
        "	temp.real = $ref(output).real * $ref(input#" + i + ").real -\n"
        + "		    $ref(output).imag * $ref(input#" + i + ").imag;\n"
        + "	temp.imag = $ref(output).real * $ref(input#" + i + ").imag +\n"
        + "		    $ref(output).imag * $ref(input#" + i + ").real;\n"
        + "	$ref(output).real = temp.real;\n"
        + "	$ref(output).imag = temp.imag;\n";
    }
}
