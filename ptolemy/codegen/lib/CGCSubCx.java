/* SubCx, CGC domain: CGCSubCx.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSubCx.pl by ptlang
*/
/*
Copyright (c) 1990-1996 The Regents of the University of California.
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
//// CGCSubCx
/**
Output the "pos" input minus all "neg" inputs.

 @Author Jose Luis Pino
 @Version $Id$, based on version 1.1 of /users/ptolemy/src/domains/cgc/stars/CGCSubCx.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSubCx extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSubCx(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        pos = new ClassicPort(this, "pos", true, false);
        pos.setTypeEquals(BaseType.COMPLEX);
        neg = new ClassicPort(this, "neg", true, false);
        neg.setMultiport(true);
        neg.setTypeEquals(BaseType.COMPLEX);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

/*     
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * pos of type complex.
     */
    public ClassicPort pos;

    /**
     * neg of type complex.
     */
    public ClassicPort neg;

    /**
     * output of type complex.
     */
    public ClassicPort output;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
return 2*(1 + neg.numberPorts());
     }

    /**
     */
    public void  generateFireCode() {
        
addCode(startOp); 
	for (int i = 1; i <= neg.numberPorts(); i++) 
	    addCode(doOp(i)); 
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String startOp = 
        "	$ref(output).real = $ref(pos).real;\n"
        + "	$ref(output).imag = $ref(pos).imag;\n";

    public String doOp (int i) {
        return
        "	$ref(output).real -= $ref(neg#" + i + ").real;\n"
        + "	$ref(output).imag -= $ref(neg#" + i + ").imag;\n";
    }
}
