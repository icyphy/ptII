/* DeMux, CGC domain: CGCDeMux.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCDeMux.pl by ptlang
*/
/*
Copyright (c) 1990-1997 The Regents of the University of California.
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
//// CGCDeMux
/**
Demultiplexes one input onto any number of output streams.
The star consumes B particles from the input, where B is the blockSize.
These B particles are copied to exactly one output,
determined by the "control" input.  The other outputs get a zero of the
appropriate type.

Integers from 0 through N-1 are accepted at the "control"
input, where N is the number of outputs.  If the control input is
outside this range, all outputs get zero.
<p>
<a name="demultiplex"></a>

 @Author S. Ha
 @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/CGCDeMux.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCDeMux extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCDeMux(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        control = new ClassicPort(this, "control", true, false);
        control.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setMultiport(true);

        // Number of particles in a block. IntState
        blockSize = new Parameter(this, "blockSize");
        blockSize.setExpression("1");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type anytype.
     */
    public ClassicPort input;

    /**
     * control of type int.
     */
    public ClassicPort control;

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

return 1 + (output.numberPorts() + 1) * ((IntToken)((blockSize).getToken())).intValue();
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if ( ((IntToken)((blockSize).getToken())).intValue() < 1 ) {
			throw new IllegalActionException(this, "blockSize must be positive");
			return;
		}
		input.setSDFParams(((IntToken)((blockSize).getToken())).intValue(),((IntToken)((blockSize).getToken())).intValue()-1);
		output.setSDFParams(((IntToken)((blockSize).getToken())).intValue(),((IntToken)((blockSize).getToken())).intValue()-1);
     }

    /**
     */
    public void  generateFireCode() {

addCode(init);
		addCode(blockIterator);
		addCode("\t{\n");
		// control value i means port number i+1
		for (int i = 0; i < output.numberPorts(); i++) {
		  if (input.resolvedType() == COMPLEX)
		    addCode(complexCopyData(i,i+1));
		  else
		    addCode(nonComplexCopyData(i,i+1));
		}
		addCode("\t}\n");
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String init =
        "	int n = $ref(control);\n"
        + "	int j = $val(blockSize);\n";

    public String nonComplexCopyData (int i, int portnum) {
        return
        "		/* Output port #" + portnum + " */\n"
        + "		if (n != " + i + ") $ref(output#" + portnum + ",j) = 0;\n"
        + "		else $ref(output#" + portnum + ",j) = $ref(input,j);\n";
    }

    public String complexCopyData (int i, int portnum) {
        return
        "		/* Output port #" + portnum + " */\n"
        + "		if (n != " + i + ") {\n"
        + "		  $ref(output#" + portnum + ",j).real = 0;\n"
        + "		  $ref(output#" + portnum + ",j).imag = 0;\n"
        + "		}\n"
        + "		else $ref(output#" + portnum + ",j) = $ref(input,j);\n";
    }

    public String blockIterator =
        "	while (j--)\n";
}
