/* Add, CGC domain: CGCAdd.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCAdd.pl by ptlang
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
//// CGCAdd
/**
Output the sum of the inputs, as a floating value.

 @Author S. Ha
 @Version $Id$, based on version 1.8 of /users/ptolemy/src/domains/cgc/stars/CGCAdd.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCAdd extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCAdd(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

/*     //# line 23 "/users/ptolemy/src/domains/cgc/stars/CGCAdd.pl"
noInternalState();
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

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        //# line 37 "/users/ptolemy/src/domains/cgc/stars/CGCAdd.pl"
return input.numberPorts();
     }

    /**
     */
    public void  generateFireCode() {
        //# line 26 "/users/ptolemy/src/domains/cgc/stars/CGCAdd.pl"
StringList out = "\t$ref(output) = ";
		for (int i = 1; i <= input.numberPorts(); i++) {
			out << "$ref(input#" << i << ")";
			if (i < input.numberPorts())
			  out << " + ";
			else
			  out << ";\n";
		}
		addCode(out);
     }
}
