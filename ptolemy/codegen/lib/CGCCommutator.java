/* Commutator, CGC domain: CGCCommutator.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCCommutator.pl by ptlang
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
//// CGCCommutator
/**
Takes N input streams (where N is the number of inputs) and
synchronously combines them into one output stream.
It consumes B input particles from each
input (where B is the blockSize), and produces N*B particles on the
output.
The first B particles on the output come from the first input,
the next B particles from the next input, etc.

 @Author E. A. Lee
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCCommutator.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCCommutator extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCCommutator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        output = new ClassicPort(this, "output", false, true);

        // Number of particles in a block. IntState
        blockSize = new Parameter(this, "blockSize");
        blockSize.setExpression("1");

/* 
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type anytype.
     */
    public ClassicPort input;

    /**
     * output of type anytype.
     */
    public ClassicPort output;

    /**
     *  Number of particles in a block. parameter with initial value "1".
     */
     public Parameter blockSize;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
return ((IntToken)((blockSize).getToken())).intValue()*2*input.numberPorts();
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {
        
int n = input.numberPorts();
		input.setSDFParams(((IntToken)((blockSize).getToken())).intValue(),((IntToken)((blockSize).getToken())).intValue()-1);
		output.setSDFParams(n*((IntToken)((blockSize).getToken())).intValue(),n*((IntToken)((blockSize).getToken())).intValue()-1);
     }

    /**
     */
    public void  generateFireCode() {
        
StringBuffer out = new StringBuffer();
		if (((IntToken)((blockSize).getToken())).intValue() > 1) out.append("\tint j;\n");
		for (int i = input.numberPorts()-1; i >= 0; i--) {
		    int port = input.numberPorts() - i;
		    if (((IntToken)((blockSize).getToken())).intValue() > 1) {
			out.append("\tfor (j = ");
			out.append(((IntToken)((blockSize).getToken())).intValue()-1);
			out.append("; j >= 0; j--)\n");
			out.append("\t\t$ref2(output,j+");
			out.append(i*((IntToken)((blockSize).getToken())).intValue());
			out.append(") = $ref2(input#" + port  + ",j");
		   } else {
			out.append("\t$ref2(output,");
			out.append(i);
			out.append(") = $ref2(input#" + port  + ",0");
		   }
		   out.append(");\n");
		   addCode(out); 
		   out.initialize();
		}
     }
}
