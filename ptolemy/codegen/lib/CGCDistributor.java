/* Distributor, CGC domain: CGCDistributor.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCDistributor.pl by ptlang
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
//// CGCDistributor
/**
Takes one input stream and synchronously splits it into N output streams,
where N is the number of outputs.  It consumes N*B input particles,
where B = blockSize, and sends the first B particles to the first output,
the next B particles to the next output, etc.

 @Author E. A. Lee
 @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCDistributor.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCDistributor extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCDistributor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        output = new ClassicPort(this, "output", false, true);
        output.setMultiport(true);

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

return ((IntToken)((blockSize).getToken())).intValue()*2*output.numberPorts();
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

int n = output.numberPorts();
		input.setSDFParams(n*((IntToken)((blockSize).getToken())).intValue(),n*((IntToken)((blockSize).getToken())).intValue()-1);
		output.setSDFParams(((IntToken)((blockSize).getToken())).intValue(),((IntToken)((blockSize).getToken())).intValue()-1);
     }

    /**
     */
    public void  generateFireCode() {

StringBuffer out = new StringBuffer();
		if (((IntToken)((blockSize).getToken())).intValue() > 1) out.append("\tint j;\n");
		for (int i = output.numberPorts() - 1; i >= 0; i--) {
		   int port = output.numberPorts() - i;
		   if (((IntToken)((blockSize).getToken())).intValue() > 1) {
			out  + "\tfor (j = " + ((IntToken)((blockSize).getToken())).intValue()-1
			     + "; j >= 0; j--)\n" + "\t\t$ref2(output#"
			    << port  + ",j) = $ref2(input,j+"
			   .append(i*((IntToken)((blockSize).getToken())).intValue()  + ");\n");
		   } else {
			out  + "\t$ref2(output#" + port
			   .append(",0) = $ref2(input," + i  + ");\n");
		   }
		}
		addCode(out);
     }
}
